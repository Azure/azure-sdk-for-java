// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test;

import com.azure.resourcemanager.redis.models.RedisCache;
import org.junit.jupiter.api.Test;

public class EnumTest {

    @Test
    public void redisVersion() {
        RedisCache.RedisVersion version = RedisCache.RedisVersion.V4;
        System.out.println(version);
    }

}
