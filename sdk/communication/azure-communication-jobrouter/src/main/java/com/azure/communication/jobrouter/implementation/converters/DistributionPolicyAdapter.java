// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.implementation.models.BestWorkerModeInternal;
import com.azure.communication.jobrouter.implementation.models.DistributionModeInternal;
import com.azure.communication.jobrouter.implementation.models.DistributionPolicyInternal;
import com.azure.communication.jobrouter.implementation.models.LongestIdleModeInternal;
import com.azure.communication.jobrouter.implementation.models.RoundRobinModeInternal;
import com.azure.communication.jobrouter.implementation.models.ScoringRuleOptionsInternal;
import com.azure.communication.jobrouter.implementation.models.ScoringRuleParameterSelectorInternal;
import com.azure.communication.jobrouter.models.BestWorkerMode;
import com.azure.communication.jobrouter.models.CreateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.DistributionMode;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.LongestIdleMode;
import com.azure.communication.jobrouter.models.RoundRobinMode;
import com.azure.communication.jobrouter.models.ScoringRuleOptions;
import com.azure.communication.jobrouter.models.ScoringRuleParameterSelector;
import com.azure.communication.jobrouter.models.UpdateDistributionPolicyOptions;

import java.util.stream.Collectors;

/**
 * Converts request options for create and update Classification Policy to {@link DistributionPolicy}.
 */
public class DistributionPolicyAdapter {
    /**
     * Converts {@link CreateDistributionPolicyOptions} to {@link DistributionPolicyInternal}.
     * @param createDistributionPolicyOptions Container with options to create a DistributionPolicy.
     * @return distribution policy.
     */
    public static DistributionPolicy convertCreateOptionsToDistributionPolicy(CreateDistributionPolicyOptions createDistributionPolicyOptions) {
        return new DistributionPolicy()
            .setMode(createDistributionPolicyOptions.getMode())
            .setOfferExpiresAfterSeconds(Long.valueOf(createDistributionPolicyOptions.getOfferExpiresAfter().getSeconds()).doubleValue())
            .setName(createDistributionPolicyOptions.getName());
    }

    /**
     * Converts {@link UpdateDistributionPolicyOptions} to {@link DistributionPolicyInternal}.
     * @param updateDistributionPolicyOptions Container with options to update a DistributionPolicy.
     * @return distribution policy.
     */
    public static DistributionPolicy convertUpdateOptionsToDistributionPolicy(UpdateDistributionPolicyOptions updateDistributionPolicyOptions) {
        return new DistributionPolicy()
            .setMode(updateDistributionPolicyOptions.getMode())
            .setName(updateDistributionPolicyOptions.getName())
            .setOfferExpiresAfterSeconds(Long.valueOf(updateDistributionPolicyOptions.getOfferExpiresAfter().getSeconds()).doubleValue());
    }

    public static DistributionModeInternal convertDistributionModeToInternal(DistributionMode mode) {
        if (mode instanceof BestWorkerMode) {
            BestWorkerMode bestWorker = (BestWorkerMode) mode;
            return new BestWorkerModeInternal()
                .setMinConcurrentOffers(bestWorker.getMinConcurrentOffers())
                .setMaxConcurrentOffers(bestWorker.getMaxConcurrentOffers())
                .setBypassSelectors(bestWorker.isBypassSelectors())
                .setScoringRule(RouterRuleAdapter.convertRouterRuleToInternal(bestWorker.getScoringRule()))
                .setScoringRuleOptions(convertScoringRuleOptionsToInternal(bestWorker.getScoringRuleOptions()));
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

    public static DistributionMode convertDistributionModeToPublic(DistributionModeInternal mode) {
        if (mode instanceof BestWorkerModeInternal) {
            BestWorkerModeInternal bestWorker = (BestWorkerModeInternal) mode;
            return new BestWorkerMode()
                .setMinConcurrentOffers(bestWorker.getMinConcurrentOffers())
                .setMaxConcurrentOffers(bestWorker.getMaxConcurrentOffers())
                .setBypassSelectors(bestWorker.isBypassSelectors())
                .setScoringRule(RouterRuleAdapter.convertRouterRuleToPublic(bestWorker.getScoringRule()))
                .setScoringRuleOptions(convertScoringRuleOptionsToPublic(bestWorker.getScoringRuleOptions()));
        } else if (mode instanceof RoundRobinModeInternal) {
            RoundRobinModeInternal roundRobin = (RoundRobinModeInternal) mode;
            return new RoundRobinMode()
                .setMinConcurrentOffers(roundRobin.getMinConcurrentOffers())
                .setMaxConcurrentOffers(roundRobin.getMaxConcurrentOffers())
                .setBypassSelectors(roundRobin.isBypassSelectors());
        } else if (mode instanceof LongestIdleModeInternal) {
            LongestIdleModeInternal longestIdle = (LongestIdleModeInternal) mode;
            return new LongestIdleMode()
                .setMinConcurrentOffers(longestIdle.getMinConcurrentOffers())
                .setMaxConcurrentOffers(longestIdle.getMaxConcurrentOffers())
                .setBypassSelectors(longestIdle.isBypassSelectors());
        }

        return null;
    }

    public static ScoringRuleOptionsInternal convertScoringRuleOptionsToInternal(ScoringRuleOptions options) {
        return options != null ? new ScoringRuleOptionsInternal()
            .setAllowScoringBatchOfWorkers(options.isAllowScoringBatchOfWorkers())
            .setDescendingOrder(options.isDescendingOrder())
            .setBatchSize(options.getBatchSize())
            .setScoringParameters(options.getScoringParameters() != null
                ? options.getScoringParameters().stream()
                    .map(p -> ScoringRuleParameterSelectorInternal.fromString(p.toString()))
                    .collect(Collectors.toList()) : null)
            : null;
    }

    public static ScoringRuleOptions convertScoringRuleOptionsToPublic(ScoringRuleOptionsInternal options) {
        return options != null ? new ScoringRuleOptions()
            .setAllowScoringBatchOfWorkers(options.isAllowScoringBatchOfWorkers())
            .setDescendingOrder(options.isDescendingOrder())
            .setBatchSize(options.getBatchSize())
            .setScoringParameters(options.getScoringParameters() != null
                ? options.getScoringParameters().stream()
                .map(p -> ScoringRuleParameterSelector.fromString(p.toString()))
                .collect(Collectors.toList()) : null)
            : null;
    }
}
