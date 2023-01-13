# Code snippets and samples


## Alerts

- [Dismiss](#alerts_dismiss)
- [Get](#alerts_get)
- [List](#alerts_list)
- [ListExternal](#alerts_listexternal)

## Dimensions

- [ByExternalCloudProviderType](#dimensions_byexternalcloudprovidertype)
- [List](#dimensions_list)

## Exports

- [CreateOrUpdate](#exports_createorupdate)
- [Delete](#exports_delete)
- [Execute](#exports_execute)
- [Get](#exports_get)
- [GetExecutionHistory](#exports_getexecutionhistory)
- [List](#exports_list)

## Forecast

- [ExternalCloudProviderUsage](#forecast_externalcloudproviderusage)
- [Usage](#forecast_usage)

## Query

- [Usage](#query_usage)
- [UsageByExternalCloudProviderType](#query_usagebyexternalcloudprovidertype)

## Views

- [CreateOrUpdate](#views_createorupdate)
- [CreateOrUpdateByScope](#views_createorupdatebyscope)
- [Delete](#views_delete)
- [DeleteByScope](#views_deletebyscope)
- [Get](#views_get)
- [GetByScope](#views_getbyscope)
- [List](#views_list)
- [ListByScope](#views_listbyscope)
### Alerts_Dismiss

```java
import com.azure.resourcemanager.costmanagement.models.AlertStatus;
import com.azure.resourcemanager.costmanagement.models.DismissAlertPayload;

/** Samples for Alerts Dismiss. */
public final class AlertsDismissSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/DismissSubscriptionAlerts.json
     */
    /**
     * Sample code: SubscriptionAlerts.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void subscriptionAlerts(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .alerts()
            .dismissWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000",
                "22222222-2222-2222-2222-222222222222",
                new DismissAlertPayload().withStatus(AlertStatus.DISMISSED),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/DismissResourceGroupAlerts.json
     */
    /**
     * Sample code: ResourceGroupAlerts.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void resourceGroupAlerts(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .alerts()
            .dismissWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/ScreenSharingTest-peer",
                "22222222-2222-2222-2222-222222222222",
                new DismissAlertPayload().withStatus(AlertStatus.DISMISSED),
                com.azure.core.util.Context.NONE);
    }
}
```

### Alerts_Get

```java
/** Samples for Alerts Get. */
public final class AlertsGetSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/SingleSubscriptionAlert.json
     */
    /**
     * Sample code: SubscriptionAlerts.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void subscriptionAlerts(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .alerts()
            .getWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000",
                "22222222-2222-2222-2222-222222222222",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/SingleResourceGroupAlert.json
     */
    /**
     * Sample code: ResourceGroupAlerts.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void resourceGroupAlerts(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .alerts()
            .getWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/ScreenSharingTest-peer",
                "22222222-2222-2222-2222-222222222222",
                com.azure.core.util.Context.NONE);
    }
}
```

### Alerts_List

```java
/** Samples for Alerts List. */
public final class AlertsListSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/SubscriptionAlerts.json
     */
    /**
     * Sample code: SubscriptionAlerts.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void subscriptionAlerts(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .alerts()
            .listWithResponse("subscriptions/00000000-0000-0000-0000-000000000000", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/BillingProfileAlerts.json
     */
    /**
     * Sample code: BillingProfileAlerts.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingProfileAlerts(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .alerts()
            .listWithResponse(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/billingProfiles/13579",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ResourceGroupAlerts.json
     */
    /**
     * Sample code: ResourceGroupAlerts.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void resourceGroupAlerts(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .alerts()
            .listWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/ScreenSharingTest-peer",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/DepartmentAlerts.json
     */
    /**
     * Sample code: DepartmentAlerts.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void departmentAlerts(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .alerts()
            .listWithResponse(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/departments/123",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/InvoiceSectionAlerts.json
     */
    /**
     * Sample code: InvoiceSectionAlerts.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void invoiceSectionAlerts(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .alerts()
            .listWithResponse(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/billingProfiles/13579/invoiceSections/9876",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/BillingAccountAlerts.json
     */
    /**
     * Sample code: BillingAccountAlerts.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingAccountAlerts(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .alerts()
            .listWithResponse(
                "providers/Microsoft.Billing/billingAccounts/12345:6789", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/EnrollmentAccountAlerts.json
     */
    /**
     * Sample code: EnrollmentAccountAlerts.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void enrollmentAccountAlerts(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .alerts()
            .listWithResponse(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/enrollmentAccounts/456",
                com.azure.core.util.Context.NONE);
    }
}
```

### Alerts_ListExternal

```java
import com.azure.resourcemanager.costmanagement.models.ExternalCloudProviderType;

/** Samples for Alerts ListExternal. */
public final class AlertsListExternalSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExternalBillingAccountAlerts.json
     */
    /**
     * Sample code: ExternalBillingAccountAlerts.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void externalBillingAccountAlerts(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .alerts()
            .listExternalWithResponse(
                ExternalCloudProviderType.EXTERNAL_BILLING_ACCOUNTS, "100", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExternalSubscriptionAlerts.json
     */
    /**
     * Sample code: ExternalSubscriptionAlerts.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void externalSubscriptionAlerts(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .alerts()
            .listExternalWithResponse(
                ExternalCloudProviderType.EXTERNAL_SUBSCRIPTIONS, "100", com.azure.core.util.Context.NONE);
    }
}
```

### Dimensions_ByExternalCloudProviderType

```java
import com.azure.resourcemanager.costmanagement.models.ExternalCloudProviderType;

/** Samples for Dimensions ByExternalCloudProviderType. */
public final class DimensionsByExternalCloudProviderTypeSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExternalSubscriptionsDimensions.json
     */
    /**
     * Sample code: ExternalSubscriptionDimensionList.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void externalSubscriptionDimensionList(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .byExternalCloudProviderType(
                ExternalCloudProviderType.EXTERNAL_SUBSCRIPTIONS,
                "100",
                null,
                null,
                null,
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExternalBillingAccountsDimensions.json
     */
    /**
     * Sample code: ExternalBillingAccountDimensionList.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void externalBillingAccountDimensionList(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .byExternalCloudProviderType(
                ExternalCloudProviderType.EXTERNAL_BILLING_ACCOUNTS,
                "100",
                null,
                null,
                null,
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Dimensions_List

```java
/** Samples for Dimensions List. */
public final class DimensionsListSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/DepartmentDimensionsList.json
     */
    /**
     * Sample code: DepartmentDimensionsList-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void departmentDimensionsListLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "providers/Microsoft.Billing/billingAccounts/100/departments/123",
                null,
                null,
                null,
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/MCACustomerDimensionsList.json
     */
    /**
     * Sample code: CustomerDimensionsList-Modern.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void customerDimensionsListModern(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/customers/5678",
                null,
                null,
                null,
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ManagementGroupDimensionsListWithFilter.json
     */
    /**
     * Sample code: ManagementGroupDimensionsListWithFilter-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void managementGroupDimensionsListWithFilterLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "providers/Microsoft.Management/managementGroups/MyMgId",
                "properties/category eq 'resourceId'",
                "properties/data",
                null,
                5,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/BillingAccountDimensionsList.json
     */
    /**
     * Sample code: BillingAccountDimensionsList-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingAccountDimensionsListLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "providers/Microsoft.Billing/billingAccounts/100",
                null,
                null,
                null,
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ManagementGroupDimensionsListExpandAndTop.json
     */
    /**
     * Sample code: ManagementGroupDimensionsListExpandAndTop-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void managementGroupDimensionsListExpandAndTopLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "providers/Microsoft.Management/managementGroups/MyMgId",
                null,
                "properties/data",
                null,
                5,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/DepartmentDimensionsListExpandAndTop.json
     */
    /**
     * Sample code: DepartmentDimensionsListExpandAndTop-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void departmentDimensionsListExpandAndTopLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "providers/Microsoft.Billing/billingAccounts/100/departments/123",
                null,
                "properties/data",
                null,
                5,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/EnrollmentAccountDimensionsList.json
     */
    /**
     * Sample code: EnrollmentAccountDimensionsList-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void enrollmentAccountDimensionsListLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "providers/Microsoft.Billing/billingAccounts/100/enrollmentAccounts/456",
                null,
                null,
                null,
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/BillingAccountDimensionsListWithFilter.json
     */
    /**
     * Sample code: BillingAccountDimensionsListWithFilter-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingAccountDimensionsListWithFilterLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "providers/Microsoft.Billing/billingAccounts/100",
                "properties/category eq 'resourceId'",
                "properties/data",
                null,
                5,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/MCACustomerDimensionsListExpandAndTop.json
     */
    /**
     * Sample code: CustomerDimensionsListExpandAndTop-Modern.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void customerDimensionsListExpandAndTopModern(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/customers/5678",
                null,
                "properties/data",
                null,
                5,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ResourceGroupDimensionsList.json
     */
    /**
     * Sample code: ResourceGroupDimensionsList-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void resourceGroupDimensionsListLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/system.orlando",
                null,
                "properties/data",
                null,
                5,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/MCAInvoiceSectionDimensionsListExpandAndTop.json
     */
    /**
     * Sample code: InvoiceSectionDimensionsListExpandAndTop-Modern.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void invoiceSectionDimensionsListExpandAndTopModern(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/billingProfiles/13579/invoiceSections/9876",
                null,
                "properties/data",
                null,
                5,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/MCACustomerDimensionsListWithFilter.json
     */
    /**
     * Sample code: CustomerDimensionsListWithFilter-Modern.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void customerDimensionsListWithFilterModern(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/customers/5678",
                "properties/category eq 'resourceId'",
                "properties/data",
                null,
                5,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/SubscriptionDimensionsList.json
     */
    /**
     * Sample code: SubscriptionDimensionsList-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void subscriptionDimensionsListLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "subscriptions/00000000-0000-0000-0000-000000000000",
                null,
                "properties/data",
                null,
                5,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/BillingAccountDimensionsListExpandAndTop.json
     */
    /**
     * Sample code: BillingAccountDimensionsListExpandAndTop-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingAccountDimensionsListExpandAndTopLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "providers/Microsoft.Billing/billingAccounts/100",
                null,
                "properties/data",
                null,
                5,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/EnrollmentAccountDimensionsListExpandAndTop.json
     */
    /**
     * Sample code: EnrollmentAccountDimensionsListExpandAndTop-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void enrollmentAccountDimensionsListExpandAndTopLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "providers/Microsoft.Billing/billingAccounts/100/enrollmentAccounts/456",
                null,
                "properties/data",
                null,
                5,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/DepartmentDimensionsListWithFilter.json
     */
    /**
     * Sample code: DepartmentDimensionsListWithFilter-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void departmentDimensionsListWithFilterLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "providers/Microsoft.Billing/billingAccounts/100/departments/123",
                "properties/category eq 'resourceId'",
                "properties/data",
                null,
                5,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/MCABillingAccountDimensionsListExpandAndTop.json
     */
    /**
     * Sample code: BillingAccountDimensionsListExpandAndTop-Modern.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingAccountDimensionsListExpandAndTopModern(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "providers/Microsoft.Billing/billingAccounts/12345:6789",
                null,
                "properties/data",
                null,
                5,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/MCABillingProfileDimensionsListExpandAndTop.json
     */
    /**
     * Sample code: BillingProfileDimensionsListExpandAndTop-Modern.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingProfileDimensionsListExpandAndTopModern(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/billingProfiles/13579",
                null,
                "properties/data",
                null,
                5,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/MCABillingProfileDimensionsList.json
     */
    /**
     * Sample code: BillingProfileDimensionsList-Modern.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingProfileDimensionsListModern(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/billingProfiles/13579",
                null,
                null,
                null,
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ManagementGroupDimensionsList.json
     */
    /**
     * Sample code: ManagementGroupDimensionsList-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void managementGroupDimensionsListLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "providers/Microsoft.Management/managementGroups/MyMgId",
                null,
                null,
                null,
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/MCABillingAccountDimensionsListWithFilter.json
     */
    /**
     * Sample code: BillingAccountDimensionsListWithFilter-Modern.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingAccountDimensionsListWithFilterModern(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "providers/Microsoft.Billing/billingAccounts/12345:6789",
                "properties/category eq 'resourceId'",
                "properties/data",
                null,
                5,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/MCABillingProfileDimensionsListWithFilter.json
     */
    /**
     * Sample code: BillingProfileDimensionsListWithFilter-Modern.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingProfileDimensionsListWithFilterModern(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/billingProfiles/13579",
                "properties/category eq 'resourceId'",
                "properties/data",
                null,
                5,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/MCABillingAccountDimensionsList.json
     */
    /**
     * Sample code: BillingAccountDimensionsList-Modern.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingAccountDimensionsListModern(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "providers/Microsoft.Billing/billingAccounts/12345:6789",
                null,
                null,
                null,
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/MCAInvoiceSectionDimensionsListWithFilter.json
     */
    /**
     * Sample code: InvoiceSectionDimensionsListWithFilter-Modern.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void invoiceSectionDimensionsListWithFilterModern(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/billingProfiles/13579/invoiceSections/9876",
                "properties/category eq 'resourceId'",
                "properties/data",
                null,
                5,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/EnrollmentAccountDimensionsListWithFilter.json
     */
    /**
     * Sample code: EnrollmentAccountDimensionsListWithFilter-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void enrollmentAccountDimensionsListWithFilterLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "providers/Microsoft.Billing/billingAccounts/100/enrollmentAccounts/456",
                "properties/category eq 'resourceId'",
                "properties/data",
                null,
                5,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/MCAInvoiceSectionDimensionsList.json
     */
    /**
     * Sample code: InvoiceSectionDimensionsList-Modern.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void invoiceSectionDimensionsListModern(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .dimensions()
            .list(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/billingProfiles/13579/invoiceSections/9876",
                null,
                null,
                null,
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Exports_CreateOrUpdate

```java
import com.azure.resourcemanager.costmanagement.models.ExportDataset;
import com.azure.resourcemanager.costmanagement.models.ExportDatasetConfiguration;
import com.azure.resourcemanager.costmanagement.models.ExportDefinition;
import com.azure.resourcemanager.costmanagement.models.ExportDeliveryDestination;
import com.azure.resourcemanager.costmanagement.models.ExportDeliveryInfo;
import com.azure.resourcemanager.costmanagement.models.ExportRecurrencePeriod;
import com.azure.resourcemanager.costmanagement.models.ExportSchedule;
import com.azure.resourcemanager.costmanagement.models.ExportType;
import com.azure.resourcemanager.costmanagement.models.FormatType;
import com.azure.resourcemanager.costmanagement.models.GranularityType;
import com.azure.resourcemanager.costmanagement.models.RecurrenceType;
import com.azure.resourcemanager.costmanagement.models.StatusType;
import com.azure.resourcemanager.costmanagement.models.TimeframeType;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Samples for Exports CreateOrUpdate. */
public final class ExportsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportCreateOrUpdateByManagementGroup.json
     */
    /**
     * Sample code: ExportCreateOrUpdateByManagementGroup.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportCreateOrUpdateByManagementGroup(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .define("TestExport")
            .withExistingScope("providers/Microsoft.Management/managementGroups/TestMG")
            .withSchedule(
                new ExportSchedule()
                    .withStatus(StatusType.ACTIVE)
                    .withRecurrence(RecurrenceType.WEEKLY)
                    .withRecurrencePeriod(
                        new ExportRecurrencePeriod()
                            .withFrom(OffsetDateTime.parse("2020-06-01T00:00:00Z"))
                            .withTo(OffsetDateTime.parse("2020-10-31T00:00:00Z"))))
            .withFormat(FormatType.CSV)
            .withDeliveryInfo(
                new ExportDeliveryInfo()
                    .withDestination(
                        new ExportDeliveryDestination()
                            .withResourceId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/MYDEVTESTRG/providers/Microsoft.Storage/storageAccounts/ccmeastusdiag182")
                            .withContainer("exports")
                            .withRootFolderPath("ad-hoc")))
            .withDefinition(
                new ExportDefinition()
                    .withType(ExportType.ACTUAL_COST)
                    .withTimeframe(TimeframeType.MONTH_TO_DATE)
                    .withDataSet(
                        new ExportDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withConfiguration(
                                new ExportDatasetConfiguration()
                                    .withColumns(
                                        Arrays
                                            .asList("Date", "MeterId", "ResourceId", "ResourceLocation", "Quantity")))))
            .create();
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportCreateOrUpdateByBillingAccount.json
     */
    /**
     * Sample code: ExportCreateOrUpdateByBillingAccount.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportCreateOrUpdateByBillingAccount(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .define("TestExport")
            .withExistingScope("providers/Microsoft.Billing/billingAccounts/123456")
            .withSchedule(
                new ExportSchedule()
                    .withStatus(StatusType.ACTIVE)
                    .withRecurrence(RecurrenceType.WEEKLY)
                    .withRecurrencePeriod(
                        new ExportRecurrencePeriod()
                            .withFrom(OffsetDateTime.parse("2020-06-01T00:00:00Z"))
                            .withTo(OffsetDateTime.parse("2020-10-31T00:00:00Z"))))
            .withFormat(FormatType.CSV)
            .withDeliveryInfo(
                new ExportDeliveryInfo()
                    .withDestination(
                        new ExportDeliveryDestination()
                            .withResourceId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/MYDEVTESTRG/providers/Microsoft.Storage/storageAccounts/ccmeastusdiag182")
                            .withContainer("exports")
                            .withRootFolderPath("ad-hoc")))
            .withDefinition(
                new ExportDefinition()
                    .withType(ExportType.ACTUAL_COST)
                    .withTimeframe(TimeframeType.MONTH_TO_DATE)
                    .withDataSet(
                        new ExportDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withConfiguration(
                                new ExportDatasetConfiguration()
                                    .withColumns(
                                        Arrays
                                            .asList("Date", "MeterId", "ResourceId", "ResourceLocation", "Quantity")))))
            .create();
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportCreateOrUpdateByDepartment.json
     */
    /**
     * Sample code: ExportCreateOrUpdateByDepartment.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportCreateOrUpdateByDepartment(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .define("TestExport")
            .withExistingScope("providers/Microsoft.Billing/billingAccounts/12/departments/1234")
            .withSchedule(
                new ExportSchedule()
                    .withStatus(StatusType.ACTIVE)
                    .withRecurrence(RecurrenceType.WEEKLY)
                    .withRecurrencePeriod(
                        new ExportRecurrencePeriod()
                            .withFrom(OffsetDateTime.parse("2020-06-01T00:00:00Z"))
                            .withTo(OffsetDateTime.parse("2020-10-31T00:00:00Z"))))
            .withFormat(FormatType.CSV)
            .withDeliveryInfo(
                new ExportDeliveryInfo()
                    .withDestination(
                        new ExportDeliveryDestination()
                            .withResourceId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/MYDEVTESTRG/providers/Microsoft.Storage/storageAccounts/ccmeastusdiag182")
                            .withContainer("exports")
                            .withRootFolderPath("ad-hoc")))
            .withDefinition(
                new ExportDefinition()
                    .withType(ExportType.ACTUAL_COST)
                    .withTimeframe(TimeframeType.MONTH_TO_DATE)
                    .withDataSet(
                        new ExportDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withConfiguration(
                                new ExportDatasetConfiguration()
                                    .withColumns(
                                        Arrays
                                            .asList("Date", "MeterId", "ResourceId", "ResourceLocation", "Quantity")))))
            .create();
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportCreateOrUpdateByResourceGroup.json
     */
    /**
     * Sample code: ExportCreateOrUpdateByResourceGroup.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportCreateOrUpdateByResourceGroup(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .define("TestExport")
            .withExistingScope("subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/MYDEVTESTRG")
            .withSchedule(
                new ExportSchedule()
                    .withStatus(StatusType.ACTIVE)
                    .withRecurrence(RecurrenceType.WEEKLY)
                    .withRecurrencePeriod(
                        new ExportRecurrencePeriod()
                            .withFrom(OffsetDateTime.parse("2020-06-01T00:00:00Z"))
                            .withTo(OffsetDateTime.parse("2020-10-31T00:00:00Z"))))
            .withFormat(FormatType.CSV)
            .withDeliveryInfo(
                new ExportDeliveryInfo()
                    .withDestination(
                        new ExportDeliveryDestination()
                            .withResourceId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/MYDEVTESTRG/providers/Microsoft.Storage/storageAccounts/ccmeastusdiag182")
                            .withContainer("exports")
                            .withRootFolderPath("ad-hoc")))
            .withDefinition(
                new ExportDefinition()
                    .withType(ExportType.ACTUAL_COST)
                    .withTimeframe(TimeframeType.MONTH_TO_DATE)
                    .withDataSet(
                        new ExportDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withConfiguration(
                                new ExportDatasetConfiguration()
                                    .withColumns(
                                        Arrays
                                            .asList("Date", "MeterId", "ResourceId", "ResourceLocation", "Quantity")))))
            .create();
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportCreateOrUpdateBySubscription.json
     */
    /**
     * Sample code: ExportCreateOrUpdateBySubscription.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportCreateOrUpdateBySubscription(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .define("TestExport")
            .withExistingScope("subscriptions/00000000-0000-0000-0000-000000000000")
            .withSchedule(
                new ExportSchedule()
                    .withStatus(StatusType.ACTIVE)
                    .withRecurrence(RecurrenceType.WEEKLY)
                    .withRecurrencePeriod(
                        new ExportRecurrencePeriod()
                            .withFrom(OffsetDateTime.parse("2020-06-01T00:00:00Z"))
                            .withTo(OffsetDateTime.parse("2020-10-31T00:00:00Z"))))
            .withFormat(FormatType.CSV)
            .withDeliveryInfo(
                new ExportDeliveryInfo()
                    .withDestination(
                        new ExportDeliveryDestination()
                            .withResourceId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/MYDEVTESTRG/providers/Microsoft.Storage/storageAccounts/ccmeastusdiag182")
                            .withContainer("exports")
                            .withRootFolderPath("ad-hoc")))
            .withDefinition(
                new ExportDefinition()
                    .withType(ExportType.ACTUAL_COST)
                    .withTimeframe(TimeframeType.MONTH_TO_DATE)
                    .withDataSet(
                        new ExportDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withConfiguration(
                                new ExportDatasetConfiguration()
                                    .withColumns(
                                        Arrays
                                            .asList("Date", "MeterId", "ResourceId", "ResourceLocation", "Quantity")))))
            .create();
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportCreateOrUpdateByEnrollmentAccount.json
     */
    /**
     * Sample code: ExportCreateOrUpdateByEnrollmentAccount.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportCreateOrUpdateByEnrollmentAccount(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .define("TestExport")
            .withExistingScope("providers/Microsoft.Billing/billingAccounts/100/enrollmentAccounts/456")
            .withSchedule(
                new ExportSchedule()
                    .withStatus(StatusType.ACTIVE)
                    .withRecurrence(RecurrenceType.WEEKLY)
                    .withRecurrencePeriod(
                        new ExportRecurrencePeriod()
                            .withFrom(OffsetDateTime.parse("2020-06-01T00:00:00Z"))
                            .withTo(OffsetDateTime.parse("2020-10-31T00:00:00Z"))))
            .withFormat(FormatType.CSV)
            .withDeliveryInfo(
                new ExportDeliveryInfo()
                    .withDestination(
                        new ExportDeliveryDestination()
                            .withResourceId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/MYDEVTESTRG/providers/Microsoft.Storage/storageAccounts/ccmeastusdiag182")
                            .withContainer("exports")
                            .withRootFolderPath("ad-hoc")))
            .withDefinition(
                new ExportDefinition()
                    .withType(ExportType.ACTUAL_COST)
                    .withTimeframe(TimeframeType.MONTH_TO_DATE)
                    .withDataSet(
                        new ExportDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withConfiguration(
                                new ExportDatasetConfiguration()
                                    .withColumns(
                                        Arrays
                                            .asList("Date", "MeterId", "ResourceId", "ResourceLocation", "Quantity")))))
            .create();
    }
}
```

### Exports_Delete

```java
/** Samples for Exports Delete. */
public final class ExportsDeleteSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportDeleteByBillingAccount.json
     */
    /**
     * Sample code: ExportDeleteByBillingAccount.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportDeleteByBillingAccount(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .deleteByResourceGroupWithResponse(
                "providers/Microsoft.Billing/billingAccounts/123456", "TestExport", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportDeleteByManagementGroup.json
     */
    /**
     * Sample code: ExportDeleteByManagementGroup.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportDeleteByManagementGroup(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .deleteByResourceGroupWithResponse(
                "providers/Microsoft.Management/managementGroups/TestMG",
                "TestExport",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportDeleteByResourceGroup.json
     */
    /**
     * Sample code: ExportDeleteByResourceGroup.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportDeleteByResourceGroup(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .deleteByResourceGroupWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/MYDEVTESTRG",
                "TestExport",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportDeleteByDepartment.json
     */
    /**
     * Sample code: ExportDeleteByDepartment.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportDeleteByDepartment(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .deleteByResourceGroupWithResponse(
                "providers/Microsoft.Billing/billingAccounts/12/departments/1234",
                "TestExport",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportDeleteBySubscription.json
     */
    /**
     * Sample code: ExportDeleteBySubscription.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportDeleteBySubscription(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .deleteByResourceGroupWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000", "TestExport", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportDeleteByEnrollmentAccount.json
     */
    /**
     * Sample code: ExportDeleteByEnrollmentAccount.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportDeleteByEnrollmentAccount(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .deleteByResourceGroupWithResponse(
                "providers/Microsoft.Billing/billingAccounts/100/enrollmentAccounts/456",
                "TestExport",
                com.azure.core.util.Context.NONE);
    }
}
```

### Exports_Execute

```java
/** Samples for Exports Execute. */
public final class ExportsExecuteSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportRunByManagementGroup.json
     */
    /**
     * Sample code: ExportRunByManagementGroup.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportRunByManagementGroup(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .executeWithResponse(
                "providers/Microsoft.Management/managementGroups/TestMG",
                "TestExport",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportRunByBillingAccount.json
     */
    /**
     * Sample code: ExportRunByBillingAccount.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportRunByBillingAccount(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .executeWithResponse(
                "providers/Microsoft.Billing/billingAccounts/123456", "TestExport", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportRunBySubscription.json
     */
    /**
     * Sample code: ExportRunBySubscription.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportRunBySubscription(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .executeWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000", "TestExport", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportRunByDepartment.json
     */
    /**
     * Sample code: ExportRunByDepartment.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportRunByDepartment(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .executeWithResponse(
                "providers/Microsoft.Billing/billingAccounts/12/departments/1234",
                "TestExport",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportRunByResourceGroup.json
     */
    /**
     * Sample code: ExportRunByResourceGroup.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportRunByResourceGroup(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .executeWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/MYDEVTESTRG",
                "TestExport",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportRunByEnrollmentAccount.json
     */
    /**
     * Sample code: ExportRunByEnrollmentAccount.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportRunByEnrollmentAccount(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .executeWithResponse(
                "providers/Microsoft.Billing/billingAccounts/100/enrollmentAccounts/456",
                "TestExport",
                com.azure.core.util.Context.NONE);
    }
}
```

### Exports_Get

```java
/** Samples for Exports Get. */
public final class ExportsGetSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportGetByEnrollmentAccount.json
     */
    /**
     * Sample code: ExportGetByEnrollmentAccount.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportGetByEnrollmentAccount(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .getWithResponse(
                "providers/Microsoft.Billing/billingAccounts/100/enrollmentAccounts/456",
                "TestExport",
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportGetByManagementGroup.json
     */
    /**
     * Sample code: ExportGetByManagementGroup.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportGetByManagementGroup(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .getWithResponse(
                "providers/Microsoft.Management/managementGroups/TestMG",
                "TestExport",
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportGetByDepartment.json
     */
    /**
     * Sample code: ExportGetByDepartment.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportGetByDepartment(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .getWithResponse(
                "providers/Microsoft.Billing/billingAccounts/12/departments/1234",
                "TestExport",
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportGetBySubscription.json
     */
    /**
     * Sample code: ExportGetBySubscription.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportGetBySubscription(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .getWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000",
                "TestExport",
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportGetByBillingAccount.json
     */
    /**
     * Sample code: ExportGetByBillingAccount.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportGetByBillingAccount(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .getWithResponse(
                "providers/Microsoft.Billing/billingAccounts/123456",
                "TestExport",
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportGetByResourceGroup.json
     */
    /**
     * Sample code: ExportGetByResourceGroup.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportGetByResourceGroup(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .getWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/MYDEVTESTRG",
                "TestExport",
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Exports_GetExecutionHistory

```java
/** Samples for Exports GetExecutionHistory. */
public final class ExportsGetExecutionHistorySamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportRunHistoryGetByManagementGroup.json
     */
    /**
     * Sample code: ExportRunHistoryGetByManagementGroup.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportRunHistoryGetByManagementGroup(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .getExecutionHistoryWithResponse(
                "providers/Microsoft.Management/managementGroups/TestMG",
                "TestExport",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportRunHistoryGetBySubscription.json
     */
    /**
     * Sample code: ExportRunHistoryGetBySubscription.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportRunHistoryGetBySubscription(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .getExecutionHistoryWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000", "TestExport", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportRunHistoryGetByResourceGroup.json
     */
    /**
     * Sample code: ExportRunHistoryGetByResourceGroup.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportRunHistoryGetByResourceGroup(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .getExecutionHistoryWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/MYDEVTESTRG",
                "TestExport",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportRunHistoryGetByEnrollmentAccount.json
     */
    /**
     * Sample code: ExportRunHistoryGetByEnrollmentAccount.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportRunHistoryGetByEnrollmentAccount(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .getExecutionHistoryWithResponse(
                "providers/Microsoft.Billing/billingAccounts/100/enrollmentAccounts/456",
                "TestExport",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportRunHistoryGetByBillingAccount.json
     */
    /**
     * Sample code: ExportRunHistoryGetByBillingAccount.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportRunHistoryGetByBillingAccount(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .getExecutionHistoryWithResponse(
                "providers/Microsoft.Billing/billingAccounts/123456", "TestExport", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportRunHistoryGetByDepartment.json
     */
    /**
     * Sample code: ExportRunHistoryGetByDepartment.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportRunHistoryGetByDepartment(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .getExecutionHistoryWithResponse(
                "providers/Microsoft.Billing/billingAccounts/12/departments/1234",
                "TestExport",
                com.azure.core.util.Context.NONE);
    }
}
```

### Exports_List

```java
/** Samples for Exports List. */
public final class ExportsListSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportsGetByResourceGroup.json
     */
    /**
     * Sample code: ExportsGetByResourceGroup.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportsGetByResourceGroup(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .listWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/MYDEVTESTRG",
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportsGetByDepartment.json
     */
    /**
     * Sample code: ExportsGetByDepartment.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportsGetByDepartment(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .listWithResponse(
                "providers/Microsoft.Billing/billingAccounts/12/departments/123",
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportsGetByBillingAccount.json
     */
    /**
     * Sample code: ExportsGetByBillingAccount.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportsGetByBillingAccount(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .listWithResponse(
                "providers/Microsoft.Billing/billingAccounts/123456", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportsGetByEnrollmentAccount.json
     */
    /**
     * Sample code: ExportsGetByEnrollmentAccount.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportsGetByEnrollmentAccount(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .listWithResponse(
                "providers/Microsoft.Billing/billingAccounts/100/enrollmentAccounts/456",
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportsGetByManagementGroup.json
     */
    /**
     * Sample code: ExportsGetByManagementGroup.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportsGetByManagementGroup(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .listWithResponse(
                "providers/Microsoft.Management/managementGroups/TestMG", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExportsGetBySubscription.json
     */
    /**
     * Sample code: ExportsGetBySubscription.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void exportsGetBySubscription(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .exports()
            .listWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000", null, com.azure.core.util.Context.NONE);
    }
}
```

### Forecast_ExternalCloudProviderUsage

```java
import com.azure.resourcemanager.costmanagement.models.ExternalCloudProviderType;
import com.azure.resourcemanager.costmanagement.models.ForecastDataset;
import com.azure.resourcemanager.costmanagement.models.ForecastDefinition;
import com.azure.resourcemanager.costmanagement.models.ForecastTimeframeType;
import com.azure.resourcemanager.costmanagement.models.ForecastType;
import com.azure.resourcemanager.costmanagement.models.GranularityType;
import com.azure.resourcemanager.costmanagement.models.QueryComparisonExpression;
import com.azure.resourcemanager.costmanagement.models.QueryFilter;
import com.azure.resourcemanager.costmanagement.models.QueryOperatorType;
import java.util.Arrays;

/** Samples for Forecast ExternalCloudProviderUsage. */
public final class ForecastExternalCloudProviderUsageSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExternalSubscriptionForecast.json
     */
    /**
     * Sample code: ExternalSubscriptionForecast.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void externalSubscriptionForecast(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .forecasts()
            .externalCloudProviderUsageWithResponse(
                ExternalCloudProviderType.EXTERNAL_SUBSCRIPTIONS,
                "100",
                new ForecastDefinition()
                    .withType(ForecastType.USAGE)
                    .withTimeframe(ForecastTimeframeType.MONTH_TO_DATE)
                    .withDataset(
                        new ForecastDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withFilter(
                                new QueryFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new QueryFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new QueryFilter()
                                                                    .withDimension(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTag(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimension(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExternalBillingAccountForecast.json
     */
    /**
     * Sample code: ExternalBillingAccountForecast.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void externalBillingAccountForecast(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .forecasts()
            .externalCloudProviderUsageWithResponse(
                ExternalCloudProviderType.EXTERNAL_BILLING_ACCOUNTS,
                "100",
                new ForecastDefinition()
                    .withType(ForecastType.USAGE)
                    .withTimeframe(ForecastTimeframeType.MONTH_TO_DATE)
                    .withDataset(
                        new ForecastDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withFilter(
                                new QueryFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new QueryFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new QueryFilter()
                                                                    .withDimension(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTag(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimension(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Forecast_Usage

```java
import com.azure.resourcemanager.costmanagement.models.ForecastDataset;
import com.azure.resourcemanager.costmanagement.models.ForecastDefinition;
import com.azure.resourcemanager.costmanagement.models.ForecastTimeframeType;
import com.azure.resourcemanager.costmanagement.models.ForecastType;
import com.azure.resourcemanager.costmanagement.models.GranularityType;
import com.azure.resourcemanager.costmanagement.models.QueryComparisonExpression;
import com.azure.resourcemanager.costmanagement.models.QueryFilter;
import com.azure.resourcemanager.costmanagement.models.QueryOperatorType;
import java.util.Arrays;

/** Samples for Forecast Usage. */
public final class ForecastUsageSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ResourceGroupForecast.json
     */
    /**
     * Sample code: ResourceGroupForecast.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void resourceGroupForecast(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .forecasts()
            .usageWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/ScreenSharingTest-peer",
                new ForecastDefinition()
                    .withType(ForecastType.USAGE)
                    .withTimeframe(ForecastTimeframeType.MONTH_TO_DATE)
                    .withDataset(
                        new ForecastDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withFilter(
                                new QueryFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new QueryFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new QueryFilter()
                                                                    .withDimension(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTag(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimension(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API")))))))
                    .withIncludeActualCost(false)
                    .withIncludeFreshPartialCost(false),
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/EnrollmentAccountForecast.json
     */
    /**
     * Sample code: EnrollmentAccountForecast.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void enrollmentAccountForecast(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .forecasts()
            .usageWithResponse(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/enrollmentAccounts/456",
                new ForecastDefinition()
                    .withType(ForecastType.USAGE)
                    .withTimeframe(ForecastTimeframeType.MONTH_TO_DATE)
                    .withDataset(
                        new ForecastDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withFilter(
                                new QueryFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new QueryFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new QueryFilter()
                                                                    .withDimension(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTag(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimension(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API")))))))
                    .withIncludeActualCost(false)
                    .withIncludeFreshPartialCost(false),
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/SubscriptionForecast.json
     */
    /**
     * Sample code: SubscriptionForecast.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void subscriptionForecast(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .forecasts()
            .usageWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000",
                new ForecastDefinition()
                    .withType(ForecastType.USAGE)
                    .withTimeframe(ForecastTimeframeType.MONTH_TO_DATE)
                    .withDataset(
                        new ForecastDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withFilter(
                                new QueryFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new QueryFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new QueryFilter()
                                                                    .withDimension(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTag(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimension(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API")))))))
                    .withIncludeActualCost(false)
                    .withIncludeFreshPartialCost(false),
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/BillingProfileForecast.json
     */
    /**
     * Sample code: BillingProfileForecast.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingProfileForecast(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .forecasts()
            .usageWithResponse(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/billingProfiles/13579",
                new ForecastDefinition()
                    .withType(ForecastType.USAGE)
                    .withTimeframe(ForecastTimeframeType.MONTH_TO_DATE)
                    .withDataset(
                        new ForecastDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withFilter(
                                new QueryFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new QueryFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new QueryFilter()
                                                                    .withDimension(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTag(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimension(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API")))))))
                    .withIncludeActualCost(false)
                    .withIncludeFreshPartialCost(false),
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/DepartmentForecast.json
     */
    /**
     * Sample code: DepartmentForecast.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void departmentForecast(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .forecasts()
            .usageWithResponse(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/departments/123",
                new ForecastDefinition()
                    .withType(ForecastType.USAGE)
                    .withTimeframe(ForecastTimeframeType.MONTH_TO_DATE)
                    .withDataset(
                        new ForecastDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withFilter(
                                new QueryFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new QueryFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new QueryFilter()
                                                                    .withDimension(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTag(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimension(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API")))))))
                    .withIncludeActualCost(false)
                    .withIncludeFreshPartialCost(false),
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/BillingAccountForecast.json
     */
    /**
     * Sample code: BillingAccountForecast.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingAccountForecast(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .forecasts()
            .usageWithResponse(
                "providers/Microsoft.Billing/billingAccounts/12345:6789",
                new ForecastDefinition()
                    .withType(ForecastType.USAGE)
                    .withTimeframe(ForecastTimeframeType.MONTH_TO_DATE)
                    .withDataset(
                        new ForecastDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withFilter(
                                new QueryFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new QueryFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new QueryFilter()
                                                                    .withDimension(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTag(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimension(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API")))))))
                    .withIncludeActualCost(false)
                    .withIncludeFreshPartialCost(false),
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/InvoiceSectionForecast.json
     */
    /**
     * Sample code: InvoiceSectionForecast.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void invoiceSectionForecast(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .forecasts()
            .usageWithResponse(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/billingProfiles/13579/invoiceSections/9876",
                new ForecastDefinition()
                    .withType(ForecastType.USAGE)
                    .withTimeframe(ForecastTimeframeType.MONTH_TO_DATE)
                    .withDataset(
                        new ForecastDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withFilter(
                                new QueryFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new QueryFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new QueryFilter()
                                                                    .withDimension(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTag(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimension(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API")))))))
                    .withIncludeActualCost(false)
                    .withIncludeFreshPartialCost(false),
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Query_Usage

```java
import com.azure.resourcemanager.costmanagement.models.ExportType;
import com.azure.resourcemanager.costmanagement.models.FunctionType;
import com.azure.resourcemanager.costmanagement.models.GranularityType;
import com.azure.resourcemanager.costmanagement.models.QueryAggregation;
import com.azure.resourcemanager.costmanagement.models.QueryColumnType;
import com.azure.resourcemanager.costmanagement.models.QueryComparisonExpression;
import com.azure.resourcemanager.costmanagement.models.QueryDataset;
import com.azure.resourcemanager.costmanagement.models.QueryDefinition;
import com.azure.resourcemanager.costmanagement.models.QueryFilter;
import com.azure.resourcemanager.costmanagement.models.QueryGrouping;
import com.azure.resourcemanager.costmanagement.models.QueryOperatorType;
import com.azure.resourcemanager.costmanagement.models.TimeframeType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Query Usage. */
public final class QueryUsageSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/MCACustomerQueryGrouping.json
     */
    /**
     * Sample code: CustomerQueryGrouping-Modern.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void customerQueryGroupingModern(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .queries()
            .usageWithResponse(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/customers/5678",
                new QueryDefinition()
                    .withType(ExportType.USAGE)
                    .withTimeframe(TimeframeType.THE_LAST_MONTH)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.fromString("None"))
                            .withAggregation(
                                mapOf(
                                    "totalCost",
                                    new QueryAggregation().withName("PreTaxCost").withFunction(FunctionType.SUM)))
                            .withGrouping(
                                Arrays
                                    .asList(
                                        new QueryGrouping()
                                            .withType(QueryColumnType.DIMENSION)
                                            .withName("ResourceGroup")))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/MCAInvoiceSectionQuery.json
     */
    /**
     * Sample code: InvoiceSectionQuery-Modern.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void invoiceSectionQueryModern(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .queries()
            .usageWithResponse(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/billingProfiles/13579/invoiceSections/9876",
                new QueryDefinition()
                    .withType(ExportType.USAGE)
                    .withTimeframe(TimeframeType.MONTH_TO_DATE)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withFilter(
                                new QueryFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new QueryFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new QueryFilter()
                                                                    .withDimension(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTag(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimension(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/SubscriptionQueryGrouping.json
     */
    /**
     * Sample code: SubscriptionQueryGrouping-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void subscriptionQueryGroupingLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .queries()
            .usageWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000",
                new QueryDefinition()
                    .withType(ExportType.USAGE)
                    .withTimeframe(TimeframeType.THE_LAST_MONTH)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.fromString("None"))
                            .withAggregation(
                                mapOf(
                                    "totalCost",
                                    new QueryAggregation().withName("PreTaxCost").withFunction(FunctionType.SUM)))
                            .withGrouping(
                                Arrays
                                    .asList(
                                        new QueryGrouping()
                                            .withType(QueryColumnType.DIMENSION)
                                            .withName("ResourceGroup")))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/MCABillingAccountQueryGrouping.json
     */
    /**
     * Sample code: BillingAccountQueryGrouping-Modern.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingAccountQueryGroupingModern(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .queries()
            .usageWithResponse(
                "providers/Microsoft.Billing/billingAccounts/12345:6789",
                new QueryDefinition()
                    .withType(ExportType.USAGE)
                    .withTimeframe(TimeframeType.THE_LAST_MONTH)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.fromString("None"))
                            .withAggregation(
                                mapOf(
                                    "totalCost",
                                    new QueryAggregation().withName("PreTaxCost").withFunction(FunctionType.SUM)))
                            .withGrouping(
                                Arrays
                                    .asList(
                                        new QueryGrouping()
                                            .withType(QueryColumnType.DIMENSION)
                                            .withName("ResourceGroup")))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/EnrollmentAccountQueryGrouping.json
     */
    /**
     * Sample code: EnrollmentAccountQueryGrouping-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void enrollmentAccountQueryGroupingLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .queries()
            .usageWithResponse(
                "providers/Microsoft.Billing/billingAccounts/100/enrollmentAccounts/456",
                new QueryDefinition()
                    .withType(ExportType.USAGE)
                    .withTimeframe(TimeframeType.THE_LAST_MONTH)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withAggregation(
                                mapOf(
                                    "totalCost",
                                    new QueryAggregation().withName("PreTaxCost").withFunction(FunctionType.SUM)))
                            .withGrouping(
                                Arrays
                                    .asList(
                                        new QueryGrouping()
                                            .withType(QueryColumnType.DIMENSION)
                                            .withName("ResourceGroup")))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/MCACustomerQuery.json
     */
    /**
     * Sample code: CustomerQuery-Modern.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void customerQueryModern(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .queries()
            .usageWithResponse(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/customers/5678",
                new QueryDefinition()
                    .withType(ExportType.USAGE)
                    .withTimeframe(TimeframeType.MONTH_TO_DATE)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withFilter(
                                new QueryFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new QueryFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new QueryFilter()
                                                                    .withDimension(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTag(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimension(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/MCABillingAccountQuery.json
     */
    /**
     * Sample code: BillingAccountQuery-Modern.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingAccountQueryModern(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .queries()
            .usageWithResponse(
                "providers/Microsoft.Billing/billingAccounts/12345:6789",
                new QueryDefinition()
                    .withType(ExportType.USAGE)
                    .withTimeframe(TimeframeType.MONTH_TO_DATE)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withFilter(
                                new QueryFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new QueryFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new QueryFilter()
                                                                    .withDimension(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTag(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimension(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/DepartmentQuery.json
     */
    /**
     * Sample code: DepartmentQuery-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void departmentQueryLegacy(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .queries()
            .usageWithResponse(
                "providers/Microsoft.Billing/billingAccounts/100/departments/123",
                new QueryDefinition()
                    .withType(ExportType.USAGE)
                    .withTimeframe(TimeframeType.MONTH_TO_DATE)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withFilter(
                                new QueryFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new QueryFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new QueryFilter()
                                                                    .withDimension(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTag(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimension(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/BillingAccountQueryGrouping.json
     */
    /**
     * Sample code: BillingAccountQueryGrouping-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingAccountQueryGroupingLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .queries()
            .usageWithResponse(
                "providers/Microsoft.Billing/billingAccounts/70664866",
                new QueryDefinition()
                    .withType(ExportType.USAGE)
                    .withTimeframe(TimeframeType.THE_LAST_MONTH)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.fromString("None"))
                            .withAggregation(
                                mapOf(
                                    "totalCost",
                                    new QueryAggregation().withName("PreTaxCost").withFunction(FunctionType.SUM)))
                            .withGrouping(
                                Arrays
                                    .asList(
                                        new QueryGrouping()
                                            .withType(QueryColumnType.DIMENSION)
                                            .withName("ResourceGroup")))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/SubscriptionQuery.json
     */
    /**
     * Sample code: SubscriptionQuery-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void subscriptionQueryLegacy(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .queries()
            .usageWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000",
                new QueryDefinition()
                    .withType(ExportType.USAGE)
                    .withTimeframe(TimeframeType.MONTH_TO_DATE)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withFilter(
                                new QueryFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new QueryFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new QueryFilter()
                                                                    .withDimension(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTag(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimension(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/EnrollmentAccountQuery.json
     */
    /**
     * Sample code: EnrollmentAccountQuery-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void enrollmentAccountQueryLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .queries()
            .usageWithResponse(
                "providers/Microsoft.Billing/billingAccounts/100/enrollmentAccounts/456",
                new QueryDefinition()
                    .withType(ExportType.USAGE)
                    .withTimeframe(TimeframeType.MONTH_TO_DATE)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withFilter(
                                new QueryFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new QueryFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new QueryFilter()
                                                                    .withDimension(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTag(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimension(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/MCABillingProfileQuery.json
     */
    /**
     * Sample code: BillingProfileQuery-Modern.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingProfileQueryModern(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .queries()
            .usageWithResponse(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/billingProfiles/13579",
                new QueryDefinition()
                    .withType(ExportType.USAGE)
                    .withTimeframe(TimeframeType.MONTH_TO_DATE)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withFilter(
                                new QueryFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new QueryFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new QueryFilter()
                                                                    .withDimension(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTag(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimension(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ResourceGroupQueryGrouping.json
     */
    /**
     * Sample code: ResourceGroupQueryGrouping-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void resourceGroupQueryGroupingLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .queries()
            .usageWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/ScreenSharingTest-peer",
                new QueryDefinition()
                    .withType(ExportType.USAGE)
                    .withTimeframe(TimeframeType.THE_LAST_MONTH)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withAggregation(
                                mapOf(
                                    "totalCost",
                                    new QueryAggregation().withName("PreTaxCost").withFunction(FunctionType.SUM)))
                            .withGrouping(
                                Arrays
                                    .asList(
                                        new QueryGrouping()
                                            .withType(QueryColumnType.DIMENSION)
                                            .withName("ResourceType")))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/BillingAccountQuery.json
     */
    /**
     * Sample code: BillingAccountQuery-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingAccountQueryLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .queries()
            .usageWithResponse(
                "providers/Microsoft.Billing/billingAccounts/70664866",
                new QueryDefinition()
                    .withType(ExportType.USAGE)
                    .withTimeframe(TimeframeType.MONTH_TO_DATE)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withFilter(
                                new QueryFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new QueryFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new QueryFilter()
                                                                    .withDimension(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTag(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimension(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ResourceGroupQuery.json
     */
    /**
     * Sample code: ResourceGroupQuery-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void resourceGroupQueryLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .queries()
            .usageWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/ScreenSharingTest-peer",
                new QueryDefinition()
                    .withType(ExportType.USAGE)
                    .withTimeframe(TimeframeType.MONTH_TO_DATE)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withFilter(
                                new QueryFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new QueryFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new QueryFilter()
                                                                    .withDimension(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTag(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimension(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ManagementGroupQuery.json
     */
    /**
     * Sample code: ManagementGroupQuery-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void managementGroupQueryLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .queries()
            .usageWithResponse(
                "providers/Microsoft.Management/managementGroups/MyMgId",
                new QueryDefinition()
                    .withType(ExportType.USAGE)
                    .withTimeframe(TimeframeType.MONTH_TO_DATE)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withFilter(
                                new QueryFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new QueryFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new QueryFilter()
                                                                    .withDimension(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTag(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimension(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/DepartmentQueryGrouping.json
     */
    /**
     * Sample code: DepartmentQueryGrouping-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void departmentQueryGroupingLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .queries()
            .usageWithResponse(
                "providers/Microsoft.Billing/billingAccounts/100/departments/123",
                new QueryDefinition()
                    .withType(ExportType.USAGE)
                    .withTimeframe(TimeframeType.THE_LAST_MONTH)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.fromString("None"))
                            .withAggregation(
                                mapOf(
                                    "totalCost",
                                    new QueryAggregation().withName("PreTaxCost").withFunction(FunctionType.SUM)))
                            .withGrouping(
                                Arrays
                                    .asList(
                                        new QueryGrouping()
                                            .withType(QueryColumnType.DIMENSION)
                                            .withName("ResourceGroup")))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/MCAInvoiceSectionQueryGrouping.json
     */
    /**
     * Sample code: InvoiceSectionQueryGrouping-Modern.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void invoiceSectionQueryGroupingModern(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .queries()
            .usageWithResponse(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/billingProfiles/13579/invoiceSections/9876",
                new QueryDefinition()
                    .withType(ExportType.USAGE)
                    .withTimeframe(TimeframeType.THE_LAST_MONTH)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.fromString("None"))
                            .withAggregation(
                                mapOf(
                                    "totalCost",
                                    new QueryAggregation().withName("PreTaxCost").withFunction(FunctionType.SUM)))
                            .withGrouping(
                                Arrays
                                    .asList(
                                        new QueryGrouping()
                                            .withType(QueryColumnType.DIMENSION)
                                            .withName("ResourceGroup")))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/MCABillingProfileQueryGrouping.json
     */
    /**
     * Sample code: BillingProfileQueryGrouping-Modern.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingProfileQueryGroupingModern(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .queries()
            .usageWithResponse(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/billingProfiles/13579",
                new QueryDefinition()
                    .withType(ExportType.USAGE)
                    .withTimeframe(TimeframeType.THE_LAST_MONTH)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.fromString("None"))
                            .withAggregation(
                                mapOf(
                                    "totalCost",
                                    new QueryAggregation().withName("PreTaxCost").withFunction(FunctionType.SUM)))
                            .withGrouping(
                                Arrays
                                    .asList(
                                        new QueryGrouping()
                                            .withType(QueryColumnType.DIMENSION)
                                            .withName("ResourceGroup")))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ManagementGroupQueryGrouping.json
     */
    /**
     * Sample code: ManagementGroupQueryGrouping-Legacy.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void managementGroupQueryGroupingLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .queries()
            .usageWithResponse(
                "providers/Microsoft.Management/managementGroups/MyMgId",
                new QueryDefinition()
                    .withType(ExportType.USAGE)
                    .withTimeframe(TimeframeType.THE_LAST_MONTH)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.fromString("None"))
                            .withAggregation(
                                mapOf(
                                    "totalCost",
                                    new QueryAggregation().withName("PreTaxCost").withFunction(FunctionType.SUM)))
                            .withGrouping(
                                Arrays
                                    .asList(
                                        new QueryGrouping()
                                            .withType(QueryColumnType.DIMENSION)
                                            .withName("ResourceGroup")))),
                com.azure.core.util.Context.NONE);
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

### Query_UsageByExternalCloudProviderType

```java
import com.azure.resourcemanager.costmanagement.models.ExportType;
import com.azure.resourcemanager.costmanagement.models.ExternalCloudProviderType;
import com.azure.resourcemanager.costmanagement.models.GranularityType;
import com.azure.resourcemanager.costmanagement.models.QueryComparisonExpression;
import com.azure.resourcemanager.costmanagement.models.QueryDataset;
import com.azure.resourcemanager.costmanagement.models.QueryDefinition;
import com.azure.resourcemanager.costmanagement.models.QueryFilter;
import com.azure.resourcemanager.costmanagement.models.QueryOperatorType;
import com.azure.resourcemanager.costmanagement.models.TimeframeType;
import java.util.Arrays;

/** Samples for Query UsageByExternalCloudProviderType. */
public final class QueryUsageByExternalCloudProviderTypeSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExternalSubscriptionsQuery.json
     */
    /**
     * Sample code: ExternalSubscriptionsQuery.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void externalSubscriptionsQuery(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .queries()
            .usageByExternalCloudProviderTypeWithResponse(
                ExternalCloudProviderType.EXTERNAL_SUBSCRIPTIONS,
                "100",
                new QueryDefinition()
                    .withType(ExportType.USAGE)
                    .withTimeframe(TimeframeType.MONTH_TO_DATE)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withFilter(
                                new QueryFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new QueryFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new QueryFilter()
                                                                    .withDimension(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTag(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimension(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ExternalBillingAccountsQuery.json
     */
    /**
     * Sample code: ExternalBillingAccountQueryList.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void externalBillingAccountQueryList(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .queries()
            .usageByExternalCloudProviderTypeWithResponse(
                ExternalCloudProviderType.EXTERNAL_BILLING_ACCOUNTS,
                "100",
                new QueryDefinition()
                    .withType(ExportType.USAGE)
                    .withTimeframe(TimeframeType.MONTH_TO_DATE)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withFilter(
                                new QueryFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new QueryFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new QueryFilter()
                                                                    .withDimension(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTag(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimension(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Views_CreateOrUpdate

```java
import com.azure.resourcemanager.costmanagement.fluent.models.ViewInner;
import com.azure.resourcemanager.costmanagement.models.AccumulatedType;
import com.azure.resourcemanager.costmanagement.models.ChartType;
import com.azure.resourcemanager.costmanagement.models.FunctionType;
import com.azure.resourcemanager.costmanagement.models.KpiProperties;
import com.azure.resourcemanager.costmanagement.models.KpiType;
import com.azure.resourcemanager.costmanagement.models.MetricType;
import com.azure.resourcemanager.costmanagement.models.PivotProperties;
import com.azure.resourcemanager.costmanagement.models.PivotType;
import com.azure.resourcemanager.costmanagement.models.ReportConfigAggregation;
import com.azure.resourcemanager.costmanagement.models.ReportConfigDataset;
import com.azure.resourcemanager.costmanagement.models.ReportConfigSorting;
import com.azure.resourcemanager.costmanagement.models.ReportConfigSortingDirection;
import com.azure.resourcemanager.costmanagement.models.ReportGranularityType;
import com.azure.resourcemanager.costmanagement.models.ReportTimeframeType;
import com.azure.resourcemanager.costmanagement.models.ReportType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Views CreateOrUpdate. */
public final class ViewsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/PrivateViewCreateOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdatePrivateView.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void createOrUpdatePrivateView(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .views()
            .createOrUpdateWithResponse(
                "swaggerExample",
                new ViewInner()
                    .withEtag("\"1d4ff9fe66f1d10\"")
                    .withDisplayName("swagger Example")
                    .withChart(ChartType.TABLE)
                    .withAccumulated(AccumulatedType.TRUE)
                    .withMetric(MetricType.ACTUAL_COST)
                    .withKpis(
                        Arrays
                            .asList(
                                new KpiProperties().withType(KpiType.FORECAST).withEnabled(true),
                                new KpiProperties()
                                    .withType(KpiType.BUDGET)
                                    .withId(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/MYDEVTESTRG/providers/Microsoft.Consumption/budgets/swaggerDemo")
                                    .withEnabled(true)))
                    .withPivots(
                        Arrays
                            .asList(
                                new PivotProperties().withType(PivotType.DIMENSION).withName("ServiceName"),
                                new PivotProperties().withType(PivotType.DIMENSION).withName("MeterCategory"),
                                new PivotProperties().withType(PivotType.TAG_KEY).withName("swaggerTagKey")))
                    .withTypePropertiesType(ReportType.USAGE)
                    .withTimeframe(ReportTimeframeType.MONTH_TO_DATE)
                    .withDataset(
                        new ReportConfigDataset()
                            .withGranularity(ReportGranularityType.DAILY)
                            .withAggregation(
                                mapOf(
                                    "totalCost",
                                    new ReportConfigAggregation()
                                        .withName("PreTaxCost")
                                        .withFunction(FunctionType.SUM)))
                            .withGrouping(Arrays.asList())
                            .withSorting(
                                Arrays
                                    .asList(
                                        new ReportConfigSorting()
                                            .withDirection(ReportConfigSortingDirection.ASCENDING)
                                            .withName("UsageDate")))),
                com.azure.core.util.Context.NONE);
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

### Views_CreateOrUpdateByScope

```java
import com.azure.resourcemanager.costmanagement.models.AccumulatedType;
import com.azure.resourcemanager.costmanagement.models.ChartType;
import com.azure.resourcemanager.costmanagement.models.FunctionType;
import com.azure.resourcemanager.costmanagement.models.KpiProperties;
import com.azure.resourcemanager.costmanagement.models.KpiType;
import com.azure.resourcemanager.costmanagement.models.MetricType;
import com.azure.resourcemanager.costmanagement.models.PivotProperties;
import com.azure.resourcemanager.costmanagement.models.PivotType;
import com.azure.resourcemanager.costmanagement.models.ReportConfigAggregation;
import com.azure.resourcemanager.costmanagement.models.ReportConfigDataset;
import com.azure.resourcemanager.costmanagement.models.ReportConfigSorting;
import com.azure.resourcemanager.costmanagement.models.ReportConfigSortingDirection;
import com.azure.resourcemanager.costmanagement.models.ReportGranularityType;
import com.azure.resourcemanager.costmanagement.models.ReportTimeframeType;
import com.azure.resourcemanager.costmanagement.models.ReportType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Views CreateOrUpdateByScope. */
public final class ViewsCreateOrUpdateByScopeSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ViewCreateOrUpdateByResourceGroup.json
     */
    /**
     * Sample code: ResourceGroupCreateOrUpdateView.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void resourceGroupCreateOrUpdateView(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .views()
            .define("swaggerExample")
            .withExistingScope("subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/MYDEVTESTRG")
            .withEtag("\"1d4ff9fe66f1d10\"")
            .withDisplayName("swagger Example")
            .withChart(ChartType.TABLE)
            .withAccumulated(AccumulatedType.TRUE)
            .withMetric(MetricType.ACTUAL_COST)
            .withKpis(
                Arrays
                    .asList(
                        new KpiProperties().withType(KpiType.FORECAST).withEnabled(true),
                        new KpiProperties()
                            .withType(KpiType.BUDGET)
                            .withId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/MYDEVTESTRG/providers/Microsoft.Consumption/budgets/swaggerDemo")
                            .withEnabled(true)))
            .withPivots(
                Arrays
                    .asList(
                        new PivotProperties().withType(PivotType.DIMENSION).withName("ServiceName"),
                        new PivotProperties().withType(PivotType.DIMENSION).withName("MeterCategory"),
                        new PivotProperties().withType(PivotType.TAG_KEY).withName("swaggerTagKey")))
            .withTypePropertiesType(ReportType.USAGE)
            .withTimeframe(ReportTimeframeType.MONTH_TO_DATE)
            .withDataset(
                new ReportConfigDataset()
                    .withGranularity(ReportGranularityType.DAILY)
                    .withAggregation(
                        mapOf(
                            "totalCost",
                            new ReportConfigAggregation().withName("PreTaxCost").withFunction(FunctionType.SUM)))
                    .withGrouping(Arrays.asList())
                    .withSorting(
                        Arrays
                            .asList(
                                new ReportConfigSorting()
                                    .withDirection(ReportConfigSortingDirection.ASCENDING)
                                    .withName("UsageDate"))))
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

### Views_Delete

```java
/** Samples for Views Delete. */
public final class ViewsDeleteSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/PrivateViewDelete.json
     */
    /**
     * Sample code: DeletePrivateView.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void deletePrivateView(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager.views().deleteWithResponse("TestView", com.azure.core.util.Context.NONE);
    }
}
```

### Views_DeleteByScope

```java
/** Samples for Views DeleteByScope. */
public final class ViewsDeleteByScopeSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ViewDeleteByResourceGroup.json
     */
    /**
     * Sample code: ResourceGroupDeleteView.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void resourceGroupDeleteView(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .views()
            .deleteByScopeWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/MYDEVTESTRG",
                "TestView",
                com.azure.core.util.Context.NONE);
    }
}
```

### Views_Get

```java
/** Samples for Views Get. */
public final class ViewsGetSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/PrivateView.json
     */
    /**
     * Sample code: PrivateView.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void privateView(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager.views().getWithResponse("swaggerExample", com.azure.core.util.Context.NONE);
    }
}
```

### Views_GetByScope

```java
/** Samples for Views GetByScope. */
public final class ViewsGetByScopeSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ViewByResourceGroup.json
     */
    /**
     * Sample code: ResourceGroupView.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void resourceGroupView(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .views()
            .getByScopeWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/MYDEVTESTRG",
                "swaggerExample",
                com.azure.core.util.Context.NONE);
    }
}
```

### Views_List

```java
/** Samples for Views List. */
public final class ViewsListSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/PrivateViewList.json
     */
    /**
     * Sample code: PrivateViewList.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void privateViewList(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager.views().list(com.azure.core.util.Context.NONE);
    }
}
```

### Views_ListByScope

```java
/** Samples for Views ListByScope. */
public final class ViewsListByScopeSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2020-06-01/examples/ViewListByResourceGroup.json
     */
    /**
     * Sample code: ResourceGroupViewList.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void resourceGroupViewList(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .views()
            .listByScope(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/MYDEVTESTRG",
                com.azure.core.util.Context.NONE);
    }
}
```

