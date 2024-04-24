// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;

/**
 *  Request options to create a DistributionPolicy.
 *  DistributionPolicy: Policy governing how jobs are distributed to workers.
 * */
@Fluent
public final class CreateDistributionPolicyOptions {
    /**
     * The unique identifier of the policy.
     */
    private final String distributionPolicyId;

    /**
     * The human-readable name of the policy.
     */
    private String name;

    /**
     * The expiry time of any offers created under this policy.
     */
    private final Duration offerExpiresAfter;

    /**
     * Abstract base class for defining a distribution mode
     */
    private final DistributionMode mode;

    /**
     * Constructor for CreateDistributionPolicyOptions.
     * @param distributionPolicyId The unique identifier of the policy.
     * @param offerExpiresAfter The expiry time of any offers created under this policy.
     * @param mode Abstract base class for defining a distribution mode.
     */
    public CreateDistributionPolicyOptions(String distributionPolicyId, Duration offerExpiresAfter, DistributionMode mode) {
        this.distributionPolicyId = distributionPolicyId;
        this.offerExpiresAfter = offerExpiresAfter;
        this.mode = mode;
    }

    /**
     * @param name The human-readable name of the policy.
     * @return this
     */
    public CreateDistributionPolicyOptions setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the unique identifier of the policy.
     * @return id
     */
    public String getDistributionPolicyId() {
        return this.distributionPolicyId;
    }

    /**
     * Get the human-readable name of the policy.
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the expiry time for offers created under this policy.
     * @return offerExpiresAfterSeconds
     */
    public Duration getOfferExpiresAfter() {
        return this.offerExpiresAfter;
    }

    /**
     * Get the distribution mode of this policy.
     * @return distributionMode
     */
    public DistributionMode getMode() {
        return this.mode;
    }
}
