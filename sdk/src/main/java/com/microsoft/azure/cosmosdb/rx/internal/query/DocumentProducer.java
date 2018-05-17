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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.microsoft.azure.cosmosdb.rx.internal.IDocumentClientRetryPolicy;
import com.microsoft.azure.cosmosdb.rx.internal.IRetryPolicyFactory;
import com.microsoft.azure.cosmosdb.rx.internal.ObservableHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKeyRange;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.internal.routing.Range;
import com.microsoft.azure.cosmosdb.rx.internal.Exceptions;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.Utils;

import rx.Observable;
import rx.Single;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func3;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
class DocumentProducer<T extends Resource> {
    private static final Logger logger = LoggerFactory.getLogger(DocumentProducer.class);

    class DocumentProducerFeedResponse {
        FeedResponse<T> pageResult;
        PartitionKeyRange sourcePartitionKeyRange;

        DocumentProducerFeedResponse(FeedResponse<T> pageResult) {
            this.pageResult = pageResult;
            this.sourcePartitionKeyRange = DocumentProducer.this.targetRange;
        }

        DocumentProducerFeedResponse(FeedResponse<T> pageResult, PartitionKeyRange pkr) {
            this.pageResult = pageResult;
            this.sourcePartitionKeyRange = pkr;
        }
    }

    protected final IDocumentQueryClient client;
    protected final String collectionRid;
    protected final FeedOptions feedOptions;
    protected final Class<T> resourceType;
    protected final PartitionKeyRange targetRange;
    protected final String collectionLink;
    protected final Func3<PartitionKeyRange, String, Integer, RxDocumentServiceRequest> createRequestFunc;
    protected final Func1<RxDocumentServiceRequest, Observable<FeedResponse<T>>> executeRequestFuncWithRetries;
    protected final Func0<IDocumentClientRetryPolicy> createRetryPolicyFunc;
    protected final int pageSize;
    protected final UUID correlatedActivityId;
    public int top;
    private volatile String lastResponseContinuationToken;

    public DocumentProducer(
            IDocumentQueryClient client,
            String collectionResourceId,
            Func3<PartitionKeyRange, String, Integer, RxDocumentServiceRequest> createRequestFunc,
            Func1<RxDocumentServiceRequest, Observable<FeedResponse<T>>> executeRequestFunc,
            PartitionKeyRange targetRange,
            String collectionLink,
            Func0<IDocumentClientRetryPolicy> createRetryPolicyFunc,
            Class<T> resourceType ,
            UUID correlatedActivityId,
            int initialPageSize, // = -1,
            String initialContinuationToken,
            int top) {

        this.client = client;
        this.collectionRid = collectionResourceId;

        this.createRequestFunc = createRequestFunc;

        this.executeRequestFuncWithRetries = request -> {
            IDocumentClientRetryPolicy retryPolicy = null;
            if (createRetryPolicyFunc != null) {
                retryPolicy = createRetryPolicyFunc.call();
                retryPolicy.onBeforeSendRequest(request);
            }
            return ObservableHelper.inlineIfPossibleAsObs(
                    () -> executeRequestFunc.call(request), retryPolicy);
        };

        this.correlatedActivityId = correlatedActivityId;

        this.feedOptions = new FeedOptions();
        this.feedOptions.setRequestContinuation(initialContinuationToken);
        this.lastResponseContinuationToken = initialContinuationToken;
        this.resourceType = resourceType;
        this.targetRange = targetRange;
        this.collectionLink = collectionLink;
        this.createRetryPolicyFunc = createRetryPolicyFunc;
        this.pageSize = initialPageSize;
        this.top = top;
    }

    public Observable<DocumentProducerFeedResponse> produceAsync() {
        Func2<String, Integer, RxDocumentServiceRequest> sourcePartitionCreateRequestFunc = 
                (token, maxItemCount) -> createRequestFunc.call(targetRange, token, maxItemCount);

        Observable<FeedResponse<T>> obs = Paginator.getPaginatedQueryResultAsObservable(feedOptions, sourcePartitionCreateRequestFunc,
                executeRequestFuncWithRetries, resourceType, top, pageSize).map(rsp -> {
                    lastResponseContinuationToken = rsp.getResponseContinuation();
                    return rsp;
                });

        return splitProof(obs.map(page -> new DocumentProducerFeedResponse(page)));
    }

    private Observable<DocumentProducerFeedResponse> splitProof(Observable<DocumentProducerFeedResponse> sourceFeedResponseObservable) {
        return sourceFeedResponseObservable.onErrorResumeNext( t -> {
            DocumentClientException dce = Utils.as(t, DocumentClientException.class);
            if (dce == null || !isSplit(dce)) {
                return Observable.error(t);
            }

            // we are dealing with Split
            Single<List<PartitionKeyRange>> replacementRangesObs = getReplacementRanges(targetRange.toRange());

            // Since new DocumentProducers are instantiated for the new replacement ranges, if for the new
            // replacement partitions split happens the corresponding DocumentProducer can recursively handle splits.
            // so this is resilient to split on splits.
            Observable<DocumentProducer<T>> replacementProducers = replacementRangesObs.toObservable().flatMap(
                    partitionKeyRanges ->  {
                        if (logger.isDebugEnabled()) {
                            logger.info("Cross Partition Query Execution detected partition [{}] split into [{} partitions,"
                                    + " last continuation token is [{}].",
                                    targetRange.toJson(),
                                    String.join(", ", partitionKeyRanges.stream()
                                            .map(pkr -> pkr.toJson()).collect(Collectors.toList())),
                                    lastResponseContinuationToken);
                        }
                        return Observable.from(createReplacingDocumentProducersOnSplit(partitionKeyRanges));
                    });

            return produceOnSplit(replacementProducers);
        });
    }

    protected Observable<DocumentProducerFeedResponse> produceOnSplit(Observable<DocumentProducer<T>> replacingDocumentProducers) {
        return replacingDocumentProducers.flatMap(dp -> dp.produceAsync(), 1);
    }

    private List<DocumentProducer<T>> createReplacingDocumentProducersOnSplit(List<PartitionKeyRange> partitionKeyRanges) {

        List<DocumentProducer<T>> replacingDocumentProducers = new ArrayList<>(partitionKeyRanges.size());
        for(PartitionKeyRange pkr: partitionKeyRanges) {
            replacingDocumentProducers.add(createChildDocumentProducerOnSplit(pkr, lastResponseContinuationToken));
        }
        return replacingDocumentProducers;
    }
    
    protected DocumentProducer<T> createChildDocumentProducerOnSplit(
            PartitionKeyRange targetRange,
            String initialContinuationToken) {

        return new DocumentProducer<T>(
                client,
                collectionRid,
                createRequestFunc,
                executeRequestFuncWithRetries,
                targetRange,
                collectionLink,
                null,
                resourceType ,
                correlatedActivityId,
                pageSize,
                initialContinuationToken,
                top);
    }

    private Single<List<PartitionKeyRange>> getReplacementRanges(Range<String> range) {
        return client.getPartitionKeyRangeCache().tryGetOverlappingRangesAsync(collectionRid, range, true);
    }

    private boolean isSplit(DocumentClientException e) {
        return Exceptions.isPartitionSplit(e);
    }
}
