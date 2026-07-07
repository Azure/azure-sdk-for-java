// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.perPartitionAutomaticFailover.GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Bounded, primary-authoritative cross-region hedging for the two hot-path metadata cache reads
 * (Collection Read and PartitionKeyRange ReadFeed first page). This is the Java port of the .NET
 * metadata hedging strategy (Azure/azure-cosmos-dotnet-v3 PR #5999).
 * <p>
 * While this class is public it is NOT part of the published public API; it is used internally by
 * the SDK caches only.
 * <p>
 * Design invariant -- <b>the primary is authoritative</b>: a hedge can only win by being faster on a
 * <i>regional</i> failure or latency; it can never override a <i>definitive</i> answer the primary has
 * already produced (e.g. a 404 for a deleted collection, a 409, a 412). See {@code internal-spec.md}.
 * <p>
 * The core race ({@link #executeHedged}) is intentionally kept generic over the payload type so it can
 * be unit-tested in isolation, mirroring the proven data-plane hedging pattern
 * ({@code RxDocumentClientImpl.executePointOperationWithAvailabilityStrategy}: a delayed hedge plus a
 * first-acceptable combinator). Each branch is expected to be pinned to a single region with inner
 * cross-region retry suppressed by the caller so that "one hedge" and "primary authoritative" hold.
 */
public class MetadataHedgingStrategy {

    private static final Duration DEFAULT_THRESHOLD = Duration.ofMillis(1500);

    // Per-client collaborators (null in the unit-test / no-arg construction path).
    private final GlobalEndpointManager globalEndpointManager;
    private final GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover perPartitionAutomaticFailoverManager;
    // Tri-state opt-in: TRUE/FALSE force on/off; null -> follow the account PPAF state.
    private final Boolean enabledOverride;
    private final Duration threshold;

    /** Test / core-only constructor: the generic race ({@link #executeHedged}) can be used without a client. */
    public MetadataHedgingStrategy() {
        this(null, null, null, DEFAULT_THRESHOLD);
    }

    public MetadataHedgingStrategy(
        GlobalEndpointManager globalEndpointManager,
        GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover perPartitionAutomaticFailoverManager,
        Boolean enabledOverride,
        Duration threshold) {
        this.globalEndpointManager = globalEndpointManager;
        this.perPartitionAutomaticFailoverManager = perPartitionAutomaticFailoverManager;
        this.enabledOverride = enabledOverride;
        this.threshold = threshold == null ? DEFAULT_THRESHOLD : threshold;
    }

    /**
     * The effective per-request opt-in: an explicit override wins; otherwise follow the account's live PPAF state
     * (there is no preview/GA package split in Java, so the .NET "preview=on" arm collapses to this).
     */
    public boolean isEffectivelyEnabled() {
        if (this.enabledOverride != null) {
            return this.enabledOverride;
        }
        return this.perPartitionAutomaticFailoverManager != null
            && this.perPartitionAutomaticFailoverManager.isPerPartitionAutomaticFailoverEnabled();
    }

    public Duration getThreshold() {
        return this.threshold;
    }

    /**
     * The ordered applicable read regions for a request (honoring excluded + per-partition-unavailable regions).
     * Returns an empty list when there is no endpoint manager (test path) or fewer than two regions.
     */
    public List<RegionalRoutingContext> getApplicableReadRegions(RxDocumentServiceRequest request) {
        if (this.globalEndpointManager == null) {
            return new ArrayList<>();
        }
        List<RegionalRoutingContext> ctxs =
            new ArrayList<>(this.globalEndpointManager.getApplicableReadRegionalRoutingContexts(request));
        return ctxs;
    }

    public String getRegionName(RegionalRoutingContext ctx, RxDocumentServiceRequest request) {
        if (this.globalEndpointManager == null || ctx == null || ctx.getGatewayRegionalEndpoint() == null) {
            return null;
        }
        return this.globalEndpointManager.getRegionName(ctx.getGatewayRegionalEndpoint(), request.getOperationType());
    }

    /**
     * Exclude every applicable region except {@code target}, so a branch is pinned to a single region with inner
     * cross-region retry suppressed. Mirrors {@code RxDocumentClientImpl.getEffectiveExcludedRegionsForHedging}.
     */
    public static List<String> excludedRegionsForTarget(List<String> baseExcluded, List<String> applicable, String target) {
        List<String> excluded = new ArrayList<>();
        if (baseExcluded != null) {
            excluded.addAll(baseExcluded);
        }
        for (String r : applicable) {
            if (r != null && !r.equals(target) && !excluded.contains(r)) {
                excluded.add(r);
            }
        }
        return excluded;
    }

    /**
     * Run a single-request metadata read (Collection Read) with hedging when eligible. Each branch is a
     * region-pinned clone (via {@code routeToLocation} + excluded-regions); otherwise the original send is returned
     * unchanged.
     */
    public Mono<RxDocumentServiceResponse> executeMetadataRead(
        RxDocumentServiceRequest request,
        Function<RxDocumentServiceRequest, Mono<RxDocumentServiceResponse>> sendFunc,
        Function<Throwable, BranchOutcome> classifier,
        Consumer<HedgeResult<RxDocumentServiceResponse>> telemetrySink) {

        if (!isEffectivelyEnabled() || this.globalEndpointManager == null) {
            return sendFunc.apply(request);
        }

        List<RegionalRoutingContext> ctxs = getApplicableReadRegions(request);
        if (ctxs.size() < 2) {
            return sendFunc.apply(request);
        }

        RegionalRoutingContext primaryCtx = ctxs.get(0);
        RegionalRoutingContext hedgeCtx = ctxs.get(1);
        String primaryRegion = getRegionName(primaryCtx, request);
        String hedgeRegion = getRegionName(hedgeCtx, request);

        List<String> allRegions = new ArrayList<>();
        for (RegionalRoutingContext ctx : ctxs) {
            String name = getRegionName(ctx, request);
            if (name != null && !allRegions.contains(name)) {
                allRegions.add(name);
            }
        }
        List<String> baseExcluded = request.requestContext == null ? null : request.requestContext.getExcludeRegions();

        RxDocumentServiceRequest primaryReq = request.clone();
        primaryReq.requestContext.routeToLocation(primaryCtx);
        primaryReq.requestContext.setExcludeRegions(excludedRegionsForTarget(baseExcluded, allRegions, primaryRegion));

        RxDocumentServiceRequest hedgeReq = request.clone();
        hedgeReq.requestContext.routeToLocation(hedgeCtx);
        hedgeReq.requestContext.setExcludeRegions(excludedRegionsForTarget(baseExcluded, allRegions, hedgeRegion));

        Mono<RxDocumentServiceResponse> primaryMono = Mono.defer(() -> sendFunc.apply(primaryReq));
        Mono<RxDocumentServiceResponse> hedgeMono = Mono.defer(() -> sendFunc.apply(hedgeReq));

        return executeHedged(primaryMono, primaryRegion, hedgeMono, hedgeRegion, this.threshold, classifier)
            .flatMap(result -> {
                if (telemetrySink != null && result.isHedgeFired()) {
                    telemetrySink.accept(result);
                }
                return result.unwrap();
            });
    }

    /**
     * Classification of a single branch's completed outcome. Only a {@link #REGIONAL_FAILURE} is worth hedging;
     * a {@link #DEFINITIVE} error is authoritative and returned verbatim.
     */
    public enum BranchOutcome {
        SUCCESS,
        REGIONAL_FAILURE,
        DEFINITIVE
    }

    private enum Origin { PRIMARY, HEDGE }

    /**
     * The outcome of a hedged execution: the winning value or error plus the hedge telemetry
     * ({@code HedgeFired}/{@code HedgeWon}/{@code winningRegion}).
     */
    public static final class HedgeResult<T> {
        private final T value;
        private final Throwable error;
        private final boolean hedgeFired;
        private final boolean hedgeWon;
        private final String winningRegion;

        private HedgeResult(T value, Throwable error, boolean hedgeFired, boolean hedgeWon, String winningRegion) {
            this.value = value;
            this.error = error;
            this.hedgeFired = hedgeFired;
            this.hedgeWon = hedgeWon;
            this.winningRegion = winningRegion;
        }

        public boolean isError() {
            return this.error != null;
        }

        public T getValue() {
            return this.value;
        }

        public Throwable getError() {
            return this.error;
        }

        public boolean isHedgeFired() {
            return this.hedgeFired;
        }

        public boolean isHedgeWon() {
            return this.hedgeWon;
        }

        public String getWinningRegion() {
            return this.winningRegion;
        }

        /** Unwrap into the caller's Mono: propagate the error verbatim, or emit the value. */
        public Mono<T> unwrap() {
            return this.isError() ? Mono.error(this.error) : Mono.just(this.value);
        }
    }

    // An internal, never-erroring materialization of a single branch attempt.
    private static final class Attempt<T> {
        private final Origin origin;
        private final String region;
        private final T value;
        private final Throwable error;
        private final BranchOutcome outcome;

        private Attempt(Origin origin, String region, T value, Throwable error, BranchOutcome outcome) {
            this.origin = origin;
            this.region = region;
            this.value = value;
            this.error = error;
            this.outcome = outcome;
        }

        private boolean isWinner() {
            // Primary is authoritative on any non-regional outcome (success or definitive error).
            // The hedge only wins by producing a good answer.
            return this.origin == Origin.PRIMARY
                ? this.outcome != BranchOutcome.REGIONAL_FAILURE
                : this.outcome == BranchOutcome.SUCCESS;
        }
    }

    /**
     * Run a primary-authoritative hedged execution.
     * <p>
     * Semantics (mirrors the .NET flow):
     * <ul>
     *   <li>The primary is subscribed immediately (single, shared subscription).</li>
     *   <li>The hedge is fired as soon as either {@code threshold} elapses (the primary is slow) or the
     *       primary settles with a <i>regional</i> failure before then; a non-regional primary outcome
     *       cancels the gate before the hedge subscribes (no phantom hedge).</li>
     *   <li>The first branch to produce a <i>winning</i> outcome wins -- primary on any non-regional
     *       outcome, hedge only on success -- so a hedge can never override a definitive primary answer.</li>
     *   <li>If neither branch wins (primary regional failure AND hedge non-success), the primary's own
     *       outcome is returned verbatim so the outer retry policy classifies the real failure.</li>
     * </ul>
     *
     * @param primary       the primary-region attempt (must be pinned to a single region by the caller)
     * @param primaryRegion the primary region name (for telemetry)
     * @param hedge         the hedge-region attempt (pinned to a different single region)
     * @param hedgeRegion   the hedge region name (for telemetry)
     * @param threshold     the delay after which the hedge is fired if the primary has not settled
     * @param classifier    maps a branch error to a {@link BranchOutcome}
     */
    public <T> Mono<HedgeResult<T>> executeHedged(
        Mono<T> primary,
        String primaryRegion,
        Mono<T> hedge,
        String hedgeRegion,
        Duration threshold,
        Function<Throwable, BranchOutcome> classifier) {

        AtomicBoolean hedgeStarted = new AtomicBoolean(false);

        // Materialize the primary once so the race, the early-hedge gate and the fallback all share a
        // single subscription.
        Mono<Attempt<T>> primaryAttempt = primary
            .map(v -> new Attempt<>(Origin.PRIMARY, primaryRegion, v, null, BranchOutcome.SUCCESS))
            .onErrorResume(e -> Mono.just(
                new Attempt<T>(Origin.PRIMARY, primaryRegion, null, e, classifier.apply(e))))
            .cache();

        // Fire the single hedge as soon as EITHER the threshold elapses (the primary is slow) OR the
        // primary settles with a regional failure before the threshold -- mirroring the .NET flow
        // ("primary is slow, or hit a regional failure -> fire one hedge"). A non-regional primary
        // outcome (success or definitive error) never triggers an early hedge; in that case the primary
        // has already won the race below and this gate is cancelled before the hedge subscribes.
        Mono<Boolean> hedgeGate = Flux.merge(
                Mono.delay(threshold).thenReturn(Boolean.TRUE),
                primaryAttempt.flatMap(attempt -> attempt.outcome == BranchOutcome.REGIONAL_FAILURE
                    ? Mono.just(Boolean.TRUE)
                    : Mono.never()))
            .next();

        Mono<Attempt<T>> hedgeAttempt = hedgeGate.then(
            hedge
                .doOnSubscribe(s -> hedgeStarted.set(true))
                .map(v -> new Attempt<>(Origin.HEDGE, hedgeRegion, v, null, BranchOutcome.SUCCESS))
                .onErrorResume(e -> Mono.just(
                    new Attempt<T>(Origin.HEDGE, hedgeRegion, null, e, classifier.apply(e)))));

        return Flux.merge(primaryAttempt.flux(), hedgeAttempt.flux())
            .filter(Attempt::isWinner)
            .next()
            // No winner at all -> return the primary's actual (regional) outcome, verbatim.
            .switchIfEmpty(primaryAttempt)
            .map(winner -> {
                boolean hedgeWon = winner.origin == Origin.HEDGE;
                return new HedgeResult<>(
                    winner.value,
                    winner.error,
                    hedgeStarted.get(),
                    hedgeWon,
                    winner.region);
            });
    }

    /**
     * Classify a completed branch throwable. Only a genuine <i>regional</i> failure of the region is
     * worth hedging; everything else (including auth failures and definitive errors) is authoritative.
     * <p>
     * NOTE: this is an explicit classifier aligned with the SDK's cross-region retry semantics, not a
     * borrow from the narrow {@code MetadataRequestRetryPolicy} region-unavailable check. The
     * PartitionKeyRange path may extend the regional set with its regional-lag 404 sub-statuses
     * (handled by {@link #isPartitionKeyRangeRegionalFailure}).
     */
    public static BranchOutcome classifyThrowable(Throwable throwable) {
        CosmosException cosmosException = asCosmosException(throwable);
        if (cosmosException == null) {
            // A non-Cosmos error (e.g. a transport error not yet mapped) is treated as regional so a
            // healthy region can still answer.
            return BranchOutcome.REGIONAL_FAILURE;
        }
        return isRegionalFailure(cosmosException.getStatusCode(), cosmosException.getSubStatusCode())
            ? BranchOutcome.REGIONAL_FAILURE
            : BranchOutcome.DEFINITIVE;
    }

    /**
     * Classifier variant for the PartitionKeyRange ReadFeed path, which additionally treats the
     * regional-lag 404 sub-statuses as regional (consistent with {@code ClientRetryPolicy}).
     */
    public static BranchOutcome classifyPartitionKeyRangeThrowable(Throwable throwable) {
        CosmosException cosmosException = asCosmosException(throwable);
        if (cosmosException == null) {
            return BranchOutcome.REGIONAL_FAILURE;
        }
        int status = cosmosException.getStatusCode();
        int subStatus = cosmosException.getSubStatusCode();
        if (isRegionalFailure(status, subStatus) || isPartitionKeyRangeRegionalFailure(status, subStatus)) {
            return BranchOutcome.REGIONAL_FAILURE;
        }
        return BranchOutcome.DEFINITIVE;
    }

    /**
     * The shared regional-failure classifier. A regional failure means "the region is at fault"
     * (worth hedging), as opposed to a definitive, request-level answer.
     */
    public static boolean isRegionalFailure(int statusCode, int subStatusCode) {
        // Transport / gateway-endpoint failures (network failure mapped by RxGatewayStoreModel).
        if (subStatusCode == HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE
            || subStatusCode == HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT) {
            return true;
        }

        switch (statusCode) {
            case HttpConstants.StatusCodes.SERVICE_UNAVAILABLE:   // 503
            case HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR: // 500
            case HttpConstants.StatusCodes.REQUEST_TIMEOUT:       // 408
                return true;
            case HttpConstants.StatusCodes.GONE:                  // 410 -- only lease-not-found is regional
                return subStatusCode == HttpConstants.SubStatusCodes.LEASE_NOT_FOUND;
            case HttpConstants.StatusCodes.FORBIDDEN:             // 403 -- only database-account-not-found is regional
                return subStatusCode == HttpConstants.SubStatusCodes.DATABASE_ACCOUNT_NOTFOUND;
            default:
                // 401, plain 403, 404, 409, 412, 429, etc. are NOT regional -- authoritative/definitive.
                return false;
        }
    }

    // The PartitionKeyRange ReadFeed treats certain 404 sub-statuses as regional lag (routing map not
    // yet available in this region), consistent with ClientRetryPolicy's PK-range handling.
    private static boolean isPartitionKeyRangeRegionalFailure(int statusCode, int subStatusCode) {
        return statusCode == HttpConstants.StatusCodes.NOTFOUND
            && (subStatusCode == 0 || subStatusCode == HttpConstants.SubStatusCodes.DATABASE_ACCOUNT_NOTFOUND);
    }

    private static CosmosException asCosmosException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof CosmosException) {
                return (CosmosException) current;
            }
            current = current.getCause();
        }
        return null;
    }
}
