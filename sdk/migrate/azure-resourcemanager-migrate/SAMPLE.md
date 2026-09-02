# Code snippets and samples


## MigrateProjects

- [CreateWavesFromPlan](#migrateprojects_createwavesfromplan)
- [FetchSasUri](#migrateprojects_fetchsasuri)
- [GenerateWavePlan](#migrateprojects_generatewaveplan)
- [GetWavePlans](#migrateprojects_getwaveplans)
- [ImportWavePlan](#migrateprojects_importwaveplan)
- [RefreshEntities](#migrateprojects_refreshentities)

## MigrationEntities

- [Create](#migrationentities_create)
- [Delete](#migrationentities_delete)
- [Get](#migrationentities_get)
- [ListByParent](#migrationentities_listbyparent)

## MigrationEntityGroups

- [Create](#migrationentitygroups_create)
- [Delete](#migrationentitygroups_delete)
- [Get](#migrationentitygroups_get)
- [ListByParent](#migrationentitygroups_listbyparent)

## Operations

- [List](#operations_list)

## Tasks

- [Create](#tasks_create)
- [Delete](#tasks_delete)
- [Get](#tasks_get)
- [GetSummary](#tasks_getsummary)
- [ListByParent](#tasks_listbyparent)

## Waves

- [Create](#waves_create)
- [Delete](#waves_delete)
- [Get](#waves_get)
- [ListByParent](#waves_listbyparent)
- [TriggerRefresh](#waves_triggerrefresh)
### MigrateProjects_CreateWavesFromPlan

```java
import com.azure.resourcemanager.migrate.models.CreateWavesFromPlanRequest;
import com.azure.resourcemanager.migrate.models.WaveSelectionItem;
import java.util.Arrays;

/**
 * Samples for MigrateProjects CreateWavesFromPlan.
 */
public final class MigrateProjectsCreateWavesFromPlanSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/MigrateProjects_CreateWavesFromPlan_MaximumSet_Gen.json
     */
    /**
     * Sample code: MigrateProjects_CreateWavesFromPlan_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void
        migrateProjectsCreateWavesFromPlanMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.migrateProjects()
            .createWavesFromPlan("rgwaves", "myProjectName",
                new CreateWavesFromPlanRequest().withAssessmentArmId("xcuuprxy")
                    .withMigrationPath("timhgrpcfekqm")
                    .withWaveSelection(Arrays.asList(
                        new WaveSelectionItem().withWaveName("il").withWaveDisplayName("xbcibpajnwxwqawxuhmq"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### MigrateProjects_FetchSasUri

```java
import com.azure.resourcemanager.migrate.models.FetchSasUriRequest;

/**
 * Samples for MigrateProjects FetchSasUri.
 */
public final class MigrateProjectsFetchSasUriSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/MigrateProjects_FetchSasUri_MaximumSet_Gen.json
     */
    /**
     * Sample code: MigrateProjects_FetchSasUri_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void migrateProjectsFetchSasUriMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.migrateProjects()
            .fetchSasUriWithResponse("rgwaves", "myProjectName",
                new FetchSasUriRequest().withAssessmentArmId("awcgjhekflopkk")
                    .withMigrationPath("jybxmlxnnkpywxdqnijfpeso")
                    .withSasVersionId("bjkxgpuhgsgqkayuaw"),
                com.azure.core.util.Context.NONE);
    }
}
```

### MigrateProjects_GenerateWavePlan

```java
import com.azure.resourcemanager.migrate.models.GenerateWavePlanRequest;

/**
 * Samples for MigrateProjects GenerateWavePlan.
 */
public final class MigrateProjectsGenerateWavePlanSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/MigrateProjects_GenerateWavePlan_MaximumSet_Gen.json
     */
    /**
     * Sample code: MigrateProjects_GenerateWavePlan_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void
        migrateProjectsGenerateWavePlanMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.migrateProjects()
            .generateWavePlan("rgwaves", "project1",
                new GenerateWavePlanRequest().withAssessmentArmId("rwwoftf").withMigrationPath("waehojas"),
                com.azure.core.util.Context.NONE);
    }
}
```

### MigrateProjects_GetWavePlans

```java
import com.azure.resourcemanager.migrate.models.GetWavePlansRequest;

/**
 * Samples for MigrateProjects GetWavePlans.
 */
public final class MigrateProjectsGetWavePlansSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/MigrateProjects_GetWavePlans_MaximumSet_Gen.json
     */
    /**
     * Sample code: MigrateProjects_GetWavePlans_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void migrateProjectsGetWavePlansMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.migrateProjects()
            .getWavePlansWithResponse("rgwaves", "myProjectName",
                new GetWavePlansRequest().withAssessmentArmId("vcpel").withMigrationPath("tckqdpgx"),
                com.azure.core.util.Context.NONE);
    }
}
```

### MigrateProjects_ImportWavePlan

```java
import com.azure.resourcemanager.migrate.models.ImportWavePlanRequest;

/**
 * Samples for MigrateProjects ImportWavePlan.
 */
public final class MigrateProjectsImportWavePlanSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/MigrateProjects_ImportWavePlan_MaximumSet_Gen.json
     */
    /**
     * Sample code: MigrateProjects_ImportWavePlan_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void
        migrateProjectsImportWavePlanMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.migrateProjects()
            .importWavePlan("rgwaves", "myProjectName",
                new ImportWavePlanRequest().withAssessmentArmId("fhdwyibrc")
                    .withMigrationPath("aywdxwnkl")
                    .withSasVersionId("pcjubmrrvrhahgyuztztftzwfjbg"),
                com.azure.core.util.Context.NONE);
    }
}
```

### MigrateProjects_RefreshEntities

```java
import com.azure.resourcemanager.migrate.models.RefreshEntitiesRequest;
import java.util.Arrays;

/**
 * Samples for MigrateProjects RefreshEntities.
 */
public final class MigrateProjectsRefreshEntitiesSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/MigrateProjects_RefreshEntities_MaximumSet_Gen.json
     */
    /**
     * Sample code: MigrateProjects_RefreshEntities_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void
        migrateProjectsRefreshEntitiesMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.migrateProjects()
            .refreshEntities("rgwaves", "project1", new RefreshEntitiesRequest().withMigrationEntityIds(Arrays.asList(
                "/subscriptions/11111111-2222-3333-4444-555555555555/resourceGroups/MyResourceGroup/providers/Microsoft.Migrate/migrateProjects/MyMigrateProject/migrationEntities/entity1"))
                .withMigrationEntityGroupIds(Arrays.asList(
                    "/subscriptions/11111111-2222-3333-4444-555555555555/resourceGroups/MyResourceGroup/providers/Microsoft.Migrate/migrateProjects/MyMigrateProject/migrationEntityGroups/group1")),
                com.azure.core.util.Context.NONE);
    }
}
```

### MigrationEntities_Create

```java
import com.azure.resourcemanager.migrate.models.MigrationEntityProperties;
import com.azure.resourcemanager.migrate.models.MigrationSpecificPropertiesBase;
import java.util.Arrays;

/**
 * Samples for MigrationEntities Create.
 */
public final class MigrationEntitiesCreateSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/MigrationEntities_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: MigrationEntities_Create_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void migrationEntitiesCreateMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.migrationEntities()
            .define("entity1")
            .withExistingMigrateProject("rgwaves", "project1")
            .withProperties(new MigrationEntityProperties().withPartnerResourceArmId("a")
                .withTargetAzureResourceArmId("veaa")
                .withAssociatedInventoryResourceId("z")
                .withInventoryDisplayName("aje")
                .withAssociatedAssessmentId(
                    "/subscriptions/11111111-2222-3333-4444-555555555555/resourceGroups/MyResourceGroup/providers/Microsoft.Migrate/assessmentprojects/myAssessmentProject/assessments/myAssessment")
                .withAssociatedWaveId(
                    "/subscriptions/11111111-2222-3333-4444-555555555555/resourceGroups/MyResourceGroup/providers/Microsoft.Migrate/migrateProjects/MyMigrateProject/waves/wave1")
                .withAssociatedMigrationEntityGroupIds(Arrays.asList(
                    "/subscriptions/11111111-2222-3333-4444-555555555555/resourceGroups/MyResourceGroup/providers/Microsoft.Migrate/migrateProjects/MyMigrateProject/migrationEntityGroups/group1"))
                .withTarget("anenonptqbrzszgdlfypqltgifinq")
                .withMigrationSpecificProperties(new MigrationSpecificPropertiesBase())
                .withMigrationTool("qqintxdthhddwkdhygom")
                .withMigrationPath("qyurpnfpqtukcrnfihrmqf")
                .withAssessedEntityArmId("k"))
            .create();
    }
}
```

### MigrationEntities_Delete

```java
/**
 * Samples for MigrationEntities Delete.
 */
public final class MigrationEntitiesDeleteSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/MigrationEntities_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: MigrationEntities_Delete_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void migrationEntitiesDeleteMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.migrationEntities().delete("rgwaves", "project1", "entity1", com.azure.core.util.Context.NONE);
    }
}
```

### MigrationEntities_Get

```java
/**
 * Samples for MigrationEntities Get.
 */
public final class MigrationEntitiesGetSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/MigrationEntities_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: MigrationEntities_Get_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void migrationEntitiesGetMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.migrationEntities().getWithResponse("rgwaves", "project1", "entity1", com.azure.core.util.Context.NONE);
    }
}
```

### MigrationEntities_ListByParent

```java
/**
 * Samples for MigrationEntities ListByParent.
 */
public final class MigrationEntitiesListByParentSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/MigrationEntities_ListByParent_MaximumSet_Gen.json
     */
    /**
     * Sample code: MigrationEntities_ListByParent_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void
        migrationEntitiesListByParentMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.migrationEntities().listByParent("rgwaves", "project1", com.azure.core.util.Context.NONE);
    }
}
```

### MigrationEntityGroups_Create

```java
import com.azure.resourcemanager.migrate.models.MigrationEntityGroupProperties;
import java.util.Arrays;

/**
 * Samples for MigrationEntityGroups Create.
 */
public final class MigrationEntityGroupsCreateSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/MigrationEntityGroups_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: MigrationEntityGroups_Create_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void migrationEntityGroupsCreateMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.migrationEntityGroups()
            .define("group1")
            .withExistingMigrateProject("rgwaves", "project1")
            .withProperties(new MigrationEntityGroupProperties().withApplicationId("xjovxgurinimcoikyvov")
                .withApplicationDisplayName("mwp")
                .withAssociatedAssessmentId(
                    "/subscriptions/11111111-2222-3333-4444-555555555555/resourceGroups/MyResourceGroup/providers/Microsoft.Migrate/assessmentprojects/myAssessmentProject/assessments/myAssessment")
                .withAssociatedWaveIds(Arrays.asList(
                    "/subscriptions/11111111-2222-3333-4444-555555555555/resourceGroups/MyResourceGroup/providers/Microsoft.Migrate/migrateProjects/MyMigrateProject/waves/wave1"))
                .withMigrationPath("yq"))
            .create();
    }
}
```

### MigrationEntityGroups_Delete

```java
/**
 * Samples for MigrationEntityGroups Delete.
 */
public final class MigrationEntityGroupsDeleteSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/MigrationEntityGroups_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: MigrationEntityGroups_Delete_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void migrationEntityGroupsDeleteMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.migrationEntityGroups().delete("rgwaves", "project1", "group1", com.azure.core.util.Context.NONE);
    }
}
```

### MigrationEntityGroups_Get

```java
/**
 * Samples for MigrationEntityGroups Get.
 */
public final class MigrationEntityGroupsGetSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/MigrationEntityGroups_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: MigrationEntityGroups_Get_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void migrationEntityGroupsGetMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.migrationEntityGroups()
            .getWithResponse("rgwaves", "project1", "group1", com.azure.core.util.Context.NONE);
    }
}
```

### MigrationEntityGroups_ListByParent

```java
/**
 * Samples for MigrationEntityGroups ListByParent.
 */
public final class MigrationEntityGroupsListByParentSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/MigrationEntityGroups_ListByParent_MaximumSet_Gen.json
     */
    /**
     * Sample code: MigrationEntityGroups_ListByParent_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void
        migrationEntityGroupsListByParentMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.migrationEntityGroups().listByParent("rgwaves", "project1", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-06-01-preview/Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MinimumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void operationsListMinimumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-06-01-preview/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void operationsListMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Tasks_Create

```java
import com.azure.resourcemanager.migrate.models.TaskProperties;
import com.azure.resourcemanager.migrate.models.TaskScope;

/**
 * Samples for Tasks Create.
 */
public final class TasksCreateSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/Tasks_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: Tasks_Create_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void tasksCreateMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.tasks()
            .define("task1")
            .withExistingMigrateProject("rgwaves", "project1")
            .withProperties(new TaskProperties().withScopeId(
                "/subscriptions/11111111-2222-3333-4444-555555555555/resourceGroups/MyResourceGroup/providers/Microsoft.Migrate/migrateProjects/MyMigrateProject/waves/wave1")
                .withStage("suwwllyupuonhzwrsl")
                .withDisplayName("rbxtqbeapvifcdgmlqmgsibudjd")
                .withStatus("txxrst")
                .withScope(TaskScope.WAVE)
                .withDescription("tomulgdavwoaev"))
            .create();
    }
}
```

### Tasks_Delete

```java
/**
 * Samples for Tasks Delete.
 */
public final class TasksDeleteSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/Tasks_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Tasks_Delete_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void tasksDeleteMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.tasks().delete("rgwaves", "project1", "task1", com.azure.core.util.Context.NONE);
    }
}
```

### Tasks_Get

```java
/**
 * Samples for Tasks Get.
 */
public final class TasksGetSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/Tasks_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Tasks_Get_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void tasksGetMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.tasks().getWithResponse("rgwaves", "project1", "task1", com.azure.core.util.Context.NONE);
    }
}
```

### Tasks_GetSummary

```java
import com.azure.resourcemanager.migrate.models.TaskSummaryRequest;

/**
 * Samples for Tasks GetSummary.
 */
public final class TasksGetSummarySamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/Tasks_GetSummary_MaximumSet_Gen.json
     */
    /**
     * Sample code: Tasks_GetSummary_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void tasksGetSummaryMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.tasks()
            .getSummaryWithResponse("rgwaves", "project1", new TaskSummaryRequest().withScopeId(
                "/subscriptions/11111111-2222-3333-4444-555555555555/resourceGroups/MyResourceGroup/providers/Microsoft.Migrate/migrateProjects/MyMigrateProject/waves/wave1"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Tasks_ListByParent

```java
/**
 * Samples for Tasks ListByParent.
 */
public final class TasksListByParentSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/Tasks_ListByParent_MaximumSet_Gen.json
     */
    /**
     * Sample code: Tasks_ListByParent_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void tasksListByParentMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.tasks().listByParent("rgwaves", "project1", com.azure.core.util.Context.NONE);
    }
}
```

### Waves_Create

```java
import com.azure.resourcemanager.migrate.fluent.models.WavePropertiesInner;
import com.azure.resourcemanager.migrate.models.Arg;
import java.time.OffsetDateTime;

/**
 * Samples for Waves Create.
 */
public final class WavesCreateSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/Waves_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: Waves_Create_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void wavesCreateMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.waves()
            .define("wave1")
            .withExistingMigrateProject("rgwaves", "project1")
            .withProperties(new WavePropertiesInner().withDescription("xajbtsvcadsmuttrdphivryx")
                .withDisplayName("gbrjctlozlwfftuzxov")
                .withArg(new Arg().withQuery("wivfwbmo"))
                .withPlannedStartDate(OffsetDateTime.parse("2026-02-12T12:54:26.848Z"))
                .withPlannedCompletionDate(OffsetDateTime.parse("2026-02-12T12:54:26.848Z")))
            .create();
    }
}
```

### Waves_Delete

```java
/**
 * Samples for Waves Delete.
 */
public final class WavesDeleteSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/Waves_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Waves_Delete_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void wavesDeleteMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.waves().delete("rgwaves", "project1", "wave1", com.azure.core.util.Context.NONE);
    }
}
```

### Waves_Get

```java
/**
 * Samples for Waves Get.
 */
public final class WavesGetSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/Waves_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Waves_Get_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void wavesGetMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.waves().getWithResponse("rgwaves", "project1", "wave1", com.azure.core.util.Context.NONE);
    }
}
```

### Waves_ListByParent

```java
/**
 * Samples for Waves ListByParent.
 */
public final class WavesListByParentSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/Waves_ListByParent_MaximumSet_Gen.json
     */
    /**
     * Sample code: Waves_ListByParent_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void wavesListByParentMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.waves().listByParent("rgwaves", "project1", com.azure.core.util.Context.NONE);
    }
}
```

### Waves_TriggerRefresh

```java
/**
 * Samples for Waves TriggerRefresh.
 */
public final class WavesTriggerRefreshSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/Waves_Refresh_MaximumSet_Gen.json
     */
    /**
     * Sample code: Waves_Refresh_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void wavesRefreshMaximumSet(com.azure.resourcemanager.migrate.MigrateManager manager) {
        manager.waves().triggerRefreshWithResponse("rgwaves", "project1", "wave1", com.azure.core.util.Context.NONE);
    }
}
```

