// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.AgentsServiceVersion;
import com.azure.ai.agents.ResponsesAsyncClient;
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
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * This sample demonstrates how to use Computer Use Agent (CUA) functionality
 * with the Azure AI Agents async client. It simulates browser automation by
 * creating an agent that can interact with computer interfaces through
 * simulated actions and screenshots.
 *
 * <p>The sample creates a Computer Use Agent that performs a web search simulation,
 * demonstrating how to handle computer actions like typing, clicking, and
 * taking screenshots in a controlled environment using reactive patterns.</p>
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
public class ComputerUseAsync {

    private static final int MAX_ITERATIONS = 10;

    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("AZURE_AGENTS_ENDPOINT");
        String model = configuration.get("AZURE_COMPUTER_USE_MODEL_DEPLOYMENT_NAME", "computer-use-preview");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .serviceVersion(AgentsServiceVersion.getLatest());

        AgentsAsyncClient agentsClient = builder.buildAgentsAsyncClient();
        ResponsesAsyncClient responsesClient = builder.buildResponsesAsyncClient();

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

        ComputerUsePreviewTool tool = new ComputerUsePreviewTool(
            ComputerEnvironment.WINDOWS,
            1026,
            769
        );

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a computer automation assistant."
            + "Be direct and efficient. When you reach the search results page, read and describe the actual search result titles and descriptions you can see.")
            .setTools(Collections.singletonList(tool));

        // Use AtomicReference to track the agent for cleanup
        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();

        // Build the initial input using proper OpenAI SDK types
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

        // Create agent and run the interaction loop
        agentsClient.createAgentVersion("ComputerUseAgent", agentDefinition)
            .doOnNext(agent -> {
                agentRef.set(agent);
                System.out.printf("Agent created (id: %s, name: %s, version: %s)%n",
                    agent.getId(), agent.getName(), agent.getVersion());
            })
            .flatMap(agent -> {
                // Create the AgentReference for the response
                AgentReference agentReference = new AgentReference(agent.getName())
                    .setVersion(agent.getVersion());

                System.out.println("Starting computer automation session (initial screenshot: cua_browser_search.png)...");

                // Send initial request
                return responsesClient.createWithAgent(agentReference, ResponseCreateParams.builder()
                        .inputOfResponse(initialInput)
                        .truncation(ResponseCreateParams.Truncation.AUTO))
                    .doOnNext(response -> System.out.printf("Initial response received (ID: %s)%n", response.id()))
                    .flatMap(response -> runInteractionLoop(
                        responsesClient, agentReference, response, screenshots, SearchState.INITIAL, 0));
            })
            .doFinally(signalType -> {
                System.out.println("\nCleaning up...");
                AgentVersionDetails agent = agentRef.get();
                if (agent != null) {
                    agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion())
                        .doOnSuccess(v -> System.out.println("Agent deleted"))
                        .doOnError(e -> System.out.println("Failed to delete agent: " + e.getMessage()))
                        .subscribe();
                }
            })
            .block(); // Block to wait for completion in main method
    }

    /**
     * Recursively processes the interaction loop with the Computer Use Agent.
     *
     * @param responsesClient The responses async client.
     * @param agentReference The agent reference.
     * @param response The current response from the agent.
     * @param screenshots The map of screenshot assets.
     * @param currentState The current search state.
     * @param iteration The current iteration number.
     * @return A Mono that completes when the loop finishes.
     */
    private static Mono<Void> runInteractionLoop(
            ResponsesAsyncClient responsesClient,
            AgentReference agentReference,
            Response response,
            Map<String, ScreenshotInfo> screenshots,
            SearchState currentState,
            int iteration) {

        if (iteration >= MAX_ITERATIONS) {
            System.out.printf("%nReached maximum iterations (%d). Stopping.%n", MAX_ITERATIONS);
            return Mono.empty();
        }

        System.out.printf("%n--- Iteration %d ---%n", iteration + 1);

        // Check for computer calls in the response
        List<ResponseOutputItem> computerCalls = response.output().stream()
            .filter(ResponseOutputItem::isComputerCall)
            .collect(Collectors.toList());

        if (computerCalls.isEmpty()) {
            ComputerUseUtil.printFinalOutput(response);
            return Mono.empty();
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
        SearchState newState = result.getState();

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

        return responsesClient.createWithAgent(agentReference, ResponseCreateParams.builder()
                .previousResponseId(response.id())
                .inputOfResponse(followUpInput)
                .truncation(ResponseCreateParams.Truncation.AUTO))
            .doOnNext(newResponse -> System.out.printf("Follow-up response received (ID: %s)%n", newResponse.id()))
            .flatMap(newResponse -> runInteractionLoop(
                responsesClient, agentReference, newResponse, screenshots, newState, iteration + 1));
    }
}
