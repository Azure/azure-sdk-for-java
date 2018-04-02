/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.internal.routing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.PartitionKeyRange;
import com.microsoft.azure.cosmosdb.internal.routing.CollectionRoutingMap;
import com.microsoft.azure.cosmosdb.internal.routing.InMemoryCollectionRoutingMap;
import com.microsoft.azure.cosmosdb.internal.routing.Range;
import com.microsoft.azure.cosmosdb.internal.routing.RoutingMapProvider;
import com.microsoft.azure.cosmosdb.internal.routing.RoutingMapProviderHelper;

public class RoutingMapProviderHelperTest {
    private static final MockRoutingMapProvider ROUTING_MAP_PROVIDER = new MockRoutingMapProvider(
            Arrays.asList(new PartitionKeyRange("0", "", "000A"), new PartitionKeyRange("1", "000A", "000D"),
                    new PartitionKeyRange("2", "000D", "0012"), new PartitionKeyRange("3", "0012", "0015"),
                    new PartitionKeyRange("4", "0015", "0020"), new PartitionKeyRange("5", "0020", "0040"),
                    new PartitionKeyRange("6", "0040", "FF")));

    private static class MockRoutingMapProvider implements RoutingMapProvider {
        private final CollectionRoutingMap routingMap;

        public MockRoutingMapProvider(Collection<PartitionKeyRange> ranges) {
            List<ImmutablePair<PartitionKeyRange, Boolean>> pairs = new ArrayList<ImmutablePair<PartitionKeyRange, Boolean>>(
                    ranges.size());
            for (PartitionKeyRange range : ranges) {
                pairs.add(new ImmutablePair<PartitionKeyRange, Boolean>(range, true));
            }

            this.routingMap = InMemoryCollectionRoutingMap.tryCreateCompleteRoutingMap(pairs, StringUtils.EMPTY);
        }

        @Override
        public Collection<PartitionKeyRange> getOverlappingRanges(String collectionIdOrNameBasedLink,
                Range<String> range, boolean forceRefresh) {
            return this.routingMap.getOverlappingRanges(range);
        }

        @Override
        public PartitionKeyRange tryGetRangeByEffectivePartitionKey(String collectionRid, String effectivePartitionKey) {
            return null;
        }

        @Override
        public PartitionKeyRange getPartitionKeyRangeById(String collectionLink, String partitionKeyRangeId, boolean forceRefresh) {
            return null;
        }
    }

    @Test(groups = { "simple" }, expectedExceptions = IllegalArgumentException.class)
    public void nonSortedRanges() {
        RoutingMapProviderHelper.getOverlappingRanges(ROUTING_MAP_PROVIDER, "dbs/db1/colls/coll1",
                Arrays.asList(new Range<String>("0B", "0B", true, true), new Range<String>("0A", "0A", true, true)));
    }

    @Test(groups = { "simple" }, expectedExceptions = IllegalArgumentException.class)
    public void overlappingRanges1() {
        RoutingMapProviderHelper.getOverlappingRanges(ROUTING_MAP_PROVIDER, "dbs/db1/colls/coll1",
                Arrays.asList(new Range<String>("0A", "0D", true, true), new Range<String>("0B", "0E", true, true)));
    }

    @Test(groups = { "simple" }, expectedExceptions = IllegalArgumentException.class)
    public void overlappingRanges2() {
        RoutingMapProviderHelper.getOverlappingRanges(ROUTING_MAP_PROVIDER, "dbs/db1/colls/coll1",
                Arrays.asList(new Range<String>("0A", "0D", true, true), new Range<String>("0D", "0E", true, true)));
    }

    @Test(groups = { "simple" })
    public void getOverlappingRanges() {
        Collection<PartitionKeyRange> ranges = RoutingMapProviderHelper.getOverlappingRanges(ROUTING_MAP_PROVIDER,
                "dbs/db1/colls/coll1",
                Arrays.asList(new Range<String>("000B", "000E", true, false),
                        new Range<String>("000E", "000F", true, false), new Range<String>("000F", "0010", true, true),
                        new Range<String>("0015", "0015", true, true)));

        Function<PartitionKeyRange, String> func = new Function<PartitionKeyRange, String>() {
            @Override
            public String apply(PartitionKeyRange range) {
                return range.getId();
            }
        };

        assertThat("1,2,4").isEqualTo(ranges.stream().map(func).collect(Collectors.joining(",")));

        // query for minimal point
        ranges = RoutingMapProviderHelper.getOverlappingRanges(ROUTING_MAP_PROVIDER, "dbs/db1/colls/coll1",
                Arrays.asList(new Range<String>("", "", true, true)));

        assertThat("0").isEqualTo(ranges.stream().map(func).collect(Collectors.joining(",")));

        // query for empty range
        ranges = RoutingMapProviderHelper.getOverlappingRanges(ROUTING_MAP_PROVIDER, "dbs/db1/colls/coll1",
                Arrays.asList(new Range<String>("", "", true, false)));

        assertThat(0).isEqualTo(ranges.size());

        // entire range
        ranges = RoutingMapProviderHelper.getOverlappingRanges(ROUTING_MAP_PROVIDER, "dbs/db1/colls/coll1",
                Arrays.asList(new Range<String>("", "FF", true, false)));

        assertThat("0,1,2,3,4,5,6").isEqualTo(ranges.stream().map(func).collect(Collectors.joining(",")));

        // matching range
        ranges = RoutingMapProviderHelper.getOverlappingRanges(ROUTING_MAP_PROVIDER, "dbs/db1/colls/coll1",
                Arrays.asList(new Range<String>("0012", "0015", true, false)));

        assertThat("3").isEqualTo(ranges.stream().map(func).collect(Collectors.joining(",")));

        // matching range with empty ranges
        ranges = RoutingMapProviderHelper.getOverlappingRanges(ROUTING_MAP_PROVIDER, "dbs/db1/colls/coll1",
                Arrays.asList(new Range<String>("", "", true, false), new Range<String>("0012", "0015", true, false)));

        assertThat("3").isEqualTo(ranges.stream().map(func).collect(Collectors.joining(",")));

        // matching range and a little bit more.
        ranges = RoutingMapProviderHelper.getOverlappingRanges(ROUTING_MAP_PROVIDER, "dbs/db1/colls/coll1",
                Arrays.asList(new Range<String>("0012", "0015", false, true)));

        assertThat("3,4").isEqualTo(ranges.stream().map(func).collect(Collectors.joining(",")));
    }
}
