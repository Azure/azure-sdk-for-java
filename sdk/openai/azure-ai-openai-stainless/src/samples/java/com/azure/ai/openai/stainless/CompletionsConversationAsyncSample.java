// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.stainless;

import static java.util.stream.Collectors.toList;

import com.azure.identity.AuthenticationUtil;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClientAsync;
import com.openai.client.okhttp.OpenAIOkHttpClientAsync;
import com.openai.credential.BearerTokenCredential;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessage;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class CompletionsConversationAsyncSample {
    private CompletionsConversationAsyncSample() {}

    public static void main(String[] args) {
        // Configures using one of:
        // - The `OPENAI_API_KEY` environment variable
        // - The `AZURE_OPENAI_ENDPOINT` and `AZURE_OPENAI_KEY` environment variables
        OpenAIOkHttpClientAsync.Builder clientBuilder = OpenAIOkHttpClientAsync.builder();

        /* Azure-specific code starts here */
        // You can either set 'endpoint' or 'apiKey' directly in the builder.
        // or set same two env vars and use fromEnv() method instead
        clientBuilder
            .baseUrl(System.getenv("AZURE_OPENAI_ENDPOINT"))
            .credential(BearerTokenCredential.create(AuthenticationUtil.getBearerTokenSupplier(
                new DefaultAzureCredentialBuilder().build(), "https://cognitiveservices.azure.com/.default")));
        /* Azure-specific code ends here */

        // All code from this line down is general-purpose OpenAI code
        OpenAIClientAsync client = clientBuilder.build();


        // Use a builder so that we can append more messages to it below.
        // Each time we call .build()` we get an immutable object that's unaffected by future mutations of the builder.
        ChatCompletionCreateParams.Builder createParamsBuilder = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O)
                .maxCompletionTokens(2048)
                .addDeveloperMessage("Make sure you mention Stainless!")
                .addUserMessage("Tell me a story about building the best SDK!");

        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        for (int i = 0; i < 4; i++) {
            final int index = i;
            future = future.thenComposeAsync(
                            unused -> client.chat().completions().create(createParamsBuilder.build()))
                    .thenAccept(completion -> {
                        List<ChatCompletionMessage> messages = completion.choices().stream()
                                .map(ChatCompletion.Choice::message)
                                .collect(toList());

                        messages.forEach(message -> message.content().ifPresent(System.out::println));

                        System.out.println("\n-----------------------------------\n");

                        messages.forEach(createParamsBuilder::addMessage);
                        createParamsBuilder
                            .addDeveloperMessage("Be as snarky as possible when replying!" + new String(new char[index]).replace("\0", "!"))
                            .addUserMessage("But why?" + new String(new char[index]).replace("\0", "?"));
                    });
        }

        future.join();
    }
}
