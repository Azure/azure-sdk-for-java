package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import io.netty.handler.codec.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.cosmos.implementation.HttpConstants.StatusCodes.REQUEST_TIMEOUT;

public class HttpTimeoutPolicyControlPlaneHotPath extends HttpTimeoutPolicy {

    public static HttpTimeoutPolicy instance = new HttpTimeoutPolicyControlPlaneHotPath(false);
    public static HttpTimeoutPolicy instanceShouldThrow503OnTimeout = new HttpTimeoutPolicyControlPlaneHotPath(true);

    public HttpTimeoutPolicyControlPlaneHotPath(Boolean shouldThrow503OnTimeout) {
        this.shouldThrow503OnTimeout = shouldThrow503OnTimeout;
    }

    @Override
    public Duration maximumRetryTimeLimit() {
        return Duration.ofSeconds(Configs.getGatewayResponseTimeoutInSeconds());
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
        List<ResponseTimeoutAndDelays> timeoutAndDelays = new ArrayList<ResponseTimeoutAndDelays>();
        timeoutAndDelays.add(new ResponseTimeoutAndDelays(Duration.ofSeconds((long).5), 0));
        timeoutAndDelays.add(new ResponseTimeoutAndDelays(Duration.ofSeconds(5), 1));
        timeoutAndDelays.add(new ResponseTimeoutAndDelays(Duration.ofSeconds(10), 0));
        return timeoutAndDelays;
    }
}
