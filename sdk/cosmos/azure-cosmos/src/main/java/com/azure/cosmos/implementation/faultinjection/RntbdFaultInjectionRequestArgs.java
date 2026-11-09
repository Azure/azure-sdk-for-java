// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;

import java.net.URI;
import java.util.Collections;
import java.util.List;

public class RntbdFaultInjectionRequestArgs extends FaultInjectionRequestArgs {
    public RntbdFaultInjectionRequestArgs(
        long transportRequestId,
        URI requestURI,
        boolean isPrimary,
        RxDocumentServiceRequest serviceRequest) {
        super(transportRequestId, requestURI, isPrimary, serviceRequest);
    }

    @Override
    public List<String> getPartitionKeyRangeIds() {
        // This method is used to check whether the request meets the partition scope in fault injection
        // however, for direct connection type, requestURI is being used for the above purpose
        // so we will always return an empty list here as it is not really being used in direct connection type
        return Collections.emptyList();
    }

    @Override
    public String getCollectionRid() {
        return this.getServiceRequest().requestContext.resolvedCollectionRid;
    }
}
