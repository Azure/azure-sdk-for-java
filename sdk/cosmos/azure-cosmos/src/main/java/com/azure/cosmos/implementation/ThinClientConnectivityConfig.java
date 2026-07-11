// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.ConnectionMode;

/**
 * Single source of truth for the thin-client (Gateway V2) enablement and connectivity-probe
 * enablement decisions.
 *
 * <p>All {@code COSMOS.THINCLIENT_*} / {@code COSMOS.HTTP2_ENABLED} inputs are evaluated
 * <em>lazily</em> on each call rather than snapshotted, so a System property or environment
 * variable changed dynamically at runtime is honored by every consumer. Only the immutable
 * {@link ConnectionPolicy} is held by reference; even its HTTP/2 "effectively enabled" state is
 * re-read on each call so a dynamic {@code COSMOS.HTTP2_ENABLED} change is picked up too.
 *
 * <p>Thin-client routing requires GATEWAY connection mode and HTTP/2 effectively enabled; it is
 * additionally controlled by {@code COSMOS.THINCLIENT_ENABLED}:
 * <ul>
 *   <li>explicit {@code true}  — thin-client enabled, probe skipped (hard opt-in).</li>
 *   <li>explicit {@code false} — thin-client disabled, probe skipped (hard opt-out).</li>
 *   <li>not set                — connectivity probe gates routing; thin-client is used only when
 *       the probe affirmatively greenlights the proxy, otherwise traffic stays on Gateway V1.</li>
 * </ul>
 * Direct connection mode is excluded because thin-client routing requires GATEWAY mode.
 */
public final class ThinClientConnectivityConfig {

    private final ConnectionPolicy connectionPolicy;

    /**
     * @param connectionPolicy the effective connection policy (mode + HTTP/2 config); held by
     * reference so mode / HTTP-2 state is read lazily on each evaluation.
     */
    public ThinClientConnectivityConfig(ConnectionPolicy connectionPolicy) {
        this.connectionPolicy = connectionPolicy;
    }

    /**
     * @return whether thin-client (Gateway V2) routing can be used. Requires GATEWAY mode, HTTP/2
     * effectively enabled, and {@code COSMOS.THINCLIENT_ENABLED} not set to {@code false}.
     * Evaluated lazily on each call.
     */
    public boolean canThinClientBeUsed() {
        return !Boolean.FALSE.equals(Configs.isThinClientEnabled())
            && this.connectionPolicy.getConnectionMode() == ConnectionMode.GATEWAY
            && this.connectionPolicy.getHttp2ConnectionConfig() != null
            && ImplementationBridgeHelpers.Http2ConnectionConfigHelper
                .getHttp2ConnectionConfigAccessor()
                .isEffectivelyEnabled(this.connectionPolicy.getHttp2ConnectionConfig());
    }

    /**
     * @return whether thin-client can be <em>implicitly</em> enabled for this client — thin-client
     * is usable and the customer did <em>not</em> set {@code COSMOS.THINCLIENT_ENABLED} at all
     * (the tri-state value is {@code null}). This is exactly when the connectivity-probe client
     * should be wired: thin-client is eligible but routing is gated on probe health, rather than
     * the customer having made a hard opt-in/opt-out decision. Evaluated lazily on each call.
     */
    public boolean canThinClientBeImplicitlyEnabled() {
        return canThinClientBeUsed()
            && Configs.isThinClientEnabled() == null;
    }

    /**
     * Per-request routing gate: decides whether a request should be routed through the thin-client
     * (Gateway V2) store model. Pure function of its inputs so it can be unit-tested without a live
     * client. The client-level signals are supplied by the caller:
     * <ul>
     *   <li>{@code canThinClientBeUsed} — see {@link #canThinClientBeUsed()};</li>
     *   <li>{@code hasThinClientReadLocations} — whether the service is still returning thin-client
     *       regional endpoints (a previously-eligible account can stop advertising them);</li>
     *   <li>{@code isThinClientEnabled} — the tri-state {@code COSMOS.THINCLIENT_ENABLED} value from
     *       {@link Configs#isThinClientEnabled()}: a non-{@code null} value is a hard contract that
     *       bypasses the connectivity-probe gate entirely ({@code TRUE} routes to Gateway V2,
     *       {@code FALSE} pins to Gateway V1 — though {@code FALSE} is already excluded upstream by
     *       {@code canThinClientBeUsed}). {@code null} means unset: defer to {@code proxyProbeDecision};</li>
     *   <li>{@code proxyProbeDecision} — the tri-state connectivity-probe verdict from
     *       {@link GlobalEndpointManager#getProxyProbeDecision()}: {@code TRUE} = proxy proven
     *       routable, so thin-client is used; {@code FALSE} = gate to Gateway V1; {@code null} =
     *       no verdict yet (probe not wired / not yet completed a cycle), also treated as
     *       "not routable" so traffic stays on Gateway V1 until the probe greenlights. Only
     *       consulted when {@code isThinClientEnabled} is {@code null}.</li>
     * </ul>
     * The remaining checks are request-level (operation type + resource type).
     */
    public static boolean shouldUseThinClientStoreModel(
        boolean canThinClientBeUsed,
        boolean hasThinClientReadLocations,
        Boolean isThinClientEnabled,
        Boolean proxyProbeDecision,
        RxDocumentServiceRequest request) {

        if (!canThinClientBeUsed
            || !hasThinClientReadLocations
            || !isThinClientEligibleRequest(request)) {
            return false;
        }

        // An explicit COSMOS.THINCLIENT_ENABLED setting is a hard contract that bypasses the
        // connectivity-probe gate entirely: TRUE routes straight to Gateway V2, FALSE pins to
        // Gateway V1 (FALSE is already filtered out upstream by canThinClientBeUsed).
        if (isThinClientEnabled != null) {
            return isThinClientEnabled;
        }

        // Unset (null): defer to the connectivity-probe verdict. Thin-client is used only when the
        // probe has affirmatively greenlit the proxy (verdict == TRUE). A null verdict (probe not
        // wired / not yet completed a cycle) or an explicit FALSE both pin traffic to Gateway V1 --
        // we never route to Gateway V2 without a positive probe result.
        return Boolean.TRUE.equals(proxyProbeDecision);
    }

    /**
     * @return whether the request's operation type and resource type are eligible for thin-client
     * routing. Only document point / query / batch / QueryPlan operations, incremental change feed,
     * and stored-procedure execution are proxied; metadata resources and all-versions-and-deletes
     * change feed continue through Gateway V1.
     */
    private static boolean isThinClientEligibleRequest(RxDocumentServiceRequest request) {
        if (request.getResourceType() != ResourceType.Document
            && !request.isExecuteStoredProcedureBasedRequest()) {
            return false;
        }

        OperationType operationType = request.getOperationType();
        return operationType.isPointOperation()
            || operationType == OperationType.Query
            || operationType == OperationType.Batch
            || (request.isChangeFeedRequest() && !request.isAllVersionsAndDeletesChangeFeedMode())
            || request.isExecuteStoredProcedureBasedRequest()
            || operationType == OperationType.QueryPlan;
    }
}
