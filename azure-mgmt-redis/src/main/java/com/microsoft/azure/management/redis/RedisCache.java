/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.redis.implementation.RedisResourceInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * An immutable client-side representation of an Azure Redis cache.
 */
public interface RedisCache extends
        GroupableResource,
        Refreshable<RedisCache>,
        Updatable<RedisCache.Update>,
        Wrapper<RedisResourceInner> {

    RedisCachePremium asPremium();

    /**
     * @return the provisioningState value
     */
    String provisioningState();

    /**
     * @return the hostName value
     */
    String hostName();

    /**
     * @return the port value
     */
    int port();

    /**
     * @return the sslPort value
     */
    int sslPort();

    /**
     * @return the redisVersion value
     */
    String redisVersion();

    /**
     * @return the sku value
     */
    Sku sku();

    /**
     * @return the redisConfiguration value
     */
    Map<String, String> redisConfiguration();

    /**
     * @return the enableNonSslPort value
     */
    Boolean enableNonSslPort();

    /**
     * @return the tenantSettings value
     */
    Map<String, String> tenantSettings();

    /**
     * @return the shardCount value
     */
    Integer shardCount();

    /**
     * @return the subnetId value
     */
    String subnetId();

    /**
     * @return the staticIP value
     */
    String staticIP();

    /**
     * @return a redis cache's access keys. This operation requires write permission to the cache resource.
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     */
    RedisAccessKeys keys() throws CloudException, IOException, IllegalArgumentException;


    /**
     * Fetch the up-to-date access keys from Azure for this redis cache.
     *
     * @return the access keys for this redis cache
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     */
    RedisAccessKeys refreshKeys() throws CloudException, IOException;

    /**
     * Regenerates the access keys for this redis cache.
     *
     * @param keyType key type to regenerate
     * @return the generated access keys for this redis cache
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     */
    RedisAccessKeys regenerateKey(RedisKeyType keyType) throws CloudException, IOException;
    /**************************************************************
     * Fluent interfaces to provision a RedisCache
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
     * Grouping of all the Redis Cache definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the Redis Cache definition.
         */
        interface Blank extends DefinitionWithRegion<WithGroup> {
        }

        /**
         * A Redis Cache definition allowing resource group to be set.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithSku> {
        }

        /**
         * A Redis Cache definition allowing the sku to be set.
         */
        interface WithSku {
            /**
             * Specifies the sku of the Redis Cache. This used to be called
             * account types.
             *
             * @param skuName the sku
             * @return the next stage of Redis Cache definition
             */
            WithCreate withSku(SkuName skuName, SkuFamily skuFamily);

            WithCreate withSku(SkuName skuName, SkuFamily skuFamily, int capacity);
        }

        /**
         * A Redis Cache definition with sufficient inputs to create a new
         * Redis Cache in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
            Creatable<RedisCache>,
            DefinitionStages.WithSku,
            DefinitionWithTags<WithCreate> {

            WithCreate withNonSslPortEnabled();

            WithCreate withNonSslPortDisabled();

            WithCreate withRedisConfiguration(Map<String,String> redisConfiguration);

            WithCreate withTenantSettings(Map<String,String> tenantSettings);

            WithCreate withSubnetId(String subnetId);

            WithCreate withStaticIP(String staticIP);

            WithCreate withShardCount(int shardCount);
        }
    }

    /**
     * Grouping of all the Redis Cache update stages.
     */
    interface UpdateStages {
        /**
         * A Redis Cache update stage allowing to change the parameters.
         */
        interface WithSku {
            /**
             * Specifies the sku of the Redis Cache. This used to be called
             * account types.             *
             * @param skuName the sku
             * @return the next stage of Redis Cache update
             */
            Update withSku(SkuName skuName, SkuFamily skuFamily);

            Update withSku(SkuName skuName, SkuFamily skuFamily, int capacity);
        }

        interface WithNonSslPort {

            Update withNonSslPortEnabled();

            Update withNonSslPortDisabled();
        }
    }

    /**
     * The template for a Redis Cache update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<RedisCache>,
            Resource.UpdateWithTags<Update>,
            UpdateStages.WithSku,
            UpdateStages.WithNonSslPort {

        Update withRedisConfiguration(Map<String,String> redisConfiguration);

        Update withTenantSettings(Map<String,String> tenantSettings);

        Update withSubnetId(String subnetId);

        Update withStaticIP(String staticIP);

        Update withShardCount(int shardCount);
    }
}

