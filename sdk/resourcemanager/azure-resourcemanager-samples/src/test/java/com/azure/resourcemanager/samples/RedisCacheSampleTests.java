// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.resourcemanager.rediscache.samples.ManageRedisCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RedisCacheSampleTests extends SamplesTestBase {
    @Test
    public void testManageRedisCache() {
        Assertions.assertTrue(ManageRedisCache.runSample(azure));
    }
}
