# Code snippets and samples


## ActionGroups

- [CreateNotificationsAtResourceGroupLevel](#actiongroups_createnotificationsatresourcegrouplevel)
- [CreateOrUpdate](#actiongroups_createorupdate)
- [Delete](#actiongroups_delete)
- [EnableReceiver](#actiongroups_enablereceiver)
- [GetByResourceGroup](#actiongroups_getbyresourcegroup)
- [GetTestNotifications](#actiongroups_gettestnotifications)
- [GetTestNotificationsAtResourceGroupLevel](#actiongroups_gettestnotificationsatresourcegrouplevel)
- [List](#actiongroups_list)
- [ListByResourceGroup](#actiongroups_listbyresourcegroup)
- [PostTestNotifications](#actiongroups_posttestnotifications)
- [Update](#actiongroups_update)

## ActivityLogAlerts

- [CreateOrUpdate](#activitylogalerts_createorupdate)
- [Delete](#activitylogalerts_delete)
- [GetByResourceGroup](#activitylogalerts_getbyresourcegroup)
- [List](#activitylogalerts_list)
- [ListByResourceGroup](#activitylogalerts_listbyresourcegroup)
- [Update](#activitylogalerts_update)

## ActivityLogs

- [List](#activitylogs_list)

## AlertRuleIncidents

- [Get](#alertruleincidents_get)
- [ListByAlertRule](#alertruleincidents_listbyalertrule)

## AlertRules

- [CreateOrUpdate](#alertrules_createorupdate)
- [Delete](#alertrules_delete)
- [GetByResourceGroup](#alertrules_getbyresourcegroup)
- [List](#alertrules_list)
- [ListByResourceGroup](#alertrules_listbyresourcegroup)
- [Update](#alertrules_update)

## AutoscaleSettings

- [CreateOrUpdate](#autoscalesettings_createorupdate)
- [Delete](#autoscalesettings_delete)
- [GetByResourceGroup](#autoscalesettings_getbyresourcegroup)
- [List](#autoscalesettings_list)
- [ListByResourceGroup](#autoscalesettings_listbyresourcegroup)
- [Update](#autoscalesettings_update)

## Baselines

- [List](#baselines_list)

## DataCollectionEndpoints

- [Create](#datacollectionendpoints_create)
- [Delete](#datacollectionendpoints_delete)
- [GetByResourceGroup](#datacollectionendpoints_getbyresourcegroup)
- [List](#datacollectionendpoints_list)
- [ListByResourceGroup](#datacollectionendpoints_listbyresourcegroup)
- [Update](#datacollectionendpoints_update)

## DataCollectionRuleAssociations

- [Create](#datacollectionruleassociations_create)
- [Delete](#datacollectionruleassociations_delete)
- [Get](#datacollectionruleassociations_get)
- [ListByDataCollectionEndpoint](#datacollectionruleassociations_listbydatacollectionendpoint)
- [ListByResource](#datacollectionruleassociations_listbyresource)
- [ListByRule](#datacollectionruleassociations_listbyrule)

## DataCollectionRules

- [Create](#datacollectionrules_create)
- [Delete](#datacollectionrules_delete)
- [GetByResourceGroup](#datacollectionrules_getbyresourcegroup)
- [List](#datacollectionrules_list)
- [ListByResourceGroup](#datacollectionrules_listbyresourcegroup)
- [Update](#datacollectionrules_update)

## DiagnosticSettingsCategory

- [Get](#diagnosticsettingscategory_get)
- [List](#diagnosticsettingscategory_list)

## DiagnosticSettingsOperation

- [CreateOrUpdate](#diagnosticsettingsoperation_createorupdate)
- [Delete](#diagnosticsettingsoperation_delete)
- [Get](#diagnosticsettingsoperation_get)
- [List](#diagnosticsettingsoperation_list)

## EventCategories

- [List](#eventcategories_list)

## LogProfiles

- [CreateOrUpdate](#logprofiles_createorupdate)
- [Delete](#logprofiles_delete)
- [Get](#logprofiles_get)
- [List](#logprofiles_list)
- [Update](#logprofiles_update)

## MetricAlerts

- [CreateOrUpdate](#metricalerts_createorupdate)
- [Delete](#metricalerts_delete)
- [GetByResourceGroup](#metricalerts_getbyresourcegroup)
- [List](#metricalerts_list)
- [ListByResourceGroup](#metricalerts_listbyresourcegroup)
- [Update](#metricalerts_update)

## MetricAlertsStatus

- [List](#metricalertsstatus_list)
- [ListByName](#metricalertsstatus_listbyname)

## MetricDefinitions

- [List](#metricdefinitions_list)

## MetricNamespaces

- [List](#metricnamespaces_list)

## Metrics

- [List](#metrics_list)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByPrivateLinkScope](#privateendpointconnections_listbyprivatelinkscope)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [ListByPrivateLinkScope](#privatelinkresources_listbyprivatelinkscope)

## PrivateLinkScopeOperationStatus

- [GetByResourceGroup](#privatelinkscopeoperationstatus_getbyresourcegroup)

## PrivateLinkScopedResources

- [CreateOrUpdate](#privatelinkscopedresources_createorupdate)
- [Delete](#privatelinkscopedresources_delete)
- [Get](#privatelinkscopedresources_get)
- [ListByPrivateLinkScope](#privatelinkscopedresources_listbyprivatelinkscope)

## PrivateLinkScopes

- [CreateOrUpdate](#privatelinkscopes_createorupdate)
- [Delete](#privatelinkscopes_delete)
- [GetByResourceGroup](#privatelinkscopes_getbyresourcegroup)
- [List](#privatelinkscopes_list)
- [ListByResourceGroup](#privatelinkscopes_listbyresourcegroup)
- [UpdateTags](#privatelinkscopes_updatetags)

## ScheduledQueryRules

- [CreateOrUpdate](#scheduledqueryrules_createorupdate)
- [Delete](#scheduledqueryrules_delete)
- [GetByResourceGroup](#scheduledqueryrules_getbyresourcegroup)
- [List](#scheduledqueryrules_list)
- [ListByResourceGroup](#scheduledqueryrules_listbyresourcegroup)
- [Update](#scheduledqueryrules_update)

## TenantActivityLogs

- [List](#tenantactivitylogs_list)

## VMInsights

- [GetOnboardingStatus](#vminsights_getonboardingstatus)
### ActionGroups_CreateNotificationsAtResourceGroupLevel

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.monitor.generated.models.ArmRoleReceiver;
import com.azure.resourcemanager.monitor.generated.models.AutomationRunbookReceiver;
import com.azure.resourcemanager.monitor.generated.models.AzureAppPushReceiver;
import com.azure.resourcemanager.monitor.generated.models.AzureFunctionReceiver;
import com.azure.resourcemanager.monitor.generated.models.EmailReceiver;
import com.azure.resourcemanager.monitor.generated.models.EventHubReceiver;
import com.azure.resourcemanager.monitor.generated.models.ItsmReceiver;
import com.azure.resourcemanager.monitor.generated.models.LogicAppReceiver;
import com.azure.resourcemanager.monitor.generated.models.NotificationRequestBody;
import com.azure.resourcemanager.monitor.generated.models.SmsReceiver;
import com.azure.resourcemanager.monitor.generated.models.VoiceReceiver;
import com.azure.resourcemanager.monitor.generated.models.WebhookReceiver;
import java.util.Arrays;

/** Samples for ActionGroups CreateNotificationsAtResourceGroupLevel. */
public final class ActionGroupsCreateNotificationsAtResourceGroupLevelSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/postTestNotificationsAtResourceGroupLevel.json
     */
    /**
     * Sample code: Create notifications at resource group level.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createNotificationsAtResourceGroupLevel(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .actionGroups()
            .createNotificationsAtResourceGroupLevel(
                "Default-TestNotifications",
                new NotificationRequestBody()
                    .withAlertType("budget")
                    .withEmailReceivers(
                        Arrays
                            .asList(
                                new EmailReceiver()
                                    .withName("John Doe's email")
                                    .withEmailAddress("johndoe@email.com")
                                    .withUseCommonAlertSchema(false),
                                new EmailReceiver()
                                    .withName("Jane Smith's email")
                                    .withEmailAddress("janesmith@email.com")
                                    .withUseCommonAlertSchema(true)))
                    .withSmsReceivers(
                        Arrays
                            .asList(
                                new SmsReceiver()
                                    .withName("John Doe's mobile")
                                    .withCountryCode("1")
                                    .withPhoneNumber("1234567890"),
                                new SmsReceiver()
                                    .withName("Jane Smith's mobile")
                                    .withCountryCode("1")
                                    .withPhoneNumber("0987654321")))
                    .withWebhookReceivers(
                        Arrays
                            .asList(
                                new WebhookReceiver()
                                    .withName("Sample webhook 1")
                                    .withServiceUri("http://www.example.com/webhook1")
                                    .withUseCommonAlertSchema(true),
                                new WebhookReceiver()
                                    .withName("Sample webhook 2")
                                    .withServiceUri("http://www.example.com/webhook2")
                                    .withUseCommonAlertSchema(true)
                                    .withUseAadAuth(true)
                                    .withObjectId("d3bb868c-fe44-452c-aa26-769a6538c808")
                                    .withIdentifierUri("http://someidentifier/d7811ba3-7996-4a93-99b6-6b2f3f355f8a")
                                    .withTenantId("68a4459a-ccb8-493c-b9da-dd30457d1b84")))
                    .withItsmReceivers(
                        Arrays
                            .asList(
                                new ItsmReceiver()
                                    .withName("Sample itsm")
                                    .withWorkspaceId(
                                        "5def922a-3ed4-49c1-b9fd-05ec533819a3|55dfd1f8-7e59-4f89-bf56-4c82f5ace23c")
                                    .withConnectionId("a3b9076c-ce8e-434e-85b4-aff10cb3c8f1")
                                    .withTicketConfiguration(
                                        "{\"PayloadRevision\":0,\"WorkItemType\":\"Incident\",\"UseTemplate\":false,\"WorkItemData\":\"{}\",\"CreateOneWIPerCI\":false}")
                                    .withRegion("westcentralus")))
                    .withAzureAppPushReceivers(
                        Arrays
                            .asList(
                                new AzureAppPushReceiver()
                                    .withName("Sample azureAppPush")
                                    .withEmailAddress("johndoe@email.com")))
                    .withAutomationRunbookReceivers(
                        Arrays
                            .asList(
                                new AutomationRunbookReceiver()
                                    .withAutomationAccountId(
                                        "/subscriptions/187f412d-1758-44d9-b052-169e2564721d/resourceGroups/runbookTest/providers/Microsoft.Automation/automationAccounts/runbooktest")
                                    .withRunbookName("Sample runbook")
                                    .withWebhookResourceId(
                                        "/subscriptions/187f412d-1758-44d9-b052-169e2564721d/resourceGroups/runbookTest/providers/Microsoft.Automation/automationAccounts/runbooktest/webhooks/Alert1510184037084")
                                    .withIsGlobalRunbook(false)
                                    .withName("testRunbook")
                                    .withServiceUri("http://test.me")
                                    .withUseCommonAlertSchema(true)))
                    .withVoiceReceivers(
                        Arrays
                            .asList(
                                new VoiceReceiver()
                                    .withName("Sample voice")
                                    .withCountryCode("1")
                                    .withPhoneNumber("1234567890")))
                    .withLogicAppReceivers(
                        Arrays
                            .asList(
                                new LogicAppReceiver()
                                    .withName("Sample logicApp")
                                    .withResourceId(
                                        "/subscriptions/187f412d-1758-44d9-b052-169e2564721d/resourceGroups/LogicApp/providers/Microsoft.Logic/workflows/testLogicApp")
                                    .withCallbackUrl(
                                        "https://prod-27.northcentralus.logic.azure.com/workflows/68e572e818e5457ba898763b7db90877/triggers/manual/paths/invoke/azns/test?api-version=2016-10-01&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=Abpsb72UYJxPPvmDo937uzofupO5r_vIeWEx7KVHo7w")
                                    .withUseCommonAlertSchema(false)))
                    .withAzureFunctionReceivers(
                        Arrays
                            .asList(
                                new AzureFunctionReceiver()
                                    .withName("Sample azureFunction")
                                    .withFunctionAppResourceId(
                                        "/subscriptions/5def922a-3ed4-49c1-b9fd-05ec533819a3/resourceGroups/aznsTest/providers/Microsoft.Web/sites/testFunctionApp")
                                    .withFunctionName("HttpTriggerCSharp1")
                                    .withHttpTriggerUrl("http://test.me")
                                    .withUseCommonAlertSchema(true)))
                    .withArmRoleReceivers(
                        Arrays
                            .asList(
                                new ArmRoleReceiver()
                                    .withName("ArmRole-Common")
                                    .withRoleId("11111111-1111-1111-1111-111111111111")
                                    .withUseCommonAlertSchema(true),
                                new ArmRoleReceiver()
                                    .withName("ArmRole-nonCommon")
                                    .withRoleId("11111111-1111-1111-1111-111111111111")
                                    .withUseCommonAlertSchema(false)))
                    .withEventHubReceivers(
                        Arrays
                            .asList(
                                new EventHubReceiver()
                                    .withName("Sample eventHub")
                                    .withEventHubNameSpace("testEventHubNameSpace")
                                    .withEventHubName("testEventHub")
                                    .withTenantId("68a4459a-ccb8-493c-b9da-dd30457d1b84")
                                    .withSubscriptionId("187f412d-1758-44d9-b052-169e2564721d"))),
                Context.NONE);
    }
}
```

### ActionGroups_CreateOrUpdate

```java
import com.azure.resourcemanager.monitor.generated.models.ArmRoleReceiver;
import com.azure.resourcemanager.monitor.generated.models.AutomationRunbookReceiver;
import com.azure.resourcemanager.monitor.generated.models.AzureAppPushReceiver;
import com.azure.resourcemanager.monitor.generated.models.AzureFunctionReceiver;
import com.azure.resourcemanager.monitor.generated.models.EmailReceiver;
import com.azure.resourcemanager.monitor.generated.models.EventHubReceiver;
import com.azure.resourcemanager.monitor.generated.models.ItsmReceiver;
import com.azure.resourcemanager.monitor.generated.models.LogicAppReceiver;
import com.azure.resourcemanager.monitor.generated.models.SmsReceiver;
import com.azure.resourcemanager.monitor.generated.models.VoiceReceiver;
import com.azure.resourcemanager.monitor.generated.models.WebhookReceiver;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for ActionGroups CreateOrUpdate. */
public final class ActionGroupsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/createOrUpdateActionGroup.json
     */
    /**
     * Sample code: Create or update an action group.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createOrUpdateAnActionGroup(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .actionGroups()
            .define("SampleActionGroup")
            .withRegion("Global")
            .withExistingResourceGroup("Default-NotificationRules")
            .withTags(mapOf())
            .withGroupShortName("sample")
            .withEnabled(true)
            .withEmailReceivers(
                Arrays
                    .asList(
                        new EmailReceiver()
                            .withName("John Doe's email")
                            .withEmailAddress("johndoe@email.com")
                            .withUseCommonAlertSchema(false),
                        new EmailReceiver()
                            .withName("Jane Smith's email")
                            .withEmailAddress("janesmith@email.com")
                            .withUseCommonAlertSchema(true)))
            .withSmsReceivers(
                Arrays
                    .asList(
                        new SmsReceiver()
                            .withName("John Doe's mobile")
                            .withCountryCode("1")
                            .withPhoneNumber("1234567890"),
                        new SmsReceiver()
                            .withName("Jane Smith's mobile")
                            .withCountryCode("1")
                            .withPhoneNumber("0987654321")))
            .withWebhookReceivers(
                Arrays
                    .asList(
                        new WebhookReceiver()
                            .withName("Sample webhook 1")
                            .withServiceUri("http://www.example.com/webhook1")
                            .withUseCommonAlertSchema(true),
                        new WebhookReceiver()
                            .withName("Sample webhook 2")
                            .withServiceUri("http://www.example.com/webhook2")
                            .withUseCommonAlertSchema(true)
                            .withUseAadAuth(true)
                            .withObjectId("d3bb868c-fe44-452c-aa26-769a6538c808")
                            .withIdentifierUri("http://someidentifier/d7811ba3-7996-4a93-99b6-6b2f3f355f8a")
                            .withTenantId("68a4459a-ccb8-493c-b9da-dd30457d1b84")))
            .withItsmReceivers(
                Arrays
                    .asList(
                        new ItsmReceiver()
                            .withName("Sample itsm")
                            .withWorkspaceId(
                                "5def922a-3ed4-49c1-b9fd-05ec533819a3|55dfd1f8-7e59-4f89-bf56-4c82f5ace23c")
                            .withConnectionId("a3b9076c-ce8e-434e-85b4-aff10cb3c8f1")
                            .withTicketConfiguration(
                                "{\"PayloadRevision\":0,\"WorkItemType\":\"Incident\",\"UseTemplate\":false,\"WorkItemData\":\"{}\",\"CreateOneWIPerCI\":false}")
                            .withRegion("westcentralus")))
            .withAzureAppPushReceivers(
                Arrays
                    .asList(
                        new AzureAppPushReceiver()
                            .withName("Sample azureAppPush")
                            .withEmailAddress("johndoe@email.com")))
            .withAutomationRunbookReceivers(
                Arrays
                    .asList(
                        new AutomationRunbookReceiver()
                            .withAutomationAccountId(
                                "/subscriptions/187f412d-1758-44d9-b052-169e2564721d/resourceGroups/runbookTest/providers/Microsoft.Automation/automationAccounts/runbooktest")
                            .withRunbookName("Sample runbook")
                            .withWebhookResourceId(
                                "/subscriptions/187f412d-1758-44d9-b052-169e2564721d/resourceGroups/runbookTest/providers/Microsoft.Automation/automationAccounts/runbooktest/webhooks/Alert1510184037084")
                            .withIsGlobalRunbook(false)
                            .withName("testRunbook")
                            .withServiceUri("<serviceUri>")
                            .withUseCommonAlertSchema(true)))
            .withVoiceReceivers(
                Arrays
                    .asList(
                        new VoiceReceiver()
                            .withName("Sample voice")
                            .withCountryCode("1")
                            .withPhoneNumber("1234567890")))
            .withLogicAppReceivers(
                Arrays
                    .asList(
                        new LogicAppReceiver()
                            .withName("Sample logicApp")
                            .withResourceId(
                                "/subscriptions/187f412d-1758-44d9-b052-169e2564721d/resourceGroups/LogicApp/providers/Microsoft.Logic/workflows/testLogicApp")
                            .withCallbackUrl(
                                "https://prod-27.northcentralus.logic.azure.com/workflows/68e572e818e5457ba898763b7db90877/triggers/manual/paths/invoke/azns/test?api-version=2016-10-01&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=Abpsb72UYJxPPvmDo937uzofupO5r_vIeWEx7KVHo7w")
                            .withUseCommonAlertSchema(false)))
            .withAzureFunctionReceivers(
                Arrays
                    .asList(
                        new AzureFunctionReceiver()
                            .withName("Sample azureFunction")
                            .withFunctionAppResourceId(
                                "/subscriptions/5def922a-3ed4-49c1-b9fd-05ec533819a3/resourceGroups/aznsTest/providers/Microsoft.Web/sites/testFunctionApp")
                            .withFunctionName("HttpTriggerCSharp1")
                            .withHttpTriggerUrl("http://test.me")
                            .withUseCommonAlertSchema(true)))
            .withArmRoleReceivers(
                Arrays
                    .asList(
                        new ArmRoleReceiver()
                            .withName("Sample armRole")
                            .withRoleId("8e3af657-a8ff-443c-a75c-2fe8c4bcb635")
                            .withUseCommonAlertSchema(true)))
            .withEventHubReceivers(
                Arrays
                    .asList(
                        new EventHubReceiver()
                            .withName("Sample eventHub")
                            .withEventHubNameSpace("testEventHubNameSpace")
                            .withEventHubName("testEventHub")
                            .withTenantId("68a4459a-ccb8-493c-b9da-dd30457d1b84")
                            .withSubscriptionId("187f412d-1758-44d9-b052-169e2564721d")))
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

### ActionGroups_Delete

```java
import com.azure.core.util.Context;

/** Samples for ActionGroups Delete. */
public final class ActionGroupsDeleteSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/deleteActionGroup.json
     */
    /**
     * Sample code: Delete an action group.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void deleteAnActionGroup(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.actionGroups().deleteWithResponse("Default-NotificationRules", "SampleActionGroup", Context.NONE);
    }
}
```

### ActionGroups_EnableReceiver

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.monitor.generated.models.EnableRequest;

/** Samples for ActionGroups EnableReceiver. */
public final class ActionGroupsEnableReceiverSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/enableReceiver.json
     */
    /**
     * Sample code: Enable the receiver.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void enableTheReceiver(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .actionGroups()
            .enableReceiverWithResponse(
                "Default-NotificationRules",
                "SampleActionGroup",
                new EnableRequest().withReceiverName("John Doe's mobile"),
                Context.NONE);
    }
}
```

### ActionGroups_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ActionGroups GetByResourceGroup. */
public final class ActionGroupsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/getActionGroup.json
     */
    /**
     * Sample code: Get an action group.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getAnActionGroup(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .actionGroups()
            .getByResourceGroupWithResponse("Default-NotificationRules", "SampleActionGroup", Context.NONE);
    }
}
```

### ActionGroups_GetTestNotifications

```java
import com.azure.core.util.Context;

/** Samples for ActionGroups GetTestNotifications. */
public final class ActionGroupsGetTestNotificationsSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/getTestNotifications.json
     */
    /**
     * Sample code: Get notification details at subscription level.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getNotificationDetailsAtSubscriptionLevel(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.actionGroups().getTestNotificationsWithResponse("11000222191287", Context.NONE);
    }
}
```

### ActionGroups_GetTestNotificationsAtResourceGroupLevel

```java
import com.azure.core.util.Context;

/** Samples for ActionGroups GetTestNotificationsAtResourceGroupLevel. */
public final class ActionGroupsGetTestNotificationsAtResourceGroupLevelSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/getTestNotificationsAtResourceGroupLevel.json
     */
    /**
     * Sample code: Get notification details at resource group level.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getNotificationDetailsAtResourceGroupLevel(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .actionGroups()
            .getTestNotificationsAtResourceGroupLevelWithResponse(
                "Default-TestNotifications", "11000222191287", Context.NONE);
    }
}
```

### ActionGroups_List

```java
import com.azure.core.util.Context;

/** Samples for ActionGroups List. */
public final class ActionGroupsListSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/listActionGroups.json
     */
    /**
     * Sample code: List action groups at subscription level.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void listActionGroupsAtSubscriptionLevel(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.actionGroups().list(Context.NONE);
    }
}
```

### ActionGroups_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ActionGroups ListByResourceGroup. */
public final class ActionGroupsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/listActionGroups.json
     */
    /**
     * Sample code: List action groups at resource group level.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void listActionGroupsAtResourceGroupLevel(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.actionGroups().listByResourceGroup("Default-NotificationRules", Context.NONE);
    }
}
```

### ActionGroups_PostTestNotifications

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.monitor.generated.models.ArmRoleReceiver;
import com.azure.resourcemanager.monitor.generated.models.AutomationRunbookReceiver;
import com.azure.resourcemanager.monitor.generated.models.AzureAppPushReceiver;
import com.azure.resourcemanager.monitor.generated.models.AzureFunctionReceiver;
import com.azure.resourcemanager.monitor.generated.models.EmailReceiver;
import com.azure.resourcemanager.monitor.generated.models.EventHubReceiver;
import com.azure.resourcemanager.monitor.generated.models.ItsmReceiver;
import com.azure.resourcemanager.monitor.generated.models.LogicAppReceiver;
import com.azure.resourcemanager.monitor.generated.models.NotificationRequestBody;
import com.azure.resourcemanager.monitor.generated.models.SmsReceiver;
import com.azure.resourcemanager.monitor.generated.models.VoiceReceiver;
import com.azure.resourcemanager.monitor.generated.models.WebhookReceiver;
import java.util.Arrays;

/** Samples for ActionGroups PostTestNotifications. */
public final class ActionGroupsPostTestNotificationsSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/postTestNotifications.json
     */
    /**
     * Sample code: Create notifications at subscription level.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createNotificationsAtSubscriptionLevel(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .actionGroups()
            .postTestNotifications(
                new NotificationRequestBody()
                    .withAlertType("budget")
                    .withEmailReceivers(
                        Arrays
                            .asList(
                                new EmailReceiver()
                                    .withName("John Doe's email")
                                    .withEmailAddress("johndoe@email.com")
                                    .withUseCommonAlertSchema(false),
                                new EmailReceiver()
                                    .withName("Jane Smith's email")
                                    .withEmailAddress("janesmith@email.com")
                                    .withUseCommonAlertSchema(true)))
                    .withSmsReceivers(
                        Arrays
                            .asList(
                                new SmsReceiver()
                                    .withName("John Doe's mobile")
                                    .withCountryCode("1")
                                    .withPhoneNumber("1234567890"),
                                new SmsReceiver()
                                    .withName("Jane Smith's mobile")
                                    .withCountryCode("1")
                                    .withPhoneNumber("0987654321")))
                    .withWebhookReceivers(
                        Arrays
                            .asList(
                                new WebhookReceiver()
                                    .withName("Sample webhook 1")
                                    .withServiceUri("http://www.example.com/webhook1")
                                    .withUseCommonAlertSchema(true),
                                new WebhookReceiver()
                                    .withName("Sample webhook 2")
                                    .withServiceUri("http://www.example.com/webhook2")
                                    .withUseCommonAlertSchema(true)
                                    .withUseAadAuth(true)
                                    .withObjectId("d3bb868c-fe44-452c-aa26-769a6538c808")
                                    .withIdentifierUri("http://someidentifier/d7811ba3-7996-4a93-99b6-6b2f3f355f8a")
                                    .withTenantId("68a4459a-ccb8-493c-b9da-dd30457d1b84")))
                    .withItsmReceivers(
                        Arrays
                            .asList(
                                new ItsmReceiver()
                                    .withName("Sample itsm")
                                    .withWorkspaceId(
                                        "5def922a-3ed4-49c1-b9fd-05ec533819a3|55dfd1f8-7e59-4f89-bf56-4c82f5ace23c")
                                    .withConnectionId("a3b9076c-ce8e-434e-85b4-aff10cb3c8f1")
                                    .withTicketConfiguration(
                                        "{\"PayloadRevision\":0,\"WorkItemType\":\"Incident\",\"UseTemplate\":false,\"WorkItemData\":\"{}\",\"CreateOneWIPerCI\":false}")
                                    .withRegion("westcentralus")))
                    .withAzureAppPushReceivers(
                        Arrays
                            .asList(
                                new AzureAppPushReceiver()
                                    .withName("Sample azureAppPush")
                                    .withEmailAddress("johndoe@email.com")))
                    .withAutomationRunbookReceivers(
                        Arrays
                            .asList(
                                new AutomationRunbookReceiver()
                                    .withAutomationAccountId(
                                        "/subscriptions/187f412d-1758-44d9-b052-169e2564721d/resourceGroups/runbookTest/providers/Microsoft.Automation/automationAccounts/runbooktest")
                                    .withRunbookName("Sample runbook")
                                    .withWebhookResourceId(
                                        "/subscriptions/187f412d-1758-44d9-b052-169e2564721d/resourceGroups/runbookTest/providers/Microsoft.Automation/automationAccounts/runbooktest/webhooks/Alert1510184037084")
                                    .withIsGlobalRunbook(false)
                                    .withName("testRunbook")
                                    .withServiceUri("http://test.me")
                                    .withUseCommonAlertSchema(true)))
                    .withVoiceReceivers(
                        Arrays
                            .asList(
                                new VoiceReceiver()
                                    .withName("Sample voice")
                                    .withCountryCode("1")
                                    .withPhoneNumber("1234567890")))
                    .withLogicAppReceivers(
                        Arrays
                            .asList(
                                new LogicAppReceiver()
                                    .withName("Sample logicApp")
                                    .withResourceId(
                                        "/subscriptions/187f412d-1758-44d9-b052-169e2564721d/resourceGroups/LogicApp/providers/Microsoft.Logic/workflows/testLogicApp")
                                    .withCallbackUrl(
                                        "https://prod-27.northcentralus.logic.azure.com/workflows/68e572e818e5457ba898763b7db90877/triggers/manual/paths/invoke/azns/test?api-version=2016-10-01&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=Abpsb72UYJxPPvmDo937uzofupO5r_vIeWEx7KVHo7w")
                                    .withUseCommonAlertSchema(false)))
                    .withAzureFunctionReceivers(
                        Arrays
                            .asList(
                                new AzureFunctionReceiver()
                                    .withName("Sample azureFunction")
                                    .withFunctionAppResourceId(
                                        "/subscriptions/5def922a-3ed4-49c1-b9fd-05ec533819a3/resourceGroups/aznsTest/providers/Microsoft.Web/sites/testFunctionApp")
                                    .withFunctionName("HttpTriggerCSharp1")
                                    .withHttpTriggerUrl("http://test.me")
                                    .withUseCommonAlertSchema(true)))
                    .withArmRoleReceivers(
                        Arrays
                            .asList(
                                new ArmRoleReceiver()
                                    .withName("ArmRole-Common")
                                    .withRoleId("11111111-1111-1111-1111-111111111111")
                                    .withUseCommonAlertSchema(true),
                                new ArmRoleReceiver()
                                    .withName("ArmRole-nonCommon")
                                    .withRoleId("11111111-1111-1111-1111-111111111111")
                                    .withUseCommonAlertSchema(false)))
                    .withEventHubReceivers(
                        Arrays
                            .asList(
                                new EventHubReceiver()
                                    .withName("Sample eventHub")
                                    .withEventHubNameSpace("testEventHubNameSpace")
                                    .withEventHubName("testEventHub")
                                    .withTenantId("68a4459a-ccb8-493c-b9da-dd30457d1b84")
                                    .withSubscriptionId("187f412d-1758-44d9-b052-169e2564721d"))),
                Context.NONE);
    }
}
```

### ActionGroups_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.monitor.generated.models.ActionGroupResource;
import java.util.HashMap;
import java.util.Map;

/** Samples for ActionGroups Update. */
public final class ActionGroupsUpdateSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2022-04-01/examples/patchActionGroup.json
     */
    /**
     * Sample code: Patch an action group.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void patchAnActionGroup(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        ActionGroupResource resource =
            manager
                .actionGroups()
                .getByResourceGroupWithResponse("Default-NotificationRules", "SampleActionGroup", Context.NONE)
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

### ActivityLogAlerts_CreateOrUpdate

```java
import com.azure.resourcemanager.monitor.generated.models.ActionGroupAutoGenerated;
import com.azure.resourcemanager.monitor.generated.models.ActionList;
import com.azure.resourcemanager.monitor.generated.models.AlertRuleAllOfCondition;
import com.azure.resourcemanager.monitor.generated.models.AlertRuleAnyOfOrLeafCondition;
import com.azure.resourcemanager.monitor.generated.models.AlertRuleLeafCondition;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for ActivityLogAlerts CreateOrUpdate. */
public final class ActivityLogAlertsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2020-10-01/examples/ActivityLogAlertRule_CreateOrUpdateRuleWithContainsAny.json
     */
    /**
     * Sample code: Create or update an Activity Log Alert rule with 'containsAny'.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createOrUpdateAnActivityLogAlertRuleWithContainsAny(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .activityLogAlerts()
            .define("SampleActivityLogAlertRuleWithContainsAny")
            .withRegion("Global")
            .withExistingResourceGroup("MyResourceGroup")
            .withTags(mapOf())
            .withScopes(Arrays.asList("subscriptions/187f412d-1758-44d9-b052-169e2564721d"))
            .withCondition(
                new AlertRuleAllOfCondition()
                    .withAllOf(
                        Arrays
                            .asList(
                                new AlertRuleAnyOfOrLeafCondition().withField("category").withEquals("ServiceHealth"),
                                new AlertRuleAnyOfOrLeafCondition()
                                    .withField("properties.impactedServices[*].ImpactedRegions[*].RegionName")
                                    .withContainsAny(Arrays.asList("North Europe", "West Europe")))))
            .withActions(
                new ActionList()
                    .withActionGroups(
                        Arrays
                            .asList(
                                new ActionGroupAutoGenerated()
                                    .withActionGroupId(
                                        "/subscriptions/187f412d-1758-44d9-b052-169e2564721d/resourceGroups/MyResourceGroup/providers/Microsoft.Insights/actionGroups/SampleActionGroup")
                                    .withWebhookProperties(mapOf("sampleWebhookProperty", "SamplePropertyValue")))))
            .withEnabled(true)
            .withDescription("Description of sample Activity Log Alert rule with 'containsAny'.")
            .create();
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2020-10-01/examples/ActivityLogAlertRule_CreateOrUpdateRuleWithAnyOfCondition.json
     */
    /**
     * Sample code: Create or update an Activity Log Alert rule with 'anyOf' condition.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createOrUpdateAnActivityLogAlertRuleWithAnyOfCondition(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .activityLogAlerts()
            .define("SampleActivityLogAlertRuleWithAnyOfCondition")
            .withRegion("Global")
            .withExistingResourceGroup("MyResourceGroup")
            .withTags(mapOf())
            .withScopes(Arrays.asList("subscriptions/187f412d-1758-44d9-b052-169e2564721d"))
            .withCondition(
                new AlertRuleAllOfCondition()
                    .withAllOf(
                        Arrays
                            .asList(
                                new AlertRuleAnyOfOrLeafCondition().withField("category").withEquals("ServiceHealth"),
                                new AlertRuleAnyOfOrLeafCondition()
                                    .withAnyOf(
                                        Arrays
                                            .asList(
                                                new AlertRuleLeafCondition()
                                                    .withField("properties.incidentType")
                                                    .withEquals("Incident"),
                                                new AlertRuleLeafCondition()
                                                    .withField("properties.incidentType")
                                                    .withEquals("Maintenance"))))))
            .withActions(
                new ActionList()
                    .withActionGroups(
                        Arrays
                            .asList(
                                new ActionGroupAutoGenerated()
                                    .withActionGroupId(
                                        "/subscriptions/187f412d-1758-44d9-b052-169e2564721d/resourceGroups/MyResourceGroup/providers/Microsoft.Insights/actionGroups/SampleActionGroup")
                                    .withWebhookProperties(mapOf("sampleWebhookProperty", "SamplePropertyValue")))))
            .withEnabled(true)
            .withDescription("Description of sample Activity Log Alert rule with 'anyOf' condition.")
            .create();
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2020-10-01/examples/ActivityLogAlertRule_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update an Activity Log Alert rule.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createOrUpdateAnActivityLogAlertRule(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .activityLogAlerts()
            .define("SampleActivityLogAlertRule")
            .withRegion("Global")
            .withExistingResourceGroup("MyResourceGroup")
            .withTags(mapOf())
            .withScopes(Arrays.asList("/subscriptions/187f412d-1758-44d9-b052-169e2564721d"))
            .withCondition(
                new AlertRuleAllOfCondition()
                    .withAllOf(
                        Arrays
                            .asList(
                                new AlertRuleAnyOfOrLeafCondition().withField("category").withEquals("Administrative"),
                                new AlertRuleAnyOfOrLeafCondition().withField("level").withEquals("Error"))))
            .withActions(
                new ActionList()
                    .withActionGroups(
                        Arrays
                            .asList(
                                new ActionGroupAutoGenerated()
                                    .withActionGroupId(
                                        "/subscriptions/187f412d-1758-44d9-b052-169e2564721d/resourceGroups/MyResourceGroup/providers/Microsoft.Insights/actionGroups/SampleActionGroup")
                                    .withWebhookProperties(mapOf("sampleWebhookProperty", "SamplePropertyValue")))))
            .withEnabled(true)
            .withDescription("Description of sample Activity Log Alert rule.")
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

### ActivityLogAlerts_Delete

```java
import com.azure.core.util.Context;

/** Samples for ActivityLogAlerts Delete. */
public final class ActivityLogAlertsDeleteSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2020-10-01/examples/ActivityLogAlertRule_Delete.json
     */
    /**
     * Sample code: Delete an Activity Log Alert rule.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void deleteAnActivityLogAlertRule(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.activityLogAlerts().deleteWithResponse("MyResourceGroup", "SampleActivityLogAlertRule", Context.NONE);
    }
}
```

### ActivityLogAlerts_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ActivityLogAlerts GetByResourceGroup. */
public final class ActivityLogAlertsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2020-10-01/examples/ActivityLogAlertRule_Get.json
     */
    /**
     * Sample code: Get an Activity Log Alert rule.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getAnActivityLogAlertRule(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .activityLogAlerts()
            .getByResourceGroupWithResponse("MyResourceGroup", "SampleActivityLogAlertRule", Context.NONE);
    }
}
```

### ActivityLogAlerts_List

```java
import com.azure.core.util.Context;

/** Samples for ActivityLogAlerts List. */
public final class ActivityLogAlertsListSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2020-10-01/examples/ActivityLogAlertRule_ListBySubscriptionId.json
     */
    /**
     * Sample code: Get list of all Activity Log Alert rules under a subscription.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getListOfAllActivityLogAlertRulesUnderASubscription(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.activityLogAlerts().list(Context.NONE);
    }
}
```

### ActivityLogAlerts_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ActivityLogAlerts ListByResourceGroup. */
public final class ActivityLogAlertsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2020-10-01/examples/ActivityLogAlertRule_ListByResourceGroupName.json
     */
    /**
     * Sample code: List activity log alerts.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void listActivityLogAlerts(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.activityLogAlerts().listByResourceGroup("MyResourceGroup", Context.NONE);
    }
}
```

### ActivityLogAlerts_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.monitor.generated.models.ActivityLogAlertResource;
import java.util.HashMap;
import java.util.Map;

/** Samples for ActivityLogAlerts Update. */
public final class ActivityLogAlertsUpdateSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2020-10-01/examples/ActivityLogAlertRule_Update.json
     */
    /**
     * Sample code: Patch an Activity Log Alert rule.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void patchAnActivityLogAlertRule(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        ActivityLogAlertResource resource =
            manager
                .activityLogAlerts()
                .getByResourceGroupWithResponse("MyResourceGroup", "SampleActivityLogAlertRule", Context.NONE)
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

### ActivityLogs_List

```java
import com.azure.core.util.Context;

/** Samples for ActivityLogs List. */
public final class ActivityLogsListSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2015-04-01/examples/GetActivityLogsFilteredAndSelected.json
     */
    /**
     * Sample code: Get Activity Logs with filter and select.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getActivityLogsWithFilterAndSelect(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .activityLogs()
            .list(
                "eventTimestamp ge '2015-01-21T20:00:00Z' and eventTimestamp le '2015-01-23T20:00:00Z' and"
                    + " resourceGroupName eq 'MSSupportGroup'",
                "eventName,id,resourceGroupName,resourceProviderName,operationName,status,eventTimestamp,correlationId,submissionTimestamp,level",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2015-04-01/examples/GetActivityLogsFiltered.json
     */
    /**
     * Sample code: Get Activity Logs with filter.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getActivityLogsWithFilter(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .activityLogs()
            .list(
                "eventTimestamp ge '2015-01-21T20:00:00Z' and eventTimestamp le '2015-01-23T20:00:00Z' and"
                    + " resourceGroupName eq 'MSSupportGroup'",
                null,
                Context.NONE);
    }
}
```

### AlertRuleIncidents_Get

```java
import com.azure.core.util.Context;

/** Samples for AlertRuleIncidents Get. */
public final class AlertRuleIncidentsGetSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2016-03-01/examples/getAlertRuleIncident.json
     */
    /**
     * Sample code: Get a single alert rule incident.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getASingleAlertRuleIncident(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.alertRuleIncidents().getWithResponse("Rac46PostSwapRG", "myRuleName", "Website_started", Context.NONE);
    }
}
```

### AlertRuleIncidents_ListByAlertRule

```java
import com.azure.core.util.Context;

/** Samples for AlertRuleIncidents ListByAlertRule. */
public final class AlertRuleIncidentsListByAlertRuleSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2016-03-01/examples/listAlertRuleIncidents.json
     */
    /**
     * Sample code: List alert rule incidents.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void listAlertRuleIncidents(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.alertRuleIncidents().listByAlertRule("Rac46PostSwapRG", "myRuleName", Context.NONE);
    }
}
```

### AlertRules_CreateOrUpdate

```java
import com.azure.resourcemanager.monitor.generated.models.ConditionOperator;
import com.azure.resourcemanager.monitor.generated.models.RuleMetricDataSource;
import com.azure.resourcemanager.monitor.generated.models.ThresholdRuleCondition;
import com.azure.resourcemanager.monitor.generated.models.TimeAggregationOperator;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for AlertRules CreateOrUpdate. */
public final class AlertRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2016-03-01/examples/createOrUpdateAlertRule.json
     */
    /**
     * Sample code: Create or update an alert rule.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createOrUpdateAnAlertRule(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .alertRules()
            .define("chiricutin")
            .withRegion("West US")
            .withExistingResourceGroup("Rac46PostSwapRG")
            .withNamePropertiesName("chiricutin")
            .withIsEnabled(true)
            .withCondition(
                new ThresholdRuleCondition()
                    .withDataSource(
                        new RuleMetricDataSource()
                            .withResourceUri(
                                "/subscriptions/b67f7fec-69fc-4974-9099-a26bd6ffeda3/resourceGroups/Rac46PostSwapRG/providers/Microsoft.Web/sites/leoalerttest")
                            .withMetricName("Requests"))
                    .withOperator(ConditionOperator.GREATER_THAN)
                    .withThreshold(3.0)
                    .withWindowSize(Duration.parse("PT5M"))
                    .withTimeAggregation(TimeAggregationOperator.TOTAL))
            .withTags(mapOf())
            .withDescription("Pura Vida")
            .withActions(Arrays.asList())
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

### AlertRules_Delete

```java
import com.azure.core.util.Context;

/** Samples for AlertRules Delete. */
public final class AlertRulesDeleteSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2016-03-01/examples/deleteAlertRule.json
     */
    /**
     * Sample code: Delete an alert rule.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void deleteAnAlertRule(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.alertRules().deleteWithResponse("Rac46PostSwapRG", "chiricutin", Context.NONE);
    }
}
```

### AlertRules_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for AlertRules GetByResourceGroup. */
public final class AlertRulesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2016-03-01/examples/getAlertRule.json
     */
    /**
     * Sample code: Get an alert rule.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getAnAlertRule(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.alertRules().getByResourceGroupWithResponse("Rac46PostSwapRG", "chiricutin", Context.NONE);
    }
}
```

### AlertRules_List

```java
import com.azure.core.util.Context;

/** Samples for AlertRules List. */
public final class AlertRulesListSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2016-03-01/examples/listAlertRuleBySubscription.json
     */
    /**
     * Sample code: List alert rules.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void listAlertRules(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.alertRules().list(Context.NONE);
    }
}
```

### AlertRules_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for AlertRules ListByResourceGroup. */
public final class AlertRulesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2016-03-01/examples/listAlertRule.json
     */
    /**
     * Sample code: List alert rules.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void listAlertRules(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.alertRules().listByResourceGroup("Rac46PostSwapRG", Context.NONE);
    }
}
```

### AlertRules_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.monitor.generated.models.AlertRuleResource;
import com.azure.resourcemanager.monitor.generated.models.ConditionOperator;
import com.azure.resourcemanager.monitor.generated.models.RuleMetricDataSource;
import com.azure.resourcemanager.monitor.generated.models.ThresholdRuleCondition;
import com.azure.resourcemanager.monitor.generated.models.TimeAggregationOperator;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for AlertRules Update. */
public final class AlertRulesUpdateSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2016-03-01/examples/patchAlertRule.json
     */
    /**
     * Sample code: Patch an alert rule.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void patchAnAlertRule(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        AlertRuleResource resource =
            manager
                .alertRules()
                .getByResourceGroupWithResponse("Rac46PostSwapRG", "chiricutin", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("$type", "Microsoft.WindowsAzure.Management.Common.Storage.CasePreservedDictionary"))
            .withName("chiricutin")
            .withDescription("Pura Vida")
            .withIsEnabled(true)
            .withCondition(
                new ThresholdRuleCondition()
                    .withDataSource(
                        new RuleMetricDataSource()
                            .withResourceUri(
                                "/subscriptions/b67f7fec-69fc-4974-9099-a26bd6ffeda3/resourceGroups/Rac46PostSwapRG/providers/Microsoft.Web/sites/leoalerttest")
                            .withMetricName("Requests"))
                    .withOperator(ConditionOperator.GREATER_THAN)
                    .withThreshold(3.0)
                    .withWindowSize(Duration.parse("PT5M"))
                    .withTimeAggregation(TimeAggregationOperator.TOTAL))
            .withActions(Arrays.asList())
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

### AutoscaleSettings_CreateOrUpdate

```java
import com.azure.resourcemanager.monitor.generated.models.AutoscaleNotification;
import com.azure.resourcemanager.monitor.generated.models.AutoscaleProfile;
import com.azure.resourcemanager.monitor.generated.models.ComparisonOperationType;
import com.azure.resourcemanager.monitor.generated.models.EmailNotification;
import com.azure.resourcemanager.monitor.generated.models.MetricStatisticType;
import com.azure.resourcemanager.monitor.generated.models.MetricTrigger;
import com.azure.resourcemanager.monitor.generated.models.Recurrence;
import com.azure.resourcemanager.monitor.generated.models.RecurrenceFrequency;
import com.azure.resourcemanager.monitor.generated.models.RecurrentSchedule;
import com.azure.resourcemanager.monitor.generated.models.ScaleAction;
import com.azure.resourcemanager.monitor.generated.models.ScaleCapacity;
import com.azure.resourcemanager.monitor.generated.models.ScaleDirection;
import com.azure.resourcemanager.monitor.generated.models.ScaleRule;
import com.azure.resourcemanager.monitor.generated.models.ScaleType;
import com.azure.resourcemanager.monitor.generated.models.TimeAggregationType;
import com.azure.resourcemanager.monitor.generated.models.TimeWindow;
import com.azure.resourcemanager.monitor.generated.models.WebhookNotification;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for AutoscaleSettings CreateOrUpdate. */
public final class AutoscaleSettingsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2015-04-01/examples/createOrUpdateAutoscaleSetting.json
     */
    /**
     * Sample code: Create or update an autoscale setting.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createOrUpdateAnAutoscaleSetting(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .autoscaleSettings()
            .define("MySetting")
            .withRegion("West US")
            .withExistingResourceGroup("TestingMetricsScaleSet")
            .withProfiles(
                Arrays
                    .asList(
                        new AutoscaleProfile()
                            .withName("adios")
                            .withCapacity(
                                new ScaleCapacity().withMinimum("1").withMaximum("10").withDefaultProperty("1"))
                            .withRules(
                                Arrays
                                    .asList(
                                        new ScaleRule()
                                            .withMetricTrigger(
                                                new MetricTrigger()
                                                    .withMetricName("Percentage CPU")
                                                    .withMetricResourceUri(
                                                        "/subscriptions/b67f7fec-69fc-4974-9099-a26bd6ffeda3/resourceGroups/TestingMetricsScaleSet/providers/Microsoft.Compute/virtualMachineScaleSets/testingsc")
                                                    .withTimeGrain(Duration.parse("PT1M"))
                                                    .withStatistic(MetricStatisticType.AVERAGE)
                                                    .withTimeWindow(Duration.parse("PT5M"))
                                                    .withTimeAggregation(TimeAggregationType.AVERAGE)
                                                    .withOperator(ComparisonOperationType.GREATER_THAN)
                                                    .withThreshold(10.0)
                                                    .withDividePerInstance(false))
                                            .withScaleAction(
                                                new ScaleAction()
                                                    .withDirection(ScaleDirection.INCREASE)
                                                    .withType(ScaleType.CHANGE_COUNT)
                                                    .withValue("1")
                                                    .withCooldown(Duration.parse("PT5M"))),
                                        new ScaleRule()
                                            .withMetricTrigger(
                                                new MetricTrigger()
                                                    .withMetricName("Percentage CPU")
                                                    .withMetricResourceUri(
                                                        "/subscriptions/b67f7fec-69fc-4974-9099-a26bd6ffeda3/resourceGroups/TestingMetricsScaleSet/providers/Microsoft.Compute/virtualMachineScaleSets/testingsc")
                                                    .withTimeGrain(Duration.parse("PT2M"))
                                                    .withStatistic(MetricStatisticType.AVERAGE)
                                                    .withTimeWindow(Duration.parse("PT5M"))
                                                    .withTimeAggregation(TimeAggregationType.AVERAGE)
                                                    .withOperator(ComparisonOperationType.GREATER_THAN)
                                                    .withThreshold(15.0)
                                                    .withDividePerInstance(false))
                                            .withScaleAction(
                                                new ScaleAction()
                                                    .withDirection(ScaleDirection.DECREASE)
                                                    .withType(ScaleType.CHANGE_COUNT)
                                                    .withValue("2")
                                                    .withCooldown(Duration.parse("PT6M")))))
                            .withFixedDate(
                                new TimeWindow()
                                    .withTimeZone("UTC")
                                    .withStart(OffsetDateTime.parse("2015-03-05T14:00:00Z"))
                                    .withEnd(OffsetDateTime.parse("2015-03-05T14:30:00Z"))),
                        new AutoscaleProfile()
                            .withName("saludos")
                            .withCapacity(
                                new ScaleCapacity().withMinimum("1").withMaximum("10").withDefaultProperty("1"))
                            .withRules(
                                Arrays
                                    .asList(
                                        new ScaleRule()
                                            .withMetricTrigger(
                                                new MetricTrigger()
                                                    .withMetricName("Percentage CPU")
                                                    .withMetricResourceUri(
                                                        "/subscriptions/b67f7fec-69fc-4974-9099-a26bd6ffeda3/resourceGroups/TestingMetricsScaleSet/providers/Microsoft.Compute/virtualMachineScaleSets/testingsc")
                                                    .withTimeGrain(Duration.parse("PT1M"))
                                                    .withStatistic(MetricStatisticType.AVERAGE)
                                                    .withTimeWindow(Duration.parse("PT5M"))
                                                    .withTimeAggregation(TimeAggregationType.AVERAGE)
                                                    .withOperator(ComparisonOperationType.GREATER_THAN)
                                                    .withThreshold(10.0)
                                                    .withDividePerInstance(false))
                                            .withScaleAction(
                                                new ScaleAction()
                                                    .withDirection(ScaleDirection.INCREASE)
                                                    .withType(ScaleType.CHANGE_COUNT)
                                                    .withValue("1")
                                                    .withCooldown(Duration.parse("PT5M"))),
                                        new ScaleRule()
                                            .withMetricTrigger(
                                                new MetricTrigger()
                                                    .withMetricName("Percentage CPU")
                                                    .withMetricResourceUri(
                                                        "/subscriptions/b67f7fec-69fc-4974-9099-a26bd6ffeda3/resourceGroups/TestingMetricsScaleSet/providers/Microsoft.Compute/virtualMachineScaleSets/testingsc")
                                                    .withTimeGrain(Duration.parse("PT2M"))
                                                    .withStatistic(MetricStatisticType.AVERAGE)
                                                    .withTimeWindow(Duration.parse("PT5M"))
                                                    .withTimeAggregation(TimeAggregationType.AVERAGE)
                                                    .withOperator(ComparisonOperationType.GREATER_THAN)
                                                    .withThreshold(15.0)
                                                    .withDividePerInstance(false))
                                            .withScaleAction(
                                                new ScaleAction()
                                                    .withDirection(ScaleDirection.DECREASE)
                                                    .withType(ScaleType.CHANGE_COUNT)
                                                    .withValue("2")
                                                    .withCooldown(Duration.parse("PT6M")))))
                            .withRecurrence(
                                new Recurrence()
                                    .withFrequency(RecurrenceFrequency.WEEK)
                                    .withSchedule(
                                        new RecurrentSchedule()
                                            .withTimeZone("UTC")
                                            .withDays(Arrays.asList("1"))
                                            .withHours(Arrays.asList(5))
                                            .withMinutes(Arrays.asList(15))))))
            .withTags(mapOf())
            .withNotifications(
                Arrays
                    .asList(
                        new AutoscaleNotification()
                            .withEmail(
                                new EmailNotification()
                                    .withSendToSubscriptionAdministrator(true)
                                    .withSendToSubscriptionCoAdministrators(true)
                                    .withCustomEmails(Arrays.asList("gu@ms.com", "ge@ns.net")))
                            .withWebhooks(
                                Arrays
                                    .asList(
                                        new WebhookNotification()
                                            .withServiceUri("http://myservice.com")
                                            .withProperties(mapOf())))))
            .withEnabled(true)
            .withTargetResourceUri(
                "/subscriptions/b67f7fec-69fc-4974-9099-a26bd6ffeda3/resourceGroups/TestingMetricsScaleSet/providers/Microsoft.Compute/virtualMachineScaleSets/testingsc")
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

### AutoscaleSettings_Delete

```java
import com.azure.core.util.Context;

/** Samples for AutoscaleSettings Delete. */
public final class AutoscaleSettingsDeleteSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2015-04-01/examples/deleteAutoscaleSetting.json
     */
    /**
     * Sample code: Delete an autoscale setting.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void deleteAnAutoscaleSetting(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.autoscaleSettings().deleteWithResponse("TestingMetricsScaleSet", "MySetting", Context.NONE);
    }
}
```

### AutoscaleSettings_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for AutoscaleSettings GetByResourceGroup. */
public final class AutoscaleSettingsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2015-04-01/examples/getAutoscaleSetting.json
     */
    /**
     * Sample code: Get an autoscale setting.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getAnAutoscaleSetting(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.autoscaleSettings().getByResourceGroupWithResponse("TestingMetricsScaleSet", "MySetting", Context.NONE);
    }
}
```

### AutoscaleSettings_List

```java
import com.azure.core.util.Context;

/** Samples for AutoscaleSettings List. */
public final class AutoscaleSettingsListSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2015-04-01/examples/listAutoscaleSettingBySubscription.json
     */
    /**
     * Sample code: List autoscale settings.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void listAutoscaleSettings(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.autoscaleSettings().list(Context.NONE);
    }
}
```

### AutoscaleSettings_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for AutoscaleSettings ListByResourceGroup. */
public final class AutoscaleSettingsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2015-04-01/examples/listAutoscaleSetting.json
     */
    /**
     * Sample code: List autoscale settings.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void listAutoscaleSettings(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.autoscaleSettings().listByResourceGroup("TestingMetricsScaleSet", Context.NONE);
    }
}
```

### AutoscaleSettings_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.monitor.generated.models.AutoscaleNotification;
import com.azure.resourcemanager.monitor.generated.models.AutoscaleProfile;
import com.azure.resourcemanager.monitor.generated.models.AutoscaleSettingResource;
import com.azure.resourcemanager.monitor.generated.models.ComparisonOperationType;
import com.azure.resourcemanager.monitor.generated.models.EmailNotification;
import com.azure.resourcemanager.monitor.generated.models.MetricStatisticType;
import com.azure.resourcemanager.monitor.generated.models.MetricTrigger;
import com.azure.resourcemanager.monitor.generated.models.Recurrence;
import com.azure.resourcemanager.monitor.generated.models.RecurrenceFrequency;
import com.azure.resourcemanager.monitor.generated.models.RecurrentSchedule;
import com.azure.resourcemanager.monitor.generated.models.ScaleAction;
import com.azure.resourcemanager.monitor.generated.models.ScaleCapacity;
import com.azure.resourcemanager.monitor.generated.models.ScaleDirection;
import com.azure.resourcemanager.monitor.generated.models.ScaleRule;
import com.azure.resourcemanager.monitor.generated.models.ScaleType;
import com.azure.resourcemanager.monitor.generated.models.TimeAggregationType;
import com.azure.resourcemanager.monitor.generated.models.TimeWindow;
import com.azure.resourcemanager.monitor.generated.models.WebhookNotification;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for AutoscaleSettings Update. */
public final class AutoscaleSettingsUpdateSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2015-04-01/examples/patchAutoscaleSetting.json
     */
    /**
     * Sample code: Patch an autoscale setting.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void patchAnAutoscaleSetting(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        AutoscaleSettingResource resource =
            manager
                .autoscaleSettings()
                .getByResourceGroupWithResponse("TestingMetricsScaleSet", "MySetting", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("$type", "Microsoft.WindowsAzure.Management.Common.Storage.CasePreservedDictionary"))
            .withProfiles(
                Arrays
                    .asList(
                        new AutoscaleProfile()
                            .withName("adios")
                            .withCapacity(
                                new ScaleCapacity().withMinimum("1").withMaximum("10").withDefaultProperty("1"))
                            .withRules(
                                Arrays
                                    .asList(
                                        new ScaleRule()
                                            .withMetricTrigger(
                                                new MetricTrigger()
                                                    .withMetricName("Percentage CPU")
                                                    .withMetricResourceUri(
                                                        "/subscriptions/b67f7fec-69fc-4974-9099-a26bd6ffeda3/resourceGroups/TestingMetricsScaleSet/providers/Microsoft.Compute/virtualMachineScaleSets/testingsc")
                                                    .withTimeGrain(Duration.parse("PT1M"))
                                                    .withStatistic(MetricStatisticType.AVERAGE)
                                                    .withTimeWindow(Duration.parse("PT5M"))
                                                    .withTimeAggregation(TimeAggregationType.AVERAGE)
                                                    .withOperator(ComparisonOperationType.GREATER_THAN)
                                                    .withThreshold(10.0)
                                                    .withDividePerInstance(false))
                                            .withScaleAction(
                                                new ScaleAction()
                                                    .withDirection(ScaleDirection.INCREASE)
                                                    .withType(ScaleType.CHANGE_COUNT)
                                                    .withValue("1")
                                                    .withCooldown(Duration.parse("PT5M"))),
                                        new ScaleRule()
                                            .withMetricTrigger(
                                                new MetricTrigger()
                                                    .withMetricName("Percentage CPU")
                                                    .withMetricResourceUri(
                                                        "/subscriptions/b67f7fec-69fc-4974-9099-a26bd6ffeda3/resourceGroups/TestingMetricsScaleSet/providers/Microsoft.Compute/virtualMachineScaleSets/testingsc")
                                                    .withTimeGrain(Duration.parse("PT2M"))
                                                    .withStatistic(MetricStatisticType.AVERAGE)
                                                    .withTimeWindow(Duration.parse("PT5M"))
                                                    .withTimeAggregation(TimeAggregationType.AVERAGE)
                                                    .withOperator(ComparisonOperationType.GREATER_THAN)
                                                    .withThreshold(15.0)
                                                    .withDividePerInstance(false))
                                            .withScaleAction(
                                                new ScaleAction()
                                                    .withDirection(ScaleDirection.DECREASE)
                                                    .withType(ScaleType.CHANGE_COUNT)
                                                    .withValue("2")
                                                    .withCooldown(Duration.parse("PT6M")))))
                            .withFixedDate(
                                new TimeWindow()
                                    .withTimeZone("UTC")
                                    .withStart(OffsetDateTime.parse("2015-03-05T14:00:00Z"))
                                    .withEnd(OffsetDateTime.parse("2015-03-05T14:30:00Z"))),
                        new AutoscaleProfile()
                            .withName("saludos")
                            .withCapacity(
                                new ScaleCapacity().withMinimum("1").withMaximum("10").withDefaultProperty("1"))
                            .withRules(
                                Arrays
                                    .asList(
                                        new ScaleRule()
                                            .withMetricTrigger(
                                                new MetricTrigger()
                                                    .withMetricName("Percentage CPU")
                                                    .withMetricResourceUri(
                                                        "/subscriptions/b67f7fec-69fc-4974-9099-a26bd6ffeda3/resourceGroups/TestingMetricsScaleSet/providers/Microsoft.Compute/virtualMachineScaleSets/testingsc")
                                                    .withTimeGrain(Duration.parse("PT1M"))
                                                    .withStatistic(MetricStatisticType.AVERAGE)
                                                    .withTimeWindow(Duration.parse("PT5M"))
                                                    .withTimeAggregation(TimeAggregationType.AVERAGE)
                                                    .withOperator(ComparisonOperationType.GREATER_THAN)
                                                    .withThreshold(10.0)
                                                    .withDividePerInstance(false))
                                            .withScaleAction(
                                                new ScaleAction()
                                                    .withDirection(ScaleDirection.INCREASE)
                                                    .withType(ScaleType.CHANGE_COUNT)
                                                    .withValue("1")
                                                    .withCooldown(Duration.parse("PT5M"))),
                                        new ScaleRule()
                                            .withMetricTrigger(
                                                new MetricTrigger()
                                                    .withMetricName("Percentage CPU")
                                                    .withMetricResourceUri(
                                                        "/subscriptions/b67f7fec-69fc-4974-9099-a26bd6ffeda3/resourceGroups/TestingMetricsScaleSet/providers/Microsoft.Compute/virtualMachineScaleSets/testingsc")
                                                    .withTimeGrain(Duration.parse("PT2M"))
                                                    .withStatistic(MetricStatisticType.AVERAGE)
                                                    .withTimeWindow(Duration.parse("PT5M"))
                                                    .withTimeAggregation(TimeAggregationType.AVERAGE)
                                                    .withOperator(ComparisonOperationType.GREATER_THAN)
                                                    .withThreshold(15.0)
                                                    .withDividePerInstance(false))
                                            .withScaleAction(
                                                new ScaleAction()
                                                    .withDirection(ScaleDirection.DECREASE)
                                                    .withType(ScaleType.CHANGE_COUNT)
                                                    .withValue("2")
                                                    .withCooldown(Duration.parse("PT6M")))))
                            .withRecurrence(
                                new Recurrence()
                                    .withFrequency(RecurrenceFrequency.WEEK)
                                    .withSchedule(
                                        new RecurrentSchedule()
                                            .withTimeZone("UTC")
                                            .withDays(Arrays.asList("1"))
                                            .withHours(Arrays.asList(5))
                                            .withMinutes(Arrays.asList(15))))))
            .withNotifications(
                Arrays
                    .asList(
                        new AutoscaleNotification()
                            .withEmail(
                                new EmailNotification()
                                    .withSendToSubscriptionAdministrator(true)
                                    .withSendToSubscriptionCoAdministrators(true)
                                    .withCustomEmails(Arrays.asList("gu@ms.com", "ge@ns.net")))
                            .withWebhooks(
                                Arrays
                                    .asList(
                                        new WebhookNotification()
                                            .withServiceUri("http://myservice.com")
                                            .withProperties(mapOf())))))
            .withEnabled(true)
            .withTargetResourceUri(
                "/subscriptions/b67f7fec-69fc-4974-9099-a26bd6ffeda3/resourceGroups/TestingMetricsScaleSet/providers/Microsoft.Compute/virtualMachineScaleSets/testingsc")
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

### Baselines_List

```java
import com.azure.core.util.Context;
import java.time.Duration;

/** Samples for Baselines List. */
public final class BaselinesListSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2019-03-01/examples/metricBaselines.json
     */
    /**
     * Sample code: Get metric baselines.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getMetricBaselines(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .baselines()
            .list(
                "subscriptions/b368ca2f-e298-46b7-b0ab-012281956afa/resourceGroups/vms/providers/Microsoft.Compute/virtualMachines/vm1",
                null,
                null,
                "2019-03-12T11:00:00.000Z/2019-03-12T12:00:00.000Z",
                Duration.parse("PT1H"),
                "average",
                "Low,Medium",
                null,
                null,
                Context.NONE);
    }
}
```

### DataCollectionEndpoints_Create

```java
/** Samples for DataCollectionEndpoints Create. */
public final class DataCollectionEndpointsCreateSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2021-09-01-preview/examples/DataCollectionEndpointsCreate.json
     */
    /**
     * Sample code: Create or update data collection endpoint.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createOrUpdateDataCollectionEndpoint(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .dataCollectionEndpoints()
            .define("myCollectionEndpoint")
            .withRegion("eastus")
            .withExistingResourceGroup("myResourceGroup")
            .create();
    }
}
```

### DataCollectionEndpoints_Delete

```java
import com.azure.core.util.Context;

/** Samples for DataCollectionEndpoints Delete. */
public final class DataCollectionEndpointsDeleteSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2021-09-01-preview/examples/DataCollectionEndpointsDelete.json
     */
    /**
     * Sample code: Delete data collection endpoint.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void deleteDataCollectionEndpoint(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.dataCollectionEndpoints().deleteWithResponse("myResourceGroup", "myCollectionEndpoint", Context.NONE);
    }
}
```

### DataCollectionEndpoints_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DataCollectionEndpoints GetByResourceGroup. */
public final class DataCollectionEndpointsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2021-09-01-preview/examples/DataCollectionEndpointsGet.json
     */
    /**
     * Sample code: Get data collection endpoint.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getDataCollectionEndpoint(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .dataCollectionEndpoints()
            .getByResourceGroupWithResponse("myResourceGroup", "myCollectionEndpoint", Context.NONE);
    }
}
```

### DataCollectionEndpoints_List

```java
import com.azure.core.util.Context;

/** Samples for DataCollectionEndpoints List. */
public final class DataCollectionEndpointsListSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2021-09-01-preview/examples/DataCollectionEndpointsListBySubscription.json
     */
    /**
     * Sample code: List data collection endpoints by subscription.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void listDataCollectionEndpointsBySubscription(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.dataCollectionEndpoints().list(Context.NONE);
    }
}
```

### DataCollectionEndpoints_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DataCollectionEndpoints ListByResourceGroup. */
public final class DataCollectionEndpointsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2021-09-01-preview/examples/DataCollectionEndpointsListByResourceGroup.json
     */
    /**
     * Sample code: List data collection endpoints by resource group.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void listDataCollectionEndpointsByResourceGroup(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.dataCollectionEndpoints().listByResourceGroup("myResourceGroup", Context.NONE);
    }
}
```

### DataCollectionEndpoints_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.monitor.generated.models.DataCollectionEndpointResource;
import java.util.HashMap;
import java.util.Map;

/** Samples for DataCollectionEndpoints Update. */
public final class DataCollectionEndpointsUpdateSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2021-09-01-preview/examples/DataCollectionEndpointsUpdate.json
     */
    /**
     * Sample code: Update data collection endpoint.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void updateDataCollectionEndpoint(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        DataCollectionEndpointResource resource =
            manager
                .dataCollectionEndpoints()
                .getByResourceGroupWithResponse("myResourceGroup", "myCollectionEndpoint", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "A", "tag2", "B", "tag3", "C")).apply();
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

### DataCollectionRuleAssociations_Create

```java
/** Samples for DataCollectionRuleAssociations Create. */
public final class DataCollectionRuleAssociationsCreateSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2021-09-01-preview/examples/DataCollectionRuleAssociationsCreate.json
     */
    /**
     * Sample code: Create or update association.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createOrUpdateAssociation(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .dataCollectionRuleAssociations()
            .define("myAssociation")
            .withExistingResourceUri(
                "subscriptions/703362b3-f278-4e4b-9179-c76eaf41ffc2/resourceGroups/myResourceGroup/providers/Microsoft.Compute/virtualMachines/myVm")
            .create();
    }
}
```

### DataCollectionRuleAssociations_Delete

```java
import com.azure.core.util.Context;

/** Samples for DataCollectionRuleAssociations Delete. */
public final class DataCollectionRuleAssociationsDeleteSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2021-09-01-preview/examples/DataCollectionRuleAssociationsDelete.json
     */
    /**
     * Sample code: Delete association.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void deleteAssociation(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .dataCollectionRuleAssociations()
            .deleteWithResponse(
                "subscriptions/703362b3-f278-4e4b-9179-c76eaf41ffc2/resourceGroups/myResourceGroup/providers/Microsoft.Compute/virtualMachines/myVm",
                "myAssociation",
                Context.NONE);
    }
}
```

### DataCollectionRuleAssociations_Get

```java
import com.azure.core.util.Context;

/** Samples for DataCollectionRuleAssociations Get. */
public final class DataCollectionRuleAssociationsGetSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2021-09-01-preview/examples/DataCollectionRuleAssociationsGet.json
     */
    /**
     * Sample code: Get association.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getAssociation(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .dataCollectionRuleAssociations()
            .getWithResponse(
                "subscriptions/703362b3-f278-4e4b-9179-c76eaf41ffc2/resourceGroups/myResourceGroup/providers/Microsoft.Compute/virtualMachines/myVm",
                "myAssociation",
                Context.NONE);
    }
}
```

### DataCollectionRuleAssociations_ListByDataCollectionEndpoint

```java
import com.azure.core.util.Context;

/** Samples for DataCollectionRuleAssociations ListByDataCollectionEndpoint. */
public final class DataCollectionRuleAssociationsListByDataCollectionEndpointSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2021-09-01-preview/examples/DataCollectionRuleAssociationsListByDataCollectionEndpoint.json
     */
    /**
     * Sample code: List associations for specified data collection endpoint.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void listAssociationsForSpecifiedDataCollectionEndpoint(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .dataCollectionRuleAssociations()
            .listByDataCollectionEndpoint("myResourceGroup", "myDataCollectionEndpointName", Context.NONE);
    }
}
```

### DataCollectionRuleAssociations_ListByResource

```java
import com.azure.core.util.Context;

/** Samples for DataCollectionRuleAssociations ListByResource. */
public final class DataCollectionRuleAssociationsListByResourceSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2021-09-01-preview/examples/DataCollectionRuleAssociationsListByResource.json
     */
    /**
     * Sample code: List associations for specified resource.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void listAssociationsForSpecifiedResource(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .dataCollectionRuleAssociations()
            .listByResource(
                "subscriptions/703362b3-f278-4e4b-9179-c76eaf41ffc2/resourceGroups/myResourceGroup/providers/Microsoft.Compute/virtualMachines/myVm",
                Context.NONE);
    }
}
```

### DataCollectionRuleAssociations_ListByRule

```java
import com.azure.core.util.Context;

/** Samples for DataCollectionRuleAssociations ListByRule. */
public final class DataCollectionRuleAssociationsListByRuleSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2021-09-01-preview/examples/DataCollectionRuleAssociationsListByRule.json
     */
    /**
     * Sample code: List associations for specified data collection rule.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void listAssociationsForSpecifiedDataCollectionRule(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.dataCollectionRuleAssociations().listByRule("myResourceGroup", "myCollectionRule", Context.NONE);
    }
}
```

### DataCollectionRules_Create

```java
/** Samples for DataCollectionRules Create. */
public final class DataCollectionRulesCreateSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2021-09-01-preview/examples/DataCollectionRulesCreate.json
     */
    /**
     * Sample code: Create or update data collection rule.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createOrUpdateDataCollectionRule(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .dataCollectionRules()
            .define("myCollectionRule")
            .withRegion("eastus")
            .withExistingResourceGroup("myResourceGroup")
            .create();
    }
}
```

### DataCollectionRules_Delete

```java
import com.azure.core.util.Context;

/** Samples for DataCollectionRules Delete. */
public final class DataCollectionRulesDeleteSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2021-09-01-preview/examples/DataCollectionRulesDelete.json
     */
    /**
     * Sample code: Delete data collection rule.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void deleteDataCollectionRule(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.dataCollectionRules().deleteWithResponse("myResourceGroup", "myCollectionRule", Context.NONE);
    }
}
```

### DataCollectionRules_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DataCollectionRules GetByResourceGroup. */
public final class DataCollectionRulesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2021-09-01-preview/examples/DataCollectionRulesGet.json
     */
    /**
     * Sample code: Get data collection rule.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getDataCollectionRule(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .dataCollectionRules()
            .getByResourceGroupWithResponse("myResourceGroup", "myCollectionRule", Context.NONE);
    }
}
```

### DataCollectionRules_List

```java
import com.azure.core.util.Context;

/** Samples for DataCollectionRules List. */
public final class DataCollectionRulesListSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2021-09-01-preview/examples/DataCollectionRulesListBySubscription.json
     */
    /**
     * Sample code: List data collection rules by subscription.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void listDataCollectionRulesBySubscription(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.dataCollectionRules().list(Context.NONE);
    }
}
```

### DataCollectionRules_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DataCollectionRules ListByResourceGroup. */
public final class DataCollectionRulesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2021-09-01-preview/examples/DataCollectionRulesListByResourceGroup.json
     */
    /**
     * Sample code: List data collection rules by resource group.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void listDataCollectionRulesByResourceGroup(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.dataCollectionRules().listByResourceGroup("myResourceGroup", Context.NONE);
    }
}
```

### DataCollectionRules_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.monitor.generated.models.DataCollectionRuleResource;
import java.util.HashMap;
import java.util.Map;

/** Samples for DataCollectionRules Update. */
public final class DataCollectionRulesUpdateSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2021-09-01-preview/examples/DataCollectionRulesUpdate.json
     */
    /**
     * Sample code: Update data collection rule.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void updateDataCollectionRule(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        DataCollectionRuleResource resource =
            manager
                .dataCollectionRules()
                .getByResourceGroupWithResponse("myResourceGroup", "myCollectionRule", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "A", "tag2", "B", "tag3", "C")).apply();
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

### DiagnosticSettingsCategory_Get

```java
import com.azure.core.util.Context;

/** Samples for DiagnosticSettingsCategory Get. */
public final class DiagnosticSettingsCategoryGetSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2017-05-01-preview/examples/getDiagnosticSettingsCategory.json
     */
    /**
     * Sample code: Gets the diagnostic setting.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getsTheDiagnosticSetting(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .diagnosticSettingsCategories()
            .getWithResponse(
                "subscriptions/1a66ce04-b633-4a0b-b2bc-a912ec8986a6/resourcegroups/viruela1/providers/microsoft.logic/workflows/viruela6",
                "WorkflowRuntime",
                Context.NONE);
    }
}
```

### DiagnosticSettingsCategory_List

```java
import com.azure.core.util.Context;

/** Samples for DiagnosticSettingsCategory List. */
public final class DiagnosticSettingsCategoryListSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2017-05-01-preview/examples/listDiagnosticSettingsCategories.json
     */
    /**
     * Sample code: Gets the diagnostic setting.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getsTheDiagnosticSetting(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .diagnosticSettingsCategories()
            .listWithResponse(
                "subscriptions/1a66ce04-b633-4a0b-b2bc-a912ec8986a6/resourcegroups/viruela1/providers/microsoft.logic/workflows/viruela6",
                Context.NONE);
    }
}
```

### DiagnosticSettingsOperation_CreateOrUpdate

```java
import com.azure.resourcemanager.monitor.generated.models.LogSettings;
import com.azure.resourcemanager.monitor.generated.models.MetricSettings;
import com.azure.resourcemanager.monitor.generated.models.RetentionPolicy;
import java.util.Arrays;

/** Samples for DiagnosticSettingsOperation CreateOrUpdate. */
public final class DiagnosticSettingsOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2017-05-01-preview/examples/createOrUpdateDiagnosticSetting.json
     */
    /**
     * Sample code: Creates or Updates the diagnostic setting.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createsOrUpdatesTheDiagnosticSetting(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .diagnosticSettingsOperations()
            .define("mysetting")
            .withExistingResourceUri(
                "subscriptions/1a66ce04-b633-4a0b-b2bc-a912ec8986a6/resourcegroups/viruela1/providers/microsoft.logic/workflows/viruela6")
            .withStorageAccountId(
                "/subscriptions/df602c9c-7aa0-407d-a6fb-eb20c8bd1192/resourceGroups/apptest/providers/Microsoft.Storage/storageAccounts/appteststorage1")
            .withEventHubAuthorizationRuleId(
                "/subscriptions/1a66ce04-b633-4a0b-b2bc-a912ec8986a6/resourceGroups/montest/providers/microsoft.eventhub/namespaces/mynamespace/eventhubs/myeventhub/authorizationrules/myrule")
            .withEventHubName("myeventhub")
            .withMetrics(
                Arrays
                    .asList(
                        new MetricSettings()
                            .withCategory("WorkflowMetrics")
                            .withEnabled(true)
                            .withRetentionPolicy(new RetentionPolicy().withEnabled(false).withDays(0))))
            .withLogs(
                Arrays
                    .asList(
                        new LogSettings()
                            .withCategory("WorkflowRuntime")
                            .withEnabled(true)
                            .withRetentionPolicy(new RetentionPolicy().withEnabled(false).withDays(0))))
            .withWorkspaceId("")
            .withLogAnalyticsDestinationType("Dedicated")
            .create();
    }
}
```

### DiagnosticSettingsOperation_Delete

```java
import com.azure.core.util.Context;

/** Samples for DiagnosticSettingsOperation Delete. */
public final class DiagnosticSettingsOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2017-05-01-preview/examples/deleteDiagnosticSetting.json
     */
    /**
     * Sample code: Deletes the diagnostic setting.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void deletesTheDiagnosticSetting(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .diagnosticSettingsOperations()
            .deleteWithResponse(
                "subscriptions/1a66ce04-b633-4a0b-b2bc-a912ec8986a6/resourcegroups/viruela1/providers/microsoft.logic/workflows/viruela6",
                "mysetting",
                Context.NONE);
    }
}
```

### DiagnosticSettingsOperation_Get

```java
import com.azure.core.util.Context;

/** Samples for DiagnosticSettingsOperation Get. */
public final class DiagnosticSettingsOperationGetSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2017-05-01-preview/examples/getDiagnosticSetting.json
     */
    /**
     * Sample code: Gets the diagnostic setting.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getsTheDiagnosticSetting(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .diagnosticSettingsOperations()
            .getWithResponse(
                "subscriptions/1a66ce04-b633-4a0b-b2bc-a912ec8986a6/resourcegroups/viruela1/providers/microsoft.logic/workflows/viruela6",
                "mysetting",
                Context.NONE);
    }
}
```

### DiagnosticSettingsOperation_List

```java
import com.azure.core.util.Context;

/** Samples for DiagnosticSettingsOperation List. */
public final class DiagnosticSettingsOperationListSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2017-05-01-preview/examples/listDiagnosticSettings.json
     */
    /**
     * Sample code: Gets the diagnostic setting.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getsTheDiagnosticSetting(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .diagnosticSettingsOperations()
            .listWithResponse(
                "subscriptions/1a66ce04-b633-4a0b-b2bc-a912ec8986a6/resourcegroups/viruela1/providers/microsoft.logic/workflows/viruela6",
                Context.NONE);
    }
}
```

### EventCategories_List

```java
import com.azure.core.util.Context;

/** Samples for EventCategories List. */
public final class EventCategoriesListSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2015-04-01/examples/GetEventCategories.json
     */
    /**
     * Sample code: Get event categories.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getEventCategories(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.eventCategories().list(Context.NONE);
    }
}
```

### LogProfiles_CreateOrUpdate

```java
import com.azure.resourcemanager.monitor.generated.models.RetentionPolicy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for LogProfiles CreateOrUpdate. */
public final class LogProfilesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2016-03-01/examples/createOrUpdateLogProfile.json
     */
    /**
     * Sample code: Create or update a log profile.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createOrUpdateALogProfile(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .logProfiles()
            .define("Rac46PostSwapRG")
            .withRegion("")
            .withLocations(Arrays.asList("global"))
            .withCategories(Arrays.asList("Write", "Delete", "Action"))
            .withRetentionPolicy(new RetentionPolicy().withEnabled(true).withDays(3))
            .withTags(mapOf())
            .withStorageAccountId(
                "/subscriptions/df602c9c-7aa0-407d-a6fb-eb20c8bd1192/resourceGroups/JohnKemTest/providers/Microsoft.Storage/storageAccounts/johnkemtest8162")
            .withServiceBusRuleId("")
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

### LogProfiles_Delete

```java
import com.azure.core.util.Context;

/** Samples for LogProfiles Delete. */
public final class LogProfilesDeleteSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2016-03-01/examples/deleteLogProfile.json
     */
    /**
     * Sample code: Delete log profile.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void deleteLogProfile(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.logProfiles().deleteWithResponse("Rac46PostSwapRG", Context.NONE);
    }
}
```

### LogProfiles_Get

```java
import com.azure.core.util.Context;

/** Samples for LogProfiles Get. */
public final class LogProfilesGetSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2016-03-01/examples/getLogProfile.json
     */
    /**
     * Sample code: Get log profile.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getLogProfile(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.logProfiles().getWithResponse("default", Context.NONE);
    }
}
```

### LogProfiles_List

```java
import com.azure.core.util.Context;

/** Samples for LogProfiles List. */
public final class LogProfilesListSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2016-03-01/examples/listLogProfile.json
     */
    /**
     * Sample code: List log profiles.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void listLogProfiles(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.logProfiles().list(Context.NONE);
    }
}
```

### LogProfiles_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.monitor.generated.models.LogProfileResource;
import com.azure.resourcemanager.monitor.generated.models.RetentionPolicy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for LogProfiles Update. */
public final class LogProfilesUpdateSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2016-03-01/examples/patchLogProfile.json
     */
    /**
     * Sample code: Patch a log profile.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void patchALogProfile(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        LogProfileResource resource = manager.logProfiles().getWithResponse("Rac46PostSwapRG", Context.NONE).getValue();
        resource
            .update()
            .withTags(mapOf("key1", "value1"))
            .withStorageAccountId(
                "/subscriptions/df602c9c-7aa0-407d-a6fb-eb20c8bd1192/resourceGroups/JohnKemTest/providers/Microsoft.Storage/storageAccounts/johnkemtest8162")
            .withServiceBusRuleId("")
            .withLocations(Arrays.asList("global"))
            .withCategories(Arrays.asList("Write", "Delete", "Action"))
            .withRetentionPolicy(new RetentionPolicy().withEnabled(true).withDays(3))
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

### MetricAlerts_CreateOrUpdate

```java
import com.azure.resourcemanager.monitor.generated.models.AggregationTypeEnum;
import com.azure.resourcemanager.monitor.generated.models.DynamicMetricCriteria;
import com.azure.resourcemanager.monitor.generated.models.DynamicThresholdFailingPeriods;
import com.azure.resourcemanager.monitor.generated.models.DynamicThresholdOperator;
import com.azure.resourcemanager.monitor.generated.models.DynamicThresholdSensitivity;
import com.azure.resourcemanager.monitor.generated.models.MetricAlertAction;
import com.azure.resourcemanager.monitor.generated.models.MetricAlertMultipleResourceMultipleMetricCriteria;
import com.azure.resourcemanager.monitor.generated.models.MetricAlertSingleResourceMultipleMetricCriteria;
import com.azure.resourcemanager.monitor.generated.models.MetricCriteria;
import com.azure.resourcemanager.monitor.generated.models.MetricDimension;
import com.azure.resourcemanager.monitor.generated.models.Operator;
import com.azure.resourcemanager.monitor.generated.models.WebtestLocationAvailabilityCriteria;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for MetricAlerts CreateOrUpdate. */
public final class MetricAlertsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-03-01/examples/createOrUpdateMetricAlertSubscription.json
     */
    /**
     * Sample code: Create or update an alert rule on Subscription.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createOrUpdateAnAlertRuleOnSubscription(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .metricAlerts()
            .define("MetricAlertAtSubscriptionLevel")
            .withRegion("global")
            .withExistingResourceGroup("gigtest")
            .withSeverity(3)
            .withEnabled(true)
            .withScopes(Arrays.asList("/subscriptions/14ddf0c5-77c5-4b53-84f6-e1fa43ad68f7"))
            .withEvaluationFrequency(Duration.parse("PT1M"))
            .withWindowSize(Duration.parse("PT15M"))
            .withCriteria(
                new MetricAlertMultipleResourceMultipleMetricCriteria()
                    .withAllOf(
                        Arrays
                            .asList(
                                new MetricCriteria()
                                    .withName("High_CPU_80")
                                    .withMetricName("Percentage CPU")
                                    .withMetricNamespace("microsoft.compute/virtualmachines")
                                    .withTimeAggregation(AggregationTypeEnum.AVERAGE)
                                    .withDimensions(Arrays.asList())
                                    .withOperator(Operator.GREATER_THAN)
                                    .withThreshold(80.5))))
            .withTags(mapOf())
            .withDescription("This is the description of the rule1")
            .withTargetResourceType("Microsoft.Compute/virtualMachines")
            .withTargetResourceRegion("southcentralus")
            .withAutoMitigate(true)
            .withActions(
                Arrays
                    .asList(
                        new MetricAlertAction()
                            .withActionGroupId(
                                "/subscriptions/14ddf0c5-77c5-4b53-84f6-e1fa43ad68f7/resourcegroups/gigtest/providers/microsoft.insights/actiongroups/group2")
                            .withWebhookProperties(mapOf("key11", "value11", "key12", "value12"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-03-01/examples/createOrUpdateMetricAlertResourceGroup.json
     */
    /**
     * Sample code: Create or update an alert rule on Resource group(s).
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createOrUpdateAnAlertRuleOnResourceGroupS(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .metricAlerts()
            .define("MetricAlertAtResourceGroupLevel")
            .withRegion("global")
            .withExistingResourceGroup("gigtest1")
            .withSeverity(3)
            .withEnabled(true)
            .withScopes(
                Arrays
                    .asList(
                        "/subscriptions/14ddf0c5-77c5-4b53-84f6-e1fa43ad68f7/resourceGroups/gigtest1",
                        "/subscriptions/14ddf0c5-77c5-4b53-84f6-e1fa43ad68f7/resourceGroups/gigtest2"))
            .withEvaluationFrequency(Duration.parse("PT1M"))
            .withWindowSize(Duration.parse("PT15M"))
            .withCriteria(
                new MetricAlertMultipleResourceMultipleMetricCriteria()
                    .withAllOf(
                        Arrays
                            .asList(
                                new MetricCriteria()
                                    .withName("High_CPU_80")
                                    .withMetricName("Percentage CPU")
                                    .withMetricNamespace("microsoft.compute/virtualmachines")
                                    .withTimeAggregation(AggregationTypeEnum.AVERAGE)
                                    .withDimensions(Arrays.asList())
                                    .withOperator(Operator.GREATER_THAN)
                                    .withThreshold(80.5))))
            .withTags(mapOf())
            .withDescription("This is the description of the rule1")
            .withTargetResourceType("Microsoft.Compute/virtualMachines")
            .withTargetResourceRegion("southcentralus")
            .withAutoMitigate(true)
            .withActions(
                Arrays
                    .asList(
                        new MetricAlertAction()
                            .withActionGroupId(
                                "/subscriptions/14ddf0c5-77c5-4b53-84f6-e1fa43ad68f7/resourcegroups/gigtest/providers/microsoft.insights/actiongroups/group2")
                            .withWebhookProperties(mapOf("key11", "value11", "key12", "value12"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-03-01/examples/createOrUpdateMetricAlertWithDimensions.json
     */
    /**
     * Sample code: Create or update an alert rules with dimensions.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createOrUpdateAnAlertRulesWithDimensions(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .metricAlerts()
            .define("MetricAlertOnMultipleDimensions")
            .withRegion("global")
            .withExistingResourceGroup("gigtest")
            .withSeverity(3)
            .withEnabled(true)
            .withScopes(
                Arrays
                    .asList(
                        "/subscriptions/14ddf0c5-77c5-4b53-84f6-e1fa43ad68f7/resourceGroups/gigtest/providers/Microsoft.KeyVault/vaults/keyVaultResource"))
            .withEvaluationFrequency(Duration.parse("PT1H"))
            .withWindowSize(Duration.parse("P1D"))
            .withCriteria(
                new MetricAlertMultipleResourceMultipleMetricCriteria()
                    .withAllOf(
                        Arrays
                            .asList(
                                new MetricCriteria()
                                    .withName("Metric1")
                                    .withMetricName("Availability")
                                    .withMetricNamespace("Microsoft.KeyVault/vaults")
                                    .withTimeAggregation(AggregationTypeEnum.AVERAGE)
                                    .withDimensions(
                                        Arrays
                                            .asList(
                                                new MetricDimension()
                                                    .withName("ActivityName")
                                                    .withOperator("Include")
                                                    .withValues(Arrays.asList("*")),
                                                new MetricDimension()
                                                    .withName("StatusCode")
                                                    .withOperator("Include")
                                                    .withValues(Arrays.asList("200"))))
                                    .withOperator(Operator.GREATER_THAN)
                                    .withThreshold(55.0))))
            .withTags(mapOf())
            .withDescription("This is the description of the rule1")
            .withAutoMitigate(true)
            .withActions(
                Arrays
                    .asList(
                        new MetricAlertAction()
                            .withActionGroupId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/gigtest/providers/microsoft.insights/actiongroups/group2")
                            .withWebhookProperties(mapOf("key11", "value11", "key12", "value12"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-03-01/examples/createOrUpdateWebTestMetricAlert.json
     */
    /**
     * Sample code: Create or update a web test alert rule.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createOrUpdateAWebTestAlertRule(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .metricAlerts()
            .define("webtest-name-example")
            .withRegion("global")
            .withExistingResourceGroup("rg-example")
            .withSeverity(4)
            .withEnabled(true)
            .withScopes(
                Arrays
                    .asList(
                        "/subscriptions/12345678-1234-1234-1234-123456789101/resourcegroups/rg-example/providers/microsoft.insights/webtests/component-example",
                        "/subscriptions/12345678-1234-1234-1234-123456789101/resourcegroups/rg-example/providers/microsoft.insights/components/webtest-name-example"))
            .withEvaluationFrequency(Duration.parse("PT1M"))
            .withWindowSize(Duration.parse("PT15M"))
            .withCriteria(
                new WebtestLocationAvailabilityCriteria()
                    .withWebTestId(
                        "/subscriptions/12345678-1234-1234-1234-123456789101/resourcegroups/rg-example/providers/microsoft.insights/webtests/component-example")
                    .withComponentId(
                        "/subscriptions/12345678-1234-1234-1234-123456789101/resourcegroups/rg-example/providers/microsoft.insights/components/webtest-name-example")
                    .withFailedLocationCount(2f))
            .withTags(
                mapOf(
                    "hidden-link:/subscriptions/12345678-1234-1234-1234-123456789101/resourcegroups/rg-example/providers/microsoft.insights/components/webtest-name-example",
                    "Resource",
                    "hidden-link:/subscriptions/12345678-1234-1234-1234-123456789101/resourcegroups/rg-example/providers/microsoft.insights/webtests/component-example",
                    "Resource"))
            .withDescription("Automatically created alert rule for availability test \"component-example\" a")
            .withActions(Arrays.asList())
            .create();
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-03-01/examples/createOrUpdateDynamicMetricAlertSingleResource.json
     */
    /**
     * Sample code: Create or update a dynamic alert rule for Single Resource.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createOrUpdateADynamicAlertRuleForSingleResource(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .metricAlerts()
            .define("chiricutin")
            .withRegion("global")
            .withExistingResourceGroup("gigtest")
            .withSeverity(3)
            .withEnabled(true)
            .withScopes(
                Arrays
                    .asList(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/gigtest/providers/Microsoft.Compute/virtualMachines/gigwadme"))
            .withEvaluationFrequency(Duration.parse("PT1M"))
            .withWindowSize(Duration.parse("PT15M"))
            .withCriteria(
                new MetricAlertMultipleResourceMultipleMetricCriteria()
                    .withAllOf(
                        Arrays
                            .asList(
                                new DynamicMetricCriteria()
                                    .withName("High_CPU_80")
                                    .withMetricName("Percentage CPU")
                                    .withMetricNamespace("microsoft.compute/virtualmachines")
                                    .withTimeAggregation(AggregationTypeEnum.AVERAGE)
                                    .withDimensions(Arrays.asList())
                                    .withOperator(DynamicThresholdOperator.GREATER_OR_LESS_THAN)
                                    .withAlertSensitivity(DynamicThresholdSensitivity.MEDIUM)
                                    .withFailingPeriods(
                                        new DynamicThresholdFailingPeriods()
                                            .withNumberOfEvaluationPeriods(4f)
                                            .withMinFailingPeriodsToAlert(4f))
                                    .withIgnoreDataBefore(OffsetDateTime.parse("2019-04-04T21:00:00.000Z")))))
            .withTags(mapOf())
            .withDescription("This is the description of the rule1")
            .withAutoMitigate(true)
            .withActions(
                Arrays
                    .asList(
                        new MetricAlertAction()
                            .withActionGroupId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/gigtest/providers/microsoft.insights/actiongroups/group2")
                            .withWebhookProperties(mapOf("key11", "value11", "key12", "value12"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-03-01/examples/createOrUpdateMetricAlertSingleResource.json
     */
    /**
     * Sample code: Create or update an alert rule for Single Resource.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createOrUpdateAnAlertRuleForSingleResource(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .metricAlerts()
            .define("chiricutin")
            .withRegion("global")
            .withExistingResourceGroup("gigtest")
            .withSeverity(3)
            .withEnabled(true)
            .withScopes(
                Arrays
                    .asList(
                        "/subscriptions/14ddf0c5-77c5-4b53-84f6-e1fa43ad68f7/resourceGroups/gigtest/providers/Microsoft.Compute/virtualMachines/gigwadme"))
            .withEvaluationFrequency(Duration.parse("Pt1m"))
            .withWindowSize(Duration.parse("Pt15m"))
            .withCriteria(
                new MetricAlertSingleResourceMultipleMetricCriteria()
                    .withAllOf(
                        Arrays
                            .asList(
                                new MetricCriteria()
                                    .withName("High_CPU_80")
                                    .withMetricName("\\Processor(_Total)\\% Processor Time")
                                    .withTimeAggregation(AggregationTypeEnum.AVERAGE)
                                    .withDimensions(Arrays.asList())
                                    .withOperator(Operator.GREATER_THAN)
                                    .withThreshold(80.5))))
            .withTags(mapOf())
            .withDescription("This is the description of the rule1")
            .withAutoMitigate(true)
            .withActions(
                Arrays
                    .asList(
                        new MetricAlertAction()
                            .withActionGroupId(
                                "/subscriptions/14ddf0c5-77c5-4b53-84f6-e1fa43ad68f7/resourcegroups/gigtest/providers/microsoft.insights/actiongroups/group2")
                            .withWebhookProperties(mapOf("key11", "value11", "key12", "value12"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-03-01/examples/createOrUpdateMetricAlertMultipleResource.json
     */
    /**
     * Sample code: Create or update an alert rule for Multiple Resource.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createOrUpdateAnAlertRuleForMultipleResource(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .metricAlerts()
            .define("MetricAlertOnMultipleResources")
            .withRegion("global")
            .withExistingResourceGroup("gigtest")
            .withSeverity(3)
            .withEnabled(true)
            .withScopes(
                Arrays
                    .asList(
                        "/subscriptions/14ddf0c5-77c5-4b53-84f6-e1fa43ad68f7/resourceGroups/gigtest/providers/Microsoft.Compute/virtualMachines/gigwadme1",
                        "/subscriptions/14ddf0c5-77c5-4b53-84f6-e1fa43ad68f7/resourceGroups/gigtest/providers/Microsoft.Compute/virtualMachines/gigwadme2"))
            .withEvaluationFrequency(Duration.parse("PT1M"))
            .withWindowSize(Duration.parse("PT15M"))
            .withCriteria(
                new MetricAlertMultipleResourceMultipleMetricCriteria()
                    .withAllOf(
                        Arrays
                            .asList(
                                new MetricCriteria()
                                    .withName("High_CPU_80")
                                    .withMetricName("Percentage CPU")
                                    .withMetricNamespace("microsoft.compute/virtualmachines")
                                    .withTimeAggregation(AggregationTypeEnum.AVERAGE)
                                    .withDimensions(Arrays.asList())
                                    .withOperator(Operator.GREATER_THAN)
                                    .withThreshold(80.5))))
            .withTags(mapOf())
            .withDescription("This is the description of the rule1")
            .withTargetResourceType("Microsoft.Compute/virtualMachines")
            .withTargetResourceRegion("southcentralus")
            .withAutoMitigate(true)
            .withActions(
                Arrays
                    .asList(
                        new MetricAlertAction()
                            .withActionGroupId(
                                "/subscriptions/14ddf0c5-77c5-4b53-84f6-e1fa43ad68f7/resourcegroups/gigtest/providers/microsoft.insights/actiongroups/group2")
                            .withWebhookProperties(mapOf("key11", "value11", "key12", "value12"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-03-01/examples/createOrUpdateDynamicMetricAlertMultipleResource.json
     */
    /**
     * Sample code: Create or update a dynamic alert rule for Multiple Resources.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createOrUpdateADynamicAlertRuleForMultipleResources(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .metricAlerts()
            .define("MetricAlertOnMultipleResources")
            .withRegion("global")
            .withExistingResourceGroup("gigtest")
            .withSeverity(3)
            .withEnabled(true)
            .withScopes(
                Arrays
                    .asList(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/gigtest/providers/Microsoft.Compute/virtualMachines/gigwadme1",
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/gigtest/providers/Microsoft.Compute/virtualMachines/gigwadme2"))
            .withEvaluationFrequency(Duration.parse("PT1M"))
            .withWindowSize(Duration.parse("PT15M"))
            .withCriteria(
                new MetricAlertMultipleResourceMultipleMetricCriteria()
                    .withAllOf(
                        Arrays
                            .asList(
                                new DynamicMetricCriteria()
                                    .withName("High_CPU_80")
                                    .withMetricName("Percentage CPU")
                                    .withMetricNamespace("microsoft.compute/virtualmachines")
                                    .withTimeAggregation(AggregationTypeEnum.AVERAGE)
                                    .withDimensions(Arrays.asList())
                                    .withOperator(DynamicThresholdOperator.GREATER_OR_LESS_THAN)
                                    .withAlertSensitivity(DynamicThresholdSensitivity.MEDIUM)
                                    .withFailingPeriods(
                                        new DynamicThresholdFailingPeriods()
                                            .withNumberOfEvaluationPeriods(4f)
                                            .withMinFailingPeriodsToAlert(4f)))))
            .withTags(mapOf())
            .withDescription("This is the description of the rule1")
            .withTargetResourceType("Microsoft.Compute/virtualMachines")
            .withTargetResourceRegion("southcentralus")
            .withAutoMitigate(true)
            .withActions(
                Arrays
                    .asList(
                        new MetricAlertAction()
                            .withActionGroupId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/gigtest/providers/microsoft.insights/actiongroups/group2")
                            .withWebhookProperties(mapOf("key11", "value11", "key12", "value12"))))
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

### MetricAlerts_Delete

```java
import com.azure.core.util.Context;

/** Samples for MetricAlerts Delete. */
public final class MetricAlertsDeleteSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-03-01/examples/deleteMetricAlert.json
     */
    /**
     * Sample code: Delete an alert rule.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void deleteAnAlertRule(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.metricAlerts().deleteWithResponse("gigtest", "chiricutin", Context.NONE);
    }
}
```

### MetricAlerts_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for MetricAlerts GetByResourceGroup. */
public final class MetricAlertsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-03-01/examples/getMetricAlertResourceGroup.json
     */
    /**
     * Sample code: Get an alert rule on resource group(s).
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getAnAlertRuleOnResourceGroupS(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .metricAlerts()
            .getByResourceGroupWithResponse("gigtest1", "MetricAlertAtResourceGroupLevel", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-03-01/examples/getDynamicMetricAlertMultipleResource.json
     */
    /**
     * Sample code: Get a dynamic alert rule for multiple resources.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getADynamicAlertRuleForMultipleResources(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .metricAlerts()
            .getByResourceGroupWithResponse("gigtest", "MetricAlertOnMultipleResources", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-03-01/examples/getWebTestMetricAlert.json
     */
    /**
     * Sample code: Get a web test alert rule.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getAWebTestAlertRule(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.metricAlerts().getByResourceGroupWithResponse("rg-example", "webtest-name-example", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-03-01/examples/getMetricAlertSingleResource.json
     */
    /**
     * Sample code: Get an alert rule for single resource.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getAnAlertRuleForSingleResource(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.metricAlerts().getByResourceGroupWithResponse("gigtest", "chiricutin", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-03-01/examples/getMetricAlertSubscription.json
     */
    /**
     * Sample code: Get an alert rule on subscription.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getAnAlertRuleOnSubscription(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .metricAlerts()
            .getByResourceGroupWithResponse("gigtest", "MetricAlertAtSubscriptionLevel", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-03-01/examples/getDynamicMetricAlertSingleResource.json
     */
    /**
     * Sample code: Get a dynamic alert rule for single resource.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getADynamicAlertRuleForSingleResource(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.metricAlerts().getByResourceGroupWithResponse("gigtest", "chiricutin", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-03-01/examples/getMetricAlertMultipleResource.json
     */
    /**
     * Sample code: Get an alert rule for multiple resources.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getAnAlertRuleForMultipleResources(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .metricAlerts()
            .getByResourceGroupWithResponse("gigtest", "MetricAlertOnMultipleResources", Context.NONE);
    }
}
```

### MetricAlerts_List

```java
import com.azure.core.util.Context;

/** Samples for MetricAlerts List. */
public final class MetricAlertsListSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-03-01/examples/listMetricAlert.json
     */
    /**
     * Sample code: List metric alert rules.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void listMetricAlertRules(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.metricAlerts().list(Context.NONE);
    }
}
```

### MetricAlerts_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for MetricAlerts ListByResourceGroup. */
public final class MetricAlertsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-03-01/examples/listMetricAlert.json
     */
    /**
     * Sample code: List metric alert rules.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void listMetricAlertRules(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.metricAlerts().listByResourceGroup("gigtest", Context.NONE);
    }
}
```

### MetricAlerts_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.monitor.generated.models.AggregationTypeEnum;
import com.azure.resourcemanager.monitor.generated.models.MetricAlertAction;
import com.azure.resourcemanager.monitor.generated.models.MetricAlertResource;
import com.azure.resourcemanager.monitor.generated.models.MetricAlertSingleResourceMultipleMetricCriteria;
import com.azure.resourcemanager.monitor.generated.models.MetricCriteria;
import com.azure.resourcemanager.monitor.generated.models.Operator;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for MetricAlerts Update. */
public final class MetricAlertsUpdateSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-03-01/examples/UpdateMetricAlert.json
     */
    /**
     * Sample code: Create or update an alert rule.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createOrUpdateAnAlertRule(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        MetricAlertResource resource =
            manager.metricAlerts().getByResourceGroupWithResponse("gigtest", "chiricutin", Context.NONE).getValue();
        resource
            .update()
            .withTags(mapOf())
            .withDescription("This is the description of the rule1")
            .withSeverity(3)
            .withEnabled(true)
            .withScopes(
                Arrays
                    .asList(
                        "/subscriptions/14ddf0c5-77c5-4b53-84f6-e1fa43ad68f7/resourceGroups/gigtest/providers/Microsoft.Compute/virtualMachines/gigwadme"))
            .withEvaluationFrequency(Duration.parse("Pt1m"))
            .withWindowSize(Duration.parse("Pt15m"))
            .withCriteria(
                new MetricAlertSingleResourceMultipleMetricCriteria()
                    .withAllOf(
                        Arrays
                            .asList(
                                new MetricCriteria()
                                    .withName("High_CPU_80")
                                    .withMetricName("\\Processor(_Total)\\% Processor Time")
                                    .withTimeAggregation(AggregationTypeEnum.AVERAGE)
                                    .withDimensions(Arrays.asList())
                                    .withOperator(Operator.GREATER_THAN)
                                    .withThreshold(80.5))))
            .withAutoMitigate(true)
            .withActions(
                Arrays
                    .asList(
                        new MetricAlertAction()
                            .withActionGroupId(
                                "/subscriptions/14ddf0c5-77c5-4b53-84f6-e1fa43ad68f7/resourcegroups/gigtest/providers/microsoft.insights/actiongroups/group2")
                            .withWebhookProperties(mapOf("key11", "value11", "key12", "value12"))))
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

### MetricAlertsStatus_List

```java
import com.azure.core.util.Context;

/** Samples for MetricAlertsStatus List. */
public final class MetricAlertsStatusListSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-03-01/examples/getMetricAlertStatus.json
     */
    /**
     * Sample code: Get an alert rule status.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getAnAlertRuleStatus(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.metricAlertsStatus().listWithResponse("gigtest", "chiricutin", Context.NONE);
    }
}
```

### MetricAlertsStatus_ListByName

```java
import com.azure.core.util.Context;

/** Samples for MetricAlertsStatus ListByName. */
public final class MetricAlertsStatusListByNameSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-03-01/examples/getMetricAlertStatusByName.json
     */
    /**
     * Sample code: Get an alert rule status.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getAnAlertRuleStatus(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .metricAlertsStatus()
            .listByNameWithResponse(
                "EastUs",
                "custom1",
                "cmVzb3VyY2VJZD0vc3Vic2NyaXB0aW9ucy8xNGRkZjBjNS03N2M1LTRiNTMtODRmNi1lMWZhNDNhZDY4ZjcvcmVzb3VyY2VHcm91cHMvZ2lndGVzdC9wcm92aWRlcnMvTWljcm9zb2Z0LkNvbXB1dGUvdmlydHVhbE1hY2hpbmVzL2dpZ3dhZG1l",
                Context.NONE);
    }
}
```

### MetricDefinitions_List

```java
import com.azure.core.util.Context;

/** Samples for MetricDefinitions List. */
public final class MetricDefinitionsListSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-01-01/examples/GetMetricDefinitions.json
     */
    /**
     * Sample code: Get Metric Definitions without filter.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getMetricDefinitionsWithoutFilter(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .metricDefinitions()
            .list(
                "subscriptions/07c0b09d-9f69-4e6e-8d05-f59f67299cb2/resourceGroups/Rac46PostSwapRG/providers/Microsoft.Web/sites/alertruleTest/providers/microsoft.insights/metricDefinitions",
                "Microsoft.Web/sites",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-01-01/examples/GetMetricDefinitionsApplicationInsights.json
     */
    /**
     * Sample code: Get Application Insights Metric Definitions without filter.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getApplicationInsightsMetricDefinitionsWithoutFilter(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .metricDefinitions()
            .list(
                "subscriptions/182c901a-129a-4f5d-86e4-cc6b294590a2/resourceGroups/hyr-log/providers/microsoft.insights/components/f1-bill/providers/microsoft.insights/metricdefinitions",
                "microsoft.insights/components",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-01-01/examples/GetMetricDefinitionsMetricClass.json
     */
    /**
     * Sample code: Get StorageCache Metric Definitions with metric class.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getStorageCacheMetricDefinitionsWithMetricClass(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .metricDefinitions()
            .list(
                "subscriptions/46841c0e-69c8-4b17-af46-6626ecb15fc2/resourceGroups/adgarntptestrg/providers/Microsoft.StorageCache/caches/adgarntptestcache",
                "microsoft.storagecache/caches",
                Context.NONE);
    }
}
```

### MetricNamespaces_List

```java
import com.azure.core.util.Context;

/** Samples for MetricNamespaces List. */
public final class MetricNamespacesListSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2017-12-01-preview/examples/GetMetricNamespaces.json
     */
    /**
     * Sample code: Get Metric Namespaces without filter.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getMetricNamespacesWithoutFilter(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .metricNamespaces()
            .list(
                "subscriptions/182c901a-129a-4f5d-86e4-cc6b294590a2/resourceGroups/hyr-log/providers/microsoft.insights/components/f1-bill",
                "2020-08-31T15:53:00Z",
                Context.NONE);
    }
}
```

### Metrics_List

```java
import com.azure.core.util.Context;
import java.time.Duration;

/** Samples for Metrics List. */
public final class MetricsListSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-01-01/examples/GetMetric.json
     */
    /**
     * Sample code: Get Metric for data.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getMetricForData(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .metrics()
            .listWithResponse(
                "subscriptions/b324c52b-4073-4807-93af-e07d289c093e/resourceGroups/test/providers/Microsoft.Storage/storageAccounts/larryshoebox/blobServices/default",
                "2017-04-14T02:20:00Z/2017-04-14T04:20:00Z",
                Duration.parse("PT1M"),
                null,
                "Average,count",
                3,
                "Average asc",
                "BlobType eq '*'",
                null,
                "Microsoft.Storage/storageAccounts/blobServices",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-01-01/examples/GetMetricMetadata.json
     */
    /**
     * Sample code: Get Metric for metadata.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getMetricForMetadata(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .metrics()
            .listWithResponse(
                "subscriptions/b324c52b-4073-4807-93af-e07d289c093e/resourceGroups/test/providers/Microsoft.Storage/storageAccounts/larryshoebox/blobServices/default",
                "2017-04-14T02:20:00Z/2017-04-14T04:20:00Z",
                Duration.parse("PT1M"),
                null,
                "Average,count",
                3,
                "Average asc",
                "BlobType eq '*'",
                null,
                "Microsoft.Storage/storageAccounts/blobServices",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-01-01/examples/GetMetricError.json
     */
    /**
     * Sample code: Get Metric with error.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getMetricWithError(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .metrics()
            .listWithResponse(
                "subscriptions/ac41e21f-afd6-4a79-8070-f01eba278f97/resourceGroups/todking/providers/Microsoft.DocumentDb/databaseAccounts/tk-cosmos-mongo",
                "2021-06-07T21:51:00Z/2021-06-08T01:51:00Z",
                Duration.parse("FULL"),
                "MongoRequestsCount,MongoRequests",
                "average",
                null,
                null,
                null,
                null,
                "microsoft.documentdb/databaseaccounts",
                Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2015-04-01/examples/OperationList.json
     */
    /**
     * Sample code: Get a list of operations for a resource provider.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getAListOfOperationsForAResourceProvider(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.operations().listWithResponse(Context.NONE);
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.monitor.generated.models.PrivateLinkServiceConnectionStateProperty;

/** Samples for PrivateEndpointConnections CreateOrUpdate. */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2019-10-17-preview/examples/PrivateEndpointConnectionUpdate.json
     */
    /**
     * Sample code: Approve or reject a private endpoint connection with a given name.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void approveOrRejectAPrivateEndpointConnectionWithAGivenName(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .privateEndpointConnections()
            .define("private-endpoint-connection-name")
            .withExistingPrivateLinkScope("MyResourceGroup", "MyPrivateLinkScope")
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionStateProperty()
                    .withStatus("Approved")
                    .withDescription("Approved by johndoe@contoso.com"))
            .create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Delete. */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2019-10-17-preview/examples/PrivateEndpointConnectionDelete.json
     */
    /**
     * Sample code: Deletes a private endpoint connection with a given name.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void deletesAPrivateEndpointConnectionWithAGivenName(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .privateEndpointConnections()
            .delete("MyResourceGroup", "MyPrivateLinkScope", "private-endpoint-connection-name", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2019-10-17-preview/examples/PrivateEndpointConnectionGet.json
     */
    /**
     * Sample code: Gets private endpoint connection.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getsPrivateEndpointConnection(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse("MyResourceGroup", "MyPrivateLinkScope", "private-endpoint-connection-name", Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByPrivateLinkScope

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections ListByPrivateLinkScope. */
public final class PrivateEndpointConnectionsListByPrivateLinkScopeSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2019-10-17-preview/examples/PrivateEndpointConnectionList.json
     */
    /**
     * Sample code: Gets list of private endpoint connections on a private link scope.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getsListOfPrivateEndpointConnectionsOnAPrivateLinkScope(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .privateEndpointConnections()
            .listByPrivateLinkScope("MyResourceGroup", "MyPrivateLinkScope", Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources Get. */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2019-10-17-preview/examples/PrivateLinkScopePrivateLinkResourceGet.json
     */
    /**
     * Sample code: Gets private endpoint connection.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getsPrivateEndpointConnection(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .privateLinkResources()
            .getWithResponse("MyResourceGroup", "MyPrivateLinkScope", "azuremonitor", Context.NONE);
    }
}
```

### PrivateLinkResources_ListByPrivateLinkScope

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources ListByPrivateLinkScope. */
public final class PrivateLinkResourcesListByPrivateLinkScopeSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2019-10-17-preview/examples/PrivateLinkScopePrivateLinkResourceListGet.json
     */
    /**
     * Sample code: Gets private endpoint connection.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getsPrivateEndpointConnection(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.privateLinkResources().listByPrivateLinkScope("MyResourceGroup", "MyPrivateLinkScope", Context.NONE);
    }
}
```

### PrivateLinkScopeOperationStatus_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkScopeOperationStatus GetByResourceGroup. */
public final class PrivateLinkScopeOperationStatusGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2019-10-17-preview/examples/privateLinkScopeOperationStatuses.json
     */
    /**
     * Sample code: Get specific operation status.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getSpecificOperationStatus(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .privateLinkScopeOperationStatus()
            .getByResourceGroupWithResponse("MyResourceGroup", "713192d7-503f-477a-9cfe-4efc3ee2bd11", Context.NONE);
    }
}
```

### PrivateLinkScopedResources_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.monitor.generated.models.ScopedResource;

/** Samples for PrivateLinkScopedResources CreateOrUpdate. */
public final class PrivateLinkScopedResourcesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2019-10-17-preview/examples/PrivateLinkScopedResourceUpdate.json
     */
    /**
     * Sample code: Update a scoped resource in a private link scope.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void updateAScopedResourceInAPrivateLinkScope(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        ScopedResource resource =
            manager
                .privateLinkScopedResources()
                .getWithResponse("MyResourceGroup", "MyPrivateLinkScope", "scoped-resource-name", Context.NONE)
                .getValue();
        resource
            .update()
            .withLinkedResourceId(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/MyResourceGroup/providers/Microsoft.Insights/components/my-component")
            .apply();
    }
}
```

### PrivateLinkScopedResources_Delete

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkScopedResources Delete. */
public final class PrivateLinkScopedResourcesDeleteSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2019-10-17-preview/examples/PrivateLinkScopedResourceDelete.json
     */
    /**
     * Sample code: Deletes a scoped resource with a given name.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void deletesAScopedResourceWithAGivenName(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .privateLinkScopedResources()
            .delete("MyResourceGroup", "MyPrivateLinkScope", "scoped-resource-name", Context.NONE);
    }
}
```

### PrivateLinkScopedResources_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkScopedResources Get. */
public final class PrivateLinkScopedResourcesGetSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2019-10-17-preview/examples/PrivateLinkScopedResourceGet.json
     */
    /**
     * Sample code: Gets private link scoped resource.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getsPrivateLinkScopedResource(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .privateLinkScopedResources()
            .getWithResponse("MyResourceGroup", "MyPrivateLinkScope", "scoped-resource-name", Context.NONE);
    }
}
```

### PrivateLinkScopedResources_ListByPrivateLinkScope

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkScopedResources ListByPrivateLinkScope. */
public final class PrivateLinkScopedResourcesListByPrivateLinkScopeSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2019-10-17-preview/examples/PrivateLinkScopedResourceList.json
     */
    /**
     * Sample code: Gets list of scoped resources in a private link scope.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getsListOfScopedResourcesInAPrivateLinkScope(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .privateLinkScopedResources()
            .listByPrivateLinkScope("MyResourceGroup", "MyPrivateLinkScope", Context.NONE);
    }
}
```

### PrivateLinkScopes_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for PrivateLinkScopes CreateOrUpdate. */
public final class PrivateLinkScopesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2019-10-17-preview/examples/PrivateLinkScopesCreate.json
     */
    /**
     * Sample code: PrivateLinkScopeCreate.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void privateLinkScopeCreate(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .privateLinkScopes()
            .define("my-privatelinkscope")
            .withRegion("Global")
            .withExistingResourceGroup("my-resource-group")
            .create();
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2019-10-17-preview/examples/PrivateLinkScopesUpdate.json
     */
    /**
     * Sample code: PrivateLinkScopeUpdate.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void privateLinkScopeUpdate(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .privateLinkScopes()
            .define("my-privatelinkscope")
            .withRegion("Global")
            .withExistingResourceGroup("my-resource-group")
            .withTags(mapOf("Tag1", "Value1"))
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

### PrivateLinkScopes_Delete

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkScopes Delete. */
public final class PrivateLinkScopesDeleteSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2019-10-17-preview/examples/PrivateLinkScopesDelete.json
     */
    /**
     * Sample code: PrivateLinkScopesDelete.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void privateLinkScopesDelete(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.privateLinkScopes().delete("my-resource-group", "my-privatelinkscope", Context.NONE);
    }
}
```

### PrivateLinkScopes_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkScopes GetByResourceGroup. */
public final class PrivateLinkScopesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2019-10-17-preview/examples/PrivateLinkScopesGet.json
     */
    /**
     * Sample code: PrivateLinkScopeGet.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void privateLinkScopeGet(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .privateLinkScopes()
            .getByResourceGroupWithResponse("my-resource-group", "my-privatelinkscope", Context.NONE);
    }
}
```

### PrivateLinkScopes_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkScopes List. */
public final class PrivateLinkScopesListSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2019-10-17-preview/examples/PrivateLinkScopesList.json
     */
    /**
     * Sample code: PrivateLinkScopesList.json.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void privateLinkScopesListJson(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.privateLinkScopes().list(Context.NONE);
    }
}
```

### PrivateLinkScopes_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkScopes ListByResourceGroup. */
public final class PrivateLinkScopesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2019-10-17-preview/examples/PrivateLinkScopesListByResourceGroup.json
     */
    /**
     * Sample code: PrivateLinkScopeListByResourceGroup.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void privateLinkScopeListByResourceGroup(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.privateLinkScopes().listByResourceGroup("my-resource-group", Context.NONE);
    }
}
```

### PrivateLinkScopes_UpdateTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.monitor.generated.models.AzureMonitorPrivateLinkScope;
import java.util.HashMap;
import java.util.Map;

/** Samples for PrivateLinkScopes UpdateTags. */
public final class PrivateLinkScopesUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2019-10-17-preview/examples/PrivateLinkScopesUpdateTagsOnly.json
     */
    /**
     * Sample code: PrivateLinkScopeUpdateTagsOnly.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void privateLinkScopeUpdateTagsOnly(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        AzureMonitorPrivateLinkScope resource =
            manager
                .privateLinkScopes()
                .getByResourceGroupWithResponse("my-resource-group", "my-privatelinkscope", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("Tag1", "Value1", "Tag2", "Value2")).apply();
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

### ScheduledQueryRules_CreateOrUpdate

```java
import com.azure.resourcemanager.monitor.generated.models.AlertSeverity;
import com.azure.resourcemanager.monitor.generated.models.AlertingAction;
import com.azure.resourcemanager.monitor.generated.models.AzNsActionGroup;
import com.azure.resourcemanager.monitor.generated.models.ConditionalOperator;
import com.azure.resourcemanager.monitor.generated.models.Criteria;
import com.azure.resourcemanager.monitor.generated.models.Enabled;
import com.azure.resourcemanager.monitor.generated.models.LogMetricTrigger;
import com.azure.resourcemanager.monitor.generated.models.LogToMetricAction;
import com.azure.resourcemanager.monitor.generated.models.MetricTriggerType;
import com.azure.resourcemanager.monitor.generated.models.QueryType;
import com.azure.resourcemanager.monitor.generated.models.Schedule;
import com.azure.resourcemanager.monitor.generated.models.Source;
import com.azure.resourcemanager.monitor.generated.models.TriggerCondition;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for ScheduledQueryRules CreateOrUpdate. */
public final class ScheduledQueryRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-04-16/examples/createOrUpdateScheduledQueryRule-LogToMetricAction.json
     */
    /**
     * Sample code: Create or Update rule - LogToMetricAction.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createOrUpdateRuleLogToMetricAction(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .scheduledQueryRules()
            .define("logtometricfoo")
            .withRegion("West Europe")
            .withExistingResourceGroup("alertsweu")
            .withSource(
                new Source()
                    .withDataSourceId(
                        "/subscriptions/af52d502-a447-4bc6-8cb7-4780fbb00490/resourceGroups/alertsweu/providers/Microsoft.OperationalInsights/workspaces/alertsweu"))
            .withAction(
                new LogToMetricAction()
                    .withCriteria(
                        Arrays
                            .asList(
                                new Criteria().withMetricName("Average_% Idle Time").withDimensions(Arrays.asList()))))
            .withTags(mapOf())
            .withDescription("log to metric description")
            .withEnabled(Enabled.TRUE)
            .create();
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-04-16/examples/createOrUpdateScheduledQueryRuleswithCrossResource.json
     */
    /**
     * Sample code: Create or Update rule - AlertingAction with Cross-Resource.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createOrUpdateRuleAlertingActionWithCrossResource(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .scheduledQueryRules()
            .define("SampleCrossResourceAlert")
            .withRegion("eastus")
            .withExistingResourceGroup("Rac46PostSwapRG")
            .withSource(
                new Source()
                    .withQuery("union requests, workspace(\"sampleWorkspace\").Update")
                    .withAuthorizedResources(
                        Arrays
                            .asList(
                                "/subscriptions/b67f7fec-69fc-4974-9099-a26bd6ffeda3/resourceGroups/Rac46PostSwapRG/providers/Microsoft.OperationalInsights/workspaces/sampleWorkspace",
                                "/subscriptions/b67f7fec-69fc-4974-9099-a26bd6ffeda3/resourceGroups/Rac46PostSwapRG/providers/microsoft.insights/components/sampleAI"))
                    .withDataSourceId(
                        "/subscriptions/b67f7fec-69fc-4974-9099-a26bd6ffeda3/resourceGroups/Rac46PostSwapRG/providers/microsoft.insights/components/sampleAI")
                    .withQueryType(QueryType.RESULT_COUNT))
            .withAction(
                new AlertingAction()
                    .withSeverity(AlertSeverity.THREE)
                    .withAznsAction(
                        new AzNsActionGroup()
                            .withActionGroup(
                                Arrays
                                    .asList(
                                        "/subscriptions/b67f7fec-69fc-4974-9099-a26bd6ffeda3/resourceGroups/Rac46PostSwapRG/providers/microsoft.insights/actiongroups/test-ag"))
                            .withEmailSubject("Cross Resource Mail!!"))
                    .withTrigger(
                        new TriggerCondition()
                            .withThresholdOperator(ConditionalOperator.GREATER_THAN)
                            .withThreshold(5000.0)))
            .withTags(mapOf())
            .withDescription("Sample Cross Resource alert")
            .withEnabled(Enabled.TRUE)
            .withSchedule(new Schedule().withFrequencyInMinutes(60).withTimeWindowInMinutes(60))
            .create();
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-04-16/examples/createOrUpdateScheduledQueryRules.json
     */
    /**
     * Sample code: Create or Update rule - AlertingAction.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void createOrUpdateRuleAlertingAction(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .scheduledQueryRules()
            .define("logalertfoo")
            .withRegion("eastus")
            .withExistingResourceGroup("Rac46PostSwapRG")
            .withSource(
                new Source()
                    .withQuery("Heartbeat | summarize AggregatedValue = count() by bin(TimeGenerated, 5m)")
                    .withDataSourceId(
                        "/subscriptions/b67f7fec-69fc-4974-9099-a26bd6ffeda3/resourceGroups/Rac46PostSwapRG/providers/Microsoft.OperationalInsights/workspaces/sampleWorkspace")
                    .withQueryType(QueryType.RESULT_COUNT))
            .withAction(
                new AlertingAction()
                    .withSeverity(AlertSeverity.ONE)
                    .withAznsAction(
                        new AzNsActionGroup()
                            .withActionGroup(Arrays.asList())
                            .withEmailSubject("Email Header")
                            .withCustomWebhookPayload("{}"))
                    .withTrigger(
                        new TriggerCondition()
                            .withThresholdOperator(ConditionalOperator.GREATER_THAN)
                            .withThreshold(3.0)
                            .withMetricTrigger(
                                new LogMetricTrigger()
                                    .withThresholdOperator(ConditionalOperator.GREATER_THAN)
                                    .withThreshold(5.0)
                                    .withMetricTriggerType(MetricTriggerType.CONSECUTIVE)
                                    .withMetricColumn("Computer"))))
            .withTags(mapOf())
            .withDescription("log alert description")
            .withEnabled(Enabled.TRUE)
            .withSchedule(new Schedule().withFrequencyInMinutes(15).withTimeWindowInMinutes(15))
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

### ScheduledQueryRules_Delete

```java
import com.azure.core.util.Context;

/** Samples for ScheduledQueryRules Delete. */
public final class ScheduledQueryRulesDeleteSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-04-16/examples/deleteScheduledQueryRules.json
     */
    /**
     * Sample code: Delete rule.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void deleteRule(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.scheduledQueryRules().deleteWithResponse("Rac46PostSwapRG", "logalertfoo", Context.NONE);
    }
}
```

### ScheduledQueryRules_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ScheduledQueryRules GetByResourceGroup. */
public final class ScheduledQueryRulesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-04-16/examples/getScheduledQueryRules.json
     */
    /**
     * Sample code: Get rule.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getRule(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.scheduledQueryRules().getByResourceGroupWithResponse("Rac46PostSwapRG", "logalertfoo", Context.NONE);
    }
}
```

### ScheduledQueryRules_List

```java
import com.azure.core.util.Context;

/** Samples for ScheduledQueryRules List. */
public final class ScheduledQueryRulesListSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-04-16/examples/listScheduledQueryRules.json
     */
    /**
     * Sample code: List rules.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void listRules(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.scheduledQueryRules().list(null, Context.NONE);
    }
}
```

### ScheduledQueryRules_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ScheduledQueryRules ListByResourceGroup. */
public final class ScheduledQueryRulesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-04-16/examples/listScheduledQueryRules.json
     */
    /**
     * Sample code: List rules.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void listRules(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.scheduledQueryRules().listByResourceGroup("gigtest", null, Context.NONE);
    }
}
```

### ScheduledQueryRules_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.monitor.generated.models.Enabled;
import com.azure.resourcemanager.monitor.generated.models.LogSearchRuleResource;

/** Samples for ScheduledQueryRules Update. */
public final class ScheduledQueryRulesUpdateSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2018-04-16/examples/patchScheduledQueryRules.json
     */
    /**
     * Sample code: Patch Log Search Rule.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void patchLogSearchRule(com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        LogSearchRuleResource resource =
            manager
                .scheduledQueryRules()
                .getByResourceGroupWithResponse("my-resource-group", "logalertfoo", Context.NONE)
                .getValue();
        resource.update().withEnabled(Enabled.TRUE).apply();
    }
}
```

### TenantActivityLogs_List

```java
import com.azure.core.util.Context;

/** Samples for TenantActivityLogs List. */
public final class TenantActivityLogsListSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2015-04-01/examples/GetTenantActivityLogsFilteredAndSelected.json
     */
    /**
     * Sample code: Get Tenant Activity Logs with filter and select.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getTenantActivityLogsWithFilterAndSelect(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .tenantActivityLogs()
            .list(
                "eventTimestamp ge '2015-01-21T20:00:00Z' and eventTimestamp le '2015-01-23T20:00:00Z' and"
                    + " resourceGroupName eq 'MSSupportGroup'",
                "eventName,id,resourceGroupName,resourceProviderName,operationName,status,eventTimestamp,correlationId,submissionTimestamp,level",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2015-04-01/examples/GetTenantActivityLogsSelected.json
     */
    /**
     * Sample code: Get Tenant Activity Logs with select.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getTenantActivityLogsWithSelect(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .tenantActivityLogs()
            .list(
                null,
                "eventName,id,resourceGroupName,resourceProviderName,operationName,status,eventTimestamp,correlationId,submissionTimestamp,level",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2015-04-01/examples/GetTenantActivityLogsFiltered.json
     */
    /**
     * Sample code: Get Tenant Activity Logs with filter.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getTenantActivityLogsWithFilter(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .tenantActivityLogs()
            .list(
                "eventTimestamp ge '2015-01-21T20:00:00Z' and eventTimestamp le '2015-01-23T20:00:00Z' and"
                    + " resourceGroupName eq 'MSSupportGroup'",
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/stable/2015-04-01/examples/GetTenantActivityLogsNoParams.json
     */
    /**
     * Sample code: Get Tenant Activity Logs without filter or select.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getTenantActivityLogsWithoutFilterOrSelect(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager.tenantActivityLogs().list(null, null, Context.NONE);
    }
}
```

### VMInsights_GetOnboardingStatus

```java
import com.azure.core.util.Context;

/** Samples for VMInsights GetOnboardingStatus. */
public final class VMInsightsGetOnboardingStatusSamples {
    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2018-11-27-preview/examples/getOnboardingStatusResourceGroup.json
     */
    /**
     * Sample code: Get status for a resource group that has at least one VM that is actively reporting data.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getStatusForAResourceGroupThatHasAtLeastOneVMThatIsActivelyReportingData(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .vMInsights()
            .getOnboardingStatusWithResponse(
                "subscriptions/3d51de47-8d1c-4d24-b42f-bcae075dfa87/resourceGroups/resource-group-with-vms",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2018-11-27-preview/examples/getOnboardingStatusSingleVMUnknown.json
     */
    /**
     * Sample code: Get status for a VM that has not yet reported data.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getStatusForAVMThatHasNotYetReportedData(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .vMInsights()
            .getOnboardingStatusWithResponse(
                "subscriptions/3d51de47-8d1c-4d24-b42f-bcae075dfa87/resourceGroups/vm-resource-group/providers/Microsoft.Compute/virtualMachines/ubuntu-vm",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2018-11-27-preview/examples/getOnboardingStatusSingleVM.json
     */
    /**
     * Sample code: Get status for a VM that is actively reporting data.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getStatusForAVMThatIsActivelyReportingData(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .vMInsights()
            .getOnboardingStatusWithResponse(
                "subscriptions/3d51de47-8d1c-4d24-b42f-bcae075dfa87/resourceGroups/vm-resource-group/providers/Microsoft.Compute/virtualMachines/ubuntu-vm",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2018-11-27-preview/examples/getOnboardingStatusVMScaleSet.json
     */
    /**
     * Sample code: Get status for a VM scale set that is actively reporting data.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getStatusForAVMScaleSetThatIsActivelyReportingData(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .vMInsights()
            .getOnboardingStatusWithResponse(
                "subscriptions/3d51de47-8d1c-4d24-b42f-bcae075dfa87/resourceGroups/my-service-cluster/providers/Microsoft.Compute/virtualMachineScaleSets/scale-set-01",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/monitor/resource-manager/Microsoft.Insights/preview/2018-11-27-preview/examples/getOnboardingStatusSubscription.json
     */
    /**
     * Sample code: Get status for a subscription that has at least one VM that is actively reporting data.
     *
     * @param manager Entry point to MonitorManager.
     */
    public static void getStatusForASubscriptionThatHasAtLeastOneVMThatIsActivelyReportingData(
        com.azure.resourcemanager.monitor.generated.MonitorManager manager) {
        manager
            .vMInsights()
            .getOnboardingStatusWithResponse("subscriptions/3d51de47-8d1c-4d24-b42f-bcae075dfa87", Context.NONE);
    }
}
```

