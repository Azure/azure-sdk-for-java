# Code snippets and samples


## Operations

- [List](#operations_list)

## ResourceProvider

- [SapAvailabilityZoneDetails](#resourceprovider_sapavailabilityzonedetails)
- [SapDiskConfigurations](#resourceprovider_sapdiskconfigurations)
- [SapSizingRecommendations](#resourceprovider_sapsizingrecommendations)
- [SapSupportedSku](#resourceprovider_sapsupportedsku)

## SapApplicationServerInstances

- [Create](#sapapplicationserverinstances_create)
- [Delete](#sapapplicationserverinstances_delete)
- [Get](#sapapplicationserverinstances_get)
- [List](#sapapplicationserverinstances_list)
- [StartInstance](#sapapplicationserverinstances_startinstance)
- [StopInstance](#sapapplicationserverinstances_stopinstance)
- [Update](#sapapplicationserverinstances_update)

## SapCentralInstances

- [Create](#sapcentralinstances_create)
- [Delete](#sapcentralinstances_delete)
- [Get](#sapcentralinstances_get)
- [List](#sapcentralinstances_list)
- [StartInstance](#sapcentralinstances_startinstance)
- [StopInstance](#sapcentralinstances_stopinstance)
- [Update](#sapcentralinstances_update)

## SapDatabaseInstances

- [Create](#sapdatabaseinstances_create)
- [Delete](#sapdatabaseinstances_delete)
- [Get](#sapdatabaseinstances_get)
- [List](#sapdatabaseinstances_list)
- [StartInstance](#sapdatabaseinstances_startinstance)
- [StopInstance](#sapdatabaseinstances_stopinstance)
- [Update](#sapdatabaseinstances_update)

## SapVirtualInstances

- [Create](#sapvirtualinstances_create)
- [Delete](#sapvirtualinstances_delete)
- [GetByResourceGroup](#sapvirtualinstances_getbyresourcegroup)
- [List](#sapvirtualinstances_list)
- [ListByResourceGroup](#sapvirtualinstances_listbyresourcegroup)
- [Start](#sapvirtualinstances_start)
- [Stop](#sapvirtualinstances_stop)
- [Update](#sapvirtualinstances_update)
### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/operations/preview/2023-10-01-preview/examples/
     * Operations_List.json
     */
    /**
     * Sample code: List the operations for the provider.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void listTheOperationsForTheProvider(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_SapAvailabilityZoneDetails

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapAvailabilityZoneDetailsRequest;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapDatabaseType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapProductType;

/**
 * Samples for ResourceProvider SapAvailabilityZoneDetails.
 */
public final class ResourceProviderSapAvailabilityZoneDetailsSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPAvailabilityZoneDetails_northeurope.json
     */
    /**
     * Sample code: SAPAvailabilityZoneDetails_northeurope.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPAvailabilityZoneDetailsNortheurope(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.resourceProviders().sapAvailabilityZoneDetailsWithResponse(
            "centralus", new SapAvailabilityZoneDetailsRequest().withAppLocation("northeurope")
                .withSapProduct(SapProductType.S4HANA).withDatabaseType(SapDatabaseType.HANA),
            com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPAvailabilityZoneDetails_eastus.json
     */
    /**
     * Sample code: SAPAvailabilityZoneDetails_eastus.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPAvailabilityZoneDetailsEastus(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.resourceProviders().sapAvailabilityZoneDetailsWithResponse(
            "centralus", new SapAvailabilityZoneDetailsRequest().withAppLocation("eastus")
                .withSapProduct(SapProductType.S4HANA).withDatabaseType(SapDatabaseType.HANA),
            com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_SapDiskConfigurations

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapDatabaseType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapDeploymentType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapDiskConfigurationsRequest;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapEnvironmentType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapProductType;

/**
 * Samples for ResourceProvider SapDiskConfigurations.
 */
public final class ResourceProviderSapDiskConfigurationsSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPDiskConfigurations_NonProd.json
     */
    /**
     * Sample code: SAPDiskConfigurations_NonProd.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPDiskConfigurationsNonProd(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.resourceProviders().sapDiskConfigurationsWithResponse("centralus",
            new SapDiskConfigurationsRequest().withAppLocation("eastus").withEnvironment(SapEnvironmentType.NON_PROD)
                .withSapProduct(SapProductType.S4HANA).withDatabaseType(SapDatabaseType.HANA)
                .withDeploymentType(SapDeploymentType.THREE_TIER).withDbVmSku("Standard_M32ts"),
            com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPDiskConfigurations_Prod.json
     */
    /**
     * Sample code: SAPDiskConfigurations_Prod.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPDiskConfigurationsProd(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.resourceProviders().sapDiskConfigurationsWithResponse("centralus",
            new SapDiskConfigurationsRequest().withAppLocation("eastus").withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA).withDatabaseType(SapDatabaseType.HANA)
                .withDeploymentType(SapDeploymentType.THREE_TIER).withDbVmSku("Standard_M32ts"),
            com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_SapSizingRecommendations

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapDatabaseScaleMethod;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapDatabaseType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapDeploymentType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapEnvironmentType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapHighAvailabilityType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapProductType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapSizingRecommendationRequest;

/**
 * Samples for ResourceProvider SapSizingRecommendations.
 */
public final class ResourceProviderSapSizingRecommendationsSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPSizingRecommendations_S4HANA_Distributed.json
     */
    /**
     * Sample code: SAPSizingRecommendations_S4HANA_Distributed.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPSizingRecommendationsS4HANADistributed(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.resourceProviders().sapSizingRecommendationsWithResponse("centralus",
            new SapSizingRecommendationRequest().withAppLocation("eastus").withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA).withDeploymentType(SapDeploymentType.THREE_TIER).withSaps(20000L)
                .withDbMemory(1024L).withDatabaseType(SapDatabaseType.HANA)
                .withDbScaleMethod(SapDatabaseScaleMethod.SCALE_UP),
            com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPSizingRecommendations_S4HANA_SingleServer.json
     */
    /**
     * Sample code: SAPSizingRecommendations_S4HANA_SingleServer.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPSizingRecommendationsS4HANASingleServer(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.resourceProviders().sapSizingRecommendationsWithResponse("centralus",
            new SapSizingRecommendationRequest().withAppLocation("eastus").withEnvironment(SapEnvironmentType.NON_PROD)
                .withSapProduct(SapProductType.S4HANA).withDeploymentType(SapDeploymentType.SINGLE_SERVER)
                .withSaps(60000L).withDbMemory(2000L).withDatabaseType(SapDatabaseType.HANA)
                .withDbScaleMethod(SapDatabaseScaleMethod.SCALE_UP),
            com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPSizingRecommendations_S4HANA_HA_AvZone.json
     */
    /**
     * Sample code: SAPSizingRecommendations_S4HANA_DistributedHA_AvZone.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPSizingRecommendationsS4HANADistributedHAAvZone(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.resourceProviders().sapSizingRecommendationsWithResponse("centralus",
            new SapSizingRecommendationRequest().withAppLocation("eastus").withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA).withDeploymentType(SapDeploymentType.THREE_TIER).withSaps(75000L)
                .withDbMemory(1024L).withDatabaseType(SapDatabaseType.HANA)
                .withDbScaleMethod(SapDatabaseScaleMethod.SCALE_UP)
                .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_ZONE),
            com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPSizingRecommendations_S4HANA_HA_AvSet.json
     */
    /**
     * Sample code: SAPSizingRecommendations_S4HANA_DistributedHA_AvSet.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPSizingRecommendationsS4HANADistributedHAAvSet(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.resourceProviders().sapSizingRecommendationsWithResponse("centralus",
            new SapSizingRecommendationRequest().withAppLocation("eastus").withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA).withDeploymentType(SapDeploymentType.THREE_TIER).withSaps(75000L)
                .withDbMemory(1024L).withDatabaseType(SapDatabaseType.HANA)
                .withDbScaleMethod(SapDatabaseScaleMethod.SCALE_UP)
                .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_SET),
            com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_SapSupportedSku

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapDatabaseType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapDeploymentType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapEnvironmentType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapHighAvailabilityType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapProductType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapSupportedSkusRequest;

/**
 * Samples for ResourceProvider SapSupportedSku.
 */
public final class ResourceProviderSapSupportedSkuSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPSupportedSkus_SingleServer.json
     */
    /**
     * Sample code: SAPSupportedSkus_SingleServer.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPSupportedSkusSingleServer(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.resourceProviders().sapSupportedSkuWithResponse("centralus",
            new SapSupportedSkusRequest().withAppLocation("eastus").withEnvironment(SapEnvironmentType.NON_PROD)
                .withSapProduct(SapProductType.S4HANA).withDeploymentType(SapDeploymentType.SINGLE_SERVER)
                .withDatabaseType(SapDatabaseType.HANA),
            com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPSupportedSkus_Distributed.json
     */
    /**
     * Sample code: SAPSupportedSkus_Distributed.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPSupportedSkusDistributed(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.resourceProviders().sapSupportedSkuWithResponse("centralus",
            new SapSupportedSkusRequest().withAppLocation("eastus").withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA).withDeploymentType(SapDeploymentType.THREE_TIER)
                .withDatabaseType(SapDatabaseType.HANA),
            com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPSupportedSkus_DistributedHA_AvZone.json
     */
    /**
     * Sample code: SAPSupportedSkus_DistributedHA_AvZone.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPSupportedSkusDistributedHAAvZone(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.resourceProviders().sapSupportedSkuWithResponse("centralus",
            new SapSupportedSkusRequest().withAppLocation("eastus").withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA).withDeploymentType(SapDeploymentType.THREE_TIER)
                .withDatabaseType(SapDatabaseType.HANA)
                .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_ZONE),
            com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPSupportedSkus_DistributedHA_AvSet.json
     */
    /**
     * Sample code: SAPSupportedSkus_DistributedHA_AvSet.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPSupportedSkusDistributedHAAvSet(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.resourceProviders().sapSupportedSkuWithResponse("centralus",
            new SapSupportedSkusRequest().withAppLocation("eastus").withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA).withDeploymentType(SapDeploymentType.THREE_TIER)
                .withDatabaseType(SapDatabaseType.HANA)
                .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_SET),
            com.azure.core.util.Context.NONE);
    }
}
```

### SapApplicationServerInstances_Create

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapApplicationServerProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SapApplicationServerInstances Create.
 */
public final class SapApplicationServerInstancesCreateSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapapplicationinstances/SAPApplicationServerInstances_Create.json
     */
    /**
     * Sample code: SAPApplicationServerInstances_Create.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPApplicationServerInstancesCreate(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapApplicationServerInstances().define("app01").withRegion("eastus")
            .withExistingSapVirtualInstance("test-rg", "X00").withTags(mapOf())
            .withProperties(new SapApplicationServerProperties()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapapplicationinstances/SAPApplicationServerInstances_Create_HA_AvSet.json
     */
    /**
     * Sample code: Create SAP Application Server Instances for HA System with Availability Set.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createSAPApplicationServerInstancesForHASystemWithAvailabilitySet(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapApplicationServerInstances().define("app01").withRegion("eastus")
            .withExistingSapVirtualInstance("test-rg", "X00").withTags(mapOf())
            .withProperties(new SapApplicationServerProperties()).create();
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

### SapApplicationServerInstances_Delete

```java
/**
 * Samples for SapApplicationServerInstances Delete.
 */
public final class SapApplicationServerInstancesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapapplicationinstances/SAPApplicationServerInstances_Delete.json
     */
    /**
     * Sample code: SAPApplicationServerInstances_Delete.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPApplicationServerInstancesDelete(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapApplicationServerInstances().delete("test-rg", "X00", "app01", com.azure.core.util.Context.NONE);
    }
}
```

### SapApplicationServerInstances_Get

```java
/**
 * Samples for SapApplicationServerInstances Get.
 */
public final class SapApplicationServerInstancesGetSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapapplicationinstances/SAPApplicationServerInstances_Get.json
     */
    /**
     * Sample code: SAPApplicationServerInstances_Get.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPApplicationServerInstancesGet(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapApplicationServerInstances().getWithResponse("test-rg", "X00", "app01",
            com.azure.core.util.Context.NONE);
    }
}
```

### SapApplicationServerInstances_List

```java
/**
 * Samples for SapApplicationServerInstances List.
 */
public final class SapApplicationServerInstancesListSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapapplicationinstances/SAPApplicationServerInstances_List.json
     */
    /**
     * Sample code: SAPApplicationServerInstances_List.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPApplicationServerInstancesList(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapApplicationServerInstances().list("test-rg", "X00", com.azure.core.util.Context.NONE);
    }
}
```

### SapApplicationServerInstances_StartInstance

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.StartRequest;

/**
 * Samples for SapApplicationServerInstances StartInstance.
 */
public final class SapApplicationServerInstancesStartInstanceSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapapplicationinstances/SAPApplicationServerInstances_StartInstance_WithInfraOperations.json
     */
    /**
     * Sample code: Start Virtual Machine and the SAP Application Server Instance on it.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void startVirtualMachineAndTheSAPApplicationServerInstanceOnIt(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapApplicationServerInstances().startInstance("test-rg", "X00", "app01",
            new StartRequest().withStartVm(true), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapapplicationinstances/SAPApplicationServerInstances_StartInstance.json
     */
    /**
     * Sample code: Start the SAP Application Server Instance.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void startTheSAPApplicationServerInstance(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapApplicationServerInstances().startInstance("test-rg", "X00", "app01", null,
            com.azure.core.util.Context.NONE);
    }
}
```

### SapApplicationServerInstances_StopInstance

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.StopRequest;

/**
 * Samples for SapApplicationServerInstances StopInstance.
 */
public final class SapApplicationServerInstancesStopInstanceSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapapplicationinstances/SAPApplicationServerInstances_StopInstance_WithInfraOperations.json
     */
    /**
     * Sample code: Stop the SAP Application Server Instance and the Virtual machine.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void stopTheSAPApplicationServerInstanceAndTheVirtualMachine(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapApplicationServerInstances().stopInstance("test-rg", "X00", "app01",
            new StopRequest().withSoftStopTimeoutSeconds(0L).withDeallocateVm(true), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapapplicationinstances/SAPApplicationServerInstances_StopInstance.json
     */
    /**
     * Sample code: Stop the SAP Application Server Instance.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void stopTheSAPApplicationServerInstance(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapApplicationServerInstances().stopInstance("test-rg", "X00", "app01",
            new StopRequest().withSoftStopTimeoutSeconds(0L), com.azure.core.util.Context.NONE);
    }
}
```

### SapApplicationServerInstances_Update

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapApplicationServerInstance;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SapApplicationServerInstances Update.
 */
public final class SapApplicationServerInstancesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapapplicationinstances/SAPApplicationServerInstances_Update.json
     */
    /**
     * Sample code: SAPApplicationServerInstances_Update.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPApplicationServerInstancesUpdate(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        SapApplicationServerInstance resource = manager.sapApplicationServerInstances()
            .getWithResponse("test-rg", "X00", "app01", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1")).apply();
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

### SapCentralInstances_Create

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapCentralServerProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SapCentralInstances Create.
 */
public final class SapCentralInstancesCreateSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapcentralinstances/SAPCentralInstances_Create_HA_AvSet.json
     */
    /**
     * Sample code: Create SAP Central Instances for HA System with Availability Set.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createSAPCentralInstancesForHASystemWithAvailabilitySet(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapCentralInstances().define("centralServer").withRegion("eastus")
            .withExistingSapVirtualInstance("test-rg", "X00").withTags(mapOf())
            .withProperties(new SapCentralServerProperties()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapcentralinstances/SAPCentralInstances_Create.json
     */
    /**
     * Sample code: SAPCentralInstances_Create.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPCentralInstancesCreate(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapCentralInstances().define("centralServer").withRegion("eastus")
            .withExistingSapVirtualInstance("test-rg", "X00").withTags(mapOf())
            .withProperties(new SapCentralServerProperties()).create();
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

### SapCentralInstances_Delete

```java
/**
 * Samples for SapCentralInstances Delete.
 */
public final class SapCentralInstancesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapcentralinstances/SAPCentralInstances_Delete.json
     */
    /**
     * Sample code: SAPCentralInstances_Delete.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPCentralInstancesDelete(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapCentralInstances().delete("test-rg", "X00", "centralServer", com.azure.core.util.Context.NONE);
    }
}
```

### SapCentralInstances_Get

```java
/**
 * Samples for SapCentralInstances Get.
 */
public final class SapCentralInstancesGetSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapcentralinstances/SAPCentralInstances_Get.json
     */
    /**
     * Sample code: SAPCentralInstances_Get.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPCentralInstancesGet(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapCentralInstances().getWithResponse("test-rg", "X00", "centralServer",
            com.azure.core.util.Context.NONE);
    }
}
```

### SapCentralInstances_List

```java
/**
 * Samples for SapCentralInstances List.
 */
public final class SapCentralInstancesListSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapcentralinstances/SAPCentralInstances_List.json
     */
    /**
     * Sample code: SAPCentralInstances_List.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPCentralInstancesList(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapCentralInstances().list("test-rg", "X00", com.azure.core.util.Context.NONE);
    }
}
```

### SapCentralInstances_StartInstance

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.StartRequest;

/**
 * Samples for SapCentralInstances StartInstance.
 */
public final class SapCentralInstancesStartInstanceSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapcentralinstances/SAPCentralInstances_StartInstance_WithInfraOperations.json
     */
    /**
     * Sample code: Start the virtual machine(s) and the SAP central services instance on it.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void startTheVirtualMachineSAndTheSAPCentralServicesInstanceOnIt(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapCentralInstances().startInstance("test-rg", "X00", "centralServer",
            new StartRequest().withStartVm(true), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapcentralinstances/SAPCentralInstances_StartInstance.json
     */
    /**
     * Sample code: Start the SAP Central Services Instance.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void startTheSAPCentralServicesInstance(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapCentralInstances().startInstance("test-rg", "X00", "centralServer", null,
            com.azure.core.util.Context.NONE);
    }
}
```

### SapCentralInstances_StopInstance

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.StopRequest;

/**
 * Samples for SapCentralInstances StopInstance.
 */
public final class SapCentralInstancesStopInstanceSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapcentralinstances/SAPCentralInstances_StopInstance.json
     */
    /**
     * Sample code: Stop the SAP Central Services Instance.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void stopTheSAPCentralServicesInstance(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapCentralInstances().stopInstance("test-rg", "X00", "centralServer",
            new StopRequest().withSoftStopTimeoutSeconds(1200L), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapcentralinstances/SAPCentralInstances_StopInstance_WithInfraOperations.json
     */
    /**
     * Sample code: Stop the SAP Central Services Instance and its underlying Virtual Machine(s).
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void stopTheSAPCentralServicesInstanceAndItsUnderlyingVirtualMachineS(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapCentralInstances().stopInstance("test-rg", "X00", "centralServer",
            new StopRequest().withDeallocateVm(true), com.azure.core.util.Context.NONE);
    }
}
```

### SapCentralInstances_Update

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapCentralServerInstance;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SapCentralInstances Update.
 */
public final class SapCentralInstancesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapcentralinstances/SAPCentralInstances_Update.json
     */
    /**
     * Sample code: SAPCentralInstances_Update.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPCentralInstancesUpdate(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        SapCentralServerInstance resource = manager.sapCentralInstances()
            .getWithResponse("test-rg", "X00", "centralServer", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1")).apply();
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

### SapDatabaseInstances_Create

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapDatabaseProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SapDatabaseInstances Create.
 */
public final class SapDatabaseInstancesCreateSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapdatabaseinstances/SAPDatabaseInstances_Create_HA_AvSet.json
     */
    /**
     * Sample code: Create SAP Database Instances for HA System with Availability Set.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createSAPDatabaseInstancesForHASystemWithAvailabilitySet(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapDatabaseInstances().define("databaseServer").withRegion("eastus")
            .withExistingSapVirtualInstance("test-rg", "X00").withTags(mapOf())
            .withProperties(new SapDatabaseProperties()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapdatabaseinstances/SAPDatabaseInstances_Create.json
     */
    /**
     * Sample code: SAPDatabaseInstances_Create.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPDatabaseInstancesCreate(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapDatabaseInstances().define("databaseServer").withRegion("eastus")
            .withExistingSapVirtualInstance("test-rg", "X00").withTags(mapOf())
            .withProperties(new SapDatabaseProperties()).create();
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

### SapDatabaseInstances_Delete

```java
/**
 * Samples for SapDatabaseInstances Delete.
 */
public final class SapDatabaseInstancesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapdatabaseinstances/SAPDatabaseInstances_Delete.json
     */
    /**
     * Sample code: SAPDatabaseInstances_Delete.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPDatabaseInstancesDelete(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapDatabaseInstances().delete("test-rg", "X00", "databaseServer", com.azure.core.util.Context.NONE);
    }
}
```

### SapDatabaseInstances_Get

```java
/**
 * Samples for SapDatabaseInstances Get.
 */
public final class SapDatabaseInstancesGetSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapdatabaseinstances/SAPDatabaseInstances_Get.json
     */
    /**
     * Sample code: SAPDatabaseInstances_Get.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPDatabaseInstancesGet(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapDatabaseInstances().getWithResponse("test-rg", "X00", "databaseServer",
            com.azure.core.util.Context.NONE);
    }
}
```

### SapDatabaseInstances_List

```java
/**
 * Samples for SapDatabaseInstances List.
 */
public final class SapDatabaseInstancesListSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapdatabaseinstances/SAPDatabaseInstances_List.json
     */
    /**
     * Sample code: SAPDatabaseInstances_List.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPDatabaseInstancesList(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapDatabaseInstances().list("test-rg", "X00", com.azure.core.util.Context.NONE);
    }
}
```

### SapDatabaseInstances_StartInstance

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.StartRequest;

/**
 * Samples for SapDatabaseInstances StartInstance.
 */
public final class SapDatabaseInstancesStartInstanceSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapdatabaseinstances/SAPDatabaseInstances_StartInstance.json
     */
    /**
     * Sample code: Start the database instance of the SAP system.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void startTheDatabaseInstanceOfTheSAPSystem(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapDatabaseInstances().startInstance("test-rg", "X00", "db0", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapdatabaseinstances/SAPDatabaseInstances_StartInstance_WithInfraOperations.json
     */
    /**
     * Sample code: Start Virtual Machine and the database instance of the SAP system on it.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void startVirtualMachineAndTheDatabaseInstanceOfTheSAPSystemOnIt(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapDatabaseInstances().startInstance("test-rg", "X00", "db0", new StartRequest().withStartVm(true),
            com.azure.core.util.Context.NONE);
    }
}
```

### SapDatabaseInstances_StopInstance

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.StopRequest;

/**
 * Samples for SapDatabaseInstances StopInstance.
 */
public final class SapDatabaseInstancesStopInstanceSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapdatabaseinstances/SAPDatabaseInstances_StopInstance.json
     */
    /**
     * Sample code: Stop the database instance of the SAP system.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void stopTheDatabaseInstanceOfTheSAPSystem(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapDatabaseInstances().stopInstance("test-rg", "X00", "db0",
            new StopRequest().withSoftStopTimeoutSeconds(0L), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapdatabaseinstances/SAPDatabaseInstances_StopInstance_WithInfraOperations.json
     */
    /**
     * Sample code: Stop the database instance of the SAP system and the underlying Virtual Machine(s).
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void stopTheDatabaseInstanceOfTheSAPSystemAndTheUnderlyingVirtualMachineS(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapDatabaseInstances().stopInstance("test-rg", "X00", "db0",
            new StopRequest().withSoftStopTimeoutSeconds(0L).withDeallocateVm(true), com.azure.core.util.Context.NONE);
    }
}
```

### SapDatabaseInstances_Update

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapDatabaseInstance;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SapDatabaseInstances Update.
 */
public final class SapDatabaseInstancesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapdatabaseinstances/SAPDatabaseInstances_Update.json
     */
    /**
     * Sample code: SAPDatabaseInstances_Update.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPDatabaseInstancesUpdate(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        SapDatabaseInstance resource = manager.sapDatabaseInstances()
            .getWithResponse("test-rg", "X00", "databaseServer", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("key1", "fakeTokenPlaceholder")).apply();
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

### SapVirtualInstances_Create

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.ApplicationServerConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.ApplicationServerFullResourceNames;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.CentralServerConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.CentralServerFullResourceNames;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.CreateAndMountFileShareConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.DatabaseConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.DatabaseServerFullResourceNames;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.DeploymentConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.DeploymentWithOSConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.DiscoveryConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.DiskConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.DiskSku;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.DiskSkuName;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.DiskVolumeConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.ExternalInstallationSoftwareConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.HighAvailabilityConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.ImageReference;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.LinuxConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.LoadBalancerResourceNames;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.ManagedRGConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.ManagedResourcesNetworkAccessType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.MountFileShareConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.NetworkConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.NetworkInterfaceResourceNames;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.OSProfile;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.OsSapConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapDatabaseType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapEnvironmentType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapHighAvailabilityType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapInstallWithoutOSConfigSoftwareConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapProductType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapVirtualInstanceProperties;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SharedStorageResourceNames;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SingleServerConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SkipFileShareConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SshConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SshKeyPair;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SshPublicKey;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.StorageConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.ThreeTierConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.ThreeTierFullResourceNames;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.VirtualMachineConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.VirtualMachineResourceNames;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SapVirtualInstances Create.
 */
public final class SapVirtualInstancesCreateSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Install_Distributed.json
     */
    /**
     * Sample code: Install SAP Software on Distributed System.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void installSAPSoftwareOnDistributedSystem(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus2").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup(
                        "{{resourcegrp}}")
                        .withNetworkConfiguration(new NetworkConfiguration().withIsSecondaryIpEnabled(true))
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/app")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E4ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat")
                                    .withOffer("RHEL-SAP-HA").withSku("8.2").withVersion("8.2.2021091201"))
                                .withOsProfile(new OSProfile().withAdminUsername("azureuser")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/app")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E4ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat")
                                    .withOffer("RHEL-SAP-HA").withSku("8.2").withVersion("8.2.2021091201"))
                                .withOsProfile(new OSProfile().withAdminUsername("azureuser")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withDatabaseServer(new DatabaseConfiguration().withSubnetId(
                            "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/app")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_M32ts")
                                .withImageReference(new ImageReference().withPublisher("RedHat")
                                    .withOffer("RHEL-SAP-HA").withSku("8.2").withVersion("8.2.2021091201"))
                                .withOsProfile(new OSProfile().withAdminUsername("azureuser")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L)))
                    .withSoftwareConfiguration(new SapInstallWithoutOSConfigSoftwareConfiguration().withBomUrl(
                        "https://teststorageaccount.blob.core.windows.net/sapbits/sapfiles/boms/S41909SPS03_v0011ms/S41909SPS03_v0011ms.yaml")
                        .withSapBitsStorageAccountId(
                            "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Storage/storageAccounts/teststorageaccount")
                        .withSoftwareVersion("SAP S/4HANA 1909 SPS 03"))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("sap.bpaas.com"))))
            .withTags(mapOf("created by", "azureuser")).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Create_HA_AvZone.json
     */
    /**
     * Sample code: Create Infrastructure only for HA System with Availability Zone.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureOnlyForHASystemWithAvailabilityZone(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E16ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSsh(new SshConfiguration().withPublicKeys(
                                            Arrays.asList(new SshPublicKey().withKeyData("fakeTokenPlaceholder")))))))
                            .withInstanceCount(2L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E32ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSsh(new SshConfiguration().withPublicKeys(
                                            Arrays.asList(new SshPublicKey().withKeyData("fakeTokenPlaceholder")))))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_M32ts")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSsh(new SshConfiguration().withPublicKeys(
                                            Arrays.asList(new SshPublicKey().withKeyData("fakeTokenPlaceholder")))))))
                            .withInstanceCount(2L))
                        .withHighAvailabilityConfig(new HighAvailabilityConfiguration()
                            .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_ZONE)))))
            .withTags(mapOf()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Create_WithOSConfig_Distributed.json
     */
    /**
     * Sample code: Create Infrastructure with OS configuration for Distributed System (Recommended).
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithOSConfigurationForDistributedSystemRecommended(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration().withVmSize(
                                "Standard_E16ds_v4").withImageReference(
                                    new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true).withSshKeyPair(
                                        new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E32ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_M32ts")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L)))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .withTags(mapOf()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_DetectInstallation_SingleServer.json
     */
    /**
     * Sample code: Detect SAP Software Installation on a Single Server System.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void detectSAPSoftwareInstallationOnASingleServerSystem(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.NON_PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new SingleServerConfiguration().withAppResourceGroup("X00-RG")
                        .withNetworkConfiguration(new NetworkConfiguration().withIsSecondaryIpEnabled(true))
                        .withDatabaseType(SapDatabaseType.HANA)
                        .withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                        .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                            .withVmSize("Standard_E32ds_v4")
                            .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP-HA")
                                .withSku("84sapha-gen2").withVersion("latest"))
                            .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                .withOsConfiguration(new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                    .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                        .withPrivateKey("fakeTokenPlaceholder"))))))
                    .withSoftwareConfiguration(new ExternalInstallationSoftwareConfiguration().withCentralServerVmId(
                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Compute/virtualMachines/sapq20scsvm0"))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .withTags(mapOf()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Create_WithOSConfig_WithTrustedAccess.json
     */
    /**
     * Sample code: Create Infrastructure (with OS configuration) with trusted access enabled.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithOSConfigurationWithTrustedAccessEnabled(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withManagedResourcesNetworkAccessType(ManagedResourcesNetworkAccessType.PRIVATE)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E16ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E32ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_M32ts")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L)))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .withTags(mapOf()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Create_DiskDetails_HA_AvSet.json
     */
    /**
     * Sample code: Create Infrastructure with Disk and OS configuration for HA System with Availability Set
     * (Recommended).
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithDiskAndOSConfigurationForHASystemWithAvailabilitySetRecommended(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E16ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E32ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_M32ts")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(
                                2L)
                            .withDiskConfiguration(
                                new DiskConfiguration()
                                    .withDiskVolumeConfigurations(
                                        mapOf("backup",
                                            new DiskVolumeConfiguration()
                                                .withCount(2L).withSizeGB(256L)
                                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                            "hana/data",
                                            new DiskVolumeConfiguration().withCount(4L).withSizeGB(128L)
                                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)),
                                            "hana/log",
                                            new DiskVolumeConfiguration().withCount(3L).withSizeGB(128L)
                                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)),
                                            "hana/shared",
                                            new DiskVolumeConfiguration().withCount(1L).withSizeGB(256L)
                                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                            "os",
                                            new DiskVolumeConfiguration().withCount(1L).withSizeGB(64L)
                                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                            "usr/sap",
                                            new DiskVolumeConfiguration().withCount(1L).withSizeGB(128L)
                                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS))))))
                        .withHighAvailabilityConfig(new HighAvailabilityConfiguration()
                            .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_SET)))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .withTags(mapOf()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Create_Discover_TrustedAccessEnable.json
     */
    /**
     * Sample code: Register with trusted access enabled.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void registerWithTrustedAccessEnabled(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("northeurope").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.NON_PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withManagedResourcesNetworkAccessType(ManagedResourcesNetworkAccessType.PRIVATE)
                .withConfiguration(new DiscoveryConfiguration().withCentralServerVmId(
                    "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Compute/virtualMachines/sapq20scsvm0")))
            .withTags(mapOf("createdby", "abc@microsoft.com", "test", "abc")).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Create_Custom_Image.json
     */
    /**
     * Sample code: Create Infrastructure (with OS configuration) with Azure Compute Gallery Image.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithOSConfigurationWithAzureComputeGalleryImage(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E16ds_v4")
                                .withImageReference(new ImageReference().withId(
                                    "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Compute/galleries/testgallery/images/rhelimagetest/versions/0.0.1"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSsh(new SshConfiguration().withPublicKeys(
                                            Arrays.asList(new SshPublicKey().withKeyData("fakeTokenPlaceholder")))))))
                            .withInstanceCount(2L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E32ds_v4")
                                .withImageReference(new ImageReference().withId(
                                    "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Compute/galleries/testgallery/images/rhelimagetest/versions/0.0.1"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSsh(new SshConfiguration().withPublicKeys(
                                            Arrays.asList(new SshPublicKey().withKeyData("fakeTokenPlaceholder")))))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_M32ts")
                                .withImageReference(new ImageReference().withId(
                                    "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Compute/galleries/testgallery/images/rhelimagetest/versions/0.0.1"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSsh(new SshConfiguration().withPublicKeys(
                                            Arrays.asList(new SshPublicKey().withKeyData("fakeTokenPlaceholder")))))))
                            .withInstanceCount(2L))
                        .withHighAvailabilityConfig(new HighAvailabilityConfiguration()
                            .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_ZONE)))))
            .withTags(mapOf()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Create_HA_AvSet.json
     */
    /**
     * Sample code: Create Infrastructure only for HA System with Availability Set.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureOnlyForHASystemWithAvailabilitySet(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E16ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSsh(new SshConfiguration().withPublicKeys(
                                            Arrays.asList(new SshPublicKey().withKeyData("fakeTokenPlaceholder")))))))
                            .withInstanceCount(2L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E32ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSsh(new SshConfiguration().withPublicKeys(
                                            Arrays.asList(new SshPublicKey().withKeyData("fakeTokenPlaceholder")))))))
                            .withInstanceCount(5L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_M32ts")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSsh(new SshConfiguration().withPublicKeys(
                                            Arrays.asList(new SshPublicKey().withKeyData("fakeTokenPlaceholder")))))))
                            .withInstanceCount(2L))
                        .withHighAvailabilityConfig(new HighAvailabilityConfiguration()
                            .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_SET)))))
            .withTags(mapOf()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_DetectInstallation_HA_AvZone.json
     */
    /**
     * Sample code: Detect SAP Software Installation on an HA System with Availability Zone.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void detectSAPSoftwareInstallationOnAnHASystemWithAvailabilityZone(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration().withVmSize(
                                "Standard_E16ds_v4").withImageReference(
                                    new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP-HA")
                                        .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true).withSshKeyPair(
                                        new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E32ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat")
                                    .withOffer("RHEL-SAP-HA").withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_M32ts")
                                .withImageReference(new ImageReference().withPublisher("RedHat")
                                    .withOffer("RHEL-SAP-HA").withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withHighAvailabilityConfig(new HighAvailabilityConfiguration()
                            .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_ZONE)))
                    .withSoftwareConfiguration(new ExternalInstallationSoftwareConfiguration().withCentralServerVmId(
                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Compute/virtualMachines/sapq20scsvm0"))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .withTags(mapOf()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Create_SingleServer.json
     */
    /**
     * Sample code: Create Infrastructure only for Single Server System.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureOnlyForSingleServerSystem(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.NON_PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new SingleServerConfiguration().withAppResourceGroup("X00-RG")
                        .withNetworkConfiguration(new NetworkConfiguration().withIsSecondaryIpEnabled(true))
                        .withDatabaseType(SapDatabaseType.HANA)
                        .withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                        .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                            .withVmSize("Standard_E32ds_v4")
                            .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                .withSku("84sapha-gen2").withVersion("latest"))
                            .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                .withOsConfiguration(new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                    .withSsh(new SshConfiguration().withPublicKeys(
                                        Arrays.asList(new SshPublicKey().withKeyData("fakeTokenPlaceholder"))))))))))
            .withTags(mapOf()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Create_Distributed_CreateTransport.json
     */
    /**
     * Sample code: Create Infrastructure with a new SAP Transport Directory Fileshare.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithANewSAPTransportDirectoryFileshare(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration().withVmSize(
                                "Standard_E16ds_v4").withImageReference(
                                    new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true).withSshKeyPair(
                                        new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E32ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_M32ts")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withStorageConfiguration(new StorageConfiguration()
                            .withTransportFileShareConfiguration(new CreateAndMountFileShareConfiguration()
                                .withResourceGroup("rgName").withStorageAccountName("storageName"))))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .withTags(mapOf()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Create_Discover.json
     */
    /**
     * Sample code: Register existing SAP system as Virtual Instance for SAP solutions.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void registerExistingSAPSystemAsVirtualInstanceForSAPSolutions(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("northeurope").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.NON_PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DiscoveryConfiguration().withCentralServerVmId(
                    "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Compute/virtualMachines/sapq20scsvm0")))
            .withTags(mapOf("createdby", "abc@microsoft.com", "test", "abc")).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Create_CustomFullResourceNames_Distributed.json
     */
    /**
     * Sample code: Create Infrastructure (with OS configuration) with custom resource names for Distributed System.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithOSConfigurationWithCustomResourceNamesForDistributedSystem(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E16ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E32ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_M32ts")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withCustomResourceNames(new ThreeTierFullResourceNames()
                            .withCentralServer(new CentralServerFullResourceNames().withVirtualMachines(Arrays.asList(
                                new VirtualMachineResourceNames().withVmName("ascsvm").withHostname("ascshostName")
                                    .withNetworkInterfaces(Arrays.asList(
                                        new NetworkInterfaceResourceNames().withNetworkInterfaceName("ascsnic")))
                                    .withOsDiskName("ascsosdisk")
                                    .withDataDiskNames(mapOf("default", Arrays.asList("ascsdisk0"))))))
                            .withApplicationServer(
                                new ApplicationServerFullResourceNames()
                                    .withVirtualMachines(Arrays.asList(
                                        new VirtualMachineResourceNames().withVmName("appvm0")
                                            .withHostname("apphostName0")
                                            .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceResourceNames()
                                                .withNetworkInterfaceName("appnic0")))
                                            .withOsDiskName("app0osdisk")
                                            .withDataDiskNames(mapOf("default", Arrays.asList("app0disk0"))),
                                        new VirtualMachineResourceNames().withVmName("appvm1")
                                            .withHostname("apphostName1")
                                            .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceResourceNames()
                                                .withNetworkInterfaceName("appnic1")))
                                            .withOsDiskName("app1osdisk")
                                            .withDataDiskNames(mapOf("default", Arrays.asList("app1disk0")))))
                                    .withAvailabilitySetName("appAvSet"))
                            .withDatabaseServer(new DatabaseServerFullResourceNames().withVirtualMachines(Arrays
                                .asList(new VirtualMachineResourceNames().withVmName("dbvm").withHostname("dbhostName")
                                    .withNetworkInterfaces(Arrays
                                        .asList(new NetworkInterfaceResourceNames().withNetworkInterfaceName("dbnic")))
                                    .withOsDiskName("dbosdisk")
                                    .withDataDiskNames(mapOf("hanaData", Arrays.asList("hanadata0", "hanadata1"),
                                        "hanaLog", Arrays.asList("hanalog0", "hanalog1", "hanalog2"), "hanaShared",
                                        Arrays.asList("hanashared0", "hanashared1"), "usrSap",
                                        Arrays.asList("usrsap0"))))))
                            .withSharedStorage(
                                new SharedStorageResourceNames().withSharedStorageAccountName("storageacc")
                                    .withSharedStorageAccountPrivateEndPointName("peForxNFS"))))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com")))
                .withManagedResourceGroupConfiguration(
                    new ManagedRGConfiguration().withName("mrg-X00-8e17e36c-42e9-4cd5-a078-7b44883414e0")))
            .withTags(mapOf()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Create_CustomFullResourceNames_SingleServer.json
     */
    /**
     * Sample code: Create Infrastructure (with OS configuration) with custom resource names for Single Server System.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithOSConfigurationWithCustomResourceNamesForSingleServerSystem(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.NON_PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new SingleServerConfiguration().withAppResourceGroup("X00-RG")
                        .withNetworkConfiguration(new NetworkConfiguration().withIsSecondaryIpEnabled(true))
                        .withDatabaseType(SapDatabaseType.HANA)
                        .withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                        .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                            .withVmSize("Standard_E32ds_v4")
                            .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                .withSku("84sapha-gen2").withVersion("latest"))
                            .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                .withOsConfiguration(new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                    .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                        .withPrivateKey("fakeTokenPlaceholder"))))))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com")))
                .withManagedResourceGroupConfiguration(
                    new ManagedRGConfiguration().withName("mrg-X00-8e17e36c-42e9-4cd5-a078-7b44883414e0")))
            .withTags(mapOf()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Create_CustomFullResourceNames_HA_AvZone.json
     */
    /**
     * Sample code: Create Infrastructure (with OS configuration) with custom resource names for HA system with
     * Availability Zone.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithOSConfigurationWithCustomResourceNamesForHASystemWithAvailabilityZone(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E16ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E32ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_M32ts")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withHighAvailabilityConfig(new HighAvailabilityConfiguration()
                            .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_ZONE))
                        .withCustomResourceNames(
                            new ThreeTierFullResourceNames()
                                .withCentralServer(new CentralServerFullResourceNames()
                                    .withVirtualMachines(Arrays.asList(
                                        new VirtualMachineResourceNames()
                                            .withVmName("ascsvm").withHostname("ascshostName")
                                            .withNetworkInterfaces(Arrays
                                                .asList(new NetworkInterfaceResourceNames()
                                                    .withNetworkInterfaceName("ascsnic")))
                                            .withOsDiskName("ascsosdisk"),
                                        new VirtualMachineResourceNames().withVmName("ersvm")
                                            .withHostname("ershostName")
                                            .withNetworkInterfaces(Arrays.asList(
                                                new NetworkInterfaceResourceNames().withNetworkInterfaceName("ersnic")))
                                            .withOsDiskName("ersosdisk")))
                                    .withLoadBalancer(new LoadBalancerResourceNames().withLoadBalancerName("ascslb")
                                        .withFrontendIpConfigurationNames(Arrays.asList("ascsip0", "ersip0"))
                                        .withBackendPoolNames(Arrays.asList("ascsBackendPool"))
                                        .withHealthProbeNames(Arrays.asList("ascsHealthProbe", "ersHealthProbe"))))
                                .withApplicationServer(
                                    new ApplicationServerFullResourceNames().withVirtualMachines(Arrays.asList(
                                        new VirtualMachineResourceNames().withVmName("appvm0")
                                            .withHostname("apphostName0")
                                            .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceResourceNames()
                                                .withNetworkInterfaceName("appnic0")))
                                            .withOsDiskName("app0osdisk")
                                            .withDataDiskNames(mapOf("default", Arrays.asList("app0disk0"))),
                                        new VirtualMachineResourceNames()
                                            .withVmName("appvm1").withHostname("apphostName1")
                                            .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceResourceNames()
                                                .withNetworkInterfaceName("appnic1")))
                                            .withOsDiskName("app1osdisk")
                                            .withDataDiskNames(mapOf("default", Arrays.asList("app1disk0"))))))
                                .withDatabaseServer(new DatabaseServerFullResourceNames()
                                    .withVirtualMachines(Arrays.asList(
                                        new VirtualMachineResourceNames()
                                            .withVmName("dbvmpr").withHostname("dbprhostName")
                                            .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceResourceNames()
                                                .withNetworkInterfaceName("dbprnic")))
                                            .withOsDiskName("dbprosdisk").withDataDiskNames(
                                                mapOf("hanaData", Arrays.asList("hanadatapr0", "hanadatapr1"),
                                                    "hanaLog", Arrays.asList("hanalogpr0", "hanalogpr1", "hanalogpr2"),
                                                    "hanaShared", Arrays.asList("hanasharedpr0", "hanasharedpr1"),
                                                    "usrSap", Arrays.asList("usrsappr0"))),
                                        new VirtualMachineResourceNames().withVmName("dbvmsr")
                                            .withHostname("dbsrhostName")
                                            .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceResourceNames()
                                                .withNetworkInterfaceName("dbsrnic")))
                                            .withOsDiskName("dbsrosdisk").withDataDiskNames(
                                                mapOf("hanaData", Arrays.asList("hanadatasr0", "hanadatasr1"),
                                                    "hanaLog", Arrays.asList("hanalogsr0", "hanalogsr1", "hanalogsr2"),
                                                    "hanaShared", Arrays.asList("hanasharedsr0", "hanasharedsr1"),
                                                    "usrSap", Arrays.asList("usrsapsr0")))))
                                    .withLoadBalancer(new LoadBalancerResourceNames().withLoadBalancerName("dblb")
                                        .withFrontendIpConfigurationNames(Arrays.asList("dbip"))
                                        .withBackendPoolNames(Arrays.asList("dbBackendPool"))
                                        .withHealthProbeNames(Arrays.asList("dbHealthProbe"))))
                                .withSharedStorage(
                                    new SharedStorageResourceNames().withSharedStorageAccountName("storageacc")
                                        .withSharedStorageAccountPrivateEndPointName("peForxNFS"))))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com")))
                .withManagedResourceGroupConfiguration(
                    new ManagedRGConfiguration().withName("mrg-X00-8e17e36c-42e9-4cd5-a078-7b44883414e0")))
            .withTags(mapOf()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Create_Distributed_MountTransport.json
     */
    /**
     * Sample code: Create Infrastructure with an existing SAP Transport Directory Fileshare.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithAnExistingSAPTransportDirectoryFileshare(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration().withVmSize(
                                "Standard_E16ds_v4").withImageReference(
                                    new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true).withSshKeyPair(
                                        new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E32ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_M32ts")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withStorageConfiguration(new StorageConfiguration()
                            .withTransportFileShareConfiguration(new MountFileShareConfiguration().withId(
                                "/subscriptions/49d64d54-e888-4c46-a868-1936802b762c/resourceGroups/testrg/providers/Microsoft.Network/privateEndpoints/endpoint")
                                .withPrivateEndpointId(
                                    "/subscriptions/49d64d54-e888-4c46-a868-1936802b762c/resourceGroups/testrg/providers/Microsoft.Network/privateEndpoints/endpoint"))))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .withTags(mapOf()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Create_CustomFullResourceNames_HA_AvSet.json
     */
    /**
     * Sample code: Create Infrastructure (with OS configuration) with custom resource names for HA System with
     * Availability Set.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithOSConfigurationWithCustomResourceNamesForHASystemWithAvailabilitySet(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E16ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E32ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_M32ts")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withHighAvailabilityConfig(new HighAvailabilityConfiguration()
                            .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_SET))
                        .withCustomResourceNames(
                            new ThreeTierFullResourceNames()
                                .withCentralServer(new CentralServerFullResourceNames()
                                    .withVirtualMachines(Arrays.asList(
                                        new VirtualMachineResourceNames()
                                            .withVmName("ascsvm").withHostname("ascshostName")
                                            .withNetworkInterfaces(Arrays
                                                .asList(new NetworkInterfaceResourceNames()
                                                    .withNetworkInterfaceName("ascsnic")))
                                            .withOsDiskName("ascsosdisk"),
                                        new VirtualMachineResourceNames().withVmName("ersvm")
                                            .withHostname("ershostName")
                                            .withNetworkInterfaces(Arrays.asList(
                                                new NetworkInterfaceResourceNames().withNetworkInterfaceName("ersnic")))
                                            .withOsDiskName("ersosdisk")))
                                    .withAvailabilitySetName("csAvSet")
                                    .withLoadBalancer(new LoadBalancerResourceNames().withLoadBalancerName("ascslb")
                                        .withFrontendIpConfigurationNames(Arrays.asList("ascsip0", "ersip0"))
                                        .withBackendPoolNames(Arrays.asList("ascsBackendPool"))
                                        .withHealthProbeNames(Arrays.asList("ascsHealthProbe", "ersHealthProbe"))))
                                .withApplicationServer(new ApplicationServerFullResourceNames()
                                    .withVirtualMachines(Arrays.asList(
                                        new VirtualMachineResourceNames().withVmName("appvm0")
                                            .withHostname("apphostName0")
                                            .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceResourceNames()
                                                .withNetworkInterfaceName("appnic0")))
                                            .withOsDiskName("app0osdisk")
                                            .withDataDiskNames(mapOf("default", Arrays.asList("app0disk0"))),
                                        new VirtualMachineResourceNames()
                                            .withVmName("appvm1").withHostname("apphostName1")
                                            .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceResourceNames()
                                                .withNetworkInterfaceName("appnic1")))
                                            .withOsDiskName("app1osdisk")
                                            .withDataDiskNames(mapOf("default", Arrays.asList("app1disk0")))))
                                    .withAvailabilitySetName("appAvSet"))
                                .withDatabaseServer(new DatabaseServerFullResourceNames()
                                    .withVirtualMachines(Arrays.asList(
                                        new VirtualMachineResourceNames()
                                            .withVmName("dbvmpr").withHostname("dbprhostName")
                                            .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceResourceNames()
                                                .withNetworkInterfaceName("dbprnic")))
                                            .withOsDiskName("dbprosdisk").withDataDiskNames(
                                                mapOf("hanaData", Arrays.asList("hanadatapr0", "hanadatapr1"),
                                                    "hanaLog", Arrays.asList("hanalogpr0", "hanalogpr1", "hanalogpr2"),
                                                    "hanaShared", Arrays.asList("hanasharedpr0", "hanasharedpr1"),
                                                    "usrSap", Arrays.asList("usrsappr0"))),
                                        new VirtualMachineResourceNames().withVmName("dbvmsr")
                                            .withHostname("dbsrhostName")
                                            .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceResourceNames()
                                                .withNetworkInterfaceName("dbsrnic")))
                                            .withOsDiskName("dbsrosdisk").withDataDiskNames(
                                                mapOf("hanaData", Arrays.asList("hanadatasr0", "hanadatasr1"),
                                                    "hanaLog", Arrays.asList("hanalogsr0", "hanalogsr1", "hanalogsr2"),
                                                    "hanaShared", Arrays.asList("hanasharedsr0", "hanasharedsr1"),
                                                    "usrSap", Arrays.asList("usrsapsr0")))))
                                    .withAvailabilitySetName("dbAvSet")
                                    .withLoadBalancer(new LoadBalancerResourceNames().withLoadBalancerName("dblb")
                                        .withFrontendIpConfigurationNames(Arrays.asList("dbip"))
                                        .withBackendPoolNames(Arrays.asList("dbBackendPool"))
                                        .withHealthProbeNames(Arrays.asList("dbHealthProbe"))))
                                .withSharedStorage(
                                    new SharedStorageResourceNames().withSharedStorageAccountName("storageacc")
                                        .withSharedStorageAccountPrivateEndPointName("peForxNFS"))))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com")))
                .withManagedResourceGroupConfiguration(
                    new ManagedRGConfiguration().withName("mrg-X00-8e17e36c-42e9-4cd5-a078-7b44883414e0")))
            .withTags(mapOf()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Create_WithOSConfig_HA_AvSet.json
     */
    /**
     * Sample code: Create Infrastructure with OS configuration for HA System with Availability Set (Recommended).
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithOSConfigurationForHASystemWithAvailabilitySetRecommended(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration().withVmSize(
                                "Standard_E16ds_v4").withImageReference(
                                    new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true).withSshKeyPair(
                                        new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E32ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_M32ts")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withHighAvailabilityConfig(new HighAvailabilityConfiguration()
                            .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_SET)))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .withTags(mapOf()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Create_DiskDetails_Distributed.json
     */
    /**
     * Sample code: Create Infrastructure with Disk and OS configuration for Distributed System (Recommended).
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithDiskAndOSConfigurationForDistributedSystemRecommended(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E16ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E32ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_M32ts")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(
                                1L)
                            .withDiskConfiguration(
                                new DiskConfiguration()
                                    .withDiskVolumeConfigurations(
                                        mapOf("backup",
                                            new DiskVolumeConfiguration()
                                                .withCount(2L).withSizeGB(256L)
                                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                            "hana/data",
                                            new DiskVolumeConfiguration().withCount(4L).withSizeGB(128L)
                                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)),
                                            "hana/log",
                                            new DiskVolumeConfiguration().withCount(3L).withSizeGB(128L)
                                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)),
                                            "hana/shared",
                                            new DiskVolumeConfiguration().withCount(1L).withSizeGB(256L)
                                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                            "os",
                                            new DiskVolumeConfiguration().withCount(1L).withSizeGB(64L)
                                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                            "usr/sap",
                                            new DiskVolumeConfiguration().withCount(1L).withSizeGB(128L)
                                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)))))))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .withTags(mapOf()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_DetectInstallation_Distributed.json
     */
    /**
     * Sample code: Detect SAP Software Installation on a Distributed System.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void detectSAPSoftwareInstallationOnADistributedSystem(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus2").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup(
                        "{{resourcegrp}}")
                        .withNetworkConfiguration(new NetworkConfiguration().withIsSecondaryIpEnabled(true))
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/app")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E4ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat")
                                    .withOffer("RHEL-SAP-HA").withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("azureuser")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/app")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E4ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat")
                                    .withOffer("RHEL-SAP-HA").withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("azureuser")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withDatabaseServer(new DatabaseConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/app")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_M32ts")
                                .withImageReference(new ImageReference().withPublisher("RedHat")
                                    .withOffer("RHEL-SAP-HA").withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("azureuser")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L)))
                    .withSoftwareConfiguration(new ExternalInstallationSoftwareConfiguration().withCentralServerVmId(
                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Compute/virtualMachines/sapq20scsvm0"))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("sap.bpaas.com"))))
            .withTags(mapOf("created by", "azureuser")).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_DetectInstallation_HA_AvSet.json
     */
    /**
     * Sample code: Detect SAP Software Installation on an HA System with Availability Set.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void detectSAPSoftwareInstallationOnAnHASystemWithAvailabilitySet(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration().withVmSize(
                                "Standard_E16ds_v4").withImageReference(
                                    new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP-HA")
                                        .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true).withSshKeyPair(
                                        new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E32ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat")
                                    .withOffer("RHEL-SAP-HA").withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_M32ts")
                                .withImageReference(new ImageReference().withPublisher("RedHat")
                                    .withOffer("RHEL-SAP-HA").withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withHighAvailabilityConfig(new HighAvailabilityConfiguration()
                            .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_SET)))
                    .withSoftwareConfiguration(new ExternalInstallationSoftwareConfiguration().withCentralServerVmId(
                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Compute/virtualMachines/sapq20scsvm0"))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .withTags(mapOf()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Install_SingleServer.json
     */
    /**
     * Sample code: Install SAP Software on Single Server System.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void installSAPSoftwareOnSingleServerSystem(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus2").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.NON_PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new SingleServerConfiguration().withAppResourceGroup("test-rg")
                        .withSubnetId(
                            "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/testsubnet")
                        .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                            .withVmSize("Standard_E32ds_v4")
                            .withImageReference(new ImageReference().withPublisher("SUSE").withOffer("SLES-SAP")
                                .withSku("12-sp4-gen2").withVersion("2022.02.01"))
                            .withOsProfile(new OSProfile().withAdminUsername("azureappadmin")
                                .withOsConfiguration(new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                    .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                        .withPrivateKey("fakeTokenPlaceholder"))))))
                    .withSoftwareConfiguration(new SapInstallWithoutOSConfigSoftwareConfiguration().withBomUrl(
                        "https://teststorageaccount.blob.core.windows.net/sapbits/sapfiles/boms/S41909SPS03_v0011ms/S41909SPS03_v0011ms.yaml")
                        .withSapBitsStorageAccountId(
                            "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Storage/storageAccounts/teststorageaccount")
                        .withSoftwareVersion("SAP S/4HANA 1909 SPS 03"))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("sap.bpaas.com"))))
            .withTags(mapOf()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Create_Distributed.json
     */
    /**
     * Sample code: Create Infrastructure only for Distributed System.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureOnlyForDistributedSystem(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E16ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSsh(new SshConfiguration().withPublicKeys(
                                            Arrays.asList(new SshPublicKey().withKeyData("fakeTokenPlaceholder")))))))
                            .withInstanceCount(1L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E32ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSsh(new SshConfiguration().withPublicKeys(
                                            Arrays.asList(new SshPublicKey().withKeyData("fakeTokenPlaceholder")))))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_M32ts")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSsh(new SshConfiguration().withPublicKeys(
                                            Arrays.asList(new SshPublicKey().withKeyData("fakeTokenPlaceholder")))))))
                            .withInstanceCount(1L)))))
            .withTags(mapOf()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Create_Discover_CustomMrgStorageAccountName.json
     */
    /**
     * Sample code: Register existing SAP system as Virtual Instance for SAP solutions with optional customizations.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void registerExistingSAPSystemAsVirtualInstanceForSAPSolutionsWithOptionalCustomizations(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("northeurope").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.NON_PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DiscoveryConfiguration().withCentralServerVmId(
                    "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Compute/virtualMachines/sapq20scsvm0")
                    .withManagedRgStorageAccountName("q20saacssgrs")))
            .withTags(mapOf("createdby", "abc@microsoft.com", "test", "abc")).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Create_Distributed_SkipTransport.json
     */
    /**
     * Sample code: Create Infrastructure without a SAP Transport Directory Fileshare.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithoutASAPTransportDirectoryFileshare(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration().withVmSize(
                                "Standard_E16ds_v4").withImageReference(
                                    new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true).withSshKeyPair(
                                        new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E32ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_M32ts")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withStorageConfiguration(new StorageConfiguration()
                            .withTransportFileShareConfiguration(new SkipFileShareConfiguration())))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .withTags(mapOf()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Create_WithOSConfig_SingleServer.json
     */
    /**
     * Sample code: Create Infrastructure with OS configuration for Single Server System (Recommended).
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithOSConfigurationForSingleServerSystemRecommended(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.NON_PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new SingleServerConfiguration().withAppResourceGroup("X00-RG")
                        .withNetworkConfiguration(new NetworkConfiguration().withIsSecondaryIpEnabled(true))
                        .withDatabaseType(SapDatabaseType.HANA)
                        .withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                        .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                            .withVmSize("Standard_E32ds_v4")
                            .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                .withSku("84sapha-gen2").withVersion("latest"))
                            .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                .withOsConfiguration(new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                    .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                        .withPrivateKey("fakeTokenPlaceholder"))))))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .withTags(mapOf()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Create_DiskDetails_HA_AvZone.json
     */
    /**
     * Sample code: Create Infrastructure with Disk and OS configuration for HA System with Availability Zone
     * (Recommended).
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithDiskAndOSConfigurationForHASystemWithAvailabilityZoneRecommended(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E16ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E32ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_M32ts")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(
                                2L)
                            .withDiskConfiguration(
                                new DiskConfiguration()
                                    .withDiskVolumeConfigurations(
                                        mapOf("backup",
                                            new DiskVolumeConfiguration()
                                                .withCount(2L).withSizeGB(256L)
                                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                            "hana/data",
                                            new DiskVolumeConfiguration().withCount(4L).withSizeGB(128L)
                                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)),
                                            "hana/log",
                                            new DiskVolumeConfiguration().withCount(3L).withSizeGB(128L)
                                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)),
                                            "hana/shared",
                                            new DiskVolumeConfiguration().withCount(1L).withSizeGB(256L)
                                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                            "os",
                                            new DiskVolumeConfiguration().withCount(1L).withSizeGB(64L)
                                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                            "usr/sap",
                                            new DiskVolumeConfiguration().withCount(1L).withSizeGB(128L)
                                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS))))))
                        .withHighAvailabilityConfig(new HighAvailabilityConfiguration()
                            .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_ZONE)))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .withTags(mapOf()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Create_WithOSConfig_HA_AvZone.json
     */
    /**
     * Sample code: Create Infrastructure with OS configuration for HA System with Availability Zone (Recommended).
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithOSConfigurationForHASystemWithAvailabilityZoneRecommended(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration().withVmSize(
                                "Standard_E16ds_v4").withImageReference(
                                    new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true).withSshKeyPair(
                                        new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E32ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_M32ts")
                                .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2").withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}").withOsConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withHighAvailabilityConfig(new HighAvailabilityConfiguration()
                            .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_ZONE)))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .withTags(mapOf()).create();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Create_DiskDetails_SingleServer.json
     */
    /**
     * Sample code: Create Infrastructure with Disk and OS configurations for Single Server System (Recommended).
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithDiskAndOSConfigurationsForSingleServerSystemRecommended(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().define("X00").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.NON_PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new SingleServerConfiguration().withAppResourceGroup("X00-RG")
                        .withNetworkConfiguration(new NetworkConfiguration().withIsSecondaryIpEnabled(true))
                        .withDatabaseType(SapDatabaseType.HANA)
                        .withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                        .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                            .withVmSize("Standard_E32ds_v4")
                            .withImageReference(new ImageReference().withPublisher("RedHat").withOffer("RHEL-SAP")
                                .withSku("84sapha-gen2").withVersion("latest"))
                            .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                .withOsConfiguration(new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                    .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                        .withPrivateKey("fakeTokenPlaceholder")))))
                        .withDbDiskConfiguration(new DiskConfiguration().withDiskVolumeConfigurations(mapOf("backup",
                            new DiskVolumeConfiguration().withCount(2L).withSizeGB(256L)
                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                            "hana/data",
                            new DiskVolumeConfiguration().withCount(4L).withSizeGB(128L)
                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)),
                            "hana/log",
                            new DiskVolumeConfiguration().withCount(3L).withSizeGB(128L)
                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)),
                            "hana/shared",
                            new DiskVolumeConfiguration().withCount(1L).withSizeGB(256L)
                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                            "os",
                            new DiskVolumeConfiguration().withCount(1L).withSizeGB(64L)
                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                            "usr/sap",
                            new DiskVolumeConfiguration().withCount(1L).withSizeGB(128L)
                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS))))))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .withTags(mapOf()).create();
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

### SapVirtualInstances_Delete

```java
/**
 * Samples for SapVirtualInstances Delete.
 */
public final class SapVirtualInstancesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Delete.json
     */
    /**
     * Sample code: SAPVirtualInstances_Delete.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPVirtualInstancesDelete(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().delete("test-rg", "X00", com.azure.core.util.Context.NONE);
    }
}
```

### SapVirtualInstances_GetByResourceGroup

```java
/**
 * Samples for SapVirtualInstances GetByResourceGroup.
 */
public final class SapVirtualInstancesGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Get.json
     */
    /**
     * Sample code: SAPVirtualInstances_Get.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPVirtualInstancesGet(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().getByResourceGroupWithResponse("test-rg", "X00",
            com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Get_ACSSInstallationBlocked.json
     */
    /**
     * Sample code: SAPVirtualInstances Get With ACSS Installation Blocked.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPVirtualInstancesGetWithACSSInstallationBlocked(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().getByResourceGroupWithResponse("test-rg", "X00",
            com.azure.core.util.Context.NONE);
    }
}
```

### SapVirtualInstances_List

```java
/**
 * Samples for SapVirtualInstances List.
 */
public final class SapVirtualInstancesListSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_ListBySubscription.json
     */
    /**
     * Sample code: SAPVirtualInstances_ListBySubscription.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPVirtualInstancesListBySubscription(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().list(com.azure.core.util.Context.NONE);
    }
}
```

### SapVirtualInstances_ListByResourceGroup

```java
/**
 * Samples for SapVirtualInstances ListByResourceGroup.
 */
public final class SapVirtualInstancesListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_ListByResourceGroup.json
     */
    /**
     * Sample code: SAPVirtualInstances_ListByResourceGroup.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPVirtualInstancesListByResourceGroup(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### SapVirtualInstances_Start

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.StartRequest;

/**
 * Samples for SapVirtualInstances Start.
 */
public final class SapVirtualInstancesStartSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Start_WithInfraOperations.json
     */
    /**
     * Sample code: Start the virtual machines and the SAP system.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void startTheVirtualMachinesAndTheSAPSystem(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().start("test-rg", "X00", new StartRequest().withStartVm(true),
            com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Start.json
     */
    /**
     * Sample code: Start the SAP system.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void startTheSAPSystem(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().start("test-rg", "X00", null, com.azure.core.util.Context.NONE);
    }
}
```

### SapVirtualInstances_Stop

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.StopRequest;

/**
 * Samples for SapVirtualInstances Stop.
 */
public final class SapVirtualInstancesStopSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Stop_WithInfraOperations.json
     */
    /**
     * Sample code: Stop the virtual machine(s) and the SAP system on it.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void stopTheVirtualMachineSAndTheSAPSystemOnIt(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().stop("test-rg", "X00",
            new StopRequest().withSoftStopTimeoutSeconds(0L).withDeallocateVm(true), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Stop.json
     */
    /**
     * Sample code: SAPVirtualInstances_Stop.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPVirtualInstancesStop(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances().stop("test-rg", "X00", new StopRequest().withSoftStopTimeoutSeconds(0L),
            com.azure.core.util.Context.NONE);
    }
}
```

### SapVirtualInstances_Update

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.ManagedResourcesNetworkAccessType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapVirtualInstance;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.UpdateSapVirtualInstanceProperties;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.UserAssignedServiceIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SapVirtualInstances Update.
 */
public final class SapVirtualInstancesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_Update.json
     */
    /**
     * Sample code: SAPVirtualInstances_Update.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPVirtualInstancesUpdate(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        SapVirtualInstance resource = manager.sapVirtualInstances()
            .getByResourceGroupWithResponse("test-rg", "X00", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withIdentity(new UserAssignedServiceIdentity().withType(ManagedServiceIdentityType.NONE)).apply();
    }

    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPVirtualInstance/preview/2023-10-01-preview/
     * examples/sapvirtualinstances/SAPVirtualInstances_TrustedAccessEnable_Update.json
     */
    /**
     * Sample code: SAPVirtualInstances_TrustedAccessEnable_Update.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPVirtualInstancesTrustedAccessEnableUpdate(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        SapVirtualInstance resource = manager.sapVirtualInstances()
            .getByResourceGroupWithResponse("test-rg", "X00", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withIdentity(new UserAssignedServiceIdentity().withType(ManagedServiceIdentityType.NONE))
            .withProperties(new UpdateSapVirtualInstanceProperties()
                .withManagedResourcesNetworkAccessType(ManagedResourcesNetworkAccessType.PRIVATE))
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

