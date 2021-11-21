// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Immutable;

/**
 * Represents a credential bag containing the subscription key and api key.
 *
 * @see MetricsAdvisorKeyCredential
 */
@Immutable
public final class MetricsAdvisorKeys {
    private final String subscriptionKey;
    private final String apiKey;

    MetricsAdvisorKeys(String subscriptionKey, String apiKey) {
        this.subscriptionKey = subscriptionKey;
        this.apiKey = apiKey;
    }

    /**
     * Retrieves the subscription key.
     *
     * @return The subscription key.
     */
    public String getSubscriptionKey() {
        return this.subscriptionKey;
    }

    /**
     * Retrieves the api key.
     *
     * @return The api key.
     */
    public String getApiKey() {
        return this.apiKey;
    }
}
