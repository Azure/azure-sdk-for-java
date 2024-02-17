// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.implementation.BackoffRetryUtility;
import com.azure.cosmos.implementation.DocumentClientRetryPolicy;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RuntimeConstants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.fasterxml.jackson.databind.node.ObjectNode;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

class QueryPlanRetriever {
    private final static
    ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.CosmosQueryRequestOptionsAccessor qryOptAccessor =
        ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.getCosmosQueryRequestOptionsAccessor();
    private final static
    ImplementationBridgeHelpers.CosmosQueryRequestOptionsBaseHelper.CosmosQueryRequestOptionsBaseAccessor qryOptBaseAccessor =
        ImplementationBridgeHelpers.CosmosQueryRequestOptionsBaseHelper.getCosmosQueryRequestOptionsBaseAccessor();

    private static final String TRUE = "True";
    private static final String SUPPORTED_QUERY_FEATURES = QueryFeature.Aggregate.name() + ", " +
                                                               QueryFeature.CompositeAggregate.name() + ", " +
                                                               QueryFeature.MultipleOrderBy.name() + ", " +
                                                               QueryFeature.MultipleAggregates.name() + ", " +
                                                               QueryFeature.OrderBy.name() + ", " +
                                                               QueryFeature.OffsetAndLimit.name() + ", " +
                                                               QueryFeature.Distinct.name() + ", " +
                                                               QueryFeature.GroupBy.name() + ", " +
                                                               QueryFeature.Top.name() + ", " +
                                                               QueryFeature.DCount.name() + ", " +
                                                               QueryFeature.NonValueAggregate.name();

    static Mono<PartitionedQueryExecutionInfo> getQueryPlanThroughGatewayAsync(DiagnosticsClientContext diagnosticsClientContext,
                                                                               IDocumentQueryClient queryClient,
                                                                               SqlQuerySpec sqlQuerySpec,
                                                                               String resourceLink,
                                                                               CosmosQueryRequestOptions initialQueryRequestOptions) {

        CosmosQueryRequestOptions nonNullRequestOptions = initialQueryRequestOptions != null
            ? initialQueryRequestOptions
            : new CosmosQueryRequestOptions();

        PartitionKey partitionKey = nonNullRequestOptions.getPartitionKey();


        final Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(HttpConstants.HttpHeaders.CONTENT_TYPE, RuntimeConstants.MediaTypes.JSON);
        requestHeaders.put(HttpConstants.HttpHeaders.IS_QUERY_PLAN_REQUEST, TRUE);
        requestHeaders.put(HttpConstants.HttpHeaders.SUPPORTED_QUERY_FEATURES, SUPPORTED_QUERY_FEATURES);
        requestHeaders.put(HttpConstants.HttpHeaders.QUERY_VERSION, HttpConstants.Versions.QUERY_VERSION);

        if (partitionKey != null && partitionKey != PartitionKey.NONE) {
            PartitionKeyInternal partitionKeyInternal = BridgeInternal.getPartitionKeyInternal(partitionKey);
            requestHeaders.put(HttpConstants.HttpHeaders.PARTITION_KEY, partitionKeyInternal.toJson());
        }

        final RxDocumentServiceRequest queryPlanRequest = RxDocumentServiceRequest.create(diagnosticsClientContext,
                                                                                 OperationType.QueryPlan,
                                                                                 ResourceType.Document,
                                                                                 resourceLink,
                                                                                 requestHeaders);
        queryPlanRequest.useGatewayMode = true;
        queryPlanRequest.setByteBuffer(ModelBridgeInternal.serializeJsonToByteBuffer(sqlQuerySpec));

        CosmosEndToEndOperationLatencyPolicyConfig end2EndConfig =
            qryOptBaseAccessor.getEndToEndOperationLatencyPolicyConfig(qryOptAccessor.getImpl(nonNullRequestOptions));
        if (end2EndConfig != null) {
            queryPlanRequest.requestContext.setEndToEndOperationLatencyPolicyConfig(end2EndConfig);
        }

        BiFunction<Supplier<DocumentClientRetryPolicy>, RxDocumentServiceRequest, Mono<PartitionedQueryExecutionInfo>> executeFunc =
            (retryPolicyFactory, req) -> {
                DocumentClientRetryPolicy retryPolicyInstance = retryPolicyFactory.get();
                retryPolicyInstance.onBeforeSendRequest(req);

                return BackoffRetryUtility.executeRetry(() ->
                    queryClient.executeQueryAsync(req).flatMap(rxDocumentServiceResponse -> {
                        PartitionedQueryExecutionInfo partitionedQueryExecutionInfo =
                            new PartitionedQueryExecutionInfo(
                                (ObjectNode)rxDocumentServiceResponse.getResponseBody(),
                                rxDocumentServiceResponse.getGatewayHttpRequestTimeline());
                        return Mono.just(partitionedQueryExecutionInfo);

                    }), retryPolicyInstance);
            };

        return queryClient.executeFeedOperationWithAvailabilityStrategy(
            ResourceType.Document,
            OperationType.QueryPlan,
            () -> queryClient.getResetSessionTokenRetryPolicy().getRequestPolicy(diagnosticsClientContext),
            queryPlanRequest,
            executeFunc
        );
    }
}
