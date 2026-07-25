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

- [AddDataAnnotation](#entities_adddataannotation)
- [CreateOrUpdate](#entities_createorupdate)
- [Delete](#entities_delete)
- [Get](#entities_get)
- [GetDataAnnotations](#entities_getdataannotations)
- [GetHistory](#entities_gethistory)
- [GetSignalHistory](#entities_getsignalhistory)
- [GetSignalRecommendations](#entities_getsignalrecommendations)
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
     * x-ms-original-file: 2026-05-01-preview/AuthenticationSettings_CreateOrUpdate.json
     */
    /**
     * Sample code: AuthenticationSettings_CreateOrUpdate.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void
        authenticationSettingsCreateOrUpdate(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.authenticationSettings()
            .define("default-auth")
            .withExistingHealthmodel("online-store-rg", "online-store")
            .withProperties(
                new ManagedIdentityAuthenticationSettingProperties().withDisplayName("Default managed identity")
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
     * x-ms-original-file: 2026-05-01-preview/AuthenticationSettings_Delete.json
     */
    /**
     * Sample code: AuthenticationSettings_Delete.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void authenticationSettingsDelete(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.authenticationSettings()
            .delete("online-store-rg", "online-store", "default-auth", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/AuthenticationSettings_Get.json
     */
    /**
     * Sample code: AuthenticationSettings_Get.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void authenticationSettingsGet(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.authenticationSettings()
            .getWithResponse("online-store-rg", "online-store", "default-auth", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/AuthenticationSettings_ListByHealthModel.json
     */
    /**
     * Sample code: AuthenticationSettings_ListByHealthModel.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void
        authenticationSettingsListByHealthModel(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.authenticationSettings()
            .listByHealthModel("online-store-rg", "online-store", com.azure.core.util.Context.NONE);
    }
}
```

### DiscoveryRules_CreateOrUpdate

```java
import com.azure.resourcemanager.cloudhealth.models.DiscoveryRuleProperties;
import com.azure.resourcemanager.cloudhealth.models.DiscoveryRuleRecommendedSignalsBehavior;
import com.azure.resourcemanager.cloudhealth.models.DiscoveryRuleRelationshipDiscoveryBehavior;
import com.azure.resourcemanager.cloudhealth.models.ResourceGraphQuerySpecification;
import com.azure.resourcemanager.cloudhealth.models.ResourceHealthAvailabilityStateSignalBehavior;

/**
 * Samples for DiscoveryRules CreateOrUpdate.
 */
public final class DiscoveryRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/DiscoveryRules_CreateOrUpdate.json
     */
    /**
     * Sample code: DiscoveryRules_CreateOrUpdate.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void discoveryRulesCreateOrUpdate(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.discoveryRules()
            .define("discover-web-apps")
            .withExistingHealthmodel("online-store-rg", "online-store")
            .withProperties(new DiscoveryRuleProperties().withDisplayName("Discover web apps")
                .withAuthenticationSetting("default-auth")
                .withDiscoverRelationships(DiscoveryRuleRelationshipDiscoveryBehavior.ENABLED)
                .withAddRecommendedSignals(DiscoveryRuleRecommendedSignalsBehavior.ENABLED)
                .withSpecification(new ResourceGraphQuerySpecification().withResourceGraphQuery(
                    "resources | where type =~ 'microsoft.web/sites' and resourceGroup =~ 'online-store-rg' | project id, name, location"))
                .withAddResourceHealthSignal(ResourceHealthAvailabilityStateSignalBehavior.ENABLED))
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
     * x-ms-original-file: 2026-05-01-preview/DiscoveryRules_Delete.json
     */
    /**
     * Sample code: DiscoveryRules_Delete.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void discoveryRulesDelete(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.discoveryRules()
            .delete("online-store-rg", "online-store", "discover-web-apps", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/DiscoveryRules_Get.json
     */
    /**
     * Sample code: DiscoveryRules_Get.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void discoveryRulesGet(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.discoveryRules()
            .getWithResponse("online-store-rg", "online-store", "discover-web-apps", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/DiscoveryRules_ListByHealthModel.json
     */
    /**
     * Sample code: DiscoveryRules_ListByHealthModel.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void
        discoveryRulesListByHealthModel(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.discoveryRules()
            .listByHealthModel("online-store-rg", "online-store", null, com.azure.core.util.Context.NONE);
    }
}
```

### Entities_AddDataAnnotation

```java
import com.azure.resourcemanager.cloudhealth.models.AddDataAnnotationRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Entities AddDataAnnotation.
 */
public final class EntitiesAddDataAnnotationSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/Entities_AddDataAnnotation.json
     */
    /**
     * Sample code: Entities_AddDataAnnotation.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void entitiesAddDataAnnotation(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.entities()
            .addDataAnnotationWithResponse("online-store-rg", "online-store", "web-frontend",
                new AddDataAnnotationRequest()
                    .withAnnotationDetails(mapOf("environment", "production", "deploymentId", "deploy-2026-05-04-001",
                        "changedBy", "release-pipeline"))
                    .withDescription("Deployed release 2.4.1 to the web frontend."),
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

### Entities_CreateOrUpdate

```java
import com.azure.resourcemanager.cloudhealth.models.AlertConfiguration;
import com.azure.resourcemanager.cloudhealth.models.AlertSeverity;
import com.azure.resourcemanager.cloudhealth.models.AzureMonitorWorkspaceSignals;
import com.azure.resourcemanager.cloudhealth.models.AzureResourceHealthSignal;
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
import com.azure.resourcemanager.cloudhealth.models.ResourceHealthAvailabilityStateSignalBehavior;
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
     * x-ms-original-file: 2026-05-01-preview/Entities_CreateOrUpdate.json
     */
    /**
     * Sample code: Entities_CreateOrUpdate.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void entitiesCreateOrUpdate(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.entities()
            .define("orders-api")
            .withExistingHealthmodel("online-store-rg", "online-store")
            .withProperties(new EntityProperties().withDisplayName("Orders API")
                .withCanvasPosition(new EntityCoordinates().withX(360.0).withY(240.0))
                .withIcon(new IconDefinition().withIconName("Kubernetes"))
                .withHealthObjective(99.9D)
                .withImpact(EntityImpact.STANDARD)
                .withTags(mapOf("environment", "production", "team", "online-store"))
                .withSignalGroups(new SignalGroups().withAzureResource(new AzureResourceSignals()
                    .withAuthenticationSetting("default-auth")
                    .withAzureResourceId(
                        "/subscriptions/abcdef12-3456-7890-abcd-ef1234567890/resourceGroups/online-store-rg/providers/Microsoft.ContainerService/managedClusters/online-store-aks")
                    .withAzureResourceKind("managedClusters")
                    .withSignals(Arrays.asList(new AzureResourceSignal().withName("node-cpu")
                        .withMetricNamespace("Microsoft.ContainerService/managedClusters")
                        .withMetricName("node_cpu_usage_percentage")
                        .withTimeGrain("PT5M")
                        .withAggregationType(MetricAggregationType.AVERAGE)
                        .withDisplayName("Node CPU utilization")
                        .withRefreshInterval(RefreshInterval.PT1M)
                        .withDataUnit("Percent")
                        .withEvaluationRules(new EvaluationRule()
                            .withDegradedRule(
                                new ThresholdRuleV2().withOperator(SignalOperator.GREATER_THAN).withThreshold(70.0D))
                            .withUnhealthyRule(
                                new ThresholdRuleV2().withOperator(SignalOperator.GREATER_THAN).withThreshold(90.0D)))))
                    .withResourceHealth(new AzureResourceHealthSignal()
                        .withEnabled(ResourceHealthAvailabilityStateSignalBehavior.ENABLED)))
                    .withAzureLogAnalytics(new LogAnalyticsSignals().withAuthenticationSetting("default-auth")
                        .withLogAnalyticsWorkspaceResourceId(
                            "/subscriptions/abcdef12-3456-7890-abcd-ef1234567890/resourceGroups/online-store-rg/providers/Microsoft.OperationalInsights/workspaces/online-store-law")
                        .withSignals(Arrays.asList(new LogAnalyticsSignal().withName("unhealthy-pods")
                            .withQueryText(
                                "KubePodInventory | where TimeGenerated > ago(5m) | where Namespace == 'online-store' | where PodStatus != 'Running' | summarize unhealthyPods = dcount(Name)")
                            .withTimeGrain("PT5M")
                            .withValueColumnName("unhealthyPods")
                            .withDisplayName("Unhealthy pods")
                            .withRefreshInterval(RefreshInterval.PT5M)
                            .withDataUnit("Count")
                            .withEvaluationRules(new EvaluationRule()
                                .withDegradedRule(
                                    new ThresholdRuleV2().withOperator(SignalOperator.GREATER_THAN).withThreshold(0.0D))
                                .withUnhealthyRule(new ThresholdRuleV2().withOperator(SignalOperator.GREATER_THAN)
                                    .withThreshold(2.0D))))))
                    .withAzureMonitorWorkspace(new AzureMonitorWorkspaceSignals()
                        .withAuthenticationSetting("default-auth")
                        .withAzureMonitorWorkspaceResourceId(
                            "/subscriptions/abcdef12-3456-7890-abcd-ef1234567890/resourceGroups/online-store-rg/providers/Microsoft.Monitor/accounts/online-store-amw")
                        .withSignals(Arrays.asList(new PrometheusMetricsSignal().withName("error-rate")
                            .withQueryText(
                                "sum(rate(http_requests_total{job=\"orders-api\", code=~\"5..\"}[5m])) / sum(rate(http_requests_total{job=\"orders-api\"}[5m])) * 100")
                            .withTimeGrain("PT5M")
                            .withDisplayName("HTTP 5xx error rate")
                            .withRefreshInterval(RefreshInterval.PT1M)
                            .withDataUnit("Percent")
                            .withEvaluationRules(new EvaluationRule()
                                .withDegradedRule(
                                    new ThresholdRuleV2().withOperator(SignalOperator.GREATER_THAN).withThreshold(1.0D))
                                .withUnhealthyRule(new ThresholdRuleV2().withOperator(SignalOperator.GREATER_THAN)
                                    .withThreshold(5.0D))),
                            new PrometheusMetricsSignal().withName("p95-latency")
                                .withQueryText(
                                    "histogram_quantile(0.95, sum by (le) (rate(http_request_duration_seconds_bucket{job=\"orders-api\"}[5m]))) * 1000")
                                .withTimeGrain("PT5M")
                                .withDisplayName("p95 request latency")
                                .withRefreshInterval(RefreshInterval.PT1M)
                                .withDataUnit("MilliSeconds")
                                .withEvaluationRules(new EvaluationRule()
                                    .withDegradedRule(new ThresholdRuleV2().withOperator(SignalOperator.GREATER_THAN)
                                        .withThreshold(300.0D))
                                    .withUnhealthyRule(new ThresholdRuleV2().withOperator(SignalOperator.GREATER_THAN)
                                        .withThreshold(800.0D))),
                            new PrometheusMetricsSignal().withName("pod-cpu")
                                .withSignalDefinitionName("pod-cpu-usage")
                                .withQueryText(
                                    "sum(rate(container_cpu_usage_seconds_total{namespace=\"online-store\", pod=~\"orders-api-.*\"}[5m])) * 100")
                                .withTimeGrain("PT5M")
                                .withDisplayName("Pod CPU utilization")
                                .withRefreshInterval(RefreshInterval.PT1M)
                                .withDataUnit("Percent")
                                .withEvaluationRules(new EvaluationRule()
                                    .withDegradedRule(new ThresholdRuleV2().withOperator(SignalOperator.GREATER_THAN)
                                        .withThreshold(70.0D))
                                    .withUnhealthyRule(new ThresholdRuleV2().withOperator(SignalOperator.GREATER_THAN)
                                        .withThreshold(90.0D))))))
                    .withDependencies(
                        new DependenciesSignalGroupV2().withAggregationType(DependenciesAggregationType.MIN_HEALTHY)
                            .withDegradedThreshold(100.0D)
                            .withUnhealthyThreshold(50.0D)
                            .withUnit(DependenciesAggregationUnit.PERCENTAGE)
                            .withIgnoreUnknown(true)))
                .withAlerts(new EntityAlerts().withUnhealthy(new AlertConfiguration().withSeverity(AlertSeverity.SEV1)
                    .withDescription("Orders API is unhealthy.")
                    .withActionGroupIds(Arrays.asList(
                        "/subscriptions/abcdef12-3456-7890-abcd-ef1234567890/resourceGroups/online-store-rg/providers/Microsoft.Insights/actionGroups/online-store-oncall")))
                    .withDegraded(new AlertConfiguration().withSeverity(AlertSeverity.SEV3)
                        .withDescription("Orders API is degraded.")
                        .withActionGroupIds(Arrays.asList(
                            "/subscriptions/abcdef12-3456-7890-abcd-ef1234567890/resourceGroups/online-store-rg/providers/Microsoft.Insights/actionGroups/online-store-oncall")))))
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
     * x-ms-original-file: 2026-05-01-preview/Entities_Delete.json
     */
    /**
     * Sample code: Entities_Delete.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void entitiesDelete(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.entities()
            .delete("online-store-rg", "online-store", "catalog-storage", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/Entities_Get.json
     */
    /**
     * Sample code: Entities_Get.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void entitiesGet(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.entities()
            .getWithResponse("online-store-rg", "online-store", "orders-db", com.azure.core.util.Context.NONE);
    }
}
```

### Entities_GetDataAnnotations

```java
import com.azure.resourcemanager.cloudhealth.models.GetDataAnnotationsRequest;
import java.time.OffsetDateTime;

/**
 * Samples for Entities GetDataAnnotations.
 */
public final class EntitiesGetDataAnnotationsSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/Entities_GetDataAnnotations.json
     */
    /**
     * Sample code: Entities_GetDataAnnotations.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void entitiesGetDataAnnotations(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.entities()
            .getDataAnnotationsWithResponse("online-store-rg", "online-store", "web-frontend",
                new GetDataAnnotationsRequest().withStartAt(OffsetDateTime.parse("2026-05-03T00:00:00Z"))
                    .withEndAt(OffsetDateTime.parse("2026-05-04T23:59:59Z")),
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/Entities_GetHistory.json
     */
    /**
     * Sample code: Entities_GetHistory.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void entitiesGetHistory(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.entities()
            .getHistoryWithResponse("online-store-rg", "online-store", "web-frontend",
                new EntityHistoryRequest().withStartAt(OffsetDateTime.parse("2026-05-03T09:30:00Z"))
                    .withEndAt(OffsetDateTime.parse("2026-05-04T09:30:00Z"))
                    .withTop(100),
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
     * x-ms-original-file: 2026-05-01-preview/Entities_GetSignalHistory.json
     */
    /**
     * Sample code: Entities_GetSignalHistory.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void entitiesGetSignalHistory(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.entities()
            .getSignalHistoryWithResponse("online-store-rg", "online-store", "web-frontend",
                new SignalHistoryRequest().withSignalName("http-5xx")
                    .withStartAt(OffsetDateTime.parse("2026-05-03T09:30:00Z"))
                    .withEndAt(OffsetDateTime.parse("2026-05-04T09:30:00Z"))
                    .withTop(7),
                com.azure.core.util.Context.NONE);
    }
}
```

### Entities_GetSignalRecommendations

```java
/**
 * Samples for Entities GetSignalRecommendations.
 */
public final class EntitiesGetSignalRecommendationsSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/Entities_GetSignalRecommendations.json
     */
    /**
     * Sample code: Entities_GetSignalRecommendations.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void
        entitiesGetSignalRecommendations(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.entities()
            .getSignalRecommendationsWithResponse("online-store-rg", "online-store", "orders-db",
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
     * x-ms-original-file: 2026-05-01-preview/Entities_IngestHealthReport.json
     */
    /**
     * Sample code: Entities_IngestHealthReport.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void entitiesIngestHealthReport(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.entities()
            .ingestHealthReportWithResponse("online-store-rg", "online-store", "orders-api",
                new HealthReportRequest().withSignalName("error-rate")
                    .withHealthState(HealthState.UNHEALTHY)
                    .withValue(6.5D)
                    .withEvaluationRules(new HealthReportEvaluationRule()
                        .withDegradedRule(
                            new ThresholdRuleV2().withOperator(SignalOperator.GREATER_THAN).withThreshold(1.0D))
                        .withUnhealthyRule(
                            new ThresholdRuleV2().withOperator(SignalOperator.GREATER_THAN).withThreshold(5.0D)))
                    .withExpiresInMinutes(60)
                    .withAdditionalContext("Elevated 5xx error rate during the checkout traffic spike."),
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
     * x-ms-original-file: 2026-05-01-preview/Entities_ListByHealthModel.json
     */
    /**
     * Sample code: Entities_ListByHealthModel.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void entitiesListByHealthModel(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.entities().listByHealthModel("online-store-rg", "online-store", null, com.azure.core.util.Context.NONE);
    }
}
```

### HealthModels_Create

```java
import com.azure.resourcemanager.cloudhealth.models.HealthModelProperties;
import com.azure.resourcemanager.cloudhealth.models.ManagedServiceIdentity;
import com.azure.resourcemanager.cloudhealth.models.ManagedServiceIdentityType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for HealthModels Create.
 */
public final class HealthModelsCreateSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/HealthModels_Create.json
     */
    /**
     * Sample code: HealthModels_Create.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void healthModelsCreate(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.healthModels()
            .define("online-store")
            .withRegion("eastus")
            .withExistingResourceGroup("online-store-rg")
            .withTags(mapOf("environment", "production", "team", "online-store"))
            .withProperties(new HealthModelProperties())
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
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
     * x-ms-original-file: 2026-05-01-preview/HealthModels_Delete.json
     */
    /**
     * Sample code: HealthModels_Delete.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void healthModelsDelete(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.healthModels().delete("online-store-rg", "online-store", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/HealthModels_Get.json
     */
    /**
     * Sample code: HealthModels_Get.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void healthModelsGet(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.healthModels()
            .getByResourceGroupWithResponse("online-store-rg", "online-store", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/HealthModels_ListBySubscription.json
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
     * x-ms-original-file: 2026-05-01-preview/HealthModels_ListByResourceGroup.json
     */
    /**
     * Sample code: HealthModels_ListByResourceGroup.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void
        healthModelsListByResourceGroup(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.healthModels().listByResourceGroup("online-store-rg", com.azure.core.util.Context.NONE);
    }
}
```

### HealthModels_Update

```java
import com.azure.resourcemanager.cloudhealth.models.HealthModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for HealthModels Update.
 */
public final class HealthModelsUpdateSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/HealthModels_Update.json
     */
    /**
     * Sample code: HealthModels_Update.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void healthModelsUpdate(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        HealthModel resource = manager.healthModels()
            .getByResourceGroupWithResponse("online-store-rg", "online-store", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("environment", "production", "team", "online-store", "tier", "gold")).apply();
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
     * x-ms-original-file: 2026-05-01-preview/Operations_List.json
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
     * x-ms-original-file: 2026-05-01-preview/Relationships_CreateOrUpdate.json
     */
    /**
     * Sample code: Relationships_CreateOrUpdate.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void relationshipsCreateOrUpdate(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.relationships()
            .define("web-frontend-to-orders-api")
            .withExistingHealthmodel("online-store-rg", "online-store")
            .withProperties(new RelationshipProperties().withDisplayName("Web Frontend depends on Orders API")
                .withParentEntityName("web-frontend")
                .withChildEntityName("orders-api")
                .withTags(mapOf("environment", "production", "team", "online-store")))
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
     * x-ms-original-file: 2026-05-01-preview/Relationships_Delete.json
     */
    /**
     * Sample code: Relationships_Delete.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void relationshipsDelete(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.relationships()
            .delete("online-store-rg", "online-store", "orders-api-to-catalog-storage",
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/Relationships_Get.json
     */
    /**
     * Sample code: Relationships_Get.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void relationshipsGet(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.relationships()
            .getWithResponse("online-store-rg", "online-store", "web-frontend-to-orders-api",
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
     * x-ms-original-file: 2026-05-01-preview/Relationships_ListByHealthModel.json
     */
    /**
     * Sample code: Relationships_ListByHealthModel.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void
        relationshipsListByHealthModel(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.relationships()
            .listByHealthModel("online-store-rg", "online-store", null, com.azure.core.util.Context.NONE);
    }
}
```

### SignalDefinitions_CreateOrUpdate

```java
import com.azure.resourcemanager.cloudhealth.models.DynamicThresholdSensitivity;
import com.azure.resourcemanager.cloudhealth.models.EvaluationRule;
import com.azure.resourcemanager.cloudhealth.models.LookBackWindow;
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
     * x-ms-original-file: 2026-05-01-preview/SignalDefinitions_CreateOrUpdate.json
     */
    /**
     * Sample code: SignalDefinitions_CreateOrUpdate.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void
        signalDefinitionsCreateOrUpdate(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.signalDefinitions()
            .define("sql-cpu-percent")
            .withExistingHealthmodel("online-store-rg", "online-store")
            .withProperties(new ResourceMetricSignalDefinitionProperties().withDisplayName("SQL CPU utilization")
                .withRefreshInterval(RefreshInterval.PT1M)
                .withTags(mapOf("environment", "production", "team", "online-store"))
                .withDataUnit("Percent")
                .withEvaluationRules(new EvaluationRule()
                    .withDegradedRule(
                        new ThresholdRuleV2().withOperator(SignalOperator.GREATER_THAN).withThreshold(70.0D))
                    .withUnhealthyRule(new ThresholdRuleV2().withOperator(SignalOperator.DYNAMIC)
                        .withSensitivity(DynamicThresholdSensitivity.MEDIUM)
                        .withLookBackWindow(LookBackWindow.PT1H)))
                .withMetricNamespace("Microsoft.Sql/servers/databases")
                .withMetricName("cpu_percent")
                .withTimeGrain("PT5M")
                .withAggregationType(MetricAggregationType.AVERAGE))
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
     * x-ms-original-file: 2026-05-01-preview/SignalDefinitions_Delete.json
     */
    /**
     * Sample code: SignalDefinitions_Delete.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void signalDefinitionsDelete(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.signalDefinitions()
            .delete("online-store-rg", "online-store", "sql-cpu-percent", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/SignalDefinitions_Get.json
     */
    /**
     * Sample code: SignalDefinitions_Get.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void signalDefinitionsGet(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.signalDefinitions()
            .getWithResponse("online-store-rg", "online-store", "sql-cpu-percent", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-01-preview/SignalDefinitions_ListByHealthModel.json
     */
    /**
     * Sample code: SignalDefinitions_ListByHealthModel.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void
        signalDefinitionsListByHealthModel(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.signalDefinitions()
            .listByHealthModel("online-store-rg", "online-store", null, com.azure.core.util.Context.NONE);
    }
}
```

