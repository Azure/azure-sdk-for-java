// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.discovery;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.resourcemanager.discovery.models.CustomerManagedKeys;
import com.azure.resourcemanager.discovery.models.Identity;
import com.azure.resourcemanager.discovery.models.KeyVaultProperties;
import com.azure.resourcemanager.discovery.models.PublicNetworkAccess;
import com.azure.resourcemanager.discovery.models.Workspace;
import com.azure.resourcemanager.discovery.models.WorkspaceProperties;
import com.azure.resourcemanager.discovery.fluent.models.WorkspaceInner;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live tests for Workspace operations against EUAP endpoint.
 *
 * Tests match the comprehensive coverage in Python SDK:
 * - test_list_workspaces_by_subscription
 * - test_list_workspaces_by_resource_group
 * - test_get_workspace
 * - test_create_workspace
 * - test_update_workspace
 * - test_delete_workspace
 */
public class WorkspaceTests extends DiscoveryManagementTest {

    // Resource group for CRUD tests
    private static final String WORKSPACE_RESOURCE_GROUP = "olawal";
    // Workspace name used for create/get/update/delete (Java-specific, different from Python)
    private static final String WORKSPACE_NAME = "test-wrksp-java01";

    // Subscription ID used in resource IDs
    private static final String SUBSCRIPTION_ID = "31b0b6a5-2647-47eb-8a38-7d12047ee8ec";

    @Test
    public void testListWorkspacesBySubscription() {
        PagedIterable<Workspace> workspaces = discoveryManager.workspaces().list();
        assertNotNull(workspaces);

        List<Workspace> workspaceList = new ArrayList<>();
        for (Workspace workspace : workspaces) {
            assertNotNull(workspace.name());
            assertNotNull(workspace.id());
            assertNotNull(workspace.type());
            workspaceList.add(workspace);
        }

        assertTrue(workspaceList.size() >= 1, "Expected at least one workspace in subscription");
    }

    @Test
    public void testListWorkspacesByResourceGroup() {
        PagedIterable<Workspace> workspaces
            = discoveryManager.workspaces().listByResourceGroup(WORKSPACE_RESOURCE_GROUP);
        assertNotNull(workspaces);

        List<Workspace> workspaceList = new ArrayList<>();
        for (Workspace workspace : workspaces) {
            assertNotNull(workspace.name());
            assertNotNull(workspace.id());
            workspaceList.add(workspace);
        }

        assertTrue(workspaceList.size() >= 1,
            "Expected at least one workspace in resource group: " + WORKSPACE_RESOURCE_GROUP);
    }

    @Test
    public void testGetWorkspace() {
        Workspace workspace
            = discoveryManager.workspaces().getByResourceGroup(WORKSPACE_RESOURCE_GROUP, WORKSPACE_NAME);

        assertNotNull(workspace);
        assertNotNull(workspace.name());
        assertNotNull(workspace.location());
        assertNotNull(workspace.id());
        assertNotNull(workspace.type());
        assertTrue(workspace.type().equalsIgnoreCase("Microsoft.Discovery/workspaces"));
    }

    @Test
    public void testGetWorkspaceWithResponse() {
        Response<Workspace> response = discoveryManager.workspaces()
            .getByResourceGroupWithResponse(WORKSPACE_RESOURCE_GROUP, WORKSPACE_NAME, null);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        Workspace workspace = response.getValue();
        assertNotNull(workspace);
        assertNotNull(workspace.name());
        assertNotNull(workspace.location());
    }

    @Test
    public void testCreateWorkspace() {
        String createWorkspaceName = "test-wrksp-java03";
        WorkspaceProperties properties = new WorkspaceProperties().withSupercomputerIds(Collections.emptyList())
            .withWorkspaceIdentity(new Identity().withId("/subscriptions/" + SUBSCRIPTION_ID
                + "/resourcegroups/olawal/providers/Microsoft.ManagedIdentity/userAssignedIdentities/myidentity"))
            .withAgentSubnetId("/subscriptions/" + SUBSCRIPTION_ID
                + "/resourceGroups/olawal/providers/Microsoft.Network/virtualNetworks/newapiv/subnets/default3")
            .withPrivateEndpointSubnetId("/subscriptions/" + SUBSCRIPTION_ID
                + "/resourceGroups/olawal/providers/Microsoft.Network/virtualNetworks/newapiv/subnets/default")
            .withWorkspaceSubnetId("/subscriptions/" + SUBSCRIPTION_ID
                + "/resourceGroups/olawal/providers/Microsoft.Network/virtualNetworks/newapiv/subnets/default2")
            .withCustomerManagedKeys(CustomerManagedKeys.ENABLED)
            .withKeyVaultProperties(new KeyVaultProperties().withKeyName("discoverykey")
                .withKeyVaultUri("https://newapik.vault.azure.net/")
                .withKeyVersion("2c9db3cf55d247b4a1c1831fbbdad906"))
            .withLogAnalyticsClusterId("/subscriptions/" + SUBSCRIPTION_ID
                + "/resourceGroups/olawal/providers/Microsoft.OperationalInsights/clusters/mycluse")
            .withPublicNetworkAccess(PublicNetworkAccess.DISABLED);

        Workspace workspace = discoveryManager.workspaces()
            .define(createWorkspaceName)
            .withRegion("uksouth")
            .withExistingResourceGroup(WORKSPACE_RESOURCE_GROUP)
            .withProperties(properties)
            .create();

        assertNotNull(workspace);
        assertNotNull(workspace.id());
        assertNotNull(workspace.name());
    }

    @Test
    public void testUpdateWorkspace() {
        // Use service client directly with a fresh inner model to avoid sending
        // read-only fields (location, id, name, type) in the PATCH body
        WorkspaceProperties updateProperties = new WorkspaceProperties().withKeyVaultProperties(
            new KeyVaultProperties().withKeyName("discoverykey").withKeyVersion("956de2fc802f49eba81ddcc348ebc27c"));

        WorkspaceInner patchBody = new WorkspaceInner().withProperties(updateProperties);

        WorkspaceInner updated = discoveryManager.serviceClient()
            .getWorkspaces()
            .update(WORKSPACE_RESOURCE_GROUP, WORKSPACE_NAME, patchBody);

        assertNotNull(updated);
        assertNotNull(updated.id());
    }

    @Test
    @Disabled("Disabled workspace deletion test to avoid impacting other tests that rely on this workspace.")
    public void testDeleteWorkspace() {
        discoveryManager.workspaces().deleteByResourceGroup(WORKSPACE_RESOURCE_GROUP, WORKSPACE_NAME);
    }
}
