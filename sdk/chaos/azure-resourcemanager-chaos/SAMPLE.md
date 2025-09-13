# Code snippets and samples


## Capabilities

- [CreateOrUpdate](#capabilities_createorupdate)
- [Delete](#capabilities_delete)
- [Get](#capabilities_get)
- [List](#capabilities_list)

## CapabilityTypes

- [Get](#capabilitytypes_get)
- [List](#capabilitytypes_list)

## Experiments

- [Cancel](#experiments_cancel)
- [CreateOrUpdate](#experiments_createorupdate)
- [Delete](#experiments_delete)
- [ExecutionDetails](#experiments_executiondetails)
- [GetByResourceGroup](#experiments_getbyresourcegroup)
- [GetExecution](#experiments_getexecution)
- [List](#experiments_list)
- [ListAllExecutions](#experiments_listallexecutions)
- [ListByResourceGroup](#experiments_listbyresourcegroup)
- [Start](#experiments_start)
- [Update](#experiments_update)

## OperationStatuses

- [Get](#operationstatuses_get)

## TargetTypes

- [Get](#targettypes_get)
- [List](#targettypes_list)

## Targets

- [CreateOrUpdate](#targets_createorupdate)
- [Delete](#targets_delete)
- [Get](#targets_get)
- [List](#targets_list)
### Capabilities_CreateOrUpdate

```java
import com.azure.resourcemanager.chaos.fluent.models.CapabilityInner;

/**
 * Samples for Capabilities CreateOrUpdate.
 */
public final class CapabilitiesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-01-01/Capabilities_CreateOrUpdate.json
     */
    /**
     * Sample code: Create/update a Capability that extends a virtual machine Target resource.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void createUpdateACapabilityThatExtendsAVirtualMachineTargetResource(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.capabilities()
            .createOrUpdateWithResponse("exampleRG", "Microsoft.Compute", "virtualMachines", "exampleVM",
                "Microsoft-VirtualMachine", "Shutdown-1.0", new CapabilityInner(), com.azure.core.util.Context.NONE);
    }
}
```

### Capabilities_Delete

```java
/**
 * Samples for Capabilities Delete.
 */
public final class CapabilitiesDeleteSamples {
    /*
     * x-ms-original-file: 2025-01-01/Capabilities_Delete.json
     */
    /**
     * Sample code: Delete a Capability that extends a virtual machine Target resource.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void deleteACapabilityThatExtendsAVirtualMachineTargetResource(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.capabilities()
            .deleteWithResponse("exampleRG", "Microsoft.Compute", "virtualMachines", "exampleVM",
                "Microsoft-VirtualMachine", "Shutdown-1.0", com.azure.core.util.Context.NONE);
    }
}
```

### Capabilities_Get

```java
/**
 * Samples for Capabilities Get.
 */
public final class CapabilitiesGetSamples {
    /*
     * x-ms-original-file: 2025-01-01/Capabilities_Get.json
     */
    /**
     * Sample code: Get a Capability that extends a virtual machine Target resource.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void
        getACapabilityThatExtendsAVirtualMachineTargetResource(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.capabilities()
            .getWithResponse("exampleRG", "Microsoft.Compute", "virtualMachines", "exampleVM",
                "Microsoft-VirtualMachine", "Shutdown-1.0", com.azure.core.util.Context.NONE);
    }
}
```

### Capabilities_List

```java
/**
 * Samples for Capabilities List.
 */
public final class CapabilitiesListSamples {
    /*
     * x-ms-original-file: 2025-01-01/Capabilities_List.json
     */
    /**
     * Sample code: List all Capabilities that extend a virtual machine Target resource.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void listAllCapabilitiesThatExtendAVirtualMachineTargetResource(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.capabilities()
            .list("exampleRG", "Microsoft.Compute", "virtualMachines", "exampleVM", "Microsoft-VirtualMachine", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### CapabilityTypes_Get

```java
/**
 * Samples for CapabilityTypes Get.
 */
public final class CapabilityTypesGetSamples {
    /*
     * x-ms-original-file: 2025-01-01/CapabilityTypes_Get.json
     */
    /**
     * Sample code: Get a Capability Type for a virtual machine Target resource on westus2 location.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getACapabilityTypeForAVirtualMachineTargetResourceOnWestus2Location(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.capabilityTypes()
            .getWithResponse("westus2", "Microsoft-VirtualMachine", "Shutdown-1.0", com.azure.core.util.Context.NONE);
    }
}
```

### CapabilityTypes_List

```java
/**
 * Samples for CapabilityTypes List.
 */
public final class CapabilityTypesListSamples {
    /*
     * x-ms-original-file: 2025-01-01/CapabilityTypes_List.json
     */
    /**
     * Sample code: List all Capability Types for a virtual machine Target resource on westus2 location.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void listAllCapabilityTypesForAVirtualMachineTargetResourceOnWestus2Location(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.capabilityTypes().list("westus2", "Microsoft-VirtualMachine", null, com.azure.core.util.Context.NONE);
    }
}
```

### Experiments_Cancel

```java
/**
 * Samples for Experiments Cancel.
 */
public final class ExperimentsCancelSamples {
    /*
     * x-ms-original-file: 2025-01-01/Experiments_Cancel.json
     */
    /**
     * Sample code: Cancel a running Experiment.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void cancelARunningExperiment(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.experiments().cancel("exampleRG", "exampleExperiment", com.azure.core.util.Context.NONE);
    }
}
```

### Experiments_CreateOrUpdate

```java
import com.azure.resourcemanager.chaos.models.ChaosExperimentBranch;
import com.azure.resourcemanager.chaos.models.ChaosExperimentStep;
import com.azure.resourcemanager.chaos.models.ChaosTargetListSelector;
import com.azure.resourcemanager.chaos.models.ContinuousAction;
import com.azure.resourcemanager.chaos.models.KeyValuePair;
import com.azure.resourcemanager.chaos.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.chaos.models.ResourceIdentity;
import com.azure.resourcemanager.chaos.models.TargetReference;
import com.azure.resourcemanager.chaos.models.TargetReferenceType;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Experiments CreateOrUpdate.
 */
public final class ExperimentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-01-01/Experiments_CreateOrUpdate.json
     */
    /**
     * Sample code: Create/update a Experiment in a resource group.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void createUpdateAExperimentInAResourceGroup(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.experiments()
            .define("exampleExperiment")
            .withRegion("eastus2euap")
            .withExistingResourceGroup("exampleRG")
            .withSteps(
                Arrays.asList(new ChaosExperimentStep().withName("step1")
                    .withBranches(Arrays.asList(new ChaosExperimentBranch().withName("branch1")
                        .withActions(Arrays.asList(new ContinuousAction()
                            .withName("urn:csci:microsoft:virtualMachine:shutdown/1.0")
                            .withDuration(Duration.parse("PT10M"))
                            .withParameters(
                                Arrays.asList(new KeyValuePair().withKey("fakeTokenPlaceholder").withValue("false")))
                            .withSelectorId("selector1")))))))
            .withSelectors(Arrays.asList(new ChaosTargetListSelector().withId("selector1")
                .withTargets(Arrays.asList(new TargetReference().withType(TargetReferenceType.CHAOS_TARGET)
                    .withId(
                        "/subscriptions/6b052e15-03d3-4f17-b2e1-be7f07588291/resourceGroups/exampleRG/providers/Microsoft.Compute/virtualMachines/exampleVM/providers/Microsoft.Chaos/targets/Microsoft-VirtualMachine")))))
            .withTags(mapOf("key7131", "fakeTokenPlaceholder", "key2138", "fakeTokenPlaceholder"))
            .withIdentity(new ResourceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
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

### Experiments_Delete

```java
/**
 * Samples for Experiments Delete.
 */
public final class ExperimentsDeleteSamples {
    /*
     * x-ms-original-file: 2025-01-01/Experiments_Delete.json
     */
    /**
     * Sample code: Delete a Experiment in a resource group.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void deleteAExperimentInAResourceGroup(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.experiments().delete("exampleRG", "exampleExperiment", com.azure.core.util.Context.NONE);
    }
}
```

### Experiments_ExecutionDetails

```java
/**
 * Samples for Experiments ExecutionDetails.
 */
public final class ExperimentsExecutionDetailsSamples {
    /*
     * x-ms-original-file: 2025-01-01/Experiments_ExecutionDetails.json
     */
    /**
     * Sample code: Get experiment execution details.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getExperimentExecutionDetails(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.experiments()
            .executionDetailsWithResponse("exampleRG", "exampleExperiment", "f24500ad-744e-4a26-864b-b76199eac333",
                com.azure.core.util.Context.NONE);
    }
}
```

### Experiments_GetByResourceGroup

```java
/**
 * Samples for Experiments GetByResourceGroup.
 */
public final class ExperimentsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-01-01/Experiments_Get.json
     */
    /**
     * Sample code: Get a Experiment in a resource group.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getAExperimentInAResourceGroup(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.experiments()
            .getByResourceGroupWithResponse("exampleRG", "exampleExperiment", com.azure.core.util.Context.NONE);
    }
}
```

### Experiments_GetExecution

```java
/**
 * Samples for Experiments GetExecution.
 */
public final class ExperimentsGetExecutionSamples {
    /*
     * x-ms-original-file: 2025-01-01/Experiments_GetExecution.json
     */
    /**
     * Sample code: Get the execution of a Experiment.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getTheExecutionOfAExperiment(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.experiments()
            .getExecutionWithResponse("exampleRG", "exampleExperiment", "f24500ad-744e-4a26-864b-b76199eac333",
                com.azure.core.util.Context.NONE);
    }
}
```

### Experiments_List

```java
/**
 * Samples for Experiments List.
 */
public final class ExperimentsListSamples {
    /*
     * x-ms-original-file: 2025-01-01/Experiments_ListAll.json
     */
    /**
     * Sample code: List all Experiments in a subscription.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void listAllExperimentsInASubscription(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.experiments().list(null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Experiments_ListAllExecutions

```java
/**
 * Samples for Experiments ListAllExecutions.
 */
public final class ExperimentsListAllExecutionsSamples {
    /*
     * x-ms-original-file: 2025-01-01/Experiments_ListAllExecutions.json
     */
    /**
     * Sample code: List all executions of an Experiment.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void listAllExecutionsOfAnExperiment(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.experiments().listAllExecutions("exampleRG", "exampleExperiment", com.azure.core.util.Context.NONE);
    }
}
```

### Experiments_ListByResourceGroup

```java
/**
 * Samples for Experiments ListByResourceGroup.
 */
public final class ExperimentsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-01-01/Experiments_List.json
     */
    /**
     * Sample code: List all Experiments in a resource group.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void listAllExperimentsInAResourceGroup(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.experiments().listByResourceGroup("exampleRG", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Experiments_Start

```java
/**
 * Samples for Experiments Start.
 */
public final class ExperimentsStartSamples {
    /*
     * x-ms-original-file: 2025-01-01/Experiments_Start.json
     */
    /**
     * Sample code: Start a Experiment.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void startAExperiment(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.experiments().start("exampleRG", "exampleExperiment", com.azure.core.util.Context.NONE);
    }
}
```

### Experiments_Update

```java
import com.azure.resourcemanager.chaos.models.Experiment;
import com.azure.resourcemanager.chaos.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.chaos.models.ResourceIdentity;
import com.azure.resourcemanager.chaos.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Experiments Update.
 */
public final class ExperimentsUpdateSamples {
    /*
     * x-ms-original-file: 2025-01-01/Experiments_Update.json
     */
    /**
     * Sample code: Update an Experiment in a resource group.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void updateAnExperimentInAResourceGroup(com.azure.resourcemanager.chaos.ChaosManager manager) {
        Experiment resource = manager.experiments()
            .getByResourceGroupWithResponse("exampleRG", "exampleExperiment", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key1", "fakeTokenPlaceholder", "key2", "fakeTokenPlaceholder"))
            .withIdentity(new ResourceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/6b052e15-03d3-4f17-b2e1-be7f07588291/resourceGroups/exampleRG/providers/Microsoft.ManagedIdentity/userAssignedIdentity/exampleUMI",
                    new UserAssignedIdentity())))
            .apply();
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

### OperationStatuses_Get

```java
/**
 * Samples for OperationStatuses Get.
 */
public final class OperationStatusesGetSamples {
    /*
     * x-ms-original-file: 2025-01-01/OperationStatuses_Get.json
     */
    /**
     * Sample code: Gets Chaos Studio async operation status.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getsChaosStudioAsyncOperationStatus(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.operationStatuses()
            .getWithResponse("westus2", "4bdadd97-207c-4de8-9bba-08339ae099c7", com.azure.core.util.Context.NONE);
    }
}
```

### TargetTypes_Get

```java
/**
 * Samples for TargetTypes Get.
 */
public final class TargetTypesGetSamples {
    /*
     * x-ms-original-file: 2025-01-01/TargetTypes_Get.json
     */
    /**
     * Sample code: Get a Target Type for westus2 location.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getATargetTypeForWestus2Location(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.targetTypes().getWithResponse("westus2", "Microsoft-Agent", com.azure.core.util.Context.NONE);
    }
}
```

### TargetTypes_List

```java
/**
 * Samples for TargetTypes List.
 */
public final class TargetTypesListSamples {
    /*
     * x-ms-original-file: 2025-01-01/TargetTypes_List.json
     */
    /**
     * Sample code: List all Target Types for westus2 location.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void listAllTargetTypesForWestus2Location(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.targetTypes().list("westus2", null, com.azure.core.util.Context.NONE);
    }
}
```

### Targets_CreateOrUpdate

```java
import com.azure.resourcemanager.chaos.fluent.models.TargetInner;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Targets CreateOrUpdate.
 */
public final class TargetsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-01-01/Targets_CreateOrUpdate.json
     */
    /**
     * Sample code: Create/update a Target that extends a virtual machine resource.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void
        createUpdateATargetThatExtendsAVirtualMachineResource(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.targets()
            .createOrUpdateWithResponse("exampleRG", "Microsoft.Compute", "virtualMachines", "exampleVM",
                "Microsoft-VirtualMachine", new TargetInner().withProperties(mapOf()),
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

### Targets_Delete

```java
/**
 * Samples for Targets Delete.
 */
public final class TargetsDeleteSamples {
    /*
     * x-ms-original-file: 2025-01-01/Targets_Delete.json
     */
    /**
     * Sample code: Delete a Target that extends a virtual machine resource.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void
        deleteATargetThatExtendsAVirtualMachineResource(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.targets()
            .deleteWithResponse("exampleRG", "Microsoft.Compute", "virtualMachines", "exampleVM", "Microsoft-Agent",
                com.azure.core.util.Context.NONE);
    }
}
```

### Targets_Get

```java
/**
 * Samples for Targets Get.
 */
public final class TargetsGetSamples {
    /*
     * x-ms-original-file: 2025-01-01/Targets_Get.json
     */
    /**
     * Sample code: Get a Target that extends a virtual machine resource.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void
        getATargetThatExtendsAVirtualMachineResource(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.targets()
            .getWithResponse("exampleRG", "Microsoft.Compute", "virtualMachines", "exampleVM", "Microsoft-Agent",
                com.azure.core.util.Context.NONE);
    }
}
```

### Targets_List

```java
/**
 * Samples for Targets List.
 */
public final class TargetsListSamples {
    /*
     * x-ms-original-file: 2025-01-01/Targets_List.json
     */
    /**
     * Sample code: List all Targets that extend a virtual machine resource.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void
        listAllTargetsThatExtendAVirtualMachineResource(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.targets()
            .list("exampleRG", "Microsoft.Compute", "virtualMachines", "exampleVM", null,
                com.azure.core.util.Context.NONE);
    }
}
```

