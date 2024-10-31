# Code snippets and samples


## AzureBareMetalInstances

- [GetByResourceGroup](#azurebaremetalinstances_getbyresourcegroup)
- [List](#azurebaremetalinstances_list)
- [ListByResourceGroup](#azurebaremetalinstances_listbyresourcegroup)
- [Restart](#azurebaremetalinstances_restart)
- [Shutdown](#azurebaremetalinstances_shutdown)
- [Start](#azurebaremetalinstances_start)
- [Update](#azurebaremetalinstances_update)

## AzureBareMetalStorageInstances

- [Create](#azurebaremetalstorageinstances_create)
- [Delete](#azurebaremetalstorageinstances_delete)
- [GetByResourceGroup](#azurebaremetalstorageinstances_getbyresourcegroup)
- [List](#azurebaremetalstorageinstances_list)
- [ListByResourceGroup](#azurebaremetalstorageinstances_listbyresourcegroup)
- [Update](#azurebaremetalstorageinstances_update)

## Operations

- [List](#operations_list)
### AzureBareMetalInstances_GetByResourceGroup

```java
/**
 * Samples for AzureBareMetalInstances GetByResourceGroup.
 */
public final class AzureBareMetalInstancesGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/baremetalinfrastructure/resource-manager/Microsoft.BareMetalInfrastructure/preview/2023-08-04-
     * preview/examples/AzureBareMetalInstances_Get.json
     */
    /**
     * Sample code: Get an Azure Bare Metal Instance.
     * 
     * @param manager Entry point to BareMetalInfrastructureManager.
     */
    public static void getAnAzureBareMetalInstance(
        com.azure.resourcemanager.baremetalinfrastructure.BareMetalInfrastructureManager manager) {
        manager.azureBareMetalInstances()
            .getByResourceGroupWithResponse("myResourceGroup", "myAzureBareMetalInstance",
                com.azure.core.util.Context.NONE);
    }
}
```

### AzureBareMetalInstances_List

```java
/**
 * Samples for AzureBareMetalInstances List.
 */
public final class AzureBareMetalInstancesListSamples {
    /*
     * x-ms-original-file:
     * specification/baremetalinfrastructure/resource-manager/Microsoft.BareMetalInfrastructure/preview/2023-08-04-
     * preview/examples/AzureBareMetalInstances_ListBySubscription.json
     */
    /**
     * Sample code: List all Azure Bare Metal Instances in a subscription.
     * 
     * @param manager Entry point to BareMetalInfrastructureManager.
     */
    public static void listAllAzureBareMetalInstancesInASubscription(
        com.azure.resourcemanager.baremetalinfrastructure.BareMetalInfrastructureManager manager) {
        manager.azureBareMetalInstances().list(com.azure.core.util.Context.NONE);
    }
}
```

### AzureBareMetalInstances_ListByResourceGroup

```java
/**
 * Samples for AzureBareMetalInstances ListByResourceGroup.
 */
public final class AzureBareMetalInstancesListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/baremetalinfrastructure/resource-manager/Microsoft.BareMetalInfrastructure/preview/2023-08-04-
     * preview/examples/AzureBareMetalInstances_ListByResourceGroup.json
     */
    /**
     * Sample code: List all Azure Bare Metal Instances in a resource group.
     * 
     * @param manager Entry point to BareMetalInfrastructureManager.
     */
    public static void listAllAzureBareMetalInstancesInAResourceGroup(
        com.azure.resourcemanager.baremetalinfrastructure.BareMetalInfrastructureManager manager) {
        manager.azureBareMetalInstances().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### AzureBareMetalInstances_Restart

```java

/**
 * Samples for AzureBareMetalInstances Restart.
 */
public final class AzureBareMetalInstancesRestartSamples {
    /*
     * x-ms-original-file:
     * specification/baremetalinfrastructure/resource-manager/Microsoft.BareMetalInfrastructure/preview/2023-08-04-
     * preview/examples/AzureBareMetalInstances_Restart.json
     */
    /**
     * Sample code: Restart an Azure Bare Metal Instance.
     * 
     * @param manager Entry point to BareMetalInfrastructureManager.
     */
    public static void restartAnAzureBareMetalInstance(
        com.azure.resourcemanager.baremetalinfrastructure.BareMetalInfrastructureManager manager) {
        manager.azureBareMetalInstances()
            .restart("myResourceGroup", "myABMInstance", null, com.azure.core.util.Context.NONE);
    }
}
```

### AzureBareMetalInstances_Shutdown

```java
/**
 * Samples for AzureBareMetalInstances Shutdown.
 */
public final class AzureBareMetalInstancesShutdownSamples {
    /*
     * x-ms-original-file:
     * specification/baremetalinfrastructure/resource-manager/Microsoft.BareMetalInfrastructure/preview/2023-08-04-
     * preview/examples/AzureBareMetalInstances_Shutdown.json
     */
    /**
     * Sample code: Shutdown an Azure Bare Metal Instance.
     * 
     * @param manager Entry point to BareMetalInfrastructureManager.
     */
    public static void shutdownAnAzureBareMetalInstance(
        com.azure.resourcemanager.baremetalinfrastructure.BareMetalInfrastructureManager manager) {
        manager.azureBareMetalInstances()
            .shutdown("myResourceGroup", "myABMInstance", com.azure.core.util.Context.NONE);
    }
}
```

### AzureBareMetalInstances_Start

```java
/**
 * Samples for AzureBareMetalInstances Start.
 */
public final class AzureBareMetalInstancesStartSamples {
    /*
     * x-ms-original-file:
     * specification/baremetalinfrastructure/resource-manager/Microsoft.BareMetalInfrastructure/preview/2023-08-04-
     * preview/examples/AzureBareMetalInstances_Start.json
     */
    /**
     * Sample code: Start an Azure Bare Metal instance.
     * 
     * @param manager Entry point to BareMetalInfrastructureManager.
     */
    public static void startAnAzureBareMetalInstance(
        com.azure.resourcemanager.baremetalinfrastructure.BareMetalInfrastructureManager manager) {
        manager.azureBareMetalInstances().start("myResourceGroup", "myABMInstance", com.azure.core.util.Context.NONE);
    }
}
```

### AzureBareMetalInstances_Update

```java
import com.azure.resourcemanager.baremetalinfrastructure.models.Tags;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AzureBareMetalInstances Update.
 */
public final class AzureBareMetalInstancesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/baremetalinfrastructure/resource-manager/Microsoft.BareMetalInfrastructure/preview/2023-08-04-
     * preview/examples/AzureBareMetalInstances_PatchTags_Delete.json
     */
    /**
     * Sample code: Delete Tags field of an Azure Bare Metal Instance.
     * 
     * @param manager Entry point to BareMetalInfrastructureManager.
     */
    public static void deleteTagsFieldOfAnAzureBareMetalInstance(
        com.azure.resourcemanager.baremetalinfrastructure.BareMetalInfrastructureManager manager) {
        manager.azureBareMetalInstances()
            .updateWithResponse("myResourceGroup", "myABMInstance", new Tags().withTags(mapOf()),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/baremetalinfrastructure/resource-manager/Microsoft.BareMetalInfrastructure/preview/2023-08-04-
     * preview/examples/AzureBareMetalInstances_PatchTags.json
     */
    /**
     * Sample code: Update Tags field of an Azure Bare Metal Instance.
     * 
     * @param manager Entry point to BareMetalInfrastructureManager.
     */
    public static void updateTagsFieldOfAnAzureBareMetalInstance(
        com.azure.resourcemanager.baremetalinfrastructure.BareMetalInfrastructureManager manager) {
        manager.azureBareMetalInstances()
            .updateWithResponse("myResourceGroup", "myABMInstance",
                new Tags().withTags(mapOf("testkey", "fakeTokenPlaceholder")), com.azure.core.util.Context.NONE);
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

### AzureBareMetalStorageInstances_Create

```java
import com.azure.resourcemanager.baremetalinfrastructure.models.ProvisioningState;
import com.azure.resourcemanager.baremetalinfrastructure.models.StorageBillingProperties;
import com.azure.resourcemanager.baremetalinfrastructure.models.StorageProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AzureBareMetalStorageInstances Create.
 */
public final class AzureBareMetalStorageInstancesCreateSamples {
    /*
     * x-ms-original-file:
     * specification/baremetalinfrastructure/resource-manager/Microsoft.BareMetalInfrastructure/preview/2023-08-04-
     * preview/examples/AzureBareMetalStorageInstances_Create.json
     */
    /**
     * Sample code: Put a new AzureBareMetalStorageInstance.
     * 
     * @param manager Entry point to BareMetalInfrastructureManager.
     */
    public static void putANewAzureBareMetalStorageInstance(
        com.azure.resourcemanager.baremetalinfrastructure.BareMetalInfrastructureManager manager) {
        manager.azureBareMetalStorageInstances()
            .define("myAzureBareMetalStorageInstance")
            .withRegion("westus2")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("key", "fakeTokenPlaceholder"))
            .withAzureBareMetalStorageInstanceUniqueIdentifier("23415635-4d7e-41dc-9598-8194f22c24e9")
            .withStorageProperties(
                new StorageProperties().withProvisioningState(ProvisioningState.SUCCEEDED)
                    .withOfferingType("EPIC")
                    .withStorageType("FC")
                    .withGeneration("Gen4")
                    .withHardwareType("NetApp")
                    .withWorkloadType("ODB")
                    .withStorageBillingProperties(new StorageBillingProperties().withBillingMode("PAYG")
                        .withAzureBareMetalStorageInstanceSize("")))
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

### AzureBareMetalStorageInstances_Delete

```java
/**
 * Samples for AzureBareMetalStorageInstances Delete.
 */
public final class AzureBareMetalStorageInstancesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/baremetalinfrastructure/resource-manager/Microsoft.BareMetalInfrastructure/preview/2023-08-04-
     * preview/examples/AzureBareMetalStorageInstances_Delete.json
     */
    /**
     * Sample code: Delete an AzureBareMetalStorageInstance.
     * 
     * @param manager Entry point to BareMetalInfrastructureManager.
     */
    public static void deleteAnAzureBareMetalStorageInstance(
        com.azure.resourcemanager.baremetalinfrastructure.BareMetalInfrastructureManager manager) {
        manager.azureBareMetalStorageInstances()
            .deleteByResourceGroupWithResponse("myResourceGroup", "myAzureBareMetalStorageInstance",
                com.azure.core.util.Context.NONE);
    }
}
```

### AzureBareMetalStorageInstances_GetByResourceGroup

```java
/**
 * Samples for AzureBareMetalStorageInstances GetByResourceGroup.
 */
public final class AzureBareMetalStorageInstancesGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/baremetalinfrastructure/resource-manager/Microsoft.BareMetalInfrastructure/preview/2023-08-04-
     * preview/examples/AzureBareMetalStorageInstances_Get.json
     */
    /**
     * Sample code: Get an AzureBareMetalStorage instance.
     * 
     * @param manager Entry point to BareMetalInfrastructureManager.
     */
    public static void getAnAzureBareMetalStorageInstance(
        com.azure.resourcemanager.baremetalinfrastructure.BareMetalInfrastructureManager manager) {
        manager.azureBareMetalStorageInstances()
            .getByResourceGroupWithResponse("myResourceGroup", "myAzureBareMetalStorageInstance",
                com.azure.core.util.Context.NONE);
    }
}
```

### AzureBareMetalStorageInstances_List

```java
/**
 * Samples for AzureBareMetalStorageInstances List.
 */
public final class AzureBareMetalStorageInstancesListSamples {
    /*
     * x-ms-original-file:
     * specification/baremetalinfrastructure/resource-manager/Microsoft.BareMetalInfrastructure/preview/2023-08-04-
     * preview/examples/AzureBareMetalStorageInstances_ListBySubscription.json
     */
    /**
     * Sample code: List all AzureBareMetalStorage instances in a subscription.
     * 
     * @param manager Entry point to BareMetalInfrastructureManager.
     */
    public static void listAllAzureBareMetalStorageInstancesInASubscription(
        com.azure.resourcemanager.baremetalinfrastructure.BareMetalInfrastructureManager manager) {
        manager.azureBareMetalStorageInstances().list(com.azure.core.util.Context.NONE);
    }
}
```

### AzureBareMetalStorageInstances_ListByResourceGroup

```java
/**
 * Samples for AzureBareMetalStorageInstances ListByResourceGroup.
 */
public final class AzureBareMetalStorageInstancesListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/baremetalinfrastructure/resource-manager/Microsoft.BareMetalInfrastructure/preview/2023-08-04-
     * preview/examples/AzureBareMetalStorageInstances_ListByResourceGroup.json
     */
    /**
     * Sample code: List all AzureBareMetalStorage instances in a resource group.
     * 
     * @param manager Entry point to BareMetalInfrastructureManager.
     */
    public static void listAllAzureBareMetalStorageInstancesInAResourceGroup(
        com.azure.resourcemanager.baremetalinfrastructure.BareMetalInfrastructureManager manager) {
        manager.azureBareMetalStorageInstances()
            .listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### AzureBareMetalStorageInstances_Update

```java
import com.azure.resourcemanager.baremetalinfrastructure.models.AzureBareMetalStorageInstance;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AzureBareMetalStorageInstances Update.
 */
public final class AzureBareMetalStorageInstancesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/baremetalinfrastructure/resource-manager/Microsoft.BareMetalInfrastructure/preview/2023-08-04-
     * preview/examples/AzureBareMetalStorageInstances_PatchTags.json
     */
    /**
     * Sample code: Update Tags field of an AzureBareMetalStorage instance.
     * 
     * @param manager Entry point to BareMetalInfrastructureManager.
     */
    public static void updateTagsFieldOfAnAzureBareMetalStorageInstance(
        com.azure.resourcemanager.baremetalinfrastructure.BareMetalInfrastructureManager manager) {
        AzureBareMetalStorageInstance resource = manager.azureBareMetalStorageInstances()
            .getByResourceGroupWithResponse("myResourceGroup", "myABMSInstance", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("testkey", "fakeTokenPlaceholder")).apply();
    }

    /*
     * x-ms-original-file:
     * specification/baremetalinfrastructure/resource-manager/Microsoft.BareMetalInfrastructure/preview/2023-08-04-
     * preview/examples/AzureBareMetalStorageInstances_PatchTags_Delete.json
     */
    /**
     * Sample code: Delete Tags field of an AzureBareMetalStorage instance.
     * 
     * @param manager Entry point to BareMetalInfrastructureManager.
     */
    public static void deleteTagsFieldOfAnAzureBareMetalStorageInstance(
        com.azure.resourcemanager.baremetalinfrastructure.BareMetalInfrastructureManager manager) {
        AzureBareMetalStorageInstance resource = manager.azureBareMetalStorageInstances()
            .getByResourceGroupWithResponse("myResourceGroup", "myABMSInstance", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf()).apply();
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
     * x-ms-original-file:
     * specification/baremetalinfrastructure/resource-manager/Microsoft.BareMetalInfrastructure/preview/2023-08-04-
     * preview/examples/AzureBareMetalOperations_List.json
     */
    /**
     * Sample code: List all management operations supported by the AzureBareMetal RP.
     * 
     * @param manager Entry point to BareMetalInfrastructureManager.
     */
    public static void listAllManagementOperationsSupportedByTheAzureBareMetalRP(
        com.azure.resourcemanager.baremetalinfrastructure.BareMetalInfrastructureManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

