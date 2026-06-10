package com.microsoft.agentserver.sample3;

import com.microsoft.agentserver.api.AgentServerCreateResponse;
import com.microsoft.agentserver.api.ResponseContext;
import com.microsoft.agentserver.api.ResponseEventStream;
import com.microsoft.agentserver.api.ResponseHandler;

import java.util.concurrent.Executors;

/**
 * A greeting handler that emits a complete text message using the convenience API.
 * Equivalent to the C# GreetingHandler using OutputItemMessage().
 */
public class GreetingHandler implements ResponseHandler {

    public static final java.util.concurrent.ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    @Override
    public ResponseEventStream createAsync(ResponseContext responseContext, AgentServerCreateResponse request) {
        String inputText = request.inputText();

        ResponseEventStream stream = ResponseEventStream.create(responseContext, request)
            .emitCreated()
            .emitInProgress();

        EXECUTOR_SERVICE.execute(() -> {
            try {
                // Emit a complete text message in one call using the convenience method.
                stream.addOutputMessage(msg -> msg
                    .outputItemMessage("Hello! You said: \"" + inputText + "\""));
            } finally {
                stream.emitCompleted();
            }
        });

        return stream;
    }
}
