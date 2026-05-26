// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentClientRetryPolicy;
import com.azure.cosmos.implementation.DocumentServiceRequestContext;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.IRetryPolicyFactory;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.faultinjection.FaultInjectionRequestContext;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.perPartitionCircuitBreaker.GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ChangeFeedFetcher} covering the {@code emptyPagesAllowed} behavior change
 * introduced for IcM 51000001033272.
 *
 * <p>The two behaviors locked in here are:
 * <ol>
 *   <li>{@code isFullyDrained} consults only the continuation (no {@code noChanges} short-circuit),
 *       which is what allows {@code emptyPagesAllowed=true} to surface empty pages without first
 *       having to call {@code reEnableShouldFetchMoreForRetry()} to undo a base-class decision.</li>
 *   <li>When {@code emptyPagesAllowed=true}, {@code nextPageInternal} returns
 *       {@code Mono.just(noChangesResponse)} instead of {@code Mono.empty()} so the empty pages
 *       bubble up to the caller (Spark connector) where the per-page end-to-end timeout applies to
 *       each individual page rather than being exceeded by serial empty-page drains.</li>
 * </ol>
 */
public class ChangeFeedFetcherEmptyPagesTest {

    @Test(groups = { "unit" })
    public void isFullyDrained_noChangesResponseWithUnfinishedContinuation_returnsFalse() throws Exception {
        // GIVEN a ChangeFeedFetcher and a noChanges response with continuation not yet done
        FeedRangeContinuation continuation = mock(FeedRangeContinuation.class);
        when(continuation.isDone()).thenReturn(false);
        ChangeFeedFetcher<Document> fetcher = newFetcher(continuation, /* emptyPagesAllowed */ true);

        FeedResponse<Document> noChangesResponse = changeFeedNoChanges("token-A");

        // WHEN
        boolean drained = invokeIsFullyDrained(fetcher, noChangesResponse);

        // THEN: the previous implementation returned true here (and relied on
        // reEnableShouldFetchMoreForRetry() in nextPageInternal to undo it). The new
        // implementation consults only the continuation.
        assertThat(drained).isFalse();
    }

    @Test(groups = { "unit" })
    public void isFullyDrained_noChangesResponseWithFinishedContinuation_returnsTrue() throws Exception {
        FeedRangeContinuation continuation = mock(FeedRangeContinuation.class);
        when(continuation.isDone()).thenReturn(true);
        ChangeFeedFetcher<Document> fetcher = newFetcher(continuation, /* emptyPagesAllowed */ true);

        FeedResponse<Document> noChangesResponse = changeFeedNoChanges("token-B");

        assertThat(invokeIsFullyDrained(fetcher, noChangesResponse)).isTrue();
    }

    @Test(groups = { "unit" })
    public void isFullyDrained_realResponseWithFinishedContinuation_returnsTrue() throws Exception {
        FeedRangeContinuation continuation = mock(FeedRangeContinuation.class);
        when(continuation.isDone()).thenReturn(true);
        ChangeFeedFetcher<Document> fetcher = newFetcher(continuation, /* emptyPagesAllowed */ false);

        FeedResponse<Document> dataResponse = changeFeedDataPage("token-C", new Document());

        assertThat(invokeIsFullyDrained(fetcher, dataResponse)).isTrue();
    }

    @Test(groups = { "unit" })
    public void isFullyDrained_noChangesResponseWithEmptyPagesAllowedFalse_returnsTrue() throws Exception {
        // Regression test: with the default emptyPagesAllowed=false the noChanges short-circuit
        // MUST stay in place. Otherwise any non-Spark caller that drains the change feed flux
        // (e.g. queryChangeFeed(...).byPage().toIterable().iterator()) would loop forever,
        // because FeedRangeCompositeContinuationImpl.isDone() returns
        // compositeContinuationTokens.size()==0, which is permanently false for incremental
        // change feed (moveToNextToken rotates the deque, never shrinks it). The
        // NO_RETRY result from handleChangeFeedNotModified is the only termination signal,
        // and Paginator only honors it via Fetcher.shouldFetchMore() being flipped by
        // isFullyDrained=true on this noChanges page.
        FeedRangeContinuation continuation = mock(FeedRangeContinuation.class);
        when(continuation.isDone()).thenReturn(false);
        ChangeFeedFetcher<Document> fetcher = newFetcher(continuation, /* emptyPagesAllowed */ false);

        FeedResponse<Document> noChangesResponse = changeFeedNoChanges("token-D");

        assertThat(invokeIsFullyDrained(fetcher, noChangesResponse))
            .describedAs("With emptyPagesAllowed=false the noChanges short-circuit must remain to terminate iteration")
            .isTrue();
    }

