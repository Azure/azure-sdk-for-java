// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.IterableStream;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseStreamEvent;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ToolStreamingMockTests {
    private static final String ENDPOINT = "https://localhost:8080/api/projects/project";
    private static final String AGENT_NAME = "streaming-tool-agent";

    @Test
    public void syncFileSearchStreamDeserializesLifecycleEvents() {
        DeterministicHttpClient httpClient
            = new DeterministicHttpClient().enqueueSse(fileSearchSse("resp-stream-sync"));
        ResponsesClient client = createBuilder(httpClient).buildResponsesClient();

        IterableStream<ResponseStreamEvent> stream = client.createStreamingAzureResponse(agentOptions(),
            ResponseCreateParams.builder().input("Search the product documentation."));
        List<ResponseStreamEvent> events = new ArrayList<>();
        stream.forEach(events::add);

        assertFileSearchEvents(events, "resp-stream-sync");
        assertStreamingRequest(httpClient.getRequest(0));
        httpClient.assertResponsesConsumed();
    }

    @Test
    public void bingGroundingStreamDeserializesUrlCitation() {
        DeterministicHttpClient httpClient = new DeterministicHttpClient().enqueueSse(bingCitationSse());
        ResponsesClient client = createBuilder(httpClient).buildResponsesClient();

        List<ResponseStreamEvent> events = new ArrayList<>();
        client
            .createStreamingAzureResponse(agentOptions(),
                ResponseCreateParams.builder().input("What is the current weather?"))
            .forEach(events::add);

        assertEquals(2, events.size());
        com.openai.models.responses.ResponseOutputText.Annotation.UrlCitation citation = events.get(0)
            .outputItemDone()
            .get()
            .item()
            .asMessage()
            .content()
            .get(0)
            .asOutputText()
            .annotations()
            .get(0)
            .asUrlCitation();
        assertEquals("https://example.test/weather", citation.url());
        assertEquals("Weather source", citation.title());
        assertEquals("resp-bing-stream", events.get(1).completed().get().response().id());
        httpClient.assertResponsesConsumed();
    }

    @Test
    public void asyncFileSearchStreamDeserializesLifecycleEvents() {
        DeterministicHttpClient httpClient
            = new DeterministicHttpClient().enqueueSse(fileSearchSse("resp-stream-async"));
        ResponsesAsyncClient client = createBuilder(httpClient).buildResponsesAsyncClient();

        StepVerifier
            .create(
                client
                    .createStreamingAzureResponse(agentOptions(),
                        ResponseCreateParams.builder().input("Search the product documentation."))
                    .collectList())
            .assertNext(events -> assertFileSearchEvents(events, "resp-stream-async"))
            .verifyComplete();
        assertStreamingRequest(httpClient.getRequest(0));
        httpClient.assertResponsesConsumed();
    }

    private static AgentsClientBuilder createBuilder(DeterministicHttpClient httpClient) {
        return new AgentsClientBuilder().endpoint(ENDPOINT)
            .credential(new MockTokenCredential())
            .httpClient(httpClient)
            .serviceVersion(AgentsServiceVersion.V1);
    }

    private static AzureCreateResponseOptions agentOptions() {
        return new AzureCreateResponseOptions().setAgentReference(new AgentReference(AGENT_NAME).setVersion("1"));
    }

    private static void assertFileSearchEvents(List<ResponseStreamEvent> events, String responseId) {
        assertEquals(5, events.size());
        assertEquals("file-search-item", events.get(0).fileSearchCallInProgress().get().itemId());
        assertEquals("file-search-item", events.get(1).fileSearchCallSearching().get().itemId());
        assertEquals("file-search-item", events.get(2).fileSearchCallCompleted().get().itemId());
        assertEquals("Product documentation result.",
            events.get(3).outputItemDone().get().item().asMessage().content().get(0).asOutputText().text());
        assertEquals(responseId, events.get(4).completed().get().response().id());
    }

    private static void assertStreamingRequest(HttpRequest request) {
        String requestBody = request.getBodyAsBinaryData().toString();
        assertTrue(request.getUrl().getPath().endsWith("/openai/v1/responses"));
        assertTrue(requestBody.contains("\"stream\":true"));
        assertTrue(requestBody.contains("\"agent_reference\""));
        assertTrue(requestBody.contains("Search the product documentation."));
    }

    private static String bingCitationSse() {
        String citation = "{\"type\":\"url_citation\",\"url\":\"https://example.test/weather\","
            + "\"title\":\"Weather source\",\"start_index\":0,\"end_index\":14}";
        String message = OpenAIResponseFixtures.message("message-bing", "Weather report.", citation);
        String response = OpenAIResponseFixtures.response("resp-bing-stream", message);
        return event("{\"type\":\"response.output_item.done\",\"output_index\":0," + "\"sequence_number\":0,\"item\":"
            + message + "}")
            + event("{\"type\":\"response.completed\",\"sequence_number\":1,\"response\":" + response + "}")
            + "data: [DONE]\n\n";
    }

    private static String fileSearchSse(String responseId) {
        String message = OpenAIResponseFixtures.message("message-item", "Product documentation result.");
        String response = OpenAIResponseFixtures.response(responseId, message);
        return event("{\"type\":\"response.file_search_call.in_progress\","
            + "\"item_id\":\"file-search-item\",\"output_index\":0,\"sequence_number\":0}")
            + event("{\"type\":\"response.file_search_call.searching\","
                + "\"item_id\":\"file-search-item\",\"output_index\":0,\"sequence_number\":1}")
            + event("{\"type\":\"response.file_search_call.completed\","
                + "\"item_id\":\"file-search-item\",\"output_index\":0,\"sequence_number\":2}")
            + event("{\"type\":\"response.output_item.done\",\"output_index\":1," + "\"sequence_number\":3,\"item\":"
                + message + "}")
            + event("{\"type\":\"response.completed\",\"sequence_number\":4,\"response\":" + response + "}")
            + "data: [DONE]\n\n";
    }

    private static String event(String json) {
        return "data: " + json + "\n\n";
    }
}
