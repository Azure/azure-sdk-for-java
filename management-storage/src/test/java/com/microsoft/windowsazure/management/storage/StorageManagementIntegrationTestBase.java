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

import java.net.URI;
import java.util.ArrayList;
import java.util.Random;

import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;

import com.microsoft.windowsazure.core.pipeline.apache.ApacheConfigurationProperties;
import com.microsoft.windowsazure.core.utils.KeyStoreType;
import com.microsoft.windowsazure.management.ManagementClient;
import com.microsoft.windowsazure.management.ManagementService;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.configuration.PublishSettingsLoader;
import com.microsoft.windowsazure.management.models.LocationAvailableServiceNames;
import com.microsoft.windowsazure.management.models.LocationsListResponse;

public abstract class StorageManagementIntegrationTestBase {

    protected static String testStorageAccountPrefix = "azurejavatest";
    protected static String storageLocation = null;
    

    protected static StorageManagementClient storageManagementClient;
    protected static ManagementClient managementClient;

    protected static void createService() throws Exception {
        // reinitialize configuration from known state
        Configuration config = createConfiguration();
        config.setProperty(ApacheConfigurationProperties.PROPERTY_RETRY_HANDLER, new DefaultHttpRequestRetryHandler());

        storageManagementClient = StorageManagementService.create(config);
    }
    
    protected static void createManagementClient() throws Exception {
        Configuration config = createConfiguration();
        config.setProperty(ApacheConfigurationProperties.PROPERTY_RETRY_HANDLER, new DefaultHttpRequestRetryHandler());
        managementClient = ManagementService.create(config);
    }       
  
    protected static Configuration createConfiguration() throws Exception {
//        String baseUri = System.getenv(ManagementConfiguration.URI);
//        return ManagementConfiguration.configure(
//            baseUri != null ? new URI(baseUri) : null,
//            System.getenv(ManagementConfiguration.SUBSCRIPTION_ID),
//            System.getenv(ManagementConfiguration.KEYSTORE_PATH),
//            System.getenv(ManagementConfiguration.KEYSTORE_PASSWORD),
//            KeyStoreType.fromString(System.getenv(ManagementConfiguration.KEYSTORE_TYPE))
//        );
        return PublishSettingsLoader.createManagementConfiguration("C:\\Users\\xuezhain\\Downloads\\Azure SDK Mooncake sandbox-5-14-2014-credentials.publishsettings","23a32706-03c1-4920-a6ac-1b7c8de24bb4");
 
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
        System.out.println("storageLocation = " + storageLocation);
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