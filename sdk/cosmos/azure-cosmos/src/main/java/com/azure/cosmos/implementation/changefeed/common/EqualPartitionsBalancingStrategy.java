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
            // Determine how many unused/expired leases to attempt this cycle.
            // 1) If maxScaleCount is not set (unlimited), try to pick at least one expired lease even if we're already overbooked.
            // 2) If maxLeasesToAcquirePerCycle is configured, cap with it (overrides the legacy non-greedy clamp by design).
            // 3) Otherwise, preserve the legacy non-greedy clamp (at most one lease per cycle when multiple workers exist).
            int leasesToAcquire = partitionsNeededForMe;
            if (this.maxPartitionCount == 0 && leasesToAcquire <= 0) {
                leasesToAcquire = 1;
            }

            if (this.maxLeasesToAcquirePerCycle > 0) {
                leasesToAcquire = Math.min(leasesToAcquire, this.maxLeasesToAcquirePerCycle);
            } else if (leasesToAcquire > 1 && workerToPartitionCount.size() > 1) {
                leasesToAcquire = 1;
            }

            if (leasesToAcquire <= 0) {
                return new ArrayList<>();
            }

            Random random = new Random();

            if (leasesToAcquire == 1) {
                // Try to minimize potential collisions between different CFP instances trying to pick the same lease.
                Lease expiredLease = expiredLeases.get(random.nextInt(expiredLeases.size()));
                this.logger.info("Found unused or expired lease {} (owner was {}); previous lease count for instance owner {} is {}, count of leases to target is {} and maxScaleCount {} and maxLeasesToAcquirePerCycle {} ",
                    expiredLease.getLeaseToken(), expiredLease.getOwner(), this.hostName, myCount, leasesToAcquire, this.maxPartitionCount, this.maxLeasesToAcquirePerCycle);

                return Collections.singletonList(expiredLease);
            }

            // For multiple acquisitions, shuffle and take a random subset.
            Collections.shuffle(expiredLeases, random);
            this.logger.info("Found {} unused or expired leases; previous lease count for instance owner {} is {}, count of leases to target is {} and maxScaleCount {} and maxLeasesToAcquirePerCycle {} ",
                expiredLeases.size(), this.hostName, myCount, leasesToAcquire, this.maxPartitionCount, this.maxLeasesToAcquirePerCycle);

            return expiredLeases.subList(0, Math.min(leasesToAcquire, expiredLeases.size()));
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
