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
    private final AzureKeyCredential subscriptionKeyCredential;
    private final AzureKeyCredential apiKeyCredential;

    /**
     * Creates a MetricsAdvisorKeyCredential credential that authorizes request with the given keys.
     *
     * @param subscriptionKey the subscription key used to authorize requests
     * @param apiKey the api key used to authorize requests
     */
    public MetricsAdvisorKeyCredential(String subscriptionKey, String apiKey) {
        this.subscriptionKeyCredential = new AzureKeyCredential(subscriptionKey);
        this.apiKeyCredential = new AzureKeyCredential(apiKey);
    }

    /**
     * Retrieves the {@link MetricsAdvisorKeys} containing the subscription key and api key
     * associated with this credential.
     *
     * @return The {@link MetricsAdvisorKeys} containing the subscription key and api key.
     */
    public MetricsAdvisorKeys getKeys() {
        return new MetricsAdvisorKeys(subscriptionKeyCredential.getKey(), apiKeyCredential.getKey());
    }

    /**
     * This would be made accessible through internal code but this is a prototype.
     *
     * @return The {@link AzureKeyCredential} for the subscription key.
     */
    public AzureKeyCredential getSubscriptionKeyCredential() {
        return subscriptionKeyCredential;
    }

    /**
     * This would be made accessible through internal code but this is a prototype.
     *
     * @return The {@link AzureKeyCredential} for the API key.
     */
    public AzureKeyCredential getApiKeyCredential() {
        return apiKeyCredential;
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
        subscriptionKeyCredential.update(subscriptionKey);
        apiKeyCredential.update(apiKey);

        return this;
    }
}
