// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.discovery;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.discovery.models.ChatModelDeployment;
import com.azure.resourcemanager.discovery.models.ChatModelDeploymentProperties;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live tests for ChatModelDeployment operations against EUAP endpoint.
 *
 * Tests match the comprehensive coverage in Python SDK.
 * Java-specific names: test-cmd-java01 under test-wrksp-java01.
 */
public class ChatModelDeploymentTests extends DiscoveryManagementTest {

    // Use the workspace created by WorkspaceTests
    private static final String WORKSPACE_RESOURCE_GROUP = "olawal";
    private static final String WORKSPACE_NAME = "test-wrksp-java01";
    private static final String DEPLOYMENT_NAME = "test-cmd-java01";

    @Test
    public void testListChatModelDeploymentsByWorkspace() {
        PagedIterable<ChatModelDeployment> deployments
            = discoveryManager.chatModelDeployments().listByWorkspace(WORKSPACE_RESOURCE_GROUP, WORKSPACE_NAME);
        assertNotNull(deployments);

        List<ChatModelDeployment> deploymentList = new ArrayList<>();
        for (ChatModelDeployment deployment : deployments) {
            assertNotNull(deployment.name());
            assertNotNull(deployment.id());
            assertNotNull(deployment.type());
            deploymentList.add(deployment);
        }

        assertNotNull(deploymentList);
    }

    @Test
    public void testGetChatModelDeployment() {
        ChatModelDeployment deployment
            = discoveryManager.chatModelDeployments().get(WORKSPACE_RESOURCE_GROUP, WORKSPACE_NAME, DEPLOYMENT_NAME);

        assertNotNull(deployment);
        assertNotNull(deployment.name());
        assertTrue(deployment.type().equalsIgnoreCase("Microsoft.Discovery/workspaces/chatModelDeployments"));
    }

    @Test
    public void testCreateChatModelDeployment() {
        ChatModelDeploymentProperties properties
            = new ChatModelDeploymentProperties().withModelFormat("OpenAI").withModelName("gpt-4o");

        ChatModelDeployment deployment = discoveryManager.chatModelDeployments()
            .define(DEPLOYMENT_NAME)
            .withRegion("uksouth")
            .withExistingWorkspace(WORKSPACE_RESOURCE_GROUP, WORKSPACE_NAME)
            .withProperties(properties)
            .create();

        assertNotNull(deployment);
        assertNotNull(deployment.id());
        assertNotNull(deployment.name());
    }

    @Test
    public void testDeleteChatModelDeployment() {
        discoveryManager.chatModelDeployments().delete(WORKSPACE_RESOURCE_GROUP, WORKSPACE_NAME, DEPLOYMENT_NAME);
    }
}
