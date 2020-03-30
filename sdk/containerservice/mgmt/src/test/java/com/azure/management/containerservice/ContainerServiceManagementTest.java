/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.containerservice;


import com.azure.management.RestClient;
import com.azure.management.containerservice.implementation.ContainerServiceManager;
import com.azure.management.resources.core.TestBase;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.implementation.ResourceManager;

public class ContainerServiceManagementTest extends TestBase {
    protected ResourceManager resourceManager;
    protected ContainerServiceManager containerServiceManager;
    protected String RG_NAME = "";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        RG_NAME = generateRandomResourceName("javaacsrg", 15);

        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(defaultSubscription);

       containerServiceManager = ContainerServiceManager
                .authenticate(restClient, defaultSubscription);

       resourceManager.resourceGroups()
               .define(RG_NAME)
               .withRegion(Region.US_EAST)
               .create();
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(RG_NAME);
    }
}