    @Test(groups = { "unit" })
    public void nextPage_emptyPagesAllowedTrue_surfacesNoChangesPagesIndividually() {
        // Scenario: 3 consecutive noChanges (304) pages from the same sub-feedRange,
        // followed by a real data page. With emptyPagesAllowed=true, each of the 4
        // physical responses must surface as its own Mono emission so the Spark
        // iterator can drain them under the per-page end-to-end timeout window.
        FeedRangeContinuation continuation = mock(FeedRangeContinuation.class);
        when(continuation.isDone()).thenReturn(false);
        // handleChangeFeedNotModified returns RETRY_NOW only for actual noChanges responses
        // (matching the real FeedRangeCompositeContinuationImpl behavior).
        when(continuation.handleChangeFeedNotModified(any())).thenAnswer(invocation -> {
            FeedResponse<?> rsp = invocation.getArgument(0);
            return ModelBridgeInternal.noChanges(rsp) ? ShouldRetryResult.RETRY_NOW : ShouldRetryResult.noRetry();
        });

        FeedResponse<Document> noChanges1 = changeFeedNoChanges("t1");
        FeedResponse<Document> noChanges2 = changeFeedNoChanges("t2");
        FeedResponse<Document> noChanges3 = changeFeedNoChanges("t3");
        FeedResponse<Document> data       = changeFeedDataPage("t4", new Document());

        FeedResponse<Document>[] script = new FeedResponse[] { noChanges1, noChanges2, noChanges3, data };
        AtomicInteger callIndex = new AtomicInteger();
        Function<RxDocumentServiceRequest, Mono<FeedResponse<Document>>> executeFunc =
            req -> Mono.just(script[callIndex.getAndIncrement()]);

        ChangeFeedFetcher<Document> fetcher =
            newFetcherWithExecuteFunc(continuation, /* emptyPagesAllowed */ true, executeFunc);

        // Drive the fetcher across 4 nextPage() invocations and assert each one surfaces.
        StepVerifier.create(fetcher.nextPage()).expectNextMatches(r -> r == noChanges1).verifyComplete();
        StepVerifier.create(fetcher.nextPage()).expectNextMatches(r -> r == noChanges2).verifyComplete();
        StepVerifier.create(fetcher.nextPage()).expectNextMatches(r -> r == noChanges3).verifyComplete();
        StepVerifier.create(fetcher.nextPage()).expectNextMatches(r -> r == data).verifyComplete();

        assertThat(callIndex.get()).describedAs("executeFunc should have been called once per surfaced page").isEqualTo(4);
    }

    @Test(groups = { "unit" })
    public void nextPage_emptyPagesAllowedFalse_swallowsNoChangesPagesUntilData() {
        // Same scenario, but with the default emptyPagesAllowed=false. The
        // 3 noChanges responses should be swallowed via repeatWhenEmpty and
        // only the data page should surface from a SINGLE nextPage() call.
        FeedRangeContinuation continuation = mock(FeedRangeContinuation.class);
        when(continuation.isDone()).thenReturn(false);
        when(continuation.handleChangeFeedNotModified(any())).thenAnswer(invocation -> {
            FeedResponse<?> rsp = invocation.getArgument(0);
            return ModelBridgeInternal.noChanges(rsp) ? ShouldRetryResult.RETRY_NOW : ShouldRetryResult.noRetry();
        });

        FeedResponse<Document> noChanges1 = changeFeedNoChanges("t1");
        FeedResponse<Document> noChanges2 = changeFeedNoChanges("t2");
        FeedResponse<Document> noChanges3 = changeFeedNoChanges("t3");
        FeedResponse<Document> data       = changeFeedDataPage("t4", new Document());

        FeedResponse<Document>[] script = new FeedResponse[] { noChanges1, noChanges2, noChanges3, data };
        AtomicInteger callIndex = new AtomicInteger();
        Function<RxDocumentServiceRequest, Mono<FeedResponse<Document>>> executeFunc =
            req -> Mono.just(script[callIndex.getAndIncrement()]);

        ChangeFeedFetcher<Document> fetcher =
            newFetcherWithExecuteFunc(continuation, /* emptyPagesAllowed */ false, executeFunc);

        // A single nextPage() should internally drain all 3 noChanges responses and
        // emit the data response.
        StepVerifier.create(fetcher.nextPage()).expectNextMatches(r -> r == data).verifyComplete();

        assertThat(callIndex.get()).describedAs("executeFunc should be called once per physical fetch").isEqualTo(4);
    }

