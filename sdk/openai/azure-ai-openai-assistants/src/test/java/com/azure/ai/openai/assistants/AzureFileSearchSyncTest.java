// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.ai.openai.assistants.models.AssistantThreadCreationOptions;
import com.azure.ai.openai.assistants.models.CreateFileSearchToolResourceOptions;
import com.azure.ai.openai.assistants.models.CreateFileSearchToolResourceVectorStoreOptions;
import com.azure.ai.openai.assistants.models.CreateFileSearchToolResourceVectorStoreOptionsList;
import com.azure.ai.openai.assistants.models.CreateToolResourcesOptions;
import com.azure.ai.openai.assistants.models.FilePurpose;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.MessageTextContent;
import com.azure.ai.openai.assistants.models.OpenAIFile;
import com.azure.ai.openai.assistants.models.PageableList;
import com.azure.ai.openai.assistants.models.RunStatus;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.ai.openai.assistants.models.ThreadMessageOptions;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureFileSearchSyncTest extends AssistantsClientTestBase {

    AssistantsClient client;

    @Disabled("Retrieval tools are not supported in Azure")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void basicRetrieval(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient, serviceVersion);

        createRetrievalRunner((fileDetails, assistantCreationOptions) -> {
            // Upload file for assistant
            OpenAIFile openAIFile = client.uploadFile(fileDetails, FilePurpose.ASSISTANTS);

            // Create assistant
            CreateToolResourcesOptions createToolResourcesOptions = new CreateToolResourcesOptions();
            createToolResourcesOptions.setFileSearch(
                new CreateFileSearchToolResourceOptions(
                    new CreateFileSearchToolResourceVectorStoreOptionsList(
                        Arrays.asList(new CreateFileSearchToolResourceVectorStoreOptions(Arrays.asList(openAIFile.getId()))))));
            assistantCreationOptions.setToolResources(createToolResourcesOptions);
            Assistant assistant = client.createAssistant(assistantCreationOptions);

            // Create thread
            AssistantThread thread = client.createThread(new AssistantThreadCreationOptions());

            // Assign message to thread
            client.createMessage(
                thread.getId(),
                new ThreadMessageOptions(
                    MessageRole.USER,
                    "Can you give me the documented codes for 'banana' and 'orange'?"));

            // Pass the message to the assistant and start the run
            ThreadRun run = client.createRun(thread, assistant);

            do {
                sleepIfRunningAgainstService(1000);
                run = client.getRun(thread.getId(), run.getId());
            } while (run.getStatus() == RunStatus.IN_PROGRESS
                || run.getStatus() == RunStatus.QUEUED);

            assertEquals(RunStatus.COMPLETED, run.getStatus());
            assertEquals(assistant.getId(), run.getAssistantId());

            // List messages from the thread
            PageableList<ThreadMessage> messageList = client.listMessages(thread.getId());

            assertEquals(2, messageList.getData().size());
            ThreadMessage firstMessage = messageList.getData().get(0);

            assertEquals(MessageRole.ASSISTANT, firstMessage.getRole());
            assertFalse(firstMessage.getContent().isEmpty());

            MessageTextContent firstMessageContent = (MessageTextContent) firstMessage.getContent().get(0);
            assertNotNull(firstMessageContent);
            assertTrue(firstMessageContent.getText().getValue().contains("232323"));

            // cleanup
            client.deleteAssistant(assistant.getId());
            client.deleteFile(openAIFile.getId());
            client.deleteThread(thread.getId());
        });
    }


}
