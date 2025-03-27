# Code snippets and samples


## Operations

- [List](#operations_list)

## Schedulers

- [CreateOrUpdate](#schedulers_createorupdate)
- [Delete](#schedulers_delete)
- [GetByResourceGroup](#schedulers_getbyresourcegroup)
- [List](#schedulers_list)
- [ListByResourceGroup](#schedulers_listbyresourcegroup)
- [Update](#schedulers_update)

## TaskHubs

- [CreateOrUpdate](#taskhubs_createorupdate)
- [Delete](#taskhubs_delete)
- [Get](#taskhubs_get)
- [ListByScheduler](#taskhubs_listbyscheduler)
### Operations_List

```java
/**
 * Samples for Schedulers GetByResourceGroup.
 */
public final class SchedulersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-10-01-preview/Schedulers_Get.json
     */
    /**
     * Sample code: Schedulers_Get.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void schedulersGet(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.schedulers()
            .getByResourceGroupWithResponse("rgopenapi", "testscheduler", com.azure.core.util.Context.NONE);
    }
}
```

### Schedulers_CreateOrUpdate

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2024-10-01-preview/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void operationsList(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Schedulers_Delete

```java
/**
 * Samples for Schedulers ListByResourceGroup.
 */
public final class SchedulersListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-10-01-preview/Schedulers_ListByResourceGroup.json
     */
    /**
     * Sample code: Schedulers_ListByResourceGroup.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void schedulersListByResourceGroup(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.schedulers().listByResourceGroup("rgopenapi", com.azure.core.util.Context.NONE);
    }
}
```

### Schedulers_GetByResourceGroup

```java
/**
 * Samples for TaskHubs Delete.
 */
public final class TaskHubsDeleteSamples {
    /*
     * x-ms-original-file: 2024-10-01-preview/TaskHubs_Delete.json
     */
    /**
     * Sample code: TaskHubs_Delete.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void taskHubsDelete(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.taskHubs().delete("rgopenapi", "testscheduler", "testtuskhub", com.azure.core.util.Context.NONE);
    }
}
```

### Schedulers_List

```java
/**
 * Samples for TaskHubs ListByScheduler.
 */
public final class TaskHubsListBySchedulerSamples {
    /*
     * x-ms-original-file: 2024-10-01-preview/TaskHubs_ListByScheduler.json
     */
    /**
     * Sample code: TaskHubs_ListByScheduler.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void taskHubsListByScheduler(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.taskHubs().listByScheduler("rgopenapi", "testtaskhub", com.azure.core.util.Context.NONE);
    }
}
```

### Schedulers_ListByResourceGroup

```java
import com.azure.resourcemanager.durabletask.models.TaskHubProperties;

/**
 * Samples for TaskHubs CreateOrUpdate.
 */
public final class TaskHubsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-10-01-preview/TaskHubs_CreateOrUpdate.json
     */
    /**
     * Sample code: TaskHubs_CreateOrUpdate.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void taskHubsCreateOrUpdate(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.taskHubs()
            .define("testtaskhub")
            .withExistingScheduler("rgopenapi", "testscheduler")
            .withProperties(new TaskHubProperties())
            .create();
    }
}
```

### Schedulers_Update

```java
import com.azure.resourcemanager.durabletask.models.SchedulerProperties;
import com.azure.resourcemanager.durabletask.models.SchedulerSku;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Schedulers CreateOrUpdate.
 */
public final class SchedulersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-10-01-preview/Schedulers_CreateOrUpdate.json
     */
    /**
     * Sample code: Schedulers_CreateOrUpdate.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void schedulersCreateOrUpdate(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.schedulers()
            .define("testscheduler")
            .withRegion("northcentralus")
            .withExistingResourceGroup("rgopenapi")
            .withTags(mapOf("key7131", "fakeTokenPlaceholder", "key2138", "fakeTokenPlaceholder"))
            .withProperties(new SchedulerProperties().withIpAllowlist(Arrays.asList("10.0.0.0/8"))
                .withSku(new SchedulerSku().withName("Dedicated")))
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

### TaskHubs_CreateOrUpdate

```java
/**
 * Samples for TaskHubs Get.
 */
public final class TaskHubsGetSamples {
    /*
     * x-ms-original-file: 2024-10-01-preview/TaskHubs_Get.json
     */
    /**
     * Sample code: TaskHubs_Get.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void taskHubsGet(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.taskHubs()
            .getWithResponse("rgopenapi", "testscheduler", "testtuskhub", com.azure.core.util.Context.NONE);
    }
}
```

### TaskHubs_Delete

```java
import com.azure.resourcemanager.durabletask.models.Scheduler;
import com.azure.resourcemanager.durabletask.models.SchedulerPropertiesUpdate;
import com.azure.resourcemanager.durabletask.models.SchedulerSkuUpdate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Schedulers Update.
 */
public final class SchedulersUpdateSamples {
    /*
     * x-ms-original-file: 2024-10-01-preview/Schedulers_Update.json
     */
    /**
     * Sample code: Schedulers_Update.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void schedulersUpdate(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        Scheduler resource = manager.schedulers()
            .getByResourceGroupWithResponse("rgopenapi", "testscheduler", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key8653", "fakeTokenPlaceholder"))
            .withProperties(new SchedulerPropertiesUpdate().withIpAllowlist(Arrays.asList("10.0.0.0/8"))
                .withSku(new SchedulerSkuUpdate().withName("Dedicated").withCapacity(10)))
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

### TaskHubs_Get

```java
/**
 * Samples for Schedulers Delete.
 */
public final class SchedulersDeleteSamples {
    /*
     * x-ms-original-file: 2024-10-01-preview/Schedulers_Delete.json
     */
    /**
     * Sample code: Schedulers_Delete.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void schedulersDelete(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.schedulers().delete("rgopenapi", "testscheduler", com.azure.core.util.Context.NONE);
    }
}
```

### TaskHubs_ListByScheduler

```java
/**
 * Samples for Schedulers List.
 */
public final class SchedulersListSamples {
    /*
     * x-ms-original-file: 2024-10-01-preview/Schedulers_ListBySubscription.json
     */
    /**
     * Sample code: Schedulers_ListBySubscription.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void schedulersListBySubscription(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.schedulers().list(com.azure.core.util.Context.NONE);
    }
}
```

