// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.usage;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ImageGenerationOptions;
import com.azure.ai.openai.models.ImageLocation;
import com.azure.ai.openai.models.ImageResponse;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.ResponseError;

/**
 * Sample demonstrates how to get the images for a given prompt.
 */
public class GetImagesSample {
    /**
     * Runs the sample algorithm and demonstrates how to get the images for a given prompt.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String azureOpenaiKey = "{azure-open-ai-key}";
        String endpoint = "{azure-open-ai-endpoint}";

        OpenAIClient client = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(azureOpenaiKey))
            .buildClient();

        ImageGenerationOptions imageGenerationOptions = new ImageGenerationOptions(
            "A drawing of the Seattle skyline in the style of Van Gogh");
        ImageResponse images = client.getImages(imageGenerationOptions);

        for (ImageLocation imageLocation : images.getData()) {
            ResponseError error = imageLocation.getError();
            if (error != null) {
                System.out.printf("Image generation operation failed. Error code: %s, error message: %s.%n",
                    error.getCode(), error.getMessage());
            } else {
                System.out.printf(
                    "Image location URL that provides temporary access to download the generated image is %s.%n",
                    imageLocation.getUrl());
            }
        }
    }
}
