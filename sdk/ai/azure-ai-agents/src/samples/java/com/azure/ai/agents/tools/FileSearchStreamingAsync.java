// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesAsyncClient;
import com.azure.ai.agents.SampleUtils;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.FileSearchTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.helpers.ResponseAccumulator;
import com.openai.models.files.FileCreateParams;
import com.openai.models.files.FileObject;
import com.openai.models.files.FilePurpose;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.vectorstores.VectorStore;
import com.openai.models.vectorstores.VectorStoreCreateParams;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Demonstrates asynchronously streaming an agent response while File Search queries an uploaded document.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} - The model deployment name.</li>
 * </ul>
 */
public class FileSearchStreamingAsync {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_MODEL_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);
        AgentsAsyncClient agentsClient = builder.buildAgentsAsyncClient();
        ResponsesAsyncClient responsesClient = builder.buildResponsesAsyncClient();
        OpenAIClient openAIClient = builder.buildOpenAIClient();

        Path document = SampleUtils.createTempFile("product-info", ".txt",
            "Contoso Smart Eyewear provides navigation, translation, and hands-free notifications.");
        FileObject uploaded = openAIClient.files().create(FileCreateParams.builder()
            .file(document).purpose(FilePurpose.ASSISTANTS).build());
        VectorStore vectorStore = openAIClient.vectorStores().create(VectorStoreCreateParams.builder()
            .name("ProductInfoStreamingStoreAsync")
            .fileIds(Collections.singletonList(uploaded.id()))
            .build());
        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();
        ResponseAccumulator accumulator = ResponseAccumulator.create();

        FileSearchTool tool = new FileSearchTool(Collections.singletonList(vectorStore.id()));
        agentsClient.createAgentVersion("file-search-streaming-async",
                new CreateAgentVersionInput(new PromptAgentDefinition(model)
                    .setInstructions("Search the product document before answering.")
                    .setTools(Collections.singletonList(tool))))
            .doOnNext(agentRef::set)
            .flatMapMany(agent -> responsesClient.createStreamingAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(SampleUtils.toAgentReference(agent)),
                ResponseCreateParams.builder().input("What features does Contoso Smart Eyewear provide?")))
            .doOnNext(event -> {
                accumulator.accumulate(event);
                event.fileSearchCallSearching().ifPresent(ignored ->
                    System.out.println("[Searching uploaded files]"));
                event.outputTextDelta().ifPresent(delta -> System.out.print(delta.delta()));
            })
            .then(Mono.fromRunnable(() -> {
                System.out.println();
                SampleUtils.printResponseText(accumulator.response());
            }))
            .then(Mono.defer(() -> cleanup(agentsClient, openAIClient, agentRef, uploaded, vectorStore, document)))
            .onErrorResume(error -> cleanup(agentsClient, openAIClient, agentRef, uploaded, vectorStore, document)
                .then(Mono.error(error)))
            .block();
    }

    private static Mono<Void> cleanup(AgentsAsyncClient agentsClient, OpenAIClient openAIClient,
        AtomicReference<AgentVersionDetails> agentRef, FileObject uploaded, VectorStore vectorStore, Path document) {
        AgentVersionDetails agent = agentRef.get();
        Mono<Void> deleteAgent = agent == null ? Mono.empty()
            : agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
        return deleteAgent.then(Mono.fromRunnable(() -> {
            openAIClient.vectorStores().delete(vectorStore.id());
            openAIClient.files().delete(uploaded.id());
            try {
                Files.deleteIfExists(document);
            } catch (Exception error) {
                throw new RuntimeException(error);
            }
        }));
    }
}
