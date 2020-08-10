// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor;

import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.compute.ComputeManager;
// import com.azure.management.eventhub.implementation.EventHubManager;
import com.azure.resourcemanager.resources.core.TestBase;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.storage.StorageManager;

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
