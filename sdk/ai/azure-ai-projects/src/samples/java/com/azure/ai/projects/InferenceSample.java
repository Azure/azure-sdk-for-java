// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.inference.ChatCompletionsClient;
import com.azure.ai.inference.EmbeddingsClient;
import com.azure.ai.inference.ImageEmbeddingsClient;
import com.azure.ai.inference.models.ChatCompletions;
import com.azure.ai.inference.models.ChatCompletionsOptions;
import com.azure.ai.inference.models.ChatRequestUserMessage;
import com.azure.ai.inference.models.EmbeddingItem;
import com.azure.ai.inference.models.EmbeddingsResult;
import com.azure.ai.inference.models.ImageEmbeddingInput;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InferenceSample {

    private static AIProjectClientBuilder projectClientBuilder
        = new AIProjectClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
        .credential(new DefaultAzureCredentialBuilder().build());

    private static ChatCompletionsClient chatCompletionsClient = projectClientBuilder.buildChatCompletionsClient();
    private static ImageEmbeddingsClient imageEmbeddingsClient = projectClientBuilder.buildImageEmbeddingsClient();
    private static EmbeddingsClient embeddingsClient = projectClientBuilder.buildEmbeddingsClient();

    public static void main(String[] args) {
        embeddingsClientSample();
    }

    public static void embeddingsClientSample() {
        // BEGIN: com.azure.ai.projects.InferenceSample.embeddingsClientSample

        List<String> promptList = new ArrayList<>();
        String prompt = "Tell me 3 jokes about trains";
        promptList.add(prompt);

        EmbeddingsResult embeddings = embeddingsClient.embed(promptList);

        for (EmbeddingItem item : embeddings.getData()) {
            System.out.printf("Index: %d.%n", item.getIndex());
            for (Float embedding : item.getEmbeddingList()) {
                System.out.printf("%f;", embedding);
            }
        }

        // END: com.azure.ai.projects.InferenceSample.embeddingsClientSample
    }

    public static void chatCompletionsClientSample() {
        // BEGIN: com.azure.ai.projects.InferenceSample.chatCompletionsClientSample

        ChatCompletionsOptions options = new ChatCompletionsOptions(Arrays.asList(
            new ChatRequestUserMessage("How many feet are in a mile?")
        ));

        ChatCompletions chatCompletions = chatCompletionsClient.complete(options);
        System.out.println(chatCompletions.getChoice().getMessage().getContent());

        // END: com.azure.ai.projects.InferenceSample.chatCompletionsClientSample
    }

    public static void imageEmbeddingsClientSample() throws FileNotFoundException, URISyntaxException {
        // BEGIN: com.azure.ai.projects.InferenceSample.imageEmbeddingsClientSample

        String imageUrl = "sample.png";
        Path imagePath = SampleUtils.getPath(imageUrl);

        EmbeddingsResult embeddings = imageEmbeddingsClient
            .embed(Arrays.asList(new ImageEmbeddingInput(imagePath, "png")));

        for (EmbeddingItem item : embeddings.getData()) {
            System.out.printf("Index: %d.%n", item.getIndex());
            for (Float embedding : item.getEmbeddingList()) {
                System.out.printf("%f;", embedding);
            }
        }

        // END: com.azure.ai.projects.InferenceSample.imageEmbeddingsClientSample
    }
}
