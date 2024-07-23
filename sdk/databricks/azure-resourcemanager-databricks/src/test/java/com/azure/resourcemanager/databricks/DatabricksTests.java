// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.databricks;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.resourcemanager.databricks.models.Sku;
import com.azure.resourcemanager.databricks.models.Workspace;
import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class DatabricksTests extends TestBase {

    private static final Random RANDOM = new Random();

    private static final Region REGION = Region.US_WEST2;
    private String resourceGroupName = "rg" + randomPadding();
    private AzureDatabricksManager databricksManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        databricksManager = AzureDatabricksManager
            .configure().withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        resourceManager = ResourceManager
            .configure().withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile)
            .withDefaultSubscription();

        // use AZURE_RESOURCE_GROUP_NAME if run in LIVE CI
        String testResourceGroup = Configuration.getGlobalConfiguration().get("AZURE_RESOURCE_GROUP_NAME");
        testEnv = !CoreUtils.isNullOrEmpty(testResourceGroup);
        if (testEnv) {
            resourceGroupName = testResourceGroup;
        } else {
            resourceManager.resourceGroups().define(resourceGroupName)
                .withRegion(REGION)
                .create();
        }
    }

    @Override
    protected void afterTest() {
        if (!testEnv) {
            resourceManager.resourceGroups().beginDeleteByName(resourceGroupName);
        }
    }

    @Test
    @LiveOnly
    public void testCrudWorkspace() {
        Workspace workspace = null;
        try {
            String workspaceName = "workspace" + randomPadding();
            String managedResourceGroupId = resourceManager.resourceGroups().getByName(resourceGroupName).id()
                .replace(resourceGroupName, "databricks-" + resourceGroupName);
            // @embedmeStart
            workspace = databricksManager.workspaces().define(workspaceName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withManagedResourceGroupId(managedResourceGroupId)
                .withSku(new Sku().withName("standard"))
                .create();
            // @embedmeEnd

            workspace.refresh();

            Assertions.assertEquals(workspaceName, workspace.name());

            Assertions.assertEquals(workspace.name(), databricksManager.workspaces().getById(workspace.id()).name());

            Assertions.assertTrue(databricksManager.workspaces().listByResourceGroup(resourceGroupName).stream().count() > 0);
        } finally {
            if (workspace != null) {
                databricksManager.workspaces().deleteById(workspace.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
