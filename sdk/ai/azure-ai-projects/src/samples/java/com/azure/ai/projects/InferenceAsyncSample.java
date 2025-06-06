// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.inference.ChatCompletionsAsyncClient;
import com.azure.ai.inference.ChatCompletionsClientBuilder;
import com.azure.ai.inference.EmbeddingsAsyncClient;
import com.azure.ai.inference.EmbeddingsClientBuilder;
import com.azure.ai.inference.ImageEmbeddingsAsyncClient;
import com.azure.ai.inference.ImageEmbeddingsClientBuilder;
import com.azure.ai.inference.models.ChatCompletionsOptions;
import com.azure.ai.inference.models.ChatRequestUserMessage;
import com.azure.ai.inference.models.EmbeddingItem;
import com.azure.ai.inference.models.ImageEmbeddingInput;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InferenceAsyncSample {

    private static ChatCompletionsAsyncClient chatCompletionsAsyncClient
        = new ChatCompletionsClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildAsyncClient();

    private static ImageEmbeddingsAsyncClient imageEmbeddingsAsyncClient
        = new ImageEmbeddingsClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildAsyncClient();

    private static EmbeddingsAsyncClient embeddingsAsyncClient
        = new EmbeddingsClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildAsyncClient();

    public static void main(String[] args) {

    }

    public static Mono<Void> embeddingsClientSample() {
        // BEGIN: com.azure.ai.projects.InferenceAsyncSample.embeddingsClientSample

        List<String> promptList = new ArrayList<>();
        String prompt = "Tell me 3 jokes about trains";
        promptList.add(prompt);

        return embeddingsAsyncClient.embed(promptList)
            .flatMap(embeddings -> {
                for (EmbeddingItem item : embeddings.getData()) {
                    System.out.printf("Index: %d.%n", item.getIndex());
                    for (Float embedding : item.getEmbeddingList()) {
                        System.out.printf("%f;", embedding);
                    }
                    System.out.println();
                }
                return Mono.empty();
            });

        // END: com.azure.ai.projects.InferenceAsyncSample.embeddingsClientSample
    }

    public static Mono<Void> chatCompletionsClientSample() {
        // BEGIN: com.azure.ai.projects.InferenceAsyncSample.chatCompletionsClientSample

        ChatCompletionsOptions options = new ChatCompletionsOptions(Arrays.asList(
            new ChatRequestUserMessage("How many feet are in a mile?")
        ));

        return chatCompletionsAsyncClient.complete(options)
            .flatMap(chatCompletions -> {
                System.out.println(chatCompletions.getChoice().getMessage().getContent());
                return Mono.empty();
            });

        // END: com.azure.ai.projects.InferenceAsyncSample.chatCompletionsClientSample
    }

    public static Mono<Void> imageEmbeddingsClientSample(String imageUrl) {
        // BEGIN: com.azure.ai.projects.InferenceAsyncSample.imageEmbeddingsClientSample

        return Mono.fromCallable(() -> SampleUtils.getPath(imageUrl))
            .flatMap(imagePath -> 
                imageEmbeddingsAsyncClient.embed(Arrays.asList(new ImageEmbeddingInput(imagePath, "png")))
            )
            .flatMap(embeddings -> {
                for (EmbeddingItem item : embeddings.getData()) {
                    System.out.printf("Index: %d.%n", item.getIndex());
                    for (Float embedding : item.getEmbeddingList()) {
                        System.out.printf("%f;", embedding);
                    }
                    System.out.println();
                }
                return Mono.empty();
            });

        // END: com.azure.ai.projects.InferenceAsyncSample.imageEmbeddingsClientSample
    }
}
