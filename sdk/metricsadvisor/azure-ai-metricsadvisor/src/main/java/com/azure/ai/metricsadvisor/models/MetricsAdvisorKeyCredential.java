// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

/**
 * The MetricsAdvisorKeyCredential class.
 */
public final class MetricsAdvisorKeyCredential {
    private final String subscriptionKey;
    private final String apiKey;

    /**
     * Creates a MetricsAdvisorKeyCredential credential that authorizes request with the given keys.
     *
     * @param subscriptionKey the subscription key used to authorize requests
     * @param apiKey the api key used to authorize requests
     */
    public MetricsAdvisorKeyCredential(String subscriptionKey, String apiKey) {
        this.subscriptionKey = subscriptionKey;
        this.apiKey = apiKey;
    }

    /**
     * Retrieves the subscription key associated to this credential.
     *
     * @return The subscription key being used to authorize requests.
     */
    public String getSubscriptionKey() {
        return this.subscriptionKey;
    }

    /**
     * Retrieves the api key associated to this credential.
     *
     * @return The api key being used to authorize requests.
     */
    public String getApiKey() {
        return this.apiKey;
    }
}
