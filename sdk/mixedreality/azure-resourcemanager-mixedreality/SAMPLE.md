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

### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file:
     * specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/proxy/
     * ExposingAvailableOperations.json
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

/**
 * Samples for RemoteRenderingAccounts Create.
 */
public final class RemoteRenderingAccountsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/remote-rendering/
     * Put.json
     */
    /**
     * Sample code: Create remote rendering account.
     * 
     * @param manager Entry point to MixedRealityManager.
     */
    public static void
        createRemoteRenderingAccount(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.remoteRenderingAccounts()
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
/**
 * Samples for RemoteRenderingAccounts Delete.
 */
public final class RemoteRenderingAccountsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/remote-rendering/
     * Delete.json
     */
    /**
     * Sample code: Delete remote rendering account.
     * 
     * @param manager Entry point to MixedRealityManager.
     */
    public static void
        deleteRemoteRenderingAccount(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.remoteRenderingAccounts()
            .deleteByResourceGroupWithResponse("MyResourceGroup", "MyAccount", com.azure.core.util.Context.NONE);
    }
}
```

### RemoteRenderingAccounts_GetByResourceGroup

```java
/**
 * Samples for RemoteRenderingAccounts GetByResourceGroup.
 */
public final class RemoteRenderingAccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/remote-rendering/
     * Get.json
     */
    /**
     * Sample code: Get remote rendering account.
     * 
     * @param manager Entry point to MixedRealityManager.
     */
    public static void getRemoteRenderingAccount(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.remoteRenderingAccounts()
            .getByResourceGroupWithResponse("MyResourceGroup", "MyAccount", com.azure.core.util.Context.NONE);
    }
}
```

### RemoteRenderingAccounts_List

```java
/**
 * Samples for RemoteRenderingAccounts List.
 */
public final class RemoteRenderingAccountsListSamples {
    /*
     * x-ms-original-file:
     * specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/remote-rendering/
     * GetBySubscription.json
     */
    /**
     * Sample code: List remote rendering accounts by subscription.
     * 
     * @param manager Entry point to MixedRealityManager.
     */
    public static void
        listRemoteRenderingAccountsBySubscription(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.remoteRenderingAccounts().list(com.azure.core.util.Context.NONE);
    }
}
```

### RemoteRenderingAccounts_ListByResourceGroup

```java
/**
 * Samples for RemoteRenderingAccounts ListByResourceGroup.
 */
public final class RemoteRenderingAccountsListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/remote-rendering/
     * GetByResourceGroup.json
     */
    /**
     * Sample code: List remote rendering accounts by resource group.
     * 
     * @param manager Entry point to MixedRealityManager.
     */
    public static void
        listRemoteRenderingAccountsByResourceGroup(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.remoteRenderingAccounts().listByResourceGroup("MyResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### RemoteRenderingAccounts_ListKeys

```java
/**
 * Samples for RemoteRenderingAccounts ListKeys.
 */
public final class RemoteRenderingAccountsListKeysSamples {
    /*
     * x-ms-original-file:
     * specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/remote-rendering/
     * ListKeys.json
     */
    /**
     * Sample code: List remote rendering account key.
     * 
     * @param manager Entry point to MixedRealityManager.
     */
    public static void
        listRemoteRenderingAccountKey(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.remoteRenderingAccounts()
            .listKeysWithResponse("MyResourceGroup", "MyAccount", com.azure.core.util.Context.NONE);
    }
}
```

### RemoteRenderingAccounts_RegenerateKeys

```java
import com.azure.resourcemanager.mixedreality.models.AccountKeyRegenerateRequest;
import com.azure.resourcemanager.mixedreality.models.Serial;

/**
 * Samples for RemoteRenderingAccounts RegenerateKeys.
 */
public final class RemoteRenderingAccountsRegenerateKeysSamples {
    /*
     * x-ms-original-file:
     * specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/remote-rendering/
     * RegenerateKey.json
     */
    /**
     * Sample code: Regenerate remote rendering account keys.
     * 
     * @param manager Entry point to MixedRealityManager.
     */
    public static void
        regenerateRemoteRenderingAccountKeys(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        manager.remoteRenderingAccounts()
            .regenerateKeysWithResponse("MyResourceGroup", "MyAccount",
                new AccountKeyRegenerateRequest().withSerial(Serial.ONE), com.azure.core.util.Context.NONE);
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

/**
 * Samples for RemoteRenderingAccounts Update.
 */
public final class RemoteRenderingAccountsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/mixedreality/resource-manager/Microsoft.MixedReality/stable/2021-01-01/examples/remote-rendering/
     * Patch.json
     */
    /**
     * Sample code: Update remote rendering account.
     * 
     * @param manager Entry point to MixedRealityManager.
     */
    public static void
        updateRemoteRenderingAccount(com.azure.resourcemanager.mixedreality.MixedRealityManager manager) {
        RemoteRenderingAccount resource = manager.remoteRenderingAccounts()
            .getByResourceGroupWithResponse("MyResourceGroup", "MyAccount", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("hero", "romeo", "heroine", "juliet"))
            .withIdentity(new Identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
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

