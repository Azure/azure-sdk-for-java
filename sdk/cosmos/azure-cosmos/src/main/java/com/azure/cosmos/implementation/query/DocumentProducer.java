// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.DocumentClientRetryPolicy;
import com.azure.cosmos.implementation.Exceptions;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ObservableHelper;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.QueryMetricsConstants;
import com.azure.cosmos.implementation.Resource;
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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
class DocumentProducer<T extends Resource> {
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
                                Arrays.asList(schedulingTimeSpanMap)
                        ), pageResult.getActivityId(),
                    pageResult.getResponseHeaders().getOrDefault(HttpConstants.HttpHeaders.INDEX_UTILIZATION, null));
                String pkrId = pageResult.getResponseHeaders().get(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID);
                String queryMetricKey = feedRange.getRange().toString() + ",pkrId:" + pkrId;
                BridgeInternal.putQueryMetricsIntoMap(pageResult, queryMetricKey, qm);
            }
        }
    }

    protected final IDocumentQueryClient client;
    protected final String collectionRid;
    protected final CosmosQueryRequestOptions cosmosQueryRequestOptions;
    protected final Class<T> resourceType;
    protected PartitionKeyRange targetRange;
    protected final String collectionLink;
    protected final TriFunction<FeedRangeEpkImpl, String, Integer, RxDocumentServiceRequest> createRequestFunc;
    protected final Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeRequestFuncWithRetries;
    protected final Callable<DocumentClientRetryPolicy> createRetryPolicyFunc;
    protected final int pageSize;
    protected final UUID correlatedActivityId;
    public int top;
    private volatile String lastResponseContinuationToken;
    private final SchedulingStopwatch fetchSchedulingMetrics;
    private SchedulingStopwatch moveNextSchedulingMetrics;
    private final FetchExecutionRangeAccumulator fetchExecutionRangeAccumulator;
    protected FeedRangeEpkImpl feedRange;

    public DocumentProducer(
            IDocumentQueryClient client,
            String collectionResourceId,
            CosmosQueryRequestOptions cosmosQueryRequestOptions,
            TriFunction<FeedRangeEpkImpl, String, Integer, RxDocumentServiceRequest> createRequestFunc,
            Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeRequestFunc,
            PartitionKeyRange targetRange,
            String collectionLink,
            Callable<DocumentClientRetryPolicy> createRetryPolicyFunc,
            Class<T> resourceType ,
            UUID correlatedActivityId,
            int initialPageSize, // = -1,
            String initialContinuationToken,
            int top,
            FeedRangeEpkImpl feedRange) {

        this.client = client;
        this.collectionRid = collectionResourceId;

        this.createRequestFunc = createRequestFunc;

        this.fetchSchedulingMetrics = new SchedulingStopwatch();
        this.fetchSchedulingMetrics.ready();
        this.fetchExecutionRangeAccumulator = new FetchExecutionRangeAccumulator(feedRange.getRange().toString());

        this.executeRequestFuncWithRetries = request -> {
            retries = -1;
            this.fetchSchedulingMetrics.start();
            this.fetchExecutionRangeAccumulator.beginFetchRange();
            DocumentClientRetryPolicy retryPolicy = null;
            if (createRetryPolicyFunc != null) {
                try {
                    retryPolicy = createRetryPolicyFunc.call();
                } catch (Exception e) {
                    return Mono.error(e);
                }
            }

            DocumentClientRetryPolicy finalRetryPolicy = retryPolicy;
            return ObservableHelper.inlineIfPossibleAsObs(
                    () -> {
                        if(finalRetryPolicy != null) {
                            finalRetryPolicy.onBeforeSendRequest(request);
                        }

                        ++retries;
                        return executeRequestFunc.apply(request);
                    }, retryPolicy);
        };

        this.correlatedActivityId = correlatedActivityId;

        this.cosmosQueryRequestOptions = cosmosQueryRequestOptions != null ?
                                             ModelBridgeInternal.createQueryRequestOptions(cosmosQueryRequestOptions)
                                             : new CosmosQueryRequestOptions();
        ModelBridgeInternal.setQueryRequestOptionsContinuationToken(this.cosmosQueryRequestOptions, initialContinuationToken);
        this.lastResponseContinuationToken = initialContinuationToken;
        this.resourceType = resourceType;
        this.targetRange = targetRange;
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
                        ModelBridgeInternal.getRequestContinuationFromQueryRequestOptions(cosmosQueryRequestOptions),
                        sourcePartitionCreateRequestFunc,
                        executeRequestFuncWithRetries,
                        resourceType,
                        top,
                        pageSize)
                .map(rsp -> {
                    lastResponseContinuationToken = rsp.getContinuationToken();
                    this.fetchExecutionRangeAccumulator.endFetchRange(rsp.getActivityId(),
                            rsp.getResults().size(),
                            this.retries);
                    this.fetchSchedulingMetrics.stop();
                    return rsp;});

        return splitProof(obs.map(DocumentProducerFeedResponse::new));
    }

    private Flux<DocumentProducerFeedResponse> splitProof(Flux<DocumentProducerFeedResponse> sourceFeedResponseObservable) {
        return sourceFeedResponseObservable.onErrorResume( t -> {
            CosmosException dce = Utils.as(t, CosmosException.class);
            if (dce == null || !isSplit(dce)) {
                logger.error("Unexpected failure", t);
                return Flux.error(t);
            }

            // we are dealing with Split
            logger.info("DocumentProducer handling a partition split in [{}], detail:[{}]", feedRange, dce);
            Mono<Utils.ValueHolder<List<PartitionKeyRange>>> replacementRangesObs = getReplacementRanges(feedRange.getRange());

            // Since new DocumentProducers are instantiated for the new replacement ranges, if for the new
            // replacement partitions split happens the corresponding DocumentProducer can recursively handle splits.
            // so this is resilient to split on splits.
            Flux<DocumentProducer<T>> replacementProducers = replacementRangesObs.flux().flatMap(
                    partitionKeyRangesValueHolder ->  {
                        if (logger.isDebugEnabled()) {
                            logger.info("Cross Partition Query Execution detected partition [{}] split into [{}] partitions,"
                                    + " last continuation token is [{}].",
                                    feedRange,
                                    partitionKeyRangesValueHolder.v.stream()
                                                                   .map(ModelBridgeInternal::toJsonFromJsonSerializable)
                                                                   .collect(Collectors.joining(", ")),
                                    lastResponseContinuationToken);
                        }
                        return Flux.fromIterable(createReplacingDocumentProducersOnSplit(partitionKeyRangesValueHolder.v));
                    });

            return produceOnSplit(replacementProducers);
        });
    }

    protected Flux<DocumentProducerFeedResponse> produceOnSplit(Flux<DocumentProducer<T>> replacingDocumentProducers) {
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

        return new DocumentProducer<T>(
                client,
                collectionRid,
                cosmosQueryRequestOptions,
                createRequestFunc,
                executeRequestFuncWithRetries,
                targetRange,
                collectionLink,
                null,
                resourceType ,
                correlatedActivityId,
                pageSize,
                initialContinuationToken,
                top,
                new FeedRangeEpkImpl(targetRange.toRange()));
    }

    private Mono<Utils.ValueHolder<List<PartitionKeyRange>>> getReplacementRanges(Range<String> range) {
        return client.getPartitionKeyRangeCache().tryGetOverlappingRangesAsync(
            null,
            collectionRid,
            range,
            true,
            ModelBridgeInternal.getPropertiesFromQueryRequestOptions(cosmosQueryRequestOptions));
    }

    private boolean isSplit(CosmosException e) {
        return Exceptions.isPartitionSplit(e);
    }
}
