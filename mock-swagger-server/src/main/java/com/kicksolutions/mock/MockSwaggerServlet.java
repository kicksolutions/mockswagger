package com.kicksolutions.mock;

import java.io.IOException;
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
		boolean mockSucessResponses = Boolean.parseBoolean(System.getProperty("mockSucessResponses", "false"));

		if (StringUtils.isNotEmpty(definitionsPath)) {
			mockSwaggerUtil = MockSwaggerUtil.getInstance(definitionsPath, mockSucessResponses,true);
		} else {
			throw new RuntimeException("Cannot Initialize MockSwaggerServlet");
		}
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
	private void processMockRequest(HttpServletRequest req, HttpServletResponse resp,String method)
			throws IOException, JsonProcessingException {
		String contentType = req.getContentType();
		
		if(StringUtils.isNotEmpty(contentType) && contentType.equalsIgnoreCase("application/json")){
			
			LOGGER.log(Level.INFO, "Processing Request for "+ req.getRequestURI() + " Method: "+ method);
			
			try{
				MockResponse response = mockSwaggerUtil.getRandomResponse(req.getRequestURI(), method);
				
				if(response!=null){
					LOGGER.log(Level.FINEST,response.getResponseCode());
					LOGGER.log(Level.FINEST,response.getMessage());
					
					String responseCode = response.getResponseCode();
					
					if(responseCode.equalsIgnoreCase("default") 
							|| (!responseCode.equalsIgnoreCase("default") && Integer.parseInt(responseCode)>=400)){
						resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,StringUtils.isNotEmpty(response.getMessage()) ? response.getMessage() : "Error Occured");
					}
					else{ 
						resp.setContentType("application/json");
						resp.setCharacterEncoding("utf-8");
						resp.setStatus(Integer.parseInt(responseCode));
						
						if(response.getExample()!=null){
							
							if(response.getExample() instanceof String){
								LOGGER.log(Level.INFO, "String Response" + response.getExample());
								resp.getWriter().write((String)response.getExample());
							}
							else if(response.getExample() instanceof Map){
								LOGGER.log(Level.INFO, "Map Response" + response.getExample());
								String json = new ObjectMapper().writeValueAsString((Map)response.getExample());
								resp.getWriter().write(json);
							}
						}
					}
				}
				else{
					LOGGER.log(Level.SEVERE,"Unable to Process");
					resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			}
			catch(MockException ex){
				LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
				resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			}
		}
		else{
			LOGGER.log(Level.SEVERE,"Content Type is Not Set or Was Set Other than application/json");
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
}