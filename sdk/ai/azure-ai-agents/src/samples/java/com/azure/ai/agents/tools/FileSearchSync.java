// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.FileSearchTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.models.conversations.Conversation;
import com.openai.models.files.FileCreateParams;
import com.openai.models.files.FileObject;
import com.openai.models.files.FilePurpose;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseFileSearchToolCall;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.vectorstores.VectorStore;
import com.openai.models.vectorstores.VectorStoreCreateParams;
import com.openai.services.blocking.ConversationService;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

/**
 * This sample demonstrates how to create an agent with the File Search tool.
 * It creates a vector store, uploads a file, and uses the agent to search the file
 * within a conversation.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 * </ul>
 */
public class FileSearchSync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");

        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(credential)
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();
        ConversationService conversationService = builder.buildOpenAIClient().conversations();
        OpenAIClient openAIClient = builder.buildOpenAIClient();

        AgentVersionDetails agent = null;
        Conversation conversation = null;
        VectorStore vectorStore = null;
        FileObject uploadedFile = null;
        Path tempFile = null;

        try {
            // Create a sample document and upload it
            String sampleContent = "The Solar System consists of the Sun and the celestial objects bound to it by gravity. "
                + "The eight planets in order from the Sun are: Mercury, Venus, Earth, Mars, Jupiter, Saturn, Uranus, and Neptune. "
                + "Earth is the third planet from the Sun and the only known planet to harbor life. "
                + "Jupiter is the largest planet, while Mercury is the smallest.";

            tempFile = Files.createTempFile("sample_document", ".txt");
            Files.write(tempFile, sampleContent.getBytes(StandardCharsets.UTF_8));

            uploadedFile = openAIClient.files().create(FileCreateParams.builder()
                .file(tempFile)
                .purpose(FilePurpose.ASSISTANTS)
                .build());
            System.out.println("Uploaded file: " + uploadedFile.id());

            // Create a vector store with the uploaded file
            vectorStore = openAIClient.vectorStores().create(VectorStoreCreateParams.builder()
                .name("SampleVectorStore")
                .fileIds(Collections.singletonList(uploadedFile.id()))
                .build());
            System.out.println("Created vector store: " + vectorStore.id());

            System.out.println("Waiting for vector store to process files...");
            Thread.sleep(5000);

            // BEGIN: com.azure.ai.agents.define_file_search
            // Create a FileSearchTool with the vector store ID
            FileSearchTool tool = new FileSearchTool(Collections.singletonList(vectorStore.id()));
            // END: com.azure.ai.agents.define_file_search

            PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
                .setInstructions("You are a helpful assistant that can search through uploaded files to answer questions. "
                    + "When asked about information, use the file search tool to find relevant content from the files.")
                .setTools(Collections.singletonList(tool));

            agent = agentsClient.createAgentVersion("file-search-agent", agentDefinition);
            System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

            AgentReference agentReference = new AgentReference(agent.getName())
                .setVersion(agent.getVersion());

            // Create a conversation and ask the agent
            conversation = conversationService.create();
            System.out.println("Created conversation: " + conversation.id());

            Response response = responsesClient.createWithAgentConversation(agentReference, conversation.id(),
                ResponseCreateParams.builder()
                    .input("What is the largest planet in the Solar System?"));

            // Process and display the response
            for (ResponseOutputItem outputItem : response.output()) {
                if (outputItem.message().isPresent()) {
                    ResponseOutputMessage message = outputItem.message().get();
                    message.content().forEach(content -> {
                        content.outputText().ifPresent(text -> {
                            System.out.println("Assistant: " + text.text());
                        });
                    });
                }

                if (outputItem.fileSearchCall().isPresent()) {
                    ResponseFileSearchToolCall fileSearchCall = outputItem.fileSearchCall().get();
                    System.out.println("\n--- File Search Execution ---");
                    System.out.println("Call ID: " + fileSearchCall.id());
                    System.out.println("Status: " + fileSearchCall.status());
                    fileSearchCall.results().ifPresent(results -> {
                        System.out.println("Results found: " + results.size());
                        results.forEach(result -> {
                            System.out.println("  - File ID: " + result.fileId());
                            result.filename().ifPresent(name -> System.out.println("    Filename: " + name));
                            result.score().ifPresent(score -> System.out.println("    Score: " + score));
                            result.text().ifPresent(text -> System.out.println("    Text: " + text));
                        });
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception ignored) {
                    // best-effort cleanup
                }
            }
            if (conversation != null) {
                conversationService.delete(conversation.id());
            }
            if (agent != null) {
                agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
                System.out.println("Agent deleted");
            }
            if (vectorStore != null) {
                openAIClient.vectorStores().delete(vectorStore.id());
                System.out.println("Vector store deleted");
            }
            if (uploadedFile != null) {
                openAIClient.files().delete(uploadedFile.id());
                System.out.println("File deleted");
            }
        }
    }
}
