// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.usage;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatMessage;
import com.azure.ai.openai.models.ChatRole;
import com.azure.ai.openai.models.CompletionsUsage;
import com.azure.core.credential.AzureKeyCredential;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to get chat completions for the provided chat messages.
 * Completions support a wide variety of tasks and generate text that continues from or "completes" provided
 * prompt data.
 */
public class GetChatCompletionsAsyncSample {
    /**
     * Runs the sample algorithm and demonstrates how to get chat completions for the provided chat messages.
     * Completions support a wide variety of tasks and generate text that continues from or "completes" provided
     * prompt data.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) throws InterruptedException {
        String azureOpenaiKey = "{azure-open-ai-key}";
        String endpoint = "{azure-open-ai-endpoint}";
        String deploymentOrModelId = "{azure-open-ai-deployment-model-id}";

        OpenAIAsyncClient client = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(azureOpenaiKey))
            .buildAsyncClient();

        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatMessage(ChatRole.SYSTEM, "You are a helpful assistant. You will talk like a pirate."));
        chatMessages.add(new ChatMessage(ChatRole.USER, "Can you help me?"));
        chatMessages.add(new ChatMessage(ChatRole.ASSISTANT, "Of course, me hearty! What can I do for ye?"));
        chatMessages.add(new ChatMessage(ChatRole.USER, "What's the best way to train a parrot?"));

        client.getChatCompletions(deploymentOrModelId, new ChatCompletionsOptions(chatMessages)).subscribe(
            chatCompletions -> {
                System.out.printf("Model ID=%s is created at %s.%n", chatCompletions.getId(), chatCompletions.getCreatedAt());
                for (ChatChoice choice : chatCompletions.getChoices()) {
                    ChatMessage message = choice.getMessage();
                    System.out.printf("Index: %d, Chat Role: %s.%n", choice.getIndex(), message.getRole());
                    System.out.println("Message:");
                    System.out.println(message.getContent());
                }

                System.out.println();
                CompletionsUsage usage = chatCompletions.getUsage();
                System.out.printf("Usage: number of prompt token is %d, "
                        + "number of completion token is %d, and number of total tokens in request and response is %d.%n",
                    usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
            },
            error -> System.err.println("There was an error getting chat completions." + error),
            () -> System.out.println("Completed called getChatCompletions."));


        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        TimeUnit.SECONDS.sleep(10);
    }
}
