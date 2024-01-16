// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.Configs;
import io.netty.handler.codec.http.HttpMethod;


import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HttpTimeoutPolicyDefault extends HttpTimeoutPolicy {

    public static final HttpTimeoutPolicy INSTANCE = new HttpTimeoutPolicyDefault();

    private HttpTimeoutPolicyDefault() {
        timeoutAndDelaysList = getTimeoutList();
    }

    public List<ResponseTimeoutAndDelays> getTimeoutList() {
        int maxTimeout = Configs.getMaxHttpRequestTimeout();
        return Collections.unmodifiableList(Arrays.asList(
            new ResponseTimeoutAndDelays(Duration.ofSeconds(maxTimeout), 0),
            new ResponseTimeoutAndDelays(Duration.ofSeconds(maxTimeout), 1),
            new ResponseTimeoutAndDelays(Duration.ofSeconds(maxTimeout), 0)));
    }
}
