# Code snippets and samples


## EduEnrollments

- [CreateOrUpdate](#eduenrollments_createorupdate)
- [Delete](#eduenrollments_delete)
- [GetByResourceGroup](#eduenrollments_getbyresourcegroup)
- [List](#eduenrollments_list)
- [ListByResourceGroup](#eduenrollments_listbyresourcegroup)
- [Update](#eduenrollments_update)

## Operations

- [List](#operations_list)
### EduEnrollments_CreateOrUpdate

```java
import com.azure.resourcemanager.programenrollment.models.DomainGroup;
import com.azure.resourcemanager.programenrollment.models.EduEnrollmentProperties;
import java.util.Arrays;

/**
 * Samples for EduEnrollments CreateOrUpdate.
 */
public final class EduEnrollmentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/EduEnrollments_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update an edu enrollment.
     * 
     * @param manager Entry point to ProgramEnrollmentManager.
     */
    public static void
        createOrUpdateAnEduEnrollment(com.azure.resourcemanager.programenrollment.ProgramEnrollmentManager manager) {
        manager.eduEnrollments()
            .define("default")
            .withRegion("eastus")
            .withExistingResourceGroup("testrg")
            .withProperties(new EduEnrollmentProperties().withDomains(
                Arrays.asList(new DomainGroup().withDomainNames(Arrays.asList("university.edu", "college.edu"))
                    .withTenantId("00000000-0000-0000-0000-000000000001"))))
            .create();
    }
}
```

### EduEnrollments_Delete

```java
/**
 * Samples for EduEnrollments Delete.
 */
public final class EduEnrollmentsDeleteSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/EduEnrollments_Delete.json
     */
    /**
     * Sample code: Delete an edu enrollment.
     * 
     * @param manager Entry point to ProgramEnrollmentManager.
     */
    public static void
        deleteAnEduEnrollment(com.azure.resourcemanager.programenrollment.ProgramEnrollmentManager manager) {
        manager.eduEnrollments().delete("testrg", "default", com.azure.core.util.Context.NONE);
    }
}
```

### EduEnrollments_GetByResourceGroup

```java
/**
 * Samples for EduEnrollments GetByResourceGroup.
 */
public final class EduEnrollmentsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/EduEnrollments_Get.json
     */
    /**
     * Sample code: Get an edu enrollment.
     * 
     * @param manager Entry point to ProgramEnrollmentManager.
     */
    public static void
        getAnEduEnrollment(com.azure.resourcemanager.programenrollment.ProgramEnrollmentManager manager) {
        manager.eduEnrollments().getByResourceGroupWithResponse("testrg", "default", com.azure.core.util.Context.NONE);
    }
}
```

### EduEnrollments_List

```java
/**
 * Samples for EduEnrollments List.
 */
public final class EduEnrollmentsListSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/EduEnrollments_ListBySubscription.json
     */
    /**
     * Sample code: List edu enrollments by subscription.
     * 
     * @param manager Entry point to ProgramEnrollmentManager.
     */
    public static void
        listEduEnrollmentsBySubscription(com.azure.resourcemanager.programenrollment.ProgramEnrollmentManager manager) {
        manager.eduEnrollments().list(com.azure.core.util.Context.NONE);
    }
}
```

### EduEnrollments_ListByResourceGroup

```java
/**
 * Samples for EduEnrollments ListByResourceGroup.
 */
public final class EduEnrollmentsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/EduEnrollments_ListByResourceGroup.json
     */
    /**
     * Sample code: List edu enrollments by resource group.
     * 
     * @param manager Entry point to ProgramEnrollmentManager.
     */
    public static void listEduEnrollmentsByResourceGroup(
        com.azure.resourcemanager.programenrollment.ProgramEnrollmentManager manager) {
        manager.eduEnrollments().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### EduEnrollments_Update

```java
import com.azure.resourcemanager.programenrollment.models.EduEnrollment;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for EduEnrollments Update.
 */
public final class EduEnrollmentsUpdateSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/EduEnrollments_Update.json
     */
    /**
     * Sample code: Update an edu enrollment.
     * 
     * @param manager Entry point to ProgramEnrollmentManager.
     */
    public static void
        updateAnEduEnrollment(com.azure.resourcemanager.programenrollment.ProgramEnrollmentManager manager) {
        EduEnrollment resource = manager.eduEnrollments()
            .getByResourceGroupWithResponse("testrg", "default", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("env", "test")).apply();
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
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/Operations_List.json
     */
    /**
     * Sample code: List operations for Microsoft.ProgramEnrollment.
     * 
     * @param manager Entry point to ProgramEnrollmentManager.
     */
    public static void listOperationsForMicrosoftProgramEnrollment(
        com.azure.resourcemanager.programenrollment.ProgramEnrollmentManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

