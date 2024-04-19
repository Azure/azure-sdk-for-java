// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestAssistantMessage;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.ChatResponseMessage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
        String azureOpenaiKey = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY");
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");
        String deploymentOrModelId = "{azure-open-ai-deployment-model-id}";

        OpenAIClient client = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(azureOpenaiKey))
            .buildClient();

        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant. You will talk like a pirate."));
        chatMessages.add(new ChatRequestUserMessage("Can you help me?"));
        chatMessages.add(new ChatRequestAssistantMessage("Of course, me hearty! What can I do for ye?"));
        chatMessages.add(new ChatRequestUserMessage("What's the best way to train a parrot?"));

        IterableStream<ChatCompletions> chatCompletionsStream = client.getChatCompletionsStream(deploymentOrModelId,
            new ChatCompletionsOptions(chatMessages));

        AtomicInteger tokenCount = new AtomicInteger();

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
                        tokenCount.addAndGet(computeToken(content));
                    }
                });

        // Use https://platform.openai.com/tokenizer to verify the token count
        System.out.println("\nTotal token count: " + tokenCount.get());
    }

    // Compute the token count for the given input.
    // For cl100k_base and p50k_base encodings, use 'jtokkit' library to compute the token count. https://github.com/knuddelsgmbh/jtokkit
    // For r50k_base (gpt2) encodings, use 'gpt2-tokenizer-java' library to compute the token count. https://github.com/hyunwoongko/gpt2-tokenizer-java
    // We use 'jtokkit' library to compute the token count for this sample.
    private static int computeToken(String input) {
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        Encoding enc = registry.getEncoding(EncodingType.CL100K_BASE);
        // Or get the tokenizer corresponding to a specific OpenAI model
        // enc = registry.getEncodingForModel(ModelType.GPT_4);
        return enc.countTokens(input);
    }
}
