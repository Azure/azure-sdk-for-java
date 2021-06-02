// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Fluent;

/**
 * The MetricsAdvisorKeyCredential class.
 */
@Fluent
public final class MetricsAdvisorKeyCredential {
    private volatile String subscriptionKey;
    private volatile String apiKey;
    private final Object updateLock = new Object();

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

    /**
     * Update the subscription and api key associated to this credential.
     * <p>
     * This is intended to be used when you've regenerated your subscription key and
     * api key want to update long lived clients.
     * </p>
     * @param subscriptionKey The new subscription key to associated with this credential.
     * @param apiKey The new api key to associated with this credential.
     * @return The updated {@code MetricsAdvisorKeyCredential} object.
     */
    public MetricsAdvisorKeyCredential updateKey(String subscriptionKey, String apiKey) {
        synchronized (this.updateLock) {
            this.subscriptionKey = subscriptionKey;
            this.apiKey = apiKey;
        }
        return this;
    }
}
