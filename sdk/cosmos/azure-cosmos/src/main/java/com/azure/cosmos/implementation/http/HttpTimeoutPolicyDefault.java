// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import io.netty.handler.codec.http.HttpMethod;


import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HttpTimeoutPolicyDefault extends HttpTimeoutPolicy {

    public static final HttpTimeoutPolicy INSTANCE = new HttpTimeoutPolicyDefault();

    private HttpTimeoutPolicyDefault() {
    }

    @Override
    public List<ResponseTimeoutAndDelays> getTimeoutList() {
        return Collections.unmodifiableList(Arrays.asList(new ResponseTimeoutAndDelays(Duration.ofSeconds(65), 0),
            new ResponseTimeoutAndDelays(Duration.ofSeconds(65), 1),
            new ResponseTimeoutAndDelays(Duration.ofSeconds(65), 0)));
    }

    // Assume that it is not safe to retry unless it is a get method.
    // Create and other operations could have succeeded even though a timeout occurred.
    @Override
    public boolean isSafeToRetry(HttpMethod httpMethod) {
        return httpMethod == HttpMethod.GET;
    }
}
