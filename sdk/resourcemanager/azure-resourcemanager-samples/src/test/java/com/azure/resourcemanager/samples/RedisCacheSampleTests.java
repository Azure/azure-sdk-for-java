/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.samples;

import com.microsoft.azure.management.rediscache.samples.ManageRedisCache;
import org.junit.Assert;
import org.junit.Test;

public class RedisCacheSampleTests extends SamplesTestBase {
    @Test
    public void testManageRedisCache() {
        Assert.assertTrue(ManageRedisCache.runSample(azure));
    }
}
