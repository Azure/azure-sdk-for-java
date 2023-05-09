// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyRangeImpl;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PartitionProcessorHelperTests {

    @DataProvider(name = "changeFeedModeArgProvider")
    public Object[][] changeFeedModeArgProvider() {
        return new Object[][]{
            { ChangeFeedMode.INCREMENTAL },
            { ChangeFeedMode.FULL_FIDELITY }
        };
    }

    @Test(groups = "unit", dataProvider = "changeFeedModeArgProvider")
    public void createChangeFeedRequestOptionsForChangeFeedState(ChangeFeedMode changeFeedMode) {
        String containerRid = "/cols/" + UUID.randomUUID();
        String pkRangeId = UUID.randomUUID().toString();
        FeedRangePartitionKeyRangeImpl feedRange = new FeedRangePartitionKeyRangeImpl(pkRangeId);
        ChangeFeedStartFromInternal startFromSettings = ChangeFeedStartFromInternal.createFromNow();
        ChangeFeedState state = new ChangeFeedStateV1(
            containerRid,
            feedRange,
            changeFeedMode,
            startFromSettings,
            null);

        CosmosChangeFeedRequestOptions cosmosChangeFeedRequestOptions =
            PartitionProcessorHelper.createChangeFeedRequestOptionsForChangeFeedState(
                state, 100, changeFeedMode);

        Map<String, String> customOptions =
            ImplementationBridgeHelpers
                .CosmosChangeFeedRequestOptionsHelper
                .getCosmosChangeFeedRequestOptionsAccessor()
                .getHeader(cosmosChangeFeedRequestOptions);

        assertThat(customOptions).isNotNull();
        assertThat(customOptions.get(HttpConstants.HttpHeaders.CHANGE_FEED_WIRE_FORMAT_VERSION)).isEqualTo(
            HttpConstants.ChangeFeedWireFormatVersions.SEPARATE_METADATA_WITH_CRTS);
    }

    @Test(groups = "unit", dataProvider = "changeFeedModeArgProvider")
    public void createForProcessingFromContinuation(ChangeFeedMode changeFeedMode) {
        String containerRid = "/cols/" + UUID.randomUUID();
        String pkRangeId = UUID.randomUUID().toString();
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

        FeedRangePartitionKeyRangeImpl feedRange = new FeedRangePartitionKeyRangeImpl(pkRangeId);
        ChangeFeedStartFromInternal startFromSettings = ChangeFeedStartFromInternal.createFromNow();
        FeedRangeContinuation continuation = FeedRangeContinuation.convert(continuationJson);

        ChangeFeedState state = new ChangeFeedStateV1(
            containerRid,
            feedRange,
            changeFeedMode,
            startFromSettings,
            continuation);

        CosmosChangeFeedRequestOptions cosmosChangeFeedRequestOptions =
            PartitionProcessorHelper.createForProcessingFromContinuation(state.toString(), changeFeedMode);

        Map<String, String> customOptions =
            ImplementationBridgeHelpers
                .CosmosChangeFeedRequestOptionsHelper
                .getCosmosChangeFeedRequestOptionsAccessor()
                .getHeader(cosmosChangeFeedRequestOptions);

        assertThat(customOptions).isNotNull();
        assertThat(customOptions.get(HttpConstants.HttpHeaders.CHANGE_FEED_WIRE_FORMAT_VERSION)).isEqualTo(
            HttpConstants.ChangeFeedWireFormatVersions.SEPARATE_METADATA_WITH_CRTS);
    }
}
