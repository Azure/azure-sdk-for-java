# Code snippets and samples


## AiGatewayResources

- [CreateOrUpdate](#aigatewayresources_createorupdate)
- [Delete](#aigatewayresources_delete)
- [GetByResourceGroup](#aigatewayresources_getbyresourcegroup)
- [List](#aigatewayresources_list)
- [ListByResourceGroup](#aigatewayresources_listbyresourcegroup)
- [Update](#aigatewayresources_update)

## Operations

- [List](#operations_list)
### AiGatewayResources_CreateOrUpdate

```java
import com.azure.resourcemanager.aigateway.models.AiGatewayBackend;
import com.azure.resourcemanager.aigateway.models.AiGatewayProperties;
import com.azure.resourcemanager.aigateway.models.AiGatewaySubnet;
import com.azure.resourcemanager.aigateway.models.ManagedServiceIdentity;
import com.azure.resourcemanager.aigateway.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.aigateway.models.Sku;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AiGatewayResources CreateOrUpdate.
 */
public final class AiGatewayResourcesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/AiGatewayCreate.json
     */
    /**
     * Sample code: Create an AI Gateway.
     * 
     * @param manager Entry point to AIGatewayManager.
     */
    public static void createAnAIGateway(com.azure.resourcemanager.aigateway.AIGatewayManager manager) {
        manager.aiGatewayResources()
            .define("example-gateway")
            .withRegion("westus2")
            .withExistingResourceGroup("example-rg")
            .withProperties(
                new AiGatewayProperties().withBackend(new AiGatewayBackend().withSubnet(new AiGatewaySubnet().withId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/example-rg/providers/Microsoft.Network/virtualNetworks/example-vnet/subnets/example-subnet"))))
            .withTags(mapOf("environment", "development"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
            .withSku(new Sku().withName("AIGateway"))
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

### AiGatewayResources_Delete

```java
/**
 * Samples for AiGatewayResources Delete.
 */
public final class AiGatewayResourcesDeleteSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/AiGatewayDelete.json
     */
    /**
     * Sample code: Delete an AI Gateway.
     * 
     * @param manager Entry point to AIGatewayManager.
     */
    public static void deleteAnAIGateway(com.azure.resourcemanager.aigateway.AIGatewayManager manager) {
        manager.aiGatewayResources().delete("example-rg", "example-gateway", "*", com.azure.core.util.Context.NONE);
    }
}
```

### AiGatewayResources_GetByResourceGroup

```java
/**
 * Samples for AiGatewayResources GetByResourceGroup.
 */
public final class AiGatewayResourcesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/AiGatewayGet.json
     */
    /**
     * Sample code: Get an AI Gateway.
     * 
     * @param manager Entry point to AIGatewayManager.
     */
    public static void getAnAIGateway(com.azure.resourcemanager.aigateway.AIGatewayManager manager) {
        manager.aiGatewayResources()
            .getByResourceGroupWithResponse("example-rg", "example-gateway", com.azure.core.util.Context.NONE);
    }
}
```

### AiGatewayResources_List

```java
/**
 * Samples for AiGatewayResources List.
 */
public final class AiGatewayResourcesListSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/AiGatewayListBySubscription.json
     */
    /**
     * Sample code: List AI Gateways by subscription.
     * 
     * @param manager Entry point to AIGatewayManager.
     */
    public static void listAIGatewaysBySubscription(com.azure.resourcemanager.aigateway.AIGatewayManager manager) {
        manager.aiGatewayResources().list(com.azure.core.util.Context.NONE);
    }
}
```

### AiGatewayResources_ListByResourceGroup

```java
/**
 * Samples for AiGatewayResources ListByResourceGroup.
 */
public final class AiGatewayResourcesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/AiGatewayListByResourceGroup.json
     */
    /**
     * Sample code: List AI Gateways by resource group.
     * 
     * @param manager Entry point to AIGatewayManager.
     */
    public static void listAIGatewaysByResourceGroup(com.azure.resourcemanager.aigateway.AIGatewayManager manager) {
        manager.aiGatewayResources().listByResourceGroup("example-rg", com.azure.core.util.Context.NONE);
    }
}
```

### AiGatewayResources_Update

```java
import com.azure.resourcemanager.aigateway.models.AiGatewayResource;
import com.azure.resourcemanager.aigateway.models.AiGatewaySkuUpdate;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AiGatewayResources Update.
 */
public final class AiGatewayResourcesUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/AiGatewayUpdate.json
     */
    /**
     * Sample code: Update AI Gateway tags and SKU.
     * 
     * @param manager Entry point to AIGatewayManager.
     */
    public static void updateAIGatewayTagsAndSKU(com.azure.resourcemanager.aigateway.AIGatewayManager manager) {
        AiGatewayResource resource = manager.aiGatewayResources()
            .getByResourceGroupWithResponse("example-rg", "example-gateway", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("environment", "production"))
            .withSku(new AiGatewaySkuUpdate().withName("AIGateway"))
            .withIfMatch("*")
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
     * x-ms-original-file: 2026-09-01-preview/AIGatewayListOperations.json
     */
    /**
     * Sample code: List AI Gateway operations.
     * 
     * @param manager Entry point to AIGatewayManager.
     */
    public static void listAIGatewayOperations(com.azure.resourcemanager.aigateway.AIGatewayManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

