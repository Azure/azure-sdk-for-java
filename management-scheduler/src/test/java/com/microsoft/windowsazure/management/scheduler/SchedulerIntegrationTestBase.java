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
package com.microsoft.windowsazure.management.scheduler;

import java.net.URI;
import java.util.ArrayList;
import java.util.Random;

import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;

import com.microsoft.windowsazure.core.pipeline.apache.ApacheConfigurationProperties;
import com.microsoft.windowsazure.core.utils.KeyStoreType;
import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.*;
import com.microsoft.windowsazure.management.models.LocationAvailableServiceNames;
import com.microsoft.windowsazure.management.models.LocationsListResponse;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import com.microsoft.windowsazure.management.configuration.PublishSettingsLoader;
import com.microsoft.windowsazure.management.ManagementClient;
import com.microsoft.windowsazure.management.compute.ComputeManagementClient;
import com.microsoft.windowsazure.management.compute.ComputeManagementService;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.scheduler.*;

public abstract class SchedulerIntegrationTestBase {
    protected static String testSchedulerPrefix = "aztstsch";
    protected static String testJobCollectionPrefix = "aztstjc";
    protected static String hostedLocation = null;

    protected static SchedulerManagementClient schedulerManagementClient;
    protected static SchedulerClient schedulerClient;
    protected static CloudServiceManagementClient cloudServiceManagementClient;
    protected static ManagementClient managementClient;

    protected static void createManagementClient() throws Exception {
        Configuration config = createConfiguration();
        managementClient = ManagementService.create(config);
    }

    protected static void createSchedulerManagementService() throws Exception {
        Configuration config = createConfiguration();
        config.setProperty(ApacheConfigurationProperties.PROPERTY_RETRY_HANDLER, new DefaultHttpRequestRetryHandler());
        schedulerManagementClient = SchedulerManagementService.create(config);
    }

    protected static void createSchedulerService(String cloudServiceName, String jobCollectionName) throws Exception {
        Configuration config = createConfiguration(cloudServiceName, jobCollectionName);
        config.setProperty(ApacheConfigurationProperties.PROPERTY_RETRY_HANDLER, new DefaultHttpRequestRetryHandler());
        schedulerClient = SchedulerService.create(config);
    }

    protected static void createCloudServiceManagementService() throws Exception {
        Configuration config = createConfiguration();
        config.setProperty(ApacheConfigurationProperties.PROPERTY_RETRY_HANDLER, new DefaultHttpRequestRetryHandler());
        cloudServiceManagementClient = CloudServiceManagementService.create(config);
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

    protected static Configuration createConfiguration(String cloudServiceName, String jobCollectionName) throws Exception {
        String baseUri = System.getenv(ManagementConfiguration.URI);
        return ManagementConfiguration.configure(
            baseUri != null ? new URI(baseUri) : null,
            System.getenv(ManagementConfiguration.SUBSCRIPTION_ID),
            System.getenv(ManagementConfiguration.KEYSTORE_PATH),
            System.getenv(ManagementConfiguration.KEYSTORE_PASSWORD),
            KeyStoreType.fromString(System.getenv(ManagementConfiguration.KEYSTORE_TYPE)),
            cloudServiceName,
            jobCollectionName
        );
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
                    hostedLocation = locationName;
                }
                if (hostedLocation==null)
                {
                    hostedLocation = locationName;
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