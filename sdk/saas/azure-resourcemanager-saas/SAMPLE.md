# Code snippets and samples


## Operations

- [List](#operations_list)

## SaasOperationStatuses

- [GetSubscriptionOperationStatus](#saasoperationstatuses_getsubscriptionoperationstatus)
- [GetTenantOperationStatus](#saasoperationstatuses_gettenantoperationstatus)

## SaasSubscriptionLevel

- [CreateOrUpdate](#saassubscriptionlevel_createorupdate)
- [Delete](#saassubscriptionlevel_delete)
- [GetByResourceGroup](#saassubscriptionlevel_getbyresourcegroup)
- [ListAccessToken](#saassubscriptionlevel_listaccesstoken)
- [ListByResourceGroup](#saassubscriptionlevel_listbyresourcegroup)
- [Unsubscribe](#saassubscriptionlevel_unsubscribe)
- [Update](#saassubscriptionlevel_update)
- [ValidateUpdate](#saassubscriptionlevel_validateupdate)

## SaasSubscriptionLevelGlobal

- [CheckNameAvailability](#saassubscriptionlevelglobal_checknameavailability)
- [List](#saassubscriptionlevelglobal_list)
- [ValidatePurchase](#saassubscriptionlevelglobal_validatepurchase)
- [ValidatePurchaseBySubscription](#saassubscriptionlevelglobal_validatepurchasebysubscription)

## SaasTenantLevel

- [DeleteResource](#saastenantlevel_deleteresource)
- [GetResource](#saastenantlevel_getresource)
- [List](#saastenantlevel_list)
- [ListAccessToken](#saastenantlevel_listaccesstoken)
- [UpdateResource](#saastenantlevel_updateresource)
### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2026-04-01/SaasGetOperations.json
     */
    /**
     * Sample code: List SaaS operations.
     * 
     * @param manager Entry point to SaaSManager.
     */
    public static void listSaaSOperations(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### SaasOperationStatuses_GetSubscriptionOperationStatus

```java
/**
 * Samples for SaasOperationStatuses GetSubscriptionOperationStatus.
 */
public final class SaasOperationStatusesGetSubscriptionOperationStatusSamples {
    /*
     * x-ms-original-file: 2026-04-01/SaasGetSubscriptionOperationStatus.json
     */
    /**
     * Sample code: Get SaaS operation status at subscription scope.
     * 
     * @param manager Entry point to SaaSManager.
     */
    public static void getSaaSOperationStatusAtSubscriptionScope(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.saasOperationStatuses()
            .getSubscriptionOperationStatusWithResponse("e24b5b6b-2dfc-41ea-c457-46d0c42494af",
                com.azure.core.util.Context.NONE);
    }
}
```

### SaasOperationStatuses_GetTenantOperationStatus

```java
/**
 * Samples for SaasOperationStatuses GetTenantOperationStatus.
 */
public final class SaasOperationStatusesGetTenantOperationStatusSamples {
    /*
     * x-ms-original-file: 2026-04-01/SaasGetTenantOperationStatus.json
     */
    /**
     * Sample code: Get SaaS operation status at tenant scope.
     * 
     * @param manager Entry point to SaaSManager.
     */
    public static void getSaaSOperationStatusAtTenantScope(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.saasOperationStatuses()
            .getTenantOperationStatusWithResponse("e24b5b6b-2dfc-41ea-c457-46d0c42494af",
                com.azure.core.util.Context.NONE);
    }
}
```

### SaasSubscriptionLevel_CreateOrUpdate

```java
import com.azure.resourcemanager.saas.models.SaasResourceProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SaasSubscriptionLevel CreateOrUpdate.
 */
public final class SaasSubscriptionLevelCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-04-01/SaasPut.json
     */
    /**
     * Sample code: Create a SaaS subscription level resource.
     * 
     * @param manager Entry point to SaaSManager.
     */
    public static void createASaaSSubscriptionLevelResource(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.saasSubscriptionLevels()
            .define("mySaasResource")
            .withRegion("global")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("environment", "test"))
            .withProperties(new SaasResourceProperties().withDescription("Contoso SaaS seat subscription")
                .withQuantity(1L)
                .withAutoRenew(true)
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

### SaasSubscriptionLevel_Delete

```java
/**
 * Samples for SaasSubscriptionLevel Delete.
 */
public final class SaasSubscriptionLevelDeleteSamples {
    /*
     * x-ms-original-file: 2026-04-01/SaasDelete.json
     */
    /**
     * Sample code: Delete a SaaS subscription level resource.
     * 
     * @param manager Entry point to SaaSManager.
     */
    public static void deleteASaaSSubscriptionLevelResource(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.saasSubscriptionLevels().delete("myResourceGroup", "mySaasResource", com.azure.core.util.Context.NONE);
    }
}
```

### SaasSubscriptionLevel_GetByResourceGroup

```java
/**
 * Samples for SaasSubscriptionLevel GetByResourceGroup.
 */
public final class SaasSubscriptionLevelGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-04-01/SaasGet.json
     */
    /**
     * Sample code: Get a SaaS subscription level resource.
     * 
     * @param manager Entry point to SaaSManager.
     */
    public static void getASaaSSubscriptionLevelResource(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.saasSubscriptionLevels()
            .getByResourceGroupWithResponse("myResourceGroup", "mySaasResource", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### SaasSubscriptionLevel_ListAccessToken

```java
/**
 * Samples for SaasSubscriptionLevel ListAccessToken.
 */
public final class SaasSubscriptionLevelListAccessTokenSamples {
    /*
     * x-ms-original-file: 2026-04-01/SaasListAccessToken.json
     */
    /**
     * Sample code: List access token for a SaaS subscription level resource.
     * 
     * @param manager Entry point to SaaSManager.
     */
    public static void
        listAccessTokenForASaaSSubscriptionLevelResource(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.saasSubscriptionLevels()
            .listAccessTokenWithResponse("myResourceGroup", "mySaasResource", com.azure.core.util.Context.NONE);
    }
}
```

### SaasSubscriptionLevel_ListByResourceGroup

```java
/**
 * Samples for SaasSubscriptionLevel ListByResourceGroup.
 */
public final class SaasSubscriptionLevelListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-04-01/SaasListByResourceGroup.json
     */
    /**
     * Sample code: List SaaS subscription level resources by resource group.
     * 
     * @param manager Entry point to SaaSManager.
     */
    public static void
        listSaaSSubscriptionLevelResourcesByResourceGroup(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.saasSubscriptionLevels().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### SaasSubscriptionLevel_Unsubscribe

```java
import com.azure.resourcemanager.saas.models.DeletionSource;
import com.azure.resourcemanager.saas.models.UnsubscribeOptions;

/**
 * Samples for SaasSubscriptionLevel Unsubscribe.
 */
public final class SaasSubscriptionLevelUnsubscribeSamples {
    /*
     * x-ms-original-file: 2026-04-01/SaasUnsubscribe.json
     */
    /**
     * Sample code: Unsubscribe from a SaaS subscription level resource.
     * 
     * @param manager Entry point to SaaSManager.
     */
    public static void
        unsubscribeFromASaaSSubscriptionLevelResource(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.saasSubscriptionLevels()
            .unsubscribe("myResourceGroup", "mySaasResource",
                new UnsubscribeOptions().withUnsubscribeOnly(true)
                    .withReasonCode(4)
                    .withFeedback("No longer needed")
                    .withDeletionSource(DeletionSource.CUSTOMER_CANCELLATION),
                com.azure.core.util.Context.NONE);
    }
}
```

### SaasSubscriptionLevel_Update

```java
import com.azure.resourcemanager.saas.models.PatchIntent;
import com.azure.resourcemanager.saas.models.SaasResource;
import com.azure.resourcemanager.saas.models.SaasResourceUpdateProperties;

/**
 * Samples for SaasSubscriptionLevel Update.
 */
public final class SaasSubscriptionLevelUpdateSamples {
    /*
     * x-ms-original-file: 2026-04-01/SaasPatch.json
     */
    /**
     * Sample code: Change plan.
     * 
     * @param manager Entry point to SaaSManager.
     */
    public static void changePlan(com.azure.resourcemanager.saas.SaaSManager manager) {
        SaasResource resource = manager.saasSubscriptionLevels()
            .getByResourceGroupWithResponse("myResourceGroup", "mySaasResource", null, com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new SaasResourceUpdateProperties().withProductCode("fakeTokenPlaceholder")
                .withIntent(PatchIntent.RETAIN))
            .apply();
    }

    /*
     * x-ms-original-file: 2026-04-01/SaasPatchQuantity.json
     */
    /**
     * Sample code: Change quantity.
     * 
     * @param manager Entry point to SaaSManager.
     */
    public static void changeQuantity(com.azure.resourcemanager.saas.SaaSManager manager) {
        SaasResource resource = manager.saasSubscriptionLevels()
            .getByResourceGroupWithResponse("myResourceGroup", "mySaasResource", null, com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withProperties(new SaasResourceUpdateProperties().withQuantity(5L)).apply();
    }
}
```

### SaasSubscriptionLevel_ValidateUpdate

```java
import com.azure.resourcemanager.saas.models.PatchIntent;
import com.azure.resourcemanager.saas.models.SaasResourceUpdate;
import com.azure.resourcemanager.saas.models.SaasResourceUpdateProperties;

/**
 * Samples for SaasSubscriptionLevel ValidateUpdate.
 */
public final class SaasSubscriptionLevelValidateUpdateSamples {
    /*
     * x-ms-original-file: 2026-04-01/SaasValidateUpdate.json
     */
    /**
     * Sample code: Validate an update to an existing SaaS resource.
     * 
     * @param manager Entry point to SaaSManager.
     */
    public static void validateAnUpdateToAnExistingSaaSResource(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.saasSubscriptionLevels()
            .validateUpdateWithResponse("myResourceGroup", "mySaasResource",
                new SaasResourceUpdate().withProperties(new SaasResourceUpdateProperties().withQuantity(10L)
                    .withProductCode("fakeTokenPlaceholder")
                    .withIntent(PatchIntent.RETAIN)),
                com.azure.core.util.Context.NONE);
    }
}
```

### SaasSubscriptionLevelGlobal_CheckNameAvailability

```java
import com.azure.resourcemanager.saas.models.CheckNameAvailabilityRequest;

/**
 * Samples for SaasSubscriptionLevelGlobal CheckNameAvailability.
 */
public final class SaasSubscriptionLevelGlobalCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: 2026-04-01/SaasCheckNameAvailability.json
     */
    /**
     * Sample code: Check name availability for a SaaS resource.
     * 
     * @param manager Entry point to SaaSManager.
     */
    public static void checkNameAvailabilityForASaaSResource(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.saasSubscriptionLevelGlobals()
            .checkNameAvailabilityWithResponse(new CheckNameAvailabilityRequest().withName(
                "/subscriptions/1a950b42-75c2-44a6-ab02-fc0f840997aa/resourceGroups/myResourceGroup/providers/Microsoft.SaaS/resources/mySaasResource")
                .withType("Microsoft.SaaS/resources"), com.azure.core.util.Context.NONE);
    }
}
```

### SaasSubscriptionLevelGlobal_List

```java
/**
 * Samples for SaasSubscriptionLevelGlobal List.
 */
public final class SaasSubscriptionLevelGlobalListSamples {
    /*
     * x-ms-original-file: 2026-04-01/SaasListBySubscription.json
     */
    /**
     * Sample code: List SaaS subscription level resources by Azure subscription.
     * 
     * @param manager Entry point to SaaSManager.
     */
    public static void
        listSaaSSubscriptionLevelResourcesByAzureSubscription(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.saasSubscriptionLevelGlobals().list(com.azure.core.util.Context.NONE);
    }
}
```

### SaasSubscriptionLevelGlobal_ValidatePurchase

```java
import com.azure.resourcemanager.saas.models.SaasPurchaseValidationProperties;
import com.azure.resourcemanager.saas.models.SaasPurchaseValidationRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SaasSubscriptionLevelGlobal ValidatePurchase.
 */
public final class SaasSubscriptionLevelGlobalValidatePurchaseSamples {
    /*
     * x-ms-original-file: 2026-04-01/SaasValidatePurchaseByResourceGroup.json
     */
    /**
     * Sample code: Validate a SaaS purchase within a resource group.
     * 
     * @param manager Entry point to SaaSManager.
     */
    public static void validateASaaSPurchaseWithinAResourceGroup(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.saasSubscriptionLevelGlobals()
            .validatePurchaseWithResponse("myResourceGroup", new SaasPurchaseValidationRequest().withLocation("global")
                .withTags(mapOf("environment", "production"))
                .withProperties(new SaasPurchaseValidationProperties().withDescription("Contoso SaaS seat subscription")
                    .withQuantity(5L)
                    .withAutoRenew(true)
                    .withStorefront("AzurePortal")
                    .withQuoteId("11d0b05ae5a5")
                    .withProductCode("fakeTokenPlaceholder")
                    .withName("mySaasResource")),
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

### SaasSubscriptionLevelGlobal_ValidatePurchaseBySubscription

```java
import com.azure.resourcemanager.saas.models.SaasPurchaseValidationProperties;
import com.azure.resourcemanager.saas.models.SaasPurchaseValidationRequest;

/**
 * Samples for SaasSubscriptionLevelGlobal ValidatePurchaseBySubscription.
 */
public final class SaasSubscriptionLevelGlobalValidatePurchaseBySubscriptionSamples {
    /*
     * x-ms-original-file: 2026-04-01/SaasValidatePurchaseBySubscription.json
     */
    /**
     * Sample code: Validate a SaaS purchase at subscription scope.
     * 
     * @param manager Entry point to SaaSManager.
     */
    public static void validateASaaSPurchaseAtSubscriptionScope(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.saasSubscriptionLevelGlobals()
            .validatePurchaseBySubscriptionWithResponse(new SaasPurchaseValidationRequest().withLocation("global")
                .withProperties(new SaasPurchaseValidationProperties().withQuantity(1L)
                    .withAutoRenew(true)
                    .withStorefront("AzurePortal")
                    .withProductCode("fakeTokenPlaceholder")
                    .withName("mySaasResource")),
                com.azure.core.util.Context.NONE);
    }
}
```

### SaasTenantLevel_DeleteResource

```java
/**
 * Samples for SaasTenantLevel DeleteResource.
 */
public final class SaasTenantLevelDeleteResourceSamples {
    /*
     * x-ms-original-file: 2026-04-01/SaasTenantDelete.json
     */
    /**
     * Sample code: Delete a tenant-level SaaS resource.
     * 
     * @param manager Entry point to SaaSManager.
     */
    public static void deleteATenantLevelSaaSResource(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.saasTenantLevels()
            .deleteResource("e24b5b6b-2dfc-41ea-c457-46d0c42494af", com.azure.core.util.Context.NONE);
    }
}
```

### SaasTenantLevel_GetResource

```java
/**
 * Samples for SaasTenantLevel GetResource.
 */
public final class SaasTenantLevelGetResourceSamples {
    /*
     * x-ms-original-file: 2026-04-01/SaasTenantGet.json
     */
    /**
     * Sample code: Get a tenant-level SaaS resource.
     * 
     * @param manager Entry point to SaaSManager.
     */
    public static void getATenantLevelSaaSResource(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.saasTenantLevels()
            .getResourceWithResponse("e24b5b6b-2dfc-41ea-c457-46d0c42494af", null, com.azure.core.util.Context.NONE);
    }
}
```

### SaasTenantLevel_List

```java
/**
 * Samples for SaasTenantLevel List.
 */
public final class SaasTenantLevelListSamples {
    /*
     * x-ms-original-file: 2026-04-01/SaasTenantList.json
     */
    /**
     * Sample code: List tenant-level SaaS resources.
     * 
     * @param manager Entry point to SaaSManager.
     */
    public static void listTenantLevelSaaSResources(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.saasTenantLevels().list(com.azure.core.util.Context.NONE);
    }
}
```

### SaasTenantLevel_ListAccessToken

```java
/**
 * Samples for SaasTenantLevel ListAccessToken.
 */
public final class SaasTenantLevelListAccessTokenSamples {
    /*
     * x-ms-original-file: 2026-04-01/SaasTenantListAccessToken.json
     */
    /**
     * Sample code: List access token for a tenant-level SaaS resource.
     * 
     * @param manager Entry point to SaaSManager.
     */
    public static void listAccessTokenForATenantLevelSaaSResource(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.saasTenantLevels()
            .listAccessTokenWithResponse("e24b5b6b-2dfc-41ea-c457-46d0c42494af", com.azure.core.util.Context.NONE);
    }
}
```

### SaasTenantLevel_UpdateResource

```java
import com.azure.resourcemanager.saas.models.PatchIntent;
import com.azure.resourcemanager.saas.models.SaasTenantResourceUpdate;
import com.azure.resourcemanager.saas.models.SaasTenantResourceUpdateProperties;

/**
 * Samples for SaasTenantLevel UpdateResource.
 */
public final class SaasTenantLevelUpdateResourceSamples {
    /*
     * x-ms-original-file: 2026-04-01/SaasTenantPatch.json
     */
    /**
     * Sample code: Update a tenant-level SaaS resource.
     * 
     * @param manager Entry point to SaaSManager.
     */
    public static void updateATenantLevelSaaSResource(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.saasTenantLevels()
            .updateResource("e24b5b6b-2dfc-41ea-c457-46d0c42494af",
                new SaasTenantResourceUpdate()
                    .withProperties(new SaasTenantResourceUpdateProperties().withProductCode("fakeTokenPlaceholder")
                        .withIntent(PatchIntent.RETAIN)),
                com.azure.core.util.Context.NONE);
    }
}
```

