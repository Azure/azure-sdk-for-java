// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.hostedagents;

import com.azure.ai.agents.AgentSessionFilesAsyncClient;
import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.hostedagents.HostedAgentsSampleUtils.HostedAgentSessionResources;
import com.azure.ai.agents.models.AgentDefinitionOptInKeys;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This sample demonstrates hosted-agent session file operations using the async client.
 *
 * <p>Session files are currently a preview feature and only work with hosted-agent sessions.</p>
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_AGENT_CONTAINER_IMAGE - The hosted-agent container image.</li>
 * </ul>
 */
public class SessionFilesAsyncSample {
    private static final String REMOTE_FILE_PATH_1 = "/remote/data_file1.txt";
    private static final String REMOTE_FILE_PATH_2 = "/remote/data_file2.txt";

    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String image = Configuration.getGlobalConfiguration().get("FOUNDRY_AGENT_CONTAINER_IMAGE");
        String agentName = HostedAgentsSampleUtils.SAMPLE_AGENT_NAME;

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();
        AgentSessionFilesAsyncClient sessionFilesAsyncClient = builder.buildAgentSessionFilesAsyncClient();

        AtomicReference<HostedAgentSessionResources> resourcesRef = new AtomicReference<>();

        Mono<Void> workflow = HostedAgentsSampleUtils.createAgentAndSessionAsync(agentsAsyncClient, agentName, image)
            .flatMap(resources -> {
                resourcesRef.set(resources);
                String sessionId = resources.getSession().getAgentSessionId();

                return sessionFilesAsyncClient.uploadSessionFile(agentName, sessionId, REMOTE_FILE_PATH_1,
                    BinaryData.fromString("Sample session file 1."),
                    AgentDefinitionOptInKeys.HOSTED_AGENTS_V1_PREVIEW, null)
                    .doOnNext(response -> System.out.printf("Uploaded session file: %s%n", response.getPath()))
                    .then(sessionFilesAsyncClient.uploadSessionFile(agentName, sessionId, REMOTE_FILE_PATH_2,
                        BinaryData.fromString("Sample session file 2."),
                        AgentDefinitionOptInKeys.HOSTED_AGENTS_V1_PREVIEW, null))
                    .doOnNext(response -> System.out.printf("Uploaded session file: %s%n", response.getPath()))
                    .then(Mono.defer(() -> {
                        System.out.println("Listing session files for the session at path '/remote'...");
                        return sessionFilesAsyncClient.listSessionFiles(agentName, sessionId,
                            AgentDefinitionOptInKeys.HOSTED_AGENTS_V1_PREVIEW, "/remote", null, null, null, null, null)
                            .doOnNext(entry -> System.out.printf("  - name=%s, size=%d, isDirectory=%s%n",
                                entry.getName(), entry.getSize(), entry.isDirectory()))
                            .then();
                    }))
                    .then(sessionFilesAsyncClient.downloadSessionFile(agentName, sessionId, REMOTE_FILE_PATH_1,
                        AgentDefinitionOptInKeys.HOSTED_AGENTS_V1_PREVIEW, null))
                    .doOnNext(downloaded -> {
                        System.out.printf("Downloading and printing content from '%s'%n", REMOTE_FILE_PATH_1);
                        String fileContent = new String(downloaded.toBytes(), StandardCharsets.UTF_8);
                        System.out.printf("Session file content (%s):%n%s%n", REMOTE_FILE_PATH_1, fileContent);
                    })
                    .then(Mono.defer(() -> {
                        System.out.printf("Deleting session file at path: %s...%n", REMOTE_FILE_PATH_1);
                        return sessionFilesAsyncClient.deleteSessionFile(agentName, sessionId, REMOTE_FILE_PATH_1,
                            AgentDefinitionOptInKeys.HOSTED_AGENTS_V1_PREVIEW, false, null);
                    }))
                    .then(Mono.defer(() -> {
                        System.out.printf("Deleting session file at path: %s...%n", REMOTE_FILE_PATH_2);
                        return sessionFilesAsyncClient.deleteSessionFile(agentName, sessionId, REMOTE_FILE_PATH_2,
                            AgentDefinitionOptInKeys.HOSTED_AGENTS_V1_PREVIEW, false, null);
                    }));
            });

        workflow
            .onErrorResume(error -> HostedAgentsSampleUtils.cleanupAsync(agentsAsyncClient, agentName,
                resourcesRef.get()).then(Mono.error(error)))
            .then(Mono.defer(() -> HostedAgentsSampleUtils.cleanupAsync(agentsAsyncClient, agentName,
                resourcesRef.get())))
            .timeout(Duration.ofMinutes(15))
            .block();
    }
}
