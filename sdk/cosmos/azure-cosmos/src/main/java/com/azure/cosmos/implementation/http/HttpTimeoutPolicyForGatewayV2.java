// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Timeout policy for Gateway V2 (Thin Client) requests.
 * This policy applies to point read operations when Thin Client mode is enabled.
 */
public class HttpTimeoutPolicyForGatewayV2 extends HttpTimeoutPolicy {

    public static final HttpTimeoutPolicy INSTANCE = new HttpTimeoutPolicyForGatewayV2();

    private HttpTimeoutPolicyForGatewayV2() {
        timeoutAndDelaysList = getTimeoutList();
    }

    public List<ResponseTimeoutAndDelays> getTimeoutList() {
        return Collections.unmodifiableList(
            Arrays.asList(
                new ResponseTimeoutAndDelays(Duration.ofSeconds(5), Duration.ofMillis(500)),
                new ResponseTimeoutAndDelays(Duration.ofSeconds(10), Duration.ofSeconds(1)),
                new ResponseTimeoutAndDelays(Duration.ofSeconds(20), Duration.ZERO)));
    }
}
