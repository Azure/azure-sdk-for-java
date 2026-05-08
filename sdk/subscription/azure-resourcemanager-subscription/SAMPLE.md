# Code snippets and samples


## Alias

- [Create](#alias_create)
- [Delete](#alias_delete)
- [Get](#alias_get)
- [List](#alias_list)

## BillingAccount

- [GetPolicy](#billingaccount_getpolicy)

## Operations

- [List](#operations_list)

## SubscriptionOperation

- [Get](#subscriptionoperation_get)

## SubscriptionPolicy

- [AddUpdatePolicyForTenant](#subscriptionpolicy_addupdatepolicyfortenant)
- [GetPolicyForTenant](#subscriptionpolicy_getpolicyfortenant)
- [ListPolicyForTenant](#subscriptionpolicy_listpolicyfortenant)

## Subscriptions

- [AcceptTargetDirectory](#subscriptions_accepttargetdirectory)
- [DeleteTargetDirectory](#subscriptions_deletetargetdirectory)
- [GetTargetDirectory](#subscriptions_gettargetdirectory)
- [List](#subscriptions_list)
- [PutTargetDirectory](#subscriptions_puttargetdirectory)
- [TargetDirectoryStatus](#subscriptions_targetdirectorystatus)
### Alias_Create

```java
import com.azure.resourcemanager.subscription.models.PutAliasRequest;
import com.azure.resourcemanager.subscription.models.PutAliasRequestAdditionalProperties;
import com.azure.resourcemanager.subscription.models.PutAliasRequestProperties;
import com.azure.resourcemanager.subscription.models.Workload;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Alias Create.
 */
public final class AliasCreateSamples {
    /*
     * x-ms-original-file: 2025-11-01-preview/createAlias.json
     */
    /**
     * Sample code: CreateAlias.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void createAlias(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.alias()
            .create("dummyalias", new PutAliasRequest().withProperties(new PutAliasRequestProperties()
                .withDisplayName("Test Subscription")
                .withWorkload(Workload.PRODUCTION)
                .withBillingScope(
                    "/billingAccounts/af6231a7-7f8d-4fcc-a993-dd8466108d07:c663dac6-a9a5-405a-8938-cd903e12ab5b_2019_05_31/billingProfiles/QWDQ-QWHI-AUW-SJDO-DJH/invoiceSections/FEUF-EUHE-ISJ-SKDW-DJH")
                .withAdditionalProperties(new PutAliasRequestAdditionalProperties()
                    .withSubscriptionTenantId("66f6e4d6-07dc-4aea-94ea-e12d3026a3c8")
                    .withSubscriptionOwnerId("f09b39eb-c496-482c-9ab9-afd799572f4c")
                    .withTags(mapOf("tag1", "Messi", "tag2", "Ronaldo", "tag3", "Lebron")))),
                com.azure.core.util.Context.NONE);
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

### Alias_Delete

```java
/**
 * Samples for Alias Delete.
 */
public final class AliasDeleteSamples {
    /*
     * x-ms-original-file: 2025-11-01-preview/deleteAlias.json
     */
    /**
     * Sample code: DeleteAlias.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void deleteAlias(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.alias().deleteWithResponse("dummyalias", com.azure.core.util.Context.NONE);
    }
}
```

### Alias_Get

```java
/**
 * Samples for Alias Get.
 */
public final class AliasGetSamples {
    /*
     * x-ms-original-file: 2025-11-01-preview/getAlias.json
     */
    /**
     * Sample code: GetAlias.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void getAlias(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.alias().getWithResponse("dummyalias", com.azure.core.util.Context.NONE);
    }
}
```

### Alias_List

```java
/**
 * Samples for Alias List.
 */
public final class AliasListSamples {
    /*
     * x-ms-original-file: 2025-11-01-preview/listAlias.json
     */
    /**
     * Sample code: ListAlias.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void listAlias(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.alias().list(com.azure.core.util.Context.NONE);
    }
}
```

### BillingAccount_GetPolicy

```java
/**
 * Samples for BillingAccount GetPolicy.
 */
public final class BillingAccountGetPolicySamples {
    /*
     * x-ms-original-file: 2025-11-01-preview/getBillingAccountPolicy.json
     */
    /**
     * Sample code: GetBillingAccountPolicy.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void getBillingAccountPolicy(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.billingAccounts().getPolicyWithResponse("testBillingAccountId", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-11-01-preview/getOperations.json
     */
    /**
     * Sample code: getOperations.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void getOperations(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### SubscriptionOperation_Get

```java
/**
 * Samples for SubscriptionOperation Get.
 */
public final class SubscriptionOperationGetSamples {
    /*
     * x-ms-original-file: 2025-11-01-preview/getSubscriptionOperation.json
     */
    /**
     * Sample code: getPendingSubscriptionOperations.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void
        getPendingSubscriptionOperations(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.subscriptionOperations()
            .getWithResponse("e4b8d068-f574-462a-a76f-6fa0afc613c9", com.azure.core.util.Context.NONE);
    }
}
```

### SubscriptionPolicy_AddUpdatePolicyForTenant

```java
import com.azure.resourcemanager.subscription.models.PutTenantPolicyRequestProperties;
import java.util.Arrays;

/**
 * Samples for SubscriptionPolicy AddUpdatePolicyForTenant.
 */
public final class SubscriptionPolicyAddUpdatePolicyForTenantSamples {
    /*
     * x-ms-original-file: 2025-11-01-preview/changeTenantPolicy.json
     */
    /**
     * Sample code: TenantPolicy.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void tenantPolicy(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.subscriptionPolicies()
            .addUpdatePolicyForTenantWithResponse(
                new PutTenantPolicyRequestProperties().withBlockSubscriptionsLeavingTenant(true)
                    .withBlockSubscriptionsIntoTenant(true)
                    .withExemptedPrincipals(
                        Arrays.asList("e879cf0f-2b4d-5431-109a-f72fc9868693", "9792da87-c97b-410d-a97d-27021ba09ce6")),
                com.azure.core.util.Context.NONE);
    }
}
```

### SubscriptionPolicy_GetPolicyForTenant

```java
/**
 * Samples for SubscriptionPolicy GetPolicyForTenant.
 */
public final class SubscriptionPolicyGetPolicyForTenantSamples {
    /*
     * x-ms-original-file: 2025-11-01-preview/getTenantPolicy.json
     */
    /**
     * Sample code: getTenantPolicy.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void getTenantPolicy(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.subscriptionPolicies().getPolicyForTenantWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### SubscriptionPolicy_ListPolicyForTenant

```java
/**
 * Samples for SubscriptionPolicy ListPolicyForTenant.
 */
public final class SubscriptionPolicyListPolicyForTenantSamples {
    /*
     * x-ms-original-file: 2025-11-01-preview/getTenantPolicyList.json
     */
    /**
     * Sample code: getTenantPolicyList.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void getTenantPolicyList(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.subscriptionPolicies().listPolicyForTenant(com.azure.core.util.Context.NONE);
    }
}
```

### Subscriptions_AcceptTargetDirectory

```java
/**
 * Samples for Subscriptions AcceptTargetDirectory.
 */
public final class SubscriptionsAcceptTargetDirectorySamples {
    /*
     * x-ms-original-file: 2025-11-01-preview/acceptTargetDirectory.json
     */
    /**
     * Sample code: acceptTargetDirectory.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void acceptTargetDirectory(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.subscriptions()
            .acceptTargetDirectoryWithResponse("6c3c85bc-5366-4eaa-8055-a10529eafd03",
                com.azure.core.util.Context.NONE);
    }
}
```

### Subscriptions_DeleteTargetDirectory

```java
/**
 * Samples for Subscriptions DeleteTargetDirectory.
 */
public final class SubscriptionsDeleteTargetDirectorySamples {
    /*
     * x-ms-original-file: 2025-11-01-preview/deleteTargetDirectory.json
     */
    /**
     * Sample code: deleteTargetDirectory.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void deleteTargetDirectory(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.subscriptions()
            .deleteTargetDirectoryWithResponse("ebe4f8fd-d8b3-4867-bcf4-b2407edd196d",
                com.azure.core.util.Context.NONE);
    }
}
```

### Subscriptions_GetTargetDirectory

```java
/**
 * Samples for Subscriptions GetTargetDirectory.
 */
public final class SubscriptionsGetTargetDirectorySamples {
    /*
     * x-ms-original-file: 2025-11-01-preview/getTargetDirectory.json
     */
    /**
     * Sample code: getTargetDirectory.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void getTargetDirectory(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.subscriptions()
            .getTargetDirectoryWithResponse("ebe4f8fd-d8b3-4867-bcf4-b2407edd196d", com.azure.core.util.Context.NONE);
    }
}
```

### Subscriptions_List

```java
/**
 * Samples for Subscriptions List.
 */
public final class SubscriptionsListSamples {
    /*
     * x-ms-original-file: 2025-11-01-preview/listTargetDirectory.json
     */
    /**
     * Sample code: listTargetDirectory.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void listTargetDirectory(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.subscriptions().list("ebe4f8fd-d8b3-4867-bcf4-b2407edd196d", com.azure.core.util.Context.NONE);
    }
}
```

### Subscriptions_PutTargetDirectory

```java
import com.azure.resourcemanager.subscription.models.TargetDirectoryRequest;
import com.azure.resourcemanager.subscription.models.TargetDirectoryRequestProperties;

/**
 * Samples for Subscriptions PutTargetDirectory.
 */
public final class SubscriptionsPutTargetDirectorySamples {
    /*
     * x-ms-original-file: 2025-11-01-preview/putTargetDirectory.json
     */
    /**
     * Sample code: putTargetDirectory.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void putTargetDirectory(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.subscriptions()
            .putTargetDirectoryWithResponse("ecce7b25-862b-44a2-9e21-a1baa50618eb",
                new TargetDirectoryRequest().withProperties(
                    new TargetDirectoryRequestProperties().withDestinationOwnerId("abhaypratap@live.com")
                        .withDestinationTenantId("111a82eb-4c7b-48bb-962b-49363c510130")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Subscriptions_TargetDirectoryStatus

```java
/**
 * Samples for Subscriptions TargetDirectoryStatus.
 */
public final class SubscriptionsTargetDirectoryStatusSamples {
    /*
     * x-ms-original-file: 2025-11-01-preview/targetDirectoryStatus.json
     */
    /**
     * Sample code: targetDirectoryStatus.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void targetDirectoryStatus(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.subscriptions()
            .targetDirectoryStatusWithResponse("e1084a54-27ab-4b72-a3ba-89fac9548f49",
                com.azure.core.util.Context.NONE);
    }
}
```

