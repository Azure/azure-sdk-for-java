// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.microsoft.agentserver.api.implementation.InMemoryResponseProvider;
import com.openai.core.JsonMissing;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputText;
import com.openai.models.responses.ResponseStatus;
import com.openai.models.responses.ToolChoiceOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ResponsesApi#cancelResponse(String)} covering the cancel
 * behaviour matrix from the API spec ( /
 * Rules ).
 */
class CancelResponseIntegrationTest {

    private InMemoryResponseProvider provider;
    private ResponsesApi api;

    @BeforeEach
    void setUp() {
        provider = new InMemoryResponseProvider();
        // The handler is never invoked by cancelResponse; a no-op is sufficient.
        ResponseHandler noopHandler = new ResponseHandler() {
            @Override
            public CreateResponse createResponse(ResponseContext ctx, AgentServerCreateResponse request) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public ResponseEventStream createAsync(ResponseContext ctx, AgentServerCreateResponse request) {
                throw new UnsupportedOperationException("not used");
            }
        };
        api = ResponsesApi.builder().responseHandler(noopHandler).provider(provider).build();
    }

    private void seed(String id, boolean background, ResponseStatus status) {
        ResponseOutputMessage message = ResponseOutputMessage.builder()
            .id("om_" + id)
            .status(ResponseOutputMessage.Status.COMPLETED)
            .addContent(ResponseOutputMessage.Content.ofOutputText(
                ResponseOutputText.builder().text("hi").annotations(List.of()).build()))
            .build();

        Response resp = Response.builder()
            .id(id)
            .createdAt(System.currentTimeMillis() / 1000.0)
            .addOutput(ResponseOutputItem.ofMessage(message))
            .model("test")
            .parallelToolCalls(false)
            .tools(List.of())
            .status(status)
            .background(background)
            .error(JsonMissing.of())
            .incompleteDetails(JsonMissing.of())
            .instructions(JsonMissing.of())
            .metadata(JsonMissing.of())
            .temperature(JsonMissing.of())
            .topP(JsonMissing.of())
            .toolChoice(ToolChoiceOptions.AUTO)
            .build();

        provider.saveResponseAsync(id, resp, null, null).join();
    }

    /**
     * Builds a well-formed response ID (caresp_ + 50 Base62 chars) from a label.
     */
    private static String vid(String label) {
        String body = (label.replaceAll("[^A-Za-z0-9]", "") + "A".repeat(50)).substring(0, 50);
        return "caresp_" + body;
    }

    @Test
    @DisplayName("Cancel unknown response → 404")
    void cancelNotFound() {
        ApiException ex = assertThrows(ApiException.class, () -> api.cancelResponse(vid("missing")));
        assertEquals(404, ex.getStatusCode());
    }

    @Test
    @DisplayName("Cancel non-background response → 400 'Cannot cancel a synchronous response.'")
    void cancelSynchronous() {
        String id = vid("sync");
        seed(id, false, ResponseStatus.COMPLETED);
        ApiException ex = assertThrows(ApiException.class, () -> api.cancelResponse(id));
        assertEquals(400, ex.getStatusCode());
        assertEquals("Cannot cancel a synchronous response.", ex.getError().message());
    }

    @Test
    @DisplayName("Cancel background in_progress → 200, status cancelled, 0 output items")
    void cancelInProgress() throws Exception {
        String id = vid("inprog");
        seed(id, true, ResponseStatus.IN_PROGRESS);
        Response cancelled = api.cancelResponse(id);
        assertEquals(java.util.Optional.of(ResponseStatus.CANCELLED), cancelled.status());
        assertTrue(cancelled.output().isEmpty(), "output must be cleared on cancel");

        // The stored response reflects the cancelled state.
        Response stored = provider.getResponseAsync(id).join().orElseThrow();
        assertEquals(java.util.Optional.of(ResponseStatus.CANCELLED), stored.status());
        assertTrue(stored.output().isEmpty());
    }

    @Test
    @DisplayName("Cancel background queued → 200, status cancelled")
    void cancelQueued() throws Exception {
        String id = vid("queued");
        seed(id, true, ResponseStatus.QUEUED);
        Response cancelled = api.cancelResponse(id);
        assertEquals(java.util.Optional.of(ResponseStatus.CANCELLED), cancelled.status());
        assertTrue(cancelled.output().isEmpty());
    }

    @Test
    @DisplayName("Cancel already-cancelled response → 200 idempotent, unchanged")
    void cancelIdempotent() throws Exception {
        String id = vid("cancelled");
        seed(id, true, ResponseStatus.CANCELLED);
        Response before = provider.getResponseAsync(id).join().orElseThrow();
        Response result = api.cancelResponse(id);
        assertSame(before, result, "idempotent cancel returns the stored response unchanged");
    }

    @Test
    @DisplayName("Cancel completed background response → 400 'Cannot cancel a completed response.'")
    void cancelCompleted() {
        String id = vid("done");
        seed(id, true, ResponseStatus.COMPLETED);
        ApiException ex = assertThrows(ApiException.class, () -> api.cancelResponse(id));
        assertEquals(400, ex.getStatusCode());
        assertEquals("Cannot cancel a completed response.", ex.getError().message());
    }

    @Test
    @DisplayName("Cancel failed background response → 400 'Cannot cancel a failed response.'")
    void cancelFailed() {
        String id = vid("failed");
        seed(id, true, ResponseStatus.FAILED);
        ApiException ex = assertThrows(ApiException.class, () -> api.cancelResponse(id));
        assertEquals(400, ex.getStatusCode());
        assertEquals("Cannot cancel a failed response.", ex.getError().message());
    }

    @Test
    @DisplayName("Cancel incomplete background response → 400 terminal state")
    void cancelIncomplete() {
        String id = vid("incomplete");
        seed(id, true, ResponseStatus.INCOMPLETE);
        ApiException ex = assertThrows(ApiException.class, () -> api.cancelResponse(id));
        assertEquals(400, ex.getStatusCode());
        assertEquals("Cannot cancel a response in terminal state.", ex.getError().message());
    }
}

