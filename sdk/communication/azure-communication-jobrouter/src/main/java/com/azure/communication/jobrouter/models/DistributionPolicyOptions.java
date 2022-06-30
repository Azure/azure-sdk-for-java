package com.azure.communication.jobrouter.models;

import com.azure.communication.jobrouter.implementation.models.DistributionMode;

/**
 * Abstract class for Create and Update DistributionPolicy Options.
 */
public abstract class DistributionPolicyOptions {
    protected String id;
    protected String name;
    protected Double offerTtlSeconds;
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
