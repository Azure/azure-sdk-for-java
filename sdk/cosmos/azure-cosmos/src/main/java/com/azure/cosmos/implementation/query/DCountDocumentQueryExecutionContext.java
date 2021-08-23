// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;

/**
 * Execution component that is able to aggregate COUNT(DISTINCT) from multiple continuations and partitions.
 *
 * @param <T> Resource generic type
 */
public class DCountDocumentQueryExecutionContext<T extends Resource> implements IDocumentQueryExecutionComponent<T> {
    private final IDocumentQueryExecutionComponent<T> component;
    private final QueryInfo info;
    private long count;
    private ConcurrentMap<String, QueryMetrics> queryMetricsMap = new ConcurrentHashMap<>();

    private DCountDocumentQueryExecutionContext(
        IDocumentQueryExecutionComponent<T> component,
        QueryInfo info,
        long count) {

        if (component == null) {
            throw new IllegalArgumentException("documentQueryExecutionComponent cannot be null");
        }

        this.component = component;
        this.count = count;
        this.info = info;
    }

    public static <T extends Resource> Flux<IDocumentQueryExecutionComponent<T>> createAsync(
        BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createSourceComponentFunction,
        QueryInfo info,
        String continuationToken,
        PipelinedDocumentQueryParams<T> documentQueryParams) {

        return createSourceComponentFunction
                   .apply(continuationToken, documentQueryParams)
                   .map(component -> new DCountDocumentQueryExecutionContext<T>(component, info, 0 /*default count*/));
    }

    IDocumentQueryExecutionComponent<T> getComponent() {
        return this.component;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Flux<FeedResponse<T>> drainAsync(int maxPageSize) {
        return this.component.drainAsync(maxPageSize)
                   .collectList()
                   .map(superList -> {
                       double requestCharge = 0;
                       final List<T> dcountResult = new ArrayList<>();
                       HashMap<String, String> headers = new HashMap<>();
                       List<ClientSideRequestStatistics> diagnosticsList = new ArrayList<>();

                       for (FeedResponse<T> page : superList) {
                           diagnosticsList.addAll(BridgeInternal
                                                      .getClientSideRequestStatisticsList(page
                                                                                              .getCosmosDiagnostics()));
                           count += page.getResults().size();
                           requestCharge += page.getRequestCharge();
                           QueryMetrics.mergeQueryMetricsMap(queryMetricsMap,
                                                             BridgeInternal.queryMetricsFromFeedResponse(page));
                       }

                       Document result = new Document();
                       if (Strings.isNullOrEmpty(info.getDCountAlias())) {
                           if (info.hasSelectValue()) {
                               result.set(Constants.Properties.VALUE, count);
                           } else {
                               // Setting $1 as the key to be consistent with service results
                               result.set("$1", count);
                           }
                       } else {
                           result.set(info.getDCountAlias(), count);
                       }
                       headers.put(HttpConstants.HttpHeaders.REQUEST_CHARGE, Double.toString(requestCharge));
                       FeedResponse<Document> frp =
                           BridgeInternal.createFeedResponseWithQueryMetrics(Collections.singletonList(result), headers,
                                                                             queryMetricsMap, null, false,
                                                                             false, null);

                       BridgeInternal.addClientSideDiagnosticsToFeed(frp.getCosmosDiagnostics(), diagnosticsList);
                       return (FeedResponse<T>) BridgeInternal
                                        .createFeedResponseWithQueryMetrics(Collections
                                                                                .singletonList(result),
                                                                            headers,
                                                                            BridgeInternal
                                                                                .queryMetricsFromFeedResponse(frp),
                                                                            ModelBridgeInternal
                                                                                .getQueryPlanDiagnosticsContext(frp),
                                                                            false,
                                                                            false,
                                                                            frp.getCosmosDiagnostics());
                   })
                   .flux();
    }
}
