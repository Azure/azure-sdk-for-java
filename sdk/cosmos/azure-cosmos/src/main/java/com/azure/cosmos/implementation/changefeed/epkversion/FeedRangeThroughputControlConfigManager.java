// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.ThroughputControlGroupConfig;
import com.azure.cosmos.ThroughputControlGroupConfigBuilder;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.models.FeedRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

// Only used in CFP when customer configure throughput control config
// The main purpose of this class is to monitor the lease feedRange -> partitionKeyRange mapping changes, and create corresponding throughput control group
public class FeedRangeThroughputControlConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(FeedRangeThroughputControlConfigManager.class);

    private final ThroughputControlGroupConfig throughputControlGroupConfig;
    private final ChangeFeedContextClient documentClient;
    private final AtomicReference<List<FeedRangeEpkImpl>> leaseTokens; // epk leases
    private final Map<PartitionKeyRange, List<FeedRange>> pkRangeToFeedRangeMap;
    private final Map<FeedRange, ThroughputControlGroupConfig> feedRangeToThroughputControlGroupConfigMap;

    public FeedRangeThroughputControlConfigManager(
        ThroughputControlGroupConfig throughputControlGroupConfig,
        ChangeFeedContextClient documentClient) {

        checkNotNull(throughputControlGroupConfig, "Argument 'throughputControlGroupConfig' can not be null");
        checkNotNull(documentClient, "Argument 'documentClient' can not be null");

        this.throughputControlGroupConfig = throughputControlGroupConfig;
        this.documentClient = documentClient;
        this.leaseTokens = new AtomicReference<>();
        this.pkRangeToFeedRangeMap = new ConcurrentHashMap<>();
        this.feedRangeToThroughputControlGroupConfigMap = new ConcurrentHashMap<>();
    }

    /**
     * This method will be called during the leases load balancing time from {@link com.azure.cosmos.implementation.changefeed.common.EqualPartitionsBalancingStrategy}.
     * We are going to track the up to date all leases and refresh the partitionKeyRangesCache
     *
     * @param leases all the current leases in lease container.
     * @return a representation of the deferred computation of this call.
     */
    public Mono<Void> refresh(List<Lease> leases) {
        if (leases != null && !leases.isEmpty()) {
            this.leaseTokens.set(leases.stream().map(lease -> (FeedRangeEpkImpl)lease.getFeedRange()).collect(Collectors.toList()));
        }

        return this.documentClient.getOverlappingRanges(PartitionKeyInternalHelper.FullRange, false)
            .doOnNext(pkRanges -> {
                if (!pkRanges.isEmpty()) {
                    // Go through the pkRangeToFeedRangeMap,
                    // if the tracked pkRange does not exist any longer,
                    // then remove it from the map and also remove the mapped feedRanges presents from feedRangeToThroughputControlGroupConfigMap
                    // empty entry in feedRangeToThroughputControlGroupConfigMap will trigger a recreation the throughputControlGroupConfig
                    for (PartitionKeyRange pkRange : pkRangeToFeedRangeMap.keySet()) {
                        if (!pkRanges.contains(pkRange)) {
                            List<FeedRange> feedRanges = pkRangeToFeedRangeMap.remove(pkRange);
                            logger.debug("PkRange {} does not exist any more, remove it from map. ", pkRange.getId());
                            for (FeedRange feedRange : feedRanges) {
                                this.feedRangeToThroughputControlGroupConfigMap.remove(feedRange);
                            }
                        }
                    }
                }
            })
            .onErrorResume(throwable -> {
                logger.warn("Refresh pkRanges failed", throwable);
                return Mono.empty();
            })
            .then();
    }

    public Mono<ThroughputControlGroupConfig> getOrCreateThroughputControlConfigForFeedRange(FeedRangeEpkImpl feedRange) {
        checkNotNull(feedRange, "Argument 'feedRange' can not be null");
        ThroughputControlGroupConfig throughputControlGroupConfigForFeedRange =
            this.feedRangeToThroughputControlGroupConfigMap.get(feedRange);

        if (throughputControlGroupConfigForFeedRange != null) {
            return Mono.just(throughputControlGroupConfigForFeedRange);
        }

        return this.createThroughputControlConfigForFeedRange(feedRange);

    }

    public Mono<ThroughputControlGroupConfig> createThroughputControlConfigForFeedRange(FeedRangeEpkImpl feedRange) {
        checkNotNull(feedRange, "Argument 'feedRange' can not be null");

        // for epk leases, it is used to support both split and merge
        // when merge happens, the current lease will be reused, so it can happen for the same partition key range, there are multiple leases map to it
        // for cases like this, we are going to find all leases mapped to the same partition key range, and then equally divide the RU allocation among the instances
        // For this purpose, we are going to create a throughputControlGroup for each lease feed range
        return this.documentClient.getOverlappingRanges(feedRange.getRange(), false)
            .flatMap(partitionKeyRanges -> {
                if (partitionKeyRanges.isEmpty()) {
                    return Mono.error(new IllegalStateException("Failed to get overlapping partition key range for range " + feedRange));
                }

                if (partitionKeyRanges.size() > 1) {
                    return Mono.error(new IllegalStateException("There are more than one partition key ranges mapped to the lease feed range. This should never happen"));
                }

                // add pkRange -> feedRange mapping so next time during refresh
                // so when we detect the pkRange does not exists any more, we can trigger a proper throughputControlGroup recreation for the feedRange
                this.pkRangeToFeedRangeMap.compute(partitionKeyRanges.get(0), (key, feedRangeList) -> {
                    if (feedRangeList == null) {
                        feedRangeList = new ArrayList<>();
                    }

                    feedRangeList.add(feedRange);
                    return feedRangeList;
                });

                // find all leases overlapping with the partition key range
                long leasesBelongToSamePartitionKeyRange =
                    this.leaseTokens
                        .get()
                        .stream()
                        .filter(leaseToken ->
                            leaseToken.getRange().getMin().compareTo(partitionKeyRanges.get(0).getMinInclusive()) >= 0
                            && leaseToken.getRange().getMax().compareTo(partitionKeyRanges.get(0).getMaxExclusive()) <= 0)
                        .count();

                ThroughputControlGroupConfig throughputControlGroupConfigForFeedRange =
                    this.getThroughputControlGroupConfigInternal(feedRange, leasesBelongToSamePartitionKeyRange);

                return Mono.just(
                    this.feedRangeToThroughputControlGroupConfigMap.compute(feedRange, (key, config) -> throughputControlGroupConfigForFeedRange)
                );
            })
            .onErrorResume(throwable -> {
                // Throughput control flow should not break the normal request flow
                // we will capture all exceptions and fall back to use partition divider factor 1
                logger.warn("getThroughputControlConfigForLeaseFeedRange failed, using divide factor 1", throwable);
                return Mono.just(
                    getThroughputControlGroupConfigInternal(feedRange, 1));
            });
    }

    private ThroughputControlGroupConfig getThroughputControlGroupConfigInternal(
        FeedRange feedRange,
        long perPartitionDivideFactor) {

        // For each feedRange, we create a local throughput control group
        // We choose to start from local throughput control based on the idea that usually each CFP instance will only process a unique subset of feedRanges
        ThroughputControlGroupConfigBuilder throughputControlGroupConfigForFeedRangeBuilder =
            new ThroughputControlGroupConfigBuilder()
                .groupName(this.throughputControlGroupConfig.getGroupName() + "-" + feedRange.toString())
                .continueOnInitError(this.throughputControlGroupConfig.continueOnInitError());

        if (this.throughputControlGroupConfig.getTargetThroughput() != null) {
            throughputControlGroupConfigForFeedRangeBuilder.targetThroughput(
                (int) Math.max(
                    this.throughputControlGroupConfig.getTargetThroughput() / perPartitionDivideFactor,
                    1
                ));
        }
        if (this.throughputControlGroupConfig.getTargetThroughputThreshold() != null) {
            throughputControlGroupConfigForFeedRangeBuilder.targetThroughputThreshold(
                this.throughputControlGroupConfig.getTargetThroughputThreshold() / perPartitionDivideFactor);
        }
        if (this.throughputControlGroupConfig.getPriorityLevel() != null) {
            throughputControlGroupConfigForFeedRangeBuilder.priorityLevel(this.throughputControlGroupConfig.getPriorityLevel());
        }

        ThroughputControlGroupConfig throughputControlGroupConfigForFeedRange = throughputControlGroupConfigForFeedRangeBuilder.build();
        this.documentClient.getContainerClient().enableLocalThroughputControlGroup(throughputControlGroupConfigForFeedRange);

        return throughputControlGroupConfigForFeedRange;
    }
}
