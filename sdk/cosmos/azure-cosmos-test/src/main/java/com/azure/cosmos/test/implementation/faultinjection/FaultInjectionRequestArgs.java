// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.implementation.faultinjection;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;

import java.net.URI;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class FaultInjectionRequestArgs {
    private final long transportRequestId;
    private final URI requestUri;
    private final RxDocumentServiceRequest serviceRequest;
    private final FaultInjectionConnectionType connectionType;
    private boolean isPrimary;

    public FaultInjectionRequestArgs(
        long transportRequestId,
        URI requestUri,
        boolean isPrimary,
        RxDocumentServiceRequest serviceRequest,
        FaultInjectionConnectionType connectionType) {

        checkNotNull(requestUri, "Argument 'requestUri' can not null");
        checkNotNull(serviceRequest, "Argument 'serviceRequest' can not be null");

        this.transportRequestId = transportRequestId;
        this.requestUri = requestUri;
        this.isPrimary = isPrimary;
        this.serviceRequest = serviceRequest;
        this.connectionType = connectionType;
    }

    public long getTransportRequestId() {
        return transportRequestId;
    }

    public URI getRequestUri() {
        return requestUri;
    }

    public RxDocumentServiceRequest getServiceRequest() {
        return serviceRequest;
    }

    public FaultInjectionConnectionType getConnectionType() {
        return connectionType;
    }

    public boolean isPrimary() {
        return isPrimary;
    }
}
