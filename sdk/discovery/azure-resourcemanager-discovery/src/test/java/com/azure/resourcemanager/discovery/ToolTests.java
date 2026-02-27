// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.discovery;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.discovery.models.Tool;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live tests for Tool operations against EUAP endpoint.
 * 
 * Tools are top-level resources under ResourceGroup.
 */
public class ToolTests extends DiscoveryManagementTest {

    private static final String TOOL_RESOURCE_GROUP = "newapiversiontest";
    private static final String TOOL_NAME = "test-tool";

    @Test
    @Disabled("Backend may not have tools in test subscription")
    public void testListToolsBySubscription() {
        PagedIterable<Tool> tools = discoveryManager.tools().list();
        assertNotNull(tools);

        List<Tool> toolList = new ArrayList<>();
        for (Tool tool : tools) {
            assertNotNull(tool.name());
            assertNotNull(tool.id());
            toolList.add(tool);
        }

        assertNotNull(toolList);
    }

    @Test
    @Disabled("Backend may not have tools in test resource group")
    public void testListToolsByResourceGroup() {
        PagedIterable<Tool> tools = discoveryManager.tools().listByResourceGroup(TOOL_RESOURCE_GROUP);
        assertNotNull(tools);

        List<Tool> toolList = new ArrayList<>();
        for (Tool tool : tools) {
            assertNotNull(tool.name());
            assertNotNull(tool.id());
            toolList.add(tool);
        }

        assertNotNull(toolList);
    }

    @Test
    @Disabled("Requires existing tool")
    public void testGetTool() {
        Tool tool = discoveryManager.tools().getByResourceGroup(TOOL_RESOURCE_GROUP, TOOL_NAME);
        assertNotNull(tool);
        assertNotNull(tool.name());
        assertNotNull(tool.id());
    }

    @Test
    @Disabled("Create is a mutating operation - requires proper tool setup")
    public void testCreateTool() {
        // Tool creation requires proper configuration
        // This test is a placeholder for integration testing
    }

    @Test
    @Disabled("Update is a mutating operation - requires existing tool")
    public void testUpdateTool() {
        // Tool update requires an existing tool
        // This test is a placeholder for integration testing
    }

    @Test
    @Disabled("Delete is a mutating operation - requires existing tool")
    public void testDeleteTool() {
        // Tool deletion requires an existing tool
        // This test is a placeholder for integration testing
    }
}
