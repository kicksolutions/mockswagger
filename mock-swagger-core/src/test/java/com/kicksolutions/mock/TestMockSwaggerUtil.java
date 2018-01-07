package com.kicksolutions.mock;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import com.kicksolutions.mock.vo.MockResponse;

import junit.framework.TestCase;

public class TestMockSwaggerUtil extends TestCase {

	private String swaggerPath;
	
	@Override
	protected void setUp() throws Exception {
		ClassLoader classLoader = this.getClass().getClassLoader();
        File file = new File(classLoader.getResource("swagger.yaml").getFile());
        if(file!=null)
        {
        	swaggerPath = file.getParent();
        }
	}
	
	public void testGetPetSucessResponse() {
		if(StringUtils.isNotEmpty(swaggerPath)){
			MockSwaggerUtil mockSwaggerUtil = MockSwaggerUtil.getInstance(swaggerPath, true);
			MockResponse expectedObject = mockSwaggerUtil.getRandomResponse("/v2/pet/{id}", "GET");
			
			if(!expectedObject.getResponseCode().equalsIgnoreCase("200") 
					|| !expectedObject.getMessage().equalsIgnoreCase("successful operation")){
				fail("Failed testGetPetSucessResponse !!!");
			}
		}else{
			fail("Cannot Initialize the Util !!");
		}
	}
	
	public void testHeadPetSucessResponse() {
		if(StringUtils.isNotEmpty(swaggerPath)){
			MockSwaggerUtil mockSwaggerUtil = MockSwaggerUtil.getInstance(swaggerPath, true);
			
			try{
				mockSwaggerUtil.getRandomResponse("/v2/pet/{id}", "HEAD");
			}
			catch(MockException ex){
				if(!ex.getMethod().equalsIgnoreCase("HEAD") 
						&& !ex.getURI().equalsIgnoreCase("/v2/pet/{id}")){
					fail("Method Shouldn't be found, some issue");
				}
			}	
			catch (Exception e) {
				fail(e.getMessage());
			}
		}else{
			fail("Cannot Initialize the Util !!");
		}
	}
	
	public void testRandomPetResponse() {
		if(StringUtils.isNotEmpty(swaggerPath)){
			MockSwaggerUtil mockSwaggerUtil = MockSwaggerUtil.getInstance(swaggerPath, false);
			MockResponse expectedObject = mockSwaggerUtil.getRandomResponse("/v2/pet/{id}", "GET");
			
			if(expectedObject==null){
				fail("Failed testGetPetSucessResponse !!!");
			}
		}else{
			fail("Cannot Initialize the Util !!");
		}
	}
	
	public void testNoPath(){
		if(StringUtils.isNotEmpty(swaggerPath)){
			MockSwaggerUtil mockSwaggerUtil = MockSwaggerUtil.getInstance(swaggerPath, true);
			
			try{
				mockSwaggerUtil.getRandomResponse("/v2/pet/{id}/nonExistingPath", "GET");
			}
			catch(MockException ex){
				if(!ex.getMethod().equalsIgnoreCase("GET") 
						&& !ex.getURI().equalsIgnoreCase("/v2/pet/{id}/nonExistingPath")){
					fail("Method Shouldn't be found, some issue");
				}
			}	
			catch (Exception e) {
				fail(e.getMessage());
			}
		}else{
			fail("Cannot Initialize the Util !!");
		}
	}
	
	public void testSlashSucessResponse(){
		if(StringUtils.isNotEmpty(swaggerPath)){
			MockSwaggerUtil mockSwaggerUtil = MockSwaggerUtil.getInstance(swaggerPath, true);
			MockResponse expectedObject = mockSwaggerUtil.getRandomResponse("/example/", "GET");
			
			if(expectedObject==null){
				fail("Failed testGetPetSucessResponse !!!");
			}
		}else{
			fail("Cannot Initialize the Util !!");
		}
	}
	
	public void testV2SucessResponse(){
		if(StringUtils.isNotEmpty(swaggerPath)){
			MockSwaggerUtil mockSwaggerUtil = MockSwaggerUtil.getInstance(swaggerPath, true);
			MockResponse expectedObject = mockSwaggerUtil.getRandomResponse("/example/v2", "GET");
			
			if(expectedObject==null){
				fail("Failed testGetPetSucessResponse !!!");
			}
		}else{
			fail("Cannot Initialize the Util !!");
		}
	}
}