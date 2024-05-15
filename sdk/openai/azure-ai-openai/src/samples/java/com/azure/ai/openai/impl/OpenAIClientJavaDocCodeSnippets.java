// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.impl;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestAssistantMessage;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.ChatResponseMessage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Code snippets for {@link OpenAIClient}
 */
public class OpenAIClientJavaDocCodeSnippets {

    private OpenAIClient openAIClient = getOpenAIClient();

    /**
     * Code snippets for {@link OpenAIClient#getChatCompletionsStream(String, ChatCompletionsOptions)}
     */
    @Test
    public void getChatCompletionsStream() {
        String deploymentOrModelId = Configuration.getGlobalConfiguration().get("OPENAI_DEPLOYMENT_OR_MODEL_ID");
        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant. You will talk like a pirate."));
        chatMessages.add(new ChatRequestUserMessage("Can you help me?"));
        chatMessages.add(new ChatRequestAssistantMessage("Of course, me hearty! What can I do for ye?"));
        chatMessages.add(new ChatRequestUserMessage("What's the best way to train a parrot?"));

        // BEGIN: com.azure.ai.openai.OpenAIClient.getChatCompletionsStream#String-ChatCompletionsOptions
        openAIClient.getChatCompletionsStream(deploymentOrModelId, new ChatCompletionsOptions(chatMessages))
                .forEach(chatCompletions -> {
                    if (CoreUtils.isNullOrEmpty(chatCompletions.getChoices())) {
                        return;
                    }
                    ChatResponseMessage delta = chatCompletions.getChoices().get(0).getDelta();
                    if (delta.getRole() != null) {
                        System.out.println("Role = " + delta.getRole());
                    }
                    if (delta.getContent() != null) {
                        String content = delta.getContent();
                        System.out.print(content);
                    }
                });
        // END: com.azure.ai.openai.OpenAIClient.getChatCompletionsStream#String-ChatCompletionsOptions

        // With Response Code Snippet

        // BEGIN: com.azure.ai.openai.OpenAIClient.getChatCompletionsStream#String-ChatCompletionsOptionsMaxOverload
        openAIClient.getChatCompletionsStreamWithResponse(deploymentOrModelId, new ChatCompletionsOptions(chatMessages),
                        new RequestOptions().setHeader("my-header", "my-header-value"))
                .getValue()
                .forEach(chatCompletions -> {
                    if (CoreUtils.isNullOrEmpty(chatCompletions.getChoices())) {
                        return;
                    }
                    ChatResponseMessage delta = chatCompletions.getChoices().get(0).getDelta();
                    if (delta.getRole() != null) {
                        System.out.println("Role = " + delta.getRole());
                    }
                    if (delta.getContent() != null) {
                        String content = delta.getContent();
                        System.out.print(content);
                    }
                });
        // END: com.azure.ai.openai.OpenAIClient.getChatCompletionsStream#String-ChatCompletionsOptionsMaxOverload
    }

    private OpenAIClient getOpenAIClient() {
        return new OpenAIClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT"))
            .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY")))
            .buildClient();
    }
}
