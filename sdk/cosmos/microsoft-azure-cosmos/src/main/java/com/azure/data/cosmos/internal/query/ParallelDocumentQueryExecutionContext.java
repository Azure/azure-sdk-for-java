// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.query;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.IDocumentClientRetryPolicy;
import com.azure.data.cosmos.internal.PartitionKeyRange;
import com.azure.data.cosmos.internal.RequestChargeTracker;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.Utils;
import com.azure.data.cosmos.internal.Utils.ValueHolder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class ParallelDocumentQueryExecutionContext<T extends Resource>
        extends ParallelDocumentQueryExecutionContextBase<T> {
    private FeedOptions feedOptions;

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
        this.feedOptions = feedOptions;
    }

    public static <T extends Resource> Flux<IDocumentQueryExecutionComponent<T>> createAsync(
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
                    feedOptions.requestContinuation());
            return Flux.just(context);
        } catch (CosmosClientException dce) {
            return Flux.error(dce);
        }
    }

    private void initialize(
            String collectionRid,
            List<PartitionKeyRange> targetRanges,
            int initialPageSize,
            String continuationToken) throws CosmosClientException {
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
            // For example if suppliedCompositeContinuationToken.RANGE.Min ==
            // partition3.RANGE.Min,
            // then we know that partitions 0, 1, 2 are fully drained.

            // Check to see if composite continuation token is a valid JSON.
            ValueHolder<CompositeContinuationToken> outCompositeContinuationToken = new ValueHolder<CompositeContinuationToken>();
            if (!CompositeContinuationToken.tryParse(continuationToken,
                    outCompositeContinuationToken)) {
                String message = String.format("INVALID JSON in continuation token %s for Parallel~Context",
                        continuationToken);
                throw BridgeInternal.createCosmosClientException(HttpConstants.StatusCodes.BADREQUEST,
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
            List<PartitionKeyRange> partitionKeyRanges) throws CosmosClientException {
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
            implements Function<Flux<DocumentProducer<T>.DocumentProducerFeedResponse>, Flux<FeedResponse<T>>> {
        private final RequestChargeTracker tracker;
        private DocumentProducer<T>.DocumentProducerFeedResponse previousPage;
        private final FeedOptions feedOptions;

        public EmptyPagesFilterTransformer(
                RequestChargeTracker tracker, FeedOptions options) {

            if (tracker == null) {
                throw new IllegalArgumentException("Request Charge Tracker must not be null.");
            }

            this.tracker = tracker;
            this.previousPage = null;
            this.feedOptions = options;
        }

        private DocumentProducer<T>.DocumentProducerFeedResponse plusCharge(
                DocumentProducer<T>.DocumentProducerFeedResponse documentProducerFeedResponse,
                double charge) {
            FeedResponse<T> page = documentProducerFeedResponse.pageResult;
            Map<String, String> headers = new HashMap<>(page.responseHeaders());
            double pageCharge = page.requestCharge();
            pageCharge += charge;
            headers.put(HttpConstants.HttpHeaders.REQUEST_CHARGE,
                    String.valueOf(pageCharge));
            FeedResponse<T> newPage = BridgeInternal.createFeedResponseWithQueryMetrics(page.results(),
                    headers,
                BridgeInternal.queryMetricsFromFeedResponse(page));
            documentProducerFeedResponse.pageResult = newPage;
            return documentProducerFeedResponse;
        }

        private DocumentProducer<T>.DocumentProducerFeedResponse addCompositeContinuationToken(
                DocumentProducer<T>.DocumentProducerFeedResponse documentProducerFeedResponse,
                String compositeContinuationToken) {
            FeedResponse<T> page = documentProducerFeedResponse.pageResult;
            Map<String, String> headers = new HashMap<>(page.responseHeaders());
            headers.put(HttpConstants.HttpHeaders.CONTINUATION,
                    compositeContinuationToken);
            FeedResponse<T> newPage = BridgeInternal.createFeedResponseWithQueryMetrics(page.results(),
                    headers,
                BridgeInternal.queryMetricsFromFeedResponse(page));
            documentProducerFeedResponse.pageResult = newPage;
            return documentProducerFeedResponse;
        }

        private static Map<String, String> headerResponse(
                double requestCharge) {
            return Utils.immutableMapOf(HttpConstants.HttpHeaders.REQUEST_CHARGE,
                    String.valueOf(requestCharge));
        }

        @Override
        public Flux<FeedResponse<T>> apply(Flux<DocumentProducer<T>.DocumentProducerFeedResponse> source) {
            // Emit an empty page so the downstream observables know when there are no more
            // results.
            return source.filter(documentProducerFeedResponse -> {
                if (documentProducerFeedResponse.pageResult.results().isEmpty()
                        && !this.feedOptions.allowEmptyPages()) {
                    // filter empty pages and accumulate charge
                    tracker.addCharge(documentProducerFeedResponse.pageResult.requestCharge());
                    return false;
                }
                return true;
            }).map(documentProducerFeedResponse -> {
                // Add the request charge
                double charge = tracker.getAndResetCharge();
                if (charge > 0) {
                    return new ValueHolder<>(plusCharge(documentProducerFeedResponse,
                            charge));
                } else {
                    return new ValueHolder<>(documentProducerFeedResponse);
                }
            }).concatWith(Flux.just(new ValueHolder<>(null))).map(heldValue -> {
                DocumentProducer<T>.DocumentProducerFeedResponse documentProducerFeedResponse = heldValue.v;
                // CREATE pairs from the stream to allow the observables downstream to "peek"
                // 1, 2, 3, null -> (null, 1), (1, 2), (2, 3), (3, null)
                    ImmutablePair<DocumentProducer<T>.DocumentProducerFeedResponse, DocumentProducer<T>.DocumentProducerFeedResponse> previousCurrent = new ImmutablePair<>(
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
                String backendContinuationToken = current.pageResult.continuationToken();
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
            }).switchIfEmpty(Flux.defer(() -> {
                // create an empty page if there is no result
                return Flux.just(BridgeInternal.createFeedResponse(Utils.immutableListOf(),
                        headerResponse(tracker.getAndResetCharge())));
            }));
        }
    }

    @Override
    public Flux<FeedResponse<T>> drainAsync(
            int maxPageSize) {
        List<Flux<DocumentProducer<T>.DocumentProducerFeedResponse>> obs = this.documentProducers
                // Get the stream.
                .stream()
                // Start from the left most partition first.
                .sorted(Comparator.comparing(dp -> dp.targetRange.getMinInclusive()))
                // For each partition get it's stream of results.
                .map(DocumentProducer::produceAsync)
                // Merge results from all partitions.
                .collect(Collectors.toList());
        return Flux.concat(obs).compose(new EmptyPagesFilterTransformer<>(new RequestChargeTracker(), this.feedOptions));
    }

    @Override
    public Flux<FeedResponse<T>> executeAsync() {
        return this.drainAsync(feedOptions.maxItemCount());
    }

    protected DocumentProducer<T> createDocumentProducer(
            String collectionRid,
            PartitionKeyRange targetRange,
            String initialContinuationToken,
            int initialPageSize,
            FeedOptions feedOptions,
            SqlQuerySpec querySpecForInit,
            Map<String, String> commonRequestHeaders,
            TriFunction<PartitionKeyRange, String, Integer, RxDocumentServiceRequest> createRequestFunc,
            Function<RxDocumentServiceRequest, Flux<FeedResponse<T>>> executeFunc,
            Callable<IDocumentClientRetryPolicy> createRetryPolicyFunc) {
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
