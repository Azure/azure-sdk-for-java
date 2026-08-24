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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ResponsesApi#signalClientDisconnected(String)}:
 * client disconnect cancels non-background responses, while
 * background responses outlive the connection.
 */
class ClientDisconnectIntegrationTest {

    private ResponsesProvider provider;

    @BeforeEach
    void setUp() {
        provider = ResponsesProvider.inMemory();
    }

    /**
     * Builds a well-formed response ID (caresp_ + 50 chars) from a label.
     */
    private static String vid(String label) {
        String body = (label.replaceAll("[^A-Za-z0-9]", "") + "A".repeat(50)).substring(0, 50);
        return "caresp_" + body;
    }

    private static AgentServerCreateResponse request(boolean background) {
        ResponseCreateParams.Body body = ResponseCreateParams.builder()
            .input("hi")
            .model("test-model")
            .background(background)
            .build()
            ._body();
        return new AgentServerCreateResponse(null, body);
    }

    private static Response seedInProgress(String id, boolean background) {
        ResponseOutputMessage message = ResponseOutputMessage.builder()
            .id("om_" + id)
            .status(ResponseOutputMessage.Status.COMPLETED)
            .addContent(ResponseOutputMessage.Content.ofOutputText(
                ResponseOutputText.builder().text("hi").annotations(List.of()).build()))
            .build();
        return Response.builder()
            .id(id)
            .createdAt(System.currentTimeMillis() / 1000.0)
            .addOutput(ResponseOutputItem.ofMessage(message))
            .model("test")
            .parallelToolCalls(false)
            .tools(List.of())
            .status(ResponseStatus.IN_PROGRESS)
            .background(background)
            .error(JsonMissing.of())
            .incompleteDetails(JsonMissing.of())
            .instructions(JsonMissing.of())
            .metadata(JsonMissing.of())
            .temperature(JsonMissing.of())
            .topP(JsonMissing.of())
            .toolChoice(ToolChoiceOptions.AUTO)
            .build();
    }

    /**
     * Handler that never returns until the stream is manually completed externally.
     */
    private static final class NoopHandler implements ResponseHandler {
        @Override
        public CreateResponse createResponse(ResponseContext ctx, AgentServerCreateResponse req) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public ResponseEventStream createAsync(ResponseContext ctx, AgentServerCreateResponse req) {
            throw new UnsupportedOperationException("not used");
        }
    }

    @Test
    @DisplayName("Unknown response ID → no-op (no exception)")
    void unknownResponseIsNoOp() {
        ResponsesApi api = ResponsesApi.builder()
            .responseHandler(new NoopHandler()).provider(provider).build();
        // Just must not throw.
        api.signalClientDisconnected(vid("notreal"));
    }

    @Test
    @DisplayName("Non-background in-flight → registers in tracker via createStreamingResponse, disconnect persists cancelled")
    void disconnectCancelsNonBackgroundStreaming() throws Exception {
        ResponsesApi api = ResponsesApi.builder()
            .responseHandler(new ResponseHandler() {
                @Override
                public ResponseEventStream createAsync(ResponseContext ctx, AgentServerCreateResponse r) {
                    // Returns a never-completing stream so the execution stays in-flight.
                    return ResponseEventStream.create(ctx, r);
                }
            })
            .provider(provider)
            .build();

        ResponseEventStream stream = api.createStreamingResponse(request(false));
        String id = stream.getResponse().id();

        // Pre-seed an in-progress snapshot (the handler would normally emit this).
        provider.saveResponseAsync(id, seedInProgress(id, false), null, null).join();

        api.signalClientDisconnected(id);

        Response stored = provider.getResponseAsync(id).join().orElseThrow();
        assertEquals(Optional.of(ResponseStatus.CANCELLED), stored.status());
        assertTrue(stored.output().isEmpty(), "output must be cleared on disconnect cancel");
    }

    @Test
    @DisplayName("Background response → disconnect is a no-op")
    void disconnectIsNoOpForBackground() throws Exception {
        // Background create runs on a daemon executor; use a latch so we control
        // exactly when the handler returns (deterministic, no Thread.sleep).
        CountDownLatch handlerGate = new CountDownLatch(1);
        ResponsesApi api = ResponsesApi.builder()
            .responseHandler(new ResponseHandler() {
                @Override
                public CreateResponse createResponse(ResponseContext ctx, AgentServerCreateResponse r) {
                    try {
                        if (!handlerGate.await(5, TimeUnit.SECONDS)) {
                            throw new IllegalStateException("handler gate not released");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                    Response done = seedInProgress(ctx.getResponseId(), true).toBuilder()
                        .status(ResponseStatus.COMPLETED).build();
                    return new CreateResponse(null, done);
                }
            })
            .provider(provider)
            .build();

        CreateResponse created = api.createResponse(request(true));
        String id = created.response().id();
        // Pre-state: in_progress (immediate-return snapshot persisted by the API).
        assertEquals(Optional.of(ResponseStatus.IN_PROGRESS),
            provider.getResponseAsync(id).join().orElseThrow().status());

        // disconnect must NOT affect a background, in-flight response.
        api.signalClientDisconnected(id);

        Response after = provider.getResponseAsync(id).join().orElseThrow();
        assertEquals(Optional.of(ResponseStatus.IN_PROGRESS), after.status(),
            "background response must outlive the connection");
        // The initial in_progress snapshot starts with 0 output items, so the only
        // assertion that matters here is that status was NOT flipped to cancelled.

        // Cleanly release the handler so the daemon thread exits.
        handlerGate.countDown();
    }
}




