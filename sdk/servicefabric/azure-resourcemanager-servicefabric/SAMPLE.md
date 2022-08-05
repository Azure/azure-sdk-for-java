# Code snippets and samples


## ApplicationTypeVersions

- [CreateOrUpdate](#applicationtypeversions_createorupdate)
- [Delete](#applicationtypeversions_delete)
- [Get](#applicationtypeversions_get)
- [List](#applicationtypeversions_list)

## ApplicationTypes

- [CreateOrUpdate](#applicationtypes_createorupdate)
- [Delete](#applicationtypes_delete)
- [Get](#applicationtypes_get)
- [List](#applicationtypes_list)

## Applications

- [CreateOrUpdate](#applications_createorupdate)
- [Delete](#applications_delete)
- [Get](#applications_get)
- [List](#applications_list)
- [Update](#applications_update)

## ClusterVersions

- [Get](#clusterversions_get)
- [GetByEnvironment](#clusterversions_getbyenvironment)
- [List](#clusterversions_list)
- [ListByEnvironment](#clusterversions_listbyenvironment)

## Clusters

- [CreateOrUpdate](#clusters_createorupdate)
- [Delete](#clusters_delete)
- [GetByResourceGroup](#clusters_getbyresourcegroup)
- [List](#clusters_list)
- [ListByResourceGroup](#clusters_listbyresourcegroup)
- [ListUpgradableVersions](#clusters_listupgradableversions)
- [Update](#clusters_update)

## Operations

- [List](#operations_list)

## Services

- [CreateOrUpdate](#services_createorupdate)
- [Delete](#services_delete)
- [Get](#services_get)
- [List](#services_list)
- [Update](#services_update)
### ApplicationTypeVersions_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for ApplicationTypeVersions CreateOrUpdate. */
public final class ApplicationTypeVersionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ApplicationTypeVersionPutOperation_example.json
     */
    /**
     * Sample code: Put an application type version.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void putAnApplicationTypeVersion(
        com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager
            .applicationTypeVersions()
            .define("1.0")
            .withExistingApplicationType("resRg", "myCluster", "myAppType")
            .withTags(mapOf())
            .withAppPackageUrl("http://fakelink.test.com/MyAppType")
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

### ApplicationTypeVersions_Delete

```java
import com.azure.core.util.Context;

/** Samples for ApplicationTypeVersions Delete. */
public final class ApplicationTypeVersionsDeleteSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ApplicationTypeVersionDeleteOperation_example.json
     */
    /**
     * Sample code: Delete an application type version.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void deleteAnApplicationTypeVersion(
        com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager.applicationTypeVersions().delete("resRg", "myCluster", "myAppType", "1.0", Context.NONE);
    }
}
```

### ApplicationTypeVersions_Get

```java
import com.azure.core.util.Context;

/** Samples for ApplicationTypeVersions Get. */
public final class ApplicationTypeVersionsGetSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ApplicationTypeVersionGetOperation_example.json
     */
    /**
     * Sample code: Get an application type version.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void getAnApplicationTypeVersion(
        com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager.applicationTypeVersions().getWithResponse("resRg", "myCluster", "myAppType", "1.0", Context.NONE);
    }
}
```

### ApplicationTypeVersions_List

```java
import com.azure.core.util.Context;

/** Samples for ApplicationTypeVersions List. */
public final class ApplicationTypeVersionsListSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ApplicationTypeVersionListOperation_example.json
     */
    /**
     * Sample code: Get a list of application type version resources.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void getAListOfApplicationTypeVersionResources(
        com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager.applicationTypeVersions().listWithResponse("resRg", "myCluster", "myAppType", Context.NONE);
    }
}
```

### ApplicationTypes_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for ApplicationTypes CreateOrUpdate. */
public final class ApplicationTypesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ApplicationTypeNamePutOperation_example.json
     */
    /**
     * Sample code: Put an application type.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void putAnApplicationType(com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager
            .applicationTypes()
            .define("myAppType")
            .withExistingCluster("resRg", "myCluster")
            .withTags(mapOf())
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

### ApplicationTypes_Delete

```java
import com.azure.core.util.Context;

/** Samples for ApplicationTypes Delete. */
public final class ApplicationTypesDeleteSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ApplicationTypeNameDeleteOperation_example.json
     */
    /**
     * Sample code: Delete an application type.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void deleteAnApplicationType(com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager.applicationTypes().delete("resRg", "myCluster", "myAppType", Context.NONE);
    }
}
```

### ApplicationTypes_Get

```java
import com.azure.core.util.Context;

/** Samples for ApplicationTypes Get. */
public final class ApplicationTypesGetSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ApplicationTypeNameGetOperation_example.json
     */
    /**
     * Sample code: Get an application type.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void getAnApplicationType(com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager.applicationTypes().getWithResponse("resRg", "myCluster", "myAppType", Context.NONE);
    }
}
```

### ApplicationTypes_List

```java
import com.azure.core.util.Context;

/** Samples for ApplicationTypes List. */
public final class ApplicationTypesListSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ApplicationTypeNameListOperation_example.json
     */
    /**
     * Sample code: Get a list of application type name resources.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void getAListOfApplicationTypeNameResources(
        com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager.applicationTypes().listWithResponse("resRg", "myCluster", Context.NONE);
    }
}
```

### Applications_CreateOrUpdate

```java
import com.azure.resourcemanager.servicefabric.models.ApplicationMetricDescription;
import com.azure.resourcemanager.servicefabric.models.ApplicationUpgradePolicy;
import com.azure.resourcemanager.servicefabric.models.ArmApplicationHealthPolicy;
import com.azure.resourcemanager.servicefabric.models.ArmRollingUpgradeMonitoringPolicy;
import com.azure.resourcemanager.servicefabric.models.ArmServiceTypeHealthPolicy;
import com.azure.resourcemanager.servicefabric.models.ArmUpgradeFailureAction;
import com.azure.resourcemanager.servicefabric.models.RollingUpgradeMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Applications CreateOrUpdate. */
public final class ApplicationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ApplicationPutOperation_example_max.json
     */
    /**
     * Sample code: Put an application with maximum parameters.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void putAnApplicationWithMaximumParameters(
        com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager
            .applications()
            .define("myApp")
            .withExistingCluster("resRg", "myCluster")
            .withTags(mapOf())
            .withTypeName("myAppType")
            .withTypeVersion("1.0")
            .withParameters(mapOf("param1", "value1"))
            .withUpgradePolicy(
                new ApplicationUpgradePolicy()
                    .withUpgradeReplicaSetCheckTimeout("01:00:00")
                    .withForceRestart(false)
                    .withRollingUpgradeMonitoringPolicy(
                        new ArmRollingUpgradeMonitoringPolicy()
                            .withFailureAction(ArmUpgradeFailureAction.ROLLBACK)
                            .withHealthCheckWaitDuration("00:02:00")
                            .withHealthCheckStableDuration("00:05:00")
                            .withHealthCheckRetryTimeout("00:10:00")
                            .withUpgradeTimeout("01:00:00")
                            .withUpgradeDomainTimeout("1.06:00:00"))
                    .withApplicationHealthPolicy(
                        new ArmApplicationHealthPolicy()
                            .withConsiderWarningAsError(true)
                            .withMaxPercentUnhealthyDeployedApplications(0)
                            .withDefaultServiceTypeHealthPolicy(
                                new ArmServiceTypeHealthPolicy()
                                    .withMaxPercentUnhealthyServices(0)
                                    .withMaxPercentUnhealthyPartitionsPerService(0)
                                    .withMaxPercentUnhealthyReplicasPerPartition(0)))
                    .withUpgradeMode(RollingUpgradeMode.MONITORED))
            .withMinimumNodes(1L)
            .withMaximumNodes(3L)
            .withRemoveApplicationCapacity(false)
            .withMetrics(
                Arrays
                    .asList(
                        new ApplicationMetricDescription()
                            .withName("metric1")
                            .withMaximumCapacity(3L)
                            .withReservationCapacity(1L)
                            .withTotalApplicationCapacity(5L)))
            .create();
    }

    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ApplicationPutOperation_example_min.json
     */
    /**
     * Sample code: Put an application with minimum parameters.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void putAnApplicationWithMinimumParameters(
        com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager
            .applications()
            .define("myApp")
            .withExistingCluster("resRg", "myCluster")
            .withRegion("eastus")
            .withTags(mapOf())
            .withTypeName("myAppType")
            .withTypeVersion("1.0")
            .withRemoveApplicationCapacity(false)
            .create();
    }

    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ApplicationPutOperation_recreate_example.json
     */
    /**
     * Sample code: Put an application with recreate option.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void putAnApplicationWithRecreateOption(
        com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager
            .applications()
            .define("myApp")
            .withExistingCluster("resRg", "myCluster")
            .withTags(mapOf())
            .withTypeName("myAppType")
            .withTypeVersion("1.0")
            .withParameters(mapOf("param1", "value1"))
            .withUpgradePolicy(new ApplicationUpgradePolicy().withRecreateApplication(true))
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

### Applications_Delete

```java
import com.azure.core.util.Context;

/** Samples for Applications Delete. */
public final class ApplicationsDeleteSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ApplicationDeleteOperation_example.json
     */
    /**
     * Sample code: Delete an application.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void deleteAnApplication(com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager.applications().delete("resRg", "myCluster", "myApp", Context.NONE);
    }
}
```

### Applications_Get

```java
import com.azure.core.util.Context;

/** Samples for Applications Get. */
public final class ApplicationsGetSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ApplicationGetOperation_example.json
     */
    /**
     * Sample code: Get an application.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void getAnApplication(com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager.applications().getWithResponse("resRg", "myCluster", "myApp", Context.NONE);
    }
}
```

### Applications_List

```java
import com.azure.core.util.Context;

/** Samples for Applications List. */
public final class ApplicationsListSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ApplicationListOperation_example.json
     */
    /**
     * Sample code: Get a list of application resources.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void getAListOfApplicationResources(
        com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager.applications().listWithResponse("resRg", "myCluster", Context.NONE);
    }
}
```

### Applications_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.servicefabric.models.ApplicationMetricDescription;
import com.azure.resourcemanager.servicefabric.models.ApplicationResource;
import java.util.Arrays;

/** Samples for Applications Update. */
public final class ApplicationsUpdateSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ApplicationPatchOperation_example.json
     */
    /**
     * Sample code: Patch an application.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void patchAnApplication(com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        ApplicationResource resource =
            manager.applications().getWithResponse("resRg", "myCluster", "myApp", Context.NONE).getValue();
        resource
            .update()
            .withTypeVersion("1.0")
            .withRemoveApplicationCapacity(false)
            .withMetrics(
                Arrays
                    .asList(
                        new ApplicationMetricDescription()
                            .withName("metric1")
                            .withMaximumCapacity(3L)
                            .withReservationCapacity(1L)
                            .withTotalApplicationCapacity(5L)))
            .apply();
    }
}
```

### ClusterVersions_Get

```java
import com.azure.core.util.Context;

/** Samples for ClusterVersions Get. */
public final class ClusterVersionsGetSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ClusterVersionsGet_example.json
     */
    /**
     * Sample code: Get cluster version.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void getClusterVersion(com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager.clusterVersions().getWithResponse("eastus", "6.1.480.9494", Context.NONE);
    }
}
```

### ClusterVersions_GetByEnvironment

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.servicefabric.models.ClusterVersionsEnvironment;

/** Samples for ClusterVersions GetByEnvironment. */
public final class ClusterVersionsGetByEnvironmentSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ClusterVersionsGetByEnvironment_example.json
     */
    /**
     * Sample code: Get cluster version by environment.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void getClusterVersionByEnvironment(
        com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager
            .clusterVersions()
            .getByEnvironmentWithResponse("eastus", ClusterVersionsEnvironment.WINDOWS, "6.1.480.9494", Context.NONE);
    }
}
```

### ClusterVersions_List

```java
import com.azure.core.util.Context;

/** Samples for ClusterVersions List. */
public final class ClusterVersionsListSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ClusterVersionsList_example.json
     */
    /**
     * Sample code: List cluster versions.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void listClusterVersions(com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager.clusterVersions().listWithResponse("eastus", Context.NONE);
    }
}
```

### ClusterVersions_ListByEnvironment

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.servicefabric.models.ClusterVersionsEnvironment;

/** Samples for ClusterVersions ListByEnvironment. */
public final class ClusterVersionsListByEnvironmentSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ClusterVersionsListByEnvironment.json
     */
    /**
     * Sample code: List cluster versions by environment.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void listClusterVersionsByEnvironment(
        com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager
            .clusterVersions()
            .listByEnvironmentWithResponse("eastus", ClusterVersionsEnvironment.WINDOWS, Context.NONE);
    }
}
```

### Clusters_CreateOrUpdate

```java
import com.azure.resourcemanager.servicefabric.models.AddOnFeatures;
import com.azure.resourcemanager.servicefabric.models.ApplicationDeltaHealthPolicy;
import com.azure.resourcemanager.servicefabric.models.ApplicationHealthPolicy;
import com.azure.resourcemanager.servicefabric.models.ApplicationTypeVersionsCleanupPolicy;
import com.azure.resourcemanager.servicefabric.models.AzureActiveDirectory;
import com.azure.resourcemanager.servicefabric.models.ClientCertificateCommonName;
import com.azure.resourcemanager.servicefabric.models.ClientCertificateThumbprint;
import com.azure.resourcemanager.servicefabric.models.ClusterHealthPolicy;
import com.azure.resourcemanager.servicefabric.models.ClusterUpgradeCadence;
import com.azure.resourcemanager.servicefabric.models.ClusterUpgradeDeltaHealthPolicy;
import com.azure.resourcemanager.servicefabric.models.ClusterUpgradePolicy;
import com.azure.resourcemanager.servicefabric.models.DiagnosticsStorageAccountConfig;
import com.azure.resourcemanager.servicefabric.models.DurabilityLevel;
import com.azure.resourcemanager.servicefabric.models.EndpointRangeDescription;
import com.azure.resourcemanager.servicefabric.models.NodeTypeDescription;
import com.azure.resourcemanager.servicefabric.models.Notification;
import com.azure.resourcemanager.servicefabric.models.NotificationCategory;
import com.azure.resourcemanager.servicefabric.models.NotificationChannel;
import com.azure.resourcemanager.servicefabric.models.NotificationLevel;
import com.azure.resourcemanager.servicefabric.models.NotificationTarget;
import com.azure.resourcemanager.servicefabric.models.ReliabilityLevel;
import com.azure.resourcemanager.servicefabric.models.ServerCertificateCommonName;
import com.azure.resourcemanager.servicefabric.models.ServerCertificateCommonNames;
import com.azure.resourcemanager.servicefabric.models.ServiceTypeDeltaHealthPolicy;
import com.azure.resourcemanager.servicefabric.models.ServiceTypeHealthPolicy;
import com.azure.resourcemanager.servicefabric.models.SettingsParameterDescription;
import com.azure.resourcemanager.servicefabric.models.SettingsSectionDescription;
import com.azure.resourcemanager.servicefabric.models.SfZonalUpgradeMode;
import com.azure.resourcemanager.servicefabric.models.StoreName;
import com.azure.resourcemanager.servicefabric.models.UpgradeMode;
import com.azure.resourcemanager.servicefabric.models.VmssZonalUpgradeMode;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Clusters CreateOrUpdate. */
public final class ClustersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ClusterPutOperation_example_max.json
     */
    /**
     * Sample code: Put a cluster with maximum parameters.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void putAClusterWithMaximumParameters(
        com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager
            .clusters()
            .define("myCluster")
            .withRegion("eastus")
            .withExistingResourceGroup("resRg")
            .withTags(mapOf())
            .withAddOnFeatures(
                Arrays
                    .asList(
                        AddOnFeatures.REPAIR_MANAGER,
                        AddOnFeatures.DNS_SERVICE,
                        AddOnFeatures.BACKUP_RESTORE_SERVICE,
                        AddOnFeatures.RESOURCE_MONITOR_SERVICE))
            .withAzureActiveDirectory(
                new AzureActiveDirectory()
                    .withTenantId("6abcc6a0-8666-43f1-87b8-172cf86a9f9c")
                    .withClusterApplication("5886372e-7bf4-4878-a497-8098aba608ae")
                    .withClientApplication("d151ad89-4bce-4ae8-b3d1-1dc79679fa75"))
            .withCertificateCommonNames(
                new ServerCertificateCommonNames()
                    .withCommonNames(
                        Arrays
                            .asList(
                                new ServerCertificateCommonName()
                                    .withCertificateCommonName("abc.com")
                                    .withCertificateIssuerThumbprint("12599211F8F14C90AFA9532AD79A6F2CA1C00622")))
                    .withX509StoreName(StoreName.MY))
            .withClientCertificateCommonNames(
                Arrays
                    .asList(
                        new ClientCertificateCommonName()
                            .withIsAdmin(true)
                            .withCertificateCommonName("abc.com")
                            .withCertificateIssuerThumbprint("5F3660C715EBBDA31DB1FFDCF508302348DE8E7A")))
            .withClientCertificateThumbprints(
                Arrays
                    .asList(
                        new ClientCertificateThumbprint()
                            .withIsAdmin(true)
                            .withCertificateThumbprint("5F3660C715EBBDA31DB1FFDCF508302348DE8E7A")))
            .withClusterCodeVersion("7.0.470.9590")
            .withDiagnosticsStorageAccountConfig(
                new DiagnosticsStorageAccountConfig()
                    .withStorageAccountName("diag")
                    .withProtectedAccountKeyName("StorageAccountKey1")
                    .withBlobEndpoint("https://diag.blob.core.windows.net/")
                    .withQueueEndpoint("https://diag.queue.core.windows.net/")
                    .withTableEndpoint("https://diag.table.core.windows.net/"))
            .withEventStoreServiceEnabled(true)
            .withFabricSettings(
                Arrays
                    .asList(
                        new SettingsSectionDescription()
                            .withName("UpgradeService")
                            .withParameters(
                                Arrays
                                    .asList(
                                        new SettingsParameterDescription()
                                            .withName("AppPollIntervalInSeconds")
                                            .withValue("60")))))
            .withManagementEndpoint("https://myCluster.eastus.cloudapp.azure.com:19080")
            .withNodeTypes(
                Arrays
                    .asList(
                        new NodeTypeDescription()
                            .withName("nt1vm")
                            .withClientConnectionEndpointPort(19000)
                            .withHttpGatewayEndpointPort(19007)
                            .withDurabilityLevel(DurabilityLevel.SILVER)
                            .withApplicationPorts(
                                new EndpointRangeDescription().withStartPort(20000).withEndPort(30000))
                            .withEphemeralPorts(new EndpointRangeDescription().withStartPort(49000).withEndPort(64000))
                            .withIsPrimary(true)
                            .withVmInstanceCount(5)
                            .withIsStateless(false)
                            .withMultipleAvailabilityZones(true)))
            .withReliabilityLevel(ReliabilityLevel.PLATINUM)
            .withReverseProxyCertificateCommonNames(
                new ServerCertificateCommonNames()
                    .withCommonNames(
                        Arrays
                            .asList(
                                new ServerCertificateCommonName()
                                    .withCertificateCommonName("abc.com")
                                    .withCertificateIssuerThumbprint("12599211F8F14C90AFA9532AD79A6F2CA1C00622")))
                    .withX509StoreName(StoreName.MY))
            .withUpgradeDescription(
                new ClusterUpgradePolicy()
                    .withForceRestart(false)
                    .withUpgradeReplicaSetCheckTimeout("00:10:00")
                    .withHealthCheckWaitDuration("00:00:30")
                    .withHealthCheckStableDuration("00:00:30")
                    .withHealthCheckRetryTimeout("00:05:00")
                    .withUpgradeTimeout("01:00:00")
                    .withUpgradeDomainTimeout("00:15:00")
                    .withHealthPolicy(
                        new ClusterHealthPolicy()
                            .withMaxPercentUnhealthyNodes(0)
                            .withMaxPercentUnhealthyApplications(0)
                            .withApplicationHealthPolicies(
                                mapOf(
                                    "fabric:/myApp1",
                                    new ApplicationHealthPolicy()
                                        .withDefaultServiceTypeHealthPolicy(
                                            new ServiceTypeHealthPolicy().withMaxPercentUnhealthyServices(0))
                                        .withServiceTypeHealthPolicies(
                                            mapOf(
                                                "myServiceType1",
                                                new ServiceTypeHealthPolicy().withMaxPercentUnhealthyServices(100))))))
                    .withDeltaHealthPolicy(
                        new ClusterUpgradeDeltaHealthPolicy()
                            .withMaxPercentDeltaUnhealthyNodes(0)
                            .withMaxPercentUpgradeDomainDeltaUnhealthyNodes(0)
                            .withMaxPercentDeltaUnhealthyApplications(0)
                            .withApplicationDeltaHealthPolicies(
                                mapOf(
                                    "fabric:/myApp1",
                                    new ApplicationDeltaHealthPolicy()
                                        .withDefaultServiceTypeDeltaHealthPolicy(
                                            new ServiceTypeDeltaHealthPolicy().withMaxPercentDeltaUnhealthyServices(0))
                                        .withServiceTypeDeltaHealthPolicies(
                                            mapOf(
                                                "myServiceType1",
                                                new ServiceTypeDeltaHealthPolicy()
                                                    .withMaxPercentDeltaUnhealthyServices(0)))))))
            .withUpgradeMode(UpgradeMode.MANUAL)
            .withApplicationTypeVersionsCleanupPolicy(
                new ApplicationTypeVersionsCleanupPolicy().withMaxUnusedVersionsToKeep(2L))
            .withVmImage("Windows")
            .withSfZonalUpgradeMode(SfZonalUpgradeMode.HIERARCHICAL)
            .withVmssZonalUpgradeMode(VmssZonalUpgradeMode.PARALLEL)
            .withInfrastructureServiceManager(true)
            .withUpgradeWave(ClusterUpgradeCadence.WAVE1)
            .withUpgradePauseStartTimestampUtc(OffsetDateTime.parse("2021-06-21T22:00:00Z"))
            .withUpgradePauseEndTimestampUtc(OffsetDateTime.parse("2021-06-25T22:00:00Z"))
            .withNotifications(
                Arrays
                    .asList(
                        new Notification()
                            .withIsEnabled(true)
                            .withNotificationCategory(NotificationCategory.WAVE_PROGRESS)
                            .withNotificationLevel(NotificationLevel.CRITICAL)
                            .withNotificationTargets(
                                Arrays
                                    .asList(
                                        new NotificationTarget()
                                            .withNotificationChannel(NotificationChannel.EMAIL_USER)
                                            .withReceivers(Arrays.asList("****@microsoft.com", "****@microsoft.com")),
                                        new NotificationTarget()
                                            .withNotificationChannel(NotificationChannel.EMAIL_SUBSCRIPTION)
                                            .withReceivers(Arrays.asList("Owner", "AccountAdmin")))),
                        new Notification()
                            .withIsEnabled(true)
                            .withNotificationCategory(NotificationCategory.WAVE_PROGRESS)
                            .withNotificationLevel(NotificationLevel.ALL)
                            .withNotificationTargets(
                                Arrays
                                    .asList(
                                        new NotificationTarget()
                                            .withNotificationChannel(NotificationChannel.EMAIL_USER)
                                            .withReceivers(Arrays.asList("****@microsoft.com", "****@microsoft.com")),
                                        new NotificationTarget()
                                            .withNotificationChannel(NotificationChannel.EMAIL_SUBSCRIPTION)
                                            .withReceivers(Arrays.asList("Owner", "AccountAdmin"))))))
            .create();
    }

    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ClusterPutOperation_example_min.json
     */
    /**
     * Sample code: Put a cluster with minimum parameters.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void putAClusterWithMinimumParameters(
        com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager
            .clusters()
            .define("myCluster")
            .withRegion("eastus")
            .withExistingResourceGroup("resRg")
            .withTags(mapOf())
            .withDiagnosticsStorageAccountConfig(
                new DiagnosticsStorageAccountConfig()
                    .withStorageAccountName("diag")
                    .withProtectedAccountKeyName("StorageAccountKey1")
                    .withBlobEndpoint("https://diag.blob.core.windows.net/")
                    .withQueueEndpoint("https://diag.queue.core.windows.net/")
                    .withTableEndpoint("https://diag.table.core.windows.net/"))
            .withFabricSettings(
                Arrays
                    .asList(
                        new SettingsSectionDescription()
                            .withName("UpgradeService")
                            .withParameters(
                                Arrays
                                    .asList(
                                        new SettingsParameterDescription()
                                            .withName("AppPollIntervalInSeconds")
                                            .withValue("60")))))
            .withManagementEndpoint("http://myCluster.eastus.cloudapp.azure.com:19080")
            .withNodeTypes(
                Arrays
                    .asList(
                        new NodeTypeDescription()
                            .withName("nt1vm")
                            .withClientConnectionEndpointPort(19000)
                            .withHttpGatewayEndpointPort(19007)
                            .withDurabilityLevel(DurabilityLevel.BRONZE)
                            .withApplicationPorts(
                                new EndpointRangeDescription().withStartPort(20000).withEndPort(30000))
                            .withEphemeralPorts(new EndpointRangeDescription().withStartPort(49000).withEndPort(64000))
                            .withIsPrimary(true)
                            .withVmInstanceCount(5)))
            .withReliabilityLevel(ReliabilityLevel.SILVER)
            .withUpgradeMode(UpgradeMode.AUTOMATIC)
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
import com.azure.core.util.Context;

/** Samples for Clusters Delete. */
public final class ClustersDeleteSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ClusterDeleteOperation_example.json
     */
    /**
     * Sample code: Delete a cluster.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void deleteACluster(com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager.clusters().deleteWithResponse("resRg", "myCluster", Context.NONE);
    }
}
```

### Clusters_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Clusters GetByResourceGroup. */
public final class ClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ClusterGetOperation_example.json
     */
    /**
     * Sample code: Get a cluster.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void getACluster(com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager.clusters().getByResourceGroupWithResponse("resRg", "myCluster", Context.NONE);
    }
}
```

### Clusters_List

```java
import com.azure.core.util.Context;

/** Samples for Clusters List. */
public final class ClustersListSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ClusterListOperation_example.json
     */
    /**
     * Sample code: List clusters.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void listClusters(com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager.clusters().listWithResponse(Context.NONE);
    }
}
```

### Clusters_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Clusters ListByResourceGroup. */
public final class ClustersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ClusterListByResourceGroupOperation_example.json
     */
    /**
     * Sample code: List cluster by resource group.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void listClusterByResourceGroup(
        com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager.clusters().listByResourceGroupWithResponse("resRg", Context.NONE);
    }
}
```

### Clusters_ListUpgradableVersions

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.servicefabric.models.UpgradableVersionsDescription;

/** Samples for Clusters ListUpgradableVersions. */
public final class ClustersListUpgradableVersionsSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ListUpgradableVersionsMinMax_example.json
     */
    /**
     * Sample code: Get minimum and maximum code versions.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void getMinimumAndMaximumCodeVersions(
        com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager.clusters().listUpgradableVersionsWithResponse("resRg", "myCluster", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ListUpgradableVersionsPath_example.json
     */
    /**
     * Sample code: Get upgrade path.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void getUpgradePath(com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager
            .clusters()
            .listUpgradableVersionsWithResponse(
                "resRg",
                "myCluster",
                new UpgradableVersionsDescription().withTargetVersion("7.2.432.9590"),
                Context.NONE);
    }
}
```

### Clusters_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.servicefabric.models.Cluster;
import com.azure.resourcemanager.servicefabric.models.ClusterUpgradeCadence;
import com.azure.resourcemanager.servicefabric.models.DurabilityLevel;
import com.azure.resourcemanager.servicefabric.models.EndpointRangeDescription;
import com.azure.resourcemanager.servicefabric.models.NodeTypeDescription;
import com.azure.resourcemanager.servicefabric.models.ReliabilityLevel;
import com.azure.resourcemanager.servicefabric.models.UpgradeMode;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Clusters Update. */
public final class ClustersUpdateSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ClusterPatchOperation_example.json
     */
    /**
     * Sample code: Patch a cluster.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void patchACluster(com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        Cluster resource =
            manager.clusters().getByResourceGroupWithResponse("resRg", "myCluster", Context.NONE).getValue();
        resource
            .update()
            .withTags(mapOf("a", "b"))
            .withEventStoreServiceEnabled(true)
            .withNodeTypes(
                Arrays
                    .asList(
                        new NodeTypeDescription()
                            .withName("nt1vm")
                            .withClientConnectionEndpointPort(19000)
                            .withHttpGatewayEndpointPort(19007)
                            .withDurabilityLevel(DurabilityLevel.BRONZE)
                            .withApplicationPorts(
                                new EndpointRangeDescription().withStartPort(20000).withEndPort(30000))
                            .withEphemeralPorts(new EndpointRangeDescription().withStartPort(49000).withEndPort(64000))
                            .withIsPrimary(true)
                            .withVmInstanceCount(5),
                        new NodeTypeDescription()
                            .withName("testnt1")
                            .withClientConnectionEndpointPort(0)
                            .withHttpGatewayEndpointPort(0)
                            .withDurabilityLevel(DurabilityLevel.BRONZE)
                            .withApplicationPorts(new EndpointRangeDescription().withStartPort(1000).withEndPort(2000))
                            .withEphemeralPorts(new EndpointRangeDescription().withStartPort(3000).withEndPort(4000))
                            .withIsPrimary(false)
                            .withVmInstanceCount(3)))
            .withReliabilityLevel(ReliabilityLevel.BRONZE)
            .withUpgradeMode(UpgradeMode.AUTOMATIC)
            .withUpgradeWave(ClusterUpgradeCadence.fromString("Wave"))
            .withUpgradePauseStartTimestampUtc(OffsetDateTime.parse("2021-06-21T22:00:00Z"))
            .withUpgradePauseEndTimestampUtc(OffsetDateTime.parse("2021-06-25T22:00:00Z"))
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
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ListOperations.json
     */
    /**
     * Sample code: ListOperations.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void listOperations(com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### Services_CreateOrUpdate

```java
import com.azure.resourcemanager.servicefabric.models.ArmServicePackageActivationMode;
import com.azure.resourcemanager.servicefabric.models.MoveCost;
import com.azure.resourcemanager.servicefabric.models.ServiceCorrelationDescription;
import com.azure.resourcemanager.servicefabric.models.ServiceCorrelationScheme;
import com.azure.resourcemanager.servicefabric.models.ServiceLoadMetricDescription;
import com.azure.resourcemanager.servicefabric.models.ServiceLoadMetricWeight;
import com.azure.resourcemanager.servicefabric.models.SingletonPartitionSchemeDescription;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Services CreateOrUpdate. */
public final class ServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ServicePutOperation_example_min.json
     */
    /**
     * Sample code: Put a service with minimum parameters.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void putAServiceWithMinimumParameters(
        com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager
            .services()
            .define("myService")
            .withExistingApplication("resRg", "myCluster", "myApp")
            .withTags(mapOf())
            .withServiceTypeName("myServiceType")
            .withPartitionDescription(new SingletonPartitionSchemeDescription())
            .create();
    }

    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ServicePutOperation_example_max.json
     */
    /**
     * Sample code: Put a service with maximum parameters.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void putAServiceWithMaximumParameters(
        com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager
            .services()
            .define("myService")
            .withExistingApplication("resRg", "myCluster", "myApp")
            .withTags(mapOf())
            .withServiceTypeName("myServiceType")
            .withPartitionDescription(new SingletonPartitionSchemeDescription())
            .withServicePackageActivationMode(ArmServicePackageActivationMode.SHARED_PROCESS)
            .withServiceDnsName("my.service.dns")
            .withPlacementConstraints("NodeType==frontend")
            .withCorrelationScheme(
                Arrays
                    .asList(
                        new ServiceCorrelationDescription()
                            .withScheme(ServiceCorrelationScheme.AFFINITY)
                            .withServiceName("fabric:/app1/app1~svc1")))
            .withServiceLoadMetrics(
                Arrays
                    .asList(
                        new ServiceLoadMetricDescription().withName("metric1").withWeight(ServiceLoadMetricWeight.LOW)))
            .withServicePlacementPolicies(Arrays.asList())
            .withDefaultMoveCost(MoveCost.MEDIUM)
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

### Services_Delete

```java
import com.azure.core.util.Context;

/** Samples for Services Delete. */
public final class ServicesDeleteSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ServiceDeleteOperation_example.json
     */
    /**
     * Sample code: Delete a service.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void deleteAService(com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager.services().delete("resRg", "myCluster", "myApp", "myService", Context.NONE);
    }
}
```

### Services_Get

```java
import com.azure.core.util.Context;

/** Samples for Services Get. */
public final class ServicesGetSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ServiceGetOperation_example.json
     */
    /**
     * Sample code: Get a service.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void getAService(com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager.services().getWithResponse("resRg", "myCluster", "myApp", "myService", Context.NONE);
    }
}
```

### Services_List

```java
import com.azure.core.util.Context;

/** Samples for Services List. */
public final class ServicesListSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ServiceListOperation_example.json
     */
    /**
     * Sample code: Get a list of service resources.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void getAListOfServiceResources(
        com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        manager.services().listWithResponse("resRg", "myCluster", "myApp", Context.NONE);
    }
}
```

### Services_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.servicefabric.models.ServiceResource;

/** Samples for Services Update. */
public final class ServicesUpdateSamples {
    /*
     * x-ms-original-file: specification/servicefabric/resource-manager/Microsoft.ServiceFabric/stable/2021-06-01/examples/ServicePatchOperation_example.json
     */
    /**
     * Sample code: Patch a service.
     *
     * @param manager Entry point to ServiceFabricManager.
     */
    public static void patchAService(com.azure.resourcemanager.servicefabric.ServiceFabricManager manager) {
        ServiceResource resource =
            manager.services().getWithResponse("resRg", "myCluster", "myApp", "myService", Context.NONE).getValue();
        resource.update().apply();
    }
}
```

