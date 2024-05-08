// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestAssistantMessage;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.ChatResponseMessage;
import com.azure.ai.openai.models.CompletionsFinishReason;
import com.azure.ai.openai.models.FunctionCall;
import com.azure.ai.openai.models.FunctionCallConfig;
import com.azure.ai.openai.models.FunctionDefinition;
import com.azure.ai.openai.models.FunctionParameters;
import com.azure.ai.openai.models.FunctionProperties;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
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
public class ChatCompletionsFunctionCall {
    /**
     * Runs the sample algorithm and demonstrates how to get chat completions using function call.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String azureOpenaiKey = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY");
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");
        // Function call is supported for model versions with suffix `-0613` and later.
        String deploymentOrModelId = "{azure-open-ai-deployment-model-id}";

        OpenAIClient client = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(azureOpenaiKey))
            .buildClient();

        List<FunctionDefinition> functions = Arrays.asList(
                new FunctionDefinition("getCurrentWeather")
                        .setDescription("Get the current weather")
                        .setParameters(BinaryData.fromObject(getCurrentWeatherFunctionParameters()))
        );
        List<ChatRequestMessage> chatRequestMessages = new ArrayList<>();
        chatRequestMessages.add(new ChatRequestUserMessage("What should I wear in Boston depending on the weather?"));

        ChatCompletions chatCompletions = client.getChatCompletions(deploymentOrModelId,
                new ChatCompletionsOptions(chatRequestMessages)
                        .setFunctionCall(FunctionCallConfig.AUTO)
                        .setFunctions(functions));

        // Take your function_call result as the input prompt to make another request to service.
        chatRequestMessages = handleFunctionCallResponse(chatCompletions.getChoices(),
                chatRequestMessages);
        ChatCompletions chatCompletionsAnswer = client.getChatCompletions(deploymentOrModelId,
                new ChatCompletionsOptions(chatRequestMessages));

        System.out.printf("Message: %s.%n", chatCompletionsAnswer.getChoices().get(0).getMessage().getContent());
    }

    private static FunctionParameters getCurrentWeatherFunctionParameters() {
        FunctionProperties location = new FunctionProperties()
                .setType("string")
                .setDescription("The city and state, e.g. San Francisco, CA");

        FunctionProperties unit = new FunctionProperties()
                .setType("string")
                .setEnumString(Arrays.asList("celsius", "fahrenheit"))
                .setDescription("The temperature unit to use. Infer this from the user's location.");

        Map<String, FunctionProperties> props = new HashMap<>();
        props.put("location", location);
        props.put("unit", unit);

        return new FunctionParameters()
                .setType("object")
                .setRequiredPropertyNames(Arrays.asList("location", "unit"))
                .setProperties(props);
    }

    private static List<ChatRequestMessage> handleFunctionCallResponse(List<ChatChoice> choices,
                                                                       List<ChatRequestMessage> chatMessages) {
        for (ChatChoice choice : choices) {
            ChatResponseMessage choiceMessage = choice.getMessage();
            FunctionCall functionCall = choiceMessage.getFunctionCall();
            // We are looking for finish_reason = "function call".
            if (CompletionsFinishReason.FUNCTION_CALL.equals(choice.getFinishReason())) {
                // We call getCurrentWeather() and pass the result to the service.
                System.out.printf("Function name: %s, arguments: %s.%n", functionCall.getName(),
                        functionCall.getArguments());
                // WeatherLocation is our class that represents the parameters to use in our function call.
                // We deserialize and pass it to our function.
                WeatherLocation weatherLocation = BinaryData.fromString(functionCall.getArguments())
                        .toObject(WeatherLocation.class);

                int currentWeather = getCurrentWeather(weatherLocation);
                chatMessages.add(new ChatRequestUserMessage(String.format("The weather in %s is %d degrees %s.",
                    weatherLocation.getLocation(), currentWeather, weatherLocation.getUnit())));
            } else {
                ChatRequestAssistantMessage messageHistory = new ChatRequestAssistantMessage(choiceMessage.getContent());
                messageHistory.setFunctionCall(choiceMessage.getFunctionCall());
                chatMessages.add(messageHistory);
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
        WeatherLocation(@JsonProperty(value = "unit") String unit,
                        @JsonProperty(value = "location") String location) {
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
