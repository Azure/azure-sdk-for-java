# Code snippets and samples


## AgentPools

- [Create](#agentpools_create)
- [Delete](#agentpools_delete)
- [Get](#agentpools_get)
- [GetQueueStatus](#agentpools_getqueuestatus)
- [List](#agentpools_list)
- [Update](#agentpools_update)

## Registries

- [GetBuildSourceUploadUrl](#registries_getbuildsourceuploadurl)
- [ScheduleRun](#registries_schedulerun)

## Runs

- [Cancel](#runs_cancel)
- [Get](#runs_get)
- [GetLogSasUrl](#runs_getlogsasurl)
- [List](#runs_list)
- [Update](#runs_update)

## TaskRuns

- [Create](#taskruns_create)
- [Delete](#taskruns_delete)
- [Get](#taskruns_get)
- [GetDetails](#taskruns_getdetails)
- [List](#taskruns_list)
- [Update](#taskruns_update)

## Tasks

- [Create](#tasks_create)
- [Delete](#tasks_delete)
- [Get](#tasks_get)
- [GetDetails](#tasks_getdetails)
- [List](#tasks_list)
- [Update](#tasks_update)
### AgentPools_Create

```java
import com.azure.resourcemanager.containerregistry.tasks.models.AgentPoolProperties;
import com.azure.resourcemanager.containerregistry.tasks.models.OS;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AgentPools Create.
 */
public final class AgentPoolsCreateSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/AgentPoolsCreate.json
     */
    /**
     * Sample code: AgentPools_Create.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        agentPoolsCreate(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.agentPools()
            .define("myAgentPool")
            .withRegion("WESTUS")
            .withExistingRegistry("myResourceGroup", "myRegistry")
            .withTags(mapOf("key", "fakeTokenPlaceholder"))
            .withProperties(new AgentPoolProperties().withCount(1).withTier("S1").withOs(OS.LINUX))
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

### AgentPools_Delete

```java
/**
 * Samples for AgentPools Delete.
 */
public final class AgentPoolsDeleteSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/AgentPoolsDelete.json
     */
    /**
     * Sample code: AgentPools_Delete.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        agentPoolsDelete(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.agentPools().delete("myResourceGroup", "myRegistry", "myAgentPool", com.azure.core.util.Context.NONE);
    }
}
```

### AgentPools_Get

```java
/**
 * Samples for AgentPools Get.
 */
public final class AgentPoolsGetSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/AgentPoolsGet.json
     */
    /**
     * Sample code: AgentPools_Get.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        agentPoolsGet(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.agentPools()
            .getWithResponse("myResourceGroup", "myRegistry", "myAgentPool", com.azure.core.util.Context.NONE);
    }
}
```

### AgentPools_GetQueueStatus

```java
/**
 * Samples for AgentPools GetQueueStatus.
 */
public final class AgentPoolsGetQueueStatusSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/AgentPoolsGetQueueStatus.json
     */
    /**
     * Sample code: AgentPools_GetQueueStatus.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void agentPoolsGetQueueStatus(
        com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.agentPools()
            .getQueueStatusWithResponse("myResourceGroup", "myRegistry", "myAgentPool",
                com.azure.core.util.Context.NONE);
    }
}
```

### AgentPools_List

```java
/**
 * Samples for AgentPools List.
 */
public final class AgentPoolsListSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/AgentPoolsList.json
     */
    /**
     * Sample code: AgentPools_List.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        agentPoolsList(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.agentPools().list("myResourceGroup", "myRegistry", com.azure.core.util.Context.NONE);
    }
}
```

### AgentPools_Update

```java
import com.azure.resourcemanager.containerregistry.tasks.models.AgentPool;

/**
 * Samples for AgentPools Update.
 */
public final class AgentPoolsUpdateSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/AgentPoolsUpdate.json
     */
    /**
     * Sample code: AgentPools_Update.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        agentPoolsUpdate(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        AgentPool resource = manager.agentPools()
            .getWithResponse("myResourceGroup", "myRegistry", "myAgentPool", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withCount(1).apply();
    }
}
```

### Registries_GetBuildSourceUploadUrl

```java
/**
 * Samples for Registries GetBuildSourceUploadUrl.
 */
public final class RegistriesGetBuildSourceUploadUrlSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/RegistriesGetBuildSourceUploadUrl.json
     */
    /**
     * Sample code: Registries_GetBuildSourceUploadUrl.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void registriesGetBuildSourceUploadUrl(
        com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.registries()
            .getBuildSourceUploadUrlWithResponse("myResourceGroup", "myRegistry", com.azure.core.util.Context.NONE);
    }
}
```

### Registries_ScheduleRun

```java
import com.azure.resourcemanager.containerregistry.tasks.models.AgentProperties;
import com.azure.resourcemanager.containerregistry.tasks.models.Architecture;
import com.azure.resourcemanager.containerregistry.tasks.models.Argument;
import com.azure.resourcemanager.containerregistry.tasks.models.Credentials;
import com.azure.resourcemanager.containerregistry.tasks.models.CustomRegistryCredentials;
import com.azure.resourcemanager.containerregistry.tasks.models.DockerBuildRequest;
import com.azure.resourcemanager.containerregistry.tasks.models.EncodedTaskRunRequest;
import com.azure.resourcemanager.containerregistry.tasks.models.FileTaskRunRequest;
import com.azure.resourcemanager.containerregistry.tasks.models.OS;
import com.azure.resourcemanager.containerregistry.tasks.models.OverrideTaskStepProperties;
import com.azure.resourcemanager.containerregistry.tasks.models.PlatformProperties;
import com.azure.resourcemanager.containerregistry.tasks.models.SecretObject;
import com.azure.resourcemanager.containerregistry.tasks.models.SecretObjectType;
import com.azure.resourcemanager.containerregistry.tasks.models.SetValue;
import com.azure.resourcemanager.containerregistry.tasks.models.SourceRegistryCredentials;
import com.azure.resourcemanager.containerregistry.tasks.models.SourceRegistryLoginMode;
import com.azure.resourcemanager.containerregistry.tasks.models.TaskRunRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Registries ScheduleRun.
 */
public final class RegistriesScheduleRunSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/RegistriesScheduleRun_FileTaskRun.json
     */
    /**
     * Sample code: Registries_ScheduleRun_FileTaskRun.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void registriesScheduleRunFileTaskRun(
        com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.registries()
            .scheduleRunWithResponse("myResourceGroup", "myRegistry", new FileTaskRunRequest()
                .withTaskFilePath("acb.yaml")
                .withValuesFilePath("prod-values.yaml")
                .withValues(Arrays.asList(
                    new SetValue().withName("mytestargument").withValue("mytestvalue").withIsSecret(false),
                    new SetValue().withName("mysecrettestargument").withValue("mysecrettestvalue").withIsSecret(true)))
                .withPlatform(new PlatformProperties().withOs(OS.LINUX))
                .withAgentConfiguration(new AgentProperties().withCpu(2))
                .withSourceLocation(
                    "https://myaccount.blob.core.windows.net/sascontainer/source.zip?sv=2015-04-05&st=2015-04-29T22%3A18%3A26Z&se=2015-04-30T02%3A23%3A26Z&sr=b&sp=rw&sip=168.1.5.60-168.1.5.70&spr=https&sig=Z%2FRHIX5Xcg0Mq2rqI3OlWTjEg2tYkboXr1P9ZUXDtkk%3D"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-03-01-preview/RegistriesScheduleRun.json
     */
    /**
     * Sample code: Registries_ScheduleRun.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        registriesScheduleRun(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.registries()
            .scheduleRunWithResponse("myResourceGroup", "myRegistry", new DockerBuildRequest()
                .withIsArchiveEnabled(true)
                .withImageNames(Arrays.asList("azurerest:testtag"))
                .withIsPushEnabled(true)
                .withNoCache(true)
                .withDockerFilePath("DockerFile")
                .withArguments(Arrays.asList(
                    new Argument().withName("mytestargument").withValue("mytestvalue").withIsSecret(false),
                    new Argument().withName("mysecrettestargument").withValue("mysecrettestvalue").withIsSecret(true)))
                .withPlatform(new PlatformProperties().withOs(OS.LINUX).withArchitecture(Architecture.AMD64))
                .withAgentConfiguration(new AgentProperties().withCpu(2))
                .withSourceLocation(
                    "https://myaccount.blob.core.windows.net/sascontainer/source.zip?sv=2015-04-05&st=2015-04-29T22%3A18%3A26Z&se=2015-04-30T02%3A23%3A26Z&sr=b&sp=rw&sip=168.1.5.60-168.1.5.70&spr=https&sig=Z%2FRHIX5Xcg0Mq2rqI3OlWTjEg2tYkboXr1P9ZUXDtkk%3D"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-03-01-preview/RegistriesScheduleRun_EncodedTaskRun.json
     */
    /**
     * Sample code: Registries_ScheduleRun_EncodedTaskRun.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void registriesScheduleRunEncodedTaskRun(
        com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.registries()
            .scheduleRunWithResponse("myResourceGroup", "myRegistry", new EncodedTaskRunRequest()
                .withEncodedTaskContent("fakeTokenPlaceholder")
                .withEncodedValuesContent("fakeTokenPlaceholder")
                .withValues(Arrays.asList(
                    new SetValue().withName("mytestargument").withValue("mytestvalue").withIsSecret(false),
                    new SetValue().withName("mysecrettestargument").withValue("mysecrettestvalue").withIsSecret(true)))
                .withPlatform(new PlatformProperties().withOs(OS.LINUX))
                .withAgentConfiguration(new AgentProperties().withCpu(2)), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-03-01-preview/RegistriesScheduleRun_WithCustomCredentials.json
     */
    /**
     * Sample code: Registries_ScheduleRun_WithCustomCredentials.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void registriesScheduleRunWithCustomCredentials(
        com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.registries()
            .scheduleRunWithResponse("myResourceGroup", "myRegistry", new DockerBuildRequest()
                .withIsArchiveEnabled(true)
                .withImageNames(Arrays.asList("azurerest:testtag"))
                .withIsPushEnabled(true)
                .withNoCache(true)
                .withDockerFilePath("DockerFile")
                .withTarget("stage1")
                .withArguments(Arrays.asList(
                    new Argument().withName("mytestargument").withValue("mytestvalue").withIsSecret(false),
                    new Argument().withName("mysecrettestargument").withValue("mysecrettestvalue").withIsSecret(true)))
                .withPlatform(new PlatformProperties().withOs(OS.LINUX).withArchitecture(Architecture.AMD64))
                .withAgentConfiguration(new AgentProperties().withCpu(2))
                .withSourceLocation(
                    "https://myaccount.blob.core.windows.net/sascontainer/source.zip?sv=2015-04-05&st=2015-04-29T22%3A18%3A26Z&se=2015-04-30T02%3A23%3A26Z&sr=b&sp=rw&sip=168.1.5.60-168.1.5.70&spr=https&sig=Z%2FRHIX5Xcg0Mq2rqI3OlWTjEg2tYkboXr1P9ZUXDtkk%3D")
                .withCredentials(new Credentials()
                    .withSourceRegistry(new SourceRegistryCredentials().withLoginMode(SourceRegistryLoginMode.DEFAULT))
                    .withCustomRegistries(mapOf("myregistry.azurecr.io",
                        new CustomRegistryCredentials()
                            .withUserName(new SecretObject().withValue("reg1").withType(SecretObjectType.OPAQUE))
                            .withPassword(new SecretObject().withValue("***").withType(SecretObjectType.OPAQUE)),
                        "myregistry2.azurecr.io",
                        new CustomRegistryCredentials()
                            .withUserName(new SecretObject().withValue("reg2").withType(SecretObjectType.OPAQUE))
                            .withPassword(new SecretObject().withValue("***").withType(SecretObjectType.OPAQUE))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-03-01-preview/RegistriesScheduleRun_WithLogTemplate.json
     */
    /**
     * Sample code: Registries_ScheduleRun_WithLogTemplate.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void registriesScheduleRunWithLogTemplate(
        com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.registries()
            .scheduleRunWithResponse("myResourceGroup", "myRegistry", new DockerBuildRequest()
                .withIsArchiveEnabled(true)
                .withLogTemplate("acr/tasks:{{.Run.OS}}")
                .withImageNames(Arrays.asList("azurerest:testtag"))
                .withIsPushEnabled(true)
                .withNoCache(true)
                .withDockerFilePath("DockerFile")
                .withArguments(Arrays.asList(
                    new Argument().withName("mytestargument").withValue("mytestvalue").withIsSecret(false),
                    new Argument().withName("mysecrettestargument").withValue("mysecrettestvalue").withIsSecret(true)))
                .withPlatform(new PlatformProperties().withOs(OS.LINUX).withArchitecture(Architecture.AMD64))
                .withAgentConfiguration(new AgentProperties().withCpu(2))
                .withSourceLocation(
                    "https://myaccount.blob.core.windows.net/sascontainer/source.zip?sv=2015-04-05&st=2015-04-29T22%3A18%3A26Z&se=2015-04-30T02%3A23%3A26Z&sr=b&sp=rw&sip=168.1.5.60-168.1.5.70&spr=https&sig=Z%2FRHIX5Xcg0Mq2rqI3OlWTjEg2tYkboXr1P9ZUXDtkk%3D"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-03-01-preview/RegistriesScheduleRun_Task.json
     */
    /**
     * Sample code: Registries_ScheduleRun_Task.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void registriesScheduleRunTask(
        com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.registries()
            .scheduleRunWithResponse("myResourceGroup", "myRegistry",
                new TaskRunRequest().withTaskId("myTask")
                    .withOverrideTaskStepProperties(new OverrideTaskStepProperties().withFile("overriddenDockerfile")
                        .withArguments(Arrays.asList(
                            new Argument().withName("mytestargument").withValue("mytestvalue").withIsSecret(false),
                            new Argument().withName("mysecrettestargument")
                                .withValue("mysecrettestvalue")
                                .withIsSecret(true)))
                        .withTarget("build")
                        .withValues(Arrays.asList(
                            new SetValue().withName("mytestname").withValue("mytestvalue").withIsSecret(false),
                            new SetValue().withName("mysecrettestname")
                                .withValue("mysecrettestvalue")
                                .withIsSecret(true)))
                        .withUpdateTriggerToken("fakeTokenPlaceholder")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-03-01-preview/RegistriesScheduleRun_FileTask_WithCustomCredentials.json
     */
    /**
     * Sample code: Registries_ScheduleRun_Task_WithCustomCredentials.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void registriesScheduleRunTaskWithCustomCredentials(
        com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.registries()
            .scheduleRunWithResponse("myResourceGroup", "myRegistry", new FileTaskRunRequest()
                .withTaskFilePath("acb.yaml")
                .withValues(Arrays.asList(
                    new SetValue().withName("mytestargument").withValue("mytestvalue").withIsSecret(false),
                    new SetValue().withName("mysecrettestargument").withValue("mysecrettestvalue").withIsSecret(true)))
                .withPlatform(new PlatformProperties().withOs(OS.LINUX))
                .withCredentials(new Credentials()
                    .withSourceRegistry(new SourceRegistryCredentials().withLoginMode(SourceRegistryLoginMode.DEFAULT))
                    .withCustomRegistries(mapOf("myregistry.azurecr.io",
                        new CustomRegistryCredentials()
                            .withUserName(new SecretObject().withValue("reg1").withType(SecretObjectType.OPAQUE))
                            .withPassword(new SecretObject().withValue("***").withType(SecretObjectType.OPAQUE))))),
                com.azure.core.util.Context.NONE);
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

### Runs_Cancel

```java
/**
 * Samples for Runs Cancel.
 */
public final class RunsCancelSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/RunsCancel.json
     */
    /**
     * Sample code: Runs_Cancel.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        runsCancel(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.runs()
            .cancelWithResponse("myResourceGroup", "myRegistry", "0accec26-d6de-4757-8e74-d080f38eaaab",
                com.azure.core.util.Context.NONE);
    }
}
```

### Runs_Get

```java
/**
 * Samples for Runs Get.
 */
public final class RunsGetSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/RunsGet.json
     */
    /**
     * Sample code: Runs_Get.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        runsGet(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.runs()
            .getWithResponse("myResourceGroup", "myRegistry", "0accec26-d6de-4757-8e74-d080f38eaaab",
                com.azure.core.util.Context.NONE);
    }
}
```

### Runs_GetLogSasUrl

```java
/**
 * Samples for Runs GetLogSasUrl.
 */
public final class RunsGetLogSasUrlSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/RunsGetLogSasUrl.json
     */
    /**
     * Sample code: Runs_GetLogSasUrl.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        runsGetLogSasUrl(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.runs()
            .getLogSasUrlWithResponse("myResourceGroup", "myRegistry", "0accec26-d6de-4757-8e74-d080f38eaaab",
                com.azure.core.util.Context.NONE);
    }
}
```

### Runs_List

```java
/**
 * Samples for Runs List.
 */
public final class RunsListSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/RunsList.json
     */
    /**
     * Sample code: Runs_List.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        runsList(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.runs().list("myResourceGroup", "myRegistry", "", 10, com.azure.core.util.Context.NONE);
    }
}
```

### Runs_Update

```java
import com.azure.resourcemanager.containerregistry.tasks.models.RunUpdateParameters;

/**
 * Samples for Runs Update.
 */
public final class RunsUpdateSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/RunsUpdate.json
     */
    /**
     * Sample code: Runs_Update.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        runsUpdate(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.runs()
            .updateWithResponse("myResourceGroup", "myRegistry", "0accec26-d6de-4757-8e74-d080f38eaaab",
                new RunUpdateParameters().withIsArchiveEnabled(true), com.azure.core.util.Context.NONE);
    }
}
```

### TaskRuns_Create

```java
import com.azure.resourcemanager.containerregistry.tasks.fluent.models.TaskRunPropertiesInner;
import com.azure.resourcemanager.containerregistry.tasks.models.Architecture;
import com.azure.resourcemanager.containerregistry.tasks.models.Credentials;
import com.azure.resourcemanager.containerregistry.tasks.models.EncodedTaskRunRequest;
import com.azure.resourcemanager.containerregistry.tasks.models.OS;
import com.azure.resourcemanager.containerregistry.tasks.models.PlatformProperties;
import java.util.Arrays;

/**
 * Samples for TaskRuns Create.
 */
public final class TaskRunsCreateSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/TaskRunsCreate.json
     */
    /**
     * Sample code: TaskRuns_Create.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        taskRunsCreate(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.taskRuns()
            .define("myRun")
            .withExistingRegistry("myResourceGroup", "myRegistry")
            .withProperties(new TaskRunPropertiesInner()
                .withRunRequest(new EncodedTaskRunRequest().withEncodedTaskContent("fakeTokenPlaceholder")
                    .withEncodedValuesContent("fakeTokenPlaceholder")
                    .withValues(Arrays.asList())
                    .withPlatform(new PlatformProperties().withOs(OS.LINUX).withArchitecture(Architecture.AMD64))
                    .withCredentials(new Credentials()))
                .withForceUpdateTag("test"))
            .create();
    }
}
```

### TaskRuns_Delete

```java
/**
 * Samples for TaskRuns Delete.
 */
public final class TaskRunsDeleteSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/TaskRunsDelete.json
     */
    /**
     * Sample code: TaskRuns_Delete.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        taskRunsDelete(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.taskRuns()
            .deleteWithResponse("myResourceGroup", "myRegistry", "myRun", com.azure.core.util.Context.NONE);
    }
}
```

### TaskRuns_Get

```java
/**
 * Samples for TaskRuns Get.
 */
public final class TaskRunsGetSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/TaskRunsGet.json
     */
    /**
     * Sample code: TaskRuns_Get.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        taskRunsGet(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.taskRuns().getWithResponse("myResourceGroup", "myRegistry", "myRun", com.azure.core.util.Context.NONE);
    }
}
```

### TaskRuns_GetDetails

```java
/**
 * Samples for TaskRuns GetDetails.
 */
public final class TaskRunsGetDetailsSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/TaskRunsGetDetails.json
     */
    /**
     * Sample code: TaskRuns_GetDetails.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        taskRunsGetDetails(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.taskRuns()
            .getDetailsWithResponse("myResourceGroup", "myRegistry", "myRun", com.azure.core.util.Context.NONE);
    }
}
```

### TaskRuns_List

```java
/**
 * Samples for TaskRuns List.
 */
public final class TaskRunsListSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/TaskRunsList.json
     */
    /**
     * Sample code: TaskRuns_List.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        taskRunsList(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.taskRuns().list("myResourceGroup", "myRegistry", com.azure.core.util.Context.NONE);
    }
}
```

### TaskRuns_Update

```java
import com.azure.resourcemanager.containerregistry.tasks.models.Architecture;
import com.azure.resourcemanager.containerregistry.tasks.models.Credentials;
import com.azure.resourcemanager.containerregistry.tasks.models.EncodedTaskRunRequest;
import com.azure.resourcemanager.containerregistry.tasks.models.OS;
import com.azure.resourcemanager.containerregistry.tasks.models.PlatformProperties;
import com.azure.resourcemanager.containerregistry.tasks.models.TaskRun;
import java.util.Arrays;

/**
 * Samples for TaskRuns Update.
 */
public final class TaskRunsUpdateSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/TaskRunsUpdate.json
     */
    /**
     * Sample code: TaskRuns_Update.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        taskRunsUpdate(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        TaskRun resource = manager.taskRuns()
            .getWithResponse("myResourceGroup", "myRegistry", "myRun", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withRunRequest(new EncodedTaskRunRequest().withIsArchiveEnabled(true)
                .withEncodedTaskContent("fakeTokenPlaceholder")
                .withEncodedValuesContent("fakeTokenPlaceholder")
                .withValues(Arrays.asList())
                .withPlatform(new PlatformProperties().withOs(OS.LINUX).withArchitecture(Architecture.AMD64))
                .withCredentials(new Credentials()))
            .withForceUpdateTag("test")
            .apply();
    }
}
```

### Tasks_Create

```java
import com.azure.resourcemanager.containerregistry.tasks.models.AgentProperties;
import com.azure.resourcemanager.containerregistry.tasks.models.Architecture;
import com.azure.resourcemanager.containerregistry.tasks.models.Argument;
import com.azure.resourcemanager.containerregistry.tasks.models.AuthInfo;
import com.azure.resourcemanager.containerregistry.tasks.models.BaseImageTrigger;
import com.azure.resourcemanager.containerregistry.tasks.models.BaseImageTriggerType;
import com.azure.resourcemanager.containerregistry.tasks.models.Credentials;
import com.azure.resourcemanager.containerregistry.tasks.models.DockerBuildStep;
import com.azure.resourcemanager.containerregistry.tasks.models.IdentityProperties;
import com.azure.resourcemanager.containerregistry.tasks.models.OS;
import com.azure.resourcemanager.containerregistry.tasks.models.PlatformProperties;
import com.azure.resourcemanager.containerregistry.tasks.models.ResourceIdentityType;
import com.azure.resourcemanager.containerregistry.tasks.models.SourceControlType;
import com.azure.resourcemanager.containerregistry.tasks.models.SourceProperties;
import com.azure.resourcemanager.containerregistry.tasks.models.SourceRegistryCredentials;
import com.azure.resourcemanager.containerregistry.tasks.models.SourceTrigger;
import com.azure.resourcemanager.containerregistry.tasks.models.SourceTriggerEvent;
import com.azure.resourcemanager.containerregistry.tasks.models.TaskProperties;
import com.azure.resourcemanager.containerregistry.tasks.models.TaskStatus;
import com.azure.resourcemanager.containerregistry.tasks.models.TimerTrigger;
import com.azure.resourcemanager.containerregistry.tasks.models.TokenType;
import com.azure.resourcemanager.containerregistry.tasks.models.TriggerProperties;
import com.azure.resourcemanager.containerregistry.tasks.models.UpdateTriggerPayloadType;
import com.azure.resourcemanager.containerregistry.tasks.models.UserIdentityProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Tasks Create.
 */
public final class TasksCreateSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/ManagedIdentity/TasksCreate_WithSystemIdentity.json
     */
    /**
     * Sample code: Tasks_Create_WithUserIdentities_WithSystemIdentity.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void tasksCreateWithUserIdentitiesWithSystemIdentity(
        com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.tasks()
            .define("mytTask")
            .withRegion("eastus")
            .withExistingRegistry("myResourceGroup", "myRegistry")
            .withTags(mapOf("testkey", "fakeTokenPlaceholder"))
            .withProperties(new TaskProperties().withStatus(TaskStatus.ENABLED)
                .withPlatform(new PlatformProperties().withOs(OS.LINUX).withArchitecture(Architecture.AMD64))
                .withAgentConfiguration(new AgentProperties().withCpu(2))
                .withStep(new DockerBuildStep().withContextPath("src")
                    .withImageNames(Arrays.asList("azurerest:testtag"))
                    .withIsPushEnabled(true)
                    .withNoCache(false)
                    .withDockerFilePath("src/DockerFile")
                    .withArguments(Arrays.asList(
                        new Argument().withName("mytestargument").withValue("mytestvalue").withIsSecret(false),
                        new Argument().withName("mysecrettestargument")
                            .withValue("mysecrettestvalue")
                            .withIsSecret(true))))
                .withTrigger(
                    new TriggerProperties()
                        .withTimerTriggers(
                            Arrays.asList(new TimerTrigger().withSchedule("30 9 * * 1-5").withName("myTimerTrigger")))
                        .withSourceTriggers(
                            Arrays.asList(new SourceTrigger()
                                .withSourceRepository(
                                    new SourceProperties().withSourceControlType(SourceControlType.GITHUB)
                                        .withRepositoryUrl("https://github.com/Azure/azure-rest-api-specs")
                                        .withBranch("master")
                                        .withSourceControlAuthProperties(new AuthInfo().withTokenType(TokenType.PAT)
                                            .withToken("fakeTokenPlaceholder")))
                                .withSourceTriggerEvents(Arrays.asList(SourceTriggerEvent.COMMIT))
                                .withName("mySourceTrigger")))
                        .withBaseImageTrigger(
                            new BaseImageTrigger().withBaseImageTriggerType(BaseImageTriggerType.RUNTIME)
                                .withName("myBaseImageTrigger")))
                .withIsSystemTask(false))
            .withIdentity(new IdentityProperties().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
            .create();
    }

    /*
     * x-ms-original-file: 2025-03-01-preview/ManagedIdentity/TasksCreate_WithLoginIdentity.json
     */
    /**
     * Sample code: Tasks_Create_WithLoginIdentity.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void tasksCreateWithLoginIdentity(
        com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.tasks()
            .define("mytTask")
            .withRegion("eastus")
            .withExistingRegistry("myResourceGroup", "myRegistry")
            .withTags(mapOf("testkey", "fakeTokenPlaceholder"))
            .withProperties(new TaskProperties().withStatus(TaskStatus.ENABLED)
                .withPlatform(new PlatformProperties().withOs(OS.LINUX).withArchitecture(Architecture.AMD64))
                .withAgentConfiguration(new AgentProperties().withCpu(2))
                .withStep(new DockerBuildStep().withContextPath("src")
                    .withImageNames(Arrays.asList("azurerest:testtag"))
                    .withIsPushEnabled(true)
                    .withNoCache(false)
                    .withDockerFilePath("src/DockerFile")
                    .withArguments(Arrays.asList(
                        new Argument().withName("mytestargument").withValue("mytestvalue").withIsSecret(false),
                        new Argument().withName("mysecrettestargument")
                            .withValue("mysecrettestvalue")
                            .withIsSecret(true))))
                .withTrigger(
                    new TriggerProperties()
                        .withTimerTriggers(
                            Arrays.asList(new TimerTrigger().withSchedule("30 9 * * 1-5").withName("myTimerTrigger")))
                        .withSourceTriggers(
                            Arrays.asList(new SourceTrigger()
                                .withSourceRepository(
                                    new SourceProperties().withSourceControlType(SourceControlType.GITHUB)
                                        .withRepositoryUrl("https://github.com/Azure/azure-rest-api-specs")
                                        .withBranch("master")
                                        .withSourceControlAuthProperties(new AuthInfo().withTokenType(TokenType.PAT)
                                            .withToken("fakeTokenPlaceholder")))
                                .withSourceTriggerEvents(Arrays.asList(SourceTriggerEvent.COMMIT))
                                .withName("mySourceTrigger")))
                        .withBaseImageTrigger(
                            new BaseImageTrigger().withBaseImageTriggerType(BaseImageTriggerType.RUNTIME)
                                .withName("myBaseImageTrigger")))
                .withCredentials(
                    new Credentials().withSourceRegistry(new SourceRegistryCredentials().withIdentity("[system]")))
                .withIsSystemTask(false))
            .withIdentity(new IdentityProperties().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
            .create();
    }

    /*
     * x-ms-original-file: 2025-03-01-preview/TasksCreate.json
     */
    /**
     * Sample code: Tasks_Create.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        tasksCreate(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.tasks()
            .define("mytTask")
            .withRegion("eastus")
            .withExistingRegistry("myResourceGroup", "myRegistry")
            .withTags(mapOf("testkey", "fakeTokenPlaceholder"))
            .withProperties(new TaskProperties().withStatus(TaskStatus.ENABLED)
                .withPlatform(new PlatformProperties().withOs(OS.LINUX).withArchitecture(Architecture.AMD64))
                .withAgentConfiguration(new AgentProperties().withCpu(2))
                .withStep(new DockerBuildStep().withContextPath("src")
                    .withImageNames(Arrays.asList("azurerest:testtag"))
                    .withIsPushEnabled(true)
                    .withNoCache(false)
                    .withDockerFilePath("src/DockerFile")
                    .withArguments(Arrays.asList(
                        new Argument().withName("mytestargument").withValue("mytestvalue").withIsSecret(false),
                        new Argument().withName("mysecrettestargument")
                            .withValue("mysecrettestvalue")
                            .withIsSecret(true))))
                .withTrigger(
                    new TriggerProperties()
                        .withTimerTriggers(
                            Arrays.asList(new TimerTrigger().withSchedule("30 9 * * 1-5").withName("myTimerTrigger")))
                        .withSourceTriggers(
                            Arrays.asList(new SourceTrigger()
                                .withSourceRepository(
                                    new SourceProperties().withSourceControlType(SourceControlType.GITHUB)
                                        .withRepositoryUrl("https://github.com/Azure/azure-rest-api-specs")
                                        .withBranch("master")
                                        .withSourceControlAuthProperties(new AuthInfo().withTokenType(TokenType.PAT)
                                            .withToken("fakeTokenPlaceholder")))
                                .withSourceTriggerEvents(Arrays.asList(SourceTriggerEvent.COMMIT))
                                .withName("mySourceTrigger")))
                        .withBaseImageTrigger(
                            new BaseImageTrigger().withBaseImageTriggerType(BaseImageTriggerType.RUNTIME)
                                .withUpdateTriggerEndpoint("https://user:pass@mycicd.webhook.com?token=foo")
                                .withUpdateTriggerPayloadType(UpdateTriggerPayloadType.TOKEN)
                                .withName("myBaseImageTrigger")))
                .withLogTemplate("acr/tasks:{{.Run.OS}}")
                .withIsSystemTask(false))
            .withIdentity(new IdentityProperties().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
            .create();
    }

    /*
     * x-ms-original-file: 2025-03-01-preview/ManagedIdentity/TasksCreate_WithSystemAndUserIdentities.json
     */
    /**
     * Sample code: Tasks_Create_WithSystemAndUserIdentities.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void tasksCreateWithSystemAndUserIdentities(
        com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.tasks()
            .define("mytTask")
            .withRegion("eastus")
            .withExistingRegistry("myResourceGroup", "myRegistry")
            .withTags(mapOf("testkey", "fakeTokenPlaceholder"))
            .withProperties(new TaskProperties().withStatus(TaskStatus.ENABLED)
                .withPlatform(new PlatformProperties().withOs(OS.LINUX).withArchitecture(Architecture.AMD64))
                .withAgentConfiguration(new AgentProperties().withCpu(2))
                .withStep(new DockerBuildStep().withContextPath("src")
                    .withImageNames(Arrays.asList("azurerest:testtag"))
                    .withIsPushEnabled(true)
                    .withNoCache(false)
                    .withDockerFilePath("src/DockerFile")
                    .withArguments(Arrays.asList(
                        new Argument().withName("mytestargument").withValue("mytestvalue").withIsSecret(false),
                        new Argument().withName("mysecrettestargument")
                            .withValue("mysecrettestvalue")
                            .withIsSecret(true))))
                .withTrigger(
                    new TriggerProperties()
                        .withTimerTriggers(
                            Arrays.asList(new TimerTrigger().withSchedule("30 9 * * 1-5").withName("myTimerTrigger")))
                        .withSourceTriggers(
                            Arrays.asList(new SourceTrigger()
                                .withSourceRepository(
                                    new SourceProperties().withSourceControlType(SourceControlType.GITHUB)
                                        .withRepositoryUrl("https://github.com/Azure/azure-rest-api-specs")
                                        .withBranch("master")
                                        .withSourceControlAuthProperties(new AuthInfo().withTokenType(TokenType.PAT)
                                            .withToken("fakeTokenPlaceholder")))
                                .withSourceTriggerEvents(Arrays.asList(SourceTriggerEvent.COMMIT))
                                .withName("mySourceTrigger")))
                        .withBaseImageTrigger(
                            new BaseImageTrigger().withBaseImageTriggerType(BaseImageTriggerType.RUNTIME)
                                .withUpdateTriggerEndpoint("https://user:pass@mycicd.webhook.com?token=foo")
                                .withUpdateTriggerPayloadType(UpdateTriggerPayloadType.DEFAULT)
                                .withName("myBaseImageTrigger")))
                .withIsSystemTask(false))
            .withIdentity(new IdentityProperties().withType(ResourceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/f9d7ebed-adbd-4cb4-b973-aaf82c136138/resourcegroups/myResourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identity2",
                    new UserIdentityProperties())))
            .create();
    }

    /*
     * x-ms-original-file: 2025-03-01-preview/ManagedIdentity/TasksCreate_WithUserIdentities.json
     */
    /**
     * Sample code: Tasks_Create_WithUserIdentities.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void tasksCreateWithUserIdentities(
        com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.tasks()
            .define("mytTask")
            .withRegion("eastus")
            .withExistingRegistry("myResourceGroup", "myRegistry")
            .withTags(mapOf("testkey", "fakeTokenPlaceholder"))
            .withProperties(new TaskProperties().withStatus(TaskStatus.ENABLED)
                .withPlatform(new PlatformProperties().withOs(OS.LINUX).withArchitecture(Architecture.AMD64))
                .withAgentConfiguration(new AgentProperties().withCpu(2))
                .withStep(new DockerBuildStep().withContextPath("src")
                    .withImageNames(Arrays.asList("azurerest:testtag"))
                    .withIsPushEnabled(true)
                    .withNoCache(false)
                    .withDockerFilePath("src/DockerFile")
                    .withArguments(Arrays.asList(
                        new Argument().withName("mytestargument").withValue("mytestvalue").withIsSecret(false),
                        new Argument().withName("mysecrettestargument")
                            .withValue("mysecrettestvalue")
                            .withIsSecret(true))))
                .withTrigger(
                    new TriggerProperties()
                        .withTimerTriggers(
                            Arrays.asList(new TimerTrigger().withSchedule("30 9 * * 1-5").withName("myTimerTrigger")))
                        .withSourceTriggers(
                            Arrays.asList(new SourceTrigger()
                                .withSourceRepository(
                                    new SourceProperties().withSourceControlType(SourceControlType.GITHUB)
                                        .withRepositoryUrl("https://github.com/Azure/azure-rest-api-specs")
                                        .withBranch("master")
                                        .withSourceControlAuthProperties(new AuthInfo().withTokenType(TokenType.PAT)
                                            .withToken("fakeTokenPlaceholder")))
                                .withSourceTriggerEvents(Arrays.asList(SourceTriggerEvent.COMMIT))
                                .withName("mySourceTrigger")))
                        .withBaseImageTrigger(
                            new BaseImageTrigger().withBaseImageTriggerType(BaseImageTriggerType.RUNTIME)
                                .withUpdateTriggerEndpoint("https://user:pass@mycicd.webhook.com?token=foo")
                                .withUpdateTriggerPayloadType(UpdateTriggerPayloadType.DEFAULT)
                                .withName("myBaseImageTrigger")))
                .withIsSystemTask(false))
            .withIdentity(new IdentityProperties().withType(ResourceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/f9d7ebed-adbd-4cb4-b973-aaf82c136138/resourcegroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identity1",
                    new UserIdentityProperties(),
                    "/subscriptions/f9d7ebed-adbd-4cb4-b973-aaf82c136138/resourcegroups/myResourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identity2",
                    new UserIdentityProperties())))
            .create();
    }

    /*
     * x-ms-original-file: 2025-03-01-preview/TasksCreate_QuickTask.json
     */
    /**
     * Sample code: Tasks_Create_QuickTask.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        tasksCreateQuickTask(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.tasks()
            .define("quicktask")
            .withRegion("eastus")
            .withExistingRegistry("myResourceGroup", "myRegistry")
            .withTags(mapOf("testkey", "fakeTokenPlaceholder"))
            .withProperties(new TaskProperties().withStatus(TaskStatus.ENABLED)
                .withLogTemplate("acr/tasks:{{.Run.OS}}")
                .withIsSystemTask(true))
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

### Tasks_Delete

```java
/**
 * Samples for Tasks Delete.
 */
public final class TasksDeleteSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/TasksDelete.json
     */
    /**
     * Sample code: Tasks_Delete.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        tasksDelete(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.tasks().deleteWithResponse("myResourceGroup", "myRegistry", "myTask", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-03-01-preview/TasksGet.json
     */
    /**
     * Sample code: Tasks_Get.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        tasksGet(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.tasks().getWithResponse("myResourceGroup", "myRegistry", "myTask", com.azure.core.util.Context.NONE);
    }
}
```

### Tasks_GetDetails

```java
/**
 * Samples for Tasks GetDetails.
 */
public final class TasksGetDetailsSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/TasksGetDetails.json
     */
    /**
     * Sample code: Tasks_GetDetails.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        tasksGetDetails(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.tasks()
            .getDetailsWithResponse("myResourceGroup", "myRegistry", "myTask", com.azure.core.util.Context.NONE);
    }
}
```

### Tasks_List

```java
/**
 * Samples for Tasks List.
 */
public final class TasksListSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/TasksList.json
     */
    /**
     * Sample code: Tasks_List.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        tasksList(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        manager.tasks().list("myResourceGroup", "myRegistry", com.azure.core.util.Context.NONE);
    }
}
```

### Tasks_Update

```java
import com.azure.resourcemanager.containerregistry.tasks.models.AgentProperties;
import com.azure.resourcemanager.containerregistry.tasks.models.AuthInfoUpdateParameters;
import com.azure.resourcemanager.containerregistry.tasks.models.Credentials;
import com.azure.resourcemanager.containerregistry.tasks.models.CustomRegistryCredentials;
import com.azure.resourcemanager.containerregistry.tasks.models.DockerBuildStepUpdateParameters;
import com.azure.resourcemanager.containerregistry.tasks.models.SecretObject;
import com.azure.resourcemanager.containerregistry.tasks.models.SecretObjectType;
import com.azure.resourcemanager.containerregistry.tasks.models.SourceRegistryCredentials;
import com.azure.resourcemanager.containerregistry.tasks.models.SourceTriggerEvent;
import com.azure.resourcemanager.containerregistry.tasks.models.SourceTriggerUpdateParameters;
import com.azure.resourcemanager.containerregistry.tasks.models.SourceUpdateParameters;
import com.azure.resourcemanager.containerregistry.tasks.models.Task;
import com.azure.resourcemanager.containerregistry.tasks.models.TaskStatus;
import com.azure.resourcemanager.containerregistry.tasks.models.TokenType;
import com.azure.resourcemanager.containerregistry.tasks.models.TriggerUpdateParameters;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Tasks Update.
 */
public final class TasksUpdateSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/ManagedIdentity/TasksUpdate_WithLoginIdentity.json
     */
    /**
     * Sample code: Tasks_Update_WithLoginIdentity.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void tasksUpdateWithLoginIdentity(
        com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        Task resource = manager.tasks()
            .getWithResponse("myResourceGroup", "myRegistry", "myTask", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("testkey", "fakeTokenPlaceholder"))
            .withStatus(TaskStatus.ENABLED)
            .withAgentConfiguration(new AgentProperties().withCpu(3))
            .withStep(new DockerBuildStepUpdateParameters().withImageNames(Arrays.asList("azurerest:testtag1"))
                .withDockerFilePath("src/DockerFile"))
            .withTrigger(
                new TriggerUpdateParameters().withSourceTriggers(Arrays.asList(new SourceTriggerUpdateParameters()
                    .withSourceRepository(new SourceUpdateParameters().withSourceControlAuthProperties(
                        new AuthInfoUpdateParameters().withTokenType(TokenType.PAT).withToken("fakeTokenPlaceholder")))
                    .withSourceTriggerEvents(Arrays.asList(SourceTriggerEvent.COMMIT))
                    .withName("mySourceTrigger"))))
            .withCredentials(
                new Credentials().withSourceRegistry(new SourceRegistryCredentials().withIdentity("[system]")))
            .apply();
    }

    /*
     * x-ms-original-file: 2025-03-01-preview/TasksUpdate_QuickTask.json
     */
    /**
     * Sample code: Tasks_Update_QuickTask.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        tasksUpdateQuickTask(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        Task resource = manager.tasks()
            .getWithResponse("myResourceGroup", "myRegistry", "quicktask", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("testkey", "fakeTokenPlaceholder"))
            .withStatus(TaskStatus.ENABLED)
            .withLogTemplate("acr/tasks:{{.Run.OS}}")
            .apply();
    }

    /*
     * x-ms-original-file: 2025-03-01-preview/ManagedIdentity/TasksUpdate_WithMSICustomCredentials.json
     */
    /**
     * Sample code: Tasks_Update_WithMSICustomCredentials.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void tasksUpdateWithMSICustomCredentials(
        com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        Task resource = manager.tasks()
            .getWithResponse("myResourceGroup", "myRegistry", "myTask", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("testkey", "fakeTokenPlaceholder"))
            .withStatus(TaskStatus.ENABLED)
            .withAgentConfiguration(new AgentProperties().withCpu(3))
            .withStep(new DockerBuildStepUpdateParameters().withImageNames(Arrays.asList("azurerest:testtag1"))
                .withDockerFilePath("src/DockerFile"))
            .withTrigger(
                new TriggerUpdateParameters().withSourceTriggers(Arrays.asList(new SourceTriggerUpdateParameters()
                    .withSourceRepository(new SourceUpdateParameters().withSourceControlAuthProperties(
                        new AuthInfoUpdateParameters().withTokenType(TokenType.PAT).withToken("fakeTokenPlaceholder")))
                    .withSourceTriggerEvents(Arrays.asList(SourceTriggerEvent.COMMIT))
                    .withName("mySourceTrigger"))))
            .withCredentials(new Credentials().withCustomRegistries(
                mapOf("myregistry.azurecr.io", new CustomRegistryCredentials().withIdentity("[system]"))))
            .apply();
    }

    /*
     * x-ms-original-file: 2025-03-01-preview/ManagedIdentity/TasksUpdate_WithKeyVaultCustomCredentials.json
     */
    /**
     * Sample code: Tasks_Update_WithKeyVaultCustomCredentials.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void tasksUpdateWithKeyVaultCustomCredentials(
        com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        Task resource = manager.tasks()
            .getWithResponse("myResourceGroup", "myRegistry", "myTask", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("testkey", "fakeTokenPlaceholder"))
            .withStatus(TaskStatus.ENABLED)
            .withAgentConfiguration(new AgentProperties().withCpu(3))
            .withStep(new DockerBuildStepUpdateParameters().withImageNames(Arrays.asList("azurerest:testtag1"))
                .withDockerFilePath("src/DockerFile"))
            .withTrigger(
                new TriggerUpdateParameters().withSourceTriggers(Arrays.asList(new SourceTriggerUpdateParameters()
                    .withSourceRepository(new SourceUpdateParameters().withSourceControlAuthProperties(
                        new AuthInfoUpdateParameters().withTokenType(TokenType.PAT).withToken("fakeTokenPlaceholder")))
                    .withSourceTriggerEvents(Arrays.asList(SourceTriggerEvent.COMMIT))
                    .withName("mySourceTrigger"))))
            .withCredentials(new Credentials().withCustomRegistries(mapOf("myregistry.azurecr.io",
                new CustomRegistryCredentials()
                    .withUserName(new SecretObject().withValue("https://myacbvault.vault.azure.net/secrets/username")
                        .withType(SecretObjectType.VAULTSECRET))
                    .withPassword(new SecretObject().withValue("https://myacbvault.vault.azure.net/secrets/password")
                        .withType(SecretObjectType.VAULTSECRET))
                    .withIdentity("[system]"))))
            .apply();
    }

    /*
     * x-ms-original-file: 2025-03-01-preview/TasksUpdate.json
     */
    /**
     * Sample code: Tasks_Update.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void
        tasksUpdate(com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        Task resource = manager.tasks()
            .getWithResponse("myResourceGroup", "myRegistry", "myTask", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("testkey", "fakeTokenPlaceholder"))
            .withStatus(TaskStatus.ENABLED)
            .withAgentConfiguration(new AgentProperties().withCpu(3))
            .withStep(new DockerBuildStepUpdateParameters().withImageNames(Arrays.asList("azurerest:testtag1"))
                .withDockerFilePath("src/DockerFile"))
            .withTrigger(
                new TriggerUpdateParameters().withSourceTriggers(Arrays.asList(new SourceTriggerUpdateParameters()
                    .withSourceRepository(new SourceUpdateParameters().withSourceControlAuthProperties(
                        new AuthInfoUpdateParameters().withTokenType(TokenType.PAT).withToken("fakeTokenPlaceholder")))
                    .withSourceTriggerEvents(Arrays.asList(SourceTriggerEvent.COMMIT))
                    .withName("mySourceTrigger"))))
            .withCredentials(new Credentials().withCustomRegistries(mapOf("myregistry.azurecr.io",
                new CustomRegistryCredentials()
                    .withUserName(new SecretObject().withValue("username").withType(SecretObjectType.OPAQUE))
                    .withPassword(new SecretObject().withValue("https://myacbvault.vault.azure.net/secrets/password")
                        .withType(SecretObjectType.VAULTSECRET))
                    .withIdentity("[system]"))))
            .withLogTemplate("acr/tasks:{{.Run.OS}}")
            .apply();
    }

    /*
     * x-ms-original-file: 2025-03-01-preview/TasksUpdate_WithOpaqueCustomCredentials.json
     */
    /**
     * Sample code: Tasks_Update_WithOpaqueCustomCredentials.
     * 
     * @param manager Entry point to ContainerRegistryTasksManager.
     */
    public static void tasksUpdateWithOpaqueCustomCredentials(
        com.azure.resourcemanager.containerregistry.tasks.ContainerRegistryTasksManager manager) {
        Task resource = manager.tasks()
            .getWithResponse("myResourceGroup", "myRegistry", "myTask", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("testkey", "fakeTokenPlaceholder"))
            .withStatus(TaskStatus.ENABLED)
            .withAgentConfiguration(new AgentProperties().withCpu(3))
            .withStep(new DockerBuildStepUpdateParameters().withImageNames(Arrays.asList("azurerest:testtag1"))
                .withDockerFilePath("src/DockerFile"))
            .withTrigger(
                new TriggerUpdateParameters().withSourceTriggers(Arrays.asList(new SourceTriggerUpdateParameters()
                    .withSourceRepository(new SourceUpdateParameters().withSourceControlAuthProperties(
                        new AuthInfoUpdateParameters().withTokenType(TokenType.PAT).withToken("fakeTokenPlaceholder")))
                    .withSourceTriggerEvents(Arrays.asList(SourceTriggerEvent.COMMIT))
                    .withName("mySourceTrigger"))))
            .withCredentials(new Credentials().withCustomRegistries(mapOf("myregistry.azurecr.io",
                new CustomRegistryCredentials()
                    .withUserName(new SecretObject().withValue("username").withType(SecretObjectType.OPAQUE))
                    .withPassword(new SecretObject().withValue("***").withType(SecretObjectType.OPAQUE)))))
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

