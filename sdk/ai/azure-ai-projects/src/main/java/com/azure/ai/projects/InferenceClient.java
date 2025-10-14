// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.ai.projects.implementation.InferenceClientImpl;
import com.openai.client.OpenAIClient;

/**
 * Client for interacting with the inference services.
 */
public final class InferenceClient {
    private final InferenceClientImpl inferenceClientImpl;

    /**
     * Initializes an instance of InferenceClient.
     *
     * @param inferenceClientImpl The implementation client.
     */
    public InferenceClient(InferenceClientImpl inferenceClientImpl) {
        this.inferenceClientImpl = inferenceClientImpl;
    }

    /**
     * Gets an instance of OpenAIClient for the specified deployment.
     *
     * @return An instance of OpenAIClient.
     */
    public OpenAIClient getOpenAIClient() {
        return inferenceClientImpl.getOpenAIClient();
    }
}
