// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.discovery;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.discovery.models.ChatModelDeployment;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live tests for ChatModelDeployment operations against EUAP endpoint.
 * 
 * Tests match the comprehensive coverage in Python SDK:
 * - test_list_chat_model_deployments_by_workspace
 */
public class ChatModelDeploymentTests extends DiscoveryManagementTest {

    // Resource group and workspace that exist in the test environment
    private static final String WORKSPACE_RESOURCE_GROUP = "newapiversiontest";
    private static final String WORKSPACE_NAME = "wrksptest44";

    @Test
    public void testListChatModelDeploymentsByWorkspace() {
        // Test listing chat model deployments in a workspace 
        // (matching Python test_list_chat_model_deployments_by_workspace)
        PagedIterable<ChatModelDeployment> deployments
            = discoveryManager.chatModelDeployments().listByWorkspace(WORKSPACE_RESOURCE_GROUP, WORKSPACE_NAME);
        assertNotNull(deployments);

        // Collect all deployments
        List<ChatModelDeployment> deploymentList = new ArrayList<>();
        for (ChatModelDeployment deployment : deployments) {
            assertNotNull(deployment.name());
            assertNotNull(deployment.id());
            assertNotNull(deployment.type());
            deploymentList.add(deployment);
        }

        // Deployments list should be a valid list (may be empty)
        assertNotNull(deploymentList);
    }

    @Test
    public void testGetChatModelDeploymentIfExists() {
        // First list deployments to find one to get
        PagedIterable<ChatModelDeployment> deployments
            = discoveryManager.chatModelDeployments().listByWorkspace(WORKSPACE_RESOURCE_GROUP, WORKSPACE_NAME);

        ChatModelDeployment firstDeployment = null;
        for (ChatModelDeployment deployment : deployments) {
            firstDeployment = deployment;
            break;
        }

        if (firstDeployment != null) {
            String deploymentName = firstDeployment.name();

            // Get the deployment by name
            ChatModelDeployment retrieved
                = discoveryManager.chatModelDeployments().get(WORKSPACE_RESOURCE_GROUP, WORKSPACE_NAME, deploymentName);

            assertNotNull(retrieved);
            assertNotNull(retrieved.name());
            // Type may be lowercase or PascalCase
            assertTrue(retrieved.type().equalsIgnoreCase("Microsoft.Discovery/workspaces/chatModelDeployments"));
        }
        // If no deployments exist, test passes (nothing to get)
    }
}
