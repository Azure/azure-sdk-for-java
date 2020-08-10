/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import com.microsoft.azure.management.storage.StorageAccount;
import org.joda.time.Period;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

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
                .withNonSslPort()
                .withFirewallRule("rule1", "192.168.0.1", "192.168.0.4")
                .withFirewallRule("rule2", "192.168.0.10", "192.168.0.40");
                // Server throws "The 'minimumTlsVersion' property is not yet supported." exception. Uncomment when fixed.
                //.withMinimumTlsVersion(TlsVersion.ONE_FULL_STOP_ONE);

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
        Assert.assertEquals(2, premiumCache.firewallRules().size());
        Assert.assertTrue(premiumCache.firewallRules().containsKey("rule1"));
        Assert.assertTrue(premiumCache.firewallRules().containsKey("rule2"));

        // Redis configuration update
        premiumCache.update()
                .withRedisConfiguration("maxclients", "3")
                .withoutFirewallRule("rule1")
                .withFirewallRule("rule3", "192.168.0.10", "192.168.0.104")
                .withoutMinimumTlsVersion()
                .apply();

        Assert.assertEquals(2, premiumCache.firewallRules().size());
        Assert.assertTrue(premiumCache.firewallRules().containsKey("rule2"));
        Assert.assertTrue(premiumCache.firewallRules().containsKey("rule3"));
        Assert.assertFalse(premiumCache.firewallRules().containsKey("rule1"));

        premiumCache.update()
                .withoutRedisConfiguration("maxclients")
                .apply();

        premiumCache.update()
                .withoutRedisConfiguration()
                .apply();

        Assert.assertEquals(0, premiumCache.patchSchedules().size());
        premiumCache.update()
                .withPatchSchedule(DayOfWeek.MONDAY, 1)
                .withPatchSchedule(DayOfWeek.TUESDAY, 5)
                .apply();

        Assert.assertEquals(2, premiumCache.patchSchedules().size());
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

    @Test
    public void canCRUDLinkedServers() throws Exception {

        RedisCache rgg = redisManager.redisCaches()
                .define(RR_NAME_THIRD)
                .withRegion(Region.US_CENTRAL)
                .withNewResourceGroup(RG_NAME_SECOND)
                .withPremiumSku(2)
                .withPatchSchedule(DayOfWeek.SATURDAY, 5, Period.hours(5))
                .withRedisConfiguration("maxclients", "2")
                .withNonSslPort()
                .withFirewallRule("rule1", "192.168.0.1", "192.168.0.4")
                .withFirewallRule("rule2", "192.168.0.10", "192.168.0.40")
                .create();

        RedisCache rggLinked = redisManager.redisCaches()
                .define(RR_NAME_SECOND)
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(RG_NAME_SECOND)
                .withPremiumSku(2)
                .create();

        Assert.assertNotNull(rgg);
        Assert.assertNotNull(rggLinked);

        RedisCachePremium premiumRgg = rgg.asPremium();

        String llName = premiumRgg.addLinkedServer(rggLinked.id(), rggLinked.regionName(), ReplicationRole.PRIMARY);

        Assert.assertEquals(ResourceUtils.nameFromResourceId(rggLinked.id()), llName);

        Map<String, ReplicationRole> linkedServers = premiumRgg.listLinkedServers();
        Assert.assertEquals(1, linkedServers.size());
        Assert.assertTrue(linkedServers.keySet().contains(llName));
        Assert.assertEquals(ReplicationRole.PRIMARY, linkedServers.get(llName));

        ReplicationRole repRole = premiumRgg.getLinkedServerRole(llName);
        Assert.assertEquals(ReplicationRole.PRIMARY, repRole);

        premiumRgg.removeLinkedServer(llName);

        rgg.update()
                .withoutPatchSchedule()
                .apply();

        rggLinked.update()
                .withFirewallRule("rulesmhule", "192.168.1.10", "192.168.1.20")
                .apply();

        linkedServers = premiumRgg.listLinkedServers();
        Assert.assertEquals(0, linkedServers.size());
    }
}
