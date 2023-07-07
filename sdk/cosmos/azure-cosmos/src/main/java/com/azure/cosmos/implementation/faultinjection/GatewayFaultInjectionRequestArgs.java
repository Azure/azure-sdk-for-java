// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.implementation.ResourceId;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.net.URI;
import java.util.List;

public class GatewayFaultInjectionRequestArgs extends FaultInjectionRequestArgs {
    private final List<String> partitionKeyRangeIds;

    public GatewayFaultInjectionRequestArgs(
        long transportRequestId,
        URI requestURI,
        RxDocumentServiceRequest serviceRequest,
        List<String> partitionKeyRangeIds) {

        super(transportRequestId, requestURI, false, serviceRequest);
        this.partitionKeyRangeIds = partitionKeyRangeIds;
    }

    @Override
    public List<String> getPartitionKeyRangeIds() {
        return this.partitionKeyRangeIds;
    }

    @Override
    public String getCollectionRid() {
        if (this.getServiceRequest().getIsNameBased()) {
            return this.getServiceRequest().requestContext.resolvedCollectionRid;
        }

        if (StringUtils.isNotEmpty(this.getServiceRequest().getResourceId())) {
            return ResourceId.parse(this.getServiceRequest().getResourceId()).getDocumentCollectionId().toString();
        }
        return Strings.Emtpy;
    }
}
