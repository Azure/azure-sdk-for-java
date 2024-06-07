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
import com.azure.ai.openai.assistants.models.StreamUpdate;
import com.azure.ai.openai.assistants.models.SubmitToolOutputsAction;
import com.azure.ai.openai.assistants.models.ThreadMessageOptions;
import com.azure.ai.openai.assistants.models.ToolOutput;
import com.azure.core.http.HttpClient;
import com.azure.core.util.IterableStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AzureStreamingSyncTest extends AssistantsClientTestBase {

    private AssistantsClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void runThreadSimpleTest(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient, serviceVersion);
        String mathTutorAssistantId = createMathTutorAssistant(client);
        createThreadAndRunRunner(createAndRunThreadOptions -> {

            IterableStream<StreamUpdate> streamEvents = client.createThreadAndRunStream(createAndRunThreadOptions);

            streamEvents.forEach(AssistantsClientTestBase::assertStreamUpdate);
        }, mathTutorAssistantId);
    }
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void runThreadWithTools(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient, serviceVersion);
        String mathTutorAssistantId = createMathTutorAssistantWithFunctionTool(client);

        createThreadRunWithFunctionCallRunner(createAndRunThreadOptions -> {

            IterableStream<StreamUpdate> streamEvents = client.createThreadAndRunStream(createAndRunThreadOptions);

            RequiredAction requiredAction = null;
            RunStep runStep = null;
            for (StreamUpdate streamUpdate : streamEvents) {
                assertStreamUpdate(streamUpdate);
                if (streamUpdate instanceof StreamRequiredAction) {
                    requiredAction = ((StreamRequiredAction) streamUpdate).getMessage().getRequiredAction();
                }

                if (streamUpdate instanceof StreamRunCreation) {
                    runStep = ((StreamRunCreation) streamUpdate).getMessage();
                }
            }

            assertNotNull(runStep);
            assertNotNull(requiredAction);
            assertInstanceOf(SubmitToolOutputsAction.class, requiredAction);

            List<ToolOutput> toolOutputs = null;
            for (RequiredToolCall toolCall : ((SubmitToolOutputsAction) requiredAction).getSubmitToolOutputs().getToolCalls()) {
                assertInstanceOf(RequiredFunctionToolCall.class, toolCall);
                assertEquals(((RequiredFunctionToolCall) toolCall).getFunction().getName(), "get_boilerplate_equation");
                toolOutputs = Arrays.asList(new ToolOutput()
                    .setToolCallId(toolCall.getId())
                    .setOutput("x^2 + y^2 = z^2"));
            }

            IterableStream<StreamUpdate> result = client.submitToolOutputsToRunStream(runStep.getThreadId(), runStep.getRunId(), toolOutputs);
            for (StreamUpdate streamUpdate : result) {
                assertStreamUpdate(streamUpdate);
            }
        }, mathTutorAssistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void runSimpleTest(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient, serviceVersion);
        String mathTutorAssistantId = createMathTutorAssistant(client);
        String threadId = createThread(client);

        client.createMessage(threadId, new ThreadMessageOptions(MessageRole.USER, "What is the value of x in the equation x^2 + 2x + 1 = 0?"));

        IterableStream<StreamUpdate> run = client.createRunStream(threadId, mathTutorAssistantId);
        for (StreamUpdate streamUpdate : run) {
            assertStreamUpdate(streamUpdate);
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void runWithTools(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient, serviceVersion);
        String assistantId = createMathTutorAssistantWithFunctionTool(client);
        createRunRunner(createThreadOption -> {
            String threadId = createThread(client);

            client.createMessage(threadId, new ThreadMessageOptions(MessageRole.USER, "Please make a graph for my boilerplate equation"));

            IterableStream<StreamUpdate> streamEvents = client.createRunStream(threadId, createThreadOption);

            RequiredAction requiredAction = null;
            RunStep runStep = null;
            for (StreamUpdate streamUpdate : streamEvents) {
                assertStreamUpdate(streamUpdate);
                if (streamUpdate instanceof StreamRequiredAction) {
                    requiredAction = ((StreamRequiredAction) streamUpdate).getMessage().getRequiredAction();
                }

                if (streamUpdate instanceof StreamRunCreation) {
                    runStep = ((StreamRunCreation) streamUpdate).getMessage();
                }
            }

            assertNotNull(runStep);
            assertNotNull(requiredAction);
            assertInstanceOf(SubmitToolOutputsAction.class, requiredAction);

            List<ToolOutput> toolOutputs = null;
            for (RequiredToolCall toolCall : ((SubmitToolOutputsAction) requiredAction).getSubmitToolOutputs().getToolCalls()) {
                assertInstanceOf(RequiredFunctionToolCall.class, toolCall);
                assertEquals(((RequiredFunctionToolCall) toolCall).getFunction().getName(), "get_boilerplate_equation");
                toolOutputs = Arrays.asList(new ToolOutput()
                    .setToolCallId(toolCall.getId())
                    .setOutput("x^2 + y^2 = z^2"));
            }

            IterableStream<StreamUpdate> result = client.submitToolOutputsToRunStream(runStep.getThreadId(), runStep.getRunId(), toolOutputs);
            for (StreamUpdate streamUpdate : result) {
                assertStreamUpdate(streamUpdate);
            }
        }, assistantId);

    }
}
