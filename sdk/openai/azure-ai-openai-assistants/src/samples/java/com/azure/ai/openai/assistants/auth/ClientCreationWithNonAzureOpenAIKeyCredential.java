// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants.auth;

import com.azure.ai.openai.assistants.AssistantThreadsClient;
import com.azure.ai.openai.assistants.AssistantsClient;
import com.azure.ai.openai.assistants.AssistantsClientBuilder;
import com.azure.ai.openai.assistants.RunStepsClient;
import com.azure.ai.openai.assistants.ThreadMessagesClient;
import com.azure.ai.openai.assistants.ThreadRunsClient;
import com.azure.ai.openai.assistants.implementation.models.OpenAIPageableListOfRunStep;
import com.azure.ai.openai.assistants.implementation.models.OpenAIPageableListOfThreadMessage;
import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.RunStatus;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;

import java.util.List;

/**
 * Sample demonstrates how to create a client with public Non-Azure API Key.
 */
public class ClientCreationWithNonAzureOpenAIKeyCredential {
    /**
     * Runs the sample algorithm and demonstrates how to create a client with public Non-Azure API Key.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String apiKey = Configuration.getGlobalConfiguration().get("NON_AZURE_OPENAI_KEY");
        AssistantsClientBuilder builder = new AssistantsClientBuilder()
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .credential(new KeyCredential(apiKey));

        String deploymentOrModelId = "gpt-4-1106-preview";

        AssistantsClient client = builder.buildAssistantsClient();
//        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions(deploymentOrModelId)
//                .setFileIds(Arrays.asList("file-cQJ0ncMcsY7B8g6mio0RE102"))
//                .setName("Shawn's Assistant")
//                .setDescription("test-description")
//                .setTools(Arrays.asList(new CodeInterpreterToolDefinition()));

//        Assistant assistant = client.createAssistant(assistantCreationOptions);
//        System.out.printf("Assistant ID=%s is created at %s.%n", assistant.getId(), assistant.getCreatedAt());

        Assistant assistant = client.retrieveAssistant("asst_zS3eRcCrpOvbLltWEs6esGv1");
        String name = assistant.getName();
        System.out.println(name);

        AssistantThreadsClient assistantThreadsClient = builder.buildAssistantThreadsClient();
        AssistantThread assistantThread = assistantThreadsClient.retrieveThread("thread_Q9nzrJBH0lRs0gDqjV13npRJ");
        System.out.println(assistantThread.getId());


        ThreadMessagesClient threadMessagesClient = builder.buildThreadMessagesClient();
        ThreadMessage message = threadMessagesClient.createMessage(assistantThread.getId(), MessageRole.USER, "Show me top 3 most profitable month.");
        System.out.println(message.getContent().get(0));
        ThreadMessage threadMessage = threadMessagesClient.retrieveMessage(assistantThread.getId(), message.getId());
        System.out.println(threadMessage.getContent().get(0));


        ThreadRunsClient threadRunsClient = builder.buildThreadRunsClient();
        ThreadRun threadRun = threadRunsClient.createRun(assistantThread.getId(), "asst_zS3eRcCrpOvbLltWEs6esGv1");

        String assistantId = threadRun.getAssistantId();
        String threadId = threadRun.getThreadId();
        String runId = threadRun.getId();
        System.out.println("assistant id = " + assistantId + ", thread id = " + threadId + ", run id = " + runId);


        ThreadRun threadRun1 = threadRunsClient.retrieveRun(threadId, runId);
        System.out.println(threadRun1.getAssistantId());
        while (threadRun1.getStatus() != RunStatus.COMPLETED) {
            System.out.println(threadRun1.getStatus());
            threadRun1 = threadRunsClient.retrieveRun(threadId, runId);
        }


        RunStepsClient runStepsClient = builder.buildRunStepsClient();
        OpenAIPageableListOfRunStep openAIPageableListOfRunStep = runStepsClient.listRunSteps(threadId, runId);
        openAIPageableListOfRunStep.getFirstId();
        openAIPageableListOfRunStep.getLastId();


        OpenAIPageableListOfThreadMessage openAIPageableListOfThreadMessage = threadMessagesClient.listMessages(threadId);

        List<ThreadMessage> data = openAIPageableListOfThreadMessage.getData();
        for (ThreadMessage threadMessage1 : data) {
            System.out.println(threadMessage1.getContent().get(0));
        }
    }
}
