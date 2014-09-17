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

package com.microsoft.windowsazure.management.compute;

import java.net.URI;
import java.util.Random;

import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;

import com.microsoft.windowsazure.core.pipeline.apache.ApacheConfigurationProperties;
import com.microsoft.windowsazure.core.utils.KeyStoreType;
import com.microsoft.windowsazure.management.configuration.*;
import com.microsoft.windowsazure.management.*;
import com.microsoft.windowsazure.management.storage.StorageManagementClient;
import com.microsoft.windowsazure.management.storage.StorageManagementService;
import com.microsoft.windowsazure.*;

public abstract class ComputeManagementIntegrationTestBase {
    protected static String testVMPrefix = "aztst";
    protected static String testStoragePrefix = "aztst";
    protected static String testHostedServicePrefix = "azhst";
    
    protected static ComputeManagementClient computeManagementClient;
    protected static StorageManagementClient storageManagementClient;
    protected static ManagementClient managementClient;

    protected static void createComputeManagementClient() throws Exception {
        Configuration config = createConfiguration();
        computeManagementClient = ComputeManagementService.create(config);
    }
    
    protected static void createStorageManagementClient() throws Exception {
        Configuration config = createConfiguration();
        storageManagementClient = StorageManagementService.create(config);
    }
    
    protected static void createManagementClient() throws Exception {
        Configuration config = createConfiguration();
        config.setProperty(ApacheConfigurationProperties.PROPERTY_RETRY_HANDLER, new DefaultHttpRequestRetryHandler());

        managementClient = ManagementService.create(config);
    }
   
    protected static Configuration createConfiguration() throws Exception {
        String baseUri = System.getenv(ManagementConfiguration.URI);
        return ManagementConfiguration.configure(
            baseUri != null ? new URI(baseUri) : null,
            System.getenv(ManagementConfiguration.SUBSCRIPTION_ID),
            System.getenv(ManagementConfiguration.KEYSTORE_PATH),
            System.getenv(ManagementConfiguration.KEYSTORE_PASSWORD),
            KeyStoreType.fromString(System.getenv(ManagementConfiguration.KEYSTORE_TYPE))
        );
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