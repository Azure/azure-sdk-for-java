// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
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

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AzureFunctionsSyncTests extends AssistantsClientTestBase {

    private AssistantsClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void parallelFunctionCallTest(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient, serviceVersion);

        createFunctionToolCallRunner((assistantCreationOptions, assistantThreadCreationOptions) -> {
            // Create the assistant
            Assistant assistant = client.createAssistant(assistantCreationOptions);
            // Create the assistant thread
            AssistantThread assistantThread = client.createThread(assistantThreadCreationOptions);

            // Send first user message
            client.createMessage(
                assistantThread.getId(),
                new ThreadMessageOptions(
                    MessageRole.USER,
                    "Assuming both my usually preferred vacation spot and favourite airline carrier, how much would it cost "
                        + "to fly there in September?"
            ));

            // Create run thread
            ThreadRun run = client.createRun(assistantThread, assistant);

            // Poll the run
            do {
                sleepIfRunningAgainstService(1000);
                run = client.getRun(assistantThread.getId(), run.getId());
            } while (run.getStatus() == RunStatus.QUEUED || run.getStatus() == RunStatus.IN_PROGRESS);

            assertEquals(RunStatus.REQUIRES_ACTION, run.getStatus());
            SubmitToolOutputsAction outputsAction = (SubmitToolOutputsAction) run.getRequiredAction();
            assertNotNull(outputsAction.getSubmitToolOutputs());
            assertFalse(outputsAction.getSubmitToolOutputs().getToolCalls().isEmpty());

            for (RequiredToolCall outputAction : outputsAction.getSubmitToolOutputs().getToolCalls()) {
                assertInstanceOf(RequiredFunctionToolCall.class, outputAction);
            }

            PageableList<RunStep> runSteps = client.listRunSteps(assistantThread.getId(), run.getId());
            assertFalse(runSteps.getData().isEmpty());

            RunStepToolCallDetails toolCallDetails = (RunStepToolCallDetails) runSteps.getData().get(0).getStepDetails();
            assertFalse(toolCallDetails.getToolCalls().isEmpty());

            // cleanup
            client.deleteThread(assistantThread.getId());
            client.deleteAssistant(assistant.getId());
        });
    }

}
