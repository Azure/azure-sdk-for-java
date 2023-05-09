// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import io.netty.handler.codec.http.HttpMethod;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HttpTimeoutPolicyControlPlaneRead extends HttpTimeoutPolicy {

    public static final HttpTimeoutPolicy INSTANCE = new HttpTimeoutPolicyControlPlaneRead();

    private HttpTimeoutPolicyControlPlaneRead() {
        timeoutAndDelaysList = getTimeoutList();
    }

    public List<ResponseTimeoutAndDelays> getTimeoutList() {
        return Collections.unmodifiableList(Arrays.asList(new ResponseTimeoutAndDelays(Duration.ofSeconds(5), 0),
            new ResponseTimeoutAndDelays(Duration.ofSeconds(10), 1),
            new ResponseTimeoutAndDelays(Duration.ofSeconds(20), 0)));
    }
}
