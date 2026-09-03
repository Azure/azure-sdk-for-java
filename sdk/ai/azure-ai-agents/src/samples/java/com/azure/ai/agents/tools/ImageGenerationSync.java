// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.ImageGenTool;
import com.azure.ai.agents.models.ImageGenToolModel;
import com.azure.ai.agents.models.ImageGenToolQuality;
import com.azure.ai.agents.models.ImageGenToolSize;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.client.OpenAIClient;
import com.azure.ai.agents.models.AgentEndpointConfig;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.FixedRatioVersionSelectionRule;
import com.azure.ai.agents.models.ProtocolConfiguration;
import com.azure.ai.agents.models.ResponsesProtocolConfiguration;
import com.azure.ai.agents.models.UpdateAgentDetailsOptions;
import com.azure.ai.agents.models.VersionSelector;

import java.util.Collections;

/**
 * This sample demonstrates how to create an agent with the Image Generation tool
 * to generate images from text descriptions.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 *   <li>IMAGE_GENERATION_MODEL_DEPLOYMENT_NAME - The image generation model deployment name.</li>
 * </ul>
 */
public class ImageGenerationSync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        String imageModel = Configuration.getGlobalConfiguration().get("IMAGE_GENERATION_MODEL_DEPLOYMENT_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();

        // BEGIN: com.azure.ai.agents.define_image_generation
        // Create image generation tool with model, quality, and size
        ImageGenTool imageGenTool = new ImageGenTool()
            .setModel(ImageGenToolModel.fromString(imageModel))
            .setQuality(ImageGenToolQuality.LOW)
            .setSize(ImageGenToolSize.fromString("1024x1024"));
        // END: com.azure.ai.agents.define_image_generation

        // Create agent with image generation tool
        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a creative assistant that can generate images based on descriptions.")
            .setTools(Collections.singletonList(imageGenTool));

        String agentName = "image-gen-agent";
        AgentVersionDetails agent = agentsClient.createAgentVersion(agentName, agentDefinition);
        try {
            agentsClient.updateAgentDetails(agentName, new UpdateAgentDetailsOptions().setAgentEndpoint(
                new AgentEndpointConfig()
                    .setVersionSelector(new VersionSelector().setVersionSelectionRules(Collections.singletonList(
                        new FixedRatioVersionSelectionRule(100).setAgentVersion(agent.getVersion()))))
                    .setProtocolConfiguration(new ProtocolConfiguration().setResponses(new ResponsesProtocolConfiguration()))));


            OpenAIClient openAIClient = builder.buildAgentScopedOpenAIClient(agentName);

            Response response = openAIClient.responses().create(ResponseCreateParams.builder()
                    .input("Generate an image of a sunset over a mountain range")
                .build());

            // The response output may include image_generation_call items with base64-encoded image data.
            // This sample prints the response status and the number of output items; image extraction
            // and saving would require additional parsing logic that is not shown here.
            System.out.println("Response status: " + response.status().map(Object::toString).orElse("unknown"));
            System.out.println("Output items: " + response.output().size());
        } finally {
            agentsClient.deleteAgentVersion(agentName, agent.getVersion());
        }
    }
}
