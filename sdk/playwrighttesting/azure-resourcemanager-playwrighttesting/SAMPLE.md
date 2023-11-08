# Code snippets and samples


## Accounts

- [CreateOrUpdate](#accounts_createorupdate)
- [Delete](#accounts_delete)
- [GetByResourceGroup](#accounts_getbyresourcegroup)
- [List](#accounts_list)
- [ListByResourceGroup](#accounts_listbyresourcegroup)
- [Update](#accounts_update)

## Operations

- [List](#operations_list)

## Quotas

- [Get](#quotas_get)
- [ListBySubscription](#quotas_listbysubscription)
### Accounts_CreateOrUpdate

```java
import com.azure.resourcemanager.playwrighttesting.models.EnablementStatus;
import java.util.HashMap;
import java.util.Map;

/** Samples for Accounts CreateOrUpdate. */
public final class AccountsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/playwrighttesting/resource-manager/Microsoft.AzurePlaywrightService/preview/2023-10-01-preview/examples/Accounts_CreateOrUpdate.json
     */
    /**
     * Sample code: Accounts_CreateOrUpdate.
     *
     * @param manager Entry point to PlaywrightTestingManager.
     */
    public static void accountsCreateOrUpdate(
        com.azure.resourcemanager.playwrighttesting.PlaywrightTestingManager manager) {
        manager
            .accounts()
            .define("myPlaywrightAccount")
            .withRegion("westus")
            .withExistingResourceGroup("dummyrg")
            .withTags(mapOf("Team", "Dev Exp"))
            .withRegionalAffinity(EnablementStatus.ENABLED)
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

### Accounts_Delete

```java
/** Samples for Accounts Delete. */
public final class AccountsDeleteSamples {
    /*
     * x-ms-original-file: specification/playwrighttesting/resource-manager/Microsoft.AzurePlaywrightService/preview/2023-10-01-preview/examples/Accounts_Delete.json
     */
    /**
     * Sample code: Accounts_Delete.
     *
     * @param manager Entry point to PlaywrightTestingManager.
     */
    public static void accountsDelete(com.azure.resourcemanager.playwrighttesting.PlaywrightTestingManager manager) {
        manager.accounts().delete("dummyrg", "myPlaywrightAccount", com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_GetByResourceGroup

```java
/** Samples for Accounts GetByResourceGroup. */
public final class AccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/playwrighttesting/resource-manager/Microsoft.AzurePlaywrightService/preview/2023-10-01-preview/examples/Accounts_Get.json
     */
    /**
     * Sample code: Accounts_Get.
     *
     * @param manager Entry point to PlaywrightTestingManager.
     */
    public static void accountsGet(com.azure.resourcemanager.playwrighttesting.PlaywrightTestingManager manager) {
        manager
            .accounts()
            .getByResourceGroupWithResponse("dummyrg", "myPlaywrightAccount", com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_List

```java
/** Samples for Accounts List. */
public final class AccountsListSamples {
    /*
     * x-ms-original-file: specification/playwrighttesting/resource-manager/Microsoft.AzurePlaywrightService/preview/2023-10-01-preview/examples/Accounts_ListBySubscription.json
     */
    /**
     * Sample code: Accounts_ListBySubscription.
     *
     * @param manager Entry point to PlaywrightTestingManager.
     */
    public static void accountsListBySubscription(
        com.azure.resourcemanager.playwrighttesting.PlaywrightTestingManager manager) {
        manager.accounts().list(com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_ListByResourceGroup

```java
/** Samples for Accounts ListByResourceGroup. */
public final class AccountsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/playwrighttesting/resource-manager/Microsoft.AzurePlaywrightService/preview/2023-10-01-preview/examples/Accounts_ListByResourceGroup.json
     */
    /**
     * Sample code: Accounts_ListByResourceGroup.
     *
     * @param manager Entry point to PlaywrightTestingManager.
     */
    public static void accountsListByResourceGroup(
        com.azure.resourcemanager.playwrighttesting.PlaywrightTestingManager manager) {
        manager.accounts().listByResourceGroup("dummyrg", com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_Update

```java
import com.azure.resourcemanager.playwrighttesting.models.Account;
import com.azure.resourcemanager.playwrighttesting.models.EnablementStatus;
import java.util.HashMap;
import java.util.Map;

/** Samples for Accounts Update. */
public final class AccountsUpdateSamples {
    /*
     * x-ms-original-file: specification/playwrighttesting/resource-manager/Microsoft.AzurePlaywrightService/preview/2023-10-01-preview/examples/Accounts_Update.json
     */
    /**
     * Sample code: Accounts_Update.
     *
     * @param manager Entry point to PlaywrightTestingManager.
     */
    public static void accountsUpdate(com.azure.resourcemanager.playwrighttesting.PlaywrightTestingManager manager) {
        Account resource =
            manager
                .accounts()
                .getByResourceGroupWithResponse("dummyrg", "myPlaywrightAccount", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("Division", "LT", "Team", "Dev Exp"))
            .withRegionalAffinity(EnablementStatus.ENABLED)
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
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/playwrighttesting/resource-manager/Microsoft.AzurePlaywrightService/preview/2023-10-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to PlaywrightTestingManager.
     */
    public static void operationsList(com.azure.resourcemanager.playwrighttesting.PlaywrightTestingManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Quotas_Get

```java
import com.azure.resourcemanager.playwrighttesting.models.QuotaNames;

/** Samples for Quotas Get. */
public final class QuotasGetSamples {
    /*
     * x-ms-original-file: specification/playwrighttesting/resource-manager/Microsoft.AzurePlaywrightService/preview/2023-10-01-preview/examples/Quotas_Get.json
     */
    /**
     * Sample code: Quotas_Get.
     *
     * @param manager Entry point to PlaywrightTestingManager.
     */
    public static void quotasGet(com.azure.resourcemanager.playwrighttesting.PlaywrightTestingManager manager) {
        manager.quotas().getWithResponse("eastus", QuotaNames.SCALABLE_EXECUTION, com.azure.core.util.Context.NONE);
    }
}
```

### Quotas_ListBySubscription

```java
/** Samples for Quotas ListBySubscription. */
public final class QuotasListBySubscriptionSamples {
    /*
     * x-ms-original-file: specification/playwrighttesting/resource-manager/Microsoft.AzurePlaywrightService/preview/2023-10-01-preview/examples/Quotas_ListBySubscription.json
     */
    /**
     * Sample code: Quotas_ListBySubscription.
     *
     * @param manager Entry point to PlaywrightTestingManager.
     */
    public static void quotasListBySubscription(
        com.azure.resourcemanager.playwrighttesting.PlaywrightTestingManager manager) {
        manager.quotas().listBySubscription("eastus", com.azure.core.util.Context.NONE);
    }
}
```

