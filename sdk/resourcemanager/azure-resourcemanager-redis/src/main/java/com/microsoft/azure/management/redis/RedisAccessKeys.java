/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis;

import com.microsoft.azure.management.apigeneration.Fluent;

/**
 * The <code>RedisCache.keys()</code> action result.
 */
@Fluent
public interface RedisAccessKeys {
    /**
     * @return a primary key value.
     */
    String primaryKey();

    /**
     * @return a secondary key value.
     */
    String secondaryKey();
}
