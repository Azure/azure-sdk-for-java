package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import io.netty.handler.codec.http.HttpMethod;
import reactor.core.publisher.Mono;

import static com.azure.cosmos.implementation.HttpConstants.StatusCodes.REQUEST_TIMEOUT;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpTimeoutPolicyDefault extends HttpTimeoutPolicy {

    public static HttpTimeoutPolicy instance = new HttpTimeoutPolicyDefault(false);
    public static HttpTimeoutPolicy instanceShouldThrow503OnTimeout = new HttpTimeoutPolicyDefault(true);

    public HttpTimeoutPolicyDefault(Boolean shouldThrow503OnTimeout) {
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

    // Assume that it is not safe to retry unless it is a get method.
    // Create and other operations could have succeeded even though a timeout occurred.
    @Override
    public Boolean isSafeToRetry(HttpMethod httpMethod) {
        return httpMethod == HttpMethod.GET;
    }

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
        timeoutAndDelays.add(new ResponseTimeoutAndDelays(Duration.ofSeconds(65), 0));
        timeoutAndDelays.add(new ResponseTimeoutAndDelays(Duration.ofSeconds(65), 1));
        timeoutAndDelays.add(new ResponseTimeoutAndDelays(Duration.ofSeconds(65), 0));
        return timeoutAndDelays;
    }
}
