// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.RequestChargeTracker;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ParallelDocumentQueryExecutionContextTest {
    @DataProvider(name = "continuations")
    public Object[][] continuations() {
        return new Object[][] {
            { "backend-token", "80", "backend-token", "", "80" },
            { null, "80", null, "80", "FF" },
            { null, "40", null, "40", "80" }, // A child range exhausted after a split.
            { null, "FF", null, null, null }
        };
    }

    @Test(groups = { "unit" }, dataProvider = "continuations")
    @SuppressWarnings("unchecked")
    public void zeroPrefetchEmitsWithoutFollowingPage(String backendToken, String rangeEnd,
                                                     String expectedBackendToken, String expectedMin, String expectedMax) {
        DocumentProducer<Document> producer = mock(DocumentProducer.class);
        FeedRangeEpkImpl range = new FeedRangeEpkImpl(new Range<>("", rangeEnd, true, false));
        FeedResponse<Document> page = FeedResponseBuilder.queryFeedResponseBuilder(Document.class)
            .withContinuationToken(backendToken).withResults(new Document()).build();
        DocumentProducer<Document>.DocumentProducerFeedResponse response =
            producer.new DocumentProducerFeedResponse(page, range);
        AtomicInteger followingPages = new AtomicInteger();
        Flux<DocumentProducer<Document>.DocumentProducerFeedResponse> source = Flux.just(response)
            .concatWith(Flux.defer(() -> {
                followingPages.incrementAndGet();
                return Flux.never();
            }));
        ParallelDocumentQueryExecutionContext.EmptyPagesFilterTransformer<Document> transformer =
            new ParallelDocumentQueryExecutionContext.EmptyPagesFilterTransformer<>(new RequestChargeTracker(),
                new CosmosQueryRequestOptions().setMaxPrefetchPageCount(0), UUID.randomUUID(), () -> "test",
                Arrays.asList(new FeedRangeEpkImpl(new Range<>("", "80", true, false)),
                    new FeedRangeEpkImpl(new Range<>("80", "FF", true, false))));
        StepVerifier.create(source.transformDeferred(transformer).next())
            .assertNext(result -> {
                assertThat(result.getResults()).hasSize(1);
                String expected = expectedMin == null ? null : new CompositeContinuationToken(expectedBackendToken,
                    new Range<>(expectedMin, expectedMax, true, false)).toJson();
                assertThat(result.getContinuationToken()).isEqualTo(expected);
            })
            .expectComplete().verify(Duration.ofSeconds(5));
        assertThat(followingPages.get()).isZero();
    }
}
