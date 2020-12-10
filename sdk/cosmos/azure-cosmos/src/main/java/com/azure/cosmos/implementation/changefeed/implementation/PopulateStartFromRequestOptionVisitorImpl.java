// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import java.time.Instant;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.feedranges.FeedRangeRxDocumentServiceRequestPopulatorVisitorImpl;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

class PopulateStartFromRequestOptionVisitorImpl extends ChangeFeedStartFromVisitor {

    public static final PopulateStartFromRequestOptionVisitorImpl SINGLETON =
        new PopulateStartFromRequestOptionVisitorImpl();

    private static final long START_FROM_BEGINNING_EPOCH_SECONDS = -62135596800L;
    private static final Instant START_FROM_BEGINNING_TIME =
        Instant.ofEpochSecond(START_FROM_BEGINNING_EPOCH_SECONDS);

    public PopulateStartFromRequestOptionVisitorImpl() {
    }

    @Override
    public void visit(
        ChangeFeedStartFromNowImpl startFromNow,
        RxDocumentServiceRequest request) {

        checkNotNull(startFromNow, "Argument 'startFromNow' must not be null.");
        checkNotNull(request, "Argument 'request' must not be null.");

        request.getHeaders().put(
            HttpConstants.HttpHeaders.IF_NONE_MATCH,
            HttpConstants.HeaderValues.IF_NONE_MATCH_ALL);
    }

    @Override
    public void visit(
        ChangeFeedStartFromPointInTimeImpl startFromTime,
        RxDocumentServiceRequest request) {

        checkNotNull(startFromTime, "Argument 'startFromTime' must not be null.");
        checkNotNull(request, "Argument 'request' must not be null.");

        // Our current public contract for ChangeFeedProcessor uses DateTime.MinValue.ToUniversalTime as beginning.
        // We need to add a special case here, otherwise it would send it as normal StartTime.
        // The problem is Multi master accounts do not support StartTime header on ReadFeed, and thus,
        // it would break multi master Change Feed Processor users using Start From Beginning semantics.
        // It's also an optimization, since the backend won't have to binary search for the value.
        Instant pointInTime = startFromTime.getPointInTime();
        if (pointInTime != START_FROM_BEGINNING_TIME)
        {
            request.getHeaders().put(
                HttpConstants.HttpHeaders.IF_MODIFIED_SINCE,
                Utils.instantAsUTCRFC1123(pointInTime));
        }
    }

    @Override
    public void visit(
        ChangeFeedStartFromETagAndFeedRangeImpl startFromEtagAndFeedRange,
        RxDocumentServiceRequest request) {

        checkNotNull(startFromEtagAndFeedRange, "Argument 'startFromEtagAndFeedRange' must not be null.");
        checkNotNull(request, "Argument 'request' must not be null.");

        if (startFromEtagAndFeedRange.getETag() != null) {
            // On REST level, change feed is using IfNoneMatch/ETag instead of continuation
            request.getHeaders().put(
                HttpConstants.HttpHeaders.IF_NONE_MATCH,
                startFromEtagAndFeedRange.getETag());
        }

        startFromEtagAndFeedRange.getFeedRange().accept(
            FeedRangeRxDocumentServiceRequestPopulatorVisitorImpl.SINGLETON,
            request);
    }

    @Override
    public void visit(
        ChangeFeedStartFromBeginningImpl startFromBeginning,
        RxDocumentServiceRequest request) {

        checkNotNull(startFromBeginning, "Argument 'startFromBeginning' must not be null.");
        checkNotNull(request, "Argument 'request' must not be null.");

        // We don't need to set any headers to start from the beginning
    }
}