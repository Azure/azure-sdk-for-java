# Code snippets and samples


## Associations

- [CreateOrUpdate](#associations_createorupdate)
- [Delete](#associations_delete)
- [Get](#associations_get)
- [ListAll](#associations_listall)

## CustomResourceProvider

- [CreateOrUpdate](#customresourceprovider_createorupdate)
- [Delete](#customresourceprovider_delete)
- [GetByResourceGroup](#customresourceprovider_getbyresourcegroup)
- [List](#customresourceprovider_list)
- [ListByResourceGroup](#customresourceprovider_listbyresourcegroup)
- [Update](#customresourceprovider_update)

## Operations

- [List](#operations_list)
### Associations_CreateOrUpdate

```java
/** Samples for Associations CreateOrUpdate. */
public final class AssociationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/customproviders/resource-manager/Microsoft.CustomProviders/preview/2018-09-01-preview/examples/createOrUpdateAssociation.json
     */
    /**
     * Sample code: Create or update an association.
     *
     * @param manager Entry point to CustomprovidersManager.
     */
    public static void createOrUpdateAnAssociation(
        com.azure.resourcemanager.customproviders.CustomprovidersManager manager) {
        manager
            .associations()
            .define("associationName")
            .withExistingScope("scope")
            .withTargetResourceId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/appRG/providers/Microsoft.Solutions/applications/applicationName")
            .create();
    }
}
```

### Associations_Delete

```java
import com.azure.core.util.Context;

/** Samples for Associations Delete. */
public final class AssociationsDeleteSamples {
    /*
     * x-ms-original-file: specification/customproviders/resource-manager/Microsoft.CustomProviders/preview/2018-09-01-preview/examples/deleteAssociation.json
     */
    /**
     * Sample code: Delete an association.
     *
     * @param manager Entry point to CustomprovidersManager.
     */
    public static void deleteAnAssociation(com.azure.resourcemanager.customproviders.CustomprovidersManager manager) {
        manager.associations().delete("scope", "associationName", Context.NONE);
    }
}
```

### Associations_Get

```java
import com.azure.core.util.Context;

/** Samples for Associations Get. */
public final class AssociationsGetSamples {
    /*
     * x-ms-original-file: specification/customproviders/resource-manager/Microsoft.CustomProviders/preview/2018-09-01-preview/examples/getAssociation.json
     */
    /**
     * Sample code: Get an association.
     *
     * @param manager Entry point to CustomprovidersManager.
     */
    public static void getAnAssociation(com.azure.resourcemanager.customproviders.CustomprovidersManager manager) {
        manager.associations().getWithResponse("scope", "associationName", Context.NONE);
    }
}
```

### Associations_ListAll

```java
import com.azure.core.util.Context;

/** Samples for Associations ListAll. */
public final class AssociationsListAllSamples {
    /*
     * x-ms-original-file: specification/customproviders/resource-manager/Microsoft.CustomProviders/preview/2018-09-01-preview/examples/getAllAssociations.json
     */
    /**
     * Sample code: Get all associations.
     *
     * @param manager Entry point to CustomprovidersManager.
     */
    public static void getAllAssociations(com.azure.resourcemanager.customproviders.CustomprovidersManager manager) {
        manager.associations().listAll("scope", Context.NONE);
    }
}
```

### CustomResourceProvider_CreateOrUpdate

```java
import com.azure.resourcemanager.customproviders.models.ActionRouting;
import com.azure.resourcemanager.customproviders.models.CustomRPActionRouteDefinition;
import com.azure.resourcemanager.customproviders.models.CustomRPResourceTypeRouteDefinition;
import com.azure.resourcemanager.customproviders.models.ResourceTypeRouting;
import java.util.Arrays;

/** Samples for CustomResourceProvider CreateOrUpdate. */
public final class CustomResourceProviderCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/customproviders/resource-manager/Microsoft.CustomProviders/preview/2018-09-01-preview/examples/createOrUpdateCustomRP.json
     */
    /**
     * Sample code: Create or update the custom resource provider.
     *
     * @param manager Entry point to CustomprovidersManager.
     */
    public static void createOrUpdateTheCustomResourceProvider(
        com.azure.resourcemanager.customproviders.CustomprovidersManager manager) {
        manager
            .customResourceProviders()
            .define("newrp")
            .withRegion("eastus")
            .withExistingResourceGroup("testRG")
            .withActions(
                Arrays
                    .asList(
                        new CustomRPActionRouteDefinition()
                            .withName("TestAction")
                            .withEndpoint("https://mytestendpoint/")
                            .withRoutingType(ActionRouting.PROXY)))
            .withResourceTypes(
                Arrays
                    .asList(
                        new CustomRPResourceTypeRouteDefinition()
                            .withName("TestResource")
                            .withEndpoint("https://mytestendpoint2/")
                            .withRoutingType(ResourceTypeRouting.PROXY_CACHE)))
            .create();
    }
}
```

### CustomResourceProvider_Delete

```java
import com.azure.core.util.Context;

/** Samples for CustomResourceProvider Delete. */
public final class CustomResourceProviderDeleteSamples {
    /*
     * x-ms-original-file: specification/customproviders/resource-manager/Microsoft.CustomProviders/preview/2018-09-01-preview/examples/deleteCustomRP.json
     */
    /**
     * Sample code: Delete a custom resource provider.
     *
     * @param manager Entry point to CustomprovidersManager.
     */
    public static void deleteACustomResourceProvider(
        com.azure.resourcemanager.customproviders.CustomprovidersManager manager) {
        manager.customResourceProviders().delete("testRG", "newrp", Context.NONE);
    }
}
```

### CustomResourceProvider_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for CustomResourceProvider GetByResourceGroup. */
public final class CustomResourceProviderGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/customproviders/resource-manager/Microsoft.CustomProviders/preview/2018-09-01-preview/examples/getCustomRP.json
     */
    /**
     * Sample code: Get a custom resource provider.
     *
     * @param manager Entry point to CustomprovidersManager.
     */
    public static void getACustomResourceProvider(
        com.azure.resourcemanager.customproviders.CustomprovidersManager manager) {
        manager.customResourceProviders().getByResourceGroupWithResponse("testRG", "newrp", Context.NONE);
    }
}
```

### CustomResourceProvider_List

```java
import com.azure.core.util.Context;

/** Samples for CustomResourceProvider List. */
public final class CustomResourceProviderListSamples {
    /*
     * x-ms-original-file: specification/customproviders/resource-manager/Microsoft.CustomProviders/preview/2018-09-01-preview/examples/listCustomRPsBySubscription.json
     */
    /**
     * Sample code: List all custom resource providers on the subscription.
     *
     * @param manager Entry point to CustomprovidersManager.
     */
    public static void listAllCustomResourceProvidersOnTheSubscription(
        com.azure.resourcemanager.customproviders.CustomprovidersManager manager) {
        manager.customResourceProviders().list(Context.NONE);
    }
}
```

### CustomResourceProvider_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for CustomResourceProvider ListByResourceGroup. */
public final class CustomResourceProviderListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/customproviders/resource-manager/Microsoft.CustomProviders/preview/2018-09-01-preview/examples/listCustomRPsByResourceGroup.json
     */
    /**
     * Sample code: List all custom resource providers on the resourceGroup.
     *
     * @param manager Entry point to CustomprovidersManager.
     */
    public static void listAllCustomResourceProvidersOnTheResourceGroup(
        com.azure.resourcemanager.customproviders.CustomprovidersManager manager) {
        manager.customResourceProviders().listByResourceGroup("testRG", Context.NONE);
    }
}
```

### CustomResourceProvider_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.customproviders.models.CustomRPManifest;
import java.util.HashMap;
import java.util.Map;

/** Samples for CustomResourceProvider Update. */
public final class CustomResourceProviderUpdateSamples {
    /*
     * x-ms-original-file: specification/customproviders/resource-manager/Microsoft.CustomProviders/preview/2018-09-01-preview/examples/updateCustomRP.json
     */
    /**
     * Sample code: Update a custom resource provider.
     *
     * @param manager Entry point to CustomprovidersManager.
     */
    public static void updateACustomResourceProvider(
        com.azure.resourcemanager.customproviders.CustomprovidersManager manager) {
        CustomRPManifest resource =
            manager
                .customResourceProviders()
                .getByResourceGroupWithResponse("testRG", "newrp", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf()).apply();
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
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/customproviders/resource-manager/Microsoft.CustomProviders/preview/2018-09-01-preview/examples/operationsList.json
     */
    /**
     * Sample code: List the custom providers operations.
     *
     * @param manager Entry point to CustomprovidersManager.
     */
    public static void listTheCustomProvidersOperations(
        com.azure.resourcemanager.customproviders.CustomprovidersManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

