# Code snippets and samples


## ActiveDirectoryConnectors

- [Create](#activedirectoryconnectors_create)
- [Delete](#activedirectoryconnectors_delete)
- [Get](#activedirectoryconnectors_get)
- [List](#activedirectoryconnectors_list)

## DataControllers

- [Delete](#datacontrollers_delete)
- [GetByResourceGroup](#datacontrollers_getbyresourcegroup)
- [List](#datacontrollers_list)
- [ListByResourceGroup](#datacontrollers_listbyresourcegroup)
- [PatchDataController](#datacontrollers_patchdatacontroller)
- [PutDataController](#datacontrollers_putdatacontroller)

## FailoverGroups

- [Create](#failovergroups_create)
- [Delete](#failovergroups_delete)
- [Get](#failovergroups_get)
- [List](#failovergroups_list)

## Operations

- [List](#operations_list)

## PostgresInstances

- [Create](#postgresinstances_create)
- [Delete](#postgresinstances_delete)
- [GetByResourceGroup](#postgresinstances_getbyresourcegroup)
- [List](#postgresinstances_list)
- [ListByResourceGroup](#postgresinstances_listbyresourcegroup)
- [Update](#postgresinstances_update)

## SqlManagedInstances

- [Create](#sqlmanagedinstances_create)
- [Delete](#sqlmanagedinstances_delete)
- [GetByResourceGroup](#sqlmanagedinstances_getbyresourcegroup)
- [List](#sqlmanagedinstances_list)
- [ListByResourceGroup](#sqlmanagedinstances_listbyresourcegroup)
- [Update](#sqlmanagedinstances_update)

## SqlServerAvailabilityGroups

- [AddDatabases](#sqlserveravailabilitygroups_adddatabases)
- [Create](#sqlserveravailabilitygroups_create)
- [CreateAvailabilityGroup](#sqlserveravailabilitygroups_createavailabilitygroup)
- [CreateDistributedAvailabilityGroup](#sqlserveravailabilitygroups_createdistributedavailabilitygroup)
- [CreateManagedInstanceLink](#sqlserveravailabilitygroups_createmanagedinstancelink)
- [Delete](#sqlserveravailabilitygroups_delete)
- [DetailView](#sqlserveravailabilitygroups_detailview)
- [Failover](#sqlserveravailabilitygroups_failover)
- [FailoverMiLink](#sqlserveravailabilitygroups_failovermilink)
- [ForceFailoverAllowDataLoss](#sqlserveravailabilitygroups_forcefailoverallowdataloss)
- [Get](#sqlserveravailabilitygroups_get)
- [List](#sqlserveravailabilitygroups_list)
- [RemoveDatabases](#sqlserveravailabilitygroups_removedatabases)
- [Update](#sqlserveravailabilitygroups_update)

## SqlServerDatabases

- [Create](#sqlserverdatabases_create)
- [Delete](#sqlserverdatabases_delete)
- [Get](#sqlserverdatabases_get)
- [List](#sqlserverdatabases_list)
- [Update](#sqlserverdatabases_update)

## SqlServerEsuLicenses

- [Create](#sqlserveresulicenses_create)
- [Delete](#sqlserveresulicenses_delete)
- [GetByResourceGroup](#sqlserveresulicenses_getbyresourcegroup)
- [List](#sqlserveresulicenses_list)
- [ListByResourceGroup](#sqlserveresulicenses_listbyresourcegroup)
- [Update](#sqlserveresulicenses_update)

## SqlServerInstances

- [Create](#sqlserverinstances_create)
- [Delete](#sqlserverinstances_delete)
- [GetByResourceGroup](#sqlserverinstances_getbyresourcegroup)
- [GetJobsStatus](#sqlserverinstances_getjobsstatus)
- [GetTelemetry](#sqlserverinstances_gettelemetry)
- [List](#sqlserverinstances_list)
- [ListByResourceGroup](#sqlserverinstances_listbyresourcegroup)
- [PostUpgrade](#sqlserverinstances_postupgrade)
- [PreUpgrade](#sqlserverinstances_preupgrade)
- [RunManagedInstanceLinkAssessment](#sqlserverinstances_runmanagedinstancelinkassessment)
- [RunMigrationAssessment](#sqlserverinstances_runmigrationassessment)
- [Update](#sqlserverinstances_update)

## SqlServerLicenses

- [Create](#sqlserverlicenses_create)
- [Delete](#sqlserverlicenses_delete)
- [GetByResourceGroup](#sqlserverlicenses_getbyresourcegroup)
- [List](#sqlserverlicenses_list)
- [ListByResourceGroup](#sqlserverlicenses_listbyresourcegroup)
- [Update](#sqlserverlicenses_update)
### ActiveDirectoryConnectors_Create

```java
/**
 * Samples for SqlServerAvailabilityGroups List.
 */
public final class SqlServerAvailabilityGroupsListSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * ListBySqlServerInstanceAvailabilityGroup.json
     */
    /**
     * Sample code: Gets all availability groups associated with an Arc Enabled Sql server.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void getsAllAvailabilityGroupsAssociatedWithAnArcEnabledSqlServer(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerAvailabilityGroups().list("testrg", "testSqlServer_INST1", com.azure.core.util.Context.NONE);
    }
}
```

### ActiveDirectoryConnectors_Delete

```java
/**
 * Samples for PostgresInstances Delete.
 */
public final class PostgresInstancesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * DeletePostgresInstance.json
     */
    /**
     * Sample code: Deletes a PostgresInstances.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void deletesAPostgresInstances(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.postgresInstances().delete("testrg", "testpostgresInstance", com.azure.core.util.Context.NONE);
    }
}
```

### ActiveDirectoryConnectors_Get

```java
/**
 * Samples for SqlServerInstances PostUpgrade.
 */
public final class SqlServerInstancesPostUpgradeSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * PostUpgradeSqlServerInstance.json
     */
    /**
     * Sample code: Post AUM Upgrade to complete SQL Instance Upgrade Process.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void postAUMUpgradeToCompleteSQLInstanceUpgradeProcess(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerInstances()
            .postUpgradeWithResponse("testrg", "testsqlServerInstance", com.azure.core.util.Context.NONE);
    }
}
```

### ActiveDirectoryConnectors_List

```java
/**
 * Samples for ActiveDirectoryConnectors Delete.
 */
public final class ActiveDirectoryConnectorsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * DeleteActiveDirectoryConnector.json
     */
    /**
     * Sample code: Deletes an Active Directory connector instance.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        deletesAnActiveDirectoryConnectorInstance(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.activeDirectoryConnectors()
            .delete("testrg", "testdataController", "testADConnector", com.azure.core.util.Context.NONE);
    }
}
```

### DataControllers_Delete

```java
import com.azure.resourcemanager.azurearcdata.models.AvailabilityGroupConfigure;
import com.azure.resourcemanager.azurearcdata.models.AvailabilityGroupInfo;
import com.azure.resourcemanager.azurearcdata.models.SqlAvailabilityGroupDatabaseReplicaResourceProperties;
import com.azure.resourcemanager.azurearcdata.models.SqlAvailabilityGroupReplicaResourceProperties;
import com.azure.resourcemanager.azurearcdata.models.SqlServerAvailabilityGroupResourceProperties;
import com.azure.resourcemanager.azurearcdata.models.SqlServerAvailabilityGroupResourcePropertiesDatabases;
import com.azure.resourcemanager.azurearcdata.models.SqlServerAvailabilityGroupResourcePropertiesReplicas;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SqlServerAvailabilityGroups Create.
 */
public final class SqlServerAvailabilityGroupsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * CreateOrUpdateArcSqlServerAvailabilityGroup.json
     */
    /**
     * Sample code: Create a Arc Sql Server availability group.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        createAArcSqlServerAvailabilityGroup(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerAvailabilityGroups()
            .define("testAG")
            .withRegion("southeastasia")
            .withExistingSqlServerInstance("testrg", "testSqlServer_INST1")
            .withProperties(
                new SqlServerAvailabilityGroupResourceProperties()
                    .withInfo(new AvailabilityGroupInfo().withFailureConditionLevel(3)
                        .withHealthCheckTimeout(30000)
                        .withBasicFeatures(false)
                        .withDtcSupport(false)
                        .withDbFailover(true)
                        .withIsDistributed(false)
                        .withRequiredSynchronizedSecondariesToCommit(0)
                        .withIsContained(false))
                    .withReplicas(
                        new SqlServerAvailabilityGroupResourcePropertiesReplicas().withValue(Arrays.asList(
                            new SqlAvailabilityGroupReplicaResourceProperties().withReplicaName("testSqlServer\\INST1")
                                .withConfigure(new AvailabilityGroupConfigure()
                                    .withEndpointUrl("TCP://mytest60-0.mytest60-svc:5022")
                                    .withSessionTimeout(10)
                                    .withBackupPriority(50)))))
                    .withDatabases(new SqlServerAvailabilityGroupResourcePropertiesDatabases().withValue(Arrays.asList(
                        new SqlAvailabilityGroupDatabaseReplicaResourceProperties().withDatabaseName("db1"),
                        new SqlAvailabilityGroupDatabaseReplicaResourceProperties().withDatabaseName("db2")))))
            .withTags(mapOf("mytag", "myval"))
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

### DataControllers_GetByResourceGroup

```java
/**
 * Samples for ActiveDirectoryConnectors List.
 */
public final class ActiveDirectoryConnectorsListSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * ListByDataControllerActiveDirectoryConnector.json
     */
    /**
     * Sample code: Gets all Active Directory connectors associated with a data controller.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void getsAllActiveDirectoryConnectorsAssociatedWithADataController(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.activeDirectoryConnectors().list("testrg", "testdataController", com.azure.core.util.Context.NONE);
    }
}
```

### DataControllers_List

```java
import com.azure.resourcemanager.azurearcdata.models.BillingPlan;
import com.azure.resourcemanager.azurearcdata.models.ScopeType;
import com.azure.resourcemanager.azurearcdata.models.SqlServerEsuLicenseProperties;
import com.azure.resourcemanager.azurearcdata.models.State;
import com.azure.resourcemanager.azurearcdata.models.Version;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SqlServerEsuLicenses Create.
 */
public final class SqlServerEsuLicensesCreateSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * CreateOrUpdateSqlServerEsuLicense.json
     */
    /**
     * Sample code: Updates a SQL Server ESU license.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void updatesASQLServerESULicense(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerEsuLicenses()
            .define("testsqlServerEsuLicense")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .withProperties(new SqlServerEsuLicenseProperties().withBillingPlan(BillingPlan.PAYG)
                .withVersion(Version.SQL_SERVER_2012)
                .withPhysicalCores(24)
                .withActivationState(State.INACTIVE)
                .withScopeType(ScopeType.SUBSCRIPTION))
            .withTags(mapOf("mytag", "myval"))
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

### DataControllers_ListByResourceGroup

```java
/**
 * Samples for SqlServerInstances ListByResourceGroup.
 */
public final class SqlServerInstancesListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * ListByResourceGroupSqlServerInstance.json
     */
    /**
     * Sample code: Gets all SQL Server Instance in a resource group.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        getsAllSQLServerInstanceInAResourceGroup(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerInstances().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### DataControllers_PatchDataController

```java
import com.azure.resourcemanager.azurearcdata.models.ArcSqlServerAvailabilityMode;
import com.azure.resourcemanager.azurearcdata.models.ArcSqlServerFailoverMode;
import com.azure.resourcemanager.azurearcdata.models.DistributedAvailabilityGroupCreateUpdateAvailabilityGroupCertificateConfiguration;
import com.azure.resourcemanager.azurearcdata.models.DistributedAvailabilityGroupCreateUpdateAvailabilityGroupConfiguration;
import com.azure.resourcemanager.azurearcdata.models.DistributedAvailabilityGroupCreateUpdateConfiguration;
import com.azure.resourcemanager.azurearcdata.models.SeedingMode;

/**
 * Samples for SqlServerAvailabilityGroups CreateDistributedAvailabilityGroup.
 */
public final class SqlServerAvailabilityGroupsCreateDistributedAvailabilityGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * CreateSqlServerDistributedAvailabilityGroup.json
     */
    /**
     * Sample code: Create a distributed availability group using this server for the primary replica.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void createADistributedAvailabilityGroupUsingThisServerForThePrimaryReplica(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerAvailabilityGroups()
            .createDistributedAvailabilityGroup("testrg", "testSqlServer_INST1",
                new DistributedAvailabilityGroupCreateUpdateConfiguration().withAvailabilityGroupName("myNewDag")
                    .withPrimaryAvailabilityGroup(
                        new DistributedAvailabilityGroupCreateUpdateAvailabilityGroupConfiguration()
                            .withAvailabilityGroup(
                                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.AzureArcData/sqlServerInstances/testSqlServer_INST1/availabilityGroups/testAG1")
                            .withListenerUrl("TCP://testAG1.contoso.com:5022")
                            .withAvailabilityMode(ArcSqlServerAvailabilityMode.ASYNCHRONOUS_COMMIT)
                            .withFailoverMode(ArcSqlServerFailoverMode.MANUAL)
                            .withSeedingMode(SeedingMode.AUTOMATIC)
                            .withCertificateConfiguration(
                                new DistributedAvailabilityGroupCreateUpdateAvailabilityGroupCertificateConfiguration()
                                    .withCertificateName("myCert")))
                    .withSecondaryAvailabilityGroup(
                        new DistributedAvailabilityGroupCreateUpdateAvailabilityGroupConfiguration()
                            .withAvailabilityGroup(
                                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.AzureArcData/sqlServerInstances/testSqlServer_INST2/availabilityGroups/testAG2")
                            .withListenerUrl("TCP://testAG2.contoso.com:5022")
                            .withAvailabilityMode(ArcSqlServerAvailabilityMode.ASYNCHRONOUS_COMMIT)
                            .withFailoverMode(ArcSqlServerFailoverMode.MANUAL)
                            .withSeedingMode(SeedingMode.AUTOMATIC)
                            .withCertificateConfiguration(
                                new DistributedAvailabilityGroupCreateUpdateAvailabilityGroupCertificateConfiguration()
                                    .withCertificateName("myCert"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### DataControllers_PutDataController

```java
/**
 * Samples for SqlServerInstances GetByResourceGroup.
 */
public final class SqlServerInstancesGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * GetSqlServerInstance.json
     */
    /**
     * Sample code: Updates a SQL Server Instance tags.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        updatesASQLServerInstanceTags(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerInstances()
            .getByResourceGroupWithResponse("testrg", "testsqlServerInstance", com.azure.core.util.Context.NONE);
    }
}
```

### FailoverGroups_Create

```java
/**
 * Samples for PostgresInstances ListByResourceGroup.
 */
public final class PostgresInstancesListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * ListByResourceGroupPostgresInstance.json
     */
    /**
     * Sample code: Gets all postgres Instances in a resource group.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        getsAllPostgresInstancesInAResourceGroup(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.postgresInstances().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### FailoverGroups_Delete

```java
/**
 * Samples for SqlServerAvailabilityGroups DetailView.
 */
public final class SqlServerAvailabilityGroupsDetailViewSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * ViewArcSqlServerAvailabilityGroup.json
     */
    /**
     * Sample code: detail view for a server availability group.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        detailViewForAServerAvailabilityGroup(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerAvailabilityGroups()
            .detailViewWithResponse("testrg", "testSqlServer_INST1", "testAG", com.azure.core.util.Context.NONE);
    }
}
```

### FailoverGroups_Get

```java
/**
 * Samples for SqlManagedInstances ListByResourceGroup.
 */
public final class SqlManagedInstancesListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * ListByResourceGroupSqlManagedInstance.json
     */
    /**
     * Sample code: Gets all SQL Instance in a resource group.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        getsAllSQLInstanceInAResourceGroup(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlManagedInstances().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### FailoverGroups_List

```java
import com.azure.resourcemanager.azurearcdata.models.SqlServerAvailabilityGroupResource;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SqlServerAvailabilityGroups Update.
 */
public final class SqlServerAvailabilityGroupsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * UpdateArcSqlServerAvailabilityGroup.json
     */
    /**
     * Sample code: Update an availability group.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void updateAnAvailabilityGroup(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        SqlServerAvailabilityGroupResource resource = manager.sqlServerAvailabilityGroups()
            .getWithResponse("testrg", "testSqlServer_INST1", "testAG", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("mytag", "myval1")).apply();
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

### Operations_List

```java
/**
 * Samples for SqlServerLicenses Delete.
 */
public final class SqlServerLicensesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * DeleteSqlServerLicense.json
     */
    /**
     * Sample code: Delete a SQL Server license.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void deleteASQLServerLicense(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerLicenses()
            .deleteByResourceGroupWithResponse("testrg", "testsqlServerLicense", com.azure.core.util.Context.NONE);
    }
}
```

### PostgresInstances_Create

```java
/**
 * Samples for DataControllers List.
 */
public final class DataControllersListSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * ListSubscriptionDataController.json
     */
    /**
     * Sample code: Gets all dataControllers in a subscription.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        getsAllDataControllersInASubscription(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.dataControllers().list(com.azure.core.util.Context.NONE);
    }
}
```

### PostgresInstances_Delete

```java
import com.azure.resourcemanager.azurearcdata.models.BillingPlan;
import com.azure.resourcemanager.azurearcdata.models.SqlServerEsuLicense;
import com.azure.resourcemanager.azurearcdata.models.SqlServerEsuLicenseUpdateProperties;
import com.azure.resourcemanager.azurearcdata.models.State;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SqlServerEsuLicenses Update.
 */
public final class SqlServerEsuLicensesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * UpdateSqlServerEsuLicense.json
     */
    /**
     * Sample code: Patch a SQL Server ESU license.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void patchASQLServerESULicense(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        SqlServerEsuLicense resource = manager.sqlServerEsuLicenses()
            .getByResourceGroupWithResponse("testrg", "testsqlServerEsuLicense", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("mytag", "myval"))
            .withProperties(new SqlServerEsuLicenseUpdateProperties().withBillingPlan(BillingPlan.PAID)
                .withPhysicalCores(28)
                .withActivationState(State.ACTIVE))
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

### PostgresInstances_GetByResourceGroup

```java
/**
 * Samples for PostgresInstances GetByResourceGroup.
 */
public final class PostgresInstancesGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * GetPostgresInstance.json
     */
    /**
     * Sample code: Gets a postgres Instances.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void getsAPostgresInstances(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.postgresInstances()
            .getByResourceGroupWithResponse("testrg", "testpostgresInstances", com.azure.core.util.Context.NONE);
    }
}
```

### PostgresInstances_List

```java
/**
 * Samples for SqlServerAvailabilityGroups Get.
 */
public final class SqlServerAvailabilityGroupsGetSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * GetArcSqlServerAvailabilityGroup.json
     */
    /**
     * Sample code: Retrieves an Arc Sql Server availability group resource.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void retrievesAnArcSqlServerAvailabilityGroupResource(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerAvailabilityGroups()
            .getWithResponse("testrg", "testSqlServer_INST1", "testAG", com.azure.core.util.Context.NONE);
    }
}
```

### PostgresInstances_ListByResourceGroup

```java
import com.azure.resourcemanager.azurearcdata.models.ActivationState;
import com.azure.resourcemanager.azurearcdata.models.BillingPlan;
import com.azure.resourcemanager.azurearcdata.models.LicenseCategory;
import com.azure.resourcemanager.azurearcdata.models.ScopeType;
import com.azure.resourcemanager.azurearcdata.models.SqlServerLicenseProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SqlServerLicenses Create.
 */
public final class SqlServerLicensesCreateSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * CreateOrUpdateSqlServerLicense.json
     */
    /**
     * Sample code: Updates a SQL Server license tags.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        updatesASQLServerLicenseTags(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerLicenses()
            .define("testsqlServerLicense")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .withProperties(new SqlServerLicenseProperties().withBillingPlan(BillingPlan.PAYG)
                .withPhysicalCores(24)
                .withLicenseCategory(LicenseCategory.CORE)
                .withActivationState(ActivationState.DEACTIVATED)
                .withScopeType(ScopeType.SUBSCRIPTION))
            .withTags(mapOf("mytag", "myval"))
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

### PostgresInstances_Update

```java
/**
 * Samples for PostgresInstances List.
 */
public final class PostgresInstancesListSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * ListSubscriptionPostgresInstance.json
     */
    /**
     * Sample code: Gets all Postgres Instance in a subscription.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        getsAllPostgresInstanceInASubscription(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.postgresInstances().list(com.azure.core.util.Context.NONE);
    }
}
```

### SqlManagedInstances_Create

```java
import com.azure.resourcemanager.azurearcdata.models.PostgresInstance;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for PostgresInstances Update.
 */
public final class PostgresInstancesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * UpdatePostgresInstance.json
     */
    /**
     * Sample code: Updates a postgres Instances tags.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        updatesAPostgresInstancesTags(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        PostgresInstance resource = manager.postgresInstances()
            .getByResourceGroupWithResponse("testrg", "testpostgresInstance", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("mytag", "myval")).apply();
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

### SqlManagedInstances_Delete

```java
import com.azure.resourcemanager.azurearcdata.models.Databases;
import java.util.Arrays;

/**
 * Samples for SqlServerAvailabilityGroups RemoveDatabases.
 */
public final class SqlServerAvailabilityGroupsRemoveDatabasesSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * RemoveDatabasesFromAvailabilityGroup.json
     */
    /**
     * Sample code: remove databases from this availability group.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        removeDatabasesFromThisAvailabilityGroup(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerAvailabilityGroups()
            .removeDatabasesWithResponse("testrg", "testSqlServer_INST1", "testAG",
                new Databases().withValues(Arrays.asList("db1")), com.azure.core.util.Context.NONE);
    }
}
```

### SqlManagedInstances_GetByResourceGroup

```java
/**
 * Samples for SqlManagedInstances List.
 */
public final class SqlManagedInstancesListSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * ListSubscriptionSqlManagedInstance.json
     */
    /**
     * Sample code: Gets all SQL Instance in a subscription.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        getsAllSQLInstanceInASubscription(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlManagedInstances().list(com.azure.core.util.Context.NONE);
    }
}
```

### SqlManagedInstances_List

```java
/**
 * Samples for SqlServerLicenses List.
 */
public final class SqlServerLicensesListSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * ListSubscriptionSqlServerLicense.json
     */
    /**
     * Sample code: Gets all SQL Server license in a subscription.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        getsAllSQLServerLicenseInASubscription(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerLicenses().list(com.azure.core.util.Context.NONE);
    }
}
```

### SqlManagedInstances_ListByResourceGroup

```java
/**
 * Samples for SqlServerInstances RunMigrationAssessment.
 */
public final class SqlServerInstancesRunMigrationAssessmentSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * RunMigrationAssessmentSqlServerInstance.json
     */
    /**
     * Sample code: Trigger migration assessment run on sql server instance.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void triggerMigrationAssessmentRunOnSqlServerInstance(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerInstances()
            .runMigrationAssessmentWithResponse("testrg", "testsqlserver", com.azure.core.util.Context.NONE);
    }
}
```

### SqlManagedInstances_Update

```java
import com.azure.resourcemanager.azurearcdata.models.BasicLoginInformation;
import com.azure.resourcemanager.azurearcdata.models.DataControllerProperties;
import com.azure.resourcemanager.azurearcdata.models.ExtendedLocation;
import com.azure.resourcemanager.azurearcdata.models.ExtendedLocationTypes;
import com.azure.resourcemanager.azurearcdata.models.Infrastructure;
import com.azure.resourcemanager.azurearcdata.models.LogAnalyticsWorkspaceConfig;
import com.azure.resourcemanager.azurearcdata.models.OnPremiseProperty;
import com.azure.resourcemanager.azurearcdata.models.UploadServicePrincipal;
import com.azure.resourcemanager.azurearcdata.models.UploadWatermark;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Samples for DataControllers PutDataController.
 */
public final class DataControllersPutDataControllerSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * CreateOrUpdateDataController.json
     */
    /**
     * Sample code: Create or update a Data Controller.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        createOrUpdateADataController(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.dataControllers()
            .define("testdataController")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .withProperties(new DataControllerProperties().withInfrastructure(Infrastructure.ONPREMISES)
                .withOnPremiseProperty(
                    new OnPremiseProperty().withId(UUID.fromString("12345678-1234-1234-ab12-1a2b3c4d5e6f"))
                        .withPublicSigningKey("fakeTokenPlaceholder"))
                .withUploadWatermark(
                    new UploadWatermark().withMetrics(OffsetDateTime.parse("2020-01-01T17:18:19.1234567Z"))
                        .withLogs(OffsetDateTime.parse("2020-01-01T17:18:19.1234567Z"))
                        .withUsages(OffsetDateTime.parse("2020-01-01T17:18:19.1234567Z")))
                .withBasicLoginInformation(
                    new BasicLoginInformation().withUsername("username").withPassword("fakeTokenPlaceholder"))
                .withMetricsDashboardCredential(
                    new BasicLoginInformation().withUsername("username").withPassword("fakeTokenPlaceholder"))
                .withLogsDashboardCredential(
                    new BasicLoginInformation().withUsername("username").withPassword("fakeTokenPlaceholder"))
                .withLogAnalyticsWorkspaceConfig(new LogAnalyticsWorkspaceConfig()
                    .withWorkspaceId(UUID.fromString("00000000-1111-2222-3333-444444444444"))
                    .withPrimaryKey("fakeTokenPlaceholder"))
                .withUploadServicePrincipal(
                    new UploadServicePrincipal().withClientId(UUID.fromString("00000000-1111-2222-3333-444444444444"))
                        .withTenantId(UUID.fromString("00000000-1111-2222-3333-444444444444"))
                        .withAuthority("https://login.microsoftonline.com/")
                        .withClientSecret("fakeTokenPlaceholder"))
                .withClusterId(
                    "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Kubernetes/connectedClusters/connectedk8s")
                .withExtensionId(
                    "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Kubernetes/connectedClusters/connectedk8s/providers/Microsoft.KubernetesConfiguration/extensions/extension"))
            .withTags(mapOf("mytag", "myval"))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.ExtendedLocation/customLocations/arclocation")
                .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
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

### SqlServerAvailabilityGroups_AddDatabases

```java
import com.azure.resourcemanager.azurearcdata.models.SqlServerInstanceTelemetryRequest;
import java.time.OffsetDateTime;

/**
 * Samples for SqlServerInstances GetTelemetry.
 */
public final class SqlServerInstancesGetTelemetrySamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * GetSqlServerInstanceTelemetry.json
     */
    /**
     * Sample code: Retrieves Arc SQL Server cpu utilization telemetry.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void retrievesArcSQLServerCpuUtilizationTelemetry(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerInstances()
            .getTelemetry("testrg", "testsqlserver",
                new SqlServerInstanceTelemetryRequest().withDatasetName("sqlserver_storage_io")
                    .withStartTime(OffsetDateTime.parse("2023-09-30T00:00:00Z")),
                com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerAvailabilityGroups_Create

```java
/**
 * Samples for SqlServerEsuLicenses ListByResourceGroup.
 */
public final class SqlServerEsuLicensesListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * ListByResourceGroupSqlServerEsuLicense.json
     */
    /**
     * Sample code: Gets all SQL Server ESU licenses in a resource group.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void getsAllSQLServerESULicensesInAResourceGroup(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerEsuLicenses().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerAvailabilityGroups_CreateAvailabilityGroup

```java
import com.azure.resourcemanager.azurearcdata.models.SqlServerInstanceJobsStatusRequest;

/**
 * Samples for SqlServerInstances GetJobsStatus.
 */
public final class SqlServerInstancesGetJobsStatusSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * GetSqlServerInstanceJobsStatus.json
     */
    /**
     * Sample code: Retrieves Arc SQL Server instance jobs status.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        retrievesArcSQLServerInstanceJobsStatus(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerInstances()
            .getJobsStatusWithResponse("testrg", "testsqlserver",
                new SqlServerInstanceJobsStatusRequest().withFeatureName("MigrationAssessment").withJobType("Ondemand"),
                com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerAvailabilityGroups_CreateDistributedAvailabilityGroup

```java
import com.azure.resourcemanager.azurearcdata.models.ArcSqlServerAvailabilityMode;
import com.azure.resourcemanager.azurearcdata.models.ArcSqlServerFailoverMode;
import com.azure.resourcemanager.azurearcdata.models.AvailabilityGroupCreateUpdateConfiguration;
import com.azure.resourcemanager.azurearcdata.models.AvailabilityGroupCreateUpdateReplicaConfiguration;
import com.azure.resourcemanager.azurearcdata.models.DistributedAvailabilityGroupCreateUpdateAvailabilityGroupCertificateConfiguration;
import com.azure.resourcemanager.azurearcdata.models.DistributedAvailabilityGroupCreateUpdateAvailabilityGroupConfiguration;
import com.azure.resourcemanager.azurearcdata.models.DistributedAvailabilityGroupCreateUpdateConfiguration;
import com.azure.resourcemanager.azurearcdata.models.ManagedInstanceLinkCreateUpdateConfiguration;
import com.azure.resourcemanager.azurearcdata.models.MiLinkCreateUpdateConfiguration;
import com.azure.resourcemanager.azurearcdata.models.SecondaryAllowConnections;
import com.azure.resourcemanager.azurearcdata.models.SeedingMode;
import java.util.Arrays;

/**
 * Samples for SqlServerAvailabilityGroups CreateManagedInstanceLink.
 */
public final class SqlServerAvailabilityGroupsCreateManagedInstanceLinkSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * CreateOrUpdateManagedInstanceLink.json
     */
    /**
     * Sample code: Create an Managed Instance Link.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void createAnManagedInstanceLink(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerAvailabilityGroups()
            .createManagedInstanceLink("testrg", "testSqlServer_INST1",
                new ManagedInstanceLinkCreateUpdateConfiguration()
                    .withAvailabilityGroup(
                        new AvailabilityGroupCreateUpdateConfiguration().withAvailabilityGroupName("myNewAg")
                            .withReplicas(Arrays.asList(new AvailabilityGroupCreateUpdateReplicaConfiguration()
                                .withEndpointUrl("TCP://testSqlServer_INST1.testSqlserverDomain:5022")
                                .withAvailabilityMode(ArcSqlServerAvailabilityMode.SYNCHRONOUS_COMMIT)
                                .withFailoverMode(ArcSqlServerFailoverMode.AUTOMATIC)
                                .withSeedingMode(SeedingMode.AUTOMATIC)
                                .withSecondaryRoleAllowConnections(SecondaryAllowConnections.ALL)))
                            .withDatabases(Arrays.asList("database1")))
                    .withDistributedAvailabilityGroup(new DistributedAvailabilityGroupCreateUpdateConfiguration()
                        .withAvailabilityGroupName("myNewDag")
                        .withPrimaryAvailabilityGroup(
                            new DistributedAvailabilityGroupCreateUpdateAvailabilityGroupConfiguration()
                                .withAvailabilityGroup(
                                    "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.AzureArcData/sqlServerInstances/testSqlServer_INST1/availabilityGroups/testAG1")
                                .withListenerUrl("TCP://testAG1.contoso.com:5022")
                                .withAvailabilityMode(ArcSqlServerAvailabilityMode.ASYNCHRONOUS_COMMIT)
                                .withFailoverMode(ArcSqlServerFailoverMode.MANUAL)
                                .withSeedingMode(SeedingMode.AUTOMATIC)
                                .withCertificateConfiguration(
                                    new DistributedAvailabilityGroupCreateUpdateAvailabilityGroupCertificateConfiguration()
                                        .withCertificateName("myCert")))
                        .withSecondaryAvailabilityGroup(
                            new DistributedAvailabilityGroupCreateUpdateAvailabilityGroupConfiguration()
                                .withAvailabilityGroup(
                                    "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Sql/managedInstances/testcl")
                                .withAvailabilityMode(ArcSqlServerAvailabilityMode.ASYNCHRONOUS_COMMIT)
                                .withFailoverMode(ArcSqlServerFailoverMode.NONE)
                                .withSeedingMode(SeedingMode.AUTOMATIC)))
                    .withMiLinkConfiguration(
                        new MiLinkCreateUpdateConfiguration().withInstanceAvailabilityGroupName("testAG2")),
                com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerAvailabilityGroups_CreateManagedInstanceLink

```java
import com.azure.resourcemanager.azurearcdata.models.SqlServerInstance;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SqlServerInstances Update.
 */
public final class SqlServerInstancesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * UpdateSqlServerInstance.json
     */
    /**
     * Sample code: Updates a SQL Server Instance tags.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        updatesASQLServerInstanceTags(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        SqlServerInstance resource = manager.sqlServerInstances()
            .getByResourceGroupWithResponse("testrg", "testsqlServerInstance", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("mytag", "myval")).apply();
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

### SqlServerAvailabilityGroups_Delete

```java
/**
 * Samples for SqlServerEsuLicenses GetByResourceGroup.
 */
public final class SqlServerEsuLicensesGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * GetSqlServerEsuLicense.json
     */
    /**
     * Sample code: Gets a SQL Server ESU license.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void getsASQLServerESULicense(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerEsuLicenses()
            .getByResourceGroupWithResponse("testrg", "testsqlServerEsuLicense", com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerAvailabilityGroups_DetailView

```java
/**
 * Samples for SqlServerInstances PreUpgrade.
 */
public final class SqlServerInstancesPreUpgradeSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * PreUpgradeSqlServerInstance.json
     */
    /**
     * Sample code: Prepare SQL server instance for AUM Upgrade.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        prepareSQLServerInstanceForAUMUpgrade(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerInstances()
            .preUpgradeWithResponse("testrg", "testsqlServerInstance", com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerAvailabilityGroups_Failover

```java
/**
 * Samples for DataControllers Delete.
 */
public final class DataControllersDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * DeleteDataController.json
     */
    /**
     * Sample code: Delete a dataController.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void deleteADataController(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.dataControllers().delete("testrg", "testdataController", com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerAvailabilityGroups_FailoverMiLink

```java
/**
 * Samples for SqlServerLicenses GetByResourceGroup.
 */
public final class SqlServerLicensesGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * GetSqlServerLicense.json
     */
    /**
     * Sample code: Gets a SQL Server license tags.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void getsASQLServerLicenseTags(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerLicenses()
            .getByResourceGroupWithResponse("testrg", "testsqlServerLicense", com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerAvailabilityGroups_ForceFailoverAllowDataLoss

```java
/**
 * Samples for SqlServerAvailabilityGroups ForceFailoverAllowDataLoss.
 */
public final class SqlServerAvailabilityGroupsForceFailoverAllowDataLossSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * ForceFailoverArcSqlServerAvailabilityGroup.json
     */
    /**
     * Sample code: availability group force failover to this server.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        availabilityGroupForceFailoverToThisServer(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerAvailabilityGroups()
            .forceFailoverAllowDataLossWithResponse("testrg", "testSqlServer_INST1", "testAG",
                com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerAvailabilityGroups_Get

```java
/**
 * Samples for DataControllers ListByResourceGroup.
 */
public final class DataControllersListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * ListByResourceGroupDataController.json
     */
    /**
     * Sample code: Gets all dataControllers in a resource group.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        getsAllDataControllersInAResourceGroup(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.dataControllers().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerAvailabilityGroups_List

```java
import com.azure.resourcemanager.azurearcdata.models.SqlManagedInstance;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SqlManagedInstances Update.
 */
public final class SqlManagedInstancesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * UpdateSqlManagedInstance.json
     */
    /**
     * Sample code: Updates a sql Instance tags.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void updatesASqlInstanceTags(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        SqlManagedInstance resource = manager.sqlManagedInstances()
            .getByResourceGroupWithResponse("testrg", "testsqlManagedInstance", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("mytag", "myval")).apply();
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

### SqlServerAvailabilityGroups_RemoveDatabases

```java
/**
 * Samples for FailoverGroups List.
 */
public final class FailoverGroupsListSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * ListBySqlManagedInstanceFailoverGroup.json
     */
    /**
     * Sample code: Gets all failover groups associated with a sql managed instance.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void getsAllFailoverGroupsAssociatedWithASqlManagedInstance(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.failoverGroups().list("testrg", "testSqlManagedInstance", com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerAvailabilityGroups_Update

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.azurearcdata.models.BasicLoginInformation;
import com.azure.resourcemanager.azurearcdata.models.ExtendedLocation;
import com.azure.resourcemanager.azurearcdata.models.ExtendedLocationTypes;
import com.azure.resourcemanager.azurearcdata.models.PostgresInstanceProperties;
import com.azure.resourcemanager.azurearcdata.models.PostgresInstanceSku;
import com.azure.resourcemanager.azurearcdata.models.PostgresInstanceSkuTier;
import java.io.IOException;

/**
 * Samples for PostgresInstances Create.
 */
public final class PostgresInstancesCreateSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * CreateOrUpdatePostgresInstance.json
     */
    /**
     * Sample code: Create or update a Postgres Instance.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void createOrUpdateAPostgresInstance(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) throws IOException {
        manager.postgresInstances()
            .define("testpostgresInstance")
            .withRegion("eastus")
            .withExistingResourceGroup("testrg")
            .withProperties(new PostgresInstanceProperties().withDataControllerId("dataControllerId")
                .withAdmin("admin")
                .withBasicLoginInformation(
                    new BasicLoginInformation().withUsername("username").withPassword("fakeTokenPlaceholder"))
                .withK8SRaw(SerializerFactory.createDefaultManagementSerializerAdapter()
                    .deserialize(
                        "{\"apiVersion\":\"apiVersion\",\"kind\":\"postgresql-12\",\"metadata\":{\"name\":\"pg1\",\"creationTimestamp\":\"2020-08-25T14:55:10Z\",\"generation\":1,\"namespace\":\"test\",\"resourceVersion\":\"527780\",\"selfLink\":\"/apis/arcdata.microsoft.com/v1alpha1/namespaces/test/postgresql-12s/pg1\",\"uid\":\"1111aaaa-ffff-ffff-ffff-99999aaaaaaa\"},\"spec\":{\"backups\":{\"deltaMinutes\":3,\"fullMinutes\":10,\"tiers\":[{\"retention\":{\"maximums\":[\"6\",\"512MB\"],\"minimums\":[\"3\"]},\"storage\":{\"volumeSize\":\"1Gi\"}}]},\"engine\":{\"extensions\":[{\"name\":\"citus\"}]},\"scale\":{\"shards\":3},\"scheduling\":{\"default\":{\"resources\":{\"requests\":{\"memory\":\"256Mi\"}}}},\"service\":{\"type\":\"NodePort\"},\"storage\":{\"data\":{\"className\":\"local-storage\",\"size\":\"5Gi\"},\"logs\":{\"className\":\"local-storage\",\"size\":\"5Gi\"}}},\"status\":{\"externalEndpoint\":null,\"readyPods\":\"4/4\",\"state\":\"Ready\"}}",
                        Object.class, SerializerEncoding.JSON)))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.ExtendedLocation/customLocations/arclocation")
                .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .withSku(new PostgresInstanceSku().withName("default")
                .withDev(true)
                .withTier(PostgresInstanceSkuTier.HYPERSCALE))
            .create();
    }
}
```

### SqlServerDatabases_Create

```java
/**
 * Samples for SqlServerInstances List.
 */
public final class SqlServerInstancesListSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * ListSubscriptionSqlServerInstance.json
     */
    /**
     * Sample code: Gets all SQL Server Instance in a subscription.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        getsAllSQLServerInstanceInASubscription(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerInstances().list(com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerDatabases_Delete

```java
/**
 * Samples for SqlServerAvailabilityGroups Failover.
 */
public final class SqlServerAvailabilityGroupsFailoverSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * FailoverArcSqlServerAvailabilityGroup.json
     */
    /**
     * Sample code: availability group manual failover to this server.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void availabilityGroupManualFailoverToThisServer(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerAvailabilityGroups()
            .failoverWithResponse("testrg", "testSqlServer_INST1", "testAG", com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerDatabases_Get

```java
/**
 * Samples for FailoverGroups Get.
 */
public final class FailoverGroupsGetSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * GetFailoverGroup.json
     */
    /**
     * Sample code: Retrieves a failover group resource.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        retrievesAFailoverGroupResource(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.failoverGroups()
            .getWithResponse("testrg", "testSqlManagedInstance", "testFailoverGroupName",
                com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerDatabases_List

```java
import com.azure.resourcemanager.azurearcdata.models.ArcSqlServerAvailabilityMode;
import com.azure.resourcemanager.azurearcdata.models.ArcSqlServerFailoverMode;
import com.azure.resourcemanager.azurearcdata.models.AutomatedBackupPreference;
import com.azure.resourcemanager.azurearcdata.models.AvailabilityGroupCreateUpdateConfiguration;
import com.azure.resourcemanager.azurearcdata.models.AvailabilityGroupCreateUpdateReplicaConfiguration;
import com.azure.resourcemanager.azurearcdata.models.ClusterType;
import com.azure.resourcemanager.azurearcdata.models.DbFailover;
import com.azure.resourcemanager.azurearcdata.models.DtcSupport;
import com.azure.resourcemanager.azurearcdata.models.FailureConditionLevel;
import com.azure.resourcemanager.azurearcdata.models.PrimaryAllowConnections;
import com.azure.resourcemanager.azurearcdata.models.SecondaryAllowConnections;
import com.azure.resourcemanager.azurearcdata.models.SeedingMode;
import com.azure.resourcemanager.azurearcdata.models.SqlAvailabilityGroupIpV4AddressesAndMasksPropertiesItem;
import com.azure.resourcemanager.azurearcdata.models.SqlAvailabilityGroupStaticIpListenerProperties;
import java.util.Arrays;

/**
 * Samples for SqlServerAvailabilityGroups CreateAvailabilityGroup.
 */
public final class SqlServerAvailabilityGroupsCreateAvailabilityGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * CreateSqlServerAvailabilityGroup.json
     */
    /**
     * Sample code: Create an availability group using this server for the primary replica.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void createAnAvailabilityGroupUsingThisServerForThePrimaryReplica(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerAvailabilityGroups()
            .createAvailabilityGroup("testrg", "testSqlServer_INST1", new AvailabilityGroupCreateUpdateConfiguration()
                .withAvailabilityGroupName("myNewAg")
                .withReplicas(Arrays.asList(new AvailabilityGroupCreateUpdateReplicaConfiguration().withServerInstance(
                    "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.AzureArcData/sqlServerInstances/testSqlServer_INST1/")
                    .withEndpointName("inst1DBMEndpoint")
                    .withEndpointUrl("TCP://testSqlServer_INST1.testSqlserverDomain:5022")
                    .withEndpointConnectLogin("NT Server\\MSSSQLSERVER")
                    .withAvailabilityMode(ArcSqlServerAvailabilityMode.SYNCHRONOUS_COMMIT)
                    .withFailoverMode(ArcSqlServerFailoverMode.AUTOMATIC)
                    .withSeedingMode(SeedingMode.AUTOMATIC)
                    .withBackupPriority(50)
                    .withSecondaryRoleAllowConnections(SecondaryAllowConnections.ALL)
                    .withPrimaryRoleAllowConnections(PrimaryAllowConnections.ALL)
                    .withSessionTimeout(10),
                    new AvailabilityGroupCreateUpdateReplicaConfiguration().withServerInstance(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.AzureArcData/sqlServerInstances/testSqlServer_INST2/")
                        .withEndpointName("inst2DBMEndpoint")
                        .withEndpointUrl("TCP://testSqlServer_INST2.testSqlserverDomain:5022")
                        .withEndpointConnectLogin("NT Server\\MSSSQLSERVER")
                        .withAvailabilityMode(ArcSqlServerAvailabilityMode.SYNCHRONOUS_COMMIT)
                        .withFailoverMode(ArcSqlServerFailoverMode.AUTOMATIC)
                        .withSeedingMode(SeedingMode.AUTOMATIC)
                        .withBackupPriority(50)
                        .withSecondaryRoleAllowConnections(SecondaryAllowConnections.ALL)
                        .withPrimaryRoleAllowConnections(PrimaryAllowConnections.ALL)
                        .withSessionTimeout(10)))
                .withDatabases(Arrays.asList("database1", "database2"))
                .withAutomatedBackupPreference(AutomatedBackupPreference.SECONDARY)
                .withFailureConditionLevel(FailureConditionLevel.THREE)
                .withHealthCheckTimeout(30000)
                .withDbFailover(DbFailover.ON)
                .withDtcSupport(DtcSupport.NONE)
                .withRequiredSynchronizedSecondariesToCommit(0)
                .withClusterType(ClusterType.WSFC)
                .withListener(new SqlAvailabilityGroupStaticIpListenerProperties().withDnsName("myNewAgListener")
                    .withIpV4AddressesAndMasks(Arrays.asList(
                        new SqlAvailabilityGroupIpV4AddressesAndMasksPropertiesItem().withIpAddress("192.1.168.5")
                            .withMask("255.255.255.0"),
                        new SqlAvailabilityGroupIpV4AddressesAndMasksPropertiesItem().withIpAddress("10.1.168.5")
                            .withMask("255.255.255.0")))
                    .withPort(1433)),
                com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerDatabases_Update

```java
import com.azure.resourcemanager.azurearcdata.models.BackupPolicy;
import com.azure.resourcemanager.azurearcdata.models.DatabaseCreateMode;
import com.azure.resourcemanager.azurearcdata.models.DatabaseState;
import com.azure.resourcemanager.azurearcdata.models.DifferentialBackupHours;
import com.azure.resourcemanager.azurearcdata.models.RecoveryMode;
import com.azure.resourcemanager.azurearcdata.models.SqlServerDatabaseResourceProperties;
import com.azure.resourcemanager.azurearcdata.models.SqlServerDatabaseResourcePropertiesBackupInformation;
import com.azure.resourcemanager.azurearcdata.models.SqlServerDatabaseResourcePropertiesDatabaseOptions;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SqlServerDatabases Create.
 */
public final class SqlServerDatabasesCreateSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * CreateOrUpdateArcSqlServerDatabase.json
     */
    /**
     * Sample code: Create a Arc Sql Server database.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void createAArcSqlServerDatabase(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerDatabases()
            .define("testdb")
            .withRegion("southeastasia")
            .withExistingSqlServerInstance("testrg", "testSqlServerInstance")
            .withProperties(new SqlServerDatabaseResourceProperties().withCollationName("SQL_Latin1_General_CP1_CI_AS")
                .withDatabaseCreationDate(OffsetDateTime.parse("2022-04-05T16:26:33.883Z"))
                .withCompatibilityLevel(150)
                .withSizeMB(150.0F)
                .withLogFileSizeMB(70.0F)
                .withDataFileSizeMB(80.0F)
                .withSpaceAvailableMB(100.0F)
                .withState(DatabaseState.ONLINE)
                .withIsReadOnly(true)
                .withRecoveryMode(RecoveryMode.FULL)
                .withDatabaseOptions(new SqlServerDatabaseResourcePropertiesDatabaseOptions().withIsAutoCloseOn(true)
                    .withIsAutoShrinkOn(true)
                    .withIsAutoCreateStatsOn(true)
                    .withIsAutoUpdateStatsOn(true)
                    .withIsRemoteDataArchiveEnabled(true)
                    .withIsMemoryOptimizationEnabled(true)
                    .withIsEncrypted(true)
                    .withIsTrustworthyOn(true))
                .withBackupInformation(new SqlServerDatabaseResourcePropertiesBackupInformation()
                    .withLastFullBackup(OffsetDateTime.parse("2022-05-05T16:26:33.883Z"))
                    .withLastLogBackup(OffsetDateTime.parse("2022-05-10T16:26:33.883Z")))
                .withBackupPolicy(new BackupPolicy().withRetentionPeriodDays(1)
                    .withFullBackupDays(1)
                    .withDifferentialBackupHours(DifferentialBackupHours.ONE_TWO)
                    .withTransactionLogBackupMinutes(30))
                .withCreateMode(DatabaseCreateMode.POINT_IN_TIME_RESTORE)
                .withSourceDatabaseId(
                    "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.AzureArcData/testSqlServerInstance/testsqlManagedInstance/databases/MyDatabase")
                .withRestorePointInTime(OffsetDateTime.parse("2022-05-05T16:26:33.883Z")))
            .withTags(mapOf("mytag", "myval"))
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

### SqlServerEsuLicenses_Create

```java
import com.azure.resourcemanager.azurearcdata.models.Authentication;
import com.azure.resourcemanager.azurearcdata.models.BackupPolicy;
import com.azure.resourcemanager.azurearcdata.models.ClientConnection;
import com.azure.resourcemanager.azurearcdata.models.DifferentialBackupHours;
import com.azure.resourcemanager.azurearcdata.models.EditionType;
import com.azure.resourcemanager.azurearcdata.models.EntraAuthentication;
import com.azure.resourcemanager.azurearcdata.models.HostType;
import com.azure.resourcemanager.azurearcdata.models.IdentityType;
import com.azure.resourcemanager.azurearcdata.models.Migration;
import com.azure.resourcemanager.azurearcdata.models.MigrationAssessment;
import com.azure.resourcemanager.azurearcdata.models.Mode;
import com.azure.resourcemanager.azurearcdata.models.Monitoring;
import com.azure.resourcemanager.azurearcdata.models.ServiceType;
import com.azure.resourcemanager.azurearcdata.models.SqlServerInstanceProperties;
import com.azure.resourcemanager.azurearcdata.models.SqlVersion;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SqlServerInstances Create.
 */
public final class SqlServerInstancesCreateSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * CreateOrUpdateSqlServerInstance.json
     */
    /**
     * Sample code: Updates a SQL Server Instance tags.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        updatesASQLServerInstanceTags(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerInstances()
            .define("testsqlServerInstance")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .withTags(mapOf("mytag", "myval"))
            .withProperties(new SqlServerInstanceProperties().withVersion(SqlVersion.SQL_SERVER_2012)
                .withEdition(EditionType.DEVELOPER)
                .withCores("4")
                .withInstanceName("name of instance")
                .withHostType(HostType.PHYSICAL_SERVER)
                .withBackupPolicy(new BackupPolicy().withRetentionPeriodDays(1)
                    .withFullBackupDays(1)
                    .withDifferentialBackupHours(DifferentialBackupHours.ONE_TWO)
                    .withTransactionLogBackupMinutes(30))
                .withMonitoring(new Monitoring().withEnabled(false))
                .withMigration(new Migration().withAssessment(new MigrationAssessment().withEnabled(false)))
                .withClientConnection(new ClientConnection().withEnabled(false))
                .withServiceType(ServiceType.ENGINE)
                .withAuthentication(new Authentication().withMode(Mode.WINDOWS)
                    .withSqlServerEntraIdentity(Arrays.asList(
                        new EntraAuthentication().withIdentityType(IdentityType.USER_ASSIGNED_MANAGED_IDENTITY)
                            .withClientId("00000000-1111-2222-3333-444444444444"),
                        new EntraAuthentication().withIdentityType(IdentityType.SYSTEM_ASSIGNED_MANAGED_IDENTITY)
                            .withClientId("")))))
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

### SqlServerEsuLicenses_Delete

```java
/**
 * Samples for SqlServerInstances Delete.
 */
public final class SqlServerInstancesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * DeleteSqlServerInstance.json
     */
    /**
     * Sample code: Delete a SQL Server Instance.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void deleteASQLServerInstance(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerInstances().delete("testrg", "testsqlServerInstance", com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerEsuLicenses_GetByResourceGroup

```java
/**
 * Samples for ActiveDirectoryConnectors Get.
 */
public final class ActiveDirectoryConnectorsGetSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * GetActiveDirectoryConnector.json
     */
    /**
     * Sample code: Retrieves an Active Directory connector resource.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void retrievesAnActiveDirectoryConnectorResource(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.activeDirectoryConnectors()
            .getWithResponse("testrg", "testdataController", "testADConnector", com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerEsuLicenses_List

```java
import com.azure.resourcemanager.azurearcdata.models.AccountProvisioningMode;
import com.azure.resourcemanager.azurearcdata.models.ActiveDirectoryConnectorDnsDetails;
import com.azure.resourcemanager.azurearcdata.models.ActiveDirectoryConnectorDomainDetails;
import com.azure.resourcemanager.azurearcdata.models.ActiveDirectoryConnectorProperties;
import com.azure.resourcemanager.azurearcdata.models.ActiveDirectoryConnectorSpec;
import com.azure.resourcemanager.azurearcdata.models.ActiveDirectoryDomainController;
import com.azure.resourcemanager.azurearcdata.models.ActiveDirectoryDomainControllers;
import java.util.Arrays;

/**
 * Samples for ActiveDirectoryConnectors Create.
 */
public final class ActiveDirectoryConnectorsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * CreateOrUpdateActiveDirectoryConnector.json
     */
    /**
     * Sample code: Create or update an Active Directory connector instance.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void createOrUpdateAnActiveDirectoryConnectorInstance(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.activeDirectoryConnectors()
            .define("testADConnector")
            .withExistingDataController("testrg", "testdataController")
            .withProperties(new ActiveDirectoryConnectorProperties().withSpec(new ActiveDirectoryConnectorSpec()
                .withActiveDirectory(new ActiveDirectoryConnectorDomainDetails().withRealm("CONTOSO.LOCAL")
                    .withServiceAccountProvisioning(AccountProvisioningMode.MANUAL)
                    .withDomainControllers(new ActiveDirectoryDomainControllers()
                        .withPrimaryDomainController(
                            new ActiveDirectoryDomainController().withHostname("dc1.contoso.local"))
                        .withSecondaryDomainControllers(
                            Arrays.asList(new ActiveDirectoryDomainController().withHostname("dc2.contoso.local"),
                                new ActiveDirectoryDomainController().withHostname("dc3.contoso.local")))))
                .withDns(new ActiveDirectoryConnectorDnsDetails()
                    .withNameserverIpAddresses(Arrays.asList("11.11.111.111", "22.22.222.222"))
                    .withReplicas(1L)
                    .withPreferK8SDnsForPtrLookups(false))))
            .create();
    }
}
```

### SqlServerEsuLicenses_ListByResourceGroup

```java
/**
 * Samples for SqlManagedInstances GetByResourceGroup.
 */
public final class SqlManagedInstancesGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * GetSqlManagedInstance.json
     */
    /**
     * Sample code: Updates a SQL Instance tags.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void updatesASQLInstanceTags(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlManagedInstances()
            .getByResourceGroupWithResponse("testrg", "testsqlManagedInstance", com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerEsuLicenses_Update

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * ListOperation.json
     */
    /**
     * Sample code: Lists all of the available Azure Data Services on Azure Arc API operations.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void listsAllOfTheAvailableAzureDataServicesOnAzureArcAPIOperations(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerInstances_Create

```java
/**
 * Samples for SqlServerLicenses ListByResourceGroup.
 */
public final class SqlServerLicensesListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * ListByResourceGroupSqlServerLicense.json
     */
    /**
     * Sample code: Gets all SQL Server license in a resource group.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        getsAllSQLServerLicenseInAResourceGroup(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerLicenses().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerInstances_Delete

```java
import com.azure.resourcemanager.azurearcdata.models.DataControllerResource;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DataControllers PatchDataController.
 */
public final class DataControllersPatchDataControllerSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * UpdateDataController.json
     */
    /**
     * Sample code: Updates a dataController tags.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void updatesADataControllerTags(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        DataControllerResource resource = manager.dataControllers()
            .getByResourceGroupWithResponse("testrg", "testdataController1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("mytag", "myval")).apply();
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

### SqlServerInstances_GetByResourceGroup

```java
/**
 * Samples for SqlManagedInstances Delete.
 */
public final class SqlManagedInstancesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * DeleteSqlManagedInstance.json
     */
    /**
     * Sample code: Delete a SQL Instance.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void deleteASQLInstance(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlManagedInstances().delete("testrg", "testsqlManagedInstance", com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerInstances_GetJobsStatus

```java
import com.azure.resourcemanager.azurearcdata.models.FailoverMiLinkResourceId;

/**
 * Samples for SqlServerAvailabilityGroups FailoverMiLink.
 */
public final class SqlServerAvailabilityGroupsFailoverMiLinkSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * failoverMiLink.json
     */
    /**
     * Sample code: Fail over from an Arc Sql Server availability group to a Managed Instance.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void failOverFromAnArcSqlServerAvailabilityGroupToAManagedInstance(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerAvailabilityGroups()
            .failoverMiLink("testrg", "testSqlServer_INST1", "testDAG",
                new FailoverMiLinkResourceId().withManagedInstanceId(
                    "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Sql/managedInstances/testcl"),
                com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerInstances_GetTelemetry

```java
import com.azure.resourcemanager.azurearcdata.models.Databases;
import java.util.Arrays;

/**
 * Samples for SqlServerAvailabilityGroups AddDatabases.
 */
public final class SqlServerAvailabilityGroupsAddDatabasesSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * AddDatabasesToAvailabilityGroup.json
     */
    /**
     * Sample code: add databases to this availability group.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        addDatabasesToThisAvailabilityGroup(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerAvailabilityGroups()
            .addDatabasesWithResponse("testrg", "testSqlServer_INST1", "testAG",
                new Databases().withValues(Arrays.asList("db1", "db2", "db3")), com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerInstances_List

```java
/**
 * Samples for SqlServerDatabases Delete.
 */
public final class SqlServerDatabasesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * DeleteArcSqlServerDatabase.json
     */
    /**
     * Sample code: Deletes a database resource.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void deletesADatabaseResource(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerDatabases()
            .delete("testrg", "testsqlManagedInstance", "testdb", com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerInstances_ListByResourceGroup

```java
import com.azure.resourcemanager.azurearcdata.models.AzureManagedInstanceRole;
import com.azure.resourcemanager.azurearcdata.models.SqlServerInstanceManagedInstanceLinkAssessmentRequest;

/**
 * Samples for SqlServerInstances RunManagedInstanceLinkAssessment.
 */
public final class SqlServerInstancesRunManagedInstanceLinkAssessmentSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * GetSqlServerInstanceManagedInstanceLinkAssessment.json
     */
    /**
     * Sample code: Retrieves Arc SQL Server cpu utilization telemetry.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void retrievesArcSQLServerCpuUtilizationTelemetry(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerInstances()
            .runManagedInstanceLinkAssessment("testrg", "testsqlserver",
                new SqlServerInstanceManagedInstanceLinkAssessmentRequest().withAzureManagedInstanceResourceId(
                    "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Sql/managedInstances/testmi")
                    .withAzureManagedInstanceRole(AzureManagedInstanceRole.SECONDARY)
                    .withDatabaseName("testdb")
                    .withAvailabilityGroupName("AG_testdb"),
                com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerInstances_PostUpgrade

```java
/**
 * Samples for SqlServerDatabases Get.
 */
public final class SqlServerDatabasesGetSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * GetArcSqlServerDatabase.json
     */
    /**
     * Sample code: Retrieves an Arc Sql Server database resource.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        retrievesAnArcSqlServerDatabaseResource(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerDatabases()
            .getWithResponse("testrg", "testSqlServerInstance", "testdb", com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerInstances_PreUpgrade

```java
import com.azure.resourcemanager.azurearcdata.models.FailoverGroupPartnerSyncMode;
import com.azure.resourcemanager.azurearcdata.models.FailoverGroupProperties;
import com.azure.resourcemanager.azurearcdata.models.FailoverGroupSpec;
import com.azure.resourcemanager.azurearcdata.models.InstanceFailoverGroupRole;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for FailoverGroups Create.
 */
public final class FailoverGroupsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * CreateOrUpdateFailoverGroup.json
     */
    /**
     * Sample code: Create or update a failover group instance.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        createOrUpdateAFailoverGroupInstance(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.failoverGroups()
            .define("testFailoverGroupName")
            .withExistingSqlManagedInstance("testrg", "testSqlManagedInstance")
            .withProperties(new FailoverGroupProperties().withPartnerManagedInstanceId(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.AzureArcData/sqlManagedInstances/partnerMI")
                .withSpec(new FailoverGroupSpec().withPartnerSyncMode(FailoverGroupPartnerSyncMode.ASYNC)
                    .withRole(InstanceFailoverGroupRole.PRIMARY)
                    .withAdditionalProperties(mapOf()))
                .withAdditionalProperties(mapOf()))
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

### SqlServerInstances_RunManagedInstanceLinkAssessment

```java
/**
 * Samples for SqlServerEsuLicenses List.
 */
public final class SqlServerEsuLicensesListSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * ListSubscriptionSqlServerEsuLicense.json
     */
    /**
     * Sample code: Gets all SQL Server ESU licenses in a subscription.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        getsAllSQLServerESULicensesInASubscription(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerEsuLicenses().list(com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerInstances_RunMigrationAssessment

```java
/**
 * Samples for SqlServerAvailabilityGroups Delete.
 */
public final class SqlServerAvailabilityGroupsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * DeleteArcSqlServerAvailabilityGroup.json
     */
    /**
     * Sample code: Deletes a availability group resource.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        deletesAAvailabilityGroupResource(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerAvailabilityGroups()
            .delete("testrg", "testsqlInstanceAvailabilityGroup", "testAG", com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerInstances_Update

```java
import com.azure.resourcemanager.azurearcdata.models.SqlServerLicense;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SqlServerLicenses Update.
 */
public final class SqlServerLicensesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * UpdateSqlServerLicense.json
     */
    /**
     * Sample code: Patch a SQL Server license tags.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void patchASQLServerLicenseTags(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        SqlServerLicense resource = manager.sqlServerLicenses()
            .getByResourceGroupWithResponse("testrg", "testsqlServerLicense", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("mytag", "myval")).apply();
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

### SqlServerLicenses_Create

```java
import com.azure.resourcemanager.azurearcdata.models.SqlServerDatabaseResource;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SqlServerDatabases Update.
 */
public final class SqlServerDatabasesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * UpdateSqlServerDatabase.json
     */
    /**
     * Sample code: Update a database.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void updateADatabase(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        SqlServerDatabaseResource resource = manager.sqlServerDatabases()
            .getWithResponse("testrg", "testsqlManagedInstance", "testdb", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("mytag", "myval1")).apply();
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

### SqlServerLicenses_Delete

```java
/**
 * Samples for SqlServerEsuLicenses Delete.
 */
public final class SqlServerEsuLicensesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * DeleteSqlServerEsuLicense.json
     */
    /**
     * Sample code: Delete a SQL Server ESU license.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void deleteASQLServerESULicense(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerEsuLicenses()
            .deleteByResourceGroupWithResponse("testrg", "testsqlServerEsuLicense", com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerLicenses_GetByResourceGroup

```java
/**
 * Samples for DataControllers GetByResourceGroup.
 */
public final class DataControllersGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * GetDataController.json
     */
    /**
     * Sample code: Get a data controller.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void getADataController(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.dataControllers()
            .getByResourceGroupWithResponse("testrg", "testdataController", com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerLicenses_List

```java
/**
 * Samples for SqlServerDatabases List.
 */
public final class SqlServerDatabasesListSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * ListBySqlServerInstanceDatabase.json
     */
    /**
     * Sample code: Gets all databases associated with an Arc Enabled Sql server.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void getsAllDatabasesAssociatedWithAnArcEnabledSqlServer(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerDatabases().list("testrg", "testSqlServerInstance", com.azure.core.util.Context.NONE);
    }
}
```

### SqlServerLicenses_ListByResourceGroup

```java
import com.azure.resourcemanager.azurearcdata.models.ActiveDirectoryInformation;
import com.azure.resourcemanager.azurearcdata.models.ArcSqlManagedInstanceLicenseType;
import com.azure.resourcemanager.azurearcdata.models.BasicLoginInformation;
import com.azure.resourcemanager.azurearcdata.models.ExtendedLocation;
import com.azure.resourcemanager.azurearcdata.models.ExtendedLocationTypes;
import com.azure.resourcemanager.azurearcdata.models.K8SActiveDirectory;
import com.azure.resourcemanager.azurearcdata.models.K8SActiveDirectoryConnector;
import com.azure.resourcemanager.azurearcdata.models.K8SNetworkSettings;
import com.azure.resourcemanager.azurearcdata.models.K8SResourceRequirements;
import com.azure.resourcemanager.azurearcdata.models.K8SScheduling;
import com.azure.resourcemanager.azurearcdata.models.K8SSchedulingOptions;
import com.azure.resourcemanager.azurearcdata.models.K8SSecurity;
import com.azure.resourcemanager.azurearcdata.models.K8SSettings;
import com.azure.resourcemanager.azurearcdata.models.K8StransparentDataEncryption;
import com.azure.resourcemanager.azurearcdata.models.KeytabInformation;
import com.azure.resourcemanager.azurearcdata.models.SqlManagedInstanceK8SRaw;
import com.azure.resourcemanager.azurearcdata.models.SqlManagedInstanceK8SSpec;
import com.azure.resourcemanager.azurearcdata.models.SqlManagedInstanceProperties;
import com.azure.resourcemanager.azurearcdata.models.SqlManagedInstanceSku;
import com.azure.resourcemanager.azurearcdata.models.SqlManagedInstanceSkuTier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SqlManagedInstances Create.
 */
public final class SqlManagedInstancesCreateSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * CreateOrUpdateSqlManagedInstance.json
     */
    /**
     * Sample code: Create or update a SQL Managed Instance.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        createOrUpdateASQLManagedInstance(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlManagedInstances()
            .define("testsqlManagedInstance")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .withProperties(
                new SqlManagedInstanceProperties().withAdmin("Admin user")
                    .withStartTime("Instance start time")
                    .withEndTime("Instance end time")
                    .withK8SRaw(
                        new SqlManagedInstanceK8SRaw()
                            .withSpec(new SqlManagedInstanceK8SSpec()
                                .withScheduling(
                                    new K8SScheduling()
                                        .withDefaultProperty(
                                            new K8SSchedulingOptions()
                                                .withResources(new K8SResourceRequirements()
                                                    .withRequests(mapOf("additionalProperty", "additionalValue", "cpu",
                                                        "1", "memory", "8Gi"))
                                                    .withLimits(mapOf("additionalProperty", "additionalValue", "cpu",
                                                        "1", "memory", "8Gi"))
                                                    .withAdditionalProperties(mapOf()))
                                                .withAdditionalProperties(mapOf()))
                                        .withAdditionalProperties(mapOf()))
                                .withReplicas(1)
                                .withSecurity(new K8SSecurity()
                                    .withAdminLoginSecret("fakeTokenPlaceholder")
                                    .withServiceCertificateSecret("fakeTokenPlaceholder")
                                    .withActiveDirectory(new K8SActiveDirectory()
                                        .withConnector(new K8SActiveDirectoryConnector().withName("Name of connector")
                                            .withNamespace("Namespace of connector"))
                                        .withAccountName("Account name")
                                        .withKeytabSecret("fakeTokenPlaceholder")
                                        .withEncryptionTypes(
                                            Arrays.asList("Encryption type item1, Encryption type item2,...")))
                                    .withTransparentDataEncryption(
                                        new K8StransparentDataEncryption().withMode("SystemManaged"))
                                    .withAdditionalProperties(mapOf()))
                                .withSettings(
                                    new K8SSettings().withNetwork(new K8SNetworkSettings().withForceencryption(0)
                                        .withTlsciphers(
                                            "ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-ECDSA-AES128-SHA256:ECDHE-ECDSA-AES256-SHA384:ECDHE-RSA-AES128-SHA256:ECDHE-RSA-AES256-SHA384")
                                        .withTlsprotocols("1.2")).withAdditionalProperties(mapOf()))
                                .withAdditionalProperties(mapOf()))
                            .withAdditionalProperties(mapOf("additionalProperty", 1234)))
                    .withBasicLoginInformation(
                        new BasicLoginInformation().withUsername("username").withPassword("fakeTokenPlaceholder"))
                    .withActiveDirectoryInformation(new ActiveDirectoryInformation()
                        .withKeytabInformation(new KeytabInformation().withKeytab("fakeTokenPlaceholder")))
                    .withLicenseType(ArcSqlManagedInstanceLicenseType.LICENSE_INCLUDED)
                    .withClusterId(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Kubernetes/connectedClusters/connectedk8s")
                    .withExtensionId(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Kubernetes/connectedClusters/connectedk8s/providers/Microsoft.KubernetesConfiguration/extensions/extension"))
            .withTags(mapOf("mytag", "myval"))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.ExtendedLocation/customLocations/arclocation")
                .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .withSku(new SqlManagedInstanceSku().withTier(SqlManagedInstanceSkuTier.GENERAL_PURPOSE).withDev(true))
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

### SqlServerLicenses_Update

```java
/**
 * Samples for FailoverGroups Delete.
 */
public final class FailoverGroupsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2025-03-01-preview/examples/
     * DeleteFailoverGroup.json
     */
    /**
     * Sample code: Deletes a failover group instance.
     * 
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void
        deletesAFailoverGroupInstance(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.failoverGroups()
            .delete("testrg", "testSqlManagedInstance", "testFailoverGroupName", com.azure.core.util.Context.NONE);
    }
}
```

