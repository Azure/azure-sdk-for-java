// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.ai.openai.assistants.models.AssistantThreadCreationOptions;
import com.azure.ai.openai.assistants.models.CreateRunOptions;
import com.azure.ai.openai.assistants.models.FileDetails;
import com.azure.ai.openai.assistants.models.FileSearchToolDefinition;
import com.azure.ai.openai.assistants.models.MessageAttachment;
import com.azure.ai.openai.assistants.models.MessageContent;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.MessageTextContent;
import com.azure.ai.openai.assistants.models.OpenAIFile;
import com.azure.ai.openai.assistants.models.PageableList;
import com.azure.ai.openai.assistants.models.RunStatus;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.ai.openai.assistants.models.ThreadMessageOptions;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.ai.openai.assistants.models.UpdateAssistantOptions;
import com.azure.ai.openai.assistants.models.UpdateFileSearchToolResourceOptions;
import com.azure.ai.openai.assistants.models.UpdateToolResourcesOptions;
import com.azure.ai.openai.assistants.models.VectorStore;
import com.azure.ai.openai.assistants.models.VectorStoreOptions;
import com.azure.ai.openai.assistants.models.VectorStoreStatus;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;

import java.util.Arrays;
import java.util.List;

import static com.azure.ai.openai.assistants.AssistantsClientTestBase.openResourceFile;
import static com.azure.ai.openai.assistants.models.FilePurpose.ASSISTANTS;

/**
 * Sample demonstrates how to create an Assistant with a Vector Store and interact with it.
 * It creates an assistant that can help answer questions about companies’ financial statements.
 */
public class VectorStoreSample {
    static String apiKey = Configuration.getGlobalConfiguration().get("NON_AZURE_OPENAI_KEY");

    static AssistantsClient client = new AssistantsClientBuilder()
            .credential(new KeyCredential(apiKey))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();

    /**
     * Runs the sample algorithm and demonstrates how to create an Assistant with a Vector Store and interact with it.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) throws InterruptedException {
        String deploymentOrModelId = "gpt-4o";

        // Step 1: Create a new Assistant with File Search Enabled
        Assistant financialAnalystAssistant = client.createAssistant(new AssistantCreationOptions(deploymentOrModelId)
                .setName("Financial Analyst Assistant")
                .setInstructions("You are an expert financial analyst. Use you knowledge base to answer questions about audited financial statements.")
                .setTools(Arrays.asList(new FileSearchToolDefinition()))
        );

        // Step 2: Upload files and add them to a Vector Store
        String fileName = "20210203_alphabet_10K.pdf";
        FileDetails fileDetails = new FileDetails(BinaryData.fromFile(openResourceFile(fileName)), fileName);
        OpenAIFile googleFile = client.uploadFile(fileDetails, ASSISTANTS);

        fileName = "20231231_brk_10k.pdf";
        fileDetails = new FileDetails(BinaryData.fromFile(openResourceFile(fileName)), fileName);
        OpenAIFile berkshireFile = client.uploadFile(fileDetails, ASSISTANTS);

        // Create a vector store called "Financial Statements"
        VectorStoreOptions vectorStoreOptions = new VectorStoreOptions().setName("Financial Statements")
                .setFileIds(Arrays.asList(googleFile.getId(), berkshireFile.getId()));
        VectorStore vectorStore = client.createVectorStore(vectorStoreOptions);

        while (VectorStoreStatus.IN_PROGRESS.equals(vectorStore.getStatus())) {
            vectorStore = client.getVectorStore(vectorStore.getId());
        }

        // Step 3: Update the assistant to use the new Vector Store
        Assistant assistantWithVectorStore = client.updateAssistant(financialAnalystAssistant.getId(),
                new UpdateAssistantOptions()
                        .setToolResources(
                                new UpdateToolResourcesOptions()
                                        .setFileSearch(
                                                new UpdateFileSearchToolResourceOptions()
                                                        .setVectorStoreIds(
                                                                Arrays.asList(vectorStore.getId()))
                                        )
                        )
        );

        // Step 4: Create a thread
        // Upload the user provided file to OpenAI
        fileName = "20220924_aapl_10k.pdf";
        fileDetails = new FileDetails(BinaryData.fromFile(openResourceFile(fileName)), fileName);
        OpenAIFile appleFile = client.uploadFile(fileDetails, ASSISTANTS);

        // Create a thread and attach the file to the message
        AssistantThread thread = client.createThread(
                new AssistantThreadCreationOptions()
                        .setMessages(Arrays.asList(
                                new ThreadMessageOptions(
                                        MessageRole.USER,
                                        "How many shares of AAPL were outstanding at the end of of October 2021?")
                                        .setAttachments(Arrays.asList(
                                                new MessageAttachment(
                                                        appleFile.getId(),
                                                        Arrays.asList(
                                                                BinaryData.fromObject(new FileSearchToolDefinition())
                                                        )
                                                )
                                        ))
                        ))
        );
        //  The thread now has a vector store with that file in its tool resources.
        String threadId = thread.getId();

        // Step 5: Create a run and check the output
        ThreadRun run = client.createRun(threadId, new CreateRunOptions(assistantWithVectorStore.getId()));
        ThreadRun threadRun = waitOnRun(run, threadId);
        PageableList<ThreadMessage> messages = client.listMessages(threadRun.getThreadId());
        List<ThreadMessage> data = messages.getData();
        for (int i = 0; i < data.size(); i++) {
            ThreadMessage dataMessage = data.get(i);
            MessageRole role = dataMessage.getRole();
            for (MessageContent messageContent : dataMessage.getContent()) {
                MessageTextContent messageTextContent = (MessageTextContent) messageContent;
                System.out.println(i + ": Role = " + role + ", content = " + messageTextContent.getText().getValue());
            }
        }

        // Clean up resources
        client.deleteFile(googleFile.getId());
        client.deleteFile(berkshireFile.getId());
        client.deleteFile(appleFile.getId());
        client.deleteThread(threadId);
        client.deleteVectorStore(vectorStore.getId());
        client.deleteAssistant(financialAnalystAssistant.getId());
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
}
