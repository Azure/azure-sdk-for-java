// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.impl;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestAssistantMessage;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.ChatResponseMessage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Code snippets for {@link OpenAIAsyncClient}
 */
public class OpenAIAsyncClientJavaDocCodeSnippets {
    private OpenAIAsyncClient openAIAsyncClient = getOpenAIAsyncClient();

    /**
     * Code snippets for {@link OpenAIClient#getChatCompletionsStream(String, ChatCompletionsOptions)}
     */
    @Test
    public void getChatCompletionsStream() {
        // BEGIN: com.azure.ai.openai.OpenAIAsyncClient.getChatCompletionsStream#String-ChatCompletionsOptions
        String deploymentOrModelId = "gpt-4-1106-preview";
        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant. You will talk like a pirate."));
        chatMessages.add(new ChatRequestUserMessage("Can you help me?"));
        chatMessages.add(new ChatRequestAssistantMessage("Of course, me hearty! What can I do for ye?"));
        chatMessages.add(new ChatRequestUserMessage("What's the best way to train a parrot?"));

        AtomicInteger tokenCount = new AtomicInteger();
        openAIAsyncClient
                .getChatCompletionsStream(deploymentOrModelId, new ChatCompletionsOptions(chatMessages))
                .toStream()
                // Remove .skip(1) when using Non-Azure OpenAI API
                // Note: the first chat completions can be ignored when using Azure OpenAI service which is a known service bug.
                // TODO: remove .skip(1) after service fixes the issue.
                .skip(1)
                .forEach(chatCompletions -> {
                    ChatResponseMessage delta = chatCompletions.getChoices().get(0).getDelta();
                    if (delta.getRole() != null) {
                        System.out.println("Role = " + delta.getRole());
                    }
                    if (delta.getContent() != null) {
                        String content = delta.getContent();
                        System.out.print(content);

                        // Token Computation:
                        // Compute the token count for the given input.
                        // For cl100k_base and p50k_base encodings, use 'jtokkit' library to compute the token count.
                        // https://github.com/knuddelsgmbh/jtokkit
                        // For r50k_base (gpt2) encodings, use 'gpt2-tokenizer-java' library to compute the token count.
                        // https://github.com/hyunwoongko/gpt2-tokenizer-java
                        // We use 'jtokkit' library to compute the token count for this sample.
                        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
                        Encoding enc = registry.getEncoding(EncodingType.CL100K_BASE);
                        tokenCount.addAndGet(enc.countTokens(content));
                    }
                });
        // Use https://platform.openai.com/tokenizer to verify the token count
        System.out.println("\nTotal token count: " + tokenCount.get());
        // END: com.azure.ai.openai.OpenAIAsyncClient.getChatCompletionsStream#String-ChatCompletionsOptions
    }

    private OpenAIAsyncClient getOpenAIAsyncClient() {
        return new OpenAIClientBuilder()
                .endpoint(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT"))
                .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY")))
                .buildAsyncClient();
    }
}
