// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

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
        return Collections.unmodifiableList(Arrays.asList(new ResponseTimeoutAndDelays(Duration.ofSeconds(60), 0),
            new ResponseTimeoutAndDelays(Duration.ofSeconds(60), 1),
            new ResponseTimeoutAndDelays(Duration.ofSeconds(60), 0)));
    }
}
