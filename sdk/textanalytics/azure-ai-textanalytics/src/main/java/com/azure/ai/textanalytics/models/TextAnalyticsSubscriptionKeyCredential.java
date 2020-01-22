// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

/**
 * Subscription key credential that shared across cognitive services, or restrict to single service.
 */
public final class TextAnalyticsSubscriptionKeyCredential {
    private String subscriptionKey;

    /**
     * Creates a {@code SharedKeyCredential} model that describes subscription key.
     *
     * @param subscriptionKey the subscription key
     */
    public TextAnalyticsSubscriptionKeyCredential(String subscriptionKey) {
        this.subscriptionKey = subscriptionKey;
    }

    /**
     * Get the subscription key value.
     *
     * @return the subscription key value
     */
    public String getSubscriptionKey() {
        return this.subscriptionKey;
    }

    /**
     * Set the subscription key value.
     *
     * @param subscriptionKey the subscription key
     * @return the {@link TextAnalyticsSubscriptionKeyCredential} itself
     */
    public TextAnalyticsSubscriptionKeyCredential updateCredential(String subscriptionKey) {
        this.subscriptionKey = subscriptionKey;
        return this;
    }
}
