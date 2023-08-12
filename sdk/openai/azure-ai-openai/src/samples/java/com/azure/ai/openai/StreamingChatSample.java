// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatMessage;
import com.azure.ai.openai.models.ChatRole;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.IterableStream;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates how to get chat completions for the provided chat messages.
 * Completions support a wide variety of tasks and generate text that continues from or "completes" provided
 * prompt data.
 */
public class StreamingChatSample {

    /**
     * Demonstrates how to get chat completions for the provided chat messages.
     * Completions support a wide variety of tasks and generate text that continues from or "completes" provided
     * prompt data.
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

        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatMessage(ChatRole.SYSTEM, "You are a helpful assistant. You will talk like a pirate."));
        chatMessages.add(new ChatMessage(ChatRole.USER, "Can you help me?"));
        chatMessages.add(new ChatMessage(ChatRole.ASSISTANT, "Of course, me hearty! What can I do for ye?"));
        chatMessages.add(new ChatMessage(ChatRole.USER, "What's the best way to train a parrot?"));

        IterableStream<ChatCompletions> chatCompletionsStream = client.getChatCompletionsStream(deploymentOrModelId,
            new ChatCompletionsOptions(chatMessages));

        // The delta is the message content for a streaming response.
        // Subsequence of streaming delta will be like:
        // "delta": {
        //     "role": "assistant"
        // },
        // "delta": {
        //     "content": "Why"
        //  },
        //  "delta": {
        //     "content": " don"
        //  },
        //  "delta": {
        //     "content": "'t"
        //  }
        chatCompletionsStream
            .stream()
            // Remove .skip(1) when using Non-Azure OpenAI API
            // Note: the first chat completions can be ignored when using Azure OpenAI service which is a known service bug.
            // TODO: remove .skip(1) when service fix the issue.
            .skip(1)
            .forEach(chatCompletions -> {
                ChatMessage delta = chatCompletions.getChoices().get(0).getDelta();
                if (delta.getRole() != null) {
                    System.out.println("Role = " + delta.getRole());
                }
                if (delta.getContent() != null) {
                    System.out.print(delta.getContent());
                }
            });
    }
}
