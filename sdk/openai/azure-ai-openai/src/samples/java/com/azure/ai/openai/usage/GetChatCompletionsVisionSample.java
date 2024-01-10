// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.usage;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatCompletions;
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

/**
 * OpenAI only. This sample demonstrates how to get chat completions using images as part of the prompt in a sync scenario.
 */
public class GetChatCompletionsVisionSample {
    /**
     * Demo showcasing the usage of images as part of the prompt in a sync scenario.
     *
     * @param args – Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String openAIKey = "{openai-secret-key}";
        String modelId = "{openai-model-id}";

        OpenAIClient client = new OpenAIClientBuilder()
                .credential(new KeyCredential(openAIKey))
                .buildClient();

        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant that describes images"));
        chatMessages.add(new ChatRequestUserMessage(Arrays.asList(
                new ChatMessageTextContentItem("Please describe this image"),
                new ChatMessageImageContentItem(
                        new ChatMessageImageUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Microsoft_logo.svg/512px-Microsoft_logo.svg.png"))
        )));

        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatMessages);
        chatCompletionsOptions.setMaxTokens(2048);
        ChatCompletions chatCompletions = client.getChatCompletions(modelId, chatCompletionsOptions);

        System.out.println("Chat completion: " + chatCompletions.getChoices().get(0).getMessage().getContent());
    }
}
