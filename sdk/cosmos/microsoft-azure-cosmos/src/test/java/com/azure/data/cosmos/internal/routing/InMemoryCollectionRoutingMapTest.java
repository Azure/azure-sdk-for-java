// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.routing;

import com.azure.data.cosmos.internal.PartitionKeyRange;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

public class InMemoryCollectionRoutingMapTest {

    static class ServerIdentityImp implements IServerIdentity {
        private int value;
        public ServerIdentityImp(int value) {
            this.value = value;
        }

        static ServerIdentityImp of(int value) {
            return new ServerIdentityImp(value);
        }
    }

    @Test(groups = { "unit" })
    public void collectionRoutingMap() {
        InMemoryCollectionRoutingMap routingMap = InMemoryCollectionRoutingMap
                .tryCreateCompleteRoutingMap(Arrays.asList(
                        new ImmutablePair<>(
                                new PartitionKeyRange("2", "0000000050", "0000000070"), ServerIdentityImp.of(2)),
                        new ImmutablePair<>(new PartitionKeyRange("0", "", "0000000030"),
                                            ServerIdentityImp.of(0)),
                        new ImmutablePair<>(
                                new PartitionKeyRange("1", "0000000030", "0000000050"), ServerIdentityImp.of(1)),
                        new ImmutablePair<>(new PartitionKeyRange("3", "0000000070", "FF"),
                                            ServerIdentityImp.of(3))),
                        StringUtils.EMPTY);

        assertThat("0").isEqualTo(routingMap.getOrderedPartitionKeyRanges().get(0).id());
        assertThat("1").isEqualTo(routingMap.getOrderedPartitionKeyRanges().get(1).id());
        assertThat("2").isEqualTo(routingMap.getOrderedPartitionKeyRanges().get(2).id());
        assertThat("3").isEqualTo(routingMap.getOrderedPartitionKeyRanges().get(3).id());


        assertThat("0").isEqualTo(routingMap.getRangeByEffectivePartitionKey("").id());
        assertThat("0").isEqualTo(routingMap.getRangeByEffectivePartitionKey("0000000000").id());
        assertThat("1").isEqualTo(routingMap.getRangeByEffectivePartitionKey("0000000030").id());
        assertThat("1").isEqualTo(routingMap.getRangeByEffectivePartitionKey("0000000031").id());
        assertThat("3").isEqualTo(routingMap.getRangeByEffectivePartitionKey("0000000071").id());

        assertThat("0").isEqualTo(routingMap.getRangeByPartitionKeyRangeId("0").id());
        assertThat("1").isEqualTo(routingMap.getRangeByPartitionKeyRangeId("1").id());

        assertThat(4).isEqualTo(
                routingMap
                        .getOverlappingRanges(Collections.singletonList(new Range<String>(PartitionKeyRange.MINIMUM_INCLUSIVE_EFFECTIVE_PARTITION_KEY,
                                PartitionKeyRange.MAXIMUM_EXCLUSIVE_EFFECTIVE_PARTITION_KEY, true, false)))
                        .size());
        assertThat(0).isEqualTo(
                routingMap
                        .getOverlappingRanges(Collections.singletonList(new Range<String>(PartitionKeyRange.MINIMUM_INCLUSIVE_EFFECTIVE_PARTITION_KEY,
                                PartitionKeyRange.MINIMUM_INCLUSIVE_EFFECTIVE_PARTITION_KEY, false, false)))
                        .size());

        Collection<PartitionKeyRange> partitionKeyRanges = routingMap
                .getOverlappingRanges(Collections.singletonList(new Range<String>("0000000040", "0000000040", true, true)));

        assertThat(1).isEqualTo(partitionKeyRanges.size());
        Iterator<PartitionKeyRange> iterator = partitionKeyRanges.iterator();
        assertThat("1").isEqualTo(iterator.next().id());

        Collection<PartitionKeyRange> partitionKeyRanges1 = routingMap
                .getOverlappingRanges(Arrays.asList(new Range<String>("0000000040", "0000000045", true, true),
                        new Range<String>("0000000045", "0000000046", true, true),
                        new Range<String>("0000000046", "0000000050", true, true)));

        assertThat(2).isEqualTo(partitionKeyRanges1.size());
        Iterator<PartitionKeyRange> iterator1 = partitionKeyRanges1.iterator();
        assertThat("1").isEqualTo(iterator1.next().id());
        assertThat("2").isEqualTo(iterator1.next().id());
    }

    @Test(groups = { "unit" }, expectedExceptions = IllegalStateException.class)
    public void invalidRoutingMap() {
        InMemoryCollectionRoutingMap.tryCreateCompleteRoutingMap(Arrays.asList(
                new ImmutablePair<>(new PartitionKeyRange("1", "0000000020", "0000000030"),
                                    ServerIdentityImp.of(2)),
                new ImmutablePair<>(new PartitionKeyRange("2", "0000000025", "0000000035"),
                                    ServerIdentityImp.of(2))),
                StringUtils.EMPTY);
    }

