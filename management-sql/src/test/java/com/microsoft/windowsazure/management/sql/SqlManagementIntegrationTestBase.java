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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.xml.sax.SAXException;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.MockIntegrationTestBase;
import com.microsoft.windowsazure.core.Builder;
import com.microsoft.windowsazure.core.ServiceClient;
import com.microsoft.windowsazure.core.Builder.Alteration;
import com.microsoft.windowsazure.core.Builder.Registry;
import com.microsoft.windowsazure.core.pipeline.apache.ApacheConfigurationProperties;
import com.microsoft.windowsazure.core.utils.KeyStoreType;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.ManagementClient;
import com.microsoft.windowsazure.management.ManagementService;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import com.microsoft.windowsazure.management.models.LocationAvailableServiceNames;
import com.microsoft.windowsazure.management.models.LocationsListResponse;
import com.microsoft.windowsazure.management.sql.models.DatabaseCreateParameters;
import com.microsoft.windowsazure.management.sql.models.DatabaseCreateResponse;
import com.microsoft.windowsazure.management.sql.models.ServerCreateParameters;
import com.microsoft.windowsazure.management.sql.models.ServerCreateResponse;
import com.microsoft.windowsazure.management.storage.StorageManagementClient;
import com.microsoft.windowsazure.management.storage.StorageManagementService;
import com.microsoft.windowsazure.management.storage.models.StorageAccount;
import com.microsoft.windowsazure.management.storage.models.StorageAccountCreateParameters;
import com.microsoft.windowsazure.management.storage.models.StorageAccountGetKeysResponse;
import com.microsoft.windowsazure.management.storage.models.StorageAccountGetResponse;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.LoggingFilter;

public abstract class SqlManagementIntegrationTestBase extends MockIntegrationTestBase {

    protected static String testStorageAccountPrefix = "azsql";
    protected static ManagementClient managementClient;
    protected static SqlManagementClient sqlManagementClient;
    protected static StorageManagementClient storageManagementClient;
    protected static DatabaseOperations databaseOperations;
    protected static ServerOperations serverOperations;
    protected static DacOperations dacOperations;
    protected static Map<String, String> firewallRuleToBeRemoved = new HashMap<String, String>();
    protected static List<String> serverToBeRemoved = new ArrayList<String>();
    protected static Map<String, String> databaseToBeRemoved = new HashMap<String, String>();
    
    protected static String testAdministratorPasswordValue = "testAdminPassword!8";
    protected static String testAdministratorUserNameValue = "testadminuser";
    protected static String testLocationValue = null;

    protected static void createService() throws Exception {
        // reinitialize configuration from known state
        Configuration config = createConfiguration();
        config.setProperty(ApacheConfigurationProperties.PROPERTY_RETRY_HANDLER, new DefaultHttpRequestRetryHandler());

        sqlManagementClient = SqlManagementService.create(config);
        addClient((ServiceClient<?>) sqlManagementClient, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createService();
                return null;
            }
        });
    }
    
    protected static void createStorageService() throws Exception {
        // reinitialize configuration from known state
        Configuration config = createConfiguration();

        // add LoggingFilter to any pipeline that is created
        Registry builder = (Registry) config.getBuilder();
        builder.alter(StorageManagementClient.class, Client.class, new Alteration<Client>() {
            @Override
            public Client alter(String profile, Client client, Builder builder, Map<String, Object> properties) {
                client.addFilter(new LoggingFilter());
                return client;
            }
        });

        storageManagementClient = StorageManagementService.create(config);
        addClient((ServiceClient<?>) storageManagementClient, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createStorageService();
                return null;
            }
        });
        addRegexRule("azsql[a-z]{10}");
    }
    
    protected static StorageAccount createStorageAccount(String storageAccountName) throws Exception { 
        String storageAccountLabel =  "Label";

        StorageAccountCreateParameters createParameters = new StorageAccountCreateParameters();
        createParameters.setName(storageAccountName); 
        createParameters.setLabel(storageAccountLabel);
        createParameters.setLocation(testLocationValue);
        createParameters.setAccountType("Standard_LRS");
        storageManagementClient.getStorageAccountsOperations().create(createParameters);
        StorageAccountGetResponse storageAccountGetResponse = storageManagementClient.getStorageAccountsOperations().get(storageAccountName);
        StorageAccount storageAccount = storageAccountGetResponse.getStorageAccount();
        return storageAccount;
     }
    
    protected static String getStorageKey(String storageAccountName) throws IOException, ServiceException, ParserConfigurationException, SAXException, URISyntaxException {
        StorageAccountGetKeysResponse storageAccountGetKeyResponse = storageManagementClient.getStorageAccountsOperations().getKeys(storageAccountName);
        return storageAccountGetKeyResponse.getPrimaryKey();
    }

    
    protected String createServer() throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException {
        ServerCreateParameters serverCreateParameters = new ServerCreateParameters();
        serverCreateParameters.setAdministratorPassword(testAdministratorPasswordValue);
        serverCreateParameters.setAdministratorUserName(testAdministratorUserNameValue);
        serverCreateParameters.setLocation(testLocationValue);
        ServerCreateResponse serverCreateResponse = serverOperations.create(serverCreateParameters);
        String serverName = serverCreateResponse.getServerName();
        serverToBeRemoved.add(serverName);
        return serverName;
    
    }
    
    protected String createDatabase(String serverName) throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException {
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

    protected static Configuration createConfiguration() throws Exception {
        String baseUri = System.getenv(ManagementConfiguration.URI);
        if (IS_MOCKED) {
            return ManagementConfiguration.configure(
                    new URI(MOCK_URI),
                    MOCK_SUBSCRIPTION,
                    null,
                    null,
                    null
            );
        } else {
            return ManagementConfiguration.configure(
                    baseUri != null ? new URI(baseUri) : null,
                    System.getenv(ManagementConfiguration.SUBSCRIPTION_ID),
                    System.getenv(ManagementConfiguration.KEYSTORE_PATH),
                    System.getenv(ManagementConfiguration.KEYSTORE_PASSWORD),
                    KeyStoreType.fromString(System.getenv(ManagementConfiguration.KEYSTORE_TYPE))
            );
        }
    }
    
    protected static void createManagementClient() throws Exception {
        Configuration config = createConfiguration();
        config.setProperty(ApacheConfigurationProperties.PROPERTY_RETRY_HANDLER, new DefaultHttpRequestRetryHandler());
        managementClient = ManagementService.create(config);
        addClient((ServiceClient<?>) managementClient, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createManagementClient();
                return null;
            }
        });
    }      
    
    protected static void getLocation() throws Exception {
        ArrayList<String> serviceName = new ArrayList<String>();       
        serviceName.add(LocationAvailableServiceNames.STORAGE);       

        LocationsListResponse locationsListResponse = managementClient.getLocationsOperations().list();
        for (LocationsListResponse.Location location : locationsListResponse) {
            ArrayList<String> availableServicelist = location.getAvailableServices();
            String locationName = location.getName();
            if (availableServicelist.containsAll(serviceName)== true) {  
                if (locationName.contains("West US") == true)
                {
                    testLocationValue = locationName;
                }
                if (testLocationValue==null)
                {
                    testLocationValue = locationName;
                }
            }
        } 
    }
    
    protected static String randomString(int length) {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i=0; i<length; i++) {
            stringBuilder.append((char)('a' + random.nextInt(26)));
        }
        return stringBuilder.toString();
    }
}