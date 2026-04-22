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

## Subscription

- [AcceptOwnership](#subscription_acceptownership)
- [AcceptOwnershipStatus](#subscription_acceptownershipstatus)
- [Cancel](#subscription_cancel)
- [Enable](#subscription_enable)
- [Rename](#subscription_rename)

## SubscriptionOperation

- [Get](#subscriptionoperation_get)

## SubscriptionPolicy

- [AddUpdatePolicyForTenant](#subscriptionpolicy_addupdatepolicyfortenant)
- [GetPolicyForTenant](#subscriptionpolicy_getpolicyfortenant)
- [ListPolicyForTenant](#subscriptionpolicy_listpolicyfortenant)
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
     * x-ms-original-file:
     * specification/subscription/resource-manager/Microsoft.Subscription/stable/2021-10-01/examples/createAlias.json
     */
    /**
     * Sample code: CreateAlias.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void createAlias(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.alias()
            .create("aliasForNewSub", new PutAliasRequest().withProperties(new PutAliasRequestProperties()
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
     * x-ms-original-file:
     * specification/subscription/resource-manager/Microsoft.Subscription/stable/2021-10-01/examples/deleteAlias.json
     */
    /**
     * Sample code: DeleteAlias.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void deleteAlias(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.alias().deleteWithResponse("aliasForNewSub", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/subscription/resource-manager/Microsoft.Subscription/stable/2021-10-01/examples/getAlias.json
     */
    /**
     * Sample code: GetAlias.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void getAlias(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.alias().getWithResponse("aliasForNewSub", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/subscription/resource-manager/Microsoft.Subscription/stable/2021-10-01/examples/listAlias.json
     */
    /**
     * Sample code: ListAlias.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void listAlias(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.alias().listWithResponse(com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/subscription/resource-manager/Microsoft.Subscription/stable/2021-10-01/examples/
     * getBillingAccountPolicy.json
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
     * x-ms-original-file:
     * specification/subscription/resource-manager/Microsoft.Subscription/stable/2021-10-01/examples/getOperations.json
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

### Subscription_AcceptOwnership

```java
import com.azure.resourcemanager.subscription.models.AcceptOwnershipRequest;
import com.azure.resourcemanager.subscription.models.AcceptOwnershipRequestProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Subscription AcceptOwnership.
 */
public final class SubscriptionAcceptOwnershipSamples {
    /*
     * x-ms-original-file:
     * specification/subscription/resource-manager/Microsoft.Subscription/stable/2021-10-01/examples/
     * acceptSubscriptionOwnership.json
     */
    /**
     * Sample code: AcceptOwnership.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void acceptOwnership(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.subscriptions()
            .acceptOwnership("291bba3f-e0a5-47bc-a099-3bdcb2a50a05",
                new AcceptOwnershipRequest()
                    .withProperties(new AcceptOwnershipRequestProperties().withDisplayName("Test Subscription")
                        .withTags(mapOf("tag1", "Messi", "tag2", "Ronaldo", "tag3", "Lebron"))),
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

### Subscription_AcceptOwnershipStatus

```java
/**
 * Samples for Subscription AcceptOwnershipStatus.
 */
public final class SubscriptionAcceptOwnershipStatusSamples {
    /*
     * x-ms-original-file:
     * specification/subscription/resource-manager/Microsoft.Subscription/stable/2021-10-01/examples/
     * acceptOwnershipStatus.json
     */
    /**
     * Sample code: AcceptOwnershipStatus.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void acceptOwnershipStatus(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.subscriptions()
            .acceptOwnershipStatusWithResponse("291bba3f-e0a5-47bc-a099-3bdcb2a50a05",
                com.azure.core.util.Context.NONE);
    }
}
```

### Subscription_Cancel

```java
/**
 * Samples for Subscription Cancel.
 */
public final class SubscriptionCancelSamples {
    /*
     * x-ms-original-file:
     * specification/subscription/resource-manager/Microsoft.Subscription/stable/2021-10-01/examples/cancelSubscription.
     * json
     */
    /**
     * Sample code: cancelSubscription.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void cancelSubscription(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.subscriptions()
            .cancelWithResponse("83aa47df-e3e9-49ff-877b-94304bf3d3ad", com.azure.core.util.Context.NONE);
    }
}
```

### Subscription_Enable

```java
/**
 * Samples for Subscription Enable.
 */
public final class SubscriptionEnableSamples {
    /*
     * x-ms-original-file:
     * specification/subscription/resource-manager/Microsoft.Subscription/stable/2021-10-01/examples/enableSubscription.
     * json
     */
    /**
     * Sample code: enableSubscription.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void enableSubscription(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.subscriptions()
            .enableWithResponse("7948bcee-488c-47ce-941c-38e20ede803d", com.azure.core.util.Context.NONE);
    }
}
```

### Subscription_Rename

```java
import com.azure.resourcemanager.subscription.models.SubscriptionName;

/**
 * Samples for Subscription Rename.
 */
public final class SubscriptionRenameSamples {
    /*
     * x-ms-original-file:
     * specification/subscription/resource-manager/Microsoft.Subscription/stable/2021-10-01/examples/renameSubscription.
     * json
     */
    /**
     * Sample code: renameSubscription.
     * 
     * @param manager Entry point to SubscriptionManager.
     */
    public static void renameSubscription(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.subscriptions()
            .renameWithResponse("83aa47df-e3e9-49ff-877b-94304bf3d3ad",
                new SubscriptionName().withSubscriptionName("Test Sub"), com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/subscription/resource-manager/Microsoft.Subscription/stable/2021-10-01/examples/
     * getSubscriptionOperation.json
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
import java.util.UUID;

/**
 * Samples for SubscriptionPolicy AddUpdatePolicyForTenant.
 */
public final class SubscriptionPolicyAddUpdatePolicyForTenantSamples {
    /*
     * x-ms-original-file:
     * specification/subscription/resource-manager/Microsoft.Subscription/stable/2021-10-01/examples/changeTenantPolicy.
     * json
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
                    .withExemptedPrincipals(Arrays.asList(UUID.fromString("e879cf0f-2b4d-5431-109a-f72fc9868693"),
                        UUID.fromString("9792da87-c97b-410d-a97d-27021ba09ce6"))),
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
     * x-ms-original-file:
     * specification/subscription/resource-manager/Microsoft.Subscription/stable/2021-10-01/examples/getTenantPolicy.
     * json
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
     * x-ms-original-file:
     * specification/subscription/resource-manager/Microsoft.Subscription/stable/2021-10-01/examples/getTenantPolicyList
     * .json
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

