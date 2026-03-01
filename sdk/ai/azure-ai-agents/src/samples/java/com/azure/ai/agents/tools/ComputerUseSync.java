// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.AgentsServiceVersion;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.ComputerEnvironment;
import com.azure.ai.agents.models.ComputerUsePreviewTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.tools.ComputerUseUtil.HandleActionResult;
import com.azure.ai.agents.tools.ComputerUseUtil.ScreenshotInfo;
import com.azure.ai.agents.tools.ComputerUseUtil.SearchState;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseComputerToolCall;
import com.openai.models.responses.ResponseComputerToolCallOutputScreenshot;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputContent;
import com.openai.models.responses.ResponseInputImage;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseInputText;
import com.openai.models.responses.ResponseOutputItem;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This sample demonstrates how to use Computer Use Agent (CUA) functionality
 * with the Azure AI Agents client. It simulates browser automation by
 * creating an agent that can interact with computer interfaces through
 * simulated actions and screenshots.
 *
 * <p>The sample creates a Computer Use Agent that performs a web search simulation,
 * demonstrating how to handle computer actions like typing, clicking, and
 * taking screenshots in a controlled environment.</p>
 *
 * <p>Before running the sample, set these environment variables with your own values:</p>
 * <ul>
 *   <li>AZURE_AGENTS_ENDPOINT - The Azure AI Project endpoint, as found in the Overview
 *       page of your Microsoft Foundry portal.</li>
 *   <li>(Optional) AZURE_COMPUTER_USE_MODEL_DEPLOYMENT_NAME - The deployment name of the
 *       computer-use-preview model, as found under the "Name" column in the "Models + endpoints"
 *       tab in your Microsoft Foundry project.</li>
 * </ul>
 */
public class ComputerUseSync {

    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("AZURE_AGENTS_ENDPOINT");
        String model = configuration.get("AZURE_COMPUTER_USE_MODEL_DEPLOYMENT_NAME", "computer-use-preview");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .serviceVersion(AgentsServiceVersion.getLatest());

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        // Initialize state machine
        SearchState currentState = SearchState.INITIAL;

        // Load screenshot assets
        Map<String, ScreenshotInfo> screenshots;
        try {
            screenshots = ComputerUseUtil.loadScreenshotAssets();
            System.out.println("Successfully loaded screenshot assets");
        } catch (IOException e) {
            System.out.println("Failed to load required screenshot assets: " + e.getMessage());
            System.out.println("Please ensure the asset files exist in the assets directory.");
            return;
        }

        AgentVersionDetails agent = null;

        try {
            ComputerUsePreviewTool tool = new ComputerUsePreviewTool(
                ComputerEnvironment.WINDOWS,
                1026,
                769
            );

            PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
                .setInstructions("You are a computer automation assistant."
                    + "Be direct and efficient. When you reach the search results page, read and describe the actual search result titles and descriptions you can see.")
                .setTools(Collections.singletonList(tool));

            agent = agentsClient.createAgentVersion("ComputerUseAgent", agentDefinition);
            System.out.printf("Agent created (id: %s, name: %s, version: %s)%n",
                agent.getId(), agent.getName(), agent.getVersion());

            // Create the AgentReference for the response
            AgentReference agentReference = new AgentReference(agent.getName())
                .setVersion(agent.getVersion());

            // Initial request with screenshot - start with Bing search page
            System.out.println("Starting computer automation session (initial screenshot: cua_browser_search.png)...");

            // Build the initial input using proper OpenAI SDK types
            // Create multimodal content with both text and image parts
            List<ResponseInputContent> contentParts = Arrays.asList(
                // Text part
                ResponseInputContent.ofInputText(
                    ResponseInputText.builder()
                        .text("I need you to help me search for 'OpenAI news'. Please type 'OpenAI news' and submit the search. Once you see search results, the task is complete.")
                        .build()),
                // Image part (screenshot)
                ResponseInputContent.ofInputImage(
                    ResponseInputImage.builder()
                        .imageUrl(screenshots.get("browser_search").getUrl())
                        .detail(ResponseInputImage.Detail.HIGH)
                        .build())
            );

            // Create the user message with multimodal content
            List<ResponseInputItem> initialInput = Arrays.asList(
                ResponseInputItem.ofEasyInputMessage(
                    EasyInputMessage.builder()
                        .role(EasyInputMessage.Role.USER)
                        .contentOfResponseInputMessageContentList(contentParts)
                        .build())
            );

            Response response = responsesClient.createWithAgent(agentReference, ResponseCreateParams.builder()
                    .inputOfResponse(initialInput)
                    .truncation(ResponseCreateParams.Truncation.AUTO));

            System.out.printf("Initial response received (ID: %s)%n", response.id());

            // Main interaction loop with deterministic completion
            int maxIterations = 10; // Allow enough iterations for completion
            int iteration = 0;

            while (true) {
                if (iteration >= maxIterations) {
                    System.out.printf("%nReached maximum iterations (%d). Stopping.%n", maxIterations);
                    break;
                }

                iteration++;
                System.out.printf("%n--- Iteration %d ---%n", iteration);

                // Check for computer calls in the response
                List<ResponseOutputItem> computerCalls = response.output().stream()
                    .filter(ResponseOutputItem::isComputerCall)
                    .collect(Collectors.toList());

                if (computerCalls.isEmpty()) {
                    ComputerUseUtil.printFinalOutput(response);
                    break;
                }

                // Process the first computer call
                ResponseOutputItem computerCallItem = computerCalls.get(0);
                ResponseComputerToolCall computerCall = computerCallItem.asComputerCall();
                String callId = computerCall.callId();

                System.out.printf("Processing computer call (ID: %s)%n", callId);

                // Handle the action and get the screenshot info
                HandleActionResult result = ComputerUseUtil.handleComputerActionAndTakeScreenshot(
                    computerCall, currentState, screenshots);
                ScreenshotInfo screenshotInfo = result.getScreenshotInfo();
                currentState = result.getState();

                System.out.printf("Sending action result back to agent (using %s)...%n", screenshotInfo.getFilename());

                // Build the follow-up input with computer call output using proper OpenAI SDK types
                List<ResponseInputItem> followUpInput = Arrays.asList(
                    ResponseInputItem.ofComputerCallOutput(
                        ResponseInputItem.ComputerCallOutput.builder()
                            .callId(callId)
                            .output(ResponseComputerToolCallOutputScreenshot.builder()
                                .imageUrl(screenshotInfo.getUrl())
                                .build())
                            .build())
                );

                response = responsesClient.createWithAgent(agentReference, ResponseCreateParams.builder()
                    .previousResponseId(response.id())
                    .inputOfResponse(followUpInput)
                    .truncation(ResponseCreateParams.Truncation.AUTO));

                System.out.printf("Follow-up response received (ID: %s)%n", response.id());
            }
        } finally {
            System.out.println("\nCleaning up...");
            if (agent != null) {
                try {
                    agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
                    System.out.println("Agent deleted");
                } catch (Exception e) {
                    System.out.println("Failed to delete agent: " + e.getMessage());
                }
            }
        }
    }
}
