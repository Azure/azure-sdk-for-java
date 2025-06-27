// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.stainless;

import com.azure.identity.AuthenticationUtil;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.azure.AzureOpenAIServiceVersion;
import com.openai.client.OpenAIClientAsync;
import com.openai.client.okhttp.OpenAIOkHttpClientAsync;
import com.openai.credential.BearerTokenCredential;
import com.openai.models.ChatModel;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseOutputMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class ResponsesConversationAsyncSample {
    private ResponsesConversationAsyncSample() {}

    public static void main(String[] args) {
        // Configures using one of:
        // - The `OPENAI_API_KEY` environment variable
        // - The `AZURE_OPENAI_ENDPOINT` and `AZURE_OPENAI_KEY` environment variables
        OpenAIClientAsync client = OpenAIOkHttpClientAsync.builder()
                .azureServiceVersion(AzureOpenAIServiceVersion.latestPreviewVersion())
                // Gets the API key from the `AZURE_OPENAI_KEY` environment variable
                .baseUrl(System.getenv("AZURE_OPENAI_ENDPOINT"))
                // Set the Azure Entra ID
                .credential(BearerTokenCredential.create(AuthenticationUtil.getBearerTokenSupplier(
                        new DefaultAzureCredentialBuilder().build(), "https://cognitiveservices.azure.com/.default")))
                .build();

        List<ResponseInputItem> inputItems = new ArrayList<>();
        inputItems.add(ResponseInputItem.ofEasyInputMessage(EasyInputMessage.builder()
                .role(EasyInputMessage.Role.USER)
                .content("Tell me a story about building the best SDK!")
                .build()));

        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        for (int i = 0; i < 4; i++) {
            final int iteration = i;
            future = future.thenCompose(ignored -> {
                ResponseCreateParams params = ResponseCreateParams.builder()
                        .inputOfResponse(inputItems)
                        .model(ChatModel.GPT_4O_MINI)
                        .build();

                return client.responses().create(params).thenAccept(responseItems -> {
                    List<ResponseOutputMessage> messages = new ArrayList<>();
                    responseItems.output().forEach(item -> item.message().ifPresent(messages::add));

                    messages.stream()
                            .flatMap(m -> m.content().stream())
                            .forEach(content -> content.outputText().ifPresent(
                                    outputText -> System.out.println(outputText.text())));

                    System.out.println("\n-----------------------------------\n");

                    messages.forEach(msg -> inputItems.add(ResponseInputItem.ofResponseOutputMessage(msg)));
                    inputItems.add(ResponseInputItem.ofEasyInputMessage(EasyInputMessage.builder()
                            .role(EasyInputMessage.Role.USER)
                            .content("But why?" + "?".repeat(iteration))
                            .build()));
                });
            });
        }

        future.join();
    }
}
