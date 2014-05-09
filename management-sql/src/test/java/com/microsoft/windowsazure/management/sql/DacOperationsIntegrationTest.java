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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.*;
import org.xml.sax.SAXException;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.sql.models.DacExportParameters;
import com.microsoft.windowsazure.management.sql.models.DacImportParameters;
import com.microsoft.windowsazure.management.storage.models.StorageAccount;

public class DacOperationsIntegrationTest extends SqlManagementIntegrationTestBase {

    private static StorageAccount storageAccount;

    @BeforeClass
    public static void setup() throws Exception {
        createService();
        createStorageService();
        databaseOperations = sqlManagementClient.getDatabasesOperations();
        serverOperations = sqlManagementClient.getServersOperations();
        dacOperations = sqlManagementClient.getDacOperations();
        storageAccount = createStorageAccount(testStorageAccountPrefix+randomString(10));
    }

    @AfterClass
    public static void cleanup() {
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
        
        try {
            storageManagementClient.getStorageAccountsOperations().delete(storageAccount.getName());
        } catch (IOException e) {
        } catch (ServiceException e) {
        }
    }

    @Test
    @Ignore("temporary disable because of long running ")
    public void importDatabaseSuccess() throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException, URISyntaxException {
        // arrange
        String serverName = createServer();
        String azureEditionValue = "sqlazure";
        URI uriValue = storageAccount.getUri();
        String accessKey = getStorageKey(storageAccount.getName());
        DacImportParameters.BlobCredentialsParameter blobCredentialsValue = new DacImportParameters.BlobCredentialsParameter();
        blobCredentialsValue.setStorageAccessKey(accessKey);
        blobCredentialsValue.setUri(uriValue);
        
        // act 
        DacImportParameters dacImportParameters = new DacImportParameters();
        dacImportParameters.setAzureEdition(azureEditionValue);
        dacImportParameters.setBlobCredentials(blobCredentialsValue);
        dacOperations.importDatabase(serverName, dacImportParameters);
    }

    @Test
    @Ignore("temporary disable because of long running ")
    public void exportDatabaseSuccess() throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException, URISyntaxException {
        // arrange
        String serverName = createServer();
        URI uriValue = storageAccount.getUri();
        DacExportParameters.BlobCredentialsParameter blobCredentialsValue = new DacExportParameters.BlobCredentialsParameter();
        String accessKey = getStorageKey(storageAccount.getName());
        blobCredentialsValue.setStorageAccessKey(accessKey);
        blobCredentialsValue.setUri(uriValue);
        
        // act 
        DacExportParameters dacExportParameters = new DacExportParameters();
        dacExportParameters.setBlobCredentials(blobCredentialsValue);
        dacOperations.exportDatabase(serverName, dacExportParameters);
    }
}