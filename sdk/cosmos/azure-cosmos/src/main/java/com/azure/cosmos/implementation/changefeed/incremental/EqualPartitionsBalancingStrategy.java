// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.incremental;

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
class EqualPartitionsBalancingStrategy implements PartitionLoadBalancingStrategy {
    private final Logger logger = LoggerFactory.getLogger(EqualPartitionsBalancingStrategy.class);
    private final String hostName;
    private final int minPartitionCount;
    private final int maxPartitionCount;
    private final Duration leaseExpirationInterval;

    public EqualPartitionsBalancingStrategy(String hostName, int minPartitionCount, int maxPartitionCount, Duration leaseExpirationInterval) {
        if (hostName == null) {
            throw new IllegalArgumentException("hostName");
        }

        this.hostName = hostName;
        this.minPartitionCount = minPartitionCount;
        this.maxPartitionCount = maxPartitionCount;
        this.leaseExpirationInterval = leaseExpirationInterval;
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
            // If other CFP instances are running, limit the number of expired leases to acquire to maximum 1 (non-greedy acquiring).
            if ((this.maxPartitionCount == 0 && partitionsNeededForMe <= 0) || (partitionsNeededForMe > 1 && workerToPartitionCount.size() > 1)) {
                partitionsNeededForMe = 1;
            }

            if (partitionsNeededForMe == 1) {
                // Try to minimize potential collisions between different CFP instances trying to pick the same lease.
                Random random = new Random();
                Lease expiredLease = expiredLeases.get(random.nextInt(expiredLeases.size()));
                this.logger.info("Found unused or expired lease {} (owner was {}); previous lease count for instance owner {} is {}, count of leases to target is {} and maxScaleCount {} ",
                    expiredLease.getLeaseToken(), expiredLease.getOwner(), this.hostName, myCount, partitionsNeededForMe, this.maxPartitionCount);

                return Collections.singletonList(expiredLease);
            } else {
                for (Lease lease : expiredLeases) {
                    this.logger.info("Found unused or expired lease {} (owner was {}); previous lease count for instance owner {} is {} and maxScaleCount {} ",
                        lease.getLeaseToken(), lease.getOwner(), this.hostName, myCount, this.maxPartitionCount);
                }
            }

            return expiredLeases.subList(0, Math.min(partitionsNeededForMe, expiredLeases.size()));
        }

        if (partitionsNeededForMe <= 0)
            return new ArrayList<Lease>();

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
