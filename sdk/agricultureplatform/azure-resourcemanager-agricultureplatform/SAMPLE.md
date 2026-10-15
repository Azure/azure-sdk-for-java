# Code snippets and samples


## AgriService

- [CreateOrUpdate](#agriservice_createorupdate)
- [Delete](#agriservice_delete)
- [GetByResourceGroup](#agriservice_getbyresourcegroup)
- [List](#agriservice_list)
- [ListAvailableSolutions](#agriservice_listavailablesolutions)
- [ListByResourceGroup](#agriservice_listbyresourcegroup)
- [Update](#agriservice_update)

## Operations

- [List](#operations_list)
### AgriService_CreateOrUpdate

```java
import com.azure.resourcemanager.agricultureplatform.models.AgriServiceConfig;
import com.azure.resourcemanager.agricultureplatform.models.AgriServiceResourceProperties;
import com.azure.resourcemanager.agricultureplatform.models.DataConnectorCredentialMap;
import com.azure.resourcemanager.agricultureplatform.models.DataConnectorCredentials;
import com.azure.resourcemanager.agricultureplatform.models.InstalledSolutionMap;
import com.azure.resourcemanager.agricultureplatform.models.ManagedServiceIdentity;
import com.azure.resourcemanager.agricultureplatform.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.agricultureplatform.models.Sku;
import com.azure.resourcemanager.agricultureplatform.models.SkuTier;
import com.azure.resourcemanager.agricultureplatform.models.Solution;
import com.azure.resourcemanager.agricultureplatform.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AgriService CreateOrUpdate.
 */
public final class AgriServiceCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/AgriService_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: AgriService_CreateOrUpdate.
     * 
     * @param manager Entry point to AgriculturePlatformManager.
     */
    public static void
        agriServiceCreateOrUpdate(com.azure.resourcemanager.agricultureplatform.AgriculturePlatformManager manager) {
        manager.agriServices()
            .define("abc123")
            .withRegion("pkneuknooprpqirnugzwbkiie")
            .withExistingResourceGroup("rgopenapi")
            .withTags(mapOf("key137", "fakeTokenPlaceholder"))
            .withProperties(new AgriServiceResourceProperties().withConfig(new AgriServiceConfig())
                .withDataConnectorCredentials(Arrays.asList(new DataConnectorCredentialMap()
                    .withKey("fakeTokenPlaceholder")
                    .withValue(new DataConnectorCredentials().withClientId("dce298a8-1eec-481a-a8f9-a3cd5a8257b2"))))
                .withInstalledSolutions(Arrays.asList(new InstalledSolutionMap().withKey("fakeTokenPlaceholder")
                    .withValue(new Solution().withApplicationName("bayerAgPowered.cwum")))))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE)
                .withUserAssignedIdentities(mapOf("key4955", new UserAssignedIdentity())))
            .withSku(new Sku().withName("kfl")
                .withTier(SkuTier.FREE)
                .withSize("r")
                .withFamily("xerdhxyjwrypvxphavgrtjphtohf")
                .withCapacity(20))
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

### AgriService_Delete

```java
/**
 * Samples for AgriService Delete.
 */
public final class AgriServiceDeleteSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/AgriService_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: AgriService_Delete.
     * 
     * @param manager Entry point to AgriculturePlatformManager.
     */
    public static void
        agriServiceDelete(com.azure.resourcemanager.agricultureplatform.AgriculturePlatformManager manager) {
        manager.agriServices().delete("rgopenapi", "abc123", com.azure.core.util.Context.NONE);
    }
}
```

### AgriService_GetByResourceGroup

```java
/**
 * Samples for AgriService GetByResourceGroup.
 */
public final class AgriServiceGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/AgriService_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AgriService_Get.
     * 
     * @param manager Entry point to AgriculturePlatformManager.
     */
    public static void
        agriServiceGet(com.azure.resourcemanager.agricultureplatform.AgriculturePlatformManager manager) {
        manager.agriServices().getByResourceGroupWithResponse("rgopenapi", "abc123", com.azure.core.util.Context.NONE);
    }
}
```

### AgriService_List

```java
/**
 * Samples for AgriService List.
 */
public final class AgriServiceListSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/AgriService_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: AgriService_ListBySubscription.
     * 
     * @param manager Entry point to AgriculturePlatformManager.
     */
    public static void agriServiceListBySubscription(
        com.azure.resourcemanager.agricultureplatform.AgriculturePlatformManager manager) {
        manager.agriServices().list(com.azure.core.util.Context.NONE);
    }
}
```

### AgriService_ListAvailableSolutions

```java
/**
 * Samples for AgriService ListAvailableSolutions.
 */
public final class AgriServiceListAvailableSolutionsSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/AgriService_ListAvailableSolutions_MaximumSet_Gen.json
     */
    /**
     * Sample code: AgriService_ListAvailableSolutions.
     * 
     * @param manager Entry point to AgriculturePlatformManager.
     */
    public static void agriServiceListAvailableSolutions(
        com.azure.resourcemanager.agricultureplatform.AgriculturePlatformManager manager) {
        manager.agriServices()
            .listAvailableSolutionsWithResponse("rgopenapi", "abc123", com.azure.core.util.Context.NONE);
    }
}
```

### AgriService_ListByResourceGroup

```java
/**
 * Samples for AgriService ListByResourceGroup.
 */
public final class AgriServiceListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/AgriService_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: AgriService_ListByResourceGroup.
     * 
     * @param manager Entry point to AgriculturePlatformManager.
     */
    public static void agriServiceListByResourceGroup(
        com.azure.resourcemanager.agricultureplatform.AgriculturePlatformManager manager) {
        manager.agriServices().listByResourceGroup("rgopenapi", com.azure.core.util.Context.NONE);
    }
}
```

### AgriService_Update

```java
import com.azure.resourcemanager.agricultureplatform.models.AgriServiceConfig;
import com.azure.resourcemanager.agricultureplatform.models.AgriServiceResource;
import com.azure.resourcemanager.agricultureplatform.models.AgriServiceResourceUpdateProperties;
import com.azure.resourcemanager.agricultureplatform.models.DataConnectorCredentialMap;
import com.azure.resourcemanager.agricultureplatform.models.DataConnectorCredentials;
import com.azure.resourcemanager.agricultureplatform.models.InstalledSolutionMap;
import com.azure.resourcemanager.agricultureplatform.models.ManagedServiceIdentity;
import com.azure.resourcemanager.agricultureplatform.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.agricultureplatform.models.Sku;
import com.azure.resourcemanager.agricultureplatform.models.SkuTier;
import com.azure.resourcemanager.agricultureplatform.models.Solution;
import com.azure.resourcemanager.agricultureplatform.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AgriService Update.
 */
public final class AgriServiceUpdateSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/AgriService_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: AgriService_Update.
     * 
     * @param manager Entry point to AgriculturePlatformManager.
     */
    public static void
        agriServiceUpdate(com.azure.resourcemanager.agricultureplatform.AgriculturePlatformManager manager) {
        AgriServiceResource resource = manager.agriServices()
            .getByResourceGroupWithResponse("rgopenapi", "abc123", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key9006", "fakeTokenPlaceholder"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE)
                .withUserAssignedIdentities(mapOf("key4771", new UserAssignedIdentity())))
            .withSku(new Sku().withName("tbdtdfffkar")
                .withTier(SkuTier.FREE)
                .withSize("iusaqqj")
                .withFamily("hxojswlgs")
                .withCapacity(22))
            .withProperties(new AgriServiceResourceUpdateProperties().withConfig(new AgriServiceConfig())
                .withDataConnectorCredentials(Arrays.asList(new DataConnectorCredentialMap()
                    .withKey("fakeTokenPlaceholder")
                    .withValue(new DataConnectorCredentials().withClientId("dce298a8-1eec-481a-a8f9-a3cd5a8257b2"))))
                .withInstalledSolutions(Arrays.asList(new InstalledSolutionMap().withKey("fakeTokenPlaceholder")
                    .withValue(new Solution().withApplicationName("bayerAgPowered.cwum")))))
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
     * x-ms-original-file: 2024-06-01-preview/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to AgriculturePlatformManager.
     */
    public static void
        operationsList(com.azure.resourcemanager.agricultureplatform.AgriculturePlatformManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

