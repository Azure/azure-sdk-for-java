# Code snippets and samples


## FirewallRules

- [CreateOrUpdate](#firewallrules_createorupdate)
- [Delete](#firewallrules_delete)
- [Get](#firewallrules_get)
- [ListByMongoCluster](#firewallrules_listbymongocluster)

## MongoClusters

- [CheckNameAvailability](#mongoclusters_checknameavailability)
- [CreateOrUpdate](#mongoclusters_createorupdate)
- [Delete](#mongoclusters_delete)
- [GetByResourceGroup](#mongoclusters_getbyresourcegroup)
- [List](#mongoclusters_list)
- [ListByResourceGroup](#mongoclusters_listbyresourcegroup)
- [ListConnectionStrings](#mongoclusters_listconnectionstrings)
- [Promote](#mongoclusters_promote)
- [Update](#mongoclusters_update)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [Create](#privateendpointconnections_create)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByMongoCluster](#privateendpointconnections_listbymongocluster)

## PrivateLinks

- [ListByMongoCluster](#privatelinks_listbymongocluster)

## Replicas

- [ListByParent](#replicas_listbyparent)

## Users

- [CreateOrUpdate](#users_createorupdate)
- [Delete](#users_delete)
- [Get](#users_get)
- [ListByMongoCluster](#users_listbymongocluster)
### FirewallRules_CreateOrUpdate

```java
import com.azure.resourcemanager.mongocluster.models.FirewallRuleProperties;

/**
 * Samples for FirewallRules CreateOrUpdate.
 */
public final class FirewallRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_FirewallRuleCreate.json
     */
    /**
     * Sample code: Creates a firewall rule on a Mongo Cluster resource.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void createsAFirewallRuleOnAMongoClusterResource(
        com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.firewallRules()
            .define("rule1")
            .withExistingMongoCluster("TestGroup", "myMongoCluster")
            .withProperties(
                new FirewallRuleProperties().withStartIpAddress("0.0.0.0").withEndIpAddress("255.255.255.255"))
            .create();
    }
}
```

### FirewallRules_Delete

```java
/**
 * Samples for FirewallRules Delete.
 */
public final class FirewallRulesDeleteSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_FirewallRuleDelete.json
     */
    /**
     * Sample code: Deletes a firewall rule on a Mongo Cluster resource.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void deletesAFirewallRuleOnAMongoClusterResource(
        com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.firewallRules().delete("TestGroup", "myMongoCluster", "rule1", com.azure.core.util.Context.NONE);
    }
}
```

### FirewallRules_Get

```java
/**
 * Samples for FirewallRules Get.
 */
public final class FirewallRulesGetSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_FirewallRuleGet.json
     */
    /**
     * Sample code: Gets a firewall rule on a Mongo Cluster resource.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void
        getsAFirewallRuleOnAMongoClusterResource(com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.firewallRules()
            .getWithResponse("TestGroup", "myMongoCluster", "rule1", com.azure.core.util.Context.NONE);
    }
}
```

### FirewallRules_ListByMongoCluster

```java
/**
 * Samples for FirewallRules ListByMongoCluster.
 */
public final class FirewallRulesListByMongoClusterSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_FirewallRuleList.json
     */
    /**
     * Sample code: List the firewall rules on a Mongo Cluster resource.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void listTheFirewallRulesOnAMongoClusterResource(
        com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.firewallRules().listByMongoCluster("TestGroup", "myMongoCluster", com.azure.core.util.Context.NONE);
    }
}
```

### MongoClusters_CheckNameAvailability

```java
import com.azure.resourcemanager.mongocluster.models.CheckNameAvailabilityRequest;

/**
 * Samples for MongoClusters CheckNameAvailability.
 */
public final class MongoClustersCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_NameAvailability.json
     */
    /**
     * Sample code: Checks and confirms the Mongo Cluster name is availability for use.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void checksAndConfirmsTheMongoClusterNameIsAvailabilityForUse(
        com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.mongoClusters()
            .checkNameAvailabilityWithResponse("westus2", new CheckNameAvailabilityRequest().withName("newmongocluster")
                .withType("Microsoft.DocumentDB/mongoClusters"), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_NameAvailability_AlreadyExists.json
     */
    /**
     * Sample code: Checks and returns that the Mongo Cluster name is already in-use.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void checksAndReturnsThatTheMongoClusterNameIsAlreadyInUse(
        com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.mongoClusters()
            .checkNameAvailabilityWithResponse("westus2",
                new CheckNameAvailabilityRequest().withName("existingmongocluster")
                    .withType("Microsoft.DocumentDB/mongoClusters"),
                com.azure.core.util.Context.NONE);
    }
}
```

### MongoClusters_CreateOrUpdate

```java
import com.azure.resourcemanager.mongocluster.models.AdministratorProperties;
import com.azure.resourcemanager.mongocluster.models.AuthConfigProperties;
import com.azure.resourcemanager.mongocluster.models.AuthenticationMode;
import com.azure.resourcemanager.mongocluster.models.ComputeProperties;
import com.azure.resourcemanager.mongocluster.models.CreateMode;
import com.azure.resourcemanager.mongocluster.models.CustomerManagedKeyEncryptionProperties;
import com.azure.resourcemanager.mongocluster.models.EncryptionProperties;
import com.azure.resourcemanager.mongocluster.models.HighAvailabilityMode;
import com.azure.resourcemanager.mongocluster.models.HighAvailabilityProperties;
import com.azure.resourcemanager.mongocluster.models.KeyEncryptionKeyIdentity;
import com.azure.resourcemanager.mongocluster.models.KeyEncryptionKeyIdentityType;
import com.azure.resourcemanager.mongocluster.models.ManagedServiceIdentity;
import com.azure.resourcemanager.mongocluster.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.mongocluster.models.MongoClusterProperties;
import com.azure.resourcemanager.mongocluster.models.MongoClusterReplicaParameters;
import com.azure.resourcemanager.mongocluster.models.MongoClusterRestoreParameters;
import com.azure.resourcemanager.mongocluster.models.ShardingProperties;
import com.azure.resourcemanager.mongocluster.models.StorageProperties;
import com.azure.resourcemanager.mongocluster.models.StorageType;
import com.azure.resourcemanager.mongocluster.models.UserAssignedIdentity;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for MongoClusters CreateOrUpdate.
 */
public final class MongoClustersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_Create_SSDv2.json
     */
    /**
     * Sample code: Creates a new Mongo Cluster resource with Premium SSDv2 storage.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void createsANewMongoClusterResourceWithPremiumSSDv2Storage(
        com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.mongoClusters()
            .define("myMongoCluster")
            .withRegion("westus2")
            .withExistingResourceGroup("TestResourceGroup")
            .withProperties(new MongoClusterProperties()
                .withAdministrator(
                    new AdministratorProperties().withUserName("mongoAdmin").withPassword("fakeTokenPlaceholder"))
                .withServerVersion("5.0")
                .withHighAvailability(
                    new HighAvailabilityProperties().withTargetMode(HighAvailabilityMode.ZONE_REDUNDANT_PREFERRED))
                .withStorage(new StorageProperties().withSizeGb(32L)
                    .withType(StorageType.PREMIUM_SSDV2)
                    .withIops(3000L)
                    .withThroughput(125L))
                .withSharding(new ShardingProperties().withShardCount(1))
                .withCompute(new ComputeProperties().withTier("M30"))
                .withAuthConfig(
                    new AuthConfigProperties().withAllowedModes(Arrays.asList(AuthenticationMode.NATIVE_AUTH))))
            .create();
    }

    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_CreateGeoReplica_CMK.json
     */
    /**
     * Sample code: Creates a replica Mongo Cluster resource with Customer Managed Key encryption from a source
     * resource.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void createsAReplicaMongoClusterResourceWithCustomerManagedKeyEncryptionFromASourceResource(
        com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.mongoClusters()
            .define("myReplicaMongoCluster")
            .withRegion("centralus")
            .withExistingResourceGroup("TestResourceGroup")
            .withProperties(new MongoClusterProperties().withCreateMode(CreateMode.GEO_REPLICA)
                .withReplicaParameters(new MongoClusterReplicaParameters().withSourceResourceId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/TestResourceGroup/providers/Microsoft.DocumentDB/mongoClusters/mySourceMongoCluster")
                    .withSourceLocation("eastus"))
                .withEncryption(new EncryptionProperties()
                    .withCustomerManagedKeyEncryption(new CustomerManagedKeyEncryptionProperties()
                        .withKeyEncryptionKeyIdentity(new KeyEncryptionKeyIdentity()
                            .withIdentityType(KeyEncryptionKeyIdentityType.USER_ASSIGNED_IDENTITY)
                            .withUserAssignedIdentityResourceId(
                                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/TestResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/myidentity"))
                        .withKeyEncryptionKeyUrl("fakeTokenPlaceholder"))))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/TestResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/myidentity",
                    new UserAssignedIdentity())))
            .create();
    }

    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_Create_CMK.json
     */
    /**
     * Sample code: Creates a new Mongo Cluster resource with Customer Managed Key encryption.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void createsANewMongoClusterResourceWithCustomerManagedKeyEncryption(
        com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.mongoClusters()
            .define("myMongoCluster")
            .withRegion("westus2")
            .withExistingResourceGroup("TestResourceGroup")
            .withProperties(new MongoClusterProperties()
                .withAdministrator(
                    new AdministratorProperties().withUserName("mongoAdmin").withPassword("fakeTokenPlaceholder"))
                .withHighAvailability(new HighAvailabilityProperties().withTargetMode(HighAvailabilityMode.DISABLED))
                .withStorage(new StorageProperties().withSizeGb(32L))
                .withSharding(new ShardingProperties().withShardCount(1))
                .withCompute(new ComputeProperties().withTier("M30"))
                .withEncryption(new EncryptionProperties()
                    .withCustomerManagedKeyEncryption(new CustomerManagedKeyEncryptionProperties()
                        .withKeyEncryptionKeyIdentity(new KeyEncryptionKeyIdentity()
                            .withIdentityType(KeyEncryptionKeyIdentityType.USER_ASSIGNED_IDENTITY)
                            .withUserAssignedIdentityResourceId(
                                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/TestResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/myidentity"))
                        .withKeyEncryptionKeyUrl("fakeTokenPlaceholder"))))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/TestResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/myidentity",
                    new UserAssignedIdentity())))
            .create();
    }

    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_CreateGeoReplica.json
     */
    /**
     * Sample code: Creates a replica Mongo Cluster resource from a source resource.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void createsAReplicaMongoClusterResourceFromASourceResource(
        com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.mongoClusters()
            .define("myReplicaMongoCluster")
            .withRegion("centralus")
            .withExistingResourceGroup("TestResourceGroup")
            .withProperties(new MongoClusterProperties().withCreateMode(CreateMode.GEO_REPLICA)
                .withReplicaParameters(new MongoClusterReplicaParameters().withSourceResourceId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/TestResourceGroup/providers/Microsoft.DocumentDB/mongoClusters/mySourceMongoCluster")
                    .withSourceLocation("eastus")))
            .create();
    }

    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_CreatePITR.json
     */
    /**
     * Sample code: Creates a Mongo Cluster resource from a point in time restore.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void createsAMongoClusterResourceFromAPointInTimeRestore(
        com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.mongoClusters()
            .define("myMongoCluster")
            .withRegion("westus2")
            .withExistingResourceGroup("TestResourceGroup")
            .withProperties(new MongoClusterProperties().withCreateMode(CreateMode.POINT_IN_TIME_RESTORE)
                .withRestoreParameters(new MongoClusterRestoreParameters()
                    .withPointInTimeUTC(OffsetDateTime.parse("2023-01-13T20:07:35Z"))
                    .withSourceResourceId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/TestResourceGroup/providers/Microsoft.DocumentDB/mongoClusters/myOtherMongoCluster")))
            .create();
    }

    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_Create.json
     */
    /**
     * Sample code: Creates a new Mongo Cluster resource.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void
        createsANewMongoClusterResource(com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.mongoClusters()
            .define("myMongoCluster")
            .withRegion("westus2")
            .withExistingResourceGroup("TestResourceGroup")
            .withProperties(new MongoClusterProperties()
                .withAdministrator(
                    new AdministratorProperties().withUserName("mongoAdmin").withPassword("fakeTokenPlaceholder"))
                .withServerVersion("5.0")
                .withHighAvailability(
                    new HighAvailabilityProperties().withTargetMode(HighAvailabilityMode.ZONE_REDUNDANT_PREFERRED))
                .withStorage(new StorageProperties().withSizeGb(128L))
                .withSharding(new ShardingProperties().withShardCount(1))
                .withCompute(new ComputeProperties().withTier("M30"))
                .withAuthConfig(
                    new AuthConfigProperties().withAllowedModes(Arrays.asList(AuthenticationMode.NATIVE_AUTH))))
            .create();
    }

    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_CreatePITR_CMK.json
     */
    /**
     * Sample code: Creates a Mongo Cluster resource with Customer Managed Key encryption from a point in time restore.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void createsAMongoClusterResourceWithCustomerManagedKeyEncryptionFromAPointInTimeRestore(
        com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.mongoClusters()
            .define("myMongoCluster")
            .withRegion("westus2")
            .withExistingResourceGroup("TestResourceGroup")
            .withProperties(new MongoClusterProperties().withCreateMode(CreateMode.POINT_IN_TIME_RESTORE)
                .withRestoreParameters(new MongoClusterRestoreParameters()
                    .withPointInTimeUTC(OffsetDateTime.parse("2023-01-13T20:07:35Z"))
                    .withSourceResourceId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/TestResourceGroup/providers/Microsoft.DocumentDB/mongoClusters/myOtherMongoCluster"))
                .withEncryption(new EncryptionProperties()
                    .withCustomerManagedKeyEncryption(new CustomerManagedKeyEncryptionProperties()
                        .withKeyEncryptionKeyIdentity(new KeyEncryptionKeyIdentity()
                            .withIdentityType(KeyEncryptionKeyIdentityType.USER_ASSIGNED_IDENTITY)
                            .withUserAssignedIdentityResourceId(
                                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/TestResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/myidentity"))
                        .withKeyEncryptionKeyUrl("fakeTokenPlaceholder"))))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/TestResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/myidentity",
                    new UserAssignedIdentity())))
            .create();
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
```

### MongoClusters_Delete

```java
/**
 * Samples for MongoClusters Delete.
 */
public final class MongoClustersDeleteSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_Delete.json
     */
    /**
     * Sample code: Deletes a Mongo Cluster resource.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void
        deletesAMongoClusterResource(com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.mongoClusters().delete("TestResourceGroup", "myMongoCluster", com.azure.core.util.Context.NONE);
    }
}
```

### MongoClusters_GetByResourceGroup

```java
/**
 * Samples for MongoClusters GetByResourceGroup.
 */
public final class MongoClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_Get.json
     */
    /**
     * Sample code: Gets a Mongo Cluster resource.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void getsAMongoClusterResource(com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.mongoClusters()
            .getByResourceGroupWithResponse("TestResourceGroup", "myMongoCluster", com.azure.core.util.Context.NONE);
    }
}
```

### MongoClusters_List

```java
/**
 * Samples for MongoClusters List.
 */
public final class MongoClustersListSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_List.json
     */
    /**
     * Sample code: Lists the Mongo Cluster resources in a subscription.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void listsTheMongoClusterResourcesInASubscription(
        com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.mongoClusters().list(com.azure.core.util.Context.NONE);
    }
}
```

### MongoClusters_ListByResourceGroup

```java
/**
 * Samples for MongoClusters ListByResourceGroup.
 */
public final class MongoClustersListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_ListByResourceGroup.json
     */
    /**
     * Sample code: Lists the Mongo Cluster resources in a resource group.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void listsTheMongoClusterResourcesInAResourceGroup(
        com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.mongoClusters().listByResourceGroup("TestResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### MongoClusters_ListConnectionStrings

```java
/**
 * Samples for MongoClusters ListConnectionStrings.
 */
public final class MongoClustersListConnectionStringsSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_ListConnectionStrings.json
     */
    /**
     * Sample code: List the available connection strings for the Mongo Cluster resource.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void listTheAvailableConnectionStringsForTheMongoClusterResource(
        com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.mongoClusters()
            .listConnectionStringsWithResponse("TestGroup", "myMongoCluster", com.azure.core.util.Context.NONE);
    }
}
```

### MongoClusters_Promote

```java
import com.azure.resourcemanager.mongocluster.models.PromoteMode;
import com.azure.resourcemanager.mongocluster.models.PromoteOption;
import com.azure.resourcemanager.mongocluster.models.PromoteReplicaRequest;

/**
 * Samples for MongoClusters Promote.
 */
public final class MongoClustersPromoteSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_ForcePromoteReplica.json
     */
    /**
     * Sample code: Promotes a replica Mongo Cluster resource to a primary role.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void promotesAReplicaMongoClusterResourceToAPrimaryRole(
        com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.mongoClusters()
            .promote("TestGroup", "myMongoCluster",
                new PromoteReplicaRequest().withPromoteOption(PromoteOption.FORCED).withMode(PromoteMode.SWITCHOVER),
                com.azure.core.util.Context.NONE);
    }
}
```

### MongoClusters_Update

```java
import com.azure.resourcemanager.mongocluster.models.AdministratorProperties;
import com.azure.resourcemanager.mongocluster.models.AuthConfigProperties;
import com.azure.resourcemanager.mongocluster.models.AuthenticationMode;
import com.azure.resourcemanager.mongocluster.models.ComputeProperties;
import com.azure.resourcemanager.mongocluster.models.DataApiMode;
import com.azure.resourcemanager.mongocluster.models.DataApiProperties;
import com.azure.resourcemanager.mongocluster.models.HighAvailabilityMode;
import com.azure.resourcemanager.mongocluster.models.HighAvailabilityProperties;
import com.azure.resourcemanager.mongocluster.models.MongoCluster;
import com.azure.resourcemanager.mongocluster.models.MongoClusterUpdateProperties;
import com.azure.resourcemanager.mongocluster.models.PublicNetworkAccess;
import com.azure.resourcemanager.mongocluster.models.ShardingProperties;
import com.azure.resourcemanager.mongocluster.models.StorageProperties;
import com.azure.resourcemanager.mongocluster.models.StorageType;
import java.util.Arrays;

/**
 * Samples for MongoClusters Update.
 */
public final class MongoClustersUpdateSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_PatchEnableEntraIDAuth.json
     */
    /**
     * Sample code: Updates the allowed authentication modes to include Microsoft Entra ID authentication.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void updatesTheAllowedAuthenticationModesToIncludeMicrosoftEntraIDAuthentication(
        com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        MongoCluster resource = manager.mongoClusters()
            .getByResourceGroupWithResponse("TestResourceGroup", "myMongoCluster", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(
                new MongoClusterUpdateProperties().withAuthConfig(new AuthConfigProperties().withAllowedModes(
                    Arrays.asList(AuthenticationMode.NATIVE_AUTH, AuthenticationMode.MICROSOFT_ENTRA_ID))))
            .apply();
    }

    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_PatchSSDv2.json
     */
    /**
     * Sample code: Updates the Premium SSDv2 size, IOPS and throughput on a Mongo Cluster resource.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void updatesThePremiumSSDv2SizeIOPSAndThroughputOnAMongoClusterResource(
        com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        MongoCluster resource = manager.mongoClusters()
            .getByResourceGroupWithResponse("TestResourceGroup", "myMongoCluster", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new MongoClusterUpdateProperties().withStorage(new StorageProperties().withSizeGb(128L)
                .withType(StorageType.PREMIUM_SSDV2)
                .withIops(5000L)
                .withThroughput(1000L)))
            .apply();
    }

    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_ResetPassword.json
     */
    /**
     * Sample code: Resets the administrator login password.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void
        resetsTheAdministratorLoginPassword(com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        MongoCluster resource = manager.mongoClusters()
            .getByResourceGroupWithResponse("TestResourceGroup", "myMongoCluster", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new MongoClusterUpdateProperties().withAdministrator(
                new AdministratorProperties().withUserName("mongoAdmin").withPassword("fakeTokenPlaceholder")))
            .apply();
    }

    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_PatchDiskSize.json
     */
    /**
     * Sample code: Updates the disk size on a Mongo Cluster resource.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void
        updatesTheDiskSizeOnAMongoClusterResource(com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        MongoCluster resource = manager.mongoClusters()
            .getByResourceGroupWithResponse("TestResourceGroup", "myMongoCluster", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new MongoClusterUpdateProperties().withStorage(new StorageProperties().withSizeGb(256L)))
            .apply();
    }

    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_PatchPrivateNetworkAccess.json
     */
    /**
     * Sample code: Disables public network access on a Mongo Cluster resource with a private endpoint connection.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void disablesPublicNetworkAccessOnAMongoClusterResourceWithAPrivateEndpointConnection(
        com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        MongoCluster resource = manager.mongoClusters()
            .getByResourceGroupWithResponse("TestResourceGroup", "myMongoCluster", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new MongoClusterUpdateProperties().withPublicNetworkAccess(PublicNetworkAccess.DISABLED))
            .apply();
    }

    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_PatchDataApi.json
     */
    /**
     * Sample code: Enables data API on a mongo cluster resource.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void
        enablesDataAPIOnAMongoClusterResource(com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        MongoCluster resource = manager.mongoClusters()
            .getByResourceGroupWithResponse("TestResourceGroup", "myMongoCluster", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(
                new MongoClusterUpdateProperties().withDataApi(new DataApiProperties().withMode(DataApiMode.ENABLED)))
            .apply();
    }

    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_Update.json
     */
    /**
     * Sample code: Updates a Mongo Cluster resource.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void
        updatesAMongoClusterResource(com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        MongoCluster resource = manager.mongoClusters()
            .getByResourceGroupWithResponse("TestResourceGroup", "myMongoCluster", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new MongoClusterUpdateProperties()
                .withAdministrator(new AdministratorProperties().withUserName("mongoAdmin"))
                .withServerVersion("5.0")
                .withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
                .withHighAvailability(new HighAvailabilityProperties().withTargetMode(HighAvailabilityMode.SAME_ZONE))
                .withStorage(new StorageProperties().withSizeGb(256L).withType(StorageType.PREMIUM_SSD))
                .withSharding(new ShardingProperties().withShardCount(4))
                .withCompute(new ComputeProperties().withTier("M50"))
                .withDataApi(new DataApiProperties().withMode(DataApiMode.DISABLED))
                .withPreviewFeatures(Arrays.asList())
                .withAuthConfig(
                    new AuthConfigProperties().withAllowedModes(Arrays.asList(AuthenticationMode.NATIVE_AUTH))))
            .apply();
    }
}
```

### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void operationsList(com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Create

```java
import com.azure.resourcemanager.mongocluster.models.PrivateEndpointConnectionProperties;
import com.azure.resourcemanager.mongocluster.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.mongocluster.models.PrivateLinkServiceConnectionState;

/**
 * Samples for PrivateEndpointConnections Create.
 */
public final class PrivateEndpointConnectionsCreateSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_PrivateEndpointConnectionPut.json
     */
    /**
     * Sample code: Approves a private endpoint connection on a Mongo Cluster resource.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void approvesAPrivateEndpointConnectionOnAMongoClusterResource(
        com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.privateEndpointConnections()
            .define("pecTest")
            .withExistingMongoCluster("TestGroup", "myMongoCluster")
            .withProperties(new PrivateEndpointConnectionProperties().withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState().withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("Auto-Approved")))
            .create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
/**
 * Samples for PrivateEndpointConnections Delete.
 */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_PrivateEndpointConnectionDelete.json
     */
    /**
     * Sample code: Delete a private endpoint connection on a Mongo Cluster resource.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void deleteAPrivateEndpointConnectionOnAMongoClusterResource(
        com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.privateEndpointConnections()
            .delete("TestGroup", "myMongoCluster", "pecTest.5d393f64-ef64-46d0-9959-308321c44ac0",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
/**
 * Samples for PrivateEndpointConnections Get.
 */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_PrivateEndpointConnectionGet.json
     */
    /**
     * Sample code: Get a private endpoint connection on a Mongo Cluster resource.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void getAPrivateEndpointConnectionOnAMongoClusterResource(
        com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.privateEndpointConnections()
            .getWithResponse("TestGroup", "myMongoCluster", "pecTest.5d393f64-ef64-46d0-9959-308321c44ac0",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByMongoCluster

```java
/**
 * Samples for PrivateEndpointConnections ListByMongoCluster.
 */
public final class PrivateEndpointConnectionsListByMongoClusterSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_PrivateEndpointConnectionList.json
     */
    /**
     * Sample code: Lists the private endpoint connection resources on a Mongo Cluster resource.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void listsThePrivateEndpointConnectionResourcesOnAMongoClusterResource(
        com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.privateEndpointConnections()
            .listByMongoCluster("TestGroup", "myMongoCluster", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinks_ListByMongoCluster

```java
/**
 * Samples for PrivateLinks ListByMongoCluster.
 */
public final class PrivateLinksListByMongoClusterSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_PrivateLinkResourceList.json
     */
    /**
     * Sample code: Lists the private link resources available on a Mongo Cluster resource.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void listsThePrivateLinkResourcesAvailableOnAMongoClusterResource(
        com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.privateLinks().listByMongoCluster("TestGroup", "myMongoCluster", com.azure.core.util.Context.NONE);
    }
}
```

### Replicas_ListByParent

```java
/**
 * Samples for Replicas ListByParent.
 */
public final class ReplicasListByParentSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_ReplicaList.json
     */
    /**
     * Sample code: List the replicas linked to a Mongo Cluster resource.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void listTheReplicasLinkedToAMongoClusterResource(
        com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.replicas().listByParent("TestGroup", "myMongoCluster", com.azure.core.util.Context.NONE);
    }
}
```

### Users_CreateOrUpdate

```java
import com.azure.resourcemanager.mongocluster.models.DatabaseRole;
import com.azure.resourcemanager.mongocluster.models.EntraIdentityProvider;
import com.azure.resourcemanager.mongocluster.models.EntraIdentityProviderProperties;
import com.azure.resourcemanager.mongocluster.models.EntraPrincipalType;
import com.azure.resourcemanager.mongocluster.models.UserProperties;
import com.azure.resourcemanager.mongocluster.models.UserRole;
import java.util.Arrays;

/**
 * Samples for Users CreateOrUpdate.
 */
public final class UsersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_UserCreateOrUpdate.json
     */
    /**
     * Sample code: Creates a user on a Mongo Cluster resource.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void
        createsAUserOnAMongoClusterResource(com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.users()
            .define("uuuuuuuu-uuuu-uuuu-uuuu-uuuuuuuuuuuu")
            .withExistingMongoCluster("TestGroup", "myMongoCluster")
            .withProperties(new UserProperties()
                .withIdentityProvider(new EntraIdentityProvider()
                    .withProperties(new EntraIdentityProviderProperties().withPrincipalType(EntraPrincipalType.USER)))
                .withRoles(Arrays.asList(new DatabaseRole().withDb("admin").withRole(UserRole.ROOT))))
            .create();
    }
}
```

### Users_Delete

```java
/**
 * Samples for Users Delete.
 */
public final class UsersDeleteSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_UserDelete.json
     */
    /**
     * Sample code: Deletes a user on a Mongo Cluster resource.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void
        deletesAUserOnAMongoClusterResource(com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.users()
            .delete("TestGroup", "myMongoCluster", "uuuuuuuu-uuuu-uuuu-uuuu-uuuuuuuuuuuu",
                com.azure.core.util.Context.NONE);
    }
}
```

### Users_Get

```java
/**
 * Samples for Users Get.
 */
public final class UsersGetSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_UserGet.json
     */
    /**
     * Sample code: Gets a user on a Mongo Cluster resource.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void
        getsAUserOnAMongoClusterResource(com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.users()
            .getWithResponse("TestGroup", "myMongoCluster", "uuuuuuuu-uuuu-uuuu-uuuu-uuuuuuuuuuuu",
                com.azure.core.util.Context.NONE);
    }
}
```

### Users_ListByMongoCluster

```java
/**
 * Samples for Users ListByMongoCluster.
 */
public final class UsersListByMongoClusterSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/MongoClusters_UserList.json
     */
    /**
     * Sample code: List the users on a Mongo Cluster resource.
     * 
     * @param manager Entry point to MongoClusterManager.
     */
    public static void
        listTheUsersOnAMongoClusterResource(com.azure.resourcemanager.mongocluster.MongoClusterManager manager) {
        manager.users().listByMongoCluster("TestGroup", "myMongoCluster", com.azure.core.util.Context.NONE);
    }
}
```

