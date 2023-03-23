// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import io.netty.handler.codec.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.cosmos.implementation.HttpConstants.StatusCodes.REQUEST_TIMEOUT;

public class HttpTimeoutPolicyControlPlaneHotPath extends HttpTimeoutPolicy {

    final public static HttpTimeoutPolicy instance = new HttpTimeoutPolicyControlPlaneHotPath();

    public HttpTimeoutPolicyControlPlaneHotPath() {
    }

    @Override
    public Duration maximumRetryTimeLimit() {
        return Duration.ofSeconds(Configs.getHttpResponseTimeoutInSeconds());
    }

    @Override
    public Integer totalRetryCount() {
        return getTimeoutAndDelays().size();
    }

    @Override
    public List<ResponseTimeoutAndDelays> getTimeoutList() {
        return getTimeoutAndDelays();
    }

    @Override
    public Boolean isSafeToRetry(HttpMethod httpMethod) {
        return true;
    }

    // The hot path should always be safe to retires since it should be retrieving meta data
    // information that is not idempotent.
    @Override
    public Boolean shouldRetryBasedOnResponse(HttpMethod requestHttpMethod, Mono<RxDocumentServiceResponse> responseMessage) {
        if (responseMessage == null) {
            return false;
        }

        final AtomicInteger statusCode = new AtomicInteger();
        responseMessage.flatMap(rm -> {
            statusCode.set(rm.getStatusCode());
            return Mono.empty();
        });
        if (statusCode.get() != REQUEST_TIMEOUT) {
            return false;
        }

        if (!this.isSafeToRetry(requestHttpMethod)) {
            return false;
        }
        return true;
    }

    private List<ResponseTimeoutAndDelays> getTimeoutAndDelays() {
        return List.of(new ResponseTimeoutAndDelays(Duration.ofSeconds((long).5), 0),
            new ResponseTimeoutAndDelays(Duration.ofSeconds(5), 1),
            new ResponseTimeoutAndDelays(Duration.ofSeconds(10), 0));
    }
}
