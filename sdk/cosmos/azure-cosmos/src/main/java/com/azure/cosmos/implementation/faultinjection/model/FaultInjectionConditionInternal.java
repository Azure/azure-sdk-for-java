// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection.model;

import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.Uri;

import java.util.List;

public class FaultInjectionConditionInternal {
    private final OperationType operationType;
    private final String region;
    private final String containerNameLink;
    private final List<Uri> physicalAddresses;

    public FaultInjectionConditionInternal(
        OperationType operationType,
        String region,
        String containerNameLink,
        List<Uri> physicalAddresses) {
        this.operationType = operationType;
        this.region = region;
        this.containerNameLink = containerNameLink;
        this.physicalAddresses = physicalAddresses;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public String getRegion() {
        return region;
    }

    public List<Uri> getPhysicalAddresses() {
        return physicalAddresses;
    }

    public boolean matches(RxDocumentServiceRequest documentServiceRequest) {
        // TO be implemented
        // Decides whether the requests match all the filters
        return true;
    }
}

