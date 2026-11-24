# Code snippets and samples


## Operations

- [List](#operations_list)

## Partitions

- [CreateOrUpdate](#partitions_createorupdate)
- [Delete](#partitions_delete)
- [Get](#partitions_get)
- [ListByParent](#partitions_listbyparent)
- [Update](#partitions_update)

## ZkpRollups

- [CreateOrUpdate](#zkprollups_createorupdate)
- [Delete](#zkprollups_delete)
- [GetByResourceGroup](#zkprollups_getbyresourcegroup)
- [List](#zkprollups_list)
- [ListByResourceGroup](#zkprollups_listbyresourcegroup)
- [Update](#zkprollups_update)
### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/Operations_List.json
     */
    /**
     * Sample code: ListOperations.
     * 
     * @param manager Entry point to DigitalAssetsManager.
     */
    public static void listOperations(com.azure.resourcemanager.digitalassets.DigitalAssetsManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Partitions_CreateOrUpdate

```java
/**
 * Samples for Partitions CreateOrUpdate.
 */
public final class PartitionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/Partitions_CreateOrUpdate.json
     */
    /**
     * Sample code: CreatePartition.
     * 
     * @param manager Entry point to DigitalAssetsManager.
     */
    public static void createPartition(com.azure.resourcemanager.digitalassets.DigitalAssetsManager manager) {
        manager.partitions().define("Partition1").withRegion("eastus").withExistingZkpRollup("testrg", "Jack").create();
    }
}
```

### Partitions_Delete

```java
/**
 * Samples for Partitions Delete.
 */
public final class PartitionsDeleteSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/Partitions_Delete.json
     */
    /**
     * Sample code: DeletePartition.
     * 
     * @param manager Entry point to DigitalAssetsManager.
     */
    public static void deletePartition(com.azure.resourcemanager.digitalassets.DigitalAssetsManager manager) {
        manager.partitions().delete("testrg", "Jack", "Partition1", com.azure.core.util.Context.NONE);
    }
}
```

### Partitions_Get

```java
/**
 * Samples for Partitions Get.
 */
public final class PartitionsGetSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/Partitions_Get.json
     */
    /**
     * Sample code: GetPartition.
     * 
     * @param manager Entry point to DigitalAssetsManager.
     */
    public static void getPartition(com.azure.resourcemanager.digitalassets.DigitalAssetsManager manager) {
        manager.partitions().getWithResponse("testrg", "Jack", "Partition1", com.azure.core.util.Context.NONE);
    }
}
```

### Partitions_ListByParent

```java
/**
 * Samples for Partitions ListByParent.
 */
public final class PartitionsListByParentSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/Partitions_ListByParent.json
     */
    /**
     * Sample code: ListPartitionsByParent.
     * 
     * @param manager Entry point to DigitalAssetsManager.
     */
    public static void listPartitionsByParent(com.azure.resourcemanager.digitalassets.DigitalAssetsManager manager) {
        manager.partitions().listByParent("testrg", "Jack", com.azure.core.util.Context.NONE);
    }
}
```

### Partitions_Update

```java
import com.azure.resourcemanager.digitalassets.models.Partition;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Partitions Update.
 */
public final class PartitionsUpdateSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/Partitions_Update.json
     */
    /**
     * Sample code: UpdatePartition.
     * 
     * @param manager Entry point to DigitalAssetsManager.
     */
    public static void updatePartition(com.azure.resourcemanager.digitalassets.DigitalAssetsManager manager) {
        Partition resource = manager.partitions()
            .getWithResponse("testrg", "Jack", "Partition1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("env", "test")).apply();
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

### ZkpRollups_CreateOrUpdate

```java
import com.azure.resourcemanager.digitalassets.models.ManagedServiceIdentity;
import com.azure.resourcemanager.digitalassets.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.digitalassets.models.UserAssignedIdentity;
import com.azure.resourcemanager.digitalassets.models.ZkpRollupProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ZkpRollups CreateOrUpdate.
 */
public final class ZkpRollupsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/ZkpRollups_CreateOrUpdate.json
     */
    /**
     * Sample code: CreateZkpRollup.
     * 
     * @param manager Entry point to DigitalAssetsManager.
     */
    public static void createZkpRollup(com.azure.resourcemanager.digitalassets.DigitalAssetsManager manager) {
        manager.zkpRollups()
            .define("Jack")
            .withRegion("eastus")
            .withExistingResourceGroup("testrg")
            .withProperties(new ZkpRollupProperties()
                .withTokenOwnerAccounts(Arrays.asList("0x1111111111111111111111111111111111111111",
                    "0x2222222222222222222222222222222222222222"))
                .withKeyVaultId("fakeTokenPlaceholder")
                .withKeyVaultUserAssignedIdentityResourceId("fakeTokenPlaceholder")
                .withCustomerVpnPublicIp("203.0.113.10")
                .withCustomerOnPremAddressPrefixes(Arrays.asList("192.168.100.0/24"))
                .withBootNodeId(
                    "79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8")
                .withBootNodeIp("10.0.0.4")
                .withBootNodeP2pPort(30303)
                .withBootNodeRpcPort(8545)
                .withServiceBusEndpoint("https://sb-da-customer.servicebus.windows.net")
                .withServiceBusQueueName("proofs-output"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/kvReader",
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

### ZkpRollups_Delete

```java
/**
 * Samples for ZkpRollups Delete.
 */
public final class ZkpRollupsDeleteSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/ZkpRollups_Delete.json
     */
    /**
     * Sample code: DeleteZkpRollup.
     * 
     * @param manager Entry point to DigitalAssetsManager.
     */
    public static void deleteZkpRollup(com.azure.resourcemanager.digitalassets.DigitalAssetsManager manager) {
        manager.zkpRollups().delete("testrg", "Jack", com.azure.core.util.Context.NONE);
    }
}
```

### ZkpRollups_GetByResourceGroup

```java
/**
 * Samples for ZkpRollups GetByResourceGroup.
 */
public final class ZkpRollupsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/ZkpRollups_Get.json
     */
    /**
     * Sample code: GetZkpRollup.
     * 
     * @param manager Entry point to DigitalAssetsManager.
     */
    public static void getZkpRollup(com.azure.resourcemanager.digitalassets.DigitalAssetsManager manager) {
        manager.zkpRollups().getByResourceGroupWithResponse("testrg", "Jack", com.azure.core.util.Context.NONE);
    }
}
```

### ZkpRollups_List

```java
/**
 * Samples for ZkpRollups List.
 */
public final class ZkpRollupsListSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/ZkpRollups_ListBySubscription.json
     */
    /**
     * Sample code: ListZkpRollupsBySubscription.
     * 
     * @param manager Entry point to DigitalAssetsManager.
     */
    public static void
        listZkpRollupsBySubscription(com.azure.resourcemanager.digitalassets.DigitalAssetsManager manager) {
        manager.zkpRollups().list(com.azure.core.util.Context.NONE);
    }
}
```

### ZkpRollups_ListByResourceGroup

```java
/**
 * Samples for ZkpRollups ListByResourceGroup.
 */
public final class ZkpRollupsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/ZkpRollups_ListByResourceGroup.json
     */
    /**
     * Sample code: ListZkpRollupsByResourceGroup.
     * 
     * @param manager Entry point to DigitalAssetsManager.
     */
    public static void
        listZkpRollupsByResourceGroup(com.azure.resourcemanager.digitalassets.DigitalAssetsManager manager) {
        manager.zkpRollups().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### ZkpRollups_Update

```java
import com.azure.resourcemanager.digitalassets.models.ZkpRollup;
import com.azure.resourcemanager.digitalassets.models.ZkpRollupIdentityUpdate;
import com.azure.resourcemanager.digitalassets.models.ZkpRollupPatchProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ZkpRollups Update.
 */
public final class ZkpRollupsUpdateSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/ZkpRollups_Update.json
     */
    /**
     * Sample code: UpdateZkpRollupTagsIdentityAndBootNode.
     * 
     * @param manager Entry point to DigitalAssetsManager.
     */
    public static void
        updateZkpRollupTagsIdentityAndBootNode(com.azure.resourcemanager.digitalassets.DigitalAssetsManager manager) {
        ZkpRollup resource = manager.zkpRollups()
            .getByResourceGroupWithResponse("testrg", "Jack", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("env", "test"))
            .withIdentity(new ZkpRollupIdentityUpdate().withUserAssignedIdentities(mapOf(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/additionalReader",
                null)))
            .withProperties(new ZkpRollupPatchProperties().withBootNodeIp("10.0.0.4"))
            .apply();
    }

    /*
     * x-ms-original-file: 2025-09-01-preview/ZkpRollups_UpdateConfiguration.json
     */
    /**
     * Sample code: UpdateZkpRollupConfiguration.
     * 
     * @param manager Entry point to DigitalAssetsManager.
     */
    public static void
        updateZkpRollupConfiguration(com.azure.resourcemanager.digitalassets.DigitalAssetsManager manager) {
        ZkpRollup resource = manager.zkpRollups()
            .getByResourceGroupWithResponse("testrg", "Jack", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new ZkpRollupPatchProperties()
                .withTokenOwnerAccounts(Arrays.asList("0x2222222222222222222222222222222222222222"))
                .withBootNodeId(
                    "79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8")
                .withBootNodeIp("10.0.0.5")
                .withBootNodeP2pPort(30304)
                .withBootNodeRpcPort(8546))
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

