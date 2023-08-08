# Code snippets and samples


## AgentPool

- [CreateOrUpdate](#agentpool_createorupdate)
- [Delete](#agentpool_delete)
- [Get](#agentpool_get)
- [ListByProvisionedCluster](#agentpool_listbyprovisionedcluster)
- [Update](#agentpool_update)

## HybridContainerService

- [ListOrchestrators](#hybridcontainerservice_listorchestrators)
- [ListVMSkus](#hybridcontainerservice_listvmskus)

## HybridIdentityMetadata

- [Delete](#hybrididentitymetadata_delete)
- [Get](#hybrididentitymetadata_get)
- [ListByCluster](#hybrididentitymetadata_listbycluster)
- [Put](#hybrididentitymetadata_put)

## Operations

- [List](#operations_list)

## ProvisionedClustersOperation

- [CreateOrUpdate](#provisionedclustersoperation_createorupdate)
- [Delete](#provisionedclustersoperation_delete)
- [GetByResourceGroup](#provisionedclustersoperation_getbyresourcegroup)
- [GetUpgradeProfile](#provisionedclustersoperation_getupgradeprofile)
- [List](#provisionedclustersoperation_list)
- [ListByResourceGroup](#provisionedclustersoperation_listbyresourcegroup)
- [Update](#provisionedclustersoperation_update)
- [UpgradeNodeImageVersionForEntireCluster](#provisionedclustersoperation_upgradenodeimageversionforentirecluster)

## StorageSpacesOperation

- [CreateOrUpdate](#storagespacesoperation_createorupdate)
- [Delete](#storagespacesoperation_delete)
- [GetByResourceGroup](#storagespacesoperation_getbyresourcegroup)
- [List](#storagespacesoperation_list)
- [ListByResourceGroup](#storagespacesoperation_listbyresourcegroup)
- [Update](#storagespacesoperation_update)

## VirtualNetworksOperation

- [CreateOrUpdate](#virtualnetworksoperation_createorupdate)
- [Delete](#virtualnetworksoperation_delete)
- [GetByResourceGroup](#virtualnetworksoperation_getbyresourcegroup)
- [List](#virtualnetworksoperation_list)
- [ListByResourceGroup](#virtualnetworksoperation_listbyresourcegroup)
- [Update](#virtualnetworksoperation_update)
### AgentPool_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridcontainerservice.models.OsType;

/** Samples for AgentPool CreateOrUpdate. */
public final class AgentPoolCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/PutAgentPool.json
     */
    /**
     * Sample code: PutAgentPool.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void putAgentPool(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .agentPools()
            .define("test-hybridaksnodepool")
            .withRegion("westus")
            .withExistingProvisionedCluster("test-arcappliance-resgrp", "test-hybridakscluster")
            .withCount(1)
            .withOsType(OsType.LINUX)
            .withVmSize("Standard_A4_v2")
            .create();
    }
}
```

### AgentPool_Delete

```java
/** Samples for AgentPool Delete. */
public final class AgentPoolDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/DeleteAgentPool.json
     */
    /**
     * Sample code: DeleteAgentPool.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void deleteAgentPool(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .agentPools()
            .deleteWithResponse(
                "test-arcappliance-resgrp",
                "test-hybridakscluster",
                "test-hybridaksnodepool",
                com.azure.core.util.Context.NONE);
    }
}
```

### AgentPool_Get

```java
/** Samples for AgentPool Get. */
public final class AgentPoolGetSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/GetAgentPool.json
     */
    /**
     * Sample code: GetAgentPool.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void getAgentPool(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .agentPools()
            .getWithResponse(
                "test-arcappliance-resgrp",
                "test-hybridakscluster",
                "test-hybridaksnodepool",
                com.azure.core.util.Context.NONE);
    }
}
```

### AgentPool_ListByProvisionedCluster

```java
/** Samples for AgentPool ListByProvisionedCluster. */
public final class AgentPoolListByProvisionedClusterSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/ListAgentPoolByProvisionedCluster.json
     */
    /**
     * Sample code: ListAgentPoolByProvisionedCluster.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void listAgentPoolByProvisionedCluster(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .agentPools()
            .listByProvisionedClusterWithResponse(
                "test-arcappliance-resgrp", "test-hybridakscluster", com.azure.core.util.Context.NONE);
    }
}
```

### AgentPool_Update

```java
import com.azure.resourcemanager.hybridcontainerservice.models.AgentPool;

/** Samples for AgentPool Update. */
public final class AgentPoolUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/UpdateAgentPool.json
     */
    /**
     * Sample code: UpdateAgentPool.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void updateAgentPool(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        AgentPool resource =
            manager
                .agentPools()
                .getWithResponse(
                    "test-arcappliance-resgrp",
                    "test-hybridakscluster",
                    "test-hybridaksnodepool",
                    com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withCount(3).apply();
    }
}
```

### HybridContainerService_ListOrchestrators

```java
/** Samples for HybridContainerService ListOrchestrators. */
public final class HybridContainerServiceListOrchestratorsSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/ListOrchestrators.json
     */
    /**
     * Sample code: ListOrchestrators.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void listOrchestrators(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .hybridContainerServices()
            .listOrchestratorsWithResponse(
                "subscriptions/a3e42606-29b1-4d7d-b1d9-9ff6b9d3c71b/resourceGroups/test-arcappliance-resgrp/providers/Microsoft.ExtendedLocation/customLocations/testcustomlocation",
                com.azure.core.util.Context.NONE);
    }
}
```

### HybridContainerService_ListVMSkus

```java
/** Samples for HybridContainerService ListVMSkus. */
public final class HybridContainerServiceListVMSkusSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/ListVMSkus.json
     */
    /**
     * Sample code: ListVMSkus.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void listVMSkus(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .hybridContainerServices()
            .listVMSkusWithResponse(
                "subscriptions/a3e42606-29b1-4d7d-b1d9-9ff6b9d3c71b/resourceGroups/test-arcappliance-resgrp/providers/Microsoft.ExtendedLocation/customLocations/testcustomlocation",
                com.azure.core.util.Context.NONE);
    }
}
```

### HybridIdentityMetadata_Delete

```java
/** Samples for HybridIdentityMetadata Delete. */
public final class HybridIdentityMetadataDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/DeleteHybridIdentityMetadata.json
     */
    /**
     * Sample code: DeleteHybridIdentityMetadata.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void deleteHybridIdentityMetadata(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .hybridIdentityMetadatas()
            .deleteWithResponse("testrg", "ContosoTargetCluster", "default", com.azure.core.util.Context.NONE);
    }
}
```

### HybridIdentityMetadata_Get

```java
/** Samples for HybridIdentityMetadata Get. */
public final class HybridIdentityMetadataGetSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/GetHybridIdentityMetadata.json
     */
    /**
     * Sample code: GetHybridIdentityMetadata.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void getHybridIdentityMetadata(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .hybridIdentityMetadatas()
            .getWithResponse("testrg", "ContosoTargetCluster", "default", com.azure.core.util.Context.NONE);
    }
}
```

### HybridIdentityMetadata_ListByCluster

```java
/** Samples for HybridIdentityMetadata ListByCluster. */
public final class HybridIdentityMetadataListByClusterSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/HybridIdentityMetadataListByCluster.json
     */
    /**
     * Sample code: HybridIdentityMetadataListByCluster.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void hybridIdentityMetadataListByCluster(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .hybridIdentityMetadatas()
            .listByCluster("testrg", "ContosoTargetCluster", com.azure.core.util.Context.NONE);
    }
}
```

### HybridIdentityMetadata_Put

```java
/** Samples for HybridIdentityMetadata Put. */
public final class HybridIdentityMetadataPutSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/CreateHybridIdentityMetadata.json
     */
    /**
     * Sample code: CreateHybridIdentityMetadata.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void createHybridIdentityMetadata(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .hybridIdentityMetadatas()
            .define("default")
            .withExistingProvisionedCluster("testrg", "ContosoTargetCluster")
            .withResourceUid("f8b82dff-38ef-4220-99ef-d3a3f86ddc6c")
            .withPublicKey("8ec7d60c-9700-40b1-8e6e-e5b2f6f477f2")
            .create();
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/ListOperations.json
     */
    /**
     * Sample code: ListOperations.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void listOperations(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ProvisionedClustersOperation_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridcontainerservice.models.CloudProviderProfile;
import com.azure.resourcemanager.hybridcontainerservice.models.CloudProviderProfileInfraNetworkProfile;
import com.azure.resourcemanager.hybridcontainerservice.models.CloudProviderProfileInfraStorageProfile;
import com.azure.resourcemanager.hybridcontainerservice.models.ControlPlaneProfile;
import com.azure.resourcemanager.hybridcontainerservice.models.LinuxProfileProperties;
import com.azure.resourcemanager.hybridcontainerservice.models.LinuxProfilePropertiesSsh;
import com.azure.resourcemanager.hybridcontainerservice.models.LinuxProfilePropertiesSshPublicKeysItem;
import com.azure.resourcemanager.hybridcontainerservice.models.LoadBalancerProfile;
import com.azure.resourcemanager.hybridcontainerservice.models.LoadBalancerSku;
import com.azure.resourcemanager.hybridcontainerservice.models.NamedAgentPoolProfile;
import com.azure.resourcemanager.hybridcontainerservice.models.NetworkPolicy;
import com.azure.resourcemanager.hybridcontainerservice.models.NetworkProfile;
import com.azure.resourcemanager.hybridcontainerservice.models.OsType;
import com.azure.resourcemanager.hybridcontainerservice.models.ProvisionedClustersAllProperties;
import com.azure.resourcemanager.hybridcontainerservice.models.ProvisionedClustersExtendedLocation;
import java.util.Arrays;

/** Samples for ProvisionedClustersOperation CreateOrUpdate. */
public final class ProvisionedClustersOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/PutProvisionedCluster.json
     */
    /**
     * Sample code: PutProvisionedCluster.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void putProvisionedCluster(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .provisionedClustersOperations()
            .define("test-hybridakscluster")
            .withRegion("westus")
            .withExistingResourceGroup("test-arcappliance-resgrp")
            .withProperties(
                new ProvisionedClustersAllProperties()
                    .withLinuxProfile(
                        new LinuxProfileProperties()
                            .withSsh(
                                new LinuxProfilePropertiesSsh()
                                    .withPublicKeys(
                                        Arrays
                                            .asList(
                                                new LinuxProfilePropertiesSshPublicKeysItem()
                                                    .withKeyData("fakeTokenPlaceholder")))))
                    .withControlPlane(
                        new ControlPlaneProfile()
                            .withCount(1)
                            .withOsType(OsType.LINUX)
                            .withVmSize("Standard_A4_v2")
                            .withLinuxProfile(
                                new LinuxProfileProperties()
                                    .withSsh(
                                        new LinuxProfilePropertiesSsh()
                                            .withPublicKeys(
                                                Arrays
                                                    .asList(
                                                        new LinuxProfilePropertiesSshPublicKeysItem()
                                                            .withKeyData("fakeTokenPlaceholder"))))))
                    .withKubernetesVersion("v1.20.5")
                    .withNetworkProfile(
                        new NetworkProfile()
                            .withLoadBalancerProfile(
                                new LoadBalancerProfile()
                                    .withCount(1)
                                    .withOsType(OsType.LINUX)
                                    .withVmSize("Standard_K8S3_v1")
                                    .withLinuxProfile(
                                        new LinuxProfileProperties()
                                            .withSsh(
                                                new LinuxProfilePropertiesSsh()
                                                    .withPublicKeys(
                                                        Arrays
                                                            .asList(
                                                                new LinuxProfilePropertiesSshPublicKeysItem()
                                                                    .withKeyData("fakeTokenPlaceholder"))))))
                            .withLoadBalancerSku(LoadBalancerSku.UNSTACKED_HAPROXY)
                            .withNetworkPolicy(NetworkPolicy.CALICO)
                            .withPodCidr("10.244.0.0/16"))
                    .withAgentPoolProfiles(
                        Arrays
                            .asList(
                                new NamedAgentPoolProfile()
                                    .withCount(1)
                                    .withOsType(OsType.LINUX)
                                    .withVmSize("Standard_A4_v2")
                                    .withName("default-nodepool-1")))
                    .withCloudProviderProfile(
                        new CloudProviderProfile()
                            .withInfraNetworkProfile(
                                new CloudProviderProfileInfraNetworkProfile()
                                    .withVnetSubnetIds(
                                        Arrays
                                            .asList(
                                                "/subscriptions/a3e42606-29b1-4d7d-b1d9-9ff6b9d3c71b/resourceGroups/test-arcappliance-resgrp/providers/Microsoft.HybridContainerService/virtualNetworks/test-vnet-static")))
                            .withInfraStorageProfile(
                                new CloudProviderProfileInfraStorageProfile()
                                    .withStorageSpaceIds(
                                        Arrays
                                            .asList(
                                                "/subscriptions/a3e42606-29b1-4d7d-b1d9-9ff6b9d3c71b/resourceGroups/test-arcappliance-resgrp/providers/Microsoft.HybridContainerService/storageSpaces/test-storage")))))
            .withExtendedLocation(
                new ProvisionedClustersExtendedLocation()
                    .withType("CustomLocation")
                    .withName(
                        "/subscriptions/a3e42606-29b1-4d7d-b1d9-9ff6b9d3c71b/resourcegroups/test-arcappliance-resgrp/providers/microsoft.extendedlocation/customlocations/testcustomlocation"))
            .create();
    }
}
```

### ProvisionedClustersOperation_Delete

```java
/** Samples for ProvisionedClustersOperation Delete. */
public final class ProvisionedClustersOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/DeleteProvisionedCluster.json
     */
    /**
     * Sample code: DeleteProvisionedCluster.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void deleteProvisionedCluster(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .provisionedClustersOperations()
            .deleteByResourceGroupWithResponse(
                "test-arcappliance-resgrp", "test-hybridakscluster", com.azure.core.util.Context.NONE);
    }
}
```

### ProvisionedClustersOperation_GetByResourceGroup

```java
/** Samples for ProvisionedClustersOperation GetByResourceGroup. */
public final class ProvisionedClustersOperationGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/GetProvisionedCluster.json
     */
    /**
     * Sample code: GetProvisionedCluster.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void getProvisionedCluster(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .provisionedClustersOperations()
            .getByResourceGroupWithResponse(
                "test-arcappliance-resgrp", "test-hybridakscluster", com.azure.core.util.Context.NONE);
    }
}
```

### ProvisionedClustersOperation_GetUpgradeProfile

```java
/** Samples for ProvisionedClustersOperation GetUpgradeProfile. */
public final class ProvisionedClustersOperationGetUpgradeProfileSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/ProvisionedClustersGetUpgradeProfile.json
     */
    /**
     * Sample code: GetUpgradeProfileForProvisionedCluster.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void getUpgradeProfileForProvisionedCluster(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .provisionedClustersOperations()
            .getUpgradeProfileWithResponse(
                "test-arcappliance-resgrp", "test-hybridakscluster", com.azure.core.util.Context.NONE);
    }
}
```

### ProvisionedClustersOperation_List

```java
/** Samples for ProvisionedClustersOperation List. */
public final class ProvisionedClustersOperationListSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/ListProvisionedClusterBySubscription.json
     */
    /**
     * Sample code: ListProvisionedClusterBySubscription.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void listProvisionedClusterBySubscription(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager.provisionedClustersOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ProvisionedClustersOperation_ListByResourceGroup

```java
/** Samples for ProvisionedClustersOperation ListByResourceGroup. */
public final class ProvisionedClustersOperationListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/ListProvisionedClusterByResourceGroup.json
     */
    /**
     * Sample code: ListProvisionedClusterByResourceGroup.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void listProvisionedClusterByResourceGroup(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .provisionedClustersOperations()
            .listByResourceGroup("test-arcappliance-resgrp", com.azure.core.util.Context.NONE);
    }
}
```

### ProvisionedClustersOperation_Update

```java
import com.azure.resourcemanager.hybridcontainerservice.models.ProvisionedClustersResponse;
import java.util.HashMap;
import java.util.Map;

/** Samples for ProvisionedClustersOperation Update. */
public final class ProvisionedClustersOperationUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/UpdateProvisionedCluster.json
     */
    /**
     * Sample code: UpdateProvisionedCluster.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void updateProvisionedCluster(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        ProvisionedClustersResponse resource =
            manager
                .provisionedClustersOperations()
                .getByResourceGroupWithResponse(
                    "test-arcappliance-resgrp", "test-hybridakscluster", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("additionalProperties", "sample")).apply();
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

### ProvisionedClustersOperation_UpgradeNodeImageVersionForEntireCluster

```java
/** Samples for ProvisionedClustersOperation UpgradeNodeImageVersionForEntireCluster. */
public final class ProvisionedClustersOperationUpgradeNodeImageVersionForEntireClust {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/ProvisionedClustersUpgradeNodeImageVersionForEntireCluster.json
     */
    /**
     * Sample code: UpgradeClusterNodeImageVersion.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void upgradeClusterNodeImageVersion(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .provisionedClustersOperations()
            .upgradeNodeImageVersionForEntireCluster(
                "test-arcappliance-resgrp", "test-hybridakscluster", com.azure.core.util.Context.NONE);
    }
}
```

### StorageSpacesOperation_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridcontainerservice.models.StorageSpacesExtendedLocation;
import com.azure.resourcemanager.hybridcontainerservice.models.StorageSpacesProperties;
import com.azure.resourcemanager.hybridcontainerservice.models.StorageSpacesPropertiesHciStorageProfile;

/** Samples for StorageSpacesOperation CreateOrUpdate. */
public final class StorageSpacesOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/PutStorageSpace.json
     */
    /**
     * Sample code: PutStorageSpace.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void putStorageSpace(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .storageSpacesOperations()
            .define("test-storage")
            .withRegion("westus")
            .withExistingResourceGroup("test-arcappliance-resgrp")
            .withProperties(
                new StorageSpacesProperties()
                    .withHciStorageProfile(
                        new StorageSpacesPropertiesHciStorageProfile()
                            .withMocGroup("target-group")
                            .withMocLocation("MocLocation")
                            .withMocStorageContainer("WssdStorageContainer")))
            .withExtendedLocation(
                new StorageSpacesExtendedLocation()
                    .withType("CustomLocation")
                    .withName(
                        "/subscriptions/a3e42606-29b1-4d7d-b1d9-9ff6b9d3c71b/resourcegroups/test-arcappliance-resgrp/providers/microsoft.extendedlocation/customlocations/testcustomlocation"))
            .create();
    }
}
```

### StorageSpacesOperation_Delete

```java
/** Samples for StorageSpacesOperation Delete. */
public final class StorageSpacesOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/DeleteStorageSpace.json
     */
    /**
     * Sample code: DeleteStorageSpace.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void deleteStorageSpace(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .storageSpacesOperations()
            .deleteByResourceGroupWithResponse(
                "test-arcappliance-resgrp", "test-storage", com.azure.core.util.Context.NONE);
    }
}
```

### StorageSpacesOperation_GetByResourceGroup

```java
/** Samples for StorageSpacesOperation GetByResourceGroup. */
public final class StorageSpacesOperationGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/GetStorageSpace.json
     */
    /**
     * Sample code: GetStorageSpace.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void getStorageSpace(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .storageSpacesOperations()
            .getByResourceGroupWithResponse(
                "test-arcappliance-resgrp", "test-storage", com.azure.core.util.Context.NONE);
    }
}
```

### StorageSpacesOperation_List

```java
/** Samples for StorageSpacesOperation List. */
public final class StorageSpacesOperationListSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/ListStorageSpaceBySubscription.json
     */
    /**
     * Sample code: ListStorageSpaceBySubscription.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void listStorageSpaceBySubscription(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager.storageSpacesOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### StorageSpacesOperation_ListByResourceGroup

```java
/** Samples for StorageSpacesOperation ListByResourceGroup. */
public final class StorageSpacesOperationListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/ListStorageSpaceByResourceGroup.json
     */
    /**
     * Sample code: ListStorageSpaceByResourceGroup.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void listStorageSpaceByResourceGroup(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .storageSpacesOperations()
            .listByResourceGroup("test-arcappliance-resgrp", com.azure.core.util.Context.NONE);
    }
}
```

### StorageSpacesOperation_Update

```java
import com.azure.resourcemanager.hybridcontainerservice.models.StorageSpaces;
import java.util.HashMap;
import java.util.Map;

/** Samples for StorageSpacesOperation Update. */
public final class StorageSpacesOperationUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/UpdateStorageSpace.json
     */
    /**
     * Sample code: UpdateStorageSpace.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void updateStorageSpace(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        StorageSpaces resource =
            manager
                .storageSpacesOperations()
                .getByResourceGroupWithResponse(
                    "test-arcappliance-resgrp", "test-storage", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("additionalProperties", "sample")).apply();
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

### VirtualNetworksOperation_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridcontainerservice.models.VirtualNetworksExtendedLocation;
import com.azure.resourcemanager.hybridcontainerservice.models.VirtualNetworksProperties;
import com.azure.resourcemanager.hybridcontainerservice.models.VirtualNetworksPropertiesInfraVnetProfile;
import com.azure.resourcemanager.hybridcontainerservice.models.VirtualNetworksPropertiesInfraVnetProfileHci;
import com.azure.resourcemanager.hybridcontainerservice.models.VirtualNetworksPropertiesVipPoolItem;
import com.azure.resourcemanager.hybridcontainerservice.models.VirtualNetworksPropertiesVmipPoolItem;
import java.util.Arrays;

/** Samples for VirtualNetworksOperation CreateOrUpdate. */
public final class VirtualNetworksOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/PutVirtualNetwork.json
     */
    /**
     * Sample code: PutVirtualNetwork.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void putVirtualNetwork(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .virtualNetworksOperations()
            .define("test-vnet-static")
            .withRegion("westus")
            .withExistingResourceGroup("test-arcappliance-resgrp")
            .withProperties(
                new VirtualNetworksProperties()
                    .withInfraVnetProfile(
                        new VirtualNetworksPropertiesInfraVnetProfile()
                            .withHci(
                                new VirtualNetworksPropertiesInfraVnetProfileHci()
                                    .withMocGroup("target-group")
                                    .withMocLocation("MocLocation")
                                    .withMocVnetName("test-vnet")))
                    .withVipPool(
                        Arrays
                            .asList(
                                new VirtualNetworksPropertiesVipPoolItem()
                                    .withEndIp("192.168.0.50")
                                    .withStartIp("192.168.0.10")))
                    .withVmipPool(
                        Arrays
                            .asList(
                                new VirtualNetworksPropertiesVmipPoolItem()
                                    .withEndIp("192.168.0.130")
                                    .withStartIp("192.168.0.110"))))
            .withExtendedLocation(
                new VirtualNetworksExtendedLocation()
                    .withType("CustomLocation")
                    .withName(
                        "/subscriptions/a3e42606-29b1-4d7d-b1d9-9ff6b9d3c71b/resourcegroups/test-arcappliance-resgrp/providers/microsoft.extendedlocation/customlocations/testcustomlocation"))
            .create();
    }
}
```

### VirtualNetworksOperation_Delete

```java
/** Samples for VirtualNetworksOperation Delete. */
public final class VirtualNetworksOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/DeleteVirtualNetwork.json
     */
    /**
     * Sample code: DeleteVirtualNetwork.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void deleteVirtualNetwork(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .virtualNetworksOperations()
            .deleteByResourceGroupWithResponse(
                "test-arcappliance-resgrp", "test-vnet-static", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworksOperation_GetByResourceGroup

```java
/** Samples for VirtualNetworksOperation GetByResourceGroup. */
public final class VirtualNetworksOperationGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/GetVirtualNetwork.json
     */
    /**
     * Sample code: GetVirtualNetwork.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void getVirtualNetwork(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .virtualNetworksOperations()
            .getByResourceGroupWithResponse(
                "test-arcappliance-resgrp", "test-vnet-static", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworksOperation_List

```java
/** Samples for VirtualNetworksOperation List. */
public final class VirtualNetworksOperationListSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/ListVirtualNetworkBySubscription.json
     */
    /**
     * Sample code: ListVirtualNetworkBySubscription.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void listVirtualNetworkBySubscription(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager.virtualNetworksOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworksOperation_ListByResourceGroup

```java
/** Samples for VirtualNetworksOperation ListByResourceGroup. */
public final class VirtualNetworksOperationListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/ListVirtualNetworkByResourceGroup.json
     */
    /**
     * Sample code: ListVirtualNetworkByResourceGroup.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void listVirtualNetworkByResourceGroup(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .virtualNetworksOperations()
            .listByResourceGroup("test-arcappliance-resgrp", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworksOperation_Update

```java
import com.azure.resourcemanager.hybridcontainerservice.models.VirtualNetworks;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualNetworksOperation Update. */
public final class VirtualNetworksOperationUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2022-09-01-preview/examples/UpdateVirtualNetwork.json
     */
    /**
     * Sample code: UpdateVirtualNetwork.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void updateVirtualNetwork(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        VirtualNetworks resource =
            manager
                .virtualNetworksOperations()
                .getByResourceGroupWithResponse(
                    "test-arcappliance-resgrp", "test-vnet-static", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("additionalProperties", "sample")).apply();
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

