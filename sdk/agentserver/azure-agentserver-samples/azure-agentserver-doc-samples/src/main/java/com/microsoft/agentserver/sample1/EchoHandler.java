package com.microsoft.agentserver.sample1;

import com.microsoft.agentserver.api.AgentServerCreateResponse;
import com.microsoft.agentserver.api.CreateResponse;
import com.microsoft.agentserver.api.ResponseBuilder;
import com.microsoft.agentserver.api.ResponseContext;
import com.microsoft.agentserver.api.ResponseEventStream;
import com.microsoft.agentserver.api.ResponseHandler;
import com.openai.models.responses.ResponseOutputText;

import java.util.ArrayList;
import java.util.concurrent.Executors;

/**
 * A simple echo handler that reads the input text from the request
 * and streams back "Echo: {input}" as a single text output message.
 */
public class EchoHandler implements ResponseHandler {

    public static final java.util.concurrent.ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    @Override
    public CreateResponse createResponse(
        ResponseContext responseContext,
        AgentServerCreateResponse request) {

        String inputText = request.inputText();
        if (!inputText.isEmpty()) {
            ResponseOutputText responseOutputText = ResponseOutputText.builder()
                .text("Echo: " + inputText)
                .annotations(new ArrayList<>())
                .build();

            return new CreateResponse(
                request.agent(),
                ResponseBuilder.convertOutputToResponse(request, responseOutputText)
            );
        }

        throw new IllegalArgumentException("No text input provided in the request");
    }

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
                    .addTextPart(text -> text
                        .emitAdded()
                        .emitDelta("Echo: " + inputText)
                        .emitDone("Echo: " + inputText))
                    .emitDone());
            } finally {
                stream.emitCompleted();
            }
        });

        return stream;
    }
}