    // ---- helpers ----

    private static FeedResponse<Document> changeFeedNoChanges(String continuationToken) {
        return FeedResponseBuilder.changeFeedResponseBuilder(Document.class)
            .withContinuationToken(continuationToken)
            .lastChangeFeedPage()
            .build();
    }

    private static FeedResponse<Document> changeFeedDataPage(String continuationToken, Document... docs) {
        return FeedResponseBuilder.changeFeedResponseBuilder(Document.class)
            .withContinuationToken(continuationToken)
            .withResults(docs)
            .build();
    }

    private static ChangeFeedFetcher<Document> newFetcher(FeedRangeContinuation continuation, boolean emptyPagesAllowed) {
        return newFetcherWithExecuteFunc(continuation, emptyPagesAllowed, req -> Mono.empty());
    }

    private static ChangeFeedFetcher<Document> newFetcherWithExecuteFunc(
        FeedRangeContinuation continuation,
        boolean emptyPagesAllowed,
        Function<RxDocumentServiceRequest, Mono<FeedResponse<Document>>> executeFunc) {

        RxDocumentClientImpl client = mock(RxDocumentClientImpl.class);
        IRetryPolicyFactory resetSessionTokenRetryPolicy = mock(IRetryPolicyFactory.class);
        DocumentClientRetryPolicy noOpRetryPolicy = mock(DocumentClientRetryPolicy.class);
        when(noOpRetryPolicy.shouldRetry(any())).thenReturn(Mono.just(ShouldRetryResult.noRetry()));
        when(resetSessionTokenRetryPolicy.getRequestPolicy(any())).thenReturn(noOpRetryPolicy);
        when(client.getResetSessionTokenRetryPolicy()).thenReturn(resetSessionTokenRetryPolicy);
        ChangeFeedState changeFeedState = mock(ChangeFeedState.class);
        FeedRangeInternal feedRange = FeedRangeEpkImpl.forFullRange();
        when(changeFeedState.getContinuation()).thenReturn(continuation);
        when(changeFeedState.getFeedRange()).thenReturn(feedRange);
        doNothing().when(changeFeedState).populateRequest(any(RxDocumentServiceRequest.class), anyInt());

        Supplier<RxDocumentServiceRequest> createRequestFunc = ChangeFeedFetcherEmptyPagesTest::stubRequest;

        Map<String, Object> requestOptionProperties = new HashMap<>();
        GlobalEndpointManager gem = mock(GlobalEndpointManager.class);
        GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker gpe =
            mock(GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.class);
        when(gpe.isPerPartitionLevelCircuitBreakingApplicable(any())).thenReturn(false);

        return new ChangeFeedFetcher<>(
            client,
            createRequestFunc,
            executeFunc,
            changeFeedState,
            requestOptionProperties,
            /* top */ -1,
            /* maxItemCount */ 100,
            /* isSplitHandlingDisabled */ true,
            /* completeAfterAllCurrentChangesRetrieved */ false,
            emptyPagesAllowed,
            /* endLSN */ null,
            /* operationContext */ null,
            gem,
            gpe,
            /* diagnosticsClientContext */ null);
    }

    private static RxDocumentServiceRequest stubRequest() {
        RxDocumentServiceRequest request = mock(RxDocumentServiceRequest.class);
        when(request.getIsNameBased()).thenReturn(true);
        when(request.getResourceAddress()).thenReturn("dbs/db1/colls/coll1");
        when(request.getOperationType()).thenReturn(OperationType.ReadFeed);
        when(request.getResourceType()).thenReturn(ResourceType.Document);
        // requestContext and faultInjectionRequestContext are public fields on the real class;
        // direct assignment on the mock works (Mockito only intercepts method calls).
        request.requestContext = new DocumentServiceRequestContext();
        request.faultInjectionRequestContext = new FaultInjectionRequestContext();
        return request;
    }

    private static boolean invokeIsFullyDrained(
        ChangeFeedFetcher<Document> fetcher,
        FeedResponse<Document> response) throws Exception {

        Method m = Fetcher.class.getDeclaredMethod("isFullyDrained", boolean.class, FeedResponse.class);
        m.setAccessible(true);
        return (Boolean) m.invoke(fetcher, true, response);
    }
}
