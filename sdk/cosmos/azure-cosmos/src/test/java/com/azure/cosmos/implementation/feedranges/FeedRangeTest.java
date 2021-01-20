// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IRoutingMapProvider;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.PartitionKeyRangeGoneException;
import com.azure.cosmos.implementation.ReadFeedKeyType;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalUtils;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKeyDefinition;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class FeedRangeTest {

    @Test(groups = "unit")
    public void feedRangeEPK_Range() {
        Range<String> range = new Range<>("AA", "BB", true, false);
        FeedRangeEpkImpl feedRangeEPK = new FeedRangeEpkImpl(range);
        assertThat(range).isEqualTo(feedRangeEPK.getRange());
    }

    @Test(groups = "unit")
    public void feedRangeEPK_PartialEpkOfSinglePhysicalPartition_PopulatedHeaders() {
        Range<String> range = new Range<>("AA", "BB", true, false);
        FeedRangeEpkImpl feedRange = new FeedRangeEpkImpl(range);
        RxDocumentServiceRequest request = createMockRequest(true);
        String pkRangeId = UUID.randomUUID().toString();
        PartitionKeyRange partitionKeyRange = new PartitionKeyRange()
            .setId(pkRangeId)
            .setMinInclusive("AA")
            .setMaxExclusive("FF");
        List<PartitionKeyRange> pkRanges = new ArrayList<>();
        pkRanges.add(partitionKeyRange);

        IRoutingMapProvider routingMapProviderMock = Mockito.mock(IRoutingMapProvider.class);
        when(
            routingMapProviderMock.tryGetOverlappingRangesAsync(
                any(MetadataDiagnosticsContext.class),
                anyString(),
                eq(range),
                anyBoolean(),
                anyMapOf(String.class, Object.class)))
            .thenReturn(Mono.just(Utils.ValueHolder.initialize(pkRanges)));

        DocumentCollection collection = new DocumentCollection();
        feedRange.populateFeedRangeFilteringHeaders(
            routingMapProviderMock,
            request,
            Mono.just(new Utils.ValueHolder<>(collection))).block();

        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.READ_FEED_KEY_TYPE))
            .isNotNull()
            .isEqualTo(ReadFeedKeyType.EffectivePartitionKeyRange.name());

        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.START_EPK))
            .isNotNull()
            .isEqualTo("AA");

        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.END_EPK))
            .isNotNull()
            .isEqualTo("BB");
    }

    @Test(groups = "unit")
    public void feedRangeEPK_EpkOfFullSinglePhysicalPartition_PopulatedHeaders() {
        Range<String> range = new Range<>("AA", "BB", true, false);
        FeedRangeEpkImpl feedRange = new FeedRangeEpkImpl(range);
        RxDocumentServiceRequest request = createMockRequest(true);
        String pkRangeId = UUID.randomUUID().toString();
        PartitionKeyRange partitionKeyRange = new PartitionKeyRange()
            .setId(pkRangeId)
            .setMinInclusive(range.getMin())
            .setMaxExclusive(range.getMax());
        List<PartitionKeyRange> pkRanges = new ArrayList<>();
        pkRanges.add(partitionKeyRange);

        IRoutingMapProvider routingMapProviderMock = Mockito.mock(IRoutingMapProvider.class);
        when(
            routingMapProviderMock.tryGetOverlappingRangesAsync(
                any(MetadataDiagnosticsContext.class),
                anyString(),
                eq(range),
                anyBoolean(),
                anyMapOf(String.class, Object.class)))
            .thenReturn(Mono.just(Utils.ValueHolder.initialize(pkRanges)));

        DocumentCollection collection = new DocumentCollection();
        feedRange.populateFeedRangeFilteringHeaders(
            routingMapProviderMock,
            request,
            Mono.just(new Utils.ValueHolder<>(collection))).block();

        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.READ_FEED_KEY_TYPE))
            .isNull();

        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.START_EPK))
            .isNull();

        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.END_EPK))
            .isNull();

        assertThat(request.getPartitionKeyRangeIdentity())
            .isNotNull();
        assertThat(request.getPartitionKeyRangeIdentity().getPartitionKeyRangeId())
            .isNotNull()
            .isEqualTo(pkRangeId);
    }

    @Test(groups = "unit")
    public void feedRangeEPK_getEffectiveRangeAsync() {
        Range<String> range = new Range<>("AA", "BB", true, false);
        FeedRangeEpkImpl FeedRangeEpk = new FeedRangeEpkImpl(range);

        IRoutingMapProvider routingMapProviderMock = Mockito.mock(IRoutingMapProvider.class);
        StepVerifier
            .create(
                FeedRangeEpk.getEffectiveRange(
                    routingMapProviderMock,
                    null,
                    null))
            .recordWith(ArrayList::new)
            .expectNextCount(1)
            .consumeRecordedWith(r -> {
                assertThat(r)
                    .hasSize(1);
                assertThat(new ArrayList<>(r).get(0))
                    .isNotNull()
                    .isEqualTo(range);
            })
            .verifyComplete();
    }

    @Test(groups = "unit")
    public void feedRangeEPK_getPartitionKeyRangesAsync() {
        Range<String> range = new Range<>("AA", "BB", true, false);
        String pkRangeId = UUID.randomUUID().toString();
        PartitionKeyRange partitionKeyRange = new PartitionKeyRange()
            .setId(pkRangeId)
            .setMinInclusive(range.getMin())
            .setMaxExclusive(range.getMax());

        List<PartitionKeyRange> pkRanges = new ArrayList<>();
        pkRanges.add(partitionKeyRange);
        IRoutingMapProvider routingMapProviderMock = Mockito.mock(IRoutingMapProvider.class);
        when(
            routingMapProviderMock.tryGetOverlappingRangesAsync(
                any(MetadataDiagnosticsContext.class),
                anyString(),
                eq(range),
                anyBoolean(),
                anyMapOf(String.class, Object.class)))
            .thenReturn(Mono.just(Utils.ValueHolder.initialize(pkRanges)));

        RxDocumentServiceRequest request = createMockRequest(true);
        DocumentCollection collection = new DocumentCollection();

        FeedRangeEpkImpl feedRangeEpk = new FeedRangeEpkImpl(range);
        StepVerifier
            .create(
                feedRangeEpk.getPartitionKeyRanges(
                    routingMapProviderMock,
                    request,
                    Mono.just(new Utils.ValueHolder<>(collection))))
            .recordWith(ArrayList::new)
            .expectNextCount(1)
            .consumeRecordedWith(r -> {
                assertThat(r).hasSize(1);
                List<String> response = new ArrayList<>(r).get(0);
                assertThat(response)
                    .hasSize(1)
                    .contains(partitionKeyRange.getId());
            })
            .verifyComplete();

        Mockito
            .verify(routingMapProviderMock, Mockito.times(1))
            .tryGetOverlappingRangesAsync(
                any(MetadataDiagnosticsContext.class),
                anyString(),
                eq(range),
                eq(false),
                anyMapOf(String.class, Object.class));
    }

    @Test(groups = "unit")
    public void feedRangeEPK_toJsonFromJson() {
        Range<String> range = new Range<>("AA", "BB", true, false);
        FeedRangeEpkImpl feedRange = new FeedRangeEpkImpl(range);
        String base64EncodedJsonRepresentation = feedRange.toString();
        String jsonRepresentation = new String(
            Base64.getUrlDecoder().decode(base64EncodedJsonRepresentation),
            StandardCharsets.UTF_8);
        assertThat(jsonRepresentation)
            .isEqualTo("{\"Range\":{\"min\":\"AA\",\"max\":\"BB\"}}");
        assertThat(FeedRange.fromString(base64EncodedJsonRepresentation))
            .isNotNull()
            .isInstanceOf(FeedRangeEpkImpl.class);
        FeedRangeEpkImpl feedRangeDeserialized =
            (FeedRangeEpkImpl)FeedRange.fromString(base64EncodedJsonRepresentation);
        String representationAfterDeserialization = feedRangeDeserialized.toString();
        assertThat(representationAfterDeserialization).isEqualTo(base64EncodedJsonRepresentation);
        assertThat(feedRangeDeserialized.getRange()).isNotNull();
        assertThat(feedRangeDeserialized.getRange().getMin())
            .isNotNull()
            .isEqualTo(range.getMin());
        assertThat(feedRangeDeserialized.getRange().getMax())
            .isNotNull()
            .isEqualTo(range.getMax());
    }

    @Test(groups = "unit")
    public void feedRangePKRangeId_PKRange() {
        String pkRangeId = UUID.randomUUID().toString();
        FeedRangePartitionKeyRangeImpl feedRangePartitionKeyRange =
            new FeedRangePartitionKeyRangeImpl(pkRangeId);
        assertThat(pkRangeId).isEqualTo(feedRangePartitionKeyRange.getPartitionKeyRangeId());
    }

    @Test(groups = "unit")
    public void feedRangePKRangeId_PopulatedHeaders() {
        Range<String> range = new Range<>("AA", "BB", true, false);
        String pkRangeId = UUID.randomUUID().toString();
        PartitionKeyRange partitionKeyRange = new PartitionKeyRange()
            .setId(pkRangeId)
            .setMinInclusive(range.getMin())
            .setMaxExclusive(range.getMax());

        FeedRangePartitionKeyRangeImpl feedRangPartitionKeyRange =
            new FeedRangePartitionKeyRangeImpl(partitionKeyRange.getId());

        RxDocumentServiceRequest request = createMockRequest(true);
        IRoutingMapProvider routingMapProviderMock = Mockito.mock(IRoutingMapProvider.class);
        when(
            routingMapProviderMock.tryGetPartitionKeyRangeByIdAsync(
                any(MetadataDiagnosticsContext.class),
                anyString(),
                eq(partitionKeyRange.getId()),
                anyBoolean(),
                anyMapOf(String.class, Object.class)))
            .thenReturn(Mono.just(Utils.ValueHolder.initialize(partitionKeyRange)));

        DocumentCollection collection = new DocumentCollection();
        feedRangPartitionKeyRange.populateFeedRangeFilteringHeaders(
            routingMapProviderMock,
            request,
            Mono.just(new Utils.ValueHolder<>(collection))).block();

        assertThat(request.getPartitionKeyRangeIdentity()).isNotNull();
    }

    @Test(groups = "unit")
    public void feedRangePKRangeId_getEffectiveRangeAsync() {
        String pkRangeId = UUID.randomUUID().toString();
        PartitionKeyRange partitionKeyRange = new PartitionKeyRange()
            .setId(pkRangeId)
            .setMinInclusive("AA")
            .setMaxExclusive("BB");

        FeedRangePartitionKeyRangeImpl feedRangePartitionKeyRange =
            new FeedRangePartitionKeyRangeImpl(partitionKeyRange.getId());

        RxDocumentServiceRequest request = createMockRequest(true);
        DocumentCollection collection = new DocumentCollection();

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
                feedRangePartitionKeyRange.getEffectiveRange(
                    routingMapProviderMock,
                    BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics),
                    Mono.just(Utils.ValueHolder.initialize(collection))))
            .recordWith(ArrayList::new)
            .expectNextCount(1)
            .consumeRecordedWith(r -> {
                assertThat(r).hasSize(1);
                Range<String> range = new ArrayList<>(r).get(0);
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
    public void feedRangePKRangeId_getEffectiveRangeAsync_Null() {
        String pkRangeId = UUID.randomUUID().toString();
        PartitionKeyRange partitionKeyRange = new PartitionKeyRange()
            .setId(pkRangeId)
            .setMinInclusive("AA")
            .setMaxExclusive("BB");

        FeedRangePartitionKeyRangeImpl feedRangePartitionKeyRange =
            new FeedRangePartitionKeyRangeImpl(partitionKeyRange.getId());

        RxDocumentServiceRequest request = createMockRequest(true);
        DocumentCollection collection = new DocumentCollection();

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
                feedRangePartitionKeyRange.getEffectiveRange(
                    routingMapProviderMock,
                    BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics),
                    Mono.just(Utils.ValueHolder.initialize(collection))))
            .recordWith(ArrayList::new)
            .expectErrorSatisfies((e) -> {
                assertThat(e).isInstanceOf(PartitionKeyRangeGoneException.class);
                PartitionKeyRangeGoneException pkGoneException = (PartitionKeyRangeGoneException)e;
                assertThat(pkGoneException.getSubStatusCode())
                    .isEqualTo(HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE);
            })
            .verify();
    }

    @Test(groups = "unit")
    public void feedRangePKRangeId_getEffectiveRangeAsync_Refresh() {
        String pkRangeId = UUID.randomUUID().toString();
        PartitionKeyRange partitionKeyRange = new PartitionKeyRange()
            .setId(pkRangeId)
            .setMinInclusive("AA")
            .setMaxExclusive("BB");

        FeedRangePartitionKeyRangeImpl feedRangePartitionKeyRange =
            new FeedRangePartitionKeyRangeImpl(partitionKeyRange.getId());

        RxDocumentServiceRequest request = createMockRequest(true);
        DocumentCollection collection = new DocumentCollection();

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
                feedRangePartitionKeyRange.getEffectiveRange(
                    routingMapProviderMock,
                    BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics),
                    Mono.just(Utils.ValueHolder.initialize(collection))))
            .recordWith(ArrayList::new)
            .expectNextCount(1)
            .consumeRecordedWith(r -> {
                assertThat(r).hasSize(1);
                Range<String> range = new ArrayList<>(r).get(0);
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
    public void feedRangePKRangeId_getPartitionKeyRangesAsync() {
        Range<String> range = new Range<>("AA", "BB", true, false);
        String pkRangeId = UUID.randomUUID().toString();
        PartitionKeyRange partitionKeyRange = new PartitionKeyRange()
            .setId(pkRangeId)
            .setMinInclusive(range.getMin())
            .setMaxExclusive(range.getMax());

        FeedRangePartitionKeyRangeImpl feedRangPartitionKeyRange =
            new FeedRangePartitionKeyRangeImpl(partitionKeyRange.getId());

        IRoutingMapProvider routingMapProviderMock = Mockito.mock(IRoutingMapProvider.class);
        StepVerifier
            .create(
                feedRangPartitionKeyRange.getPartitionKeyRanges(
                    routingMapProviderMock,
                    null,
                    null))
            .recordWith(ArrayList::new)
            .expectNextCount(1)
            .consumeRecordedWith(r -> {
                assertThat(r).hasSize(1);
                List<String> response = new ArrayList<>(r).get(0);
                assertThat(response)
                    .hasSize(1)
                    .contains(partitionKeyRange.getId());
            })
            .verifyComplete();
    }

    @Test(groups = "unit")
    public void feedRangePKRangeId_toJsonFromJson() {
        String pkRangeId = UUID.randomUUID().toString();
        FeedRangePartitionKeyRangeImpl feedRange = new FeedRangePartitionKeyRangeImpl(pkRangeId);
        String base64EncodedJsonRepresentation = feedRange.toString();
        String jsonRepresentation = new String(
            Base64.getUrlDecoder().decode(base64EncodedJsonRepresentation),
            StandardCharsets.UTF_8);
        assertThat(jsonRepresentation).isEqualTo("{\"PKRangeId\":\"" + pkRangeId + "\"}");
        assertThat(FeedRange.fromString(base64EncodedJsonRepresentation))
            .isNotNull()
            .isInstanceOf(FeedRangePartitionKeyRangeImpl.class);
        FeedRangePartitionKeyRangeImpl feedRangeDeserialized =
            (FeedRangePartitionKeyRangeImpl)FeedRange.fromString(base64EncodedJsonRepresentation);
        String representationAfterDeserialization = feedRangeDeserialized.toString();
        assertThat(representationAfterDeserialization).isEqualTo(base64EncodedJsonRepresentation);
    }

    @Test(groups = "unit")
    public void feedRangePK_PK() {
        PartitionKeyInternal partitionKey = PartitionKeyInternalUtils.createPartitionKeyInternal(
            "Test");
        FeedRangePartitionKeyImpl feedRangePartitionKey =
            new FeedRangePartitionKeyImpl(partitionKey);
        assertThat(partitionKey).isEqualTo(feedRangePartitionKey.getPartitionKeyInternal());
    }

    @Test(groups = "unit")
    public void feedRangePK_PopulatedHeaders() {
        PartitionKeyInternal partitionKey = PartitionKeyInternalUtils.createPartitionKeyInternal(
            "Test");
        FeedRangePartitionKeyImpl feedRangePartitionKey =
            new FeedRangePartitionKeyImpl(partitionKey);
        RxDocumentServiceRequest request = createMockRequest(true);
        IRoutingMapProvider routingMapProviderMock = Mockito.mock(IRoutingMapProvider.class);
        DocumentCollection collection = new DocumentCollection();
        List<String> pkPaths = new ArrayList<>();
        pkPaths.add("/Test");
        collection.setPartitionKey(new PartitionKeyDefinition().setPaths(pkPaths));
        feedRangePartitionKey.populateFeedRangeFilteringHeaders(
            routingMapProviderMock,
            request,
            Mono.just(new Utils.ValueHolder<>(collection))).block();

        assertThat(request.getPartitionKeyInternal()).isNotNull();
        assertThat(request.getPartitionKeyInternal().toJson())
            .isNotNull()
            .isEqualTo(request.getHeaders().get(HttpConstants.HttpHeaders.PARTITION_KEY));
    }

    @Test(groups = "unit")
    public void feedRangePK_getEffectiveRangeAsync() {
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        partitionKeyDefinition.getPaths().add("/id");
        PartitionKeyInternal partitionKey = PartitionKeyInternalUtils.createPartitionKeyInternal(
            "Test");
        FeedRangePartitionKeyImpl feedRangePartitionKey =
            new FeedRangePartitionKeyImpl(partitionKey);
        Range<String> range = Range.getPointRange(
            partitionKey.getEffectivePartitionKeyString(partitionKey, partitionKeyDefinition));

        DocumentCollection collection = new DocumentCollection();
        collection.setPartitionKey(partitionKeyDefinition);

        IRoutingMapProvider routingMapProviderMock = Mockito.mock(IRoutingMapProvider.class);
        StepVerifier
            .create(
                feedRangePartitionKey.getEffectiveRange(
                    routingMapProviderMock,
                    null,
                    Mono.just(new Utils.ValueHolder<>(collection))))
            .recordWith(ArrayList::new)
            .expectNextCount(1)
            .consumeRecordedWith(r -> {
                assertThat(r).hasSize(1);
                assertThat(new ArrayList<>(r).get(0))
                    .isNotNull()
                    .isEqualTo(range);
            })
            .verifyComplete();
    }

    @Test(groups = "unit")
    public void feedRangePK_getPartitionKeyRangesAsync() {
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        partitionKeyDefinition.getPaths().add("/id");
        PartitionKeyInternal partitionKey = PartitionKeyInternalUtils.createPartitionKeyInternal(
            "Test");

        Range<String> range = new Range<>("AA", "BB", true, false);
        String pkRangeId = UUID.randomUUID().toString();
        PartitionKeyRange partitionKeyRange = new PartitionKeyRange()
            .setId(pkRangeId)
            .setMinInclusive(range.getMin())
            .setMaxExclusive(range.getMax());
        List<PartitionKeyRange> pkRanges = new ArrayList<>();
        pkRanges.add(partitionKeyRange);

        RxDocumentServiceRequest request = createMockRequest(true);
        IRoutingMapProvider routingMapProviderMock = Mockito.mock(IRoutingMapProvider.class);
        when(
            routingMapProviderMock.tryGetOverlappingRangesAsync(
                any(MetadataDiagnosticsContext.class),
                anyString(),
                any(),
                anyBoolean(),
                anyMapOf(String.class, Object.class)))
            .thenReturn(Mono.just(Utils.ValueHolder.initialize(pkRanges)));

        DocumentCollection collection = new DocumentCollection();
        collection.setPartitionKey(partitionKeyDefinition);

        FeedRangePartitionKeyImpl feedRangPartitionKey =
            new FeedRangePartitionKeyImpl(partitionKey);
        StepVerifier
            .create(
                feedRangPartitionKey.getPartitionKeyRanges(
                    routingMapProviderMock,
                    request,
                    Mono.just(new Utils.ValueHolder<>(collection))))
            .recordWith(ArrayList::new)
            .expectNextCount(1)
            .consumeRecordedWith(r -> {
                assertThat(r).hasSize(1);
                List<String> response = new ArrayList<>(r).get(0);
                assertThat(response)
                    .hasSize(1)
                    .contains(partitionKeyRange.getId());
            })
            .verifyComplete();
    }

    @Test(groups = "unit")
    public void feedRangePK_toJsonFromJson() {
        PartitionKeyInternal partitionKey = PartitionKeyInternalUtils.createPartitionKeyInternal(
            "Test");
        FeedRangePartitionKeyImpl feedRange = new FeedRangePartitionKeyImpl(partitionKey);
        String base64EncodedJsonRepresentation = feedRange.toString();
        String jsonRepresentation = new String(
            Base64.getUrlDecoder().decode(base64EncodedJsonRepresentation),
            StandardCharsets.UTF_8);
        assertThat(jsonRepresentation).isEqualTo("{\"PK\":[\"Test\"]}");
        assertThat(FeedRange.fromString(base64EncodedJsonRepresentation))
            .isNotNull()
            .isInstanceOf(FeedRangePartitionKeyImpl.class);
        FeedRangePartitionKeyImpl feedRangeDeserialized =
            (FeedRangePartitionKeyImpl)FeedRange.fromString(base64EncodedJsonRepresentation);
        String representationAfterDeserialization = feedRangeDeserialized.toString();
        assertThat(representationAfterDeserialization).isEqualTo(base64EncodedJsonRepresentation);
    }

    private static RxDocumentServiceRequest createMockRequest(boolean hasProperties) {
        RequestOptions requestOptions = new RequestOptions();

        if (hasProperties) {
            requestOptions.setProperties(new HashMap<>());
        }

        return RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(),
            OperationType.Read,
            ResourceType.Document,
            "/dbs/db/colls/col/docs/docId",
            null,
            requestOptions);
    }


}
