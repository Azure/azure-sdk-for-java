# Code snippets and samples


## BlobContainers

- [ClearLegalHold](#blobcontainers_clearlegalhold)
- [Create](#blobcontainers_create)
- [CreateOrUpdateImmutabilityPolicy](#blobcontainers_createorupdateimmutabilitypolicy)
- [Delete](#blobcontainers_delete)
- [DeleteImmutabilityPolicy](#blobcontainers_deleteimmutabilitypolicy)
- [ExtendImmutabilityPolicy](#blobcontainers_extendimmutabilitypolicy)
- [Get](#blobcontainers_get)
- [GetImmutabilityPolicy](#blobcontainers_getimmutabilitypolicy)
- [Lease](#blobcontainers_lease)
- [List](#blobcontainers_list)
- [LockImmutabilityPolicy](#blobcontainers_lockimmutabilitypolicy)
- [ObjectLevelWorm](#blobcontainers_objectlevelworm)
- [SetLegalHold](#blobcontainers_setlegalhold)
- [Update](#blobcontainers_update)

## BlobInventoryPolicies

- [CreateOrUpdate](#blobinventorypolicies_createorupdate)
- [Delete](#blobinventorypolicies_delete)
- [Get](#blobinventorypolicies_get)
- [List](#blobinventorypolicies_list)

## BlobServices

- [GetServiceProperties](#blobservices_getserviceproperties)
- [List](#blobservices_list)
- [SetServiceProperties](#blobservices_setserviceproperties)

## DeletedAccounts

- [Get](#deletedaccounts_get)
- [List](#deletedaccounts_list)

## EncryptionScopes

- [Get](#encryptionscopes_get)
- [List](#encryptionscopes_list)
- [Patch](#encryptionscopes_patch)
- [Put](#encryptionscopes_put)

## FileServices

- [GetServiceProperties](#fileservices_getserviceproperties)
- [List](#fileservices_list)
- [SetServiceProperties](#fileservices_setserviceproperties)

## FileShares

- [Create](#fileshares_create)
- [Delete](#fileshares_delete)
- [Get](#fileshares_get)
- [Lease](#fileshares_lease)
- [List](#fileshares_list)
- [Restore](#fileshares_restore)
- [Update](#fileshares_update)

## LocalUsersOperation

- [CreateOrUpdate](#localusersoperation_createorupdate)
- [Delete](#localusersoperation_delete)
- [Get](#localusersoperation_get)
- [List](#localusersoperation_list)
- [ListKeys](#localusersoperation_listkeys)
- [RegeneratePassword](#localusersoperation_regeneratepassword)

## ManagementPolicies

- [CreateOrUpdate](#managementpolicies_createorupdate)
- [Delete](#managementpolicies_delete)
- [Get](#managementpolicies_get)

## ObjectReplicationPoliciesOperation

- [CreateOrUpdate](#objectreplicationpoliciesoperation_createorupdate)
- [Delete](#objectreplicationpoliciesoperation_delete)
- [Get](#objectreplicationpoliciesoperation_get)
- [List](#objectreplicationpoliciesoperation_list)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [List](#privateendpointconnections_list)
- [Put](#privateendpointconnections_put)

## PrivateLinkResources

- [ListByStorageAccount](#privatelinkresources_listbystorageaccount)

## Queue

- [Create](#queue_create)
- [Delete](#queue_delete)
- [Get](#queue_get)
- [List](#queue_list)
- [Update](#queue_update)

## QueueServices

- [GetServiceProperties](#queueservices_getserviceproperties)
- [List](#queueservices_list)
- [SetServiceProperties](#queueservices_setserviceproperties)

## Skus

- [List](#skus_list)

## StorageAccounts

- [AbortHierarchicalNamespaceMigration](#storageaccounts_aborthierarchicalnamespacemigration)
- [CheckNameAvailability](#storageaccounts_checknameavailability)
- [Create](#storageaccounts_create)
- [Delete](#storageaccounts_delete)
- [Failover](#storageaccounts_failover)
- [GetByResourceGroup](#storageaccounts_getbyresourcegroup)
- [HierarchicalNamespaceMigration](#storageaccounts_hierarchicalnamespacemigration)
- [List](#storageaccounts_list)
- [ListAccountSas](#storageaccounts_listaccountsas)
- [ListByResourceGroup](#storageaccounts_listbyresourcegroup)
- [ListKeys](#storageaccounts_listkeys)
- [ListServiceSas](#storageaccounts_listservicesas)
- [RegenerateKey](#storageaccounts_regeneratekey)
- [RestoreBlobRanges](#storageaccounts_restoreblobranges)
- [RevokeUserDelegationKeys](#storageaccounts_revokeuserdelegationkeys)
- [Update](#storageaccounts_update)

## Table

- [Create](#table_create)
- [Delete](#table_delete)
- [Get](#table_get)
- [List](#table_list)
- [Update](#table_update)

## TableServices

- [GetServiceProperties](#tableservices_getserviceproperties)
- [List](#tableservices_list)
- [SetServiceProperties](#tableservices_setserviceproperties)

## Usages

- [ListByLocation](#usages_listbylocation)
### BlobContainers_ClearLegalHold

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.generated.fluent.models.LegalHoldInner;
import java.util.Arrays;

/** Samples for BlobContainers ClearLegalHold. */
public final class BlobContainersClearLegalHoldSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobContainersClearLegalHold.json
     */
    /**
     * Sample code: ClearLegalHoldContainers.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void clearLegalHoldContainers(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .blobContainers()
            .clearLegalHoldWithResponse(
                "res4303",
                "sto7280",
                "container8723",
                new LegalHoldInner().withTags(Arrays.asList("tag1", "tag2", "tag3")),
                Context.NONE);
    }
}
```

### BlobContainers_Create

```java
import com.azure.resourcemanager.storage.generated.models.ImmutableStorageWithVersioning;

/** Samples for BlobContainers Create. */
public final class BlobContainersCreateSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobContainersPutDefaultEncryptionScope.json
     */
    /**
     * Sample code: PutContainerWithDefaultEncryptionScope.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void putContainerWithDefaultEncryptionScope(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .blobContainers()
            .define("container6185")
            .withExistingStorageAccount("res3376", "sto328")
            .withDefaultEncryptionScope("encryptionscope185")
            .withDenyEncryptionScopeOverride(true)
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobContainersPutObjectLevelWorm.json
     */
    /**
     * Sample code: PutContainerWithObjectLevelWorm.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void putContainerWithObjectLevelWorm(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .blobContainers()
            .define("container6185")
            .withExistingStorageAccount("res3376", "sto328")
            .withImmutableStorageWithVersioning(new ImmutableStorageWithVersioning().withEnabled(true))
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobContainersPut.json
     */
    /**
     * Sample code: PutContainers.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void putContainers(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.blobContainers().define("container6185").withExistingStorageAccount("res3376", "sto328").create();
    }
}
```

### BlobContainers_CreateOrUpdateImmutabilityPolicy

```java
/** Samples for BlobContainers CreateOrUpdateImmutabilityPolicy. */
public final class BlobContainersCreateOrUpdateImmutabilityPolicySamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobContainersPutImmutabilityPolicy.json
     */
    /**
     * Sample code: CreateOrUpdateImmutabilityPolicy.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void createOrUpdateImmutabilityPolicy(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .blobContainers()
            .defineImmutabilityPolicy()
            .withExistingContainer("res1782", "sto7069", "container6397")
            .withImmutabilityPeriodSinceCreationInDays(3)
            .withAllowProtectedAppendWrites(true)
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobContainersPutImmutabilityPolicyAllowProtectedAppendWritesAll.json
     */
    /**
     * Sample code: CreateOrUpdateImmutabilityPolicyWithAllowProtectedAppendWritesAll.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void createOrUpdateImmutabilityPolicyWithAllowProtectedAppendWritesAll(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .blobContainers()
            .defineImmutabilityPolicy()
            .withExistingContainer("res1782", "sto7069", "container6397")
            .withImmutabilityPeriodSinceCreationInDays(3)
            .withAllowProtectedAppendWritesAll(true)
            .create();
    }
}
```

### BlobContainers_Delete

```java
import com.azure.core.util.Context;

/** Samples for BlobContainers Delete. */
public final class BlobContainersDeleteSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobContainersDelete.json
     */
    /**
     * Sample code: DeleteContainers.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void deleteContainers(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.blobContainers().deleteWithResponse("res4079", "sto4506", "container9689", Context.NONE);
    }
}
```

### BlobContainers_DeleteImmutabilityPolicy

```java
import com.azure.core.util.Context;

/** Samples for BlobContainers DeleteImmutabilityPolicy. */
public final class BlobContainersDeleteImmutabilityPolicySamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobContainersDeleteImmutabilityPolicy.json
     */
    /**
     * Sample code: DeleteImmutabilityPolicy.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void deleteImmutabilityPolicy(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .blobContainers()
            .deleteImmutabilityPolicyWithResponse(
                "res1581", "sto9621", "container4910", "\"8d59f81a7fa7be0\"", Context.NONE);
    }
}
```

### BlobContainers_ExtendImmutabilityPolicy

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.generated.fluent.models.ImmutabilityPolicyInner;

/** Samples for BlobContainers ExtendImmutabilityPolicy. */
public final class BlobContainersExtendImmutabilityPolicySamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobContainersExtendImmutabilityPolicy.json
     */
    /**
     * Sample code: ExtendImmutabilityPolicy.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void extendImmutabilityPolicy(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .blobContainers()
            .extendImmutabilityPolicyWithResponse(
                "res6238",
                "sto232",
                "container5023",
                "\"8d59f830d0c3bf9\"",
                new ImmutabilityPolicyInner().withImmutabilityPeriodSinceCreationInDays(100),
                Context.NONE);
    }
}
```

### BlobContainers_Get

```java
import com.azure.core.util.Context;

/** Samples for BlobContainers Get. */
public final class BlobContainersGetSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobContainersGet.json
     */
    /**
     * Sample code: GetContainers.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void getContainers(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.blobContainers().getWithResponse("res9871", "sto6217", "container1634", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobContainersGetWithAllowProtectedAppendWritesAll.json
     */
    /**
     * Sample code: GetBlobContainersGetWithAllowProtectedAppendWritesAll.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void getBlobContainersGetWithAllowProtectedAppendWritesAll(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.blobContainers().getWithResponse("res9871", "sto6217", "container1634", Context.NONE);
    }
}
```

### BlobContainers_GetImmutabilityPolicy

```java
import com.azure.core.util.Context;

/** Samples for BlobContainers GetImmutabilityPolicy. */
public final class BlobContainersGetImmutabilityPolicySamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobContainersGetImmutabilityPolicy.json
     */
    /**
     * Sample code: GetImmutabilityPolicy.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void getImmutabilityPolicy(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .blobContainers()
            .getImmutabilityPolicyWithResponse("res5221", "sto9177", "container3489", null, Context.NONE);
    }
}
```

### BlobContainers_Lease

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.generated.models.LeaseContainerRequest;
import com.azure.resourcemanager.storage.generated.models.LeaseContainerRequestAction;

/** Samples for BlobContainers Lease. */
public final class BlobContainersLeaseSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobContainersLease_Break.json
     */
    /**
     * Sample code: Break a lease on a container.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void breakALeaseOnAContainer(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .blobContainers()
            .leaseWithResponse(
                "res3376",
                "sto328",
                "container6185",
                new LeaseContainerRequest()
                    .withAction(LeaseContainerRequestAction.BREAK)
                    .withLeaseId("8698f513-fa75-44a1-b8eb-30ba336af27d"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobContainersLease_Acquire.json
     */
    /**
     * Sample code: Acquire a lease on a container.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void acquireALeaseOnAContainer(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .blobContainers()
            .leaseWithResponse(
                "res3376",
                "sto328",
                "container6185",
                new LeaseContainerRequest().withAction(LeaseContainerRequestAction.ACQUIRE).withLeaseDuration(-1),
                Context.NONE);
    }
}
```

### BlobContainers_List

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.generated.models.ListContainersInclude;

/** Samples for BlobContainers List. */
public final class BlobContainersListSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobContainersList.json
     */
    /**
     * Sample code: ListContainers.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void listContainers(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.blobContainers().list("res9290", "sto1590", null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/DeletedBlobContainersList.json
     */
    /**
     * Sample code: ListDeletedContainers.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void listDeletedContainers(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.blobContainers().list("res9290", "sto1590", null, null, ListContainersInclude.DELETED, Context.NONE);
    }
}
```

### BlobContainers_LockImmutabilityPolicy

```java
import com.azure.core.util.Context;

/** Samples for BlobContainers LockImmutabilityPolicy. */
public final class BlobContainersLockImmutabilityPolicySamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobContainersLockImmutabilityPolicy.json
     */
    /**
     * Sample code: LockImmutabilityPolicy.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void lockImmutabilityPolicy(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .blobContainers()
            .lockImmutabilityPolicyWithResponse(
                "res2702", "sto5009", "container1631", "\"8d59f825b721dd3\"", Context.NONE);
    }
}
```

### BlobContainers_ObjectLevelWorm

```java
import com.azure.core.util.Context;

/** Samples for BlobContainers ObjectLevelWorm. */
public final class BlobContainersObjectLevelWormSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/ObjectLevelWormContainerMigration.json
     */
    /**
     * Sample code: VersionLevelWormContainerMigration.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void versionLevelWormContainerMigration(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.blobContainers().objectLevelWorm("res1782", "sto7069", "container6397", Context.NONE);
    }
}
```

### BlobContainers_SetLegalHold

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.generated.fluent.models.LegalHoldInner;
import java.util.Arrays;

/** Samples for BlobContainers SetLegalHold. */
public final class BlobContainersSetLegalHoldSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobContainersSetLegalHold.json
     */
    /**
     * Sample code: SetLegalHoldContainers.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void setLegalHoldContainers(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .blobContainers()
            .setLegalHoldWithResponse(
                "res4303",
                "sto7280",
                "container8723",
                new LegalHoldInner().withTags(Arrays.asList("tag1", "tag2", "tag3")),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobContainersSetLegalHoldAllowProtectedAppendWritesAll.json
     */
    /**
     * Sample code: SetLegalHoldContainersWithAllowProtectedAppendWritesAll.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void setLegalHoldContainersWithAllowProtectedAppendWritesAll(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .blobContainers()
            .setLegalHoldWithResponse(
                "res4303",
                "sto7280",
                "container8723",
                new LegalHoldInner()
                    .withTags(Arrays.asList("tag1", "tag2", "tag3"))
                    .withAllowProtectedAppendWritesAll(true),
                Context.NONE);
    }
}
```

### BlobContainers_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.generated.models.BlobContainer;
import com.azure.resourcemanager.storage.generated.models.PublicAccess;
import java.util.HashMap;
import java.util.Map;

/** Samples for BlobContainers Update. */
public final class BlobContainersUpdateSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobContainersPatch.json
     */
    /**
     * Sample code: UpdateContainers.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void updateContainers(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        BlobContainer resource =
            manager.blobContainers().getWithResponse("res3376", "sto328", "container6185", Context.NONE).getValue();
        resource.update().withPublicAccess(PublicAccess.CONTAINER).withMetadata(mapOf("metadata", "true")).apply();
    }

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

### BlobInventoryPolicies_CreateOrUpdate

```java
import com.azure.resourcemanager.storage.generated.models.BlobInventoryPolicyDefinition;
import com.azure.resourcemanager.storage.generated.models.BlobInventoryPolicyFilter;
import com.azure.resourcemanager.storage.generated.models.BlobInventoryPolicyName;
import com.azure.resourcemanager.storage.generated.models.BlobInventoryPolicyRule;
import com.azure.resourcemanager.storage.generated.models.BlobInventoryPolicySchema;
import com.azure.resourcemanager.storage.generated.models.Format;
import com.azure.resourcemanager.storage.generated.models.InventoryRuleType;
import com.azure.resourcemanager.storage.generated.models.ObjectType;
import com.azure.resourcemanager.storage.generated.models.Schedule;
import java.util.Arrays;

/** Samples for BlobInventoryPolicies CreateOrUpdate. */
public final class BlobInventoryPoliciesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountSetBlobInventoryPolicyIncludeDeleteAndNewSchemaForNonHnsAccount.json
     */
    /**
     * Sample code: StorageAccountSetBlobInventoryPolicyIncludeDeleteAndNewSchemaForNonHnsAccount.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountSetBlobInventoryPolicyIncludeDeleteAndNewSchemaForNonHnsAccount(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .blobInventoryPolicies()
            .define(BlobInventoryPolicyName.DEFAULT)
            .withExistingStorageAccount("res7687", "sto9699")
            .withPolicy(
                new BlobInventoryPolicySchema()
                    .withEnabled(true)
                    .withType(InventoryRuleType.INVENTORY)
                    .withRules(
                        Arrays
                            .asList(
                                new BlobInventoryPolicyRule()
                                    .withEnabled(true)
                                    .withName("inventoryPolicyRule1")
                                    .withDestination("container1")
                                    .withDefinition(
                                        new BlobInventoryPolicyDefinition()
                                            .withFilters(
                                                new BlobInventoryPolicyFilter()
                                                    .withPrefixMatch(
                                                        Arrays.asList("inventoryprefix1", "inventoryprefix2"))
                                                    .withExcludePrefix(
                                                        Arrays.asList("excludeprefix1", "excludeprefix2"))
                                                    .withBlobTypes(Arrays.asList("blockBlob", "appendBlob", "pageBlob"))
                                                    .withIncludeBlobVersions(true)
                                                    .withIncludeSnapshots(true)
                                                    .withIncludeDeleted(true))
                                            .withFormat(Format.CSV)
                                            .withSchedule(Schedule.DAILY)
                                            .withObjectType(ObjectType.BLOB)
                                            .withSchemaFields(
                                                Arrays
                                                    .asList(
                                                        "Name",
                                                        "Creation-Time",
                                                        "Last-Modified",
                                                        "Content-Length",
                                                        "Content-MD5",
                                                        "BlobType",
                                                        "AccessTier",
                                                        "AccessTierChangeTime",
                                                        "Snapshot",
                                                        "VersionId",
                                                        "IsCurrentVersion",
                                                        "Tags",
                                                        "ContentType",
                                                        "ContentEncoding",
                                                        "ContentLanguage",
                                                        "ContentCRC64",
                                                        "CacheControl",
                                                        "Metadata",
                                                        "Deleted",
                                                        "RemainingRetentionDays"))),
                                new BlobInventoryPolicyRule()
                                    .withEnabled(true)
                                    .withName("inventoryPolicyRule2")
                                    .withDestination("container2")
                                    .withDefinition(
                                        new BlobInventoryPolicyDefinition()
                                            .withFormat(Format.PARQUET)
                                            .withSchedule(Schedule.WEEKLY)
                                            .withObjectType(ObjectType.CONTAINER)
                                            .withSchemaFields(
                                                Arrays
                                                    .asList(
                                                        "Name",
                                                        "Last-Modified",
                                                        "Metadata",
                                                        "LeaseStatus",
                                                        "LeaseState",
                                                        "LeaseDuration",
                                                        "PublicAccess",
                                                        "HasImmutabilityPolicy",
                                                        "HasLegalHold",
                                                        "Etag",
                                                        "DefaultEncryptionScope",
                                                        "DenyEncryptionScopeOverride",
                                                        "ImmutableStorageWithVersioningEnabled",
                                                        "Deleted",
                                                        "Version",
                                                        "DeletedTime",
                                                        "RemainingRetentionDays"))))))
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountSetBlobInventoryPolicy.json
     */
    /**
     * Sample code: StorageAccountSetBlobInventoryPolicy.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountSetBlobInventoryPolicy(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .blobInventoryPolicies()
            .define(BlobInventoryPolicyName.DEFAULT)
            .withExistingStorageAccount("res7687", "sto9699")
            .withPolicy(
                new BlobInventoryPolicySchema()
                    .withEnabled(true)
                    .withType(InventoryRuleType.INVENTORY)
                    .withRules(
                        Arrays
                            .asList(
                                new BlobInventoryPolicyRule()
                                    .withEnabled(true)
                                    .withName("inventoryPolicyRule1")
                                    .withDestination("container1")
                                    .withDefinition(
                                        new BlobInventoryPolicyDefinition()
                                            .withFilters(
                                                new BlobInventoryPolicyFilter()
                                                    .withPrefixMatch(
                                                        Arrays.asList("inventoryprefix1", "inventoryprefix2"))
                                                    .withExcludePrefix(
                                                        Arrays.asList("excludeprefix1", "excludeprefix2"))
                                                    .withBlobTypes(Arrays.asList("blockBlob", "appendBlob", "pageBlob"))
                                                    .withIncludeBlobVersions(true)
                                                    .withIncludeSnapshots(true))
                                            .withFormat(Format.CSV)
                                            .withSchedule(Schedule.DAILY)
                                            .withObjectType(ObjectType.BLOB)
                                            .withSchemaFields(
                                                Arrays
                                                    .asList(
                                                        "Name",
                                                        "Creation-Time",
                                                        "Last-Modified",
                                                        "Content-Length",
                                                        "Content-MD5",
                                                        "BlobType",
                                                        "AccessTier",
                                                        "AccessTierChangeTime",
                                                        "Snapshot",
                                                        "VersionId",
                                                        "IsCurrentVersion",
                                                        "Metadata"))),
                                new BlobInventoryPolicyRule()
                                    .withEnabled(true)
                                    .withName("inventoryPolicyRule2")
                                    .withDestination("container2")
                                    .withDefinition(
                                        new BlobInventoryPolicyDefinition()
                                            .withFormat(Format.PARQUET)
                                            .withSchedule(Schedule.WEEKLY)
                                            .withObjectType(ObjectType.CONTAINER)
                                            .withSchemaFields(
                                                Arrays
                                                    .asList(
                                                        "Name",
                                                        "Last-Modified",
                                                        "Metadata",
                                                        "LeaseStatus",
                                                        "LeaseState",
                                                        "LeaseDuration",
                                                        "PublicAccess",
                                                        "HasImmutabilityPolicy",
                                                        "HasLegalHold"))))))
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountSetBlobInventoryPolicyIncludeDeleteAndNewSchemaForHnsAccount.json
     */
    /**
     * Sample code: StorageAccountSetBlobInventoryPolicyIncludeDeleteAndNewSchemaForHnsAccount.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountSetBlobInventoryPolicyIncludeDeleteAndNewSchemaForHnsAccount(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .blobInventoryPolicies()
            .define(BlobInventoryPolicyName.DEFAULT)
            .withExistingStorageAccount("res7687", "sto9699")
            .withPolicy(
                new BlobInventoryPolicySchema()
                    .withEnabled(true)
                    .withType(InventoryRuleType.INVENTORY)
                    .withRules(
                        Arrays
                            .asList(
                                new BlobInventoryPolicyRule()
                                    .withEnabled(true)
                                    .withName("inventoryPolicyRule1")
                                    .withDestination("container1")
                                    .withDefinition(
                                        new BlobInventoryPolicyDefinition()
                                            .withFilters(
                                                new BlobInventoryPolicyFilter()
                                                    .withPrefixMatch(
                                                        Arrays.asList("inventoryprefix1", "inventoryprefix2"))
                                                    .withExcludePrefix(
                                                        Arrays.asList("excludeprefix1", "excludeprefix2"))
                                                    .withBlobTypes(Arrays.asList("blockBlob", "appendBlob", "pageBlob"))
                                                    .withIncludeBlobVersions(true)
                                                    .withIncludeSnapshots(true)
                                                    .withIncludeDeleted(true))
                                            .withFormat(Format.CSV)
                                            .withSchedule(Schedule.DAILY)
                                            .withObjectType(ObjectType.BLOB)
                                            .withSchemaFields(
                                                Arrays
                                                    .asList(
                                                        "Name",
                                                        "Creation-Time",
                                                        "Last-Modified",
                                                        "Content-Length",
                                                        "Content-MD5",
                                                        "BlobType",
                                                        "AccessTier",
                                                        "AccessTierChangeTime",
                                                        "Snapshot",
                                                        "VersionId",
                                                        "IsCurrentVersion",
                                                        "ContentType",
                                                        "ContentEncoding",
                                                        "ContentLanguage",
                                                        "ContentCRC64",
                                                        "CacheControl",
                                                        "Metadata",
                                                        "DeletionId",
                                                        "Deleted",
                                                        "DeletedTime",
                                                        "RemainingRetentionDays"))),
                                new BlobInventoryPolicyRule()
                                    .withEnabled(true)
                                    .withName("inventoryPolicyRule2")
                                    .withDestination("container2")
                                    .withDefinition(
                                        new BlobInventoryPolicyDefinition()
                                            .withFormat(Format.PARQUET)
                                            .withSchedule(Schedule.WEEKLY)
                                            .withObjectType(ObjectType.CONTAINER)
                                            .withSchemaFields(
                                                Arrays
                                                    .asList(
                                                        "Name",
                                                        "Last-Modified",
                                                        "Metadata",
                                                        "LeaseStatus",
                                                        "LeaseState",
                                                        "LeaseDuration",
                                                        "PublicAccess",
                                                        "HasImmutabilityPolicy",
                                                        "HasLegalHold",
                                                        "Etag",
                                                        "DefaultEncryptionScope",
                                                        "DenyEncryptionScopeOverride",
                                                        "ImmutableStorageWithVersioningEnabled",
                                                        "Deleted",
                                                        "Version",
                                                        "DeletedTime",
                                                        "RemainingRetentionDays"))))))
            .create();
    }
}
```

### BlobInventoryPolicies_Delete

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.generated.models.BlobInventoryPolicyName;

/** Samples for BlobInventoryPolicies Delete. */
public final class BlobInventoryPoliciesDeleteSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountDeleteBlobInventoryPolicy.json
     */
    /**
     * Sample code: StorageAccountDeleteBlobInventoryPolicy.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountDeleteBlobInventoryPolicy(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .blobInventoryPolicies()
            .deleteWithResponse("res6977", "sto2527", BlobInventoryPolicyName.DEFAULT, Context.NONE);
    }
}
```

### BlobInventoryPolicies_Get

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.generated.models.BlobInventoryPolicyName;

/** Samples for BlobInventoryPolicies Get. */
public final class BlobInventoryPoliciesGetSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountGetBlobInventoryPolicy.json
     */
    /**
     * Sample code: StorageAccountGetBlobInventoryPolicy.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountGetBlobInventoryPolicy(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .blobInventoryPolicies()
            .getWithResponse("res7687", "sto9699", BlobInventoryPolicyName.DEFAULT, Context.NONE);
    }
}
```

### BlobInventoryPolicies_List

```java
import com.azure.core.util.Context;

/** Samples for BlobInventoryPolicies List. */
public final class BlobInventoryPoliciesListSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountListBlobInventoryPolicy.json
     */
    /**
     * Sample code: StorageAccountGetBlobInventoryPolicy.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountGetBlobInventoryPolicy(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.blobInventoryPolicies().list("res7687", "sto9699", Context.NONE);
    }
}
```

### BlobServices_GetServiceProperties

```java
import com.azure.core.util.Context;

/** Samples for BlobServices GetServiceProperties. */
public final class BlobServicesGetServicePropertiesSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobServicesGet.json
     */
    /**
     * Sample code: GetBlobServices.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void getBlobServices(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.blobServices().getServicePropertiesWithResponse("res4410", "sto8607", Context.NONE);
    }
}
```

### BlobServices_List

```java
import com.azure.core.util.Context;

/** Samples for BlobServices List. */
public final class BlobServicesListSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobServicesList.json
     */
    /**
     * Sample code: ListBlobServices.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void listBlobServices(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.blobServices().list("res4410", "sto8607", Context.NONE);
    }
}
```

### BlobServices_SetServiceProperties

```java
import com.azure.resourcemanager.storage.generated.models.ChangeFeed;
import com.azure.resourcemanager.storage.generated.models.CorsRule;
import com.azure.resourcemanager.storage.generated.models.CorsRuleAllowedMethodsItem;
import com.azure.resourcemanager.storage.generated.models.CorsRules;
import com.azure.resourcemanager.storage.generated.models.DeleteRetentionPolicy;
import com.azure.resourcemanager.storage.generated.models.LastAccessTimeTrackingPolicy;
import com.azure.resourcemanager.storage.generated.models.Name;
import java.util.Arrays;

/** Samples for BlobServices SetServiceProperties. */
public final class BlobServicesSetServicePropertiesSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobServicesPut.json
     */
    /**
     * Sample code: PutBlobServices.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void putBlobServices(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .blobServices()
            .define()
            .withExistingStorageAccount("res4410", "sto8607")
            .withCors(
                new CorsRules()
                    .withCorsRules(
                        Arrays
                            .asList(
                                new CorsRule()
                                    .withAllowedOrigins(
                                        Arrays.asList("http://www.contoso.com", "http://www.fabrikam.com"))
                                    .withAllowedMethods(
                                        Arrays
                                            .asList(
                                                CorsRuleAllowedMethodsItem.GET,
                                                CorsRuleAllowedMethodsItem.HEAD,
                                                CorsRuleAllowedMethodsItem.POST,
                                                CorsRuleAllowedMethodsItem.OPTIONS,
                                                CorsRuleAllowedMethodsItem.MERGE,
                                                CorsRuleAllowedMethodsItem.PUT))
                                    .withMaxAgeInSeconds(100)
                                    .withExposedHeaders(Arrays.asList("x-ms-meta-*"))
                                    .withAllowedHeaders(
                                        Arrays.asList("x-ms-meta-abc", "x-ms-meta-data*", "x-ms-meta-target*")),
                                new CorsRule()
                                    .withAllowedOrigins(Arrays.asList("*"))
                                    .withAllowedMethods(Arrays.asList(CorsRuleAllowedMethodsItem.GET))
                                    .withMaxAgeInSeconds(2)
                                    .withExposedHeaders(Arrays.asList("*"))
                                    .withAllowedHeaders(Arrays.asList("*")),
                                new CorsRule()
                                    .withAllowedOrigins(
                                        Arrays.asList("http://www.abc23.com", "https://www.fabrikam.com/*"))
                                    .withAllowedMethods(
                                        Arrays.asList(CorsRuleAllowedMethodsItem.GET, CorsRuleAllowedMethodsItem.PUT))
                                    .withMaxAgeInSeconds(2000)
                                    .withExposedHeaders(
                                        Arrays.asList("x-ms-meta-abc", "x-ms-meta-data*", "x -ms-meta-target*"))
                                    .withAllowedHeaders(Arrays.asList("x-ms-meta-12345675754564*")))))
            .withDefaultServiceVersion("2017-07-29")
            .withDeleteRetentionPolicy(new DeleteRetentionPolicy().withEnabled(true).withDays(300))
            .withIsVersioningEnabled(true)
            .withChangeFeed(new ChangeFeed().withEnabled(true).withRetentionInDays(7))
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobServicesPutAllowPermanentDelete.json
     */
    /**
     * Sample code: BlobServicesPutAllowPermanentDelete.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void blobServicesPutAllowPermanentDelete(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .blobServices()
            .define()
            .withExistingStorageAccount("res4410", "sto8607")
            .withDeleteRetentionPolicy(
                new DeleteRetentionPolicy().withEnabled(true).withDays(300).withAllowPermanentDelete(true))
            .withIsVersioningEnabled(true)
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobServicesPutLastAccessTimeBasedTracking.json
     */
    /**
     * Sample code: BlobServicesPutLastAccessTimeBasedTracking.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void blobServicesPutLastAccessTimeBasedTracking(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .blobServices()
            .define()
            .withExistingStorageAccount("res4410", "sto8607")
            .withLastAccessTimeTrackingPolicy(
                new LastAccessTimeTrackingPolicy()
                    .withEnable(true)
                    .withName(Name.ACCESS_TIME_TRACKING)
                    .withTrackingGranularityInDays(1)
                    .withBlobType(Arrays.asList("blockBlob")))
            .create();
    }
}
```

### DeletedAccounts_Get

```java
import com.azure.core.util.Context;

/** Samples for DeletedAccounts Get. */
public final class DeletedAccountsGetSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/DeletedAccountGet.json
     */
    /**
     * Sample code: DeletedAccountGet.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void deletedAccountGet(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.deletedAccounts().getWithResponse("sto1125", "eastus", Context.NONE);
    }
}
```

### DeletedAccounts_List

```java
import com.azure.core.util.Context;

/** Samples for DeletedAccounts List. */
public final class DeletedAccountsListSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/DeletedAccountList.json
     */
    /**
     * Sample code: DeletedAccountList.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void deletedAccountList(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.deletedAccounts().list(Context.NONE);
    }
}
```

### EncryptionScopes_Get

```java
import com.azure.core.util.Context;

/** Samples for EncryptionScopes Get. */
public final class EncryptionScopesGetSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountGetEncryptionScope.json
     */
    /**
     * Sample code: StorageAccountGetEncryptionScope.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountGetEncryptionScope(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .encryptionScopes()
            .getWithResponse("resource-group-name", "{storage-account-name}", "{encryption-scope-name}", Context.NONE);
    }
}
```

### EncryptionScopes_List

```java
import com.azure.core.util.Context;

/** Samples for EncryptionScopes List. */
public final class EncryptionScopesListSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountEncryptionScopeList.json
     */
    /**
     * Sample code: StorageAccountEncryptionScopeList.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountEncryptionScopeList(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.encryptionScopes().list("resource-group-name", "{storage-account-name}", Context.NONE);
    }
}
```

### EncryptionScopes_Patch

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.generated.models.EncryptionScope;
import com.azure.resourcemanager.storage.generated.models.EncryptionScopeKeyVaultProperties;
import com.azure.resourcemanager.storage.generated.models.EncryptionScopeSource;

/** Samples for EncryptionScopes Patch. */
public final class EncryptionScopesPatchSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountPatchEncryptionScope.json
     */
    /**
     * Sample code: StorageAccountPatchEncryptionScope.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountPatchEncryptionScope(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        EncryptionScope resource =
            manager
                .encryptionScopes()
                .getWithResponse(
                    "resource-group-name", "{storage-account-name}", "{encryption-scope-name}", Context.NONE)
                .getValue();
        resource
            .update()
            .withSource(EncryptionScopeSource.MICROSOFT_KEY_VAULT)
            .withKeyVaultProperties(
                new EncryptionScopeKeyVaultProperties()
                    .withKeyUri("https://testvault.vault.core.windows.net/keys/key1/863425f1358359c"))
            .apply();
    }
}
```

### EncryptionScopes_Put

```java
/** Samples for EncryptionScopes Put. */
public final class EncryptionScopesPutSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountPutEncryptionScopeWithInfrastructureEncryption.json
     */
    /**
     * Sample code: StorageAccountPutEncryptionScopeWithInfrastructureEncryption.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountPutEncryptionScopeWithInfrastructureEncryption(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .encryptionScopes()
            .define("{encryption-scope-name}")
            .withExistingStorageAccount("resource-group-name", "{storage-account-name}")
            .withRequireInfrastructureEncryption(true)
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountPutEncryptionScope.json
     */
    /**
     * Sample code: StorageAccountPutEncryptionScope.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountPutEncryptionScope(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .encryptionScopes()
            .define("{encryption-scope-name}")
            .withExistingStorageAccount("resource-group-name", "{storage-account-name}")
            .create();
    }
}
```

### FileServices_GetServiceProperties

```java
import com.azure.core.util.Context;

/** Samples for FileServices GetServiceProperties. */
public final class FileServicesGetServicePropertiesSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/FileServicesGet.json
     */
    /**
     * Sample code: GetFileServices.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void getFileServices(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.fileServices().getServicePropertiesWithResponse("res4410", "sto8607", Context.NONE);
    }
}
```

### FileServices_List

```java
import com.azure.core.util.Context;

/** Samples for FileServices List. */
public final class FileServicesListSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/FileServicesList.json
     */
    /**
     * Sample code: ListFileServices.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void listFileServices(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.fileServices().listWithResponse("res9290", "sto1590", Context.NONE);
    }
}
```

### FileServices_SetServiceProperties

```java
import com.azure.resourcemanager.storage.generated.models.CorsRule;
import com.azure.resourcemanager.storage.generated.models.CorsRuleAllowedMethodsItem;
import com.azure.resourcemanager.storage.generated.models.CorsRules;
import com.azure.resourcemanager.storage.generated.models.Multichannel;
import com.azure.resourcemanager.storage.generated.models.ProtocolSettings;
import com.azure.resourcemanager.storage.generated.models.SmbSetting;
import java.util.Arrays;

/** Samples for FileServices SetServiceProperties. */
public final class FileServicesSetServicePropertiesSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/FileServicesPut.json
     */
    /**
     * Sample code: PutFileServices.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void putFileServices(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .fileServices()
            .define()
            .withExistingStorageAccount("res4410", "sto8607")
            .withCors(
                new CorsRules()
                    .withCorsRules(
                        Arrays
                            .asList(
                                new CorsRule()
                                    .withAllowedOrigins(
                                        Arrays.asList("http://www.contoso.com", "http://www.fabrikam.com"))
                                    .withAllowedMethods(
                                        Arrays
                                            .asList(
                                                CorsRuleAllowedMethodsItem.GET,
                                                CorsRuleAllowedMethodsItem.HEAD,
                                                CorsRuleAllowedMethodsItem.POST,
                                                CorsRuleAllowedMethodsItem.OPTIONS,
                                                CorsRuleAllowedMethodsItem.MERGE,
                                                CorsRuleAllowedMethodsItem.PUT))
                                    .withMaxAgeInSeconds(100)
                                    .withExposedHeaders(Arrays.asList("x-ms-meta-*"))
                                    .withAllowedHeaders(
                                        Arrays.asList("x-ms-meta-abc", "x-ms-meta-data*", "x-ms-meta-target*")),
                                new CorsRule()
                                    .withAllowedOrigins(Arrays.asList("*"))
                                    .withAllowedMethods(Arrays.asList(CorsRuleAllowedMethodsItem.GET))
                                    .withMaxAgeInSeconds(2)
                                    .withExposedHeaders(Arrays.asList("*"))
                                    .withAllowedHeaders(Arrays.asList("*")),
                                new CorsRule()
                                    .withAllowedOrigins(
                                        Arrays.asList("http://www.abc23.com", "https://www.fabrikam.com/*"))
                                    .withAllowedMethods(
                                        Arrays.asList(CorsRuleAllowedMethodsItem.GET, CorsRuleAllowedMethodsItem.PUT))
                                    .withMaxAgeInSeconds(2000)
                                    .withExposedHeaders(
                                        Arrays.asList("x-ms-meta-abc", "x-ms-meta-data*", "x-ms-meta-target*"))
                                    .withAllowedHeaders(Arrays.asList("x-ms-meta-12345675754564*")))))
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/FileServicesPut_EnableSecureSmbFeatures.json
     */
    /**
     * Sample code: PutFileServices_EnableSecureSmbFeatures.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void putFileServicesEnableSecureSmbFeatures(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .fileServices()
            .define()
            .withExistingStorageAccount("res4410", "sto8607")
            .withProtocolSettings(
                new ProtocolSettings()
                    .withSmb(
                        new SmbSetting()
                            .withVersions("SMB2.1;SMB3.0;SMB3.1.1")
                            .withAuthenticationMethods("NTLMv2;Kerberos")
                            .withKerberosTicketEncryption("RC4-HMAC;AES-256")
                            .withChannelEncryption("AES-128-CCM;AES-128-GCM;AES-256-GCM")))
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/FileServicesPut_EnableSMBMultichannel.json
     */
    /**
     * Sample code: PutFileServices_EnableSMBMultichannel.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void putFileServicesEnableSMBMultichannel(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .fileServices()
            .define()
            .withExistingStorageAccount("res4410", "sto8607")
            .withProtocolSettings(
                new ProtocolSettings().withSmb(new SmbSetting().withMultichannel(new Multichannel().withEnabled(true))))
            .create();
    }
}
```

### FileShares_Create

```java
import com.azure.resourcemanager.storage.generated.models.EnabledProtocols;
import com.azure.resourcemanager.storage.generated.models.ShareAccessTier;

/** Samples for FileShares Create. */
public final class FileSharesCreateSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/FileSharesPut_AccessTier.json
     */
    /**
     * Sample code: PutShares with Access Tier.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void putSharesWithAccessTier(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .fileShares()
            .define("share1235")
            .withExistingStorageAccount("res346", "sto666")
            .withAccessTier(ShareAccessTier.HOT)
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/FileSharesPut_NFS.json
     */
    /**
     * Sample code: Create NFS Shares.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void createNFSShares(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .fileShares()
            .define("share1235")
            .withExistingStorageAccount("res346", "sto666")
            .withEnabledProtocols(EnabledProtocols.NFS)
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/FileSharesPut.json
     */
    /**
     * Sample code: PutShares.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void putShares(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.fileShares().define("share6185").withExistingStorageAccount("res3376", "sto328").create();
    }
}
```

### FileShares_Delete

```java
import com.azure.core.util.Context;

/** Samples for FileShares Delete. */
public final class FileSharesDeleteSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/FileSharesDelete.json
     */
    /**
     * Sample code: DeleteShares.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void deleteShares(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.fileShares().deleteWithResponse("res4079", "sto4506", "share9689", null, null, Context.NONE);
    }
}
```

### FileShares_Get

```java
import com.azure.core.util.Context;

/** Samples for FileShares Get. */
public final class FileSharesGetSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/FileSharesGet_Stats.json
     */
    /**
     * Sample code: GetShareStats.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void getShareStats(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.fileShares().getWithResponse("res9871", "sto6217", "share1634", "stats", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/FileSharesGet.json
     */
    /**
     * Sample code: GetShares.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void getShares(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.fileShares().getWithResponse("res9871", "sto6217", "share1634", null, null, Context.NONE);
    }
}
```

### FileShares_Lease

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.generated.models.LeaseShareAction;
import com.azure.resourcemanager.storage.generated.models.LeaseShareRequest;

/** Samples for FileShares Lease. */
public final class FileSharesLeaseSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/FileSharesLease_Break.json
     */
    /**
     * Sample code: Break a lease on a share.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void breakALeaseOnAShare(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .fileShares()
            .leaseWithResponse(
                "res3376",
                "sto328",
                "share12",
                null,
                new LeaseShareRequest()
                    .withAction(LeaseShareAction.BREAK)
                    .withLeaseId("8698f513-fa75-44a1-b8eb-30ba336af27d"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/FileSharesLease_Acquire.json
     */
    /**
     * Sample code: Acquire a lease on a share.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void acquireALeaseOnAShare(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .fileShares()
            .leaseWithResponse(
                "res3376",
                "sto328",
                "share124",
                null,
                new LeaseShareRequest().withAction(LeaseShareAction.ACQUIRE).withLeaseDuration(-1),
                Context.NONE);
    }
}
```

### FileShares_List

```java
import com.azure.core.util.Context;

/** Samples for FileShares List. */
public final class FileSharesListSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/FileSharesList.json
     */
    /**
     * Sample code: ListShares.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void listShares(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.fileShares().list("res9290", "sto1590", null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/DeletedFileSharesList.json
     */
    /**
     * Sample code: ListDeletedShares.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void listDeletedShares(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.fileShares().list("res9290", "sto1590", null, null, "deleted", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/FileShareSnapshotsList.json
     */
    /**
     * Sample code: ListShareSnapshots.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void listShareSnapshots(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.fileShares().list("res9290", "sto1590", null, null, "snapshots", Context.NONE);
    }
}
```

### FileShares_Restore

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.generated.models.DeletedShare;

/** Samples for FileShares Restore. */
public final class FileSharesRestoreSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/FileSharesRestore.json
     */
    /**
     * Sample code: RestoreShares.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void restoreShares(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .fileShares()
            .restoreWithResponse(
                "res3376",
                "sto328",
                "share1249",
                new DeletedShare().withDeletedShareName("share1249").withDeletedShareVersion("1234567890"),
                Context.NONE);
    }
}
```

### FileShares_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.generated.models.AccessPolicy;
import com.azure.resourcemanager.storage.generated.models.FileShare;
import com.azure.resourcemanager.storage.generated.models.SignedIdentifier;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for FileShares Update. */
public final class FileSharesUpdateSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/FileSharesPatch.json
     */
    /**
     * Sample code: UpdateShares.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void updateShares(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        FileShare resource =
            manager.fileShares().getWithResponse("res3376", "sto328", "share6185", null, null, Context.NONE).getValue();
        resource.update().withMetadata(mapOf("type", "image")).apply();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/FileShareAclsPatch.json
     */
    /**
     * Sample code: UpdateShareAcls.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void updateShareAcls(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        FileShare resource =
            manager.fileShares().getWithResponse("res3376", "sto328", "share6185", null, null, Context.NONE).getValue();
        resource
            .update()
            .withSignedIdentifiers(
                Arrays
                    .asList(
                        new SignedIdentifier()
                            .withId("MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI")
                            .withAccessPolicy(
                                new AccessPolicy()
                                    .withStartTime(OffsetDateTime.parse("2021-04-01T08:49:37.0000000Z"))
                                    .withExpiryTime(OffsetDateTime.parse("2021-05-01T08:49:37.0000000Z"))
                                    .withPermission("rwd"))))
            .apply();
    }

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

### LocalUsersOperation_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.generated.models.LocalUser;
import com.azure.resourcemanager.storage.generated.models.PermissionScope;
import com.azure.resourcemanager.storage.generated.models.SshPublicKey;
import java.util.Arrays;

/** Samples for LocalUsersOperation CreateOrUpdate. */
public final class LocalUsersOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/LocalUserCreate.json
     */
    /**
     * Sample code: CreateLocalUser.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void createLocalUser(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .localUsersOperations()
            .define("user1")
            .withExistingStorageAccount("res6977", "sto2527")
            .withPermissionScopes(
                Arrays
                    .asList(
                        new PermissionScope().withPermissions("rwd").withService("file").withResourceName("share1"),
                        new PermissionScope().withPermissions("rw").withService("file").withResourceName("share2")))
            .withHomeDirectory("homedirectory")
            .withSshAuthorizedKeys(
                Arrays.asList(new SshPublicKey().withDescription("key name").withKey("ssh-rsa keykeykeykeykey=")))
            .withHasSshPassword(true)
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/LocalUserUpdate.json
     */
    /**
     * Sample code: UpdateLocalUser.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void updateLocalUser(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        LocalUser resource =
            manager.localUsersOperations().getWithResponse("res6977", "sto2527", "user1", Context.NONE).getValue();
        resource
            .update()
            .withHomeDirectory("homedirectory2")
            .withHasSharedKey(false)
            .withHasSshKey(false)
            .withHasSshPassword(false)
            .apply();
    }
}
```

### LocalUsersOperation_Delete

```java
import com.azure.core.util.Context;

/** Samples for LocalUsersOperation Delete. */
public final class LocalUsersOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/LocalUserDelete.json
     */
    /**
     * Sample code: DeleteLocalUser.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void deleteLocalUser(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.localUsersOperations().deleteWithResponse("res6977", "sto2527", "user1", Context.NONE);
    }
}
```

### LocalUsersOperation_Get

```java
import com.azure.core.util.Context;

/** Samples for LocalUsersOperation Get. */
public final class LocalUsersOperationGetSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/LocalUserGet.json
     */
    /**
     * Sample code: GetLocalUser.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void getLocalUser(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.localUsersOperations().getWithResponse("res6977", "sto2527", "user1", Context.NONE);
    }
}
```

### LocalUsersOperation_List

```java
import com.azure.core.util.Context;

/** Samples for LocalUsersOperation List. */
public final class LocalUsersOperationListSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/LocalUsersList.json
     */
    /**
     * Sample code: ListLocalUsers.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void listLocalUsers(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.localUsersOperations().list("res6977", "sto2527", Context.NONE);
    }
}
```

### LocalUsersOperation_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for LocalUsersOperation ListKeys. */
public final class LocalUsersOperationListKeysSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/LocalUserListKeys.json
     */
    /**
     * Sample code: ListLocalUserKeys.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void listLocalUserKeys(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.localUsersOperations().listKeysWithResponse("res6977", "sto2527", "user1", Context.NONE);
    }
}
```

### LocalUsersOperation_RegeneratePassword

```java
import com.azure.core.util.Context;

/** Samples for LocalUsersOperation RegeneratePassword. */
public final class LocalUsersOperationRegeneratePasswordSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/LocalUserRegeneratePassword.json
     */
    /**
     * Sample code: RegenerateLocalUserPassword.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void regenerateLocalUserPassword(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.localUsersOperations().regeneratePasswordWithResponse("res6977", "sto2527", "user1", Context.NONE);
    }
}
```

### ManagementPolicies_CreateOrUpdate

```java
import com.azure.resourcemanager.storage.generated.models.DateAfterCreation;
import com.azure.resourcemanager.storage.generated.models.DateAfterModification;
import com.azure.resourcemanager.storage.generated.models.ManagementPolicyAction;
import com.azure.resourcemanager.storage.generated.models.ManagementPolicyBaseBlob;
import com.azure.resourcemanager.storage.generated.models.ManagementPolicyDefinition;
import com.azure.resourcemanager.storage.generated.models.ManagementPolicyFilter;
import com.azure.resourcemanager.storage.generated.models.ManagementPolicyName;
import com.azure.resourcemanager.storage.generated.models.ManagementPolicyRule;
import com.azure.resourcemanager.storage.generated.models.ManagementPolicySchema;
import com.azure.resourcemanager.storage.generated.models.ManagementPolicySnapShot;
import com.azure.resourcemanager.storage.generated.models.ManagementPolicyVersion;
import com.azure.resourcemanager.storage.generated.models.RuleType;
import com.azure.resourcemanager.storage.generated.models.TagFilter;
import java.util.Arrays;

/** Samples for ManagementPolicies CreateOrUpdate. */
public final class ManagementPoliciesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountSetManagementPolicyWithSnapshotAndVersion.json
     */
    /**
     * Sample code: StorageAccountSetManagementPolicyWithSnapshotAndVersion.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountSetManagementPolicyWithSnapshotAndVersion(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .managementPolicies()
            .define(ManagementPolicyName.DEFAULT)
            .withExistingStorageAccount("res7687", "sto9699")
            .withPolicy(
                new ManagementPolicySchema()
                    .withRules(
                        Arrays
                            .asList(
                                new ManagementPolicyRule()
                                    .withEnabled(true)
                                    .withName("olcmtest1")
                                    .withType(RuleType.LIFECYCLE)
                                    .withDefinition(
                                        new ManagementPolicyDefinition()
                                            .withActions(
                                                new ManagementPolicyAction()
                                                    .withBaseBlob(
                                                        new ManagementPolicyBaseBlob()
                                                            .withTierToCool(
                                                                new DateAfterModification()
                                                                    .withDaysAfterModificationGreaterThan(30.0f))
                                                            .withTierToArchive(
                                                                new DateAfterModification()
                                                                    .withDaysAfterModificationGreaterThan(90.0f))
                                                            .withDelete(
                                                                new DateAfterModification()
                                                                    .withDaysAfterModificationGreaterThan(1000.0f)))
                                                    .withSnapshot(
                                                        new ManagementPolicySnapShot()
                                                            .withTierToCool(
                                                                new DateAfterCreation()
                                                                    .withDaysAfterCreationGreaterThan(30f))
                                                            .withTierToArchive(
                                                                new DateAfterCreation()
                                                                    .withDaysAfterCreationGreaterThan(90f))
                                                            .withDelete(
                                                                new DateAfterCreation()
                                                                    .withDaysAfterCreationGreaterThan(1000f)))
                                                    .withVersion(
                                                        new ManagementPolicyVersion()
                                                            .withTierToCool(
                                                                new DateAfterCreation()
                                                                    .withDaysAfterCreationGreaterThan(30f))
                                                            .withTierToArchive(
                                                                new DateAfterCreation()
                                                                    .withDaysAfterCreationGreaterThan(90f))
                                                            .withDelete(
                                                                new DateAfterCreation()
                                                                    .withDaysAfterCreationGreaterThan(1000f))))
                                            .withFilters(
                                                new ManagementPolicyFilter()
                                                    .withPrefixMatch(Arrays.asList("olcmtestcontainer1"))
                                                    .withBlobTypes(Arrays.asList("blockBlob")))))))
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountSetManagementPolicyForBlockAndAppendBlobs.json
     */
    /**
     * Sample code: StorageAccountSetManagementPolicyForBlockAndAppendBlobs.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountSetManagementPolicyForBlockAndAppendBlobs(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .managementPolicies()
            .define(ManagementPolicyName.DEFAULT)
            .withExistingStorageAccount("res7687", "sto9699")
            .withPolicy(
                new ManagementPolicySchema()
                    .withRules(
                        Arrays
                            .asList(
                                new ManagementPolicyRule()
                                    .withEnabled(true)
                                    .withName("olcmtest1")
                                    .withType(RuleType.LIFECYCLE)
                                    .withDefinition(
                                        new ManagementPolicyDefinition()
                                            .withActions(
                                                new ManagementPolicyAction()
                                                    .withBaseBlob(
                                                        new ManagementPolicyBaseBlob()
                                                            .withDelete(
                                                                new DateAfterModification()
                                                                    .withDaysAfterModificationGreaterThan(90.0f)))
                                                    .withSnapshot(
                                                        new ManagementPolicySnapShot()
                                                            .withDelete(
                                                                new DateAfterCreation()
                                                                    .withDaysAfterCreationGreaterThan(90f)))
                                                    .withVersion(
                                                        new ManagementPolicyVersion()
                                                            .withDelete(
                                                                new DateAfterCreation()
                                                                    .withDaysAfterCreationGreaterThan(90f))))
                                            .withFilters(
                                                new ManagementPolicyFilter()
                                                    .withPrefixMatch(Arrays.asList("olcmtestcontainer1"))
                                                    .withBlobTypes(Arrays.asList("blockBlob", "appendBlob")))))))
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountSetManagementPolicy_LastAccessTimeBasedBlobActions.json
     */
    /**
     * Sample code: StorageAccountSetManagementPolicy_LastAccessTimeBasedBlobActions.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountSetManagementPolicyLastAccessTimeBasedBlobActions(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .managementPolicies()
            .define(ManagementPolicyName.DEFAULT)
            .withExistingStorageAccount("res7687", "sto9699")
            .withPolicy(
                new ManagementPolicySchema()
                    .withRules(
                        Arrays
                            .asList(
                                new ManagementPolicyRule()
                                    .withEnabled(true)
                                    .withName("olcmtest")
                                    .withType(RuleType.LIFECYCLE)
                                    .withDefinition(
                                        new ManagementPolicyDefinition()
                                            .withActions(
                                                new ManagementPolicyAction()
                                                    .withBaseBlob(
                                                        new ManagementPolicyBaseBlob()
                                                            .withTierToCool(
                                                                new DateAfterModification()
                                                                    .withDaysAfterLastAccessTimeGreaterThan(30.0f))
                                                            .withTierToArchive(
                                                                new DateAfterModification()
                                                                    .withDaysAfterLastAccessTimeGreaterThan(90.0f))
                                                            .withDelete(
                                                                new DateAfterModification()
                                                                    .withDaysAfterLastAccessTimeGreaterThan(1000.0f))
                                                            .withEnableAutoTierToHotFromCool(true))
                                                    .withSnapshot(
                                                        new ManagementPolicySnapShot()
                                                            .withDelete(
                                                                new DateAfterCreation()
                                                                    .withDaysAfterCreationGreaterThan(30f))))
                                            .withFilters(
                                                new ManagementPolicyFilter()
                                                    .withPrefixMatch(Arrays.asList("olcmtestcontainer"))
                                                    .withBlobTypes(Arrays.asList("blockBlob")))))))
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountSetManagementPolicy.json
     */
    /**
     * Sample code: StorageAccountSetManagementPolicies.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountSetManagementPolicies(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .managementPolicies()
            .define(ManagementPolicyName.DEFAULT)
            .withExistingStorageAccount("res7687", "sto9699")
            .withPolicy(
                new ManagementPolicySchema()
                    .withRules(
                        Arrays
                            .asList(
                                new ManagementPolicyRule()
                                    .withEnabled(true)
                                    .withName("olcmtest1")
                                    .withType(RuleType.LIFECYCLE)
                                    .withDefinition(
                                        new ManagementPolicyDefinition()
                                            .withActions(
                                                new ManagementPolicyAction()
                                                    .withBaseBlob(
                                                        new ManagementPolicyBaseBlob()
                                                            .withTierToCool(
                                                                new DateAfterModification()
                                                                    .withDaysAfterModificationGreaterThan(30.0f))
                                                            .withTierToArchive(
                                                                new DateAfterModification()
                                                                    .withDaysAfterModificationGreaterThan(90.0f))
                                                            .withDelete(
                                                                new DateAfterModification()
                                                                    .withDaysAfterModificationGreaterThan(1000.0f)))
                                                    .withSnapshot(
                                                        new ManagementPolicySnapShot()
                                                            .withDelete(
                                                                new DateAfterCreation()
                                                                    .withDaysAfterCreationGreaterThan(30f))))
                                            .withFilters(
                                                new ManagementPolicyFilter()
                                                    .withPrefixMatch(Arrays.asList("olcmtestcontainer1"))
                                                    .withBlobTypes(Arrays.asList("blockBlob")))),
                                new ManagementPolicyRule()
                                    .withEnabled(true)
                                    .withName("olcmtest2")
                                    .withType(RuleType.LIFECYCLE)
                                    .withDefinition(
                                        new ManagementPolicyDefinition()
                                            .withActions(
                                                new ManagementPolicyAction()
                                                    .withBaseBlob(
                                                        new ManagementPolicyBaseBlob()
                                                            .withTierToCool(
                                                                new DateAfterModification()
                                                                    .withDaysAfterModificationGreaterThan(30.0f))
                                                            .withTierToArchive(
                                                                new DateAfterModification()
                                                                    .withDaysAfterModificationGreaterThan(90.0f))
                                                            .withDelete(
                                                                new DateAfterModification()
                                                                    .withDaysAfterModificationGreaterThan(1000.0f))))
                                            .withFilters(
                                                new ManagementPolicyFilter()
                                                    .withPrefixMatch(Arrays.asList("olcmtestcontainer2"))
                                                    .withBlobTypes(Arrays.asList("blockBlob"))
                                                    .withBlobIndexMatch(
                                                        Arrays
                                                            .asList(
                                                                new TagFilter()
                                                                    .withName("tag1")
                                                                    .withOp("==")
                                                                    .withValue("val1"),
                                                                new TagFilter()
                                                                    .withName("tag2")
                                                                    .withOp("==")
                                                                    .withValue("val2"))))))))
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountSetManagementPolicy_BaseBlobDaysAfterCreationActions.json
     */
    /**
     * Sample code: StorageAccountSetManagementPolicy_BaseBlobDaysAfterCreationActions.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountSetManagementPolicyBaseBlobDaysAfterCreationActions(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .managementPolicies()
            .define(ManagementPolicyName.DEFAULT)
            .withExistingStorageAccount("res7687", "sto9699")
            .withPolicy(
                new ManagementPolicySchema()
                    .withRules(
                        Arrays
                            .asList(
                                new ManagementPolicyRule()
                                    .withEnabled(true)
                                    .withName("olcmtest1")
                                    .withType(RuleType.LIFECYCLE)
                                    .withDefinition(
                                        new ManagementPolicyDefinition()
                                            .withActions(
                                                new ManagementPolicyAction()
                                                    .withBaseBlob(
                                                        new ManagementPolicyBaseBlob()
                                                            .withTierToCool(
                                                                new DateAfterModification()
                                                                    .withDaysAfterCreationGreaterThan(30.0f))
                                                            .withTierToArchive(
                                                                new DateAfterModification()
                                                                    .withDaysAfterCreationGreaterThan(90.0f))
                                                            .withDelete(
                                                                new DateAfterModification()
                                                                    .withDaysAfterCreationGreaterThan(1000.0f))))
                                            .withFilters(
                                                new ManagementPolicyFilter()
                                                    .withPrefixMatch(Arrays.asList("olcmtestcontainer1"))
                                                    .withBlobTypes(Arrays.asList("blockBlob")))))))
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountSetManagementPolicy_LastTierChangeTimeActions.json
     */
    /**
     * Sample code: StorageAccountSetManagementPolicy_LastTierChangeTimeActions.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountSetManagementPolicyLastTierChangeTimeActions(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .managementPolicies()
            .define(ManagementPolicyName.DEFAULT)
            .withExistingStorageAccount("res7687", "sto9699")
            .withPolicy(
                new ManagementPolicySchema()
                    .withRules(
                        Arrays
                            .asList(
                                new ManagementPolicyRule()
                                    .withEnabled(true)
                                    .withName("olcmtest")
                                    .withType(RuleType.LIFECYCLE)
                                    .withDefinition(
                                        new ManagementPolicyDefinition()
                                            .withActions(
                                                new ManagementPolicyAction()
                                                    .withBaseBlob(
                                                        new ManagementPolicyBaseBlob()
                                                            .withTierToCool(
                                                                new DateAfterModification()
                                                                    .withDaysAfterModificationGreaterThan(30.0f))
                                                            .withTierToArchive(
                                                                new DateAfterModification()
                                                                    .withDaysAfterModificationGreaterThan(90.0f)
                                                                    .withDaysAfterLastTierChangeGreaterThan(120.0f))
                                                            .withDelete(
                                                                new DateAfterModification()
                                                                    .withDaysAfterModificationGreaterThan(1000.0f)))
                                                    .withSnapshot(
                                                        new ManagementPolicySnapShot()
                                                            .withTierToArchive(
                                                                new DateAfterCreation()
                                                                    .withDaysAfterCreationGreaterThan(30f)
                                                                    .withDaysAfterLastTierChangeGreaterThan(90.0f)))
                                                    .withVersion(
                                                        new ManagementPolicyVersion()
                                                            .withTierToArchive(
                                                                new DateAfterCreation()
                                                                    .withDaysAfterCreationGreaterThan(30f)
                                                                    .withDaysAfterLastTierChangeGreaterThan(90.0f))))
                                            .withFilters(
                                                new ManagementPolicyFilter()
                                                    .withPrefixMatch(Arrays.asList("olcmtestcontainer"))
                                                    .withBlobTypes(Arrays.asList("blockBlob")))))))
            .create();
    }
}
```

### ManagementPolicies_Delete

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.generated.models.ManagementPolicyName;

/** Samples for ManagementPolicies Delete. */
public final class ManagementPoliciesDeleteSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountDeleteManagementPolicy.json
     */
    /**
     * Sample code: StorageAccountDeleteManagementPolicies.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountDeleteManagementPolicies(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .managementPolicies()
            .deleteWithResponse("res6977", "sto2527", ManagementPolicyName.DEFAULT, Context.NONE);
    }
}
```

### ManagementPolicies_Get

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.generated.models.ManagementPolicyName;

/** Samples for ManagementPolicies Get. */
public final class ManagementPoliciesGetSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountGetManagementPolicy.json
     */
    /**
     * Sample code: StorageAccountGetManagementPolicies.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountGetManagementPolicies(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.managementPolicies().getWithResponse("res6977", "sto2527", ManagementPolicyName.DEFAULT, Context.NONE);
    }
}
```

### ObjectReplicationPoliciesOperation_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.generated.models.ObjectReplicationPolicy;
import com.azure.resourcemanager.storage.generated.models.ObjectReplicationPolicyFilter;
import com.azure.resourcemanager.storage.generated.models.ObjectReplicationPolicyRule;
import java.util.Arrays;

/** Samples for ObjectReplicationPoliciesOperation CreateOrUpdate. */
public final class ObjectReplicationPoliciesOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountCreateObjectReplicationPolicyOnSource.json
     */
    /**
     * Sample code: StorageAccountCreateObjectReplicationPolicyOnSource.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountCreateObjectReplicationPolicyOnSource(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .objectReplicationPoliciesOperations()
            .define("2a20bb73-5717-4635-985a-5d4cf777438f")
            .withExistingStorageAccount("res7687", "src1122")
            .withSourceAccount("src1122")
            .withDestinationAccount("dst112")
            .withRules(
                Arrays
                    .asList(
                        new ObjectReplicationPolicyRule()
                            .withRuleId("d5d18a48-8801-4554-aeaa-74faf65f5ef9")
                            .withSourceContainer("scont139")
                            .withDestinationContainer("dcont139")
                            .withFilters(
                                new ObjectReplicationPolicyFilter()
                                    .withPrefixMatch(Arrays.asList("blobA", "blobB"))
                                    .withMinCreationTime("2020-02-19T16:05:00Z"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountCreateObjectReplicationPolicyOnDestination.json
     */
    /**
     * Sample code: StorageAccountCreateObjectReplicationPolicyOnDestination.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountCreateObjectReplicationPolicyOnDestination(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .objectReplicationPoliciesOperations()
            .define("default")
            .withExistingStorageAccount("res7687", "dst112")
            .withSourceAccount("src1122")
            .withDestinationAccount("dst112")
            .withRules(
                Arrays
                    .asList(
                        new ObjectReplicationPolicyRule()
                            .withSourceContainer("scont139")
                            .withDestinationContainer("dcont139")
                            .withFilters(
                                new ObjectReplicationPolicyFilter().withPrefixMatch(Arrays.asList("blobA", "blobB")))))
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountUpdateObjectReplicationPolicyOnDestination.json
     */
    /**
     * Sample code: StorageAccountUpdateObjectReplicationPolicyOnDestination.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountUpdateObjectReplicationPolicyOnDestination(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        ObjectReplicationPolicy resource =
            manager
                .objectReplicationPoliciesOperations()
                .getWithResponse("res7687", "dst112", "2a20bb73-5717-4635-985a-5d4cf777438f", Context.NONE)
                .getValue();
        resource
            .update()
            .withSourceAccount("src1122")
            .withDestinationAccount("dst112")
            .withRules(
                Arrays
                    .asList(
                        new ObjectReplicationPolicyRule()
                            .withRuleId("d5d18a48-8801-4554-aeaa-74faf65f5ef9")
                            .withSourceContainer("scont139")
                            .withDestinationContainer("dcont139")
                            .withFilters(
                                new ObjectReplicationPolicyFilter().withPrefixMatch(Arrays.asList("blobA", "blobB"))),
                        new ObjectReplicationPolicyRule()
                            .withSourceContainer("scont179")
                            .withDestinationContainer("dcont179")))
            .apply();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountUpdateObjectReplicationPolicyOnSource.json
     */
    /**
     * Sample code: StorageAccountUpdateObjectReplicationPolicyOnSource.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountUpdateObjectReplicationPolicyOnSource(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        ObjectReplicationPolicy resource =
            manager
                .objectReplicationPoliciesOperations()
                .getWithResponse("res7687", "src1122", "2a20bb73-5717-4635-985a-5d4cf777438f", Context.NONE)
                .getValue();
        resource
            .update()
            .withSourceAccount("src1122")
            .withDestinationAccount("dst112")
            .withRules(
                Arrays
                    .asList(
                        new ObjectReplicationPolicyRule()
                            .withRuleId("d5d18a48-8801-4554-aeaa-74faf65f5ef9")
                            .withSourceContainer("scont139")
                            .withDestinationContainer("dcont139")
                            .withFilters(
                                new ObjectReplicationPolicyFilter().withPrefixMatch(Arrays.asList("blobA", "blobB"))),
                        new ObjectReplicationPolicyRule()
                            .withRuleId("cfbb4bc2-8b60-429f-b05a-d1e0942b33b2")
                            .withSourceContainer("scont179")
                            .withDestinationContainer("dcont179")))
            .apply();
    }
}
```

### ObjectReplicationPoliciesOperation_Delete

```java
import com.azure.core.util.Context;

/** Samples for ObjectReplicationPoliciesOperation Delete. */
public final class ObjectReplicationPoliciesOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountDeleteObjectReplicationPolicy.json
     */
    /**
     * Sample code: StorageAccountDeleteObjectReplicationPolicies.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountDeleteObjectReplicationPolicies(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .objectReplicationPoliciesOperations()
            .deleteWithResponse("res6977", "sto2527", "{objectReplicationPolicy-Id}", Context.NONE);
    }
}
```

### ObjectReplicationPoliciesOperation_Get

```java
import com.azure.core.util.Context;

/** Samples for ObjectReplicationPoliciesOperation Get. */
public final class ObjectReplicationPoliciesOperationGetSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountGetObjectReplicationPolicy.json
     */
    /**
     * Sample code: StorageAccountGetObjectReplicationPolicies.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountGetObjectReplicationPolicies(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .objectReplicationPoliciesOperations()
            .getWithResponse("res6977", "sto2527", "{objectReplicationPolicy-Id}", Context.NONE);
    }
}
```

### ObjectReplicationPoliciesOperation_List

```java
import com.azure.core.util.Context;

/** Samples for ObjectReplicationPoliciesOperation List. */
public final class ObjectReplicationPoliciesOperationListSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountListObjectReplicationPolicies.json
     */
    /**
     * Sample code: StorageAccountListObjectReplicationPolicies.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountListObjectReplicationPolicies(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.objectReplicationPoliciesOperations().list("res6977", "sto2527", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/OperationsList.json
     */
    /**
     * Sample code: OperationsList.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void operationsList(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### PrivateEndpointConnections_Delete

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Delete. */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountDeletePrivateEndpointConnection.json
     */
    /**
     * Sample code: StorageAccountDeletePrivateEndpointConnection.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountDeletePrivateEndpointConnection(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .privateEndpointConnections()
            .deleteWithResponse("res6977", "sto2527", "{privateEndpointConnectionName}", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountGetPrivateEndpointConnection.json
     */
    /**
     * Sample code: StorageAccountGetPrivateEndpointConnection.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountGetPrivateEndpointConnection(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse("res6977", "sto2527", "{privateEndpointConnectionName}", Context.NONE);
    }
}
```

### PrivateEndpointConnections_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections List. */
public final class PrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountListPrivateEndpointConnections.json
     */
    /**
     * Sample code: StorageAccountListPrivateEndpointConnections.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountListPrivateEndpointConnections(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.privateEndpointConnections().list("res6977", "sto2527", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Put

```java
import com.azure.resourcemanager.storage.generated.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.storage.generated.models.PrivateLinkServiceConnectionState;

/** Samples for PrivateEndpointConnections Put. */
public final class PrivateEndpointConnectionsPutSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountPutPrivateEndpointConnection.json
     */
    /**
     * Sample code: StorageAccountPutPrivateEndpointConnection.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountPutPrivateEndpointConnection(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .privateEndpointConnections()
            .define("{privateEndpointConnectionName}")
            .withExistingStorageAccount("res7687", "sto9699")
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState()
                    .withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("Auto-Approved"))
            .create();
    }
}
```

### PrivateLinkResources_ListByStorageAccount

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources ListByStorageAccount. */
public final class PrivateLinkResourcesListByStorageAccountSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountListPrivateLinkResources.json
     */
    /**
     * Sample code: StorageAccountListPrivateLinkResources.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountListPrivateLinkResources(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.privateLinkResources().listByStorageAccountWithResponse("res6977", "sto2527", Context.NONE);
    }
}
```

### Queue_Create

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for Queue Create. */
public final class QueueCreateSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/QueueOperationPut.json
     */
    /**
     * Sample code: QueueOperationPut.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void queueOperationPut(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.queues().define("queue6185").withExistingStorageAccount("res3376", "sto328").create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/QueueOperationPutWithMetadata.json
     */
    /**
     * Sample code: QueueOperationPutWithMetadata.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void queueOperationPutWithMetadata(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .queues()
            .define("queue6185")
            .withExistingStorageAccount("res3376", "sto328")
            .withMetadata(mapOf("sample1", "meta1", "sample2", "meta2"))
            .create();
    }

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

### Queue_Delete

```java
import com.azure.core.util.Context;

/** Samples for Queue Delete. */
public final class QueueDeleteSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/QueueOperationDelete.json
     */
    /**
     * Sample code: QueueOperationDelete.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void queueOperationDelete(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.queues().deleteWithResponse("res3376", "sto328", "queue6185", Context.NONE);
    }
}
```

### Queue_Get

```java
import com.azure.core.util.Context;

/** Samples for Queue Get. */
public final class QueueGetSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/QueueOperationGet.json
     */
    /**
     * Sample code: QueueOperationGet.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void queueOperationGet(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.queues().getWithResponse("res3376", "sto328", "queue6185", Context.NONE);
    }
}
```

### Queue_List

```java
import com.azure.core.util.Context;

/** Samples for Queue List. */
public final class QueueListSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/QueueOperationList.json
     */
    /**
     * Sample code: QueueOperationList.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void queueOperationList(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.queues().list("res9290", "sto328", null, null, Context.NONE);
    }
}
```

### Queue_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.generated.models.StorageQueue;

/** Samples for Queue Update. */
public final class QueueUpdateSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/QueueOperationPatch.json
     */
    /**
     * Sample code: QueueOperationPatch.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void queueOperationPatch(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        StorageQueue resource =
            manager.queues().getWithResponse("res3376", "sto328", "queue6185", Context.NONE).getValue();
        resource.update().apply();
    }
}
```

### QueueServices_GetServiceProperties

```java
import com.azure.core.util.Context;

/** Samples for QueueServices GetServiceProperties. */
public final class QueueServicesGetServicePropertiesSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/QueueServicesGet.json
     */
    /**
     * Sample code: QueueServicesGet.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void queueServicesGet(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.queueServices().getServicePropertiesWithResponse("res4410", "sto8607", Context.NONE);
    }
}
```

### QueueServices_List

```java
import com.azure.core.util.Context;

/** Samples for QueueServices List. */
public final class QueueServicesListSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/QueueServicesList.json
     */
    /**
     * Sample code: QueueServicesList.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void queueServicesList(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.queueServices().listWithResponse("res9290", "sto1590", Context.NONE);
    }
}
```

### QueueServices_SetServiceProperties

```java
import com.azure.resourcemanager.storage.generated.models.CorsRule;
import com.azure.resourcemanager.storage.generated.models.CorsRuleAllowedMethodsItem;
import com.azure.resourcemanager.storage.generated.models.CorsRules;
import java.util.Arrays;

/** Samples for QueueServices SetServiceProperties. */
public final class QueueServicesSetServicePropertiesSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/QueueServicesPut.json
     */
    /**
     * Sample code: QueueServicesPut.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void queueServicesPut(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .queueServices()
            .define()
            .withExistingStorageAccount("res4410", "sto8607")
            .withCors(
                new CorsRules()
                    .withCorsRules(
                        Arrays
                            .asList(
                                new CorsRule()
                                    .withAllowedOrigins(
                                        Arrays.asList("http://www.contoso.com", "http://www.fabrikam.com"))
                                    .withAllowedMethods(
                                        Arrays
                                            .asList(
                                                CorsRuleAllowedMethodsItem.GET,
                                                CorsRuleAllowedMethodsItem.HEAD,
                                                CorsRuleAllowedMethodsItem.POST,
                                                CorsRuleAllowedMethodsItem.OPTIONS,
                                                CorsRuleAllowedMethodsItem.MERGE,
                                                CorsRuleAllowedMethodsItem.PUT))
                                    .withMaxAgeInSeconds(100)
                                    .withExposedHeaders(Arrays.asList("x-ms-meta-*"))
                                    .withAllowedHeaders(
                                        Arrays.asList("x-ms-meta-abc", "x-ms-meta-data*", "x-ms-meta-target*")),
                                new CorsRule()
                                    .withAllowedOrigins(Arrays.asList("*"))
                                    .withAllowedMethods(Arrays.asList(CorsRuleAllowedMethodsItem.GET))
                                    .withMaxAgeInSeconds(2)
                                    .withExposedHeaders(Arrays.asList("*"))
                                    .withAllowedHeaders(Arrays.asList("*")),
                                new CorsRule()
                                    .withAllowedOrigins(
                                        Arrays.asList("http://www.abc23.com", "https://www.fabrikam.com/*"))
                                    .withAllowedMethods(
                                        Arrays.asList(CorsRuleAllowedMethodsItem.GET, CorsRuleAllowedMethodsItem.PUT))
                                    .withMaxAgeInSeconds(2000)
                                    .withExposedHeaders(
                                        Arrays.asList("x-ms-meta-abc", "x-ms-meta-data*", "x-ms-meta-target*"))
                                    .withAllowedHeaders(Arrays.asList("x-ms-meta-12345675754564*")))))
            .create();
    }
}
```

### Skus_List

```java
import com.azure.core.util.Context;

/** Samples for Skus List. */
public final class SkusListSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/SKUList.json
     */
    /**
     * Sample code: SkuList.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void skuList(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.skus().list(Context.NONE);
    }
}
```

### StorageAccounts_AbortHierarchicalNamespaceMigration

```java
import com.azure.core.util.Context;

/** Samples for StorageAccounts AbortHierarchicalNamespaceMigration. */
public final class StorageAccountsAbortHierarchicalNamespaceMigrationSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountAbortHierarchicalNamespaceMigration.json
     */
    /**
     * Sample code: StorageAccountAbortHierarchicalNamespaceMigration.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountAbortHierarchicalNamespaceMigration(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.storageAccounts().abortHierarchicalNamespaceMigration("res4228", "sto2434", Context.NONE);
    }
}
```

### StorageAccounts_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.generated.models.StorageAccountCheckNameAvailabilityParameters;

/** Samples for StorageAccounts CheckNameAvailability. */
public final class StorageAccountsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountCheckNameAvailability.json
     */
    /**
     * Sample code: StorageAccountCheckNameAvailability.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountCheckNameAvailability(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .storageAccounts()
            .checkNameAvailabilityWithResponse(
                new StorageAccountCheckNameAvailabilityParameters().withName("sto3363"), Context.NONE);
    }
}
```

### StorageAccounts_Create

```java
import com.azure.resourcemanager.storage.generated.models.AccountImmutabilityPolicyProperties;
import com.azure.resourcemanager.storage.generated.models.AccountImmutabilityPolicyState;
import com.azure.resourcemanager.storage.generated.models.AllowedCopyScope;
import com.azure.resourcemanager.storage.generated.models.Bypass;
import com.azure.resourcemanager.storage.generated.models.DefaultAction;
import com.azure.resourcemanager.storage.generated.models.DnsEndpointType;
import com.azure.resourcemanager.storage.generated.models.Encryption;
import com.azure.resourcemanager.storage.generated.models.EncryptionIdentity;
import com.azure.resourcemanager.storage.generated.models.EncryptionService;
import com.azure.resourcemanager.storage.generated.models.EncryptionServices;
import com.azure.resourcemanager.storage.generated.models.ExpirationAction;
import com.azure.resourcemanager.storage.generated.models.ExtendedLocation;
import com.azure.resourcemanager.storage.generated.models.ExtendedLocationTypes;
import com.azure.resourcemanager.storage.generated.models.Identity;
import com.azure.resourcemanager.storage.generated.models.IdentityType;
import com.azure.resourcemanager.storage.generated.models.ImmutableStorageAccount;
import com.azure.resourcemanager.storage.generated.models.KeyPolicy;
import com.azure.resourcemanager.storage.generated.models.KeySource;
import com.azure.resourcemanager.storage.generated.models.KeyType;
import com.azure.resourcemanager.storage.generated.models.KeyVaultProperties;
import com.azure.resourcemanager.storage.generated.models.Kind;
import com.azure.resourcemanager.storage.generated.models.MinimumTlsVersion;
import com.azure.resourcemanager.storage.generated.models.NetworkRuleSet;
import com.azure.resourcemanager.storage.generated.models.PublicNetworkAccess;
import com.azure.resourcemanager.storage.generated.models.RoutingChoice;
import com.azure.resourcemanager.storage.generated.models.RoutingPreference;
import com.azure.resourcemanager.storage.generated.models.SasPolicy;
import com.azure.resourcemanager.storage.generated.models.Sku;
import com.azure.resourcemanager.storage.generated.models.SkuName;
import com.azure.resourcemanager.storage.generated.models.UserAssignedIdentity;
import com.azure.resourcemanager.storage.generated.models.VirtualNetworkRule;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for StorageAccounts Create. */
public final class StorageAccountsCreateSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/NfsV3AccountCreate.json
     */
    /**
     * Sample code: NfsV3AccountCreate.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void nfsV3AccountCreate(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .storageAccounts()
            .define("sto4445")
            .withRegion("eastus")
            .withExistingResourceGroup("res9101")
            .withSku(new Sku().withName(SkuName.PREMIUM_LRS))
            .withKind(Kind.BLOCK_BLOB_STORAGE)
            .withNetworkRuleSet(
                new NetworkRuleSet()
                    .withBypass(Bypass.AZURE_SERVICES)
                    .withVirtualNetworkRules(
                        Arrays
                            .asList(
                                new VirtualNetworkRule()
                                    .withVirtualNetworkResourceId(
                                        "/subscriptions/{subscription-id}/resourceGroups/res9101/providers/Microsoft.Network/virtualNetworks/net123/subnets/subnet12")))
                    .withIpRules(Arrays.asList())
                    .withDefaultAction(DefaultAction.ALLOW))
            .withEnableHttpsTrafficOnly(false)
            .withIsHnsEnabled(true)
            .withEnableNfsV3(true)
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountCreatePremiumBlockBlobStorage.json
     */
    /**
     * Sample code: StorageAccountCreatePremiumBlockBlobStorage.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountCreatePremiumBlockBlobStorage(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .storageAccounts()
            .define("sto4445")
            .withRegion("eastus")
            .withExistingResourceGroup("res9101")
            .withSku(new Sku().withName(SkuName.PREMIUM_LRS))
            .withKind(Kind.BLOCK_BLOB_STORAGE)
            .withTags(mapOf("key1", "value1", "key2", "value2"))
            .withEncryption(
                new Encryption()
                    .withServices(
                        new EncryptionServices()
                            .withBlob(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT))
                            .withFile(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT)))
                    .withKeySource(KeySource.MICROSOFT_STORAGE)
                    .withRequireInfrastructureEncryption(false))
            .withMinimumTlsVersion(MinimumTlsVersion.TLS1_2)
            .withAllowSharedKeyAccess(true)
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountCreateWithImmutabilityPolicy.json
     */
    /**
     * Sample code: StorageAccountCreateWithImmutabilityPolicy.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountCreateWithImmutabilityPolicy(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .storageAccounts()
            .define("sto4445")
            .withRegion("eastus")
            .withExistingResourceGroup("res9101")
            .withSku(new Sku().withName(SkuName.STANDARD_GRS))
            .withKind(Kind.STORAGE)
            .withExtendedLocation(
                new ExtendedLocation().withName("losangeles001").withType(ExtendedLocationTypes.EDGE_ZONE))
            .withImmutableStorageWithVersioning(
                new ImmutableStorageAccount()
                    .withEnabled(true)
                    .withImmutabilityPolicy(
                        new AccountImmutabilityPolicyProperties()
                            .withImmutabilityPeriodSinceCreationInDays(15)
                            .withState(AccountImmutabilityPolicyState.UNLOCKED)
                            .withAllowProtectedAppendWrites(true)))
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountCreateAllowedCopyScopeToPrivateLink.json
     */
    /**
     * Sample code: StorageAccountCreateAllowedCopyScopeToPrivateLink.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountCreateAllowedCopyScopeToPrivateLink(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .storageAccounts()
            .define("sto4445")
            .withRegion("eastus")
            .withExistingResourceGroup("res9101")
            .withSku(new Sku().withName(SkuName.STANDARD_GRS))
            .withKind(Kind.STORAGE)
            .withTags(mapOf("key1", "value1", "key2", "value2"))
            .withAllowedCopyScope(AllowedCopyScope.PRIVATE_LINK)
            .withSasPolicy(
                new SasPolicy().withSasExpirationPeriod("1.15:59:59").withExpirationAction(ExpirationAction.LOG))
            .withKeyPolicy(new KeyPolicy().withKeyExpirationPeriodInDays(20))
            .withEncryption(
                new Encryption()
                    .withServices(
                        new EncryptionServices()
                            .withBlob(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT))
                            .withFile(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT)))
                    .withKeySource(KeySource.MICROSOFT_STORAGE)
                    .withRequireInfrastructureEncryption(false))
            .withIsHnsEnabled(true)
            .withRoutingPreference(
                new RoutingPreference()
                    .withRoutingChoice(RoutingChoice.MICROSOFT_ROUTING)
                    .withPublishMicrosoftEndpoints(true)
                    .withPublishInternetEndpoints(true))
            .withAllowBlobPublicAccess(false)
            .withMinimumTlsVersion(MinimumTlsVersion.TLS1_2)
            .withAllowSharedKeyAccess(true)
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountCreate.json
     */
    /**
     * Sample code: StorageAccountCreate.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountCreate(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .storageAccounts()
            .define("sto4445")
            .withRegion("eastus")
            .withExistingResourceGroup("res9101")
            .withSku(new Sku().withName(SkuName.STANDARD_GRS))
            .withKind(Kind.STORAGE)
            .withTags(mapOf("key1", "value1", "key2", "value2"))
            .withExtendedLocation(
                new ExtendedLocation().withName("losangeles001").withType(ExtendedLocationTypes.EDGE_ZONE))
            .withSasPolicy(
                new SasPolicy().withSasExpirationPeriod("1.15:59:59").withExpirationAction(ExpirationAction.LOG))
            .withKeyPolicy(new KeyPolicy().withKeyExpirationPeriodInDays(20))
            .withEncryption(
                new Encryption()
                    .withServices(
                        new EncryptionServices()
                            .withBlob(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT))
                            .withFile(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT)))
                    .withKeySource(KeySource.MICROSOFT_STORAGE)
                    .withRequireInfrastructureEncryption(false))
            .withIsSftpEnabled(true)
            .withIsHnsEnabled(true)
            .withRoutingPreference(
                new RoutingPreference()
                    .withRoutingChoice(RoutingChoice.MICROSOFT_ROUTING)
                    .withPublishMicrosoftEndpoints(true)
                    .withPublishInternetEndpoints(true))
            .withAllowBlobPublicAccess(false)
            .withMinimumTlsVersion(MinimumTlsVersion.TLS1_2)
            .withAllowSharedKeyAccess(true)
            .withDefaultToOAuthAuthentication(false)
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountCreateEnablePublicNetworkAccess.json
     */
    /**
     * Sample code: StorageAccountCreateEnablePublicNetworkAccess.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountCreateEnablePublicNetworkAccess(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .storageAccounts()
            .define("sto4445")
            .withRegion("eastus")
            .withExistingResourceGroup("res9101")
            .withSku(new Sku().withName(SkuName.STANDARD_GRS))
            .withKind(Kind.STORAGE)
            .withTags(mapOf("key1", "value1", "key2", "value2"))
            .withExtendedLocation(
                new ExtendedLocation().withName("losangeles001").withType(ExtendedLocationTypes.EDGE_ZONE))
            .withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
            .withSasPolicy(
                new SasPolicy().withSasExpirationPeriod("1.15:59:59").withExpirationAction(ExpirationAction.LOG))
            .withKeyPolicy(new KeyPolicy().withKeyExpirationPeriodInDays(20))
            .withEncryption(
                new Encryption()
                    .withServices(
                        new EncryptionServices()
                            .withBlob(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT))
                            .withFile(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT)))
                    .withKeySource(KeySource.MICROSOFT_STORAGE)
                    .withRequireInfrastructureEncryption(false))
            .withIsHnsEnabled(true)
            .withRoutingPreference(
                new RoutingPreference()
                    .withRoutingChoice(RoutingChoice.MICROSOFT_ROUTING)
                    .withPublishMicrosoftEndpoints(true)
                    .withPublishInternetEndpoints(true))
            .withAllowBlobPublicAccess(false)
            .withMinimumTlsVersion(MinimumTlsVersion.TLS1_2)
            .withAllowSharedKeyAccess(true)
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountCreateAllowedCopyScopeToAAD.json
     */
    /**
     * Sample code: StorageAccountCreateAllowedCopyScopeToAAD.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountCreateAllowedCopyScopeToAAD(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .storageAccounts()
            .define("sto4445")
            .withRegion("eastus")
            .withExistingResourceGroup("res9101")
            .withSku(new Sku().withName(SkuName.STANDARD_GRS))
            .withKind(Kind.STORAGE)
            .withTags(mapOf("key1", "value1", "key2", "value2"))
            .withAllowedCopyScope(AllowedCopyScope.AAD)
            .withSasPolicy(
                new SasPolicy().withSasExpirationPeriod("1.15:59:59").withExpirationAction(ExpirationAction.LOG))
            .withKeyPolicy(new KeyPolicy().withKeyExpirationPeriodInDays(20))
            .withEncryption(
                new Encryption()
                    .withServices(
                        new EncryptionServices()
                            .withBlob(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT))
                            .withFile(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT)))
                    .withKeySource(KeySource.MICROSOFT_STORAGE)
                    .withRequireInfrastructureEncryption(false))
            .withIsHnsEnabled(true)
            .withRoutingPreference(
                new RoutingPreference()
                    .withRoutingChoice(RoutingChoice.MICROSOFT_ROUTING)
                    .withPublishMicrosoftEndpoints(true)
                    .withPublishInternetEndpoints(true))
            .withAllowBlobPublicAccess(false)
            .withMinimumTlsVersion(MinimumTlsVersion.TLS1_2)
            .withAllowSharedKeyAccess(true)
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountCreateUserAssignedIdentityWithFederatedIdentityClientId.json
     */
    /**
     * Sample code: StorageAccountCreateUserAssignedIdentityWithFederatedIdentityClientId.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountCreateUserAssignedIdentityWithFederatedIdentityClientId(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .storageAccounts()
            .define("sto131918")
            .withRegion("eastus")
            .withExistingResourceGroup("res131918")
            .withSku(new Sku().withName(SkuName.STANDARD_LRS))
            .withKind(Kind.STORAGE)
            .withIdentity(
                new Identity()
                    .withType(IdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/{subscription-id}/resourceGroups/res9101/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{managed-identity-name}",
                            new UserAssignedIdentity())))
            .withEncryption(
                new Encryption()
                    .withServices(
                        new EncryptionServices()
                            .withBlob(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT))
                            .withFile(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT)))
                    .withKeySource(KeySource.MICROSOFT_KEYVAULT)
                    .withKeyVaultProperties(
                        new KeyVaultProperties()
                            .withKeyName("wrappingKey")
                            .withKeyVersion("")
                            .withKeyVaultUri("https://myvault8569.vault.azure.net"))
                    .withEncryptionIdentity(
                        new EncryptionIdentity()
                            .withEncryptionUserAssignedIdentity(
                                "/subscriptions/{subscription-id}/resourceGroups/res9101/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{managed-identity-name}")
                            .withEncryptionFederatedIdentityClientId("f83c6b1b-4d34-47e4-bb34-9d83df58b540")))
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountCreateDisallowPublicNetworkAccess.json
     */
    /**
     * Sample code: StorageAccountCreateDisallowPublicNetworkAccess.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountCreateDisallowPublicNetworkAccess(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .storageAccounts()
            .define("sto4445")
            .withRegion("eastus")
            .withExistingResourceGroup("res9101")
            .withSku(new Sku().withName(SkuName.STANDARD_GRS))
            .withKind(Kind.STORAGE)
            .withTags(mapOf("key1", "value1", "key2", "value2"))
            .withExtendedLocation(
                new ExtendedLocation().withName("losangeles001").withType(ExtendedLocationTypes.EDGE_ZONE))
            .withPublicNetworkAccess(PublicNetworkAccess.DISABLED)
            .withSasPolicy(
                new SasPolicy().withSasExpirationPeriod("1.15:59:59").withExpirationAction(ExpirationAction.LOG))
            .withKeyPolicy(new KeyPolicy().withKeyExpirationPeriodInDays(20))
            .withEncryption(
                new Encryption()
                    .withServices(
                        new EncryptionServices()
                            .withBlob(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT))
                            .withFile(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT)))
                    .withKeySource(KeySource.MICROSOFT_STORAGE)
                    .withRequireInfrastructureEncryption(false))
            .withIsHnsEnabled(true)
            .withRoutingPreference(
                new RoutingPreference()
                    .withRoutingChoice(RoutingChoice.MICROSOFT_ROUTING)
                    .withPublishMicrosoftEndpoints(true)
                    .withPublishInternetEndpoints(true))
            .withAllowBlobPublicAccess(false)
            .withMinimumTlsVersion(MinimumTlsVersion.TLS1_2)
            .withAllowSharedKeyAccess(true)
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountCreateDnsEndpointTypeToStandard.json
     */
    /**
     * Sample code: StorageAccountCreateDnsEndpointTypeToStandard.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountCreateDnsEndpointTypeToStandard(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .storageAccounts()
            .define("sto4445")
            .withRegion("eastus")
            .withExistingResourceGroup("res9101")
            .withSku(new Sku().withName(SkuName.STANDARD_GRS))
            .withKind(Kind.STORAGE)
            .withTags(mapOf("key1", "value1", "key2", "value2"))
            .withExtendedLocation(
                new ExtendedLocation().withName("losangeles001").withType(ExtendedLocationTypes.EDGE_ZONE))
            .withSasPolicy(
                new SasPolicy().withSasExpirationPeriod("1.15:59:59").withExpirationAction(ExpirationAction.LOG))
            .withKeyPolicy(new KeyPolicy().withKeyExpirationPeriodInDays(20))
            .withEncryption(
                new Encryption()
                    .withServices(
                        new EncryptionServices()
                            .withBlob(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT))
                            .withFile(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT)))
                    .withKeySource(KeySource.MICROSOFT_STORAGE)
                    .withRequireInfrastructureEncryption(false))
            .withIsSftpEnabled(true)
            .withIsHnsEnabled(true)
            .withRoutingPreference(
                new RoutingPreference()
                    .withRoutingChoice(RoutingChoice.MICROSOFT_ROUTING)
                    .withPublishMicrosoftEndpoints(true)
                    .withPublishInternetEndpoints(true))
            .withAllowBlobPublicAccess(false)
            .withMinimumTlsVersion(MinimumTlsVersion.TLS1_2)
            .withAllowSharedKeyAccess(true)
            .withDefaultToOAuthAuthentication(false)
            .withDnsEndpointType(DnsEndpointType.STANDARD)
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountCreateDnsEndpointTypeToAzureDnsZone.json
     */
    /**
     * Sample code: StorageAccountCreateDnsEndpointTypeToAzureDnsZone.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountCreateDnsEndpointTypeToAzureDnsZone(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .storageAccounts()
            .define("sto4445")
            .withRegion("eastus")
            .withExistingResourceGroup("res9101")
            .withSku(new Sku().withName(SkuName.STANDARD_GRS))
            .withKind(Kind.STORAGE)
            .withTags(mapOf("key1", "value1", "key2", "value2"))
            .withExtendedLocation(
                new ExtendedLocation().withName("losangeles001").withType(ExtendedLocationTypes.EDGE_ZONE))
            .withSasPolicy(
                new SasPolicy().withSasExpirationPeriod("1.15:59:59").withExpirationAction(ExpirationAction.LOG))
            .withKeyPolicy(new KeyPolicy().withKeyExpirationPeriodInDays(20))
            .withEncryption(
                new Encryption()
                    .withServices(
                        new EncryptionServices()
                            .withBlob(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT))
                            .withFile(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT)))
                    .withKeySource(KeySource.MICROSOFT_STORAGE)
                    .withRequireInfrastructureEncryption(false))
            .withIsSftpEnabled(true)
            .withIsHnsEnabled(true)
            .withRoutingPreference(
                new RoutingPreference()
                    .withRoutingChoice(RoutingChoice.MICROSOFT_ROUTING)
                    .withPublishMicrosoftEndpoints(true)
                    .withPublishInternetEndpoints(true))
            .withAllowBlobPublicAccess(false)
            .withMinimumTlsVersion(MinimumTlsVersion.TLS1_2)
            .withAllowSharedKeyAccess(true)
            .withDefaultToOAuthAuthentication(false)
            .withDnsEndpointType(DnsEndpointType.AZURE_DNS_ZONE)
            .create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountCreateUserAssignedEncryptionIdentityWithCMK.json
     */
    /**
     * Sample code: StorageAccountCreateUserAssignedEncryptionIdentityWithCMK.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountCreateUserAssignedEncryptionIdentityWithCMK(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .storageAccounts()
            .define("sto4445")
            .withRegion("eastus")
            .withExistingResourceGroup("res9101")
            .withSku(new Sku().withName(SkuName.STANDARD_LRS))
            .withKind(Kind.STORAGE)
            .withIdentity(
                new Identity()
                    .withType(IdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/{subscription-id}/resourceGroups/res9101/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{managed-identity-name}",
                            new UserAssignedIdentity())))
            .withEncryption(
                new Encryption()
                    .withServices(
                        new EncryptionServices()
                            .withBlob(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT))
                            .withFile(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT)))
                    .withKeySource(KeySource.MICROSOFT_KEYVAULT)
                    .withKeyVaultProperties(
                        new KeyVaultProperties()
                            .withKeyName("wrappingKey")
                            .withKeyVersion("")
                            .withKeyVaultUri("https://myvault8569.vault.azure.net"))
                    .withEncryptionIdentity(
                        new EncryptionIdentity()
                            .withEncryptionUserAssignedIdentity(
                                "/subscriptions/{subscription-id}/resourceGroups/res9101/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{managed-identity-name}")))
            .create();
    }

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

### StorageAccounts_Delete

```java
import com.azure.core.util.Context;

/** Samples for StorageAccounts Delete. */
public final class StorageAccountsDeleteSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountDelete.json
     */
    /**
     * Sample code: StorageAccountDelete.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountDelete(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.storageAccounts().deleteWithResponse("res4228", "sto2434", Context.NONE);
    }
}
```

### StorageAccounts_Failover

```java
import com.azure.core.util.Context;

/** Samples for StorageAccounts Failover. */
public final class StorageAccountsFailoverSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountFailover.json
     */
    /**
     * Sample code: StorageAccountFailover.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountFailover(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.storageAccounts().failover("res4228", "sto2434", Context.NONE);
    }
}
```

### StorageAccounts_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for StorageAccounts GetByResourceGroup. */
public final class StorageAccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountGetProperties.json
     */
    /**
     * Sample code: StorageAccountGetProperties.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountGetProperties(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.storageAccounts().getByResourceGroupWithResponse("res9407", "sto8596", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountGetPropertiesCMKVersionExpirationTime.json
     */
    /**
     * Sample code: StorageAccountGetPropertiesCMKVersionExpirationTime.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountGetPropertiesCMKVersionExpirationTime(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.storageAccounts().getByResourceGroupWithResponse("res9407", "sto8596", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountGetAsyncSkuConversionStatus.json
     */
    /**
     * Sample code: StorageAccountGetAsyncSkuConversionStatus.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountGetAsyncSkuConversionStatus(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.storageAccounts().getByResourceGroupWithResponse("res9407", "sto8596", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountGetPropertiesCMKEnabled.json
     */
    /**
     * Sample code: StorageAccountGetPropertiesCMKEnabled.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountGetPropertiesCMKEnabled(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.storageAccounts().getByResourceGroupWithResponse("res9407", "sto8596", null, Context.NONE);
    }
}
```

### StorageAccounts_HierarchicalNamespaceMigration

```java
import com.azure.core.util.Context;

/** Samples for StorageAccounts HierarchicalNamespaceMigration. */
public final class StorageAccountsHierarchicalNamespaceMigrationSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountHierarchicalNamespaceMigration.json
     */
    /**
     * Sample code: StorageAccountHierarchicalNamespaceMigration.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountHierarchicalNamespaceMigration(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .storageAccounts()
            .hierarchicalNamespaceMigration("res4228", "sto2434", "HnsOnValidationRequest", Context.NONE);
    }
}
```

### StorageAccounts_List

```java
import com.azure.core.util.Context;

/** Samples for StorageAccounts List. */
public final class StorageAccountsListSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountList.json
     */
    /**
     * Sample code: StorageAccountList.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountList(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.storageAccounts().list(Context.NONE);
    }
}
```

### StorageAccounts_ListAccountSas

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.generated.models.AccountSasParameters;
import com.azure.resourcemanager.storage.generated.models.HttpProtocol;
import com.azure.resourcemanager.storage.generated.models.Permissions;
import com.azure.resourcemanager.storage.generated.models.Services;
import com.azure.resourcemanager.storage.generated.models.SignedResourceTypes;
import java.time.OffsetDateTime;

/** Samples for StorageAccounts ListAccountSas. */
public final class StorageAccountsListAccountSasSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountListAccountSAS.json
     */
    /**
     * Sample code: StorageAccountListAccountSAS.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountListAccountSAS(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .storageAccounts()
            .listAccountSasWithResponse(
                "res7985",
                "sto8588",
                new AccountSasParameters()
                    .withServices(Services.B)
                    .withResourceTypes(SignedResourceTypes.S)
                    .withPermissions(Permissions.R)
                    .withProtocols(HttpProtocol.HTTPS_HTTP)
                    .withSharedAccessStartTime(OffsetDateTime.parse("2017-05-24T10:42:03.1567373Z"))
                    .withSharedAccessExpiryTime(OffsetDateTime.parse("2017-05-24T11:42:03.1567373Z"))
                    .withKeyToSign("key1"),
                Context.NONE);
    }
}
```

### StorageAccounts_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for StorageAccounts ListByResourceGroup. */
public final class StorageAccountsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountListByResourceGroup.json
     */
    /**
     * Sample code: StorageAccountListByResourceGroup.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountListByResourceGroup(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.storageAccounts().listByResourceGroup("res6117", Context.NONE);
    }
}
```

### StorageAccounts_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for StorageAccounts ListKeys. */
public final class StorageAccountsListKeysSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountListKeys.json
     */
    /**
     * Sample code: StorageAccountListKeys.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountListKeys(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.storageAccounts().listKeysWithResponse("res418", "sto2220", null, Context.NONE);
    }
}
```

### StorageAccounts_ListServiceSas

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.generated.models.Permissions;
import com.azure.resourcemanager.storage.generated.models.ServiceSasParameters;
import com.azure.resourcemanager.storage.generated.models.SignedResource;
import java.time.OffsetDateTime;

/** Samples for StorageAccounts ListServiceSas. */
public final class StorageAccountsListServiceSasSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountListServiceSAS.json
     */
    /**
     * Sample code: StorageAccountListServiceSAS.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountListServiceSAS(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .storageAccounts()
            .listServiceSasWithResponse(
                "res7439",
                "sto1299",
                new ServiceSasParameters()
                    .withCanonicalizedResource("/blob/sto1299/music")
                    .withResource(SignedResource.C)
                    .withPermissions(Permissions.L)
                    .withSharedAccessExpiryTime(OffsetDateTime.parse("2017-05-24T11:32:48.8457197Z")),
                Context.NONE);
    }
}
```

### StorageAccounts_RegenerateKey

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.generated.models.StorageAccountRegenerateKeyParameters;

/** Samples for StorageAccounts RegenerateKey. */
public final class StorageAccountsRegenerateKeySamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountRegenerateKey.json
     */
    /**
     * Sample code: StorageAccountRegenerateKey.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountRegenerateKey(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .storageAccounts()
            .regenerateKeyWithResponse(
                "res4167", "sto3539", new StorageAccountRegenerateKeyParameters().withKeyName("key2"), Context.NONE);
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountRegenerateKerbKey.json
     */
    /**
     * Sample code: StorageAccountRegenerateKerbKey.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountRegenerateKerbKey(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .storageAccounts()
            .regenerateKeyWithResponse(
                "res4167", "sto3539", new StorageAccountRegenerateKeyParameters().withKeyName("kerb1"), Context.NONE);
    }
}
```

### StorageAccounts_RestoreBlobRanges

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.generated.models.BlobRestoreParameters;
import com.azure.resourcemanager.storage.generated.models.BlobRestoreRange;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Samples for StorageAccounts RestoreBlobRanges. */
public final class StorageAccountsRestoreBlobRangesSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/BlobRangesRestore.json
     */
    /**
     * Sample code: BlobRangesRestore.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void blobRangesRestore(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .storageAccounts()
            .restoreBlobRanges(
                "res9101",
                "sto4445",
                new BlobRestoreParameters()
                    .withTimeToRestore(OffsetDateTime.parse("2019-04-20T15:30:00.0000000Z"))
                    .withBlobRanges(
                        Arrays
                            .asList(
                                new BlobRestoreRange()
                                    .withStartRange("container/blobpath1")
                                    .withEndRange("container/blobpath2"),
                                new BlobRestoreRange().withStartRange("container2/blobpath3").withEndRange(""))),
                Context.NONE);
    }
}
```

### StorageAccounts_RevokeUserDelegationKeys

```java
import com.azure.core.util.Context;

/** Samples for StorageAccounts RevokeUserDelegationKeys. */
public final class StorageAccountsRevokeUserDelegationKeysSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountRevokeUserDelegationKeys.json
     */
    /**
     * Sample code: StorageAccountRevokeUserDelegationKeys.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountRevokeUserDelegationKeys(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.storageAccounts().revokeUserDelegationKeysWithResponse("res4167", "sto3539", Context.NONE);
    }
}
```

### StorageAccounts_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.generated.models.AccountImmutabilityPolicyProperties;
import com.azure.resourcemanager.storage.generated.models.AccountImmutabilityPolicyState;
import com.azure.resourcemanager.storage.generated.models.ActiveDirectoryProperties;
import com.azure.resourcemanager.storage.generated.models.ActiveDirectoryPropertiesAccountType;
import com.azure.resourcemanager.storage.generated.models.AllowedCopyScope;
import com.azure.resourcemanager.storage.generated.models.AzureFilesIdentityBasedAuthentication;
import com.azure.resourcemanager.storage.generated.models.DefaultAction;
import com.azure.resourcemanager.storage.generated.models.DirectoryServiceOptions;
import com.azure.resourcemanager.storage.generated.models.Encryption;
import com.azure.resourcemanager.storage.generated.models.EncryptionIdentity;
import com.azure.resourcemanager.storage.generated.models.EncryptionService;
import com.azure.resourcemanager.storage.generated.models.EncryptionServices;
import com.azure.resourcemanager.storage.generated.models.ExpirationAction;
import com.azure.resourcemanager.storage.generated.models.Identity;
import com.azure.resourcemanager.storage.generated.models.IdentityType;
import com.azure.resourcemanager.storage.generated.models.ImmutableStorageAccount;
import com.azure.resourcemanager.storage.generated.models.KeyPolicy;
import com.azure.resourcemanager.storage.generated.models.KeySource;
import com.azure.resourcemanager.storage.generated.models.KeyType;
import com.azure.resourcemanager.storage.generated.models.KeyVaultProperties;
import com.azure.resourcemanager.storage.generated.models.Kind;
import com.azure.resourcemanager.storage.generated.models.MinimumTlsVersion;
import com.azure.resourcemanager.storage.generated.models.NetworkRuleSet;
import com.azure.resourcemanager.storage.generated.models.PublicNetworkAccess;
import com.azure.resourcemanager.storage.generated.models.ResourceAccessRule;
import com.azure.resourcemanager.storage.generated.models.RoutingChoice;
import com.azure.resourcemanager.storage.generated.models.RoutingPreference;
import com.azure.resourcemanager.storage.generated.models.SasPolicy;
import com.azure.resourcemanager.storage.generated.models.Sku;
import com.azure.resourcemanager.storage.generated.models.SkuName;
import com.azure.resourcemanager.storage.generated.models.StorageAccount;
import com.azure.resourcemanager.storage.generated.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for StorageAccounts Update. */
public final class StorageAccountsUpdateSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountUpdateWithImmutabilityPolicy.json
     */
    /**
     * Sample code: StorageAccountUpdateWithImmutabilityPolicy.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountUpdateWithImmutabilityPolicy(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        StorageAccount resource =
            manager
                .storageAccounts()
                .getByResourceGroupWithResponse("res9407", "sto8596", null, Context.NONE)
                .getValue();
        resource
            .update()
            .withImmutableStorageWithVersioning(
                new ImmutableStorageAccount()
                    .withEnabled(true)
                    .withImmutabilityPolicy(
                        new AccountImmutabilityPolicyProperties()
                            .withImmutabilityPeriodSinceCreationInDays(15)
                            .withState(AccountImmutabilityPolicyState.LOCKED)
                            .withAllowProtectedAppendWrites(true)))
            .apply();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountUpdateUserAssignedIdentityWithFederatedIdentityClientId.json
     */
    /**
     * Sample code: StorageAccountUpdateUserAssignedIdentityWithFederatedIdentityClientId.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountUpdateUserAssignedIdentityWithFederatedIdentityClientId(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        StorageAccount resource =
            manager
                .storageAccounts()
                .getByResourceGroupWithResponse("res131918", "sto131918", null, Context.NONE)
                .getValue();
        resource
            .update()
            .withSku(new Sku().withName(SkuName.STANDARD_LRS))
            .withIdentity(
                new Identity()
                    .withType(IdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/{subscription-id}/resourceGroups/res9101/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{managed-identity-name}",
                            new UserAssignedIdentity())))
            .withKind(Kind.STORAGE)
            .withEncryption(
                new Encryption()
                    .withServices(
                        new EncryptionServices()
                            .withBlob(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT))
                            .withFile(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT)))
                    .withKeySource(KeySource.MICROSOFT_KEYVAULT)
                    .withKeyVaultProperties(
                        new KeyVaultProperties()
                            .withKeyName("wrappingKey")
                            .withKeyVersion("")
                            .withKeyVaultUri("https://myvault8569.vault.azure.net"))
                    .withEncryptionIdentity(
                        new EncryptionIdentity()
                            .withEncryptionUserAssignedIdentity(
                                "/subscriptions/{subscription-id}/resourceGroups/res9101/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{managed-identity-name}")
                            .withEncryptionFederatedIdentityClientId("3109d1c4-a5de-4d84-8832-feabb916a4b6")))
            .apply();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountEnableAD.json
     */
    /**
     * Sample code: StorageAccountEnableAD.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountEnableAD(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        StorageAccount resource =
            manager
                .storageAccounts()
                .getByResourceGroupWithResponse("res9407", "sto8596", null, Context.NONE)
                .getValue();
        resource
            .update()
            .withAzureFilesIdentityBasedAuthentication(
                new AzureFilesIdentityBasedAuthentication()
                    .withDirectoryServiceOptions(DirectoryServiceOptions.AD)
                    .withActiveDirectoryProperties(
                        new ActiveDirectoryProperties()
                            .withDomainName("adtest.com")
                            .withNetBiosDomainName("adtest.com")
                            .withForestName("adtest.com")
                            .withDomainGuid("aebfc118-9fa9-4732-a21f-d98e41a77ae1")
                            .withDomainSid("S-1-5-21-2400535526-2334094090-2402026252")
                            .withAzureStorageSid("S-1-5-21-2400535526-2334094090-2402026252-0012")
                            .withSamAccountName("sam12498")
                            .withAccountType(ActiveDirectoryPropertiesAccountType.USER)))
            .apply();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountUpdateAllowedCopyScopeToAAD.json
     */
    /**
     * Sample code: StorageAccountUpdateAllowedCopyScopeToAAD.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountUpdateAllowedCopyScopeToAAD(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        StorageAccount resource =
            manager
                .storageAccounts()
                .getByResourceGroupWithResponse("res9407", "sto8596", null, Context.NONE)
                .getValue();
        resource
            .update()
            .withEncryption(
                new Encryption()
                    .withServices(
                        new EncryptionServices()
                            .withBlob(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT))
                            .withFile(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT)))
                    .withKeySource(KeySource.MICROSOFT_STORAGE))
            .withSasPolicy(
                new SasPolicy().withSasExpirationPeriod("1.15:59:59").withExpirationAction(ExpirationAction.LOG))
            .withKeyPolicy(new KeyPolicy().withKeyExpirationPeriodInDays(20))
            .withNetworkRuleSet(
                new NetworkRuleSet()
                    .withResourceAccessRules(
                        Arrays
                            .asList(
                                new ResourceAccessRule()
                                    .withTenantId("72f988bf-86f1-41af-91ab-2d7cd011db47")
                                    .withResourceId(
                                        "/subscriptions/a7e99807-abbf-4642-bdec-2c809a96a8bc/resourceGroups/res9407/providers/Microsoft.Synapse/workspaces/testworkspace")))
                    .withDefaultAction(DefaultAction.ALLOW))
            .withRoutingPreference(
                new RoutingPreference()
                    .withRoutingChoice(RoutingChoice.MICROSOFT_ROUTING)
                    .withPublishMicrosoftEndpoints(true)
                    .withPublishInternetEndpoints(true))
            .withAllowBlobPublicAccess(false)
            .withMinimumTlsVersion(MinimumTlsVersion.TLS1_2)
            .withAllowSharedKeyAccess(true)
            .withAllowedCopyScope(AllowedCopyScope.AAD)
            .apply();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountUpdateDisablePublicNetworkAccess.json
     */
    /**
     * Sample code: StorageAccountUpdateDisablePublicNetworkAccess.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountUpdateDisablePublicNetworkAccess(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        StorageAccount resource =
            manager
                .storageAccounts()
                .getByResourceGroupWithResponse("res9407", "sto8596", null, Context.NONE)
                .getValue();
        resource
            .update()
            .withEncryption(
                new Encryption()
                    .withServices(
                        new EncryptionServices()
                            .withBlob(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT))
                            .withFile(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT)))
                    .withKeySource(KeySource.MICROSOFT_STORAGE))
            .withSasPolicy(
                new SasPolicy().withSasExpirationPeriod("1.15:59:59").withExpirationAction(ExpirationAction.LOG))
            .withKeyPolicy(new KeyPolicy().withKeyExpirationPeriodInDays(20))
            .withNetworkRuleSet(
                new NetworkRuleSet()
                    .withResourceAccessRules(
                        Arrays
                            .asList(
                                new ResourceAccessRule()
                                    .withTenantId("72f988bf-86f1-41af-91ab-2d7cd011db47")
                                    .withResourceId(
                                        "/subscriptions/a7e99807-abbf-4642-bdec-2c809a96a8bc/resourceGroups/res9407/providers/Microsoft.Synapse/workspaces/testworkspace")))
                    .withDefaultAction(DefaultAction.ALLOW))
            .withRoutingPreference(
                new RoutingPreference()
                    .withRoutingChoice(RoutingChoice.MICROSOFT_ROUTING)
                    .withPublishMicrosoftEndpoints(true)
                    .withPublishInternetEndpoints(true))
            .withAllowBlobPublicAccess(false)
            .withMinimumTlsVersion(MinimumTlsVersion.TLS1_2)
            .withAllowSharedKeyAccess(true)
            .withPublicNetworkAccess(PublicNetworkAccess.DISABLED)
            .apply();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountEnableCMK.json
     */
    /**
     * Sample code: StorageAccountEnableCMK.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountEnableCMK(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        StorageAccount resource =
            manager
                .storageAccounts()
                .getByResourceGroupWithResponse("res9407", "sto8596", null, Context.NONE)
                .getValue();
        resource
            .update()
            .withEncryption(
                new Encryption()
                    .withServices(
                        new EncryptionServices()
                            .withBlob(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT))
                            .withFile(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT)))
                    .withKeySource(KeySource.MICROSOFT_KEYVAULT)
                    .withKeyVaultProperties(
                        new KeyVaultProperties()
                            .withKeyName("wrappingKey")
                            .withKeyVersion("")
                            .withKeyVaultUri("https://myvault8569.vault.azure.net")))
            .apply();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountUpdate.json
     */
    /**
     * Sample code: StorageAccountUpdate.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountUpdate(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        StorageAccount resource =
            manager
                .storageAccounts()
                .getByResourceGroupWithResponse("res9407", "sto8596", null, Context.NONE)
                .getValue();
        resource
            .update()
            .withEncryption(
                new Encryption()
                    .withServices(
                        new EncryptionServices()
                            .withBlob(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT))
                            .withFile(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT)))
                    .withKeySource(KeySource.MICROSOFT_STORAGE))
            .withSasPolicy(
                new SasPolicy().withSasExpirationPeriod("1.15:59:59").withExpirationAction(ExpirationAction.LOG))
            .withKeyPolicy(new KeyPolicy().withKeyExpirationPeriodInDays(20))
            .withIsSftpEnabled(true)
            .withIsLocalUserEnabled(true)
            .withNetworkRuleSet(
                new NetworkRuleSet()
                    .withResourceAccessRules(
                        Arrays
                            .asList(
                                new ResourceAccessRule()
                                    .withTenantId("72f988bf-86f1-41af-91ab-2d7cd011db47")
                                    .withResourceId(
                                        "/subscriptions/a7e99807-abbf-4642-bdec-2c809a96a8bc/resourceGroups/res9407/providers/Microsoft.Synapse/workspaces/testworkspace")))
                    .withDefaultAction(DefaultAction.ALLOW))
            .withRoutingPreference(
                new RoutingPreference()
                    .withRoutingChoice(RoutingChoice.MICROSOFT_ROUTING)
                    .withPublishMicrosoftEndpoints(true)
                    .withPublishInternetEndpoints(true))
            .withAllowBlobPublicAccess(false)
            .withMinimumTlsVersion(MinimumTlsVersion.TLS1_2)
            .withAllowSharedKeyAccess(true)
            .withDefaultToOAuthAuthentication(false)
            .apply();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountUpdateUserAssignedEncryptionIdentityWithCMK.json
     */
    /**
     * Sample code: StorageAccountUpdateUserAssignedEncryptionIdentityWithCMK.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void storageAccountUpdateUserAssignedEncryptionIdentityWithCMK(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        StorageAccount resource =
            manager
                .storageAccounts()
                .getByResourceGroupWithResponse("res9101", "sto4445", null, Context.NONE)
                .getValue();
        resource
            .update()
            .withSku(new Sku().withName(SkuName.STANDARD_LRS))
            .withIdentity(
                new Identity()
                    .withType(IdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/{subscription-id}/resourceGroups/res9101/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{managed-identity-name}",
                            new UserAssignedIdentity())))
            .withKind(Kind.STORAGE)
            .withEncryption(
                new Encryption()
                    .withServices(
                        new EncryptionServices()
                            .withBlob(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT))
                            .withFile(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT)))
                    .withKeySource(KeySource.MICROSOFT_KEYVAULT)
                    .withKeyVaultProperties(
                        new KeyVaultProperties()
                            .withKeyName("wrappingKey")
                            .withKeyVersion("")
                            .withKeyVaultUri("https://myvault8569.vault.azure.net"))
                    .withEncryptionIdentity(
                        new EncryptionIdentity()
                            .withEncryptionUserAssignedIdentity(
                                "/subscriptions/{subscription-id}/resourceGroups/res9101/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{managed-identity-name}")))
            .apply();
    }

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

### Table_Create

```java
import com.azure.resourcemanager.storage.generated.models.TableAccessPolicy;
import com.azure.resourcemanager.storage.generated.models.TableSignedIdentifier;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Samples for Table Create. */
public final class TableCreateSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/TableOperationPut.json
     */
    /**
     * Sample code: TableOperationPut.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void tableOperationPut(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.tables().define("table6185").withExistingStorageAccount("res3376", "sto328").create();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/TableOperationPutOrPatchAcls.json
     */
    /**
     * Sample code: TableOperationPutOrPatchAcls.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void tableOperationPutOrPatchAcls(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .tables()
            .define("table6185")
            .withExistingStorageAccount("res3376", "sto328")
            .withSignedIdentifiers(
                Arrays
                    .asList(
                        new TableSignedIdentifier()
                            .withId("MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI")
                            .withAccessPolicy(
                                new TableAccessPolicy()
                                    .withStartTime(OffsetDateTime.parse("2022-03-17T08:49:37.0000000Z"))
                                    .withExpiryTime(OffsetDateTime.parse("2022-03-20T08:49:37.0000000Z"))
                                    .withPermission("raud")),
                        new TableSignedIdentifier()
                            .withId("PTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODklMTI")
                            .withAccessPolicy(
                                new TableAccessPolicy()
                                    .withStartTime(OffsetDateTime.parse("2022-03-17T08:49:37.0000000Z"))
                                    .withExpiryTime(OffsetDateTime.parse("2022-03-20T08:49:37.0000000Z"))
                                    .withPermission("rad"))))
            .create();
    }
}
```

### Table_Delete

```java
import com.azure.core.util.Context;

/** Samples for Table Delete. */
public final class TableDeleteSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/TableOperationDelete.json
     */
    /**
     * Sample code: TableOperationDelete.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void tableOperationDelete(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.tables().deleteWithResponse("res3376", "sto328", "table6185", Context.NONE);
    }
}
```

### Table_Get

```java
import com.azure.core.util.Context;

/** Samples for Table Get. */
public final class TableGetSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/TableOperationGet.json
     */
    /**
     * Sample code: TableOperationGet.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void tableOperationGet(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.tables().getWithResponse("res3376", "sto328", "table6185", Context.NONE);
    }
}
```

### Table_List

```java
import com.azure.core.util.Context;

/** Samples for Table List. */
public final class TableListSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/TableOperationList.json
     */
    /**
     * Sample code: TableOperationList.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void tableOperationList(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.tables().list("res9290", "sto328", Context.NONE);
    }
}
```

### Table_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.generated.models.Table;
import com.azure.resourcemanager.storage.generated.models.TableAccessPolicy;
import com.azure.resourcemanager.storage.generated.models.TableSignedIdentifier;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Samples for Table Update. */
public final class TableUpdateSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/TableOperationPutOrPatchAcls.json
     */
    /**
     * Sample code: TableOperationPutOrPatchAcls.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void tableOperationPutOrPatchAcls(
        com.azure.resourcemanager.storage.generated.StorageManager manager) {
        Table resource = manager.tables().getWithResponse("res3376", "sto328", "table6185", Context.NONE).getValue();
        resource
            .update()
            .withSignedIdentifiers(
                Arrays
                    .asList(
                        new TableSignedIdentifier()
                            .withId("MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI")
                            .withAccessPolicy(
                                new TableAccessPolicy()
                                    .withStartTime(OffsetDateTime.parse("2022-03-17T08:49:37.0000000Z"))
                                    .withExpiryTime(OffsetDateTime.parse("2022-03-20T08:49:37.0000000Z"))
                                    .withPermission("raud")),
                        new TableSignedIdentifier()
                            .withId("PTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODklMTI")
                            .withAccessPolicy(
                                new TableAccessPolicy()
                                    .withStartTime(OffsetDateTime.parse("2022-03-17T08:49:37.0000000Z"))
                                    .withExpiryTime(OffsetDateTime.parse("2022-03-20T08:49:37.0000000Z"))
                                    .withPermission("rad"))))
            .apply();
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/TableOperationPatch.json
     */
    /**
     * Sample code: TableOperationPatch.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void tableOperationPatch(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        Table resource = manager.tables().getWithResponse("res3376", "sto328", "table6185", Context.NONE).getValue();
        resource.update().apply();
    }
}
```

### TableServices_GetServiceProperties

```java
import com.azure.core.util.Context;

/** Samples for TableServices GetServiceProperties. */
public final class TableServicesGetServicePropertiesSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/TableServicesGet.json
     */
    /**
     * Sample code: TableServicesGet.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void tableServicesGet(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.tableServices().getServicePropertiesWithResponse("res4410", "sto8607", Context.NONE);
    }
}
```

### TableServices_List

```java
import com.azure.core.util.Context;

/** Samples for TableServices List. */
public final class TableServicesListSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/TableServicesList.json
     */
    /**
     * Sample code: TableServicesList.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void tableServicesList(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.tableServices().listWithResponse("res9290", "sto1590", Context.NONE);
    }
}
```

### TableServices_SetServiceProperties

```java
import com.azure.resourcemanager.storage.generated.models.CorsRule;
import com.azure.resourcemanager.storage.generated.models.CorsRuleAllowedMethodsItem;
import com.azure.resourcemanager.storage.generated.models.CorsRules;
import java.util.Arrays;

/** Samples for TableServices SetServiceProperties. */
public final class TableServicesSetServicePropertiesSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/TableServicesPut.json
     */
    /**
     * Sample code: TableServicesPut.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void tableServicesPut(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager
            .tableServices()
            .define()
            .withExistingStorageAccount("res4410", "sto8607")
            .withCors(
                new CorsRules()
                    .withCorsRules(
                        Arrays
                            .asList(
                                new CorsRule()
                                    .withAllowedOrigins(
                                        Arrays.asList("http://www.contoso.com", "http://www.fabrikam.com"))
                                    .withAllowedMethods(
                                        Arrays
                                            .asList(
                                                CorsRuleAllowedMethodsItem.GET,
                                                CorsRuleAllowedMethodsItem.HEAD,
                                                CorsRuleAllowedMethodsItem.POST,
                                                CorsRuleAllowedMethodsItem.OPTIONS,
                                                CorsRuleAllowedMethodsItem.MERGE,
                                                CorsRuleAllowedMethodsItem.PUT))
                                    .withMaxAgeInSeconds(100)
                                    .withExposedHeaders(Arrays.asList("x-ms-meta-*"))
                                    .withAllowedHeaders(
                                        Arrays.asList("x-ms-meta-abc", "x-ms-meta-data*", "x-ms-meta-target*")),
                                new CorsRule()
                                    .withAllowedOrigins(Arrays.asList("*"))
                                    .withAllowedMethods(Arrays.asList(CorsRuleAllowedMethodsItem.GET))
                                    .withMaxAgeInSeconds(2)
                                    .withExposedHeaders(Arrays.asList("*"))
                                    .withAllowedHeaders(Arrays.asList("*")),
                                new CorsRule()
                                    .withAllowedOrigins(
                                        Arrays.asList("http://www.abc23.com", "https://www.fabrikam.com/*"))
                                    .withAllowedMethods(
                                        Arrays.asList(CorsRuleAllowedMethodsItem.GET, CorsRuleAllowedMethodsItem.PUT))
                                    .withMaxAgeInSeconds(2000)
                                    .withExposedHeaders(
                                        Arrays.asList("x-ms-meta-abc", "x-ms-meta-data*", "x-ms-meta-target*"))
                                    .withAllowedHeaders(Arrays.asList("x-ms-meta-12345675754564*")))))
            .create();
    }
}
```

### Usages_ListByLocation

```java
import com.azure.core.util.Context;

/** Samples for Usages ListByLocation. */
public final class UsagesListByLocationSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2021-09-01/examples/StorageAccountListLocationUsage.json
     */
    /**
     * Sample code: UsageList.
     *
     * @param manager Entry point to StorageManager.
     */
    public static void usageList(com.azure.resourcemanager.storage.generated.StorageManager manager) {
        manager.usages().listByLocation("eastus2(stage)", Context.NONE);
    }
}
```

