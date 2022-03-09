# Code snippets and samples


## CustomLocations

- [CreateOrUpdate](#customlocations_createorupdate)
- [Delete](#customlocations_delete)
- [GetByResourceGroup](#customlocations_getbyresourcegroup)
- [List](#customlocations_list)
- [ListByResourceGroup](#customlocations_listbyresourcegroup)
- [ListEnabledResourceTypes](#customlocations_listenabledresourcetypes)
- [ListOperations](#customlocations_listoperations)
- [Update](#customlocations_update)
### CustomLocations_CreateOrUpdate

```java
import com.azure.resourcemanager.extendedlocation.models.CustomLocationPropertiesAuthentication;
import com.azure.resourcemanager.extendedlocation.models.Identity;
import com.azure.resourcemanager.extendedlocation.models.ResourceIdentityType;
import java.util.Arrays;

/** Samples for CustomLocations CreateOrUpdate. */
public final class CustomLocationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/extendedlocation/resource-manager/Microsoft.ExtendedLocation/stable/2021-08-15/examples/CustomLocationsCreate_Update.json
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
     * x-ms-original-file: specification/extendedlocation/resource-manager/Microsoft.ExtendedLocation/stable/2021-08-15/examples/CustomLocationsDelete.json
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

### CustomLocations_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for CustomLocations GetByResourceGroup. */
public final class CustomLocationsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/extendedlocation/resource-manager/Microsoft.ExtendedLocation/stable/2021-08-15/examples/CustomLocationsGet.json
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
     * x-ms-original-file: specification/extendedlocation/resource-manager/Microsoft.ExtendedLocation/stable/2021-08-15/examples/CustomLocationsListBySubscription.json
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
     * x-ms-original-file: specification/extendedlocation/resource-manager/Microsoft.ExtendedLocation/stable/2021-08-15/examples/CustomLocationsListByResourceGroup.json
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
     * x-ms-original-file: specification/extendedlocation/resource-manager/Microsoft.ExtendedLocation/stable/2021-08-15/examples/CustomLocationsListEnabledResourceTypes.json
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
     * x-ms-original-file: specification/extendedlocation/resource-manager/Microsoft.ExtendedLocation/stable/2021-08-15/examples/CustomLocationsListOperations.json
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
     * x-ms-original-file: specification/extendedlocation/resource-manager/Microsoft.ExtendedLocation/stable/2021-08-15/examples/CustomLocationsPatch.json
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

