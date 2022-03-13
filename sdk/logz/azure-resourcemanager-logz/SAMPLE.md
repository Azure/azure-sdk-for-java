# Code snippets and samples


## MonitorOperation

- [ListVMHosts](#monitoroperation_listvmhosts)
- [ListVmHostUpdate](#monitoroperation_listvmhostupdate)
- [VMHostPayload](#monitoroperation_vmhostpayload)

## Monitors

- [Create](#monitors_create)
- [Delete](#monitors_delete)
- [GetByResourceGroup](#monitors_getbyresourcegroup)
- [List](#monitors_list)
- [ListByResourceGroup](#monitors_listbyresourcegroup)
- [ListMonitoredResources](#monitors_listmonitoredresources)
- [ListUserRoles](#monitors_listuserroles)
- [Update](#monitors_update)

## Operations

- [List](#operations_list)

## SingleSignOn

- [CreateOrUpdate](#singlesignon_createorupdate)
- [Get](#singlesignon_get)
- [List](#singlesignon_list)

## SubAccount

- [Create](#subaccount_create)
- [Delete](#subaccount_delete)
- [Get](#subaccount_get)
- [List](#subaccount_list)
- [ListMonitoredResources](#subaccount_listmonitoredresources)
- [ListVMHosts](#subaccount_listvmhosts)
- [ListVmHostUpdate](#subaccount_listvmhostupdate)
- [Update](#subaccount_update)
- [VMHostPayload](#subaccount_vmhostpayload)

## SubAccountTagRules

- [CreateOrUpdate](#subaccounttagrules_createorupdate)
- [Delete](#subaccounttagrules_delete)
- [Get](#subaccounttagrules_get)
- [List](#subaccounttagrules_list)

## TagRules

- [CreateOrUpdate](#tagrules_createorupdate)
- [Delete](#tagrules_delete)
- [Get](#tagrules_get)
- [List](#tagrules_list)
### MonitorOperation_ListVMHosts

```java
import com.azure.core.util.Context;

/** Samples for MonitorOperation ListVMHosts. */
public final class MonitorOperationListVMHostsSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/MainAccount_VMHosts_List.json
     */
    /**
     * Sample code: MainAccount_VMHosts_List.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void mainAccountVMHostsList(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.monitorOperations().listVMHosts("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### MonitorOperation_ListVmHostUpdate

```java
import com.azure.core.util.Context;

/** Samples for MonitorOperation ListVmHostUpdate. */
public final class MonitorOperationListVmHostUpdateSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/MainAccount_VMHosts_Update.json
     */
    /**
     * Sample code: MainAccount_VMHosts_Update.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void mainAccountVMHostsUpdate(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.monitorOperations().listVmHostUpdate("myResourceGroup", "myMonitor", null, Context.NONE);
    }
}
```

### MonitorOperation_VMHostPayload

```java
import com.azure.core.util.Context;

/** Samples for MonitorOperation VMHostPayload. */
public final class MonitorOperationVMHostPayloadSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/MainAccount_VMHosts_Payload.json
     */
    /**
     * Sample code: MainAccount_VMHosts_Payload.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void mainAccountVMHostsPayload(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.monitorOperations().vMHostPayloadWithResponse("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### Monitors_Create

```java
/** Samples for Monitors Create. */
public final class MonitorsCreateSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/Monitors_Create.json
     */
    /**
     * Sample code: Monitors_Create.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void monitorsCreate(com.azure.resourcemanager.logz.LogzManager manager) {
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
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/Monitors_Delete.json
     */
    /**
     * Sample code: Monitors_Delete.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void monitorsDelete(com.azure.resourcemanager.logz.LogzManager manager) {
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
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/Monitors_Get.json
     */
    /**
     * Sample code: Monitors_Get.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void monitorsGet(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.monitors().getByResourceGroupWithResponse("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### Monitors_List

```java
import com.azure.core.util.Context;

/** Samples for Monitors List. */
public final class MonitorsListSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/Monitors_List.json
     */
    /**
     * Sample code: Monitors_List.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void monitorsList(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.monitors().list(Context.NONE);
    }
}
```

### Monitors_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Monitors ListByResourceGroup. */
public final class MonitorsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/Monitors_ListByResourceGroup.json
     */
    /**
     * Sample code: Monitors_ListByResourceGroup.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void monitorsListByResourceGroup(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.monitors().listByResourceGroup("myResourceGroup", Context.NONE);
    }
}
```

### Monitors_ListMonitoredResources

```java
import com.azure.core.util.Context;

/** Samples for Monitors ListMonitoredResources. */
public final class MonitorsListMonitoredResourcesSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/MonitoredResources_List.json
     */
    /**
     * Sample code: MonitoredResources_List.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void monitoredResourcesList(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.monitors().listMonitoredResources("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### Monitors_ListUserRoles

```java
import com.azure.core.util.Context;

/** Samples for Monitors ListUserRoles. */
public final class MonitorsListUserRolesSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/MainAccount_listUserRoles.json
     */
    /**
     * Sample code: MainAccount_VMHosts_Update.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void mainAccountVMHostsUpdate(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.monitors().listUserRoles("myResourceGroup", "myMonitor", null, Context.NONE);
    }
}
```

### Monitors_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.logz.models.LogzMonitorResource;

/** Samples for Monitors Update. */
public final class MonitorsUpdateSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/Monitors_Update.json
     */
    /**
     * Sample code: Monitors_Update.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void monitorsUpdate(com.azure.resourcemanager.logz.LogzManager manager) {
        LogzMonitorResource resource =
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
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void operationsList(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### SingleSignOn_CreateOrUpdate

```java
/** Samples for SingleSignOn CreateOrUpdate. */
public final class SingleSignOnCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/SingleSignOnConfigurations_CreateOrUpdate.json
     */
    /**
     * Sample code: SingleSignOnConfigurations_CreateOrUpdate.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void singleSignOnConfigurationsCreateOrUpdate(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.singleSignOns().define("default").withExistingMonitor("myResourceGroup", "myMonitor").create();
    }
}
```

### SingleSignOn_Get

```java
import com.azure.core.util.Context;

/** Samples for SingleSignOn Get. */
public final class SingleSignOnGetSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/SingleSignOnConfigurations_Get.json
     */
    /**
     * Sample code: SingleSignOnConfigurations_Get.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void singleSignOnConfigurationsGet(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.singleSignOns().getWithResponse("myResourceGroup", "myMonitor", "default", Context.NONE);
    }
}
```

### SingleSignOn_List

```java
import com.azure.core.util.Context;

/** Samples for SingleSignOn List. */
public final class SingleSignOnListSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/SingleSignOnConfigurations_List.json
     */
    /**
     * Sample code: SingleSignOnConfigurations_List.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void singleSignOnConfigurationsList(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.singleSignOns().list("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### SubAccount_Create

```java
import com.azure.core.util.Context;

/** Samples for SubAccount Create. */
public final class SubAccountCreateSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/SubAccount_Create.json
     */
    /**
     * Sample code: subAccount_Create.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void subAccountCreate(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.subAccounts().create("myResourceGroup", "myMonitor", "SubAccount1", null, Context.NONE);
    }
}
```

### SubAccount_Delete

```java
import com.azure.core.util.Context;

/** Samples for SubAccount Delete. */
public final class SubAccountDeleteSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/SubAccount_Delete.json
     */
    /**
     * Sample code: SubAccount_Delete.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void subAccountDelete(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.subAccounts().delete("myResourceGroup", "myMonitor", "someName", Context.NONE);
    }
}
```

### SubAccount_Get

```java
import com.azure.core.util.Context;

/** Samples for SubAccount Get. */
public final class SubAccountGetSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/SubAccount_Get.json
     */
    /**
     * Sample code: SubAccount_Get.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void subAccountGet(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.subAccounts().getWithResponse("myResourceGroup", "myMonitor", "SubAccount1", Context.NONE);
    }
}
```

### SubAccount_List

```java
import com.azure.core.util.Context;

/** Samples for SubAccount List. */
public final class SubAccountListSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/SubAccount_ListByResourceGroup.json
     */
    /**
     * Sample code: SubAccount_List.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void subAccountList(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.subAccounts().list("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### SubAccount_ListMonitoredResources

```java
import com.azure.core.util.Context;

/** Samples for SubAccount ListMonitoredResources. */
public final class SubAccountListMonitoredResourcesSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/SubAccount_MonitoredResources_List.json
     */
    /**
     * Sample code: SubAccount_MonitoredResources_List.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void subAccountMonitoredResourcesList(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.subAccounts().listMonitoredResources("myResourceGroup", "myMonitor", "SubAccount1", Context.NONE);
    }
}
```

### SubAccount_ListVMHosts

```java
import com.azure.core.util.Context;

/** Samples for SubAccount ListVMHosts. */
public final class SubAccountListVMHostsSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/SubAccount_VMHosts_List.json
     */
    /**
     * Sample code: SubAccount_VMHosts_List.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void subAccountVMHostsList(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.subAccounts().listVMHosts("myResourceGroup", "myMonitor", "SubAccount1", Context.NONE);
    }
}
```

### SubAccount_ListVmHostUpdate

```java
import com.azure.core.util.Context;

/** Samples for SubAccount ListVmHostUpdate. */
public final class SubAccountListVmHostUpdateSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/SubAccount_VMHosts_Update.json
     */
    /**
     * Sample code: SubAccount_VMHosts_Update.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void subAccountVMHostsUpdate(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.subAccounts().listVmHostUpdate("myResourceGroup", "myMonitor", "SubAccount1", null, Context.NONE);
    }
}
```

### SubAccount_Update

```java
import com.azure.core.util.Context;

/** Samples for SubAccount Update. */
public final class SubAccountUpdateSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/SubAccount_Update.json
     */
    /**
     * Sample code: SubAccount_Update.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void subAccountUpdate(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.subAccounts().updateWithResponse("myResourceGroup", "myMonitor", "SubAccount1", null, Context.NONE);
    }
}
```

### SubAccount_VMHostPayload

```java
import com.azure.core.util.Context;

/** Samples for SubAccount VMHostPayload. */
public final class SubAccountVMHostPayloadSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/SubAccount_VMHosts_Payload.json
     */
    /**
     * Sample code: SubAccount_VMHosts_Payload.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void subAccountVMHostsPayload(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.subAccounts().vMHostPayloadWithResponse("myResourceGroup", "myMonitor", "SubAccount1", Context.NONE);
    }
}
```

### SubAccountTagRules_CreateOrUpdate

```java
import com.azure.core.util.Context;

/** Samples for SubAccountTagRules CreateOrUpdate. */
public final class SubAccountTagRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/SubAccountTagRules_CreateOrUpdate.json
     */
    /**
     * Sample code: SubAccountTagRules_CreateOrUpdate.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void subAccountTagRulesCreateOrUpdate(com.azure.resourcemanager.logz.LogzManager manager) {
        manager
            .subAccountTagRules()
            .createOrUpdateWithResponse("myResourceGroup", "myMonitor", "SubAccount1", "default", null, Context.NONE);
    }
}
```

### SubAccountTagRules_Delete

```java
import com.azure.core.util.Context;

/** Samples for SubAccountTagRules Delete. */
public final class SubAccountTagRulesDeleteSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/SubAccountTagRules_Delete.json
     */
    /**
     * Sample code: TagRules_Delete.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void tagRulesDelete(com.azure.resourcemanager.logz.LogzManager manager) {
        manager
            .subAccountTagRules()
            .deleteWithResponse("myResourceGroup", "myMonitor", "SubAccount1", "default", Context.NONE);
    }
}
```

### SubAccountTagRules_Get

```java
import com.azure.core.util.Context;

/** Samples for SubAccountTagRules Get. */
public final class SubAccountTagRulesGetSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/SubAccountTagRules_Get.json
     */
    /**
     * Sample code: SubAccountTagRules_Get.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void subAccountTagRulesGet(com.azure.resourcemanager.logz.LogzManager manager) {
        manager
            .subAccountTagRules()
            .getWithResponse("myResourceGroup", "myMonitor", "SubAccount1", "default", Context.NONE);
    }
}
```

### SubAccountTagRules_List

```java
import com.azure.core.util.Context;

/** Samples for SubAccountTagRules List. */
public final class SubAccountTagRulesListSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/SubAccountTagRules_List.json
     */
    /**
     * Sample code: SubAccountTagRules_List.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void subAccountTagRulesList(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.subAccountTagRules().list("myResourceGroup", "myMonitor", "SubAccount1", Context.NONE);
    }
}
```

### TagRules_CreateOrUpdate

```java
/** Samples for TagRules CreateOrUpdate. */
public final class TagRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/TagRules_CreateOrUpdate.json
     */
    /**
     * Sample code: TagRules_CreateOrUpdate.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void tagRulesCreateOrUpdate(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.tagRules().define("default").withExistingMonitor("myResourceGroup", "myMonitor").create();
    }
}
```

### TagRules_Delete

```java
import com.azure.core.util.Context;

/** Samples for TagRules Delete. */
public final class TagRulesDeleteSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/TagRules_Delete.json
     */
    /**
     * Sample code: TagRules_Delete.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void tagRulesDelete(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.tagRules().deleteWithResponse("myResourceGroup", "myMonitor", "default", Context.NONE);
    }
}
```

### TagRules_Get

```java
import com.azure.core.util.Context;

/** Samples for TagRules Get. */
public final class TagRulesGetSamples {
    /*
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/TagRules_Get.json
     */
    /**
     * Sample code: TagRules_Get.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void tagRulesGet(com.azure.resourcemanager.logz.LogzManager manager) {
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
     * x-ms-original-file: specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/TagRules_List.json
     */
    /**
     * Sample code: TagRules_List.
     *
     * @param manager Entry point to LogzManager.
     */
    public static void tagRulesList(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.tagRules().list("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

