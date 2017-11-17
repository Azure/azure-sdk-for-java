/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.RestClient;

/**
 * The base for storage manager tests.
 */
public abstract class GraphRbacManagementTest extends TestBase {
    protected static GraphRbacManager graphRbacManager;
    protected static ResourceManager resourceManager;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        graphRbacManager = GraphRbacManager.authenticate(restClient, domain);
        resourceManager = ResourceManager.authenticate(restClient).withSubscription(defaultSubscription);
    }

    @Override
    protected void cleanUpResources() {
    }
}
