package com.azure.cosmos.implementation.http;

import java.time.Duration;

public class ResponseTimeoutAndDelays {

    private final Duration responseTimeout;
    private final Duration delayForNextRequest;

    ResponseTimeoutAndDelays(Duration requestTimeout, Duration delayForNextRequest) {
        this.responseTimeout = requestTimeout;
        this.delayForNextRequest = delayForNextRequest;
    }

    public Duration getResponseTimeout() {
        return responseTimeout;
    }

    public Duration getDelayForNextRequest() {
        return delayForNextRequest;
    }

}
