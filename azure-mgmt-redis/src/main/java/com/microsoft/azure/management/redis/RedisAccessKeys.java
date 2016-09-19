/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis;

/**
 * The {@link RedisCache#keys} action result.
 */
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
