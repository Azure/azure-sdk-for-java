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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.*;
import org.xml.sax.SAXException;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.sql.models.Database;
import com.microsoft.windowsazure.management.sql.models.DatabaseCreateParameters;
import com.microsoft.windowsazure.management.sql.models.DatabaseCreateResponse;
import com.microsoft.windowsazure.management.sql.models.DatabaseListResponse;
import com.microsoft.windowsazure.management.sql.models.DatabaseUpdateParameters;
import com.microsoft.windowsazure.management.sql.models.DatabaseUpdateResponse;

public class DatabaseOperationsIntegrationTest extends SqlManagementIntegrationTestBase {

    @BeforeClass
    public static void setup() throws Exception {
        createService();
        databaseOperations = sqlManagementClient.getDatabasesOperations();
        serverOperations = sqlManagementClient.getServersOperations();
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

        serverToBeRemoved.clear();
    }
    
    @Test
    public void createDatabaseWithRequiredParameters() throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException {
        // arrange 
        String expectedServerName = createServer();
        String expectedCollationName = "SQL_Latin1_General_CP1_CI_AS";
        String expectedEdition = "Web";
        String expectedDatabaseName = "expecteddatabasename";
        int expectedMaximumDatabaseSizeInGBValue = 5;

        // act 
        DatabaseCreateParameters databaseCreateParameters = new DatabaseCreateParameters();
        databaseCreateParameters.setName(expectedDatabaseName);
        databaseCreateParameters.setCollationName(expectedCollationName);
        databaseCreateParameters.setEdition(expectedEdition);
        databaseCreateParameters.setMaximumDatabaseSizeInGB(expectedMaximumDatabaseSizeInGBValue);
        DatabaseCreateResponse databaseCreateResponse = databaseOperations.create(expectedServerName, databaseCreateParameters);
        Database database= databaseCreateResponse.getDatabase();
        String databaseName = database.getName();
        databaseToBeRemoved.put(databaseName, expectedServerName);

        // assert
        assertNotNull(databaseCreateResponse);
    }

    @Test
    public void createDatabaseWithOptionalParameters() throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException {
        // arrange 
        int expectedMaximumDatabaseSizeInGBValue = 1; 
        String expectedDatabaseName = "expecteddatabasename";
        String expectedServerName = createServer();
        String expectedCollationName = "SQL_Latin1_General_CP1_CI_AS";
        String expectedEdition = "Web";
        
        // act 
        DatabaseCreateParameters databaseCreateParameters = new DatabaseCreateParameters();
        databaseCreateParameters.setMaximumDatabaseSizeInGB(expectedMaximumDatabaseSizeInGBValue);
        databaseCreateParameters.setName(expectedDatabaseName);
        databaseCreateParameters.setCollationName(expectedCollationName);
        databaseCreateParameters.setEdition(expectedEdition);
        DatabaseCreateResponse databaseCreateResponse = databaseOperations.create(expectedServerName, databaseCreateParameters);
        databaseToBeRemoved.put(databaseCreateResponse.getDatabase().getName(), expectedServerName);
        
        
        // assert
        assertEquals(expectedDatabaseName, databaseCreateResponse.getDatabase().getName());
        assertEquals(expectedMaximumDatabaseSizeInGBValue, databaseCreateResponse.getDatabase().getMaximumDatabaseSizeInGB());
        
    }

    @Test
    public void deleteDatabaseSuccess() throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException {
        // arrange 
        String expectedCollationName = "SQL_Latin1_General_CP1_CI_AS";
        String expectedEdition = "Web";
        String expectedDatabaseName = "expecteddatabasename";
        String serverName = createServer();
        int expectedMaxSizeInGB = 5;

        // act 
        DatabaseCreateParameters databaseCreateParameters = new DatabaseCreateParameters();
        databaseCreateParameters.setName(expectedDatabaseName);
        databaseCreateParameters.setCollationName(expectedCollationName);
        databaseCreateParameters.setEdition(expectedEdition);
        databaseCreateParameters.setMaximumDatabaseSizeInGB(expectedMaxSizeInGB);
        DatabaseCreateResponse databaseCreateResponse = databaseOperations.create(serverName, databaseCreateParameters);
        String databaseName = databaseCreateResponse.getDatabase().getName();
        databaseOperations.delete(serverName, databaseName);

        // assert
        DatabaseListResponse databaseListResponse = databaseOperations.list(serverName);
        ArrayList<Database> databaseList = databaseListResponse.getDatabases();
        for (Database database : databaseList) {
            assertNotEquals(databaseName, database.getName());
        }
    }

    @Test
    public void updateDatabaseSuccess() throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException {
        // arrange
        String expectedCollationName = "SQL_Latin1_General_CP1_CI_AS";
        String expectedEdition = "Web";
        String updatedEdition = "Business";
        String expectedDatabaseName = "expecteddatabasename";
        String serverName = createServer();
        int expectedMaxSizeInGB = 5;
        int updatedMaxSizeInGB = 10;

        // act 
        DatabaseCreateParameters databaseCreateParameters = new DatabaseCreateParameters();
        databaseCreateParameters.setName(expectedDatabaseName);
        databaseCreateParameters.setCollationName(expectedCollationName);
        databaseCreateParameters.setEdition(expectedEdition);
        databaseCreateParameters.setMaximumDatabaseSizeInGB(expectedMaxSizeInGB);
        DatabaseCreateResponse databaseCreateResponse = databaseOperations.create(serverName, databaseCreateParameters);
        String databaseName = databaseCreateResponse.getDatabase().getName();
        databaseToBeRemoved.put(databaseName, serverName);
        DatabaseUpdateParameters databaseUpdateParameters = new DatabaseUpdateParameters();
        databaseUpdateParameters.setName(expectedDatabaseName);
        databaseUpdateParameters.setMaximumDatabaseSizeInGB(updatedMaxSizeInGB);
        databaseUpdateParameters.setCollationName(expectedCollationName);
        databaseUpdateParameters.setEdition(updatedEdition);
        DatabaseUpdateResponse databaseUpdateResponse = databaseOperations.update(serverName, expectedDatabaseName, databaseUpdateParameters);

        // assert
        assertEquals(updatedMaxSizeInGB, databaseUpdateResponse.getDatabase().getMaximumDatabaseSizeInGB());
        assertEquals(updatedEdition, databaseUpdateResponse.getDatabase().getEdition());
    }
}