# Code snippets and samples


## AccountQuotas

- [Get](#accountquotas_get)
- [ListByAccount](#accountquotas_listbyaccount)

## Accounts

- [CheckNameAvailability](#accounts_checknameavailability)
- [CreateOrUpdate](#accounts_createorupdate)
- [Delete](#accounts_delete)
- [Update](#accounts_update)

## Operations

- [List](#operations_list)

## Quotas

- [Get](#quotas_get)
- [ListBySubscription](#quotas_listbysubscription)
### AccountQuotas_Get

```java
import com.azure.resourcemanager.playwrighttesting.models.QuotaNames;

/**
 * Samples for AccountQuotas Get.
 */
public final class AccountQuotasGetSamples {
    /*
     * x-ms-original-file: 2024-12-01/AccountQuotas_Get.json
     */
    /**
     * Sample code: AccountQuotas_Get.
     * 
     * @param manager Entry point to PlaywrightTestingManager.
     */
    public static void accountQuotasGet(com.azure.resourcemanager.playwrighttesting.PlaywrightTestingManager manager) {
        manager.accountQuotas()
            .getWithResponse("dummyrg", "myPlaywrightAccount", QuotaNames.SCALABLE_EXECUTION,
                com.azure.core.util.Context.NONE);
    }
}
```

### AccountQuotas_ListByAccount

```java
/**
 * Samples for AccountQuotas ListByAccount.
 */
public final class AccountQuotasListByAccountSamples {
    /*
     * x-ms-original-file: 2024-12-01/AccountQuotas_ListByAccount.json
     */
    /**
     * Sample code: AccountQuotas_ListByAccount.
     * 
     * @param manager Entry point to PlaywrightTestingManager.
     */
    public static void
        accountQuotasListByAccount(com.azure.resourcemanager.playwrighttesting.PlaywrightTestingManager manager) {
        manager.accountQuotas().listByAccount("dummyrg", "myPlaywrightAccount", com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_CheckNameAvailability

```java
import com.azure.resourcemanager.playwrighttesting.models.CheckNameAvailabilityRequest;

/**
 * Samples for Accounts CheckNameAvailability.
 */
public final class AccountsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: 2024-12-01/Accounts_CheckNameAvailability.json
     */
    /**
     * Sample code: Accounts_CheckNameAvailability.
     * 
     * @param manager Entry point to PlaywrightTestingManager.
     */
    public static void
        accountsCheckNameAvailability(com.azure.resourcemanager.playwrighttesting.PlaywrightTestingManager manager) {
        manager.accounts()
            .checkNameAvailabilityWithResponse(new CheckNameAvailabilityRequest().withName("dummyName")
                .withType("Microsoft.AzurePlaywrightService/Accounts"), com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_CreateOrUpdate

```java
import com.azure.resourcemanager.playwrighttesting.models.AccountProperties;
import com.azure.resourcemanager.playwrighttesting.models.EnablementStatus;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Accounts CreateOrUpdate.
 */
public final class AccountsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-12-01/Accounts_CreateOrUpdate.json
     */
    /**
     * Sample code: Accounts_CreateOrUpdate.
     * 
     * @param manager Entry point to PlaywrightTestingManager.
     */
    public static void
        accountsCreateOrUpdate(com.azure.resourcemanager.playwrighttesting.PlaywrightTestingManager manager) {
        manager.accounts()
            .define("myPlaywrightAccount")
            .withRegion("westus")
            .withExistingResourceGroup("dummyrg")
            .withTags(mapOf("Team", "Dev Exp"))
            .withProperties(new AccountProperties().withRegionalAffinity(EnablementStatus.ENABLED))
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
/**
 * Samples for Accounts Delete.
 */
public final class AccountsDeleteSamples {
    /*
     * x-ms-original-file: 2024-12-01/Accounts_Delete.json
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

### Accounts_Update

```java
import com.azure.resourcemanager.playwrighttesting.models.Account;
import com.azure.resourcemanager.playwrighttesting.models.AccountUpdateProperties;
import com.azure.resourcemanager.playwrighttesting.models.EnablementStatus;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Accounts Update.
 */
public final class AccountsUpdateSamples {
    /*
     * x-ms-original-file: 2024-12-01/Accounts_Update.json
     */
    /**
     * Sample code: Accounts_Update.
     * 
     * @param manager Entry point to PlaywrightTestingManager.
     */
    public static void accountsUpdate(com.azure.resourcemanager.playwrighttesting.PlaywrightTestingManager manager) {
        Account resource = manager.accounts()
            .getByResourceGroupWithResponse("dummyrg", "myPlaywrightAccount", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("Team", "Dev Exp", "Division", "LT"))
            .withProperties(new AccountUpdateProperties().withRegionalAffinity(EnablementStatus.ENABLED))
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
     * x-ms-original-file: 2024-12-01/Operations_List.json
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

/**
 * Samples for Quotas Get.
 */
public final class QuotasGetSamples {
    /*
     * x-ms-original-file: 2024-12-01/Quotas_Get.json
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
/**
 * Samples for Quotas ListBySubscription.
 */
public final class QuotasListBySubscriptionSamples {
    /*
     * x-ms-original-file: 2024-12-01/Quotas_ListBySubscription.json
     */
    /**
     * Sample code: Quotas_ListBySubscription.
     * 
     * @param manager Entry point to PlaywrightTestingManager.
     */
    public static void
        quotasListBySubscription(com.azure.resourcemanager.playwrighttesting.PlaywrightTestingManager manager) {
        manager.quotas().listBySubscription("eastus", com.azure.core.util.Context.NONE);
    }
}
```

