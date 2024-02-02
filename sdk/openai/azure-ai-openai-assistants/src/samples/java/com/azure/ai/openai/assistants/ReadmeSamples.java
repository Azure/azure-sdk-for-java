// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.ai.openai.assistants.models.AssistantThreadCreationOptions;
import com.azure.ai.openai.assistants.models.CreateAndRunThreadOptions;
import com.azure.ai.openai.assistants.models.CreateRunOptions;
import com.azure.ai.openai.assistants.models.FileDetails;
import com.azure.ai.openai.assistants.models.FilePurpose;
import com.azure.ai.openai.assistants.models.MessageContent;
import com.azure.ai.openai.assistants.models.MessageImageFileContent;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.MessageTextContent;
import com.azure.ai.openai.assistants.models.MessageTextDetails;
import com.azure.ai.openai.assistants.models.OpenAIFile;
import com.azure.ai.openai.assistants.models.OpenAIPageableListOfThreadMessage;
import com.azure.ai.openai.assistants.models.RetrievalToolDefinition;
import com.azure.ai.openai.assistants.models.RunStatus;
import com.azure.ai.openai.assistants.models.ThreadInitializationMessage;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.ai.openai.assistants.models.UploadFileRequest;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public final class ReadmeSamples {
    private AssistantsClient client = new AssistantsClientBuilder().buildClient();

    public void createSyncClientKeyCredential() {
        // BEGIN: readme-sample-createSyncClientKeyCredential
        AssistantsClient client = new AssistantsClientBuilder()
                .credential(new AzureKeyCredential("{key}"))
                .endpoint("{endpoint}")
                .buildClient();
        // END: readme-sample-createSyncClientKeyCredential
    }

    public void createAsyncClientKeyCredential() {
        // BEGIN: readme-sample-createAsyncClientKeyCredential
        AssistantsAsyncClient client = new AssistantsClientBuilder()
                .credential(new AzureKeyCredential("{key}"))
                .endpoint("{endpoint}")
                .buildAsyncClient();
        // END: readme-sample-createAsyncClientKeyCredential
    }

    public void createNonAzureSyncClientWithApiKey() {
        // BEGIN: readme-sample-createNonAzureAssistantSyncClientApiKey
        AssistantsClient client = new AssistantsClientBuilder()
                .credential(new KeyCredential("{openai-secret-key}"))
                .buildClient();
        // END: readme-sample-createNonAzureAssistantSyncClientApiKey
    }

    public void createNonAzureAsyncClientWithApiKey() {
        // BEGIN: readme-sample-createNonAzureAssistantAsyncClientApiKey
        AssistantsAsyncClient client = new AssistantsClientBuilder()
                .credential(new KeyCredential("{openai-secret-key}"))
                .buildAsyncClient();
        // END: readme-sample-createNonAzureAssistantAsyncClientApiKey
    }

    public void createAssistant() {
        // BEGIN: readme-sample-createAssistant
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions("{deploymentOrModelId}")
                .setName("Math Tutor")
                .setInstructions("You are a personal math tutor. Answer questions briefly, in a sentence or less.");
        Assistant assistant = client.createAssistant(assistantCreationOptions);
        // END: readme-sample-createAssistant
    }

    @Test
    public void simpleMathAssistantOperations() throws InterruptedException {
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

        // BEGIN: readme-sample-createThread
        AssistantThread thread = client.createThread(new AssistantThreadCreationOptions());
        String threadId = thread.getId();
        // END: readme-sample-createThread

        // BEGIN: readme-sample-createMessage
        String userMessage = "I need to solve the equation `3x + 11 = 14`. Can you help me?";
        ThreadMessage threadMessage = client.createMessage(threadId, MessageRole.USER, userMessage);
        // END: readme-sample-createMessage

        // BEGIN: readme-sample-createRun
        ThreadRun run = client.createRun(threadId, new CreateRunOptions(assistantId));
        // END: readme-sample-createRun

        // BEGIN: readme-sample-createThreadAndRun
        CreateAndRunThreadOptions createAndRunThreadOptions = new CreateAndRunThreadOptions(assistantId)
                .setThread(new AssistantThreadCreationOptions()
                        .setMessages(Arrays.asList(new ThreadInitializationMessage(MessageRole.USER,
                                "I need to solve the equation `3x + 11 = 14`. Can you help me?"))));
        run = client.createThreadAndRun(createAndRunThreadOptions);
        // END: readme-sample-createThreadAndRun

        // BEGIN: readme-sample-pollRun
        do {
            run = client.getRun(run.getThreadId(), run.getId());
            Thread.sleep(1000);
        } while (run.getStatus() == RunStatus.QUEUED || run.getStatus() == RunStatus.IN_PROGRESS);
        // END: readme-sample-pollRun

        // BEGIN: readme-sample-listMessagesAfterRun
        OpenAIPageableListOfThreadMessage messages = client.listMessages(run.getThreadId());
        List<ThreadMessage> data = messages.getData();
        for (int i = 0; i < data.size(); i++) {
            ThreadMessage dataMessage = data.get(i);
            MessageRole role = dataMessage.getRole();
            for (MessageContent messageContent : dataMessage.getContent()) {
                MessageTextContent messageTextContent = (MessageTextContent) messageContent;
                System.out.println(i + ": Role = " + role + ", content = " + messageTextContent.getText().getValue());
            }
        }
        // END: readme-sample-listMessagesAfterRun
    }

    @Test
    public void simpleRetrievalOperation() throws InterruptedException {
        String apiKey = Configuration.getGlobalConfiguration().get("NON_AZURE_OPENAI_KEY");
        String deploymentOrModelId = "gpt-4-1106-preview";
        String fileName = "retrieval_sample_java_aoai_sdk.txt";
        client = new AssistantsClientBuilder()
            .credential(new KeyCredential(apiKey))
            .buildClient();

        // BEGIN: readme-sample-uploadFile
        Path filePath = Paths.get("src", "samples", "resources", fileName);
        BinaryData fileData = BinaryData.fromFile(filePath);
        FileDetails fileDetails = new FileDetails(fileData).setFilename(fileName);

        OpenAIFile openAIFile = client.uploadFile(new UploadFileRequest(fileDetails, FilePurpose.ASSISTANTS));
        // END: readme-sample-uploadFile

        // BEGIN: readme-sample-createRetrievalAssistant
        Assistant assistant = client.createAssistant(
            new AssistantCreationOptions(deploymentOrModelId)
                .setName("Java SDK Retrieval Sample")
                .setInstructions("You are a helpful assistant that can help fetch data from files you know about.")
                .setTools(Arrays.asList(new RetrievalToolDefinition()))
                .setFileIds(Arrays.asList(openAIFile.getId()))
        );
        // END: readme-sample-createRetrievalAssistant

        AssistantThread thread = client.createThread(new AssistantThreadCreationOptions());

        // Assign message to thread
        client.createMessage(
            thread.getId(),
            MessageRole.USER,
            "Can you give me the documented codes for 'banana' and 'orange'?");

        // Pass the message to the assistant and start the run
        ThreadRun run = client.createRun(thread, assistant);

        do {
            Thread.sleep(500);
            run = client.getRun(thread.getId(), run.getId());
        } while (run.getStatus() == RunStatus.IN_PROGRESS
            || run.getStatus() == RunStatus.QUEUED);

        OpenAIPageableListOfThreadMessage messages = client.listMessages(thread.getId());
        for (ThreadMessage message : messages.getData()) {
            message.getContent().forEach(content -> {
                if (content instanceof MessageTextContent) {
                    MessageTextDetails messageTextDetails = ((MessageTextContent) content).getText();
                    System.out.println(messageTextDetails.getValue());
                    messageTextDetails.getAnnotations().forEach(annotation ->
                        System.out.println("\tAnnotation start: " + annotation.getStartIndex()
                            + " ,end: " + annotation.getEndIndex() + " ,text: \"" + annotation.getText() + "\""));
                } else if (content instanceof MessageImageFileContent) {
                    System.out.print("Image file ID: ");
                    System.out.println(((MessageImageFileContent) content).getImageFile().getFileId());
                }
            });
        }
    }
}
