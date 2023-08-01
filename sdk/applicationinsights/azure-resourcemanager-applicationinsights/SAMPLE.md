# Code snippets and samples


## AnalyticsItems

- [Delete](#analyticsitems_delete)
- [Get](#analyticsitems_get)
- [List](#analyticsitems_list)
- [Put](#analyticsitems_put)

## Annotations

- [Create](#annotations_create)
- [Delete](#annotations_delete)
- [Get](#annotations_get)
- [List](#annotations_list)

## ApiKeys

- [Create](#apikeys_create)
- [Delete](#apikeys_delete)
- [Get](#apikeys_get)
- [List](#apikeys_list)

## ComponentAvailableFeatures

- [Get](#componentavailablefeatures_get)

## ComponentCurrentBillingFeatures

- [Get](#componentcurrentbillingfeatures_get)
- [Update](#componentcurrentbillingfeatures_update)

## ComponentFeatureCapabilities

- [Get](#componentfeaturecapabilities_get)

## ComponentLinkedStorageAccountsOperation

- [CreateAndUpdate](#componentlinkedstorageaccountsoperation_createandupdate)
- [Delete](#componentlinkedstorageaccountsoperation_delete)
- [Get](#componentlinkedstorageaccountsoperation_get)
- [Update](#componentlinkedstorageaccountsoperation_update)

## ComponentQuotaStatus

- [Get](#componentquotastatus_get)

## Components

- [CreateOrUpdate](#components_createorupdate)
- [Delete](#components_delete)
- [GetByResourceGroup](#components_getbyresourcegroup)
- [GetPurgeStatus](#components_getpurgestatus)
- [List](#components_list)
- [ListByResourceGroup](#components_listbyresourcegroup)
- [Purge](#components_purge)
- [UpdateTags](#components_updatetags)

## ExportConfigurations

- [Create](#exportconfigurations_create)
- [Delete](#exportconfigurations_delete)
- [Get](#exportconfigurations_get)
- [List](#exportconfigurations_list)
- [Update](#exportconfigurations_update)

## Favorites

- [Add](#favorites_add)
- [Delete](#favorites_delete)
- [Get](#favorites_get)
- [List](#favorites_list)
- [Update](#favorites_update)

## LiveToken

- [Get](#livetoken_get)

## MyWorkbooks

- [CreateOrUpdate](#myworkbooks_createorupdate)
- [Delete](#myworkbooks_delete)
- [GetByResourceGroup](#myworkbooks_getbyresourcegroup)
- [List](#myworkbooks_list)
- [ListByResourceGroup](#myworkbooks_listbyresourcegroup)
- [Update](#myworkbooks_update)

## ProactiveDetectionConfigurations

- [Get](#proactivedetectionconfigurations_get)
- [List](#proactivedetectionconfigurations_list)
- [Update](#proactivedetectionconfigurations_update)

## WebTestLocations

- [List](#webtestlocations_list)

## WebTests

- [CreateOrUpdate](#webtests_createorupdate)
- [Delete](#webtests_delete)
- [GetByResourceGroup](#webtests_getbyresourcegroup)
- [List](#webtests_list)
- [ListByComponent](#webtests_listbycomponent)
- [ListByResourceGroup](#webtests_listbyresourcegroup)
- [UpdateTags](#webtests_updatetags)

## WorkItemConfigurations

- [Create](#workitemconfigurations_create)
- [Delete](#workitemconfigurations_delete)
- [GetDefault](#workitemconfigurations_getdefault)
- [GetItem](#workitemconfigurations_getitem)
- [List](#workitemconfigurations_list)
- [UpdateItem](#workitemconfigurations_updateitem)

## WorkbookTemplates

- [CreateOrUpdate](#workbooktemplates_createorupdate)
- [Delete](#workbooktemplates_delete)
- [GetByResourceGroup](#workbooktemplates_getbyresourcegroup)
- [ListByResourceGroup](#workbooktemplates_listbyresourcegroup)
- [Update](#workbooktemplates_update)

## Workbooks

- [CreateOrUpdate](#workbooks_createorupdate)
- [Delete](#workbooks_delete)
- [GetByResourceGroup](#workbooks_getbyresourcegroup)
- [List](#workbooks_list)
- [ListByResourceGroup](#workbooks_listbyresourcegroup)
- [RevisionGet](#workbooks_revisionget)
- [RevisionsList](#workbooks_revisionslist)
- [Update](#workbooks_update)
### AnalyticsItems_Delete

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.models.ItemScopePath;

/** Samples for AnalyticsItems Delete. */
public final class AnalyticsItemsDeleteSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/AnalyticsItemDelete.json
     */
    /**
     * Sample code: AnalyticsItemDelete.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void analyticsItemDelete(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .analyticsItems()
            .deleteWithResponse(
                "my-resource-group",
                "my-component",
                ItemScopePath.ANALYTICS_ITEMS,
                "3466c160-4a10-4df8-afdf-0007f3f6dee5",
                null,
                Context.NONE);
    }
}
```

### AnalyticsItems_Get

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.models.ItemScopePath;

/** Samples for AnalyticsItems Get. */
public final class AnalyticsItemsGetSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/AnalyticsItemGet.json
     */
    /**
     * Sample code: AnalyticsItemGet.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void analyticsItemGet(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .analyticsItems()
            .getWithResponse(
                "my-resource-group",
                "my-component",
                ItemScopePath.ANALYTICS_ITEMS,
                "3466c160-4a10-4df8-afdf-0007f3f6dee5",
                null,
                Context.NONE);
    }
}
```

### AnalyticsItems_List

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.models.ItemScopePath;

/** Samples for AnalyticsItems List. */
public final class AnalyticsItemsListSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/AnalyticsItemList.json
     */
    /**
     * Sample code: AnalyticsItemList.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void analyticsItemList(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .analyticsItems()
            .listWithResponse(
                "my-resource-group", "my-component", ItemScopePath.ANALYTICS_ITEMS, null, null, null, Context.NONE);
    }
}
```

### AnalyticsItems_Put

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.fluent.models.ApplicationInsightsComponentAnalyticsItemInner;
import com.azure.resourcemanager.applicationinsights.models.ItemScope;
import com.azure.resourcemanager.applicationinsights.models.ItemScopePath;
import com.azure.resourcemanager.applicationinsights.models.ItemType;

/** Samples for AnalyticsItems Put. */
public final class AnalyticsItemsPutSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/AnalyticsItemPut.json
     */
    /**
     * Sample code: AnalyticsItemPut.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void analyticsItemPut(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .analyticsItems()
            .putWithResponse(
                "my-resource-group",
                "my-component",
                ItemScopePath.ANALYTICS_ITEMS,
                new ApplicationInsightsComponentAnalyticsItemInner()
                    .withName("Exceptions - New in the last 24 hours")
                    .withContent(
                        "let newExceptionsTimeRange = 1d;\n"
                            + "let timeRangeToCheckBefore = 7d;\n"
                            + "exceptions\n"
                            + "| where timestamp < ago(timeRangeToCheckBefore)\n"
                            + "| summarize count() by problemId\n"
                            + "| join kind= rightanti (\n"
                            + "exceptions\n"
                            + "| where timestamp >= ago(newExceptionsTimeRange)\n"
                            + "| extend stack = tostring(details[0].rawStack)\n"
                            + "| summarize count(), dcount(user_AuthenticatedId), min(timestamp), max(timestamp),"
                            + " any(stack) by problemId  \n"
                            + ") on problemId \n"
                            + "| order by  count_ desc\n")
                    .withScope(ItemScope.SHARED)
                    .withType(ItemType.QUERY),
                null,
                Context.NONE);
    }
}
```

### Annotations_Create

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.fluent.models.AnnotationInner;
import java.time.OffsetDateTime;

/** Samples for Annotations Create. */
public final class AnnotationsCreateSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/AnnotationsCreate.json
     */
    /**
     * Sample code: AnnotationsCreate.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void annotationsCreate(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .annotations()
            .createWithResponse(
                "my-resource-group",
                "my-component",
                new AnnotationInner()
                    .withAnnotationName("TestAnnotation")
                    .withCategory("Text")
                    .withEventTime(OffsetDateTime.parse("2018-01-31T13:41:38.657Z"))
                    .withId("444e2c08-274a-4bbb-a89e-d77bb720f44a")
                    .withProperties("{\"Comments\":\"Testing\",\"Label\":\"Success\"}"),
                Context.NONE);
    }
}
```

### Annotations_Delete

```java
import com.azure.core.util.Context;

/** Samples for Annotations Delete. */
public final class AnnotationsDeleteSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/AnnotationsDelete.json
     */
    /**
     * Sample code: AnnotationsDelete.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void annotationsDelete(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .annotations()
            .deleteWithResponse(
                "my-resource-group", "my-component", "bb820f1b-3110-4a8b-ba2c-8c1129d7eb6a", Context.NONE);
    }
}
```

### Annotations_Get

```java
import com.azure.core.util.Context;

/** Samples for Annotations Get. */
public final class AnnotationsGetSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/AnnotationsGet.json
     */
    /**
     * Sample code: AnnotationsGet.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void annotationsGet(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .annotations()
            .getWithResponse("my-resource-group", "my-component", "444e2c08-274a-4bbb-a89e-d77bb720f44a", Context.NONE);
    }
}
```

### Annotations_List

```java
import com.azure.core.util.Context;

/** Samples for Annotations List. */
public final class AnnotationsListSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/AnnotationsList.json
     */
    /**
     * Sample code: AnnotationsList.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void annotationsList(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .annotations()
            .list(
                "my-resource-group",
                "my-component",
                "2018-02-05T00%3A30%3A00.000Z",
                "2018-02-06T00%3A33A00.000Z",
                Context.NONE);
    }
}
```

### ApiKeys_Create

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.models.ApiKeyRequest;
import java.util.Arrays;

/** Samples for ApiKeys Create. */
public final class ApiKeysCreateSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/APIKeysCreate.json
     */
    /**
     * Sample code: APIKeyCreate.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void aPIKeyCreate(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .apiKeys()
            .createWithResponse(
                "my-resource-group",
                "my-component",
                new ApiKeyRequest()
                    .withName("test2")
                    .withLinkedReadProperties(
                        Arrays
                            .asList(
                                "/subscriptions/subid/resourceGroups/my-resource-group/providers/Microsoft.Insights/components/my-component/api",
                                "/subscriptions/subid/resourceGroups/my-resource-group/providers/Microsoft.Insights/components/my-component/agentconfig"))
                    .withLinkedWriteProperties(
                        Arrays
                            .asList(
                                "/subscriptions/subid/resourceGroups/my-resource-group/providers/Microsoft.Insights/components/my-component/annotations")),
                Context.NONE);
    }
}
```

### ApiKeys_Delete

```java
import com.azure.core.util.Context;

/** Samples for ApiKeys Delete. */
public final class ApiKeysDeleteSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/APIKeysDelete.json
     */
    /**
     * Sample code: APIKeyDelete.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void aPIKeyDelete(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .apiKeys()
            .deleteWithResponse(
                "my-resource-group", "my-component", "bb820f1b-3110-4a8b-ba2c-8c1129d7eb6a", Context.NONE);
    }
}
```

### ApiKeys_Get

```java
import com.azure.core.util.Context;

/** Samples for ApiKeys Get. */
public final class ApiKeysGetSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/APIKeysGet.json
     */
    /**
     * Sample code: APIKeysGet.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void aPIKeysGet(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .apiKeys()
            .getWithResponse("my-resource-group", "my-component", "bb820f1b-3110-4a8b-ba2c-8c1129d7eb6a", Context.NONE);
    }
}
```

### ApiKeys_List

```java
import com.azure.core.util.Context;

/** Samples for ApiKeys List. */
public final class ApiKeysListSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/APIKeysList.json
     */
    /**
     * Sample code: APIKeysList.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void aPIKeysList(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.apiKeys().list("my-resource-group", "my-component", Context.NONE);
    }
}
```

### ComponentAvailableFeatures_Get

```java
import com.azure.core.util.Context;

/** Samples for ComponentAvailableFeatures Get. */
public final class ComponentAvailableFeaturesGetSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/AvailableBillingFeaturesGet.json
     */
    /**
     * Sample code: ComponentCurrentBillingFeaturesGet.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void componentCurrentBillingFeaturesGet(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.componentAvailableFeatures().getWithResponse("my-resource-group", "my-component", Context.NONE);
    }
}
```

### ComponentCurrentBillingFeatures_Get

```java
import com.azure.core.util.Context;

/** Samples for ComponentCurrentBillingFeatures Get. */
public final class ComponentCurrentBillingFeaturesGetSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/CurrentBillingFeaturesGet.json
     */
    /**
     * Sample code: ComponentCurrentBillingFeaturesGet.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void componentCurrentBillingFeaturesGet(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.componentCurrentBillingFeatures().getWithResponse("my-resource-group", "my-component", Context.NONE);
    }
}
```

### ComponentCurrentBillingFeatures_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.fluent.models.ApplicationInsightsComponentBillingFeaturesInner;
import com.azure.resourcemanager.applicationinsights.models.ApplicationInsightsComponentDataVolumeCap;
import java.util.Arrays;

/** Samples for ComponentCurrentBillingFeatures Update. */
public final class ComponentCurrentBillingFeaturesUpdateSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/CurrentBillingFeaturesUpdate.json
     */
    /**
     * Sample code: ComponentCurrentBillingFeaturesUpdate.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void componentCurrentBillingFeaturesUpdate(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .componentCurrentBillingFeatures()
            .updateWithResponse(
                "my-resource-group",
                "my-component",
                new ApplicationInsightsComponentBillingFeaturesInner()
                    .withDataVolumeCap(
                        new ApplicationInsightsComponentDataVolumeCap()
                            .withCap(100.0f)
                            .withStopSendNotificationWhenHitCap(true))
                    .withCurrentBillingFeatures(Arrays.asList("Basic", "Application Insights Enterprise")),
                Context.NONE);
    }
}
```

### ComponentFeatureCapabilities_Get

```java
import com.azure.core.util.Context;

/** Samples for ComponentFeatureCapabilities Get. */
public final class ComponentFeatureCapabilitiesGetSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/FeatureCapabilitiesGet.json
     */
    /**
     * Sample code: ComponentCurrentBillingFeaturesGet.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void componentCurrentBillingFeaturesGet(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.componentFeatureCapabilities().getWithResponse("my-resource-group", "my-component", Context.NONE);
    }
}
```

### ComponentLinkedStorageAccountsOperation_CreateAndUpdate

```java
import com.azure.resourcemanager.applicationinsights.models.StorageType;

/** Samples for ComponentLinkedStorageAccountsOperation CreateAndUpdate. */
public final class ComponentLinkedStorageAccountsOperationCreateAndUpdateSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/preview/2020-03-01-preview/examples/ComponentLinkedStorageAccountsCreateAndUpdate.json
     */
    /**
     * Sample code: ComponentLinkedStorageAccountsCreateAndUpdate.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void componentLinkedStorageAccountsCreateAndUpdate(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .componentLinkedStorageAccountsOperations()
            .define(StorageType.SERVICE_PROFILER)
            .withExistingComponent("someResourceGroupName", "myComponent")
            .withLinkedStorageAccount(
                "/subscriptions/86dc51d3-92ed-4d7e-947a-775ea79b4918/resourceGroups/someResourceGroupName/providers/Microsoft.Storage/storageAccounts/storageaccountname")
            .create();
    }
}
```

### ComponentLinkedStorageAccountsOperation_Delete

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.models.StorageType;

/** Samples for ComponentLinkedStorageAccountsOperation Delete. */
public final class ComponentLinkedStorageAccountsOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/preview/2020-03-01-preview/examples/ComponentLinkedStorageAccountsDelete.json
     */
    /**
     * Sample code: ComponentLinkedStorageAccountsDelete.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void componentLinkedStorageAccountsDelete(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .componentLinkedStorageAccountsOperations()
            .deleteWithResponse("someResourceGroupName", "myComponent", StorageType.SERVICE_PROFILER, Context.NONE);
    }
}
```

### ComponentLinkedStorageAccountsOperation_Get

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.models.StorageType;

/** Samples for ComponentLinkedStorageAccountsOperation Get. */
public final class ComponentLinkedStorageAccountsOperationGetSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/preview/2020-03-01-preview/examples/ComponentLinkedStorageAccountsGet.json
     */
    /**
     * Sample code: ComponentLinkedStorageAccountsGet.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void componentLinkedStorageAccountsGet(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .componentLinkedStorageAccountsOperations()
            .getWithResponse("someResourceGroupName", "myComponent", StorageType.SERVICE_PROFILER, Context.NONE);
    }
}
```

### ComponentLinkedStorageAccountsOperation_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.models.ComponentLinkedStorageAccounts;
import com.azure.resourcemanager.applicationinsights.models.StorageType;

/** Samples for ComponentLinkedStorageAccountsOperation Update. */
public final class ComponentLinkedStorageAccountsOperationUpdateSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/preview/2020-03-01-preview/examples/ComponentLinkedStorageAccountsUpdate.json
     */
    /**
     * Sample code: ComponentLinkedStorageAccountsUpdate.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void componentLinkedStorageAccountsUpdate(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        ComponentLinkedStorageAccounts resource =
            manager
                .componentLinkedStorageAccountsOperations()
                .getWithResponse("someResourceGroupName", "myComponent", StorageType.SERVICE_PROFILER, Context.NONE)
                .getValue();
        resource
            .update()
            .withLinkedStorageAccount(
                "/subscriptions/86dc51d3-92ed-4d7e-947a-775ea79b4918/resourceGroups/someResourceGroupName/providers/Microsoft.Storage/storageAccounts/storageaccountname")
            .apply();
    }
}
```

### ComponentQuotaStatus_Get

```java
import com.azure.core.util.Context;

/** Samples for ComponentQuotaStatus Get. */
public final class ComponentQuotaStatusGetSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/QuotaStatusGet.json
     */
    /**
     * Sample code: ComponentCurrentBillingFeaturesGet.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void componentCurrentBillingFeaturesGet(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.componentQuotaStatus().getWithResponse("my-resource-group", "my-component", Context.NONE);
    }
}
```

### Components_CreateOrUpdate

```java
import com.azure.resourcemanager.applicationinsights.models.ApplicationType;
import com.azure.resourcemanager.applicationinsights.models.FlowType;
import com.azure.resourcemanager.applicationinsights.models.RequestSource;
import java.util.HashMap;
import java.util.Map;

/** Samples for Components CreateOrUpdate. */
public final class ComponentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2020-02-02/examples/ComponentsCreate.json
     */
    /**
     * Sample code: ComponentCreate.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void componentCreate(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .components()
            .define("my-component")
            .withRegion("South Central US")
            .withExistingResourceGroup("my-resource-group")
            .withKind("web")
            .withApplicationType(ApplicationType.WEB)
            .withFlowType(FlowType.BLUEFIELD)
            .withRequestSource(RequestSource.REST)
            .withWorkspaceResourceId(
                "/subscriptions/subid/resourcegroups/my-resource-group/providers/microsoft.operationalinsights/workspaces/my-workspace")
            .create();
    }

    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2020-02-02/examples/ComponentsUpdate.json
     */
    /**
     * Sample code: ComponentUpdate.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void componentUpdate(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .components()
            .define("my-component")
            .withRegion("South Central US")
            .withExistingResourceGroup("my-resource-group")
            .withKind("web")
            .withTags(mapOf("ApplicationGatewayType", "Internal-Only", "BillingEntity", "Self"))
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

### Components_Delete

```java
import com.azure.core.util.Context;

/** Samples for Components Delete. */
public final class ComponentsDeleteSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2020-02-02/examples/ComponentsDelete.json
     */
    /**
     * Sample code: ComponentsDelete.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void componentsDelete(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.components().deleteWithResponse("my-resource-group", "my-component", Context.NONE);
    }
}
```

### Components_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Components GetByResourceGroup. */
public final class ComponentsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2020-02-02/examples/ComponentsGet.json
     */
    /**
     * Sample code: ComponentGet.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void componentGet(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.components().getByResourceGroupWithResponse("my-resource-group", "my-component", Context.NONE);
    }
}
```

### Components_GetPurgeStatus

```java
import com.azure.core.util.Context;

/** Samples for Components GetPurgeStatus. */
public final class ComponentsGetPurgeStatusSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2020-02-02/examples/ComponentsPurgeStatus.json
     */
    /**
     * Sample code: ComponentPurge.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void componentPurge(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .components()
            .getPurgeStatusWithResponse(
                "OIAutoRest5123", "aztest5048", "purge-970318e7-b859-4edb-8903-83b1b54d0b74", Context.NONE);
    }
}
```

### Components_List

```java
import com.azure.core.util.Context;

/** Samples for Components List. */
public final class ComponentsListSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2020-02-02/examples/ComponentsList.json
     */
    /**
     * Sample code: ComponentsList.json.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void componentsListJson(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.components().list(Context.NONE);
    }
}
```

### Components_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Components ListByResourceGroup. */
public final class ComponentsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2020-02-02/examples/ComponentsListByResourceGroup.json
     */
    /**
     * Sample code: ComponentListByResourceGroup.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void componentListByResourceGroup(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.components().listByResourceGroup("my-resource-group", Context.NONE);
    }
}
```

### Components_Purge

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.models.ComponentPurgeBody;
import com.azure.resourcemanager.applicationinsights.models.ComponentPurgeBodyFilters;
import java.util.Arrays;

/** Samples for Components Purge. */
public final class ComponentsPurgeSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2020-02-02/examples/ComponentsPurge.json
     */
    /**
     * Sample code: ComponentPurge.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void componentPurge(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .components()
            .purgeWithResponse(
                "OIAutoRest5123",
                "aztest5048",
                new ComponentPurgeBody()
                    .withTable("Heartbeat")
                    .withFilters(
                        Arrays
                            .asList(
                                new ComponentPurgeBodyFilters()
                                    .withColumn("TimeGenerated")
                                    .withOperator(">")
                                    .withValue("2017-09-01T00:00:00"))),
                Context.NONE);
    }
}
```

### Components_UpdateTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.models.ApplicationInsightsComponent;
import java.util.HashMap;
import java.util.Map;

/** Samples for Components UpdateTags. */
public final class ComponentsUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2020-02-02/examples/ComponentsUpdateTagsOnly.json
     */
    /**
     * Sample code: ComponentUpdateTagsOnly.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void componentUpdateTagsOnly(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        ApplicationInsightsComponent resource =
            manager
                .components()
                .getByResourceGroupWithResponse("my-resource-group", "my-component", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(
                mapOf(
                    "ApplicationGatewayType",
                    "Internal-Only",
                    "BillingEntity",
                    "Self",
                    "Color",
                    "AzureBlue",
                    "CustomField_01",
                    "Custom text in some random field named randomly",
                    "NodeType",
                    "Edge"))
            .apply();
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

### ExportConfigurations_Create

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.models.ApplicationInsightsComponentExportRequest;

/** Samples for ExportConfigurations Create. */
public final class ExportConfigurationsCreateSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/ExportConfigurationsPost.json
     */
    /**
     * Sample code: ExportConfigurationPost.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void exportConfigurationPost(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .exportConfigurations()
            .createWithResponse(
                "my-resource-group",
                "my-component",
                new ApplicationInsightsComponentExportRequest()
                    .withRecordTypes(
                        "Requests, Event, Exceptions, Metrics, PageViews, PageViewPerformance, Rdd,"
                            + " PerformanceCounters, Availability")
                    .withDestinationType("Blob")
                    .withDestinationAddress(
                        "https://mystorageblob.blob.core.windows.net/testexport?sv=2015-04-05&sr=c&sig=token")
                    .withIsEnabled("true")
                    .withNotificationQueueEnabled("false")
                    .withNotificationQueueUri("")
                    .withDestinationStorageSubscriptionId("subid")
                    .withDestinationStorageLocationId("eastus")
                    .withDestinationAccountId(
                        "/subscriptions/subid/resourceGroups/my-resource-group/providers/Microsoft.ClassicStorage/storageAccounts/mystorageblob"),
                Context.NONE);
    }
}
```

### ExportConfigurations_Delete

```java
import com.azure.core.util.Context;

/** Samples for ExportConfigurations Delete. */
public final class ExportConfigurationsDeleteSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/ExportConfigurationDelete.json
     */
    /**
     * Sample code: ExportConfigurationDelete.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void exportConfigurationDelete(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .exportConfigurations()
            .deleteWithResponse("my-resource-group", "my-component", "uGOoki0jQsyEs3IdQ83Q4QsNr4=", Context.NONE);
    }
}
```

### ExportConfigurations_Get

```java
import com.azure.core.util.Context;

/** Samples for ExportConfigurations Get. */
public final class ExportConfigurationsGetSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/ExportConfigurationGet.json
     */
    /**
     * Sample code: ExportConfigurationGet.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void exportConfigurationGet(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .exportConfigurations()
            .getWithResponse("my-resource-group", "my-component", "uGOoki0jQsyEs3IdQ83Q4QsNr4=", Context.NONE);
    }
}
```

### ExportConfigurations_List

```java
import com.azure.core.util.Context;

/** Samples for ExportConfigurations List. */
public final class ExportConfigurationsListSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/ExportConfigurationsList.json
     */
    /**
     * Sample code: ExportConfigurationsList.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void exportConfigurationsList(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.exportConfigurations().listWithResponse("my-resource-group", "my-component", Context.NONE);
    }
}
```

### ExportConfigurations_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.models.ApplicationInsightsComponentExportRequest;

/** Samples for ExportConfigurations Update. */
public final class ExportConfigurationsUpdateSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/ExportConfigurationUpdate.json
     */
    /**
     * Sample code: ExportConfigurationUpdate.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void exportConfigurationUpdate(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .exportConfigurations()
            .updateWithResponse(
                "my-resource-group",
                "my-component",
                "uGOoki0jQsyEs3IdQ83Q4QsNr4=",
                new ApplicationInsightsComponentExportRequest()
                    .withRecordTypes(
                        "Requests, Event, Exceptions, Metrics, PageViews, PageViewPerformance, Rdd,"
                            + " PerformanceCounters, Availability")
                    .withDestinationType("Blob")
                    .withDestinationAddress(
                        "https://mystorageblob.blob.core.windows.net/fchentest?sv=2015-04-05&sr=c&sig=token")
                    .withIsEnabled("true")
                    .withNotificationQueueEnabled("false")
                    .withNotificationQueueUri("")
                    .withDestinationStorageSubscriptionId("subid")
                    .withDestinationStorageLocationId("eastus")
                    .withDestinationAccountId(
                        "/subscriptions/subid/resourceGroups/my-resource-group/providers/Microsoft.ClassicStorage/storageAccounts/mystorageblob"),
                Context.NONE);
    }
}
```

### Favorites_Add

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.fluent.models.ApplicationInsightsComponentFavoriteInner;
import com.azure.resourcemanager.applicationinsights.models.FavoriteType;
import java.util.Arrays;

/** Samples for Favorites Add. */
public final class FavoritesAddSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/FavoriteAdd.json
     */
    /**
     * Sample code: FavoriteAdd.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void favoriteAdd(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .favorites()
            .addWithResponse(
                "my-resource-group",
                "my-ai-component",
                "deadb33f-8bee-4d3b-a059-9be8dac93960",
                new ApplicationInsightsComponentFavoriteInner()
                    .withName("Blah Blah Blah")
                    .withConfig(
                        "{\"MEDataModelRawJSON\":\"{\\n"
                            + "  \\\"version\\\": \\\"1.4.1\\\",\\n"
                            + "  \\\"isCustomDataModel\\\": true,\\n"
                            + "  \\\"items\\\": [\\n"
                            + "    {\\n"
                            + "      \\\"id\\\": \\\"90a7134d-9a38-4c25-88d3-a495209873eb\\\",\\n"
                            + "      \\\"chartType\\\": \\\"Area\\\",\\n"
                            + "      \\\"chartHeight\\\": 4,\\n"
                            + "      \\\"metrics\\\": [\\n"
                            + "        {\\n"
                            + "          \\\"id\\\": \\\"preview/requests/count\\\",\\n"
                            + "          \\\"metricAggregation\\\": \\\"Sum\\\",\\n"
                            + "          \\\"color\\\": \\\"msportalfx-bgcolor-d0\\\"\\n"
                            + "        }\\n"
                            + "      ],\\n"
                            + "      \\\"priorPeriod\\\": false,\\n"
                            + "      \\\"clickAction\\\": {\\n"
                            + "        \\\"defaultBlade\\\": \\\"SearchBlade\\\"\\n"
                            + "      },\\n"
                            + "      \\\"horizontalBars\\\": true,\\n"
                            + "      \\\"showOther\\\": true,\\n"
                            + "      \\\"aggregation\\\": \\\"Sum\\\",\\n"
                            + "      \\\"percentage\\\": false,\\n"
                            + "      \\\"palette\\\": \\\"fail\\\",\\n"
                            + "      \\\"yAxisOption\\\": 0,\\n"
                            + "      \\\"title\\\": \\\"\\\"\\n"
                            + "    },\\n"
                            + "    {\\n"
                            + "      \\\"id\\\": \\\"0c289098-88e8-4010-b212-546815cddf70\\\",\\n"
                            + "      \\\"chartType\\\": \\\"Area\\\",\\n"
                            + "      \\\"chartHeight\\\": 2,\\n"
                            + "      \\\"metrics\\\": [\\n"
                            + "        {\\n"
                            + "          \\\"id\\\": \\\"preview/requests/duration\\\",\\n"
                            + "          \\\"metricAggregation\\\": \\\"Avg\\\",\\n"
                            + "          \\\"color\\\": \\\"msportalfx-bgcolor-j1\\\"\\n"
                            + "        }\\n"
                            + "      ],\\n"
                            + "      \\\"priorPeriod\\\": false,\\n"
                            + "      \\\"clickAction\\\": {\\n"
                            + "        \\\"defaultBlade\\\": \\\"SearchBlade\\\"\\n"
                            + "      },\\n"
                            + "      \\\"horizontalBars\\\": true,\\n"
                            + "      \\\"showOther\\\": true,\\n"
                            + "      \\\"aggregation\\\": \\\"Avg\\\",\\n"
                            + "      \\\"percentage\\\": false,\\n"
                            + "      \\\"palette\\\": \\\"greenHues\\\",\\n"
                            + "      \\\"yAxisOption\\\": 0,\\n"
                            + "      \\\"title\\\": \\\"\\\"\\n"
                            + "    },\\n"
                            + "    {\\n"
                            + "      \\\"id\\\": \\\"cbdaab6f-a808-4f71-aca5-b3976cbb7345\\\",\\n"
                            + "      \\\"chartType\\\": \\\"Bar\\\",\\n"
                            + "      \\\"chartHeight\\\": 4,\\n"
                            + "      \\\"metrics\\\": [\\n"
                            + "        {\\n"
                            + "          \\\"id\\\": \\\"preview/requests/duration\\\",\\n"
                            + "          \\\"metricAggregation\\\": \\\"Avg\\\",\\n"
                            + "          \\\"color\\\": \\\"msportalfx-bgcolor-d0\\\"\\n"
                            + "        }\\n"
                            + "      ],\\n"
                            + "      \\\"priorPeriod\\\": false,\\n"
                            + "      \\\"clickAction\\\": {\\n"
                            + "        \\\"defaultBlade\\\": \\\"SearchBlade\\\"\\n"
                            + "      },\\n"
                            + "      \\\"horizontalBars\\\": true,\\n"
                            + "      \\\"showOther\\\": true,\\n"
                            + "      \\\"aggregation\\\": \\\"Avg\\\",\\n"
                            + "      \\\"percentage\\\": false,\\n"
                            + "      \\\"palette\\\": \\\"magentaHues\\\",\\n"
                            + "      \\\"yAxisOption\\\": 0,\\n"
                            + "      \\\"title\\\": \\\"\\\"\\n"
                            + "    },\\n"
                            + "    {\\n"
                            + "      \\\"id\\\": \\\"1d5a6a3a-9fa1-4099-9cf9-05eff72d1b02\\\",\\n"
                            + "      \\\"grouping\\\": {\\n"
                            + "        \\\"kind\\\": \\\"ByDimension\\\",\\n"
                            + "        \\\"dimension\\\": \\\"context.application.version\\\"\\n"
                            + "      },\\n"
                            + "      \\\"chartType\\\": \\\"Grid\\\",\\n"
                            + "      \\\"chartHeight\\\": 1,\\n"
                            + "      \\\"metrics\\\": [\\n"
                            + "        {\\n"
                            + "          \\\"id\\\": \\\"basicException.count\\\",\\n"
                            + "          \\\"metricAggregation\\\": \\\"Sum\\\",\\n"
                            + "          \\\"color\\\": \\\"msportalfx-bgcolor-g0\\\"\\n"
                            + "        },\\n"
                            + "        {\\n"
                            + "          \\\"id\\\": \\\"requestFailed.count\\\",\\n"
                            + "          \\\"metricAggregation\\\": \\\"Sum\\\",\\n"
                            + "          \\\"color\\\": \\\"msportalfx-bgcolor-f0s2\\\"\\n"
                            + "        }\\n"
                            + "      ],\\n"
                            + "      \\\"priorPeriod\\\": true,\\n"
                            + "      \\\"clickAction\\\": {\\n"
                            + "        \\\"defaultBlade\\\": \\\"SearchBlade\\\"\\n"
                            + "      },\\n"
                            + "      \\\"horizontalBars\\\": true,\\n"
                            + "      \\\"showOther\\\": true,\\n"
                            + "      \\\"percentage\\\": false,\\n"
                            + "      \\\"palette\\\": \\\"blueHues\\\",\\n"
                            + "      \\\"yAxisOption\\\": 0,\\n"
                            + "      \\\"title\\\": \\\"\\\"\\n"
                            + "    }\\n"
                            + "  ],\\n"
                            + "  \\\"currentFilter\\\": {\\n"
                            + "    \\\"eventTypes\\\": [\\n"
                            + "      1,\\n"
                            + "      2\\n"
                            + "    ],\\n"
                            + "    \\\"typeFacets\\\": {},\\n"
                            + "    \\\"isPermissive\\\": false\\n"
                            + "  },\\n"
                            + "  \\\"timeContext\\\": {\\n"
                            + "    \\\"durationMs\\\": 75600000,\\n"
                            + "    \\\"endTime\\\": \\\"2018-01-31T20:30:00.000Z\\\",\\n"
                            + "    \\\"createdTime\\\": \\\"2018-01-31T23:54:26.280Z\\\",\\n"
                            + "    \\\"isInitialTime\\\": false,\\n"
                            + "    \\\"grain\\\": 1,\\n"
                            + "    \\\"useDashboardTimeRange\\\": false\\n"
                            + "  },\\n"
                            + "  \\\"jsonUri\\\": \\\"Favorite_BlankChart\\\",\\n"
                            + "  \\\"timeSource\\\": 0\\n"
                            + "}\"}")
                    .withVersion("ME")
                    .withFavoriteType(FavoriteType.SHARED)
                    .withTags(Arrays.asList("TagSample01", "TagSample02"))
                    .withIsGeneratedFromTemplate(false),
                Context.NONE);
    }
}
```

### Favorites_Delete

```java
import com.azure.core.util.Context;

/** Samples for Favorites Delete. */
public final class FavoritesDeleteSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/FavoriteDelete.json
     */
    /**
     * Sample code: FavoriteList.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void favoriteList(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .favorites()
            .deleteWithResponse(
                "my-resource-group", "my-ai-component", "deadb33f-5e0d-4064-8ebb-1a4ed0313eb2", Context.NONE);
    }
}
```

### Favorites_Get

```java
import com.azure.core.util.Context;

/** Samples for Favorites Get. */
public final class FavoritesGetSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/FavoriteGet.json
     */
    /**
     * Sample code: FavoriteGet.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void favoriteGet(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .favorites()
            .getWithResponse(
                "my-resource-group", "my-ai-component", "deadb33f-5e0d-4064-8ebb-1a4ed0313eb2", Context.NONE);
    }
}
```

### Favorites_List

```java
import com.azure.core.util.Context;

/** Samples for Favorites List. */
public final class FavoritesListSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/FavoritesList.json
     */
    /**
     * Sample code: FavoritesList.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void favoritesList(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .favorites()
            .listWithResponse("my-resource-group", "my-ai-component", null, null, null, null, Context.NONE);
    }
}
```

### Favorites_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.fluent.models.ApplicationInsightsComponentFavoriteInner;
import com.azure.resourcemanager.applicationinsights.models.FavoriteType;
import java.util.Arrays;

/** Samples for Favorites Update. */
public final class FavoritesUpdateSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/FavoriteUpdate.json
     */
    /**
     * Sample code: FavoriteList.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void favoriteList(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .favorites()
            .updateWithResponse(
                "my-resource-group",
                "my-ai-component",
                "deadb33f-5e0d-4064-8ebb-1a4ed0313eb2",
                new ApplicationInsightsComponentFavoriteInner()
                    .withName("Derek Changed This")
                    .withConfig(
                        "{\"MEDataModelRawJSON\":\"{\\\"version\\\": \\\"1.4.1\\\",\\\"isCustomDataModel\\\":"
                            + " true,\\\"items\\\": [{\\\"id\\\":"
                            + " \\\"90a7134d-9a38-4c25-88d3-a495209873eb\\\",\\\"chartType\\\":"
                            + " \\\"Area\\\",\\\"chartHeight\\\": 4,\\\"metrics\\\": [{\\\"id\\\":"
                            + " \\\"preview/requests/count\\\",\\\"metricAggregation\\\": \\\"Sum\\\",\\\"color\\\":"
                            + " \\\"msportalfx-bgcolor-d0\\\"}],\\\"priorPeriod\\\": false,\\\"clickAction\\\":"
                            + " {\\\"defaultBlade\\\": \\\"SearchBlade\\\"},\\\"horizontalBars\\\":"
                            + " true,\\\"showOther\\\": true,\\\"aggregation\\\": \\\"Sum\\\",\\\"percentage\\\":"
                            + " false,\\\"palette\\\": \\\"fail\\\",\\\"yAxisOption\\\": 0,\\\"title\\\":"
                            + " \\\"\\\"},{\\\"id\\\": \\\"0c289098-88e8-4010-b212-546815cddf70\\\",\\\"chartType\\\":"
                            + " \\\"Area\\\",\\\"chartHeight\\\": 2,\\\"metrics\\\": [{\\\"id\\\":"
                            + " \\\"preview/requests/duration\\\",\\\"metricAggregation\\\": \\\"Avg\\\",\\\"color\\\":"
                            + " \\\"msportalfx-bgcolor-j1\\\"}],\\\"priorPeriod\\\": false,\\\"clickAction\\\":"
                            + " {\\\"defaultBlade\\\": \\\"SearchBlade\\\"},\\\"horizontalBars\\\":"
                            + " true,\\\"showOther\\\": true,\\\"aggregation\\\": \\\"Avg\\\",\\\"percentage\\\":"
                            + " false,\\\"palette\\\": \\\"greenHues\\\",\\\"yAxisOption\\\": 0,\\\"title\\\":"
                            + " \\\"\\\"},{\\\"id\\\": \\\"cbdaab6f-a808-4f71-aca5-b3976cbb7345\\\",\\\"chartType\\\":"
                            + " \\\"Bar\\\",\\\"chartHeight\\\": 4,\\\"metrics\\\": [{\\\"id\\\":"
                            + " \\\"preview/requests/duration\\\",\\\"metricAggregation\\\": \\\"Avg\\\",\\\"color\\\":"
                            + " \\\"msportalfx-bgcolor-d0\\\"}],\\\"priorPeriod\\\": false,\\\"clickAction\\\":"
                            + " {\\\"defaultBlade\\\": \\\"SearchBlade\\\"},\\\"horizontalBars\\\":"
                            + " true,\\\"showOther\\\": true,\\\"aggregation\\\": \\\"Avg\\\",\\\"percentage\\\":"
                            + " false,\\\"palette\\\": \\\"magentaHues\\\",\\\"yAxisOption\\\": 0,\\\"title\\\":"
                            + " \\\"\\\"},{\\\"id\\\": \\\"1d5a6a3a-9fa1-4099-9cf9-05eff72d1b02\\\",\\\"grouping\\\":"
                            + " {\\\"kind\\\": \\\"ByDimension\\\",\\\"dimension\\\":"
                            + " \\\"context.application.version\\\"},\\\"chartType\\\":"
                            + " \\\"Grid\\\",\\\"chartHeight\\\": 1,\\\"metrics\\\": [{\\\"id\\\":"
                            + " \\\"basicException.count\\\",\\\"metricAggregation\\\": \\\"Sum\\\",\\\"color\\\":"
                            + " \\\"msportalfx-bgcolor-g0\\\"},{\\\"id\\\":"
                            + " \\\"requestFailed.count\\\",\\\"metricAggregation\\\": \\\"Sum\\\",\\\"color\\\":"
                            + " \\\"msportalfx-bgcolor-f0s2\\\"}],\\\"priorPeriod\\\": true,\\\"clickAction\\\":"
                            + " {\\\"defaultBlade\\\": \\\"SearchBlade\\\"},\\\"horizontalBars\\\":"
                            + " true,\\\"showOther\\\": true,\\\"percentage\\\": false,\\\"palette\\\":"
                            + " \\\"blueHues\\\",\\\"yAxisOption\\\": 0,\\\"title\\\":"
                            + " \\\"\\\"}],\\\"currentFilter\\\": {\\\"eventTypes\\\": [1,2],\\\"typeFacets\\\":"
                            + " {},\\\"isPermissive\\\": false},\\\"timeContext\\\": {\\\"durationMs\\\":"
                            + " 75600000,\\\"endTime\\\": \\\"2018-01-31T20:30:00.000Z\\\",\\\"createdTime\\\":"
                            + " \\\"2018-01-31T23:54:26.280Z\\\",\\\"isInitialTime\\\": false,\\\"grain\\\":"
                            + " 1,\\\"useDashboardTimeRange\\\": false},\\\"jsonUri\\\":"
                            + " \\\"Favorite_BlankChart\\\",\\\"timeSource\\\": 0}\"}")
                    .withVersion("ME")
                    .withFavoriteType(FavoriteType.SHARED)
                    .withTags(Arrays.asList("TagSample01", "TagSample02", "TagSample03"))
                    .withIsGeneratedFromTemplate(false),
                Context.NONE);
    }
}
```

### LiveToken_Get

```java
import com.azure.core.util.Context;

/** Samples for LiveToken Get. */
public final class LiveTokenGetSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2021-10-14/examples/LiveTokenGet.json
     */
    /**
     * Sample code: Get live token for resource.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void getLiveTokenForResource(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .liveTokens()
            .getWithResponse(
                "subscriptions/df602c9c-7aa0-407d-a6fb-eb20c8bd1192/resourceGroups/FabrikamFiberApp/providers/microsoft.insights/components/CustomAvailabilityTest/providers/microsoft.insights/generatelivetoken",
                Context.NONE);
    }
}
```

### MyWorkbooks_CreateOrUpdate

```java
import com.azure.resourcemanager.applicationinsights.models.Kind;

/** Samples for MyWorkbooks CreateOrUpdate. */
public final class MyWorkbooksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2021-03-08/examples/MyWorkbookAdd.json
     */
    /**
     * Sample code: WorkbookAdd.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbookAdd(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .myWorkbooks()
            .define("deadb33f-8bee-4d3b-a059-9be8dac93960")
            .withRegion("west us")
            .withExistingResourceGroup("my-resource-group")
            .withName("deadb33f-8bee-4d3b-a059-9be8dac93960")
            .withKind(Kind.USER)
            .withDisplayName("Blah Blah Blah")
            .withSerializedData(
                "{\"version\":\"Notebook/1.0\",\"items\":[{\"type\":1,\"content\":\"{\"json\":\"## New workbook\\r"
                    + "\\n"
                    + "---\\r"
                    + "\\n"
                    + "\\r"
                    + "\\n"
                    + "Welcome to your new workbook.  This area will display text formatted as markdown.\\r"
                    + "\\n"
                    + "\\r"
                    + "\\n"
                    + "\\r"
                    + "\\n"
                    + "We've included a basic analytics query to get you started. Use the `Edit` button below each"
                    + " section to configure it or add more"
                    + " sections.\"}\",\"halfWidth\":null,\"conditionalVisibility\":null},{\"type\":3,\"content\":\"{\"version\":\"KqlItem/1.0\",\"query\":\"union"
                    + " withsource=TableName *\\n"
                    + "| summarize Count=count() by TableName\\n"
                    + "| render"
                    + " barchart\",\"showQuery\":false,\"size\":1,\"aggregation\":0,\"showAnnotations\":false}\",\"halfWidth\":null,\"conditionalVisibility\":null}],\"isLocked\":false}")
            .withCategory("workbook")
            .withSourceId(
                "/subscriptions/00000000-0000-0000-0000-00000000/resourceGroups/MyGroup/providers/Microsoft.Web/sites/MyTestApp-CodeLens")
            .create();
    }
}
```

### MyWorkbooks_Delete

```java
import com.azure.core.util.Context;

/** Samples for MyWorkbooks Delete. */
public final class MyWorkbooksDeleteSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2021-03-08/examples/MyWorkbookDelete.json
     */
    /**
     * Sample code: WorkbookDelete.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbookDelete(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .myWorkbooks()
            .deleteWithResponse("my-resource-group", "deadb33f-5e0d-4064-8ebb-1a4ed0313eb2", Context.NONE);
    }
}
```

### MyWorkbooks_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for MyWorkbooks GetByResourceGroup. */
public final class MyWorkbooksGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2021-03-08/examples/MyWorkbookGet.json
     */
    /**
     * Sample code: WorkbookGet.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbookGet(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .myWorkbooks()
            .getByResourceGroupWithResponse("my-resource-group", "deadb33f-5e0d-4064-8ebb-1a4ed0313eb2", Context.NONE);
    }
}
```

### MyWorkbooks_List

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.models.CategoryType;

/** Samples for MyWorkbooks List. */
public final class MyWorkbooksListSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2021-03-08/examples/MyWorkbooksList.json
     */
    /**
     * Sample code: WorkbooksList.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbooksList(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.myWorkbooks().list(CategoryType.WORKBOOK, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2021-03-08/examples/MyWorkbooksList2.json
     */
    /**
     * Sample code: WorkbooksList2.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbooksList2(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.myWorkbooks().list(CategoryType.WORKBOOK, null, null, Context.NONE);
    }
}
```

### MyWorkbooks_ListByResourceGroup

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.models.CategoryType;

/** Samples for MyWorkbooks ListByResourceGroup. */
public final class MyWorkbooksListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2021-03-08/examples/MyWorkbooksList.json
     */
    /**
     * Sample code: WorkbooksList.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbooksList(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .myWorkbooks()
            .listByResourceGroup("my-resource-group", CategoryType.WORKBOOK, null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2021-03-08/examples/MyWorkbooksList2.json
     */
    /**
     * Sample code: WorkbooksList2.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbooksList2(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .myWorkbooks()
            .listByResourceGroup("my-resource-group", CategoryType.WORKBOOK, null, null, null, Context.NONE);
    }
}
```

### MyWorkbooks_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.models.Kind;
import com.azure.resourcemanager.applicationinsights.models.MyWorkbook;

/** Samples for MyWorkbooks Update. */
public final class MyWorkbooksUpdateSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2021-03-08/examples/MyWorkbookUpdate.json
     */
    /**
     * Sample code: WorkbookUpdate.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbookUpdate(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        MyWorkbook resource =
            manager
                .myWorkbooks()
                .getByResourceGroupWithResponse(
                    "my-resource-group", "deadb33f-5e0d-4064-8ebb-1a4ed0313eb2", Context.NONE)
                .getValue();
        resource
            .update()
            .withKind(Kind.USER)
            .withDisplayName("Blah Blah Blah")
            .withSerializedData(
                "{\"version\":\"Notebook/1.0\",\"items\":[{\"type\":1,\"content\":\"{\"json\":\"## New workbook\\r"
                    + "\\n"
                    + "---\\r"
                    + "\\n"
                    + "\\r"
                    + "\\n"
                    + "Welcome to your new workbook.  This area will display text formatted as markdown.\\r"
                    + "\\n"
                    + "\\r"
                    + "\\n"
                    + "\\r"
                    + "\\n"
                    + "We've included a basic analytics query to get you started. Use the `Edit` button below each"
                    + " section to configure it or add more"
                    + " sections.\"}\",\"halfWidth\":null,\"conditionalVisibility\":null},{\"type\":3,\"content\":\"{\"version\":\"KqlItem/1.0\",\"query\":\"union"
                    + " withsource=TableName *\\n"
                    + "| summarize Count=count() by TableName\\n"
                    + "| render"
                    + " barchart\",\"showQuery\":false,\"size\":1,\"aggregation\":0,\"showAnnotations\":false}\",\"halfWidth\":null,\"conditionalVisibility\":null}],\"isLocked\":false}")
            .withVersion("ME")
            .withCategory("workbook")
            .withSourceId(
                "/subscriptions/00000000-0000-0000-0000-00000000/resourceGroups/MyGroup/providers/Microsoft.Web/sites/MyTestApp-CodeLens")
            .apply();
    }
}
```

### ProactiveDetectionConfigurations_Get

```java
import com.azure.core.util.Context;

/** Samples for ProactiveDetectionConfigurations Get. */
public final class ProactiveDetectionConfigurationsGetSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/ProactiveDetectionConfigurationGet.json
     */
    /**
     * Sample code: ProactiveDetectionConfigurationGet.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void proactiveDetectionConfigurationGet(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .proactiveDetectionConfigurations()
            .getWithResponse("my-resource-group", "my-component", "slowpageloadtime", Context.NONE);
    }
}
```

### ProactiveDetectionConfigurations_List

```java
import com.azure.core.util.Context;

/** Samples for ProactiveDetectionConfigurations List. */
public final class ProactiveDetectionConfigurationsListSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/ProactiveDetectionConfigurationsList.json
     */
    /**
     * Sample code: ProactiveDetectionConfigurationsList.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void proactiveDetectionConfigurationsList(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.proactiveDetectionConfigurations().listWithResponse("my-resource-group", "my-component", Context.NONE);
    }
}
```

### ProactiveDetectionConfigurations_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.fluent.models.ApplicationInsightsComponentProactiveDetectionConfigurationInner;
import com.azure.resourcemanager.applicationinsights.models.ApplicationInsightsComponentProactiveDetectionConfigurationRuleDefinitions;
import java.util.Arrays;

/** Samples for ProactiveDetectionConfigurations Update. */
public final class ProactiveDetectionConfigurationsUpdateSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/ProactiveDetectionConfigurationUpdate.json
     */
    /**
     * Sample code: ProactiveDetectionConfigurationUpdate.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void proactiveDetectionConfigurationUpdate(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .proactiveDetectionConfigurations()
            .updateWithResponse(
                "my-resource-group",
                "my-component",
                "slowpageloadtime",
                new ApplicationInsightsComponentProactiveDetectionConfigurationInner()
                    .withName("slowpageloadtime")
                    .withEnabled(true)
                    .withSendEmailsToSubscriptionOwners(true)
                    .withCustomEmails(Arrays.asList("foo@microsoft.com", "foo2@microsoft.com"))
                    .withRuleDefinitions(
                        new ApplicationInsightsComponentProactiveDetectionConfigurationRuleDefinitions()
                            .withName("slowpageloadtime")
                            .withDisplayName("Slow page load time")
                            .withDescription("Smart Detection rules notify you of performance anomaly issues.")
                            .withHelpUrl(
                                "https://docs.microsoft.com/en-us/azure/application-insights/app-insights-proactive-performance-diagnostics")
                            .withIsHidden(false)
                            .withIsEnabledByDefault(true)
                            .withIsInPreview(false)
                            .withSupportsEmailNotifications(true)),
                Context.NONE);
    }
}
```

### WebTestLocations_List

```java
import com.azure.core.util.Context;

/** Samples for WebTestLocations List. */
public final class WebTestLocationsListSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/WebTestLocationsList.json
     */
    /**
     * Sample code: WebTestLocationsList.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void webTestLocationsList(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.webTestLocations().list("my-resource-group", "my-component", Context.NONE);
    }
}
```

### WebTests_CreateOrUpdate

```java
import com.azure.resourcemanager.applicationinsights.models.WebTestGeolocation;
import com.azure.resourcemanager.applicationinsights.models.WebTestKind;
import com.azure.resourcemanager.applicationinsights.models.WebTestPropertiesConfiguration;
import java.util.Arrays;

/** Samples for WebTests CreateOrUpdate. */
public final class WebTestsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/WebTestCreate.json
     */
    /**
     * Sample code: webTestCreate.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void webTestCreate(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .webTests()
            .define("my-webtest-my-component")
            .withRegion("South Central US")
            .withExistingResourceGroup("my-resource-group")
            .withKind(WebTestKind.PING)
            .withSyntheticMonitorId("my-webtest-my-component")
            .withWebTestName("my-webtest-my-component")
            .withDescription("Ping web test alert for mytestwebapp")
            .withEnabled(true)
            .withFrequency(900)
            .withTimeout(120)
            .withWebTestKind(WebTestKind.PING)
            .withRetryEnabled(true)
            .withLocations(Arrays.asList(new WebTestGeolocation().withLocation("us-fl-mia-edge")))
            .withConfiguration(
                new WebTestPropertiesConfiguration()
                    .withWebTest(
                        "<WebTest Name=\"my-webtest\" Id=\"678ddf96-1ab8-44c8-9274-123456789abc\" Enabled=\"True\""
                            + " CssProjectStructure=\"\" CssIteration=\"\" Timeout=\"120\" WorkItemIds=\"\""
                            + " xmlns=\"http://microsoft.com/schemas/VisualStudio/TeamTest/2010\" Description=\"\""
                            + " CredentialUserName=\"\" CredentialPassword=\"\" PreAuthenticate=\"True\""
                            + " Proxy=\"default\" StopOnError=\"False\" RecordedResultFile=\"\" ResultsLocale=\"\""
                            + " ><Items><Request Method=\"GET\" Guid=\"a4162485-9114-fcfc-e086-123456789abc\""
                            + " Version=\"1.1\" Url=\"http://my-component.azurewebsites.net\" ThinkTime=\"0\""
                            + " Timeout=\"120\" ParseDependentRequests=\"True\" FollowRedirects=\"True\""
                            + " RecordResult=\"True\" Cache=\"False\" ResponseTimeGoal=\"0\" Encoding=\"utf-8\""
                            + " ExpectedHttpStatusCode=\"200\" ExpectedResponseUrl=\"\" ReportingName=\"\""
                            + " IgnoreHttpStatusCode=\"False\" /></Items></WebTest>"))
            .create();
    }

    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/WebTestUpdate.json
     */
    /**
     * Sample code: webTestUpdate.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void webTestUpdate(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .webTests()
            .define("my-webtest-my-component")
            .withRegion("South Central US")
            .withExistingResourceGroup("my-resource-group")
            .withKind(WebTestKind.PING)
            .withSyntheticMonitorId("my-webtest-my-component")
            .withWebTestName("my-webtest-my-component")
            .withFrequency(600)
            .withTimeout(30)
            .withWebTestKind(WebTestKind.PING)
            .withLocations(
                Arrays
                    .asList(
                        new WebTestGeolocation().withLocation("us-fl-mia-edge"),
                        new WebTestGeolocation().withLocation("apac-hk-hkn-azr")))
            .withConfiguration(
                new WebTestPropertiesConfiguration()
                    .withWebTest(
                        "<WebTest Name=\"my-webtest\" Id=\"678ddf96-1ab8-44c8-9274-123456789abc\" Enabled=\"True\""
                            + " CssProjectStructure=\"\" CssIteration=\"\" Timeout=\"30\" WorkItemIds=\"\""
                            + " xmlns=\"http://microsoft.com/schemas/VisualStudio/TeamTest/2010\" Description=\"\""
                            + " CredentialUserName=\"\" CredentialPassword=\"\" PreAuthenticate=\"True\""
                            + " Proxy=\"default\" StopOnError=\"False\" RecordedResultFile=\"\" ResultsLocale=\"\""
                            + " ><Items><Request Method=\"GET\" Guid=\"a4162485-9114-fcfc-e086-123456789abc\""
                            + " Version=\"1.1\" Url=\"http://my-component.azurewebsites.net\" ThinkTime=\"0\""
                            + " Timeout=\"30\" ParseDependentRequests=\"True\" FollowRedirects=\"True\""
                            + " RecordResult=\"True\" Cache=\"False\" ResponseTimeGoal=\"0\" Encoding=\"utf-8\""
                            + " ExpectedHttpStatusCode=\"200\" ExpectedResponseUrl=\"\" ReportingName=\"\""
                            + " IgnoreHttpStatusCode=\"False\" /></Items></WebTest>"))
            .create();
    }
}
```

### WebTests_Delete

```java
import com.azure.core.util.Context;

/** Samples for WebTests Delete. */
public final class WebTestsDeleteSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/WebTestDelete.json
     */
    /**
     * Sample code: webTestDelete.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void webTestDelete(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.webTests().deleteWithResponse("my-resource-group", "my-webtest-01-mywebservice", Context.NONE);
    }
}
```

### WebTests_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for WebTests GetByResourceGroup. */
public final class WebTestsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/WebTestGet.json
     */
    /**
     * Sample code: webTestGet.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void webTestGet(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .webTests()
            .getByResourceGroupWithResponse("my-resource-group", "my-webtest-01-mywebservice", Context.NONE);
    }
}
```

### WebTests_List

```java
import com.azure.core.util.Context;

/** Samples for WebTests List. */
public final class WebTestsListSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/WebTestList.json
     */
    /**
     * Sample code: webTestList.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void webTestList(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.webTests().list(Context.NONE);
    }
}
```

### WebTests_ListByComponent

```java
import com.azure.core.util.Context;

/** Samples for WebTests ListByComponent. */
public final class WebTestsListByComponentSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/WebTestListByComponent.json
     */
    /**
     * Sample code: webTestListByComponent.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void webTestListByComponent(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.webTests().listByComponent("my-component", "my-resource-group", Context.NONE);
    }
}
```

### WebTests_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for WebTests ListByResourceGroup. */
public final class WebTestsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/WebTestListByResourceGroup.json
     */
    /**
     * Sample code: webTestListByResourceGroup.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void webTestListByResourceGroup(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.webTests().listByResourceGroup("my-resource-group", Context.NONE);
    }
}
```

### WebTests_UpdateTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.models.WebTest;
import java.util.HashMap;
import java.util.Map;

/** Samples for WebTests UpdateTags. */
public final class WebTestsUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/WebTestUpdateTagsOnly.json
     */
    /**
     * Sample code: webTestUpdateTags.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void webTestUpdateTags(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        WebTest resource =
            manager
                .webTests()
                .getByResourceGroupWithResponse("my-resource-group", "my-webtest-my-component", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(
                mapOf(
                    "Color",
                    "AzureBlue",
                    "CustomField-01",
                    "This is a random value",
                    "SystemType",
                    "A08",
                    "hidden-link:/subscriptions/subid/resourceGroups/my-resource-group/providers/Microsoft.Insights/components/my-component",
                    "Resource",
                    "hidden-link:/subscriptions/subid/resourceGroups/my-resource-group/providers/Microsoft.Web/sites/mytestwebapp",
                    "Resource"))
            .apply();
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

### WorkItemConfigurations_Create

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.models.WorkItemCreateConfiguration;
import java.util.HashMap;
import java.util.Map;

/** Samples for WorkItemConfigurations Create. */
public final class WorkItemConfigurationsCreateSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/WorkItemConfigCreate.json
     */
    /**
     * Sample code: WorkItemConfigurationsCreate.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workItemConfigurationsCreate(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .workItemConfigurations()
            .createWithResponse(
                "my-resource-group",
                "my-component",
                new WorkItemCreateConfiguration()
                    .withConnectorId("d334e2a4-6733-488e-8645-a9fdc1694f41")
                    .withConnectorDataConfiguration(
                        "{\"VSOAccountBaseUrl\":\"https://testtodelete.visualstudio.com\",\"ProjectCollection\":\"DefaultCollection\",\"Project\":\"todeletefirst\",\"ResourceId\":\"d0662b05-439a-4a1b-840b-33a7f8b42ebf\",\"Custom\":\"{\\\"/fields/System.WorkItemType\\\":\\\"Bug\\\",\\\"/fields/System.AreaPath\\\":\\\"todeletefirst\\\",\\\"/fields/System.AssignedTo\\\":\\\"\\\"}\"}")
                    .withValidateOnly(true)
                    .withWorkItemProperties(mapOf()),
                Context.NONE);
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

### WorkItemConfigurations_Delete

```java
import com.azure.core.util.Context;

/** Samples for WorkItemConfigurations Delete. */
public final class WorkItemConfigurationsDeleteSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/WorkItemConfigDelete.json
     */
    /**
     * Sample code: WorkItemConfigurationDelete.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workItemConfigurationDelete(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .workItemConfigurations()
            .deleteWithResponse("my-resource-group", "my-component", "Visual Studio Team Services", Context.NONE);
    }
}
```

### WorkItemConfigurations_GetDefault

```java
import com.azure.core.util.Context;

/** Samples for WorkItemConfigurations GetDefault. */
public final class WorkItemConfigurationsGetDefaultSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/WorkItemConfigDefaultGet.json
     */
    /**
     * Sample code: WorkItemConfigurationsGetDefault.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workItemConfigurationsGetDefault(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.workItemConfigurations().getDefaultWithResponse("my-resource-group", "my-component", Context.NONE);
    }
}
```

### WorkItemConfigurations_GetItem

```java
import com.azure.core.util.Context;

/** Samples for WorkItemConfigurations GetItem. */
public final class WorkItemConfigurationsGetItemSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/WorkItemConfigGet.json
     */
    /**
     * Sample code: WorkItemConfigurationsGetDefault.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workItemConfigurationsGetDefault(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .workItemConfigurations()
            .getItemWithResponse("my-resource-group", "my-component", "Visual Studio Team Services", Context.NONE);
    }
}
```

### WorkItemConfigurations_List

```java
import com.azure.core.util.Context;

/** Samples for WorkItemConfigurations List. */
public final class WorkItemConfigurationsListSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/WorkItemConfigsGet.json
     */
    /**
     * Sample code: WorkItemConfigurationsList.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workItemConfigurationsList(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.workItemConfigurations().list("my-resource-group", "my-component", Context.NONE);
    }
}
```

### WorkItemConfigurations_UpdateItem

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.models.WorkItemCreateConfiguration;
import java.util.HashMap;
import java.util.Map;

/** Samples for WorkItemConfigurations UpdateItem. */
public final class WorkItemConfigurationsUpdateItemSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2015-05-01/examples/WorkItemConfigUpdate.json
     */
    /**
     * Sample code: WorkItemConfigurationsCreate.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workItemConfigurationsCreate(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .workItemConfigurations()
            .updateItemWithResponse(
                "my-resource-group",
                "my-component",
                "Visual Studio Team Services",
                new WorkItemCreateConfiguration()
                    .withConnectorId("d334e2a4-6733-488e-8645-a9fdc1694f41")
                    .withConnectorDataConfiguration(
                        "{\"VSOAccountBaseUrl\":\"https://testtodelete.visualstudio.com\",\"ProjectCollection\":\"DefaultCollection\",\"Project\":\"todeletefirst\",\"ResourceId\":\"d0662b05-439a-4a1b-840b-33a7f8b42ebf\",\"Custom\":\"{\\\"/fields/System.WorkItemType\\\":\\\"Bug\\\",\\\"/fields/System.AreaPath\\\":\\\"todeletefirst\\\",\\\"/fields/System.AssignedTo\\\":\\\"\\\"}\"}")
                    .withValidateOnly(true)
                    .withWorkItemProperties(mapOf()),
                Context.NONE);
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

### WorkbookTemplates_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.applicationinsights.models.WorkbookTemplateGallery;
import java.io.IOException;
import java.util.Arrays;

/** Samples for WorkbookTemplates CreateOrUpdate. */
public final class WorkbookTemplatesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2020-11-20/examples/WorkbookTemplateAdd.json
     */
    /**
     * Sample code: WorkbookTemplateAdd.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbookTemplateAdd(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) throws IOException {
        manager
            .workbookTemplates()
            .define("testtemplate2")
            .withRegion("west us")
            .withExistingResourceGroup("my-resource-group")
            .withPriority(1)
            .withAuthor("Contoso")
            .withTemplateData(
                SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize(
                        "{\"$schema\":\"https://github.com/Microsoft/Application-Insights-Workbooks/blob/master/schema/workbook.json\",\"items\":[{\"name\":\"text"
                            + " - 2\",\"type\":1,\"content\":{\"json\":\"## New workbook\\n"
                            + "---\\n"
                            + "\\n"
                            + "Welcome to your new workbook.  This area will display text formatted as markdown.\\n"
                            + "\\n"
                            + "\\n"
                            + "We've included a basic analytics query to get you started. Use the `Edit` button below"
                            + " each section to configure it or add more sections.\"}},{\"name\":\"query -"
                            + " 2\",\"type\":3,\"content\":{\"exportToExcelOptions\":\"visible\",\"query\":\"union"
                            + " withsource=TableName *\\n"
                            + "| summarize Count=count() by TableName\\n"
                            + "| render"
                            + " barchart\",\"queryType\":0,\"resourceType\":\"microsoft.operationalinsights/workspaces\",\"size\":1,\"version\":\"KqlItem/1.0\"}}],\"styleSettings\":{},\"version\":\"Notebook/1.0\"}",
                        Object.class,
                        SerializerEncoding.JSON))
            .withGalleries(
                Arrays
                    .asList(
                        new WorkbookTemplateGallery()
                            .withName("Simple Template")
                            .withCategory("Failures")
                            .withType("tsg")
                            .withOrder(100)
                            .withResourceType("microsoft.insights/components")))
            .create();
    }
}
```

### WorkbookTemplates_Delete

```java
import com.azure.core.util.Context;

/** Samples for WorkbookTemplates Delete. */
public final class WorkbookTemplatesDeleteSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2020-11-20/examples/WorkbookTemplateDelete.json
     */
    /**
     * Sample code: WorkbookTemplateDelete.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbookTemplateDelete(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.workbookTemplates().deleteWithResponse("my-resource-group", "my-template-resource", Context.NONE);
    }
}
```

### WorkbookTemplates_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for WorkbookTemplates GetByResourceGroup. */
public final class WorkbookTemplatesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2020-11-20/examples/WorkbookTemplateGet.json
     */
    /**
     * Sample code: WorkbookTemplateGet.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbookTemplateGet(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .workbookTemplates()
            .getByResourceGroupWithResponse("my-resource-group", "my-resource-name", Context.NONE);
    }
}
```

### WorkbookTemplates_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for WorkbookTemplates ListByResourceGroup. */
public final class WorkbookTemplatesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2020-11-20/examples/WorkbookTemplatesList.json
     */
    /**
     * Sample code: WorkbookTemplatesList.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbookTemplatesList(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.workbookTemplates().listByResourceGroup("my-resource-group", Context.NONE);
    }
}
```

### WorkbookTemplates_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.models.WorkbookTemplate;

/** Samples for WorkbookTemplates Update. */
public final class WorkbookTemplatesUpdateSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2020-11-20/examples/WorkbookTemplateUpdate.json
     */
    /**
     * Sample code: WorkbookTemplateUpdate.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbookTemplateUpdate(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        WorkbookTemplate resource =
            manager
                .workbookTemplates()
                .getByResourceGroupWithResponse("my-resource-group", "my-template-resource", Context.NONE)
                .getValue();
        resource.update().apply();
    }
}
```

### Workbooks_CreateOrUpdate

```java
import com.azure.resourcemanager.applicationinsights.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.applicationinsights.models.UserAssignedIdentity;
import com.azure.resourcemanager.applicationinsights.models.WorkbookResourceIdentity;
import com.azure.resourcemanager.applicationinsights.models.WorkbookSharedTypeKind;
import java.util.HashMap;
import java.util.Map;

/** Samples for Workbooks CreateOrUpdate. */
public final class WorkbooksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/WorkbookManagedAdd.json
     */
    /**
     * Sample code: WorkbookManagedAdd.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbookManagedAdd(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .workbooks()
            .define("deadb33f-5e0d-4064-8ebb-1a4ed0313eb2")
            .withRegion("westus")
            .withExistingResourceGroup("my-resource-group")
            .withIdentity(
                new WorkbookResourceIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/my-resource-group/providers/Microsoft.ManagedIdentity/userAssignedIdentities/myid",
                            new UserAssignedIdentity())))
            .withKind(WorkbookSharedTypeKind.SHARED)
            .withDisplayName("Sample workbook")
            .withSerializedData(
                "{\"version\":\"Notebook/1.0\",\"items\":[{\"type\":1,\"content\":{\"json\":\"test\"},\"name\":\"text -"
                    + " 0\"}],\"isLocked\":false,\"fallbackResourceIds\":[\"/subscriptions/00000000-0000-0000-0000-00000000/resourceGroups/my-resource-group\"]}")
            .withVersion("Notebook/1.0")
            .withCategory("workbook")
            .withStorageUri(
                "/subscriptions/6b643656-33eb-422f-aee8-3ac145d124af/resourceGroups/my-resource-group/providers/Microsoft.Storage/storageAccounts/mystorage/blobServices/default/containers/mycontainer")
            .withDescription("Sample workbook")
            .withSourceIdParameter(
                "/subscriptions/6b643656-33eb-422f-aee8-3ac145d124af/resourcegroups/my-resource-group")
            .create();
    }

    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/WorkbookAdd.json
     */
    /**
     * Sample code: WorkbookAdd.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbookAdd(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .workbooks()
            .define("deadb33f-5e0d-4064-8ebb-1a4ed0313eb2")
            .withRegion("westus")
            .withExistingResourceGroup("my-resource-group")
            .withTags(mapOf("TagSample01", "sample01", "TagSample02", "sample02"))
            .withKind(WorkbookSharedTypeKind.SHARED)
            .withDisplayName("Sample workbook")
            .withSerializedData(
                "{\"version\":\"Notebook/1.0\",\"items\":[{\"type\":1,\"content\":\"{\"json\":\"## New workbook\\r"
                    + "\\n"
                    + "---\\r"
                    + "\\n"
                    + "\\r"
                    + "\\n"
                    + "Welcome to your new workbook.  This area will display text formatted as markdown.\\r"
                    + "\\n"
                    + "\\r"
                    + "\\n"
                    + "\\r"
                    + "\\n"
                    + "We've included a basic analytics query to get you started. Use the `Edit` button below each"
                    + " section to configure it or add more"
                    + " sections.\"}\",\"halfWidth\":null,\"conditionalVisibility\":null},{\"type\":3,\"content\":\"{\"version\":\"KqlItem/1.0\",\"query\":\"union"
                    + " withsource=TableName *\\n"
                    + "| summarize Count=count() by TableName\\n"
                    + "| render"
                    + " barchart\",\"showQuery\":false,\"size\":1,\"aggregation\":0,\"showAnnotations\":false}\",\"halfWidth\":null,\"conditionalVisibility\":null}],\"isLocked\":false}")
            .withCategory("workbook")
            .withDescription("Sample workbook")
            .withSourceIdParameter(
                "/subscriptions/6b643656-33eb-422f-aee8-3ac145d124af/resourcegroups/my-resource-group")
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

### Workbooks_Delete

```java
import com.azure.core.util.Context;

/** Samples for Workbooks Delete. */
public final class WorkbooksDeleteSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/WorkbookDelete.json
     */
    /**
     * Sample code: WorkbookDelete.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbookDelete(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .workbooks()
            .deleteWithResponse("my-resource-group", "deadb33f-5e0d-4064-8ebb-1a4ed0313eb2", Context.NONE);
    }
}
```

### Workbooks_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Workbooks GetByResourceGroup. */
public final class WorkbooksGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/WorkbookGet1.json
     */
    /**
     * Sample code: WorkbookGet1.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbookGet1(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .workbooks()
            .getByResourceGroupWithResponse(
                "my-resource-group", "deadb33f-5e0d-4064-8ebb-1a4ed0313eb2", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/WorkbookManagedGet.json
     */
    /**
     * Sample code: WorkbookManagedGet.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbookManagedGet(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .workbooks()
            .getByResourceGroupWithResponse(
                "my-resource-group", "deadb33f-5e0d-4064-8ebb-1a4ed0313eb2", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/WorkbookGet.json
     */
    /**
     * Sample code: WorkbookGet.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbookGet(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .workbooks()
            .getByResourceGroupWithResponse(
                "my-resource-group", "deadb33f-5e0d-4064-8ebb-1a4ed0313eb2", null, Context.NONE);
    }
}
```

### Workbooks_List

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.models.CategoryType;

/** Samples for Workbooks List. */
public final class WorkbooksListSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/WorkbooksListSub.json
     */
    /**
     * Sample code: WorkbooksListSub.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbooksListSub(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.workbooks().list(CategoryType.WORKBOOK, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/WorkbooksList2.json
     */
    /**
     * Sample code: WorkbooksList2.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbooksList2(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.workbooks().list(CategoryType.WORKBOOK, null, null, Context.NONE);
    }
}
```

### Workbooks_ListByResourceGroup

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.models.CategoryType;

/** Samples for Workbooks ListByResourceGroup. */
public final class WorkbooksListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/WorkbooksManagedList.json
     */
    /**
     * Sample code: WorkbooksManagedList.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbooksManagedList(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .workbooks()
            .listByResourceGroup(
                "my-resource-group",
                CategoryType.WORKBOOK,
                null,
                "/subscriptions/6b643656-33eb-422f-aee8-3ac119r124af/resourceGroups/my-resource-group/providers/Microsoft.Web/sites/MyApp",
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/WorkbooksList.json
     */
    /**
     * Sample code: WorkbooksList.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbooksList(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .workbooks()
            .listByResourceGroup(
                "my-resource-group",
                CategoryType.WORKBOOK,
                null,
                "/subscriptions/6b643656-33eb-422f-aee8-3ac145d124af/resourceGroups/my-resource-group/providers/Microsoft.Web/sites/MyApp",
                null,
                Context.NONE);
    }
}
```

### Workbooks_RevisionGet

```java
import com.azure.core.util.Context;

/** Samples for Workbooks RevisionGet. */
public final class WorkbooksRevisionGetSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/WorkbookRevisionGet.json
     */
    /**
     * Sample code: WorkbookRevisionGet.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbookRevisionGet(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager
            .workbooks()
            .revisionGetWithResponse(
                "my-resource-group",
                "deadb33f-5e0d-4064-8ebb-1a4ed0313eb2",
                "1e2f8435b98248febee70c64ac22e1ab",
                Context.NONE);
    }
}
```

### Workbooks_RevisionsList

```java
import com.azure.core.util.Context;

/** Samples for Workbooks RevisionsList. */
public final class WorkbooksRevisionsListSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/WorkbookRevisionsList.json
     */
    /**
     * Sample code: WorkbookRevisionsList.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbookRevisionsList(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.workbooks().revisionsList("my-resource-group", "deadb33f-5e0d-4064-8ebb-1a4ed0313eb2", Context.NONE);
    }
}
```

### Workbooks_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.applicationinsights.models.Workbook;

/** Samples for Workbooks Update. */
public final class WorkbooksUpdateSamples {
    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/WorkbookUpdate.json
     */
    /**
     * Sample code: WorkbookUpdate.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbookUpdate(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        Workbook resource =
            manager
                .workbooks()
                .getByResourceGroupWithResponse(
                    "my-resource-group", "deadb33f-5e0d-4064-8ebb-1a4ed0313eb2", null, Context.NONE)
                .getValue();
        resource
            .update()
            .withSourceId(
                "/subscriptions/6b643656-33eb-422f-aee8-3ac145d124af/resourceGroups/my-resource-group/providers/Microsoft.Web/sites/MyApp")
            .apply();
    }

    /*
     * x-ms-original-file: specification/applicationinsights/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/WorkbookManagedUpdate.json
     */
    /**
     * Sample code: WorkbookManagedUpdate.
     *
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workbookManagedUpdate(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        Workbook resource =
            manager
                .workbooks()
                .getByResourceGroupWithResponse(
                    "my-resource-group", "deadb33f-5e0d-4064-8ebb-1a4ed0313eb2", null, Context.NONE)
                .getValue();
        resource
            .update()
            .withSourceId("/subscriptions/6b643656-33eb-422f-aee8-3ac145d124af/resourcegroups/my-resource-group")
            .apply();
    }
}
```

