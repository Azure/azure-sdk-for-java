# Code snippets and samples


## ActionVersions

- [Get](#actionversions_get)
- [List](#actionversions_list)

## Actions

- [Get](#actions_get)
- [List](#actions_list)

## Capabilities

- [CreateOrUpdate](#capabilities_createorupdate)
- [Delete](#capabilities_delete)
- [Get](#capabilities_get)
- [List](#capabilities_list)

## CapabilityTypes

- [Get](#capabilitytypes_get)
- [List](#capabilitytypes_list)

## Connections

- [CreateOrUpdate](#connections_createorupdate)
- [Delete](#connections_delete)
- [Get](#connections_get)
- [ListAll](#connections_listall)

## DiscoveredResources

- [Get](#discoveredresources_get)
- [ListByWorkspace](#discoveredresources_listbyworkspace)

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

## Operations

- [List](#operations_list)

## ScenarioConfigurations

- [CreateOrUpdate](#scenarioconfigurations_createorupdate)
- [Delete](#scenarioconfigurations_delete)
- [Execute](#scenarioconfigurations_execute)
- [FixResourcePermissions](#scenarioconfigurations_fixresourcepermissions)
- [Get](#scenarioconfigurations_get)
- [ListAll](#scenarioconfigurations_listall)
- [Validate](#scenarioconfigurations_validate)

## ScenarioRuns

- [Cancel](#scenarioruns_cancel)
- [Get](#scenarioruns_get)
- [ListAll](#scenarioruns_listall)

## ScenarioTemplateVersions

- [Get](#scenariotemplateversions_get)
- [List](#scenariotemplateversions_list)

## ScenarioTemplates

- [Get](#scenariotemplates_get)
- [List](#scenariotemplates_list)

## Scenarios

- [CreateOrUpdate](#scenarios_createorupdate)
- [Delete](#scenarios_delete)
- [Get](#scenarios_get)
- [ListAll](#scenarios_listall)

## TargetTypes

- [Get](#targettypes_get)
- [List](#targettypes_list)

## Targets

- [CreateOrUpdate](#targets_createorupdate)
- [Delete](#targets_delete)
- [Get](#targets_get)
- [List](#targets_list)

## WorkspaceDiscoveries

- [Get](#workspacediscoveries_get)
- [List](#workspacediscoveries_list)

## WorkspaceEvaluations

- [Get](#workspaceevaluations_get)
- [List](#workspaceevaluations_list)

## Workspaces

- [CreateOrUpdate](#workspaces_createorupdate)
- [Delete](#workspaces_delete)
- [Discover](#workspaces_discover)
- [Evaluate](#workspaces_evaluate)
- [GetByResourceGroup](#workspaces_getbyresourcegroup)
- [List](#workspaces_list)
- [ListByResourceGroup](#workspaces_listbyresourcegroup)
- [Update](#workspaces_update)
### ActionVersions_Get

```java
/**
 * Samples for ActionVersions Get.
 */
public final class ActionVersionsGetSamples {
    /*
     * x-ms-original-file: 2026-11-01/ActionVersions_Get.json
     */
    /**
     * Sample code: Get an Action Version for westus2 location.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getAnActionVersionForWestus2Location(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.actionVersions()
            .getWithResponse("westus2", "microsoft-compute-shutdown", "1.0", com.azure.core.util.Context.NONE);
    }
}
```

### ActionVersions_List

```java
/**
 * Samples for ActionVersions List.
 */
public final class ActionVersionsListSamples {
    /*
     * x-ms-original-file: 2026-11-01/ActionVersions_List.json
     */
    /**
     * Sample code: List all Action Versions for a given action.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void listAllActionVersionsForAGivenAction(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.actionVersions().list("westus2", "microsoft-compute-shutdown", null, com.azure.core.util.Context.NONE);
    }
}
```

### Actions_Get

```java
/**
 * Samples for Actions Get.
 */
public final class ActionsGetSamples {
    /*
     * x-ms-original-file: 2026-11-01/Actions_Get.json
     */
    /**
     * Sample code: Get an Action for westus2 location.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getAnActionForWestus2Location(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.actions().getWithResponse("westus2", "microsoft-compute-shutdown", com.azure.core.util.Context.NONE);
    }
}
```

### Actions_List

```java
/**
 * Samples for Actions List.
 */
public final class ActionsListSamples {
    /*
     * x-ms-original-file: 2026-11-01/Actions_List.json
     */
    /**
     * Sample code: List all Actions for westus2 location.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void listAllActionsForWestus2Location(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.actions().list("westus2", null, com.azure.core.util.Context.NONE);
    }
}
```

### Capabilities_CreateOrUpdate

```java
import com.azure.resourcemanager.chaos.fluent.models.CapabilityInner;

/**
 * Samples for Capabilities CreateOrUpdate.
 */
public final class CapabilitiesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-11-01/Capabilities_CreateOrUpdate.json
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
     * x-ms-original-file: 2026-11-01/Capabilities_Delete.json
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
     * x-ms-original-file: 2026-11-01/Capabilities_Get.json
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
     * x-ms-original-file: 2026-11-01/Capabilities_List.json
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
     * x-ms-original-file: 2026-11-01/CapabilityTypes_Get.json
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
     * x-ms-original-file: 2026-11-01/CapabilityTypes_List.json
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

### Connections_CreateOrUpdate

```java
import com.azure.resourcemanager.chaos.models.ConnectionKind;
import com.azure.resourcemanager.chaos.models.ConnectionProperties;

/**
 * Samples for Connections CreateOrUpdate.
 */
public final class ConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-11-01/Connections_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update a connection.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void createOrUpdateAConnection(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.connections()
            .define("aksClusterConnection")
            .withExistingWorkspace("exampleRG", "exampleWorkspace")
            .withProperties(new ConnectionProperties().withKind(ConnectionKind.AKS_EXTENSION)
                .withTargetResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/exampleRG/providers/Microsoft.ContainerService/managedClusters/exampleCluster")
                .withPrincipalId("00000000-0000-0000-0000-000000000000")
                .withTenantId("00000000-0000-0000-0000-000000000000"))
            .create();
    }
}
```

### Connections_Delete

```java
/**
 * Samples for Connections Delete.
 */
public final class ConnectionsDeleteSamples {
    /*
     * x-ms-original-file: 2026-11-01/Connections_Delete.json
     */
    /**
     * Sample code: Delete a connection in a workspace.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void deleteAConnectionInAWorkspace(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.connections()
            .deleteWithResponse("exampleRG", "exampleWorkspace", "aksClusterConnection",
                com.azure.core.util.Context.NONE);
    }
}
```

### Connections_Get

```java
/**
 * Samples for Connections Get.
 */
public final class ConnectionsGetSamples {
    /*
     * x-ms-original-file: 2026-11-01/Connections_Get.json
     */
    /**
     * Sample code: Get a connection.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getAConnection(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.connections()
            .getWithResponse("exampleRG", "exampleWorkspace", "aksClusterConnection", com.azure.core.util.Context.NONE);
    }
}
```

### Connections_ListAll

```java
/**
 * Samples for Connections ListAll.
 */
public final class ConnectionsListAllSamples {
    /*
     * x-ms-original-file: 2026-11-01/Connections_ListAll.json
     */
    /**
     * Sample code: Get a list of connections.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getAListOfConnections(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.connections().listAll("exampleRG", "exampleWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### DiscoveredResources_Get

```java
/**
 * Samples for DiscoveredResources Get.
 */
public final class DiscoveredResourcesGetSamples {
    /*
     * x-ms-original-file: 2026-11-01/DiscoveredResources_Get.json
     */
    /**
     * Sample code: Get a discovered resource.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getADiscoveredResource(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.discoveredResources()
            .getWithResponse("exampleRG", "exampleWorkspace", "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                com.azure.core.util.Context.NONE);
    }
}
```

### DiscoveredResources_ListByWorkspace

```java
/**
 * Samples for DiscoveredResources ListByWorkspace.
 */
public final class DiscoveredResourcesListByWorkspaceSamples {
    /*
     * x-ms-original-file: 2026-11-01/DiscoveredResources_ListByWorkspace.json
     */
    /**
     * Sample code: Get a list of discovered resources for a workspace.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void
        getAListOfDiscoveredResourcesForAWorkspace(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.discoveredResources()
            .listByWorkspace("exampleRG", "exampleWorkspace", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-11-01/Experiments_Cancel.json
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
import com.azure.resourcemanager.chaos.models.CustomerDataStorageProperties;
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
     * x-ms-original-file: 2026-11-01/Experiments_CreateOrUpdate.json
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
            .withCustomerDataStorage(new CustomerDataStorageProperties().withStorageAccountResourceId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/exampleRG/providers/Microsoft.Storage/storageAccounts/exampleStorage")
                .withBlobContainerName("azurechaosstudioexperiments"))
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
     * x-ms-original-file: 2026-11-01/Experiments_Delete.json
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
     * x-ms-original-file: 2026-11-01/Experiments_ExecutionDetails.json
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
     * x-ms-original-file: 2026-11-01/Experiments_Get.json
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
     * x-ms-original-file: 2026-11-01/Experiments_GetExecution.json
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
     * x-ms-original-file: 2026-11-01/Experiments_ListAll.json
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
     * x-ms-original-file: 2026-11-01/Experiments_ListAllExecutions.json
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
     * x-ms-original-file: 2026-11-01/Experiments_List.json
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
     * x-ms-original-file: 2026-11-01/Experiments_Start.json
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
     * x-ms-original-file: 2026-11-01/Experiments_Update.json
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
     * x-ms-original-file: 2026-11-01/OperationStatuses_Get_CustomerKeyAccessFailed.json
     */
    /**
     * Sample code: Get a failed Workspace encryption operation status.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void
        getAFailedWorkspaceEncryptionOperationStatus(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.operationStatuses()
            .getWithResponse("westus2", "4bdadd97-207c-4de8-9bba-08339ae099c7", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-11-01/OperationStatuses_Get.json
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

### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2026-11-01/Operations_List.json
     */
    /**
     * Sample code: Lists all Chaos Studio operations.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void listsAllChaosStudioOperations(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ScenarioConfigurations_CreateOrUpdate

```java
import com.azure.resourcemanager.chaos.models.KeyValuePair;
import com.azure.resourcemanager.chaos.models.ResourceTargeting;
import com.azure.resourcemanager.chaos.models.ResourceTargetingCriteria;
import com.azure.resourcemanager.chaos.models.ScenarioConfiguration;
import com.azure.resourcemanager.chaos.models.ScenarioConfigurationProperties;
import java.util.Arrays;

/**
 * Samples for ScenarioConfigurations CreateOrUpdate.
 */
public final class ScenarioConfigurationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-11-01/ScenarioConfigurations_CreateOrUpdate_CreateOnly.json
     */
    /**
     * Sample code: Create a scenario configuration only when absent, or return 412 when it already exists.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void createAScenarioConfigurationOnlyWhenAbsentOrReturn412WhenItAlreadyExists(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.scenarioConfigurations()
            .define("exampleConfiguration")
            .withExistingScenario("exampleRG", "exampleWorkspace", "exampleScenario")
            .withProperties(new ScenarioConfigurationProperties().withScenarioId(
                "/subscriptions/6b052e15-03d3-4f17-b2e1-be7f07588291/resourceGroups/exampleRG/providers/Microsoft.Chaos/workspaces/exampleWorkspace/scenarios/exampleScenario"))
            .withIfNoneMatch("*")
            .create();
    }

    /*
     * x-ms-original-file: 2026-11-01/ScenarioConfigurations_CreateOrUpdate_With_Physical_Zones.json
     */
    /**
     * Sample code: Create or update a scenario configuration with physical zone targeting.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void createOrUpdateAScenarioConfigurationWithPhysicalZoneTargeting(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.scenarioConfigurations()
            .define("config-physical-zone")
            .withExistingScenario("exampleRG", "exampleWorkspace", "12345678-1234-1234-1234-123456789012")
            .withProperties(new ScenarioConfigurationProperties().withScenarioId(
                "/subscriptions/6b052e15-03d3-4f17-b2e1-be7f07588291/resourceGroups/exampleRG/providers/Microsoft.Chaos/workspaces/exampleWorkspace/scenarios/12345678-1234-1234-1234-123456789012")
                .withParameters(Arrays.asList(new KeyValuePair().withKey("fakeTokenPlaceholder").withValue("PT10M")))
                .withResourceTargeting(new ResourceTargeting()
                    .withInclude(new ResourceTargetingCriteria().withPhysicalZones(Arrays.asList("westus2-az1")))
                    .withExclude(new ResourceTargetingCriteria().withResources(Arrays.asList(
                        "/subscriptions/6b052e15-03d3-4f17-b2e1-be7f07588291/resourceGroups/exampleRG/providers/Microsoft.Compute/virtualMachines/protectedVM")))))
            .create();
    }

    /*
     * x-ms-original-file: 2026-11-01/ScenarioConfigurations_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update a scenario configuration.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void createOrUpdateAScenarioConfiguration(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.scenarioConfigurations()
            .define("config-5678-9012-3456-789012345678")
            .withExistingScenario("exampleRG", "exampleWorkspace", "12345678-1234-1234-1234-123456789012")
            .withProperties(new ScenarioConfigurationProperties().withScenarioId(
                "/subscriptions/6b052e15-03d3-4f17-b2e1-be7f07588291/resourceGroups/exampleRG/providers/Microsoft.Chaos/workspaces/exampleWorkspace/scenarios/12345678-1234-1234-1234-123456789012")
                .withParameters(Arrays.asList(new KeyValuePair().withKey("fakeTokenPlaceholder").withValue("PT10M"),
                    new KeyValuePair().withKey("fakeTokenPlaceholder")
                        .withValue(
                            "[\"/subscriptions/6b052e15-03d3-4f17-b2e1-be7f07588291/resourceGroups/exampleRG/providers/Microsoft.Compute/virtualMachines/vm1\",\"/subscriptions/6b052e15-03d3-4f17-b2e1-be7f07588291/resourceGroups/exampleRG/providers/Microsoft.Compute/virtualMachines/vm2\"]")))
                .withResourceTargeting(new ResourceTargeting()
                    .withInclude(new ResourceTargetingCriteria().withLocations(Arrays.asList("eastus"))
                        .withZones(Arrays.asList("1")))
                    .withExclude(new ResourceTargetingCriteria()
                        .withTypes(Arrays.asList("Microsoft.Compute/virtualMachineScaleSets"))
                        .withTags(
                            Arrays.asList(new KeyValuePair().withKey("fakeTokenPlaceholder").withValue("production")))
                        .withResources(Arrays.asList(
                            "/subscriptions/6b052e15-03d3-4f17-b2e1-be7f07588291/resourceGroups/exampleRG/providers/Microsoft.Compute/virtualMachines/protectedVM")))))
            .create();
    }

    /*
     * x-ms-original-file: 2026-11-01/ScenarioConfigurations_CreateOrUpdate_UpdateOnly.json
     */
    /**
     * Sample code: Update a scenario configuration only when present, or return 412 when it does not exist.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void updateAScenarioConfigurationOnlyWhenPresentOrReturn412WhenItDoesNotExist(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        ScenarioConfiguration resource = manager.scenarioConfigurations()
            .getWithResponse("exampleRG", "exampleWorkspace", "exampleScenario", "exampleConfiguration",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new ScenarioConfigurationProperties().withScenarioId(
                "/subscriptions/6b052e15-03d3-4f17-b2e1-be7f07588291/resourceGroups/exampleRG/providers/Microsoft.Chaos/workspaces/exampleWorkspace/scenarios/exampleScenario"))
            .withIfMatch("*")
            .apply();
    }
}
```

### ScenarioConfigurations_Delete

```java
/**
 * Samples for ScenarioConfigurations Delete.
 */
public final class ScenarioConfigurationsDeleteSamples {
    /*
     * x-ms-original-file: 2026-11-01/ScenarioConfigurations_Delete.json
     */
    /**
     * Sample code: Delete a scenario configuration.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void deleteAScenarioConfiguration(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.scenarioConfigurations()
            .delete("exampleRG", "exampleWorkspace", "12345678-1234-1234-1234-123456789012",
                "config-5678-9012-3456-789012345678", com.azure.core.util.Context.NONE);
    }
}
```

### ScenarioConfigurations_Execute

```java
/**
 * Samples for ScenarioConfigurations Execute.
 */
public final class ScenarioConfigurationsExecuteSamples {
    /*
     * x-ms-original-file: 2026-11-01/ScenarioConfigurations_Execute.json
     */
    /**
     * Sample code: Execute the scenario execution with the given scenario configuration.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void executeTheScenarioExecutionWithTheGivenScenarioConfiguration(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.scenarioConfigurations()
            .execute("exampleRG", "exampleWorkspace", "12345678-1234-1234-1234-123456789012",
                "config-5678-9012-3456-789012345678", com.azure.core.util.Context.NONE);
    }
}
```

### ScenarioConfigurations_FixResourcePermissions

```java

/**
 * Samples for ScenarioConfigurations FixResourcePermissions.
 */
public final class ScenarioConfigurationsFixResourcePermissionsSamples {
    /*
     * x-ms-original-file: 2026-11-01/ScenarioConfigurations_FixResourcePermissions.json
     */
    /**
     * Sample code: Fixes resource permissions for the given scenario configuration.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void
        fixesResourcePermissionsForTheGivenScenarioConfiguration(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.scenarioConfigurations()
            .fixResourcePermissions("exampleRG", "exampleWorkspace", "12345678-1234-1234-1234-123456789012",
                "config-5678-9012-3456-789012345678", null, com.azure.core.util.Context.NONE);
    }
}
```

### ScenarioConfigurations_Get

```java
/**
 * Samples for ScenarioConfigurations Get.
 */
public final class ScenarioConfigurationsGetSamples {
    /*
     * x-ms-original-file: 2026-11-01/ScenarioConfigurations_Get.json
     */
    /**
     * Sample code: Get a scenario configuration.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getAScenarioConfiguration(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.scenarioConfigurations()
            .getWithResponse("exampleRG", "exampleWorkspace", "12345678-1234-1234-1234-123456789012",
                "config-5678-9012-3456-789012345678", com.azure.core.util.Context.NONE);
    }
}
```

### ScenarioConfigurations_ListAll

```java
/**
 * Samples for ScenarioConfigurations ListAll.
 */
public final class ScenarioConfigurationsListAllSamples {
    /*
     * x-ms-original-file: 2026-11-01/ScenarioConfigurations_ListAll.json
     */
    /**
     * Sample code: Get a list of scenario configurations.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getAListOfScenarioConfigurations(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.scenarioConfigurations()
            .listAll("exampleRG", "exampleWorkspace", "12345678-1234-1234-1234-123456789012",
                com.azure.core.util.Context.NONE);
    }
}
```

### ScenarioConfigurations_Validate

```java
/**
 * Samples for ScenarioConfigurations Validate.
 */
public final class ScenarioConfigurationsValidateSamples {
    /*
     * x-ms-original-file: 2026-11-01/ScenarioConfigurations_Validate.json
     */
    /**
     * Sample code: Validate the given scenario configuration.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void validateTheGivenScenarioConfiguration(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.scenarioConfigurations()
            .validate("exampleRG", "exampleWorkspace", "12345678-1234-1234-1234-123456789012",
                "config-5678-9012-3456-789012345678", com.azure.core.util.Context.NONE);
    }
}
```

### ScenarioRuns_Cancel

```java
/**
 * Samples for ScenarioRuns Cancel.
 */
public final class ScenarioRunsCancelSamples {
    /*
     * x-ms-original-file: 2026-11-01/ScenarioRuns_Cancel.json
     */
    /**
     * Sample code: Cancel a running scenario run.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void cancelARunningScenarioRun(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.scenarioRuns()
            .cancel("exampleRG", "exampleWorkspace", "12345678-1234-1234-1234-123456789012",
                "abcd1234-5678-9012-3456-789012345678", com.azure.core.util.Context.NONE);
    }
}
```

### ScenarioRuns_Get

```java
/**
 * Samples for ScenarioRuns Get.
 */
public final class ScenarioRunsGetSamples {
    /*
     * x-ms-original-file: 2026-11-01/ScenarioRuns_Get_Partially_Succeeded.json
     */
    /**
     * Sample code: Get a scenario run that completed with suppressed action failures.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void
        getAScenarioRunThatCompletedWithSuppressedActionFailures(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.scenarioRuns()
            .getWithResponse("exampleRG", "exampleWorkspace", "12345678-1234-1234-1234-123456789012",
                "abcd1234-5678-9012-3456-789012345678", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-11-01/ScenarioRuns_Get.json
     */
    /**
     * Sample code: Get a scenario run.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getAScenarioRun(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.scenarioRuns()
            .getWithResponse("exampleRG", "exampleWorkspace", "12345678-1234-1234-1234-123456789012",
                "abcd1234-5678-9012-3456-789012345678", com.azure.core.util.Context.NONE);
    }
}
```

### ScenarioRuns_ListAll

```java
/**
 * Samples for ScenarioRuns ListAll.
 */
public final class ScenarioRunsListAllSamples {
    /*
     * x-ms-original-file: 2026-11-01/ScenarioRuns_ListAll.json
     */
    /**
     * Sample code: Get a list of scenario runs.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getAListOfScenarioRuns(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.scenarioRuns()
            .listAll("exampleRG", "exampleWorkspace", "12345678-1234-1234-1234-123456789012",
                com.azure.core.util.Context.NONE);
    }
}
```

### ScenarioTemplateVersions_Get

```java
/**
 * Samples for ScenarioTemplateVersions Get.
 */
public final class ScenarioTemplateVersionsGetSamples {
    /*
     * x-ms-original-file: 2026-11-01/ScenarioTemplateVersions_Get.json
     */
    /**
     * Sample code: Get a specific version of a built-in scenario template.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void
        getASpecificVersionOfABuiltInScenarioTemplate(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.scenarioTemplateVersions().getWithResponse("ComputeZoneDown", "1.2", com.azure.core.util.Context.NONE);
    }
}
```

### ScenarioTemplateVersions_List

```java
/**
 * Samples for ScenarioTemplateVersions List.
 */
public final class ScenarioTemplateVersionsListSamples {
    /*
     * x-ms-original-file: 2026-11-01/ScenarioTemplateVersions_List.json
     */
    /**
     * Sample code: List the published versions of a built-in scenario template.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void
        listThePublishedVersionsOfABuiltInScenarioTemplate(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.scenarioTemplateVersions().list("ComputeZoneDown", com.azure.core.util.Context.NONE);
    }
}
```

### ScenarioTemplates_Get

```java
/**
 * Samples for ScenarioTemplates Get.
 */
public final class ScenarioTemplatesGetSamples {
    /*
     * x-ms-original-file: 2026-11-01/ScenarioTemplates_Get.json
     */
    /**
     * Sample code: Get the latest version of a built-in scenario template.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void
        getTheLatestVersionOfABuiltInScenarioTemplate(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.scenarioTemplates().getWithResponse("ComputeZoneDown", com.azure.core.util.Context.NONE);
    }
}
```

### ScenarioTemplates_List

```java
/**
 * Samples for ScenarioTemplates List.
 */
public final class ScenarioTemplatesListSamples {
    /*
     * x-ms-original-file: 2026-11-01/ScenarioTemplates_List.json
     */
    /**
     * Sample code: List the built-in scenario templates.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void listTheBuiltInScenarioTemplates(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.scenarioTemplates().list(com.azure.core.util.Context.NONE);
    }
}
```

### Scenarios_CreateOrUpdate

```java
import com.azure.resourcemanager.chaos.models.ActionDependency;
import com.azure.resourcemanager.chaos.models.ActionDependencyType;
import com.azure.resourcemanager.chaos.models.ActionLifecycle;
import com.azure.resourcemanager.chaos.models.ExternalResource;
import com.azure.resourcemanager.chaos.models.FailureBehavior;
import com.azure.resourcemanager.chaos.models.KeyValuePair;
import com.azure.resourcemanager.chaos.models.ParameterType;
import com.azure.resourcemanager.chaos.models.RunAfter;
import com.azure.resourcemanager.chaos.models.RunAfterBehavior;
import com.azure.resourcemanager.chaos.models.Scenario;
import com.azure.resourcemanager.chaos.models.ScenarioAction;
import com.azure.resourcemanager.chaos.models.ScenarioActionSelector;
import com.azure.resourcemanager.chaos.models.ScenarioActionSelectorName;
import com.azure.resourcemanager.chaos.models.ScenarioParameter;
import com.azure.resourcemanager.chaos.models.ScenarioProperties;
import java.util.Arrays;

/**
 * Samples for Scenarios CreateOrUpdate.
 */
public final class ScenariosCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-11-01/Scenarios_CreateOrUpdate_With_Failure_Behavior.json
     */
    /**
     * Sample code: Create or update a scenario that continues past suppressed action failures.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void createOrUpdateAScenarioThatContinuesPastSuppressedActionFailures(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.scenarios()
            .define("bestEffortProbeScenario")
            .withExistingWorkspace("exampleRG", "exampleWorkspace")
            .withProperties(new ScenarioProperties()
                .withDescription(
                    "Probes dependencies best-effort, then runs a critical shutdown whose failure must stop the run.")
                .withActions(Arrays.asList(new ScenarioAction().withName("dependencyProbe")
                    .withActionId("urn:csci:microsoft:agent:networkDisconnect/1.0.0")
                    .withDescription(
                        "Best-effort dependency probe. Inherits the scenario-level Continue behavior, so a failure here does not fail the run.")
                    .withDuration("PT5M"),
                    new ScenarioAction().withName("criticalShutdown")
                        .withActionId("urn:csci:microsoft:compute:shutdown/1.0.0")
                        .withDescription(
                            "Critical shutdown. Overrides the scenario default so that a failure stops the run.")
                        .withDuration("PT10M")
                        .withRunAfter(new RunAfter().withBehavior(RunAfterBehavior.ALL)
                            .withItems(Arrays.asList(new ActionDependency().withType(ActionDependencyType.ACTION)
                                .withName("dependencyProbe")
                                .withOnActionLifecycle(ActionLifecycle.ANY_TERMINAL))))
                        .withFailureBehavior(FailureBehavior.STOP)))
                .withFailureBehavior(FailureBehavior.CONTINUE))
            .create();
    }

    /*
     * x-ms-original-file: 2026-11-01/Scenarios_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update a scenario.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void createOrUpdateAScenario(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.scenarios()
            .define("zoneDownScenario")
            .withExistingWorkspace("exampleRG", "exampleWorkspace")
            .withProperties(new ScenarioProperties().withDescription(
                "Induces an outage of all discovered VM and VMSS instances in the target zone with an option to invoke custom scripted actions using Automation Runbooks. Additionally, it forces failover of discovered Redis instances to simulate backend zonal outage.")
                .withParameters(Arrays.asList(
                    new ScenarioParameter().withName("logicalZone")
                        .withType(ParameterType.STRING)
                        .withRequired(true)
                        .withDescription(
                            "Logical availability-zone label to test in each target subscription, for example 1."),
                    new ScenarioParameter().withName("location")
                        .withType(ParameterType.STRING)
                        .withRequired(true)
                        .withDescription("Azure location name in which to test the logical zone, for example eastus."),
                    new ScenarioParameter().withName("duration")
                        .withType(ParameterType.STRING)
                        .withDefaultProperty("PT15M")
                        .withRequired(false)
                        .withDescription("The duration of the outage scenario."),
                    new ScenarioParameter().withName("customRunbook1ResourceId")
                        .withType(ParameterType.STRING)
                        .withRequired(false)
                        .withDescription(
                            "Optional custom runbook 1 resource ID. If not provided, this action will be skipped."),
                    new ScenarioParameter().withName("customRunbook1ParametersJson")
                        .withType(ParameterType.STRING)
                        .withDefaultProperty("{}")
                        .withRequired(false)
                        .withDescription("Optional custom runbook 1 parameters in JSON format.")))
                .withActions(Arrays.asList(
                    new ScenarioAction().withName("vmssZoneDown")
                        .withActionId("microsoft-compute-shutdown/2.0")
                        .withDescription("Force shutdown VMSS instances in target zone")
                        .withDuration("%%{parameters.duration}%%")
                        .withParameters(Arrays.asList())
                        .withSelectors(Arrays.asList(
                            new ScenarioActionSelector().withName(ScenarioActionSelectorName.LOGICAL_ZONE)
                                .withValue("%%{parameters.logicalZone}%%"),
                            new ScenarioActionSelector().withName(ScenarioActionSelectorName.LOCATION)
                                .withValue("%%{parameters.location}%%"))),
                    new ScenarioAction().withName("vmZoneDown")
                        .withActionId("microsoft-compute-shutdown/2.0")
                        .withDescription("Force shutdown VM instances in target zone")
                        .withDuration("%%{parameters.duration}%%")
                        .withParameters(Arrays.asList())
                        .withSelectors(Arrays.asList(
                            new ScenarioActionSelector().withName(ScenarioActionSelectorName.LOGICAL_ZONE)
                                .withValue("%%{parameters.logicalZone}%%"),
                            new ScenarioActionSelector().withName(ScenarioActionSelectorName.LOCATION)
                                .withValue("%%{parameters.location}%%"))),
                    new ScenarioAction().withName("redisFailover")
                        .withActionId("urn:csci:microsoft:azureClusteredCacheForRedis:Reboot/1.0.0")
                        .withDescription("Force failover of Redis instances to simulate backend zonal outage")
                        .withDuration("PT5M")
                        .withParameters(
                            Arrays.asList(new KeyValuePair().withKey("fakeTokenPlaceholder").withValue("PrimaryNode"))),
                    new ScenarioAction().withName("custom-runbook-1")
                        .withActionId("urn:csci:microsoft:Automation:StartRunbook/1.0.0")
                        .withDescription("Custom Runbook 1")
                        .withDuration("PT30M")
                        .withParameters(Arrays.asList(new KeyValuePair().withKey("fakeTokenPlaceholder")
                            .withValue("%%{parameters.customRunbook1ParametersJson}%%")))
                        .withExternalResource(new ExternalResource().withResourceId(
                            "/subscriptions/6b052e15-03d3-4f17-b2e1-be7f07588291/resourceGroups/exampleRG/providers/Microsoft.Automation/automationAccounts/exampleAutomationAccount/runbooks/exampleRunbook")))))
            .create();
    }

    /*
     * x-ms-original-file: 2026-11-01/Scenarios_CreateOrUpdate_CreateOnly.json
     */
    /**
     * Sample code: Create a scenario only when absent, or return 412 when it already exists.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void createAScenarioOnlyWhenAbsentOrReturn412WhenItAlreadyExists(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.scenarios()
            .define("exampleScenario")
            .withExistingWorkspace("exampleRG", "exampleWorkspace")
            .withProperties(new ScenarioProperties().withParameters(Arrays.asList())
                .withActions(Arrays.asList(new ScenarioAction().withName("restart")
                    .withActionId("microsoft-compute-shutdown/2.0")
                    .withDuration("PT5M")
                    .withSelectors(Arrays.asList(
                        new ScenarioActionSelector().withName(ScenarioActionSelectorName.LOGICAL_ZONE).withValue("1"),
                        new ScenarioActionSelector().withName(ScenarioActionSelectorName.LOCATION)
                            .withValue("eastus"))))))
            .withIfNoneMatch("*")
            .create();
    }

    /*
     * x-ms-original-file: 2026-11-01/Scenarios_CreateOrUpdate_From_Template.json
     */
    /**
     * Sample code: Create a scenario from a built-in scenario template version.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void
        createAScenarioFromABuiltInScenarioTemplateVersion(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.scenarios()
            .define("computeZoneDownScenario")
            .withExistingWorkspace("exampleRG", "exampleWorkspace")
            .withProperties(new ScenarioProperties()
                .withCreatedFrom("/providers/Microsoft.Chaos/scenarioTemplates/ComputeZoneDown/versions/1.2")
                .withDescription(
                    "Simulates a datacenter/availability zone failure by shutting down VMSS instances and standalone VMs in the target zone. Zone failures account for ~25% of all cloud incidents — this scenario validates your application's cross-zone resilience."))
            .create();
    }

    /*
     * x-ms-original-file: 2026-11-01/Scenarios_CreateOrUpdate_UpdateOnly.json
     */
    /**
     * Sample code: Update a scenario only when present, or return 412 when it does not exist.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void updateAScenarioOnlyWhenPresentOrReturn412WhenItDoesNotExist(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        Scenario resource = manager.scenarios()
            .getWithResponse("exampleRG", "exampleWorkspace", "exampleScenario", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new ScenarioProperties().withParameters(Arrays.asList())
                .withActions(Arrays.asList(new ScenarioAction().withName("restart")
                    .withActionId("microsoft-compute-shutdown/2.0")
                    .withDuration("PT5M")
                    .withSelectors(Arrays.asList(
                        new ScenarioActionSelector().withName(ScenarioActionSelectorName.LOGICAL_ZONE).withValue("1"),
                        new ScenarioActionSelector().withName(ScenarioActionSelectorName.LOCATION)
                            .withValue("eastus"))))))
            .withIfMatch("*")
            .apply();
    }
}
```

### Scenarios_Delete

```java
/**
 * Samples for Scenarios Delete.
 */
public final class ScenariosDeleteSamples {
    /*
     * x-ms-original-file: 2026-11-01/Scenarios_Delete.json
     */
    /**
     * Sample code: Delete a Scenario in a workspace.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void deleteAScenarioInAWorkspace(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.scenarios()
            .deleteWithResponse("exampleRG", "exampleWorkspace", "myVMShutdownScenario",
                com.azure.core.util.Context.NONE);
    }
}
```

### Scenarios_Get

```java
/**
 * Samples for Scenarios Get.
 */
public final class ScenariosGetSamples {
    /*
     * x-ms-original-file: 2026-11-01/Scenarios_Get.json
     */
    /**
     * Sample code: Get a scenario.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getAScenario(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.scenarios()
            .getWithResponse("exampleRG", "exampleWorkspace", "zoneDownScenario", com.azure.core.util.Context.NONE);
    }
}
```

### Scenarios_ListAll

```java
/**
 * Samples for Scenarios ListAll.
 */
public final class ScenariosListAllSamples {
    /*
     * x-ms-original-file: 2026-11-01/Scenarios_ListAll.json
     */
    /**
     * Sample code: Get a list of scenarios.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getAListOfScenarios(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.scenarios().listAll("exampleRG", "exampleWorkspace", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-11-01/TargetTypes_Get.json
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
     * x-ms-original-file: 2026-11-01/TargetTypes_List.json
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
     * x-ms-original-file: 2026-11-01/Targets_CreateOrUpdate.json
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
     * x-ms-original-file: 2026-11-01/Targets_Delete.json
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
     * x-ms-original-file: 2026-11-01/Targets_Get.json
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
     * x-ms-original-file: 2026-11-01/Targets_List.json
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

### WorkspaceDiscoveries_Get

```java
/**
 * Samples for WorkspaceDiscoveries Get.
 */
public final class WorkspaceDiscoveriesGetSamples {
    /*
     * x-ms-original-file: 2026-11-01/WorkspaceDiscoveries_Get_Established.json
     */
    /**
     * Sample code: Get an established discovery result with a known resource population.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getAnEstablishedDiscoveryResultWithAKnownResourcePopulation(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaceDiscoveries()
            .getWithResponse("exampleRG", "exampleWorkspace", "00000000-0000-0000-0000-000000000002",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-11-01/WorkspaceDiscoveries_Get_Latest.json
     */
    /**
     * Sample code: Read latest as an alias while returning the canonical discovery identity.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void readLatestAsAnAliasWhileReturningTheCanonicalDiscoveryIdentity(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaceDiscoveries()
            .getWithResponse("exampleRG", "exampleWorkspace", "latest", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-11-01/WorkspaceDiscoveries_Get_InProgress.json
     */
    /**
     * Sample code: Read a running discovery with HTTP 200 without inventing accepted coverage.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void readARunningDiscoveryWithHTTP200WithoutInventingAcceptedCoverage(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaceDiscoveries()
            .getWithResponse("exampleRG", "exampleWorkspace", "00000000-0000-0000-0000-000000000009",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-11-01/WorkspaceDiscoveries_Get_PartialMixed.json
     */
    /**
     * Sample code: Get a partially succeeded result with classified guidance, system uncertainty, and a truncated
     * warning sample.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getAPartiallySucceededResultWithClassifiedGuidanceSystemUncertaintyAndATruncatedWarningSample(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaceDiscoveries()
            .getWithResponse("exampleRG", "exampleWorkspace", "00000000-0000-0000-0000-000000000008",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-11-01/WorkspaceDiscoveries_Get_Failed.json
     */
    /**
     * Sample code: Get a terminal failed discovery without an accepted inventory result.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getATerminalFailedDiscoveryWithoutAnAcceptedInventoryResult(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaceDiscoveries()
            .getWithResponse("exampleRG", "exampleWorkspace", "00000000-0000-0000-0000-000000000005",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-11-01/WorkspaceDiscoveries_Get_KnownEmpty.json
     */
    /**
     * Sample code: Get an established discovery result with an authoritatively empty expected set.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getAnEstablishedDiscoveryResultWithAnAuthoritativelyEmptyExpectedSet(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaceDiscoveries()
            .getWithResponse("exampleRG", "exampleWorkspace", "00000000-0000-0000-0000-000000000007",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-11-01/WorkspaceDiscoveries_Get_BaselineUnknown.json
     */
    /**
     * Sample code: Get a healthy container scan with a structurally unknown inventory denominator.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getAHealthyContainerScanWithAStructurallyUnknownInventoryDenominator(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaceDiscoveries()
            .getWithResponse("exampleRG", "exampleWorkspace", "00000000-0000-0000-0000-000000000006",
                com.azure.core.util.Context.NONE);
    }
}
```

### WorkspaceDiscoveries_List

```java
/**
 * Samples for WorkspaceDiscoveries List.
 */
public final class WorkspaceDiscoveriesListSamples {
    /*
     * x-ms-original-file: 2026-11-01/WorkspaceDiscoveries_List.json
     */
    /**
     * Sample code: List retained workspace discoveries using canonical IDs and an opaque nextLink.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void listRetainedWorkspaceDiscoveriesUsingCanonicalIDsAndAnOpaqueNextLink(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaceDiscoveries().list("exampleRG", "exampleWorkspace", null, com.azure.core.util.Context.NONE);
    }
}
```

### WorkspaceEvaluations_Get

```java
/**
 * Samples for WorkspaceEvaluations Get.
 */
public final class WorkspaceEvaluationsGetSamples {
    /*
     * x-ms-original-file: 2026-11-01/WorkspaceEvaluations_Get_Latest.json
     */
    /**
     * Sample code: Read latest as an alias while returning the canonical GA evaluation identity.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void readLatestAsAnAliasWhileReturningTheCanonicalGAEvaluationIdentity(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaceEvaluations()
            .getWithResponse("exampleRG", "exampleWorkspace", "latest", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-11-01/WorkspaceEvaluations_Get_EmptyCatalog.json
     */
    /**
     * Sample code: A valid empty published catalog succeeds with zero counts and no aggregate recommendation.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void aValidEmptyPublishedCatalogSucceedsWithZeroCountsAndNoAggregateRecommendation(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaceEvaluations()
            .getWithResponse("exampleRG", "exampleWorkspace", "00000000-0000-0000-0000-000000000003",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-11-01/WorkspaceEvaluations_Get_InProgress.json
     */
    /**
     * Sample code: Read a InProgress template evaluation independently of workspace scenarios.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void readAInProgressTemplateEvaluationIndependentlyOfWorkspaceScenarios(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaceEvaluations()
            .getWithResponse("exampleRG", "exampleWorkspace", "00000000-0000-0000-0000-000000000003",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-11-01/WorkspaceEvaluations_Get_Succeeded.json
     */
    /**
     * Sample code: Read a Succeeded template evaluation independently of workspace scenarios.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void readASucceededTemplateEvaluationIndependentlyOfWorkspaceScenarios(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaceEvaluations()
            .getWithResponse("exampleRG", "exampleWorkspace", "00000000-0000-0000-0000-000000000003",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-11-01/WorkspaceEvaluations_Get_Failed.json
     */
    /**
     * Sample code: Read a Failed template evaluation independently of workspace scenarios.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void readAFailedTemplateEvaluationIndependentlyOfWorkspaceScenarios(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaceEvaluations()
            .getWithResponse("exampleRG", "exampleWorkspace", "00000000-0000-0000-0000-000000000003",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-11-01/WorkspaceEvaluations_Get_Canceled.json
     */
    /**
     * Sample code: Read a Canceled template evaluation independently of workspace scenarios.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void readACanceledTemplateEvaluationIndependentlyOfWorkspaceScenarios(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaceEvaluations()
            .getWithResponse("exampleRG", "exampleWorkspace", "00000000-0000-0000-0000-000000000003",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-11-01/WorkspaceEvaluations_Get_PartiallySucceeded.json
     */
    /**
     * Sample code: Read a PartiallySucceeded template evaluation independently of workspace scenarios.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void readAPartiallySucceededTemplateEvaluationIndependentlyOfWorkspaceScenarios(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaceEvaluations()
            .getWithResponse("exampleRG", "exampleWorkspace", "00000000-0000-0000-0000-000000000003",
                com.azure.core.util.Context.NONE);
    }
}
```

### WorkspaceEvaluations_List

```java
/**
 * Samples for WorkspaceEvaluations List.
 */
public final class WorkspaceEvaluationsListSamples {
    /*
     * x-ms-original-file: 2026-11-01/WorkspaceEvaluations_List.json
     */
    /**
     * Sample code: List retained workspace evaluations using canonical IDs and an opaque nextLink.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void listRetainedWorkspaceEvaluationsUsingCanonicalIDsAndAnOpaqueNextLink(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaceEvaluations().list("exampleRG", "exampleWorkspace", null, com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_CreateOrUpdate

```java
import com.azure.resourcemanager.chaos.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.chaos.models.ResourceIdentity;
import com.azure.resourcemanager.chaos.models.UserAssignedIdentity;
import com.azure.resourcemanager.chaos.models.WorkspaceCustomerManagedKeyEncryption;
import com.azure.resourcemanager.chaos.models.WorkspaceEncryption;
import com.azure.resourcemanager.chaos.models.WorkspaceProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Workspaces CreateOrUpdate.
 */
public final class WorkspacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-11-01/Workspaces_CreateOrUpdate.json
     */
    /**
     * Sample code: Create/update a workspace in a resource group.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void createUpdateAWorkspaceInAResourceGroup(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaces()
            .define("exampleWorkspace")
            .withRegion("westus2")
            .withExistingResourceGroup("exampleRG")
            .withProperties(new WorkspaceProperties().withScopes(Arrays
                .asList("/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/exampleResourceGroup")))
            .withTags(mapOf("environment", "production", "department", "engineering", "project", "chaos-testing"))
            .withIdentity(new ResourceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/exampleResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/exampleIdentity",
                    new UserAssignedIdentity())))
            .create();
    }

    /*
     * x-ms-original-file: 2026-11-01/Workspaces_CreateOrUpdate_WithCustomerKey.json
     */
    /**
     * Sample code: Create or update a Workspace with a customer-managed key.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void
        createOrUpdateAWorkspaceWithACustomerManagedKey(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaces()
            .define("exampleWorkspace")
            .withRegion("westus2")
            .withExistingResourceGroup("exampleRG")
            .withProperties(new WorkspaceProperties()
                .withScopes(
                    Arrays.asList("/subscriptions/6b052e15-03d3-4f17-b2e1-be7f07588291/resourceGroups/exampleRG"))
                .withEncryption(new WorkspaceEncryption().withCustomerManagedKeyEncryption(
                    new WorkspaceCustomerManagedKeyEncryption().withKeyEncryptionKeyUrl("fakeTokenPlaceholder")
                        .withKeyVaultResourceId("fakeTokenPlaceholder"))))
            .create();
    }

    /*
     * x-ms-original-file: 2026-11-01/Workspaces_CreateOrUpdate_EmptyEncryption.json
     */
    /**
     * Sample code: Create or update a Microsoft-managed Workspace with empty encryption settings.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void createOrUpdateAMicrosoftManagedWorkspaceWithEmptyEncryptionSettings(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaces()
            .define("exampleWorkspace")
            .withRegion("westus2")
            .withExistingResourceGroup("exampleRG")
            .withProperties(new WorkspaceProperties()
                .withScopes(
                    Arrays.asList("/subscriptions/6b052e15-03d3-4f17-b2e1-be7f07588291/resourceGroups/exampleRG"))
                .withEncryption(new WorkspaceEncryption()))
            .create();
    }

    /*
     * x-ms-original-file: 2026-11-01/Workspaces_CreateOrUpdate_OmitEncryption.json
     */
    /**
     * Sample code: Create or update a Microsoft-managed Workspace without encryption settings.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void createOrUpdateAMicrosoftManagedWorkspaceWithoutEncryptionSettings(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaces()
            .define("exampleWorkspace")
            .withRegion("westus2")
            .withExistingResourceGroup("exampleRG")
            .withProperties(new WorkspaceProperties().withScopes(
                Arrays.asList("/subscriptions/6b052e15-03d3-4f17-b2e1-be7f07588291/resourceGroups/exampleRG")))
            .create();
    }

    /*
     * x-ms-original-file: 2026-11-01/Workspaces_CreateOrUpdate_CreateOnly.json
     */
    /**
     * Sample code: Create a workspace only when absent, or return 412 when it already exists.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void createAWorkspaceOnlyWhenAbsentOrReturn412WhenItAlreadyExists(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaces()
            .define("exampleWorkspace")
            .withRegion("westus2")
            .withExistingResourceGroup("exampleRG")
            .withProperties(new WorkspaceProperties().withScopes(
                Arrays.asList("/subscriptions/6b052e15-03d3-4f17-b2e1-be7f07588291/resourceGroups/exampleRG")))
            .withIfNoneMatch("*")
            .create();
    }

    /*
     * x-ms-original-file: 2026-11-01/Workspaces_CreateOrUpdate_UpdateOnly.json
     */
    /**
     * Sample code: Update a workspace only when present, or return 412 when it does not exist.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void updateAWorkspaceOnlyWhenPresentOrReturn412WhenItDoesNotExist(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaces()
            .define("exampleWorkspace")
            .withRegion("westus2")
            .withExistingResourceGroup("exampleRG")
            .withProperties(new WorkspaceProperties().withScopes(
                Arrays.asList("/subscriptions/6b052e15-03d3-4f17-b2e1-be7f07588291/resourceGroups/exampleRG")))
            .withIfMatch("*")
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

### Workspaces_Delete

```java
/**
 * Samples for Workspaces Delete.
 */
public final class WorkspacesDeleteSamples {
    /*
     * x-ms-original-file: 2026-11-01/Workspaces_Delete.json
     */
    /**
     * Sample code: Delete a Workspace in a resource group.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void deleteAWorkspaceInAResourceGroup(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaces().delete("exampleRG", "exampleWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_Discover

```java
/**
 * Samples for Workspaces Discover.
 */
public final class WorkspacesDiscoverSamples {
    /*
     * x-ms-original-file: 2026-11-01/Workspaces_Discover.json
     */
    /**
     * Sample code: Trigger resource discovery for the workspace.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void triggerResourceDiscoveryForTheWorkspace(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaces().discover("exampleRG", "exampleWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_Evaluate

```java
/**
 * Samples for Workspaces Evaluate.
 */
public final class WorkspacesEvaluateSamples {
    /*
     * x-ms-original-file: 2026-11-01/Workspaces_Evaluate.json
     */
    /**
     * Sample code: Start evaluation of published scenario-template versions.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void
        startEvaluationOfPublishedScenarioTemplateVersions(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaces().evaluate("exampleRG", "exampleWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_GetByResourceGroup

```java
/**
 * Samples for Workspaces GetByResourceGroup.
 */
public final class WorkspacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-11-01/Workspaces_Get_EncryptionPending.json
     */
    /**
     * Sample code: Get a CMK-created Workspace while its customer key is being replaced.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getACMKCreatedWorkspaceWhileItsCustomerKeyIsBeingReplaced(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaces()
            .getByResourceGroupWithResponse("exampleRG", "exampleWorkspace", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-11-01/Workspaces_Get.json
     */
    /**
     * Sample code: Get a Workspace in a resource group.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getAWorkspaceInAResourceGroup(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaces()
            .getByResourceGroupWithResponse("exampleRG", "exampleWorkspace", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-11-01/Workspaces_Get_EncryptionUnknown.json
     */
    /**
     * Sample code: Get a Workspace when active encryption protection is unknown.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void
        getAWorkspaceWhenActiveEncryptionProtectionIsUnknown(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaces()
            .getByResourceGroupWithResponse("exampleRG", "exampleWorkspace", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-11-01/Workspaces_Get_EncryptionFailed.json
     */
    /**
     * Sample code: Get a Workspace after a customer key replacement fails.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void
        getAWorkspaceAfterACustomerKeyReplacementFails(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaces()
            .getByResourceGroupWithResponse("exampleRG", "exampleWorkspace", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-11-01/Workspaces_Get_EncryptionNotConfigured.json
     */
    /**
     * Sample code: Get a Workspace service application before customer key enrollment.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getAWorkspaceServiceApplicationBeforeCustomerKeyEnrollment(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaces()
            .getByResourceGroupWithResponse("exampleRG", "exampleWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_List

```java
/**
 * Samples for Workspaces List.
 */
public final class WorkspacesListSamples {
    /*
     * x-ms-original-file: 2026-11-01/Workspaces_ListAll.json
     */
    /**
     * Sample code: List all Workspaces in a subscription.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void listAllWorkspacesInASubscription(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaces().list(null, com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_ListByResourceGroup

```java
/**
 * Samples for Workspaces ListByResourceGroup.
 */
public final class WorkspacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-11-01/Workspaces_List_EncryptionPending.json
     */
    /**
     * Sample code: List CMK-created Workspaces with customer key replacement pending.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void listCMKCreatedWorkspacesWithCustomerKeyReplacementPending(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaces().listByResourceGroup("exampleRG", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-11-01/Workspaces_List.json
     */
    /**
     * Sample code: List all Workspaces in a resource group.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void listAllWorkspacesInAResourceGroup(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaces().listByResourceGroup("exampleRG", null, com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_Update

```java
import com.azure.resourcemanager.chaos.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.chaos.models.ResourceIdentity;
import com.azure.resourcemanager.chaos.models.UserAssignedIdentity;
import com.azure.resourcemanager.chaos.models.Workspace;
import com.azure.resourcemanager.chaos.models.WorkspaceCustomerManagedKeyEncryptionPatchMergePatchUpdateOrCreate;
import com.azure.resourcemanager.chaos.models.WorkspaceEncryptionPatchMergePatchUpdateOrCreate;
import com.azure.resourcemanager.chaos.models.WorkspaceUpdateProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Workspaces Update.
 */
public final class WorkspacesUpdateSamples {
    /*
     * x-ms-original-file: 2026-11-01/Workspaces_Update_ChangeKeyVault.json
     */
    /**
     * Sample code: Update a Workspace with a validated key and resource ID in another vault.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void updateAWorkspaceWithAValidatedKeyAndResourceIDInAnotherVault(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        Workspace resource = manager.workspaces()
            .getByResourceGroupWithResponse("exampleRG", "exampleWorkspace", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new WorkspaceUpdateProperties()
                .withEncryption(new WorkspaceEncryptionPatchMergePatchUpdateOrCreate().withCustomerManagedKeyEncryption(
                    new WorkspaceCustomerManagedKeyEncryptionPatchMergePatchUpdateOrCreate()
                        .withKeyEncryptionKeyUrl("fakeTokenPlaceholder")
                        .withKeyVaultResourceId("fakeTokenPlaceholder"))))
            .apply();
    }

    /*
     * x-ms-original-file: 2026-11-01/Workspaces_Update_ReplaceCustomerKey.json
     */
    /**
     * Sample code: Update only the customer key URI of an encrypted Workspace.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void
        updateOnlyTheCustomerKeyURIOfAnEncryptedWorkspace(com.azure.resourcemanager.chaos.ChaosManager manager) {
        Workspace resource = manager.workspaces()
            .getByResourceGroupWithResponse("exampleRG", "exampleWorkspace", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new WorkspaceUpdateProperties()
                .withEncryption(new WorkspaceEncryptionPatchMergePatchUpdateOrCreate().withCustomerManagedKeyEncryption(
                    new WorkspaceCustomerManagedKeyEncryptionPatchMergePatchUpdateOrCreate()
                        .withKeyEncryptionKeyUrl("fakeTokenPlaceholder"))))
            .apply();
    }

    /*
     * x-ms-original-file: 2026-11-01/Workspaces_Update_EmptyCustomerKeyPatch.json
     */
    /**
     * Sample code: Update a Workspace with an empty CMK patch and retain its existing key URL.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void updateAWorkspaceWithAnEmptyCMKPatchAndRetainItsExistingKeyURL(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        Workspace resource = manager.workspaces()
            .getByResourceGroupWithResponse("exampleRG", "exampleWorkspace", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new WorkspaceUpdateProperties()
                .withEncryption(new WorkspaceEncryptionPatchMergePatchUpdateOrCreate().withCustomerManagedKeyEncryption(
                    new WorkspaceCustomerManagedKeyEncryptionPatchMergePatchUpdateOrCreate())))
            .apply();
    }

    /*
     * x-ms-original-file: 2026-11-01/Workspaces_Update.json
     */
    /**
     * Sample code: Update a Workspace in a resource group.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void updateAWorkspaceInAResourceGroup(com.azure.resourcemanager.chaos.ChaosManager manager) {
        Workspace resource = manager.workspaces()
            .getByResourceGroupWithResponse("exampleRG", "exampleWorkspace", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("environment", "production", "department", "engineering", "project", "chaos-testing"))
            .withIdentity(new ResourceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/exampleResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/exampleIdentity",
                    new UserAssignedIdentity())))
            .apply();
    }

    /*
     * x-ms-original-file: 2026-11-01/Workspaces_Update_OmitEncryption.json
     */
    /**
     * Sample code: Update Workspace tags without changing customer-managed encryption.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void updateWorkspaceTagsWithoutChangingCustomerManagedEncryption(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        Workspace resource = manager.workspaces()
            .getByResourceGroupWithResponse("exampleRG", "exampleWorkspace", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("environment", "production")).apply();
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

