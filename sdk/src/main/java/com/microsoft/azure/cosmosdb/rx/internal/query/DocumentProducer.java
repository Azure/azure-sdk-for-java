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

import java.util.UUID;

import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKeyRange;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.SqlQuerySpec;
import com.microsoft.azure.cosmosdb.rx.internal.IDocumentClientRetryPolicy;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class DocumentProducer<T extends Resource> {

    private FeedOptions feedOptions;
    private Class<T> resourceType;
    private PartitionKeyRange targetRange;
    private SqlQuerySpec query;
    private String collectionLink;
    private Func2<String, Integer, RxDocumentServiceRequest> createRequestFunc;
    private Func1<RxDocumentServiceRequest, Observable<FeedResponse<T>>> executeRequestFunc;
    private int pageSize;
    private UUID correlatedActivityId;
    public int top;
    public DocumentProducer(
            Func2<String, Integer, RxDocumentServiceRequest> createRequestFunc,
            Func1<RxDocumentServiceRequest, Observable<FeedResponse<T>>> executeRequestFunc,
            PartitionKeyRange targetRange,
            SqlQuerySpec query,
            String collectionLink,
            Func0<IDocumentClientRetryPolicy> createRetryPolicyFunc,
            Class<T> resourceType ,
            UUID correlatedActivityId,
            int initialPageSize, // = -1,
            String initialContinuationToken,
            int top) {

        this.createRequestFunc = createRequestFunc;
        this.executeRequestFunc = executeRequestFunc;
        this.correlatedActivityId = correlatedActivityId;

        this.feedOptions = new FeedOptions();
        this.resourceType = resourceType;
        this.targetRange = targetRange;
        this.collectionLink = collectionLink;
        this.query = query;
        this.pageSize = initialPageSize;
        this.top = top;
    }

    public PartitionKeyRange getTargetPartitionKeyRange() {
        return targetRange;
    }
    
    public Observable<FeedResponse<T>> produceAsync() {

        // TODO: 
        // 1) find the rx pattern to make this code re-usable for orderby, split, etc
        // 2) find the rx pattern for controlling degree of parallelism and other parallel query options
        // 3) support rx backpressure?
        // 4) add retry support

        // TODO: add retry here

        return Paginator.getPaginatedQueryResultAsObservable(feedOptions, createRequestFunc,
                executeRequestFunc, resourceType, top, pageSize); 
    }
}
