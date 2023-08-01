# Code snippets and samples


## CustomLocations

- [CreateOrUpdate](#customlocations_createorupdate)
- [Delete](#customlocations_delete)
- [FindTargetResourceGroup](#customlocations_findtargetresourcegroup)
- [GetByResourceGroup](#customlocations_getbyresourcegroup)
- [List](#customlocations_list)
- [ListByResourceGroup](#customlocations_listbyresourcegroup)
- [ListEnabledResourceTypes](#customlocations_listenabledresourcetypes)
- [ListOperations](#customlocations_listoperations)
- [Update](#customlocations_update)

## ResourceSyncRules

- [CreateOrUpdate](#resourcesyncrules_createorupdate)
- [Delete](#resourcesyncrules_delete)
- [Get](#resourcesyncrules_get)
- [ListByCustomLocationId](#resourcesyncrules_listbycustomlocationid)
- [Update](#resourcesyncrules_update)
### CustomLocations_CreateOrUpdate

```java
import com.azure.resourcemanager.extendedlocation.models.CustomLocationPropertiesAuthentication;
import com.azure.resourcemanager.extendedlocation.models.Identity;
import com.azure.resourcemanager.extendedlocation.models.ResourceIdentityType;
import java.util.Arrays;

/** Samples for CustomLocations CreateOrUpdate. */
public final class CustomLocationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/extendedlocation/resource-manager/Microsoft.ExtendedLocation/preview/2021-08-31-preview/examples/CustomLocationsCreate_Update.json
     */
    /**
     * Sample code: Create/Update Custom Location.
     *
     * @param manager Entry point to CustomLocationsManager.
     */
    public static void createUpdateCustomLocation(
        com.azure.resourcemanager.extendedlocation.CustomLocationsManager manager) {
        manager
            .customLocations()
            .define("customLocation01")
            .withRegion("West US")
            .withExistingResourceGroup("testresourcegroup")
            .withIdentity(new Identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
            .withAuthentication(
                new CustomLocationPropertiesAuthentication().withType("KubeConfig").withValue("<base64 KubeConfig>"))
            .withClusterExtensionIds(
                Arrays
                    .asList(
                        "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Kubernetes/connectedCluster/someCluster/Microsoft.KubernetesConfiguration/clusterExtensions/fooExtension"))
            .withDisplayName("customLocationLocation01")
            .withHostResourceId(
                "/subscriptions/11111111-2222-3333-4444-555555555555/resourceGroups/testresourcegroup/providers/Microsoft.ContainerService/managedClusters/cluster01")
            .withNamespace("namespace01")
            .create();
    }
}
```

### CustomLocations_Delete

```java
import com.azure.core.util.Context;

/** Samples for CustomLocations Delete. */
public final class CustomLocationsDeleteSamples {
    /*
     * x-ms-original-file: specification/extendedlocation/resource-manager/Microsoft.ExtendedLocation/preview/2021-08-31-preview/examples/CustomLocationsDelete.json
     */
    /**
     * Sample code: Delete Custom Location.
     *
     * @param manager Entry point to CustomLocationsManager.
     */
    public static void deleteCustomLocation(com.azure.resourcemanager.extendedlocation.CustomLocationsManager manager) {
        manager.customLocations().delete("testresourcegroup", "customLocation01", Context.NONE);
    }
}
```

### CustomLocations_FindTargetResourceGroup

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.extendedlocation.models.CustomLocationFindTargetResourceGroupProperties;
import java.util.HashMap;
import java.util.Map;

/** Samples for CustomLocations FindTargetResourceGroup. */
public final class CustomLocationsFindTargetResourceGroupSamples {
    /*
     * x-ms-original-file: specification/extendedlocation/resource-manager/Microsoft.ExtendedLocation/preview/2021-08-31-preview/examples/CustomLocationsFindTargetResourceGroup.json
     */
    /**
     * Sample code: Post Custom Location Find Target Resource Group.
     *
     * @param manager Entry point to CustomLocationsManager.
     */
    public static void postCustomLocationFindTargetResourceGroup(
        com.azure.resourcemanager.extendedlocation.CustomLocationsManager manager) {
        manager
            .customLocations()
            .findTargetResourceGroupWithResponse(
                "testresourcegroup",
                "customLocation01",
                new CustomLocationFindTargetResourceGroupProperties()
                    .withLabels(mapOf("key1", "value1", "key2", "value2")),
                Context.NONE);
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

### CustomLocations_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for CustomLocations GetByResourceGroup. */
public final class CustomLocationsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/extendedlocation/resource-manager/Microsoft.ExtendedLocation/preview/2021-08-31-preview/examples/CustomLocationsGet.json
     */
    /**
     * Sample code: Get Custom Location.
     *
     * @param manager Entry point to CustomLocationsManager.
     */
    public static void getCustomLocation(com.azure.resourcemanager.extendedlocation.CustomLocationsManager manager) {
        manager.customLocations().getByResourceGroupWithResponse("testresourcegroup", "customLocation01", Context.NONE);
    }
}
```

### CustomLocations_List

```java
import com.azure.core.util.Context;

/** Samples for CustomLocations List. */
public final class CustomLocationsListSamples {
    /*
     * x-ms-original-file: specification/extendedlocation/resource-manager/Microsoft.ExtendedLocation/preview/2021-08-31-preview/examples/CustomLocationsListBySubscription.json
     */
    /**
     * Sample code: List Custom Locations by subscription.
     *
     * @param manager Entry point to CustomLocationsManager.
     */
    public static void listCustomLocationsBySubscription(
        com.azure.resourcemanager.extendedlocation.CustomLocationsManager manager) {
        manager.customLocations().list(Context.NONE);
    }
}
```

### CustomLocations_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for CustomLocations ListByResourceGroup. */
public final class CustomLocationsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/extendedlocation/resource-manager/Microsoft.ExtendedLocation/preview/2021-08-31-preview/examples/CustomLocationsListByResourceGroup.json
     */
    /**
     * Sample code: List Custom Locations by resource group.
     *
     * @param manager Entry point to CustomLocationsManager.
     */
    public static void listCustomLocationsByResourceGroup(
        com.azure.resourcemanager.extendedlocation.CustomLocationsManager manager) {
        manager.customLocations().listByResourceGroup("testresourcegroup", Context.NONE);
    }
}
```

### CustomLocations_ListEnabledResourceTypes

```java
import com.azure.core.util.Context;

/** Samples for CustomLocations ListEnabledResourceTypes. */
public final class CustomLocationsListEnabledResourceTypesSamples {
    /*
     * x-ms-original-file: specification/extendedlocation/resource-manager/Microsoft.ExtendedLocation/preview/2021-08-31-preview/examples/CustomLocationsListEnabledResourceTypes.json
     */
    /**
     * Sample code: Get Custom Location.
     *
     * @param manager Entry point to CustomLocationsManager.
     */
    public static void getCustomLocation(com.azure.resourcemanager.extendedlocation.CustomLocationsManager manager) {
        manager.customLocations().listEnabledResourceTypes("testresourcegroup", "customLocation01", Context.NONE);
    }
}
```

### CustomLocations_ListOperations

```java
import com.azure.core.util.Context;

/** Samples for CustomLocations ListOperations. */
public final class CustomLocationsListOperationsSamples {
    /*
     * x-ms-original-file: specification/extendedlocation/resource-manager/Microsoft.ExtendedLocation/preview/2021-08-31-preview/examples/CustomLocationsListOperations.json
     */
    /**
     * Sample code: List Custom Locations operations.
     *
     * @param manager Entry point to CustomLocationsManager.
     */
    public static void listCustomLocationsOperations(
        com.azure.resourcemanager.extendedlocation.CustomLocationsManager manager) {
        manager.customLocations().listOperations(Context.NONE);
    }
}
```

### CustomLocations_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.extendedlocation.models.CustomLocation;
import com.azure.resourcemanager.extendedlocation.models.Identity;
import com.azure.resourcemanager.extendedlocation.models.ResourceIdentityType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for CustomLocations Update. */
public final class CustomLocationsUpdateSamples {
    /*
     * x-ms-original-file: specification/extendedlocation/resource-manager/Microsoft.ExtendedLocation/preview/2021-08-31-preview/examples/CustomLocationsPatch.json
     */
    /**
     * Sample code: Update Custom Location.
     *
     * @param manager Entry point to CustomLocationsManager.
     */
    public static void updateCustomLocation(com.azure.resourcemanager.extendedlocation.CustomLocationsManager manager) {
        CustomLocation resource =
            manager
                .customLocations()
                .getByResourceGroupWithResponse("testresourcegroup", "customLocation01", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("archv3", "", "tier", "testing"))
            .withIdentity(new Identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
            .withClusterExtensionIds(
                Arrays
                    .asList(
                        "/subscriptions/11111111-2222-3333-4444-555555555555/resourceGroups/testresourcegroup/providers/Microsoft.ContainerService/managedClusters/cluster01/Microsoft.KubernetesConfiguration/clusterExtensions/fooExtension",
                        "/subscriptions/11111111-2222-3333-4444-555555555555/resourceGroups/testresourcegroup/providers/Microsoft.ContainerService/managedClusters/cluster01/Microsoft.KubernetesConfiguration/clusterExtensions/barExtension"))
            .apply();
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

### ResourceSyncRules_CreateOrUpdate

```java
import com.azure.resourcemanager.extendedlocation.models.MatchExpressionsProperties;
import com.azure.resourcemanager.extendedlocation.models.ResourceSyncRulePropertiesSelector;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for ResourceSyncRules CreateOrUpdate. */
public final class ResourceSyncRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/extendedlocation/resource-manager/Microsoft.ExtendedLocation/preview/2021-08-31-preview/examples/ResourceSyncRulesCreate_Update.json
     */
    /**
     * Sample code: Create/Update Resource Sync Rule.
     *
     * @param manager Entry point to CustomLocationsManager.
     */
    public static void createUpdateResourceSyncRule(
        com.azure.resourcemanager.extendedlocation.CustomLocationsManager manager) {
        manager
            .resourceSyncRules()
            .define("resourceSyncRule01")
            .withRegion("West US")
            .withExistingCustomLocation("testresourcegroup", "customLocation01")
            .withPriority(999)
            .withSelector(
                new ResourceSyncRulePropertiesSelector()
                    .withMatchExpressions(
                        Arrays
                            .asList(
                                new MatchExpressionsProperties()
                                    .withKey("key4")
                                    .withOperator("In")
                                    .withValues(Arrays.asList("value4"))))
                    .withMatchLabels(mapOf("key1", "value1")))
            .withTargetResourceGroup(
                "/subscriptions/11111111-2222-3333-4444-555555555555/resourceGroups/testresourcegroup")
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

### ResourceSyncRules_Delete

```java
import com.azure.core.util.Context;

/** Samples for ResourceSyncRules Delete. */
public final class ResourceSyncRulesDeleteSamples {
    /*
     * x-ms-original-file: specification/extendedlocation/resource-manager/Microsoft.ExtendedLocation/preview/2021-08-31-preview/examples/ResourceSyncRulesDelete.json
     */
    /**
     * Sample code: Delete Resource Sync Rule.
     *
     * @param manager Entry point to CustomLocationsManager.
     */
    public static void deleteResourceSyncRule(
        com.azure.resourcemanager.extendedlocation.CustomLocationsManager manager) {
        manager
            .resourceSyncRules()
            .deleteWithResponse("testresourcegroup", "customLocation01", "resourceSyncRule01", Context.NONE);
    }
}
```

### ResourceSyncRules_Get

```java
import com.azure.core.util.Context;

/** Samples for ResourceSyncRules Get. */
public final class ResourceSyncRulesGetSamples {
    /*
     * x-ms-original-file: specification/extendedlocation/resource-manager/Microsoft.ExtendedLocation/preview/2021-08-31-preview/examples/ResourceSyncRulesGet.json
     */
    /**
     * Sample code: Get Custom Location.
     *
     * @param manager Entry point to CustomLocationsManager.
     */
    public static void getCustomLocation(com.azure.resourcemanager.extendedlocation.CustomLocationsManager manager) {
        manager
            .resourceSyncRules()
            .getWithResponse("testresourcegroup", "customLocation01", "resourceSyncRule01", Context.NONE);
    }
}
```

### ResourceSyncRules_ListByCustomLocationId

```java
import com.azure.core.util.Context;

/** Samples for ResourceSyncRules ListByCustomLocationId. */
public final class ResourceSyncRulesListByCustomLocationIdSamples {
    /*
     * x-ms-original-file: specification/extendedlocation/resource-manager/Microsoft.ExtendedLocation/preview/2021-08-31-preview/examples/ResourceSyncRulesListByCustomLocationID.json
     */
    /**
     * Sample code: List Resource Sync Rules by subscription.
     *
     * @param manager Entry point to CustomLocationsManager.
     */
    public static void listResourceSyncRulesBySubscription(
        com.azure.resourcemanager.extendedlocation.CustomLocationsManager manager) {
        manager.resourceSyncRules().listByCustomLocationId("testresourcegroup", "customLocation01", Context.NONE);
    }
}
```

### ResourceSyncRules_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.extendedlocation.models.ResourceSyncRule;
import java.util.HashMap;
import java.util.Map;

/** Samples for ResourceSyncRules Update. */
public final class ResourceSyncRulesUpdateSamples {
    /*
     * x-ms-original-file: specification/extendedlocation/resource-manager/Microsoft.ExtendedLocation/preview/2021-08-31-preview/examples/ResourceSyncRulesPatch.json
     */
    /**
     * Sample code: Update Resource Sync Rule.
     *
     * @param manager Entry point to CustomLocationsManager.
     */
    public static void updateResourceSyncRule(
        com.azure.resourcemanager.extendedlocation.CustomLocationsManager manager) {
        ResourceSyncRule resource =
            manager
                .resourceSyncRules()
                .getWithResponse("testresourcegroup", "customLocation01", "resourceSyncRule01", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("tier", "testing"))
            .withTargetResourceGroup("/subscriptions/11111111-2222-3333-4444-555555555555/resourceGroups/testrg/")
            .apply();
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

