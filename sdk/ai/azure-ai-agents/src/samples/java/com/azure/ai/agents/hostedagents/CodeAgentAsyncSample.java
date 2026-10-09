// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.hostedagents;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.hostedagents.utils.CodeAgentSampleUtils;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * This sample demonstrates creating and downloading a code-based hosted agent using the asynchronous AgentsAsyncClient.
 *
 * <p>Code-based hosted agents are a preview feature. Before running, set {@code FOUNDRY_PROJECT_ENDPOINT} to your
 * Azure AI Foundry project endpoint.</p>
 */
public class CodeAgentAsyncSample {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String agentName = CodeAgentSampleUtils.SAMPLE_AGENT_NAME;

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);
        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();

        Mono<Void> workflow = agentsAsyncClient.deleteAgent(agentName)
            .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty())
            .then(Mono.fromCallable(CodeAgentSampleUtils::createCodeZip).subscribeOn(Schedulers.boundedElastic()))
            .flatMap(codeZipPath -> {
                // BEGIN: com.azure.ai.agents.hostedagents.CodeAgentAsyncSample.createAgentVersionFromCode_initial

                return agentsAsyncClient.createAgentVersionFromCode(
                    agentName,
                    CodeAgentSampleUtils.createHostedAgentDefinition(),
                    CodeAgentSampleUtils.createCodeFileDetails(codeZipPath),
                    CodeAgentSampleUtils.SAMPLE_DESCRIPTION,
                    CodeAgentSampleUtils.sampleMetadata())
                    .doOnNext(version -> {
                        System.out.printf("Created code-based agent: %s%n", version.getName());
                        CodeAgentSampleUtils.printLatestVersion(version);
                    })

                    // END: com.azure.ai.agents.hostedagents.CodeAgentAsyncSample.createAgentVersionFromCode_initial
                    // BEGIN: com.azure.ai.agents.hostedagents.CodeAgentAsyncSample.downloadAgentCode

                    .then(downloadAgentCode(agentsAsyncClient, agentName))

                    // END: com.azure.ai.agents.hostedagents.CodeAgentAsyncSample.downloadAgentCode

                    // BEGIN: com.azure.ai.agents.hostedagents.CodeAgentAsyncSample.createAgentVersionFromCode

                    .then(agentsAsyncClient.createAgentVersionFromCode(
                        agentName,
                        CodeAgentSampleUtils.createHostedAgentDefinition(),
                        CodeAgentSampleUtils.createCodeFileDetails(codeZipPath),
                        CodeAgentSampleUtils.SAMPLE_DESCRIPTION,
                        CodeAgentSampleUtils.sampleMetadata()))
                    .doOnNext(newVersion -> {
                        System.out.printf("Created code-based agent version: %s%n", newVersion.getVersion());
                        CodeAgentSampleUtils.printLatestVersion(newVersion);
                    })
                    .then();

                    // END: com.azure.ai.agents.hostedagents.CodeAgentAsyncSample.createAgentVersionFromCode
            });

        workflow
            .onErrorResume(error -> agentsAsyncClient.deleteAgent(agentName)
                .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty())
                .then(Mono.error(error)))
            .then(agentsAsyncClient.deleteAgent(agentName)
                .doOnSuccess(unused -> System.out.printf("Deleted code-based agent: %s%n", agentName))
                .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty()))
            .timeout(Duration.ofMinutes(15))
            .block();
    }

    private static Mono<Path> downloadAgentCode(AgentsAsyncClient agentsAsyncClient, String agentName) {
        return Mono.fromCallable(() -> {
            Path downloadPath = Files.createTempDirectory(agentName + "-").resolve("code.zip");
            return downloadPath;
        }).subscribeOn(Schedulers.boundedElastic())
            .flatMap(downloadPath -> agentsAsyncClient.downloadAgentCodeWithResponse(
                agentName, downloadPath.toString(), new RequestOptions()).thenReturn(downloadPath))
            .doOnNext(downloadPath -> System.out.println("Downloaded code package path: " + downloadPath));
    }
}
