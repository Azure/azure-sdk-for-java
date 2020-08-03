// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerinstance;

import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.resources.core.TestBase;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import java.io.IOException;

public class ContainerInstanceManagementTest extends TestBase {
    protected ContainerInstanceManager containerInstanceManager;
    protected String rgName = "";

    public ContainerInstanceManagementTest() {
    }

    ContainerInstanceManagementTest(RunCondition runCondition) {
        super(runCondition);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) throws IOException {
        rgName = generateRandomResourceName("rg", 20);
        containerInstanceManager = ContainerInstanceManager.authenticate(httpPipeline, profile, sdkContext);
    }

    @Override
    protected void cleanUpResources() {
        try {
            containerInstanceManager.resourceManager().resourceGroups().beginDeleteByName(rgName);
        } catch (Exception e) {
        }
    }
}
