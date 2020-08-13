/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.rediscache.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.redis.DayOfWeek;
import com.microsoft.azure.management.redis.RebootType;
import com.microsoft.azure.management.redis.RedisAccessKeys;
import com.microsoft.azure.management.redis.RedisCache;
import com.microsoft.azure.management.redis.RedisCachePremium;
import com.microsoft.azure.management.redis.RedisKeyType;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.util.List;

/**
 * Azure Redis sample for managing Redis Cache:
 *  - Create a Redis Cache and print out hostname.
 *  - Get access keys.
 *  - Regenerate access keys.
 *  - Create another 2 Redis Caches with Premium Sku.
 *  - List all Redis Caches in a resource group – for each cache with Premium Sku:
 *     - set Redis patch schedule to Monday at 5 am.
 *     - update shard count.
 *     - enable non-SSL port.
 *     - modify max memory policy and reserved settings.
 *     - restart it.
 *  - Clean up all resources.
 */

public final class ManageRedisCache {


    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String redisCacheName1 = Utils.createRandomName("rc1");
        final String redisCacheName2 = Utils.createRandomName("rc2");
        final String redisCacheName3 = Utils.createRandomName("rc3");
        final String rgName = Utils.createRandomName("rgRCMC");
        try {
            // ============================================================
            // Define a redis cache

            System.out.println("Creating a Redis Cache");

            Creatable<RedisCache> redisCache1Definition = azure.redisCaches().define(redisCacheName1)
                    .withRegion(Region.US_CENTRAL)
                    .withNewResourceGroup(rgName)
                    .withBasicSku();

            // ============================================================
            // Define two more Redis caches

            Creatable<RedisCache> redisCache2Definition = azure.redisCaches().define(redisCacheName2)
                    .withRegion(Region.US_CENTRAL)
                    .withNewResourceGroup(rgName)
                    .withPremiumSku()
                    .withShardCount(3);

            Creatable<RedisCache> redisCache3Definition = azure.redisCaches().define(redisCacheName3)
                    .withRegion(Region.US_CENTRAL)
                    .withNewResourceGroup(rgName)
                    .withPremiumSku(2)
                    .withShardCount(3);

            // ============================================================
            // Create all the caches in parallel to save time

            System.out.println("Creating three Redis Caches in parallel... (this will take several minutes)");

            @SuppressWarnings("unchecked")
            CreatedResources<RedisCache> createdCaches = azure.redisCaches().create(
                    redisCache1Definition,
                    redisCache2Definition,
                    redisCache3Definition);

            System.out.println("Created Redis caches:");
            RedisCache redisCache1 = createdCaches.get(redisCache1Definition.key());

            for (RedisCache redisCache : createdCaches.values()) {
                Utils.print(redisCache);
                System.out.println();
            }

            // ============================================================
            // Get | regenerate Redis Cache access keys

            System.out.println("Getting the first Redis cache's access keys");
            RedisAccessKeys redisAccessKeys = redisCache1.keys();
            Utils.print(redisAccessKeys);

            System.out.println("Regenerating secondary Redis cache access key");
            redisAccessKeys = redisCache1.regenerateKey(RedisKeyType.SECONDARY);
            Utils.print(redisAccessKeys);

            // ============================================================
            // List Redis Caches inside the resource group

            System.out.println("Listing Redis Caches");

            List<RedisCache> caches = azure.redisCaches().listByResourceGroup(rgName);

            // Walk through all the caches
            for (RedisCache redis : caches) {
                // If the instance of the Redis Cache is Premium Sku
                if (redis.isPremium()) {
                    RedisCachePremium premium = redis.asPremium();

                    // Update each Premium Sku Redis Cache instance
                    System.out.println("Updating Premium Redis Cache");
                    premium.update()
                            .withPatchSchedule(DayOfWeek.MONDAY, 5)
                            .withShardCount(4)
                            .withNonSslPort()
                            .withRedisConfiguration("maxmemory-policy", "allkeys-random")
                            .withRedisConfiguration("maxmemory-reserved", "20")
                            .apply();

                    System.out.println("Updated Redis Cache:");
                    Utils.print(premium);

                    // Restart Redis Cache
                    System.out.println("Restarting updated Redis Cache");
                    premium.forceReboot(RebootType.ALL_NODES, 1);

                    System.out.println("Redis Cache restart scheduled");
                }
            }

            // ============================================================
            // Delete a Redis Cache

            System.out.println("Deleting a Redis Cache  - " + redisCache1.name());

            azure.redisCaches().deleteById(redisCache1.id());

            System.out.println("Deleted Redis Cache");
            return true;
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();
        } finally {
            if (azure.resourceGroups().getByName(rgName) != null) {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().deleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } else {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            }
        }
        return false;
    }

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                    .withLogLevel(LogLevel.BASIC)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageRedisCache() {
    }
}