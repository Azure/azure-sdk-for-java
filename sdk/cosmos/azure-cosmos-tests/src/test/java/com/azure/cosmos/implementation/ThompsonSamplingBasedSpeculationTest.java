// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.directconnectivity.speculativeprocessors.ThompsonSamplingBasedSpeculation;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import org.HdrHistogram.ConcurrentDoubleHistogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ThompsonSamplingBasedSpeculationTest {

    private static final Logger logger = LoggerFactory.getLogger(ThompsonSamplingBasedSpeculationTest.class);
    ThompsonSamplingBasedSpeculation thompsonSamplingBasedSpeculation;
    ConcurrentDoubleHistogram histogram;

    List<URI> regions = new ArrayList<>(
        ImmutableList.of(URI.create("https://test1"), URI.create("https://test2"), URI.create("https://test3"))
    );

    public ThompsonSamplingBasedSpeculationTest() {
        thompsonSamplingBasedSpeculation = new ThompsonSamplingBasedSpeculation(regions);
        histogram = new ConcurrentDoubleHistogram(4);
    }

    @Test(groups = {"unit"})
    public void testGetRegionsToExplore() {
        // test that getRegionsToExplore returns the regions to explore
        HashMap<URI, Integer> regionToCount = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            List<URI> regionsToExplore = thompsonSamplingBasedSpeculation.getRegionsForPureExploration();
            if (regionsToExplore.size() == 1) {
                regionToCount.put(regionsToExplore.get(0), regionToCount.getOrDefault(regionsToExplore.get(0), 0) + 1);
            } else {
                regionToCount.put(URI.create("empty"), regionToCount.getOrDefault(URI.create("empty"), 0) + 1);
            }
        }
        assertThat(regionToCount.get(regions.get(0))).isGreaterThan(0);
        logger.info("regionToCount = " + regionToCount);
    }

    @Test(groups = {"unit"})
    public void testGetRegionsToSpeculate() {
        HashMap<URI, Integer> regionToCount = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            List<URI> regionsToSpeculate = thompsonSamplingBasedSpeculation.getRegionsToSpeculate(null, regions, new PartitionKeyRangeIdentity("aa", String.valueOf(i)));
            if (!regionsToSpeculate.isEmpty()) {
                regionToCount.put(regionsToSpeculate.get(0), regionToCount.getOrDefault(regionsToSpeculate.get(0), 0) + 1);
            }
        }
        // First call to each partition, speculation should be empty
        assertThat(regionToCount.size()).isEqualTo(0);

        for (int i = 0; i < 10; i++) {
            List<URI> regionsToSpeculate = thompsonSamplingBasedSpeculation.getRegionsToSpeculate(null, regions, new PartitionKeyRangeIdentity("aa", String.valueOf(i)));
            if (!regionsToSpeculate.isEmpty()) {
                regionToCount.put(regionsToSpeculate.get(0), regionToCount.getOrDefault(regionsToSpeculate.get(0), 0) + 1);
            }
        }

        int returnedCount = regionToCount.values().stream().reduce(0, Integer::sum);

        // subsequent calls should always return a value
        assertThat(returnedCount).isEqualTo(10);

    }

    @Test(groups = {"unit"})
    public void testOnResponseReceived() {
        assertThat(true).isTrue();
    }

}
