// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputText;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@code x-agent-response-id} request-header override.
 */
class ResponseIdOverrideIntegrationTest {

    /**
     * Builds a well-formed response ID (caresp_ + 50 chars) from a label.
     */
    private static String vid(String label) {
        String body = (label.replaceAll("[^A-Za-z0-9]", "") + "A".repeat(50)).substring(0, 50);
        return "caresp_" + body;
    }

    private ResponsesApi api;

    @BeforeEach
    void setUp() {
        api = ResponsesApi.builder()
            .responseHandler(new ResponseHandler() {
                @Override
                public CreateResponse createResponse(ResponseContext ctx, AgentServerCreateResponse request) {
                    ResponseOutputText text = ResponseOutputText.builder()
                        .text("ok").annotations(List.of()).build();
                    Response resp = ResponseBuilder.convertOutputToResponse(request, text);
                    return new CreateResponse(null, resp);
                }
            })
            .provider(ResponsesProvider.inMemory())
            .build();
    }

    private static AgentServerCreateResponse request() {
        ResponseCreateParams.Body body = ResponseCreateParams.builder()
            .input("hi").model("test-model").build()._body();
        return new AgentServerCreateResponse(null, body);
    }

    private static RequestMetadata withResponseIdHeader(String value) {
        return new RequestMetadata(
            IsolationContext.EMPTY,
            Collections.emptyMap(),
            Collections.emptyMap(),
            null,
            value);
    }

    @Test
    @DisplayName("Non-empty x-agent-response-id header is used as the response ID")
    void overrideHeaderWins() throws ApiException {
        String custom = vid("custom");
        CreateResponse created = api.createResponse(request(), withResponseIdHeader(custom));
        assertEquals(custom, created.response().id(),
            "the returned response.id must match the x-agent-response-id header");

        // And the persisted state is keyed by that override too.
        com.openai.models.responses.Response persisted = api.getResponse(custom, List.of());
        assertEquals(custom, persisted.id());
    }

    @Test
    @DisplayName("Empty x-agent-response-id header falls back to generation ( negative)")
    void emptyOverrideFallsBackToGeneration() throws ApiException {
        CreateResponse a = api.createResponse(request(), withResponseIdHeader(""));
        CreateResponse b = api.createResponse(request(), withResponseIdHeader(null));
        assertTrue(a.response().id().startsWith("caresp_"));
        assertTrue(b.response().id().startsWith("caresp_"));
        assertNotEquals(a.response().id(), b.response().id());
    }

    @Test
    @DisplayName("Header override beats body metadata.response_id ( precedence)")
    void headerBeatsBodyMetadata() throws ApiException {
        String headerId = vid("hdr");
        String bodyId = vid("body");
        ResponseCreateParams.Body body = ResponseCreateParams.builder()
            .input("hi").model("test-model")
            .metadata(ResponseCreateParams.Metadata.builder()
                .putAdditionalProperty("response_id", com.openai.core.JsonValue.from(bodyId))
                .build())
            .build()._body();
        AgentServerCreateResponse req = new AgentServerCreateResponse(null, body);

        CreateResponse created = api.createResponse(req, withResponseIdHeader(headerId));
        assertEquals(headerId, created.response().id(),
            "header override must beat body metadata.response_id");
        // Persisted state confirms it.
        assertEquals(headerId, api.getResponse(headerId, List.of()).id());
    }
}


