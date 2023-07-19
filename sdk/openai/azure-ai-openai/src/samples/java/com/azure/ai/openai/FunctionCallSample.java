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
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // Function call is supported for model versions with suffix `-0613` and later.
        String deploymentOrModelId = "{azure-open-ai-deployment-model-id}";

        OpenAIClient client = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(azureOpenaiKey))
            .buildClient();

        List<FunctionDefinition> functions = Arrays.asList(
            new FunctionDefinition("getCurrentWeather")
                .setDescription("Get the current weather")
                .setParameters(getFunctionDefinition())
        );

        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatMessage(ChatRole.USER, "What should I wear in Boston depending on the weather?"));

        ChatCompletionsOptions chatCompletionOptions = new ChatCompletionsOptions(chatMessages)
            .setFunctionCall(FunctionCallConfig.AUTO)
            .setFunctions(functions);

        ChatCompletions chatCompletions = client.getChatCompletions(deploymentOrModelId, chatCompletionOptions);
        List<ChatMessage> chatMessages2 = handleFunctionCallResponse(chatCompletions.getChoices(), chatMessages);

        // Take your function_call result as the input prompt to make another request to service.
        ChatCompletionsOptions chatCompletionOptions2 = new ChatCompletionsOptions(chatMessages2);
        ChatCompletions chatCompletions2 = client.getChatCompletions(deploymentOrModelId, chatCompletionOptions2);
        List<ChatChoice> choices = chatCompletions2.getChoices();
        ChatMessage message = choices.get(0).getMessage();
        System.out.printf("Message: %s.%n", message.getContent());
    }

    private static Map<String, Object> getFunctionDefinition() {
        // Construct JSON in Map, or you can use create your own customized model.
        Map<String, Object> location = new HashMap<>();
        location.put("type", "string");
        location.put("description", "The city and state, e.g. San Francisco, CA");
        Map<String, Object> unit = new HashMap<>();
        unit.put("type", "string");
        unit.put("enum", Arrays.asList("celsius", "fahrenheit"));
        Map<String, Object> prop1 = new HashMap<>();
        prop1.put("location", location);
        prop1.put("unit", unit);
        Map<String, Object> functionDefinition = new HashMap<>();
        functionDefinition.put("type", "object");
        functionDefinition.put("required", Arrays.asList("location", "unit"));
        functionDefinition.put("properties", prop1);
        return functionDefinition;
    }

    private static List<ChatMessage> handleFunctionCallResponse(List<ChatChoice> choices, List<ChatMessage> chatMessages) {
        for (ChatChoice choice : choices) {
            ChatMessage choiceMessage = choice.getMessage();
            FunctionCall functionCall = choiceMessage.getFunctionCall();
            // We are looking for finish_reason = "function call".
            if (CompletionsFinishReason.FUNCTION_CALL.equals(choice.getFinishReason())) {
                // We call getCurrentWeather() and pass the result to the service.
                System.out.printf("Function name: %s, arguments: %s.%n", functionCall.getName(), functionCall.getArguments());
                // WeatherLocation is our class that represents the parameters to use in our function call.
                // We deserialize and pass it to our function.
                WeatherLocation weatherLocation = BinaryData.fromString(functionCall.getArguments()).toObject(WeatherLocation.class);

                int currentWeather = getCurrentWeather(weatherLocation);
                chatMessages.add(new ChatMessage(ChatRole.USER, String.format("The weather in %s is %d degrees %s.",
                    weatherLocation.getLocation(), currentWeather, weatherLocation.getUnit())));
            } else {
                chatMessages.add(choiceMessage);
            }
        }
        return chatMessages;
    }

    // This is the method we offer to OpenAI to be used as a function_call.
    // For this example, we ignore the input parameter and return a simple value.
    private static int getCurrentWeather(WeatherLocation weatherLocation) {
        return 35;
    }

    // WeatherLocation is used for this sample. This describes the parameter of the function you want to use.
    private static class WeatherLocation {
        @JsonProperty(value = "unit") String unit;
        @JsonProperty(value = "location") String location;
        @JsonCreator
        WeatherLocation(@JsonProperty(value = "unit") String unit, @JsonProperty(value = "location") String location) {
            this.unit = unit;
            this.location = location;
        }

        public String getUnit() {
            return unit;
        }

        public String getLocation() {
            return location;
        }
    }
}
