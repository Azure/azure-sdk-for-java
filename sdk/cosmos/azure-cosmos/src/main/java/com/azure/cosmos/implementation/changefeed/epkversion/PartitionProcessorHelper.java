// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;

public class PartitionProcessorHelper {
    public static ChangeFeedStartFromInternal getStartFromSettings(
            FeedRangeInternal feedRange,
            ChangeFeedProcessorOptions processorOptions,
            ChangeFeedMode changeFeedMode) {

        switch (changeFeedMode) {
            case INCREMENTAL:
                return IncrementalPartitionProcessorHelper.getStartFromSettings(feedRange, processorOptions);
            case FULL_FIDELITY:
                return FullFidelityPartitionProcessorHelper.getStartFromSettings(feedRange, processorOptions);
            default:
                throw new IllegalStateException("ChangeFeed mode " + changeFeedMode + " is not supported");
        }
    }

    public static CosmosChangeFeedRequestOptions createChangeFeedRequestOptionsForChangeFeedState(
            ChangeFeedState state,
            int maxItemCount,
            ChangeFeedMode changeFeedMode) {
        switch (changeFeedMode) {
            case INCREMENTAL:
                return IncrementalPartitionProcessorHelper.createChangeFeedRequestOptionsForChangeFeedState(state, maxItemCount);
            case FULL_FIDELITY:
                return FullFidelityPartitionProcessorHelper.createChangeFeedRequestOptionsForChangeFeedState(state, maxItemCount);
            default:
                throw new IllegalStateException("ChangeFeed mode " + changeFeedMode + " is not supported");
        }
    }

    public static CosmosChangeFeedRequestOptions createForProcessingFromContinuation(String continuationToken, ChangeFeedMode changeFeedMode) {
        switch (changeFeedMode) {
            case INCREMENTAL:
                return IncrementalPartitionProcessorHelper.createForProcessingFromContinuation(continuationToken);
            case FULL_FIDELITY:
                return FullFidelityPartitionProcessorHelper.createForProcessingFromContinuation(continuationToken);
            default:
                throw new IllegalStateException("ChangeFeed mode " + changeFeedMode + " is not supported");
        }
    }

    private static class IncrementalPartitionProcessorHelper {
        public static ChangeFeedStartFromInternal getStartFromSettings(
                FeedRangeInternal feedRange,
                ChangeFeedProcessorOptions processorOptions) {

            if (!Strings.isNullOrWhiteSpace(processorOptions.getStartContinuation()))
            {
                return ChangeFeedStartFromInternal.createFromETagAndFeedRange(
                        processorOptions.getStartContinuation(),
                        feedRange);
            }

            if (processorOptions.getStartTime() != null) {
                return ChangeFeedStartFromInternal.createFromPointInTime(processorOptions.getStartTime());
            }

            if (processorOptions.isStartFromBeginning()) {
                return ChangeFeedStartFromInternal.createFromBeginning();
            }

            return ChangeFeedStartFromInternal.createFromNow();
        }

        public static CosmosChangeFeedRequestOptions createChangeFeedRequestOptionsForChangeFeedState(
                ChangeFeedState state,
                int maxItemCount) {

            CosmosChangeFeedRequestOptions changeFeedRequestOptions =
                ModelBridgeInternal
                .createChangeFeedRequestOptionsForChangeFeedState(state)
                .setMaxItemCount(maxItemCount);

            // in epk version change feed processor, we are going to use new wire format to be consistent with full fidelity
            ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper.getCosmosChangeFeedRequestOptionsAccessor()
                .setHeader(
                    changeFeedRequestOptions,
                    HttpConstants.HttpHeaders.CHANGE_FEED_WIRE_FORMAT_VERSION,
                    HttpConstants.ChangeFeedWireFormatVersions.SEPARATE_METADATA_WITH_CRTS);

            return changeFeedRequestOptions;
        }

        private static CosmosChangeFeedRequestOptions createForProcessingFromContinuation(String continuationToken) {
            CosmosChangeFeedRequestOptions changeFeedRequestOptions =
                CosmosChangeFeedRequestOptions.createForProcessingFromContinuation(continuationToken);
            // in epk version change feed processor, we are going to use new wire format to be consistent with full fidelity
            ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper.getCosmosChangeFeedRequestOptionsAccessor()
                .setHeader(
                    changeFeedRequestOptions,
                    HttpConstants.HttpHeaders.CHANGE_FEED_WIRE_FORMAT_VERSION,
                    HttpConstants.ChangeFeedWireFormatVersions.SEPARATE_METADATA_WITH_CRTS);

            return changeFeedRequestOptions;
        }
    }

    private static class FullFidelityPartitionProcessorHelper {
        public static ChangeFeedStartFromInternal getStartFromSettings(
                FeedRangeInternal feedRange,
                ChangeFeedProcessorOptions processorOptions) {

            if (!Strings.isNullOrWhiteSpace(processorOptions.getStartContinuation())) {
                ChangeFeedState changeFeedState = ChangeFeedStateV1.fromString(processorOptions.getStartContinuation());
                return ChangeFeedStartFromInternal.createFromETagAndFeedRange(
                        changeFeedState.getContinuation().getCurrentContinuationToken().getToken(),
                        feedRange);
            }
            return ChangeFeedStartFromInternal.createFromNow();
        }

        public static CosmosChangeFeedRequestOptions createChangeFeedRequestOptionsForChangeFeedState(
                ChangeFeedState state,
                int maxItemCount) {

            return ModelBridgeInternal
                    .createChangeFeedRequestOptionsForChangeFeedState(state)
                    .setMaxItemCount(maxItemCount)
                    .allVersionsAndDeletes();
        }

        private static CosmosChangeFeedRequestOptions createForProcessingFromContinuation(String continuationToken) {

            return CosmosChangeFeedRequestOptions
                    .createForProcessingFromContinuation(continuationToken)
                    .allVersionsAndDeletes();
        }
    }
}
