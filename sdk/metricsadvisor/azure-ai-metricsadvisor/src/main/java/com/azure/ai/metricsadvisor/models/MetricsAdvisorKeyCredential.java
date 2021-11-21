// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Fluent;

/**
 * The MetricsAdvisorKeyCredential class.
 */
@Fluent
public final class MetricsAdvisorKeyCredential {
    private volatile MetricsAdvisorKeys keys;
    private final Object updateLock = new Object();

    /**
     * Creates a MetricsAdvisorKeyCredential credential that authorizes request with the given keys.
     *
     * @param subscriptionKey the subscription key used to authorize requests
     * @param apiKey the api key used to authorize requests
     */
    public MetricsAdvisorKeyCredential(String subscriptionKey, String apiKey) {
        this.keys = new MetricsAdvisorKeys(subscriptionKey, apiKey);
    }

    /**
     * Retrieves the {@link MetricsAdvisorKeys} containing the subscription key and api key
     * associated with this credential.
     *
     * @return The {@link MetricsAdvisorKeys} containing the subscription key and api key.
     */
    public MetricsAdvisorKeys getKeys() {
        return this.keys;
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
            this.keys = new MetricsAdvisorKeys(subscriptionKey, apiKey);
        }
        return this;
    }
}
