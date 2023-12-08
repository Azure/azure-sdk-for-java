// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.core.credential.KeyCredential;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample demonstrates how to use a fixed integer seed to generate consistent outputs from our model.This is
 * particularly useful in scenarios where reproducibility is important. However, it's important to note that
 * while the seed ensures consistency, it does not guarantee the quality of the output.
 */
public class ChatCompletionsWithSeedSystemFingerprint {
    /**
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String azureOpenaiKey = "{azure-open-ai-key}";
        String endpoint = "{azure-open-ai-endpoint}";
        String deploymentOrModelId = "{azure-open-ai-deployment-model-id}"; // "gpt-3.5-turbo-1106";

        OpenAIClient client = new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(new KeyCredential(azureOpenaiKey))
                .buildClient();

        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant that generates short stories."));
        chatMessages.add(new ChatRequestUserMessage("Generate a short story about a journey to California"));

        // Generating a consistent short story with a fixed seed. Use a fixed integer to generate consistent outputs.
        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatMessages)
                .setSeed(123L);

        ChatCompletions previousResponse = client.getChatCompletions(deploymentOrModelId, chatCompletionsOptions);
        ChatCompletions currentResponse = client.getChatCompletions(deploymentOrModelId, chatCompletionsOptions);

        printResponse(previousResponse, currentResponse);
    }

    private static void printResponse(ChatCompletions previousResponse, ChatCompletions currentResponse) {
        System.out.println("--------------------------------------------------------------");

        System.out.println("System fingerprint(previous): " + previousResponse.getSystemFingerprint());
        System.out.println("System fingerprint(current): " + currentResponse.getSystemFingerprint());

        System.out.println("Number of prompt tokens(previous): " + previousResponse.getUsage().getPromptTokens());
        System.out.println("Number of prompt tokens(current): " + currentResponse.getUsage().getPromptTokens());

        System.out.println("Number of completion tokens(previous): " + previousResponse.getUsage().getCompletionTokens());
        System.out.println("Number of completion tokens(current): " + currentResponse.getUsage().getCompletionTokens());

        System.out.println("Content(previous): " + previousResponse.getChoices().get(0).getMessage().getContent());
        System.out.print("\n\n\n\n");
        System.out.println("Content(current): " + currentResponse.getChoices().get(0).getMessage().getContent());

        System.out.println("--------------------------------------------------------------");
    }
}
