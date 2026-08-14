// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.server.spring;

import com.microsoft.agentserver.api.AgentServerCreateResponse;
import com.microsoft.agentserver.api.AgentServerResponseItemList;
import com.microsoft.agentserver.api.AgentServerException;
import com.microsoft.agentserver.api.CreateResponse;
import com.microsoft.agentserver.api.ResponseEventStream;
import com.microsoft.agentserver.api.ResponseStreamReplay;
import com.microsoft.agentserver.api.ResponsesApi;
import com.openai.models.responses.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that a streaming request whose {@code Accept} header does not include
 * {@code text/event-stream} (e.g. {@code application/json}, as the Azure AI Foundry
 * orchestrator sometimes sends) still negotiates to SSE instead of failing with
 * {@code 406 Not Acceptable}.
 * <p>
 * Uses a real embedded servlet container (not MockMvc) because the fix depends on
 * {@link StreamRoutingFilter} performing a servlet {@code forward} to the SSE
 * sub-resource, which MockMvc does not execute.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringStreamingAcceptHeaderTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void streamingWithNonSseAcceptHeaderNegotiatesToSse() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        String body = "{\"input\": \"stream please\", \"model\": \"test-model\", \"stream\": true}";
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:" + port + "/responses",
            org.springframework.http.HttpMethod.POST,
            new HttpEntity<>(body, headers),
            String.class);

        assertEquals(200, response.getStatusCode().value(),
            "Streaming request must not 406 on a non-SSE Accept header. Body:\n" + response.getBody());

        MediaType contentType = response.getHeaders().getContentType();
        assertTrue(contentType != null && contentType.toString().contains("text/event-stream"),
            "Content-Type should be text/event-stream, was: " + contentType);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({ ResponsesController.class, StreamRoutingFilter.class, SseHeaderFilter.class })
    static class TestApp {

        @Bean
        ResponsesApi responsesApi() {
            return new StubResponsesApi();
        }
    }

    /**
     * Minimal {@link ResponsesApi} whose streaming path emits an empty (immediately completed)
     * SSE stream — enough to drive content negotiation without any model backend.
     */
    private static final class StubResponsesApi implements ResponsesApi {

        @Override
        public CreateResponse createResponse(AgentServerCreateResponse createResponse) throws AgentServerException {
            throw new UnsupportedOperationException();
        }

        @Override
        public ResponseEventStream createStreamingResponse(AgentServerCreateResponse createResponse)
            throws AgentServerException {
            ResponseEventStream stream = org.mockito.Mockito.mock(ResponseEventStream.class);
            org.mockito.Mockito.when(stream.getResponse()).thenReturn(null);
            // subscribe(onEvent, onFailure, onComplete) → emit an empty, immediately-completed stream.
            org.mockito.Mockito.doAnswer(invocation -> {
                Runnable onComplete = invocation.getArgument(2);
                onComplete.run();
                return null;
            }).when(stream).subscribe(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
            return stream;
        }

        @Override
        public Response getResponse(String responseId, List<String> include) throws AgentServerException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Response cancelResponse(String responseId) throws AgentServerException {
            throw new UnsupportedOperationException();
        }

        @Override
        public ResponseStreamReplay replayResponseStream(String responseId, Integer startingAfter)
            throws AgentServerException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteResponse(String responseId) throws AgentServerException {
            throw new UnsupportedOperationException();
        }

        @Override
        public AgentServerResponseItemList listInputItems(String responseId, Integer limit, String order,
            String after, String before, List<String> include) throws AgentServerException {
            throw new UnsupportedOperationException();
        }
    }
}
