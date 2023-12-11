// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsFunctionToolCall;
import com.azure.ai.openai.models.ChatCompletionsFunctionToolDefinition;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatCompletionsToolCall;
import com.azure.ai.openai.models.ChatRequestAssistantMessage;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.ChatResponseMessage;
import com.azure.ai.openai.models.FunctionCall;
import com.azure.ai.openai.models.FunctionDefinition;
import com.azure.ai.openai.models.FunctionParameters;
import com.azure.ai.openai.models.FunctionProperties;
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
 * Sample demonstrates how to do a single and parallel function calling via 'tools' calling.
 */
public class FunctionToolCallingSample {

    /**
     * Runs the sample algorithm and demonstrates how to get chat completions using function tool calling.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String endpoint = "{azure-open-ai-endpoint}";
        String azureOpenaiKey = "{azure-open-ai-key}";
        // Newer models like gpt-4-1106-preview or gpt-3.5-turbo-1106 can call multiple functions in one turn.
        String deploymentOrModelId = "{azure-open-ai-deployment-model-id}";

        // Function call is supported for model versions with suffix `-0613` and later.
        OpenAIClient client = new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(azureOpenaiKey))
                .buildClient();

        singleFunctionCallingSample(client, deploymentOrModelId);
        parallelFunctionCallingSample(client, deploymentOrModelId);

    }

    private static void singleFunctionCallingSample(OpenAIClient client, String deploymentOrModelId) {
        List<ChatRequestMessage> chatRequestMessages = new ArrayList<>();
        chatRequestMessages.add(new ChatRequestSystemMessage("Don't make assumptions about what values to plug into functions. Ask for clarification if a user request is ambiguous."));
        chatRequestMessages.add(new ChatRequestUserMessage("Give me a weather report for Toronto, Canada."));

        // A list of tools to use (currently only has one tool type, function tool).
        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatRequestMessages)
                .setTools(
                        Arrays.asList(
                                getCurrentWeatherToolFunction,
                                getNDayWeatherForecastToolFunction
                        )
                );

        // We can force the model to use a specific function, for example get_n_day_weather_forecast by using the
        // function_call argument. By doing so, we force the model to make assumptions about how to use it.
        chatCompletionsOptions.setToolChoice(BinaryData.fromObject(getNDayWeatherForecastToolFunction));

        // We can also force the model to not use a function at all. By doing so we prevent it from producing a proper function calling.
        // chatCompletionsOptions.setToolChoice(BinaryData.fromString("none"));

        ChatCompletions chatCompletions = client.getChatCompletions(deploymentOrModelId, chatCompletionsOptions);

        // Take your function tool calling result as the input prompt to make another request to service.
        chatRequestMessages = handleFunctionToolResponse(chatCompletions.getChoices(), chatRequestMessages);
        ChatCompletions chatCompletionsAnswer = client.getChatCompletions(deploymentOrModelId,
                new ChatCompletionsOptions(chatRequestMessages));
        getContentFromMessageResponse(chatCompletionsAnswer);
    }

    private static void parallelFunctionCallingSample(OpenAIClient client, String deploymentOrModelId) {
        List<ChatRequestMessage> chatRequestMessages = new ArrayList<>();
        chatRequestMessages.add(new ChatRequestSystemMessage("Don't make assumptions about what values to plug into functions. Ask for clarification if a user request is ambiguous."));
        chatRequestMessages.add(new ChatRequestUserMessage("what is the weather going to be like in San Francisco and Glasgow over the next 4 days"));

        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatRequestMessages)
                .setTools(
                        Arrays.asList(
                                getCurrentWeatherToolFunction,
                                getNDayWeatherForecastToolFunction
                        )
                );
        // Newer models like gpt-4-1106-preview or gpt-3.5-turbo-1106 can call multiple functions in one turn.
        ChatCompletions chatCompletions = client.getChatCompletions(deploymentOrModelId, chatCompletionsOptions);
        chatRequestMessages = handleFunctionToolResponse(chatCompletions.getChoices(), chatRequestMessages);

        ChatCompletions chatCompletionsAnswer = client.getChatCompletions(deploymentOrModelId,
                new ChatCompletionsOptions(chatRequestMessages));
        getContentFromMessageResponse(chatCompletionsAnswer);
    }

    private static String getContentFromMessageResponse(ChatCompletions chatCompletions) {
        List<ChatChoice> choices = chatCompletions.getChoices();
        ChatResponseMessage message = choices.get(0).getMessage();
        System.out.printf("Message: %s.%n", message.getContent());
        return message.getContent();
    }

    private static FunctionParameters getCurrentWeatherFunctionParameters() {
        FunctionProperties location = new FunctionProperties()
                .setType("string")
                .setDescription("The city and state, e.g. San Francisco, CA");

        FunctionProperties format = new FunctionProperties()
                .setType("string")
                .setEnumString(Arrays.asList("celsius", "fahrenheit"))
                .setDescription("The temperature unit to use. Infer this from the users location.");

        Map<String, FunctionProperties> props = new HashMap<>();
        props.put("location", location);
        props.put("format", format);

        return new FunctionParameters()
                .setType("object")
                .setRequiredPropertyNames(Arrays.asList("location", "format"))
                .setProperties(props);
    }

    private static FunctionParameters getNDayWeatherForecastFunctionParameters() {
        FunctionProperties location = new FunctionProperties()
                .setType("string")
                .setDescription("The city and state, e.g. San Francisco, CA");

        FunctionProperties format = new FunctionProperties()
                .setType("string")
                .setEnumString(Arrays.asList("celsius", "fahrenheit"))
                .setDescription("The temperature unit to use. Infer this from the users location.");

        FunctionProperties numDays = new FunctionProperties()
                .setType("integer")
                .setDescription("The number of days to forecast");

        Map<String, FunctionProperties> props = new HashMap<>();
        props.put("location", location);
        props.put("format", format);
        props.put("num_days", numDays);

        return new FunctionParameters()
                .setType("object")
                .setRequiredPropertyNames(Arrays.asList("location", "format", "num_days"))
                .setProperties(props);
    }

    private static List<ChatRequestMessage> handleFunctionToolResponse(List<ChatChoice> choices,
                                                                      List<ChatRequestMessage> chatMessages) {
        for (ChatChoice choice : choices) {
            ChatResponseMessage choiceMessage = choice.getMessage();

            List<ChatCompletionsToolCall> toolCalls = choiceMessage.getToolCalls();
            if (toolCalls != null) {

                for (ChatCompletionsToolCall toolCall : toolCalls) {
                    ChatCompletionsFunctionToolCall functionToolCallResult =
                            (ChatCompletionsFunctionToolCall) toolCall;

                    FunctionCall functionCall = functionToolCallResult.getFunction();

                    // We call getNDayWeatherForecast() and pass the result to the service.
                    System.out.printf("Function name: %s, arguments: %s.%n", functionCall.getName(),
                            functionCall.getArguments());
                    // WeatherLocation is our class that represents the parameters to use in our function call.
                    // We deserialize and pass it to our function.
                    WeatherLocation weatherLocation = BinaryData.fromString(functionCall.getArguments())
                            .toObject(WeatherLocation.class);

                    chatMessages.add(new ChatRequestUserMessage(String.format("I want to have %d days weather forecast in %s with degrees format, %s%n",
                            getNDayWeatherForecast(weatherLocation),
                            weatherLocation.getLocation(),
                            weatherLocation.getFormat()
                    )));
                }
            } else {
                ChatRequestAssistantMessage messageHistory = new ChatRequestAssistantMessage(choiceMessage.getContent());
                messageHistory.setFunctionCall(choiceMessage.getFunctionCall());
                chatMessages.add(messageHistory);
            }
        }
        return chatMessages;
    }

    private static int getNDayWeatherForecast(WeatherLocation weatherLocation) {
        int ourOwnDefinedNumDays = 3;
        System.out.printf("Ignore the weatherLocation's number of days, which is %d, to forecast but use our "
                + "own defined value, %d.%n", weatherLocation.getNumDays(), ourOwnDefinedNumDays);
        return ourOwnDefinedNumDays;
    }

    // WeatherLocation is used for this sample. This describes the parameter of the function you want to use.
    private static class WeatherLocation {
        @JsonProperty(value = "format") String format;
        @JsonProperty(value = "location") String location;

        @JsonProperty(value = "num_days")Integer numDays;
        @JsonCreator
        WeatherLocation(@JsonProperty(value = "format") String format,
                        @JsonProperty(value = "location") String location) {
            this.format = format;
            this.location = location;
        }

        public WeatherLocation setNumDays(Integer numDays) {
            this.numDays = numDays;
            return this;
        }

        public String getFormat() {
            return format;
        }

        public String getLocation() {
            return location;
        }

        public Integer getNumDays() {
            return numDays;
        }
    }

    // Function tool 1: getCurrentWeather
    private static ChatCompletionsFunctionToolDefinition getCurrentWeatherToolFunction = new ChatCompletionsFunctionToolDefinition(
            new FunctionDefinition("getCurrentWeather")
                    .setParameters(BinaryData.fromObject(getCurrentWeatherFunctionParameters()))
    );

    // Function tool 2: getNDayWeatherForecast
    private static ChatCompletionsFunctionToolDefinition getNDayWeatherForecastToolFunction = new ChatCompletionsFunctionToolDefinition(
            new FunctionDefinition("getNDayWeatherForecast")
                    .setParameters(BinaryData.fromObject(getNDayWeatherForecastFunctionParameters()))
    );
}
