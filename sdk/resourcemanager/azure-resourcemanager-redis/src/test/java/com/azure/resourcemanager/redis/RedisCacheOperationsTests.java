// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.redis;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.redis.models.DayOfWeek;
import com.azure.resourcemanager.redis.models.RebootType;
import com.azure.resourcemanager.redis.models.RedisAccessKeys;
import com.azure.resourcemanager.redis.models.RedisCache;
import com.azure.resourcemanager.redis.models.RedisCachePremium;
import com.azure.resourcemanager.redis.models.RedisConfiguration;
import com.azure.resourcemanager.redis.models.RedisKeyType;
import com.azure.resourcemanager.redis.models.ReplicationRole;
import com.azure.resourcemanager.redis.models.ScheduleEntry;
import com.azure.resourcemanager.redis.models.SkuFamily;
import com.azure.resourcemanager.redis.models.SkuName;
import com.azure.resourcemanager.redis.models.TlsVersion;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.CreatedResources;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        Assertions.assertEquals(rgName, redisCache.resourceGroupName());
        Assertions.assertEquals(SkuName.BASIC, redisCache.sku().name());
        assertSameVersion(RedisCache.RedisVersion.V6, redisCache.redisVersion());

        // Premium SKU Functionality
        RedisCache redisCachePremium = batchRedisCaches.get(redisCacheDefinition3.key());
        RedisCachePremium premiumCache = redisCachePremium.asPremium();
        Assertions.assertEquals(SkuFamily.P, premiumCache.sku().family());
        assertSameVersion(RedisCache.RedisVersion.V6, premiumCache.redisVersion());
        Assertions.assertEquals(2, premiumCache.firewallRules().size());
        Assertions.assertTrue(premiumCache.firewallRules().containsKey("rule1"));
        Assertions.assertTrue(premiumCache.firewallRules().containsKey("rule2"));

        // Redis configuration update
        premiumCache
            .update()
            .withoutFirewallRule("rule1")
            .withFirewallRule("rule3", "192.168.0.10", "192.168.0.104")
            .withoutMinimumTlsVersion()
            .apply();

        ResourceManagerUtils.sleep(Duration.ofSeconds(10));

        premiumCache.refresh();

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
    public void canRedisVersionUpdate() {
        RedisCache.RedisVersion redisVersion = RedisCache.RedisVersion.V4;

        RedisCache redisCache =
            redisManager
                .redisCaches()
                .define(rrName)
                .withRegion(Region.ASIA_EAST)
                .withNewResourceGroup(rgName)
                .withBasicSku()
                .withRedisVersion(redisVersion)
                .create();

        assertSameVersion(RedisCache.RedisVersion.V4, redisCache.redisVersion());

        redisVersion = RedisCache.RedisVersion.V6;
        redisCache = redisCache.update()
                .withRedisVersion(redisVersion)
                .apply(); // response with "provisioningState" : "Succeeded", but it takes quite a while for the client to detect the actual version change

        ResourceManagerUtils.sleep(Duration.ofSeconds(300)); // let redis cache take its time

        redisCache = redisCache.refresh();
        assertSameVersion(RedisCache.RedisVersion.V6, redisCache.redisVersion());

        RedisCache redisCacheLocal = redisCache;

        // Cannot downgrade redisCache version.
        Assertions.assertThrows(
            ManagementException.class,
            () -> redisCacheLocal.update()
                .withRedisVersion(RedisCache.RedisVersion.V4)
                .apply());
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
        assertSameVersion(RedisCache.RedisVersion.V6, premiumRgg.redisVersion());

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

    @Test
    public void canCreateRedisWithRdbAof() {
        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(saName)
                .withRegion(Region.US_WEST3)
                .withNewResourceGroup(rgName)
                .create();

        String connectionString = ResourceManagerUtils.getStorageConnectionString(saName, storageAccount.getKeys().get(0).value(), AzureEnvironment.AZURE);

        // RDB
        RedisCache redisCache =
            redisManager
                .redisCaches()
                .define(rrName)
                .withRegion(Region.US_WEST3)
                .withExistingResourceGroup(rgName)
                .withPremiumSku()
                .withMinimumTlsVersion(TlsVersion.ONE_TWO)
                .withRedisConfiguration(new RedisConfiguration()
                    .withRdbBackupEnabled("true")
                    .withRdbBackupFrequency("15")
                    .withRdbBackupMaxSnapshotCount("1")
                    .withRdbStorageConnectionString(connectionString))
                .create();
        Assertions.assertEquals("true", redisCache.innerModel().redisConfiguration().rdbBackupEnabled());
        Assertions.assertEquals("15", redisCache.innerModel().redisConfiguration().rdbBackupFrequency());
        Assertions.assertEquals("1", redisCache.innerModel().redisConfiguration().rdbBackupMaxSnapshotCount());
        Assertions.assertNotNull(redisCache.innerModel().redisConfiguration().rdbStorageConnectionString());
        assertSameVersion(RedisCache.RedisVersion.V6, redisCache.redisVersion());

        redisManager.redisCaches().deleteById(redisCache.id());

        // AOF
        redisCache =
            redisManager
                .redisCaches()
                .define(rrName)
                .withRegion(Region.US_WEST3)
                .withExistingResourceGroup(rgName)
                .withPremiumSku()
                .withMinimumTlsVersion(TlsVersion.ONE_TWO)
                .withRedisConfiguration("aof-backup-enabled", "true")
                .withRedisConfiguration("aof-storage-connection-string-0", connectionString)
                .withRedisConfiguration("aof-storage-connection-string-1", connectionString)
                .create();
        Assertions.assertEquals("true", redisCache.innerModel().redisConfiguration().additionalProperties().get("aof-backup-enabled"));
        Assertions.assertNotNull(redisCache.innerModel().redisConfiguration().aofStorageConnectionString0());
        Assertions.assertNotNull(redisCache.innerModel().redisConfiguration().aofStorageConnectionString1());

        assertSameVersion(RedisCache.RedisVersion.V6, redisCache.redisVersion());
    }

    // e.g 6.xxxx
    private static final Pattern MINOR_VERSION_REGEX = Pattern.compile("([1-9]+)\\..*");

    private static void assertSameVersion(RedisCache.RedisVersion majorVersion, String minorVersion) {
        Matcher matcher = MINOR_VERSION_REGEX.matcher(minorVersion);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(String.format("Invalid redis minor version: %s", minorVersion));
        }
        Assertions.assertEquals(matcher.group(1), majorVersion.getValue());
    }
}
