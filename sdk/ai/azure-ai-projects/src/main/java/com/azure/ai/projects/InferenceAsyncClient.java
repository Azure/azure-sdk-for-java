// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.ai.projects.implementation.InferenceClientImpl;
import com.openai.client.OpenAIClientAsync;

/**
 * Async client for interacting with the inference services.
 */
public final class InferenceAsyncClient {
    private final InferenceClientImpl inferenceClientImpl;

    /**
     * Initializes an instance of InferenceAsyncClient.
     *
     * @param inferenceClientImpl The implementation client.
     */
    public InferenceAsyncClient(InferenceClientImpl inferenceClientImpl) {
        this.inferenceClientImpl = inferenceClientImpl;
    }

    /**
     * Gets an instance of OpenAIAsyncClient for the specified deployment.
     *
     * @return An instance of OpenAIAsyncClient.
     */
    public OpenAIClientAsync getOpenAIAsyncClient() {
        return inferenceClientImpl.getOpenAIAsyncClient();
    }
}
