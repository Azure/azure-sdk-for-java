// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.redis.implementation;

import com.azure.resourcemanager.redis.fluent.models.RedisAccessKeysInner;
import com.azure.resourcemanager.redis.models.RedisAccessKeys;
import com.azure.resourcemanager.redis.models.RedisCache;

/** The {@link RedisCache#keys} action result. */
class RedisAccessKeysImpl implements RedisAccessKeys {
    private RedisAccessKeysInner inner;

    /**
     * Creates an instance of the Redis Access keys result object.
     *
     * @param inner the inner object
     */
    RedisAccessKeysImpl(RedisAccessKeysInner inner) {
        this.inner = inner;
    }

    /** @return a pri,ary key value. */
    public String primaryKey() {
        return inner.primaryKey();
    }

    /** @return a secondary key value. */
    public String secondaryKey() {
        return inner.secondaryKey();
    }
}
