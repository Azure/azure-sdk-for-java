// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.hostedagents;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaAgentsClient;
import com.azure.ai.agents.hostedagents.utils.CodeAgentSampleUtils;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This sample demonstrates creating and downloading a code-based hosted agent using the synchronous AgentsClient.
 *
 * <p>Code-based hosted agents are a preview feature. Before running, set {@code FOUNDRY_PROJECT_ENDPOINT} to your
 * Azure AI Foundry project endpoint.</p>
 */
public class CodeAgentSample {
    public static void main(String[] args) throws IOException {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String agentName = CodeAgentSampleUtils.SAMPLE_AGENT_NAME;

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);
        AgentsClient agentsClient = builder.buildAgentsClient();
        BetaAgentsClient betaAgentsClient = builder.beta().buildBetaAgentsClient();

        try {
            agentsClient.deleteAgent(agentName);
        } catch (ResourceNotFoundException ignored) {
            // The sample agent does not already exist.
        }

        try {
            // BEGIN: com.azure.ai.agents.hostedagents.CodeAgentSample.createAgentVersionFromCode_initial

            BinaryData codeZip = CodeAgentSampleUtils.createCodeZip();
            String codeZipSha256 = CodeAgentSampleUtils.sha256(codeZip);

            AgentVersionDetails version = betaAgentsClient.createAgentVersionFromCode(
                agentName,
                codeZipSha256,
                CodeAgentSampleUtils.createAgentVersionFromCodeContent(codeZip));

            System.out.printf("Created code-based agent: %s%n", version.getName());
            CodeAgentSampleUtils.printLatestVersion(version);

            // END: com.azure.ai.agents.hostedagents.CodeAgentSample.createAgentVersionFromCode_initial

            // BEGIN: com.azure.ai.agents.hostedagents.CodeAgentSample.downloadAgentCode

            BinaryData downloadedCode = betaAgentsClient.downloadAgentCode(agentName, null);
            Path downloadPath = Files.createTempFile(agentName + "-", ".zip");
            Files.write(downloadPath, downloadedCode.toBytes());
            System.out.println("Downloaded code package path: " + downloadPath);

            // END: com.azure.ai.agents.hostedagents.CodeAgentSample.downloadAgentCode

            // BEGIN: com.azure.ai.agents.hostedagents.CodeAgentSample.createAgentVersionFromCode

            AgentVersionDetails newVersion = betaAgentsClient.createAgentVersionFromCode(
                agentName,
                codeZipSha256,
                CodeAgentSampleUtils.createAgentVersionFromCodeContent(codeZip));

            System.out.printf("Created code-based agent version: %s%n", newVersion.getVersion());
            CodeAgentSampleUtils.printLatestVersion(newVersion);

            // END: com.azure.ai.agents.hostedagents.CodeAgentSample.createAgentVersionFromCode
        } finally {
            try {
                agentsClient.deleteAgent(agentName);
                System.out.printf("Deleted code-based agent: %s%n", agentName);
            } catch (ResourceNotFoundException ignored) {
                // The sample agent may not have been created.
            }
        }
    }
}
