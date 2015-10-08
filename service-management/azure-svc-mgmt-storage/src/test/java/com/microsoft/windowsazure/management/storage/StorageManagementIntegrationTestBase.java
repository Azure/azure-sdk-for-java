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
package com.microsoft.windowsazure.management.storage;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.MockIntegrationTestBase;
import com.microsoft.windowsazure.core.ServiceClient;
import com.microsoft.windowsazure.core.pipeline.apache.ApacheConfigurationProperties;
import com.microsoft.windowsazure.core.utils.KeyStoreType;
import com.microsoft.windowsazure.management.ManagementClient;
import com.microsoft.windowsazure.management.ManagementService;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import com.microsoft.windowsazure.management.models.LocationAvailableServiceNames;
import com.microsoft.windowsazure.management.models.LocationsListResponse;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;

import java.net.URI;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;

public abstract class StorageManagementIntegrationTestBase extends MockIntegrationTestBase {

    protected static String testStorageAccountPrefix = "azurejavatest";
    protected static String storageLocation = null;
    

    protected static StorageManagementClient storageManagementClient;
    protected static ManagementClient managementClient;

    protected static void createService() throws Exception {
        // reinitialize configuration from known state
        Configuration config = createConfiguration();
        config.setProperty(ApacheConfigurationProperties.PROPERTY_RETRY_HANDLER, new DefaultHttpRequestRetryHandler());

        storageManagementClient = StorageManagementService.create(config);
        addClient((ServiceClient<?>) storageManagementClient, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createService();
                return null;
            }
        });
        addRegexRule("azurejavatest[a-z]{10}");
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
                    storageLocation = locationName;
                }
                if (storageLocation==null)
                {
                    storageLocation = locationName;
                }
            }
        }         
    }
    
    protected static String randomString(int length)
    {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i=0; i<length; i++)
        {
                stringBuilder.append((char)('a' + random.nextInt(26)));
        }
        return stringBuilder.toString();
    }
}