// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Timeout policy for Gateway V2 (Thin Client) requests.
 * This policy has separate configurations for point read operations vs query/change feed operations.
 */
public class HttpTimeoutPolicyForGatewayV2 extends HttpTimeoutPolicy {

    public static final HttpTimeoutPolicy INSTANCE_FOR_POINT_READ = new HttpTimeoutPolicyForGatewayV2(true);
    public static final HttpTimeoutPolicy INSTANCE_FOR_QUERY_AND_CHANGE_FEED = new HttpTimeoutPolicyForGatewayV2(false);

    private final boolean isPointRead;

    private HttpTimeoutPolicyForGatewayV2(boolean isPointRead) {
        this.isPointRead = isPointRead;
        this.timeoutAndDelaysList = getTimeoutList();
    }

    public List<ResponseTimeoutAndDelays> getTimeoutList() {
        if (this.isPointRead) {
            return Collections.unmodifiableList(
                Arrays.asList(
                    new ResponseTimeoutAndDelays(Duration.ofSeconds(6), Duration.ZERO),
                    new ResponseTimeoutAndDelays(Duration.ofSeconds(6), Duration.ZERO),
                    new ResponseTimeoutAndDelays(Duration.ofSeconds(10), Duration.ZERO)));
        } else {
            return Collections.unmodifiableList(
                Arrays.asList(
                    new ResponseTimeoutAndDelays(Duration.ofSeconds(6), Duration.ZERO),
                    new ResponseTimeoutAndDelays(Duration.ofSeconds(6), Duration.ZERO),
                    new ResponseTimeoutAndDelays(Duration.ofSeconds(10), Duration.ZERO)));
        }
    }
}
