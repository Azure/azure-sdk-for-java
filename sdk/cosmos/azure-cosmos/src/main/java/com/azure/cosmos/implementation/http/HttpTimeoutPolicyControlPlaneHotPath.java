package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.Configs;
import io.netty.handler.codec.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.cosmos.implementation.HttpConstants.StatusCodes.REQUEST_TIMEOUT;

public class HttpTimeoutPolicyControlPlaneHotPath extends HttpTimeoutPolicy{

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
    public Iterator<ResponseTimeoutAndDelays> getTimeoutIterator() {
        return getTimeoutAndDelays().listIterator();
    }

    @Override
    public Boolean isSafeToRetry(HttpMethod httpMethod) {
        return true;
    }

    @Override
    public Boolean shouldRetryBasedOnResponse(HttpMethod requestHttpMethod, Mono<HttpResponse> responseMessage) {
        if (responseMessage == null) {
            return false;
        }

        final AtomicInteger statusCode = new AtomicInteger();
        responseMessage.flatMap(rm -> {
            statusCode.set(rm.statusCode());
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
        timeoutAndDelays.add(new ResponseTimeoutAndDelays(Duration.ofSeconds((long).5), Duration.ZERO));
        timeoutAndDelays.add(new ResponseTimeoutAndDelays(Duration.ofSeconds(5), Duration.ofSeconds(1)));
        timeoutAndDelays.add(new ResponseTimeoutAndDelays(Duration.ofSeconds(10), Duration.ZERO));
        return timeoutAndDelays;
    }
}
