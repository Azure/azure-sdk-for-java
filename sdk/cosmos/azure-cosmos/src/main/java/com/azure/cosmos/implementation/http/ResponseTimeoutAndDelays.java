package com.azure.cosmos.implementation.http;

import java.time.Duration;

public class ResponseTimeoutAndDelays {

    private static Duration responseTimeout;
    private static Duration delayForNextRequest;

    ResponseTimeoutAndDelays(Duration requestTimeout, Duration delayForNextRequest) {
        this.responseTimeout = requestTimeout;
        this.delayForNextRequest = delayForNextRequest;
    }

    public Duration getResponseTimeout() {
        return responseTimeout;
    }

    public static void setResponseTimeout(Duration responseTimeout) {
        ResponseTimeoutAndDelays.responseTimeout = responseTimeout;
    }

    public Duration getDelayForNextRequest() {
        return delayForNextRequest;
    }

    public static void setDelayForNextRequest(Duration delayForNextRequest) {
        ResponseTimeoutAndDelays.delayForNextRequest = delayForNextRequest;
    }
}
