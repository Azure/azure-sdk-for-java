# Code snippets and samples


## AssessedMachinesOperations

- [Get](#assessedmachinesoperations_get)
- [ListByAssessment](#assessedmachinesoperations_listbyassessment)

## AssessedSqlDatabaseV2Operations

- [Get](#assessedsqldatabasev2operations_get)
- [ListBySqlAssessmentV2](#assessedsqldatabasev2operations_listbysqlassessmentv2)

## AssessedSqlInstanceV2Operations

- [Get](#assessedsqlinstancev2operations_get)
- [ListBySqlAssessmentV2](#assessedsqlinstancev2operations_listbysqlassessmentv2)

## AssessedSqlMachinesOperations

- [Get](#assessedsqlmachinesoperations_get)
- [ListBySqlAssessmentV2](#assessedsqlmachinesoperations_listbysqlassessmentv2)

## AssessedSqlRecommendedEntityOperations

- [Get](#assessedsqlrecommendedentityoperations_get)
- [ListBySqlAssessmentV2](#assessedsqlrecommendedentityoperations_listbysqlassessmentv2)

## AssessmentOptionsOperations

- [Get](#assessmentoptionsoperations_get)
- [ListByAssessmentProject](#assessmentoptionsoperations_listbyassessmentproject)

## AssessmentProjectSummaryOperations

- [Get](#assessmentprojectsummaryoperations_get)
- [ListByAssessmentProject](#assessmentprojectsummaryoperations_listbyassessmentproject)

## AssessmentProjectsOperations

- [Create](#assessmentprojectsoperations_create)
- [Delete](#assessmentprojectsoperations_delete)
- [GetByResourceGroup](#assessmentprojectsoperations_getbyresourcegroup)
- [List](#assessmentprojectsoperations_list)
- [ListByResourceGroup](#assessmentprojectsoperations_listbyresourcegroup)
- [Update](#assessmentprojectsoperations_update)

## AssessmentsOperations

- [Create](#assessmentsoperations_create)
- [Delete](#assessmentsoperations_delete)
- [DownloadUrl](#assessmentsoperations_downloadurl)
- [Get](#assessmentsoperations_get)
- [ListByGroup](#assessmentsoperations_listbygroup)

## AvsAssessedMachinesOperations

- [Get](#avsassessedmachinesoperations_get)
- [ListByAvsAssessment](#avsassessedmachinesoperations_listbyavsassessment)

## AvsAssessmentOptionsOperations

- [Get](#avsassessmentoptionsoperations_get)
- [ListByAssessmentProject](#avsassessmentoptionsoperations_listbyassessmentproject)

## AvsAssessmentsOperations

- [Create](#avsassessmentsoperations_create)
- [Delete](#avsassessmentsoperations_delete)
- [DownloadUrl](#avsassessmentsoperations_downloadurl)
- [Get](#avsassessmentsoperations_get)
- [ListByGroup](#avsassessmentsoperations_listbygroup)

## GroupsOperations

- [Create](#groupsoperations_create)
- [Delete](#groupsoperations_delete)
- [Get](#groupsoperations_get)
- [ListByAssessmentProject](#groupsoperations_listbyassessmentproject)
- [UpdateMachines](#groupsoperations_updatemachines)

## HypervCollectorsOperations

- [Create](#hypervcollectorsoperations_create)
- [Delete](#hypervcollectorsoperations_delete)
- [Get](#hypervcollectorsoperations_get)
- [ListByAssessmentProject](#hypervcollectorsoperations_listbyassessmentproject)

## ImportCollectorsOperations

- [Create](#importcollectorsoperations_create)
- [Delete](#importcollectorsoperations_delete)
- [Get](#importcollectorsoperations_get)
- [ListByAssessmentProject](#importcollectorsoperations_listbyassessmentproject)

## MachinesOperations

- [Get](#machinesoperations_get)
- [ListByAssessmentProject](#machinesoperations_listbyassessmentproject)

## Operations

- [List](#operations_list)

## PrivateEndpointConnectionOperations

- [Delete](#privateendpointconnectionoperations_delete)
- [Get](#privateendpointconnectionoperations_get)
- [ListByAssessmentProject](#privateendpointconnectionoperations_listbyassessmentproject)
- [Update](#privateendpointconnectionoperations_update)

## PrivateLinkResourceOperations

- [Get](#privatelinkresourceoperations_get)
- [ListByAssessmentProject](#privatelinkresourceoperations_listbyassessmentproject)

## ServerCollectorsOperations

- [Create](#servercollectorsoperations_create)
- [Delete](#servercollectorsoperations_delete)
- [Get](#servercollectorsoperations_get)
- [ListByAssessmentProject](#servercollectorsoperations_listbyassessmentproject)

## SqlAssessmentOptionsOperations

- [Get](#sqlassessmentoptionsoperations_get)
- [ListByAssessmentProject](#sqlassessmentoptionsoperations_listbyassessmentproject)

## SqlAssessmentV2Operations

- [Create](#sqlassessmentv2operations_create)
- [Delete](#sqlassessmentv2operations_delete)
- [DownloadUrl](#sqlassessmentv2operations_downloadurl)
- [Get](#sqlassessmentv2operations_get)
- [ListByGroup](#sqlassessmentv2operations_listbygroup)

## SqlAssessmentV2SummaryOperations

- [Get](#sqlassessmentv2summaryoperations_get)
- [ListBySqlAssessmentV2](#sqlassessmentv2summaryoperations_listbysqlassessmentv2)

## SqlCollectorOperations

- [Create](#sqlcollectoroperations_create)
- [Delete](#sqlcollectoroperations_delete)
- [Get](#sqlcollectoroperations_get)
- [ListByAssessmentProject](#sqlcollectoroperations_listbyassessmentproject)

## VmwareCollectorsOperations

- [Create](#vmwarecollectorsoperations_create)
- [Delete](#vmwarecollectorsoperations_delete)
- [Get](#vmwarecollectorsoperations_get)
- [ListByAssessmentProject](#vmwarecollectorsoperations_listbyassessmentproject)
### AssessedMachinesOperations_Get

```java
/**
 * Samples for AssessedMachinesOperations Get.
 */
public final class AssessedMachinesOperationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessedMachinesOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessedMachinesOperations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessedMachinesOperationsGetMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.assessedMachinesOperations()
            .getWithResponse("rgopenapi", "pavqtntysjn", "smawqdmhfngray", "qjlumxyqsitd", "oqxjeheiipjmuo",
                com.azure.core.util.Context.NONE);
    }
}
```

### AssessedMachinesOperations_ListByAssessment

```java
/**
 * Samples for AssessedMachinesOperations ListByAssessment.
 */
public final class AssessedMachinesOperationsListByAssessmentSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessedMachinesOperations_ListByAssessment_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessedMachinesOperations_ListByAssessment_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessedMachinesOperationsListByAssessmentMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.assessedMachinesOperations()
            .listByAssessment("rgopenapi", "sloqixzfjk", "kjuepxerwseq", "rhzcmubwrrkhtocsibu", "sbkdovsfqldhdb", 10,
                "hbyseetshbplfkjmpjhsiurqgt", 25, com.azure.core.util.Context.NONE);
    }
}
```

### AssessedSqlDatabaseV2Operations_Get

```java
/**
 * Samples for AssessedSqlDatabaseV2Operations Get.
 */
public final class AssessedSqlDatabaseV2OperationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessedSqlDatabaseV2Operations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessedSqlDatabaseV2Operations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessedSqlDatabaseV2OperationsGetMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.assessedSqlDatabaseV2Operations()
            .getWithResponse("rgmigrate", "fci-test6904project", "test_fci_hadr", "test_swagger_1",
                "858eb860-9e07-417c-91b6-bca1bffb3bf5", com.azure.core.util.Context.NONE);
    }
}
```

### AssessedSqlDatabaseV2Operations_ListBySqlAssessmentV2

```java
/**
 * Samples for AssessedSqlDatabaseV2Operations ListBySqlAssessmentV2.
 */
public final class AssessedSqlDatabaseV2OperationsListBySqlAssessmentV2Samples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessedSqlDatabaseV2Operations_ListBySqlAssessmentV2_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessedSqlDatabaseV2Operations_ListBySqlAssessmentV2_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessedSqlDatabaseV2OperationsListBySqlAssessmentV2MaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.assessedSqlDatabaseV2Operations()
            .listBySqlAssessmentV2("rgmigrate", "fci-test6904project", "test_fci_hadr", "test_swagger_1",
                "(contains(Properties/DatabaseName,'adv130'))", 23, null, 1, com.azure.core.util.Context.NONE);
    }
}
```

### AssessedSqlInstanceV2Operations_Get

```java
/**
 * Samples for AssessedSqlInstanceV2Operations Get.
 */
public final class AssessedSqlInstanceV2OperationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessedSqlInstanceV2Operations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessedSqlInstanceV2Operations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessedSqlInstanceV2OperationsGetMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.assessedSqlInstanceV2Operations()
            .getWithResponse("rgmigrate", "fci-test6904project", "test_fci_hadr", "test_swagger_1",
                "3c6574cf-b4e1-4fdc-93db-6bbcc570dda2", com.azure.core.util.Context.NONE);
    }
}
```

### AssessedSqlInstanceV2Operations_ListBySqlAssessmentV2

```java
/**
 * Samples for AssessedSqlInstanceV2Operations ListBySqlAssessmentV2.
 */
public final class AssessedSqlInstanceV2OperationsListBySqlAssessmentV2Samples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessedSqlInstanceV2Operations_ListBySqlAssessmentV2_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessedSqlInstanceV2Operations_ListBySqlAssessmentV2_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessedSqlInstanceV2OperationsListBySqlAssessmentV2MaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.assessedSqlInstanceV2Operations()
            .listBySqlAssessmentV2("rgmigrate", "fci-test6904project", "test_fci_hadr", "test_swagger_1",
                "(contains(Properties/InstanceName,'MSSQLSERVER'))", 23, null, 1, com.azure.core.util.Context.NONE);
    }
}
```

### AssessedSqlMachinesOperations_Get

```java
/**
 * Samples for AssessedSqlMachinesOperations Get.
 */
public final class AssessedSqlMachinesOperationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessedSqlMachinesOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessedSqlMachinesOperations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessedSqlMachinesOperationsGetMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.assessedSqlMachinesOperations()
            .getWithResponse("rgmigrate", "fci-test6904project", "test_fci_hadr", "test_swagger_1",
                "cc64c9dc-b38e-435d-85ad-d509df5d92c6", com.azure.core.util.Context.NONE);
    }
}
```

### AssessedSqlMachinesOperations_ListBySqlAssessmentV2

```java
/**
 * Samples for AssessedSqlMachinesOperations ListBySqlAssessmentV2.
 */
public final class AssessedSqlMachinesOperationsListBySqlAssessmentV2Samples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessedSqlMachinesOperations_ListBySqlAssessmentV2_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessedSqlMachinesOperations_ListBySqlAssessmentV2_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessedSqlMachinesOperationsListBySqlAssessmentV2MaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.assessedSqlMachinesOperations()
            .listBySqlAssessmentV2("rgmigrate", "fci-test6904project", "test_fci_hadr", "test_swagger_1",
                "(contains(Properties/DisplayName,'SQLHAVM17'))", 23, null, 1, com.azure.core.util.Context.NONE);
    }
}
```

### AssessedSqlRecommendedEntityOperations_Get

```java
/**
 * Samples for AssessedSqlRecommendedEntityOperations Get.
 */
public final class AssessedSqlRecommendedEntityOperationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessedSqlRecommendedEntityOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessedSqlRecommendedEntityOperations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessedSqlRecommendedEntityOperationsGetMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.assessedSqlRecommendedEntityOperations()
            .getWithResponse("rgmigrate", "fci-test6904project", "test_fci_hadr", "test_swagger_1",
                "cc64c9dc-b38e-435d-85ad-d509df5d92c6", com.azure.core.util.Context.NONE);
    }
}
```

### AssessedSqlRecommendedEntityOperations_ListBySqlAssessmentV2

```java
/**
 * Samples for AssessedSqlRecommendedEntityOperations ListBySqlAssessmentV2.
 */
public final class AssessedSqlRecommendedEntityOperationsListBySqlAssessmentV2Samples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessedSqlRecommendedEntityOperations_ListBySqlAssessmentV2_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessedSqlRecommendedEntityOperations_ListBySqlAssessmentV2_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessedSqlRecommendedEntityOperationsListBySqlAssessmentV2MaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.assessedSqlRecommendedEntityOperations()
            .listBySqlAssessmentV2("rgmigrate", "fci-test6904project", "test_fci_hadr", "test_swagger_1",
                "(contains(Properties/InstanceName,'MSSQLSERVER'))", 23, null, 1, com.azure.core.util.Context.NONE);
    }
}
```

### AssessmentOptionsOperations_Get

```java
/**
 * Samples for AssessmentOptionsOperations Get.
 */
public final class AssessmentOptionsOperationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessmentOptionsOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessmentOptionsOperations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessmentOptionsOperationsGetMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.assessmentOptionsOperations()
            .getWithResponse("ayagrawrg", "app18700project", "default", com.azure.core.util.Context.NONE);
    }
}
```

### AssessmentOptionsOperations_ListByAssessmentProject

```java
/**
 * Samples for AssessmentOptionsOperations ListByAssessmentProject.
 */
public final class AssessmentOptionsOperationsListByAssessmentProjectSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessmentOptionsOperations_ListByAssessmentProject_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessmentOptionsOperations_ListByAssessmentProject_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessmentOptionsOperationsListByAssessmentProjectMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.assessmentOptionsOperations()
            .listByAssessmentProject("rgmigrate", "fhodvffhuoqwbysrrqbizete", com.azure.core.util.Context.NONE);
    }
}
```

### AssessmentProjectSummaryOperations_Get

```java
/**
 * Samples for AssessmentProjectSummaryOperations Get.
 */
public final class AssessmentProjectSummaryOperationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessmentProjectSummaryOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessmentProjectSummaryOperations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessmentProjectSummaryOperationsGetMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.assessmentProjectSummaryOperations()
            .getWithResponse("piyushapp1", "PiyushApp15328project", "default", com.azure.core.util.Context.NONE);
    }
}
```

### AssessmentProjectSummaryOperations_ListByAssessmentProject

```java
/**
 * Samples for AssessmentProjectSummaryOperations ListByAssessmentProject.
 */
public final class AssessmentProjectSummaryOperationsListByAssessmentProjectSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessmentProjectSummaryOperations_ListByAssessmentProject_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessmentProjectSummaryOperations_ListByAssessmentProject_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessmentProjectSummaryOperationsListByAssessmentProjectMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.assessmentProjectSummaryOperations()
            .listByAssessmentProject("piyushapp1", "PiyushApp15328project", com.azure.core.util.Context.NONE);
    }
}
```

### AssessmentProjectsOperations_Create

```java
import com.azure.resourcemanager.migration.assessment.models.ProjectProperties;
import com.azure.resourcemanager.migration.assessment.models.ProjectStatus;
import com.azure.resourcemanager.migration.assessment.models.ProvisioningState;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AssessmentProjectsOperations Create.
 */
public final class AssessmentProjectsOperationsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessmentProjectsOperations_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessmentProjectsOperations_Create_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessmentProjectsOperationsCreateMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.assessmentProjectsOperations()
            .define("sakanwar1204project")
            .withRegion("southeastasia")
            .withExistingResourceGroup("sakanwar")
            .withTags(mapOf("Migrate Project", "sakanwar-PE-SEA"))
            .withProperties(new ProjectProperties().withProvisioningState(ProvisioningState.SUCCEEDED)
                .withAssessmentSolutionId(
                    "/subscriptions/4bd2aa0f-2bd2-4d67-91a8-5a4533d58600/resourceGroups/sakanwar/providers/Microsoft.Storage/storageAccounts/sakanwar1204usa")
                .withProjectStatus(ProjectStatus.ACTIVE)
                .withPublicNetworkAccess("Disabled")
                .withCustomerStorageAccountArmId(
                    "/subscriptions/4bd2aa0f-2bd2-4d67-91a8-5a4533d58600/resourceGroups/sakanwar/providers/Microsoft.Storage/storageAccounts/sakanwar1204usa"))
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

### AssessmentProjectsOperations_Delete

```java
/**
 * Samples for AssessmentProjectsOperations Delete.
 */
public final class AssessmentProjectsOperationsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessmentProjectsOperations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessmentProjectsOperations_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessmentProjectsOperationsDeleteMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.assessmentProjectsOperations()
            .deleteByResourceGroupWithResponse("rgmigrate", "zqrsyncwahgydqvwuchkfd", com.azure.core.util.Context.NONE);
    }
}
```

### AssessmentProjectsOperations_GetByResourceGroup

```java
/**
 * Samples for AssessmentProjectsOperations GetByResourceGroup.
 */
public final class AssessmentProjectsOperationsGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessmentProjectsOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessmentProjectsOperations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessmentProjectsOperationsGetMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.assessmentProjectsOperations()
            .getByResourceGroupWithResponse("sakanwar", "sakanwar1204project", com.azure.core.util.Context.NONE);
    }
}
```

### AssessmentProjectsOperations_List

```java
/**
 * Samples for AssessmentProjectsOperations List.
 */
public final class AssessmentProjectsOperationsListSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessmentProjectsOperations_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessmentProjectsOperations_ListBySubscription_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessmentProjectsOperationsListBySubscriptionMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.assessmentProjectsOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### AssessmentProjectsOperations_ListByResourceGroup

```java
/**
 * Samples for AssessmentProjectsOperations ListByResourceGroup.
 */
public final class AssessmentProjectsOperationsListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessmentProjectsOperations_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessmentProjectsOperations_ListByResourceGroup_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessmentProjectsOperationsListByResourceGroupMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.assessmentProjectsOperations().listByResourceGroup("sakanwar", com.azure.core.util.Context.NONE);
    }
}
```

### AssessmentProjectsOperations_Update

```java
import com.azure.resourcemanager.migration.assessment.models.AssessmentProject;
import com.azure.resourcemanager.migration.assessment.models.AssessmentProjectUpdateProperties;
import com.azure.resourcemanager.migration.assessment.models.ProjectStatus;
import com.azure.resourcemanager.migration.assessment.models.ProvisioningState;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AssessmentProjectsOperations Update.
 */
public final class AssessmentProjectsOperationsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessmentProjectsOperations_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessmentProjectsOperations_Update_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessmentProjectsOperationsUpdateMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        AssessmentProject resource = manager.assessmentProjectsOperations()
            .getByResourceGroupWithResponse("sakanwar", "sakanwar1204project", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("Migrate Project", "sakanwar-PE-SEA"))
            .withProperties(new AssessmentProjectUpdateProperties().withAssessmentSolutionId(
                "/subscriptions/4bd2aa0f-2bd2-4d67-91a8-5a4533d58600/resourceGroups/sakanwar/providers/Microsoft.Storage/storageAccounts/sakanwar1204usa")
                .withProjectStatus(ProjectStatus.ACTIVE)
                .withPublicNetworkAccess("Disabled")
                .withCustomerStorageAccountArmId(
                    "/subscriptions/4bd2aa0f-2bd2-4d67-91a8-5a4533d58600/resourceGroups/sakanwar/providers/Microsoft.Storage/storageAccounts/sakanwar1204usa")
                .withProvisioningState(ProvisioningState.SUCCEEDED))
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

### AssessmentsOperations_Create

```java
import com.azure.resourcemanager.migration.assessment.models.AssessmentSizingCriterion;
import com.azure.resourcemanager.migration.assessment.models.AzureCurrency;
import com.azure.resourcemanager.migration.assessment.models.AzureDiskType;
import com.azure.resourcemanager.migration.assessment.models.AzureHybridUseBenefit;
import com.azure.resourcemanager.migration.assessment.models.AzureOfferCode;
import com.azure.resourcemanager.migration.assessment.models.AzurePricingTier;
import com.azure.resourcemanager.migration.assessment.models.AzureReservedInstance;
import com.azure.resourcemanager.migration.assessment.models.AzureStorageRedundancy;
import com.azure.resourcemanager.migration.assessment.models.AzureVmFamily;
import com.azure.resourcemanager.migration.assessment.models.MachineAssessmentProperties;
import com.azure.resourcemanager.migration.assessment.models.Percentile;
import com.azure.resourcemanager.migration.assessment.models.ProvisioningState;
import com.azure.resourcemanager.migration.assessment.models.TimeRange;
import com.azure.resourcemanager.migration.assessment.models.VmUptime;
import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Samples for AssessmentsOperations Create.
 */
public final class AssessmentsOperationsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessmentsOperations_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessmentsOperations_Create_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessmentsOperationsCreateMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.assessmentsOperations()
            .define("asm1")
            .withExistingGroup("ayagrawrg", "app18700project", "kuchatur-test")
            .withProperties(new MachineAssessmentProperties().withProvisioningState(ProvisioningState.SUCCEEDED)
                .withEaSubscriptionId("kwsu")
                .withAzurePricingTier(AzurePricingTier.STANDARD)
                .withAzureStorageRedundancy(AzureStorageRedundancy.UNKNOWN)
                .withReservedInstance(AzureReservedInstance.NONE)
                .withAzureHybridUseBenefit(AzureHybridUseBenefit.UNKNOWN)
                .withAzureDiskTypes(Arrays.asList(AzureDiskType.PREMIUM, AzureDiskType.STANDARD_SSD))
                .withAzureVmFamilies(
                    Arrays.asList(AzureVmFamily.D_SERIES, AzureVmFamily.LSV2_SERIES, AzureVmFamily.M_SERIES,
                        AzureVmFamily.MDSV2_SERIES, AzureVmFamily.MSV2_SERIES, AzureVmFamily.MV2_SERIES))
                .withVmUptime(new VmUptime().withDaysPerMonth(13).withHoursPerDay(26))
                .withAzureLocation("njxbwdtsxzhichsnk")
                .withAzureOfferCode(AzureOfferCode.UNKNOWN)
                .withCurrency(AzureCurrency.UNKNOWN)
                .withScalingFactor(24.0F)
                .withPercentile(Percentile.PERCENTILE50)
                .withTimeRange(TimeRange.DAY)
                .withPerfDataStartTime(OffsetDateTime.parse("2023-09-26T09:36:48.491Z"))
                .withPerfDataEndTime(OffsetDateTime.parse("2023-09-26T09:36:48.491Z"))
                .withDiscountPercentage(6.0F)
                .withSizingCriterion(AssessmentSizingCriterion.PERFORMANCE_BASED))
            .create();
    }
}
```

### AssessmentsOperations_Delete

```java
/**
 * Samples for AssessmentsOperations Delete.
 */
public final class AssessmentsOperationsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessmentsOperations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessmentsOperations_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessmentsOperationsDeleteMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.assessmentsOperations()
            .deleteWithResponse("ayagrawrg", "app18700project", "kuchatur-test", "asm1",
                com.azure.core.util.Context.NONE);
    }
}
```

### AssessmentsOperations_DownloadUrl

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import java.io.IOException;

/**
 * Samples for AssessmentsOperations DownloadUrl.
 */
public final class AssessmentsOperationsDownloadUrlSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessmentsOperations_DownloadUrl_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessmentsOperations_DownloadUrl_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessmentsOperationsDownloadUrlMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) throws IOException {
        manager.assessmentsOperations()
            .downloadUrl("ayagrawrg", "app18700project", "kuchatur-test", "asm1",
                SerializerFactory.createDefaultManagementSerializerAdapter()
                    .deserialize("{}", Object.class, SerializerEncoding.JSON),
                com.azure.core.util.Context.NONE);
    }
}
```

### AssessmentsOperations_Get

```java
/**
 * Samples for AssessmentsOperations Get.
 */
public final class AssessmentsOperationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessmentsOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessmentsOperations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessmentsOperationsGetMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.assessmentsOperations()
            .getWithResponse("ayagrawrg", "app18700project", "kuchatur-test", "asm1", com.azure.core.util.Context.NONE);
    }
}
```

### AssessmentsOperations_ListByGroup

```java
/**
 * Samples for AssessmentsOperations ListByGroup.
 */
public final class AssessmentsOperationsListByGroupSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AssessmentsOperations_ListByGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessmentsOperations_ListByGroup_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void assessmentsOperationsListByGroupMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.assessmentsOperations()
            .listByGroup("ayagrawrg", "app18700project", "kuchatur-test", com.azure.core.util.Context.NONE);
    }
}
```

### AvsAssessedMachinesOperations_Get

```java
/**
 * Samples for AvsAssessedMachinesOperations Get.
 */
public final class AvsAssessedMachinesOperationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AvsAssessedMachinesOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsAssessedMachinesOperations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void avsAssessedMachinesOperationsGetMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.avsAssessedMachinesOperations()
            .getWithResponse("ayagrawrg", "app18700project", "kuchatur-test", "asm2",
                "b6d6fc6f-796f-4c16-96af-a6d22e0f12f7", com.azure.core.util.Context.NONE);
    }
}
```

### AvsAssessedMachinesOperations_ListByAvsAssessment

```java
/**
 * Samples for AvsAssessedMachinesOperations ListByAvsAssessment.
 */
public final class AvsAssessedMachinesOperationsListByAvsAssessmentSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AvsAssessedMachinesOperations_ListByAvsAssessment_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsAssessedMachinesOperations_ListByAvsAssessment_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void avsAssessedMachinesOperationsListByAvsAssessmentMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.avsAssessedMachinesOperations()
            .listByAvsAssessment("ayagrawrg", "app18700project", "kuchatur-test", "asm2", "ujmwhhuloficljxcjyc", 6,
                "qwrjeiukbcicfrkqlqsfukfc", 19, com.azure.core.util.Context.NONE);
    }
}
```

### AvsAssessmentOptionsOperations_Get

```java
/**
 * Samples for AvsAssessmentOptionsOperations Get.
 */
public final class AvsAssessmentOptionsOperationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AvsAssessmentOptionsOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsAssessmentOptionsOperations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void avsAssessmentOptionsOperationsGetMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.avsAssessmentOptionsOperations()
            .getWithResponse("ayagrawrg", "app18700project", "default", com.azure.core.util.Context.NONE);
    }
}
```

### AvsAssessmentOptionsOperations_ListByAssessmentProject

```java
/**
 * Samples for AvsAssessmentOptionsOperations ListByAssessmentProject.
 */
public final class AvsAssessmentOptionsOperationsListByAssessmentProjectSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AvsAssessmentOptionsOperations_ListByAssessmentProject_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsAssessmentOptionsOperations_ListByAssessmentProject_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void avsAssessmentOptionsOperationsListByAssessmentProjectMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.avsAssessmentOptionsOperations()
            .listByAssessmentProject("ayagrawrg", "app18700project", com.azure.core.util.Context.NONE);
    }
}
```

### AvsAssessmentsOperations_Create

```java
import com.azure.resourcemanager.migration.assessment.models.AssessmentSizingCriterion;
import com.azure.resourcemanager.migration.assessment.models.AvsAssessmentProperties;
import com.azure.resourcemanager.migration.assessment.models.AzureAvsNodeType;
import com.azure.resourcemanager.migration.assessment.models.AzureCurrency;
import com.azure.resourcemanager.migration.assessment.models.AzureLocation;
import com.azure.resourcemanager.migration.assessment.models.AzureOfferCode;
import com.azure.resourcemanager.migration.assessment.models.AzureReservedInstance;
import com.azure.resourcemanager.migration.assessment.models.FttAndRaidLevel;
import com.azure.resourcemanager.migration.assessment.models.Percentile;
import com.azure.resourcemanager.migration.assessment.models.ProvisioningState;
import com.azure.resourcemanager.migration.assessment.models.TimeRange;
import java.time.OffsetDateTime;

/**
 * Samples for AvsAssessmentsOperations Create.
 */
public final class AvsAssessmentsOperationsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AvsAssessmentsOperations_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsAssessmentsOperations_Create_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void avsAssessmentsOperationsCreateMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.avsAssessmentsOperations()
            .define("asm2")
            .withExistingGroup("ayagrawrg", "app18700project", "kuchatur-test")
            .withProperties(new AvsAssessmentProperties().withProvisioningState(ProvisioningState.SUCCEEDED)
                .withFailuresToTolerateAndRaidLevel(FttAndRaidLevel.FTT1RAID1)
                .withVcpuOversubscription(4.0F)
                .withNodeType(AzureAvsNodeType.AV36)
                .withReservedInstance(AzureReservedInstance.RI3YEAR)
                .withMemOvercommit(1.0F)
                .withDedupeCompression(1.5F)
                .withIsStretchClusterEnabled(true)
                .withAzureLocation(AzureLocation.EAST_US)
                .withAzureOfferCode(AzureOfferCode.MSAZR0003P)
                .withCurrency(AzureCurrency.USD)
                .withScalingFactor(1.0F)
                .withPercentile(Percentile.PERCENTILE95)
                .withTimeRange(TimeRange.DAY)
                .withPerfDataStartTime(OffsetDateTime.parse("2023-09-25T13:35:56.5671462Z"))
                .withPerfDataEndTime(OffsetDateTime.parse("2023-09-26T13:35:56.5671462Z"))
                .withDiscountPercentage(0.0F)
                .withSizingCriterion(AssessmentSizingCriterion.AS_ON_PREMISES))
            .create();
    }
}
```

### AvsAssessmentsOperations_Delete

```java
/**
 * Samples for AvsAssessmentsOperations Delete.
 */
public final class AvsAssessmentsOperationsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AvsAssessmentsOperations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsAssessmentsOperations_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void avsAssessmentsOperationsDeleteMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.avsAssessmentsOperations()
            .deleteWithResponse("ayagrawrg", "app18700project", "kuchatur-test", "asm2",
                com.azure.core.util.Context.NONE);
    }
}
```

### AvsAssessmentsOperations_DownloadUrl

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import java.io.IOException;

/**
 * Samples for AvsAssessmentsOperations DownloadUrl.
 */
public final class AvsAssessmentsOperationsDownloadUrlSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AvsAssessmentsOperations_DownloadUrl_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsAssessmentsOperations_DownloadUrl_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void avsAssessmentsOperationsDownloadUrlMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) throws IOException {
        manager.avsAssessmentsOperations()
            .downloadUrl("ayagrawrg", "app18700project", "kuchatur-test", "asm2",
                SerializerFactory.createDefaultManagementSerializerAdapter()
                    .deserialize("{}", Object.class, SerializerEncoding.JSON),
                com.azure.core.util.Context.NONE);
    }
}
```

### AvsAssessmentsOperations_Get

```java
/**
 * Samples for AvsAssessmentsOperations Get.
 */
public final class AvsAssessmentsOperationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AvsAssessmentsOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsAssessmentsOperations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void avsAssessmentsOperationsGetMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.avsAssessmentsOperations()
            .getWithResponse("ayagrawrg", "app18700project", "kuchatur-test", "asm2", com.azure.core.util.Context.NONE);
    }
}
```

### AvsAssessmentsOperations_ListByGroup

```java
/**
 * Samples for AvsAssessmentsOperations ListByGroup.
 */
public final class AvsAssessmentsOperationsListByGroupSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * AvsAssessmentsOperations_ListByGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: AvsAssessmentsOperations_ListByGroup_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void avsAssessmentsOperationsListByGroupMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.avsAssessmentsOperations()
            .listByGroup("ayagrawrg", "app18700project", "kuchatur-test", com.azure.core.util.Context.NONE);
    }
}
```

### GroupsOperations_Create

```java
import com.azure.resourcemanager.migration.assessment.models.GroupProperties;
import com.azure.resourcemanager.migration.assessment.models.GroupType;
import com.azure.resourcemanager.migration.assessment.models.ProvisioningState;

/**
 * Samples for GroupsOperations Create.
 */
public final class GroupsOperationsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * GroupsOperations_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: GroupsOperations_Create_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void groupsOperationsCreateMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.groupsOperations()
            .define("kuchatur-test")
            .withExistingAssessmentProject("ayagrawrg", "app18700project")
            .withProperties(new GroupProperties().withProvisioningState(ProvisioningState.SUCCEEDED)
                .withGroupType(GroupType.DEFAULT))
            .create();
    }
}
```

### GroupsOperations_Delete

```java
/**
 * Samples for GroupsOperations Delete.
 */
public final class GroupsOperationsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * GroupsOperations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: GroupsOperations_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void groupsOperationsDeleteMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.groupsOperations()
            .deleteWithResponse("ayagrawrg", "app18700project", "kuchatur-test", com.azure.core.util.Context.NONE);
    }
}
```

### GroupsOperations_Get

```java
/**
 * Samples for GroupsOperations Get.
 */
public final class GroupsOperationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * GroupsOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: GroupsOperations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void groupsOperationsGetMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.groupsOperations()
            .getWithResponse("ayagrawrg", "app18700project", "kuchatur-test", com.azure.core.util.Context.NONE);
    }
}
```

### GroupsOperations_ListByAssessmentProject

```java
/**
 * Samples for GroupsOperations ListByAssessmentProject.
 */
public final class GroupsOperationsListByAssessmentProjectSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * GroupsOperations_ListByAssessmentProject_MaximumSet_Gen.json
     */
    /**
     * Sample code: GroupsOperations_ListByAssessmentProject_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void groupsOperationsListByAssessmentProjectMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.groupsOperations()
            .listByAssessmentProject("ayagrawrg", "app18700project", com.azure.core.util.Context.NONE);
    }
}
```

### GroupsOperations_UpdateMachines

```java
import com.azure.resourcemanager.migration.assessment.models.GroupBodyProperties;
import com.azure.resourcemanager.migration.assessment.models.GroupUpdateOperation;
import com.azure.resourcemanager.migration.assessment.models.UpdateGroupBody;
import java.util.Arrays;

/**
 * Samples for GroupsOperations UpdateMachines.
 */
public final class GroupsOperationsUpdateMachinesSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * GroupsOperations_UpdateMachines_MaximumSet_Gen.json
     */
    /**
     * Sample code: GroupsOperations_UpdateMachines_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void groupsOperationsUpdateMachinesMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.groupsOperations()
            .updateMachines("ayagrawrg", "app18700project", "kuchatur-test", new UpdateGroupBody().withEtag("*")
                .withProperties(new GroupBodyProperties().withOperationType(GroupUpdateOperation.ADD)
                    .withMachines(Arrays.asList(
                        "/subscriptions/4bd2aa0f-2bd2-4d67-91a8-5a4533d58600/resourceGroups/ayagrawrg/providers/Microsoft.Migrate/assessmentprojects/app18700project/machines/18895660-c5e5-4247-8cfc-cd24e1fe57f3"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### HypervCollectorsOperations_Create

```java
import com.azure.resourcemanager.migration.assessment.models.CollectorAgentPropertiesBase;
import com.azure.resourcemanager.migration.assessment.models.CollectorAgentSpnPropertiesBase;
import com.azure.resourcemanager.migration.assessment.models.CollectorPropertiesBaseWithAgent;
import com.azure.resourcemanager.migration.assessment.models.ProvisioningState;
import java.time.OffsetDateTime;

/**
 * Samples for HypervCollectorsOperations Create.
 */
public final class HypervCollectorsOperationsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * HypervCollectorsOperations_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: HypervCollectorsOperations_Create_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void hypervCollectorsOperationsCreateMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.hypervCollectorsOperations()
            .define("test-697cecollector")
            .withExistingAssessmentProject("ayagrawRG", "app18700project")
            .withProperties(new CollectorPropertiesBaseWithAgent().withProvisioningState(ProvisioningState.SUCCEEDED)
                .withAgentProperties(new CollectorAgentPropertiesBase().withId("12f1d90f-b3fa-4926-8893-e56803a09af0")
                    .withVersion("2.0.1993.19")
                    .withLastHeartbeatUtc(OffsetDateTime.parse("2022-07-07T14:25:35.708325Z"))
                    .withSpnDetails(new CollectorAgentSpnPropertiesBase()
                        .withAuthority("https://login.windows.net/72f988bf-86f1-41af-91ab-2d7cd011db47")
                        .withApplicationId("e3bd6eaa-980b-40ae-a30e-2a5069ba097c")
                        .withAudience("e3bd6eaa-980b-40ae-a30e-2a5069ba097c")
                        .withObjectId("01b9f9e2-2d82-414c-adaa-09ce259b6b44")
                        .withTenantId("72f988bf-86f1-41af-91ab-2d7cd011db47")))
                .withDiscoverySiteId(
                    "/subscriptions/4bd2aa0f-2bd2-4d67-91a8-5a4533d58600/resourceGroups/ayagrawRG/providers/Microsoft.OffAzure/HyperVSites/test-60527site"))
            .create();
    }
}
```

### HypervCollectorsOperations_Delete

```java
/**
 * Samples for HypervCollectorsOperations Delete.
 */
public final class HypervCollectorsOperationsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * HypervCollectorsOperations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: HypervCollectorsOperations_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void hypervCollectorsOperationsDeleteMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.hypervCollectorsOperations()
            .deleteWithResponse("ayagrawRG", "app18700project", "test-697cecollector",
                com.azure.core.util.Context.NONE);
    }
}
```

### HypervCollectorsOperations_Get

```java
/**
 * Samples for HypervCollectorsOperations Get.
 */
public final class HypervCollectorsOperationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * HypervCollectorsOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: HypervCollectorsOperations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void hypervCollectorsOperationsGetMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.hypervCollectorsOperations()
            .getWithResponse("ayagrawRG", "app18700project", "test-697cecollector", com.azure.core.util.Context.NONE);
    }
}
```

### HypervCollectorsOperations_ListByAssessmentProject

```java
/**
 * Samples for HypervCollectorsOperations ListByAssessmentProject.
 */
public final class HypervCollectorsOperationsListByAssessmentProjectSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * HypervCollectorsOperations_ListByAssessmentProject_MaximumSet_Gen.json
     */
    /**
     * Sample code: HypervCollectorsOperations_ListByAssessmentProject_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void hypervCollectorsOperationsListByAssessmentProjectMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.hypervCollectorsOperations()
            .listByAssessmentProject("ayagrawRG", "app18700project", com.azure.core.util.Context.NONE);
    }
}
```

### ImportCollectorsOperations_Create

```java
import com.azure.resourcemanager.migration.assessment.models.CollectorPropertiesBase;
import com.azure.resourcemanager.migration.assessment.models.ProvisioningState;

/**
 * Samples for ImportCollectorsOperations Create.
 */
public final class ImportCollectorsOperationsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * ImportCollectorsOperations_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: ImportCollectorsOperations_Create_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void importCollectorsOperationsCreateMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.importCollectorsOperations()
            .define("importCollectore7d5")
            .withExistingAssessmentProject("ayagrawRG", "app18700project")
            .withProperties(new CollectorPropertiesBase().withProvisioningState(ProvisioningState.SUCCEEDED)
                .withDiscoverySiteId(
                    "/subscriptions/4bd2aa0f-2bd2-4d67-91a8-5a4533d58600/resourcegroups/ayagrawRG/providers/microsoft.offazure/importsites/actualSEA37d4importSite"))
            .create();
    }
}
```

### ImportCollectorsOperations_Delete

```java
/**
 * Samples for ImportCollectorsOperations Delete.
 */
public final class ImportCollectorsOperationsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * ImportCollectorsOperations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: ImportCollectorsOperations_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void importCollectorsOperationsDeleteMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.importCollectorsOperations()
            .deleteWithResponse("ayagrawRG", "app18700project", "importCollectore7d5",
                com.azure.core.util.Context.NONE);
    }
}
```

### ImportCollectorsOperations_Get

```java
/**
 * Samples for ImportCollectorsOperations Get.
 */
public final class ImportCollectorsOperationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * ImportCollectorsOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ImportCollectorsOperations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void importCollectorsOperationsGetMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.importCollectorsOperations()
            .getWithResponse("ayagrawRG", "app18700project", "importCollectore7d5", com.azure.core.util.Context.NONE);
    }
}
```

### ImportCollectorsOperations_ListByAssessmentProject

```java
/**
 * Samples for ImportCollectorsOperations ListByAssessmentProject.
 */
public final class ImportCollectorsOperationsListByAssessmentProjectSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * ImportCollectorsOperations_ListByAssessmentProject_MaximumSet_Gen.json
     */
    /**
     * Sample code: ImportCollectorsOperations_ListByAssessmentProject_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void importCollectorsOperationsListByAssessmentProjectMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.importCollectorsOperations()
            .listByAssessmentProject("ayagrawRG", "app18700project", com.azure.core.util.Context.NONE);
    }
}
```

### MachinesOperations_Get

```java
/**
 * Samples for MachinesOperations Get.
 */
public final class MachinesOperationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * MachinesOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: MachinesOperations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void machinesOperationsGetMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.machinesOperations()
            .getWithResponse("ayagrawrg", "app18700project", "55082b89-99e2-4c40-b63f-d4f4d6ba961d",
                com.azure.core.util.Context.NONE);
    }
}
```

### MachinesOperations_ListByAssessmentProject

```java
/**
 * Samples for MachinesOperations ListByAssessmentProject.
 */
public final class MachinesOperationsListByAssessmentProjectSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * MachinesOperations_ListByAssessmentProject_MaximumSet_Gen.json
     */
    /**
     * Sample code: MachinesOperations_ListByAssessmentProject_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void machinesOperationsListByAssessmentProjectMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.machinesOperations()
            .listByAssessmentProject("ayagrawrg", "app18700project", null, 1, null, 1,
                com.azure.core.util.Context.NONE);
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
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void
        operationsListMaximumSetGen(com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnectionOperations_Delete

```java
/**
 * Samples for PrivateEndpointConnectionOperations Delete.
 */
public final class PrivateEndpointConnectionOperationsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * PrivateEndpointConnectionOperations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrivateEndpointConnectionOperations_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void privateEndpointConnectionOperationsDeleteMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.privateEndpointConnectionOperations()
            .deleteWithResponse("sakanwar", "sakanwar1204project",
                "sakanwar1204project1634pe.bf42f8a1-09f5-4ee4-aea6-a019cc60f9d7", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnectionOperations_Get

```java
/**
 * Samples for PrivateEndpointConnectionOperations Get.
 */
public final class PrivateEndpointConnectionOperationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * PrivateEndpointConnectionOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrivateEndpointConnectionOperations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void privateEndpointConnectionOperationsGetMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.privateEndpointConnectionOperations()
            .getWithResponse("sakanwar", "sakanwar1204project",
                "sakanwar1204project1634pe.bf42f8a1-09f5-4ee4-aea6-a019cc60f9d7", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnectionOperations_ListByAssessmentProject

```java
/**
 * Samples for PrivateEndpointConnectionOperations ListByAssessmentProject.
 */
public final class PrivateEndpointConnectionOperationsListByAssessmentProjectSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * PrivateEndpointConnectionOperations_ListByAssessmentProject_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrivateEndpointConnectionOperations_ListByAssessmentProject_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void privateEndpointConnectionOperationsListByAssessmentProjectMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.privateEndpointConnectionOperations()
            .listByAssessmentProject("sakanwar", "sakanwar1204project", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnectionOperations_Update

```java
import com.azure.resourcemanager.migration.assessment.fluent.models.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.migration.assessment.models.PrivateEndpoint;
import com.azure.resourcemanager.migration.assessment.models.PrivateEndpointConnectionProperties;
import com.azure.resourcemanager.migration.assessment.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.migration.assessment.models.PrivateLinkServiceConnectionState;

/**
 * Samples for PrivateEndpointConnectionOperations Update.
 */
public final class PrivateEndpointConnectionOperationsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * PrivateEndpointConnectionOperations_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrivateEndpointConnectionOperations_Update_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void privateEndpointConnectionOperationsUpdateMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.privateEndpointConnectionOperations()
            .update("sakanwar", "sakanwar1204project", "sakanwar1204project1634pe.bf42f8a1-09f5-4ee4-aea6-a019cc60f9d7",
                new PrivateEndpointConnectionInner()
                    .withProperties(new PrivateEndpointConnectionProperties().withPrivateEndpoint(new PrivateEndpoint())
                        .withPrivateLinkServiceConnectionState(new PrivateLinkServiceConnectionState()
                            .withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                            .withActionsRequired(""))),
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResourceOperations_Get

```java
/**
 * Samples for PrivateLinkResourceOperations Get.
 */
public final class PrivateLinkResourceOperationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * PrivateLinkResourceOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrivateLinkResourceOperations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void privateLinkResourceOperationsGetMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.privateLinkResourceOperations()
            .getWithResponse("sakanwar", "sakanwar1204project", "Default", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResourceOperations_ListByAssessmentProject

```java
/**
 * Samples for PrivateLinkResourceOperations ListByAssessmentProject.
 */
public final class PrivateLinkResourceOperationsListByAssessmentProjectSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * PrivateLinkResourceOperations_ListByAssessmentProject_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrivateLinkResourceOperations_ListByAssessmentProject_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void privateLinkResourceOperationsListByAssessmentProjectMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.privateLinkResourceOperations()
            .listByAssessmentProject("sakanwar", "sakanwar1204project", com.azure.core.util.Context.NONE);
    }
}
```

### ServerCollectorsOperations_Create

```java
import com.azure.resourcemanager.migration.assessment.models.CollectorAgentPropertiesBase;
import com.azure.resourcemanager.migration.assessment.models.CollectorAgentSpnPropertiesBase;
import com.azure.resourcemanager.migration.assessment.models.CollectorPropertiesBaseWithAgent;
import com.azure.resourcemanager.migration.assessment.models.ProvisioningState;

/**
 * Samples for ServerCollectorsOperations Create.
 */
public final class ServerCollectorsOperationsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * ServerCollectorsOperations_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: ServerCollectorsOperations_Create_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void serverCollectorsOperationsCreateMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.serverCollectorsOperations()
            .define("walter389fcollector")
            .withExistingAssessmentProject("ayagrawRG", "app18700project")
            .withProperties(new CollectorPropertiesBaseWithAgent().withProvisioningState(ProvisioningState.SUCCEEDED)
                .withAgentProperties(new CollectorAgentPropertiesBase().withId("498e4965-bbb1-47c2-8613-345baff9c509")
                    .withSpnDetails(new CollectorAgentSpnPropertiesBase()
                        .withAuthority("https://login.windows.net/72f988bf-86f1-41af-91ab-2d7cd011db47")
                        .withApplicationId("65153d2f-9afb-44e8-b3ca-1369150b7354")
                        .withAudience("65153d2f-9afb-44e8-b3ca-1369150b7354")
                        .withObjectId("ddde6f96-87c8-420b-9d4d-f16a5090519e")
                        .withTenantId("72f988bf-86f1-41af-91ab-2d7cd011db47")))
                .withDiscoverySiteId(
                    "/subscriptions/4bd2aa0f-2bd2-4d67-91a8-5a4533d58600/resourceGroups/ayagrawRG/providers/Microsoft.OffAzure/ServerSites/walter7155site"))
            .create();
    }
}
```

### ServerCollectorsOperations_Delete

```java
/**
 * Samples for ServerCollectorsOperations Delete.
 */
public final class ServerCollectorsOperationsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * ServerCollectorsOperations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: ServerCollectorsOperations_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void serverCollectorsOperationsDeleteMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.serverCollectorsOperations()
            .deleteWithResponse("ayagrawRG", "app18700project", "walter389fcollector",
                com.azure.core.util.Context.NONE);
    }
}
```

### ServerCollectorsOperations_Get

```java
/**
 * Samples for ServerCollectorsOperations Get.
 */
public final class ServerCollectorsOperationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * ServerCollectorsOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ServerCollectorsOperations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void serverCollectorsOperationsGetMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.serverCollectorsOperations()
            .getWithResponse("ayagrawRG", "app18700project", "walter389fcollector", com.azure.core.util.Context.NONE);
    }
}
```

### ServerCollectorsOperations_ListByAssessmentProject

```java
/**
 * Samples for ServerCollectorsOperations ListByAssessmentProject.
 */
public final class ServerCollectorsOperationsListByAssessmentProjectSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * ServerCollectorsOperations_ListByAssessmentProject_MaximumSet_Gen.json
     */
    /**
     * Sample code: ServerCollectorsOperations_ListByAssessmentProject_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void serverCollectorsOperationsListByAssessmentProjectMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.serverCollectorsOperations()
            .listByAssessmentProject("ayagrawRG", "app18700project", com.azure.core.util.Context.NONE);
    }
}
```

### SqlAssessmentOptionsOperations_Get

```java
/**
 * Samples for SqlAssessmentOptionsOperations Get.
 */
public final class SqlAssessmentOptionsOperationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * SqlAssessmentOptionsOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: SqlAssessmentOptionsOperations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void sqlAssessmentOptionsOperationsGetMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.sqlAssessmentOptionsOperations()
            .getWithResponse("rgmigrate", "fci-test6904project", "default", com.azure.core.util.Context.NONE);
    }
}
```

### SqlAssessmentOptionsOperations_ListByAssessmentProject

```java
/**
 * Samples for SqlAssessmentOptionsOperations ListByAssessmentProject.
 */
public final class SqlAssessmentOptionsOperationsListByAssessmentProjectSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * SqlAssessmentOptionsOperations_ListByAssessmentProject_MaximumSet_Gen.json
     */
    /**
     * Sample code: SqlAssessmentOptionsOperations_ListByAssessmentProject_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void sqlAssessmentOptionsOperationsListByAssessmentProjectMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.sqlAssessmentOptionsOperations()
            .listByAssessmentProject("rgmigrate", "fci-test6904project", com.azure.core.util.Context.NONE);
    }
}
```

### SqlAssessmentV2Operations_Create

```java
import com.azure.resourcemanager.migration.assessment.models.AssessmentSizingCriterion;
import com.azure.resourcemanager.migration.assessment.models.AsyncCommitModeIntent;
import com.azure.resourcemanager.migration.assessment.models.AzureCurrency;
import com.azure.resourcemanager.migration.assessment.models.AzureLocation;
import com.azure.resourcemanager.migration.assessment.models.AzureOfferCode;
import com.azure.resourcemanager.migration.assessment.models.AzureReservedInstance;
import com.azure.resourcemanager.migration.assessment.models.AzureSqlDataBaseType;
import com.azure.resourcemanager.migration.assessment.models.AzureSqlInstanceType;
import com.azure.resourcemanager.migration.assessment.models.AzureSqlPurchaseModel;
import com.azure.resourcemanager.migration.assessment.models.AzureSqlServiceTier;
import com.azure.resourcemanager.migration.assessment.models.AzureVmFamily;
import com.azure.resourcemanager.migration.assessment.models.ComputeTier;
import com.azure.resourcemanager.migration.assessment.models.EntityUptime;
import com.azure.resourcemanager.migration.assessment.models.EnvironmentType;
import com.azure.resourcemanager.migration.assessment.models.MultiSubnetIntent;
import com.azure.resourcemanager.migration.assessment.models.OptimizationLogic;
import com.azure.resourcemanager.migration.assessment.models.OsLicense;
import com.azure.resourcemanager.migration.assessment.models.Percentile;
import com.azure.resourcemanager.migration.assessment.models.SqlAssessmentV2Properties;
import com.azure.resourcemanager.migration.assessment.models.SqlDbSettings;
import com.azure.resourcemanager.migration.assessment.models.SqlMiSettings;
import com.azure.resourcemanager.migration.assessment.models.SqlServerLicense;
import com.azure.resourcemanager.migration.assessment.models.SqlVmSettings;
import com.azure.resourcemanager.migration.assessment.models.TimeRange;
import java.util.Arrays;

/**
 * Samples for SqlAssessmentV2Operations Create.
 */
public final class SqlAssessmentV2OperationsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * SqlAssessmentV2Operations_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: SqlAssessmentV2Operations_Create_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void sqlAssessmentV2OperationsCreateMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.sqlAssessmentV2Operations()
            .define("test_swagger_1")
            .withExistingGroup("rgmigrate", "fci-test6904project", "test_fci_hadr")
            .withProperties(new SqlAssessmentV2Properties().withOsLicense(OsLicense.UNKNOWN)
                .withEnvironmentType(EnvironmentType.PRODUCTION)
                .withEntityUptime(new EntityUptime().withDaysPerMonth(30).withHoursPerDay(24))
                .withOptimizationLogic(OptimizationLogic.MINIMIZE_COST)
                .withReservedInstanceForVm(AzureReservedInstance.NONE)
                .withAzureOfferCodeForVm(AzureOfferCode.MSAZR0003P)
                .withAzureSqlManagedInstanceSettings(
                    new SqlMiSettings().withAzureSqlServiceTier(AzureSqlServiceTier.AUTOMATIC)
                        .withAzureSqlInstanceType(AzureSqlInstanceType.SINGLE_INSTANCE))
                .withAzureSqlDatabaseSettings(new SqlDbSettings().withAzureSqlServiceTier(AzureSqlServiceTier.AUTOMATIC)
                    .withAzureSqlDataBaseType(AzureSqlDataBaseType.SINGLE_DATABASE)
                    .withAzureSqlComputeTier(ComputeTier.AUTOMATIC)
                    .withAzureSqlPurchaseModel(AzureSqlPurchaseModel.VCORE))
                .withAzureSqlVmSettings(
                    new SqlVmSettings().withInstanceSeries(Arrays.asList(AzureVmFamily.EADSV5_SERIES)))
                .withMultiSubnetIntent(MultiSubnetIntent.DISASTER_RECOVERY)
                .withAsyncCommitModeIntent(AsyncCommitModeIntent.DISASTER_RECOVERY)
                .withDisasterRecoveryLocation(AzureLocation.EAST_ASIA)
                .withEnableHadrAssessment(true)
                .withReservedInstance(AzureReservedInstance.NONE)
                .withSqlServerLicense(SqlServerLicense.UNKNOWN)
                .withAzureLocation("SoutheastAsia")
                .withAzureOfferCode(AzureOfferCode.MSAZR0003P)
                .withCurrency(AzureCurrency.USD)
                .withScalingFactor(1.0F)
                .withPercentile(Percentile.PERCENTILE95)
                .withTimeRange(TimeRange.DAY)
                .withDiscountPercentage(0.0F)
                .withSizingCriterion(AssessmentSizingCriterion.PERFORMANCE_BASED))
            .create();
    }
}
```

### SqlAssessmentV2Operations_Delete

```java
/**
 * Samples for SqlAssessmentV2Operations Delete.
 */
public final class SqlAssessmentV2OperationsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * SqlAssessmentV2Operations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: SqlAssessmentV2Operations_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void sqlAssessmentV2OperationsDeleteMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.sqlAssessmentV2Operations()
            .deleteWithResponse("rgmigrate", "fci-test6904project", "test_fci_hadr", "test_swagger_1",
                com.azure.core.util.Context.NONE);
    }
}
```

### SqlAssessmentV2Operations_DownloadUrl

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import java.io.IOException;

/**
 * Samples for SqlAssessmentV2Operations DownloadUrl.
 */
public final class SqlAssessmentV2OperationsDownloadUrlSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * SqlAssessmentV2Operations_DownloadUrl_MaximumSet_Gen.json
     */
    /**
     * Sample code: SqlAssessmentV2Operations_DownloadUrl_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void sqlAssessmentV2OperationsDownloadUrlMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) throws IOException {
        manager.sqlAssessmentV2Operations()
            .downloadUrl("rgmigrate", "fci-test6904project", "test_fci_hadr", "test_swagger_1",
                SerializerFactory.createDefaultManagementSerializerAdapter()
                    .deserialize("{}", Object.class, SerializerEncoding.JSON),
                com.azure.core.util.Context.NONE);
    }
}
```

### SqlAssessmentV2Operations_Get

```java
/**
 * Samples for SqlAssessmentV2Operations Get.
 */
public final class SqlAssessmentV2OperationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * SqlAssessmentV2Operations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: SqlAssessmentV2Operations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void sqlAssessmentV2OperationsGetMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.sqlAssessmentV2Operations()
            .getWithResponse("rgmigrate", "fci-test6904project", "test_fci_hadr", "test_swagger_1",
                com.azure.core.util.Context.NONE);
    }
}
```

### SqlAssessmentV2Operations_ListByGroup

```java
/**
 * Samples for SqlAssessmentV2Operations ListByGroup.
 */
public final class SqlAssessmentV2OperationsListByGroupSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * SqlAssessmentV2Operations_ListByGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: SqlAssessmentV2Operations_ListByGroup_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void sqlAssessmentV2OperationsListByGroupMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.sqlAssessmentV2Operations()
            .listByGroup("rgmigrate", "fci-test6904project", "test_fci_hadr", com.azure.core.util.Context.NONE);
    }
}
```

### SqlAssessmentV2SummaryOperations_Get

```java
/**
 * Samples for SqlAssessmentV2SummaryOperations Get.
 */
public final class SqlAssessmentV2SummaryOperationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * SqlAssessmentV2SummaryOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: SqlAssessmentV2SummaryOperations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void sqlAssessmentV2SummaryOperationsGetMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.sqlAssessmentV2SummaryOperations()
            .getWithResponse("rgmigrate", "fci-test6904project", "test_fci_hadr", "test_swagger_1", "default",
                com.azure.core.util.Context.NONE);
    }
}
```

### SqlAssessmentV2SummaryOperations_ListBySqlAssessmentV2

```java
/**
 * Samples for SqlAssessmentV2SummaryOperations ListBySqlAssessmentV2.
 */
public final class SqlAssessmentV2SummaryOperationsListBySqlAssessmentV2Samples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * SqlAssessmentV2SummaryOperations_ListBySqlAssessmentV2_MaximumSet_Gen.json
     */
    /**
     * Sample code: SqlAssessmentV2SummaryOperations_ListBySqlAssessmentV2_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void sqlAssessmentV2SummaryOperationsListBySqlAssessmentV2MaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.sqlAssessmentV2SummaryOperations()
            .listBySqlAssessmentV2("rgmigrate", "fci-test6904project", "test_fci_hadr", "test_swagger_1",
                com.azure.core.util.Context.NONE);
    }
}
```

### SqlCollectorOperations_Create

```java
import com.azure.resourcemanager.migration.assessment.models.CollectorAgentPropertiesBase;
import com.azure.resourcemanager.migration.assessment.models.CollectorAgentSpnPropertiesBase;
import com.azure.resourcemanager.migration.assessment.models.CollectorPropertiesBaseWithAgent;

/**
 * Samples for SqlCollectorOperations Create.
 */
public final class SqlCollectorOperationsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * SqlCollectorOperations_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: SqlCollectorOperations_Create_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void sqlCollectorOperationsCreateMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.sqlCollectorOperations()
            .define("fci-test0c1esqlsitecollector")
            .withExistingAssessmentProject("rgmigrate", "fci-test6904project")
            .withProperties(new CollectorPropertiesBaseWithAgent()
                .withAgentProperties(
                    new CollectorAgentPropertiesBase().withId("630da710-4d44-41f7-a189-72fe3db5502b-agent")
                        .withSpnDetails(new CollectorAgentSpnPropertiesBase()
                            .withAuthority("https://login.windows.net/72f988bf-86f1-41af-91ab-2d7cd011db47")
                            .withApplicationId("db9c4c3d-477c-4d5a-817b-318276713565")
                            .withAudience("db9c4c3d-477c-4d5a-817b-318276713565")
                            .withObjectId("e50236ad-ad07-47d4-af71-ed7b52d200d5")
                            .withTenantId("72f988bf-86f1-41af-91ab-2d7cd011db47")))
                .withDiscoverySiteId(
                    "/subscriptions/4bd2aa0f-2bd2-4d67-91a8-5a4533d58600/resourceGroups/bansalankit-rg/providers/Microsoft.OffAzure/MasterSites/fci-ankit-test6065mastersite/SqlSites/fci-ankit-test6065sqlsites"))
            .create();
    }
}
```

### SqlCollectorOperations_Delete

```java
/**
 * Samples for SqlCollectorOperations Delete.
 */
public final class SqlCollectorOperationsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * SqlCollectorOperations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: SqlCollectorOperations_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void sqlCollectorOperationsDeleteMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.sqlCollectorOperations()
            .deleteWithResponse("rgmigrate", "fci-test6904project", "fci-test0c1esqlsitecollector",
                com.azure.core.util.Context.NONE);
    }
}
```

### SqlCollectorOperations_Get

```java
/**
 * Samples for SqlCollectorOperations Get.
 */
public final class SqlCollectorOperationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * SqlCollectorOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: SqlCollectorOperations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void sqlCollectorOperationsGetMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.sqlCollectorOperations()
            .getWithResponse("rgmigrate", "fci-test6904project", "fci-test0c1esqlsitecollector",
                com.azure.core.util.Context.NONE);
    }
}
```

### SqlCollectorOperations_ListByAssessmentProject

```java
/**
 * Samples for SqlCollectorOperations ListByAssessmentProject.
 */
public final class SqlCollectorOperationsListByAssessmentProjectSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * SqlCollectorOperations_ListByAssessmentProject_MaximumSet_Gen.json
     */
    /**
     * Sample code: SqlCollectorOperations_ListByAssessmentProject_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void sqlCollectorOperationsListByAssessmentProjectMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.sqlCollectorOperations()
            .listByAssessmentProject("rgmigrate", "fci-test6904project", com.azure.core.util.Context.NONE);
    }
}
```

### VmwareCollectorsOperations_Create

```java
import com.azure.resourcemanager.migration.assessment.models.CollectorAgentPropertiesBase;
import com.azure.resourcemanager.migration.assessment.models.CollectorAgentSpnPropertiesBase;
import com.azure.resourcemanager.migration.assessment.models.CollectorPropertiesBaseWithAgent;
import com.azure.resourcemanager.migration.assessment.models.ProvisioningState;
import java.time.OffsetDateTime;

/**
 * Samples for VmwareCollectorsOperations Create.
 */
public final class VmwareCollectorsOperationsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * VmwareCollectorsOperations_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: VmwareCollectorsOperations_Create_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void vmwareCollectorsOperationsCreateMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.vmwareCollectorsOperations()
            .define("Vmware2258collector")
            .withExistingAssessmentProject("ayagrawRG", "app18700project")
            .withProperties(new CollectorPropertiesBaseWithAgent().withProvisioningState(ProvisioningState.SUCCEEDED)
                .withAgentProperties(new CollectorAgentPropertiesBase().withId("fe243486-3318-41fa-aaba-c48b5df75308")
                    .withVersion("1.0.8.383")
                    .withLastHeartbeatUtc(OffsetDateTime.parse("2022-03-29T12:10:08.9167289Z"))
                    .withSpnDetails(new CollectorAgentSpnPropertiesBase()
                        .withAuthority("https://login.windows.net/72f988bf-86f1-41af-91ab-2d7cd011db47")
                        .withApplicationId("82b3e452-c0e8-4662-8347-58282925ae84")
                        .withAudience("82b3e452-c0e8-4662-8347-58282925ae84")
                        .withObjectId("3fc89111-1405-4938-9214-37aa4739401d")
                        .withTenantId("72f988bf-86f1-41af-91ab-2d7cd011db47")))
                .withDiscoverySiteId(
                    "/subscriptions/4bd2aa0f-2bd2-4d67-91a8-5a4533d58600/resourceGroups/ayagrawRG/providers/Microsoft.OffAzure/VMwareSites/Vmware2744site"))
            .create();
    }
}
```

### VmwareCollectorsOperations_Delete

```java
/**
 * Samples for VmwareCollectorsOperations Delete.
 */
public final class VmwareCollectorsOperationsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * VmwareCollectorsOperations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: VmwareCollectorsOperations_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void vmwareCollectorsOperationsDeleteMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.vmwareCollectorsOperations()
            .deleteWithResponse("ayagrawRG", "app18700project", "Vmware2258collector",
                com.azure.core.util.Context.NONE);
    }
}
```

### VmwareCollectorsOperations_Get

```java
/**
 * Samples for VmwareCollectorsOperations Get.
 */
public final class VmwareCollectorsOperationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * VmwareCollectorsOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: VmwareCollectorsOperations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void vmwareCollectorsOperationsGetMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.vmwareCollectorsOperations()
            .getWithResponse("ayagrawRG", "app18700project", "Vmware2258collector", com.azure.core.util.Context.NONE);
    }
}
```

### VmwareCollectorsOperations_ListByAssessmentProject

```java
/**
 * Samples for VmwareCollectorsOperations ListByAssessmentProject.
 */
public final class VmwareCollectorsOperationsListByAssessmentProjectSamples {
    /*
     * x-ms-original-file:
     * specification/migrate/resource-manager/Microsoft.Migrate/AssessmentProjects/stable/2023-03-15/examples/
     * VmwareCollectorsOperations_ListByAssessmentProject_MaximumSet_Gen.json
     */
    /**
     * Sample code: VmwareCollectorsOperations_ListByAssessmentProject_MaximumSet_Gen.
     * 
     * @param manager Entry point to MigrationAssessmentManager.
     */
    public static void vmwareCollectorsOperationsListByAssessmentProjectMaximumSetGen(
        com.azure.resourcemanager.migration.assessment.MigrationAssessmentManager manager) {
        manager.vmwareCollectorsOperations()
            .listByAssessmentProject("ayagrawRG", "app18700project", com.azure.core.util.Context.NONE);
    }
}
```

