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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKeyRange;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.SqlQuerySpec;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.ResourceType;
import com.microsoft.azure.cosmosdb.internal.routing.Range;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.Strings;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public abstract class ParallelDocumentQueryExecutionContextBase<T extends Resource> extends DocumentQueryExecutionContextBase<T> implements IDocumentQueryExecutionComponent<T> {

    protected List<DocumentProducer<T>> documentProducers;
    protected SqlQuerySpec querySpec;
    protected int pageSize;
    private int top = -1;

    protected ParallelDocumentQueryExecutionContextBase(
            IDocumentQueryClient client,
            ResourceType resourceTypeEnum, 
            Class<T> resourceType, 
            SqlQuerySpec query, 
            FeedOptions feedOptions,
            String resourceLink, 
            String rewrittenQuery,
            boolean isContinuationExpected,
            boolean getLazyFeedResponse, 
            UUID correlatedActivityId) {
        super(client, resourceTypeEnum, resourceType, query, feedOptions, resourceLink, getLazyFeedResponse,
                correlatedActivityId);

        documentProducers = new ArrayList<>();

        if (!Strings.isNullOrEmpty(rewrittenQuery)) {
            this.querySpec = new SqlQuerySpec(rewrittenQuery, super.query.getParameters());
        } else {
            this.querySpec = super.query;
        }
    }

    protected void initialize(
            String collectionRid,
            List<Range<String>> queryRanges,
            List<PartitionKeyRange> partitionKeyRanges,
            int initialPageSize,
            SqlQuerySpec querySpecForInit){ 
        this.pageSize = initialPageSize; 
        Map<String, String> commonRequestHeaders = createCommonHeadersAsync(this.getFeedOptions(null, null));

        for(PartitionKeyRange targetRange: partitionKeyRanges) {

            Func2<String, Integer, RxDocumentServiceRequest>  createRequestFunc = (continuationToken, pageSize) -> {
                Map<String, String> headers = new HashMap<>(commonRequestHeaders);
                headers.put(HttpConstants.HttpHeaders.CONTINUATION, continuationToken);
                headers.put(HttpConstants.HttpHeaders.PAGE_SIZE, Strings.toString(pageSize));
                return this.createDocumentServiceRequest(
                        headers,
                        querySpecForInit,
                        targetRange,
                        collectionRid);
            };

            Func1<RxDocumentServiceRequest, Observable<FeedResponse<T>>> executeFunc =
                    (request) -> {
                        return this.executeRequestAsync(request).toObservable();
                    };
                    DocumentProducer<T> dp = new DocumentProducer<T>(
                            createRequestFunc,
                            executeFunc,
                            targetRange, 
                            querySpecForInit,
                            collectionRid, 
                            null, resourceType, correlatedActivityId, initialPageSize, 
                            null, top);

                    documentProducers.add(dp);
        }
    }

    @Override
    abstract public Observable<FeedResponse<T>> drainAsync(int maxPageSize);

    public void setTop(int newTop) {
        this.top = newTop;

        for (DocumentProducer<T> producer : this.documentProducers) {
            producer.top = newTop;
        }
    }
}
