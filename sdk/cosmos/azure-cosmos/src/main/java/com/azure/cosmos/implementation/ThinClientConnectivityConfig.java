// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.ConnectionMode;

/**
 * Single source of truth for thin-client (Gateway V2) enablement and connectivity-probe decisions.
 *
 * <p>All {@code COSMOS.THINCLIENT_*} / {@code COSMOS.HTTP2_ENABLED} inputs are read lazily on each
 * call (not snapshotted), so runtime changes to a System property/env var are honored. Only the
 * immutable {@link ConnectionPolicy} is held by reference, and its HTTP/2 "effectively enabled"
 * state is re-read each call too.
 *
 * <p>Thin-client routing requires GATEWAY mode + HTTP/2 effectively enabled, further gated by
 * {@code COSMOS.THINCLIENT_ENABLED}: {@code true} = hard opt-in (probe skipped), {@code false} =
 * hard opt-out, unset = the connectivity probe gates routing (Gateway V2 only on an affirmative
 * probe, else Gateway V1).
 */
public final class ThinClientConnectivityConfig {

    private final ConnectionPolicy connectionPolicy;

    /**
     * @param connectionPolicy effective connection policy (mode + HTTP/2); held by reference so its
     * state is read lazily on each evaluation.
     */
    public ThinClientConnectivityConfig(ConnectionPolicy connectionPolicy) {
        this.connectionPolicy = connectionPolicy;
    }

    /**
     * @return whether thin-client (Gateway V2) routing can be used: GATEWAY mode, HTTP/2 effectively
     * enabled, and {@code COSMOS.THINCLIENT_ENABLED} not {@code false}. Evaluated lazily.
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
     * Per-request routing gate for the thin-client (Gateway V2) store model. Pure function of its
     * inputs (unit-testable without a live client). Client-level signals supplied by the caller:
     * <ul>
     *   <li>{@code canThinClientBeUsed} — see {@link #canThinClientBeUsed()};</li>
     *   <li>{@code hasThinClientReadLocations} — whether the service still advertises thin-client
     *       regional endpoints;</li>
     *   <li>{@code isThinClientEnabled} — tri-state {@code COSMOS.THINCLIENT_ENABLED}: non-{@code null}
     *       is a hard contract bypassing the probe ({@code TRUE}→V2, {@code FALSE}→V1, already
     *       excluded upstream); {@code null} defers to {@code proxyProbeDecision};</li>
     *   <li>{@code proxyProbeDecision} — probe verdict ({@link GlobalEndpointManager#getProxyProbeDecision()}):
     *       {@code TRUE}=proxy routable; {@code FALSE}/{@code null}=stay on Gateway V1. Consulted only
     *       when {@code isThinClientEnabled} is {@code null}.</li>
     * </ul>
     * Remaining checks are request-level (operation + resource type).
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

        // Explicit setting is a hard contract bypassing the probe (TRUE→V2, FALSE→V1, FALSE already
        // filtered upstream).
        if (isThinClientEnabled != null) {
            return isThinClientEnabled;
        }

        // Unset: route to V2 only on an affirmative probe verdict; null/FALSE stay on Gateway V1.
        return Boolean.TRUE.equals(proxyProbeDecision);
    }

    /**
     * @return whether the request's operation + resource type are thin-client eligible. Only document
     * point/query/batch/QueryPlan, incremental change feed, and stored-proc execution are proxied;
     * metadata and all-versions-and-deletes change feed stay on Gateway V1.
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
