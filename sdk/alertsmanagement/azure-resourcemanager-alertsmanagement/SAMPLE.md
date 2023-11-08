# Code snippets and samples


## AlertProcessingRules

- [CreateOrUpdate](#alertprocessingrules_createorupdate)
- [Delete](#alertprocessingrules_delete)
- [GetByResourceGroup](#alertprocessingrules_getbyresourcegroup)
- [List](#alertprocessingrules_list)
- [ListByResourceGroup](#alertprocessingrules_listbyresourcegroup)
- [Update](#alertprocessingrules_update)

## Alerts

- [ChangeState](#alerts_changestate)
- [GetById](#alerts_getbyid)
- [GetHistory](#alerts_gethistory)
- [GetSummary](#alerts_getsummary)
- [List](#alerts_list)
- [Metadata](#alerts_metadata)

## Operations

- [List](#operations_list)

## SmartGroups

- [ChangeState](#smartgroups_changestate)
- [GetById](#smartgroups_getbyid)
- [GetHistory](#smartgroups_gethistory)
- [List](#smartgroups_list)
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

/** Samples for AlertProcessingRules CreateOrUpdate. */
public final class AlertProcessingRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/stable/2021-08-08/examples/AlertProcessingRules_Create_or_update_remove_all_action_groups_recurring_maintenance_window.json
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
        manager
            .alertProcessingRules()
            .define("RemoveActionGroupsRecurringMaintenance")
            .withRegion("Global")
            .withExistingResourceGroup("alertscorrelationrg")
            .withTags(mapOf())
            .withProperties(
                new AlertProcessingRuleProperties()
                    .withScopes(
                        Arrays
                            .asList(
                                "/subscriptions/subId1/resourceGroups/RGId1",
                                "/subscriptions/subId1/resourceGroups/RGId2"))
                    .withConditions(
                        Arrays
                            .asList(
                                new Condition()
                                    .withField(Field.TARGET_RESOURCE_TYPE)
                                    .withOperator(Operator.EQUALS)
                                    .withValues(Arrays.asList("microsoft.compute/virtualmachines"))))
                    .withSchedule(
                        new Schedule()
                            .withTimeZone("India Standard Time")
                            .withRecurrences(
                                Arrays
                                    .asList(
                                        new WeeklyRecurrence()
                                            .withStartTime("22:00:00")
                                            .withEndTime("04:00:00")
                                            .withDaysOfWeek(Arrays.asList(DaysOfWeek.SATURDAY, DaysOfWeek.SUNDAY)))))
                    .withActions(Arrays.asList(new RemoveAllActionGroups()))
                    .withDescription(
                        "Remove all ActionGroups from all Vitual machine Alerts during the recurring maintenance")
                    .withEnabled(true))
            .create();
    }

    /*
     * x-ms-original-file: specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/stable/2021-08-08/examples/AlertProcessingRules_Create_or_update_add_two_action_groups_all_Sev0_Sev1_two_resource_groups.json
     */
    /**
     * Sample code: Create or update a rule that adds two action groups to all Sev0 and Sev1 alerts in two resource
     * groups.
     *
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void createOrUpdateARuleThatAddsTwoActionGroupsToAllSev0AndSev1AlertsInTwoResourceGroups(
        com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager
            .alertProcessingRules()
            .define("AddActionGroupsBySeverity")
            .withRegion("Global")
            .withExistingResourceGroup("alertscorrelationrg")
            .withTags(mapOf())
            .withProperties(
                new AlertProcessingRuleProperties()
                    .withScopes(
                        Arrays
                            .asList(
                                "/subscriptions/subId1/resourceGroups/RGId1",
                                "/subscriptions/subId1/resourceGroups/RGId2"))
                    .withConditions(
                        Arrays
                            .asList(
                                new Condition()
                                    .withField(Field.SEVERITY)
                                    .withOperator(Operator.EQUALS)
                                    .withValues(Arrays.asList("sev0", "sev1"))))
                    .withActions(
                        Arrays
                            .asList(
                                new AddActionGroups()
                                    .withActionGroupIds(
                                        Arrays
                                            .asList(
                                                "/subscriptions/subId1/resourcegroups/RGId1/providers/microsoft.insights/actiongroups/AGId1",
                                                "/subscriptions/subId1/resourcegroups/RGId1/providers/microsoft.insights/actiongroups/AGId2"))))
                    .withDescription("Add AGId1 and AGId2 to all Sev0 and Sev1 alerts in these resourceGroups")
                    .withEnabled(true))
            .create();
    }

    /*
     * x-ms-original-file: specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/stable/2021-08-08/examples/AlertProcessingRules_Create_or_update_add_action_group_all_alerts_in_subscription.json
     */
    /**
     * Sample code: Create or update a rule that adds an action group to all alerts in a subscription.
     *
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void createOrUpdateARuleThatAddsAnActionGroupToAllAlertsInASubscription(
        com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager
            .alertProcessingRules()
            .define("AddActionGroupToSubscription")
            .withRegion("Global")
            .withExistingResourceGroup("alertscorrelationrg")
            .withTags(mapOf())
            .withProperties(
                new AlertProcessingRuleProperties()
                    .withScopes(Arrays.asList("/subscriptions/subId1"))
                    .withActions(
                        Arrays
                            .asList(
                                new AddActionGroups()
                                    .withActionGroupIds(
                                        Arrays
                                            .asList(
                                                "/subscriptions/subId1/resourcegroups/RGId1/providers/microsoft.insights/actiongroups/ActionGroup1"))))
                    .withDescription("Add ActionGroup1 to all alerts in the subscription")
                    .withEnabled(true))
            .create();
    }

    /*
     * x-ms-original-file: specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/stable/2021-08-08/examples/AlertProcessingRules_Create_or_update_remove_all_action_groups_specific_VM_one-off_maintenance_window.json
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
        manager
            .alertProcessingRules()
            .define("RemoveActionGroupsMaintenanceWindow")
            .withRegion("Global")
            .withExistingResourceGroup("alertscorrelationrg")
            .withTags(mapOf())
            .withProperties(
                new AlertProcessingRuleProperties()
                    .withScopes(
                        Arrays
                            .asList(
                                "/subscriptions/subId1/resourceGroups/RGId1/providers/Microsoft.Compute/virtualMachines/VMName"))
                    .withSchedule(
                        new Schedule()
                            .withEffectiveFrom("2021-04-15T18:00:00")
                            .withEffectiveUntil("2021-04-15T20:00:00")
                            .withTimeZone("Pacific Standard Time"))
                    .withActions(Arrays.asList(new RemoveAllActionGroups()))
                    .withDescription("Removes all ActionGroups from all Alerts on VMName during the maintenance window")
                    .withEnabled(true))
            .create();
    }

    /*
     * x-ms-original-file: specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/stable/2021-08-08/examples/AlertProcessingRules_Create_or_update_remove_all_action_groups_from_specific_alert_rule.json
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
        manager
            .alertProcessingRules()
            .define("RemoveActionGroupsSpecificAlertRule")
            .withRegion("Global")
            .withExistingResourceGroup("alertscorrelationrg")
            .withTags(mapOf())
            .withProperties(
                new AlertProcessingRuleProperties()
                    .withScopes(Arrays.asList("/subscriptions/subId1"))
                    .withConditions(
                        Arrays
                            .asList(
                                new Condition()
                                    .withField(Field.ALERT_RULE_ID)
                                    .withOperator(Operator.EQUALS)
                                    .withValues(
                                        Arrays
                                            .asList(
                                                "/subscriptions/suubId1/resourceGroups/Rgid2/providers/microsoft.insights/activityLogAlerts/RuleName"))))
                    .withActions(Arrays.asList(new RemoveAllActionGroups()))
                    .withDescription("Removes all ActionGroups from all Alerts that fire on above AlertRule")
                    .withEnabled(true))
            .create();
    }

    /*
     * x-ms-original-file: specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/stable/2021-08-08/examples/AlertProcessingRules_Create_or_update_remove_all_action_groups_outside_business_hours.json
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
        manager
            .alertProcessingRules()
            .define("RemoveActionGroupsOutsideBusinessHours")
            .withRegion("Global")
            .withExistingResourceGroup("alertscorrelationrg")
            .withTags(mapOf())
            .withProperties(
                new AlertProcessingRuleProperties()
                    .withScopes(Arrays.asList("/subscriptions/subId1"))
                    .withSchedule(
                        new Schedule()
                            .withTimeZone("Eastern Standard Time")
                            .withRecurrences(
                                Arrays
                                    .asList(
                                        new DailyRecurrence().withStartTime("17:00:00").withEndTime("09:00:00"),
                                        new WeeklyRecurrence()
                                            .withDaysOfWeek(Arrays.asList(DaysOfWeek.SATURDAY, DaysOfWeek.SUNDAY)))))
                    .withActions(Arrays.asList(new RemoveAllActionGroups()))
                    .withDescription("Remove all ActionGroups outside business hours")
                    .withEnabled(true))
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

### AlertProcessingRules_Delete

```java
import com.azure.core.util.Context;

/** Samples for AlertProcessingRules Delete. */
public final class AlertProcessingRulesDeleteSamples {
    /*
     * x-ms-original-file: specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/stable/2021-08-08/examples/AlertProcessingRules_Delete.json
     */
    /**
     * Sample code: DeleteAlertProcessingRule.
     *
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void deleteAlertProcessingRule(
        com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alertProcessingRules().deleteWithResponse("alertscorrelationrg", "DailySuppression", Context.NONE);
    }
}
```

### AlertProcessingRules_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for AlertProcessingRules GetByResourceGroup. */
public final class AlertProcessingRulesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/stable/2021-08-08/examples/AlertProcessingRules_GetById.json
     */
    /**
     * Sample code: GetAlertProcessingRuleById.
     *
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void getAlertProcessingRuleById(
        com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager
            .alertProcessingRules()
            .getByResourceGroupWithResponse("alertscorrelationrg", "DailySuppression", Context.NONE);
    }
}
```

### AlertProcessingRules_List

```java
import com.azure.core.util.Context;

/** Samples for AlertProcessingRules List. */
public final class AlertProcessingRulesListSamples {
    /*
     * x-ms-original-file: specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/stable/2021-08-08/examples/AlertProcessingRules_List_Subscription.json
     */
    /**
     * Sample code: GetAlertProcessingRulesSubscriptionWide.
     *
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void getAlertProcessingRulesSubscriptionWide(
        com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alertProcessingRules().list(Context.NONE);
    }
}
```

### AlertProcessingRules_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for AlertProcessingRules ListByResourceGroup. */
public final class AlertProcessingRulesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/stable/2021-08-08/examples/AlertProcessingRules_List_ResourceGroup.json
     */
    /**
     * Sample code: GetAlertProcessingRulesResourceGroupWide.
     *
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void getAlertProcessingRulesResourceGroupWide(
        com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alertProcessingRules().listByResourceGroup("alertscorrelationrg", Context.NONE);
    }
}
```

### AlertProcessingRules_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.alertsmanagement.models.AlertProcessingRule;
import java.util.HashMap;
import java.util.Map;

/** Samples for AlertProcessingRules Update. */
public final class AlertProcessingRulesUpdateSamples {
    /*
     * x-ms-original-file: specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/stable/2021-08-08/examples/AlertProcessingRules_Patch.json
     */
    /**
     * Sample code: PatchAlertProcessingRule.
     *
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void patchAlertProcessingRule(
        com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        AlertProcessingRule resource =
            manager
                .alertProcessingRules()
                .getByResourceGroupWithResponse("alertscorrelationrg", "WeeklySuppression", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("key1", "value1", "key2", "value2")).withEnabled(false).apply();
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

### Alerts_ChangeState

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.alertsmanagement.models.AlertState;

/** Samples for Alerts ChangeState. */
public final class AlertsChangeStateSamples {
    /*
     * x-ms-original-file: specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2019-05-05-preview/examples/Alerts_ChangeState.json
     */
    /**
     * Sample code: Resolve.
     *
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void resolve(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager
            .alerts()
            .changeStateWithResponse(
                "66114d64-d9d9-478b-95c9-b789d6502100", AlertState.ACKNOWLEDGED, null, Context.NONE);
    }
}
```

### Alerts_GetById

```java
import com.azure.core.util.Context;

/** Samples for Alerts GetById. */
public final class AlertsGetByIdSamples {
    /*
     * x-ms-original-file: specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2019-05-05-preview/examples/Alerts_GetById.json
     */
    /**
     * Sample code: GetById.
     *
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void getById(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alerts().getByIdWithResponse("66114d64-d9d9-478b-95c9-b789d6502100", Context.NONE);
    }
}
```

### Alerts_GetHistory

```java
import com.azure.core.util.Context;

/** Samples for Alerts GetHistory. */
public final class AlertsGetHistorySamples {
    /*
     * x-ms-original-file: specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2019-05-05-preview/examples/Alerts_History.json
     */
    /**
     * Sample code: Resolve.
     *
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void resolve(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alerts().getHistoryWithResponse("66114d64-d9d9-478b-95c9-b789d6502100", Context.NONE);
    }
}
```

### Alerts_GetSummary

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.alertsmanagement.models.AlertsSummaryGroupByFields;

/** Samples for Alerts GetSummary. */
public final class AlertsGetSummarySamples {
    /*
     * x-ms-original-file: specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2019-05-05-preview/examples/Alerts_Summary.json
     */
    /**
     * Sample code: Summary.
     *
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void summary(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager
            .alerts()
            .getSummaryWithResponse(
                AlertsSummaryGroupByFields.fromString("severity,alertState"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Context.NONE);
    }
}
```

### Alerts_List

```java
import com.azure.core.util.Context;

/** Samples for Alerts List. */
public final class AlertsListSamples {
    /*
     * x-ms-original-file: specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2019-05-05-preview/examples/Alerts_List.json
     */
    /**
     * Sample code: ListAlerts.
     *
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void listAlerts(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager
            .alerts()
            .list(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Context.NONE);
    }
}
```

### Alerts_Metadata

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.alertsmanagement.models.Identifier;

/** Samples for Alerts Metadata. */
public final class AlertsMetadataSamples {
    /*
     * x-ms-original-file: specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2019-05-05-preview/examples/AlertsMetaData_MonitorService.json
     */
    /**
     * Sample code: MonService.
     *
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void monService(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alerts().metadataWithResponse(Identifier.MONITOR_SERVICE_LIST, Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2019-05-05-preview/examples/Operations_List.json
     */
    /**
     * Sample code: ListOperations.
     *
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void listOperations(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### SmartGroups_ChangeState

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.alertsmanagement.models.AlertState;

/** Samples for SmartGroups ChangeState. */
public final class SmartGroupsChangeStateSamples {
    /*
     * x-ms-original-file: specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2019-05-05-preview/examples/SmartGroups_ChangeState.json
     */
    /**
     * Sample code: changestate.
     *
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void changestate(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager
            .smartGroups()
            .changeStateWithResponse("a808445e-bb38-4751-85c2-1b109ccc1059", AlertState.ACKNOWLEDGED, Context.NONE);
    }
}
```

### SmartGroups_GetById

```java
import com.azure.core.util.Context;

/** Samples for SmartGroups GetById. */
public final class SmartGroupsGetByIdSamples {
    /*
     * x-ms-original-file: specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2019-05-05-preview/examples/SmartGroups_GetById.json
     */
    /**
     * Sample code: Get.
     *
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void get(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.smartGroups().getByIdWithResponse("603675da-9851-4b26-854a-49fc53d32715", Context.NONE);
    }
}
```

### SmartGroups_GetHistory

```java
import com.azure.core.util.Context;

/** Samples for SmartGroups GetHistory. */
public final class SmartGroupsGetHistorySamples {
    /*
     * x-ms-original-file: specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2019-05-05-preview/examples/SmartGroups_History.json
     */
    /**
     * Sample code: Resolve.
     *
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void resolve(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.smartGroups().getHistoryWithResponse("a808445e-bb38-4751-85c2-1b109ccc1059", Context.NONE);
    }
}
```

### SmartGroups_List

```java
import com.azure.core.util.Context;

/** Samples for SmartGroups List. */
public final class SmartGroupsListSamples {
    /*
     * x-ms-original-file: specification/alertsmanagement/resource-manager/Microsoft.AlertsManagement/preview/2019-05-05-preview/examples/SmartGroups_List.json
     */
    /**
     * Sample code: List.
     *
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void list(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.smartGroups().list(null, null, null, null, null, null, null, null, null, null, null, Context.NONE);
    }
}
```

