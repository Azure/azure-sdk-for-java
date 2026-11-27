# Code snippets and samples


## Operations

- [List](#operations_list)

## ProfessionalServiceOperationStatuses

- [GetSubscriptionOperationStatus](#professionalserviceoperationstatuses_getsubscriptionoperationstatus)

## ProfessionalServiceSubscriptionLevel

- [CreateOrUpdate](#professionalservicesubscriptionlevel_createorupdate)
- [Delete](#professionalservicesubscriptionlevel_delete)
- [GetByResourceGroup](#professionalservicesubscriptionlevel_getbyresourcegroup)
- [ListByResourceGroup](#professionalservicesubscriptionlevel_listbyresourcegroup)
- [Unsubscribe](#professionalservicesubscriptionlevel_unsubscribe)
- [Update](#professionalservicesubscriptionlevel_update)
- [ValidateUpdate](#professionalservicesubscriptionlevel_validateupdate)

## ProfessionalServiceSubscriptionLevelGlobal

- [CheckNameAvailability](#professionalservicesubscriptionlevelglobal_checknameavailability)
- [List](#professionalservicesubscriptionlevelglobal_list)
- [ValidatePurchase](#professionalservicesubscriptionlevelglobal_validatepurchase)
- [ValidatePurchaseBySubscription](#professionalservicesubscriptionlevelglobal_validatepurchasebysubscription)
### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2026-05-01/ProfessionalServiceGetOperations.json
     */
    /**
     * Sample code: List Professional Service operations.
     * 
     * @param manager Entry point to ProfessionalServiceManager.
     */
    public static void listProfessionalServiceOperations(
        com.azure.resourcemanager.professionalservice.ProfessionalServiceManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ProfessionalServiceOperationStatuses_GetSubscriptionOperationStatus

```java
/**
 * Samples for ProfessionalServiceOperationStatuses GetSubscriptionOperationStatus.
 */
public final class ProfessionalServiceOperationStatusesGetSubscriptionOperationStatusSamples {
    /*
     * x-ms-original-file: 2026-05-01/ProfessionalServiceGetSubscriptionOperationStatus.json
     */
    /**
     * Sample code: Get Professional Service operation status at subscription scope.
     * 
     * @param manager Entry point to ProfessionalServiceManager.
     */
    public static void getProfessionalServiceOperationStatusAtSubscriptionScope(
        com.azure.resourcemanager.professionalservice.ProfessionalServiceManager manager) {
        manager.professionalServiceOperationStatuses()
            .getSubscriptionOperationStatusWithResponse("e24b5b6b-2dfc-41ea-c457-46d0c42494af",
                com.azure.core.util.Context.NONE);
    }
}
```

### ProfessionalServiceSubscriptionLevel_CreateOrUpdate

```java
import com.azure.resourcemanager.professionalservice.models.ProfessionalServiceResourceProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ProfessionalServiceSubscriptionLevel CreateOrUpdate.
 */
public final class ProfessionalServiceSubscriptionLevelCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-05-01/ProfessionalServicePut.json
     */
    /**
     * Sample code: Create a Professional Service subscription level resource.
     * 
     * @param manager Entry point to ProfessionalServiceManager.
     */
    public static void createAProfessionalServiceSubscriptionLevelResource(
        com.azure.resourcemanager.professionalservice.ProfessionalServiceManager manager) {
        manager.professionalServiceSubscriptionLevels()
            .define("myPsResource")
            .withRegion("global")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("environment", "test"))
            .withProperties(new ProfessionalServiceResourceProperties().withDescription("Contoso consulting engagement")
                .withQuantity(1L)
                .withAutoRenew(false)
                .withStorefront("AzurePortal")
                .withQuoteId("11d0b05ae5a5")
                .withProductCode("fakeTokenPlaceholder"))
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

### ProfessionalServiceSubscriptionLevel_Delete

```java
/**
 * Samples for ProfessionalServiceSubscriptionLevel Delete.
 */
public final class ProfessionalServiceSubscriptionLevelDeleteSamples {
    /*
     * x-ms-original-file: 2026-05-01/ProfessionalServiceDelete.json
     */
    /**
     * Sample code: Delete a Professional Service subscription level resource.
     * 
     * @param manager Entry point to ProfessionalServiceManager.
     */
    public static void deleteAProfessionalServiceSubscriptionLevelResource(
        com.azure.resourcemanager.professionalservice.ProfessionalServiceManager manager) {
        manager.professionalServiceSubscriptionLevels()
            .delete("myResourceGroup", "myPsResource", com.azure.core.util.Context.NONE);
    }
}
```

### ProfessionalServiceSubscriptionLevel_GetByResourceGroup

```java
/**
 * Samples for ProfessionalServiceSubscriptionLevel GetByResourceGroup.
 */
public final class ProfessionalServiceSubscriptionLevelGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-05-01/ProfessionalServiceGet.json
     */
    /**
     * Sample code: Get a Professional Service subscription level resource.
     * 
     * @param manager Entry point to ProfessionalServiceManager.
     */
    public static void getAProfessionalServiceSubscriptionLevelResource(
        com.azure.resourcemanager.professionalservice.ProfessionalServiceManager manager) {
        manager.professionalServiceSubscriptionLevels()
            .getByResourceGroupWithResponse("myResourceGroup", "myPsResource", null, com.azure.core.util.Context.NONE);
    }
}
```

### ProfessionalServiceSubscriptionLevel_ListByResourceGroup

```java
/**
 * Samples for ProfessionalServiceSubscriptionLevel ListByResourceGroup.
 */
public final class ProfessionalServiceSubscriptionLevelListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-05-01/ProfessionalServiceListByResourceGroup.json
     */
    /**
     * Sample code: List Professional Service subscription level resources by resource group.
     * 
     * @param manager Entry point to ProfessionalServiceManager.
     */
    public static void listProfessionalServiceSubscriptionLevelResourcesByResourceGroup(
        com.azure.resourcemanager.professionalservice.ProfessionalServiceManager manager) {
        manager.professionalServiceSubscriptionLevels()
            .listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### ProfessionalServiceSubscriptionLevel_Unsubscribe

```java
import com.azure.resourcemanager.professionalservice.models.DeletionSource;
import com.azure.resourcemanager.professionalservice.models.UnsubscribeOptions;

/**
 * Samples for ProfessionalServiceSubscriptionLevel Unsubscribe.
 */
public final class ProfessionalServiceSubscriptionLevelUnsubscribeSamples {
    /*
     * x-ms-original-file: 2026-05-01/ProfessionalServiceUnsubscribe.json
     */
    /**
     * Sample code: Unsubscribe from a Professional Service subscription level resource.
     * 
     * @param manager Entry point to ProfessionalServiceManager.
     */
    public static void unsubscribeFromAProfessionalServiceSubscriptionLevelResource(
        com.azure.resourcemanager.professionalservice.ProfessionalServiceManager manager) {
        manager.professionalServiceSubscriptionLevels()
            .unsubscribe("myResourceGroup", "myPsResource",
                new UnsubscribeOptions().withUnsubscribeOnly(true)
                    .withReasonCode(4)
                    .withFeedback("Engagement complete")
                    .withDeletionSource(DeletionSource.CUSTOMER_CANCELLATION),
                com.azure.core.util.Context.NONE);
    }
}
```

### ProfessionalServiceSubscriptionLevel_Update

```java
import com.azure.resourcemanager.professionalservice.models.ProfessionalServiceResource;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ProfessionalServiceSubscriptionLevel Update.
 */
public final class ProfessionalServiceSubscriptionLevelUpdateSamples {
    /*
     * x-ms-original-file: 2026-05-01/ProfessionalServiceUpdate.json
     */
    /**
     * Sample code: Update a Professional Service subscription level resource.
     * 
     * @param manager Entry point to ProfessionalServiceManager.
     */
    public static void updateAProfessionalServiceSubscriptionLevelResource(
        com.azure.resourcemanager.professionalservice.ProfessionalServiceManager manager) {
        ProfessionalServiceResource resource = manager.professionalServiceSubscriptionLevels()
            .getByResourceGroupWithResponse("myResourceGroup", "myPsResource", null, com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("environment", "production", "costCenter", "CC-4820")).apply();
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

### ProfessionalServiceSubscriptionLevel_ValidateUpdate

```java
import com.azure.resourcemanager.professionalservice.models.ProfessionalServiceResourceUpdate;
import com.azure.resourcemanager.professionalservice.models.ProfessionalServiceResourceUpdateProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ProfessionalServiceSubscriptionLevel ValidateUpdate.
 */
public final class ProfessionalServiceSubscriptionLevelValidateUpdateSamples {
    /*
     * x-ms-original-file: 2026-05-01/ProfessionalServiceValidateUpdate.json
     */
    /**
     * Sample code: Validate an update to an existing Professional Service resource.
     * 
     * @param manager Entry point to ProfessionalServiceManager.
     */
    public static void validateAnUpdateToAnExistingProfessionalServiceResource(
        com.azure.resourcemanager.professionalservice.ProfessionalServiceManager manager) {
        manager.professionalServiceSubscriptionLevels()
            .validateUpdateWithResponse("myResourceGroup", "myPsResource",
                new ProfessionalServiceResourceUpdate()
                    .withTags(mapOf("environment", "production", "costCenter", "CC-4820"))
                    .withProperties(new ProfessionalServiceResourceUpdateProperties().withQuantity(2L)),
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

### ProfessionalServiceSubscriptionLevelGlobal_CheckNameAvailability

```java
import com.azure.resourcemanager.professionalservice.models.CheckNameAvailabilityRequest;

/**
 * Samples for ProfessionalServiceSubscriptionLevelGlobal CheckNameAvailability.
 */
public final class ProfessionalServiceSubscriptionLevelGlobalCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: 2026-05-01/ProfessionalServiceCheckNameAvailability.json
     */
    /**
     * Sample code: Check name availability for a Professional Service resource.
     * 
     * @param manager Entry point to ProfessionalServiceManager.
     */
    public static void checkNameAvailabilityForAProfessionalServiceResource(
        com.azure.resourcemanager.professionalservice.ProfessionalServiceManager manager) {
        manager.professionalServiceSubscriptionLevelGlobals()
            .checkNameAvailabilityWithResponse(new CheckNameAvailabilityRequest().withName(
                "/subscriptions/1a950b42-75c2-44a6-ab02-fc0f840997aa/resourceGroups/myResourceGroup/providers/Microsoft.ProfessionalService/resources/myPsResource")
                .withType("Microsoft.ProfessionalService/resources"), com.azure.core.util.Context.NONE);
    }
}
```

### ProfessionalServiceSubscriptionLevelGlobal_List

```java
/**
 * Samples for ProfessionalServiceSubscriptionLevelGlobal List.
 */
public final class ProfessionalServiceSubscriptionLevelGlobalListSamples {
    /*
     * x-ms-original-file: 2026-05-01/ProfessionalServiceListBySubscription.json
     */
    /**
     * Sample code: List Professional Service subscription level resources by Azure subscription.
     * 
     * @param manager Entry point to ProfessionalServiceManager.
     */
    public static void listProfessionalServiceSubscriptionLevelResourcesByAzureSubscription(
        com.azure.resourcemanager.professionalservice.ProfessionalServiceManager manager) {
        manager.professionalServiceSubscriptionLevelGlobals().list(com.azure.core.util.Context.NONE);
    }
}
```

### ProfessionalServiceSubscriptionLevelGlobal_ValidatePurchase

```java
import com.azure.resourcemanager.professionalservice.models.ProfessionalServicePurchaseValidationProperties;
import com.azure.resourcemanager.professionalservice.models.ProfessionalServicePurchaseValidationRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ProfessionalServiceSubscriptionLevelGlobal ValidatePurchase.
 */
public final class ProfessionalServiceSubscriptionLevelGlobalValidatePurchaseSamples {
    /*
     * x-ms-original-file: 2026-05-01/ProfessionalServiceValidatePurchaseByResourceGroup.json
     */
    /**
     * Sample code: Validate a Professional Service purchase within a resource group.
     * 
     * @param manager Entry point to ProfessionalServiceManager.
     */
    public static void validateAProfessionalServicePurchaseWithinAResourceGroup(
        com.azure.resourcemanager.professionalservice.ProfessionalServiceManager manager) {
        manager.professionalServiceSubscriptionLevelGlobals()
            .validatePurchaseWithResponse("myResourceGroup",
                new ProfessionalServicePurchaseValidationRequest().withLocation("global")
                    .withTags(mapOf("environment", "production"))
                    .withProperties(new ProfessionalServicePurchaseValidationProperties()
                        .withDescription("Contoso consulting engagement")
                        .withQuantity(1L)
                        .withAutoRenew(false)
                        .withStorefront("AzurePortal")
                        .withQuoteId("11d0b05ae5a5")
                        .withProductCode("fakeTokenPlaceholder")
                        .withName("myPsResource")),
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

### ProfessionalServiceSubscriptionLevelGlobal_ValidatePurchaseBySubscription

```java
import com.azure.resourcemanager.professionalservice.models.ProfessionalServicePurchaseValidationProperties;
import com.azure.resourcemanager.professionalservice.models.ProfessionalServicePurchaseValidationRequest;

/**
 * Samples for ProfessionalServiceSubscriptionLevelGlobal ValidatePurchaseBySubscription.
 */
public final class ProfessionalServiceSubscriptionLevelGlobalValidatePurchaseBySubscriptionSamples {
    /*
     * x-ms-original-file: 2026-05-01/ProfessionalServiceValidatePurchaseBySubscription.json
     */
    /**
     * Sample code: Validate a Professional Service purchase at subscription scope.
     * 
     * @param manager Entry point to ProfessionalServiceManager.
     */
    public static void validateAProfessionalServicePurchaseAtSubscriptionScope(
        com.azure.resourcemanager.professionalservice.ProfessionalServiceManager manager) {
        manager.professionalServiceSubscriptionLevelGlobals()
            .validatePurchaseBySubscriptionWithResponse(
                new ProfessionalServicePurchaseValidationRequest().withLocation("global")
                    .withProperties(new ProfessionalServicePurchaseValidationProperties().withQuantity(1L)
                        .withAutoRenew(false)
                        .withStorefront("AzurePortal")
                        .withQuoteId("11d0b05ae5a5")
                        .withProductCode("fakeTokenPlaceholder")
                        .withName("myPsResource")),
                com.azure.core.util.Context.NONE);
    }
}
```

