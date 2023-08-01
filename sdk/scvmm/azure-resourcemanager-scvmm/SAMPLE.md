# Code snippets and samples


## AvailabilitySets

- [CreateOrUpdate](#availabilitysets_createorupdate)
- [Delete](#availabilitysets_delete)
- [GetByResourceGroup](#availabilitysets_getbyresourcegroup)
- [List](#availabilitysets_list)
- [ListByResourceGroup](#availabilitysets_listbyresourcegroup)
- [Update](#availabilitysets_update)

## Clouds

- [CreateOrUpdate](#clouds_createorupdate)
- [Delete](#clouds_delete)
- [GetByResourceGroup](#clouds_getbyresourcegroup)
- [List](#clouds_list)
- [ListByResourceGroup](#clouds_listbyresourcegroup)
- [Update](#clouds_update)

## InventoryItems

- [Create](#inventoryitems_create)
- [Delete](#inventoryitems_delete)
- [Get](#inventoryitems_get)
- [ListByVmmServer](#inventoryitems_listbyvmmserver)

## Operations

- [List](#operations_list)

## VirtualMachineTemplates

- [CreateOrUpdate](#virtualmachinetemplates_createorupdate)
- [Delete](#virtualmachinetemplates_delete)
- [GetByResourceGroup](#virtualmachinetemplates_getbyresourcegroup)
- [List](#virtualmachinetemplates_list)
- [ListByResourceGroup](#virtualmachinetemplates_listbyresourcegroup)
- [Update](#virtualmachinetemplates_update)

## VirtualMachines

- [CreateCheckpoint](#virtualmachines_createcheckpoint)
- [CreateOrUpdate](#virtualmachines_createorupdate)
- [Delete](#virtualmachines_delete)
- [DeleteCheckpoint](#virtualmachines_deletecheckpoint)
- [GetByResourceGroup](#virtualmachines_getbyresourcegroup)
- [List](#virtualmachines_list)
- [ListByResourceGroup](#virtualmachines_listbyresourcegroup)
- [Restart](#virtualmachines_restart)
- [RestoreCheckpoint](#virtualmachines_restorecheckpoint)
- [Start](#virtualmachines_start)
- [Stop](#virtualmachines_stop)
- [Update](#virtualmachines_update)

## VirtualNetworks

- [CreateOrUpdate](#virtualnetworks_createorupdate)
- [Delete](#virtualnetworks_delete)
- [GetByResourceGroup](#virtualnetworks_getbyresourcegroup)
- [List](#virtualnetworks_list)
- [ListByResourceGroup](#virtualnetworks_listbyresourcegroup)
- [Update](#virtualnetworks_update)

## VmmServers

- [CreateOrUpdate](#vmmservers_createorupdate)
- [Delete](#vmmservers_delete)
- [GetByResourceGroup](#vmmservers_getbyresourcegroup)
- [List](#vmmservers_list)
- [ListByResourceGroup](#vmmservers_listbyresourcegroup)
- [Update](#vmmservers_update)
### AvailabilitySets_CreateOrUpdate

```java
import com.azure.resourcemanager.scvmm.models.ExtendedLocation;

/** Samples for AvailabilitySets CreateOrUpdate. */
public final class AvailabilitySetsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/CreateAvailabilitySet.json
     */
    /**
     * Sample code: CreateAvailabilitySet.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void createAvailabilitySet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .availabilitySets()
            .define("HRAvailabilitySet")
            .withRegion("East US")
            .withExistingResourceGroup("testrg")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withType("customLocation")
                    .withName(
                        "/subscriptions/a5015e1c-867f-4533-8541-85cd470d0cfb/resourceGroups/demoRG/providers/Microsoft.Arc/customLocations/contoso"))
            .withAvailabilitySetName("hr-avset")
            .withVmmServerId(
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.ScVmm/VMMServers/ContosoVMMServer")
            .create();
    }
}
```

### AvailabilitySets_Delete

```java
import com.azure.core.util.Context;

/** Samples for AvailabilitySets Delete. */
public final class AvailabilitySetsDeleteSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/DeleteAvailabilitySet.json
     */
    /**
     * Sample code: DeleteAvailabilitySet.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void deleteAvailabilitySet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.availabilitySets().delete("testrg", "HRAvailabilitySet", null, Context.NONE);
    }
}
```

### AvailabilitySets_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for AvailabilitySets GetByResourceGroup. */
public final class AvailabilitySetsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/GetAvailabilitySet.json
     */
    /**
     * Sample code: GetAvailabilitySet.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void getAvailabilitySet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.availabilitySets().getByResourceGroupWithResponse("testrg", "HRAvailabilitySet", Context.NONE);
    }
}
```

### AvailabilitySets_List

```java
import com.azure.core.util.Context;

/** Samples for AvailabilitySets List. */
public final class AvailabilitySetsListSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/ListAvailabilitySetsBySubscription.json
     */
    /**
     * Sample code: ListAvailabilitySetsBySubscription.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listAvailabilitySetsBySubscription(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.availabilitySets().list(Context.NONE);
    }
}
```

### AvailabilitySets_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for AvailabilitySets ListByResourceGroup. */
public final class AvailabilitySetsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/ListAvailabilitySetsByResourceGroup.json
     */
    /**
     * Sample code: ListAvailabilitySetsByResourceGroup.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listAvailabilitySetsByResourceGroup(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.availabilitySets().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### AvailabilitySets_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.scvmm.models.AvailabilitySet;
import java.util.HashMap;
import java.util.Map;

/** Samples for AvailabilitySets Update. */
public final class AvailabilitySetsUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/UpdateAvailabilitySet.json
     */
    /**
     * Sample code: UpdateAvailabilitySet.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void updateAvailabilitySet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        AvailabilitySet resource =
            manager
                .availabilitySets()
                .getByResourceGroupWithResponse("testrg", "HRAvailabilitySet", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### Clouds_CreateOrUpdate

```java
import com.azure.resourcemanager.scvmm.models.ExtendedLocation;

/** Samples for Clouds CreateOrUpdate. */
public final class CloudsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/CreateCloud.json
     */
    /**
     * Sample code: CreateCloud.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void createCloud(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .clouds()
            .define("HRCloud")
            .withRegion("East US")
            .withExistingResourceGroup("testrg")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withType("customLocation")
                    .withName(
                        "/subscriptions/a5015e1c-867f-4533-8541-85cd470d0cfb/resourceGroups/demoRG/providers/Microsoft.Arc/customLocations/contoso"))
            .withUuid("aaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
            .withVmmServerId(
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.SCVMM/VMMServers/ContosoVMMServer")
            .create();
    }
}
```

### Clouds_Delete

```java
import com.azure.core.util.Context;

/** Samples for Clouds Delete. */
public final class CloudsDeleteSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/DeleteCloud.json
     */
    /**
     * Sample code: DeleteCloud.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void deleteCloud(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.clouds().delete("testrg", "HRCloud", null, Context.NONE);
    }
}
```

### Clouds_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Clouds GetByResourceGroup. */
public final class CloudsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/GetCloud.json
     */
    /**
     * Sample code: GetCloud.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void getCloud(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.clouds().getByResourceGroupWithResponse("testrg", "HRCloud", Context.NONE);
    }
}
```

### Clouds_List

```java
import com.azure.core.util.Context;

/** Samples for Clouds List. */
public final class CloudsListSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/ListCloudsBySubscription.json
     */
    /**
     * Sample code: ListCloudsBySubscription.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listCloudsBySubscription(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.clouds().list(Context.NONE);
    }
}
```

### Clouds_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Clouds ListByResourceGroup. */
public final class CloudsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/ListCloudsByResourceGroup.json
     */
    /**
     * Sample code: ListCloudsByResourceGroup.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listCloudsByResourceGroup(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.clouds().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### Clouds_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.scvmm.models.Cloud;
import java.util.HashMap;
import java.util.Map;

/** Samples for Clouds Update. */
public final class CloudsUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/UpdateCloud.json
     */
    /**
     * Sample code: UpdateCloud.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void updateCloud(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        Cloud resource = manager.clouds().getByResourceGroupWithResponse("testrg", "HRCloud", Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### InventoryItems_Create

```java
/** Samples for InventoryItems Create. */
public final class InventoryItemsCreateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/CreateInventoryItem.json
     */
    /**
     * Sample code: CreateInventoryItem.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void createInventoryItem(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .inventoryItems()
            .define("12345678-1234-1234-1234-123456789abc")
            .withExistingVmmServer("testrg", "ContosoVMMServer")
            .create();
    }
}
```

### InventoryItems_Delete

```java
import com.azure.core.util.Context;

/** Samples for InventoryItems Delete. */
public final class InventoryItemsDeleteSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/DeleteInventoryItem.json
     */
    /**
     * Sample code: DeleteInventoryItem.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void deleteInventoryItem(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .inventoryItems()
            .deleteWithResponse("testrg", "ContosoVMMServer", "12345678-1234-1234-1234-123456789abc", Context.NONE);
    }
}
```

### InventoryItems_Get

```java
import com.azure.core.util.Context;

/** Samples for InventoryItems Get. */
public final class InventoryItemsGetSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/GetInventoryItem.json
     */
    /**
     * Sample code: GetInventoryItem.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void getInventoryItem(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .inventoryItems()
            .getWithResponse("testrg", "ContosoVMMServer", "12345678-1234-1234-1234-123456789abc", Context.NONE);
    }
}
```

### InventoryItems_ListByVmmServer

```java
import com.azure.core.util.Context;

/** Samples for InventoryItems ListByVmmServer. */
public final class InventoryItemsListByVmmServerSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/ListInventoryItemsByVMMServer.json
     */
    /**
     * Sample code: InventoryItemsListByVMMServer.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void inventoryItemsListByVMMServer(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.inventoryItems().listByVmmServer("testrg", "ContosoVMMServer", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/ListOperations.json
     */
    /**
     * Sample code: ListOperations.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listOperations(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### VirtualMachineTemplates_CreateOrUpdate

```java
import com.azure.resourcemanager.scvmm.models.ExtendedLocation;

/** Samples for VirtualMachineTemplates CreateOrUpdate. */
public final class VirtualMachineTemplatesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/CreateVirtualMachineTemplate.json
     */
    /**
     * Sample code: CreateVirtualMachineTemplate.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void createVirtualMachineTemplate(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .virtualMachineTemplates()
            .define("HRVirtualMachineTemplate")
            .withRegion("East US")
            .withExistingResourceGroup("testrg")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withType("customLocation")
                    .withName(
                        "/subscriptions/a5015e1c-867f-4533-8541-85cd470d0cfb/resourceGroups/demoRG/providers/Microsoft.Arc/customLocations/contoso"))
            .withUuid("aaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
            .withVmmServerId(
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.SCVMM/VMMServers/ContosoVMMServer")
            .create();
    }
}
```

### VirtualMachineTemplates_Delete

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachineTemplates Delete. */
public final class VirtualMachineTemplatesDeleteSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/DeleteVirtualMachineTemplate.json
     */
    /**
     * Sample code: DeleteVirtualMachineTemplate.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void deleteVirtualMachineTemplate(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineTemplates().delete("testrg", "HRVirtualMachineTemplate", null, Context.NONE);
    }
}
```

### VirtualMachineTemplates_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachineTemplates GetByResourceGroup. */
public final class VirtualMachineTemplatesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/GetVirtualMachineTemplate.json
     */
    /**
     * Sample code: GetVirtualMachineTemplate.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void getVirtualMachineTemplate(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .virtualMachineTemplates()
            .getByResourceGroupWithResponse("testrg", "HRVirtualMachineTemplate", Context.NONE);
    }
}
```

### VirtualMachineTemplates_List

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachineTemplates List. */
public final class VirtualMachineTemplatesListSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/ListVirtualMachineTemplatesBySubscription.json
     */
    /**
     * Sample code: ListVirtualMachineTemplatesBySubscription.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listVirtualMachineTemplatesBySubscription(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineTemplates().list(Context.NONE);
    }
}
```

### VirtualMachineTemplates_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachineTemplates ListByResourceGroup. */
public final class VirtualMachineTemplatesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/ListVirtualMachineTemplatesByResourceGroup.json
     */
    /**
     * Sample code: ListVirtualMachineTemplatesByResourceGroup.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listVirtualMachineTemplatesByResourceGroup(
        com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineTemplates().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### VirtualMachineTemplates_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.scvmm.models.VirtualMachineTemplate;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualMachineTemplates Update. */
public final class VirtualMachineTemplatesUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/UpdateVirtualMachineTemplate.json
     */
    /**
     * Sample code: UpdateVirtualMachineTemplate.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void updateVirtualMachineTemplate(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        VirtualMachineTemplate resource =
            manager
                .virtualMachineTemplates()
                .getByResourceGroupWithResponse("testrg", "HRVirtualMachineTemplate", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### VirtualMachines_CreateCheckpoint

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.scvmm.models.VirtualMachineCreateCheckpoint;

/** Samples for VirtualMachines CreateCheckpoint. */
public final class VirtualMachinesCreateCheckpointSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/CreateCheckpointVirtualMachine.json
     */
    /**
     * Sample code: CreateCheckpointVirtualMachine.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void createCheckpointVirtualMachine(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .virtualMachines()
            .createCheckpoint(
                "testrg",
                "DemoVM",
                new VirtualMachineCreateCheckpoint()
                    .withName("Demo Checkpoint name")
                    .withDescription("Demo Checkpoint description"),
                Context.NONE);
    }
}
```

### VirtualMachines_CreateOrUpdate

```java
import com.azure.resourcemanager.scvmm.models.ExtendedLocation;
import com.azure.resourcemanager.scvmm.models.HardwareProfile;

/** Samples for VirtualMachines CreateOrUpdate. */
public final class VirtualMachinesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/CreateVirtualMachine.json
     */
    /**
     * Sample code: CreateVirtualMachine.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void createVirtualMachine(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .virtualMachines()
            .define("DemoVM")
            .withRegion("East US")
            .withExistingResourceGroup("testrg")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withType("customLocation")
                    .withName(
                        "/subscriptions/a5015e1c-867f-4533-8541-85cd470d0cfb/resourceGroups/demoRG/providers/Microsoft.Arc/customLocations/contoso"))
            .withVmmServerId(
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.SCVMM/VMMServers/ContosoVMMServer")
            .withCloudId(
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.SCVMM/Clouds/HRCloud")
            .withTemplateId(
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.SCVMM/VirtualMachineTemplates/HRVirtualMachineTemplate")
            .withHardwareProfile(new HardwareProfile().withMemoryMB(4096).withCpuCount(4))
            .create();
    }
}
```

### VirtualMachines_Delete

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines Delete. */
public final class VirtualMachinesDeleteSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/DeleteVirtualMachine.json
     */
    /**
     * Sample code: DeleteVirtualMachine.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void deleteVirtualMachine(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachines().delete("testrg", "DemoVM", null, null, Context.NONE);
    }
}
```

### VirtualMachines_DeleteCheckpoint

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.scvmm.models.VirtualMachineDeleteCheckpoint;

/** Samples for VirtualMachines DeleteCheckpoint. */
public final class VirtualMachinesDeleteCheckpointSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/DeleteCheckpointVirtualMachine.json
     */
    /**
     * Sample code: DeleteCheckpointVirtualMachine.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void deleteCheckpointVirtualMachine(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .virtualMachines()
            .deleteCheckpoint(
                "testrg", "DemoVM", new VirtualMachineDeleteCheckpoint().withId("Demo CheckpointID"), Context.NONE);
    }
}
```

### VirtualMachines_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines GetByResourceGroup. */
public final class VirtualMachinesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/GetVirtualMachine.json
     */
    /**
     * Sample code: GetVirtualMachine.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void getVirtualMachine(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachines().getByResourceGroupWithResponse("testrg", "DemoVM", Context.NONE);
    }
}
```

### VirtualMachines_List

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines List. */
public final class VirtualMachinesListSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/ListVirtualMachinesBySubscription.json
     */
    /**
     * Sample code: ListVirtualMachinesBySubscription.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listVirtualMachinesBySubscription(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachines().list(Context.NONE);
    }
}
```

### VirtualMachines_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines ListByResourceGroup. */
public final class VirtualMachinesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/ListVirtualMachinesByResourceGroup.json
     */
    /**
     * Sample code: ListVirtualMachinesByResourceGroup.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listVirtualMachinesByResourceGroup(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachines().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### VirtualMachines_Restart

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines Restart. */
public final class VirtualMachinesRestartSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/RestartVirtualMachine.json
     */
    /**
     * Sample code: RestartVirtualMachine.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void restartVirtualMachine(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachines().restart("testrg", "DemoVM", Context.NONE);
    }
}
```

### VirtualMachines_RestoreCheckpoint

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.scvmm.models.VirtualMachineRestoreCheckpoint;

/** Samples for VirtualMachines RestoreCheckpoint. */
public final class VirtualMachinesRestoreCheckpointSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/RestoreCheckpointVirtualMachine.json
     */
    /**
     * Sample code: RestoreCheckpointVirtualMachine.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void restoreCheckpointVirtualMachine(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .virtualMachines()
            .restoreCheckpoint(
                "testrg", "DemoVM", new VirtualMachineRestoreCheckpoint().withId("Demo CheckpointID"), Context.NONE);
    }
}
```

### VirtualMachines_Start

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines Start. */
public final class VirtualMachinesStartSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/StartVirtualMachine.json
     */
    /**
     * Sample code: StartVirtualMachine.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void startVirtualMachine(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachines().start("testrg", "DemoVM", Context.NONE);
    }
}
```

### VirtualMachines_Stop

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.scvmm.models.StopVirtualMachineOptions;

/** Samples for VirtualMachines Stop. */
public final class VirtualMachinesStopSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/StopVirtualMachine.json
     */
    /**
     * Sample code: StopVirtualMachine.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void stopVirtualMachine(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .virtualMachines()
            .stop("testrg", "DemoVM", new StopVirtualMachineOptions().withSkipShutdown(true), Context.NONE);
    }
}
```

### VirtualMachines_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.scvmm.models.AllocationMethod;
import com.azure.resourcemanager.scvmm.models.HardwareProfileUpdate;
import com.azure.resourcemanager.scvmm.models.NetworkInterfacesUpdate;
import com.azure.resourcemanager.scvmm.models.NetworkProfileUpdate;
import com.azure.resourcemanager.scvmm.models.StorageProfileUpdate;
import com.azure.resourcemanager.scvmm.models.VirtualDiskUpdate;
import com.azure.resourcemanager.scvmm.models.VirtualMachine;
import com.azure.resourcemanager.scvmm.models.VirtualMachineUpdateProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualMachines Update. */
public final class VirtualMachinesUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/UpdateVirtualMachine.json
     */
    /**
     * Sample code: UpdateVirtualMachine.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void updateVirtualMachine(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        VirtualMachine resource =
            manager.virtualMachines().getByResourceGroupWithResponse("testrg", "DemoVM", Context.NONE).getValue();
        resource
            .update()
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
            .withProperties(
                new VirtualMachineUpdateProperties()
                    .withHardwareProfile(new HardwareProfileUpdate().withMemoryMB(4096).withCpuCount(4))
                    .withStorageProfile(
                        new StorageProfileUpdate()
                            .withDisks(Arrays.asList(new VirtualDiskUpdate().withName("test").withDiskSizeGB(10))))
                    .withNetworkProfile(
                        new NetworkProfileUpdate()
                            .withNetworkInterfaces(
                                Arrays
                                    .asList(
                                        new NetworkInterfacesUpdate()
                                            .withName("test")
                                            .withIpv4AddressType(AllocationMethod.DYNAMIC)
                                            .withIpv6AddressType(AllocationMethod.DYNAMIC)
                                            .withMacAddressType(AllocationMethod.STATIC)))))
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

### VirtualNetworks_CreateOrUpdate

```java
import com.azure.resourcemanager.scvmm.models.ExtendedLocation;

/** Samples for VirtualNetworks CreateOrUpdate. */
public final class VirtualNetworksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/CreateVirtualNetwork.json
     */
    /**
     * Sample code: CreateVirtualNetwork.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void createVirtualNetwork(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .virtualNetworks()
            .define("HRVirtualNetwork")
            .withRegion("East US")
            .withExistingResourceGroup("testrg")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withType("customLocation")
                    .withName(
                        "/subscriptions/a5015e1c-867f-4533-8541-85cd470d0cfb/resourceGroups/demoRG/providers/Microsoft.Arc/customLocations/contoso"))
            .withUuid("aaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
            .withVmmServerId(
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.SCVMM/VMMServers/ContosoVMMServer")
            .create();
    }
}
```

### VirtualNetworks_Delete

```java
import com.azure.core.util.Context;

/** Samples for VirtualNetworks Delete. */
public final class VirtualNetworksDeleteSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/DeleteVirtualNetwork.json
     */
    /**
     * Sample code: DeleteVirtualNetwork.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void deleteVirtualNetwork(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualNetworks().delete("testrg", "HRVirtualNetwork", null, Context.NONE);
    }
}
```

### VirtualNetworks_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for VirtualNetworks GetByResourceGroup. */
public final class VirtualNetworksGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/GetVirtualNetwork.json
     */
    /**
     * Sample code: GetVirtualNetwork.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void getVirtualNetwork(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualNetworks().getByResourceGroupWithResponse("testrg", "HRVirtualNetwork", Context.NONE);
    }
}
```

### VirtualNetworks_List

```java
import com.azure.core.util.Context;

/** Samples for VirtualNetworks List. */
public final class VirtualNetworksListSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/ListVirtualNetworksBySubscription.json
     */
    /**
     * Sample code: ListVirtualNetworksBySubscription.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listVirtualNetworksBySubscription(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualNetworks().list(Context.NONE);
    }
}
```

### VirtualNetworks_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for VirtualNetworks ListByResourceGroup. */
public final class VirtualNetworksListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/ListVirtualNetworksByResourceGroup.json
     */
    /**
     * Sample code: ListVirtualNetworksByResourceGroup.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listVirtualNetworksByResourceGroup(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualNetworks().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### VirtualNetworks_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.scvmm.models.VirtualNetwork;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualNetworks Update. */
public final class VirtualNetworksUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/UpdateVirtualNetwork.json
     */
    /**
     * Sample code: UpdateVirtualNetwork.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void updateVirtualNetwork(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        VirtualNetwork resource =
            manager
                .virtualNetworks()
                .getByResourceGroupWithResponse("testrg", "HRVirtualNetwork", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### VmmServers_CreateOrUpdate

```java
import com.azure.resourcemanager.scvmm.models.ExtendedLocation;
import com.azure.resourcemanager.scvmm.models.VmmServerPropertiesCredentials;

/** Samples for VmmServers CreateOrUpdate. */
public final class VmmServersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/CreateVMMServer.json
     */
    /**
     * Sample code: CreateVMMServer.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void createVMMServer(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .vmmServers()
            .define("ContosoVMMServer")
            .withRegion("East US")
            .withExistingResourceGroup("testrg")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withType("customLocation")
                    .withName(
                        "/subscriptions/a5015e1c-867f-4533-8541-85cd470d0cfb/resourceGroups/demoRG/providers/Microsoft.Arc/customLocations/contoso"))
            .withFqdn("VMM.contoso.com")
            .withCredentials(new VmmServerPropertiesCredentials().withUsername("testuser").withPassword("password"))
            .withPort(1234)
            .create();
    }
}
```

### VmmServers_Delete

```java
import com.azure.core.util.Context;

/** Samples for VmmServers Delete. */
public final class VmmServersDeleteSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/DeleteVMMServer.json
     */
    /**
     * Sample code: DeleteVMMServer.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void deleteVMMServer(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.vmmServers().delete("testrg", "ContosoVMMServer", null, Context.NONE);
    }
}
```

### VmmServers_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for VmmServers GetByResourceGroup. */
public final class VmmServersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/GetVMMServer.json
     */
    /**
     * Sample code: GetVMMServer.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void getVMMServer(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.vmmServers().getByResourceGroupWithResponse("testrg", "ContosoVMMServer", Context.NONE);
    }
}
```

### VmmServers_List

```java
import com.azure.core.util.Context;

/** Samples for VmmServers List. */
public final class VmmServersListSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/ListVMMServersBySubscription.json
     */
    /**
     * Sample code: ListVmmServersBySubscription.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listVmmServersBySubscription(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.vmmServers().list(Context.NONE);
    }
}
```

### VmmServers_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for VmmServers ListByResourceGroup. */
public final class VmmServersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/ListVMMServersByResourceGroup.json
     */
    /**
     * Sample code: ListVmmServersByResourceGroup.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listVmmServersByResourceGroup(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.vmmServers().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### VmmServers_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.scvmm.models.VmmServer;
import java.util.HashMap;
import java.util.Map;

/** Samples for VmmServers Update. */
public final class VmmServersUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/UpdateVMMServer.json
     */
    /**
     * Sample code: UpdateVMMServer.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void updateVMMServer(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        VmmServer resource =
            manager.vmmServers().getByResourceGroupWithResponse("testrg", "ContosoVMMServer", Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

