// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.core.annotation.Fluent;

/**
 *  Request options to create a DistributionPolicy.
 *  DistributionPolicy: Policy governing how jobs are distributed to workers.
 * */
@Fluent
public class CreateDistributionPolicyOptions {
    /**
     * The unique identifier of the policy.
     */
    private String id;

    /**
     * The human readable name of the policy.
     */
    private String name;

    /**
     * The expiry time of any offers created under this policy will be governed
     * by the offer time to live.
     */
    private Double offerTtlSeconds;

    /**
     * Abstract base class for defining a distribution mode
     */
    private DistributionMode mode;

    /**
     * Constructor for CreateDistributionPolicyOptions.
     * @param id The unique identifier of the policy.
     * @param offerTtlSeconds The expiry time of any offers created under this policy will be governed
     *   by the offer time to live.
     * @param mode Abstract base class for defining a distribution mode.
     */
    public CreateDistributionPolicyOptions(String id, Double offerTtlSeconds, DistributionMode mode) {
        this.id = id;
        this.offerTtlSeconds = offerTtlSeconds;
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
    public String getId() {
        return this.id;
    }

    /**
     * Get the human-readable name of the policy.
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the offer time to live of offers created under this policy.
     * @return offerTtlSeconds
     */
    public Double getOfferTtlSeconds() {
        return this.offerTtlSeconds;
    }

    /**
     * Get the distribution mode of this policy.
     * @return distributionMode
     */
    public DistributionMode getMode() {
        return this.mode;
    }
}
