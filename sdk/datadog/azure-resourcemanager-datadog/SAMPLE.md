# Code snippets and samples


## CreationSupported

- [Get](#creationsupported_get)
- [List](#creationsupported_list)

## MarketplaceAgreements

- [CreateOrUpdate](#marketplaceagreements_createorupdate)
- [List](#marketplaceagreements_list)

## MonitoredSubscriptions

- [CreateorUpdate](#monitoredsubscriptions_createorupdate)
- [Delete](#monitoredsubscriptions_delete)
- [Get](#monitoredsubscriptions_get)
- [List](#monitoredsubscriptions_list)
- [Update](#monitoredsubscriptions_update)

## Monitors

- [Create](#monitors_create)
- [Delete](#monitors_delete)
- [GetByResourceGroup](#monitors_getbyresourcegroup)
- [GetDefaultKey](#monitors_getdefaultkey)
- [List](#monitors_list)
- [ListApiKeys](#monitors_listapikeys)
- [ListByResourceGroup](#monitors_listbyresourcegroup)
- [ListHosts](#monitors_listhosts)
- [ListLinkedResources](#monitors_listlinkedresources)
- [ListMonitoredResources](#monitors_listmonitoredresources)
- [RefreshSetPasswordLink](#monitors_refreshsetpasswordlink)
- [SetDefaultKey](#monitors_setdefaultkey)
- [Update](#monitors_update)

## Operations

- [List](#operations_list)

## SingleSignOnConfigurations

- [CreateOrUpdate](#singlesignonconfigurations_createorupdate)
- [Get](#singlesignonconfigurations_get)
- [List](#singlesignonconfigurations_list)

## TagRules

- [CreateOrUpdate](#tagrules_createorupdate)
- [Get](#tagrules_get)
- [List](#tagrules_list)
### CreationSupported_Get

```java
/**
 * Samples for CreationSupported Get.
 */
public final class CreationSupportedGetSamples {
    /*
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/CreationSupported_Get.json
     */
    /**
     * Sample code: CreationSupported_Get.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void creationSupportedGet(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.creationSupporteds().getWithResponse("00000000-0000-0000-0000", com.azure.core.util.Context.NONE);
    }
}
```

### CreationSupported_List

```java
/**
 * Samples for CreationSupported List.
 */
public final class CreationSupportedListSamples {
    /*
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/CreationSupported_List.json
     */
    /**
     * Sample code: CreationSupported_List.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void creationSupportedList(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.creationSupporteds().list("00000000-0000-0000-0000", com.azure.core.util.Context.NONE);
    }
}
```

### MarketplaceAgreements_CreateOrUpdate

```java
import com.azure.resourcemanager.datadog.fluent.models.DatadogAgreementResourceInner;
import com.azure.resourcemanager.datadog.models.DatadogAgreementProperties;

/**
 * Samples for MarketplaceAgreements CreateOrUpdate.
 */
public final class MarketplaceAgreementsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/MarketplaceAgreements_Create.
     * json
     */
    /**
     * Sample code: MarketplaceAgreements_CreateOrUpdate.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void
        marketplaceAgreementsCreateOrUpdate(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.marketplaceAgreements()
            .createOrUpdateWithResponse(
                new DatadogAgreementResourceInner().withProperties(new DatadogAgreementProperties().withAccepted(true)),
                com.azure.core.util.Context.NONE);
    }
}
```

### MarketplaceAgreements_List

```java
/**
 * Samples for MarketplaceAgreements List.
 */
public final class MarketplaceAgreementsListSamples {
    /*
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/MarketplaceAgreements_List.
     * json
     */
    /**
     * Sample code: MarketplaceAgreements_List.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void marketplaceAgreementsList(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.marketplaceAgreements().list(com.azure.core.util.Context.NONE);
    }
}
```

### MonitoredSubscriptions_CreateorUpdate

```java
import com.azure.resourcemanager.datadog.models.FilteringTag;
import com.azure.resourcemanager.datadog.models.LogRules;
import com.azure.resourcemanager.datadog.models.MetricRules;
import com.azure.resourcemanager.datadog.models.MonitoredSubscription;
import com.azure.resourcemanager.datadog.models.MonitoringTagRulesProperties;
import com.azure.resourcemanager.datadog.models.Operation;
import com.azure.resourcemanager.datadog.models.Status;
import com.azure.resourcemanager.datadog.models.SubscriptionList;
import com.azure.resourcemanager.datadog.models.TagAction;
import java.util.Arrays;

/**
 * Samples for MonitoredSubscriptions CreateorUpdate.
 */
public final class MonitoredSubscriptionsCreateorUpdateSamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/
     * MonitoredSubscriptions_CreateorUpdate.json
     */
    /**
     * Sample code: Monitors_AddMonitoredSubscriptions.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void
        monitorsAddMonitoredSubscriptions(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitoredSubscriptions()
            .define("default")
            .withExistingMonitor("myResourceGroup", "myMonitor")
            .withProperties(new SubscriptionList().withOperation(Operation.ADD_BEGIN)
                .withMonitoredSubscriptionList(Arrays.asList(new MonitoredSubscription()
                    .withSubscriptionId("/subscriptions/00000000-0000-0000-0000-000000000000")
                    .withStatus(Status.ACTIVE)
                    .withTagRules(new MonitoringTagRulesProperties().withLogRules(new LogRules().withSendAadLogs(false)
                        .withSendSubscriptionLogs(true)
                        .withSendResourceLogs(true)
                        .withFilteringTags(Arrays.asList(
                            new FilteringTag().withName("Environment").withValue("Prod").withAction(TagAction.INCLUDE),
                            new FilteringTag().withName("Environment").withValue("Dev").withAction(TagAction.EXCLUDE))))
                        .withMetricRules(new MetricRules().withFilteringTags(Arrays.asList()))
                        .withAutomuting(true)),
                    new MonitoredSubscription()
                        .withSubscriptionId("/subscriptions/00000000-0000-0000-0000-000000000001")
                        .withStatus(Status.FAILED)
                        .withTagRules(new MonitoringTagRulesProperties()
                            .withLogRules(new LogRules().withSendAadLogs(false)
                                .withSendSubscriptionLogs(true)
                                .withSendResourceLogs(true)
                                .withFilteringTags(Arrays.asList(
                                    new FilteringTag().withName("Environment")
                                        .withValue("Prod")
                                        .withAction(TagAction.INCLUDE),
                                    new FilteringTag().withName("Environment")
                                        .withValue("Dev")
                                        .withAction(TagAction.EXCLUDE))))
                            .withMetricRules(new MetricRules().withFilteringTags(Arrays.asList()))
                            .withAutomuting(true)))))
            .create();
    }
}
```

### MonitoredSubscriptions_Delete

```java
/**
 * Samples for MonitoredSubscriptions Delete.
 */
public final class MonitoredSubscriptionsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/MonitoredSubscriptions_Delete
     * .json
     */
    /**
     * Sample code: Monitors_DeleteMonitoredSubscriptions.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void
        monitorsDeleteMonitoredSubscriptions(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitoredSubscriptions()
            .delete("myResourceGroup", "myMonitor", "default", com.azure.core.util.Context.NONE);
    }
}
```

### MonitoredSubscriptions_Get

```java
/**
 * Samples for MonitoredSubscriptions Get.
 */
public final class MonitoredSubscriptionsGetSamples {
    /*
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/MonitoredSubscriptions_Get.
     * json
     */
    /**
     * Sample code: Monitors_GetMonitoredSubscriptions.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void
        monitorsGetMonitoredSubscriptions(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitoredSubscriptions()
            .getWithResponse("myResourceGroup", "myMonitor", "default", com.azure.core.util.Context.NONE);
    }
}
```

### MonitoredSubscriptions_List

```java
/**
 * Samples for MonitoredSubscriptions List.
 */
public final class MonitoredSubscriptionsListSamples {
    /*
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/MonitoredSubscriptions_List.
     * json
     */
    /**
     * Sample code: Monitors_GetMonitoredSubscriptions.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void
        monitorsGetMonitoredSubscriptions(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitoredSubscriptions().list("myResourceGroup", "myMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### MonitoredSubscriptions_Update

```java
import com.azure.resourcemanager.datadog.models.FilteringTag;
import com.azure.resourcemanager.datadog.models.LogRules;
import com.azure.resourcemanager.datadog.models.MetricRules;
import com.azure.resourcemanager.datadog.models.MonitoredSubscription;
import com.azure.resourcemanager.datadog.models.MonitoredSubscriptionProperties;
import com.azure.resourcemanager.datadog.models.MonitoringTagRulesProperties;
import com.azure.resourcemanager.datadog.models.Operation;
import com.azure.resourcemanager.datadog.models.Status;
import com.azure.resourcemanager.datadog.models.SubscriptionList;
import com.azure.resourcemanager.datadog.models.TagAction;
import java.util.Arrays;

/**
 * Samples for MonitoredSubscriptions Update.
 */
public final class MonitoredSubscriptionsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/MonitoredSubscriptions_Update
     * .json
     */
    /**
     * Sample code: Monitors_UpdateMonitoredSubscriptions.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void
        monitorsUpdateMonitoredSubscriptions(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        MonitoredSubscriptionProperties resource = manager.monitoredSubscriptions()
            .getWithResponse("myResourceGroup", "myMonitor", "default", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new SubscriptionList().withOperation(Operation.ADD_COMPLETE)
                .withMonitoredSubscriptionList(Arrays.asList(new MonitoredSubscription()
                    .withSubscriptionId("/subscriptions/00000000-0000-0000-0000-000000000000")
                    .withStatus(Status.ACTIVE)
                    .withTagRules(new MonitoringTagRulesProperties().withLogRules(new LogRules().withSendAadLogs(false)
                        .withSendSubscriptionLogs(true)
                        .withSendResourceLogs(true)
                        .withFilteringTags(Arrays.asList(
                            new FilteringTag().withName("Environment").withValue("Prod").withAction(TagAction.INCLUDE),
                            new FilteringTag().withName("Environment").withValue("Dev").withAction(TagAction.EXCLUDE))))
                        .withMetricRules(new MetricRules().withFilteringTags(Arrays.asList()))
                        .withAutomuting(true)),
                    new MonitoredSubscription()
                        .withSubscriptionId("/subscriptions/00000000-0000-0000-0000-000000000001")
                        .withStatus(Status.FAILED)
                        .withTagRules(new MonitoringTagRulesProperties()
                            .withLogRules(new LogRules().withSendAadLogs(false)
                                .withSendSubscriptionLogs(true)
                                .withSendResourceLogs(true)
                                .withFilteringTags(Arrays.asList(
                                    new FilteringTag().withName("Environment")
                                        .withValue("Prod")
                                        .withAction(TagAction.INCLUDE),
                                    new FilteringTag().withName("Environment")
                                        .withValue("Dev")
                                        .withAction(TagAction.EXCLUDE))))
                            .withMetricRules(new MetricRules().withFilteringTags(Arrays.asList()))
                            .withAutomuting(true)))))
            .apply();
    }
}
```

### Monitors_Create

```java
import com.azure.resourcemanager.datadog.models.DatadogOrganizationProperties;
import com.azure.resourcemanager.datadog.models.MonitorProperties;
import com.azure.resourcemanager.datadog.models.MonitoringStatus;
import com.azure.resourcemanager.datadog.models.ResourceSku;
import com.azure.resourcemanager.datadog.models.UserInfo;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Monitors Create.
 */
public final class MonitorsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/Monitors_Create.json
     */
    /**
     * Sample code: Monitors_Create.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void monitorsCreate(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitors()
            .define("myMonitor")
            .withRegion("West US")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("Environment", "Dev"))
            .withSku(new ResourceSku().withName("free_Monthly"))
            .withProperties(new MonitorProperties().withMonitoringStatus(MonitoringStatus.ENABLED)
                .withDatadogOrganizationProperties(new DatadogOrganizationProperties().withName("myOrg")
                    .withId("myOrg123")
                    .withLinkingAuthCode("fakeTokenPlaceholder")
                    .withLinkingClientId("00000000-0000-0000-0000-000000000000")
                    .withEnterpriseAppId("00000000-0000-0000-0000-000000000000")
                    .withCspm(false))
                .withUserInfo(new UserInfo().withName("Alice")
                    .withEmailAddress("alice@microsoft.com")
                    .withPhoneNumber("123-456-7890")))
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

### Monitors_Delete

```java
/**
 * Samples for Monitors Delete.
 */
public final class MonitorsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/Monitors_Delete.json
     */
    /**
     * Sample code: Monitors_Delete.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void monitorsDelete(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitors().delete("myResourceGroup", "myMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_GetByResourceGroup

```java
/**
 * Samples for Monitors GetByResourceGroup.
 */
public final class MonitorsGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/Monitors_Get.json
     */
    /**
     * Sample code: Monitors_Get.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void monitorsGet(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitors()
            .getByResourceGroupWithResponse("myResourceGroup", "myMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_GetDefaultKey

```java
/**
 * Samples for Monitors GetDefaultKey.
 */
public final class MonitorsGetDefaultKeySamples {
    /*
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/ApiKeys_GetDefaultKey.json
     */
    /**
     * Sample code: Monitors_GetDefaultKey.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void monitorsGetDefaultKey(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitors().getDefaultKeyWithResponse("myResourceGroup", "myMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_List

```java
/**
 * Samples for Monitors List.
 */
public final class MonitorsListSamples {
    /*
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/Monitors_List.json
     */
    /**
     * Sample code: Monitors_List.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void monitorsList(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitors().list(com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_ListApiKeys

```java
/**
 * Samples for Monitors ListApiKeys.
 */
public final class MonitorsListApiKeysSamples {
    /*
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/ApiKeys_List.json
     */
    /**
     * Sample code: Monitors_ListApiKeys.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void monitorsListApiKeys(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitors().listApiKeys("myResourceGroup", "myMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_ListByResourceGroup

```java
/**
 * Samples for Monitors ListByResourceGroup.
 */
public final class MonitorsListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/Monitors_ListByResourceGroup.
     * json
     */
    /**
     * Sample code: Monitors_ListByResourceGroup.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void monitorsListByResourceGroup(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitors().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_ListHosts

```java
/**
 * Samples for Monitors ListHosts.
 */
public final class MonitorsListHostsSamples {
    /*
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/Hosts_List.json
     */
    /**
     * Sample code: Monitors_ListHosts.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void monitorsListHosts(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitors().listHosts("myResourceGroup", "myMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_ListLinkedResources

```java
/**
 * Samples for Monitors ListLinkedResources.
 */
public final class MonitorsListLinkedResourcesSamples {
    /*
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/LinkedResources_List.json
     */
    /**
     * Sample code: Monitors_ListLinkedResources.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void monitorsListLinkedResources(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitors().listLinkedResources("myResourceGroup", "myMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_ListMonitoredResources

```java
/**
 * Samples for Monitors ListMonitoredResources.
 */
public final class MonitorsListMonitoredResourcesSamples {
    /*
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/MonitoredResources_List.json
     */
    /**
     * Sample code: Monitors_ListMonitoredResources.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void
        monitorsListMonitoredResources(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitors().listMonitoredResources("myResourceGroup", "myMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_RefreshSetPasswordLink

```java
/**
 * Samples for Monitors RefreshSetPasswordLink.
 */
public final class MonitorsRefreshSetPasswordLinkSamples {
    /*
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/RefreshSetPassword_Get.json
     */
    /**
     * Sample code: Monitors_RefreshSetPasswordLink.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void
        monitorsRefreshSetPasswordLink(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitors()
            .refreshSetPasswordLinkWithResponse("myResourceGroup", "myMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_SetDefaultKey

```java
import com.azure.resourcemanager.datadog.fluent.models.DatadogApiKeyInner;

/**
 * Samples for Monitors SetDefaultKey.
 */
public final class MonitorsSetDefaultKeySamples {
    /*
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/ApiKeys_SetDefaultKey.json
     */
    /**
     * Sample code: Monitors_SetDefaultKey.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void monitorsSetDefaultKey(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitors()
            .setDefaultKeyWithResponse("myResourceGroup", "myMonitor",
                new DatadogApiKeyInner().withKey("fakeTokenPlaceholder"), com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_Update

```java
import com.azure.resourcemanager.datadog.models.DatadogMonitorResource;
import com.azure.resourcemanager.datadog.models.MonitorUpdateProperties;
import com.azure.resourcemanager.datadog.models.MonitoringStatus;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Monitors Update.
 */
public final class MonitorsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/Monitors_Update.json
     */
    /**
     * Sample code: Monitors_Update.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void monitorsUpdate(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        DatadogMonitorResource resource = manager.monitors()
            .getByResourceGroupWithResponse("myResourceGroup", "myMonitor", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("Environment", "Dev"))
            .withProperties(new MonitorUpdateProperties().withMonitoringStatus(MonitoringStatus.ENABLED))
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
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void operationsList(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### SingleSignOnConfigurations_CreateOrUpdate

```java
import com.azure.resourcemanager.datadog.models.DatadogSingleSignOnProperties;
import com.azure.resourcemanager.datadog.models.SingleSignOnStates;

/**
 * Samples for SingleSignOnConfigurations CreateOrUpdate.
 */
public final class SingleSignOnConfigurationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/
     * SingleSignOnConfigurations_CreateOrUpdate.json
     */
    /**
     * Sample code: SingleSignOnConfigurations_CreateOrUpdate.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void
        singleSignOnConfigurationsCreateOrUpdate(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.singleSignOnConfigurations()
            .define("default")
            .withExistingMonitor("myResourceGroup", "myMonitor")
            .withProperties(new DatadogSingleSignOnProperties().withSingleSignOnState(SingleSignOnStates.ENABLE)
                .withEnterpriseAppId("00000000-0000-0000-0000-000000000000"))
            .create();
    }
}
```

### SingleSignOnConfigurations_Get

```java
/**
 * Samples for SingleSignOnConfigurations Get.
 */
public final class SingleSignOnConfigurationsGetSamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/
     * SingleSignOnConfigurations_Get.json
     */
    /**
     * Sample code: SingleSignOnConfigurations_Get.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void
        singleSignOnConfigurationsGet(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.singleSignOnConfigurations()
            .getWithResponse("myResourceGroup", "myMonitor", "default", com.azure.core.util.Context.NONE);
    }
}
```

### SingleSignOnConfigurations_List

```java
/**
 * Samples for SingleSignOnConfigurations List.
 */
public final class SingleSignOnConfigurationsListSamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/
     * SingleSignOnConfigurations_List.json
     */
    /**
     * Sample code: SingleSignOnConfigurations_List.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void
        singleSignOnConfigurationsList(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.singleSignOnConfigurations().list("myResourceGroup", "myMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### TagRules_CreateOrUpdate

```java
import com.azure.resourcemanager.datadog.models.FilteringTag;
import com.azure.resourcemanager.datadog.models.LogRules;
import com.azure.resourcemanager.datadog.models.MetricRules;
import com.azure.resourcemanager.datadog.models.MonitoringTagRulesProperties;
import com.azure.resourcemanager.datadog.models.TagAction;
import java.util.Arrays;

/**
 * Samples for TagRules CreateOrUpdate.
 */
public final class TagRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/TagRules_CreateOrUpdate.json
     */
    /**
     * Sample code: TagRules_CreateOrUpdate.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void tagRulesCreateOrUpdate(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.tagRules()
            .define("default")
            .withExistingMonitor("myResourceGroup", "myMonitor")
            .withProperties(new MonitoringTagRulesProperties()
                .withLogRules(new LogRules().withSendAadLogs(false)
                    .withSendSubscriptionLogs(true)
                    .withSendResourceLogs(true)
                    .withFilteringTags(Arrays.asList(
                        new FilteringTag().withName("Environment").withValue("Prod").withAction(TagAction.INCLUDE),
                        new FilteringTag().withName("Environment").withValue("Dev").withAction(TagAction.EXCLUDE))))
                .withMetricRules(new MetricRules().withFilteringTags(Arrays.asList()))
                .withAutomuting(true))
            .create();
    }
}
```

### TagRules_Get

```java
/**
 * Samples for TagRules Get.
 */
public final class TagRulesGetSamples {
    /*
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/TagRules_Get.json
     */
    /**
     * Sample code: TagRules_Get.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void tagRulesGet(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.tagRules().getWithResponse("myResourceGroup", "myMonitor", "default", com.azure.core.util.Context.NONE);
    }
}
```

### TagRules_List

```java
/**
 * Samples for TagRules List.
 */
public final class TagRulesListSamples {
    /*
     * x-ms-original-file:
     * specification/datadog/resource-manager/Microsoft.Datadog/stable/2023-01-01/examples/TagRules_List.json
     */
    /**
     * Sample code: TagRules_List.
     * 
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void tagRulesList(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.tagRules().list("myResourceGroup", "myMonitor", com.azure.core.util.Context.NONE);
    }
}
```

