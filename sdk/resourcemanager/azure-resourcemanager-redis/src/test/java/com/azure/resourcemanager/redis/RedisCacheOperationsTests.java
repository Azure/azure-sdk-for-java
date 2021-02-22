// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.redis;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.redis.models.DayOfWeek;
import com.azure.resourcemanager.redis.models.RebootType;
import com.azure.resourcemanager.redis.models.RedisAccessKeys;
import com.azure.resourcemanager.redis.models.RedisCache;
import com.azure.resourcemanager.redis.models.RedisCachePremium;
import com.azure.resourcemanager.redis.models.RedisKeyType;
import com.azure.resourcemanager.redis.models.ReplicationRole;
import com.azure.resourcemanager.redis.models.ScheduleEntry;
import com.azure.resourcemanager.redis.models.SkuFamily;
import com.azure.resourcemanager.redis.models.SkuName;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.CreatedResources;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RedisCacheOperationsTests extends RedisManagementTest {
    @Test
    @SuppressWarnings("unchecked")
    public void canCRUDRedisCache() throws Exception {
        // Create
        Creatable<ResourceGroup> resourceGroups =
            resourceManager.resourceGroups().define(rgNameSecond).withRegion(Region.US_CENTRAL);

        Creatable<RedisCache> redisCacheDefinition1 =
            redisManager
                .redisCaches()
                .define(rrName)
                .withRegion(Region.ASIA_EAST)
                .withNewResourceGroup(rgName)
                .withBasicSku();
        Creatable<RedisCache> redisCacheDefinition2 =
            redisManager
                .redisCaches()
                .define(rrNameSecond)
                .withRegion(Region.US_CENTRAL)
                .withNewResourceGroup(resourceGroups)
                .withPremiumSku()
                .withShardCount(2)
                .withPatchSchedule(DayOfWeek.SUNDAY, 10, Duration.ofMinutes(302));
        Creatable<RedisCache> redisCacheDefinition3 =
            redisManager
                .redisCaches()
                .define(rrNameThird)
                .withRegion(Region.US_CENTRAL)
                .withNewResourceGroup(resourceGroups)
                .withPremiumSku(2)
                .withRedisConfiguration("maxclients", "2")
                .withNonSslPort()
                .withFirewallRule("rule1", "192.168.0.1", "192.168.0.4")
                .withFirewallRule("rule2", "192.168.0.10", "192.168.0.40");
        // Server throws "The 'minimumTlsVersion' property is not yet supported." exception. Uncomment when fixed.
        // .withMinimumTlsVersion(TlsVersion.ONE_FULL_STOP_ONE);

        CreatedResources<RedisCache> batchRedisCaches =
            redisManager.redisCaches().create(redisCacheDefinition1, redisCacheDefinition2, redisCacheDefinition3);

//        StorageAccount storageAccount =
//            storageManager
//                .storageAccounts()
//                .define(saName)
//                .withRegion(Region.US_CENTRAL)
//                .withExistingResourceGroup(rgNameSecond)
//                .create();

        RedisCache redisCache = batchRedisCaches.get(redisCacheDefinition1.key());
        RedisCache redisCachePremium = batchRedisCaches.get(redisCacheDefinition3.key());
        Assertions.assertEquals(rgName, redisCache.resourceGroupName());
        Assertions.assertEquals(SkuName.BASIC, redisCache.sku().name());

        // Premium SKU Functionality
        RedisCachePremium premiumCache = redisCachePremium.asPremium();
        Assertions.assertEquals(SkuFamily.P, premiumCache.sku().family());
        Assertions.assertEquals(2, premiumCache.firewallRules().size());
        Assertions.assertTrue(premiumCache.firewallRules().containsKey("rule1"));
        Assertions.assertTrue(premiumCache.firewallRules().containsKey("rule2"));

        // Redis configuration update
        premiumCache
            .update()
            .withRedisConfiguration("maxclients", "3")
            .withoutFirewallRule("rule1")
            .withFirewallRule("rule3", "192.168.0.10", "192.168.0.104")
            .withoutMinimumTlsVersion()
            .apply();

        Assertions.assertEquals(2, premiumCache.firewallRules().size());
        Assertions.assertTrue(premiumCache.firewallRules().containsKey("rule2"));
        Assertions.assertTrue(premiumCache.firewallRules().containsKey("rule3"));
        Assertions.assertFalse(premiumCache.firewallRules().containsKey("rule1"));

        premiumCache.update().withoutRedisConfiguration("maxclients").apply();

        premiumCache.update().withoutRedisConfiguration().apply();

        Assertions.assertEquals(0, premiumCache.patchSchedules().size());
        premiumCache.update().withPatchSchedule(DayOfWeek.MONDAY, 1).withPatchSchedule(DayOfWeek.TUESDAY, 5).apply();

        Assertions.assertEquals(2, premiumCache.patchSchedules().size());
        // Reboot
        premiumCache.forceReboot(RebootType.ALL_NODES);

        // Patch Schedule
        List<ScheduleEntry> patchSchedule = premiumCache.listPatchSchedules();
        Assertions.assertEquals(2, patchSchedule.size());

        premiumCache.deletePatchSchedule();

        patchSchedule = redisManager.redisCaches().getById(premiumCache.id()).asPremium().listPatchSchedules();
        Assertions.assertNull(patchSchedule);

        // List by Resource Group
        List<RedisCache> redisCaches =
            redisManager.redisCaches().listByResourceGroup(rgName).stream().collect(Collectors.toList());
        boolean found = false;
        for (RedisCache existingRedisCache : redisCaches) {
            if (existingRedisCache.name().equals(rrName)) {
                found = true;
            }
        }
        Assertions.assertTrue(found);
        Assertions.assertEquals(1, redisCaches.size());

        // List all Redis resources
        redisCaches = redisManager.redisCaches().list().stream().collect(Collectors.toList());
        found = false;
        for (RedisCache existingRedisCache : redisCaches) {
            if (existingRedisCache.name().equals(rrName)) {
                found = true;
            }
        }
        Assertions.assertTrue(found);
        Assertions.assertTrue(redisCaches.size() >= 3);

        // Get
        RedisCache redisCacheGet = redisManager.redisCaches().getByResourceGroup(rgName, rrName);
        Assertions.assertNotNull(redisCacheGet);
        Assertions.assertEquals(redisCache.id(), redisCacheGet.id());
        Assertions.assertEquals(redisCache.provisioningState(), redisCacheGet.provisioningState());

        // Get Keys
        RedisAccessKeys redisKeys = redisCache.keys();
        Assertions.assertNotNull(redisKeys);
        Assertions.assertNotNull(redisKeys.primaryKey());
        Assertions.assertNotNull(redisKeys.secondaryKey());

        // Regen key
        RedisAccessKeys oldKeys = redisCache.refreshKeys();
        RedisAccessKeys updatedPrimaryKey = redisCache.regenerateKey(RedisKeyType.PRIMARY);
        RedisAccessKeys updatedSecondaryKey = redisCache.regenerateKey(RedisKeyType.SECONDARY);
        Assertions.assertNotNull(oldKeys);
        Assertions.assertNotNull(updatedPrimaryKey);
        Assertions.assertNotNull(updatedSecondaryKey);
        if (!isPlaybackMode()) {
            Assertions.assertNotEquals(oldKeys.primaryKey(), updatedPrimaryKey.primaryKey());
            Assertions.assertEquals(oldKeys.secondaryKey(), updatedPrimaryKey.secondaryKey());
            Assertions.assertNotEquals(oldKeys.secondaryKey(), updatedSecondaryKey.secondaryKey());
            Assertions.assertNotEquals(updatedPrimaryKey.secondaryKey(), updatedSecondaryKey.secondaryKey());
            Assertions.assertEquals(updatedPrimaryKey.primaryKey(), updatedSecondaryKey.primaryKey());
        }

        // Update to STANDARD Sku from BASIC SKU
        redisCache = redisCache.update().withStandardSku().apply();
        Assertions.assertEquals(SkuName.STANDARD, redisCache.sku().name());
        Assertions.assertEquals(SkuFamily.C, redisCache.sku().family());

        try {
            redisCache.update().withBasicSku(1).apply();
            Assertions.fail();
        } catch (ManagementException e) {
            // expected since Sku downgrade is not supported
        }

        // Refresh
        redisCache.refresh();

        // delete
        redisManager.redisCaches().deleteById(redisCache.id());

        // currently throws because SAS url of the container should be provided as
        // {"error":{
        //      "code":"InvalidRequestBody",
        //      "message": "One of the SAS URIs provided could not be used for the following reason:
        //                  The SAS token is poorly formatted.\r\nRequestID=ed105089-b93b-427e-9cbb-d78ed80d23b0",
        //      "target":null}}
        // com.microsoft.azure.CloudException: One of the SAS URIs provided could not be used for the following reason:
        // The SAS token is poorly formatted.
        /*premiumCache.exportData(storageAccount.name(),"snapshot1");

        premiumCache.importData(Arrays.asList("snapshot1"));*/
    }

    @Test
    public void canCRUDLinkedServers() throws Exception {

        RedisCache rgg =
            redisManager
                .redisCaches()
                .define(rrNameThird)
                .withRegion(Region.US_CENTRAL)
                .withNewResourceGroup(rgNameSecond)
                .withPremiumSku(2)
                .withPatchSchedule(DayOfWeek.SATURDAY, 5, Duration.ofHours(5))
                .withRedisConfiguration("maxclients", "2")
                .withNonSslPort()
                .withFirewallRule("rule1", "192.168.0.1", "192.168.0.4")
                .withFirewallRule("rule2", "192.168.0.10", "192.168.0.40")
                .create();

        RedisCache rggLinked =
            redisManager
                .redisCaches()
                .define(rrNameSecond)
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(rgNameSecond)
                .withPremiumSku(2)
                .create();

        Assertions.assertNotNull(rgg);
        Assertions.assertNotNull(rggLinked);

        RedisCachePremium premiumRgg = rgg.asPremium();

        String llName = premiumRgg.addLinkedServer(rggLinked.id(), rggLinked.regionName(), ReplicationRole.PRIMARY);

        Assertions.assertEquals(ResourceUtils.nameFromResourceId(rggLinked.id()), llName);

        Map<String, ReplicationRole> linkedServers = premiumRgg.listLinkedServers();
        Assertions.assertEquals(1, linkedServers.size());
        Assertions.assertTrue(linkedServers.keySet().contains(llName));
        Assertions.assertEquals(ReplicationRole.PRIMARY, linkedServers.get(llName));

        ReplicationRole repRole = premiumRgg.getLinkedServerRole(llName);
        Assertions.assertEquals(ReplicationRole.PRIMARY, repRole);

        premiumRgg.removeLinkedServer(llName);

        rgg.update().withoutPatchSchedule().apply();

        rggLinked.update().withFirewallRule("rulesmhule", "192.168.1.10", "192.168.1.20").apply();

        linkedServers = premiumRgg.listLinkedServers();
        Assertions.assertEquals(0, linkedServers.size());
    }
}
