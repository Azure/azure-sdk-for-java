package com.azure.ai.openai;

import com.openai.client.OpenAIClientAsync;

public class AzureAsyncClientCompanion {

    private final OpenAIClientAsync client;

    public AzureAsyncClientCompanion(OpenAIClientAsync client) {
        this.client = client;
    }
}
