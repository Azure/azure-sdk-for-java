/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources;

import com.azure.management.RestClient;
import com.azure.management.resources.core.TestBase;
import com.azure.management.resources.implementation.ResourceManager;

/**
 * The base for resource manager tests.
 */
class ResourceManagerTestBase extends TestBase {
    protected ResourceManager resourceClient;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        resourceClient = ResourceManager
                .authenticate(restClient)
                .withSdkContext(sdkContext)
                .withSubscription(defaultSubscription);
    }

    @Override
    protected void cleanUpResources() {

    }
}
