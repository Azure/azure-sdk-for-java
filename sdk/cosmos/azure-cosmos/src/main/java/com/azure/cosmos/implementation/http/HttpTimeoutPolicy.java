package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import io.netty.handler.codec.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Iterator;

public abstract class HttpTimeoutPolicy {
    public Boolean shouldThrow503OnTimeout = false;

    public static HttpTimeoutPolicy getTimeoutPolicy(RxDocumentServiceRequest request) {
        if (request.getResourceType() == ResourceType.Document && request.getOperationType() == OperationType.QueryPlan) {
            return HttpTimeoutPolicyControlPlaneHotPath.instanceShouldThrow503OnTimeout;
        }

        if (request.getResourceType() == ResourceType.PartitionKeyRange) {
            return HttpTimeoutPolicyControlPlaneHotPath.instance;
        }

        if (!isMetaData(request) && request.isReadOnlyRequest()) {
            return HttpTimeoutPolicyDefault.instanceShouldThrow503OnTimeout;
        }

        if (isMetaData(request) && request.isReadOnlyRequest()) {
            return HttpTimeoutPolicyDefault.instanceShouldThrow503OnTimeout;
        }

        return HttpTimeoutPolicyDefault.instance;
    }

    private static Boolean isMetaData(RxDocumentServiceRequest request) {
        return (request.getOperationType() != OperationType.ExecuteJavaScript &&
            request.getResourceType() == ResourceType.StoredProcedure) ||
            request.getResourceType() != ResourceType.Document;
    }

    public abstract Duration maximumRetryTimeLimit();

    public abstract Integer totalRetryCount();

    public abstract Iterator<ResponseTimeoutAndDelays> getTimeoutIterator();

    public abstract Boolean isSafeToRetry(HttpMethod httpMethod);

    public abstract Boolean shouldRetryBasedOnResponse(HttpMethod requestHttpMethod, Mono<HttpResponse> responseMessage);
}
