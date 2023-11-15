# Code snippets and samples


## AgentPool

- [CreateOrUpdate](#agentpool_createorupdate)
- [Delete](#agentpool_delete)
- [Get](#agentpool_get)
- [ListByProvisionedCluster](#agentpool_listbyprovisionedcluster)
- [Update](#agentpool_update)

## HybridIdentityMetadata

- [Delete](#hybrididentitymetadata_delete)
- [Get](#hybrididentitymetadata_get)
- [ListByCluster](#hybrididentitymetadata_listbycluster)
- [Put](#hybrididentitymetadata_put)

## KubernetesVersions

- [List](#kubernetesversions_list)

## Operations

- [List](#operations_list)

## ProvisionedClusterInstances

- [CreateOrUpdate](#provisionedclusterinstances_createorupdate)
- [Delete](#provisionedclusterinstances_delete)
- [Get](#provisionedclusterinstances_get)
- [GetUpgradeProfile](#provisionedclusterinstances_getupgradeprofile)
- [List](#provisionedclusterinstances_list)
- [ListAdminKubeconfig](#provisionedclusterinstances_listadminkubeconfig)
- [ListUserKubeconfig](#provisionedclusterinstances_listuserkubeconfig)

## ResourceProvider

- [DeleteKubernetesVersions](#resourceprovider_deletekubernetesversions)
- [DeleteVMSkus](#resourceprovider_deletevmskus)
- [GetKubernetesVersions](#resourceprovider_getkubernetesversions)
- [GetVMSkus](#resourceprovider_getvmskus)
- [PutKubernetesVersions](#resourceprovider_putkubernetesversions)
- [PutVMSkus](#resourceprovider_putvmskus)

## VMSkus

- [List](#vmskus_list)

## VirtualNetworks

- [CreateOrUpdate](#virtualnetworks_createorupdate)
- [Delete](#virtualnetworks_delete)
- [GetByResourceGroup](#virtualnetworks_getbyresourcegroup)
- [List](#virtualnetworks_list)
- [ListByResourceGroup](#virtualnetworks_listbyresourcegroup)
- [Update](#virtualnetworks_update)
### AgentPool_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridcontainerservice.models.OsType;

/** Samples for AgentPool CreateOrUpdate. */
public final class AgentPoolCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/PutAgentPool.json
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
            .withExistingConnectedClusterResourceUri(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.Kubernetes/connectedClusters/test-hybridakscluster")
            .withCount(1)
            .withVmSize("Standard_A4_v2")
            .withOsType(OsType.LINUX)
            .create();
    }
}
```

### AgentPool_Delete

```java
/** Samples for AgentPool Delete. */
public final class AgentPoolDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/DeleteAgentPool.json
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
            .delete(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.Kubernetes/connectedClusters/test-hybridakscluster",
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
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/GetAgentPool.json
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
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.Kubernetes/connectedClusters/test-hybridakscluster",
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
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/ListAgentPoolByProvisionedClusterInstance.json
     */
    /**
     * Sample code: ListAgentPoolByProvisionedClusterInstance.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void listAgentPoolByProvisionedClusterInstance(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .agentPools()
            .listByProvisionedClusterWithResponse(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.Kubernetes/connectedClusters/test-hybridakscluster",
                com.azure.core.util.Context.NONE);
    }
}
```

### AgentPool_Update

```java
import com.azure.resourcemanager.hybridcontainerservice.models.AgentPool;
import java.util.HashMap;
import java.util.Map;

/** Samples for AgentPool Update. */
public final class AgentPoolUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/UpdateAgentPool.json
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
                    "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.Kubernetes/connectedClusters/test-hybridakscluster",
                    "test-hybridaksnodepool",
                    com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("additionalProperties", "sample")).apply();
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

### HybridIdentityMetadata_Delete

```java
/** Samples for HybridIdentityMetadata Delete. */
public final class HybridIdentityMetadataDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/DeleteHybridIdentityMetadata.json
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
            .delete(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.Kubernetes/connectedClusters/test-hybridakscluster",
                com.azure.core.util.Context.NONE);
    }
}
```

### HybridIdentityMetadata_Get

```java
/** Samples for HybridIdentityMetadata Get. */
public final class HybridIdentityMetadataGetSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/GetHybridIdentityMetadata.json
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
            .getWithResponse(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.Kubernetes/connectedClusters/test-hybridakscluster",
                com.azure.core.util.Context.NONE);
    }
}
```

### HybridIdentityMetadata_ListByCluster

```java
/** Samples for HybridIdentityMetadata ListByCluster. */
public final class HybridIdentityMetadataListByClusterSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/HybridIdentityMetadataListByCluster.json
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
            .listByCluster(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.Kubernetes/connectedClusters/test-hybridakscluster",
                com.azure.core.util.Context.NONE);
    }
}
```

### HybridIdentityMetadata_Put

```java
import com.azure.resourcemanager.hybridcontainerservice.fluent.models.HybridIdentityMetadataInner;

/** Samples for HybridIdentityMetadata Put. */
public final class HybridIdentityMetadataPutSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/CreateHybridIdentityMetadata.json
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
            .putWithResponse(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.Kubernetes/connectedClusters/test-hybridakscluster",
                new HybridIdentityMetadataInner()
                    .withResourceUid("f8b82dff-38ef-4220-99ef-d3a3f86ddc6c")
                    .withPublicKey("fakeTokenPlaceholder"),
                com.azure.core.util.Context.NONE);
    }
}
```

### KubernetesVersions_List

```java
/** Samples for KubernetesVersions List. */
public final class KubernetesVersionsListSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/ListKubernetesVersions.json
     */
    /**
     * Sample code: ListKubernetesVersions.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void listKubernetesVersions(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .kubernetesVersions()
            .list(
                "subscriptions/a3e42606-29b1-4d7d-b1d9-9ff6b9d3c71b/resourceGroups/test-arcappliance-resgrp/providers/Microsoft.ExtendedLocation/customLocations/testcustomlocation",
                com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/ListOperations.json
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

### ProvisionedClusterInstances_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridcontainerservice.fluent.models.ProvisionedClustersInner;
import com.azure.resourcemanager.hybridcontainerservice.models.AzureHybridBenefit;
import com.azure.resourcemanager.hybridcontainerservice.models.CloudProviderProfile;
import com.azure.resourcemanager.hybridcontainerservice.models.CloudProviderProfileInfraNetworkProfile;
import com.azure.resourcemanager.hybridcontainerservice.models.ControlPlaneProfile;
import com.azure.resourcemanager.hybridcontainerservice.models.ExtendedLocation;
import com.azure.resourcemanager.hybridcontainerservice.models.ExtendedLocationTypes;
import com.azure.resourcemanager.hybridcontainerservice.models.LinuxProfileProperties;
import com.azure.resourcemanager.hybridcontainerservice.models.LinuxProfilePropertiesSsh;
import com.azure.resourcemanager.hybridcontainerservice.models.LinuxProfilePropertiesSshPublicKeysItem;
import com.azure.resourcemanager.hybridcontainerservice.models.NamedAgentPoolProfile;
import com.azure.resourcemanager.hybridcontainerservice.models.NetworkPolicy;
import com.azure.resourcemanager.hybridcontainerservice.models.NetworkProfile;
import com.azure.resourcemanager.hybridcontainerservice.models.OsType;
import com.azure.resourcemanager.hybridcontainerservice.models.ProvisionedClusterLicenseProfile;
import com.azure.resourcemanager.hybridcontainerservice.models.ProvisionedClusterProperties;
import java.util.Arrays;

/** Samples for ProvisionedClusterInstances CreateOrUpdate. */
public final class ProvisionedClusterInstancesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/PutProvisionedClusterInstance.json
     */
    /**
     * Sample code: PutProvisionedClusterInstance.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void putProvisionedClusterInstance(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .provisionedClusterInstances()
            .createOrUpdate(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.Kubernetes/connectedClusters/test-hybridakscluster",
                new ProvisionedClustersInner()
                    .withProperties(
                        new ProvisionedClusterProperties()
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
                                    .withOsType(OsType.LINUX)
                                    .withCount(1)
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
                                    .withNetworkPolicy(NetworkPolicy.CALICO)
                                    .withPodCidr("10.244.0.0/16"))
                            .withAgentPoolProfiles(
                                Arrays
                                    .asList(
                                        new NamedAgentPoolProfile()
                                            .withOsType(OsType.LINUX)
                                            .withCount(1)
                                            .withVmSize("Standard_A4_v2")
                                            .withName("default-nodepool-1")))
                            .withCloudProviderProfile(
                                new CloudProviderProfile()
                                    .withInfraNetworkProfile(
                                        new CloudProviderProfileInfraNetworkProfile()
                                            .withVnetSubnetIds(
                                                Arrays
                                                    .asList(
                                                        "/subscriptions/a3e42606-29b1-4d7d-b1d9-9ff6b9d3c71b/resourceGroups/test-arcappliance-resgrp/providers/Microsoft.AzureStackHCI/logicalNetworks/test-vnet-static"))))
                            .withLicenseProfile(
                                new ProvisionedClusterLicenseProfile()
                                    .withAzureHybridBenefit(AzureHybridBenefit.NOT_APPLICABLE)))
                    .withExtendedLocation(
                        new ExtendedLocation()
                            .withType(ExtendedLocationTypes.CUSTOM_LOCATION)
                            .withName(
                                "/subscriptions/a3e42606-29b1-4d7d-b1d9-9ff6b9d3c71b/resourcegroups/test-arcappliance-resgrp/providers/microsoft.extendedlocation/customlocations/testcustomlocation")),
                com.azure.core.util.Context.NONE);
    }
}
```

### ProvisionedClusterInstances_Delete

```java
/** Samples for ProvisionedClusterInstances Delete. */
public final class ProvisionedClusterInstancesDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/DeleteProvisionedClusterInstance.json
     */
    /**
     * Sample code: DeleteProvisionedClusterInstance.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void deleteProvisionedClusterInstance(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .provisionedClusterInstances()
            .delete(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.Kubernetes/connectedClusters/test-hybridakscluster",
                com.azure.core.util.Context.NONE);
    }
}
```

### ProvisionedClusterInstances_Get

```java
/** Samples for ProvisionedClusterInstances Get. */
public final class ProvisionedClusterInstancesGetSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/GetProvisionedClusterInstance.json
     */
    /**
     * Sample code: GetProvisionedClusterInstance.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void getProvisionedClusterInstance(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .provisionedClusterInstances()
            .getWithResponse(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.Kubernetes/connectedClusters/test-hybridakscluster",
                com.azure.core.util.Context.NONE);
    }
}
```

### ProvisionedClusterInstances_GetUpgradeProfile

```java
/** Samples for ProvisionedClusterInstances GetUpgradeProfile. */
public final class ProvisionedClusterInstancesGetUpgradeProfileSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/ProvisionedClusterInstanceGetUpgradeProfile.json
     */
    /**
     * Sample code: GetUpgradeProfileForProvisionedClusterInstance.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void getUpgradeProfileForProvisionedClusterInstance(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .provisionedClusterInstances()
            .getUpgradeProfileWithResponse(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.Kubernetes/connectedClusters/test-hybridakscluster",
                com.azure.core.util.Context.NONE);
    }
}
```

### ProvisionedClusterInstances_List

```java
/** Samples for ProvisionedClusterInstances List. */
public final class ProvisionedClusterInstancesListSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/ListProvisionedClusterInstances.json
     */
    /**
     * Sample code: ListProvisionedClusterInstances.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void listProvisionedClusterInstances(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .provisionedClusterInstances()
            .list(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.Kubernetes/connectedClusters/test-hybridakscluster",
                com.azure.core.util.Context.NONE);
    }
}
```

### ProvisionedClusterInstances_ListAdminKubeconfig

```java
/** Samples for ProvisionedClusterInstances ListAdminKubeconfig. */
public final class ProvisionedClusterInstancesListAdminKubeconfigSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/ProvisionedClusterInstanceListAdminKubeconfig.json
     */
    /**
     * Sample code: ListClusterAdminCredentials.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void listClusterAdminCredentials(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .provisionedClusterInstances()
            .listAdminKubeconfig(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.Kubernetes/connectedClusters/test-hybridakscluster",
                com.azure.core.util.Context.NONE);
    }
}
```

### ProvisionedClusterInstances_ListUserKubeconfig

```java
/** Samples for ProvisionedClusterInstances ListUserKubeconfig. */
public final class ProvisionedClusterInstancesListUserKubeconfigSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/ProvisionedClusterInstanceListUserKubeconfig.json
     */
    /**
     * Sample code: ListClusterUserCredentials.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void listClusterUserCredentials(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .provisionedClusterInstances()
            .listUserKubeconfig(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.Kubernetes/connectedClusters/test-hybridakscluster",
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_DeleteKubernetesVersions

```java
/** Samples for ResourceProvider DeleteKubernetesVersions. */
public final class ResourceProviderDeleteKubernetesVersionsSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/DeleteKubernetesVersions.json
     */
    /**
     * Sample code: DeleteKubernetesVersions.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void deleteKubernetesVersions(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .resourceProviders()
            .deleteKubernetesVersions(
                "subscriptions/a3e42606-29b1-4d7d-b1d9-9ff6b9d3c71b/resourceGroups/test-arcappliance-resgrp/providers/Microsoft.ExtendedLocation/customLocations/testcustomlocation",
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_DeleteVMSkus

```java
/** Samples for ResourceProvider DeleteVMSkus. */
public final class ResourceProviderDeleteVMSkusSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/DeleteVmSkus.json
     */
    /**
     * Sample code: DeleteVMSkus.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void deleteVMSkus(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .resourceProviders()
            .deleteVMSkus(
                "subscriptions/a3e42606-29b1-4d7d-b1d9-9ff6b9d3c71b/resourceGroups/test-arcappliance-resgrp/providers/Microsoft.ExtendedLocation/customLocations/testcustomlocation",
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_GetKubernetesVersions

```java
/** Samples for ResourceProvider GetKubernetesVersions. */
public final class ResourceProviderGetKubernetesVersionsSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/GetKubernetesVersions.json
     */
    /**
     * Sample code: GetKubernetesVersions.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void getKubernetesVersions(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .resourceProviders()
            .getKubernetesVersionsWithResponse(
                "subscriptions/a3e42606-29b1-4d7d-b1d9-9ff6b9d3c71b/resourceGroups/test-arcappliance-resgrp/providers/Microsoft.ExtendedLocation/customLocations/testcustomlocation",
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_GetVMSkus

```java
/** Samples for ResourceProvider GetVMSkus. */
public final class ResourceProviderGetVMSkusSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/GetVmSkus.json
     */
    /**
     * Sample code: GetVMSkus.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void getVMSkus(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .resourceProviders()
            .getVMSkusWithResponse(
                "subscriptions/a3e42606-29b1-4d7d-b1d9-9ff6b9d3c71b/resourceGroups/test-arcappliance-resgrp/providers/Microsoft.ExtendedLocation/customLocations/testcustomlocation",
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_PutKubernetesVersions

```java
import com.azure.resourcemanager.hybridcontainerservice.fluent.models.KubernetesVersionProfileInner;
import com.azure.resourcemanager.hybridcontainerservice.models.ExtendedLocation;
import com.azure.resourcemanager.hybridcontainerservice.models.ExtendedLocationTypes;

/** Samples for ResourceProvider PutKubernetesVersions. */
public final class ResourceProviderPutKubernetesVersionsSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/PutKubernetesVersions.json
     */
    /**
     * Sample code: PutKubernetesVersions.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void putKubernetesVersions(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .resourceProviders()
            .putKubernetesVersions(
                "subscriptions/a3e42606-29b1-4d7d-b1d9-9ff6b9d3c71b/resourceGroups/test-arcappliance-resgrp/providers/Microsoft.ExtendedLocation/customLocations/testcustomlocation",
                new KubernetesVersionProfileInner()
                    .withExtendedLocation(
                        new ExtendedLocation()
                            .withType(ExtendedLocationTypes.CUSTOM_LOCATION)
                            .withName(
                                "/subscriptions/a3e42606-29b1-4d7d-b1d9-9ff6b9d3c71b/resourcegroups/test-arcappliance-resgrp/providers/microsoft.extendedlocation/customlocations/testcustomlocation")),
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_PutVMSkus

```java
import com.azure.resourcemanager.hybridcontainerservice.fluent.models.VmSkuProfileInner;
import com.azure.resourcemanager.hybridcontainerservice.models.ExtendedLocation;
import com.azure.resourcemanager.hybridcontainerservice.models.ExtendedLocationTypes;

/** Samples for ResourceProvider PutVMSkus. */
public final class ResourceProviderPutVMSkusSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/PutVmSkus.json
     */
    /**
     * Sample code: PutVMSkus.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void putVMSkus(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .resourceProviders()
            .putVMSkus(
                "subscriptions/a3e42606-29b1-4d7d-b1d9-9ff6b9d3c71b/resourceGroups/test-arcappliance-resgrp/providers/Microsoft.ExtendedLocation/customLocations/testcustomlocation",
                new VmSkuProfileInner()
                    .withExtendedLocation(
                        new ExtendedLocation()
                            .withType(ExtendedLocationTypes.CUSTOM_LOCATION)
                            .withName(
                                "/subscriptions/a3e42606-29b1-4d7d-b1d9-9ff6b9d3c71b/resourcegroups/test-arcappliance-resgrp/providers/microsoft.extendedlocation/customlocations/testcustomlocation")),
                com.azure.core.util.Context.NONE);
    }
}
```

### VMSkus_List

```java
/** Samples for VMSkus List. */
public final class VMSkusListSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/ListVmSkus.json
     */
    /**
     * Sample code: ListVmSkus.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void listVmSkus(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .vMSkus()
            .list(
                "subscriptions/a3e42606-29b1-4d7d-b1d9-9ff6b9d3c71b/resourceGroups/test-arcappliance-resgrp/providers/Microsoft.ExtendedLocation/customLocations/testcustomlocation",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworks_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridcontainerservice.models.VirtualNetworkExtendedLocation;
import com.azure.resourcemanager.hybridcontainerservice.models.VirtualNetworkProperties;
import com.azure.resourcemanager.hybridcontainerservice.models.VirtualNetworkPropertiesInfraVnetProfile;
import com.azure.resourcemanager.hybridcontainerservice.models.VirtualNetworkPropertiesInfraVnetProfileVmware;
import com.azure.resourcemanager.hybridcontainerservice.models.VirtualNetworkPropertiesVipPoolItem;
import com.azure.resourcemanager.hybridcontainerservice.models.VirtualNetworkPropertiesVmipPoolItem;
import java.util.Arrays;

/** Samples for VirtualNetworks CreateOrUpdate. */
public final class VirtualNetworksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/PutVirtualNetwork.json
     */
    /**
     * Sample code: PutVirtualNetwork.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void putVirtualNetwork(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .virtualNetworks()
            .define("test-vnet-static")
            .withRegion("westus")
            .withExistingResourceGroup("test-arcappliance-resgrp")
            .withProperties(
                new VirtualNetworkProperties()
                    .withInfraVnetProfile(
                        new VirtualNetworkPropertiesInfraVnetProfile()
                            .withVmware(
                                new VirtualNetworkPropertiesInfraVnetProfileVmware().withSegmentName("test-network")))
                    .withVipPool(
                        Arrays
                            .asList(
                                new VirtualNetworkPropertiesVipPoolItem()
                                    .withEndIp("192.168.0.50")
                                    .withStartIp("192.168.0.10")))
                    .withVmipPool(
                        Arrays
                            .asList(
                                new VirtualNetworkPropertiesVmipPoolItem()
                                    .withEndIp("192.168.0.130")
                                    .withStartIp("192.168.0.110")))
                    .withDnsServers(Arrays.asList("192.168.0.1"))
                    .withGateway("192.168.0.1")
                    .withIpAddressPrefix("192.168.0.0/16")
                    .withVlanId(10))
            .withExtendedLocation(
                new VirtualNetworkExtendedLocation()
                    .withType("CustomLocation")
                    .withName(
                        "/subscriptions/a3e42606-29b1-4d7d-b1d9-9ff6b9d3c71b/resourcegroups/test-arcappliance-resgrp/providers/microsoft.extendedlocation/customlocations/testcustomlocation"))
            .create();
    }
}
```

### VirtualNetworks_Delete

```java
/** Samples for VirtualNetworks Delete. */
public final class VirtualNetworksDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/DeleteVirtualNetwork.json
     */
    /**
     * Sample code: DeleteVirtualNetwork.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void deleteVirtualNetwork(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .virtualNetworks()
            .delete("test-arcappliance-resgrp", "test-vnet-static", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworks_GetByResourceGroup

```java
/** Samples for VirtualNetworks GetByResourceGroup. */
public final class VirtualNetworksGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/GetVirtualNetwork.json
     */
    /**
     * Sample code: GetVirtualNetwork.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void getVirtualNetwork(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager
            .virtualNetworks()
            .getByResourceGroupWithResponse(
                "test-arcappliance-resgrp", "test-vnet-static", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworks_List

```java
/** Samples for VirtualNetworks List. */
public final class VirtualNetworksListSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/ListVirtualNetworkBySubscription.json
     */
    /**
     * Sample code: ListVirtualNetworkBySubscription.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void listVirtualNetworkBySubscription(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager.virtualNetworks().list(com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworks_ListByResourceGroup

```java
/** Samples for VirtualNetworks ListByResourceGroup. */
public final class VirtualNetworksListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/ListVirtualNetworkByResourceGroup.json
     */
    /**
     * Sample code: ListVirtualNetworkByResourceGroup.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void listVirtualNetworkByResourceGroup(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        manager.virtualNetworks().listByResourceGroup("test-arcappliance-resgrp", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworks_Update

```java
import com.azure.resourcemanager.hybridcontainerservice.models.VirtualNetwork;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualNetworks Update. */
public final class VirtualNetworksUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridaks/resource-manager/Microsoft.HybridContainerService/preview/2023-11-15-preview/examples/UpdateVirtualNetwork.json
     */
    /**
     * Sample code: UpdateVirtualNetwork.
     *
     * @param manager Entry point to HybridContainerServiceManager.
     */
    public static void updateVirtualNetwork(
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager) {
        VirtualNetwork resource =
            manager
                .virtualNetworks()
                .getByResourceGroupWithResponse(
                    "test-arcappliance-resgrp", "test-vnet-static", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("additionalProperties", "sample")).apply();
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

