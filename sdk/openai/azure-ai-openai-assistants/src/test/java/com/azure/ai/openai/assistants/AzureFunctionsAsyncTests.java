// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.implementation.AsyncUtils;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.PageableList;
import com.azure.ai.openai.assistants.models.RequiredFunctionToolCall;
import com.azure.ai.openai.assistants.models.RequiredToolCall;
import com.azure.ai.openai.assistants.models.RunStatus;
import com.azure.ai.openai.assistants.models.RunStep;
import com.azure.ai.openai.assistants.models.RunStepToolCallDetails;
import com.azure.ai.openai.assistants.models.SubmitToolOutputsAction;
import com.azure.ai.openai.assistants.models.ThreadMessageOptions;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AzureFunctionsAsyncTests extends AssistantsClientTestBase {

    private AssistantsAsyncClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void parallelFunctionCallTest(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);

        createFunctionToolCallRunner((assistantCreationOptions, assistantThreadCreationOptions) -> {
            StepVerifier.create(
                // Create the assistant
                client.createAssistant(assistantCreationOptions)
                    .flatMap(assistant -> {
                        AsyncUtils cleanUp = new AsyncUtils();
                        cleanUp.setAssistant(assistant);

                        // Create the assistant thread
                        return client.createThread(assistantThreadCreationOptions).zipWith(Mono.just(cleanUp));
                    }).flatMap(tuple -> {
                        AssistantThread assistantThread = tuple.getT1();
                        AsyncUtils cleanUp = tuple.getT2();
                        cleanUp.setThread(assistantThread);

                        // Send first user message
                        return client.createMessage(
                            assistantThread.getId(),
                            new ThreadMessageOptions(
                                MessageRole.USER,
                                "Assuming both my usually preferred vacation spot and favourite airline carrier, how much would it cost "
                                    + "to fly there in September?"
                        )).then(Mono.just(cleanUp));
                    }).flatMap(cleanUp ->
                        // Create run thread
                        client.createRun(cleanUp.getThread(), cleanUp.getAssistant())
                            .zipWith(Mono.just(cleanUp))
                    ).flatMap(tuple -> {
                        ThreadRun createdRun = tuple.getT1();
                        AsyncUtils cleanUp = tuple.getT2();

                        // Poll the run
                        return client.getRun(cleanUp.getThread().getId(), createdRun.getId()).zipWith(Mono.just(cleanUp))
                            .repeatWhen(complete -> complete.delayElements(java.time.Duration.ofMillis(1000)))
                            .takeUntil(tuple2 -> {
                                ThreadRun run = tuple2.getT1();

                                return run.getStatus() != RunStatus.IN_PROGRESS
                                    && run.getStatus() != RunStatus.QUEUED;
                            })
                            .last();
                    }).flatMap(tuple -> {
                        // Assert run requested actions
                        ThreadRun run = tuple.getT1();
                        AsyncUtils cleanUp = tuple.getT2();

                        assertEquals(RunStatus.REQUIRES_ACTION, run.getStatus());
                        SubmitToolOutputsAction outputsAction = (SubmitToolOutputsAction) run.getRequiredAction();
                        assertNotNull(outputsAction.getSubmitToolOutputs());
                        assertFalse(outputsAction.getSubmitToolOutputs().getToolCalls().isEmpty());

                        for (RequiredToolCall outputAction : outputsAction.getSubmitToolOutputs().getToolCalls()) {
                            assertInstanceOf(RequiredFunctionToolCall.class, outputAction);
                        }

                        return client.listRunSteps(cleanUp.getThread().getId(), run.getId()).zipWith(Mono.just(cleanUp));
                    }).map(tuple -> {
                        // Detailed step list assertions
                        PageableList<RunStep> runSteps = tuple.getT1();
                        AsyncUtils cleanUp = tuple.getT2();

                        assertFalse(runSteps.getData().isEmpty());

                        RunStepToolCallDetails toolCallDetails = (RunStepToolCallDetails) runSteps.getData().get(0).getStepDetails();
                        assertFalse(toolCallDetails.getToolCalls().isEmpty());

                        return cleanUp;
                    }).flatMap(cleanUp -> client.deleteAssistant(cleanUp.getAssistant().getId())
                        .flatMap(cleanup -> client.deleteThread(cleanUp.getThread().getId())))
                    .then() // We don't care about the response from the deletion operation
            ).verifyComplete();
        });
    }

}
