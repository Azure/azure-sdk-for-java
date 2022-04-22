# Code snippets and samples


## ObjectAnchorsAccounts

- [Create](#objectanchorsaccounts_create)
- [Delete](#objectanchorsaccounts_delete)
- [GetByResourceGroup](#objectanchorsaccounts_getbyresourcegroup)
- [List](#objectanchorsaccounts_list)
- [ListByResourceGroup](#objectanchorsaccounts_listbyresourcegroup)
- [ListKeys](#objectanchorsaccounts_listkeys)
- [RegenerateKeys](#objectanchorsaccounts_regeneratekeys)
- [Update](#objectanchorsaccounts_update)

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
### ObjectAnchorsAccounts_Create

```java
import com.azure.resourcemanager.mixedreality.models.ObjectAnchorsAccountIdentity;
import com.azure.resourcemanager.mixedreality.models.ResourceIdentityType;

/** Samples for ObjectAnchorsAccounts Create. */
public final class ObjectAnchorsAccountsCreateSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/object-anchors/Put.json
     */
    /**
     * Sample code: Create object anchors account.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void createObjectAnchorsAccount(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager
            .objectAnchorsAccounts()
            .define("MyAccount")
            .withRegion("eastus2euap")
            .withExistingResourceGroup("MyResourceGroup")
            .withIdentity(new ObjectAnchorsAccountIdentity().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
            .create();
    }
}
```

### ObjectAnchorsAccounts_Delete

```java
import com.azure.core.util.Context;

/** Samples for ObjectAnchorsAccounts Delete. */
public final class ObjectAnchorsAccountsDeleteSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/object-anchors/Delete.json
     */
    /**
     * Sample code: Delete object anchors account.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void deleteObjectAnchorsAccount(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.objectAnchorsAccounts().deleteWithResponse("MyResourceGroup", "MyAccount", Context.NONE);
    }
}
```

### ObjectAnchorsAccounts_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ObjectAnchorsAccounts GetByResourceGroup. */
public final class ObjectAnchorsAccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/object-anchors/Get.json
     */
    /**
     * Sample code: Get object anchors account.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void getObjectAnchorsAccount(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.objectAnchorsAccounts().getByResourceGroupWithResponse("MyResourceGroup", "MyAccount", Context.NONE);
    }
}
```

### ObjectAnchorsAccounts_List

```java
import com.azure.core.util.Context;

/** Samples for ObjectAnchorsAccounts List. */
public final class ObjectAnchorsAccountsListSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/object-anchors/GetBySubscription.json
     */
    /**
     * Sample code: List object anchors accounts by subscription.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void listObjectAnchorsAccountsBySubscription(
        com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.objectAnchorsAccounts().list(Context.NONE);
    }
}
```

### ObjectAnchorsAccounts_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ObjectAnchorsAccounts ListByResourceGroup. */
public final class ObjectAnchorsAccountsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/object-anchors/GetByResourceGroup.json
     */
    /**
     * Sample code: List object anchors accounts by resource group.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void listObjectAnchorsAccountsByResourceGroup(
        com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.objectAnchorsAccounts().listByResourceGroup("MyResourceGroup", Context.NONE);
    }
}
```

### ObjectAnchorsAccounts_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for ObjectAnchorsAccounts ListKeys. */
public final class ObjectAnchorsAccountsListKeysSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/object-anchors/ListKeys.json
     */
    /**
     * Sample code: List object anchors account key.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void listObjectAnchorsAccountKey(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.objectAnchorsAccounts().listKeysWithResponse("MyResourceGroup", "MyAccount", Context.NONE);
    }
}
```

### ObjectAnchorsAccounts_RegenerateKeys

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mixedreality.models.AccountKeyRegenerateRequest;
import com.azure.resourcemanager.mixedreality.models.Serial;

/** Samples for ObjectAnchorsAccounts RegenerateKeys. */
public final class ObjectAnchorsAccountsRegenerateKeysSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/object-anchors/RegenerateKey.json
     */
    /**
     * Sample code: Regenerate object anchors account keys.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void regenerateObjectAnchorsAccountKeys(
        com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager
            .objectAnchorsAccounts()
            .regenerateKeysWithResponse(
                "MyResourceGroup", "MyAccount", new AccountKeyRegenerateRequest().withSerial(Serial.ONE), Context.NONE);
    }
}
```

### ObjectAnchorsAccounts_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mixedreality.models.ObjectAnchorsAccount;
import com.azure.resourcemanager.mixedreality.models.ObjectAnchorsAccountIdentity;
import com.azure.resourcemanager.mixedreality.models.ResourceIdentityType;
import java.util.HashMap;
import java.util.Map;

/** Samples for ObjectAnchorsAccounts Update. */
public final class ObjectAnchorsAccountsUpdateSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/object-anchors/Patch.json
     */
    /**
     * Sample code: Update object anchors account.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void updateObjectAnchorsAccount(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        ObjectAnchorsAccount resource =
            manager
                .objectAnchorsAccounts()
                .getByResourceGroupWithResponse("MyResourceGroup", "MyAccount", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("hero", "romeo", "heroine", "juliet"))
            .withIdentity(new ObjectAnchorsAccountIdentity().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
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

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/proxy/ExposingAvailableOperations.json
     */
    /**
     * Sample code: List available operations.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void listAvailableOperations(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.operations().list(Context.NONE);
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
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/remote-rendering/Put.json
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
import com.azure.core.util.Context;

/** Samples for RemoteRenderingAccounts Delete. */
public final class RemoteRenderingAccountsDeleteSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/remote-rendering/Delete.json
     */
    /**
     * Sample code: Delete remote rendering account.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void deleteRemoteRenderingAccount(
        com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.remoteRenderingAccounts().deleteWithResponse("MyResourceGroup", "MyAccount", Context.NONE);
    }
}
```

### RemoteRenderingAccounts_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for RemoteRenderingAccounts GetByResourceGroup. */
public final class RemoteRenderingAccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/remote-rendering/Get.json
     */
    /**
     * Sample code: Get remote rendering account.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void getRemoteRenderingAccount(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.remoteRenderingAccounts().getByResourceGroupWithResponse("MyResourceGroup", "MyAccount", Context.NONE);
    }
}
```

### RemoteRenderingAccounts_List

```java
import com.azure.core.util.Context;

/** Samples for RemoteRenderingAccounts List. */
public final class RemoteRenderingAccountsListSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/remote-rendering/GetBySubscription.json
     */
    /**
     * Sample code: List remote rendering accounts by subscription.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void listRemoteRenderingAccountsBySubscription(
        com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.remoteRenderingAccounts().list(Context.NONE);
    }
}
```

### RemoteRenderingAccounts_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for RemoteRenderingAccounts ListByResourceGroup. */
public final class RemoteRenderingAccountsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/remote-rendering/GetByResourceGroup.json
     */
    /**
     * Sample code: List remote rendering accounts by resource group.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void listRemoteRenderingAccountsByResourceGroup(
        com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.remoteRenderingAccounts().listByResourceGroup("MyResourceGroup", Context.NONE);
    }
}
```

### RemoteRenderingAccounts_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for RemoteRenderingAccounts ListKeys. */
public final class RemoteRenderingAccountsListKeysSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/remote-rendering/ListKeys.json
     */
    /**
     * Sample code: List remote rendering account key.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void listRemoteRenderingAccountKey(
        com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.remoteRenderingAccounts().listKeysWithResponse("MyResourceGroup", "MyAccount", Context.NONE);
    }
}
```

### RemoteRenderingAccounts_RegenerateKeys

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mixedreality.models.AccountKeyRegenerateRequest;
import com.azure.resourcemanager.mixedreality.models.Serial;

/** Samples for RemoteRenderingAccounts RegenerateKeys. */
public final class RemoteRenderingAccountsRegenerateKeysSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/remote-rendering/RegenerateKey.json
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
                "MyResourceGroup", "MyAccount", new AccountKeyRegenerateRequest().withSerial(Serial.ONE), Context.NONE);
    }
}
```

### RemoteRenderingAccounts_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mixedreality.models.Identity;
import com.azure.resourcemanager.mixedreality.models.RemoteRenderingAccount;
import com.azure.resourcemanager.mixedreality.models.ResourceIdentityType;
import java.util.HashMap;
import java.util.Map;

/** Samples for RemoteRenderingAccounts Update. */
public final class RemoteRenderingAccountsUpdateSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/remote-rendering/Patch.json
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
                .getByResourceGroupWithResponse("MyResourceGroup", "MyAccount", Context.NONE)
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
import com.azure.core.util.Context;
import com.azure.resourcemanager.mixedreality.models.CheckNameAvailabilityRequest;

/** Samples for ResourceProvider CheckNameAvailabilityLocal. */
public final class ResourceProviderCheckNameAvailabilityLocalSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/proxy/CheckNameAvailabilityForLocalUniqueness.json
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
                Context.NONE);
    }
}
```

### SpatialAnchorsAccounts_Create

```java
/** Samples for SpatialAnchorsAccounts Create. */
public final class SpatialAnchorsAccountsCreateSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/spatial-anchors/Put.json
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
import com.azure.core.util.Context;

/** Samples for SpatialAnchorsAccounts Delete. */
public final class SpatialAnchorsAccountsDeleteSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/spatial-anchors/Delete.json
     */
    /**
     * Sample code: Delete spatial anchors account.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void deleteSpatialAnchorsAccount(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.spatialAnchorsAccounts().deleteWithResponse("MyResourceGroup", "MyAccount", Context.NONE);
    }
}
```

### SpatialAnchorsAccounts_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for SpatialAnchorsAccounts GetByResourceGroup. */
public final class SpatialAnchorsAccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/spatial-anchors/Get.json
     */
    /**
     * Sample code: Get spatial anchors account.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void getSpatialAnchorsAccount(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.spatialAnchorsAccounts().getByResourceGroupWithResponse("MyResourceGroup", "MyAccount", Context.NONE);
    }
}
```

### SpatialAnchorsAccounts_List

```java
import com.azure.core.util.Context;

/** Samples for SpatialAnchorsAccounts List. */
public final class SpatialAnchorsAccountsListSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/spatial-anchors/GetBySubscription.json
     */
    /**
     * Sample code: List spatial anchors accounts by subscription.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void listSpatialAnchorsAccountsBySubscription(
        com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.spatialAnchorsAccounts().list(Context.NONE);
    }
}
```

### SpatialAnchorsAccounts_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for SpatialAnchorsAccounts ListByResourceGroup. */
public final class SpatialAnchorsAccountsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/spatial-anchors/GetByResourceGroup.json
     */
    /**
     * Sample code: List spatial anchor accounts by resource group.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void listSpatialAnchorAccountsByResourceGroup(
        com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.spatialAnchorsAccounts().listByResourceGroup("MyResourceGroup", Context.NONE);
    }
}
```

### SpatialAnchorsAccounts_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for SpatialAnchorsAccounts ListKeys. */
public final class SpatialAnchorsAccountsListKeysSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/spatial-anchors/ListKeys.json
     */
    /**
     * Sample code: List spatial anchor account key.
     *
     * @param manager Entry point to MixedRealityManager.
     */
    public static void listSpatialAnchorAccountKey(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.spatialAnchorsAccounts().listKeysWithResponse("MyResourceGroup", "MyAccount", Context.NONE);
    }
}
```

### SpatialAnchorsAccounts_RegenerateKeys

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mixedreality.models.AccountKeyRegenerateRequest;
import com.azure.resourcemanager.mixedreality.models.Serial;

/** Samples for SpatialAnchorsAccounts RegenerateKeys. */
public final class SpatialAnchorsAccountsRegenerateKeysSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/spatial-anchors/RegenerateKey.json
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
                "MyResourceGroup", "MyAccount", new AccountKeyRegenerateRequest().withSerial(Serial.ONE), Context.NONE);
    }
}
```

### SpatialAnchorsAccounts_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mixedreality.models.SpatialAnchorsAccount;
import java.util.HashMap;
import java.util.Map;

/** Samples for SpatialAnchorsAccounts Update. */
public final class SpatialAnchorsAccountsUpdateSamples {
    /*
     * x-ms-original-file: specification/mixedreality/resource-manager/Microsoft.MixedReality/preview/2021-03-01-preview/examples/spatial-anchors/Patch.json
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
                .getByResourceGroupWithResponse("MyResourceGroup", "MyAccount", Context.NONE)
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

