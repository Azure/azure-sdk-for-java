// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.implementation.models.BestWorkerModeInternal;
import com.azure.communication.jobrouter.implementation.models.DistributionModeInternal;
import com.azure.communication.jobrouter.implementation.models.DistributionPolicyInternal;
import com.azure.communication.jobrouter.implementation.models.LongestIdleModeInternal;
import com.azure.communication.jobrouter.implementation.models.RoundRobinModeInternal;
import com.azure.communication.jobrouter.models.BestWorkerMode;
import com.azure.communication.jobrouter.models.CreateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.DistributionMode;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.LongestIdleMode;
import com.azure.communication.jobrouter.models.RoundRobinMode;

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
            .setId(createDistributionPolicyOptions.getDistributionPolicyId())
            .setMode(convertDistributionModeToInternal(createDistributionPolicyOptions.getMode()))
            .setOfferExpiresAfterSeconds((double) createDistributionPolicyOptions.getOfferExpiresAfter().getSeconds())
            .setName(createDistributionPolicyOptions.getName());
    }

    public static DistributionModeInternal convertDistributionModeToInternal(DistributionMode mode) {
        if (mode instanceof BestWorkerMode) {
            BestWorkerMode bestWorker = (BestWorkerMode) mode;
            return new BestWorkerModeInternal()
                .setMinConcurrentOffers(bestWorker.getMinConcurrentOffers())
                .setMaxConcurrentOffers(bestWorker.getMaxConcurrentOffers())
                .setBypassSelectors(bestWorker.isBypassSelectors())
                .setScoringRule(RouterRuleAdapter.convertRouterRuleToInternal(bestWorker.getScoringRule()))
                .setScoringRuleOptions(bestWorker.getScoringRuleOptions());
        } else if (mode instanceof RoundRobinMode) {
            RoundRobinMode roundRobin = (RoundRobinMode) mode;
            return new RoundRobinModeInternal()
                .setMinConcurrentOffers(roundRobin.getMinConcurrentOffers())
                .setMaxConcurrentOffers(roundRobin.getMaxConcurrentOffers())
                .setBypassSelectors(roundRobin.isBypassSelectors());
        } else if (mode instanceof LongestIdleMode) {
            LongestIdleMode longestIdle = (LongestIdleMode) mode;
            return new LongestIdleModeInternal()
                .setMinConcurrentOffers(longestIdle.getMinConcurrentOffers())
                .setMaxConcurrentOffers(longestIdle.getMaxConcurrentOffers())
                .setBypassSelectors(longestIdle.isBypassSelectors());
        }

        return null;
    }

}
