/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.rx.internal.query;

import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceResponse;

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
