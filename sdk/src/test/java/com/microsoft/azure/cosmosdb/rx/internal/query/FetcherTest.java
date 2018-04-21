/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.rx.internal.query;

import com.microsoft.azure.cosmosdb.ChangeFeedOptions;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedOptionsBase;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.observers.TestSubscriber;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class FetcherTest {

    @DataProvider(name = "queryParams")
    public static Object[][] queryParamProvider() {

        FeedOptions options1 = new FeedOptions();
        options1.setMaxItemCount(100);
        options1.setRequestContinuation("cp-init"); // initial continuation token
        int top1 = -1; // no top

        // no continuation token
        FeedOptions options2 = new FeedOptions();
        options2.setMaxItemCount(100);
        int top2 = -1; // no top

        // top more than max item count
        FeedOptions options3 = new FeedOptions();
        options3.setMaxItemCount(100);
        int top3 = 200;

        // top less than max item count
        FeedOptions options4 = new FeedOptions();
        options4.setMaxItemCount(100);
        int top4 = 20;

        return new Object[][] {
                { options1, top1 },
                { options2, top2 },
                { options3, top3 },
                { options4, top4 }};
    }

    @Test(dataProvider = "queryParams")
    public void query(FeedOptions options, int top) {

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

        Func2<String, Integer, RxDocumentServiceRequest> createRequestFunc = (token, maxItemCount) -> {
            assertThat(maxItemCount).describedAs("max item count").isEqualTo(
                    getExpectedMaxItemCountInRequest(options, top, feedResponseList, requestIndex.get()));
            assertThat(token).describedAs("continuation token").isEqualTo(
                    getExpectedContinuationTokenInRequest(options, feedResponseList, requestIndex.get()));
            requestIndex.getAndIncrement();

            return mock(RxDocumentServiceRequest.class);
        };

        AtomicInteger executeIndex = new AtomicInteger(0);

        Func1<RxDocumentServiceRequest, Observable<FeedResponse<Document>>> executeFunc = request ->  {
                FeedResponse<Document> rsp = feedResponseList.get(executeIndex.getAndIncrement());
                totalResultsReceived.addAndGet(rsp.getResults().size());
                return Observable.just(rsp);
        };

        Fetcher<Document> fetcher =
                new Fetcher<>(createRequestFunc, executeFunc, options, false, top,
                        options.getMaxItemCount());

        validateFetcher(fetcher, options, top, feedResponseList);
    }

    private void validateFetcher(Fetcher<Document> fetcher,
                                 FeedOptions options,
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

    @Test
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

        Func2<String, Integer, RxDocumentServiceRequest> createRequestFunc = (token, maxItemCount) -> {
            assertThat(maxItemCount).describedAs("max item count").isEqualTo(options.getMaxItemCount());
            assertThat(token).describedAs("continuation token").isEqualTo(
                    getExpectedContinuationTokenInRequest(options, feedResponseList, requestIndex.getAndIncrement()));

            return mock(RxDocumentServiceRequest.class);
        };

        AtomicInteger executeIndex = new AtomicInteger(0);

        Func1<RxDocumentServiceRequest, Observable<FeedResponse<Document>>> executeFunc = request -> {
            return Observable.just(feedResponseList.get(executeIndex.getAndIncrement()));
        };

        Fetcher<Document> fetcher =
                new Fetcher<>(createRequestFunc, executeFunc, options, isChangeFeed, top,
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

    private FeedResponse<Document> validate(Observable<FeedResponse<Document>> page) {
        TestSubscriber<FeedResponse<Document>> subscriber = new TestSubscriber();
        page.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertCompleted();
        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        return subscriber.getOnNextEvents().get(0);
    }

    private String getExpectedContinuationTokenInRequest(FeedOptionsBase options,
                                                         List<FeedResponse<Document>> feedResponseList,
                                                         int requestIndex) {
        if (requestIndex == 0) {
            return options.getRequestContinuation();
        }

        return feedResponseList.get(requestIndex - 1).getResponseContinuation();
    }

    private int getExpectedMaxItemCountInRequest(FeedOptionsBase options,
                                                 int top,
                                                 List<FeedResponse<Document>> feedResponseList,
                                                 int requestIndex) {
        if (top == -1) {
            return options.getMaxItemCount();
        }

        int numberOfReceivedItemsSoFar  =
                feedResponseList.subList(0, requestIndex).stream().mapToInt(rsp -> rsp.getResults().size()).sum();

        return Math.min(top - numberOfReceivedItemsSoFar, options.getMaxItemCount());
    }
}
