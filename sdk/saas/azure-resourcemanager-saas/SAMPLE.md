# Code snippets and samples


## Applications

- [ListByResourceGroup](#applications_listbyresourcegroup)

## Operations

- [List](#operations_list)

## ResourceProvider

- [SaasResourceListAccessToken](#resourceprovider_saasresourcelistaccesstoken)

## SaaS

- [CreateResource](#saas_createresource)
- [Delete](#saas_delete)
- [GetResource](#saas_getresource)
- [UpdateResource](#saas_updateresource)

## SaaSOperation

- [Get](#saasoperation_get)

## SaasResources

- [List](#saasresources_list)

## SaasSubscriptionLevel

- [CreateOrUpdate](#saassubscriptionlevel_createorupdate)
- [Delete](#saassubscriptionlevel_delete)
- [GetByResourceGroup](#saassubscriptionlevel_getbyresourcegroup)
- [List](#saassubscriptionlevel_list)
- [ListAccessToken](#saassubscriptionlevel_listaccesstoken)
- [ListByResourceGroup](#saassubscriptionlevel_listbyresourcegroup)
- [MoveResources](#saassubscriptionlevel_moveresources)
- [Update](#saassubscriptionlevel_update)
- [UpdateToUnsubscribed](#saassubscriptionlevel_updatetounsubscribed)
- [ValidateMoveResources](#saassubscriptionlevel_validatemoveresources)
### Applications_ListByResourceGroup

```java
/** Samples for Applications ListByResourceGroup. */
public final class ApplicationsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/saas/resource-manager/Microsoft.SaaS/preview/2018-03-01-beta/examples/saasV1/SaaSGetApplications.json
     */
    /**
     * Sample code: Get saas application.
     *
     * @param manager Entry point to SaaSManager.
     */
    public static void getSaasApplication(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.applications().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/saas/resource-manager/Microsoft.SaaS/preview/2018-03-01-beta/examples/saasV1/SaaSGetOperations.json
     */
    /**
     * Sample code: Get saas operations.
     *
     * @param manager Entry point to SaaSManager.
     */
    public static void getSaasOperations(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_SaasResourceListAccessToken

```java
/** Samples for ResourceProvider SaasResourceListAccessToken. */
public final class ResourceProviderSaasResourceListAccessTokenSamples {
    /*
     * x-ms-original-file: specification/saas/resource-manager/Microsoft.SaaS/preview/2018-03-01-beta/examples/saasV2/ListAccessTokenPost.json
     */
    /**
     * Sample code: generated SaaS resource token.
     *
     * @param manager Entry point to SaaSManager.
     */
    public static void generatedSaaSResourceToken(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager
            .resourceProviders()
            .saasResourceListAccessTokenWithResponse(
                "c825645b-e31b-9cf4-1cee-2aba9e58bc7c", com.azure.core.util.Context.NONE);
    }
}
```

### SaaS_CreateResource

```java
import com.azure.resourcemanager.saas.models.PaymentChannelType;
import com.azure.resourcemanager.saas.models.SaasCreationProperties;
import com.azure.resourcemanager.saas.models.SaasResourceCreation;
import java.util.HashMap;
import java.util.Map;

/** Samples for SaaS CreateResource. */
public final class SaaSCreateResourceSamples {
    /*
     * x-ms-original-file: specification/saas/resource-manager/Microsoft.SaaS/preview/2018-03-01-beta/examples/saasV2/SaasPut.json
     */
    /**
     * Sample code: Create SaaS resource.
     *
     * @param manager Entry point to SaaSManager.
     */
    public static void createSaaSResource(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager
            .saaS()
            .createResource(
                new SaasResourceCreation()
                    .withProperties(
                        new SaasCreationProperties()
                            .withOfferId("microsofthealthcarebot")
                            .withPublisherId("microsoft-hcb")
                            .withSkuId("free")
                            .withPaymentChannelType(PaymentChannelType.SUBSCRIPTION_DELEGATED)
                            .withPaymentChannelMetadata(
                                mapOf("AzureSubscriptionId", "155af98a-3205-47e7-883b-a2ab9db9f88d"))
                            .withSaasResourceName("testRunnerFromArm")
                            .withTermId("hjdtn7tfnxcy")),
                com.azure.core.util.Context.NONE);
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

### SaaS_Delete

```java
import com.azure.resourcemanager.saas.models.DeleteOptions;

/** Samples for SaaS Delete. */
public final class SaaSDeleteSamples {
    /*
     * x-ms-original-file: specification/saas/resource-manager/Microsoft.SaaS/preview/2018-03-01-beta/examples/saasV2/SaasDelete.json
     */
    /**
     * Sample code: Delete SaaS resource.
     *
     * @param manager Entry point to SaaSManager.
     */
    public static void deleteSaaSResource(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager
            .saaS()
            .delete(
                "115c3523-1fae-757f-af86-7b27cfd29805",
                new DeleteOptions().withUnsubscribeOnly(true).withReasonCode(0.0F),
                com.azure.core.util.Context.NONE);
    }
}
```

### SaaS_GetResource

```java
/** Samples for SaaS GetResource. */
public final class SaaSGetResourceSamples {
    /*
     * x-ms-original-file: specification/saas/resource-manager/Microsoft.SaaS/preview/2018-03-01-beta/examples/saasV2/SaasGet.json
     */
    /**
     * Sample code: Get SaaS resource.
     *
     * @param manager Entry point to SaaSManager.
     */
    public static void getSaaSResource(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager
            .saaS()
            .getResourceWithResponse("115c3523-1fae-757f-af86-7b27cfd29805", com.azure.core.util.Context.NONE);
    }
}
```

### SaaS_UpdateResource

```java
import com.azure.resourcemanager.saas.models.SaasCreationProperties;
import com.azure.resourcemanager.saas.models.SaasResourceCreation;
import java.util.HashMap;
import java.util.Map;

/** Samples for SaaS UpdateResource. */
public final class SaaSUpdateResourceSamples {
    /*
     * x-ms-original-file: specification/saas/resource-manager/Microsoft.SaaS/preview/2018-03-01-beta/examples/saasV2/SaasPatch.json
     */
    /**
     * Sample code: Update SaaS resource.
     *
     * @param manager Entry point to SaaSManager.
     */
    public static void updateSaaSResource(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager
            .saaS()
            .updateResource(
                "115c3523-1fae-757f-af86-7b27cfd29805",
                new SaasResourceCreation()
                    .withTags(mapOf())
                    .withProperties(new SaasCreationProperties().withSkuId("premium")),
                com.azure.core.util.Context.NONE);
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

### SaaSOperation_Get

```java
/** Samples for SaaSOperation Get. */
public final class SaaSOperationGetSamples {
    /*
     * x-ms-original-file: specification/saas/resource-manager/Microsoft.SaaS/preview/2018-03-01-beta/examples/OperationResults/Get.json
     */
    /**
     * Sample code: Get operation status.
     *
     * @param manager Entry point to SaaSManager.
     */
    public static void getOperationStatus(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.saaSOperations().get("5f35cb4c-8065-45b3-9116-5ba335462e95", com.azure.core.util.Context.NONE);
    }
}
```

### SaasResources_List

```java
/** Samples for SaasResources List. */
public final class SaasResourcesListSamples {
    /*
     * x-ms-original-file: specification/saas/resource-manager/Microsoft.SaaS/preview/2018-03-01-beta/examples/saasV2/SaaSGetAllResources.json
     */
    /**
     * Sample code: Get all SaaS resources.
     *
     * @param manager Entry point to SaaSManager.
     */
    public static void getAllSaaSResources(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.saasResources().list(com.azure.core.util.Context.NONE);
    }
}
```

### SaasSubscriptionLevel_CreateOrUpdate

```java
import com.azure.resourcemanager.saas.models.PaymentChannelType;
import com.azure.resourcemanager.saas.models.SaasCreationProperties;
import java.util.HashMap;
import java.util.Map;

/** Samples for SaasSubscriptionLevel CreateOrUpdate. */
public final class SaasSubscriptionLevelCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/saas/resource-manager/Microsoft.SaaS/preview/2018-03-01-beta/examples/saasSubscriptionLevel/SaasPut.json
     */
    /**
     * Sample code: Create subscription level SaaS resource.
     *
     * @param manager Entry point to SaaSManager.
     */
    public static void createSubscriptionLevelSaaSResource(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager
            .saasSubscriptionLevels()
            .define("MyContosoSubscription")
            .withExistingResourceGroup("my-saas-rg")
            .withRegion("global")
            .withName("MyContosoSubscription")
            .withProperties(
                new SaasCreationProperties()
                    .withOfferId("contosoOffer")
                    .withPublisherId("microsoft-contoso")
                    .withSkuId("free")
                    .withPaymentChannelType(PaymentChannelType.SUBSCRIPTION_DELEGATED)
                    .withPaymentChannelMetadata(mapOf("AzureSubscriptionId", "155af98a-3205-47e7-883b-a2ab9db9f88d"))
                    .withSaasResourceName("MyContosoSubscription")
                    .withTermId("hjdtn7tfnxcy"))
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

### SaasSubscriptionLevel_Delete

```java
/** Samples for SaasSubscriptionLevel Delete. */
public final class SaasSubscriptionLevelDeleteSamples {
    /*
     * x-ms-original-file: specification/saas/resource-manager/Microsoft.SaaS/preview/2018-03-01-beta/examples/saasSubscriptionLevel/SaasDelete.json
     */
    /**
     * Sample code: Delete Subscription Level SaaS resource.
     *
     * @param manager Entry point to SaaSManager.
     */
    public static void deleteSubscriptionLevelSaaSResource(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager
            .saasSubscriptionLevels()
            .delete("my-saas-rg", "MyContosoSubscription", com.azure.core.util.Context.NONE);
    }
}
```

### SaasSubscriptionLevel_GetByResourceGroup

```java
/** Samples for SaasSubscriptionLevel GetByResourceGroup. */
public final class SaasSubscriptionLevelGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/saas/resource-manager/Microsoft.SaaS/preview/2018-03-01-beta/examples/saasSubscriptionLevel/SaasGet.json
     */
    /**
     * Sample code: Get subscription level saas resource.
     *
     * @param manager Entry point to SaaSManager.
     */
    public static void getSubscriptionLevelSaasResource(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager
            .saasSubscriptionLevels()
            .getByResourceGroupWithResponse("my-saas-rg", "MyContosoSubscription", com.azure.core.util.Context.NONE);
    }
}
```

### SaasSubscriptionLevel_List

```java
/** Samples for SaasSubscriptionLevel List. */
public final class SaasSubscriptionLevelListSamples {
    /*
     * x-ms-original-file: specification/saas/resource-manager/Microsoft.SaaS/preview/2018-03-01-beta/examples/saasSubscriptionLevel/SaasGetAllInAzureSubscription.json
     */
    /**
     * Sample code: Get subscription level saas resources in Azure subscription.
     *
     * @param manager Entry point to SaaSManager.
     */
    public static void getSubscriptionLevelSaasResourcesInAzureSubscription(
        com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.saasSubscriptionLevels().list(com.azure.core.util.Context.NONE);
    }
}
```

### SaasSubscriptionLevel_ListAccessToken

```java
/** Samples for SaasSubscriptionLevel ListAccessToken. */
public final class SaasSubscriptionLevelListAccessTokenSamples {
    /*
     * x-ms-original-file: specification/saas/resource-manager/Microsoft.SaaS/preview/2018-03-01-beta/examples/saasSubscriptionLevel/ListAccessTokenPost.json
     */
    /**
     * Sample code: List subscription level SaaS resource access token.
     *
     * @param manager Entry point to SaaSManager.
     */
    public static void listSubscriptionLevelSaaSResourceAccessToken(
        com.azure.resourcemanager.saas.SaaSManager manager) {
        manager
            .saasSubscriptionLevels()
            .listAccessTokenWithResponse("my-saas-rg", "MyContosoSubscription", com.azure.core.util.Context.NONE);
    }
}
```

### SaasSubscriptionLevel_ListByResourceGroup

```java
/** Samples for SaasSubscriptionLevel ListByResourceGroup. */
public final class SaasSubscriptionLevelListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/saas/resource-manager/Microsoft.SaaS/preview/2018-03-01-beta/examples/saasSubscriptionLevel/SaasGetAllInResourceGroup.json
     */
    /**
     * Sample code: Get subscription level saas resources in resource group.
     *
     * @param manager Entry point to SaaSManager.
     */
    public static void getSubscriptionLevelSaasResourcesInResourceGroup(
        com.azure.resourcemanager.saas.SaaSManager manager) {
        manager.saasSubscriptionLevels().listByResourceGroup("my-saas-rg", com.azure.core.util.Context.NONE);
    }
}
```

### SaasSubscriptionLevel_MoveResources

```java
import com.azure.resourcemanager.saas.models.MoveResource;
import java.util.Arrays;

/** Samples for SaasSubscriptionLevel MoveResources. */
public final class SaasSubscriptionLevelMoveResourcesSamples {
    /*
     * x-ms-original-file: specification/saas/resource-manager/Microsoft.SaaS/preview/2018-03-01-beta/examples/saasSubscriptionLevel/ResourceMove.json
     */
    /**
     * Sample code: Move of a subscription Level SaaS resource.
     *
     * @param manager Entry point to SaaSManager.
     */
    public static void moveOfASubscriptionLevelSaaSResource(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager
            .saasSubscriptionLevels()
            .moveResources(
                "my-saas-rg",
                new MoveResource()
                    .withTargetResourceGroup(
                        "/subscriptions/5122d0a3-1e10-4baf-bdc5-c2a452489525/resourceGroups/new-saas-rg")
                    .withResources(
                        Arrays
                            .asList(
                                "/subscriptions/c825645b-e31b-9cf4-1cee-2aba9e58bc7c/resourceGroups/my-saas-rg/providers/Microsoft.SaaS/resources/saas1",
                                "/subscriptions/c825645b-e31b-9cf4-1cee-2aba9e58bc7c/resourceGroups/my-saas-rg/providers/Microsoft.SaaS/resources/saas2",
                                "/subscriptions/c825645b-e31b-9cf4-1cee-2aba9e58bc7c/resourceGroups/my-saas-rg/providers/Microsoft.SaaS/resources/saas3")),
                com.azure.core.util.Context.NONE);
    }
}
```

### SaasSubscriptionLevel_Update

```java
import com.azure.resourcemanager.saas.models.SaasCreationProperties;
import com.azure.resourcemanager.saas.models.SaasResource;
import java.util.HashMap;
import java.util.Map;

/** Samples for SaasSubscriptionLevel Update. */
public final class SaasSubscriptionLevelUpdateSamples {
    /*
     * x-ms-original-file: specification/saas/resource-manager/Microsoft.SaaS/preview/2018-03-01-beta/examples/saasSubscriptionLevel/SaasPatch.json
     */
    /**
     * Sample code: Update subscription level SaaS resource.
     *
     * @param manager Entry point to SaaSManager.
     */
    public static void updateSubscriptionLevelSaaSResource(com.azure.resourcemanager.saas.SaaSManager manager) {
        SaasResource resource =
            manager
                .saasSubscriptionLevels()
                .getByResourceGroupWithResponse("my-saas-rg", "MyContosoSubscription", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf()).withProperties(new SaasCreationProperties().withSkuId("premium")).apply();
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

### SaasSubscriptionLevel_UpdateToUnsubscribed

```java
import com.azure.resourcemanager.saas.models.DeleteOptions;

/** Samples for SaasSubscriptionLevel UpdateToUnsubscribed. */
public final class SaasSubscriptionLevelUpdateToUnsubscribedSamples {
    /*
     * x-ms-original-file: specification/saas/resource-manager/Microsoft.SaaS/preview/2018-03-01-beta/examples/saasSubscriptionLevel/SaasUnsubscribe.json
     */
    /**
     * Sample code: Unsubscribe Subscription Level SaaS resource.
     *
     * @param manager Entry point to SaaSManager.
     */
    public static void unsubscribeSubscriptionLevelSaaSResource(com.azure.resourcemanager.saas.SaaSManager manager) {
        manager
            .saasSubscriptionLevels()
            .updateToUnsubscribed(
                "my-saas-rg",
                "MyContosoSubscription",
                new DeleteOptions()
                    .withUnsubscribeOnly(true)
                    .withReasonCode(0.0F)
                    .withFeedback("No longer need this SaaS"),
                com.azure.core.util.Context.NONE);
    }
}
```

### SaasSubscriptionLevel_ValidateMoveResources

```java
import com.azure.resourcemanager.saas.models.MoveResource;
import java.util.Arrays;

/** Samples for SaasSubscriptionLevel ValidateMoveResources. */
public final class SaasSubscriptionLevelValidateMoveResourcesSamples {
    /*
     * x-ms-original-file: specification/saas/resource-manager/Microsoft.SaaS/preview/2018-03-01-beta/examples/saasSubscriptionLevel/ValidateResourceMove.json
     */
    /**
     * Sample code: Validate move of a subscription Level SaaS resource.
     *
     * @param manager Entry point to SaaSManager.
     */
    public static void validateMoveOfASubscriptionLevelSaaSResource(
        com.azure.resourcemanager.saas.SaaSManager manager) {
        manager
            .saasSubscriptionLevels()
            .validateMoveResourcesWithResponse(
                "my-saas-rg",
                new MoveResource()
                    .withTargetResourceGroup(
                        "/subscriptions/5122d0a3-1e10-4baf-bdc5-c2a452489525/resourceGroups/new-saas-rg")
                    .withResources(
                        Arrays
                            .asList(
                                "/subscriptions/c825645b-e31b-9cf4-1cee-2aba9e58bc7c/resourceGroups/my-saas-rg/providers/Microsoft.SaaS/resources/saas1",
                                "/subscriptions/c825645b-e31b-9cf4-1cee-2aba9e58bc7c/resourceGroups/my-saas-rg/providers/Microsoft.SaaS/resources/saas2",
                                "/subscriptions/c825645b-e31b-9cf4-1cee-2aba9e58bc7c/resourceGroups/my-saas-rg/providers/Microsoft.SaaS/resources/saas3")),
                com.azure.core.util.Context.NONE);
    }
}
```

