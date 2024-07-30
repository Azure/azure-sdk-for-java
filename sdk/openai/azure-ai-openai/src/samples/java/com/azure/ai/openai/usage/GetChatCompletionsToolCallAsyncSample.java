// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.usage;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletionsFunctionToolCall;
import com.azure.ai.openai.models.ChatCompletionsFunctionToolDefinition;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatCompletionsToolDefinition;
import com.azure.ai.openai.models.ChatRequestAssistantMessage;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestToolMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.CompletionsFinishReason;
import com.azure.ai.openai.models.FunctionDefinition;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates the usage for using tool_calls. This allows the LLM to request additional information from the client
 * to fulfill the request. Particularly, this sample shows how to handle responses in an async non-streaming scenario.
 */
public class GetChatCompletionsToolCallAsyncSample {

    /**
     * Running this sample will result in 2 requests. The first offering the LLM our function and a prompt that will
     * need the output of out function. The 2nd request will be the LLM asking us to call our function and provide the
     * output, so it can continue the text generation.
     *
     * @param args – Unused. Arguments to the program.
     */
    public static void main(String[] args) throws InterruptedException {
        String azureOpenaiKey = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY");
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");
        String deploymentOrModelId = "{azure-open-ai-deployment-model-id}";

        OpenAIAsyncClient client = new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(azureOpenaiKey))
                .buildAsyncClient();

        List<ChatRequestMessage> chatMessages = Arrays.asList(
                new ChatRequestSystemMessage("You are a helpful assistant."),
                new ChatRequestUserMessage("What sort of clothing should I wear today in Berlin?")
        );
        ChatCompletionsToolDefinition toolDefinition = new ChatCompletionsFunctionToolDefinition(
                getFutureTemperatureFunctionDefinition());

        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatMessages);
        chatCompletionsOptions.setTools(Arrays.asList(toolDefinition));


        client.getChatCompletions(deploymentOrModelId, chatCompletionsOptions)
                // The LLM is requesting the calling of the function we defined in the original request
                .filter(chatCompletions -> chatCompletions.getChoices().get(0).getFinishReason() == CompletionsFinishReason.TOOL_CALLS)
                .flatMap(chatCompletions -> {
                    ChatChoice choice = chatCompletions.getChoices().get(0);
                    ChatCompletionsFunctionToolCall toolCall = (ChatCompletionsFunctionToolCall) choice.getMessage().getToolCalls().get(0);
                    String functionName = toolCall.getFunction().getName();
                    String functionArguments = toolCall.getFunction().getArguments();

                    System.out.println("Function Name: " + functionName);
                    System.out.println("Function Arguments: " + functionArguments);

                    // As an additional step, you may want to deserialize the parameters, so you can call your function
                    FunctionArguments parameters = BinaryData.fromString(functionArguments).toObject(FunctionArguments.class);
                    System.out.println("Location Name: " + parameters.locationName);
                    System.out.println("Date: " + parameters.date);

                    String functionCallResult = futureTemperature(parameters.locationName, parameters.date);

                    ChatRequestAssistantMessage assistantMessage = new ChatRequestAssistantMessage("");
                    assistantMessage.setToolCalls(choice.getMessage().getToolCalls());

                    // We include:
                    // - The past 2 messages from the original request
                    // - A new ChatRequestAssistantMessage with the tool calls from the original request
                    // - A new ChatRequestToolMessage with the result of our function call
                    List<ChatRequestMessage> followUpMessages = Arrays.asList(
                            chatMessages.get(0),
                            chatMessages.get(1),
                            assistantMessage,
                            new ChatRequestToolMessage(functionCallResult, toolCall.getId())
                    );

                    ChatCompletionsOptions followUpChatCompletionsOptions = new ChatCompletionsOptions(followUpMessages);

                    return client.getChatCompletions(deploymentOrModelId, followUpChatCompletionsOptions);
                })
                .subscribe(
                        followUpChatCompletions -> {
                            // This time the finish reason is STOPPED
                            ChatChoice followUpChoice = followUpChatCompletions.getChoices().get(0);
                            if (followUpChoice.getFinishReason() == CompletionsFinishReason.STOPPED) {
                                System.out.println("Chat Completions Result: " + followUpChoice.getMessage().getContent());
                            }
                        },
                        error -> System.err.println("There was an error getting chat completions." + error),
                        () -> System.out.println("Completed called getChatCompletions."));
        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        TimeUnit.SECONDS.sleep(10);
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
        @JsonProperty(value = "unit")
        StringField unit = new StringField("Temperature unit. Can be either Celsius or Fahrenheit. Defaults to Celsius.");
        @JsonProperty(value = "location_name")
        StringField locationName = new StringField("The name of the location to get the future temperature for.");
        @JsonProperty(value = "date")
        StringField date = new StringField("The date to get the future temperature for. The format is YYYY-MM-DD.");
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
