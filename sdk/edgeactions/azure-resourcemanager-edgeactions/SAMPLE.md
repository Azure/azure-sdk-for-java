# Code snippets and samples


## EdgeActionExecutionFilters

- [Create](#edgeactionexecutionfilters_create)
- [Delete](#edgeactionexecutionfilters_delete)
- [Get](#edgeactionexecutionfilters_get)
- [ListByEdgeAction](#edgeactionexecutionfilters_listbyedgeaction)
- [Update](#edgeactionexecutionfilters_update)

## EdgeActionVersions

- [Create](#edgeactionversions_create)
- [Delete](#edgeactionversions_delete)
- [DeployVersionCode](#edgeactionversions_deployversioncode)
- [Get](#edgeactionversions_get)
- [GetVersionCode](#edgeactionversions_getversioncode)
- [ListByEdgeAction](#edgeactionversions_listbyedgeaction)
- [SwapDefault](#edgeactionversions_swapdefault)
- [Update](#edgeactionversions_update)

## EdgeActions

- [AddAttachment](#edgeactions_addattachment)
- [Create](#edgeactions_create)
- [Delete](#edgeactions_delete)
- [DeleteAttachment](#edgeactions_deleteattachment)
- [GetByResourceGroup](#edgeactions_getbyresourcegroup)
- [List](#edgeactions_list)
- [ListByResourceGroup](#edgeactions_listbyresourcegroup)
- [Update](#edgeactions_update)
### EdgeActionExecutionFilters_Create

```java
import com.azure.resourcemanager.edgeactions.models.EdgeActionExecutionFilterProperties;

/**
 * Samples for EdgeActionExecutionFilters Create.
 */
public final class EdgeActionExecutionFiltersCreateSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/EdgeActionExecutionFilters_Create.json
     */
    /**
     * Sample code: CreateEdgeActionExecutionFilters.
     * 
     * @param manager Entry point to EdgeActionsManager.
     */
    public static void
        createEdgeActionExecutionFilters(com.azure.resourcemanager.edgeactions.EdgeActionsManager manager) {
        manager.edgeActionExecutionFilters()
            .define("executionFilter1")
            .withRegion("global")
            .withExistingEdgeAction("testrg", "edgeAction1")
            .withProperties(new EdgeActionExecutionFilterProperties().withVersionId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/testrg/providers/Microsoft.Cdn/EdgeActions/edgeAction1/versions/version1")
                .withExecutionFilterIdentifierHeaderName("header-key")
                .withExecutionFilterIdentifierHeaderValue("header-value"))
            .create();
    }
}
```

### EdgeActionExecutionFilters_Delete

```java
/**
 * Samples for EdgeActionExecutionFilters Delete.
 */
public final class EdgeActionExecutionFiltersDeleteSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/EdgeActionExecutionFilters_Delete.json
     */
    /**
     * Sample code: DeleteEdgeActionExecutionFilters.
     * 
     * @param manager Entry point to EdgeActionsManager.
     */
    public static void
        deleteEdgeActionExecutionFilters(com.azure.resourcemanager.edgeactions.EdgeActionsManager manager) {
        manager.edgeActionExecutionFilters()
            .delete("testrg", "edgeAction1", "executionFilter1", com.azure.core.util.Context.NONE);
    }
}
```

### EdgeActionExecutionFilters_Get

```java
/**
 * Samples for EdgeActionExecutionFilters Get.
 */
public final class EdgeActionExecutionFiltersGetSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/EdgeActionExecutionFilters_Get.json
     */
    /**
     * Sample code: GetEdgeActionExecutionFilters.
     * 
     * @param manager Entry point to EdgeActionsManager.
     */
    public static void getEdgeActionExecutionFilters(com.azure.resourcemanager.edgeactions.EdgeActionsManager manager) {
        manager.edgeActionExecutionFilters()
            .getWithResponse("testrg", "edgeAction1", "executionFilter1", com.azure.core.util.Context.NONE);
    }
}
```

### EdgeActionExecutionFilters_ListByEdgeAction

```java
/**
 * Samples for EdgeActionExecutionFilters ListByEdgeAction.
 */
public final class EdgeActionExecutionFiltersListByEdgeActionSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/EdgeActionExecutionFilters_ListByEdgeAction.json
     */
    /**
     * Sample code: ListEdgeActionsExecutionFiltersByEdgeAction.
     * 
     * @param manager Entry point to EdgeActionsManager.
     */
    public static void
        listEdgeActionsExecutionFiltersByEdgeAction(com.azure.resourcemanager.edgeactions.EdgeActionsManager manager) {
        manager.edgeActionExecutionFilters()
            .listByEdgeAction("testrg", "edgeAction1", com.azure.core.util.Context.NONE);
    }
}
```

### EdgeActionExecutionFilters_Update

```java
import com.azure.resourcemanager.edgeactions.models.EdgeActionExecutionFilter;
import com.azure.resourcemanager.edgeactions.models.EdgeActionExecutionFilterProperties;

/**
 * Samples for EdgeActionExecutionFilters Update.
 */
public final class EdgeActionExecutionFiltersUpdateSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/EdgeActionExecutionFilters_Update.json
     */
    /**
     * Sample code: UpdateEdgeActionExecutionFilters.
     * 
     * @param manager Entry point to EdgeActionsManager.
     */
    public static void
        updateEdgeActionExecutionFilters(com.azure.resourcemanager.edgeactions.EdgeActionsManager manager) {
        EdgeActionExecutionFilter resource = manager.edgeActionExecutionFilters()
            .getWithResponse("testrg", "edgeAction1", "executionFilter1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(
                new EdgeActionExecutionFilterProperties().withExecutionFilterIdentifierHeaderValue("header-value2"))
            .apply();
    }
}
```

### EdgeActionVersions_Create

```java
import com.azure.resourcemanager.edgeactions.fluent.models.EdgeActionVersionPropertiesInner;
import com.azure.resourcemanager.edgeactions.models.EdgeActionIsDefaultVersion;
import com.azure.resourcemanager.edgeactions.models.EdgeActionVersionDeploymentType;

/**
 * Samples for EdgeActionVersions Create.
 */
public final class EdgeActionVersionsCreateSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/EdgeActionVersions_Create.json
     */
    /**
     * Sample code: CreateEdgeActionVersion.
     * 
     * @param manager Entry point to EdgeActionsManager.
     */
    public static void createEdgeActionVersion(com.azure.resourcemanager.edgeactions.EdgeActionsManager manager) {
        manager.edgeActionVersions()
            .define("version2")
            .withRegion("global")
            .withExistingEdgeAction("testrg", "edgeAction1")
            .withProperties(
                new EdgeActionVersionPropertiesInner().withDeploymentType(EdgeActionVersionDeploymentType.ZIP)
                    .withIsDefaultVersion(EdgeActionIsDefaultVersion.TRUE))
            .create();
    }
}
```

### EdgeActionVersions_Delete

```java
/**
 * Samples for EdgeActionVersions Delete.
 */
public final class EdgeActionVersionsDeleteSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/EdgeActionVersions_Delete.json
     */
    /**
     * Sample code: DeleteEdgeActionVersion.
     * 
     * @param manager Entry point to EdgeActionsManager.
     */
    public static void deleteEdgeActionVersion(com.azure.resourcemanager.edgeactions.EdgeActionsManager manager) {
        manager.edgeActionVersions().delete("testrg", "edgeAction1", "version1", com.azure.core.util.Context.NONE);
    }
}
```

### EdgeActionVersions_DeployVersionCode

```java
import com.azure.resourcemanager.edgeactions.fluent.models.VersionCodeInner;

/**
 * Samples for EdgeActionVersions DeployVersionCode.
 */
public final class EdgeActionVersionsDeployVersionCodeSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/EdgeActionVersions_DeployVersionCode.json
     */
    /**
     * Sample code: DeployVersionCode.
     * 
     * @param manager Entry point to EdgeActionsManager.
     */
    public static void deployVersionCode(com.azure.resourcemanager.edgeactions.EdgeActionsManager manager) {
        manager.edgeActionVersions()
            .deployVersionCode("testrg", "edgeAction1", "version2",
                new VersionCodeInner().withContent("UEsDBBQAAAAIAI1NzkQAAAAABQAAAA==").withName("zippedFile"),
                com.azure.core.util.Context.NONE);
    }
}
```

### EdgeActionVersions_Get

```java
/**
 * Samples for EdgeActionVersions Get.
 */
public final class EdgeActionVersionsGetSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/EdgeActionVersions_Get.json
     */
    /**
     * Sample code: GetEdgeActionVersion.
     * 
     * @param manager Entry point to EdgeActionsManager.
     */
    public static void getEdgeActionVersion(com.azure.resourcemanager.edgeactions.EdgeActionsManager manager) {
        manager.edgeActionVersions()
            .getWithResponse("testrg", "edgeAction1", "version1", com.azure.core.util.Context.NONE);
    }
}
```

### EdgeActionVersions_GetVersionCode

```java
/**
 * Samples for EdgeActionVersions GetVersionCode.
 */
public final class EdgeActionVersionsGetVersionCodeSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/EdgeActionVersions_GetVersionCode.json
     */
    /**
     * Sample code: GetVersionCode.
     * 
     * @param manager Entry point to EdgeActionsManager.
     */
    public static void getVersionCode(com.azure.resourcemanager.edgeactions.EdgeActionsManager manager) {
        manager.edgeActionVersions()
            .getVersionCode("testrg", "edgeAction1", "version1", com.azure.core.util.Context.NONE);
    }
}
```

### EdgeActionVersions_ListByEdgeAction

```java
/**
 * Samples for EdgeActionVersions ListByEdgeAction.
 */
public final class EdgeActionVersionsListByEdgeActionSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/EdgeActionVersions_ListByEdgeAction.json
     */
    /**
     * Sample code: GetEdgeActionVersionsByEdgeAction.
     * 
     * @param manager Entry point to EdgeActionsManager.
     */
    public static void
        getEdgeActionVersionsByEdgeAction(com.azure.resourcemanager.edgeactions.EdgeActionsManager manager) {
        manager.edgeActionVersions().listByEdgeAction("testrg", "edgeAction1", com.azure.core.util.Context.NONE);
    }
}
```

### EdgeActionVersions_SwapDefault

```java
/**
 * Samples for EdgeActionVersions SwapDefault.
 */
public final class EdgeActionVersionsSwapDefaultSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/EdgeActionVersions_SwapDefault.json
     */
    /**
     * Sample code: Swap Default Version.
     * 
     * @param manager Entry point to EdgeActionsManager.
     */
    public static void swapDefaultVersion(com.azure.resourcemanager.edgeactions.EdgeActionsManager manager) {
        manager.edgeActionVersions().swapDefault("testrg", "edgeAction1", "1.0", com.azure.core.util.Context.NONE);
    }
}
```

### EdgeActionVersions_Update

```java
import com.azure.resourcemanager.edgeactions.fluent.models.EdgeActionVersionPropertiesInner;
import com.azure.resourcemanager.edgeactions.models.EdgeActionVersion;
import com.azure.resourcemanager.edgeactions.models.EdgeActionVersionDeploymentType;

/**
 * Samples for EdgeActionVersions Update.
 */
public final class EdgeActionVersionsUpdateSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/EdgeActionVersions_Update.json
     */
    /**
     * Sample code: UpdateEdgeActionVersion.
     * 
     * @param manager Entry point to EdgeActionsManager.
     */
    public static void updateEdgeActionVersion(com.azure.resourcemanager.edgeactions.EdgeActionsManager manager) {
        EdgeActionVersion resource = manager.edgeActionVersions()
            .getWithResponse("testrg", "edgeAction1", "version1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(
                new EdgeActionVersionPropertiesInner().withDeploymentType(EdgeActionVersionDeploymentType.OTHERS))
            .apply();
    }
}
```

### EdgeActions_AddAttachment

```java
import com.azure.resourcemanager.edgeactions.models.EdgeActionAttachment;

/**
 * Samples for EdgeActions AddAttachment.
 */
public final class EdgeActionsAddAttachmentSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/EdgeActions_AddAttachment.json
     */
    /**
     * Sample code: AddAttachment.
     * 
     * @param manager Entry point to EdgeActionsManager.
     */
    public static void addAttachment(com.azure.resourcemanager.edgeactions.EdgeActionsManager manager) {
        manager.edgeActions()
            .addAttachment("testrg", "edgeAction1", new EdgeActionAttachment().withAttachedResourceId(
                "/subscriptions/sub1/resourceGroups/rs1/providers/Microsoft.Cdn/Profiles/myProfile/afdEndpoints/ep1/routes/route1"),
                com.azure.core.util.Context.NONE);
    }
}
```

### EdgeActions_Create

```java
import com.azure.resourcemanager.edgeactions.models.SkuType;

/**
 * Samples for EdgeActions Create.
 */
public final class EdgeActionsCreateSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/EdgeActions_Create.json
     */
    /**
     * Sample code: CreateEdgeAction.
     * 
     * @param manager Entry point to EdgeActionsManager.
     */
    public static void createEdgeAction(com.azure.resourcemanager.edgeactions.EdgeActionsManager manager) {
        manager.edgeActions()
            .define("edgeAction1")
            .withRegion("global")
            .withExistingResourceGroup("testrg")
            .withSku(new SkuType().withName("Standard").withTier("Standard"))
            .create();
    }
}
```

### EdgeActions_Delete

```java
/**
 * Samples for EdgeActions Delete.
 */
public final class EdgeActionsDeleteSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/EdgeActions_Delete.json
     */
    /**
     * Sample code: DeleteEdgeAction.
     * 
     * @param manager Entry point to EdgeActionsManager.
     */
    public static void deleteEdgeAction(com.azure.resourcemanager.edgeactions.EdgeActionsManager manager) {
        manager.edgeActions().delete("testrg", "edgeAction1", com.azure.core.util.Context.NONE);
    }
}
```

### EdgeActions_DeleteAttachment

```java
import com.azure.resourcemanager.edgeactions.models.EdgeActionAttachment;

/**
 * Samples for EdgeActions DeleteAttachment.
 */
public final class EdgeActionsDeleteAttachmentSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/EdgeActions_DeleteAttachment.json
     */
    /**
     * Sample code: DeleteAttachment.
     * 
     * @param manager Entry point to EdgeActionsManager.
     */
    public static void deleteAttachment(com.azure.resourcemanager.edgeactions.EdgeActionsManager manager) {
        manager.edgeActions()
            .deleteAttachment("testrg", "edgeAction1", new EdgeActionAttachment().withAttachedResourceId(
                "/subscriptions/sub1/resourceGroups/rs1/providers/Microsoft.Cdn/Profiles/myProfile/afdEndpoints/ep1/routes/route1"),
                com.azure.core.util.Context.NONE);
    }
}
```

### EdgeActions_GetByResourceGroup

```java
/**
 * Samples for EdgeActions GetByResourceGroup.
 */
public final class EdgeActionsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/EdgeActions_Get.json
     */
    /**
     * Sample code: GetEdgeAction.
     * 
     * @param manager Entry point to EdgeActionsManager.
     */
    public static void getEdgeAction(com.azure.resourcemanager.edgeactions.EdgeActionsManager manager) {
        manager.edgeActions().getByResourceGroupWithResponse("testrg", "edgeAction1", com.azure.core.util.Context.NONE);
    }
}
```

### EdgeActions_List

```java
/**
 * Samples for EdgeActions List.
 */
public final class EdgeActionsListSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/EdgeActions_ListBySubscription.json
     */
    /**
     * Sample code: ListEdgeActions_bySubscription.
     * 
     * @param manager Entry point to EdgeActionsManager.
     */
    public static void listEdgeActionsBySubscription(com.azure.resourcemanager.edgeactions.EdgeActionsManager manager) {
        manager.edgeActions().list(com.azure.core.util.Context.NONE);
    }
}
```

### EdgeActions_ListByResourceGroup

```java
/**
 * Samples for EdgeActions ListByResourceGroup.
 */
public final class EdgeActionsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/EdgeActions_ListByResourceGroup.json
     */
    /**
     * Sample code: ListEdgeActions_byResourceGroup.
     * 
     * @param manager Entry point to EdgeActionsManager.
     */
    public static void
        listEdgeActionsByResourceGroup(com.azure.resourcemanager.edgeactions.EdgeActionsManager manager) {
        manager.edgeActions().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### EdgeActions_Update

```java
import com.azure.resourcemanager.edgeactions.models.EdgeAction;
import com.azure.resourcemanager.edgeactions.models.SkuType;

/**
 * Samples for EdgeActions Update.
 */
public final class EdgeActionsUpdateSamples {
    /*
     * x-ms-original-file: 2025-09-01-preview/EdgeActions_Update.json
     */
    /**
     * Sample code: UpdateEdgeAction.
     * 
     * @param manager Entry point to EdgeActionsManager.
     */
    public static void updateEdgeAction(com.azure.resourcemanager.edgeactions.EdgeActionsManager manager) {
        EdgeAction resource = manager.edgeActions()
            .getByResourceGroupWithResponse("testrg", "edgeAction1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withSku(new SkuType().withName("Standard").withTier("Standard")).apply();
    }
}
```

