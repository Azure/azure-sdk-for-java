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
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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
    public void isFullyDrained_noChangesResponseWithUnfinishedContinuation_returnsFalse() {
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
    public void isFullyDrained_noChangesResponseWithFinishedContinuation_returnsTrue() {
        FeedRangeContinuation continuation = mock(FeedRangeContinuation.class);
        when(continuation.isDone()).thenReturn(true);
        ChangeFeedFetcher<Document> fetcher = newFetcher(continuation, /* emptyPagesAllowed */ true);

        FeedResponse<Document> noChangesResponse = changeFeedNoChanges("token-B");

        assertThat(invokeIsFullyDrained(fetcher, noChangesResponse)).isTrue();
    }

    @Test(groups = { "unit" })
    public void isFullyDrained_realResponseWithFinishedContinuation_returnsTrue() {
        FeedRangeContinuation continuation = mock(FeedRangeContinuation.class);
        when(continuation.isDone()).thenReturn(true);
        ChangeFeedFetcher<Document> fetcher = newFetcher(continuation, /* emptyPagesAllowed */ false);

        FeedResponse<Document> dataResponse = changeFeedDataPage("token-C", new Document());

        assertThat(invokeIsFullyDrained(fetcher, dataResponse)).isTrue();
    }

    @Test(groups = { "unit" })
    public void isFullyDrained_noChangesResponseWithEmptyPagesAllowedFalse_returnsTrue() {
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
        // The short-circuit must fire WITHOUT consulting the continuation; if a future
        // refactor accidentally drops the noChanges check and falls through to
        // continuation.isDone() (which is permanently false in incremental change feed),
        // this verify catches it as a loud failure rather than a hard-to-diagnose hang.
        Mockito.verify(continuation, Mockito.never()).isDone();
    }

    @Test(groups = { "unit" }, timeOut = 10_000)
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

    @Test(groups = { "unit" }, timeOut = 10_000)
    public void nextPage_emptyPagesAllowedTrueWithNoRetryOnNoChanges_terminatesIteration() {
        // Defense-in-depth: with emptyPagesAllowed=true, isFullyDrained() consults only
        // continuation.isDone() (permanently false in incremental change feed), so the
        // SDK's own termination signal would otherwise be lost. nextPageInternal must
        // explicitly disableShouldFetchMore() when handleChangeFeedNotModified returns
        // NO_RETRY on a noChanges page (single-partition case, multi-partition full
        // cycle complete, or the >4*(size+1) consecutive-304 defense).
        //
        // This test scripts: 3 noChanges with RETRY_NOW (mid-cycle), followed by a 4th
        // noChanges with NO_RETRY (terminal). All 4 must surface, and shouldFetchMore()
        // must be false after the terminal page so Paginator's outer loop stops.
        FeedRangeContinuation continuation = mock(FeedRangeContinuation.class);
        when(continuation.isDone()).thenReturn(false);

        FeedResponse<Document> mid1 = changeFeedNoChanges("t1");
        FeedResponse<Document> mid2 = changeFeedNoChanges("t2");
        FeedResponse<Document> mid3 = changeFeedNoChanges("t3");
        FeedResponse<Document> terminal = changeFeedNoChanges("t4");

        // The continuation distinguishes terminal from mid-cycle by reference identity.
        when(continuation.handleChangeFeedNotModified(any())).thenAnswer(invocation -> {
            FeedResponse<?> rsp = invocation.getArgument(0);
            return rsp == terminal ? ShouldRetryResult.noRetry() : ShouldRetryResult.RETRY_NOW;
        });

        FeedResponse<Document>[] script = new FeedResponse[] { mid1, mid2, mid3, terminal };
        AtomicInteger callIndex = new AtomicInteger();
        Function<RxDocumentServiceRequest, Mono<FeedResponse<Document>>> executeFunc =
            req -> Mono.just(script[callIndex.getAndIncrement()]);

        ChangeFeedFetcher<Document> fetcher =
            newFetcherWithExecuteFunc(continuation, /* emptyPagesAllowed */ true, executeFunc);

        StepVerifier.create(fetcher.nextPage()).expectNextMatches(r -> r == mid1).verifyComplete();
        assertThat(fetcher.shouldFetchMore()).describedAs("after mid1").isTrue();
        StepVerifier.create(fetcher.nextPage()).expectNextMatches(r -> r == mid2).verifyComplete();
        assertThat(fetcher.shouldFetchMore()).describedAs("after mid2").isTrue();
        StepVerifier.create(fetcher.nextPage()).expectNextMatches(r -> r == mid3).verifyComplete();
        assertThat(fetcher.shouldFetchMore()).describedAs("after mid3").isTrue();
        StepVerifier.create(fetcher.nextPage()).expectNextMatches(r -> r == terminal).verifyComplete();
        assertThat(fetcher.shouldFetchMore())
            .describedAs("NO_RETRY on terminal noChanges page MUST stop Paginator from polling again")
            .isFalse();
        assertThat(callIndex.get())
            .describedAs("after NO_RETRY termination, executeFunc must NOT be called again")
            .isEqualTo(4);
    }

    @Test(groups = { "unit" }, timeOut = 10_000)
    public void nextPage_emptyPagesAllowedTrueWithDataPages_doesNotTerminate() {
        // Defense-in-depth contract guard: handleChangeFeedNotModified in the real
        // FeedRangeCompositeContinuationImpl returns NO_RETRY for EVERY non-noChanges
        // response (the early `if (!noChanges(r))` clause resets state and then falls
        // through to the final `return NO_RETRY`). The branch-2 termination logic in
        // ChangeFeedFetcher.nextPageInternal therefore MUST gate the
        // disableShouldFetchMore() call on `noChanges(r) && emptyPagesAllowed`; if a
        // future refactor drops the noChanges(r) guard, every data page would silently
        // truncate iteration after the first emission. This test pins that contract.
        FeedRangeContinuation continuation = mock(FeedRangeContinuation.class);
        when(continuation.isDone()).thenReturn(false);
        // Match real production behavior: NO_RETRY on data pages, RETRY_NOW on noChanges.
        when(continuation.handleChangeFeedNotModified(any())).thenAnswer(invocation -> {
            FeedResponse<?> rsp = invocation.getArgument(0);
            return ModelBridgeInternal.noChanges(rsp) ? ShouldRetryResult.RETRY_NOW : ShouldRetryResult.noRetry();
        });

        FeedResponse<Document> data1 = changeFeedDataPage("d1", new Document());
        FeedResponse<Document> data2 = changeFeedDataPage("d2", new Document());
        FeedResponse<Document> data3 = changeFeedDataPage("d3", new Document());

        FeedResponse<Document>[] script = new FeedResponse[] { data1, data2, data3 };
        AtomicInteger callIndex = new AtomicInteger();
        Function<RxDocumentServiceRequest, Mono<FeedResponse<Document>>> executeFunc =
            req -> Mono.just(script[callIndex.getAndIncrement()]);

        ChangeFeedFetcher<Document> fetcher =
            newFetcherWithExecuteFunc(continuation, /* emptyPagesAllowed */ true, executeFunc);

        StepVerifier.create(fetcher.nextPage()).expectNextMatches(r -> r == data1).verifyComplete();
        assertThat(fetcher.shouldFetchMore())
            .describedAs("data pages must NOT terminate iteration even when handleChangeFeedNotModified returns NO_RETRY")
            .isTrue();
        StepVerifier.create(fetcher.nextPage()).expectNextMatches(r -> r == data2).verifyComplete();
        assertThat(fetcher.shouldFetchMore()).describedAs("after data2").isTrue();
        StepVerifier.create(fetcher.nextPage()).expectNextMatches(r -> r == data3).verifyComplete();
        assertThat(fetcher.shouldFetchMore()).describedAs("after data3").isTrue();
    }

    @Test(groups = { "unit" }, timeOut = 10_000)
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

    @Test(groups = { "unit" }, timeOut = 10_000)
    public void nextPage_endLsnSet_emptyPagesAllowedTrue_surfacesNoChangesUntilHasFetchedAllChanges() {
        // Spark batch reads set BOTH endLSN AND emptyPagesAllowed=true, routing through
        // ChangeFeedFetcher.nextPageInternal branch 1 (the
        // `completeAfterAllCurrentChangesRetrieved || endLSN != null` path). That branch calls
        // surfaceOrSwallowNoChangesPage(r) on noChanges pages, then terminates via
        // hasFetchedAllChanges -> disableShouldFetchMore(). This test pins both halves of the
        // contract: empty pages surface to the caller (per-page timeout enforcement) AND
        // iteration eventually terminates when hasFetchedAllChanges returns true.
        FeedRangeContinuation continuation = mock(FeedRangeContinuation.class);
        when(continuation.isDone()).thenReturn(false);

        FeedResponse<Document> noChanges1 = changeFeedNoChanges("t1");
        FeedResponse<Document> noChanges2 = changeFeedNoChanges("t2");
        FeedResponse<Document> terminal = changeFeedNoChanges("t3");

        // hasFetchedAllChanges returns true only for the terminal page (reference identity).
        when(continuation.hasFetchedAllChanges(any(), any())).thenAnswer(invocation -> {
            FeedResponse<?> rsp = invocation.getArgument(0);
            return rsp == terminal;
        });

        FeedResponse<Document>[] script = new FeedResponse[] { noChanges1, noChanges2, terminal };
        AtomicInteger callIndex = new AtomicInteger();
        Function<RxDocumentServiceRequest, Mono<FeedResponse<Document>>> executeFunc =
            req -> Mono.just(script[callIndex.getAndIncrement()]);

        ChangeFeedFetcher<Document> fetcher =
            newFetcherWithExecuteFunc(continuation, /* emptyPagesAllowed */ true, /* endLSN */ 999L, executeFunc);

        StepVerifier.create(fetcher.nextPage()).expectNextMatches(r -> r == noChanges1).verifyComplete();
        assertThat(fetcher.shouldFetchMore()).describedAs("after noChanges1, mid-cycle").isTrue();
        StepVerifier.create(fetcher.nextPage()).expectNextMatches(r -> r == noChanges2).verifyComplete();
        assertThat(fetcher.shouldFetchMore()).describedAs("after noChanges2, mid-cycle").isTrue();
        StepVerifier.create(fetcher.nextPage()).expectNextMatches(r -> r == terminal).verifyComplete();
        assertThat(fetcher.shouldFetchMore())
            .describedAs("hasFetchedAllChanges=true on terminal page MUST stop Paginator from polling again")
            .isFalse();
        assertThat(callIndex.get())
            .describedAs("after termination, executeFunc must NOT be called again")
            .isEqualTo(3);
    }

    @Test(groups = { "unit" }, timeOut = 10_000)
    public void nextPage_endLsnSet_emptyPagesAllowedFalse_swallowsNoChangesViaRepeatWhenEmpty() {
        // Legacy regression guard for branch 1 with the default emptyPagesAllowed=false.
        // The 2 noChanges responses should be swallowed via repeatWhenEmpty; only the data
        // page should surface from a SINGLE nextPage() call (matching legacy behavior).
        FeedRangeContinuation continuation = mock(FeedRangeContinuation.class);
        when(continuation.isDone()).thenReturn(false);

        FeedResponse<Document> noChanges1 = changeFeedNoChanges("t1");
        FeedResponse<Document> noChanges2 = changeFeedNoChanges("t2");
        FeedResponse<Document> data = changeFeedDataPage("t3", new Document());

        // hasFetchedAllChanges returns false for all 3 (iteration shouldn't terminate via this signal).
        when(continuation.hasFetchedAllChanges(any(), any())).thenReturn(false);

        FeedResponse<Document>[] script = new FeedResponse[] { noChanges1, noChanges2, data };
        AtomicInteger callIndex = new AtomicInteger();
        Function<RxDocumentServiceRequest, Mono<FeedResponse<Document>>> executeFunc =
            req -> Mono.just(script[callIndex.getAndIncrement()]);

        ChangeFeedFetcher<Document> fetcher =
            newFetcherWithExecuteFunc(continuation, /* emptyPagesAllowed */ false, /* endLSN */ 999L, executeFunc);

        // A single nextPage() should drain all 2 noChanges responses via repeatWhenEmpty
        // and surface the data page.
        StepVerifier.create(fetcher.nextPage()).expectNextMatches(r -> r == data).verifyComplete();
        assertThat(callIndex.get()).describedAs("executeFunc should be called once per physical fetch").isEqualTo(3);
    }

    // Note: the >4*(size+1) consecutive-304 defense in FeedRangeCompositeContinuationImpl is
    // pinned end-to-end by CosmosContainerChangeFeedTest.changeFeedQuery_emptyPagesAllowed_*
    // (emulator group). That impl class is package-private, so a unit-level integration test
    // here would require reflection — the emulator test exercises the real production path.

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
        return newFetcherWithExecuteFunc(continuation, emptyPagesAllowed, /* endLSN */ null, req -> Mono.empty());
    }

    private static ChangeFeedFetcher<Document> newFetcherWithExecuteFunc(
        FeedRangeContinuation continuation,
        boolean emptyPagesAllowed,
        Function<RxDocumentServiceRequest, Mono<FeedResponse<Document>>> executeFunc) {

        return newFetcherWithExecuteFunc(continuation, emptyPagesAllowed, /* endLSN */ null, executeFunc);
    }

    private static ChangeFeedFetcher<Document> newFetcherWithExecuteFunc(
        FeedRangeContinuation continuation,
        boolean emptyPagesAllowed,
        Long endLSN,
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
            endLSN,
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

    // isFullyDrained is `protected` on Fetcher; this test class lives in the same package
    // (com.azure.cosmos.implementation.query), so we can call it directly without reflection.
    private static boolean invokeIsFullyDrained(
        ChangeFeedFetcher<Document> fetcher,
        FeedResponse<Document> response) {

        return fetcher.isFullyDrained(/* isChangeFeed */ true, response);
    }
}
