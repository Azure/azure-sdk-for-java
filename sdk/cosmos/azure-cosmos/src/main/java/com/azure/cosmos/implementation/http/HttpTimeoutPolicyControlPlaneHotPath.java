// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.Configs;
import io.netty.handler.codec.http.HttpMethod;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HttpTimeoutPolicyControlPlaneHotPath extends HttpTimeoutPolicy {

    public static final HttpTimeoutPolicy INSTANCE = new HttpTimeoutPolicyControlPlaneHotPath();

    private HttpTimeoutPolicyControlPlaneHotPath() {
    }

    @Override
    public Duration maximumRetryTimeLimit() {
        return Duration.ofSeconds(Configs.getHttpResponseTimeoutInSeconds());
    }

    @Override
    public List<ResponseTimeoutAndDelays> getTimeoutList() {
        return getTimeoutAndDelays();
    }

    @Override
    public boolean isSafeToRetry(HttpMethod httpMethod) {
        return true;
    }

    private List<ResponseTimeoutAndDelays> getTimeoutAndDelays() {
        return Collections.unmodifiableList(Arrays.asList(new ResponseTimeoutAndDelays(Duration.ofMillis(500), 0),
            new ResponseTimeoutAndDelays(Duration.ofSeconds(5), 1),
            new ResponseTimeoutAndDelays(Duration.ofSeconds(10), 0)));
    }
}
