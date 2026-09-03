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
import com.azure.core.util.BinaryData;
import com.openai.client.OpenAIClient;
import com.openai.models.files.FileCreateParams;
import com.openai.models.files.FileObject;
import com.openai.models.files.FilePurpose;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ToolChoiceOptions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Demonstrates binding an uploaded file to a Code Interpreter tool through structured inputs.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} - The model deployment name.</li>
 * </ul>
 */
public class CodeInterpreterStructuredInputsSync {
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

        Path csv = SampleUtils.createTempFile("numbers", ".csv", "x\n1\n2\n3\n");
        FileObject uploaded = null;
        AgentVersionDetails agent = null;
        try {
            uploaded = openAIClient.files().create(FileCreateParams.builder()
                .file(csv).purpose(FilePurpose.ASSISTANTS).build());
            CodeInterpreterTool tool = new CodeInterpreterTool().setContainer(
                new AutoCodeInterpreterToolParameter()
                    .setFileIds(Collections.singletonList("{{analysis_file_id}}")));
            PromptAgentDefinition definition = new PromptAgentDefinition(model)
                .setInstructions("Read the bound CSV file and calculate the sum of x.")
                .setTools(Collections.singletonList(tool))
                .setStructuredInputs(ToolSampleUtils.structuredInput("analysis_file_id",
                    "File ID available to Code Interpreter"));
            agent = agentsClient.createAgentVersion("code-interpreter-structured-input",
                new CreateAgentVersionInput(definition));

            Map<String, BinaryData> values = new LinkedHashMap<>();
            values.put("analysis_file_id", BinaryData.fromObject(uploaded.id()));
            Response response = responsesClient.createAzureResponse(
                new AzureCreateResponseOptions()
                    .setAgentReference(SampleUtils.toAgentReference(agent))
                    .setStructuredInputs(values),
                ResponseCreateParams.builder()
                    .input("Return the sum of x in numbers.csv.")
                    .toolChoice(ToolChoiceOptions.REQUIRED));
            SampleUtils.printResponseText(response);
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
}
