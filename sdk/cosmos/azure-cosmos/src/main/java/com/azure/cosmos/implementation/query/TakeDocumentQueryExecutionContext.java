// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Utils.ValueHolder;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import org.slf4j.Logger;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class TakeDocumentQueryExecutionContext<T>
    implements IDocumentQueryExecutionComponent<T> {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(TakeDocumentQueryExecutionContext.class);
    private final IDocumentQueryExecutionComponent<T> component;
    private final int takeCount;
    private final TakeEnum takeEnum;

    public TakeDocumentQueryExecutionContext(IDocumentQueryExecutionComponent<T> component, int take, TakeEnum takeEnum) {
        this.component = component;
        this.takeCount = take;
        this.takeEnum = takeEnum;
    }

    public static <T> Flux<IDocumentQueryExecutionComponent<T>> createAsync(BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createSourceComponentFunction,
                                                                            int takeCount,
                                                                            String takeContinuationTokenString,
                                                                            PipelinedDocumentQueryParams<T> documentQueryParams,
                                                                            TakeEnum takeEnum) {
        switch (takeEnum) {
            case LIMIT:
                return createLimitAsync(createSourceComponentFunction, takeCount, takeContinuationTokenString,
                    documentQueryParams);
            case TOP:
                return createTopAsync(createSourceComponentFunction, takeCount, takeContinuationTokenString,
                    documentQueryParams);
            default:
                throw new IllegalArgumentException("Unknown take enum: " + takeEnum);
        }

    }

    private static <T> Flux<IDocumentQueryExecutionComponent<T>> createTopAsync(BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createSourceComponentFunction,
                                                                               int topCount,
                                                                               String topContinuationTokenString,
                                                                               PipelinedDocumentQueryParams<T> documentQueryParams) {
        TopContinuationToken topContinuationToken;

        if (topContinuationTokenString == null) {
            topContinuationToken = new TopContinuationToken(topCount, null);
        } else {
            ValueHolder<TopContinuationToken> outTopContinuationToken = new ValueHolder<>();
            if (!TopContinuationToken.tryParse(topContinuationTokenString, outTopContinuationToken)) {
                String message = String.format("INVALID JSON in continuation token %s for Top~Context",
                    topContinuationTokenString);
                CosmosException dce = BridgeInternal.createCosmosException(HttpConstants.StatusCodes.BADREQUEST,
                        message);
                return Flux.error(dce);
            }

            topContinuationToken = outTopContinuationToken.v;
        }

        if (topContinuationToken.getTopCount() > topCount) {
            String message = String.format(
                "top count in continuation token: %d can not be greater than the top count in the query: %d.",
                topContinuationToken.getTopCount(), topCount);
            CosmosException dce = BridgeInternal.createCosmosException(HttpConstants.StatusCodes.BADREQUEST, message);
            return Flux.error(dce);
        }

        // The top value setting here will be propagated down to document producer.
        documentQueryParams.setTop(topCount);

        return createSourceComponentFunction
            .apply(topContinuationToken.getSourceToken(), documentQueryParams)
            .map(component -> new TakeDocumentQueryExecutionContext<>(component,
                topContinuationToken.getTopCount(), TakeEnum.TOP));
    }

    private static <T> Flux<IDocumentQueryExecutionComponent<T>> createLimitAsync(
            BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createSourceComponentFunction,
            int limit,
            String limitContinuationTokenString,
            PipelinedDocumentQueryParams<T> documentQueryParams) {
        LimitContinuationToken limitContinuationToken;

        if (limitContinuationTokenString == null) {
            limitContinuationToken = new LimitContinuationToken(limit, null);
        } else {
            ValueHolder<LimitContinuationToken> outLimitContinuationToken = new ValueHolder<>();
            if (!LimitContinuationToken.tryParse(limitContinuationTokenString, outLimitContinuationToken)) {
                String message = String.format("INVALID JSON in continuation token %s for Limit~Context",
                        limitContinuationTokenString);
                CosmosException dce = BridgeInternal.createCosmosException(HttpConstants.StatusCodes.BADREQUEST,
                        message);
                return Flux.error(dce);
            }

            limitContinuationToken = outLimitContinuationToken.v;
        }

        if (limitContinuationToken.getLimitCount() > limit) {
            String message = String.format(
                    "limit count in continuation token: %d can not be greater than the limit count in the query: %d.",
                    limitContinuationToken.getLimitCount(), limit);
            CosmosException dce = BridgeInternal.createCosmosException(HttpConstants.StatusCodes.BADREQUEST, message);
            return Flux.error(dce);
        }

        return createSourceComponentFunction
                .apply(limitContinuationToken.getSourceToken(), documentQueryParams)
                .map(component -> new TakeDocumentQueryExecutionContext<>(component,
                    limitContinuationToken.getLimitCount(), TakeEnum.LIMIT));
    }

    //  A must-read article on lambdas and anonymous classes - https://www.infoq.com/articles/Java-8-Lambdas-A-Peek-Under-the-Hood/
    @Override
    public Flux<FeedResponse<T>> drainAsync(int maxPageSize) {

        return this.component.drainAsync(maxPageSize).takeUntil(new Predicate<FeedResponse<T>>() {

            private int fetchedItems = 0;

            @Override
            public boolean test(FeedResponse<T> frp) {

                fetchedItems += frp.getResults().size();

                // take until we have at least top many elements fetched
                return fetchedItems >= takeCount;
            }
        }).map(new Function<FeedResponse<T>, FeedResponse<T>>() {

            private int collectedItems = 0;
            private boolean lastPage = false;
            @Override
            public FeedResponse<T> apply(FeedResponse<T> t) {

                if (collectedItems + t.getResults().size() <= takeCount) {
                    collectedItems += t.getResults().size();

                    Map<String, String> headers = new HashMap<>(t.getResponseHeaders());
                    if (takeCount != collectedItems) {
                        // Add Take Continuation Token
                        String sourceContinuationToken = t.getContinuationToken();
                        if (sourceContinuationToken != null) {
                            String continuationTokenJson = null;
                            switch (takeEnum) {
                                case LIMIT:
                                    continuationTokenJson = new LimitContinuationToken(takeCount - collectedItems,
                                        sourceContinuationToken).toJson();
                                    break;
                                case TOP:
                                    continuationTokenJson = new TopContinuationToken(takeCount - collectedItems,
                                        sourceContinuationToken).toJson();
                                    break;
                            }
                            headers.put(HttpConstants.HttpHeaders.CONTINUATION, continuationTokenJson);
                        } else {
                            // Null out the continuation token. The sourceContinuationToken being null means
                            // that this is the last page and there are no more elements left to fetch.
                            headers.put(HttpConstants.HttpHeaders.CONTINUATION, null);
                        }
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
                    assert !lastPage;
                    lastPage = true;
                    int lastPageSize = takeCount - collectedItems;
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

    enum TakeEnum {
        LIMIT,
        TOP
    }
}
