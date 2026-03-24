// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.discovery;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.discovery.models.Tool;
import com.azure.resourcemanager.discovery.models.ToolProperties;
import com.azure.resourcemanager.discovery.fluent.models.ToolInner;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live tests for Tool operations against EUAP endpoint.
 *
 * Tests match the comprehensive coverage in Python SDK.
 * Java-specific resource name: test-tool-java01 (different from Python's test-tool-50d87c62).
 */
public class ToolTests extends DiscoveryManagementTest {

    private static final String TOOL_RESOURCE_GROUP = "olawal";
    private static final String TOOL_NAME = "test-tool-java01";

    @Test
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
    public void testGetTool() {
        Tool tool = discoveryManager.tools().getByResourceGroup(TOOL_RESOURCE_GROUP, TOOL_NAME);
        assertNotNull(tool);
        assertNotNull(tool.name());
        assertNotNull(tool.id());
    }

    @Test
    public void testCreateTool() {
        // Build infra as Java objects so BinaryData.fromObject serializes as raw JSON
        Map<String, Object> minResources = new LinkedHashMap<>();
        minResources.put("cpu", "1");
        minResources.put("ram", "1Gi");
        minResources.put("storage", "32");
        minResources.put("gpu", "0");

        Map<String, Object> maxResources = new LinkedHashMap<>();
        maxResources.put("cpu", "2");
        maxResources.put("ram", "1Gi");
        maxResources.put("storage", "64");
        maxResources.put("gpu", "0");

        Map<String, Object> compute = new LinkedHashMap<>();
        compute.put("min_resources", minResources);
        compute.put("max_resources", maxResources);
        compute.put("recommended_sku", Arrays.asList("Standard_D4s_v6"));
        compute.put("pool_type", "static");
        compute.put("pool_size", 1);

        Map<String, Object> image = new LinkedHashMap<>();
        image.put("acr", "demodiscoveryacr.azurecr.io/molpredictor:latest");

        Map<String, Object> worker = new LinkedHashMap<>();
        worker.put("name", "worker");
        worker.put("infra_type", "container");
        worker.put("image", image);
        worker.put("compute", compute);

        // Build actions
        Map<String, Object> actionProp = new LinkedHashMap<>();
        actionProp.put("type", "string");
        actionProp.put("description", "The property to predict.");
        Map<String, Object> schemaProps = new LinkedHashMap<>();
        schemaProps.put("action", actionProp);
        Map<String, Object> inputSchema = new LinkedHashMap<>();
        inputSchema.put("type", "object");
        inputSchema.put("properties", schemaProps);
        inputSchema.put("required", Arrays.asList("action"));

        Map<String, Object> action = new LinkedHashMap<>();
        action.put("name", "predict");
        action.put("description", "Predict molecular properties for SMILES strings.");
        action.put("input_schema", inputSchema);
        action.put("command", "python molpredictor.py --action {{ action }}");
        action.put("infra_node", "worker");

        // Build definitionContent map using BinaryData.fromObject (writes raw JSON, not quoted strings)
        Map<String, BinaryData> definitionContent = new HashMap<>();
        definitionContent.put("name", BinaryData.fromObject("molpredictor"));
        definitionContent.put("description",
            BinaryData.fromObject("Molecular property prediction for single SMILES strings."));
        definitionContent.put("version", BinaryData.fromObject("1.0.0"));
        definitionContent.put("category", BinaryData.fromObject("cheminformatics"));
        definitionContent.put("license", BinaryData.fromObject("MIT"));
        definitionContent.put("infra", BinaryData.fromObject(Arrays.asList(worker)));
        definitionContent.put("actions", BinaryData.fromObject(Arrays.asList(action)));

        ToolProperties toolProps = new ToolProperties().withVersion("1.0.0").withDefinitionContent(definitionContent);

        Tool tool = discoveryManager.tools()
            .define(TOOL_NAME)
            .withRegion("uksouth")
            .withExistingResourceGroup(TOOL_RESOURCE_GROUP)
            .withProperties(toolProps)
            .create();

        assertNotNull(tool);
        assertNotNull(tool.id());
        assertNotNull(tool.name());
    }

    @Test
    public void testUpdateTool() {
        // Use service client directly with a fresh inner model to avoid sending
        // read-only fields (location) in the PATCH body
        Map<String, String> tags = new HashMap<>();
        tags.put("SkipAutoDeleteTill", "2026-12-31");

        ToolInner patchBody = new ToolInner().withTags(tags);

        ToolInner updated
            = discoveryManager.serviceClient().getTools().update(TOOL_RESOURCE_GROUP, TOOL_NAME, patchBody);

        assertNotNull(updated);
        assertNotNull(updated.id());
    }

    @Test
    public void testDeleteTool() {
        discoveryManager.tools().deleteByResourceGroup(TOOL_RESOURCE_GROUP, TOOL_NAME);
    }
}
