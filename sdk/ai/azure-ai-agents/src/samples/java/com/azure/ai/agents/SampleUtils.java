// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentDetails;
import com.azure.ai.agents.models.AgentEndpointConfig;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.FixedRatioVersionSelectionRule;
import com.azure.ai.agents.models.ProtocolConfiguration;
import com.azure.ai.agents.models.ResponsesProtocolConfiguration;
import com.azure.ai.agents.models.UpdateAgentDetailsOptions;
import com.azure.ai.agents.models.VersionSelector;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class SampleUtils {

    /**
     * Pins an agent endpoint to a specific agent version and enables the OpenAI Responses protocol.
     *
     * @param agentsClient the agents client
     * @param versionDetails the agent version to pin
     * @return the updated agent details
     */
    public static AgentDetails pinAgentVersion(AgentsClient agentsClient, AgentVersionDetails versionDetails) {
        return agentsClient.updateAgentDetails(versionDetails.getName(), createPinnedEndpointOptions(versionDetails));
    }

    /**
     * Pins an agent endpoint to a specific agent version and enables the OpenAI Responses protocol.
     *
     * @param agentsAsyncClient the asynchronous agents client
     * @param versionDetails the agent version to pin
     * @return a publisher containing the updated agent details
     */
    public static Mono<AgentDetails> pinAgentVersion(AgentsAsyncClient agentsAsyncClient,
        AgentVersionDetails versionDetails) {
        return agentsAsyncClient.updateAgentDetails(versionDetails.getName(),
            createPinnedEndpointOptions(versionDetails));
    }

    /**
     * Gets the path to a file in the sample resource folder.
     * @param fileName the name of the file in the sample resource folder
     * @return Path to the sample resource file
     */
    public static Path getResourcePath(String fileName) {
        try {
            URL resourceUrl = SampleUtils.class.getClassLoader().getResource(fileName);
            if (resourceUrl != null) {
                return Paths.get(resourceUrl.toURI());
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URI for sample resource: " + fileName, e);
        }

        Path[] resourcePaths = new Path[] {
            Paths.get("src", "samples", "resources", fileName),
            Paths.get("sdk", "ai", "azure-ai-agents", "src", "samples", "resources", fileName)
        };
        for (Path resourcePath : resourcePaths) {
            if (Files.exists(resourcePath)) {
                return resourcePath;
            }
        }
        throw new RuntimeException("Sample resource file not found: " + fileName);
    }

    private static UpdateAgentDetailsOptions createPinnedEndpointOptions(AgentVersionDetails versionDetails) {
        AgentEndpointConfig endpointConfig = new AgentEndpointConfig()
            .setVersionSelector(new VersionSelector().setVersionSelectionRules(Collections.singletonList(
                new FixedRatioVersionSelectionRule(100).setAgentVersion(versionDetails.getVersion()))))
            .setProtocolConfiguration(new ProtocolConfiguration().setResponses(new ResponsesProtocolConfiguration()));

        return new UpdateAgentDetailsOptions().setAgentEndpoint(endpointConfig);
    }
}
