// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedStateV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyRangeImpl;
import com.azure.cosmos.implementation.query.CompositeContinuationToken;
import com.azure.cosmos.implementation.routing.Range;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ChangeFeedStateTest {
    @Test(groups = "unit")
    public void changeFeedState_startFromNow_PKRangeId_toJsonFromJson() {
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

    private ChangeFeedState createStateWithContinuation(String continuationAAToCC, String continuationCCToEE)
    {
        String containerRid = "/cols/" + UUID.randomUUID().toString();
        String pkRangeId = UUID.randomUUID().toString();
        FeedRangePartitionKeyRangeImpl feedRange = new FeedRangePartitionKeyRangeImpl(pkRangeId);
        ChangeFeedStartFromInternal startFromSettings = ChangeFeedStartFromInternal.createFromNow();
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
            pkRangeId);

        FeedRangeContinuation continuation = FeedRangeContinuation.convert(continuationJson);
        return new ChangeFeedStateV1(
            containerRid,
            feedRange,
            ChangeFeedMode.INCREMENTAL,
            startFromSettings,
            continuation);
    }

    @Test(groups = "unit")
    public void changeFeedState_extractContinuationTokens() {
        String continuationAAToCC = UUID.randomUUID().toString();
        String continuationCCToEE = UUID.randomUUID().toString();
        List<CompositeContinuationToken> tokens =
            this
            .createStateWithContinuation(continuationAAToCC, continuationCCToEE)
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
                .createStateWithContinuation(continuationAAToCC, continuationCCToEE)
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
            .createStateWithContinuation(continuationAAToCC, continuationCCToEE);
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
}
