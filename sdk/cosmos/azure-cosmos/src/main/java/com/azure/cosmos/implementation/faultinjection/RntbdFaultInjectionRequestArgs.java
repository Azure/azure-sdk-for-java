// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;

import java.net.URI;

public class RntbdFaultInjectionRequestArgs extends FaultInjectionRequestArgs {
    public RntbdFaultInjectionRequestArgs(
        long transportRequestId,
        URI requestURI,
        boolean isPrimary,
        RxDocumentServiceRequest serviceRequest) {
        super(transportRequestId, requestURI, isPrimary, serviceRequest);
    }
}
