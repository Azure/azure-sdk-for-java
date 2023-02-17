// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.credential.AzureKeyCredential;

/**
 * The MetricsAdvisorKeyCredential class.
 */
@Fluent
public final class MetricsAdvisorKeyCredential {
    private final AzureKeyCredential credential;

    /**
     * Creates a MetricsAdvisorKeyCredential credential that authorizes request with the given keys.
     *
     * @param subscriptionKey the subscription key used to authorize requests
     * @param apiKey the api key used to authorize requests
     */
    public MetricsAdvisorKeyCredential(String subscriptionKey, String apiKey) {
        this.credential = new AzureKeyCredential(subscriptionKey, apiKey);
    }

    /**
     * Retrieves the {@link MetricsAdvisorKeys} containing the subscription key and api key
     * associated with this credential.
     *
     * @return The {@link MetricsAdvisorKeys} containing the subscription key and api key.
     */
    public MetricsAdvisorKeys getKeys() {
        String[] keys = credential.getKeys();
        return new MetricsAdvisorKeys(keys[0], keys[1]);
    }

    /**
     * This would be a package-private method made accessible through internal code, but this is a prototype PR.
     *
     * @return The {@link AzureKeyCredential} backing the {@link MetricsAdvisorKeyCredential}.
     */
    public AzureKeyCredential getCredential() {
        return credential;
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
        credential.update(subscriptionKey, apiKey);
        return this;
    }
}
