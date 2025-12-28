// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.PartitionLoadBalancingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Implementation for {@link PartitionLoadBalancingStrategy}.
 */
public class EqualPartitionsBalancingStrategy implements PartitionLoadBalancingStrategy {
    private final Logger logger = LoggerFactory.getLogger(EqualPartitionsBalancingStrategy.class);
    private final String hostName;
    private final int minPartitionCount;
    private final int maxPartitionCount;
    private final Duration leaseExpirationInterval;
    private final int maxLeasesToAcquirePerCycle;

    public EqualPartitionsBalancingStrategy(String hostName, int minPartitionCount, int maxPartitionCount, Duration leaseExpirationInterval) {
        this(hostName, minPartitionCount, maxPartitionCount, leaseExpirationInterval, 0);
    }

    public EqualPartitionsBalancingStrategy(
        String hostName,
        int minPartitionCount,
        int maxPartitionCount,
        Duration leaseExpirationInterval,
        int maxLeasesToAcquirePerCycle) {
        if (hostName == null) {
            throw new IllegalArgumentException("hostName");
        }

        this.hostName = hostName;
        this.minPartitionCount = minPartitionCount;
        this.maxPartitionCount = maxPartitionCount;
        this.leaseExpirationInterval = leaseExpirationInterval;
        if (maxLeasesToAcquirePerCycle < 0) {
            throw new IllegalArgumentException("maxLeasesToAcquirePerCycle cannot be negative");
        }
        this.maxLeasesToAcquirePerCycle = maxLeasesToAcquirePerCycle;
    }

    @Override
    public List<Lease> selectLeasesToTake(List<Lease> allLeases) {
        Map<String, Integer> workerToPartitionCount = new HashMap<>();
        List<Lease> expiredLeases = new ArrayList<>();
        Map<String, Lease> allPartitions = new HashMap<>();

        this.categorizeLeases(allLeases, allPartitions, expiredLeases, workerToPartitionCount);

        int partitionCount = allPartitions.size();
        int workerCount = workerToPartitionCount.size();

        if (partitionCount <= 0) {
            return new ArrayList<Lease>();
        }

        int target = this.calculateTargetPartitionCount(partitionCount, workerCount);
        int myCount = workerToPartitionCount.get(this.hostName);
        int partitionsNeededForMe = target - myCount;

        if (expiredLeases.size() > 0) {
            // We should try to pick at least one expired lease even if already overbooked when maximum partition count is not set.
            // If other CFP instances are running, legacy behavior limits the number of expired leases to acquire to maximum 1
            // (non-greedy acquiring) to reduce collisions. A configured maxLeasesToAcquirePerCycle overrides this clamp.
            if (this.maxPartitionCount == 0 && partitionsNeededForMe <= 0) {
                partitionsNeededForMe = 1;
            } else if (partitionsNeededForMe > 1 && workerToPartitionCount.size() > 1 && this.maxLeasesToAcquirePerCycle == 0) {
                // Legacy behavior: clamp to 1 when multiple workers exist.
                partitionsNeededForMe = 1;
            }

            if (this.maxLeasesToAcquirePerCycle > 0) {
                partitionsNeededForMe = Math.min(partitionsNeededForMe, this.maxLeasesToAcquirePerCycle);
            }

            if (partitionsNeededForMe <= 0) {
                return new ArrayList<>();
            }

            // Try to minimize potential collisions between different CFP instances trying to pick the same lease.
            // For multiple acquisitions, take a random subset.
            Random random = new Random();
            Collections.shuffle(expiredLeases, random);

            if (partitionsNeededForMe == 1) {
                Lease expiredLease = expiredLeases.get(0);
                this.logger.info("Found unused or expired lease {} (owner was {}); previous lease count for instance owner {} is {}, count of leases to target is {} and maxScaleCount {} ",
                    expiredLease.getLeaseToken(), expiredLease.getOwner(), this.hostName, myCount, partitionsNeededForMe, this.maxPartitionCount);

                return Collections.singletonList(expiredLease);
            }

            this.logger.info("Found {} unused or expired leases; previous lease count for instance owner {} is {}, count of leases to target is {} and maxScaleCount {} ",
                expiredLeases.size(), this.hostName, myCount, partitionsNeededForMe, this.maxPartitionCount);

            return expiredLeases.subList(0, Math.min(partitionsNeededForMe, expiredLeases.size()));
        }

        if (partitionsNeededForMe <= 0) {
            return new ArrayList<>();
        }

        // Intentionally keep the legacy behavior for stealing: attempt to steal at most 1 lease per cycle.
        Lease stolenLease = getLeaseToSteal(workerToPartitionCount, target, partitionsNeededForMe, allPartitions);
        List<Lease> stolenLeases = new ArrayList<>();
        if (stolenLease != null) {
            stolenLeases.add(stolenLease);
        }

        return stolenLeases;
    }

