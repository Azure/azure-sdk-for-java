// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatMessage;
import com.azure.ai.openai.models.ChatRole;
import com.azure.ai.openai.models.CompletionsFinishReason;
import com.azure.ai.openai.models.FunctionCall;
import com.azure.ai.openai.models.FunctionCallConfig;
import com.azure.ai.openai.models.FunctionDefinition;
import com.azure.ai.openai.usage.models.Parameters;
import com.azure.ai.openai.usage.models.Properties;
import com.azure.ai.openai.usage.models.Properties2;
import com.azure.core.credential.AzureKeyCredential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrates how to get chat completions using function call.
 */
public class FunctionCallSample {
    /**
     * Runs the sample algorithm and demonstrates how to get chat completions using function call.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String azureOpenaiKey = "{azure-open-ai-key}";
        String endpoint = "{azure-open-ai-endpoint}";
        String deploymentOrModelId = "{azure-open-ai-deployment-model-id}";

        OpenAIClient client = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(azureOpenaiKey))
            .buildClient();

        List<FunctionDefinition> functions = Arrays.asList(
            new FunctionDefinition("getCurrentWeather")
                .setDescription("Get the current weather")
                .setParameters(new Parameters("object", new Properties())),
            new FunctionDefinition("getNumberDayWeatherForecast")
                .setDescription("Get an N-day weather forecast")
                .setParameters(new Parameters("object", new Properties2()))
        );

        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatMessage(ChatRole.SYSTEM, "Don't make assumptions about what values to plug into functions. Ask for clarification if a user request is ambiguous."));
        chatMessages.add(new ChatMessage(ChatRole.USER, "What's the weather like today"));

        ChatCompletionsOptions chatCompletionOptions = new ChatCompletionsOptions(chatMessages)
            .setFunctionCall(FunctionCallConfig.AUTO)
            .setFunctions(functions);

        ChatCompletions chatCompletions = client.getChatCompletions(deploymentOrModelId, chatCompletionOptions);
        System.out.printf("Model ID=%s is created at %s.%n", chatCompletions.getId(), chatCompletions.getCreatedAt());
        handleResponse(chatCompletions.getChoices(), chatMessages);

        chatMessages.add(new ChatMessage(ChatRole.USER, "I'm in Glasgow, Scotland"));
        ChatCompletions chatCompletions2 = client.getChatCompletions(deploymentOrModelId, chatCompletionOptions);
        handleResponse(chatCompletions2.getChoices(), chatMessages);

        chatMessages.add(new ChatMessage(ChatRole.USER, "what is the weather going to be like in Glasgow, Scotland over the next x days"));
        ChatCompletions chatCompletions3 = client.getChatCompletions(deploymentOrModelId, chatCompletionOptions);
        handleResponse(chatCompletions3.getChoices(), chatMessages);

        chatMessages.add(new ChatMessage(ChatRole.USER, "five days"));
        ChatCompletions chatCompletions4 = client.getChatCompletions(deploymentOrModelId, chatCompletionOptions);
        handleResponse(chatCompletions4.getChoices(), chatMessages);
    }

    private static List<ChatMessage> handleResponse(List<ChatChoice> choices, List<ChatMessage> chatMessages) {
        for (ChatChoice choice : choices) {
            ChatMessage message = choice.getMessage();
            System.out.printf("Index: %d, Chat Role: %s.%n", choice.getIndex(), message.getRole());
            ChatMessage choiceMessage = choice.getMessage();
            System.out.printf("Role=%s, content=%s.%n", choiceMessage.getRole(), choiceMessage.getContent());
            FunctionCall functionCall = message.getFunctionCall();
            System.out.printf("Finished reason is %s.%n", choice.getFinishReason());

            if (CompletionsFinishReason.FUNCTION_CALL.equals(choice.getFinishReason())) {
                System.out.printf("Function name: %s, arguments: %s.%n", functionCall.getName(), functionCall.getArguments());
            } else {
                chatMessages.add(choiceMessage);
            }
        }
        return chatMessages;
    }
}
