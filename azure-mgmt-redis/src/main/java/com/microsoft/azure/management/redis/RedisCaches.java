/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis;

import com.microsoft.azure.management.resources.fluentcore.arm.collection.*;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 * Entry point for storage accounts management API.
 */
public interface RedisCaches extends
        SupportsCreating<RedisCache.DefinitionStages.Blank>,
        SupportsListing<RedisCache>,
        SupportsListingByGroup<RedisCache>,
        SupportsGettingByGroup<RedisCache>,
        SupportsGettingById<RedisCache>,
        SupportsDeleting,
        SupportsDeletingByGroup,
        SupportsBatchCreation<RedisCache> {
}
