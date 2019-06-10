package com.kicksolutions.mock;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kicksolutions.mock.vo.MockResponse;

import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;

/**
 * 
 * @author MSANTOSH Name: com.kicksolutions.mock.MockSwaggerServlet
 */
public class MockSwaggerServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4442214296310544250L;
	private MockSwaggerUtil mockSwaggerUtil;
	private static final Logger LOGGER = Logger.getLogger(MockSwaggerServlet.class.getName());

	@Override
	public void init() throws ServletException {
		String definitionsPath = System.getProperty("swaggerLocation");
		boolean mockSucessResponses = Boolean.parseBoolean(System.getProperty("mockSuccessResponsesOnly", "false"));

		if (StringUtils.isNotEmpty(definitionsPath)) {
			mockSwaggerUtil = MockSwaggerUtil.getInstance(definitionsPath, mockSucessResponses, true);
		} else {
			throw new RuntimeException("Cannot Initialize MockSwaggerServlet");
		}
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getMethod().equalsIgnoreCase("PATCH")) {
			doPatch(request, response);
		} else {
			super.service(request, response);
		}
	}

	protected void doPatch(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processMockRequest(request, response, "PATCH");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processMockRequest(req, resp, "GET");
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processMockRequest(req, resp, "DELETE");
	}

	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processMockRequest(req, resp, "HEAD");
	}

	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processMockRequest(req, resp, "OPTIONS");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processMockRequest(req, resp, "POST");
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processMockRequest(req, resp, "PUT");
	}

	/**
	 * 
	 * @param req
	 * @param resp
	 * @param method
	 * @throws IOException
	 * @throws JsonProcessingException
	 */
	private void processMockRequest(HttpServletRequest req, HttpServletResponse resp, String method)
			throws IOException, JsonProcessingException {
		String contentType = req.getContentType();
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf-8");
		
		if (!StringUtils.isNotEmpty(contentType)){
			contentType = "application/json"; // If No Content Type was set
		}		

		if (StringUtils.isNotEmpty(contentType) && contentType.equalsIgnoreCase("application/json")) {

			LOGGER.log(Level.INFO, "Processing Request for " + req.getRequestURI() + " Method: " + method);

			try {
				if (areAllRequiredParametersPopulated(req, method, req.getRequestURI())) {

					MockResponse response = mockSwaggerUtil.getRandomResponse(req.getRequestURI(), method);

					if (response != null) {
						LOGGER.log(Level.FINEST, response.getResponseCode());
						LOGGER.log(Level.FINEST, response.getMessage());

						String responseCode = response.getResponseCode();

						if (responseCode.equalsIgnoreCase("default") || (!responseCode.equalsIgnoreCase("default")
								&& Integer.parseInt(responseCode) >= 400)) {
							if (responseCode.equalsIgnoreCase("default")) {
								resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
								resp.getWriter().write(new ObjectMapper().writeValueAsString(prepareException(StringUtils.isNotEmpty(response.getMessage()) ? response.getMessage(): "Error Occured",HttpServletResponse.SC_INTERNAL_SERVER_ERROR, req.getRequestURI())));
							} else {
								resp.setStatus(Integer.parseInt(responseCode));
								resp.getWriter()
								.write(new ObjectMapper().writeValueAsString(prepareException(StringUtils.isNotEmpty(response.getMessage()) ? response.getMessage(): "Error Occured",Integer.parseInt(responseCode), req.getRequestURI())));
							}
							
						} else {
							resp.setStatus(Integer.parseInt(responseCode));

							if (response.getExample() != null) {

								if (response.getExample() instanceof String) {
									LOGGER.log(Level.INFO, "String Response" + response.getExample());
									resp.getWriter().write((String) response.getExample());
								} else if (response.getExample() instanceof Map) {
									LOGGER.log(Level.INFO, "Map Response" + response.getExample());
									String json = new ObjectMapper().writeValueAsString((Map) response.getExample());
									resp.getWriter().write(json);
								}
							}
						}
					} else {
						LOGGER.log(Level.SEVERE, "Unable to Process");
						resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
						resp.getWriter()
								.write(new ObjectMapper().writeValueAsString(prepareException("No Mock Response Found",
										HttpServletResponse.SC_INTERNAL_SERVER_ERROR, req.getRequestURI())));
					}
				} else {
					LOGGER.log(Level.SEVERE, "Mandatory Parameters are not Populated");
					resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					resp.getWriter()
							.write(new ObjectMapper().writeValueAsString(prepareException("Mandatory Parameters are not Populated",
									HttpServletResponse.SC_BAD_REQUEST, req.getRequestURI())));
				}
			} catch (MockException ex) {
				LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
				resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
				resp.getWriter().write(new ObjectMapper().writeValueAsString(prepareException(ex.getMessage(),
						HttpServletResponse.SC_METHOD_NOT_ALLOWED, req.getRequestURI())));
			}
		} else {
			LOGGER.log(Level.SEVERE, "Content Type is Not Set or Was Set Other than application/json");
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter()
					.write(new ObjectMapper().writeValueAsString(
							prepareException("Content Type is Not Set or Was Set Other than application/json",
									HttpServletResponse.SC_BAD_REQUEST, req.getRequestURI())));
		}
	}

	/**
	 * 
	 * @param req
	 * @param method
	 * @param URI
	 * @return
	 */
	private boolean areAllRequiredParametersPopulated(HttpServletRequest req, String method, String URI) {
		List<Parameter> parameters = mockSwaggerUtil.getAllRequestParameters(URI, method);

		LOGGER.log(Level.INFO, "Parameters" +req.getParameterMap());
		
		if (parameters != null) {
			for (Parameter requestParameter : parameters) {
				if (requestParameter.getRequired()
						&& StringUtils.isEmpty(req.getParameter(requestParameter.getName())) && (requestParameter instanceof QueryParameter)) {
					
					LOGGER.log(Level.SEVERE, "Mandatory Parameter " +requestParameter.getName() + " is not Populated");
					
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * 
	 * @param e
	 * @return
	 */
	private MockExceptionResponse prepareException(String message, int code, String link) {
		MockExceptionResponse exception = new MockExceptionResponse();
		exception.setCode(String.valueOf(code));
		exception.setLink(link);
		exception.setMessage(message);

		return exception;
	}
}
