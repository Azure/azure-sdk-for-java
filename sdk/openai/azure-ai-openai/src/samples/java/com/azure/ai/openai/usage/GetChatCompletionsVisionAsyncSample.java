// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.usage;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatMessageImageContentItem;
import com.azure.ai.openai.models.ChatMessageImageUrl;
import com.azure.ai.openai.models.ChatMessageTextContentItem;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.core.credential.KeyCredential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * OpenAI only. This sample demonstrates how to get chat completions using images as part of the prompt in an async scenario.
 */
public class GetChatCompletionsVisionAsyncSample {
    /**
     * Demo showcasing the usage of images as part of the prompt in an async scenario.
     *
     * @param args – Unused. Arguments to the program.
     */
    public static void main(String[] args) throws InterruptedException {
        String openAIKey = "{openai-secret-key}";
        String modelId = "{openai-model-id}";

        OpenAIAsyncClient client = new OpenAIClientBuilder()
                .credential(new KeyCredential(openAIKey))
                .buildAsyncClient();

        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant that describes images"));
        chatMessages.add(new ChatRequestUserMessage(Arrays.asList(
                new ChatMessageTextContentItem("Please describe this image"),
                new ChatMessageImageContentItem(
                        new ChatMessageImageUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Microsoft_logo.svg/512px-Microsoft_logo.svg.png"))
        )));

        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatMessages);
        chatCompletionsOptions.setMaxTokens(2048);
        client.getChatCompletions(modelId, chatCompletionsOptions)
                .subscribe(chatCompletions -> System.out.println("Chat completion: " + chatCompletions.getChoices().get(0).getMessage().getContent()),
                    error -> System.err.println("There was an error getting chat completions." + error),
                    () -> System.out.println("Completed called getChatCompletions."));

        TimeUnit.SECONDS.sleep(10);
    }
}
