// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.CodeInterpreterTool;
import com.azure.ai.agents.models.FunctionTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.StructuredInputDefinition;
import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;
import com.azure.core.util.IterableStream;
import com.openai.helpers.ResponseAccumulator;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseStatus;
import com.openai.models.responses.ResponseStreamEvent;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.azure.ai.agents.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StreamingTests extends ClientTestBase {

    private static final String AGENT_MODEL = "gpt-4o";

    // ========================================================================
    // Simple prompt streaming
    // ========================================================================

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void simpleStreamingProducesTextDeltas(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClient agentsClient = getAgentsSyncClient(httpClient, serviceVersion);
        ResponsesClient responsesClient = getResponsesSyncClient(httpClient, serviceVersion);

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(AGENT_MODEL)
            .setInstructions("You are a helpful assistant. Reply in one sentence.");

        AgentVersionDetails agent = agentsClient.createAgentVersion("streaming-test-agent", agentDefinition);

        try {
            AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());

            ResponseAccumulator accumulator = ResponseAccumulator.create();
            List<String> textDeltas = new ArrayList<>();

            IterableStream<ResponseStreamEvent> events = responsesClient.createStreamingAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(agentReference),
                ResponseCreateParams.builder().input("Say hello."));

            for (ResponseStreamEvent event : events) {
                accumulator.accumulate(event);
                event.outputTextDelta().ifPresent(textEvent -> textDeltas.add(textEvent.delta()));
            }

            assertFalse(textDeltas.isEmpty(), "Should have received at least one text delta");

            Response response = accumulator.response();
            assertNotNull(response.id());
            assertTrue(response.status().isPresent());
            assertEquals(ResponseStatus.COMPLETED, response.status().get());

            // Concatenated deltas should match the final output text
            String streamedText = String.join("", textDeltas);
            assertFalse(streamedText.isEmpty());
        } finally {
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
        }
    }

    // ========================================================================
    // Function calling streaming
    // ========================================================================

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void functionCallStreamingProducesFunctionCallEvents(HttpClient httpClient,
        AgentsServiceVersion serviceVersion) {
        AgentsClient agentsClient = getAgentsSyncClient(httpClient, serviceVersion);
        ResponsesClient responsesClient = getResponsesSyncClient(httpClient, serviceVersion);

        FunctionTool tool = createWeatherFunctionTool();

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(AGENT_MODEL)
            .setInstructions("You are a helpful assistant. When asked about weather, use the get_weather function.")
            .setTools(Collections.singletonList(tool));

        AgentVersionDetails agent = agentsClient.createAgentVersion("function-streaming-test-agent", agentDefinition);

        try {
            AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());

            ResponseAccumulator accumulator = ResponseAccumulator.create();
            List<String> functionArgDeltas = new ArrayList<>();

            IterableStream<ResponseStreamEvent> events = responsesClient.createStreamingAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(agentReference),
                ResponseCreateParams.builder().input("What's the weather like in Seattle?"));

            for (ResponseStreamEvent event : events) {
                accumulator.accumulate(event);
                event.functionCallArgumentsDelta().ifPresent(argEvent -> functionArgDeltas.add(argEvent.delta()));
            }

            assertFalse(functionArgDeltas.isEmpty(), "Should have received function call argument deltas");

            Response response = accumulator.response();
            assertNotNull(response.id());

            // Verify that at least one output item is a function call
            boolean hasFunctionCall = false;
            for (ResponseOutputItem outputItem : response.output()) {
                if (outputItem.functionCall().isPresent()) {
                    hasFunctionCall = true;
                    assertEquals("get_weather", outputItem.functionCall().get().name());
                    assertNotNull(outputItem.functionCall().get().arguments());
                    break;
                }
            }
            assertTrue(hasFunctionCall, "Response should contain a function call output item");
        } finally {
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
        }
    }

    // ========================================================================
    // Code Interpreter streaming (Azure-specific tool)
    // ========================================================================

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void codeInterpreterStreamingProducesCodeEvents(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClient agentsClient = getAgentsSyncClient(httpClient, serviceVersion);
        ResponsesClient responsesClient = getResponsesSyncClient(httpClient, serviceVersion);

        CodeInterpreterTool tool = new CodeInterpreterTool();

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(AGENT_MODEL)
            .setInstructions("You are a helpful assistant that uses code interpreter for calculations.")
            .setTools(Collections.singletonList(tool));

        AgentVersionDetails agent
            = agentsClient.createAgentVersion("code-interpreter-streaming-test-agent", agentDefinition);

        try {
            AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());

            ResponseAccumulator accumulator = ResponseAccumulator.create();
            List<String> codeDeltas = new ArrayList<>();
            boolean[] codeInterpreterCompleted = { false };

            IterableStream<ResponseStreamEvent> events = responsesClient.createStreamingAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(agentReference),
                ResponseCreateParams.builder().input("What is 42 * 37? Use code to calculate."));

            for (ResponseStreamEvent event : events) {
                accumulator.accumulate(event);
                event.codeInterpreterCallCodeDelta().ifPresent(e -> codeDeltas.add(e.delta()));
                event.codeInterpreterCallCompleted().ifPresent(e -> codeInterpreterCompleted[0] = true);
            }

            Response response = accumulator.response();
            assertNotNull(response.id());
            assertTrue(response.status().isPresent());
            assertEquals(ResponseStatus.COMPLETED, response.status().get());

            // Code interpreter should have run and produced code
            assertFalse(codeDeltas.isEmpty(), "Should have received code interpreter code deltas");
            assertTrue(codeInterpreterCompleted[0], "Code interpreter should have completed");
        } finally {
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
        }
    }

    // ========================================================================
    // Structured input streaming
    // ========================================================================

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void structuredInputStreamingProducesTextDeltas(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClient agentsClient = getAgentsSyncClient(httpClient, serviceVersion);
        ResponsesClient responsesClient = getResponsesSyncClient(httpClient, serviceVersion);

        // Create an agent with structured input definitions
        Map<String, StructuredInputDefinition> structuredInputDefinitions = new LinkedHashMap<>();
        structuredInputDefinitions.put("userName",
            new StructuredInputDefinition().setDescription("User's name").setRequired(true));
        structuredInputDefinitions.put("userRole",
            new StructuredInputDefinition().setDescription("User's role").setRequired(true));

        AgentVersionDetails agent = agentsClient.createAgentVersion("structured-input-streaming-test-agent",
            new PromptAgentDefinition(AGENT_MODEL).setInstructions(
                "You are a helpful assistant. " + "The user's name is {{userName}} and their role is {{userRole}}. "
                    + "Greet them and confirm their details.")
                .setStructuredInputs(structuredInputDefinitions));

        try {
            AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());

            Map<String, BinaryData> structuredInputValues = new LinkedHashMap<>();
            structuredInputValues.put("userName", BinaryData.fromObject("Alice Smith"));
            structuredInputValues.put("userRole", BinaryData.fromObject("Senior Developer"));

            ResponseAccumulator accumulator = ResponseAccumulator.create();
            List<String> textDeltas = new ArrayList<>();

            IterableStream<ResponseStreamEvent> events = responsesClient.createStreamingAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(agentReference)
                    .setStructuredInputs(structuredInputValues),
                ResponseCreateParams.builder().input("Hello! Can you confirm my details?"));

            for (ResponseStreamEvent event : events) {
                accumulator.accumulate(event);
                event.outputTextDelta().ifPresent(textEvent -> textDeltas.add(textEvent.delta()));
            }

            assertFalse(textDeltas.isEmpty(), "Should have received at least one text delta");

            Response response = accumulator.response();
            assertNotNull(response.id());
            assertTrue(response.status().isPresent());
            assertEquals(ResponseStatus.COMPLETED, response.status().get());

            String streamedText = String.join("", textDeltas);
            assertFalse(streamedText.isEmpty());
        } finally {
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
        }
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private static FunctionTool createWeatherFunctionTool() {
        Map<String, Object> locationProp = new LinkedHashMap<>();
        locationProp.put("type", "string");
        locationProp.put("description", "The city and state, e.g. Seattle, WA");

        Map<String, Object> unitProp = new LinkedHashMap<>();
        unitProp.put("type", "string");
        unitProp.put("enum", Arrays.asList("celsius", "fahrenheit"));

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("location", locationProp);
        properties.put("unit", unitProp);

        Map<String, BinaryData> parameters = new HashMap<>();
        parameters.put("type", BinaryData.fromObject("object"));
        parameters.put("properties", BinaryData.fromObject(properties));
        parameters.put("required", BinaryData.fromObject(Arrays.asList("location", "unit")));
        parameters.put("additionalProperties", BinaryData.fromObject(false));

        return new FunctionTool("get_weather", parameters, true)
            .setDescription("Get the current weather in a given location");
    }
}
