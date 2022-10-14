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

## BookmarkOperation

- [Expand](#bookmarkoperation_expand)

## BookmarkRelations

- [CreateOrUpdate](#bookmarkrelations_createorupdate)
- [Delete](#bookmarkrelations_delete)
- [Get](#bookmarkrelations_get)
- [List](#bookmarkrelations_list)

## Bookmarks

- [CreateOrUpdate](#bookmarks_createorupdate)
- [Delete](#bookmarks_delete)
- [Get](#bookmarks_get)
- [List](#bookmarks_list)

## DataConnectors

- [Connect](#dataconnectors_connect)
- [CreateOrUpdate](#dataconnectors_createorupdate)
- [Delete](#dataconnectors_delete)
- [Disconnect](#dataconnectors_disconnect)
- [Get](#dataconnectors_get)
- [List](#dataconnectors_list)

## DataConnectorsCheckRequirementsOperation

- [Post](#dataconnectorscheckrequirementsoperation_post)

## DomainWhois

- [Get](#domainwhois_get)

## Entities

- [Expand](#entities_expand)
- [Get](#entities_get)
- [GetInsights](#entities_getinsights)
- [List](#entities_list)
- [Queries](#entities_queries)

## EntitiesGetTimeline

- [List](#entitiesgettimeline_list)

## EntitiesRelations

- [List](#entitiesrelations_list)

## EntityQueries

- [CreateOrUpdate](#entityqueries_createorupdate)
- [Delete](#entityqueries_delete)
- [Get](#entityqueries_get)
- [List](#entityqueries_list)

## EntityQueryTemplates

- [Get](#entityquerytemplates_get)
- [List](#entityquerytemplates_list)

## EntityRelations

- [GetRelation](#entityrelations_getrelation)

## FileImports

- [Create](#fileimports_create)
- [Delete](#fileimports_delete)
- [Get](#fileimports_get)
- [List](#fileimports_list)

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
- [CreateTeam](#incidents_createteam)
- [Delete](#incidents_delete)
- [Get](#incidents_get)
- [List](#incidents_list)
- [ListAlerts](#incidents_listalerts)
- [ListBookmarks](#incidents_listbookmarks)
- [ListEntities](#incidents_listentities)
- [RunPlaybook](#incidents_runplaybook)

## IpGeodata

- [Get](#ipgeodata_get)

## Metadata

- [Create](#metadata_create)
- [Delete](#metadata_delete)
- [Get](#metadata_get)
- [List](#metadata_list)
- [Update](#metadata_update)

## OfficeConsents

- [Delete](#officeconsents_delete)
- [Get](#officeconsents_get)
- [List](#officeconsents_list)

## Operations

- [List](#operations_list)

## ProductSettings

- [Delete](#productsettings_delete)
- [Get](#productsettings_get)
- [List](#productsettings_list)
- [Update](#productsettings_update)

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

## SourceControl

- [ListRepositories](#sourcecontrol_listrepositories)

## SourceControlsOperation

- [Create](#sourcecontrolsoperation_create)
- [Delete](#sourcecontrolsoperation_delete)
- [Get](#sourcecontrolsoperation_get)
- [List](#sourcecontrolsoperation_list)

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
/** Samples for Actions CreateOrUpdate. */
public final class ActionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/actions/CreateActionOfAlertRule.json
     */
    /**
     * Sample code: Creates or updates an action of alert rule.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAnActionOfAlertRule(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .actions()
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
import com.azure.core.util.Context;

/** Samples for Actions Delete. */
public final class ActionsDeleteSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/actions/DeleteActionOfAlertRule.json
     */
    /**
     * Sample code: Delete an action of alert rule.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteAnActionOfAlertRule(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .actions()
            .deleteWithResponse(
                "myRg",
                "myWorkspace",
                "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                "912bec42-cb66-4c03-ac63-1761b6898c3e",
                Context.NONE);
    }
}
```

### Actions_Get

```java
import com.azure.core.util.Context;

/** Samples for Actions Get. */
public final class ActionsGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/actions/GetActionOfAlertRuleById.json
     */
    /**
     * Sample code: Get an action of alert rule.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAnActionOfAlertRule(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .actions()
            .getWithResponse(
                "myRg",
                "myWorkspace",
                "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                "912bec42-cb66-4c03-ac63-1761b6898c3e",
                Context.NONE);
    }
}
```

### Actions_ListByAlertRule

```java
import com.azure.core.util.Context;

/** Samples for Actions ListByAlertRule. */
public final class ActionsListByAlertRuleSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/actions/GetAllActionsByAlertRule.json
     */
    /**
     * Sample code: Get all actions of alert rule.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllActionsOfAlertRule(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.actions().listByAlertRule("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5", Context.NONE);
    }
}
```

### AlertRuleTemplates_Get

```java
import com.azure.core.util.Context;

/** Samples for AlertRuleTemplates Get. */
public final class AlertRuleTemplatesGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/alertRuleTemplates/GetAlertRuleTemplateById.json
     */
    /**
     * Sample code: Get alert rule template by Id.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAlertRuleTemplateById(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .alertRuleTemplates()
            .getWithResponse("myRg", "myWorkspace", "65360bb0-8986-4ade-a89d-af3cf44d28aa", Context.NONE);
    }
}
```

### AlertRuleTemplates_List

```java
import com.azure.core.util.Context;

/** Samples for AlertRuleTemplates List. */
public final class AlertRuleTemplatesListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/alertRuleTemplates/GetAlertRuleTemplates.json
     */
    /**
     * Sample code: Get all alert rule templates.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllAlertRuleTemplates(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.alertRuleTemplates().list("myRg", "myWorkspace", Context.NONE);
    }
}
```

### AlertRules_CreateOrUpdate

```java
import com.azure.core.util.Context;
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
import com.azure.resourcemanager.securityinsights.models.FusionSourceSettings;
import com.azure.resourcemanager.securityinsights.models.FusionSourceSubTypeSetting;
import com.azure.resourcemanager.securityinsights.models.FusionSubTypeSeverityFilter;
import com.azure.resourcemanager.securityinsights.models.FusionSubTypeSeverityFiltersItem;
import com.azure.resourcemanager.securityinsights.models.GroupingConfiguration;
import com.azure.resourcemanager.securityinsights.models.IncidentConfiguration;
import com.azure.resourcemanager.securityinsights.models.MatchingMethod;
import com.azure.resourcemanager.securityinsights.models.MicrosoftSecurityIncidentCreationAlertRule;
import com.azure.resourcemanager.securityinsights.models.MicrosoftSecurityProductName;
import com.azure.resourcemanager.securityinsights.models.NrtAlertRule;
import com.azure.resourcemanager.securityinsights.models.ScheduledAlertRule;
import com.azure.resourcemanager.securityinsights.models.TriggerOperator;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for AlertRules CreateOrUpdate. */
public final class AlertRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/alertRules/CreateFusionAlertRule.json
     */
    /**
     * Sample code: Creates or updates a Fusion alert rule.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAFusionAlertRule(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .alertRules()
            .createOrUpdateWithResponse(
                "myRg",
                "myWorkspace",
                "myFirstFusionRule",
                new FusionAlertRule()
                    .withEtag("3d00c3ca-0000-0100-0000-5d42d5010000")
                    .withAlertRuleTemplateName("f71aba3d-28fb-450b-b192-4e76a83015c8")
                    .withEnabled(true)
                    .withSourceSettings(
                        Arrays
                            .asList(
                                new FusionSourceSettings().withEnabled(true).withSourceName("Anomalies"),
                                new FusionSourceSettings()
                                    .withEnabled(true)
                                    .withSourceName("Alert providers")
                                    .withSourceSubTypes(
                                        Arrays
                                            .asList(
                                                new FusionSourceSubTypeSetting()
                                                    .withEnabled(true)
                                                    .withSourceSubTypeName("Azure Active Directory Identity Protection")
                                                    .withSeverityFilters(
                                                        new FusionSubTypeSeverityFilter()
                                                            .withFilters(
                                                                Arrays
                                                                    .asList(
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.HIGH)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.MEDIUM)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.LOW)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.INFORMATIONAL)
                                                                            .withEnabled(true)))),
                                                new FusionSourceSubTypeSetting()
                                                    .withEnabled(true)
                                                    .withSourceSubTypeName("Azure Defender")
                                                    .withSeverityFilters(
                                                        new FusionSubTypeSeverityFilter()
                                                            .withFilters(
                                                                Arrays
                                                                    .asList(
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.HIGH)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.MEDIUM)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.LOW)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.INFORMATIONAL)
                                                                            .withEnabled(true)))),
                                                new FusionSourceSubTypeSetting()
                                                    .withEnabled(true)
                                                    .withSourceSubTypeName("Azure Defender for IoT")
                                                    .withSeverityFilters(
                                                        new FusionSubTypeSeverityFilter()
                                                            .withFilters(
                                                                Arrays
                                                                    .asList(
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.HIGH)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.MEDIUM)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.LOW)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.INFORMATIONAL)
                                                                            .withEnabled(true)))),
                                                new FusionSourceSubTypeSetting()
                                                    .withEnabled(true)
                                                    .withSourceSubTypeName("Microsoft 365 Defender")
                                                    .withSeverityFilters(
                                                        new FusionSubTypeSeverityFilter()
                                                            .withFilters(
                                                                Arrays
                                                                    .asList(
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.HIGH)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.MEDIUM)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.LOW)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.INFORMATIONAL)
                                                                            .withEnabled(true)))),
                                                new FusionSourceSubTypeSetting()
                                                    .withEnabled(true)
                                                    .withSourceSubTypeName("Microsoft Cloud App Security")
                                                    .withSeverityFilters(
                                                        new FusionSubTypeSeverityFilter()
                                                            .withFilters(
                                                                Arrays
                                                                    .asList(
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.HIGH)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.MEDIUM)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.LOW)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.INFORMATIONAL)
                                                                            .withEnabled(true)))),
                                                new FusionSourceSubTypeSetting()
                                                    .withEnabled(true)
                                                    .withSourceSubTypeName("Microsoft Defender for Endpoint")
                                                    .withSeverityFilters(
                                                        new FusionSubTypeSeverityFilter()
                                                            .withFilters(
                                                                Arrays
                                                                    .asList(
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.HIGH)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.MEDIUM)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.LOW)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.INFORMATIONAL)
                                                                            .withEnabled(true)))),
                                                new FusionSourceSubTypeSetting()
                                                    .withEnabled(true)
                                                    .withSourceSubTypeName("Microsoft Defender for Identity")
                                                    .withSeverityFilters(
                                                        new FusionSubTypeSeverityFilter()
                                                            .withFilters(
                                                                Arrays
                                                                    .asList(
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.HIGH)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.MEDIUM)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.LOW)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.INFORMATIONAL)
                                                                            .withEnabled(true)))),
                                                new FusionSourceSubTypeSetting()
                                                    .withEnabled(true)
                                                    .withSourceSubTypeName("Microsoft Defender for Office 365")
                                                    .withSeverityFilters(
                                                        new FusionSubTypeSeverityFilter()
                                                            .withFilters(
                                                                Arrays
                                                                    .asList(
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.HIGH)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.MEDIUM)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.LOW)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.INFORMATIONAL)
                                                                            .withEnabled(true)))),
                                                new FusionSourceSubTypeSetting()
                                                    .withEnabled(true)
                                                    .withSourceSubTypeName("Azure Sentinel scheduled analytics rules")
                                                    .withSeverityFilters(
                                                        new FusionSubTypeSeverityFilter()
                                                            .withFilters(
                                                                Arrays
                                                                    .asList(
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.HIGH)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.MEDIUM)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.LOW)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.INFORMATIONAL)
                                                                            .withEnabled(true)))))),
                                new FusionSourceSettings()
                                    .withEnabled(true)
                                    .withSourceName("Raw logs from other sources")
                                    .withSourceSubTypes(
                                        Arrays
                                            .asList(
                                                new FusionSourceSubTypeSetting()
                                                    .withEnabled(true)
                                                    .withSourceSubTypeName("Palo Alto Networks")
                                                    .withSeverityFilters(new FusionSubTypeSeverityFilter()))))),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/alertRules/CreateMicrosoftSecurityIncidentCreationAlertRule.json
     */
    /**
     * Sample code: Creates or updates a MicrosoftSecurityIncidentCreation rule.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAMicrosoftSecurityIncidentCreationRule(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .alertRules()
            .createOrUpdateWithResponse(
                "myRg",
                "myWorkspace",
                "microsoftSecurityIncidentCreationRuleExample",
                new MicrosoftSecurityIncidentCreationAlertRule()
                    .withEtag("\"260097e0-0000-0d00-0000-5d6fa88f0000\"")
                    .withDisplayName("testing displayname")
                    .withEnabled(true)
                    .withProductFilter(MicrosoftSecurityProductName.MICROSOFT_CLOUD_APP_SECURITY),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/alertRules/CreateScheduledAlertRule.json
     */
    /**
     * Sample code: Creates or updates a Scheduled alert rule.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAScheduledAlertRule(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .alertRules()
            .createOrUpdateWithResponse(
                "myRg",
                "myWorkspace",
                "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                new ScheduledAlertRule()
                    .withEtag("\"0300bf09-0000-0000-0000-5c37296e0000\"")
                    .withDescription("An example for a scheduled rule")
                    .withDisplayName("My scheduled rule")
                    .withEnabled(true)
                    .withSuppressionDuration(Duration.parse("PT1H"))
                    .withSuppressionEnabled(false)
                    .withTactics(Arrays.asList(AttackTactic.PERSISTENCE, AttackTactic.LATERAL_MOVEMENT))
                    .withTechniques(Arrays.asList("T1037", "T1021"))
                    .withIncidentConfiguration(
                        new IncidentConfiguration()
                            .withCreateIncident(true)
                            .withGroupingConfiguration(
                                new GroupingConfiguration()
                                    .withEnabled(true)
                                    .withReopenClosedIncident(false)
                                    .withLookbackDuration(Duration.parse("PT5H"))
                                    .withMatchingMethod(MatchingMethod.SELECTED)
                                    .withGroupByEntities(Arrays.asList(EntityMappingType.HOST))
                                    .withGroupByAlertDetails(Arrays.asList(AlertDetail.DISPLAY_NAME))
                                    .withGroupByCustomDetails(
                                        Arrays.asList("OperatingSystemType", "OperatingSystemName"))))
                    .withQuery("Heartbeat")
                    .withQueryFrequency(Duration.parse("PT1H"))
                    .withQueryPeriod(Duration.parse("P2DT1H30M"))
                    .withSeverity(AlertSeverity.HIGH)
                    .withTriggerOperator(TriggerOperator.GREATER_THAN)
                    .withTriggerThreshold(0)
                    .withEventGroupingSettings(
                        new EventGroupingSettings().withAggregationKind(EventGroupingAggregationKind.ALERT_PER_RESULT))
                    .withCustomDetails(mapOf("OperatingSystemName", "OSName", "OperatingSystemType", "OSType"))
                    .withEntityMappings(
                        Arrays
                            .asList(
                                new EntityMapping()
                                    .withEntityType(EntityMappingType.HOST)
                                    .withFieldMappings(
                                        Arrays
                                            .asList(
                                                new FieldMapping()
                                                    .withIdentifier("FullName")
                                                    .withColumnName("Computer"))),
                                new EntityMapping()
                                    .withEntityType(EntityMappingType.IP)
                                    .withFieldMappings(
                                        Arrays
                                            .asList(
                                                new FieldMapping()
                                                    .withIdentifier("Address")
                                                    .withColumnName("ComputerIP")))))
                    .withAlertDetailsOverride(
                        new AlertDetailsOverride()
                            .withAlertDisplayNameFormat("Alert from {{Computer}}")
                            .withAlertDescriptionFormat("Suspicious activity was made by {{ComputerIP}}")),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/alertRules/CreateFusionAlertRuleWithFusionScenarioExclusion.json
     */
    /**
     * Sample code: Creates or updates a Fusion alert rule with scenario exclusion pattern.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAFusionAlertRuleWithScenarioExclusionPattern(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .alertRules()
            .createOrUpdateWithResponse(
                "myRg",
                "myWorkspace",
                "myFirstFusionRule",
                new FusionAlertRule()
                    .withEtag("3d00c3ca-0000-0100-0000-5d42d5010000")
                    .withAlertRuleTemplateName("f71aba3d-28fb-450b-b192-4e76a83015c8")
                    .withEnabled(true)
                    .withSourceSettings(
                        Arrays
                            .asList(
                                new FusionSourceSettings().withEnabled(true).withSourceName("Anomalies"),
                                new FusionSourceSettings()
                                    .withEnabled(true)
                                    .withSourceName("Alert providers")
                                    .withSourceSubTypes(
                                        Arrays
                                            .asList(
                                                new FusionSourceSubTypeSetting()
                                                    .withEnabled(true)
                                                    .withSourceSubTypeName("Azure Active Directory Identity Protection")
                                                    .withSeverityFilters(
                                                        new FusionSubTypeSeverityFilter()
                                                            .withFilters(
                                                                Arrays
                                                                    .asList(
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.HIGH)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.MEDIUM)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.LOW)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.INFORMATIONAL)
                                                                            .withEnabled(true)))),
                                                new FusionSourceSubTypeSetting()
                                                    .withEnabled(true)
                                                    .withSourceSubTypeName("Azure Defender")
                                                    .withSeverityFilters(
                                                        new FusionSubTypeSeverityFilter()
                                                            .withFilters(
                                                                Arrays
                                                                    .asList(
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.HIGH)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.MEDIUM)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.LOW)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.INFORMATIONAL)
                                                                            .withEnabled(true)))),
                                                new FusionSourceSubTypeSetting()
                                                    .withEnabled(true)
                                                    .withSourceSubTypeName("Azure Defender for IoT")
                                                    .withSeverityFilters(
                                                        new FusionSubTypeSeverityFilter()
                                                            .withFilters(
                                                                Arrays
                                                                    .asList(
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.HIGH)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.MEDIUM)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.LOW)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.INFORMATIONAL)
                                                                            .withEnabled(true)))),
                                                new FusionSourceSubTypeSetting()
                                                    .withEnabled(true)
                                                    .withSourceSubTypeName("Microsoft 365 Defender")
                                                    .withSeverityFilters(
                                                        new FusionSubTypeSeverityFilter()
                                                            .withFilters(
                                                                Arrays
                                                                    .asList(
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.HIGH)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.MEDIUM)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.LOW)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.INFORMATIONAL)
                                                                            .withEnabled(true)))),
                                                new FusionSourceSubTypeSetting()
                                                    .withEnabled(true)
                                                    .withSourceSubTypeName("Microsoft Cloud App Security")
                                                    .withSeverityFilters(
                                                        new FusionSubTypeSeverityFilter()
                                                            .withFilters(
                                                                Arrays
                                                                    .asList(
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.HIGH)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.MEDIUM)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.LOW)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.INFORMATIONAL)
                                                                            .withEnabled(true)))),
                                                new FusionSourceSubTypeSetting()
                                                    .withEnabled(true)
                                                    .withSourceSubTypeName("Microsoft Defender for Endpoint")
                                                    .withSeverityFilters(
                                                        new FusionSubTypeSeverityFilter()
                                                            .withFilters(
                                                                Arrays
                                                                    .asList(
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.HIGH)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.MEDIUM)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.LOW)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.INFORMATIONAL)
                                                                            .withEnabled(true)))),
                                                new FusionSourceSubTypeSetting()
                                                    .withEnabled(true)
                                                    .withSourceSubTypeName("Microsoft Defender for Identity")
                                                    .withSeverityFilters(
                                                        new FusionSubTypeSeverityFilter()
                                                            .withFilters(
                                                                Arrays
                                                                    .asList(
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.HIGH)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.MEDIUM)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.LOW)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.INFORMATIONAL)
                                                                            .withEnabled(true)))),
                                                new FusionSourceSubTypeSetting()
                                                    .withEnabled(true)
                                                    .withSourceSubTypeName("Microsoft Defender for Office 365")
                                                    .withSeverityFilters(
                                                        new FusionSubTypeSeverityFilter()
                                                            .withFilters(
                                                                Arrays
                                                                    .asList(
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.HIGH)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.MEDIUM)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.LOW)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.INFORMATIONAL)
                                                                            .withEnabled(true)))),
                                                new FusionSourceSubTypeSetting()
                                                    .withEnabled(true)
                                                    .withSourceSubTypeName("Azure Sentinel scheduled analytics rules")
                                                    .withSeverityFilters(
                                                        new FusionSubTypeSeverityFilter()
                                                            .withFilters(
                                                                Arrays
                                                                    .asList(
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.HIGH)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.MEDIUM)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.LOW)
                                                                            .withEnabled(true),
                                                                        new FusionSubTypeSeverityFiltersItem()
                                                                            .withSeverity(AlertSeverity.INFORMATIONAL)
                                                                            .withEnabled(true)))))),
                                new FusionSourceSettings()
                                    .withEnabled(true)
                                    .withSourceName("Raw logs from other sources")
                                    .withSourceSubTypes(
                                        Arrays
                                            .asList(
                                                new FusionSourceSubTypeSetting()
                                                    .withEnabled(true)
                                                    .withSourceSubTypeName("Palo Alto Networks")
                                                    .withSeverityFilters(new FusionSubTypeSeverityFilter()))))),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/alertRules/CreateNrtAlertRule.json
     */
    /**
     * Sample code: Creates or updates a Nrt alert rule.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesANrtAlertRule(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .alertRules()
            .createOrUpdateWithResponse(
                "myRg",
                "myWorkspace",
                "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                new NrtAlertRule()
                    .withEtag("\"0300bf09-0000-0000-0000-5c37296e0000\"")
                    .withDescription("")
                    .withQuery(
                        "ProtectionStatus | extend HostCustomEntity = Computer | extend IPCustomEntity ="
                            + " ComputerIP_Hidden")
                    .withTactics(Arrays.asList(AttackTactic.PERSISTENCE, AttackTactic.LATERAL_MOVEMENT))
                    .withTechniques(Arrays.asList("T1037", "T1021"))
                    .withDisplayName("Rule2")
                    .withEnabled(true)
                    .withSuppressionDuration(Duration.parse("PT1H"))
                    .withSuppressionEnabled(false)
                    .withSeverity(AlertSeverity.HIGH)
                    .withIncidentConfiguration(
                        new IncidentConfiguration()
                            .withCreateIncident(true)
                            .withGroupingConfiguration(
                                new GroupingConfiguration()
                                    .withEnabled(true)
                                    .withReopenClosedIncident(false)
                                    .withLookbackDuration(Duration.parse("PT5H"))
                                    .withMatchingMethod(MatchingMethod.SELECTED)
                                    .withGroupByEntities(
                                        Arrays.asList(EntityMappingType.HOST, EntityMappingType.ACCOUNT))))
                    .withEventGroupingSettings(
                        new EventGroupingSettings().withAggregationKind(EventGroupingAggregationKind.ALERT_PER_RESULT)),
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

### AlertRules_Delete

```java
import com.azure.core.util.Context;

/** Samples for AlertRules Delete. */
public final class AlertRulesDeleteSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/alertRules/DeleteAlertRule.json
     */
    /**
     * Sample code: Delete an alert rule.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteAnAlertRule(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .alertRules()
            .deleteWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5", Context.NONE);
    }
}
```

### AlertRules_Get

```java
import com.azure.core.util.Context;

/** Samples for AlertRules Get. */
public final class AlertRulesGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/alertRules/GetScheduledAlertRule.json
     */
    /**
     * Sample code: Get a Scheduled alert rule.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAScheduledAlertRule(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .alertRules()
            .getWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/alertRules/GetNrtAlertRule.json
     */
    /**
     * Sample code: Get an Nrt alert rule.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAnNrtAlertRule(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .alertRules()
            .getWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/alertRules/GetFusionAlertRule.json
     */
    /**
     * Sample code: Get a Fusion alert rule.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAFusionAlertRule(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.alertRules().getWithResponse("myRg", "myWorkspace", "myFirstFusionRule", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/alertRules/GetMicrosoftSecurityIncidentCreationAlertRule.json
     */
    /**
     * Sample code: Get a MicrosoftSecurityIncidentCreation rule.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAMicrosoftSecurityIncidentCreationRule(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .alertRules()
            .getWithResponse("myRg", "myWorkspace", "microsoftSecurityIncidentCreationRuleExample", Context.NONE);
    }
}
```

### AlertRules_List

```java
import com.azure.core.util.Context;

/** Samples for AlertRules List. */
public final class AlertRulesListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/alertRules/GetAllAlertRules.json
     */
    /**
     * Sample code: Get all alert rules.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllAlertRules(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.alertRules().list("myRg", "myWorkspace", Context.NONE);
    }
}
```

### AutomationRules_CreateOrUpdate

```java
import com.azure.resourcemanager.securityinsights.models.AutomationRuleAction;
import com.azure.resourcemanager.securityinsights.models.AutomationRuleTriggeringLogic;
import java.util.List;

/** Samples for AutomationRules CreateOrUpdate. */
public final class AutomationRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/automationRules/AutomationRules_CreateOrUpdate.json
     */
    /**
     * Sample code: AutomationRules_CreateOrUpdate.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void automationRulesCreateOrUpdate(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .automationRules()
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
import com.azure.core.util.Context;

/** Samples for AutomationRules Delete. */
public final class AutomationRulesDeleteSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/automationRules/AutomationRules_Delete.json
     */
    /**
     * Sample code: AutomationRules_Delete.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void automationRulesDelete(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .automationRules()
            .deleteWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5", Context.NONE);
    }
}
```

### AutomationRules_Get

```java
import com.azure.core.util.Context;

/** Samples for AutomationRules Get. */
public final class AutomationRulesGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/automationRules/AutomationRules_Get.json
     */
    /**
     * Sample code: AutomationRules_Get.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void automationRulesGet(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .automationRules()
            .getWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5", Context.NONE);
    }
}
```

### AutomationRules_List

```java
import com.azure.core.util.Context;

/** Samples for AutomationRules List. */
public final class AutomationRulesListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/automationRules/AutomationRules_List.json
     */
    /**
     * Sample code: AutomationRules_List.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void automationRulesList(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.automationRules().list("myRg", "myWorkspace", Context.NONE);
    }
}
```

### BookmarkOperation_Expand

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securityinsights.models.BookmarkExpandParameters;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Samples for BookmarkOperation Expand. */
public final class BookmarkOperationExpandSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/bookmarks/expand/PostExpandBookmark.json
     */
    /**
     * Sample code: Expand an bookmark.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void expandAnBookmark(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .bookmarkOperations()
            .expandWithResponse(
                "myRg",
                "myWorkspace",
                "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                new BookmarkExpandParameters()
                    .withEndTime(OffsetDateTime.parse("2020-01-24T17:21:00.000Z"))
                    .withExpansionId(UUID.fromString("27f76e63-c41b-480f-bb18-12ad2e011d49"))
                    .withStartTime(OffsetDateTime.parse("2019-12-25T17:21:00.000Z")),
                Context.NONE);
    }
}
```

### BookmarkRelations_CreateOrUpdate

```java
/** Samples for BookmarkRelations CreateOrUpdate. */
public final class BookmarkRelationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/bookmarks/relations/CreateBookmarkRelation.json
     */
    /**
     * Sample code: Creates or updates a bookmark relation.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesABookmarkRelation(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .bookmarkRelations()
            .define("4bb36b7b-26ff-4d1c-9cbe-0d8ab3da0014")
            .withExistingBookmark("myRg", "myWorkspace", "2216d0e1-91e3-4902-89fd-d2df8c535096")
            .withRelatedResourceId(
                "/subscriptions/d0cfe6b2-9ac0-4464-9919-dccaee2e48c0/resourceGroups/myRg/providers/Microsoft.OperationalInsights/workspaces/myWorkspace/providers/Microsoft.SecurityInsights/incidents/afbd324f-6c48-459c-8710-8d1e1cd03812")
            .create();
    }
}
```

### BookmarkRelations_Delete

```java
import com.azure.core.util.Context;

/** Samples for BookmarkRelations Delete. */
public final class BookmarkRelationsDeleteSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/bookmarks/relations/DeleteBookmarkRelation.json
     */
    /**
     * Sample code: Delete the bookmark relation.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteTheBookmarkRelation(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .bookmarkRelations()
            .deleteWithResponse(
                "myRg",
                "myWorkspace",
                "2216d0e1-91e3-4902-89fd-d2df8c535096",
                "4bb36b7b-26ff-4d1c-9cbe-0d8ab3da0014",
                Context.NONE);
    }
}
```

### BookmarkRelations_Get

```java
import com.azure.core.util.Context;

/** Samples for BookmarkRelations Get. */
public final class BookmarkRelationsGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/bookmarks/relations/GetBookmarkRelationByName.json
     */
    /**
     * Sample code: Get a bookmark relation.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getABookmarkRelation(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .bookmarkRelations()
            .getWithResponse(
                "myRg",
                "myWorkspace",
                "2216d0e1-91e3-4902-89fd-d2df8c535096",
                "4bb36b7b-26ff-4d1c-9cbe-0d8ab3da0014",
                Context.NONE);
    }
}
```

### BookmarkRelations_List

```java
import com.azure.core.util.Context;

/** Samples for BookmarkRelations List. */
public final class BookmarkRelationsListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/bookmarks/relations/GetAllBookmarkRelations.json
     */
    /**
     * Sample code: Get all bookmark relations.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllBookmarkRelations(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .bookmarkRelations()
            .list("myRg", "myWorkspace", "2216d0e1-91e3-4902-89fd-d2df8c535096", null, null, null, null, Context.NONE);
    }
}
```

### Bookmarks_CreateOrUpdate

```java
import com.azure.resourcemanager.securityinsights.models.AttackTactic;
import com.azure.resourcemanager.securityinsights.models.BookmarkEntityMappings;
import com.azure.resourcemanager.securityinsights.models.EntityFieldMapping;
import com.azure.resourcemanager.securityinsights.models.UserInfo;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;

/** Samples for Bookmarks CreateOrUpdate. */
public final class BookmarksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/bookmarks/CreateBookmark.json
     */
    /**
     * Sample code: Creates or updates a bookmark.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesABookmark(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .bookmarks()
            .define("73e01a99-5cd7-4139-a149-9f2736ff2ab5")
            .withExistingWorkspace("myRg", "myWorkspace")
            .withEtag("\"0300bf09-0000-0000-0000-5c37296e0000\"")
            .withCreated(OffsetDateTime.parse("2021-09-01T13:15:30Z"))
            .withCreatedBy(new UserInfo().withObjectId(UUID.fromString("2046feea-040d-4a46-9e2b-91c2941bfa70")))
            .withDisplayName("My bookmark")
            .withLabels(Arrays.asList("Tag1", "Tag2"))
            .withNotes("Found a suspicious activity")
            .withQuery("SecurityEvent | where TimeGenerated > ago(1d) and TimeGenerated < ago(2d)")
            .withQueryResult("Security Event query result")
            .withUpdated(OffsetDateTime.parse("2021-09-01T13:15:30Z"))
            .withUpdatedBy(new UserInfo().withObjectId(UUID.fromString("2046feea-040d-4a46-9e2b-91c2941bfa70")))
            .withEntityMappings(
                Arrays
                    .asList(
                        new BookmarkEntityMappings()
                            .withEntityType("Account")
                            .withFieldMappings(
                                Arrays
                                    .asList(
                                        new EntityFieldMapping()
                                            .withIdentifier("Fullname")
                                            .withValue("johndoe@microsoft.com")))))
            .withTactics(Arrays.asList(AttackTactic.EXECUTION))
            .withTechniques(Arrays.asList("T1609"))
            .create();
    }
}
```

### Bookmarks_Delete

```java
import com.azure.core.util.Context;

/** Samples for Bookmarks Delete. */
public final class BookmarksDeleteSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/bookmarks/DeleteBookmark.json
     */
    /**
     * Sample code: Delete a bookmark.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteABookmark(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .bookmarks()
            .deleteWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5", Context.NONE);
    }
}
```

### Bookmarks_Get

```java
import com.azure.core.util.Context;

/** Samples for Bookmarks Get. */
public final class BookmarksGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/bookmarks/GetBookmarkById.json
     */
    /**
     * Sample code: Get a bookmark.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getABookmark(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .bookmarks()
            .getWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5", Context.NONE);
    }
}
```

### Bookmarks_List

```java
import com.azure.core.util.Context;

/** Samples for Bookmarks List. */
public final class BookmarksListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/bookmarks/GetBookmarks.json
     */
    /**
     * Sample code: Get all bookmarks.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllBookmarks(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.bookmarks().list("myRg", "myWorkspace", Context.NONE);
    }
}
```

### DataConnectors_Connect

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.securityinsights.models.ConnectAuthKind;
import com.azure.resourcemanager.securityinsights.models.DataConnectorConnectBody;
import java.io.IOException;
import java.util.Arrays;

/** Samples for DataConnectors Connect. */
public final class DataConnectorsConnectSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/ConnectAPIPollingV2Logs.json
     */
    /**
     * Sample code: Connect an APIPolling V2 logs data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void connectAnAPIPollingV2LogsDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) throws IOException {
        manager
            .dataConnectors()
            .connectWithResponse(
                "myRg",
                "myWorkspace",
                "316ec55e-7138-4d63-ab18-90c8a60fd1c8",
                new DataConnectorConnectBody()
                    .withKind(ConnectAuthKind.APIKEY)
                    .withApiKey("123456789")
                    .withDataCollectionEndpoint("https://test.eastus.ingest.monitor.azure.com")
                    .withDataCollectionRuleImmutableId("dcr-34adsj9o7d6f9de204478b9cgb43b631")
                    .withOutputStream("Custom-MyTableRawData")
                    .withRequestConfigUserInputValues(
                        Arrays
                            .asList(
                                SerializerFactory
                                    .createDefaultManagementSerializerAdapter()
                                    .deserialize(
                                        "{\"displayText\":\"Organization"
                                            + " Name\",\"placeHolderName\":\"{{placeHolder1}}\",\"placeHolderValue\":\"somePlaceHolderValue\",\"requestObjectKey\":\"apiEndpoint\"}",
                                        Object.class,
                                        SerializerEncoding.JSON))),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/ConnectAPIPolling.json
     */
    /**
     * Sample code: Connect an APIPolling data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void connectAnAPIPollingDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) throws IOException {
        manager
            .dataConnectors()
            .connectWithResponse(
                "myRg",
                "myWorkspace",
                "316ec55e-7138-4d63-ab18-90c8a60fd1c8",
                new DataConnectorConnectBody()
                    .withKind(ConnectAuthKind.APIKEY)
                    .withApiKey("123456789")
                    .withRequestConfigUserInputValues(
                        Arrays
                            .asList(
                                SerializerFactory
                                    .createDefaultManagementSerializerAdapter()
                                    .deserialize(
                                        "{\"displayText\":\"Organization"
                                            + " Name\",\"placeHolderName\":\"{{placeHolder1}}\",\"placeHolderValue\":\"somePlaceHolderValue\",\"requestObjectKey\":\"apiEndpoint\"}",
                                        Object.class,
                                        SerializerEncoding.JSON))),
                Context.NONE);
    }
}
```

### DataConnectors_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.securityinsights.models.Availability;
import com.azure.resourcemanager.securityinsights.models.AvailabilityStatus;
import com.azure.resourcemanager.securityinsights.models.CodelessApiPollingDataConnector;
import com.azure.resourcemanager.securityinsights.models.CodelessConnectorPollingAuthProperties;
import com.azure.resourcemanager.securityinsights.models.CodelessConnectorPollingConfigProperties;
import com.azure.resourcemanager.securityinsights.models.CodelessConnectorPollingPagingProperties;
import com.azure.resourcemanager.securityinsights.models.CodelessConnectorPollingRequestProperties;
import com.azure.resourcemanager.securityinsights.models.CodelessConnectorPollingResponseProperties;
import com.azure.resourcemanager.securityinsights.models.CodelessUiConnectorConfigProperties;
import com.azure.resourcemanager.securityinsights.models.CodelessUiConnectorConfigPropertiesConnectivityCriteriaItem;
import com.azure.resourcemanager.securityinsights.models.CodelessUiConnectorConfigPropertiesDataTypesItem;
import com.azure.resourcemanager.securityinsights.models.CodelessUiConnectorConfigPropertiesGraphQueriesItem;
import com.azure.resourcemanager.securityinsights.models.CodelessUiConnectorConfigPropertiesInstructionStepsItem;
import com.azure.resourcemanager.securityinsights.models.CodelessUiConnectorConfigPropertiesSampleQueriesItem;
import com.azure.resourcemanager.securityinsights.models.CodelessUiDataConnector;
import com.azure.resourcemanager.securityinsights.models.ConnectivityType;
import com.azure.resourcemanager.securityinsights.models.DataTypeState;
import com.azure.resourcemanager.securityinsights.models.Dynamics365DataConnector;
import com.azure.resourcemanager.securityinsights.models.Dynamics365DataConnectorDataTypes;
import com.azure.resourcemanager.securityinsights.models.Dynamics365DataConnectorDataTypesDynamics365CdsActivities;
import com.azure.resourcemanager.securityinsights.models.InstructionStepsInstructionsItem;
import com.azure.resourcemanager.securityinsights.models.Office365ProjectConnectorDataTypes;
import com.azure.resourcemanager.securityinsights.models.Office365ProjectConnectorDataTypesLogs;
import com.azure.resourcemanager.securityinsights.models.Office365ProjectDataConnector;
import com.azure.resourcemanager.securityinsights.models.OfficeDataConnector;
import com.azure.resourcemanager.securityinsights.models.OfficeDataConnectorDataTypes;
import com.azure.resourcemanager.securityinsights.models.OfficeDataConnectorDataTypesExchange;
import com.azure.resourcemanager.securityinsights.models.OfficeDataConnectorDataTypesSharePoint;
import com.azure.resourcemanager.securityinsights.models.OfficeDataConnectorDataTypesTeams;
import com.azure.resourcemanager.securityinsights.models.OfficePowerBIConnectorDataTypes;
import com.azure.resourcemanager.securityinsights.models.OfficePowerBIConnectorDataTypesLogs;
import com.azure.resourcemanager.securityinsights.models.OfficePowerBIDataConnector;
import com.azure.resourcemanager.securityinsights.models.PermissionProviderScope;
import com.azure.resourcemanager.securityinsights.models.Permissions;
import com.azure.resourcemanager.securityinsights.models.PermissionsCustomsItem;
import com.azure.resourcemanager.securityinsights.models.PermissionsResourceProviderItem;
import com.azure.resourcemanager.securityinsights.models.PollingFrequency;
import com.azure.resourcemanager.securityinsights.models.ProviderName;
import com.azure.resourcemanager.securityinsights.models.RequiredPermissions;
import com.azure.resourcemanager.securityinsights.models.SettingType;
import com.azure.resourcemanager.securityinsights.models.TIDataConnector;
import com.azure.resourcemanager.securityinsights.models.TIDataConnectorDataTypes;
import com.azure.resourcemanager.securityinsights.models.TIDataConnectorDataTypesIndicators;
import com.azure.resourcemanager.securityinsights.models.TiTaxiiDataConnector;
import com.azure.resourcemanager.securityinsights.models.TiTaxiiDataConnectorDataTypes;
import com.azure.resourcemanager.securityinsights.models.TiTaxiiDataConnectorDataTypesTaxiiClient;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Samples for DataConnectors CreateOrUpdate. */
public final class DataConnectorsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/CreateOfficeDataConnetor.json
     */
    /**
     * Sample code: Creates or updates an Office365 data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAnOffice365DataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .createOrUpdateWithResponse(
                "myRg",
                "myWorkspace",
                "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                new OfficeDataConnector()
                    .withEtag("\"0300bf09-0000-0000-0000-5c37296e0000\"")
                    .withDataTypes(
                        new OfficeDataConnectorDataTypes()
                            .withExchange(new OfficeDataConnectorDataTypesExchange().withState(DataTypeState.ENABLED))
                            .withSharePoint(
                                new OfficeDataConnectorDataTypesSharePoint().withState(DataTypeState.ENABLED))
                            .withTeams(new OfficeDataConnectorDataTypesTeams().withState(DataTypeState.ENABLED)))
                    .withTenantId("2070ecc9-b4d5-4ae4-adaa-936fa1954fa8"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/CreateOfficePowerBIDataConnector.json
     */
    /**
     * Sample code: Creates or updates an Office PowerBI data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAnOfficePowerBIDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .createOrUpdateWithResponse(
                "myRg",
                "myWorkspace",
                "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                new OfficePowerBIDataConnector()
                    .withEtag("\"0300bf09-0000-0000-0000-5c37296e0000\"")
                    .withDataTypes(
                        new OfficePowerBIConnectorDataTypes()
                            .withLogs(new OfficePowerBIConnectorDataTypesLogs().withState(DataTypeState.ENABLED)))
                    .withTenantId("2070ecc9-b4d5-4ae4-adaa-936fa1954fa8"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/CreateThreatIntelligenceTaxiiDataConnector.json
     */
    /**
     * Sample code: Creates or updates a Threat Intelligence Taxii data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAThreatIntelligenceTaxiiDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .createOrUpdateWithResponse(
                "myRg",
                "myWorkspace",
                "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                new TiTaxiiDataConnector()
                    .withEtag("d12423f6-a60b-4ca5-88c0-feb1a182d0f0")
                    .withWorkspaceId("dd124572-4962-4495-9bd2-9dade12314b4")
                    .withFriendlyName("testTaxii")
                    .withTaxiiServer("https://limo.anomali.com/api/v1/taxii2/feeds")
                    .withCollectionId("135")
                    .withUsername("--")
                    .withPassword("--")
                    .withTaxiiLookbackPeriod(OffsetDateTime.parse("2020-01-01T13:00:30.123Z"))
                    .withPollingFrequency(PollingFrequency.ONCE_ADAY)
                    .withDataTypes(
                        new TiTaxiiDataConnectorDataTypes()
                            .withTaxiiClient(
                                new TiTaxiiDataConnectorDataTypesTaxiiClient().withState(DataTypeState.ENABLED)))
                    .withTenantId("06b3ccb8-1384-4bcc-aec7-852f6d57161b"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/CreateThreatIntelligenceDataConnector.json
     */
    /**
     * Sample code: Creates or updates an Threat Intelligence Platform data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAnThreatIntelligencePlatformDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .createOrUpdateWithResponse(
                "myRg",
                "myWorkspace",
                "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                new TIDataConnector()
                    .withTipLookbackPeriod(OffsetDateTime.parse("2020-01-01T13:00:30.123Z"))
                    .withDataTypes(
                        new TIDataConnectorDataTypes()
                            .withIndicators(new TIDataConnectorDataTypesIndicators().withState(DataTypeState.ENABLED)))
                    .withTenantId("06b3ccb8-1384-4bcc-aec7-852f6d57161b"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/CreateDynamics365DataConnetor.json
     */
    /**
     * Sample code: Creates or updates a Dynamics365 data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesADynamics365DataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .createOrUpdateWithResponse(
                "myRg",
                "myWorkspace",
                "c2541efb-c9a6-47fe-9501-87d1017d1512",
                new Dynamics365DataConnector()
                    .withEtag("\"0300bf09-0000-0000-0000-5c37296e0000\"")
                    .withDataTypes(
                        new Dynamics365DataConnectorDataTypes()
                            .withDynamics365CdsActivities(
                                new Dynamics365DataConnectorDataTypesDynamics365CdsActivities()
                                    .withState(DataTypeState.ENABLED)))
                    .withTenantId("2070ecc9-b4d5-4ae4-adaa-936fa1954fa8"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/CreateGenericUI.json
     */
    /**
     * Sample code: Creates or updates a GenericUI data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAGenericUIDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) throws IOException {
        manager
            .dataConnectors()
            .createOrUpdateWithResponse(
                "myRg",
                "myWorkspace",
                "316ec55e-7138-4d63-ab18-90c8a60fd1c8",
                new CodelessUiDataConnector()
                    .withConnectorUiConfig(
                        new CodelessUiConnectorConfigProperties()
                            .withTitle("Qualys Vulnerability Management (CCP DEMO)")
                            .withPublisher("Qualys")
                            .withDescriptionMarkdown(
                                "The [Qualys Vulnerability Management"
                                    + " (VM)](https://www.qualys.com/apps/vulnerability-management/) data connector"
                                    + " provides the capability to ingest vulnerability host detection data into Azure"
                                    + " Sentinel through the Qualys API. The connector provides visibility into host"
                                    + " detection data from vulerability scans. This connector provides Azure Sentinel"
                                    + " the capability to view dashboards, create custom alerts, and improve"
                                    + " investigation ")
                            .withGraphQueriesTableName("QualysHostDetection_CL")
                            .withGraphQueries(
                                Arrays
                                    .asList(
                                        new CodelessUiConnectorConfigPropertiesGraphQueriesItem()
                                            .withMetricName("Total data received")
                                            .withLegend("{{graphQueriesTableName}}")
                                            .withBaseQuery("{{graphQueriesTableName}}")))
                            .withSampleQueries(
                                Arrays
                                    .asList(
                                        new CodelessUiConnectorConfigPropertiesSampleQueriesItem()
                                            .withDescription("Top 10 Vulerabilities detected")
                                            .withQuery(
                                                "{{graphQueriesTableName}}\n"
                                                    + " | mv-expand todynamic(Detections_s)\n"
                                                    + " | extend Vulnerability = tostring(Detections_s.Results)\n"
                                                    + " | summarize count() by Vulnerability\n"
                                                    + " | top 10 by count_")))
                            .withDataTypes(
                                Arrays
                                    .asList(
                                        new CodelessUiConnectorConfigPropertiesDataTypesItem()
                                            .withName("{{graphQueriesTableName}}")
                                            .withLastDataReceivedQuery(
                                                "{{graphQueriesTableName}}\n"
                                                    + "            | summarize Time = max(TimeGenerated)\n"
                                                    + "            | where isnotempty(Time)")))
                            .withConnectivityCriteria(
                                Arrays
                                    .asList(
                                        new CodelessUiConnectorConfigPropertiesConnectivityCriteriaItem()
                                            .withType(ConnectivityType.IS_CONNECTED_QUERY)
                                            .withValue(
                                                Arrays
                                                    .asList(
                                                        "{{graphQueriesTableName}}\n"
                                                            + "            | summarize LastLogReceived ="
                                                            + " max(TimeGenerated)\n"
                                                            + "            | project IsConnected = LastLogReceived >"
                                                            + " ago(30d)"))))
                            .withAvailability(new Availability().withStatus(AvailabilityStatus.ONE).withIsPreview(true))
                            .withPermissions(
                                new Permissions()
                                    .withResourceProvider(
                                        Arrays
                                            .asList(
                                                new PermissionsResourceProviderItem()
                                                    .withProvider(
                                                        ProviderName.MICROSOFT_OPERATIONAL_INSIGHTS_WORKSPACES)
                                                    .withPermissionsDisplayText(
                                                        "read and write permissions on the workspace are required.")
                                                    .withProviderDisplayName("Workspace")
                                                    .withScope(PermissionProviderScope.WORKSPACE)
                                                    .withRequiredPermissions(
                                                        new RequiredPermissions()
                                                            .withWrite(true)
                                                            .withRead(true)
                                                            .withDelete(true)),
                                                new PermissionsResourceProviderItem()
                                                    .withProvider(
                                                        ProviderName
                                                            .MICROSOFT_OPERATIONAL_INSIGHTS_WORKSPACES_SHARED_KEYS)
                                                    .withPermissionsDisplayText(
                                                        "read permissions to shared keys for the workspace are"
                                                            + " required. [See the documentation to learn more about"
                                                            + " workspace"
                                                            + " keys](https://docs.microsoft.com/azure/azure-monitor/platform/agent-windows#obtain-workspace-id-and-key).")
                                                    .withProviderDisplayName("Keys")
                                                    .withScope(PermissionProviderScope.WORKSPACE)
                                                    .withRequiredPermissions(
                                                        new RequiredPermissions().withAction(true))))
                                    .withCustoms(
                                        Arrays
                                            .asList(
                                                new PermissionsCustomsItem()
                                                    .withName("Microsoft.Web/sites permissions")
                                                    .withDescription(
                                                        "Read and write permissions to Azure Functions to create a"
                                                            + " Function App is required. [See the documentation to"
                                                            + " learn more about Azure"
                                                            + " Functions](https://docs.microsoft.com/azure/azure-functions/)."),
                                                new PermissionsCustomsItem()
                                                    .withName("Qualys API Key")
                                                    .withDescription(
                                                        "A Qualys VM API username and password is required. [See the"
                                                            + " documentation to learn more about Qualys VM"
                                                            + " API](https://www.qualys.com/docs/qualys-api-vmpc-user-guide.pdf)."))))
                            .withInstructionSteps(
                                Arrays
                                    .asList(
                                        new CodelessUiConnectorConfigPropertiesInstructionStepsItem()
                                            .withTitle("")
                                            .withDescription(
                                                ">**NOTE:** This connector uses Azure Functions to connect to Qualys VM"
                                                    + " to pull its logs into Azure Sentinel. This might result in"
                                                    + " additional data ingestion costs. Check the [Azure Functions"
                                                    + " pricing"
                                                    + " page](https://azure.microsoft.com/pricing/details/functions/)"
                                                    + " for details."),
                                        new CodelessUiConnectorConfigPropertiesInstructionStepsItem()
                                            .withTitle("")
                                            .withDescription(
                                                ">**(Optional Step)** Securely store workspace and API authorization"
                                                    + " key(s) or token(s) in Azure Key Vault. Azure Key Vault provides"
                                                    + " a secure mechanism to store and retrieve key values. [Follow"
                                                    + " these"
                                                    + " instructions](https://docs.microsoft.com/azure/app-service/app-service-key-vault-references)"
                                                    + " to use Azure Key Vault with an Azure Function App."),
                                        new CodelessUiConnectorConfigPropertiesInstructionStepsItem()
                                            .withTitle("")
                                            .withDescription(
                                                "**STEP 1 - Configuration steps for the Qualys VM API**\n\n"
                                                    + "1. Log into the Qualys Vulnerability Management console with an"
                                                    + " administrator account, select the **Users** tab and the"
                                                    + " **Users** subtab. \n"
                                                    + "2. Click on the **New** drop-down menu and select **Users..**\n"
                                                    + "3. Create a username and password for the API account. \n"
                                                    + "4. In the **User Roles** tab, ensure the account role is set to"
                                                    + " **Manager** and access is allowed to **GUI** and **API**\n"
                                                    + "4. Log out of the administrator account and log into the console"
                                                    + " with the new API credentials for validation, then log out of"
                                                    + " the API account. \n"
                                                    + "5. Log back into the console using an administrator account and"
                                                    + " modify the API accounts User Roles, removing access to **GUI**."
                                                    + " \n"
                                                    + "6. Save all changes."),
                                        new CodelessUiConnectorConfigPropertiesInstructionStepsItem()
                                            .withTitle("")
                                            .withDescription(
                                                "**STEP 2 - Choose ONE from the following two deployment options to"
                                                    + " deploy the connector and the associated Azure Function**\n\n"
                                                    + ">**IMPORTANT:** Before deploying the Qualys VM connector, have"
                                                    + " the Workspace ID and Workspace Primary Key (can be copied from"
                                                    + " the following), as well as the Qualys VM API Authorization"
                                                    + " Key(s), readily available.")
                                            .withInstructions(
                                                Arrays
                                                    .asList(
                                                        new InstructionStepsInstructionsItem()
                                                            .withParameters(
                                                                SerializerFactory
                                                                    .createDefaultManagementSerializerAdapter()
                                                                    .deserialize(
                                                                        "{\"fillWith\":[\"WorkspaceId\"],\"label\":\"Workspace"
                                                                            + " ID\"}",
                                                                        Object.class,
                                                                        SerializerEncoding.JSON))
                                                            .withType(SettingType.COPYABLE_LABEL),
                                                        new InstructionStepsInstructionsItem()
                                                            .withParameters(
                                                                SerializerFactory
                                                                    .createDefaultManagementSerializerAdapter()
                                                                    .deserialize(
                                                                        "{\"fillWith\":[\"PrimaryKey\"],\"label\":\"Primary"
                                                                            + " Key\"}",
                                                                        Object.class,
                                                                        SerializerEncoding.JSON))
                                                            .withType(SettingType.COPYABLE_LABEL))),
                                        new CodelessUiConnectorConfigPropertiesInstructionStepsItem()
                                            .withTitle("Option 1 - Azure Resource Manager (ARM) Template")
                                            .withDescription(
                                                "Use this method for automated deployment of the Qualys VM connector"
                                                    + " using an ARM Tempate.\n\n"
                                                    + "1. Click the **Deploy to Azure** button below. \n\n"
                                                    + "\t[![Deploy To"
                                                    + " Azure](https://aka.ms/deploytoazurebutton)](https://aka.ms/sentinelqualysvmazuredeploy)\n"
                                                    + "2. Select the preferred **Subscription**, **Resource Group** and"
                                                    + " **Location**. \n"
                                                    + "3. Enter the **Workspace ID**, **Workspace Key**, **API"
                                                    + " Username**, **API Password** , update the **URI**, and any"
                                                    + " additional URI **Filter Parameters** (each filter should be"
                                                    + " separated by an \"&\" symbol, no spaces.) \n"
                                                    + "> - Enter the URI that corresponds to your region. The complete"
                                                    + " list of API Server URLs can be [found"
                                                    + " here](https://www.qualys.com/docs/qualys-api-vmpc-user-guide.pdf#G4.735348)"
                                                    + " -- There is no need to add a time suffix to the URI, the"
                                                    + " Function App will dynamically append the Time Value to the URI"
                                                    + " in the proper format. \n"
                                                    + " - The default **Time Interval** is set to pull the last five"
                                                    + " (5) minutes of data. If the time interval needs to be modified,"
                                                    + " it is recommended to change the Function App Timer Trigger"
                                                    + " accordingly (in the function.json file, post deployment) to"
                                                    + " prevent overlapping data ingestion. \n"
                                                    + "> - Note: If using Azure Key Vault secrets for any of the values"
                                                    + " above, use the`@Microsoft.KeyVault(SecretUri={Security"
                                                    + " Identifier})`schema in place of the string values. Refer to"
                                                    + " [Key Vault references"
                                                    + " documentation](https://docs.microsoft.com/azure/app-service/app-service-key-vault-references)"
                                                    + " for further details. \n"
                                                    + "4. Mark the checkbox labeled **I agree to the terms and"
                                                    + " conditions stated above**. \n"
                                                    + "5. Click **Purchase** to deploy."),
                                        new CodelessUiConnectorConfigPropertiesInstructionStepsItem()
                                            .withTitle("Option 2 - Manual Deployment of Azure Functions")
                                            .withDescription(
                                                "Use the following step-by-step instructions to deploy the Quayls VM"
                                                    + " connector manually with Azure Functions."),
                                        new CodelessUiConnectorConfigPropertiesInstructionStepsItem()
                                            .withTitle("")
                                            .withDescription(
                                                "**1. Create a Function App**\n\n"
                                                    + "1.  From the Azure Portal, navigate to [Function"
                                                    + " App](https://portal.azure.com/#blade/HubsExtension/BrowseResource/resourceType/Microsoft.Web%2Fsites/kind/functionapp),"
                                                    + " and select **+ Add**.\n"
                                                    + "2. In the **Basics** tab, ensure Runtime stack is set to"
                                                    + " **Powershell Core**. \n"
                                                    + "3. In the **Hosting** tab, ensure the **Consumption"
                                                    + " (Serverless)** plan type is selected.\n"
                                                    + "4. Make other preferrable configuration changes, if needed, then"
                                                    + " click **Create**."),
                                        new CodelessUiConnectorConfigPropertiesInstructionStepsItem()
                                            .withTitle("")
                                            .withDescription(
                                                "**2. Import Function App Code**\n\n"
                                                    + "1. In the newly created Function App, select **Functions** on"
                                                    + " the left pane and click **+ New Function**.\n"
                                                    + "2. Select **Timer Trigger**.\n"
                                                    + "3. Enter a unique Function **Name** and leave the default cron"
                                                    + " schedule of every 5 minutes, then click **Create**.\n"
                                                    + "5. Click on **Code + Test** on the left pane. \n"
                                                    + "6. Copy the [Function App"
                                                    + " Code](https://aka.ms/sentinelqualysvmazurefunctioncode) and"
                                                    + " paste into the Function App `run.ps1` editor.\n"
                                                    + "7. Click **Save**."),
                                        new CodelessUiConnectorConfigPropertiesInstructionStepsItem()
                                            .withTitle("")
                                            .withDescription(
                                                "**3. Configure the Function App**\n\n"
                                                    + "1. In the Function App, select the Function App Name and select"
                                                    + " **Configuration**.\n"
                                                    + "2. In the **Application settings** tab, select **+ New"
                                                    + " application setting**.\n"
                                                    + "3. Add each of the following seven (7) application settings"
                                                    + " individually, with their respective string values"
                                                    + " (case-sensitive): \n"
                                                    + "\t\tapiUsername\n"
                                                    + "\t\tapiPassword\n"
                                                    + "\t\tworkspaceID\n"
                                                    + "\t\tworkspaceKey\n"
                                                    + "\t\turi\n"
                                                    + "\t\tfilterParameters\n"
                                                    + "\t\ttimeInterval\n"
                                                    + "> - Enter the URI that corresponds to your region. The complete"
                                                    + " list of API Server URLs can be [found"
                                                    + " here](https://www.qualys.com/docs/qualys-api-vmpc-user-guide.pdf#G4.735348)."
                                                    + " The `uri` value must follow the following schema: `https://<API"
                                                    + " Server>/api/2.0/fo/asset/host/vm/detection/?action=list&vm_processed_after=`"
                                                    + " -- There is no need to add a time suffix to the URI, the"
                                                    + " Function App will dynamically append the Time Value to the URI"
                                                    + " in the proper format.\n"
                                                    + "> - Add any additional filter parameters, for the"
                                                    + " `filterParameters` variable, that need to be appended to the"
                                                    + " URI. Each parameter should be seperated by an \"&\" symbol and"
                                                    + " should not include any spaces.\n"
                                                    + "> - Set the `timeInterval` (in minutes) to the value of `5` to"
                                                    + " correspond to the Timer Trigger of every `5` minutes. If the"
                                                    + " time interval needs to be modified, it is recommended to change"
                                                    + " the Function App Timer Trigger accordingly to prevent"
                                                    + " overlapping data ingestion.\n"
                                                    + "> - Note: If using Azure Key Vault, use"
                                                    + " the`@Microsoft.KeyVault(SecretUri={Security Identifier})`schema"
                                                    + " in place of the string values. Refer to [Key Vault references"
                                                    + " documentation](https://docs.microsoft.com/azure/app-service/app-service-key-vault-references)"
                                                    + " for further details.\n"
                                                    + "4. Once all application settings have been entered, click"
                                                    + " **Save**."),
                                        new CodelessUiConnectorConfigPropertiesInstructionStepsItem()
                                            .withTitle("")
                                            .withDescription(
                                                "**4. Configure the host.json**.\n\n"
                                                    + "Due to the potentially large amount of Qualys host detection"
                                                    + " data being ingested, it can cause the execution time to surpass"
                                                    + " the default Function App timeout of five (5) minutes. Increase"
                                                    + " the default timeout duration to the maximum of ten (10)"
                                                    + " minutes, under the Consumption Plan, to allow more time for the"
                                                    + " Function App to execute.\n\n"
                                                    + "1. In the Function App, select the Function App Name and select"
                                                    + " the **App Service Editor** blade.\n"
                                                    + "2. Click **Go** to open the editor, then select the"
                                                    + " **host.json** file under the **wwwroot** directory.\n"
                                                    + "3. Add the line `\"functionTimeout\": \"00:10:00\",` above the"
                                                    + " `managedDependancy` line \n"
                                                    + "4. Ensure **SAVED** appears on the top right corner of the"
                                                    + " editor, then exit the editor.\n\n"
                                                    + "> NOTE: If a longer timeout duration is required, consider"
                                                    + " upgrading to an [App Service"
                                                    + " Plan](https://docs.microsoft.com/azure/azure-functions/functions-scale#timeout)")))),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/CreateOffice365ProjectDataConnetor.json
     */
    /**
     * Sample code: Creates or updates an Office365 Project data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAnOffice365ProjectDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .createOrUpdateWithResponse(
                "myRg",
                "myWorkspace",
                "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                new Office365ProjectDataConnector()
                    .withEtag("\"0300bf09-0000-0000-0000-5c37296e0000\"")
                    .withDataTypes(
                        new Office365ProjectConnectorDataTypes()
                            .withLogs(new Office365ProjectConnectorDataTypesLogs().withState(DataTypeState.ENABLED)))
                    .withTenantId("2070ecc9-b4d5-4ae4-adaa-936fa1954fa8"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/CreateAPIPolling.json
     */
    /**
     * Sample code: Creates or updates a APIPolling data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAAPIPollingDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) throws IOException {
        manager
            .dataConnectors()
            .createOrUpdateWithResponse(
                "myRg",
                "myWorkspace",
                "316ec55e-7138-4d63-ab18-90c8a60fd1c8",
                new CodelessApiPollingDataConnector()
                    .withConnectorUiConfig(
                        new CodelessUiConnectorConfigProperties()
                            .withTitle("GitHub Enterprise Audit Log")
                            .withPublisher("GitHub")
                            .withDescriptionMarkdown(
                                "The GitHub audit log connector provides the capability to ingest GitHub logs into"
                                    + " Azure Sentinel. By connecting GitHub audit logs into Azure Sentinel, you can"
                                    + " view this data in workbooks, use it to create custom alerts, and improve your"
                                    + " investigation process.")
                            .withGraphQueriesTableName("GitHubAuditLogPolling_CL")
                            .withGraphQueries(
                                Arrays
                                    .asList(
                                        new CodelessUiConnectorConfigPropertiesGraphQueriesItem()
                                            .withMetricName("Total events received")
                                            .withLegend("GitHub audit log events")
                                            .withBaseQuery("{{graphQueriesTableName}}")))
                            .withSampleQueries(
                                Arrays
                                    .asList(
                                        new CodelessUiConnectorConfigPropertiesSampleQueriesItem()
                                            .withDescription("All logs")
                                            .withQuery("{{graphQueriesTableName}}\n | take 10 <change>")))
                            .withDataTypes(
                                Arrays
                                    .asList(
                                        new CodelessUiConnectorConfigPropertiesDataTypesItem()
                                            .withName("{{graphQueriesTableName}}")
                                            .withLastDataReceivedQuery(
                                                "{{graphQueriesTableName}}\n"
                                                    + "            | summarize Time = max(TimeGenerated)\n"
                                                    + "            | where isnotempty(Time)")))
                            .withConnectivityCriteria(
                                Arrays
                                    .asList(
                                        new CodelessUiConnectorConfigPropertiesConnectivityCriteriaItem()
                                            .withType(ConnectivityType.fromString("SentinelKindsV2"))
                                            .withValue(Arrays.asList())))
                            .withAvailability(new Availability().withStatus(AvailabilityStatus.ONE).withIsPreview(true))
                            .withPermissions(
                                new Permissions()
                                    .withResourceProvider(
                                        Arrays
                                            .asList(
                                                new PermissionsResourceProviderItem()
                                                    .withProvider(
                                                        ProviderName.MICROSOFT_OPERATIONAL_INSIGHTS_WORKSPACES)
                                                    .withPermissionsDisplayText(
                                                        "read and write permissions are required.")
                                                    .withProviderDisplayName("Workspace")
                                                    .withScope(PermissionProviderScope.WORKSPACE)
                                                    .withRequiredPermissions(
                                                        new RequiredPermissions()
                                                            .withWrite(true)
                                                            .withRead(true)
                                                            .withDelete(true))))
                                    .withCustoms(
                                        Arrays
                                            .asList(
                                                new PermissionsCustomsItem()
                                                    .withName("GitHub API personal token Key")
                                                    .withDescription(
                                                        "You need access to GitHub personal token, the key should have"
                                                            + " 'admin:org' scope"))))
                            .withInstructionSteps(
                                Arrays
                                    .asList(
                                        new CodelessUiConnectorConfigPropertiesInstructionStepsItem()
                                            .withTitle("Connect GitHub Enterprise Audit Log to Azure Sentinel")
                                            .withDescription(
                                                "Enable GitHub audit Logs. \n"
                                                    + " Follow"
                                                    + " [this](https://docs.github.com/en/github/authenticating-to-github/keeping-your-account-and-data-secure/creating-a-personal-access-token)"
                                                    + " to create or find your personal key")
                                            .withInstructions(
                                                Arrays
                                                    .asList(
                                                        new InstructionStepsInstructionsItem()
                                                            .withParameters(
                                                                SerializerFactory
                                                                    .createDefaultManagementSerializerAdapter()
                                                                    .deserialize(
                                                                        "{\"enable\":\"true\",\"userRequestPlaceHoldersInput\":[{\"displayText\":\"Organization"
                                                                            + " Name\",\"placeHolderName\":\"{{placeHolder1}}\",\"placeHolderValue\":\"\",\"requestObjectKey\":\"apiEndpoint\"}]}",
                                                                        Object.class,
                                                                        SerializerEncoding.JSON))
                                                            .withType(SettingType.fromString("APIKey")))))))
                    .withPollingConfig(
                        new CodelessConnectorPollingConfigProperties()
                            .withAuth(
                                new CodelessConnectorPollingAuthProperties()
                                    .withAuthType("APIKey")
                                    .withApiKeyName("Authorization")
                                    .withApiKeyIdentifier("token"))
                            .withRequest(
                                new CodelessConnectorPollingRequestProperties()
                                    .withApiEndpoint("https://api.github.com/organizations/{{placeHolder1}}/audit-log")
                                    .withRateLimitQps(50)
                                    .withQueryWindowInMin(15)
                                    .withHttpMethod("Get")
                                    .withQueryTimeFormat("yyyy-MM-ddTHH:mm:ssZ")
                                    .withRetryCount(2)
                                    .withTimeoutInSeconds(60)
                                    .withHeaders(
                                        SerializerFactory
                                            .createDefaultManagementSerializerAdapter()
                                            .deserialize(
                                                "{\"Accept\":\"application/json\",\"User-Agent\":\"Scuba\"}",
                                                Object.class,
                                                SerializerEncoding.JSON))
                                    .withQueryParameters(
                                        SerializerFactory
                                            .createDefaultManagementSerializerAdapter()
                                            .deserialize(
                                                "{\"phrase\":\"created:{_QueryWindowStartTime}..{_QueryWindowEndTime}\"}",
                                                Object.class,
                                                SerializerEncoding.JSON)))
                            .withPaging(
                                new CodelessConnectorPollingPagingProperties()
                                    .withPagingType("LinkHeader")
                                    .withPageSizeParaName("per_page"))
                            .withResponse(
                                new CodelessConnectorPollingResponseProperties()
                                    .withEventsJsonPaths(Arrays.asList("$")))),
                Context.NONE);
    }
}
```

### DataConnectors_Delete

```java
import com.azure.core.util.Context;

/** Samples for DataConnectors Delete. */
public final class DataConnectorsDeleteSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/DeleteOffice365ProjectDataConnetor.json
     */
    /**
     * Sample code: Delete an Office365 Project data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteAnOffice365ProjectDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .deleteWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/DeleteGenericUI.json
     */
    /**
     * Sample code: Delete a GenericUI data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteAGenericUIDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .deleteWithResponse("myRg", "myWorkspace", "316ec55e-7138-4d63-ab18-90c8a60fd1c8", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/DeleteOfficeDataConnetor.json
     */
    /**
     * Sample code: Delete an Office365 data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteAnOffice365DataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .deleteWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/DeleteOfficePowerBIDataConnetor.json
     */
    /**
     * Sample code: Delete an Office PowerBI data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteAnOfficePowerBIDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .deleteWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/DeleteAPIPolling.json
     */
    /**
     * Sample code: Delete a APIPolling data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteAAPIPollingDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .deleteWithResponse("myRg", "myWorkspace", "316ec55e-7138-4d63-ab18-90c8a60fd1c8", Context.NONE);
    }
}
```

### DataConnectors_Disconnect

```java
import com.azure.core.util.Context;

/** Samples for DataConnectors Disconnect. */
public final class DataConnectorsDisconnectSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/DisconnectAPIPolling.json
     */
    /**
     * Sample code: Disconnect an APIPolling data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void disconnectAnAPIPollingDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .disconnectWithResponse("myRg", "myWorkspace", "316ec55e-7138-4d63-ab18-90c8a60fd1c8", Context.NONE);
    }
}
```

### DataConnectors_Get

```java
import com.azure.core.util.Context;

/** Samples for DataConnectors Get. */
public final class DataConnectorsGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/GetOffice365ProjectDataConnetorById.json
     */
    /**
     * Sample code: Get an Office365 Project data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAnOffice365ProjectDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/GetGenericUI.json
     */
    /**
     * Sample code: Get a GenericUI data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAGenericUIDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "316ec55e-7138-4d63-ab18-90c8a60fd1c8", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/GetOffice365AdvancedThreatProtectionById.json
     */
    /**
     * Sample code: Get an Office ATP data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAnOfficeATPDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "3d3e955e-33eb-401d-89a7-251c81ddd660", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/GetIoTById.json
     */
    /**
     * Sample code: Get a IoT data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAIoTDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "d2e5dc7a-f3a2-429d-954b-939fa8c2932e", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/GetMicrosoftCloudAppSecurityById.json
     */
    /**
     * Sample code: Get a MCAS data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAMCASDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "b96d014d-b5c2-4a01-9aba-a8058f629d42", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/GetThreatIntelligenceTaxiiById.json
     */
    /**
     * Sample code: Get a TI Taxii data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getATITaxiiDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "c39bb458-02a7-4b3f-b0c8-71a1d2692652", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/GetMicrosoftThreatIntelligenceById.json
     */
    /**
     * Sample code: Get a MicrosoftThreatIntelligence data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAMicrosoftThreatIntelligenceDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "c345bf40-8509-4ed2-b947-50cb773aaf04", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/GetAzureSecurityCenterById.json
     */
    /**
     * Sample code: Get a ASC data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAASCDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "763f9fa1-c2d3-4fa2-93e9-bccd4899aa12", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/GetThreatIntelligenceById.json
     */
    /**
     * Sample code: Get a TI data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getATIDataConnector(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "c345bf40-8509-4ed2-b947-50cb773aaf04", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/GetAmazonWebServicesCloudTrailById.json
     */
    /**
     * Sample code: Get an AwsCloudTrail data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAnAwsCloudTrailDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "c345bf40-8509-4ed2-b947-50cb773aaf04", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/GetMicrosoftDefenderAdvancedThreatProtectionById.json
     */
    /**
     * Sample code: Get a MDATP data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAMDATPDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "06b3ccb8-1384-4bcc-aec7-852f6d57161b", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/GetMicrosoftThreatProtectionById.json
     */
    /**
     * Sample code: Get a MicrosoftThreatProtection data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAMicrosoftThreatProtectionDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "c345bf40-8509-4ed2-b947-50cb773aaf04", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/GetAzureActiveDirectoryById.json
     */
    /**
     * Sample code: Get an AAD data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAnAADDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "f0cd27d2-5f03-4c06-ba31-d2dc82dcb51d", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/GetAzureAdvancedThreatProtectionById.json
     */
    /**
     * Sample code: Get an AATP data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAnAATPDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "07e42cb3-e658-4e90-801c-efa0f29d3d44", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/GetAPIPolling.json
     */
    /**
     * Sample code: Get a APIPolling data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAAPIPollingDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "316ec55e-7138-4d63-ab18-90c8a60fd1c8", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/GetMicrosoftInsiderRiskManagementById.json
     */
    /**
     * Sample code: Get an Office IRM data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAnOfficeIRMDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "3d3e955e-33eb-401d-89a7-251c81ddd660", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/GetDynamics365DataConnectorById.json
     */
    /**
     * Sample code: Get a Dynamics365 data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getADynamics365DataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "c2541efb-c9a6-47fe-9501-87d1017d1512", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/GetOfficeDataConnetorById.json
     */
    /**
     * Sample code: Get an Office365 data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAnOffice365DataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/GetAmazonWebServicesS3ById.json
     */
    /**
     * Sample code: Get an Aws S3 data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAnAwsS3DataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "afef3743-0c88-469c-84ff-ca2e87dc1e48", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/GetOfficePowerBIDataConnetorById.json
     */
    /**
     * Sample code: Get an Office365 PowerBI data connector.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAnOffice365PowerBIDataConnector(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectors()
            .getWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5", Context.NONE);
    }
}
```

### DataConnectors_List

```java
import com.azure.core.util.Context;

/** Samples for DataConnectors List. */
public final class DataConnectorsListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/GetDataConnectors.json
     */
    /**
     * Sample code: Get all data connectors.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllDataConnectors(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.dataConnectors().list("myRg", "myWorkspace", Context.NONE);
    }
}
```

### DataConnectorsCheckRequirementsOperation_Post

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securityinsights.models.AadCheckRequirements;
import com.azure.resourcemanager.securityinsights.models.AscCheckRequirements;
import com.azure.resourcemanager.securityinsights.models.Dynamics365CheckRequirements;
import com.azure.resourcemanager.securityinsights.models.IoTCheckRequirements;
import com.azure.resourcemanager.securityinsights.models.McasCheckRequirements;
import com.azure.resourcemanager.securityinsights.models.MstiCheckRequirements;
import com.azure.resourcemanager.securityinsights.models.MtpCheckRequirements;
import com.azure.resourcemanager.securityinsights.models.Office365ProjectCheckRequirements;
import com.azure.resourcemanager.securityinsights.models.OfficeAtpCheckRequirements;
import com.azure.resourcemanager.securityinsights.models.OfficeIrmCheckRequirements;
import com.azure.resourcemanager.securityinsights.models.OfficePowerBICheckRequirements;
import com.azure.resourcemanager.securityinsights.models.TICheckRequirements;
import com.azure.resourcemanager.securityinsights.models.TiTaxiiCheckRequirements;

/** Samples for DataConnectorsCheckRequirementsOperation Post. */
public final class DataConnectorsCheckRequirementsOperationPostSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/CheckRequirementsOfficeATP.json
     */
    /**
     * Sample code: Check requirements for OfficeATP.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void checkRequirementsForOfficeATP(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectorsCheckRequirementsOperations()
            .postWithResponse("myRg", "myWorkspace", new OfficeAtpCheckRequirements(), Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/CheckRequirementsOffice365Project.json
     */
    /**
     * Sample code: Check requirements for Office365Project.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void checkRequirementsForOffice365Project(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectorsCheckRequirementsOperations()
            .postWithResponse("myRg", "myWorkspace", new Office365ProjectCheckRequirements(), Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/CheckRequirementsAzureActiveDirectory.json
     */
    /**
     * Sample code: Check requirements for AAD.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void checkRequirementsForAAD(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectorsCheckRequirementsOperations()
            .postWithResponse("myRg", "myWorkspace", new AadCheckRequirements(), Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/CheckRequirementsMicrosoftCloudAppSecurity.json
     */
    /**
     * Sample code: Check requirements for Mcas.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void checkRequirementsForMcas(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectorsCheckRequirementsOperations()
            .postWithResponse("myRg", "myWorkspace", new McasCheckRequirements(), Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/CheckRequirementsIoT.json
     */
    /**
     * Sample code: Check requirements for IoT.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void checkRequirementsForIoT(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectorsCheckRequirementsOperations()
            .postWithResponse(
                "myRg",
                "myWorkspace",
                new IoTCheckRequirements().withSubscriptionId("c0688291-89d7-4bed-87a2-a7b1bff43f4c"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/CheckRequirementsMdatp.json
     */
    /**
     * Sample code: Check requirements for Mdatp.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void checkRequirementsForMdatp(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectorsCheckRequirementsOperations()
            .postWithResponse("myRg", "myWorkspace", new McasCheckRequirements(), Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/CheckRequirementsAzureActiveDirectoryNoAuthorization.json
     */
    /**
     * Sample code: Check requirements for AAD - no authorization.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void checkRequirementsForAADNoAuthorization(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectorsCheckRequirementsOperations()
            .postWithResponse("myRg", "myWorkspace", new AadCheckRequirements(), Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/CheckRequirementsAzureSecurityCenter.json
     */
    /**
     * Sample code: Check requirements for ASC.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void checkRequirementsForASC(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectorsCheckRequirementsOperations()
            .postWithResponse(
                "myRg",
                "myWorkspace",
                new AscCheckRequirements().withSubscriptionId("c0688291-89d7-4bed-87a2-a7b1bff43f4c"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/CheckRequirementsMicrosoftThreatProtection.json
     */
    /**
     * Sample code: Check requirements for MicrosoftThreatProtection.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void checkRequirementsForMicrosoftThreatProtection(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectorsCheckRequirementsOperations()
            .postWithResponse("myRg", "myWorkspace", new MtpCheckRequirements(), Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/CheckRequirementsThreatIntelligenceTaxii.json
     */
    /**
     * Sample code: Check requirements for TI Taxii.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void checkRequirementsForTITaxii(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectorsCheckRequirementsOperations()
            .postWithResponse("myRg", "myWorkspace", new TiTaxiiCheckRequirements(), Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/CheckRequirementsOfficeIRM.json
     */
    /**
     * Sample code: Check requirements for OfficeIRM.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void checkRequirementsForOfficeIRM(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectorsCheckRequirementsOperations()
            .postWithResponse("myRg", "myWorkspace", new OfficeIrmCheckRequirements(), Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/CheckRequirementsMicrosoftThreatIntelligence.json
     */
    /**
     * Sample code: Check requirements for MicrosoftThreatIntelligence.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void checkRequirementsForMicrosoftThreatIntelligence(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectorsCheckRequirementsOperations()
            .postWithResponse("myRg", "myWorkspace", new MstiCheckRequirements(), Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/CheckRequirementsOfficePowerBI.json
     */
    /**
     * Sample code: Check requirements for OfficePowerBI.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void checkRequirementsForOfficePowerBI(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectorsCheckRequirementsOperations()
            .postWithResponse("myRg", "myWorkspace", new OfficePowerBICheckRequirements(), Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/CheckRequirementsDynamics365.json
     */
    /**
     * Sample code: Check requirements for Dynamics365.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void checkRequirementsForDynamics365(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectorsCheckRequirementsOperations()
            .postWithResponse("myRg", "myWorkspace", new Dynamics365CheckRequirements(), Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/CheckRequirementsAzureActiveDirectoryNoLicense.json
     */
    /**
     * Sample code: Check requirements for AAD - no license.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void checkRequirementsForAADNoLicense(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectorsCheckRequirementsOperations()
            .postWithResponse("myRg", "myWorkspace", new AadCheckRequirements(), Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/dataConnectors/CheckRequirementsThreatIntelligence.json
     */
    /**
     * Sample code: Check requirements for TI.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void checkRequirementsForTI(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .dataConnectorsCheckRequirementsOperations()
            .postWithResponse("myRg", "myWorkspace", new TICheckRequirements(), Context.NONE);
    }
}
```

### DomainWhois_Get

```java
import com.azure.core.util.Context;

/** Samples for DomainWhois Get. */
public final class DomainWhoisGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/enrichment/GetWhoisByDomainName.json
     */
    /**
     * Sample code: Get whois information for a single domain name.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getWhoisInformationForASingleDomainName(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.domainWhois().getWithResponse("myRg", "microsoft.com", Context.NONE);
    }
}
```

### Entities_Expand

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securityinsights.models.EntityExpandParameters;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Samples for Entities Expand. */
public final class EntitiesExpandSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/expand/PostExpandEntity.json
     */
    /**
     * Sample code: Expand an entity.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void expandAnEntity(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .entities()
            .expandWithResponse(
                "myRg",
                "myWorkspace",
                "e1d3d618-e11f-478b-98e3-bb381539a8e1",
                new EntityExpandParameters()
                    .withEndTime(OffsetDateTime.parse("2019-05-26T00:00:00.000Z"))
                    .withExpansionId(UUID.fromString("a77992f3-25e9-4d01-99a4-5ff606cc410a"))
                    .withStartTime(OffsetDateTime.parse("2019-04-25T00:00:00.000Z")),
                Context.NONE);
    }
}
```

### Entities_Get

```java
import com.azure.core.util.Context;

/** Samples for Entities Get. */
public final class EntitiesGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/GetSubmissionMailEntityById.json
     */
    /**
     * Sample code: Get a submissionMail entity.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getASubmissionMailEntity(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.entities().getWithResponse("myRg", "myWorkspace", "e1d3d618-e11f-478b-98e3-bb381539a8e1", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/GetDnsEntityById.json
     */
    /**
     * Sample code: Get a dns entity.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getADnsEntity(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.entities().getWithResponse("myRg", "myWorkspace", "f4e74920-f2c0-4412-a45f-66d94fdf01f8", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/GetFileHashEntityById.json
     */
    /**
     * Sample code: Get a file hash entity.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAFileHashEntity(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.entities().getWithResponse("myRg", "myWorkspace", "ea359fa6-c1e5-f878-e105-6344f3e399a1", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/GetIoTDeviceEntityById.json
     */
    /**
     * Sample code: Get an IoT device entity.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAnIoTDeviceEntity(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.entities().getWithResponse("myRg", "myWorkspace", "e1d3d618-e11f-478b-98e3-bb381539a8e1", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/GetRegistryKeyEntityById.json
     */
    /**
     * Sample code: Get a registry key entity.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getARegistryKeyEntity(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.entities().getWithResponse("myRg", "myWorkspace", "e1d3d618-e11f-478b-98e3-bb381539a8e1", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/GetMailboxEntityById.json
     */
    /**
     * Sample code: Get a mailbox entity.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAMailboxEntity(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.entities().getWithResponse("myRg", "myWorkspace", "e1d3d618-e11f-478b-98e3-bb381539a8e1", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/GetMalwareEntityById.json
     */
    /**
     * Sample code: Get a malware entity.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAMalwareEntity(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.entities().getWithResponse("myRg", "myWorkspace", "af378b21-b4aa-4fe7-bc70-13f8621a322f", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/GetSecurityAlertEntityById.json
     */
    /**
     * Sample code: Get a security alert entity.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getASecurityAlertEntity(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.entities().getWithResponse("myRg", "myWorkspace", "4aa486e0-6f85-41af-99ea-7acdce7be6c8", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/GetIpEntityById.json
     */
    /**
     * Sample code: Get an ip entity.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAnIpEntity(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.entities().getWithResponse("myRg", "myWorkspace", "e1d3d618-e11f-478b-98e3-bb381539a8e1", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/GetAccountEntityById.json
     */
    /**
     * Sample code: Get an account entity.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAnAccountEntity(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.entities().getWithResponse("myRg", "myWorkspace", "e1d3d618-e11f-478b-98e3-bb381539a8e1", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/GetCloudApplicationEntityById.json
     */
    /**
     * Sample code: Get a cloud application entity.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getACloudApplicationEntity(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.entities().getWithResponse("myRg", "myWorkspace", "e1d3d618-e11f-478b-98e3-bb381539a8e1", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/GetFileEntityById.json
     */
    /**
     * Sample code: Get a file entity.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAFileEntity(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.entities().getWithResponse("myRg", "myWorkspace", "af378b21-b4aa-4fe7-bc70-13f8621a322f", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/GetRegistryValueEntityById.json
     */
    /**
     * Sample code: Get a registry value entity.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getARegistryValueEntity(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.entities().getWithResponse("myRg", "myWorkspace", "dc44bd11-b348-4d76-ad29-37bf7aa41356", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/GetUrlEntityById.json
     */
    /**
     * Sample code: Get a url entity.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAUrlEntity(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.entities().getWithResponse("myRg", "myWorkspace", "e1d3d618-e11f-478b-98e3-bb381539a8e1", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/GetMailClusterEntityById.json
     */
    /**
     * Sample code: Get a mailCluster entity.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAMailClusterEntity(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.entities().getWithResponse("myRg", "myWorkspace", "e1d3d618-e11f-478b-98e3-bb381539a8e1", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/GetSecurityGroupEntityById.json
     */
    /**
     * Sample code: Get a security group entity.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getASecurityGroupEntity(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.entities().getWithResponse("myRg", "myWorkspace", "e1d3d618-e11f-478b-98e3-bb381539a8e1", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/GetMailMessageEntityById.json
     */
    /**
     * Sample code: Get a mailMessage entity.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAMailMessageEntity(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.entities().getWithResponse("myRg", "myWorkspace", "e1d3d618-e11f-478b-98e3-bb381539a8e1", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/GetAzureResourceEntityById.json
     */
    /**
     * Sample code: Get an azure resource entity.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAnAzureResourceEntity(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.entities().getWithResponse("myRg", "myWorkspace", "e1d3d618-e11f-478b-98e3-bb381539a8e1", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/GetHostEntityById.json
     */
    /**
     * Sample code: Get a host entity.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAHostEntity(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.entities().getWithResponse("myRg", "myWorkspace", "e1d3d618-e11f-478b-98e3-bb381539a8e1", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/GetProcessEntityById.json
     */
    /**
     * Sample code: Get a process entity.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAProcessEntity(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.entities().getWithResponse("myRg", "myWorkspace", "7264685c-038c-42c6-948c-38e14ef1fb98", Context.NONE);
    }
}
```

### Entities_GetInsights

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securityinsights.models.EntityGetInsightsParameters;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;

/** Samples for Entities GetInsights. */
public final class EntitiesGetInsightsSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/insights/PostGetInsights.json
     */
    /**
     * Sample code: Entity Insight.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void entityInsight(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .entities()
            .getInsightsWithResponse(
                "myRg",
                "myWorkspace",
                "e1d3d618-e11f-478b-98e3-bb381539a8e1",
                new EntityGetInsightsParameters()
                    .withStartTime(OffsetDateTime.parse("2021-09-01T00:00:00.000Z"))
                    .withEndTime(OffsetDateTime.parse("2021-10-01T00:00:00.000Z"))
                    .withAddDefaultExtendedTimeRange(false)
                    .withInsightQueryIds(Arrays.asList(UUID.fromString("cae8d0aa-aa45-4d53-8d88-17dd64ffd4e4"))),
                Context.NONE);
    }
}
```

### Entities_List

```java
import com.azure.core.util.Context;

/** Samples for Entities List. */
public final class EntitiesListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/GetEntities.json
     */
    /**
     * Sample code: Get all entities.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllEntities(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.entities().list("myRg", "myWorkspace", Context.NONE);
    }
}
```

### Entities_Queries

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securityinsights.models.EntityItemQueryKind;

/** Samples for Entities Queries. */
public final class EntitiesQueriesSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/GetQueries.json
     */
    /**
     * Sample code: Get Entity Query.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getEntityQuery(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .entities()
            .queriesWithResponse(
                "myRg",
                "myWorkspace",
                "e1d3d618-e11f-478b-98e3-bb381539a8e1",
                EntityItemQueryKind.INSIGHT,
                Context.NONE);
    }
}
```

### EntitiesGetTimeline_List

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securityinsights.models.EntityTimelineParameters;
import java.time.OffsetDateTime;

/** Samples for EntitiesGetTimeline List. */
public final class EntitiesGetTimelineListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/timeline/PostTimelineEntity.json
     */
    /**
     * Sample code: Entity timeline.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void entityTimeline(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .entitiesGetTimelines()
            .listWithResponse(
                "myRg",
                "myWorkspace",
                "e1d3d618-e11f-478b-98e3-bb381539a8e1",
                new EntityTimelineParameters()
                    .withStartTime(OffsetDateTime.parse("2021-09-01T00:00:00.000Z"))
                    .withEndTime(OffsetDateTime.parse("2021-10-01T00:00:00.000Z"))
                    .withNumberOfBucket(4),
                Context.NONE);
    }
}
```

### EntitiesRelations_List

```java
import com.azure.core.util.Context;

/** Samples for EntitiesRelations List. */
public final class EntitiesRelationsListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/relations/GetAllEntityRelations.json
     */
    /**
     * Sample code: Get all relations of an entity.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllRelationsOfAnEntity(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .entitiesRelations()
            .list("myRg", "myWorkspace", "afbd324f-6c48-459c-8710-8d1e1cd03812", null, null, null, null, Context.NONE);
    }
}
```

### EntityQueries_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securityinsights.models.ActivityCustomEntityQuery;
import com.azure.resourcemanager.securityinsights.models.ActivityEntityQueriesPropertiesQueryDefinitions;
import com.azure.resourcemanager.securityinsights.models.EntityType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for EntityQueries CreateOrUpdate. */
public final class EntityQueriesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entityQueries/CreateEntityQueryActivity.json
     */
    /**
     * Sample code: Creates or updates an Activity entity query.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAnActivityEntityQuery(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .entityQueries()
            .createOrUpdateWithResponse(
                "myRg",
                "myWorkspace",
                "07da3cc8-c8ad-4710-a44e-334cdcb7882b",
                new ActivityCustomEntityQuery()
                    .withEtag("\"0300bf09-0000-0000-0000-5c37296e0000\"")
                    .withTitle("An account was deleted on this host")
                    .withContent("On '{{Computer}}' the account '{{TargetAccount}}' was deleted by '{{AddedBy}}'")
                    .withDescription("Account deleted on host")
                    .withQueryDefinitions(
                        new ActivityEntityQueriesPropertiesQueryDefinitions()
                            .withQuery(
                                "let GetAccountActions = (v_Host_Name:string, v_Host_NTDomain:string,"
                                    + " v_Host_DnsDomain:string, v_Host_AzureID:string, v_Host_OMSAgentID:string){\n"
                                    + "SecurityEvent\n"
                                    + "| where EventID in (4725, 4726, 4767, 4720, 4722, 4723, 4724)\n"
                                    + "// parsing for Host to handle variety of conventions coming from data\n"
                                    + "| extend Host_HostName = case(\n"
                                    + "Computer has '@', tostring(split(Computer, '@')[0]),\n"
                                    + "Computer has '\\\\', tostring(split(Computer, '\\\\')[1]),\n"
                                    + "Computer has '.', tostring(split(Computer, '.')[0]),\n"
                                    + "Computer\n"
                                    + ")\n"
                                    + "| extend Host_NTDomain = case(\n"
                                    + "Computer has '\\\\', tostring(split(Computer, '\\\\')[0]), \n"
                                    + "Computer has '.', tostring(split(Computer, '.')[-2]), \n"
                                    + "Computer\n"
                                    + ")\n"
                                    + "| extend Host_DnsDomain = case(\n"
                                    + "Computer has '\\\\', tostring(split(Computer, '\\\\')[0]), \n"
                                    + "Computer has '.', strcat_array(array_slice(split(Computer,'.'),-2,-1),'.'), \n"
                                    + "Computer\n"
                                    + ")\n"
                                    + "| where (Host_HostName =~ v_Host_Name and Host_NTDomain =~ v_Host_NTDomain) \n"
                                    + "or (Host_HostName =~ v_Host_Name and Host_DnsDomain =~ v_Host_DnsDomain) \n"
                                    + "or v_Host_AzureID =~ _ResourceId \n"
                                    + "or v_Host_OMSAgentID == SourceComputerId\n"
                                    + "| project TimeGenerated, EventID, Activity, Computer, TargetAccount,"
                                    + " TargetUserName, TargetDomainName, TargetSid, SubjectUserName, SubjectUserSid,"
                                    + " _ResourceId, SourceComputerId\n"
                                    + "| extend AddedBy = SubjectUserName\n"
                                    + "// Future support for Activities\n"
                                    + "| extend timestamp = TimeGenerated, HostCustomEntity = Computer,"
                                    + " AccountCustomEntity = TargetAccount\n"
                                    + "};\n"
                                    + "GetAccountActions('{{Host_HostName}}', '{{Host_NTDomain}}',"
                                    + " '{{Host_DnsDomain}}', '{{Host_AzureID}}', '{{Host_OMSAgentID}}')\n"
                                    + " \n"
                                    + "| where EventID == 4726 "))
                    .withInputEntityType(EntityType.HOST)
                    .withRequiredInputFieldsSets(
                        Arrays
                            .asList(
                                Arrays.asList("Host_HostName", "Host_NTDomain"),
                                Arrays.asList("Host_HostName", "Host_DnsDomain"),
                                Arrays.asList("Host_AzureID"),
                                Arrays.asList("Host_OMSAgentID")))
                    .withEntitiesFilter(mapOf("Host_OsFamily", Arrays.asList("Windows")))
                    .withEnabled(true),
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

### EntityQueries_Delete

```java
import com.azure.core.util.Context;

/** Samples for EntityQueries Delete. */
public final class EntityQueriesDeleteSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entityQueries/DeleteEntityQuery.json
     */
    /**
     * Sample code: Delete an entity query.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteAnEntityQuery(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .entityQueries()
            .deleteWithResponse("myRg", "myWorkspace", "07da3cc8-c8ad-4710-a44e-334cdcb7882b", Context.NONE);
    }
}
```

### EntityQueries_Get

```java
import com.azure.core.util.Context;

/** Samples for EntityQueries Get. */
public final class EntityQueriesGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entityQueries/GetActivityEntityQueryById.json
     */
    /**
     * Sample code: Get an Activity entity query.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAnActivityEntityQuery(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .entityQueries()
            .getWithResponse("myRg", "myWorkspace", "07da3cc8-c8ad-4710-a44e-334cdcb7882b", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entityQueries/GetExpansionEntityQueryById.json
     */
    /**
     * Sample code: Get an Expansion entity query.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAnExpansionEntityQuery(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .entityQueries()
            .getWithResponse("myRg", "myWorkspace", "07da3cc8-c8ad-4710-a44e-334cdcb7882b", Context.NONE);
    }
}
```

### EntityQueries_List

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securityinsights.models.EntityQueriesKind;

/** Samples for EntityQueries List. */
public final class EntityQueriesListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entityQueries/GetEntityQueries.json
     */
    /**
     * Sample code: Get all entity queries.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllEntityQueries(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.entityQueries().list("myRg", "myWorkspace", EntityQueriesKind.EXPANSION, Context.NONE);
    }
}
```

### EntityQueryTemplates_Get

```java
import com.azure.core.util.Context;

/** Samples for EntityQueryTemplates Get. */
public final class EntityQueryTemplatesGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entityQueryTemplates/GetActivityEntityQueryTemplateById.json
     */
    /**
     * Sample code: Get an Activity entity query template.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAnActivityEntityQueryTemplate(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .entityQueryTemplates()
            .getWithResponse("myRg", "myWorkspace", "07da3cc8-c8ad-4710-a44e-334cdcb7882b", Context.NONE);
    }
}
```

### EntityQueryTemplates_List

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securityinsights.models.Constant88;

/** Samples for EntityQueryTemplates List. */
public final class EntityQueryTemplatesListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entityQueryTemplates/GetEntityQueryTemplates.json
     */
    /**
     * Sample code: Get all entity query templates.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllEntityQueryTemplates(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.entityQueryTemplates().list("myRg", "myWorkspace", Constant88.ACTIVITY, Context.NONE);
    }
}
```

### EntityRelations_GetRelation

```java
import com.azure.core.util.Context;

/** Samples for EntityRelations GetRelation. */
public final class EntityRelationsGetRelationSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/entities/relations/GetEntityRelationByName.json
     */
    /**
     * Sample code: Get an entity relation.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAnEntityRelation(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .entityRelations()
            .getRelationWithResponse(
                "myRg",
                "myWorkspace",
                "afbd324f-6c48-459c-8710-8d1e1cd03812",
                "4bb36b7b-26ff-4d1c-9cbe-0d8ab3da0014",
                Context.NONE);
    }
}
```

### FileImports_Create

```java
import com.azure.resourcemanager.securityinsights.models.FileFormat;
import com.azure.resourcemanager.securityinsights.models.FileImportContentType;
import com.azure.resourcemanager.securityinsights.models.FileMetadata;
import com.azure.resourcemanager.securityinsights.models.IngestionMode;

/** Samples for FileImports Create. */
public final class FileImportsCreateSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/fileImports/CreateFileImport.json
     */
    /**
     * Sample code: Create a file import.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createAFileImport(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .fileImports()
            .define("73e01a99-5cd7-4139-a149-9f2736ff2ab5")
            .withExistingWorkspace("myRg", "myWorkspace")
            .withIngestionMode(IngestionMode.INGEST_ANY_VALID_RECORDS)
            .withContentType(FileImportContentType.STIX_INDICATOR)
            .withImportFile(
                new FileMetadata().withFileFormat(FileFormat.JSON).withFileName("myFile.json").withFileSize(4653))
            .withSource("mySource")
            .create();
    }
}
```

### FileImports_Delete

```java
import com.azure.core.util.Context;

/** Samples for FileImports Delete. */
public final class FileImportsDeleteSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/fileImports/DeleteFileImport.json
     */
    /**
     * Sample code: Delete a file import.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteAFileImport(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.fileImports().delete("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5", Context.NONE);
    }
}
```

### FileImports_Get

```java
import com.azure.core.util.Context;

/** Samples for FileImports Get. */
public final class FileImportsGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/fileImports/GetFileImportById.json
     */
    /**
     * Sample code: Get a file import.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAFileImport(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .fileImports()
            .getWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5", Context.NONE);
    }
}
```

### FileImports_List

```java
import com.azure.core.util.Context;

/** Samples for FileImports List. */
public final class FileImportsListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/fileImports/GetFileImports.json
     */
    /**
     * Sample code: Get all file imports.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllFileImports(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .fileImports()
            .list("myRg", "myWorkspace", null, "properties/createdTimeUtc desc", 1, null, Context.NONE);
    }
}
```

### IncidentComments_CreateOrUpdate

```java
/** Samples for IncidentComments CreateOrUpdate. */
public final class IncidentCommentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/incidents/comments/CreateIncidentComment.json
     */
    /**
     * Sample code: Creates or updates an incident comment.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAnIncidentComment(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .incidentComments()
            .define("4bb36b7b-26ff-4d1c-9cbe-0d8ab3da0014")
            .withExistingIncident("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5")
            .withMessage("Some message")
            .create();
    }
}
```

### IncidentComments_Delete

```java
import com.azure.core.util.Context;

/** Samples for IncidentComments Delete. */
public final class IncidentCommentsDeleteSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/incidents/comments/DeleteIncidentComment.json
     */
    /**
     * Sample code: Delete the incident comment.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteTheIncidentComment(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .incidentComments()
            .deleteWithResponse(
                "myRg",
                "myWorkspace",
                "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                "4bb36b7b-26ff-4d1c-9cbe-0d8ab3da0014",
                Context.NONE);
    }
}
```

### IncidentComments_Get

```java
import com.azure.core.util.Context;

/** Samples for IncidentComments Get. */
public final class IncidentCommentsGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/incidents/comments/GetIncidentCommentById.json
     */
    /**
     * Sample code: Get an incident comment.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAnIncidentComment(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .incidentComments()
            .getWithResponse(
                "myRg",
                "myWorkspace",
                "73e01a99-5cd7-4139-a149-9f2736ff2ab5",
                "4bb36b7b-26ff-4d1c-9cbe-0d8ab3da0014",
                Context.NONE);
    }
}
```

### IncidentComments_List

```java
import com.azure.core.util.Context;

/** Samples for IncidentComments List. */
public final class IncidentCommentsListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/incidents/comments/GetAllIncidentComments.json
     */
    /**
     * Sample code: Get all incident comments.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllIncidentComments(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .incidentComments()
            .list("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5", null, null, null, null, Context.NONE);
    }
}
```

### IncidentRelations_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securityinsights.fluent.models.RelationInner;

/** Samples for IncidentRelations CreateOrUpdate. */
public final class IncidentRelationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/incidents/relations/CreateIncidentRelation.json
     */
    /**
     * Sample code: Creates or updates an incident relation.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAnIncidentRelation(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .incidentRelations()
            .createOrUpdateWithResponse(
                "myRg",
                "myWorkspace",
                "afbd324f-6c48-459c-8710-8d1e1cd03812",
                "4bb36b7b-26ff-4d1c-9cbe-0d8ab3da0014",
                new RelationInner()
                    .withRelatedResourceId(
                        "/subscriptions/d0cfe6b2-9ac0-4464-9919-dccaee2e48c0/resourceGroups/myRg/providers/Microsoft.OperationalIinsights/workspaces/myWorkspace/providers/Microsoft.SecurityInsights/bookmarks/2216d0e1-91e3-4902-89fd-d2df8c535096"),
                Context.NONE);
    }
}
```

### IncidentRelations_Delete

```java
import com.azure.core.util.Context;

/** Samples for IncidentRelations Delete. */
public final class IncidentRelationsDeleteSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/incidents/relations/DeleteIncidentRelation.json
     */
    /**
     * Sample code: Delete the incident relation.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteTheIncidentRelation(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .incidentRelations()
            .deleteWithResponse(
                "myRg",
                "myWorkspace",
                "afbd324f-6c48-459c-8710-8d1e1cd03812",
                "4bb36b7b-26ff-4d1c-9cbe-0d8ab3da0014",
                Context.NONE);
    }
}
```

### IncidentRelations_Get

```java
import com.azure.core.util.Context;

/** Samples for IncidentRelations Get. */
public final class IncidentRelationsGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/incidents/relations/GetIncidentRelationByName.json
     */
    /**
     * Sample code: Get an incident relation.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAnIncidentRelation(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .incidentRelations()
            .getWithResponse(
                "myRg",
                "myWorkspace",
                "afbd324f-6c48-459c-8710-8d1e1cd03812",
                "4bb36b7b-26ff-4d1c-9cbe-0d8ab3da0014",
                Context.NONE);
    }
}
```

### IncidentRelations_List

```java
import com.azure.core.util.Context;

/** Samples for IncidentRelations List. */
public final class IncidentRelationsListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/incidents/relations/GetAllIncidentRelations.json
     */
    /**
     * Sample code: Get all incident relations.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllIncidentRelations(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .incidentRelations()
            .list("myRg", "myWorkspace", "afbd324f-6c48-459c-8710-8d1e1cd03812", null, null, null, null, Context.NONE);
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

/** Samples for Incidents CreateOrUpdate. */
public final class IncidentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/incidents/CreateIncident.json
     */
    /**
     * Sample code: Creates or updates an incident.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAnIncident(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .incidents()
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

### Incidents_CreateTeam

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securityinsights.models.TeamProperties;

/** Samples for Incidents CreateTeam. */
public final class IncidentsCreateTeamSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/incidents/CreateTeam.json
     */
    /**
     * Sample code: Creates incident teams group.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsIncidentTeamsGroup(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .incidents()
            .createTeamWithResponse(
                "ambawolvese5resourcegroup",
                "AmbaE5WestCentralUS",
                "69a30280-6a4c-4aa7-9af0-5d63f335d600",
                new TeamProperties().withTeamName("Team name").withTeamDescription("Team description"),
                Context.NONE);
    }
}
```

### Incidents_Delete

```java
import com.azure.core.util.Context;

/** Samples for Incidents Delete. */
public final class IncidentsDeleteSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/incidents/DeleteIncident.json
     */
    /**
     * Sample code: Delete an incident.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteAnIncident(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .incidents()
            .deleteWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5", Context.NONE);
    }
}
```

### Incidents_Get

```java
import com.azure.core.util.Context;

/** Samples for Incidents Get. */
public final class IncidentsGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/incidents/GetIncidentById.json
     */
    /**
     * Sample code: Get an incident.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAnIncident(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .incidents()
            .getWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ab5", Context.NONE);
    }
}
```

### Incidents_List

```java
import com.azure.core.util.Context;

/** Samples for Incidents List. */
public final class IncidentsListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/incidents/GetIncidents.json
     */
    /**
     * Sample code: Get all incidents.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllIncidents(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.incidents().list("myRg", "myWorkspace", null, "properties/createdTimeUtc desc", 1, null, Context.NONE);
    }
}
```

### Incidents_ListAlerts

```java
import com.azure.core.util.Context;

/** Samples for Incidents ListAlerts. */
public final class IncidentsListAlertsSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/incidents/GetAllIncidentAlerts.json
     */
    /**
     * Sample code: Get all incident alerts.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllIncidentAlerts(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .incidents()
            .listAlertsWithResponse("myRg", "myWorkspace", "afbd324f-6c48-459c-8710-8d1e1cd03812", Context.NONE);
    }
}
```

### Incidents_ListBookmarks

```java
import com.azure.core.util.Context;

/** Samples for Incidents ListBookmarks. */
public final class IncidentsListBookmarksSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/incidents/GetAllIncidentBookmarks.json
     */
    /**
     * Sample code: Get all incident bookmarks.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllIncidentBookmarks(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .incidents()
            .listBookmarksWithResponse("myRg", "myWorkspace", "afbd324f-6c48-459c-8710-8d1e1cd03812", Context.NONE);
    }
}
```

### Incidents_ListEntities

```java
import com.azure.core.util.Context;

/** Samples for Incidents ListEntities. */
public final class IncidentsListEntitiesSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/incidents/entities/GetAllIncidentEntities.json
     */
    /**
     * Sample code: Gets all incident related entities.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getsAllIncidentRelatedEntities(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .incidents()
            .listEntitiesWithResponse("myRg", "myWorkspace", "afbd324f-6c48-459c-8710-8d1e1cd03812", Context.NONE);
    }
}
```

### Incidents_RunPlaybook

```java
import com.azure.core.util.Context;

/** Samples for Incidents RunPlaybook. */
public final class IncidentsRunPlaybookSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/manualTrigger/Incidents_RunPlaybook.json
     */
    /**
     * Sample code: Incidents_RunPlaybook.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void incidentsRunPlaybook(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .incidents()
            .runPlaybookWithResponse("myRg", "myWorkspace", "73e01a99-5cd7-4139-a149-9f2736ff2ar4", null, Context.NONE);
    }
}
```

### IpGeodata_Get

```java
import com.azure.core.util.Context;

/** Samples for IpGeodata Get. */
public final class IpGeodataGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/enrichment/GetGeodataByIp.json
     */
    /**
     * Sample code: Get geodata for a single IP address.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getGeodataForASingleIPAddress(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.ipGeodatas().getWithResponse("myRg", "1.2.3.4", Context.NONE);
    }
}
```

### Metadata_Create

```java
import com.azure.resourcemanager.securityinsights.models.Kind;
import com.azure.resourcemanager.securityinsights.models.MetadataAuthor;
import com.azure.resourcemanager.securityinsights.models.MetadataCategories;
import com.azure.resourcemanager.securityinsights.models.MetadataDependencies;
import com.azure.resourcemanager.securityinsights.models.MetadataSource;
import com.azure.resourcemanager.securityinsights.models.MetadataSupport;
import com.azure.resourcemanager.securityinsights.models.Operator;
import com.azure.resourcemanager.securityinsights.models.SourceKind;
import com.azure.resourcemanager.securityinsights.models.SupportTier;
import java.time.LocalDate;
import java.util.Arrays;

/** Samples for Metadata Create. */
public final class MetadataCreateSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/metadata/PutMetadata.json
     */
    /**
     * Sample code: Create/update full metadata.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createUpdateFullMetadata(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .metadatas()
            .define("metadataName")
            .withExistingWorkspace("myRg", "myWorkspace")
            .withContentId("c00ee137-7475-47c8-9cce-ec6f0f1bedd0")
            .withParentId(
                "/subscriptions/2e1dc338-d04d-4443-b721-037eff4fdcac/resourceGroups/myRg/providers/Microsoft.OperationalInsights/workspaces/myWorkspace/providers/Microsoft.SecurityInsights/alertRules/ruleName")
            .withVersion("1.0.0.0")
            .withKind(Kind.ANALYTICS_RULE)
            .withSource(
                new MetadataSource()
                    .withKind(SourceKind.SOLUTION)
                    .withName("Contoso Solution 1.0")
                    .withSourceId("b688a130-76f4-4a07-bf57-762222a3cadf"))
            .withAuthor(new MetadataAuthor().withName("User Name").withEmail("email@microsoft.com"))
            .withSupport(
                new MetadataSupport()
                    .withTier(SupportTier.PARTNER)
                    .withName("Microsoft")
                    .withEmail("support@microsoft.com")
                    .withLink("https://support.microsoft.com/"))
            .withDependencies(
                new MetadataDependencies()
                    .withOperator(Operator.AND)
                    .withCriteria(
                        Arrays
                            .asList(
                                new MetadataDependencies()
                                    .withOperator(Operator.OR)
                                    .withCriteria(
                                        Arrays
                                            .asList(
                                                new MetadataDependencies()
                                                    .withContentId("045d06d0-ee72-4794-aba4-cf5646e4c756")
                                                    .withKind(Kind.DATA_CONNECTOR)
                                                    .withName("Microsoft Defender for Endpoint"),
                                                new MetadataDependencies()
                                                    .withContentId("dbfcb2cc-d782-40ef-8d94-fe7af58a6f2d")
                                                    .withKind(Kind.DATA_CONNECTOR),
                                                new MetadataDependencies()
                                                    .withContentId("de4dca9b-eb37-47d6-a56f-b8b06b261593")
                                                    .withKind(Kind.DATA_CONNECTOR)
                                                    .withVersion("2.0"))),
                                new MetadataDependencies()
                                    .withContentId("31ee11cc-9989-4de8-b176-5e0ef5c4dbab")
                                    .withKind(Kind.PLAYBOOK)
                                    .withVersion("1.0"),
                                new MetadataDependencies()
                                    .withContentId("21ba424a-9438-4444-953a-7059539a7a1b")
                                    .withKind(Kind.PARSER))))
            .withCategories(
                new MetadataCategories()
                    .withDomains(Arrays.asList("Application", "Security – Insider Threat"))
                    .withVerticals(Arrays.asList("Healthcare")))
            .withProviders(Arrays.asList("Amazon", "Microsoft"))
            .withFirstPublishDate(LocalDate.parse("2021-05-18"))
            .withLastPublishDate(LocalDate.parse("2021-05-18"))
            .withCustomVersion("1.0")
            .withContentSchemaVersion("2.0")
            .withThreatAnalysisTactics(Arrays.asList("reconnaissance", "commandandcontrol"))
            .withThreatAnalysisTechniques(Arrays.asList("T1548", "T1548.001"))
            .withPreviewImages(Arrays.asList("firstImage.png", "secondImage.jpeg"))
            .withPreviewImagesDark(Arrays.asList("firstImageDark.png", "secondImageDark.jpeg"))
            .create();
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/metadata/PutMetadataMinimal.json
     */
    /**
     * Sample code: Create/update minimal metadata.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createUpdateMinimalMetadata(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .metadatas()
            .define("metadataName")
            .withExistingWorkspace("myRg", "myWorkspace")
            .withContentId("c00ee137-7475-47c8-9cce-ec6f0f1bedd0")
            .withParentId(
                "/subscriptions/2e1dc338-d04d-4443-b721-037eff4fdcac/resourceGroups/myRg/providers/Microsoft.OperationalInsights/workspaces/myWorkspace/providers/Microsoft.SecurityInsights/alertRules/ruleName")
            .withKind(Kind.ANALYTICS_RULE)
            .create();
    }
}
```

### Metadata_Delete

```java
import com.azure.core.util.Context;

/** Samples for Metadata Delete. */
public final class MetadataDeleteSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/metadata/DeleteMetadata.json
     */
    /**
     * Sample code: Delete metadata.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteMetadata(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.metadatas().deleteWithResponse("myRg", "myWorkspace", "metadataName", Context.NONE);
    }
}
```

### Metadata_Get

```java
import com.azure.core.util.Context;

/** Samples for Metadata Get. */
public final class MetadataGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/metadata/GetMetadata.json
     */
    /**
     * Sample code: Get single metadata by name.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getSingleMetadataByName(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.metadatas().getWithResponse("myRg", "myWorkspace", "metadataName", Context.NONE);
    }
}
```

### Metadata_List

```java
import com.azure.core.util.Context;

/** Samples for Metadata List. */
public final class MetadataListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/metadata/GetAllMetadata.json
     */
    /**
     * Sample code: Get all metadata.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllMetadata(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.metadatas().list("myRg", "myWorkspace", null, null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/metadata/GetAllMetadataOData.json
     */
    /**
     * Sample code: Get all metadata with OData filter/orderby/skip/top.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllMetadataWithODataFilterOrderbySkipTop(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.metadatas().list("myRg", "myWorkspace", null, null, null, null, Context.NONE);
    }
}
```

### Metadata_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securityinsights.models.MetadataAuthor;
import com.azure.resourcemanager.securityinsights.models.MetadataModel;

/** Samples for Metadata Update. */
public final class MetadataUpdateSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/metadata/PatchMetadata.json
     */
    /**
     * Sample code: Update metadata.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void updateMetadata(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        MetadataModel resource =
            manager.metadatas().getWithResponse("myRg", "myWorkspace", "metadataName", Context.NONE).getValue();
        resource
            .update()
            .withAuthor(new MetadataAuthor().withName("User Name").withEmail("email@microsoft.com"))
            .apply();
    }
}
```

### OfficeConsents_Delete

```java
import com.azure.core.util.Context;

/** Samples for OfficeConsents Delete. */
public final class OfficeConsentsDeleteSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/officeConsents/DeleteOfficeConsents.json
     */
    /**
     * Sample code: Delete an office consent.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteAnOfficeConsent(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .officeConsents()
            .deleteWithResponse("myRg", "myWorkspace", "04e5fd05-ff86-4b97-b8d2-1c20933cb46c", Context.NONE);
    }
}
```

### OfficeConsents_Get

```java
import com.azure.core.util.Context;

/** Samples for OfficeConsents Get. */
public final class OfficeConsentsGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/officeConsents/GetOfficeConsentsById.json
     */
    /**
     * Sample code: Get an office consent.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAnOfficeConsent(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .officeConsents()
            .getWithResponse("myRg", "myWorkspace", "04e5fd05-ff86-4b97-b8d2-1c20933cb46c", Context.NONE);
    }
}
```

### OfficeConsents_List

```java
import com.azure.core.util.Context;

/** Samples for OfficeConsents List. */
public final class OfficeConsentsListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/officeConsents/GetOfficeConsents.json
     */
    /**
     * Sample code: Get all office consents.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllOfficeConsents(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.officeConsents().list("myRg", "myWorkspace", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/operations/ListOperations.json
     */
    /**
     * Sample code: Get all operations.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllOperations(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### ProductSettings_Delete

```java
import com.azure.core.util.Context;

/** Samples for ProductSettings Delete. */
public final class ProductSettingsDeleteSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/settings/DeleteEyesOnSetting.json
     */
    /**
     * Sample code: Delete EyesOn settings.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteEyesOnSettings(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.productSettings().deleteWithResponse("myRg", "myWorkspace", "EyesOn", Context.NONE);
    }
}
```

### ProductSettings_Get

```java
import com.azure.core.util.Context;

/** Samples for ProductSettings Get. */
public final class ProductSettingsGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/settings/GetEyesOnSetting.json
     */
    /**
     * Sample code: Get EyesOn settings.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getEyesOnSettings(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.productSettings().getWithResponse("myRg", "myWorkspace", "EyesOn", Context.NONE);
    }
}
```

### ProductSettings_List

```java
import com.azure.core.util.Context;

/** Samples for ProductSettings List. */
public final class ProductSettingsListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/settings/GetAllSettings.json
     */
    /**
     * Sample code: Get all settings.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllSettings(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.productSettings().listWithResponse("myRg", "myWorkspace", Context.NONE);
    }
}
```

### ProductSettings_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securityinsights.models.EyesOn;

/** Samples for ProductSettings Update. */
public final class ProductSettingsUpdateSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/settings/UpdateEyesOnSetting.json
     */
    /**
     * Sample code: Update EyesOn settings.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void updateEyesOnSettings(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .productSettings()
            .updateWithResponse(
                "myRg",
                "myWorkspace",
                "EyesOn",
                new EyesOn().withEtag("\"0300bf09-0000-0000-0000-5c37296e0000\""),
                Context.NONE);
    }
}
```

### SecurityMLAnalyticsSettings_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.securityinsights.models.AnomalySecurityMLAnalyticsSettings;
import com.azure.resourcemanager.securityinsights.models.AttackTactic;
import com.azure.resourcemanager.securityinsights.models.SecurityMLAnalyticsSettingsDataSource;
import com.azure.resourcemanager.securityinsights.models.SettingsStatus;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

/** Samples for SecurityMLAnalyticsSettings CreateOrUpdate. */
public final class SecurityMLAnalyticsSettingsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/securityMLAnalyticsSettings/CreateAnomalySecurityMLAnalyticsSetting.json
     */
    /**
     * Sample code: Creates or updates a Anomaly Security ML Analytics Settings.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAAnomalySecurityMLAnalyticsSettings(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) throws IOException {
        manager
            .securityMLAnalyticsSettings()
            .createOrUpdateWithResponse(
                "myRg",
                "myWorkspace",
                "f209187f-1d17-4431-94af-c141bf5f23db",
                new AnomalySecurityMLAnalyticsSettings()
                    .withEtag("\"260090e2-0000-0d00-0000-5d6fb8670000\"")
                    .withDescription(
                        "When account logs from a source region that has rarely been logged in from during the last 14"
                            + " days, an anomaly is triggered.")
                    .withDisplayName("Login from unusual region")
                    .withEnabled(true)
                    .withRequiredDataConnectors(
                        Arrays
                            .asList(
                                new SecurityMLAnalyticsSettingsDataSource()
                                    .withConnectorId("AWS")
                                    .withDataTypes(Arrays.asList("AWSCloudTrail"))))
                    .withTactics(Arrays.asList(AttackTactic.EXFILTRATION, AttackTactic.COMMAND_AND_CONTROL))
                    .withTechniques(Arrays.asList("T1037", "T1021"))
                    .withAnomalyVersion("1.0.5")
                    .withCustomizableObservations(
                        SerializerFactory
                            .createDefaultManagementSerializerAdapter()
                            .deserialize(
                                "{\"multiSelectObservations\":null,\"prioritizeExcludeObservations\":null,\"singleSelectObservations\":[{\"name\":\"Device"
                                    + " vendor\",\"description\":\"Select device vendor of network connection logs from"
                                    + " CommonSecurityLog\",\"rerun\":\"RerunAlways\",\"sequenceNumber\":1,\"supportedValues\":[\"Palo"
                                    + " Alto Networks\",\"Fortinet\",\"Check"
                                    + " Point\"],\"supportedValuesKql\":null,\"value\":[\"Palo Alto"
                                    + " Networks\"],\"valuesKql\":null}],\"singleValueObservations\":null,\"thresholdObservations\":[{\"name\":\"Daily"
                                    + " data transfer threshold in MB\",\"description\":\"Suppress anomalies when daily"
                                    + " data transfered (in MB) per hour is less than the chosen"
                                    + " value\",\"maximum\":\"100\",\"minimum\":\"1\",\"rerun\":\"RerunAlways\",\"sequenceNumber\":1,\"value\":\"25\"},{\"name\":\"Number"
                                    + " of standard deviations\",\"description\":\"Triggers anomalies when number of"
                                    + " standard deviations is greater than the chosen"
                                    + " value\",\"maximum\":\"10\",\"minimum\":\"2\",\"rerun\":\"RerunAlways\",\"sequenceNumber\":2,\"value\":\"3\"}]}",
                                Object.class,
                                SerializerEncoding.JSON))
                    .withFrequency(Duration.parse("PT1H"))
                    .withSettingsStatus(SettingsStatus.PRODUCTION)
                    .withIsDefaultSettings(true)
                    .withAnomalySettingsVersion(0)
                    .withSettingsDefinitionId(UUID.fromString("f209187f-1d17-4431-94af-c141bf5f23db")),
                Context.NONE);
    }
}
```

### SecurityMLAnalyticsSettings_Delete

```java
import com.azure.core.util.Context;

/** Samples for SecurityMLAnalyticsSettings Delete. */
public final class SecurityMLAnalyticsSettingsDeleteSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/securityMLAnalyticsSettings/DeleteSecurityMLAnalyticsSetting.json
     */
    /**
     * Sample code: Delete a Security ML Analytics Settings.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteASecurityMLAnalyticsSettings(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .securityMLAnalyticsSettings()
            .deleteWithResponse("myRg", "myWorkspace", "f209187f-1d17-4431-94af-c141bf5f23db", Context.NONE);
    }
}
```

### SecurityMLAnalyticsSettings_Get

```java
import com.azure.core.util.Context;

/** Samples for SecurityMLAnalyticsSettings Get. */
public final class SecurityMLAnalyticsSettingsGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/securityMLAnalyticsSettings/GetAnomalySecurityMLAnalyticsSetting.json
     */
    /**
     * Sample code: Get a Anomaly Security ML Analytics Settings.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAAnomalySecurityMLAnalyticsSettings(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .securityMLAnalyticsSettings()
            .getWithResponse("myRg", "myWorkspace", "myFirstAnomalySettings", Context.NONE);
    }
}
```

### SecurityMLAnalyticsSettings_List

```java
import com.azure.core.util.Context;

/** Samples for SecurityMLAnalyticsSettings List. */
public final class SecurityMLAnalyticsSettingsListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/securityMLAnalyticsSettings/GetAllSecurityMLAnalyticsSettings.json
     */
    /**
     * Sample code: Get all Security ML Analytics Settings.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllSecurityMLAnalyticsSettings(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.securityMLAnalyticsSettings().list("myRg", "myWorkspace", Context.NONE);
    }
}
```

### SentinelOnboardingStates_Create

```java
/** Samples for SentinelOnboardingStates Create. */
public final class SentinelOnboardingStatesCreateSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/onboardingStates/CreateSentinelOnboardingState.json
     */
    /**
     * Sample code: Create Sentinel onboarding state.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createSentinelOnboardingState(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .sentinelOnboardingStates()
            .define("default")
            .withExistingWorkspace("myRg", "myWorkspace")
            .withCustomerManagedKey(false)
            .create();
    }
}
```

### SentinelOnboardingStates_Delete

```java
import com.azure.core.util.Context;

/** Samples for SentinelOnboardingStates Delete. */
public final class SentinelOnboardingStatesDeleteSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/onboardingStates/DeleteSentinelOnboardingState.json
     */
    /**
     * Sample code: Delete Sentinel onboarding state.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteSentinelOnboardingState(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.sentinelOnboardingStates().deleteWithResponse("myRg", "myWorkspace", "default", Context.NONE);
    }
}
```

### SentinelOnboardingStates_Get

```java
import com.azure.core.util.Context;

/** Samples for SentinelOnboardingStates Get. */
public final class SentinelOnboardingStatesGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/onboardingStates/GetSentinelOnboardingState.json
     */
    /**
     * Sample code: Get Sentinel onboarding state.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getSentinelOnboardingState(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.sentinelOnboardingStates().getWithResponse("myRg", "myWorkspace", "default", Context.NONE);
    }
}
```

### SentinelOnboardingStates_List

```java
import com.azure.core.util.Context;

/** Samples for SentinelOnboardingStates List. */
public final class SentinelOnboardingStatesListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/onboardingStates/GetAllSentinelOnboardingStates.json
     */
    /**
     * Sample code: Get all Sentinel onboarding states.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllSentinelOnboardingStates(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.sentinelOnboardingStates().listWithResponse("myRg", "myWorkspace", Context.NONE);
    }
}
```

### SourceControl_ListRepositories

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securityinsights.models.RepoType;

/** Samples for SourceControl ListRepositories. */
public final class SourceControlListRepositoriesSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/repositories/GetRepositories.json
     */
    /**
     * Sample code: Get repository list.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getRepositoryList(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.sourceControls().listRepositories("myRg", "myWorkspace", RepoType.GITHUB, Context.NONE);
    }
}
```

### SourceControlsOperation_Create

```java
import com.azure.resourcemanager.securityinsights.models.ContentPathMap;
import com.azure.resourcemanager.securityinsights.models.ContentType;
import com.azure.resourcemanager.securityinsights.models.RepoType;
import com.azure.resourcemanager.securityinsights.models.Repository;
import java.util.Arrays;

/** Samples for SourceControlsOperation Create. */
public final class SourceControlsOperationCreateSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/sourcecontrols/CreateSourceControl.json
     */
    /**
     * Sample code: Creates a source control.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsASourceControl(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .sourceControlsOperations()
            .define("789e0c1f-4a3d-43ad-809c-e713b677b04a")
            .withExistingWorkspace("myRg", "myWorkspace")
            .withEtag("\"0300bf09-0000-0000-0000-5c37296e0000\"")
            .withDisplayName("My Source Control")
            .withDescription("This is a source control")
            .withRepoType(RepoType.GITHUB)
            .withContentTypes(Arrays.asList(ContentType.fromString("AnalyticRules"), ContentType.WORKBOOK))
            .withRepository(
                new Repository()
                    .withUrl("https://github.com/user/repo")
                    .withBranch("master")
                    .withDisplayUrl("https://github.com/user/repo")
                    .withPathMapping(
                        Arrays
                            .asList(
                                new ContentPathMap()
                                    .withContentType(ContentType.fromString("AnalyticRules"))
                                    .withPath("path/to/rules"),
                                new ContentPathMap()
                                    .withContentType(ContentType.WORKBOOK)
                                    .withPath("path/to/workbooks"))))
            .create();
    }
}
```

### SourceControlsOperation_Delete

```java
import com.azure.core.util.Context;

/** Samples for SourceControlsOperation Delete. */
public final class SourceControlsOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/sourcecontrols/DeleteSourceControl.json
     */
    /**
     * Sample code: Delete a source control.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteASourceControl(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .sourceControlsOperations()
            .deleteWithResponse("myRg", "myWorkspace", "789e0c1f-4a3d-43ad-809c-e713b677b04a", Context.NONE);
    }
}
```

### SourceControlsOperation_Get

```java
import com.azure.core.util.Context;

/** Samples for SourceControlsOperation Get. */
public final class SourceControlsOperationGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/sourcecontrols/GetSourceControlById.json
     */
    /**
     * Sample code: Get a source control.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getASourceControl(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .sourceControlsOperations()
            .getWithResponse("myRg", "myWorkspace", "789e0c1f-4a3d-43ad-809c-e713b677b04a", Context.NONE);
    }
}
```

### SourceControlsOperation_List

```java
import com.azure.core.util.Context;

/** Samples for SourceControlsOperation List. */
public final class SourceControlsOperationListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/sourcecontrols/GetSourceControls.json
     */
    /**
     * Sample code: Get all source controls.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllSourceControls(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.sourceControlsOperations().list("myRg", "myWorkspace", Context.NONE);
    }
}
```

### ThreatIntelligenceIndicator_AppendTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securityinsights.models.ThreatIntelligenceAppendTags;
import java.util.Arrays;

/** Samples for ThreatIntelligenceIndicator AppendTags. */
public final class ThreatIntelligenceIndicatorAppendTagsSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/threatintelligence/AppendTagsThreatIntelligence.json
     */
    /**
     * Sample code: Append tags to a threat intelligence indicator.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void appendTagsToAThreatIntelligenceIndicator(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .threatIntelligenceIndicators()
            .appendTagsWithResponse(
                "myRg",
                "myWorkspace",
                "d9cd6f0b-96b9-3984-17cd-a779d1e15a93",
                new ThreatIntelligenceAppendTags().withThreatIntelligenceTags(Arrays.asList("tag1", "tag2")),
                Context.NONE);
    }
}
```

### ThreatIntelligenceIndicator_Create

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securityinsights.models.ThreatIntelligenceIndicatorModel;
import java.util.Arrays;

/** Samples for ThreatIntelligenceIndicator Create. */
public final class ThreatIntelligenceIndicatorCreateSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/threatintelligence/UpdateThreatIntelligence.json
     */
    /**
     * Sample code: Update a threat Intelligence indicator.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void updateAThreatIntelligenceIndicator(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .threatIntelligenceIndicators()
            .createWithResponse(
                "myRg",
                "myWorkspace",
                "d9cd6f0b-96b9-3984-17cd-a779d1e15a93",
                new ThreatIntelligenceIndicatorModel()
                    .withThreatIntelligenceTags(Arrays.asList("new schema"))
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
                Context.NONE);
    }
}
```

### ThreatIntelligenceIndicator_CreateIndicator

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securityinsights.models.ThreatIntelligenceIndicatorModel;
import java.util.Arrays;

/** Samples for ThreatIntelligenceIndicator CreateIndicator. */
public final class ThreatIntelligenceIndicatorCreateIndicatorSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/threatintelligence/CreateThreatIntelligence.json
     */
    /**
     * Sample code: Create a new Threat Intelligence.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createANewThreatIntelligence(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .threatIntelligenceIndicators()
            .createIndicatorWithResponse(
                "myRg",
                "myWorkspace",
                new ThreatIntelligenceIndicatorModel()
                    .withThreatIntelligenceTags(Arrays.asList("new schema"))
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
                    .withValidFrom("2021-09-15T17:44:00.114052Z")
                    .withValidUntil("")
                    .withModified(""),
                Context.NONE);
    }
}
```

### ThreatIntelligenceIndicator_Delete

```java
import com.azure.core.util.Context;

/** Samples for ThreatIntelligenceIndicator Delete. */
public final class ThreatIntelligenceIndicatorDeleteSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/threatintelligence/DeleteThreatIntelligence.json
     */
    /**
     * Sample code: Delete a threat intelligence indicator.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteAThreatIntelligenceIndicator(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .threatIntelligenceIndicators()
            .deleteWithResponse("myRg", "myWorkspace", "d9cd6f0b-96b9-3984-17cd-a779d1e15a93", Context.NONE);
    }
}
```

### ThreatIntelligenceIndicator_Get

```java
import com.azure.core.util.Context;

/** Samples for ThreatIntelligenceIndicator Get. */
public final class ThreatIntelligenceIndicatorGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/threatintelligence/GetThreatIntelligenceById.json
     */
    /**
     * Sample code: View a threat intelligence indicator by name.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void viewAThreatIntelligenceIndicatorByName(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .threatIntelligenceIndicators()
            .getWithResponse("myRg", "myWorkspace", "e16ef847-962e-d7b6-9c8b-a33e4bd30e47", Context.NONE);
    }
}
```

### ThreatIntelligenceIndicator_QueryIndicators

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securityinsights.models.ThreatIntelligenceFilteringCriteria;
import com.azure.resourcemanager.securityinsights.models.ThreatIntelligenceSortingCriteria;
import com.azure.resourcemanager.securityinsights.models.ThreatIntelligenceSortingCriteriaEnum;
import java.util.Arrays;

/** Samples for ThreatIntelligenceIndicator QueryIndicators. */
public final class ThreatIntelligenceIndicatorQueryIndicatorsSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/threatintelligence/QueryThreatIntelligence.json
     */
    /**
     * Sample code: Query threat intelligence indicators as per filtering criteria.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void queryThreatIntelligenceIndicatorsAsPerFilteringCriteria(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .threatIntelligenceIndicators()
            .queryIndicators(
                "myRg",
                "myWorkspace",
                new ThreatIntelligenceFilteringCriteria()
                    .withPageSize(100)
                    .withMinConfidence(25)
                    .withMaxConfidence(80)
                    .withMinValidUntil("2021-04-05T17:44:00.114052Z")
                    .withMaxValidUntil("2021-04-25T17:44:00.114052Z")
                    .withSortBy(
                        Arrays
                            .asList(
                                new ThreatIntelligenceSortingCriteria()
                                    .withItemKey("lastUpdatedTimeUtc")
                                    .withSortOrder(ThreatIntelligenceSortingCriteriaEnum.DESCENDING)))
                    .withSources(Arrays.asList("Azure Sentinel")),
                Context.NONE);
    }
}
```

### ThreatIntelligenceIndicator_ReplaceTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securityinsights.models.ThreatIntelligenceIndicatorModel;
import java.util.Arrays;

/** Samples for ThreatIntelligenceIndicator ReplaceTags. */
public final class ThreatIntelligenceIndicatorReplaceTagsSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/threatintelligence/ReplaceTagsThreatIntelligence.json
     */
    /**
     * Sample code: Replace tags to a Threat Intelligence.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void replaceTagsToAThreatIntelligence(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .threatIntelligenceIndicators()
            .replaceTagsWithResponse(
                "myRg",
                "myWorkspace",
                "d9cd6f0b-96b9-3984-17cd-a779d1e15a93",
                new ThreatIntelligenceIndicatorModel()
                    .withEtag("\"0000262c-0000-0800-0000-5e9767060000\"")
                    .withThreatIntelligenceTags(Arrays.asList("patching tags")),
                Context.NONE);
    }
}
```

### ThreatIntelligenceIndicatorMetrics_List

```java
import com.azure.core.util.Context;

/** Samples for ThreatIntelligenceIndicatorMetrics List. */
public final class ThreatIntelligenceIndicatorMetricsListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/threatintelligence/CollectThreatIntelligenceMetrics.json
     */
    /**
     * Sample code: Get threat intelligence indicators metrics.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getThreatIntelligenceIndicatorsMetrics(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.threatIntelligenceIndicatorMetrics().listWithResponse("myRg", "myWorkspace", Context.NONE);
    }
}
```

### ThreatIntelligenceIndicatorsOperation_List

```java
import com.azure.core.util.Context;

/** Samples for ThreatIntelligenceIndicatorsOperation List. */
public final class ThreatIntelligenceIndicatorsOperationListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/threatintelligence/GetThreatIntelligence.json
     */
    /**
     * Sample code: Get all threat intelligence indicators.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllThreatIntelligenceIndicators(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .threatIntelligenceIndicatorsOperations()
            .list("myRg", "myWorkspace", null, null, null, null, Context.NONE);
    }
}
```

### WatchlistItems_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for WatchlistItems CreateOrUpdate. */
public final class WatchlistItemsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/watchlists/CreateWatchlistItem.json
     */
    /**
     * Sample code: Creates or updates a watchlist item.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAWatchlistItem(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .watchlistItems()
            .define("82ba292c-dc97-4dfc-969d-d4dd9e666842")
            .withExistingWatchlist("myRg", "myWorkspace", "highValueAsset")
            .withEtag("0300bf09-0000-0000-0000-5c37296e0000")
            .withItemsKeyValue(
                mapOf(
                    "Business tier",
                    "10.0.2.0/24",
                    "Data tier",
                    "10.0.2.0/24",
                    "Gateway subnet",
                    "10.0.255.224/27",
                    "Private DMZ in",
                    "10.0.0.0/27",
                    "Public DMZ out",
                    "10.0.0.96/27",
                    "Web Tier",
                    "10.0.1.0/24"))
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

### WatchlistItems_Delete

```java
import com.azure.core.util.Context;

/** Samples for WatchlistItems Delete. */
public final class WatchlistItemsDeleteSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/watchlists/DeleteWatchlistItem.json
     */
    /**
     * Sample code: Delete a watchlist Item.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteAWatchlistItem(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .watchlistItems()
            .deleteWithResponse(
                "myRg", "myWorkspace", "highValueAsset", "4008512e-1d30-48b2-9ee2-d3612ed9d3ea", Context.NONE);
    }
}
```

### WatchlistItems_Get

```java
import com.azure.core.util.Context;

/** Samples for WatchlistItems Get. */
public final class WatchlistItemsGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/watchlists/GetWatchlistItemById.json
     */
    /**
     * Sample code: Get a watchlist item.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAWatchlistItem(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .watchlistItems()
            .getWithResponse(
                "myRg", "myWorkspace", "highValueAsset", "3f8901fe-63d9-4875-9ad5-9fb3b8105797", Context.NONE);
    }
}
```

### WatchlistItems_List

```java
import com.azure.core.util.Context;

/** Samples for WatchlistItems List. */
public final class WatchlistItemsListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/watchlists/GetWatchlistItems.json
     */
    /**
     * Sample code: Get all watchlist Items.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllWatchlistItems(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.watchlistItems().list("myRg", "myWorkspace", "highValueAsset", null, Context.NONE);
    }
}
```

### Watchlists_CreateOrUpdate

```java
import com.azure.resourcemanager.securityinsights.models.SourceType;

/** Samples for Watchlists CreateOrUpdate. */
public final class WatchlistsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/watchlists/CreateWatchlistAndWatchlistItems.json
     */
    /**
     * Sample code: Creates or updates a watchlist and bulk creates watchlist items.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAWatchlistAndBulkCreatesWatchlistItems(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .watchlists()
            .define("highValueAsset")
            .withExistingWorkspace("myRg", "myWorkspace")
            .withEtag("\"0300bf09-0000-0000-0000-5c37296e0000\"")
            .withDisplayName("High Value Assets Watchlist")
            .withProvider("Microsoft")
            .withSource("watchlist.csv")
            .withSourceType(SourceType.LOCAL_FILE)
            .withDescription("Watchlist from CSV content")
            .withNumberOfLinesToSkip(1)
            .withRawContent("This line will be skipped\nheader1,header2\nvalue1,value2")
            .withItemsSearchKey("header1")
            .withContentType("text/csv")
            .create();
    }

    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/watchlists/CreateWatchlist.json
     */
    /**
     * Sample code: Creates or updates a watchlist.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void createsOrUpdatesAWatchlist(
        com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager
            .watchlists()
            .define("highValueAsset")
            .withExistingWorkspace("myRg", "myWorkspace")
            .withEtag("\"0300bf09-0000-0000-0000-5c37296e0000\"")
            .withDisplayName("High Value Assets Watchlist")
            .withProvider("Microsoft")
            .withSource("watchlist.csv")
            .withSourceType(SourceType.LOCAL_FILE)
            .withDescription("Watchlist from CSV content")
            .withItemsSearchKey("header1")
            .create();
    }
}
```

### Watchlists_Delete

```java
import com.azure.core.util.Context;

/** Samples for Watchlists Delete. */
public final class WatchlistsDeleteSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/watchlists/DeleteWatchlist.json
     */
    /**
     * Sample code: Delete a watchlist.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void deleteAWatchlist(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.watchlists().deleteWithResponse("myRg", "myWorkspace", "highValueAsset", Context.NONE);
    }
}
```

### Watchlists_Get

```java
import com.azure.core.util.Context;

/** Samples for Watchlists Get. */
public final class WatchlistsGetSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/watchlists/GetWatchlistByAlias.json
     */
    /**
     * Sample code: Get a watchlist.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAWatchlist(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.watchlists().getWithResponse("myRg", "myWorkspace", "highValueAsset", Context.NONE);
    }
}
```

### Watchlists_List

```java
import com.azure.core.util.Context;

/** Samples for Watchlists List. */
public final class WatchlistsListSamples {
    /*
     * x-ms-original-file: specification/securityinsights/resource-manager/Microsoft.SecurityInsights/preview/2022-09-01-preview/examples/watchlists/GetWatchlists.json
     */
    /**
     * Sample code: Get all watchlists.
     *
     * @param manager Entry point to SecurityInsightsManager.
     */
    public static void getAllWatchlists(com.azure.resourcemanager.securityinsights.SecurityInsightsManager manager) {
        manager.watchlists().list("myRg", "myWorkspace", null, Context.NONE);
    }
}
```

