// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.SampleUtils;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AutoCodeInterpreterToolParameter;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.CodeInterpreterTool;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.core.http.HttpResponse;
import com.openai.models.containers.files.content.ContentRetrieveParams;
import com.openai.models.files.FileCreateParams;
import com.openai.models.files.FileObject;
import com.openai.models.files.FilePurpose;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;

/**
 * Demonstrates using an uploaded file with Code Interpreter and downloading a generated file.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} - The model deployment name.</li>
 * </ul>
 */
public class CodeInterpreterWithFilesSync {
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

        Path csv = SampleUtils.createTempFile("quarterly-results", ".csv",
            "quarter,revenue\nQ1,120\nQ2,150\nQ3,180\nQ4,210\n");
        FileObject uploaded = null;
        AgentVersionDetails agent = null;
        try {
            uploaded = openAIClient.files().create(FileCreateParams.builder()
                .file(csv).purpose(FilePurpose.ASSISTANTS).build());
            CodeInterpreterTool tool = new CodeInterpreterTool().setContainer(
                new AutoCodeInterpreterToolParameter().setFileIds(Collections.singletonList(uploaded.id())));
            agent = agentsClient.createAgentVersion("code-interpreter-files",
                new CreateAgentVersionInput(new PromptAgentDefinition(model)
                    .setInstructions("Analyze uploaded data and create downloadable files when requested.")
                    .setTools(Collections.singletonList(tool))));

            Response response = responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(SampleUtils.toAgentReference(agent)),
                ResponseCreateParams.builder().input("Create a CSV summary of quarterly revenue."));
            SampleUtils.printResponseText(response);
            downloadGeneratedFile(openAIClient, ToolSampleUtils.findContainerFile(response));
        } finally {
            if (agent != null) {
                agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
            }
            if (uploaded != null) {
                openAIClient.files().delete(uploaded.id());
            }
            Files.deleteIfExists(csv);
        }
    }

    static void downloadGeneratedFile(OpenAIClient client, ToolSampleUtils.ContainerFile generatedFile)
        throws Exception {
        if (generatedFile == null) {
            System.out.println("No generated file was returned.");
            return;
        }
        Path output = Files.createTempFile("agent-output-", "-" + generatedFile.getFilename());
        ContentRetrieveParams params = ContentRetrieveParams.builder()
            .containerId(generatedFile.getContainerId())
            .fileId(generatedFile.getFileId())
            .build();
        try (HttpResponse content = client.containers().files().content().retrieve(params)) {
            Files.copy(content.body(), output, StandardCopyOption.REPLACE_EXISTING);
        }
        System.out.println("Generated file downloaded to: " + output);
    }
}
