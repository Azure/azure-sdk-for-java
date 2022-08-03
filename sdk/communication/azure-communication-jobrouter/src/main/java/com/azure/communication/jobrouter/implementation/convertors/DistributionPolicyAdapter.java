// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.convertors;

import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.options.CreateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.options.UpdateDistributionPolicyOptions;

/**
 * Converts request options for create and update Classification Policy to {@link DistributionPolicy}.
 */
public class DistributionPolicyAdapter {
    /**
     * Converts {@link CreateDistributionPolicyOptions} to {@link DistributionPolicy}.
     * @param createDistributionPolicyOptions Container with options to create a DistributionPolicy.
     * @return distribution policy.
     */
    public static DistributionPolicy convertCreateOptionsToDistributionPolicy(CreateDistributionPolicyOptions createDistributionPolicyOptions) {
        return new DistributionPolicy()
            .setMode(createDistributionPolicyOptions.getMode())
            .setOfferTtlSeconds(Long.valueOf(createDistributionPolicyOptions.getOfferTtl().getSeconds()).doubleValue())
            .setName(createDistributionPolicyOptions.getName());
    }

    /**
     * Converts {@link UpdateDistributionPolicyOptions} to {@link DistributionPolicy}.
     * @param updateDistributionPolicyOptions Container with options to update a DistributionPolicy.
     * @return distribution policy.
     */
    public static DistributionPolicy convertUpdateOptionsToClassificationPolicy(UpdateDistributionPolicyOptions updateDistributionPolicyOptions) {
        return new DistributionPolicy()
            .setMode(updateDistributionPolicyOptions.getMode())
            .setName(updateDistributionPolicyOptions.getName())
            .setOfferTtlSeconds(Long.valueOf(updateDistributionPolicyOptions.getOfferTtl().getSeconds()).doubleValue());
    }
}
