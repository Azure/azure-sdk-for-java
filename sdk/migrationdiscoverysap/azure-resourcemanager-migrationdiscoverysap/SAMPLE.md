# Code snippets and samples


## Operations

- [List](#operations_list)

## SapDiscoverySites

- [Create](#sapdiscoverysites_create)
- [Delete](#sapdiscoverysites_delete)
- [GetByResourceGroup](#sapdiscoverysites_getbyresourcegroup)
- [ImportEntities](#sapdiscoverysites_importentities)
- [List](#sapdiscoverysites_list)
- [ListByResourceGroup](#sapdiscoverysites_listbyresourcegroup)
- [Update](#sapdiscoverysites_update)

## SapInstances

- [Create](#sapinstances_create)
- [Delete](#sapinstances_delete)
- [Get](#sapinstances_get)
- [ListBySapDiscoverySite](#sapinstances_listbysapdiscoverysite)
- [Update](#sapinstances_update)

## ServerInstances

- [Create](#serverinstances_create)
- [Delete](#serverinstances_delete)
- [Get](#serverinstances_get)
- [ListBySapInstance](#serverinstances_listbysapinstance)
- [Update](#serverinstances_update)
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
     * @param manager Entry point to MigrationDiscoverySapManager.
     */
    public static void listTheOperationsForTheProvider(
        com.azure.resourcemanager.migrationdiscoverysap.MigrationDiscoverySapManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### SapDiscoverySites_Create

```java
import com.azure.resourcemanager.migrationdiscoverysap.models.SapDiscoverySiteProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SapDiscoverySites Create.
 */
public final class SapDiscoverySitesCreateSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPDiscoverySites/preview/2023-10-01-preview/
     * examples/SAPDiscoverySites_Create.json
     */
    /**
     * Sample code: Create resource for Import based input.
     * 
     * @param manager Entry point to MigrationDiscoverySapManager.
     */
    public static void createResourceForImportBasedInput(
        com.azure.resourcemanager.migrationdiscoverysap.MigrationDiscoverySapManager manager) {
        manager.sapDiscoverySites().define("SampleSite").withRegion("eastus").withExistingResourceGroup("test-rg")
            .withTags(mapOf("property1", "value1", "property2", "value2"))
            .withProperties(new SapDiscoverySiteProperties().withMasterSiteId("MasterSiteIdResourceId")
                .withMigrateProjectId("MigrateProjectId"))
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

### SapDiscoverySites_Delete

```java
/**
 * Samples for SapDiscoverySites Delete.
 */
public final class SapDiscoverySitesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPDiscoverySites/preview/2023-10-01-preview/
     * examples/SAPDiscoverySites_Delete.json
     */
    /**
     * Sample code: Deletes a SAP Migration discovery site resource.
     * 
     * @param manager Entry point to MigrationDiscoverySapManager.
     */
    public static void deletesASAPMigrationDiscoverySiteResource(
        com.azure.resourcemanager.migrationdiscoverysap.MigrationDiscoverySapManager manager) {
        manager.sapDiscoverySites().delete("test-rg", "SampleSite", com.azure.core.util.Context.NONE);
    }
}
```

### SapDiscoverySites_GetByResourceGroup

```java
/**
 * Samples for SapDiscoverySites GetByResourceGroup.
 */
public final class SapDiscoverySitesGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPDiscoverySites/preview/2023-10-01-preview/
     * examples/SAPDiscoverySites_Get.json
     */
    /**
     * Sample code: GET a SAP Migration discovery site resource.
     * 
     * @param manager Entry point to MigrationDiscoverySapManager.
     */
    public static void gETASAPMigrationDiscoverySiteResource(
        com.azure.resourcemanager.migrationdiscoverysap.MigrationDiscoverySapManager manager) {
        manager.sapDiscoverySites().getByResourceGroupWithResponse("test-rg", "SampleSite",
            com.azure.core.util.Context.NONE);
    }
}
```

### SapDiscoverySites_ImportEntities

```java
/**
 * Samples for SapDiscoverySites ImportEntities.
 */
public final class SapDiscoverySitesImportEntitiesSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPDiscoverySites/preview/2023-10-01-preview/
     * examples/SAPDiscoverySites_ImportEntities.json
     */
    /**
     * Sample code: Import a SAP Migration discovery site resource and it's child resources.
     * 
     * @param manager Entry point to MigrationDiscoverySapManager.
     */
    public static void importASAPMigrationDiscoverySiteResourceAndItSChildResources(
        com.azure.resourcemanager.migrationdiscoverysap.MigrationDiscoverySapManager manager) {
        manager.sapDiscoverySites().importEntities("test-rg", "SampleSite", com.azure.core.util.Context.NONE);
    }
}
```

### SapDiscoverySites_List

```java
/**
 * Samples for SapDiscoverySites List.
 */
public final class SapDiscoverySitesListSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPDiscoverySites/preview/2023-10-01-preview/
     * examples/SAPDiscoverySites_ListBySubscription.json
     */
    /**
     * Sample code: List SAP Migration discovery site resources in a subscription.
     * 
     * @param manager Entry point to MigrationDiscoverySapManager.
     */
    public static void listSAPMigrationDiscoverySiteResourcesInASubscription(
        com.azure.resourcemanager.migrationdiscoverysap.MigrationDiscoverySapManager manager) {
        manager.sapDiscoverySites().list(com.azure.core.util.Context.NONE);
    }
}
```

### SapDiscoverySites_ListByResourceGroup

```java
/**
 * Samples for SapDiscoverySites ListByResourceGroup.
 */
public final class SapDiscoverySitesListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPDiscoverySites/preview/2023-10-01-preview/
     * examples/SAPDiscoverySites_ListByResourceGroup.json
     */
    /**
     * Sample code: List SAP Migration discovery site resources by Resource group.
     * 
     * @param manager Entry point to MigrationDiscoverySapManager.
     */
    public static void listSAPMigrationDiscoverySiteResourcesByResourceGroup(
        com.azure.resourcemanager.migrationdiscoverysap.MigrationDiscoverySapManager manager) {
        manager.sapDiscoverySites().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### SapDiscoverySites_Update

```java
import com.azure.resourcemanager.migrationdiscoverysap.models.SapDiscoverySite;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SapDiscoverySites Update.
 */
public final class SapDiscoverySitesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPDiscoverySites/preview/2023-10-01-preview/
     * examples/SAPDiscoverySites_Update.json
     */
    /**
     * Sample code: Updates a SAP Migration discovery site resource.
     * 
     * @param manager Entry point to MigrationDiscoverySapManager.
     */
    public static void updatesASAPMigrationDiscoverySiteResource(
        com.azure.resourcemanager.migrationdiscoverysap.MigrationDiscoverySapManager manager) {
        SapDiscoverySite resource = manager.sapDiscoverySites()
            .getByResourceGroupWithResponse("test-rg", "SampleSite", com.azure.core.util.Context.NONE).getValue();
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

### SapInstances_Create

```java
import com.azure.resourcemanager.migrationdiscoverysap.models.SapInstanceProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SapInstances Create.
 */
public final class SapInstancesCreateSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPDiscoverySites/preview/2023-10-01-preview/
     * examples/SAPInstances_Create.json
     */
    /**
     * Sample code: Creates the SAP Instance resource.
     * 
     * @param manager Entry point to MigrationDiscoverySapManager.
     */
    public static void createsTheSAPInstanceResource(
        com.azure.resourcemanager.migrationdiscoverysap.MigrationDiscoverySapManager manager) {
        manager.sapInstances().define("MPP_MPP").withRegion("eastus")
            .withExistingSapDiscoverySite("test-rg", "SampleSite")
            .withTags(mapOf("property1", "value1", "property2", "value2")).withProperties(new SapInstanceProperties())
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

### SapInstances_Delete

```java
/**
 * Samples for SapInstances Delete.
 */
public final class SapInstancesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPDiscoverySites/preview/2023-10-01-preview/
     * examples/SAPInstances_Delete.json
     */
    /**
     * Sample code: Deletes the SAP Instance resource.
     * 
     * @param manager Entry point to MigrationDiscoverySapManager.
     */
    public static void deletesTheSAPInstanceResource(
        com.azure.resourcemanager.migrationdiscoverysap.MigrationDiscoverySapManager manager) {
        manager.sapInstances().delete("test-rg", "SampleSite", "MPP_MPP", com.azure.core.util.Context.NONE);
    }
}
```

### SapInstances_Get

```java
/**
 * Samples for SapInstances Get.
 */
public final class SapInstancesGetSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPDiscoverySites/preview/2023-10-01-preview/
     * examples/SAPInstances_Get.json
     */
    /**
     * Sample code: GET a SAP Instance resource.
     * 
     * @param manager Entry point to MigrationDiscoverySapManager.
     */
    public static void
        gETASAPInstanceResource(com.azure.resourcemanager.migrationdiscoverysap.MigrationDiscoverySapManager manager) {
        manager.sapInstances().getWithResponse("test-rg", "SampleSite", "MPP_MPP", com.azure.core.util.Context.NONE);
    }
}
```

### SapInstances_ListBySapDiscoverySite

```java
/**
 * Samples for SapInstances ListBySapDiscoverySite.
 */
public final class SapInstancesListBySapDiscoverySiteSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPDiscoverySites/preview/2023-10-01-preview/
     * examples/SAPInstances_List.json
     */
    /**
     * Sample code: Lists the SAP Instance resources for the given SAP Migration discovery site resource.
     * 
     * @param manager Entry point to MigrationDiscoverySapManager.
     */
    public static void listsTheSAPInstanceResourcesForTheGivenSAPMigrationDiscoverySiteResource(
        com.azure.resourcemanager.migrationdiscoverysap.MigrationDiscoverySapManager manager) {
        manager.sapInstances().listBySapDiscoverySite("test-rg", "SampleSite", com.azure.core.util.Context.NONE);
    }
}
```

### SapInstances_Update

```java
import com.azure.resourcemanager.migrationdiscoverysap.models.SapInstance;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SapInstances Update.
 */
public final class SapInstancesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPDiscoverySites/preview/2023-10-01-preview/
     * examples/SAPInstances_Update.json
     */
    /**
     * Sample code: Updates the SAP Instance resource.
     * 
     * @param manager Entry point to MigrationDiscoverySapManager.
     */
    public static void updatesTheSAPInstanceResource(
        com.azure.resourcemanager.migrationdiscoverysap.MigrationDiscoverySapManager manager) {
        SapInstance resource = manager.sapInstances()
            .getWithResponse("test-rg", "SampleSite", "MPP_MPP", com.azure.core.util.Context.NONE).getValue();
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

### ServerInstances_Create

```java
/**
 * Samples for ServerInstances Create.
 */
public final class ServerInstancesCreateSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPDiscoverySites/preview/2023-10-01-preview/
     * examples/ServerInstances_Create.json
     */
    /**
     * Sample code: Creates the Server Instance resource.
     * 
     * @param manager Entry point to MigrationDiscoverySapManager.
     */
    public static void createsTheServerInstanceResource(
        com.azure.resourcemanager.migrationdiscoverysap.MigrationDiscoverySapManager manager) {
        manager.serverInstances().define("APP_SapServer1").withExistingSapInstance("test-rg", "SampleSite", "MPP_MPP")
            .create();
    }
}
```

### ServerInstances_Delete

```java
/**
 * Samples for ServerInstances Delete.
 */
public final class ServerInstancesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPDiscoverySites/preview/2023-10-01-preview/
     * examples/ServerInstances_Delete.json
     */
    /**
     * Sample code: Deletes the Server Instance resource.
     * 
     * @param manager Entry point to MigrationDiscoverySapManager.
     */
    public static void deletesTheServerInstanceResource(
        com.azure.resourcemanager.migrationdiscoverysap.MigrationDiscoverySapManager manager) {
        manager.serverInstances().delete("test-rg", "SampleSite", "MPP_MPP", "APP_SapServer1",
            com.azure.core.util.Context.NONE);
    }
}
```

### ServerInstances_Get

```java
/**
 * Samples for ServerInstances Get.
 */
public final class ServerInstancesGetSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPDiscoverySites/preview/2023-10-01-preview/
     * examples/ServerInstances_Get.json
     */
    /**
     * Sample code: GET a Server Instance resource.
     * 
     * @param manager Entry point to MigrationDiscoverySapManager.
     */
    public static void gETAServerInstanceResource(
        com.azure.resourcemanager.migrationdiscoverysap.MigrationDiscoverySapManager manager) {
        manager.serverInstances().getWithResponse("test-rg", "SampleSite", "MPP_MPP", "APP_SapServer1",
            com.azure.core.util.Context.NONE);
    }
}
```

### ServerInstances_ListBySapInstance

```java
/**
 * Samples for ServerInstances ListBySapInstance.
 */
public final class ServerInstancesListBySapInstanceSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPDiscoverySites/preview/2023-10-01-preview/
     * examples/ServerInstances_List.json
     */
    /**
     * Sample code: Lists the Server Instance resources for the given SAP Instance resource.
     * 
     * @param manager Entry point to MigrationDiscoverySapManager.
     */
    public static void listsTheServerInstanceResourcesForTheGivenSAPInstanceResource(
        com.azure.resourcemanager.migrationdiscoverysap.MigrationDiscoverySapManager manager) {
        manager.serverInstances().listBySapInstance("test-rg", "SampleSite", "MPP_MPP",
            com.azure.core.util.Context.NONE);
    }
}
```

### ServerInstances_Update

```java
import com.azure.resourcemanager.migrationdiscoverysap.models.ServerInstance;
import com.azure.resourcemanager.migrationdiscoverysap.models.ServerInstanceProperties;

/**
 * Samples for ServerInstances Update.
 */
public final class ServerInstancesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/SAPDiscoverySites/preview/2023-10-01-preview/
     * examples/ServerInstances_Update.json
     */
    /**
     * Sample code: Updates the Server Instance resource.
     * 
     * @param manager Entry point to MigrationDiscoverySapManager.
     */
    public static void updatesTheServerInstanceResource(
        com.azure.resourcemanager.migrationdiscoverysap.MigrationDiscoverySapManager manager) {
        ServerInstance resource = manager.serverInstances()
            .getWithResponse("test-rg", "SampleSite", "MPP_MPP", "APP_SapServer1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withProperties(new ServerInstanceProperties()).apply();
    }
}
```

