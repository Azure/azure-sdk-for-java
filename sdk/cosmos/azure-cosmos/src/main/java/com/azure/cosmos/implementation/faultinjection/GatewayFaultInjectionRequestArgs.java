// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;

import java.net.URI;

public class GatewayFaultInjectionRequestArgs extends FaultInjectionRequestArgs {
    public GatewayFaultInjectionRequestArgs(
        long transportRequestId,
        URI requestURI,
        RxDocumentServiceRequest serviceRequest) {
        super(transportRequestId, requestURI, false, serviceRequest);
    }
}
