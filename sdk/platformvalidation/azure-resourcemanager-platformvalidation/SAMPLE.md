# Code snippets and samples


## CloudValidations

- [CreateOrUpdate](#cloudvalidations_createorupdate)
- [Delete](#cloudvalidations_delete)
- [GetByResourceGroup](#cloudvalidations_getbyresourcegroup)
- [List](#cloudvalidations_list)
- [ListByResourceGroup](#cloudvalidations_listbyresourcegroup)
- [Update](#cloudvalidations_update)

## ExecutionPlanRuns

- [CreateOrUpdate](#executionplanruns_createorupdate)
- [Delete](#executionplanruns_delete)
- [Get](#executionplanruns_get)
- [ListByExecutionPlan](#executionplanruns_listbyexecutionplan)

## OperationStatus

- [Get](#operationstatus_get)

## Operations

- [List](#operations_list)

## ValidationExecutionPlans

- [CreateOrUpdate](#validationexecutionplans_createorupdate)
- [Delete](#validationexecutionplans_delete)
- [Get](#validationexecutionplans_get)
- [ListByResourceGroup](#validationexecutionplans_listbyresourcegroup)
- [Update](#validationexecutionplans_update)

## ValidationTestCategories

- [Get](#validationtestcategories_get)
- [List](#validationtestcategories_list)

## ValidationTestRuns

- [Get](#validationtestruns_get)
- [ListByExecutionPlanRun](#validationtestruns_listbyexecutionplanrun)

## ValidationTestVersions

- [Get](#validationtestversions_get)
- [List](#validationtestversions_list)

## ValidationTests

- [Get](#validationtests_get)
- [List](#validationtests_list)
### CloudValidations_CreateOrUpdate

```java
import com.azure.resourcemanager.platformvalidation.models.CloudValidationProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for CloudValidations CreateOrUpdate.
 */
public final class CloudValidationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-08-01-preview/CloudValidations_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: CloudValidations_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void cloudValidationsCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.cloudValidations()
            .define("cvtest01")
            .withRegion("southcentralus")
            .withExistingResourceGroup("rgvalidate")
            .withTags(mapOf("environment", "production"))
            .withProperties(new CloudValidationProperties().withDescription(
                "Cloud validation that groups platform validation execution plans for the target subscription."))
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

### CloudValidations_Delete

```java
/**
 * Samples for CloudValidations Delete.
 */
public final class CloudValidationsDeleteSamples {
    /*
     * x-ms-original-file: 2026-08-01-preview/CloudValidations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: CloudValidations_Delete_MaximumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void cloudValidationsDeleteMaximumSet(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.cloudValidations().delete("rgvalidate", "cvtest01", com.azure.core.util.Context.NONE);
    }
}
```

### CloudValidations_GetByResourceGroup

```java
/**
 * Samples for CloudValidations GetByResourceGroup.
 */
public final class CloudValidationsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-08-01-preview/CloudValidations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: CloudValidations_Get_MaximumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void
        cloudValidationsGetMaximumSet(com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.cloudValidations()
            .getByResourceGroupWithResponse("rgvalidate", "cvtest01", com.azure.core.util.Context.NONE);
    }
}
```

### CloudValidations_List

```java
/**
 * Samples for CloudValidations List.
 */
public final class CloudValidationsListSamples {
    /*
     * x-ms-original-file: 2026-08-01-preview/CloudValidations_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: CloudValidations_ListBySubscription_MinimumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void cloudValidationsListBySubscriptionMinimumSet(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.cloudValidations().list(null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-08-01-preview/CloudValidations_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: CloudValidations_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void cloudValidationsListBySubscriptionMaximumSet(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.cloudValidations().list(null, com.azure.core.util.Context.NONE);
    }
}
```

### CloudValidations_ListByResourceGroup

```java
/**
 * Samples for CloudValidations ListByResourceGroup.
 */
public final class CloudValidationsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-08-01-preview/CloudValidations_ListByResourceGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: CloudValidations_ListByResourceGroup_MinimumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void cloudValidationsListByResourceGroupMinimumSet(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.cloudValidations().listByResourceGroup("rgplatformvalidation", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-08-01-preview/CloudValidations_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: CloudValidations_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void cloudValidationsListByResourceGroupMaximumSet(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.cloudValidations().listByResourceGroup("rgvalidate", null, com.azure.core.util.Context.NONE);
    }
}
```

### CloudValidations_Update

```java
import com.azure.resourcemanager.platformvalidation.models.CloudValidation;
import com.azure.resourcemanager.platformvalidation.models.CloudValidationUpdateProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for CloudValidations Update.
 */
public final class CloudValidationsUpdateSamples {
    /*
     * x-ms-original-file: 2026-08-01-preview/CloudValidations_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: CloudValidations_Update_MaximumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void cloudValidationsUpdateMaximumSet(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        CloudValidation resource = manager.cloudValidations()
            .getByResourceGroupWithResponse("rgvalidate", "cvtest01", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("cost-center", "platform-validation"))
            .withProperties(new CloudValidationUpdateProperties().withDescription(
                "Cloud validation that groups platform validation execution plans for the target subscription."))
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

### ExecutionPlanRuns_CreateOrUpdate

```java
import com.azure.resourcemanager.platformvalidation.models.ExecutionPlanRunProperties;

/**
 * Samples for ExecutionPlanRuns CreateOrUpdate.
 */
public final class ExecutionPlanRunsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-08-01-preview/ExecutionPlanRuns_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: ExecutionPlanRuns_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void executionPlanRunsCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.executionPlanRuns()
            .define("run-001")
            .withExistingValidationExecutionPlan("rgvalidate", "cvtest01", "contoso-linux-cert")
            .withProperties(
                new ExecutionPlanRunProperties().withDescription("Run the Contoso Linux image certification plan."))
            .create();
    }
}
```

### ExecutionPlanRuns_Delete

```java
/**
 * Samples for ExecutionPlanRuns Delete.
 */
public final class ExecutionPlanRunsDeleteSamples {
    /*
     * x-ms-original-file: 2026-08-01-preview/ExecutionPlanRuns_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: ExecutionPlanRuns_Delete_MaximumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void executionPlanRunsDeleteMaximumSet(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.executionPlanRuns()
            .delete("rgvalidate", "cvtest01", "veptest01", "veprun01", com.azure.core.util.Context.NONE);
    }
}
```

### ExecutionPlanRuns_Get

```java
/**
 * Samples for ExecutionPlanRuns Get.
 */
public final class ExecutionPlanRunsGetSamples {
    /*
     * x-ms-original-file: 2026-08-01-preview/ExecutionPlanRuns_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ExecutionPlanRuns_Get_MaximumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void
        executionPlanRunsGetMaximumSet(com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.executionPlanRuns()
            .getWithResponse("rgvalidate", "cvtest01", "contoso-linux-cert", "run-001",
                com.azure.core.util.Context.NONE);
    }
}
```

### ExecutionPlanRuns_ListByExecutionPlan

```java
/**
 * Samples for ExecutionPlanRuns ListByExecutionPlan.
 */
public final class ExecutionPlanRunsListByExecutionPlanSamples {
    /*
     * x-ms-original-file: 2026-08-01-preview/ExecutionPlanRuns_ListByExecutionPlan_MaximumSet_Gen.json
     */
    /**
     * Sample code: ExecutionPlanRuns_ListByExecutionPlan_MaximumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void executionPlanRunsListByExecutionPlanMaximumSet(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.executionPlanRuns()
            .listByExecutionPlan("rgvalidate", "cvtest01", "contoso-linux-cert", null,
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-08-01-preview/OperationStatus_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: OperationStatus_Get_MaximumSet - generated by [MinimumSet] rule.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void operationStatusGetMaximumSetGeneratedByMinimumSetRule(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.operationStatus()
            .getWithResponse("southcentralus", "11111111-1111-4111-8111-111111111111",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-08-01-preview/OperationStatus_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: OperationStatus_Get_MaximumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void
        operationStatusGetMaximumSet(com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.operationStatus()
            .getWithResponse("southcentralus", "11111111-1111-4111-8111-111111111111",
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
     * x-ms-original-file: 2026-08-01-preview/Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MinimumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void
        operationsListMinimumSet(com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-08-01-preview/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MaximumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void
        operationsListMaximumSet(com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ValidationExecutionPlans_CreateOrUpdate

```java
import com.azure.resourcemanager.platformvalidation.models.ValidationExecutionPlanProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ValidationExecutionPlans CreateOrUpdate.
 */
public final class ValidationExecutionPlansCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-08-01-preview/ValidationExecutionPlans_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: ValidationExecutionPlans_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void validationExecutionPlansCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.validationExecutionPlans()
            .define("contoso-linux-cert")
            .withRegion("southcentralus")
            .withExistingCloudValidation("rgvalidate", "cvtest01")
            .withTags(mapOf("owner-team", "azure-platform-validation"))
            .withProperties(new ValidationExecutionPlanProperties()
                .withDescription(
                    "Runs all public Linux-compatible AzCertify catalog tests against the Contoso Linux image.")
                .withPlanConfigurationJson(
                    "{\"apiVersion\":\"microsoft.PlatformValidation/validationExecutionPlan.v0\",\"kind\":\"ValidationExecutionPlan\",\"metadata\":{\"name\":\"contoso-linux-cert\"},\"parameters\":{\"certificationPackageReference\":{\"osType\":\"Linux\",\"vmGenerationType\":\"V1\",\"architectureType\":\"X64\",\"recommendedVMSizes\":[\"Standard_D4s_v3\"],\"storageProfile\":{\"osDiskImage\":{\"sourceVhdUri\":\"https://contoso.blob.core.windows.net/vhds/img.vhd?<sas>\"},\"dataDiskImages\":[]},\"additionalProperties\":{}}},\"authoring\":{\"steps\":[{\"name\":\"os-disk-size\",\"type\":\"test\",\"testRef\":\"/providers/Microsoft.PlatformValidation/validationTests/os-disk-size/versions/1.0.0\"},{\"name\":\"data-disk-size\",\"type\":\"test\",\"testRef\":\"/providers/Microsoft.PlatformValidation/validationTests/data-disk-size/versions/1.0.0\"},{\"name\":\"malware-defender\",\"type\":\"test\",\"testRef\":\"/providers/Microsoft.PlatformValidation/validationTests/malware-defender/versions/1.0.0\"},{\"name\":\"malware-esrp\",\"type\":\"test\",\"testRef\":\"/providers/Microsoft.PlatformValidation/validationTests/malware-esrp/versions/1.0.0\"},{\"name\":\"linux-quality-validation\",\"type\":\"test\",\"testRef\":\"/providers/Microsoft.PlatformValidation/validationTests/linux-quality-validation/versions/1.0.0\",\"inputs\":{\"concurrency\":1,\"testSuite\":[{\"testNames\":[\"smoke_test\",\"validate_netvsc_reload\"]}]}}]}}"))
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

### ValidationExecutionPlans_Delete

```java
/**
 * Samples for ValidationExecutionPlans Delete.
 */
public final class ValidationExecutionPlansDeleteSamples {
    /*
     * x-ms-original-file: 2026-08-01-preview/ValidationExecutionPlans_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: ValidationExecutionPlans_Delete_MaximumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void validationExecutionPlansDeleteMaximumSet(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.validationExecutionPlans()
            .delete("rgvalidate", "cvtest01", "veptest01", com.azure.core.util.Context.NONE);
    }
}
```

### ValidationExecutionPlans_Get

```java
/**
 * Samples for ValidationExecutionPlans Get.
 */
public final class ValidationExecutionPlansGetSamples {
    /*
     * x-ms-original-file: 2026-08-01-preview/ValidationExecutionPlans_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ValidationExecutionPlans_Get_MaximumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void validationExecutionPlansGetMaximumSet(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.validationExecutionPlans()
            .getWithResponse("rgvalidate", "cvtest01", "contoso-linux-cert", com.azure.core.util.Context.NONE);
    }
}
```

### ValidationExecutionPlans_ListByResourceGroup

```java
/**
 * Samples for ValidationExecutionPlans ListByResourceGroup.
 */
public final class ValidationExecutionPlansListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-08-01-preview/ValidationExecutionPlans_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: ValidationExecutionPlans_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void validationExecutionPlansListByResourceGroupMaximumSet(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.validationExecutionPlans()
            .listByResourceGroup("rgvalidate", "cvtest01", null, com.azure.core.util.Context.NONE);
    }
}
```

### ValidationExecutionPlans_Update

```java
import com.azure.resourcemanager.platformvalidation.models.ValidationExecutionPlan;
import com.azure.resourcemanager.platformvalidation.models.ValidationExecutionPlanUpdateProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ValidationExecutionPlans Update.
 */
public final class ValidationExecutionPlansUpdateSamples {
    /*
     * x-ms-original-file: 2026-08-01-preview/ValidationExecutionPlans_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: ValidationExecutionPlans_Update_MaximumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void validationExecutionPlansUpdateMaximumSet(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        ValidationExecutionPlan resource = manager.validationExecutionPlans()
            .getWithResponse("rgvalidate", "cvtest01", "veptest01", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("environment", "production"))
            .withProperties(new ValidationExecutionPlanUpdateProperties()
                .withDescription(
                    "Validation execution plan that runs the network connectivity checks against the target image.")
                .withPlanConfigurationJson(
                    "{\"apiVersion\":\"microsoft.PlatformValidation/validationExecutionPlan.v0\",\"kind\":\"ValidationExecutionPlan\",\"metadata\":{\"name\":\"contoso-linux-cert\"},\"parameters\":{\"certificationPackageReference\":{\"osType\":\"Linux\",\"vmGenerationType\":\"V1\",\"architectureType\":\"X64\",\"recommendedVMSizes\":[\"Standard_D4s_v3\"],\"storageProfile\":{\"osDiskImage\":{\"sourceVhdUri\":\"https://contoso.blob.core.windows.net/vhds/img.vhd?<sas>\"},\"dataDiskImages\":[]},\"additionalProperties\":{}}},\"authoring\":{\"steps\":[{\"name\":\"os-disk-size\",\"type\":\"test\",\"testRef\":\"/providers/Microsoft.PlatformValidation/validationTests/os-disk-size/versions/1.0.0\"},{\"name\":\"data-disk-size\",\"type\":\"test\",\"testRef\":\"/providers/Microsoft.PlatformValidation/validationTests/data-disk-size/versions/1.0.0\"},{\"name\":\"malware-defender\",\"type\":\"test\",\"testRef\":\"/providers/Microsoft.PlatformValidation/validationTests/malware-defender/versions/1.0.0\"},{\"name\":\"malware-esrp\",\"type\":\"test\",\"testRef\":\"/providers/Microsoft.PlatformValidation/validationTests/malware-esrp/versions/1.0.0\"},{\"name\":\"linux-quality-validation\",\"type\":\"test\",\"testRef\":\"/providers/Microsoft.PlatformValidation/validationTests/linux-quality-validation/versions/1.0.0\",\"inputs\":{\"concurrency\":1,\"testSuite\":[{\"testNames\":[\"smoke_test\",\"validate_netvsc_reload\"]}]}}]}}"))
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

### ValidationTestCategories_Get

```java
/**
 * Samples for ValidationTestCategories Get.
 */
public final class ValidationTestCategoriesGetSamples {
    /*
     * x-ms-original-file: 2026-08-01-preview/ValidationTestCategories_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ValidationTestCategories_Get_MaximumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void validationTestCategoriesGetMaximumSet(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.validationTestCategories()
            .getWithResponse("linux-quality-validations", com.azure.core.util.Context.NONE);
    }
}
```

### ValidationTestCategories_List

```java
/**
 * Samples for ValidationTestCategories List.
 */
public final class ValidationTestCategoriesListSamples {
    /*
     * x-ms-original-file: 2026-08-01-preview/ValidationTestCategories_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: ValidationTestCategories_ListBySubscription_MinimumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void validationTestCategoriesListBySubscriptionMinimumSet(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.validationTestCategories().list(null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-08-01-preview/ValidationTestCategories_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: ValidationTestCategories_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void validationTestCategoriesListBySubscriptionMaximumSet(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.validationTestCategories().list("audience eq 'Public'", com.azure.core.util.Context.NONE);
    }
}
```

### ValidationTestRuns_Get

```java
/**
 * Samples for ValidationTestRuns Get.
 */
public final class ValidationTestRunsGetSamples {
    /*
     * x-ms-original-file: 2026-08-01-preview/ValidationTestRuns_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ValidationTestRuns_Get_MaximumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void validationTestRunsGetMaximumSet(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.validationTestRuns()
            .getWithResponse("rgvalidate", "cvtest01", "contoso-linux-cert", "run-001", "linux-quality-run",
                com.azure.core.util.Context.NONE);
    }
}
```

### ValidationTestRuns_ListByExecutionPlanRun

```java
/**
 * Samples for ValidationTestRuns ListByExecutionPlanRun.
 */
public final class ValidationTestRunsListByExecutionPlanRunSamples {
    /*
     * x-ms-original-file: 2026-08-01-preview/ValidationTestRuns_ListByExecutionPlanRun_MaximumSet_Gen.json
     */
    /**
     * Sample code: ValidationTestRuns_ListByExecutionPlanRun_MaximumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void validationTestRunsListByExecutionPlanRunMaximumSet(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.validationTestRuns()
            .listByExecutionPlanRun("rgvalidate", "cvtest01", "contoso-linux-cert", "run-001",
                com.azure.core.util.Context.NONE);
    }
}
```

### ValidationTestVersions_Get

```java
/**
 * Samples for ValidationTestVersions Get.
 */
public final class ValidationTestVersionsGetSamples {
    /*
     * x-ms-original-file: 2026-08-01-preview/ValidationTestVersions_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ValidationTestVersions_Get_MaximumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void validationTestVersionsGetMaximumSet(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.validationTestVersions()
            .getWithResponse("linux-quality-validation", "1.0.0", com.azure.core.util.Context.NONE);
    }
}
```

### ValidationTestVersions_List

```java
/**
 * Samples for ValidationTestVersions List.
 */
public final class ValidationTestVersionsListSamples {
    /*
     * x-ms-original-file: 2026-08-01-preview/ValidationTestVersions_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: ValidationTestVersions_List_MaximumSet - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void validationTestVersionsListMaximumSetGeneratedByMaximumSetRule(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.validationTestVersions()
            .list("linux-quality-validation", "audience eq 'Public'", com.azure.core.util.Context.NONE);
    }
}
```

### ValidationTests_Get

```java
/**
 * Samples for ValidationTests Get.
 */
public final class ValidationTestsGetSamples {
    /*
     * x-ms-original-file: 2026-08-01-preview/ValidationTests_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ValidationTests_Get_MaximumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void
        validationTestsGetMaximumSet(com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.validationTests().getWithResponse("linux-quality-validation", com.azure.core.util.Context.NONE);
    }
}
```

### ValidationTests_List

```java
/**
 * Samples for ValidationTests List.
 */
public final class ValidationTestsListSamples {
    /*
     * x-ms-original-file: 2026-08-01-preview/ValidationTests_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: ValidationTests_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void validationTestsListBySubscriptionMaximumSet(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.validationTests().list("audience eq 'Public'", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-08-01-preview/ValidationTests_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: ValidationTests_ListBySubscription_MinimumSet.
     * 
     * @param manager Entry point to PlatformValidationManager.
     */
    public static void validationTestsListBySubscriptionMinimumSet(
        com.azure.resourcemanager.platformvalidation.PlatformValidationManager manager) {
        manager.validationTests().list(null, com.azure.core.util.Context.NONE);
    }
}
```

