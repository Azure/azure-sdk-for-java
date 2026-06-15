// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.metadatahedging;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.WebExceptionUtility;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

/**
 * Bounded cross-region hedging for cold-start metadata cache population. One instance per
 * client. Consumed by {@code RxClientCollectionCache} (and, in a later phase,
 * {@code RxPartitionKeyRangeCache}) for cold-start metadata cache reads.
 * <p>
 * Java (Project Reactor) port of the .NET {@code MetadataHedgingStrategy}
 * (Azure/azure-cosmos-dotnet-v3#5923). The orchestration races a primary regional read against
 * a hedge dispatched after a fixed SDK-derived threshold, returning the first acceptable winner
 * and cancelling the loser. When ineligible (single region, not a cold start, opt-in disabled,
 * budget exhausted, ...) the read collapses to a primary-only send with identical behaviour to
 * the non-hedged path.
 */
public final class MetadataHedgingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(MetadataHedgingStrategy.class);

    /**
     * Per-client concurrency budget for in-flight metadata hedges (design &sect;5.11).
     * Not customer-configurable.
     */
    public static final int DEFAULT_PER_CLIENT_CONCURRENCY_BUDGET = 8;

    /**
     * Added to the first-attempt timeout to derive the fixed hedge threshold (today
     * 1&nbsp;s + 500&nbsp;ms = 1.5&nbsp;s). Not customer-configurable. See design &sect;5.1 / &sect;5.9.
     */
    public static final Duration DEFAULT_THRESHOLD = Duration.ofMillis(1500);

    private final GlobalEndpointManager globalEndpointManager;
    private final BooleanSupplier isHedgingDisabledByGateway;
    private final BooleanSupplier isPpafEnabled;
    private final Boolean customerOptIn;
    private final Duration threshold;
    private final Semaphore hedgeBudget;
    private final int perClientConcurrencyBudget;

    public MetadataHedgingStrategy(
        GlobalEndpointManager globalEndpointManager,
        BooleanSupplier isHedgingDisabledByGateway,
        BooleanSupplier isPpafEnabled,
        Boolean customerOptIn,
        Duration threshold,
        int perClientConcurrencyBudget) {

        if (globalEndpointManager == null) {
            throw new NullPointerException("globalEndpointManager");
        }
        if (threshold == null || threshold.isZero() || threshold.isNegative()) {
            throw new IllegalArgumentException("threshold must be a positive duration");
        }

        this.globalEndpointManager = globalEndpointManager;
        this.isHedgingDisabledByGateway = isHedgingDisabledByGateway != null ? isHedgingDisabledByGateway : () -> false;
        this.isPpafEnabled = isPpafEnabled != null ? isPpafEnabled : () -> false;
        this.customerOptIn = customerOptIn;
        this.threshold = threshold;
        this.perClientConcurrencyBudget = Math.max(1, perClientConcurrencyBudget);
        this.hedgeBudget = new Semaphore(this.perClientConcurrencyBudget);
    }

    public Duration getThreshold() {
        return this.threshold;
    }

    public int getPerClientConcurrencyBudget() {
        return this.perClientConcurrencyBudget;
    }

    public int getAvailableBudget() {
        return this.hedgeBudget.availablePermits();
    }

    /**
     * Resolves the tri-state opt-in to a concrete {@code boolean}. When the customer leaves the
     * property {@code null}, cold-start metadata hedging follows the account's PPAF state. An
     * explicit {@code true} enables hedging even when PPAF is disabled, and an explicit
     * {@code false} disables it regardless of PPAF.
     */
    public static boolean resolveOptIn(Boolean customerOptIn, boolean isPpafEnabled) {
        return customerOptIn != null ? customerOptIn : isPpafEnabled;
    }

    /**
     * Builds the strategy from the customer-supplied tri-state opt-in. Returns {@code null} only
     * when the customer explicitly disabled hedging ({@code false}). When the property is
     * {@code true} or left {@code null} the strategy is created and the per-request eligibility
     * check resolves the effective opt-in against the live PPAF state. The Gateway kill-switch is
     * hard-wired to {@code false} in Phase 1; see design &sect;5.1.
     */
    public static MetadataHedgingStrategy createIfEnabled(
        Boolean enableMetadataHedgingForColdStart,
        GlobalEndpointManager globalEndpointManager,
        BooleanSupplier isPpafEnabled) {

        if (Boolean.FALSE.equals(enableMetadataHedgingForColdStart)) {
            return null;
        }

        return new MetadataHedgingStrategy(
            globalEndpointManager,
            () -> false,
            isPpafEnabled,
            enableMetadataHedgingForColdStart,
            DEFAULT_THRESHOLD,
            DEFAULT_PER_CLIENT_CONCURRENCY_BUDGET);
    }

    /**
     * Evaluate hedging eligibility for a single send attempt. Does NOT touch the concurrency
     * semaphore.
     */
    public MetadataHedgeEligibility evaluateEligibility(
        RxDocumentServiceRequest request,
        MetadataHedgingContext hedgeContext) {

        if (request == null) {
            throw new NullPointerException("request");
        }
        if (hedgeContext == null) {
            throw new NullPointerException("hedgeContext");
        }

        // A live strategy is only constructed when hedging is enabled (see createIfEnabled), so
        // customerOptIn is never false here in production -- defense-in-depth for direct construction.
        if (Boolean.FALSE.equals(this.customerOptIn)) {
            return MetadataHedgeEligibility.skip(MetadataHedgeSkipReason.OPT_IN_DISABLED);
        }

        if (this.isHedgingDisabledByGateway.getAsBoolean()) {
            return MetadataHedgeEligibility.skip(MetadataHedgeSkipReason.GATEWAY_KILL_SWITCH_ON);
        }

        // When the customer leaves the opt-in null, cold-start metadata hedging follows the live
        // PPAF state. An explicit opt-in of true bypasses this gate.
        if (!resolveOptIn(this.customerOptIn, this.isPpafEnabled.getAsBoolean())) {
            return MetadataHedgeEligibility.skip(MetadataHedgeSkipReason.PPAF_DISABLED);
        }

        if (!hedgeContext.isColdStart()) {
            return MetadataHedgeEligibility.skip(MetadataHedgeSkipReason.NOT_COLD_START);
        }

        if (hedgeContext.hasHedgedThisOperation()) {
            return MetadataHedgeEligibility.skip(MetadataHedgeSkipReason.ALREADY_HEDGED_THIS_OPERATION);
        }

        if (!isSupportedResource(request)) {
            return MetadataHedgeEligibility.skip(MetadataHedgeSkipReason.RESOURCE_TYPE_NOT_SUPPORTED);
        }

        if (request.getResourceType() == ResourceType.PartitionKeyRange && !hedgeContext.isFirstReadFeedPage()) {
            return MetadataHedgeEligibility.skip(MetadataHedgeSkipReason.NOT_FIRST_READ_FEED_PAGE);
        }

        List<RegionalRoutingContext> readEndpoints = this.globalEndpointManager.getReadEndpoints();
        if (readEndpoints == null || readEndpoints.size() <= 1) {
            return MetadataHedgeEligibility.skip(MetadataHedgeSkipReason.SINGLE_REGION);
        }

        List<RegionalRoutingContext> applicable =
            this.globalEndpointManager.getApplicableReadRegionalRoutingContexts(request);
        if (applicable == null || applicable.size() <= 1) {
            return MetadataHedgeEligibility.skip(MetadataHedgeSkipReason.EXCLUDED_REGION_LEAVES_NO_TARGET);
        }

        return MetadataHedgeEligibility.eligible();
    }

    /**
     * Execute a single metadata send with optional cross-region hedging. The strategy clones the
     * request per branch and routes each clone to its target region before delegating to the
     * supplied sender (which performs the actual store-model dispatch).
     */
    public Mono<MetadataHedgingResult> executeAsync(
        RxDocumentServiceRequest request,
        Function<RxDocumentServiceRequest, Mono<RxDocumentServiceResponse>> sender,
        MetadataHedgingContext hedgeContext) {

        if (request == null) {
            return Mono.error(new NullPointerException("request"));
        }
        if (sender == null) {
            return Mono.error(new NullPointerException("sender"));
        }
        if (hedgeContext == null) {
            return Mono.error(new NullPointerException("hedgeContext"));
        }

        return Mono.defer(() -> {
            MetadataHedgeDiagnostics diag =
                new MetadataHedgeDiagnostics(request.getResourceType().toString(), this.threshold.toMillis());

            MetadataHedgeEligibility eligibility = this.evaluateEligibility(request, hedgeContext);
            diag.setEligible(eligibility.isEligible());
            diag.setSkipReason(eligibility.getSkipReason());

            RegionalRoutingContext primaryEndpoint = this.primaryEndpoint(request);
            diag.setPrimaryRegion(this.safeGetRegion(primaryEndpoint));

            if (!eligibility.isEligible()) {
                logger.debug("Metadata hedge skipped (reason: {}, resourceType: {})",
                    diag.getSkipReason(), diag.getResourceType());
                return this.primaryOnly(request, primaryEndpoint, sender, hedgeContext, diag);
            }

            // Non-blocking concurrency budget check (equivalent to Wait(TimeSpan.Zero)).
            if (!this.hedgeBudget.tryAcquire()) {
                diag.setEligible(false);
                diag.setSkipReason(MetadataHedgeSkipReason.BUDGET_EXHAUSTED);
                logger.debug("Metadata hedge skipped (budget exhausted, resourceType: {})", diag.getResourceType());
                return this.primaryOnly(request, primaryEndpoint, sender, hedgeContext, diag);
            }

            RegionalRoutingContext hedgeEndpoint = this.resolveHedgeEndpoint(request, primaryEndpoint);
            if (hedgeEndpoint == null) {
                this.hedgeBudget.release();
                diag.setEligible(false);
                diag.setSkipReason(MetadataHedgeSkipReason.SINGLE_REGION);
                return this.primaryOnly(request, primaryEndpoint, sender, hedgeContext, diag);
            }

            diag.setHedgeRegion(this.safeGetRegion(hedgeEndpoint));
            if (primaryEndpoint != null) {
                hedgeContext.getAttemptedEndpoints().putIfAbsent(endpointKey(primaryEndpoint), (byte) 0);
            }

            AtomicBoolean budgetReleased = new AtomicBoolean(false);
            return this.raceWithHedge(request, sender, hedgeContext, diag, primaryEndpoint, hedgeEndpoint)
                .doFinally(signalType -> {
                    if (budgetReleased.compareAndSet(false, true)) {
                        this.hedgeBudget.release();
                    }
                });
        });
    }

    private Mono<MetadataHedgingResult> raceWithHedge(
        RxDocumentServiceRequest request,
        Function<RxDocumentServiceRequest, Mono<RxDocumentServiceResponse>> sender,
        MetadataHedgingContext hedgeContext,
        MetadataHedgeDiagnostics diag,
        RegionalRoutingContext primaryEndpoint,
        RegionalRoutingContext hedgeEndpoint) {

        long startNanos = System.nanoTime();

        Mono<HedgeOutcome> primaryOutcome =
            this.sendOutcome(sender, request, primaryEndpoint, HedgeBranch.PRIMARY).cache();

        // Fire the hedge when the threshold elapses OR when the primary settles as a regional
        // failure before the threshold (fail-fast hedging).
        Mono<Long> timerArm = Mono.delay(this.threshold).thenReturn(0L);
        Mono<Long> primaryFaultArm = primaryOutcome.flatMap(outcome ->
            outcome.isAcceptable() ? Mono.never() : Mono.just(0L));
        Mono<Long> hedgeTrigger = Mono.firstWithSignal(timerArm, primaryFaultArm);

        Mono<HedgeOutcome> hedgeOutcome = hedgeTrigger.flatMap(ignored -> {
            hedgeContext.getAttemptedEndpoints().putIfAbsent(endpointKey(hedgeEndpoint), (byte) 0);
            hedgeContext.tryMarkHedgedThisOperation();
            diag.setHedgeFired(true);
            diag.setHedgeFiredElapsedMs((System.nanoTime() - startNanos) / 1_000_000.0);
            logger.debug("Metadata hedge fired (primary: {}, hedge: {}, elapsedMs: {})",
                diag.getPrimaryRegion(), diag.getHedgeRegion(), diag.getHedgeFiredElapsedMs());
            return this.sendOutcome(sender, request, hedgeEndpoint, HedgeBranch.HEDGE);
        }).cache();

        Mono<HedgeOutcome> winner = Flux.merge(primaryOutcome, hedgeOutcome)
            .filter(HedgeOutcome::isAcceptable)
            .next()
            // Neither branch produced an acceptable winner: prefer the primary's outcome so the
            // metadata retry path classifies the failure normally (design &sect;10).
            .switchIfEmpty(primaryOutcome);

        return winner.flatMap(outcome -> {
            diag.setTotalAttempts(diag.isHedgeFired() ? 2 : 1);
            String winningRegion =
                outcome.getBranch() == HedgeBranch.PRIMARY ? diag.getPrimaryRegion() : diag.getHedgeRegion();
            diag.setWinningRegion(winningRegion);
            if (outcome.getBranch() == HedgeBranch.HEDGE) {
                diag.setHedgeOutcome("Won");
            }
            hedgeContext.recordWinner(outcome.getEndpoint());

            if (outcome.getError() != null) {
                return Mono.error(outcome.getError());
            }

            return Mono.just(new MetadataHedgingResult(
                outcome.getResponse(),
                outcome.getEndpoint(),
                winningRegion,
                diag.isHedgeFired(),
                diag));
        });
    }

    private Mono<MetadataHedgingResult> primaryOnly(
        RxDocumentServiceRequest request,
        RegionalRoutingContext primaryEndpoint,
        Function<RxDocumentServiceRequest, Mono<RxDocumentServiceResponse>> sender,
        MetadataHedgingContext hedgeContext,
        MetadataHedgeDiagnostics diag) {

        return this.sendOutcome(sender, request, primaryEndpoint, HedgeBranch.PRIMARY)
            .flatMap(outcome -> {
                diag.setTotalAttempts(1);
                diag.setWinningRegion(diag.getPrimaryRegion());
                hedgeContext.recordWinner(primaryEndpoint);
                if (outcome.getError() != null) {
                    return Mono.error(outcome.getError());
                }
                return Mono.just(new MetadataHedgingResult(
                    outcome.getResponse(), primaryEndpoint, diag.getPrimaryRegion(), false, diag));
            });
    }

    private Mono<HedgeOutcome> sendOutcome(
        Function<RxDocumentServiceRequest, Mono<RxDocumentServiceResponse>> sender,
        RxDocumentServiceRequest request,
        RegionalRoutingContext targetEndpoint,
        HedgeBranch branch) {

        return Mono.defer(() -> {
            // Clone per branch. The primary and hedge sends run concurrently; sharing one request
            // would let one branch overwrite the other's target region. clone() produces an
            // independent requestContext.
            RxDocumentServiceRequest branchRequest = request.clone();
            if (targetEndpoint != null) {
                branchRequest.requestContext.routeToLocation(targetEndpoint);
            }
            return sender.apply(branchRequest);
        })
            .map(response -> new HedgeOutcome(branch, targetEndpoint, response, null, isAcceptableWinner(response, branch)))
            .onErrorResume(error -> Mono.just(
                new HedgeOutcome(branch, targetEndpoint, null, error, isAcceptableWinner(error, branch))));
    }

    private RegionalRoutingContext primaryEndpoint(RxDocumentServiceRequest request) {
        if (request.requestContext != null && request.requestContext.regionalRoutingContextToRoute != null) {
            return request.requestContext.regionalRoutingContextToRoute;
        }
        return this.globalEndpointManager.resolveServiceEndpoint(request);
    }

    private RegionalRoutingContext resolveHedgeEndpoint(
        RxDocumentServiceRequest request,
        RegionalRoutingContext primaryEndpoint) {

        List<RegionalRoutingContext> applicable =
            this.globalEndpointManager.getApplicableReadRegionalRoutingContexts(request);
        if (applicable == null) {
            return null;
        }

        for (RegionalRoutingContext candidate : applicable) {
            if (candidate != null && !candidate.equals(primaryEndpoint)) {
                return candidate;
            }
        }
        return null;
    }

    private String safeGetRegion(RegionalRoutingContext endpoint) {
        if (endpoint == null || endpoint.getGatewayRegionalEndpoint() == null) {
            return null;
        }
        try {
            return this.globalEndpointManager.getRegionName(
                endpoint.getGatewayRegionalEndpoint(), OperationType.Read, false);
        } catch (Exception e) {
            return null;
        }
    }

    private static String endpointKey(RegionalRoutingContext endpoint) {
        URI uri = endpoint.getGatewayRegionalEndpoint();
        return uri != null ? uri.toString() : endpoint.toString();
    }

    private static boolean isSupportedResource(RxDocumentServiceRequest request) {
        return (request.getResourceType() == ResourceType.DocumentCollection
            && request.getOperationType() == OperationType.Read)
            || (request.getResourceType() == ResourceType.PartitionKeyRange
            && request.getOperationType() == OperationType.ReadFeed);
    }

    /**
     * Per-branch acceptable-winner predicate for a successful response. A 401 / plain 403 from the
     * hedge branch is rejected (design &sect;5.13).
     */
    public static boolean isAcceptableWinner(RxDocumentServiceResponse response, HedgeBranch branch) {
        if (response == null) {
            return false;
        }

        if (isRegionalFailure(response.getStatusCode(), getSubStatusCode(response))) {
            return false;
        }

        if (branch == HedgeBranch.HEDGE
            && (response.getStatusCode() == HttpConstants.StatusCodes.UNAUTHORIZED
            || response.getStatusCode() == HttpConstants.StatusCodes.FORBIDDEN)) {
            return false;
        }

        return true;
    }

    /**
     * Per-branch acceptable-winner predicate for a terminal error. A regional failure is not an
     * acceptable winner (let the other branch win); a hedge-branch 401 / plain 403 is rejected.
     * Any other terminal error is an acceptable winner so the metadata retry path classifies it.
     */
    public static boolean isAcceptableWinner(Throwable error, HedgeBranch branch) {
        if (error == null) {
            return false;
        }

        if (isRegionalFailure(error)) {
            return false;
        }

        CosmosException cosmosException = Utils.as(error, CosmosException.class);
        if (branch == HedgeBranch.HEDGE && cosmosException != null
            && (cosmosException.getStatusCode() == HttpConstants.StatusCodes.UNAUTHORIZED
            || cosmosException.getStatusCode() == HttpConstants.StatusCodes.FORBIDDEN)) {
            return false;
        }

        return true;
    }

    /**
     * Returns {@code true} iff the throwable represents a regional failure that should advance a
     * metadata retry/hedge to a different preferred location. Single source of truth shared by the
     * metadata retry path and the cold-start metadata hedging strategy (design &sect;5.7.2).
     */
    public static boolean isRegionalFailure(Throwable error) {
        if (error == null) {
            return false;
        }

        if (error instanceof Exception && WebExceptionUtility.isNetworkFailure((Exception) error)) {
            return true;
        }

        CosmosException cosmosException = Utils.as(error, CosmosException.class);
        if (cosmosException == null) {
            return false;
        }

        return isRegionalFailure(cosmosException.getStatusCode(), cosmosException.getSubStatusCode());
    }

    /**
     * Status/sub-status overlay of {@link #isRegionalFailure(Throwable)}: 503 / 500, 410 +
     * LeaseNotFound, 403 + DatabaseAccountNotFound.
     */
    public static boolean isRegionalFailure(int statusCode, int subStatusCode) {
        switch (statusCode) {
            case HttpConstants.StatusCodes.SERVICE_UNAVAILABLE:
            case HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR:
                return true;
            case HttpConstants.StatusCodes.GONE:
                return subStatusCode == HttpConstants.SubStatusCodes.LEASE_NOT_FOUND;
            case HttpConstants.StatusCodes.FORBIDDEN:
                return subStatusCode == HttpConstants.SubStatusCodes.DATABASE_ACCOUNT_NOTFOUND;
            default:
                return false;
        }
    }

    private static int getSubStatusCode(RxDocumentServiceResponse response) {
        if (response.getResponseHeaders() == null) {
            return HttpConstants.SubStatusCodes.UNKNOWN;
        }
        String subStatus = response.getResponseHeaders().get(HttpConstants.HttpHeaders.SUB_STATUS);
        if (subStatus == null) {
            return HttpConstants.SubStatusCodes.UNKNOWN;
        }
        try {
            return Integer.parseInt(subStatus);
        } catch (NumberFormatException e) {
            return HttpConstants.SubStatusCodes.UNKNOWN;
        }
    }

    /**
     * Captured outcome of one branch (primary or hedge): either a successful response or a
     * terminal error, plus whether it qualifies as an acceptable winner.
     */
    private static final class HedgeOutcome {
        private final HedgeBranch branch;
        private final RegionalRoutingContext endpoint;
        private final RxDocumentServiceResponse response;
        private final Throwable error;
        private final boolean acceptable;

        HedgeOutcome(
            HedgeBranch branch,
            RegionalRoutingContext endpoint,
            RxDocumentServiceResponse response,
            Throwable error,
            boolean acceptable) {
            this.branch = branch;
            this.endpoint = endpoint;
            this.response = response;
            this.error = error;
            this.acceptable = acceptable;
        }

        HedgeBranch getBranch() {
            return this.branch;
        }

        RegionalRoutingContext getEndpoint() {
            return this.endpoint;
        }

        RxDocumentServiceResponse getResponse() {
            return this.response;
        }

        Throwable getError() {
            return this.error;
        }

        boolean isAcceptable() {
            return this.acceptable;
        }
    }
}
