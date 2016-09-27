/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis;

import org.joda.time.Period;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.redis.implementation.RedisResourceInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import java.util.List;
import java.util.Map;

/**
 * An immutable client-side representation of an Azure Redis Cache.
 */
@Fluent
public interface RedisCache extends
        GroupableResource,
        Refreshable<RedisCache>,
        Updatable<RedisCache.Update>,
        Wrapper<RedisResourceInner> {

    /**
     * @return exposes features available only to Premium Sku Redis Cache instances.
     */
    RedisCachePremium asPremium();

    /**
     * @return returns true if current Redis Cache instance has Premium Sku.
     */
    boolean isPremium();

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
     * @return the Redis version value
     */
    String redisVersion();

    /**
     * @return the sku value
     */
    Sku sku();

    /**
     * @return the Redis configuration value
     */
    Map<String, String> redisConfiguration();

    /**
     * @return true if non SSL port is enabled, false otherwise
     */
    boolean nonSslPort();

    /**
     * @return the shardCount value
     */
    int shardCount();

    /**
     * @return the subnetId value
     */
    String subnetId();

    /**
     * @return the staticIP value
     */
    String staticIP();

    /**
     * @return a Redis Cache's access keys. This operation requires write permission to the Cache resource.
     */
    RedisAccessKeys keys();

    /**
     * Fetch the up-to-date access keys from Azure for this Redis Cache.
     *
     * @return the access keys for this Redis Cache
     */
    @Method
    RedisAccessKeys refreshKeys();

    /**
     * Regenerates the access keys for this Redis Cache.
     *
     * @param keyType key type to regenerate
     * @return the generated access keys for this Redis Cache
     */
    RedisAccessKeys regenerateKey(RedisKeyType keyType);

    /**************************************************************
     * Fluent interfaces to provision a RedisCache
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithSku,
            DefinitionStages.WithCreate,
            DefinitionStages.WithPremiumSkuCreate {
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
             * Specifies the Basic sku of the Redis Cache.
             *
             * @return the next stage of Redis Cache definition.
             */
            WithCreate withBasicSku();

            /**
             * Specifies the Basic sku of the Redis Cache.
             *
             * @param capacity specifies what size of Redis Cache to deploy for Basic sku with C family (0, 1, 2, 3, 4, 5, 6).
             * @return the next stage of Redis Cache definition.
             */
            WithCreate withBasicSku(int capacity);

            /**
             * Specifies the Standard Sku of the Redis Cache.
             *
             * @return the next stage of Redis Cache definition.
             */
            WithCreate withStandardSku();

            /**
             * Specifies the Standard sku of the Redis Cache.
             *
             * @param capacity specifies what size of Redis Cache to deploy for Standard sku with C family (0, 1, 2, 3, 4, 5, 6).
             * @return the next stage of Redis Cache definition.
             */
            WithCreate withStandardSku(int capacity);

            /**
             * Specifies the Premium sku of the Redis Cache.
             *
             * @return the next stage of Redis Cache definition.
             */
            WithPremiumSkuCreate withPremiumSku();

            /**
             * Specifies the Premium sku of the Redis Cache.
             *
             * @param capacity specifies what size of Redis Cache to deploy for Standard sku with P family (1, 2, 3, 4).
             * @return the next stage of Redis Cache definition.
             */
            WithPremiumSkuCreate withPremiumSku(int capacity);
        }

         /**
         * A Redis Cache definition with sufficient inputs to create a new
         * Redis Cache in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                 Creatable<RedisCache>,
                 DefinitionWithTags<WithCreate> {
             /**
              * Enables non-ssl Redis server port (6379).
              *
              * @return the next stage of Redis Cache definition.
              */
             WithCreate withNonSslPort();

             /**
              * All Redis Settings. Few possible keys:
              * rdb-backup-enabled, rdb-storage-connection-string, rdb-backup-frequency, maxmemory-delta, maxmemory-policy,
              * notify-keyspace-events, maxmemory-samples, slowlog-log-slower-than, slowlog-max-len, list-max-ziplist-entries,
              * list-max-ziplist-value, hash-max-ziplist-entries, hash-max-ziplist-value, set -max-intset-entries,
              * zset-max-ziplist-entries, zset-max-ziplist-value etc.
              *
              * @param redisConfiguration configuration of Redis Cache as a map indexed by configuration name
              * @return the next stage of Redis Cache definition.
              */
             WithCreate withRedisConfiguration(Map<String, String> redisConfiguration);

             /**
              * Specifies Redis Setting.
              * rdb-backup-enabled, rdb-storage-connection-string, rdb-backup-frequency, maxmemory-delta, maxmemory-policy,
              * notify-keyspace-events, maxmemory-samples, slowlog-log-slower-than, slowlog-max-len, list-max-ziplist-entries,
              * list-max-ziplist-value, hash-max-ziplist-entries, hash-max-ziplist-value, set -max-intset-entries,
              * zset-max-ziplist-entries, zset-max-ziplist-value etc.
              *
              * @param key   Redis configuration name.
              * @param value Redis configuration value.
              * @return the next stage of Redis Cache definition.
              */
             WithCreate withRedisConfiguration(String key, String value);

             /**
              * Assigns the specified subnet to this instance of Redis Cache.
              *
              * @param networkResource instance of Network object.
              * @param subnetName the name of the subnet.
              * @return the next stage of Redis Cache definition.
              */
             WithCreate withSubnet(GroupableResource networkResource, String subnetName);

             /**
              * Sets Redis Cache static IP. Required when deploying a Redis Cache inside an existing Azure Virtual Network.
              *
              * @param staticIP the static IP value to set.
              * @return the next stage of Redis Cache definition.
              */
             WithCreate withStaticIP(String staticIP);
        }

        /**
         * A Redis Cache definition with Premium Sku specific functionality.
         */
        interface WithPremiumSkuCreate extends DefinitionStages.WithCreate {

            /**
             * The number of shards to be created on a Premium Cluster Cache.
             *
             * @param shardCount the shard count value to set.
             * @return the next stage of Redis Cache with Premium SKU definition.
             */
            WithPremiumSkuCreate withShardCount(int shardCount);

            /**
             * Patch schedule on a Premium Cluster Cache.
             *
             * @param dayOfWeek day of week when cache can be patched.
             * @param startHourUtc start hour after which cache patching can start.
             * @return the next stage of Redis Cache with Premium SKU definition.
             */
            WithPremiumSkuCreate withPatchSchedule(DayOfWeek dayOfWeek, int startHourUtc);

            /**
             * Patch schedule on a Premium Cluster Cache.
             *
             * @param dayOfWeek day of week when cache can be patched.
             * @param startHourUtc start hour after which cache patching can start.
             * @param maintenanceWindow ISO8601 timespan specifying how much time cache patching can take.
             * @return the next stage of Redis Cache with Premium SKU definition.
             */
            WithPremiumSkuCreate withPatchSchedule(DayOfWeek dayOfWeek, int startHourUtc, Period maintenanceWindow);

            /**
             * Patch schedule on a Premium Cluster Cache.
             *
             * @param scheduleEntry Patch schedule entry for Premium Redis Cache.
             * @return the next stage of Redis Cache with Premium SKU definition.
             */
            WithPremiumSkuCreate withPatchSchedule(ScheduleEntry scheduleEntry);

            /**
             * Patch schedule on a Premium Cluster Cache.
             *
             * @param scheduleEntry List of patch schedule entries for Premium Redis Cache.
             * @return the next stage of Redis Cache with Premium SKU definition.
             */
            WithPremiumSkuCreate withPatchSchedule(List<ScheduleEntry> scheduleEntry);
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
             * Updates Redis Cache to Basic sku with new capacity.
             *
             * @param capacity specifies what size of Redis Cache to update to for Basic sku with C family (0, 1, 2, 3, 4, 5, 6).
             * @return the next stage of Redis Cache update.
             */
            Update withBasicSku(int capacity);

            /**
             * Updates Redis Cache to Standard sku.
             *
             * @return the next stage of Redis Cache update.
             */
            Update withStandardSku();

            /**
             * Updates Redis Cache to Standard sku with new capacity.
             *
             * @param capacity specifies what size of Redis Cache to update to for Standard sku with C family (0, 1, 2, 3, 4, 5, 6).
             * @return the next stage of Redis Cache update.
             */
            Update withStandardSku(int capacity);

            /**
             * Updates Redis Cache to Premium sku.
             *
             * @return the next stage of Redis Cache update.
             */
            Update withPremiumSku();

            /**
             * Updates Redis Cache to Premium sku with new capacity.
             *
             * @param capacity specifies what size of Redis Cache to update to for Premium sku with P family (1, 2, 3, 4).
             * @return the next stage of Redis Cache update.
             */
            Update withPremiumSku(int capacity);
        }

        /**
         * A Redis Cache update allowing non SSL port to be enabled or disabled.
         */
        interface WithNonSslPort {
            /**
             * Enables non-ssl Redis server port (6379).
             *
             * @return the next stage of Redis Cache update.
             */
            Update withNonSslPort();

            /**
             * Disables non-ssl Redis server port (6379).
             *
             * @return the next stage of Redis Cache update.
             */
            Update withoutNonSslPort();
        }

        /**
         * A Redis Cache update allowing Redis configuration to be modified.
         */
        interface WithRedisConfiguration {
            /**
             * All Redis Settings. Few possible keys:
             * rdb-backup-enabled, rdb-storage-connection-string, rdb-backup-frequency, maxmemory-delta, maxmemory-policy,
             * notify-keyspace-events, maxmemory-samples, slowlog-log-slower-than, slowlog-max-len, list-max-ziplist-entries,
             * list-max-ziplist-value, hash-max-ziplist-entries, hash-max-ziplist-value, set -max-intset-entries,
             * zset-max-ziplist-entries, zset-max-ziplist-value etc.
             *
             * @param redisConfiguration configuration of Redis Cache as a map indexed by configuration name
             * @return the next stage of Redis Cache update.
             */
            Update withRedisConfiguration(Map<String, String> redisConfiguration);

            /**
             * Specifies Redis Setting.
             * rdb-backup-enabled, rdb-storage-connection-string, rdb-backup-frequency, maxmemory-delta, maxmemory-policy,
             * notify-keyspace-events, maxmemory-samples, slowlog-log-slower-than, slowlog-max-len, list-max-ziplist-entries,
             * list-max-ziplist-value, hash-max-ziplist-entries, hash-max-ziplist-value, set -max-intset-entries,
             * zset-max-ziplist-entries, zset-max-ziplist-value etc.
             *
             * @param key   Redis configuration name.
             * @param value Redis configuration value.
             * @return the next stage of Redis Cache update.
             */
            Update withRedisConfiguration(String key, String value);

            /**
             * Cleans all the configuration settings being set on Redis Cache.
             *
             * @return the next stage of Redis Cache update.
             */
            Update withoutRedisConfiguration();

            /**
             * Removes specified Redis Cache configuration setting.
             *
             * @param key Redis configuration name.
             * @return the next stage of Redis Cache update.
             */
            Update withoutRedisConfiguration(String key);
        }
    }

    /**
     * The template for a Redis Cache update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<RedisCache>,
            Resource.UpdateWithTags<Update>,
            UpdateStages.WithSku,
            UpdateStages.WithNonSslPort,
            UpdateStages.WithRedisConfiguration {

        /**
         * Assigns the specified subnet to this instance of Redis Cache.
         *
         * @param networkResource instance of Network object.
         * @param subnetName the name of the subnet.
         * @return the next stage of Redis Cache update.
         */
        Update withSubnet(GroupableResource networkResource, String subnetName);

        /**
         * Sets Redis Cache static IP. Required when deploying a Redis Cache inside an existing Azure Virtual Network.
         *
         * @param staticIP the staticIP value to set.
         * @return the next stage of Redis Cache update.
         */
        Update withStaticIP(String staticIP);

        /**
         * The number of shards to be created on a Premium Cluster Cache.
         *
         * @param shardCount the shard count value to set.
         * @return the next stage of Redis Cache update.
         */
        Update withShardCount(int shardCount);

        /**
         * Patch schedule on a Premium Cluster Cache.
         *
         * @param dayOfWeek day of week when cache can be patched.
         * @param startHourUtc start hour after which cache patching can start.
         * @return the next stage of Redis Cache with Premium SKU definition.
         */
        Update withPatchSchedule(DayOfWeek dayOfWeek, int startHourUtc);

        /**
         * The number of shards to be created on a Premium Cluster Cache.
         *
         * @param dayOfWeek day of week when cache can be patched.
         * @param startHourUtc start hour after which cache patching can start.
         * @param maintenanceWindow ISO8601 timespan specifying how much time cache patching can take.
         * @return the next stage of Redis Cache with Premium SKU definition.
         */
        Update withPatchSchedule(DayOfWeek dayOfWeek, int startHourUtc, Period maintenanceWindow);

        /**
         * Patch schedule on a Premium Cluster Cache.
         *
         * @param scheduleEntry Patch schedule entry for Premium Redis Cache.
         * @return the next stage of Redis Cache with Premium SKU definition.
         */
        Update withPatchSchedule(ScheduleEntry scheduleEntry);

        /**
         * Patch schedule on a Premium Cluster Cache.
         *
         * @param scheduleEntry List of patch schedule entries for Premium Redis Cache.
         * @return the next stage of Redis Cache with Premium SKU definition.
         */
        Update withPatchSchedule(List<ScheduleEntry> scheduleEntry);
    }
}

