// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.routing.UInt128;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public class DistinctDocumentQueryExecutionContext<T extends Resource> implements IDocumentQueryExecutionComponent<T> {
    private final IDocumentQueryExecutionComponent<T> component;
    private final DistinctMap distinctMap;
    private final AtomicReference<UInt128> lastHash;

    private DistinctDocumentQueryExecutionContext(
        IDocumentQueryExecutionComponent<T> component,
        DistinctQueryType distinctQueryType,
        UInt128 previousHash) {
        if (distinctQueryType == DistinctQueryType.NONE) {
            throw new IllegalArgumentException("Invalid distinct query type");
        }

        if (component == null) {
            throw new IllegalArgumentException("documentQueryExecutionComponent cannot be null");
        }

        this.component = component;
        this.distinctMap = DistinctMap.create(distinctQueryType, previousHash);
        this.lastHash = new AtomicReference<>();
    }

    public static <T extends Resource> Flux<IDocumentQueryExecutionComponent<T>> createAsync(
        BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createSourceComponentFunction,
        DistinctQueryType distinctQueryType,
        String continuationToken,
        PipelinedDocumentQueryParams<T> documentQueryParams) {

        Utils.ValueHolder<DistinctContinuationToken> outDistinctcontinuationtoken = new Utils.ValueHolder<>();
        DistinctContinuationToken distinctContinuationToken = new DistinctContinuationToken(null /*lasthash*/,
                                                                                            null /*sourceToken*/);

        if (continuationToken != null) {
            if (!DistinctContinuationToken.tryParse(continuationToken, outDistinctcontinuationtoken)) {
                return Flux.error(new BadRequestException("Invalid DistinctContinuationToken" + continuationToken));
            } else {
                distinctContinuationToken = outDistinctcontinuationtoken.v;
                if (distinctQueryType != DistinctQueryType.ORDERED && distinctContinuationToken.getLastHash() != null) {
                    CosmosException dce = new BadRequestException("DistinctContinuationToken is malformed." +
                                                                              " DistinctContinuationToken can not" +
                                                                              " have a 'lastHash', when the query" +
                                                                              " type is not ordered (ex SELECT " +
                                                                              "DISTINCT VALUE c.blah FROM c ORDER" +
                                                                              " BY c.blah).");
                    return Flux.error(dce);
                }
            }
        }

        final UInt128 continuationTokenLastHash = distinctContinuationToken.getLastHash();

        return createSourceComponentFunction
            .apply(distinctContinuationToken.getSourceToken(), documentQueryParams)
            .map(component -> new DistinctDocumentQueryExecutionContext<T>(component, distinctQueryType, continuationTokenLastHash));
    }

    IDocumentQueryExecutionComponent<T> getComponent() {
        return this.component;
    }

    @Override
    public Flux<FeedResponse<T>> drainAsync(int maxPageSize) {
        return this.component.drainAsync(maxPageSize).map(tFeedResponse -> {
            final List<T> distinctResults = new ArrayList<>();

            tFeedResponse.getResults().forEach(document -> {
                Utils.ValueHolder<UInt128> outHash = new Utils.ValueHolder<>();
                if (this.distinctMap.add(document, outHash)) {
                    distinctResults.add(document);
                    this.lastHash.set(outHash.v);
                }
            });
            Map<String, String> headers = new HashMap<>(tFeedResponse.getResponseHeaders());
            if (tFeedResponse.getContinuationToken() != null) {

                final String sourceContinuationToken = tFeedResponse.getContinuationToken();
                final DistinctContinuationToken distinctContinuationToken =
                    new DistinctContinuationToken(this.lastHash.get(),
                        sourceContinuationToken);
                headers.put(HttpConstants.HttpHeaders.CONTINUATION,
                            ModelBridgeInternal.toJsonFromJsonSerializable(distinctContinuationToken));
            }

            return BridgeInternal.createFeedResponseWithQueryMetrics(distinctResults,
                headers,
                BridgeInternal.queryMetricsFromFeedResponse(tFeedResponse),
                ModelBridgeInternal.getQueryPlanDiagnosticsContext(tFeedResponse),
                false,
                false,
                tFeedResponse.getCosmosDiagnostics());
        });

    }
}
