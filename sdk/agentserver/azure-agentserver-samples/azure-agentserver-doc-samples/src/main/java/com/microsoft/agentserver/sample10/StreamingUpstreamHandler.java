package com.microsoft.agentserver.sample10;

import com.microsoft.agentserver.api.AgentServerCreateResponse;
import com.microsoft.agentserver.api.ResponseContext;
import com.microsoft.agentserver.api.ResponseEventStream;
import com.microsoft.agentserver.api.ResponseHandler;
import com.openai.client.OpenAIClient;
import com.openai.models.responses.ResponseCreateParams;

import java.util.concurrent.Executors;

/**
 * A handler that forwards requests to an upstream OpenAI-compatible endpoint
 * and streams the response back token by token. Demonstrates the proxy/passthrough pattern.
 * Equivalent to the C# StreamingUpstreamHandler.
 */
public class StreamingUpstreamHandler implements ResponseHandler {

    public static final java.util.concurrent.ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private final OpenAIClient upstream;

    public StreamingUpstreamHandler(OpenAIClient upstream) {
        this.upstream = upstream;
    }

    @Override
    public ResponseEventStream createAsync(ResponseContext responseContext, AgentServerCreateResponse request) {
        ResponseEventStream stream = ResponseEventStream.create(responseContext, request)
            .emitCreated()
            .emitInProgress();

        EXECUTOR_SERVICE.execute(() -> {
            try {
                // Build the upstream request using the OpenAI Java SDK.
                ResponseCreateParams params = ResponseCreateParams.builder()
                    .model(request.responseCreateParams().model().get())
                    .input(request.responseCreateParams().input()
                        .orElse(ResponseCreateParams.Input.ofText("")))
                    .instructions(request.responseCreateParams().instructions().orElse(null))
                    .build();

                // Forward the upstream stream — handles output message lifecycle,
                // text deltas, and failure detection automatically.
                try (var streamingResponse = upstream.responses().createStreaming(params)) {
                    stream.forwardUpstream(streamingResponse.stream());
                }

                stream.emitCompleted();
            } catch (Exception e) {
                stream.emitFailed();
            }
        });

        return stream;
    }
}
