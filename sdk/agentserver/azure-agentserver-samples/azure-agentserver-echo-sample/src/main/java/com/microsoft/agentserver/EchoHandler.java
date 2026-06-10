package com.microsoft.agentserver;

import com.microsoft.agentserver.api.AgentServerCreateResponse;
import com.microsoft.agentserver.api.CreateResponse;
import com.microsoft.agentserver.api.ResponseBuilder;
import com.microsoft.agentserver.api.ResponseContext;
import com.microsoft.agentserver.api.ResponseEventStream;
import com.microsoft.agentserver.api.ResponseHandler;
import com.openai.models.responses.ResponseOutputText;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class EchoHandler implements ResponseHandler {
    @Override
    public CreateResponse createResponse(
        ResponseContext responseContext,
        AgentServerCreateResponse request) {

        String text = request.inputText();
        if (!text.isEmpty()) {
            ResponseOutputText responseOutputText = ResponseOutputText.builder()
                .text(text)
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
        // Simulate asynchronous response generation with multiple updates to the same message over time.
        ResponseEventStream stream = ResponseEventStream.create(responseContext, request)
            .emitCreated()
            .emitInProgress();
        Executors
            .newSingleThreadExecutor()
            .execute(() -> {
                try {
                    stream
                        .addOutputMessage(msg -> msg
                            .emitAdded()
                            .addTextPart(text -> {
                                text = text
                                    .emitAdded()
                                    .emitDelta("Hello from");

                                text.emitDelta(" the echo ");

                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }

                                text.emitDelta("handler!")
                                    .emitDone("Hello from the echo handler!");
                            })
                            .addTextPart(text -> {
                                text = text
                                    .emitAdded()
                                    .emitDelta("Hello from");

                                text.emitDelta(" the second echo ");

                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }

                                text.emitDelta("handler!")
                                    .emitDone("Hello from the second echo handler!");
                            })
                            .emitDone());
                } finally {
                    stream.emitCompleted();
                }
            });

        return stream;
    }
}
