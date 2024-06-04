// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.RequiredAction;
import com.azure.ai.openai.assistants.models.RequiredFunctionToolCall;
import com.azure.ai.openai.assistants.models.RequiredToolCall;
import com.azure.ai.openai.assistants.models.RunStep;
import com.azure.ai.openai.assistants.models.StreamRequiredAction;
import com.azure.ai.openai.assistants.models.StreamRunCreation;
import com.azure.ai.openai.assistants.models.SubmitToolOutputsAction;
import com.azure.ai.openai.assistants.models.ThreadMessageOptions;
import com.azure.ai.openai.assistants.models.ToolOutput;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AzureStreamingAsyncTest extends AssistantsClientTestBase {

    private AssistantsAsyncClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void runThreadSimpleTest(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        String mathTutorAssistantId = createMathTutorAssistant(client);
        createThreadAndRunRunner(createAndRunThreadOptions -> {
            StepVerifier.create(client.createThreadAndRunStream(createAndRunThreadOptions))
                    .thenConsumeWhile(streamUpdate -> true, AssistantsClientTestBase::assertStreamUpdate)
                .verifyComplete();
        }, mathTutorAssistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void runThreadWithTools(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        String mathTutorAssistantId = createMathTutorAssistantWithFunctionTool(client);

        createThreadRunWithFunctionCallRunner(createAndRunThreadOptions -> {

            final AtomicReference<RequiredAction> requiredAction = new AtomicReference<>();
            final AtomicReference<RunStep> runStep = new AtomicReference<>();
            StepVerifier.create(client.createThreadAndRunStream(createAndRunThreadOptions))
                    .thenConsumeWhile(streamUpdate -> true, streamUpdate -> {
                        assertStreamUpdate(streamUpdate);
                        if (streamUpdate instanceof StreamRequiredAction) {
                            requiredAction.set(((StreamRequiredAction) streamUpdate).getMessage().getRequiredAction());
                        }
                        if (streamUpdate instanceof StreamRunCreation) {
                            runStep.set(((StreamRunCreation) streamUpdate).getMessage());
                        }
                    })
                .verifyComplete();

            assertNotNull(runStep.get());
            assertNotNull(requiredAction.get());
            assertInstanceOf(SubmitToolOutputsAction.class, requiredAction.get());

            List<ToolOutput> toolOutputs = null;
            for (RequiredToolCall toolCall : ((SubmitToolOutputsAction) requiredAction.get()).getSubmitToolOutputs().getToolCalls()) {
                assertInstanceOf(RequiredFunctionToolCall.class, toolCall);
                assertEquals(((RequiredFunctionToolCall) toolCall).getFunction().getName(), "get_boilerplate_equation");
                toolOutputs = Arrays.asList(new ToolOutput()
                    .setToolCallId(toolCall.getId())
                    .setOutput("x^2 + y^2 = z^2"));
            }

            StepVerifier.create(client.submitToolOutputsToRunStream(runStep.get().getThreadId(), runStep.get().getRunId(), toolOutputs))
                .thenConsumeWhile(streamUpdate -> true, AssistantsClientTestBase::assertStreamUpdate)
                .verifyComplete();
        }, mathTutorAssistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void runSimpleTest(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        String mathTutorAssistantId = createMathTutorAssistant(client);
        String threadId = createThread(client);

        StepVerifier.create(client.createMessage(threadId, new ThreadMessageOptions(MessageRole.USER, "What is the value of x in the equation x^2 + 2x + 1 = 0?")))
            .assertNext(threadMessage -> validateThreadMessage(threadMessage, threadId))
            .verifyComplete();

        StepVerifier.create(client.createRunStream(threadId, mathTutorAssistantId))
            .thenConsumeWhile(streamUpdate -> true, AssistantsClientTestBase::assertStreamUpdate).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void runWithTools(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        String assistantId = createMathTutorAssistantWithFunctionTool(client);
        createRunRunner(createThreadOption -> {
            String threadId = createThread(client);

            StepVerifier.create(client.createMessage(threadId, new ThreadMessageOptions(MessageRole.USER, "Please make a graph for my boilerplate equation")))
                .assertNext(threadMessage -> validateThreadMessage(threadMessage, threadId))
                .verifyComplete();

            final AtomicReference<RequiredAction> requiredAction = new AtomicReference<>();
            final AtomicReference<RunStep> runStep = new AtomicReference<>();
            StepVerifier.create(client.createRunStream(threadId, createThreadOption))
                .thenConsumeWhile(streamUpdate -> {
                    assertStreamUpdate(streamUpdate);
                    if (streamUpdate instanceof StreamRequiredAction) {
                        requiredAction.set(((StreamRequiredAction) streamUpdate).getMessage().getRequiredAction());
                    }

                    if (streamUpdate instanceof StreamRunCreation) {
                        runStep.set(((StreamRunCreation) streamUpdate).getMessage());
                    }
                    return true;
                }).verifyComplete();


            assertNotNull(runStep.get());
            assertNotNull(requiredAction.get());
            assertInstanceOf(SubmitToolOutputsAction.class, requiredAction.get());

            List<ToolOutput> toolOutputs = null;
            for (RequiredToolCall toolCall : ((SubmitToolOutputsAction) requiredAction.get()).getSubmitToolOutputs().getToolCalls()) {
                assertInstanceOf(RequiredFunctionToolCall.class, toolCall);
                assertEquals(((RequiredFunctionToolCall) toolCall).getFunction().getName(), "get_boilerplate_equation");
                toolOutputs = Arrays.asList(new ToolOutput()
                    .setToolCallId(toolCall.getId())
                    .setOutput("x^2 + y^2 = z^2"));
            }

            StepVerifier.create(client.submitToolOutputsToRunStream(runStep.get().getThreadId(), runStep.get().getRunId(), toolOutputs))
                .thenConsumeWhile(streamUpdate -> true, AssistantsClientTestBase::assertStreamUpdate).verifyComplete();
        }, assistantId);

    }
}
