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
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.*;
import org.xml.sax.SAXException;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.sql.models.ServiceObjective;
import com.microsoft.windowsazure.management.sql.models.ServiceObjectiveGetResponse;
import com.microsoft.windowsazure.management.sql.models.ServiceObjectiveListResponse;

public class ServiceObjectiveIntegrationTest extends SqlManagementIntegrationTestBase {

    private static List<String> serverToBeRemoved = new ArrayList<String>();
    private static ServiceObjectiveOperations serviceObjectivesOperations;

    @BeforeClass
    public static void setup() throws Exception {
        createService();
        databaseOperations = sqlManagementClient.getDatabasesOperations();
        serverOperations = sqlManagementClient.getServersOperations();
        serviceObjectivesOperations = sqlManagementClient.getServiceObjectivesOperations();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        for (String databaseName : databaseToBeRemoved.keySet()) {
            String serverName = databaseToBeRemoved.get(databaseName);
            try {
                databaseOperations.delete(serverName, databaseName);
            } catch (IOException e) {
            } catch (ServiceException e) {
            }
        }
        databaseToBeRemoved.clear();
        
        for (String serverName : serverToBeRemoved) {
            try {
                serverOperations.delete(serverName);
            } catch (IOException e) {
            } catch (ServiceException e) {
            }
        }
    }

    @Test
    public void listServiceObjectiveSuccess() throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException {
        // arrange 
        String serverName = createServer();
        createDatabase(serverName);

        // act 
        ServiceObjectiveListResponse serviceObjectiveListResponse = serviceObjectivesOperations.list(serverName);

        // assert
        assertTrue(serviceObjectiveListResponse.getServiceObjectives().size() > 0);
    }

    @Test
    public void getServiceObjectiveSuccess() throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException {
        // arrange 
        String serverName = createServer();
        createDatabase(serverName);

        // act 
        ServiceObjectiveListResponse serviceObjectivesListResponse = serviceObjectivesOperations.list(serverName);
        ServiceObjective serviceObjective = serviceObjectivesListResponse.getServiceObjectives().get(0);
        ServiceObjectiveGetResponse serviceObjectiveGetResponse = serviceObjectivesOperations.get(serverName, serviceObjective.getId());
        ServiceObjective actualServiceObjective = serviceObjectiveGetResponse.getServiceObjective();

        // assert
        assertEquals(serviceObjective.getId(), actualServiceObjective.getId());
        assertEquals(serviceObjective.getName(), actualServiceObjective.getName());
        assertEquals(serviceObjective.getName(), actualServiceObjective.getName());
    }
}