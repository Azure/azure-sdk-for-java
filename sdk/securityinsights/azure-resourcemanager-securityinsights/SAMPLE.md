# Code snippets and samples


## Actions

- [CreateOrUpdate](#actions_createorupdate)
- [Delete](#actions_delete)
- [Get](#actions_get)
- [ListByAlertRule](#actions_listbyalertrule)

## AlertRuleTemplates

- [Get](#alertruletemplates_get)
- [List](#alertruletemplates_list)

## AlertRules

- [CreateOrUpdate](#alertrules_createorupdate)
- [Delete](#alertrules_delete)
- [Get](#alertrules_get)
- [List](#alertrules_list)

## AutomationRules

- [CreateOrUpdate](#automationrules_createorupdate)
- [Delete](#automationrules_delete)
- [Get](#automationrules_get)
- [List](#automationrules_list)

## Bookmarks

- [CreateOrUpdate](#bookmarks_createorupdate)
- [Delete](#bookmarks_delete)
- [Get](#bookmarks_get)
- [List](#bookmarks_list)

## DataConnectors

- [CreateOrUpdate](#dataconnectors_createorupdate)
- [Delete](#dataconnectors_delete)
- [Get](#dataconnectors_get)
- [List](#dataconnectors_list)

## IncidentComments

- [CreateOrUpdate](#incidentcomments_createorupdate)
- [Delete](#incidentcomments_delete)
- [Get](#incidentcomments_get)
- [List](#incidentcomments_list)

## IncidentRelations

- [CreateOrUpdate](#incidentrelations_createorupdate)
- [Delete](#incidentrelations_delete)
- [Get](#incidentrelations_get)
- [List](#incidentrelations_list)

## Incidents

- [CreateOrUpdate](#incidents_createorupdate)
- [Delete](#incidents_delete)
- [Get](#incidents_get)
- [List](#incidents_list)
- [ListAlerts](#incidents_listalerts)
- [ListBookmarks](#incidents_listbookmarks)
- [ListEntities](#incidents_listentities)

## Operations

- [List](#operations_list)

## SecurityMLAnalyticsSettings

- [CreateOrUpdate](#securitymlanalyticssettings_createorupdate)
- [Delete](#securitymlanalyticssettings_delete)
- [Get](#securitymlanalyticssettings_get)
- [List](#securitymlanalyticssettings_list)

## SentinelOnboardingStates

- [Create](#sentinelonboardingstates_create)
- [Delete](#sentinelonboardingstates_delete)
- [Get](#sentinelonboardingstates_get)
- [List](#sentinelonboardingstates_list)

## ThreatIntelligenceIndicator

- [AppendTags](#threatintelligenceindicator_appendtags)
- [Create](#threatintelligenceindicator_create)
- [CreateIndicator](#threatintelligenceindicator_createindicator)
- [Delete](#threatintelligenceindicator_delete)
- [Get](#threatintelligenceindicator_get)
- [QueryIndicators](#threatintelligenceindicator_queryindicators)
- [ReplaceTags](#threatintelligenceindicator_replacetags)

## ThreatIntelligenceIndicatorMetrics

- [List](#threatintelligenceindicatormetrics_list)

## ThreatIntelligenceIndicatorsOperation

- [List](#threatintelligenceindicatorsoperation_list)

## WatchlistItems

- [CreateOrUpdate](#watchlistitems_createorupdate)
- [Delete](#watchlistitems_delete)
- [Get](#watchlistitems_get)
- [List](#watchlistitems_list)

## Watchlists

- [CreateOrUpdate](#watchlists_createorupdate)
- [Delete](#watchlists_delete)
- [Get](#watchlists_get)
- [List](#watchlists_list)
### Actions_CreateOrUpdate

```java
/**
 * Samples for Actions CreateOrUpdate.
 */
public final class ActionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/actions/
     * CreateActionOfAlertRule.json
     */
    /**
     * Sample code: Creates or updates an action of alert rule.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAnActionOfAlertRule(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.actions()
            .define("912bec42-cb66-4c03-ac63-1761b6898c3e")
            .withExistingAlertRule("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5")
            .withEtag("\"0300bf09-0000-0000-0000-5c37296e0000\"")
            .withTriggerUri(
                "https://prod-31.northcentralus.logic.azure.com:443/workflows/cd3765391efd48549fd7681ded1d48d7/triggers/manual/paths/invoke?api-version=2016-10-01&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=signature")
            .withLogicAppResourceId(
                "/subscriptions/d0cfe6b2-9ac0-4464-9919-dccaee2e48c0/resourceGroups/myRg/providers/Microsoft.Logic/workflows/MyAlerts")
            .create();
    }
}
```

### Actions_Delete

```java
/**
 * Samples for Actions Delete.
 */
public final class ActionsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/actions/
     * DeleteActionOfAlertRule.json
     */
    /**
     * Sample code: Delete an action of alert rule.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        deleteAnActionOfAlertRule(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.actions()
            .deleteWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                "912bec42-cb66-4c03-ac63-1761b6898c3e", com.azure.core.util.Context.NONE);
    }
}
```

### Actions_Get

```java
/**
 * Samples for Actions Get.
 */
public final class ActionsGetSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/actions/
     * GetActionOfAlertRuleById.json
     */
    /**
     * Sample code: Get an action of alert rule.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getAnActionOfAlertRule(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.actions()
            .getWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                "912bec42-cb66-4c03-ac63-1761b6898c3e", com.azure.core.util.Context.NONE);
    }
}
```

### Actions_ListByAlertRule

```java
/**
 * Samples for Actions ListByAlertRule.
 */
public final class ActionsListByAlertRuleSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/actions/
     * GetAllActionsByAlertRule.json
     */
    /**
     * Sample code: Get all actions of alert rule.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getAllActionsOfAlertRule(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.actions()
            .listByAlertRule("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                com.azure.core.util.Context.NONE);
    }
}
```

### AlertRuleTemplates_Get

```java
/**
 * Samples for AlertRuleTemplates Get.
 */
public final class AlertRuleTemplatesGetSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * alertRuleTemplates/GetAlertRuleTemplateById.json
     */
    /**
     * Sample code: Get alert rule template by Id.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getAlertRuleTemplateById(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.alertRuleTemplates()
            .getWithResponse("myRg", "myWorkspace", "65360bb0-8986-4ade-a89d-af3cf44d28aa",
                com.azure.core.util.Context.NONE);
    }
}
```

### AlertRuleTemplates_List

```java
/**
 * Samples for AlertRuleTemplates List.
 */
public final class AlertRuleTemplatesListSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * alertRuleTemplates/GetAlertRuleTemplates.json
     */
    /**
     * Sample code: Get all alert rule templates.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getAllAlertRuleTemplates(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.alertRuleTemplates().list("myRg", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### AlertRules_CreateOrUpdate

```java
import com.azure.resourcemanager.securityinsights.models.AlertDetail;
import com.azure.resourcemanager.securityinsights.models.AlertDetailsOverride;
import com.azure.resourcemanager.securityinsights.models.AlertSeverity;
import com.azure.resourcemanager.securityinsights.models.AttackTactic;
import com.azure.resourcemanager.securityinsights.models.EntityMapping;
import com.azure.resourcemanager.securityinsights.models.EntityMappingType;
import com.azure.resourcemanager.securityinsights.models.EventGroupingAggregationKind;
import com.azure.resourcemanager.securityinsights.models.EventGroupingSettings;
import com.azure.resourcemanager.securityinsights.models.FieldMapping;
import com.azure.resourcemanager.securityinsights.models.FusionAlertRule;
import com.azure.resourcemanager.securityinsights.models.GroupingConfiguration;
import com.azure.resourcemanager.securityinsights.models.IncidentConfiguration;
import com.azure.resourcemanager.securityinsights.models.MatchingMethod;
import com.azure.resourcemanager.securityinsights.models.MicrosoftSecurityIncidentCreationAlertRule;
import com.azure.resourcemanager.securityinsights.models.MicrosoftSecurityProductName;
import com.azure.resourcemanager.securityinsights.models.ScheduledAlertRule;
import com.azure.resourcemanager.securityinsights.models.TriggerOperator;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AlertRules CreateOrUpdate.
 */
public final class AlertRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/alertRules/
     * CreateFusionAlertRule.json
     */
    /**
     * Sample code: Creates or updates a Fusion alert rule.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        createsOrUpdatesAFusionAlertRule(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.alertRules()
            .createOrUpdateWithResponse("myRg", "myWorkspace", "myFirstFusionRule",
                new FusionAlertRule().withEtag("3d00c3ca-0000-0100-0000-5d42d5010000")
                    .withAlertRuleTemplateName("f71aba3d-28fb-450b-b192-4e76a83015c8")
                    .withEnabled(true),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/alertRules/
     * CreateMicrosoftSecurityIncidentCreationAlertRule.json
     */
    /**
     * Sample code: Creates or updates a MicrosoftSecurityIncidentCreation rule.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAMicrosoftSecurityIncidentCreationRule(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.alertRules()
            .createOrUpdateWithResponse("myRg", "myWorkspace", "microsoftSecurityIncidentCreationRuleExample",
                new MicrosoftSecurityIncidentCreationAlertRule().withEtag("\"260097e0-0000-0d00-0000-5d6fa88f0000\"")
                    .withDisplayName("testing displayname")
                    .withEnabled(true)
                    .withProductFilter(MicrosoftSecurityProductName.MICROSOFT_CLOUD_APP_SECURITY),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/alertRules/
     * CreateScheduledAlertRule.json
     */
    /**
     * Sample code: Creates or updates a Scheduled alert rule.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAScheduledAlertRule(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.alertRules()
            .createOrUpdateWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                new ScheduledAlertRule().withEtag("\"0300bf09-0000-0000-0000-5c37296e0000\"")
                    .withDescription("An example for a scheduled rule")
                    .withDisplayName("My scheduled rule")
                    .withEnabled(true)
                    .withSuppressionDuration(Duration.parse("PT1H"))
                    .withSuppressionEnabled(false)
                    .withTactics(Arrays.asList(AttackTactic.PERSISTENCE, AttackTactic.LATERAL_MOVEMENT))
                    .withIncidentConfiguration(new IncidentConfiguration().withCreateIncident(true)
                        .withGroupingConfiguration(new GroupingConfiguration().withEnabled(true)
                            .withReopenClosedIncident(false)
                            .withLookbackDuration(Duration.parse("PT5H"))
                            .withMatchingMethod(MatchingMethod.SELECTED)
                            .withGroupByEntities(Arrays.asList(EntityMappingType.HOST))
                            .withGroupByAlertDetails(Arrays.asList(AlertDetail.DISPLAY_NAME))
                            .withGroupByCustomDetails(Arrays.asList("OperatingSystemType", "OperatingSystemName"))))
                    .withQuery("Heartbeat")
                    .withQueryFrequency(Duration.parse("PT1H"))
                    .withQueryPeriod(Duration.parse("P2DT1H30M"))
                    .withSeverity(AlertSeverity.HIGH)
                    .withTriggerOperator(TriggerOperator.GREATER_THAN)
                    .withTriggerThreshold(0)
                    .withEventGroupingSettings(
                        new EventGroupingSettings().withAggregationKind(EventGroupingAggregationKind.ALERT_PER_RESULT))
                    .withCustomDetails(mapOf("OperatingSystemName", "OSName", "OperatingSystemType", "OSType"))
                    .withEntityMappings(Arrays.asList(
                        new EntityMapping().withEntityType(EntityMappingType.HOST)
                            .withFieldMappings(Arrays
                                .asList(new FieldMapping().withIdentifier("FullName").withColumnName("Computer"))),
                        new EntityMapping().withEntityType(EntityMappingType.IP)
                            .withFieldMappings(Arrays
                                .asList(new FieldMapping().withIdentifier("Address").withColumnName("ComputerIP")))))
                    .withAlertDetailsOverride(
                        new AlertDetailsOverride().withAlertDisplayNameFormat("Alert from {{Computer}}")
                            .withAlertDescriptionFormat("Suspicious activity was made by {{ComputerIP}}")),
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

### AlertRules_Delete

```java
/**
 * Samples for AlertRules Delete.
 */
public final class AlertRulesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/alertRules/
     * DeleteAlertRule.json
     */
    /**
     * Sample code: Delete an alert rule.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteAnAlertRule(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.alertRules()
            .deleteWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                com.azure.core.util.Context.NONE);
    }
}
```

### AlertRules_Get

```java
/**
 * Samples for AlertRules Get.
 */
public final class AlertRulesGetSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/alertRules/
     * GetScheduledAlertRule.json
     */
    /**
     * Sample code: Get a Scheduled alert rule.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getAScheduledAlertRule(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.alertRules()
            .getWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/alertRules/
     * GetFusionAlertRule.json
     */
    /**
     * Sample code: Get a Fusion alert rule.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAFusionAlertRule(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.alertRules()
            .getWithResponse("myRg", "myWorkspace", "myFirstFusionRule", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/alertRules/
     * GetMicrosoftSecurityIncidentCreationAlertRule.json
     */
    /**
     * Sample code: Get a MicrosoftSecurityIncidentCreation rule.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAMicrosoftSecurityIncidentCreationRule(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.alertRules()
            .getWithResponse("myRg", "myWorkspace", "microsoftSecurityIncidentCreationRuleExample",
                com.azure.core.util.Context.NONE);
    }
}
```

### AlertRules_List

```java
/**
 * Samples for AlertRules List.
 */
public final class AlertRulesListSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/alertRules/
     * GetAllAlertRules.json
     */
    /**
     * Sample code: Get all alert rules.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllAlertRules(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.alertRules().list("myRg", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### AutomationRules_CreateOrUpdate

```java
import com.azure.resourcemanager.securityinsights.models.AutomationRuleAction;
import com.azure.resourcemanager.securityinsights.models.AutomationRuleTriggeringLogic;
import java.util.List;

/**
 * Samples for AutomationRules CreateOrUpdate.
 */
public final class AutomationRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * automationRules/AutomationRules_CreateOrUpdate.json
     */
    /**
     * Sample code: AutomationRules_CreateOrUpdate.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        automationRulesCreateOrUpdate(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.automationRules()
            .define("73e01a99-5cd7-4139-a149-9f2736ff2ab5")
            .withExistingWorkspace("myRg", "myWorkspace")
            .withDisplayName((String) null)
            .withOrder(0)
            .withTriggeringLogic((AutomationRuleTriggeringLogic) null)
            .withActions((List<AutomationRuleAction>) null)
            .create();
    }
}
```

### AutomationRules_Delete

```java
/**
 * Samples for AutomationRules Delete.
 */
public final class AutomationRulesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * automationRules/AutomationRules_Delete.json
     */
    /**
     * Sample code: AutomationRules_Delete.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        automationRulesDelete(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.automationRules()
            .deleteWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                com.azure.core.util.Context.NONE);
    }
}
```

### AutomationRules_Get

```java
/**
 * Samples for AutomationRules Get.
 */
public final class AutomationRulesGetSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * automationRules/AutomationRules_Get.json
     */
    /**
     * Sample code: AutomationRules_Get.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void automationRulesGet(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.automationRules()
            .getWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                com.azure.core.util.Context.NONE);
    }
}
```

### AutomationRules_List

```java
/**
 * Samples for AutomationRules List.
 */
public final class AutomationRulesListSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * automationRules/AutomationRules_List.json
     */
    /**
     * Sample code: AutomationRules_List.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void automationRulesList(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.automationRules().list("myRg", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### Bookmarks_CreateOrUpdate

```java
import com.azure.resourcemanager.securityinsights.models.UserInfo;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;

/**
 * Samples for Bookmarks CreateOrUpdate.
 */
public final class BookmarksCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/bookmarks/
     * CreateBookmark.json
     */
    /**
     * Sample code: Creates or updates a bookmark.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        createsOrUpdatesABookmark(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.bookmarks()
            .define("73e01a99-5cd7-4139-a149-9f2736ff2ab5")
            .withExistingWorkspace("myRg", "myWorkspace")
            .withEtag("\"0300bf09-0000-0000-0000-5c37296e0000\"")
            .withCreated(OffsetDateTime.parse("2019-01-01T13:15:30Z"))
            .withCreatedBy(new UserInfo().withObjectId(UUID.fromString("2046feea-040d-4a46-9e2b-91c2941bfa70")))
            .withDisplayName("My bookmark")
            .withLabels(Arrays.asList("Tag1", "Tag2"))
            .withNotes("Found a suspicious activity")
            .withQuery("SecurityEvent | where TimeGenerated > ago(1d) and TimeGenerated < ago(2d)")
            .withQueryResult("Security Event query result")
            .withUpdated(OffsetDateTime.parse("2019-01-01T13:15:30Z"))
            .withUpdatedBy(new UserInfo().withObjectId(UUID.fromString("2046feea-040d-4a46-9e2b-91c2941bfa70")))
            .create();
    }
}
```

### Bookmarks_Delete

```java
/**
 * Samples for Bookmarks Delete.
 */
public final class BookmarksDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/bookmarks/
     * DeleteBookmark.json
     */
    /**
     * Sample code: Delete a bookmark.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteABookmark(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.bookmarks()
            .deleteWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                com.azure.core.util.Context.NONE);
    }
}
```

### Bookmarks_Get

```java
/**
 * Samples for Bookmarks Get.
 */
public final class BookmarksGetSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/bookmarks/
     * GetBookmarkById.json
     */
    /**
     * Sample code: Get a bookmark.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getABookmark(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.bookmarks()
            .getWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                com.azure.core.util.Context.NONE);
    }
}
```

### Bookmarks_List

```java
/**
 * Samples for Bookmarks List.
 */
public final class BookmarksListSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/bookmarks/
     * GetBookmarks.json
     */
    /**
     * Sample code: Get all bookmarks.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllBookmarks(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.bookmarks().list("myRg", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### DataConnectors_CreateOrUpdate

```java
import com.azure.resourcemanager.securityinsights.models.DataTypeState;
import com.azure.resourcemanager.securityinsights.models.OfficeDataConnector;
import com.azure.resourcemanager.securityinsights.models.OfficeDataConnectorDataTypes;
import com.azure.resourcemanager.securityinsights.models.OfficeDataConnectorDataTypesExchange;
import com.azure.resourcemanager.securityinsights.models.OfficeDataConnectorDataTypesSharePoint;
import com.azure.resourcemanager.securityinsights.models.OfficeDataConnectorDataTypesTeams;
import com.azure.resourcemanager.securityinsights.models.TIDataConnector;
import com.azure.resourcemanager.securityinsights.models.TIDataConnectorDataTypes;
import com.azure.resourcemanager.securityinsights.models.TIDataConnectorDataTypesIndicators;
import java.time.OffsetDateTime;

/**
 * Samples for DataConnectors CreateOrUpdate.
 */
public final class DataConnectorsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * dataConnectors/CreateOfficeDataConnetor.json
     */
    /**
     * Sample code: Creates or updates an Office365 data connector.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAnOffice365DataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.dataConnectors()
            .createOrUpdateWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                new OfficeDataConnector().withEtag("\"0300bf09-0000-0000-0000-5c37296e0000\"")
                    .withTenantId("2070ecc9-b4d5-4ae4-adaa-936fa1954fa8")
                    .withDataTypes(new OfficeDataConnectorDataTypes()
                        .withExchange(new OfficeDataConnectorDataTypesExchange().withState(DataTypeState.ENABLED))
                        .withSharePoint(new OfficeDataConnectorDataTypesSharePoint().withState(DataTypeState.ENABLED))
                        .withTeams(new OfficeDataConnectorDataTypesTeams().withState(DataTypeState.ENABLED))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * dataConnectors/CreateThreatIntelligenceDataConnector.json
     */
    /**
     * Sample code: Creates or updates an Threat Intelligence Platform data connector.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAnThreatIntelligencePlatformDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.dataConnectors()
            .createOrUpdateWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                new TIDataConnector().withTenantId("06b3ccb8-1384-4bcc-aec7-852f6d57161b")
                    .withTipLookbackPeriod(OffsetDateTime.parse("2020-01-01T13:00:30.123Z"))
                    .withDataTypes(new TIDataConnectorDataTypes()
                        .withIndicators(new TIDataConnectorDataTypesIndicators().withState(DataTypeState.ENABLED))),
                com.azure.core.util.Context.NONE);
    }
}
```

### DataConnectors_Delete

```java
/**
 * Samples for DataConnectors Delete.
 */
public final class DataConnectorsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * dataConnectors/DeleteOfficeDataConnetor.json
     */
    /**
     * Sample code: Delete an Office365 data connector.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        deleteAnOffice365DataConnector(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.dataConnectors()
            .deleteWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                com.azure.core.util.Context.NONE);
    }
}
```

### DataConnectors_Get

```java
/**
 * Samples for DataConnectors Get.
 */
public final class DataConnectorsGetSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * dataConnectors/GetAzureSecurityCenterById.json
     */
    /**
     * Sample code: Get a ASC data connector.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getAASCDataConnector(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "763f9fa1-c2d3-4fa2-93e9-bccd4899aa12",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * dataConnectors/GetOfficeDataConnetorById.json
     */
    /**
     * Sample code: Get an Office365 data connector.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getAnOffice365DataConnector(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * dataConnectors/GetMicrosoftCloudAppSecurityById.json
     */
    /**
     * Sample code: Get a MCAS data connector.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getAMCASDataConnector(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "b96d014d-b5c2-4a01-9aba-a8058f629d42",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * dataConnectors/GetAmazonWebServicesCloudTrailById.json
     */
    /**
     * Sample code: Get an AwsCloudTrail data connector.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getAnAwsCloudTrailDataConnector(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "c345bf40-8509-4ed2-b947-50cb773aaf04",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * dataConnectors/GetAzureAdvancedThreatProtectionById.json
     */
    /**
     * Sample code: Get an AATP data connector.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getAnAATPDataConnector(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "07e42cb3-e658-4e90-801c-efa0f29d3d44",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * dataConnectors/GetMicrosoftDefenderAdvancedThreatProtectionById.json
     */
    /**
     * Sample code: Get a MDATP data connector.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getAMDATPDataConnector(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "06b3ccb8-1384-4bcc-aec7-852f6d57161b",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * dataConnectors/GetThreatIntelligenceById.json
     */
    /**
     * Sample code: Get a TI data connector.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getATIDataConnector(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "c345bf40-8509-4ed2-b947-50cb773aaf04",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * dataConnectors/GetAzureActiveDirectoryById.json
     */
    /**
     * Sample code: Get an AAD data connector.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getAnAADDataConnector(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "f0cd27d2-5f03-4c06-ba31-d2dc82dcb51d",
                com.azure.core.util.Context.NONE);
    }
}
```

### DataConnectors_List

```java
/**
 * Samples for DataConnectors List.
 */
public final class DataConnectorsListSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * dataConnectors/GetDataConnectors.json
     */
    /**
     * Sample code: Get all data connectors.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getAllDataConnectors(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.dataConnectors().list("myRg", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### IncidentComments_CreateOrUpdate

```java
/**
 * Samples for IncidentComments CreateOrUpdate.
 */
public final class IncidentCommentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/incidents/
     * comments/CreateIncidentComment.json
     */
    /**
     * Sample code: Creates or updates an incident comment.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        createsOrUpdatesAnIncidentComment(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.incidentComments()
            .define("4bb36b7b-26ff-4d1c-9cbe-0d8ab3da0014")
            .withExistingIncident("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5")
            .withMessage("Some message")
            .create();
    }
}
```

### IncidentComments_Delete

```java
/**
 * Samples for IncidentComments Delete.
 */
public final class IncidentCommentsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/incidents/
     * comments/DeleteIncidentComment.json
     */
    /**
     * Sample code: Delete the incident comment.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        deleteTheIncidentComment(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.incidentComments()
            .deleteWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                "4bb36b7b-26ff-4d1c-9cbe-0d8ab3da0014", com.azure.core.util.Context.NONE);
    }
}
```

### IncidentComments_Get

```java
/**
 * Samples for IncidentComments Get.
 */
public final class IncidentCommentsGetSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/incidents/
     * comments/GetIncidentCommentById.json
     */
    /**
     * Sample code: Get an incident comment.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getAnIncidentComment(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.incidentComments()
            .getWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                "4bb36b7b-26ff-4d1c-9cbe-0d8ab3da0014", com.azure.core.util.Context.NONE);
    }
}
```

### IncidentComments_List

```java
/**
 * Samples for IncidentComments List.
 */
public final class IncidentCommentsListSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/incidents/
     * comments/GetAllIncidentComments.json
     */
    /**
     * Sample code: Get all incident comments.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getAllIncidentComments(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.incidentComments()
            .list("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5", null, null, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### IncidentRelations_CreateOrUpdate

```java
/**
 * Samples for IncidentRelations CreateOrUpdate.
 */
public final class IncidentRelationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/incidents/
     * relations/CreateIncidentRelation.json
     */
    /**
     * Sample code: Creates or updates an incident relation.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        createsOrUpdatesAnIncidentRelation(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.incidentRelations()
            .define("4bb36b7b-26ff-4d1c-9cbe-0d8ab3da0014")
            .withExistingIncident("myRg", "myWorkspace", "afbd324f-6c48-459c-8710-8d1e1cd03812")
            .withRelatedResourceId(
                "/subscriptions/d0cfe6b2-9ac0-4464-9919-dccaee2e48c0/resourceGroups/myRg/providers/Microsoft.OperationalInsights/workspaces/myWorkspace/providers/Microsoft.SecurityInsights/bookmarks/2216d0e1-91e3-4902-89fd-d2df8c535096")
            .create();
    }
}
```

### IncidentRelations_Delete

```java
/**
 * Samples for IncidentRelations Delete.
 */
public final class IncidentRelationsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/incidents/
     * relations/DeleteIncidentRelation.json
     */
    /**
     * Sample code: Delete the incident relation.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        deleteTheIncidentRelation(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.incidentRelations()
            .deleteWithResponse("myRg", "myWorkspace", "afbd324f-6c48-459c-8710-8d1e1cd03812",
                "4bb36b7b-26ff-4d1c-9cbe-0d8ab3da0014", com.azure.core.util.Context.NONE);
    }
}
```

### IncidentRelations_Get

```java
/**
 * Samples for IncidentRelations Get.
 */
public final class IncidentRelationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/incidents/
     * relations/GetIncidentRelationByName.json
     */
    /**
     * Sample code: Get an incident relation.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getAnIncidentRelation(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.incidentRelations()
            .getWithResponse("myRg", "myWorkspace", "afbd324f-6c48-459c-8710-8d1e1cd03812",
                "4bb36b7b-26ff-4d1c-9cbe-0d8ab3da0014", com.azure.core.util.Context.NONE);
    }
}
```

### IncidentRelations_List

```java
/**
 * Samples for IncidentRelations List.
 */
public final class IncidentRelationsListSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/incidents/
     * relations/GetAllIncidentRelations.json
     */
    /**
     * Sample code: Get all incident relations.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getAllIncidentRelations(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.incidentRelations()
            .list("myRg", "myWorkspace", "afbd324f-6c48-459c-8710-8d1e1cd03812", null, null, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Incidents_CreateOrUpdate

```java
import com.azure.resourcemanager.securityinsights.models.IncidentClassification;
import com.azure.resourcemanager.securityinsights.models.IncidentClassificationReason;
import com.azure.resourcemanager.securityinsights.models.IncidentOwnerInfo;
import com.azure.resourcemanager.securityinsights.models.IncidentSeverity;
import com.azure.resourcemanager.securityinsights.models.IncidentStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Samples for Incidents CreateOrUpdate.
 */
public final class IncidentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/incidents/
     * CreateIncident.json
     */
    /**
     * Sample code: Creates or updates an incident.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        createsOrUpdatesAnIncident(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.incidents()
            .define("73e01a99-5cd7-4139-a149-9f2736ff2ab5")
            .withExistingWorkspace("myRg", "myWorkspace")
            .withEtag("\"0300bf09-0000-0000-0000-5c37296e0000\"")
            .withClassification(IncidentClassification.FALSE_POSITIVE)
            .withClassificationComment("Not a malicious activity")
            .withClassificationReason(IncidentClassificationReason.INCORRECT_ALERT_LOGIC)
            .withDescription("This is a demo incident")
            .withFirstActivityTimeUtc(OffsetDateTime.parse("2019-01-01T13:00:30Z"))
            .withLastActivityTimeUtc(OffsetDateTime.parse("2019-01-01T13:05:30Z"))
            .withOwner(new IncidentOwnerInfo().withObjectId(UUID.fromString("2046feea-040d-4a46-9e2b-91c2941bfa70")))
            .withSeverity(IncidentSeverity.HIGH)
            .withStatus(IncidentStatus.CLOSED)
            .withTitle("My incident")
            .create();
    }
}
```

### Incidents_Delete

```java
/**
 * Samples for Incidents Delete.
 */
public final class IncidentsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/incidents/
     * DeleteIncident.json
     */
    /**
     * Sample code: Delete an incident.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteAnIncident(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.incidents()
            .deleteWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                com.azure.core.util.Context.NONE);
    }
}
```

### Incidents_Get

```java
/**
 * Samples for Incidents Get.
 */
public final class IncidentsGetSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/incidents/
     * GetIncidentById.json
     */
    /**
     * Sample code: Get an incident.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAnIncident(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.incidents()
            .getWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                com.azure.core.util.Context.NONE);
    }
}
```

### Incidents_List

```java
/**
 * Samples for Incidents List.
 */
public final class IncidentsListSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/incidents/
     * GetIncidents.json
     */
    /**
     * Sample code: Get all incidents.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllIncidents(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.incidents()
            .list("myRg", "myWorkspace", null, "properties/createdTimeUtc desc", 1, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Incidents_ListAlerts

```java
/**
 * Samples for Incidents ListAlerts.
 */
public final class IncidentsListAlertsSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/incidents/
     * GetAllIncidentAlerts.json
     */
    /**
     * Sample code: Get all incident alerts.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getAllIncidentAlerts(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.incidents()
            .listAlertsWithResponse("myRg", "myWorkspace", "afbd324f-6c48-459c-8710-8d1e1cd03812",
                com.azure.core.util.Context.NONE);
    }
}
```

### Incidents_ListBookmarks

```java
/**
 * Samples for Incidents ListBookmarks.
 */
public final class IncidentsListBookmarksSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/incidents/
     * GetAllIncidentBookmarks.json
     */
    /**
     * Sample code: Get all incident bookmarks.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getAllIncidentBookmarks(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.incidents()
            .listBookmarksWithResponse("myRg", "myWorkspace", "afbd324f-6c48-459c-8710-8d1e1cd03812",
                com.azure.core.util.Context.NONE);
    }
}
```

### Incidents_ListEntities

```java
/**
 * Samples for Incidents ListEntities.
 */
public final class IncidentsListEntitiesSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/incidents/
     * GetAllIncidentEntities.json
     */
    /**
     * Sample code: Gets all incident related entities.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getsAllIncidentRelatedEntities(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.incidents()
            .listEntitiesWithResponse("myRg", "myWorkspace", "afbd324f-6c48-459c-8710-8d1e1cd03812",
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/operations/
     * ListOperations.json
     */
    /**
     * Sample code: Get all operations.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllOperations(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### SecurityMLAnalyticsSettings_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.securityinsights.models.AnomalySecurityMLAnalyticsSettings;
import com.azure.resourcemanager.securityinsights.models.AttackTactic;
import com.azure.resourcemanager.securityinsights.models.SecurityMLAnalyticsSettingsDataSource;
import com.azure.resourcemanager.securityinsights.models.SettingsStatus;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

/**
 * Samples for SecurityMLAnalyticsSettings CreateOrUpdate.
 */
public final class SecurityMLAnalyticsSettingsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * securityMLAnalyticsSettings/CreateAnomalySecurityMLAnalyticsSetting.json
     */
    /**
     * Sample code: Creates or updates a Anomaly Security ML Analytics Settings.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAAnomalySecurityMLAnalyticsSettings(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) throws IOException {
        manager.securityMLAnalyticsSettings()
            .createOrUpdateWithResponse("myRg", "myWorkspace", "f209187f-1d17-4431-94af-c141bf5f23db",
                new AnomalySecurityMLAnalyticsSettings().withEtag("\"260090e2-0000-0d00-0000-5d6fb8670000\"")
                    .withDescription(
                        "When account logs from a source region that has rarely been logged in from during the last 14 days, an anomaly is triggered.")
                    .withDisplayName("Login from unusual region")
                    .withEnabled(true)
                    .withRequiredDataConnectors(
                        Arrays.asList(new SecurityMLAnalyticsSettingsDataSource().withConnectorId("AWS")
                            .withDataTypes(Arrays.asList("AWSCloudTrail"))))
                    .withTactics(Arrays.asList(AttackTactic.EXFILTRATION, AttackTactic.COMMAND_AND_CONTROL))
                    .withTechniques(Arrays.asList("T1037", "T1021"))
                    .withAnomalyVersion("1.0.5")
                    .withCustomizableObservations(SerializerFactory.createDefaultManagementSerializerAdapter()
                        .deserialize(
                            "{\"multiSelectObservations\":null,\"prioritizeExcludeObservations\":null,\"singleSelectObservations\":[{\"name\":\"Device vendor\",\"description\":\"Select device vendor of network connection logs from CommonSecurityLog\",\"rerun\":\"RerunAlways\",\"sequenceNumber\":1,\"supportedValues\":[\"Palo Alto Networks\",\"Fortinet\",\"Check Point\"],\"supportedValuesKql\":null,\"value\":[\"Palo Alto Networks\"],\"valuesKql\":null}],\"singleValueObservations\":null,\"thresholdObservations\":[{\"name\":\"Daily data transfer threshold in MB\",\"description\":\"Suppress anomalies when daily data transfered (in MB) per hour is less than the chosen value\",\"maximum\":\"100\",\"minimum\":\"1\",\"rerun\":\"RerunAlways\",\"sequenceNumber\":1,\"value\":\"25\"},{\"name\":\"Number of standard deviations\",\"description\":\"Triggers anomalies when number of standard deviations is greater than the chosen value\",\"maximum\":\"10\",\"minimum\":\"2\",\"rerun\":\"RerunAlways\",\"sequenceNumber\":2,\"value\":\"3\"}]}",
                            Object.class, SerializerEncoding.JSON))
                    .withFrequency(Duration.parse("PT1H"))
                    .withSettingsStatus(SettingsStatus.PRODUCTION)
                    .withIsDefaultSettings(true)
                    .withAnomalySettingsVersion(0)
                    .withSettingsDefinitionId(UUID.fromString("f209187f-1d17-4431-94af-c141bf5f23db")),
                com.azure.core.util.Context.NONE);
    }
}
```

### SecurityMLAnalyticsSettings_Delete

```java
/**
 * Samples for SecurityMLAnalyticsSettings Delete.
 */
public final class SecurityMLAnalyticsSettingsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * securityMLAnalyticsSettings/DeleteSecurityMLAnalyticsSetting.json
     */
    /**
     * Sample code: Delete a Security ML Analytics Settings.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        deleteASecurityMLAnalyticsSettings(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.securityMLAnalyticsSettings()
            .deleteWithResponse("myRg", "myWorkspace", "f209187f-1d17-4431-94af-c141bf5f23db",
                com.azure.core.util.Context.NONE);
    }
}
```

### SecurityMLAnalyticsSettings_Get

```java
/**
 * Samples for SecurityMLAnalyticsSettings Get.
 */
public final class SecurityMLAnalyticsSettingsGetSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * securityMLAnalyticsSettings/GetAnomalySecurityMLAnalyticsSetting.json
     */
    /**
     * Sample code: Get a Anomaly Security ML Analytics Settings.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAAnomalySecurityMLAnalyticsSettings(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.securityMLAnalyticsSettings()
            .getWithResponse("myRg", "myWorkspace", "myFirstAnomalySettings", com.azure.core.util.Context.NONE);
    }
}
```

### SecurityMLAnalyticsSettings_List

```java
/**
 * Samples for SecurityMLAnalyticsSettings List.
 */
public final class SecurityMLAnalyticsSettingsListSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * securityMLAnalyticsSettings/GetAllSecurityMLAnalyticsSettings.json
     */
    /**
     * Sample code: Get all Security ML Analytics Settings.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getAllSecurityMLAnalyticsSettings(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.securityMLAnalyticsSettings().list("myRg", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### SentinelOnboardingStates_Create

```java
/**
 * Samples for SentinelOnboardingStates Create.
 */
public final class SentinelOnboardingStatesCreateSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * onboardingStates/CreateSentinelOnboardingState.json
     */
    /**
     * Sample code: Create Sentinel onboarding state.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        createSentinelOnboardingState(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.sentinelOnboardingStates()
            .define("default")
            .withExistingWorkspace("myRg", "myWorkspace")
            .withCustomerManagedKey(false)
            .create();
    }
}
```

### SentinelOnboardingStates_Delete

```java
/**
 * Samples for SentinelOnboardingStates Delete.
 */
public final class SentinelOnboardingStatesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * onboardingStates/DeleteSentinelOnboardingState.json
     */
    /**
     * Sample code: Delete Sentinel onboarding state.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        deleteSentinelOnboardingState(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.sentinelOnboardingStates()
            .deleteWithResponse("myRg", "myWorkspace", "default", com.azure.core.util.Context.NONE);
    }
}
```

### SentinelOnboardingStates_Get

```java
/**
 * Samples for SentinelOnboardingStates Get.
 */
public final class SentinelOnboardingStatesGetSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * onboardingStates/GetSentinelOnboardingState.json
     */
    /**
     * Sample code: Get Sentinel onboarding state.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getSentinelOnboardingState(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.sentinelOnboardingStates()
            .getWithResponse("myRg", "myWorkspace", "default", com.azure.core.util.Context.NONE);
    }
}
```

### SentinelOnboardingStates_List

```java
/**
 * Samples for SentinelOnboardingStates List.
 */
public final class SentinelOnboardingStatesListSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * onboardingStates/GetAllSentinelOnboardingStates.json
     */
    /**
     * Sample code: Get all Sentinel onboarding states.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getAllSentinelOnboardingStates(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.sentinelOnboardingStates().listWithResponse("myRg", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### ThreatIntelligenceIndicator_AppendTags

```java
import com.azure.resourcemanager.securityinsights.models.ThreatIntelligenceAppendTags;
import java.util.Arrays;

/**
 * Samples for ThreatIntelligenceIndicator AppendTags.
 */
public final class ThreatIntelligenceIndicatorAppendTagsSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * threatintelligence/AppendTagsThreatIntelligence.json
     */
    /**
     * Sample code: Append tags to a threat intelligence indicator.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void appendTagsToAThreatIntelligenceIndicator(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.threatIntelligenceIndicators()
            .appendTagsWithResponse("myRg", "myWorkspace", "d9cd6f0b-96b9-3984-17cd-a779d1e15a93",
                new ThreatIntelligenceAppendTags().withThreatIntelligenceTags(Arrays.asList("tag1", "tag2")),
                com.azure.core.util.Context.NONE);
    }
}
```

### ThreatIntelligenceIndicator_Create

```java
import com.azure.resourcemanager.securityinsights.models.ThreatIntelligenceIndicatorModel;
import java.util.Arrays;

/**
 * Samples for ThreatIntelligenceIndicator Create.
 */
public final class ThreatIntelligenceIndicatorCreateSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * threatintelligence/UpdateThreatIntelligence.json
     */
    /**
     * Sample code: Update a threat Intelligence indicator.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        updateAThreatIntelligenceIndicator(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.threatIntelligenceIndicators()
            .createWithResponse("myRg", "myWorkspace", "d9cd6f0b-96b9-3984-17cd-a779d1e15a93",
                new ThreatIntelligenceIndicatorModel().withThreatIntelligenceTags(Arrays.asList("new schema"))
                    .withSource("Azure Sentinel")
                    .withDisplayName("new schema")
                    .withDescription("debugging indicators")
                    .withPattern("[url:value = 'https://www.contoso.com']")
                    .withPatternType("url")
                    .withKillChainPhases(Arrays.asList())
                    .withCreatedByRef("contoso@contoso.com")
                    .withExternalReferences(Arrays.asList())
                    .withGranularMarkings(Arrays.asList())
                    .withLabels(Arrays.asList())
                    .withRevoked(false)
                    .withConfidence(78)
                    .withThreatTypes(Arrays.asList("compromised"))
                    .withValidFrom("2020-04-15T17:44:00.114052Z")
                    .withValidUntil("")
                    .withModified(""),
                com.azure.core.util.Context.NONE);
    }
}
```

### ThreatIntelligenceIndicator_CreateIndicator

```java
import com.azure.resourcemanager.securityinsights.models.ThreatIntelligenceIndicatorModel;
import java.util.Arrays;

/**
 * Samples for ThreatIntelligenceIndicator CreateIndicator.
 */
public final class ThreatIntelligenceIndicatorCreateIndicatorSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * threatintelligence/CreateThreatIntelligence.json
     */
    /**
     * Sample code: Create a new Threat Intelligence.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        createANewThreatIntelligence(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.threatIntelligenceIndicators()
            .createIndicatorWithResponse("myRg", "myWorkspace",
                new ThreatIntelligenceIndicatorModel().withThreatIntelligenceTags(Arrays.asList("new schema"))
                    .withSource("Azure Sentinel")
                    .withDisplayName("new schema")
                    .withDescription("debugging indicators")
                    .withPattern("[url:value = 'https://www.contoso.com']")
                    .withPatternType("url")
                    .withKillChainPhases(Arrays.asList())
                    .withCreatedByRef("contoso@contoso.com")
                    .withExternalReferences(Arrays.asList())
                    .withGranularMarkings(Arrays.asList())
                    .withLabels(Arrays.asList())
                    .withRevoked(false)
                    .withConfidence(78)
                    .withThreatTypes(Arrays.asList("compromised"))
                    .withValidFrom("2020-04-15T17:44:00.114052Z")
                    .withValidUntil("")
                    .withModified(""),
                com.azure.core.util.Context.NONE);
    }
}
```

### ThreatIntelligenceIndicator_Delete

```java
/**
 * Samples for ThreatIntelligenceIndicator Delete.
 */
public final class ThreatIntelligenceIndicatorDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * threatintelligence/DeleteThreatIntelligence.json
     */
    /**
     * Sample code: Delete a threat intelligence indicator.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        deleteAThreatIntelligenceIndicator(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.threatIntelligenceIndicators()
            .deleteWithResponse("myRg", "myWorkspace", "d9cd6f0b-96b9-3984-17cd-a779d1e15a93",
                com.azure.core.util.Context.NONE);
    }
}
```

### ThreatIntelligenceIndicator_Get

```java
/**
 * Samples for ThreatIntelligenceIndicator Get.
 */
public final class ThreatIntelligenceIndicatorGetSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * threatintelligence/GetThreatIntelligenceById.json
     */
    /**
     * Sample code: View a threat intelligence indicator by name.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void viewAThreatIntelligenceIndicatorByName(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.threatIntelligenceIndicators()
            .getWithResponse("myRg", "myWorkspace", "e16ef847-962e-d7b6-9c8b-a33e4bd30e47",
                com.azure.core.util.Context.NONE);
    }
}
```

### ThreatIntelligenceIndicator_QueryIndicators

```java
import com.azure.resourcemanager.securityinsights.models.ThreatIntelligenceFilteringCriteria;
import com.azure.resourcemanager.securityinsights.models.ThreatIntelligenceSortingCriteria;
import com.azure.resourcemanager.securityinsights.models.ThreatIntelligenceSortingOrder;
import java.util.Arrays;

/**
 * Samples for ThreatIntelligenceIndicator QueryIndicators.
 */
public final class ThreatIntelligenceIndicatorQueryIndicatorsSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * threatintelligence/QueryThreatIntelligence.json
     */
    /**
     * Sample code: Query threat intelligence indicators as per filtering criteria.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void queryThreatIntelligenceIndicatorsAsPerFilteringCriteria(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.threatIntelligenceIndicators()
            .queryIndicators("myRg", "myWorkspace", new ThreatIntelligenceFilteringCriteria().withPageSize(100)
                .withMinConfidence(25)
                .withMaxConfidence(80)
                .withMinValidUntil("2020-04-05T17:44:00.114052Z")
                .withMaxValidUntil("2020-04-25T17:44:00.114052Z")
                .withSortBy(Arrays.asList(new ThreatIntelligenceSortingCriteria().withItemKey("fakeTokenPlaceholder")
                    .withSortOrder(ThreatIntelligenceSortingOrder.DESCENDING)))
                .withSources(Arrays.asList("Azure Sentinel")), com.azure.core.util.Context.NONE);
    }
}
```

### ThreatIntelligenceIndicator_ReplaceTags

```java
import com.azure.resourcemanager.securityinsights.models.ThreatIntelligenceIndicatorModel;
import java.util.Arrays;

/**
 * Samples for ThreatIntelligenceIndicator ReplaceTags.
 */
public final class ThreatIntelligenceIndicatorReplaceTagsSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * threatintelligence/ReplaceTagsThreatIntelligence.json
     */
    /**
     * Sample code: Replace tags to a Threat Intelligence.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        replaceTagsToAThreatIntelligence(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.threatIntelligenceIndicators()
            .replaceTagsWithResponse("myRg", "myWorkspace", "d9cd6f0b-96b9-3984-17cd-a779d1e15a93",
                new ThreatIntelligenceIndicatorModel().withEtag("\"0000262c-0000-0800-0000-5e9767060000\"")
                    .withThreatIntelligenceTags(Arrays.asList("patching tags")),
                com.azure.core.util.Context.NONE);
    }
}
```

### ThreatIntelligenceIndicatorMetrics_List

```java
/**
 * Samples for ThreatIntelligenceIndicatorMetrics List.
 */
public final class ThreatIntelligenceIndicatorMetricsListSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * threatintelligence/CollectThreatIntelligenceMetrics.json
     */
    /**
     * Sample code: Get threat intelligence indicators metrics.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getThreatIntelligenceIndicatorsMetrics(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.threatIntelligenceIndicatorMetrics()
            .listWithResponse("myRg", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### ThreatIntelligenceIndicatorsOperation_List

```java
/**
 * Samples for ThreatIntelligenceIndicatorsOperation List.
 */
public final class ThreatIntelligenceIndicatorsOperationListSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/
     * threatintelligence/GetThreatIntelligence.json
     */
    /**
     * Sample code: Get all threat intelligence indicators.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getAllThreatIntelligenceIndicators(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.threatIntelligenceIndicatorsOperations()
            .list("myRg", "myWorkspace", null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### WatchlistItems_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import java.io.IOException;

/**
 * Samples for WatchlistItems CreateOrUpdate.
 */
public final class WatchlistItemsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/watchlists/
     * CreateWatchlistItem.json
     */
    /**
     * Sample code: Create or update a watchlist item.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createOrUpdateAWatchlistItem(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) throws IOException {
        manager.watchlistItems()
            .define("82ba292c-dc97-4dfc-969d-d4dd9e666842")
            .withExistingWatchlist("myRg", "myWorkspace", "highValueAsset")
            .withEtag("0300bf09-0000-0000-0000-5c37296e0000")
            .withItemsKeyValue(SerializerFactory.createDefaultManagementSerializerAdapter()
                .deserialize(
                    "{\"Business tier\":\"10.0.2.0/24\",\"Data tier\":\"10.0.2.0/24\",\"Gateway subnet\":\"10.0.255.224/27\",\"Private DMZ in\":\"10.0.0.0/27\",\"Public DMZ out\":\"10.0.0.96/27\",\"Web Tier\":\"10.0.1.0/24\"}",
                    Object.class, SerializerEncoding.JSON))
            .create();
    }
}
```

### WatchlistItems_Delete

```java
/**
 * Samples for WatchlistItems Delete.
 */
public final class WatchlistItemsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/watchlists/
     * DeleteWatchlistItem.json
     */
    /**
     * Sample code: Delete a watchlist item.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        deleteAWatchlistItem(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.watchlistItems()
            .deleteWithResponse("myRg", "myWorkspace", "highValueAsset", "4008512e-1d30-48b2-9ee2-d3612ed9d3ea",
                com.azure.core.util.Context.NONE);
    }
}
```

### WatchlistItems_Get

```java
/**
 * Samples for WatchlistItems Get.
 */
public final class WatchlistItemsGetSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/watchlists/
     * GetWatchlistItemById.json
     */
    /**
     * Sample code: Get a watchlist item.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAWatchlistItem(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.watchlistItems()
            .getWithResponse("myRg", "myWorkspace", "highValueAsset", "3f8901fe-63d9-4875-9ad5-9fb3b8105797",
                com.azure.core.util.Context.NONE);
    }
}
```

### WatchlistItems_List

```java
/**
 * Samples for WatchlistItems List.
 */
public final class WatchlistItemsListSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/watchlists/
     * GetWatchlistItems.json
     */
    /**
     * Sample code: Get all watchlist Items.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        getAllWatchlistItems(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.watchlistItems().list("myRg", "myWorkspace", "highValueAsset", null, com.azure.core.util.Context.NONE);
    }
}
```

### Watchlists_CreateOrUpdate

```java
import com.azure.resourcemanager.securityinsights.models.Source;

/**
 * Samples for Watchlists CreateOrUpdate.
 */
public final class WatchlistsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/watchlists/
     * CreateWatchlist.json
     */
    /**
     * Sample code: Create or update a watchlist.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void
        createOrUpdateAWatchlist(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.watchlists()
            .define("highValueAsset")
            .withExistingWorkspace("myRg", "myWorkspace")
            .withEtag("\"0300bf09-0000-0000-0000-5c37296e0000\"")
            .withDisplayName("High Value Assets Watchlist")
            .withProvider("Microsoft")
            .withSource(Source.LOCAL_FILE)
            .withDescription("Watchlist from CSV content")
            .withItemsSearchKey("header1")
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/watchlists/
     * CreateWatchlistAndWatchlistItems.json
     */
    /**
     * Sample code: Create or update a watchlist and bulk creates watchlist items.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createOrUpdateAWatchlistAndBulkCreatesWatchlistItems(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.watchlists()
            .define("highValueAsset")
            .withExistingWorkspace("myRg", "myWorkspace")
            .withEtag("\"0300bf09-0000-0000-0000-5c37296e0000\"")
            .withDisplayName("High Value Assets Watchlist")
            .withProvider("Microsoft")
            .withSource(Source.LOCAL_FILE)
            .withDescription("Watchlist from CSV content")
            .withNumberOfLinesToSkip(1)
            .withRawContent("This line will be skipped\nheader1,header2\nvalue1,value2")
            .withItemsSearchKey("header1")
            .withContentType("text/csv")
            .create();
    }
}
```

### Watchlists_Delete

```java
/**
 * Samples for Watchlists Delete.
 */
public final class WatchlistsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/watchlists/
     * DeleteWatchlist.json
     */
    /**
     * Sample code: Delete a watchlist.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteAWatchlist(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.watchlists()
            .deleteWithResponse("myRg", "myWorkspace", "highValueAsset", com.azure.core.util.Context.NONE);
    }
}
```

### Watchlists_Get

```java
/**
 * Samples for Watchlists Get.
 */
public final class WatchlistsGetSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/watchlists/
     * GetWatchlistByAlias.json
     */
    /**
     * Sample code: Get a watchlist.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAWatchlist(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.watchlists().getWithResponse("myRg", "myWorkspace", "highValueAsset", com.azure.core.util.Context.NONE);
    }
}
```

### Watchlists_List

```java
/**
 * Samples for Watchlists List.
 */
public final class WatchlistsListSamples {
    /*
     * x-ms-original-file:
     * specification/securityinsights/resource-manager/Microsoft.SecurityInsights/stable/2022-11-01/examples/watchlists/
     * GetWatchlists.json
     */
    /**
     * Sample code: Get all watchlists.
     * 
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllWatchlists(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.watchlists().list("myRg", "myWorkspace", null, com.azure.core.util.Context.NONE);
    }
}
```

