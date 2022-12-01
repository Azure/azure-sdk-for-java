// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models.options;

import com.azure.communication.jobrouter.models.DistributionMode;
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
    private final String id;

    /**
     * The human readable name of the policy.
     */
    private String name;

    /**
     * The expiry time of any offers created under this policy will be governed
     * by the offer time to live.
     */
    private final Duration offerTtl;

    /**
     * Abstract base class for defining a distribution mode
     */
    private final DistributionMode mode;

    /**
     * Constructor for CreateDistributionPolicyOptions.
     * @param id The unique identifier of the policy.
     * @param offerTtl The expiry time of any offers created under this policy will be governed
     *   by the offer time to live.
     * @param mode Abstract base class for defining a distribution mode.
     */
    public CreateDistributionPolicyOptions(String id, Duration offerTtl, DistributionMode mode) {
        this.id = id;
        this.offerTtl = offerTtl;
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
