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
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Code snippets for {@link OpenAIAsyncClient}
 */
public class OpenAIAsyncClientJavaDocCodeSnippets {
    private OpenAIAsyncClient openAIAsyncClient = getOpenAIAsyncClient();

    /**
     * Code snippets for {@link OpenAIClient#getChatCompletionsStream(String, ChatCompletionsOptions)}
     */
    @Test
    public void getChatCompletionsStream() throws InterruptedException {
        String deploymentOrModelId = Configuration.getGlobalConfiguration().get("OPENAI_DEPLOYMENT_OR_MODEL_ID");
        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant. You will talk like a pirate."));
        chatMessages.add(new ChatRequestUserMessage("Can you help me?"));
        chatMessages.add(new ChatRequestAssistantMessage("Of course, me hearty! What can I do for ye?"));
        chatMessages.add(new ChatRequestUserMessage("What's the best way to train a parrot?"));

        // BEGIN: com.azure.ai.openai.OpenAIAsyncClient.getChatCompletionsStream#String-ChatCompletionsOptions
        openAIAsyncClient
                .getChatCompletionsStream(deploymentOrModelId, new ChatCompletionsOptions(chatMessages))
                .subscribe(
                        chatCompletions -> System.out.print(chatCompletions.getId()),
                        error -> System.err.println("There was an error getting chat completions." + error),
                        () -> System.out.println("Completed called getChatCompletionsStream."));
        // END: com.azure.ai.openai.OpenAIAsyncClient.getChatCompletionsStream#String-ChatCompletionsOptions

        // With Response Code Snippet

        // BEGIN: com.azure.ai.openai.OpenAIAsyncClient.getChatCompletionsStream#String-ChatCompletionsOptionsMaxOverload
        openAIAsyncClient.getChatCompletionsStreamWithResponse(deploymentOrModelId, new ChatCompletionsOptions(chatMessages),
                        new RequestOptions().setHeader("my-header", "my-header-value"))
                .subscribe(
                        response -> System.out.print(response.getValue().getId()),
                        error -> System.err.println("There was an error getting chat completions." + error),
                        () -> System.out.println("Completed called getChatCompletionsStreamWithResponse."));
        // END: com.azure.ai.openai.OpenAIAsyncClient.getChatCompletionsStream#String-ChatCompletionsOptionsMaxOverload

        TimeUnit.SECONDS.sleep(10);
    }

    private OpenAIAsyncClient getOpenAIAsyncClient() {
        return new OpenAIClientBuilder()
                .endpoint(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT"))
                .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY")))
                .buildAsyncClient();
    }
}
