# Code snippets and samples


## Appliances

- [CreateOrUpdate](#appliances_createorupdate)
- [Delete](#appliances_delete)
- [GetByResourceGroup](#appliances_getbyresourcegroup)
- [List](#appliances_list)
- [ListByResourceGroup](#appliances_listbyresourcegroup)
- [ListClusterUserCredential](#appliances_listclusterusercredential)
- [ListOperations](#appliances_listoperations)
- [Update](#appliances_update)
### Appliances_CreateOrUpdate

```java
import com.azure.resourcemanager.resourceconnector.models.AppliancePropertiesInfrastructureConfig;
import com.azure.resourcemanager.resourceconnector.models.Distro;
import com.azure.resourcemanager.resourceconnector.models.Provider;

/** Samples for Appliances CreateOrUpdate. */
public final class AppliancesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/resourceconnector/resource-manager/Microsoft.ResourceConnector/preview/2021-10-31-preview/examples/AppliancesCreate_Update.json
     */
    /**
     * Sample code: Create/Update Appliance.
     *
     * @param manager Entry point to AppliancesManager.
     */
    public static void createUpdateAppliance(com.azure.resourcemanager.resourceconnector.AppliancesManager manager) {
        manager
            .appliances()
            .define("appliance01")
            .withRegion("West US")
            .withExistingResourceGroup("testresourcegroup")
            .withDistro(Distro.AKSEDGE)
            .withInfrastructureConfig(new AppliancePropertiesInfrastructureConfig().withProvider(Provider.VMWARE))
            .create();
    }
}
```

### Appliances_Delete

```java
import com.azure.core.util.Context;

/** Samples for Appliances Delete. */
public final class AppliancesDeleteSamples {
    /*
     * x-ms-original-file: specification/resourceconnector/resource-manager/Microsoft.ResourceConnector/preview/2021-10-31-preview/examples/AppliancesDelete.json
     */
    /**
     * Sample code: Delete Appliance.
     *
     * @param manager Entry point to AppliancesManager.
     */
    public static void deleteAppliance(com.azure.resourcemanager.resourceconnector.AppliancesManager manager) {
        manager.appliances().delete("testresourcegroup", "appliance01", Context.NONE);
    }
}
```

### Appliances_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Appliances GetByResourceGroup. */
public final class AppliancesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/resourceconnector/resource-manager/Microsoft.ResourceConnector/preview/2021-10-31-preview/examples/AppliancesGet.json
     */
    /**
     * Sample code: Get Appliance.
     *
     * @param manager Entry point to AppliancesManager.
     */
    public static void getAppliance(com.azure.resourcemanager.resourceconnector.AppliancesManager manager) {
        manager.appliances().getByResourceGroupWithResponse("testresourcegroup", "appliance01", Context.NONE);
    }
}
```

### Appliances_List

```java
import com.azure.core.util.Context;

/** Samples for Appliances List. */
public final class AppliancesListSamples {
    /*
     * x-ms-original-file: specification/resourceconnector/resource-manager/Microsoft.ResourceConnector/preview/2021-10-31-preview/examples/AppliancesListBySubscription.json
     */
    /**
     * Sample code: List Appliances by subscription.
     *
     * @param manager Entry point to AppliancesManager.
     */
    public static void listAppliancesBySubscription(
        com.azure.resourcemanager.resourceconnector.AppliancesManager manager) {
        manager.appliances().list(Context.NONE);
    }
}
```

### Appliances_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Appliances ListByResourceGroup. */
public final class AppliancesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/resourceconnector/resource-manager/Microsoft.ResourceConnector/preview/2021-10-31-preview/examples/AppliancesListByResourceGroup.json
     */
    /**
     * Sample code: List Appliances by resource group.
     *
     * @param manager Entry point to AppliancesManager.
     */
    public static void listAppliancesByResourceGroup(
        com.azure.resourcemanager.resourceconnector.AppliancesManager manager) {
        manager.appliances().listByResourceGroup("testresourcegroup", Context.NONE);
    }
}
```

### Appliances_ListClusterUserCredential

```java
import com.azure.core.util.Context;

/** Samples for Appliances ListClusterUserCredential. */
public final class AppliancesListClusterUserCredentialSamples {
    /*
     * x-ms-original-file: specification/resourceconnector/resource-manager/Microsoft.ResourceConnector/preview/2021-10-31-preview/examples/AppliancesListClusterUserCredential.json
     */
    /**
     * Sample code: ListClusterUserCredentialAppliance.
     *
     * @param manager Entry point to AppliancesManager.
     */
    public static void listClusterUserCredentialAppliance(
        com.azure.resourcemanager.resourceconnector.AppliancesManager manager) {
        manager
            .appliances()
            .listClusterUserCredentialWithResponse("testresourcegroup", "testresourcename", Context.NONE);
    }
}
```

### Appliances_ListOperations

```java
import com.azure.core.util.Context;

/** Samples for Appliances ListOperations. */
public final class AppliancesListOperationsSamples {
    /*
     * x-ms-original-file: specification/resourceconnector/resource-manager/Microsoft.ResourceConnector/preview/2021-10-31-preview/examples/AppliancesListOperations.json
     */
    /**
     * Sample code: List Appliances operations.
     *
     * @param manager Entry point to AppliancesManager.
     */
    public static void listAppliancesOperations(com.azure.resourcemanager.resourceconnector.AppliancesManager manager) {
        manager.appliances().listOperations(Context.NONE);
    }
}
```

### Appliances_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.resourceconnector.models.Appliance;
import java.util.HashMap;
import java.util.Map;

/** Samples for Appliances Update. */
public final class AppliancesUpdateSamples {
    /*
     * x-ms-original-file: specification/resourceconnector/resource-manager/Microsoft.ResourceConnector/preview/2021-10-31-preview/examples/AppliancesPatch.json
     */
    /**
     * Sample code: Update Appliance.
     *
     * @param manager Entry point to AppliancesManager.
     */
    public static void updateAppliance(com.azure.resourcemanager.resourceconnector.AppliancesManager manager) {
        Appliance resource =
            manager
                .appliances()
                .getByResourceGroupWithResponse("testresourcegroup", "appliance01", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("key", "value")).apply();
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

