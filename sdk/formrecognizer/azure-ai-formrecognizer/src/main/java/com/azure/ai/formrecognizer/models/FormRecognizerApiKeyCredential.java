// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

/**
 * API key credential that shared across cognitive services, or restrict to single service.
 *
 * <p>Be able to rotate an existing API key</p>
 * TODO: codesnippet
 */
public final class FormRecognizerApiKeyCredential {
    private final ClientLogger logger = new ClientLogger(FormRecognizerApiKeyCredential.class);
    private volatile String apiKey;

    /**
     * Creates a {@link FormRecognizerApiKeyCredential} model that describes API key for authentication.
     *
     * @param apiKey the API key for authentication
     */
    public FormRecognizerApiKeyCredential(String apiKey) {
        Objects.requireNonNull(apiKey, "`apiKey` cannot be null.");
        if (apiKey.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'apiKey' cannot be empty."));
        }
        this.apiKey = apiKey;
    }

    /**
     * Get the API key.
     *
     * @return the API key
     */
    public String getApiKey() {
        return this.apiKey;
    }

    /**
     * Set the API key.
     *
     * @param apiKey the API key for authentication
     */
    public void updateCredential(String apiKey) {
        this.apiKey = Objects.requireNonNull(apiKey, "`apiKey` cannot be null.");
    }
}
