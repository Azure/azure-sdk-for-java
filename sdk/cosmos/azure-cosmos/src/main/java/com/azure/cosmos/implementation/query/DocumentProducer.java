// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.DocumentClientRetryPolicy;
import com.azure.cosmos.implementation.Exceptions;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.ObservableHelper;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.QueryMetricsConstants;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.query.metrics.ClientSideMetrics;
import com.azure.cosmos.implementation.query.metrics.FetchExecutionRangeAccumulator;
import com.azure.cosmos.implementation.query.metrics.SchedulingStopwatch;
import com.azure.cosmos.implementation.query.metrics.SchedulingTimeSpan;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
class DocumentProducer<T> {

    private static final ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.CosmosQueryRequestOptionsAccessor qryOptionsAccessor =
        ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.getCosmosQueryRequestOptionsAccessor();

    private static final Logger logger = LoggerFactory.getLogger(DocumentProducer.class);
    private int retries;

    class DocumentProducerFeedResponse {
        FeedResponse<T> pageResult;
        FeedRangeEpkImpl sourceFeedRange;

        DocumentProducerFeedResponse(FeedResponse<T> pageResult) {
            this.pageResult = pageResult;
            this.sourceFeedRange = DocumentProducer.this.feedRange;
            populatePartitionedQueryMetrics();
        }

        DocumentProducerFeedResponse(FeedResponse<T> pageResult, FeedRange feedRange) {
            this.pageResult = pageResult;
            this.sourceFeedRange = (FeedRangeEpkImpl) feedRange;
            populatePartitionedQueryMetrics();
        }

        void populatePartitionedQueryMetrics() {
            String queryMetricsDelimitedString = pageResult.getResponseHeaders().get(HttpConstants.HttpHeaders.QUERY_METRICS);
            if (!StringUtils.isEmpty(queryMetricsDelimitedString)) {
                queryMetricsDelimitedString += String.format(Locale.ROOT,
                                                             ";%s=%.2f",
                                                             QueryMetricsConstants.RequestCharge,
                                                             pageResult.getRequestCharge());
                ImmutablePair<String, SchedulingTimeSpan> schedulingTimeSpanMap =
                        new ImmutablePair<>(feedRange.getRange().toString(), fetchSchedulingMetrics.getElapsedTime());

                QueryMetrics qm =BridgeInternal.createQueryMetricsFromDelimitedStringAndClientSideMetrics(queryMetricsDelimitedString,
                        new ClientSideMetrics(retries,
                                pageResult.getRequestCharge(),
                                fetchExecutionRangeAccumulator.getExecutionRanges(),
                            Collections.singletonList(schedulingTimeSpanMap)
                        ), pageResult.getActivityId(),
                    pageResult.getResponseHeaders().getOrDefault(HttpConstants.HttpHeaders.INDEX_UTILIZATION, null));
                String pkrId = pageResult.getResponseHeaders().get(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID);
                String queryMetricKey = feedRange.getRange().toString() + ",pkrId:" + pkrId;
                BridgeInternal.putQueryMetricsIntoMap(pageResult, queryMetricKey, qm);
            }
        }
    }

    protected final IDocumentQueryClient client;
    protected final Supplier<String> operationContextTextProvider;
    protected final String collectionRid;
    protected final CosmosQueryRequestOptions cosmosQueryRequestOptions;
    protected final Class<T> resourceType;
    protected final String collectionLink;
    protected final TriFunction<FeedRangeEpkImpl, String, Integer, RxDocumentServiceRequest> createRequestFunc;
    protected final Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeRequestFuncWithRetries;
    protected final Supplier<DocumentClientRetryPolicy> createRetryPolicyFunc;
    protected final int pageSize;
    protected final UUID correlatedActivityId;
    public int top;
    private volatile String lastResponseContinuationToken;
    private final SchedulingStopwatch fetchSchedulingMetrics;
    private final FetchExecutionRangeAccumulator fetchExecutionRangeAccumulator;
    protected FeedRangeEpkImpl feedRange;

