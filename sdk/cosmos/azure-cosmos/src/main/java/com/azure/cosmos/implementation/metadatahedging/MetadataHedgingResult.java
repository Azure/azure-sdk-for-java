// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.metadatahedging;

import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;

/**
 * Result of {@link MetadataHedgingStrategy#executeAsync}.
 * <p>
 * Java port of the .NET {@code MetadataHedgingStrategy.MetadataHedgingResult}
 * (Azure/azure-cosmos-dotnet-v3#5923).
 */
public final class MetadataHedgingResult {

    private final RxDocumentServiceResponse response;
    private final RegionalRoutingContext winningEndpoint;
    private final String winningRegion;
    private final boolean hedgeFired;
    private final MetadataHedgeDiagnostics diagnostics;

    public MetadataHedgingResult(
        RxDocumentServiceResponse response,
        RegionalRoutingContext winningEndpoint,
        String winningRegion,
        boolean hedgeFired,
        MetadataHedgeDiagnostics diagnostics) {
        this.response = response;
        this.winningEndpoint = winningEndpoint;
        this.winningRegion = winningRegion;
        this.hedgeFired = hedgeFired;
        this.diagnostics = diagnostics;
    }

    public RxDocumentServiceResponse getResponse() {
        return this.response;
    }

    public RegionalRoutingContext getWinningEndpoint() {
        return this.winningEndpoint;
    }

    public String getWinningRegion() {
        return this.winningRegion;
    }

    public boolean isHedgeFired() {
        return this.hedgeFired;
    }

    public MetadataHedgeDiagnostics getDiagnostics() {
        return this.diagnostics;
    }
}
