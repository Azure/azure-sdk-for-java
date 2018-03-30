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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.PartitionKeyRange;
import com.microsoft.azure.cosmosdb.internal.routing.InMemoryCollectionRoutingMap;
import com.microsoft.azure.cosmosdb.internal.routing.Range;

public class InMemoryCollectionRoutingMapTest {
    @Test(groups = { "simple" })
    public void collectionRoutingMap() {
        InMemoryCollectionRoutingMap<Integer> routingMap = InMemoryCollectionRoutingMap
                .tryCreateCompleteRoutingMap(Arrays.asList(
                        new ImmutablePair<PartitionKeyRange, Integer>(
                                new PartitionKeyRange("2", "0000000050", "0000000070"), 2),
                        new ImmutablePair<PartitionKeyRange, Integer>(new PartitionKeyRange("0", "", "0000000030"), 0),
                        new ImmutablePair<PartitionKeyRange, Integer>(
                                new PartitionKeyRange("1", "0000000030", "0000000050"), 1),
                        new ImmutablePair<PartitionKeyRange, Integer>(new PartitionKeyRange("3", "0000000070", "FF"),
                                3)),
                        StringUtils.EMPTY);

        assertThat("0").isEqualTo(routingMap.getOrderedPartitionKeyRanges().get(0).getId());
        assertThat("1").isEqualTo(routingMap.getOrderedPartitionKeyRanges().get(1).getId());
        assertThat("2").isEqualTo(routingMap.getOrderedPartitionKeyRanges().get(2).getId());
        assertThat("3").isEqualTo(routingMap.getOrderedPartitionKeyRanges().get(3).getId());

        assertThat((Integer) 0).isEqualTo(routingMap.getOrderedPartitionInfo().get(0));
        assertThat((Integer) 1).isEqualTo(routingMap.getOrderedPartitionInfo().get(1));
        assertThat((Integer) 2).isEqualTo(routingMap.getOrderedPartitionInfo().get(2));
        assertThat((Integer) 3).isEqualTo(routingMap.getOrderedPartitionInfo().get(3));

        assertThat("0").isEqualTo(routingMap.getRangeByEffectivePartitionKey("").getId());
        assertThat("0").isEqualTo(routingMap.getRangeByEffectivePartitionKey("0000000000").getId());
        assertThat("1").isEqualTo(routingMap.getRangeByEffectivePartitionKey("0000000030").getId());
        assertThat("1").isEqualTo(routingMap.getRangeByEffectivePartitionKey("0000000031").getId());
        assertThat("3").isEqualTo(routingMap.getRangeByEffectivePartitionKey("0000000071").getId());

        assertThat("0").isEqualTo(routingMap.getRangeByPartitionKeyRangeId("0").getId());
        assertThat("1").isEqualTo(routingMap.getRangeByPartitionKeyRangeId("1").getId());

        assertThat(4).isEqualTo(
                routingMap
                        .getOverlappingRanges(Arrays
                                .asList(new Range<String>(PartitionKeyRange.MINIMUM_INCLUSIVE_EFFECTIVE_PARTITION_KEY,
                                        PartitionKeyRange.MAXIMUM_EXCLUSIVE_EFFECTIVE_PARTITION_KEY, true, false)))
                        .size());
        assertThat(0).isEqualTo(
                routingMap
                        .getOverlappingRanges(Arrays
                                .asList(new Range<String>(PartitionKeyRange.MINIMUM_INCLUSIVE_EFFECTIVE_PARTITION_KEY,
                                        PartitionKeyRange.MINIMUM_INCLUSIVE_EFFECTIVE_PARTITION_KEY, false, false)))
                        .size());

        Collection<PartitionKeyRange> partitionKeyRanges = routingMap
                .getOverlappingRanges(Arrays.asList(new Range<String>("0000000040", "0000000040", true, true)));

        assertThat(1).isEqualTo(partitionKeyRanges.size());
        Iterator<PartitionKeyRange> iterator = partitionKeyRanges.iterator();
        assertThat("1").isEqualTo(iterator.next().getId());

        Collection<PartitionKeyRange> partitionKeyRanges1 = routingMap
                .getOverlappingRanges(Arrays.asList(new Range<String>("0000000040", "0000000045", true, true),
                        new Range<String>("0000000045", "0000000046", true, true),
                        new Range<String>("0000000046", "0000000050", true, true)));

        assertThat(2).isEqualTo(partitionKeyRanges1.size());
        Iterator<PartitionKeyRange> iterator1 = partitionKeyRanges1.iterator();
        assertThat("1").isEqualTo(iterator1.next().getId());
        assertThat("2").isEqualTo(iterator1.next().getId());
    }

    @Test(groups = { "simple" }, expectedExceptions = IllegalStateException.class)
    public void invalidRoutingMap() {
        InMemoryCollectionRoutingMap.tryCreateCompleteRoutingMap(Arrays.asList(
                new ImmutablePair<PartitionKeyRange, Integer>(new PartitionKeyRange("1", "0000000020", "0000000030"),
                        2),
                new ImmutablePair<PartitionKeyRange, Integer>(new PartitionKeyRange("2", "0000000025", "0000000035"),
                        2)),
                StringUtils.EMPTY);
    }

    @Test(groups = { "simple" })
    public void incompleteRoutingMap() {
        InMemoryCollectionRoutingMap<Integer> routingMap = InMemoryCollectionRoutingMap
                .tryCreateCompleteRoutingMap(Arrays.asList(
                        new ImmutablePair<PartitionKeyRange, Integer>(new PartitionKeyRange("2", "", "0000000030"), 2),
                        new ImmutablePair<PartitionKeyRange, Integer>(new PartitionKeyRange("3", "0000000031", "FF"),
                                2)),
                        StringUtils.EMPTY);

        assertThat(routingMap).isNull();;

        routingMap = InMemoryCollectionRoutingMap.tryCreateCompleteRoutingMap(Arrays.asList(
                new ImmutablePair<PartitionKeyRange, Integer>(new PartitionKeyRange("2", "", "0000000030"), 2),
                new ImmutablePair<PartitionKeyRange, Integer>(new PartitionKeyRange("2", "0000000030", "FF"), 2)),
                StringUtils.EMPTY);

        assertThat(routingMap).isNotNull();
    }
}
