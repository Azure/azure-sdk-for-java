// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.monitor;

import com.azure.core.http.HttpPipeline;
import com.azure.management.appservice.implementation.AppServiceManager;
import com.azure.management.compute.implementation.ComputeManager;
// import com.azure.management.eventhub.implementation.EventHubManager;
import com.azure.management.monitor.implementation.MonitorManager;
import com.azure.management.resources.core.TestBase;
import com.azure.management.resources.fluentcore.profile.AzureProfile;
import com.azure.management.resources.implementation.ResourceManager;
import com.azure.management.storage.implementation.StorageManager;

/** The base for Monitor manager tests. */
public class MonitorManagementTest extends TestBase {
    protected ResourceManager resourceManager;
    protected MonitorManager monitorManager;
    protected ComputeManager computeManager;
    protected StorageManager storageManager;
    //    protected EventHubManager eventHubManager;
    protected AppServiceManager appServiceManager;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {

        appServiceManager = AppServiceManager.authenticate(httpPipeline, profile, sdkContext);

        resourceManager =
            ResourceManager.authenticate(httpPipeline, profile).withSdkContext(sdkContext).withDefaultSubscription();

        monitorManager = MonitorManager.authenticate(httpPipeline, profile, sdkContext);

        computeManager = ComputeManager.authenticate(httpPipeline, profile, sdkContext);

        storageManager = StorageManager.authenticate(httpPipeline, profile, sdkContext);

        //        eventHubManager = EventHubManager
        //                .authenticate(restClient, defaultSubscription, sdkContext);
    }

    @Override
    protected void cleanUpResources() {
    }
}
