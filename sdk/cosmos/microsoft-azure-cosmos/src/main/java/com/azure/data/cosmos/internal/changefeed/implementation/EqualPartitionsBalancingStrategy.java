// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.internal.changefeed.Lease;
import com.azure.data.cosmos.internal.changefeed.PartitionLoadBalancingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (hostName == null) throw new IllegalArgumentException("hostName");
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
        if (partitionCount <= 0)
            return new ArrayList<Lease>();

        int target = this.calculateTargetPartitionCount(partitionCount, workerCount);
        int myCount = workerToPartitionCount.get(this.hostName);
        int partitionsNeededForMe = target - myCount;

        /*
        Logger.InfoFormat(
            "Host '{0}' {1} partitions, {2} hosts, {3} available leases, target = {4}, min = {5}, max = {6}, mine = {7}, will try to take {8} lease(s) for myself'.",
            this.hostName,
            partitionCount,
            workerCount,
            expiredLeases.Count,
            target,
            this.minScaleCount,
            this.maxScaleCount,
            myCount,
            Math.Max(partitionsNeededForMe, 0));
            */

        if (partitionsNeededForMe <= 0)
            return new ArrayList<Lease>();

        if (expiredLeases.size() > 0) {
            return expiredLeases.subList(0, partitionsNeededForMe);
        }

        Lease stolenLease = getLeaseToSteal(workerToPartitionCount, target, partitionsNeededForMe, allPartitions);
        List<Lease> stolenLeases = new ArrayList<>();

        if (stolenLease == null) {
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
                // Logger.DebugFormat("Found unused or expired lease: {0}", lease);
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
        ZonedDateTime time = ZonedDateTime.parse(lease.getTimestamp());
        return time.plus(this.leaseExpirationInterval).isBefore(ZonedDateTime.now(ZoneId.of("UTC")));
    }
}
