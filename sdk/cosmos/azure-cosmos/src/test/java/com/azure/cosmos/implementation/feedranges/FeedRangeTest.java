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
import com.azure.cosmos.models.PartitionKeyDefinitionVersion;
import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class FeedRangeTest {
    private String hashV1Baseline;
    private String hashV2Baseline;

    /*  NOTE these baseline files have been generated using .Net SDK
        The intent is to double-check that Java is following the same split algorithm
        Code to generate/update the baseline

        PartitionKeyDefinition pk = new PartitionKeyDefinition
        {
            Version = PartitionKeyDefinitionVersion.V2 // or V1
        };

        int[] testCases = new[] { 2, 3, 4, 5, 8, 20, 53, 64, 117, 127, 128, 129, 255, 256, 512, 1000, 5003, 8876, 9999, 100001 };

        using (StreamWriter output = File.CreateText(@"C:\\Temp\\Hash" + pk.Version.ToString() + "_Baseline.txt"))
        {
            foreach (int i in testCases)
            {
                string[] results = PartitionKeyInternal.GetNEqualRangeEffectivePartitionKeys(
                    "",
                    "FF",
                    pk,
                    i);

                for (int k = 0; k < results.Length; k++)
                {
                    if (k > 0)
                    {
                        output.Write("|");
                    }
                    output.Write(results[k]);

                }

                output.WriteLine();
            }
        }
    */
    @BeforeClass(groups = { "unit" })
    public void before_FeedRangeTests() throws Exception {

        InputStream hashV1BaselineStream = getClass().getClassLoader().getResourceAsStream(
            "FeedRangeSplit_HashV1_Baseline.txt");

        InputStream hashV2BaselineStream = getClass().getClassLoader().getResourceAsStream(
            "FeedRangeSplit_HashV2_Baseline.txt");

        assertThat(hashV1BaselineStream).isNotNull();
        assertThat(hashV2BaselineStream).isNotNull();

        this.hashV1Baseline = IOUtils.toString(hashV1BaselineStream, StandardCharsets.UTF_8);
        this.hashV2Baseline = IOUtils.toString(hashV2BaselineStream, StandardCharsets.UTF_8);
    }

    @Test(groups = "unit")
    public void feedRange_Split_HashV1() {
        Range<String> fullRange = new Range<>("", "FF", true, false);

        int[] testCases = new int[] {
            2, 3, 4, 5, 8, 20, 53, 64, 117, 127, 128, 129, 255, 256, 512, 1000, 5003, 8876, 9999, 100001
        };

        StringBuilder sb = new StringBuilder();
        for (int targetSplitCount : testCases) {
            List<FeedRangeEpkImpl> feedRanges = FeedRangeInternal.trySplitWithHashV1(fullRange,
                targetSplitCount);

            for (int i = 0; i < feedRanges.size() - 1; i++) {
                FeedRangeEpkImpl epkFeedRange = feedRanges.get(i);

                if (i > 0) {
                    sb.append("|");
                }
                sb.append(epkFeedRange.getRange().getMax());
            }
            sb.append(System.getProperty("line.separator"));
        }

        assertThat(sb.toString()).isEqualTo(hashV1Baseline);
    }

    @Test(groups = "unit")
    public void feedRange_Split_HashV1_NonPKRangeAligned_And_NotFullRange() {
        Range<String> startRange = new Range<>("05C1B9CD673390", "05C1C9CD673390", true, false);

        assertThat(FeedRangeInternal.fromHexEncodedBinaryString(startRange.getMin()))
            .isEqualTo(429496729);

        int targetSplitCount = 7;
        List<FeedRangeEpkImpl> feedRanges = FeedRangeInternal.trySplitWithHashV1(startRange, targetSplitCount);

        String[][] expectedValues = new String[7][2];
        expectedValues[0][0] = "05C1B9CD673390";
        expectedValues[0][1] = "05C1BDA17583C0";
        expectedValues[1][0] = "05C1BDA17583C0";
        expectedValues[1][1] = "05C1C13B41E9F8";
        expectedValues[2][0] = "05C1C13B41E9F8";
        expectedValues[2][1] = "05C1C325499310";
        expectedValues[3][0] = "05C1C325499310";
        expectedValues[3][1] = "05C1C50F513B28";
        expectedValues[4][0] = "05C1C50F513B28";
        expectedValues[4][1] = "05C1C5F957E340";
        expectedValues[5][0] = "05C1C5F957E340";
        expectedValues[5][1] = "05C1C7E35F8B58";
        expectedValues[6][0] = "05C1C7E35F8B58";
        expectedValues[6][1] = "05C1C9CD673390";

        for (int i = 0; i < feedRanges.size() - 1; i++) {
            FeedRangeEpkImpl epkFeedRange = feedRanges.get(i);
            assertThat(epkFeedRange.getRange().getMin()).isEqualTo(expectedValues[i][0]);
            assertThat(epkFeedRange.getRange().getMax()).isEqualTo(expectedValues[i][1]);
        }
    }

    @Test(groups = "unit")
    public void feedRange_Split_HashV2() {
        Range<String> fullRange = new Range<>("", "FF", true, false);

        int[] testCases = new int[] {
            2, 3, 4, 5, 8, 20, 53, 64, 117, 127, 128, 129, 255, 256, 512, 1000, 5003, 8876, 9999, 100001
        };

        StringBuilder sb = new StringBuilder();
        for (int targetSplitCount : testCases) {
            List<FeedRangeEpkImpl> feedRanges = FeedRangeInternal.trySplitWithHashV2(fullRange, targetSplitCount);

            for (int i = 0; i < feedRanges.size() - 1; i++) {
                FeedRangeEpkImpl epkFeedRange = feedRanges.get(i);

                if (i > 0) {
                    sb.append("|");
                }
                sb.append(epkFeedRange.getRange().getMax());
            }
            sb.append(System.getProperty("line.separator"));
        }

        assertThat(sb.toString()).isEqualTo(hashV2Baseline);
    }

    @Test(groups = "unit")
    public void feedRange_Split_HashV1_forSubRange() {

        // this test re-evaluates the initialization when min/max range is
        // not on the edge - like "" or "FF". In these cases the binary decoding
        // needs to be applied. Doing it for one range is sufficient along
        // with the .Net comparison test above because the transformations and
        // nit-mask operations are identical.

        String[] lines = hashV1Baseline.split(System.getProperty("line.separator"));
        String[] rangesForFour = lines[2].split("\\|");
        assertThat(rangesForFour).isNotNull().hasSize(3);

        Range<String> rangeToBeSplit = new Range<>(
            rangesForFour[0],
            rangesForFour[2],
            true,
            false);

        List<FeedRangeEpkImpl> feedRanges = FeedRangeInternal.trySplitWithHashV1(rangeToBeSplit,2);
        assertThat(feedRanges).isNotNull().hasSize(2);
        FeedRangeEpkImpl leftEpkFeedRange = feedRanges.get(0);
        FeedRangeEpkImpl rightEpkFeedRange = feedRanges.get(1);
        assertThat(rangesForFour[1]).isEqualTo(leftEpkFeedRange.getRange().getMax());
        assertThat(rangesForFour[1]).isEqualTo(rightEpkFeedRange.getRange().getMin());
    }

    @Test(groups = "unit")
    public void feedRange_Split_HashV2_forSubRange() {

        // this test re-evaluates the initialization when min/max range is
        // not on the edge - like "" or "FF". In these cases the binary decoding
        // needs to be applied. Doing it for one range is sufficient along
        // with the .Net comparison test above because the transformations and
        // nit-mask operations are identical.

        String[] lines = hashV2Baseline.split(System.getProperty("line.separator"));
        String[] rangesForFour = lines[2].split("\\|");
        assertThat(rangesForFour).isNotNull().hasSize(3);

        Range<String> rangeToBeSplit = new Range<>(
            rangesForFour[0],
            rangesForFour[2],
            true,
            false);
        List<FeedRangeEpkImpl> feedRanges = FeedRangeInternal.trySplitWithHashV2(rangeToBeSplit,2);
        assertThat(feedRanges).isNotNull().hasSize(2);
        FeedRangeEpkImpl leftEpkFeedRange = feedRanges.get(0);
        FeedRangeEpkImpl rightEpkFeedRange = feedRanges.get(1);
        assertThat(rangesForFour[1]).isEqualTo(leftEpkFeedRange.getRange().getMax());
        assertThat(rangesForFour[1]).isEqualTo(rightEpkFeedRange.getRange().getMin());
    }

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
        RxDocumentServiceRequest request = createMockRequest();
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
                any(),
                any(),
                eq(range),
                anyBoolean(),
                any()))
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
        RxDocumentServiceRequest request = createMockRequest();
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
                any(),
                any(),
                eq(range),
                anyBoolean(),
                any()))
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

        PartitionKeyDefinition pkDef = new PartitionKeyDefinition();
        pkDef.setVersion(PartitionKeyDefinitionVersion.V2);

        DocumentCollection collection = new DocumentCollection();
        collection.setPartitionKey(pkDef);

        IRoutingMapProvider routingMapProviderMock = Mockito.mock(IRoutingMapProvider.class);
        StepVerifier
            .create(
                FeedRangeEpk.getNormalizedEffectiveRange(
                    routingMapProviderMock,
                    null,
                    Mono.just(Utils.ValueHolder.initialize(collection))))
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
                any(),
                any(),
                eq(range),
                anyBoolean(),
                any()))
            .thenReturn(Mono.just(Utils.ValueHolder.initialize(pkRanges)));

        RxDocumentServiceRequest request = createMockRequest();
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
                any(),
                any(),
                eq(range),
                eq(false),
                any());
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

        RxDocumentServiceRequest request = createMockRequest();
        IRoutingMapProvider routingMapProviderMock = Mockito.mock(IRoutingMapProvider.class);
        when(
            routingMapProviderMock.tryGetPartitionKeyRangeByIdAsync(
                any(),
                any(),
                eq(partitionKeyRange.getId()),
                anyBoolean(),
                any()))
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

        RxDocumentServiceRequest request = createMockRequest();
        DocumentCollection collection = new DocumentCollection();

        IRoutingMapProvider routingMapProviderMock = Mockito.mock(IRoutingMapProvider.class);
        when(
            routingMapProviderMock.tryGetPartitionKeyRangeByIdAsync(
                any(),
                any(),
                eq(partitionKeyRange.getId()),
                anyBoolean(),
                any()))
            .thenReturn(Mono.just(Utils.ValueHolder.initialize(partitionKeyRange)));

        StepVerifier
            .create(
                feedRangePartitionKeyRange.getNormalizedEffectiveRange(
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

        RxDocumentServiceRequest request = createMockRequest();
        DocumentCollection collection = new DocumentCollection();

        IRoutingMapProvider routingMapProviderMock = Mockito.mock(IRoutingMapProvider.class);
        when(
            routingMapProviderMock.tryGetPartitionKeyRangeByIdAsync(
                any(),
                any(),
                eq(partitionKeyRange.getId()),
                anyBoolean(),
                any()))
            .thenReturn(Mono.just(Utils.ValueHolder.initialize(null)))
            .thenReturn(Mono.just(Utils.ValueHolder.initialize(null)));

        StepVerifier
            .create(
                feedRangePartitionKeyRange.getNormalizedEffectiveRange(
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

        RxDocumentServiceRequest request = createMockRequest();
        DocumentCollection collection = new DocumentCollection();

        IRoutingMapProvider routingMapProviderMock = Mockito.mock(IRoutingMapProvider.class);
        when(
            routingMapProviderMock.tryGetPartitionKeyRangeByIdAsync(
                any(),
                any(),
                eq(partitionKeyRange.getId()),
                anyBoolean(),
                any()))
            .thenReturn(Mono.just(Utils.ValueHolder.initialize(null)))
            .thenReturn(Mono.just(Utils.ValueHolder.initialize(partitionKeyRange)));

        StepVerifier
            .create(
                feedRangePartitionKeyRange.getNormalizedEffectiveRange(
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
                any(),
                any(),
                eq(partitionKeyRange.getId()),
                anyBoolean(),
                any());
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
        RxDocumentServiceRequest request = createMockRequest();
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
        partitionKeyDefinition.setVersion(PartitionKeyDefinitionVersion.V2);
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
                feedRangePartitionKey.getNormalizedEffectiveRange(
                    routingMapProviderMock,
                    null,
                    Mono.just(new Utils.ValueHolder<>(collection))))
            .recordWith(ArrayList::new)
            .expectNextCount(1)
            .consumeRecordedWith(r -> {
                assertThat(r).hasSize(1);
                assertThat(new ArrayList<>(r).get(0))
                    .isNotNull()
                    .isEqualTo(convertToMaxExclusive(range));
            })
            .verifyComplete();
    }

    private Range<String> convertToMaxExclusive(Range<String> maxInclusiveRange) {
        assertThat(maxInclusiveRange)
            .isNotNull()
            .matches(r -> r.isMaxInclusive(), "Ensure isMaxInclusive is set");
        String max = maxInclusiveRange.getMax();
        int i = max.length() - 1;
        while (i >= 0) {
            if (max.charAt(i) == 'F') {
                i--;
                continue;
            }
            char newChar = (char)(((int)max.charAt(i))+1);
            if (i < max.length() - 1) {
                max = max.substring(0, i) + newChar + max.substring(i + 1);
            } else {
                max = max.substring(0, i) + newChar;
            }
            break;
        }
        return new Range<>(maxInclusiveRange.getMin(), max, true, false);
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

        RxDocumentServiceRequest request = createMockRequest();
        IRoutingMapProvider routingMapProviderMock = Mockito.mock(IRoutingMapProvider.class);
        when(
            routingMapProviderMock.tryGetOverlappingRangesAsync(
                any(),
                any(),
                any(),
                anyBoolean(),
                any()))
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

    private static RxDocumentServiceRequest createMockRequest() {
        RequestOptions requestOptions = new RequestOptions();

        requestOptions.setProperties(new HashMap<>());

        return RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(),
            OperationType.Read,
            ResourceType.Document,
            "/dbs/db/colls/col/docs/docId",
            null,
            requestOptions);
    }
}
