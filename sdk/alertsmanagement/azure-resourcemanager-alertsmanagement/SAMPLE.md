# Code snippets and samples


## AlertProcessingRules

- [CreateOrUpdate](#alertprocessingrules_createorupdate)
- [Delete](#alertprocessingrules_delete)
- [GetByResourceGroup](#alertprocessingrules_getbyresourcegroup)
- [List](#alertprocessingrules_list)
- [ListByResourceGroup](#alertprocessingrules_listbyresourcegroup)
- [Update](#alertprocessingrules_update)

## AlertRuleRecommendations

- [List](#alertrulerecommendations_list)
- [ListByResource](#alertrulerecommendations_listbyresource)

## Alerts

- [ChangeState](#alerts_changestate)
- [GetById](#alerts_getbyid)
- [GetHistory](#alerts_gethistory)
- [GetSummary](#alerts_getsummary)
- [List](#alerts_list)
- [Metadata](#alerts_metadata)

## Operations

- [List](#operations_list)

## PrometheusRuleGroups

- [CreateOrUpdate](#prometheusrulegroups_createorupdate)
- [Delete](#prometheusrulegroups_delete)
- [GetByResourceGroup](#prometheusrulegroups_getbyresourcegroup)
- [List](#prometheusrulegroups_list)
- [ListByResourceGroup](#prometheusrulegroups_listbyresourcegroup)
- [Update](#prometheusrulegroups_update)

## SmartGroups

- [ChangeState](#smartgroups_changestate)
- [GetById](#smartgroups_getbyid)
- [GetHistory](#smartgroups_gethistory)
- [List](#smartgroups_list)

## TenantActivityLogAlerts

- [CreateOrUpdate](#tenantactivitylogalerts_createorupdate)
- [Delete](#tenantactivitylogalerts_delete)
- [Get](#tenantactivitylogalerts_get)
- [ListByManagementGroup](#tenantactivitylogalerts_listbymanagementgroup)
- [ListByTenant](#tenantactivitylogalerts_listbytenant)
- [Update](#tenantactivitylogalerts_update)
### AlertProcessingRules_CreateOrUpdate

```java
import com.azure.resourcemanager.alertsmanagement.models.AddActionGroups;
import com.azure.resourcemanager.alertsmanagement.models.AlertProcessingRuleProperties;
import com.azure.resourcemanager.alertsmanagement.models.Condition;
import com.azure.resourcemanager.alertsmanagement.models.DailyRecurrence;
import com.azure.resourcemanager.alertsmanagement.models.DaysOfWeek;
import com.azure.resourcemanager.alertsmanagement.models.Field;
import com.azure.resourcemanager.alertsmanagement.models.Operator;
import com.azure.resourcemanager.alertsmanagement.models.RemoveAllActionGroups;
import com.azure.resourcemanager.alertsmanagement.models.Schedule;
import com.azure.resourcemanager.alertsmanagement.models.WeeklyRecurrence;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AlertProcessingRules CreateOrUpdate.
 */
public final class AlertProcessingRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/stable/2021-08-08/examples/
     * AlertProcessingRules_Create_or_update_remove_all_action_groups_recurring_maintenance_window.json
     */
    /**
     * Sample code: Create or update a rule that removes all action groups from all alerts on any VM in two resource
     * groups during a recurring maintenance window (2200-0400 every Sat and Sun, India Standard Time).
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void
        createOrUpdateARuleThatRemovesAllActionGroupsFromAllAlertsOnAnyVMInTwoResourceGroupsDuringARecurringMaintenanceWindow22000400EverySatAndSunIndiaStandardTime(
            com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alertProcessingRules()
            .define("RemoveActionGroupsRecurringMaintenance")
            .withRegion("Global")
            .withExistingResourceGroup("alertscorrelationrg")
            .withTags(mapOf())
            .withProperties(new AlertProcessingRuleProperties()
                .withScopes(Arrays.asList("/subscriptions/subId1/resourceGroups/RGId1",
                    "/subscriptions/subId1/resourceGroups/RGId2"))
                .withConditions(Arrays.asList(new Condition().withField(Field.TARGET_RESOURCE_TYPE)
                    .withOperator(Operator.EQUALS)
                    .withValues(Arrays.asList("microsoft.compute/virtualmachines"))))
                .withSchedule(new Schedule().withTimeZone("India Standard Time")
                    .withRecurrences(Arrays.asList(new WeeklyRecurrence().withStartTime("22:00:00")
                        .withEndTime("04:00:00")
                        .withDaysOfWeek(Arrays.asList(DaysOfWeek.SATURDAY, DaysOfWeek.SUNDAY)))))
                .withActions(Arrays.asList(new RemoveAllActionGroups()))
                .withDescription(
                    "Remove all ActionGroups from all Vitual machine Alerts during the recurring maintenance")
                .withEnabled(true))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/stable/2021-08-08/examples/
     * AlertProcessingRules_Create_or_update_add_two_action_groups_all_Sev0_Sev1_two_resource_groups.json
     */
    /**
     * Sample code: Create or update a rule that adds two action groups to all Sev0 and Sev1 alerts in two resource
     * groups.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void createOrUpdateARuleThatAddsTwoActionGroupsToAllSev0AndSev1AlertsInTwoResourceGroups(
        com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alertProcessingRules()
            .define("AddActionGroupsBySeverity")
            .withRegion("Global")
            .withExistingResourceGroup("alertscorrelationrg")
            .withTags(mapOf())
            .withProperties(new AlertProcessingRuleProperties()
                .withScopes(Arrays.asList("/subscriptions/subId1/resourceGroups/RGId1",
                    "/subscriptions/subId1/resourceGroups/RGId2"))
                .withConditions(Arrays.asList(new Condition().withField(Field.SEVERITY)
                    .withOperator(Operator.EQUALS)
                    .withValues(Arrays.asList("sev0", "sev1"))))
                .withActions(Arrays.asList(new AddActionGroups().withActionGroupIds(Arrays.asList(
                    "/subscriptions/subId1/resourcegroups/RGId1/providers/microsoft.insights/actiongroups/AGId1",
                    "/subscriptions/subId1/resourcegroups/RGId1/providers/microsoft.insights/actiongroups/AGId2"))))
                .withDescription("Add AGId1 and AGId2 to all Sev0 and Sev1 alerts in these resourceGroups")
                .withEnabled(true))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/stable/2021-08-08/examples/
     * AlertProcessingRules_Create_or_update_add_action_group_all_alerts_in_subscription.json
     */
    /**
     * Sample code: Create or update a rule that adds an action group to all alerts in a subscription.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void createOrUpdateARuleThatAddsAnActionGroupToAllAlertsInASubscription(
        com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alertProcessingRules()
            .define("AddActionGroupToSubscription")
            .withRegion("Global")
            .withExistingResourceGroup("alertscorrelationrg")
            .withTags(mapOf())
            .withProperties(new AlertProcessingRuleProperties().withScopes(Arrays.asList("/subscriptions/subId1"))
                .withActions(Arrays.asList(new AddActionGroups().withActionGroupIds(Arrays.asList(
                    "/subscriptions/subId1/resourcegroups/RGId1/providers/microsoft.insights/actiongroups/ActionGroup1"))))
                .withDescription("Add ActionGroup1 to all alerts in the subscription")
                .withEnabled(true))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/stable/2021-08-08/examples/
     * AlertProcessingRules_Create_or_update_remove_all_action_groups_specific_VM_one-off_maintenance_window.json
     */
    /**
     * Sample code: Create or update a rule that removes all action groups from alerts on a specific VM during a one-off
     * maintenance window (1800-2000 at a specific date, Pacific Standard Time).
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void
        createOrUpdateARuleThatRemovesAllActionGroupsFromAlertsOnASpecificVMDuringAOneOffMaintenanceWindow18002000AtASpecificDatePacificStandardTime(
            com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alertProcessingRules()
            .define("RemoveActionGroupsMaintenanceWindow")
            .withRegion("Global")
            .withExistingResourceGroup("alertscorrelationrg")
            .withTags(mapOf())
            .withProperties(new AlertProcessingRuleProperties()
                .withScopes(Arrays.asList(
                    "/subscriptions/subId1/resourceGroups/RGId1/providers/Microsoft.Compute/virtualMachines/VMName"))
                .withSchedule(new Schedule().withEffectiveFrom("2021-04-15T18:00:00")
                    .withEffectiveUntil("2021-04-15T20:00:00")
                    .withTimeZone("Pacific Standard Time"))
                .withActions(Arrays.asList(new RemoveAllActionGroups()))
                .withDescription("Removes all ActionGroups from all Alerts on VMName during the maintenance window")
                .withEnabled(true))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/stable/2021-08-08/examples/
     * AlertProcessingRules_Create_or_update_remove_all_action_groups_from_specific_alert_rule.json
     */
    /**
     * Sample code: Create or update a rule that removes all action groups from all alerts in a subscription coming from
     * a specific alert rule.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void
        createOrUpdateARuleThatRemovesAllActionGroupsFromAllAlertsInASubscriptionComingFromASpecificAlertRule(
            com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alertProcessingRules()
            .define("RemoveActionGroupsSpecificAlertRule")
            .withRegion("Global")
            .withExistingResourceGroup("alertscorrelationrg")
            .withTags(mapOf())
            .withProperties(new AlertProcessingRuleProperties().withScopes(Arrays.asList("/subscriptions/subId1"))
                .withConditions(Arrays.asList(new Condition().withField(Field.ALERT_RULE_ID)
                    .withOperator(Operator.EQUALS)
                    .withValues(Arrays.asList(
                        "/subscriptions/suubId1/resourceGroups/Rgid2/providers/microsoft.insights/activityLogAlerts/RuleName"))))
                .withActions(Arrays.asList(new RemoveAllActionGroups()))
                .withDescription("Removes all ActionGroups from all Alerts that fire on above AlertRule")
                .withEnabled(true))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/stable/2021-08-08/examples/
     * AlertProcessingRules_Create_or_update_remove_all_action_groups_outside_business_hours.json
     */
    /**
     * Sample code: Create or update a rule that removes all action groups outside business hours (Mon-Fri 09:00-17:00,
     * Eastern Standard Time).
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void
        createOrUpdateARuleThatRemovesAllActionGroupsOutsideBusinessHoursMonFri09001700EasternStandardTime(
            com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alertProcessingRules()
            .define("RemoveActionGroupsOutsideBusinessHours")
            .withRegion("Global")
            .withExistingResourceGroup("alertscorrelationrg")
            .withTags(mapOf())
            .withProperties(new AlertProcessingRuleProperties().withScopes(Arrays.asList("/subscriptions/subId1"))
                .withSchedule(new Schedule().withTimeZone("Eastern Standard Time")
                    .withRecurrences(
                        Arrays.asList(new DailyRecurrence().withStartTime("17:00:00").withEndTime("09:00:00"),
                            new WeeklyRecurrence()
                                .withDaysOfWeek(Arrays.asList(DaysOfWeek.SATURDAY, DaysOfWeek.SUNDAY)))))
                .withActions(Arrays.asList(new RemoveAllActionGroups()))
                .withDescription("Remove all ActionGroups outside business hours")
                .withEnabled(true))
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

### AlertProcessingRules_Delete

```java
/**
 * Samples for AlertProcessingRules Delete.
 */
public final class AlertProcessingRulesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/stable/2021-08-08/examples/
     * AlertProcessingRules_Delete.json
     */
    /**
     * Sample code: DeleteAlertProcessingRule.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void
        deleteAlertProcessingRule(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alertProcessingRules()
            .deleteByResourceGroupWithResponse("alertscorrelationrg", "DailySuppression",
                com.azure.core.util.Context.NONE);
    }
}
```

### AlertProcessingRules_GetByResourceGroup

```java
/**
 * Samples for AlertProcessingRules GetByResourceGroup.
 */
public final class AlertProcessingRulesGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/stable/2021-08-08/examples/
     * AlertProcessingRules_GetById.json
     */
    /**
     * Sample code: GetAlertProcessingRuleById.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void
        getAlertProcessingRuleById(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alertProcessingRules()
            .getByResourceGroupWithResponse("alertscorrelationrg", "DailySuppression",
                com.azure.core.util.Context.NONE);
    }
}
```

### AlertProcessingRules_List

```java
/**
 * Samples for AlertProcessingRules List.
 */
public final class AlertProcessingRulesListSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/stable/2021-08-08/examples/
     * AlertProcessingRules_List_Subscription.json
     */
    /**
     * Sample code: GetAlertProcessingRulesSubscriptionWide.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void getAlertProcessingRulesSubscriptionWide(
        com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alertProcessingRules().list(com.azure.core.util.Context.NONE);
    }
}
```

### AlertProcessingRules_ListByResourceGroup

```java
/**
 * Samples for AlertProcessingRules ListByResourceGroup.
 */
public final class AlertProcessingRulesListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/stable/2021-08-08/examples/
     * AlertProcessingRules_List_ResourceGroup.json
     */
    /**
     * Sample code: GetAlertProcessingRulesResourceGroupWide.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void getAlertProcessingRulesResourceGroupWide(
        com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alertProcessingRules().listByResourceGroup("alertscorrelationrg", com.azure.core.util.Context.NONE);
    }
}
```

### AlertProcessingRules_Update

```java
import com.azure.resourcemanager.alertsmanagement.models.AlertProcessingRule;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AlertProcessingRules Update.
 */
public final class AlertProcessingRulesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/stable/2021-08-08/examples/
     * AlertProcessingRules_Patch.json
     */
    /**
     * Sample code: PatchAlertProcessingRule.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void
        patchAlertProcessingRule(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        AlertProcessingRule resource = manager.alertProcessingRules()
            .getByResourceGroupWithResponse("alertscorrelationrg", "WeeklySuppression",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key1", "fakeTokenPlaceholder", "key2", "fakeTokenPlaceholder"))
            .withEnabled(false)
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

### AlertRuleRecommendations_List

```java
/**
 * Samples for AlertRuleRecommendations List.
 */
public final class AlertRuleRecommendationsListSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2023-01-01-preview/examples/
     * AlertRuleRecommendations_GetBySubscription_VM.json
     */
    /**
     * Sample code: List alert rule recommendations for virtual machines at subscription level.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void listAlertRuleRecommendationsForVirtualMachinesAtSubscriptionLevel(
        com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alertRuleRecommendations().list("microsoft.compute/virtualmachines", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2023-01-01-preview/examples/
     * AlertRuleRecommendations_GetBySubscription_MAC.json
     */
    /**
     * Sample code: List alert rule recommendations for Monitoring accounts at subscription level.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void listAlertRuleRecommendationsForMonitoringAccountsAtSubscriptionLevel(
        com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alertRuleRecommendations().list("microsoft.monitor/accounts", com.azure.core.util.Context.NONE);
    }
}
```

### AlertRuleRecommendations_ListByResource

```java
/**
 * Samples for AlertRuleRecommendations ListByResource.
 */
public final class AlertRuleRecommendationsListByResourceSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2023-01-01-preview/examples/
     * AlertRuleRecommendations_GetByResource_MAC.json
     */
    /**
     * Sample code: List alert rule recommendations for Monitoring accounts at resource level.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void listAlertRuleRecommendationsForMonitoringAccountsAtResourceLevel(
        com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alertRuleRecommendations()
            .listByResource(
                "subscriptions/2f00cc51-6809-498f-9ffc-48c42aff570d/resourceGroups/GenevaAlertRP-RunnerResources-eastus/providers/microsoft.monitor/accounts/alertsrp-eastus-pgms",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2023-01-01-preview/examples/
     * AlertRuleRecommendations_GetByResource_VM.json
     */
    /**
     * Sample code: List alert rule recommendations for virtual machines at resource level.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void listAlertRuleRecommendationsForVirtualMachinesAtResourceLevel(
        com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alertRuleRecommendations()
            .listByResource(
                "subscriptions/2f00cc51-6809-498f-9ffc-48c42aff570d/resourcegroups/test/providers/Microsoft.Compute/virtualMachines/testMachineCanBeSafelyDeleted",
                com.azure.core.util.Context.NONE);
    }
}
```

### Alerts_ChangeState

```java
import com.azure.resourcemanager.alertsmanagement.models.AlertState;
import com.azure.resourcemanager.alertsmanagement.models.Comments;

/**
 * Samples for Alerts ChangeState.
 */
public final class AlertsChangeStateSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2019-05-05-preview/examples/
     * Alerts_ChangeState.json
     */
    /**
     * Sample code: Resolve.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void resolve(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alerts()
            .changeStateWithResponse("66114d64-d9d9-478b-95c9-b789d6502100", AlertState.ACKNOWLEDGED, new Comments(),
                com.azure.core.util.Context.NONE);
    }
}
```

### Alerts_GetById

```java
/**
 * Samples for Alerts GetById.
 */
public final class AlertsGetByIdSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2019-05-05-preview/examples/
     * Alerts_GetById.json
     */
    /**
     * Sample code: GetById.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void getById(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alerts().getByIdWithResponse("66114d64-d9d9-478b-95c9-b789d6502100", com.azure.core.util.Context.NONE);
    }
}
```

### Alerts_GetHistory

```java
/**
 * Samples for Alerts GetHistory.
 */
public final class AlertsGetHistorySamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2019-05-05-preview/examples/
     * Alerts_History.json
     */
    /**
     * Sample code: Resolve.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void resolve(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alerts()
            .getHistoryWithResponse("66114d64-d9d9-478b-95c9-b789d6502100", com.azure.core.util.Context.NONE);
    }
}
```

### Alerts_GetSummary

```java
import com.azure.resourcemanager.alertsmanagement.models.AlertsSummaryGroupByFields;

/**
 * Samples for Alerts GetSummary.
 */
public final class AlertsGetSummarySamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2019-05-05-preview/examples/
     * Alerts_Summary.json
     */
    /**
     * Sample code: Summary.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void summary(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alerts()
            .getSummaryWithResponse(AlertsSummaryGroupByFields.fromString("severity,alertState"), null, null, null,
                null, null, null, null, null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Alerts_List

```java

/**
 * Samples for Alerts List.
 */
public final class AlertsListSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2019-05-05-preview/examples/
     * Alerts_List.json
     */
    /**
     * Sample code: ListAlerts.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void listAlerts(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alerts()
            .list(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Alerts_Metadata

```java
import com.azure.resourcemanager.alertsmanagement.models.Identifier;

/**
 * Samples for Alerts Metadata.
 */
public final class AlertsMetadataSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2019-05-05-preview/examples/
     * AlertsMetaData_MonitorService.json
     */
    /**
     * Sample code: MonService.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void monService(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alerts().metadataWithResponse(Identifier.MONITOR_SERVICE_LIST, com.azure.core.util.Context.NONE);
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
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2019-05-05-preview/examples/
     * Operations_List.json
     */
    /**
     * Sample code: ListOperations.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void listOperations(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrometheusRuleGroups_CreateOrUpdate

```java
import com.azure.resourcemanager.alertsmanagement.models.PrometheusRule;
import com.azure.resourcemanager.alertsmanagement.models.PrometheusRuleGroupAction;
import com.azure.resourcemanager.alertsmanagement.models.PrometheusRuleResolveConfiguration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for PrometheusRuleGroups CreateOrUpdate.
 */
public final class PrometheusRuleGroupsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2021-07-22-preview/examples/
     * createOrUpdatePrometheusRuleGroup.json
     */
    /**
     * Sample code: CreatePrometheusRuleGroup.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void
        createPrometheusRuleGroup(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.prometheusRuleGroups()
            .define("myPrometheusRuleGroup")
            .withRegion("East US")
            .withExistingResourceGroup("promResourceGroup")
            .withScopes(Arrays.asList(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/promResourceGroup/providers/microsoft.monitor/accounts/myMonitoringAccount"))
            .withRules(Arrays.asList(new PrometheusRule().withRecord("job_type:billing_jobs_duration_seconds:99p5m")
                .withExpression(
                    "histogram_quantile(0.99, sum(rate(jobs_duration_seconds_bucket{service=\"billing-processing\"}[5m])) by (job_type))")
                .withLabels(mapOf("team", "prod")),
                new PrometheusRule().withAlert("Billing_Processing_Very_Slow")
                    .withExpression("job_type:billing_jobs_duration_seconds:99p5m > 30")
                    .withSeverity(2)
                    .withForProperty("PT5M")
                    .withLabels(mapOf("team", "prod"))
                    .withAnnotations(mapOf("annotationName1", "annotationValue1"))
                    .withActions(Arrays.asList(new PrometheusRuleGroupAction().withActionGroupId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourcegroups/promResourceGroup/providers/microsoft.insights/actiongroups/group2")
                        .withActionProperties(mapOf("key11", "fakeTokenPlaceholder", "key12", "fakeTokenPlaceholder"))))
                    .withResolveConfiguration(
                        new PrometheusRuleResolveConfiguration().withAutoResolved(true).withTimeToResolve("PT10M"))))
            .withDescription("This is the description of the first rule group")
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

### PrometheusRuleGroups_Delete

```java
/**
 * Samples for PrometheusRuleGroups Delete.
 */
public final class PrometheusRuleGroupsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2021-07-22-preview/examples/
     * deletePrometheusRuleGroup.json
     */
    /**
     * Sample code: DeletePrometheusRuleGroup.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void
        deletePrometheusRuleGroup(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.prometheusRuleGroups()
            .deleteByResourceGroupWithResponse("promResourceGroup", "myPrometheusRuleGroup",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrometheusRuleGroups_GetByResourceGroup

```java
/**
 * Samples for PrometheusRuleGroups GetByResourceGroup.
 */
public final class PrometheusRuleGroupsGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2021-07-22-preview/examples/
     * getPrometheusRuleGroup.json
     */
    /**
     * Sample code: GetPrometheusRuleGroup.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void
        getPrometheusRuleGroup(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.prometheusRuleGroups()
            .getByResourceGroupWithResponse("promResourceGroup", "myPrometheusRuleGroup",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrometheusRuleGroups_List

```java
/**
 * Samples for PrometheusRuleGroups List.
 */
public final class PrometheusRuleGroupsListSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2021-07-22-preview/examples/
     * listSubscriptionPrometheusRuleGroups.json
     */
    /**
     * Sample code: ListSubscriptionResourcePrometheusRuleGroups.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void listSubscriptionResourcePrometheusRuleGroups(
        com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.prometheusRuleGroups().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrometheusRuleGroups_ListByResourceGroup

```java
/**
 * Samples for PrometheusRuleGroups ListByResourceGroup.
 */
public final class PrometheusRuleGroupsListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2021-07-22-preview/examples/
     * listPrometheusRuleGroups.json
     */
    /**
     * Sample code: ListResourcePrometheusRuleGroups.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void
        listResourcePrometheusRuleGroups(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.prometheusRuleGroups().listByResourceGroup("promResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### PrometheusRuleGroups_Update

```java
import com.azure.resourcemanager.alertsmanagement.models.PrometheusRuleGroupResource;
import com.azure.resourcemanager.alertsmanagement.models.PrometheusRuleGroupResourcePatchProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for PrometheusRuleGroups Update.
 */
public final class PrometheusRuleGroupsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2021-07-22-preview/examples/
     * patchPrometheusRuleGroup.json
     */
    /**
     * Sample code: PatchPrometheusRuleGroup.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void
        patchPrometheusRuleGroup(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        PrometheusRuleGroupResource resource = manager.prometheusRuleGroups()
            .getByResourceGroupWithResponse("promResourceGroup", "myPrometheusRuleGroup",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("tag1", "value1"))
            .withProperties(new PrometheusRuleGroupResourcePatchProperties().withEnabled(false))
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

### SmartGroups_ChangeState

```java
import com.azure.resourcemanager.alertsmanagement.models.AlertState;

/**
 * Samples for SmartGroups ChangeState.
 */
public final class SmartGroupsChangeStateSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2019-05-05-preview/examples/
     * SmartGroups_ChangeState.json
     */
    /**
     * Sample code: changestate.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void changestate(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.smartGroups()
            .changeStateWithResponse("a808445e-bb38-4751-85c2-1b109ccc1059", AlertState.ACKNOWLEDGED,
                com.azure.core.util.Context.NONE);
    }
}
```

### SmartGroups_GetById

```java
/**
 * Samples for SmartGroups GetById.
 */
public final class SmartGroupsGetByIdSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2019-05-05-preview/examples/
     * SmartGroups_GetById.json
     */
    /**
     * Sample code: Get.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void get(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.smartGroups()
            .getByIdWithResponse("603675da-9851-4b26-854a-49fc53d32715", com.azure.core.util.Context.NONE);
    }
}
```

### SmartGroups_GetHistory

```java
/**
 * Samples for SmartGroups GetHistory.
 */
public final class SmartGroupsGetHistorySamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2019-05-05-preview/examples/
     * SmartGroups_History.json
     */
    /**
     * Sample code: Resolve.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void resolve(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.smartGroups()
            .getHistoryWithResponse("a808445e-bb38-4751-85c2-1b109ccc1059", com.azure.core.util.Context.NONE);
    }
}
```

### SmartGroups_List

```java

/**
 * Samples for SmartGroups List.
 */
public final class SmartGroupsListSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2019-05-05-preview/examples/
     * SmartGroups_List.json
     */
    /**
     * Sample code: List.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void list(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.smartGroups()
            .list(null, null, null, null, null, null, null, null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### TenantActivityLogAlerts_CreateOrUpdate

```java
import com.azure.resourcemanager.alertsmanagement.fluent.models.TenantActivityLogAlertResourceInner;
import com.azure.resourcemanager.alertsmanagement.models.ActionGroup;
import com.azure.resourcemanager.alertsmanagement.models.ActionList;
import com.azure.resourcemanager.alertsmanagement.models.AlertRuleAllOfCondition;
import com.azure.resourcemanager.alertsmanagement.models.AlertRuleAnyOfOrLeafCondition;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for TenantActivityLogAlerts CreateOrUpdate.
 */
public final class TenantActivityLogAlertsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2023-04-01-preview/examples/
     * TenantActivityLogAlertRule_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update a Tenant Activity Log Alert rule for tenant level events.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void createOrUpdateATenantActivityLogAlertRuleForTenantLevelEvents(
        com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.tenantActivityLogAlerts()
            .createOrUpdateWithResponse("72f988bf-86f1-41af-91ab-2d7cd011db47",
                "SampleActivityLogAlertSHRuleOnTenantLevel",
                new TenantActivityLogAlertResourceInner().withLocation("Global")
                    .withTags(mapOf())
                    .withTenantScope("72f988bf-86f1-41af-91ab-2d7cd011db47")
                    .withCondition(new AlertRuleAllOfCondition().withAllOf(Arrays
                        .asList(new AlertRuleAnyOfOrLeafCondition().withField("category").withEquals("ServiceHealth"))))
                    .withActions(new ActionList().withActionGroups(Arrays.asList(new ActionGroup().withActionGroupId(
                        "/providers/Microsoft.Management/ManagementGroups/72f988bf-86f1-41af-91ab-2d7cd011db47/providers/Microsoft.Insights/actionGroups/SampleActionGroup")
                        .withWebhookProperties(mapOf("sampleWebhookProperty", "SamplePropertyValue"))
                        .withActionProperties(mapOf("Email.Title", "my email title")))))
                    .withEnabled(true)
                    .withDescription(
                        "Description of sample Activity Log Alert service health rule on tenant level events."),
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

### TenantActivityLogAlerts_Delete

```java
/**
 * Samples for TenantActivityLogAlerts Delete.
 */
public final class TenantActivityLogAlertsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2023-04-01-preview/examples/
     * TenantActivityLogAlertRule_DeleteRule.json
     */
    /**
     * Sample code: Delete a Tenant Activity Log Alert rule.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void
        deleteATenantActivityLogAlertRule(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.tenantActivityLogAlerts()
            .deleteByResourceGroupWithResponse("72f988bf-86f1-41af-91ab-2d7cd011db47",
                "SampleActivityLogAlertSHRuleOnTenantLevel", com.azure.core.util.Context.NONE);
    }
}
```

### TenantActivityLogAlerts_Get

```java
/**
 * Samples for TenantActivityLogAlerts Get.
 */
public final class TenantActivityLogAlertsGetSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2023-04-01-preview/examples/
     * TenantActivityLogAlertRule_GetRule.json
     */
    /**
     * Sample code: Get a Tenant Activity Log Alert rule.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void
        getATenantActivityLogAlertRule(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.tenantActivityLogAlerts()
            .getWithResponse("72f988bf-86f1-41af-91ab-2d7cd011db47", "SampleActivityLogAlertSHRuleOnTenantLevel",
                com.azure.core.util.Context.NONE);
    }
}
```

### TenantActivityLogAlerts_ListByManagementGroup

```java
/**
 * Samples for TenantActivityLogAlerts ListByManagementGroup.
 */
public final class TenantActivityLogAlertsListByManagementGroupSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2023-04-01-preview/examples/
     * TenantActivityLogAlertRule_ListByManagementGroup.json
     */
    /**
     * Sample code: List Activity Log Alerts by management group.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void listActivityLogAlertsByManagementGroup(
        com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.tenantActivityLogAlerts()
            .listByManagementGroup("72f988bf-86f1-41af-91ab-2d7cd011db47", com.azure.core.util.Context.NONE);
    }
}
```

### TenantActivityLogAlerts_ListByTenant

```java
/**
 * Samples for TenantActivityLogAlerts ListByTenant.
 */
public final class TenantActivityLogAlertsListByTenantSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2023-04-01-preview/examples/
     * TenantActivityLogAlertRule_ListByTenant.json
     */
    /**
     * Sample code: List Activity Log Alerts by tenant.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void
        listActivityLogAlertsByTenant(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.tenantActivityLogAlerts().listByTenant(com.azure.core.util.Context.NONE);
    }
}
```

### TenantActivityLogAlerts_Update

```java
import com.azure.resourcemanager.alertsmanagement.models.TenantAlertRulePatchObject;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for TenantActivityLogAlerts Update.
 */
public final class TenantActivityLogAlertsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2023-04-01-preview/examples/
     * TenantActivityLogAlertRule_UpdateRule.json
     */
    /**
     * Sample code: Patch a Tenant Activity Log Alert rule.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void
        patchATenantActivityLogAlertRule(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.tenantActivityLogAlerts()
            .updateWithResponse("72f988bf-86f1-41af-91ab-2d7cd011db47", "SampleActivityLogAlertSHRuleOnTenantLevel",
                new TenantAlertRulePatchObject()
                    .withTags(mapOf("key1", "fakeTokenPlaceholder", "key2", "fakeTokenPlaceholder"))
                    .withEnabled(false),
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

