// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public abstract class FeedRangeContinuation extends JsonSerializable {
    private final static Logger LOGGER = LoggerFactory.getLogger(FeedRangeContinuation.class);

    protected final  FeedRangeInternal feedRange;
    private final String containerRid;

    // for mocking
    protected FeedRangeContinuation() {
        this.feedRange = null;
        this.containerRid = null;
    }

    public FeedRangeContinuation(String containerRid, FeedRangeInternal feedRange) {
        checkNotNull(feedRange, "expected non-null feedRange");
        this.feedRange = feedRange;
        this.containerRid = containerRid;
    }

    public String getContainerRid() {
        return this.containerRid;
    }

    public FeedRangeInternal getFeedRange() {
        return this.feedRange;
    }

    public abstract String getContinuation();

    public abstract void replaceContinuation(String continuationToken);

    public abstract boolean isDone();

    public abstract void validateContainer(String containerRid);

    public void populatePropertyBag() {
        super.populatePropertyBag();
    }

    public static FeedRangeContinuation tryParse(String jsonString) {
        if (jsonString == null) {
            return null;
        }

        try
        {
            return FeedRangeCompositeContinuationImpl.parse(jsonString);
        }
        catch (final IOException ioError) {
            LOGGER.debug(
                "Failed to parse feed range continuation JSON {}",
                jsonString,
                ioError);
            return null;
        }
    }

    public static FeedRangeContinuation convert(final String continuationToken) {
        if (Strings.isNullOrWhiteSpace(continuationToken)) {
            throw new NullPointerException("continuationToken");
        }

        FeedRangeContinuation returnValue = tryParse(continuationToken);
        if (returnValue != null) {
            return returnValue;
        }

        throw new IllegalArgumentException(
            String.format(
                "Invalid Feed range continuation token '%s'",
                continuationToken));
    }

    public abstract ShouldRetryResult handleChangeFeedNotModified(
        RxDocumentServiceResponse responseMessage);

    public abstract Mono<ShouldRetryResult> handleSplit(
        RxDocumentClientImpl client,
        RxDocumentServiceResponse responseMessage);

    public abstract void accept(FeedRangeContinuationVisitor visitor);
}
