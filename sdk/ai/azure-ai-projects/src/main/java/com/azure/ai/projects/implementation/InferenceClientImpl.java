// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.implementation;

import com.openai.client.OpenAIClient;
import com.openai.client.OpenAIClientAsync;
import com.openai.client.okhttp.OpenAIOkHttpClient;

/**
 * Initializes a new instance of the InferenceClient interface for inference operations.
 */
public class InferenceClientImpl {

    private final OpenAIOkHttpClient.Builder openAIOkHttpClientBuilder;

    /**
     * Initializes an instance of InferenceClientImpl.
     *
     * @param openAIOkHttpClientBuilder the OpenAI OkHttp client builder.
     */
    public InferenceClientImpl(OpenAIOkHttpClient.Builder openAIOkHttpClientBuilder) {
        this.openAIOkHttpClientBuilder = openAIOkHttpClientBuilder;
    }

    /**
     * Gets an instance of OpenAIClient.
     *
     * @return an instance of OpenAIClient.
     */
    public OpenAIClient getOpenAIClient() {
        return openAIOkHttpClientBuilder.build();
    }

    /**
     * Gets an instance of OpenAIAsyncClient.
     *
     * @return an instance of OpenAIAsyncClient.
     */
    public OpenAIClientAsync getOpenAIAsyncClient() {
        return openAIOkHttpClientBuilder.build().async();
    }
}
