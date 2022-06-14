# Code snippets and samples


## MarketplaceAgreements

- [CreateOrUpdate](#marketplaceagreements_createorupdate)
- [List](#marketplaceagreements_list)

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
### MarketplaceAgreements_CreateOrUpdate

```java
import com.azure.core.util.Context;

/** Samples for MarketplaceAgreements CreateOrUpdate. */
public final class MarketplaceAgreementsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2021-03-01/examples/MarketplaceAgreements_Create.json
     */
    /**
     * Sample code: MarketplaceAgreements_CreateOrUpdate.
     *
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void marketplaceAgreementsCreateOrUpdate(
        com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.marketplaceAgreements().createOrUpdateWithResponse(null, Context.NONE);
    }
}
```

### MarketplaceAgreements_List

```java
import com.azure.core.util.Context;

/** Samples for MarketplaceAgreements List. */
public final class MarketplaceAgreementsListSamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2021-03-01/examples/MarketplaceAgreements_List.json
     */
    /**
     * Sample code: MarketplaceAgreements_List.
     *
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void marketplaceAgreementsList(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.marketplaceAgreements().list(Context.NONE);
    }
}
```

### Monitors_Create

```java
/** Samples for Monitors Create. */
public final class MonitorsCreateSamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2021-03-01/examples/Monitors_Create.json
     */
    /**
     * Sample code: Monitors_Create.
     *
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void monitorsCreate(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager
            .monitors()
            .define("myMonitor")
            .withRegion((String) null)
            .withExistingResourceGroup("myResourceGroup")
            .create();
    }
}
```

### Monitors_Delete

```java
import com.azure.core.util.Context;

/** Samples for Monitors Delete. */
public final class MonitorsDeleteSamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2021-03-01/examples/Monitors_Delete.json
     */
    /**
     * Sample code: Monitors_Delete.
     *
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void monitorsDelete(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitors().delete("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### Monitors_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Monitors GetByResourceGroup. */
public final class MonitorsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2021-03-01/examples/Monitors_Get.json
     */
    /**
     * Sample code: Monitors_Get.
     *
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void monitorsGet(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitors().getByResourceGroupWithResponse("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### Monitors_GetDefaultKey

```java
import com.azure.core.util.Context;

/** Samples for Monitors GetDefaultKey. */
public final class MonitorsGetDefaultKeySamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2021-03-01/examples/ApiKeys_GetDefaultKey.json
     */
    /**
     * Sample code: Monitors_GetDefaultKey.
     *
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void monitorsGetDefaultKey(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitors().getDefaultKeyWithResponse("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### Monitors_List

```java
import com.azure.core.util.Context;

/** Samples for Monitors List. */
public final class MonitorsListSamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2021-03-01/examples/Monitors_List.json
     */
    /**
     * Sample code: Monitors_List.
     *
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void monitorsList(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitors().list(Context.NONE);
    }
}
```

### Monitors_ListApiKeys

```java
import com.azure.core.util.Context;

/** Samples for Monitors ListApiKeys. */
public final class MonitorsListApiKeysSamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2021-03-01/examples/ApiKeys_List.json
     */
    /**
     * Sample code: Monitors_ListApiKeys.
     *
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void monitorsListApiKeys(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitors().listApiKeys("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### Monitors_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Monitors ListByResourceGroup. */
public final class MonitorsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2021-03-01/examples/Monitors_ListByResourceGroup.json
     */
    /**
     * Sample code: Monitors_ListByResourceGroup.
     *
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void monitorsListByResourceGroup(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitors().listByResourceGroup("myResourceGroup", Context.NONE);
    }
}
```

### Monitors_ListHosts

```java
import com.azure.core.util.Context;

/** Samples for Monitors ListHosts. */
public final class MonitorsListHostsSamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2021-03-01/examples/Hosts_List.json
     */
    /**
     * Sample code: Monitors_ListHosts.
     *
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void monitorsListHosts(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitors().listHosts("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### Monitors_ListLinkedResources

```java
import com.azure.core.util.Context;

/** Samples for Monitors ListLinkedResources. */
public final class MonitorsListLinkedResourcesSamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2021-03-01/examples/LinkedResources_List.json
     */
    /**
     * Sample code: Monitors_ListLinkedResources.
     *
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void monitorsListLinkedResources(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitors().listLinkedResources("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### Monitors_ListMonitoredResources

```java
import com.azure.core.util.Context;

/** Samples for Monitors ListMonitoredResources. */
public final class MonitorsListMonitoredResourcesSamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2021-03-01/examples/MonitoredResources_List.json
     */
    /**
     * Sample code: Monitors_ListMonitoredResources.
     *
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void monitorsListMonitoredResources(
        com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitors().listMonitoredResources("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### Monitors_RefreshSetPasswordLink

```java
import com.azure.core.util.Context;

/** Samples for Monitors RefreshSetPasswordLink. */
public final class MonitorsRefreshSetPasswordLinkSamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2021-03-01/examples/RefreshSetPassword_Get.json
     */
    /**
     * Sample code: Monitors_RefreshSetPasswordLink.
     *
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void monitorsRefreshSetPasswordLink(
        com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitors().refreshSetPasswordLinkWithResponse("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### Monitors_SetDefaultKey

```java
import com.azure.core.util.Context;

/** Samples for Monitors SetDefaultKey. */
public final class MonitorsSetDefaultKeySamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2021-03-01/examples/ApiKeys_SetDefaultKey.json
     */
    /**
     * Sample code: Monitors_SetDefaultKey.
     *
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void monitorsSetDefaultKey(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.monitors().setDefaultKeyWithResponse("myResourceGroup", "myMonitor", null, Context.NONE);
    }
}
```

### Monitors_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datadog.models.DatadogMonitorResource;

/** Samples for Monitors Update. */
public final class MonitorsUpdateSamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2021-03-01/examples/Monitors_Update.json
     */
    /**
     * Sample code: Monitors_Update.
     *
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void monitorsUpdate(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        DatadogMonitorResource resource =
            manager.monitors().getByResourceGroupWithResponse("myResourceGroup", "myMonitor", Context.NONE).getValue();
        resource.update().apply();
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2021-03-01/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void operationsList(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### SingleSignOnConfigurations_CreateOrUpdate

```java
/** Samples for SingleSignOnConfigurations CreateOrUpdate. */
public final class SingleSignOnConfigurationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2021-03-01/examples/SingleSignOnConfigurations_CreateOrUpdate.json
     */
    /**
     * Sample code: SingleSignOnConfigurations_CreateOrUpdate.
     *
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void singleSignOnConfigurationsCreateOrUpdate(
        com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager
            .singleSignOnConfigurations()
            .define("default")
            .withExistingMonitor("myResourceGroup", "myMonitor")
            .create();
    }
}
```

### SingleSignOnConfigurations_Get

```java
import com.azure.core.util.Context;

/** Samples for SingleSignOnConfigurations Get. */
public final class SingleSignOnConfigurationsGetSamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2021-03-01/examples/SingleSignOnConfigurations_Get.json
     */
    /**
     * Sample code: SingleSignOnConfigurations_Get.
     *
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void singleSignOnConfigurationsGet(
        com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.singleSignOnConfigurations().getWithResponse("myResourceGroup", "myMonitor", "default", Context.NONE);
    }
}
```

### SingleSignOnConfigurations_List

```java
import com.azure.core.util.Context;

/** Samples for SingleSignOnConfigurations List. */
public final class SingleSignOnConfigurationsListSamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2021-03-01/examples/SingleSignOnConfigurations_List.json
     */
    /**
     * Sample code: SingleSignOnConfigurations_List.
     *
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void singleSignOnConfigurationsList(
        com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.singleSignOnConfigurations().list("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### TagRules_CreateOrUpdate

```java
/** Samples for TagRules CreateOrUpdate. */
public final class TagRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2021-03-01/examples/TagRules_CreateOrUpdate.json
     */
    /**
     * Sample code: TagRules_CreateOrUpdate.
     *
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void tagRulesCreateOrUpdate(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.tagRules().define("default").withExistingMonitor("myResourceGroup", "myMonitor").create();
    }
}
```

### TagRules_Get

```java
import com.azure.core.util.Context;

/** Samples for TagRules Get. */
public final class TagRulesGetSamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2021-03-01/examples/TagRules_Get.json
     */
    /**
     * Sample code: TagRules_Get.
     *
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void tagRulesGet(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.tagRules().getWithResponse("myResourceGroup", "myMonitor", "default", Context.NONE);
    }
}
```

### TagRules_List

```java
import com.azure.core.util.Context;

/** Samples for TagRules List. */
public final class TagRulesListSamples {
    /*
     * x-ms-original-file: specification/datadog/resource-manager/Microsoft.Datadog/stable/2021-03-01/examples/TagRules_List.json
     */
    /**
     * Sample code: TagRules_List.
     *
     * @param manager Entry point to MicrosoftDatadogManager.
     */
    public static void tagRulesList(com.azure.resourcemanager.datadog.MicrosoftDatadogManager manager) {
        manager.tagRules().list("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

