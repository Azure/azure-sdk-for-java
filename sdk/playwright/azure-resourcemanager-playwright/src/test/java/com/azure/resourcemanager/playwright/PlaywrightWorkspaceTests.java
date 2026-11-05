// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.playwright;

import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.playwright.models.EnablementStatus;
import com.azure.resourcemanager.playwright.models.PlaywrightWorkspace;
import com.azure.resourcemanager.playwright.models.PlaywrightWorkspaceProperties;
import com.azure.resourcemanager.playwright.models.PlaywrightWorkspaceUpdateProperties;
import com.azure.resourcemanager.playwright.models.ProvisioningState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LiveOnly
public class PlaywrightWorkspaceTests {
    private final ClientLogger logger = new ClientLogger(PlaywrightWorkspaceTests.class);
    private PlaywrightManager manager;
    private String resourceGroupName;
    private String workspaceName;

    @Test
    public void testCreateUpdateGetWorkspace() {
        String subscriptionId = requireConfiguration("AZURE_SUBSCRIPTION_ID");
        String tenantId = requireConfiguration("AZURE_TENANT_ID");
        resourceGroupName = requireConfiguration("AZURE_RESOURCE_GROUP_NAME");
        String location = requireConfiguration("AZURE_PLAYWRIGHT_LOCATION");
        String storageUri = requireConfiguration("AZURE_PLAYWRIGHT_STORAGE_URI");

        AzureProfile profile = new AzureProfile(tenantId, subscriptionId, AzureCloud.AZURE_PUBLIC_CLOUD);
        manager = PlaywrightManager.authenticate(
            new DefaultAzureCredentialBuilder().authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build(),
            profile);

        Map<String, String> tags = new HashMap<>();
        tags.put("Environment", "Test");
        tags.put("CreatedBy", "JavaSDKTest");

        // Use a unique test-owned name within the service's 24-character limit.
        workspaceName = "javasdk-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        logger.info("Creating workspace '{}' in resource group '{}'.", workspaceName, resourceGroupName);
        PlaywrightWorkspace workspace = manager.playwrightWorkspaces()
            .define(workspaceName)
            .withRegion(location)
            .withExistingResourceGroup(resourceGroupName)
            .withTags(tags)
            .withProperties(new PlaywrightWorkspaceProperties().withLocalAuth(EnablementStatus.DISABLED)
                .withReporting(EnablementStatus.ENABLED)
                .withStorageUri(storageUri))
            .create();

        assertWorkspace(workspace, storageUri, EnablementStatus.DISABLED, tags);
        logger.info("Create verified: reporting enabled, local authentication disabled.");

        PlaywrightWorkspace updatedWorkspace = workspace.update()
            .withProperties(new PlaywrightWorkspaceUpdateProperties().withLocalAuth(EnablementStatus.ENABLED)
                .withReporting(EnablementStatus.ENABLED)
                .withStorageUri(storageUri))
            .apply();

        assertWorkspace(updatedWorkspace, storageUri, EnablementStatus.ENABLED, tags);
        logger.info("Update verified: local authentication enabled.");

        PlaywrightWorkspace finalWorkspace = manager.playwrightWorkspaces()
            .getByResourceGroupWithResponse(resourceGroupName, workspaceName, Context.NONE)
            .getValue();

        assertWorkspace(finalWorkspace, storageUri, EnablementStatus.ENABLED, tags);
        Assertions.assertEquals(workspace.id(), finalWorkspace.id());
        logger.info("Get verified: provisioning state '{}', dataplane URI '{}'.",
            finalWorkspace.properties().provisioningState(), finalWorkspace.properties().dataplaneUri());
    }

    @AfterEach
    public void deleteWorkspace() {
        if (workspaceName != null) {
            // Also attempt cleanup when creation fails after Azure has accepted the request.
            logger.info("Deleting test workspace '{}'.", workspaceName);
            manager.playwrightWorkspaces().deleteByResourceGroup(resourceGroupName, workspaceName);
            ManagementException exception = Assertions.assertThrows(ManagementException.class,
                () -> manager.playwrightWorkspaces().getByResourceGroup(resourceGroupName, workspaceName));
            Assertions.assertEquals(404, exception.getResponse().getStatusCode());
            logger.info("Delete verified: test workspace no longer exists.");
        }
    }

    private void assertWorkspace(PlaywrightWorkspace workspace, String storageUri, EnablementStatus localAuth,
        Map<String, String> tags) {
        Assertions.assertNotNull(workspace);
        Assertions.assertEquals(workspaceName, workspace.name());
        Assertions.assertNotNull(workspace.id());
        Assertions.assertEquals(tags, workspace.tags());
        Assertions.assertNotNull(workspace.properties());
        Assertions.assertEquals(ProvisioningState.SUCCEEDED, workspace.properties().provisioningState());
        Assertions.assertEquals(EnablementStatus.ENABLED, workspace.properties().reporting());
        Assertions.assertEquals(storageUri, workspace.properties().storageUri());
        Assertions.assertEquals(localAuth, workspace.properties().localAuth());
        Assertions.assertNotNull(workspace.properties().dataplaneUri());
        Assertions.assertFalse(workspace.properties().dataplaneUri().isEmpty());
    }

    private String requireConfiguration(String name) {
        String value = Configuration.getGlobalConfiguration().get(name);
        if (value == null || value.trim().isEmpty()) {
            throw logger
                .logExceptionAsError(new IllegalStateException("Set " + name + " before running the live test."));
        }
        return value.trim();
    }
}
