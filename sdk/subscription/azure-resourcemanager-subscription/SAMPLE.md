# Code snippets and samples


## Alias

- [Create](#alias_create)
- [Delete](#alias_delete)
- [Get](#alias_get)
- [List](#alias_list)

## Operations

- [List](#operations_list)

## SubscriptionOperation

- [Cancel](#subscriptionoperation_cancel)
- [Enable](#subscriptionoperation_enable)
- [Rename](#subscriptionoperation_rename)

## Subscriptions

- [Get](#subscriptions_get)
- [List](#subscriptions_list)
- [ListLocations](#subscriptions_listlocations)

## Tenants

- [List](#tenants_list)
### Alias_Create

```java
import com.azure.resourcemanager.subscription.models.PutAliasRequest;
import com.azure.resourcemanager.subscription.models.PutAliasRequestProperties;
import com.azure.resourcemanager.subscription.models.Workload;

/** Samples for Alias Create. */
public final class AliasCreateSamples {
    /*
     * x-ms-original-file: specification/subscription/resource-manager/Microsoft.Subscription/stable/2020-09-01/examples/createAlias.json
     */
    /**
     * Sample code: CreateAlias.
     *
     * @param manager Entry point to SubscriptionManager.
     */
    public static void createAlias(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager
            .alias()
            .create(
                "aliasForNewSub",
                new PutAliasRequest()
                    .withProperties(
                        new PutAliasRequestProperties()
                            .withDisplayName("Contoso MCA subscription")
                            .withWorkload(Workload.PRODUCTION)
                            .withBillingScope(
                                "/providers/Microsoft.Billing/billingAccounts/e879cf0f-2b4d-5431-109a-f72fc9868693:024cabf4-7321-4cf9-be59-df0c77ca51de_2019-05-31/billingProfiles/PE2Q-NOIT-BG7-TGB/invoiceSections/MTT4-OBS7-PJA-TGB")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Alias_Delete

```java
/** Samples for Alias Delete. */
public final class AliasDeleteSamples {
    /*
     * x-ms-original-file: specification/subscription/resource-manager/Microsoft.Subscription/stable/2020-09-01/examples/deleteAlias.json
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
/** Samples for Alias Get. */
public final class AliasGetSamples {
    /*
     * x-ms-original-file: specification/subscription/resource-manager/Microsoft.Subscription/stable/2020-09-01/examples/getAlias.json
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
/** Samples for Alias List. */
public final class AliasListSamples {
    /*
     * x-ms-original-file: specification/subscription/resource-manager/Microsoft.Subscription/stable/2020-09-01/examples/listAlias.json
     */
    /**
     * Sample code: GetAlias.
     *
     * @param manager Entry point to SubscriptionManager.
     */
    public static void getAlias(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.alias().listWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/subscription/resource-manager/Microsoft.Subscription/stable/2020-09-01/examples/getOperations.json
     */
    /**
     * Sample code: getOperations.
     *
     * @param manager Entry point to SubscriptionManager.
     */
    public static void getOperations(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.operations().listWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### SubscriptionOperation_Cancel

```java
/** Samples for SubscriptionOperation Cancel. */
public final class SubscriptionOperationCancelSamples {
    /*
     * x-ms-original-file: specification/subscription/resource-manager/Microsoft.Subscription/stable/2020-09-01/examples/cancelSubscription.json
     */
    /**
     * Sample code: cancelSubscription.
     *
     * @param manager Entry point to SubscriptionManager.
     */
    public static void cancelSubscription(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager
            .subscriptionOperations()
            .cancelWithResponse("83aa47df-e3e9-49ff-877b-94304bf3d3ad", com.azure.core.util.Context.NONE);
    }
}
```

### SubscriptionOperation_Enable

```java
/** Samples for SubscriptionOperation Enable. */
public final class SubscriptionOperationEnableSamples {
    /*
     * x-ms-original-file: specification/subscription/resource-manager/Microsoft.Subscription/stable/2020-09-01/examples/enableSubscription.json
     */
    /**
     * Sample code: enableSubscription.
     *
     * @param manager Entry point to SubscriptionManager.
     */
    public static void enableSubscription(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager
            .subscriptionOperations()
            .enableWithResponse("7948bcee-488c-47ce-941c-38e20ede803d", com.azure.core.util.Context.NONE);
    }
}
```

### SubscriptionOperation_Rename

```java
import com.azure.resourcemanager.subscription.models.SubscriptionName;

/** Samples for SubscriptionOperation Rename. */
public final class SubscriptionOperationRenameSamples {
    /*
     * x-ms-original-file: specification/subscription/resource-manager/Microsoft.Subscription/stable/2020-09-01/examples/renameSubscription.json
     */
    /**
     * Sample code: renameSubscription.
     *
     * @param manager Entry point to SubscriptionManager.
     */
    public static void renameSubscription(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager
            .subscriptionOperations()
            .renameWithResponse(
                "83aa47df-e3e9-49ff-877b-94304bf3d3ad",
                new SubscriptionName().withSubscriptionName("Test Sub"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Subscriptions_Get

```java
/** Samples for Subscriptions Get. */
public final class SubscriptionsGetSamples {
    /*
     * x-ms-original-file: specification/subscription/resource-manager/Microsoft.Subscription/stable/2016-06-01/examples/getSubscription.json
     */
    /**
     * Sample code: getSubscription.
     *
     * @param manager Entry point to SubscriptionManager.
     */
    public static void getSubscription(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager
            .subscriptions()
            .getWithResponse("83aa47df-e3e9-49ff-877b-94304bf3d3ad", com.azure.core.util.Context.NONE);
    }
}
```

### Subscriptions_List

```java
/** Samples for Subscriptions List. */
public final class SubscriptionsListSamples {
    /*
     * x-ms-original-file: specification/subscription/resource-manager/Microsoft.Subscription/stable/2016-06-01/examples/listSubscriptions.json
     */
    /**
     * Sample code: listSubscriptions.
     *
     * @param manager Entry point to SubscriptionManager.
     */
    public static void listSubscriptions(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.subscriptions().list(com.azure.core.util.Context.NONE);
    }
}
```

### Subscriptions_ListLocations

```java
/** Samples for Subscriptions ListLocations. */
public final class SubscriptionsListLocationsSamples {
    /*
     * x-ms-original-file: specification/subscription/resource-manager/Microsoft.Subscription/stable/2016-06-01/examples/listLocations.json
     */
    /**
     * Sample code: listLocations.
     *
     * @param manager Entry point to SubscriptionManager.
     */
    public static void listLocations(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.subscriptions().listLocations("83aa47df-e3e9-49ff-877b-94304bf3d3ad", com.azure.core.util.Context.NONE);
    }
}
```

### Tenants_List

```java
/** Samples for Tenants List. */
public final class TenantsListSamples {
    /*
     * x-ms-original-file: specification/subscription/resource-manager/Microsoft.Subscription/stable/2016-06-01/examples/listTenants.json
     */
    /**
     * Sample code: listTenants.
     *
     * @param manager Entry point to SubscriptionManager.
     */
    public static void listTenants(com.azure.resourcemanager.subscription.SubscriptionManager manager) {
        manager.tenants().list(com.azure.core.util.Context.NONE);
    }
}
```

