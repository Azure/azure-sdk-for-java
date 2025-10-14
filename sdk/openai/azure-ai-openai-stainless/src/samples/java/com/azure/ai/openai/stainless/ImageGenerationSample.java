// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.stainless;

import com.azure.identity.AuthenticationUtil;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.credential.BearerTokenCredential;
import com.openai.models.images.Image;
import com.openai.models.images.ImageGenerateParams;
import com.openai.models.images.ImageModel;

import java.util.List;
import java.util.Optional;

public class ImageGenerationSample {

    public static void main(String[] args) {
        OpenAIOkHttpClient.Builder clientBuilder = OpenAIOkHttpClient.builder();

        /* Azure-specific code starts here */
        // You can either set 'endpoint' or 'apiKey' directly in the builder.
        // or set same two env vars and use fromEnv() method instead
        clientBuilder
            .baseUrl(System.getenv("AZURE_OPENAI_ENDPOINT"))
            .credential(BearerTokenCredential
                .create(
                    AuthenticationUtil.getBearerTokenSupplier(new DefaultAzureCredentialBuilder().build(),
                        "https://cognitiveservices.azure.com/.default")));
        /* Azure-specific code ends here */

        // All code from this line down is general-purpose OpenAI code
        OpenAIClient client = clientBuilder.build();

        // Example usage of the client to generate images
        String prompt = "Golden Retriever dog smiling when running on flower field";
        int numberOfImages = 1;

        ImageGenerateParams params = ImageGenerateParams.builder()
            .prompt(prompt)
            .model(ImageModel.DALL_E_3)
            .n(numberOfImages)
            .quality(ImageGenerateParams.Quality.HD)
            .build();

        // Call the image generation API with the specified parameters
        Optional<List<Image>> images = client.images().generate(params).data();
        images.ifPresent(list -> list.forEach(image -> System.out.println("Generated Image URL: " + image.url())));
    }
}
