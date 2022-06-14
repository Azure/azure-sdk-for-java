# Code snippets and samples


## Operations

- [List](#operations_list)

## ProviderInstances

- [Create](#providerinstances_create)
- [Delete](#providerinstances_delete)
- [Get](#providerinstances_get)
- [List](#providerinstances_list)

## SapMonitors

- [Create](#sapmonitors_create)
- [Delete](#sapmonitors_delete)
- [GetByResourceGroup](#sapmonitors_getbyresourcegroup)
- [List](#sapmonitors_list)
- [Update](#sapmonitors_update)
### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/hanaonazure/resource-manager/Microsoft.HanaOnAzure/preview/2020-02-07-preview/examples/HanaOperations_List.json
     */
    /**
     * Sample code: List all HANA management operations supported by HANA RP.
     *
     * @param manager Entry point to HanaManager.
     */
    public static void listAllHANAManagementOperationsSupportedByHANARP(
        com.azure.resourcemanager.hanaonazure.HanaManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### ProviderInstances_Create

```java
/** Samples for ProviderInstances Create. */
public final class ProviderInstancesCreateSamples {
    /*
     * x-ms-original-file: specification/hanaonazure/resource-manager/Microsoft.HanaOnAzure/preview/2020-02-07-preview/examples/ProviderInstances_Create.json
     */
    /**
     * Sample code: Create a SAP Monitor.
     *
     * @param manager Entry point to HanaManager.
     */
    public static void createASAPMonitor(com.azure.resourcemanager.hanaonazure.HanaManager manager) {
        manager
            .providerInstances()
            .define("myProviderInstance")
            .withExistingSapMonitor("myResourceGroup", "mySapMonitor")
            .withTypePropertiesType("hana")
            .withProperties(
                "{\"hostname\":\"10.0.0.6\",\"dbName\":\"SYSTEMDB\",\"sqlPort\":30013,\"dbUsername\":\"SYSTEM\",\"dbPassword\":\"PASSWORD\"}")
            .withMetadata("{\"key\":\"value\"}")
            .create();
    }
}
```

### ProviderInstances_Delete

```java
import com.azure.core.util.Context;

/** Samples for ProviderInstances Delete. */
public final class ProviderInstancesDeleteSamples {
    /*
     * x-ms-original-file: specification/hanaonazure/resource-manager/Microsoft.HanaOnAzure/preview/2020-02-07-preview/examples/ProviderInstances_Delete.json
     */
    /**
     * Sample code: Deletes a SAP monitor.
     *
     * @param manager Entry point to HanaManager.
     */
    public static void deletesASAPMonitor(com.azure.resourcemanager.hanaonazure.HanaManager manager) {
        manager.providerInstances().delete("myResourceGroup", "mySapMonitor", "myProviderInstance", Context.NONE);
    }
}
```

### ProviderInstances_Get

```java
import com.azure.core.util.Context;

/** Samples for ProviderInstances Get. */
public final class ProviderInstancesGetSamples {
    /*
     * x-ms-original-file: specification/hanaonazure/resource-manager/Microsoft.HanaOnAzure/preview/2020-02-07-preview/examples/ProviderInstances_Get.json
     */
    /**
     * Sample code: Get properties of a SAP monitor.
     *
     * @param manager Entry point to HanaManager.
     */
    public static void getPropertiesOfASAPMonitor(com.azure.resourcemanager.hanaonazure.HanaManager manager) {
        manager
            .providerInstances()
            .getWithResponse("myResourceGroup", "mySapMonitor", "myProviderInstance", Context.NONE);
    }
}
```

### ProviderInstances_List

```java
import com.azure.core.util.Context;

/** Samples for ProviderInstances List. */
public final class ProviderInstancesListSamples {
    /*
     * x-ms-original-file: specification/hanaonazure/resource-manager/Microsoft.HanaOnAzure/preview/2020-02-07-preview/examples/ProviderInstances_List.json
     */
    /**
     * Sample code: List all SAP Monitors in a subscription.
     *
     * @param manager Entry point to HanaManager.
     */
    public static void listAllSAPMonitorsInASubscription(com.azure.resourcemanager.hanaonazure.HanaManager manager) {
        manager.providerInstances().list("myResourceGroup", "mySapMonitor", Context.NONE);
    }
}
```

### SapMonitors_Create

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for SapMonitors Create. */
public final class SapMonitorsCreateSamples {
    /*
     * x-ms-original-file: specification/hanaonazure/resource-manager/Microsoft.HanaOnAzure/preview/2020-02-07-preview/examples/SapMonitors_Create.json
     */
    /**
     * Sample code: Create a SAP Monitor.
     *
     * @param manager Entry point to HanaManager.
     */
    public static void createASAPMonitor(com.azure.resourcemanager.hanaonazure.HanaManager manager) {
        manager
            .sapMonitors()
            .define("mySapMonitor")
            .withRegion("westus")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("key", "value"))
            .withLogAnalyticsWorkspaceArmId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/microsoft.operationalinsights/workspaces/myWorkspace")
            .withEnableCustomerAnalytics(true)
            .withLogAnalyticsWorkspaceId("00000000-0000-0000-0000-000000000000")
            .withLogAnalyticsWorkspaceSharedKey(
                "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000==")
            .withMonitorSubnet(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Network/virtualNetworks/myVnet/subnets/mySubnet")
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

### SapMonitors_Delete

```java
import com.azure.core.util.Context;

/** Samples for SapMonitors Delete. */
public final class SapMonitorsDeleteSamples {
    /*
     * x-ms-original-file: specification/hanaonazure/resource-manager/Microsoft.HanaOnAzure/preview/2020-02-07-preview/examples/SapMonitors_Delete.json
     */
    /**
     * Sample code: Deletes a SAP monitor.
     *
     * @param manager Entry point to HanaManager.
     */
    public static void deletesASAPMonitor(com.azure.resourcemanager.hanaonazure.HanaManager manager) {
        manager.sapMonitors().delete("myResourceGroup", "mySapMonitor", Context.NONE);
    }
}
```

### SapMonitors_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for SapMonitors GetByResourceGroup. */
public final class SapMonitorsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hanaonazure/resource-manager/Microsoft.HanaOnAzure/preview/2020-02-07-preview/examples/SapMonitors_Get.json
     */
    /**
     * Sample code: Get properties of a SAP monitor.
     *
     * @param manager Entry point to HanaManager.
     */
    public static void getPropertiesOfASAPMonitor(com.azure.resourcemanager.hanaonazure.HanaManager manager) {
        manager.sapMonitors().getByResourceGroupWithResponse("myResourceGroup", "mySapMonitor", Context.NONE);
    }
}
```

### SapMonitors_List

```java
import com.azure.core.util.Context;

/** Samples for SapMonitors List. */
public final class SapMonitorsListSamples {
    /*
     * x-ms-original-file: specification/hanaonazure/resource-manager/Microsoft.HanaOnAzure/preview/2020-02-07-preview/examples/SapMonitors_List.json
     */
    /**
     * Sample code: List all SAP Monitors in a subscription.
     *
     * @param manager Entry point to HanaManager.
     */
    public static void listAllSAPMonitorsInASubscription(com.azure.resourcemanager.hanaonazure.HanaManager manager) {
        manager.sapMonitors().list(Context.NONE);
    }
}
```

### SapMonitors_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.hanaonazure.models.SapMonitor;
import java.util.HashMap;
import java.util.Map;

/** Samples for SapMonitors Update. */
public final class SapMonitorsUpdateSamples {
    /*
     * x-ms-original-file: specification/hanaonazure/resource-manager/Microsoft.HanaOnAzure/preview/2020-02-07-preview/examples/SapMonitors_PatchTags_Delete.json
     */
    /**
     * Sample code: Delete Tags field of a SAP monitor.
     *
     * @param manager Entry point to HanaManager.
     */
    public static void deleteTagsFieldOfASAPMonitor(com.azure.resourcemanager.hanaonazure.HanaManager manager) {
        SapMonitor resource =
            manager
                .sapMonitors()
                .getByResourceGroupWithResponse("myResourceGroup", "mySapMonitor", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf()).apply();
    }

    /*
     * x-ms-original-file: specification/hanaonazure/resource-manager/Microsoft.HanaOnAzure/preview/2020-02-07-preview/examples/SapMonitors_PatchTags.json
     */
    /**
     * Sample code: Update Tags field of a SAP monitor.
     *
     * @param manager Entry point to HanaManager.
     */
    public static void updateTagsFieldOfASAPMonitor(com.azure.resourcemanager.hanaonazure.HanaManager manager) {
        SapMonitor resource =
            manager
                .sapMonitors()
                .getByResourceGroupWithResponse("myResourceGroup", "mySapMonitor", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("testkey", "testvalue")).apply();
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

