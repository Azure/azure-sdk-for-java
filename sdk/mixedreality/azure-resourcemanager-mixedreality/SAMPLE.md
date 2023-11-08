# Code snippets and samples


## Operations

- [List](#operations_list)

## RemoteRenderingAccounts

- [Create](#remoterenderingaccounts_create)
- [Delete](#remoterenderingaccounts_delete)
- [GetByResourceGroup](#remoterenderingaccounts_getbyresourcegroup)
- [List](#remoterenderingaccounts_list)
- [ListByResourceGroup](#remoterenderingaccounts_listbyresourcegroup)
- [ListKeys](#remoterenderingaccounts_listkeys)
- [RegenerateKeys](#remoterenderingaccounts_regeneratekeys)
- [Update](#remoterenderingaccounts_update)

## ResourceProvider

- [CheckNameAvailabilityLocal](#resourceprovider_checknameavailabilitylocal)

## SpatialAnchorsAccounts

- [Create](#spatialanchorsaccounts_create)
- [Delete](#spatialanchorsaccounts_delete)
- [GetByResourceGroup](#spatialanchorsaccounts_getbyresourcegroup)
- [List](#spatialanchorsaccounts_list)
- [ListByResourceGroup](#spatialanchorsaccounts_listbyresourcegroup)
- [ListKeys](#spatialanchorsaccounts_listkeys)
- [RegenerateKeys](#spatialanchorsaccounts_regeneratekeys)
- [Update](#spatialanchorsaccounts_update)
### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/proxy/ExposingAvailableOperations.json
     */
    /**
     * Sample code: List available operations.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void listAvailableOperations(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### RemoteRenderingAccounts_Create

```java
import com.azure.resourcemanager.mixedreality.models.Identity;
import com.azure.resourcemanager.mixedreality.models.ResourceIdentityType;

/** Samples for RemoteRenderingAccounts Create. */
public final class RemoteRenderingAccountsCreateSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/remote-rendering/Put.json
     */
    /**
     * Sample code: Create remote rendering account.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void createRemoteRenderingAccount(
        com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager
            .remoteRenderingAccounts()
            .define("MyAccount")
            .withRegion("eastus2euap")
            .withExistingResourceGroup("MyResourceGroup")
            .withIdentity(new Identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
            .create();
    }
}
```

### RemoteRenderingAccounts_Delete

```java
/** Samples for RemoteRenderingAccounts Delete. */
public final class RemoteRenderingAccountsDeleteSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/remote-rendering/Delete.json
     */
    /**
     * Sample code: Delete remote rendering account.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void deleteRemoteRenderingAccount(
        com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager
            .remoteRenderingAccounts()
            .deleteByResourceGroupWithResponse("MyResourceGroup", "MyAccount", com.azure.core.util.Context.NONE);
    }
}
```

### RemoteRenderingAccounts_GetByResourceGroup

```java
/** Samples for RemoteRenderingAccounts GetByResourceGroup. */
public final class RemoteRenderingAccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/remote-rendering/Get.json
     */
    /**
     * Sample code: Get remote rendering account.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void getRemoteRenderingAccount(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager
            .remoteRenderingAccounts()
            .getByResourceGroupWithResponse("MyResourceGroup", "MyAccount", com.azure.core.util.Context.NONE);
    }
}
```

### RemoteRenderingAccounts_List

```java
/** Samples for RemoteRenderingAccounts List. */
public final class RemoteRenderingAccountsListSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/remote-rendering/GetBySubscription.json
     */
    /**
     * Sample code: List remote rendering accounts by subscription.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void listRemoteRenderingAccountsBySubscription(
        com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.remoteRenderingAccounts().list(com.azure.core.util.Context.NONE);
    }
}
```

### RemoteRenderingAccounts_ListByResourceGroup

```java
/** Samples for RemoteRenderingAccounts ListByResourceGroup. */
public final class RemoteRenderingAccountsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/remote-rendering/GetByResourceGroup.json
     */
    /**
     * Sample code: List remote rendering accounts by resource group.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void listRemoteRenderingAccountsByResourceGroup(
        com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.remoteRenderingAccounts().listByResourceGroup("MyResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### RemoteRenderingAccounts_ListKeys

```java
/** Samples for RemoteRenderingAccounts ListKeys. */
public final class RemoteRenderingAccountsListKeysSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/remote-rendering/ListKeys.json
     */
    /**
     * Sample code: List remote rendering account key.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void listRemoteRenderingAccountKey(
        com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager
            .remoteRenderingAccounts()
            .listKeysWithResponse("MyResourceGroup", "MyAccount", com.azure.core.util.Context.NONE);
    }
}
```

### RemoteRenderingAccounts_RegenerateKeys

```java
import com.azure.resourcemanager.mixedreality.models.AccountKeyRegenerateRequest;
import com.azure.resourcemanager.mixedreality.models.Serial;

/** Samples for RemoteRenderingAccounts RegenerateKeys. */
public final class RemoteRenderingAccountsRegenerateKeysSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/remote-rendering/RegenerateKey.json
     */
    /**
     * Sample code: Regenerate remote rendering account keys.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void regenerateRemoteRenderingAccountKeys(
        com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager
            .remoteRenderingAccounts()
            .regenerateKeysWithResponse(
                "MyResourceGroup",
                "MyAccount",
                new AccountKeyRegenerateRequest().withSerial(Serial.ONE),
                com.azure.core.util.Context.NONE);
    }
}
```

### RemoteRenderingAccounts_Update

```java
import com.azure.resourcemanager.mixedreality.models.Identity;
import com.azure.resourcemanager.mixedreality.models.RemoteRenderingAccount;
import com.azure.resourcemanager.mixedreality.models.ResourceIdentityType;
import java.util.HashMap;
import java.util.Map;

/** Samples for RemoteRenderingAccounts Update. */
public final class RemoteRenderingAccountsUpdateSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/remote-rendering/Patch.json
     */
    /**
     * Sample code: Update remote rendering account.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void updateRemoteRenderingAccount(
        com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        RemoteRenderingAccount resource =
            manager
                .remoteRenderingAccounts()
                .getByResourceGroupWithResponse("MyResourceGroup", "MyAccount", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("hero", "romeo", "heroine", "juliet"))
            .withIdentity(new Identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
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

### ResourceProvider_CheckNameAvailabilityLocal

```java
import com.azure.resourcemanager.mixedreality.models.CheckNameAvailabilityRequest;

/** Samples for ResourceProvider CheckNameAvailabilityLocal. */
public final class ResourceProviderCheckNameAvailabilityLocalSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/proxy/CheckNameAvailabilityForLocalUniqueness.json
     */
    /**
     * Sample code: CheckLocalNameAvailability.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void checkLocalNameAvailability(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager
            .resourceProviders()
            .checkNameAvailabilityLocalWithResponse(
                "eastus2euap",
                new CheckNameAvailabilityRequest()
                    .withName("MyAccount")
                    .withType("Microsoft.MixedReality/spatialAnchorsAccounts"),
                com.azure.core.util.Context.NONE);
    }
}
```

### SpatialAnchorsAccounts_Create

```java
/** Samples for SpatialAnchorsAccounts Create. */
public final class SpatialAnchorsAccountsCreateSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/spatial-anchors/Put.json
     */
    /**
     * Sample code: Create spatial anchor account.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void createSpatialAnchorAccount(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager
            .spatialAnchorsAccounts()
            .define("MyAccount")
            .withRegion("eastus2euap")
            .withExistingResourceGroup("MyResourceGroup")
            .create();
    }
}
```

### SpatialAnchorsAccounts_Delete

```java
/** Samples for SpatialAnchorsAccounts Delete. */
public final class SpatialAnchorsAccountsDeleteSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/spatial-anchors/Delete.json
     */
    /**
     * Sample code: Delete spatial anchors account.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void deleteSpatialAnchorsAccount(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager
            .spatialAnchorsAccounts()
            .deleteByResourceGroupWithResponse("MyResourceGroup", "MyAccount", com.azure.core.util.Context.NONE);
    }
}
```

### SpatialAnchorsAccounts_GetByResourceGroup

```java
/** Samples for SpatialAnchorsAccounts GetByResourceGroup. */
public final class SpatialAnchorsAccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/spatial-anchors/Get.json
     */
    /**
     * Sample code: Get spatial anchors account.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void getSpatialAnchorsAccount(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager
            .spatialAnchorsAccounts()
            .getByResourceGroupWithResponse("MyResourceGroup", "MyAccount", com.azure.core.util.Context.NONE);
    }
}
```

### SpatialAnchorsAccounts_List

```java
/** Samples for SpatialAnchorsAccounts List. */
public final class SpatialAnchorsAccountsListSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/spatial-anchors/GetBySubscription.json
     */
    /**
     * Sample code: List spatial anchors accounts by subscription.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void listSpatialAnchorsAccountsBySubscription(
        com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.spatialAnchorsAccounts().list(com.azure.core.util.Context.NONE);
    }
}
```

### SpatialAnchorsAccounts_ListByResourceGroup

```java
/** Samples for SpatialAnchorsAccounts ListByResourceGroup. */
public final class SpatialAnchorsAccountsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/spatial-anchors/GetByResourceGroup.json
     */
    /**
     * Sample code: List spatial anchor accounts by resource group.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void listSpatialAnchorAccountsByResourceGroup(
        com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.spatialAnchorsAccounts().listByResourceGroup("MyResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### SpatialAnchorsAccounts_ListKeys

```java
/** Samples for SpatialAnchorsAccounts ListKeys. */
public final class SpatialAnchorsAccountsListKeysSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/spatial-anchors/ListKeys.json
     */
    /**
     * Sample code: List spatial anchor account key.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void listSpatialAnchorAccountKey(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager
            .spatialAnchorsAccounts()
            .listKeysWithResponse("MyResourceGroup", "MyAccount", com.azure.core.util.Context.NONE);
    }
}
```

### SpatialAnchorsAccounts_RegenerateKeys

```java
import com.azure.resourcemanager.mixedreality.models.AccountKeyRegenerateRequest;
import com.azure.resourcemanager.mixedreality.models.Serial;

/** Samples for SpatialAnchorsAccounts RegenerateKeys. */
public final class SpatialAnchorsAccountsRegenerateKeysSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/spatial-anchors/RegenerateKey.json
     */
    /**
     * Sample code: Regenerate spatial anchors account keys.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void regenerateSpatialAnchorsAccountKeys(
        com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager
            .spatialAnchorsAccounts()
            .regenerateKeysWithResponse(
                "MyResourceGroup",
                "MyAccount",
                new AccountKeyRegenerateRequest().withSerial(Serial.ONE),
                com.azure.core.util.Context.NONE);
    }
}
```

### SpatialAnchorsAccounts_Update

```java
import com.azure.resourcemanager.mixedreality.models.SpatialAnchorsAccount;
import java.util.HashMap;
import java.util.Map;

/** Samples for SpatialAnchorsAccounts Update. */
public final class SpatialAnchorsAccountsUpdateSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/spatial-anchors/Patch.json
     */
    /**
     * Sample code: Update spatial anchors account.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void updateSpatialAnchorsAccount(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        SpatialAnchorsAccount resource =
            manager
                .spatialAnchorsAccounts()
                .getByResourceGroupWithResponse("MyResourceGroup", "MyAccount", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("hero", "romeo", "heroine", "juliet")).apply();
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

