// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.ChangeFeedOptions;
import com.azure.cosmos.models.QueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.models.ModelBridgeInternal;
import io.reactivex.subscribers.TestSubscriber;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class FetcherTest {

    @DataProvider(name = "queryParams")
    public static Object[][] queryParamProvider() {

        QueryRequestOptions options1 = new QueryRequestOptions();
        // initial continuation token
        ModelBridgeInternal.setQueryRequestOptionsContinuationTokenAndMaxItemCount(options1,"cp-init",100);
        int top1 = -1; // no top

        // no continuation token
        QueryRequestOptions options2 = new QueryRequestOptions();
        ModelBridgeInternal.setQueryRequestOptionsMaxItemCount(options2, 100);
        int top2 = -1; // no top

        // top more than max item count
        QueryRequestOptions options3 = new QueryRequestOptions();
        ModelBridgeInternal.setQueryRequestOptionsMaxItemCount(options3, 100);
        int top3 = 200;

        // top less than max item count
        QueryRequestOptions options4 = new QueryRequestOptions();
        ModelBridgeInternal.setQueryRequestOptionsMaxItemCount(options4, 100);
        int top4 = 20;

        return new Object[][] {
                { options1, top1 },
                { options2, top2 },
                { options3, top3 },
                { options4, top4 }};
    }

    @Test(groups = { "unit" }, dataProvider = "queryParams")
    public void query(QueryRequestOptions options, int top) {

        FeedResponse<Document> fp1 = FeedResponseBuilder.queryFeedResponseBuilder(Document.class)
                .withContinuationToken("cp1")
                .withResults(new Document(), new Document(), new Document())
                .build();

        FeedResponse<Document> fp2 = FeedResponseBuilder.queryFeedResponseBuilder(Document.class)
                .withContinuationToken(null)
                .withResults(new Document())
                .build();

        List<FeedResponse<Document>> feedResponseList = Arrays.asList(fp1, fp2);

        AtomicInteger totalResultsReceived = new AtomicInteger(0);

        AtomicInteger requestIndex = new AtomicInteger(0);

        BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc = (token, maxItemCount) -> {
            assertThat(maxItemCount).describedAs("max item count").isEqualTo(
                    getExpectedMaxItemCountInRequest(options, top, feedResponseList, requestIndex.get()));
            assertThat(token).describedAs("continuation token").isEqualTo(
                    getExpectedContinuationTokenInRequest(
                        ModelBridgeInternal.getRequestContinuationFromQueryRequestOptions(options), feedResponseList, requestIndex.get()));
            requestIndex.getAndIncrement();

            return mock(RxDocumentServiceRequest.class);
        };

        AtomicInteger executeIndex = new AtomicInteger(0);

        Function<RxDocumentServiceRequest, Mono<FeedResponse<Document>>> executeFunc = request ->  {
                FeedResponse<Document> rsp = feedResponseList.get(executeIndex.getAndIncrement());
                totalResultsReceived.addAndGet(rsp.getResults().size());
                return Mono.just(rsp);
        };

        Fetcher<Document> fetcher =
                new Fetcher<>(createRequestFunc, executeFunc, ModelBridgeInternal.getRequestContinuationFromQueryRequestOptions(options), false, top,
                        ModelBridgeInternal.getMaxItemCountFromQueryRequestOptions(options));

        validateFetcher(fetcher, options, top, feedResponseList);
    }

    private void validateFetcher(Fetcher<Document> fetcher,
                                 QueryRequestOptions options,
                                 int top,
                                 List<FeedResponse<Document>> feedResponseList) {

        int totalNumberOfDocs = 0;

        int index = 0;
        while(index < feedResponseList.size()) {
            assertThat(fetcher.shouldFetchMore()).describedAs("should fetch more pages").isTrue();
            totalNumberOfDocs += validate(fetcher.nextPage()).getResults().size();

            if ((top != -1) && (totalNumberOfDocs >= top)) {
                break;
            }
            index++;
        }
        assertThat(fetcher.shouldFetchMore()).describedAs("should not fetch more pages").isFalse();
    }

    @Test(groups = { "unit" })
    public void changeFeed() {

        ChangeFeedOptions options = new ChangeFeedOptions();
        options.setMaxItemCount(100);

        boolean isChangeFeed = true;
        int top = -1;

        FeedResponse<Document> fp1 = FeedResponseBuilder.changeFeedResponseBuilder(Document.class)
                .withContinuationToken("cp1")
                .withResults(new Document())
                .build();

        FeedResponse<Document> fp2 = FeedResponseBuilder.changeFeedResponseBuilder(Document.class)
                .withContinuationToken("cp2")
                .lastChangeFeedPage()
                .build();

        List<FeedResponse<Document>> feedResponseList = Arrays.asList(fp1, fp2);

        AtomicInteger requestIndex = new AtomicInteger(0);

        BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc = (token, maxItemCount) -> {
            assertThat(maxItemCount).describedAs("max getItem count").isEqualTo(options.getMaxItemCount());
            assertThat(token).describedAs("continuation token").isEqualTo(
                    getExpectedContinuationTokenInRequest(options.getRequestContinuation(), feedResponseList, requestIndex.getAndIncrement()));

            return mock(RxDocumentServiceRequest.class);
        };

        AtomicInteger executeIndex = new AtomicInteger(0);

        Function<RxDocumentServiceRequest, Mono<FeedResponse<Document>>> executeFunc = request -> {
            return Mono.just(feedResponseList.get(executeIndex.getAndIncrement()));
        };

        Fetcher<Document> fetcher =
                new Fetcher<>(createRequestFunc, executeFunc, options.getRequestContinuation(), isChangeFeed, top,
                        options.getMaxItemCount());

        validateFetcher(fetcher, options, feedResponseList);
    }

    private void validateFetcher(Fetcher<Document> fetcher,
                                 ChangeFeedOptions options,
                                 List<FeedResponse<Document>> feedResponseList) {


        for(FeedResponse<Document> change: feedResponseList) {
            assertThat(fetcher.shouldFetchMore()).describedAs("should fetch more pages").isTrue();
            validate(fetcher.nextPage());
        }

        assertThat(fetcher.shouldFetchMore()).describedAs("should not fetch more pages").isFalse();
    }

    private FeedResponse<Document> validate(Mono<FeedResponse<Document>> page) {
        TestSubscriber<FeedResponse<Document>> subscriber = new TestSubscriber<>();
        page.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertComplete();
        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        return subscriber.values().get(0);
    }

    private String getExpectedContinuationTokenInRequest(String continuationToken,
                                                         List<FeedResponse<Document>> feedResponseList,
                                                         int requestIndex) {
        if (requestIndex == 0) {
            return continuationToken;
        }

        return feedResponseList.get(requestIndex - 1).getContinuationToken();
    }

    private int getExpectedMaxItemCountInRequest(QueryRequestOptions options,
                                                 int top,
                                                 List<FeedResponse<Document>> feedResponseList,
                                                 int requestIndex) {
        if (top == -1) {
            return ModelBridgeInternal.getMaxItemCountFromQueryRequestOptions(options);
        }

        int numberOfReceivedItemsSoFar  =
                feedResponseList.subList(0, requestIndex).stream().mapToInt(rsp -> rsp.getResults().size()).sum();

        return Math.min(top - numberOfReceivedItemsSoFar, ModelBridgeInternal.getMaxItemCountFromQueryRequestOptions(options));
    }
}
