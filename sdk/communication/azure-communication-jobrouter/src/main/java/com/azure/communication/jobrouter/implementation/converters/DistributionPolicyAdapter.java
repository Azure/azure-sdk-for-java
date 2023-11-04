// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.implementation.models.DistributionPolicyInternal;
import com.azure.communication.jobrouter.models.CreateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.DistributionPolicy;

/**
 * Converts request options for create and update Classification Policy to {@link DistributionPolicy}.
 */
public class DistributionPolicyAdapter {
    /**
     * Converts {@link CreateDistributionPolicyOptions} to {@link DistributionPolicyInternal}.
     * @param createDistributionPolicyOptions Container with options to create a DistributionPolicy.
     * @return distribution policy.
     */
    public static DistributionPolicyInternal convertCreateOptionsToDistributionPolicy(CreateDistributionPolicyOptions createDistributionPolicyOptions) {
        return new DistributionPolicyInternal()
            .setMode(createDistributionPolicyOptions.getMode())
            .setOfferExpiresAfterSeconds((double) createDistributionPolicyOptions.getOfferExpiresAfter().getSeconds())
            .setName(createDistributionPolicyOptions.getName());
    }
}
