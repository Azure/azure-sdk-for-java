package com.azure.communication.jobrouter.models;

import com.azure.communication.jobrouter.implementation.models.DistributionMode;

public abstract class DistributionPolicyOptions {
    protected String id;
    protected String name;
    protected Double offerTtlSeconds;
    protected DistributionMode mode;

    /*
     * Get the unique identifier of the policy.
     */
    public String getId() {
        return this.id;
    }

    /*
     * Get the human-readable name of the policy.
     */
    public String getName() {
        return this.name;
    }

    /*
     * Get the offer time to live of offers created under this policy.
     */
    public Double getOfferTtlSeconds() {
        return this.offerTtlSeconds;
    }

    /*
     * Get the distribution mode of this policy.
     */
    public DistributionMode getMode() {
        return this.mode;
    }
}
