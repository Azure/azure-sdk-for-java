// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ConversationsAsyncClient;
import com.azure.ai.agents.ResponsesAsyncClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.FileSearchTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
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
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This sample demonstrates how to create an agent with the File Search tool
 * using the async client. It creates a vector store, uploads a file, and uses
 * the agent to search the file within a conversation.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_DEPLOYMENT_NAME - The model deployment name.</li>
 * </ul>
 */
public class FileSearchAsync {
    public static void main(String[] args) throws Exception {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_DEPLOYMENT_NAME");

        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(credential)
            .endpoint(endpoint);

        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();
        ResponsesAsyncClient responsesAsyncClient = builder.buildResponsesAsyncClient();
        ConversationsAsyncClient conversationsAsyncClient = builder.buildConversationsAsyncClient();
        // Vector store and file operations use the sync OpenAI client for setup
        OpenAIClient openAIClient = builder.buildOpenAIClient();

        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();
        AtomicReference<String> conversationIdRef = new AtomicReference<>();

        // Create a sample document and upload it
        String sampleContent = "The Solar System consists of the Sun and the celestial objects bound to it by gravity. "
            + "The eight planets in order from the Sun are: Mercury, Venus, Earth, Mars, Jupiter, Saturn, Uranus, and Neptune. "
            + "Earth is the third planet from the Sun and the only known planet to harbor life. "
            + "Jupiter is the largest planet, while Mercury is the smallest.";

        Path tempFile = Files.createTempFile("sample_document", ".txt");
        Files.write(tempFile, sampleContent.getBytes(StandardCharsets.UTF_8));

        FileObject uploadedFile = openAIClient.files().create(FileCreateParams.builder()
            .file(tempFile)
            .purpose(FilePurpose.ASSISTANTS)
            .build());
        System.out.println("Uploaded file: " + uploadedFile.id());

        VectorStore vectorStore = openAIClient.vectorStores().create(VectorStoreCreateParams.builder()
            .name("SampleVectorStore")
            .fileIds(Collections.singletonList(uploadedFile.id()))
            .build());
        System.out.println("Created vector store: " + vectorStore.id());

        Thread.sleep(5000); // Wait for vector store to process

        FileSearchTool tool = new FileSearchTool(Collections.singletonList(vectorStore.id()));

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant that can search through uploaded files to answer questions.")
            .setTools(Collections.singletonList(tool));

        agentsAsyncClient.createAgentVersion("file-search-agent", agentDefinition)
            .flatMap(agent -> {
                agentRef.set(agent);
                System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

                AgentReference agentReference = new AgentReference(agent.getName())
                    .setVersion(agent.getVersion());

                return Mono.fromFuture(conversationsAsyncClient.getConversationServiceAsync().create())
                    .<Response>flatMap(conversation -> {
                        conversationIdRef.set(conversation.id());
                        System.out.println("Created conversation: " + conversation.id());

                        return responsesAsyncClient.createWithAgentConversation(agentReference, conversation.id(),
                            ResponseCreateParams.builder()
                                .input("What is the largest planet in the Solar System?"));
                    });
            })
            .doOnNext(response -> {
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
                            });
                        });
                    }
                }
            })
            .then(Mono.defer(() -> {
                String convId = conversationIdRef.get();
                if (convId != null) {
                    return Mono.fromFuture(conversationsAsyncClient.getConversationServiceAsync().delete(convId))
                        .then();
                }
                return Mono.empty();
            }))
            .then(Mono.defer(() -> {
                AgentVersionDetails agent = agentRef.get();
                if (agent != null) {
                    return agentsAsyncClient.deleteAgentVersion(agent.getName(), agent.getVersion())
                        .doOnSuccess(v -> System.out.println("Agent deleted"));
                }
                return Mono.empty();
            }))
            .doFinally(signal -> {
                openAIClient.vectorStores().delete(vectorStore.id());
                openAIClient.files().delete(uploadedFile.id());
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception ignored) {
                    // best-effort
                }
                System.out.println("Vector store and file deleted");
            })
            .doOnError(error -> System.err.println("Error: " + error.getMessage()))
            .timeout(Duration.ofSeconds(300))
            .block();
    }
}
