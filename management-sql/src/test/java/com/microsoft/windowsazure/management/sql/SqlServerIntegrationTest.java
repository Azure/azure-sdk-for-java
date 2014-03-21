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


import com.microsoft.windowsazure.management.sql.models.Server;
import com.microsoft.windowsazure.management.sql.models.ServerCreateParameters;
import com.microsoft.windowsazure.management.sql.models.ServerCreateResponse;
import com.microsoft.windowsazure.management.sql.models.ServerListResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.*;
import org.xml.sax.SAXException;
import static org.junit.Assert.*;

import com.microsoft.windowsazure.exception.ServiceException;

public class SqlServerIntegrationTest extends SqlManagementIntegrationTestBase {

    private static List<String> serverToBeRemoved = new ArrayList<String>();
    private static ServerOperations serverOperations;
	
    @Before
    public void setup() throws Exception
    {
        createService();
	serverOperations = sqlManagementClient.getServersOperations();
    }
	
    @After
    public void tearDown() throws Exception 
    {
        for (String serverName : serverToBeRemoved)
        {
            serverOperations.delete(serverName);
        }
        serverToBeRemoved.clear();
    }
	
    @Test
    public void createSqlServerWithRequiredParameters() throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException 
    {
    	//arrange 
    	String testAdministratorUserName = "testadminname";
    	String testPassword = "testpassword8!";
    	String testLocation = "West US";
    	
    	// act
    	ServerCreateParameters serverCreateParameters = new ServerCreateParameters();
    	serverCreateParameters.setAdministratorUserName(testAdministratorUserName);
    	serverCreateParameters.setAdministratorPassword(testPassword);
    	serverCreateParameters.setLocation(testLocation);
        ServerCreateResponse serverCreateResponse = serverOperations.create(serverCreateParameters);
        String serverName = serverCreateResponse.getServerName();
        serverToBeRemoved.add(serverName);
        
        //assert
        
        ServerListResponse serverListResponse = serverOperations.list();
        Iterator<Server> serverList = serverListResponse.iterator();
        Server createdServer = null;
        while (serverList.hasNext())
        {
            Server nextServer = serverList.next();
            if (nextServer.getName().equals(serverName))
            {
            	createdServer = nextServer;
            }
        }
        assertNotNull(createdServer);
        assertEquals(testAdministratorUserName, createdServer.getAdministratorUserName());
        assertEquals(testLocation, createdServer.getLocation());
        
    }
    
    @Test
    public void deleteServerSuccess() throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException
    {
    	//arrange 
    	String testAdministratorUserName = "testadminname";
    	String testPassword = "testpassword8!";
    	String testLocation = "West US";
    	
    	// act
    	ServerCreateParameters serverCreateParameters = new ServerCreateParameters();
    	serverCreateParameters.setAdministratorUserName(testAdministratorUserName);
    	serverCreateParameters.setAdministratorPassword(testPassword);
    	serverCreateParameters.setLocation(testLocation);
        ServerCreateResponse serverCreateResponse = serverOperations.create(serverCreateParameters);
        String serverName = serverCreateResponse.getServerName();
        serverOperations.delete(serverName);
        
        // assert
        ServerListResponse serverListResponse = serverOperations.list();
        Iterator<Server> serverList = serverListResponse.iterator();
        Server createdServer = null;
        while (serverList.hasNext())
        {
            Server nextServer = serverList.next();
            if (nextServer.getName().equals(serverName))
            {
            	createdServer = nextServer;
            }
        }
        assertNull(createdServer);
        
    }
    
}