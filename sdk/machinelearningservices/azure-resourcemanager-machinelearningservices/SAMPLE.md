# Code snippets and samples


## Compute

- [CreateOrUpdate](#compute_createorupdate)
- [Delete](#compute_delete)
- [Get](#compute_get)
- [List](#compute_list)
- [ListKeys](#compute_listkeys)
- [ListNodes](#compute_listnodes)
- [Restart](#compute_restart)
- [Start](#compute_start)
- [Stop](#compute_stop)
- [Update](#compute_update)

## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [List](#privateendpointconnections_list)

## PrivateLinkResources

- [List](#privatelinkresources_list)

## Quotas

- [List](#quotas_list)
- [Update](#quotas_update)

## Usages

- [List](#usages_list)

## VirtualMachineSizes

- [List](#virtualmachinesizes_list)

## WorkspaceConnections

- [Create](#workspaceconnections_create)
- [Delete](#workspaceconnections_delete)
- [Get](#workspaceconnections_get)
- [List](#workspaceconnections_list)

## WorkspaceFeatures

- [List](#workspacefeatures_list)

## WorkspaceSkus

- [List](#workspaceskus_list)

## Workspaces

- [CreateOrUpdate](#workspaces_createorupdate)
- [Delete](#workspaces_delete)
- [Diagnose](#workspaces_diagnose)
- [GetByResourceGroup](#workspaces_getbyresourcegroup)
- [List](#workspaces_list)
- [ListByResourceGroup](#workspaces_listbyresourcegroup)
- [ListKeys](#workspaces_listkeys)
- [ListNotebookAccessToken](#workspaces_listnotebookaccesstoken)
- [ListNotebookKeys](#workspaces_listnotebookkeys)
- [ListOutboundNetworkDependenciesEndpoints](#workspaces_listoutboundnetworkdependenciesendpoints)
- [ListStorageAccountKeys](#workspaces_liststorageaccountkeys)
- [PrepareNotebook](#workspaces_preparenotebook)
- [ResyncKeys](#workspaces_resynckeys)
- [Update](#workspaces_update)
### Compute_CreateOrUpdate

```java
import com.azure.resourcemanager.machinelearningservices.models.Aks;
import com.azure.resourcemanager.machinelearningservices.models.AksProperties;
import com.azure.resourcemanager.machinelearningservices.models.AmlCompute;
import com.azure.resourcemanager.machinelearningservices.models.AmlComputeProperties;
import com.azure.resourcemanager.machinelearningservices.models.ApplicationSharingPolicy;
import com.azure.resourcemanager.machinelearningservices.models.AssignedUser;
import com.azure.resourcemanager.machinelearningservices.models.ComputeInstance;
import com.azure.resourcemanager.machinelearningservices.models.ComputeInstanceAuthorizationType;
import com.azure.resourcemanager.machinelearningservices.models.ComputeInstanceProperties;
import com.azure.resourcemanager.machinelearningservices.models.ComputeInstanceSshSettings;
import com.azure.resourcemanager.machinelearningservices.models.DataFactory;
import com.azure.resourcemanager.machinelearningservices.models.InstanceTypeSchema;
import com.azure.resourcemanager.machinelearningservices.models.InstanceTypeSchemaResources;
import com.azure.resourcemanager.machinelearningservices.models.Kubernetes;
import com.azure.resourcemanager.machinelearningservices.models.KubernetesProperties;
import com.azure.resourcemanager.machinelearningservices.models.OsType;
import com.azure.resourcemanager.machinelearningservices.models.PersonalComputeInstanceSettings;
import com.azure.resourcemanager.machinelearningservices.models.RemoteLoginPortPublicAccess;
import com.azure.resourcemanager.machinelearningservices.models.ResourceId;
import com.azure.resourcemanager.machinelearningservices.models.ScaleSettings;
import com.azure.resourcemanager.machinelearningservices.models.SshPublicAccess;
import com.azure.resourcemanager.machinelearningservices.models.VirtualMachineImage;
import com.azure.resourcemanager.machinelearningservices.models.VmPriority;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/** Samples for Compute CreateOrUpdate. */
public final class ComputeCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Compute/createOrUpdate/BasicAKSCompute.json
     */
    /**
     * Sample code: Create an AKS Compute.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void createAnAKSCompute(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager
            .computes()
            .define("compute123")
            .withExistingWorkspace("testrg123", "workspaces123")
            .withRegion("eastus")
            .withProperties(new Aks())
            .create();
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Compute/createOrUpdate/AKSCompute.json
     */
    /**
     * Sample code: Update an AKS Compute.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void updateAnAKSCompute(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager
            .computes()
            .define("compute123")
            .withExistingWorkspace("testrg123", "workspaces123")
            .withRegion("eastus")
            .withProperties(
                new Aks()
                    .withDescription("some compute")
                    .withResourceId(
                        "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourcegroups/testrg123/providers/Microsoft.ContainerService/managedClusters/compute123-56826-c9b00420020b2")
                    .withProperties(new AksProperties().withAgentCount(4)))
            .create();
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Compute/createOrUpdate/KubernetesCompute.json
     */
    /**
     * Sample code: Attach a Kubernetes Compute.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void attachAKubernetesCompute(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager
            .computes()
            .define("compute123")
            .withExistingWorkspace("testrg123", "workspaces123")
            .withRegion("eastus")
            .withProperties(
                new Kubernetes()
                    .withDescription("some compute")
                    .withResourceId(
                        "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourcegroups/testrg123/providers/Microsoft.ContainerService/managedClusters/compute123-56826-c9b00420020b2")
                    .withProperties(
                        new KubernetesProperties()
                            .withNamespace("default")
                            .withDefaultInstanceType("defaultInstanceType")
                            .withInstanceTypes(
                                mapOf(
                                    "defaultInstanceType",
                                    new InstanceTypeSchema()
                                        .withResources(
                                            new InstanceTypeSchemaResources()
                                                .withRequests(
                                                    mapOf("cpu", "1", "memory", "4Gi", "nvidia.com/gpu", null))
                                                .withLimits(
                                                    mapOf("cpu", "1", "memory", "4Gi", "nvidia.com/gpu", null)))))))
            .create();
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Compute/createOrUpdate/BasicAmlCompute.json
     */
    /**
     * Sample code: Create a AML Compute.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void createAAMLCompute(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager
            .computes()
            .define("compute123")
            .withExistingWorkspace("testrg123", "workspaces123")
            .withRegion("eastus")
            .withProperties(
                new AmlCompute()
                    .withProperties(
                        new AmlComputeProperties()
                            .withOsType(OsType.WINDOWS)
                            .withVmSize("STANDARD_NC6")
                            .withVmPriority(VmPriority.DEDICATED)
                            .withVirtualMachineImage(
                                new VirtualMachineImage()
                                    .withId(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Compute/galleries/myImageGallery/images/myImageDefinition/versions/0.0.1"))
                            .withIsolatedNetwork(false)
                            .withScaleSettings(
                                new ScaleSettings()
                                    .withMaxNodeCount(1)
                                    .withMinNodeCount(0)
                                    .withNodeIdleTimeBeforeScaleDown(Duration.parse("PT5M")))
                            .withRemoteLoginPortPublicAccess(RemoteLoginPortPublicAccess.NOT_SPECIFIED)
                            .withEnableNodePublicIp(true)))
            .create();
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Compute/createOrUpdate/ComputeInstance.json
     */
    /**
     * Sample code: Create an ComputeInstance Compute.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void createAnComputeInstanceCompute(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager
            .computes()
            .define("compute123")
            .withExistingWorkspace("testrg123", "workspaces123")
            .withRegion("eastus")
            .withProperties(
                new ComputeInstance()
                    .withProperties(
                        new ComputeInstanceProperties()
                            .withVmSize("STANDARD_NC6")
                            .withSubnet(new ResourceId())
                            .withApplicationSharingPolicy(ApplicationSharingPolicy.PERSONAL)
                            .withSshSettings(
                                new ComputeInstanceSshSettings().withSshPublicAccess(SshPublicAccess.DISABLED))
                            .withComputeInstanceAuthorizationType(ComputeInstanceAuthorizationType.PERSONAL)
                            .withPersonalComputeInstanceSettings(
                                new PersonalComputeInstanceSettings()
                                    .withAssignedUser(
                                        new AssignedUser()
                                            .withObjectId("00000000-0000-0000-0000-000000000000")
                                            .withTenantId("00000000-0000-0000-0000-000000000000")))))
            .create();
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Compute/createOrUpdate/ComputeInstanceMinimal.json
     */
    /**
     * Sample code: Create an ComputeInstance Compute with minimal inputs.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void createAnComputeInstanceComputeWithMinimalInputs(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager
            .computes()
            .define("compute123")
            .withExistingWorkspace("testrg123", "workspaces123")
            .withRegion("eastus")
            .withProperties(
                new ComputeInstance().withProperties(new ComputeInstanceProperties().withVmSize("STANDARD_NC6")))
            .create();
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Compute/createOrUpdate/AmlCompute.json
     */
    /**
     * Sample code: Update a AML Compute.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void updateAAMLCompute(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager
            .computes()
            .define("compute123")
            .withExistingWorkspace("testrg123", "workspaces123")
            .withRegion("eastus")
            .withProperties(
                new AmlCompute()
                    .withDescription("some compute")
                    .withProperties(
                        new AmlComputeProperties()
                            .withScaleSettings(
                                new ScaleSettings()
                                    .withMaxNodeCount(4)
                                    .withMinNodeCount(4)
                                    .withNodeIdleTimeBeforeScaleDown(Duration.parse("PT5M")))))
            .create();
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Compute/createOrUpdate/BasicDataFactoryCompute.json
     */
    /**
     * Sample code: Create a DataFactory Compute.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void createADataFactoryCompute(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager
            .computes()
            .define("compute123")
            .withExistingWorkspace("testrg123", "workspaces123")
            .withRegion("eastus")
            .withProperties(new DataFactory())
            .create();
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

### Compute_Delete

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.machinelearningservices.models.UnderlyingResourceAction;

/** Samples for Compute Delete. */
public final class ComputeDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Compute/delete.json
     */
    /**
     * Sample code: Delete Compute.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void deleteCompute(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager
            .computes()
            .delete("testrg123", "workspaces123", "compute123", UnderlyingResourceAction.DELETE, Context.NONE);
    }
}
```

### Compute_Get

```java
import com.azure.core.util.Context;

/** Samples for Compute Get. */
public final class ComputeGetSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Compute/get/AKSCompute.json
     */
    /**
     * Sample code: Get a AKS Compute.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void getAAKSCompute(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.computes().getWithResponse("testrg123", "workspaces123", "compute123", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Compute/get/KubernetesCompute.json
     */
    /**
     * Sample code: Get a Kubernetes Compute.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void getAKubernetesCompute(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.computes().getWithResponse("testrg123", "workspaces123", "compute123", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Compute/get/ComputeInstance.json
     */
    /**
     * Sample code: Get an ComputeInstance.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void getAnComputeInstance(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.computes().getWithResponse("testrg123", "workspaces123", "compute123", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Compute/get/AmlCompute.json
     */
    /**
     * Sample code: Get a AML Compute.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void getAAMLCompute(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.computes().getWithResponse("testrg123", "workspaces123", "compute123", Context.NONE);
    }
}
```

### Compute_List

```java
import com.azure.core.util.Context;

/** Samples for Compute List. */
public final class ComputeListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Compute/list.json
     */
    /**
     * Sample code: Get Computes.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void getComputes(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.computes().list("testrg123", "workspaces123", null, Context.NONE);
    }
}
```

### Compute_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for Compute ListKeys. */
public final class ComputeListKeysSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Compute/listKeys.json
     */
    /**
     * Sample code: List AKS Compute Keys.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void listAKSComputeKeys(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.computes().listKeysWithResponse("testrg123", "workspaces123", "compute123", Context.NONE);
    }
}
```

### Compute_ListNodes

```java
import com.azure.core.util.Context;

/** Samples for Compute ListNodes. */
public final class ComputeListNodesSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Compute/listNodes.json
     */
    /**
     * Sample code: Get compute nodes information for a compute.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void getComputeNodesInformationForACompute(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.computes().listNodes("testrg123", "workspaces123", "compute123", Context.NONE);
    }
}
```

### Compute_Restart

```java
import com.azure.core.util.Context;

/** Samples for Compute Restart. */
public final class ComputeRestartSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Compute/restart.json
     */
    /**
     * Sample code: Restart ComputeInstance Compute.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void restartComputeInstanceCompute(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.computes().restart("testrg123", "workspaces123", "compute123", Context.NONE);
    }
}
```

### Compute_Start

```java
import com.azure.core.util.Context;

/** Samples for Compute Start. */
public final class ComputeStartSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Compute/start.json
     */
    /**
     * Sample code: Start ComputeInstance Compute.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void startComputeInstanceCompute(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.computes().start("testrg123", "workspaces123", "compute123", Context.NONE);
    }
}
```

### Compute_Stop

```java
import com.azure.core.util.Context;

/** Samples for Compute Stop. */
public final class ComputeStopSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Compute/stop.json
     */
    /**
     * Sample code: Stop ComputeInstance Compute.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void stopComputeInstanceCompute(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.computes().stop("testrg123", "workspaces123", "compute123", Context.NONE);
    }
}
```

### Compute_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.machinelearningservices.models.ComputeResource;
import com.azure.resourcemanager.machinelearningservices.models.ScaleSettings;
import com.azure.resourcemanager.machinelearningservices.models.ScaleSettingsInformation;
import java.time.Duration;

/** Samples for Compute Update. */
public final class ComputeUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Compute/patch.json
     */
    /**
     * Sample code: Update a AmlCompute Compute.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void updateAAmlComputeCompute(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        ComputeResource resource =
            manager.computes().getWithResponse("testrg123", "workspaces123", "compute123", Context.NONE).getValue();
        resource
            .update()
            .withProperties(
                new ScaleSettingsInformation()
                    .withScaleSettings(
                        new ScaleSettings()
                            .withMaxNodeCount(4)
                            .withMinNodeCount(4)
                            .withNodeIdleTimeBeforeScaleDown(Duration.parse("PT5M"))))
            .apply();
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.machinelearningservices.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.machinelearningservices.models.PrivateLinkServiceConnectionState;

/** Samples for PrivateEndpointConnections CreateOrUpdate. */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/PrivateEndpointConnection/createOrUpdate.json
     */
    /**
     * Sample code: WorkspacePutPrivateEndpointConnection.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void workspacePutPrivateEndpointConnection(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager
            .privateEndpointConnections()
            .define("{privateEndpointConnectionName}")
            .withExistingWorkspace("rg-1234", "testworkspace")
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState()
                    .withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("Auto-Approved"))
            .create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Delete. */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/PrivateEndpointConnection/delete.json
     */
    /**
     * Sample code: WorkspaceDeletePrivateEndpointConnection.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void workspaceDeletePrivateEndpointConnection(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager
            .privateEndpointConnections()
            .deleteWithResponse("rg-1234", "testworkspace", "{privateEndpointConnectionName}", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/PrivateEndpointConnection/get.json
     */
    /**
     * Sample code: WorkspaceGetPrivateEndpointConnection.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void workspaceGetPrivateEndpointConnection(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse("rg-1234", "testworkspace", "{privateEndpointConnectionName}", Context.NONE);
    }
}
```

### PrivateEndpointConnections_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections List. */
public final class PrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/PrivateEndpointConnection/list.json
     */
    /**
     * Sample code: StorageAccountListPrivateEndpointConnections.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void storageAccountListPrivateEndpointConnections(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.privateEndpointConnections().list("rg-1234", "testworkspace", Context.NONE);
    }
}
```

### PrivateLinkResources_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources List. */
public final class PrivateLinkResourcesListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/PrivateLinkResource/list.json
     */
    /**
     * Sample code: WorkspaceListPrivateLinkResources.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void workspaceListPrivateLinkResources(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.privateLinkResources().listWithResponse("rg-1234", "testworkspace", Context.NONE);
    }
}
```

### Quotas_List

```java
import com.azure.core.util.Context;

/** Samples for Quotas List. */
public final class QuotasListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Quota/list.json
     */
    /**
     * Sample code: List workspace quotas by VMFamily.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void listWorkspaceQuotasByVMFamily(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.quotas().list("eastus", Context.NONE);
    }
}
```

### Quotas_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.machinelearningservices.models.QuotaBaseProperties;
import com.azure.resourcemanager.machinelearningservices.models.QuotaUnit;
import com.azure.resourcemanager.machinelearningservices.models.QuotaUpdateParameters;
import java.util.Arrays;

/** Samples for Quotas Update. */
public final class QuotasUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Quota/update.json
     */
    /**
     * Sample code: update quotas.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void updateQuotas(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager
            .quotas()
            .updateWithResponse(
                "eastus",
                new QuotaUpdateParameters()
                    .withValue(
                        Arrays
                            .asList(
                                new QuotaBaseProperties()
                                    .withId(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.MachineLearningServices/workspaces/demo_workspace1/quotas/Standard_DSv2_Family_Cluster_Dedicated_vCPUs")
                                    .withType("Microsoft.MachineLearningServices/workspaces/quotas")
                                    .withLimit(100L)
                                    .withUnit(QuotaUnit.COUNT),
                                new QuotaBaseProperties()
                                    .withId(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.MachineLearningServices/workspaces/demo_workspace2/quotas/Standard_DSv2_Family_Cluster_Dedicated_vCPUs")
                                    .withType("Microsoft.MachineLearningServices/workspaces/quotas")
                                    .withLimit(200L)
                                    .withUnit(QuotaUnit.COUNT))),
                Context.NONE);
    }
}
```

### Usages_List

```java
import com.azure.core.util.Context;

/** Samples for Usages List. */
public final class UsagesListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Usage/list.json
     */
    /**
     * Sample code: List Usages.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void listUsages(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.usages().list("eastus", Context.NONE);
    }
}
```

### VirtualMachineSizes_List

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachineSizes List. */
public final class VirtualMachineSizesListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/VirtualMachineSize/list.json
     */
    /**
     * Sample code: List VM Sizes.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void listVMSizes(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.virtualMachineSizes().listWithResponse("eastus", Context.NONE);
    }
}
```

### WorkspaceConnections_Create

```java
/** Samples for WorkspaceConnections Create. */
public final class WorkspaceConnectionsCreateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/WorkspaceConnection/create.json
     */
    /**
     * Sample code: CreateWorkspaceConnection.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void createWorkspaceConnection(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager
            .workspaceConnections()
            .define("connection-1")
            .withExistingWorkspace("resourceGroup-1", "workspace-1")
            .withCategory("ACR")
            .withTarget("www.facebook.com")
            .withAuthType("PAT")
            .withValue("secrets")
            .create();
    }
}
```

### WorkspaceConnections_Delete

```java
import com.azure.core.util.Context;

/** Samples for WorkspaceConnections Delete. */
public final class WorkspaceConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/WorkspaceConnection/delete.json
     */
    /**
     * Sample code: DeleteWorkspaceConnection.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void deleteWorkspaceConnection(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager
            .workspaceConnections()
            .deleteWithResponse("resourceGroup-1", "workspace-1", "connection-1", Context.NONE);
    }
}
```

### WorkspaceConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for WorkspaceConnections Get. */
public final class WorkspaceConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/WorkspaceConnection/get.json
     */
    /**
     * Sample code: GetWorkspaceConnection.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void getWorkspaceConnection(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.workspaceConnections().getWithResponse("resourceGroup-1", "workspace-1", "connection-1", Context.NONE);
    }
}
```

### WorkspaceConnections_List

```java
import com.azure.core.util.Context;

/** Samples for WorkspaceConnections List. */
public final class WorkspaceConnectionsListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/WorkspaceConnection/list.json
     */
    /**
     * Sample code: ListWorkspaceConnections.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void listWorkspaceConnections(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.workspaceConnections().list("resourceGroup-1", "workspace-1", "www.facebook.com", "ACR", Context.NONE);
    }
}
```

### WorkspaceFeatures_List

```java
import com.azure.core.util.Context;

/** Samples for WorkspaceFeatures List. */
public final class WorkspaceFeaturesListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/WorkspaceFeature/list.json
     */
    /**
     * Sample code: List Workspace features.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void listWorkspaceFeatures(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.workspaceFeatures().list("myResourceGroup", "testworkspace", Context.NONE);
    }
}
```

### WorkspaceSkus_List

```java
import com.azure.core.util.Context;

/** Samples for WorkspaceSkus List. */
public final class WorkspaceSkusListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/WorkspaceSku/list.json
     */
    /**
     * Sample code: List Skus.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void listSkus(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.workspaceSkus().list(Context.NONE);
    }
}
```

### Workspaces_CreateOrUpdate

```java
import com.azure.resourcemanager.machinelearningservices.models.EncryptionProperty;
import com.azure.resourcemanager.machinelearningservices.models.EncryptionStatus;
import com.azure.resourcemanager.machinelearningservices.models.Identity;
import com.azure.resourcemanager.machinelearningservices.models.IdentityForCmk;
import com.azure.resourcemanager.machinelearningservices.models.KeyVaultProperties;
import com.azure.resourcemanager.machinelearningservices.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.machinelearningservices.models.ResourceIdentityType;
import com.azure.resourcemanager.machinelearningservices.models.SharedPrivateLinkResource;
import com.azure.resourcemanager.machinelearningservices.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Workspaces CreateOrUpdate. */
public final class WorkspacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Workspace/create.json
     */
    /**
     * Sample code: Create Workspace.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void createWorkspace(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager
            .workspaces()
            .define("testworkspace")
            .withExistingResourceGroup("workspace-1234")
            .withRegion("eastus2euap")
            .withIdentity(
                new Identity()
                    .withType(ResourceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/workspace-1234/providers/Microsoft.ManagedIdentity/userAssignedIdentities/testuai",
                            new UserAssignedIdentity())))
            .withDescription("test description")
            .withFriendlyName("HelloName")
            .withKeyVault(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/workspace-1234/providers/Microsoft.KeyVault/vaults/testkv")
            .withApplicationInsights(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/workspace-1234/providers/microsoft.insights/components/testinsights")
            .withContainerRegistry(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/workspace-1234/providers/Microsoft.ContainerRegistry/registries/testRegistry")
            .withStorageAccount(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/accountcrud-1234/providers/Microsoft.Storage/storageAccounts/testStorageAccount")
            .withEncryption(
                new EncryptionProperty()
                    .withStatus(EncryptionStatus.ENABLED)
                    .withIdentity(
                        new IdentityForCmk()
                            .withUserAssignedIdentity(
                                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/workspace-1234/providers/Microsoft.ManagedIdentity/userAssignedIdentities/testuai"))
                    .withKeyVaultProperties(
                        new KeyVaultProperties()
                            .withKeyVaultArmId(
                                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/workspace-1234/providers/Microsoft.KeyVault/vaults/testkv")
                            .withKeyIdentifier(
                                "https://testkv.vault.azure.net/keys/testkey/aabbccddee112233445566778899aabb")
                            .withIdentityClientId("")))
            .withHbiWorkspace(false)
            .withSharedPrivateLinkResources(
                Arrays
                    .asList(
                        new SharedPrivateLinkResource()
                            .withName("testdbresource")
                            .withPrivateLinkResourceId(
                                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/workspace-1234/providers/Microsoft.DocumentDB/databaseAccounts/testdbresource/privateLinkResources/Sql")
                            .withGroupId("Sql")
                            .withRequestMessage("Please approve")
                            .withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)))
            .create();
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

### Workspaces_Delete

```java
import com.azure.core.util.Context;

/** Samples for Workspaces Delete. */
public final class WorkspacesDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Workspace/delete.json
     */
    /**
     * Sample code: Delete Workspace.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void deleteWorkspace(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.workspaces().delete("workspace-1234", "testworkspace", Context.NONE);
    }
}
```

### Workspaces_Diagnose

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.machinelearningservices.models.DiagnoseRequestProperties;
import com.azure.resourcemanager.machinelearningservices.models.DiagnoseWorkspaceParameters;
import java.util.HashMap;
import java.util.Map;

/** Samples for Workspaces Diagnose. */
public final class WorkspacesDiagnoseSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Workspace/diagnose.json
     */
    /**
     * Sample code: Diagnose Workspace.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void diagnoseWorkspace(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager
            .workspaces()
            .diagnose(
                "workspace-1234",
                "testworkspace",
                new DiagnoseWorkspaceParameters()
                    .withValue(
                        new DiagnoseRequestProperties()
                            .withUdr(mapOf())
                            .withNsg(mapOf())
                            .withResourceLock(mapOf())
                            .withDnsResolution(mapOf())
                            .withStorageAccount(mapOf())
                            .withKeyVault(mapOf())
                            .withContainerRegistry(mapOf())
                            .withApplicationInsights(mapOf())
                            .withOthers(mapOf())),
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

### Workspaces_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Workspaces GetByResourceGroup. */
public final class WorkspacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Workspace/get.json
     */
    /**
     * Sample code: Get Workspace.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void getWorkspace(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.workspaces().getByResourceGroupWithResponse("workspace-1234", "testworkspace", Context.NONE);
    }
}
```

### Workspaces_List

```java
import com.azure.core.util.Context;

/** Samples for Workspaces List. */
public final class WorkspacesListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Workspace/listBySubscription.json
     */
    /**
     * Sample code: Get Workspaces by subscription.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void getWorkspacesBySubscription(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.workspaces().list(null, Context.NONE);
    }
}
```

### Workspaces_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Workspaces ListByResourceGroup. */
public final class WorkspacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Workspace/listByResourceGroup.json
     */
    /**
     * Sample code: Get Workspaces by Resource Group.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void getWorkspacesByResourceGroup(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.workspaces().listByResourceGroup("workspace-1234", null, Context.NONE);
    }
}
```

### Workspaces_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for Workspaces ListKeys. */
public final class WorkspacesListKeysSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Workspace/listKeys.json
     */
    /**
     * Sample code: List Workspace Keys.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void listWorkspaceKeys(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.workspaces().listKeysWithResponse("testrg123", "workspaces123", Context.NONE);
    }
}
```

### Workspaces_ListNotebookAccessToken

```java
import com.azure.core.util.Context;

/** Samples for Workspaces ListNotebookAccessToken. */
public final class WorkspacesListNotebookAccessTokenSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Workspace/listNotebookAccessToken.json
     */
    /**
     * Sample code: List Workspace Keys.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void listWorkspaceKeys(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.workspaces().listNotebookAccessTokenWithResponse("workspace-1234", "testworkspace", Context.NONE);
    }
}
```

### Workspaces_ListNotebookKeys

```java
import com.azure.core.util.Context;

/** Samples for Workspaces ListNotebookKeys. */
public final class WorkspacesListNotebookKeysSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Notebook/listKeys.json
     */
    /**
     * Sample code: List Workspace Keys.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void listWorkspaceKeys(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.workspaces().listNotebookKeysWithResponse("testrg123", "workspaces123", Context.NONE);
    }
}
```

### Workspaces_ListOutboundNetworkDependenciesEndpoints

```java
import com.azure.core.util.Context;

/** Samples for Workspaces ListOutboundNetworkDependenciesEndpoints. */
public final class WorkspacesListOutboundNetworkDependenciesEndpointsSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/ExternalFQDN/get.json
     */
    /**
     * Sample code: ListOutboundNetworkDependenciesEndpoints.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void listOutboundNetworkDependenciesEndpoints(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager
            .workspaces()
            .listOutboundNetworkDependenciesEndpointsWithResponse("workspace-1234", "testworkspace", Context.NONE);
    }
}
```

### Workspaces_ListStorageAccountKeys

```java
import com.azure.core.util.Context;

/** Samples for Workspaces ListStorageAccountKeys. */
public final class WorkspacesListStorageAccountKeysSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Workspace/listStorageAccountKeys.json
     */
    /**
     * Sample code: List Workspace Keys.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void listWorkspaceKeys(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.workspaces().listStorageAccountKeysWithResponse("testrg123", "workspaces123", Context.NONE);
    }
}
```

### Workspaces_PrepareNotebook

```java
import com.azure.core.util.Context;

/** Samples for Workspaces PrepareNotebook. */
public final class WorkspacesPrepareNotebookSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Notebook/prepare.json
     */
    /**
     * Sample code: Prepare Notebook.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void prepareNotebook(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.workspaces().prepareNotebook("testrg123", "workspaces123", Context.NONE);
    }
}
```

### Workspaces_ResyncKeys

```java
import com.azure.core.util.Context;

/** Samples for Workspaces ResyncKeys. */
public final class WorkspacesResyncKeysSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Workspace/resyncKeys.json
     */
    /**
     * Sample code: Resync Workspace Keys.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void resyncWorkspaceKeys(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        manager.workspaces().resyncKeys("testrg123", "workspaces123", Context.NONE);
    }
}
```

### Workspaces_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.machinelearningservices.models.PublicNetworkAccess;
import com.azure.resourcemanager.machinelearningservices.models.Workspace;

/** Samples for Workspaces Update. */
public final class WorkspacesUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2021-07-01/examples/Workspace/update.json
     */
    /**
     * Sample code: Update Workspace.
     *
     * @param manager Entry point to MachineLearningServicesManager.
     */
    public static void updateWorkspace(
        com.azure.resourcemanager.machinelearningservices.MachineLearningServicesManager manager) {
        Workspace resource =
            manager
                .workspaces()
                .getByResourceGroupWithResponse("workspace-1234", "testworkspace", Context.NONE)
                .getValue();
        resource
            .update()
            .withDescription("new description")
            .withFriendlyName("New friendly name")
            .withPublicNetworkAccess(PublicNetworkAccess.DISABLED)
            .apply();
    }
}
```

