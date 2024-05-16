// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.usage;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.EmbeddingItem;
import com.azure.ai.openai.models.Embeddings;
import com.azure.ai.openai.models.EmbeddingsOptions;
import com.azure.ai.openai.models.EmbeddingsUsage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;

import java.util.Arrays;

/**
 * Sample demonstrates how to get the embeddings for a given prompt.
 */
public class GetEmbeddingsSample {
    /**
     * Runs the sample algorithm and demonstrates how to get the embeddings for a given prompt.
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

        EmbeddingsOptions embeddingsOptions = new EmbeddingsOptions(Arrays.asList("Your text string goes here"));

        Embeddings embeddings = client.getEmbeddings(deploymentOrModelId, embeddingsOptions);

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
    }
}
