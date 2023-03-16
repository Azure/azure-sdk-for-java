package com.azure.cosmos.implementation.http;

import java.time.Duration;

public class ResponseTimeoutAndDelays {

    private final Duration responseTimeout;
    private final int delayForNextRequest;

    ResponseTimeoutAndDelays(Duration requestTimeout, int delayForNextRequest) {
        this.responseTimeout = requestTimeout;
        this.delayForNextRequest = delayForNextRequest;
    }

    public Duration getResponseTimeout() {
        return responseTimeout;
    }

    public int getDelayForNextRequest() {
        return delayForNextRequest;
    }

}
