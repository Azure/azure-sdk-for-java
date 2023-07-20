// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import io.netty.handler.codec.http.HttpMethod;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HttpTimeoutPolicyControlPlaneHotPath extends HttpTimeoutPolicy {

    public static final HttpTimeoutPolicy INSTANCE = new HttpTimeoutPolicyControlPlaneHotPath();

    private HttpTimeoutPolicyControlPlaneHotPath() {
        timeoutAndDelaysList = getTimeoutList();
    }

    public List<ResponseTimeoutAndDelays> getTimeoutList() {
        return Collections.unmodifiableList(Arrays.asList(new ResponseTimeoutAndDelays(Duration.ofMillis(500), 0),
            new ResponseTimeoutAndDelays(Duration.ofSeconds(5), 1),
            new ResponseTimeoutAndDelays(Duration.ofSeconds(10), 0)));
    }
}
