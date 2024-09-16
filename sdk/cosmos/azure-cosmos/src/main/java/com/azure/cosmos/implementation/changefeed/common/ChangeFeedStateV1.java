// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.query.CompositeContinuationToken;

import java.util.Objects;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class ChangeFeedStateV1 extends ChangeFeedState {
    private final String containerRid;
    private final FeedRangeInternal feedRange;
    private final ChangeFeedMode mode;
    private final ChangeFeedStartFromInternal startFromSettings;
    private FeedRangeContinuation continuation;

    public ChangeFeedStateV1(
        String containerRid,
        FeedRangeInternal feedRange,
        ChangeFeedMode mode,
        ChangeFeedStartFromInternal startFromSettings,
        FeedRangeContinuation continuation) {

        super();

        this.containerRid = containerRid;
        this.feedRange = feedRange;
        this.startFromSettings = startFromSettings;
        this.continuation = continuation;
        this.mode = mode;
    }

    @Override
    public FeedRangeContinuation getContinuation() {
        return this.continuation;
    }

    @Override
    public ChangeFeedState setContinuation(FeedRangeContinuation continuation) {
        checkNotNull(continuation, "Argument 'continuation' must not be null.");
        continuation.validateContainer(this.containerRid);
        this.continuation = continuation;

        return this;
    }

    @Override
    public FeedRangeInternal getFeedRange() {
        return this.feedRange;
    }

    @Override
    public ChangeFeedMode getMode() {
        return this.mode;
    }

    @Override
    public ChangeFeedStartFromInternal getStartFromSettings() {
        return this.startFromSettings;
    }

    @Override
    public String applyServerResponseContinuation(
        String serverContinuationToken,
        RxDocumentServiceRequest request,
        boolean shouldMoveToNextTokenOnETagReplace) {

        checkNotNull(
            serverContinuationToken,
            "Argument 'serverContinuationToken' must not be null");
        checkNotNull(request, "Argument 'request' must not be null");

        if (this.continuation == null) {
            this.continuation = FeedRangeContinuation.create(
                this.containerRid,
                request.getFeedRange() != null ?
                    request.getFeedRange()
                    : new FeedRangeEpkImpl(request.getEffectiveRange()),
                request.getEffectiveRange());
        }

        this.continuation.replaceContinuation(serverContinuationToken, shouldMoveToNextTokenOnETagReplace);
        return this.toString();
    }

    @Override
    public String getContainerRid() {
        return this.containerRid;
    }

    private void populateEffectiveRangeAndStartFromSettingsToRequest(RxDocumentServiceRequest request) {
        final ChangeFeedStartFromInternal effectiveStartFrom;
        final CompositeContinuationToken continuationToken;
        if (this.continuation != null) {
            continuationToken = this.continuation.getCurrentContinuationToken();
            request.applyFeedRangeFilter(this.continuation.getFeedRange());
        } else {
            continuationToken = null;
            request.applyFeedRangeFilter(this.feedRange);
        }

        if (continuationToken == null || continuationToken.getToken() == null) {
            effectiveStartFrom = this.startFromSettings;
        } else {
            effectiveStartFrom = new ChangeFeedStartFromETagAndFeedRangeImpl(
                continuationToken.getToken(),
                new FeedRangeEpkImpl(continuationToken.getRange()));
        }

        this.populateStartFrom(this.startFromSettings, effectiveStartFrom, request);
    }

    private void populateStartFrom(
        ChangeFeedStartFromInternal initialStartFrom,
        ChangeFeedStartFromInternal effectiveStartFrom,
        RxDocumentServiceRequest request) {

        checkNotNull(initialStartFrom, "Argument 'initialStartFrom' should not be null");
        checkNotNull(effectiveStartFrom, "Argument 'effectiveStartFromSettings' should not be null");
        checkNotNull(request, "Argument 'request' should not be null");

        // When a merge happens, the child partition will contain documents ordered by LSN but the _ts/creation time
        // of the documents may not be sequential. So when reading the changeFeed by LSN, it is possible to encounter documents with lower _ts.
        // In order to guarantee we always get the documents after customer's point start time, we will need to always pass the start time in the header.
        // NOTE: the sequence of calling populate request order matters here, as both can try to populate the same header, effective ones will win
        initialStartFrom.populateRequest(request, this.mode);
        effectiveStartFrom.populateRequest(request, this.mode);
    }

    @Override
    public void populatePropertyBag() {
        super.populatePropertyBag();

        this.set(
            Constants.Properties.CHANGE_FEED_STATE_VERSION,
            ChangeFeedStateVersions.V1,
            CosmosItemSerializer.DEFAULT_SERIALIZER);

        this.set(
            Constants.Properties.CHANGE_FEED_STATE_RESOURCE_ID,
            this.containerRid,
            CosmosItemSerializer.DEFAULT_SERIALIZER);

        this.set(
            Constants.Properties.CHANGE_FEED_STATE_MODE,
            this.mode,
            CosmosItemSerializer.DEFAULT_SERIALIZER);

        this.set(
            Constants.Properties.CHANGE_FEED_STATE_START_FROM,
            this.startFromSettings,
            CosmosItemSerializer.DEFAULT_SERIALIZER);

        if (this.continuation != null) {
            this.continuation.populatePropertyBag();
            this.set(
                Constants.Properties.CHANGE_FEED_STATE_CONTINUATION,
                this.continuation,
                CosmosItemSerializer.DEFAULT_SERIALIZER);
            this.feedRange.removeProperties(this);
        } else {
            this.feedRange.setProperties(this, true);
        }
    }

    @Override
    public void populateRequest(RxDocumentServiceRequest request, int maxItemCount) {
        // Page size
        request.getHeaders().put(
            HttpConstants.HttpHeaders.PAGE_SIZE,
            String.valueOf(maxItemCount));
        request.getHeaders().put(HttpConstants.HttpHeaders.POPULATE_QUERY_METRICS, String.valueOf(true));
        switch (this.mode) {
            case INCREMENTAL:
                request.getHeaders().put(
                    HttpConstants.HttpHeaders.A_IM,
                    HttpConstants.A_IMHeaderValues.INCREMENTAL_FEED);
                break;
            case FULL_FIDELITY:
                request.getHeaders().put(
                    HttpConstants.HttpHeaders.A_IM,
                    HttpConstants.A_IMHeaderValues.FULL_FIDELITY_FEED);
                request.useGatewayMode = true;
                // Above, defaulting to Gateway is necessary for Full-Fidelity Change Feed since the Split-handling logic resides within Compute Gateway.
                // TODO: If and when, this changes, it will be necessary to remove this.
                break;
            default:
                throw new IllegalStateException("Unsupported change feed mode");
        }

        this.populateEffectiveRangeAndStartFromSettingsToRequest(request);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChangeFeedStateV1)) {
            return false;
        }

        ChangeFeedStateV1 other = (ChangeFeedStateV1)o;
        return Objects.equals(this.feedRange, other.feedRange) &&
            Objects.equals(this.containerRid, other.containerRid) &&
            Objects.equals(this.startFromSettings, other.startFromSettings) &&
            Objects.equals(this.mode, other.mode) &&
            Objects.equals(this.continuation, other.continuation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            this.feedRange,
            this.containerRid,
            this.startFromSettings,
            this.mode,
            this.continuation);
    }
}
