// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.NonAzureOpenAIKeyCredential;

/**
 * Sample demonstrates how to create a client with public Non-Azure API Key.
 */
public class ClientCreationWithNonAzureOpenAIKeyCredentialSample {
    /**
     * Runs the sample algorithm and demonstrates how to create a client with public Non-Azure API Key.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String apiKey = "{non-azure-open-ai-api-key}";

        OpenAIClientBuilder builder = new OpenAIClientBuilder()
            .credential(new NonAzureOpenAIKeyCredential(apiKey));

        OpenAIClient client = builder.buildClient();
        OpenAIAsyncClient asyncClient = builder.buildAsyncClient();
    }
}
