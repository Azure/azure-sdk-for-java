# Code snippets and samples


## Capabilities

- [CreateOrUpdate](#capabilities_createorupdate)
- [Delete](#capabilities_delete)
- [Get](#capabilities_get)
- [ListByWorkloadSpace](#capabilities_listbyworkloadspace)
- [Update](#capabilities_update)

## RuntimeBindings

- [CreateOrUpdate](#runtimebindings_createorupdate)
- [Delete](#runtimebindings_delete)
- [Get](#runtimebindings_get)
- [ListByWorkloadSpace](#runtimebindings_listbyworkloadspace)
- [Update](#runtimebindings_update)

## RuntimeLinks

- [CreateOrUpdate](#runtimelinks_createorupdate)
- [Delete](#runtimelinks_delete)
- [Get](#runtimelinks_get)
- [ListByWorkloadSpace](#runtimelinks_listbyworkloadspace)
- [Update](#runtimelinks_update)

## WorkloadSpaces

- [CreateOrUpdate](#workloadspaces_createorupdate)
- [Delete](#workloadspaces_delete)
- [GetByResourceGroup](#workloadspaces_getbyresourcegroup)
- [List](#workloadspaces_list)
- [ListByResourceGroup](#workloadspaces_listbyresourcegroup)
- [Update](#workloadspaces_update)
### Capabilities_CreateOrUpdate

```java
import com.azure.resourcemanager.compute.workloadmanager.models.CapabilityKind;
import com.azure.resourcemanager.compute.workloadmanager.models.CapabilityProperties;
import com.azure.resourcemanager.compute.workloadmanager.models.VersionPolicy;

/**
 * Samples for Capabilities CreateOrUpdate.
 */
public final class CapabilitiesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-11-01-preview/Capabilities_CreateOrUpdate_AgentSandbox.json
     */
    /**
     * Sample code: Enable the Agent Sandbox capability.
     * 
     * @param manager Entry point to ComputeWorkloadManagerManager.
     */
    public static void enableTheAgentSandboxCapability(
        com.azure.resourcemanager.compute.workloadmanager.ComputeWorkloadManagerManager manager) {
        manager.capabilities()
            .define("agentSandbox")
            .withRegion("eastus2")
            .withExistingWorkloadSpace("rg-workload", "managed-agents-prod")
            .withProperties(new CapabilityProperties().withVersionPolicy(VersionPolicy.SERVICE_MANAGED))
            .withKind(CapabilityKind.AGENT_SANDBOX)
            .create();
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
     * x-ms-original-file: 2026-11-01-preview/Capabilities_Delete.json
     */
    /**
     * Sample code: Delete the Agent Sandbox capability.
     * 
     * @param manager Entry point to ComputeWorkloadManagerManager.
     */
    public static void deleteTheAgentSandboxCapability(
        com.azure.resourcemanager.compute.workloadmanager.ComputeWorkloadManagerManager manager) {
        manager.capabilities()
            .delete("rg-workload", "managed-agents-prod", "agentSandbox", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-11-01-preview/Capabilities_Get.json
     */
    /**
     * Sample code: Get the Agent Sandbox capability.
     * 
     * @param manager Entry point to ComputeWorkloadManagerManager.
     */
    public static void getTheAgentSandboxCapability(
        com.azure.resourcemanager.compute.workloadmanager.ComputeWorkloadManagerManager manager) {
        manager.capabilities()
            .getWithResponse("rg-workload", "managed-agents-prod", "agentSandbox", com.azure.core.util.Context.NONE);
    }
}
```

### Capabilities_ListByWorkloadSpace

```java
/**
 * Samples for Capabilities ListByWorkloadSpace.
 */
public final class CapabilitiesListByWorkloadSpaceSamples {
    /*
     * x-ms-original-file: 2026-11-01-preview/Capabilities_ListByWorkloadSpace.json
     */
    /**
     * Sample code: List capabilities in a workload space.
     * 
     * @param manager Entry point to ComputeWorkloadManagerManager.
     */
    public static void listCapabilitiesInAWorkloadSpace(
        com.azure.resourcemanager.compute.workloadmanager.ComputeWorkloadManagerManager manager) {
        manager.capabilities()
            .listByWorkloadSpace("rg-workload", "managed-agents-prod", com.azure.core.util.Context.NONE);
    }
}
```

### Capabilities_Update

```java
import com.azure.resourcemanager.compute.workloadmanager.models.Capability;
import com.azure.resourcemanager.compute.workloadmanager.models.CapabilityUpdateProperties;
import com.azure.resourcemanager.compute.workloadmanager.models.VersionPolicy;

/**
 * Samples for Capabilities Update.
 */
public final class CapabilitiesUpdateSamples {
    /*
     * x-ms-original-file: 2026-11-01-preview/Capabilities_Update.json
     */
    /**
     * Sample code: Update the Agent Sandbox version policy.
     * 
     * @param manager Entry point to ComputeWorkloadManagerManager.
     */
    public static void updateTheAgentSandboxVersionPolicy(
        com.azure.resourcemanager.compute.workloadmanager.ComputeWorkloadManagerManager manager) {
        Capability resource = manager.capabilities()
            .getWithResponse("rg-workload", "managed-agents-prod", "agentSandbox", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new CapabilityUpdateProperties().withVersionPolicy(VersionPolicy.SERVICE_MANAGED))
            .apply();
    }
}
```

### RuntimeBindings_CreateOrUpdate

```java
import com.azure.resourcemanager.compute.workloadmanager.models.ExecutionIdentityScope;
import com.azure.resourcemanager.compute.workloadmanager.models.ManagedRuntimeBindingProperties;
import com.azure.resourcemanager.compute.workloadmanager.models.ManagedRuntimeProfile;
import com.azure.resourcemanager.compute.workloadmanager.models.ReferencedRuntimeBindingProperties;
import com.azure.resourcemanager.compute.workloadmanager.models.RuntimeBindingKind;
import com.azure.resourcemanager.compute.workloadmanager.models.RuntimeIdentityProfile;
import com.azure.resourcemanager.compute.workloadmanager.models.ServiceManagedExecutionIdentity;

/**
 * Samples for RuntimeBindings CreateOrUpdate.
 */
public final class RuntimeBindingsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-11-01-preview/RuntimeBindings_CreateOrUpdate_ReferencedKubernetes.json
     */
    /**
     * Sample code: Attach a referenced Kubernetes runtime binding.
     * 
     * @param manager Entry point to ComputeWorkloadManagerManager.
     */
    public static void attachAReferencedKubernetesRuntimeBinding(
        com.azure.resourcemanager.compute.workloadmanager.ComputeWorkloadManagerManager manager) {
        manager.runtimeBindings()
            .define("customer-aks")
            .withRegion("eastus2")
            .withExistingWorkloadSpace("rg-workload", "managed-agents-prod")
            .withProperties(new ReferencedRuntimeBindingProperties().withResourceId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/aks-rg/providers/Microsoft.ContainerService/managedClusters/aks-prod"))
            .withKind(RuntimeBindingKind.KUBERNETES)
            .create();
    }

    /*
     * x-ms-original-file: 2026-11-01-preview/RuntimeBindings_CreateOrUpdate_ManagedServerless.json
     */
    /**
     * Sample code: Create a managed serverless container runtime binding.
     * 
     * @param manager Entry point to ComputeWorkloadManagerManager.
     */
    public static void createAManagedServerlessContainerRuntimeBinding(
        com.azure.resourcemanager.compute.workloadmanager.ComputeWorkloadManagerManager manager) {
        manager.runtimeBindings()
            .define("serverless-default")
            .withRegion("eastus2")
            .withExistingWorkloadSpace("rg-workload", "managed-agents-prod")
            .withProperties(new ManagedRuntimeBindingProperties()
                .withIdentityProfile(new RuntimeIdentityProfile().withExecutionIdentity(
                    new ServiceManagedExecutionIdentity().withScope(ExecutionIdentityScope.SANDBOX_GROUP)))
                .withManagedProfile(new ManagedRuntimeProfile().withProvider("ACI")))
            .withKind(RuntimeBindingKind.SERVERLESS_CONTAINERS)
            .create();
    }

    /*
     * x-ms-original-file: 2026-11-01-preview/RuntimeBindings_CreateOrUpdate_ManagedKubernetes.json
     */
    /**
     * Sample code: Create a managed Kubernetes runtime binding.
     * 
     * @param manager Entry point to ComputeWorkloadManagerManager.
     */
    public static void createAManagedKubernetesRuntimeBinding(
        com.azure.resourcemanager.compute.workloadmanager.ComputeWorkloadManagerManager manager) {
        manager.runtimeBindings()
            .define("kubernetes-default")
            .withRegion("eastus2")
            .withExistingWorkloadSpace("rg-workload", "managed-agents-prod")
            .withProperties(new ManagedRuntimeBindingProperties()
                .withManagedProfile(new ManagedRuntimeProfile().withOffering("Automatic")))
            .withKind(RuntimeBindingKind.KUBERNETES)
            .create();
    }
}
```

### RuntimeBindings_Delete

```java
/**
 * Samples for RuntimeBindings Delete.
 */
public final class RuntimeBindingsDeleteSamples {
    /*
     * x-ms-original-file: 2026-11-01-preview/RuntimeBindings_Delete.json
     */
    /**
     * Sample code: Delete a runtime binding.
     * 
     * @param manager Entry point to ComputeWorkloadManagerManager.
     */
    public static void
        deleteARuntimeBinding(com.azure.resourcemanager.compute.workloadmanager.ComputeWorkloadManagerManager manager) {
        manager.runtimeBindings()
            .delete("rg-workload", "managed-agents-prod", "kubernetes-default", com.azure.core.util.Context.NONE);
    }
}
```

### RuntimeBindings_Get

```java
/**
 * Samples for RuntimeBindings Get.
 */
public final class RuntimeBindingsGetSamples {
    /*
     * x-ms-original-file: 2026-11-01-preview/RuntimeBindings_Get.json
     */
    /**
     * Sample code: Get a runtime binding.
     * 
     * @param manager Entry point to ComputeWorkloadManagerManager.
     */
    public static void
        getARuntimeBinding(com.azure.resourcemanager.compute.workloadmanager.ComputeWorkloadManagerManager manager) {
        manager.runtimeBindings()
            .getWithResponse("rg-workload", "managed-agents-prod", "kubernetes-default",
                com.azure.core.util.Context.NONE);
    }
}
```

### RuntimeBindings_ListByWorkloadSpace

```java
/**
 * Samples for RuntimeBindings ListByWorkloadSpace.
 */
public final class RuntimeBindingsListByWorkloadSpaceSamples {
    /*
     * x-ms-original-file: 2026-11-01-preview/RuntimeBindings_ListByWorkloadSpace.json
     */
    /**
     * Sample code: List runtime bindings in a workload space.
     * 
     * @param manager Entry point to ComputeWorkloadManagerManager.
     */
    public static void listRuntimeBindingsInAWorkloadSpace(
        com.azure.resourcemanager.compute.workloadmanager.ComputeWorkloadManagerManager manager) {
        manager.runtimeBindings()
            .listByWorkloadSpace("rg-workload", "managed-agents-prod", com.azure.core.util.Context.NONE);
    }
}
```

### RuntimeBindings_Update

```java
import com.azure.resourcemanager.compute.workloadmanager.models.EgressMode;
import com.azure.resourcemanager.compute.workloadmanager.models.RuntimeBinding;
import com.azure.resourcemanager.compute.workloadmanager.models.RuntimeBindingUpdateProperties;
import com.azure.resourcemanager.compute.workloadmanager.models.RuntimeNetworkProfile;

/**
 * Samples for RuntimeBindings Update.
 */
public final class RuntimeBindingsUpdateSamples {
    /*
     * x-ms-original-file: 2026-11-01-preview/RuntimeBindings_Update.json
     */
    /**
     * Sample code: Update mutable runtime binding network configuration.
     * 
     * @param manager Entry point to ComputeWorkloadManagerManager.
     */
    public static void updateMutableRuntimeBindingNetworkConfiguration(
        com.azure.resourcemanager.compute.workloadmanager.ComputeWorkloadManagerManager manager) {
        RuntimeBinding resource = manager.runtimeBindings()
            .getWithResponse("rg-workload", "managed-agents-prod", "serverless-default",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new RuntimeBindingUpdateProperties().withNetworkProfile(new RuntimeNetworkProfile()
                .withSubnetResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg-network/providers/Microsoft.Network/virtualNetworks/workload-vnet/subnets/execution")
                .withEgressMode(EgressMode.CUSTOMER_MANAGED)))
            .apply();
    }
}
```

### RuntimeLinks_CreateOrUpdate

```java
import com.azure.resourcemanager.compute.workloadmanager.models.CapacityProfile;
import com.azure.resourcemanager.compute.workloadmanager.models.RuntimeLinkProperties;

/**
 * Samples for RuntimeLinks CreateOrUpdate.
 */
public final class RuntimeLinksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-11-01-preview/RuntimeLinks_CreateOrUpdate_AksAci.json
     */
    /**
     * Sample code: Create a runtime link between Kubernetes and serverless containers.
     * 
     * @param manager Entry point to ComputeWorkloadManagerManager.
     */
    public static void createARuntimeLinkBetweenKubernetesAndServerlessContainers(
        com.azure.resourcemanager.compute.workloadmanager.ComputeWorkloadManagerManager manager) {
        manager.runtimeLinks()
            .define("default")
            .withRegion("eastus2")
            .withExistingWorkloadSpace("rg-workload", "managed-agents-prod")
            .withProperties(new RuntimeLinkProperties().withOrchestratorBindingResourceId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg-workload/providers/Microsoft.Compute/workloadSpaces/managed-agents-prod/runtimeBindings/kubernetes-default")
                .withExecutionBindingResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg-workload/providers/Microsoft.Compute/workloadSpaces/managed-agents-prod/runtimeBindings/serverless-default")
                .withCapacityProfile(new CapacityProfile().withMinimumNodes(1).withMaximumNodes(100)))
            .create();
    }

    /*
     * x-ms-original-file: 2026-11-01-preview/RuntimeLinks_CreateOrUpdate_AksOnly.json
     */
    /**
     * Sample code: Create a Kubernetes-only runtime link.
     * 
     * @param manager Entry point to ComputeWorkloadManagerManager.
     */
    public static void createAKubernetesOnlyRuntimeLink(
        com.azure.resourcemanager.compute.workloadmanager.ComputeWorkloadManagerManager manager) {
        manager.runtimeLinks()
            .define("kubernetes-only")
            .withRegion("eastus2")
            .withExistingWorkloadSpace("rg-workload", "managed-agents-prod")
            .withProperties(new RuntimeLinkProperties().withOrchestratorBindingResourceId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg-workload/providers/Microsoft.Compute/workloadSpaces/managed-agents-prod/runtimeBindings/customer-aks"))
            .create();
    }
}
```

### RuntimeLinks_Delete

```java
/**
 * Samples for RuntimeLinks Delete.
 */
public final class RuntimeLinksDeleteSamples {
    /*
     * x-ms-original-file: 2026-11-01-preview/RuntimeLinks_Delete.json
     */
    /**
     * Sample code: Delete a runtime link.
     * 
     * @param manager Entry point to ComputeWorkloadManagerManager.
     */
    public static void
        deleteARuntimeLink(com.azure.resourcemanager.compute.workloadmanager.ComputeWorkloadManagerManager manager) {
        manager.runtimeLinks()
            .delete("rg-workload", "managed-agents-prod", "default", com.azure.core.util.Context.NONE);
    }
}
```

### RuntimeLinks_Get

```java
/**
 * Samples for RuntimeLinks Get.
 */
public final class RuntimeLinksGetSamples {
    /*
     * x-ms-original-file: 2026-11-01-preview/RuntimeLinks_Get.json
     */
    /**
     * Sample code: Get a runtime link.
     * 
     * @param manager Entry point to ComputeWorkloadManagerManager.
     */
    public static void
        getARuntimeLink(com.azure.resourcemanager.compute.workloadmanager.ComputeWorkloadManagerManager manager) {
        manager.runtimeLinks()
            .getWithResponse("rg-workload", "managed-agents-prod", "default", com.azure.core.util.Context.NONE);
    }
}
```

### RuntimeLinks_ListByWorkloadSpace

```java
/**
 * Samples for RuntimeLinks ListByWorkloadSpace.
 */
public final class RuntimeLinksListByWorkloadSpaceSamples {
    /*
     * x-ms-original-file: 2026-11-01-preview/RuntimeLinks_ListByWorkloadSpace.json
     */
    /**
     * Sample code: List runtime links in a workload space.
     * 
     * @param manager Entry point to ComputeWorkloadManagerManager.
     */
    public static void listRuntimeLinksInAWorkloadSpace(
        com.azure.resourcemanager.compute.workloadmanager.ComputeWorkloadManagerManager manager) {
        manager.runtimeLinks()
            .listByWorkloadSpace("rg-workload", "managed-agents-prod", com.azure.core.util.Context.NONE);
    }
}
```

### RuntimeLinks_Update

```java
import com.azure.resourcemanager.compute.workloadmanager.models.CapacityProfileUpdate;
import com.azure.resourcemanager.compute.workloadmanager.models.RuntimeLink;
import com.azure.resourcemanager.compute.workloadmanager.models.RuntimeLinkUpdateProperties;

/**
 * Samples for RuntimeLinks Update.
 */
public final class RuntimeLinksUpdateSamples {
    /*
     * x-ms-original-file: 2026-11-01-preview/RuntimeLinks_Update.json
     */
    /**
     * Sample code: Update runtime link capacity.
     * 
     * @param manager Entry point to ComputeWorkloadManagerManager.
     */
    public static void updateRuntimeLinkCapacity(
        com.azure.resourcemanager.compute.workloadmanager.ComputeWorkloadManagerManager manager) {
        RuntimeLink resource = manager.runtimeLinks()
            .getWithResponse("rg-workload", "managed-agents-prod", "default", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new RuntimeLinkUpdateProperties()
                .withCapacityProfile(new CapacityProfileUpdate().withMinimumNodes(2).withMaximumNodes(150)))
            .apply();
    }
}
```

### WorkloadSpaces_CreateOrUpdate

```java
import com.azure.resourcemanager.compute.workloadmanager.models.WorkloadSpaceProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for WorkloadSpaces CreateOrUpdate.
 */
public final class WorkloadSpacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-11-01-preview/WorkloadSpaces_CreateOrUpdate.json
     */
    /**
     * Sample code: Create a production workload space.
     * 
     * @param manager Entry point to ComputeWorkloadManagerManager.
     */
    public static void createAProductionWorkloadSpace(
        com.azure.resourcemanager.compute.workloadmanager.ComputeWorkloadManagerManager manager) {
        manager.workloadSpaces()
            .define("managed-agents-prod")
            .withRegion("eastus2")
            .withExistingResourceGroup("rg-workload")
            .withTags(mapOf("environment", "Production"))
            .withProperties(new WorkloadSpaceProperties())
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

### WorkloadSpaces_Delete

```java
/**
 * Samples for WorkloadSpaces Delete.
 */
public final class WorkloadSpacesDeleteSamples {
    /*
     * x-ms-original-file: 2026-11-01-preview/WorkloadSpaces_Delete.json
     */
    /**
     * Sample code: Delete a workload space.
     * 
     * @param manager Entry point to ComputeWorkloadManagerManager.
     */
    public static void
        deleteAWorkloadSpace(com.azure.resourcemanager.compute.workloadmanager.ComputeWorkloadManagerManager manager) {
        manager.workloadSpaces().delete("rg-workload", "managed-agents-prod", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadSpaces_GetByResourceGroup

```java
/**
 * Samples for WorkloadSpaces GetByResourceGroup.
 */
public final class WorkloadSpacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-11-01-preview/WorkloadSpaces_Get.json
     */
    /**
     * Sample code: Get a workload space.
     * 
     * @param manager Entry point to ComputeWorkloadManagerManager.
     */
    public static void
        getAWorkloadSpace(com.azure.resourcemanager.compute.workloadmanager.ComputeWorkloadManagerManager manager) {
        manager.workloadSpaces()
            .getByResourceGroupWithResponse("rg-workload", "managed-agents-prod", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadSpaces_List

```java
/**
 * Samples for WorkloadSpaces List.
 */
public final class WorkloadSpacesListSamples {
    /*
     * x-ms-original-file: 2026-11-01-preview/WorkloadSpaces_ListBySubscription.json
     */
    /**
     * Sample code: List workload spaces in a subscription.
     * 
     * @param manager Entry point to ComputeWorkloadManagerManager.
     */
    public static void listWorkloadSpacesInASubscription(
        com.azure.resourcemanager.compute.workloadmanager.ComputeWorkloadManagerManager manager) {
        manager.workloadSpaces().list(com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadSpaces_ListByResourceGroup

```java
/**
 * Samples for WorkloadSpaces ListByResourceGroup.
 */
public final class WorkloadSpacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-11-01-preview/WorkloadSpaces_ListByResourceGroup.json
     */
    /**
     * Sample code: List workload spaces in a resource group.
     * 
     * @param manager Entry point to ComputeWorkloadManagerManager.
     */
    public static void listWorkloadSpacesInAResourceGroup(
        com.azure.resourcemanager.compute.workloadmanager.ComputeWorkloadManagerManager manager) {
        manager.workloadSpaces().listByResourceGroup("rg-workload", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadSpaces_Update

```java
import com.azure.resourcemanager.compute.workloadmanager.models.WorkloadSpace;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for WorkloadSpaces Update.
 */
public final class WorkloadSpacesUpdateSamples {
    /*
     * x-ms-original-file: 2026-11-01-preview/WorkloadSpaces_Update.json
     */
    /**
     * Sample code: Update workload space tags.
     * 
     * @param manager Entry point to ComputeWorkloadManagerManager.
     */
    public static void updateWorkloadSpaceTags(
        com.azure.resourcemanager.compute.workloadmanager.ComputeWorkloadManagerManager manager) {
        WorkloadSpace resource = manager.workloadSpaces()
            .getByResourceGroupWithResponse("rg-workload", "managed-agents-prod", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("environment", "Production", "costCenter", "AI-Platform")).apply();
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

