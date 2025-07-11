# Code snippets and samples


## FileSystems

- [CreateOrUpdate](#filesystems_createorupdate)
- [Delete](#filesystems_delete)
- [GetByResourceGroup](#filesystems_getbyresourcegroup)
- [List](#filesystems_list)
- [ListByResourceGroup](#filesystems_listbyresourcegroup)
- [Update](#filesystems_update)

## Operations

- [List](#operations_list)
### FileSystems_CreateOrUpdate

```java
import com.azure.resourcemanager.dell.storage.models.EncryptionIdentityProperties;
import com.azure.resourcemanager.dell.storage.models.EncryptionIdentityType;
import com.azure.resourcemanager.dell.storage.models.EncryptionProperties;
import com.azure.resourcemanager.dell.storage.models.FileSystemResourceProperties;
import com.azure.resourcemanager.dell.storage.models.ManagedServiceIdentity;
import com.azure.resourcemanager.dell.storage.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.dell.storage.models.MarketplaceDetails;
import com.azure.resourcemanager.dell.storage.models.ResourceEncryptionType;
import com.azure.resourcemanager.dell.storage.models.UserAssignedIdentity;
import com.azure.resourcemanager.dell.storage.models.UserDetails;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for FileSystems CreateOrUpdate.
 */
public final class FileSystemsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-03-21-preview/FileSystems_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_CreateOrUpdate_MaximumSet_Gen.
     * 
     * @param manager Entry point to DellStorageManager.
     */
    public static void
        fileSystemsCreateOrUpdateMaximumSetGen(com.azure.resourcemanager.dell.storage.DellStorageManager manager) {
        manager.fileSystems()
            .define("abcd")
            .withRegion("cvbmsqftppe")
            .withExistingResourceGroup("rgDell")
            .withTags(mapOf("key7594", "fakeTokenPlaceholder"))
            .withProperties(new FileSystemResourceProperties()
                .withMarketplace(new MarketplaceDetails().withMarketplaceSubscriptionId("mvjcxwndudbylynme")
                    .withPlanId("eekvwfndjoxijeasksnt")
                    .withOfferId("bcganbkmvznyqfnvhjuag")
                    .withPublisherId("trdzykoeskmcwpo")
                    .withPrivateOfferId("privateOfferId")
                    .withPlanName("planeName"))
                .withDelegatedSubnetId("rqkpvczbtqcxiaivtbuixblb")
                .withDelegatedSubnetCidr("10.0.0.1/24")
                .withUser(new UserDetails().withEmail("jwogfgznmjabdbcjcljjlkxdpc"))
                .withSmartConnectFqdn("fqdn")
                .withOneFsUrl("oneFsUrl")
                .withDellReferenceNumber("fhewkj")
                .withEncryption(new EncryptionProperties()
                    .withEncryptionType(ResourceEncryptionType.CUSTOMER_MANAGED_KEYS_CMK)
                    .withKeyUrl("fakeTokenPlaceholder")
                    .withEncryptionIdentityProperties(new EncryptionIdentityProperties()
                        .withIdentityType(EncryptionIdentityType.USER_ASSIGNED)
                        .withIdentityResourceId(
                            "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{identityName}"))))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf("key7644", new UserAssignedIdentity())))
            .create();
    }

    /*
     * x-ms-original-file: 2025-03-21-preview/FileSystems_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_CreateOrUpdate_MinimumSet_Gen.
     * 
     * @param manager Entry point to DellStorageManager.
     */
    public static void
        fileSystemsCreateOrUpdateMinimumSetGen(com.azure.resourcemanager.dell.storage.DellStorageManager manager) {
        manager.fileSystems()
            .define("abcd")
            .withRegion("tbcvhxzpgrijtdygkttnfswwtacs")
            .withExistingResourceGroup("rgDell")
            .withProperties(
                new FileSystemResourceProperties()
                    .withMarketplace(new MarketplaceDetails().withPlanId("lgozf")
                        .withOfferId("pzhjvibxqgeqkndqnjlduwnxqbr")
                        .withPrivateOfferId("privateOfferId")
                        .withPlanName("planeName"))
                    .withDelegatedSubnetId("yp")
                    .withDelegatedSubnetCidr("10.0.0.1/24")
                    .withUser(new UserDetails().withEmail("hoznewwtzmyjzctzosfuh"))
                    .withDellReferenceNumber("fhewkj")
                    .withEncryption(new EncryptionProperties()
                        .withEncryptionType(ResourceEncryptionType.MICROSOFT_MANAGED_KEYS_MMK)))
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

### FileSystems_Delete

```java
/**
 * Samples for FileSystems Delete.
 */
public final class FileSystemsDeleteSamples {
    /*
     * x-ms-original-file: 2025-03-21-preview/FileSystems_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to DellStorageManager.
     */
    public static void
        fileSystemsDeleteMaximumSetGen(com.azure.resourcemanager.dell.storage.DellStorageManager manager) {
        manager.fileSystems().delete("rgDell", "abcd", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-03-21-preview/FileSystems_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_Delete_MinimumSet_Gen.
     * 
     * @param manager Entry point to DellStorageManager.
     */
    public static void
        fileSystemsDeleteMinimumSetGen(com.azure.resourcemanager.dell.storage.DellStorageManager manager) {
        manager.fileSystems().delete("rgDell", "abcd", com.azure.core.util.Context.NONE);
    }
}
```

### FileSystems_GetByResourceGroup

```java
/**
 * Samples for FileSystems GetByResourceGroup.
 */
public final class FileSystemsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-03-21-preview/FileSystems_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to DellStorageManager.
     */
    public static void fileSystemsGetMaximumSetGen(com.azure.resourcemanager.dell.storage.DellStorageManager manager) {
        manager.fileSystems().getByResourceGroupWithResponse("rgDell", "abcd", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-03-21-preview/FileSystems_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to DellStorageManager.
     */
    public static void fileSystemsGetMinimumSetGen(com.azure.resourcemanager.dell.storage.DellStorageManager manager) {
        manager.fileSystems().getByResourceGroupWithResponse("rgDell", "abcd", com.azure.core.util.Context.NONE);
    }
}
```

### FileSystems_List

```java
/**
 * Samples for FileSystems List.
 */
public final class FileSystemsListSamples {
    /*
     * x-ms-original-file: 2025-03-21-preview/FileSystems_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_ListBySubscription_MinimumSet_Gen.
     * 
     * @param manager Entry point to DellStorageManager.
     */
    public static void
        fileSystemsListBySubscriptionMinimumSetGen(com.azure.resourcemanager.dell.storage.DellStorageManager manager) {
        manager.fileSystems().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-03-21-preview/FileSystems_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_ListBySubscription_MaximumSet_Gen.
     * 
     * @param manager Entry point to DellStorageManager.
     */
    public static void
        fileSystemsListBySubscriptionMaximumSetGen(com.azure.resourcemanager.dell.storage.DellStorageManager manager) {
        manager.fileSystems().list(com.azure.core.util.Context.NONE);
    }
}
```

### FileSystems_ListByResourceGroup

```java
/**
 * Samples for FileSystems ListByResourceGroup.
 */
public final class FileSystemsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-03-21-preview/FileSystems_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_ListByResourceGroup_MaximumSet_Gen.
     * 
     * @param manager Entry point to DellStorageManager.
     */
    public static void
        fileSystemsListByResourceGroupMaximumSetGen(com.azure.resourcemanager.dell.storage.DellStorageManager manager) {
        manager.fileSystems().listByResourceGroup("rgDell", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-03-21-preview/FileSystems_ListByResourceGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_ListByResourceGroup_MinimumSet_Gen.
     * 
     * @param manager Entry point to DellStorageManager.
     */
    public static void
        fileSystemsListByResourceGroupMinimumSetGen(com.azure.resourcemanager.dell.storage.DellStorageManager manager) {
        manager.fileSystems().listByResourceGroup("rgDell", com.azure.core.util.Context.NONE);
    }
}
```

### FileSystems_Update

```java
import com.azure.resourcemanager.dell.storage.models.Capacity;
import com.azure.resourcemanager.dell.storage.models.EncryptionIdentityType;
import com.azure.resourcemanager.dell.storage.models.EncryptionIdentityUpdateProperties;
import com.azure.resourcemanager.dell.storage.models.EncryptionUpdateProperties;
import com.azure.resourcemanager.dell.storage.models.FileSystemResource;
import com.azure.resourcemanager.dell.storage.models.FileSystemResourceUpdateProperties;
import com.azure.resourcemanager.dell.storage.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.dell.storage.models.ManagedServiceIdentityUpdate;
import com.azure.resourcemanager.dell.storage.models.ResourceEncryptionType;
import com.azure.resourcemanager.dell.storage.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for FileSystems Update.
 */
public final class FileSystemsUpdateSamples {
    /*
     * x-ms-original-file: 2025-03-21-preview/FileSystems_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_Update_MinimumSet_Gen.
     * 
     * @param manager Entry point to DellStorageManager.
     */
    public static void
        fileSystemsUpdateMinimumSetGen(com.azure.resourcemanager.dell.storage.DellStorageManager manager) {
        FileSystemResource resource = manager.fileSystems()
            .getByResourceGroupWithResponse("rgDell", "abcd", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(
                new FileSystemResourceUpdateProperties().withDelegatedSubnetId("uqfvajvyltgmqvdnxhbrfqbpuey")
                    .withCapacity(new Capacity().withCurrent("5")))
            .apply();
    }

    /*
     * x-ms-original-file: 2025-03-21-preview/FileSystems_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_Update_MaximumSet_Gen.
     * 
     * @param manager Entry point to DellStorageManager.
     */
    public static void
        fileSystemsUpdateMaximumSetGen(com.azure.resourcemanager.dell.storage.DellStorageManager manager) {
        FileSystemResource resource = manager.fileSystems()
            .getByResourceGroupWithResponse("rgDell", "abcd", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key6099", "fakeTokenPlaceholder"))
            .withIdentity(
                new ManagedServiceIdentityUpdate().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                    .withUserAssignedIdentities(mapOf("key7645", new UserAssignedIdentity())))
            .withProperties(new FileSystemResourceUpdateProperties().withDelegatedSubnetId("bfpuabdz")
                .withCapacity(new Capacity().withCurrent("5"))
                .withEncryption(new EncryptionUpdateProperties()
                    .withEncryptionType(ResourceEncryptionType.CUSTOMER_MANAGED_KEYS_CMK)
                    .withKeyUrl("fakeTokenPlaceholder")
                    .withEncryptionIdentityProperties(new EncryptionIdentityUpdateProperties()
                        .withIdentityType(EncryptionIdentityType.USER_ASSIGNED)
                        .withIdentityResourceId(
                            "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{identityName}"))))
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

### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2025-03-21-preview/Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MinimumSet_Gen.
     * 
     * @param manager Entry point to DellStorageManager.
     */
    public static void operationsListMinimumSetGen(com.azure.resourcemanager.dell.storage.DellStorageManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-03-21-preview/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MaximumSet_Gen.
     * 
     * @param manager Entry point to DellStorageManager.
     */
    public static void operationsListMaximumSetGen(com.azure.resourcemanager.dell.storage.DellStorageManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

