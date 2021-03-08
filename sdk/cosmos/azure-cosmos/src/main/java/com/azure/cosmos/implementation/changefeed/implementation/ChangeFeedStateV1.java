// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.query.CompositeContinuationToken;

import java.util.Objects;

import static com.azure.cosmos.BridgeInternal.setProperty;
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
        RxDocumentServiceRequest request) {

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

        this.continuation.replaceContinuation(serverContinuationToken);
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

        effectiveStartFrom.populateRequest(request);
    }

    @Override
    public void populatePropertyBag() {
        super.populatePropertyBag();

        setProperty(
            this,
            Constants.Properties.CHANGE_FEED_STATE_VERSION,
            ChangeFeedStateVersions.V1);

        setProperty(
            this,
            Constants.Properties.CHANGE_FEED_STATE_RESOURCE_ID,
            this.containerRid);

        setProperty(
            this,
            Constants.Properties.CHANGE_FEED_STATE_MODE,
            this.mode);

        setProperty(
            this,
            Constants.Properties.CHANGE_FEED_STATE_START_FROM,
            this.startFromSettings);

        if (this.continuation != null) {
            this.continuation.populatePropertyBag();
            setProperty(
                this,
                Constants.Properties.CHANGE_FEED_STATE_CONTINUATION,
                this.continuation);
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
        switch (this.mode) {
            case INCREMENTAL:
                request.getHeaders().put(
                    HttpConstants.HttpHeaders.A_IM,
                    HttpConstants.A_IMHeaderValues.INCREMENTAL_FEED);
                break;
            case FULL_FIDELITY:
                request.getHeaders().put(
                    HttpConstants.HttpHeaders.A_IM,
                    HttpConstants.A_IMHeaderValues.FullFidelityFeed);
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
