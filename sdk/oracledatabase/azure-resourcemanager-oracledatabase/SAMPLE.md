# Code snippets and samples


## AutonomousDatabaseBackups

- [CreateOrUpdate](#autonomousdatabasebackups_createorupdate)
- [Delete](#autonomousdatabasebackups_delete)
- [Get](#autonomousdatabasebackups_get)
- [ListByAutonomousDatabase](#autonomousdatabasebackups_listbyautonomousdatabase)
- [Update](#autonomousdatabasebackups_update)

## AutonomousDatabaseCharacterSets

- [Get](#autonomousdatabasecharactersets_get)
- [ListByLocation](#autonomousdatabasecharactersets_listbylocation)

## AutonomousDatabaseNationalCharacterSets

- [Get](#autonomousdatabasenationalcharactersets_get)
- [ListByLocation](#autonomousdatabasenationalcharactersets_listbylocation)

## AutonomousDatabaseVersions

- [Get](#autonomousdatabaseversions_get)
- [ListByLocation](#autonomousdatabaseversions_listbylocation)

## AutonomousDatabases

- [CreateOrUpdate](#autonomousdatabases_createorupdate)
- [Delete](#autonomousdatabases_delete)
- [Failover](#autonomousdatabases_failover)
- [GenerateWallet](#autonomousdatabases_generatewallet)
- [GetByResourceGroup](#autonomousdatabases_getbyresourcegroup)
- [List](#autonomousdatabases_list)
- [ListByResourceGroup](#autonomousdatabases_listbyresourcegroup)
- [Restore](#autonomousdatabases_restore)
- [Shrink](#autonomousdatabases_shrink)
- [Switchover](#autonomousdatabases_switchover)
- [Update](#autonomousdatabases_update)

## CloudExadataInfrastructures

- [AddStorageCapacity](#cloudexadatainfrastructures_addstoragecapacity)
- [CreateOrUpdate](#cloudexadatainfrastructures_createorupdate)
- [Delete](#cloudexadatainfrastructures_delete)
- [GetByResourceGroup](#cloudexadatainfrastructures_getbyresourcegroup)
- [List](#cloudexadatainfrastructures_list)
- [ListByResourceGroup](#cloudexadatainfrastructures_listbyresourcegroup)
- [Update](#cloudexadatainfrastructures_update)

## CloudVmClusters

- [AddVms](#cloudvmclusters_addvms)
- [CreateOrUpdate](#cloudvmclusters_createorupdate)
- [Delete](#cloudvmclusters_delete)
- [GetByResourceGroup](#cloudvmclusters_getbyresourcegroup)
- [List](#cloudvmclusters_list)
- [ListByResourceGroup](#cloudvmclusters_listbyresourcegroup)
- [ListPrivateIpAddresses](#cloudvmclusters_listprivateipaddresses)
- [RemoveVms](#cloudvmclusters_removevms)
- [Update](#cloudvmclusters_update)

## DbNodes

- [Action](#dbnodes_action)
- [Get](#dbnodes_get)
- [ListByCloudVmCluster](#dbnodes_listbycloudvmcluster)

## DbServers

- [Get](#dbservers_get)
- [ListByCloudExadataInfrastructure](#dbservers_listbycloudexadatainfrastructure)

## DbSystemShapes

- [Get](#dbsystemshapes_get)
- [ListByLocation](#dbsystemshapes_listbylocation)

## DnsPrivateViews

- [Get](#dnsprivateviews_get)
- [ListByLocation](#dnsprivateviews_listbylocation)

## DnsPrivateZones

- [Get](#dnsprivatezones_get)
- [ListByLocation](#dnsprivatezones_listbylocation)

## GiVersions

- [Get](#giversions_get)
- [ListByLocation](#giversions_listbylocation)

## Operations

- [List](#operations_list)

## OracleSubscriptions

- [CreateOrUpdate](#oraclesubscriptions_createorupdate)
- [Delete](#oraclesubscriptions_delete)
- [Get](#oraclesubscriptions_get)
- [List](#oraclesubscriptions_list)
- [ListActivationLinks](#oraclesubscriptions_listactivationlinks)
- [ListCloudAccountDetails](#oraclesubscriptions_listcloudaccountdetails)
- [ListSaasSubscriptionDetails](#oraclesubscriptions_listsaassubscriptiondetails)
- [Update](#oraclesubscriptions_update)

## SystemVersions

- [Get](#systemversions_get)
- [ListByLocation](#systemversions_listbylocation)

## VirtualNetworkAddresses

- [CreateOrUpdate](#virtualnetworkaddresses_createorupdate)
- [Delete](#virtualnetworkaddresses_delete)
- [Get](#virtualnetworkaddresses_get)
- [ListByCloudVmCluster](#virtualnetworkaddresses_listbycloudvmcluster)
### AutonomousDatabaseBackups_CreateOrUpdate

```java
import com.azure.resourcemanager.oracledatabase.models.AutonomousDatabaseBackupProperties;

/**
 * Samples for AutonomousDatabaseBackups CreateOrUpdate.
 */
public final class AutonomousDatabaseBackupsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/autonomousDatabaseBackup_create.
     * json
     */
    /**
     * Sample code: Create Autonomous Database Backup.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        createAutonomousDatabaseBackup(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabaseBackups()
            .define("1711644130")
            .withExistingAutonomousDatabase("rg000", "databasedb1")
            .withProperties(new AutonomousDatabaseBackupProperties().withDisplayName("Nightly Backup")
                .withRetentionPeriodInDays(365))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/autonomousDatabaseBackup_create.
     * json
     */
    /**
     * Sample code: AutonomousDatabaseBackups_CreateOrUpdate.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void autonomousDatabaseBackupsCreateOrUpdate(
        com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabaseBackups()
            .define("1711644130")
            .withExistingAutonomousDatabase("rg000", "databasedb1")
            .withProperties(new AutonomousDatabaseBackupProperties().withDisplayName("Nightly Backup")
                .withRetentionPeriodInDays(365))
            .create();
    }
}
```

### AutonomousDatabaseBackups_Delete

```java
/**
 * Samples for AutonomousDatabaseBackups Delete.
 */
public final class AutonomousDatabaseBackupsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/autonomousDatabaseBackup_delete.
     * json
     */
    /**
     * Sample code: Delete Autonomous Database Backup.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        deleteAutonomousDatabaseBackup(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabaseBackups()
            .delete("rg000", "databasedb1", "1711644130", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/autonomousDatabaseBackup_delete.
     * json
     */
    /**
     * Sample code: AutonomousDatabaseBackups_Delete.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        autonomousDatabaseBackupsDelete(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabaseBackups()
            .delete("rg000", "databasedb1", "1711644130", com.azure.core.util.Context.NONE);
    }
}
```

### AutonomousDatabaseBackups_Get

```java
/**
 * Samples for AutonomousDatabaseBackups Get.
 */
public final class AutonomousDatabaseBackupsGetSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/autonomousDatabaseBackup_get.
     * json
     */
    /**
     * Sample code: Get Autonomous Database Backup.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        getAutonomousDatabaseBackup(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabaseBackups()
            .getWithResponse("rg000", "databasedb1", "1711644130", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/autonomousDatabaseBackup_get.
     * json
     */
    /**
     * Sample code: AutonomousDatabaseBackups_Get.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        autonomousDatabaseBackupsGet(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabaseBackups()
            .getWithResponse("rg000", "databasedb1", "1711644130", com.azure.core.util.Context.NONE);
    }
}
```

### AutonomousDatabaseBackups_ListByAutonomousDatabase

```java
/**
 * Samples for AutonomousDatabaseBackups ListByAutonomousDatabase.
 */
public final class AutonomousDatabaseBackupsListByAutonomousDatabaseSamples {
    /*
     * x-ms-original-file: specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/
     * autonomousDatabaseBackup_listByParent.json
     */
    /**
     * Sample code: List Autonomous Database Backups by Autonomous Database.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void listAutonomousDatabaseBackupsByAutonomousDatabase(
        com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabaseBackups()
            .listByAutonomousDatabase("rg000", "databasedb1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/
     * autonomousDatabaseBackup_listByParent.json
     */
    /**
     * Sample code: AutonomousDatabaseBackups_ListByAutonomousDatabase.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void autonomousDatabaseBackupsListByAutonomousDatabase(
        com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabaseBackups()
            .listByAutonomousDatabase("rg000", "databasedb1", com.azure.core.util.Context.NONE);
    }
}
```

### AutonomousDatabaseBackups_Update

```java
import com.azure.resourcemanager.oracledatabase.models.AutonomousDatabaseBackup;

/**
 * Samples for AutonomousDatabaseBackups Update.
 */
public final class AutonomousDatabaseBackupsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/autonomousDatabaseBackup_patch.
     * json
     */
    /**
     * Sample code: Patch Autonomous Database Backup.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        patchAutonomousDatabaseBackup(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        AutonomousDatabaseBackup resource = manager.autonomousDatabaseBackups()
            .getWithResponse("rg000", "databasedb1", "1711644130", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }

    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/autonomousDatabaseBackup_patch.
     * json
     */
    /**
     * Sample code: AutonomousDatabaseBackups_Update.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        autonomousDatabaseBackupsUpdate(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        AutonomousDatabaseBackup resource = manager.autonomousDatabaseBackups()
            .getWithResponse("rg000", "databasedb1", "1711644130", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }
}
```

### AutonomousDatabaseCharacterSets_Get

```java
/**
 * Samples for AutonomousDatabaseCharacterSets Get.
 */
public final class AutonomousDatabaseCharacterSetsGetSamples {
    /*
     * x-ms-original-file: specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/
     * autonomousDatabaseCharacterSet_get.json
     */
    /**
     * Sample code: Get autonomous db character set.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        getAutonomousDbCharacterSet(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabaseCharacterSets()
            .getWithResponse("eastus", "DATABASE", com.azure.core.util.Context.NONE);
    }
}
```

### AutonomousDatabaseCharacterSets_ListByLocation

```java
/**
 * Samples for AutonomousDatabaseCharacterSets ListByLocation.
 */
public final class AutonomousDatabaseCharacterSetsListByLocationSamples {
    /*
     * x-ms-original-file: specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/
     * autonomousDatabaseCharacterSet_listByLocation.json
     */
    /**
     * Sample code: List autonomous db character sets by location.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void listAutonomousDbCharacterSetsByLocation(
        com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabaseCharacterSets().listByLocation("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### AutonomousDatabaseNationalCharacterSets_Get

```java
/**
 * Samples for AutonomousDatabaseNationalCharacterSets Get.
 */
public final class AutonomousDatabaseNationalCharacterSetsGetSamples {
    /*
     * x-ms-original-file: specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/
     * autonomousDatabaseNationalCharacterSet_get.json
     */
    /**
     * Sample code: Get autonomous db national character set.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        getAutonomousDbNationalCharacterSet(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabaseNationalCharacterSets()
            .getWithResponse("eastus", "NATIONAL", com.azure.core.util.Context.NONE);
    }
}
```

### AutonomousDatabaseNationalCharacterSets_ListByLocation

```java
/**
 * Samples for AutonomousDatabaseNationalCharacterSets ListByLocation.
 */
public final class AutonomousDatabaseNationalCharacterSetsListByLocationSamples {
    /*
     * x-ms-original-file: specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/
     * autonomousDatabaseNationalCharacterSet_listByLocation.json
     */
    /**
     * Sample code: List autonomous db national character sets by location.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void listAutonomousDbNationalCharacterSetsByLocation(
        com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabaseNationalCharacterSets().listByLocation("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### AutonomousDatabaseVersions_Get

```java
/**
 * Samples for AutonomousDatabaseVersions Get.
 */
public final class AutonomousDatabaseVersionsGetSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/autonomousDatabaseVersion_get.
     * json
     */
    /**
     * Sample code: Get an autonomous version.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void getAnAutonomousVersion(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabaseVersions().getWithResponse("eastus", "18.4.0.0", com.azure.core.util.Context.NONE);
    }
}
```

### AutonomousDatabaseVersions_ListByLocation

```java
/**
 * Samples for AutonomousDatabaseVersions ListByLocation.
 */
public final class AutonomousDatabaseVersionsListByLocationSamples {
    /*
     * x-ms-original-file: specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/
     * autonomousDatabaseVersion_listByLocation.json
     */
    /**
     * Sample code: List an autonomous versions by location.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        listAnAutonomousVersionsByLocation(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabaseVersions().listByLocation("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### AutonomousDatabases_CreateOrUpdate

```java
import com.azure.resourcemanager.oracledatabase.models.AutonomousDatabaseCloneProperties;
import com.azure.resourcemanager.oracledatabase.models.AutonomousDatabaseProperties;
import com.azure.resourcemanager.oracledatabase.models.CloneType;
import com.azure.resourcemanager.oracledatabase.models.ComputeModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AutonomousDatabases CreateOrUpdate.
 */
public final class AutonomousDatabasesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/autonomousDatabase_create.json
     */
    /**
     * Sample code: AutonomousDatabases_CreateOrUpdate.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        autonomousDatabasesCreateOrUpdate(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabases()
            .define("databasedb1")
            .withRegion("eastus")
            .withExistingResourceGroup("rg000")
            .withTags(mapOf("tagK1", "tagV1"))
            .withProperties(new AutonomousDatabaseProperties().withAdminPassword("fakeTokenPlaceholder")
                .withCharacterSet("AL32UTF8")
                .withComputeCount(2.0F)
                .withComputeModel(ComputeModel.ECPU)
                .withDataStorageSizeInTbs(1)
                .withDbVersion("18.4.0.0")
                .withDisplayName("example_autonomous_databasedb1")
                .withNcharacterSet("AL16UTF16")
                .withSubnetId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg000/providers/Microsoft.Network/virtualNetworks/vnet1/subnets/subnet1")
                .withVnetId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg000/providers/Microsoft.Network/virtualNetworks/vnet1"))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/autonomousDatabase_create.json
     */
    /**
     * Sample code: Create Autonomous Database.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        createAutonomousDatabase(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabases()
            .define("databasedb1")
            .withRegion("eastus")
            .withExistingResourceGroup("rg000")
            .withTags(mapOf("tagK1", "tagV1"))
            .withProperties(new AutonomousDatabaseProperties().withAdminPassword("fakeTokenPlaceholder")
                .withCharacterSet("AL32UTF8")
                .withComputeCount(2.0F)
                .withComputeModel(ComputeModel.ECPU)
                .withDataStorageSizeInTbs(1)
                .withDbVersion("18.4.0.0")
                .withDisplayName("example_autonomous_databasedb1")
                .withNcharacterSet("AL16UTF16")
                .withSubnetId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg000/providers/Microsoft.Network/virtualNetworks/vnet1/subnets/subnet1")
                .withVnetId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg000/providers/Microsoft.Network/virtualNetworks/vnet1"))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/autonomousDatabaseClone_create.
     * json
     */
    /**
     * Sample code: Create clone Autonomous Database.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        createCloneAutonomousDatabase(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabases()
            .define("databasedb1")
            .withRegion("eastus")
            .withExistingResourceGroup("rg000")
            .withTags(mapOf("tagK1", "tagV1"))
            .withProperties(new AutonomousDatabaseCloneProperties().withAdminPassword("fakeTokenPlaceholder")
                .withCharacterSet("AL32UTF8")
                .withComputeCount(2.0F)
                .withComputeModel(ComputeModel.ECPU)
                .withDataStorageSizeInTbs(1)
                .withDisplayName("example_autonomous_databasedb1_clone")
                .withNcharacterSet("AL16UTF16")
                .withSubnetId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg000/providers/Microsoft.Network/virtualNetworks/vnet1/subnets/subnet1")
                .withVnetId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg000/providers/Microsoft.Network/virtualNetworks/vnet1")
                .withSourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg000/providers/Oracle.Database/autonomousDatabases/databasedb1")
                .withCloneType(CloneType.FULL))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/autonomousDatabaseClone_create.
     * json
     */
    /**
     * Sample code: AutonomousDatabases_CreateOrUpdate_clone.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        autonomousDatabasesCreateOrUpdateClone(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabases()
            .define("databasedb1")
            .withRegion("eastus")
            .withExistingResourceGroup("rg000")
            .withTags(mapOf("tagK1", "tagV1"))
            .withProperties(new AutonomousDatabaseCloneProperties().withAdminPassword("fakeTokenPlaceholder")
                .withCharacterSet("AL32UTF8")
                .withComputeCount(2.0F)
                .withComputeModel(ComputeModel.ECPU)
                .withDataStorageSizeInTbs(1)
                .withDisplayName("example_autonomous_databasedb1_clone")
                .withNcharacterSet("AL16UTF16")
                .withSubnetId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg000/providers/Microsoft.Network/virtualNetworks/vnet1/subnets/subnet1")
                .withVnetId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg000/providers/Microsoft.Network/virtualNetworks/vnet1")
                .withSourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg000/providers/Oracle.Database/autonomousDatabases/databasedb1")
                .withCloneType(CloneType.FULL))
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

### AutonomousDatabases_Delete

```java
/**
 * Samples for AutonomousDatabases Delete.
 */
public final class AutonomousDatabasesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/autonomousDatabase_delete.json
     */
    /**
     * Sample code: Delete Autonomous Database.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        deleteAutonomousDatabase(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabases().delete("rg000", "databasedb1", com.azure.core.util.Context.NONE);
    }
}
```

### AutonomousDatabases_Failover

```java
import com.azure.resourcemanager.oracledatabase.models.PeerDbDetails;

/**
 * Samples for AutonomousDatabases Failover.
 */
public final class AutonomousDatabasesFailoverSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/autonomousDatabase_failover.json
     */
    /**
     * Sample code: AutonomousDatabases_Failover.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        autonomousDatabasesFailover(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabases()
            .failover("rg000", "databasedb1", new PeerDbDetails().withPeerDbId("peerDbId"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/autonomousDatabase_failover.json
     */
    /**
     * Sample code: Perform failover action on Autonomous Database.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void performFailoverActionOnAutonomousDatabase(
        com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabases()
            .failover("rg000", "databasedb1", new PeerDbDetails().withPeerDbId("peerDbId"),
                com.azure.core.util.Context.NONE);
    }
}
```

### AutonomousDatabases_GenerateWallet

```java
import com.azure.resourcemanager.oracledatabase.models.GenerateAutonomousDatabaseWalletDetails;
import com.azure.resourcemanager.oracledatabase.models.GenerateType;

/**
 * Samples for AutonomousDatabases GenerateWallet.
 */
public final class AutonomousDatabasesGenerateWalletSamples {
    /*
     * x-ms-original-file: specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/
     * autonomousDatabase_generateWallet.json
     */
    /**
     * Sample code: Generate wallet action on Autonomous Database.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void generateWalletActionOnAutonomousDatabase(
        com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabases()
            .generateWalletWithResponse("rg000", "databasedb1",
                new GenerateAutonomousDatabaseWalletDetails().withGenerateType(GenerateType.SINGLE)
                    .withIsRegional(false)
                    .withPassword("fakeTokenPlaceholder"),
                com.azure.core.util.Context.NONE);
    }
}
```

### AutonomousDatabases_GetByResourceGroup

```java
/**
 * Samples for AutonomousDatabases GetByResourceGroup.
 */
public final class AutonomousDatabasesGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/autonomousDatabase_get.json
     */
    /**
     * Sample code: Get Autonomous Database.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void getAutonomousDatabase(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabases()
            .getByResourceGroupWithResponse("rg000", "databasedb1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/autonomousDatabase_get.json
     */
    /**
     * Sample code: AutonomousDatabases_Get.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void autonomousDatabasesGet(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabases()
            .getByResourceGroupWithResponse("rg000", "databasedb1", com.azure.core.util.Context.NONE);
    }
}
```

### AutonomousDatabases_List

```java
/**
 * Samples for AutonomousDatabases List.
 */
public final class AutonomousDatabasesListSamples {
    /*
     * x-ms-original-file: specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/
     * autonomousDatabase_listBySubscription.json
     */
    /**
     * Sample code: List Autonomous Database by subscription.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        listAutonomousDatabaseBySubscription(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabases().list(com.azure.core.util.Context.NONE);
    }
}
```

### AutonomousDatabases_ListByResourceGroup

```java
/**
 * Samples for AutonomousDatabases ListByResourceGroup.
 */
public final class AutonomousDatabasesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/
     * autonomousDatabase_listByResourceGroup.json
     */
    /**
     * Sample code: List Autonomous Database by resource group.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        listAutonomousDatabaseByResourceGroup(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabases().listByResourceGroup("rg000", com.azure.core.util.Context.NONE);
    }
}
```

### AutonomousDatabases_Restore

```java
import com.azure.resourcemanager.oracledatabase.models.RestoreAutonomousDatabaseDetails;
import java.time.OffsetDateTime;

/**
 * Samples for AutonomousDatabases Restore.
 */
public final class AutonomousDatabasesRestoreSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/autonomousDatabase_restore.json
     */
    /**
     * Sample code: AutonomousDatabases_Restore.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        autonomousDatabasesRestore(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabases()
            .restore("rg000", "databasedb1",
                new RestoreAutonomousDatabaseDetails().withTimestamp(OffsetDateTime.parse("2024-04-23T00:00:00.000Z")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/autonomousDatabase_restore.json
     */
    /**
     * Sample code: Perform restore action on Autonomous Database.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void performRestoreActionOnAutonomousDatabase(
        com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabases()
            .restore("rg000", "databasedb1",
                new RestoreAutonomousDatabaseDetails().withTimestamp(OffsetDateTime.parse("2024-04-23T00:00:00.000Z")),
                com.azure.core.util.Context.NONE);
    }
}
```

### AutonomousDatabases_Shrink

```java
/**
 * Samples for AutonomousDatabases Shrink.
 */
public final class AutonomousDatabasesShrinkSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/autonomousDatabase_shrink.json
     */
    /**
     * Sample code: Perform shrink action on Autonomous Database.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void performShrinkActionOnAutonomousDatabase(
        com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabases().shrink("rg000", "databasedb1", com.azure.core.util.Context.NONE);
    }
}
```

### AutonomousDatabases_Switchover

```java
import com.azure.resourcemanager.oracledatabase.models.PeerDbDetails;

/**
 * Samples for AutonomousDatabases Switchover.
 */
public final class AutonomousDatabasesSwitchoverSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/autonomousDatabase_switchover.
     * json
     */
    /**
     * Sample code: Perform switchover action on Autonomous Database.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void performSwitchoverActionOnAutonomousDatabase(
        com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabases()
            .switchover("rg000", "databasedb1", new PeerDbDetails().withPeerDbId("peerDbId"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/autonomousDatabase_switchover.
     * json
     */
    /**
     * Sample code: AutonomousDatabases_Switchover.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        autonomousDatabasesSwitchover(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.autonomousDatabases()
            .switchover("rg000", "databasedb1", new PeerDbDetails().withPeerDbId("peerDbId"),
                com.azure.core.util.Context.NONE);
    }
}
```

### AutonomousDatabases_Update

```java
import com.azure.resourcemanager.oracledatabase.models.AutonomousDatabase;

/**
 * Samples for AutonomousDatabases Update.
 */
public final class AutonomousDatabasesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/autonomousDatabase_patch.json
     */
    /**
     * Sample code: Patch Autonomous Database.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void patchAutonomousDatabase(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        AutonomousDatabase resource = manager.autonomousDatabases()
            .getByResourceGroupWithResponse("rg000", "databasedb1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }
}
```

### CloudExadataInfrastructures_AddStorageCapacity

```java
/**
 * Samples for CloudExadataInfrastructures AddStorageCapacity.
 */
public final class CloudExadataInfrastructuresAddStorageCapacitySamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/exaInfra_addStorageCapacity.json
     */
    /**
     * Sample code: Perform add storage capacity on exadata infra.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void performAddStorageCapacityOnExadataInfra(
        com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.cloudExadataInfrastructures().addStorageCapacity("rg000", "infra1", com.azure.core.util.Context.NONE);
    }
}
```

### CloudExadataInfrastructures_CreateOrUpdate

```java
import com.azure.resourcemanager.oracledatabase.models.CloudExadataInfrastructureProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for CloudExadataInfrastructures CreateOrUpdate.
 */
public final class CloudExadataInfrastructuresCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/exaInfra_create.json
     */
    /**
     * Sample code: Create Exadata Infrastructure.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        createExadataInfrastructure(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.cloudExadataInfrastructures()
            .define("infra1")
            .withRegion("eastus")
            .withExistingResourceGroup("rg000")
            .withZones(Arrays.asList("1"))
            .withTags(mapOf("tagK1", "tagV1"))
            .withProperties(new CloudExadataInfrastructureProperties().withComputeCount(100)
                .withStorageCount(10)
                .withShape("EXADATA.X9M")
                .withDisplayName("infra 1"))
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

### CloudExadataInfrastructures_Delete

```java
/**
 * Samples for CloudExadataInfrastructures Delete.
 */
public final class CloudExadataInfrastructuresDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/exaInfra_delete.json
     */
    /**
     * Sample code: Delete Exadata Infrastructure.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        deleteExadataInfrastructure(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.cloudExadataInfrastructures().delete("rg000", "infra1", com.azure.core.util.Context.NONE);
    }
}
```

### CloudExadataInfrastructures_GetByResourceGroup

```java
/**
 * Samples for CloudExadataInfrastructures GetByResourceGroup.
 */
public final class CloudExadataInfrastructuresGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/exaInfra_get.json
     */
    /**
     * Sample code: Get Exadata Infrastructure.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        getExadataInfrastructure(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.cloudExadataInfrastructures()
            .getByResourceGroupWithResponse("rg000", "infra1", com.azure.core.util.Context.NONE);
    }
}
```

### CloudExadataInfrastructures_List

```java
/**
 * Samples for CloudExadataInfrastructures List.
 */
public final class CloudExadataInfrastructuresListSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/exaInfra_listBySubscription.json
     */
    /**
     * Sample code: List Exadata Infrastructure by subscription.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void listExadataInfrastructureBySubscription(
        com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.cloudExadataInfrastructures().list(com.azure.core.util.Context.NONE);
    }
}
```

### CloudExadataInfrastructures_ListByResourceGroup

```java
/**
 * Samples for CloudExadataInfrastructures ListByResourceGroup.
 */
public final class CloudExadataInfrastructuresListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/exaInfra_listByResourceGroup.
     * json
     */
    /**
     * Sample code: List Exadata Infrastructure by resource group.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void listExadataInfrastructureByResourceGroup(
        com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.cloudExadataInfrastructures().listByResourceGroup("rg000", com.azure.core.util.Context.NONE);
    }
}
```

### CloudExadataInfrastructures_Update

```java
import com.azure.resourcemanager.oracledatabase.models.CloudExadataInfrastructure;

/**
 * Samples for CloudExadataInfrastructures Update.
 */
public final class CloudExadataInfrastructuresUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/exaInfra_patch.json
     */
    /**
     * Sample code: Patch Exadata Infrastructure.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        patchExadataInfrastructure(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        CloudExadataInfrastructure resource = manager.cloudExadataInfrastructures()
            .getByResourceGroupWithResponse("rg000", "infra1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }
}
```

### CloudVmClusters_AddVms

```java
import com.azure.resourcemanager.oracledatabase.models.AddRemoveDbNode;
import java.util.Arrays;

/**
 * Samples for CloudVmClusters AddVms.
 */
public final class CloudVmClustersAddVmsSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/vmClusters_addVms.json
     */
    /**
     * Sample code: Add VMs to VM Cluster.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void addVMsToVMCluster(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.cloudVmClusters()
            .addVms("rg000", "cluster1",
                new AddRemoveDbNode().withDbServers(Arrays.asList("ocid1..aaaa", "ocid1..aaaaaa")),
                com.azure.core.util.Context.NONE);
    }
}
```

### CloudVmClusters_CreateOrUpdate

```java
import com.azure.resourcemanager.oracledatabase.models.CloudVmClusterProperties;
import com.azure.resourcemanager.oracledatabase.models.DataCollectionOptions;
import com.azure.resourcemanager.oracledatabase.models.LicenseModel;
import com.azure.resourcemanager.oracledatabase.models.NsgCidr;
import com.azure.resourcemanager.oracledatabase.models.PortRange;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for CloudVmClusters CreateOrUpdate.
 */
public final class CloudVmClustersCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/vmClusters_create.json
     */
    /**
     * Sample code: Create VM Cluster.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void createVMCluster(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.cloudVmClusters()
            .define("cluster1")
            .withRegion("eastus")
            .withExistingResourceGroup("rg000")
            .withTags(mapOf("tagK1", "tagV1"))
            .withProperties(new CloudVmClusterProperties().withDataStorageSizeInTbs(1000.0D)
                .withDbNodeStorageSizeInGbs(1000)
                .withMemorySizeInGbs(1000)
                .withTimeZone("UTC")
                .withHostname("hostname1")
                .withDomain("domain1")
                .withCpuCoreCount(2)
                .withOcpuCount(3.0F)
                .withClusterName("cluster1")
                .withDataStoragePercentage(100)
                .withIsLocalBackupEnabled(false)
                .withCloudExadataInfrastructureId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg000/providers/Oracle.Database/cloudExadataInfrastructures/infra1")
                .withIsSparseDiskgroupEnabled(false)
                .withSshPublicKeys(Arrays.asList("ssh-key 1"))
                .withLicenseModel(LicenseModel.LICENSE_INCLUDED)
                .withScanListenerPortTcp(1050)
                .withScanListenerPortTcpSsl(1025)
                .withVnetId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg000/providers/Microsoft.Network/virtualNetworks/vnet1")
                .withGiVersion("19.0.0.0")
                .withSubnetId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg000/providers/Microsoft.Network/virtualNetworks/vnet1/subnets/subnet1")
                .withBackupSubnetCidr("172.17.5.0/24")
                .withNsgCidrs(Arrays.asList(
                    new NsgCidr().withSource("10.0.0.0/16")
                        .withDestinationPortRange(new PortRange().withMin(1520).withMax(1522)),
                    new NsgCidr().withSource("10.10.0.0/24")))
                .withDataCollectionOptions(new DataCollectionOptions().withIsDiagnosticsEventsEnabled(false)
                    .withIsHealthMonitoringEnabled(false)
                    .withIsIncidentLogsEnabled(false))
                .withDisplayName("cluster 1")
                .withDbServers(Arrays.asList("ocid1..aaaa")))
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

### CloudVmClusters_Delete

```java
/**
 * Samples for CloudVmClusters Delete.
 */
public final class CloudVmClustersDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/vmClusters_delete.json
     */
    /**
     * Sample code: Delete VM Cluster.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void deleteVMCluster(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.cloudVmClusters().delete("rg000", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### CloudVmClusters_GetByResourceGroup

```java
/**
 * Samples for CloudVmClusters GetByResourceGroup.
 */
public final class CloudVmClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/vmClusters_get.json
     */
    /**
     * Sample code: Get VM Cluster.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void getVMCluster(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.cloudVmClusters().getByResourceGroupWithResponse("rg000", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### CloudVmClusters_List

```java
/**
 * Samples for CloudVmClusters List.
 */
public final class CloudVmClustersListSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/vmClusters_listBySubscription.
     * json
     */
    /**
     * Sample code: List VM Clusters by subscription.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        listVMClustersBySubscription(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.cloudVmClusters().list(com.azure.core.util.Context.NONE);
    }
}
```

### CloudVmClusters_ListByResourceGroup

```java
/**
 * Samples for CloudVmClusters ListByResourceGroup.
 */
public final class CloudVmClustersListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/vmClusters_listByResourceGroup.
     * json
     */
    /**
     * Sample code: List VM Clusters by resource group.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        listVMClustersByResourceGroup(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.cloudVmClusters().listByResourceGroup("rg000", com.azure.core.util.Context.NONE);
    }
}
```

### CloudVmClusters_ListPrivateIpAddresses

```java
import com.azure.resourcemanager.oracledatabase.models.PrivateIpAddressesFilter;

/**
 * Samples for CloudVmClusters ListPrivateIpAddresses.
 */
public final class CloudVmClustersListPrivateIpAddressesSamples {
    /*
     * x-ms-original-file: specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/
     * vmClusters_listPrivateIpAddresses.json
     */
    /**
     * Sample code: List Private IP Addresses for VM Cluster.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        listPrivateIPAddressesForVMCluster(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.cloudVmClusters()
            .listPrivateIpAddressesWithResponse("rg000", "cluster1",
                new PrivateIpAddressesFilter().withSubnetId("ocid1..aaaaaa").withVnicId("ocid1..aaaaa"),
                com.azure.core.util.Context.NONE);
    }
}
```

### CloudVmClusters_RemoveVms

```java
import com.azure.resourcemanager.oracledatabase.models.AddRemoveDbNode;
import java.util.Arrays;

/**
 * Samples for CloudVmClusters RemoveVms.
 */
public final class CloudVmClustersRemoveVmsSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/vmClusters_removeVms.json
     */
    /**
     * Sample code: Remove VMs from VM Cluster.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void removeVMsFromVMCluster(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.cloudVmClusters()
            .removeVms("rg000", "cluster1", new AddRemoveDbNode().withDbServers(Arrays.asList("ocid1..aaaa")),
                com.azure.core.util.Context.NONE);
    }
}
```

### CloudVmClusters_Update

```java
import com.azure.resourcemanager.oracledatabase.models.CloudVmCluster;

/**
 * Samples for CloudVmClusters Update.
 */
public final class CloudVmClustersUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/vmClusters_patch.json
     */
    /**
     * Sample code: Patch VM Cluster.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void patchVMCluster(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        CloudVmCluster resource = manager.cloudVmClusters()
            .getByResourceGroupWithResponse("rg000", "cluster1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }
}
```

### DbNodes_Action

```java
import com.azure.resourcemanager.oracledatabase.models.DbNodeAction;
import com.azure.resourcemanager.oracledatabase.models.DbNodeActionEnum;

/**
 * Samples for DbNodes Action.
 */
public final class DbNodesActionSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/dbNodes_action.json
     */
    /**
     * Sample code: DbNodes_Action.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void dbNodesAction(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.dbNodes()
            .action("rg000", "cluster1", "ocid1....aaaaaa", new DbNodeAction().withAction(DbNodeActionEnum.START),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/dbNodes_action.json
     */
    /**
     * Sample code: VM actions on DbNodes of VM Cluster.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        vmActionsOnDbNodesOfVMCluster(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.dbNodes()
            .action("rg000", "cluster1", "ocid1....aaaaaa", new DbNodeAction().withAction(DbNodeActionEnum.START),
                com.azure.core.util.Context.NONE);
    }
}
```

### DbNodes_Get

```java
/**
 * Samples for DbNodes Get.
 */
public final class DbNodesGetSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/dbNodes_get.json
     */
    /**
     * Sample code: Get DbNode.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void getDbNode(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.dbNodes().getWithResponse("rg000", "cluster1", "ocid1....aaaaaa", com.azure.core.util.Context.NONE);
    }
}
```

### DbNodes_ListByCloudVmCluster

```java
/**
 * Samples for DbNodes ListByCloudVmCluster.
 */
public final class DbNodesListByCloudVmClusterSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/dbNodes_listByParent.json
     */
    /**
     * Sample code: List DbNodes by VM Cluster.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void listDbNodesByVMCluster(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.dbNodes().listByCloudVmCluster("rg000", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### DbServers_Get

```java
/**
 * Samples for DbServers Get.
 */
public final class DbServersGetSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/dbServers_get.json
     */
    /**
     * Sample code: Get DbServer by parent.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void getDbServerByParent(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.dbServers().getWithResponse("rg000", "infra1", "ocid1....aaaaaa", com.azure.core.util.Context.NONE);
    }
}
```

### DbServers_ListByCloudExadataInfrastructure

```java
/**
 * Samples for DbServers ListByCloudExadataInfrastructure.
 */
public final class DbServersListByCloudExadataInfrastructureSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/dbServers_listByParent.json
     */
    /**
     * Sample code: List DbServers by Exadata Infrastructure.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        listDbServersByExadataInfrastructure(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.dbServers().listByCloudExadataInfrastructure("rg000", "infra1", com.azure.core.util.Context.NONE);
    }
}
```

### DbSystemShapes_Get

```java
/**
 * Samples for DbSystemShapes Get.
 */
public final class DbSystemShapesGetSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/dbSystemShapes_get.json
     */
    /**
     * Sample code: Get a DbSystemShape by name.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void getADbSystemShapeByName(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.dbSystemShapes().getWithResponse("eastus", "EXADATA.X9M", com.azure.core.util.Context.NONE);
    }
}
```

### DbSystemShapes_ListByLocation

```java
/**
 * Samples for DbSystemShapes ListByLocation.
 */
public final class DbSystemShapesListByLocationSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/dbSystemShapes_listByLocation.
     * json
     */
    /**
     * Sample code: List DbSystemShapes by location.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        listDbSystemShapesByLocation(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.dbSystemShapes().listByLocation("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### DnsPrivateViews_Get

```java
/**
 * Samples for DnsPrivateViews Get.
 */
public final class DnsPrivateViewsGetSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/dnsPrivateViews_get.json
     */
    /**
     * Sample code: Get a DnsPrivateView by name.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        getADnsPrivateViewByName(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.dnsPrivateViews().getWithResponse("eastus", "ocid1....aaaaaa", com.azure.core.util.Context.NONE);
    }
}
```

### DnsPrivateViews_ListByLocation

```java
/**
 * Samples for DnsPrivateViews ListByLocation.
 */
public final class DnsPrivateViewsListByLocationSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/dnsPrivateViews_listByLocation.
     * json
     */
    /**
     * Sample code: List DnsPrivateViews by location.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        listDnsPrivateViewsByLocation(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.dnsPrivateViews().listByLocation("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### DnsPrivateZones_Get

```java
/**
 * Samples for DnsPrivateZones Get.
 */
public final class DnsPrivateZonesGetSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/dnsPrivateZones_get.json
     */
    /**
     * Sample code: Get a DnsPrivateZone by name.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        getADnsPrivateZoneByName(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.dnsPrivateZones()
            .getWithResponse("eastus", "example-dns-private-zone", com.azure.core.util.Context.NONE);
    }
}
```

### DnsPrivateZones_ListByLocation

```java
/**
 * Samples for DnsPrivateZones ListByLocation.
 */
public final class DnsPrivateZonesListByLocationSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/dnsPrivateZones_listByLocation.
     * json
     */
    /**
     * Sample code: List DnsPrivateZones by location.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        listDnsPrivateZonesByLocation(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.dnsPrivateZones().listByLocation("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### GiVersions_Get

```java
/**
 * Samples for GiVersions Get.
 */
public final class GiVersionsGetSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/giVersions_get.json
     */
    /**
     * Sample code: Get a GiVersion by name.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void getAGiVersionByName(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.giVersions().getWithResponse("eastus", "19.0.0.0", com.azure.core.util.Context.NONE);
    }
}
```

### GiVersions_ListByLocation

```java
/**
 * Samples for GiVersions ListByLocation.
 */
public final class GiVersionsListByLocationSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/giVersions_listByLocation.json
     */
    /**
     * Sample code: List GiVersions by location.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        listGiVersionsByLocation(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.giVersions().listByLocation("eastus", com.azure.core.util.Context.NONE);
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
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/operations_list.json
     */
    /**
     * Sample code: List Operations.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void listOperations(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### OracleSubscriptions_CreateOrUpdate

```java
import com.azure.resourcemanager.oracledatabase.fluent.models.OracleSubscriptionInner;
import com.azure.resourcemanager.oracledatabase.models.OracleSubscriptionProperties;
import com.azure.resourcemanager.oracledatabase.models.Plan;

/**
 * Samples for OracleSubscriptions CreateOrUpdate.
 */
public final class OracleSubscriptionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/oracleSubscriptions_create.json
     */
    /**
     * Sample code: Create or Update Oracle Subscription.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        createOrUpdateOracleSubscription(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.oracleSubscriptions()
            .createOrUpdate(new OracleSubscriptionInner().withProperties(new OracleSubscriptionProperties())
                .withPlan(new Plan().withName("plan1")
                    .withPublisher("publisher1")
                    .withProduct("product1")
                    .withPromotionCode("fakeTokenPlaceholder")
                    .withVersion("alpha")),
                com.azure.core.util.Context.NONE);
    }
}
```

### OracleSubscriptions_Delete

```java
/**
 * Samples for OracleSubscriptions Delete.
 */
public final class OracleSubscriptionsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/oracleSubscriptions_delete.json
     */
    /**
     * Sample code: Delete Oracle Subscription.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        deleteOracleSubscription(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.oracleSubscriptions().delete(com.azure.core.util.Context.NONE);
    }
}
```

### OracleSubscriptions_Get

```java
/**
 * Samples for OracleSubscriptions Get.
 */
public final class OracleSubscriptionsGetSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/oracleSubscriptions_get.json
     */
    /**
     * Sample code: Get Oracle Subscription.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void getOracleSubscription(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.oracleSubscriptions().getWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### OracleSubscriptions_List

```java
/**
 * Samples for OracleSubscriptions List.
 */
public final class OracleSubscriptionsListSamples {
    /*
     * x-ms-original-file: specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/
     * oracleSubscriptions_listBySubscription.json
     */
    /**
     * Sample code: List Oracle Subscriptions by subscription.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        listOracleSubscriptionsBySubscription(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.oracleSubscriptions().list(com.azure.core.util.Context.NONE);
    }
}
```

### OracleSubscriptions_ListActivationLinks

```java
/**
 * Samples for OracleSubscriptions ListActivationLinks.
 */
public final class OracleSubscriptionsListActivationLinksSamples {
    /*
     * x-ms-original-file: specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/
     * oracleSubscriptions_listActivationLinks.json
     */
    /**
     * Sample code: List Activation Links for the Oracle Subscription.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void listActivationLinksForTheOracleSubscription(
        com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.oracleSubscriptions().listActivationLinks(com.azure.core.util.Context.NONE);
    }
}
```

### OracleSubscriptions_ListCloudAccountDetails

```java
/**
 * Samples for OracleSubscriptions ListCloudAccountDetails.
 */
public final class OracleSubscriptionsListCloudAccountDetailsSamples {
    /*
     * x-ms-original-file: specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/
     * oracleSubscriptions_listCloudAccountDetails.json
     */
    /**
     * Sample code: List Cloud Account details for the Oracle Subscription.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void listCloudAccountDetailsForTheOracleSubscription(
        com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.oracleSubscriptions().listCloudAccountDetails(com.azure.core.util.Context.NONE);
    }
}
```

### OracleSubscriptions_ListSaasSubscriptionDetails

```java
/**
 * Samples for OracleSubscriptions ListSaasSubscriptionDetails.
 */
public final class OracleSubscriptionsListSaasSubscriptionDetailsSamples {
    /*
     * x-ms-original-file: specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/
     * oracleSubscriptions_listSaasSubscriptionDetails.json
     */
    /**
     * Sample code: List Saas Subscription details for the Oracle Subscription.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void listSaasSubscriptionDetailsForTheOracleSubscription(
        com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.oracleSubscriptions().listSaasSubscriptionDetails(com.azure.core.util.Context.NONE);
    }
}
```

### OracleSubscriptions_Update

```java
import com.azure.resourcemanager.oracledatabase.models.OracleSubscriptionUpdate;

/**
 * Samples for OracleSubscriptions Update.
 */
public final class OracleSubscriptionsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/oracleSubscriptions_patch.json
     */
    /**
     * Sample code: Patch Oracle Subscription.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void patchOracleSubscription(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.oracleSubscriptions().update(new OracleSubscriptionUpdate(), com.azure.core.util.Context.NONE);
    }
}
```

### SystemVersions_Get

```java
/**
 * Samples for SystemVersions Get.
 */
public final class SystemVersionsGetSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/systemVersions_get.json
     */
    /**
     * Sample code: systemVersions_listSystemVersions.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        systemVersionsListSystemVersions(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.systemVersions().getWithResponse("eastus", "22.x", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/systemVersions_get.json
     */
    /**
     * Sample code: Get Exadata System Version.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void getExadataSystemVersion(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.systemVersions().getWithResponse("eastus", "22.x", com.azure.core.util.Context.NONE);
    }
}
```

### SystemVersions_ListByLocation

```java
/**
 * Samples for SystemVersions ListByLocation.
 */
public final class SystemVersionsListByLocationSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/systemVersions_listByLocation.
     * json
     */
    /**
     * Sample code: List Exadata System Versions by the provided filter.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void listExadataSystemVersionsByTheProvidedFilter(
        com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.systemVersions().listByLocation("eastus", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/systemVersions_listByLocation.
     * json
     */
    /**
     * Sample code: systemVersions_listByLocation.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        systemVersionsListByLocation(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.systemVersions().listByLocation("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworkAddresses_CreateOrUpdate

```java
import com.azure.resourcemanager.oracledatabase.models.VirtualNetworkAddressProperties;

/**
 * Samples for VirtualNetworkAddresses CreateOrUpdate.
 */
public final class VirtualNetworkAddressesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/virtualNetworkAddresses_create.
     * json
     */
    /**
     * Sample code: Create Virtual Network Address.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        createVirtualNetworkAddress(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.virtualNetworkAddresses()
            .define("hostname1")
            .withExistingCloudVmCluster("rg000", "cluster1")
            .withProperties(
                new VirtualNetworkAddressProperties().withIpAddress("192.168.0.1").withVmOcid("ocid1..aaaa"))
            .create();
    }
}
```

### VirtualNetworkAddresses_Delete

```java
/**
 * Samples for VirtualNetworkAddresses Delete.
 */
public final class VirtualNetworkAddressesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/virtualNetworkAddresses_delete.
     * json
     */
    /**
     * Sample code: Delete Virtual Network Address.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        deleteVirtualNetworkAddress(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.virtualNetworkAddresses().delete("rg000", "cluster1", "hostname1", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworkAddresses_Get

```java
/**
 * Samples for VirtualNetworkAddresses Get.
 */
public final class VirtualNetworkAddressesGetSamples {
    /*
     * x-ms-original-file:
     * specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/virtualNetworkAddresses_get.json
     */
    /**
     * Sample code: Get Virtual Network Address.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        getVirtualNetworkAddress(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.virtualNetworkAddresses()
            .getWithResponse("rg000", "cluster1", "hostname1", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworkAddresses_ListByCloudVmCluster

```java
/**
 * Samples for VirtualNetworkAddresses ListByCloudVmCluster.
 */
public final class VirtualNetworkAddressesListByCloudVmClusterSamples {
    /*
     * x-ms-original-file: specification/oracle/resource-manager/Oracle.Database/stable/2023-09-01/examples/
     * virtualNetworkAddresses_listByParent.json
     */
    /**
     * Sample code: List Virtual Network Addresses by VM Cluster.
     * 
     * @param manager Entry point to OracleDatabaseManager.
     */
    public static void
        listVirtualNetworkAddressesByVMCluster(com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager) {
        manager.virtualNetworkAddresses().listByCloudVmCluster("rg000", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

