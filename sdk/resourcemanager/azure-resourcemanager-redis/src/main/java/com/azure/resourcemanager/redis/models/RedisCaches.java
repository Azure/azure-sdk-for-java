// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.redis.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.redis.RedisManager;
import com.azure.resourcemanager.redis.fluent.RedisClient;
import com.azure.resourcemanager.redis.fluent.inner.OperationInner;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/** Entry point for Redis Cache management API. */
@Fluent
public interface RedisCaches
    extends SupportsCreating<RedisCache.DefinitionStages.Blank>,
        SupportsListing<RedisCache>,
        SupportsListingByResourceGroup<RedisCache>,
        SupportsGettingByResourceGroup<RedisCache>,
        SupportsGettingById<RedisCache>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<RedisCache>,
        SupportsBatchDeletion,
        HasManager<RedisManager>,
        HasInner<RedisClient> {

    /**
     * Lists all of the available Redis REST API operations.
     *
     * @return list of available Redis REST operations.
     */
    PagedIterable<OperationInner> listOperations();

    /**
     * Lists all of the available Redis REST API operations.
     *
     * @return a representation of the future computation of this call
     */
    PagedFlux<OperationInner> listOperationsAsync();
}
