package com.azure.communication.jobrouter.models;

import com.azure.communication.jobrouter.implementation.models.DistributionMode;

/**
 * Abstract class for Create and Update DistributionPolicy Options.
 */
public abstract class DistributionPolicyOptions {
    /**
     * The unique identifier of the policy.
     */
    protected String id;

    /**
     * The human readable name of the policy.
     */
    protected String name;

    /**
     * The expiry time of any offers created under this policy will be governed
     * by the offer time to live.
     */
    protected Double offerTtlSeconds;

    /**
     * Abstract base class for defining a distribution mode
     */
    protected DistributionMode mode;

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
