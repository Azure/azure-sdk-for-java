package com.microsoft.agentserver.sample6;

import com.microsoft.agentserver.api.AgentServerCreateResponse;
import com.microsoft.agentserver.api.ResponseContext;
import com.microsoft.agentserver.api.ResponseEventStream;
import com.microsoft.agentserver.api.ResponseHandler;
import com.openai.models.responses.ResponseReasoningItem;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * A math solver handler demonstrating multi-output: reasoning + text message.
 * Uses the convenience API to emit a reasoning item followed by a text answer.
 * Equivalent to the C# MathSolverHandler (convenience version).
 */
public class MathSolverHandler implements ResponseHandler {

    public static final java.util.concurrent.ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    @Override
    public ResponseEventStream createAsync(ResponseContext responseContext, AgentServerCreateResponse request) {
        String question = request.inputText();

        ResponseEventStream stream = ResponseEventStream.create(responseContext, request)
            .emitCreated()
            .emitInProgress();

        EXECUTOR_SERVICE.execute(() -> {
            try {
                // Output item 0: Reasoning — show the thought process.
                String thought = "The user asked: \"" + question + "\". " +
                    "I need to identify the mathematical operation, " +
                    "compute the result, and explain the steps.";

                ResponseReasoningItem reasoningItem = ResponseReasoningItem.builder()
                    .id("reasoning_0")
                    .summary(List.of(ResponseReasoningItem.Summary.builder()
                        .text(thought)
                        .build()))
                    .status(ResponseReasoningItem.Status.COMPLETED)
                    .build();

                stream.addOutputReasoningItem(reasoning -> reasoning
                    .emitAdded(reasoningItem)
                    .emitDone(reasoningItem));

                // Output item 1: Message — the final answer.
                String answer = "The answer is 42. Here's how: " +
                    "6 × 7 = 42. The multiplication of 6 and 7 gives 42.";

                stream.addOutputMessage(msg -> msg.outputItemMessage(answer));

                stream.emitCompleted();
            } catch (Exception e) {
                stream.emitFailed();
            }
        });

        return stream;
    }

}

