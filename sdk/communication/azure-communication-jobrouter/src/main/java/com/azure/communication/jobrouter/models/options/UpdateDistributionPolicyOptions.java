// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models.options;

import com.azure.communication.jobrouter.models.DistributionMode;

import java.time.Duration;

/**
 *  Request options to update a DistributionPolicy.
 *  DistributionPolicy: Policy governing how jobs are distributed to workers.
 * */
public final class UpdateDistributionPolicyOptions {
    /**
     * The unique identifier of the policy.
     */
    private final String id;

    /**
     * The human readable name of the policy.
     */
    private String name;

    /**
     * The expiry time of any offers created under this policy will be governed
     * by the offer time to live.
     */
    private Duration offerTtl;

    /**
     * Abstract base class for defining a distribution mode
     */
    private DistributionMode mode;

    /**
     * Constructor for UpdateDistributionPolicyOptions.
     * @param id The unique identifier of the policy.
     */
    public UpdateDistributionPolicyOptions(String id) {
        this.id = id;
    }

    /**
     * Sets distribution policy name.
     * @param name The human-readable name of the policy.
     * @return this
     */
    public UpdateDistributionPolicyOptions setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets offerTtlSeconds.
     * @param offerTtl The expiry time of any offers created under this policy will be governed
     *   by the offer time to live.
     * @return this
     */
    public UpdateDistributionPolicyOptions setOfferTtl(Duration offerTtl) {
        this.offerTtl = offerTtl;
        return this;
    }

    /**
     * Sets DistributionMode.
     * @param mode One of best-worker, round-robin, longest-idle modes.
     * @return this
     */
    public UpdateDistributionPolicyOptions setMode(DistributionMode mode) {
        this.mode = mode;
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
    public Duration getOfferTtl() {
        return this.offerTtl;
    }

    /**
     * Get the distribution mode of this policy.
     * @return distributionMode
     */
    public DistributionMode getMode() {
        return this.mode;
    }
}
