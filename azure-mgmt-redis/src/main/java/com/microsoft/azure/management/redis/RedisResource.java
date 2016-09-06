/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis;

import com.microsoft.azure.management.redis.implementation.RedisResourceWithAccessKeyInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.*;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * An immutable client-side representation of an Azure Redis cache.
 */
public interface RedisResource extends
        GroupableResource,
        Refreshable<RedisResource>,
        Updatable<RedisResource.Update>,
        Wrapper<RedisResourceWithAccessKeyInner> {

    Sku sku();

    // Map<String, String> redisConfiguration();


    /**************************************************************
     * Fluent interfaces to provision a StorageAccount
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the storage account definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the storage account definition.
         */
        interface Blank extends DefinitionWithRegion<WithGroup> {
        }

        /**
         * A storage account definition allowing resource group to be set.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithSku> {
        }

        /**
         * A storage account definition allowing the sku to be set.
         */
        interface WithSku {
            /**
             * Specifies the sku of the storage account. This used to be called
             * account types.
             *
             * @param skuName the sku
             * @return the next stage of storage account definition
             */
            WithCreate withSku(SkuName skuName, SkuFamily skuFamily);

            WithCreate withSku(SkuName skuName, SkuFamily skuFamily, int capacity);
        }

        /**
         * A storage account definition with sufficient inputs to create a new
         * storage account in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
            Creatable<RedisResource>,
            DefinitionStages.WithSku,
            DefinitionWithTags<WithCreate> {
        }
    }

    /**
     * Grouping of all the storage account update stages.
     */
    interface UpdateStages {
        /**
         * A storage account update stage allowing to change the parameters.
         */
        interface WithSku {
            /**
             * Specifies the sku of the storage account. This used to be called
             * account types.             *
             * @param skuName the sku
             * @return the next stage of storage account update
             */
            Update withSku(SkuName skuName, SkuFamily skuFamily);

            Update withSku(SkuName skuName, SkuFamily skuFamily, int capacity);
        }
    }

    /**
     * The template for a storage account update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<RedisResource>,
            UpdateStages.WithSku,
            Resource.UpdateWithTags<Update> {
    }
}

