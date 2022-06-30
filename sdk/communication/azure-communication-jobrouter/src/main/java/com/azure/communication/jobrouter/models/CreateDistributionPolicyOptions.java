package com.azure.communication.jobrouter.models;

import com.azure.communication.jobrouter.implementation.models.DistributionMode;
import com.azure.core.annotation.Fluent;

/**
 *  Request options to create a DistributionPolicy.
 *  DistributionPolicy: Policy governing how jobs are distributed to workers.
 * */
@Fluent
public class CreateDistributionPolicyOptions extends DistributionPolicyOptions {

    /**
     * Constructor for CreateDistributionPolicyOptions.
     * @param id The unique identifier of the policy.
     * @param name The human-readable name of the policy.
     * @param offerTtlSeconds The expiry time of any offers created under this policy will be governed
     *   by the offer time to live.
     * @param mode Abstract base class for defining a distribution mode.
     */
    public CreateDistributionPolicyOptions(String id, String name, Double offerTtlSeconds, DistributionMode mode) {
        this.id = id;
        this.name = name;
        this.offerTtlSeconds = offerTtlSeconds;
        this.mode = mode;
    }
}
