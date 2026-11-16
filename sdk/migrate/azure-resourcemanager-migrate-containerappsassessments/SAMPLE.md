# Code snippets and samples


## AssessedWebApplicationOperations

- [Get](#assessedwebapplicationoperations_get)
- [ListByParent](#assessedwebapplicationoperations_listbyparent)

## ContainerAppsAssessmentOperations

- [Create](#containerappsassessmentoperations_create)
- [Delete](#containerappsassessmentoperations_delete)
- [DownloadUrl](#containerappsassessmentoperations_downloadurl)
- [Get](#containerappsassessmentoperations_get)
- [ListByParent](#containerappsassessmentoperations_listbyparent)

## ContainerAppsCostDetailOperations

- [ListByParent](#containerappscostdetailoperations_listbyparent)

## ContainerAppsManagedEnvironmentOperations

- [Get](#containerappsmanagedenvironmentoperations_get)
- [ListByParent](#containerappsmanagedenvironmentoperations_listbyparent)

## ContainerAppsOptionsOperations

- [Get](#containerappsoptionsoperations_get)
- [ListByParent](#containerappsoptionsoperations_listbyparent)

## ContainerAppsSummaryOperations

- [Get](#containerappssummaryoperations_get)
- [ListByParent](#containerappssummaryoperations_listbyparent)
### AssessedWebApplicationOperations_Get

```java
/**
 * Samples for AssessedWebApplicationOperations Get.
 */
public final class AssessedWebApplicationOperationsGetSamples {
    /*
     * x-ms-original-file: 2025-09-09-preview/AssessedWebApplicationOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessedWebApplicationOperations_Get_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void assessedWebApplicationOperationsGetMaximumSet(
        com.azure.resourcemanager.migrate.containerappsassessments.MigrateManager manager) {
        manager.assessedWebApplicationOperations()
            .getWithResponse("rgcontainerAppsAssessments", "testProject", "testcontainerAppsAssessment", "webapp",
                com.azure.core.util.Context.NONE);
    }
}
```

### AssessedWebApplicationOperations_ListByParent

```java
/**
 * Samples for AssessedWebApplicationOperations ListByParent.
 */
public final class AssessedWebApplicationOperationsListByParentSamples {
    /*
     * x-ms-original-file: 2025-09-09-preview/AssessedWebApplicationOperations_ListByParent_MaximumSet_Gen.json
     */
    /**
     * Sample code: AssessedWebApplicationOperations_ListByParent_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void assessedWebApplicationOperationsListByParentMaximumSet(
        com.azure.resourcemanager.migrate.containerappsassessments.MigrateManager manager) {
        manager.assessedWebApplicationOperations()
            .listByParent("rgcontainerAppsAssessments", "testProject", "testcontainerAppsAssessment",
                com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsAssessmentOperations_Create

```java
import com.azure.resourcemanager.migrate.containerappsassessments.models.AssessmentSizingCriterion;
import com.azure.resourcemanager.migrate.containerappsassessments.models.AzureCurrency;
import com.azure.resourcemanager.migrate.containerappsassessments.models.AzureOffer;
import com.azure.resourcemanager.migrate.containerappsassessments.models.BillingSettings;
import com.azure.resourcemanager.migrate.containerappsassessments.models.ContainerAppsAssessmentProperties;
import com.azure.resourcemanager.migrate.containerappsassessments.models.ContainerAppsAssessmentSettings;
import com.azure.resourcemanager.migrate.containerappsassessments.models.ContainerAppsWorkloadProfileSku;
import com.azure.resourcemanager.migrate.containerappsassessments.models.ContainerAppsWorkloadProfileType;
import com.azure.resourcemanager.migrate.containerappsassessments.models.EnvironmentType;
import com.azure.resourcemanager.migrate.containerappsassessments.models.LicensingProgram;
import com.azure.resourcemanager.migrate.containerappsassessments.models.Percentile;
import com.azure.resourcemanager.migrate.containerappsassessments.models.PerformanceData;
import com.azure.resourcemanager.migrate.containerappsassessments.models.SavingsOptions;
import com.azure.resourcemanager.migrate.containerappsassessments.models.SavingsSettings;
import com.azure.resourcemanager.migrate.containerappsassessments.models.Scope;
import com.azure.resourcemanager.migrate.containerappsassessments.models.ScopeType;
import com.azure.resourcemanager.migrate.containerappsassessments.models.TimeRange;
import java.time.OffsetDateTime;

/**
 * Samples for ContainerAppsAssessmentOperations Create.
 */
public final class ContainerAppsAssessmentOperationsCreateSamples {
    /*
     * x-ms-original-file: 2025-09-09-preview/ContainerAppsAssessmentOperations_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: ContainerAppsAssessmentOperations_Create_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void containerAppsAssessmentOperationsCreateMaximumSet(
        com.azure.resourcemanager.migrate.containerappsassessments.MigrateManager manager) {
        manager.containerAppsAssessmentOperations()
            .define("testcontainerAppsAssessment")
            .withExistingAssessmentProject("rgcontainerAppsAssessments", "testProject")
            .withProperties(new ContainerAppsAssessmentProperties()
                .withScope(new Scope().withScopeType(ScopeType.SERVER_GROUP_ID)
                    .withServerGroupId("this-will-be-a-valid-arm-id")
                    .withAzureResourceGraphQuery("zyhczkubwaqjqrmyithugmedmtdjxb"))
                .withSettings(new ContainerAppsAssessmentSettings().withAzureLocation("EastUS")
                    .withCurrency(AzureCurrency.USD)
                    .withScalingFactor(20.0D)
                    .withDiscountPercentage(14.0D)
                    .withSizingCriterion(AssessmentSizingCriterion.PERFORMANCE_BASED)
                    .withPerformanceData(new PerformanceData().withPercentile(Percentile.fromString("Unknown"))
                        .withTimeRange(TimeRange.DAY)
                        .withPerfDataStartTime(OffsetDateTime.parse("2025-02-14T07:09:09.242Z"))
                        .withPerfDataEndTime(OffsetDateTime.parse("2025-02-14T07:09:09.242Z")))
                    .withSavingsSettings(new SavingsSettings().withSavingsOptions(SavingsOptions.NONE)
                        .withAzureOfferCode(AzureOffer.UNKNOWN))
                    .withBillingSettings(new BillingSettings().withLicensingProgram(LicensingProgram.EA)
                        .withSubscriptionId("572C3D3F-425D-4AE8-A935-72631EADDE56"))
                    .withEnvironmentType(EnvironmentType.PRODUCTION)
                    .withSku(ContainerAppsWorkloadProfileSku.CONSUMPTION)
                    .withWorkloadProfileType(ContainerAppsWorkloadProfileType.CONSUMPTION)))
            .create();
    }
}
```

### ContainerAppsAssessmentOperations_Delete

```java
/**
 * Samples for ContainerAppsAssessmentOperations Delete.
 */
public final class ContainerAppsAssessmentOperationsDeleteSamples {
    /*
     * x-ms-original-file: 2025-09-09-preview/ContainerAppsAssessmentOperations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: ContainerAppsAssessmentOperations_Delete_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void containerAppsAssessmentOperationsDeleteMaximumSet(
        com.azure.resourcemanager.migrate.containerappsassessments.MigrateManager manager) {
        manager.containerAppsAssessmentOperations()
            .deleteWithResponse("rgcontainerAppsAssessments", "testProject", "testcontainerAppsAssessment",
                com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsAssessmentOperations_DownloadUrl

```java
/**
 * Samples for ContainerAppsAssessmentOperations DownloadUrl.
 */
public final class ContainerAppsAssessmentOperationsDownloadUrlSamples {
    /*
     * x-ms-original-file: 2025-09-09-preview/ContainerAppsAssessmentOperations_DownloadUrl_MaximumSet_Gen.json
     */
    /**
     * Sample code: ContainerAppsAssessmentOperations_DownloadUrl_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void containerAppsAssessmentOperationsDownloadUrlMaximumSet(
        com.azure.resourcemanager.migrate.containerappsassessments.MigrateManager manager) {
        manager.containerAppsAssessmentOperations()
            .downloadUrl("rgcontainerAppsAssessments", "testProject", "testcontainerAppsAssessment",
                com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsAssessmentOperations_Get

```java
/**
 * Samples for ContainerAppsAssessmentOperations Get.
 */
public final class ContainerAppsAssessmentOperationsGetSamples {
    /*
     * x-ms-original-file: 2025-09-09-preview/ContainerAppsAssessmentOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ContainerAppsAssessmentOperations_Get_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void containerAppsAssessmentOperationsGetMaximumSet(
        com.azure.resourcemanager.migrate.containerappsassessments.MigrateManager manager) {
        manager.containerAppsAssessmentOperations()
            .getWithResponse("rgcontainerAppsAssessments", "testProject", "testcontainerAppsAssessment",
                com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsAssessmentOperations_ListByParent

```java
/**
 * Samples for ContainerAppsAssessmentOperations ListByParent.
 */
public final class ContainerAppsAssessmentOperationsListByParentSamples {
    /*
     * x-ms-original-file: 2025-09-09-preview/ContainerAppsAssessmentOperations_ListByParent_MaximumSet_Gen.json
     */
    /**
     * Sample code: ContainerAppsAssessmentOperations_ListByParent_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void containerAppsAssessmentOperationsListByParentMaximumSet(
        com.azure.resourcemanager.migrate.containerappsassessments.MigrateManager manager) {
        manager.containerAppsAssessmentOperations()
            .listByParent("rgcontainerAppsAssessments", "testProject", com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsCostDetailOperations_ListByParent

```java
/**
 * Samples for ContainerAppsCostDetailOperations ListByParent.
 */
public final class ContainerAppsCostDetailOperationsListByParentSamples {
    /*
     * x-ms-original-file: 2025-09-09-preview/ContainerAppsCostDetailOperations_ListByParent_MaximumSet_Gen.json
     */
    /**
     * Sample code: ContainerAppsCostDetailOperations_ListByParent_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void containerAppsCostDetailOperationsListByParentMaximumSet(
        com.azure.resourcemanager.migrate.containerappsassessments.MigrateManager manager) {
        manager.containerAppsCostDetailOperations()
            .listByParent("rgcontainerAppsAssessments", "testProject", "testcontainerAppsAssessment",
                com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsManagedEnvironmentOperations_Get

```java
/**
 * Samples for ContainerAppsManagedEnvironmentOperations Get.
 */
public final class ContainerAppsManagedEnvironmentOperationsGetSamples {
    /*
     * x-ms-original-file: 2025-09-09-preview/ContainerAppsManagedEnvironmentOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ContainerAppsManagedEnvironmentOperations_Get_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void containerAppsManagedEnvironmentOperationsGetMaximumSet(
        com.azure.resourcemanager.migrate.containerappsassessments.MigrateManager manager) {
        manager.containerAppsManagedEnvironmentOperations()
            .getWithResponse("rgcontainerAppsAssessments", "testProject", "testcontainerAppsAssessment",
                "testcontainerAppsAssessment-env", com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsManagedEnvironmentOperations_ListByParent

```java
/**
 * Samples for ContainerAppsManagedEnvironmentOperations ListByParent.
 */
public final class ContainerAppsManagedEnvironmentOperationsListByParentSamples {
    /*
     * x-ms-original-file: 2025-09-09-preview/ContainerAppsManagedEnvironmentOperations_ListByParent_MaximumSet_Gen.json
     */
    /**
     * Sample code: ContainerAppsManagedEnvironmentOperations_ListByParent_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void containerAppsManagedEnvironmentOperationsListByParentMaximumSet(
        com.azure.resourcemanager.migrate.containerappsassessments.MigrateManager manager) {
        manager.containerAppsManagedEnvironmentOperations()
            .listByParent("rgcontainerAppsAssessments", "testProject", "testcontainerAppsAssessment",
                com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsOptionsOperations_Get

```java
/**
 * Samples for ContainerAppsOptionsOperations Get.
 */
public final class ContainerAppsOptionsOperationsGetSamples {
    /*
     * x-ms-original-file: 2025-09-09-preview/ContainerAppsOptionsOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ContainerAppsOptionsOperations_Get_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void containerAppsOptionsOperationsGetMaximumSet(
        com.azure.resourcemanager.migrate.containerappsassessments.MigrateManager manager) {
        manager.containerAppsOptionsOperations()
            .getWithResponse("rgcontainerAppsAssessments", "testProject", "default", com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsOptionsOperations_ListByParent

```java
/**
 * Samples for ContainerAppsOptionsOperations ListByParent.
 */
public final class ContainerAppsOptionsOperationsListByParentSamples {
    /*
     * x-ms-original-file: 2025-09-09-preview/ContainerAppsOptionsOperations_ListByParent_MaximumSet_Gen.json
     */
    /**
     * Sample code: ContainerAppsOptionsOperations_ListByParent_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void containerAppsOptionsOperationsListByParentMaximumSet(
        com.azure.resourcemanager.migrate.containerappsassessments.MigrateManager manager) {
        manager.containerAppsOptionsOperations()
            .listByParent("rgcontainerAppsAssessments", "testProject", com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsSummaryOperations_Get

```java
/**
 * Samples for ContainerAppsSummaryOperations Get.
 */
public final class ContainerAppsSummaryOperationsGetSamples {
    /*
     * x-ms-original-file: 2025-09-09-preview/ContainerAppsSummaryOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ContainerAppsSummaryOperations_Get_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void containerAppsSummaryOperationsGetMaximumSet(
        com.azure.resourcemanager.migrate.containerappsassessments.MigrateManager manager) {
        manager.containerAppsSummaryOperations()
            .getWithResponse("rgcontainerAppsAssessments", "testProject", "testcontainerAppsAssessment",
                "ContainerApps", com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsSummaryOperations_ListByParent

```java
/**
 * Samples for ContainerAppsSummaryOperations ListByParent.
 */
public final class ContainerAppsSummaryOperationsListByParentSamples {
    /*
     * x-ms-original-file: 2025-09-09-preview/ContainerAppsSummaryOperations_ListByParent_MaximumSet_Gen.json
     */
    /**
     * Sample code: ContainerAppsSummaryOperations_ListByParent_MaximumSet.
     * 
     * @param manager Entry point to MigrateManager.
     */
    public static void containerAppsSummaryOperationsListByParentMaximumSet(
        com.azure.resourcemanager.migrate.containerappsassessments.MigrateManager manager) {
        manager.containerAppsSummaryOperations()
            .listByParent("rgcontainerAppsAssessments", "testProject", "testcontainerAppsAssessment",
                com.azure.core.util.Context.NONE);
    }
}
```

