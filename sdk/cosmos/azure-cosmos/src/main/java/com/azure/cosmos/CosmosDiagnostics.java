// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.diagnostics.ICosmosDiagnostics;
import com.azure.cosmos.implementation.query.QueryInfo;
import com.azure.cosmos.util.Beta;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class represents response diagnostic statistics associated with a request to Azure Cosmos DB
 */
public final class CosmosDiagnostics {
    private ICosmosDiagnostics internalCosmosDiagnostics;

    CosmosDiagnostics(ICosmosDiagnostics internalCosmosDiagnostics) {
        this.internalCosmosDiagnostics = internalCosmosDiagnostics;
    }

    /**
     * Retrieves Response Diagnostic String
     *
     * @return Response Diagnostic String
     */
    @Override
    public String toString() {
        return this.internalCosmosDiagnostics.toString();
    }

    /**
     * Retrieves duration related to the completion of the request.
     * This represents end to end duration of an operation including all the retries.
     * This is meant for point operation only, for query please use toString() to get full query diagnostics.
     *
     * @return request completion duration
     */
    public Duration getDuration() {
        return this.internalCosmosDiagnostics.getDuration();
    }

    /**
     * Regions contacted for this request
     *
     * @return set of regions contacted for this request
     */
    @Beta(value = Beta.SinceVersion.V4_9_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    @Deprecated
    public Set<URI> getRegionsContacted() {
        return this.internalCosmosDiagnostics.getRegionsContacted();
    }

    /**
     * Regions contacted for this request
     *
     * @return set of regions contacted for this request
     */
    @Beta(value = Beta.SinceVersion.V4_22_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Set<String> getContactedRegionNames() {
        return this.getContactedRegionNames();
    }

    AtomicBoolean isDiagnosticsCapturedInPagedFlux(){
        return this.internalCosmosDiagnostics.isDiagnosticsCapturedInPagedFlux();
    }

    List<ClientSideRequestStatistics> getClientSideRequestDiagnosticsList() {
        return this.internalCosmosDiagnostics.getClientSideRequestDiagnosticsList();
    }

    void setQueryPlanDiagnosticsContext(QueryInfo.QueryPlanDiagnosticsContext queryPlanDiagnosticsContext) {
        this.internalCosmosDiagnostics.setQueryPlanDiagnosticsContext(queryPlanDiagnosticsContext);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////

    static {
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.setCosmosDiagnosticsAccessor(
            new ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor() {
                @Override
                public AtomicBoolean isDiagnosticsCapturedInPagedFlux(CosmosDiagnostics cosmosDiagnostics) {
                    return cosmosDiagnostics.isDiagnosticsCapturedInPagedFlux();
                }

                @Override
                public List<ClientSideRequestStatistics> getClientSideRequestDiagnosticsList(CosmosDiagnostics cosmosDiagnostics) {
                    return cosmosDiagnostics.getClientSideRequestDiagnosticsList();
                }
            });
    }
}
