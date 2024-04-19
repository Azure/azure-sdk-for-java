// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.usage;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ImageGenerationData;
import com.azure.ai.openai.models.ImageGenerationOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to get the images for a given prompt.
 */
public class GetImagesAsyncSample {

    /**
     * Runs the sample algorithm and demonstrates how to get the images for a given prompt.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) throws InterruptedException {
        String azureOpenaiKey = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY");
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");
        String deploymentOrModelName = "{image-generation-deployment-or-model-name}";

        OpenAIAsyncClient client = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(azureOpenaiKey))
            .buildAsyncClient();

        ImageGenerationOptions imageGenerationOptions = new ImageGenerationOptions(
            "A drawing of the Seattle skyline in the style of Van Gogh");
        client.getImageGenerations(deploymentOrModelName, imageGenerationOptions).subscribe(
            images -> {
                for (ImageGenerationData imageGenerationData : images.getData()) {
                    System.out.printf(
                        "Image location URL that provides temporary access to download the generated image is %s.%n",
                        imageGenerationData.getUrl());
                }
            },
            error -> System.err.println("There was an error getting images." + error),
            () -> System.out.println("Completed getImages."));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        TimeUnit.SECONDS.sleep(10);
    }
}
