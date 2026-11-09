// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import java.time.Duration;

public class ResponseTimeoutAndDelays {

    private final Duration responseTimeout;
    private final int delayForNextRequestInSeconds;
    private final Duration delayForNextRequest;

    ResponseTimeoutAndDelays(Duration requestTimeout, int delayForNextRequest) {
        this.responseTimeout = requestTimeout;
        this.delayForNextRequestInSeconds = delayForNextRequest;
        this.delayForNextRequest = Duration.ofSeconds(delayForNextRequest);
    }

    ResponseTimeoutAndDelays(Duration requestTimeout, Duration delayForNextRequest) {
        this.responseTimeout = requestTimeout;
        this.delayForNextRequest = delayForNextRequest;
        this.delayForNextRequestInSeconds = (int) delayForNextRequest.getSeconds();
    }

    public Duration getResponseTimeout() {
        return responseTimeout;
    }

    public int getDelayForNextRequestInSeconds() {
        return delayForNextRequestInSeconds;
    }

    public Duration getDelayForNextRequest() {
        return delayForNextRequest;
    }

}
