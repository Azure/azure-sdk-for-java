package com.azure.communication.jobrouter.models;

import com.azure.communication.jobrouter.implementation.models.DistributionMode;

/**
 *  Request options to update a DistributionPolicy.
 *  DistributionPolicy: Policy governing how jobs are distributed to workers.
 * */
public class UpdateDistributionPolicyOptions extends DistributionPolicyOptions {

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
     * @param offerTtlSeconds The expiry time of any offers created under this policy will be governed
     *   by the offer time to live.
     * @return this
     */
    public UpdateDistributionPolicyOptions setOfferTtlSeconds(Double offerTtlSeconds) {
        this.offerTtlSeconds = offerTtlSeconds;
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
}
