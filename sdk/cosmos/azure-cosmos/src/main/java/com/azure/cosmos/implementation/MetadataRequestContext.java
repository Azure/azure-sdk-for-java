// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.faultinjection.FaultInjectionRequestContext;

public class MetadataRequestContext {
    private final MetadataDiagnosticsContext metadataDiagnosticsContext;
    private final FaultInjectionRequestContext faultInjectionRequestContext;

    private MetadataRequestContext(
        MetadataDiagnosticsContext metadataDiagnosticsContext,
        FaultInjectionRequestContext faultInjectionRequestContext) {

        this.metadataDiagnosticsContext = metadataDiagnosticsContext;
        this.faultInjectionRequestContext = faultInjectionRequestContext;
    }

    public MetadataDiagnosticsContext getMetadataDiagnosticsContext() {
        return metadataDiagnosticsContext;
    }

    public FaultInjectionRequestContext getFaultInjectionRequestContext() {
        return faultInjectionRequestContext;
    }

    public static MetadataRequestContext getMetadataRequestContext(RxDocumentServiceRequest request) {
        if (request == null) {
            return null;
        }

        if (request.requestContext.cosmosDiagnostics != null) {
            return new MetadataRequestContext(
                ImplementationBridgeHelpers
                    .CosmosDiagnosticsHelper
                    .getCosmosDiagnosticsAccessor()
                    .getClientSideRequestStatisticsRaw(request.requestContext.cosmosDiagnostics)
                    .getMetadataDiagnosticsContext(),
                request.faultInjectionRequestContext
            );
        }

        return new MetadataRequestContext(null, request.faultInjectionRequestContext);
    }
}
