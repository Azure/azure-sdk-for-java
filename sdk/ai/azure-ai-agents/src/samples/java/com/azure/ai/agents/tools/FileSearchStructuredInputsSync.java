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
import com.azure.ai.agents.models.StructuredInputDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.util.BinaryData;
import com.openai.client.OpenAIClient;
import com.openai.models.files.FileCreateParams;
import com.openai.models.files.FileObject;
import com.openai.models.files.FilePurpose;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.vectorstores.VectorStore;
import com.openai.models.vectorstores.VectorStoreCreateParams;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Demonstrates binding a vector store to File Search through structured inputs.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} - The model deployment name.</li>
 * </ul>
 */
public class FileSearchStructuredInputsSync {
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
                .name("ProductInfoStructuredStore")
                .fileIds(Collections.singletonList(uploaded.id()))
                .build());

            Map<String, StructuredInputDefinition> definitions = new LinkedHashMap<>();
            definitions.putAll(ToolSampleUtils.structuredInput("vector_store_id",
                "Vector store ID used by File Search"));
            definitions.putAll(ToolSampleUtils.structuredInput("source_file_id",
                "Source file ID for the prompt context"));
            FileSearchTool tool = new FileSearchTool(Collections.singletonList("{{vector_store_id}}"));
            PromptAgentDefinition definition = new PromptAgentDefinition(model)
                .setInstructions("Search the bound vector store. The source file is {{source_file_id}}.")
                .setTools(Collections.singletonList(tool))
                .setStructuredInputs(definitions);
            agent = agentsClient.createAgentVersion("file-search-structured-input",
                new CreateAgentVersionInput(definition));

            Map<String, BinaryData> values = new LinkedHashMap<>();
            values.put("vector_store_id", BinaryData.fromObject(vectorStore.id()));
            values.put("source_file_id", BinaryData.fromObject(uploaded.id()));
            Response response = responsesClient.createAzureResponse(
                new AzureCreateResponseOptions()
                    .setAgentReference(SampleUtils.toAgentReference(agent))
                    .setStructuredInputs(values),
                ResponseCreateParams.builder().input("What features does Contoso Smart Eyewear provide?"));
            SampleUtils.printResponseText(response);
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
