package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IRoutingMapProvider;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.PartitionKeyRangeGoneException;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalUtils;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.PartitionKeyDefinition;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

public class FeedRangeTest {

    @Test(groups = "unit")
    public void feedRangeEPK_Range() {
        Range<String> range = new Range<>("AA", "BB", true, false);
        FeedRangeEpkImpl feedRangeEPK = new FeedRangeEpkImpl(range);
        assertThat(range).isEqualTo(feedRangeEPK.getRange());
    }

    @Test(groups = "unit")
    public void feedRangePK_PK()
    {
        PartitionKeyInternal partitionKey = PartitionKeyInternalUtils.createPartitionKeyInternal("Test");
        FeedRangePartitionKeyImpl feedRangePartitionKey = new FeedRangePartitionKeyImpl(partitionKey);
        assertThat(partitionKey).isEqualTo(feedRangePartitionKey.getPartitionKeyInternal());
    }

    @Test(groups = "unit")
    public void FeedRangePKRangeId_PKRange()
    {
        String pkRangeId = UUID.randomUUID().toString();
        FeedRangePartitionKeyRangeImpl feedRangePartitionKeyRange = new FeedRangePartitionKeyRangeImpl(pkRangeId);
        assertThat(pkRangeId).isEqualTo(feedRangePartitionKeyRange.getPartitionKeyRangeId());
    }

    @Test(groups = "unit")
    public void FeedRangeEPK_GetEffectiveRangesAsync()
    {
        Range<String> range = new Range<>("AA", "BB", true, false);
        FeedRangeEpkImpl FeedRangeEpk = new FeedRangeEpkImpl(range);

        IRoutingMapProvider routingMapProviderMock = Mockito.mock(IRoutingMapProvider.class);
        StepVerifier
            .create(
                FeedRangeEpk.getEffectiveRangesAsync(
                    routingMapProviderMock,
                    null,
                    null))
            .recordWith(ArrayList::new)
            .expectNextCount(1)
            .consumeRecordedWith(r -> {
                assertThat(r).hasSize(1);
                assertThat(new ArrayList<>(r).get(0))
                    .hasSize(1)
                    .contains(range);
            })
            .verifyComplete();
    }

    @Test(groups = "unit")
    public void FeedRangePK_GetEffectiveRangesAsync()
    {
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        partitionKeyDefinition.getPaths().add("/id");
        PartitionKeyInternal partitionKey = PartitionKeyInternalUtils.createPartitionKeyInternal("Test");
        FeedRangePartitionKeyImpl feedRangePartitionKey = new FeedRangePartitionKeyImpl(partitionKey);
        Range<String> range = Range.getPointRange(
            partitionKey.getEffectivePartitionKeyString(partitionKey, partitionKeyDefinition));

        IRoutingMapProvider routingMapProviderMock = Mockito.mock(IRoutingMapProvider.class);
        StepVerifier
            .create(
                feedRangePartitionKey.getEffectiveRangesAsync(
                    routingMapProviderMock,
                    null,
                    partitionKeyDefinition))
            .recordWith(ArrayList::new)
            .expectNextCount(1)
            .consumeRecordedWith(r -> {
                assertThat(r).hasSize(1);
                assertThat(new ArrayList<>(r).get(0))
                    .hasSize(1)
                    .contains(range);
            })
            .verifyComplete();
    }

    @Test(groups = "unit")
    public void FeedRangePKRangeId_GetEffectiveRangesAsync()
    {
        String pkRangeId = UUID.randomUUID().toString();
        PartitionKeyRange partitionKeyRange = new PartitionKeyRange()
            .setId(pkRangeId)
            .setMinInclusive("AA")
            .setMaxExclusive("BB");

        FeedRangePartitionKeyRangeImpl feedRangePartitionKeyRange =
            new FeedRangePartitionKeyRangeImpl(partitionKeyRange.getId());
        IRoutingMapProvider routingMapProviderMock = Mockito.mock(IRoutingMapProvider.class);
        when(
            routingMapProviderMock.tryGetPartitionKeyRangeByIdAsync(
                any(MetadataDiagnosticsContext.class),
                anyString(),
                eq(partitionKeyRange.getId()),
                anyBoolean(),
                anyMapOf(String.class, Object.class)))
            .thenReturn(Mono.just(Utils.ValueHolder.initialize(partitionKeyRange)));

        StepVerifier
            .create(
                feedRangePartitionKeyRange.getEffectiveRangesAsync(
                    routingMapProviderMock, null, null))
            .recordWith(ArrayList::new)
            .expectNextCount(1)
            .consumeRecordedWith(r -> {
                assertThat(r).hasSize(1);
                UnmodifiableList<Range<String>> ranges = new ArrayList<>(r).get(0);
                assertThat(ranges).hasSize(1);
                Range<String> range = ranges.get(0);
                assertThat(range).isNotNull();
                assertThat(range.getMin()).isEqualTo(partitionKeyRange.getMinInclusive());
                assertThat(range.getMax()).isEqualTo(partitionKeyRange.getMaxExclusive());
                assertThat(range.getMin()).isEqualTo(partitionKeyRange.toRange().getMin());
                assertThat(range.getMax()).isEqualTo(partitionKeyRange.toRange().getMax());
            })
            .verifyComplete();

        Mockito
            .verify(routingMapProviderMock, Mockito.times(1))
            .tryGetPartitionKeyRangeByIdAsync(
                null,
                null,
                partitionKeyRange.getId(),
                false,
                null);
    }

