# Code snippets and samples


## AuthenticationSettings

- [CreateOrUpdate](#authenticationsettings_createorupdate)
- [Delete](#authenticationsettings_delete)
- [Get](#authenticationsettings_get)
- [ListByHealthModel](#authenticationsettings_listbyhealthmodel)

## DiscoveryRules

- [CreateOrUpdate](#discoveryrules_createorupdate)
- [Delete](#discoveryrules_delete)
- [Get](#discoveryrules_get)
- [ListByHealthModel](#discoveryrules_listbyhealthmodel)

## Entities

- [CreateOrUpdate](#entities_createorupdate)
- [Delete](#entities_delete)
- [Get](#entities_get)
- [GetHistory](#entities_gethistory)
- [GetSignalHistory](#entities_getsignalhistory)
- [IngestHealthReport](#entities_ingesthealthreport)
- [ListByHealthModel](#entities_listbyhealthmodel)

## HealthModels

- [Create](#healthmodels_create)
- [Delete](#healthmodels_delete)
- [GetByResourceGroup](#healthmodels_getbyresourcegroup)
- [List](#healthmodels_list)
- [ListByResourceGroup](#healthmodels_listbyresourcegroup)
- [Update](#healthmodels_update)

## Operations

- [List](#operations_list)

## Relationships

- [CreateOrUpdate](#relationships_createorupdate)
- [Delete](#relationships_delete)
- [Get](#relationships_get)
- [ListByHealthModel](#relationships_listbyhealthmodel)

## SignalDefinitions

- [CreateOrUpdate](#signaldefinitions_createorupdate)
- [Delete](#signaldefinitions_delete)
- [Get](#signaldefinitions_get)
- [ListByHealthModel](#signaldefinitions_listbyhealthmodel)
### AuthenticationSettings_CreateOrUpdate

```java
import com.azure.resourcemanager.cloudhealth.models.ManagedIdentityAuthenticationSettingProperties;

/**
 * Samples for AuthenticationSettings CreateOrUpdate.
 */
public final class AuthenticationSettingsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/AuthenticationSettings_CreateOrUpdate.json
     */
    /**
     * Sample code: AuthenticationSettings_CreateOrUpdate.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void
        authenticationSettingsCreateOrUpdate(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.authenticationSettings()
            .define("myAuthSetting")
            .withExistingHealthmodel("myResourceGroup", "myHealthModel")
            .withProperties(new ManagedIdentityAuthenticationSettingProperties().withDisplayName("myDisplayName")
                .withManagedIdentityName("SystemAssigned"))
            .create();
    }
}
```

### AuthenticationSettings_Delete

```java
/**
 * Samples for AuthenticationSettings Delete.
 */
public final class AuthenticationSettingsDeleteSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/AuthenticationSettings_Delete.json
     */
    /**
     * Sample code: AuthenticationSettings_Delete.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void authenticationSettingsDelete(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.authenticationSettings()
            .delete("my-resource-group", "my-health-model", "my-auth-setting", com.azure.core.util.Context.NONE);
    }
}
```

### AuthenticationSettings_Get

```java
/**
 * Samples for AuthenticationSettings Get.
 */
public final class AuthenticationSettingsGetSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/AuthenticationSettings_Get.json
     */
    /**
     * Sample code: AuthenticationSettings_Get.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void authenticationSettingsGet(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.authenticationSettings()
            .getWithResponse("my-resource-group", "my-health-model", "my-auth-setting",
                com.azure.core.util.Context.NONE);
    }
}
```

### AuthenticationSettings_ListByHealthModel

```java
/**
 * Samples for AuthenticationSettings ListByHealthModel.
 */
public final class AuthenticationSettingsListByHealthModelSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/AuthenticationSettings_ListByHealthModel.json
     */
    /**
     * Sample code: AuthenticationSettings_ListByHealthModel.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void
        authenticationSettingsListByHealthModel(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.authenticationSettings()
            .listByHealthModel("my-resource-group", "my-health-model", com.azure.core.util.Context.NONE);
    }
}
```

### DiscoveryRules_CreateOrUpdate

```java
import com.azure.resourcemanager.cloudhealth.models.DiscoveryRuleProperties;
import com.azure.resourcemanager.cloudhealth.models.DiscoveryRuleRecommendedSignalsBehavior;
import com.azure.resourcemanager.cloudhealth.models.DiscoveryRuleRelationshipDiscoveryBehavior;
import com.azure.resourcemanager.cloudhealth.models.ResourceGraphQuerySpecification;

/**
 * Samples for DiscoveryRules CreateOrUpdate.
 */
public final class DiscoveryRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/DiscoveryRules_CreateOrUpdate.json
     */
    /**
     * Sample code: DiscoveryRules_CreateOrUpdate.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void discoveryRulesCreateOrUpdate(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.discoveryRules()
            .define("myDiscoveryRule")
            .withExistingHealthmodel("myResourceGroup", "myHealthModel")
            .withProperties(new DiscoveryRuleProperties().withDisplayName("myDisplayName")
                .withAuthenticationSetting("authSetting1")
                .withDiscoverRelationships(DiscoveryRuleRelationshipDiscoveryBehavior.ENABLED)
                .withAddRecommendedSignals(DiscoveryRuleRecommendedSignalsBehavior.ENABLED)
                .withSpecification(new ResourceGraphQuerySpecification().withResourceGraphQuery(
                    "resources | where subscriptionId == '7ddfffd7-9b32-40df-1234-828cbd55d6f4' | where resourceGroup == 'my-rg'")))
            .create();
    }
}
```

### DiscoveryRules_Delete

```java
/**
 * Samples for DiscoveryRules Delete.
 */
public final class DiscoveryRulesDeleteSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/DiscoveryRules_Delete.json
     */
    /**
     * Sample code: DiscoveryRules_Delete.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void discoveryRulesDelete(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.discoveryRules()
            .delete("my-resource-group", "my-health-model", "my-discovery-rule", com.azure.core.util.Context.NONE);
    }
}
```

### DiscoveryRules_Get

```java
/**
 * Samples for DiscoveryRules Get.
 */
public final class DiscoveryRulesGetSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/DiscoveryRules_Get.json
     */
    /**
     * Sample code: DiscoveryRules_Get.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void discoveryRulesGet(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.discoveryRules()
            .getWithResponse("myResourceGroup", "myHealthModel", "myDiscoveryRule", com.azure.core.util.Context.NONE);
    }
}
```

### DiscoveryRules_ListByHealthModel

```java

/**
 * Samples for DiscoveryRules ListByHealthModel.
 */
public final class DiscoveryRulesListByHealthModelSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/DiscoveryRules_ListByHealthModel.json
     */
    /**
     * Sample code: DiscoveryRules_ListByHealthModel.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void
        discoveryRulesListByHealthModel(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.discoveryRules()
            .listByHealthModel("my-resource-group", "my-health-model", null, com.azure.core.util.Context.NONE);
    }
}
```

### Entities_CreateOrUpdate

```java
import com.azure.resourcemanager.cloudhealth.models.AlertConfiguration;
import com.azure.resourcemanager.cloudhealth.models.AlertSeverity;
import com.azure.resourcemanager.cloudhealth.models.AzureMonitorWorkspaceSignals;
import com.azure.resourcemanager.cloudhealth.models.AzureResourceSignal;
import com.azure.resourcemanager.cloudhealth.models.AzureResourceSignals;
import com.azure.resourcemanager.cloudhealth.models.DependenciesAggregationType;
import com.azure.resourcemanager.cloudhealth.models.DependenciesAggregationUnit;
import com.azure.resourcemanager.cloudhealth.models.DependenciesSignalGroupV2;
import com.azure.resourcemanager.cloudhealth.models.EntityAlerts;
import com.azure.resourcemanager.cloudhealth.models.EntityCoordinates;
import com.azure.resourcemanager.cloudhealth.models.EntityImpact;
import com.azure.resourcemanager.cloudhealth.models.EntityProperties;
import com.azure.resourcemanager.cloudhealth.models.EvaluationRule;
import com.azure.resourcemanager.cloudhealth.models.IconDefinition;
import com.azure.resourcemanager.cloudhealth.models.LogAnalyticsSignal;
import com.azure.resourcemanager.cloudhealth.models.LogAnalyticsSignals;
import com.azure.resourcemanager.cloudhealth.models.MetricAggregationType;
import com.azure.resourcemanager.cloudhealth.models.PrometheusMetricsSignal;
import com.azure.resourcemanager.cloudhealth.models.RefreshInterval;
import com.azure.resourcemanager.cloudhealth.models.SignalGroups;
import com.azure.resourcemanager.cloudhealth.models.SignalOperator;
import com.azure.resourcemanager.cloudhealth.models.ThresholdRuleV2;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Entities CreateOrUpdate.
 */
public final class EntitiesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/Entities_CreateOrUpdate.json
     */
    /**
     * Sample code: Entities_CreateOrUpdate.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void entitiesCreateOrUpdate(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.entities()
            .define("uszrxbdkxesdrxhmagmzywebgbjj")
            .withExistingHealthmodel("rgopenapi", "myHealthModel")
            .withProperties(new EntityProperties().withDisplayName("My entity")
                .withCanvasPosition(new EntityCoordinates().withX(14.0).withY(13.0))
                .withIcon(new IconDefinition().withIconName("Custom").withCustomData("rcitntvapruccrhtxmkqjphbxunkz"))
                .withHealthObjective(62.0D)
                .withImpact(EntityImpact.STANDARD)
                .withTags(mapOf("key1376", "fakeTokenPlaceholder"))
                .withSignalGroups(new SignalGroups().withAzureResource(new AzureResourceSignals()
                    .withAuthenticationSetting("auth123")
                    .withAzureResourceId(
                        "/subscriptions/12345678-1234-1234-1234-123456789012/resourceGroups/rg1/providers/Microsoft.Compute/virtualMachines/vm1")
                    .withAzureResourceKind("functionapp")
                    .withSignals(Arrays.asList(new AzureResourceSignal().withName("uniqueSignalName1")
                        .withSignalDefinitionName("sigdef1")
                        .withMetricNamespace("microsoft.compute/virtualMachines")
                        .withMetricName("cpuusage")
                        .withTimeGrain("PT1M")
                        .withAggregationType(MetricAggregationType.NONE)
                        .withDimension("nodename")
                        .withDimensionFilter("node1")
                        .withDisplayName("CPU usage")
                        .withRefreshInterval(RefreshInterval.PT1M)
                        .withDataUnit("Count")
                        .withEvaluationRules(new EvaluationRule()
                            .withDegradedRule(new ThresholdRuleV2().withOperator(SignalOperator.fromString("LowerThan"))
                                .withThreshold(10.0))
                            .withUnhealthyRule(
                                new ThresholdRuleV2().withOperator(SignalOperator.fromString("LowerThan"))
                                    .withThreshold(1.0))))))
                    .withAzureLogAnalytics(new LogAnalyticsSignals().withAuthenticationSetting("auth123")
                        .withLogAnalyticsWorkspaceResourceId(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.OperationalInsights/workspaces/myworkspace")
                        .withSignals(Arrays.asList(new LogAnalyticsSignal().withName("uniqueSignalName2")
                            .withQueryText("print 1")
                            .withTimeGrain("PT30M")
                            .withValueColumnName("result")
                            .withDisplayName("Test LA signal")
                            .withRefreshInterval(RefreshInterval.PT1M)
                            .withDataUnit("my unit")
                            .withEvaluationRules(new EvaluationRule()
                                .withDegradedRule(
                                    new ThresholdRuleV2().withOperator(SignalOperator.GREATER_THAN).withThreshold(1.0))
                                .withUnhealthyRule(new ThresholdRuleV2().withOperator(SignalOperator.GREATER_THAN)
                                    .withThreshold(5.0))))))
                    .withAzureMonitorWorkspace(new AzureMonitorWorkspaceSignals().withAuthenticationSetting("auth123")
                        .withAzureMonitorWorkspaceResourceId(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.OperationalInsights/workspaces/myworkspace")
                        .withSignals(Arrays.asList(new PrometheusMetricsSignal().withName("pod-cpu-usage")
                            .withSignalDefinitionName("PodCpuUsageDefinition")
                            .withQueryText("rate(container_cpu_usage_seconds_total{pod=~\"my-app-.*\"}[5m]) * 100")
                            .withTimeGrain("PT5M")
                            .withDisplayName("Pod CPU Usage")
                            .withRefreshInterval(RefreshInterval.PT1M)
                            .withDataUnit("Percent")
                            .withEvaluationRules(new EvaluationRule()
                                .withDegradedRule(
                                    new ThresholdRuleV2().withOperator(SignalOperator.GREATER_THAN).withThreshold(70.0))
                                .withUnhealthyRule(new ThresholdRuleV2().withOperator(SignalOperator.GREATER_THAN)
                                    .withThreshold(90.0))))))
                    .withDependencies(
                        new DependenciesSignalGroupV2().withAggregationType(DependenciesAggregationType.MIN_HEALTHY)
                            .withDegradedThreshold(80.0D)
                            .withUnhealthyThreshold(50.0D)
                            .withUnit(DependenciesAggregationUnit.PERCENTAGE)))
                .withAlerts(new EntityAlerts().withUnhealthy(new AlertConfiguration().withSeverity(AlertSeverity.SEV1)
                    .withDescription("Alert description")
                    .withActionGroupIds(Arrays.asList(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.Insights/actionGroups/myactiongroup")))
                    .withDegraded(new AlertConfiguration().withSeverity(AlertSeverity.SEV4)
                        .withDescription("Alert description")
                        .withActionGroupIds(Arrays.asList(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.Insights/actionGroups/myactiongroup")))))
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

### Entities_Delete

```java
/**
 * Samples for Entities Delete.
 */
public final class EntitiesDeleteSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/Entities_Delete.json
     */
    /**
     * Sample code: Entities_Delete.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void entitiesDelete(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.entities()
            .delete("rgopenapi", "model1", "U4VTRFlUkm9kR6H23-c-6U-XHq7n", com.azure.core.util.Context.NONE);
    }
}
```

### Entities_Get

```java
/**
 * Samples for Entities Get.
 */
public final class EntitiesGetSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/Entities_Get.json
     */
    /**
     * Sample code: Entities_Get.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void entitiesGet(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.entities().getWithResponse("rgopenapi", "myHealthModel", "entity1", com.azure.core.util.Context.NONE);
    }
}
```

### Entities_GetHistory

```java
import com.azure.resourcemanager.cloudhealth.models.EntityHistoryRequest;
import java.time.OffsetDateTime;

/**
 * Samples for Entities GetHistory.
 */
public final class EntitiesGetHistorySamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/Entities_GetHistory.json
     */
    /**
     * Sample code: Entities_GetHistory.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void entitiesGetHistory(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.entities()
            .getHistoryWithResponse("rgopenapi", "myHealthModel", "entity1",
                new EntityHistoryRequest().withStartAt(OffsetDateTime.parse("2025-12-11T10:00:00Z"))
                    .withEndAt(OffsetDateTime.parse("2025-12-12T10:00:00Z")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Entities_GetSignalHistory

```java
import com.azure.resourcemanager.cloudhealth.models.SignalHistoryRequest;
import java.time.OffsetDateTime;

/**
 * Samples for Entities GetSignalHistory.
 */
public final class EntitiesGetSignalHistorySamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/Entities_GetSignalHistory.json
     */
    /**
     * Sample code: Entities_GetSignalHistory.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void entitiesGetSignalHistory(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.entities()
            .getSignalHistoryWithResponse("rgopenapi", "myHealthModel", "entity1",
                new SignalHistoryRequest().withSignalName("uniqueSignalName1")
                    .withStartAt(OffsetDateTime.parse("2025-12-11T10:00:00Z"))
                    .withEndAt(OffsetDateTime.parse("2025-12-12T10:00:00Z")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Entities_IngestHealthReport

```java
import com.azure.resourcemanager.cloudhealth.models.HealthReportEvaluationRule;
import com.azure.resourcemanager.cloudhealth.models.HealthReportRequest;
import com.azure.resourcemanager.cloudhealth.models.HealthState;
import com.azure.resourcemanager.cloudhealth.models.SignalOperator;
import com.azure.resourcemanager.cloudhealth.models.ThresholdRuleV2;

/**
 * Samples for Entities IngestHealthReport.
 */
public final class EntitiesIngestHealthReportSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/Entities_IngestHealthReport.json
     */
    /**
     * Sample code: Entities_IngestHealthReport.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void entitiesIngestHealthReport(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.entities()
            .ingestHealthReportWithResponse("rgopenapi", "myHealthModel", "entity1",
                new HealthReportRequest().withSignalName("uniqueSignalName1")
                    .withHealthState(HealthState.DEGRADED)
                    .withValue(85.5D)
                    .withEvaluationRules(new HealthReportEvaluationRule()
                        .withDegradedRule(
                            new ThresholdRuleV2().withOperator(SignalOperator.GREATER_THAN).withThreshold(70.0))
                        .withUnhealthyRule(
                            new ThresholdRuleV2().withOperator(SignalOperator.GREATER_THAN).withThreshold(90.0)))
                    .withExpiresInMinutes(60)
                    .withAdditionalContext("CPU usage elevated due to batch processing job"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Entities_ListByHealthModel

```java

/**
 * Samples for Entities ListByHealthModel.
 */
public final class EntitiesListByHealthModelSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/Entities_ListByHealthModel.json
     */
    /**
     * Sample code: Entities_ListByHealthModel.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void entitiesListByHealthModel(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.entities()
            .listByHealthModel("rgopenapi", "gPWT6GP85xRV248L7LhNRTD--2Yc73wu-5Qk-0tS", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### HealthModels_Create

```java
import com.azure.resourcemanager.cloudhealth.models.HealthModelProperties;
import com.azure.resourcemanager.cloudhealth.models.ManagedServiceIdentity;
import com.azure.resourcemanager.cloudhealth.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.cloudhealth.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for HealthModels Create.
 */
public final class HealthModelsCreateSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/HealthModels_Create.json
     */
    /**
     * Sample code: HealthModels_Create.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void healthModelsCreate(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.healthModels()
            .define("model1")
            .withRegion("eastus2")
            .withExistingResourceGroup("rgopenapi")
            .withTags(mapOf("key2961", "fakeTokenPlaceholder"))
            .withProperties(new HealthModelProperties())
            .withIdentity(new ManagedServiceIdentity()
                .withType(ManagedServiceIdentityType.fromString("SystemAssigned, UserAssigned"))
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/4980D7D5-4E07-47AD-AD34-E76C6BC9F061/resourceGroups/rgopenapi/providers/Microsoft.ManagedIdentity/userAssignedIdentities/ua1",
                    new UserAssignedIdentity())))
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

### HealthModels_Delete

```java
/**
 * Samples for HealthModels Delete.
 */
public final class HealthModelsDeleteSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/HealthModels_Delete.json
     */
    /**
     * Sample code: HealthModels_Delete.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void healthModelsDelete(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.healthModels().delete("rgopenapi", "model1", com.azure.core.util.Context.NONE);
    }
}
```

### HealthModels_GetByResourceGroup

```java
/**
 * Samples for HealthModels GetByResourceGroup.
 */
public final class HealthModelsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/HealthModels_Get.json
     */
    /**
     * Sample code: HealthModels_Get.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void healthModelsGet(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.healthModels()
            .getByResourceGroupWithResponse("rgopenapi", "myHealthModel", com.azure.core.util.Context.NONE);
    }
}
```

### HealthModels_List

```java
/**
 * Samples for HealthModels List.
 */
public final class HealthModelsListSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/HealthModels_ListBySubscription.json
     */
    /**
     * Sample code: HealthModels_ListBySubscription.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void
        healthModelsListBySubscription(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.healthModels().list(com.azure.core.util.Context.NONE);
    }
}
```

### HealthModels_ListByResourceGroup

```java
/**
 * Samples for HealthModels ListByResourceGroup.
 */
public final class HealthModelsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/HealthModels_ListByResourceGroup.json
     */
    /**
     * Sample code: HealthModels_ListByResourceGroup.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void
        healthModelsListByResourceGroup(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.healthModels().listByResourceGroup("rgopenapi", com.azure.core.util.Context.NONE);
    }
}
```

### HealthModels_Update

```java
import com.azure.resourcemanager.cloudhealth.models.HealthModel;
import com.azure.resourcemanager.cloudhealth.models.ManagedServiceIdentity;
import com.azure.resourcemanager.cloudhealth.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.cloudhealth.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for HealthModels Update.
 */
public final class HealthModelsUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/HealthModels_Update.json
     */
    /**
     * Sample code: HealthModels_Update.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void healthModelsUpdate(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        HealthModel resource = manager.healthModels()
            .getByResourceGroupWithResponse("rgopenapi", "model1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key21", "fakeTokenPlaceholder"))
            .withIdentity(new ManagedServiceIdentity()
                .withType(ManagedServiceIdentityType.fromString("SystemAssigned, UserAssigned"))
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/4980D7D5-4E07-47AD-AD34-E76C6BC9F061/resourceGroups/rgopenapi/providers/Microsoft.ManagedIdentity/userAssignedIdentities/ua1",
                    new UserAssignedIdentity())))
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
     * x-ms-original-file: 2026-01-01-preview/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void operationsList(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Relationships_CreateOrUpdate

```java
import com.azure.resourcemanager.cloudhealth.models.RelationshipProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Relationships CreateOrUpdate.
 */
public final class RelationshipsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/Relationships_CreateOrUpdate.json
     */
    /**
     * Sample code: Relationships_CreateOrUpdate.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void relationshipsCreateOrUpdate(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.relationships()
            .define("rel1")
            .withExistingHealthmodel("rgopenapi", "model1")
            .withProperties(new RelationshipProperties().withDisplayName("My relationship")
                .withParentEntityName("Entity1")
                .withChildEntityName("Entity2")
                .withTags(mapOf("key9681", "fakeTokenPlaceholder")))
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

### Relationships_Delete

```java
/**
 * Samples for Relationships Delete.
 */
public final class RelationshipsDeleteSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/Relationships_Delete.json
     */
    /**
     * Sample code: Relationships_Delete.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void relationshipsDelete(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.relationships().delete("rgopenapi", "model1", "rel1", com.azure.core.util.Context.NONE);
    }
}
```

### Relationships_Get

```java
/**
 * Samples for Relationships Get.
 */
public final class RelationshipsGetSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/Relationships_Get.json
     */
    /**
     * Sample code: Relationships_Get.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void relationshipsGet(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.relationships()
            .getWithResponse("rgopenapi", "myHealthModel", "Ue-21-F3M12V3w-13x18F8H-7HOk--kq6tP-HB",
                com.azure.core.util.Context.NONE);
    }
}
```

### Relationships_ListByHealthModel

```java

/**
 * Samples for Relationships ListByHealthModel.
 */
public final class RelationshipsListByHealthModelSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/Relationships_ListByHealthModel.json
     */
    /**
     * Sample code: Relationships_ListByHealthModel.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void
        relationshipsListByHealthModel(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.relationships().listByHealthModel("rgopenapi", "model1", null, com.azure.core.util.Context.NONE);
    }
}
```

### SignalDefinitions_CreateOrUpdate

```java
import com.azure.resourcemanager.cloudhealth.models.EvaluationRule;
import com.azure.resourcemanager.cloudhealth.models.MetricAggregationType;
import com.azure.resourcemanager.cloudhealth.models.RefreshInterval;
import com.azure.resourcemanager.cloudhealth.models.ResourceMetricSignalDefinitionProperties;
import com.azure.resourcemanager.cloudhealth.models.SignalOperator;
import com.azure.resourcemanager.cloudhealth.models.ThresholdRuleV2;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SignalDefinitions CreateOrUpdate.
 */
public final class SignalDefinitionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/SignalDefinitions_CreateOrUpdate.json
     */
    /**
     * Sample code: SignalDefinitions_CreateOrUpdate.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void
        signalDefinitionsCreateOrUpdate(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.signalDefinitions()
            .define("sig1")
            .withExistingHealthmodel("rgopenapi", "myHealthModel")
            .withProperties(new ResourceMetricSignalDefinitionProperties().withDisplayName("cpu usage")
                .withRefreshInterval(RefreshInterval.PT1M)
                .withTags(mapOf("key4788", "fakeTokenPlaceholder"))
                .withDataUnit("byte")
                .withEvaluationRules(new EvaluationRule()
                    .withDegradedRule(
                        new ThresholdRuleV2().withOperator(SignalOperator.fromString("LowerThan")).withThreshold(65.0))
                    .withUnhealthyRule(
                        new ThresholdRuleV2().withOperator(SignalOperator.fromString("LowerThan")).withThreshold(60.0)))
                .withMetricNamespace("microsoft.compute/virtualMachines")
                .withMetricName("cpuusage")
                .withTimeGrain("PT1M")
                .withAggregationType(MetricAggregationType.NONE)
                .withDimension("nodename")
                .withDimensionFilter("node1"))
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

### SignalDefinitions_Delete

```java
/**
 * Samples for SignalDefinitions Delete.
 */
public final class SignalDefinitionsDeleteSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/SignalDefinitions_Delete.json
     */
    /**
     * Sample code: SignalDefinitions_Delete.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void signalDefinitionsDelete(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.signalDefinitions().delete("rgopenapi", "model1", "sig", com.azure.core.util.Context.NONE);
    }
}
```

### SignalDefinitions_Get

```java
/**
 * Samples for SignalDefinitions Get.
 */
public final class SignalDefinitionsGetSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/SignalDefinitions_Get.json
     */
    /**
     * Sample code: SignalDefinitions_Get.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void signalDefinitionsGet(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.signalDefinitions()
            .getWithResponse("rgopenapi", "myHealthModel", "sig1", com.azure.core.util.Context.NONE);
    }
}
```

### SignalDefinitions_ListByHealthModel

```java

/**
 * Samples for SignalDefinitions ListByHealthModel.
 */
public final class SignalDefinitionsListByHealthModelSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/SignalDefinitions_ListByHealthModel.json
     */
    /**
     * Sample code: SignalDefinitions_ListByHealthModel.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void
        signalDefinitionsListByHealthModel(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.signalDefinitions()
            .listByHealthModel("rgopenapi", "myHealthModel", null, com.azure.core.util.Context.NONE);
    }
}
```

