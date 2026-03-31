# Code snippets and samples


## ResourceProvider

- [CreateOrUpdateServiceGroup](#resourceprovider_createorupdateservicegroup)
- [DeleteServiceGroup](#resourceprovider_deleteservicegroup)
- [UpdateServiceGroup](#resourceprovider_updateservicegroup)

## ServiceGroups

- [Get](#servicegroups_get)
- [ListAncestors](#servicegroups_listancestors)
### ResourceProvider_CreateOrUpdateServiceGroup

```java
import com.azure.resourcemanager.servicegroups.fluent.models.ServiceGroupInner;
import com.azure.resourcemanager.servicegroups.models.ParentServiceGroupProperties;
import com.azure.resourcemanager.servicegroups.models.ServiceGroupProperties;

/**
 * Samples for ResourceProvider CreateOrUpdateServiceGroup.
 */
public final class ResourceProviderCreateOrUpdateServiceGroupSamples {
    /*
     * x-ms-original-file: 2024-02-01-preview/ServiceGroup_Put.json
     */
    /**
     * Sample code: PutServiceGroup.
     * 
     * @param manager Entry point to ServiceGroupsManager.
     */
    public static void putServiceGroup(com.azure.resourcemanager.servicegroups.ServiceGroupsManager manager) {
        manager.resourceProviders()
            .createOrUpdateServiceGroup("ServiceGroup1",
                new ServiceGroupInner()
                    .withProperties(new ServiceGroupProperties().withDisplayName("ServiceGroup 1 Name")
                        .withParent(new ParentServiceGroupProperties()
                            .withResourceId("/providers/Microsoft.Management/serviceGroups/RootGroup"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_DeleteServiceGroup

```java
/**
 * Samples for ResourceProvider DeleteServiceGroup.
 */
public final class ResourceProviderDeleteServiceGroupSamples {
    /*
     * x-ms-original-file: 2024-02-01-preview/ServiceGroup_Delete.json
     */
    /**
     * Sample code: DeleteServiceGroup.
     * 
     * @param manager Entry point to ServiceGroupsManager.
     */
    public static void deleteServiceGroup(com.azure.resourcemanager.servicegroups.ServiceGroupsManager manager) {
        manager.resourceProviders()
            .deleteServiceGroup("20000000-0001-0000-0000-000000000000", com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_UpdateServiceGroup

```java
import com.azure.resourcemanager.servicegroups.fluent.models.ServiceGroupInner;
import com.azure.resourcemanager.servicegroups.models.ServiceGroupProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ResourceProvider UpdateServiceGroup.
 */
public final class ResourceProviderUpdateServiceGroupSamples {
    /*
     * x-ms-original-file: 2024-02-01-preview/ServiceGroup_Patch.json
     */
    /**
     * Sample code: PatchServiceGroup.
     * 
     * @param manager Entry point to ServiceGroupsManager.
     */
    public static void patchServiceGroup(com.azure.resourcemanager.servicegroups.ServiceGroupsManager manager) {
        manager.resourceProviders()
            .updateServiceGroup("ServiceGroup1",
                new ServiceGroupInner()
                    .withProperties(new ServiceGroupProperties().withDisplayName("ServiceGroup 1 Name"))
                    .withTags(mapOf("tag1", "value1", "tag2", "value2")),
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

### ServiceGroups_Get

```java
/**
 * Samples for ServiceGroups Get.
 */
public final class ServiceGroupsGetSamples {
    /*
     * x-ms-original-file: 2024-02-01-preview/ServiceGroup_Get.json
     */
    /**
     * Sample code: GetServiceGroup.
     * 
     * @param manager Entry point to ServiceGroupsManager.
     */
    public static void getServiceGroup(com.azure.resourcemanager.servicegroups.ServiceGroupsManager manager) {
        manager.serviceGroups()
            .getWithResponse("20000000-0001-0000-0000-000000000000", com.azure.core.util.Context.NONE);
    }
}
```

### ServiceGroups_ListAncestors

```java
/**
 * Samples for ServiceGroups ListAncestors.
 */
public final class ServiceGroupsListAncestorsSamples {
    /*
     * x-ms-original-file: 2024-02-01-preview/ServiceGroup_ListAncestors.json
     */
    /**
     * Sample code: ListServiceGroupAncestors.
     * 
     * @param manager Entry point to ServiceGroupsManager.
     */
    public static void listServiceGroupAncestors(com.azure.resourcemanager.servicegroups.ServiceGroupsManager manager) {
        manager.serviceGroups()
            .listAncestorsWithResponse("20000000-0001-0000-0000-000000000000", com.azure.core.util.Context.NONE);
    }
}
```

