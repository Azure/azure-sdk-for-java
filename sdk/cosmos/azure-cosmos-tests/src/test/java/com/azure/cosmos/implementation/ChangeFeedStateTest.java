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
            String token = "token_" + i;
            continuationEntries.append(
                String.format("{\"token\":\"%s\",\"range\":{\"min\":\"%s\",\"max\":\"%s\"}}", token, min, max));
        }

        String continuationJson = String.format(
            "{\"V\":1,\"Rid\":\"%s\",\"Continuation\":[%s],\"PKRangeId\":\"%s\"}",
            containerRid,
            continuationEntries,
            pkRangeId);

        FeedRangeContinuation continuation = FeedRangeContinuation.convert(continuationJson);
        return new ChangeFeedStateV1(
            containerRid,
            feedRange,
            ChangeFeedMode.INCREMENTAL,
            ChangeFeedStartFromInternal.createFromNow(),
            continuation);
    }

    @Test(groups = "unit")
    public void changeFeedState_extractForEffectiveRange_multipleCallsProduceCorrectResults() {
        // Verify that calling extractForEffectiveRange multiple times on the same
        // state with different ranges returns correct tokens each time (sort-once caching)
        int tokenCount = 100;
        ChangeFeedState state = createStateWithManyTokens(tokenCount);

        // Extract first range: tokens 0-9
        String min0 = String.format("%06X", 0);
        String max10 = String.format("%06X", 10);
        List<CompositeContinuationToken> tokens1 =
            state.extractForEffectiveRange(new Range<>(min0, max10, true, false))
                .extractContinuationTokens();

        assertThat(tokens1).hasSize(10);
        for (int i = 0; i < 10; i++) {
            assertThat(tokens1.get(i).getToken()).isEqualTo("token_" + i);
            assertThat(tokens1.get(i).getRange().getMin()).isEqualTo(String.format("%06X", i));
            assertThat(tokens1.get(i).getRange().getMax()).isEqualTo(String.format("%06X", i + 1));
        }

        // Extract middle range: tokens 50-59
        String min50 = String.format("%06X", 50);
        String max60 = String.format("%06X", 60);
        List<CompositeContinuationToken> tokens2 =
            state.extractForEffectiveRange(new Range<>(min50, max60, true, false))
                .extractContinuationTokens();

        assertThat(tokens2).hasSize(10);
        for (int i = 0; i < 10; i++) {
            assertThat(tokens2.get(i).getToken()).isEqualTo("token_" + (50 + i));
        }

        // Extract last range: tokens 90-99
        String min90 = String.format("%06X", 90);
        String max100 = String.format("%06X", 100);
        List<CompositeContinuationToken> tokens3 =
            state.extractForEffectiveRange(new Range<>(min90, max100, true, false))
                .extractContinuationTokens();

        assertThat(tokens3).hasSize(10);
        for (int i = 0; i < 10; i++) {
            assertThat(tokens3.get(i).getToken()).isEqualTo("token_" + (90 + i));
        }

        // Extract single token
        String min25 = String.format("%06X", 25);
        String max26 = String.format("%06X", 26);
        List<CompositeContinuationToken> tokens4 =
            state.extractForEffectiveRange(new Range<>(min25, max26, true, false))
                .extractContinuationTokens();

        assertThat(tokens4).hasSize(1);
        assertThat(tokens4.get(0).getToken()).isEqualTo("token_25");
    }

    @Test(groups = "unit")
    public void changeFeedState_extractForEffectiveRange_largeScale() {
        // Test with a large number of tokens to verify the optimization prevents
        // quadratic behavior. With 10,000 tokens and extracting for each partition,
        // the optimized code should complete within a reasonable time.
        int tokenCount = 10000;
        ChangeFeedState state = createStateWithManyTokens(tokenCount);

        long startTime = System.nanoTime();

        // Extract for every individual partition range (simulates Spark planning)
        for (int i = 0; i < tokenCount; i++) {
            String min = String.format("%06X", i);
            String max = String.format("%06X", i + 1);
            List<CompositeContinuationToken> tokens =
                state.extractForEffectiveRange(new Range<>(min, max, true, false))
                    .extractContinuationTokens();

            assertThat(tokens).hasSize(1);
            assertThat(tokens.get(0).getToken()).isEqualTo("token_" + i);
        }

        long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;

        // With the optimization, 10k partitions × 10k tokens should complete
        // well under 30 seconds even on slow CI agents. Without the optimization
        // (quadratic), this would take minutes with the sort-per-call overhead.
        // The generous margin prevents flakiness on overloaded agents or GC pauses;
        // the quadratic version would hang the test runner entirely.
        assertThat(elapsedMs)
            .as("10,000 extractions took %d ms, should be < 30,000 ms", elapsedMs)
            .isLessThan(30_000);
    }

    @Test(groups = "unit")
    public void changeFeedState_extractContinuationTokens_nullContinuation() {
        String containerRid = "/cols/" + UUID.randomUUID();
        String pkRangeId = UUID.randomUUID().toString();
        FeedRangePartitionKeyRangeImpl feedRange = new FeedRangePartitionKeyRangeImpl(pkRangeId);

        ChangeFeedState state = new ChangeFeedStateV1(
            containerRid,
            feedRange,
            ChangeFeedMode.INCREMENTAL,
            ChangeFeedStartFromInternal.createFromNow(),
            null);

        // With null continuation, extractContinuationTokens returns empty
        List<CompositeContinuationToken> tokens = state.extractContinuationTokens();
        assertThat(tokens).isEmpty();
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void changeFeedState_extractForEffectiveRange_nullContinuation_throws() {
        String containerRid = "/cols/" + UUID.randomUUID();
        String pkRangeId = UUID.randomUUID().toString();
        FeedRangePartitionKeyRangeImpl feedRange = new FeedRangePartitionKeyRangeImpl(pkRangeId);

        ChangeFeedState state = new ChangeFeedStateV1(
            containerRid,
            feedRange,
            ChangeFeedMode.INCREMENTAL,
            ChangeFeedStartFromInternal.createFromNow(),
            null);

        // With null continuation, extractForEffectiveRange passes an empty token list to
        // FeedRangeContinuation.create(), which throws IllegalArgumentException.
        state.extractForEffectiveRange(new Range<>("AA", "BB", true, false));
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

        // Full range overlap
        List<CompositeContinuationToken> tokens =
            state.extractForEffectiveRange(new Range<>("AA", "CC", true, false))
                .extractContinuationTokens();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getToken()).isEqualTo("tok1");
        assertThat(tokens.get(0).getRange()).isEqualTo(new Range<>("AA", "CC", true, false));

        // Partial overlap
        tokens = state.extractForEffectiveRange(new Range<>("AB", "BB", true, false))
            .extractContinuationTokens();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getToken()).isEqualTo("tok1");
        assertThat(tokens.get(0).getRange()).isEqualTo(new Range<>("AB", "BB", true, false));
    }

    @Test(groups = "unit")
    public void changeFeedState_extractForEffectiveRange_lastTokenExactMatch() {
        // When query range exactly matches the last token, verify it is found correctly.
        ChangeFeedState state = createStateWithManyTokens(5);

        // Range overlapping the last token
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

        // Extract full range should return all tokens
        List<CompositeContinuationToken> tokens = state.extractContinuationTokens();

        assertThat(tokens).hasSize(tokenCount);
        for (int i = 0; i < tokenCount; i++) {
            assertThat(tokens.get(i).getToken()).isEqualTo("token_" + i);
        }
    }

    @Test(groups = "unit")
    public void changeFeedState_extractForEffectiveRange_partialOverlapAcrossTokenBoundary() {
        // Two tokens: [AA, CC) and [CC, EE), query range [BB, DD)
        // should get both tokens trimmed to [BB, CC) and [CC, DD)
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
        // Query range entirely outside all token ranges should cause extractForEffectiveRange
        // to throw, because FeedRangeContinuation.create rejects empty token lists.
        ChangeFeedState state = createStateWithManyTokens(5);

        // All tokens are in [000000, 000005). Query a range beyond all tokens.
        state.extractForEffectiveRange(new Range<>("000010", "000011", true, false));
    }

    @Test(groups = "unit")
    public void changeFeedState_extractForEffectiveRange_fallbackCompleteMiss() {
        // Tests the fallback path when binary search completely misses overlapping tokens.
        // Token [00, EE) is a wide range, [01, 02) is narrow inside it.
        // Query [DD, EE) overlaps [00, EE) but binary search starts at index 1 ([01, 02)),
        // which doesn't overlap [DD, EE). Fallback scans from 0 and finds [00, EE).
        ChangeFeedState state = createStateWithTokenRanges(new String[][] {
            {"00", "EE"},
            {"01", "02"}
        });

        List<CompositeContinuationToken> tokens =
            state.extractForEffectiveRange(new Range<>("DD", "EE", true, false))
                .extractContinuationTokens();

        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getToken()).isEqualTo("tok_0");
        assertThat(tokens.get(0).getRange()).isEqualTo(new Range<>("DD", "EE", true, false));
    }

    @Test(groups = "unit")
    public void changeFeedState_extractForEffectiveRange_fallbackPartialMiss() {
        // Tests the fallback path for partial misses (F1 fix): binary search finds some
        // overlapping tokens but misses earlier ones.
        // Token [00, EE) is wide, [02, 03) is narrow, [0B, 0C) is later.
        // Query [0B, EE): binary search starts at index 2 ([0B, 0C)), finding one overlap.
        // Fallback scans indices 0-1 and finds [00, EE) also overlaps, catching the partial miss.
        ChangeFeedState state = createStateWithTokenRanges(new String[][] {
            {"00", "EE"},
            {"02", "03"},
            {"0B", "0C"}
        });

        List<CompositeContinuationToken> tokens =
            state.extractForEffectiveRange(new Range<>("0B", "EE", true, false))
                .extractContinuationTokens();

        // Should find both [00, EE) trimmed to [0B, EE) and [0B, 0C) trimmed to [0B, 0C)
        assertThat(tokens).hasSize(2);
        assertThat(tokens.get(0).getToken()).isEqualTo("tok_0");
        assertThat(tokens.get(0).getRange()).isEqualTo(new Range<>("0B", "EE", true, false));
        assertThat(tokens.get(1).getToken()).isEqualTo("tok_2");
        assertThat(tokens.get(1).getRange()).isEqualTo(new Range<>("0B", "0C", true, false));
    }

    @Test(groups = "unit")
    public void changeFeedState_extractForEffectiveRange_unsortedInput() {
        // Tokens provided in reverse order to verify that the sort is actually exercised.
        // Without sorting, binary search and overlap detection would produce wrong results.
        ChangeFeedState state = createStateWithTokenRanges(new String[][] {
            {"CC", "DD"},
            {"AA", "BB"},
            {"EE", "FF"}
        });

        // After sorting, tokens should be [AA,BB), [CC,DD), [EE,FF)
        // Query [AA, FF) should return all three
        List<CompositeContinuationToken> allTokens =
            state.extractForEffectiveRange(new Range<>("AA", "FF", true, false))
                .extractContinuationTokens();

        assertThat(allTokens).hasSize(3);
        // Verify sorted order in results
        assertThat(allTokens.get(0).getRange().getMin()).isEqualTo("AA");
        assertThat(allTokens.get(1).getRange().getMin()).isEqualTo("CC");
        assertThat(allTokens.get(2).getRange().getMin()).isEqualTo("EE");

        // Query a specific middle range
        List<CompositeContinuationToken> middleToken =
            state.extractForEffectiveRange(new Range<>("CC", "DD", true, false))
                .extractContinuationTokens();

        assertThat(middleToken).hasSize(1);
        assertThat(middleToken.get(0).getRange()).isEqualTo(new Range<>("CC", "DD", true, false));
    }

    @Test(groups = "unit")
    public void changeFeedState_extractForEffectiveRange_cacheInvalidatedAfterSetContinuation() {
        // Verify that replacing the continuation via setContinuation() invalidates
        // the cached sorted snapshot, ensuring subsequent extractions use the new tokens.
        String containerRid = "/cols/" + UUID.randomUUID();
        String pkRangeId = UUID.randomUUID().toString();
        FeedRangePartitionKeyRangeImpl feedRange = new FeedRangePartitionKeyRangeImpl(pkRangeId);

        // Create state with 10 tokens
        ChangeFeedState state = createStateWithFixedRid(containerRid, pkRangeId, feedRange, 10);

        // Prime the cache by extracting a range
        List<CompositeContinuationToken> tokensBefore =
            state.extractForEffectiveRange(new Range<>(
                String.format("%06X", 0), String.format("%06X", 5), true, false))
                .extractContinuationTokens();
        assertThat(tokensBefore).hasSize(5);

        // Build a replacement continuation with only 3 tokens but same containerRid
        ChangeFeedState newState = createStateWithFixedRid(containerRid, pkRangeId, feedRange, 3);
        state.setContinuation(newState.getContinuation());

        // Extract all tokens - should reflect the new 3-token continuation, not the stale 10-token cache
        List<CompositeContinuationToken> tokensAfter = state.extractContinuationTokens();
        assertThat(tokensAfter).hasSize(3);
        for (int i = 0; i < 3; i++) {
            assertThat(tokensAfter.get(i).getToken()).isEqualTo("token_" + i);
        }
    }

    private ChangeFeedState createStateWithFixedRid(
        String containerRid,
        String pkRangeId,
        FeedRangePartitionKeyRangeImpl feedRange,
        int tokenCount) {

        StringBuilder continuationEntries = new StringBuilder();
        for (int i = 0; i < tokenCount; i++) {
            if (i > 0) {
                continuationEntries.append(",");
            }
            String min = String.format("%06X", i);
            String max = String.format("%06X", i + 1);
            String token = "token_" + i;
            continuationEntries.append(
                String.format("{\"token\":\"%s\",\"range\":{\"min\":\"%s\",\"max\":\"%s\"}}", token, min, max));
        }

        String continuationJson = String.format(
            "{\"V\":1,\"Rid\":\"%s\",\"Continuation\":[%s],\"PKRangeId\":\"%s\"}",
            containerRid, continuationEntries, pkRangeId);

        FeedRangeContinuation continuation = FeedRangeContinuation.convert(continuationJson);
        return new ChangeFeedStateV1(
            containerRid,
            feedRange,
            ChangeFeedMode.INCREMENTAL,
            ChangeFeedStartFromInternal.createFromNow(),
            continuation);
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
            containerRid,
            feedRange,
            ChangeFeedMode.INCREMENTAL,
            ChangeFeedStartFromInternal.createFromNow(),
            continuation);
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
}