    private static Lease getLeaseToSteal(
        Map<String, Integer> workerToPartitionCount,
        int target,
        int partitionsNeededForMe,
        Map<String, Lease> allPartitions) {

        Map.Entry<String, Integer> workerToStealFrom = findWorkerWithMostPartitions(workerToPartitionCount);

        if (workerToStealFrom.getValue() > target - (partitionsNeededForMe > 1 ? 1 : 0)) {
            for (Map.Entry<String, Lease> entry : allPartitions.entrySet()) {
                if (entry.getValue().getOwner().equalsIgnoreCase(workerToStealFrom.getKey())) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    private static Map.Entry<String, Integer> findWorkerWithMostPartitions(Map<String, Integer> workerToPartitionCount) {
        Map.Entry<String, Integer> workerToStealFrom = new ChangeFeedHelper.KeyValuePair<>("", 0);

        for (Map.Entry<String, Integer> entry : workerToPartitionCount.entrySet()) {
            if (workerToStealFrom.getValue() <= entry.getValue()) {
                workerToStealFrom = entry;
            }
        }

        return workerToStealFrom;
    }

    private int calculateTargetPartitionCount(int partitionCount, int workerCount) {
        int target = 1;

        if (partitionCount > workerCount) {
            target = (int)Math.ceil((double)partitionCount / workerCount);
        }

        if (this.maxPartitionCount > 0 && target > this.maxPartitionCount) {
            target = this.maxPartitionCount;
        }

        if (this.minPartitionCount > 0 && target < this.minPartitionCount) {
            target = this.minPartitionCount;
        }

        return target;
    }

    private void categorizeLeases(
        List<Lease> allLeases,
        Map<String, Lease> allPartitions,
        List<Lease> expiredLeases,
        Map<String, Integer> workerToPartitionCount) {

        for (Lease lease : allLeases) {
            // Debug.Assert(lease.LeaseToken != null, "TakeLeasesAsync: lease.LeaseToken cannot be null.");

            allPartitions.put(lease.getLeaseToken(), lease);

            if (lease.getOwner() == null || lease.getOwner().isEmpty() || this.isExpired(lease)) {
                expiredLeases.add(lease);
            } else {
                String assignedTo = lease.getOwner();
                Integer count = workerToPartitionCount.get(assignedTo);

                if (count != null) {
                    workerToPartitionCount.replace(assignedTo, count + 1);
                } else {
                    workerToPartitionCount.put(assignedTo, 1);
                }
            }
        }

        if (!workerToPartitionCount.containsKey(this.hostName)) {
            workerToPartitionCount.put(this.hostName, 0);
        }
    }

    private boolean isExpired(Lease lease) {
        if (lease.getOwner() == null || lease.getOwner().isEmpty() || lease.getTimestamp() == null) {
            return true;
        }


        Instant leaseExpireTime = Instant.parse(lease.getTimestamp()).plus(this.leaseExpirationInterval);
        this.logger.debug("Current lease timestamp: {}, current time: {}", leaseExpireTime, Instant.now());
        return leaseExpireTime.isBefore(Instant.now());
    }
}
