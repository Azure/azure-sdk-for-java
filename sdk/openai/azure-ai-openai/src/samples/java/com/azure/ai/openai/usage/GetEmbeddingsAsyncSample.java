// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.usage;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.EmbeddingItem;
import com.azure.ai.openai.models.EmbeddingsOptions;
import com.azure.ai.openai.models.EmbeddingsUsage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to get the embeddings for a given prompt.
 */
public class GetEmbeddingsAsyncSample {
    /**
     * Runs the sample algorithm and demonstrates how to get the embeddings for a given prompt.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) throws InterruptedException {
        String azureOpenaiKey = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY");
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");
        String deploymentOrModelId = "{azure-open-ai-deployment-model-id}";

        OpenAIAsyncClient client = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(azureOpenaiKey))
            .buildAsyncClient();

        EmbeddingsOptions embeddingsOptions = new EmbeddingsOptions(Arrays.asList("Your text string goes here"));
        client.getEmbeddings(deploymentOrModelId, embeddingsOptions).subscribe(
            embeddings -> {
                for (EmbeddingItem item : embeddings.getData()) {
                    System.out.printf("Index: %d.%n", item.getPromptIndex());
                    System.out.println("Embedding as base64 encoded string: " +  item.getEmbeddingAsString());
                    System.out.println("Embedding as list of floats: ");
                    for (Float embedding : item.getEmbedding()) {
                        System.out.printf("%f;", embedding);
                    }
                }
                EmbeddingsUsage usage = embeddings.getUsage();
                System.out.printf(
                    "Usage: number of prompt token is %d and number of total tokens in request and response is %d.%n",
                    usage.getPromptTokens(), usage.getTotalTokens());
            },
            error -> System.err.println("There was an error getting embeddings." + error),
            () -> System.out.println("Completed called getEmbeddings."));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        TimeUnit.SECONDS.sleep(10);
    }
}
