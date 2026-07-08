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

## PrivateAccesses

- [CreateOrUpdate](#privateaccesses_createorupdate)
- [Delete](#privateaccesses_delete)
- [DeleteAPrivateEndpointConnection](#privateaccesses_deleteaprivateendpointconnection)
- [GetAPrivateEndpointConnection](#privateaccesses_getaprivateendpointconnection)
- [GetByResourceGroup](#privateaccesses_getbyresourcegroup)
- [GetPrivateLinkResources](#privateaccesses_getprivatelinkresources)
- [List](#privateaccesses_list)
- [ListByResourceGroup](#privateaccesses_listbyresourcegroup)
- [ListPrivateEndpointConnections](#privateaccesses_listprivateendpointconnections)
- [Update](#privateaccesses_update)

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

## Workspaces

- [CreateOrUpdate](#workspaces_createorupdate)
- [Delete](#workspaces_delete)
- [GetByResourceGroup](#workspaces_getbyresourcegroup)
- [List](#workspaces_list)
- [ListByResourceGroup](#workspaces_listbyresourcegroup)
- [RefreshRecommendations](#workspaces_refreshrecommendations)
- [Update](#workspaces_update)
### ActionVersions_Get

```java
/**
 * Samples for ActionVersions Get.
 */
public final class ActionVersionsGetSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/ActionVersions_Get.json
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
     * x-ms-original-file: 2026-05-01-preview/ActionVersions_List.json
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
     * x-ms-original-file: 2026-05-01-preview/Actions_Get.json
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
     * x-ms-original-file: 2026-05-01-preview/Actions_List.json
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
     * x-ms-original-file: 2026-05-01-preview/Capabilities_CreateOrUpdate.json
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
     * x-ms-original-file: 2026-05-01-preview/Capabilities_Delete.json
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
     * x-ms-original-file: 2026-05-01-preview/Capabilities_Get.json
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
     * x-ms-original-file: 2026-05-01-preview/Capabilities_List.json
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
     * x-ms-original-file: 2026-05-01-preview/CapabilityTypes_Get.json
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
     * x-ms-original-file: 2026-05-01-preview/CapabilityTypes_List.json
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

### DiscoveredResources_Get

```java
/**
 * Samples for DiscoveredResources Get.
 */
public final class DiscoveredResourcesGetSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/DiscoveredResources_Get.json
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
     * x-ms-original-file: 2026-05-01-preview/DiscoveredResources_ListByWorkspace.json
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
     * x-ms-original-file: 2026-05-01-preview/Experiments_Cancel.json
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
     * x-ms-original-file: 2026-05-01-preview/Experiments_CreateOrUpdate.json
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
     * x-ms-original-file: 2026-05-01-preview/Experiments_Delete.json
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
     * x-ms-original-file: 2026-05-01-preview/Experiments_ExecutionDetails.json
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
     * x-ms-original-file: 2026-05-01-preview/Experiments_Get.json
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
     * x-ms-original-file: 2026-05-01-preview/Experiments_GetExecution.json
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
     * x-ms-original-file: 2026-05-01-preview/Experiments_ListAll.json
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
     * x-ms-original-file: 2026-05-01-preview/Experiments_ListAllExecutions.json
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
     * x-ms-original-file: 2026-05-01-preview/Experiments_List.json
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
     * x-ms-original-file: 2026-05-01-preview/Experiments_Start.json
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
     * x-ms-original-file: 2026-05-01-preview/Experiments_Update.json
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
     * x-ms-original-file: 2026-05-01-preview/OperationStatuses_Get.json
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

### PrivateAccesses_CreateOrUpdate

```java
import com.azure.resourcemanager.chaos.models.PrivateAccessProperties;
import com.azure.resourcemanager.chaos.models.PublicNetworkAccessOption;

/**
 * Samples for PrivateAccesses CreateOrUpdate.
 */
public final class PrivateAccessesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * 2026-05-01-preview/PrivateAccesses_CreateOrUpdate_Create_Or_Update_A_Private_Access_Resource.json
     */
    /**
     * Sample code: Create or Update a private access resource.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void createOrUpdateAPrivateAccessResource(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.privateAccesses()
            .define("myPrivateAccess")
            .withRegion("centraluseuap")
            .withExistingResourceGroup("myResourceGroup")
            .withProperties(new PrivateAccessProperties())
            .create();
    }

    /*
     * x-ms-original-file: 2026-05-01-preview/
     * PrivateAccesses_CreateOrUpdate_Create_Or_Update_A_Private_Access_Resource_With_Public_Network_Access.json
     */
    /**
     * Sample code: Create or Update a private access resource with publicNetworkAccess.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void createOrUpdateAPrivateAccessResourceWithPublicNetworkAccess(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.privateAccesses()
            .define("myPrivateAccess")
            .withRegion("centraluseuap")
            .withExistingResourceGroup("myResourceGroup")
            .withProperties(new PrivateAccessProperties().withPublicNetworkAccess(PublicNetworkAccessOption.ENABLED))
            .create();
    }
}
```

### PrivateAccesses_Delete

```java
/**
 * Samples for PrivateAccesses Delete.
 */
public final class PrivateAccessesDeleteSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/PrivateAccesses_Delete.json
     */
    /**
     * Sample code: Delete a private access resource.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void deleteAPrivateAccessResource(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.privateAccesses().delete("myResourceGroup", "myPrivateAccess", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateAccesses_DeleteAPrivateEndpointConnection

```java
/**
 * Samples for PrivateAccesses DeleteAPrivateEndpointConnection.
 */
public final class PrivateAccessesDeleteAPrivateEndpointConnectionSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/PrivateAccesses_DeleteAPrivateEndpointConnection.json
     */
    /**
     * Sample code: Delete a private endpoint connection under a private access resource.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void deleteAPrivateEndpointConnectionUnderAPrivateAccessResource(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.privateAccesses()
            .deleteAPrivateEndpointConnection("myResourceGroup", "myPrivateAccess", "myPrivateEndpointConnection",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateAccesses_GetAPrivateEndpointConnection

```java
/**
 * Samples for PrivateAccesses GetAPrivateEndpointConnection.
 */
public final class PrivateAccessesGetAPrivateEndpointConnectionSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/PrivateAccesses_GetAPrivateEndpointConnection.json
     */
    /**
     * Sample code: Get information about a private endpoint connection under a private access resource.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getInformationAboutAPrivateEndpointConnectionUnderAPrivateAccessResource(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.privateAccesses()
            .getAPrivateEndpointConnectionWithResponse("myResourceGroup", "myPrivateAccess",
                "myPrivateEndpointConnection", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateAccesses_GetByResourceGroup

```java
/**
 * Samples for PrivateAccesses GetByResourceGroup.
 */
public final class PrivateAccessesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/PrivateAccesses_Get_Get_A_Private_Access_Resource.json
     */
    /**
     * Sample code: Get a private access resource.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void getAPrivateAccessResource(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.privateAccesses()
            .getByResourceGroupWithResponse("myResourceGroup", "myPrivateAccess", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * 2026-05-01-preview/PrivateAccesses_Get_Get_A_Private_Access_Resource_With_Private_Endpoint.json
     */
    /**
     * Sample code: Get a private access resource with private endpoint.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void
        getAPrivateAccessResourceWithPrivateEndpoint(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.privateAccesses()
            .getByResourceGroupWithResponse("myResourceGroup", "myPrivateAccess", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateAccesses_GetPrivateLinkResources

```java
/**
 * Samples for PrivateAccesses GetPrivateLinkResources.
 */
public final class PrivateAccessesGetPrivateLinkResourcesSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/PrivateAccesses_GetPrivateLinkResources.json
     */
    /**
     * Sample code: List all possible private link resources under private access resource.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void listAllPossiblePrivateLinkResourcesUnderPrivateAccessResource(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.privateAccesses()
            .getPrivateLinkResourcesWithResponse("myResourceGroup", "myPrivateAccess",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateAccesses_List

```java
/**
 * Samples for PrivateAccesses List.
 */
public final class PrivateAccessesListSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/PrivateAccesses_ListAll.json
     */
    /**
     * Sample code: List all private accesses in a subscription.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void listAllPrivateAccessesInASubscription(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.privateAccesses().list(null, com.azure.core.util.Context.NONE);
    }
}
```

### PrivateAccesses_ListByResourceGroup

```java
/**
 * Samples for PrivateAccesses ListByResourceGroup.
 */
public final class PrivateAccessesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/PrivateAccesses_List.json
     */
    /**
     * Sample code: List all private access in a resource group.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void listAllPrivateAccessInAResourceGroup(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.privateAccesses().listByResourceGroup("myResourceGroup", null, com.azure.core.util.Context.NONE);
    }
}
```

### PrivateAccesses_ListPrivateEndpointConnections

```java
/**
 * Samples for PrivateAccesses ListPrivateEndpointConnections.
 */
public final class PrivateAccessesListPrivateEndpointConnectionsSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/PrivateAccesses_ListPrivateEndpointConnections.json
     */
    /**
     * Sample code: List all private endpoint connections under a private access resource.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void listAllPrivateEndpointConnectionsUnderAPrivateAccessResource(
        com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.privateAccesses()
            .listPrivateEndpointConnections("myResourceGroup", "myPrivateAccess", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateAccesses_Update

```java
import com.azure.resourcemanager.chaos.models.PrivateAccess;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for PrivateAccesses Update.
 */
public final class PrivateAccessesUpdateSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/PrivateAccesses_Update.json
     */
    /**
     * Sample code: Update a private access resource's tags.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void updateAPrivateAccessResourceSTags(com.azure.resourcemanager.chaos.ChaosManager manager) {
        PrivateAccess resource = manager.privateAccesses()
            .getByResourceGroupWithResponse("myResourceGroup", "myPrivateAccess", com.azure.core.util.Context.NONE)
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

### ScenarioConfigurations_CreateOrUpdate

```java
import com.azure.resourcemanager.chaos.models.ConfigurationExclusions;
import com.azure.resourcemanager.chaos.models.ConfigurationFilters;
import com.azure.resourcemanager.chaos.models.KeyValuePair;
import com.azure.resourcemanager.chaos.models.ScenarioConfigurationProperties;
import java.util.Arrays;

/**
 * Samples for ScenarioConfigurations CreateOrUpdate.
 */
public final class ScenarioConfigurationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/ScenarioConfigurations_CreateOrUpdate_With_Physical_Zones.json
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
                .withExclusions(new ConfigurationExclusions().withResources(Arrays.asList(
                    "/subscriptions/6b052e15-03d3-4f17-b2e1-be7f07588291/resourceGroups/exampleRG/providers/Microsoft.Compute/virtualMachines/protectedVM")))
                .withFilters(new ConfigurationFilters().withLocations(Arrays.asList("westus2"))
                    .withPhysicalZones(Arrays.asList("westus2-az1"))))
            .create();
    }

    /*
     * x-ms-original-file: 2026-05-01-preview/ScenarioConfigurations_CreateOrUpdate.json
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
                .withExclusions(new ConfigurationExclusions().withResources(Arrays.asList(
                    "/subscriptions/6b052e15-03d3-4f17-b2e1-be7f07588291/resourceGroups/exampleRG/providers/Microsoft.Compute/virtualMachines/protectedVM"))
                    .withTags(Arrays.asList(new KeyValuePair().withKey("fakeTokenPlaceholder").withValue("production")))
                    .withTypes(Arrays.asList("Microsoft.Compute/virtualMachineScaleSets")))
                .withFilters(
                    new ConfigurationFilters().withLocations(Arrays.asList("eastus")).withZones(Arrays.asList("1"))))
            .create();
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
     * x-ms-original-file: 2026-05-01-preview/ScenarioConfigurations_Delete.json
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
     * x-ms-original-file: 2026-05-01-preview/ScenarioConfigurations_Execute.json
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
     * x-ms-original-file: 2026-05-01-preview/ScenarioConfigurations_FixResourcePermissions.json
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
     * x-ms-original-file: 2026-05-01-preview/ScenarioConfigurations_Get.json
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
     * x-ms-original-file: 2026-05-01-preview/ScenarioConfigurations_ListAll.json
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
     * x-ms-original-file: 2026-05-01-preview/ScenarioConfigurations_Validate.json
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
     * x-ms-original-file: 2026-05-01-preview/ScenarioRuns_Cancel.json
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
     * x-ms-original-file: 2026-05-01-preview/ScenarioRuns_Get.json
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
     * x-ms-original-file: 2026-05-01-preview/ScenarioRuns_ListAll.json
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

### Scenarios_CreateOrUpdate

```java
import com.azure.resourcemanager.chaos.models.ExternalResource;
import com.azure.resourcemanager.chaos.models.KeyValuePair;
import com.azure.resourcemanager.chaos.models.ParameterType;
import com.azure.resourcemanager.chaos.models.ScenarioAction;
import com.azure.resourcemanager.chaos.models.ScenarioParameter;
import com.azure.resourcemanager.chaos.models.ScenarioProperties;
import java.util.Arrays;

/**
 * Samples for Scenarios CreateOrUpdate.
 */
public final class ScenariosCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/Scenarios_CreateOrUpdate.json
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
                .withActions(
                    Arrays
                        .asList(
                            new ScenarioAction().withName("vmssZoneDown")
                                .withActionId("urn:csci:microsoft:compute:shutdown/1.0.0")
                                .withDescription("Force shutdown VMSS instances in target zone")
                                .withDuration("%%{parameters.duration}%%")
                                .withParameters(Arrays.asList(new KeyValuePair().withKey("fakeTokenPlaceholder")
                                    .withValue("%%{filters.zones}%%"))),
                            new ScenarioAction().withName("vmZoneDown")
                                .withActionId("urn:csci:microsoft:compute:shutdown/1.0.0")
                                .withDescription("Force shutdown VM instances in target zone")
                                .withDuration("%%{parameters.duration}%%")
                                .withParameters(Arrays.asList(new KeyValuePair().withKey("fakeTokenPlaceholder")
                                    .withValue("%%{filters.zones}%%"))),
                            new ScenarioAction()
                                .withName("redisFailover")
                                .withActionId("urn:csci:microsoft:azureClusteredCacheForRedis:Reboot/1.0.0")
                                .withDescription("Force failover of Redis instances to simulate backend zonal outage")
                                .withDuration("PT5M")
                                .withParameters(
                                    Arrays.asList(
                                        new KeyValuePair().withKey("fakeTokenPlaceholder").withValue("PrimaryNode"))),
                            new ScenarioAction().withName("custom-runbook-1")
                                .withActionId("urn:csci:microsoft:Automation:StartRunbook/1.0.0")
                                .withDescription("Custom Runbook 1")
                                .withDuration("PT30M")
                                .withParameters(Arrays.asList(new KeyValuePair()
                                    .withKey("fakeTokenPlaceholder")
                                    .withValue("%%{parameters.customRunbook1ParametersJson}%%")))
                                .withExternalResource(new ExternalResource().withResourceId(
                                    "/subscriptions/6b052e15-03d3-4f17-b2e1-be7f07588291/resourceGroups/exampleRG/providers/Microsoft.Automation/automationAccounts/exampleAutomationAccount/runbooks/exampleRunbook")))))
            .create();
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
     * x-ms-original-file: 2026-05-01-preview/Scenarios_Delete.json
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
     * x-ms-original-file: 2026-05-01-preview/Scenarios_Get.json
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
     * x-ms-original-file: 2026-05-01-preview/Scenarios_ListAll.json
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
     * x-ms-original-file: 2026-05-01-preview/TargetTypes_Get.json
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
     * x-ms-original-file: 2026-05-01-preview/TargetTypes_List.json
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
     * x-ms-original-file: 2026-05-01-preview/Targets_CreateOrUpdate.json
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
     * x-ms-original-file: 2026-05-01-preview/Targets_Delete.json
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
     * x-ms-original-file: 2026-05-01-preview/Targets_Get.json
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
     * x-ms-original-file: 2026-05-01-preview/Targets_List.json
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

### Workspaces_CreateOrUpdate

```java
import com.azure.resourcemanager.chaos.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.chaos.models.ResourceIdentity;
import com.azure.resourcemanager.chaos.models.UserAssignedIdentity;
import com.azure.resourcemanager.chaos.models.WorkspaceProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Workspaces CreateOrUpdate.
 */
public final class WorkspacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/Workspaces_CreateOrUpdate.json
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
     * x-ms-original-file: 2026-05-01-preview/Workspaces_Delete.json
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

### Workspaces_GetByResourceGroup

```java
/**
 * Samples for Workspaces GetByResourceGroup.
 */
public final class WorkspacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/Workspaces_Get.json
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
}
```

### Workspaces_List

```java
/**
 * Samples for Workspaces List.
 */
public final class WorkspacesListSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/Workspaces_ListAll.json
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
     * x-ms-original-file: 2026-05-01-preview/Workspaces_List.json
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

### Workspaces_RefreshRecommendations

```java
/**
 * Samples for Workspaces RefreshRecommendations.
 */
public final class WorkspacesRefreshRecommendationsSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/Workspaces_RefreshRecommendations.json
     */
    /**
     * Sample code: Refresh recommendations for all scenarios in a workspace.
     * 
     * @param manager Entry point to ChaosManager.
     */
    public static void
        refreshRecommendationsForAllScenariosInAWorkspace(com.azure.resourcemanager.chaos.ChaosManager manager) {
        manager.workspaces().refreshRecommendations("exampleRG", "exampleWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_Update

```java
import com.azure.resourcemanager.chaos.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.chaos.models.ResourceIdentity;
import com.azure.resourcemanager.chaos.models.UserAssignedIdentity;
import com.azure.resourcemanager.chaos.models.Workspace;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Workspaces Update.
 */
public final class WorkspacesUpdateSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/Workspaces_Update.json
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

