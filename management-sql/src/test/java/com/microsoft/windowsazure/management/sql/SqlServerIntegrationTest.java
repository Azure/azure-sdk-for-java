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


import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import com.microsoft.windowsazure.management.sql.models.DatabaseOperationListResponse.DatabaseOperation;
import com.microsoft.windowsazure.management.sql.models.ServerCreateParameters;
import com.microsoft.windowsazure.management.sql.models.ServerListResponse;
import com.microsoft.windowsazure.management.sql.models.ServerListResponse.Server;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.*;
import org.xml.sax.SAXException;

import com.microsoft.windowsazure.core.Builder;
import com.microsoft.windowsazure.core.Builder.Alteration;
import com.microsoft.windowsazure.core.Builder.Registry;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.Configuration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.xml.bind.v2.schemagen.xmlschema.List;

public class SqlServerIntegrationTest extends SqlManagementIntegrationTestBase {

	@Before
	public void setup() throws Exception
	{
		createService();
	}
	
    @Test
    public void createSqlServerWithRequiredParameters() throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException 
    {
    	//arrange 
    	ServerOperations serverOperations = sqlManagementClient.getServersOperations();
    	ServerCreateParameters serverCreateParameters = new ServerCreateParameters();
    	serverCreateParameters.setAdministratorUserName("testadminname");
    	serverCreateParameters.setAdministratorPassword("testpassword8!");
    	serverCreateParameters.setLocation("West US");
        serverOperations.create(serverCreateParameters);
        ServerListResponse serverListResponse = serverOperations.list();
        Iterator<Server> serverList = serverListResponse.iterator();
        Server nextServer = serverList.next();
        
    }
    
    
    
    
}