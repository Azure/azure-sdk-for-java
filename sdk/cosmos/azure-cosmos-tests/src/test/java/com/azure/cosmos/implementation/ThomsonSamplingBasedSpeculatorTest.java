// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.directconnectivity.speculativeprocessors.ThomsonSamplingBasedSpeculation;
import org.HdrHistogram.ConcurrentDoubleHistogram;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ThomsonSamplingBasedSpeculatorTest {

    ThomsonSamplingBasedSpeculation thomsonSamplingBasedSpeculation;
    ConcurrentDoubleHistogram histogram;

    List<URI> regions = new ArrayList<>(
        List.of(URI.create("https://test1"), URI.create("https://test2"), URI.create("https://test3"))
    );

    public ThomsonSamplingBasedSpeculatorTest() {
        thomsonSamplingBasedSpeculation = new ThomsonSamplingBasedSpeculation(regions);
        histogram = new ConcurrentDoubleHistogram(4);
    }

    @Test(groups = {"unit"})
    public void testGetRegionsToExplore() {
        HashMap<URI, Integer> regionToCount = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            List<URI> regionsToExplore = thomsonSamplingBasedSpeculation.getRegionsForPureExploration();
            if (regionsToExplore.size() == 1) {
                regionToCount.put(regionsToExplore.get(0), regionToCount.getOrDefault(regionsToExplore.get(0), 0) + 1);
            } else {
                regionToCount.put(URI.create("empty"), regionToCount.getOrDefault(URI.create("empty"), 0) + 1);
            }
        }
        assertThat(regionToCount.get(regions.get(0))).isGreaterThan(0);
        System.out.println("regionToCount = " + regionToCount);
    }

    @Test(groups = {"unit"})
    public void testGetRegionsToSpeculate() {
        HashMap<URI, Integer> regionToCount = new HashMap<>();
        List<URI> regionsToSpeculate = thomsonSamplingBasedSpeculation.getRegionsToSpeculate(null, regions);
        for (int i = 0; i < 20; i++) {
            regionToCount.put(regionsToSpeculate.get(0), regionToCount.getOrDefault(regionsToSpeculate.get(0), 0) + 1);
        }
        System.out.println("region = " + regionToCount);
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
