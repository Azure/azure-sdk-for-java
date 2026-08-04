package com.microsoft.agentserver.sample3;

import com.microsoft.agentserver.api.AgentServerCreateResponse;
import com.microsoft.agentserver.api.ResponseContext;
import com.microsoft.agentserver.api.ResponseEventStream;
import com.microsoft.agentserver.api.ResponseHandler;

import java.util.concurrent.Executors;

/**
 * A greeting handler with full control over each individual SSE event.
 * Demonstrates the builder API for fine-grained control over the event lifecycle.
 * Equivalent to the C# GreetingHandlerFullControl.
 */
public class GreetingHandlerFullControl implements ResponseHandler {

    public static final java.util.concurrent.ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    @Override
    public ResponseEventStream createAsync(ResponseContext responseContext, AgentServerCreateResponse request) {
        String inputText = request.inputText();

        ResponseEventStream stream = ResponseEventStream.create(responseContext, request)
            .emitCreated()       // response.created
            .emitInProgress();   // response.in_progress

        EXECUTOR_SERVICE.execute(() -> {
            try {
                String reply = "Hello! You said: \"" + inputText + "\"";

                // Add a message output item with full control over each event.
                stream.addOutputMessage(msg -> {
                    msg.emitAdded();              // response.output_item.added

                    msg.addTextPart(text -> {
                        text.emitAdded();         // response.content_part.added

                        // Emit the text body — delta first, then the final "done" with full text.
                        text.emitDelta(reply);    // response.output_text.delta
                        text.emitDone(reply);     // response.output_text.done
                        // response.content_part.done
                    });

                    msg.emitDone();               // response.output_item.done
                });
            } finally {
                stream.emitCompleted();           // response.completed
            }
        });

        return stream;
    }
}
