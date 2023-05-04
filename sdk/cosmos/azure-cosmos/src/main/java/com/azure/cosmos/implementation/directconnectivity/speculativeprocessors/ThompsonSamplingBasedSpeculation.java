// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.speculativeprocessors;

import com.azure.cosmos.CosmosE2EOperationRetryPolicyConfig;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.util.Pair;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ThompsonSamplingBasedSpeculation implements SpeculativeProcessor {

    public static final float EXPLORE_PROBABILITY = 0.1f;
    private static final int DEFAULT_WINDOW_SIZE = 10;
    private final Random random = new Random();
    private final Map<Integer, String> regionNameMap = new HashMap<>();
    private final List<URI> endpoints;

    private final Map<URI, Double> alphaMap;
    private final Map<URI, Double> betaMap;
    private final Map<URI, Double> thetaMap;
    private volatile Map<URI, Deque<Double>> rewardsMap;
    private final URI localRegion;
    private final EnumeratedDistribution<URI> armExplorationDist;
    private final EnumeratedIntegerDistribution shouldExploreDist;


    public ThompsonSamplingBasedSpeculation(List<URI> endpoints) {
        this.endpoints = endpoints;
        localRegion = endpoints.get(0);
        int numRegions = endpoints.size();
        alphaMap = new HashMap<>(numRegions);
        betaMap = new HashMap<>(numRegions);
        thetaMap = new HashMap<>(numRegions);

        initializeMaps(alphaMap, endpoints, 1.0);
        initializeMaps(betaMap, endpoints, 1.0);
        initializeMaps(thetaMap, endpoints, 0.0);

        // Initialize the sliding window of rewards for each arm
        rewardsMap = new HashMap<>(numRegions);
        for (URI endpoint : endpoints) {
            rewardsMap.put(endpoint, new ArrayDeque<>());
        }

        armExplorationDist = new EnumeratedDistribution<>(getPriorExplorationProbabilities1(endpoints));
        shouldExploreDist = new EnumeratedIntegerDistribution(new int[]{0, 1}, new double[]{1 - EXPLORE_PROBABILITY, EXPLORE_PROBABILITY});
    }

    private void initializeMaps(Map<URI, Double> map, List<URI> endpoints, double value) {
        for (URI endpoint : endpoints) {
            map.put(endpoint, value);
        }
    }

    private List<Pair<URI, Double>> getPriorExplorationProbabilities1(List<URI> endpoints) {
        List<Pair<URI, Double>> pairs = new ArrayList<>();
        int numRegions = endpoints.size();
        // Bias the first arm(local region) to be explored more often
        pairs.add(new Pair<>(localRegion, 0.7)); // 70% of the time
        for (URI uri : endpoints) {
            if (uri.equals(localRegion)) {
                continue;
            }
            pairs.add(new Pair<>(uri, 0.3 / (numRegions - 1))); // 30% of the time
        }
        return pairs;
    }

    public URI getSelection() {
        for (URI endpoint : endpoints) {
            thetaMap.put(endpoint, new GammaDistribution(alphaMap.get(endpoint), 1 / betaMap.get(endpoint)).sample());
        }
        return Collections.min(thetaMap.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    public void updateReward(URI uri, double latencyInMs) {
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
        // Should return the next arm to explore. This is random with max weight on local
        return armExplorationDist.sample();
    }

    public boolean shouldExplore() {
        return shouldExploreDist.sample() == 1;
    }

    @Override
    public List<URI> getRegionsForPureExploration() {
        if (shouldExplore()) {
            return List.of(getRegionToExplore());
        }
        return List.of();
    }

    @Override
    public List<URI> getRegionsToSpeculate(CosmosE2EOperationRetryPolicyConfig config, List<URI> availableReadEndpoints) {
        return List.of(getSelection());
    }

    @Override
    public Duration getThreshold(CosmosE2EOperationRetryPolicyConfig config) {
        return Duration.ZERO;
    }

    @Override
    public boolean shouldIncludeOriginalRequestRegion() {
        return true;
    }

    @Override
    public void onResponseReceived(URI region, Duration latency) {
        updateReward(region, latency.toMillis());
    }
}
