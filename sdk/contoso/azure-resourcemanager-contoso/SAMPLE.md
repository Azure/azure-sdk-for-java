# Code snippets and samples


## Employees

- [CreateOrUpdate](#employees_createorupdate)
- [Delete](#employees_delete)
- [GetByResourceGroup](#employees_getbyresourcegroup)
- [List](#employees_list)
- [ListByResourceGroup](#employees_listbyresourcegroup)
- [Update](#employees_update)

## Operations

- [List](#operations_list)
### Employees_CreateOrUpdate

```java
import com.azure.resourcemanager.contoso.models.EmployeeProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Employees CreateOrUpdate.
 */
public final class EmployeesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2021-11-01/Employees_CreateOrUpdate.json
     */
    /**
     * Sample code: Employees_CreateOrUpdate.
     * 
     * @param manager Entry point to ContosoManager.
     */
    public static void employeesCreateOrUpdate(com.azure.resourcemanager.contoso.ContosoManager manager) {
        manager.employees()
            .define("9KF-f-8b")
            .withRegion("itajgxyqozseoygnl")
            .withExistingResourceGroup("rgopenapi")
            .withTags(mapOf("key2913", "fakeTokenPlaceholder"))
            .withProperties(new EmployeeProperties().withAge(30)
                .withCity("gydhnntudughbmxlkyzrskcdkotrxn")
                .withProfile("ms".getBytes()))
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

### Employees_Delete

```java
/**
 * Samples for Employees Delete.
 */
public final class EmployeesDeleteSamples {
    /*
     * x-ms-original-file: 2021-11-01/Employees_Delete.json
     */
    /**
     * Sample code: Employees_Delete.
     * 
     * @param manager Entry point to ContosoManager.
     */
    public static void employeesDelete(com.azure.resourcemanager.contoso.ContosoManager manager) {
        manager.employees().delete("rgopenapi", "5vX--BxSu3ux48rI4O9OQ569", com.azure.core.util.Context.NONE);
    }
}
```

### Employees_GetByResourceGroup

```java
/**
 * Samples for Employees GetByResourceGroup.
 */
public final class EmployeesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2021-11-01/Employees_Get.json
     */
    /**
     * Sample code: Employees_Get.
     * 
     * @param manager Entry point to ContosoManager.
     */
    public static void employeesGet(com.azure.resourcemanager.contoso.ContosoManager manager) {
        manager.employees()
            .getByResourceGroupWithResponse("rgopenapi", "le-8MU--J3W6q8D386p3-iT3", com.azure.core.util.Context.NONE);
    }
}
```

### Employees_List

```java
/**
 * Samples for Employees List.
 */
public final class EmployeesListSamples {
    /*
     * x-ms-original-file: 2021-11-01/Employees_ListBySubscription.json
     */
    /**
     * Sample code: Employees_ListBySubscription.
     * 
     * @param manager Entry point to ContosoManager.
     */
    public static void employeesListBySubscription(com.azure.resourcemanager.contoso.ContosoManager manager) {
        manager.employees().list(com.azure.core.util.Context.NONE);
    }
}
```

### Employees_ListByResourceGroup

```java
/**
 * Samples for Employees ListByResourceGroup.
 */
public final class EmployeesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2021-11-01/Employees_ListByResourceGroup.json
     */
    /**
     * Sample code: Employees_ListByResourceGroup.
     * 
     * @param manager Entry point to ContosoManager.
     */
    public static void employeesListByResourceGroup(com.azure.resourcemanager.contoso.ContosoManager manager) {
        manager.employees().listByResourceGroup("rgopenapi", com.azure.core.util.Context.NONE);
    }
}
```

### Employees_Update

```java
import com.azure.resourcemanager.contoso.models.Employee;
import com.azure.resourcemanager.contoso.models.EmployeeProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Employees Update.
 */
public final class EmployeesUpdateSamples {
    /*
     * x-ms-original-file: 2021-11-01/Employees_Update.json
     */
    /**
     * Sample code: Employees_Update.
     * 
     * @param manager Entry point to ContosoManager.
     */
    public static void employeesUpdate(com.azure.resourcemanager.contoso.ContosoManager manager) {
        Employee resource = manager.employees()
            .getByResourceGroupWithResponse("rgopenapi", "-XhyNJ--", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key7952", "fakeTokenPlaceholder"))
            .withProperties(
                new EmployeeProperties().withAge(24).withCity("uyfg").withProfile("oapgijcswfkruiuuzbwco".getBytes()))
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

### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2021-11-01/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to ContosoManager.
     */
    public static void operationsList(com.azure.resourcemanager.contoso.ContosoManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

