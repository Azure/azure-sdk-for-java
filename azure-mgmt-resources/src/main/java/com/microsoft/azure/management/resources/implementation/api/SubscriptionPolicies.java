/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;


/**
 * Subscription policies.
 */
public class SubscriptionPolicies {
    /**
     * Gets or sets the subscription location placement Id.
     */
    private String locationPlacementId;

    /**
     * Gets or sets the subscription quota Id.
     */
    private String quotaId;

    /**
     * Get the locationPlacementId value.
     *
     * @return the locationPlacementId value
     */
    public String locationPlacementId() {
        return this.locationPlacementId;
    }

    /**
     * Set the locationPlacementId value.
     *
     * @param locationPlacementId the locationPlacementId value to set
     * @return the SubscriptionPolicies object itself.
     */
    public SubscriptionPolicies withLocationPlacementId(String locationPlacementId) {
        this.locationPlacementId = locationPlacementId;
        return this;
    }

    /**
     * Get the quotaId value.
     *
     * @return the quotaId value
     */
    public String quotaId() {
        return this.quotaId;
    }

    /**
     * Set the quotaId value.
     *
     * @param quotaId the quotaId value to set
     * @return the SubscriptionPolicies object itself.
     */
    public SubscriptionPolicies withQuotaId(String quotaId) {
        this.quotaId = quotaId;
        return this;
    }

}
