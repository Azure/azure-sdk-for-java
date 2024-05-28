// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.ai.openai.assistants.models.AssistantStreamEvent;
import com.azure.ai.openai.assistants.models.AssistantThreadCreationOptions;
import com.azure.ai.openai.assistants.models.CodeInterpreterToolDefinition;
import com.azure.ai.openai.assistants.models.CreateAndRunThreadOptions;
import com.azure.ai.openai.assistants.models.FunctionDefinition;
import com.azure.ai.openai.assistants.models.FunctionToolDefinition;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.RequiredAction;
import com.azure.ai.openai.assistants.models.RequiredFunctionToolCall;
import com.azure.ai.openai.assistants.models.RequiredToolCall;
import com.azure.ai.openai.assistants.models.RunStepDeltaCodeInterpreterToolCall;
import com.azure.ai.openai.assistants.models.RunStepDeltaToolCallObject;
import com.azure.ai.openai.assistants.models.StreamRequiredAction;
import com.azure.ai.openai.assistants.models.StreamRunCreation;
import com.azure.ai.openai.assistants.models.StreamRunStepUpdate;
import com.azure.ai.openai.assistants.models.SubmitToolOutputsAction;
import com.azure.ai.openai.assistants.models.ThreadMessageOptions;
import com.azure.ai.openai.assistants.models.ToolOutput;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StreamingFunctionCallExample {

    public static void main(String[] args) throws InterruptedException {
        String apiKey = System.getenv("NON_AZURE_OPENAI_KEY");
        String deploymentOrModelId = "gpt-4-1106-preview";

        AssistantsAsyncClient client = new AssistantsClientBuilder()
            .credential(new KeyCredential(apiKey))
            .buildAsyncClient();

        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions(deploymentOrModelId)
            .setName("Java SDK Test Assistant: math function plotter")
            .setDescription("This assistant helps you plot math functions.")
            .setInstructions("You are a helpful math assistant that helps with visualizing equations. Use the code "
                + "interpreter tool when asked to generate images. Use provided functions to resolve appropriate unknown values")
            .setTools(Arrays.asList(
                new CodeInterpreterToolDefinition(),
                new FunctionToolDefinition(
                    new FunctionDefinition("get_boilerplate_equation", BinaryData.fromString("{\"type\":\"object\",\"properties\":{}}"))
                        .setDescription("Retrieves a predefined 'boilerplate equation' from the caller")
                ))
            );

        final AtomicReference<String> runId = new AtomicReference<>();
        final AtomicReference<String> threadId = new AtomicReference<>();
        final AtomicReference<RequiredAction> requiredAction = new AtomicReference<>();

        client.createAssistant(assistantCreationOptions)
            .flatMapMany(assistant -> {
                System.out.println("Assistant created: " + assistant.getId());
                return client.createThreadAndRunStream(new CreateAndRunThreadOptions(assistant.getId())
                    .setThread(new AssistantThreadCreationOptions()
                        .setMessages(Arrays.asList(new ThreadMessageOptions(MessageRole.USER,
                            "Please make a graph for my boilerplate equation")))));
            }).doOnNext(streamUpdate -> {
                System.out.println("Stream update class name: " + streamUpdate.getClass().getSimpleName());
                if (streamUpdate instanceof StreamRequiredAction) {
                    requiredAction.set(((StreamRequiredAction) streamUpdate).getMessage().getRequiredAction());
                }
                if (streamUpdate instanceof StreamRunCreation) {
                    runId.set(((StreamRunCreation) streamUpdate).getMessage().getRunId());
                    threadId.set(((StreamRunCreation) streamUpdate).getMessage().getThreadId());
                }
            })
            .blockLast();


        System.out.println("Submitting tool outputs");
        System.out.println("Generating python code:");
        client.submitToolOutputsToRunStream(threadId.get(), runId.get(),
            prepareToolOutputs((SubmitToolOutputsAction) requiredAction.get())
        ).doOnNext(streamUpdate -> {
            if (streamUpdate.getKind() == AssistantStreamEvent.THREAD_RUN_STEP_DELTA) {
                RunStepDeltaToolCallObject runStepDetails = (RunStepDeltaToolCallObject) ((StreamRunStepUpdate) streamUpdate).getMessage().getDelta().getStepDetails();
                handleToolCallOutput((RunStepDeltaCodeInterpreterToolCall) runStepDetails.getToolCalls().get(0));
            }
        })
        .blockLast();

    }

    private static List<ToolOutput> prepareToolOutputs(SubmitToolOutputsAction requiredAction) {
        for (RequiredToolCall toolCall : requiredAction.getSubmitToolOutputs().getToolCalls()) {
            if (toolCall instanceof RequiredFunctionToolCall) {
                assertEquals(((RequiredFunctionToolCall) toolCall).getFunction().getName(), "get_boilerplate_equation");
                return Arrays.asList(new ToolOutput()
                    .setToolCallId(toolCall.getId())
                    .setOutput("x^2 + y^2 = z^2"));
            }
        }

        throw new IllegalStateException("No tool outputs found");
    }

    private static void handleToolCallOutput(RunStepDeltaCodeInterpreterToolCall toolCall) {
        System.out.print(toolCall.getCodeInterpreter().getInput());
    }
}
