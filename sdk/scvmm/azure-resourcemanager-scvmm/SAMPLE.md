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

## GuestAgents

- [Create](#guestagents_create)
- [Delete](#guestagents_delete)
- [Get](#guestagents_get)
- [ListByVirtualMachineInstance](#guestagents_listbyvirtualmachineinstance)

## InventoryItems

- [Create](#inventoryitems_create)
- [Delete](#inventoryitems_delete)
- [Get](#inventoryitems_get)
- [ListByVmmServer](#inventoryitems_listbyvmmserver)

## Operations

- [List](#operations_list)

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

## VmInstanceHybridIdentityMetadatas

- [Get](#vminstancehybrididentitymetadatas_get)
- [ListByVirtualMachineInstance](#vminstancehybrididentitymetadatas_listbyvirtualmachineinstance)

## VmmServers

- [CreateOrUpdate](#vmmservers_createorupdate)
- [Delete](#vmmservers_delete)
- [GetByResourceGroup](#vmmservers_getbyresourcegroup)
- [List](#vmmservers_list)
- [ListByResourceGroup](#vmmservers_listbyresourcegroup)
- [Update](#vmmservers_update)
### AvailabilitySets_CreateOrUpdate

```java
import com.azure.resourcemanager.scvmm.models.AvailabilitySetProperties;
import com.azure.resourcemanager.scvmm.models.ExtendedLocation;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AvailabilitySets CreateOrUpdate.
 */
public final class AvailabilitySetsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * AvailabilitySets_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: AvailabilitySets_CreateOrUpdate_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void availabilitySetsCreateOrUpdateMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.availabilitySets()
            .define("_")
            .withRegion("jelevilan")
            .withExistingResourceGroup("rgscvmm")
            .withExtendedLocation(new ExtendedLocation())
            .create();
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * AvailabilitySets_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvailabilitySets_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void availabilitySetsCreateOrUpdateMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.availabilitySets()
            .define("-")
            .withRegion("jelevilan")
            .withExistingResourceGroup("rgscvmm")
            .withExtendedLocation(new ExtendedLocation().withType("customLocation")
                .withName(
                    "/subscriptions/12345678-1234-1234-1234-12345678abc/resourceGroups/exampleResourceGroup/providers/Microsoft.ExtendedLocation/customLocations/customLocationName"))
            .withTags(mapOf("key5701", "fakeTokenPlaceholder"))
            .withProperties(new AvailabilitySetProperties().withAvailabilitySetName("njrpftunzo")
                .withVmmServerId(
                    "/subscriptions/12345678-1234-1234-1234-12345678abc/resourceGroups/exampleResourceGroup/providers/Microsoft.ScVmm/vmmServers/vmmServerName"))
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

### AvailabilitySets_Delete

```java
import com.azure.resourcemanager.scvmm.models.ForceDelete;

/**
 * Samples for AvailabilitySets Delete.
 */
public final class AvailabilitySetsDeleteSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * AvailabilitySets_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvailabilitySets_Delete_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void availabilitySetsDeleteMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.availabilitySets().delete("rgscvmm", "_", ForceDelete.TRUE, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * AvailabilitySets_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: AvailabilitySets_Delete_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void availabilitySetsDeleteMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.availabilitySets().delete("rgscvmm", "6", null, com.azure.core.util.Context.NONE);
    }
}
```

### AvailabilitySets_GetByResourceGroup

```java
/**
 * Samples for AvailabilitySets GetByResourceGroup.
 */
public final class AvailabilitySetsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * AvailabilitySets_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: AvailabilitySets_Get_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void availabilitySetsGetMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.availabilitySets().getByResourceGroupWithResponse("rgscvmm", "V", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * AvailabilitySets_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvailabilitySets_Get_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void availabilitySetsGetMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.availabilitySets().getByResourceGroupWithResponse("rgscvmm", "-", com.azure.core.util.Context.NONE);
    }
}
```

### AvailabilitySets_List

```java
/**
 * Samples for AvailabilitySets List.
 */
public final class AvailabilitySetsListSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * AvailabilitySets_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: AvailabilitySets_ListBySubscription_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        availabilitySetsListBySubscriptionMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.availabilitySets().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * AvailabilitySets_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvailabilitySets_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        availabilitySetsListBySubscriptionMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.availabilitySets().list(com.azure.core.util.Context.NONE);
    }
}
```

### AvailabilitySets_ListByResourceGroup

```java
/**
 * Samples for AvailabilitySets ListByResourceGroup.
 */
public final class AvailabilitySetsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * AvailabilitySets_ListByResourceGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: AvailabilitySets_ListByResourceGroup_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        availabilitySetsListByResourceGroupMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.availabilitySets().listByResourceGroup("rgscvmm", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * AvailabilitySets_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvailabilitySets_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        availabilitySetsListByResourceGroupMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.availabilitySets().listByResourceGroup("rgscvmm", com.azure.core.util.Context.NONE);
    }
}
```

### AvailabilitySets_Update

```java
import com.azure.resourcemanager.scvmm.models.AvailabilitySet;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AvailabilitySets Update.
 */
public final class AvailabilitySetsUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * AvailabilitySets_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: AvailabilitySets_Update_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void availabilitySetsUpdateMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        AvailabilitySet resource = manager.availabilitySets()
            .getByResourceGroupWithResponse("rgscvmm", "1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * AvailabilitySets_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvailabilitySets_Update_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void availabilitySetsUpdateMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        AvailabilitySet resource = manager.availabilitySets()
            .getByResourceGroupWithResponse("rgscvmm", "-", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key1460", "fakeTokenPlaceholder")).apply();
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
import com.azure.resourcemanager.scvmm.models.CloudProperties;
import com.azure.resourcemanager.scvmm.models.ExtendedLocation;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Clouds CreateOrUpdate.
 */
public final class CloudsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * Clouds_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: Clouds_CreateOrUpdate_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void cloudsCreateOrUpdateMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.clouds()
            .define("-")
            .withRegion("khwsdmaxfhmbu")
            .withExistingResourceGroup("rgscvmm")
            .withExtendedLocation(new ExtendedLocation())
            .create();
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * Clouds_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Clouds_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void cloudsCreateOrUpdateMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.clouds()
            .define("2")
            .withRegion("khwsdmaxfhmbu")
            .withExistingResourceGroup("rgscvmm")
            .withExtendedLocation(new ExtendedLocation().withType("customLocation")
                .withName(
                    "/subscriptions/12345678-1234-1234-1234-12345678abc/resourceGroups/exampleResourceGroup/providers/Microsoft.ExtendedLocation/customLocations/customLocationName"))
            .withTags(mapOf("key4295", "fakeTokenPlaceholder"))
            .withProperties(new CloudProperties().withInventoryItemId("qjd")
                .withUuid("12345678-1234-1234-1234-12345678abcd")
                .withVmmServerId(
                    "/subscriptions/12345678-1234-1234-1234-12345678abc/resourceGroups/exampleResourceGroup/providers/Microsoft.ScVmm/vmmServers/vmmServerName"))
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

### Clouds_Delete

```java
import com.azure.resourcemanager.scvmm.models.ForceDelete;

/**
 * Samples for Clouds Delete.
 */
public final class CloudsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/Clouds_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Clouds_Delete_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void cloudsDeleteMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.clouds().delete("rgscvmm", "-", ForceDelete.TRUE, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/Clouds_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: Clouds_Delete_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void cloudsDeleteMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.clouds().delete("rgscvmm", "1", null, com.azure.core.util.Context.NONE);
    }
}
```

### Clouds_GetByResourceGroup

```java
/**
 * Samples for Clouds GetByResourceGroup.
 */
public final class CloudsGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/Clouds_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Clouds_Get_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void cloudsGetMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.clouds().getByResourceGroupWithResponse("rgscvmm", "_", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/Clouds_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: Clouds_Get_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void cloudsGetMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.clouds().getByResourceGroupWithResponse("rgscvmm", "i", com.azure.core.util.Context.NONE);
    }
}
```

### Clouds_List

```java
/**
 * Samples for Clouds List.
 */
public final class CloudsListSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * Clouds_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: Clouds_ListBySubscription_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void cloudsListBySubscriptionMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.clouds().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * Clouds_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: Clouds_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void cloudsListBySubscriptionMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.clouds().list(com.azure.core.util.Context.NONE);
    }
}
```

### Clouds_ListByResourceGroup

```java
/**
 * Samples for Clouds ListByResourceGroup.
 */
public final class CloudsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * Clouds_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Clouds_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void cloudsListByResourceGroupMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.clouds().listByResourceGroup("rgscvmm", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * Clouds_ListByResourceGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: Clouds_ListByResourceGroup_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void cloudsListByResourceGroupMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.clouds().listByResourceGroup("rgscvmm", com.azure.core.util.Context.NONE);
    }
}
```

### Clouds_Update

```java
import com.azure.resourcemanager.scvmm.models.Cloud;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Clouds Update.
 */
public final class CloudsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/Clouds_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: Clouds_Update_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void cloudsUpdateMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        Cloud resource = manager.clouds()
            .getByResourceGroupWithResponse("rgscvmm", "_", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }

    /*
     * x-ms-original-file:
     * specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/Clouds_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Clouds_Update_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void cloudsUpdateMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        Cloud resource = manager.clouds()
            .getByResourceGroupWithResponse("rgscvmm", "P", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key5266", "fakeTokenPlaceholder")).apply();
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

### GuestAgents_Create

```java
import com.azure.resourcemanager.scvmm.fluent.models.GuestAgentInner;
import com.azure.resourcemanager.scvmm.models.GuestAgentProperties;
import com.azure.resourcemanager.scvmm.models.GuestCredential;
import com.azure.resourcemanager.scvmm.models.HttpProxyConfiguration;
import com.azure.resourcemanager.scvmm.models.ProvisioningAction;

/**
 * Samples for GuestAgents Create.
 */
public final class GuestAgentsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/GuestAgents_Create_MinimumSet_Gen
     * .json
     */
    /**
     * Sample code: GuestAgents_Create_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void guestAgentsCreateMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.guestAgents().create("gtgclehcbsyave", new GuestAgentInner(), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/GuestAgents_Create_MaximumSet_Gen
     * .json
     */
    /**
     * Sample code: GuestAgents_Create_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void guestAgentsCreateMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.guestAgents()
            .create("gtgclehcbsyave",
                new GuestAgentInner().withProperties(new GuestAgentProperties()
                    .withCredentials(
                        new GuestCredential().withUsername("jqxuwirrcpfv").withPassword("fakeTokenPlaceholder"))
                    .withHttpProxyConfig(new HttpProxyConfiguration().withHttpsProxy("uoyzyticmohohomlkwct"))
                    .withProvisioningAction(ProvisioningAction.INSTALL)),
                com.azure.core.util.Context.NONE);
    }
}
```

### GuestAgents_Delete

```java
/**
 * Samples for GuestAgents Delete.
 */
public final class GuestAgentsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/GuestAgents_Delete_MaximumSet_Gen
     * .json
     */
    /**
     * Sample code: GuestAgents_Delete_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void guestAgentsDeleteMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.guestAgents().deleteWithResponse("gtgclehcbsyave", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/GuestAgents_Delete_MinimumSet_Gen
     * .json
     */
    /**
     * Sample code: GuestAgents_Delete_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void guestAgentsDeleteMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.guestAgents().deleteWithResponse("gtgclehcbsyave", com.azure.core.util.Context.NONE);
    }
}
```

### GuestAgents_Get

```java
/**
 * Samples for GuestAgents Get.
 */
public final class GuestAgentsGetSamples {
    /*
     * x-ms-original-file:
     * specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/GuestAgents_Get_MaximumSet_Gen.
     * json
     */
    /**
     * Sample code: GuestAgents_Get_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void guestAgentsGetMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.guestAgents().getWithResponse("gtgclehcbsyave", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/GuestAgents_Get_MinimumSet_Gen.
     * json
     */
    /**
     * Sample code: GuestAgents_Get_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void guestAgentsGetMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.guestAgents().getWithResponse("gtgclehcbsyave", com.azure.core.util.Context.NONE);
    }
}
```

### GuestAgents_ListByVirtualMachineInstance

```java
/**
 * Samples for GuestAgents ListByVirtualMachineInstance.
 */
public final class GuestAgentsListByVirtualMachineInstanceSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * GuestAgents_ListByVirtualMachineInstance_MaximumSet_Gen.json
     */
    /**
     * Sample code: GuestAgents_ListByVirtualMachineInstance_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        guestAgentsListByVirtualMachineInstanceMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.guestAgents().listByVirtualMachineInstance("gtgclehcbsyave", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * GuestAgents_ListByVirtualMachineInstance_MinimumSet_Gen.json
     */
    /**
     * Sample code: GuestAgents_ListByVirtualMachineInstance_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        guestAgentsListByVirtualMachineInstanceMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.guestAgents().listByVirtualMachineInstance("gtgclehcbsyave", com.azure.core.util.Context.NONE);
    }
}
```

### InventoryItems_Create

```java
import com.azure.resourcemanager.scvmm.models.InventoryItemProperties;

/**
 * Samples for InventoryItems Create.
 */
public final class InventoryItemsCreateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * InventoryItems_Create_MinimumSet_Gen.json
     */
    /**
     * Sample code: InventoryItems_Create_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void inventoryItemsCreateMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.inventoryItems()
            .define("bbFb0cBb-50ce-4bfc-3eeD-bC26AbCC257a")
            .withExistingVmmServer("rgscvmm", ".")
            .create();
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * InventoryItems_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: InventoryItems_Create_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void inventoryItemsCreateMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.inventoryItems()
            .define("1BdDc2Ab-bDd9-Ebd6-bfdb-C0dbbdB5DEDf")
            .withExistingVmmServer("rgscvmm", "O")
            .withProperties(new InventoryItemProperties())
            .withKind("M\\d_,V.")
            .create();
    }
}
```

### InventoryItems_Delete

```java
/**
 * Samples for InventoryItems Delete.
 */
public final class InventoryItemsDeleteSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * InventoryItems_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: InventoryItems_Delete_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void inventoryItemsDeleteMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.inventoryItems()
            .deleteWithResponse("rgscvmm", "b", "EcECadfd-Eaaa-e5Ce-ebdA-badeEd3c6af1",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * InventoryItems_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: InventoryItems_Delete_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void inventoryItemsDeleteMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.inventoryItems()
            .deleteWithResponse("rgscvmm", "_", "cDBcbae6-BC3d-52fe-CedC-7eFeaBFabb82",
                com.azure.core.util.Context.NONE);
    }
}
```

### InventoryItems_Get

```java
/**
 * Samples for InventoryItems Get.
 */
public final class InventoryItemsGetSamples {
    /*
     * x-ms-original-file:
     * specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/InventoryItems_Get_MinimumSet_Gen
     * .json
     */
    /**
     * Sample code: InventoryItems_Get_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void inventoryItemsGetMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.inventoryItems()
            .getWithResponse("rgscvmm", "_", "cacb8Ceb-efAC-bebb-ae7C-dec8C5Bb7100", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/InventoryItems_Get_MaximumSet_Gen
     * .json
     */
    /**
     * Sample code: InventoryItems_Get_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void inventoryItemsGetMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.inventoryItems()
            .getWithResponse("rgscvmm", "1", "2bFBede6-EEf8-becB-dBbd-B96DbBFdB3f3", com.azure.core.util.Context.NONE);
    }
}
```

### InventoryItems_ListByVmmServer

```java
/**
 * Samples for InventoryItems ListByVmmServer.
 */
public final class InventoryItemsListByVmmServerSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * InventoryItems_ListByVmmServer_MaximumSet_Gen.json
     */
    /**
     * Sample code: InventoryItems_ListByVmmServer_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void inventoryItemsListByVmmServerMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.inventoryItems().listByVmmServer("rgscvmm", "X", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * InventoryItems_ListByVmmServer_MinimumSet_Gen.json
     */
    /**
     * Sample code: InventoryItems_ListByVmmServer_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void inventoryItemsListByVmmServerMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.inventoryItems().listByVmmServer("rgscvmm", "H", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/Operations_List_MinimumSet_Gen.
     * json
     */
    /**
     * Sample code: Operations_List_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void operationsListMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/Operations_List_MaximumSet_Gen.
     * json
     */
    /**
     * Sample code: Operations_List_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void operationsListMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_CreateCheckpoint

```java
import com.azure.resourcemanager.scvmm.models.VirtualMachineCreateCheckpoint;

/**
 * Samples for VirtualMachineInstances CreateCheckpoint.
 */
public final class VirtualMachineInstancesCreateCheckpointSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineInstances_CreateCheckpoint_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineInstances_CreateCheckpoint_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        virtualMachineInstancesCreateCheckpointMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineInstances()
            .createCheckpoint("gtgclehcbsyave", new VirtualMachineCreateCheckpoint().withName("ilvltf")
                .withDescription("zoozhfbepldrgpjqsbhpqebtodrhvy"), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineInstances_CreateCheckpoint_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineInstances_CreateCheckpoint_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        virtualMachineInstancesCreateCheckpointMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineInstances()
            .createCheckpoint("gtgclehcbsyave", new VirtualMachineCreateCheckpoint(), com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_CreateOrUpdate

```java
import com.azure.resourcemanager.scvmm.fluent.models.VirtualMachineInstanceInner;
import com.azure.resourcemanager.scvmm.models.AllocationMethod;
import com.azure.resourcemanager.scvmm.models.AvailabilitySetListItem;
import com.azure.resourcemanager.scvmm.models.CreateDiffDisk;
import com.azure.resourcemanager.scvmm.models.DynamicMemoryEnabled;
import com.azure.resourcemanager.scvmm.models.ExtendedLocation;
import com.azure.resourcemanager.scvmm.models.HardwareProfile;
import com.azure.resourcemanager.scvmm.models.InfrastructureProfile;
import com.azure.resourcemanager.scvmm.models.LimitCpuForMigration;
import com.azure.resourcemanager.scvmm.models.NetworkInterface;
import com.azure.resourcemanager.scvmm.models.NetworkProfile;
import com.azure.resourcemanager.scvmm.models.OsProfileForVmInstance;
import com.azure.resourcemanager.scvmm.models.StorageProfile;
import com.azure.resourcemanager.scvmm.models.StorageQosPolicyDetails;
import com.azure.resourcemanager.scvmm.models.VirtualDisk;
import com.azure.resourcemanager.scvmm.models.VirtualMachineInstanceProperties;
import java.util.Arrays;

/**
 * Samples for VirtualMachineInstances CreateOrUpdate.
 */
public final class VirtualMachineInstancesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineInstances_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineInstances_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        virtualMachineInstancesCreateOrUpdateMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineInstances()
            .createOrUpdate("gtgclehcbsyave", new VirtualMachineInstanceInner()
                .withProperties(new VirtualMachineInstanceProperties()
                    .withAvailabilitySets(Arrays.asList(new AvailabilitySetListItem().withId(
                        "/subscriptions/12345678-1234-1234-1234-12345678abc/resourceGroups/exampleResourceGroup/providers/Microsoft.ScVmm/availabilitySets/availabilitySetResourceName")
                        .withName("lwbhaseo")))
                    .withOsProfile(new OsProfileForVmInstance().withAdminPassword("fakeTokenPlaceholder")
                        .withComputerName("uuxpcxuxcufllc"))
                    .withHardwareProfile(new HardwareProfile().withMemoryMB(5)
                        .withCpuCount(22)
                        .withLimitCpuForMigration(LimitCpuForMigration.TRUE)
                        .withDynamicMemoryEnabled(DynamicMemoryEnabled.TRUE)
                        .withDynamicMemoryMaxMB(2)
                        .withDynamicMemoryMinMB(30))
                    .withNetworkProfile(new NetworkProfile().withNetworkInterfaces(Arrays.asList(new NetworkInterface()
                        .withName("kvofzqulbjlbtt")
                        .withMacAddress("oaeqqegt")
                        .withVirtualNetworkId(
                            "/subscriptions/12345678-1234-1234-1234-12345678abc/resourceGroups/exampleResourceGroup/providers/Microsoft.ScVmm/virtualNetworks/virtualNetworkName")
                        .withIpv4AddressType(AllocationMethod.DYNAMIC)
                        .withIpv6AddressType(AllocationMethod.DYNAMIC)
                        .withMacAddressType(AllocationMethod.DYNAMIC)
                        .withNicId("roxpsvlo"))))
                    .withStorageProfile(new StorageProfile()
                        .withDisks(Arrays.asList(new VirtualDisk().withName("fgnckfymwdsqnfxkdvexuaobe")
                            .withDiskId("ltdrwcfjklpsimhzqyh")
                            .withDiskSizeGB(30)
                            .withBus(8)
                            .withLun(10)
                            .withBusType("zu")
                            .withVhdType("cnbeeeylrvopigdynvgpkfp")
                            .withTemplateDiskId("lcdwrokpyvekqccclf")
                            .withStorageQosPolicy(new StorageQosPolicyDetails().withName("ceiyfrflu").withId("o"))
                            .withCreateDiffDisk(CreateDiffDisk.TRUE))))
                    .withInfrastructureProfile(new InfrastructureProfile().withInventoryItemId("ihkkqmg")
                        .withVmmServerId(
                            "/subscriptions/12345678-1234-1234-1234-12345678abc/resourceGroups/exampleResourceGroup/providers/Microsoft.ScVmm/vmmServers/vmmServerName")
                        .withCloudId(
                            "/subscriptions/12345678-1234-1234-1234-12345678abc/resourceGroups/exampleResourceGroup/providers/Microsoft.ScVmm/clouds/cloudResourceName")
                        .withTemplateId(
                            "/subscriptions/12345678-1234-1234-1234-12345678abc/resourceGroups/exampleResourceGroup/providers/Microsoft.ScVmm/virtualMachineTemplates/virtualMachineTemplateName")
                        .withVmName("qovpayfydhcvfrhe")
                        .withUuid("hrpw")
                        .withCheckpointType("jkbpzjxpeegackhsvikrnlnwqz")
                        .withGeneration(28)
                        .withBiosGuid("xixivxifyql")))
                .withExtendedLocation(new ExtendedLocation().withType("customLocation")
                    .withName(
                        "/subscriptions/12345678-1234-1234-1234-12345678abc/resourceGroups/exampleResourceGroup/providers/Microsoft.ExtendedLocation/customLocations/customLocationName")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineInstances_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineInstances_CreateOrUpdate_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        virtualMachineInstancesCreateOrUpdateMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineInstances()
            .createOrUpdate("gtgclehcbsyave",
                new VirtualMachineInstanceInner().withExtendedLocation(new ExtendedLocation()),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Delete

```java
import com.azure.resourcemanager.scvmm.models.DeleteFromHost;
import com.azure.resourcemanager.scvmm.models.ForceDelete;

/**
 * Samples for VirtualMachineInstances Delete.
 */
public final class VirtualMachineInstancesDeleteSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineInstances_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineInstances_Delete_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualMachineInstancesDeleteMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineInstances()
            .delete("gtgclehcbsyave", ForceDelete.TRUE, DeleteFromHost.TRUE, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineInstances_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineInstances_Delete_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualMachineInstancesDeleteMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineInstances().delete("gtgclehcbsyave", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_DeleteCheckpoint

```java
import com.azure.resourcemanager.scvmm.models.VirtualMachineDeleteCheckpoint;

/**
 * Samples for VirtualMachineInstances DeleteCheckpoint.
 */
public final class VirtualMachineInstancesDeleteCheckpointSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineInstances_DeleteCheckpoint_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineInstances_DeleteCheckpoint_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        virtualMachineInstancesDeleteCheckpointMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineInstances()
            .deleteCheckpoint("gtgclehcbsyave",
                new VirtualMachineDeleteCheckpoint().withId("eenfflimcbgqfsebdusophahjpk"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineInstances_DeleteCheckpoint_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineInstances_DeleteCheckpoint_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        virtualMachineInstancesDeleteCheckpointMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineInstances()
            .deleteCheckpoint("gtgclehcbsyave", new VirtualMachineDeleteCheckpoint(), com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Get

```java
/**
 * Samples for VirtualMachineInstances Get.
 */
public final class VirtualMachineInstancesGetSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineInstances_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineInstances_Get_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualMachineInstancesGetMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineInstances().getWithResponse("gtgclehcbsyave", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineInstances_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineInstances_Get_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualMachineInstancesGetMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineInstances().getWithResponse("gtgclehcbsyave", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_List

```java
/**
 * Samples for VirtualMachineInstances List.
 */
public final class VirtualMachineInstancesListSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineInstances_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineInstances_List_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualMachineInstancesListMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineInstances().list("gtgclehcbsyave", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineInstances_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineInstances_List_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualMachineInstancesListMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineInstances().list("gtgclehcbsyave", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Restart

```java
/**
 * Samples for VirtualMachineInstances Restart.
 */
public final class VirtualMachineInstancesRestartSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineInstances_Restart_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineInstances_Restart_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualMachineInstancesRestartMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineInstances().restart("gtgclehcbsyave", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineInstances_Restart_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineInstances_Restart_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualMachineInstancesRestartMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineInstances().restart("gtgclehcbsyave", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_RestoreCheckpoint

```java
import com.azure.resourcemanager.scvmm.models.VirtualMachineRestoreCheckpoint;

/**
 * Samples for VirtualMachineInstances RestoreCheckpoint.
 */
public final class VirtualMachineInstancesRestoreCheckpointSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineInstances_RestoreCheckpoint_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineInstances_RestoreCheckpoint_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        virtualMachineInstancesRestoreCheckpointMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineInstances()
            .restoreCheckpoint("gtgclehcbsyave", new VirtualMachineRestoreCheckpoint(),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineInstances_RestoreCheckpoint_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineInstances_RestoreCheckpoint_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        virtualMachineInstancesRestoreCheckpointMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineInstances()
            .restoreCheckpoint("gtgclehcbsyave", new VirtualMachineRestoreCheckpoint().withId("rweqduwzsn"),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Start

```java
/**
 * Samples for VirtualMachineInstances Start.
 */
public final class VirtualMachineInstancesStartSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineInstances_Start_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineInstances_Start_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualMachineInstancesStartMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineInstances().start("gtgclehcbsyave", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineInstances_Start_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineInstances_Start_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualMachineInstancesStartMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineInstances().start("gtgclehcbsyave", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Stop

```java
import com.azure.resourcemanager.scvmm.models.SkipShutdown;
import com.azure.resourcemanager.scvmm.models.StopVirtualMachineOptions;

/**
 * Samples for VirtualMachineInstances Stop.
 */
public final class VirtualMachineInstancesStopSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineInstances_Stop_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineInstances_Stop_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualMachineInstancesStopMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineInstances()
            .stop("gtgclehcbsyave", new StopVirtualMachineOptions().withSkipShutdown(SkipShutdown.TRUE),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineInstances_Stop_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineInstances_Stop_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualMachineInstancesStopMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineInstances()
            .stop("gtgclehcbsyave", new StopVirtualMachineOptions(), com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Update

```java
import com.azure.resourcemanager.scvmm.models.AllocationMethod;
import com.azure.resourcemanager.scvmm.models.AvailabilitySetListItem;
import com.azure.resourcemanager.scvmm.models.DynamicMemoryEnabled;
import com.azure.resourcemanager.scvmm.models.HardwareProfileUpdate;
import com.azure.resourcemanager.scvmm.models.InfrastructureProfileUpdate;
import com.azure.resourcemanager.scvmm.models.LimitCpuForMigration;
import com.azure.resourcemanager.scvmm.models.NetworkInterfaceUpdate;
import com.azure.resourcemanager.scvmm.models.NetworkProfileUpdate;
import com.azure.resourcemanager.scvmm.models.StorageProfileUpdate;
import com.azure.resourcemanager.scvmm.models.StorageQosPolicyDetails;
import com.azure.resourcemanager.scvmm.models.VirtualDiskUpdate;
import com.azure.resourcemanager.scvmm.models.VirtualMachineInstanceUpdate;
import com.azure.resourcemanager.scvmm.models.VirtualMachineInstanceUpdateProperties;
import java.util.Arrays;

/**
 * Samples for VirtualMachineInstances Update.
 */
public final class VirtualMachineInstancesUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineInstances_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineInstances_Update_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualMachineInstancesUpdateMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineInstances()
            .update("gtgclehcbsyave", new VirtualMachineInstanceUpdate(), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineInstances_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineInstances_Update_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualMachineInstancesUpdateMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineInstances()
            .update("gtgclehcbsyave",
                new VirtualMachineInstanceUpdate().withProperties(new VirtualMachineInstanceUpdateProperties()
                    .withAvailabilitySets(Arrays.asList(new AvailabilitySetListItem().withId(
                        "/subscriptions/12345678-1234-1234-1234-12345678abc/resourceGroups/exampleResourceGroup/providers/Microsoft.ScVmm/availabilitySets/availabilitySetResourceName")
                        .withName("lwbhaseo")))
                    .withHardwareProfile(new HardwareProfileUpdate().withMemoryMB(5)
                        .withCpuCount(22)
                        .withLimitCpuForMigration(LimitCpuForMigration.TRUE)
                        .withDynamicMemoryEnabled(DynamicMemoryEnabled.TRUE)
                        .withDynamicMemoryMaxMB(2)
                        .withDynamicMemoryMinMB(30))
                    .withNetworkProfile(new NetworkProfileUpdate()
                        .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceUpdate().withName("kvofzqulbjlbtt")
                            .withMacAddress("oaeqqegt")
                            .withVirtualNetworkId(
                                "/subscriptions/12345678-1234-1234-1234-12345678abc/resourceGroups/exampleResourceGroup/providers/Microsoft.ScVmm/virtualNetworks/virtualNetworkName")
                            .withIpv4AddressType(AllocationMethod.DYNAMIC)
                            .withIpv6AddressType(AllocationMethod.DYNAMIC)
                            .withMacAddressType(AllocationMethod.DYNAMIC)
                            .withNicId("roxpsvlo"))))
                    .withStorageProfile(new StorageProfileUpdate()
                        .withDisks(Arrays.asList(new VirtualDiskUpdate().withName("fgnckfymwdsqnfxkdvexuaobe")
                            .withDiskId("ltdrwcfjklpsimhzqyh")
                            .withDiskSizeGB(30)
                            .withBus(8)
                            .withLun(10)
                            .withBusType("zu")
                            .withVhdType("cnbeeeylrvopigdynvgpkfp")
                            .withStorageQosPolicy(new StorageQosPolicyDetails().withName("ceiyfrflu").withId("o")))))
                    .withInfrastructureProfile(
                        new InfrastructureProfileUpdate().withCheckpointType("jkbpzjxpeegackhsvikrnlnwqz"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineTemplates_CreateOrUpdate

```java
import com.azure.resourcemanager.scvmm.models.ExtendedLocation;
import com.azure.resourcemanager.scvmm.models.VirtualMachineTemplateProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for VirtualMachineTemplates CreateOrUpdate.
 */
public final class VirtualMachineTemplatesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineTemplates_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineTemplates_CreateOrUpdate_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        virtualMachineTemplatesCreateOrUpdateMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineTemplates()
            .define("P")
            .withRegion("ayxsyduviotylbojh")
            .withExistingResourceGroup("rgscvmm")
            .withExtendedLocation(new ExtendedLocation())
            .create();
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineTemplates_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineTemplates_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        virtualMachineTemplatesCreateOrUpdateMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineTemplates()
            .define("6")
            .withRegion("ayxsyduviotylbojh")
            .withExistingResourceGroup("rgscvmm")
            .withExtendedLocation(new ExtendedLocation().withType("customLocation")
                .withName(
                    "/subscriptions/12345678-1234-1234-1234-12345678abc/resourceGroups/exampleResourceGroup/providers/Microsoft.ExtendedLocation/customLocations/customLocationName"))
            .withTags(mapOf("key9494", "fakeTokenPlaceholder"))
            .withProperties(new VirtualMachineTemplateProperties().withInventoryItemId("qjrykoogccwlgkd")
                .withUuid("12345678-1234-1234-1234-12345678abcd")
                .withVmmServerId(
                    "/subscriptions/12345678-1234-1234-1234-12345678abc/resourceGroups/exampleResourceGroup/providers/Microsoft.ScVmm/vmmServers/vmmServerName"))
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

### VirtualMachineTemplates_Delete

```java
import com.azure.resourcemanager.scvmm.models.ForceDelete;

/**
 * Samples for VirtualMachineTemplates Delete.
 */
public final class VirtualMachineTemplatesDeleteSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineTemplates_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineTemplates_Delete_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualMachineTemplatesDeleteMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineTemplates().delete("rgscvmm", "5", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineTemplates_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineTemplates_Delete_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualMachineTemplatesDeleteMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineTemplates().delete("rgscvmm", "6", ForceDelete.TRUE, com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineTemplates_GetByResourceGroup

```java
/**
 * Samples for VirtualMachineTemplates GetByResourceGroup.
 */
public final class VirtualMachineTemplatesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineTemplates_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineTemplates_Get_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualMachineTemplatesGetMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineTemplates()
            .getByResourceGroupWithResponse("rgscvmm", "m", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineTemplates_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineTemplates_Get_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualMachineTemplatesGetMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineTemplates()
            .getByResourceGroupWithResponse("rgscvmm", "4", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineTemplates_List

```java
/**
 * Samples for VirtualMachineTemplates List.
 */
public final class VirtualMachineTemplatesListSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineTemplates_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineTemplates_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        virtualMachineTemplatesListBySubscriptionMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineTemplates().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineTemplates_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineTemplates_ListBySubscription_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        virtualMachineTemplatesListBySubscriptionMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineTemplates().list(com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineTemplates_ListByResourceGroup

```java
/**
 * Samples for VirtualMachineTemplates ListByResourceGroup.
 */
public final class VirtualMachineTemplatesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineTemplates_ListByResourceGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineTemplates_ListByResourceGroup_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        virtualMachineTemplatesListByResourceGroupMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineTemplates().listByResourceGroup("rgscvmm", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineTemplates_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineTemplates_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        virtualMachineTemplatesListByResourceGroupMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualMachineTemplates().listByResourceGroup("rgscvmm", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineTemplates_Update

```java
import com.azure.resourcemanager.scvmm.models.VirtualMachineTemplate;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for VirtualMachineTemplates Update.
 */
public final class VirtualMachineTemplatesUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineTemplates_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineTemplates_Update_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualMachineTemplatesUpdateMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        VirtualMachineTemplate resource = manager.virtualMachineTemplates()
            .getByResourceGroupWithResponse("rgscvmm", "g", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key6634", "fakeTokenPlaceholder")).apply();
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualMachineTemplates_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualMachineTemplates_Update_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualMachineTemplatesUpdateMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        VirtualMachineTemplate resource = manager.virtualMachineTemplates()
            .getByResourceGroupWithResponse("rgscvmm", "-", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
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
import com.azure.resourcemanager.scvmm.models.VirtualNetworkProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for VirtualNetworks CreateOrUpdate.
 */
public final class VirtualNetworksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualNetworks_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualNetworks_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualNetworksCreateOrUpdateMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualNetworks()
            .define("_")
            .withRegion("fky")
            .withExistingResourceGroup("rgscvmm")
            .withExtendedLocation(new ExtendedLocation().withType("customLocation")
                .withName(
                    "/subscriptions/12345678-1234-1234-1234-12345678abc/resourceGroups/exampleResourceGroup/providers/Microsoft.ExtendedLocation/customLocations/customLocationName"))
            .withTags(mapOf("key705", "fakeTokenPlaceholder"))
            .withProperties(new VirtualNetworkProperties().withInventoryItemId("bxn")
                .withUuid("12345678-1234-1234-1234-12345678abcd")
                .withVmmServerId(
                    "/subscriptions/12345678-1234-1234-1234-12345678abc/resourceGroups/exampleResourceGroup/providers/Microsoft.ScVmm/vmmServers/vmmServerName"))
            .create();
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualNetworks_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualNetworks_CreateOrUpdate_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualNetworksCreateOrUpdateMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualNetworks()
            .define("-")
            .withRegion("fky")
            .withExistingResourceGroup("rgscvmm")
            .withExtendedLocation(new ExtendedLocation())
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

### VirtualNetworks_Delete

```java
import com.azure.resourcemanager.scvmm.models.ForceDelete;

/**
 * Samples for VirtualNetworks Delete.
 */
public final class VirtualNetworksDeleteSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualNetworks_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualNetworks_Delete_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualNetworksDeleteMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualNetworks().delete("rgscvmm", ".", ForceDelete.TRUE, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualNetworks_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualNetworks_Delete_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualNetworksDeleteMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualNetworks().delete("rgscvmm", "1", null, com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworks_GetByResourceGroup

```java
/**
 * Samples for VirtualNetworks GetByResourceGroup.
 */
public final class VirtualNetworksGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualNetworks_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualNetworks_Get_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualNetworksGetMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualNetworks().getByResourceGroupWithResponse("rgscvmm", "2", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualNetworks_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualNetworks_Get_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualNetworksGetMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualNetworks().getByResourceGroupWithResponse("rgscvmm", "-", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworks_List

```java
/**
 * Samples for VirtualNetworks List.
 */
public final class VirtualNetworksListSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualNetworks_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualNetworks_ListBySubscription_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        virtualNetworksListBySubscriptionMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualNetworks().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualNetworks_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualNetworks_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        virtualNetworksListBySubscriptionMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualNetworks().list(com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworks_ListByResourceGroup

```java
/**
 * Samples for VirtualNetworks ListByResourceGroup.
 */
public final class VirtualNetworksListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualNetworks_ListByResourceGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualNetworks_ListByResourceGroup_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        virtualNetworksListByResourceGroupMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualNetworks().listByResourceGroup("rgscvmm", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualNetworks_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualNetworks_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        virtualNetworksListByResourceGroupMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.virtualNetworks().listByResourceGroup("rgscvmm", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworks_Update

```java
import com.azure.resourcemanager.scvmm.models.VirtualNetwork;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for VirtualNetworks Update.
 */
public final class VirtualNetworksUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualNetworks_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: VirtualNetworks_Update_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualNetworksUpdateMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        VirtualNetwork resource = manager.virtualNetworks()
            .getByResourceGroupWithResponse("rgscvmm", "-", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VirtualNetworks_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: VirtualNetworks_Update_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void virtualNetworksUpdateMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        VirtualNetwork resource = manager.virtualNetworks()
            .getByResourceGroupWithResponse("rgscvmm", "S", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key9516", "fakeTokenPlaceholder")).apply();
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

### VmInstanceHybridIdentityMetadatas_Get

```java
/**
 * Samples for VmInstanceHybridIdentityMetadatas Get.
 */
public final class VmInstanceHybridIdentityMetadatasGetSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VmInstanceHybridIdentityMetadatas_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: VmInstanceHybridIdentityMetadatas_Get_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        vmInstanceHybridIdentityMetadatasGetMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.vmInstanceHybridIdentityMetadatas().getWithResponse("gtgclehcbsyave", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VmInstanceHybridIdentityMetadatas_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: VmInstanceHybridIdentityMetadatas_Get_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void
        vmInstanceHybridIdentityMetadatasGetMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.vmInstanceHybridIdentityMetadatas().getWithResponse("gtgclehcbsyave", com.azure.core.util.Context.NONE);
    }
}
```

### VmInstanceHybridIdentityMetadatas_ListByVirtualMachineInstance

```java
/**
 * Samples for VmInstanceHybridIdentityMetadatas ListByVirtualMachineInstance.
 */
public final class VmInstanceHybridIdentityMetadatasListByVirtualMachineInstanceSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VmInstanceHybridIdentityMetadatas_ListByVirtualMachineInstance_MaximumSet_Gen.json
     */
    /**
     * Sample code: VmInstanceHybridIdentityMetadatas_ListByVirtualMachineInstance_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void vmInstanceHybridIdentityMetadatasListByVirtualMachineInstanceMaximumSet(
        com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.vmInstanceHybridIdentityMetadatas()
            .listByVirtualMachineInstance("gtgclehcbsyave", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VmInstanceHybridIdentityMetadatas_ListByVirtualMachineInstance_MinimumSet_Gen.json
     */
    /**
     * Sample code: VmInstanceHybridIdentityMetadatas_ListByVirtualMachineInstance_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void vmInstanceHybridIdentityMetadatasListByVirtualMachineInstanceMinimumSet(
        com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.vmInstanceHybridIdentityMetadatas()
            .listByVirtualMachineInstance("gtgclehcbsyave", com.azure.core.util.Context.NONE);
    }
}
```

### VmmServers_CreateOrUpdate

```java
import com.azure.resourcemanager.scvmm.models.ExtendedLocation;
import com.azure.resourcemanager.scvmm.models.VmmCredential;
import com.azure.resourcemanager.scvmm.models.VmmServerProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for VmmServers CreateOrUpdate.
 */
public final class VmmServersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VmmServers_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: VmmServers_CreateOrUpdate_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void vmmServersCreateOrUpdateMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.vmmServers()
            .define("w")
            .withRegion("hslxkyzktvwpqbypvs")
            .withExistingResourceGroup("rgscvmm")
            .withExtendedLocation(new ExtendedLocation())
            .create();
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VmmServers_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: VmmServers_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void vmmServersCreateOrUpdateMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.vmmServers()
            .define("-")
            .withRegion("hslxkyzktvwpqbypvs")
            .withExistingResourceGroup("rgscvmm")
            .withExtendedLocation(new ExtendedLocation().withType("customLocation")
                .withName(
                    "/subscriptions/12345678-1234-1234-1234-12345678abc/resourceGroups/exampleResourceGroup/providers/Microsoft.ExtendedLocation/customLocations/customLocationName"))
            .withTags(mapOf("key4834", "fakeTokenPlaceholder"))
            .withProperties(new VmmServerProperties()
                .withCredentials(
                    new VmmCredential().withUsername("jbuoltypmrgqfi").withPassword("fakeTokenPlaceholder"))
                .withFqdn("pvzcjaqrswbvptgx")
                .withPort(4))
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

### VmmServers_Delete

```java
import com.azure.resourcemanager.scvmm.models.ForceDelete;

/**
 * Samples for VmmServers Delete.
 */
public final class VmmServersDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/VmmServers_Delete_MaximumSet_Gen.
     * json
     */
    /**
     * Sample code: VmmServers_Delete_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void vmmServersDeleteMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.vmmServers().delete("rgscvmm", ".", ForceDelete.TRUE, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/VmmServers_Delete_MinimumSet_Gen.
     * json
     */
    /**
     * Sample code: VmmServers_Delete_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void vmmServersDeleteMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.vmmServers().delete("rgscvmm", "8", null, com.azure.core.util.Context.NONE);
    }
}
```

### VmmServers_GetByResourceGroup

```java
/**
 * Samples for VmmServers GetByResourceGroup.
 */
public final class VmmServersGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/VmmServers_Get_MinimumSet_Gen.
     * json
     */
    /**
     * Sample code: VmmServers_Get_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void vmmServersGetMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.vmmServers().getByResourceGroupWithResponse("rgscvmm", "D", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/VmmServers_Get_MaximumSet_Gen.
     * json
     */
    /**
     * Sample code: VmmServers_Get_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void vmmServersGetMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.vmmServers().getByResourceGroupWithResponse("rgscvmm", ".", com.azure.core.util.Context.NONE);
    }
}
```

### VmmServers_List

```java
/**
 * Samples for VmmServers List.
 */
public final class VmmServersListSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VmmServers_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: VmmServers_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void vmmServersListBySubscriptionMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.vmmServers().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VmmServers_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: VmmServers_ListBySubscription_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void vmmServersListBySubscriptionMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.vmmServers().list(com.azure.core.util.Context.NONE);
    }
}
```

### VmmServers_ListByResourceGroup

```java
/**
 * Samples for VmmServers ListByResourceGroup.
 */
public final class VmmServersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VmmServers_ListByResourceGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: VmmServers_ListByResourceGroup_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void vmmServersListByResourceGroupMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.vmmServers().listByResourceGroup("rgscvmm", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/
     * VmmServers_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: VmmServers_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void vmmServersListByResourceGroupMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        manager.vmmServers().listByResourceGroup("rgscvmm", com.azure.core.util.Context.NONE);
    }
}
```

### VmmServers_Update

```java
import com.azure.resourcemanager.scvmm.models.VmmServer;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for VmmServers Update.
 */
public final class VmmServersUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/VmmServers_Update_MinimumSet_Gen.
     * json
     */
    /**
     * Sample code: VmmServers_Update_MinimumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void vmmServersUpdateMinimumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        VmmServer resource = manager.vmmServers()
            .getByResourceGroupWithResponse("rgscvmm", "_", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }

    /*
     * x-ms-original-file:
     * specification/scvmm/resource-manager/Microsoft.ScVmm/stable/2023-10-07/examples/VmmServers_Update_MaximumSet_Gen.
     * json
     */
    /**
     * Sample code: VmmServers_Update_MaximumSet.
     * 
     * @param manager Entry point to ScvmmManager.
     */
    public static void vmmServersUpdateMaximumSet(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        VmmServer resource = manager.vmmServers()
            .getByResourceGroupWithResponse("rgscvmm", "Y", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key7187", "fakeTokenPlaceholder")).apply();
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

