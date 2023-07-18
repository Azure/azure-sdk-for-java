// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatMessage;
import com.azure.ai.openai.models.ChatRole;
import com.azure.ai.openai.models.CompletionsUsage;
import com.azure.ai.openai.models.FunctionCall;
import com.azure.ai.openai.models.FunctionCallConfig;
import com.azure.ai.openai.models.FunctionDefinition;
import com.azure.ai.openai.usage.models.Parameters;
import com.azure.core.credential.AzureKeyCredential;

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
            new FunctionDefinition("MyFunction").setParameters(new Parameters())
        );

        List<ChatMessage> chatMessages = Arrays.asList(
            new ChatMessage(ChatRole.USER).setContent("What's the weather like in San Francisco in Celsius?")
        );

        ChatCompletionsOptions chatCompletionOptions = new ChatCompletionsOptions(chatMessages)
            .setFunctionCall(FunctionCallConfig.AUTO)
            .setFunctions(functions);

        ChatCompletions chatCompletions = client.getChatCompletions(deploymentOrModelId, chatCompletionOptions);

        System.out.printf("Model ID=%s is created at %d.%n", chatCompletions.getId(), chatCompletions.getCreatedAt());
        for (ChatChoice choice : chatCompletions.getChoices()) {
            ChatMessage message = choice.getMessage();
            System.out.printf("Index: %d, Chat Role: %s.%n", choice.getIndex(), message.getRole());
            FunctionCall functionCall = message.getFunctionCall();
            System.out.printf("Function name: %s, arguments: %s.%n", functionCall.getName(), functionCall.getArguments());
        }

        System.out.println();
        CompletionsUsage usage = chatCompletions.getUsage();
        System.out.printf("Usage: number of prompt token is %d, "
                + "number of completion token is %d, and number of total tokens in request and response is %d.%n",
            usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
    }
}
