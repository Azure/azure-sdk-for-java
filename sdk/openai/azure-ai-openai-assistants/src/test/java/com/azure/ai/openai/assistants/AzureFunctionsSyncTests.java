package com.azure.ai.openai.assistants;


import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.OpenAIPageableListOfRunStep;
import com.azure.ai.openai.assistants.models.RequiredFunctionToolCall;
import com.azure.ai.openai.assistants.models.RunStatus;
import com.azure.ai.openai.assistants.models.RunStepToolCallDetails;
import com.azure.ai.openai.assistants.models.SubmitToolOutputsAction;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AzureFunctionsSyncTests extends AssistantsClientTestBase {

    private AssistantsClient client;

    @Disabled("Support for FUNCTION tool calls is unclear in Azure at the moment")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void parallelFunctionCallTest(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient, serviceVersion);

        createFunctionToolCallRunner((assistantCreationOptions, assistantThreadCreationOptions) -> {
            // Create the assistant
            Assistant assistant = client.createAssistant(assistantCreationOptions);
            // Create the assistant thread
            AssistantThread assistantThread = client.createThread(assistantThreadCreationOptions);

            // Send first user message
            client.createMessage(
                assistantThread.getId(),
                MessageRole.USER,
                "Assuming both my usually preferred vacation spot and favourite airline carrier, how much would it cost "
                    + "to fly there in September?"
            );

            // Create run thread
            ThreadRun run = client.createRun(assistantThread, assistant);

            // Poll the run
            do {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                run = client.getRun(assistantThread.getId(), run.getId());
            } while (run.getStatus() == RunStatus.QUEUED || run.getStatus() == RunStatus.IN_PROGRESS);

            assertEquals(RunStatus.REQUIRES_ACTION, run.getStatus());
            SubmitToolOutputsAction outputsAction = (SubmitToolOutputsAction) run.getRequiredAction();
            assertNotNull(outputsAction.getSubmitToolOutputs());
            assertFalse(outputsAction.getSubmitToolOutputs().getToolCalls().isEmpty());

            for(var outputAction : outputsAction.getSubmitToolOutputs().getToolCalls()) {
                assertInstanceOf(RequiredFunctionToolCall.class, outputAction);
            }

            OpenAIPageableListOfRunStep runSteps = client.listRunSteps(assistantThread.getId(), run.getId());
            assertFalse(runSteps.getData().isEmpty());

            RunStepToolCallDetails toolCallDetails = (RunStepToolCallDetails) runSteps.getData().get(0).getStepDetails();
            assertFalse(toolCallDetails.getToolCalls().isEmpty());

            // cleanup
            client.deleteThread(assistantThread.getId());
            client.deleteAssistant(assistant.getId());
        });
    }

}
