# Code snippets and samples


## Operations

- [List](#operations_list)

## ProfessionalServiceOperation

- [Get](#professionalserviceoperation_get)

## ProfessionalServiceSubscriptionLevel

- [CreateOrUpdate](#professionalservicesubscriptionlevel_createorupdate)
- [Delete](#professionalservicesubscriptionlevel_delete)
- [GetByResourceGroup](#professionalservicesubscriptionlevel_getbyresourcegroup)
- [List](#professionalservicesubscriptionlevel_list)
- [ListByResourceGroup](#professionalservicesubscriptionlevel_listbyresourcegroup)
- [UpdateToUnsubscribed](#professionalservicesubscriptionlevel_updatetounsubscribed)
### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/professionalservice/resource-manager/Microsoft.ProfessionalService/preview/2023-07-01-preview/examples/Operation/GetRpOperations.json
     */
    /**
     * Sample code: Get ProfessionalService operations.
     *
     * @param manager Entry point to ProfessionalServiceManager.
     */
    public static void getProfessionalServiceOperations(
        com.azure.resourcemanager.professionalservice.ProfessionalServiceManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ProfessionalServiceOperation_Get

```java
/** Samples for ProfessionalServiceOperation Get. */
public final class ProfessionalServiceOperationGetSamples {
    /*
     * x-ms-original-file: specification/professionalservice/resource-manager/Microsoft.ProfessionalService/preview/2023-07-01-preview/examples/Operation/GetOperation.json
     */
    /**
     * Sample code: Get operation status.
     *
     * @param manager Entry point to ProfessionalServiceManager.
     */
    public static void getOperationStatus(
        com.azure.resourcemanager.professionalservice.ProfessionalServiceManager manager) {
        manager
            .professionalServiceOperations()
            .get("2499e7c2-d251-4a54-9b0f-7673fe325283", com.azure.core.util.Context.NONE);
    }
}
```

### ProfessionalServiceSubscriptionLevel_CreateOrUpdate

```java
import com.azure.resourcemanager.professionalservice.models.ProfessionalServiceCreationProperties;

/** Samples for ProfessionalServiceSubscriptionLevel CreateOrUpdate. */
public final class ProfessionalServiceSubscriptionLevelCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/professionalservice/resource-manager/Microsoft.ProfessionalService/preview/2023-07-01-preview/examples/ProfessionalService/ProfessionalServicePutIndefiniteTerm.json
     */
    /**
     * Sample code: Create subscription level ProfessionalService resource (indefinite term).
     *
     * @param manager Entry point to ProfessionalServiceManager.
     */
    public static void createSubscriptionLevelProfessionalServiceResourceIndefiniteTerm(
        com.azure.resourcemanager.professionalservice.ProfessionalServiceManager manager) {
        manager
            .professionalServiceSubscriptionLevels()
            .define("MyContosoPS")
            .withExistingResourceGroup("c825645b-e31b-9cf4-1cee-2aba9e58bc7c", "my-ps-rg")
            .withRegion("global")
            .withName("MyContosoPS")
            .withProperties(
                new ProfessionalServiceCreationProperties()
                    .withOfferId("testprofservice")
                    .withPublisherId("microsoft-contoso")
                    .withSkuId("ff051f4f-a6d9-4cbc-8d9a-2a41bd468abc")
                    .withQuoteId("quoteabc"))
            .create();
    }

    /*
     * x-ms-original-file: specification/professionalservice/resource-manager/Microsoft.ProfessionalService/preview/2023-07-01-preview/examples/ProfessionalService/ProfessionalServicePutWithTerm.json
     */
    /**
     * Sample code: Create subscription level ProfessionalService resource (with term).
     *
     * @param manager Entry point to ProfessionalServiceManager.
     */
    public static void createSubscriptionLevelProfessionalServiceResourceWithTerm(
        com.azure.resourcemanager.professionalservice.ProfessionalServiceManager manager) {
        manager
            .professionalServiceSubscriptionLevels()
            .define("MyContosoPS")
            .withExistingResourceGroup("c825645b-e31b-9cf4-1cee-2aba9e58bc7c", "my-ps-rg")
            .withRegion("global")
            .withName("MyContosoPS")
            .withProperties(
                new ProfessionalServiceCreationProperties()
                    .withOfferId("testprofservice")
                    .withPublisherId("microsoft-contoso")
                    .withSkuId("ff051f4f-a6d9-4cbc-8d9a-2a41bd468abc")
                    .withQuoteId("quoteabc")
                    .withTermUnit("P3Y")
                    .withBillingPeriod("P1Y"))
            .create();
    }
}
```

### ProfessionalServiceSubscriptionLevel_Delete

```java
/** Samples for ProfessionalServiceSubscriptionLevel Delete. */
public final class ProfessionalServiceSubscriptionLevelDeleteSamples {
    /*
     * x-ms-original-file: specification/professionalservice/resource-manager/Microsoft.ProfessionalService/preview/2023-07-01-preview/examples/ProfessionalService/ProfessionalServiceDelete.json
     */
    /**
     * Sample code: Delete Subscription Level ProfessionalService resource.
     *
     * @param manager Entry point to ProfessionalServiceManager.
     */
    public static void deleteSubscriptionLevelProfessionalServiceResource(
        com.azure.resourcemanager.professionalservice.ProfessionalServiceManager manager) {
        manager
            .professionalServiceSubscriptionLevels()
            .delete(
                "c825645b-e31b-9cf4-1cee-2aba9e58bc7c", "my-ps-rg", "MyContosoPS", com.azure.core.util.Context.NONE);
    }
}
```

### ProfessionalServiceSubscriptionLevel_GetByResourceGroup

```java
/** Samples for ProfessionalServiceSubscriptionLevel GetByResourceGroup. */
public final class ProfessionalServiceSubscriptionLevelGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/professionalservice/resource-manager/Microsoft.ProfessionalService/preview/2023-07-01-preview/examples/ProfessionalService/ProfessionalServiceGet.json
     */
    /**
     * Sample code: Get subscription level ProfessionalService resource.
     *
     * @param manager Entry point to ProfessionalServiceManager.
     */
    public static void getSubscriptionLevelProfessionalServiceResource(
        com.azure.resourcemanager.professionalservice.ProfessionalServiceManager manager) {
        manager
            .professionalServiceSubscriptionLevels()
            .getByResourceGroupWithResponse(
                "c825645b-e31b-9cf4-1cee-2aba9e58bc7c",
                "my-ps-rg",
                "ps_subscription",
                com.azure.core.util.Context.NONE);
    }
}
```

### ProfessionalServiceSubscriptionLevel_List

```java
/** Samples for ProfessionalServiceSubscriptionLevel List. */
public final class ProfessionalServiceSubscriptionLevelListSamples {
    /*
     * x-ms-original-file: specification/professionalservice/resource-manager/Microsoft.ProfessionalService/preview/2023-07-01-preview/examples/ProfessionalService/ProfessionalServiceGetAllInAzureSubscription.json
     */
    /**
     * Sample code: Get subscription level ProfessionalService resources in Azure subscription.
     *
     * @param manager Entry point to ProfessionalServiceManager.
     */
    public static void getSubscriptionLevelProfessionalServiceResourcesInAzureSubscription(
        com.azure.resourcemanager.professionalservice.ProfessionalServiceManager manager) {
        manager
            .professionalServiceSubscriptionLevels()
            .list("c825645b-e31b-9cf4-1cee-2aba9e58bc7c", com.azure.core.util.Context.NONE);
    }
}
```

### ProfessionalServiceSubscriptionLevel_ListByResourceGroup

```java
/** Samples for ProfessionalServiceSubscriptionLevel ListByResourceGroup. */
public final class ProfessionalServiceSubscriptionLevelListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/professionalservice/resource-manager/Microsoft.ProfessionalService/preview/2023-07-01-preview/examples/ProfessionalService/ProfessionalServiceGetAllInResourceGroup.json
     */
    /**
     * Sample code: Get subscription level ProfessionalService resources in resource group.
     *
     * @param manager Entry point to ProfessionalServiceManager.
     */
    public static void getSubscriptionLevelProfessionalServiceResourcesInResourceGroup(
        com.azure.resourcemanager.professionalservice.ProfessionalServiceManager manager) {
        manager
            .professionalServiceSubscriptionLevels()
            .listByResourceGroup("c825645b-e31b-9cf4-1cee-2aba9e58bc7c", "my-ps-rg", com.azure.core.util.Context.NONE);
    }
}
```

### ProfessionalServiceSubscriptionLevel_UpdateToUnsubscribed

```java
import com.azure.resourcemanager.professionalservice.models.DeleteOptions;

/** Samples for ProfessionalServiceSubscriptionLevel UpdateToUnsubscribed. */
public final class ProfessionalServiceSubscriptionLevelUpdateToUnsubscribedSamples {
    /*
     * x-ms-original-file: specification/professionalservice/resource-manager/Microsoft.ProfessionalService/preview/2023-07-01-preview/examples/ProfessionalService/ProfessionalServiceUnsubscribe.json
     */
    /**
     * Sample code: Unsubscribe Subscription Level ProfessionalService resource.
     *
     * @param manager Entry point to ProfessionalServiceManager.
     */
    public static void unsubscribeSubscriptionLevelProfessionalServiceResource(
        com.azure.resourcemanager.professionalservice.ProfessionalServiceManager manager) {
        manager
            .professionalServiceSubscriptionLevels()
            .updateToUnsubscribed(
                "c825645b-e31b-9cf4-1cee-2aba9e58bc7c",
                "my-ps-rg",
                "MyContosoPS",
                new DeleteOptions()
                    .withUnsubscribeOnly(true)
                    .withReasonCode(0.0F)
                    .withFeedback("No longer need this ProfessionalService"),
                com.azure.core.util.Context.NONE);
    }
}
```

