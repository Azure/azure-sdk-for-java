# Code snippets and samples


## Databases

- [Create](#databases_create)
- [Delete](#databases_delete)
- [Export](#databases_export)
- [Flush](#databases_flush)
- [ForceLinkToReplicationGroup](#databases_forcelinktoreplicationgroup)
- [ForceUnlink](#databases_forceunlink)
- [Get](#databases_get)
- [ImportMethod](#databases_importmethod)
- [ListByCluster](#databases_listbycluster)
- [ListKeys](#databases_listkeys)
- [RegenerateKey](#databases_regeneratekey)
- [Update](#databases_update)
- [UpgradeDBRedisVersion](#databases_upgradedbredisversion)

## Operations

- [List](#operations_list)

## OperationsStatus

- [Get](#operationsstatus_get)

## PrivateEndpointConnections

- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [List](#privateendpointconnections_list)
- [Put](#privateendpointconnections_put)

## PrivateLinkResources

- [ListByCluster](#privatelinkresources_listbycluster)

## RedisEnterprise

- [Create](#redisenterprise_create)
- [Delete](#redisenterprise_delete)
- [GetByResourceGroup](#redisenterprise_getbyresourcegroup)
- [List](#redisenterprise_list)
- [ListByResourceGroup](#redisenterprise_listbyresourcegroup)
- [Update](#redisenterprise_update)
### Databases_Create

```java
import com.azure.resourcemanager.redisenterprise.models.AofFrequency;
import com.azure.resourcemanager.redisenterprise.models.ClusteringPolicy;
import com.azure.resourcemanager.redisenterprise.models.DatabasePropertiesGeoReplication;
import com.azure.resourcemanager.redisenterprise.models.DeferUpgradeSetting;
import com.azure.resourcemanager.redisenterprise.models.EvictionPolicy;
import com.azure.resourcemanager.redisenterprise.models.LinkedDatabase;
import com.azure.resourcemanager.redisenterprise.models.Module;
import com.azure.resourcemanager.redisenterprise.models.Persistence;
import com.azure.resourcemanager.redisenterprise.models.Protocol;
import java.util.Arrays;

/**
 * Samples for Databases Create.
 */
public final class DatabasesCreateSamples {
    /*
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterpriseDatabasesCreate.json
     */
    /**
     * Sample code: RedisEnterpriseDatabasesCreate.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void
        redisEnterpriseDatabasesCreate(com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.databases()
            .define("default")
            .withExistingRedisEnterprise("rg1", "cache1")
            .withClientProtocol(Protocol.ENCRYPTED)
            .withPort(10000)
            .withClusteringPolicy(ClusteringPolicy.ENTERPRISE_CLUSTER)
            .withEvictionPolicy(EvictionPolicy.ALL_KEYS_LRU)
            .withPersistence(new Persistence().withAofEnabled(true).withAofFrequency(AofFrequency.ONES))
            .withModules(Arrays.asList(new Module().withName("RedisBloom").withArgs("ERROR_RATE 0.00 INITIAL_SIZE 400"),
                new Module().withName("RedisTimeSeries").withArgs("RETENTION_POLICY 20"),
                new Module().withName("RediSearch")))
            .withDeferUpgrade(DeferUpgradeSetting.NOT_DEFERRED)
            .create();
    }

    /*
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterpriseDatabasesCreateWithGeoReplication.json
     */
    /**
     * Sample code: RedisEnterpriseDatabasesCreate With Active Geo Replication.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void redisEnterpriseDatabasesCreateWithActiveGeoReplication(
        com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.databases()
            .define("default")
            .withExistingRedisEnterprise("rg1", "cache1")
            .withClientProtocol(Protocol.ENCRYPTED)
            .withPort(10000)
            .withClusteringPolicy(ClusteringPolicy.ENTERPRISE_CLUSTER)
            .withEvictionPolicy(EvictionPolicy.NO_EVICTION)
            .withGeoReplication(new DatabasePropertiesGeoReplication().withGroupNickname("groupName")
                .withLinkedDatabases(Arrays.asList(new LinkedDatabase().withId(
                    "/subscriptions/subid1/resourceGroups/rg1/providers/Microsoft.Cache/redisEnterprise/cache1/databases/default"),
                    new LinkedDatabase().withId(
                        "/subscriptions/subid2/resourceGroups/rg2/providers/Microsoft.Cache/redisEnterprise/cache2/databases/default"))))
            .create();
    }
}
```

### Databases_Delete

```java
/**
 * Samples for Databases Delete.
 */
public final class DatabasesDeleteSamples {
    /*
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterpriseDatabasesDelete.json
     */
    /**
     * Sample code: RedisEnterpriseDatabasesDelete.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void
        redisEnterpriseDatabasesDelete(com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.databases().delete("rg1", "cache1", "db1", com.azure.core.util.Context.NONE);
    }
}
```

### Databases_Export

```java
import com.azure.resourcemanager.redisenterprise.models.ExportClusterParameters;

/**
 * Samples for Databases Export.
 */
public final class DatabasesExportSamples {
    /*
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterpriseDatabasesExport.json
     */
    /**
     * Sample code: RedisEnterpriseDatabasesExport.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void
        redisEnterpriseDatabasesExport(com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.databases()
            .export("rg1", "cache1", "default",
                new ExportClusterParameters()
                    .withSasUri("https://contosostorage.blob.core.window.net/urlToBlobContainer?sasKeyParameters"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Databases_Flush

```java
import com.azure.resourcemanager.redisenterprise.models.FlushParameters;
import java.util.Arrays;

/**
 * Samples for Databases Flush.
 */
public final class DatabasesFlushSamples {
    /*
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterpriseDatabasesFlush.json
     */
    /**
     * Sample code: How to flush all the keys in the database.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void
        howToFlushAllTheKeysInTheDatabase(com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.databases()
            .flush("rg1", "cache1", "default", new FlushParameters().withIds(Arrays.asList(
                "/subscriptions/subid2/resourceGroups/rg2/providers/Microsoft.Cache/redisEnterprise/cache2/databases/default")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Databases_ForceLinkToReplicationGroup

```java
import com.azure.resourcemanager.redisenterprise.models.ForceLinkParameters;
import com.azure.resourcemanager.redisenterprise.models.LinkedDatabase;
import java.util.Arrays;

/**
 * Samples for Databases ForceLinkToReplicationGroup.
 */
public final class DatabasesForceLinkToReplicationGroupSamples {
    /*
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterpriseDatabasesForceLink.json
     */
    /**
     * Sample code: How to relink a database after a regional outage.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void howToRelinkADatabaseAfterARegionalOutage(
        com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.databases()
            .forceLinkToReplicationGroup("rg1", "cache1", "default", new ForceLinkParameters()
                .withGroupNickname("groupName")
                .withLinkedDatabases(Arrays.asList(new LinkedDatabase().withId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg1/providers/Microsoft.Cache/redisEnterprise/cache1/databases/default"),
                    new LinkedDatabase().withId(
                        "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rg2/providers/Microsoft.Cache/redisEnterprise/cache2/databases/default"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Databases_ForceUnlink

```java
import com.azure.resourcemanager.redisenterprise.models.ForceUnlinkParameters;
import java.util.Arrays;

/**
 * Samples for Databases ForceUnlink.
 */
public final class DatabasesForceUnlinkSamples {
    /*
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterpriseDatabasesForceUnlink.json
     */
    /**
     * Sample code: How to unlink a database during a regional outage.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void howToUnlinkADatabaseDuringARegionalOutage(
        com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.databases()
            .forceUnlink("rg1", "cache1", "default", new ForceUnlinkParameters().withIds(Arrays.asList(
                "/subscriptions/subid2/resourceGroups/rg2/providers/Microsoft.Cache/redisEnterprise/cache2/databases/default")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Databases_Get

```java
/**
 * Samples for Databases Get.
 */
public final class DatabasesGetSamples {
    /*
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterpriseDatabasesGet.json
     */
    /**
     * Sample code: RedisEnterpriseDatabasesGet.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void
        redisEnterpriseDatabasesGet(com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.databases().getWithResponse("rg1", "cache1", "default", com.azure.core.util.Context.NONE);
    }
}
```

### Databases_ImportMethod

```java
import com.azure.resourcemanager.redisenterprise.models.ImportClusterParameters;
import java.util.Arrays;

/**
 * Samples for Databases ImportMethod.
 */
public final class DatabasesImportMethodSamples {
    /*
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterpriseDatabasesImport.json
     */
    /**
     * Sample code: RedisEnterpriseDatabasesImport.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void
        redisEnterpriseDatabasesImport(com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.databases()
            .importMethod("rg1", "cache1", "default",
                new ImportClusterParameters().withSasUris(
                    Arrays.asList("https://contosostorage.blob.core.window.net/urltoBlobFile1?sasKeyParameters",
                        "https://contosostorage.blob.core.window.net/urltoBlobFile2?sasKeyParameters")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Databases_ListByCluster

```java
/**
 * Samples for Databases ListByCluster.
 */
public final class DatabasesListByClusterSamples {
    /*
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterpriseDatabasesListByCluster.json
     */
    /**
     * Sample code: RedisEnterpriseDatabasesListByCluster.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void redisEnterpriseDatabasesListByCluster(
        com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.databases().listByCluster("rg1", "cache1", com.azure.core.util.Context.NONE);
    }
}
```

### Databases_ListKeys

```java
/**
 * Samples for Databases ListKeys.
 */
public final class DatabasesListKeysSamples {
    /*
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterpriseDatabasesListKeys.json
     */
    /**
     * Sample code: RedisEnterpriseDatabasesListKeys.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void
        redisEnterpriseDatabasesListKeys(com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.databases().listKeysWithResponse("rg1", "cache1", "default", com.azure.core.util.Context.NONE);
    }
}
```

### Databases_RegenerateKey

```java
import com.azure.resourcemanager.redisenterprise.models.AccessKeyType;
import com.azure.resourcemanager.redisenterprise.models.RegenerateKeyParameters;

/**
 * Samples for Databases RegenerateKey.
 */
public final class DatabasesRegenerateKeySamples {
    /*
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterpriseDatabasesRegenerateKey.json
     */
    /**
     * Sample code: RedisEnterpriseDatabasesRegenerateKey.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void redisEnterpriseDatabasesRegenerateKey(
        com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.databases()
            .regenerateKey("rg1", "cache1", "default", new RegenerateKeyParameters().withKeyType(AccessKeyType.PRIMARY),
                com.azure.core.util.Context.NONE);
    }
}
```

### Databases_Update

```java
import com.azure.resourcemanager.redisenterprise.models.Database;
import com.azure.resourcemanager.redisenterprise.models.EvictionPolicy;
import com.azure.resourcemanager.redisenterprise.models.Persistence;
import com.azure.resourcemanager.redisenterprise.models.Protocol;
import com.azure.resourcemanager.redisenterprise.models.RdbFrequency;

/**
 * Samples for Databases Update.
 */
public final class DatabasesUpdateSamples {
    /*
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterpriseDatabasesUpdate.json
     */
    /**
     * Sample code: RedisEnterpriseDatabasesUpdate.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void
        redisEnterpriseDatabasesUpdate(com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        Database resource = manager.databases()
            .getWithResponse("rg1", "cache1", "default", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withClientProtocol(Protocol.ENCRYPTED)
            .withEvictionPolicy(EvictionPolicy.ALL_KEYS_LRU)
            .withPersistence(new Persistence().withRdbEnabled(true).withRdbFrequency(RdbFrequency.ONE_TWOH))
            .apply();
    }
}
```

### Databases_UpgradeDBRedisVersion

```java
/**
 * Samples for Databases UpgradeDBRedisVersion.
 */
public final class DatabasesUpgradeDBRedisVersionSamples {
    /*
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterpriseDatabasesUpgradeDBRedisVersion.json
     */
    /**
     * Sample code: How to upgrade your database Redis version.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void
        howToUpgradeYourDatabaseRedisVersion(com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.databases().upgradeDBRedisVersion("rg1", "cache1", "default", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/OperationsList.json
     */
    /**
     * Sample code: OperationsList.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void operationsList(com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### OperationsStatus_Get

```java
/**
 * Samples for OperationsStatus Get.
 */
public final class OperationsStatusGetSamples {
    /*
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/OperationsStatusGet.json
     */
    /**
     * Sample code: OperationsStatusGet.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void operationsStatusGet(com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.operationsStatus().getWithResponse("West US", "testoperationid", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterpriseDeletePrivateEndpointConnection.json
     */
    /**
     * Sample code: RedisEnterpriseDeletePrivateEndpointConnection.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void redisEnterpriseDeletePrivateEndpointConnection(
        com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.privateEndpointConnections().delete("rg1", "cache1", "pectest01", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterpriseGetPrivateEndpointConnection.json
     */
    /**
     * Sample code: RedisEnterpriseGetPrivateEndpointConnection.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void redisEnterpriseGetPrivateEndpointConnection(
        com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.privateEndpointConnections()
            .getWithResponse("rg1", "cache1", "pectest01", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_List

```java
/**
 * Samples for PrivateEndpointConnections List.
 */
public final class PrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterpriseListPrivateEndpointConnections.json
     */
    /**
     * Sample code: RedisEnterpriseListPrivateEndpointConnections.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void redisEnterpriseListPrivateEndpointConnections(
        com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.privateEndpointConnections().list("rg1", "cache1", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Put

```java
import com.azure.resourcemanager.redisenterprise.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.redisenterprise.models.PrivateLinkServiceConnectionState;

/**
 * Samples for PrivateEndpointConnections Put.
 */
public final class PrivateEndpointConnectionsPutSamples {
    /*
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterprisePutPrivateEndpointConnection.json
     */
    /**
     * Sample code: RedisEnterprisePutPrivateEndpointConnection.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void redisEnterprisePutPrivateEndpointConnection(
        com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.privateEndpointConnections()
            .define("pectest01")
            .withExistingRedisEnterprise("rg1", "cache1")
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState().withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("Auto-Approved"))
            .create();
    }
}
```

### PrivateLinkResources_ListByCluster

```java
/**
 * Samples for PrivateLinkResources ListByCluster.
 */
public final class PrivateLinkResourcesListByClusterSamples {
    /*
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterpriseListPrivateLinkResources.json
     */
    /**
     * Sample code: RedisEnterpriseListPrivateLinkResources.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void redisEnterpriseListPrivateLinkResources(
        com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.privateLinkResources().listByCluster("rg1", "cache1", com.azure.core.util.Context.NONE);
    }
}
```

### RedisEnterprise_Create

```java
import com.azure.resourcemanager.redisenterprise.models.ClusterPropertiesEncryption;
import com.azure.resourcemanager.redisenterprise.models.ClusterPropertiesEncryptionCustomerManagedKeyEncryption;
import com.azure.resourcemanager.redisenterprise.models.ClusterPropertiesEncryptionCustomerManagedKeyEncryptionKeyIdentity;
import com.azure.resourcemanager.redisenterprise.models.CmkIdentityType;
import com.azure.resourcemanager.redisenterprise.models.ManagedServiceIdentity;
import com.azure.resourcemanager.redisenterprise.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.redisenterprise.models.Sku;
import com.azure.resourcemanager.redisenterprise.models.SkuName;
import com.azure.resourcemanager.redisenterprise.models.TlsVersion;
import com.azure.resourcemanager.redisenterprise.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for RedisEnterprise Create.
 */
public final class RedisEnterpriseCreateSamples {
    /*
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterpriseCreate.json
     */
    /**
     * Sample code: RedisEnterpriseCreate.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void redisEnterpriseCreate(com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.redisEnterprises()
            .define("cache1")
            .withRegion("West US")
            .withExistingResourceGroup("rg1")
            .withSku(new Sku().withName(SkuName.ENTERPRISE_FLASH_F300).withCapacity(3))
            .withTags(mapOf("tag1", "value1"))
            .withZones(Arrays.asList("1", "2", "3"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/your-subscription/resourceGroups/your-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/your-identity",
                    new UserAssignedIdentity())))
            .withMinimumTlsVersion(TlsVersion.ONE_TWO)
            .withEncryption(new ClusterPropertiesEncryption().withCustomerManagedKeyEncryption(
                new ClusterPropertiesEncryptionCustomerManagedKeyEncryption().withKeyEncryptionKeyIdentity(
                    new ClusterPropertiesEncryptionCustomerManagedKeyEncryptionKeyIdentity()
                        .withUserAssignedIdentityResourceId(
                            "/subscriptions/your-subscription/resourceGroups/your-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/your-identity")
                        .withIdentityType(CmkIdentityType.USER_ASSIGNED_IDENTITY))
                    .withKeyEncryptionKeyUrl("fakeTokenPlaceholder")))
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

### RedisEnterprise_Delete

```java
/**
 * Samples for RedisEnterprise Delete.
 */
public final class RedisEnterpriseDeleteSamples {
    /*
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterpriseDelete.json
     */
    /**
     * Sample code: RedisEnterpriseDelete.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void redisEnterpriseDelete(com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.redisEnterprises().delete("rg1", "cache1", com.azure.core.util.Context.NONE);
    }
}
```

### RedisEnterprise_GetByResourceGroup

```java
/**
 * Samples for RedisEnterprise GetByResourceGroup.
 */
public final class RedisEnterpriseGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterpriseGet.json
     */
    /**
     * Sample code: RedisEnterpriseGet.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void redisEnterpriseGet(com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.redisEnterprises().getByResourceGroupWithResponse("rg1", "cache1", com.azure.core.util.Context.NONE);
    }
}
```

### RedisEnterprise_List

```java
/**
 * Samples for RedisEnterprise List.
 */
public final class RedisEnterpriseListSamples {
    /*
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterpriseList.json
     */
    /**
     * Sample code: RedisEnterpriseList.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void redisEnterpriseList(com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.redisEnterprises().list(com.azure.core.util.Context.NONE);
    }
}
```

### RedisEnterprise_ListByResourceGroup

```java
/**
 * Samples for RedisEnterprise ListByResourceGroup.
 */
public final class RedisEnterpriseListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterpriseListByResourceGroup.json
     */
    /**
     * Sample code: RedisEnterpriseListByResourceGroup.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void
        redisEnterpriseListByResourceGroup(com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        manager.redisEnterprises().listByResourceGroup("rg1", com.azure.core.util.Context.NONE);
    }
}
```

### RedisEnterprise_Update

```java
import com.azure.resourcemanager.redisenterprise.models.Cluster;
import com.azure.resourcemanager.redisenterprise.models.Sku;
import com.azure.resourcemanager.redisenterprise.models.SkuName;
import com.azure.resourcemanager.redisenterprise.models.TlsVersion;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for RedisEnterprise Update.
 */
public final class RedisEnterpriseUpdateSamples {
    /*
     * x-ms-original-file: specification/redisenterprise/resource-manager/Microsoft.Cache/preview/2024-03-01-preview/examples/RedisEnterpriseUpdate.json
     */
    /**
     * Sample code: RedisEnterpriseUpdate.
     * 
     * @param manager Entry point to RedisEnterpriseManager.
     */
    public static void redisEnterpriseUpdate(com.azure.resourcemanager.redisenterprise.RedisEnterpriseManager manager) {
        Cluster resource = manager.redisEnterprises()
            .getByResourceGroupWithResponse("rg1", "cache1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("tag1", "value1"))
            .withSku(new Sku().withName(SkuName.ENTERPRISE_FLASH_F300).withCapacity(9))
            .withMinimumTlsVersion(TlsVersion.ONE_TWO)
            .apply();
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

