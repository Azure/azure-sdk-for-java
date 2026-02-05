// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.FileSearchTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.AuthenticationUtil;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.credential.BearerTokenCredential;
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

/**
 * This sample demonstrates how to create an Azure AI Agent with the File Search tool
 * and use it to get responses that involve searching uploaded files.
 * It creates a vector store, uploads a file, and then uses the agent to search the file.
 */
public class FileSearchAgent {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_MODEL");

        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(credential)
            .serviceVersion(AgentsServiceVersion.getLatest())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();
        ConversationsClient conversationsClient = builder.buildConversationsClient();

        // Create OpenAI client for vector store and file operations
        OpenAIClient openAIClient = OpenAIOkHttpClient.builder()
            .baseUrl(endpoint)
            .credential(BearerTokenCredential.create(AuthenticationUtil.getBearerTokenSupplier(
                        credential, "https://cognitiveservices.azure.com/.default")))
            .build();

        AgentVersionDetails agent = null;
        Conversation conversation = null;
        VectorStore vectorStore = null;
        FileObject uploadedFile = null;
        Path tempFile = null;

        try {
            String sampleContent = "The Solar System consists of the Sun and the celestial objects bound to it by gravity. "
                + "The eight planets in order from the Sun are: Mercury, Venus, Earth, Mars, Jupiter, Saturn, Uranus, and Neptune. "
                + "Earth is the third planet from the Sun and the only known planet to harbor life. "
                + "Jupiter is the largest planet, while Mercury is the smallest. ";

            tempFile = Files.createTempFile("sample_document", ".txt");
            Files.write(tempFile, sampleContent.getBytes(StandardCharsets.UTF_8));

            uploadedFile = openAIClient.files().create(FileCreateParams.builder()
                .file(tempFile)
                .purpose(FilePurpose.ASSISTANTS)
                .build());
            System.out.println("Uploaded file: " + uploadedFile.id());

            vectorStore = openAIClient.vectorStores().create(VectorStoreCreateParams.builder()
                .name("SampleVectorStore")
                .fileIds(Collections.singletonList(uploadedFile.id()))
                .build());
            System.out.println("Created vector store: " + vectorStore.id());

            System.out.println("Waiting for vector store to process files...");
            sleepSeconds(5); // Wait for vector store to be ready

            // Create a FileSearchTool with the vector store ID
            FileSearchTool tool = new FileSearchTool(Collections.singletonList(vectorStore.id()));

            // Create the agent definition with File Search tool enabled
            PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
                .setInstructions("You are a helpful assistant that can search through uploaded files to answer questions. "
                    + "When asked about information, use the file search tool to find relevant content from the files.")
                .setTools(Collections.singletonList(tool));

            agent = agentsClient.createAgentVersion("MyFileSearchAgent", agentDefinition);
            System.out.printf("Agent created (id: %s, version: %s)\n", agent.getId(), agent.getVersion());

            AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());

            conversation = conversationsClient.getConversationService().create();
            System.out.println("Created Conversation: " + conversation.id());

            Response response = responsesClient.createWithAgentConversation(agentReference, conversation.id(),
                    ResponseCreateParams.builder().input("What is the largest planet in the Solar System?"));

            // Process and display the response
            System.out.println("\n=== Agent Response ===");
            for (ResponseOutputItem outputItem : response.output()) {
                // Handle message output
                if (outputItem.message().isPresent()) {
                    ResponseOutputMessage message = outputItem.message().get();
                    message.content().forEach(content -> {
                        content.outputText().ifPresent(text -> {
                            System.out.println("Assistant: " + text.text());
                        });
                    });
                }

                // Handle file search tool call output
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

            System.out.println("\nResponse ID: " + response.id());
            System.out.println("Model Used: " + response.model());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cleanup created resources
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception e) {
                    System.err.println("Failed to delete temp file: " + e.getMessage());
                }
            }
            if (conversation != null) {
                conversationsClient.getConversationService().delete(conversation.id());
                System.out.println("Conversation deleted successfully.");
            }
            if (agent != null) {
                agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
                System.out.println("Agent deleted successfully.");
            }
            if (vectorStore != null) {
                openAIClient.vectorStores().delete(vectorStore.id());
                System.out.println("Vector store deleted successfully.");
            }
            if (uploadedFile != null) {
                openAIClient.files().delete(uploadedFile.id());
                System.out.println("File deleted successfully.");
            }
        }
    }

    private static void sleepSeconds(long seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Sleep interrupted", e);
        }
    }
}