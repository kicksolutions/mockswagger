package com.kicksolutions.mock;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.kicksolutions.mock.vo.MockResponse;

import io.swagger.models.Operation;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;

/**
 * 
 * @author MSANTOSH
 *
 */
public class MockSwaggerUtil {

	private static final Logger LOGGER = Logger.getLogger(MockSwaggerUtil.class.getName());

	private static Map<String, Map<String, Object>> swaggerMap = new TreeMap<>();
	private static MockSwaggerUtil INSTANCE = null;
	private String swaggerFolderPath = null;
	private boolean onlySucessResponses = false;
	
	/**
	 * 
	 * @param swaggerFolderPath
	 */
	private MockSwaggerUtil(String swaggerFolderPath,boolean onlySucessResponses) {
		this.swaggerFolderPath = swaggerFolderPath;
		this.onlySucessResponses = onlySucessResponses;
		init();
	}

	/**
	 * 
	 * @param swaggerFolderPath
	 * @return
	 */
	public static MockSwaggerUtil getInstance(String swaggerFolderPath,boolean onlySucessResponses) {
		if (INSTANCE == null) {
			INSTANCE = new MockSwaggerUtil(swaggerFolderPath,onlySucessResponses);
		}

		return INSTANCE;
	}

	/**
	 * 
	 */
	private void init() {
		if (StringUtils.isNotEmpty(swaggerFolderPath)) {
			process(swaggerFolderPath);
		} else {
			LOGGER.log(Level.SEVERE, "No Input Folder Was provided");
		}
	}

	/**
	 * 
	 * @param swaggerFolder
	 */
	private void process(String swaggerFolder) {
		File folder = new File(swaggerFolder);
		if (folder.isDirectory()) {
			File[] listOfFiles = folder.listFiles();

			for (File swaggerDefinition : listOfFiles) {

				if (swaggerDefinition.isFile() && (swaggerDefinition.getName().endsWith("yaml")
						|| swaggerDefinition.getName().endsWith("yml")))

					processSwagger(swaggerDefinition.getAbsolutePath());
			}
		}

		LOGGER.log(Level.INFO, "Map Object" + swaggerMap);
	}

	/**
	 * 
	 * @param swaggerPath
	 */
	private void processSwagger(String swaggerPath) {
		Swagger swaggerObject = new SwaggerParser().read(swaggerPath);

		if (swaggerObject != null) {
			String basePath = swaggerObject.getBasePath();

			Map<String, io.swagger.models.Path> swaggerPaths = swaggerObject.getPaths();

			for (Map.Entry<String, io.swagger.models.Path> entrySet : swaggerPaths.entrySet()) {
				processSwaggerPath(basePath, entrySet.getKey(), entrySet.getValue());
			}
		}
	}

	/**
	 * 
	 * @param basePath
	 * @param path
	 * @param pathObject
	 */
	private void processSwaggerPath(String basePath, String path, io.swagger.models.Path pathObject) {
		String URI = new StringBuilder().append(StringUtils.isNotEmpty(basePath) ? basePath :"").append(path.replaceAll("\\{([^}]+)\\}", "\\\\{([^}]+)\\\\}"))
				.toString();
		populateSwaggerMap(URI, "GET", pathObject.getGet());
		populateSwaggerMap(URI, "POST", pathObject.getPost());
		populateSwaggerMap(URI, "DELETE", pathObject.getDelete());
		populateSwaggerMap(URI, "PATCH", pathObject.getPatch());
		populateSwaggerMap(URI, "PUT", pathObject.getPut());
		populateSwaggerMap(URI, "HEAD", pathObject.getHead());
		populateSwaggerMap(URI, "OPTIONS", pathObject.getOptions());
	}

	/**
	 * 
	 * @param URI
	 * @param getOperation
	 */
	private void populateSwaggerMap(String URI, String method, Operation operation) {
		if (operation != null) {
			Map<String, Object> responseObject = new HashMap<>();
			responseObject.put(method, operation.getResponses());

			if (swaggerMap.containsKey(URI)) {
				responseObject.putAll(swaggerMap.get(URI));
			}

			swaggerMap.put(URI, responseObject);
		}
	}

	/**
	 * 
	 * @param URI
	 * @param method
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public MockResponse getRandomResponse(String URI, String method) {
		for (Map.Entry<String, Map<String, Object>> entrySet : swaggerMap.entrySet()) {

			if (URI.matches(entrySet.getKey())) {
				Map<String, Object> responseObject = entrySet.getValue();
				Map<String, Response> responses = (Map<String, Response>) responseObject.get(method);

				if (responses != null) {
					
					if(onlySucessResponses){
						return getSucessResponse(responses);
					}else{
						return getRandomResponse(responses);
					}
				}else{
					throw new MockException(method,URI);
				}
			}
		}

		throw new MockException(method,URI);
	}

	/**
	 * 
	 * @param responses
	 * @return
	 */
	private MockResponse getSucessResponse(Map<String, Response> responses){
		List<String> keys = new ArrayList<>();
		Random random = new Random();
		
		for (Map.Entry<String, Response> entrySet : responses.entrySet()) {
			if(entrySet.getKey()!=null && !entrySet.getKey().equalsIgnoreCase("default") && Integer.parseInt(entrySet.getKey())<400){
				keys.add(entrySet.getKey());
			}			
		}
		
		if(!keys.isEmpty())
		{
			String sucessCode = keys.get(random.nextInt(keys.size()));
			
			return getRandomExamplesfromResponse(sucessCode,(Response)responses.get(sucessCode));
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param responses
	 * @return
	 */
	private MockResponse getRandomResponse(Map<String, Response> responses) {
		Random random = new Random();
		
		if(responses!=null){
			List<String> keys = new ArrayList<>(responses.keySet());
			String randomKey = keys.get(random.nextInt(keys.size()));
			return getRandomExamplesfromResponse(randomKey, (Response)responses.get(randomKey));
		}
		
		return null;
	}

	/**
	 * 
	 * @param response
	 * @return
	 */
	private MockResponse getRandomExamplesfromResponse(String responseCode, Response response){
		Map<String, Object> examples = response.getExamples();
		MockResponse mockResponse = null;
				
		if(examples!=null){
			Random random = new Random();
			List<String> keys = new ArrayList<>(examples.keySet());
			String randomKey = keys.get(random.nextInt(keys.size()));
			mockResponse =  new MockResponse(responseCode, examples.get(randomKey),response.getDescription());
		}
		else{
			LOGGER.log(Level.WARNING, "No Example Object Set for ResponseCode " +responseCode);
			mockResponse = new MockResponse(responseCode, null, response.getDescription());
		}
		
		return mockResponse;
	}
}