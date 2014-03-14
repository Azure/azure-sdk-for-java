/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.management.sql;


import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.*;
import org.xml.sax.SAXException;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.sql.models.DatabaseCreateParameters;
import com.microsoft.windowsazure.management.sql.models.DatabaseCreateResponse;
import com.microsoft.windowsazure.management.sql.models.DatabaseListResponse;
import com.microsoft.windowsazure.management.sql.models.DatabaseListResponse.Database;
import com.microsoft.windowsazure.management.sql.models.ServerCreateParameters;
import com.microsoft.windowsazure.management.sql.models.ServerCreateResponse;

public class DatabaseOperationsIntegrationTest extends SqlManagementIntegrationTestBase {
	
	private static Map<String, String> databaseToBeRemoved = new HashMap<String, String>();
	private static List<String> serverToBeRemoved = new ArrayList<String>();
	private static DatabaseOperations databaseOperations = sqlManagementClient.getDatabasesOperations();
	private static ServerOperations serverOperations = sqlManagementClient.getServersOperations();
	private static String testAdministratorPasswordValue = "testAdminPassword!8";
	private static String testAdministratorUserNameValue = "testadminuser";
	private static String testLocationValue = "West US";
	
	@Before
	public void setup() throws Exception
	{
		createService();
		databaseOperations = sqlManagementClient.getDatabasesOperations();
		serverOperations = sqlManagementClient.getServersOperations();
	}
	
	@After
	public void tearDown() throws Exception 
	{
        for (String databaseName : databaseToBeRemoved.keySet())
        {
        	String serverName = databaseToBeRemoved.get(databaseName);
        	databaseOperations.delete(serverName, databaseName);
        }
        
        for (String serverName : serverToBeRemoved)
        {
        	serverOperations.delete(serverName);
        }
	}
	
	private String createServer() throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException
	{
		ServerCreateParameters serverCreateParameters = new ServerCreateParameters();
		serverCreateParameters.setAdministratorPassword(testAdministratorPasswordValue);
		serverCreateParameters.setAdministratorUserName(testAdministratorUserNameValue);
		serverCreateParameters.setLocation(testLocationValue);
		ServerCreateResponse serverCreateResponse = serverOperations.create(serverCreateParameters);
		String serverName = serverCreateResponse.getServerName();
		serverToBeRemoved.add(serverName);
		return serverName; 
	
	}
	
    @Test
    public void createDatabaseWithRequiredParameters() throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException 
    {
    	// arrange 
    	String expectedServerName = createServer();
    	
    	// act 
    	DatabaseCreateParameters databaseCreateParameters = new DatabaseCreateParameters();
    	DatabaseCreateResponse databaseCreateResponse = databaseOperations.create(expectedServerName, databaseCreateParameters);
    	databaseToBeRemoved.put(expectedServerName, databaseCreateResponse.getName());
    	
    	// assert
    	assertNotNull(databaseCreateResponse);
    }
    
    @Test
    public void createDatabaseWithOptionalParameters() throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException 
    {
    	// arrange 
    	long expectedMaximumDatabaseSizeInGBValue = 15; 
    	String expectedDatabaseName = "expecteddatabasename";
    	String expectedServerName = createServer();
    	
    	// act 
    	DatabaseCreateParameters databaseCreateParameters = new DatabaseCreateParameters();
    	databaseCreateParameters.setMaximumDatabaseSizeInGB(expectedMaximumDatabaseSizeInGBValue);
    	databaseCreateParameters.setName(expectedDatabaseName);
    	DatabaseCreateResponse databaseCreateResponse = databaseOperations.create(expectedServerName, databaseCreateParameters);
    	
    	
    	// assert
    	assertEquals(expectedDatabaseName, databaseCreateResponse.getName());
    	assertEquals(expectedMaximumDatabaseSizeInGBValue, databaseCreateResponse.getMaximumDatabaseSizeInGB());
        
    }
    
    @Test
    public void deleteDatabaseSuccess() throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException
    {
    	// arrange 
    	String serverName = createServer();
    	
    	// act 
    	DatabaseCreateParameters databaseCreateParameters = new DatabaseCreateParameters();
    	DatabaseCreateResponse databaseCreateResponse = databaseOperations.create(serverName, databaseCreateParameters);
    	String databaseName = databaseCreateResponse.getName();
    	databaseToBeRemoved.put(serverName, databaseName);
    	databaseOperations.delete(serverName, databaseName);
    	
    	// assert
    	DatabaseListResponse databaseListResponse = databaseOperations.list(serverName);
    	ArrayList<Database> databaseList = databaseListResponse.getDatabases();
    	for (Database database : databaseList)
    	{
    		assertNotEquals(databaseName, database.getName());
    	}

    }
    
    
}