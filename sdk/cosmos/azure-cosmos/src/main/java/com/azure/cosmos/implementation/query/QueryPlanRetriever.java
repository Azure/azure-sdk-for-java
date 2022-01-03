// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
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
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class QueryPlanRetriever {
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
                                                                               PartitionKey partitionKey) {
        final Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(HttpConstants.HttpHeaders.CONTENT_TYPE, RuntimeConstants.MediaTypes.JSON);
        requestHeaders.put(HttpConstants.HttpHeaders.IS_QUERY_PLAN_REQUEST, TRUE);
        requestHeaders.put(HttpConstants.HttpHeaders.SUPPORTED_QUERY_FEATURES, SUPPORTED_QUERY_FEATURES);
        requestHeaders.put(HttpConstants.HttpHeaders.QUERY_VERSION, HttpConstants.Versions.QUERY_VERSION);

        if (partitionKey != null && partitionKey != PartitionKey.NONE) {
            PartitionKeyInternal partitionKeyInternal = BridgeInternal.getPartitionKeyInternal(partitionKey);
            requestHeaders.put(HttpConstants.HttpHeaders.PARTITION_KEY, partitionKeyInternal.toJson());
        }

        final RxDocumentServiceRequest request = RxDocumentServiceRequest.create(diagnosticsClientContext,
                                                                                 OperationType.QueryPlan,
                                                                                 ResourceType.Document,
                                                                                 resourceLink,
                                                                                 requestHeaders);
        request.UseGatewayMode = true;
        request.setByteBuffer(ModelBridgeInternal.serializeJsonToByteBuffer(sqlQuerySpec));

        final DocumentClientRetryPolicy retryPolicyInstance =
            queryClient.getResetSessionTokenRetryPolicy().getRequestPolicy();

        Function<RxDocumentServiceRequest, Mono<PartitionedQueryExecutionInfo>> executeFunc = req -> {
            return BackoffRetryUtility.executeRetry(() -> {
                retryPolicyInstance.onBeforeSendRequest(req);
                return queryClient.executeQueryAsync(request).flatMap(rxDocumentServiceResponse -> {
                    PartitionedQueryExecutionInfo partitionedQueryExecutionInfo =
                        new PartitionedQueryExecutionInfo(rxDocumentServiceResponse.getResponseBodyAsByteArray(), rxDocumentServiceResponse.getGatewayHttpRequestTimeline());
                    return Mono.just(partitionedQueryExecutionInfo);

                });
            }, retryPolicyInstance);
        };

        return executeFunc.apply(request);
    }
}
