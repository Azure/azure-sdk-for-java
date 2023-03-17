package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import io.netty.handler.codec.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

public abstract class HttpTimeoutPolicy {

    public static HttpTimeoutPolicy getTimeoutPolicy(RxDocumentServiceRequest request) {

        if (OperationType.QueryPlan.equals(request.getOperationType()) ||
            request.isAddressRefresh() ||
            request.getResourceType() == ResourceType.PartitionKeyRange) {
            return HttpTimeoutPolicyControlPlaneHotPath.instance;
        }

        return HttpTimeoutPolicyDefault.instance;
    }

    public abstract Duration maximumRetryTimeLimit();

    public abstract Integer totalRetryCount();

    public abstract List<ResponseTimeoutAndDelays> getTimeoutList();

    public abstract Boolean isSafeToRetry(HttpMethod httpMethod);

    public abstract Boolean shouldRetryBasedOnResponse(HttpMethod requestHttpMethod, Mono<RxDocumentServiceResponse> responseMessage);
}
