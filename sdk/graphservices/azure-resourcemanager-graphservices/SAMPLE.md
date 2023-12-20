# Code snippets and samples


## Accounts

- [CreateAndUpdate](#accounts_createandupdate)
- [Delete](#accounts_delete)
- [GetByResourceGroup](#accounts_getbyresourcegroup)
- [List](#accounts_list)
- [ListByResourceGroup](#accounts_listbyresourcegroup)
- [Update](#accounts_update)

## Operations

- [List](#operations_list)
### Accounts_CreateAndUpdate

```java
import com.azure.resourcemanager.graphservices.models.AccountResourceProperties;

/** Samples for Accounts CreateAndUpdate. */
public final class AccountsCreateAndUpdateSamples {
    /*
     * x-ms-original-file: specification/graphservicesprod/resource-manager/Microsoft.GraphServices/stable/2023-04-13/examples/Accounts_Create.json
     */
    /**
     * Sample code: Create Account resource.
     *
     * @param manager Entry point to GraphServicesManager.
     */
    public static void createAccountResource(com.azure.resourcemanager.graphservices.GraphServicesManager manager) {
        manager
            .accounts()
            .define("11111111-aaaa-1111-bbbb-1111111111111")
            .withRegion((String) null)
            .withExistingResourceGroup("testResourceGroupGRAM")
            .withProperties(new AccountResourceProperties().withAppId("11111111-aaaa-1111-bbbb-111111111111"))
            .create();
    }
}
```

### Accounts_Delete

```java
/** Samples for Accounts Delete. */
public final class AccountsDeleteSamples {
    /*
     * x-ms-original-file: specification/graphservicesprod/resource-manager/Microsoft.GraphServices/stable/2023-04-13/examples/Accounts_Delete.json
     */
    /**
     * Sample code: Delete account resource.
     *
     * @param manager Entry point to GraphServicesManager.
     */
    public static void deleteAccountResource(com.azure.resourcemanager.graphservices.GraphServicesManager manager) {
        manager
            .accounts()
            .deleteByResourceGroupWithResponse(
                "testResourceGroupGRAM", "11111111-aaaa-1111-bbbb-111111111111", com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_GetByResourceGroup

```java
/** Samples for Accounts GetByResourceGroup. */
public final class AccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/graphservicesprod/resource-manager/Microsoft.GraphServices/stable/2023-04-13/examples/Accounts_Get.json
     */
    /**
     * Sample code: Get accounts.
     *
     * @param manager Entry point to GraphServicesManager.
     */
    public static void getAccounts(com.azure.resourcemanager.graphservices.GraphServicesManager manager) {
        manager
            .accounts()
            .getByResourceGroupWithResponse(
                "testResourceGroupGRAM", "11111111-aaaa-1111-bbbb-111111111111", com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_List

```java
/** Samples for Accounts List. */
public final class AccountsListSamples {
    /*
     * x-ms-original-file: specification/graphservicesprod/resource-manager/Microsoft.GraphServices/stable/2023-04-13/examples/Accounts_List_Sub.json
     */
    /**
     * Sample code: Get list of accounts by subscription.
     *
     * @param manager Entry point to GraphServicesManager.
     */
    public static void getListOfAccountsBySubscription(
        com.azure.resourcemanager.graphservices.GraphServicesManager manager) {
        manager.accounts().list(com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_ListByResourceGroup

```java
/** Samples for Accounts ListByResourceGroup. */
public final class AccountsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/graphservicesprod/resource-manager/Microsoft.GraphServices/stable/2023-04-13/examples/Accounts_List.json
     */
    /**
     * Sample code: Create or update account resource.
     *
     * @param manager Entry point to GraphServicesManager.
     */
    public static void createOrUpdateAccountResource(
        com.azure.resourcemanager.graphservices.GraphServicesManager manager) {
        manager.accounts().listByResourceGroup("testResourceGroupGRAM", com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_Update

```java
import com.azure.resourcemanager.graphservices.models.AccountResource;
import java.util.HashMap;
import java.util.Map;

/** Samples for Accounts Update. */
public final class AccountsUpdateSamples {
    /*
     * x-ms-original-file: specification/graphservicesprod/resource-manager/Microsoft.GraphServices/stable/2023-04-13/examples/Accounts_Update.json
     */
    /**
     * Sample code: Update account resource.
     *
     * @param manager Entry point to GraphServicesManager.
     */
    public static void updateAccountResource(com.azure.resourcemanager.graphservices.GraphServicesManager manager) {
        AccountResource resource =
            manager
                .accounts()
                .getByResourceGroupWithResponse(
                    "testResourceGroupGRAM", "11111111-aaaa-1111-bbbb-111111111111", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/graphservicesprod/resource-manager/Microsoft.GraphServices/stable/2023-04-13/examples/Operations_List.json
     */
    /**
     * Sample code: Get list of operations.
     *
     * @param manager Entry point to GraphServicesManager.
     */
    public static void getListOfOperations(com.azure.resourcemanager.graphservices.GraphServicesManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

