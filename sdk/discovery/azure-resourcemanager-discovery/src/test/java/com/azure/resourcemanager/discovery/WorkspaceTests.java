// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.discovery;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.resourcemanager.discovery.models.Workspace;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live tests for Workspace operations against EUAP endpoint.
 * 
 * Tests match the comprehensive coverage in Python SDK:
 * - test_list_workspaces_by_subscription
 * - test_list_workspaces_by_resource_group
 * - test_get_workspace
 */
public class WorkspaceTests extends DiscoveryManagementTest {

    // Resource group and workspace that exist in the test environment
    private static final String WORKSPACE_RESOURCE_GROUP = "newapiversiontest";
    private static final String WORKSPACE_NAME = "wrksptest44";

    @Test
    public void testListWorkspacesBySubscription() {
        // Test listing workspaces in the subscription
        PagedIterable<Workspace> workspaces = discoveryManager.workspaces().list();
        assertNotNull(workspaces);

        // Collect all workspaces to verify we got at least one
        List<Workspace> workspaceList = new ArrayList<>();
        for (Workspace workspace : workspaces) {
            assertNotNull(workspace.name());
            assertNotNull(workspace.id());
            assertNotNull(workspace.type());
            workspaceList.add(workspace);
        }

        // Verify at least one workspace exists (matching Python test assertion)
        assertTrue(workspaceList.size() >= 1, "Expected at least one workspace in subscription");
    }

    @Test
    public void testListWorkspacesByResourceGroup() {
        // Test listing workspaces in a specific resource group
        PagedIterable<Workspace> workspaces
            = discoveryManager.workspaces().listByResourceGroup(WORKSPACE_RESOURCE_GROUP);
        assertNotNull(workspaces);

        // Collect all workspaces to verify we got at least one
        List<Workspace> workspaceList = new ArrayList<>();
        for (Workspace workspace : workspaces) {
            assertNotNull(workspace.name());
            assertNotNull(workspace.id());
            workspaceList.add(workspace);
        }

        // Verify at least one workspace exists (matching Python test assertion)
        assertTrue(workspaceList.size() >= 1,
            "Expected at least one workspace in resource group: " + WORKSPACE_RESOURCE_GROUP);
    }

    @Test
    public void testGetWorkspace() {
        // Test getting a specific workspace by name (matching Python test_get_workspace)
        Workspace workspace
            = discoveryManager.workspaces().getByResourceGroup(WORKSPACE_RESOURCE_GROUP, WORKSPACE_NAME);

        assertNotNull(workspace);
        assertNotNull(workspace.name());
        assertNotNull(workspace.location());
        assertNotNull(workspace.id());
        assertNotNull(workspace.type());

        // Verify the workspace has expected properties (type may be lowercase or PascalCase)
        assertTrue(workspace.type().equalsIgnoreCase("Microsoft.Discovery/workspaces"));
    }

    @Test
    public void testGetWorkspaceWithResponse() {
        // Test getting a workspace with full response details
        Response<Workspace> response = discoveryManager.workspaces()
            .getByResourceGroupWithResponse(WORKSPACE_RESOURCE_GROUP, WORKSPACE_NAME, null);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        Workspace workspace = response.getValue();
        assertNotNull(workspace);
        assertNotNull(workspace.name());
        assertNotNull(workspace.location());
    }
}
