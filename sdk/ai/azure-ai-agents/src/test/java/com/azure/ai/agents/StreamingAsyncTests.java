// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.CodeInterpreterTool;
import com.azure.ai.agents.models.FunctionTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;
import com.openai.helpers.ResponseAccumulator;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseStatus;
import com.openai.models.responses.ResponseStreamEvent;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.agents.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StreamingAsyncTests extends ClientTestBase {

    private static final String AGENT_MODEL = "gpt-4o";

    // ========================================================================
    // Simple prompt streaming
    // ========================================================================

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void simpleStreamingProducesTextDeltas(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsAsyncClient agentsClient = getAgentsAsyncClient(httpClient, serviceVersion);
        ResponsesAsyncClient responsesClient = getResponsesAsyncClient(httpClient, serviceVersion);

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(AGENT_MODEL)
            .setInstructions("You are a helpful assistant. Reply in one sentence.");

        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();

        StepVerifier
            .create(agentsClient.createAgentVersion("streaming-async-test-agent", agentDefinition).flatMap(agent -> {
                agentRef.set(agent);

                AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());

                ResponseAccumulator accumulator = ResponseAccumulator.create();
                List<String> textDeltas = new ArrayList<>();

                Flux<ResponseStreamEvent> events = responsesClient.createStreamingWithAgent(agentReference,
                    ResponseCreateParams.builder().input("Say hello."));

                return events.doOnNext(event -> {
                    accumulator.accumulate(event);
                    event.outputTextDelta().ifPresent(textEvent -> textDeltas.add(textEvent.delta()));
                }).then(Mono.fromCallable(() -> {
                    assertFalse(textDeltas.isEmpty(), "Should have received at least one text delta");

                    Response response = accumulator.response();
                    assertNotNull(response.id());
                    assertTrue(response.status().isPresent());
                    assertEquals(ResponseStatus.COMPLETED, response.status().get());

                    String streamedText = String.join("", textDeltas);
                    assertFalse(streamedText.isEmpty());
                    return response;
                }));
            }).flatMap(response -> {
                AgentVersionDetails agent = agentRef.get();
                return agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion()).thenReturn(response);
            }))
            .assertNext(response -> assertNotNull(response.id()))
            .verifyComplete();
    }

    // ========================================================================
    // Function calling streaming
    // ========================================================================

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void functionCallStreamingProducesFunctionCallEvents(HttpClient httpClient,
        AgentsServiceVersion serviceVersion) {
        AgentsAsyncClient agentsClient = getAgentsAsyncClient(httpClient, serviceVersion);
        ResponsesAsyncClient responsesClient = getResponsesAsyncClient(httpClient, serviceVersion);

        FunctionTool tool = createWeatherFunctionTool();

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(AGENT_MODEL)
            .setInstructions("You are a helpful assistant. When asked about weather, use the get_weather function.")
            .setTools(Collections.singletonList(tool));

        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();

        StepVerifier.create(
            agentsClient.createAgentVersion("function-streaming-async-test-agent", agentDefinition).flatMap(agent -> {
                agentRef.set(agent);

                AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());

                ResponseAccumulator accumulator = ResponseAccumulator.create();
                List<String> functionArgDeltas = new ArrayList<>();

                Flux<ResponseStreamEvent> events = responsesClient.createStreamingWithAgent(agentReference,
                    ResponseCreateParams.builder().input("What's the weather like in Seattle?"));

                return events.doOnNext(event -> {
                    accumulator.accumulate(event);
                    event.functionCallArgumentsDelta().ifPresent(argEvent -> functionArgDeltas.add(argEvent.delta()));
                }).then(Mono.fromCallable(() -> {
                    assertFalse(functionArgDeltas.isEmpty(), "Should have received function call argument deltas");

                    Response response = accumulator.response();
                    assertNotNull(response.id());

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
                    return response;
                }));
            }).flatMap(response -> {
                AgentVersionDetails agent = agentRef.get();
                return agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion()).thenReturn(response);
            })).assertNext(response -> assertNotNull(response.id())).verifyComplete();
    }

    // ========================================================================
    // Code Interpreter streaming (Azure-specific tool)
    // ========================================================================

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void codeInterpreterStreamingProducesCodeEvents(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsAsyncClient agentsClient = getAgentsAsyncClient(httpClient, serviceVersion);
        ResponsesAsyncClient responsesClient = getResponsesAsyncClient(httpClient, serviceVersion);

        CodeInterpreterTool tool = new CodeInterpreterTool();

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(AGENT_MODEL)
            .setInstructions("You are a helpful assistant that uses code interpreter for calculations.")
            .setTools(Collections.singletonList(tool));

        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();

        StepVerifier
            .create(agentsClient.createAgentVersion("code-interpreter-streaming-async-test-agent", agentDefinition)
                .flatMap(agent -> {
                    agentRef.set(agent);

                    AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());

                    ResponseAccumulator accumulator = ResponseAccumulator.create();
                    List<String> codeDeltas = new ArrayList<>();
                    boolean[] codeInterpreterCompleted = { false };

                    Flux<ResponseStreamEvent> events = responsesClient.createStreamingWithAgent(agentReference,
                        ResponseCreateParams.builder().input("What is 42 * 37? Use code to calculate."));

                    return events.doOnNext(event -> {
                        accumulator.accumulate(event);
                        event.codeInterpreterCallCodeDelta().ifPresent(e -> codeDeltas.add(e.delta()));
                        event.codeInterpreterCallCompleted().ifPresent(e -> codeInterpreterCompleted[0] = true);
                    }).then(Mono.fromCallable(() -> {
                        Response response = accumulator.response();
                        assertNotNull(response.id());
                        assertTrue(response.status().isPresent());
                        assertEquals(ResponseStatus.COMPLETED, response.status().get());

                        assertFalse(codeDeltas.isEmpty(), "Should have received code interpreter code deltas");
                        assertTrue(codeInterpreterCompleted[0], "Code interpreter should have completed");
                        return response;
                    }));
                })
                .flatMap(response -> {
                    AgentVersionDetails agent = agentRef.get();
                    return agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion()).thenReturn(response);
                }))
            .assertNext(response -> assertNotNull(response.id()))
            .verifyComplete();
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
