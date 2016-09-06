/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis;

import com.microsoft.azure.management.redis.implementation.RedisAccessKeysInner;

/**
 * The {@link RedisCache#keys} action result.
 */
public class RedisAccessKeys {
    private RedisAccessKeysInner inner;

    /**
     * Creates an instance of the Redis Access keys result object.
     *
     * @param inner the inner object
     */
    public RedisAccessKeys(RedisAccessKeysInner inner) {
        this.inner = inner;
    }

    /**
     * @return a pri,ary key value.
     */
    public String primaryKey() {
        return inner.primaryKey();
    }

    /**
     * @return a secondary key value.
     */
    public String secondaryKey() {
        return inner.secondaryKey();
    }
}
