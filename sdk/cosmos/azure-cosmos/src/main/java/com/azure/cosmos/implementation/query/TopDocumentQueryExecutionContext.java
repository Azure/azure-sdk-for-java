// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.Utils.ValueHolder;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class TopDocumentQueryExecutionContext<T extends Resource> implements IDocumentQueryExecutionComponent<T> {

    private final IDocumentQueryExecutionComponent<T> component;
    private final int top;
    // limit from rewritten query
    private final int limit;

    public TopDocumentQueryExecutionContext(IDocumentQueryExecutionComponent<T> component, int top, int limit) {
        this.component = component;
        this.top = top;
        this.limit = limit;
    }

    public static <T extends Resource> Flux<IDocumentQueryExecutionComponent<T>> createAsync(
            BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createSourceComponentFunction,
            int topCount,
            int limit,
            String topContinuationToken,
            PipelinedDocumentQueryParams<T> documentQueryParams) {
        TakeContinuationToken takeContinuationToken;

        if (topContinuationToken == null) {
            takeContinuationToken = new TakeContinuationToken(topCount, null);
        } else {
            ValueHolder<TakeContinuationToken> outTakeContinuationToken = new ValueHolder<TakeContinuationToken>();
            if (!TakeContinuationToken.tryParse(topContinuationToken, outTakeContinuationToken)) {
                String message = String.format("INVALID JSON in continuation token %s for Top~Context",
                        topContinuationToken);
                CosmosException dce = BridgeInternal.createCosmosException(HttpConstants.StatusCodes.BADREQUEST,
                        message);
                return Flux.error(dce);
            }

            takeContinuationToken = outTakeContinuationToken.v;
        }

        if (takeContinuationToken.getTakeCount() > topCount) {
            String message = String.format(
                    "top count in continuation token: %d can not be greater than the top count in the query: %d.",
                    takeContinuationToken.getTakeCount(), topCount);
            CosmosException dce = BridgeInternal.createCosmosException(HttpConstants.StatusCodes.BADREQUEST, message);
            return Flux.error(dce);
        }

        // The top value setting here will be propagated down to document producer.
        documentQueryParams.setTop(limit);

        return createSourceComponentFunction
                .apply(takeContinuationToken.getSourceToken(), documentQueryParams)
                .map(component -> new TopDocumentQueryExecutionContext<>(component,
                                                                         takeContinuationToken.getTakeCount(), limit));
    }

    @Override
    public Flux<FeedResponse<T>> drainAsync(int maxPageSize) {

        return this.component.drainAsync(maxPageSize).takeUntil(new Predicate<FeedResponse<T>>() {

            private volatile int fetchedItems = 0;

            @Override
            public boolean test(FeedResponse<T> frp) {

                fetchedItems += frp.getResults().size();

                // take until we have at least top many elements fetched
                return fetchedItems >= top;
            }
        }).map(new Function<FeedResponse<T>, FeedResponse<T>>() {

            private volatile int collectedItems = 0;
            private volatile boolean lastPage = false;

            @Override
            public FeedResponse<T> apply(FeedResponse<T> t) {

                if (collectedItems + t.getResults().size() <= top) {
                    collectedItems += t.getResults().size();

                    Map<String, String> headers = new HashMap<>(t.getResponseHeaders());
                    if (top != collectedItems) {
                        // Add Take Continuation Token
                        String sourceContinuationToken = t.getContinuationToken();
                        TakeContinuationToken takeContinuationToken = new TakeContinuationToken(top - collectedItems,
                                sourceContinuationToken);
                        headers.put(HttpConstants.HttpHeaders.CONTINUATION, takeContinuationToken.toJson());
                    } else {
                        // Null out the continuation token
                        headers.put(HttpConstants.HttpHeaders.CONTINUATION, null);
                    }

                    return BridgeInternal.createFeedResponseWithQueryMetrics(t.getResults(),
                        headers,
                        BridgeInternal.queryMetricsFromFeedResponse(t),
                        ModelBridgeInternal.getQueryPlanDiagnosticsContext(t),
                        false,
                        false,
                        t.getCosmosDiagnostics());
                } else {
                    assert lastPage == false;
                    lastPage = true;
                    int lastPageSize = top - collectedItems;
                    collectedItems += lastPageSize;

                    // Null out the continuation token
                    Map<String, String> headers = new HashMap<>(t.getResponseHeaders());
                    headers.put(HttpConstants.HttpHeaders.CONTINUATION, null);

                    return BridgeInternal.createFeedResponseWithQueryMetrics(t.getResults().subList(0, lastPageSize),
                        headers,
                        BridgeInternal.queryMetricsFromFeedResponse(t),
                        ModelBridgeInternal.getQueryPlanDiagnosticsContext(t),
                        false,
                        false,
                        t.getCosmosDiagnostics());
                }
            }
        });
    }
}
