// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.resources;

import com.azure.core.http.HttpPipeline;
import com.azure.management.resources.core.TestBase;
import com.azure.management.resources.fluentcore.profile.AzureProfile;
import com.azure.management.resources.implementation.ResourceManager;

/**
 * The base for resource manager tests.
 */
class ResourceManagerTestBase extends TestBase {
    protected ResourceManager resourceClient;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        resourceClient = ResourceManager
                .authenticate(httpPipeline, profile)
                .withSdkContext(sdkContext)
                .withDefaultSubscription();

    }

    @Override
    protected void cleanUpResources() {

    }
}
