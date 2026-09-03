// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentEndpointConfig;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.FixedRatioVersionSelectionRule;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.PromptAgentDefinitionTextOptions;
import com.azure.ai.agents.models.ProtocolConfiguration;
import com.azure.ai.agents.models.ResponseFormatJsonSchemaInner;
import com.azure.ai.agents.models.ResponsesProtocolConfiguration;
import com.azure.ai.agents.models.TextResponseFormatJsonSchema;
import com.azure.ai.agents.models.UpdateAgentDetailsOptions;
import com.azure.ai.agents.models.VersionSelector;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.BinaryData;
import com.openai.client.OpenAIClient;
import com.openai.client.OpenAIClientAsync;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AgentProtocolMockTests {
    private static final String ENDPOINT = "https://localhost:8080/api/projects/project";
    private static final String AGENT_NAME = "protocol-agent-test";
    private static final String CALENDAR_JSON
        = "{\"name\":\"Science fair\",\"date\":\"2025-11-07\",\"participants\":[\"Alice\",\"Bob\"]}";

    @Test
    public void syncStructuredOutputIsSentAndParsed() {
        DeterministicHttpClient httpClient
            = new DeterministicHttpClient().enqueueJson(200, promptVersionJson("1", true))
                .enqueueJson(200, responseJson("resp-structured-sync", CALENDAR_JSON));
        AgentsClientBuilder builder = createBuilder(httpClient);
        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        agentsClient.createAgentVersion(AGENT_NAME, new CreateAgentVersionInput(createStructuredOutputDefinition()));
        Response response = responsesClient.createAzureResponse(
            new AzureCreateResponseOptions().setAgentReference(new AgentReference(AGENT_NAME).setVersion("1")),
            ResponseCreateParams.builder().conversation("conv-structured-sync"));

        assertEquals(CALENDAR_JSON, responseText(response));
        String createAgentBody = body(httpClient.getRequest(0));
        assertTrue(createAgentBody.contains("\"name\":\"CalendarEvent\""));
        assertTrue(createAgentBody.contains("\"additionalProperties\":false"));
        String responseBody = body(httpClient.getRequest(1));
        assertTrue(responseBody.contains("\"conversation\":\"conv-structured-sync\""));
        assertTrue(responseBody.contains("\"agent_reference\""));
        assertTrue(responseBody.contains("\"name\":\"" + AGENT_NAME + "\""));
        httpClient.assertResponsesConsumed();
    }

    @Test
    public void asyncStructuredOutputIsSentAndParsed() {
        DeterministicHttpClient httpClient
            = new DeterministicHttpClient().enqueueJson(200, promptVersionJson("1", true))
                .enqueueJson(200, responseJson("resp-structured-async", CALENDAR_JSON));
        AgentsClientBuilder builder = createBuilder(httpClient);
        AgentsAsyncClient agentsClient = builder.buildAgentsAsyncClient();
        ResponsesAsyncClient responsesClient = builder.buildResponsesAsyncClient();

        Mono<Response> operation = agentsClient
            .createAgentVersion(AGENT_NAME, new CreateAgentVersionInput(createStructuredOutputDefinition()))
            .then(Mono.defer(() -> responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(new AgentReference(AGENT_NAME).setVersion("1")),
                ResponseCreateParams.builder().conversation("conv-structured-async"))));

        StepVerifier.create(operation)
            .assertNext(response -> assertEquals(CALENDAR_JSON, responseText(response)))
            .verifyComplete();
        assertTrue(body(httpClient.getRequest(0)).contains("\"name\":\"CalendarEvent\""));
        assertTrue(body(httpClient.getRequest(1)).contains("\"conversation\":\"conv-structured-async\""));
        httpClient.assertResponsesConsumed();
    }

    @Test
    public void syncAgentEndpointRoutesResponsesProtocol() {
        DeterministicHttpClient httpClient
            = new DeterministicHttpClient().enqueueJson(200, promptVersionJson("2", false))
                .enqueueJson(200, agentJson("2"))
                .enqueueJson(200, responseJson("resp-endpoint-sync", "routed response"));
        AgentsClientBuilder builder = createBuilder(httpClient).allowPreview(true);
        AgentsClient agentsClient = builder.buildAgentsClient();

        agentsClient.createAgentVersion(AGENT_NAME, new CreateAgentVersionInput(new PromptAgentDefinition("gpt-4o")));
        agentsClient.updateAgentDetails(AGENT_NAME, endpointUpdate("2"));
        OpenAIClient endpointClient = builder.buildAgentScopedOpenAIClient(AGENT_NAME);
        Response response
            = endpointClient.responses().create(ResponseCreateParams.builder().input("Route this request.").build());

        assertEquals("routed response", responseText(response));
        HttpRequest patchRequest = httpClient.getRequest(1);
        assertEquals(HttpMethod.PATCH, patchRequest.getHttpMethod());
        assertTrue(patchRequest.getUrl().getPath().endsWith("/agents/" + AGENT_NAME));
        String patchBody = body(patchRequest);
        assertTrue(patchBody.contains("\"agent_version\":\"2\""));
        assertTrue(patchBody.contains("\"traffic_percentage\":100"));
        assertTrue(patchBody.contains("\"responses\":{}"));
        assertEndpointResponseRequest(httpClient.getRequest(2));
        httpClient.assertResponsesConsumed();
    }

    @Test
    public void asyncAgentEndpointRoutesResponsesProtocol() {
        DeterministicHttpClient httpClient
            = new DeterministicHttpClient().enqueueJson(200, promptVersionJson("2", false))
                .enqueueJson(200, agentJson("2"))
                .enqueueJson(200, responseJson("resp-endpoint-async", "async routed response"));
        AgentsClientBuilder builder = createBuilder(httpClient).allowPreview(true);
        AgentsAsyncClient agentsClient = builder.buildAgentsAsyncClient();
        OpenAIClientAsync endpointClient = builder.buildAgentScopedOpenAIAsyncClient(AGENT_NAME);

        Mono<Response> operation = agentsClient
            .createAgentVersion(AGENT_NAME, new CreateAgentVersionInput(new PromptAgentDefinition("gpt-4o")))
            .then(agentsClient.updateAgentDetails(AGENT_NAME, endpointUpdate("2")))
            .then(Mono.defer(() -> Mono.fromFuture(
                endpointClient.responses().create(ResponseCreateParams.builder().input("Route async.").build()))));

        StepVerifier.create(operation)
            .assertNext(response -> assertEquals("async routed response", responseText(response)))
            .verifyComplete();
        assertTrue(body(httpClient.getRequest(1)).contains("\"agent_version\":\"2\""));
        assertEndpointResponseRequest(httpClient.getRequest(2));
        httpClient.assertResponsesConsumed();
    }

    private static AgentsClientBuilder createBuilder(DeterministicHttpClient httpClient) {
        return new AgentsClientBuilder().endpoint(ENDPOINT)
            .credential(new MockTokenCredential())
            .httpClient(httpClient)
            .serviceVersion(AgentsServiceVersion.V1);
    }

    private static PromptAgentDefinition createStructuredOutputDefinition() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("name", Collections.singletonMap("type", "string"));
        properties.put("date", Collections.singletonMap("type", "string"));
        Map<String, Object> participants = new LinkedHashMap<>();
        participants.put("type", "array");
        participants.put("items", Collections.singletonMap("type", "string"));
        properties.put("participants", participants);
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", Arrays.asList("name", "date", "participants"));
        schema.put("additionalProperties", false);
        ResponseFormatJsonSchemaInner schemaModel
            = BinaryData.fromObject(schema).toObject(ResponseFormatJsonSchemaInner.class);
        return new PromptAgentDefinition("gpt-4o").setInstructions("Extract a calendar event.")
            .setText(new PromptAgentDefinitionTextOptions()
                .setFormat(new TextResponseFormatJsonSchema("CalendarEvent", schemaModel).setStrict(true)));
    }

    private static UpdateAgentDetailsOptions endpointUpdate(String version) {
        AgentEndpointConfig endpoint = new AgentEndpointConfig()
            .setVersionSelector(new VersionSelector().setVersionSelectionRules(
                Collections.singletonList(new FixedRatioVersionSelectionRule(100).setAgentVersion(version))))
            .setProtocolConfiguration(new ProtocolConfiguration().setResponses(new ResponsesProtocolConfiguration()));
        return new UpdateAgentDetailsOptions().setAgentEndpoint(endpoint);
    }

    private static void assertEndpointResponseRequest(HttpRequest request) {
        assertEquals(HttpMethod.POST, request.getHttpMethod());
        assertTrue(
            request.getUrl().getPath().endsWith("/agents/" + AGENT_NAME + "/endpoint/protocols/openai/responses"));
        assertTrue(body(request).contains("Route"));
    }

    private static String responseText(Response response) {
        return response.output().get(0).asMessage().content().get(0).asOutputText().text();
    }

    private static String body(HttpRequest request) {
        return request.getBodyAsBinaryData().toString();
    }

    private static String promptVersionJson(String version, boolean structuredOutput) {
        String text = structuredOutput
            ? ",\"text\":{\"format\":{\"type\":\"json_schema\",\"name\":\"CalendarEvent\","
                + "\"schema\":{\"type\":\"object\"},\"strict\":true}}"
            : "";
        return "{\"object\":\"agent.version\",\"id\":\"agent-" + version + "\",\"name\":\"" + AGENT_NAME
            + "\",\"version\":\"" + version + "\",\"created_at\":1,\"metadata\":{},"
            + "\"definition\":{\"kind\":\"prompt\",\"model\":\"gpt-4o\"" + text + "}}";
    }

    private static String agentJson(String latestVersion) {
        return "{\"object\":\"agent\",\"id\":\"agent-id\",\"name\":\"" + AGENT_NAME
            + "\",\"state\":\"enabled\",\"versions\":{\"latest\":" + promptVersionJson(latestVersion, false) + "}}";
    }

    static String responseJson(String id, String text) {
        return "{\"id\":\"" + id + "\",\"object\":\"response\",\"created_at\":1,"
            + "\"model\":\"gpt-4o\",\"status\":\"completed\",\"parallel_tool_calls\":true,"
            + "\"tool_choice\":\"auto\",\"tools\":[],\"output\":[{\"id\":\"msg-1\","
            + "\"type\":\"message\",\"role\":\"assistant\",\"status\":\"completed\","
            + "\"content\":[{\"type\":\"output_text\",\"text\":" + BinaryData.fromObject(text)
            + ",\"annotations\":[]}]}]}";
    }
}
