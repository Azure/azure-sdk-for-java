// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.core.credential.AzureKeyCredential;

/**
 * Sample demonstrates how to create a client with `AzureKeyCredential`.
 */
public class ClientCreationWithAzureKeyCredentialSample {
    /**
     * Runs the sample algorithm and demonstrates how to create a client with `AzureKeyCredential`.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String endpoint = "{azure-open-ai-endpoint}";
        String azureOpenaiKey = "{azure-open-ai-key}";

        OpenAIClientBuilder builder = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(azureOpenaiKey));

        OpenAIClient client = builder.buildClient();
        OpenAIAsyncClient asyncClient = builder.buildAsyncClient();
    }
}
