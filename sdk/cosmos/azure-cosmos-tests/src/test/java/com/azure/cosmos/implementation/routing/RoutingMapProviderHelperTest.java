// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.implementation.IRoutingMapProvider;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class RoutingMapProviderHelperTest {
    private static final MockRoutingMapProvider ROUTING_MAP_PROVIDER = new MockRoutingMapProvider(
            Arrays.asList(new PartitionKeyRange("0", "", "000A"), new PartitionKeyRange("1", "000A", "000D"),
                    new PartitionKeyRange("2", "000D", "0012"), new PartitionKeyRange("3", "0012", "0015"),
                    new PartitionKeyRange("4", "0015", "0020"), new PartitionKeyRange("5", "0020", "0040"),
                    new PartitionKeyRange("6", "0040", "FF")));

    private static class MockRoutingMapProvider implements RoutingMapProvider {
        private final CollectionRoutingMap routingMap;

        public MockRoutingMapProvider(Collection<PartitionKeyRange> ranges) {
            List<ImmutablePair<PartitionKeyRange, IServerIdentity>> pairs = new ArrayList<>(
                    ranges.size());
            for (PartitionKeyRange range : ranges) {
                pairs.add(new ImmutablePair<>(range, null));
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

    private class MockIRoutingMapProvider implements IRoutingMapProvider {
        private final CollectionRoutingMap routingMap;

        public MockIRoutingMapProvider(List<PartitionKeyRange> ranges) {
            List<ImmutablePair<PartitionKeyRange, IServerIdentity>> pairs = new ArrayList<>(
                ranges.size());
            for (PartitionKeyRange range : ranges) {
                pairs.add(new ImmutablePair<>(range, null));
            }

            this.routingMap = InMemoryCollectionRoutingMap.tryCreateCompleteRoutingMap(pairs, StringUtils.EMPTY);
        }

        @Override
        public Mono<Utils.ValueHolder<List<PartitionKeyRange>>> tryGetOverlappingRangesAsync(
            MetadataDiagnosticsContext metaDataDiagnosticsContext, String collectionResourceId,
            Range<String> range, boolean forceRefresh, Map<String, Object> properties) {
            Utils.ValueHolder<List<PartitionKeyRange>> valueHolder = new Utils.ValueHolder<>();
            valueHolder.v = this.routingMap.getOverlappingRanges(range);
            return Mono.just(valueHolder);
        }

        @Override
        public Mono<Utils.ValueHolder<PartitionKeyRange>> tryGetPartitionKeyRangeByIdAsync(
            MetadataDiagnosticsContext metaDataDiagnosticsContext, String collectionResourceId,
            String partitionKeyRangeId, boolean forceRefresh, Map<String, Object> properties) {
            return null;
        }
    }


    @Test(groups = { "unit" }, expectedExceptions = IllegalArgumentException.class)
    public void nonSortedRanges() {
        RoutingMapProviderHelper.getOverlappingRanges(ROUTING_MAP_PROVIDER, "dbs/db1/colls/coll1",
                Arrays.asList(new Range<String>("0B", "0B", true, true), new Range<String>("0A", "0A", true, true)));
    }

    @Test(groups = { "unit" }, expectedExceptions = IllegalArgumentException.class)
    public void overlappingRanges1() {
        RoutingMapProviderHelper.getOverlappingRanges(ROUTING_MAP_PROVIDER, "dbs/db1/colls/coll1",
                Arrays.asList(new Range<String>("0A", "0D", true, true), new Range<String>("0B", "0E", true, true)));
    }

    @Test(groups = { "unit" }, expectedExceptions = IllegalArgumentException.class)
    public void overlappingRanges2() {
        RoutingMapProviderHelper.getOverlappingRanges(ROUTING_MAP_PROVIDER, "dbs/db1/colls/coll1",
                Arrays.asList(new Range<String>("0A", "0D", true, true), new Range<String>("0D", "0E", true, true)));
    }

    @Test(groups = { "unit" })
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
                Collections.singletonList(new Range<String>("", "", true, true)));

        assertThat("0").isEqualTo(ranges.stream().map(func).collect(Collectors.joining(",")));

        // query for empty range
        ranges = RoutingMapProviderHelper.getOverlappingRanges(ROUTING_MAP_PROVIDER, "dbs/db1/colls/coll1",
                Collections.singletonList(new Range<String>("", "", true, false)));

        assertThat(0).isEqualTo(ranges.size());

        // entire range
        ranges = RoutingMapProviderHelper.getOverlappingRanges(ROUTING_MAP_PROVIDER, "dbs/db1/colls/coll1",
                Collections.singletonList(new Range<String>("", "FF", true, false)));

        assertThat("0,1,2,3,4,5,6").isEqualTo(ranges.stream().map(func).collect(Collectors.joining(",")));

        // matching range
        ranges = RoutingMapProviderHelper.getOverlappingRanges(ROUTING_MAP_PROVIDER, "dbs/db1/colls/coll1",
                Collections.singletonList(new Range<String>("0012", "0015", true, false)));

        assertThat("3").isEqualTo(ranges.stream().map(func).collect(Collectors.joining(",")));

        // matching range with empty ranges
        ranges = RoutingMapProviderHelper.getOverlappingRanges(ROUTING_MAP_PROVIDER, "dbs/db1/colls/coll1",
                Arrays.asList(new Range<String>("", "", true, false), new Range<String>("0012", "0015", true, false)));

        assertThat("3").isEqualTo(ranges.stream().map(func).collect(Collectors.joining(",")));

        // matching range and a little bit more.
        ranges = RoutingMapProviderHelper.getOverlappingRanges(ROUTING_MAP_PROVIDER, "dbs/db1/colls/coll1",
                Collections.singletonList(new Range<String>("0012", "0015", false, true)));

        assertThat("3,4").isEqualTo(ranges.stream().map(func).collect(Collectors.joining(",")));
    }

    @Test(groups = {"unit"})
    public void getOverlappingRangesWithList() {

        Function<PartitionKeyRange, String> func = new Function<PartitionKeyRange, String>() {
            @Override
            public String apply(PartitionKeyRange range) {
                return range.getId();
            }
        };

        List<PartitionKeyRange> rangeList = Arrays.asList(new PartitionKeyRange("0", "", "000A"),
                                                          new PartitionKeyRange("1", "000A", "000D"),
                                                          new PartitionKeyRange("2", "000D", "0012"),
                                                          new PartitionKeyRange("3", "0012", "0015"),
                                                          new PartitionKeyRange("4", "0015", "0020"),
                                                          new PartitionKeyRange("5", "0020", "0040"),
                                                          new PartitionKeyRange("6", "0040", "FF"));

        IRoutingMapProvider routingMapProviderMock = new MockIRoutingMapProvider(rangeList);

        Mono<List<PartitionKeyRange>> overlappingRanges;
        overlappingRanges = RoutingMapProviderHelper.getOverlappingRanges(routingMapProviderMock,
                                                                          "coll1",
                                                                          Arrays.asList(new Range<String>("000D", "0012", true, false),
                                                                                        new Range<String>("0012", "0015", true, false),
                                                                                        new Range<>("0015", "0020", true, false)));
        assertThat("2,3,4").isEqualTo(overlappingRanges.block().stream().map(func).collect(Collectors.joining(",")));

        overlappingRanges = RoutingMapProviderHelper.getOverlappingRanges(routingMapProviderMock,
                                                                          "coll1",
                                                                          Arrays.asList(new Range<String>("000D", "0012", true, false)));
        assertThat("2").isEqualTo(overlappingRanges.block().stream().map(func).collect(Collectors.joining(",")));

        //duplicate ranges
        List<Range<String>> sortedRanges = Arrays.asList(new Range<>("", "FF", true, false),
                                                         new Range<>("", "FF", true, false));
        overlappingRanges = RoutingMapProviderHelper.getOverlappingRanges(routingMapProviderMock, "coll1", sortedRanges);
        List<PartitionKeyRange> overLappingRangeList = overlappingRanges.block();
        assertThat(overLappingRangeList).isNotNull();
        assertThat(7).isEqualTo(overLappingRangeList.size());
        assertThat("0,1,2,3,4,5,6").isEqualTo(overLappingRangeList.stream().map(func).collect(Collectors.joining(",")));

        sortedRanges = Arrays.asList(new Range<>("", "000D", true, false),
                                                         new Range<>("", "000D", true, false));
        overlappingRanges = RoutingMapProviderHelper.getOverlappingRanges(routingMapProviderMock, "coll1", sortedRanges);
        overLappingRangeList = overlappingRanges.block();
        assertThat(overLappingRangeList).isNotNull();
        assertThat(2).isEqualTo(overLappingRangeList.size());
        assertThat("0,1").isEqualTo(overLappingRangeList.stream().map(func).collect(Collectors.joining(",")));
    }
}
