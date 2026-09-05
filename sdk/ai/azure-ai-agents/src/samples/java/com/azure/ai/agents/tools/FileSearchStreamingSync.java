// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.SampleUtils;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.FileSearchTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.util.IterableStream;
import com.openai.client.OpenAIClient;
import com.openai.helpers.ResponseAccumulator;
import com.openai.models.files.FileCreateParams;
import com.openai.models.files.FileObject;
import com.openai.models.files.FilePurpose;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseStreamEvent;
import com.openai.models.vectorstores.VectorStore;
import com.openai.models.vectorstores.VectorStoreCreateParams;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

/**
 * Demonstrates streaming an agent response while File Search queries an uploaded document.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} - The model deployment name.</li>
 * </ul>
 */
public class FileSearchStreamingSync {
    public static void main(String[] args) throws Exception {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_MODEL_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);
        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();
        OpenAIClient openAIClient = builder.buildOpenAIClient();

        Path document = SampleUtils.createTempFile("product-info", ".txt",
            "Contoso Smart Eyewear provides navigation, translation, and hands-free notifications.");
        FileObject uploaded = null;
        VectorStore vectorStore = null;
        AgentVersionDetails agent = null;
        try {
            uploaded = openAIClient.files().create(FileCreateParams.builder()
                .file(document).purpose(FilePurpose.ASSISTANTS).build());
            vectorStore = openAIClient.vectorStores().create(VectorStoreCreateParams.builder()
                .name("ProductInfoStreamingStore")
                .fileIds(Collections.singletonList(uploaded.id()))
                .build());
            FileSearchTool tool = new FileSearchTool(Collections.singletonList(vectorStore.id()));
            agent = agentsClient.createAgentVersion("file-search-streaming-agent",
                new CreateAgentVersionInput(new PromptAgentDefinition(model)
                    .setInstructions("Search the product document before answering.")
                    .setTools(Collections.singletonList(tool))));

            ResponseAccumulator accumulator = ResponseAccumulator.create();
            IterableStream<ResponseStreamEvent> events = responsesClient.createStreamingAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(SampleUtils.toAgentReference(agent)),
                ResponseCreateParams.builder().input("What features does Contoso Smart Eyewear provide?"));
            for (ResponseStreamEvent event : events) {
                accumulator.accumulate(event);
                event.fileSearchCallSearching().ifPresent(ignored ->
                    System.out.println("[Searching uploaded files]"));
                event.outputTextDelta().ifPresent(delta -> System.out.print(delta.delta()));
            }
            System.out.println();
            SampleUtils.printResponseText(accumulator.response());
        } finally {
            if (agent != null) {
                agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
            }
            if (vectorStore != null) {
                openAIClient.vectorStores().delete(vectorStore.id());
            }
            if (uploaded != null) {
                openAIClient.files().delete(uploaded.id());
            }
            Files.deleteIfExists(document);
        }
    }
}
