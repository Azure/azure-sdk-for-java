// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluent.models.DeploymentStackInner;
import com.azure.resourcemanager.resources.models.ActionOnUnmanage;
import com.azure.resourcemanager.resources.models.DenySettings;
import com.azure.resourcemanager.resources.models.DenySettingsMode;
import com.azure.resourcemanager.resources.models.DeploymentStacksDeleteDetachEnum;
import com.azure.resourcemanager.resources.models.DeploymentStacksParametersLink;
import com.azure.resourcemanager.resources.models.DeploymentStacksTemplateLink;
import com.azure.resourcemanager.resources.models.ResourceGroups;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class DeploymentStacksTests extends ResourceManagementTest {

    private ResourceGroups resourceGroups;

    private String testId;
    private String rgName;

    private static final Region REGION = Region.US_WEST3;
    private static final String TEMPLATE_URI = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/master/quickstarts/microsoft.network/vnet-two-subnets/azuredeploy.json";
    private static final String PARAMETERS_URI = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/master/quickstarts/microsoft.network/vnet-two-subnets/azuredeploy.parameters.json";
    private static final String CONTENT_VERSION = "1.0.0.0";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        super.initializeClients(httpPipeline, profile);
        testId = generateRandomResourceName("", 9);
        resourceGroups = resourceClient.resourceGroups();
        rgName = "rg" + testId;
        resourceGroups.define(rgName)
            .withRegion(REGION)
            .create();
    }

    @Override
    protected void cleanUpResources() {
        resourceGroups.beginDeleteByName(rgName);
    }

    @Test
    public void testDeploymentStacks() {
        final String dpName = "dpA" + testId;

        DeploymentStackInner deploymentStack = resourceClient.deploymentStackClient().getDeploymentStacks()
            .createOrUpdateAtResourceGroup(rgName, dpName,
                new DeploymentStackInner()
                    .withTags(Collections.singletonMap("usage", "test"))
                    .withTemplateLink(
                        new DeploymentStacksTemplateLink()
                            .withUri(TEMPLATE_URI)
                            .withContentVersion(CONTENT_VERSION))
                    .withParametersLink(
                        new DeploymentStacksParametersLink()
                            .withUri(PARAMETERS_URI)
                            .withContentVersion(CONTENT_VERSION))
                    .withActionOnUnmanage(
                        new ActionOnUnmanage()
                            .withResources(DeploymentStacksDeleteDetachEnum.DELETE)
                            .withResourceGroups(DeploymentStacksDeleteDetachEnum.DETACH)
                            .withManagementGroups(DeploymentStacksDeleteDetachEnum.DETACH))
                    .withDenySettings(new DenySettings()
                        .withMode(DenySettingsMode.NONE)));

        Assertions.assertEquals(dpName, deploymentStack.name());
        Assertions.assertEquals(DeploymentStacksDeleteDetachEnum.DELETE, deploymentStack.actionOnUnmanage().resources());

        deploymentStack = resourceClient.deploymentStackClient().getDeploymentStacks()
            .getByResourceGroup(rgName, dpName);
        Assertions.assertEquals(dpName, deploymentStack.name());
        Assertions.assertEquals(DeploymentStacksDeleteDetachEnum.DELETE, deploymentStack.actionOnUnmanage().resources());
    }
}
