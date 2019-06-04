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
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKeyRange;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.SqlQuerySpec;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.RequestChargeTracker;
import com.microsoft.azure.cosmosdb.internal.ResourceType;
import com.microsoft.azure.cosmosdb.internal.query.PartitionedQueryExecutionInfo;
import com.microsoft.azure.cosmosdb.rx.internal.IDocumentClientRetryPolicy;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.Utils;
import com.microsoft.azure.cosmosdb.rx.internal.Utils.ValueHolder;

import rx.Observable;
import rx.Observable.Transformer;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func3;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class ParallelDocumentQueryExecutionContext<T extends Resource>
        extends ParallelDocumentQueryExecutionContextBase<T> {

    private ParallelDocumentQueryExecutionContext(
            IDocumentQueryClient client,
            List<PartitionKeyRange> partitionKeyRanges,
            ResourceType resourceTypeEnum,
            Class<T> resourceType,
            SqlQuerySpec query,
            FeedOptions feedOptions,
            String resourceLink,
            String rewrittenQuery,
            String collectionRid,
            boolean isContinuationExpected,
            boolean getLazyFeedResponse,
            UUID correlatedActivityId) {
        super(client, partitionKeyRanges, resourceTypeEnum, resourceType, query, feedOptions, resourceLink,
                rewrittenQuery, isContinuationExpected, getLazyFeedResponse, correlatedActivityId);
    }

    public static <T extends Resource> Observable<IDocumentQueryExecutionComponent<T>> createAsync(
            IDocumentQueryClient client,
            ResourceType resourceTypeEnum,
            Class<T> resourceType,
            SqlQuerySpec query,
            FeedOptions feedOptions,
            String resourceLink,
            String collectionRid,
            PartitionedQueryExecutionInfo partitionedQueryExecutionInfo,
            List<PartitionKeyRange> targetRanges,
            int initialPageSize,
            boolean isContinuationExpected,
            boolean getLazyFeedResponse,
            UUID correlatedActivityId) {

        ParallelDocumentQueryExecutionContext<T> context = new ParallelDocumentQueryExecutionContext<T>(client,
                targetRanges,
                resourceTypeEnum,
                resourceType,
                query,
                feedOptions,
                resourceLink,
                partitionedQueryExecutionInfo.getQueryInfo().getRewrittenQuery(),
                collectionRid,
                isContinuationExpected,
                getLazyFeedResponse,
                correlatedActivityId);

        try {
            context.initialize(collectionRid,
                    targetRanges,
                    initialPageSize,
                    feedOptions.getRequestContinuation());
            return Observable.just(context);
        } catch (DocumentClientException dce) {
            return Observable.error(dce);
        }
    }

    private void initialize(
            String collectionRid,
            List<PartitionKeyRange> targetRanges,
            int initialPageSize,
            String continuationToken) throws DocumentClientException {
        // Generate the corresponding continuation token map.
        Map<PartitionKeyRange, String> partitionKeyRangeToContinuationTokenMap = new HashMap<PartitionKeyRange, String>();
        if (continuationToken == null) {
            // If the user does not give a continuation token,
            // then just start the query from the first partition.
            for (PartitionKeyRange targetRange : targetRanges) {
                partitionKeyRangeToContinuationTokenMap.put(targetRange,
                        null);
            }
        } else {
            // Figure out which partitions to resume from:

            // If a continuation token is given then we need to figure out partition key
            // range it maps to
            // in order to filter the partition key ranges.
            // For example if suppliedCompositeContinuationToken.Range.Min ==
            // partition3.Range.Min,
            // then we know that partitions 0, 1, 2 are fully drained.

            // Check to see if composite continuation token is a valid JSON.
            ValueHolder<CompositeContinuationToken> outCompositeContinuationToken = new ValueHolder<CompositeContinuationToken>();
            if (!CompositeContinuationToken.tryParse(continuationToken,
                    outCompositeContinuationToken)) {
                String message = String.format("Invalid JSON in continuation token %s for Parallel~Context",
                        continuationToken);
                throw new DocumentClientException(HttpConstants.StatusCodes.BADREQUEST,
                        message);
            }

            CompositeContinuationToken compositeContinuationToken = outCompositeContinuationToken.v;

            // Get the right hand side of the query ranges:
            List<PartitionKeyRange> filteredPartitionKeyRanges = this.getPartitionKeyRangesForContinuation(
                    compositeContinuationToken,
                    targetRanges);

            // The first partition is the one we left off on and have a backend continuation
            // token for.
            partitionKeyRangeToContinuationTokenMap.put(filteredPartitionKeyRanges.get(0),
                    compositeContinuationToken.getToken());

            // The remaining partitions we have yet to touch / have null continuation tokens
            for (int i = 1; i < filteredPartitionKeyRanges.size(); i++) {
                partitionKeyRangeToContinuationTokenMap.put(filteredPartitionKeyRanges.get(i),
                        null);
            }
        }

        super.initialize(collectionRid,
                partitionKeyRangeToContinuationTokenMap,
                initialPageSize,
                this.querySpec);
    }

    private List<PartitionKeyRange> getPartitionKeyRangesForContinuation(
            CompositeContinuationToken compositeContinuationToken,
            List<PartitionKeyRange> partitionKeyRanges) throws DocumentClientException {
        // Find the partition key range we left off on
        int startIndex = this.FindTargetRangeAndExtractContinuationTokens(partitionKeyRanges,
                compositeContinuationToken.getRange());

        List<PartitionKeyRange> rightHandSideRanges = new ArrayList<PartitionKeyRange>();
        for (int i = startIndex; i < partitionKeyRanges.size(); i++) {
            rightHandSideRanges.add(partitionKeyRanges.get(i));
        }

        return rightHandSideRanges;
    }

    private static class EmptyPagesFilterTransformer<T extends Resource>
            implements Transformer<DocumentProducer<T>.DocumentProducerFeedResponse, FeedResponse<T>> {
        private final RequestChargeTracker tracker;
        private DocumentProducer<T>.DocumentProducerFeedResponse previousPage;

        public EmptyPagesFilterTransformer(
                RequestChargeTracker tracker) {

            if (tracker == null) {
                throw new IllegalArgumentException("Request Charge Tracker must not be null.");
            }

            this.tracker = tracker;
            this.previousPage = null;
        }

        private DocumentProducer<T>.DocumentProducerFeedResponse plusCharge(
                DocumentProducer<T>.DocumentProducerFeedResponse documentProducerFeedResponse,
                double charge) {
            FeedResponse<T> page = documentProducerFeedResponse.pageResult;
            Map<String, String> headers = new HashMap<>(page.getResponseHeaders());
            double pageCharge = page.getRequestCharge();
            pageCharge += charge;
            headers.put(HttpConstants.HttpHeaders.REQUEST_CHARGE,
                    String.valueOf(pageCharge));
            FeedResponse<T> newPage = BridgeInternal.createFeedResponseWithQueryMetrics(page.getResults(),
                    headers,
                    page.getQueryMetrics());
            documentProducerFeedResponse.pageResult = newPage;
            return documentProducerFeedResponse;
        }

        private DocumentProducer<T>.DocumentProducerFeedResponse addCompositeContinuationToken(
                DocumentProducer<T>.DocumentProducerFeedResponse documentProducerFeedResponse,
                String compositeContinuationToken) {
            FeedResponse<T> page = documentProducerFeedResponse.pageResult;
            Map<String, String> headers = new HashMap<>(page.getResponseHeaders());
            headers.put(HttpConstants.HttpHeaders.CONTINUATION,
                    compositeContinuationToken);
            FeedResponse<T> newPage = BridgeInternal.createFeedResponseWithQueryMetrics(page.getResults(),
                    headers,
                    page.getQueryMetrics());
            documentProducerFeedResponse.pageResult = newPage;
            return documentProducerFeedResponse;
        }

        private static Map<String, String> headerResponse(
                double requestCharge) {
            return Utils.immutableMapOf(HttpConstants.HttpHeaders.REQUEST_CHARGE,
                    String.valueOf(requestCharge));
        }

        @Override
        public Observable<FeedResponse<T>> call(
                Observable<DocumentProducer<T>.DocumentProducerFeedResponse> source) {
            return source.filter(documentProducerFeedResponse -> {
                if (documentProducerFeedResponse.pageResult.getResults().isEmpty()) {
                    // filter empty pages and accumulate charge
                    tracker.addCharge(documentProducerFeedResponse.pageResult.getRequestCharge());
                    return false;
                }
                return true;
            }).map(documentProducerFeedResponse -> {
                // Add the request charge
                double charge = tracker.getAndResetCharge();
                if (charge > 0) {
                    return plusCharge(documentProducerFeedResponse,
                            charge);
                } else {
                    return documentProducerFeedResponse;
                }
            }).concatWith(Observable.defer(() -> {
                // Emit an empty page so the downstream observables know when there are no more
                // results.
                return Observable.just(null);
            })).map(documentProducerFeedResponse -> {
                // Create pairs from the stream to allow the observables downstream to "peek"
                // 1, 2, 3, null -> (null, 1), (1, 2), (2, 3), (3, null)
                ImmutablePair<DocumentProducer<T>.DocumentProducerFeedResponse, DocumentProducer<T>.DocumentProducerFeedResponse> previousCurrent = new ImmutablePair<DocumentProducer<T>.DocumentProducerFeedResponse, DocumentProducer<T>.DocumentProducerFeedResponse>(
                        this.previousPage,
                        documentProducerFeedResponse);
                this.previousPage = documentProducerFeedResponse;
                return previousCurrent;
            }).skip(1).map(currentNext -> {
                // remove the (null, 1)
                // Add the continuation token based on the current and next page.
                DocumentProducer<T>.DocumentProducerFeedResponse current = currentNext.left;
                DocumentProducer<T>.DocumentProducerFeedResponse next = currentNext.right;

                String compositeContinuationToken;
                String backendContinuationToken = current.pageResult.getResponseContinuation();
                if (backendContinuationToken == null) {
                    // We just finished reading the last document from a partition
                    if (next == null) {
                        // It was the last partition and we are done
                        compositeContinuationToken = null;
                    } else {
                        // It wasn't the last partition, so we need to give the next range, but with a
                        // null continuation
                        CompositeContinuationToken compositeContinuationTokenDom = new CompositeContinuationToken(null,
                                next.sourcePartitionKeyRange.toRange());
                        compositeContinuationToken = compositeContinuationTokenDom.toJson();
                    }
                } else {
                    // We are in the middle of reading a partition,
                    // so give back this partition with a backend continuation token
                    CompositeContinuationToken compositeContinuationTokenDom = new CompositeContinuationToken(
                            backendContinuationToken,
                            current.sourcePartitionKeyRange.toRange());
                    compositeContinuationToken = compositeContinuationTokenDom.toJson();
                }

                DocumentProducer<T>.DocumentProducerFeedResponse page;
                page = current;
                page = this.addCompositeContinuationToken(page,
                        compositeContinuationToken);

                return page;
            }).map(documentProducerFeedResponse -> {
                // Unwrap the documentProducerFeedResponse and get back the feedResponse
                return documentProducerFeedResponse.pageResult;
            }).switchIfEmpty(Observable.defer(() -> {
                // create an empty page if there is no result
                return Observable.just(BridgeInternal.createFeedResponse(Utils.immutableListOf(),
                        headerResponse(tracker.getAndResetCharge())));
            }));
        }
    }

    @Override
    public Observable<FeedResponse<T>> drainAsync(
            int maxPageSize) {
        List<Observable<DocumentProducer<T>.DocumentProducerFeedResponse>> obs = this.documentProducers
                // Get the stream.
                .stream()
                // Start from the left most partition first.
                .sorted((
                        dp1,
                        dp2) -> dp1.targetRange.getMinInclusive().compareTo(dp2.targetRange.getMinInclusive()))
                // For each partition get it's stream of results.
                .map(dp -> dp.produceAsync())
                // Merge results from all partitions.
                .collect(Collectors.toList());
        return Observable.concat(obs).compose(new EmptyPagesFilterTransformer<>(new RequestChargeTracker()));
    }

    @Override
    public Observable<FeedResponse<T>> executeAsync() {
        return this.drainAsync(feedOptions.getMaxItemCount());
    }

    protected DocumentProducer<T> createDocumentProducer(
            String collectionRid,
            PartitionKeyRange targetRange,
            String initialContinuationToken,
            int initialPageSize,
            FeedOptions feedOptions,
            SqlQuerySpec querySpecForInit,
            Map<String, String> commonRequestHeaders,
            Func3<PartitionKeyRange, String, Integer, RxDocumentServiceRequest> createRequestFunc,
            Func1<RxDocumentServiceRequest, Observable<FeedResponse<T>>> executeFunc,
            Func0<IDocumentClientRetryPolicy> createRetryPolicyFunc) {
        return new DocumentProducer<T>(client,
                collectionRid,
                feedOptions,
                createRequestFunc,
                executeFunc,
                targetRange,
                collectionRid,
                () -> client.getResetSessionTokenRetryPolicy().getRequestPolicy(),
                resourceType,
                correlatedActivityId,
                initialPageSize,
                initialContinuationToken,
                top);
    }
}
