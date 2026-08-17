package com.microsoft.agentserver.sample3;

import com.microsoft.agentserver.api.AgentServerCreateResponse;
import com.microsoft.agentserver.api.ResponseContext;
import com.microsoft.agentserver.api.ResponseEventStream;
import com.microsoft.agentserver.api.ResponseHandler;

import java.util.concurrent.Executors;

/**
 * A streaming greeting handler that simulates token-by-token streaming.
 * Each token is emitted as a separate delta event — the client sees tokens in real time.
 * Equivalent to the C# StreamingGreetingHandler.
 */
public class StreamingGreetingHandler implements ResponseHandler {

    public static final java.util.concurrent.ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    @Override
    public ResponseEventStream createAsync(ResponseContext responseContext, AgentServerCreateResponse request) {
        String inputText = request.inputText();

        ResponseEventStream stream = ResponseEventStream.create(responseContext, request)
            .emitCreated()
            .emitInProgress();

        EXECUTOR_SERVICE.execute(() -> {
            try {
                stream.addOutputMessage(msg -> msg
                    .emitAdded()
                    .addTextPart(text -> {
                        text.emitAdded();

                        // Stream tokens as they arrive — each chunk becomes a delta event.
                        String[] tokens = {"Hello! ", "You ", "said: ", "\"" + inputText + "\""};
                        for (String token : tokens) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new RuntimeException(e);
                            }
                            text.emitDelta(token);
                        }

                        text.emitDone("Hello! You said: \"" + inputText + "\"");
                    })
                    .emitDone());
            } finally {
                stream.emitCompleted();
            }
        });

        return stream;
    }
}
