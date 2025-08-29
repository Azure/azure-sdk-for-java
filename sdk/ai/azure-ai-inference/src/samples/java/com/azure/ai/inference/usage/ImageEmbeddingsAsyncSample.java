// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.inference.usage;

import com.azure.ai.inference.ImageEmbeddingsAsyncClient;
import com.azure.ai.inference.ImageEmbeddingsClientBuilder;
import com.azure.ai.inference.models.EmbeddingItem;
import com.azure.ai.inference.models.EmbeddingsUsage;
import com.azure.ai.inference.models.ImageEmbeddingInput;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class ImageEmbeddingsAsyncSample {
    private static final String TEST_IMAGE_PATH = "./src/samples/resources/sample-images/sample.png";
    private static final String TEST_IMAGE_FORMAT = "png";
     /**
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) throws InterruptedException {
        String key = Configuration.getGlobalConfiguration().get("AZURE_IMAGE_EMBEDDINGS_KEY");
        String endpoint = Configuration.getGlobalConfiguration().get("IMAGE_EMBEDDINGS_MODEL_ENDPOINT");
        ImageEmbeddingsAsyncClient client = new ImageEmbeddingsClientBuilder()
            .credential(new AzureKeyCredential(key))
            .endpoint(endpoint)
            .buildAsyncClient();

        List<ImageEmbeddingInput> inputList = new ArrayList<>();
        Path testFilePath = Paths.get(TEST_IMAGE_PATH);
        inputList.add(new ImageEmbeddingInput(testFilePath, TEST_IMAGE_FORMAT));

        client.embed(inputList).subscribe(
            embeddings -> {
                for (EmbeddingItem item : embeddings.getData()) {
                    System.out.printf("Index: %d.%n", item.getIndex());
                    System.out.println("Embedding as list of floats: ");
                    for (Float embedding : item.getEmbeddingList()) {
                        System.out.printf("%f;", embedding);
                    }
                }
                EmbeddingsUsage usage = embeddings.getUsage();
                System.out.println("");
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
