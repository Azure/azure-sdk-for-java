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
- [GetByResourceGroup](#experiments_getbyresourcegroup)
- [GetExecutionDetails](#experiments_getexecutiondetails)
- [GetStatus](#experiments_getstatus)
- [List](#experiments_list)
- [ListAllStatuses](#experiments_listallstatuses)
- [ListByResourceGroup](#experiments_listbyresourcegroup)
- [ListExecutionDetails](#experiments_listexecutiondetails)
- [Start](#experiments_start)

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
import com.azure.core.util.Context;
import com.azure.resourcemanager.chaos.fluent.models.CapabilityInner;

/** Samples for Capabilities CreateOrUpdate. */
public final class CapabilitiesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/chaos/resource-manager/Microsoft.Chaos/preview/2021-09-15-preview/examples/CreateOrUpdateACapability.json
     */
    /**
     * Sample code: Create/update a Capability that extends a virtual machine Target resource.
     *
     * @param manager Entry point to ChaosManager.
     */
    public static void createUpdateACapabilityThatExtendsAVirtualMachineTargetResource(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager
            .capabilities()
            .createOrUpdateWithResponse(
                "exampleRG",
                "Microsoft.Compute",
                "virtualMachines",
                "exampleVM",
                "Microsoft-VirtualMachine",
                "Shutdown-1.0",
                new CapabilityInner(),
                Context.NONE);
    }
}
```

### Capabilities_Delete

```java
import com.azure.core.util.Context;

/** Samples for Capabilities Delete. */
public final class CapabilitiesDeleteSamples {
    /*
     * x-ms-original-file: specification/chaos/resource-manager/Microsoft.Chaos/preview/2021-09-15-preview/examples/DeleteACapability.json
     */
    /**
     * Sample code: Delete a Capability that extends a virtual machine Target resource.
     *
     * @param manager Entry point to ChaosManager.
     */
    public static void deleteACapabilityThatExtendsAVirtualMachineTargetResource(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager
            .capabilities()
            .deleteWithResponse(
                "exampleRG",
                "Microsoft.Compute",
                "virtualMachines",
                "exampleVM",
                "Microsoft-VirtualMachine",
                "Shutdown-1.0",
                Context.NONE);
    }
}
```

### Capabilities_Get

```java
import com.azure.core.util.Context;

/** Samples for Capabilities Get. */
public final class CapabilitiesGetSamples {
    /*
     * x-ms-original-file: specification/chaos/resource-manager/Microsoft.Chaos/preview/2021-09-15-preview/examples/GetACapability.json
     */
    /**
     * Sample code: Get a Capability that extends a virtual machine Target resource.
     *
     * @param manager Entry point to ChaosManager.
     */
    public static void getACapabilityThatExtendsAVirtualMachineTargetResource(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager
            .capabilities()
            .getWithResponse(
                "exampleRG",
                "Microsoft.Compute",
                "virtualMachines",
                "exampleVM",
                "Microsoft-VirtualMachine",
                "Shutdown-1.0",
                Context.NONE);
    }
}
```

### Capabilities_List

```java
import com.azure.core.util.Context;

/** Samples for Capabilities List. */
public final class CapabilitiesListSamples {
    /*
     * x-ms-original-file: specification/chaos/resource-manager/Microsoft.Chaos/preview/2021-09-15-preview/examples/ListCapabilities.json
     */
    /**
     * Sample code: List all Capabilities that extend a virtual machine Target resource.
     *
     * @param manager Entry point to ChaosManager.
     */
    public static void listAllCapabilitiesThatExtendAVirtualMachineTargetResource(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager
            .capabilities()
            .list(
                "exampleRG",
                "Microsoft.Compute",
                "virtualMachines",
                "exampleVM",
                "Microsoft-VirtualMachine",
                null,
                Context.NONE);
    }
}
```

### CapabilityTypes_Get

```java
import com.azure.core.util.Context;

/** Samples for CapabilityTypes Get. */
public final class CapabilityTypesGetSamples {
    /*
     * x-ms-original-file: specification/chaos/resource-manager/Microsoft.Chaos/preview/2021-09-15-preview/examples/GetACapabilityType.json
     */
    /**
     * Sample code: Get a Capability Type for a virtual machine Target resource on westus2 location.
     *
     * @param manager Entry point to ChaosManager.
     */
    public static void getACapabilityTypeForAVirtualMachineTargetResourceOnWestus2Location(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.capabilityTypes().getWithResponse("westus2", "Microsoft-VirtualMachine", "Shutdown-1.0", Context.NONE);
    }
}
```

### CapabilityTypes_List

```java
import com.azure.core.util.Context;

/** Samples for CapabilityTypes List. */
public final class CapabilityTypesListSamples {
    /*
     * x-ms-original-file: specification/chaos/resource-manager/Microsoft.Chaos/preview/2021-09-15-preview/examples/ListCapabilityTypes.json
     */
    /**
     * Sample code: List all Capability Types for a virtual machine Target resource on westus2 location.
     *
     * @param manager Entry point to ChaosManager.
     */
    public static void listAllCapabilityTypesForAVirtualMachineTargetResourceOnWestus2Location(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.capabilityTypes().list("westus2", "Microsoft-VirtualMachine", null, Context.NONE);
    }
}
```

### Experiments_Cancel

```java
import com.azure.core.util.Context;

/** Samples for Experiments Cancel. */
public final class ExperimentsCancelSamples {
    /*
     * x-ms-original-file: specification/chaos/resource-manager/Microsoft.Chaos/preview/2021-09-15-preview/examples/CancelAExperiment.json
     */
    /**
     * Sample code: Cancel a running Experiment.
     *
     * @param manager Entry point to ChaosManager.
     */
    public static void cancelARunningExperiment(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.experiments().cancel("exampleRG", "exampleExperiment", Context.NONE);
    }
}
```

### Experiments_CreateOrUpdate

```java
import com.azure.resourcemanager.chaos.models.Branch;
import com.azure.resourcemanager.chaos.models.ContinuousAction;
import com.azure.resourcemanager.chaos.models.ResourceIdentity;
import com.azure.resourcemanager.chaos.models.ResourceIdentityType;
import com.azure.resourcemanager.chaos.models.Selector;
import com.azure.resourcemanager.chaos.models.SelectorType;
import com.azure.resourcemanager.chaos.models.Step;
import com.azure.resourcemanager.chaos.models.TargetReference;
import java.util.Arrays;

/** Samples for Experiments CreateOrUpdate. */
public final class ExperimentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/chaos/resource-manager/Microsoft.Chaos/preview/2021-09-15-preview/examples/CreateOrUpdateAExperiment.json
     */
    /**
     * Sample code: Create/update a Experiment in a resource group.
     *
     * @param manager Entry point to ChaosManager.
     */
    public static void createUpdateAExperimentInAResourceGroup(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager
            .experiments()
            .define("exampleExperiment")
            .withRegion("centraluseuap")
            .withExistingResourceGroup("exampleRG")
            .withSteps(
                Arrays
                    .asList(
                        new Step()
                            .withName("step1")
                            .withBranches(
                                Arrays
                                    .asList(
                                        new Branch()
                                            .withName("branch1")
                                            .withActions(
                                                Arrays
                                                    .asList(
                                                        new ContinuousAction()
                                                            .withName("urn:csci:provider:providername:Shutdown/1.0")
                                                            .withDuration("PT10M")
                                                            .withParameters(Arrays.asList())
                                                            .withSelectorId("selector1")))))))
            .withSelectors(
                Arrays
                    .asList(
                        new Selector()
                            .withType(SelectorType.LIST)
                            .withId("selector1")
                            .withTargets(
                                Arrays
                                    .asList(
                                        new TargetReference()
                                            .withId(
                                                "/subscriptions/6b052e15-03d3-4f17-b2e1-be7f07588291/resourceGroups/exampleRG/providers/Microsoft.Compute/virtualMachines/exampleVM/providers/Microsoft.Chaos/targets/Microsoft-VirtualMachine")))))
            .withIdentity(new ResourceIdentity().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
            .create();
    }
}
```

### Experiments_Delete

```java
import com.azure.core.util.Context;

/** Samples for Experiments Delete. */
public final class ExperimentsDeleteSamples {
    /*
     * x-ms-original-file: specification/chaos/resource-manager/Microsoft.Chaos/preview/2021-09-15-preview/examples/DeleteAExperiment.json
     */
    /**
     * Sample code: Delete a Experiment in a resource group.
     *
     * @param manager Entry point to ChaosManager.
     */
    public static void deleteAExperimentInAResourceGroup(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.experiments().deleteWithResponse("exampleRG", "exampleExperiment", Context.NONE);
    }
}
```

### Experiments_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Experiments GetByResourceGroup. */
public final class ExperimentsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/chaos/resource-manager/Microsoft.Chaos/preview/2021-09-15-preview/examples/GetAExperiment.json
     */
    /**
     * Sample code: Get a Experiment in a resource group.
     *
     * @param manager Entry point to ChaosManager.
     */
    public static void getAExperimentInAResourceGroup(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.experiments().getByResourceGroupWithResponse("exampleRG", "exampleExperiment", Context.NONE);
    }
}
```

### Experiments_GetExecutionDetails

```java
import com.azure.core.util.Context;

/** Samples for Experiments GetExecutionDetails. */
public final class ExperimentsGetExecutionDetailsSamples {
    /*
     * x-ms-original-file: specification/chaos/resource-manager/Microsoft.Chaos/preview/2021-09-15-preview/examples/GetAExperimentExecutionDetails.json
     */
    /**
     * Sample code: Get experiment execution details.
     *
     * @param manager Entry point to ChaosManager.
     */
    public static void getExperimentExecutionDetails(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager
            .experiments()
            .getExecutionDetailsWithResponse(
                "exampleRG", "exampleExperiment", "f24500ad-744e-4a26-864b-b76199eac333", Context.NONE);
    }
}
```

### Experiments_GetStatus

```java
import com.azure.core.util.Context;

/** Samples for Experiments GetStatus. */
public final class ExperimentsGetStatusSamples {
    /*
     * x-ms-original-file: specification/chaos/resource-manager/Microsoft.Chaos/preview/2021-09-15-preview/examples/GetAExperimentStatus.json
     */
    /**
     * Sample code: Get the status of a Experiment.
     *
     * @param manager Entry point to ChaosManager.
     */
    public static void getTheStatusOfAExperiment(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager
            .experiments()
            .getStatusWithResponse(
                "exampleRG", "exampleExperiment", "50734542-2e64-4e08-814c-cc0e7475f7e4", Context.NONE);
    }
}
```

### Experiments_List

```java
import com.azure.core.util.Context;

/** Samples for Experiments List. */
public final class ExperimentsListSamples {
    /*
     * x-ms-original-file: specification/chaos/resource-manager/Microsoft.Chaos/preview/2021-09-15-preview/examples/ListExperimentsInASubscription.json
     */
    /**
     * Sample code: List all Experiments in a subscription.
     *
     * @param manager Entry point to ChaosManager.
     */
    public static void listAllExperimentsInASubscription(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.experiments().list(null, null, Context.NONE);
    }
}
```

### Experiments_ListAllStatuses

```java
import com.azure.core.util.Context;

/** Samples for Experiments ListAllStatuses. */
public final class ExperimentsListAllStatusesSamples {
    /*
     * x-ms-original-file: specification/chaos/resource-manager/Microsoft.Chaos/preview/2021-09-15-preview/examples/ListExperimentStatuses.json
     */
    /**
     * Sample code: List all statuses of a Experiment.
     *
     * @param manager Entry point to ChaosManager.
     */
    public static void listAllStatusesOfAExperiment(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.experiments().listAllStatuses("exampleRG", "exampleExperiment", Context.NONE);
    }
}
```

### Experiments_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Experiments ListByResourceGroup. */
public final class ExperimentsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/chaos/resource-manager/Microsoft.Chaos/preview/2021-09-15-preview/examples/ListExperimentsInAResourceGroup.json
     */
    /**
     * Sample code: List all Experiments in a resource group.
     *
     * @param manager Entry point to ChaosManager.
     */
    public static void listAllExperimentsInAResourceGroup(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.experiments().listByResourceGroup("exampleRG", null, null, Context.NONE);
    }
}
```

### Experiments_ListExecutionDetails

```java
import com.azure.core.util.Context;

/** Samples for Experiments ListExecutionDetails. */
public final class ExperimentsListExecutionDetailsSamples {
    /*
     * x-ms-original-file: specification/chaos/resource-manager/Microsoft.Chaos/preview/2021-09-15-preview/examples/ListExperimentExecutionsDetails.json
     */
    /**
     * Sample code: List experiment executions details.
     *
     * @param manager Entry point to ChaosManager.
     */
    public static void listExperimentExecutionsDetails(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.experiments().listExecutionDetails("exampleRG", "exampleExperiment", Context.NONE);
    }
}
```

### Experiments_Start

```java
import com.azure.core.util.Context;

/** Samples for Experiments Start. */
public final class ExperimentsStartSamples {
    /*
     * x-ms-original-file: specification/chaos/resource-manager/Microsoft.Chaos/preview/2021-09-15-preview/examples/StartAExperiment.json
     */
    /**
     * Sample code: Start a Experiment.
     *
     * @param manager Entry point to ChaosManager.
     */
    public static void startAExperiment(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.experiments().startWithResponse("exampleRG", "exampleExperiment", Context.NONE);
    }
}
```

### TargetTypes_Get

```java
import com.azure.core.util.Context;

/** Samples for TargetTypes Get. */
public final class TargetTypesGetSamples {
    /*
     * x-ms-original-file: specification/chaos/resource-manager/Microsoft.Chaos/preview/2021-09-15-preview/examples/GetATargetType.json
     */
    /**
     * Sample code: Get a Target Type for westus2 location.
     *
     * @param manager Entry point to ChaosManager.
     */
    public static void getATargetTypeForWestus2Location(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.targetTypes().getWithResponse("westus2", "Microsoft-Agent", Context.NONE);
    }
}
```

### TargetTypes_List

```java
import com.azure.core.util.Context;

/** Samples for TargetTypes List. */
public final class TargetTypesListSamples {
    /*
     * x-ms-original-file: specification/chaos/resource-manager/Microsoft.Chaos/preview/2021-09-15-preview/examples/ListTargetTypes.json
     */
    /**
     * Sample code: List all Target Types for westus2 location.
     *
     * @param manager Entry point to ChaosManager.
     */
    public static void listAllTargetTypesForWestus2Location(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.targetTypes().list("westus2", null, Context.NONE);
    }
}
```

### Targets_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.chaos.fluent.models.TargetInner;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Samples for Targets CreateOrUpdate. */
public final class TargetsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/chaos/resource-manager/Microsoft.Chaos/preview/2021-09-15-preview/examples/CreateOrUpdateATarget.json
     */
    /**
     * Sample code: Create/update a Target that extends a virtual machine resource.
     *
     * @param manager Entry point to ChaosManager.
     */
    public static void createUpdateATargetThatExtendsAVirtualMachineResource(
        com.azure.resourcemanager.chaos.ChaosManager manager) throws IOException {
        manager
            .targets()
            .createOrUpdateWithResponse(
                "exampleRG",
                "Microsoft.Compute",
                "virtualMachines",
                "exampleVM",
                "Microsoft-Agent",
                new TargetInner()
                    .withProperties(
                        mapOf(
                            "identities",
                            SerializerFactory
                                .createDefaultManagementSerializerAdapter()
                                .deserialize(
                                    "[{\"type\":\"CertificateSubjectIssuer\",\"subject\":\"CN=example.subject\"}]",
                                    Object.class,
                                    SerializerEncoding.JSON))),
                Context.NONE);
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

### Targets_Delete

```java
import com.azure.core.util.Context;

/** Samples for Targets Delete. */
public final class TargetsDeleteSamples {
    /*
     * x-ms-original-file: specification/chaos/resource-manager/Microsoft.Chaos/preview/2021-09-15-preview/examples/DeleteATarget.json
     */
    /**
     * Sample code: Delete a Target that extends a virtual machine resource.
     *
     * @param manager Entry point to ChaosManager.
     */
    public static void deleteATargetThatExtendsAVirtualMachineResource(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager
            .targets()
            .deleteWithResponse(
                "exampleRG", "Microsoft.Compute", "virtualMachines", "exampleVM", "Microsoft-Agent", Context.NONE);
    }
}
```

### Targets_Get

```java
import com.azure.core.util.Context;

/** Samples for Targets Get. */
public final class TargetsGetSamples {
    /*
     * x-ms-original-file: specification/chaos/resource-manager/Microsoft.Chaos/preview/2021-09-15-preview/examples/GetATarget.json
     */
    /**
     * Sample code: Get a Target that extends a virtual machine resource.
     *
     * @param manager Entry point to ChaosManager.
     */
    public static void getATargetThatExtendsAVirtualMachineResource(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager
            .targets()
            .getWithResponse(
                "exampleRG", "Microsoft.Compute", "virtualMachines", "exampleVM", "Microsoft-Agent", Context.NONE);
    }
}
```

### Targets_List

```java
import com.azure.core.util.Context;

/** Samples for Targets List. */
public final class TargetsListSamples {
    /*
     * x-ms-original-file: specification/chaos/resource-manager/Microsoft.Chaos/preview/2021-09-15-preview/examples/ListTargets.json
     */
    /**
     * Sample code: List all Targets that extend a virtual machine resource.
     *
     * @param manager Entry point to ChaosManager.
     */
    public static void listAllTargetsThatExtendAVirtualMachineResource(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.targets().list("exampleRG", "Microsoft.Compute", "virtualMachines", "exampleVM", null, Context.NONE);
    }
}
```

