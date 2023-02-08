# Code snippets and samples


## Accounts

- [List](#accounts_list)

## Monitors

- [CreateOrUpdate](#monitors_createorupdate)
- [Delete](#monitors_delete)
- [GetByResourceGroup](#monitors_getbyresourcegroup)
- [GetMetricRules](#monitors_getmetricrules)
- [GetMetricStatus](#monitors_getmetricstatus)
- [List](#monitors_list)
- [ListAppServices](#monitors_listappservices)
- [ListByResourceGroup](#monitors_listbyresourcegroup)
- [ListHosts](#monitors_listhosts)
- [ListMonitoredResources](#monitors_listmonitoredresources)
- [SwitchBilling](#monitors_switchbilling)
- [Update](#monitors_update)
- [VmHostPayload](#monitors_vmhostpayload)

## Operations

- [List](#operations_list)

## Organizations

- [List](#organizations_list)

## Plans

- [List](#plans_list)

## TagRules

- [CreateOrUpdate](#tagrules_createorupdate)
- [Delete](#tagrules_delete)
- [Get](#tagrules_get)
- [ListByNewRelicMonitorResource](#tagrules_listbynewrelicmonitorresource)
- [Update](#tagrules_update)
### Accounts_List

```java
/** Samples for Accounts List. */
public final class AccountsListSamples {
    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Accounts_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Accounts_List_MinimumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void accountsListMinimumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager.accounts().list("ruxvg@xqkmdhrnoo.hlmbpm", "egh", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Accounts_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Accounts_List_MaximumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void accountsListMaximumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager.accounts().list("ruxvg@xqkmdhrnoo.hlmbpm", "egh", com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_CreateOrUpdate

```java
import com.azure.resourcemanager.newrelic.models.AccountCreationSource;
import com.azure.resourcemanager.newrelic.models.AccountInfo;
import com.azure.resourcemanager.newrelic.models.BillingCycle;
import com.azure.resourcemanager.newrelic.models.ManagedServiceIdentity;
import com.azure.resourcemanager.newrelic.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.newrelic.models.NewRelicAccountProperties;
import com.azure.resourcemanager.newrelic.models.NewRelicSingleSignOnProperties;
import com.azure.resourcemanager.newrelic.models.OrgCreationSource;
import com.azure.resourcemanager.newrelic.models.OrganizationInfo;
import com.azure.resourcemanager.newrelic.models.PlanData;
import com.azure.resourcemanager.newrelic.models.ProvisioningState;
import com.azure.resourcemanager.newrelic.models.SingleSignOnStates;
import com.azure.resourcemanager.newrelic.models.UsageType;
import com.azure.resourcemanager.newrelic.models.UserAssignedIdentity;
import com.azure.resourcemanager.newrelic.models.UserInfo;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/** Samples for Monitors CreateOrUpdate. */
public final class MonitorsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Monitors_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_CreateOrUpdate_MaximumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void monitorsCreateOrUpdateMaximumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager
            .monitors()
            .define("cdlymktqw")
            .withRegion("k")
            .withExistingResourceGroup("rgNewRelic")
            .withTags(mapOf("key6976", "oaxfhf"))
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.NONE)
                    .withUserAssignedIdentities(mapOf("key8903", new UserAssignedIdentity())))
            .withNewRelicAccountProperties(
                new NewRelicAccountProperties()
                    .withUserId("vcscxlncofcuduadesd")
                    .withAccountInfo(
                        new AccountInfo()
                            .withAccountId("xhqmg")
                            .withIngestionKey("fakeTokenPlaceholder")
                            .withRegion("ljcf"))
                    .withOrganizationInfo(new OrganizationInfo().withOrganizationId("k"))
                    .withSingleSignOnProperties(
                        new NewRelicSingleSignOnProperties()
                            .withSingleSignOnState(SingleSignOnStates.INITIAL)
                            .withEnterpriseAppId("kwiwfz")
                            .withSingleSignOnUrl("kvseueuljsxmfwpqctz")
                            .withProvisioningState(ProvisioningState.ACCEPTED)))
            .withUserInfo(
                new UserInfo()
                    .withFirstName("vdftzcggirefejajwahhwhyibutramdaotvnuf")
                    .withLastName("bcsztgqovdlmzfkjdrngidwzqsevagexzzilnlc")
                    .withEmailAddress("%6%@4-g.N1.3F-kI1.Ue-.lJso")
                    .withPhoneNumber("krf")
                    .withCountry("hslqnwdanrconqyekwbnttaetv"))
            .withPlanData(
                new PlanData()
                    .withUsageType(UsageType.PAYG)
                    .withBillingCycle(BillingCycle.YEARLY)
                    .withPlanDetails("tbbiaga")
                    .withEffectiveDate(OffsetDateTime.parse("2022-12-05T14:11:37.786Z")))
            .withOrgCreationSource(OrgCreationSource.LIFTR)
            .withAccountCreationSource(AccountCreationSource.LIFTR)
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
/** Samples for Monitors Delete. */
public final class MonitorsDeleteSamples {
    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Monitors_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: Monitors_Delete_MinimumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void monitorsDeleteMinimumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager.monitors().delete("rgopenapi", null, "ipxmlcbonyxtolzejcjshkmlron", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Monitors_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_Delete_MaximumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void monitorsDeleteMaximumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager
            .monitors()
            .delete(
                "rgopenapi",
                "ruxvg@xqkmdhrnoo.hlmbpm",
                "ipxmlcbonyxtolzejcjshkmlron",
                com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_GetByResourceGroup

```java
/** Samples for Monitors GetByResourceGroup. */
public final class MonitorsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Monitors_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_Get_MaximumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void monitorsGetMaximumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager.monitors().getByResourceGroupWithResponse("rgNewRelic", "cdlymktqw", com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_GetMetricRules

```java
import com.azure.resourcemanager.newrelic.models.MetricsRequest;

/** Samples for Monitors GetMetricRules. */
public final class MonitorsGetMetricRulesSamples {
    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Monitors_GetMetricRules_MinimumSet_Gen.json
     */
    /**
     * Sample code: Monitors_GetMetricRules_MinimumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void monitorsGetMetricRulesMinimumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager
            .monitors()
            .getMetricRulesWithResponse(
                "rgNewRelic",
                "fhcjxnxumkdlgpwanewtkdnyuz",
                new MetricsRequest().withUserEmail("ruxvg@xqkmdhrnoo.hlmbpm"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Monitors_GetMetricRules_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_GetMetricRules_MaximumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void monitorsGetMetricRulesMaximumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager
            .monitors()
            .getMetricRulesWithResponse(
                "rgNewRelic",
                "fhcjxnxumkdlgpwanewtkdnyuz",
                new MetricsRequest().withUserEmail("ruxvg@xqkmdhrnoo.hlmbpm"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_GetMetricStatus

```java
import com.azure.resourcemanager.newrelic.models.MetricsStatusRequest;
import java.util.Arrays;

/** Samples for Monitors GetMetricStatus. */
public final class MonitorsGetMetricStatusSamples {
    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Monitors_GetMetricStatus_MinimumSet_Gen.json
     */
    /**
     * Sample code: Monitors_GetMetricStatus_MinimumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void monitorsGetMetricStatusMinimumSetGen(
        com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager
            .monitors()
            .getMetricStatusWithResponse(
                "rgNewRelic",
                "fhcjxnxumkdlgpwanewtkdnyuz",
                new MetricsStatusRequest().withUserEmail("ruxvg@xqkmdhrnoo.hlmbpm"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Monitors_GetMetricStatus_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_GetMetricStatus_MaximumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void monitorsGetMetricStatusMaximumSetGen(
        com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager
            .monitors()
            .getMetricStatusWithResponse(
                "rgNewRelic",
                "fhcjxnxumkdlgpwanewtkdnyuz",
                new MetricsStatusRequest()
                    .withAzureResourceIds(Arrays.asList("enfghpfw"))
                    .withUserEmail("ruxvg@xqkmdhrnoo.hlmbpm"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_List

```java
/** Samples for Monitors List. */
public final class MonitorsListSamples {
    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Monitors_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_ListBySubscription_MaximumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void monitorsListBySubscriptionMaximumSetGen(
        com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager.monitors().list(com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_ListAppServices

```java
import com.azure.resourcemanager.newrelic.models.AppServicesGetRequest;
import java.util.Arrays;

/** Samples for Monitors ListAppServices. */
public final class MonitorsListAppServicesSamples {
    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Monitors_ListAppServices_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_ListAppServices_MaximumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void monitorsListAppServicesMaximumSetGen(
        com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager
            .monitors()
            .listAppServices(
                "rgNewRelic",
                "fhcjxnxumkdlgpwanewtkdnyuz",
                new AppServicesGetRequest()
                    .withAzureResourceIds(Arrays.asList("pvzrksrmzowobuhxpwiotnpcvjbu"))
                    .withUserEmail("ruxvg@xqkmdhrnoo.hlmbpm"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Monitors_ListAppServices_MinimumSet_Gen.json
     */
    /**
     * Sample code: Monitors_ListAppServices_MinimumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void monitorsListAppServicesMinimumSetGen(
        com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager
            .monitors()
            .listAppServices(
                "rgNewRelic",
                "fhcjxnxumkdlgpwanewtkdnyuz",
                new AppServicesGetRequest().withUserEmail("ruxvg@xqkmdhrnoo.hlmbpm"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_ListByResourceGroup

```java
/** Samples for Monitors ListByResourceGroup. */
public final class MonitorsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Monitors_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_ListByResourceGroup_MaximumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void monitorsListByResourceGroupMaximumSetGen(
        com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager.monitors().listByResourceGroup("rgNewRelic", com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_ListHosts

```java
import com.azure.resourcemanager.newrelic.models.HostsGetRequest;
import java.util.Arrays;

/** Samples for Monitors ListHosts. */
public final class MonitorsListHostsSamples {
    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Monitors_ListHosts_MinimumSet_Gen.json
     */
    /**
     * Sample code: Monitors_ListHosts_MinimumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void monitorsListHostsMinimumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager
            .monitors()
            .listHosts(
                "rgopenapi",
                "ipxmlcbonyxtolzejcjshkmlron",
                new HostsGetRequest()
                    .withVmIds(Arrays.asList("xzphvxvfmvjrnsgyns"))
                    .withUserEmail("ruxvg@xqkmdhrnoo.hlmbpm"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Monitors_ListHosts_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_ListHosts_MaximumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void monitorsListHostsMaximumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager
            .monitors()
            .listHosts(
                "rgopenapi",
                "ipxmlcbonyxtolzejcjshkmlron",
                new HostsGetRequest()
                    .withVmIds(Arrays.asList("xzphvxvfmvjrnsgyns"))
                    .withUserEmail("ruxvg@xqkmdhrnoo.hlmbpm"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_ListMonitoredResources

```java
/** Samples for Monitors ListMonitoredResources. */
public final class MonitorsListMonitoredResourcesSamples {
    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Monitors_ListMonitoredResources_MinimumSet_Gen.json
     */
    /**
     * Sample code: Monitors_ListMonitoredResources_MinimumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void monitorsListMonitoredResourcesMinimumSetGen(
        com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager
            .monitors()
            .listMonitoredResources("rgopenapi", "ipxmlcbonyxtolzejcjshkmlron", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Monitors_ListMonitoredResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_ListMonitoredResources_MaximumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void monitorsListMonitoredResourcesMaximumSetGen(
        com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager
            .monitors()
            .listMonitoredResources("rgopenapi", "ipxmlcbonyxtolzejcjshkmlron", com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_SwitchBilling

```java
import com.azure.resourcemanager.newrelic.models.BillingCycle;
import com.azure.resourcemanager.newrelic.models.PlanData;
import com.azure.resourcemanager.newrelic.models.SwitchBillingRequest;
import com.azure.resourcemanager.newrelic.models.UsageType;
import java.time.OffsetDateTime;

/** Samples for Monitors SwitchBilling. */
public final class MonitorsSwitchBillingSamples {
    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Monitors_SwitchBilling_MinimumSet_Gen.json
     */
    /**
     * Sample code: Monitors_SwitchBilling_MinimumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void monitorsSwitchBillingMinimumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager
            .monitors()
            .switchBillingWithResponse(
                "rgNewRelic",
                "fhcjxnxumkdlgpwanewtkdnyuz",
                new SwitchBillingRequest().withUserEmail("ruxvg@xqkmdhrnoo.hlmbpm"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Monitors_SwitchBilling_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_SwitchBilling_MaximumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void monitorsSwitchBillingMaximumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager
            .monitors()
            .switchBillingWithResponse(
                "rgNewRelic",
                "fhcjxnxumkdlgpwanewtkdnyuz",
                new SwitchBillingRequest()
                    .withAzureResourceId("enfghpfw")
                    .withOrganizationId("k")
                    .withPlanData(
                        new PlanData()
                            .withUsageType(UsageType.PAYG)
                            .withBillingCycle(BillingCycle.YEARLY)
                            .withPlanDetails("tbbiaga")
                            .withEffectiveDate(OffsetDateTime.parse("2022-12-05T14:11:37.786Z")))
                    .withUserEmail("ruxvg@xqkmdhrnoo.hlmbpm"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_Update

```java
import com.azure.resourcemanager.newrelic.models.AccountCreationSource;
import com.azure.resourcemanager.newrelic.models.AccountInfo;
import com.azure.resourcemanager.newrelic.models.BillingCycle;
import com.azure.resourcemanager.newrelic.models.ManagedServiceIdentity;
import com.azure.resourcemanager.newrelic.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.newrelic.models.NewRelicAccountProperties;
import com.azure.resourcemanager.newrelic.models.NewRelicMonitorResource;
import com.azure.resourcemanager.newrelic.models.NewRelicSingleSignOnProperties;
import com.azure.resourcemanager.newrelic.models.OrgCreationSource;
import com.azure.resourcemanager.newrelic.models.OrganizationInfo;
import com.azure.resourcemanager.newrelic.models.PlanData;
import com.azure.resourcemanager.newrelic.models.ProvisioningState;
import com.azure.resourcemanager.newrelic.models.SingleSignOnStates;
import com.azure.resourcemanager.newrelic.models.UsageType;
import com.azure.resourcemanager.newrelic.models.UserAssignedIdentity;
import com.azure.resourcemanager.newrelic.models.UserInfo;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/** Samples for Monitors Update. */
public final class MonitorsUpdateSamples {
    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Monitors_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_Update_MaximumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void monitorsUpdateMaximumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        NewRelicMonitorResource resource =
            manager
                .monitors()
                .getByResourceGroupWithResponse("rgNewRelic", "cdlymktqw", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key164", "jqakdrrmmyzytqu"))
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.NONE)
                    .withUserAssignedIdentities(mapOf("key8903", new UserAssignedIdentity())))
            .withNewRelicAccountProperties(
                new NewRelicAccountProperties()
                    .withUserId("vcscxlncofcuduadesd")
                    .withAccountInfo(
                        new AccountInfo()
                            .withAccountId("xhqmg")
                            .withIngestionKey("fakeTokenPlaceholder")
                            .withRegion("ljcf"))
                    .withOrganizationInfo(new OrganizationInfo().withOrganizationId("k"))
                    .withSingleSignOnProperties(
                        new NewRelicSingleSignOnProperties()
                            .withSingleSignOnState(SingleSignOnStates.INITIAL)
                            .withEnterpriseAppId("kwiwfz")
                            .withSingleSignOnUrl("kvseueuljsxmfwpqctz")
                            .withProvisioningState(ProvisioningState.ACCEPTED)))
            .withUserInfo(
                new UserInfo()
                    .withFirstName("vdftzcggirefejajwahhwhyibutramdaotvnuf")
                    .withLastName("bcsztgqovdlmzfkjdrngidwzqsevagexzzilnlc")
                    .withEmailAddress("%6%@4-g.N1.3F-kI1.Ue-.lJso")
                    .withPhoneNumber("krf")
                    .withCountry("hslqnwdanrconqyekwbnttaetv"))
            .withPlanData(
                new PlanData()
                    .withUsageType(UsageType.PAYG)
                    .withBillingCycle(BillingCycle.YEARLY)
                    .withPlanDetails("tbbiaga")
                    .withEffectiveDate(OffsetDateTime.parse("2022-12-05T14:11:37.786Z")))
            .withOrgCreationSource(OrgCreationSource.LIFTR)
            .withAccountCreationSource(AccountCreationSource.LIFTR)
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

### Monitors_VmHostPayload

```java
/** Samples for Monitors VmHostPayload. */
public final class MonitorsVmHostPayloadSamples {
    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Monitors_VmHostPayload_MinimumSet_Gen.json
     */
    /**
     * Sample code: Monitors_VmHostPayload_MinimumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void monitorsVmHostPayloadMinimumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager
            .monitors()
            .vmHostPayloadWithResponse("rgopenapi", "ipxmlcbonyxtolzejcjshkmlron", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Monitors_VmHostPayload_MaximumSet_Gen.json
     */
    /**
     * Sample code: Monitors_VmHostPayload_MaximumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void monitorsVmHostPayloadMaximumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager
            .monitors()
            .vmHostPayloadWithResponse("rgopenapi", "ipxmlcbonyxtolzejcjshkmlron", com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MinimumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void operationsListMinimumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MaximumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void operationsListMaximumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Organizations_List

```java
/** Samples for Organizations List. */
public final class OrganizationsListSamples {
    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Organizations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Organizations_List_MinimumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void organizationsListMinimumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager.organizations().list("ruxvg@xqkmdhrnoo.hlmbpm", "egh", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Organizations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_List_MaximumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void organizationsListMaximumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager.organizations().list("ruxvg@xqkmdhrnoo.hlmbpm", "egh", com.azure.core.util.Context.NONE);
    }
}
```

### Plans_List

```java
/** Samples for Plans List. */
public final class PlansListSamples {
    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Plans_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Plans_List_MaximumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void plansListMaximumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager.plans().list("pwuxgvrmkk", "hilawwjz", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/Plans_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Plans_List_MinimumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void plansListMinimumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager.plans().list(null, null, com.azure.core.util.Context.NONE);
    }
}
```

### TagRules_CreateOrUpdate

```java
import com.azure.resourcemanager.newrelic.fluent.models.MetricRulesInner;
import com.azure.resourcemanager.newrelic.models.FilteringTag;
import com.azure.resourcemanager.newrelic.models.LogRules;
import com.azure.resourcemanager.newrelic.models.SendAadLogsStatus;
import com.azure.resourcemanager.newrelic.models.SendActivityLogsStatus;
import com.azure.resourcemanager.newrelic.models.SendSubscriptionLogsStatus;
import com.azure.resourcemanager.newrelic.models.TagAction;
import java.util.Arrays;

/** Samples for TagRules CreateOrUpdate. */
public final class TagRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/TagRules_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: TagRules_CreateOrUpdate_MaximumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void tagRulesCreateOrUpdateMaximumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager
            .tagRules()
            .define("bxcantgzggsepbhqmedjqyrqeezmfb")
            .withExistingMonitor("rgopenapi", "ipxmlcbonyxtolzejcjshkmlron")
            .withLogRules(
                new LogRules()
                    .withSendAadLogs(SendAadLogsStatus.ENABLED)
                    .withSendSubscriptionLogs(SendSubscriptionLogsStatus.ENABLED)
                    .withSendActivityLogs(SendActivityLogsStatus.ENABLED)
                    .withFilteringTags(
                        Arrays
                            .asList(
                                new FilteringTag()
                                    .withName("saokgpjvdlorciqbjmjxazpee")
                                    .withValue("sarxrqsxouhdjwsrqqicbeirdb")
                                    .withAction(TagAction.INCLUDE))))
            .withMetricRules(
                new MetricRulesInner()
                    .withFilteringTags(
                        Arrays
                            .asList(
                                new FilteringTag()
                                    .withName("saokgpjvdlorciqbjmjxazpee")
                                    .withValue("sarxrqsxouhdjwsrqqicbeirdb")
                                    .withAction(TagAction.INCLUDE)))
                    .withUserEmail("test@testing.com"))
            .create();
    }

    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/TagRules_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: TagRules_CreateOrUpdate_MinimumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void tagRulesCreateOrUpdateMinimumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager
            .tagRules()
            .define("bxcantgzggsepbhqmedjqyrqeezmfb")
            .withExistingMonitor("rgopenapi", "ipxmlcbonyxtolzejcjshkmlron")
            .create();
    }
}
```

### TagRules_Delete

```java
/** Samples for TagRules Delete. */
public final class TagRulesDeleteSamples {
    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/TagRules_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: TagRules_Delete_MinimumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void tagRulesDeleteMinimumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager
            .tagRules()
            .delete(
                "rgopenapi",
                "ipxmlcbonyxtolzejcjshkmlron",
                "bxcantgzggsepbhqmedjqyrqeezmfb",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/TagRules_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: TagRules_Delete_MaximumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void tagRulesDeleteMaximumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager
            .tagRules()
            .delete(
                "rgopenapi",
                "ipxmlcbonyxtolzejcjshkmlron",
                "bxcantgzggsepbhqmedjqyrqeezmfb",
                com.azure.core.util.Context.NONE);
    }
}
```

### TagRules_Get

```java
/** Samples for TagRules Get. */
public final class TagRulesGetSamples {
    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/TagRules_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: TagRules_Get_MinimumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void tagRulesGetMinimumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager
            .tagRules()
            .getWithResponse(
                "rgopenapi",
                "ipxmlcbonyxtolzejcjshkmlron",
                "bxcantgzggsepbhqmedjqyrqeezmfb",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/TagRules_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: TagRules_Get_MaximumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void tagRulesGetMaximumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager
            .tagRules()
            .getWithResponse(
                "rgopenapi",
                "ipxmlcbonyxtolzejcjshkmlron",
                "bxcantgzggsepbhqmedjqyrqeezmfb",
                com.azure.core.util.Context.NONE);
    }
}
```

### TagRules_ListByNewRelicMonitorResource

```java
/** Samples for TagRules ListByNewRelicMonitorResource. */
public final class TagRulesListByNewRelicMonitorResourceSamples {
    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/TagRules_ListByNewRelicMonitorResource_MinimumSet_Gen.json
     */
    /**
     * Sample code: TagRules_ListByNewRelicMonitorResource_MinimumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void tagRulesListByNewRelicMonitorResourceMinimumSetGen(
        com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager
            .tagRules()
            .listByNewRelicMonitorResource(
                "rgopenapi", "ipxmlcbonyxtolzejcjshkmlron", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/TagRules_ListByNewRelicMonitorResource_MaximumSet_Gen.json
     */
    /**
     * Sample code: TagRules_ListByNewRelicMonitorResource_MaximumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void tagRulesListByNewRelicMonitorResourceMaximumSetGen(
        com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        manager
            .tagRules()
            .listByNewRelicMonitorResource(
                "rgopenapi", "ipxmlcbonyxtolzejcjshkmlron", com.azure.core.util.Context.NONE);
    }
}
```

### TagRules_Update

```java
import com.azure.resourcemanager.newrelic.fluent.models.MetricRulesInner;
import com.azure.resourcemanager.newrelic.models.FilteringTag;
import com.azure.resourcemanager.newrelic.models.LogRules;
import com.azure.resourcemanager.newrelic.models.SendAadLogsStatus;
import com.azure.resourcemanager.newrelic.models.SendActivityLogsStatus;
import com.azure.resourcemanager.newrelic.models.SendSubscriptionLogsStatus;
import com.azure.resourcemanager.newrelic.models.TagAction;
import com.azure.resourcemanager.newrelic.models.TagRule;
import java.util.Arrays;

/** Samples for TagRules Update. */
public final class TagRulesUpdateSamples {
    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/TagRules_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: TagRules_Update_MaximumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void tagRulesUpdateMaximumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        TagRule resource =
            manager
                .tagRules()
                .getWithResponse(
                    "rgopenapi",
                    "ipxmlcbonyxtolzejcjshkmlron",
                    "bxcantgzggsepbhqmedjqyrqeezmfb",
                    com.azure.core.util.Context.NONE)
                .getValue();
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
                                    .withName("saokgpjvdlorciqbjmjxazpee")
                                    .withValue("sarxrqsxouhdjwsrqqicbeirdb")
                                    .withAction(TagAction.INCLUDE))))
            .withMetricRules(
                new MetricRulesInner()
                    .withFilteringTags(
                        Arrays
                            .asList(
                                new FilteringTag()
                                    .withName("saokgpjvdlorciqbjmjxazpee")
                                    .withValue("sarxrqsxouhdjwsrqqicbeirdb")
                                    .withAction(TagAction.INCLUDE)))
                    .withUserEmail("test@testing.com"))
            .apply();
    }

    /*
     * x-ms-original-file: specification/newrelic/resource-manager/NewRelic.Observability/preview/2022-07-01-preview/examples/TagRules_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: TagRules_Update_MinimumSet_Gen.
     *
     * @param manager Entry point to NewRelicManager.
     */
    public static void tagRulesUpdateMinimumSetGen(com.azure.resourcemanager.newrelic.NewRelicManager manager) {
        TagRule resource =
            manager
                .tagRules()
                .getWithResponse(
                    "rgopenapi",
                    "ipxmlcbonyxtolzejcjshkmlron",
                    "bxcantgzggsepbhqmedjqyrqeezmfb",
                    com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().apply();
    }
}
```

