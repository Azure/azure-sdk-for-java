# Code snippets and samples


## Agents

- [CreateOrUpdate](#agents_createorupdate)
- [Delete](#agents_delete)
- [Get](#agents_get)
- [List](#agents_list)
- [Update](#agents_update)

## Endpoints

- [CreateOrUpdate](#endpoints_createorupdate)
- [Delete](#endpoints_delete)
- [Get](#endpoints_get)
- [List](#endpoints_list)
- [Update](#endpoints_update)

## JobDefinitions

- [CreateOrUpdate](#jobdefinitions_createorupdate)
- [Delete](#jobdefinitions_delete)
- [Get](#jobdefinitions_get)
- [List](#jobdefinitions_list)
- [StartJob](#jobdefinitions_startjob)
- [StopJob](#jobdefinitions_stopjob)
- [Update](#jobdefinitions_update)

## JobRuns

- [Get](#jobruns_get)
- [List](#jobruns_list)

## Operations

- [List](#operations_list)

## Projects

- [CreateOrUpdate](#projects_createorupdate)
- [Delete](#projects_delete)
- [Get](#projects_get)
- [List](#projects_list)
- [Update](#projects_update)

## StorageMovers

- [CreateOrUpdate](#storagemovers_createorupdate)
- [Delete](#storagemovers_delete)
- [GetByResourceGroup](#storagemovers_getbyresourcegroup)
- [List](#storagemovers_list)
- [ListByResourceGroup](#storagemovers_listbyresourcegroup)
- [Update](#storagemovers_update)
### Agents_CreateOrUpdate

```java
/** Samples for Agents CreateOrUpdate. */
public final class AgentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/Agents_CreateOrUpdate.json
     */
    /**
     * Sample code: Agents_CreateOrUpdate.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void agentsCreateOrUpdate(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager
            .agents()
            .define("examples-agentName")
            .withExistingStorageMover("examples-rg", "examples-storageMoverName")
            .withArcResourceId(
                "/subscriptions/11111111-2222-3333-4444-555555555555/resourceGroups/examples-rg/providers/Microsoft.HybridCompute/machines/examples-hybridComputeName")
            .withArcVmUuid("3bb2c024-eba9-4d18-9e7a-1d772fcc5fe9")
            .withDescription("Example Agent Description")
            .create();
    }
}
```

### Agents_Delete

```java
/** Samples for Agents Delete. */
public final class AgentsDeleteSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/Agents_Delete.json
     */
    /**
     * Sample code: Agents_Delete.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void agentsDelete(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager
            .agents()
            .delete("examples-rg", "examples-storageMoverName", "examples-agentName", com.azure.core.util.Context.NONE);
    }
}
```

### Agents_Get

```java
/** Samples for Agents Get. */
public final class AgentsGetSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/Agents_Get.json
     */
    /**
     * Sample code: Agents_Get.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void agentsGet(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager
            .agents()
            .getWithResponse(
                "examples-rg", "examples-storageMoverName", "examples-agentName", com.azure.core.util.Context.NONE);
    }
}
```

### Agents_List

```java
/** Samples for Agents List. */
public final class AgentsListSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/Agents_List.json
     */
    /**
     * Sample code: Agents_List.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void agentsList(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager.agents().list("examples-rg", "examples-storageMoverName", com.azure.core.util.Context.NONE);
    }
}
```

### Agents_Update

```java
import com.azure.resourcemanager.storagemover.models.Agent;

/** Samples for Agents Update. */
public final class AgentsUpdateSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/Agents_Update.json
     */
    /**
     * Sample code: Agents_Update.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void agentsUpdate(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        Agent resource =
            manager
                .agents()
                .getWithResponse(
                    "examples-rg", "examples-storageMoverName", "examples-agentName", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withDescription("Updated Agent Description").apply();
    }
}
```

### Endpoints_CreateOrUpdate

```java
import com.azure.resourcemanager.storagemover.models.EndpointBaseProperties;

/** Samples for Endpoints CreateOrUpdate. */
public final class EndpointsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/Endpoints_CreateOrUpdate.json
     */
    /**
     * Sample code: Endpoints_CreateOrUpdate.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void endpointsCreateOrUpdate(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager
            .endpoints()
            .define("examples-endpointName")
            .withExistingStorageMover("examples-rg", "examples-storageMoverName")
            .withProperties((EndpointBaseProperties) null)
            .create();
    }
}
```

### Endpoints_Delete

```java
/** Samples for Endpoints Delete. */
public final class EndpointsDeleteSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/Endpoints_Delete.json
     */
    /**
     * Sample code: Endpoints_Delete.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void endpointsDelete(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager
            .endpoints()
            .delete(
                "examples-rg", "examples-storageMoverName", "examples-endpointName", com.azure.core.util.Context.NONE);
    }
}
```

### Endpoints_Get

```java
/** Samples for Endpoints Get. */
public final class EndpointsGetSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/Endpoints_Get.json
     */
    /**
     * Sample code: Endpoints_Get.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void endpointsGet(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager
            .endpoints()
            .getWithResponse(
                "examples-rg", "examples-storageMoverName", "examples-endpointName", com.azure.core.util.Context.NONE);
    }
}
```

### Endpoints_List

```java
/** Samples for Endpoints List. */
public final class EndpointsListSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/Endpoints_List.json
     */
    /**
     * Sample code: Endpoints_List.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void endpointsList(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager.endpoints().list("examples-rg", "examples-storageMoverName", com.azure.core.util.Context.NONE);
    }
}
```

### Endpoints_Update

```java
import com.azure.resourcemanager.storagemover.models.Endpoint;

/** Samples for Endpoints Update. */
public final class EndpointsUpdateSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/Endpoints_Update.json
     */
    /**
     * Sample code: Endpoints_Update.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void endpointsUpdate(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        Endpoint resource =
            manager
                .endpoints()
                .getWithResponse(
                    "examples-rg",
                    "examples-storageMoverName",
                    "examples-endpointName",
                    com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().apply();
    }
}
```

### JobDefinitions_CreateOrUpdate

```java
import com.azure.resourcemanager.storagemover.models.CopyMode;

/** Samples for JobDefinitions CreateOrUpdate. */
public final class JobDefinitionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/JobDefinitions_CreateOrUpdate.json
     */
    /**
     * Sample code: JobDefinitions_CreateOrUpdate.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void jobDefinitionsCreateOrUpdate(
        com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager
            .jobDefinitions()
            .define("examples-jobDefinitionName")
            .withExistingProject("examples-rg", "examples-storageMoverName", "examples-projectName")
            .withCopyMode(CopyMode.ADDITIVE)
            .withSourceName("examples-sourceEndpointName")
            .withTargetName("examples-targetEndpointName")
            .withDescription("Example Job Definition Description")
            .withSourceSubpath("/")
            .withTargetSubpath("/")
            .withAgentName("migration-agent")
            .create();
    }
}
```

### JobDefinitions_Delete

```java
/** Samples for JobDefinitions Delete. */
public final class JobDefinitionsDeleteSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/JobDefinitions_Delete.json
     */
    /**
     * Sample code: Projects_Delete.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void projectsDelete(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager
            .jobDefinitions()
            .delete(
                "examples-rg",
                "examples-storageMoverName",
                "examples-projectName",
                "examples-jobDefinitionName",
                com.azure.core.util.Context.NONE);
    }
}
```

### JobDefinitions_Get

```java
/** Samples for JobDefinitions Get. */
public final class JobDefinitionsGetSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/JobDefinitions_Get.json
     */
    /**
     * Sample code: JobDefinitions_Get.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void jobDefinitionsGet(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager
            .jobDefinitions()
            .getWithResponse(
                "examples-rg",
                "examples-storageMoverName",
                "examples-projectName",
                "examples-jobDefinitionName",
                com.azure.core.util.Context.NONE);
    }
}
```

### JobDefinitions_List

```java
/** Samples for JobDefinitions List. */
public final class JobDefinitionsListSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/JobDefinitions_List.json
     */
    /**
     * Sample code: JobDefinitions_List.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void jobDefinitionsList(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager
            .jobDefinitions()
            .list("examples-rg", "examples-storageMoverName", "examples-projectName", com.azure.core.util.Context.NONE);
    }
}
```

### JobDefinitions_StartJob

```java
/** Samples for JobDefinitions StartJob. */
public final class JobDefinitionsStartJobSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/JobDefinitions_StartJob.json
     */
    /**
     * Sample code: JobDefinitions_StartJob.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void jobDefinitionsStartJob(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager
            .jobDefinitions()
            .startJobWithResponse(
                "examples-rg",
                "examples-storageMoverName",
                "examples-projectName",
                "examples-jobDefinitionName",
                com.azure.core.util.Context.NONE);
    }
}
```

### JobDefinitions_StopJob

```java
/** Samples for JobDefinitions StopJob. */
public final class JobDefinitionsStopJobSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/JobDefinitions_StopJob.json
     */
    /**
     * Sample code: JobDefinitions_StopJob.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void jobDefinitionsStopJob(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager
            .jobDefinitions()
            .stopJobWithResponse(
                "examples-rg",
                "examples-storageMoverName",
                "examples-projectName",
                "examples-jobDefinitionName",
                com.azure.core.util.Context.NONE);
    }
}
```

### JobDefinitions_Update

```java
import com.azure.resourcemanager.storagemover.models.JobDefinition;

/** Samples for JobDefinitions Update. */
public final class JobDefinitionsUpdateSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/JobDefinitions_Update.json
     */
    /**
     * Sample code: JobDefinitions_Update.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void jobDefinitionsUpdate(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        JobDefinition resource =
            manager
                .jobDefinitions()
                .getWithResponse(
                    "examples-rg",
                    "examples-storageMoverName",
                    "examples-projectName",
                    "examples-jobDefinitionName",
                    com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withDescription("Updated Job Definition Description")
            .withAgentName("updatedAgentName")
            .apply();
    }
}
```

### JobRuns_Get

```java
/** Samples for JobRuns Get. */
public final class JobRunsGetSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/JobRuns_Get.json
     */
    /**
     * Sample code: JobRuns_Get.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void jobRunsGet(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager
            .jobRuns()
            .getWithResponse(
                "examples-rg",
                "examples-storageMoverName",
                "examples-projectName",
                "examples-jobDefinitionName",
                "examples-jobRunName",
                com.azure.core.util.Context.NONE);
    }
}
```

### JobRuns_List

```java
/** Samples for JobRuns List. */
public final class JobRunsListSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/JobRuns_List.json
     */
    /**
     * Sample code: JobRuns_List.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void jobRunsList(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager
            .jobRuns()
            .list(
                "examples-rg",
                "examples-storageMoverName",
                "examples-projectName",
                "examples-jobDefinitionName",
                com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void operationsList(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Projects_CreateOrUpdate

```java
/** Samples for Projects CreateOrUpdate. */
public final class ProjectsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/Projects_CreateOrUpdate.json
     */
    /**
     * Sample code: Projects_CreateOrUpdate.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void projectsCreateOrUpdate(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager
            .projects()
            .define("examples-projectName")
            .withExistingStorageMover("examples-rg", "examples-storageMoverName")
            .withDescription("Example Project Description")
            .create();
    }
}
```

### Projects_Delete

```java
/** Samples for Projects Delete. */
public final class ProjectsDeleteSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/Projects_Delete.json
     */
    /**
     * Sample code: Projects_Delete.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void projectsDelete(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager
            .projects()
            .delete(
                "examples-rg", "examples-storageMoverName", "examples-projectName", com.azure.core.util.Context.NONE);
    }
}
```

### Projects_Get

```java
/** Samples for Projects Get. */
public final class ProjectsGetSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/Projects_Get.json
     */
    /**
     * Sample code: Projects_Get.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void projectsGet(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager
            .projects()
            .getWithResponse(
                "examples-rg", "examples-storageMoverName", "examples-projectName", com.azure.core.util.Context.NONE);
    }
}
```

### Projects_List

```java
/** Samples for Projects List. */
public final class ProjectsListSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/Projects_List.json
     */
    /**
     * Sample code: Projects_List.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void projectsList(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager.projects().list("examples-rg", "examples-storageMoverName", com.azure.core.util.Context.NONE);
    }
}
```

### Projects_Update

```java
import com.azure.resourcemanager.storagemover.models.Project;

/** Samples for Projects Update. */
public final class ProjectsUpdateSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/Projects_Update.json
     */
    /**
     * Sample code: Projects_Update.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void projectsUpdate(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        Project resource =
            manager
                .projects()
                .getWithResponse(
                    "examples-rg",
                    "examples-storageMoverName",
                    "examples-projectName",
                    com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withDescription("Example Project Description").apply();
    }
}
```

### StorageMovers_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for StorageMovers CreateOrUpdate. */
public final class StorageMoversCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/StorageMovers_CreateOrUpdate.json
     */
    /**
     * Sample code: StorageMovers_CreateOrUpdate.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void storageMoversCreateOrUpdate(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager
            .storageMovers()
            .define("examples-storageMoverName")
            .withRegion("eastus2")
            .withExistingResourceGroup("examples-rg")
            .withTags(mapOf("key1", "value1", "key2", "value2"))
            .withDescription("Example Storage Mover Description")
            .create();
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

### StorageMovers_Delete

```java
/** Samples for StorageMovers Delete. */
public final class StorageMoversDeleteSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/StorageMovers_Delete.json
     */
    /**
     * Sample code: StorageMovers_Delete.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void storageMoversDelete(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager.storageMovers().delete("examples-rg", "examples-storageMoverName", com.azure.core.util.Context.NONE);
    }
}
```

### StorageMovers_GetByResourceGroup

```java
/** Samples for StorageMovers GetByResourceGroup. */
public final class StorageMoversGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/StorageMovers_Get.json
     */
    /**
     * Sample code: StorageMovers_Get.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void storageMoversGet(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager
            .storageMovers()
            .getByResourceGroupWithResponse(
                "examples-rg", "examples-storageMoverName", com.azure.core.util.Context.NONE);
    }
}
```

### StorageMovers_List

```java
/** Samples for StorageMovers List. */
public final class StorageMoversListSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/StorageMovers_ListBySubscription.json
     */
    /**
     * Sample code: StorageMovers_List.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void storageMoversList(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager.storageMovers().list(com.azure.core.util.Context.NONE);
    }
}
```

### StorageMovers_ListByResourceGroup

```java
/** Samples for StorageMovers ListByResourceGroup. */
public final class StorageMoversListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/StorageMovers_List.json
     */
    /**
     * Sample code: StorageMovers_List.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void storageMoversList(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        manager.storageMovers().listByResourceGroup("examples-rg", com.azure.core.util.Context.NONE);
    }
}
```

### StorageMovers_Update

```java
import com.azure.resourcemanager.storagemover.models.StorageMover;

/** Samples for StorageMovers Update. */
public final class StorageMoversUpdateSamples {
    /*
     * x-ms-original-file: specification/storagemover/resource-manager/Microsoft.StorageMover/stable/2023-03-01/examples/StorageMovers_Update.json
     */
    /**
     * Sample code: StorageMovers_Update.
     *
     * @param manager Entry point to StorageMoverManager.
     */
    public static void storageMoversUpdate(com.azure.resourcemanager.storagemover.StorageMoverManager manager) {
        StorageMover resource =
            manager
                .storageMovers()
                .getByResourceGroupWithResponse(
                    "examples-rg", "examples-storageMoverName", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withDescription("Updated Storage Mover Description").apply();
    }
}
```

