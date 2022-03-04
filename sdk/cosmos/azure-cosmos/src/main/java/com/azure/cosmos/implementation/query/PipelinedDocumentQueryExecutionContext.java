// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.GenericItemTrait;
import com.azure.cosmos.implementation.GenericItemTraitFactory;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.SqlQuerySpec;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class PipelinedDocumentQueryExecutionContext<T extends GenericItemTrait<?>>
    implements IDocumentQueryExecutionContext<T> {

    private IDocumentQueryExecutionComponent<Document> pipelinedComponent;
    private IDocumentQueryExecutionComponent<T> shortCutComponent;
    private int actualPageSize;
    private UUID correlatedActivityId;
    private QueryInfo queryInfo;
    private final Class<T> classOfT;

    private PipelinedDocumentQueryExecutionContext(
        IDocumentQueryExecutionComponent<Document> pipelinedComponent,
        IDocumentQueryExecutionComponent<T> shortCutComponent,
        int actualPageSize,
            UUID correlatedActivityId, QueryInfo queryInfo, Class<T> classOfT) {
        this.shortCutComponent = shortCutComponent;
        this.pipelinedComponent = pipelinedComponent;
        this.actualPageSize = actualPageSize;
        this.correlatedActivityId = correlatedActivityId;
        this.queryInfo = queryInfo;
        this.classOfT = classOfT;

        // this.executeNextSchedulingMetrics = new SchedulingStopwatch();
        // this.executeNextSchedulingMetrics.Ready();

        // DefaultTrace.TraceVerbose(string.Format(
        // CultureInfo.InvariantCulture,
        // "{0} Pipelined~Context, actual page size: {1}",
        // DateTime.UtcNow.ToString("o", CultureInfo.InvariantCulture),
        // this.actualPageSize));
    }

    public static <T extends GenericItemTrait<?>> Flux<PipelinedDocumentQueryExecutionContext<T>> createAsync(
        DiagnosticsClientContext diagnosticsClientContext,
        IDocumentQueryClient client,
        PipelinedDocumentQueryParams<T> initParams,
        Class<T> classOfT) {

        // Use nested callback pattern to unwrap the continuation token and query params at each level.
        BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>> createBaseComponentFunction;

        QueryInfo queryInfo = initParams.getQueryInfo();
        UUID correlatedActivityId = initParams.getCorrelatedActivityId();
        CosmosQueryRequestOptions cosmosQueryRequestOptions = initParams.getCosmosQueryRequestOptions();

        if (queryInfo.hasOrderBy()) {
            createBaseComponentFunction = (continuationToken, documentQueryParams) -> {
                CosmosQueryRequestOptions orderByCosmosQueryRequestOptions = ModelBridgeInternal.createQueryRequestOptions(cosmosQueryRequestOptions);
                ModelBridgeInternal.setQueryRequestOptionsContinuationToken(orderByCosmosQueryRequestOptions, continuationToken);
                initParams.setCosmosQueryRequestOptions(orderByCosmosQueryRequestOptions);

                return OrderByDocumentQueryExecutionContext.createAsync(diagnosticsClientContext, client, documentQueryParams);
            };
        } else {
            createBaseComponentFunction = (continuationToken, documentQueryParams) -> {
                CosmosQueryRequestOptions parallelCosmosQueryRequestOptions = ModelBridgeInternal.createQueryRequestOptions(cosmosQueryRequestOptions);
                ModelBridgeInternal.setQueryRequestOptionsContinuationToken(parallelCosmosQueryRequestOptions, continuationToken);
                initParams.setCosmosQueryRequestOptions(parallelCosmosQueryRequestOptions);

                return ParallelDocumentQueryExecutionContext.createAsync(diagnosticsClientContext, client, documentQueryParams);
            };
        }

        BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>> createAggregateComponentFunction;
        if (queryInfo.hasAggregates() && !queryInfo.hasGroupBy()) {
            createAggregateComponentFunction =
                (continuationToken, documentQueryParams) ->
                    AggregateDocumentQueryExecutionContext.createAsync(createBaseComponentFunction,
                                                                      queryInfo.getAggregates(),
                                                                      queryInfo.getGroupByAliasToAggregateType(),
                                                                      queryInfo.getGroupByAliases(),
                                                                      queryInfo.hasSelectValue(),
                                                                      continuationToken,
                                                                      documentQueryParams);
        } else {
            createAggregateComponentFunction = createBaseComponentFunction;
        }

        BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>> createDistinctComponentFunction;
        if (queryInfo.hasDistinct()) {
            createDistinctComponentFunction =
                (continuationToken, documentQueryParams) ->
                    DistinctDocumentQueryExecutionContext.createAsync(createAggregateComponentFunction,
                                                                    queryInfo.getDistinctQueryType(),
                                                                    continuationToken,
                                                                    documentQueryParams);
        } else {
            createDistinctComponentFunction = createAggregateComponentFunction;
        }

        BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>> createGroupByComponentFunction;
        if (queryInfo.hasGroupBy()) {
            createGroupByComponentFunction =
                (continuationToken, documentQueryParams) ->
                    GroupByDocumentQueryExecutionContext.createAsync(createDistinctComponentFunction,
                                                                    continuationToken,
                                                                    queryInfo.getGroupByAliasToAggregateType(),
                                                                    queryInfo.getGroupByAliases(),
                                                                    queryInfo.hasSelectValue(),
                                                                    documentQueryParams);
        } else{
            createGroupByComponentFunction = createDistinctComponentFunction;
        }

        BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>> createSkipComponentFunction;
        if (queryInfo.hasOffset()) {
            createSkipComponentFunction =
                (continuationToken, documentQueryParams) ->
                    SkipDocumentQueryExecutionContext.createAsync(createGroupByComponentFunction,
                                                                 queryInfo.getOffset(),
                                                                 continuationToken,
                                                                 documentQueryParams);
        } else {
            createSkipComponentFunction = createGroupByComponentFunction;
        }

        BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>> createTopComponentFunction;
        if (queryInfo.hasTop()) {
            createTopComponentFunction =
                (continuationToken, documentQueryParams) ->
                    TopDocumentQueryExecutionContext.createAsync(createSkipComponentFunction,
                                                                queryInfo.getTop(),
                                                                queryInfo.getTop(),
                                                                continuationToken,
                                                                documentQueryParams);
        } else {
            createTopComponentFunction = createSkipComponentFunction;
        }

        BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>> createTakeComponentFunction;
        if (queryInfo.hasLimit()) {
            createTakeComponentFunction = (continuationToken, documentQueryParams) -> {
                int totalLimit = queryInfo.getLimit();
                if (queryInfo.hasOffset()) {
                    // This is being done to match the limit from rewritten query
                    totalLimit = queryInfo.getOffset() + queryInfo.getLimit();
                }
                return TopDocumentQueryExecutionContext.createAsync(createTopComponentFunction,
                                                                    queryInfo.getLimit(),
                                                                    totalLimit,
                                                                    continuationToken,
                                                                    documentQueryParams);
            };
        } else {
            createTakeComponentFunction = createTopComponentFunction;
        }

        BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>> createDCountComponentFunction;
        if (queryInfo.hasDCount()) {
            createDCountComponentFunction = (continuationToken, documentQueryParams) -> {
                return DCountDocumentQueryExecutionContext.createAsync(createTakeComponentFunction,
                                                                    queryInfo,
                                                                    continuationToken,
                                                                    documentQueryParams);
            };
        } else {
            createDCountComponentFunction = createTakeComponentFunction;
        }

        int actualPageSize = Utils.getValueOrDefault(ModelBridgeInternal.getMaxItemCountFromQueryRequestOptions(cosmosQueryRequestOptions),
                ParallelQueryConfig.ClientInternalPageSize);

        if (actualPageSize == -1) {
            actualPageSize = Integer.MAX_VALUE;
        }

        int pageSize = Math.min(actualPageSize, Utils.getValueOrDefault(queryInfo.getTop(), (actualPageSize)));

        if (createBaseComponentFunction == createDCountComponentFunction) {
            // simple parallelized query - we can use short-cut
            BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createShortCutBaseComponentFunction;
            createShortCutBaseComponentFunction = (continuationToken, documentQueryParams) -> {
                CosmosQueryRequestOptions parallelCosmosQueryRequestOptions = ModelBridgeInternal.createQueryRequestOptions(cosmosQueryRequestOptions);
                ModelBridgeInternal.setQueryRequestOptionsContinuationToken(parallelCosmosQueryRequestOptions, continuationToken);
                initParams.setCosmosQueryRequestOptions(parallelCosmosQueryRequestOptions);

                return ParallelDocumentQueryExecutionContext.createAsync(diagnosticsClientContext, client, documentQueryParams);
            };
            return createShortCutBaseComponentFunction.apply(ModelBridgeInternal.getRequestContinuationFromQueryRequestOptions(cosmosQueryRequestOptions), initParams)
                                                .map(c -> new PipelinedDocumentQueryExecutionContext<>(null, c, pageSize, correlatedActivityId, queryInfo, classOfT));
        } else {
            return createDCountComponentFunction.apply(ModelBridgeInternal.getRequestContinuationFromQueryRequestOptions(cosmosQueryRequestOptions), initParams.convertGenericType(Document.class))
                                                .map(c -> new PipelinedDocumentQueryExecutionContext<>(c, null, pageSize, correlatedActivityId, queryInfo, classOfT));
        }
    }

    public static <T extends GenericItemTrait<?>> Flux<PipelinedDocumentQueryExecutionContext<T>> createReadManyAsync(
        DiagnosticsClientContext diagnosticsClientContext, IDocumentQueryClient queryClient, String collectionResourceId, SqlQuerySpec sqlQuery,
        Map<PartitionKeyRange, SqlQuerySpec> rangeQueryMap, CosmosQueryRequestOptions cosmosQueryRequestOptions,
        String resourceId, String collectionLink, UUID activityId, Class<T> klass,
        ResourceType resourceTypeEnum) {
        Flux<IDocumentQueryExecutionComponent<T>> documentQueryExecutionComponentFlux =
            ParallelDocumentQueryExecutionContext.createReadManyQueryAsync(diagnosticsClientContext, queryClient,
                                                                           collectionResourceId, sqlQuery,
                                                                           rangeQueryMap,
                                                                           cosmosQueryRequestOptions, resourceId,
                                                                           collectionLink, activityId, klass,
                                                                           resourceTypeEnum);

        // TODO: Making pagesize -1. Should be reviewed
        return documentQueryExecutionComponentFlux.map(c -> new PipelinedDocumentQueryExecutionContext<T>(null, c, -1,
                                                                                                  activityId, null, klass));
    }

    @Override
    public Flux<FeedResponse<T>> executeAsync() {
        // TODO Auto-generated method stub

        // TODO add more code here
        if (this.shortCutComponent != null) {
            return this
                .shortCutComponent
                .drainAsync(actualPageSize);
        } else {
            return this
                .pipelinedComponent
                .drainAsync(actualPageSize)
                .map(documentFeedResponse -> documentFeedResponse.convertGenericType(
                    document -> GenericItemTraitFactory.createInstance(document.getPropertyBag(), this.classOfT)
                ));
        }
    }

    public QueryInfo getQueryInfo() {
        return this.queryInfo;
    }
}
