// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class Paginator {

    private final static Logger logger = LoggerFactory.getLogger(Paginator.class);

    public static <T extends Resource> Flux<FeedResponse<T>> getPaginatedQueryResultAsObservable(
        CosmosQueryRequestOptions cosmosQueryRequestOptions,
        BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc,
        Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc,
        Class<T> resourceType,
        int maxPageSize) {

        return getPaginatedQueryResultAsObservable(
            ModelBridgeInternal.getRequestContinuationFromQueryRequestOptions(cosmosQueryRequestOptions),
            createRequestFunc,
            executeFunc,
            resourceType,
            -1, maxPageSize);
    }

    public static <T extends Resource> Flux<FeedResponse<T>> getPaginatedQueryResultAsObservable(
            String continuationToken,
            BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc,
            Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc,
            Class<T> resourceType,
            int top,
            int maxPageSize) {

        return getPaginatedQueryResultAsObservable(
            continuationToken,
            createRequestFunc,
            executeFunc,
            resourceType,
            top,
            maxPageSize,
            false);
    }

    public static <T extends Resource> Flux<FeedResponse<T>> getChangeFeedQueryResultAsObservable(
        FeedRangeContinuation continuation,
        BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc,
        Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc,
        Class<T> resourceType,
        int top,
        int maxPageSize) {

        return getPaginatedQueryResultAsObservable(
            () -> new ChangeFeedFetcher<T>(
                createRequestFunc,
                executeFunc,
                continuation,
                true,
                top,
                maxPageSize));
    }

    private static <T extends Resource> Flux<FeedResponse<T>> getPaginatedQueryResultAsObservable(
        Supplier<Fetcher<T>> fetcherFactory) {

        return Flux.defer(() -> {
            Flux<Flux<FeedResponse<T>>> generate = Flux.generate(
                fetcherFactory::get,
                (tFetcher, sink) -> {
                    if (tFetcher.shouldFetchMore()) {
                        Mono<FeedResponse<T>> nextPage = tFetcher.nextPage();
                        sink.next(nextPage.flux());
                    } else {
                        logger.debug("No more results");
                        sink.complete();
                    }
                    return tFetcher;
                });

            return generate.flatMapSequential(feedResponseFlux -> feedResponseFlux, 1);
        });
    }

    private static <T extends Resource> Flux<FeedResponse<T>> getPaginatedQueryResultAsObservable(
            String continuationToken,
            BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc,
            Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc,
            Class<T> resourceType,
            int top,
            int maxPageSize,
            boolean isChangeFeed) {

        return getPaginatedQueryResultAsObservable(
            () -> new ServerSideOnlyContinuationFetcherImpl<>(
                createRequestFunc,
                executeFunc,
                continuationToken,
                isChangeFeed,
                top,
                maxPageSize));
    }
}
