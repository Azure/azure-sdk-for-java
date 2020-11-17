// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;

class PopulateStartFromRequestOptionVisitorImpl extends ChangeFeedStartFromVisitor {
    private static final long START_FROM_BEGINNING_EPOCH_SECONDS = -62135596800L;
    private static final Instant START_FROM_BEGINNING_TIME =
        Instant.ofEpochSecond(START_FROM_BEGINNING_EPOCH_SECONDS);

    private final RxDocumentServiceRequest request;

    public PopulateStartFromRequestOptionVisitorImpl(final RxDocumentServiceRequest request) {
        if (request == null) {
            throw new NullPointerException("request");
        }

        this.request = request;
    }

    @Override
    public void Visit(ChangeFeedStartFromNowImpl startFromNow) {
        this.request.getHeaders().put(
            HttpConstants.HttpHeaders.IF_NONE_MATCH,
            HttpConstants.HeaderValues.IF_NONE_MATCH_ALL);
    }

    @Override
    public void Visit(ChangeFeedStartFromPointInTimeImpl startFromTime) {
        // Our current public contract for ChangeFeedProcessor uses DateTime.MinValue.ToUniversalTime as beginning.
        // We need to add a special case here, otherwise it would send it as normal StartTime.
        // The problem is Multi master accounts do not support StartTime header on ReadFeed, and thus,
        // it would break multi master Change Feed Processor users using Start From Beginning semantics.
        // It's also an optimization, since the backend won't have to binary search for the value.
        Instant pointInTime = startFromTime.getPointInTime();
        if (pointInTime != START_FROM_BEGINNING_TIME)
        {
            this.request.getHeaders().put(
                HttpConstants.HttpHeaders.IF_MODIFIED_SINCE,
                DateTimeFormatter.RFC_1123_DATE_TIME.format(pointInTime));
        }
    }

    @Override
    public void Visit(ChangeFeedStartFromContinuationImpl startFromContinuation) {
        // On REST level, change feed is using IfNoneMatch/ETag instead of continuation
        this.request.getHeaders().put(
            HttpConstants.HttpHeaders.IF_NONE_MATCH,
            startFromContinuation.getContinuation());
    }

    @Override
    public void Visit(ChangeFeedStartFromBeginningImpl startFromBeginning) {
        // We don't need to set any headers to start from the beginning
    }
}