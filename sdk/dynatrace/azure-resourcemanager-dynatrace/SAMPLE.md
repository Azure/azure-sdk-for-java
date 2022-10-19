# Code snippets and samples


## Monitors

- [CreateOrUpdate](#monitors_createorupdate)
- [Delete](#monitors_delete)
- [GetAccountCredentials](#monitors_getaccountcredentials)
- [GetByResourceGroup](#monitors_getbyresourcegroup)
- [GetSsoDetails](#monitors_getssodetails)
- [GetVMHostPayload](#monitors_getvmhostpayload)
- [List](#monitors_list)
- [ListAppServices](#monitors_listappservices)
- [ListByResourceGroup](#monitors_listbyresourcegroup)
- [ListHosts](#monitors_listhosts)
- [ListLinkableEnvironments](#monitors_listlinkableenvironments)
- [ListMonitoredResources](#monitors_listmonitoredresources)
- [Update](#monitors_update)

## Operations

- [List](#operations_list)

## SingleSignOn

- [CreateOrUpdate](#singlesignon_createorupdate)
- [Get](#singlesignon_get)
- [List](#singlesignon_list)

## TagRules

- [CreateOrUpdate](#tagrules_createorupdate)
- [Delete](#tagrules_delete)
- [Get](#tagrules_get)
- [List](#tagrules_list)
- [Update](#tagrules_update)
### Monitors_CreateOrUpdate

```java
import com.azure.resourcemanager.dynatrace.fluent.models.DynatraceSingleSignOnProperties;
import com.azure.resourcemanager.dynatrace.models.AccountInfo;
import com.azure.resourcemanager.dynatrace.models.DynatraceEnvironmentProperties;
import com.azure.resourcemanager.dynatrace.models.EnvironmentInfo;
import com.azure.resourcemanager.dynatrace.models.IdentityProperties;
import com.azure.resourcemanager.dynatrace.models.ManagedIdentityType;
import com.azure.resourcemanager.dynatrace.models.MarketplaceSubscriptionStatus;
import com.azure.resourcemanager.dynatrace.models.MonitoringStatus;
import com.azure.resourcemanager.dynatrace.models.PlanData;
import com.azure.resourcemanager.dynatrace.models.UserInfo;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/** Samples for Monitors CreateOrUpdate. */
public final class MonitorsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_CreateOrUpdate_MaximumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager
            .monitors()
            .define("myMonitor")
            .withRegion("West US 2")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("Environment", "Dev"))
            .withIdentity(new IdentityProperties().withType(ManagedIdentityType.SYSTEM_ASSIGNED))
            .withMonitoringStatus(MonitoringStatus.ENABLED)
            .withMarketplaceSubscriptionStatus(MarketplaceSubscriptionStatus.ACTIVE)
            .withDynatraceEnvironmentProperties(
                new DynatraceEnvironmentProperties()
                    .withAccountInfo(new AccountInfo())
                    .withEnvironmentInfo(new EnvironmentInfo())
                    .withSingleSignOnProperties(new DynatraceSingleSignOnProperties()))
            .withUserInfo(
                new UserInfo()
                    .withFirstName("Alice")
                    .withLastName("Bobab")
                    .withEmailAddress("alice@microsoft.com")
                    .withPhoneNumber("123456")
                    .withCountry("westus2"))
            .withPlanData(
                new PlanData()
                    .withUsageType("Committed")
                    .withBillingCycle("Monthly")
                    .withPlanDetails("dynatraceapitestplan")
                    .withEffectiveDate(OffsetDateTime.parse("2019-08-30T15:14:33+02:00")))
            .create();
    }

    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: Monitors_CreateOrUpdate_MinimumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager
            .monitors()
            .define("myMonitor")
            .withRegion("West US 2")
            .withExistingResourceGroup("myResourceGroup")
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

### Monitors_Delete

```java
import com.azure.core.util.Context;

/** Samples for Monitors Delete. */
public final class MonitorsDeleteSamples {
    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: Monitors_Delete_MinimumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsDeleteMinimumSetGen(com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.monitors().delete("myResourceGroup", "myMonitor", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_Delete_MaximumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsDeleteMaximumSetGen(com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.monitors().delete("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### Monitors_GetAccountCredentials

```java
import com.azure.core.util.Context;

/** Samples for Monitors GetAccountCredentials. */
public final class MonitorsGetAccountCredentialsSamples {
    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_GetAccountCredentials_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_GetAccountCredentials_MaximumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsGetAccountCredentialsMaximumSetGen(
        com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.monitors().getAccountCredentialsWithResponse("myResourceGroup", "myMonitor", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_GetAccountCredentials_MinimumSet_Gen.json
     */
    /**
     * Sample code: Monitors_GetAccountCredentials_MinimumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsGetAccountCredentialsMinimumSetGen(
        com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.monitors().getAccountCredentialsWithResponse("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### Monitors_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Monitors GetByResourceGroup. */
public final class MonitorsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: Monitors_Get_MinimumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsGetMinimumSetGen(com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.monitors().getByResourceGroupWithResponse("myResourceGroup", "myMonitor", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_Get_MaximumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsGetMaximumSetGen(com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.monitors().getByResourceGroupWithResponse("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### Monitors_GetSsoDetails

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dynatrace.models.SsoDetailsRequest;

/** Samples for Monitors GetSsoDetails. */
public final class MonitorsGetSsoDetailsSamples {
    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_GetSSODetails_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_GetSSODetails_MaximumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsGetSSODetailsMaximumSetGen(
        com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager
            .monitors()
            .getSsoDetailsWithResponse(
                "myResourceGroup",
                "myMonitor",
                new SsoDetailsRequest().withUserPrincipal("alice@microsoft.com"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_GetSSODetails_MinimumSet_Gen.json
     */
    /**
     * Sample code: Monitors_GetSSODetails_MinimumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsGetSSODetailsMinimumSetGen(
        com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager
            .monitors()
            .getSsoDetailsWithResponse("myResourceGroup", "myMonitor", new SsoDetailsRequest(), Context.NONE);
    }
}
```

### Monitors_GetVMHostPayload

```java
import com.azure.core.util.Context;

/** Samples for Monitors GetVMHostPayload. */
public final class MonitorsGetVMHostPayloadSamples {
    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_GetVMHostPayload_MinimumSet_Gen.json
     */
    /**
     * Sample code: Monitors_GetVMHostPayload_MinimumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsGetVMHostPayloadMinimumSetGen(
        com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.monitors().getVMHostPayloadWithResponse("myResourceGroup", "myMonitor", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_GetVMHostPayload_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_GetVMHostPayload_MaximumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsGetVMHostPayloadMaximumSetGen(
        com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.monitors().getVMHostPayloadWithResponse("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### Monitors_List

```java
import com.azure.core.util.Context;

/** Samples for Monitors List. */
public final class MonitorsListSamples {
    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_ListBySubscriptionId_MinimumSet_Gen.json
     */
    /**
     * Sample code: Monitors_ListBySubscriptionId_MinimumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsListBySubscriptionIdMinimumSetGen(
        com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.monitors().list(Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_ListBySubscriptionId_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_ListBySubscriptionId_MaximumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsListBySubscriptionIdMaximumSetGen(
        com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.monitors().list(Context.NONE);
    }
}
```

### Monitors_ListAppServices

```java
import com.azure.core.util.Context;

/** Samples for Monitors ListAppServices. */
public final class MonitorsListAppServicesSamples {
    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_ListAppServices_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_ListAppServices_MaximumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsListAppServicesMaximumSetGen(
        com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.monitors().listAppServices("myResourceGroup", "myMonitor", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_ListAppServices_MinimumSet_Gen.json
     */
    /**
     * Sample code: Monitors_ListAppServices_MinimumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsListAppServicesMinimumSetGen(
        com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.monitors().listAppServices("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### Monitors_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Monitors ListByResourceGroup. */
public final class MonitorsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_ListByResourceGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: Monitors_ListByResourceGroup_MinimumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsListByResourceGroupMinimumSetGen(
        com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.monitors().listByResourceGroup("myResourceGroup", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_ListByResourceGroup_MaximumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsListByResourceGroupMaximumSetGen(
        com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
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
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_ListHosts_MinimumSet_Gen.json
     */
    /**
     * Sample code: Monitors_ListHosts_MinimumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsListHostsMinimumSetGen(com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.monitors().listHosts("myResourceGroup", "myMonitor", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_ListHosts_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_ListHosts_MaximumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsListHostsMaximumSetGen(com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.monitors().listHosts("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### Monitors_ListLinkableEnvironments

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dynatrace.models.LinkableEnvironmentRequest;

/** Samples for Monitors ListLinkableEnvironments. */
public final class MonitorsListLinkableEnvironmentsSamples {
    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_ListLinkableEnvironments_MinimumSet_Gen.json
     */
    /**
     * Sample code: Monitors_ListLinkableEnvironments_MinimumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsListLinkableEnvironmentsMinimumSetGen(
        com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager
            .monitors()
            .listLinkableEnvironments("myResourceGroup", "myMonitor", new LinkableEnvironmentRequest(), Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_ListLinkableEnvironments_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_ListLinkableEnvironments_MaximumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsListLinkableEnvironmentsMaximumSetGen(
        com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager
            .monitors()
            .listLinkableEnvironments(
                "myResourceGroup",
                "myMonitor",
                new LinkableEnvironmentRequest()
                    .withTenantId("00000000-0000-0000-0000-000000000000")
                    .withUserPrincipal("alice@microsoft.com")
                    .withRegion("East US"),
                Context.NONE);
    }
}
```

### Monitors_ListMonitoredResources

```java
import com.azure.core.util.Context;

/** Samples for Monitors ListMonitoredResources. */
public final class MonitorsListMonitoredResourcesSamples {
    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_ListMonitoredResources_MinimumSet_Gen.json
     */
    /**
     * Sample code: Monitors_ListMonitoredResources_MinimumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsListMonitoredResourcesMinimumSetGen(
        com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.monitors().listMonitoredResources("myResourceGroup", "myMonitor", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_ListMonitoredResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_ListMonitoredResources_MaximumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsListMonitoredResourcesMaximumSetGen(
        com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.monitors().listMonitoredResources("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### Monitors_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dynatrace.fluent.models.DynatraceSingleSignOnProperties;
import com.azure.resourcemanager.dynatrace.models.AccountInfo;
import com.azure.resourcemanager.dynatrace.models.DynatraceEnvironmentProperties;
import com.azure.resourcemanager.dynatrace.models.EnvironmentInfo;
import com.azure.resourcemanager.dynatrace.models.MarketplaceSubscriptionStatus;
import com.azure.resourcemanager.dynatrace.models.MonitorResource;
import com.azure.resourcemanager.dynatrace.models.MonitoringStatus;
import com.azure.resourcemanager.dynatrace.models.PlanData;
import com.azure.resourcemanager.dynatrace.models.UserInfo;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/** Samples for Monitors Update. */
public final class MonitorsUpdateSamples {
    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: Monitors_Update_MinimumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsUpdateMinimumSetGen(com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        MonitorResource resource =
            manager.monitors().getByResourceGroupWithResponse("myResourceGroup", "myMonitor", Context.NONE).getValue();
        resource.update().apply();
    }

    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Monitors_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_Update_MaximumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void monitorsUpdateMaximumSetGen(com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        MonitorResource resource =
            manager.monitors().getByResourceGroupWithResponse("myResourceGroup", "myMonitor", Context.NONE).getValue();
        resource
            .update()
            .withTags(mapOf("Environment", "Dev"))
            .withMonitoringStatus(MonitoringStatus.ENABLED)
            .withMarketplaceSubscriptionStatus(MarketplaceSubscriptionStatus.ACTIVE)
            .withDynatraceEnvironmentProperties(
                new DynatraceEnvironmentProperties()
                    .withAccountInfo(new AccountInfo())
                    .withEnvironmentInfo(new EnvironmentInfo())
                    .withSingleSignOnProperties(new DynatraceSingleSignOnProperties()))
            .withUserInfo(
                new UserInfo()
                    .withFirstName("Alice")
                    .withLastName("Bobab")
                    .withEmailAddress("alice@microsoft.com")
                    .withPhoneNumber("123456")
                    .withCountry("westus2"))
            .withPlanData(
                new PlanData()
                    .withUsageType("Committed")
                    .withBillingCycle("Monthly")
                    .withPlanDetails("dynatraceapitestplan")
                    .withEffectiveDate(OffsetDateTime.parse("2019-08-30T15:14:33+02:00")))
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

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MinimumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void operationsListMinimumSetGen(com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.operations().list(Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MaximumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void operationsListMaximumSetGen(com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### SingleSignOn_CreateOrUpdate

```java
import com.azure.resourcemanager.dynatrace.models.SingleSignOnStates;
import java.util.Arrays;

/** Samples for SingleSignOn CreateOrUpdate. */
public final class SingleSignOnCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/SingleSignOn_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: SingleSignOn_CreateOrUpdate_MaximumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void singleSignOnCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager
            .singleSignOns()
            .define("default")
            .withExistingMonitor("myResourceGroup", "myMonitor")
            .withSingleSignOnState(SingleSignOnStates.ENABLE)
            .withEnterpriseAppId("00000000-0000-0000-0000-000000000000")
            .withSingleSignOnUrl("https://www.dynatrace.io")
            .withAadDomains(Arrays.asList("mpliftrdt20210811outlook.onmicrosoft.com"))
            .create();
    }

    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/SingleSignOn_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: SingleSignOn_CreateOrUpdate_MinimumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void singleSignOnCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager
            .singleSignOns()
            .define("default")
            .withExistingMonitor("myResourceGroup", "myMonitor")
            .withSingleSignOnUrl("https://www.dynatrace.io")
            .withAadDomains(Arrays.asList("mpliftrdt20210811outlook.onmicrosoft.com"))
            .create();
    }
}
```

### SingleSignOn_Get

```java
import com.azure.core.util.Context;

/** Samples for SingleSignOn Get. */
public final class SingleSignOnGetSamples {
    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/SingleSignOn_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: SingleSignOn_Get_MinimumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void singleSignOnGetMinimumSetGen(com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.singleSignOns().getWithResponse("myResourceGroup", "myMonitor", "default", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/SingleSignOn_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: SingleSignOn_Get_MaximumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void singleSignOnGetMaximumSetGen(com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
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
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/SingleSignOn_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: SingleSignOn_List_MaximumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void singleSignOnListMaximumSetGen(com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.singleSignOns().list("myResourceGroup", "myMonitor", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/SingleSignOn_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: SingleSignOn_List_MinimumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void singleSignOnListMinimumSetGen(com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.singleSignOns().list("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### TagRules_CreateOrUpdate

```java
import com.azure.resourcemanager.dynatrace.models.FilteringTag;
import com.azure.resourcemanager.dynatrace.models.LogRules;
import com.azure.resourcemanager.dynatrace.models.MetricRules;
import com.azure.resourcemanager.dynatrace.models.SendAadLogsStatus;
import com.azure.resourcemanager.dynatrace.models.SendActivityLogsStatus;
import com.azure.resourcemanager.dynatrace.models.SendSubscriptionLogsStatus;
import com.azure.resourcemanager.dynatrace.models.TagAction;
import java.util.Arrays;

/** Samples for TagRules CreateOrUpdate. */
public final class TagRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/TagRules_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: TagRules_CreateOrUpdate_MaximumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void tagRulesCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager
            .tagRules()
            .define("default")
            .withExistingMonitor("myResourceGroup", "myMonitor")
            .withLogRules(
                new LogRules()
                    .withSendAadLogs(SendAadLogsStatus.ENABLED)
                    .withSendSubscriptionLogs(SendSubscriptionLogsStatus.ENABLED)
                    .withSendActivityLogs(SendActivityLogsStatus.ENABLED)
                    .withFilteringTags(
                        Arrays
                            .asList(
                                new FilteringTag()
                                    .withName("Environment")
                                    .withValue("Prod")
                                    .withAction(TagAction.INCLUDE),
                                new FilteringTag()
                                    .withName("Environment")
                                    .withValue("Dev")
                                    .withAction(TagAction.EXCLUDE))))
            .withMetricRules(
                new MetricRules()
                    .withFilteringTags(
                        Arrays
                            .asList(
                                new FilteringTag()
                                    .withName("Environment")
                                    .withValue("Prod")
                                    .withAction(TagAction.INCLUDE))))
            .create();
    }

    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/TagRules_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: TagRules_CreateOrUpdate_MinimumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void tagRulesCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
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
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/TagRules_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: TagRules_Delete_MinimumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void tagRulesDeleteMinimumSetGen(com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.tagRules().delete("myResourceGroup", "myMonitor", "default", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/TagRules_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: TagRules_Delete_MaximumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void tagRulesDeleteMaximumSetGen(com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.tagRules().delete("myResourceGroup", "myMonitor", "default", Context.NONE);
    }
}
```

### TagRules_Get

```java
import com.azure.core.util.Context;

/** Samples for TagRules Get. */
public final class TagRulesGetSamples {
    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/TagRules_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: TagRules_Get_MinimumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void tagRulesGetMinimumSetGen(com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.tagRules().getWithResponse("myResourceGroup", "myMonitor", "default", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/TagRules_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: TagRules_Get_MaximumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void tagRulesGetMaximumSetGen(com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
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
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/TagRules_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: TagRules_List_MaximumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void tagRulesListMaximumSetGen(com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.tagRules().list("myResourceGroup", "myMonitor", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/TagRules_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: TagRules_List_MinimumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void tagRulesListMinimumSetGen(com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        manager.tagRules().list("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### TagRules_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dynatrace.models.FilteringTag;
import com.azure.resourcemanager.dynatrace.models.LogRules;
import com.azure.resourcemanager.dynatrace.models.MetricRules;
import com.azure.resourcemanager.dynatrace.models.SendAadLogsStatus;
import com.azure.resourcemanager.dynatrace.models.SendActivityLogsStatus;
import com.azure.resourcemanager.dynatrace.models.SendSubscriptionLogsStatus;
import com.azure.resourcemanager.dynatrace.models.TagAction;
import com.azure.resourcemanager.dynatrace.models.TagRule;
import java.util.Arrays;

/** Samples for TagRules Update. */
public final class TagRulesUpdateSamples {
    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/TagRules_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: TagRules_Update_MaximumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void tagRulesUpdateMaximumSetGen(com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        TagRule resource =
            manager.tagRules().getWithResponse("myResourceGroup", "myMonitor", "default", Context.NONE).getValue();
        resource
            .update()
            .withLogRules(
                new LogRules()
                    .withSendAadLogs(SendAadLogsStatus.ENABLED)
                    .withSendSubscriptionLogs(SendSubscriptionLogsStatus.ENABLED)
                    .withSendActivityLogs(SendActivityLogsStatus.ENABLED)
                    .withFilteringTags(
                        Arrays
                            .asList(
                                new FilteringTag()
                                    .withName("Environment")
                                    .withValue("Prod")
                                    .withAction(TagAction.INCLUDE),
                                new FilteringTag()
                                    .withName("Environment")
                                    .withValue("Dev")
                                    .withAction(TagAction.EXCLUDE))))
            .withMetricRules(
                new MetricRules()
                    .withFilteringTags(
                        Arrays
                            .asList(
                                new FilteringTag()
                                    .withName("Environment")
                                    .withValue("Prod")
                                    .withAction(TagAction.INCLUDE))))
            .apply();
    }

    /*
     * x-ms-original-file: specification/dynatrace/resource-manager/Dynatrace.Observability/stable/2021-09-01/examples/TagRules_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: TagRules_Update_MinimumSet_Gen.
     *
     * @param manager Entry point to DynatraceManager.
     */
    public static void tagRulesUpdateMinimumSetGen(com.azure.resourcemanager.dynatrace.DynatraceManager manager) {
        TagRule resource =
            manager.tagRules().getWithResponse("myResourceGroup", "myMonitor", "default", Context.NONE).getValue();
        resource.update().apply();
    }
}
```

