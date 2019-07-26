// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.query;

import com.azure.data.cosmos.ChangeFeedOptions;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class Paginator {

    private final static Logger logger = LoggerFactory.getLogger(Paginator.class);

    public static <T extends Resource> Flux<FeedResponse<T>> getPaginatedChangeFeedQueryResultAsObservable(
            ChangeFeedOptions feedOptions, BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc,
            Function<RxDocumentServiceRequest, Flux<FeedResponse<T>>> executeFunc, Class<T> resourceType,
            int maxPageSize) {
        return getPaginatedQueryResultAsObservable(feedOptions.requestContinuation(), createRequestFunc, executeFunc, resourceType,
                -1, maxPageSize, true);
    }

    public static <T extends Resource> Flux<FeedResponse<T>> getPaginatedQueryResultAsObservable(
            FeedOptions feedOptions,
            BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc,
            Function<RxDocumentServiceRequest, Flux<FeedResponse<T>>> executeFunc, Class<T> resourceType,
            int maxPageSize) {
        return getPaginatedQueryResultAsObservable(feedOptions.requestContinuation(), createRequestFunc, executeFunc, resourceType,
                -1, maxPageSize);
    }

    public static <T extends Resource> Flux<FeedResponse<T>> getPaginatedQueryResultAsObservable(
            String continuationToken,
            BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc,
            Function<RxDocumentServiceRequest, Flux<FeedResponse<T>>> executeFunc, Class<T> resourceType,
            int top, int maxPageSize) {
        return getPaginatedQueryResultAsObservable(continuationToken, createRequestFunc, executeFunc, resourceType,
                top, maxPageSize, false);
    }

    private static <T extends Resource> Flux<FeedResponse<T>> getPaginatedQueryResultAsObservable(
            String continuationToken,
            BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc,
            Function<RxDocumentServiceRequest, Flux<FeedResponse<T>>> executeFunc, Class<T> resourceType,
            int top, int maxPageSize, boolean isChangeFeed) {

        return Flux.defer(() -> {
            Flux<Flux<FeedResponse<T>>> generate = Flux.generate(() ->
                    new Fetcher<>(createRequestFunc, executeFunc, continuationToken, isChangeFeed, top, maxPageSize),
                    (tFetcher, sink) -> {
                        if (tFetcher.shouldFetchMore()) {
                            Flux<FeedResponse<T>> nextPage = tFetcher.nextPage();
                            sink.next(nextPage);
                        } else {
                            logger.debug("No more results");
                            sink.complete();
                        }
                        return tFetcher;
            });

            return generate.flatMapSequential(feedResponseFlux -> feedResponseFlux, 1);
        });
    }
}
