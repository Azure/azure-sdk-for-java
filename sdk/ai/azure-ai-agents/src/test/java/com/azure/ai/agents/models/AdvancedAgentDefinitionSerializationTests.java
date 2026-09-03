// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AdvancedAgentDefinitionSerializationTests {

    @Test
    public void structuredOutputCalendarSchemaRoundTrips() throws IOException {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("name", Collections.singletonMap("type", "string"));
        properties.put("date", field("string", "Date in YYYY-MM-DD format"));
        properties.put("participants", arrayField("string"));
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", Arrays.asList("name", "date", "participants"));
        schema.put("additionalProperties", false);

        ResponseFormatJsonSchemaInner schemaModel
            = BinaryData.fromObject(schema).toObject(ResponseFormatJsonSchemaInner.class);
        PromptAgentDefinition original
            = new PromptAgentDefinition("gpt-4o").setInstructions("Extract a calendar event.")
                .setText(new PromptAgentDefinitionTextOptions()
                    .setFormat(new TextResponseFormatJsonSchema("CalendarEvent", schemaModel).setStrict(true)));

        String json = serialize(original);
        assertTrue(json.contains("\"name\":\"CalendarEvent\""));
        assertTrue(json.contains("\"type\":\"json_schema\""));
        assertTrue(json.contains("\"strict\":true"));
        assertTrue(json.contains("\"additionalProperties\":false"));
        assertTrue(json.contains("\"participants\""));

        PromptAgentDefinition deserialized;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            deserialized = PromptAgentDefinition.fromJson(reader);
        }
        assertNotNull(deserialized.getText());
        assertInstanceOf(TextResponseFormatJsonSchema.class, deserialized.getText().getFormat());
        TextResponseFormatJsonSchema format = (TextResponseFormatJsonSchema) deserialized.getText().getFormat();
        assertEquals("CalendarEvent", format.getName());
        assertEquals(Boolean.TRUE, format.isStrict());
    }

    @Test
    public void workflowDefinitionRoundTrips() throws IOException {
        String workflow = "kind: workflow\ntrigger:\n  kind: OnConversationStart\n  actions: []\n";
        WorkflowAgentDefinition original = new WorkflowAgentDefinition().setWorkflow(workflow);

        String json = serialize(original);
        assertTrue(json.contains("\"kind\":\"workflow\""));
        assertTrue(json.contains("OnConversationStart"));

        AgentDefinition deserialized;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            deserialized = AgentDefinition.fromJson(reader);
        }
        assertInstanceOf(WorkflowAgentDefinition.class, deserialized);
        assertEquals(workflow, ((WorkflowAgentDefinition) deserialized).getWorkflow());
    }

    @Test
    public void endpointRoutingSerializesResponsesProtocol() throws IOException {
        AgentEndpointConfig endpoint = new AgentEndpointConfig()
            .setVersionSelector(new VersionSelector().setVersionSelectionRules(
                Collections.singletonList(new FixedRatioVersionSelectionRule(100).setAgentVersion("2"))))
            .setProtocolConfiguration(new ProtocolConfiguration().setResponses(new ResponsesProtocolConfiguration()));
        UpdateAgentDetailsOptions update = new UpdateAgentDetailsOptions().setAgentEndpoint(endpoint);

        String json = serialize(update);
        assertTrue(json.contains("\"agent_endpoint\""));
        assertTrue(json.contains("\"agent_version\":\"2\""));
        assertTrue(json.contains("\"traffic_percentage\":100"));
        assertTrue(json.contains("\"type\":\"FixedRatio\""));
        assertTrue(json.contains("\"responses\":{}"));
    }

    @Test
    public void multitoolDefinitionPreservesToolOrderAndTypes() throws IOException {
        Map<String, BinaryData> functionParameters = new LinkedHashMap<>();
        functionParameters.put("type", BinaryData.fromObject("object"));
        functionParameters.put("properties",
            BinaryData.fromObject(Collections.singletonMap("report_name", Collections.singletonMap("type", "string"))));
        functionParameters.put("required", BinaryData.fromObject(Collections.singletonList("report_name")));
        functionParameters.put("additionalProperties", BinaryData.fromObject(false));

        PromptAgentDefinition original = new PromptAgentDefinition("gpt-4o")
            .setTools(Arrays.asList(new FileSearchTool(Collections.singletonList("vs_test")),
                new CodeInterpreterTool().setContainer(new AutoCodeInterpreterToolParameter()),
                new FunctionTool("save_analysis", functionParameters, true)));

        String json = serialize(original);
        int fileSearch = json.indexOf("\"type\":\"file_search\"");
        int codeInterpreter = json.indexOf("\"type\":\"code_interpreter\"");
        int function = json.indexOf("\"type\":\"function\"");
        assertTrue(fileSearch >= 0 && codeInterpreter > fileSearch && function > codeInterpreter);
        assertTrue(json.contains("\"vector_store_ids\":[\"vs_test\"]"));
        assertTrue(json.contains("\"name\":\"save_analysis\""));
        assertTrue(json.contains("\"strict\":true"));
        assertFalse(json.contains("additionalProperties\":true"));

        PromptAgentDefinition deserialized;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            deserialized = PromptAgentDefinition.fromJson(reader);
        }
        assertEquals(3, deserialized.getTools().size());
        assertInstanceOf(FileSearchTool.class, deserialized.getTools().get(0));
        assertInstanceOf(CodeInterpreterTool.class, deserialized.getTools().get(1));
        assertInstanceOf(FunctionTool.class, deserialized.getTools().get(2));
    }

    private static Map<String, Object> field(String type, String description) {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("type", type);
        field.put("description", description);
        return field;
    }

    private static Map<String, Object> arrayField(String itemType) {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("type", "array");
        field.put("items", Collections.singletonMap("type", itemType));
        return field;
    }

    private static String serialize(JsonSerializableModel model) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(output)) {
            model.write(writer);
        }
        return output.toString("UTF-8");
    }

    private interface JsonSerializableModel {
        void write(JsonWriter writer) throws IOException;
    }

    private static String serialize(PromptAgentDefinition model) throws IOException {
        return serialize(model::toJson);
    }

    private static String serialize(WorkflowAgentDefinition model) throws IOException {
        return serialize(model::toJson);
    }

    private static String serialize(UpdateAgentDetailsOptions model) throws IOException {
        return serialize(model::toJson);
    }
}
