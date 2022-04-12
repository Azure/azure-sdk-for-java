# Code snippets and samples


## AgentPools

- [CreateOrUpdate](#agentpools_createorupdate)
- [Delete](#agentpools_delete)
- [Get](#agentpools_get)
- [GetAvailableAgentPoolVersions](#agentpools_getavailableagentpoolversions)
- [GetUpgradeProfile](#agentpools_getupgradeprofile)
- [List](#agentpools_list)
- [UpgradeNodeImageVersion](#agentpools_upgradenodeimageversion)

## ContainerServices

- [ListOrchestrators](#containerservices_listorchestrators)

## MaintenanceConfigurations

- [CreateOrUpdate](#maintenanceconfigurations_createorupdate)
- [Delete](#maintenanceconfigurations_delete)
- [Get](#maintenanceconfigurations_get)
- [ListByManagedCluster](#maintenanceconfigurations_listbymanagedcluster)

## ManagedClusters

- [CreateOrUpdate](#managedclusters_createorupdate)
- [Delete](#managedclusters_delete)
- [GetAccessProfile](#managedclusters_getaccessprofile)
- [GetByResourceGroup](#managedclusters_getbyresourcegroup)
- [GetCommandResult](#managedclusters_getcommandresult)
- [GetOSOptions](#managedclusters_getosoptions)
- [GetUpgradeProfile](#managedclusters_getupgradeprofile)
- [List](#managedclusters_list)
- [ListByResourceGroup](#managedclusters_listbyresourcegroup)
- [ListClusterAdminCredentials](#managedclusters_listclusteradmincredentials)
- [ListClusterMonitoringUserCredentials](#managedclusters_listclustermonitoringusercredentials)
- [ListClusterUserCredentials](#managedclusters_listclusterusercredentials)
- [ListOutboundNetworkDependenciesEndpoints](#managedclusters_listoutboundnetworkdependenciesendpoints)
- [ResetAadProfile](#managedclusters_resetaadprofile)
- [ResetServicePrincipalProfile](#managedclusters_resetserviceprincipalprofile)
- [RotateClusterCertificates](#managedclusters_rotateclustercertificates)
- [RunCommand](#managedclusters_runcommand)
- [Start](#managedclusters_start)
- [Stop](#managedclusters_stop)
- [UpdateTags](#managedclusters_updatetags)

## OpenShiftManagedClusters

- [CreateOrUpdate](#openshiftmanagedclusters_createorupdate)
- [Delete](#openshiftmanagedclusters_delete)
- [GetByResourceGroup](#openshiftmanagedclusters_getbyresourcegroup)
- [List](#openshiftmanagedclusters_list)
- [ListByResourceGroup](#openshiftmanagedclusters_listbyresourcegroup)
- [UpdateTags](#openshiftmanagedclusters_updatetags)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [List](#privateendpointconnections_list)
- [Update](#privateendpointconnections_update)

## PrivateLinkResources

- [List](#privatelinkresources_list)

## ResolvePrivateLinkServiceId

- [Post](#resolveprivatelinkserviceid_post)

## Snapshots

- [CreateOrUpdate](#snapshots_createorupdate)
- [Delete](#snapshots_delete)
- [GetByResourceGroup](#snapshots_getbyresourcegroup)
- [List](#snapshots_list)
- [ListByResourceGroup](#snapshots_listbyresourcegroup)
- [UpdateTags](#snapshots_updatetags)
### AgentPools_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.containerservice.generated.models.AgentPool;
import com.azure.resourcemanager.containerservice.generated.models.AgentPoolMode;
import com.azure.resourcemanager.containerservice.generated.models.Code;
import com.azure.resourcemanager.containerservice.generated.models.CreationData;
import com.azure.resourcemanager.containerservice.generated.models.GpuInstanceProfile;
import com.azure.resourcemanager.containerservice.generated.models.KubeletConfig;
import com.azure.resourcemanager.containerservice.generated.models.LinuxOSConfig;
import com.azure.resourcemanager.containerservice.generated.models.OSDiskType;
import com.azure.resourcemanager.containerservice.generated.models.OSType;
import com.azure.resourcemanager.containerservice.generated.models.Ossku;
import com.azure.resourcemanager.containerservice.generated.models.PowerState;
import com.azure.resourcemanager.containerservice.generated.models.ScaleSetEvictionPolicy;
import com.azure.resourcemanager.containerservice.generated.models.ScaleSetPriority;
import com.azure.resourcemanager.containerservice.generated.models.SysctlConfig;
import com.azure.resourcemanager.containerservice.generated.models.WorkloadRuntime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for AgentPools CreateOrUpdate. */
public final class AgentPoolsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/AgentPoolsCreate_EnableFIPS.json
     */
    /**
     * Sample code: Create Agent Pool with FIPS enabled OS.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createAgentPoolWithFIPSEnabledOS(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .agentPools()
            .define("agentpool1")
            .withExistingManagedCluster("rg1", "clustername1")
            .withCount(3)
            .withVmSize("Standard_DS2_v2")
            .withOsType(OSType.LINUX)
            .withOrchestratorVersion("")
            .withEnableFips(true)
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/AgentPoolsCreate_GPUMIG.json
     */
    /**
     * Sample code: Create Agent Pool with GPUMIG.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createAgentPoolWithGPUMIG(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .agentPools()
            .define("agentpool1")
            .withExistingManagedCluster("rg1", "clustername1")
            .withCount(3)
            .withVmSize("Standard_ND96asr_v4")
            .withOsType(OSType.LINUX)
            .withOrchestratorVersion("")
            .withKubeletConfig(
                new KubeletConfig()
                    .withCpuManagerPolicy("static")
                    .withCpuCfsQuota(true)
                    .withCpuCfsQuotaPeriod("200ms")
                    .withImageGcHighThreshold(90)
                    .withImageGcLowThreshold(70)
                    .withTopologyManagerPolicy("best-effort")
                    .withAllowedUnsafeSysctls(Arrays.asList("kernel.msg*", "net.core.somaxconn"))
                    .withFailSwapOn(false))
            .withLinuxOSConfig(
                new LinuxOSConfig()
                    .withSysctls(
                        new SysctlConfig()
                            .withNetCoreWmemDefault(12345)
                            .withNetIpv4TcpTwReuse(true)
                            .withNetIpv4IpLocalPortRange("20000 60000")
                            .withKernelThreadsMax(99999))
                    .withTransparentHugePageEnabled("always")
                    .withTransparentHugePageDefrag("madvise")
                    .withSwapFileSizeMB(1500))
            .withGpuInstanceProfile(GpuInstanceProfile.MIG2G)
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/AgentPoolsCreate_Update.json
     */
    /**
     * Sample code: Create/Update Agent Pool.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createUpdateAgentPool(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .agentPools()
            .define("agentpool1")
            .withExistingManagedCluster("rg1", "clustername1")
            .withTags(mapOf("name1", "val1"))
            .withCount(3)
            .withVmSize("Standard_DS1_v2")
            .withOsType(OSType.LINUX)
            .withMode(AgentPoolMode.USER)
            .withOrchestratorVersion("")
            .withScaleSetPriority(ScaleSetPriority.SPOT)
            .withScaleSetEvictionPolicy(ScaleSetEvictionPolicy.DELETE)
            .withNodeLabels(mapOf("key1", "val1"))
            .withNodeTaints(Arrays.asList("Key1=Value1:NoSchedule"))
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/AgentPools_Start.json
     */
    /**
     * Sample code: Start Agent Pool.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void startAgentPool(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .agentPools()
            .define("agentpool1")
            .withExistingManagedCluster("rg1", "clustername1")
            .withPowerState(new PowerState().withCode(Code.RUNNING))
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/AgentPoolsCreate_Spot.json
     */
    /**
     * Sample code: Create Spot Agent Pool.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createSpotAgentPool(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .agentPools()
            .define("agentpool1")
            .withExistingManagedCluster("rg1", "clustername1")
            .withTags(mapOf("name1", "val1"))
            .withCount(3)
            .withVmSize("Standard_DS1_v2")
            .withOsType(OSType.LINUX)
            .withOrchestratorVersion("")
            .withScaleSetPriority(ScaleSetPriority.SPOT)
            .withScaleSetEvictionPolicy(ScaleSetEvictionPolicy.DELETE)
            .withNodeLabels(mapOf("key1", "val1"))
            .withNodeTaints(Arrays.asList("Key1=Value1:NoSchedule"))
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/AgentPoolsCreate_Ephemeral.json
     */
    /**
     * Sample code: Create Agent Pool with Ephemeral OS Disk.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createAgentPoolWithEphemeralOSDisk(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .agentPools()
            .define("agentpool1")
            .withExistingManagedCluster("rg1", "clustername1")
            .withCount(3)
            .withVmSize("Standard_DS2_v2")
            .withOsDiskSizeGB(64)
            .withOsDiskType(OSDiskType.EPHEMERAL)
            .withOsType(OSType.LINUX)
            .withOrchestratorVersion("")
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/AgentPoolsCreate_EnableEncryptionAtHost.json
     */
    /**
     * Sample code: Create Agent Pool with EncryptionAtHost enabled.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createAgentPoolWithEncryptionAtHostEnabled(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .agentPools()
            .define("agentpool1")
            .withExistingManagedCluster("rg1", "clustername1")
            .withCount(3)
            .withVmSize("Standard_DS2_v2")
            .withOsType(OSType.LINUX)
            .withOrchestratorVersion("")
            .withEnableEncryptionAtHost(true)
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/AgentPoolsCreate_EnableUltraSSD.json
     */
    /**
     * Sample code: Create Agent Pool with UltraSSD enabled.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createAgentPoolWithUltraSSDEnabled(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .agentPools()
            .define("agentpool1")
            .withExistingManagedCluster("rg1", "clustername1")
            .withCount(3)
            .withVmSize("Standard_DS2_v2")
            .withOsType(OSType.LINUX)
            .withOrchestratorVersion("")
            .withEnableUltraSsd(true)
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/AgentPoolsCreate_WasmWasi.json
     */
    /**
     * Sample code: Create Agent Pool with Krustlet and the WASI runtime.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createAgentPoolWithKrustletAndTheWASIRuntime(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .agentPools()
            .define("agentpool1")
            .withExistingManagedCluster("rg1", "clustername1")
            .withCount(3)
            .withVmSize("Standard_DS2_v2")
            .withOsDiskSizeGB(64)
            .withWorkloadRuntime(WorkloadRuntime.WASM_WASI)
            .withOsType(OSType.LINUX)
            .withMode(AgentPoolMode.USER)
            .withOrchestratorVersion("")
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/AgentPoolsCreate_Snapshot.json
     */
    /**
     * Sample code: Create Agent Pool using an agent pool snapshot.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createAgentPoolUsingAnAgentPoolSnapshot(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .agentPools()
            .define("agentpool1")
            .withExistingManagedCluster("rg1", "clustername1")
            .withCount(3)
            .withVmSize("Standard_DS2_v2")
            .withOsType(OSType.LINUX)
            .withOrchestratorVersion("")
            .withEnableFips(true)
            .withCreationData(
                new CreationData()
                    .withSourceResourceId(
                        "/subscriptions/subid1/resourceGroups/rg1/providers/Microsoft.ContainerService/snapshots/snapshot1"))
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/AgentPoolsCreate_PPG.json
     */
    /**
     * Sample code: Create Agent Pool with PPG.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createAgentPoolWithPPG(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .agentPools()
            .define("agentpool1")
            .withExistingManagedCluster("rg1", "clustername1")
            .withCount(3)
            .withVmSize("Standard_DS2_v2")
            .withOsType(OSType.LINUX)
            .withOrchestratorVersion("")
            .withProximityPlacementGroupId(
                "/subscriptions/subid1/resourcegroups/rg1/providers//Microsoft.Compute/proximityPlacementGroups/ppg1")
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/AgentPoolsCreate_CustomNodeConfig.json
     */
    /**
     * Sample code: Create Agent Pool with KubeletConfig and LinuxOSConfig.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createAgentPoolWithKubeletConfigAndLinuxOSConfig(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .agentPools()
            .define("agentpool1")
            .withExistingManagedCluster("rg1", "clustername1")
            .withCount(3)
            .withVmSize("Standard_DS2_v2")
            .withOsType(OSType.LINUX)
            .withOrchestratorVersion("")
            .withKubeletConfig(
                new KubeletConfig()
                    .withCpuManagerPolicy("static")
                    .withCpuCfsQuota(true)
                    .withCpuCfsQuotaPeriod("200ms")
                    .withImageGcHighThreshold(90)
                    .withImageGcLowThreshold(70)
                    .withTopologyManagerPolicy("best-effort")
                    .withAllowedUnsafeSysctls(Arrays.asList("kernel.msg*", "net.core.somaxconn"))
                    .withFailSwapOn(false))
            .withLinuxOSConfig(
                new LinuxOSConfig()
                    .withSysctls(
                        new SysctlConfig()
                            .withNetCoreWmemDefault(12345)
                            .withNetIpv4TcpTwReuse(true)
                            .withNetIpv4IpLocalPortRange("20000 60000")
                            .withKernelThreadsMax(99999))
                    .withTransparentHugePageEnabled("always")
                    .withTransparentHugePageDefrag("madvise")
                    .withSwapFileSizeMB(1500))
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/AgentPools_Stop.json
     */
    /**
     * Sample code: Stop Agent Pool.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void stopAgentPool(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .agentPools()
            .define("agentpool1")
            .withExistingManagedCluster("rg1", "clustername1")
            .withPowerState(new PowerState().withCode(Code.STOPPED))
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/AgentPoolsCreate_OSSKU.json
     */
    /**
     * Sample code: Create Agent Pool with OSSKU.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createAgentPoolWithOSSKU(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .agentPools()
            .define("agentpool1")
            .withExistingManagedCluster("rg1", "clustername1")
            .withCount(3)
            .withVmSize("Standard_DS2_v2")
            .withOsType(OSType.LINUX)
            .withOsSku(Ossku.CBLMARINER)
            .withOrchestratorVersion("")
            .withKubeletConfig(
                new KubeletConfig()
                    .withCpuManagerPolicy("static")
                    .withCpuCfsQuota(true)
                    .withCpuCfsQuotaPeriod("200ms")
                    .withImageGcHighThreshold(90)
                    .withImageGcLowThreshold(70)
                    .withTopologyManagerPolicy("best-effort")
                    .withAllowedUnsafeSysctls(Arrays.asList("kernel.msg*", "net.core.somaxconn"))
                    .withFailSwapOn(false))
            .withLinuxOSConfig(
                new LinuxOSConfig()
                    .withSysctls(
                        new SysctlConfig()
                            .withNetCoreWmemDefault(12345)
                            .withNetIpv4TcpTwReuse(true)
                            .withNetIpv4IpLocalPortRange("20000 60000")
                            .withKernelThreadsMax(99999))
                    .withTransparentHugePageEnabled("always")
                    .withTransparentHugePageDefrag("madvise")
                    .withSwapFileSizeMB(1500))
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/AgentPools_Update.json
     */
    /**
     * Sample code: Update Agent Pool.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void updateAgentPool(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        AgentPool resource =
            manager.agentPools().getWithResponse("rg1", "clustername1", "agentpool1", Context.NONE).getValue();
        resource
            .update()
            .withCount(3)
            .withVmSize("Standard_DS1_v2")
            .withOsType(OSType.LINUX)
            .withMaxCount(2)
            .withMinCount(2)
            .withEnableAutoScaling(true)
            .withOrchestratorVersion("")
            .withScaleSetPriority(ScaleSetPriority.SPOT)
            .withScaleSetEvictionPolicy(ScaleSetEvictionPolicy.DELETE)
            .withNodeTaints(Arrays.asList("Key1=Value1:NoSchedule"))
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

### AgentPools_Delete

```java
import com.azure.core.util.Context;

/** Samples for AgentPools Delete. */
public final class AgentPoolsDeleteSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/AgentPoolsDelete.json
     */
    /**
     * Sample code: Delete Agent Pool.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void deleteAgentPool(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.agentPools().delete("rg1", "clustername1", "agentpool1", Context.NONE);
    }
}
```

### AgentPools_Get

```java
import com.azure.core.util.Context;

/** Samples for AgentPools Get. */
public final class AgentPoolsGetSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/AgentPoolsGet.json
     */
    /**
     * Sample code: Get Agent Pool.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void getAgentPool(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.agentPools().getWithResponse("rg1", "clustername1", "agentpool1", Context.NONE);
    }
}
```

### AgentPools_GetAvailableAgentPoolVersions

```java
import com.azure.core.util.Context;

/** Samples for AgentPools GetAvailableAgentPoolVersions. */
public final class AgentPoolsGetAvailableAgentPoolVersionsSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/AgentPoolsGetAgentPoolAvailableVersions.json
     */
    /**
     * Sample code: Get available versions for agent pool.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void getAvailableVersionsForAgentPool(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.agentPools().getAvailableAgentPoolVersionsWithResponse("rg1", "clustername1", Context.NONE);
    }
}
```

### AgentPools_GetUpgradeProfile

```java
import com.azure.core.util.Context;

/** Samples for AgentPools GetUpgradeProfile. */
public final class AgentPoolsGetUpgradeProfileSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/AgentPoolsGetUpgradeProfile.json
     */
    /**
     * Sample code: Get Upgrade Profile for Agent Pool.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void getUpgradeProfileForAgentPool(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.agentPools().getUpgradeProfileWithResponse("rg1", "clustername1", "agentpool1", Context.NONE);
    }
}
```

### AgentPools_List

```java
import com.azure.core.util.Context;

/** Samples for AgentPools List. */
public final class AgentPoolsListSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/AgentPoolsList.json
     */
    /**
     * Sample code: List Agent Pools by Managed Cluster.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void listAgentPoolsByManagedCluster(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.agentPools().list("rg1", "clustername1", Context.NONE);
    }
}
```

### AgentPools_UpgradeNodeImageVersion

```java
import com.azure.core.util.Context;

/** Samples for AgentPools UpgradeNodeImageVersion. */
public final class AgentPoolsUpgradeNodeImageVersionSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/AgentPoolsUpgradeNodeImageVersion.json
     */
    /**
     * Sample code: Upgrade Agent Pool Node Image Version.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void upgradeAgentPoolNodeImageVersion(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.agentPools().upgradeNodeImageVersion("rg1", "clustername1", "agentpool1", Context.NONE);
    }
}
```

### ContainerServices_ListOrchestrators

```java
import com.azure.core.util.Context;

/** Samples for ContainerServices ListOrchestrators. */
public final class ContainerServicesListOrchestratorsSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2019-08-01/examples/ContainerServiceListOrchestrators.json
     */
    /**
     * Sample code: List Container Service Orchestrators.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void listContainerServiceOrchestrators(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.containerServices().listOrchestratorsWithResponse("location1", null, Context.NONE);
    }
}
```

### MaintenanceConfigurations_CreateOrUpdate

```java
import com.azure.resourcemanager.containerservice.generated.models.TimeInWeek;
import com.azure.resourcemanager.containerservice.generated.models.TimeSpan;
import com.azure.resourcemanager.containerservice.generated.models.WeekDay;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Samples for MaintenanceConfigurations CreateOrUpdate. */
public final class MaintenanceConfigurationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/MaintenanceConfigurationsCreate_Update.json
     */
    /**
     * Sample code: Create/Update Maintenance Configuration.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createUpdateMaintenanceConfiguration(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .maintenanceConfigurations()
            .define("default")
            .withExistingManagedCluster("rg1", "clustername1")
            .withTimeInWeek(Arrays.asList(new TimeInWeek().withDay(WeekDay.MONDAY).withHourSlots(Arrays.asList(1, 2))))
            .withNotAllowedTime(
                Arrays
                    .asList(
                        new TimeSpan()
                            .withStart(OffsetDateTime.parse("2020-11-26T03:00:00Z"))
                            .withEnd(OffsetDateTime.parse("2020-11-30T12:00:00Z"))))
            .create();
    }
}
```

### MaintenanceConfigurations_Delete

```java
import com.azure.core.util.Context;

/** Samples for MaintenanceConfigurations Delete. */
public final class MaintenanceConfigurationsDeleteSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/MaintenanceConfigurationsDelete.json
     */
    /**
     * Sample code: Delete Maintenance Configuration.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void deleteMaintenanceConfiguration(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.maintenanceConfigurations().deleteWithResponse("rg1", "clustername1", "default", Context.NONE);
    }
}
```

### MaintenanceConfigurations_Get

```java
import com.azure.core.util.Context;

/** Samples for MaintenanceConfigurations Get. */
public final class MaintenanceConfigurationsGetSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/MaintenanceConfigurationsGet.json
     */
    /**
     * Sample code: Get Maintenance Configuration.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void getMaintenanceConfiguration(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.maintenanceConfigurations().getWithResponse("rg1", "clustername1", "default", Context.NONE);
    }
}
```

### MaintenanceConfigurations_ListByManagedCluster

```java
import com.azure.core.util.Context;

/** Samples for MaintenanceConfigurations ListByManagedCluster. */
public final class MaintenanceConfigurationsListByManagedClusterSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/MaintenanceConfigurationsList.json
     */
    /**
     * Sample code: List maintenance configurations by Managed Cluster.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void listMaintenanceConfigurationsByManagedCluster(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.maintenanceConfigurations().listByManagedCluster("rg1", "clustername1", Context.NONE);
    }
}
```

### ManagedClusters_CreateOrUpdate

```java
import com.azure.resourcemanager.containerservice.generated.models.AgentPoolMode;
import com.azure.resourcemanager.containerservice.generated.models.AgentPoolType;
import com.azure.resourcemanager.containerservice.generated.models.ContainerServiceLinuxProfile;
import com.azure.resourcemanager.containerservice.generated.models.ContainerServiceNetworkProfile;
import com.azure.resourcemanager.containerservice.generated.models.ContainerServiceSshConfiguration;
import com.azure.resourcemanager.containerservice.generated.models.ContainerServiceSshPublicKey;
import com.azure.resourcemanager.containerservice.generated.models.CreationData;
import com.azure.resourcemanager.containerservice.generated.models.Expander;
import com.azure.resourcemanager.containerservice.generated.models.GpuInstanceProfile;
import com.azure.resourcemanager.containerservice.generated.models.IpFamily;
import com.azure.resourcemanager.containerservice.generated.models.LicenseType;
import com.azure.resourcemanager.containerservice.generated.models.LoadBalancerSku;
import com.azure.resourcemanager.containerservice.generated.models.ManagedClusterAadProfile;
import com.azure.resourcemanager.containerservice.generated.models.ManagedClusterAgentPoolProfile;
import com.azure.resourcemanager.containerservice.generated.models.ManagedClusterApiServerAccessProfile;
import com.azure.resourcemanager.containerservice.generated.models.ManagedClusterHttpProxyConfig;
import com.azure.resourcemanager.containerservice.generated.models.ManagedClusterIdentity;
import com.azure.resourcemanager.containerservice.generated.models.ManagedClusterLoadBalancerProfile;
import com.azure.resourcemanager.containerservice.generated.models.ManagedClusterLoadBalancerProfileManagedOutboundIPs;
import com.azure.resourcemanager.containerservice.generated.models.ManagedClusterManagedOutboundIpProfile;
import com.azure.resourcemanager.containerservice.generated.models.ManagedClusterNatGatewayProfile;
import com.azure.resourcemanager.containerservice.generated.models.ManagedClusterPodIdentityProfile;
import com.azure.resourcemanager.containerservice.generated.models.ManagedClusterPropertiesAutoScalerProfile;
import com.azure.resourcemanager.containerservice.generated.models.ManagedClusterSecurityProfile;
import com.azure.resourcemanager.containerservice.generated.models.ManagedClusterSecurityProfileAzureDefender;
import com.azure.resourcemanager.containerservice.generated.models.ManagedClusterServicePrincipalProfile;
import com.azure.resourcemanager.containerservice.generated.models.ManagedClusterSku;
import com.azure.resourcemanager.containerservice.generated.models.ManagedClusterSkuName;
import com.azure.resourcemanager.containerservice.generated.models.ManagedClusterSkuTier;
import com.azure.resourcemanager.containerservice.generated.models.ManagedClusterWindowsProfile;
import com.azure.resourcemanager.containerservice.generated.models.ManagedServiceIdentityUserAssignedIdentitiesValue;
import com.azure.resourcemanager.containerservice.generated.models.OSType;
import com.azure.resourcemanager.containerservice.generated.models.Ossku;
import com.azure.resourcemanager.containerservice.generated.models.OutboundType;
import com.azure.resourcemanager.containerservice.generated.models.ResourceIdentityType;
import com.azure.resourcemanager.containerservice.generated.models.ScaleDownMode;
import com.azure.resourcemanager.containerservice.generated.models.WindowsGmsaProfile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for ManagedClusters CreateOrUpdate. */
public final class ManagedClustersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersCreate_HTTPProxy.json
     */
    /**
     * Sample code: Create Managed Cluster with HTTP proxy configured.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createManagedClusterWithHTTPProxyConfigured(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .define("clustername1")
            .withRegion("location1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("archv2", "", "tier", "production"))
            .withSku(new ManagedClusterSku().withName(ManagedClusterSkuName.BASIC).withTier(ManagedClusterSkuTier.FREE))
            .withKubernetesVersion("")
            .withDnsPrefix("dnsprefix1")
            .withAgentPoolProfiles(
                Arrays
                    .asList(
                        new ManagedClusterAgentPoolProfile()
                            .withCount(3)
                            .withVmSize("Standard_DS2_v2")
                            .withOsType(OSType.LINUX)
                            .withType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                            .withMode(AgentPoolMode.SYSTEM)
                            .withEnableNodePublicIp(true)
                            .withName("nodepool1")))
            .withLinuxProfile(
                new ContainerServiceLinuxProfile()
                    .withAdminUsername("azureuser")
                    .withSsh(
                        new ContainerServiceSshConfiguration()
                            .withPublicKeys(Arrays.asList(new ContainerServiceSshPublicKey().withKeyData("keydata")))))
            .withWindowsProfile(
                new ManagedClusterWindowsProfile()
                    .withAdminUsername("azureuser")
                    .withAdminPassword("replacePassword1234$"))
            .withServicePrincipalProfile(
                new ManagedClusterServicePrincipalProfile().withClientId("clientid").withSecret("secret"))
            .withAddonProfiles(mapOf())
            .withEnableRbac(true)
            .withEnablePodSecurityPolicy(true)
            .withNetworkProfile(
                new ContainerServiceNetworkProfile()
                    .withOutboundType(OutboundType.LOAD_BALANCER)
                    .withLoadBalancerSku(LoadBalancerSku.STANDARD)
                    .withLoadBalancerProfile(
                        new ManagedClusterLoadBalancerProfile()
                            .withManagedOutboundIPs(
                                new ManagedClusterLoadBalancerProfileManagedOutboundIPs().withCount(2))))
            .withAutoScalerProfile(
                new ManagedClusterPropertiesAutoScalerProfile()
                    .withScanInterval("20s")
                    .withScaleDownDelayAfterAdd("15m"))
            .withDiskEncryptionSetId(
                "/subscriptions/subid1/resourceGroups/rg1/providers/Microsoft.Compute/diskEncryptionSets/des")
            .withHttpProxyConfig(
                new ManagedClusterHttpProxyConfig()
                    .withHttpProxy("http://myproxy.server.com:8080")
                    .withHttpsProxy("https://myproxy.server.com:8080")
                    .withNoProxy(Arrays.asList("localhost", "127.0.0.1"))
                    .withTrustedCa("Q29uZ3JhdHMhIFlvdSBoYXZlIGZvdW5kIGEgaGlkZGVuIG1lc3NhZ2U="))
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersCreate_EnabledFIPS.json
     */
    /**
     * Sample code: Create Managed Cluster with FIPS enabled OS.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createManagedClusterWithFIPSEnabledOS(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .define("clustername1")
            .withRegion("location1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("archv2", "", "tier", "production"))
            .withSku(new ManagedClusterSku().withName(ManagedClusterSkuName.BASIC).withTier(ManagedClusterSkuTier.FREE))
            .withKubernetesVersion("")
            .withDnsPrefix("dnsprefix1")
            .withAgentPoolProfiles(
                Arrays
                    .asList(
                        new ManagedClusterAgentPoolProfile()
                            .withCount(3)
                            .withVmSize("Standard_DS2_v2")
                            .withOsType(OSType.LINUX)
                            .withType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                            .withMode(AgentPoolMode.SYSTEM)
                            .withEnableNodePublicIp(true)
                            .withEnableFips(true)
                            .withName("nodepool1")))
            .withLinuxProfile(
                new ContainerServiceLinuxProfile()
                    .withAdminUsername("azureuser")
                    .withSsh(
                        new ContainerServiceSshConfiguration()
                            .withPublicKeys(Arrays.asList(new ContainerServiceSshPublicKey().withKeyData("keydata")))))
            .withWindowsProfile(
                new ManagedClusterWindowsProfile()
                    .withAdminUsername("azureuser")
                    .withAdminPassword("replacePassword1234$"))
            .withServicePrincipalProfile(
                new ManagedClusterServicePrincipalProfile().withClientId("clientid").withSecret("secret"))
            .withAddonProfiles(mapOf())
            .withEnableRbac(true)
            .withEnablePodSecurityPolicy(false)
            .withNetworkProfile(
                new ContainerServiceNetworkProfile()
                    .withOutboundType(OutboundType.LOAD_BALANCER)
                    .withLoadBalancerSku(LoadBalancerSku.STANDARD)
                    .withLoadBalancerProfile(
                        new ManagedClusterLoadBalancerProfile()
                            .withManagedOutboundIPs(
                                new ManagedClusterLoadBalancerProfileManagedOutboundIPs().withCount(2))))
            .withAutoScalerProfile(
                new ManagedClusterPropertiesAutoScalerProfile()
                    .withScanInterval("20s")
                    .withScaleDownDelayAfterAdd("15m"))
            .withDiskEncryptionSetId(
                "/subscriptions/subid1/resourceGroups/rg1/providers/Microsoft.Compute/diskEncryptionSets/des")
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersCreate_SecurityProfile.json
     */
    /**
     * Sample code: Create Managed Cluster with Security Profile configured.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createManagedClusterWithSecurityProfileConfigured(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .define("clustername1")
            .withRegion("location1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("archv2", "", "tier", "production"))
            .withSku(new ManagedClusterSku().withName(ManagedClusterSkuName.BASIC).withTier(ManagedClusterSkuTier.FREE))
            .withKubernetesVersion("")
            .withDnsPrefix("dnsprefix1")
            .withAgentPoolProfiles(
                Arrays
                    .asList(
                        new ManagedClusterAgentPoolProfile()
                            .withCount(3)
                            .withVmSize("Standard_DS2_v2")
                            .withOsType(OSType.LINUX)
                            .withType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                            .withMode(AgentPoolMode.SYSTEM)
                            .withEnableNodePublicIp(true)
                            .withName("nodepool1")))
            .withLinuxProfile(
                new ContainerServiceLinuxProfile()
                    .withAdminUsername("azureuser")
                    .withSsh(
                        new ContainerServiceSshConfiguration()
                            .withPublicKeys(Arrays.asList(new ContainerServiceSshPublicKey().withKeyData("keydata")))))
            .withNetworkProfile(
                new ContainerServiceNetworkProfile()
                    .withOutboundType(OutboundType.LOAD_BALANCER)
                    .withLoadBalancerSku(LoadBalancerSku.STANDARD)
                    .withLoadBalancerProfile(
                        new ManagedClusterLoadBalancerProfile()
                            .withManagedOutboundIPs(
                                new ManagedClusterLoadBalancerProfileManagedOutboundIPs().withCount(2))))
            .withSecurityProfile(
                new ManagedClusterSecurityProfile()
                    .withAzureDefender(
                        new ManagedClusterSecurityProfileAzureDefender()
                            .withEnabled(true)
                            .withLogAnalyticsWorkspaceResourceId(
                                "/subscriptions/SUB_ID/resourcegroups/RG_NAME/providers/microsoft.operationalinsights/workspaces/WORKSPACE_NAME")))
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersCreate_DualStackNetworking.json
     */
    /**
     * Sample code: Create/Update Managed Cluster with dual-stack networking.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createUpdateManagedClusterWithDualStackNetworking(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .define("clustername1")
            .withRegion("location1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("archv2", "", "tier", "production"))
            .withSku(new ManagedClusterSku().withName(ManagedClusterSkuName.BASIC).withTier(ManagedClusterSkuTier.FREE))
            .withIdentity(
                new ManagedClusterIdentity()
                    .withType(ResourceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/subid1/resourceGroups/rgName1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identity1",
                            new ManagedServiceIdentityUserAssignedIdentitiesValue())))
            .withKubernetesVersion("")
            .withDnsPrefix("dnsprefix1")
            .withAgentPoolProfiles(
                Arrays
                    .asList(
                        new ManagedClusterAgentPoolProfile()
                            .withCount(3)
                            .withVmSize("Standard_DS1_v2")
                            .withOsType(OSType.LINUX)
                            .withScaleDownMode(ScaleDownMode.DEALLOCATE)
                            .withType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                            .withMode(AgentPoolMode.SYSTEM)
                            .withAvailabilityZones(Arrays.asList("1", "2", "3"))
                            .withEnableNodePublicIp(true)
                            .withName("nodepool1")))
            .withLinuxProfile(
                new ContainerServiceLinuxProfile()
                    .withAdminUsername("azureuser")
                    .withSsh(
                        new ContainerServiceSshConfiguration()
                            .withPublicKeys(Arrays.asList(new ContainerServiceSshPublicKey().withKeyData("keydata")))))
            .withWindowsProfile(
                new ManagedClusterWindowsProfile()
                    .withAdminUsername("azureuser")
                    .withAdminPassword("replacePassword1234$"))
            .withServicePrincipalProfile(
                new ManagedClusterServicePrincipalProfile().withClientId("clientid").withSecret("secret"))
            .withAddonProfiles(mapOf())
            .withEnableRbac(true)
            .withEnablePodSecurityPolicy(true)
            .withNetworkProfile(
                new ContainerServiceNetworkProfile()
                    .withOutboundType(OutboundType.LOAD_BALANCER)
                    .withLoadBalancerSku(LoadBalancerSku.STANDARD)
                    .withLoadBalancerProfile(
                        new ManagedClusterLoadBalancerProfile()
                            .withManagedOutboundIPs(
                                new ManagedClusterLoadBalancerProfileManagedOutboundIPs().withCount(2)))
                    .withIpFamilies(Arrays.asList(IpFamily.IPV4, IpFamily.IPV6)))
            .withAutoScalerProfile(
                new ManagedClusterPropertiesAutoScalerProfile()
                    .withBalanceSimilarNodeGroups("true")
                    .withExpander(Expander.PRIORITY)
                    .withMaxNodeProvisionTime("15m")
                    .withNewPodScaleUpDelay("1m")
                    .withScanInterval("20s")
                    .withScaleDownDelayAfterAdd("15m")
                    .withSkipNodesWithSystemPods("false"))
            .withDiskEncryptionSetId(
                "/subscriptions/subid1/resourceGroups/rg1/providers/Microsoft.Compute/diskEncryptionSets/des")
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersCreate_PPG.json
     */
    /**
     * Sample code: Create Managed Cluster with PPG.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createManagedClusterWithPPG(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .define("clustername1")
            .withRegion("location1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("archv2", "", "tier", "production"))
            .withSku(new ManagedClusterSku().withName(ManagedClusterSkuName.BASIC).withTier(ManagedClusterSkuTier.FREE))
            .withKubernetesVersion("")
            .withDnsPrefix("dnsprefix1")
            .withAgentPoolProfiles(
                Arrays
                    .asList(
                        new ManagedClusterAgentPoolProfile()
                            .withCount(3)
                            .withVmSize("Standard_DS2_v2")
                            .withOsType(OSType.LINUX)
                            .withType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                            .withMode(AgentPoolMode.SYSTEM)
                            .withEnableNodePublicIp(true)
                            .withProximityPlacementGroupId(
                                "/subscriptions/subid1/resourcegroups/rg1/providers//Microsoft.Compute/proximityPlacementGroups/ppg1")
                            .withName("nodepool1")))
            .withLinuxProfile(
                new ContainerServiceLinuxProfile()
                    .withAdminUsername("azureuser")
                    .withSsh(
                        new ContainerServiceSshConfiguration()
                            .withPublicKeys(Arrays.asList(new ContainerServiceSshPublicKey().withKeyData("keydata")))))
            .withWindowsProfile(
                new ManagedClusterWindowsProfile()
                    .withAdminUsername("azureuser")
                    .withAdminPassword("replacePassword1234$"))
            .withServicePrincipalProfile(
                new ManagedClusterServicePrincipalProfile().withClientId("clientid").withSecret("secret"))
            .withAddonProfiles(mapOf())
            .withEnableRbac(true)
            .withEnablePodSecurityPolicy(true)
            .withNetworkProfile(
                new ContainerServiceNetworkProfile()
                    .withOutboundType(OutboundType.LOAD_BALANCER)
                    .withLoadBalancerSku(LoadBalancerSku.STANDARD)
                    .withLoadBalancerProfile(
                        new ManagedClusterLoadBalancerProfile()
                            .withManagedOutboundIPs(
                                new ManagedClusterLoadBalancerProfileManagedOutboundIPs().withCount(2))))
            .withAutoScalerProfile(
                new ManagedClusterPropertiesAutoScalerProfile()
                    .withScanInterval("20s")
                    .withScaleDownDelayAfterAdd("15m"))
            .withDiskEncryptionSetId(
                "/subscriptions/subid1/resourceGroups/rg1/providers/Microsoft.Compute/diskEncryptionSets/des")
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersCreate_UpdateWithAHUB.json
     */
    /**
     * Sample code: Create/Update Managed Cluster with EnableAHUB.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createUpdateManagedClusterWithEnableAHUB(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .define("clustername1")
            .withRegion("location1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("archv2", "", "tier", "production"))
            .withSku(new ManagedClusterSku().withName(ManagedClusterSkuName.BASIC).withTier(ManagedClusterSkuTier.FREE))
            .withIdentity(
                new ManagedClusterIdentity()
                    .withType(ResourceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/subid1/resourceGroups/rgName1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identity1",
                            new ManagedServiceIdentityUserAssignedIdentitiesValue())))
            .withKubernetesVersion("")
            .withDnsPrefix("dnsprefix1")
            .withAgentPoolProfiles(
                Arrays
                    .asList(
                        new ManagedClusterAgentPoolProfile()
                            .withCount(3)
                            .withVmSize("Standard_DS1_v2")
                            .withOsType(OSType.LINUX)
                            .withType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                            .withMode(AgentPoolMode.SYSTEM)
                            .withAvailabilityZones(Arrays.asList("1", "2", "3"))
                            .withEnableNodePublicIp(true)
                            .withName("nodepool1")))
            .withLinuxProfile(
                new ContainerServiceLinuxProfile()
                    .withAdminUsername("azureuser")
                    .withSsh(
                        new ContainerServiceSshConfiguration()
                            .withPublicKeys(Arrays.asList(new ContainerServiceSshPublicKey().withKeyData("keydata")))))
            .withWindowsProfile(
                new ManagedClusterWindowsProfile()
                    .withAdminUsername("azureuser")
                    .withAdminPassword("replacePassword1234$")
                    .withLicenseType(LicenseType.WINDOWS_SERVER))
            .withServicePrincipalProfile(
                new ManagedClusterServicePrincipalProfile().withClientId("clientid").withSecret("secret"))
            .withAddonProfiles(mapOf())
            .withEnableRbac(true)
            .withEnablePodSecurityPolicy(true)
            .withNetworkProfile(
                new ContainerServiceNetworkProfile()
                    .withOutboundType(OutboundType.LOAD_BALANCER)
                    .withLoadBalancerSku(LoadBalancerSku.STANDARD)
                    .withLoadBalancerProfile(
                        new ManagedClusterLoadBalancerProfile()
                            .withManagedOutboundIPs(
                                new ManagedClusterLoadBalancerProfileManagedOutboundIPs().withCount(2))))
            .withAutoScalerProfile(
                new ManagedClusterPropertiesAutoScalerProfile()
                    .withScanInterval("20s")
                    .withScaleDownDelayAfterAdd("15m"))
            .withDiskEncryptionSetId(
                "/subscriptions/subid1/resourceGroups/rg1/providers/Microsoft.Compute/diskEncryptionSets/des")
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersCreate_PodIdentity.json
     */
    /**
     * Sample code: Create Managed Cluster with PodIdentity enabled.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createManagedClusterWithPodIdentityEnabled(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .define("clustername1")
            .withRegion("location1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("archv2", "", "tier", "production"))
            .withSku(new ManagedClusterSku().withName(ManagedClusterSkuName.BASIC).withTier(ManagedClusterSkuTier.FREE))
            .withKubernetesVersion("")
            .withDnsPrefix("dnsprefix1")
            .withAgentPoolProfiles(
                Arrays
                    .asList(
                        new ManagedClusterAgentPoolProfile()
                            .withCount(3)
                            .withVmSize("Standard_DS2_v2")
                            .withOsType(OSType.LINUX)
                            .withType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                            .withMode(AgentPoolMode.SYSTEM)
                            .withEnableNodePublicIp(true)
                            .withName("nodepool1")))
            .withLinuxProfile(
                new ContainerServiceLinuxProfile()
                    .withAdminUsername("azureuser")
                    .withSsh(
                        new ContainerServiceSshConfiguration()
                            .withPublicKeys(Arrays.asList(new ContainerServiceSshPublicKey().withKeyData("keydata")))))
            .withWindowsProfile(
                new ManagedClusterWindowsProfile()
                    .withAdminUsername("azureuser")
                    .withAdminPassword("replacePassword1234$"))
            .withServicePrincipalProfile(
                new ManagedClusterServicePrincipalProfile().withClientId("clientid").withSecret("secret"))
            .withAddonProfiles(mapOf())
            .withPodIdentityProfile(
                new ManagedClusterPodIdentityProfile().withEnabled(true).withAllowNetworkPluginKubenet(true))
            .withEnableRbac(true)
            .withEnablePodSecurityPolicy(true)
            .withNetworkProfile(
                new ContainerServiceNetworkProfile()
                    .withOutboundType(OutboundType.LOAD_BALANCER)
                    .withLoadBalancerSku(LoadBalancerSku.STANDARD)
                    .withLoadBalancerProfile(
                        new ManagedClusterLoadBalancerProfile()
                            .withManagedOutboundIPs(
                                new ManagedClusterLoadBalancerProfileManagedOutboundIPs().withCount(2))))
            .withAutoScalerProfile(
                new ManagedClusterPropertiesAutoScalerProfile()
                    .withScanInterval("20s")
                    .withScaleDownDelayAfterAdd("15m"))
            .withDiskEncryptionSetId(
                "/subscriptions/subid1/resourceGroups/rg1/providers/Microsoft.Compute/diskEncryptionSets/des")
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersCreate_DisableRunCommand.json
     */
    /**
     * Sample code: Create Managed Cluster with RunCommand disabled.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createManagedClusterWithRunCommandDisabled(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .define("clustername1")
            .withRegion("location1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("archv2", "", "tier", "production"))
            .withSku(new ManagedClusterSku().withName(ManagedClusterSkuName.BASIC).withTier(ManagedClusterSkuTier.FREE))
            .withKubernetesVersion("")
            .withDnsPrefix("dnsprefix1")
            .withAgentPoolProfiles(
                Arrays
                    .asList(
                        new ManagedClusterAgentPoolProfile()
                            .withCount(3)
                            .withVmSize("Standard_DS2_v2")
                            .withOsType(OSType.LINUX)
                            .withType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                            .withMode(AgentPoolMode.SYSTEM)
                            .withEnableNodePublicIp(true)
                            .withEnableEncryptionAtHost(true)
                            .withName("nodepool1")))
            .withLinuxProfile(
                new ContainerServiceLinuxProfile()
                    .withAdminUsername("azureuser")
                    .withSsh(
                        new ContainerServiceSshConfiguration()
                            .withPublicKeys(Arrays.asList(new ContainerServiceSshPublicKey().withKeyData("keydata")))))
            .withWindowsProfile(
                new ManagedClusterWindowsProfile()
                    .withAdminUsername("azureuser")
                    .withAdminPassword("replacePassword1234$"))
            .withServicePrincipalProfile(
                new ManagedClusterServicePrincipalProfile().withClientId("clientid").withSecret("secret"))
            .withAddonProfiles(mapOf())
            .withEnableRbac(true)
            .withEnablePodSecurityPolicy(true)
            .withNetworkProfile(
                new ContainerServiceNetworkProfile()
                    .withOutboundType(OutboundType.LOAD_BALANCER)
                    .withLoadBalancerSku(LoadBalancerSku.STANDARD)
                    .withLoadBalancerProfile(
                        new ManagedClusterLoadBalancerProfile()
                            .withManagedOutboundIPs(
                                new ManagedClusterLoadBalancerProfileManagedOutboundIPs().withCount(2))))
            .withAutoScalerProfile(
                new ManagedClusterPropertiesAutoScalerProfile()
                    .withScanInterval("20s")
                    .withScaleDownDelayAfterAdd("15m"))
            .withApiServerAccessProfile(new ManagedClusterApiServerAccessProfile().withDisableRunCommand(true))
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersCreate_UserAssignedNATGateway.json
     */
    /**
     * Sample code: Create Managed Cluster with user-assigned NAT gateway as outbound type.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createManagedClusterWithUserAssignedNATGatewayAsOutboundType(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .define("clustername1")
            .withRegion("location1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("archv2", "", "tier", "production"))
            .withSku(new ManagedClusterSku().withName(ManagedClusterSkuName.BASIC).withTier(ManagedClusterSkuTier.FREE))
            .withKubernetesVersion("")
            .withDnsPrefix("dnsprefix1")
            .withAgentPoolProfiles(
                Arrays
                    .asList(
                        new ManagedClusterAgentPoolProfile()
                            .withCount(3)
                            .withVmSize("Standard_DS2_v2")
                            .withOsType(OSType.LINUX)
                            .withType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                            .withMode(AgentPoolMode.SYSTEM)
                            .withEnableNodePublicIp(false)
                            .withName("nodepool1")))
            .withLinuxProfile(
                new ContainerServiceLinuxProfile()
                    .withAdminUsername("azureuser")
                    .withSsh(
                        new ContainerServiceSshConfiguration()
                            .withPublicKeys(Arrays.asList(new ContainerServiceSshPublicKey().withKeyData("keydata")))))
            .withWindowsProfile(
                new ManagedClusterWindowsProfile()
                    .withAdminUsername("azureuser")
                    .withAdminPassword("replacePassword1234$"))
            .withServicePrincipalProfile(
                new ManagedClusterServicePrincipalProfile().withClientId("clientid").withSecret("secret"))
            .withAddonProfiles(mapOf())
            .withEnableRbac(true)
            .withEnablePodSecurityPolicy(true)
            .withNetworkProfile(
                new ContainerServiceNetworkProfile()
                    .withOutboundType(OutboundType.USER_ASSIGNED_NATGATEWAY)
                    .withLoadBalancerSku(LoadBalancerSku.STANDARD))
            .withAutoScalerProfile(
                new ManagedClusterPropertiesAutoScalerProfile()
                    .withScanInterval("20s")
                    .withScaleDownDelayAfterAdd("15m"))
            .withDiskEncryptionSetId(
                "/subscriptions/subid1/resourceGroups/rg1/providers/Microsoft.Compute/diskEncryptionSets/des")
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersCreate_Snapshot.json
     */
    /**
     * Sample code: Create Managed Cluster using an agent pool snapshot.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createManagedClusterUsingAnAgentPoolSnapshot(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .define("clustername1")
            .withRegion("location1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("archv2", "", "tier", "production"))
            .withSku(new ManagedClusterSku().withName(ManagedClusterSkuName.BASIC).withTier(ManagedClusterSkuTier.FREE))
            .withKubernetesVersion("")
            .withDnsPrefix("dnsprefix1")
            .withAgentPoolProfiles(
                Arrays
                    .asList(
                        new ManagedClusterAgentPoolProfile()
                            .withCount(3)
                            .withVmSize("Standard_DS2_v2")
                            .withOsType(OSType.LINUX)
                            .withType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                            .withMode(AgentPoolMode.SYSTEM)
                            .withEnableNodePublicIp(true)
                            .withEnableFips(true)
                            .withCreationData(
                                new CreationData()
                                    .withSourceResourceId(
                                        "/subscriptions/subid1/resourceGroups/rg1/providers/Microsoft.ContainerService/snapshots/snapshot1"))
                            .withName("nodepool1")))
            .withLinuxProfile(
                new ContainerServiceLinuxProfile()
                    .withAdminUsername("azureuser")
                    .withSsh(
                        new ContainerServiceSshConfiguration()
                            .withPublicKeys(Arrays.asList(new ContainerServiceSshPublicKey().withKeyData("keydata")))))
            .withWindowsProfile(
                new ManagedClusterWindowsProfile()
                    .withAdminUsername("azureuser")
                    .withAdminPassword("replacePassword1234$"))
            .withServicePrincipalProfile(
                new ManagedClusterServicePrincipalProfile().withClientId("clientid").withSecret("secret"))
            .withAddonProfiles(mapOf())
            .withEnableRbac(true)
            .withEnablePodSecurityPolicy(false)
            .withNetworkProfile(
                new ContainerServiceNetworkProfile()
                    .withOutboundType(OutboundType.LOAD_BALANCER)
                    .withLoadBalancerSku(LoadBalancerSku.STANDARD)
                    .withLoadBalancerProfile(
                        new ManagedClusterLoadBalancerProfile()
                            .withManagedOutboundIPs(
                                new ManagedClusterLoadBalancerProfileManagedOutboundIPs().withCount(2))))
            .withAutoScalerProfile(
                new ManagedClusterPropertiesAutoScalerProfile()
                    .withScanInterval("20s")
                    .withScaleDownDelayAfterAdd("15m"))
            .withDiskEncryptionSetId(
                "/subscriptions/subid1/resourceGroups/rg1/providers/Microsoft.Compute/diskEncryptionSets/des")
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersCreate_OSSKU.json
     */
    /**
     * Sample code: Create Managed Cluster with OSSKU.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createManagedClusterWithOSSKU(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .define("clustername1")
            .withRegion("location1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("archv2", "", "tier", "production"))
            .withSku(new ManagedClusterSku().withName(ManagedClusterSkuName.BASIC).withTier(ManagedClusterSkuTier.FREE))
            .withKubernetesVersion("")
            .withDnsPrefix("dnsprefix1")
            .withAgentPoolProfiles(
                Arrays
                    .asList(
                        new ManagedClusterAgentPoolProfile()
                            .withCount(3)
                            .withVmSize("Standard_DS2_v2")
                            .withOsType(OSType.LINUX)
                            .withOsSku(Ossku.CBLMARINER)
                            .withType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                            .withMode(AgentPoolMode.SYSTEM)
                            .withEnableNodePublicIp(true)
                            .withName("nodepool1")))
            .withLinuxProfile(
                new ContainerServiceLinuxProfile()
                    .withAdminUsername("azureuser")
                    .withSsh(
                        new ContainerServiceSshConfiguration()
                            .withPublicKeys(Arrays.asList(new ContainerServiceSshPublicKey().withKeyData("keydata")))))
            .withWindowsProfile(
                new ManagedClusterWindowsProfile()
                    .withAdminUsername("azureuser")
                    .withAdminPassword("replacePassword1234$"))
            .withServicePrincipalProfile(
                new ManagedClusterServicePrincipalProfile().withClientId("clientid").withSecret("secret"))
            .withAddonProfiles(mapOf())
            .withEnableRbac(true)
            .withEnablePodSecurityPolicy(true)
            .withNetworkProfile(
                new ContainerServiceNetworkProfile()
                    .withOutboundType(OutboundType.LOAD_BALANCER)
                    .withLoadBalancerSku(LoadBalancerSku.STANDARD)
                    .withLoadBalancerProfile(
                        new ManagedClusterLoadBalancerProfile()
                            .withManagedOutboundIPs(
                                new ManagedClusterLoadBalancerProfileManagedOutboundIPs().withCount(2))))
            .withAutoScalerProfile(
                new ManagedClusterPropertiesAutoScalerProfile()
                    .withScanInterval("20s")
                    .withScaleDownDelayAfterAdd("15m"))
            .withDiskEncryptionSetId(
                "/subscriptions/subid1/resourceGroups/rg1/providers/Microsoft.Compute/diskEncryptionSets/des")
            .withHttpProxyConfig(
                new ManagedClusterHttpProxyConfig()
                    .withHttpProxy("http://myproxy.server.com:8080")
                    .withHttpsProxy("https://myproxy.server.com:8080")
                    .withNoProxy(Arrays.asList("localhost", "127.0.0.1"))
                    .withTrustedCa("Q29uZ3JhdHMhIFlvdSBoYXZlIGZvdW5kIGEgaGlkZGVuIG1lc3NhZ2U="))
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersCreate_Update.json
     */
    /**
     * Sample code: Create/Update Managed Cluster.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createUpdateManagedCluster(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .define("clustername1")
            .withRegion("location1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("archv2", "", "tier", "production"))
            .withSku(new ManagedClusterSku().withName(ManagedClusterSkuName.BASIC).withTier(ManagedClusterSkuTier.FREE))
            .withIdentity(
                new ManagedClusterIdentity()
                    .withType(ResourceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/subid1/resourceGroups/rgName1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identity1",
                            new ManagedServiceIdentityUserAssignedIdentitiesValue())))
            .withKubernetesVersion("")
            .withDnsPrefix("dnsprefix1")
            .withAgentPoolProfiles(
                Arrays
                    .asList(
                        new ManagedClusterAgentPoolProfile()
                            .withCount(3)
                            .withVmSize("Standard_DS1_v2")
                            .withOsType(OSType.LINUX)
                            .withScaleDownMode(ScaleDownMode.DEALLOCATE)
                            .withType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                            .withMode(AgentPoolMode.SYSTEM)
                            .withAvailabilityZones(Arrays.asList("1", "2", "3"))
                            .withEnableNodePublicIp(true)
                            .withName("nodepool1")))
            .withLinuxProfile(
                new ContainerServiceLinuxProfile()
                    .withAdminUsername("azureuser")
                    .withSsh(
                        new ContainerServiceSshConfiguration()
                            .withPublicKeys(Arrays.asList(new ContainerServiceSshPublicKey().withKeyData("keydata")))))
            .withWindowsProfile(
                new ManagedClusterWindowsProfile()
                    .withAdminUsername("azureuser")
                    .withAdminPassword("replacePassword1234$"))
            .withServicePrincipalProfile(
                new ManagedClusterServicePrincipalProfile().withClientId("clientid").withSecret("secret"))
            .withAddonProfiles(mapOf())
            .withEnableRbac(true)
            .withEnablePodSecurityPolicy(true)
            .withNetworkProfile(
                new ContainerServiceNetworkProfile()
                    .withOutboundType(OutboundType.LOAD_BALANCER)
                    .withLoadBalancerSku(LoadBalancerSku.STANDARD)
                    .withLoadBalancerProfile(
                        new ManagedClusterLoadBalancerProfile()
                            .withManagedOutboundIPs(
                                new ManagedClusterLoadBalancerProfileManagedOutboundIPs().withCount(2))))
            .withAutoScalerProfile(
                new ManagedClusterPropertiesAutoScalerProfile()
                    .withBalanceSimilarNodeGroups("true")
                    .withExpander(Expander.PRIORITY)
                    .withMaxNodeProvisionTime("15m")
                    .withNewPodScaleUpDelay("1m")
                    .withScanInterval("20s")
                    .withScaleDownDelayAfterAdd("15m")
                    .withSkipNodesWithSystemPods("false"))
            .withDiskEncryptionSetId(
                "/subscriptions/subid1/resourceGroups/rg1/providers/Microsoft.Compute/diskEncryptionSets/des")
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersCreate_PrivateClusterFQDNSubdomain.json
     */
    /**
     * Sample code: Create Managed Private Cluster with fqdn subdomain specified.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createManagedPrivateClusterWithFqdnSubdomainSpecified(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .define("clustername1")
            .withRegion("location1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("archv2", "", "tier", "production"))
            .withSku(new ManagedClusterSku().withName(ManagedClusterSkuName.BASIC).withTier(ManagedClusterSkuTier.FREE))
            .withKubernetesVersion("")
            .withFqdnSubdomain("domain1")
            .withAgentPoolProfiles(
                Arrays
                    .asList(
                        new ManagedClusterAgentPoolProfile()
                            .withCount(3)
                            .withVmSize("Standard_DS2_v2")
                            .withOsType(OSType.LINUX)
                            .withType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                            .withMode(AgentPoolMode.SYSTEM)
                            .withEnableNodePublicIp(true)
                            .withEnableEncryptionAtHost(true)
                            .withName("nodepool1")))
            .withLinuxProfile(
                new ContainerServiceLinuxProfile()
                    .withAdminUsername("azureuser")
                    .withSsh(
                        new ContainerServiceSshConfiguration()
                            .withPublicKeys(Arrays.asList(new ContainerServiceSshPublicKey().withKeyData("keydata")))))
            .withWindowsProfile(
                new ManagedClusterWindowsProfile()
                    .withAdminUsername("azureuser")
                    .withAdminPassword("replacePassword1234$"))
            .withServicePrincipalProfile(
                new ManagedClusterServicePrincipalProfile().withClientId("clientid").withSecret("secret"))
            .withAddonProfiles(mapOf())
            .withEnableRbac(true)
            .withEnablePodSecurityPolicy(true)
            .withNetworkProfile(
                new ContainerServiceNetworkProfile()
                    .withOutboundType(OutboundType.LOAD_BALANCER)
                    .withLoadBalancerSku(LoadBalancerSku.STANDARD)
                    .withLoadBalancerProfile(
                        new ManagedClusterLoadBalancerProfile()
                            .withManagedOutboundIPs(
                                new ManagedClusterLoadBalancerProfileManagedOutboundIPs().withCount(2))))
            .withAutoScalerProfile(
                new ManagedClusterPropertiesAutoScalerProfile()
                    .withScanInterval("20s")
                    .withScaleDownDelayAfterAdd("15m"))
            .withApiServerAccessProfile(
                new ManagedClusterApiServerAccessProfile()
                    .withEnablePrivateCluster(true)
                    .withPrivateDnsZone(
                        "/subscriptions/subid1/resourcegroups/rg1/providers/Microsoft.Network/privateDnsZones/privatelink.location1.azmk8s.io"))
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersCreate_ManagedNATGateway.json
     */
    /**
     * Sample code: Create Managed Cluster with AKS-managed NAT gateway as outbound type.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createManagedClusterWithAKSManagedNATGatewayAsOutboundType(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .define("clustername1")
            .withRegion("location1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("archv2", "", "tier", "production"))
            .withSku(new ManagedClusterSku().withName(ManagedClusterSkuName.BASIC).withTier(ManagedClusterSkuTier.FREE))
            .withKubernetesVersion("")
            .withDnsPrefix("dnsprefix1")
            .withAgentPoolProfiles(
                Arrays
                    .asList(
                        new ManagedClusterAgentPoolProfile()
                            .withCount(3)
                            .withVmSize("Standard_DS2_v2")
                            .withOsType(OSType.LINUX)
                            .withType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                            .withMode(AgentPoolMode.SYSTEM)
                            .withEnableNodePublicIp(false)
                            .withName("nodepool1")))
            .withLinuxProfile(
                new ContainerServiceLinuxProfile()
                    .withAdminUsername("azureuser")
                    .withSsh(
                        new ContainerServiceSshConfiguration()
                            .withPublicKeys(Arrays.asList(new ContainerServiceSshPublicKey().withKeyData("keydata")))))
            .withWindowsProfile(
                new ManagedClusterWindowsProfile()
                    .withAdminUsername("azureuser")
                    .withAdminPassword("replacePassword1234$"))
            .withServicePrincipalProfile(
                new ManagedClusterServicePrincipalProfile().withClientId("clientid").withSecret("secret"))
            .withAddonProfiles(mapOf())
            .withEnableRbac(true)
            .withEnablePodSecurityPolicy(true)
            .withNetworkProfile(
                new ContainerServiceNetworkProfile()
                    .withOutboundType(OutboundType.MANAGED_NATGATEWAY)
                    .withLoadBalancerSku(LoadBalancerSku.STANDARD)
                    .withNatGatewayProfile(
                        new ManagedClusterNatGatewayProfile()
                            .withManagedOutboundIpProfile(new ManagedClusterManagedOutboundIpProfile().withCount(2))))
            .withAutoScalerProfile(
                new ManagedClusterPropertiesAutoScalerProfile()
                    .withScanInterval("20s")
                    .withScaleDownDelayAfterAdd("15m"))
            .withDiskEncryptionSetId(
                "/subscriptions/subid1/resourceGroups/rg1/providers/Microsoft.Compute/diskEncryptionSets/des")
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersCreate_UpdateWithEnableAzureRBAC.json
     */
    /**
     * Sample code: Create/Update AAD Managed Cluster with EnableAzureRBAC.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createUpdateAADManagedClusterWithEnableAzureRBAC(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .define("clustername1")
            .withRegion("location1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("archv2", "", "tier", "production"))
            .withSku(new ManagedClusterSku().withName(ManagedClusterSkuName.BASIC).withTier(ManagedClusterSkuTier.FREE))
            .withKubernetesVersion("")
            .withDnsPrefix("dnsprefix1")
            .withAgentPoolProfiles(
                Arrays
                    .asList(
                        new ManagedClusterAgentPoolProfile()
                            .withCount(3)
                            .withVmSize("Standard_DS1_v2")
                            .withOsType(OSType.LINUX)
                            .withType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                            .withMode(AgentPoolMode.SYSTEM)
                            .withAvailabilityZones(Arrays.asList("1", "2", "3"))
                            .withEnableNodePublicIp(true)
                            .withName("nodepool1")))
            .withLinuxProfile(
                new ContainerServiceLinuxProfile()
                    .withAdminUsername("azureuser")
                    .withSsh(
                        new ContainerServiceSshConfiguration()
                            .withPublicKeys(Arrays.asList(new ContainerServiceSshPublicKey().withKeyData("keydata")))))
            .withWindowsProfile(
                new ManagedClusterWindowsProfile()
                    .withAdminUsername("azureuser")
                    .withAdminPassword("replacePassword1234$"))
            .withServicePrincipalProfile(
                new ManagedClusterServicePrincipalProfile().withClientId("clientid").withSecret("secret"))
            .withAddonProfiles(mapOf())
            .withEnableRbac(true)
            .withEnablePodSecurityPolicy(true)
            .withNetworkProfile(
                new ContainerServiceNetworkProfile()
                    .withOutboundType(OutboundType.LOAD_BALANCER)
                    .withLoadBalancerSku(LoadBalancerSku.STANDARD)
                    .withLoadBalancerProfile(
                        new ManagedClusterLoadBalancerProfile()
                            .withManagedOutboundIPs(
                                new ManagedClusterLoadBalancerProfileManagedOutboundIPs().withCount(2))))
            .withAadProfile(new ManagedClusterAadProfile().withManaged(true).withEnableAzureRbac(true))
            .withAutoScalerProfile(
                new ManagedClusterPropertiesAutoScalerProfile()
                    .withScanInterval("20s")
                    .withScaleDownDelayAfterAdd("15m"))
            .withDiskEncryptionSetId(
                "/subscriptions/subid1/resourceGroups/rg1/providers/Microsoft.Compute/diskEncryptionSets/des")
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersCreate_EnableUltraSSD.json
     */
    /**
     * Sample code: Create Managed Cluster with UltraSSD enabled.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createManagedClusterWithUltraSSDEnabled(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .define("clustername1")
            .withRegion("location1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("archv2", "", "tier", "production"))
            .withSku(new ManagedClusterSku().withName(ManagedClusterSkuName.BASIC).withTier(ManagedClusterSkuTier.FREE))
            .withKubernetesVersion("")
            .withDnsPrefix("dnsprefix1")
            .withAgentPoolProfiles(
                Arrays
                    .asList(
                        new ManagedClusterAgentPoolProfile()
                            .withCount(3)
                            .withVmSize("Standard_DS2_v2")
                            .withOsType(OSType.LINUX)
                            .withType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                            .withMode(AgentPoolMode.SYSTEM)
                            .withEnableNodePublicIp(true)
                            .withEnableUltraSsd(true)
                            .withName("nodepool1")))
            .withLinuxProfile(
                new ContainerServiceLinuxProfile()
                    .withAdminUsername("azureuser")
                    .withSsh(
                        new ContainerServiceSshConfiguration()
                            .withPublicKeys(Arrays.asList(new ContainerServiceSshPublicKey().withKeyData("keydata")))))
            .withWindowsProfile(
                new ManagedClusterWindowsProfile()
                    .withAdminUsername("azureuser")
                    .withAdminPassword("replacePassword1234$"))
            .withServicePrincipalProfile(
                new ManagedClusterServicePrincipalProfile().withClientId("clientid").withSecret("secret"))
            .withAddonProfiles(mapOf())
            .withEnableRbac(true)
            .withEnablePodSecurityPolicy(true)
            .withNetworkProfile(
                new ContainerServiceNetworkProfile()
                    .withOutboundType(OutboundType.LOAD_BALANCER)
                    .withLoadBalancerSku(LoadBalancerSku.STANDARD)
                    .withLoadBalancerProfile(
                        new ManagedClusterLoadBalancerProfile()
                            .withManagedOutboundIPs(
                                new ManagedClusterLoadBalancerProfileManagedOutboundIPs().withCount(2))))
            .withAutoScalerProfile(
                new ManagedClusterPropertiesAutoScalerProfile()
                    .withScanInterval("20s")
                    .withScaleDownDelayAfterAdd("15m"))
            .withDiskEncryptionSetId(
                "/subscriptions/subid1/resourceGroups/rg1/providers/Microsoft.Compute/diskEncryptionSets/des")
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersCreate_UpdateWindowsGmsa.json
     */
    /**
     * Sample code: Create/Update Managed Cluster with Windows gMSA enabled.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createUpdateManagedClusterWithWindowsGMSAEnabled(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .define("clustername1")
            .withRegion("location1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("archv2", "", "tier", "production"))
            .withSku(new ManagedClusterSku().withName(ManagedClusterSkuName.BASIC).withTier(ManagedClusterSkuTier.FREE))
            .withIdentity(
                new ManagedClusterIdentity()
                    .withType(ResourceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/subid1/resourceGroups/rgName1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identity1",
                            new ManagedServiceIdentityUserAssignedIdentitiesValue())))
            .withKubernetesVersion("")
            .withDnsPrefix("dnsprefix1")
            .withAgentPoolProfiles(
                Arrays
                    .asList(
                        new ManagedClusterAgentPoolProfile()
                            .withCount(3)
                            .withVmSize("Standard_DS1_v2")
                            .withOsType(OSType.LINUX)
                            .withType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                            .withMode(AgentPoolMode.SYSTEM)
                            .withAvailabilityZones(Arrays.asList("1", "2", "3"))
                            .withEnableNodePublicIp(true)
                            .withName("nodepool1")))
            .withLinuxProfile(
                new ContainerServiceLinuxProfile()
                    .withAdminUsername("azureuser")
                    .withSsh(
                        new ContainerServiceSshConfiguration()
                            .withPublicKeys(Arrays.asList(new ContainerServiceSshPublicKey().withKeyData("keydata")))))
            .withWindowsProfile(
                new ManagedClusterWindowsProfile()
                    .withAdminUsername("azureuser")
                    .withAdminPassword("replacePassword1234$")
                    .withGmsaProfile(new WindowsGmsaProfile().withEnabled(true)))
            .withServicePrincipalProfile(
                new ManagedClusterServicePrincipalProfile().withClientId("clientid").withSecret("secret"))
            .withAddonProfiles(mapOf())
            .withEnableRbac(true)
            .withEnablePodSecurityPolicy(true)
            .withNetworkProfile(
                new ContainerServiceNetworkProfile()
                    .withOutboundType(OutboundType.LOAD_BALANCER)
                    .withLoadBalancerSku(LoadBalancerSku.STANDARD)
                    .withLoadBalancerProfile(
                        new ManagedClusterLoadBalancerProfile()
                            .withManagedOutboundIPs(
                                new ManagedClusterLoadBalancerProfileManagedOutboundIPs().withCount(2))))
            .withAutoScalerProfile(
                new ManagedClusterPropertiesAutoScalerProfile()
                    .withScanInterval("20s")
                    .withScaleDownDelayAfterAdd("15m"))
            .withDiskEncryptionSetId(
                "/subscriptions/subid1/resourceGroups/rg1/providers/Microsoft.Compute/diskEncryptionSets/des")
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersCreate_GPUMIG.json
     */
    /**
     * Sample code: Create Managed Cluster with GPUMIG.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createManagedClusterWithGPUMIG(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .define("clustername1")
            .withRegion("location1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("archv2", "", "tier", "production"))
            .withSku(new ManagedClusterSku().withName(ManagedClusterSkuName.BASIC).withTier(ManagedClusterSkuTier.FREE))
            .withKubernetesVersion("")
            .withDnsPrefix("dnsprefix1")
            .withAgentPoolProfiles(
                Arrays
                    .asList(
                        new ManagedClusterAgentPoolProfile()
                            .withCount(3)
                            .withVmSize("Standard_ND96asr_v4")
                            .withOsType(OSType.LINUX)
                            .withType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                            .withMode(AgentPoolMode.SYSTEM)
                            .withEnableNodePublicIp(true)
                            .withGpuInstanceProfile(GpuInstanceProfile.MIG3G)
                            .withName("nodepool1")))
            .withLinuxProfile(
                new ContainerServiceLinuxProfile()
                    .withAdminUsername("azureuser")
                    .withSsh(
                        new ContainerServiceSshConfiguration()
                            .withPublicKeys(Arrays.asList(new ContainerServiceSshPublicKey().withKeyData("keydata")))))
            .withWindowsProfile(
                new ManagedClusterWindowsProfile()
                    .withAdminUsername("azureuser")
                    .withAdminPassword("replacePassword1234$"))
            .withServicePrincipalProfile(
                new ManagedClusterServicePrincipalProfile().withClientId("clientid").withSecret("secret"))
            .withAddonProfiles(mapOf())
            .withEnableRbac(true)
            .withEnablePodSecurityPolicy(true)
            .withNetworkProfile(
                new ContainerServiceNetworkProfile()
                    .withOutboundType(OutboundType.LOAD_BALANCER)
                    .withLoadBalancerSku(LoadBalancerSku.STANDARD)
                    .withLoadBalancerProfile(
                        new ManagedClusterLoadBalancerProfile()
                            .withManagedOutboundIPs(
                                new ManagedClusterLoadBalancerProfileManagedOutboundIPs().withCount(2))))
            .withAutoScalerProfile(
                new ManagedClusterPropertiesAutoScalerProfile()
                    .withScanInterval("20s")
                    .withScaleDownDelayAfterAdd("15m"))
            .withDiskEncryptionSetId(
                "/subscriptions/subid1/resourceGroups/rg1/providers/Microsoft.Compute/diskEncryptionSets/des")
            .withHttpProxyConfig(
                new ManagedClusterHttpProxyConfig()
                    .withHttpProxy("http://myproxy.server.com:8080")
                    .withHttpsProxy("https://myproxy.server.com:8080")
                    .withNoProxy(Arrays.asList("localhost", "127.0.0.1"))
                    .withTrustedCa("Q29uZ3JhdHMhIFlvdSBoYXZlIGZvdW5kIGEgaGlkZGVuIG1lc3NhZ2U="))
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersCreate_NodePublicIPPrefix.json
     */
    /**
     * Sample code: Create Managed Cluster with Node Public IP Prefix.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createManagedClusterWithNodePublicIPPrefix(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .define("clustername1")
            .withRegion("location1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("archv2", "", "tier", "production"))
            .withSku(new ManagedClusterSku().withName(ManagedClusterSkuName.BASIC).withTier(ManagedClusterSkuTier.FREE))
            .withKubernetesVersion("")
            .withDnsPrefix("dnsprefix1")
            .withAgentPoolProfiles(
                Arrays
                    .asList(
                        new ManagedClusterAgentPoolProfile()
                            .withCount(3)
                            .withVmSize("Standard_DS2_v2")
                            .withOsType(OSType.LINUX)
                            .withType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                            .withMode(AgentPoolMode.SYSTEM)
                            .withEnableNodePublicIp(true)
                            .withNodePublicIpPrefixId(
                                "/subscriptions/subid1/resourcegroups/rg1/providers/Microsoft.Network/publicIPPrefixes/public-ip-prefix")
                            .withName("nodepool1")))
            .withLinuxProfile(
                new ContainerServiceLinuxProfile()
                    .withAdminUsername("azureuser")
                    .withSsh(
                        new ContainerServiceSshConfiguration()
                            .withPublicKeys(Arrays.asList(new ContainerServiceSshPublicKey().withKeyData("keydata")))))
            .withWindowsProfile(
                new ManagedClusterWindowsProfile()
                    .withAdminUsername("azureuser")
                    .withAdminPassword("replacePassword1234$"))
            .withServicePrincipalProfile(
                new ManagedClusterServicePrincipalProfile().withClientId("clientid").withSecret("secret"))
            .withAddonProfiles(mapOf())
            .withEnableRbac(true)
            .withEnablePodSecurityPolicy(true)
            .withNetworkProfile(
                new ContainerServiceNetworkProfile()
                    .withOutboundType(OutboundType.LOAD_BALANCER)
                    .withLoadBalancerSku(LoadBalancerSku.STANDARD)
                    .withLoadBalancerProfile(
                        new ManagedClusterLoadBalancerProfile()
                            .withManagedOutboundIPs(
                                new ManagedClusterLoadBalancerProfileManagedOutboundIPs().withCount(2))))
            .withAutoScalerProfile(
                new ManagedClusterPropertiesAutoScalerProfile()
                    .withScanInterval("20s")
                    .withScaleDownDelayAfterAdd("15m"))
            .withDiskEncryptionSetId(
                "/subscriptions/subid1/resourceGroups/rg1/providers/Microsoft.Compute/diskEncryptionSets/des")
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersCreate_EnableEncryptionAtHost.json
     */
    /**
     * Sample code: Create Managed Cluster with EncryptionAtHost enabled.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createManagedClusterWithEncryptionAtHostEnabled(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .define("clustername1")
            .withRegion("location1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("archv2", "", "tier", "production"))
            .withSku(new ManagedClusterSku().withName(ManagedClusterSkuName.BASIC).withTier(ManagedClusterSkuTier.FREE))
            .withKubernetesVersion("")
            .withDnsPrefix("dnsprefix1")
            .withAgentPoolProfiles(
                Arrays
                    .asList(
                        new ManagedClusterAgentPoolProfile()
                            .withCount(3)
                            .withVmSize("Standard_DS2_v2")
                            .withOsType(OSType.LINUX)
                            .withType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                            .withMode(AgentPoolMode.SYSTEM)
                            .withEnableNodePublicIp(true)
                            .withEnableEncryptionAtHost(true)
                            .withName("nodepool1")))
            .withLinuxProfile(
                new ContainerServiceLinuxProfile()
                    .withAdminUsername("azureuser")
                    .withSsh(
                        new ContainerServiceSshConfiguration()
                            .withPublicKeys(Arrays.asList(new ContainerServiceSshPublicKey().withKeyData("keydata")))))
            .withWindowsProfile(
                new ManagedClusterWindowsProfile()
                    .withAdminUsername("azureuser")
                    .withAdminPassword("replacePassword1234$"))
            .withServicePrincipalProfile(
                new ManagedClusterServicePrincipalProfile().withClientId("clientid").withSecret("secret"))
            .withAddonProfiles(mapOf())
            .withEnableRbac(true)
            .withEnablePodSecurityPolicy(true)
            .withNetworkProfile(
                new ContainerServiceNetworkProfile()
                    .withOutboundType(OutboundType.LOAD_BALANCER)
                    .withLoadBalancerSku(LoadBalancerSku.STANDARD)
                    .withLoadBalancerProfile(
                        new ManagedClusterLoadBalancerProfile()
                            .withManagedOutboundIPs(
                                new ManagedClusterLoadBalancerProfileManagedOutboundIPs().withCount(2))))
            .withAutoScalerProfile(
                new ManagedClusterPropertiesAutoScalerProfile()
                    .withScanInterval("20s")
                    .withScaleDownDelayAfterAdd("15m"))
            .withDiskEncryptionSetId(
                "/subscriptions/subid1/resourceGroups/rg1/providers/Microsoft.Compute/diskEncryptionSets/des")
            .create();
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersCreate_PrivateClusterPublicFQDN.json
     */
    /**
     * Sample code: Create Managed Private Cluster with Public FQDN specified.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createManagedPrivateClusterWithPublicFQDNSpecified(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .define("clustername1")
            .withRegion("location1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("archv2", "", "tier", "production"))
            .withSku(new ManagedClusterSku().withName(ManagedClusterSkuName.BASIC).withTier(ManagedClusterSkuTier.FREE))
            .withKubernetesVersion("")
            .withDnsPrefix("dnsprefix1")
            .withAgentPoolProfiles(
                Arrays
                    .asList(
                        new ManagedClusterAgentPoolProfile()
                            .withCount(3)
                            .withVmSize("Standard_DS2_v2")
                            .withOsType(OSType.LINUX)
                            .withType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                            .withMode(AgentPoolMode.SYSTEM)
                            .withEnableNodePublicIp(true)
                            .withEnableEncryptionAtHost(true)
                            .withName("nodepool1")))
            .withLinuxProfile(
                new ContainerServiceLinuxProfile()
                    .withAdminUsername("azureuser")
                    .withSsh(
                        new ContainerServiceSshConfiguration()
                            .withPublicKeys(Arrays.asList(new ContainerServiceSshPublicKey().withKeyData("keydata")))))
            .withWindowsProfile(
                new ManagedClusterWindowsProfile()
                    .withAdminUsername("azureuser")
                    .withAdminPassword("replacePassword1234$"))
            .withServicePrincipalProfile(
                new ManagedClusterServicePrincipalProfile().withClientId("clientid").withSecret("secret"))
            .withAddonProfiles(mapOf())
            .withEnableRbac(true)
            .withEnablePodSecurityPolicy(true)
            .withNetworkProfile(
                new ContainerServiceNetworkProfile()
                    .withOutboundType(OutboundType.LOAD_BALANCER)
                    .withLoadBalancerSku(LoadBalancerSku.STANDARD)
                    .withLoadBalancerProfile(
                        new ManagedClusterLoadBalancerProfile()
                            .withManagedOutboundIPs(
                                new ManagedClusterLoadBalancerProfileManagedOutboundIPs().withCount(2))))
            .withAutoScalerProfile(
                new ManagedClusterPropertiesAutoScalerProfile()
                    .withScanInterval("20s")
                    .withScaleDownDelayAfterAdd("15m"))
            .withApiServerAccessProfile(
                new ManagedClusterApiServerAccessProfile()
                    .withEnablePrivateCluster(true)
                    .withEnablePrivateClusterPublicFqdn(true))
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

### ManagedClusters_Delete

```java
import com.azure.core.util.Context;

/** Samples for ManagedClusters Delete. */
public final class ManagedClustersDeleteSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersDelete.json
     */
    /**
     * Sample code: Delete Managed Cluster.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void deleteManagedCluster(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.managedClusters().delete("rg1", "clustername1", Context.NONE);
    }
}
```

### ManagedClusters_GetAccessProfile

```java
import com.azure.core.util.Context;

/** Samples for ManagedClusters GetAccessProfile. */
public final class ManagedClustersGetAccessProfileSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersGetAccessProfile.json
     */
    /**
     * Sample code: Get Managed Cluster.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void getManagedCluster(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.managedClusters().getAccessProfileWithResponse("rg1", "clustername1", "clusterUser", Context.NONE);
    }
}
```

### ManagedClusters_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ManagedClusters GetByResourceGroup. */
public final class ManagedClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersGet.json
     */
    /**
     * Sample code: Get Managed Cluster.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void getManagedCluster(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.managedClusters().getByResourceGroupWithResponse("rg1", "clustername1", Context.NONE);
    }
}
```

### ManagedClusters_GetCommandResult

```java
import com.azure.core.util.Context;

/** Samples for ManagedClusters GetCommandResult. */
public final class ManagedClustersGetCommandResultSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/RunCommandResultFailed.json
     */
    /**
     * Sample code: commandFailedResult.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void commandFailedResult(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .getCommandResultWithResponse("rg1", "clustername1", "def7b3ea71bd4f7e9d226ddbc0f00ad9", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/RunCommandResultSucceed.json
     */
    /**
     * Sample code: commandSucceedResult.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void commandSucceedResult(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .getCommandResultWithResponse("rg1", "clustername1", "def7b3ea71bd4f7e9d226ddbc0f00ad9", Context.NONE);
    }
}
```

### ManagedClusters_GetOSOptions

```java
import com.azure.core.util.Context;

/** Samples for ManagedClusters GetOSOptions. */
public final class ManagedClustersGetOSOptionsSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ContainerServiceGetOSOptions.json
     */
    /**
     * Sample code: Get Container Service OS Options.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void getContainerServiceOSOptions(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.managedClusters().getOSOptionsWithResponse("location1", null, Context.NONE);
    }
}
```

### ManagedClusters_GetUpgradeProfile

```java
import com.azure.core.util.Context;

/** Samples for ManagedClusters GetUpgradeProfile. */
public final class ManagedClustersGetUpgradeProfileSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersGetUpgradeProfile.json
     */
    /**
     * Sample code: Get Upgrade Profile for Managed Cluster.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void getUpgradeProfileForManagedCluster(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.managedClusters().getUpgradeProfileWithResponse("rg1", "clustername1", Context.NONE);
    }
}
```

### ManagedClusters_List

```java
import com.azure.core.util.Context;

/** Samples for ManagedClusters List. */
public final class ManagedClustersListSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersList.json
     */
    /**
     * Sample code: List Managed Clusters.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void listManagedClusters(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.managedClusters().list(Context.NONE);
    }
}
```

### ManagedClusters_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ManagedClusters ListByResourceGroup. */
public final class ManagedClustersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersListByResourceGroup.json
     */
    /**
     * Sample code: Get Managed Clusters by Resource Group.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void getManagedClustersByResourceGroup(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.managedClusters().listByResourceGroup("rg1", Context.NONE);
    }
}
```

### ManagedClusters_ListClusterAdminCredentials

```java
import com.azure.core.util.Context;

/** Samples for ManagedClusters ListClusterAdminCredentials. */
public final class ManagedClustersListClusterAdminCredentialsSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersListClusterCredentialResult.json
     */
    /**
     * Sample code: Get Managed Cluster.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void getManagedCluster(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.managedClusters().listClusterAdminCredentialsWithResponse("rg1", "clustername1", null, Context.NONE);
    }
}
```

### ManagedClusters_ListClusterMonitoringUserCredentials

```java
import com.azure.core.util.Context;

/** Samples for ManagedClusters ListClusterMonitoringUserCredentials. */
public final class ManagedClustersListClusterMonitoringUserCredentialsSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersListClusterCredentialResult.json
     */
    /**
     * Sample code: Get Managed Cluster.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void getManagedCluster(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .listClusterMonitoringUserCredentialsWithResponse("rg1", "clustername1", null, Context.NONE);
    }
}
```

### ManagedClusters_ListClusterUserCredentials

```java
import com.azure.core.util.Context;

/** Samples for ManagedClusters ListClusterUserCredentials. */
public final class ManagedClustersListClusterUserCredentialsSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersListClusterCredentialResult.json
     */
    /**
     * Sample code: Get Managed Cluster.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void getManagedCluster(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .listClusterUserCredentialsWithResponse("rg1", "clustername1", null, null, Context.NONE);
    }
}
```

### ManagedClusters_ListOutboundNetworkDependenciesEndpoints

```java
import com.azure.core.util.Context;

/** Samples for ManagedClusters ListOutboundNetworkDependenciesEndpoints. */
public final class ManagedClustersListOutboundNetworkDependenciesEndpointsSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/OutboundNetworkDependenciesEndpointsList.json
     */
    /**
     * Sample code: List OutboundNetworkDependenciesEndpoints by Managed Cluster.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void listOutboundNetworkDependenciesEndpointsByManagedCluster(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.managedClusters().listOutboundNetworkDependenciesEndpoints("rg1", "clustername1", Context.NONE);
    }
}
```

### ManagedClusters_ResetAadProfile

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.containerservice.generated.models.ManagedClusterAadProfile;

/** Samples for ManagedClusters ResetAadProfile. */
public final class ManagedClustersResetAadProfileSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersResetAADProfile.json
     */
    /**
     * Sample code: Reset AAD Profile.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void resetAADProfile(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .resetAadProfile(
                "rg1",
                "clustername1",
                new ManagedClusterAadProfile()
                    .withClientAppId("clientappid")
                    .withServerAppId("serverappid")
                    .withServerAppSecret("serverappsecret")
                    .withTenantId("tenantid"),
                Context.NONE);
    }
}
```

### ManagedClusters_ResetServicePrincipalProfile

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.containerservice.generated.models.ManagedClusterServicePrincipalProfile;

/** Samples for ManagedClusters ResetServicePrincipalProfile. */
public final class ManagedClustersResetServicePrincipalProfileSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersResetServicePrincipalProfile.json
     */
    /**
     * Sample code: Reset Service Principal Profile.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void resetServicePrincipalProfile(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .resetServicePrincipalProfile(
                "rg1",
                "clustername1",
                new ManagedClusterServicePrincipalProfile().withClientId("clientid").withSecret("secret"),
                Context.NONE);
    }
}
```

### ManagedClusters_RotateClusterCertificates

```java
import com.azure.core.util.Context;

/** Samples for ManagedClusters RotateClusterCertificates. */
public final class ManagedClustersRotateClusterCertificatesSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersRotateClusterCertificates.json
     */
    /**
     * Sample code: Rotate Cluster Certificates.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void rotateClusterCertificates(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.managedClusters().rotateClusterCertificates("rg1", "clustername1", Context.NONE);
    }
}
```

### ManagedClusters_RunCommand

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.containerservice.generated.models.RunCommandRequest;

/** Samples for ManagedClusters RunCommand. */
public final class ManagedClustersRunCommandSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/RunCommandRequest.json
     */
    /**
     * Sample code: submitNewCommand.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void submitNewCommand(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .managedClusters()
            .runCommand(
                "rg1",
                "clustername1",
                new RunCommandRequest().withCommand("kubectl apply -f ns.yaml").withContext("").withClusterToken(""),
                Context.NONE);
    }
}
```

### ManagedClusters_Start

```java
import com.azure.core.util.Context;

/** Samples for ManagedClusters Start. */
public final class ManagedClustersStartSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersStart.json
     */
    /**
     * Sample code: Start Managed Cluster.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void startManagedCluster(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.managedClusters().start("rg1", "clustername1", Context.NONE);
    }
}
```

### ManagedClusters_Stop

```java
import com.azure.core.util.Context;

/** Samples for ManagedClusters Stop. */
public final class ManagedClustersStopSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersStop.json
     */
    /**
     * Sample code: Stop Managed Cluster.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void stopManagedCluster(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.managedClusters().stop("rg1", "clustername1", Context.NONE);
    }
}
```

### ManagedClusters_UpdateTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.containerservice.generated.models.ManagedCluster;
import java.util.HashMap;
import java.util.Map;

/** Samples for ManagedClusters UpdateTags. */
public final class ManagedClustersUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ManagedClustersUpdateTags.json
     */
    /**
     * Sample code: Update Managed Cluster Tags.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void updateManagedClusterTags(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        ManagedCluster resource =
            manager.managedClusters().getByResourceGroupWithResponse("rg1", "clustername1", Context.NONE).getValue();
        resource.update().withTags(mapOf("archv3", "", "tier", "testing")).apply();
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

### OpenShiftManagedClusters_CreateOrUpdate

```java
import com.azure.resourcemanager.containerservice.generated.models.NetworkProfile;
import com.azure.resourcemanager.containerservice.generated.models.OSType;
import com.azure.resourcemanager.containerservice.generated.models.OpenShiftAgentPoolProfileRole;
import com.azure.resourcemanager.containerservice.generated.models.OpenShiftContainerServiceVMSize;
import com.azure.resourcemanager.containerservice.generated.models.OpenShiftManagedClusterAadIdentityProvider;
import com.azure.resourcemanager.containerservice.generated.models.OpenShiftManagedClusterAgentPoolProfile;
import com.azure.resourcemanager.containerservice.generated.models.OpenShiftManagedClusterAuthProfile;
import com.azure.resourcemanager.containerservice.generated.models.OpenShiftManagedClusterIdentityProvider;
import com.azure.resourcemanager.containerservice.generated.models.OpenShiftManagedClusterMasterPoolProfile;
import com.azure.resourcemanager.containerservice.generated.models.OpenShiftRouterProfile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for OpenShiftManagedClusters CreateOrUpdate. */
public final class OpenShiftManagedClustersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2019-04-30/examples/OpenShiftManagedClustersCreate_Update.json
     */
    /**
     * Sample code: Create/Update OpenShift Managed Cluster.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createUpdateOpenShiftManagedCluster(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .openShiftManagedClusters()
            .define("clustername1")
            .withRegion("location1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("archv2", "", "tier", "production"))
            .withOpenShiftVersion("v3.11")
            .withNetworkProfile(new NetworkProfile().withVnetCidr("10.0.0.0/8"))
            .withRouterProfiles(Arrays.asList(new OpenShiftRouterProfile().withName("default")))
            .withMasterPoolProfile(
                new OpenShiftManagedClusterMasterPoolProfile()
                    .withName("master")
                    .withCount(3)
                    .withVmSize(OpenShiftContainerServiceVMSize.STANDARD_D4S_V3)
                    .withSubnetCidr("10.0.0.0/24")
                    .withOsType(OSType.LINUX))
            .withAgentPoolProfiles(
                Arrays
                    .asList(
                        new OpenShiftManagedClusterAgentPoolProfile()
                            .withName("infra")
                            .withCount(2)
                            .withVmSize(OpenShiftContainerServiceVMSize.STANDARD_D4S_V3)
                            .withSubnetCidr("10.0.0.0/24")
                            .withOsType(OSType.LINUX)
                            .withRole(OpenShiftAgentPoolProfileRole.INFRA),
                        new OpenShiftManagedClusterAgentPoolProfile()
                            .withName("compute")
                            .withCount(4)
                            .withVmSize(OpenShiftContainerServiceVMSize.STANDARD_D4S_V3)
                            .withSubnetCidr("10.0.0.0/24")
                            .withOsType(OSType.LINUX)
                            .withRole(OpenShiftAgentPoolProfileRole.COMPUTE)))
            .withAuthProfile(
                new OpenShiftManagedClusterAuthProfile()
                    .withIdentityProviders(
                        Arrays
                            .asList(
                                new OpenShiftManagedClusterIdentityProvider()
                                    .withName("Azure AD")
                                    .withProvider(
                                        new OpenShiftManagedClusterAadIdentityProvider()
                                            .withClientId("clientId")
                                            .withSecret("secret")
                                            .withTenantId("tenantId")
                                            .withCustomerAdminGroupId("customerAdminGroupId")))))
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

### OpenShiftManagedClusters_Delete

```java
import com.azure.core.util.Context;

/** Samples for OpenShiftManagedClusters Delete. */
public final class OpenShiftManagedClustersDeleteSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2019-04-30/examples/OpenShiftManagedClustersDelete.json
     */
    /**
     * Sample code: Delete OpenShift Managed Cluster.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void deleteOpenShiftManagedCluster(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.openShiftManagedClusters().delete("rg1", "clustername1", Context.NONE);
    }
}
```

### OpenShiftManagedClusters_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for OpenShiftManagedClusters GetByResourceGroup. */
public final class OpenShiftManagedClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2019-04-30/examples/OpenShiftManagedClustersGet.json
     */
    /**
     * Sample code: Get OpenShift Managed Cluster.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void getOpenShiftManagedCluster(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.openShiftManagedClusters().getByResourceGroupWithResponse("rg1", "clustername1", Context.NONE);
    }
}
```

### OpenShiftManagedClusters_List

```java
import com.azure.core.util.Context;

/** Samples for OpenShiftManagedClusters List. */
public final class OpenShiftManagedClustersListSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2019-04-30/examples/OpenShiftManagedClustersList.json
     */
    /**
     * Sample code: List Managed Clusters.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void listManagedClusters(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.openShiftManagedClusters().list(Context.NONE);
    }
}
```

### OpenShiftManagedClusters_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for OpenShiftManagedClusters ListByResourceGroup. */
public final class OpenShiftManagedClustersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2019-04-30/examples/OpenShiftManagedClustersListByResourceGroup.json
     */
    /**
     * Sample code: Get Managed Clusters by Resource Group.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void getManagedClustersByResourceGroup(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.openShiftManagedClusters().listByResourceGroup("rg1", Context.NONE);
    }
}
```

### OpenShiftManagedClusters_UpdateTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.containerservice.generated.models.OpenShiftManagedCluster;
import java.util.HashMap;
import java.util.Map;

/** Samples for OpenShiftManagedClusters UpdateTags. */
public final class OpenShiftManagedClustersUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2019-04-30/examples/OpenShiftManagedClustersUpdateTags.json
     */
    /**
     * Sample code: Update OpenShift Managed Cluster Tags.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void updateOpenShiftManagedClusterTags(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        OpenShiftManagedCluster resource =
            manager
                .openShiftManagedClusters()
                .getByResourceGroupWithResponse("rg1", "clustername1", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("archv3", "", "tier", "testing")).apply();
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
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/Operation_List.json
     */
    /**
     * Sample code: List available operations for the container service resource provider.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void listAvailableOperationsForTheContainerServiceResourceProvider(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### PrivateEndpointConnections_Delete

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Delete. */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/PrivateEndpointConnectionsDelete.json
     */
    /**
     * Sample code: Delete Private Endpoint Connection.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void deletePrivateEndpointConnection(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.privateEndpointConnections().delete("rg1", "clustername1", "privateendpointconnection1", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/PrivateEndpointConnectionsGet.json
     */
    /**
     * Sample code: Get Private Endpoint Connection.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void getPrivateEndpointConnection(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse("rg1", "clustername1", "privateendpointconnection1", Context.NONE);
    }
}
```

### PrivateEndpointConnections_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections List. */
public final class PrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/PrivateEndpointConnectionsList.json
     */
    /**
     * Sample code: List Private Endpoint Connections by Managed Cluster.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void listPrivateEndpointConnectionsByManagedCluster(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.privateEndpointConnections().listWithResponse("rg1", "clustername1", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.containerservice.generated.fluent.models.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.containerservice.generated.models.ConnectionStatus;
import com.azure.resourcemanager.containerservice.generated.models.PrivateLinkServiceConnectionState;

/** Samples for PrivateEndpointConnections Update. */
public final class PrivateEndpointConnectionsUpdateSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/PrivateEndpointConnectionsUpdate.json
     */
    /**
     * Sample code: Update Private Endpoint Connection.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void updatePrivateEndpointConnection(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .privateEndpointConnections()
            .updateWithResponse(
                "rg1",
                "clustername1",
                "privateendpointconnection1",
                new PrivateEndpointConnectionInner()
                    .withPrivateLinkServiceConnectionState(
                        new PrivateLinkServiceConnectionState().withStatus(ConnectionStatus.APPROVED)),
                Context.NONE);
    }
}
```

### PrivateLinkResources_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources List. */
public final class PrivateLinkResourcesListSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/PrivateLinkResourcesList.json
     */
    /**
     * Sample code: List Private Link Resources by Managed Cluster.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void listPrivateLinkResourcesByManagedCluster(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.privateLinkResources().listWithResponse("rg1", "clustername1", Context.NONE);
    }
}
```

### ResolvePrivateLinkServiceId_Post

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.containerservice.generated.fluent.models.PrivateLinkResourceInner;

/** Samples for ResolvePrivateLinkServiceId Post. */
public final class ResolvePrivateLinkServiceIdPostSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/ResolvePrivateLinkServiceId.json
     */
    /**
     * Sample code: Resolve the Private Link Service ID for Managed Cluster.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void resolveThePrivateLinkServiceIDForManagedCluster(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .resolvePrivateLinkServiceIds()
            .postWithResponse(
                "rg1", "clustername1", new PrivateLinkResourceInner().withName("management"), Context.NONE);
    }
}
```

### Snapshots_CreateOrUpdate

```java
import com.azure.resourcemanager.containerservice.generated.models.CreationData;
import java.util.HashMap;
import java.util.Map;

/** Samples for Snapshots CreateOrUpdate. */
public final class SnapshotsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/SnapshotsCreate.json
     */
    /**
     * Sample code: Create/Update Snapshot.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void createUpdateSnapshot(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager
            .snapshots()
            .define("snapshot1")
            .withRegion("westus")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("key1", "val1", "key2", "val2"))
            .withCreationData(
                new CreationData()
                    .withSourceResourceId(
                        "/subscriptions/subid1/resourcegroups/rg1/providers/Microsoft.ContainerService/managedClusters/cluster1/agentPools/pool0"))
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

### Snapshots_Delete

```java
import com.azure.core.util.Context;

/** Samples for Snapshots Delete. */
public final class SnapshotsDeleteSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/SnapshotsDelete.json
     */
    /**
     * Sample code: Delete Snapshot.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void deleteSnapshot(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.snapshots().deleteWithResponse("rg1", "snapshot1", Context.NONE);
    }
}
```

### Snapshots_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Snapshots GetByResourceGroup. */
public final class SnapshotsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/SnapshotsGet.json
     */
    /**
     * Sample code: Get Snapshot.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void getSnapshot(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.snapshots().getByResourceGroupWithResponse("rg1", "snapshot1", Context.NONE);
    }
}
```

### Snapshots_List

```java
import com.azure.core.util.Context;

/** Samples for Snapshots List. */
public final class SnapshotsListSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/SnapshotsList.json
     */
    /**
     * Sample code: List Snapshots.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void listSnapshots(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.snapshots().list(Context.NONE);
    }
}
```

### Snapshots_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Snapshots ListByResourceGroup. */
public final class SnapshotsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/SnapshotsListByResourceGroup.json
     */
    /**
     * Sample code: List Snapshots by Resource Group.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void listSnapshotsByResourceGroup(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        manager.snapshots().listByResourceGroup("rg1", Context.NONE);
    }
}
```

### Snapshots_UpdateTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.containerservice.generated.models.Snapshot;
import java.util.HashMap;
import java.util.Map;

/** Samples for Snapshots UpdateTags. */
public final class SnapshotsUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/stable/2022-02-01/examples/SnapshotsUpdateTags.json
     */
    /**
     * Sample code: Update Snapshot Tags.
     *
     * @param manager Entry point to ContainerServiceManager.
     */
    public static void updateSnapshotTags(
        com.azure.resourcemanager.containerservice.generated.ContainerServiceManager manager) {
        Snapshot resource =
            manager.snapshots().getByResourceGroupWithResponse("rg1", "snapshot1", Context.NONE).getValue();
        resource.update().withTags(mapOf("key2", "new-val2", "key3", "val3")).apply();
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

