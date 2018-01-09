package com.kicksolutions.mock;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriTemplate;

import com.kicksolutions.mock.vo.MockResponse;

import io.swagger.models.Operation;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.parser.SwaggerParser;

/**
 * 
 * @author MSANTOSH
 *
 */
public class MockSwaggerUtil {

	private static final Logger LOGGER = Logger.getLogger(MockSwaggerUtil.class.getName());

	private static Map<String, Map<String, Map<String, Response>>> swaggerResponseMap = new TreeMap<>();
	private static Map<String, Map<String, List<Parameter>>> swaggerRequestMap = new TreeMap<>();
	private static MockSwaggerUtil INSTANCE = null;
	private String swaggerFolderPath = null;
	private boolean onlySucessResponses = false;
	private boolean softDeploy = true;

	/**
	 * 
	 * @param swaggerFolderPath
	 */
	private MockSwaggerUtil(String swaggerFolderPath, boolean onlySucessResponses,boolean softDeploy) {
		this.swaggerFolderPath = swaggerFolderPath;
		this.onlySucessResponses = onlySucessResponses;
		this.softDeploy = softDeploy;
		init();
	}

	/**
	 * 
	 * @param swaggerFolderPath
	 * @return
	 */
	public static MockSwaggerUtil getInstance(String swaggerFolderPath, boolean onlySucessResponses,boolean softDeploy) {
		if (INSTANCE == null) {
			INSTANCE = new MockSwaggerUtil(swaggerFolderPath, onlySucessResponses,softDeploy);
		}

		return INSTANCE;
	}

	/**
	 * 
	 */
	private void init() {
		if (StringUtils.isNotEmpty(swaggerFolderPath)) {
			process(swaggerFolderPath);
			if(softDeploy)
			{
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						watchForChanges();						
					}
				});
			}
		} else {
			LOGGER.log(Level.SEVERE, "No Input Folder Was provided");
		}
	}

	/**
	 * 
	 * @param swaggerFolder
	 */
	private void process(String swaggerFolder) {
		swaggerResponseMap.clear();
		swaggerRequestMap.clear();
		
		File folder = new File(swaggerFolder);
		if (folder.isDirectory()) {
			File[] listOfFiles = folder.listFiles();

			for (File swaggerDefinition : listOfFiles) {
				if (isYamlFile(swaggerDefinition)){
					processSwagger(swaggerDefinition.getAbsolutePath());
				}	
			}
		}

		LOGGER.log(Level.INFO, "Map Object" + swaggerResponseMap);
	}
	
	/**
	 * 
	 * @param swaggerDefinition
	 * @return
	 */
	private boolean isYamlFile(File swaggerDefinition){
		return (swaggerDefinition.isFile() && (swaggerDefinition.getName().endsWith("yaml")
				|| swaggerDefinition.getName().endsWith("yml")));
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
		String URI = new StringBuilder().append(StringUtils.isNotEmpty(basePath) ? basePath : "").append(path)
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
			Map<String, Map<String, Response>> responseObject = new HashMap<>();
			responseObject.put(method, operation.getResponses());

			if (swaggerResponseMap.containsKey(URI)) {
				responseObject.putAll(swaggerResponseMap.get(URI));
			}

			swaggerResponseMap.put(URI, responseObject);
			
			Map<String, List<Parameter>> requestObject = new HashMap<>();
			requestObject.put(method, operation.getParameters());

			if (swaggerRequestMap.containsKey(URI)) {
				requestObject.putAll(swaggerRequestMap.get(URI));
			}
			
			swaggerRequestMap.put(URI, requestObject);
		}
	}
	
	/**
	 * 
	 * @param URI
	 * @param method
	 * @return
	 */
	public List<Parameter> getAllRequestParameters(String URI, String method){
		for (Map.Entry<String, Map<String, List<Parameter>>> entrySet : swaggerRequestMap.entrySet()) {
			if (isURIMatch(URI, entrySet.getKey())) {
				Map<String, List<Parameter>> requestObject = entrySet.getValue();
				return requestObject.get(method);
			}
		}
		
		return null;
	}

	/**
	 * 
	 * @param URI
	 * @param method
	 * @return
	 */
	public MockResponse getRandomResponse(String URI, String method) {
		for (Map.Entry<String, Map<String, Map<String, Response>>> entrySet : swaggerResponseMap.entrySet()) {

			if (isURIMatch(URI, entrySet.getKey())) {
				Map<String, Map<String, Response>> responseObject = entrySet.getValue();
				Map<String, Response> responses = responseObject.get(method);

				if (responses != null) {

					if (onlySucessResponses) {
						return getSucessResponse(responses);
					} else {
						return getRandomResponse(responses);
					}
				} else {
					throw new MockException(method, URI);
				}
			}
		}

		throw new MockException(method, URI);
	}

	/**
	 * 
	 * @param sourceURI
	 * @param targetURI
	 * @return
	 */
	private boolean isURIMatch(String sourceURI, String targetURI) {
		UriTemplate uriTemplate = new UriTemplate(targetURI);
		return uriTemplate.matches(sourceURI);
	}

	/**
	 * 
	 * @param responses
	 * @return
	 */
	private MockResponse getSucessResponse(Map<String, Response> responses) {
		List<String> keys = new ArrayList<>();
		Random random = new Random();

		for (Map.Entry<String, Response> entrySet : responses.entrySet()) {
			if (entrySet.getKey() != null && !entrySet.getKey().equalsIgnoreCase("default")
					&& Integer.parseInt(entrySet.getKey()) < 400) {
				keys.add(entrySet.getKey());
			}
		}

		if (!keys.isEmpty()) {
			String sucessCode = keys.get(random.nextInt(keys.size()));

			return getRandomExamplesfromResponse(sucessCode, (Response) responses.get(sucessCode));
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

		if (responses != null) {
			List<String> keys = new ArrayList<>(responses.keySet());
			String randomKey = keys.get(random.nextInt(keys.size()));
			return getRandomExamplesfromResponse(randomKey, (Response) responses.get(randomKey));
		}

		return null;
	}

	/**
	 * 
	 * @param response
	 * @return
	 */
	private MockResponse getRandomExamplesfromResponse(String responseCode, Response response) {
		Map<String, Object> examples = response.getExamples();
		MockResponse mockResponse = null;

		if (examples != null) {
			Random random = new Random();
			List<String> keys = new ArrayList<>(examples.keySet());
			String randomKey = keys.get(random.nextInt(keys.size()));
			mockResponse = new MockResponse(responseCode, examples.get(randomKey), response.getDescription(),
					"application/json");
		} else {
			LOGGER.log(Level.WARNING, "No Example Object Set for ResponseCode " + responseCode);
			mockResponse = new MockResponse(responseCode, null, response.getDescription(), "application/json");
		}

		return mockResponse;
	}

	private void watchForChanges() {
		Path swaggerFolderPathObj = Paths.get(swaggerFolderPath);

		try {
			WatchService watcher = swaggerFolderPathObj.getFileSystem().newWatchService();
			swaggerFolderPathObj.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY);
			
			boolean valid = true;
			
			do{
				WatchKey watckKey = watcher.take();

				List<WatchEvent<?>> events = watckKey.pollEvents();
				for (WatchEvent event : events) {
					if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
						process(swaggerFolderPath);
					}
					if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
						process(swaggerFolderPath);
					}
					if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
						process(swaggerFolderPath);
					}
				}
				valid = watckKey.reset();
			}while(valid);
			
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}
}