/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;


/**
 * Subscription information.
 */
public class SubscriptionInner {
    /**
     * Gets or sets the ID of the resource (/subscriptions/SubscriptionId).
     */
    private String id;

    /**
     * Gets or sets the subscription Id.
     */
    private String subscriptionId;

    /**
     * Gets or sets the subscription display name.
     */
    private String displayName;

    /**
     * Gets or sets the subscription state.
     */
    private String state;

    /**
     * Gets or sets the subscription policies.
     */
    private SubscriptionPolicies subscriptionPolicies;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     * @return the SubscriptionInner object itself.
     */
    public SubscriptionInner withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the subscriptionId value.
     *
     * @return the subscriptionId value
     */
    public String subscriptionId() {
        return this.subscriptionId;
    }

    /**
     * Set the subscriptionId value.
     *
     * @param subscriptionId the subscriptionId value to set
     * @return the SubscriptionInner object itself.
     */
    public SubscriptionInner withSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    /**
     * Get the displayName value.
     *
     * @return the displayName value
     */
    public String displayName() {
        return this.displayName;
    }

    /**
     * Set the displayName value.
     *
     * @param displayName the displayName value to set
     * @return the SubscriptionInner object itself.
     */
    public SubscriptionInner withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the state value.
     *
     * @return the state value
     */
    public String state() {
        return this.state;
    }

    /**
     * Set the state value.
     *
     * @param state the state value to set
     * @return the SubscriptionInner object itself.
     */
    public SubscriptionInner withState(String state) {
        this.state = state;
        return this;
    }

    /**
     * Get the subscriptionPolicies value.
     *
     * @return the subscriptionPolicies value
     */
    public SubscriptionPolicies subscriptionPolicies() {
        return this.subscriptionPolicies;
    }

    /**
     * Set the subscriptionPolicies value.
     *
     * @param subscriptionPolicies the subscriptionPolicies value to set
     * @return the SubscriptionInner object itself.
     */
    public SubscriptionInner withSubscriptionPolicies(SubscriptionPolicies subscriptionPolicies) {
        this.subscriptionPolicies = subscriptionPolicies;
        return this;
    }

}