    public DocumentProducer(
            IDocumentQueryClient client,
            String collectionResourceId,
            CosmosQueryRequestOptions cosmosQueryRequestOptions,
            TriFunction<FeedRangeEpkImpl, String, Integer, RxDocumentServiceRequest> createRequestFunc,
            Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeRequestFunc,
            String collectionLink,
            Supplier<DocumentClientRetryPolicy> createRetryPolicyFunc,
            Class<T> resourceType ,
            UUID correlatedActivityId,
            int initialPageSize, // = -1,
            String initialContinuationToken,
            int top,
            FeedRangeEpkImpl feedRange,
            Supplier<String> operationContextTextProvider) {

        this.client = client;
        this.collectionRid = collectionResourceId;

        this.createRequestFunc = createRequestFunc;

        this.fetchSchedulingMetrics = new SchedulingStopwatch();
        this.fetchSchedulingMetrics.ready();
        this.fetchExecutionRangeAccumulator = new FetchExecutionRangeAccumulator(feedRange.getRange().toString());
        this.operationContextTextProvider = operationContextTextProvider;

        BiFunction<Supplier<DocumentClientRetryPolicy>, RxDocumentServiceRequest, Mono<FeedResponse<T>>>
            executeFeedOperationCore = (clientRetryPolicyFactory, request) -> {
            DocumentClientRetryPolicy finalRetryPolicy = clientRetryPolicyFactory.get();
            return ObservableHelper.inlineIfPossibleAsObs(
                () -> Mono
                    .just(request)
                    .flatMap(req -> {

                        if(finalRetryPolicy != null) {
                            finalRetryPolicy.onBeforeSendRequest(req);
                        }

                        return client.populateFeedRangeHeader(req);
                    })
                    .flatMap(req -> client.addPartitionLevelUnavailableRegionsOnRequest(req, cosmosQueryRequestOptions, finalRetryPolicy))
                    .flatMap(req -> {
                        ++retries;
                        return executeRequestFunc.apply(req);
                    }), finalRetryPolicy);
        };

        this.correlatedActivityId = correlatedActivityId;

        this.cosmosQueryRequestOptions = cosmosQueryRequestOptions != null
            ? qryOptionsAccessor.clone(cosmosQueryRequestOptions)
            : new CosmosQueryRequestOptions();
        ModelBridgeInternal.setQueryRequestOptionsContinuationToken(this.cosmosQueryRequestOptions, initialContinuationToken);

        this.executeRequestFuncWithRetries = request -> {
            retries = -1;
            this.fetchSchedulingMetrics.start();
            this.fetchExecutionRangeAccumulator.beginFetchRange();

            return this.client.executeFeedOperationWithAvailabilityStrategy(
                ResourceType.Document,
                OperationType.Query,
                () -> {
                    if (createRetryPolicyFunc != null) {
                        return createRetryPolicyFunc.get();
                    }

                    return null;
                },
                request,
                executeFeedOperationCore,
                collectionLink);
        };

        this.lastResponseContinuationToken = initialContinuationToken;
        this.resourceType = resourceType;
        this.collectionLink = collectionLink;
        this.createRetryPolicyFunc = createRetryPolicyFunc;
        this.pageSize = initialPageSize;
        this.top = top;
        this.feedRange = feedRange;
    }

    public Flux<DocumentProducerFeedResponse> produceAsync() {
        BiFunction<String, Integer, RxDocumentServiceRequest> sourcePartitionCreateRequestFunc =
                (token, maxItemCount) -> createRequestFunc.apply(feedRange, token, maxItemCount);
        Flux<FeedResponse<T>> obs = Paginator
                .getPaginatedQueryResultAsObservable(
                        this.lastResponseContinuationToken,
                        sourcePartitionCreateRequestFunc,
                        executeRequestFuncWithRetries,
                        top,
                        pageSize,
                        Paginator.getPreFetchCount(cosmosQueryRequestOptions, top, pageSize),
                        qryOptionsAccessor.getImpl(cosmosQueryRequestOptions).getOperationContextAndListenerTuple(),
                        qryOptionsAccessor.getCancelledRequestDiagnosticsTracker(cosmosQueryRequestOptions),
                    client.getGlobalEndpointManager(),
                    client.getGlobalPartitionEndpointManagerForCircuitBreaker()
                )
                .map(rsp -> {
                    this.lastResponseContinuationToken = rsp.getContinuationToken();
                    this.fetchExecutionRangeAccumulator.endFetchRange(rsp.getActivityId(),
                            rsp.getResults().size(),
                            this.retries);
                    this.fetchSchedulingMetrics.stop();
                    return rsp;});

        return feedRangeGoneProof(obs.map(DocumentProducerFeedResponse::new));
    }

