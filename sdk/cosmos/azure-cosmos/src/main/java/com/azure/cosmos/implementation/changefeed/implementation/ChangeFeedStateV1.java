package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.query.CompositeContinuationToken;

import static com.azure.cosmos.BridgeInternal.setProperty;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class ChangeFeedStateV1 extends ChangeFeedState{
    private final ChangeFeedMode mode;
    private final String containerRid;
    private final FeedRangeInternal feedRange;
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
    public ChangeFeedState setContinuation(FeedRangeContinuation continuation) {
        checkNotNull(continuation, "Argument 'continuation' must not be null.");
        continuation.validateContainer(this.containerRid);
        this.continuation = continuation;

        return this;
    }

    @Override
    public FeedRangeContinuation getContinuation() {
        return this.continuation;
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
    public String applyServerResponseContinuation(String serverContinuationToken) {
        checkNotNull(serverContinuationToken, "Argument 'serverContinuationToken' must not be null");

        if (this.continuation == null) {
            this.continuation = FeedRangeContinuation.createForFullFeedRange(this.containerRid, this.feedRange);
        }

        this.continuation.replaceContinuation(serverContinuationToken);
        return this.toJson();
    }

    @Override
    public void populateRequest(RxDocumentServiceRequest request, int maxItemCount) {
        // Page size
        request.getHeaders().put(
            HttpConstants.HttpHeaders.PAGE_SIZE,
            String.valueOf(maxItemCount));

        if (this.mode == ChangeFeedMode.INCREMENTAL) {
            request.getHeaders().put(
                HttpConstants.HttpHeaders.A_IM,
                HttpConstants.A_IMHeaderValues.INCREMENTAL_FEED);
        } else {

            // TODO fabianm implement full fiedelity
            throw new IllegalStateException("Unsupported change feed mode");
        }

        final FeedRangeInternal effectiveFeedRange;
        final ChangeFeedStartFromInternal effectiveStartFrom;
        final CompositeContinuationToken continuationToken;
        if (this.continuation != null) {
            continuationToken = this.continuation.getCurrentContinuationToken();
        } else {
            continuationToken = null;
        }

        if (continuationToken == null) {
            effectiveFeedRange = this.feedRange;
            effectiveStartFrom = this.startFromSettings;
        }
        else {
            effectiveFeedRange = new FeedRangeEpkImpl(continuationToken.getRange());
            effectiveStartFrom = new ChangeFeedStartFromEtagAndFeedRangeImpl(
                continuationToken.getToken(),
                effectiveFeedRange);
        }

        //TODO fabianm FINSIH this
        Finish this
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
}
