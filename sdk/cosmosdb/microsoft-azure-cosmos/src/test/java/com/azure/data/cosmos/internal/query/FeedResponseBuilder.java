// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.query;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.RxDocumentServiceResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FeedResponseBuilder<T extends Resource> {
    private final boolean isChangeFeed;
    private final Class<T> klass;

    private Map<String, String> headers = new HashMap<>();
    private boolean noMoreChangesInChangeFeed = false;
    private List<T> results;

    private FeedResponseBuilder(Class<T> klass, boolean isChangeFeed) {
        this.klass = klass;
        this.isChangeFeed = isChangeFeed;
    }

    public FeedResponseBuilder<T> withContinuationToken(String continuationToken) {

        if (isChangeFeed) {
            headers.put(HttpConstants.HttpHeaders.E_TAG, continuationToken);
        } else {
            headers.put(HttpConstants.HttpHeaders.CONTINUATION, continuationToken);
        }
        return this;
    }

    public FeedResponseBuilder<T> withResults(List<T> results) {
        this.results = results;
        return this;
    }

    public FeedResponseBuilder<T> withResults(T... results) {
        this.results = Arrays.asList(results);
        return this;
    }

    public FeedResponseBuilder<T> lastChangeFeedPage() {
        this.noMoreChangesInChangeFeed = true;
        return this;
    }

    public FeedResponse<T> build() {
        RxDocumentServiceResponse rsp = mock(RxDocumentServiceResponse.class);
        when(rsp.getResponseHeaders()).thenReturn(headers);
        when(rsp.getQueryResponse(klass)).thenReturn(results);
        if (isChangeFeed) {
            when(rsp.getStatusCode()).thenReturn(noMoreChangesInChangeFeed?
                    HttpConstants.StatusCodes.NOT_MODIFIED : 200);
            return BridgeInternal.toChaneFeedResponsePage(rsp, klass);
        } else {
            return BridgeInternal.toFeedResponsePage(rsp, klass);
        }
    }

    public static <T extends Resource> FeedResponseBuilder<T> queryFeedResponseBuilder(Class<T> klass) {
        return new FeedResponseBuilder<T>(klass, false);
    }

    public static <T extends Resource> FeedResponseBuilder<T> changeFeedResponseBuilder(Class<T> klass) {
        return new FeedResponseBuilder<T>(klass, true);
    }
}
