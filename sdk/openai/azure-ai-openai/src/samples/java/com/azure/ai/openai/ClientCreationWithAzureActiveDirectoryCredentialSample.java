// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Sample demonstrates how to create a client with `AzureKeyCredential`.
 */
public class ClientCreationWithAzureActiveDirectoryCredentialSample {
    /**
     * Runs the sample algorithm and demonstrates how to create a client with `AzureKeyCredential`.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String endpoint = "{azure-open-ai-endpoint}";

        OpenAIClientBuilder builder = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build());

        OpenAIClient client = builder.buildClient();
        OpenAIAsyncClient asyncClient = builder.buildAsyncClient();
    }
}
