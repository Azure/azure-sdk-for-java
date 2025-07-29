# Code snippets and samples


## Operations

- [List](#operations_list)

## SapApplicationServerInstances

- [Create](#sapapplicationserverinstances_create)
- [Delete](#sapapplicationserverinstances_delete)
- [Get](#sapapplicationserverinstances_get)
- [List](#sapapplicationserverinstances_list)
- [Start](#sapapplicationserverinstances_start)
- [Stop](#sapapplicationserverinstances_stop)
- [Update](#sapapplicationserverinstances_update)

## SapCentralServerInstances

- [Create](#sapcentralserverinstances_create)
- [Delete](#sapcentralserverinstances_delete)
- [Get](#sapcentralserverinstances_get)
- [List](#sapcentralserverinstances_list)
- [Start](#sapcentralserverinstances_start)
- [Stop](#sapcentralserverinstances_stop)
- [Update](#sapcentralserverinstances_update)

## SapDatabaseInstances

- [Create](#sapdatabaseinstances_create)
- [Delete](#sapdatabaseinstances_delete)
- [Get](#sapdatabaseinstances_get)
- [List](#sapdatabaseinstances_list)
- [Start](#sapdatabaseinstances_start)
- [Stop](#sapdatabaseinstances_stop)
- [Update](#sapdatabaseinstances_update)

## SapVirtualInstances

- [Create](#sapvirtualinstances_create)
- [Delete](#sapvirtualinstances_delete)
- [GetAvailabilityZoneDetails](#sapvirtualinstances_getavailabilityzonedetails)
- [GetByResourceGroup](#sapvirtualinstances_getbyresourcegroup)
- [GetDiskConfigurations](#sapvirtualinstances_getdiskconfigurations)
- [GetSapSupportedSku](#sapvirtualinstances_getsapsupportedsku)
- [GetSizingRecommendations](#sapvirtualinstances_getsizingrecommendations)
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
     * x-ms-original-file: 2024-09-01/Operations_List.json
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
     * x-ms-original-file: 2024-09-01/SapApplicationServerInstances_Create.json
     */
    /**
     * Sample code: SAPApplicationServerInstances_Create.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPApplicationServerInstancesCreate(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapApplicationServerInstances()
            .define("app01")
            .withRegion("westcentralus")
            .withExistingSapVirtualInstance("test-rg", "X00")
            .withTags(mapOf())
            .withProperties(new SapApplicationServerProperties())
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapApplicationServerInstances_CreateForHaWithAvailabilitySet.json
     */
    /**
     * Sample code: Create SAP Application Server Instances for HA System with Availability Set.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createSAPApplicationServerInstancesForHASystemWithAvailabilitySet(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapApplicationServerInstances()
            .define("app01")
            .withRegion("westcentralus")
            .withExistingSapVirtualInstance("test-rg", "X00")
            .withTags(mapOf())
            .withProperties(new SapApplicationServerProperties())
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

### SapApplicationServerInstances_Delete

```java
/**
 * Samples for SapApplicationServerInstances Delete.
 */
public final class SapApplicationServerInstancesDeleteSamples {
    /*
     * x-ms-original-file: 2024-09-01/SapApplicationServerInstances_Delete.json
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
     * x-ms-original-file: 2024-09-01/SapApplicationServerInstances_Get.json
     */
    /**
     * Sample code: SAPApplicationServerInstances_Get.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPApplicationServerInstancesGet(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapApplicationServerInstances()
            .getWithResponse("test-rg", "X00", "app01", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2024-09-01/SapApplicationServerInstances_ListBySapVirtualInstance.json
     */
    /**
     * Sample code: SapApplicationServerInstances List By SAP Virtual Instance.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sapApplicationServerInstancesListBySAPVirtualInstance(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapApplicationServerInstances().list("test-rg", "X00", com.azure.core.util.Context.NONE);
    }
}
```

### SapApplicationServerInstances_Start

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.StartRequest;

/**
 * Samples for SapApplicationServerInstances Start.
 */
public final class SapApplicationServerInstancesStartSamples {
    /*
     * x-ms-original-file: 2024-09-01/SapApplicationServerInstances_StartInstanceVM.json
     */
    /**
     * Sample code: Start Virtual Machine and the SAP Application Server Instance on it.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void startVirtualMachineAndTheSAPApplicationServerInstanceOnIt(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapApplicationServerInstances()
            .start("test-rg", "X00", "app01", new StartRequest().withStartVm(true), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-09-01/SapApplicationServerInstances_StartInstance.json
     */
    /**
     * Sample code: Start the SAP Application Server Instance.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void startTheSAPApplicationServerInstance(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapApplicationServerInstances()
            .start("test-rg", "X00", "app01", new StartRequest(), com.azure.core.util.Context.NONE);
    }
}
```

### SapApplicationServerInstances_Stop

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.StopRequest;

/**
 * Samples for SapApplicationServerInstances Stop.
 */
public final class SapApplicationServerInstancesStopSamples {
    /*
     * x-ms-original-file: 2024-09-01/SapApplicationServerInstances_StopInstanceSoftInfrastructure.json
     */
    /**
     * Sample code: Soft Stop the SAP Application Server Instance and it's infrastructure.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void softStopTheSAPApplicationServerInstanceAndItSInfrastructure(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapApplicationServerInstances()
            .stop("test-rg", "X00", "app01", new StopRequest().withSoftStopTimeoutSeconds(300L).withDeallocateVm(true),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-09-01/SapApplicationServerInstances_StopInstanceInfrastructure.json
     */
    /**
     * Sample code: Stop the SAP Application Server Instance and it's infrastructure.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void stopTheSAPApplicationServerInstanceAndItSInfrastructure(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapApplicationServerInstances()
            .stop("test-rg", "X00", "app01", new StopRequest().withSoftStopTimeoutSeconds(0L).withDeallocateVm(true),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-09-01/SapApplicationServerInstances_StopInstance.json
     */
    /**
     * Sample code: Stop the SAP Application Server Instance.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void stopTheSAPApplicationServerInstance(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapApplicationServerInstances()
            .stop("test-rg", "X00", "app01", new StopRequest().withSoftStopTimeoutSeconds(0L),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-09-01/SapApplicationServerInstances_StopInstanceSoft.json
     */
    /**
     * Sample code: Soft Stop the SAP Application Server Instance.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void softStopTheSAPApplicationServerInstance(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapApplicationServerInstances()
            .stop("test-rg", "X00", "app01", new StopRequest().withSoftStopTimeoutSeconds(300L),
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2024-09-01/SapApplicationServerInstances_Update.json
     */
    /**
     * Sample code: SAPApplicationServerInstances_Update.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPApplicationServerInstancesUpdate(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        SapApplicationServerInstance resource = manager.sapApplicationServerInstances()
            .getWithResponse("test-rg", "X00", "app01", com.azure.core.util.Context.NONE)
            .getValue();
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

### SapCentralServerInstances_Create

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapCentralServerProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SapCentralServerInstances Create.
 */
public final class SapCentralServerInstancesCreateSamples {
    /*
     * x-ms-original-file: 2024-09-01/SapCentralInstances_CreateForHaWithAvailabilitySet.json
     */
    /**
     * Sample code: Create SAP Central Instances for HA System with Availability Set.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createSAPCentralInstancesForHASystemWithAvailabilitySet(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapCentralServerInstances()
            .define("centralServer")
            .withRegion("westcentralus")
            .withExistingSapVirtualInstance("test-rg", "X00")
            .withTags(mapOf())
            .withProperties(new SapCentralServerProperties())
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapCentralInstances_Create.json
     */
    /**
     * Sample code: SapCentralServerInstances_Create.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sapCentralServerInstancesCreate(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapCentralServerInstances()
            .define("centralServer")
            .withRegion("westcentralus")
            .withExistingSapVirtualInstance("test-rg", "X00")
            .withTags(mapOf())
            .withProperties(new SapCentralServerProperties())
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

### SapCentralServerInstances_Delete

```java
/**
 * Samples for SapCentralServerInstances Delete.
 */
public final class SapCentralServerInstancesDeleteSamples {
    /*
     * x-ms-original-file: 2024-09-01/SapCentralInstances_Delete.json
     */
    /**
     * Sample code: SapCentralServerInstances_Delete.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sapCentralServerInstancesDelete(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapCentralServerInstances().delete("test-rg", "X00", "centralServer", com.azure.core.util.Context.NONE);
    }
}
```

### SapCentralServerInstances_Get

```java
/**
 * Samples for SapCentralServerInstances Get.
 */
public final class SapCentralServerInstancesGetSamples {
    /*
     * x-ms-original-file: 2024-09-01/SapCentralInstances_Get.json
     */
    /**
     * Sample code: SapCentralServerInstances_Get.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sapCentralServerInstancesGet(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapCentralServerInstances()
            .getWithResponse("test-rg", "X00", "centralServer", com.azure.core.util.Context.NONE);
    }
}
```

### SapCentralServerInstances_List

```java
/**
 * Samples for SapCentralServerInstances List.
 */
public final class SapCentralServerInstancesListSamples {
    /*
     * x-ms-original-file: 2024-09-01/SapCentralServerInstances_ListBySapVirtualInstance.json
     */
    /**
     * Sample code: SAPCentralInstances List by SAP virtual instance.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPCentralInstancesListBySAPVirtualInstance(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapCentralServerInstances().list("test-rg", "X00", com.azure.core.util.Context.NONE);
    }
}
```

### SapCentralServerInstances_Start

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.StartRequest;

/**
 * Samples for SapCentralServerInstances Start.
 */
public final class SapCentralServerInstancesStartSamples {
    /*
     * x-ms-original-file: 2024-09-01/SapCentralInstances_StartInstanceVM.json
     */
    /**
     * Sample code: Start the virtual machine(s) and the SAP central services instance on it.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void startTheVirtualMachineSAndTheSAPCentralServicesInstanceOnIt(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapCentralServerInstances()
            .start("test-rg", "X00", "centralServer", new StartRequest().withStartVm(true),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-09-01/SapCentralInstances_StartInstance.json
     */
    /**
     * Sample code: Start the SAP Central Services Instance.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void startTheSAPCentralServicesInstance(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapCentralServerInstances()
            .start("test-rg", "X00", "centralServer", new StartRequest(), com.azure.core.util.Context.NONE);
    }
}
```

### SapCentralServerInstances_Stop

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.StopRequest;

/**
 * Samples for SapCentralServerInstances Stop.
 */
public final class SapCentralServerInstancesStopSamples {
    /*
     * x-ms-original-file: 2024-09-01/SapCentralInstances_StopInstance.json
     */
    /**
     * Sample code: Stop the SAP Central Services Instance.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void stopTheSAPCentralServicesInstance(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapCentralServerInstances()
            .stop("test-rg", "X00", "centralServer", new StopRequest().withSoftStopTimeoutSeconds(1200L),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-09-01/SapCentralInstances_StopInstanceVM.json
     */
    /**
     * Sample code: Stop the SAP Central Services Instance and its underlying Virtual Machine(s).
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void stopTheSAPCentralServicesInstanceAndItsUnderlyingVirtualMachineS(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapCentralServerInstances()
            .stop("test-rg", "X00", "centralServer", new StopRequest().withDeallocateVm(true),
                com.azure.core.util.Context.NONE);
    }
}
```

### SapCentralServerInstances_Update

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapCentralServerInstance;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SapCentralServerInstances Update.
 */
public final class SapCentralServerInstancesUpdateSamples {
    /*
     * x-ms-original-file: 2024-09-01/SapCentralInstances_Update.json
     */
    /**
     * Sample code: SapCentralServerInstances_Update.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sapCentralServerInstancesUpdate(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        SapCentralServerInstance resource = manager.sapCentralServerInstances()
            .getWithResponse("test-rg", "X00", "centralServer", com.azure.core.util.Context.NONE)
            .getValue();
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
     * x-ms-original-file: 2024-09-01/SapDatabaseInstances_CreateForHaWithAvailabilitySet.json
     */
    /**
     * Sample code: Create SAP Database Instances for HA System with Availability Set.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createSAPDatabaseInstancesForHASystemWithAvailabilitySet(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapDatabaseInstances()
            .define("databaseServer")
            .withRegion("westcentralus")
            .withExistingSapVirtualInstance("test-rg", "X00")
            .withTags(mapOf())
            .withProperties(new SapDatabaseProperties())
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapDatabaseInstances_Create.json
     */
    /**
     * Sample code: SAPDatabaseInstances_Create.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPDatabaseInstancesCreate(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapDatabaseInstances()
            .define("databaseServer")
            .withRegion("westcentralus")
            .withExistingSapVirtualInstance("test-rg", "X00")
            .withTags(mapOf())
            .withProperties(new SapDatabaseProperties())
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

### SapDatabaseInstances_Delete

```java
/**
 * Samples for SapDatabaseInstances Delete.
 */
public final class SapDatabaseInstancesDeleteSamples {
    /*
     * x-ms-original-file: 2024-09-01/SapDatabaseInstances_Delete.json
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
     * x-ms-original-file: 2024-09-01/SapDatabaseInstances_Get.json
     */
    /**
     * Sample code: SAPDatabaseInstances_Get.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPDatabaseInstancesGet(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapDatabaseInstances()
            .getWithResponse("test-rg", "X00", "databaseServer", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2024-09-01/SapDatabaseInstances_List.json
     */
    /**
     * Sample code: SAPDatabaseInstances list by SAP virtual instance.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPDatabaseInstancesListBySAPVirtualInstance(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapDatabaseInstances().list("test-rg", "X00", com.azure.core.util.Context.NONE);
    }
}
```

### SapDatabaseInstances_Start

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.StartRequest;

/**
 * Samples for SapDatabaseInstances Start.
 */
public final class SapDatabaseInstancesStartSamples {
    /*
     * x-ms-original-file: 2024-09-01/SapDatabaseInstances_StartInstance.json
     */
    /**
     * Sample code: Start the database instance of the SAP system.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void startTheDatabaseInstanceOfTheSAPSystem(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapDatabaseInstances()
            .start("test-rg", "X00", "db0", new StartRequest(), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-09-01/SapDatabaseInstances_StartInstanceVM.json
     */
    /**
     * Sample code: Start Virtual Machine and the database instance of the SAP system on it.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void startVirtualMachineAndTheDatabaseInstanceOfTheSAPSystemOnIt(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapDatabaseInstances()
            .start("test-rg", "X00", "db0", new StartRequest().withStartVm(true), com.azure.core.util.Context.NONE);
    }
}
```

### SapDatabaseInstances_Stop

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.StopRequest;

/**
 * Samples for SapDatabaseInstances Stop.
 */
public final class SapDatabaseInstancesStopSamples {
    /*
     * x-ms-original-file: 2024-09-01/SapDatabaseInstances_StopInstance.json
     */
    /**
     * Sample code: Stop the database instance of the SAP system.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void stopTheDatabaseInstanceOfTheSAPSystem(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapDatabaseInstances()
            .stop("test-rg", "X00", "db0", new StopRequest().withSoftStopTimeoutSeconds(0L),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-09-01/SapDatabaseInstances_StopInstanceSoft.json
     */
    /**
     * Sample code: Soft Stop the database instance of the SAP system.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void softStopTheDatabaseInstanceOfTheSAPSystem(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapDatabaseInstances()
            .stop("test-rg", "X00", "db0", new StopRequest().withSoftStopTimeoutSeconds(300L),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-09-01/SapDatabaseInstances_StopInstanceSoftVM.json
     */
    /**
     * Sample code: Soft Stop the database instance of the SAP system and the underlying Virtual Machine(s).
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void softStopTheDatabaseInstanceOfTheSAPSystemAndTheUnderlyingVirtualMachineS(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapDatabaseInstances()
            .stop("test-rg", "X00", "db0", new StopRequest().withSoftStopTimeoutSeconds(300L).withDeallocateVm(true),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-09-01/SapDatabaseInstances_StopInstanceVM.json
     */
    /**
     * Sample code: Stop the database instance of the SAP system and the underlying Virtual Machine(s).
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void stopTheDatabaseInstanceOfTheSAPSystemAndTheUnderlyingVirtualMachineS(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapDatabaseInstances()
            .stop("test-rg", "X00", "db0", new StopRequest().withSoftStopTimeoutSeconds(0L).withDeallocateVm(true),
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2024-09-01/SapDatabaseInstances_Update.json
     */
    /**
     * Sample code: SAPDatabaseInstances_Update.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPDatabaseInstancesUpdate(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        SapDatabaseInstance resource = manager.sapDatabaseInstances()
            .getWithResponse("test-rg", "X00", "databaseServer", com.azure.core.util.Context.NONE)
            .getValue();
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
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateInstallDS.json
     */
    /**
     * Sample code: Install SAP Software on Distributed System.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void installSAPSoftwareOnDistributedSystem(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("eastus2")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf("created by", "azureuser"))
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration()
                        .withAppResourceGroup("{{resourcegrp}}")
                        .withNetworkConfiguration(new NetworkConfiguration().withIsSecondaryIpEnabled(true))
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/app")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E4ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP-HA")
                                        .withSku("8.2")
                                        .withVersion("8.2.2021091201"))
                                    .withOsProfile(new OSProfile().withAdminUsername("azureuser")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/app")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E4ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP-HA")
                                        .withSku("8.2")
                                        .withVersion("8.2.2021091201"))
                                    .withOsProfile(new OSProfile().withAdminUsername("azureuser")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withDatabaseServer(new DatabaseConfiguration().withSubnetId(
                            "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/app")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_M32ts")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP-HA")
                                        .withSku("8.2")
                                        .withVersion("8.2.2021091201"))
                                    .withOsProfile(
                                        new OSProfile().withAdminUsername("azureuser")
                                            .withOsConfiguration(new LinuxConfiguration()
                                                .withDisablePasswordAuthentication(true)
                                                .withSshKeyPair(new SshKeyPair()
                                                    .withPublicKey("fakeTokenPlaceholder")
                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L)))
                    .withSoftwareConfiguration(new SapInstallWithoutOSConfigSoftwareConfiguration().withBomUrl(
                        "https://teststorageaccount.blob.core.windows.net/sapbits/sapfiles/boms/S41909SPS03_v0011ms/S41909SPS03_v0011ms.yaml")
                        .withSapBitsStorageAccountId(
                            "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Storage/storageAccounts/teststorageaccount")
                        .withSoftwareVersion("SAP S/4HANA 1909 SPS 03"))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("sap.bpaas.com"))))
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateInfraHaAvailabilityZone.json
     */
    /**
     * Sample code: Create Infrastructure only for HA System with Availability Zone.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureOnlyForHASystemWithAvailabilityZone(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E16ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat")
                                    .withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2")
                                    .withVersion("latest"))
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
                                .withImageReference(new ImageReference().withPublisher("RedHat")
                                    .withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2")
                                    .withVersion("latest"))
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
                                .withImageReference(new ImageReference().withPublisher("RedHat")
                                    .withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2")
                                    .withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSsh(new SshConfiguration().withPublicKeys(
                                            Arrays.asList(new SshPublicKey().withKeyData("fakeTokenPlaceholder")))))))
                            .withInstanceCount(2L))
                        .withHighAvailabilityConfig(new HighAvailabilityConfiguration()
                            .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_ZONE)))))
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateInfraOsDSRecommended.json
     */
    /**
     * Sample code: Create Infrastructure with OS configuration for Distributed System (Recommended).
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithOSConfigurationForDistributedSystemRecommended(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E16ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E32ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_M32ts")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(
                                        new OSProfile().withAdminUsername("{your-username}")
                                            .withOsConfiguration(new LinuxConfiguration()
                                                .withDisablePasswordAuthentication(true)
                                                .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L)))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateDetectSingleServer.json
     */
    /**
     * Sample code: Detect SAP Software Installation on a Single Server System.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void detectSAPSoftwareInstallationOnASingleServerSystem(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
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
                            .withImageReference(new ImageReference().withPublisher("RedHat")
                                .withOffer("RHEL-SAP-HA")
                                .withSku("84sapha-gen2")
                                .withVersion("latest"))
                            .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                .withOsConfiguration(new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                    .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                        .withPrivateKey("fakeTokenPlaceholder"))))))
                    .withSoftwareConfiguration(new ExternalInstallationSoftwareConfiguration().withCentralServerVmId(
                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Compute/virtualMachines/sapq20scsvm0"))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateInfraWithOsTrustedAccess.json
     */
    /**
     * Sample code: Create Infrastructure (with OS configuration) with trusted access enabled.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithOSConfigurationWithTrustedAccessEnabled(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withManagedResourcesNetworkAccessType(ManagedResourcesNetworkAccessType.PRIVATE)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E16ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E32ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_M32ts")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(
                                        new OSProfile().withAdminUsername("{your-username}")
                                            .withOsConfiguration(new LinuxConfiguration()
                                                .withDisablePasswordAuthentication(true)
                                                .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L)))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateInfraDiskOsHaAvailabilitySetRecommended.json
     */
    /**
     * Sample code: Create Infrastructure with Disk and OS configuration for HA System with Availability Set
     * (Recommended).
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithDiskAndOSConfigurationForHASystemWithAvailabilitySetRecommended(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E16ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E32ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_M32ts")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(
                                        new OSProfile().withAdminUsername("{your-username}")
                                            .withOsConfiguration(new LinuxConfiguration()
                                                .withDisablePasswordAuthentication(true)
                                                .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L)
                            .withDiskConfiguration(
                                new DiskConfiguration()
                                    .withDiskVolumeConfigurations(
                                        mapOf("backup",
                                            new DiskVolumeConfiguration()
                                                .withCount(2L)
                                                .withSizeGB(256L)
                                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                            "hana/data",
                                            new DiskVolumeConfiguration().withCount(4L)
                                                .withSizeGB(128L)
                                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)),
                                            "hana/log",
                                            new DiskVolumeConfiguration().withCount(3L)
                                                .withSizeGB(128L)
                                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)),
                                            "hana/shared",
                                            new DiskVolumeConfiguration().withCount(1L)
                                                .withSizeGB(256L)
                                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                            "os",
                                            new DiskVolumeConfiguration().withCount(1L)
                                                .withSizeGB(64L)
                                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                            "usr/sap",
                                            new DiskVolumeConfiguration().withCount(1L)
                                                .withSizeGB(128L)
                                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS))))))
                        .withHighAvailabilityConfig(new HighAvailabilityConfiguration()
                            .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_SET)))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateRegisterWithTrustedAccess.json
     */
    /**
     * Sample code: Register with trusted access enabled.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void registerWithTrustedAccessEnabled(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("northeurope")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf("createdby", "abc@microsoft.com", "test", "abc"))
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.NON_PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withManagedResourcesNetworkAccessType(ManagedResourcesNetworkAccessType.PRIVATE)
                .withConfiguration(new DiscoveryConfiguration().withCentralServerVmId(
                    "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Compute/virtualMachines/sapq20scsvm0")))
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateDetectHaAvailabilityZone.json
     */
    /**
     * Sample code: Detect SAP Software Installation on an HA System with Availability Zone.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void detectSAPSoftwareInstallationOnAnHASystemWithAvailabilityZone(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E16ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP-HA")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E32ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP-HA")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_M32ts")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP-HA")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
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
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateInfraHaAvailabilitySet.json
     */
    /**
     * Sample code: Create Infrastructure only for HA System with Availability Set.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureOnlyForHASystemWithAvailabilitySet(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E16ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat")
                                    .withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2")
                                    .withVersion("latest"))
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
                                .withImageReference(new ImageReference().withPublisher("RedHat")
                                    .withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2")
                                    .withVersion("latest"))
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
                                .withImageReference(new ImageReference().withPublisher("RedHat")
                                    .withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2")
                                    .withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSsh(new SshConfiguration().withPublicKeys(
                                            Arrays.asList(new SshPublicKey().withKeyData("fakeTokenPlaceholder")))))))
                            .withInstanceCount(2L))
                        .withHighAvailabilityConfig(new HighAvailabilityConfiguration()
                            .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_SET)))))
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateInfraOsComputeGalleryImage.json
     */
    /**
     * Sample code: Create Infrastructure (with OS configuration) with Azure Compute Gallery Image.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithOSConfigurationWithAzureComputeGalleryImage(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
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
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateInfraSingleServer.json
     */
    /**
     * Sample code: Create Infrastructure only for Single Server System.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureOnlyForSingleServerSystem(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
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
                            .withImageReference(new ImageReference().withPublisher("RedHat")
                                .withOffer("RHEL-SAP")
                                .withSku("84sapha-gen2")
                                .withVersion("latest"))
                            .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                .withOsConfiguration(new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                    .withSsh(new SshConfiguration().withPublicKeys(
                                        Arrays.asList(new SshPublicKey().withKeyData("fakeTokenPlaceholder"))))))))))
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateInfraWithNewFileshare.json
     */
    /**
     * Sample code: Create Infrastructure with a new SAP Transport Directory Fileshare.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithANewSAPTransportDirectoryFileshare(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E16ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E32ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_M32ts")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(
                                        new OSProfile().withAdminUsername("{your-username}")
                                            .withOsConfiguration(new LinuxConfiguration()
                                                .withDisablePasswordAuthentication(true)
                                                .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withStorageConfiguration(new StorageConfiguration().withTransportFileShareConfiguration(
                            new CreateAndMountFileShareConfiguration().withResourceGroup("rgName")
                                .withStorageAccountName("storageName"))))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateRegisterSapSolutions.json
     */
    /**
     * Sample code: Register existing SAP system as Virtual Instance for SAP solutions.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void registerExistingSAPSystemAsVirtualInstanceForSAPSolutions(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("northeurope")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf("createdby", "abc@microsoft.com", "test", "abc"))
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.NON_PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DiscoveryConfiguration().withCentralServerVmId(
                    "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Compute/virtualMachines/sapq20scsvm0")))
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateInfraOsCustomResourceNamesDS.json
     */
    /**
     * Sample code: Create Infrastructure (with OS configuration) with custom resource names for Distributed System.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithOSConfigurationWithCustomResourceNamesForDistributedSystem(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration().withVmSize(
                                "Standard_E16ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat")
                                    .withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2")
                                    .withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration().withDisablePasswordAuthentication(
                                        true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration().withVmSize(
                                "Standard_E32ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat")
                                    .withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2")
                                    .withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration().withDisablePasswordAuthentication(
                                        true)
                                        .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                            .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration().withVmSize(
                                "Standard_M32ts")
                                .withImageReference(new ImageReference().withPublisher("RedHat")
                                    .withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2")
                                    .withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(
                                        new LinuxConfiguration().withDisablePasswordAuthentication(
                                            true)
                                            .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withCustomResourceNames(new ThreeTierFullResourceNames()
                            .withCentralServer(new CentralServerFullResourceNames().withVirtualMachines(
                                Arrays.asList(new VirtualMachineResourceNames().withVmName("ascsvm")
                                    .withHostName("ascshostName")
                                    .withNetworkInterfaces(Arrays.asList(
                                        new NetworkInterfaceResourceNames().withNetworkInterfaceName("ascsnic")))
                                    .withOsDiskName("ascsosdisk")
                                    .withDataDiskNames(mapOf("default", Arrays.asList("ascsdisk0"))))))
                            .withApplicationServer(
                                new ApplicationServerFullResourceNames()
                                    .withVirtualMachines(Arrays.asList(
                                        new VirtualMachineResourceNames().withVmName("appvm0")
                                            .withHostName("apphostName0")
                                            .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceResourceNames()
                                                .withNetworkInterfaceName("appnic0")))
                                            .withOsDiskName("app0osdisk")
                                            .withDataDiskNames(mapOf("default", Arrays.asList("app0disk0"))),
                                        new VirtualMachineResourceNames().withVmName("appvm1")
                                            .withHostName("apphostName1")
                                            .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceResourceNames()
                                                .withNetworkInterfaceName("appnic1")))
                                            .withOsDiskName("app1osdisk")
                                            .withDataDiskNames(mapOf("default", Arrays.asList("app1disk0")))))
                                    .withAvailabilitySetName("appAvSet"))
                            .withDatabaseServer(new DatabaseServerFullResourceNames()
                                .withVirtualMachines(Arrays.asList(new VirtualMachineResourceNames().withVmName("dbvm")
                                    .withHostName("dbhostName")
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
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateInfraOsCustomResourceNamesSingleServer.json
     */
    /**
     * Sample code: Create Infrastructure (with OS configuration) with custom resource names for Single Server System.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithOSConfigurationWithCustomResourceNamesForSingleServerSystem(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
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
                            .withImageReference(new ImageReference().withPublisher("RedHat")
                                .withOffer("RHEL-SAP")
                                .withSku("84sapha-gen2")
                                .withVersion("latest"))
                            .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                .withOsConfiguration(new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                    .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                        .withPrivateKey("fakeTokenPlaceholder"))))))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateInfraOsCustomResourceNamesHaAvailabilityZone.json
     */
    /**
     * Sample code: Create Infrastructure (with OS configuration) with custom resource names for HA system with
     * Availability Zone.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithOSConfigurationWithCustomResourceNamesForHASystemWithAvailabilityZone(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E16ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E32ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_M32ts")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(
                                        new OSProfile().withAdminUsername("{your-username}")
                                            .withOsConfiguration(new LinuxConfiguration()
                                                .withDisablePasswordAuthentication(true)
                                                .withSshKeyPair(new SshKeyPair()
                                                    .withPublicKey("fakeTokenPlaceholder")
                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withHighAvailabilityConfig(new HighAvailabilityConfiguration()
                            .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_ZONE))
                        .withCustomResourceNames(
                            new ThreeTierFullResourceNames()
                                .withCentralServer(new CentralServerFullResourceNames()
                                    .withVirtualMachines(Arrays.asList(
                                        new VirtualMachineResourceNames().withVmName("ascsvm")
                                            .withHostName("ascshostName")
                                            .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceResourceNames()
                                                .withNetworkInterfaceName("ascsnic")))
                                            .withOsDiskName("ascsosdisk"),
                                        new VirtualMachineResourceNames().withVmName("ersvm")
                                            .withHostName("ershostName")
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
                                            .withHostName("apphostName0")
                                            .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceResourceNames()
                                                .withNetworkInterfaceName("appnic0")))
                                            .withOsDiskName("app0osdisk")
                                            .withDataDiskNames(mapOf("default", Arrays.asList("app0disk0"))),
                                        new VirtualMachineResourceNames().withVmName("appvm1")
                                            .withHostName("apphostName1")
                                            .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceResourceNames()
                                                .withNetworkInterfaceName("appnic1")))
                                            .withOsDiskName("app1osdisk")
                                            .withDataDiskNames(mapOf("default", Arrays.asList("app1disk0"))))))
                                .withDatabaseServer(new DatabaseServerFullResourceNames()
                                    .withVirtualMachines(Arrays.asList(
                                        new VirtualMachineResourceNames().withVmName("dbvmpr")
                                            .withHostName("dbprhostName")
                                            .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceResourceNames()
                                                .withNetworkInterfaceName("dbprnic")))
                                            .withOsDiskName("dbprosdisk")
                                            .withDataDiskNames(mapOf(
                                                "hanaData", Arrays.asList("hanadatapr0", "hanadatapr1"), "hanaLog",
                                                Arrays.asList("hanalogpr0", "hanalogpr1", "hanalogpr2"), "hanaShared",
                                                Arrays.asList("hanasharedpr0", "hanasharedpr1"), "usrSap",
                                                Arrays.asList("usrsappr0"))),
                                        new VirtualMachineResourceNames().withVmName("dbvmsr")
                                            .withHostName("dbsrhostName")
                                            .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceResourceNames()
                                                .withNetworkInterfaceName("dbsrnic")))
                                            .withOsDiskName("dbsrosdisk")
                                            .withDataDiskNames(
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
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateInfraWithExistingFileshare.json
     */
    /**
     * Sample code: Create Infrastructure with an existing SAP Transport Directory Fileshare.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithAnExistingSAPTransportDirectoryFileshare(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E16ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E32ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_M32ts")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
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
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateInfraOsCustomResourceNamesHaAvailabilitySet.json
     */
    /**
     * Sample code: Create Infrastructure (with OS configuration) with custom resource names for HA System with
     * Availability Set.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithOSConfigurationWithCustomResourceNamesForHASystemWithAvailabilitySet(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E16ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E32ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_M32ts")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(
                                        new OSProfile().withAdminUsername("{your-username}")
                                            .withOsConfiguration(new LinuxConfiguration()
                                                .withDisablePasswordAuthentication(true)
                                                .withSshKeyPair(new SshKeyPair()
                                                    .withPublicKey("fakeTokenPlaceholder")
                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withHighAvailabilityConfig(new HighAvailabilityConfiguration()
                            .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_SET))
                        .withCustomResourceNames(
                            new ThreeTierFullResourceNames()
                                .withCentralServer(new CentralServerFullResourceNames()
                                    .withVirtualMachines(Arrays.asList(
                                        new VirtualMachineResourceNames().withVmName("ascsvm")
                                            .withHostName("ascshostName")
                                            .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceResourceNames()
                                                .withNetworkInterfaceName("ascsnic")))
                                            .withOsDiskName("ascsosdisk"),
                                        new VirtualMachineResourceNames().withVmName("ersvm")
                                            .withHostName("ershostName")
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
                                            .withHostName("apphostName0")
                                            .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceResourceNames()
                                                .withNetworkInterfaceName("appnic0")))
                                            .withOsDiskName("app0osdisk")
                                            .withDataDiskNames(mapOf("default", Arrays.asList("app0disk0"))),
                                        new VirtualMachineResourceNames().withVmName("appvm1")
                                            .withHostName("apphostName1")
                                            .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceResourceNames()
                                                .withNetworkInterfaceName("appnic1")))
                                            .withOsDiskName("app1osdisk")
                                            .withDataDiskNames(mapOf("default", Arrays.asList("app1disk0")))))
                                    .withAvailabilitySetName("appAvSet"))
                                .withDatabaseServer(new DatabaseServerFullResourceNames()
                                    .withVirtualMachines(Arrays.asList(
                                        new VirtualMachineResourceNames().withVmName("dbvmpr")
                                            .withHostName("dbprhostName")
                                            .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceResourceNames()
                                                .withNetworkInterfaceName("dbprnic")))
                                            .withOsDiskName("dbprosdisk")
                                            .withDataDiskNames(mapOf(
                                                "hanaData", Arrays.asList("hanadatapr0", "hanadatapr1"), "hanaLog",
                                                Arrays.asList("hanalogpr0", "hanalogpr1", "hanalogpr2"), "hanaShared",
                                                Arrays.asList("hanasharedpr0", "hanasharedpr1"), "usrSap",
                                                Arrays.asList("usrsappr0"))),
                                        new VirtualMachineResourceNames().withVmName("dbvmsr")
                                            .withHostName("dbsrhostName")
                                            .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceResourceNames()
                                                .withNetworkInterfaceName("dbsrnic")))
                                            .withOsDiskName("dbsrosdisk")
                                            .withDataDiskNames(
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
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateInfraOsHaAvailabilitySetRecommended.json
     */
    /**
     * Sample code: Create Infrastructure with OS configuration for HA System with Availability Set (Recommended).
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithOSConfigurationForHASystemWithAvailabilitySetRecommended(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E16ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E32ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_M32ts")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(
                                        new OSProfile().withAdminUsername("{your-username}")
                                            .withOsConfiguration(new LinuxConfiguration()
                                                .withDisablePasswordAuthentication(true)
                                                .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withHighAvailabilityConfig(new HighAvailabilityConfiguration()
                            .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_SET)))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateInfraDiskOsDSRecommended.json
     */
    /**
     * Sample code: Create Infrastructure with Disk and OS configuration for Distributed System (Recommended).
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithDiskAndOSConfigurationForDistributedSystemRecommended(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E16ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E32ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_M32ts")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(
                                        new OSProfile().withAdminUsername("{your-username}")
                                            .withOsConfiguration(new LinuxConfiguration()
                                                .withDisablePasswordAuthentication(true)
                                                .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L)
                            .withDiskConfiguration(
                                new DiskConfiguration()
                                    .withDiskVolumeConfigurations(
                                        mapOf("backup",
                                            new DiskVolumeConfiguration()
                                                .withCount(2L)
                                                .withSizeGB(256L)
                                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                            "hana/data",
                                            new DiskVolumeConfiguration().withCount(4L)
                                                .withSizeGB(128L)
                                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)),
                                            "hana/log",
                                            new DiskVolumeConfiguration().withCount(3L)
                                                .withSizeGB(128L)
                                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)),
                                            "hana/shared",
                                            new DiskVolumeConfiguration().withCount(1L)
                                                .withSizeGB(256L)
                                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                            "os",
                                            new DiskVolumeConfiguration().withCount(1L)
                                                .withSizeGB(64L)
                                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                            "usr/sap",
                                            new DiskVolumeConfiguration().withCount(1L)
                                                .withSizeGB(128L)
                                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)))))))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateDetectDS.json
     */
    /**
     * Sample code: Detect SAP Software Installation on a Distributed System.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void detectSAPSoftwareInstallationOnADistributedSystem(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("eastus2")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf("created by", "azureuser"))
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration()
                        .withAppResourceGroup("{{resourcegrp}}")
                        .withNetworkConfiguration(new NetworkConfiguration().withIsSecondaryIpEnabled(true))
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/app")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E4ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP-HA")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("azureuser")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/app")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E4ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP-HA")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("azureuser")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withDatabaseServer(new DatabaseConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/app")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_M32ts")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP-HA")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(
                                        new OSProfile().withAdminUsername("azureuser")
                                            .withOsConfiguration(new LinuxConfiguration()
                                                .withDisablePasswordAuthentication(true)
                                                .withSshKeyPair(new SshKeyPair()
                                                    .withPublicKey("fakeTokenPlaceholder")
                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L)))
                    .withSoftwareConfiguration(new ExternalInstallationSoftwareConfiguration().withCentralServerVmId(
                        "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Compute/virtualMachines/sapq20scsvm0"))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("sap.bpaas.com"))))
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateDetectHaAvailabilitySet.json
     */
    /**
     * Sample code: Detect SAP Software Installation on an HA System with Availability Set.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void detectSAPSoftwareInstallationOnAnHASystemWithAvailabilitySet(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E16ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP-HA")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E32ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP-HA")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_M32ts")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP-HA")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
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
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateInstallSingleServer.json
     */
    /**
     * Sample code: Install SAP Software on Single Server System.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void installSAPSoftwareOnSingleServerSystem(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("eastus2")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.NON_PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new SingleServerConfiguration().withAppResourceGroup("test-rg")
                        .withSubnetId(
                            "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/testsubnet")
                        .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                            .withVmSize("Standard_E32ds_v4")
                            .withImageReference(new ImageReference().withPublisher("SUSE")
                                .withOffer("SLES-SAP")
                                .withSku("12-sp4-gen2")
                                .withVersion("2022.02.01"))
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
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateInfraDS.json
     */
    /**
     * Sample code: Create Infrastructure only for Distributed System.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureOnlyForDistributedSystem(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                .withVmSize("Standard_E16ds_v4")
                                .withImageReference(new ImageReference().withPublisher("RedHat")
                                    .withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2")
                                    .withVersion("latest"))
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
                                .withImageReference(new ImageReference().withPublisher("RedHat")
                                    .withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2")
                                    .withVersion("latest"))
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
                                .withImageReference(new ImageReference().withPublisher("RedHat")
                                    .withOffer("RHEL-SAP")
                                    .withSku("84sapha-gen2")
                                    .withVersion("latest"))
                                .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                    .withOsConfiguration(new LinuxConfiguration()
                                        .withDisablePasswordAuthentication(true)
                                        .withSsh(new SshConfiguration().withPublicKeys(
                                            Arrays.asList(new SshPublicKey().withKeyData("fakeTokenPlaceholder")))))))
                            .withInstanceCount(1L)))))
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateRegisterSapSolutionsCustom.json
     */
    /**
     * Sample code: Register existing SAP system as Virtual Instance for SAP solutions with optional customizations.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void registerExistingSAPSystemAsVirtualInstanceForSAPSolutionsWithOptionalCustomizations(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("northeurope")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf("createdby", "abc@microsoft.com", "test", "abc"))
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.NON_PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DiscoveryConfiguration().withCentralServerVmId(
                    "/subscriptions/8e17e36c-42e9-4cd5-a078-7b44883414e0/resourceGroups/test-rg/providers/Microsoft.Compute/virtualMachines/sapq20scsvm0")
                    .withManagedRgStorageAccountName("q20saacssgrs")))
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateInfraWithoutFileshare.json
     */
    /**
     * Sample code: Create Infrastructure without a SAP Transport Directory Fileshare.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithoutASAPTransportDirectoryFileshare(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E16ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E32ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_M32ts")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(
                                        new OSProfile().withAdminUsername("{your-username}")
                                            .withOsConfiguration(new LinuxConfiguration()
                                                .withDisablePasswordAuthentication(true)
                                                .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(1L))
                        .withStorageConfiguration(new StorageConfiguration()
                            .withTransportFileShareConfiguration(new SkipFileShareConfiguration())))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateInfraOsSIngleServerRecommended.json
     */
    /**
     * Sample code: Create Infrastructure with OS configuration for Single Server System (Recommended).
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithOSConfigurationForSingleServerSystemRecommended(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
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
                            .withImageReference(new ImageReference().withPublisher("RedHat")
                                .withOffer("RHEL-SAP")
                                .withSku("84sapha-gen2")
                                .withVersion("latest"))
                            .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                .withOsConfiguration(new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                    .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                        .withPrivateKey("fakeTokenPlaceholder"))))))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateInfraDiskOsHaAvailabilityZoneRecommended.json
     */
    /**
     * Sample code: Create Infrastructure with Disk and OS configuration for HA System with Availability Zone
     * (Recommended).
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithDiskAndOSConfigurationForHASystemWithAvailabilityZoneRecommended(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E16ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E32ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/dindurkhya-e2etesting/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_M32ts")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(
                                        new OSProfile().withAdminUsername("{your-username}")
                                            .withOsConfiguration(new LinuxConfiguration()
                                                .withDisablePasswordAuthentication(true)
                                                .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L)
                            .withDiskConfiguration(
                                new DiskConfiguration()
                                    .withDiskVolumeConfigurations(
                                        mapOf("backup",
                                            new DiskVolumeConfiguration()
                                                .withCount(2L)
                                                .withSizeGB(256L)
                                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                            "hana/data",
                                            new DiskVolumeConfiguration().withCount(4L)
                                                .withSizeGB(128L)
                                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)),
                                            "hana/log",
                                            new DiskVolumeConfiguration().withCount(3L)
                                                .withSizeGB(128L)
                                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)),
                                            "hana/shared",
                                            new DiskVolumeConfiguration().withCount(1L)
                                                .withSizeGB(256L)
                                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                            "os",
                                            new DiskVolumeConfiguration().withCount(1L)
                                                .withSizeGB(64L)
                                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                                            "usr/sap",
                                            new DiskVolumeConfiguration().withCount(1L)
                                                .withSizeGB(128L)
                                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS))))))
                        .withHighAvailabilityConfig(new HighAvailabilityConfiguration()
                            .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_ZONE)))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateInfraOsHaAvailabilityZoneRecommended.json
     */
    /**
     * Sample code: Create Infrastructure with OS configuration for HA System with Availability Zone (Recommended).
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithOSConfigurationForHASystemWithAvailabilityZoneRecommended(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
            .withProperties(new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.PROD)
                .withSapProduct(SapProductType.S4HANA)
                .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation("eastus")
                    .withInfrastructureConfiguration(new ThreeTierConfiguration().withAppResourceGroup("X00-RG")
                        .withCentralServer(new CentralServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E16ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withApplicationServer(new ApplicationServerConfiguration().withSubnetId(
                            "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/appsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_E32ds_v4")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                        .withOsConfiguration(new LinuxConfiguration()
                                            .withDisablePasswordAuthentication(true)
                                            .withSshKeyPair(new SshKeyPair()
                                                .withPublicKey("fakeTokenPlaceholder")
                                                .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(6L))
                        .withDatabaseServer(new DatabaseConfiguration().withDatabaseType(SapDatabaseType.HANA)
                            .withSubnetId(
                                "/subscriptions/49d64d54-e966-4c46-a868-1999802b762c/resourceGroups/test-rg/providers/Microsoft.Networks/virtualNetworks/test-vnet/subnets/dbsubnet")
                            .withVirtualMachineConfiguration(
                                new VirtualMachineConfiguration().withVmSize("Standard_M32ts")
                                    .withImageReference(new ImageReference().withPublisher("RedHat")
                                        .withOffer("RHEL-SAP")
                                        .withSku("84sapha-gen2")
                                        .withVersion("latest"))
                                    .withOsProfile(
                                        new OSProfile().withAdminUsername("{your-username}")
                                            .withOsConfiguration(new LinuxConfiguration()
                                                .withDisablePasswordAuthentication(true)
                                                .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                                    .withPrivateKey("fakeTokenPlaceholder")))))
                            .withInstanceCount(2L))
                        .withHighAvailabilityConfig(new HighAvailabilityConfiguration()
                            .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_ZONE)))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
            .create();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_CreateInfraDiskOsSingleServerRecommended.json
     */
    /**
     * Sample code: Create Infrastructure with Disk and OS configurations for Single Server System (Recommended).
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void createInfrastructureWithDiskAndOSConfigurationsForSingleServerSystemRecommended(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .define("X00")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf())
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
                            .withImageReference(new ImageReference().withPublisher("RedHat")
                                .withOffer("RHEL-SAP")
                                .withSku("84sapha-gen2")
                                .withVersion("latest"))
                            .withOsProfile(new OSProfile().withAdminUsername("{your-username}")
                                .withOsConfiguration(new LinuxConfiguration().withDisablePasswordAuthentication(true)
                                    .withSshKeyPair(new SshKeyPair().withPublicKey("fakeTokenPlaceholder")
                                        .withPrivateKey("fakeTokenPlaceholder")))))
                        .withDbDiskConfiguration(new DiskConfiguration().withDiskVolumeConfigurations(mapOf("backup",
                            new DiskVolumeConfiguration().withCount(2L)
                                .withSizeGB(256L)
                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                            "hana/data",
                            new DiskVolumeConfiguration().withCount(4L)
                                .withSizeGB(128L)
                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)),
                            "hana/log",
                            new DiskVolumeConfiguration().withCount(3L)
                                .withSizeGB(128L)
                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)),
                            "hana/shared",
                            new DiskVolumeConfiguration().withCount(1L)
                                .withSizeGB(256L)
                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                            "os",
                            new DiskVolumeConfiguration().withCount(1L)
                                .withSizeGB(64L)
                                .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)),
                            "usr/sap",
                            new DiskVolumeConfiguration().withCount(1L)
                                .withSizeGB(128L)
                                .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS))))))
                    .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("xyz.test.com"))))
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

### SapVirtualInstances_Delete

```java
/**
 * Samples for SapVirtualInstances Delete.
 */
public final class SapVirtualInstancesDeleteSamples {
    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_Delete.json
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

### SapVirtualInstances_GetAvailabilityZoneDetails

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapAvailabilityZoneDetailsRequest;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapDatabaseType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapProductType;

/**
 * Samples for SapVirtualInstances GetAvailabilityZoneDetails.
 */
public final class SapVirtualInstancesGetAvailabilityZoneDetailsSamples {
    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_InvokeAvailabilityZoneDetails_northeurope.json
     */
    /**
     * Sample code: SAP Availability zone details in north europe.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPAvailabilityZoneDetailsInNorthEurope(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .getAvailabilityZoneDetailsWithResponse("northeurope",
                new SapAvailabilityZoneDetailsRequest().withAppLocation("northeurope")
                    .withSapProduct(SapProductType.S4HANA)
                    .withDatabaseType(SapDatabaseType.HANA),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_InvokeAvailabilityZoneDetails_eastus.json
     */
    /**
     * Sample code: SAP Availability zone details in east us.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPAvailabilityZoneDetailsInEastUs(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .getAvailabilityZoneDetailsWithResponse("eastus",
                new SapAvailabilityZoneDetailsRequest().withAppLocation("eastus")
                    .withSapProduct(SapProductType.S4HANA)
                    .withDatabaseType(SapDatabaseType.HANA),
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_Get.json
     */
    /**
     * Sample code: SAPVirtualInstances_Get.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPVirtualInstancesGet(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .getByResourceGroupWithResponse("test-rg", "X00", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_GetAcssInstallationBlocked.json
     */
    /**
     * Sample code: SAPVirtualInstances Get With ACSS Installation Blocked.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPVirtualInstancesGetWithACSSInstallationBlocked(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .getByResourceGroupWithResponse("test-rg", "X00", com.azure.core.util.Context.NONE);
    }
}
```

### SapVirtualInstances_GetDiskConfigurations

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapDatabaseType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapDeploymentType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapDiskConfigurationsRequest;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapEnvironmentType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapProductType;

/**
 * Samples for SapVirtualInstances GetDiskConfigurations.
 */
public final class SapVirtualInstancesGetDiskConfigurationsSamples {
    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_InvokeDiskConfigurations_Prod.json
     */
    /**
     * Sample code: SAP disk configurations for input environment Prod.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPDiskConfigurationsForInputEnvironmentProd(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .getDiskConfigurationsWithResponse("centralus",
                new SapDiskConfigurationsRequest().withAppLocation("eastus")
                    .withEnvironment(SapEnvironmentType.PROD)
                    .withSapProduct(SapProductType.S4HANA)
                    .withDatabaseType(SapDatabaseType.HANA)
                    .withDeploymentType(SapDeploymentType.THREE_TIER)
                    .withDbVmSku("Standard_M32ts"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_InvokeDiskConfigurations_NonProd.json
     */
    /**
     * Sample code: SAP disk configurations for input environment NonProd.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPDiskConfigurationsForInputEnvironmentNonProd(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .getDiskConfigurationsWithResponse("centralus",
                new SapDiskConfigurationsRequest().withAppLocation("eastus")
                    .withEnvironment(SapEnvironmentType.NON_PROD)
                    .withSapProduct(SapProductType.S4HANA)
                    .withDatabaseType(SapDatabaseType.HANA)
                    .withDeploymentType(SapDeploymentType.THREE_TIER)
                    .withDbVmSku("Standard_M32ts"),
                com.azure.core.util.Context.NONE);
    }
}
```

### SapVirtualInstances_GetSapSupportedSku

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapDatabaseType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapDeploymentType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapEnvironmentType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapHighAvailabilityType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapProductType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapSupportedSkusRequest;

/**
 * Samples for SapVirtualInstances GetSapSupportedSku.
 */
public final class SapVirtualInstancesGetSapSupportedSkuSamples {
    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_InvokeSapSupportedSku_DistributedHA_AvSet.json
     */
    /**
     * Sample code: SAP supported SKUs for distributed HA environment with Availability set.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPSupportedSKUsForDistributedHAEnvironmentWithAvailabilitySet(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .getSapSupportedSkuWithResponse("centralus",
                new SapSupportedSkusRequest().withAppLocation("eastus")
                    .withEnvironment(SapEnvironmentType.PROD)
                    .withSapProduct(SapProductType.S4HANA)
                    .withDeploymentType(SapDeploymentType.THREE_TIER)
                    .withDatabaseType(SapDatabaseType.HANA)
                    .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_SET),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_InvokeSapSupportedSku_DistributedHA_AvZone.json
     */
    /**
     * Sample code: SAP supported Skus for HA with availability zone.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPSupportedSkusForHAWithAvailabilityZone(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .getSapSupportedSkuWithResponse("centralus",
                new SapSupportedSkusRequest().withAppLocation("eastus")
                    .withEnvironment(SapEnvironmentType.PROD)
                    .withSapProduct(SapProductType.S4HANA)
                    .withDeploymentType(SapDeploymentType.THREE_TIER)
                    .withDatabaseType(SapDatabaseType.HANA)
                    .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_ZONE),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_InvokeSapSupportedSku_Distributed.json
     */
    /**
     * Sample code: SAP supported SKUs for distributed Non HA environment.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPSupportedSKUsForDistributedNonHAEnvironment(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .getSapSupportedSkuWithResponse("centralus",
                new SapSupportedSkusRequest().withAppLocation("eastus")
                    .withEnvironment(SapEnvironmentType.PROD)
                    .withSapProduct(SapProductType.S4HANA)
                    .withDeploymentType(SapDeploymentType.THREE_TIER)
                    .withDatabaseType(SapDatabaseType.HANA),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_InvokeSapSupportedSku_SingleServer.json
     */
    /**
     * Sample code: SAP supported SKUs for single server.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPSupportedSKUsForSingleServer(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .getSapSupportedSkuWithResponse("centralus",
                new SapSupportedSkusRequest().withAppLocation("eastus")
                    .withEnvironment(SapEnvironmentType.NON_PROD)
                    .withSapProduct(SapProductType.S4HANA)
                    .withDeploymentType(SapDeploymentType.SINGLE_SERVER)
                    .withDatabaseType(SapDatabaseType.HANA),
                com.azure.core.util.Context.NONE);
    }
}
```

### SapVirtualInstances_GetSizingRecommendations

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapDatabaseScaleMethod;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapDatabaseType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapDeploymentType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapEnvironmentType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapHighAvailabilityType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapProductType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapSizingRecommendationRequest;

/**
 * Samples for SapVirtualInstances GetSizingRecommendations.
 */
public final class SapVirtualInstancesGetSizingRecommendationsSamples {
    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_InvokeSizingRecommendations_S4HANA_HA_AvZone.json
     */
    /**
     * Sample code: SAP sizing recommendations for HA with availability zone.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPSizingRecommendationsForHAWithAvailabilityZone(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .getSizingRecommendationsWithResponse("centralus",
                new SapSizingRecommendationRequest().withAppLocation("eastus")
                    .withEnvironment(SapEnvironmentType.PROD)
                    .withSapProduct(SapProductType.S4HANA)
                    .withDeploymentType(SapDeploymentType.THREE_TIER)
                    .withSaps(75000L)
                    .withDbMemory(1024L)
                    .withDatabaseType(SapDatabaseType.HANA)
                    .withDbScaleMethod(SapDatabaseScaleMethod.SCALE_UP)
                    .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_ZONE),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_InvokeSizingRecommendations_S4HANA_Distributed.json
     */
    /**
     * Sample code: SAP sizing recommendations for non HA distributed system.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPSizingRecommendationsForNonHADistributedSystem(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .getSizingRecommendationsWithResponse("centralus",
                new SapSizingRecommendationRequest().withAppLocation("eastus")
                    .withEnvironment(SapEnvironmentType.PROD)
                    .withSapProduct(SapProductType.S4HANA)
                    .withDeploymentType(SapDeploymentType.THREE_TIER)
                    .withSaps(20000L)
                    .withDbMemory(1024L)
                    .withDatabaseType(SapDatabaseType.HANA)
                    .withDbScaleMethod(SapDatabaseScaleMethod.SCALE_UP),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_InvokeSizingRecommendations_S4HANA_HA_AvSet.json
     */
    /**
     * Sample code: SAP sizing recommendations for HA with availability set.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPSizingRecommendationsForHAWithAvailabilitySet(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .getSizingRecommendationsWithResponse("centralus",
                new SapSizingRecommendationRequest().withAppLocation("eastus")
                    .withEnvironment(SapEnvironmentType.PROD)
                    .withSapProduct(SapProductType.S4HANA)
                    .withDeploymentType(SapDeploymentType.THREE_TIER)
                    .withSaps(75000L)
                    .withDbMemory(1024L)
                    .withDatabaseType(SapDatabaseType.HANA)
                    .withDbScaleMethod(SapDatabaseScaleMethod.SCALE_UP)
                    .withHighAvailabilityType(SapHighAvailabilityType.AVAILABILITY_SET),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_InvokeSizingRecommendations_S4HANA_SingleServer.json
     */
    /**
     * Sample code: SAP sizing recommendations for single server.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPSizingRecommendationsForSingleServer(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .getSizingRecommendationsWithResponse("centralus",
                new SapSizingRecommendationRequest().withAppLocation("eastus")
                    .withEnvironment(SapEnvironmentType.NON_PROD)
                    .withSapProduct(SapProductType.S4HANA)
                    .withDeploymentType(SapDeploymentType.SINGLE_SERVER)
                    .withSaps(60000L)
                    .withDbMemory(2000L)
                    .withDatabaseType(SapDatabaseType.HANA)
                    .withDbScaleMethod(SapDatabaseScaleMethod.SCALE_UP),
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
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_ListBySubscription.json
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
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_ListByResourceGroup.json
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
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_Start.json
     */
    /**
     * Sample code: SAPVirtualInstances_Start.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPVirtualInstancesStart(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .start("test-rg", "X00", new StartRequest().withStartVm(true), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_StartWithInfraOperations.json
     */
    /**
     * Sample code: SAPVirtualInstances_Start_WithInfraOperations.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPVirtualInstancesStartWithInfraOperations(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .start("test-rg", "X00", new StartRequest().withStartVm(true), com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_StopVMAndSystem.json
     */
    /**
     * Sample code: Stop the virtual machine(s) and the SAP system on it.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void stopTheVirtualMachineSAndTheSAPSystemOnIt(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .stop("test-rg", "X00", new StopRequest().withSoftStopTimeoutSeconds(0L).withDeallocateVm(true),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_SoftStop.json
     */
    /**
     * Sample code: Soft Stop of SapVirtualInstances_Stop.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void softStopOfSapVirtualInstancesStop(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .stop("test-rg", "X00", new StopRequest().withSoftStopTimeoutSeconds(300L),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_Stop.json
     */
    /**
     * Sample code: SAPVirtualInstances_Stop.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPVirtualInstancesStop(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .stop("test-rg", "X00", new StopRequest().withSoftStopTimeoutSeconds(0L), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_SoftStopVMAndSystem.json
     */
    /**
     * Sample code: Soft Stop the virtual machine(s) and the SAP system on it.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void softStopTheVirtualMachineSAndTheSAPSystemOnIt(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        manager.sapVirtualInstances()
            .stop("test-rg", "X00", new StopRequest().withSoftStopTimeoutSeconds(300L).withDeallocateVm(true),
                com.azure.core.util.Context.NONE);
    }
}
```

### SapVirtualInstances_Update

```java
import com.azure.resourcemanager.workloadssapvirtualinstance.models.ManagedResourcesNetworkAccessType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SAPVirtualInstanceIdentity;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SAPVirtualInstanceIdentityType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapVirtualInstance;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.UpdateSapVirtualInstanceProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SapVirtualInstances Update.
 */
public final class SapVirtualInstancesUpdateSamples {
    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_Update.json
     */
    /**
     * Sample code: SAPVirtualInstances_Update.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPVirtualInstancesUpdate(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        SapVirtualInstance resource = manager.sapVirtualInstances()
            .getByResourceGroupWithResponse("test-rg", "X00", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withIdentity(new SAPVirtualInstanceIdentity().withType(SAPVirtualInstanceIdentityType.NONE))
            .withProperties(new UpdateSapVirtualInstanceProperties())
            .apply();
    }

    /*
     * x-ms-original-file: 2024-09-01/SapVirtualInstances_UpdateTrustedAccess.json
     */
    /**
     * Sample code: SAPVirtualInstances_TrustedAccessEnable_Update.
     * 
     * @param manager Entry point to WorkloadsSapVirtualInstanceManager.
     */
    public static void sAPVirtualInstancesTrustedAccessEnableUpdate(
        com.azure.resourcemanager.workloadssapvirtualinstance.WorkloadsSapVirtualInstanceManager manager) {
        SapVirtualInstance resource = manager.sapVirtualInstances()
            .getByResourceGroupWithResponse("test-rg", "X00", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withIdentity(new SAPVirtualInstanceIdentity().withType(SAPVirtualInstanceIdentityType.NONE))
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

