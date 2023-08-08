// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.models;

import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

/**
 * Non-Azure OpenAI API keys credential class for accepting non-Azure OpenAI API key credential string.
 */
public final class NonAzureOpenAIKeyCredential {
    private static final ClientLogger LOGGER = new ClientLogger(NonAzureOpenAIKeyCredential.class);

    private String key;

    /**
     * Create a non-Azure OpenAI API key credential.
     *
     * @param key non-Azure OpenAI API key.
     * @throws NullPointerException If key is null.
     * @throws IllegalArgumentException If key is an empty string.
     */
    public NonAzureOpenAIKeyCredential(String key) {
        Objects.requireNonNull(key, "'key' cannot be null.");
        if (key.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'key' cannot be empty."));
        }
        this.key = key;
    }

    /**
     * Retrieves the key associated to this credential.
     *
     * @return The key being used to authorize requests.
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Update the OpenAI API key value.
     *
     * @param key The OpenAI API key value.
     * @return the object itself.
     */
    public NonAzureOpenAIKeyCredential update(String key) {
        Objects.requireNonNull(key, "'key' cannot be null.");
        if (key.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'key' cannot be empty."));
        } else {
            this.key = key;
            return this;
        }
    }
}
