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
package com.microsoft.windowsazure.management.mediaservices;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.junit.Assert;
import org.xml.sax.SAXException;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.MockIntegrationTestBase;
import com.microsoft.windowsazure.core.AzureOperationResponse;
import com.microsoft.windowsazure.core.ServiceClient;
import com.microsoft.windowsazure.core.pipeline.apache.ApacheConfigurationProperties;
import com.microsoft.windowsazure.core.utils.KeyStoreType;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.ManagementClient;
import com.microsoft.windowsazure.management.ManagementService;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import com.microsoft.windowsazure.management.mediaservices.models.MediaServicesAccountListResponse;
import com.microsoft.windowsazure.management.mediaservices.models.MediaServicesAccountListResponse.MediaServiceAccount;
import com.microsoft.windowsazure.management.models.LocationAvailableServiceNames;
import com.microsoft.windowsazure.management.models.LocationsListResponse;
import com.microsoft.windowsazure.management.storage.StorageManagementClient;
import com.microsoft.windowsazure.management.storage.StorageManagementService;
import com.microsoft.windowsazure.management.storage.models.StorageAccountCreateParameters;
import com.microsoft.windowsazure.management.storage.models.StorageAccountGetKeysResponse;
import com.microsoft.windowsazure.management.storage.models.StorageAccountGetResponse;

public abstract class MediaServiceManagementIntegrationTestBase extends MockIntegrationTestBase {
    protected static String testMediaServicesAccountPrefix = "aztst" + "media"; 
    protected static String testStoragePrefix = "aztst" + "mediastorage";
    protected static String storageAccountKey = "";
    protected static URI storageEndpointUri;
    protected static String storageLocation = null;
   
    protected static StorageManagementClient storageManagementClient;
    protected static ManagementClient managementClient;
    protected static MediaServicesManagementClient mediaServicesManagementClient;

    protected static void createMediaServiceManagementClient() throws Exception {       
        Configuration config = createConfiguration();
        config.setProperty(ApacheConfigurationProperties.PROPERTY_RETRY_HANDLER, new DefaultHttpRequestRetryHandler());
        mediaServicesManagementClient = MediaServicesManagementService.create(config);
        addClient((ServiceClient<?>) mediaServicesManagementClient, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createMediaServiceManagementClient();
                return null;
            }
        });
    }
    
    protected static void createStorageManagementClient() throws Exception {
        Configuration config = createConfiguration();
        config.setProperty(ApacheConfigurationProperties.PROPERTY_RETRY_HANDLER, new DefaultHttpRequestRetryHandler());
        storageManagementClient = StorageManagementService.create(config);
        addClient((ServiceClient<?>) storageManagementClient, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createStorageManagementClient();
                return null;
            }
        });
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
    
    protected static void createStorageAccount(String storageAccountName) throws Exception {      
        String storageAccountLabel = storageAccountName + "Label1";

        //Arrange
        StorageAccountCreateParameters createParameters = new StorageAccountCreateParameters();
        //required
        createParameters.setName(storageAccountName);
        //required
        createParameters.setLabel(storageAccountLabel);
        //required if no affinity group has set
        createParameters.setLocation(storageLocation);
        createParameters.setAccountType("Standard_LRS");

        //act
        AzureOperationResponse operationResponse = storageManagementClient.getStorageAccountsOperations().create(createParameters);
        Assert.assertEquals(200, operationResponse.getStatusCode());
         
        //use container inside storage account, needed for os image storage.
        StorageAccountGetKeysResponse storageAccountGetKeysResponse = storageManagementClient.getStorageAccountsOperations().getKeys(storageAccountName);
        storageAccountKey = storageAccountGetKeysResponse.getPrimaryKey();
        storageEndpointUri = storageManagementClient.getStorageAccountsOperations().get(storageAccountName).getStorageAccount().getProperties().getEndpoints().get(0);
    }
    
    protected static void cleanStorageAccount(String storageAccountName) {
        StorageAccountGetResponse storageAccountGetResponse = null;
        try {
            storageAccountGetResponse = storageManagementClient.getStorageAccountsOperations().get(storageAccountName); 
        } catch (ServiceException e) {
        } catch (IOException e) {
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (URISyntaxException e) {
        }

        if ((storageAccountGetResponse != null) && (storageAccountGetResponse.getStorageAccount().getName().contains(storageAccountName))) {
            AzureOperationResponse operationResponse = null;
            try {
                operationResponse = storageManagementClient.getStorageAccountsOperations().delete(storageAccountName);
            } catch (IOException e) {
            } catch (ServiceException e) {
            }
            if (operationResponse != null) {
                Assert.assertEquals(200, operationResponse.getStatusCode());
            }
        }
    } 
    
    public static void cleanMediaServicesAccount() {
        MediaServicesAccountListResponse mediaServicesAccountListResponse = null;
        try {
            mediaServicesAccountListResponse  = mediaServicesManagementClient.getAccountsOperations().list();
        } catch (IOException e) {
        } catch (ServiceException e) {
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (URISyntaxException e) {
        }

        if (mediaServicesAccountListResponse != null){
            ArrayList<MediaServiceAccount> mediaServicesAccountlist = mediaServicesAccountListResponse.getAccounts();
            for (MediaServiceAccount mediaServiceAccount : mediaServicesAccountlist) { 
                if (mediaServiceAccount.getName().startsWith(testMediaServicesAccountPrefix)) {
                    try {
                        mediaServicesManagementClient.getAccountsOperations().delete(mediaServiceAccount.getName());
                    } catch (IOException e) {
                    } catch (ServiceException e) {
                    }
                }
            }
        }
    }    
    
    protected static void getLocation() throws Exception {
        ArrayList<String> serviceName = new ArrayList<String>();       
        serviceName.add(LocationAvailableServiceNames.HIGHMEMORY);       

        LocationsListResponse locationsListResponse = managementClient.getLocationsOperations().list();
        for (LocationsListResponse.Location location : locationsListResponse) {
            ArrayList<String> availableServicelist = location.getAvailableServices();
            String locationName = location.getName();
            if (availableServicelist.containsAll(serviceName)== true) {  
                if (locationName.contains("West US") == true)
                {
                    storageLocation = locationName;
                }
                if (storageLocation==null)
                {
                    storageLocation = locationName;
                }
            }
        } 
    }
    
    protected static String randomString(int length) {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i=0; i<length; i++)
        {
            stringBuilder.append((char)('a' + random.nextInt(26)));
        }
        return stringBuilder.toString();
    }
}