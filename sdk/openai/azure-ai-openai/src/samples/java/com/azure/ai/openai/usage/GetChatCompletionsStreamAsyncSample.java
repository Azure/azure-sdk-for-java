// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.usage;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestAssistantMessage;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.ChatResponseMessage;
import com.azure.core.credential.AzureKeyCredential;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to get chat completions for the provided chat messages.
 * Completions support a wide variety of tasks and generate text that continues from or "completes" provided
 * prompt data.
 */
public class GetChatCompletionsStreamAsyncSample {

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

        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant. You will talk like a pirate."));
        chatMessages.add(new ChatRequestUserMessage("Can you help me?"));
        chatMessages.add(new ChatRequestAssistantMessage("Of course, me hearty! What can I do for ye?"));
        chatMessages.add(new ChatRequestUserMessage("What's the best way to train a parrot?"));

        client.getChatCompletionsStream(deploymentOrModelId, new ChatCompletionsOptions(chatMessages))
            // Remove .skip(1) when using Non-Azure OpenAI API
            // Note: the first chat completions can be ignored when using Azure OpenAI service which is a known service bug.
            // TODO: remove .skip(1) when service fix the issue.
            .skip(1)
            .map(chatCompletions -> {
                /* The delta is the message content for a streaming response.
                 * Subsequence of streaming delta will be like:
                 * "delta": {
                 *     "role": "assistant"
                 * },
                 * "delta": {
                 *     "content": "Why"
                 * },
                 * "delta": {
                 *     "content": " don"
                 * },
                 * "delta": {
                 *     "content": "'t"
                 * }
                 */
                ChatResponseMessage delta = chatCompletions.getChoices().get(0).getDelta();

                if (delta.getRole() != null) {
                    System.out.println("Role = " + delta.getRole());
                }
                return delta.getContent() == null ? "" : delta.getContent();
            })
            .subscribe(
                System.out::print,
                error -> System.err.println("There was an error getting chat completions." + error),
                () -> System.out.println("Completed called getChatCompletions."));


        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        TimeUnit.SECONDS.sleep(10);
    }
}
