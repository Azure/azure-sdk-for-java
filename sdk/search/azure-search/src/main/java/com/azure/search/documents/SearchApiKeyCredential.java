// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

/**
 * Credentials that authorize requests to Azure Cognitive Search. It uses content within the HTTP request to generate
 * the correct "api-key" header value
 *
 * @see SearchIndexClientBuilder
 * @see SearchServiceClientBuilder
 */
public final class SearchApiKeyCredential {
    private String apiKey;

    /**
     * Creates an instance that is able to authorize requests to Azure Cognitive Search.
     *
     * @param apiKey a query or admin API key
     */
    public SearchApiKeyCredential(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Gets the API key
     *
     * @return the API key
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Rotates the Search credential.
     *
     * @param apiKey a query or admin API key
     * @return {@link SearchApiKeyCredential}
     */
    public SearchApiKeyCredential updateApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }
}
