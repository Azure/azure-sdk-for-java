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

## ProactiveDetectionConfigurations

- [Get](#proactivedetectionconfigurations_get)
- [List](#proactivedetectionconfigurations_list)
- [Update](#proactivedetectionconfigurations_update)

## WebTestLocations

- [List](#webtestlocations_list)

## WorkItemConfigurations

- [Create](#workitemconfigurations_create)
- [Delete](#workitemconfigurations_delete)
- [GetDefault](#workitemconfigurations_getdefault)
- [GetItem](#workitemconfigurations_getitem)
- [List](#workitemconfigurations_list)
- [UpdateItem](#workitemconfigurations_updateitem)
### AnalyticsItems_Delete

```java
import com.azure.resourcemanager.applicationinsights.models.ItemScopePath;

/**
 * Samples for AnalyticsItems Delete.
 */
public final class AnalyticsItemsDeleteSamples {
    /*
     * x-ms-original-file: 2015-05-01/AnalyticsItemDelete.json
     */
    /**
     * Sample code: AnalyticsItemDelete.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        analyticsItemDelete(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.analyticsItems()
            .deleteWithResponse("my-resource-group", "my-component", ItemScopePath.ANALYTICS_ITEMS,
                "3466c160-4a10-4df8-afdf-0007f3f6dee5", null, com.azure.core.util.Context.NONE);
    }
}
```

### AnalyticsItems_Get

```java
import com.azure.resourcemanager.applicationinsights.models.ItemScopePath;

/**
 * Samples for AnalyticsItems Get.
 */
public final class AnalyticsItemsGetSamples {
    /*
     * x-ms-original-file: 2015-05-01/AnalyticsItemGet.json
     */
    /**
     * Sample code: AnalyticsItemGet.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        analyticsItemGet(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.analyticsItems()
            .getWithResponse("my-resource-group", "my-component", ItemScopePath.ANALYTICS_ITEMS,
                "3466c160-4a10-4df8-afdf-0007f3f6dee5", null, com.azure.core.util.Context.NONE);
    }
}
```

### AnalyticsItems_List

```java
import com.azure.resourcemanager.applicationinsights.models.ItemScopePath;

/**
 * Samples for AnalyticsItems List.
 */
public final class AnalyticsItemsListSamples {
    /*
     * x-ms-original-file: 2015-05-01/AnalyticsItemList.json
     */
    /**
     * Sample code: AnalyticsItemList.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        analyticsItemList(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.analyticsItems()
            .listWithResponse("my-resource-group", "my-component", ItemScopePath.ANALYTICS_ITEMS, null, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### AnalyticsItems_Put

```java
import com.azure.resourcemanager.applicationinsights.fluent.models.ApplicationInsightsComponentAnalyticsItemInner;
import com.azure.resourcemanager.applicationinsights.models.ItemScope;
import com.azure.resourcemanager.applicationinsights.models.ItemScopePath;
import com.azure.resourcemanager.applicationinsights.models.ItemType;

/**
 * Samples for AnalyticsItems Put.
 */
public final class AnalyticsItemsPutSamples {
    /*
     * x-ms-original-file: 2015-05-01/AnalyticsItemPut.json
     */
    /**
     * Sample code: AnalyticsItemPut.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        analyticsItemPut(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.analyticsItems()
            .putWithResponse("my-resource-group", "my-component", ItemScopePath.ANALYTICS_ITEMS,
                new ApplicationInsightsComponentAnalyticsItemInner().withName("Exceptions - New in the last 24 hours")
                    .withContent(
                        "let newExceptionsTimeRange = 1d;\nlet timeRangeToCheckBefore = 7d;\nexceptions\n| where timestamp < ago(timeRangeToCheckBefore)\n| summarize count() by problemId\n| join kind= rightanti (\nexceptions\n| where timestamp >= ago(newExceptionsTimeRange)\n| extend stack = tostring(details[0].rawStack)\n| summarize count(), dcount(user_AuthenticatedId), min(timestamp), max(timestamp), any(stack) by problemId  \n) on problemId \n| order by  count_ desc\n")
                    .withScope(ItemScope.SHARED)
                    .withType(ItemType.QUERY),
                null, com.azure.core.util.Context.NONE);
    }
}
```

### Annotations_Create

```java
import com.azure.resourcemanager.applicationinsights.fluent.models.AnnotationInner;
import java.time.OffsetDateTime;

/**
 * Samples for Annotations Create.
 */
public final class AnnotationsCreateSamples {
    /*
     * x-ms-original-file: 2015-05-01/AnnotationsCreate.json
     */
    /**
     * Sample code: AnnotationsCreate.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        annotationsCreate(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.annotations()
            .createWithResponse("my-resource-group", "my-component",
                new AnnotationInner().withAnnotationName("TestAnnotation")
                    .withCategory("Text")
                    .withEventTime(OffsetDateTime.parse("2018-01-31T13:41:38.657Z"))
                    .withId("444e2c08-274a-4bbb-a89e-d77bb720f44a")
                    .withProperties("{\"Comments\":\"Testing\",\"Label\":\"Success\"}"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Annotations_Delete

```java
/**
 * Samples for Annotations Delete.
 */
public final class AnnotationsDeleteSamples {
    /*
     * x-ms-original-file: 2015-05-01/AnnotationsDelete.json
     */
    /**
     * Sample code: AnnotationsDelete.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        annotationsDelete(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.annotations()
            .deleteWithResponse("my-resource-group", "my-component", "bb820f1b-3110-4a8b-ba2c-8c1129d7eb6a",
                com.azure.core.util.Context.NONE);
    }
}
```

### Annotations_Get

```java
/**
 * Samples for Annotations Get.
 */
public final class AnnotationsGetSamples {
    /*
     * x-ms-original-file: 2015-05-01/AnnotationsGet.json
     */
    /**
     * Sample code: AnnotationsGet.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        annotationsGet(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.annotations()
            .getWithResponse("my-resource-group", "my-component", "444e2c08-274a-4bbb-a89e-d77bb720f44a",
                com.azure.core.util.Context.NONE);
    }
}
```

### Annotations_List

```java
/**
 * Samples for Annotations List.
 */
public final class AnnotationsListSamples {
    /*
     * x-ms-original-file: 2015-05-01/AnnotationsList.json
     */
    /**
     * Sample code: AnnotationsList.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        annotationsList(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.annotations()
            .list("my-resource-group", "my-component", "2018-02-05T00%3A30%3A00.000Z", "2018-02-06T00%3A33A00.000Z",
                com.azure.core.util.Context.NONE);
    }
}
```

### ApiKeys_Create

```java
import com.azure.resourcemanager.applicationinsights.models.ApiKeyRequest;
import java.util.Arrays;

/**
 * Samples for ApiKeys Create.
 */
public final class ApiKeysCreateSamples {
    /*
     * x-ms-original-file: 2015-05-01/APIKeysCreate.json
     */
    /**
     * Sample code: APIKeyCreate.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void aPIKeyCreate(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.apiKeys()
            .createWithResponse("my-resource-group", "my-component", new ApiKeyRequest().withName("test2")
                .withLinkedReadProperties(Arrays.asList(
                    "/subscriptions/subid/resourceGroups/my-resource-group/providers/Microsoft.Insights/components/my-component/api",
                    "/subscriptions/subid/resourceGroups/my-resource-group/providers/Microsoft.Insights/components/my-component/agentconfig"))
                .withLinkedWriteProperties(Arrays.asList(
                    "/subscriptions/subid/resourceGroups/my-resource-group/providers/Microsoft.Insights/components/my-component/annotations")),
                com.azure.core.util.Context.NONE);
    }
}
```

### ApiKeys_Delete

```java
/**
 * Samples for ApiKeys Delete.
 */
public final class ApiKeysDeleteSamples {
    /*
     * x-ms-original-file: 2015-05-01/APIKeysDelete.json
     */
    /**
     * Sample code: APIKeyDelete.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void aPIKeyDelete(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.apiKeys()
            .deleteWithResponse("my-resource-group", "my-component", "bb820f1b-3110-4a8b-ba2c-8c1129d7eb6a",
                com.azure.core.util.Context.NONE);
    }
}
```

### ApiKeys_Get

```java
/**
 * Samples for ApiKeys Get.
 */
public final class ApiKeysGetSamples {
    /*
     * x-ms-original-file: 2015-05-01/APIKeysGet.json
     */
    /**
     * Sample code: APIKeysGet.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void aPIKeysGet(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.apiKeys()
            .getWithResponse("my-resource-group", "my-component", "bb820f1b-3110-4a8b-ba2c-8c1129d7eb6a",
                com.azure.core.util.Context.NONE);
    }
}
```

### ApiKeys_List

```java
/**
 * Samples for ApiKeys List.
 */
public final class ApiKeysListSamples {
    /*
     * x-ms-original-file: 2015-05-01/APIKeysList.json
     */
    /**
     * Sample code: APIKeysList.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void aPIKeysList(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.apiKeys().list("my-resource-group", "my-component", com.azure.core.util.Context.NONE);
    }
}
```

### ComponentAvailableFeatures_Get

```java
/**
 * Samples for ComponentAvailableFeatures Get.
 */
public final class ComponentAvailableFeaturesGetSamples {
    /*
     * x-ms-original-file: 2015-05-01/AvailableBillingFeaturesGet.json
     */
    /**
     * Sample code: ComponentCurrentBillingFeaturesGet.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void componentCurrentBillingFeaturesGet(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.componentAvailableFeatures()
            .getWithResponse("my-resource-group", "my-component", com.azure.core.util.Context.NONE);
    }
}
```

### ComponentCurrentBillingFeatures_Get

```java
/**
 * Samples for ComponentCurrentBillingFeatures Get.
 */
public final class ComponentCurrentBillingFeaturesGetSamples {
    /*
     * x-ms-original-file: 2015-05-01/CurrentBillingFeaturesGet.json
     */
    /**
     * Sample code: ComponentCurrentBillingFeaturesGet.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void componentCurrentBillingFeaturesGet(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.componentCurrentBillingFeatures()
            .getWithResponse("my-resource-group", "my-component", com.azure.core.util.Context.NONE);
    }
}
```

### ComponentCurrentBillingFeatures_Update

```java
import com.azure.resourcemanager.applicationinsights.fluent.models.ApplicationInsightsComponentBillingFeaturesInner;
import com.azure.resourcemanager.applicationinsights.models.ApplicationInsightsComponentDataVolumeCap;
import java.util.Arrays;

/**
 * Samples for ComponentCurrentBillingFeatures Update.
 */
public final class ComponentCurrentBillingFeaturesUpdateSamples {
    /*
     * x-ms-original-file: 2015-05-01/CurrentBillingFeaturesUpdate.json
     */
    /**
     * Sample code: ComponentCurrentBillingFeaturesUpdate.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void componentCurrentBillingFeaturesUpdate(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.componentCurrentBillingFeatures()
            .updateWithResponse("my-resource-group", "my-component",
                new ApplicationInsightsComponentBillingFeaturesInner()
                    .withDataVolumeCap(new ApplicationInsightsComponentDataVolumeCap().withCap(100.0F)
                        .withStopSendNotificationWhenHitCap(true))
                    .withCurrentBillingFeatures(Arrays.asList("Basic", "Application Insights Enterprise")),
                com.azure.core.util.Context.NONE);
    }
}
```

### ComponentFeatureCapabilities_Get

```java
/**
 * Samples for ComponentFeatureCapabilities Get.
 */
public final class ComponentFeatureCapabilitiesGetSamples {
    /*
     * x-ms-original-file: 2015-05-01/FeatureCapabilitiesGet.json
     */
    /**
     * Sample code: ComponentCurrentBillingFeaturesGet.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void componentCurrentBillingFeaturesGet(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.componentFeatureCapabilities()
            .getWithResponse("my-resource-group", "my-component", com.azure.core.util.Context.NONE);
    }
}
```

### ComponentQuotaStatus_Get

```java
/**
 * Samples for ComponentQuotaStatus Get.
 */
public final class ComponentQuotaStatusGetSamples {
    /*
     * x-ms-original-file: 2015-05-01/QuotaStatusGet.json
     */
    /**
     * Sample code: ComponentCurrentBillingFeaturesGet.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void componentCurrentBillingFeaturesGet(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.componentQuotaStatus()
            .getWithResponse("my-resource-group", "my-component", com.azure.core.util.Context.NONE);
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

/**
 * Samples for Components CreateOrUpdate.
 */
public final class ComponentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2020-02-02/ComponentsCreate.json
     */
    /**
     * Sample code: ComponentCreate.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        componentCreate(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.components()
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
     * x-ms-original-file: 2020-02-02/ComponentsUpdate.json
     */
    /**
     * Sample code: ComponentUpdate.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        componentUpdate(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.components()
            .define("my-component")
            .withRegion("South Central US")
            .withExistingResourceGroup("my-resource-group")
            .withKind("web")
            .withTags(mapOf("ApplicationGatewayType", "Internal-Only", "BillingEntity", "Self"))
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

### Components_Delete

```java
/**
 * Samples for Components Delete.
 */
public final class ComponentsDeleteSamples {
    /*
     * x-ms-original-file: 2020-02-02/ComponentsDelete.json
     */
    /**
     * Sample code: ComponentsDelete.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        componentsDelete(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.components()
            .deleteByResourceGroupWithResponse("my-resource-group", "my-component", com.azure.core.util.Context.NONE);
    }
}
```

### Components_GetByResourceGroup

```java
/**
 * Samples for Components GetByResourceGroup.
 */
public final class ComponentsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2020-02-02/ComponentsGet.json
     */
    /**
     * Sample code: ComponentGet.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void componentGet(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.components()
            .getByResourceGroupWithResponse("my-resource-group", "my-component", com.azure.core.util.Context.NONE);
    }
}
```

### Components_GetPurgeStatus

```java
/**
 * Samples for Components GetPurgeStatus.
 */
public final class ComponentsGetPurgeStatusSamples {
    /*
     * x-ms-original-file: 2020-02-02/ComponentsPurgeStatus.json
     */
    /**
     * Sample code: ComponentPurge.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        componentPurge(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.components()
            .getPurgeStatusWithResponse("OIAutoRest5123", "aztest5048", "purge-970318e7-b859-4edb-8903-83b1b54d0b74",
                com.azure.core.util.Context.NONE);
    }
}
```

### Components_List

```java
/**
 * Samples for Components List.
 */
public final class ComponentsListSamples {
    /*
     * x-ms-original-file: 2020-02-02/ComponentsList.json
     */
    /**
     * Sample code: ComponentsList.json.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        componentsListJson(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.components().list(com.azure.core.util.Context.NONE);
    }
}
```

### Components_ListByResourceGroup

```java
/**
 * Samples for Components ListByResourceGroup.
 */
public final class ComponentsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2020-02-02/ComponentsListByResourceGroup.json
     */
    /**
     * Sample code: ComponentListByResourceGroup.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        componentListByResourceGroup(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.components().listByResourceGroup("my-resource-group", com.azure.core.util.Context.NONE);
    }
}
```

### Components_Purge

```java
import com.azure.resourcemanager.applicationinsights.models.ComponentPurgeBody;
import com.azure.resourcemanager.applicationinsights.models.ComponentPurgeBodyFilters;
import java.util.Arrays;

/**
 * Samples for Components Purge.
 */
public final class ComponentsPurgeSamples {
    /*
     * x-ms-original-file: 2020-02-02/ComponentsPurge.json
     */
    /**
     * Sample code: ComponentPurge.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        componentPurge(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.components()
            .purgeWithResponse("OIAutoRest5123", "aztest5048",
                new ComponentPurgeBody().withTable("Heartbeat")
                    .withFilters(Arrays.asList(new ComponentPurgeBodyFilters().withColumn("TimeGenerated")
                        .withOperator(">")
                        .withValue("2017-09-01T00:00:00"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Components_UpdateTags

```java
import com.azure.resourcemanager.applicationinsights.models.ApplicationInsightsComponent;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Components UpdateTags.
 */
public final class ComponentsUpdateTagsSamples {
    /*
     * x-ms-original-file: 2020-02-02/ComponentsUpdateTagsOnly.json
     */
    /**
     * Sample code: ComponentUpdateTagsOnly.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        componentUpdateTagsOnly(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        ApplicationInsightsComponent resource = manager.components()
            .getByResourceGroupWithResponse("my-resource-group", "my-component", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("ApplicationGatewayType", "Internal-Only", "BillingEntity", "Self", "Color", "AzureBlue",
                "CustomField_01", "Custom text in some random field named randomly", "NodeType", "Edge"))
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

### ExportConfigurations_Create

```java
import com.azure.resourcemanager.applicationinsights.models.ApplicationInsightsComponentExportRequest;

/**
 * Samples for ExportConfigurations Create.
 */
public final class ExportConfigurationsCreateSamples {
    /*
     * x-ms-original-file: 2015-05-01/ExportConfigurationsPost.json
     */
    /**
     * Sample code: ExportConfigurationPost.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        exportConfigurationPost(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.exportConfigurations()
            .createWithResponse("my-resource-group", "my-component", new ApplicationInsightsComponentExportRequest()
                .withRecordTypes(
                    "Requests, Event, Exceptions, Metrics, PageViews, PageViewPerformance, Rdd, PerformanceCounters, Availability")
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
                com.azure.core.util.Context.NONE);
    }
}
```

### ExportConfigurations_Delete

```java
/**
 * Samples for ExportConfigurations Delete.
 */
public final class ExportConfigurationsDeleteSamples {
    /*
     * x-ms-original-file: 2015-05-01/ExportConfigurationDelete.json
     */
    /**
     * Sample code: ExportConfigurationDelete.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        exportConfigurationDelete(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.exportConfigurations()
            .deleteWithResponse("my-resource-group", "my-component", "uGOoki0jQsyEs3IdQ83Q4QsNr4=",
                com.azure.core.util.Context.NONE);
    }
}
```

### ExportConfigurations_Get

```java
/**
 * Samples for ExportConfigurations Get.
 */
public final class ExportConfigurationsGetSamples {
    /*
     * x-ms-original-file: 2015-05-01/ExportConfigurationGet.json
     */
    /**
     * Sample code: ExportConfigurationGet.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        exportConfigurationGet(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.exportConfigurations()
            .getWithResponse("my-resource-group", "my-component", "uGOoki0jQsyEs3IdQ83Q4QsNr4=",
                com.azure.core.util.Context.NONE);
    }
}
```

### ExportConfigurations_List

```java
/**
 * Samples for ExportConfigurations List.
 */
public final class ExportConfigurationsListSamples {
    /*
     * x-ms-original-file: 2015-05-01/ExportConfigurationsList.json
     */
    /**
     * Sample code: ExportConfigurationsList.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        exportConfigurationsList(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.exportConfigurations()
            .listWithResponse("my-resource-group", "my-component", com.azure.core.util.Context.NONE);
    }
}
```

### ExportConfigurations_Update

```java
import com.azure.resourcemanager.applicationinsights.models.ApplicationInsightsComponentExportRequest;

/**
 * Samples for ExportConfigurations Update.
 */
public final class ExportConfigurationsUpdateSamples {
    /*
     * x-ms-original-file: 2015-05-01/ExportConfigurationUpdate.json
     */
    /**
     * Sample code: ExportConfigurationUpdate.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        exportConfigurationUpdate(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.exportConfigurations()
            .updateWithResponse("my-resource-group", "my-component", "uGOoki0jQsyEs3IdQ83Q4QsNr4=",
                new ApplicationInsightsComponentExportRequest().withRecordTypes(
                    "Requests, Event, Exceptions, Metrics, PageViews, PageViewPerformance, Rdd, PerformanceCounters, Availability")
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
                com.azure.core.util.Context.NONE);
    }
}
```

### Favorites_Add

```java
import com.azure.resourcemanager.applicationinsights.fluent.models.ApplicationInsightsComponentFavoriteInner;
import com.azure.resourcemanager.applicationinsights.models.FavoriteType;
import java.util.Arrays;

/**
 * Samples for Favorites Add.
 */
public final class FavoritesAddSamples {
    /*
     * x-ms-original-file: 2015-05-01/FavoriteAdd.json
     */
    /**
     * Sample code: FavoriteAdd.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void favoriteAdd(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.favorites()
            .addWithResponse("my-resource-group", "my-ai-component", "deadb33f-8bee-4d3b-a059-9be8dac93960",
                new ApplicationInsightsComponentFavoriteInner().withName("Blah Blah Blah")
                    .withConfig(
                        "{\"MEDataModelRawJSON\":\"{\\n  \\\"version\\\": \\\"1.4.1\\\",\\n  \\\"isCustomDataModel\\\": true,\\n  \\\"items\\\": [\\n    {\\n      \\\"id\\\": \\\"90a7134d-9a38-4c25-88d3-a495209873eb\\\",\\n      \\\"chartType\\\": \\\"Area\\\",\\n      \\\"chartHeight\\\": 4,\\n      \\\"metrics\\\": [\\n        {\\n          \\\"id\\\": \\\"preview/requests/count\\\",\\n          \\\"metricAggregation\\\": \\\"Sum\\\",\\n          \\\"color\\\": \\\"msportalfx-bgcolor-d0\\\"\\n        }\\n      ],\\n      \\\"priorPeriod\\\": false,\\n      \\\"clickAction\\\": {\\n        \\\"defaultBlade\\\": \\\"SearchBlade\\\"\\n      },\\n      \\\"horizontalBars\\\": true,\\n      \\\"showOther\\\": true,\\n      \\\"aggregation\\\": \\\"Sum\\\",\\n      \\\"percentage\\\": false,\\n      \\\"palette\\\": \\\"fail\\\",\\n      \\\"yAxisOption\\\": 0,\\n      \\\"title\\\": \\\"\\\"\\n    },\\n    {\\n      \\\"id\\\": \\\"0c289098-88e8-4010-b212-546815cddf70\\\",\\n      \\\"chartType\\\": \\\"Area\\\",\\n      \\\"chartHeight\\\": 2,\\n      \\\"metrics\\\": [\\n        {\\n          \\\"id\\\": \\\"preview/requests/duration\\\",\\n          \\\"metricAggregation\\\": \\\"Avg\\\",\\n          \\\"color\\\": \\\"msportalfx-bgcolor-j1\\\"\\n        }\\n      ],\\n      \\\"priorPeriod\\\": false,\\n      \\\"clickAction\\\": {\\n        \\\"defaultBlade\\\": \\\"SearchBlade\\\"\\n      },\\n      \\\"horizontalBars\\\": true,\\n      \\\"showOther\\\": true,\\n      \\\"aggregation\\\": \\\"Avg\\\",\\n      \\\"percentage\\\": false,\\n      \\\"palette\\\": \\\"greenHues\\\",\\n      \\\"yAxisOption\\\": 0,\\n      \\\"title\\\": \\\"\\\"\\n    },\\n    {\\n      \\\"id\\\": \\\"cbdaab6f-a808-4f71-aca5-b3976cbb7345\\\",\\n      \\\"chartType\\\": \\\"Bar\\\",\\n      \\\"chartHeight\\\": 4,\\n      \\\"metrics\\\": [\\n        {\\n          \\\"id\\\": \\\"preview/requests/duration\\\",\\n          \\\"metricAggregation\\\": \\\"Avg\\\",\\n          \\\"color\\\": \\\"msportalfx-bgcolor-d0\\\"\\n        }\\n      ],\\n      \\\"priorPeriod\\\": false,\\n      \\\"clickAction\\\": {\\n        \\\"defaultBlade\\\": \\\"SearchBlade\\\"\\n      },\\n      \\\"horizontalBars\\\": true,\\n      \\\"showOther\\\": true,\\n      \\\"aggregation\\\": \\\"Avg\\\",\\n      \\\"percentage\\\": false,\\n      \\\"palette\\\": \\\"magentaHues\\\",\\n      \\\"yAxisOption\\\": 0,\\n      \\\"title\\\": \\\"\\\"\\n    },\\n    {\\n      \\\"id\\\": \\\"1d5a6a3a-9fa1-4099-9cf9-05eff72d1b02\\\",\\n      \\\"grouping\\\": {\\n        \\\"kind\\\": \\\"ByDimension\\\",\\n        \\\"dimension\\\": \\\"context.application.version\\\"\\n      },\\n      \\\"chartType\\\": \\\"Grid\\\",\\n      \\\"chartHeight\\\": 1,\\n      \\\"metrics\\\": [\\n        {\\n          \\\"id\\\": \\\"basicException.count\\\",\\n          \\\"metricAggregation\\\": \\\"Sum\\\",\\n          \\\"color\\\": \\\"msportalfx-bgcolor-g0\\\"\\n        },\\n        {\\n          \\\"id\\\": \\\"requestFailed.count\\\",\\n          \\\"metricAggregation\\\": \\\"Sum\\\",\\n          \\\"color\\\": \\\"msportalfx-bgcolor-f0s2\\\"\\n        }\\n      ],\\n      \\\"priorPeriod\\\": true,\\n      \\\"clickAction\\\": {\\n        \\\"defaultBlade\\\": \\\"SearchBlade\\\"\\n      },\\n      \\\"horizontalBars\\\": true,\\n      \\\"showOther\\\": true,\\n      \\\"percentage\\\": false,\\n      \\\"palette\\\": \\\"blueHues\\\",\\n      \\\"yAxisOption\\\": 0,\\n      \\\"title\\\": \\\"\\\"\\n    }\\n  ],\\n  \\\"currentFilter\\\": {\\n    \\\"eventTypes\\\": [\\n      1,\\n      2\\n    ],\\n    \\\"typeFacets\\\": {},\\n    \\\"isPermissive\\\": false\\n  },\\n  \\\"timeContext\\\": {\\n    \\\"durationMs\\\": 75600000,\\n    \\\"endTime\\\": \\\"2018-01-31T20:30:00.000Z\\\",\\n    \\\"createdTime\\\": \\\"2018-01-31T23:54:26.280Z\\\",\\n    \\\"isInitialTime\\\": false,\\n    \\\"grain\\\": 1,\\n    \\\"useDashboardTimeRange\\\": false\\n  },\\n  \\\"jsonUri\\\": \\\"Favorite_BlankChart\\\",\\n  \\\"timeSource\\\": 0\\n}\"}")
                    .withVersion("ME")
                    .withFavoriteType(FavoriteType.SHARED)
                    .withTags(Arrays.asList("TagSample01", "TagSample02"))
                    .withIsGeneratedFromTemplate(false),
                com.azure.core.util.Context.NONE);
    }
}
```

### Favorites_Delete

```java
/**
 * Samples for Favorites Delete.
 */
public final class FavoritesDeleteSamples {
    /*
     * x-ms-original-file: 2015-05-01/FavoriteDelete.json
     */
    /**
     * Sample code: FavoriteList.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void favoriteList(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.favorites()
            .deleteWithResponse("my-resource-group", "my-ai-component", "deadb33f-5e0d-4064-8ebb-1a4ed0313eb2",
                com.azure.core.util.Context.NONE);
    }
}
```

### Favorites_Get

```java
/**
 * Samples for Favorites Get.
 */
public final class FavoritesGetSamples {
    /*
     * x-ms-original-file: 2015-05-01/FavoriteGet.json
     */
    /**
     * Sample code: FavoriteGet.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void favoriteGet(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.favorites()
            .getWithResponse("my-resource-group", "my-ai-component", "deadb33f-5e0d-4064-8ebb-1a4ed0313eb2",
                com.azure.core.util.Context.NONE);
    }
}
```

### Favorites_List

```java

/**
 * Samples for Favorites List.
 */
public final class FavoritesListSamples {
    /*
     * x-ms-original-file: 2015-05-01/FavoritesList.json
     */
    /**
     * Sample code: FavoritesList.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void favoritesList(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.favorites()
            .listWithResponse("my-resource-group", "my-ai-component", null, null, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Favorites_Update

```java
import com.azure.resourcemanager.applicationinsights.fluent.models.ApplicationInsightsComponentFavoriteInner;
import com.azure.resourcemanager.applicationinsights.models.FavoriteType;
import java.util.Arrays;

/**
 * Samples for Favorites Update.
 */
public final class FavoritesUpdateSamples {
    /*
     * x-ms-original-file: 2015-05-01/FavoriteUpdate.json
     */
    /**
     * Sample code: FavoriteList.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void favoriteList(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.favorites()
            .updateWithResponse("my-resource-group", "my-ai-component", "deadb33f-5e0d-4064-8ebb-1a4ed0313eb2",
                new ApplicationInsightsComponentFavoriteInner().withName("Derek Changed This")
                    .withConfig(
                        "{\"MEDataModelRawJSON\":\"{\\\"version\\\": \\\"1.4.1\\\",\\\"isCustomDataModel\\\": true,\\\"items\\\": [{\\\"id\\\": \\\"90a7134d-9a38-4c25-88d3-a495209873eb\\\",\\\"chartType\\\": \\\"Area\\\",\\\"chartHeight\\\": 4,\\\"metrics\\\": [{\\\"id\\\": \\\"preview/requests/count\\\",\\\"metricAggregation\\\": \\\"Sum\\\",\\\"color\\\": \\\"msportalfx-bgcolor-d0\\\"}],\\\"priorPeriod\\\": false,\\\"clickAction\\\": {\\\"defaultBlade\\\": \\\"SearchBlade\\\"},\\\"horizontalBars\\\": true,\\\"showOther\\\": true,\\\"aggregation\\\": \\\"Sum\\\",\\\"percentage\\\": false,\\\"palette\\\": \\\"fail\\\",\\\"yAxisOption\\\": 0,\\\"title\\\": \\\"\\\"},{\\\"id\\\": \\\"0c289098-88e8-4010-b212-546815cddf70\\\",\\\"chartType\\\": \\\"Area\\\",\\\"chartHeight\\\": 2,\\\"metrics\\\": [{\\\"id\\\": \\\"preview/requests/duration\\\",\\\"metricAggregation\\\": \\\"Avg\\\",\\\"color\\\": \\\"msportalfx-bgcolor-j1\\\"}],\\\"priorPeriod\\\": false,\\\"clickAction\\\": {\\\"defaultBlade\\\": \\\"SearchBlade\\\"},\\\"horizontalBars\\\": true,\\\"showOther\\\": true,\\\"aggregation\\\": \\\"Avg\\\",\\\"percentage\\\": false,\\\"palette\\\": \\\"greenHues\\\",\\\"yAxisOption\\\": 0,\\\"title\\\": \\\"\\\"},{\\\"id\\\": \\\"cbdaab6f-a808-4f71-aca5-b3976cbb7345\\\",\\\"chartType\\\": \\\"Bar\\\",\\\"chartHeight\\\": 4,\\\"metrics\\\": [{\\\"id\\\": \\\"preview/requests/duration\\\",\\\"metricAggregation\\\": \\\"Avg\\\",\\\"color\\\": \\\"msportalfx-bgcolor-d0\\\"}],\\\"priorPeriod\\\": false,\\\"clickAction\\\": {\\\"defaultBlade\\\": \\\"SearchBlade\\\"},\\\"horizontalBars\\\": true,\\\"showOther\\\": true,\\\"aggregation\\\": \\\"Avg\\\",\\\"percentage\\\": false,\\\"palette\\\": \\\"magentaHues\\\",\\\"yAxisOption\\\": 0,\\\"title\\\": \\\"\\\"},{\\\"id\\\": \\\"1d5a6a3a-9fa1-4099-9cf9-05eff72d1b02\\\",\\\"grouping\\\": {\\\"kind\\\": \\\"ByDimension\\\",\\\"dimension\\\": \\\"context.application.version\\\"},\\\"chartType\\\": \\\"Grid\\\",\\\"chartHeight\\\": 1,\\\"metrics\\\": [{\\\"id\\\": \\\"basicException.count\\\",\\\"metricAggregation\\\": \\\"Sum\\\",\\\"color\\\": \\\"msportalfx-bgcolor-g0\\\"},{\\\"id\\\": \\\"requestFailed.count\\\",\\\"metricAggregation\\\": \\\"Sum\\\",\\\"color\\\": \\\"msportalfx-bgcolor-f0s2\\\"}],\\\"priorPeriod\\\": true,\\\"clickAction\\\": {\\\"defaultBlade\\\": \\\"SearchBlade\\\"},\\\"horizontalBars\\\": true,\\\"showOther\\\": true,\\\"percentage\\\": false,\\\"palette\\\": \\\"blueHues\\\",\\\"yAxisOption\\\": 0,\\\"title\\\": \\\"\\\"}],\\\"currentFilter\\\": {\\\"eventTypes\\\": [1,2],\\\"typeFacets\\\": {},\\\"isPermissive\\\": false},\\\"timeContext\\\": {\\\"durationMs\\\": 75600000,\\\"endTime\\\": \\\"2018-01-31T20:30:00.000Z\\\",\\\"createdTime\\\": \\\"2018-01-31T23:54:26.280Z\\\",\\\"isInitialTime\\\": false,\\\"grain\\\": 1,\\\"useDashboardTimeRange\\\": false},\\\"jsonUri\\\": \\\"Favorite_BlankChart\\\",\\\"timeSource\\\": 0}\"}")
                    .withVersion("ME")
                    .withFavoriteType(FavoriteType.SHARED)
                    .withTags(Arrays.asList("TagSample01", "TagSample02", "TagSample03"))
                    .withIsGeneratedFromTemplate(false),
                com.azure.core.util.Context.NONE);
    }
}
```

### ProactiveDetectionConfigurations_Get

```java
/**
 * Samples for ProactiveDetectionConfigurations Get.
 */
public final class ProactiveDetectionConfigurationsGetSamples {
    /*
     * x-ms-original-file: 2015-05-01/ProactiveDetectionConfigurationGet.json
     */
    /**
     * Sample code: ProactiveDetectionConfigurationGet.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void proactiveDetectionConfigurationGet(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.proactiveDetectionConfigurations()
            .getWithResponse("my-resource-group", "my-component", "slowpageloadtime", com.azure.core.util.Context.NONE);
    }
}
```

### ProactiveDetectionConfigurations_List

```java
/**
 * Samples for ProactiveDetectionConfigurations List.
 */
public final class ProactiveDetectionConfigurationsListSamples {
    /*
     * x-ms-original-file: 2015-05-01/ProactiveDetectionConfigurationsList.json
     */
    /**
     * Sample code: ProactiveDetectionConfigurationsList.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void proactiveDetectionConfigurationsList(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.proactiveDetectionConfigurations()
            .listWithResponse("my-resource-group", "my-component", com.azure.core.util.Context.NONE);
    }
}
```

### ProactiveDetectionConfigurations_Update

```java
import com.azure.resourcemanager.applicationinsights.fluent.models.ApplicationInsightsComponentProactiveDetectionConfigurationInner;
import com.azure.resourcemanager.applicationinsights.models.ApplicationInsightsComponentProactiveDetectionConfigurationRuleDefinitions;
import java.util.Arrays;

/**
 * Samples for ProactiveDetectionConfigurations Update.
 */
public final class ProactiveDetectionConfigurationsUpdateSamples {
    /*
     * x-ms-original-file: 2015-05-01/ProactiveDetectionConfigurationUpdate.json
     */
    /**
     * Sample code: ProactiveDetectionConfigurationUpdate.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void proactiveDetectionConfigurationUpdate(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.proactiveDetectionConfigurations()
            .updateWithResponse("my-resource-group", "my-component", "slowpageloadtime",
                new ApplicationInsightsComponentProactiveDetectionConfigurationInner().withName("slowpageloadtime")
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
                com.azure.core.util.Context.NONE);
    }
}
```

### WebTestLocations_List

```java
/**
 * Samples for WebTestLocations List.
 */
public final class WebTestLocationsListSamples {
    /*
     * x-ms-original-file: 2015-05-01/WebTestLocationsList.json
     */
    /**
     * Sample code: WebTestLocationsList.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        webTestLocationsList(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.webTestLocations().list("my-resource-group", "my-component", com.azure.core.util.Context.NONE);
    }
}
```

### WorkItemConfigurations_Create

```java
import com.azure.resourcemanager.applicationinsights.models.WorkItemCreateConfiguration;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for WorkItemConfigurations Create.
 */
public final class WorkItemConfigurationsCreateSamples {
    /*
     * x-ms-original-file: 2015-05-01/WorkItemConfigCreate.json
     */
    /**
     * Sample code: WorkItemConfigurationsCreate.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        workItemConfigurationsCreate(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.workItemConfigurations()
            .createWithResponse("my-resource-group", "my-component", new WorkItemCreateConfiguration()
                .withConnectorId("d334e2a4-6733-488e-8645-a9fdc1694f41")
                .withConnectorDataConfiguration(
                    "{\"VSOAccountBaseUrl\":\"https://testtodelete.visualstudio.com\",\"ProjectCollection\":\"DefaultCollection\",\"Project\":\"todeletefirst\",\"ResourceId\":\"d0662b05-439a-4a1b-840b-33a7f8b42ebf\",\"Custom\":\"{\\\"/fields/System.WorkItemType\\\":\\\"Bug\\\",\\\"/fields/System.AreaPath\\\":\\\"todeletefirst\\\",\\\"/fields/System.AssignedTo\\\":\\\"\\\"}\"}")
                .withValidateOnly(true)
                .withWorkItemProperties(mapOf("name", "Title", "value", "Validate Only Title")),
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

### WorkItemConfigurations_Delete

```java
/**
 * Samples for WorkItemConfigurations Delete.
 */
public final class WorkItemConfigurationsDeleteSamples {
    /*
     * x-ms-original-file: 2015-05-01/WorkItemConfigDelete.json
     */
    /**
     * Sample code: WorkItemConfigurationDelete.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        workItemConfigurationDelete(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.workItemConfigurations()
            .deleteWithResponse("my-resource-group", "my-component", "Visual Studio Team Services",
                com.azure.core.util.Context.NONE);
    }
}
```

### WorkItemConfigurations_GetDefault

```java
/**
 * Samples for WorkItemConfigurations GetDefault.
 */
public final class WorkItemConfigurationsGetDefaultSamples {
    /*
     * x-ms-original-file: 2015-05-01/WorkItemConfigDefaultGet.json
     */
    /**
     * Sample code: WorkItemConfigurationsGetDefault.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workItemConfigurationsGetDefault(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.workItemConfigurations()
            .getDefaultWithResponse("my-resource-group", "my-component", com.azure.core.util.Context.NONE);
    }
}
```

### WorkItemConfigurations_GetItem

```java
/**
 * Samples for WorkItemConfigurations GetItem.
 */
public final class WorkItemConfigurationsGetItemSamples {
    /*
     * x-ms-original-file: 2015-05-01/WorkItemConfigGet.json
     */
    /**
     * Sample code: WorkItemConfigurationsGetDefault.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void workItemConfigurationsGetDefault(
        com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.workItemConfigurations()
            .getItemWithResponse("my-resource-group", "my-component", "Visual Studio Team Services",
                com.azure.core.util.Context.NONE);
    }
}
```

### WorkItemConfigurations_List

```java
/**
 * Samples for WorkItemConfigurations List.
 */
public final class WorkItemConfigurationsListSamples {
    /*
     * x-ms-original-file: 2015-05-01/WorkItemConfigsGet.json
     */
    /**
     * Sample code: WorkItemConfigurationsList.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        workItemConfigurationsList(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.workItemConfigurations().list("my-resource-group", "my-component", com.azure.core.util.Context.NONE);
    }
}
```

### WorkItemConfigurations_UpdateItem

```java
import com.azure.resourcemanager.applicationinsights.models.WorkItemCreateConfiguration;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for WorkItemConfigurations UpdateItem.
 */
public final class WorkItemConfigurationsUpdateItemSamples {
    /*
     * x-ms-original-file: 2015-05-01/WorkItemConfigUpdate.json
     */
    /**
     * Sample code: WorkItemConfigurationsCreate.
     * 
     * @param manager Entry point to ApplicationInsightsManager.
     */
    public static void
        workItemConfigurationsCreate(com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager manager) {
        manager.workItemConfigurations()
            .updateItemWithResponse("my-resource-group", "my-component", "Visual Studio Team Services",
                new WorkItemCreateConfiguration().withConnectorId("d334e2a4-6733-488e-8645-a9fdc1694f41")
                    .withConnectorDataConfiguration(
                        "{\"VSOAccountBaseUrl\":\"https://testtodelete.visualstudio.com\",\"ProjectCollection\":\"DefaultCollection\",\"Project\":\"todeletefirst\",\"ResourceId\":\"d0662b05-439a-4a1b-840b-33a7f8b42ebf\",\"Custom\":\"{\\\"/fields/System.WorkItemType\\\":\\\"Bug\\\",\\\"/fields/System.AreaPath\\\":\\\"todeletefirst\\\",\\\"/fields/System.AssignedTo\\\":\\\"\\\"}\"}")
                    .withValidateOnly(true)
                    .withWorkItemProperties(mapOf("name", "Title", "value", "Validate Only Title")),
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

