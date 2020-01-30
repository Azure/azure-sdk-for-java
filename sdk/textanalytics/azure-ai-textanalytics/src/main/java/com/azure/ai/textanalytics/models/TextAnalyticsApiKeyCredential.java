// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import java.util.Objects;

/**
 * API key credential that shared across cognitive services, or restrict to single service.
 *
 * <p>Be able to rotate an existing API key</p>
 * {@codesnippet com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential}
 *
 */
public final class TextAnalyticsApiKeyCredential {
    private volatile String apiKey;

    /**
     * Creates a {@link TextAnalyticsApiKeyCredential} model that describes API key for authentication.
     *
     * @param apiKey the API key for authentication
     */
    public TextAnalyticsApiKeyCredential(String apiKey) {
        this.apiKey = Objects.requireNonNull(apiKey, "`apiKey` cannot be null.");
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