    @Test(groups = "unit")
    public void FeedRangePKRangeId_GetEffectiveRangesAsync_Refresh()
    {
        String pkRangeId = UUID.randomUUID().toString();
        PartitionKeyRange partitionKeyRange = new PartitionKeyRange()
            .setId(pkRangeId)
            .setMinInclusive("AA")
            .setMaxExclusive("BB");

        FeedRangePartitionKeyRangeImpl feedRangePartitionKeyRange =
            new FeedRangePartitionKeyRangeImpl(partitionKeyRange.getId());
        IRoutingMapProvider routingMapProviderMock = Mockito.mock(IRoutingMapProvider.class);
        when(
            routingMapProviderMock.tryGetPartitionKeyRangeByIdAsync(
                any(MetadataDiagnosticsContext.class),
                anyString(),
                eq(partitionKeyRange.getId()),
                anyBoolean(),
                anyMapOf(String.class, Object.class)))
            .thenReturn(Mono.just(Utils.ValueHolder.initialize(null)))
            .thenReturn(Mono.just(Utils.ValueHolder.initialize(partitionKeyRange)));

        StepVerifier
            .create(
                feedRangePartitionKeyRange.getEffectiveRangesAsync(
                    routingMapProviderMock, null, null))
            .recordWith(ArrayList::new)
            .expectNextCount(1)
            .consumeRecordedWith(r -> {
                assertThat(r).hasSize(1);
                UnmodifiableList<Range<String>> ranges = new ArrayList<>(r).get(0);
                assertThat(ranges).hasSize(1);
                Range<String> range = ranges.get(0);
                assertThat(range).isNotNull();
                assertThat(range.getMin()).isEqualTo(partitionKeyRange.getMinInclusive());
                assertThat(range.getMax()).isEqualTo(partitionKeyRange.getMaxExclusive());
                assertThat(range.getMin()).isEqualTo(partitionKeyRange.toRange().getMin());
                assertThat(range.getMax()).isEqualTo(partitionKeyRange.toRange().getMax());
            })
            .verifyComplete();

        Mockito
            .verify(routingMapProviderMock, Mockito.times(2))
            .tryGetPartitionKeyRangeByIdAsync(
                any(MetadataDiagnosticsContext.class),
                anyString(),
                eq(partitionKeyRange.getId()),
                anyBoolean(),
                anyMapOf(String.class, Object.class));
    }

    @Test(groups = "unit")
    public void FeedRangePKRangeId_GetEffectiveRangesAsync_Null()
    {
        String pkRangeId = UUID.randomUUID().toString();
        PartitionKeyRange partitionKeyRange = new PartitionKeyRange()
            .setId(pkRangeId)
            .setMinInclusive("AA")
            .setMaxExclusive("BB");

        FeedRangePartitionKeyRangeImpl feedRangePartitionKeyRange =
            new FeedRangePartitionKeyRangeImpl(partitionKeyRange.getId());
        IRoutingMapProvider routingMapProviderMock = Mockito.mock(IRoutingMapProvider.class);
        when(
            routingMapProviderMock.tryGetPartitionKeyRangeByIdAsync(
                any(MetadataDiagnosticsContext.class),
                anyString(),
                eq(partitionKeyRange.getId()),
                anyBoolean(),
                anyMapOf(String.class, Object.class)))
            .thenReturn(Mono.just(Utils.ValueHolder.initialize(null)))
            .thenReturn(Mono.just(Utils.ValueHolder.initialize(null)));

        StepVerifier
            .create(
                feedRangePartitionKeyRange.getEffectiveRangesAsync(
                    routingMapProviderMock, null, null))
            .recordWith(ArrayList::new)
            .expectErrorSatisfies((e) -> {
                assertThat(e).isInstanceOf(PartitionKeyRangeGoneException.class);
                PartitionKeyRangeGoneException pkGoneException = (PartitionKeyRangeGoneException)e;
                assertThat(pkGoneException.getSubStatusCode())
                    .isEqualTo(HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE);
            })
            .verify();
    }
}
