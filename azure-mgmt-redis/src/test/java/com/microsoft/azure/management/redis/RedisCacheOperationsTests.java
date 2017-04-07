/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import com.microsoft.azure.management.storage.StorageAccount;
import org.joda.time.Period;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.fail;

public class RedisCacheOperationsTests extends RedisManagementTest {
    @Test
    public void canCRUDRedisCache() throws Exception {
        // Create
        Creatable<ResourceGroup> resourceGroups = resourceManager.resourceGroups()
                .define(RG_NAME_SECOND)
                .withRegion(Region.US_CENTRAL);

        Creatable<RedisCache> redisCacheDefinition1 = redisManager.redisCaches()
                .define(RR_NAME)
                .withRegion(Region.ASIA_EAST)
                .withNewResourceGroup(RG_NAME)
                .withBasicSku();
        Creatable<RedisCache> redisCacheDefinition2 = redisManager.redisCaches()
                .define(RR_NAME_SECOND)
                .withRegion(Region.US_CENTRAL)
                .withNewResourceGroup(resourceGroups)
                .withPremiumSku()
                .withShardCount(10)
                .withPatchSchedule(DayOfWeek.SUNDAY, 10, Period.minutes(302));
        Creatable<RedisCache> redisCacheDefinition3 = redisManager.redisCaches()
                .define(RR_NAME_THIRD)
                .withRegion(Region.US_CENTRAL)
                .withNewResourceGroup(resourceGroups)
                .withPremiumSku(2)
                .withRedisConfiguration("maxclients", "2")
                .withNonSslPort();

        CreatedResources<RedisCache> batchRedisCaches = redisManager.redisCaches()
                .create(redisCacheDefinition1, redisCacheDefinition2, redisCacheDefinition3);

        StorageAccount storageAccount = storageManager.storageAccounts()
                .define(SA_NAME)
                .withRegion(Region.US_CENTRAL)
                .withExistingResourceGroup(RG_NAME_SECOND)
                .create();

        RedisCache redisCache = batchRedisCaches.get(redisCacheDefinition1.key());
        RedisCache redisCachePremium = batchRedisCaches.get(redisCacheDefinition3.key());
        Assert.assertEquals(RG_NAME, redisCache.resourceGroupName());
        Assert.assertEquals(SkuName.BASIC, redisCache.sku().name());

        // List by Resource Group
        List<RedisCache> redisCaches = redisManager.redisCaches().listByResourceGroup(RG_NAME);
        boolean found = false;
        for (RedisCache existingRedisCache : redisCaches) {
            if (existingRedisCache.name().equals(RR_NAME)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
        Assert.assertEquals(1, redisCaches.size());

        // List all Redis resources
        redisCaches = redisManager.redisCaches().list();
        found = false;
        for (RedisCache existingRedisCache : redisCaches) {
            if (existingRedisCache.name().equals(RR_NAME)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
        Assert.assertTrue(redisCaches.size() >= 3);

        // Get
        RedisCache redisCacheGet = redisManager.redisCaches().getByResourceGroup(RG_NAME, RR_NAME);
        Assert.assertNotNull(redisCacheGet);
        Assert.assertEquals(redisCache.id(), redisCacheGet.id());
        Assert.assertEquals(redisCache.provisioningState(), redisCacheGet.provisioningState());

        // Get Keys
        RedisAccessKeys redisKeys = redisCache.keys();
        Assert.assertNotNull(redisKeys);
        Assert.assertNotNull(redisKeys.primaryKey());
        Assert.assertNotNull(redisKeys.secondaryKey());

        // Regen key
        RedisAccessKeys oldKeys = redisCache.refreshKeys();
        RedisAccessKeys updatedPrimaryKey = redisCache.regenerateKey(RedisKeyType.PRIMARY);
        RedisAccessKeys updatedSecondaryKey = redisCache.regenerateKey(RedisKeyType.SECONDARY);
        Assert.assertNotNull(oldKeys);
        Assert.assertNotNull(updatedPrimaryKey);
        Assert.assertNotNull(updatedSecondaryKey);
        Assert.assertNotEquals(oldKeys.primaryKey(), updatedPrimaryKey.primaryKey());
        Assert.assertEquals(oldKeys.secondaryKey(), updatedPrimaryKey.secondaryKey());
        Assert.assertNotEquals(oldKeys.secondaryKey(), updatedSecondaryKey.secondaryKey());
        Assert.assertNotEquals(updatedPrimaryKey.secondaryKey(), updatedSecondaryKey.secondaryKey());
        Assert.assertEquals(updatedPrimaryKey.primaryKey(), updatedSecondaryKey.primaryKey());

        // Update to STANDARD Sku from BASIC SKU
        redisCache = redisCache.update()
                .withStandardSku()
                .apply();
        Assert.assertEquals(SkuName.STANDARD, redisCache.sku().name());
        Assert.assertEquals(SkuFamily.C, redisCache.sku().family());

        try {
            redisCache.update()
                    .withBasicSku(1)
                    .apply();
            fail();
        } catch (CloudException e) {
            // expected since Sku downgrade is not supported
        }

        // Refresh
        redisCache.refresh();

        // delete
        redisManager.redisCaches().deleteById(redisCache.id());

        // Premium SKU Functionality
        RedisCachePremium premiumCache = redisCachePremium.asPremium();
        Assert.assertEquals(SkuFamily.P, premiumCache.sku().family());

        // Redis configuration update
        premiumCache.update()
                .withRedisConfiguration("maxclients", "3")
                .apply();

        premiumCache.update()
                .withoutRedisConfiguration("maxclients")
                .apply();

        premiumCache.update()
                .withoutRedisConfiguration()
                .apply();

        premiumCache.update()
                .withPatchSchedule(DayOfWeek.MONDAY, 1)
                .withPatchSchedule(DayOfWeek.TUESDAY, 5)
                .apply();

        // Reboot
        premiumCache.forceReboot(RebootType.ALL_NODES);

        // Patch Schedule
        List<ScheduleEntry> patchSchedule = premiumCache.listPatchSchedules();
        Assert.assertEquals(2, patchSchedule.size());

        premiumCache.deletePatchSchedule();

        patchSchedule = redisManager.redisCaches()
                                    .getById(premiumCache.id())
                                    .asPremium()
                                    .listPatchSchedules();
        Assert.assertNull(patchSchedule);

        // currently throws because SAS url of the container should be provided as
        // {"error":{
        //      "code":"InvalidRequestBody",
        //      "message": "One of the SAS URIs provided could not be used for the following reason:
        //                  The SAS token is poorly formatted.\r\nRequestID=ed105089-b93b-427e-9cbb-d78ed80d23b0",
        //      "target":null}}
        // com.microsoft.azure.CloudException: One of the SAS URIs provided could not be used for the following reason: The SAS token is poorly formatted.
        /*premiumCache.exportData(storageAccount.name(),"snapshot1");

        premiumCache.importData(Arrays.asList("snapshot1"));*/
    }
}
