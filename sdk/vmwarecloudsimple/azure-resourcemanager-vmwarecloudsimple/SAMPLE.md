# Code snippets and samples


## CustomizationPolicies

- [Get](#customizationpolicies_get)
- [List](#customizationpolicies_list)

## DedicatedCloudNodes

- [CreateOrUpdate](#dedicatedcloudnodes_createorupdate)
- [Delete](#dedicatedcloudnodes_delete)
- [GetByResourceGroup](#dedicatedcloudnodes_getbyresourcegroup)
- [List](#dedicatedcloudnodes_list)
- [ListByResourceGroup](#dedicatedcloudnodes_listbyresourcegroup)
- [Update](#dedicatedcloudnodes_update)

## DedicatedCloudServices

- [CreateOrUpdate](#dedicatedcloudservices_createorupdate)
- [Delete](#dedicatedcloudservices_delete)
- [GetByResourceGroup](#dedicatedcloudservices_getbyresourcegroup)
- [List](#dedicatedcloudservices_list)
- [ListByResourceGroup](#dedicatedcloudservices_listbyresourcegroup)
- [Update](#dedicatedcloudservices_update)

## Operations

- [Get](#operations_get)
- [List](#operations_list)

## PrivateClouds

- [Get](#privateclouds_get)
- [List](#privateclouds_list)

## ResourcePools

- [Get](#resourcepools_get)
- [List](#resourcepools_list)

## SkusAvailability

- [List](#skusavailability_list)

## Usages

- [List](#usages_list)

## VirtualMachineTemplates

- [Get](#virtualmachinetemplates_get)
- [List](#virtualmachinetemplates_list)

## VirtualMachines

- [CreateOrUpdate](#virtualmachines_createorupdate)
- [Delete](#virtualmachines_delete)
- [GetByResourceGroup](#virtualmachines_getbyresourcegroup)
- [List](#virtualmachines_list)
- [ListByResourceGroup](#virtualmachines_listbyresourcegroup)
- [Start](#virtualmachines_start)
- [Stop](#virtualmachines_stop)
- [Update](#virtualmachines_update)

## VirtualNetworks

- [Get](#virtualnetworks_get)
- [List](#virtualnetworks_list)
### CustomizationPolicies_Get

```java
/** Samples for CustomizationPolicies Get. */
public final class CustomizationPoliciesGetSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/GetCustomizationPolicy.json
     */
    /**
     * Sample code: GetCustomizationPolicy.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void getCustomizationPolicy(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager
            .customizationPolicies()
            .getWithResponse("myResourceGroup", "myPrivateCloud", "Linux1", com.azure.core.util.Context.NONE);
    }
}
```

### CustomizationPolicies_List

```java
/** Samples for CustomizationPolicies List. */
public final class CustomizationPoliciesListSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/ListCustomizationPolicies.json
     */
    /**
     * Sample code: ListCustomizationPolicies.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void listCustomizationPolicies(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager
            .customizationPolicies()
            .list("myResourceGroup", "myPrivateCloud", null, com.azure.core.util.Context.NONE);
    }
}
```

### DedicatedCloudNodes_CreateOrUpdate

```java
import com.azure.resourcemanager.vmwarecloudsimple.models.Sku;
import java.util.UUID;

/** Samples for DedicatedCloudNodes CreateOrUpdate. */
public final class DedicatedCloudNodesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/CreateDedicatedCloudNode.json
     */
    /**
     * Sample code: CreateDedicatedCloudNode.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void createDedicatedCloudNode(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager
            .dedicatedCloudNodes()
            .define("myNode")
            .withRegion("westus")
            .withExistingResourceGroup("myResourceGroup")
            .withSku(new Sku().withName("VMware_CloudSimple_CS28"))
            .withAvailabilityZoneId("az1")
            .withNodesCount(1)
            .withPlacementGroupId("n1")
            .withPurchaseId(UUID.fromString("56acbd46-3d36-4bbf-9b08-57c30fdf6932"))
            .withIdPropertiesId("general")
            .withNamePropertiesName("CS28-Node")
            .withReferer("https://management.azure.com/")
            .create();
    }
}
```

### DedicatedCloudNodes_Delete

```java
/** Samples for DedicatedCloudNodes Delete. */
public final class DedicatedCloudNodesDeleteSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/DeleteDedicatedCloudNode.json
     */
    /**
     * Sample code: DeleteDedicatedCloudNode.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void deleteDedicatedCloudNode(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager
            .dedicatedCloudNodes()
            .deleteByResourceGroupWithResponse("myResourceGroup", "myNode", com.azure.core.util.Context.NONE);
    }
}
```

### DedicatedCloudNodes_GetByResourceGroup

```java
/** Samples for DedicatedCloudNodes GetByResourceGroup. */
public final class DedicatedCloudNodesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/GetDedicatedCloudNode.json
     */
    /**
     * Sample code: GetDedicatedCloudNode.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void getDedicatedCloudNode(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager
            .dedicatedCloudNodes()
            .getByResourceGroupWithResponse("myResourceGroup", "myNode", com.azure.core.util.Context.NONE);
    }
}
```

### DedicatedCloudNodes_List

```java
/** Samples for DedicatedCloudNodes List. */
public final class DedicatedCloudNodesListSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/ListDedicatedCloudNodes.json
     */
    /**
     * Sample code: ListDedicatedCloudNodes.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void listDedicatedCloudNodes(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager.dedicatedCloudNodes().list(null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### DedicatedCloudNodes_ListByResourceGroup

```java
/** Samples for DedicatedCloudNodes ListByResourceGroup. */
public final class DedicatedCloudNodesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/ListRGDedicatedCloudNodes.json
     */
    /**
     * Sample code: ListRGDedicatedCloudNodes.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void listRGDedicatedCloudNodes(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager
            .dedicatedCloudNodes()
            .listByResourceGroup("myResourceGroup", null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### DedicatedCloudNodes_Update

```java
import com.azure.resourcemanager.vmwarecloudsimple.models.DedicatedCloudNode;
import java.util.HashMap;
import java.util.Map;

/** Samples for DedicatedCloudNodes Update. */
public final class DedicatedCloudNodesUpdateSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/PatchDedicatedCloudNode.json
     */
    /**
     * Sample code: PatchDedicatedCloudNode.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void patchDedicatedCloudNode(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        DedicatedCloudNode resource =
            manager
                .dedicatedCloudNodes()
                .getByResourceGroupWithResponse("myResourceGroup", "myNode", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("myTag", "tagValue")).apply();
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

### DedicatedCloudServices_CreateOrUpdate

```java
/** Samples for DedicatedCloudServices CreateOrUpdate. */
public final class DedicatedCloudServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/CreateDedicatedCloudService.json
     */
    /**
     * Sample code: CreateDedicatedCloudService.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void createDedicatedCloudService(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager
            .dedicatedCloudServices()
            .define("myService")
            .withRegion("westus")
            .withExistingResourceGroup("myResourceGroup")
            .withGatewaySubnet("10.0.0.0")
            .create();
    }
}
```

### DedicatedCloudServices_Delete

```java
/** Samples for DedicatedCloudServices Delete. */
public final class DedicatedCloudServicesDeleteSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/DeleteDedicatedCloudService.json
     */
    /**
     * Sample code: DeleteDedicatedCloudService.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void deleteDedicatedCloudService(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager.dedicatedCloudServices().delete("myResourceGroup", "myService", com.azure.core.util.Context.NONE);
    }
}
```

### DedicatedCloudServices_GetByResourceGroup

```java
/** Samples for DedicatedCloudServices GetByResourceGroup. */
public final class DedicatedCloudServicesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/GetDedicatedCloudService.json
     */
    /**
     * Sample code: GetDedicatedCloudService.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void getDedicatedCloudService(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager
            .dedicatedCloudServices()
            .getByResourceGroupWithResponse("myResourceGroup", "myService", com.azure.core.util.Context.NONE);
    }
}
```

### DedicatedCloudServices_List

```java
/** Samples for DedicatedCloudServices List. */
public final class DedicatedCloudServicesListSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/ListDedicatedCloudServices.json
     */
    /**
     * Sample code: ListDedicatedCloudServices.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void listDedicatedCloudServices(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager.dedicatedCloudServices().list(null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### DedicatedCloudServices_ListByResourceGroup

```java
/** Samples for DedicatedCloudServices ListByResourceGroup. */
public final class DedicatedCloudServicesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/ListRGDedicatedCloudServices.json
     */
    /**
     * Sample code: ListRGDedicatedCloudServices.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void listRGDedicatedCloudServices(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager
            .dedicatedCloudServices()
            .listByResourceGroup("myResourceGroup", null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### DedicatedCloudServices_Update

```java
import com.azure.resourcemanager.vmwarecloudsimple.models.DedicatedCloudService;
import java.util.HashMap;
import java.util.Map;

/** Samples for DedicatedCloudServices Update. */
public final class DedicatedCloudServicesUpdateSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/PatchDedicatedService.json
     */
    /**
     * Sample code: PatchDedicatedService.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void patchDedicatedService(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        DedicatedCloudService resource =
            manager
                .dedicatedCloudServices()
                .getByResourceGroupWithResponse("myResourceGroup", "myService", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("myTag", "tagValue")).apply();
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

### Operations_Get

```java
/** Samples for Operations Get. */
public final class OperationsGetSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/GetFailedOperationResult.json
     */
    /**
     * Sample code: GetFailedOperationResult.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void getFailedOperationResult(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager
            .operations()
            .getWithResponse(
                "westus2",
                "https://management.azure.com/",
                "d030bb3f-7d53-11e9-8e09-9a86872085ff",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/GetOperationResult.json
     */
    /**
     * Sample code: GetOperationResult.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void getOperationResult(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager
            .operations()
            .getWithResponse(
                "westus2",
                "https://management.azure.com/",
                "f8e1c8f1-7d52-11e9-8e07-9a86872085ff",
                com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/ListOperations.json
     */
    /**
     * Sample code: ListOperations.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void listOperations(com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateClouds_Get

```java
/** Samples for PrivateClouds Get. */
public final class PrivateCloudsGetSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/GetPrivateCloud.json
     */
    /**
     * Sample code: GetPrivateCloud.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void getPrivateCloud(com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager.privateClouds().getWithResponse("myPrivateCloud", "westus2", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateClouds_List

```java
/** Samples for PrivateClouds List. */
public final class PrivateCloudsListSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/ListPrivateCloudInLocation.json
     */
    /**
     * Sample code: ListPrivateCloudInLocation.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void listPrivateCloudInLocation(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager.privateClouds().list("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### ResourcePools_Get

```java
/** Samples for ResourcePools Get. */
public final class ResourcePoolsGetSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/GetResourcePool.json
     */
    /**
     * Sample code: GetResourcePool.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void getResourcePool(com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager
            .resourcePools()
            .getWithResponse("westus2", "myPrivateCloud", "resgroup-26", com.azure.core.util.Context.NONE);
    }
}
```

### ResourcePools_List

```java
/** Samples for ResourcePools List. */
public final class ResourcePoolsListSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/ListResourcePools.json
     */
    /**
     * Sample code: ListResourcePools.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void listResourcePools(com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager.resourcePools().list("westus2", "myPrivateCloud", com.azure.core.util.Context.NONE);
    }
}
```

### SkusAvailability_List

```java
/** Samples for SkusAvailability List. */
public final class SkusAvailabilityListSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/ListAvailabilities.json
     */
    /**
     * Sample code: ListAvailabilities.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void listAvailabilities(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager.skusAvailabilities().list("westus2", null, com.azure.core.util.Context.NONE);
    }
}
```

### Usages_List

```java
/** Samples for Usages List. */
public final class UsagesListSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/ListUsages.json
     */
    /**
     * Sample code: ListUsages.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void listUsages(com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager.usages().list("westus2", null, com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineTemplates_Get

```java
/** Samples for VirtualMachineTemplates Get. */
public final class VirtualMachineTemplatesGetSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/GetVirtualMachineTemplate.json
     */
    /**
     * Sample code: GetVirtualMachineTemplate.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void getVirtualMachineTemplate(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager
            .virtualMachineTemplates()
            .getWithResponse("westus2", "myPrivateCloud", "vm-34", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineTemplates_List

```java
/** Samples for VirtualMachineTemplates List. */
public final class VirtualMachineTemplatesListSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/ListVirtualMachineTemplates.json
     */
    /**
     * Sample code: ListVirtualMachineTemplates.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void listVirtualMachineTemplates(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager
            .virtualMachineTemplates()
            .list(
                "myPrivateCloud",
                "westus2",
                "/subscriptions/{subscription-id}/providers/Microsoft.VMwareCloudSimple/locations/westus2/privateClouds/myPrivateCloud/resourcePools/resgroup-26",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachines_CreateOrUpdate

```java
import com.azure.resourcemanager.vmwarecloudsimple.fluent.models.ResourcePoolInner;
import com.azure.resourcemanager.vmwarecloudsimple.fluent.models.VirtualNetworkInner;
import com.azure.resourcemanager.vmwarecloudsimple.fluent.models.VirtualNicInner;
import com.azure.resourcemanager.vmwarecloudsimple.models.DiskIndependenceMode;
import com.azure.resourcemanager.vmwarecloudsimple.models.NicType;
import com.azure.resourcemanager.vmwarecloudsimple.models.VirtualDisk;
import java.util.Arrays;

/** Samples for VirtualMachines CreateOrUpdate. */
public final class VirtualMachinesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/CreateVirtualMachine.json
     */
    /**
     * Sample code: CreateVirtualMachine.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void createVirtualMachine(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager
            .virtualMachines()
            .define("myVirtualMachine")
            .withRegion("westus2")
            .withExistingResourceGroup("myResourceGroup")
            .withAmountOfRam(4096)
            .withDisks(
                Arrays
                    .asList(
                        new VirtualDisk()
                            .withControllerId("1000")
                            .withIndependenceMode(DiskIndependenceMode.PERSISTENT)
                            .withTotalSize(10485760)
                            .withVirtualDiskId("2000")))
            .withNics(
                Arrays
                    .asList(
                        new VirtualNicInner()
                            .withNetwork(
                                new VirtualNetworkInner()
                                    .withId(
                                        "/subscriptions/{subscription-id}/providers/Microsoft.VMwareCloudSimple/locations/westus2/privateClouds/myPrivateCloud/virtualNetworks/dvportgroup-19"))
                            .withNicType(NicType.E1000)
                            .withPowerOnBoot(true)
                            .withVirtualNicId("4000")))
            .withNumberOfCores(2)
            .withPrivateCloudId(
                "/subscriptions/{subscription-id}/providers/Microsoft.VMwareCloudSimple/locations/westus2/privateClouds/myPrivateCloud")
            .withResourcePool(
                new ResourcePoolInner()
                    .withId(
                        "/subscriptions/{subscription-id}/providers/Microsoft.VMwareCloudSimple/locations/westus2/privateClouds/myPrivateCloud/resourcePools/resgroup-26"))
            .withTemplateId(
                "/subscriptions/{subscription-id}/providers/Microsoft.VMwareCloudSimple/locations/westus2/privateClouds/myPrivateCloud/virtualMachineTemplates/vm-34")
            .withReferer("https://management.azure.com/")
            .create();
    }
}
```

### VirtualMachines_Delete

```java
/** Samples for VirtualMachines Delete. */
public final class VirtualMachinesDeleteSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/DeleteVirtualMachine.json
     */
    /**
     * Sample code: DeleteVirtualMachine.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void deleteVirtualMachine(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager
            .virtualMachines()
            .delete(
                "myResourceGroup",
                "https://management.azure.com/",
                "myVirtualMachine",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachines_GetByResourceGroup

```java
/** Samples for VirtualMachines GetByResourceGroup. */
public final class VirtualMachinesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/GetVirtualMachine.json
     */
    /**
     * Sample code: GetVirtualMachine.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void getVirtualMachine(com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager
            .virtualMachines()
            .getByResourceGroupWithResponse("myResourceGroup", "myVirtualMachine", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachines_List

```java
/** Samples for VirtualMachines List. */
public final class VirtualMachinesListSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/ListVirtualMachines.json
     */
    /**
     * Sample code: ListVirtualMachines.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void listVirtualMachines(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager.virtualMachines().list(null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachines_ListByResourceGroup

```java
/** Samples for VirtualMachines ListByResourceGroup. */
public final class VirtualMachinesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/ListRGVirtualMachines.json
     */
    /**
     * Sample code: ListRGVirtualMachines.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void listRGVirtualMachines(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager
            .virtualMachines()
            .listByResourceGroup("myResourceGroup", null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachines_Start

```java
/** Samples for VirtualMachines Start. */
public final class VirtualMachinesStartSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/StartVirtualMachine.json
     */
    /**
     * Sample code: StartVirtualMachine.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void startVirtualMachine(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager
            .virtualMachines()
            .start(
                "myResourceGroup",
                "https://management.azure.com/",
                "myVirtualMachine",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachines_Stop

```java
import com.azure.resourcemanager.vmwarecloudsimple.models.StopMode;
import com.azure.resourcemanager.vmwarecloudsimple.models.VirtualMachineStopMode;

/** Samples for VirtualMachines Stop. */
public final class VirtualMachinesStopSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/StopInBodyVirtualMachine.json
     */
    /**
     * Sample code: StopInBodyVirtualMachine.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void stopInBodyVirtualMachine(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager
            .virtualMachines()
            .stop(
                "myResourceGroup",
                "https://management.azure.com/",
                "myVirtualMachine",
                null,
                new VirtualMachineStopMode(),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/StopInQueryVirtualMachine.json
     */
    /**
     * Sample code: StopInQueryVirtualMachine.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void stopInQueryVirtualMachine(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager
            .virtualMachines()
            .stop(
                "myResourceGroup",
                "https://management.azure.com/",
                "myVirtualMachine",
                StopMode.SUSPEND,
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachines_Update

```java
import com.azure.resourcemanager.vmwarecloudsimple.models.VirtualMachine;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualMachines Update. */
public final class VirtualMachinesUpdateSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/PatchVirtualMachine.json
     */
    /**
     * Sample code: PatchVirtualMachine.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void patchVirtualMachine(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        VirtualMachine resource =
            manager
                .virtualMachines()
                .getByResourceGroupWithResponse("myResourceGroup", "myVirtualMachine", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("myTag", "tagValue")).apply();
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

### VirtualNetworks_Get

```java
/** Samples for VirtualNetworks Get. */
public final class VirtualNetworksGetSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/GetVirtualNetwork.json
     */
    /**
     * Sample code: GetVirtualNetwork.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void getVirtualNetwork(com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager
            .virtualNetworks()
            .getWithResponse("westus2", "myPrivateCloud", "dvportgroup-19", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworks_List

```java
/** Samples for VirtualNetworks List. */
public final class VirtualNetworksListSamples {
    /*
     * x-ms-original-file: specification/vmwarecloudsimple/resource-manager/Microsoft.VMwareCloudSimple/stable/2019-04-01/examples/ListVirtualNetworks.json
     */
    /**
     * Sample code: ListVirtualNetworks.
     *
     * @param manager Entry point to VMwareCloudSimpleManager.
     */
    public static void listVirtualNetworks(
        com.azure.resourcemanager.vmwarecloudsimple.VMwareCloudSimpleManager manager) {
        manager
            .virtualNetworks()
            .list(
                "westus2",
                "myPrivateCloud",
                "/subscriptions/{subscription-id}/providers/Microsoft.VMwareCloudSimple/locations/westus2/privateClouds/myPrivateCloud/resourcePools/resgroup-26",
                com.azure.core.util.Context.NONE);
    }
}
```

