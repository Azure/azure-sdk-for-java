// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.perPartitionCircuitBreaker.GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

public class PaginatorTest {
    @Test(groups = { "unit" }, dataProvider = "queryParams")
    public void preFetchCalculation(
        CosmosQueryRequestOptions testQueryOptions,
        int testTop,
        int testPageSize,
        int expectedPreFetchCount) {

        assertThat(
            Paginator
                .getPreFetchCount(testQueryOptions, testTop, testPageSize)
        ).isEqualTo(expectedPreFetchCount);
    }

    @Test(groups = { "unit" })
    public void prefetchOptionsAreValidatedAndCloned() {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        assertThat(options.getMaxPrefetchPageCount()).isNull();
        assertThat(options.setMaxPrefetchPageCount(0)).isSameAs(options);
        assertThat(Paginator.getPreFetchCount(options, -1, 20)).isZero();
        CosmosQueryRequestOptions clone = ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper
            .getCosmosQueryRequestOptionsAccessor().clone(options);
        assertThat(clone.getMaxPrefetchPageCount()).isZero();
        options.setMaxPrefetchPageCount(5);
        assertThat(Paginator.getPreFetchCount(options, -1, 20)).isEqualTo(5);
        assertThat(clone.getMaxPrefetchPageCount()).isZero();
        options.setMaxPrefetchPageCount(Integer.MAX_VALUE);
        assertThat(Paginator.getPreFetchCount(options, -1, 20)).isEqualTo(32);
        assertThat(options.setMaxPrefetchPageCount(null).getMaxPrefetchPageCount()).isNull();
        assertThatThrownBy(() -> options.setMaxPrefetchPageCount(-1))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DataProvider(name = "asyncResponses")
    public static Object[][] asyncResponses() {
        return new Object[][] { { false }, { true } };
    }

    @Test(groups = { "unit" }, dataProvider = "asyncResponses")
    public void zeroPrefetchWaitsForDemand(boolean async) {
        AtomicInteger requests = new AtomicInteger();
        Flux<FeedResponse<Document>> pages = queryPages(requests, async, false);
        StepVerifier.create(pages, 0)
            .expectSubscription()
            .expectNoEvent(Duration.ofMillis(20))
            .then(() -> assertThat(requests.get()).isZero())
            .thenRequest(1)
            .expectNextCount(1)
            .expectNoEvent(Duration.ofMillis(20))
            .then(() -> assertThat(requests.get()).isEqualTo(1))
            .thenRequest(1)
            .expectNextCount(1)
            .expectNoEvent(Duration.ofMillis(20))
            .then(() -> assertThat(requests.get()).isEqualTo(2))
            .thenCancel()
            .verify(Duration.ofSeconds(5));
        assertThat(requests.get()).isEqualTo(2);
    }

    @Test(groups = { "unit" }, dataProvider = "asyncResponses")
    public void zeroPrefetchNextFetchesOnlyOnePage(boolean async) {
        AtomicInteger requests = new AtomicInteger();
        StepVerifier.create(queryPages(requests, async, false).next())
            .assertNext(page -> assertThat(page.getContinuationToken()).isEqualTo("cp1"))
            .expectComplete()
            .verify(Duration.ofSeconds(5));
        assertThat(requests.get()).isEqualTo(1);
    }

    @Test(groups = { "unit" }, dataProvider = "asyncResponses")
    public void zeroPrefetchCompletesAfterLastPage(boolean async) {
        AtomicInteger requests = new AtomicInteger();
        StepVerifier.create(queryPages(requests, async, false))
            .expectNextCount(3)
            .expectComplete()
            .verify(Duration.ofSeconds(5));
        assertThat(requests.get()).isEqualTo(3);
    }

    @Test(groups = { "unit" }, dataProvider = "asyncResponses")
    public void zeroPrefetchPropagatesErrors(boolean async) {
        AtomicInteger requests = new AtomicInteger();
        StepVerifier.create(queryPages(requests, async, true))
            .expectErrorMessage("query failed")
            .verify(Duration.ofSeconds(5));
        assertThat(requests.get()).isEqualTo(1);
    }

    private Flux<FeedResponse<Document>> queryPages(AtomicInteger requests, boolean async, boolean fail) {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions().setMaxPrefetchPageCount(0);
        ModelBridgeInternal.setQueryRequestOptionsContinuationTokenAndMaxItemCount(options, "cp0", 20);
        return Paginator.getPaginatedQueryResultAsObservable(
            options,
            (token, pageSize) -> {
                assertThat(token).isEqualTo("cp" + requests.getAndIncrement());
                assertThat(pageSize).isEqualTo(20);
                return mock(RxDocumentServiceRequest.class);
            },
            request -> {
                Mono<FeedResponse<Document>> response = fail ? Mono.error(new IllegalStateException("query failed"))
                    : Mono.just(FeedResponseBuilder.queryFeedResponseBuilder(Document.class)
                        .withContinuationToken(requests.get() == 3 ? null : "cp" + requests.get())
                        .withResults(new Document())
                        .build());
                return async ? Mono.delay(Duration.ofMillis(1)).then(response) : response;
            },
            20,
            mock(GlobalEndpointManager.class),
            mock(GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.class));
    }

    @DataProvider(name = "queryParams")
    public static Object[][] queryParamProvider() {

        CosmosQueryRequestOptions optionsWithBufferSizeTenThousandAndDifferentMaxItemCount =
            new CosmosQueryRequestOptions().setMaxBufferedItemCount(10_000);
        // initial continuation token
        ModelBridgeInternal
            .setQueryRequestOptionsContinuationTokenAndMaxItemCount(
                optionsWithBufferSizeTenThousandAndDifferentMaxItemCount,
                "someContinuation",
                1); // maxItemCount 1 to test that explicit page Size trumps query options

        CosmosQueryRequestOptions optionsWithBufferSizeTenThousand =
            new CosmosQueryRequestOptions().setMaxBufferedItemCount(10_000);
        // initial continuation token
        ModelBridgeInternal
            .setQueryRequestOptionsContinuationTokenAndMaxItemCount(
                optionsWithBufferSizeTenThousand,
                "someContinuation",
                1_000);

        CosmosQueryRequestOptions optionsWithMaxIntAsMaxItemCount =
            new CosmosQueryRequestOptions().setMaxBufferedItemCount(Integer.MAX_VALUE);

        return new Object[][] {
            //options, top, pageSize, expectedPreFetchCount
            { optionsWithBufferSizeTenThousand, -1, 1_000, 10 },  // top ignored
            { optionsWithBufferSizeTenThousandAndDifferentMaxItemCount, -1, 1_000, 10 }, // explicit pageSize wins
            { optionsWithBufferSizeTenThousand, 0, 1_000, 10 },   // top ignored
            { optionsWithBufferSizeTenThousand, 500, 1_000, 20 }, // effective page size is top
            { optionsWithBufferSizeTenThousand, 100, 1_000, 32 }, // effective page size is top - should result in 100
                                                                  // but max prefetch count is 32
            { optionsWithBufferSizeTenThousand, -1, -1, 32 },     // effective pageSize is at least 1
            { optionsWithBufferSizeTenThousand, -1, 0, 32 },      // effective pageSize is at least 1
            { optionsWithBufferSizeTenThousand, -1, 20_000, 1 },  // at least 1 page is buffered even when
                                                                  // maxBufferedItemCount < maxItemCount
            { optionsWithMaxIntAsMaxItemCount, -1, Integer.MAX_VALUE, 1 },  // Exactly 1 page is buffered when
                                                                            // maxBufferedItemCount == maxItemCount
        };
    }
}
