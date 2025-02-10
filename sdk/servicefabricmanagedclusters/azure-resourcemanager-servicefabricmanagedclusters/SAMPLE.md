# Code snippets and samples


## ApplicationTypeVersions

- [CreateOrUpdate](#applicationtypeversions_createorupdate)
- [Delete](#applicationtypeversions_delete)
- [Get](#applicationtypeversions_get)
- [ListByApplicationTypes](#applicationtypeversions_listbyapplicationtypes)
- [Update](#applicationtypeversions_update)

## ApplicationTypes

- [CreateOrUpdate](#applicationtypes_createorupdate)
- [Delete](#applicationtypes_delete)
- [Get](#applicationtypes_get)
- [List](#applicationtypes_list)
- [Update](#applicationtypes_update)

## Applications

- [CreateOrUpdate](#applications_createorupdate)
- [Delete](#applications_delete)
- [Get](#applications_get)
- [List](#applications_list)
- [ReadUpgrade](#applications_readupgrade)
- [ResumeUpgrade](#applications_resumeupgrade)
- [StartRollback](#applications_startrollback)
- [Update](#applications_update)

## ManagedApplyMaintenanceWindow

- [Post](#managedapplymaintenancewindow_post)

## ManagedAzResiliencyStatuses

- [Get](#managedazresiliencystatuses_get)

## ManagedClusterVersion

- [Get](#managedclusterversion_get)
- [GetByEnvironment](#managedclusterversion_getbyenvironment)
- [List](#managedclusterversion_list)
- [ListByEnvironment](#managedclusterversion_listbyenvironment)

## ManagedClusters

- [CreateOrUpdate](#managedclusters_createorupdate)
- [Delete](#managedclusters_delete)
- [GetByResourceGroup](#managedclusters_getbyresourcegroup)
- [List](#managedclusters_list)
- [ListByResourceGroup](#managedclusters_listbyresourcegroup)
- [Update](#managedclusters_update)

## ManagedMaintenanceWindowStatuses

- [Get](#managedmaintenancewindowstatuses_get)

## ManagedUnsupportedVMSizes

- [Get](#managedunsupportedvmsizes_get)
- [List](#managedunsupportedvmsizes_list)

## NodeTypeSkus

- [List](#nodetypeskus_list)

## NodeTypes

- [CreateOrUpdate](#nodetypes_createorupdate)
- [Delete](#nodetypes_delete)
- [DeleteNode](#nodetypes_deletenode)
- [Get](#nodetypes_get)
- [ListByManagedClusters](#nodetypes_listbymanagedclusters)
- [Reimage](#nodetypes_reimage)
- [Restart](#nodetypes_restart)
- [Update](#nodetypes_update)

## OperationResults

- [Get](#operationresults_get)

## OperationStatus

- [Get](#operationstatus_get)

## Operations

- [List](#operations_list)

## Services

- [CreateOrUpdate](#services_createorupdate)
- [Delete](#services_delete)
- [Get](#services_get)
- [ListByApplications](#services_listbyapplications)
- [Update](#services_update)
### ApplicationTypeVersions_CreateOrUpdate

```java
/**
 * Samples for ApplicationTypeVersions CreateOrUpdate.
 */
public final class ApplicationTypeVersionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ApplicationTypeVersionPutOperation_example.json
     */
    /**
     * Sample code: Put an application type version.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void putAnApplicationTypeVersion(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.applicationTypeVersions()
            .define("1.0")
            .withExistingApplicationType("resRg", "myCluster", "myAppType")
            .withRegion("eastus")
            .withAppPackageUrl("http://fakelink.test.com/MyAppType")
            .create();
    }
}
```

### ApplicationTypeVersions_Delete

```java
/**
 * Samples for ApplicationTypeVersions Delete.
 */
public final class ApplicationTypeVersionsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ApplicationTypeVersionDeleteOperation_example.json
     */
    /**
     * Sample code: Delete an application type version.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void deleteAnApplicationTypeVersion(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.applicationTypeVersions()
            .delete("resRg", "myCluster", "myAppType", "1.0", com.azure.core.util.Context.NONE);
    }
}
```

### ApplicationTypeVersions_Get

```java
/**
 * Samples for ApplicationTypeVersions Get.
 */
public final class ApplicationTypeVersionsGetSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ApplicationTypeVersionGetOperation_example.json
     */
    /**
     * Sample code: Get an application type version.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void getAnApplicationTypeVersion(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.applicationTypeVersions()
            .getWithResponse("resRg", "myCluster", "myAppType", "1.0", com.azure.core.util.Context.NONE);
    }
}
```

### ApplicationTypeVersions_ListByApplicationTypes

```java
/**
 * Samples for ApplicationTypeVersions ListByApplicationTypes.
 */
public final class ApplicationTypeVersionsListByApplicationTypesSa {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ApplicationTypeVersionListOperation_example.json
     */
    /**
     * Sample code: Get a list of application type version resources.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void getAListOfApplicationTypeVersionResources(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.applicationTypeVersions()
            .listByApplicationTypes("resRg", "myCluster", "myAppType", com.azure.core.util.Context.NONE);
    }
}
```

### ApplicationTypeVersions_Update

```java
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ApplicationTypeVersionResource;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ApplicationTypeVersions Update.
 */
public final class ApplicationTypeVersionsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ApplicationTypeVersionPatchOperation_example.json
     */
    /**
     * Sample code: Patch an application type version.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void patchAnApplicationTypeVersion(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        ApplicationTypeVersionResource resource = manager.applicationTypeVersions()
            .getWithResponse("resRg", "myCluster", "myAppType", "1.0", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("a", "b")).apply();
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

### ApplicationTypes_CreateOrUpdate

```java
/**
 * Samples for ApplicationTypes CreateOrUpdate.
 */
public final class ApplicationTypesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ApplicationTypeNamePutOperation_example.json
     */
    /**
     * Sample code: Put an application type.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void putAnApplicationType(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.applicationTypes()
            .define("myAppType")
            .withExistingManagedcluster("resRg", "myCluster")
            .withRegion("eastus")
            .create();
    }
}
```

### ApplicationTypes_Delete

```java
/**
 * Samples for ApplicationTypes Delete.
 */
public final class ApplicationTypesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ApplicationTypeNameDeleteOperation_example.json
     */
    /**
     * Sample code: Delete an application type.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void deleteAnApplicationType(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.applicationTypes().delete("resRg", "myCluster", "myAppType", com.azure.core.util.Context.NONE);
    }
}
```

### ApplicationTypes_Get

```java
/**
 * Samples for ApplicationTypes Get.
 */
public final class ApplicationTypesGetSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ApplicationTypeNameGetOperation_example.json
     */
    /**
     * Sample code: Get an application type.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void getAnApplicationType(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.applicationTypes().getWithResponse("resRg", "myCluster", "myAppType", com.azure.core.util.Context.NONE);
    }
}
```

### ApplicationTypes_List

```java
/**
 * Samples for ApplicationTypes List.
 */
public final class ApplicationTypesListSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ApplicationTypeNameListOperation_example.json
     */
    /**
     * Sample code: Get a list of application type name resources.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void getAListOfApplicationTypeNameResources(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.applicationTypes().list("resRg", "myCluster", com.azure.core.util.Context.NONE);
    }
}
```

### ApplicationTypes_Update

```java
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ApplicationTypeResource;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ApplicationTypes Update.
 */
public final class ApplicationTypesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ApplicationTypeNamePatchOperation_example.json
     */
    /**
     * Sample code: Patch an application type.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void patchAnApplicationType(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        ApplicationTypeResource resource = manager.applicationTypes()
            .getWithResponse("resRg", "myCluster", "myAppType", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("a", "b")).apply();
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

### Applications_CreateOrUpdate

```java
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ApplicationHealthPolicy;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ApplicationUpgradePolicy;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.FailureAction;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.RollingUpgradeMode;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.RollingUpgradeMonitoringPolicy;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ServiceTypeHealthPolicy;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Applications CreateOrUpdate.
 */
public final class ApplicationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ApplicationPutOperation_example_max.json
     */
    /**
     * Sample code: Put an application with maximum parameters.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void putAnApplicationWithMaximumParameters(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.applications()
            .define("myApp")
            .withExistingManagedcluster("resRg", "myCluster")
            .withRegion("eastus")
            .withTags(mapOf("a", "b"))
            .withVersion(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/resRg/providers/Microsoft.ServiceFabric/managedclusters/myCluster/applicationTypes/myAppType/versions/1.0")
            .withParameters(mapOf("param1", "value1"))
            .withUpgradePolicy(new ApplicationUpgradePolicy()
                .withApplicationHealthPolicy(new ApplicationHealthPolicy().withConsiderWarningAsError(true)
                    .withMaxPercentUnhealthyDeployedApplications(0)
                    .withDefaultServiceTypeHealthPolicy(new ServiceTypeHealthPolicy().withMaxPercentUnhealthyServices(0)
                        .withMaxPercentUnhealthyPartitionsPerService(0)
                        .withMaxPercentUnhealthyReplicasPerPartition(0))
                    .withServiceTypeHealthPolicyMap(mapOf("service1",
                        new ServiceTypeHealthPolicy().withMaxPercentUnhealthyServices(30)
                            .withMaxPercentUnhealthyPartitionsPerService(30)
                            .withMaxPercentUnhealthyReplicasPerPartition(30))))
                .withForceRestart(false)
                .withRollingUpgradeMonitoringPolicy(
                    new RollingUpgradeMonitoringPolicy().withFailureAction(FailureAction.ROLLBACK)
                        .withHealthCheckWaitDuration("00:02:00")
                        .withHealthCheckStableDuration("00:05:00")
                        .withHealthCheckRetryTimeout("00:10:00")
                        .withUpgradeTimeout("01:00:00")
                        .withUpgradeDomainTimeout("00:15:00"))
                .withInstanceCloseDelayDuration(600L)
                .withUpgradeMode(RollingUpgradeMode.UNMONITORED_AUTO)
                .withUpgradeReplicaSetCheckTimeout(3600L)
                .withRecreateApplication(false))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ApplicationPutOperation_example_min.json
     */
    /**
     * Sample code: Put an application with minimum parameters.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void putAnApplicationWithMinimumParameters(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.applications()
            .define("myApp")
            .withExistingManagedcluster("resRg", "myCluster")
            .withRegion("eastus")
            .withVersion(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/resRg/providers/Microsoft.ServiceFabric/managedclusters/myCluster/applicationTypes/myAppType/versions/1.0")
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

### Applications_Delete

```java
/**
 * Samples for Applications Delete.
 */
public final class ApplicationsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ApplicationDeleteOperation_example.json
     */
    /**
     * Sample code: Delete an application.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void deleteAnApplication(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.applications().delete("resRg", "myCluster", "myApp", com.azure.core.util.Context.NONE);
    }
}
```

### Applications_Get

```java
/**
 * Samples for Applications Get.
 */
public final class ApplicationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ApplicationGetOperation_example.json
     */
    /**
     * Sample code: Get an application.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void getAnApplication(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.applications().getWithResponse("resRg", "myCluster", "myApp", com.azure.core.util.Context.NONE);
    }
}
```

### Applications_List

```java
/**
 * Samples for Applications List.
 */
public final class ApplicationsListSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ApplicationListOperation_example.json
     */
    /**
     * Sample code: Get a list of application resources.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void getAListOfApplicationResources(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.applications().list("resRg", "myCluster", com.azure.core.util.Context.NONE);
    }
}
```

### Applications_ReadUpgrade

```java
/**
 * Samples for Applications ReadUpgrade.
 */
public final class ApplicationsReadUpgradeSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ApplicationActionGetUpgrade_example.json
     */
    /**
     * Sample code: Get an application upgrade.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void getAnApplicationUpgrade(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.applications().readUpgrade("resRg", "myCluster", "myApp", com.azure.core.util.Context.NONE);
    }
}
```

### Applications_ResumeUpgrade

```java
import com.azure.resourcemanager.servicefabricmanagedclusters.models.RuntimeResumeApplicationUpgradeParameters;

/**
 * Samples for Applications ResumeUpgrade.
 */
public final class ApplicationsResumeUpgradeSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ApplicationActionResumeUpgrade_example.json
     */
    /**
     * Sample code: Resume upgrade.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void resumeUpgrade(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.applications()
            .resumeUpgrade("resRg", "myCluster", "myApp",
                new RuntimeResumeApplicationUpgradeParameters().withUpgradeDomainName("UD1"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Applications_StartRollback

```java
/**
 * Samples for Applications StartRollback.
 */
public final class ApplicationsStartRollbackSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ApplicationActionStartRollback_example.json
     */
    /**
     * Sample code: Start an application upgrade rollback.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void startAnApplicationUpgradeRollback(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.applications().startRollback("resRg", "myCluster", "myApp", com.azure.core.util.Context.NONE);
    }
}
```

### Applications_Update

```java
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ApplicationResource;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Applications Update.
 */
public final class ApplicationsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ApplicationPatchOperation_example.json
     */
    /**
     * Sample code: Patch an application.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void patchAnApplication(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        ApplicationResource resource = manager.applications()
            .getWithResponse("resRg", "myCluster", "myApp", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("a", "b")).apply();
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

### ManagedApplyMaintenanceWindow_Post

```java
/**
 * Samples for ManagedApplyMaintenanceWindow Post.
 */
public final class ManagedApplyMaintenanceWindowPostSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ManagedApplyMaintenanceWindowPost_example.json
     */
    /**
     * Sample code: Maintenance Window Status.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void maintenanceWindowStatus(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.managedApplyMaintenanceWindows()
            .postWithResponse("resourceGroup1", "mycluster1", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedAzResiliencyStatuses_Get

```java
/**
 * Samples for ManagedAzResiliencyStatuses Get.
 */
public final class ManagedAzResiliencyStatusesGetSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * managedAzResiliencyStatusGet_example.json
     */
    /**
     * Sample code: Az Resiliency status of Base Resources.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void azResiliencyStatusOfBaseResources(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.managedAzResiliencyStatuses()
            .getWithResponse("resourceGroup1", "mycluster1", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedClusterVersion_Get

```java
/**
 * Samples for ManagedClusterVersion Get.
 */
public final class ManagedClusterVersionGetSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ManagedClusterVersionGet_example.json
     */
    /**
     * Sample code: Get cluster version.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void getClusterVersion(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.managedClusterVersions().getWithResponse("eastus", "7.2.477.9590", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedClusterVersion_GetByEnvironment

```java
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ManagedClusterVersionEnvironment;

/**
 * Samples for ManagedClusterVersion GetByEnvironment.
 */
public final class ManagedClusterVersionGetByEnvironmentSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ManagedClusterVersionGetByEnvironment_example.json
     */
    /**
     * Sample code: Get cluster version by environment.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void getClusterVersionByEnvironment(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.managedClusterVersions()
            .getByEnvironmentWithResponse("eastus", ManagedClusterVersionEnvironment.WINDOWS, "7.2.477.9590",
                com.azure.core.util.Context.NONE);
    }
}
```

### ManagedClusterVersion_List

```java
/**
 * Samples for ManagedClusterVersion List.
 */
public final class ManagedClusterVersionListSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ManagedClusterVersionList_example.json
     */
    /**
     * Sample code: List cluster versions.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void listClusterVersions(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.managedClusterVersions().listWithResponse("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedClusterVersion_ListByEnvironment

```java
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ManagedClusterVersionEnvironment;

/**
 * Samples for ManagedClusterVersion ListByEnvironment.
 */
public final class ManagedClusterVersionListByEnvironmentSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ManagedClusterVersionListByEnvironment.json
     */
    /**
     * Sample code: List cluster versions by environment.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void listClusterVersionsByEnvironment(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.managedClusterVersions()
            .listByEnvironmentWithResponse("eastus", ManagedClusterVersionEnvironment.WINDOWS,
                com.azure.core.util.Context.NONE);
    }
}
```

### ManagedClusters_CreateOrUpdate

```java
import com.azure.resourcemanager.servicefabricmanagedclusters.models.Access;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ApplicationTypeVersionsCleanupPolicy;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ClusterHealthPolicy;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ClusterMonitoringPolicy;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ClusterUpgradeCadence;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ClusterUpgradeDeltaHealthPolicy;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ClusterUpgradeMode;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ClusterUpgradePolicy;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.Direction;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.IpTag;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.LoadBalancingRule;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ManagedClusterAddOnFeature;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.NetworkSecurityRule;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.NsgProtocol;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.PrivateEndpointNetworkPolicies;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.PrivateLinkServiceNetworkPolicies;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ProbeProtocol;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.Protocol;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ServiceEndpoint;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.SettingsParameterDescription;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.SettingsSectionDescription;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.Sku;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.SkuName;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.Subnet;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ZonalUpdateMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ManagedClusters CreateOrUpdate.
 */
public final class ManagedClustersCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ManagedClusterPutOperation_example_max.json
     */
    /**
     * Sample code: Put a cluster with maximum parameters.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void putAClusterWithMaximumParameters(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.managedClusters()
            .define("myCluster")
            .withRegion("eastus")
            .withExistingResourceGroup("resRg")
            .withSku(new Sku().withName(SkuName.BASIC))
            .withTags(mapOf())
            .withDnsName("myCluster")
            .withClientConnectionPort(19000)
            .withHttpGatewayConnectionPort(19080)
            .withAdminUsername("vmadmin")
            .withAdminPassword("{vm-password}")
            .withLoadBalancingRules(Arrays.asList(
                new LoadBalancingRule().withFrontendPort(80)
                    .withBackendPort(80)
                    .withProtocol(Protocol.fromString("http"))
                    .withProbePort(80)
                    .withProbeProtocol(ProbeProtocol.HTTP),
                new LoadBalancingRule().withFrontendPort(443)
                    .withBackendPort(443)
                    .withProtocol(Protocol.fromString("http"))
                    .withProbePort(443)
                    .withProbeProtocol(ProbeProtocol.HTTP),
                new LoadBalancingRule().withFrontendPort(10000)
                    .withBackendPort(10000)
                    .withProtocol(Protocol.TCP)
                    .withProbePort(10000)
                    .withProbeProtocol(ProbeProtocol.HTTP)
                    .withLoadDistribution("Default")))
            .withAllowRdpAccess(true)
            .withNetworkSecurityRules(Arrays.asList(
                new NetworkSecurityRule().withName("TestName")
                    .withDescription("Test description")
                    .withProtocol(NsgProtocol.TCP)
                    .withSourceAddressPrefixes(Arrays.asList("*"))
                    .withDestinationAddressPrefixes(Arrays.asList("*"))
                    .withSourcePortRanges(Arrays.asList("*"))
                    .withDestinationPortRanges(Arrays.asList("*"))
                    .withAccess(Access.ALLOW)
                    .withPriority(1010)
                    .withDirection(Direction.INBOUND),
                new NetworkSecurityRule().withName("AllowARM")
                    .withProtocol(NsgProtocol.fromString("*"))
                    .withSourceAddressPrefix("AzureResourceManager")
                    .withDestinationAddressPrefix("*")
                    .withSourcePortRange("*")
                    .withDestinationPortRange("33500-33699")
                    .withAccess(Access.ALLOW)
                    .withPriority(2002)
                    .withDirection(Direction.INBOUND)))
            .withFabricSettings(Arrays.asList(new SettingsSectionDescription().withName("ManagedIdentityTokenService")
                .withParameters(
                    Arrays.asList(new SettingsParameterDescription().withName("IsEnabled").withValue("true")))))
            .withClusterCodeVersion("7.1.168.9494")
            .withClusterUpgradeMode(ClusterUpgradeMode.MANUAL)
            .withAddonFeatures(Arrays.asList(ManagedClusterAddOnFeature.DNS_SERVICE,
                ManagedClusterAddOnFeature.BACKUP_RESTORE_SERVICE, ManagedClusterAddOnFeature.RESOURCE_MONITOR_SERVICE))
            .withEnableAutoOSUpgrade(true)
            .withZonalResiliency(true)
            .withApplicationTypeVersionsCleanupPolicy(
                new ApplicationTypeVersionsCleanupPolicy().withMaxUnusedVersionsToKeep(3))
            .withEnableIpv6(true)
            .withIpTags(Arrays.asList(new IpTag().withIpTagType("FirstPartyUsage").withTag("SQL")))
            .withAuxiliarySubnets(Arrays.asList(new Subnet().withName("testSubnet1")
                .withEnableIpv6(true)
                .withPrivateEndpointNetworkPolicies(PrivateEndpointNetworkPolicies.ENABLED)
                .withPrivateLinkServiceNetworkPolicies(PrivateLinkServiceNetworkPolicies.ENABLED)
                .withNetworkSecurityGroupId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/resRg/providers/Microsoft.Network/networkSecurityGroups/sn1")))
            .withServiceEndpoints(Arrays.asList(new ServiceEndpoint().withService("Microsoft.Storage")
                .withLocations(Arrays.asList("eastus2", "usnorth"))))
            .withZonalUpdateMode(ZonalUpdateMode.FAST)
            .withUseCustomVnet(true)
            .withPublicIpPrefixId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/resRg/providers/Microsoft.Network/publicIPPrefixes/myPublicIPPrefix")
            .withPublicIPv6PrefixId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/resRg/providers/Microsoft.Network/publicIPPrefixes/myPublicIPv6Prefix")
            .withDdosProtectionPlanId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/resRg/providers/Microsoft.Network/ddosProtectionPlans/myDDoSProtectionPlan")
            .withUpgradeDescription(new ClusterUpgradePolicy().withForceRestart(false)
                .withHealthPolicy(
                    new ClusterHealthPolicy().withMaxPercentUnhealthyNodes(10).withMaxPercentUnhealthyApplications(30))
                .withDeltaHealthPolicy(new ClusterUpgradeDeltaHealthPolicy().withMaxPercentDeltaUnhealthyNodes(20)
                    .withMaxPercentUpgradeDomainDeltaUnhealthyNodes(40)
                    .withMaxPercentDeltaUnhealthyApplications(40))
                .withMonitoringPolicy(new ClusterMonitoringPolicy().withHealthCheckWaitDuration("00:05:00")
                    .withHealthCheckStableDuration("00:45:00")
                    .withHealthCheckRetryTimeout("00:55:00")
                    .withUpgradeTimeout("12:00:00")
                    .withUpgradeDomainTimeout("03:00:00")))
            .withHttpGatewayTokenAuthConnectionPort(19081)
            .withEnableHttpGatewayExclusiveAuthMode(true)
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ManagedClusterPutOperation_example_min.json
     */
    /**
     * Sample code: Put a cluster with minimum parameters.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void putAClusterWithMinimumParameters(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.managedClusters()
            .define("myCluster")
            .withRegion("eastus")
            .withExistingResourceGroup("resRg")
            .withSku(new Sku().withName(SkuName.BASIC))
            .withDnsName("myCluster")
            .withAdminUsername("vmadmin")
            .withAdminPassword("{vm-password}")
            .withFabricSettings(Arrays.asList(new SettingsSectionDescription().withName("ManagedIdentityTokenService")
                .withParameters(
                    Arrays.asList(new SettingsParameterDescription().withName("IsEnabled").withValue("true")))))
            .withClusterUpgradeMode(ClusterUpgradeMode.AUTOMATIC)
            .withClusterUpgradeCadence(ClusterUpgradeCadence.WAVE1)
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

### ManagedClusters_Delete

```java
/**
 * Samples for ManagedClusters Delete.
 */
public final class ManagedClustersDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ManagedClusterDeleteOperation_example.json
     */
    /**
     * Sample code: Delete a cluster.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void deleteACluster(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.managedClusters().delete("resRg", "myCluster", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedClusters_GetByResourceGroup

```java
/**
 * Samples for ManagedClusters GetByResourceGroup.
 */
public final class ManagedClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ManagedClusterGetOperation_example.json
     */
    /**
     * Sample code: Get a cluster.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void getACluster(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.managedClusters()
            .getByResourceGroupWithResponse("resRg", "myCluster", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedClusters_List

```java
/**
 * Samples for ManagedClusters List.
 */
public final class ManagedClustersListSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ManagedClusterListBySubscriptionOperation_example.json
     */
    /**
     * Sample code: List managed clusters.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void listManagedClusters(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.managedClusters().list(com.azure.core.util.Context.NONE);
    }
}
```

### ManagedClusters_ListByResourceGroup

```java
/**
 * Samples for ManagedClusters ListByResourceGroup.
 */
public final class ManagedClustersListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ManagedClusterListByResourceGroupOperation_example.json
     */
    /**
     * Sample code: List cluster by resource group.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void listClusterByResourceGroup(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.managedClusters().listByResourceGroup("resRg", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedClusters_Update

```java
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ManagedCluster;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ManagedClusters Update.
 */
public final class ManagedClustersUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ManagedClusterPatchOperation_example.json
     */
    /**
     * Sample code: Patch a managed cluster.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void patchAManagedCluster(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        ManagedCluster resource = manager.managedClusters()
            .getByResourceGroupWithResponse("resRg", "myCluster", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("a", "b")).apply();
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

### ManagedMaintenanceWindowStatuses_Get

```java
/**
 * Samples for ManagedMaintenanceWindowStatuses Get.
 */
public final class ManagedMaintenanceWindowStatusesGetSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ManagedMaintenanceWindowStatusGet_example.json
     */
    /**
     * Sample code: Maintenance Window Status.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void maintenanceWindowStatus(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.managedMaintenanceWindowStatuses()
            .getWithResponse("resourceGroup1", "mycluster1", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedUnsupportedVMSizes_Get

```java
/**
 * Samples for ManagedUnsupportedVMSizes Get.
 */
public final class ManagedUnsupportedVMSizesGetSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * managedUnsupportedVMSizesGet_example.json
     */
    /**
     * Sample code: Get unsupported vm sizes.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void getUnsupportedVmSizes(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.managedUnsupportedVMSizes()
            .getWithResponse("eastus", "Standard_B1ls1", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedUnsupportedVMSizes_List

```java
/**
 * Samples for ManagedUnsupportedVMSizes List.
 */
public final class ManagedUnsupportedVMSizesListSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * managedUnsupportedVMSizesList_example.json
     */
    /**
     * Sample code: List unsupported vm sizes.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void listUnsupportedVmSizes(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.managedUnsupportedVMSizes().list("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### NodeTypeSkus_List

```java
/**
 * Samples for NodeTypeSkus List.
 */
public final class NodeTypeSkusListSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * NodeTypeSkusListOperation_example.json
     */
    /**
     * Sample code: List a node type SKUs.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void listANodeTypeSKUs(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.nodeTypeSkus().list("resRg", "myCluster", "BE", com.azure.core.util.Context.NONE);
    }
}
```

### NodeTypes_CreateOrUpdate

```java
import com.azure.core.management.SubResource;
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.AdditionalNetworkInterfaceConfiguration;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.DiskType;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.EvictionPolicyType;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.FrontendConfiguration;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.IpConfiguration;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.IpConfigurationPublicIpAddressConfiguration;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.IpTag;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.PrivateIpAddressVersion;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.PublicIpAddressVersion;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.SecurityType;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.VaultCertificate;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.VaultSecretGroup;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.VmImagePlan;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.VmManagedIdentity;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.VmSetupAction;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.VmssDataDisk;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.VmssExtension;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.VmssExtensionSetupOrder;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NodeTypes CreateOrUpdate.
 */
public final class NodeTypesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * NodeTypePutOperationStateless_example.json
     */
    /**
     * Sample code: Put an stateless node type with temporary disk for service fabric.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void putAnStatelessNodeTypeWithTemporaryDiskForServiceFabric(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager)
        throws IOException {
        manager.nodeTypes()
            .define("BE")
            .withExistingManagedCluster("resRg", "myCluster")
            .withIsPrimary(false)
            .withVmInstanceCount(10)
            .withVmSize("Standard_DS3")
            .withVmImagePublisher("MicrosoftWindowsServer")
            .withVmImageOffer("WindowsServer")
            .withVmImageSku("2016-Datacenter-Server-Core")
            .withVmImageVersion("latest")
            .withVmExtensions(Arrays.asList(new VmssExtension().withName("Microsoft.Azure.Geneva.GenevaMonitoring")
                .withPublisher("Microsoft.Azure.Geneva")
                .withType("GenevaMonitoring")
                .withTypeHandlerVersion("2.0")
                .withAutoUpgradeMinorVersion(true)
                .withSettings(SerializerFactory.createDefaultManagementSerializerAdapter()
                    .deserialize("{}", Object.class, SerializerEncoding.JSON))))
            .withIsStateless(true)
            .withMultiplePlacementGroups(true)
            .withEnableEncryptionAtHost(true)
            .withUseTempDataDisk(true)
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * NodeTypePutOperation_example_max.json
     */
    /**
     * Sample code: Put a node type with maximum parameters.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void putANodeTypeWithMaximumParameters(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager)
        throws IOException {
        manager.nodeTypes()
            .define("BE-testResourceGroup-testRegion-test")
            .withExistingManagedCluster("resRg", "myCluster")
            .withIsPrimary(false)
            .withVmInstanceCount(10)
            .withDataDiskSizeGB(200)
            .withDataDiskType(DiskType.PREMIUM_LRS)
            .withDataDiskLetter("S")
            .withPlacementProperties(mapOf("HasSSD", "true", "NodeColor", "green", "SomeProperty", "5"))
            .withCapacities(mapOf("ClientConnections", "65536"))
            .withVmSize("Standard_DS3")
            .withVmImagePublisher("MicrosoftWindowsServer")
            .withVmImageOffer("WindowsServer")
            .withVmImageSku("2016-Datacenter-Server-Core")
            .withVmImageVersion("latest")
            .withVmSecrets(Arrays.asList(new VaultSecretGroup().withSourceVault(new SubResource().withId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/resRg/providers/Microsoft.KeyVault/vaults/myVault"))
                .withVaultCertificates(Arrays.asList(new VaultCertificate()
                    .withCertificateUrl(
                        "https://myVault.vault.azure.net:443/secrets/myCert/ef1a31d39e1f46bca33def54b6cda54c")
                    .withCertificateStore("My")))))
            .withVmExtensions(Arrays.asList(new VmssExtension().withName("Microsoft.Azure.Geneva.GenevaMonitoring")
                .withPublisher("Microsoft.Azure.Geneva")
                .withType("GenevaMonitoring")
                .withTypeHandlerVersion("2.0")
                .withAutoUpgradeMinorVersion(true)
                .withSettings(SerializerFactory.createDefaultManagementSerializerAdapter()
                    .deserialize("{}", Object.class, SerializerEncoding.JSON))
                .withForceUpdateTag("v.1.0")
                .withEnableAutomaticUpgrade(true)
                .withSetupOrder(Arrays.asList(VmssExtensionSetupOrder.BEFORE_SFRUNTIME))))
            .withVmManagedIdentity(new VmManagedIdentity().withUserAssignedIdentities(Arrays.asList(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/resRg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/myIdentity",
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/resRg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/myIdentity2")))
            .withIsStateless(true)
            .withMultiplePlacementGroups(true)
            .withFrontendConfigurations(Arrays.asList(new FrontendConfiguration().withLoadBalancerBackendAddressPoolId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/resRg/providers/Microsoft.Network/loadBalancers/test-LB/backendAddressPools/LoadBalancerBEAddressPool")
                .withLoadBalancerInboundNatPoolId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/resRg/providers/Microsoft.Network/loadBalancers/test-LB/inboundNatPools/LoadBalancerNATPool")
                .withApplicationGatewayBackendAddressPoolId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/resRg/providers/Microsoft.Network/applicationGateways/appgw-test/backendAddressPools/appgwBepoolTest")))
            .withAdditionalDataDisks(Arrays.asList(
                new VmssDataDisk().withLun(1)
                    .withDiskSizeGB(256)
                    .withDiskType(DiskType.STANDARD_SSD_LRS)
                    .withDiskLetter("F"),
                new VmssDataDisk().withLun(2)
                    .withDiskSizeGB(150)
                    .withDiskType(DiskType.PREMIUM_LRS)
                    .withDiskLetter("G")))
            .withEnableEncryptionAtHost(true)
            .withEnableAcceleratedNetworking(true)
            .withUseDefaultPublicLoadBalancer(true)
            .withEnableOverProvisioning(false)
            .withIsSpotVM(true)
            .withUseEphemeralOSDisk(true)
            .withSpotRestoreTimeout("PT30M")
            .withEvictionPolicy(EvictionPolicyType.DEALLOCATE)
            .withSubnetId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/resRg/providers/Microsoft.Network/virtualNetworks/vnet1/subnets/subnet1")
            .withVmSetupActions(Arrays.asList(VmSetupAction.ENABLE_CONTAINERS, VmSetupAction.ENABLE_HYPERV))
            .withSecurityType(SecurityType.TRUSTED_LAUNCH)
            .withSecureBootEnabled(true)
            .withEnableNodePublicIp(true)
            .withEnableNodePublicIPv6(true)
            .withNatGatewayId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/resRg/providers/Microsoft.Network/natGateways/myNatGateway")
            .withServiceArtifactReferenceId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/resRg/providers/Microsoft.Compute/galleries/myGallery/serviceArtifacts/myServiceArtifact/vmArtifactsProfiles/myVmArtifactProfile")
            .withDscpConfigurationId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/resRg/providers/Microsoft.Network/dscpConfigurations/myDscpConfig")
            .withAdditionalNetworkInterfaceConfigurations(Arrays.asList(new AdditionalNetworkInterfaceConfiguration()
                .withName("nic-1")
                .withEnableAcceleratedNetworking(true)
                .withDscpConfiguration(new SubResource().withId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/resRg/providers/Microsoft.Network/dscpConfigurations/myDscpConfig"))
                .withIpConfigurations(Arrays.asList(new IpConfiguration().withName("ipconfig-1")
                    .withApplicationGatewayBackendAddressPools(Arrays.asList(new SubResource().withId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/resRg/providers/Microsoft.Network/applicationGateways/appgw-test/backendAddressPools/appgwBepoolTest")))
                    .withLoadBalancerBackendAddressPools(Arrays.asList(new SubResource().withId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/resRg/providers/Microsoft.Network/loadBalancers/test-LB/backendAddressPools/LoadBalancerBEAddressPool")))
                    .withLoadBalancerInboundNatPools(Arrays.asList(new SubResource().withId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/resRg/providers/Microsoft.Network/loadBalancers/test-LB/inboundNatPools/LoadBalancerNATPool")))
                    .withSubnet(new SubResource().withId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/resRg/providers/Microsoft.Network/virtualNetworks/vnet1/subnets/subnet1"))
                    .withPrivateIpAddressVersion(PrivateIpAddressVersion.IPV4)
                    .withPublicIpAddressConfiguration(new IpConfigurationPublicIpAddressConfiguration()
                        .withName("publicip-1")
                        .withIpTags(Arrays.asList(new IpTag().withIpTagType("RoutingPreference").withTag("Internet")))
                        .withPublicIpAddressVersion(PublicIpAddressVersion.IPV4))))))
            .withComputerNamePrefix("BE")
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * NodeTypePutOperationAutoScale_example.json
     */
    /**
     * Sample code: Put a node type with auto-scale parameters.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void putANodeTypeWithAutoScaleParameters(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager)
        throws IOException {
        manager.nodeTypes()
            .define("BE")
            .withExistingManagedCluster("resRg", "myCluster")
            .withIsPrimary(false)
            .withVmInstanceCount(-1)
            .withDataDiskSizeGB(200)
            .withDataDiskType(DiskType.PREMIUM_LRS)
            .withPlacementProperties(mapOf("HasSSD", "true", "NodeColor", "green", "SomeProperty", "5"))
            .withCapacities(mapOf("ClientConnections", "65536"))
            .withVmSize("Standard_DS3")
            .withVmImagePublisher("MicrosoftWindowsServer")
            .withVmImageOffer("WindowsServer")
            .withVmImageSku("2016-Datacenter-Server-Core")
            .withVmImageVersion("latest")
            .withVmSecrets(Arrays.asList(new VaultSecretGroup().withSourceVault(new SubResource().withId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/resRg/providers/Microsoft.KeyVault/vaults/myVault"))
                .withVaultCertificates(Arrays.asList(new VaultCertificate()
                    .withCertificateUrl(
                        "https://myVault.vault.azure.net:443/secrets/myCert/ef1a31d39e1f46bca33def54b6cda54c")
                    .withCertificateStore("My")))))
            .withVmExtensions(Arrays.asList(new VmssExtension().withName("Microsoft.Azure.Geneva.GenevaMonitoring")
                .withPublisher("Microsoft.Azure.Geneva")
                .withType("GenevaMonitoring")
                .withTypeHandlerVersion("2.0")
                .withAutoUpgradeMinorVersion(true)
                .withSettings(SerializerFactory.createDefaultManagementSerializerAdapter()
                    .deserialize("{}", Object.class, SerializerEncoding.JSON))))
            .withVmManagedIdentity(new VmManagedIdentity().withUserAssignedIdentities(Arrays.asList(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/resRg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/myIdentity",
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/resRg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/myIdentity2")))
            .withIsStateless(true)
            .withMultiplePlacementGroups(true)
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * NodeTypePutOperation_example_min.json
     */
    /**
     * Sample code: Put a node type with minimum parameters.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void putANodeTypeWithMinimumParameters(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.nodeTypes()
            .define("BE")
            .withExistingManagedCluster("resRg", "myCluster")
            .withIsPrimary(false)
            .withVmInstanceCount(10)
            .withDataDiskSizeGB(200)
            .withVmSize("Standard_D3")
            .withVmImagePublisher("MicrosoftWindowsServer")
            .withVmImageOffer("WindowsServer")
            .withVmImageSku("2016-Datacenter-Server-Core")
            .withVmImageVersion("latest")
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * NodeTypePutOperationDedicatedHost_example.json
     */
    /**
     * Sample code: Put node type with dedicated hosts.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void putNodeTypeWithDedicatedHosts(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.nodeTypes()
            .define("BE")
            .withExistingManagedCluster("resRg", "myCluster")
            .withIsPrimary(false)
            .withVmInstanceCount(10)
            .withDataDiskSizeGB(200)
            .withDataDiskType(DiskType.STANDARD_SSD_LRS)
            .withPlacementProperties(mapOf())
            .withCapacities(mapOf())
            .withVmSize("Standard_D8s_v3")
            .withVmImagePublisher("MicrosoftWindowsServer")
            .withVmImageOffer("WindowsServer")
            .withVmImageSku("2019-Datacenter")
            .withVmImageVersion("latest")
            .withZones(Arrays.asList("1"))
            .withHostGroupId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testhostgroupRG/providers/Microsoft.Compute/hostGroups/testHostGroup")
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * NodeTypePutOperationVmImagePlan_example.json
     */
    /**
     * Sample code: Put node type with vm image plan.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void putNodeTypeWithVmImagePlan(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.nodeTypes()
            .define("BE")
            .withExistingManagedCluster("resRg", "myCluster")
            .withIsPrimary(false)
            .withVmInstanceCount(10)
            .withDataDiskSizeGB(200)
            .withVmSize("Standard_D3")
            .withVmImagePublisher("testpublisher")
            .withVmImageOffer("windows_2022_test")
            .withVmImageSku("win_2022_test_20_10_gen2")
            .withVmImageVersion("latest")
            .withVmImagePlan(new VmImagePlan().withName("win_2022_test_20_10_gen2")
                .withProduct("windows_2022_test")
                .withPublisher("testpublisher"))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * NodeTypePutOperationCustomSharedGalleriesImage_example.json
     */
    /**
     * Sample code: Put node type with shared galleries custom vm image.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void putNodeTypeWithSharedGalleriesCustomVmImage(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.nodeTypes()
            .define("BE")
            .withExistingManagedCluster("resRg", "myCluster")
            .withIsPrimary(false)
            .withVmInstanceCount(10)
            .withDataDiskSizeGB(200)
            .withVmSize("Standard_D3")
            .withVmSharedGalleryImageId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-custom-image/providers/Microsoft.Compute/sharedGalleries/35349201-a0b3-405e-8a23-9f1450984307-SFSHAREDGALLERY/images/TestNoProdContainerDImage/versions/latest")
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * NodeTypePutOperationCustomImage_example.json
     */
    /**
     * Sample code: Put node type with custom vm image.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void putNodeTypeWithCustomVmImage(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.nodeTypes()
            .define("BE")
            .withExistingManagedCluster("resRg", "myCluster")
            .withIsPrimary(false)
            .withVmInstanceCount(10)
            .withDataDiskSizeGB(200)
            .withVmSize("Standard_D3")
            .withVmImageResourceId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-custom-image/providers/Microsoft.Compute/galleries/myCustomImages/images/Win2019DC")
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

### NodeTypes_Delete

```java
/**
 * Samples for NodeTypes Delete.
 */
public final class NodeTypesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * NodeTypeDeleteOperation_example.json
     */
    /**
     * Sample code: Delete a node type.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void deleteANodeType(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.nodeTypes().delete("resRg", "myCluster", "BE", com.azure.core.util.Context.NONE);
    }
}
```

### NodeTypes_DeleteNode

```java
import com.azure.resourcemanager.servicefabricmanagedclusters.models.NodeTypeActionParameters;
import java.util.Arrays;

/**
 * Samples for NodeTypes DeleteNode.
 */
public final class NodeTypesDeleteNodeSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * DeleteNodes_example.json
     */
    /**
     * Sample code: Delete nodes.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void deleteNodes(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.nodeTypes()
            .deleteNode("resRg", "myCluster", "BE",
                new NodeTypeActionParameters().withNodes(Arrays.asList("BE_0", "BE_3")),
                com.azure.core.util.Context.NONE);
    }
}
```

### NodeTypes_Get

```java
/**
 * Samples for NodeTypes Get.
 */
public final class NodeTypesGetSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * NodeTypeGetOperation_example.json
     */
    /**
     * Sample code: Get a node type.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void getANodeType(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.nodeTypes().getWithResponse("resRg", "myCluster", "FE", com.azure.core.util.Context.NONE);
    }
}
```

### NodeTypes_ListByManagedClusters

```java
/**
 * Samples for NodeTypes ListByManagedClusters.
 */
public final class NodeTypesListByManagedClustersSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * NodeTypeListOperation_example.json
     */
    /**
     * Sample code: List node type of the specified managed cluster.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void listNodeTypeOfTheSpecifiedManagedCluster(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.nodeTypes().listByManagedClusters("resRg", "myCluster", com.azure.core.util.Context.NONE);
    }
}
```

### NodeTypes_Reimage

```java
import com.azure.resourcemanager.servicefabricmanagedclusters.models.NodeTypeActionParameters;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.UpdateType;
import java.util.Arrays;

/**
 * Samples for NodeTypes Reimage.
 */
public final class NodeTypesReimageSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ReimageNodes_example.json
     */
    /**
     * Sample code: Reimage nodes.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void reimageNodes(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.nodeTypes()
            .reimage("resRg", "myCluster", "BE",
                new NodeTypeActionParameters().withNodes(Arrays.asList("BE_0", "BE_3")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ReimageNodes_UD_example.json
     */
    /**
     * Sample code: Reimage all nodes By upgrade domain.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void reimageAllNodesByUpgradeDomain(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.nodeTypes()
            .reimage("resRg", "myCluster", "BE",
                new NodeTypeActionParameters().withUpdateType(UpdateType.BY_UPGRADE_DOMAIN),
                com.azure.core.util.Context.NONE);
    }
}
```

### NodeTypes_Restart

```java
import com.azure.resourcemanager.servicefabricmanagedclusters.models.NodeTypeActionParameters;
import java.util.Arrays;

/**
 * Samples for NodeTypes Restart.
 */
public final class NodeTypesRestartSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * RestartNodes_example.json
     */
    /**
     * Sample code: Restart nodes.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void restartNodes(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.nodeTypes()
            .restart("resRg", "myCluster", "BE",
                new NodeTypeActionParameters().withNodes(Arrays.asList("BE_0", "BE_3")),
                com.azure.core.util.Context.NONE);
    }
}
```

### NodeTypes_Update

```java
import com.azure.resourcemanager.servicefabricmanagedclusters.models.NodeType;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.NodeTypeSku;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NodeTypes Update.
 */
public final class NodeTypesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * NodeTypePatchOperation_example.json
     */
    /**
     * Sample code: Patch a node type.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void patchANodeType(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        NodeType resource = manager.nodeTypes()
            .getWithResponse("resRg", "myCluster", "BE", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("a", "b")).apply();
    }

    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * NodeTypePatchOperationAutoScale_example.json
     */
    /**
     * Sample code: Patch a node type while auto-scaling.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void patchANodeTypeWhileAutoScaling(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        NodeType resource = manager.nodeTypes()
            .getWithResponse("resRg", "myCluster", "BE", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("a", "b"))
            .withSku(new NodeTypeSku().withName("Standard_S0").withTier("Standard").withCapacity(10))
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

### OperationResults_Get

```java
/**
 * Samples for OperationResults Get.
 */
public final class OperationResultsGetSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * Long_running_operation_result.json
     */
    /**
     * Sample code: Get operation result.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void getOperationResult(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.operationResults()
            .getWithResponse("eastus", "00000000-0000-0000-0000-000000001234", com.azure.core.util.Context.NONE);
    }
}
```

### OperationStatus_Get

```java
/**
 * Samples for OperationStatus Get.
 */
public final class OperationStatusGetSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * Long_running_operation_status_succeeded.json
     */
    /**
     * Sample code: Get succeeded operation result.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void getSucceededOperationResult(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.operationStatus()
            .getWithResponse("eastus", "00000000-0000-0000-0000-000000001234", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * Long_running_operation_status_failed.json
     */
    /**
     * Sample code: Get failed operation status.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void getFailedOperationStatus(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.operationStatus()
            .getWithResponse("eastus", "00000000-0000-0000-0000-000000001234", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * Operations_example.json
     */
    /**
     * Sample code: List available operations.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void listAvailableOperations(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Services_CreateOrUpdate

```java
import com.azure.resourcemanager.servicefabricmanagedclusters.models.AveragePartitionLoadScalingTrigger;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.MoveCost;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.PartitionInstanceCountScaleMechanism;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ScalingPolicy;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ServiceCorrelation;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ServiceCorrelationScheme;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ServiceLoadMetric;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ServiceLoadMetricWeight;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ServicePackageActivationMode;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ServicePlacementNonPartiallyPlaceServicePolicy;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.SingletonPartitionScheme;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.StatelessServiceProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Services CreateOrUpdate.
 */
public final class ServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ServicePutOperation_example_min.json
     */
    /**
     * Sample code: Put a service with minimum parameters.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void putAServiceWithMinimumParameters(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.services()
            .define("myService")
            .withExistingApplication("resRg", "myCluster", "myApp")
            .withRegion("eastus")
            .withProperties(new StatelessServiceProperties().withServiceTypeName("myServiceType")
                .withPartitionDescription(new SingletonPartitionScheme())
                .withInstanceCount(1))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ServicePutOperation_example_max.json
     */
    /**
     * Sample code: Put a service with maximum parameters.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void putAServiceWithMaximumParameters(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.services()
            .define("myService")
            .withExistingApplication("resRg", "myCluster", "myApp")
            .withRegion("eastus")
            .withTags(mapOf("a", "b"))
            .withProperties(new StatelessServiceProperties().withPlacementConstraints("NodeType==frontend")
                .withCorrelationScheme(Arrays.asList(new ServiceCorrelation()
                    .withScheme(ServiceCorrelationScheme.ALIGNED_AFFINITY)
                    .withServiceName(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/resRg/providers/Microsoft.ServiceFabric/managedclusters/myCluster/applications/myApp/services/myService1")))
                .withServiceLoadMetrics(Arrays.asList(new ServiceLoadMetric().withName("metric1")
                    .withWeight(ServiceLoadMetricWeight.LOW)
                    .withDefaultLoad(3)))
                .withServicePlacementPolicies(Arrays.asList(new ServicePlacementNonPartiallyPlaceServicePolicy()))
                .withDefaultMoveCost(MoveCost.MEDIUM)
                .withScalingPolicies(Arrays.asList(new ScalingPolicy()
                    .withScalingMechanism(new PartitionInstanceCountScaleMechanism().withMinInstanceCount(3)
                        .withMaxInstanceCount(9)
                        .withScaleIncrement(2))
                    .withScalingTrigger(new AveragePartitionLoadScalingTrigger().withMetricName("metricName")
                        .withLowerLoadThreshold(2.0)
                        .withUpperLoadThreshold(8.0)
                        .withScaleInterval("00:01:00"))))
                .withServiceTypeName("myServiceType")
                .withPartitionDescription(new SingletonPartitionScheme())
                .withServicePackageActivationMode(ServicePackageActivationMode.SHARED_PROCESS)
                .withServiceDnsName("myservicednsname.myApp")
                .withInstanceCount(5)
                .withMinInstanceCount(3)
                .withMinInstancePercentage(30))
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

### Services_Delete

```java
/**
 * Samples for Services Delete.
 */
public final class ServicesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ServiceDeleteOperation_example.json
     */
    /**
     * Sample code: Delete a service.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void deleteAService(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.services().delete("resRg", "myCluster", "myApp", "myService", com.azure.core.util.Context.NONE);
    }
}
```

### Services_Get

```java
/**
 * Samples for Services Get.
 */
public final class ServicesGetSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ServiceGetOperation_example.json
     */
    /**
     * Sample code: Get a service.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void getAService(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.services()
            .getWithResponse("resRg", "myCluster", "myApp", "myService", com.azure.core.util.Context.NONE);
    }
}
```

### Services_ListByApplications

```java
/**
 * Samples for Services ListByApplications.
 */
public final class ServicesListByApplicationsSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ServiceListOperation_example.json
     */
    /**
     * Sample code: Get a list of service resources.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void getAListOfServiceResources(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.services().listByApplications("resRg", "myCluster", "myApp", com.azure.core.util.Context.NONE);
    }
}
```

### Services_Update

```java
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ServiceResource;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Services Update.
 */
public final class ServicesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/servicefabricmanagedclusters/resource-manager/Microsoft.ServiceFabric/stable/2024-04-01/examples/
     * ServicePatchOperation_example.json
     */
    /**
     * Sample code: Patch a service.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void patchAService(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        ServiceResource resource = manager.services()
            .getWithResponse("resRg", "myCluster", "myApp", "myService", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("a", "b")).apply();
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

