// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.utils;

import com.azure.core.http.HttpHeaders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class ResourceManagerThrottlingInfoTests {

    @Test
    public void canCalculateThrottlingInfo() {
        String resourceHeaderValue = "Microsoft.Compute/PutVM3Min;237,Microsoft.Compute/PutVM30Min;1197";
        HttpHeaders headers = new HttpHeaders()
            .set("x-ms-ratelimit-remaining-subscription-writes", "1193")
            .set("x-ms-ratelimit-remaining-resource", resourceHeaderValue);

        ResourceManagerThrottlingInfo info = ResourceManagerThrottlingInfo.fromHeaders(headers);
        Assertions.assertEquals(resourceHeaderValue, info.getResourceRateLimit());
        Assertions.assertEquals(237, info.getRateLimit().orElse(0));

        Map<String, String> rateLimits = info.getRateLimits();
        Assertions.assertEquals("1193", rateLimits.get("x-ms-ratelimit-remaining-subscription-writes"));
        Assertions.assertEquals("237", rateLimits.get("x-ms-ratelimit-remaining-resource-PutVM3Min"));
        Assertions.assertEquals("1197", rateLimits.get("x-ms-ratelimit-remaining-resource-PutVM30Min"));
    }
}
