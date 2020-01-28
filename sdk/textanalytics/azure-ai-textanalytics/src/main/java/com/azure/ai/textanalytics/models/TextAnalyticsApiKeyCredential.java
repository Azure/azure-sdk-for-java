// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import java.util.Objects;

/**
 * Subscription key credential that shared across cognitive services, or restrict to single service.
 *
 * <p>Be able to rotate an existing subscription key</p>
 * {@codesnippet com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential}
 *
 */
public final class TextAnalyticsApiKeyCredential {
    private volatile String subscriptionKey;

    /**
     * Creates a {@link TextAnalyticsApiKeyCredential} model that describes subscription key for
     * authentication.
     *
     * @param subscriptionKey the subscription key for authentication
     */
    public TextAnalyticsApiKeyCredential(String subscriptionKey) {
        this.subscriptionKey = Objects.requireNonNull(subscriptionKey, "`subscriptionKey` cannot be null.");
    }

    /**
     * Get the subscription key.
     *
     * @return the subscription key
     */
    public String getSubscriptionKey() {
        return this.subscriptionKey;
    }

    /**
     * Set the subscription key.
     *
     * @param subscriptionKey the subscription key for authentication
     */
    public void updateCredential(String subscriptionKey) {
        this.subscriptionKey = Objects.requireNonNull(subscriptionKey, "`subscriptionKey` cannot be null.");
    }
}
