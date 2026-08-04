// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.openai.core.JsonMissing;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputText;
import com.openai.models.responses.ResponseStatus;
import com.openai.models.responses.ToolChoiceOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@code background=true} response creation (Responses API behaviour
 * contract matrix C3 / Rules ) and its interaction with cancel.
 *
 * <p>
 * Synchronisation is deterministic: the test handler blocks on a {@link CountDownLatch}
 * gate so the test controls exactly when background processing finishes.
 */
class BackgroundResponseIntegrationTest {

    private ResponsesProvider provider;

    @BeforeEach
    void setUp() {
        provider = ResponsesProvider.inMemory();
    }

    private static AgentServerCreateResponse backgroundRequest(boolean store) {
        ResponseCreateParams.Body body = ResponseCreateParams.builder()
            .input("hello")
            .model("test-model")
            .background(true)
            .store(store)
            .build()
            ._body();
        return new AgentServerCreateResponse(null, body);
    }

    /**
     * Handler whose {@code createResponse} blocks on {@code gate} until released.
     */
    private static final class GatedHandler implements ResponseHandler {
        final CountDownLatch gate = new CountDownLatch(1);
        final CountDownLatch handlerReturned = new CountDownLatch(1);

        @Override
        public CreateResponse createResponse(ResponseContext ctx, AgentServerCreateResponse request) {
            try {
                if (!gate.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("gate not released in time");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            Response resp = completed(ctx.getResponseId());
            handlerReturned.countDown();
            return new CreateResponse(null, resp);
        }

        @Override
        public ResponseEventStream createAsync(ResponseContext ctx, AgentServerCreateResponse request) {
            throw new UnsupportedOperationException("not used");
        }
    }

    private static Response completed(String responseId) {
        ResponseOutputMessage message = ResponseOutputMessage.builder()
            .id("om_" + responseId)
            .status(ResponseOutputMessage.Status.COMPLETED)
            .addContent(ResponseOutputMessage.Content.ofOutputText(
                ResponseOutputText.builder().text("done").annotations(List.of()).build()))
            .build();
        return Response.builder()
            .id(responseId)
            .createdAt(System.currentTimeMillis() / 1000.0)
            .addOutput(ResponseOutputItem.ofMessage(message))
            .model("test-model")
            .parallelToolCalls(false)
            .tools(List.of())
            .status(ResponseStatus.COMPLETED)
            .background(true)
            .error(JsonMissing.of())
            .incompleteDetails(JsonMissing.of())
            .instructions(JsonMissing.of())
            .metadata(JsonMissing.of())
            .temperature(JsonMissing.of())
            .topP(JsonMissing.of())
            .toolChoice(ToolChoiceOptions.AUTO)
            .build();
    }

    private ResponseStatus pollUntil(ResponsesApi api, String id, ResponseStatus expected) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            ResponseStatus status = api.getResponse(id, List.of()).status().orElse(null);
            if (expected.equals(status)) {
                return status;
            }
            TimeUnit.MILLISECONDS.sleep(20);
        }
        fail("Response " + id + " did not reach " + expected + " within timeout");
        return null; // unreachable
    }

    @Test
    @DisplayName("background=true + store=false → 400")
    void backgroundRequiresStore() {
        ResponsesApi api = ResponsesApi.builder()
            .responseHandler(new GatedHandler())
            .provider(provider)
            .build();

        ApiException ex = assertThrows(ApiException.class, () -> api.createResponse(backgroundRequest(false)));
        assertEquals(400, ex.getStatusCode());
    }

    @Test
    @DisplayName("background=true returns in_progress immediately; GET reflects in_progress then completed")
    void backgroundReturnsInProgressThenCompletes() throws Exception {
        GatedHandler handler = new GatedHandler();
        ResponsesApi api = ResponsesApi.builder().responseHandler(handler).provider(provider).build();

        CreateResponse created = api.createResponse(backgroundRequest(true));
        String id = created.response().id();

        // immediate return with in_progress while the handler is still blocked.
        assertEquals(Optional.of(ResponseStatus.IN_PROGRESS), created.response().status());
        assertEquals(Optional.of(ResponseStatus.IN_PROGRESS),
            api.getResponse(id, List.of()).status());

        // Release the handler and verify the response transitions to completed.
        handler.gate.countDown();
        pollUntil(api, id, ResponseStatus.COMPLETED);
        assertTrue(api.getResponse(id, List.of()).status().isPresent());
    }

    @Test
    @DisplayName("Cancel during background processing wins; later completion does not overwrite")
    void cancelDuringBackgroundWins() throws Exception {
        GatedHandler handler = new GatedHandler();
        ResponsesApi api = ResponsesApi.builder().responseHandler(handler).provider(provider).build();

        CreateResponse created = api.createResponse(backgroundRequest(true));
        String id = created.response().id();
        assertEquals(Optional.of(ResponseStatus.IN_PROGRESS),
            api.getResponse(id, List.of()).status());

        // Cancel while in-flight → cancelled, output cleared.
        Response cancelled = api.cancelResponse(id);
        assertEquals(Optional.of(ResponseStatus.CANCELLED), cancelled.status());
        assertTrue(cancelled.output().isEmpty());

        // Release the handler; the background finaliser must NOT overwrite the cancel.
        handler.gate.countDown();
        assertTrue(handler.handlerReturned.await(10, TimeUnit.SECONDS),
            "handler should have returned");

        // Cancelled state is authoritative and stable.
        assertEquals(Optional.of(ResponseStatus.CANCELLED),
            api.getResponse(id, List.of()).status());
        assertTrue(api.getResponse(id, List.of()).output().isEmpty());
    }
}

