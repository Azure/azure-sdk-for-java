// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.TestUtils;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.directconnectivity.rntbd.ChangeFeedResponseMappingTest;
import com.azure.cosmos.implementation.perPartitionCircuitBreaker.GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker;
import com.azure.cosmos.models.FeedResponse;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChangeFeedQueryFilteringFetcherTest {
    @DataProvider(name = "modes")
    public Object[][] modes() {
        return new Object[][] { { ChangeFeedMode.INCREMENTAL }, { ChangeFeedMode.FULL_FIDELITY } };
    }

    @Test(groups = "unit", dataProvider = "modes")
    public void consecutiveEmptyPhysicalPagesAdvanceUntilNotModified(ChangeFeedMode mode) {
        AtomicInteger physicalPage = new AtomicInteger();
        ChangeFeedFetcher<Document> fetcher = createFetcher(mode, request -> {
            int page = physicalPage.incrementAndGet();
            FeedResponseBuilder<Document> builder = FeedResponseBuilder.changeFeedResponseBuilder(Document.class)
                .withResults(Collections.emptyList()).withContinuationToken("\"" + page + "\"");
            return Mono.just((page == 101 ? builder.lastChangeFeedPage() : builder).build());
        });
        for (int page = 1; page <= 100; page++) {
            assertThat(fetcher.shouldFetchMore()).isTrue();
            FeedResponse<Document> response = fetcher.nextPage().block();
            assertThat(response.getResults()).isEmpty();
            ChangeFeedState state = ChangeFeedState.fromString(response.getContinuationToken());
            assertThat(state.getContinuation().getCurrentContinuationToken().getToken())
                .isEqualTo("\"" + page + "\"");
            assertThat(fetcher.shouldFetchMore()).isTrue();
        }
        fetcher.nextPage().block();
        assertThat(fetcher.shouldFetchMore()).isFalse();
        assertThat(physicalPage).hasValue(101);
    }

    @Test(groups = "unit", dataProvider = "modes")
    public void repeatedEmptyPageWithoutPhysicalProgressFailsInsteadOfLooping(ChangeFeedMode mode) {
        AtomicInteger calls = new AtomicInteger();
        ChangeFeedFetcher<Document> fetcher = createFetcher(mode, request -> {
            calls.incrementAndGet();
            return Mono.just(FeedResponseBuilder.changeFeedResponseBuilder(Document.class)
                .withResults(Collections.emptyList()).withContinuationToken("\"1\"").build());
        });
        fetcher.nextPage().block();
        StepVerifier.create(fetcher.nextPage()).expectErrorMatches(error -> error instanceof IllegalStateException
            && error.getMessage().contains("did not advance")).verify();
        assertThat(calls).hasValue(2);
    }

    @Test(groups = "unit")
    public void validationErrorsAreNotConvertedToEmptyPages() {
        BadRequestException failure = new BadRequestException("Unsupported JOIN");
        ChangeFeedFetcher<Document> fetcher = createFetcher(ChangeFeedMode.INCREMENTAL, request -> Mono.error(failure));
        StepVerifier.create(fetcher.nextPage()).expectErrorMatches(error -> error == failure).verify();
    }

    @Test(groups = "unit", dataProvider = "modes")
    public void decodedDirectResponseAdvancesPhysicalProgressUntilNotModified(ChangeFeedMode mode) {
        AtomicInteger calls = new AtomicInteger();
        ChangeFeedFetcher<Document> fetcher = createFetcher(mode, request -> {
            int call = calls.incrementAndGet();
            return Mono.just(ChangeFeedResponseMappingTest.mappedChangeFeed(call == 3 ? 304 : 200,
                "\"" + call + "\"", "not-a-physical-token", call == 1));
        });
        for (int page = 1; page <= 3; page++) {
            FeedResponse<Document> response = fetcher.nextPage().block();
            ChangeFeedState state = ChangeFeedState.fromString(response.getContinuationToken());
            assertThat(state.getContinuation().getCurrentContinuationToken().getToken()).isEqualTo("\"" + page + "\"");
        }
        assertThat(fetcher.shouldFetchMore()).isFalse();
    }

    @Test(groups = "unit", dataProvider = "modes")
    public void missingPhysicalContinuationFailsForAllDecodedDirectResponseShapesWithoutUpdatingState(
        ChangeFeedMode mode) {
        for (int status : new int[] { 200, 304 }) {
            for (boolean hasItems : status == 304 ? new boolean[] { false } : new boolean[] { false, true }) {
                for (String queryContinuation : new String[] { null, "not-a-physical-token" }) {
                    ChangeFeedStateV1 state = new ChangeFeedStateV1("rid", FeedRangeEpkImpl.forFullRange(), mode,
                        ChangeFeedStartFromInternal.createFromBeginning(), null);
                    AtomicInteger calls = new AtomicInteger();
                    ChangeFeedFetcher<Document> fetcher = createFetcher(state, request -> Mono.just(
                        calls.incrementAndGet() == 1
                            ? ChangeFeedResponseMappingTest.mappedChangeFeed(200, "\"1\"", null, true)
                            : ChangeFeedResponseMappingTest.mappedChangeFeed(status, null, queryContinuation, hasItems)));
                    fetcher.nextPage().block();
                    String previousState = state.toJson();
                    StepVerifier.create(fetcher.nextPage())
                        .expectErrorMatches(error -> error instanceof IllegalStateException
                            && error.getMessage().contains("missing the required ETag physical continuation"))
                        .verify();
                    assertThat(state.toJson()).isEqualTo(previousState);
                    assertThat(calls).hasValue(2);
                }
            }
        }
    }

    @Test(groups = "unit")
    public void cancellationReachesThePhysicalRead() {
        AtomicBoolean cancelled = new AtomicBoolean();
        ChangeFeedFetcher<Document> fetcher = createFetcher(ChangeFeedMode.INCREMENTAL,
            request -> Mono.<FeedResponse<Document>>never().doOnCancel(() -> cancelled.set(true)));
        StepVerifier.create(fetcher.nextPage()).thenAwait(java.time.Duration.ofMillis(10)).thenCancel().verify();
        assertThat(cancelled).isTrue();
    }

    private ChangeFeedFetcher<Document> createFetcher(ChangeFeedMode mode,
        Function<RxDocumentServiceRequest, Mono<FeedResponse<Document>>> execute) {
        return createFetcher(new ChangeFeedStateV1("rid", FeedRangeEpkImpl.forFullRange(), mode,
            ChangeFeedStartFromInternal.createFromBeginning(), null), execute);
    }

    private ChangeFeedFetcher<Document> createFetcher(ChangeFeedStateV1 state,
        Function<RxDocumentServiceRequest, Mono<FeedResponse<Document>>> execute) {
        RxDocumentClientImpl client = mock(RxDocumentClientImpl.class, Mockito.RETURNS_DEEP_STUBS);
        when(client.getResetSessionTokenRetryPolicy().getRequestPolicy(any())).thenReturn(null);
        return new ChangeFeedFetcher<>(client,
            () -> {
                RxDocumentServiceRequest request = RxDocumentServiceRequest.create(TestUtils.mockDiagnosticsClientContext(),
                    OperationType.Query, ResourceType.Document, "dbs/db/colls/coll/docs", new HashMap<>(), (Object) null);
                request.setEffectiveRange(FeedRangeEpkImpl.forFullRange().getRange());
                return request;
            },
            execute, state, null, -1, 100, true, false, null, null,
            mock(GlobalEndpointManager.class), mock(GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.class),
            TestUtils.mockDiagnosticsClientContext());
    }
}
