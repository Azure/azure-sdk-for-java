// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.ai.openai.assistants.models.AssistantThreadCreationOptions;
import com.azure.ai.openai.assistants.models.CreateRunOptions;
import com.azure.ai.openai.assistants.models.MessageContent;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.MessageTextContent;
import com.azure.ai.openai.assistants.models.PageableList;
import com.azure.ai.openai.assistants.models.RunStatus;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.ai.openai.assistants.models.ThreadMessageOptions;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.Configuration;

import java.util.List;

/**
 * Sample demonstrates how to crate a Math Tutor assistant and interact with it.
 */
public class MathTutorAssistant {

    static AssistantsClient client;
    public static void main(String[] args) throws InterruptedException {
        String apiKey = Configuration.getGlobalConfiguration().get("NON_AZURE_OPENAI_KEY");
        String deploymentOrModelId = "gpt-4-1106-preview";

        client = new AssistantsClientBuilder()
                .credential(new KeyCredential(apiKey))
                .buildClient();

        // Create a new assistant
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions(deploymentOrModelId)
                .setName("Math Tutor")
                .setInstructions("You are a personal math tutor. Answer questions briefly, in a sentence or less.");
        Assistant assistant = client.createAssistant(assistantCreationOptions);
        System.out.printf("Assistant ID = \"%s\" is created at %s.%n", assistant.getId(), assistant.getCreatedAt());

        String assistantId = assistant.getId();

        // Emulating concurrent user requests
        ThreadRun threadAndRun1 = createThreadAndRun(assistantId, "I need to solve the equation `3x + 11 = 14`. Can you help me?");
        ThreadRun threadAndRun2 = createThreadAndRun(assistantId, "Could you explain linear algebra to me?");
        ThreadRun threadAndRun3 = createThreadAndRun(assistantId, "I don't like math. What can I do?");

        String threadId1 = threadAndRun1.getThreadId();
        String threadId2 = threadAndRun2.getThreadId();
        String threadId3 = threadAndRun3.getThreadId();

        // Wait for the Run to complete
        waitOnRun(threadAndRun1, threadId1);
        waitOnRun(threadAndRun2, threadId2);
        waitOnRun(threadAndRun3, threadId3);

        getResponse(threadId1);
        getResponse(threadId2);
        getResponse(threadId3);

        // Clean up
        client.deleteAssistant(assistantId);
    }

    private static void getResponse(String threadId) {
        // Now that the Run has completed, we can list the Messages in the Thread to see what got added by the Assistant.
        // Messages are ordered in reverse-chronological order – this was done so the most recent results are always on
        // the first page (since results can be paginated).
        PageableList<ThreadMessage> messages = client.listMessages(threadId);
        List<ThreadMessage> data = messages.getData();
        for (int i = 0; i < data.size(); i++) {
            ThreadMessage dataMessage = data.get(i);
            MessageRole role = dataMessage.getRole();
            for (MessageContent messageContent : dataMessage.getContent()) {
                MessageTextContent messageTextContent = (MessageTextContent) messageContent;
                System.out.println(i + ": Role = " + role + ", content = " + messageTextContent.getText().getValue());
            }
        }
    }

    private static ThreadRun createThreadAndRun(String assistantId, String userMessage) {
        AssistantThread thread = client.createThread(new AssistantThreadCreationOptions());
        return submitMessage(assistantId, thread.getId(), userMessage);
    }

    private static ThreadRun waitOnRun(ThreadRun run, String threadId) throws InterruptedException {
        // Poll the Run in a loop
        while (run.getStatus() == RunStatus.QUEUED || run.getStatus() == RunStatus.IN_PROGRESS) {
            String runId = run.getId();
            run = client.getRun(threadId, runId);
            System.out.println("Run ID: " + runId + ", Run Status: " + run.getStatus());
            Thread.sleep(1000);
        }
        return run;
    }

    private static ThreadRun submitMessage(String assistantId, String threadId, String userMessage) {
        // Then add the Message to the thread
        ThreadMessage threadMessage = client.createMessage(threadId, new ThreadMessageOptions(MessageRole.USER, userMessage));
        System.out.printf("Thread Message ID = \"%s\" is created at %s.%n", threadMessage.getId(), threadMessage.getCreatedAt());
        System.out.println("Message Content: " + ((MessageTextContent) threadMessage.getContent().get(0)).getText().getValue());

        // Then create a Run. You must specify both the Assistant and the Thread.
        // Unlike creating a completion in the Chat Completions API, creating a Run is an asynchronous operation.
        // It will return immediately with the Run's metadata, which includes a status that will initially be set to
        // queued. The status will be updated as the Assistant performs operations (like using tools and adding messages).
        return client.createRun(threadId, new CreateRunOptions(assistantId));
    }
}
