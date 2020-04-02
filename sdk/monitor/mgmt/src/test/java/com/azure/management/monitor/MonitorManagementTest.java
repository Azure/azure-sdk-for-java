/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.monitor;

import com.azure.management.RestClient;
import com.azure.management.resources.core.TestBase;
import com.azure.management.appservice.implementation.AppServiceManager;
import com.azure.management.compute.implementation.ComputeManager;
//import com.azure.management.eventhub.implementation.EventHubManager;
import com.azure.management.monitor.implementation.MonitorManager;
import com.azure.management.resources.implementation.ResourceManager;
import com.azure.management.storage.implementation.StorageManager;

/**
 * The base for Monitor manager tests.
 */
public class MonitorManagementTest extends TestBase {
    protected ResourceManager resourceManager;
    protected MonitorManager monitorManager;
    protected ComputeManager computeManager;
    protected StorageManager storageManager;
//    protected EventHubManager eventHubManager;
    protected AppServiceManager appServiceManager;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {

        appServiceManager = AppServiceManager
                .authenticate(restClient, domain, defaultSubscription, sdkContext);

        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSdkContext(sdkContext)
                .withSubscription(defaultSubscription);

        monitorManager = MonitorManager
                .authenticate(restClient, defaultSubscription, sdkContext);

        computeManager = ComputeManager
                .authenticate(restClient, defaultSubscription, sdkContext);

        storageManager = StorageManager
                .authenticate(restClient, defaultSubscription, sdkContext);

//        eventHubManager = EventHubManager
//                .authenticate(restClient, defaultSubscription, sdkContext);
    }

    @Override
    protected void cleanUpResources() {
    }
}

