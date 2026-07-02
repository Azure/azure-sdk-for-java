// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.metadatahedging;

import com.azure.cosmos.implementation.ForbiddenException;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.ServiceUnavailableException;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;

public class MetadataHedgingStrategyTest {

    private static final int TIMEOUT = 10_000;

    private GlobalEndpointManager globalEndpointManager;
    private RegionalRoutingContext east;
    private RegionalRoutingContext west;

    @BeforeMethod(groups = "unit")
    public void before() {
        this.east = new RegionalRoutingContext(URI.create("https://east.example.com"));
        this.west = new RegionalRoutingContext(URI.create("https://west.example.com"));
        this.globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);

        Mockito.when(this.globalEndpointManager.getReadEndpoints())
            .thenReturn(new UnmodifiableList<>(Arrays.asList(this.east, this.west)));
        Mockito.when(this.globalEndpointManager.getApplicableReadRegionalRoutingContexts(any(RxDocumentServiceRequest.class)))
            .thenReturn(new UnmodifiableList<>(Arrays.asList(this.east, this.west)));
        Mockito.when(this.globalEndpointManager.resolveServiceEndpoint(any())).thenReturn(this.east);
        Mockito.when(this.globalEndpointManager.getRegionName(eq(this.east.getGatewayRegionalEndpoint()), any(), anyBoolean()))
            .thenReturn("East");
        Mockito.when(this.globalEndpointManager.getRegionName(eq(this.west.getGatewayRegionalEndpoint()), any(), anyBoolean()))
            .thenReturn("West");
    }

    // ---------- resolveOptIn ----------

    @Test(groups = "unit")
    public void resolveOptIn_nullFollowsPpaf() {
        assertThat(MetadataHedgingStrategy.resolveOptIn(null, true)).isTrue();
        assertThat(MetadataHedgingStrategy.resolveOptIn(null, false)).isFalse();
    }

    @Test(groups = "unit")
    public void resolveOptIn_explicitWins() {
        assertThat(MetadataHedgingStrategy.resolveOptIn(Boolean.TRUE, false)).isTrue();
        assertThat(MetadataHedgingStrategy.resolveOptIn(Boolean.FALSE, true)).isFalse();
    }

    // ---------- createIfEnabled ----------

    @Test(groups = "unit")
    public void createIfEnabled_explicitFalseReturnsNull() {
        assertThat(MetadataHedgingStrategy.createIfEnabled(Boolean.FALSE, this.globalEndpointManager, () -> true)).isNull();
    }

    @Test(groups = "unit")
    public void createIfEnabled_nullOrTrueReturnsStrategy() {
        assertThat(MetadataHedgingStrategy.createIfEnabled(null, this.globalEndpointManager, () -> false)).isNotNull();
        assertThat(MetadataHedgingStrategy.createIfEnabled(Boolean.TRUE, this.globalEndpointManager, () -> false)).isNotNull();
    }

    // ---------- isRegionalFailure(status, subStatus) ----------

    @Test(groups = "unit")
    public void isRegionalFailure_statusOverlay() {
        assertThat(MetadataHedgingStrategy.isRegionalFailure(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE, 0)).isTrue();
        assertThat(MetadataHedgingStrategy.isRegionalFailure(HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, 0)).isTrue();
        assertThat(MetadataHedgingStrategy.isRegionalFailure(
            HttpConstants.StatusCodes.GONE, HttpConstants.SubStatusCodes.LEASE_NOT_FOUND)).isTrue();
        assertThat(MetadataHedgingStrategy.isRegionalFailure(HttpConstants.StatusCodes.GONE, 0)).isFalse();
        assertThat(MetadataHedgingStrategy.isRegionalFailure(
            HttpConstants.StatusCodes.FORBIDDEN, HttpConstants.SubStatusCodes.DATABASE_ACCOUNT_NOTFOUND)).isTrue();
        assertThat(MetadataHedgingStrategy.isRegionalFailure(HttpConstants.StatusCodes.FORBIDDEN, 0)).isFalse();
        assertThat(MetadataHedgingStrategy.isRegionalFailure(HttpConstants.StatusCodes.NOTFOUND, 0)).isFalse();
    }

    // ---------- isRegionalFailure(Throwable) ----------

    @Test(groups = "unit")
    public void isRegionalFailure_throwable() {
        assertThat(MetadataHedgingStrategy.isRegionalFailure((Throwable) null)).isFalse();
        assertThat(MetadataHedgingStrategy.isRegionalFailure(serviceUnavailable())).isTrue();
        assertThat(MetadataHedgingStrategy.isRegionalFailure(new NotFoundException())).isFalse();
    }

    // ---------- isAcceptableWinner(response, branch) ----------

    @Test(groups = "unit")
    public void isAcceptableWinner_response() {
        assertThat(MetadataHedgingStrategy.isAcceptableWinner(okResponse(), HedgeBranch.PRIMARY)).isTrue();
        assertThat(MetadataHedgingStrategy.isAcceptableWinner(okResponse(), HedgeBranch.HEDGE)).isTrue();
        assertThat(MetadataHedgingStrategy.isAcceptableWinner(
            responseWithStatus(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE), HedgeBranch.PRIMARY)).isFalse();
        // Hedge-branch 401 / 403 overlay
        assertThat(MetadataHedgingStrategy.isAcceptableWinner(
            responseWithStatus(HttpConstants.StatusCodes.UNAUTHORIZED), HedgeBranch.HEDGE)).isFalse();
        assertThat(MetadataHedgingStrategy.isAcceptableWinner(
            responseWithStatus(HttpConstants.StatusCodes.FORBIDDEN), HedgeBranch.HEDGE)).isFalse();
        assertThat(MetadataHedgingStrategy.isAcceptableWinner((RxDocumentServiceResponse) null, HedgeBranch.PRIMARY)).isFalse();
    }

    // ---------- isAcceptableWinner(throwable, branch) ----------

    @Test(groups = "unit")
    public void isAcceptableWinner_throwable() {
        assertThat(MetadataHedgingStrategy.isAcceptableWinner(serviceUnavailable(), HedgeBranch.PRIMARY)).isFalse();
        assertThat(MetadataHedgingStrategy.isAcceptableWinner(serviceUnavailable(), HedgeBranch.HEDGE)).isFalse();
        // Non-regional terminal error is an acceptable winner so the retry path classifies it.
        assertThat(MetadataHedgingStrategy.isAcceptableWinner(new NotFoundException(), HedgeBranch.PRIMARY)).isTrue();
        // Hedge-branch 403 is rejected even though it is not a regional failure.
        assertThat(MetadataHedgingStrategy.isAcceptableWinner(forbidden(), HedgeBranch.HEDGE)).isFalse();
        assertThat(MetadataHedgingStrategy.isAcceptableWinner(forbidden(), HedgeBranch.PRIMARY)).isTrue();
    }

    // ---------- evaluateEligibility ----------

    @Test(groups = "unit")
    public void eligibility_eligibleColdStartCollectionRead() {
        MetadataHedgingStrategy strategy = strategy(Boolean.TRUE, () -> true, 1000);
        MetadataHedgingContext ctx = coldStartContext();
        MetadataHedgeEligibility eligibility = strategy.evaluateEligibility(collectionReadRequest(), ctx);
        assertThat(eligibility.isEligible()).isTrue();
    }

    @Test(groups = "unit")
    public void eligibility_notColdStart() {
        MetadataHedgingStrategy strategy = strategy(Boolean.TRUE, () -> true, 1000);
        MetadataHedgingContext ctx = new MetadataHedgingContext();
        ctx.setColdStart(false);
        assertThat(strategy.evaluateEligibility(collectionReadRequest(), ctx).getSkipReason())
            .isEqualTo(MetadataHedgeSkipReason.NOT_COLD_START);
    }

    @Test(groups = "unit")
    public void eligibility_unsupportedResource() {
        MetadataHedgingStrategy strategy = strategy(Boolean.TRUE, () -> true, 1000);
        RxDocumentServiceRequest documentRead = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document, "/dbs/db/colls/col/docs/d", new HashMap<>());
        assertThat(strategy.evaluateEligibility(documentRead, coldStartContext()).getSkipReason())
            .isEqualTo(MetadataHedgeSkipReason.RESOURCE_TYPE_NOT_SUPPORTED);
    }

    @Test(groups = "unit")
    public void eligibility_partitionKeyRangeNotFirstPage() {
        MetadataHedgingStrategy strategy = strategy(Boolean.TRUE, () -> true, 1000);
        MetadataHedgingContext ctx = coldStartContext();
        ctx.setFirstReadFeedPage(false);
        RxDocumentServiceRequest pkRangeRead = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(), OperationType.ReadFeed, ResourceType.PartitionKeyRange, "/dbs/db/colls/col/pkranges", new HashMap<>());
        assertThat(strategy.evaluateEligibility(pkRangeRead, ctx).getSkipReason())
            .isEqualTo(MetadataHedgeSkipReason.NOT_FIRST_READ_FEED_PAGE);
    }

    @Test(groups = "unit")
    public void eligibility_singleRegion() {
        Mockito.when(this.globalEndpointManager.getReadEndpoints())
            .thenReturn(new UnmodifiableList<>(Collections.singletonList(this.east)));
        MetadataHedgingStrategy strategy = strategy(Boolean.TRUE, () -> true, 1000);
        assertThat(strategy.evaluateEligibility(collectionReadRequest(), coldStartContext()).getSkipReason())
            .isEqualTo(MetadataHedgeSkipReason.SINGLE_REGION);
    }

    @Test(groups = "unit")
    public void eligibility_excludedRegionLeavesNoTarget() {
        Mockito.when(this.globalEndpointManager.getApplicableReadRegionalRoutingContexts(any(RxDocumentServiceRequest.class)))
            .thenReturn(new UnmodifiableList<>(Collections.singletonList(this.east)));
        MetadataHedgingStrategy strategy = strategy(Boolean.TRUE, () -> true, 1000);
        assertThat(strategy.evaluateEligibility(collectionReadRequest(), coldStartContext()).getSkipReason())
            .isEqualTo(MetadataHedgeSkipReason.EXCLUDED_REGION_LEAVES_NO_TARGET);
    }

    @Test(groups = "unit")
    public void eligibility_ppafDisabledWithNullOptIn() {
        MetadataHedgingStrategy strategy = strategy(null, () -> false, 1000);
        assertThat(strategy.evaluateEligibility(collectionReadRequest(), coldStartContext()).getSkipReason())
            .isEqualTo(MetadataHedgeSkipReason.PPAF_DISABLED);
    }

    // ---------- executeAsync ----------

    @Test(groups = "unit", timeOut = TIMEOUT)
    public void executeAsync_ineligibleFallsBackToPrimaryOnly() {
        Mockito.when(this.globalEndpointManager.getReadEndpoints())
            .thenReturn(new UnmodifiableList<>(Collections.singletonList(this.east)));
        MetadataHedgingStrategy strategy = strategy(Boolean.TRUE, () -> true, 1000);

        RxDocumentServiceResponse ok = okResponse();
        AtomicInteger calls = new AtomicInteger();
        Function<RxDocumentServiceRequest, Mono<RxDocumentServiceResponse>> sender = req -> {
            calls.incrementAndGet();
            return Mono.just(ok);
        };

        StepVerifier.create(strategy.executeAsync(collectionReadRequest(), sender, coldStartContext()))
            .assertNext(result -> {
                assertThat(result.isHedgeFired()).isFalse();
                assertThat(result.getResponse()).isSameAs(ok);
                assertThat(result.getDiagnostics().getTotalAttempts()).isEqualTo(1);
            })
            .verifyComplete();

        assertThat(calls.get()).isEqualTo(1);
    }

    @Test(groups = "unit", timeOut = TIMEOUT)
    public void executeAsync_primaryWinsNoHedge() {
        MetadataHedgingStrategy strategy = strategy(Boolean.TRUE, () -> true, 2000);

        RxDocumentServiceResponse ok = okResponse();
        Function<RxDocumentServiceRequest, Mono<RxDocumentServiceResponse>> sender = req -> Mono.just(ok);

        StepVerifier.create(strategy.executeAsync(collectionReadRequest(), sender, coldStartContext()))
            .assertNext(result -> {
                assertThat(result.isHedgeFired()).isFalse();
                assertThat(result.getResponse()).isSameAs(ok);
                assertThat(result.getWinningRegion()).isEqualTo("East");
            })
            .verifyComplete();
    }

    @Test(groups = "unit", timeOut = TIMEOUT)
    public void executeAsync_fastPrimaryWinDoesNotLeakLateHedge() throws InterruptedException {
        // Regression: the hedge timer must be cancelled when the primary wins before the
        // threshold. With a hot/cached hedge pipeline the detached timer would dispatch a
        // spurious hedge ~threshold after the operation already returned.
        long thresholdMs = 200;
        MetadataHedgingStrategy strategy = strategy(Boolean.TRUE, () -> true, thresholdMs);

        AtomicInteger hedgeCalls = new AtomicInteger();
        RxDocumentServiceResponse primaryOk = okResponse();
        Function<RxDocumentServiceRequest, Mono<RxDocumentServiceResponse>> sender = req -> {
            RegionalRoutingContext routed = req.requestContext.regionalRoutingContextToRoute;
            if (routed != null && routed.equals(this.west)) {
                hedgeCalls.incrementAndGet();
                return Mono.just(okResponse());
            }
            return Mono.just(primaryOk);
        };

        StepVerifier.create(strategy.executeAsync(collectionReadRequest(), sender, coldStartContext()))
            .assertNext(result -> {
                assertThat(result.isHedgeFired()).isFalse();
                assertThat(result.getResponse()).isSameAs(primaryOk);
            })
            .verifyComplete();

        // Wait well past the threshold; a leaked timer would have fired the hedge by now.
        Thread.sleep(thresholdMs * 4);
        assertThat(hedgeCalls.get()).isZero();
    }

    @Test(groups = "unit", timeOut = TIMEOUT)
    public void executeAsync_hedgeWinsWhenPrimaryRegionalFailure() {
        MetadataHedgingStrategy strategy = strategy(Boolean.TRUE, () -> true, 2000);

        RxDocumentServiceResponse hedgeOk = okResponse();
        Function<RxDocumentServiceRequest, Mono<RxDocumentServiceResponse>> sender = req -> {
            RegionalRoutingContext routed = req.requestContext.regionalRoutingContextToRoute;
            if (routed != null && routed.equals(this.west)) {
                return Mono.just(hedgeOk);
            }
            return Mono.error(serviceUnavailable());
        };

        StepVerifier.create(strategy.executeAsync(collectionReadRequest(), sender, coldStartContext()))
            .assertNext(result -> {
                assertThat(result.isHedgeFired()).isTrue();
                assertThat(result.getResponse()).isSameAs(hedgeOk);
                assertThat(result.getWinningRegion()).isEqualTo("West");
                assertThat(result.getDiagnostics().getTotalAttempts()).isEqualTo(2);
            })
            .verifyComplete();
    }

    @Test(groups = "unit", timeOut = TIMEOUT)
    public void executeAsync_budgetExhaustedFallsBackToPrimaryOnly() {
        MetadataHedgingStrategy strategy = strategy(Boolean.TRUE, () -> true, 2000, 1);

        // Occupy the single permit with a hanging primary so the budget is exhausted.
        Disposable hanging = strategy
            .executeAsync(collectionReadRequest(), req -> Mono.never(), coldStartContext())
            .subscribe();

        try {
            assertThat(strategy.getAvailableBudget()).isEqualTo(0);

            RxDocumentServiceResponse ok = okResponse();
            StepVerifier.create(strategy.executeAsync(collectionReadRequest(), req -> Mono.just(ok), coldStartContext()))
                .assertNext(result -> {
                    assertThat(result.isHedgeFired()).isFalse();
                    assertThat(result.getResponse()).isSameAs(ok);
                    assertThat(result.getDiagnostics().getSkipReason())
                        .isEqualTo(MetadataHedgeSkipReason.BUDGET_EXHAUSTED);
                })
                .verifyComplete();
        } finally {
            hanging.dispose();
        }
    }

    // ---------- helpers ----------

    private MetadataHedgingStrategy strategy(Boolean optIn, java.util.function.BooleanSupplier ppaf, long thresholdMs) {
        return strategy(optIn, ppaf, thresholdMs, MetadataHedgingStrategy.DEFAULT_PER_CLIENT_CONCURRENCY_BUDGET);
    }

    private MetadataHedgingStrategy strategy(Boolean optIn, java.util.function.BooleanSupplier ppaf, long thresholdMs, int budget) {
        return new MetadataHedgingStrategy(
            this.globalEndpointManager,
            () -> false,
            ppaf,
            optIn,
            Duration.ofMillis(thresholdMs),
            budget);
    }

    private static MetadataHedgingContext coldStartContext() {
        MetadataHedgingContext ctx = new MetadataHedgingContext();
        ctx.setColdStart(true);
        return ctx;
    }

    private static RxDocumentServiceRequest collectionReadRequest() {
        return RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(), OperationType.Read, ResourceType.DocumentCollection, "/dbs/db/colls/col", new HashMap<>());
    }

    private static RxDocumentServiceResponse okResponse() {
        return responseWithStatus(HttpConstants.StatusCodes.OK);
    }

    private static RxDocumentServiceResponse responseWithStatus(int statusCode) {
        RxDocumentServiceResponse response = Mockito.mock(RxDocumentServiceResponse.class);
        Mockito.when(response.getStatusCode()).thenReturn(statusCode);
        Mockito.when(response.getResponseHeaders()).thenReturn(new HashMap<>());
        return response;
    }

    private static ServiceUnavailableException serviceUnavailable() {
        return new ServiceUnavailableException("metadata hedge test", new HttpHeaders(), null, 0);
    }

    private static ForbiddenException forbidden() {
        return new ForbiddenException("metadata hedge test", new HttpHeaders(), null);
    }
}

