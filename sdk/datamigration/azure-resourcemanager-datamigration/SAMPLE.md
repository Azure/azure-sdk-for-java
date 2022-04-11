# Code snippets and samples


## DatabaseMigrationsSqlDb

- [Cancel](#databasemigrationssqldb_cancel)
- [CreateOrUpdate](#databasemigrationssqldb_createorupdate)
- [Delete](#databasemigrationssqldb_delete)
- [Get](#databasemigrationssqldb_get)

## DatabaseMigrationsSqlMi

- [Cancel](#databasemigrationssqlmi_cancel)
- [CreateOrUpdate](#databasemigrationssqlmi_createorupdate)
- [Cutover](#databasemigrationssqlmi_cutover)
- [Get](#databasemigrationssqlmi_get)

## DatabaseMigrationsSqlVm

- [Cancel](#databasemigrationssqlvm_cancel)
- [CreateOrUpdate](#databasemigrationssqlvm_createorupdate)
- [Cutover](#databasemigrationssqlvm_cutover)
- [Get](#databasemigrationssqlvm_get)

## Files

- [CreateOrUpdate](#files_createorupdate)
- [Delete](#files_delete)
- [Get](#files_get)
- [List](#files_list)
- [Read](#files_read)
- [ReadWrite](#files_readwrite)
- [Update](#files_update)

## Operations

- [List](#operations_list)

## Projects

- [CreateOrUpdate](#projects_createorupdate)
- [Delete](#projects_delete)
- [Get](#projects_get)
- [List](#projects_list)
- [Update](#projects_update)

## ResourceSkus

- [List](#resourceskus_list)

## ServiceTasks

- [Cancel](#servicetasks_cancel)
- [CreateOrUpdate](#servicetasks_createorupdate)
- [Delete](#servicetasks_delete)
- [Get](#servicetasks_get)
- [List](#servicetasks_list)
- [Update](#servicetasks_update)

## Services

- [CheckChildrenNameAvailability](#services_checkchildrennameavailability)
- [CheckNameAvailability](#services_checknameavailability)
- [CheckStatus](#services_checkstatus)
- [CreateOrUpdate](#services_createorupdate)
- [Delete](#services_delete)
- [GetByResourceGroup](#services_getbyresourcegroup)
- [List](#services_list)
- [ListByResourceGroup](#services_listbyresourcegroup)
- [ListSkus](#services_listskus)
- [Start](#services_start)
- [Stop](#services_stop)
- [Update](#services_update)

## SqlMigrationServices

- [CreateOrUpdate](#sqlmigrationservices_createorupdate)
- [Delete](#sqlmigrationservices_delete)
- [DeleteNode](#sqlmigrationservices_deletenode)
- [GetByResourceGroup](#sqlmigrationservices_getbyresourcegroup)
- [List](#sqlmigrationservices_list)
- [ListAuthKeys](#sqlmigrationservices_listauthkeys)
- [ListByResourceGroup](#sqlmigrationservices_listbyresourcegroup)
- [ListMigrations](#sqlmigrationservices_listmigrations)
- [ListMonitoringData](#sqlmigrationservices_listmonitoringdata)
- [RegenerateAuthKeys](#sqlmigrationservices_regenerateauthkeys)
- [Update](#sqlmigrationservices_update)

## Tasks

- [Cancel](#tasks_cancel)
- [Command](#tasks_command)
- [CreateOrUpdate](#tasks_createorupdate)
- [Delete](#tasks_delete)
- [Get](#tasks_get)
- [List](#tasks_list)
- [Update](#tasks_update)

## Usages

- [List](#usages_list)
### DatabaseMigrationsSqlDb_Cancel

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datamigration.models.MigrationOperationInput;
import java.util.UUID;

/** Samples for DatabaseMigrationsSqlDb Cancel. */
public final class DatabaseMigrationsSqlDbCancelSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/SqlDbCancelDatabaseMigration.json
     */
    /**
     * Sample code: Stop ongoing migration for the database.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void stopOngoingMigrationForTheDatabase(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .databaseMigrationsSqlDbs()
            .cancel(
                "testrg",
                "sqldbinstance",
                "db1",
                new MigrationOperationInput()
                    .withMigrationOperationId(UUID.fromString("9a90bb84-e70f-46f7-b0ae-1aef5b3b9f07")),
                Context.NONE);
    }
}
```

### DatabaseMigrationsSqlDb_CreateOrUpdate

```java
import com.azure.resourcemanager.datamigration.models.DatabaseMigrationPropertiesSqlDb;
import com.azure.resourcemanager.datamigration.models.SqlConnectionInformation;
import java.util.Arrays;

/** Samples for DatabaseMigrationsSqlDb CreateOrUpdate. */
public final class DatabaseMigrationsSqlDbCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/SqlDbCreateOrUpdateDatabaseMigrationMAX.json
     */
    /**
     * Sample code: Create or Update Database Migration resource with Maximum parameters.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void createOrUpdateDatabaseMigrationResourceWithMaximumParameters(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .databaseMigrationsSqlDbs()
            .define("db1")
            .withExistingServer("testrg", "sqldbinstance")
            .withProperties(
                new DatabaseMigrationPropertiesSqlDb()
                    .withScope(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Sql/servers/sqldbinstance")
                    .withSourceSqlConnection(
                        new SqlConnectionInformation()
                            .withDataSource("aaa")
                            .withAuthentication("WindowsAuthentication")
                            .withUsername("bbb")
                            .withPassword("placeholder")
                            .withEncryptConnection(true)
                            .withTrustServerCertificate(true))
                    .withSourceDatabaseName("aaa")
                    .withMigrationService(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.DataMigration/sqlMigrationServices/testagent")
                    .withTargetSqlConnection(
                        new SqlConnectionInformation()
                            .withDataSource("sqldbinstance")
                            .withAuthentication("SqlAuthentication")
                            .withUsername("bbb")
                            .withPassword("placeholder")
                            .withEncryptConnection(true)
                            .withTrustServerCertificate(true))
                    .withTableList(Arrays.asList("[Schema1].[TableName1]", "[Schema2].[TableName2]")))
            .create();
    }

    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/SqlDbCreateOrUpdateDatabaseMigrationMIN.json
     */
    /**
     * Sample code: Create or Update Database Migration resource with Minimum parameters.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void createOrUpdateDatabaseMigrationResourceWithMinimumParameters(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .databaseMigrationsSqlDbs()
            .define("db1")
            .withExistingServer("testrg", "sqldbinstance")
            .withProperties(
                new DatabaseMigrationPropertiesSqlDb()
                    .withScope(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Sql/servers/sqldbinstance")
                    .withSourceSqlConnection(
                        new SqlConnectionInformation()
                            .withDataSource("aaa")
                            .withAuthentication("WindowsAuthentication")
                            .withUsername("bbb")
                            .withPassword("placeholder")
                            .withEncryptConnection(true)
                            .withTrustServerCertificate(true))
                    .withSourceDatabaseName("aaa")
                    .withMigrationService(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.DataMigration/sqlMigrationServices/testagent")
                    .withTargetSqlConnection(
                        new SqlConnectionInformation()
                            .withDataSource("sqldbinstance")
                            .withAuthentication("SqlAuthentication")
                            .withUsername("bbb")
                            .withPassword("placeholder")
                            .withEncryptConnection(true)
                            .withTrustServerCertificate(true)))
            .create();
    }
}
```

### DatabaseMigrationsSqlDb_Delete

```java
import com.azure.core.util.Context;

/** Samples for DatabaseMigrationsSqlDb Delete. */
public final class DatabaseMigrationsSqlDbDeleteSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/SqlDbDeleteDatabaseMigration.json
     */
    /**
     * Sample code: Delete Database Migration resource.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void deleteDatabaseMigrationResource(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.databaseMigrationsSqlDbs().delete("testrg", "sqldbinstance", "db1", null, Context.NONE);
    }
}
```

### DatabaseMigrationsSqlDb_Get

```java
import com.azure.core.util.Context;

/** Samples for DatabaseMigrationsSqlDb Get. */
public final class DatabaseMigrationsSqlDbGetSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/SqlDbGetDatabaseMigration.json
     */
    /**
     * Sample code: Get Sql DB database Migration without the expand parameter.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void getSqlDBDatabaseMigrationWithoutTheExpandParameter(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.databaseMigrationsSqlDbs().getWithResponse("testrg", "sqldbinstance", "db1", null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/SqlDbGetDatabaseMigrationExpanded.json
     */
    /**
     * Sample code: Get Sql DB database Migration with the expand parameter.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void getSqlDBDatabaseMigrationWithTheExpandParameter(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .databaseMigrationsSqlDbs()
            .getWithResponse("testrg", "sqldbinstance", "db1", null, "MigrationStatusDetails", Context.NONE);
    }
}
```

### DatabaseMigrationsSqlMi_Cancel

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datamigration.models.MigrationOperationInput;
import java.util.UUID;

/** Samples for DatabaseMigrationsSqlMi Cancel. */
public final class DatabaseMigrationsSqlMiCancelSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/SqlMiCancelDatabaseMigration.json
     */
    /**
     * Sample code: Stop ongoing migration for the database.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void stopOngoingMigrationForTheDatabase(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .databaseMigrationsSqlMis()
            .cancel(
                "testrg",
                "managedInstance1",
                "db1",
                new MigrationOperationInput()
                    .withMigrationOperationId(UUID.fromString("4124fe90-d1b6-4b50-b4d9-46d02381f59a")),
                Context.NONE);
    }
}
```

### DatabaseMigrationsSqlMi_CreateOrUpdate

```java
import com.azure.resourcemanager.datamigration.models.BackupConfiguration;
import com.azure.resourcemanager.datamigration.models.DatabaseMigrationPropertiesSqlMi;
import com.azure.resourcemanager.datamigration.models.OfflineConfiguration;
import com.azure.resourcemanager.datamigration.models.SourceLocation;
import com.azure.resourcemanager.datamigration.models.SqlConnectionInformation;
import com.azure.resourcemanager.datamigration.models.SqlFileShare;
import com.azure.resourcemanager.datamigration.models.TargetLocation;

/** Samples for DatabaseMigrationsSqlMi CreateOrUpdate. */
public final class DatabaseMigrationsSqlMiCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/SqlMiCreateOrUpdateDatabaseMigrationMAX.json
     */
    /**
     * Sample code: Create or Update Database Migration resource with Maximum parameters.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void createOrUpdateDatabaseMigrationResourceWithMaximumParameters(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .databaseMigrationsSqlMis()
            .define("db1")
            .withExistingManagedInstance("testrg", "managedInstance1")
            .withProperties(
                new DatabaseMigrationPropertiesSqlMi()
                    .withScope(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Sql/managedInstances/instance")
                    .withSourceSqlConnection(
                        new SqlConnectionInformation()
                            .withDataSource("aaa")
                            .withAuthentication("WindowsAuthentication")
                            .withUsername("bbb")
                            .withPassword("placeholder")
                            .withEncryptConnection(true)
                            .withTrustServerCertificate(true))
                    .withSourceDatabaseName("aaa")
                    .withMigrationService(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.DataMigration/sqlMigrationServices/testagent")
                    .withBackupConfiguration(
                        new BackupConfiguration()
                            .withSourceLocation(
                                new SourceLocation()
                                    .withFileShare(
                                        new SqlFileShare()
                                            .withPath("C:\\aaa\\bbb\\ccc")
                                            .withUsername("name")
                                            .withPassword("placeholder")))
                            .withTargetLocation(
                                new TargetLocation()
                                    .withStorageAccountResourceId("account.database.windows.net")
                                    .withAccountKey("abcd")))
                    .withOfflineConfiguration(
                        new OfflineConfiguration().withOffline(true).withLastBackupName("last_backup_file_name")))
            .create();
    }

    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/SqlMiCreateOrUpdateDatabaseMigrationMIN.json
     */
    /**
     * Sample code: Create or Update Database Migration resource with Minimum parameters.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void createOrUpdateDatabaseMigrationResourceWithMinimumParameters(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .databaseMigrationsSqlMis()
            .define("db1")
            .withExistingManagedInstance("testrg", "managedInstance1")
            .withProperties(
                new DatabaseMigrationPropertiesSqlMi()
                    .withScope(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Sql/managedInstances/instance")
                    .withSourceSqlConnection(
                        new SqlConnectionInformation()
                            .withDataSource("aaa")
                            .withAuthentication("WindowsAuthentication")
                            .withUsername("bbb")
                            .withPassword("placeholder")
                            .withEncryptConnection(true)
                            .withTrustServerCertificate(true))
                    .withSourceDatabaseName("aaa")
                    .withMigrationService(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.DataMigration/sqlMigrationServices/testagent")
                    .withBackupConfiguration(
                        new BackupConfiguration()
                            .withSourceLocation(
                                new SourceLocation()
                                    .withFileShare(
                                        new SqlFileShare()
                                            .withPath("C:\\aaa\\bbb\\ccc")
                                            .withUsername("name")
                                            .withPassword("placeholder")))
                            .withTargetLocation(
                                new TargetLocation()
                                    .withStorageAccountResourceId("account.database.windows.net")
                                    .withAccountKey("abcd"))))
            .create();
    }
}
```

### DatabaseMigrationsSqlMi_Cutover

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datamigration.models.MigrationOperationInput;
import java.util.UUID;

/** Samples for DatabaseMigrationsSqlMi Cutover. */
public final class DatabaseMigrationsSqlMiCutoverSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/SqlMiCutoverDatabaseMigration.json
     */
    /**
     * Sample code: Cutover online migration operation for the database.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void cutoverOnlineMigrationOperationForTheDatabase(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .databaseMigrationsSqlMis()
            .cutover(
                "testrg",
                "managedInstance1",
                "db1",
                new MigrationOperationInput()
                    .withMigrationOperationId(UUID.fromString("4124fe90-d1b6-4b50-b4d9-46d02381f59a")),
                Context.NONE);
    }
}
```

### DatabaseMigrationsSqlMi_Get

```java
import com.azure.core.util.Context;

/** Samples for DatabaseMigrationsSqlMi Get. */
public final class DatabaseMigrationsSqlMiGetSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/SqlMiGetDatabaseMigrationExpanded.json
     */
    /**
     * Sample code: Get Sql MI database Migration with the expand parameter.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void getSqlMIDatabaseMigrationWithTheExpandParameter(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .databaseMigrationsSqlMis()
            .getWithResponse("testrg", "managedInstance1", "db1", null, "MigrationStatusDetails", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/SqlMiGetDatabaseMigration.json
     */
    /**
     * Sample code: Get Sql MI database Migration without the expand parameter.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void getSqlMIDatabaseMigrationWithoutTheExpandParameter(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .databaseMigrationsSqlMis()
            .getWithResponse("testrg", "managedInstance1", "db1", null, null, Context.NONE);
    }
}
```

### DatabaseMigrationsSqlVm_Cancel

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datamigration.models.MigrationOperationInput;
import java.util.UUID;

/** Samples for DatabaseMigrationsSqlVm Cancel. */
public final class DatabaseMigrationsSqlVmCancelSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/SqlVmCancelDatabaseMigration.json
     */
    /**
     * Sample code: Stop ongoing migration for the database.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void stopOngoingMigrationForTheDatabase(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .databaseMigrationsSqlVms()
            .cancel(
                "testrg",
                "testvm",
                "db1",
                new MigrationOperationInput()
                    .withMigrationOperationId(UUID.fromString("4124fe90-d1b6-4b50-b4d9-46d02381f59a")),
                Context.NONE);
    }
}
```

### DatabaseMigrationsSqlVm_CreateOrUpdate

```java
import com.azure.resourcemanager.datamigration.models.BackupConfiguration;
import com.azure.resourcemanager.datamigration.models.DatabaseMigrationPropertiesSqlVm;
import com.azure.resourcemanager.datamigration.models.OfflineConfiguration;
import com.azure.resourcemanager.datamigration.models.SourceLocation;
import com.azure.resourcemanager.datamigration.models.SqlConnectionInformation;
import com.azure.resourcemanager.datamigration.models.SqlFileShare;
import com.azure.resourcemanager.datamigration.models.TargetLocation;

/** Samples for DatabaseMigrationsSqlVm CreateOrUpdate. */
public final class DatabaseMigrationsSqlVmCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/SqlVmCreateOrUpdateDatabaseMigrationMAX.json
     */
    /**
     * Sample code: Create or Update Database Migration resource with Maximum parameters.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void createOrUpdateDatabaseMigrationResourceWithMaximumParameters(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .databaseMigrationsSqlVms()
            .define("db1")
            .withExistingSqlVirtualMachine("testrg", "testvm")
            .withProperties(
                new DatabaseMigrationPropertiesSqlVm()
                    .withScope(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.SqlVirtualMachine/sqlVirtualMachines/testvm")
                    .withSourceSqlConnection(
                        new SqlConnectionInformation()
                            .withDataSource("aaa")
                            .withAuthentication("WindowsAuthentication")
                            .withUsername("bbb")
                            .withPassword("placeholder")
                            .withEncryptConnection(true)
                            .withTrustServerCertificate(true))
                    .withSourceDatabaseName("aaa")
                    .withMigrationService(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.DataMigration/sqlMigrationServices/testagent")
                    .withBackupConfiguration(
                        new BackupConfiguration()
                            .withSourceLocation(
                                new SourceLocation()
                                    .withFileShare(
                                        new SqlFileShare()
                                            .withPath("C:\\aaa\\bbb\\ccc")
                                            .withUsername("name")
                                            .withPassword("placeholder")))
                            .withTargetLocation(
                                new TargetLocation()
                                    .withStorageAccountResourceId("account.database.windows.net")
                                    .withAccountKey("abcd")))
                    .withOfflineConfiguration(
                        new OfflineConfiguration().withOffline(true).withLastBackupName("last_backup_file_name")))
            .create();
    }

    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/SqlVmCreateOrUpdateDatabaseMigrationMIN.json
     */
    /**
     * Sample code: Create or Update Database Migration resource with Minimum parameters.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void createOrUpdateDatabaseMigrationResourceWithMinimumParameters(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .databaseMigrationsSqlVms()
            .define("db1")
            .withExistingSqlVirtualMachine("testrg", "testvm")
            .withProperties(
                new DatabaseMigrationPropertiesSqlVm()
                    .withScope(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.SqlVirtualMachine/sqlVirtualMachines/testvm")
                    .withSourceSqlConnection(
                        new SqlConnectionInformation()
                            .withDataSource("aaa")
                            .withAuthentication("WindowsAuthentication")
                            .withUsername("bbb")
                            .withPassword("placeholder")
                            .withEncryptConnection(true)
                            .withTrustServerCertificate(true))
                    .withSourceDatabaseName("aaa")
                    .withMigrationService(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.DataMigration/sqlMigrationServices/testagent")
                    .withBackupConfiguration(
                        new BackupConfiguration()
                            .withSourceLocation(
                                new SourceLocation()
                                    .withFileShare(
                                        new SqlFileShare()
                                            .withPath("C:\\aaa\\bbb\\ccc")
                                            .withUsername("name")
                                            .withPassword("placeholder")))
                            .withTargetLocation(
                                new TargetLocation()
                                    .withStorageAccountResourceId("account.database.windows.net")
                                    .withAccountKey("abcd"))))
            .create();
    }
}
```

### DatabaseMigrationsSqlVm_Cutover

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datamigration.models.MigrationOperationInput;
import java.util.UUID;

/** Samples for DatabaseMigrationsSqlVm Cutover. */
public final class DatabaseMigrationsSqlVmCutoverSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/SqlVmCutoverDatabaseMigration.json
     */
    /**
     * Sample code: Cutover online migration operation for the database.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void cutoverOnlineMigrationOperationForTheDatabase(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .databaseMigrationsSqlVms()
            .cutover(
                "testrg",
                "testvm",
                "db1",
                new MigrationOperationInput()
                    .withMigrationOperationId(UUID.fromString("4124fe90-d1b6-4b50-b4d9-46d02381f59a")),
                Context.NONE);
    }
}
```

### DatabaseMigrationsSqlVm_Get

```java
import com.azure.core.util.Context;

/** Samples for DatabaseMigrationsSqlVm Get. */
public final class DatabaseMigrationsSqlVmGetSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/SqlVmGetDatabaseMigrationExpanded.json
     */
    /**
     * Sample code: Get Sql VM database Migration with the expand parameter.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void getSqlVMDatabaseMigrationWithTheExpandParameter(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .databaseMigrationsSqlVms()
            .getWithResponse("testrg", "testvm", "db1", null, "MigrationStatusDetails", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/SqlVmGetDatabaseMigration.json
     */
    /**
     * Sample code: Get Sql VM database Migration without the expand parameter.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void getSqlVMDatabaseMigrationWithoutTheExpandParameter(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.databaseMigrationsSqlVms().getWithResponse("testrg", "testvm", "db1", null, null, Context.NONE);
    }
}
```

### Files_CreateOrUpdate

```java
import com.azure.resourcemanager.datamigration.models.ProjectFileProperties;

/** Samples for Files CreateOrUpdate. */
public final class FilesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Files_CreateOrUpdate.json
     */
    /**
     * Sample code: Files_CreateOrUpdate.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void filesCreateOrUpdate(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .files()
            .define("x114d023d8")
            .withExistingProject("DmsSdkRg", "DmsSdkService", "DmsSdkProject")
            .withProperties(new ProjectFileProperties().withFilePath("DmsSdkFilePath/DmsSdkFile.sql"))
            .create();
    }
}
```

### Files_Delete

```java
import com.azure.core.util.Context;

/** Samples for Files Delete. */
public final class FilesDeleteSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Files_Delete.json
     */
    /**
     * Sample code: Files_Delete.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void filesDelete(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.files().deleteWithResponse("DmsSdkRg", "DmsSdkService", "DmsSdkProject", "x114d023d8", Context.NONE);
    }
}
```

### Files_Get

```java
import com.azure.core.util.Context;

/** Samples for Files Get. */
public final class FilesGetSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Files_Get.json
     */
    /**
     * Sample code: Files_List.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void filesList(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.files().getWithResponse("DmsSdkRg", "DmsSdkService", "DmsSdkProject", "x114d023d8", Context.NONE);
    }
}
```

### Files_List

```java
import com.azure.core.util.Context;

/** Samples for Files List. */
public final class FilesListSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Files_List.json
     */
    /**
     * Sample code: Files_List.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void filesList(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.files().list("DmsSdkRg", "DmsSdkService", "DmsSdkProject", Context.NONE);
    }
}
```

### Files_Read

```java
import com.azure.core.util.Context;

/** Samples for Files Read. */
public final class FilesReadSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Files_Read.json
     */
    /**
     * Sample code: Files_List.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void filesList(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.files().readWithResponse("DmsSdkRg", "DmsSdkService", "DmsSdkProject", "x114d023d8", Context.NONE);
    }
}
```

### Files_ReadWrite

```java
import com.azure.core.util.Context;

/** Samples for Files ReadWrite. */
public final class FilesReadWriteSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Files_ReadWrite.json
     */
    /**
     * Sample code: Files_List.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void filesList(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.files().readWriteWithResponse("DmsSdkRg", "DmsSdkService", "DmsSdkProject", "x114d023d8", Context.NONE);
    }
}
```

### Files_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datamigration.models.ProjectFile;
import com.azure.resourcemanager.datamigration.models.ProjectFileProperties;

/** Samples for Files Update. */
public final class FilesUpdateSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Files_Update.json
     */
    /**
     * Sample code: Files_Update.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void filesUpdate(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        ProjectFile resource =
            manager
                .files()
                .getWithResponse("DmsSdkRg", "DmsSdkService", "DmsSdkProject", "x114d023d8", Context.NONE)
                .getValue();
        resource
            .update()
            .withProperties(new ProjectFileProperties().withFilePath("DmsSdkFilePath/DmsSdkFile.sql"))
            .apply();
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/ListOperation.json
     */
    /**
     * Sample code: Lists all of the available SQL Rest API operations.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void listsAllOfTheAvailableSQLRestAPIOperations(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### Projects_CreateOrUpdate

```java
import com.azure.resourcemanager.datamigration.models.ProjectSourcePlatform;
import com.azure.resourcemanager.datamigration.models.ProjectTargetPlatform;

/** Samples for Projects CreateOrUpdate. */
public final class ProjectsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Projects_CreateOrUpdate.json
     */
    /**
     * Sample code: Projects_CreateOrUpdate.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void projectsCreateOrUpdate(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .projects()
            .define("DmsSdkProject")
            .withRegion("southcentralus")
            .withExistingService("DmsSdkRg", "DmsSdkService")
            .withSourcePlatform(ProjectSourcePlatform.SQL)
            .withTargetPlatform(ProjectTargetPlatform.SQLDB)
            .create();
    }
}
```

### Projects_Delete

```java
import com.azure.core.util.Context;

/** Samples for Projects Delete. */
public final class ProjectsDeleteSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Projects_Delete.json
     */
    /**
     * Sample code: Projects_Delete.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void projectsDelete(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.projects().deleteWithResponse("DmsSdkRg", "DmsSdkService", "DmsSdkProject", null, Context.NONE);
    }
}
```

### Projects_Get

```java
import com.azure.core.util.Context;

/** Samples for Projects Get. */
public final class ProjectsGetSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Projects_Get.json
     */
    /**
     * Sample code: Projects_Get.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void projectsGet(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.projects().getWithResponse("DmsSdkRg", "DmsSdkService", "DmsSdkProject", Context.NONE);
    }
}
```

### Projects_List

```java
import com.azure.core.util.Context;

/** Samples for Projects List. */
public final class ProjectsListSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Projects_List.json
     */
    /**
     * Sample code: Projects_List.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void projectsList(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.projects().list("DmsSdkRg", "DmsSdkService", Context.NONE);
    }
}
```

### Projects_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datamigration.models.Project;
import com.azure.resourcemanager.datamigration.models.ProjectSourcePlatform;
import com.azure.resourcemanager.datamigration.models.ProjectTargetPlatform;

/** Samples for Projects Update. */
public final class ProjectsUpdateSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Projects_Update.json
     */
    /**
     * Sample code: Projects_Update.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void projectsUpdate(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        Project resource =
            manager.projects().getWithResponse("DmsSdkRg", "DmsSdkService", "DmsSdkProject", Context.NONE).getValue();
        resource
            .update()
            .withSourcePlatform(ProjectSourcePlatform.SQL)
            .withTargetPlatform(ProjectTargetPlatform.SQLDB)
            .apply();
    }
}
```

### ResourceSkus_List

```java
import com.azure.core.util.Context;

/** Samples for ResourceSkus List. */
public final class ResourceSkusListSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/ResourceSkus_ListSkus.json
     */
    /**
     * Sample code: ListSkus.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void listSkus(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.resourceSkus().list(Context.NONE);
    }
}
```

### ServiceTasks_Cancel

```java
import com.azure.core.util.Context;

/** Samples for ServiceTasks Cancel. */
public final class ServiceTasksCancelSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/ServiceTasks_Cancel.json
     */
    /**
     * Sample code: Tasks_Cancel.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void tasksCancel(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.serviceTasks().cancelWithResponse("DmsSdkRg", "DmsSdkService", "DmsSdkTask", Context.NONE);
    }
}
```

### ServiceTasks_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datamigration.fluent.models.ProjectTaskInner;
import com.azure.resourcemanager.datamigration.models.CheckOciDriverTaskInput;
import com.azure.resourcemanager.datamigration.models.CheckOciDriverTaskProperties;

/** Samples for ServiceTasks CreateOrUpdate. */
public final class ServiceTasksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/ServiceTasks_CreateOrUpdate.json
     */
    /**
     * Sample code: Tasks_CreateOrUpdate.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void tasksCreateOrUpdate(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .serviceTasks()
            .createOrUpdateWithResponse(
                "DmsSdkRg",
                "DmsSdkService",
                "DmsSdkTask",
                new ProjectTaskInner()
                    .withProperties(
                        new CheckOciDriverTaskProperties()
                            .withInput(new CheckOciDriverTaskInput().withServerVersion("NA"))),
                Context.NONE);
    }
}
```

### ServiceTasks_Delete

```java
import com.azure.core.util.Context;

/** Samples for ServiceTasks Delete. */
public final class ServiceTasksDeleteSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/ServiceTasks_Delete.json
     */
    /**
     * Sample code: Tasks_Delete.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void tasksDelete(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.serviceTasks().deleteWithResponse("DmsSdkRg", "DmsSdkService", "DmsSdkTask", null, Context.NONE);
    }
}
```

### ServiceTasks_Get

```java
import com.azure.core.util.Context;

/** Samples for ServiceTasks Get. */
public final class ServiceTasksGetSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/ServiceTasks_Get.json
     */
    /**
     * Sample code: Tasks_Get.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void tasksGet(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.serviceTasks().getWithResponse("DmsSdkRg", "DmsSdkService", "DmsSdkTask", null, Context.NONE);
    }
}
```

### ServiceTasks_List

```java
import com.azure.core.util.Context;

/** Samples for ServiceTasks List. */
public final class ServiceTasksListSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/ServiceTasks_List.json
     */
    /**
     * Sample code: ServiceTasks_List.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void serviceTasksList(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.serviceTasks().list("DmsSdkRg", "DmsSdkService", null, Context.NONE);
    }
}
```

### ServiceTasks_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datamigration.fluent.models.ProjectTaskInner;
import com.azure.resourcemanager.datamigration.models.CheckOciDriverTaskInput;
import com.azure.resourcemanager.datamigration.models.CheckOciDriverTaskProperties;

/** Samples for ServiceTasks Update. */
public final class ServiceTasksUpdateSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/ServiceTasks_Update.json
     */
    /**
     * Sample code: Tasks_Update.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void tasksUpdate(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .serviceTasks()
            .updateWithResponse(
                "DmsSdkRg",
                "DmsSdkService",
                "DmsSdkTask",
                new ProjectTaskInner()
                    .withProperties(
                        new CheckOciDriverTaskProperties()
                            .withInput(new CheckOciDriverTaskInput().withServerVersion("NA"))),
                Context.NONE);
    }
}
```

### Services_CheckChildrenNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datamigration.models.NameAvailabilityRequest;

/** Samples for Services CheckChildrenNameAvailability. */
public final class ServicesCheckChildrenNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Services_CheckChildrenNameAvailability.json
     */
    /**
     * Sample code: Services_CheckChildrenNameAvailability.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesCheckChildrenNameAvailability(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .services()
            .checkChildrenNameAvailabilityWithResponse(
                "DmsSdkRg",
                "DmsSdkService",
                new NameAvailabilityRequest().withName("Task1").withType("tasks"),
                Context.NONE);
    }
}
```

### Services_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datamigration.models.NameAvailabilityRequest;

/** Samples for Services CheckNameAvailability. */
public final class ServicesCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Services_CheckNameAvailability.json
     */
    /**
     * Sample code: Services_CheckNameAvailability.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesCheckNameAvailability(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .services()
            .checkNameAvailabilityWithResponse(
                "eastus", new NameAvailabilityRequest().withName("DmsSdkService").withType("services"), Context.NONE);
    }
}
```

### Services_CheckStatus

```java
import com.azure.core.util.Context;

/** Samples for Services CheckStatus. */
public final class ServicesCheckStatusSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Services_CheckStatus.json
     */
    /**
     * Sample code: Services_CheckStatus.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesCheckStatus(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.services().checkStatusWithResponse("DmsSdkRg", "DmsSdkService", Context.NONE);
    }
}
```

### Services_CreateOrUpdate

```java
import com.azure.resourcemanager.datamigration.models.ServiceSku;

/** Samples for Services CreateOrUpdate. */
public final class ServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Services_CreateOrUpdate.json
     */
    /**
     * Sample code: Services_CreateOrUpdate.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesCreateOrUpdate(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .services()
            .define("DmsSdkService")
            .withRegion("southcentralus")
            .withExistingResourceGroup("DmsSdkRg")
            .withSku(new ServiceSku().withName("Basic_1vCore"))
            .withVirtualSubnetId(
                "/subscriptions/fc04246f-04c5-437e-ac5e-206a19e7193f/resourceGroups/DmsSdkTestNetwork/providers/Microsoft.Network/virtualNetworks/DmsSdkTestNetwork/subnets/default")
            .create();
    }
}
```

### Services_Delete

```java
import com.azure.core.util.Context;

/** Samples for Services Delete. */
public final class ServicesDeleteSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Services_Delete.json
     */
    /**
     * Sample code: Services_CreateOrUpdate.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesCreateOrUpdate(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.services().delete("DmsSdkRg", "DmsSdkService", null, Context.NONE);
    }
}
```

### Services_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Services GetByResourceGroup. */
public final class ServicesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Services_Get.json
     */
    /**
     * Sample code: Services_CreateOrUpdate.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesCreateOrUpdate(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.services().getByResourceGroupWithResponse("DmsSdkRg", "DmsSdkService", Context.NONE);
    }
}
```

### Services_List

```java
import com.azure.core.util.Context;

/** Samples for Services List. */
public final class ServicesListSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Services_List.json
     */
    /**
     * Sample code: Services_List.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesList(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.services().list(Context.NONE);
    }
}
```

### Services_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Services ListByResourceGroup. */
public final class ServicesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Services_ListByResourceGroup.json
     */
    /**
     * Sample code: Services_ListByResourceGroup.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesListByResourceGroup(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.services().listByResourceGroup("DmsSdkRg", Context.NONE);
    }
}
```

### Services_ListSkus

```java
import com.azure.core.util.Context;

/** Samples for Services ListSkus. */
public final class ServicesListSkusSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Services_ListSkus.json
     */
    /**
     * Sample code: Services_ListSkus.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesListSkus(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.services().listSkus("DmsSdkRg", "DmsSdkService", Context.NONE);
    }
}
```

### Services_Start

```java
import com.azure.core.util.Context;

/** Samples for Services Start. */
public final class ServicesStartSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Services_Start.json
     */
    /**
     * Sample code: Services_Start.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesStart(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.services().start("DmsSdkRg", "DmsSdkService", Context.NONE);
    }
}
```

### Services_Stop

```java
import com.azure.core.util.Context;

/** Samples for Services Stop. */
public final class ServicesStopSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Services_Stop.json
     */
    /**
     * Sample code: Services_Stop.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesStop(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.services().stop("DmsSdkRg", "DmsSdkService", Context.NONE);
    }
}
```

### Services_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datamigration.models.DataMigrationService;

/** Samples for Services Update. */
public final class ServicesUpdateSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Services_Update.json
     */
    /**
     * Sample code: Services_CreateOrUpdate.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesCreateOrUpdate(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        DataMigrationService resource =
            manager.services().getByResourceGroupWithResponse("DmsSdkRg", "DmsSdkService", Context.NONE).getValue();
        resource
            .update()
            .withVirtualSubnetId(
                "/subscriptions/fc04246f-04c5-437e-ac5e-206a19e7193f/resourceGroups/DmsSdkTestNetwork/providers/Microsoft.Network/virtualNetworks/DmsSdkTestNetwork/subnets/default")
            .apply();
    }
}
```

### SqlMigrationServices_CreateOrUpdate

```java
/** Samples for SqlMigrationServices CreateOrUpdate. */
public final class SqlMigrationServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/CreateOrUpdateMigrationServiceMAX.json
     */
    /**
     * Sample code: Create or Update SQL Migration Service with maximum parameters.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void createOrUpdateSQLMigrationServiceWithMaximumParameters(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .sqlMigrationServices()
            .define("testagent")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .create();
    }

    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/CreateOrUpdateMigrationServiceMIN.json
     */
    /**
     * Sample code: Create or Update SQL Migration Service with minimum parameters.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void createOrUpdateSQLMigrationServiceWithMinimumParameters(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .sqlMigrationServices()
            .define("testagent")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .create();
    }
}
```

### SqlMigrationServices_Delete

```java
import com.azure.core.util.Context;

/** Samples for SqlMigrationServices Delete. */
public final class SqlMigrationServicesDeleteSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/DeleteMigrationService.json
     */
    /**
     * Sample code: Delete SQL Migration Service.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void deleteSQLMigrationService(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.sqlMigrationServices().delete("testrg", "service1", Context.NONE);
    }
}
```

### SqlMigrationServices_DeleteNode

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datamigration.fluent.models.DeleteNodeInner;

/** Samples for SqlMigrationServices DeleteNode. */
public final class SqlMigrationServicesDeleteNodeSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/DeleteIntegrationRuntimeNode.json
     */
    /**
     * Sample code: Delete the integration runtime node.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void deleteTheIntegrationRuntimeNode(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .sqlMigrationServices()
            .deleteNodeWithResponse(
                "testrg",
                "service1",
                new DeleteNodeInner().withNodeName("nodeName").withIntegrationRuntimeName("IRName"),
                Context.NONE);
    }
}
```

### SqlMigrationServices_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for SqlMigrationServices GetByResourceGroup. */
public final class SqlMigrationServicesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/GetMigrationService.json
     */
    /**
     * Sample code: Get Migration Service.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void getMigrationService(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.sqlMigrationServices().getByResourceGroupWithResponse("testrg", "service1", Context.NONE);
    }
}
```

### SqlMigrationServices_List

```java
import com.azure.core.util.Context;

/** Samples for SqlMigrationServices List. */
public final class SqlMigrationServicesListSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/ListBySubscriptionMigrationService.json
     */
    /**
     * Sample code: Get Services in the Subscriptions.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void getServicesInTheSubscriptions(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.sqlMigrationServices().list(Context.NONE);
    }
}
```

### SqlMigrationServices_ListAuthKeys

```java
import com.azure.core.util.Context;

/** Samples for SqlMigrationServices ListAuthKeys. */
public final class SqlMigrationServicesListAuthKeysSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/ListAuthKeysMigrationService.json
     */
    /**
     * Sample code: Retrieve the List of Authentication Keys.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void retrieveTheListOfAuthenticationKeys(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.sqlMigrationServices().listAuthKeysWithResponse("testrg", "service1", Context.NONE);
    }
}
```

### SqlMigrationServices_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for SqlMigrationServices ListByResourceGroup. */
public final class SqlMigrationServicesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/ListByResourceGroupMigrationService.json
     */
    /**
     * Sample code: Get Migration Services in the Resource Group.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void getMigrationServicesInTheResourceGroup(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.sqlMigrationServices().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### SqlMigrationServices_ListMigrations

```java
import com.azure.core.util.Context;

/** Samples for SqlMigrationServices ListMigrations. */
public final class SqlMigrationServicesListMigrationsSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/ListMigrationsByMigrationService.json
     */
    /**
     * Sample code: List database migrations attached to the service.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void listDatabaseMigrationsAttachedToTheService(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.sqlMigrationServices().listMigrations("testrg", "service1", Context.NONE);
    }
}
```

### SqlMigrationServices_ListMonitoringData

```java
import com.azure.core.util.Context;

/** Samples for SqlMigrationServices ListMonitoringData. */
public final class SqlMigrationServicesListMonitoringDataSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/GetMonitorDataMigrationService.json
     */
    /**
     * Sample code: Retrieve the Monitoring Data.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void retrieveTheMonitoringData(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.sqlMigrationServices().listMonitoringDataWithResponse("testrg", "service1", Context.NONE);
    }
}
```

### SqlMigrationServices_RegenerateAuthKeys

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datamigration.fluent.models.RegenAuthKeysInner;

/** Samples for SqlMigrationServices RegenerateAuthKeys. */
public final class SqlMigrationServicesRegenerateAuthKeysSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/RegenAuthKeysMigrationService.json
     */
    /**
     * Sample code: Regenerate the of Authentication Keys.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void regenerateTheOfAuthenticationKeys(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .sqlMigrationServices()
            .regenerateAuthKeysWithResponse(
                "testrg", "service1", new RegenAuthKeysInner().withKeyName("authKey1"), Context.NONE);
    }
}
```

### SqlMigrationServices_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datamigration.models.SqlMigrationService;
import java.util.HashMap;
import java.util.Map;

/** Samples for SqlMigrationServices Update. */
public final class SqlMigrationServicesUpdateSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/UpdateMigrationService.json
     */
    /**
     * Sample code: Update SQL Migration Service.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void updateSQLMigrationService(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        SqlMigrationService resource =
            manager
                .sqlMigrationServices()
                .getByResourceGroupWithResponse("testrg", "testagent", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("mytag", "myval")).apply();
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

### Tasks_Cancel

```java
import com.azure.core.util.Context;

/** Samples for Tasks Cancel. */
public final class TasksCancelSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Tasks_Cancel.json
     */
    /**
     * Sample code: Tasks_Cancel.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void tasksCancel(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.tasks().cancelWithResponse("DmsSdkRg", "DmsSdkService", "DmsSdkProject", "DmsSdkTask", Context.NONE);
    }
}
```

### Tasks_Command

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datamigration.models.MigrateSyncCompleteCommandInput;
import com.azure.resourcemanager.datamigration.models.MigrateSyncCompleteCommandProperties;

/** Samples for Tasks Command. */
public final class TasksCommandSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Tasks_Command.json
     */
    /**
     * Sample code: Tasks_Command.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void tasksCommand(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .tasks()
            .commandWithResponse(
                "DmsSdkRg",
                "DmsSdkService",
                "DmsSdkProject",
                "DmsSdkTask",
                new MigrateSyncCompleteCommandProperties()
                    .withInput(new MigrateSyncCompleteCommandInput().withDatabaseName("TestDatabase")),
                Context.NONE);
    }
}
```

### Tasks_CreateOrUpdate

```java
import com.azure.resourcemanager.datamigration.models.AuthenticationType;
import com.azure.resourcemanager.datamigration.models.ConnectToTargetSqlDbTaskInput;
import com.azure.resourcemanager.datamigration.models.ConnectToTargetSqlDbTaskProperties;
import com.azure.resourcemanager.datamigration.models.SqlConnectionInfo;

/** Samples for Tasks CreateOrUpdate. */
public final class TasksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Tasks_CreateOrUpdate.json
     */
    /**
     * Sample code: Tasks_CreateOrUpdate.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void tasksCreateOrUpdate(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .tasks()
            .define("DmsSdkTask")
            .withExistingProject("DmsSdkRg", "DmsSdkService", "DmsSdkProject")
            .withProperties(
                new ConnectToTargetSqlDbTaskProperties()
                    .withInput(
                        new ConnectToTargetSqlDbTaskInput()
                            .withTargetConnectionInfo(
                                new SqlConnectionInfo()
                                    .withUsername("testuser")
                                    .withPassword("testpassword")
                                    .withDataSource("ssma-test-server.database.windows.net")
                                    .withAuthentication(AuthenticationType.SQL_AUTHENTICATION)
                                    .withEncryptConnection(true)
                                    .withTrustServerCertificate(true))))
            .create();
    }
}
```

### Tasks_Delete

```java
import com.azure.core.util.Context;

/** Samples for Tasks Delete. */
public final class TasksDeleteSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Tasks_Delete.json
     */
    /**
     * Sample code: Tasks_Delete.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void tasksDelete(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .tasks()
            .deleteWithResponse("DmsSdkRg", "DmsSdkService", "DmsSdkProject", "DmsSdkTask", null, Context.NONE);
    }
}
```

### Tasks_Get

```java
import com.azure.core.util.Context;

/** Samples for Tasks Get. */
public final class TasksGetSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Tasks_Get.json
     */
    /**
     * Sample code: Tasks_Get.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void tasksGet(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.tasks().getWithResponse("DmsSdkRg", "DmsSdkService", "DmsSdkProject", "DmsSdkTask", null, Context.NONE);
    }
}
```

### Tasks_List

```java
import com.azure.core.util.Context;

/** Samples for Tasks List. */
public final class TasksListSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Tasks_List.json
     */
    /**
     * Sample code: Tasks_List.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void tasksList(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.tasks().list("DmsSdkRg", "DmsSdkService", "DmsSdkProject", null, Context.NONE);
    }
}
```

### Tasks_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datamigration.models.AuthenticationType;
import com.azure.resourcemanager.datamigration.models.ConnectToTargetSqlDbTaskInput;
import com.azure.resourcemanager.datamigration.models.ConnectToTargetSqlDbTaskProperties;
import com.azure.resourcemanager.datamigration.models.ProjectTask;
import com.azure.resourcemanager.datamigration.models.SqlConnectionInfo;

/** Samples for Tasks Update. */
public final class TasksUpdateSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Tasks_Update.json
     */
    /**
     * Sample code: Tasks_Update.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void tasksUpdate(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        ProjectTask resource =
            manager
                .tasks()
                .getWithResponse("DmsSdkRg", "DmsSdkService", "DmsSdkProject", "DmsSdkTask", null, Context.NONE)
                .getValue();
        resource
            .update()
            .withProperties(
                new ConnectToTargetSqlDbTaskProperties()
                    .withInput(
                        new ConnectToTargetSqlDbTaskInput()
                            .withTargetConnectionInfo(
                                new SqlConnectionInfo()
                                    .withUsername("testuser")
                                    .withPassword("testpassword")
                                    .withDataSource("ssma-test-server.database.windows.net")
                                    .withAuthentication(AuthenticationType.SQL_AUTHENTICATION)
                                    .withEncryptConnection(true)
                                    .withTrustServerCertificate(true))))
            .apply();
    }
}
```

### Usages_List

```java
import com.azure.core.util.Context;

/** Samples for Usages List. */
public final class UsagesListSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/preview/2022-03-30-preview/examples/Usages_List.json
     */
    /**
     * Sample code: Services_Usages.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesUsages(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.usages().list("westus", Context.NONE);
    }
}
```

