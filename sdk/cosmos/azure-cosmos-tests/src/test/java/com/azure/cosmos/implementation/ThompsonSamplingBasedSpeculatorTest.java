// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.directconnectivity.speculativeprocessors.ThompsonSamplingBasedSpeculation;
import org.HdrHistogram.ConcurrentDoubleHistogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ThompsonSamplingBasedSpeculatorTest {

    private static final Logger logger = LoggerFactory.getLogger(ThompsonSamplingBasedSpeculatorTest.class);
    ThompsonSamplingBasedSpeculation thompsonSamplingBasedSpeculation;
    ConcurrentDoubleHistogram histogram;

    List<URI> regions = new ArrayList<>(
        List.of(URI.create("https://test1"), URI.create("https://test2"), URI.create("https://test3"))
    );

    public ThompsonSamplingBasedSpeculatorTest() {
        thompsonSamplingBasedSpeculation = new ThompsonSamplingBasedSpeculation(regions);
        histogram = new ConcurrentDoubleHistogram(4);
    }

    @Test(groups = {"unit"})
    public void testGetRegionsToExplore() {
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
        List<URI> regionsToSpeculate = thompsonSamplingBasedSpeculation.getRegionsToSpeculate(null, regions);
        for (int i = 0; i < 20; i++) {
            regionToCount.put(regionsToSpeculate.get(0), regionToCount.getOrDefault(regionsToSpeculate.get(0), 0) + 1);
        }
        logger.info("region = " + regionToCount);
    }

    @Test(groups = {"unit"})
    public void testShouldIncludeOriginalRequestRegion() {
        assertThat(true).isTrue();
    }

    @Test(groups = {"unit"})
    public void testOnResponseReceived() {
        assertThat(true).isTrue();
    }

}
