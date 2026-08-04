package com.microsoft.agentserver.sample2;

import com.microsoft.agentserver.api.AgentServerCreateResponse;
import com.microsoft.agentserver.api.ResponseContext;
import com.microsoft.agentserver.api.ResponseEventStream;
import com.microsoft.agentserver.api.ResponseHandler;

import java.util.concurrent.Executors;

/**
 * A streaming handler that simulates an LLM producing tokens one at a time.
 * Each token is emitted as a separate delta event — the client sees tokens in real time.
 */
public class StreamingHandler implements ResponseHandler {

    public static final java.util.concurrent.ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    @Override
    public ResponseEventStream createAsync(ResponseContext responseContext, AgentServerCreateResponse request) {
        ResponseEventStream stream = ResponseEventStream.create(responseContext, request)
            .emitCreated()
            .emitInProgress();

        EXECUTOR_SERVICE.execute(() -> {
            try {
                stream.addOutputMessage(msg -> msg
                    .emitAdded()
                    .addTextPart(text -> {
                        text.emitAdded();

                        // Simulate an LLM producing tokens one at a time.
                        // Replace this with your actual model call.
                        String[] tokens = {"Hello", ", ", "world", "!"};
                        for (String token : tokens) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new RuntimeException(e);
                            }
                            text.emitDelta(token);
                        }

                        text.emitDone("Hello, world!");
                    })
                    .emitDone());
            } finally {
                stream.emitCompleted();
            }
        });

        return stream;
    }
}

