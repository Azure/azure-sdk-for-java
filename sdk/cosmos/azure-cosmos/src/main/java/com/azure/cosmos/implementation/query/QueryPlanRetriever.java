// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.SqlQuerySpec;
import com.azure.cosmos.implementation.BackoffRetryUtility;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IDocumentClientRetryPolicy;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RuntimeConstants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class QueryPlanRetriever {
    private static final String TRUE = "True";
    private static final String SUPPORTED_QUERY_FEATURES = QueryFeature.Aggregate.name() + ", " +
                                                               QueryFeature.CompositeAggregate.name() + ", " +
                                                               QueryFeature.MultipleOrderBy.name() + ", " +
                                                               QueryFeature.OrderBy.name() + ", " +
                                                               QueryFeature.OffsetAndLimit.name() + ", " +
                                                               QueryFeature.Top.name();

    static Mono<PartitionedQueryExecutionInfo> getQueryPlanThroughGatewayAsync(IDocumentQueryClient queryClient,
                                                                               SqlQuerySpec sqlQuerySpec,
                                                                               String resourceLink) {
        final Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(HttpConstants.HttpHeaders.CONTENT_TYPE, RuntimeConstants.MediaTypes.JSON);
        requestHeaders.put(HttpConstants.HttpHeaders.IS_QUERY_PLAN_REQUEST, TRUE);
        requestHeaders.put(HttpConstants.HttpHeaders.SUPPORTED_QUERY_FEATURES, SUPPORTED_QUERY_FEATURES);
        requestHeaders.put(HttpConstants.HttpHeaders.QUERY_VERSION, HttpConstants.Versions.QUERY_VERSION);

        final RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.QueryPlan,
                                                                                 ResourceType.Document,
                                                                                 resourceLink,
                                                                                 requestHeaders);
        request.UseGatewayMode = true;
        request.setContentBytes(sqlQuerySpec.toJson().getBytes(StandardCharsets.UTF_8));

        final IDocumentClientRetryPolicy retryPolicyInstance =
            queryClient.getResetSessionTokenRetryPolicy().getRequestPolicy();

        Function<RxDocumentServiceRequest, Mono<PartitionedQueryExecutionInfo>> executeFunc = req -> {
            return BackoffRetryUtility.executeRetry(() -> {
                retryPolicyInstance.onBeforeSendRequest(req);
                return queryClient.executeQueryAsync(request).flatMap(rxDocumentServiceResponse -> {
                    PartitionedQueryExecutionInfo partitionedQueryExecutionInfo =
                        new PartitionedQueryExecutionInfo(rxDocumentServiceResponse.getReponseBodyAsString());
                    return Mono.just(partitionedQueryExecutionInfo);

                });
            }, retryPolicyInstance);
        };

        return executeFunc.apply(request);
    }
}
