// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.hostedagents;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.BetaAgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.hostedagents.utils.HostedAgentsSampleUtils;
import com.azure.ai.agents.hostedagents.utils.HostedAgentsSampleUtils.HostedAgentSessionResources;
import com.azure.ai.agents.models.SessionDirectoryEntry;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.nio.charset.StandardCharsets;

/**
 * This sample demonstrates hosted-agent session file upload, list, download, and delete operations.
 *
 * <p>Session files are currently a preview feature and only work with hosted-agent sessions.</p>
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_AGENT_CONTAINER_IMAGE - The hosted-agent container image.</li>
 * </ul>
 */
public class SessionFilesSample {
    private static final String REMOTE_FILE_PATH_1 = "/remote/data_file1.txt";
    private static final String REMOTE_FILE_PATH_2 = "/remote/data_file2.txt";

    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String image = Configuration.getGlobalConfiguration().get("FOUNDRY_AGENT_CONTAINER_IMAGE");
        String agentName = HostedAgentsSampleUtils.SAMPLE_AGENT_NAME;

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.allowPreview(true).buildAgentsClient();
        BetaAgentsClient betaAgentsClient = builder.beta().buildBetaAgentsClient();

        HostedAgentSessionResources resources = null;
        try {
            resources = HostedAgentsSampleUtils.createAgentAndSession(agentsClient, betaAgentsClient, agentName, image);
            String sessionId = resources.getSession().getAgentSessionId();

            betaAgentsClient.uploadSessionFile(agentName, sessionId, REMOTE_FILE_PATH_1,
                BinaryData.fromString("Sample session file 1."), null);
            System.out.printf("Uploaded session file: %s%n", REMOTE_FILE_PATH_1);

            betaAgentsClient.uploadSessionFile(agentName, sessionId, REMOTE_FILE_PATH_2,
                BinaryData.fromString("Sample session file 2."), null);
            System.out.printf("Uploaded session file: %s%n", REMOTE_FILE_PATH_2);

            System.out.println("Listing session files for the session at path '/remote'...");
            for (SessionDirectoryEntry entry : betaAgentsClient.listSessionFiles(agentName, sessionId, "/remote", null, null, null, null, null)) {
                System.out.printf("  - name=%s, size=%d, isDirectory=%s%n", entry.getName(), entry.getSize(),
                    entry.isDirectory());
            }

            System.out.printf("Downloading and printing content from '%s'%n", REMOTE_FILE_PATH_1);
            BinaryData downloaded = betaAgentsClient.downloadSessionFile(agentName, sessionId, REMOTE_FILE_PATH_1, null);
            String fileContent = new String(downloaded.toBytes(), StandardCharsets.UTF_8);
            System.out.printf("Session file content (%s):%n%s%n", REMOTE_FILE_PATH_1, fileContent);

            System.out.printf("Deleting session file at path: %s...%n", REMOTE_FILE_PATH_1);
            betaAgentsClient.deleteSessionFile(agentName, sessionId, REMOTE_FILE_PATH_1, false, null);

            System.out.printf("Deleting session file at path: %s...%n", REMOTE_FILE_PATH_2);
            betaAgentsClient.deleteSessionFile(agentName, sessionId, REMOTE_FILE_PATH_2, false, null);
        } finally {
            HostedAgentsSampleUtils.cleanup(agentsClient, betaAgentsClient, agentName, resources);
        }
    }
}
