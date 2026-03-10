// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.ImageGenTool;
import com.azure.ai.agents.models.ImageGenToolModel;
import com.azure.ai.agents.models.ImageGenToolQuality;
import com.azure.ai.agents.models.ImageGenToolSize;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.util.Collections;

/**
 * This sample demonstrates how to create an agent with the Image Generation tool
 * to generate images from text descriptions.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_DEPLOYMENT_NAME - The model deployment name.</li>
 *   <li>IMAGE_GENERATION_MODEL_DEPLOYMENT_NAME - The image generation model deployment name.</li>
 * </ul>
 */
public class ImageGenerationSync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_DEPLOYMENT_NAME");
        String imageModel = Configuration.getGlobalConfiguration().get("IMAGE_GENERATION_MODEL_DEPLOYMENT_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

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

        AgentVersionDetails agent = agentsClient.createAgentVersion("image-gen-agent", agentDefinition);
        System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

        try {
            // Create a response
            AgentReference agentReference = new AgentReference(agent.getName())
                .setVersion(agent.getVersion());

            Response response = responsesClient.createWithAgent(
                agentReference,
                ResponseCreateParams.builder()
                    .input("Generate an image of a sunset over a mountain range"));

            // The response output may include image_generation_call items with base64-encoded image data.
            // This sample prints the response status and the number of output items; image extraction
            // and saving would require additional parsing logic that is not shown here.
            System.out.println("Response status: " + response.status().map(Object::toString).orElse("unknown"));
            System.out.println("Output items: " + response.output().size());
        } finally {
            // Clean up
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
        }
    }
}
