# Code snippets and samples


## AccountOperation

- [CreateAndUpdate](#accountoperation_createandupdate)
- [Delete](#accountoperation_delete)
- [GetByResourceGroup](#accountoperation_getbyresourcegroup)
- [Update](#accountoperation_update)

## Accounts

- [List](#accounts_list)
- [ListByResourceGroup](#accounts_listbyresourcegroup)

## Operation

- [List](#operation_list)
### AccountOperation_CreateAndUpdate

```java
import com.azure.resourcemanager.graphservices.models.AccountResourceProperties;
import java.util.HashMap;
import java.util.Map;

/** Samples for AccountOperation CreateAndUpdate. */
public final class AccountOperationCreateAndUpdateSamples {
    /*
     * x-ms-original-file: specification/graphservicesprod/resource-manager/Microsoft.GraphServices/preview/2022-09-22-preview/examples/Accounts_Create.json
     */
    /**
     * Sample code: Create Account resource.
     *
     * @param manager Entry point to GraphServicesManager.
     */
    public static void createAccountResource(com.azure.resourcemanager.graphservices.GraphServicesManager manager) {
        manager
            .accountOperations()
            .define("11111111-aaaa-1111-bbbb-1111111111111")
            .withRegion((String) null)
            .withExistingResourceGroup("testResourceGroupGRAM")
            .withProperties(new AccountResourceProperties().withAppId("11111111-aaaa-1111-bbbb-111111111111"))
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
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

### AccountOperation_Delete

```java
/** Samples for AccountOperation Delete. */
public final class AccountOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/graphservicesprod/resource-manager/Microsoft.GraphServices/preview/2022-09-22-preview/examples/Accounts_Delete.json
     */
    /**
     * Sample code: Delete account resource.
     *
     * @param manager Entry point to GraphServicesManager.
     */
    public static void deleteAccountResource(com.azure.resourcemanager.graphservices.GraphServicesManager manager) {
        manager
            .accountOperations()
            .deleteByResourceGroupWithResponse(
                "testResourceGroupGRAM", "11111111-aaaa-1111-bbbb-111111111111", com.azure.core.util.Context.NONE);
    }
}
```

### AccountOperation_GetByResourceGroup

```java
/** Samples for AccountOperation GetByResourceGroup. */
public final class AccountOperationGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/graphservicesprod/resource-manager/Microsoft.GraphServices/preview/2022-09-22-preview/examples/Accounts_Get.json
     */
    /**
     * Sample code: Get accounts.
     *
     * @param manager Entry point to GraphServicesManager.
     */
    public static void getAccounts(com.azure.resourcemanager.graphservices.GraphServicesManager manager) {
        manager
            .accountOperations()
            .getByResourceGroupWithResponse(
                "testResourceGroupGRAM", "11111111-aaaa-1111-bbbb-111111111111", com.azure.core.util.Context.NONE);
    }
}
```

### AccountOperation_Update

```java
import com.azure.resourcemanager.graphservices.models.AccountResource;
import java.util.HashMap;
import java.util.Map;

/** Samples for AccountOperation Update. */
public final class AccountOperationUpdateSamples {
    /*
     * x-ms-original-file: specification/graphservicesprod/resource-manager/Microsoft.GraphServices/preview/2022-09-22-preview/examples/Accounts_Update.json
     */
    /**
     * Sample code: Update account resource.
     *
     * @param manager Entry point to GraphServicesManager.
     */
    public static void updateAccountResource(com.azure.resourcemanager.graphservices.GraphServicesManager manager) {
        AccountResource resource =
            manager
                .accountOperations()
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

### Accounts_List

```java
/** Samples for Accounts List. */
public final class AccountsListSamples {
    /*
     * x-ms-original-file: specification/graphservicesprod/resource-manager/Microsoft.GraphServices/preview/2022-09-22-preview/examples/Accounts_List_Sub.json
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
     * x-ms-original-file: specification/graphservicesprod/resource-manager/Microsoft.GraphServices/preview/2022-09-22-preview/examples/Accounts_List.json
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

### Operation_List

```java
/** Samples for Operation List. */
public final class OperationListSamples {
    /*
     * x-ms-original-file: specification/graphservicesprod/resource-manager/Microsoft.GraphServices/preview/2022-09-22-preview/examples/Operations_List.json
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