    @Test(groups = { "unit" })
    public void incompleteRoutingMap() {
        InMemoryCollectionRoutingMap routingMap = InMemoryCollectionRoutingMap
                .tryCreateCompleteRoutingMap(Arrays.asList(
                        new ImmutablePair<>(new PartitionKeyRange("2", "", "0000000030"),
                                            ServerIdentityImp.of(2)),
                        new ImmutablePair<>(new PartitionKeyRange("3", "0000000031", "FF"),
                                            ServerIdentityImp.of(2))),
                        StringUtils.EMPTY);

        assertThat(routingMap).isNull();

        routingMap = InMemoryCollectionRoutingMap.tryCreateCompleteRoutingMap(Arrays.asList(
                new ImmutablePair<>(new PartitionKeyRange("2", "", "0000000030"), ServerIdentityImp.of(2)),
                new ImmutablePair<>(new PartitionKeyRange("3", "0000000030", "FF"), ServerIdentityImp.of(2))),
                StringUtils.EMPTY);

        assertThat(routingMap).isNotNull();
    }

    @Test(groups = {"unit"})
    public void goneRanges() {
        CollectionRoutingMap routingMap = InMemoryCollectionRoutingMap.tryCreateCompleteRoutingMap(
            ImmutableList.of(
                new ImmutablePair(new PartitionKeyRange("2", "", "0000000030", ImmutableList.of("1", "0")), null),
                new ImmutablePair(new PartitionKeyRange("3", "0000000030", "0000000032", ImmutableList.of("5")), null),
                new ImmutablePair(new PartitionKeyRange("4", "0000000032", "FF"), null)),
            StringUtils.EMPTY);

        assertThat(routingMap.IsGone("1")).isTrue();
        assertThat(routingMap.IsGone("0")).isTrue();
        assertThat(routingMap.IsGone("5")).isTrue();

        assertThat(routingMap.IsGone("2")).isFalse();
        assertThat(routingMap.IsGone("3")).isFalse();
        assertThat(routingMap.IsGone("4")).isFalse();
        assertThat(routingMap.IsGone("100")).isFalse();
    }
    
    @Test(groups = {"unit"})
    public void tryCombineRanges() {
        CollectionRoutingMap routingMap = InMemoryCollectionRoutingMap.tryCreateCompleteRoutingMap(
            ImmutableList.of(
                new ImmutablePair(
                    new PartitionKeyRange(
                        "2",
                        "0000000050",
                        "0000000070"),
                    null),

                new ImmutablePair(
                    new PartitionKeyRange(
                        "0",
                        "",
                        "0000000030"),
                    null),

                new ImmutablePair(
                    new PartitionKeyRange(
                        "1",
                        "0000000030",
                        "0000000050"),
                    null),

                new ImmutablePair(
                    new PartitionKeyRange(
                        "3",
                        "0000000070",
                        "FF"),
                    null)
            ), StringUtils.EMPTY);

        CollectionRoutingMap newRoutingMap = routingMap.tryCombine(
            ImmutableList.of(
                new ImmutablePair(
                    new PartitionKeyRange(
                        "4",
                        "",
                        "0000000010",
                        ImmutableList.of("0")
                    ),
                    null),

                new ImmutablePair(
                    new PartitionKeyRange(
                        "5",
                        "0000000010",
                        "0000000030",
                        ImmutableList.of("0")
                    ),
                    null)
            ));

        assertThat(newRoutingMap).isNotNull();

        newRoutingMap = routingMap.tryCombine(
            ImmutableList.of(
                new ImmutablePair(
                    new PartitionKeyRange(
                        "6",
                        "",
                        "0000000005",
                        ImmutableList.of("0", "4")
                    ),
                    null),

                new ImmutablePair(
                    new PartitionKeyRange(
                        "7",
                        "0000000005",
                        "0000000010",
                        ImmutableList.of("0", "4")
                    ),
                    null),

                new ImmutablePair(
                    new PartitionKeyRange(
                        "8",
                        "0000000010",
                        "0000000015",
                        ImmutableList.of("0", "5")
                    ),
                    null),

                new ImmutablePair(
                    new PartitionKeyRange(
                        "9",
                        "0000000015",
                        "0000000030",
                        ImmutableList.of("0", "5")
                    ),
                    null)
            ));

        assertThat(newRoutingMap).isNotNull();

        newRoutingMap = routingMap.tryCombine(
            ImmutableList.of(
                new ImmutablePair(
                    new PartitionKeyRange(
                        "10",
                        "",
                        "0000000002",
                        ImmutableList.of("0", "4", "6")
                    ),
                    null)
            ));

        assertThat(newRoutingMap).isNull();
    }
}
