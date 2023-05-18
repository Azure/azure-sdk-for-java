// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.speculativeprocessors;

import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.implementation.apachecommons.math.distribution.EnumeratedDistribution;
import com.azure.cosmos.implementation.apachecommons.math.distribution.EnumeratedIntegerDistribution;
import com.azure.cosmos.implementation.apachecommons.math.distribution.GammaDistribution;
import com.azure.cosmos.implementation.apachecommons.math.util.Pair;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThompsonSamplingBasedSpeculation implements SpeculativeProcessor {

    public static final float EXPLORE_PROBABILITY = 0.1f; // 10% of the time explore
    private static final int DEFAULT_WINDOW_SIZE = 10;
    private static final double LOCAL_BIAS = 0.7; // 70 % of the time explore local region
    private final List<URI> endpoints;

    private final Map<PartitionKeyRangeIdentity, Map<URI, Double>> partitionAlphaMap;
    private final Map<PartitionKeyRangeIdentity, Map<URI, Double>> partitionBetaMap;
    private final Map<PartitionKeyRangeIdentity, Map<URI, Double>> partitionThetaMap;
    private final Map<PartitionKeyRangeIdentity, Map<URI, Deque<Double>>> partitionRewardsMap;
    private final URI primaryRegion;
    private final EnumeratedDistribution<URI> armExplorationDist;
    private final EnumeratedIntegerDistribution shouldExploreDist;


    public ThompsonSamplingBasedSpeculation(List<URI> endpoints) {
        this.endpoints = endpoints;
        primaryRegion = endpoints.get(0);

        partitionAlphaMap = new HashMap<>();
        partitionBetaMap = new HashMap<>();
        partitionThetaMap = new HashMap<>();
        partitionRewardsMap = new HashMap<>();

        armExplorationDist = new EnumeratedDistribution<>(getPriorExplorationProbabilities(endpoints));
        shouldExploreDist = new EnumeratedIntegerDistribution(new int[]{0, 1}, new double[]{1 - EXPLORE_PROBABILITY, EXPLORE_PROBABILITY});
    }

    private void addNewPartition(PartitionKeyRangeIdentity partitionId, List<URI> endpoints) {
        int numRegions = endpoints.size();
        Map<URI, Double> aMap = new HashMap<>(numRegions);
        Map<URI, Double> bMap = new HashMap<>(numRegions);
        Map<URI, Double> tMap = new HashMap<>(numRegions);
        initializeMaps(aMap, endpoints, 1.0);
        initializeMaps(bMap, endpoints, 1.0);
        initializeMaps(tMap, endpoints, 0.0);

        Map<URI, Deque<Double>> rewardsMap = new HashMap<>(numRegions);
        for (URI endpoint : endpoints) {
            rewardsMap.put(endpoint, new ArrayDeque<>());
        }

        partitionAlphaMap.put(partitionId, aMap);
        partitionBetaMap.put(partitionId, bMap);
        partitionThetaMap.put(partitionId, tMap);
        partitionRewardsMap.put(partitionId, rewardsMap);

    }

    private void initializeMaps(Map<URI, Double> map, List<URI> endpoints, double value) {
        for (URI endpoint : endpoints) {
            map.put(endpoint, value);
        }
    }

    private List<Pair<URI, Double>> getPriorExplorationProbabilities(List<URI> endpoints) {
        List<Pair<URI, Double>> pairs = new ArrayList<>();
        int numRegions = endpoints.size();
        // Bias the first arm(local region) to be explored more often
        pairs.add(new Pair<>(primaryRegion, LOCAL_BIAS)); // local bias % of the time
        for (URI uri : endpoints) {
            if (uri.equals(primaryRegion)) {
                continue;
            }
            pairs.add(new Pair<>(uri, (1.0 - LOCAL_BIAS) / (numRegions - 1))); // remaining % of the time
        }
        return pairs;
    }

    public URI getSelection(PartitionKeyRangeIdentity partitionKeyRangeId) {
        if (!partitionRewardsMap.containsKey(partitionKeyRangeId)) {
            addNewPartition(partitionKeyRangeId, endpoints);
            return null;
        }

        Map<URI, Double> thetaMap = partitionThetaMap.get(partitionKeyRangeId);
        Map<URI, Double> alphaMap = partitionAlphaMap.get(partitionKeyRangeId);
        Map<URI, Double> betaMap = partitionBetaMap.get(partitionKeyRangeId);
        for (URI endpoint : endpoints) {
            thetaMap.put(endpoint, new GammaDistribution(alphaMap.get(endpoint), 1 / betaMap.get(endpoint)).sample());
        }
        return Collections.min(thetaMap.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    public void updateReward(URI uri, double latencyInMs, PartitionKeyRangeIdentity partitionKeyRangeId) {
        Map<URI, Deque<Double>> rewardsMap = partitionRewardsMap.get(partitionKeyRangeId);
        Map<URI, Double> alphaMap = partitionAlphaMap.get(partitionKeyRangeId);
        Map<URI, Double> betaMap = partitionBetaMap.get(partitionKeyRangeId);
        rewardsMap.get(uri).add(latencyInMs);
        //Update the posterior distribution for the parameters of the Gamma distribution for the selected arm
        if (rewardsMap.get(uri).size() > DEFAULT_WINDOW_SIZE) {
            double oldReward = rewardsMap.get(uri).pop();
            alphaMap.computeIfPresent(uri, (k, v) -> v - oldReward);
            betaMap.computeIfPresent(uri, (k, v) -> v - 1);
        }
        alphaMap.computeIfPresent(uri, (k, v) -> v + latencyInMs);
        betaMap.computeIfPresent(uri, (k, v) -> v + 1);
    }

    public URI getRegionToExplore() {
        // Should return the next region to explore. This is random with max weight on primary region
        return armExplorationDist.sample();
    }

    public boolean shouldExplore() {
        return shouldExploreDist.sample() == 1;
    }

    @Override
    public List<URI> getRegionsForPureExploration() {
        if (shouldExplore()) {
            return ImmutableList.of(getRegionToExplore());
        }
        return ImmutableList.of();
    }

    @Override
    public List<URI> getRegionsToSpeculate(CosmosEndToEndOperationLatencyPolicyConfig config, List<URI> availableReadEndpoints, PartitionKeyRangeIdentity partitionKeyRangeId) {
        if (getSelection(partitionKeyRangeId) == null) {
            return ImmutableList.of();
        }
        return ImmutableList.of(getSelection(partitionKeyRangeId));
    }

    @Override
    public Duration getThreshold(CosmosEndToEndOperationLatencyPolicyConfig config) {
        return Duration.ZERO;
    }

    @Override
    public Duration getThresholdStepDuration(CosmosEndToEndOperationLatencyPolicyConfig config, long stepNumber) {
        return Duration.ZERO;
    }

    @Override
    public boolean shouldIncludeOriginalRequestRegion() {
        return true;
    }

    @Override
    public void onResponseReceived(URI region, Duration latency, PartitionKeyRangeIdentity partitionKeyRangeId) {
        updateReward(region, latency.toMillis(), partitionKeyRangeId);
    }
}
