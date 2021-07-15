// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.BackoffRetryUtility;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.DocumentClientRetryPolicy;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InvalidPartitionExceptionRetryPolicy;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.PartitionKeyRangeGoneRetryPolicy;
import com.azure.cosmos.implementation.PathsHelper;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils.ValueHolder;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.caches.IPartitionKeyRangeCache;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.query.metrics.ClientSideMetrics;
import com.azure.cosmos.implementation.query.metrics.FetchExecutionRangeAccumulator;
import com.azure.cosmos.implementation.query.metrics.SchedulingStopwatch;
import com.azure.cosmos.implementation.query.metrics.SchedulingTimeSpan;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.implementation.routing.RoutingMapProviderHelper;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.SqlQuerySpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.azure.cosmos.models.ModelBridgeInternal.getPartitionKeyRangeIdInternal;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class DefaultDocumentQueryExecutionContext<T extends Resource> extends DocumentQueryExecutionContextBase<T> {

    private boolean isContinuationExpected;
    private volatile int retries = -1;

    private final SchedulingStopwatch fetchSchedulingMetrics;
    private final FetchExecutionRangeAccumulator fetchExecutionRangeAccumulator;
    private static final String DEFAULT_PARTITION_RANGE = "00-FF";

    public DefaultDocumentQueryExecutionContext(DiagnosticsClientContext diagnosticsClientContext, IDocumentQueryClient client, ResourceType resourceTypeEnum,
                                                Class<T> resourceType, SqlQuerySpec query, CosmosQueryRequestOptions cosmosQueryRequestOptions, String resourceLink,
                                                UUID correlatedActivityId, boolean isContinuationExpected) {

        super(diagnosticsClientContext, client,
                resourceTypeEnum,
                resourceType,
                query,
                cosmosQueryRequestOptions,
                resourceLink,
                false,
                correlatedActivityId);

        this.isContinuationExpected = isContinuationExpected;
        this.fetchSchedulingMetrics = new SchedulingStopwatch();
        this.fetchSchedulingMetrics.ready();
        this.fetchExecutionRangeAccumulator = new FetchExecutionRangeAccumulator(DEFAULT_PARTITION_RANGE);
    }

    protected PartitionKeyInternal getPartitionKeyInternal() {
        return this.cosmosQueryRequestOptions.getPartitionKey() == null ? null : BridgeInternal.getPartitionKeyInternal(cosmosQueryRequestOptions.getPartitionKey());
    }

    @Override
    public Flux<FeedResponse<T>> executeAsync() {

        if (cosmosQueryRequestOptions == null) {
            cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        }

        CosmosQueryRequestOptions newCosmosQueryRequestOptions = ModelBridgeInternal.createQueryRequestOptions(cosmosQueryRequestOptions);

        // We can not go to backend with the composite continuation token,
        // but we still need the gateway for the query plan.
        // The workaround is to try and parse the continuation token as a composite continuation token.
        // If it is, then we send the query to the gateway with max degree of parallelism to force getting back the query plan

        String originalContinuation = ModelBridgeInternal.getRequestContinuationFromQueryRequestOptions(newCosmosQueryRequestOptions);

        if (isClientSideContinuationToken(originalContinuation)) {
            // At this point we know we want back a query plan
            ModelBridgeInternal.setQueryRequestOptionsContinuationToken(newCosmosQueryRequestOptions, null);
            newCosmosQueryRequestOptions.setMaxDegreeOfParallelism(Integer.MAX_VALUE);
        }

        Integer maxItemCount = ModelBridgeInternal.getMaxItemCountFromQueryRequestOptions(newCosmosQueryRequestOptions);
        int maxPageSize = maxItemCount != null ? maxItemCount : Constants.Properties.DEFAULT_MAX_PAGE_SIZE;

        BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc = (continuationToken, pageSize) -> this.createRequestAsync(continuationToken, pageSize);

        // TODO: clean up if we want to use single vs observable.
        Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc = executeInternalAsyncFunc();

        return Paginator
    			.getPaginatedQueryResultAsObservable(newCosmosQueryRequestOptions, createRequestFunc, executeFunc, resourceType, maxPageSize);
    }

    public Mono<List<PartitionKeyRange>> getTargetPartitionKeyRanges(String resourceId, List<Range<String>> queryRanges) {
        return RoutingMapProviderHelper.getOverlappingRanges(client.getPartitionKeyRangeCache(), resourceId, queryRanges);
    }

    public Mono<Range<String>> getTargetRange(String collectionRid, FeedRangeInternal feedRangeInternal) {
        return feedRangeInternal.getNormalizedEffectiveRange(client.getPartitionKeyRangeCache(),
            /*metadataDiagnosticsCtx*/null,
                                                   this.client.getCollectionCache().resolveByRidAsync(
                                                       /*metadataDiagnosticsCtx*/ null,
                                                                                  collectionRid,
                                                       /*properties*/null));
    }

    public Mono<List<PartitionKeyRange>> getTargetPartitionKeyRangesById(String resourceId,
                                                                                      String partitionKeyRangeIdInternal) {
        return client.getPartitionKeyRangeCache()
                   .tryGetPartitionKeyRangeByIdAsync(null,
                                                     resourceId,
                                                     partitionKeyRangeIdInternal,
                                                     false,
                                                     null)
                   .flatMap(partitionKeyRange -> Mono.just(Collections.singletonList(partitionKeyRange.v)));
    }

    protected Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeInternalAsyncFunc() {
        RxCollectionCache collectionCache = this.client.getCollectionCache();
        IPartitionKeyRangeCache partitionKeyRangeCache =  this.client.getPartitionKeyRangeCache();
        DocumentClientRetryPolicy retryPolicyInstance = this.client.getResetSessionTokenRetryPolicy().getRequestPolicy();

        retryPolicyInstance = new InvalidPartitionExceptionRetryPolicy(
            collectionCache,
            retryPolicyInstance,
            resourceLink,
            ModelBridgeInternal.getPropertiesFromQueryRequestOptions(this.cosmosQueryRequestOptions));
        if (super.resourceTypeEnum.isPartitioned()) {
            retryPolicyInstance = new PartitionKeyRangeGoneRetryPolicy(this.diagnosticsClientContext,
                    collectionCache,
                    partitionKeyRangeCache,
                    PathsHelper.getCollectionPath(super.resourceLink),
                    retryPolicyInstance,
                    ModelBridgeInternal.getPropertiesFromQueryRequestOptions(this.cosmosQueryRequestOptions));
        }

        final DocumentClientRetryPolicy finalRetryPolicyInstance = retryPolicyInstance;

        return req -> {
            finalRetryPolicyInstance.onBeforeSendRequest(req);
            this.fetchExecutionRangeAccumulator.beginFetchRange();
            this.fetchSchedulingMetrics.start();
            return BackoffRetryUtility.executeRetry(() -> {
                ++this.retries;
                return executeRequestAsync(req);
            }, finalRetryPolicyInstance)
                    .map(tFeedResponse -> {
                        this.fetchSchedulingMetrics.stop();
                        this.fetchExecutionRangeAccumulator.endFetchRange(tFeedResponse.getActivityId(),
                                tFeedResponse.getResults().size(),
                                this.retries);
                        ImmutablePair<String, SchedulingTimeSpan> schedulingTimeSpanMap =
                                new ImmutablePair<>(DEFAULT_PARTITION_RANGE, this.fetchSchedulingMetrics.getElapsedTime());
                        if (!StringUtils.isEmpty(tFeedResponse.getResponseHeaders().get(HttpConstants.HttpHeaders.QUERY_METRICS))) {
                            QueryMetrics qm =
                                    BridgeInternal.createQueryMetricsFromDelimitedStringAndClientSideMetrics(tFeedResponse.getResponseHeaders()
                                                    .get(HttpConstants.HttpHeaders.QUERY_METRICS),
                                            new ClientSideMetrics(this.retries,
                                                    tFeedResponse.getRequestCharge(),
                                                    this.fetchExecutionRangeAccumulator.getExecutionRanges(),
                                                    Arrays.asList(schedulingTimeSpanMap)),
                                            tFeedResponse.getActivityId());
                            String pkrId = tFeedResponse.getResponseHeaders().get(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID);
                            String queryMetricKey = DEFAULT_PARTITION_RANGE + ",pkrId:" + pkrId;
                            BridgeInternal.putQueryMetricsIntoMap(tFeedResponse, queryMetricKey, qm);
                        }
                        return tFeedResponse;
                    });
        };
    }

    public RxDocumentServiceRequest createRequestAsync(String continuationToken, Integer maxPageSize) {

        // TODO this should be async
        Map<String, String> requestHeaders = this.createCommonHeadersAsync(
                this.getFeedOptions(continuationToken, maxPageSize));

        // TODO: add support for simple continuation for single partition query
        //requestHeaders.put(keyHttpConstants.HttpHeaders.IsContinuationExpected, isContinuationExpected.ToString())

        RxDocumentServiceRequest request = this.createDocumentServiceRequest(
                requestHeaders,
                this.query,
                this.getPartitionKeyInternal());

        if (!StringUtils.isEmpty(getPartitionKeyRangeIdInternal(cosmosQueryRequestOptions))) {
            request.routeTo(new PartitionKeyRangeIdentity(getPartitionKeyRangeIdInternal(cosmosQueryRequestOptions)));
        }

        return request;
    }

    private static boolean isClientSideContinuationToken(String continuationToken) {
        if (continuationToken != null) {
            ValueHolder<CompositeContinuationToken> outCompositeContinuationToken = new ValueHolder<CompositeContinuationToken>();
            if (CompositeContinuationToken.tryParse(continuationToken, outCompositeContinuationToken)) {
                return true;
            }

            ValueHolder<OrderByContinuationToken> outOrderByContinuationToken = new ValueHolder<OrderByContinuationToken>();
            if (OrderByContinuationToken.tryParse(continuationToken, outOrderByContinuationToken)) {
                return true;
            }

            ValueHolder<TakeContinuationToken> outTakeContinuationToken = new ValueHolder<TakeContinuationToken>();
            if (TakeContinuationToken.tryParse(continuationToken, outTakeContinuationToken)) {
                return true;
            }
        }

        return false;
    }
}

