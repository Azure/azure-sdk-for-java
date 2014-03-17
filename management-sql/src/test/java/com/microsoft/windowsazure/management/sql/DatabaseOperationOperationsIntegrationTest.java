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
import com.microsoft.windowsazure.management.sql.models.DatabaseOperation;
import com.microsoft.windowsazure.management.sql.models.DatabaseOperationGetResponse;
import com.microsoft.windowsazure.management.sql.models.DatabaseOperationListResponse;
import com.microsoft.windowsazure.management.sql.models.ServerCreateParameters;
import com.microsoft.windowsazure.management.sql.models.ServerCreateResponse;

public class DatabaseOperationOperationsIntegrationTest extends SqlManagementIntegrationTestBase {
	
	private static Map<String, String> databaseToBeRemoved = new HashMap<String, String>();
	private static List<String> serverToBeRemoved = new ArrayList<String>();
	private static DatabaseOperations databaseOperations;
	private static ServerOperations serverOperations;
	private static DatabaseOperationOperations databaseOperationOperations;
	private static String testAdministratorPasswordValue = "testAdminPassword!8";
	private static String testAdministratorUserNameValue = "testadminuser";
	private static String testLocationValue = "West US";
	
	@Before
	public void setup() throws Exception
	{
		createService();
		databaseOperations = sqlManagementClient.getDatabasesOperations();
		serverOperations = sqlManagementClient.getServersOperations();
		databaseOperationOperations = sqlManagementClient.getDatabaseOperationsOperations();
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
	
	private String createDatabase(String serverName) throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException 
	{
    	String expectedCollationName = "SQL_Latin1_General_CP1_CI_AS";
    	String expectedEdition = "Web";
    	String expectedDatabaseName = "expecteddatabasename";
    	int expectedMaxSizeInGB = 5; 
    	
    	DatabaseCreateParameters databaseCreateParameters = new DatabaseCreateParameters();
    	databaseCreateParameters.setName(expectedDatabaseName);
    	databaseCreateParameters.setCollationName(expectedCollationName);
    	databaseCreateParameters.setEdition(expectedEdition);
    	databaseCreateParameters.setMaximumDatabaseSizeInGB(expectedMaxSizeInGB);
    	DatabaseCreateResponse databaseCreateResponse = databaseOperations.create(serverName, databaseCreateParameters);
    	databaseToBeRemoved.put(databaseCreateResponse.getDatabase().getName(), serverName);
    	return databaseCreateResponse.getDatabase().getName();
	}
	
    @Test
    public void listDatabaseOperationsOperationSuccess() throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException 
    {
    	// arrange 
    	String serverName = createServer();
    	createDatabase(serverName);
    	
    	// act 
    	DatabaseOperationListResponse databaseOperationOperationsListResponse = databaseOperationOperations.listByServer(serverName);
    	
    	// assert
    	assertEquals(1, databaseOperationOperationsListResponse.getDatabaseOperations().size());
    	
    }
    
    @Test
    public void getDatabaseOperationsOperationSuccess() throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException 
    {
    	// arrange 
    	String serverName = createServer();
    	createDatabase(serverName);
    	
    	// act 
    	DatabaseOperationListResponse databaseOperationOperationsListResponse = databaseOperationOperations.listByServer(serverName);
    	DatabaseOperation databaseOperation = databaseOperationOperationsListResponse.getDatabaseOperations().get(0);
    	DatabaseOperationGetResponse databaseOperationGetResponse = databaseOperationOperations.get(serverName, databaseOperation.getId());
    	DatabaseOperation actualDatabaseOperation = databaseOperationGetResponse.getDatabaseOperation();
    	
    	
    	// assert
    	assertEquals(databaseOperation, actualDatabaseOperation);
    }   
    
}