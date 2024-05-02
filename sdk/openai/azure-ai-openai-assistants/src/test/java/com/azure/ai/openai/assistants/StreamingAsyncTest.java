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
import com.azure.ai.openai.assistants.models.ToolOutput;
import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;
import com.azure.core.util.IterableStream;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StreamingAsyncTest extends AssistantsClientTestBase {

    private AssistantsAsyncClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void runThreadSimpleTest(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client);
        createThreadAndRunRunner(createAndRunThreadOptions -> {
            StepVerifier.create(client.createThreadAndRunStream(createAndRunThreadOptions))
                    .thenConsumeWhile(streamUpdate -> true, streamUpdate -> {
                        String streamUpdateJson = BinaryData.fromObject(streamUpdate).toString();
                        assertTrue(streamUpdateJson != null && !streamUpdateJson.isEmpty() && !streamUpdateJson.isBlank());
                    })
                .verifyComplete();
        }, mathTutorAssistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void runThreadWithTools(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistantWithFunctionTool(client);

        createThreadRunWithFunctionCallRunner(createAndRunThreadOptions -> {

            final AtomicReference<RequiredAction> requiredAction = new AtomicReference<>();
            final AtomicReference<RunStep> runStep = new AtomicReference<>();
            StepVerifier.create(client.createThreadAndRunStream(createAndRunThreadOptions))
                    .thenConsumeWhile(streamUpdate -> true, streamUpdate -> {
                        String streamUpdateJson = BinaryData.fromObject(streamUpdate).toString();
                        assertTrue(streamUpdateJson != null && !streamUpdateJson.isEmpty() && !streamUpdateJson.isBlank());
                        if (streamUpdate instanceof StreamRequiredAction) {
                            requiredAction.set(((StreamRequiredAction) streamUpdate).getAction().getRequiredAction());
                        }
                        if (streamUpdate instanceof StreamRunCreation) {
                            runStep.set(((StreamRunCreation) streamUpdate).getRun());
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
                .thenConsumeWhile(streamUpdate -> true, streamUpdate -> {
                    String streamUpdateJson = BinaryData.fromObject(streamUpdate).toString();
                    assertTrue(streamUpdateJson != null && !streamUpdateJson.isEmpty() && !streamUpdateJson.isBlank());
                })
                .verifyComplete();
        }, mathTutorAssistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void runSimpleTest(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client);
        String threadId = createThread(client);

        StepVerifier.create(client.createMessage(threadId, MessageRole.USER, "What is the value of x in the equation x^2 + 2x + 1 = 0?"))
            .assertNext(threadMessage -> validateThreadMessage(threadMessage, threadId))
            .verifyComplete();

        StepVerifier.create(client.createRunStream(mathTutorAssistantId, threadId))
            .thenConsumeWhile(streamUpdate -> {
                String streamUpdateJson = BinaryData.fromObject(streamUpdate).toString();
                assertTrue(streamUpdateJson != null && !streamUpdateJson.isEmpty() && !streamUpdateJson.isBlank());
                return true;
            }).verifyComplete();
    }
//
//    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
//    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
//    public void runWithTools(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
//        client = getAssistantsClient(httpClient);
//        String assistantId = createMathTutorAssistantWithFunctionTool(client);
//        createRunRunner(createThreadOption -> {
//            String threadId = createThread(client);
//
//            client.createMessage(threadId, MessageRole.USER, "Please make a graph for my boilerplate equation");
//
//            IterableStream<StreamUpdate> streamEvents = client.createRunStream(threadId, createThreadOption);
//
//            RequiredAction requiredAction = null;
//            RunStep runStep = null;
//            for (StreamUpdate streamUpdate : streamEvents) {
//                String streamUpdateJson = BinaryData.fromObject(streamUpdate).toString();
//                assertTrue(streamUpdateJson != null && !streamUpdateJson.isEmpty() && !streamUpdateJson.isBlank());
//                if (streamUpdate instanceof StreamRequiredAction) {
//                    requiredAction = ((StreamRequiredAction) streamUpdate).getAction().getRequiredAction();
//                }
//
//                if (streamUpdate instanceof StreamRunCreation) {
//                    runStep = ((StreamRunCreation) streamUpdate).getRun();
//                }
//            }
//
//            assertNotNull(runStep);
//            assertNotNull(requiredAction);
//            assertInstanceOf(SubmitToolOutputsAction.class, requiredAction);
//
//            List<ToolOutput> toolOutputs = null;
//            for (RequiredToolCall toolCall : ((SubmitToolOutputsAction) requiredAction).getSubmitToolOutputs().getToolCalls()) {
//                assertInstanceOf(RequiredFunctionToolCall.class, toolCall);
//                assertEquals(((RequiredFunctionToolCall) toolCall).getFunction().getName(), "get_boilerplate_equation");
//                toolOutputs = Arrays.asList(new ToolOutput()
//                    .setToolCallId(toolCall.getId())
//                    .setOutput("x^2 + y^2 = z^2"));
//            }
//
//            IterableStream<StreamUpdate> result = client.submitToolOutputsToRunStream(runStep.getThreadId(), runStep.getRunId(), toolOutputs);
//            for (StreamUpdate streamUpdate : result) {
//                String streamUpdateJson = BinaryData.fromObject(streamUpdate).toString();
//                assertTrue(streamUpdateJson != null && !streamUpdateJson.isEmpty() && !streamUpdateJson.isBlank());
//            }
//        }, assistantId);
//
//    }
}
