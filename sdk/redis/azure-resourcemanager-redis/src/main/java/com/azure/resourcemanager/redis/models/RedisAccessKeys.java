// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.redis.models;

import com.azure.core.annotation.Fluent;

/** The <code>RedisCache.keys()</code> action result. */
@Fluent
public interface RedisAccessKeys {
    /**
     * Gets a primary key value.
     *
     * @return a primary key value.
     */
    String primaryKey();

    /**
     * Gets a secondary key value.
     *
     * @return a secondary key value.
     */
    String secondaryKey();
}