    /***
     * Split or merge proof method.
     *
     * @param sourceFeedResponseObservable the original response flux.
     * @return the new response flux with split or merge handling.
     */
    private Flux<DocumentProducerFeedResponse> feedRangeGoneProof(Flux<DocumentProducerFeedResponse> sourceFeedResponseObservable) {
        return sourceFeedResponseObservable.onErrorResume( t -> {
            CosmosException dce = Utils.as(t, CosmosException.class);
            if (dce == null || !isSplitOrMerge(dce)) {
                logger.error(
                    "Unexpected failure, Context: {}",
                    this.operationContextTextProvider.get(),
                    t);
                return Flux.error(t);
            }

            // we are dealing with Split
            logger.debug(
                "DocumentProducer handling a partition gone in [{}], detail:[{}], Context: {}",
                feedRange,
                dce,
                this.operationContextTextProvider.get());
            Mono<Utils.ValueHolder<List<PartitionKeyRange>>> replacementRangesObs = getReplacementRanges(feedRange.getRange());

            // Since new DocumentProducers are instantiated for the new replacement ranges, if for the new
            // replacement partitions split happens the corresponding DocumentProducer can recursively handle splits.
            // so this is resilient to split on splits.
            Flux<DocumentProducer<T>> replacementProducers = replacementRangesObs.flux().flatMap(
                    partitionKeyRangesValueHolder ->  {
                        if (partitionKeyRangesValueHolder == null
                            || partitionKeyRangesValueHolder.v == null
                            || partitionKeyRangesValueHolder.v.size() == 0) {
                            logger.error("Failed to find at least one child range");
                            return Mono.error(new IllegalStateException("Failed to find at least one child range"));
                        }

                        if (partitionKeyRangesValueHolder.v.size() == 1) {
                            // The feedRange is gone due to merge
                            // we are going to continue drain the current document producer
                            // Due to the feedRange does not cover full partition anymore, during populateHeaders, startEpk and endEpk headers will be added
                            if (logger.isInfoEnabled()) {
                                logger.info(
                                    "Cross Partition Query Execution detected partition gone due to merge for feedRange [{}] with continuationToken [{}]",
                                    this.feedRange,
                                    lastResponseContinuationToken);
                            }

                            return Mono.just(this);
                        } else {
                            //The feedRange is gone due to split
                            if (logger.isInfoEnabled()) {
                                logger.info("Cross Partition Query Execution detected partition [{}] split into [{}] partitions,"
                                        + " last continuation token is [{}]. - Context: {}",
                                    feedRange,
                                    partitionKeyRangesValueHolder.v.stream()
                                        .map(JsonSerializable::toJson)
                                        .collect(Collectors.joining(", ")),
                                    lastResponseContinuationToken,
                                    this.operationContextTextProvider.get());
                            }
                            return Flux.fromIterable(createReplacingDocumentProducersOnSplit(partitionKeyRangesValueHolder.v));
                        }
                    });

            return produceOnFeedRangeGone(replacementProducers);
        });
    }

    protected Flux<DocumentProducerFeedResponse> produceOnFeedRangeGone(Flux<DocumentProducer<T>> replacingDocumentProducers) {
        return replacingDocumentProducers.flatMap(DocumentProducer::produceAsync, 1);
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

        return new DocumentProducer<>(
                client,
                collectionRid,
                cosmosQueryRequestOptions,
                createRequestFunc,
                executeRequestFuncWithRetries,
                collectionLink,
                null,
                resourceType ,
                correlatedActivityId,
                pageSize,
                initialContinuationToken,
                top,
                new FeedRangeEpkImpl(targetRange.toRange()),
                this.operationContextTextProvider);
    }

    private Mono<Utils.ValueHolder<List<PartitionKeyRange>>> getReplacementRanges(Range<String> range) {
        return client.getPartitionKeyRangeCache().tryGetOverlappingRangesAsync(
            null,
            collectionRid,
            range,
            true,
            qryOptionsAccessor.getProperties(cosmosQueryRequestOptions));
    }

    private boolean isSplitOrMerge(CosmosException e) {
        return Exceptions.isPartitionSplitOrMerge(e);
    }
}
