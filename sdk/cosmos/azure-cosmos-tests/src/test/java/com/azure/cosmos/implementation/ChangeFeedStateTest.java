// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStartFromTypes;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyRangeImpl;
import com.azure.cosmos.implementation.query.CompositeContinuationToken;
import com.azure.cosmos.implementation.routing.Range;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangeFeedStateTest {
    @DataProvider(name = "populateRequestArgProvider")
    public Object[][] populateRequestArgProvider() {
        return new Object[][] {
            // changeFeed mode, changeFeed startFrom type, use continuation
            { ChangeFeedMode.INCREMENTAL, ChangeFeedStartFromTypes.BEGINNING, true },
            { ChangeFeedMode.INCREMENTAL, ChangeFeedStartFromTypes.NOW, true },
            { ChangeFeedMode.INCREMENTAL, ChangeFeedStartFromTypes.POINT_IN_TIME, true },
            { ChangeFeedMode.INCREMENTAL, ChangeFeedStartFromTypes.BEGINNING, false },
            { ChangeFeedMode.INCREMENTAL, ChangeFeedStartFromTypes.NOW, false },
            { ChangeFeedMode.INCREMENTAL, ChangeFeedStartFromTypes.POINT_IN_TIME, false },
            { ChangeFeedMode.FULL_FIDELITY, ChangeFeedStartFromTypes.NOW, true },
            { ChangeFeedMode.FULL_FIDELITY, ChangeFeedStartFromTypes.NOW, false }
        };
    }

    @Test(groups = "unit")
    public void changeFeedState_incrementalMode_startFromNow_PKRangeId_toJsonFromJson() {
        String containerRid = "/cols/" + UUID.randomUUID().toString();
        String pkRangeId = UUID.randomUUID().toString();
        FeedRangePartitionKeyRangeImpl feedRange = new FeedRangePartitionKeyRangeImpl(pkRangeId);
        ChangeFeedStartFromInternal startFromSettings = ChangeFeedStartFromInternal.createFromNow();
        ChangeFeedState stateWithoutContinuation = new ChangeFeedStateV1(
            containerRid,
            feedRange,
            ChangeFeedMode.INCREMENTAL,
            startFromSettings,
            null);

        String base64EncodedJsonRepresentation = stateWithoutContinuation.toString();
        String jsonRepresentation = new String(
            Base64.getUrlDecoder().decode(base64EncodedJsonRepresentation),
            StandardCharsets.UTF_8);
        assertThat(jsonRepresentation)
            .isEqualTo(
                String.format(
                    "{\"V\":1," +
                        "\"Rid\":\"%s\"," +
                        "\"Mode\":\"INCREMENTAL\"," +
                        "\"StartFrom\":{\"Type\":\"NOW\"}," +
                        "\"PKRangeId\":\"%s\"}",
                    containerRid,
                    pkRangeId));

        assertThat(ChangeFeedState.fromString(base64EncodedJsonRepresentation))
            .isNotNull()
            .isInstanceOf(ChangeFeedStateV1.class);

        ChangeFeedStateV1 stateWithoutContinuationDeserialized =
            (ChangeFeedStateV1)ChangeFeedState.fromString(base64EncodedJsonRepresentation);

        String representationAfterDeserialization = stateWithoutContinuationDeserialized.toString();
        assertThat(representationAfterDeserialization).isEqualTo(base64EncodedJsonRepresentation);

        String continuationDummy = UUID.randomUUID().toString();
        String continuationJson = String.format(
            "{\"V\":1," +
                "\"Rid\":\"%s\"," +
                "\"Continuation\":[" +
                "{\"token\":\"%s\",\"range\":{\"min\":\"AA\",\"max\":\"BB\"}}," +
                "{\"token\":\"%s\",\"range\":{\"min\":\"CC\",\"max\":\"DD\"}}" +
                "]," +
                "\"PKRangeId\":\"%s\"}",
            containerRid,
            continuationDummy,
            continuationDummy,
            pkRangeId);

        FeedRangeContinuation continuation = FeedRangeContinuation.convert(continuationJson);

        ChangeFeedState stateWithContinuation =
            stateWithoutContinuation.setContinuation(continuation);
        base64EncodedJsonRepresentation = stateWithContinuation.toString();
        jsonRepresentation = new String(
            Base64.getUrlDecoder().decode(base64EncodedJsonRepresentation),
            StandardCharsets.UTF_8);

        assertThat(jsonRepresentation)
            .isEqualTo(
                String.format(
                    "{\"V\":1," +
                        "\"Rid\":\"%s\"," +
                        "\"Mode\":\"INCREMENTAL\"," +
                        "\"StartFrom\":{\"Type\":\"NOW\"}," +
                        "\"Continuation\":%s}",
                    containerRid,
                    continuationJson));

        assertThat(ChangeFeedState.fromString(base64EncodedJsonRepresentation))
            .isNotNull()
            .isInstanceOf(ChangeFeedStateV1.class);

        ChangeFeedStateV1 stateWithContinuationDeserialized =
            (ChangeFeedStateV1)ChangeFeedState.fromString(base64EncodedJsonRepresentation);

        representationAfterDeserialization = stateWithContinuationDeserialized.toString();
        assertThat(representationAfterDeserialization).isEqualTo(base64EncodedJsonRepresentation);
    }

    @Test(groups = "unit")
    public void changeFeedState_fullFidelityMode_startFromNow_PKRangeId_toJsonFromJson() {
        String containerRid = "/cols/" + UUID.randomUUID().toString();
        String pkRangeId = UUID.randomUUID().toString();
        FeedRangePartitionKeyRangeImpl feedRange = new FeedRangePartitionKeyRangeImpl(pkRangeId);
        ChangeFeedStartFromInternal startFromSettings = ChangeFeedStartFromInternal.createFromNow();
        ChangeFeedState stateWithoutContinuation = new ChangeFeedStateV1(
            containerRid,
            feedRange,
            ChangeFeedMode.FULL_FIDELITY,
            startFromSettings,
            null);

        String base64EncodedJsonRepresentation = stateWithoutContinuation.toString();
        String jsonRepresentation = new String(
            Base64.getUrlDecoder().decode(base64EncodedJsonRepresentation),
            StandardCharsets.UTF_8);
        assertThat(jsonRepresentation)
            .isEqualTo(
                String.format(
                    "{\"V\":1," +
                        "\"Rid\":\"%s\"," +
                        "\"Mode\":\"FULL_FIDELITY\"," +
                        "\"StartFrom\":{\"Type\":\"NOW\"}," +
                        "\"PKRangeId\":\"%s\"}",
                    containerRid,
                    pkRangeId));

        assertThat(ChangeFeedState.fromString(base64EncodedJsonRepresentation))
            .isNotNull()
            .isInstanceOf(ChangeFeedStateV1.class);

        ChangeFeedStateV1 stateWithoutContinuationDeserialized =
            (ChangeFeedStateV1)ChangeFeedState.fromString(base64EncodedJsonRepresentation);

        String representationAfterDeserialization = stateWithoutContinuationDeserialized.toString();
        assertThat(representationAfterDeserialization).isEqualTo(base64EncodedJsonRepresentation);

        String continuationDummy = UUID.randomUUID().toString();
        String continuationJson = String.format(
            "{\"V\":1," +
                "\"Rid\":\"%s\"," +
                "\"Continuation\":[" +
                "{\"token\":\"%s\",\"range\":{\"min\":\"AA\",\"max\":\"BB\"}}," +
                "{\"token\":\"%s\",\"range\":{\"min\":\"CC\",\"max\":\"DD\"}}" +
                "]," +
                "\"PKRangeId\":\"%s\"}",
            containerRid,
            continuationDummy,
            continuationDummy,
            pkRangeId);

        FeedRangeContinuation continuation = FeedRangeContinuation.convert(continuationJson);

        ChangeFeedState stateWithContinuation =
            stateWithoutContinuation.setContinuation(continuation);
        base64EncodedJsonRepresentation = stateWithContinuation.toString();
        jsonRepresentation = new String(
            Base64.getUrlDecoder().decode(base64EncodedJsonRepresentation),
            StandardCharsets.UTF_8);

        assertThat(jsonRepresentation)
            .isEqualTo(
                String.format(
                    "{\"V\":1," +
                        "\"Rid\":\"%s\"," +
                        "\"Mode\":\"FULL_FIDELITY\"," +
                        "\"StartFrom\":{\"Type\":\"NOW\"}," +
                        "\"Continuation\":%s}",
                    containerRid,
                    continuationJson));

        assertThat(ChangeFeedState.fromString(base64EncodedJsonRepresentation))
            .isNotNull()
            .isInstanceOf(ChangeFeedStateV1.class);

        ChangeFeedStateV1 stateWithContinuationDeserialized =
            (ChangeFeedStateV1)ChangeFeedState.fromString(base64EncodedJsonRepresentation);

        representationAfterDeserialization = stateWithContinuationDeserialized.toString();
        assertThat(representationAfterDeserialization).isEqualTo(base64EncodedJsonRepresentation);
    }

    private ChangeFeedState createDefaultStateWithContinuation(String continuationAAToCC, String continuationCCToEE)
    {
        String containerRid = "/cols/" + UUID.randomUUID();
        String pkRangeId = UUID.randomUUID().toString();
        FeedRangePartitionKeyRangeImpl feedRange = new FeedRangePartitionKeyRangeImpl(pkRangeId);

        return this.createStateWithContinuation(
            containerRid,
            feedRange,
            continuationAAToCC,
            continuationCCToEE,
            ChangeFeedMode.INCREMENTAL,
            ChangeFeedStartFromInternal.createFromNow());
    }

    private ChangeFeedState createStateWithContinuation(
        String containerRid,
        FeedRangePartitionKeyRangeImpl feedRange,
        String continuationAAToCC,
        String continuationCCToEE,
        ChangeFeedMode changeFeedMode,
        ChangeFeedStartFromInternal startFromSettings)
    {
        String continuationJson = String.format(
            "{\"V\":1," +
                "\"Rid\":\"%s\"," +
                "\"Continuation\":[" +
                "{\"token\":\"%s\",\"range\":{\"min\":\"AA\",\"max\":\"CC\"}}," +
                "{\"token\":\"%s\",\"range\":{\"min\":\"CC\",\"max\":\"EE\"}}" +
                "]," +
                "\"PKRangeId\":\"%s\"}",
            containerRid,
            continuationAAToCC,
            continuationCCToEE,
            feedRange.getPartitionKeyRangeId());

        FeedRangeContinuation continuation = FeedRangeContinuation.convert(continuationJson);
        return new ChangeFeedStateV1(
            containerRid,
            feedRange,
            changeFeedMode,
            startFromSettings,
            continuation);
    }

    @Test(groups = "unit")
    public void changeFeedState_extractContinuationTokens() {
        String continuationAAToCC = UUID.randomUUID().toString();
        String continuationCCToEE = UUID.randomUUID().toString();
        List<CompositeContinuationToken> tokens =
            this
            .createDefaultStateWithContinuation(continuationAAToCC, continuationCCToEE)
            .extractForEffectiveRange(new Range<>("AA", "CC", true, false))
            .extractContinuationTokens();

        assertThat(tokens)
            .isNotNull()
            .hasSize(1);
        assertThat(tokens.get(0))
            .isNotNull();
        assertThat(tokens.get(0).getRange())
            .isNotNull()
            .isEqualTo(new Range<>("AA", "CC", true, false));
        assertThat(tokens.get(0).getToken())
            .isNotNull()
            .isEqualTo(continuationAAToCC);

        tokens =
            this
                .createDefaultStateWithContinuation(continuationAAToCC, continuationCCToEE)
                .extractForEffectiveRange(new Range<>("BB", "DD", true, false))
                .extractContinuationTokens();

        assertThat(tokens)
            .isNotNull()
            .hasSize(2);
        assertThat(tokens.get(0))
            .isNotNull();
        assertThat(tokens.get(0).getRange())
            .isNotNull()
            .isEqualTo(new Range<>("BB", "CC", true, false));
        assertThat(tokens.get(0).getToken())
            .isNotNull()
            .isEqualTo(continuationAAToCC);
        assertThat(tokens.get(1))
            .isNotNull();
        assertThat(tokens.get(1).getRange())
            .isNotNull()
            .isEqualTo(new Range<>("CC", "DD", true, false));
        assertThat(tokens.get(1).getToken())
            .isNotNull()
            .isEqualTo(continuationCCToEE);
    }

    @Test(groups = "unit")
    public void changeFeedState_merge() {
        String continuationAAToCC = UUID.randomUUID().toString();
        String continuationCCToEE = UUID.randomUUID().toString();
        ChangeFeedState original = this
            .createDefaultStateWithContinuation(continuationAAToCC, continuationCCToEE);
        ChangeFeedState stateAAToBB = original.extractForEffectiveRange(
            new Range<>("AA", "BB", true, false));
        ChangeFeedState stateBBToDD = original.extractForEffectiveRange(
            new Range<>("BB", "DD", true, false));
        ChangeFeedState stateDDToEE = original.extractForEffectiveRange(
            new Range<>("DD", "EE", true, false));

        ChangeFeedState merged = ChangeFeedState.merge(
            new ChangeFeedState[] { stateAAToBB, stateBBToDD, stateDDToEE}
        );

        assertThat(merged)
            .isNotNull();

        List<CompositeContinuationToken> tokens = merged.extractContinuationTokens();

        assertThat(tokens)
            .isNotNull()
            .hasSize(4);
        assertThat(tokens.get(0))
            .isNotNull();
        assertThat(tokens.get(0).getRange())
            .isNotNull()
            .isEqualTo(new Range<>("AA", "BB", true, false));
        assertThat(tokens.get(0).getToken())
            .isNotNull()
            .isEqualTo(continuationAAToCC);

        assertThat(tokens.get(1))
            .isNotNull();
        assertThat(tokens.get(1).getRange())
            .isNotNull()
            .isEqualTo(new Range<>("BB", "CC", true, false));
        assertThat(tokens.get(1).getToken())
            .isNotNull()
            .isEqualTo(continuationAAToCC);

        assertThat(tokens.get(2))
            .isNotNull();
        assertThat(tokens.get(2).getRange())
            .isNotNull()
            .isEqualTo(new Range<>("CC", "DD", true, false));
        assertThat(tokens.get(2).getToken())
            .isNotNull()
            .isEqualTo(continuationCCToEE);

        assertThat(tokens.get(3))
            .isNotNull();
        assertThat(tokens.get(3).getRange())
            .isNotNull()
            .isEqualTo(new Range<>("DD", "EE", true, false));
        assertThat(tokens.get(3).getToken())
            .isNotNull()
            .isEqualTo(continuationCCToEE);
    }

    @Test(dataProvider = "populateRequestArgProvider", groups = "unit")
    public void changeFeedState_populateRequest(
        ChangeFeedMode changeFeedMode,
        ChangeFeedStartFromTypes initialChangeFeedStartFromTypes,
        boolean useContinuationToken) {

        String containerRid = "/cols/" + UUID.randomUUID();
        String pkRangeId = UUID.randomUUID().toString();
        FeedRangePartitionKeyRangeImpl feedRange = new FeedRangePartitionKeyRangeImpl(pkRangeId);
        ChangeFeedStartFromInternal changeFeedStartFromInternal;
        Map<String, String> expectedHeaders = new HashMap<>();

        switch (initialChangeFeedStartFromTypes) {
            case BEGINNING:
                changeFeedStartFromInternal = ChangeFeedStartFromInternal.createFromBeginning();
                break;
            case NOW:
                changeFeedStartFromInternal = ChangeFeedStartFromInternal.createFromNow();
                expectedHeaders.put(HttpConstants.HttpHeaders.IF_NONE_MATCH, HttpConstants.HeaderValues.IF_NONE_MATCH_ALL);
                break;
            case POINT_IN_TIME:
                Instant startTime = Instant.now();
                changeFeedStartFromInternal = ChangeFeedStartFromInternal.createFromPointInTime(startTime);
                expectedHeaders.put(HttpConstants.HttpHeaders.IF_MODIFIED_SINCE, Utils.instantAsUTCRFC1123(startTime));
                break;
            default:
                throw new IllegalStateException("Invalid initialChangeFeedStartFromTypes " + initialChangeFeedStartFromTypes);
        }

        ChangeFeedState changeFeedState;
        if (useContinuationToken) {
            String continuationAAToCC = UUID.randomUUID().toString();
            String continuationCCToEE = UUID.randomUUID().toString();
            changeFeedState = this.createStateWithContinuation(
                containerRid,
                feedRange,
                continuationAAToCC,
                continuationCCToEE,
                changeFeedMode,
                changeFeedStartFromInternal);

            expectedHeaders.put(
                HttpConstants.HttpHeaders.IF_NONE_MATCH,
                changeFeedState.getContinuation().getCurrentContinuationToken().getToken());
        } else {
            changeFeedState = new ChangeFeedStateV1(
                containerRid,
                feedRange,
                changeFeedMode,
                changeFeedStartFromInternal,
                null);
        }

        int maxItemCount = 1;
        expectedHeaders.put(HttpConstants.HttpHeaders.PAGE_SIZE, String.valueOf(maxItemCount));
        expectedHeaders.put(HttpConstants.HttpHeaders.POPULATE_QUERY_METRICS, "true");
        if (changeFeedMode == ChangeFeedMode.INCREMENTAL) {
            expectedHeaders.put(HttpConstants.HttpHeaders.A_IM, HttpConstants.A_IMHeaderValues.INCREMENTAL_FEED);
        } else {
            expectedHeaders.put(HttpConstants.HttpHeaders.A_IM, HttpConstants.A_IMHeaderValues.FULL_FIDELITY_FEED);
        }

        RxDocumentServiceRequest serviceRequest =
            RxDocumentServiceRequest.create(
                mockDiagnosticsClientContext(),
                OperationType.Read,
                ResourceType.Document);
        changeFeedState.populateRequest(serviceRequest, maxItemCount);
        Map<String, String> headers = serviceRequest.getHeaders();

        for (String key : expectedHeaders.keySet()) {
            assertThat(headers.containsKey(key)).isTrue();
            assertThat(headers.get(key)).isEqualTo(expectedHeaders.get(key));
        }
    }

    @Test(groups = "unit")
    public void changeFeedState_extractForEffectiveRange_multipleCallsProduceCorrectResults() {
        int tokenCount = 100;
        ChangeFeedState state = createStateWithManyTokens(tokenCount);

        List<Range<String>> ranges = new ArrayList<>();
        ranges.add(new Range<>(String.format("%06X", 0), String.format("%06X", 10), true, false));
        ranges.add(new Range<>(String.format("%06X", 50), String.format("%06X", 60), true, false));
        ranges.add(new Range<>(String.format("%06X", 90), String.format("%06X", 100), true, false));
        ranges.add(new Range<>(String.format("%06X", 25), String.format("%06X", 26), true, false));

        List<ChangeFeedState> results = state.extractForEffectiveRanges(ranges);
        assertThat(results).hasSize(4);

        List<CompositeContinuationToken> tokens1 = results.get(0).extractContinuationTokens();
        assertThat(tokens1).hasSize(10);
        for (int i = 0; i < 10; i++) {
            assertThat(tokens1.get(i).getToken()).isEqualTo("token_" + i);
        }

        List<CompositeContinuationToken> tokens2 = results.get(1).extractContinuationTokens();
        assertThat(tokens2).hasSize(10);
        for (int i = 0; i < 10; i++) {
            assertThat(tokens2.get(i).getToken()).isEqualTo("token_" + (50 + i));
        }

        List<CompositeContinuationToken> tokens3 = results.get(2).extractContinuationTokens();
        assertThat(tokens3).hasSize(10);
        for (int i = 0; i < 10; i++) {
            assertThat(tokens3.get(i).getToken()).isEqualTo("token_" + (90 + i));
        }

        List<CompositeContinuationToken> tokens4 = results.get(3).extractContinuationTokens();
        assertThat(tokens4).hasSize(1);
        assertThat(tokens4.get(0).getToken()).isEqualTo("token_25");
    }

    @Test(groups = "unit")
    public void changeFeedState_extractForEffectiveRanges_singleBatchAndSingleCallParity() {
        ChangeFeedState state = createStateWithManyTokens(20);

        List<Range<String>> ranges = new ArrayList<>();
        ranges.add(new Range<>(String.format("%06X", 0), String.format("%06X", 5), true, false));
        ranges.add(new Range<>(String.format("%06X", 10), String.format("%06X", 15), true, false));
        ranges.add(new Range<>(String.format("%06X", 18), String.format("%06X", 20), true, false));

        List<ChangeFeedState> batchResults = state.extractForEffectiveRanges(ranges);

        for (int i = 0; i < ranges.size(); i++) {
            ChangeFeedState singleResult = state.extractForEffectiveRange(ranges.get(i));
            assertThat(batchResults.get(i).toString())
                .as("Batch result at index %d should match single-range result", i)
                .isEqualTo(singleResult.toString());
        }
    }

    @Test(groups = "unit")
    public void changeFeedState_extractForEffectiveRange_singleToken() {
        String containerRid = "/cols/" + UUID.randomUUID();
        String pkRangeId = UUID.randomUUID().toString();
        FeedRangePartitionKeyRangeImpl feedRange = new FeedRangePartitionKeyRangeImpl(pkRangeId);

        String continuationJson = String.format(
            "{\"V\":1,\"Rid\":\"%s\",\"Continuation\":[" +
                "{\"token\":\"tok1\",\"range\":{\"min\":\"AA\",\"max\":\"CC\"}}" +
                "],\"PKRangeId\":\"%s\"}",
            containerRid, pkRangeId);

        FeedRangeContinuation continuation = FeedRangeContinuation.convert(continuationJson);
        ChangeFeedState state = new ChangeFeedStateV1(
            containerRid, feedRange, ChangeFeedMode.INCREMENTAL,
            ChangeFeedStartFromInternal.createFromNow(), continuation);

        List<CompositeContinuationToken> tokens =
            state.extractForEffectiveRange(new Range<>("AA", "CC", true, false))
                .extractContinuationTokens();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getToken()).isEqualTo("tok1");
        assertThat(tokens.get(0).getRange()).isEqualTo(new Range<>("AA", "CC", true, false));

        tokens = state.extractForEffectiveRange(new Range<>("AB", "BB", true, false))
            .extractContinuationTokens();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getToken()).isEqualTo("tok1");
        assertThat(tokens.get(0).getRange()).isEqualTo(new Range<>("AB", "BB", true, false));
    }

    @Test(groups = "unit")
    public void changeFeedState_extractForEffectiveRange_lastTokenExactMatch() {
        ChangeFeedState state = createStateWithManyTokens(5);

        String min4 = String.format("%06X", 4);
        String max5 = String.format("%06X", 5);
        List<CompositeContinuationToken> tokens =
            state.extractForEffectiveRange(new Range<>(min4, max5, true, false))
                .extractContinuationTokens();

        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getToken()).isEqualTo("token_4");
    }

    @Test(groups = "unit")
    public void changeFeedState_extractForEffectiveRange_fullRange() {
        int tokenCount = 20;
        ChangeFeedState state = createStateWithManyTokens(tokenCount);

        List<CompositeContinuationToken> tokens = state.extractContinuationTokens();

        assertThat(tokens).hasSize(tokenCount);
        for (int i = 0; i < tokenCount; i++) {
            assertThat(tokens.get(i).getToken()).isEqualTo("token_" + i);
        }
    }

    @Test(groups = "unit")
    public void changeFeedState_extractForEffectiveRange_partialOverlapAcrossTokenBoundary() {
        String continuationAAToCC = "tok_aa_cc";
        String continuationCCToEE = "tok_cc_ee";
        ChangeFeedState state = this.createDefaultStateWithContinuation(continuationAAToCC, continuationCCToEE);

        List<CompositeContinuationToken> tokens =
            state.extractForEffectiveRange(new Range<>("BB", "DD", true, false))
                .extractContinuationTokens();

        assertThat(tokens).hasSize(2);
        assertThat(tokens.get(0).getRange()).isEqualTo(new Range<>("BB", "CC", true, false));
        assertThat(tokens.get(0).getToken()).isEqualTo(continuationAAToCC);
        assertThat(tokens.get(1).getRange()).isEqualTo(new Range<>("CC", "DD", true, false));
        assertThat(tokens.get(1).getToken()).isEqualTo(continuationCCToEE);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void changeFeedState_extractForEffectiveRange_noOverlapReturnsEmpty() {
        ChangeFeedState state = createStateWithManyTokens(5);
        state.extractForEffectiveRange(new Range<>("000010", "000011", true, false));
    }

    @Test(groups = "unit")
    public void changeFeedState_extractForEffectiveRange_unsortedInput() {
        ChangeFeedState state = createStateWithTokenRanges(new String[][] {
            {"CC", "DD"}, {"AA", "BB"}, {"EE", "FF"}
        });

        List<CompositeContinuationToken> allTokens =
            state.extractForEffectiveRange(new Range<>("AA", "FF", true, false))
                .extractContinuationTokens();

        assertThat(allTokens).hasSize(3);
        assertThat(allTokens.get(0).getRange().getMin()).isEqualTo("AA");
        assertThat(allTokens.get(1).getRange().getMin()).isEqualTo("CC");
        assertThat(allTokens.get(2).getRange().getMin()).isEqualTo("EE");

        List<CompositeContinuationToken> middleToken =
            state.extractForEffectiveRange(new Range<>("CC", "DD", true, false))
                .extractContinuationTokens();

        assertThat(middleToken).hasSize(1);
        assertThat(middleToken.get(0).getRange()).isEqualTo(new Range<>("CC", "DD", true, false));
    }

    @Test(groups = "unit")
    public void changeFeedState_extractContinuationTokens_nullContinuation() {
        String containerRid = "/cols/" + UUID.randomUUID();
        String pkRangeId = UUID.randomUUID().toString();
        FeedRangePartitionKeyRangeImpl feedRange = new FeedRangePartitionKeyRangeImpl(pkRangeId);

        ChangeFeedState state = new ChangeFeedStateV1(
            containerRid, feedRange, ChangeFeedMode.INCREMENTAL,
            ChangeFeedStartFromInternal.createFromNow(), null);

        List<CompositeContinuationToken> tokens = state.extractContinuationTokens();
        assertThat(tokens).isEmpty();
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void changeFeedState_extractForEffectiveRange_nullContinuation_throws() {
        String containerRid = "/cols/" + UUID.randomUUID();
        String pkRangeId = UUID.randomUUID().toString();
        FeedRangePartitionKeyRangeImpl feedRange = new FeedRangePartitionKeyRangeImpl(pkRangeId);

        ChangeFeedState state = new ChangeFeedStateV1(
            containerRid, feedRange, ChangeFeedMode.INCREMENTAL,
            ChangeFeedStartFromInternal.createFromNow(), null);

        state.extractForEffectiveRange(new Range<>("AA", "BB", true, false));
    }

    @Test(groups = "unit")
    public void changeFeedState_extractForEffectiveRange_binarySearchBacktrack() {
        // Tokens: [000000,000002), [000002,000004), [000004,000006)
        // Query:  [000003,000005) — starts mid-way through the second token
        // binarySearch returns insertionPoint=2 (negative), so startIndex = Math.max(0, 2-2) = 1
        // This exercises the backtrack path where startIndex > 0 after binary search
        ChangeFeedState state = createStateWithTokenRanges(new String[][] {
            {"000000", "000002"}, {"000002", "000004"}, {"000004", "000006"}
        });

        List<CompositeContinuationToken> tokens =
            state.extractForEffectiveRange(new Range<>("000003", "000005", true, false))
                .extractContinuationTokens();

        assertThat(tokens).hasSize(2);
        assertThat(tokens.get(0).getRange()).isEqualTo(new Range<>("000003", "000004", true, false));
        assertThat(tokens.get(0).getToken()).isEqualTo("tok_1");
        assertThat(tokens.get(1).getRange()).isEqualTo(new Range<>("000004", "000005", true, false));
        assertThat(tokens.get(1).getToken()).isEqualTo("tok_2");

        // Also verify batch API produces the same result
        List<ChangeFeedState> batchResults = state.extractForEffectiveRanges(
            Collections.singletonList(new Range<>("000003", "000005", true, false)));
        assertThat(batchResults).hasSize(1);
        assertThat(batchResults.get(0).toString())
            .isEqualTo(state.extractForEffectiveRange(new Range<>("000003", "000005", true, false)).toString());
    }

    private ChangeFeedState createStateWithManyTokens(int tokenCount) {
        String containerRid = "/cols/" + UUID.randomUUID();
        String pkRangeId = UUID.randomUUID().toString();
        FeedRangePartitionKeyRangeImpl feedRange = new FeedRangePartitionKeyRangeImpl(pkRangeId);

        StringBuilder continuationEntries = new StringBuilder();
        for (int i = 0; i < tokenCount; i++) {
            if (i > 0) {
                continuationEntries.append(",");
            }
            String min = String.format("%06X", i);
            String max = String.format("%06X", i + 1);
            continuationEntries.append(
                String.format("{\"token\":\"token_%d\",\"range\":{\"min\":\"%s\",\"max\":\"%s\"}}", i, min, max));
        }

        String continuationJson = String.format(
            "{\"V\":1,\"Rid\":\"%s\",\"Continuation\":[%s],\"PKRangeId\":\"%s\"}",
            containerRid, continuationEntries, pkRangeId);

        FeedRangeContinuation continuation = FeedRangeContinuation.convert(continuationJson);
        return new ChangeFeedStateV1(
            containerRid, feedRange, ChangeFeedMode.INCREMENTAL,
            ChangeFeedStartFromInternal.createFromNow(), continuation);
    }

    private ChangeFeedState createStateWithTokenRanges(String[][] tokenRanges) {
        String containerRid = "/cols/" + UUID.randomUUID();
        String pkRangeId = UUID.randomUUID().toString();
        FeedRangePartitionKeyRangeImpl feedRange = new FeedRangePartitionKeyRangeImpl(pkRangeId);

        StringBuilder entries = new StringBuilder();
        for (int i = 0; i < tokenRanges.length; i++) {
            if (i > 0) {
                entries.append(",");
            }
            entries.append(String.format(
                "{\"token\":\"tok_%d\",\"range\":{\"min\":\"%s\",\"max\":\"%s\"}}",
                i, tokenRanges[i][0], tokenRanges[i][1]));
        }

        String continuationJson = String.format(
            "{\"V\":1,\"Rid\":\"%s\",\"Continuation\":[%s],\"PKRangeId\":\"%s\"}",
            containerRid, entries, pkRangeId);

        FeedRangeContinuation continuation = FeedRangeContinuation.convert(continuationJson);
        return new ChangeFeedStateV1(
            containerRid, feedRange, ChangeFeedMode.INCREMENTAL,
            ChangeFeedStartFromInternal.createFromNow(), continuation);
    }

}
