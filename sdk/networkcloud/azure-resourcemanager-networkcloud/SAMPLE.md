# Code snippets and samples


## AgentPools

- [CreateOrUpdate](#agentpools_createorupdate)
- [Delete](#agentpools_delete)
- [Get](#agentpools_get)
- [ListByKubernetesCluster](#agentpools_listbykubernetescluster)
- [Update](#agentpools_update)

## BareMetalMachineKeySets

- [CreateOrUpdate](#baremetalmachinekeysets_createorupdate)
- [Delete](#baremetalmachinekeysets_delete)
- [Get](#baremetalmachinekeysets_get)
- [ListByCluster](#baremetalmachinekeysets_listbycluster)
- [Update](#baremetalmachinekeysets_update)

## BareMetalMachines

- [Cordon](#baremetalmachines_cordon)
- [CreateOrUpdate](#baremetalmachines_createorupdate)
- [Delete](#baremetalmachines_delete)
- [GetByResourceGroup](#baremetalmachines_getbyresourcegroup)
- [List](#baremetalmachines_list)
- [ListByResourceGroup](#baremetalmachines_listbyresourcegroup)
- [PowerOff](#baremetalmachines_poweroff)
- [Reimage](#baremetalmachines_reimage)
- [Replace](#baremetalmachines_replace)
- [Restart](#baremetalmachines_restart)
- [RunCommand](#baremetalmachines_runcommand)
- [RunDataExtracts](#baremetalmachines_rundataextracts)
- [RunReadCommands](#baremetalmachines_runreadcommands)
- [Start](#baremetalmachines_start)
- [Uncordon](#baremetalmachines_uncordon)
- [Update](#baremetalmachines_update)
- [ValidateHardware](#baremetalmachines_validatehardware)

## BmcKeySets

- [CreateOrUpdate](#bmckeysets_createorupdate)
- [Delete](#bmckeysets_delete)
- [Get](#bmckeysets_get)
- [ListByCluster](#bmckeysets_listbycluster)
- [Update](#bmckeysets_update)

## CloudServicesNetworks

- [CreateOrUpdate](#cloudservicesnetworks_createorupdate)
- [Delete](#cloudservicesnetworks_delete)
- [GetByResourceGroup](#cloudservicesnetworks_getbyresourcegroup)
- [List](#cloudservicesnetworks_list)
- [ListByResourceGroup](#cloudservicesnetworks_listbyresourcegroup)
- [Update](#cloudservicesnetworks_update)

## ClusterManagers

- [CreateOrUpdate](#clustermanagers_createorupdate)
- [Delete](#clustermanagers_delete)
- [GetByResourceGroup](#clustermanagers_getbyresourcegroup)
- [List](#clustermanagers_list)
- [ListByResourceGroup](#clustermanagers_listbyresourcegroup)
- [Update](#clustermanagers_update)

## Clusters

- [CreateOrUpdate](#clusters_createorupdate)
- [Delete](#clusters_delete)
- [Deploy](#clusters_deploy)
- [GetByResourceGroup](#clusters_getbyresourcegroup)
- [List](#clusters_list)
- [ListByResourceGroup](#clusters_listbyresourcegroup)
- [Update](#clusters_update)
- [UpdateVersion](#clusters_updateversion)

## Consoles

- [CreateOrUpdate](#consoles_createorupdate)
- [Delete](#consoles_delete)
- [Get](#consoles_get)
- [ListByVirtualMachine](#consoles_listbyvirtualmachine)
- [Update](#consoles_update)

## KubernetesClusters

- [CreateOrUpdate](#kubernetesclusters_createorupdate)
- [Delete](#kubernetesclusters_delete)
- [GetByResourceGroup](#kubernetesclusters_getbyresourcegroup)
- [List](#kubernetesclusters_list)
- [ListByResourceGroup](#kubernetesclusters_listbyresourcegroup)
- [RestartNode](#kubernetesclusters_restartnode)
- [Update](#kubernetesclusters_update)

## L2Networks

- [CreateOrUpdate](#l2networks_createorupdate)
- [Delete](#l2networks_delete)
- [GetByResourceGroup](#l2networks_getbyresourcegroup)
- [List](#l2networks_list)
- [ListByResourceGroup](#l2networks_listbyresourcegroup)
- [Update](#l2networks_update)

## L3Networks

- [CreateOrUpdate](#l3networks_createorupdate)
- [Delete](#l3networks_delete)
- [GetByResourceGroup](#l3networks_getbyresourcegroup)
- [List](#l3networks_list)
- [ListByResourceGroup](#l3networks_listbyresourcegroup)
- [Update](#l3networks_update)

## MetricsConfigurations

- [CreateOrUpdate](#metricsconfigurations_createorupdate)
- [Delete](#metricsconfigurations_delete)
- [Get](#metricsconfigurations_get)
- [ListByCluster](#metricsconfigurations_listbycluster)
- [Update](#metricsconfigurations_update)

## Operations

- [List](#operations_list)

## RackSkus

- [Get](#rackskus_get)
- [List](#rackskus_list)

## Racks

- [CreateOrUpdate](#racks_createorupdate)
- [Delete](#racks_delete)
- [GetByResourceGroup](#racks_getbyresourcegroup)
- [List](#racks_list)
- [ListByResourceGroup](#racks_listbyresourcegroup)
- [Update](#racks_update)

## StorageAppliances

- [CreateOrUpdate](#storageappliances_createorupdate)
- [Delete](#storageappliances_delete)
- [DisableRemoteVendorManagement](#storageappliances_disableremotevendormanagement)
- [EnableRemoteVendorManagement](#storageappliances_enableremotevendormanagement)
- [GetByResourceGroup](#storageappliances_getbyresourcegroup)
- [List](#storageappliances_list)
- [ListByResourceGroup](#storageappliances_listbyresourcegroup)
- [RunReadCommands](#storageappliances_runreadcommands)
- [Update](#storageappliances_update)

## TrunkedNetworks

- [CreateOrUpdate](#trunkednetworks_createorupdate)
- [Delete](#trunkednetworks_delete)
- [GetByResourceGroup](#trunkednetworks_getbyresourcegroup)
- [List](#trunkednetworks_list)
- [ListByResourceGroup](#trunkednetworks_listbyresourcegroup)
- [Update](#trunkednetworks_update)

## VirtualMachines

- [AttachVolume](#virtualmachines_attachvolume)
- [CreateOrUpdate](#virtualmachines_createorupdate)
- [Delete](#virtualmachines_delete)
- [DetachVolume](#virtualmachines_detachvolume)
- [GetByResourceGroup](#virtualmachines_getbyresourcegroup)
- [List](#virtualmachines_list)
- [ListByResourceGroup](#virtualmachines_listbyresourcegroup)
- [PowerOff](#virtualmachines_poweroff)
- [Reimage](#virtualmachines_reimage)
- [Restart](#virtualmachines_restart)
- [Start](#virtualmachines_start)
- [Update](#virtualmachines_update)

## Volumes

- [CreateOrUpdate](#volumes_createorupdate)
- [Delete](#volumes_delete)
- [GetByResourceGroup](#volumes_getbyresourcegroup)
- [List](#volumes_list)
- [ListByResourceGroup](#volumes_listbyresourcegroup)
- [Update](#volumes_update)
### AgentPools_CreateOrUpdate

```java
import com.azure.resourcemanager.networkcloud.models.AdministratorConfiguration;
import com.azure.resourcemanager.networkcloud.models.AgentOptions;
import com.azure.resourcemanager.networkcloud.models.AgentPoolMode;
import com.azure.resourcemanager.networkcloud.models.AgentPoolUpgradeSettings;
import com.azure.resourcemanager.networkcloud.models.AttachedNetworkConfiguration;
import com.azure.resourcemanager.networkcloud.models.ExtendedLocation;
import com.azure.resourcemanager.networkcloud.models.HugepagesSize;
import com.azure.resourcemanager.networkcloud.models.KubernetesLabel;
import com.azure.resourcemanager.networkcloud.models.KubernetesPluginType;
import com.azure.resourcemanager.networkcloud.models.L2NetworkAttachmentConfiguration;
import com.azure.resourcemanager.networkcloud.models.L3NetworkAttachmentConfiguration;
import com.azure.resourcemanager.networkcloud.models.L3NetworkConfigurationIpamEnabled;
import com.azure.resourcemanager.networkcloud.models.SshPublicKey;
import com.azure.resourcemanager.networkcloud.models.TrunkedNetworkAttachmentConfiguration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for AgentPools CreateOrUpdate. */
public final class AgentPoolsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/AgentPools_Create.json
     */
    /**
     * Sample code: Create or update Kubernetes cluster agent pool.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void createOrUpdateKubernetesClusterAgentPool(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .agentPools()
            .define("agentPoolName")
            .withRegion("location")
            .withExistingKubernetesCluster("resourceGroupName", "kubernetesClusterName")
            .withCount(3L)
            .withMode(AgentPoolMode.SYSTEM)
            .withVmSkuName("NC_M16_v1")
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ExtendedLocation/customLocations/clusterExtendedLocationName")
                    .withType("CustomLocation"))
            .withAdministratorConfiguration(
                new AdministratorConfiguration()
                    .withAdminUsername("azure")
                    .withSshPublicKeys(Arrays.asList(new SshPublicKey().withKeyData("fakeTokenPlaceholder"))))
            .withAgentOptions(new AgentOptions().withHugepagesCount(96L).withHugepagesSize(HugepagesSize.ONEG))
            .withAttachedNetworkConfiguration(
                new AttachedNetworkConfiguration()
                    .withL2Networks(
                        Arrays
                            .asList(
                                new L2NetworkAttachmentConfiguration()
                                    .withNetworkId(
                                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.NetworkCloud/l2Networks/l2NetworkName")
                                    .withPluginType(KubernetesPluginType.DPDK)))
                    .withL3Networks(
                        Arrays
                            .asList(
                                new L3NetworkAttachmentConfiguration()
                                    .withIpamEnabled(L3NetworkConfigurationIpamEnabled.FALSE)
                                    .withNetworkId(
                                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.NetworkCloud/l3Networks/l3NetworkName")
                                    .withPluginType(KubernetesPluginType.SRIOV)))
                    .withTrunkedNetworks(
                        Arrays
                            .asList(
                                new TrunkedNetworkAttachmentConfiguration()
                                    .withNetworkId(
                                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.NetworkCloud/trunkedNetworks/trunkedNetworkName")
                                    .withPluginType(KubernetesPluginType.MACVLAN))))
            .withAvailabilityZones(Arrays.asList("1", "2", "3"))
            .withLabels(Arrays.asList(new KubernetesLabel().withKey("fakeTokenPlaceholder").withValue("true")))
            .withTaints(Arrays.asList(new KubernetesLabel().withKey("fakeTokenPlaceholder").withValue("true")))
            .withUpgradeSettings(new AgentPoolUpgradeSettings().withMaxSurge("1"))
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

### AgentPools_Delete

```java
/** Samples for AgentPools Delete. */
public final class AgentPoolsDeleteSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/AgentPools_Delete.json
     */
    /**
     * Sample code: Delete Kubernetes cluster agent pool.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void deleteKubernetesClusterAgentPool(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .agentPools()
            .delete("resourceGroupName", "kubernetesClusterName", "agentPoolName", com.azure.core.util.Context.NONE);
    }
}
```

### AgentPools_Get

```java
/** Samples for AgentPools Get. */
public final class AgentPoolsGetSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/AgentPools_Get.json
     */
    /**
     * Sample code: Get Kubernetes cluster agent pool.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void getKubernetesClusterAgentPool(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .agentPools()
            .getWithResponse(
                "resourceGroupName", "kubernetesClusterName", "agentPoolName", com.azure.core.util.Context.NONE);
    }
}
```

### AgentPools_ListByKubernetesCluster

```java
/** Samples for AgentPools ListByKubernetesCluster. */
public final class AgentPoolsListByKubernetesClusterSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/AgentPools_ListByKubernetesCluster.json
     */
    /**
     * Sample code: List agent pools of the Kubernetes cluster.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listAgentPoolsOfTheKubernetesCluster(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .agentPools()
            .listByKubernetesCluster("resourceGroupName", "kubernetesClusterName", com.azure.core.util.Context.NONE);
    }
}
```

### AgentPools_Update

```java
import com.azure.resourcemanager.networkcloud.models.AgentPool;
import com.azure.resourcemanager.networkcloud.models.AgentPoolUpgradeSettings;
import java.util.HashMap;
import java.util.Map;

/** Samples for AgentPools Update. */
public final class AgentPoolsUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/AgentPools_Patch.json
     */
    /**
     * Sample code: Patch Kubernetes cluster agent pool.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void patchKubernetesClusterAgentPool(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        AgentPool resource =
            manager
                .agentPools()
                .getWithResponse(
                    "resourceGroupName", "kubernetesClusterName", "agentPoolName", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
            .withCount(3L)
            .withUpgradeSettings(new AgentPoolUpgradeSettings().withMaxSurge("1"))
            .apply();
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

### BareMetalMachineKeySets_CreateOrUpdate

```java
import com.azure.resourcemanager.networkcloud.models.BareMetalMachineKeySetPrivilegeLevel;
import com.azure.resourcemanager.networkcloud.models.ExtendedLocation;
import com.azure.resourcemanager.networkcloud.models.KeySetUser;
import com.azure.resourcemanager.networkcloud.models.SshPublicKey;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for BareMetalMachineKeySets CreateOrUpdate. */
public final class BareMetalMachineKeySetsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BareMetalMachineKeySets_Create.json
     */
    /**
     * Sample code: Create or update bare metal machine key set of cluster.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void createOrUpdateBareMetalMachineKeySetOfCluster(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .bareMetalMachineKeySets()
            .define("bareMetalMachineKeySetName")
            .withRegion("location")
            .withExistingCluster("resourceGroupName", "clusterName")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ExtendedLocation/customLocations/clusterExtendedLocationName")
                    .withType("CustomLocation"))
            .withAzureGroupId("f110271b-XXXX-4163-9b99-214d91660f0e")
            .withExpiration(OffsetDateTime.parse("2022-12-31T23:59:59.008Z"))
            .withJumpHostsAllowed(Arrays.asList("192.0.2.1", "192.0.2.5"))
            .withPrivilegeLevel(BareMetalMachineKeySetPrivilegeLevel.STANDARD)
            .withUserList(
                Arrays
                    .asList(
                        new KeySetUser()
                            .withAzureUsername("userABC")
                            .withDescription("Needs access for troubleshooting as a part of the support team")
                            .withSshPublicKey(new SshPublicKey().withKeyData("fakeTokenPlaceholder")),
                        new KeySetUser()
                            .withAzureUsername("userXYZ")
                            .withDescription("Needs access for troubleshooting as a part of the support team")
                            .withSshPublicKey(new SshPublicKey().withKeyData("fakeTokenPlaceholder"))))
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
            .withOsGroupName("standardAccessGroup")
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

### BareMetalMachineKeySets_Delete

```java
/** Samples for BareMetalMachineKeySets Delete. */
public final class BareMetalMachineKeySetsDeleteSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BareMetalMachineKeySets_Delete.json
     */
    /**
     * Sample code: Delete bare metal machine key set of cluster.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void deleteBareMetalMachineKeySetOfCluster(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .bareMetalMachineKeySets()
            .delete("resourceGroupName", "clusterName", "bareMetalMachineKeySetName", com.azure.core.util.Context.NONE);
    }
}
```

### BareMetalMachineKeySets_Get

```java
/** Samples for BareMetalMachineKeySets Get. */
public final class BareMetalMachineKeySetsGetSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BareMetalMachineKeySets_Get.json
     */
    /**
     * Sample code: Get bare metal machine key set of cluster.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void getBareMetalMachineKeySetOfCluster(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .bareMetalMachineKeySets()
            .getWithResponse(
                "resourceGroupName", "clusterName", "bareMetalMachineKeySetName", com.azure.core.util.Context.NONE);
    }
}
```

### BareMetalMachineKeySets_ListByCluster

```java
/** Samples for BareMetalMachineKeySets ListByCluster. */
public final class BareMetalMachineKeySetsListByClusterSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BareMetalMachineKeySets_ListByCluster.json
     */
    /**
     * Sample code: List bare metal machine key sets of the cluster.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listBareMetalMachineKeySetsOfTheCluster(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .bareMetalMachineKeySets()
            .listByCluster("resourceGroupName", "clusterName", com.azure.core.util.Context.NONE);
    }
}
```

### BareMetalMachineKeySets_Update

```java
import com.azure.resourcemanager.networkcloud.models.BareMetalMachineKeySet;
import com.azure.resourcemanager.networkcloud.models.KeySetUser;
import com.azure.resourcemanager.networkcloud.models.SshPublicKey;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for BareMetalMachineKeySets Update. */
public final class BareMetalMachineKeySetsUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BareMetalMachineKeySets_Patch.json
     */
    /**
     * Sample code: Patch bare metal machine key set of cluster.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void patchBareMetalMachineKeySetOfCluster(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        BareMetalMachineKeySet resource =
            manager
                .bareMetalMachineKeySets()
                .getWithResponse(
                    "resourceGroupName", "clusterName", "bareMetalMachineKeySetName", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
            .withExpiration(OffsetDateTime.parse("2022-12-31T23:59:59.008Z"))
            .withJumpHostsAllowed(Arrays.asList("192.0.2.1", "192.0.2.5"))
            .withUserList(
                Arrays
                    .asList(
                        new KeySetUser()
                            .withAzureUsername("userABC")
                            .withDescription("Needs access for troubleshooting as a part of the support team")
                            .withSshPublicKey(new SshPublicKey().withKeyData("fakeTokenPlaceholder")),
                        new KeySetUser()
                            .withAzureUsername("userXYZ")
                            .withDescription("Needs access for troubleshooting as a part of the support team")
                            .withSshPublicKey(new SshPublicKey().withKeyData("fakeTokenPlaceholder"))))
            .apply();
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

### BareMetalMachines_Cordon

```java
import com.azure.resourcemanager.networkcloud.models.BareMetalMachineCordonParameters;
import com.azure.resourcemanager.networkcloud.models.BareMetalMachineEvacuate;

/** Samples for BareMetalMachines Cordon. */
public final class BareMetalMachinesCordonSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BareMetalMachines_Cordon.json
     */
    /**
     * Sample code: Cordon bare metal machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void cordonBareMetalMachine(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .bareMetalMachines()
            .cordon(
                "resourceGroupName",
                "bareMetalMachineName",
                new BareMetalMachineCordonParameters().withEvacuate(BareMetalMachineEvacuate.TRUE),
                com.azure.core.util.Context.NONE);
    }
}
```

### BareMetalMachines_CreateOrUpdate

```java
import com.azure.resourcemanager.networkcloud.models.AdministrativeCredentials;
import com.azure.resourcemanager.networkcloud.models.ExtendedLocation;
import java.util.HashMap;
import java.util.Map;

/** Samples for BareMetalMachines CreateOrUpdate. */
public final class BareMetalMachinesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BareMetalMachines_Create.json
     */
    /**
     * Sample code: Create or update bare metal machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void createOrUpdateBareMetalMachine(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .bareMetalMachines()
            .define("bareMetalMachineName")
            .withRegion("location")
            .withExistingResourceGroup("resourceGroupName")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ExtendedLocation/customLocations/clusterExtendedLocationName")
                    .withType("CustomLocation"))
            .withBmcConnectionString("bmcconnectionstring")
            .withBmcCredentials(
                new AdministrativeCredentials().withPassword("fakeTokenPlaceholder").withUsername("bmcuser"))
            .withBmcMacAddress("00:00:4f:00:57:00")
            .withBootMacAddress("00:00:4e:00:58:af")
            .withMachineDetails("User-provided machine details.")
            .withMachineName("r01c001")
            .withMachineSkuId("684E-3B16-399E")
            .withRackId(
                "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.NetworkCloud/racks/rackName")
            .withRackSlot(1L)
            .withSerialNumber("BM1219XXX")
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
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

### BareMetalMachines_Delete

```java
/** Samples for BareMetalMachines Delete. */
public final class BareMetalMachinesDeleteSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BareMetalMachines_Delete.json
     */
    /**
     * Sample code: Delete bare metal machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void deleteBareMetalMachine(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .bareMetalMachines()
            .delete("resourceGroupName", "bareMetalMachineName", com.azure.core.util.Context.NONE);
    }
}
```

### BareMetalMachines_GetByResourceGroup

```java
/** Samples for BareMetalMachines GetByResourceGroup. */
public final class BareMetalMachinesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BareMetalMachines_Get.json
     */
    /**
     * Sample code: Get bare metal machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void getBareMetalMachine(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .bareMetalMachines()
            .getByResourceGroupWithResponse(
                "resourceGroupName", "bareMetalMachineName", com.azure.core.util.Context.NONE);
    }
}
```

### BareMetalMachines_List

```java
/** Samples for BareMetalMachines List. */
public final class BareMetalMachinesListSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BareMetalMachines_ListBySubscription.json
     */
    /**
     * Sample code: List bare metal machines for subscription.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listBareMetalMachinesForSubscription(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.bareMetalMachines().list(com.azure.core.util.Context.NONE);
    }
}
```

### BareMetalMachines_ListByResourceGroup

```java
/** Samples for BareMetalMachines ListByResourceGroup. */
public final class BareMetalMachinesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BareMetalMachines_ListByResourceGroup.json
     */
    /**
     * Sample code: List bare metal machines for resource group.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listBareMetalMachinesForResourceGroup(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.bareMetalMachines().listByResourceGroup("resourceGroupName", com.azure.core.util.Context.NONE);
    }
}
```

### BareMetalMachines_PowerOff

```java
import com.azure.resourcemanager.networkcloud.models.BareMetalMachinePowerOffParameters;
import com.azure.resourcemanager.networkcloud.models.BareMetalMachineSkipShutdown;

/** Samples for BareMetalMachines PowerOff. */
public final class BareMetalMachinesPowerOffSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BareMetalMachines_PowerOff.json
     */
    /**
     * Sample code: Power off bare metal machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void powerOffBareMetalMachine(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .bareMetalMachines()
            .powerOff(
                "resourceGroupName",
                "bareMetalMachineName",
                new BareMetalMachinePowerOffParameters().withSkipShutdown(BareMetalMachineSkipShutdown.TRUE),
                com.azure.core.util.Context.NONE);
    }
}
```

### BareMetalMachines_Reimage

```java
/** Samples for BareMetalMachines Reimage. */
public final class BareMetalMachinesReimageSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BareMetalMachines_Reimage.json
     */
    /**
     * Sample code: Reimage bare metal machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void reimageBareMetalMachine(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .bareMetalMachines()
            .reimage("resourceGroupName", "bareMetalMachineName", com.azure.core.util.Context.NONE);
    }
}
```

### BareMetalMachines_Replace

```java
import com.azure.resourcemanager.networkcloud.models.AdministrativeCredentials;
import com.azure.resourcemanager.networkcloud.models.BareMetalMachineReplaceParameters;

/** Samples for BareMetalMachines Replace. */
public final class BareMetalMachinesReplaceSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BareMetalMachines_Replace.json
     */
    /**
     * Sample code: Replace bare metal machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void replaceBareMetalMachine(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .bareMetalMachines()
            .replace(
                "resourceGroupName",
                "bareMetalMachineName",
                new BareMetalMachineReplaceParameters()
                    .withBmcCredentials(
                        new AdministrativeCredentials().withPassword("fakeTokenPlaceholder").withUsername("bmcuser"))
                    .withBmcMacAddress("00:00:4f:00:57:ad")
                    .withBootMacAddress("00:00:4e:00:58:af")
                    .withMachineName("name")
                    .withSerialNumber("BM1219XXX"),
                com.azure.core.util.Context.NONE);
    }
}
```

### BareMetalMachines_Restart

```java
/** Samples for BareMetalMachines Restart. */
public final class BareMetalMachinesRestartSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BareMetalMachines_Restart.json
     */
    /**
     * Sample code: Restart bare metal machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void restartBareMetalMachine(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .bareMetalMachines()
            .restart("resourceGroupName", "bareMetalMachineName", com.azure.core.util.Context.NONE);
    }
}
```

### BareMetalMachines_RunCommand

```java
import com.azure.resourcemanager.networkcloud.models.BareMetalMachineRunCommandParameters;
import java.util.Arrays;

/** Samples for BareMetalMachines RunCommand. */
public final class BareMetalMachinesRunCommandSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BareMetalMachines_RunCommand.json
     */
    /**
     * Sample code: Run command on bare metal machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void runCommandOnBareMetalMachine(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .bareMetalMachines()
            .runCommand(
                "resourceGroupName",
                "bareMetalMachineName",
                new BareMetalMachineRunCommandParameters()
                    .withArguments(Arrays.asList("--argument1", "argument2"))
                    .withLimitTimeSeconds(60L)
                    .withScript("cHdkCg=="),
                com.azure.core.util.Context.NONE);
    }
}
```

### BareMetalMachines_RunDataExtracts

```java
import com.azure.resourcemanager.networkcloud.models.BareMetalMachineCommandSpecification;
import com.azure.resourcemanager.networkcloud.models.BareMetalMachineRunDataExtractsParameters;
import java.util.Arrays;

/** Samples for BareMetalMachines RunDataExtracts. */
public final class BareMetalMachinesRunDataExtractsSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BareMetalMachines_RunDataExtracts.json
     */
    /**
     * Sample code: Run data extraction on bare metal machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void runDataExtractionOnBareMetalMachine(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .bareMetalMachines()
            .runDataExtracts(
                "resourceGroupName",
                "bareMetalMachineName",
                new BareMetalMachineRunDataExtractsParameters()
                    .withCommands(
                        Arrays
                            .asList(
                                new BareMetalMachineCommandSpecification()
                                    .withArguments(Arrays.asList("SysInfo", "TTYLog"))
                                    .withCommand("hardware-support-data-collection")))
                    .withLimitTimeSeconds(60L),
                com.azure.core.util.Context.NONE);
    }
}
```

### BareMetalMachines_RunReadCommands

```java
import com.azure.resourcemanager.networkcloud.models.BareMetalMachineCommandSpecification;
import com.azure.resourcemanager.networkcloud.models.BareMetalMachineRunReadCommandsParameters;
import java.util.Arrays;

/** Samples for BareMetalMachines RunReadCommands. */
public final class BareMetalMachinesRunReadCommandsSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BareMetalMachines_RunReadCommands.json
     */
    /**
     * Sample code: Run and retrieve output from read only commands on bare metal machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void runAndRetrieveOutputFromReadOnlyCommandsOnBareMetalMachine(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .bareMetalMachines()
            .runReadCommands(
                "resourceGroupName",
                "bareMetalMachineName",
                new BareMetalMachineRunReadCommandsParameters()
                    .withCommands(
                        Arrays
                            .asList(
                                new BareMetalMachineCommandSpecification()
                                    .withArguments(Arrays.asList("pods", "-A"))
                                    .withCommand("kubectl get"),
                                new BareMetalMachineCommandSpecification()
                                    .withArguments(Arrays.asList("192.168.0.99", "-c", "3"))
                                    .withCommand("ping")))
                    .withLimitTimeSeconds(60L),
                com.azure.core.util.Context.NONE);
    }
}
```

### BareMetalMachines_Start

```java
/** Samples for BareMetalMachines Start. */
public final class BareMetalMachinesStartSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BareMetalMachines_Start.json
     */
    /**
     * Sample code: Start bare metal machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void startBareMetalMachine(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .bareMetalMachines()
            .start("resourceGroupName", "bareMetalMachineName", com.azure.core.util.Context.NONE);
    }
}
```

### BareMetalMachines_Uncordon

```java
/** Samples for BareMetalMachines Uncordon. */
public final class BareMetalMachinesUncordonSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BareMetalMachines_Uncordon.json
     */
    /**
     * Sample code: Uncordon bare metal machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void uncordonBareMetalMachine(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .bareMetalMachines()
            .uncordon("resourceGroupName", "bareMetalMachineName", com.azure.core.util.Context.NONE);
    }
}
```

### BareMetalMachines_Update

```java
import com.azure.resourcemanager.networkcloud.models.BareMetalMachine;
import java.util.HashMap;
import java.util.Map;

/** Samples for BareMetalMachines Update. */
public final class BareMetalMachinesUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BareMetalMachines_Patch.json
     */
    /**
     * Sample code: Patch bare metal machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void patchBareMetalMachine(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        BareMetalMachine resource =
            manager
                .bareMetalMachines()
                .getByResourceGroupWithResponse(
                    "resourceGroupName", "bareMetalMachineName", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
            .withMachineDetails("machinedetails")
            .apply();
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

### BareMetalMachines_ValidateHardware

```java
import com.azure.resourcemanager.networkcloud.models.BareMetalMachineHardwareValidationCategory;
import com.azure.resourcemanager.networkcloud.models.BareMetalMachineValidateHardwareParameters;

/** Samples for BareMetalMachines ValidateHardware. */
public final class BareMetalMachinesValidateHardwareSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BareMetalMachines_ValidateHardware.json
     */
    /**
     * Sample code: Validate the bare metal machine hardware.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void validateTheBareMetalMachineHardware(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .bareMetalMachines()
            .validateHardware(
                "resourceGroupName",
                "bareMetalMachineName",
                new BareMetalMachineValidateHardwareParameters()
                    .withValidationCategory(BareMetalMachineHardwareValidationCategory.BASIC_VALIDATION),
                com.azure.core.util.Context.NONE);
    }
}
```

### BmcKeySets_CreateOrUpdate

```java
import com.azure.resourcemanager.networkcloud.models.BmcKeySetPrivilegeLevel;
import com.azure.resourcemanager.networkcloud.models.ExtendedLocation;
import com.azure.resourcemanager.networkcloud.models.KeySetUser;
import com.azure.resourcemanager.networkcloud.models.SshPublicKey;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for BmcKeySets CreateOrUpdate. */
public final class BmcKeySetsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BmcKeySets_Create.json
     */
    /**
     * Sample code: Create or update baseboard management controller key set of cluster.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void createOrUpdateBaseboardManagementControllerKeySetOfCluster(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .bmcKeySets()
            .define("bmcKeySetName")
            .withRegion("location")
            .withExistingCluster("resourceGroupName", "clusterName")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ExtendedLocation/customLocations/clusterExtendedLocationName")
                    .withType("CustomLocation"))
            .withAzureGroupId("f110271b-XXXX-4163-9b99-214d91660f0e")
            .withExpiration(OffsetDateTime.parse("2022-12-31T23:59:59.008Z"))
            .withPrivilegeLevel(BmcKeySetPrivilegeLevel.ADMINISTRATOR)
            .withUserList(
                Arrays
                    .asList(
                        new KeySetUser()
                            .withAzureUsername("userABC")
                            .withDescription("Needs access for troubleshooting as a part of the support team")
                            .withSshPublicKey(new SshPublicKey().withKeyData("fakeTokenPlaceholder")),
                        new KeySetUser()
                            .withAzureUsername("userXYZ")
                            .withDescription("Needs access for troubleshooting as a part of the support team")
                            .withSshPublicKey(new SshPublicKey().withKeyData("fakeTokenPlaceholder"))))
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
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

### BmcKeySets_Delete

```java
/** Samples for BmcKeySets Delete. */
public final class BmcKeySetsDeleteSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BmcKeySets_Delete.json
     */
    /**
     * Sample code: Delete baseboard management controller key set of cluster.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void deleteBaseboardManagementControllerKeySetOfCluster(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .bmcKeySets()
            .delete("resourceGroupName", "clusterName", "bmcKeySetName", com.azure.core.util.Context.NONE);
    }
}
```

### BmcKeySets_Get

```java
/** Samples for BmcKeySets Get. */
public final class BmcKeySetsGetSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BmcKeySets_Get.json
     */
    /**
     * Sample code: Get baseboard management controller key set of cluster.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void getBaseboardManagementControllerKeySetOfCluster(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .bmcKeySets()
            .getWithResponse("resourceGroupName", "clusterName", "bmcKeySetName", com.azure.core.util.Context.NONE);
    }
}
```

### BmcKeySets_ListByCluster

```java
/** Samples for BmcKeySets ListByCluster. */
public final class BmcKeySetsListByClusterSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BmcKeySets_ListByCluster.json
     */
    /**
     * Sample code: List baseboard management controller key sets of the cluster.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listBaseboardManagementControllerKeySetsOfTheCluster(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.bmcKeySets().listByCluster("resourceGroupName", "clusterName", com.azure.core.util.Context.NONE);
    }
}
```

### BmcKeySets_Update

```java
import com.azure.resourcemanager.networkcloud.models.BmcKeySet;
import com.azure.resourcemanager.networkcloud.models.KeySetUser;
import com.azure.resourcemanager.networkcloud.models.SshPublicKey;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for BmcKeySets Update. */
public final class BmcKeySetsUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/BmcKeySets_Patch.json
     */
    /**
     * Sample code: Patch baseboard management controller key set of cluster.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void patchBaseboardManagementControllerKeySetOfCluster(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        BmcKeySet resource =
            manager
                .bmcKeySets()
                .getWithResponse("resourceGroupName", "clusterName", "bmcKeySetName", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
            .withExpiration(OffsetDateTime.parse("2022-12-31T23:59:59.008Z"))
            .withUserList(
                Arrays
                    .asList(
                        new KeySetUser()
                            .withAzureUsername("userABC")
                            .withDescription("Needs access for troubleshooting as a part of the support team")
                            .withSshPublicKey(new SshPublicKey().withKeyData("fakeTokenPlaceholder")),
                        new KeySetUser()
                            .withAzureUsername("userXYZ")
                            .withDescription("Needs access for troubleshooting as a part of the support team")
                            .withSshPublicKey(new SshPublicKey().withKeyData("fakeTokenPlaceholder"))))
            .apply();
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

### CloudServicesNetworks_CreateOrUpdate

```java
import com.azure.resourcemanager.networkcloud.models.CloudServicesNetworkEnableDefaultEgressEndpoints;
import com.azure.resourcemanager.networkcloud.models.EgressEndpoint;
import com.azure.resourcemanager.networkcloud.models.EndpointDependency;
import com.azure.resourcemanager.networkcloud.models.ExtendedLocation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for CloudServicesNetworks CreateOrUpdate. */
public final class CloudServicesNetworksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/CloudServicesNetworks_Create.json
     */
    /**
     * Sample code: Create or update cloud services network.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void createOrUpdateCloudServicesNetwork(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .cloudServicesNetworks()
            .define("cloudServicesNetworkName")
            .withRegion("location")
            .withExistingResourceGroup("resourceGroupName")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ExtendedLocation/customLocations/clusterExtendedLocationName")
                    .withType("CustomLocation"))
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
            .withAdditionalEgressEndpoints(
                Arrays
                    .asList(
                        new EgressEndpoint()
                            .withCategory("azure-resource-management")
                            .withEndpoints(
                                Arrays
                                    .asList(
                                        new EndpointDependency()
                                            .withDomainName("https://storageaccountex.blob.core.windows.net")
                                            .withPort(443L)))))
            .withEnableDefaultEgressEndpoints(CloudServicesNetworkEnableDefaultEgressEndpoints.FALSE)
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

### CloudServicesNetworks_Delete

```java
/** Samples for CloudServicesNetworks Delete. */
public final class CloudServicesNetworksDeleteSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/CloudServicesNetworks_Delete.json
     */
    /**
     * Sample code: Delete cloud services network.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void deleteCloudServicesNetwork(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .cloudServicesNetworks()
            .delete("resourceGroupName", "cloudServicesNetworkName", com.azure.core.util.Context.NONE);
    }
}
```

### CloudServicesNetworks_GetByResourceGroup

```java
/** Samples for CloudServicesNetworks GetByResourceGroup. */
public final class CloudServicesNetworksGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/CloudServicesNetworks_Get.json
     */
    /**
     * Sample code: Get cloud services network.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void getCloudServicesNetwork(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .cloudServicesNetworks()
            .getByResourceGroupWithResponse(
                "resourceGroupName", "cloudServicesNetworkName", com.azure.core.util.Context.NONE);
    }
}
```

### CloudServicesNetworks_List

```java
/** Samples for CloudServicesNetworks List. */
public final class CloudServicesNetworksListSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/CloudServicesNetworks_ListBySubscription.json
     */
    /**
     * Sample code: List cloud services networks for subscription.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listCloudServicesNetworksForSubscription(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.cloudServicesNetworks().list(com.azure.core.util.Context.NONE);
    }
}
```

### CloudServicesNetworks_ListByResourceGroup

```java
/** Samples for CloudServicesNetworks ListByResourceGroup. */
public final class CloudServicesNetworksListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/CloudServicesNetworks_ListByResourceGroup.json
     */
    /**
     * Sample code: List cloud services networks for resource group.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listCloudServicesNetworksForResourceGroup(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.cloudServicesNetworks().listByResourceGroup("resourceGroupName", com.azure.core.util.Context.NONE);
    }
}
```

### CloudServicesNetworks_Update

```java
import com.azure.resourcemanager.networkcloud.models.CloudServicesNetwork;
import com.azure.resourcemanager.networkcloud.models.CloudServicesNetworkEnableDefaultEgressEndpoints;
import com.azure.resourcemanager.networkcloud.models.EgressEndpoint;
import com.azure.resourcemanager.networkcloud.models.EndpointDependency;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for CloudServicesNetworks Update. */
public final class CloudServicesNetworksUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/CloudServicesNetworks_Patch.json
     */
    /**
     * Sample code: Patch cloud services network.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void patchCloudServicesNetwork(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        CloudServicesNetwork resource =
            manager
                .cloudServicesNetworks()
                .getByResourceGroupWithResponse(
                    "resourceGroupName", "cloudServicesNetworkName", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
            .withAdditionalEgressEndpoints(
                Arrays
                    .asList(
                        new EgressEndpoint()
                            .withCategory("azure-resource-management")
                            .withEndpoints(
                                Arrays
                                    .asList(
                                        new EndpointDependency()
                                            .withDomainName("https://storageaccountex.blob.core.windows.net")
                                            .withPort(443L)))))
            .withEnableDefaultEgressEndpoints(CloudServicesNetworkEnableDefaultEgressEndpoints.FALSE)
            .apply();
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

### ClusterManagers_CreateOrUpdate

```java
import com.azure.resourcemanager.networkcloud.models.ManagedResourceGroupConfiguration;
import java.util.HashMap;
import java.util.Map;

/** Samples for ClusterManagers CreateOrUpdate. */
public final class ClusterManagersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/ClusterManagers_Create.json
     */
    /**
     * Sample code: Create or update cluster manager.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void createOrUpdateClusterManager(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .clusterManagers()
            .define("clusterManagerName")
            .withRegion("location")
            .withExistingResourceGroup("resourceGroupName")
            .withFabricControllerId(
                "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ManagedNetworkFabric/networkFabricControllers/fabricControllerName")
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
            .withAnalyticsWorkspaceId(
                "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/microsoft.operationalInsights/workspaces/logAnalyticsWorkspaceName")
            .withManagedResourceGroupConfiguration(
                new ManagedResourceGroupConfiguration().withLocation("East US").withName("my-managed-rg"))
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

### ClusterManagers_Delete

```java
/** Samples for ClusterManagers Delete. */
public final class ClusterManagersDeleteSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/ClusterManagers_Delete.json
     */
    /**
     * Sample code: Delete cluster manager.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void deleteClusterManager(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.clusterManagers().delete("resourceGroupName", "clusterManagerName", com.azure.core.util.Context.NONE);
    }
}
```

### ClusterManagers_GetByResourceGroup

```java
/** Samples for ClusterManagers GetByResourceGroup. */
public final class ClusterManagersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/ClusterManagers_Get.json
     */
    /**
     * Sample code: Get cluster manager.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void getClusterManager(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .clusterManagers()
            .getByResourceGroupWithResponse(
                "resourceGroupName", "clusterManagerName", com.azure.core.util.Context.NONE);
    }
}
```

### ClusterManagers_List

```java
/** Samples for ClusterManagers List. */
public final class ClusterManagersListSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/ClusterManagers_ListBySubscription.json
     */
    /**
     * Sample code: List cluster managers for subscription.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listClusterManagersForSubscription(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.clusterManagers().list(com.azure.core.util.Context.NONE);
    }
}
```

### ClusterManagers_ListByResourceGroup

```java
/** Samples for ClusterManagers ListByResourceGroup. */
public final class ClusterManagersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/ClusterManagers_ListByResourceGroup.json
     */
    /**
     * Sample code: List cluster managers for resource group.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listClusterManagersForResourceGroup(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.clusterManagers().listByResourceGroup("resourceGroupName", com.azure.core.util.Context.NONE);
    }
}
```

### ClusterManagers_Update

```java
import com.azure.resourcemanager.networkcloud.models.ClusterManager;
import java.util.HashMap;
import java.util.Map;

/** Samples for ClusterManagers Update. */
public final class ClusterManagersUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/ClusterManagers_Patch.json
     */
    /**
     * Sample code: Patch cluster manager.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void patchClusterManager(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        ClusterManager resource =
            manager
                .clusterManagers()
                .getByResourceGroupWithResponse(
                    "resourceGroupName", "clusterManagerName", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("key1", "myvalue1", "key2", "myvalue2")).apply();
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

### Clusters_CreateOrUpdate

```java
import com.azure.resourcemanager.networkcloud.models.AdministrativeCredentials;
import com.azure.resourcemanager.networkcloud.models.BareMetalMachineConfigurationData;
import com.azure.resourcemanager.networkcloud.models.ClusterType;
import com.azure.resourcemanager.networkcloud.models.ExtendedLocation;
import com.azure.resourcemanager.networkcloud.models.ManagedResourceGroupConfiguration;
import com.azure.resourcemanager.networkcloud.models.RackDefinition;
import com.azure.resourcemanager.networkcloud.models.ServicePrincipalInformation;
import com.azure.resourcemanager.networkcloud.models.StorageApplianceConfigurationData;
import com.azure.resourcemanager.networkcloud.models.ValidationThreshold;
import com.azure.resourcemanager.networkcloud.models.ValidationThresholdGrouping;
import com.azure.resourcemanager.networkcloud.models.ValidationThresholdType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Clusters CreateOrUpdate. */
public final class ClustersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Clusters_Create.json
     */
    /**
     * Sample code: Create or update cluster.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void createOrUpdateCluster(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .clusters()
            .define("clusterName")
            .withRegion("location")
            .withExistingResourceGroup("resourceGroupName")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ExtendedLocation/customLocations/clusterManagerExtendedLocationName")
                    .withType("CustomLocation"))
            .withAggregatorOrSingleRackDefinition(
                new RackDefinition()
                    .withBareMetalMachineConfigurationData(
                        Arrays
                            .asList(
                                new BareMetalMachineConfigurationData()
                                    .withBmcCredentials(
                                        new AdministrativeCredentials()
                                            .withPassword("fakeTokenPlaceholder")
                                            .withUsername("username"))
                                    .withBmcMacAddress("AA:BB:CC:DD:EE:FF")
                                    .withBootMacAddress("00:BB:CC:DD:EE:FF")
                                    .withMachineDetails("extraDetails")
                                    .withMachineName("bmmName1")
                                    .withRackSlot(1L)
                                    .withSerialNumber("BM1219XXX"),
                                new BareMetalMachineConfigurationData()
                                    .withBmcCredentials(
                                        new AdministrativeCredentials()
                                            .withPassword("fakeTokenPlaceholder")
                                            .withUsername("username"))
                                    .withBmcMacAddress("AA:BB:CC:DD:EE:00")
                                    .withBootMacAddress("00:BB:CC:DD:EE:00")
                                    .withMachineDetails("extraDetails")
                                    .withMachineName("bmmName2")
                                    .withRackSlot(2L)
                                    .withSerialNumber("BM1219YYY")))
                    .withNetworkRackId(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ManagedNetworkFabric/networkRacks/networkRackName")
                    .withRackLocation("Foo Datacenter, Floor 3, Aisle 9, Rack 2")
                    .withRackSerialNumber("AA1234")
                    .withRackSkuId(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/providers/Microsoft.NetworkCloud/rackSkus/rackSkuName")
                    .withStorageApplianceConfigurationData(
                        Arrays
                            .asList(
                                new StorageApplianceConfigurationData()
                                    .withAdminCredentials(
                                        new AdministrativeCredentials()
                                            .withPassword("fakeTokenPlaceholder")
                                            .withUsername("username"))
                                    .withRackSlot(1L)
                                    .withSerialNumber("BM1219XXX")
                                    .withStorageApplianceName("vmName"))))
            .withClusterType(ClusterType.SINGLE_RACK)
            .withClusterVersion("1.0.0")
            .withNetworkFabricId(
                "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ManagedNetworkFabric/networkFabrics/fabricName")
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
            .withAnalyticsWorkspaceId(
                "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/microsoft.operationalInsights/workspaces/logAnalyticsWorkspaceName")
            .withClusterLocation("Foo Street, 3rd Floor, row 9")
            .withClusterServicePrincipal(
                new ServicePrincipalInformation()
                    .withApplicationId("12345678-1234-1234-1234-123456789012")
                    .withPassword("fakeTokenPlaceholder")
                    .withPrincipalId("00000008-0004-0004-0004-000000000012")
                    .withTenantId("80000000-4000-4000-4000-120000000000"))
            .withComputeDeploymentThreshold(
                new ValidationThreshold()
                    .withGrouping(ValidationThresholdGrouping.PER_CLUSTER)
                    .withType(ValidationThresholdType.PERCENT_SUCCESS)
                    .withValue(90L))
            .withComputeRackDefinitions(
                Arrays
                    .asList(
                        new RackDefinition()
                            .withBareMetalMachineConfigurationData(
                                Arrays
                                    .asList(
                                        new BareMetalMachineConfigurationData()
                                            .withBmcCredentials(
                                                new AdministrativeCredentials()
                                                    .withPassword("fakeTokenPlaceholder")
                                                    .withUsername("username"))
                                            .withBmcMacAddress("AA:BB:CC:DD:EE:FF")
                                            .withBootMacAddress("00:BB:CC:DD:EE:FF")
                                            .withMachineDetails("extraDetails")
                                            .withMachineName("bmmName1")
                                            .withRackSlot(1L)
                                            .withSerialNumber("BM1219XXX"),
                                        new BareMetalMachineConfigurationData()
                                            .withBmcCredentials(
                                                new AdministrativeCredentials()
                                                    .withPassword("fakeTokenPlaceholder")
                                                    .withUsername("username"))
                                            .withBmcMacAddress("AA:BB:CC:DD:EE:00")
                                            .withBootMacAddress("00:BB:CC:DD:EE:00")
                                            .withMachineDetails("extraDetails")
                                            .withMachineName("bmmName2")
                                            .withRackSlot(2L)
                                            .withSerialNumber("BM1219YYY")))
                            .withNetworkRackId(
                                "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ManagedNetworkFabric/networkRacks/networkRackName")
                            .withRackLocation("Foo Datacenter, Floor 3, Aisle 9, Rack 2")
                            .withRackSerialNumber("AA1234")
                            .withRackSkuId(
                                "/subscriptions/123e4567-e89b-12d3-a456-426655440000/providers/Microsoft.NetworkCloud/rackSkus/rackSkuName")
                            .withStorageApplianceConfigurationData(
                                Arrays
                                    .asList(
                                        new StorageApplianceConfigurationData()
                                            .withAdminCredentials(
                                                new AdministrativeCredentials()
                                                    .withPassword("fakeTokenPlaceholder")
                                                    .withUsername("username"))
                                            .withRackSlot(1L)
                                            .withSerialNumber("BM1219XXX")
                                            .withStorageApplianceName("vmName")))))
            .withManagedResourceGroupConfiguration(
                new ManagedResourceGroupConfiguration().withLocation("East US").withName("my-managed-rg"))
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

### Clusters_Delete

```java
/** Samples for Clusters Delete. */
public final class ClustersDeleteSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Clusters_Delete.json
     */
    /**
     * Sample code: Delete cluster.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void deleteCluster(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.clusters().delete("resourceGroupName", "clusterName", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Deploy

```java
import com.azure.resourcemanager.networkcloud.models.ClusterDeployParameters;
import java.util.Arrays;

/** Samples for Clusters Deploy. */
public final class ClustersDeploySamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Clusters_Deploy.json
     */
    /**
     * Sample code: Deploy cluster.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void deployCluster(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .clusters()
            .deploy(
                "resourceGroupName", "clusterName", new ClusterDeployParameters(), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Clusters_Deploy_SkipValidation.json
     */
    /**
     * Sample code: Deploy cluster skipping validation.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void deployClusterSkippingValidation(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .clusters()
            .deploy(
                "resourceGroupName",
                "clusterName",
                new ClusterDeployParameters().withSkipValidationsForMachines(Arrays.asList("bmmName1")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_GetByResourceGroup

```java
/** Samples for Clusters GetByResourceGroup. */
public final class ClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Clusters_Get.json
     */
    /**
     * Sample code: Get cluster.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void getCluster(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .clusters()
            .getByResourceGroupWithResponse("resourceGroupName", "clusterName", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_List

```java
/** Samples for Clusters List. */
public final class ClustersListSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Clusters_ListBySubscription.json
     */
    /**
     * Sample code: List clusters for subscription.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listClustersForSubscription(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.clusters().list(com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_ListByResourceGroup

```java
/** Samples for Clusters ListByResourceGroup. */
public final class ClustersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Clusters_ListByResourceGroup.json
     */
    /**
     * Sample code: List clusters for resource group.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listClustersForResourceGroup(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.clusters().listByResourceGroup("resourceGroupName", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Update

```java
import com.azure.resourcemanager.networkcloud.models.AdministrativeCredentials;
import com.azure.resourcemanager.networkcloud.models.BareMetalMachineConfigurationData;
import com.azure.resourcemanager.networkcloud.models.Cluster;
import com.azure.resourcemanager.networkcloud.models.RackDefinition;
import com.azure.resourcemanager.networkcloud.models.StorageApplianceConfigurationData;
import com.azure.resourcemanager.networkcloud.models.ValidationThreshold;
import com.azure.resourcemanager.networkcloud.models.ValidationThresholdGrouping;
import com.azure.resourcemanager.networkcloud.models.ValidationThresholdType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Clusters Update. */
public final class ClustersUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Clusters_Patch_Location.json
     */
    /**
     * Sample code: Patch cluster location.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void patchClusterLocation(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        Cluster resource =
            manager
                .clusters()
                .getByResourceGroupWithResponse("resourceGroupName", "clusterName", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
            .withClusterLocation("Foo Street, 3rd Floor, row 9")
            .apply();
    }

    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Clusters_Patch_AggregatorOrSingleRackDefinition.json
     */
    /**
     * Sample code: Patch cluster AggregatorOrSingleRackDefinition.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void patchClusterAggregatorOrSingleRackDefinition(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        Cluster resource =
            manager
                .clusters()
                .getByResourceGroupWithResponse("resourceGroupName", "clusterName", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
            .withAggregatorOrSingleRackDefinition(
                new RackDefinition()
                    .withBareMetalMachineConfigurationData(
                        Arrays
                            .asList(
                                new BareMetalMachineConfigurationData()
                                    .withBmcCredentials(
                                        new AdministrativeCredentials()
                                            .withPassword("fakeTokenPlaceholder")
                                            .withUsername("username"))
                                    .withBmcMacAddress("AA:BB:CC:DD:EE:FF")
                                    .withBootMacAddress("00:BB:CC:DD:EE:FF")
                                    .withMachineDetails("extraDetails")
                                    .withMachineName("bmmName1")
                                    .withRackSlot(1L)
                                    .withSerialNumber("BM1219XXX"),
                                new BareMetalMachineConfigurationData()
                                    .withBmcCredentials(
                                        new AdministrativeCredentials()
                                            .withPassword("fakeTokenPlaceholder")
                                            .withUsername("username"))
                                    .withBmcMacAddress("AA:BB:CC:DD:EE:00")
                                    .withBootMacAddress("00:BB:CC:DD:EE:00")
                                    .withMachineDetails("extraDetails")
                                    .withMachineName("bmmName2")
                                    .withRackSlot(2L)
                                    .withSerialNumber("BM1219YYY")))
                    .withNetworkRackId(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ManagedNetworkFabric/networkRacks/networkRackName")
                    .withRackLocation("Foo Datacenter, Floor 3, Aisle 9, Rack 2")
                    .withRackSerialNumber("newSerialNumber")
                    .withRackSkuId(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/providers/Microsoft.NetworkCloud/rackSkus/rackSkuName")
                    .withStorageApplianceConfigurationData(
                        Arrays
                            .asList(
                                new StorageApplianceConfigurationData()
                                    .withAdminCredentials(
                                        new AdministrativeCredentials()
                                            .withPassword("fakeTokenPlaceholder")
                                            .withUsername("username"))
                                    .withRackSlot(1L)
                                    .withSerialNumber("BM1219XXX")
                                    .withStorageApplianceName("vmName"))))
            .withComputeDeploymentThreshold(
                new ValidationThreshold()
                    .withGrouping(ValidationThresholdGrouping.PER_CLUSTER)
                    .withType(ValidationThresholdType.PERCENT_SUCCESS)
                    .withValue(90L))
            .apply();
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

### Clusters_UpdateVersion

```java
import com.azure.resourcemanager.networkcloud.models.ClusterUpdateVersionParameters;

/** Samples for Clusters UpdateVersion. */
public final class ClustersUpdateVersionSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Clusters_UpdateVersion.json
     */
    /**
     * Sample code: Update cluster version.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void updateClusterVersion(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .clusters()
            .updateVersion(
                "resourceGroupName",
                "clusterName",
                new ClusterUpdateVersionParameters().withTargetClusterVersion("2.0"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Consoles_CreateOrUpdate

```java
import com.azure.resourcemanager.networkcloud.models.ConsoleEnabled;
import com.azure.resourcemanager.networkcloud.models.ExtendedLocation;
import com.azure.resourcemanager.networkcloud.models.SshPublicKey;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/** Samples for Consoles CreateOrUpdate. */
public final class ConsolesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Consoles_Create.json
     */
    /**
     * Sample code: Create or update virtual machine console.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void createOrUpdateVirtualMachineConsole(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .consoles()
            .define("default")
            .withRegion("location")
            .withExistingVirtualMachine("resourceGroupName", "virtualMachineName")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ExtendedLocation/customLocations/clusterManagerExtendedLocationName")
                    .withType("CustomLocation"))
            .withEnabled(ConsoleEnabled.TRUE)
            .withSshPublicKey(new SshPublicKey().withKeyData("fakeTokenPlaceholder"))
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
            .withExpiration(OffsetDateTime.parse("2022-06-01T01:27:03.008Z"))
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

### Consoles_Delete

```java
/** Samples for Consoles Delete. */
public final class ConsolesDeleteSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Consoles_Delete.json
     */
    /**
     * Sample code: Delete virtual machine console.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void deleteVirtualMachineConsole(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .consoles()
            .delete("resourceGroupName", "virtualMachineName", "default", com.azure.core.util.Context.NONE);
    }
}
```

### Consoles_Get

```java
/** Samples for Consoles Get. */
public final class ConsolesGetSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Consoles_Get.json
     */
    /**
     * Sample code: Get virtual machine console.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void getVirtualMachineConsole(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .consoles()
            .getWithResponse("resourceGroupName", "virtualMachineName", "default", com.azure.core.util.Context.NONE);
    }
}
```

### Consoles_ListByVirtualMachine

```java
/** Samples for Consoles ListByVirtualMachine. */
public final class ConsolesListByVirtualMachineSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Consoles_ListByVirtualMachine.json
     */
    /**
     * Sample code: List consoles of the virtual machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listConsolesOfTheVirtualMachine(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .consoles()
            .listByVirtualMachine("resourceGroupName", "virtualMachineName", com.azure.core.util.Context.NONE);
    }
}
```

### Consoles_Update

```java
import com.azure.resourcemanager.networkcloud.models.Console;
import com.azure.resourcemanager.networkcloud.models.ConsoleEnabled;
import com.azure.resourcemanager.networkcloud.models.SshPublicKey;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/** Samples for Consoles Update. */
public final class ConsolesUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Consoles_Patch.json
     */
    /**
     * Sample code: Patch virtual machine console.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void patchVirtualMachineConsole(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        Console resource =
            manager
                .consoles()
                .getWithResponse("resourceGroupName", "virtualMachineName", "default", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
            .withEnabled(ConsoleEnabled.TRUE)
            .withExpiration(OffsetDateTime.parse("2022-06-01T01:27:03.008Z"))
            .withSshPublicKey(new SshPublicKey().withKeyData("fakeTokenPlaceholder"))
            .apply();
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

### KubernetesClusters_CreateOrUpdate

```java
import com.azure.resourcemanager.networkcloud.models.AadConfiguration;
import com.azure.resourcemanager.networkcloud.models.AdministratorConfiguration;
import com.azure.resourcemanager.networkcloud.models.AdvertiseToFabric;
import com.azure.resourcemanager.networkcloud.models.AgentOptions;
import com.azure.resourcemanager.networkcloud.models.AgentPoolMode;
import com.azure.resourcemanager.networkcloud.models.AgentPoolUpgradeSettings;
import com.azure.resourcemanager.networkcloud.models.AttachedNetworkConfiguration;
import com.azure.resourcemanager.networkcloud.models.BfdEnabled;
import com.azure.resourcemanager.networkcloud.models.BgpAdvertisement;
import com.azure.resourcemanager.networkcloud.models.BgpMultiHop;
import com.azure.resourcemanager.networkcloud.models.BgpServiceLoadBalancerConfiguration;
import com.azure.resourcemanager.networkcloud.models.ControlPlaneNodeConfiguration;
import com.azure.resourcemanager.networkcloud.models.ExtendedLocation;
import com.azure.resourcemanager.networkcloud.models.FabricPeeringEnabled;
import com.azure.resourcemanager.networkcloud.models.HugepagesSize;
import com.azure.resourcemanager.networkcloud.models.InitialAgentPoolConfiguration;
import com.azure.resourcemanager.networkcloud.models.IpAddressPool;
import com.azure.resourcemanager.networkcloud.models.KubernetesLabel;
import com.azure.resourcemanager.networkcloud.models.KubernetesPluginType;
import com.azure.resourcemanager.networkcloud.models.L2NetworkAttachmentConfiguration;
import com.azure.resourcemanager.networkcloud.models.L3NetworkAttachmentConfiguration;
import com.azure.resourcemanager.networkcloud.models.L3NetworkConfigurationIpamEnabled;
import com.azure.resourcemanager.networkcloud.models.ManagedResourceGroupConfiguration;
import com.azure.resourcemanager.networkcloud.models.NetworkConfiguration;
import com.azure.resourcemanager.networkcloud.models.ServiceLoadBalancerBgpPeer;
import com.azure.resourcemanager.networkcloud.models.SshPublicKey;
import com.azure.resourcemanager.networkcloud.models.TrunkedNetworkAttachmentConfiguration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for KubernetesClusters CreateOrUpdate. */
public final class KubernetesClustersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/KubernetesClusters_Create.json
     */
    /**
     * Sample code: Create or update Kubernetes cluster.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void createOrUpdateKubernetesCluster(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .kubernetesClusters()
            .define("kubernetesClusterName")
            .withRegion("location")
            .withExistingResourceGroup("resourceGroupName")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ExtendedLocation/customLocations/clusterExtendedLocationName")
                    .withType("CustomLocation"))
            .withControlPlaneNodeConfiguration(
                new ControlPlaneNodeConfiguration()
                    .withAdministratorConfiguration(
                        new AdministratorConfiguration()
                            .withAdminUsername("azure")
                            .withSshPublicKeys(Arrays.asList(new SshPublicKey().withKeyData("fakeTokenPlaceholder"))))
                    .withAvailabilityZones(Arrays.asList("1", "2", "3"))
                    .withCount(3L)
                    .withVmSkuName("NC_G4_v1"))
            .withInitialAgentPoolConfigurations(
                Arrays
                    .asList(
                        new InitialAgentPoolConfiguration()
                            .withAdministratorConfiguration(
                                new AdministratorConfiguration()
                                    .withAdminUsername("azure")
                                    .withSshPublicKeys(
                                        Arrays.asList(new SshPublicKey().withKeyData("fakeTokenPlaceholder"))))
                            .withAgentOptions(
                                new AgentOptions().withHugepagesCount(96L).withHugepagesSize(HugepagesSize.ONEG))
                            .withAttachedNetworkConfiguration(
                                new AttachedNetworkConfiguration()
                                    .withL2Networks(
                                        Arrays
                                            .asList(
                                                new L2NetworkAttachmentConfiguration()
                                                    .withNetworkId(
                                                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.NetworkCloud/l2Networks/l2NetworkName")
                                                    .withPluginType(KubernetesPluginType.DPDK)))
                                    .withL3Networks(
                                        Arrays
                                            .asList(
                                                new L3NetworkAttachmentConfiguration()
                                                    .withIpamEnabled(L3NetworkConfigurationIpamEnabled.FALSE)
                                                    .withNetworkId(
                                                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.NetworkCloud/l3Networks/l3NetworkName")
                                                    .withPluginType(KubernetesPluginType.SRIOV)))
                                    .withTrunkedNetworks(
                                        Arrays
                                            .asList(
                                                new TrunkedNetworkAttachmentConfiguration()
                                                    .withNetworkId(
                                                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.NetworkCloud/trunkedNetworks/trunkedNetworkName")
                                                    .withPluginType(KubernetesPluginType.MACVLAN))))
                            .withAvailabilityZones(Arrays.asList("1", "2", "3"))
                            .withCount(3L)
                            .withLabels(
                                Arrays.asList(new KubernetesLabel().withKey("fakeTokenPlaceholder").withValue("true")))
                            .withMode(AgentPoolMode.SYSTEM)
                            .withName("SystemPool-1")
                            .withTaints(
                                Arrays.asList(new KubernetesLabel().withKey("fakeTokenPlaceholder").withValue("true")))
                            .withUpgradeSettings(new AgentPoolUpgradeSettings().withMaxSurge("1"))
                            .withVmSkuName("NC_M16_v1")))
            .withKubernetesVersion("1.24.12-1")
            .withNetworkConfiguration(
                new NetworkConfiguration()
                    .withAttachedNetworkConfiguration(
                        new AttachedNetworkConfiguration()
                            .withL2Networks(
                                Arrays
                                    .asList(
                                        new L2NetworkAttachmentConfiguration()
                                            .withNetworkId(
                                                "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.NetworkCloud/l2Networks/l2NetworkName")
                                            .withPluginType(KubernetesPluginType.DPDK)))
                            .withL3Networks(
                                Arrays
                                    .asList(
                                        new L3NetworkAttachmentConfiguration()
                                            .withIpamEnabled(L3NetworkConfigurationIpamEnabled.FALSE)
                                            .withNetworkId(
                                                "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.NetworkCloud/l3Networks/l3NetworkName")
                                            .withPluginType(KubernetesPluginType.SRIOV)))
                            .withTrunkedNetworks(
                                Arrays
                                    .asList(
                                        new TrunkedNetworkAttachmentConfiguration()
                                            .withNetworkId(
                                                "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.NetworkCloud/trunkedNetworks/trunkedNetworkName")
                                            .withPluginType(KubernetesPluginType.MACVLAN))))
                    .withBgpServiceLoadBalancerConfiguration(
                        new BgpServiceLoadBalancerConfiguration()
                            .withBgpAdvertisements(
                                Arrays
                                    .asList(
                                        new BgpAdvertisement()
                                            .withAdvertiseToFabric(AdvertiseToFabric.TRUE)
                                            .withCommunities(Arrays.asList("64512:100"))
                                            .withIpAddressPools(Arrays.asList("pool1"))
                                            .withPeers(Arrays.asList("peer1"))))
                            .withBgpPeers(
                                Arrays
                                    .asList(
                                        new ServiceLoadBalancerBgpPeer()
                                            .withBfdEnabled(BfdEnabled.FALSE)
                                            .withBgpMultiHop(BgpMultiHop.FALSE)
                                            .withHoldTime("P300s")
                                            .withKeepAliveTime("P300s")
                                            .withMyAsn(64512L)
                                            .withName("peer1")
                                            .withPeerAddress("203.0.113.254")
                                            .withPeerAsn(64497L)
                                            .withPeerPort(179L)))
                            .withFabricPeeringEnabled(FabricPeeringEnabled.TRUE)
                            .withIpAddressPools(
                                Arrays
                                    .asList(
                                        new IpAddressPool()
                                            .withAddresses(Arrays.asList("198.51.102.0/24"))
                                            .withAutoAssign(BfdEnabled.TRUE)
                                            .withName("pool1")
                                            .withOnlyUseHostIps(BfdEnabled.TRUE))))
                    .withCloudServicesNetworkId(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.NetworkCloud/cloudServicesNetworks/cloudServicesNetworkName")
                    .withCniNetworkId(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.NetworkCloud/l3Networks/l3NetworkName")
                    .withDnsServiceIp("198.51.101.2")
                    .withPodCidrs(Arrays.asList("198.51.100.0/24"))
                    .withServiceCidrs(Arrays.asList("198.51.101.0/24")))
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
            .withAadConfiguration(
                new AadConfiguration().withAdminGroupObjectIds(Arrays.asList("ffffffff-ffff-ffff-ffff-ffffffffffff")))
            .withAdministratorConfiguration(
                new AdministratorConfiguration()
                    .withAdminUsername("azure")
                    .withSshPublicKeys(Arrays.asList(new SshPublicKey().withKeyData("fakeTokenPlaceholder"))))
            .withManagedResourceGroupConfiguration(
                new ManagedResourceGroupConfiguration().withLocation("East US").withName("my-managed-rg"))
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

### KubernetesClusters_Delete

```java
/** Samples for KubernetesClusters Delete. */
public final class KubernetesClustersDeleteSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/KubernetesClusters_Delete.json
     */
    /**
     * Sample code: Delete Kubernetes cluster.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void deleteKubernetesCluster(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .kubernetesClusters()
            .delete("resourceGroupName", "kubernetesClusterName", com.azure.core.util.Context.NONE);
    }
}
```

### KubernetesClusters_GetByResourceGroup

```java
/** Samples for KubernetesClusters GetByResourceGroup. */
public final class KubernetesClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/KubernetesClusters_Get.json
     */
    /**
     * Sample code: Get Kubernetes cluster.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void getKubernetesCluster(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .kubernetesClusters()
            .getByResourceGroupWithResponse(
                "resourceGroupName", "kubernetesClusterName", com.azure.core.util.Context.NONE);
    }
}
```

### KubernetesClusters_List

```java
/** Samples for KubernetesClusters List. */
public final class KubernetesClustersListSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/KubernetesClusters_ListBySubscription.json
     */
    /**
     * Sample code: List Kubernetes clusters for subscription.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listKubernetesClustersForSubscription(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.kubernetesClusters().list(com.azure.core.util.Context.NONE);
    }
}
```

### KubernetesClusters_ListByResourceGroup

```java
/** Samples for KubernetesClusters ListByResourceGroup. */
public final class KubernetesClustersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/KubernetesClusters_ListByResourceGroup.json
     */
    /**
     * Sample code: List Kubernetes clusters for resource group.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listKubernetesClustersForResourceGroup(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.kubernetesClusters().listByResourceGroup("resourceGroupName", com.azure.core.util.Context.NONE);
    }
}
```

### KubernetesClusters_RestartNode

```java
import com.azure.resourcemanager.networkcloud.models.KubernetesClusterRestartNodeParameters;

/** Samples for KubernetesClusters RestartNode. */
public final class KubernetesClustersRestartNodeSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/KubernetesClusters_RestartNode.json
     */
    /**
     * Sample code: Restart a Kubernetes cluster node.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void restartAKubernetesClusterNode(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .kubernetesClusters()
            .restartNode(
                "resourceGroupName",
                "kubernetesClusterName",
                new KubernetesClusterRestartNodeParameters().withNodeName("nodeName"),
                com.azure.core.util.Context.NONE);
    }
}
```

### KubernetesClusters_Update

```java
import com.azure.resourcemanager.networkcloud.models.ControlPlaneNodePatchConfiguration;
import com.azure.resourcemanager.networkcloud.models.KubernetesCluster;
import java.util.HashMap;
import java.util.Map;

/** Samples for KubernetesClusters Update. */
public final class KubernetesClustersUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/KubernetesClusters_Patch.json
     */
    /**
     * Sample code: Patch Kubernetes cluster.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void patchKubernetesCluster(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        KubernetesCluster resource =
            manager
                .kubernetesClusters()
                .getByResourceGroupWithResponse(
                    "resourceGroupName", "kubernetesClusterName", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
            .withControlPlaneNodeConfiguration(new ControlPlaneNodePatchConfiguration().withCount(3L))
            .withKubernetesVersion("1.24.12")
            .apply();
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

### L2Networks_CreateOrUpdate

```java
import com.azure.resourcemanager.networkcloud.models.ExtendedLocation;
import com.azure.resourcemanager.networkcloud.models.HybridAksPluginType;
import java.util.HashMap;
import java.util.Map;

/** Samples for L2Networks CreateOrUpdate. */
public final class L2NetworksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/L2Networks_Create.json
     */
    /**
     * Sample code: Create or update L2 network.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void createOrUpdateL2Network(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .l2Networks()
            .define("l2NetworkName")
            .withRegion("location")
            .withExistingResourceGroup("resourceGroupName")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ExtendedLocation/customLocations/clusterExtendedLocationName")
                    .withType("CustomLocation"))
            .withL2IsolationDomainId(
                "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ManagedNetworkFabric/l2IsolationDomains/l2IsolationDomainName")
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
            .withHybridAksPluginType(HybridAksPluginType.DPDK)
            .withInterfaceName("eth0")
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

### L2Networks_Delete

```java
/** Samples for L2Networks Delete. */
public final class L2NetworksDeleteSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/L2Networks_Delete.json
     */
    /**
     * Sample code: Delete L2 network.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void deleteL2Network(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.l2Networks().delete("resourceGroupName", "l2NetworkName", com.azure.core.util.Context.NONE);
    }
}
```

### L2Networks_GetByResourceGroup

```java
/** Samples for L2Networks GetByResourceGroup. */
public final class L2NetworksGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/L2Networks_Get.json
     */
    /**
     * Sample code: Get L2 network.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void getL2Network(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .l2Networks()
            .getByResourceGroupWithResponse("resourceGroupName", "l2NetworkName", com.azure.core.util.Context.NONE);
    }
}
```

### L2Networks_List

```java
/** Samples for L2Networks List. */
public final class L2NetworksListSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/L2Networks_ListBySubscription.json
     */
    /**
     * Sample code: List L2 networks for subscription.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listL2NetworksForSubscription(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.l2Networks().list(com.azure.core.util.Context.NONE);
    }
}
```

### L2Networks_ListByResourceGroup

```java
/** Samples for L2Networks ListByResourceGroup. */
public final class L2NetworksListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/L2Networks_ListByResourceGroup.json
     */
    /**
     * Sample code: List L2 networks for resource group.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listL2NetworksForResourceGroup(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.l2Networks().listByResourceGroup("resourceGroupName", com.azure.core.util.Context.NONE);
    }
}
```

### L2Networks_Update

```java
import com.azure.resourcemanager.networkcloud.models.L2Network;
import java.util.HashMap;
import java.util.Map;

/** Samples for L2Networks Update. */
public final class L2NetworksUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/L2Networks_Patch.json
     */
    /**
     * Sample code: Patch L2 network.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void patchL2Network(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        L2Network resource =
            manager
                .l2Networks()
                .getByResourceGroupWithResponse("resourceGroupName", "l2NetworkName", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("key1", "myvalue1", "key2", "myvalue2")).apply();
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

### L3Networks_CreateOrUpdate

```java
import com.azure.resourcemanager.networkcloud.models.ExtendedLocation;
import com.azure.resourcemanager.networkcloud.models.IpAllocationType;
import java.util.HashMap;
import java.util.Map;

/** Samples for L3Networks CreateOrUpdate. */
public final class L3NetworksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/L3Networks_Create.json
     */
    /**
     * Sample code: Create or update L3 network.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void createOrUpdateL3Network(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .l3Networks()
            .define("l3NetworkName")
            .withRegion("location")
            .withExistingResourceGroup("resourceGroupName")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ExtendedLocation/customLocations/clusterExtendedLocationName")
                    .withType("CustomLocation"))
            .withL3IsolationDomainId(
                "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ManagedNetworkFabric/l3IsolationDomains/l3IsolationDomainName")
            .withVlan(12L)
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
            .withInterfaceName("eth0")
            .withIpAllocationType(IpAllocationType.DUAL_STACK)
            .withIpv4ConnectedPrefix("198.51.100.0/24")
            .withIpv6ConnectedPrefix("2001:db8::/64")
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

### L3Networks_Delete

```java
/** Samples for L3Networks Delete. */
public final class L3NetworksDeleteSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/L3Networks_Delete.json
     */
    /**
     * Sample code: Delete L3 network.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void deleteL3Network(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.l3Networks().delete("resourceGroupName", "l3NetworkName", com.azure.core.util.Context.NONE);
    }
}
```

### L3Networks_GetByResourceGroup

```java
/** Samples for L3Networks GetByResourceGroup. */
public final class L3NetworksGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/L3Networks_Get.json
     */
    /**
     * Sample code: Get L3network.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void getL3network(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .l3Networks()
            .getByResourceGroupWithResponse("resourceGroupName", "l3NetworkName", com.azure.core.util.Context.NONE);
    }
}
```

### L3Networks_List

```java
/** Samples for L3Networks List. */
public final class L3NetworksListSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/L3Networks_ListBySubscription.json
     */
    /**
     * Sample code: List L3 networks for subscription.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listL3NetworksForSubscription(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.l3Networks().list(com.azure.core.util.Context.NONE);
    }
}
```

### L3Networks_ListByResourceGroup

```java
/** Samples for L3Networks ListByResourceGroup. */
public final class L3NetworksListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/L3Networks_ListByResourceGroup.json
     */
    /**
     * Sample code: List L3 networks for resource group.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listL3NetworksForResourceGroup(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.l3Networks().listByResourceGroup("resourceGroupName", com.azure.core.util.Context.NONE);
    }
}
```

### L3Networks_Update

```java
import com.azure.resourcemanager.networkcloud.models.L3Network;
import java.util.HashMap;
import java.util.Map;

/** Samples for L3Networks Update. */
public final class L3NetworksUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/L3Networks_Patch.json
     */
    /**
     * Sample code: Patch L3 network.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void patchL3Network(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        L3Network resource =
            manager
                .l3Networks()
                .getByResourceGroupWithResponse("resourceGroupName", "l3NetworkName", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("key1", "myvalue1", "key2", "myvalue2")).apply();
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

### MetricsConfigurations_CreateOrUpdate

```java
import com.azure.resourcemanager.networkcloud.models.ExtendedLocation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for MetricsConfigurations CreateOrUpdate. */
public final class MetricsConfigurationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/ClusterMetricsConfigurations_Create.json
     */
    /**
     * Sample code: Create or update metrics configuration of cluster.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void createOrUpdateMetricsConfigurationOfCluster(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .metricsConfigurations()
            .define("default")
            .withRegion("location")
            .withExistingCluster("resourceGroupName", "clusterName")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ExtendedLocation/customLocations/clusterExtendedLocationName")
                    .withType("CustomLocation"))
            .withCollectionInterval(15L)
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
            .withEnabledMetrics(Arrays.asList("metric1", "metric2"))
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

### MetricsConfigurations_Delete

```java
/** Samples for MetricsConfigurations Delete. */
public final class MetricsConfigurationsDeleteSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/ClusterMetricsConfigurations_Delete.json
     */
    /**
     * Sample code: Delete metrics configuration of cluster.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void deleteMetricsConfigurationOfCluster(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .metricsConfigurations()
            .delete("resourceGroupName", "clusterName", "default", com.azure.core.util.Context.NONE);
    }
}
```

### MetricsConfigurations_Get

```java
/** Samples for MetricsConfigurations Get. */
public final class MetricsConfigurationsGetSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/ClusterMetricsConfigurations_Get.json
     */
    /**
     * Sample code: Get metrics configuration of cluster.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void getMetricsConfigurationOfCluster(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .metricsConfigurations()
            .getWithResponse("resourceGroupName", "clusterName", "default", com.azure.core.util.Context.NONE);
    }
}
```

### MetricsConfigurations_ListByCluster

```java
/** Samples for MetricsConfigurations ListByCluster. */
public final class MetricsConfigurationsListByClusterSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/ClusterMetricsConfigurations_ListByCluster.json
     */
    /**
     * Sample code: List metrics configurations of the cluster.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listMetricsConfigurationsOfTheCluster(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .metricsConfigurations()
            .listByCluster("resourceGroupName", "clusterName", com.azure.core.util.Context.NONE);
    }
}
```

### MetricsConfigurations_Update

```java
import com.azure.resourcemanager.networkcloud.models.ClusterMetricsConfiguration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for MetricsConfigurations Update. */
public final class MetricsConfigurationsUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/ClusterMetricsConfigurations_Patch.json
     */
    /**
     * Sample code: Patch metrics configuration of cluster.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void patchMetricsConfigurationOfCluster(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        ClusterMetricsConfiguration resource =
            manager
                .metricsConfigurations()
                .getWithResponse("resourceGroupName", "clusterName", "default", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
            .withCollectionInterval(15L)
            .withEnabledMetrics(Arrays.asList("metric1", "metric2"))
            .apply();
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

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: List resource provider operations.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listResourceProviderOperations(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### RackSkus_Get

```java
/** Samples for RackSkus Get. */
public final class RackSkusGetSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/RackSkus_Get.json
     */
    /**
     * Sample code: Get rack SKU resource.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void getRackSKUResource(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.rackSkus().getWithResponse("rackSkuName", com.azure.core.util.Context.NONE);
    }
}
```

### RackSkus_List

```java
/** Samples for RackSkus List. */
public final class RackSkusListSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/RackSkus_ListBySubscription.json
     */
    /**
     * Sample code: List rack SKUs for subscription.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listRackSKUsForSubscription(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.rackSkus().list(com.azure.core.util.Context.NONE);
    }
}
```

### Racks_CreateOrUpdate

```java
import com.azure.resourcemanager.networkcloud.models.ExtendedLocation;
import java.util.HashMap;
import java.util.Map;

/** Samples for Racks CreateOrUpdate. */
public final class RacksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Racks_Create.json
     */
    /**
     * Sample code: Create or update rack.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void createOrUpdateRack(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .racks()
            .define("rackName")
            .withRegion("location")
            .withExistingResourceGroup("resourceGroupName")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ExtendedLocation/customLocations/clusterExtendedLocationName")
                    .withType("CustomLocation"))
            .withAvailabilityZone("1")
            .withRackLocation("Rack 28")
            .withRackSerialNumber("RACK_SERIAL_NUMBER")
            .withRackSkuId("RACK-TYPE-1")
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
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

### Racks_Delete

```java
/** Samples for Racks Delete. */
public final class RacksDeleteSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Racks_Delete.json
     */
    /**
     * Sample code: Delete rack.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void deleteRack(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.racks().delete("resourceGroupName", "rackName", com.azure.core.util.Context.NONE);
    }
}
```

### Racks_GetByResourceGroup

```java
/** Samples for Racks GetByResourceGroup. */
public final class RacksGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Racks_Get.json
     */
    /**
     * Sample code: Get rack.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void getRack(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .racks()
            .getByResourceGroupWithResponse("resourceGroupName", "rackName", com.azure.core.util.Context.NONE);
    }
}
```

### Racks_List

```java
/** Samples for Racks List. */
public final class RacksListSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Racks_ListBySubscription.json
     */
    /**
     * Sample code: List racks for subscription.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listRacksForSubscription(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.racks().list(com.azure.core.util.Context.NONE);
    }
}
```

### Racks_ListByResourceGroup

```java
/** Samples for Racks ListByResourceGroup. */
public final class RacksListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Racks_ListByResourceGroup.json
     */
    /**
     * Sample code: List racks for resource group.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listRacksForResourceGroup(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.racks().listByResourceGroup("resourceGroupName", com.azure.core.util.Context.NONE);
    }
}
```

### Racks_Update

```java
import com.azure.resourcemanager.networkcloud.models.Rack;
import java.util.HashMap;
import java.util.Map;

/** Samples for Racks Update. */
public final class RacksUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Racks_Patch.json
     */
    /**
     * Sample code: Patch rack.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void patchRack(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        Rack resource =
            manager
                .racks()
                .getByResourceGroupWithResponse("resourceGroupName", "rackName", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
            .withRackLocation("Rack 2B")
            .withRackSerialNumber("RACK_SERIAL_NUMBER")
            .apply();
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

### StorageAppliances_CreateOrUpdate

```java
import com.azure.resourcemanager.networkcloud.models.AdministrativeCredentials;
import com.azure.resourcemanager.networkcloud.models.ExtendedLocation;
import java.util.HashMap;
import java.util.Map;

/** Samples for StorageAppliances CreateOrUpdate. */
public final class StorageAppliancesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/StorageAppliances_Create.json
     */
    /**
     * Sample code: Create or update storage appliance.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void createOrUpdateStorageAppliance(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .storageAppliances()
            .define("storageApplianceName")
            .withRegion("location")
            .withExistingResourceGroup("resourceGroupName")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ExtendedLocation/customLocations/clusterExtendedLocationName")
                    .withType("CustomLocation"))
            .withAdministratorCredentials(
                new AdministrativeCredentials().withPassword("fakeTokenPlaceholder").withUsername("adminUser"))
            .withRackId(
                "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.NetworkCloud/racks/rackName")
            .withRackSlot(1L)
            .withSerialNumber("BM1219XXX")
            .withStorageApplianceSkuId("684E-3B16-399E")
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
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

### StorageAppliances_Delete

```java
/** Samples for StorageAppliances Delete. */
public final class StorageAppliancesDeleteSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/StorageAppliances_Delete.json
     */
    /**
     * Sample code: Delete storage appliance.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void deleteStorageAppliance(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .storageAppliances()
            .delete("resourceGroupName", "storageApplianceName", com.azure.core.util.Context.NONE);
    }
}
```

### StorageAppliances_DisableRemoteVendorManagement

```java
/** Samples for StorageAppliances DisableRemoteVendorManagement. */
public final class StorageAppliancesDisableRemoteVendorManagementSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/StorageAppliances_DisableRemoteVendorManagement.json
     */
    /**
     * Sample code: Turn off remote vendor management for storage appliance.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void turnOffRemoteVendorManagementForStorageAppliance(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .storageAppliances()
            .disableRemoteVendorManagement(
                "resourceGroupName", "storageApplianceName", com.azure.core.util.Context.NONE);
    }
}
```

### StorageAppliances_EnableRemoteVendorManagement

```java
import com.azure.resourcemanager.networkcloud.models.StorageApplianceEnableRemoteVendorManagementParameters;
import java.util.Arrays;

/** Samples for StorageAppliances EnableRemoteVendorManagement. */
public final class StorageAppliancesEnableRemoteVendorManagementSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/StorageAppliances_EnableRemoteVendorManagement.json
     */
    /**
     * Sample code: Turn on remote vendor management for storage appliance.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void turnOnRemoteVendorManagementForStorageAppliance(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .storageAppliances()
            .enableRemoteVendorManagement(
                "resourceGroupName",
                "storageApplianceName",
                new StorageApplianceEnableRemoteVendorManagementParameters()
                    .withSupportEndpoints(Arrays.asList("10.0.0.0/24")),
                com.azure.core.util.Context.NONE);
    }
}
```

### StorageAppliances_GetByResourceGroup

```java
/** Samples for StorageAppliances GetByResourceGroup. */
public final class StorageAppliancesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/StorageAppliances_Get.json
     */
    /**
     * Sample code: Get storage appliance.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void getStorageAppliance(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .storageAppliances()
            .getByResourceGroupWithResponse(
                "resourceGroupName", "storageApplianceName", com.azure.core.util.Context.NONE);
    }
}
```

### StorageAppliances_List

```java
/** Samples for StorageAppliances List. */
public final class StorageAppliancesListSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/StorageAppliances_ListBySubscription.json
     */
    /**
     * Sample code: List storage appliances for subscription.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listStorageAppliancesForSubscription(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.storageAppliances().list(com.azure.core.util.Context.NONE);
    }
}
```

### StorageAppliances_ListByResourceGroup

```java
/** Samples for StorageAppliances ListByResourceGroup. */
public final class StorageAppliancesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/StorageAppliances_ListByResourceGroup.json
     */
    /**
     * Sample code: List storage appliances for resource group.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listStorageAppliancesForResourceGroup(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.storageAppliances().listByResourceGroup("resourceGroupName", com.azure.core.util.Context.NONE);
    }
}
```

### StorageAppliances_RunReadCommands

```java
import com.azure.resourcemanager.networkcloud.models.StorageApplianceCommandSpecification;
import com.azure.resourcemanager.networkcloud.models.StorageApplianceRunReadCommandsParameters;
import java.util.Arrays;

/** Samples for StorageAppliances RunReadCommands. */
public final class StorageAppliancesRunReadCommandsSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/StorageAppliances_RunReadCommands.json
     */
    /**
     * Sample code: Run and retrieve output from read only commands on storage appliance.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void runAndRetrieveOutputFromReadOnlyCommandsOnStorageAppliance(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .storageAppliances()
            .runReadCommands(
                "resourceGroupName",
                "storageApplianceName",
                new StorageApplianceRunReadCommandsParameters()
                    .withCommands(Arrays.asList(new StorageApplianceCommandSpecification().withCommand("AlertList")))
                    .withLimitTimeSeconds(60L),
                com.azure.core.util.Context.NONE);
    }
}
```

### StorageAppliances_Update

```java
import com.azure.resourcemanager.networkcloud.models.StorageAppliance;
import java.util.HashMap;
import java.util.Map;

/** Samples for StorageAppliances Update. */
public final class StorageAppliancesUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/StorageAppliances_Patch.json
     */
    /**
     * Sample code: Patch storage appliance.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void patchStorageAppliance(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        StorageAppliance resource =
            manager
                .storageAppliances()
                .getByResourceGroupWithResponse(
                    "resourceGroupName", "storageApplianceName", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("key1", "myvalue1", "key2", "myvalue2")).withSerialNumber("BM1219XXX").apply();
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

### TrunkedNetworks_CreateOrUpdate

```java
import com.azure.resourcemanager.networkcloud.models.ExtendedLocation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for TrunkedNetworks CreateOrUpdate. */
public final class TrunkedNetworksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/TrunkedNetworks_Create.json
     */
    /**
     * Sample code: Create or update trunked network.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void createOrUpdateTrunkedNetwork(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .trunkedNetworks()
            .define("trunkedNetworkName")
            .withRegion("location")
            .withExistingResourceGroup("resourceGroupName")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ExtendedLocation/customLocations/clusterExtendedLocationName")
                    .withType("CustomLocation"))
            .withIsolationDomainIds(
                Arrays
                    .asList(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ManagedNetworkFabric/l2IsolationDomains/l2IsolationDomainName",
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ManagedNetworkFabric/l3IsolationDomains/l3IsolationDomainName"))
            .withVlans(Arrays.asList(12L, 14L))
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
            .withInterfaceName("eth0")
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

### TrunkedNetworks_Delete

```java
/** Samples for TrunkedNetworks Delete. */
public final class TrunkedNetworksDeleteSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/TrunkedNetworks_Delete.json
     */
    /**
     * Sample code: Delete trunked network.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void deleteTrunkedNetwork(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.trunkedNetworks().delete("resourceGroupName", "trunkedNetworkName", com.azure.core.util.Context.NONE);
    }
}
```

### TrunkedNetworks_GetByResourceGroup

```java
/** Samples for TrunkedNetworks GetByResourceGroup. */
public final class TrunkedNetworksGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/TrunkedNetworks_Get.json
     */
    /**
     * Sample code: Get Trunked network.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void getTrunkedNetwork(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .trunkedNetworks()
            .getByResourceGroupWithResponse(
                "resourceGroupName", "trunkedNetworkName", com.azure.core.util.Context.NONE);
    }
}
```

### TrunkedNetworks_List

```java
/** Samples for TrunkedNetworks List. */
public final class TrunkedNetworksListSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/TrunkedNetworks_ListBySubscription.json
     */
    /**
     * Sample code: List trunked networks for subscription.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listTrunkedNetworksForSubscription(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.trunkedNetworks().list(com.azure.core.util.Context.NONE);
    }
}
```

### TrunkedNetworks_ListByResourceGroup

```java
/** Samples for TrunkedNetworks ListByResourceGroup. */
public final class TrunkedNetworksListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/TrunkedNetworks_ListByResourceGroup.json
     */
    /**
     * Sample code: List Trunked networks for resource group.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listTrunkedNetworksForResourceGroup(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.trunkedNetworks().listByResourceGroup("resourceGroupName", com.azure.core.util.Context.NONE);
    }
}
```

### TrunkedNetworks_Update

```java
import com.azure.resourcemanager.networkcloud.models.TrunkedNetwork;
import java.util.HashMap;
import java.util.Map;

/** Samples for TrunkedNetworks Update. */
public final class TrunkedNetworksUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/TrunkedNetworks_Patch.json
     */
    /**
     * Sample code: Patch trunked network.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void patchTrunkedNetwork(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        TrunkedNetwork resource =
            manager
                .trunkedNetworks()
                .getByResourceGroupWithResponse(
                    "resourceGroupName", "trunkedNetworkName", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("key1", "myvalue1", "key2", "myvalue2")).apply();
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

### VirtualMachines_AttachVolume

```java
import com.azure.resourcemanager.networkcloud.models.VirtualMachineVolumeParameters;

/** Samples for VirtualMachines AttachVolume. */
public final class VirtualMachinesAttachVolumeSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/VirtualMachines_AttachVolume.json
     */
    /**
     * Sample code: Attach volume to virtual machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void attachVolumeToVirtualMachine(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .virtualMachines()
            .attachVolume(
                "resourceGroupName",
                "virtualMachineName",
                new VirtualMachineVolumeParameters()
                    .withVolumeId(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.NetworkCloud/volumes/volumeName"),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachines_CreateOrUpdate

```java
import com.azure.resourcemanager.networkcloud.models.DefaultGateway;
import com.azure.resourcemanager.networkcloud.models.ExtendedLocation;
import com.azure.resourcemanager.networkcloud.models.ImageRepositoryCredentials;
import com.azure.resourcemanager.networkcloud.models.NetworkAttachment;
import com.azure.resourcemanager.networkcloud.models.OsDisk;
import com.azure.resourcemanager.networkcloud.models.OsDiskCreateOption;
import com.azure.resourcemanager.networkcloud.models.OsDiskDeleteOption;
import com.azure.resourcemanager.networkcloud.models.SshPublicKey;
import com.azure.resourcemanager.networkcloud.models.StorageProfile;
import com.azure.resourcemanager.networkcloud.models.VirtualMachineBootMethod;
import com.azure.resourcemanager.networkcloud.models.VirtualMachineDeviceModelType;
import com.azure.resourcemanager.networkcloud.models.VirtualMachineIpAllocationMethod;
import com.azure.resourcemanager.networkcloud.models.VirtualMachinePlacementHint;
import com.azure.resourcemanager.networkcloud.models.VirtualMachinePlacementHintPodAffinityScope;
import com.azure.resourcemanager.networkcloud.models.VirtualMachinePlacementHintType;
import com.azure.resourcemanager.networkcloud.models.VirtualMachineSchedulingExecution;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualMachines CreateOrUpdate. */
public final class VirtualMachinesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/VirtualMachines_Create.json
     */
    /**
     * Sample code: Create or update virtual machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void createOrUpdateVirtualMachine(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .virtualMachines()
            .define("virtualMachineName")
            .withRegion("location")
            .withExistingResourceGroup("resourceGroupName")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ExtendedLocation/customLocations/clusterExtendedLocationName")
                    .withType("CustomLocation"))
            .withAdminUsername("username")
            .withCloudServicesNetworkAttachment(
                new NetworkAttachment()
                    .withAttachedNetworkId(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.NetworkCloud/cloudServicesNetworks/cloudServicesNetworkName")
                    .withIpAllocationMethod(VirtualMachineIpAllocationMethod.DYNAMIC))
            .withCpuCores(2L)
            .withMemorySizeGB(8L)
            .withStorageProfile(
                new StorageProfile()
                    .withOsDisk(
                        new OsDisk()
                            .withCreateOption(OsDiskCreateOption.EPHEMERAL)
                            .withDeleteOption(OsDiskDeleteOption.DELETE)
                            .withDiskSizeGB(120L))
                    .withVolumeAttachments(
                        Arrays
                            .asList(
                                "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.NetworkCloud/volumes/volumeName")))
            .withVmImage("myacr.azurecr.io/foobar:latest")
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
            .withBootMethod(VirtualMachineBootMethod.UEFI)
            .withNetworkAttachments(
                Arrays
                    .asList(
                        new NetworkAttachment()
                            .withAttachedNetworkId(
                                "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.NetworkCloud/l3Networks/l3NetworkName")
                            .withDefaultGateway(DefaultGateway.TRUE)
                            .withIpAllocationMethod(VirtualMachineIpAllocationMethod.DYNAMIC)
                            .withIpv4Address("198.51.100.1")
                            .withIpv6Address("2001:0db8:0000:0000:0000:0000:0000:0000")
                            .withNetworkAttachmentName("netAttachName01")))
            .withNetworkData("bmV0d29ya0RhdGVTYW1wbGU=")
            .withPlacementHints(
                Arrays
                    .asList(
                        new VirtualMachinePlacementHint()
                            .withHintType(VirtualMachinePlacementHintType.AFFINITY)
                            .withResourceId(
                                "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.NetworkCloud/racks/rackName")
                            .withSchedulingExecution(VirtualMachineSchedulingExecution.HARD)
                            .withScope(VirtualMachinePlacementHintPodAffinityScope.fromString(""))))
            .withSshPublicKeys(Arrays.asList(new SshPublicKey().withKeyData("fakeTokenPlaceholder")))
            .withUserData("dXNlckRhdGVTYW1wbGU=")
            .withVmDeviceModel(VirtualMachineDeviceModelType.T2)
            .withVmImageRepositoryCredentials(
                new ImageRepositoryCredentials()
                    .withPassword("fakeTokenPlaceholder")
                    .withRegistryUrl("myacr.azurecr.io")
                    .withUsername("myuser"))
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

### VirtualMachines_Delete

```java
/** Samples for VirtualMachines Delete. */
public final class VirtualMachinesDeleteSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/VirtualMachines_Delete.json
     */
    /**
     * Sample code: Delete virtual machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void deleteVirtualMachine(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.virtualMachines().delete("resourceGroupName", "virtualMachineName", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachines_DetachVolume

```java
import com.azure.resourcemanager.networkcloud.models.VirtualMachineVolumeParameters;

/** Samples for VirtualMachines DetachVolume. */
public final class VirtualMachinesDetachVolumeSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/VirtualMachines_DetachVolume.json
     */
    /**
     * Sample code: Detach volume from virtual machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void detachVolumeFromVirtualMachine(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .virtualMachines()
            .detachVolume(
                "resourceGroupName",
                "virtualMachineName",
                new VirtualMachineVolumeParameters()
                    .withVolumeId(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.NetworkCloud/volumes/volumeName"),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachines_GetByResourceGroup

```java
/** Samples for VirtualMachines GetByResourceGroup. */
public final class VirtualMachinesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/VirtualMachines_Get.json
     */
    /**
     * Sample code: Get virtual machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void getVirtualMachine(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .virtualMachines()
            .getByResourceGroupWithResponse(
                "resourceGroupName", "virtualMachineName", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachines_List

```java
/** Samples for VirtualMachines List. */
public final class VirtualMachinesListSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/VirtualMachines_ListBySubscription.json
     */
    /**
     * Sample code: List virtual machines for subscription.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listVirtualMachinesForSubscription(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.virtualMachines().list(com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachines_ListByResourceGroup

```java
/** Samples for VirtualMachines ListByResourceGroup. */
public final class VirtualMachinesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/VirtualMachines_ListByResourceGroup.json
     */
    /**
     * Sample code: List virtual machines for resource group.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listVirtualMachinesForResourceGroup(
        com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.virtualMachines().listByResourceGroup("resourceGroupName", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachines_PowerOff

```java
import com.azure.resourcemanager.networkcloud.models.SkipShutdown;
import com.azure.resourcemanager.networkcloud.models.VirtualMachinePowerOffParameters;

/** Samples for VirtualMachines PowerOff. */
public final class VirtualMachinesPowerOffSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/VirtualMachines_PowerOff.json
     */
    /**
     * Sample code: Power off virtual machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void powerOffVirtualMachine(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .virtualMachines()
            .powerOff(
                "resourceGroupName",
                "virtualMachineName",
                new VirtualMachinePowerOffParameters().withSkipShutdown(SkipShutdown.TRUE),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachines_Reimage

```java
/** Samples for VirtualMachines Reimage. */
public final class VirtualMachinesReimageSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/VirtualMachines_Reimage.json
     */
    /**
     * Sample code: Reimage virtual machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void reimageVirtualMachine(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.virtualMachines().reimage("resourceGroupName", "virtualMachineName", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachines_Restart

```java
/** Samples for VirtualMachines Restart. */
public final class VirtualMachinesRestartSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/VirtualMachines_Restart.json
     */
    /**
     * Sample code: Restart virtual machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void restartVirtualMachine(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.virtualMachines().restart("resourceGroupName", "virtualMachineName", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachines_Start

```java
/** Samples for VirtualMachines Start. */
public final class VirtualMachinesStartSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/VirtualMachines_Start.json
     */
    /**
     * Sample code: Start virtual machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void startVirtualMachine(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.virtualMachines().start("resourceGroupName", "virtualMachineName", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachines_Update

```java
import com.azure.resourcemanager.networkcloud.models.ImageRepositoryCredentials;
import com.azure.resourcemanager.networkcloud.models.VirtualMachine;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualMachines Update. */
public final class VirtualMachinesUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/VirtualMachines_Patch.json
     */
    /**
     * Sample code: Patch virtual machine.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void patchVirtualMachine(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        VirtualMachine resource =
            manager
                .virtualMachines()
                .getByResourceGroupWithResponse(
                    "resourceGroupName", "virtualMachineName", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
            .withVmImageRepositoryCredentials(
                new ImageRepositoryCredentials()
                    .withPassword("fakeTokenPlaceholder")
                    .withRegistryUrl("myacr.azurecr.io")
                    .withUsername("myuser"))
            .apply();
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

### Volumes_CreateOrUpdate

```java
import com.azure.resourcemanager.networkcloud.models.ExtendedLocation;
import java.util.HashMap;
import java.util.Map;

/** Samples for Volumes CreateOrUpdate. */
public final class VolumesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Volumes_Create.json
     */
    /**
     * Sample code: Create or update volume.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void createOrUpdateVolume(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .volumes()
            .define("volumeName")
            .withRegion("location")
            .withExistingResourceGroup("resourceGroupName")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/123e4567-e89b-12d3-a456-426655440000/resourceGroups/resourceGroupName/providers/Microsoft.ExtendedLocation/customLocations/clusterExtendedLocationName")
                    .withType("CustomLocation"))
            .withSizeMiB(10000L)
            .withTags(mapOf("key1", "myvalue1", "key2", "myvalue2"))
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

### Volumes_Delete

```java
/** Samples for Volumes Delete. */
public final class VolumesDeleteSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Volumes_Delete.json
     */
    /**
     * Sample code: Delete volume.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void deleteVolume(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.volumes().delete("resourceGroupName", "volumeName", com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_GetByResourceGroup

```java
/** Samples for Volumes GetByResourceGroup. */
public final class VolumesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Volumes_Get.json
     */
    /**
     * Sample code: Get volume.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void getVolume(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager
            .volumes()
            .getByResourceGroupWithResponse("resourceGroupName", "volumeName", com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_List

```java
/** Samples for Volumes List. */
public final class VolumesListSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Volumes_ListBySubscription.json
     */
    /**
     * Sample code: List volume for subscription.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listVolumeForSubscription(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.volumes().list(com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_ListByResourceGroup

```java
/** Samples for Volumes ListByResourceGroup. */
public final class VolumesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Volumes_ListByResourceGroup.json
     */
    /**
     * Sample code: List volumes for resource group.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void listVolumesForResourceGroup(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        manager.volumes().listByResourceGroup("resourceGroupName", com.azure.core.util.Context.NONE);
    }
}
```

### Volumes_Update

```java
import com.azure.resourcemanager.networkcloud.models.Volume;
import java.util.HashMap;
import java.util.Map;

/** Samples for Volumes Update. */
public final class VolumesUpdateSamples {
    /*
     * x-ms-original-file: specification/networkcloud/resource-manager/Microsoft.NetworkCloud/preview/2023-05-01-preview/examples/Volumes_Patch.json
     */
    /**
     * Sample code: Patch volume.
     *
     * @param manager Entry point to NetworkCloudManager.
     */
    public static void patchVolume(com.azure.resourcemanager.networkcloud.NetworkCloudManager manager) {
        Volume resource =
            manager
                .volumes()
                .getByResourceGroupWithResponse("resourceGroupName", "volumeName", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("key1", "myvalue1", "key2", "myvalue2")).apply();
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

