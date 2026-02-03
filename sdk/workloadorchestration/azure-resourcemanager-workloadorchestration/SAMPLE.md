# Code snippets and samples


## ConfigTemplateVersions

- [Get](#configtemplateversions_get)
- [ListByConfigTemplate](#configtemplateversions_listbyconfigtemplate)

## ConfigTemplates

- [CreateOrUpdate](#configtemplates_createorupdate)
- [CreateVersion](#configtemplates_createversion)
- [Delete](#configtemplates_delete)
- [GetByResourceGroup](#configtemplates_getbyresourcegroup)
- [List](#configtemplates_list)
- [ListByResourceGroup](#configtemplates_listbyresourcegroup)
- [RemoveVersion](#configtemplates_removeversion)
- [Update](#configtemplates_update)

## Contexts

- [CreateOrUpdate](#contexts_createorupdate)
- [Delete](#contexts_delete)
- [GetByResourceGroup](#contexts_getbyresourcegroup)
- [List](#contexts_list)
- [ListByResourceGroup](#contexts_listbyresourcegroup)
- [Update](#contexts_update)

## Diagnostics

- [CreateOrUpdate](#diagnostics_createorupdate)
- [Delete](#diagnostics_delete)
- [GetByResourceGroup](#diagnostics_getbyresourcegroup)
- [List](#diagnostics_list)
- [ListByResourceGroup](#diagnostics_listbyresourcegroup)
- [Update](#diagnostics_update)

## DynamicSchemaVersions

- [CreateOrUpdate](#dynamicschemaversions_createorupdate)
- [Delete](#dynamicschemaversions_delete)
- [Get](#dynamicschemaversions_get)
- [ListByDynamicSchema](#dynamicschemaversions_listbydynamicschema)
- [Update](#dynamicschemaversions_update)

## DynamicSchemas

- [CreateOrUpdate](#dynamicschemas_createorupdate)
- [Delete](#dynamicschemas_delete)
- [Get](#dynamicschemas_get)
- [ListBySchema](#dynamicschemas_listbyschema)
- [Update](#dynamicschemas_update)

## Executions

- [CreateOrUpdate](#executions_createorupdate)
- [Delete](#executions_delete)
- [Get](#executions_get)
- [ListByWorkflowVersion](#executions_listbyworkflowversion)
- [Update](#executions_update)

## InstanceHistories

- [Get](#instancehistories_get)
- [ListByInstance](#instancehistories_listbyinstance)

## Instances

- [CreateOrUpdate](#instances_createorupdate)
- [Delete](#instances_delete)
- [Get](#instances_get)
- [ListBySolution](#instances_listbysolution)
- [Update](#instances_update)

## Jobs

- [Get](#jobs_get)
- [ListByTarget](#jobs_listbytarget)

## SchemaReferences

- [Get](#schemareferences_get)
- [ListByResourceGroup](#schemareferences_listbyresourcegroup)

## SchemaVersions

- [CreateOrUpdate](#schemaversions_createorupdate)
- [Delete](#schemaversions_delete)
- [Get](#schemaversions_get)
- [ListBySchema](#schemaversions_listbyschema)
- [Update](#schemaversions_update)

## Schemas

- [CreateOrUpdate](#schemas_createorupdate)
- [CreateVersion](#schemas_createversion)
- [Delete](#schemas_delete)
- [GetByResourceGroup](#schemas_getbyresourcegroup)
- [List](#schemas_list)
- [ListByResourceGroup](#schemas_listbyresourcegroup)
- [RemoveVersion](#schemas_removeversion)
- [Update](#schemas_update)

## SiteReferences

- [CreateOrUpdate](#sitereferences_createorupdate)
- [Delete](#sitereferences_delete)
- [Get](#sitereferences_get)
- [ListByContext](#sitereferences_listbycontext)
- [Update](#sitereferences_update)

## SolutionTemplateVersions

- [BulkDeploySolution](#solutiontemplateversions_bulkdeploysolution)
- [BulkPublishSolution](#solutiontemplateversions_bulkpublishsolution)
- [Get](#solutiontemplateversions_get)
- [ListBySolutionTemplate](#solutiontemplateversions_listbysolutiontemplate)

## SolutionTemplates

- [CreateOrUpdate](#solutiontemplates_createorupdate)
- [CreateVersion](#solutiontemplates_createversion)
- [Delete](#solutiontemplates_delete)
- [GetByResourceGroup](#solutiontemplates_getbyresourcegroup)
- [List](#solutiontemplates_list)
- [ListByResourceGroup](#solutiontemplates_listbyresourcegroup)
- [RemoveVersion](#solutiontemplates_removeversion)
- [Update](#solutiontemplates_update)

## SolutionVersions

- [CreateOrUpdate](#solutionversions_createorupdate)
- [Delete](#solutionversions_delete)
- [Get](#solutionversions_get)
- [ListBySolution](#solutionversions_listbysolution)
- [Update](#solutionversions_update)

## Solutions

- [CreateOrUpdate](#solutions_createorupdate)
- [Delete](#solutions_delete)
- [Get](#solutions_get)
- [ListByTarget](#solutions_listbytarget)
- [Update](#solutions_update)

## Targets

- [CreateOrUpdate](#targets_createorupdate)
- [Delete](#targets_delete)
- [GetByResourceGroup](#targets_getbyresourcegroup)
- [InstallSolution](#targets_installsolution)
- [List](#targets_list)
- [ListByResourceGroup](#targets_listbyresourcegroup)
- [PublishSolutionVersion](#targets_publishsolutionversion)
- [RemoveRevision](#targets_removerevision)
- [ResolveConfiguration](#targets_resolveconfiguration)
- [ReviewSolutionVersion](#targets_reviewsolutionversion)
- [UninstallSolution](#targets_uninstallsolution)
- [Update](#targets_update)
- [UpdateExternalValidationStatus](#targets_updateexternalvalidationstatus)

## WorkflowVersions

- [CreateOrUpdate](#workflowversions_createorupdate)
- [Delete](#workflowversions_delete)
- [Get](#workflowversions_get)
- [ListByWorkflow](#workflowversions_listbyworkflow)
- [Update](#workflowversions_update)

## Workflows

- [CreateOrUpdate](#workflows_createorupdate)
- [Delete](#workflows_delete)
- [Get](#workflows_get)
- [ListByContext](#workflows_listbycontext)
- [Update](#workflows_update)
### ConfigTemplateVersions_Get

```java
/**
 * Samples for ConfigTemplateVersions Get.
 */
public final class ConfigTemplateVersionsGetSamples {
    /*
     * x-ms-original-file: 2025-06-01/ConfigTemplateVersions_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ConfigTemplateVersions_Get_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void configTemplateVersionsGetMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.configTemplateVersions()
            .getWithResponse("rgconfigurationmanager", "testname", "1.0.0", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigTemplateVersions_ListByConfigTemplate

```java
/**
 * Samples for ConfigTemplateVersions ListByConfigTemplate.
 */
public final class ConfigTemplateVersionsListByConfigTemplateSamples {
    /*
     * x-ms-original-file: 2025-06-01/ConfigTemplateVersions_ListByConfigTemplate_MaximumSet_Gen.json
     */
    /**
     * Sample code: ConfigTemplateVersions_ListByConfigTemplate_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void configTemplateVersionsListByConfigTemplateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.configTemplateVersions()
            .listByConfigTemplate("rgconfigurationmanager", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigTemplates_CreateOrUpdate

```java
import com.azure.resourcemanager.workloadorchestration.models.ConfigTemplateProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ConfigTemplates CreateOrUpdate.
 */
public final class ConfigTemplatesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/ConfigTemplates_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: ConfigTemplates_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void configTemplatesCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.configTemplates()
            .define("testname")
            .withRegion("egqjo")
            .withExistingResourceGroup("rgconfigurationmanager")
            .withTags(mapOf("key6936", "fakeTokenPlaceholder"))
            .withProperties(new ConfigTemplateProperties().withDescription("ccdyggozwmhyvemlcwlsnhijwg"))
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

### ConfigTemplates_CreateVersion

```java
import com.azure.resourcemanager.workloadorchestration.fluent.models.ConfigTemplateVersionInner;
import com.azure.resourcemanager.workloadorchestration.fluent.models.ConfigTemplateVersionWithUpdateTypeInner;
import com.azure.resourcemanager.workloadorchestration.models.ConfigTemplateVersionProperties;
import com.azure.resourcemanager.workloadorchestration.models.UpdateType;

/**
 * Samples for ConfigTemplates CreateVersion.
 */
public final class ConfigTemplatesCreateVersionSamples {
    /*
     * x-ms-original-file: 2025-06-01/ConfigTemplates_CreateVersion_MaximumSet_Gen.json
     */
    /**
     * Sample code: ConfigTemplates_CreateVersion_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void configTemplatesCreateVersionMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.configTemplates()
            .createVersion("rgconfigurationmanager", "testname",
                new ConfigTemplateVersionWithUpdateTypeInner().withUpdateType(UpdateType.MAJOR)
                    .withVersion("1.0.0")
                    .withConfigTemplateVersion(new ConfigTemplateVersionInner()
                        .withProperties(new ConfigTemplateVersionProperties().withConfigurations("rgricnhvcbqykc"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### ConfigTemplates_Delete

```java
/**
 * Samples for ConfigTemplates Delete.
 */
public final class ConfigTemplatesDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01/ConfigTemplates_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: ConfigTemplates_Delete_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void configTemplatesDeleteMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.configTemplates().delete("rgconfigurationmanager", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigTemplates_GetByResourceGroup

```java
/**
 * Samples for ConfigTemplates GetByResourceGroup.
 */
public final class ConfigTemplatesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01/ConfigTemplates_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ConfigTemplates_Get_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void configTemplatesGetMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.configTemplates()
            .getByResourceGroupWithResponse("rgconfigurationmanager", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigTemplates_List

```java
/**
 * Samples for ConfigTemplates List.
 */
public final class ConfigTemplatesListSamples {
    /*
     * x-ms-original-file: 2025-06-01/ConfigTemplates_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: ConfigTemplates_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void configTemplatesListBySubscriptionMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.configTemplates().list(com.azure.core.util.Context.NONE);
    }
}
```

### ConfigTemplates_ListByResourceGroup

```java
/**
 * Samples for ConfigTemplates ListByResourceGroup.
 */
public final class ConfigTemplatesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01/ConfigTemplates_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: ConfigTemplates_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void configTemplatesListByResourceGroupMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.configTemplates().listByResourceGroup("rgconfigurationmanager", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigTemplates_RemoveVersion

```java
import com.azure.resourcemanager.workloadorchestration.models.VersionParameter;

/**
 * Samples for ConfigTemplates RemoveVersion.
 */
public final class ConfigTemplatesRemoveVersionSamples {
    /*
     * x-ms-original-file: 2025-06-01/ConfigTemplates_RemoveVersion_MaximumSet_Gen.json
     */
    /**
     * Sample code: ConfigTemplates_RemoveVersion_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void configTemplatesRemoveVersionMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.configTemplates()
            .removeVersionWithResponse("rgconfigurationmanager", "testname",
                new VersionParameter().withVersion("ghtvdzgmzncaifrnuumg"), com.azure.core.util.Context.NONE);
    }
}
```

### ConfigTemplates_Update

```java
import com.azure.resourcemanager.workloadorchestration.models.ConfigTemplate;
import com.azure.resourcemanager.workloadorchestration.models.ConfigTemplateProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ConfigTemplates Update.
 */
public final class ConfigTemplatesUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/ConfigTemplates_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: ConfigTemplates_Update_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void configTemplatesUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        ConfigTemplate resource = manager.configTemplates()
            .getByResourceGroupWithResponse("rgconfigurationmanager", "testname", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key7701", "fakeTokenPlaceholder"))
            .withProperties(new ConfigTemplateProperties().withDescription("cavjiqnrbzsvedicrixhwnfj"))
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

### Contexts_CreateOrUpdate

```java
import com.azure.resourcemanager.workloadorchestration.models.Capability;
import com.azure.resourcemanager.workloadorchestration.models.ContextProperties;
import com.azure.resourcemanager.workloadorchestration.models.Hierarchy;
import com.azure.resourcemanager.workloadorchestration.models.ResourceState;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Contexts CreateOrUpdate.
 */
public final class ContextsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/Contexts_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Contexts_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void contextsCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.contexts()
            .define("testname")
            .withRegion("pkquwbplcp")
            .withExistingResourceGroup("rgconfigurationmanager")
            .withTags(mapOf("key3046", "fakeTokenPlaceholder"))
            .withProperties(new ContextProperties()
                .withCapabilities(Arrays.asList(new Capability().withName("tpylinjcmlnycfpofpxjtqmt")
                    .withDescription("banbenutsngwytoqh")
                    .withState(ResourceState.ACTIVE)))
                .withHierarchies(Arrays.asList(new Hierarchy().withName("upqe").withDescription("vg"))))
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

### Contexts_Delete

```java
/**
 * Samples for Contexts Delete.
 */
public final class ContextsDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01/Contexts_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Contexts_Delete_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void
        contextsDeleteMaximumSet(com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.contexts().delete("rgconfigurationmanager", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### Contexts_GetByResourceGroup

```java
/**
 * Samples for Contexts GetByResourceGroup.
 */
public final class ContextsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01/Contexts_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Contexts_Get_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void
        contextsGetMaximumSet(com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.contexts()
            .getByResourceGroupWithResponse("rgconfigurationmanager", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### Contexts_List

```java
/**
 * Samples for Contexts List.
 */
public final class ContextsListSamples {
    /*
     * x-ms-original-file: 2025-06-01/Contexts_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: Contexts_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void contextsListBySubscriptionMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.contexts().list(com.azure.core.util.Context.NONE);
    }
}
```

### Contexts_ListByResourceGroup

```java
/**
 * Samples for Contexts ListByResourceGroup.
 */
public final class ContextsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01/Contexts_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Contexts_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void contextsListByResourceGroupMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.contexts().listByResourceGroup("rgconfigurationmanager", com.azure.core.util.Context.NONE);
    }
}
```

### Contexts_Update

```java
import com.azure.resourcemanager.workloadorchestration.models.Capability;
import com.azure.resourcemanager.workloadorchestration.models.ContextModel;
import com.azure.resourcemanager.workloadorchestration.models.ContextProperties;
import com.azure.resourcemanager.workloadorchestration.models.Hierarchy;
import com.azure.resourcemanager.workloadorchestration.models.ResourceState;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Contexts Update.
 */
public final class ContextsUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/Contexts_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Contexts_Update_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void
        contextsUpdateMaximumSet(com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        ContextModel resource = manager.contexts()
            .getByResourceGroupWithResponse("rgconfigurationmanager", "testname", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key9545", "fakeTokenPlaceholder"))
            .withProperties(new ContextProperties()
                .withCapabilities(Arrays.asList(new Capability().withName("tpylinjcmlnycfpofpxjtqmt")
                    .withDescription("banbenutsngwytoqh")
                    .withState(ResourceState.ACTIVE)))
                .withHierarchies(Arrays.asList(new Hierarchy().withName("upqe").withDescription("vg"))))
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

### Diagnostics_CreateOrUpdate

```java
import com.azure.resourcemanager.workloadorchestration.models.DiagnosticProperties;
import com.azure.resourcemanager.workloadorchestration.models.ExtendedLocation;
import com.azure.resourcemanager.workloadorchestration.models.ExtendedLocationType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Diagnostics CreateOrUpdate.
 */
public final class DiagnosticsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/Diagnostics_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Diagnostics_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void diagnosticsCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.diagnostics()
            .define("testname")
            .withRegion("ouwfvnokjvivmjzqpupwrbsmls")
            .withExistingResourceGroup("rgconfigurationmanager")
            .withTags(mapOf("key4304", "fakeTokenPlaceholder"))
            .withProperties(new DiagnosticProperties())
            .withExtendedLocation(
                new ExtendedLocation().withName("szjrwimeqyiue").withType(ExtendedLocationType.EDGE_ZONE))
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

### Diagnostics_Delete

```java
/**
 * Samples for Diagnostics Delete.
 */
public final class DiagnosticsDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01/Diagnostics_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Diagnostics_Delete_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void diagnosticsDeleteMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.diagnostics().delete("rgconfigurationmanager", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### Diagnostics_GetByResourceGroup

```java
/**
 * Samples for Diagnostics GetByResourceGroup.
 */
public final class DiagnosticsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01/Diagnostics_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Diagnostics_Get_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void
        diagnosticsGetMaximumSet(com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.diagnostics()
            .getByResourceGroupWithResponse("rgconfigurationmanager", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### Diagnostics_List

```java
/**
 * Samples for Diagnostics List.
 */
public final class DiagnosticsListSamples {
    /*
     * x-ms-original-file: 2025-06-01/Diagnostics_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: Diagnostics_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void diagnosticsListBySubscriptionMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.diagnostics().list(com.azure.core.util.Context.NONE);
    }
}
```

### Diagnostics_ListByResourceGroup

```java
/**
 * Samples for Diagnostics ListByResourceGroup.
 */
public final class DiagnosticsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01/Diagnostics_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Diagnostics_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void diagnosticsListByResourceGroupMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.diagnostics().listByResourceGroup("rgconfigurationmanager", com.azure.core.util.Context.NONE);
    }
}
```

### Diagnostics_Update

```java
import com.azure.resourcemanager.workloadorchestration.models.Diagnostic;
import com.azure.resourcemanager.workloadorchestration.models.DiagnosticProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Diagnostics Update.
 */
public final class DiagnosticsUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/Diagnostics_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Diagnostics_Update_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void diagnosticsUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        Diagnostic resource = manager.diagnostics()
            .getByResourceGroupWithResponse("rgconfigurationmanager", "testname", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key1922", "fakeTokenPlaceholder"))
            .withProperties(new DiagnosticProperties())
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

### DynamicSchemaVersions_CreateOrUpdate

```java
import com.azure.resourcemanager.workloadorchestration.models.SchemaVersionProperties;

/**
 * Samples for DynamicSchemaVersions CreateOrUpdate.
 */
public final class DynamicSchemaVersionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/DynamicSchemaVersions_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: DynamicSchemaVersions_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void dynamicSchemaVersionsCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.dynamicSchemaVersions()
            .define("1.0.0")
            .withExistingDynamicSchema("rgconfigurationmanager", "testname", "testname")
            .withProperties(new SchemaVersionProperties().withValue("uiaqdwsi"))
            .create();
    }
}
```

### DynamicSchemaVersions_Delete

```java
/**
 * Samples for DynamicSchemaVersions Delete.
 */
public final class DynamicSchemaVersionsDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01/DynamicSchemaVersions_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: DynamicSchemaVersions_Delete_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void dynamicSchemaVersionsDeleteMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.dynamicSchemaVersions()
            .delete("rgconfigurationmanager", "testname", "testname", "1.0.0", com.azure.core.util.Context.NONE);
    }
}
```

### DynamicSchemaVersions_Get

```java
/**
 * Samples for DynamicSchemaVersions Get.
 */
public final class DynamicSchemaVersionsGetSamples {
    /*
     * x-ms-original-file: 2025-06-01/DynamicSchemaVersions_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: DynamicSchemaVersions_Get_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void dynamicSchemaVersionsGetMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.dynamicSchemaVersions()
            .getWithResponse("rgconfigurationmanager", "testname", "testname", "1.0.0",
                com.azure.core.util.Context.NONE);
    }
}
```

### DynamicSchemaVersions_ListByDynamicSchema

```java
/**
 * Samples for DynamicSchemaVersions ListByDynamicSchema.
 */
public final class DynamicSchemaVersionsListByDynamicSchemaSamples {
    /*
     * x-ms-original-file: 2025-06-01/DynamicSchemaVersions_ListByDynamicSchema_MaximumSet_Gen.json
     */
    /**
     * Sample code: DynamicSchemaVersions_ListByDynamicSchema_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void dynamicSchemaVersionsListByDynamicSchemaMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.dynamicSchemaVersions()
            .listByDynamicSchema("rgconfigurationmanager", "testname", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### DynamicSchemaVersions_Update

```java
import com.azure.resourcemanager.workloadorchestration.models.DynamicSchemaVersion;
import com.azure.resourcemanager.workloadorchestration.models.SchemaVersionProperties;

/**
 * Samples for DynamicSchemaVersions Update.
 */
public final class DynamicSchemaVersionsUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/DynamicSchemaVersions_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: DynamicSchemaVersions_Update_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void dynamicSchemaVersionsUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        DynamicSchemaVersion resource = manager.dynamicSchemaVersions()
            .getWithResponse("rgconfigurationmanager", "testname", "testname", "1.0.0",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withProperties(new SchemaVersionProperties().withValue("muezi")).apply();
    }
}
```

### DynamicSchemas_CreateOrUpdate

```java
import com.azure.resourcemanager.workloadorchestration.models.DynamicSchemaProperties;

/**
 * Samples for DynamicSchemas CreateOrUpdate.
 */
public final class DynamicSchemasCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/DynamicSchemas_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: DynamicSchemas_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void dynamicSchemasCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.dynamicSchemas()
            .define("testname")
            .withExistingSchema("rgconfigurationmanager", "testname")
            .withProperties(new DynamicSchemaProperties())
            .create();
    }
}
```

### DynamicSchemas_Delete

```java
/**
 * Samples for DynamicSchemas Delete.
 */
public final class DynamicSchemasDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01/DynamicSchemas_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: DynamicSchemas_Delete_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void dynamicSchemasDeleteMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.dynamicSchemas()
            .delete("rgconfigurationmanager", "testname", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### DynamicSchemas_Get

```java
/**
 * Samples for DynamicSchemas Get.
 */
public final class DynamicSchemasGetSamples {
    /*
     * x-ms-original-file: 2025-06-01/DynamicSchemas_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: DynamicSchemas_Get_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void dynamicSchemasGetMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.dynamicSchemas()
            .getWithResponse("rgconfigurationmanager", "testname", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### DynamicSchemas_ListBySchema

```java
/**
 * Samples for DynamicSchemas ListBySchema.
 */
public final class DynamicSchemasListBySchemaSamples {
    /*
     * x-ms-original-file: 2025-06-01/DynamicSchemas_ListBySchema_MaximumSet_Gen.json
     */
    /**
     * Sample code: DynamicSchemas_ListBySchema_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void dynamicSchemasListBySchemaMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.dynamicSchemas().listBySchema("rgconfigurationmanager", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### DynamicSchemas_Update

```java
import com.azure.resourcemanager.workloadorchestration.models.DynamicSchema;
import com.azure.resourcemanager.workloadorchestration.models.DynamicSchemaProperties;

/**
 * Samples for DynamicSchemas Update.
 */
public final class DynamicSchemasUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/DynamicSchemas_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: DynamicSchemas_Update_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void dynamicSchemasUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        DynamicSchema resource = manager.dynamicSchemas()
            .getWithResponse("rgconfigurationmanager", "testname", "testname", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withProperties(new DynamicSchemaProperties()).apply();
    }
}
```

### Executions_CreateOrUpdate

```java
import com.azure.resourcemanager.workloadorchestration.models.ExecutionProperties;
import com.azure.resourcemanager.workloadorchestration.models.ExtendedLocation;
import com.azure.resourcemanager.workloadorchestration.models.ExtendedLocationType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Executions CreateOrUpdate.
 */
public final class ExecutionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/Executions_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Executions_CreateOrUpdate_MaximumSet - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void executionsCreateOrUpdateMaximumSetGeneratedByMaximumSetRule(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.executions()
            .define("abcde")
            .withExistingVersion("rgconfigurationmanager", "abcde", "abcde", "abcde")
            .withProperties(
                new ExecutionProperties().withWorkflowVersionId("souenlqwltljsojdcbpc").withSpecification(mapOf()))
            .withExtendedLocation(new ExtendedLocation().withName("ugf").withType(ExtendedLocationType.EDGE_ZONE))
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

### Executions_Delete

```java
/**
 * Samples for Executions Delete.
 */
public final class ExecutionsDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01/Executions_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Executions_Delete_MaximumSet - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void executionsDeleteMaximumSetGeneratedByMaximumSetRule(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.executions()
            .delete("rgconfigurationmanager", "abcde", "abcde", "abcde", "abcde", com.azure.core.util.Context.NONE);
    }
}
```

### Executions_Get

```java
/**
 * Samples for Executions Get.
 */
public final class ExecutionsGetSamples {
    /*
     * x-ms-original-file: 2025-06-01/Executions_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Executions_Get_MaximumSet - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void executionsGetMaximumSetGeneratedByMaximumSetRule(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.executions()
            .getWithResponse("rgconfigurationmanager", "abcde", "abcde", "abcde", "abcde",
                com.azure.core.util.Context.NONE);
    }
}
```

### Executions_ListByWorkflowVersion

```java
/**
 * Samples for Executions ListByWorkflowVersion.
 */
public final class ExecutionsListByWorkflowVersionSamples {
    /*
     * x-ms-original-file: 2025-06-01/Executions_ListByWorkflowVersion_MaximumSet_Gen.json
     */
    /**
     * Sample code: Executions_ListByWorkflowVersion_MaximumSet - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void executionsListByWorkflowVersionMaximumSetGeneratedByMaximumSetRule(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.executions()
            .listByWorkflowVersion("rgconfigurationmanager", "abcde", "abcde", "abcde",
                com.azure.core.util.Context.NONE);
    }
}
```

### Executions_Update

```java
import com.azure.resourcemanager.workloadorchestration.models.Execution;
import com.azure.resourcemanager.workloadorchestration.models.ExecutionProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Executions Update.
 */
public final class ExecutionsUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/Executions_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Executions_Update_MaximumSet - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void executionsUpdateMaximumSetGeneratedByMaximumSetRule(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        Execution resource = manager.executions()
            .getWithResponse("rgconfigurationmanager", "abcde", "abcde", "abcde", "abcde",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(
                new ExecutionProperties().withWorkflowVersionId("xjsxzbfltzvbuvn").withSpecification(mapOf()))
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

### InstanceHistories_Get

```java
/**
 * Samples for InstanceHistories Get.
 */
public final class InstanceHistoriesGetSamples {
    /*
     * x-ms-original-file: 2025-06-01/InstanceHistories_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: InstanceHistories_Get_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void instanceHistoriesGetMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.instanceHistories()
            .getWithResponse("rgconfigurationmanager", "testname", "testname", "testname", "testname",
                com.azure.core.util.Context.NONE);
    }
}
```

### InstanceHistories_ListByInstance

```java
/**
 * Samples for InstanceHistories ListByInstance.
 */
public final class InstanceHistoriesListByInstanceSamples {
    /*
     * x-ms-original-file: 2025-06-01/InstanceHistories_ListByInstance_MaximumSet_Gen.json
     */
    /**
     * Sample code: InstanceHistories_ListByInstance_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void instanceHistoriesListByInstanceMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.instanceHistories()
            .listByInstance("rgconfigurationmanager", "testname", "testname", "testname",
                com.azure.core.util.Context.NONE);
    }
}
```

### Instances_CreateOrUpdate

```java
import com.azure.resourcemanager.workloadorchestration.models.ActiveState;
import com.azure.resourcemanager.workloadorchestration.models.ExtendedLocation;
import com.azure.resourcemanager.workloadorchestration.models.ExtendedLocationType;
import com.azure.resourcemanager.workloadorchestration.models.InstanceProperties;
import com.azure.resourcemanager.workloadorchestration.models.ReconciliationPolicyProperties;
import com.azure.resourcemanager.workloadorchestration.models.ReconciliationState;

/**
 * Samples for Instances CreateOrUpdate.
 */
public final class InstancesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/Instances_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Instances_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void instancesCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.instances()
            .define("testname")
            .withExistingSolution("rgconfigurationmanager", "testname", "testname")
            .withProperties(new InstanceProperties().withSolutionVersionId("acpddbkfclsgxg")
                .withTargetId("eguutiftuxrsavvckjrv")
                .withActiveState(ActiveState.ACTIVE)
                .withReconciliationPolicy(new ReconciliationPolicyProperties().withState(ReconciliationState.INACTIVE)
                    .withInterval("szucgzdbydcowvhprhx"))
                .withSolutionScope("testname"))
            .withExtendedLocation(
                new ExtendedLocation().withName("szjrwimeqyiue").withType(ExtendedLocationType.EDGE_ZONE))
            .create();
    }
}
```

### Instances_Delete

```java
/**
 * Samples for Instances Delete.
 */
public final class InstancesDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01/Instances_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Instances_Delete_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void instancesDeleteMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.instances()
            .delete("rgconfigurationmanager", "testname", "testname", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### Instances_Get

```java
/**
 * Samples for Instances Get.
 */
public final class InstancesGetSamples {
    /*
     * x-ms-original-file: 2025-06-01/Instances_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Instances_Get_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void
        instancesGetMaximumSet(com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.instances()
            .getWithResponse("rgconfigurationmanager", "testname", "testname", "testname",
                com.azure.core.util.Context.NONE);
    }
}
```

### Instances_ListBySolution

```java
/**
 * Samples for Instances ListBySolution.
 */
public final class InstancesListBySolutionSamples {
    /*
     * x-ms-original-file: 2025-06-01/Instances_ListBySolution_MaximumSet_Gen.json
     */
    /**
     * Sample code: Instances_ListBySolution_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void instancesListBySolutionMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.instances()
            .listBySolution("rgconfigurationmanager", "testname", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### Instances_Update

```java
import com.azure.resourcemanager.workloadorchestration.models.ActiveState;
import com.azure.resourcemanager.workloadorchestration.models.Instance;
import com.azure.resourcemanager.workloadorchestration.models.InstanceProperties;
import com.azure.resourcemanager.workloadorchestration.models.ReconciliationPolicyProperties;
import com.azure.resourcemanager.workloadorchestration.models.ReconciliationState;

/**
 * Samples for Instances Update.
 */
public final class InstancesUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/Instances_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Instances_Update_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void instancesUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        Instance resource = manager.instances()
            .getWithResponse("rgconfigurationmanager", "testname", "testname", "testname",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new InstanceProperties().withSolutionVersionId("vrpzlamkvanqibtjarpxit")
                .withTargetId("tqkdvc")
                .withActiveState(ActiveState.ACTIVE)
                .withReconciliationPolicy(new ReconciliationPolicyProperties().withState(ReconciliationState.INACTIVE)
                    .withInterval("cmzlrjwnlshnkgv"))
                .withSolutionScope("testname"))
            .apply();
    }
}
```

### Jobs_Get

```java
/**
 * Samples for Jobs Get.
 */
public final class JobsGetSamples {
    /*
     * x-ms-original-file: 2025-06-01/Jobs_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Jobs_Get_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void
        jobsGetMaximumSet(com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.jobs().getWithResponse("gt", "jobsName", com.azure.core.util.Context.NONE);
    }
}
```

### Jobs_ListByTarget

```java
/**
 * Samples for Jobs ListByTarget.
 */
public final class JobsListByTargetSamples {
    /*
     * x-ms-original-file: 2025-06-01/Jobs_ListByTarget_MaximumSet_Gen.json
     */
    /**
     * Sample code: Jobs_ListByTarget_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void jobsListByTargetMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.jobs().listByTarget("gt", com.azure.core.util.Context.NONE);
    }
}
```

### SchemaReferences_Get

```java
/**
 * Samples for SchemaReferences Get.
 */
public final class SchemaReferencesGetSamples {
    /*
     * x-ms-original-file: 2025-06-01/SchemaReferences_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: SchemaReferences_Get_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void schemaReferencesGetMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.schemaReferences().getWithResponse("jdvtghygpz", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### SchemaReferences_ListByResourceGroup

```java
/**
 * Samples for SchemaReferences ListByResourceGroup.
 */
public final class SchemaReferencesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01/SchemaReferences_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: SchemaReferences_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void schemaReferencesListByResourceGroupMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.schemaReferences().listByResourceGroup("jdvtghygpz", com.azure.core.util.Context.NONE);
    }
}
```

### SchemaVersions_CreateOrUpdate

```java
import com.azure.resourcemanager.workloadorchestration.models.SchemaVersionProperties;

/**
 * Samples for SchemaVersions CreateOrUpdate.
 */
public final class SchemaVersionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/SchemaVersions_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: SchemaVersions_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void schemaVersionsCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.schemaVersions()
            .define("1.0.0")
            .withExistingSchema("rgconfigurationmanager", "testname")
            .withProperties(new SchemaVersionProperties().withValue("uiaqdwsi"))
            .create();
    }
}
```

### SchemaVersions_Delete

```java
/**
 * Samples for SchemaVersions Delete.
 */
public final class SchemaVersionsDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01/SchemaVersions_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: SchemaVersions_Delete_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void schemaVersionsDeleteMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.schemaVersions()
            .delete("rgconfigurationmanager", "testname", "1.0.0", com.azure.core.util.Context.NONE);
    }
}
```

### SchemaVersions_Get

```java
/**
 * Samples for SchemaVersions Get.
 */
public final class SchemaVersionsGetSamples {
    /*
     * x-ms-original-file: 2025-06-01/SchemaVersions_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: SchemaVersions_Get_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void schemaVersionsGetMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.schemaVersions()
            .getWithResponse("rgconfigurationmanager", "testname", "1.0.0", com.azure.core.util.Context.NONE);
    }
}
```

### SchemaVersions_ListBySchema

```java
/**
 * Samples for SchemaVersions ListBySchema.
 */
public final class SchemaVersionsListBySchemaSamples {
    /*
     * x-ms-original-file: 2025-06-01/SchemaVersions_ListBySchema_MaximumSet_Gen.json
     */
    /**
     * Sample code: SchemaVersions_ListBySchema_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void schemaVersionsListBySchemaMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.schemaVersions().listBySchema("rgconfigurationmanager", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### SchemaVersions_Update

```java
import com.azure.resourcemanager.workloadorchestration.models.SchemaVersion;
import com.azure.resourcemanager.workloadorchestration.models.SchemaVersionProperties;

/**
 * Samples for SchemaVersions Update.
 */
public final class SchemaVersionsUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/SchemaVersions_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: SchemaVersions_Update_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void schemaVersionsUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        SchemaVersion resource = manager.schemaVersions()
            .getWithResponse("rgconfigurationmanager", "testname", "1.0.0", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withProperties(new SchemaVersionProperties().withValue("muezi")).apply();
    }
}
```

### Schemas_CreateOrUpdate

```java
import com.azure.resourcemanager.workloadorchestration.models.SchemaProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Schemas CreateOrUpdate.
 */
public final class SchemasCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/Schemas_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Schemas_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void schemasCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.schemas()
            .define("testname")
            .withRegion("alvi")
            .withExistingResourceGroup("rgconfigurationmanager")
            .withTags(mapOf("key7017", "fakeTokenPlaceholder"))
            .withProperties(new SchemaProperties())
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

### Schemas_CreateVersion

```java
import com.azure.resourcemanager.workloadorchestration.fluent.models.SchemaVersionInner;
import com.azure.resourcemanager.workloadorchestration.fluent.models.SchemaVersionWithUpdateTypeInner;
import com.azure.resourcemanager.workloadorchestration.models.SchemaVersionProperties;
import com.azure.resourcemanager.workloadorchestration.models.UpdateType;

/**
 * Samples for Schemas CreateVersion.
 */
public final class SchemasCreateVersionSamples {
    /*
     * x-ms-original-file: 2025-06-01/Schemas_CreateVersion_MaximumSet_Gen.json
     */
    /**
     * Sample code: Schemas_CreateVersion_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void schemasCreateVersionMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.schemas()
            .createVersion("rgconfigurationmanager", "testname",
                new SchemaVersionWithUpdateTypeInner().withUpdateType(UpdateType.MAJOR)
                    .withVersion("1.0.0")
                    .withSchemaVersion(
                        new SchemaVersionInner().withProperties(new SchemaVersionProperties().withValue("uiaqdwsi"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Schemas_Delete

```java
/**
 * Samples for Schemas Delete.
 */
public final class SchemasDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01/Schemas_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Schemas_Delete_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void
        schemasDeleteMaximumSet(com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.schemas().delete("rgconfigurationmanager", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### Schemas_GetByResourceGroup

```java
/**
 * Samples for Schemas GetByResourceGroup.
 */
public final class SchemasGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01/Schemas_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Schemas_Get_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void
        schemasGetMaximumSet(com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.schemas()
            .getByResourceGroupWithResponse("rgconfigurationmanager", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### Schemas_List

```java
/**
 * Samples for Schemas List.
 */
public final class SchemasListSamples {
    /*
     * x-ms-original-file: 2025-06-01/Schemas_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: Schemas_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void schemasListBySubscriptionMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.schemas().list(com.azure.core.util.Context.NONE);
    }
}
```

### Schemas_ListByResourceGroup

```java
/**
 * Samples for Schemas ListByResourceGroup.
 */
public final class SchemasListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01/Schemas_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Schemas_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void schemasListByResourceGroupMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.schemas().listByResourceGroup("rgconfigurationmanager", com.azure.core.util.Context.NONE);
    }
}
```

### Schemas_RemoveVersion

```java
import com.azure.resourcemanager.workloadorchestration.models.VersionParameter;

/**
 * Samples for Schemas RemoveVersion.
 */
public final class SchemasRemoveVersionSamples {
    /*
     * x-ms-original-file: 2025-06-01/Schemas_RemoveVersion_MaximumSet_Gen.json
     */
    /**
     * Sample code: Schemas_RemoveVersion_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void schemasRemoveVersionMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.schemas()
            .removeVersionWithResponse("rgconfigurationmanager", "testname",
                new VersionParameter().withVersion("ghtvdzgmzncaifrnuumg"), com.azure.core.util.Context.NONE);
    }
}
```

### Schemas_Update

```java
import com.azure.resourcemanager.workloadorchestration.models.Schema;
import com.azure.resourcemanager.workloadorchestration.models.SchemaProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Schemas Update.
 */
public final class SchemasUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/Schemas_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Schemas_Update_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void
        schemasUpdateMaximumSet(com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        Schema resource = manager.schemas()
            .getByResourceGroupWithResponse("rgconfigurationmanager", "testname", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key6760", "fakeTokenPlaceholder"))
            .withProperties(new SchemaProperties())
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

### SiteReferences_CreateOrUpdate

```java
import com.azure.resourcemanager.workloadorchestration.models.SiteReferenceProperties;

/**
 * Samples for SiteReferences CreateOrUpdate.
 */
public final class SiteReferencesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/SiteReferences_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: SiteReferences_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void siteReferencesCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.siteReferences()
            .define("testname")
            .withExistingContext("rgconfigurationmanager", "testname")
            .withProperties(new SiteReferenceProperties().withSiteId("xxjpxdcaumewwgpbwzkcrgrcw"))
            .create();
    }
}
```

### SiteReferences_Delete

```java
/**
 * Samples for SiteReferences Delete.
 */
public final class SiteReferencesDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01/SiteReferences_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: SiteReferences_Delete_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void siteReferencesDeleteMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.siteReferences()
            .delete("rgconfigurationmanager", "testname", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### SiteReferences_Get

```java
/**
 * Samples for SiteReferences Get.
 */
public final class SiteReferencesGetSamples {
    /*
     * x-ms-original-file: 2025-06-01/SiteReferences_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: SiteReferences_Get_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void siteReferencesGetMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.siteReferences()
            .getWithResponse("rgconfigurationmanager", "testname", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### SiteReferences_ListByContext

```java
/**
 * Samples for SiteReferences ListByContext.
 */
public final class SiteReferencesListByContextSamples {
    /*
     * x-ms-original-file: 2025-06-01/SiteReferences_ListByContext_MaximumSet_Gen.json
     */
    /**
     * Sample code: SiteReferences_ListByContext_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void siteReferencesListByContextMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.siteReferences().listByContext("rgconfigurationmanager", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### SiteReferences_Update

```java
import com.azure.resourcemanager.workloadorchestration.models.SiteReference;
import com.azure.resourcemanager.workloadorchestration.models.SiteReferenceProperties;

/**
 * Samples for SiteReferences Update.
 */
public final class SiteReferencesUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/SiteReferences_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: SiteReferences_Update_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void siteReferencesUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        SiteReference resource = manager.siteReferences()
            .getWithResponse("rgconfigurationmanager", "testname", "testname", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withProperties(new SiteReferenceProperties().withSiteId("nwiuyaro")).apply();
    }
}
```

### SolutionTemplateVersions_BulkDeploySolution

```java
import com.azure.resourcemanager.workloadorchestration.models.BulkDeploySolutionParameter;
import com.azure.resourcemanager.workloadorchestration.models.BulkDeployTargetDetails;
import java.util.Arrays;

/**
 * Samples for SolutionTemplateVersions BulkDeploySolution.
 */
public final class SolutionTemplateVersionsBulkDeploySolutionSamples {
    /*
     * x-ms-original-file: 2025-06-01/SolutionTemplateVersions_BulkDeploySolution_MaximumSet_Gen.json
     */
    /**
     * Sample code: SolutionTemplateVersions_BulkDeploySolution_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void solutionTemplateVersionsBulkDeploySolutionMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.solutionTemplateVersions()
            .bulkDeploySolution("rgconfigurationmanager", "testname", "1.0.0", new BulkDeploySolutionParameter()
                .withTargets(Arrays.asList(new BulkDeployTargetDetails().withSolutionVersionId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.Edge/Targets/target/Solutions/solution/Versions/solution-1.0.0.1"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### SolutionTemplateVersions_BulkPublishSolution

```java
import com.azure.resourcemanager.workloadorchestration.models.BulkPublishSolutionParameter;
import com.azure.resourcemanager.workloadorchestration.models.BulkPublishTargetDetails;
import com.azure.resourcemanager.workloadorchestration.models.SolutionDependencyParameter;
import java.util.Arrays;

/**
 * Samples for SolutionTemplateVersions BulkPublishSolution.
 */
public final class SolutionTemplateVersionsBulkPublishSolutionSamples {
    /*
     * x-ms-original-file: 2025-06-01/SolutionTemplateVersions_BulkPublishSolution_MaximumSet_Gen.json
     */
    /**
     * Sample code: SolutionTemplateVersions_BulkPublishSolution_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void solutionTemplateVersionsBulkPublishSolutionMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.solutionTemplateVersions()
            .bulkPublishSolution("rgconfigurationmanager", "testname", "1.0.0", new BulkPublishSolutionParameter()
                .withTargets(Arrays.asList(new BulkPublishTargetDetails().withTargetId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.Edge/Targets/target")
                    .withSolutionInstanceName("test-instance")))
                .withSolutionInstanceName("test-instance")
                .withSolutionDependencies(Arrays.asList(new SolutionDependencyParameter().withSolutionVersionId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.Edge/Targets/target/Solutions/solution/Versions/solution-1.0.0.1")
                    .withSolutionTemplateId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.Edge/SolutionTemplates/st")
                    .withSolutionTemplateVersion("1.0.0")
                    .withSolutionInstanceName("test-instance")
                    .withTargetId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.Edge/Targets/target")
                    .withDependencies(Arrays.asList()))),
                com.azure.core.util.Context.NONE);
    }
}
```

### SolutionTemplateVersions_Get

```java
/**
 * Samples for SolutionTemplateVersions Get.
 */
public final class SolutionTemplateVersionsGetSamples {
    /*
     * x-ms-original-file: 2025-06-01/SolutionTemplateVersions_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: SolutionTemplateVersions_Get_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void solutionTemplateVersionsGetMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.solutionTemplateVersions()
            .getWithResponse("rgconfigurationmanager", "testname", "1.0.0", com.azure.core.util.Context.NONE);
    }
}
```

### SolutionTemplateVersions_ListBySolutionTemplate

```java
/**
 * Samples for SolutionTemplateVersions ListBySolutionTemplate.
 */
public final class SolutionTemplateVersionsListBySolutionTemplateSamples {
    /*
     * x-ms-original-file: 2025-06-01/SolutionTemplateVersions_ListBySolutionTemplate_MaximumSet_Gen.json
     */
    /**
     * Sample code: SolutionTemplateVersions_ListBySolutionTemplate_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void solutionTemplateVersionsListBySolutionTemplateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.solutionTemplateVersions()
            .listBySolutionTemplate("rgconfigurationmanager", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### SolutionTemplates_CreateOrUpdate

```java
import com.azure.resourcemanager.workloadorchestration.models.ResourceState;
import com.azure.resourcemanager.workloadorchestration.models.SolutionTemplateProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SolutionTemplates CreateOrUpdate.
 */
public final class SolutionTemplatesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/SolutionTemplates_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: SolutionTemplates_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void solutionTemplatesCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.solutionTemplates()
            .define("testname")
            .withRegion("zheaaqvadewftnctxzpinrgeproqs")
            .withExistingResourceGroup("rgconfigurationmanager")
            .withTags(mapOf("key5091", "fakeTokenPlaceholder"))
            .withProperties(new SolutionTemplateProperties().withDescription("psrftehgzngcdlccivhjmwsmiz")
                .withCapabilities(Arrays.asList("dfoyxbbknrhvlunhmuyyt"))
                .withState(ResourceState.ACTIVE)
                .withEnableExternalValidation(true))
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

### SolutionTemplates_CreateVersion

```java
import com.azure.resourcemanager.workloadorchestration.fluent.models.SolutionTemplateVersionInner;
import com.azure.resourcemanager.workloadorchestration.fluent.models.SolutionTemplateVersionWithUpdateTypeInner;
import com.azure.resourcemanager.workloadorchestration.models.OrchestratorType;
import com.azure.resourcemanager.workloadorchestration.models.SolutionTemplateVersionProperties;
import com.azure.resourcemanager.workloadorchestration.models.UpdateType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SolutionTemplates CreateVersion.
 */
public final class SolutionTemplatesCreateVersionSamples {
    /*
     * x-ms-original-file: 2025-06-01/SolutionTemplates_CreateVersion_MaximumSet_Gen.json
     */
    /**
     * Sample code: SolutionTemplates_CreateVersion_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void solutionTemplatesCreateVersionMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.solutionTemplates()
            .createVersion("rgconfigurationmanager", "testname",
                new SolutionTemplateVersionWithUpdateTypeInner().withUpdateType(UpdateType.MAJOR)
                    .withVersion("1.0.0")
                    .withSolutionTemplateVersion(new SolutionTemplateVersionInner().withProperties(
                        new SolutionTemplateVersionProperties().withConfigurations("ofqcsavwmeuwmvtjnqpoybtjvkmrlh")
                            .withSpecification(mapOf())
                            .withOrchestratorType(OrchestratorType.TO))),
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

### SolutionTemplates_Delete

```java
/**
 * Samples for SolutionTemplates Delete.
 */
public final class SolutionTemplatesDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01/SolutionTemplates_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: SolutionTemplates_Delete_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void solutionTemplatesDeleteMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.solutionTemplates().delete("rgconfigurationmanager", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### SolutionTemplates_GetByResourceGroup

```java
/**
 * Samples for SolutionTemplates GetByResourceGroup.
 */
public final class SolutionTemplatesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01/SolutionTemplates_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: SolutionTemplates_Get_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void solutionTemplatesGetMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.solutionTemplates()
            .getByResourceGroupWithResponse("rgconfigurationmanager", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### SolutionTemplates_List

```java
/**
 * Samples for SolutionTemplates List.
 */
public final class SolutionTemplatesListSamples {
    /*
     * x-ms-original-file: 2025-06-01/SolutionTemplates_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: SolutionTemplates_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void solutionTemplatesListBySubscriptionMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.solutionTemplates().list(com.azure.core.util.Context.NONE);
    }
}
```

### SolutionTemplates_ListByResourceGroup

```java
/**
 * Samples for SolutionTemplates ListByResourceGroup.
 */
public final class SolutionTemplatesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01/SolutionTemplates_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: SolutionTemplates_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void solutionTemplatesListByResourceGroupMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.solutionTemplates().listByResourceGroup("rgconfigurationmanager", com.azure.core.util.Context.NONE);
    }
}
```

### SolutionTemplates_RemoveVersion

```java
import com.azure.resourcemanager.workloadorchestration.models.VersionParameter;

/**
 * Samples for SolutionTemplates RemoveVersion.
 */
public final class SolutionTemplatesRemoveVersionSamples {
    /*
     * x-ms-original-file: 2025-06-01/SolutionTemplates_RemoveVersion_MaximumSet_Gen.json
     */
    /**
     * Sample code: SolutionTemplates_RemoveVersion_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void solutionTemplatesRemoveVersionMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.solutionTemplates()
            .removeVersion("rgconfigurationmanager", "testname",
                new VersionParameter().withVersion("ghtvdzgmzncaifrnuumg"), com.azure.core.util.Context.NONE);
    }
}
```

### SolutionTemplates_Update

```java
import com.azure.resourcemanager.workloadorchestration.models.ResourceState;
import com.azure.resourcemanager.workloadorchestration.models.SolutionTemplate;
import com.azure.resourcemanager.workloadorchestration.models.SolutionTemplateProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SolutionTemplates Update.
 */
public final class SolutionTemplatesUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/SolutionTemplates_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: SolutionTemplates_Update_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void solutionTemplatesUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        SolutionTemplate resource = manager.solutionTemplates()
            .getByResourceGroupWithResponse("rgconfigurationmanager", "testname", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key8772", "fakeTokenPlaceholder"))
            .withProperties(new SolutionTemplateProperties().withDescription("onqlteg")
                .withCapabilities(Arrays.asList("relsv"))
                .withState(ResourceState.ACTIVE)
                .withEnableExternalValidation(true))
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

### SolutionVersions_CreateOrUpdate

```java
import com.azure.resourcemanager.workloadorchestration.models.ExtendedLocation;
import com.azure.resourcemanager.workloadorchestration.models.ExtendedLocationType;
import com.azure.resourcemanager.workloadorchestration.models.SolutionVersionProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SolutionVersions CreateOrUpdate.
 */
public final class SolutionVersionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/SolutionVersions_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: SolutionVersions_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void solutionVersionsCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.solutionVersions()
            .define("testname")
            .withExistingSolution("rgconfigurationmanager", "testname", "testname")
            .withProperties(new SolutionVersionProperties().withSpecification(mapOf()))
            .withExtendedLocation(
                new ExtendedLocation().withName("szjrwimeqyiue").withType(ExtendedLocationType.EDGE_ZONE))
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

### SolutionVersions_Delete

```java
/**
 * Samples for SolutionVersions Delete.
 */
public final class SolutionVersionsDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01/SolutionVersions_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: SolutionVersions_Delete_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void solutionVersionsDeleteMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.solutionVersions()
            .delete("rgconfigurationmanager", "testname", "testname", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### SolutionVersions_Get

```java
/**
 * Samples for SolutionVersions Get.
 */
public final class SolutionVersionsGetSamples {
    /*
     * x-ms-original-file: 2025-06-01/SolutionVersions_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: SolutionVersions_Get_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void solutionVersionsGetMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.solutionVersions()
            .getWithResponse("rgconfigurationmanager", "testname", "testname", "testname",
                com.azure.core.util.Context.NONE);
    }
}
```

### SolutionVersions_ListBySolution

```java
/**
 * Samples for SolutionVersions ListBySolution.
 */
public final class SolutionVersionsListBySolutionSamples {
    /*
     * x-ms-original-file: 2025-06-01/SolutionVersions_ListBySolution_MaximumSet_Gen.json
     */
    /**
     * Sample code: SolutionVersions_ListBySolution_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void solutionVersionsListBySolutionMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.solutionVersions()
            .listBySolution("rgconfigurationmanager", "testname", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### SolutionVersions_Update

```java
import com.azure.resourcemanager.workloadorchestration.models.SolutionVersion;
import com.azure.resourcemanager.workloadorchestration.models.SolutionVersionProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SolutionVersions Update.
 */
public final class SolutionVersionsUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/SolutionVersions_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: SolutionVersions_Update_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void solutionVersionsUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        SolutionVersion resource = manager.solutionVersions()
            .getWithResponse("rgconfigurationmanager", "testname", "testname", "testname",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withProperties(new SolutionVersionProperties().withSpecification(mapOf())).apply();
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

### Solutions_CreateOrUpdate

```java
import com.azure.resourcemanager.workloadorchestration.models.ExtendedLocation;
import com.azure.resourcemanager.workloadorchestration.models.ExtendedLocationType;
import com.azure.resourcemanager.workloadorchestration.models.SolutionProperties;

/**
 * Samples for Solutions CreateOrUpdate.
 */
public final class SolutionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/Solutions_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Solutions_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void solutionsCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.solutions()
            .define("testname")
            .withExistingTarget("rgconfigurationmanager", "testname")
            .withProperties(new SolutionProperties())
            .withExtendedLocation(
                new ExtendedLocation().withName("szjrwimeqyiue").withType(ExtendedLocationType.EDGE_ZONE))
            .create();
    }
}
```

### Solutions_Delete

```java
/**
 * Samples for Solutions Delete.
 */
public final class SolutionsDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01/Solutions_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Solutions_Delete_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void solutionsDeleteMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.solutions().delete("rgconfigurationmanager", "testname", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### Solutions_Get

```java
/**
 * Samples for Solutions Get.
 */
public final class SolutionsGetSamples {
    /*
     * x-ms-original-file: 2025-06-01/Solutions_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Solutions_Get_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void
        solutionsGetMaximumSet(com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.solutions()
            .getWithResponse("rgconfigurationmanager", "testname", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### Solutions_ListByTarget

```java
/**
 * Samples for Solutions ListByTarget.
 */
public final class SolutionsListByTargetSamples {
    /*
     * x-ms-original-file: 2025-06-01/Solutions_ListByTarget_MaximumSet_Gen.json
     */
    /**
     * Sample code: Solutions_ListByTarget_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void solutionsListByTargetMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.solutions().listByTarget("rgconfigurationmanager", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### Solutions_Update

```java
import com.azure.resourcemanager.workloadorchestration.models.Solution;
import com.azure.resourcemanager.workloadorchestration.models.SolutionProperties;

/**
 * Samples for Solutions Update.
 */
public final class SolutionsUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/Solutions_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Solutions_Update_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void solutionsUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        Solution resource = manager.solutions()
            .getWithResponse("rgconfigurationmanager", "testname", "testname", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withProperties(new SolutionProperties()).apply();
    }
}
```

### Targets_CreateOrUpdate

```java
import com.azure.resourcemanager.workloadorchestration.models.ExtendedLocation;
import com.azure.resourcemanager.workloadorchestration.models.ExtendedLocationType;
import com.azure.resourcemanager.workloadorchestration.models.ResourceState;
import com.azure.resourcemanager.workloadorchestration.models.TargetProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Targets CreateOrUpdate.
 */
public final class TargetsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/Targets_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Targets_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void targetsCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.targets()
            .define("testname")
            .withRegion("kckloegmwsjgwtcl")
            .withExistingResourceGroup("rgconfigurationmanager")
            .withTags(mapOf("key612", "fakeTokenPlaceholder"))
            .withProperties(new TargetProperties().withDescription("riabrxtvhlmizyhffdpjeyhvw")
                .withDisplayName("qjlbshhqzfmwxvvynibkoi")
                .withContextId(
                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/{resourceProviderNamespace}/{resourceType}/{resourceName}")
                .withTargetSpecification(mapOf())
                .withCapabilities(Arrays.asList("grjapghdidoao"))
                .withHierarchyLevel("octqptfirejhjfavlnfqeiikqx")
                .withSolutionScope("testname")
                .withState(ResourceState.ACTIVE))
            .withExtendedLocation(
                new ExtendedLocation().withName("szjrwimeqyiue").withType(ExtendedLocationType.EDGE_ZONE))
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

### Targets_Delete

```java
/**
 * Samples for Targets Delete.
 */
public final class TargetsDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01/Targets_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Targets_Delete_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void
        targetsDeleteMaximumSet(com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.targets().delete("rgconfigurationmanager", "testname", true, com.azure.core.util.Context.NONE);
    }
}
```

### Targets_GetByResourceGroup

```java
/**
 * Samples for Targets GetByResourceGroup.
 */
public final class TargetsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01/Targets_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Targets_Get_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void
        targetsGetMaximumSet(com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.targets()
            .getByResourceGroupWithResponse("rgconfigurationmanager", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### Targets_InstallSolution

```java
import com.azure.resourcemanager.workloadorchestration.models.InstallSolutionParameter;

/**
 * Samples for Targets InstallSolution.
 */
public final class TargetsInstallSolutionSamples {
    /*
     * x-ms-original-file: 2025-06-01/Targets_InstallSolution_MaximumSet_Gen.json
     */
    /**
     * Sample code: Targets_InstallSolution_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void targetsInstallSolutionMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.targets()
            .installSolution("rgconfigurationmanager", "testname", new InstallSolutionParameter().withSolutionVersionId(
                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/{resourceProviderNamespace}/{resourceType}/{resourceName}"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Targets_List

```java
/**
 * Samples for Targets List.
 */
public final class TargetsListSamples {
    /*
     * x-ms-original-file: 2025-06-01/Targets_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: Targets_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void targetsListBySubscriptionMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.targets().list(com.azure.core.util.Context.NONE);
    }
}
```

### Targets_ListByResourceGroup

```java
/**
 * Samples for Targets ListByResourceGroup.
 */
public final class TargetsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01/Targets_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Targets_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void targetsListByResourceGroupMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.targets().listByResourceGroup("rgconfigurationmanager", com.azure.core.util.Context.NONE);
    }
}
```

### Targets_PublishSolutionVersion

```java
import com.azure.resourcemanager.workloadorchestration.models.SolutionVersionParameter;

/**
 * Samples for Targets PublishSolutionVersion.
 */
public final class TargetsPublishSolutionVersionSamples {
    /*
     * x-ms-original-file: 2025-06-01/Targets_PublishSolutionVersion_MaximumSet_Gen.json
     */
    /**
     * Sample code: Targets_PublishSolutionVersion_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void targetsPublishSolutionVersionMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.targets()
            .publishSolutionVersion("rgconfigurationmanager", "testname",
                new SolutionVersionParameter().withSolutionVersionId(
                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/{resourceProviderNamespace}/{resourceType}/{resourceName}"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Targets_RemoveRevision

```java
import com.azure.resourcemanager.workloadorchestration.models.RemoveRevisionParameter;

/**
 * Samples for Targets RemoveRevision.
 */
public final class TargetsRemoveRevisionSamples {
    /*
     * x-ms-original-file: 2025-06-01/Targets_RemoveRevision_MaximumSet_Gen.json
     */
    /**
     * Sample code: Targets_RemoveRevision_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void targetsRemoveRevisionMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.targets()
            .removeRevision("rgconfigurationmanager", "testname", new RemoveRevisionParameter().withSolutionTemplateId(
                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/{resourceProviderNamespace}/{resourceType}/{resourceName}")
                .withSolutionVersion("tomwmqybqomwkfaeukjneva"), com.azure.core.util.Context.NONE);
    }
}
```

### Targets_ResolveConfiguration

```java
import com.azure.resourcemanager.workloadorchestration.models.SolutionDependencyParameter;
import com.azure.resourcemanager.workloadorchestration.models.SolutionTemplateParameter;
import java.util.Arrays;

/**
 * Samples for Targets ResolveConfiguration.
 */
public final class TargetsResolveConfigurationSamples {
    /*
     * x-ms-original-file: 2025-06-01/Targets_ResolveConfiguration_MaximumSet_Gen.json
     */
    /**
     * Sample code: Targets_ResolveConfiguration_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void targetsResolveConfigurationMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.targets()
            .resolveConfiguration("rgconfigurationmanager", "testname",
                new SolutionTemplateParameter().withSolutionTemplateVersionId(
                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/{resourceProviderNamespace}/{resourceType}/{resourceName}/{resourceType}/{resourceName}")
                    .withSolutionInstanceName("testname")
                    .withSolutionDependencies(
                        Arrays.asList(new SolutionDependencyParameter().withSolutionVersionId("cydzqntmjlqtksbavjwteru")
                            .withSolutionTemplateId("liqauthxnscodbiwktwfwrrsg")
                            .withSolutionTemplateVersion("gordjasyxxrj")
                            .withSolutionInstanceName("testname")
                            .withTargetId("steadvphxtyhjokqicrtg")
                            .withDependencies(Arrays.asList()))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Targets_ReviewSolutionVersion

```java
import com.azure.resourcemanager.workloadorchestration.models.SolutionDependencyParameter;
import com.azure.resourcemanager.workloadorchestration.models.SolutionTemplateParameter;
import java.util.Arrays;

/**
 * Samples for Targets ReviewSolutionVersion.
 */
public final class TargetsReviewSolutionVersionSamples {
    /*
     * x-ms-original-file: 2025-06-01/Targets_ReviewSolutionVersion_MaximumSet_Gen.json
     */
    /**
     * Sample code: Targets_ReviewSolutionVersion_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void targetsReviewSolutionVersionMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.targets()
            .reviewSolutionVersion("rgconfigurationmanager", "testname",
                new SolutionTemplateParameter().withSolutionTemplateVersionId(
                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/{resourceProviderNamespace}/{resourceType}/{resourceName}/{resourceType}/{resourceName}")
                    .withSolutionInstanceName("testname")
                    .withSolutionDependencies(
                        Arrays.asList(new SolutionDependencyParameter().withSolutionVersionId("cydzqntmjlqtksbavjwteru")
                            .withSolutionTemplateId("liqauthxnscodbiwktwfwrrsg")
                            .withSolutionTemplateVersion("gordjasyxxrj")
                            .withSolutionInstanceName("testname")
                            .withTargetId("steadvphxtyhjokqicrtg")
                            .withDependencies(Arrays.asList()))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Targets_UninstallSolution

```java
import com.azure.resourcemanager.workloadorchestration.models.UninstallSolutionParameter;

/**
 * Samples for Targets UninstallSolution.
 */
public final class TargetsUninstallSolutionSamples {
    /*
     * x-ms-original-file: 2025-06-01/Targets_UninstallSolution_MaximumSet_Gen.json
     */
    /**
     * Sample code: Targets_UninstallSolution_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void targetsUninstallSolutionMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.targets()
            .uninstallSolution("rgconfigurationmanager", "testname",
                new UninstallSolutionParameter().withSolutionTemplateId(
                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/{resourceProviderNamespace}/{resourceType}/{resourceName}")
                    .withSolutionInstanceName("lzihiumrcxbolmkqktvtuqyhg"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Targets_Update

```java
import com.azure.resourcemanager.workloadorchestration.models.ResourceState;
import com.azure.resourcemanager.workloadorchestration.models.Target;
import com.azure.resourcemanager.workloadorchestration.models.TargetProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Targets Update.
 */
public final class TargetsUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/Targets_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Targets_Update_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void
        targetsUpdateMaximumSet(com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        Target resource = manager.targets()
            .getByResourceGroupWithResponse("rgconfigurationmanager", "testname", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key8026", "fakeTokenPlaceholder"))
            .withProperties(new TargetProperties().withDescription("yhnhdpznncdvncmnvoeohqjx")
                .withDisplayName("pguujtzjjvixgjitugybrefp")
                .withContextId(
                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/{resourceProviderNamespace}/{resourceType}/{resourceName}")
                .withTargetSpecification(mapOf())
                .withCapabilities(Arrays.asList("dasqhyxfakivfzqb"))
                .withHierarchyLevel("hfyntwxetgsmnucbjvvphtyxu")
                .withSolutionScope("testname")
                .withState(ResourceState.ACTIVE))
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

### Targets_UpdateExternalValidationStatus

```java
import com.azure.core.management.exception.ManagementError;
import com.azure.resourcemanager.workloadorchestration.models.UpdateExternalValidationStatusParameter;
import com.azure.resourcemanager.workloadorchestration.models.ValidationStatus;

/**
 * Samples for Targets UpdateExternalValidationStatus.
 */
public final class TargetsUpdateExternalValidationStatusSamples {
    /*
     * x-ms-original-file: 2025-06-01/Targets_UpdateExternalValidationStatus_MaximumSet_Gen.json
     */
    /**
     * Sample code: Targets_UpdateExternalValidationStatus_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void targetsUpdateExternalValidationStatusMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.targets()
            .updateExternalValidationStatus("rgconfigurationmanager", "testname",
                new UpdateExternalValidationStatusParameter().withSolutionVersionId("shntcsuwlmpehmuqkrbf")
                    .withErrorDetails(new ManagementError())
                    .withExternalValidationId("ivsjzwy")
                    .withValidationStatus(ValidationStatus.VALID),
                com.azure.core.util.Context.NONE);
    }
}
```

### WorkflowVersions_CreateOrUpdate

```java
import com.azure.resourcemanager.workloadorchestration.models.ErrorAction;
import com.azure.resourcemanager.workloadorchestration.models.ErrorActionMode;
import com.azure.resourcemanager.workloadorchestration.models.ExtendedLocation;
import com.azure.resourcemanager.workloadorchestration.models.ExtendedLocationType;
import com.azure.resourcemanager.workloadorchestration.models.StageSpec;
import com.azure.resourcemanager.workloadorchestration.models.TaskOption;
import com.azure.resourcemanager.workloadorchestration.models.TaskSpec;
import com.azure.resourcemanager.workloadorchestration.models.WorkflowVersionProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for WorkflowVersions CreateOrUpdate.
 */
public final class WorkflowVersionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/WorkflowVersions_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: WorkflowVersions_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void workflowVersionsCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.workflowVersions()
            .define("testname")
            .withExistingWorkflow("rgconfigurationmanager", "testname", "testname")
            .withProperties(new WorkflowVersionProperties().withStageSpec(Arrays.asList(new StageSpec()
                .withName("amrbjd")
                .withSpecification(mapOf())
                .withTasks(Arrays.asList(new TaskSpec().withName("xxmeyvmgydbcwxqwjhadjxjod")
                    .withTargetId(
                        "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/{resourceProviderNamespace}/{resourceType}/{resourceName}")
                    .withSpecification(mapOf())))
                .withTaskOption(
                    new TaskOption().withConcurrency(3)
                        .withErrorAction(new ErrorAction().withMode(ErrorActionMode.STOP_ON_ANY_FAILURE)
                            .withMaxToleratedFailures(0)))))
                .withSpecification(mapOf()))
            .withExtendedLocation(
                new ExtendedLocation().withName("szjrwimeqyiue").withType(ExtendedLocationType.EDGE_ZONE))
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

### WorkflowVersions_Delete

```java
/**
 * Samples for WorkflowVersions Delete.
 */
public final class WorkflowVersionsDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01/WorkflowVersions_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: WorkflowVersions_Delete_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void workflowVersionsDeleteMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.workflowVersions()
            .delete("rgconfigurationmanager", "testname", "testname", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### WorkflowVersions_Get

```java
/**
 * Samples for WorkflowVersions Get.
 */
public final class WorkflowVersionsGetSamples {
    /*
     * x-ms-original-file: 2025-06-01/WorkflowVersions_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: WorkflowVersions_Get_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void workflowVersionsGetMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.workflowVersions()
            .getWithResponse("rgconfigurationmanager", "testname", "testname", "testname",
                com.azure.core.util.Context.NONE);
    }
}
```

### WorkflowVersions_ListByWorkflow

```java
/**
 * Samples for WorkflowVersions ListByWorkflow.
 */
public final class WorkflowVersionsListByWorkflowSamples {
    /*
     * x-ms-original-file: 2025-06-01/WorkflowVersions_ListByWorkflow_MaximumSet_Gen.json
     */
    /**
     * Sample code: WorkflowVersions_ListByWorkflow_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void workflowVersionsListByWorkflowMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.workflowVersions()
            .listByWorkflow("rgconfigurationmanager", "testname", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### WorkflowVersions_Update

```java
import com.azure.resourcemanager.workloadorchestration.models.ErrorAction;
import com.azure.resourcemanager.workloadorchestration.models.ErrorActionMode;
import com.azure.resourcemanager.workloadorchestration.models.StageSpec;
import com.azure.resourcemanager.workloadorchestration.models.TaskOption;
import com.azure.resourcemanager.workloadorchestration.models.TaskSpec;
import com.azure.resourcemanager.workloadorchestration.models.WorkflowVersion;
import com.azure.resourcemanager.workloadorchestration.models.WorkflowVersionProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for WorkflowVersions Update.
 */
public final class WorkflowVersionsUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/WorkflowVersions_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: WorkflowVersions_Update_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void workflowVersionsUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        WorkflowVersion resource = manager.workflowVersions()
            .getWithResponse("rgconfigurationmanager", "testname", "testname", "testname",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new WorkflowVersionProperties().withStageSpec(Arrays.asList(new StageSpec()
                .withName("amrbjd")
                .withSpecification(mapOf())
                .withTasks(Arrays.asList(new TaskSpec().withName("xxmeyvmgydbcwxqwjhadjxjod")
                    .withTargetId(
                        "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/{resourceProviderNamespace}/{resourceType}/{resourceName}")
                    .withSpecification(mapOf())))
                .withTaskOption(
                    new TaskOption().withConcurrency(3)
                        .withErrorAction(new ErrorAction().withMode(ErrorActionMode.STOP_ON_ANY_FAILURE)
                            .withMaxToleratedFailures(0)))))
                .withSpecification(mapOf()))
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

### Workflows_CreateOrUpdate

```java
import com.azure.resourcemanager.workloadorchestration.models.ExtendedLocation;
import com.azure.resourcemanager.workloadorchestration.models.ExtendedLocationType;
import com.azure.resourcemanager.workloadorchestration.models.WorkflowProperties;

/**
 * Samples for Workflows CreateOrUpdate.
 */
public final class WorkflowsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/Workflows_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workflows_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void workflowsCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.workflows()
            .define("testname")
            .withExistingContext("rgconfigurationmanager", "testname")
            .withProperties(new WorkflowProperties())
            .withExtendedLocation(
                new ExtendedLocation().withName("szjrwimeqyiue").withType(ExtendedLocationType.EDGE_ZONE))
            .create();
    }
}
```

### Workflows_Delete

```java
/**
 * Samples for Workflows Delete.
 */
public final class WorkflowsDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01/Workflows_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workflows_Delete_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void workflowsDeleteMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.workflows().delete("rgconfigurationmanager", "testname", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### Workflows_Get

```java
/**
 * Samples for Workflows Get.
 */
public final class WorkflowsGetSamples {
    /*
     * x-ms-original-file: 2025-06-01/Workflows_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workflows_Get_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void
        workflowsGetMaximumSet(com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.workflows()
            .getWithResponse("rgconfigurationmanager", "testname", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### Workflows_ListByContext

```java
/**
 * Samples for Workflows ListByContext.
 */
public final class WorkflowsListByContextSamples {
    /*
     * x-ms-original-file: 2025-06-01/Workflows_ListByContext_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workflows_ListByContext_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void workflowsListByContextMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        manager.workflows().listByContext("rgconfigurationmanager", "testname", com.azure.core.util.Context.NONE);
    }
}
```

### Workflows_Update

```java
import com.azure.resourcemanager.workloadorchestration.models.Workflow;
import com.azure.resourcemanager.workloadorchestration.models.WorkflowProperties;

/**
 * Samples for Workflows Update.
 */
public final class WorkflowsUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01/Workflows_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workflows_Update_MaximumSet.
     * 
     * @param manager Entry point to WorkloadOrchestrationManager.
     */
    public static void workflowsUpdateMaximumSet(
        com.azure.resourcemanager.workloadorchestration.WorkloadOrchestrationManager manager) {
        Workflow resource = manager.workflows()
            .getWithResponse("rgconfigurationmanager", "testname", "testname", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withProperties(new WorkflowProperties()).apply();
    }
}
```

