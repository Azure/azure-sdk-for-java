// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.implementation.HttpConstants;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Function;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for the primary-authoritative metadata hedging core. These exercise the pure
 * regional-failure classifier and the Reactor race semantics (no live client / store model),
 * mirroring the key cells of the .NET MetadataHedgingStrategyTests.
 */
public class MetadataHedgingStrategyTest {

    private static final Duration BLOCK = Duration.ofSeconds(15);

    // Sentinel branch errors + a classifier so the race can be tested without CosmosExceptions.
    private static final class RegionalError extends RuntimeException {
        RegionalError(String m) { super(m); }
    }
    private static final class DefinitiveError extends RuntimeException {
        DefinitiveError(String m) { super(m); }
    }
    private static final Function<Throwable, MetadataHedgingStrategy.BranchOutcome> CLASSIFIER =
        e -> (e instanceof DefinitiveError)
            ? MetadataHedgingStrategy.BranchOutcome.DEFINITIVE
            : MetadataHedgingStrategy.BranchOutcome.REGIONAL_FAILURE;

    private final MetadataHedgingStrategy strategy = new MetadataHedgingStrategy();

    private <T> MetadataHedgingStrategy.HedgeResult<T> run(Mono<T> primary, Mono<T> hedge, Duration threshold) {
        return strategy.executeHedged(primary, "primaryRegion", hedge, "hedgeRegion", threshold, CLASSIFIER)
            .block(BLOCK);
    }

    // ---- classifier (pure) ----

    @Test(groups = "unit")
    public void isRegionalFailure_regionalSet() {
        assertTrue(MetadataHedgingStrategy.isRegionalFailure(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE, 0));
        assertTrue(MetadataHedgingStrategy.isRegionalFailure(HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, 0));
        assertTrue(MetadataHedgingStrategy.isRegionalFailure(HttpConstants.StatusCodes.REQUEST_TIMEOUT, 0));
        assertTrue(MetadataHedgingStrategy.isRegionalFailure(
            HttpConstants.StatusCodes.GONE, HttpConstants.SubStatusCodes.LEASE_NOT_FOUND));
        assertTrue(MetadataHedgingStrategy.isRegionalFailure(
            HttpConstants.StatusCodes.FORBIDDEN, HttpConstants.SubStatusCodes.DATABASE_ACCOUNT_NOTFOUND));
        assertTrue(MetadataHedgingStrategy.isRegionalFailure(200, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE));
        assertTrue(MetadataHedgingStrategy.isRegionalFailure(200, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT));
    }

    @Test(groups = "unit")
    public void isRegionalFailure_definitiveSet() {
        assertFalse(MetadataHedgingStrategy.isRegionalFailure(HttpConstants.StatusCodes.NOTFOUND, 0));      // 404
        assertFalse(MetadataHedgingStrategy.isRegionalFailure(409, 0));                                     // conflict
        assertFalse(MetadataHedgingStrategy.isRegionalFailure(412, 0));                                     // precondition
        assertFalse(MetadataHedgingStrategy.isRegionalFailure(401, 0));                                     // unauthorized
        assertFalse(MetadataHedgingStrategy.isRegionalFailure(HttpConstants.StatusCodes.FORBIDDEN, 0));     // plain 403
        assertFalse(MetadataHedgingStrategy.isRegionalFailure(HttpConstants.StatusCodes.GONE, 0));          // plain 410 (split)
        assertFalse(MetadataHedgingStrategy.isRegionalFailure(HttpConstants.StatusCodes.TOO_MANY_REQUESTS, 0));
    }

    @Test(groups = "unit")
    public void classifyThrowable_nonCosmosIsRegional() {
        assertEquals(MetadataHedgingStrategy.classifyThrowable(new RuntimeException("boom")),
            MetadataHedgingStrategy.BranchOutcome.REGIONAL_FAILURE);
    }

    // ---- race semantics ----

    @Test(groups = "unit")
    public void primaryFast_noHedge() {
        MetadataHedgingStrategy.HedgeResult<String> r =
            run(Mono.just("P"), Mono.just("H"), Duration.ofMillis(300));
        assertFalse(r.isError());
        assertEquals(r.getValue(), "P");
        assertFalse(r.isHedgeFired());
        assertFalse(r.isHedgeWon());
    }

    @Test(groups = "unit")
    public void primarySlow_hedgeWins() {
        MetadataHedgingStrategy.HedgeResult<String> r = run(
            Mono.just("P").delayElement(Duration.ofMillis(800)),
            Mono.just("H"),
            Duration.ofMillis(100));
        assertFalse(r.isError());
        assertEquals(r.getValue(), "H");
        assertTrue(r.isHedgeFired());
        assertTrue(r.isHedgeWon());
        assertEquals(r.getWinningRegion(), "hedgeRegion");
    }

    @Test(groups = "unit")
    public void primaryFastDefinitiveError_noHedge() {
        MetadataHedgingStrategy.HedgeResult<String> r = run(
            Mono.error(new DefinitiveError("404")),
            Mono.just("H"),
            Duration.ofMillis(300));
        assertTrue(r.isError());
        assertTrue(r.getError() instanceof DefinitiveError);
        assertFalse(r.isHedgeFired());
        assertFalse(r.isHedgeWon());
    }

    @Test(groups = "unit")
    public void primaryRegionalError_hedgeWins() {
        MetadataHedgingStrategy.HedgeResult<String> r = run(
            Mono.error(new RegionalError("503")),
            Mono.just("H"),
            Duration.ofMillis(100));
        assertFalse(r.isError());
        assertEquals(r.getValue(), "H");
        assertTrue(r.isHedgeFired());
        assertTrue(r.isHedgeWon());
    }

    @Test(groups = "unit")
    public void primaryRegional_hedgeAlsoFails_primaryReturned() {
        MetadataHedgingStrategy.HedgeResult<String> r = run(
            Mono.error(new RegionalError("primary-503")),
            Mono.error(new RegionalError("hedge-401")),
            Duration.ofMillis(100));
        assertTrue(r.isError());
        assertEquals(r.getError().getMessage(), "primary-503");
        assertTrue(r.isHedgeFired());
        assertFalse(r.isHedgeWon());
    }

    @Test(groups = "unit")
    public void slowPrimaryDefinitive_beatsHedge_primaryAuthoritative() {
        // Threshold elapses (100ms) so the hedge fires; the primary then settles Definitive at 250ms,
        // before the hedge succeeds at ~100+400=500ms. The definitive primary must win.
        MetadataHedgingStrategy.HedgeResult<String> r = run(
            Mono.<String>error(new DefinitiveError("409")).delaySubscription(Duration.ofMillis(250)),
            Mono.just("H").delayElement(Duration.ofMillis(400)),
            Duration.ofMillis(100));
        assertTrue(r.isError());
        assertTrue(r.getError() instanceof DefinitiveError);
        assertTrue(r.isHedgeFired());
        assertFalse(r.isHedgeWon());
    }
}
