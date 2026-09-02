// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesAsyncClient;
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
import com.openai.models.files.FileCreateParams;
import com.openai.models.files.FileObject;
import com.openai.models.files.FilePurpose;
import com.openai.models.responses.ResponseCreateParams;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Demonstrates asynchronously using an uploaded file with Code Interpreter and downloading generated output.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} - The model deployment name.</li>
 * </ul>
 */
public class CodeInterpreterWithFilesAsync {
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
        Path csv = SampleUtils.createTempFile("quarterly-results", ".csv",
            "quarter,revenue\nQ1,120\nQ2,150\nQ3,180\nQ4,210\n");
        FileObject uploaded = openAIClient.files().create(FileCreateParams.builder()
            .file(csv).purpose(FilePurpose.ASSISTANTS).build());
        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();

        CodeInterpreterTool tool = new CodeInterpreterTool().setContainer(
            new AutoCodeInterpreterToolParameter().setFileIds(Collections.singletonList(uploaded.id())));
        agentsClient.createAgentVersion("code-interpreter-files-async",
                new CreateAgentVersionInput(new PromptAgentDefinition(model)
                    .setInstructions("Analyze uploaded data and create downloadable files when requested.")
                    .setTools(Collections.singletonList(tool))))
            .doOnNext(agentRef::set)
            .flatMap(agent -> responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(SampleUtils.toAgentReference(agent)),
                ResponseCreateParams.builder().input("Create a CSV summary of quarterly revenue.")))
            .doOnNext(SampleUtils::printResponseText)
            .flatMap(response -> Mono.fromCallable(() -> {
                CodeInterpreterWithFilesSync.downloadGeneratedFile(openAIClient,
                    ToolSampleUtils.findContainerFile(response));
                return response;
            }))
            .then(cleanup(agentsClient, openAIClient, uploaded, csv, agentRef))
            .onErrorResume(error -> cleanup(agentsClient, openAIClient, uploaded, csv, agentRef)
                .then(Mono.error(error)))
            .block();
    }

    private static Mono<Void> cleanup(AgentsAsyncClient agentsClient, OpenAIClient openAIClient,
        FileObject uploaded, Path csv, AtomicReference<AgentVersionDetails> agentRef) {
        AgentVersionDetails agent = agentRef.get();
        Mono<Void> deleteAgent = agent == null ? Mono.empty()
            : agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
        return deleteAgent.then(Mono.fromRunnable(() -> {
            openAIClient.files().delete(uploaded.id());
            try {
                Files.deleteIfExists(csv);
            } catch (Exception error) {
                throw new RuntimeException(error);
            }
        }));
    }
}
