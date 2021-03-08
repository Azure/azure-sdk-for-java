// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedStateV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyRangeImpl;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
}
