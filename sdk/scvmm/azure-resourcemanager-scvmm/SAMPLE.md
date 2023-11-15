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

## VMInstanceGuestAgents

- [Create](#vminstanceguestagents_create)
- [Delete](#vminstanceguestagents_delete)
- [Get](#vminstanceguestagents_get)
- [List](#vminstanceguestagents_list)

## VirtualMachineInstanceHybridIdentityMetadata

- [Get](#virtualmachineinstancehybrididentitymetadata_get)
- [List](#virtualmachineinstancehybrididentitymetadata_list)

## VirtualMachineInstances

- [CreateCheckpoint](#virtualmachineinstances_createcheckpoint)
- [CreateOrUpdate](#virtualmachineinstances_createorupdate)
- [Delete](#virtualmachineinstances_delete)
- [DeleteCheckpoint](#virtualmachineinstances_deletecheckpoint)
- [Get](#virtualmachineinstances_get)
- [List](#virtualmachineinstances_list)
- [Restart](#virtualmachineinstances_restart)
- [RestoreCheckpoint](#virtualmachineinstances_restorecheckpoint)
- [Start](#virtualmachineinstances_start)
- [Stop](#virtualmachineinstances_stop)
- [Update](#virtualmachineinstances_update)

## VirtualMachineTemplates

- [CreateOrUpdate](#virtualmachinetemplates_createorupdate)
- [Delete](#virtualmachinetemplates_delete)
- [GetByResourceGroup](#virtualmachinetemplates_getbyresourcegroup)
- [List](#virtualmachinetemplates_list)
- [ListByResourceGroup](#virtualmachinetemplates_listbyresourcegroup)
- [Update](#virtualmachinetemplates_update)

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
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/CreateAvailabilitySet.json
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
/** Samples for AvailabilitySets Delete. */
public final class AvailabilitySetsDeleteSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/DeleteAvailabilitySet.json
     */
    /**
     * Sample code: DeleteAvailabilitySet.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void deleteAvailabilitySet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.availabilitySets().delete("testrg", "HRAvailabilitySet", null, com.azure.core.util.Context.NONE);
    }
}
```

### AvailabilitySets_GetByResourceGroup

```java
/** Samples for AvailabilitySets GetByResourceGroup. */
public final class AvailabilitySetsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/GetAvailabilitySet.json
     */
    /**
     * Sample code: GetAvailabilitySet.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void getAvailabilitySet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .availabilitySets()
            .getByResourceGroupWithResponse("testrg", "HRAvailabilitySet", com.azure.core.util.Context.NONE);
    }
}
```

### AvailabilitySets_List

```java
/** Samples for AvailabilitySets List. */
public final class AvailabilitySetsListSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/ListAvailabilitySetsBySubscription.json
     */
    /**
     * Sample code: ListAvailabilitySetsBySubscription.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listAvailabilitySetsBySubscription(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.availabilitySets().list(com.azure.core.util.Context.NONE);
    }
}
```

### AvailabilitySets_ListByResourceGroup

```java
/** Samples for AvailabilitySets ListByResourceGroup. */
public final class AvailabilitySetsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/ListAvailabilitySetsByResourceGroup.json
     */
    /**
     * Sample code: ListAvailabilitySetsByResourceGroup.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listAvailabilitySetsByResourceGroup(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.availabilitySets().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### AvailabilitySets_Update

```java
import com.azure.resourcemanager.scvmm.models.AvailabilitySet;
import java.util.HashMap;
import java.util.Map;

/** Samples for AvailabilitySets Update. */
public final class AvailabilitySetsUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/UpdateAvailabilitySet.json
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
                .getByResourceGroupWithResponse("testrg", "HRAvailabilitySet", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### Clouds_CreateOrUpdate

```java
import com.azure.resourcemanager.scvmm.models.ExtendedLocation;

/** Samples for Clouds CreateOrUpdate. */
public final class CloudsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/CreateCloud.json
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
            .withUuid("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
            .withVmmServerId(
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.SCVMM/VMMServers/ContosoVMMServer")
            .create();
    }
}
```

### Clouds_Delete

```java
/** Samples for Clouds Delete. */
public final class CloudsDeleteSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/DeleteCloud.json
     */
    /**
     * Sample code: DeleteCloud.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void deleteCloud(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.clouds().delete("testrg", "HRCloud", null, com.azure.core.util.Context.NONE);
    }
}
```

### Clouds_GetByResourceGroup

```java
/** Samples for Clouds GetByResourceGroup. */
public final class CloudsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/GetCloud.json
     */
    /**
     * Sample code: GetCloud.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void getCloud(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.clouds().getByResourceGroupWithResponse("testrg", "HRCloud", com.azure.core.util.Context.NONE);
    }
}
```

### Clouds_List

```java
/** Samples for Clouds List. */
public final class CloudsListSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/ListCloudsBySubscription.json
     */
    /**
     * Sample code: ListCloudsBySubscription.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listCloudsBySubscription(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.clouds().list(com.azure.core.util.Context.NONE);
    }
}
```

### Clouds_ListByResourceGroup

```java
/** Samples for Clouds ListByResourceGroup. */
public final class CloudsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/ListCloudsByResourceGroup.json
     */
    /**
     * Sample code: ListCloudsByResourceGroup.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listCloudsByResourceGroup(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.clouds().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### Clouds_Update

```java
import com.azure.resourcemanager.scvmm.models.Cloud;
import java.util.HashMap;
import java.util.Map;

/** Samples for Clouds Update. */
public final class CloudsUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/UpdateCloud.json
     */
    /**
     * Sample code: UpdateCloud.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void updateCloud(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        Cloud resource =
            manager
                .clouds()
                .getByResourceGroupWithResponse("testrg", "HRCloud", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### InventoryItems_Create

```java
import com.azure.resourcemanager.scvmm.models.CloudInventoryItem;

/** Samples for InventoryItems Create. */
public final class InventoryItemsCreateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/CreateInventoryItem.json
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
            .withProperties(new CloudInventoryItem())
            .create();
    }
}
```

### InventoryItems_Delete

```java
/** Samples for InventoryItems Delete. */
public final class InventoryItemsDeleteSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/DeleteInventoryItem.json
     */
    /**
     * Sample code: DeleteInventoryItem.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void deleteInventoryItem(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .inventoryItems()
            .deleteWithResponse(
                "testrg", "ContosoVMMServer", "12345678-1234-1234-1234-123456789abc", com.azure.core.util.Context.NONE);
    }
}
```

### InventoryItems_Get

```java
/** Samples for InventoryItems Get. */
public final class InventoryItemsGetSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/GetInventoryItem.json
     */
    /**
     * Sample code: GetInventoryItem.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void getInventoryItem(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .inventoryItems()
            .getWithResponse(
                "testrg", "ContosoVMMServer", "12345678-1234-1234-1234-123456789abc", com.azure.core.util.Context.NONE);
    }
}
```

### InventoryItems_ListByVmmServer

```java
/** Samples for InventoryItems ListByVmmServer. */
public final class InventoryItemsListByVmmServerSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/ListInventoryItemsByVMMServer.json
     */
    /**
     * Sample code: InventoryItemsListByVMMServer.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void inventoryItemsListByVMMServer(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.inventoryItems().listByVmmServer("testrg", "ContosoVMMServer", com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/ListOperations.json
     */
    /**
     * Sample code: ListOperations.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listOperations(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### VMInstanceGuestAgents_Create

```java
import com.azure.resourcemanager.scvmm.fluent.models.GuestAgentInner;
import com.azure.resourcemanager.scvmm.models.GuestCredential;
import com.azure.resourcemanager.scvmm.models.HttpProxyConfiguration;
import com.azure.resourcemanager.scvmm.models.ProvisioningAction;

/** Samples for VMInstanceGuestAgents Create. */
public final class VMInstanceGuestAgentsCreateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/CreateVMInstanceGuestAgent.json
     */
    /**
     * Sample code: CreateGuestAgent.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void createGuestAgent(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .vMInstanceGuestAgents()
            .create(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                new GuestAgentInner()
                    .withCredentials(
                        new GuestCredential().withUsername("tempuser").withPassword("fakeTokenPlaceholder"))
                    .withHttpProxyConfig(new HttpProxyConfiguration().withHttpsProxy("http://192.1.2.3:8080"))
                    .withProvisioningAction(ProvisioningAction.INSTALL),
                com.azure.core.util.Context.NONE);
    }
}
```

### VMInstanceGuestAgents_Delete

```java
/** Samples for VMInstanceGuestAgents Delete. */
public final class VMInstanceGuestAgentsDeleteSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/DeleteVMInstanceGuestAgent.json
     */
    /**
     * Sample code: DeleteGuestAgent.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void deleteGuestAgent(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .vMInstanceGuestAgents()
            .deleteWithResponse(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### VMInstanceGuestAgents_Get

```java
/** Samples for VMInstanceGuestAgents Get. */
public final class VMInstanceGuestAgentsGetSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/GetVMInstanceGuestAgent.json
     */
    /**
     * Sample code: GetGuestAgent.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void getGuestAgent(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .vMInstanceGuestAgents()
            .getWithResponse(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### VMInstanceGuestAgents_List

```java
/** Samples for VMInstanceGuestAgents List. */
public final class VMInstanceGuestAgentsListSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/VMInstanceGuestAgent_ListByVm.json
     */
    /**
     * Sample code: GuestAgentListByVm.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void guestAgentListByVm(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .vMInstanceGuestAgents()
            .list(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstanceHybridIdentityMetadata_Get

```java
/** Samples for VirtualMachineInstanceHybridIdentityMetadata Get. */
public final class VirtualMachineInstanceHybridIdentityMetadataGetSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/GetVmInstanceHybridIdentityMetadata.json
     */
    /**
     * Sample code: GetHybridIdentityMetadata.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void getHybridIdentityMetadata(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .virtualMachineInstanceHybridIdentityMetadatas()
            .getWithResponse(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstanceHybridIdentityMetadata_List

```java
/** Samples for VirtualMachineInstanceHybridIdentityMetadata List. */
public final class VirtualMachineInstanceHybridIdentityMetadataListSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/HybridIdentityMetadata_ListByVmInstance.json
     */
    /**
     * Sample code: HybridIdentityMetadataListByVm.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void hybridIdentityMetadataListByVm(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .virtualMachineInstanceHybridIdentityMetadatas()
            .list(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_CreateCheckpoint

```java
import com.azure.resourcemanager.scvmm.models.VirtualMachineCreateCheckpoint;

/** Samples for VirtualMachineInstances CreateCheckpoint. */
public final class VirtualMachineInstancesCreateCheckpointSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/CreateCheckpointVirtualMachineInstance.json
     */
    /**
     * Sample code: CreateCheckpointVirtualMachine.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void createCheckpointVirtualMachine(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .virtualMachineInstances()
            .createCheckpoint(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                new VirtualMachineCreateCheckpoint()
                    .withName("Demo Checkpoint name")
                    .withDescription("Demo Checkpoint description"),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_CreateOrUpdate

```java
import com.azure.resourcemanager.scvmm.fluent.models.VirtualMachineInstanceInner;
import com.azure.resourcemanager.scvmm.models.ExtendedLocation;
import com.azure.resourcemanager.scvmm.models.HardwareProfile;
import com.azure.resourcemanager.scvmm.models.InfrastructureProfile;

/** Samples for VirtualMachineInstances CreateOrUpdate. */
public final class VirtualMachineInstancesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/CreateVirtualMachineInstance.json
     */
    /**
     * Sample code: CreateVirtualMachine.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void createVirtualMachine(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .virtualMachineInstances()
            .createOrUpdate(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                new VirtualMachineInstanceInner()
                    .withExtendedLocation(
                        new ExtendedLocation()
                            .withType("customLocation")
                            .withName(
                                "/subscriptions/a5015e1c-867f-4533-8541-85cd470d0cfb/resourceGroups/demoRG/providers/Microsoft.ExtendedLocation/customLocations/contoso"))
                    .withHardwareProfile(new HardwareProfile().withMemoryMB(4196).withCpuCount(4))
                    .withInfrastructureProfile(
                        new InfrastructureProfile()
                            .withVmmServerId(
                                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.SCVMM/VMMServers/ContosoVMMServer")
                            .withCloudId(
                                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.SCVMM/Clouds/HRCloud")
                            .withTemplateId(
                                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.SCVMM/VirtualMachineTemplates/HRVirtualMachineTemplate")),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Delete

```java
/** Samples for VirtualMachineInstances Delete. */
public final class VirtualMachineInstancesDeleteSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/DeleteVirtualMachineInstance.json
     */
    /**
     * Sample code: DeleteVirtualMachine.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void deleteVirtualMachine(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .virtualMachineInstances()
            .delete(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                null,
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_DeleteCheckpoint

```java
import com.azure.resourcemanager.scvmm.models.VirtualMachineDeleteCheckpoint;

/** Samples for VirtualMachineInstances DeleteCheckpoint. */
public final class VirtualMachineInstancesDeleteCheckpointSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/DeleteCheckpointVirtualMachineInstance.json
     */
    /**
     * Sample code: DeleteCheckpointVirtualMachine.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void deleteCheckpointVirtualMachine(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .virtualMachineInstances()
            .deleteCheckpoint(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                new VirtualMachineDeleteCheckpoint().withId("Demo CheckpointID"),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Get

```java
/** Samples for VirtualMachineInstances Get. */
public final class VirtualMachineInstancesGetSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/GetVirtualMachineInstance.json
     */
    /**
     * Sample code: GetVirtualMachine.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void getVirtualMachine(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .virtualMachineInstances()
            .getWithResponse(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_List

```java
/** Samples for VirtualMachineInstances List. */
public final class VirtualMachineInstancesListSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/ListVirtualMachineInstances.json
     */
    /**
     * Sample code: ListVirtualMachines.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listVirtualMachines(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .virtualMachineInstances()
            .list(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Restart

```java
/** Samples for VirtualMachineInstances Restart. */
public final class VirtualMachineInstancesRestartSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/RestartVirtualMachineInstance.json
     */
    /**
     * Sample code: RestartVirtualMachine.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void restartVirtualMachine(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .virtualMachineInstances()
            .restart(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_RestoreCheckpoint

```java
import com.azure.resourcemanager.scvmm.models.VirtualMachineRestoreCheckpoint;

/** Samples for VirtualMachineInstances RestoreCheckpoint. */
public final class VirtualMachineInstancesRestoreCheckpointSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/RestoreCheckpointVirtualMachineInstance.json
     */
    /**
     * Sample code: RestoreCheckpointVirtualMachine.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void restoreCheckpointVirtualMachine(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .virtualMachineInstances()
            .restoreCheckpoint(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                new VirtualMachineRestoreCheckpoint().withId("Demo CheckpointID"),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Start

```java
/** Samples for VirtualMachineInstances Start. */
public final class VirtualMachineInstancesStartSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/StartVirtualMachineInstance.json
     */
    /**
     * Sample code: StartVirtualMachine.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void startVirtualMachine(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .virtualMachineInstances()
            .start(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Stop

```java
import com.azure.resourcemanager.scvmm.models.SkipShutdown;
import com.azure.resourcemanager.scvmm.models.StopVirtualMachineOptions;

/** Samples for VirtualMachineInstances Stop. */
public final class VirtualMachineInstancesStopSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/StopVirtualMachineInstance.json
     */
    /**
     * Sample code: StopVirtualMachine.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void stopVirtualMachine(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .virtualMachineInstances()
            .stop(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                new StopVirtualMachineOptions().withSkipShutdown(SkipShutdown.TRUE),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Update

```java
import com.azure.resourcemanager.scvmm.models.HardwareProfileUpdate;
import com.azure.resourcemanager.scvmm.models.VirtualMachineInstanceUpdate;

/** Samples for VirtualMachineInstances Update. */
public final class VirtualMachineInstancesUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/UpdateVirtualMachineInstance.json
     */
    /**
     * Sample code: UpdateVirtualMachine.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void updateVirtualMachine(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .virtualMachineInstances()
            .update(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                new VirtualMachineInstanceUpdate()
                    .withHardwareProfile(new HardwareProfileUpdate().withMemoryMB(4196).withCpuCount(4)),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineTemplates_CreateOrUpdate

```java
import com.azure.resourcemanager.scvmm.models.ExtendedLocation;

/** Samples for VirtualMachineTemplates CreateOrUpdate. */
public final class VirtualMachineTemplatesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/CreateVirtualMachineTemplate.json
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
            .withUuid("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
            .withVmmServerId(
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.SCVMM/VMMServers/ContosoVMMServer")
            .create();
    }
}
```

### VirtualMachineTemplates_Delete

```java
/** Samples for VirtualMachineTemplates Delete. */
public final class VirtualMachineTemplatesDeleteSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/DeleteVirtualMachineTemplate.json
     */
    /**
     * Sample code: DeleteVirtualMachineTemplate.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void deleteVirtualMachineTemplate(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .virtualMachineTemplates()
            .delete("testrg", "HRVirtualMachineTemplate", null, com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineTemplates_GetByResourceGroup

```java
/** Samples for VirtualMachineTemplates GetByResourceGroup. */
public final class VirtualMachineTemplatesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/GetVirtualMachineTemplate.json
     */
    /**
     * Sample code: GetVirtualMachineTemplate.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void getVirtualMachineTemplate(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .virtualMachineTemplates()
            .getByResourceGroupWithResponse("testrg", "HRVirtualMachineTemplate", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineTemplates_List

```java
/** Samples for VirtualMachineTemplates List. */
public final class VirtualMachineTemplatesListSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/ListVirtualMachineTemplatesBySubscription.json
     */
    /**
     * Sample code: ListVirtualMachineTemplatesBySubscription.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listVirtualMachineTemplatesBySubscription(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineTemplates().list(com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineTemplates_ListByResourceGroup

```java
/** Samples for VirtualMachineTemplates ListByResourceGroup. */
public final class VirtualMachineTemplatesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/ListVirtualMachineTemplatesByResourceGroup.json
     */
    /**
     * Sample code: ListVirtualMachineTemplatesByResourceGroup.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listVirtualMachineTemplatesByResourceGroup(
        com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineTemplates().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineTemplates_Update

```java
import com.azure.resourcemanager.scvmm.models.VirtualMachineTemplate;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualMachineTemplates Update. */
public final class VirtualMachineTemplatesUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/UpdateVirtualMachineTemplate.json
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
                .getByResourceGroupWithResponse("testrg", "HRVirtualMachineTemplate", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### VirtualNetworks_CreateOrUpdate

```java
import com.azure.resourcemanager.scvmm.models.ExtendedLocation;

/** Samples for VirtualNetworks CreateOrUpdate. */
public final class VirtualNetworksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/CreateVirtualNetwork.json
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
            .withUuid("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
            .withVmmServerId(
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.SCVMM/VMMServers/ContosoVMMServer")
            .create();
    }
}
```

### VirtualNetworks_Delete

```java
/** Samples for VirtualNetworks Delete. */
public final class VirtualNetworksDeleteSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/DeleteVirtualNetwork.json
     */
    /**
     * Sample code: DeleteVirtualNetwork.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void deleteVirtualNetwork(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualNetworks().delete("testrg", "HRVirtualNetwork", null, com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworks_GetByResourceGroup

```java
/** Samples for VirtualNetworks GetByResourceGroup. */
public final class VirtualNetworksGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/GetVirtualNetwork.json
     */
    /**
     * Sample code: GetVirtualNetwork.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void getVirtualNetwork(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .virtualNetworks()
            .getByResourceGroupWithResponse("testrg", "HRVirtualNetwork", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworks_List

```java
/** Samples for VirtualNetworks List. */
public final class VirtualNetworksListSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/ListVirtualNetworksBySubscription.json
     */
    /**
     * Sample code: ListVirtualNetworksBySubscription.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listVirtualNetworksBySubscription(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualNetworks().list(com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworks_ListByResourceGroup

```java
/** Samples for VirtualNetworks ListByResourceGroup. */
public final class VirtualNetworksListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/ListVirtualNetworksByResourceGroup.json
     */
    /**
     * Sample code: ListVirtualNetworksByResourceGroup.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listVirtualNetworksByResourceGroup(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualNetworks().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworks_Update

```java
import com.azure.resourcemanager.scvmm.models.VirtualNetwork;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualNetworks Update. */
public final class VirtualNetworksUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/UpdateVirtualNetwork.json
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
                .getByResourceGroupWithResponse("testrg", "HRVirtualNetwork", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### VmmServers_CreateOrUpdate

```java
import com.azure.resourcemanager.scvmm.models.ExtendedLocation;
import com.azure.resourcemanager.scvmm.models.VmmCredential;

/** Samples for VmmServers CreateOrUpdate. */
public final class VmmServersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/CreateVMMServer.json
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
            .withCredentials(new VmmCredential().withUsername("testuser").withPassword("fakeTokenPlaceholder"))
            .withPort(1234)
            .create();
    }
}
```

### VmmServers_Delete

```java
/** Samples for VmmServers Delete. */
public final class VmmServersDeleteSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/DeleteVMMServer.json
     */
    /**
     * Sample code: DeleteVMMServer.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void deleteVMMServer(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.vmmServers().delete("testrg", "ContosoVMMServer", null, com.azure.core.util.Context.NONE);
    }
}
```

### VmmServers_GetByResourceGroup

```java
/** Samples for VmmServers GetByResourceGroup. */
public final class VmmServersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/GetVMMServer.json
     */
    /**
     * Sample code: GetVMMServer.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void getVMMServer(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager
            .vmmServers()
            .getByResourceGroupWithResponse("testrg", "ContosoVMMServer", com.azure.core.util.Context.NONE);
    }
}
```

### VmmServers_List

```java
/** Samples for VmmServers List. */
public final class VmmServersListSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/ListVMMServersBySubscription.json
     */
    /**
     * Sample code: ListVmmServersBySubscription.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listVmmServersBySubscription(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.vmmServers().list(com.azure.core.util.Context.NONE);
    }
}
```

### VmmServers_ListByResourceGroup

```java
/** Samples for VmmServers ListByResourceGroup. */
public final class VmmServersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/ListVMMServersByResourceGroup.json
     */
    /**
     * Sample code: ListVmmServersByResourceGroup.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void listVmmServersByResourceGroup(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.vmmServers().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### VmmServers_Update

```java
import com.azure.resourcemanager.scvmm.models.VmmServer;
import java.util.HashMap;
import java.util.Map;

/** Samples for VmmServers Update. */
public final class VmmServersUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/UpdateVMMServer.json
     */
    /**
     * Sample code: UpdateVMMServer.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void updateVMMServer(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        VmmServer resource =
            manager
                .vmmServers()
                .getByResourceGroupWithResponse("testrg", "ContosoVMMServer", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

