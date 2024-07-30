// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsFunctionToolCall;
import com.azure.ai.openai.models.ChatCompletionsFunctionToolDefinition;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatCompletionsToolCall;
import com.azure.ai.openai.models.ChatCompletionsToolDefinition;
import com.azure.ai.openai.models.ChatRequestAssistantMessage;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestToolMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.CompletionsFinishReason;
import com.azure.ai.openai.models.FunctionCall;
import com.azure.ai.openai.models.FunctionDefinition;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.IterableStream;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrates the usage for using tool_calls. This allows the LLM to request additional information from the client
 * to fulfill the request. Particularly, this sample shows how to handle responses in a sync streaming scenario.
 */
public class StreamingToolCall {

    /**
     * Running this sample will result in 2 requests. The first offering the LLM our function and a prompt that will
     * need the output of out function. The 2nd request will be the LLM asking us to call our function and provide the
     * output, so it can continue the text generation.
     *
     * @param args – Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String azureOpenaiKey = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY");
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");
        String deploymentOrModelId = "{azure-open-ai-deployment-model-id}";

        OpenAIClient client = new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(azureOpenaiKey))
                .buildClient();

        List<ChatRequestMessage> chatMessages = Arrays.asList(
                new ChatRequestSystemMessage("You are a helpful assistant."),
                new ChatRequestUserMessage("What sort of clothing should I wear today in Berlin?")
        );
        ChatCompletionsToolDefinition toolDefinition = new ChatCompletionsFunctionToolDefinition(
                getFutureTemperatureFunctionDefinition());

        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatMessages);
        chatCompletionsOptions.setTools(Arrays.asList(toolDefinition));

        IterableStream<ChatCompletions> chatCompletionsStream = client.getChatCompletionsStream(deploymentOrModelId, chatCompletionsOptions);

        String toolCallId = null;
        String functionName = null;
        StringBuilder functionArguments = new StringBuilder();
        CompletionsFinishReason finishReason = null;
        for (ChatCompletions chatCompletions : chatCompletionsStream) {
            // In the case of Azure, the 1st message will contain filter information but no choices sometimes
            if (chatCompletions.getChoices().isEmpty()) {
                continue;
            }
            ChatChoice choice = chatCompletions.getChoices().get(0);
            if (choice.getFinishReason() != null) {
                finishReason = choice.getFinishReason();
            }
            List<ChatCompletionsToolCall> toolCalls = choice.getDelta().getToolCalls();
            // We take the functionName when it's available, and we aggregate the arguments.
            // We also monitor FinishReason for TOOL_CALL. That's the LLM signaling we should
            // call our function
            if (toolCalls != null) {
                ChatCompletionsFunctionToolCall toolCall = (ChatCompletionsFunctionToolCall) toolCalls.get(0);
                if (toolCall != null) {
                    functionArguments.append(toolCall.getFunction().getArguments());
                    if (toolCall.getId() != null) {
                        toolCallId = toolCall.getId();
                    }

                    if (toolCall.getFunction().getName() != null) {
                        functionName = toolCall.getFunction().getName();
                    }
                }
            }
        }

        System.out.println("Tool Call Id: " + toolCallId);
        System.out.println("Function Name: " + functionName);
        System.out.println("Function Arguments: " + functionArguments);
        System.out.println("Finish Reason: " + finishReason);

        // We verify that the LLM wants us to call the function we advertised in the original request
        // Preparation for follow-up with the service we add:
        // - All the messages we sent
        // - The ChatCompletionsFunctionToolCall from the service as part of a ChatRequestAssistantMessage
        // - The result of function tool as part of a ChatRequestToolMessage
        if (finishReason == CompletionsFinishReason.TOOL_CALLS) {
            // Here the "content" can be null if used in non-Azure OpenAI
            // We prepare the assistant message reminding the LLM of the context of this request. We provide:
            // - The tool call id
            // - The function description
            FunctionCall functionCall = new FunctionCall(functionName, functionArguments.toString());
            ChatCompletionsFunctionToolCall functionToolCall = new ChatCompletionsFunctionToolCall(toolCallId, functionCall);
            ChatRequestAssistantMessage assistantRequestMessage = new ChatRequestAssistantMessage("");
            assistantRequestMessage.setToolCalls(Arrays.asList(functionToolCall));

            // As an additional step, you may want to deserialize the parameters, so you can call your function
            FunctionArguments parameters = BinaryData.fromString(functionArguments.toString()).toObject(FunctionArguments.class);
            System.out.println("Location Name: " + parameters.locationName);
            System.out.println("Date: " + parameters.date);
            String functionCallResult = futureTemperature(parameters.locationName, parameters.date);

            // This message contains the information that will allow the LLM to resume the text generation
            ChatRequestToolMessage toolRequestMessage = new ChatRequestToolMessage(functionCallResult, toolCallId);
            List<ChatRequestMessage> followUpMessages = Arrays.asList(
                    // We add the original messages from the request
                    chatMessages.get(0),
                    chatMessages.get(1),
                    assistantRequestMessage,
                    toolRequestMessage
            );

            IterableStream<ChatCompletions> followUpChatCompletionsStream = client.getChatCompletionsStream(
                    deploymentOrModelId, new ChatCompletionsOptions(followUpMessages));

            StringBuilder finalResult = new StringBuilder();
            CompletionsFinishReason finalFinishReason = null;
            for (ChatCompletions chatCompletions : followUpChatCompletionsStream) {
                if (chatCompletions.getChoices().isEmpty()) {
                    continue;
                }
                ChatChoice choice = chatCompletions.getChoices().get(0);
                if (choice.getFinishReason() != null) {
                    finalFinishReason = choice.getFinishReason();
                }
                if (choice.getDelta().getContent() != null) {
                    finalResult.append(choice.getDelta().getContent());
                }
            }

            // We verify that the LLM has STOPPED as a finishing reason
            if (finalFinishReason == CompletionsFinishReason.STOPPED) {
                System.out.println("Final Result: " + finalResult);
            }
        }
    }

    // In this example we ignore the parameters for our tool function
    private static String futureTemperature(String locationName, String data) {
        return "-7 C";
    }

    private static FunctionDefinition getFutureTemperatureFunctionDefinition() {
        FunctionDefinition functionDefinition = new FunctionDefinition("FutureTemperature");
        functionDefinition.setDescription("Get the future temperature for a given location and date.");
        FutureTemperatureParameters parameters = new FutureTemperatureParameters();
        functionDefinition.setParameters(BinaryData.fromObject(parameters));
        return functionDefinition;
    }

    private static class FunctionArguments {
        @JsonProperty(value = "location_name")
        private String locationName;

        @JsonProperty(value = "date")
        private String date;
    }

    private static class FutureTemperatureParameters {
        @JsonProperty(value = "type")
        private String type = "object";

        @JsonProperty(value = "properties")
        private FutureTemperatureProperties properties = new FutureTemperatureProperties();
    }

    private static class FutureTemperatureProperties {
        @JsonProperty(value = "unit") StringField unit = new StringField("Temperature unit. Can be either Celsius or Fahrenheit. Defaults to Celsius.");
        @JsonProperty(value = "location_name") StringField locationName = new StringField("The name of the location to get the future temperature for.");
        @JsonProperty(value = "date") StringField date = new StringField("The date to get the future temperature for. The format is YYYY-MM-DD.");
    }

    private static class StringField {
        @JsonProperty(value = "type")
        private final String type = "string";

        @JsonProperty(value = "description")
        private String description;

        @JsonCreator
        StringField(@JsonProperty(value = "description") String description) {
            this.description = description;
        }
    }
}
