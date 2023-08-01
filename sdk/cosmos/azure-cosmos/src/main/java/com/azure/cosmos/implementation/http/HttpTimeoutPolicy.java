// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import io.netty.handler.codec.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public abstract class HttpTimeoutPolicy {
    public List<ResponseTimeoutAndDelays> timeoutAndDelaysList;

    public static final HttpTimeoutPolicy getTimeoutPolicy(RxDocumentServiceRequest request) {
        if (OperationType.QueryPlan.equals(request.getOperationType()) ||
            request.isAddressRefresh() ||
            request.getResourceType() == ResourceType.PartitionKeyRange) {
            return HttpTimeoutPolicyControlPlaneHotPath.INSTANCE;
        }
        if (OperationType.Read.equals(request.getOperationType()) && request.getResourceType() == ResourceType.DatabaseAccount) {
            return HttpTimeoutPolicyControlPlaneRead.INSTANCE;
        }
        return HttpTimeoutPolicyDefault.INSTANCE;
    }

    public int totalRetryCount() {
        return timeoutAndDelaysList.size();
    }

    public long maximumRetryTimeLimit() { return Configs.getHttpResponseTimeoutInSeconds(); };

    public List<ResponseTimeoutAndDelays> getTimeoutAndDelaysList() {
        return timeoutAndDelaysList;
    }
}
