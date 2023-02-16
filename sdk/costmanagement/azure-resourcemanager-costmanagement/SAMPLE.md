# Code snippets and samples


## Alerts

- [Dismiss](#alerts_dismiss)
- [Get](#alerts_get)
- [List](#alerts_list)
- [ListExternal](#alerts_listexternal)

## BenefitRecommendations

- [List](#benefitrecommendations_list)

## BenefitUtilizationSummaries

- [ListByBillingAccountId](#benefitutilizationsummaries_listbybillingaccountid)
- [ListByBillingProfileId](#benefitutilizationsummaries_listbybillingprofileid)
- [ListBySavingsPlanId](#benefitutilizationsummaries_listbysavingsplanid)
- [ListBySavingsPlanOrder](#benefitutilizationsummaries_listbysavingsplanorder)

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

## GenerateCostDetailsReport

- [CreateOperation](#generatecostdetailsreport_createoperation)
- [GetOperationResults](#generatecostdetailsreport_getoperationresults)

## GenerateDetailedCostReport

- [CreateOperation](#generatedetailedcostreport_createoperation)

## GenerateDetailedCostReportOperationResults

- [Get](#generatedetailedcostreportoperationresults_get)

## GenerateDetailedCostReportOperationStatus

- [Get](#generatedetailedcostreportoperationstatus_get)

## GenerateReservationDetailsReport

- [ByBillingAccountId](#generatereservationdetailsreport_bybillingaccountid)
- [ByBillingProfileId](#generatereservationdetailsreport_bybillingprofileid)

## Operations

- [List](#operations_list)

## PriceSheet

- [Download](#pricesheet_download)
- [DownloadByBillingProfile](#pricesheet_downloadbybillingprofile)

## Query

- [Usage](#query_usage)
- [UsageByExternalCloudProviderType](#query_usagebyexternalcloudprovidertype)

## ScheduledActions

- [CheckNameAvailability](#scheduledactions_checknameavailability)
- [CheckNameAvailabilityByScope](#scheduledactions_checknameavailabilitybyscope)
- [CreateOrUpdate](#scheduledactions_createorupdate)
- [CreateOrUpdateByScope](#scheduledactions_createorupdatebyscope)
- [Delete](#scheduledactions_delete)
- [DeleteByScope](#scheduledactions_deletebyscope)
- [Get](#scheduledactions_get)
- [GetByScope](#scheduledactions_getbyscope)
- [List](#scheduledactions_list)
- [ListByScope](#scheduledactions_listbyscope)
- [Run](#scheduledactions_run)
- [RunByScope](#scheduledactions_runbyscope)

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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/DismissResourceGroupAlerts.json
     */
    /**
     * Sample code: PatchResourceGroupAlerts.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void patchResourceGroupAlerts(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .alerts()
            .dismissWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/ScreenSharingTest-peer",
                "22222222-2222-2222-2222-222222222222",
                new DismissAlertPayload().withStatus(AlertStatus.DISMISSED),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/DismissSubscriptionAlerts.json
     */
    /**
     * Sample code: PatchSubscriptionAlerts.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void patchSubscriptionAlerts(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .alerts()
            .dismissWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000",
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/SingleSubscriptionAlert.json
     */
    /**
     * Sample code: SingleSubscriptionAlerts.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void singleSubscriptionAlerts(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .alerts()
            .getWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000",
                "22222222-2222-2222-2222-222222222222",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/SingleResourceGroupAlert.json
     */
    /**
     * Sample code: SingleResourceGroupAlerts.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void singleResourceGroupAlerts(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/SubscriptionAlerts.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/BillingProfileAlerts.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ResourceGroupAlerts.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/DepartmentAlerts.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/InvoiceSectionAlerts.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/BillingAccountAlerts.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/EnrollmentAccountAlerts.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExternalBillingAccountAlerts.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExternalSubscriptionAlerts.json
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

### BenefitRecommendations_List

```java
/** Samples for BenefitRecommendations List. */
public final class BenefitRecommendationsListSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/BenefitRecommendationsByBillingAccount.json
     */
    /**
     * Sample code: BenefitRecommendationsBillingAccountList.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void benefitRecommendationsBillingAccountList(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .benefitRecommendations()
            .list(
                "providers/Microsoft.Billing/billingAccounts/123456",
                "properties/lookBackPeriod eq 'Last7Days' AND properties/term eq 'P1Y'",
                null,
                "properties/usage,properties/allRecommendationDetails",
                com.azure.core.util.Context.NONE);
    }
}
```

### BenefitUtilizationSummaries_ListByBillingAccountId

```java
/** Samples for BenefitUtilizationSummaries ListByBillingAccountId. */
public final class BenefitUtilizationSummariesListByBillingAccountIdSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/BenefitUtilizationSummaries/SavingsPlan-BillingAccount.json
     */
    /**
     * Sample code: SavingsPlanUtilizationSummaries-BillingAccount.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void savingsPlanUtilizationSummariesBillingAccount(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .benefitUtilizationSummaries()
            .listByBillingAccountId(
                "12345",
                null,
                "properties/usageDate ge 2022-10-15 and properties/usageDate le 2022-10-18",
                com.azure.core.util.Context.NONE);
    }
}
```

### BenefitUtilizationSummaries_ListByBillingProfileId

```java
/** Samples for BenefitUtilizationSummaries ListByBillingProfileId. */
public final class BenefitUtilizationSummariesListByBillingProfileIdSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/BenefitUtilizationSummaries/SavingsPlan-BillingProfile.json
     */
    /**
     * Sample code: SavingsPlanUtilizationSummaries-BillingProfile.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void savingsPlanUtilizationSummariesBillingProfile(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .benefitUtilizationSummaries()
            .listByBillingProfileId(
                "c0a00000-0e04-5ee3-000e-f0c6e00000ec:c0a00000-0e04-5ee3-000e-f0c6e00000ec",
                "200e5e90-000e-4960-8dcd-8d00a02db000",
                null,
                "properties/usageDate ge 2022-10-15 and properties/usageDate le 2022-10-18",
                com.azure.core.util.Context.NONE);
    }
}
```

### BenefitUtilizationSummaries_ListBySavingsPlanId

```java
/** Samples for BenefitUtilizationSummaries ListBySavingsPlanId. */
public final class BenefitUtilizationSummariesListBySavingsPlanIdSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/BenefitUtilizationSummaries/SavingsPlan-SavingsPlanId-Monthly.json
     */
    /**
     * Sample code: SavingsPlanUtilizationSummariesMonthlyWithSavingsPlanId.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void savingsPlanUtilizationSummariesMonthlyWithSavingsPlanId(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .benefitUtilizationSummaries()
            .listBySavingsPlanId(
                "66cccc66-6ccc-6c66-666c-66cc6c6c66c6",
                "222d22dd-d2d2-2dd2-222d-2dd2222ddddd",
                null,
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### BenefitUtilizationSummaries_ListBySavingsPlanOrder

```java
/** Samples for BenefitUtilizationSummaries ListBySavingsPlanOrder. */
public final class BenefitUtilizationSummariesListBySavingsPlanOrderSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/BenefitUtilizationSummaries/SavingsPlan-SavingsPlanOrderId-Daily.json
     */
    /**
     * Sample code: SavingsPlanUtilizationSummariesDaily.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void savingsPlanUtilizationSummariesDaily(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .benefitUtilizationSummaries()
            .listBySavingsPlanOrder(
                "66cccc66-6ccc-6c66-666c-66cc6c6c66c6", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Dimensions_ByExternalCloudProviderType

```java
import com.azure.resourcemanager.costmanagement.models.ExternalCloudProviderType;

/** Samples for Dimensions ByExternalCloudProviderType. */
public final class DimensionsByExternalCloudProviderTypeSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExternalSubscriptionsDimensions.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExternalBillingAccountsDimensions.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/MCACustomerDimensionsList.json
     */
    /**
     * Sample code: CustomerDimensionsList-MCA.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void customerDimensionsListMCA(
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/DepartmentDimensionsList.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/MCAInvoiceSectionDimensionsListWithFilter.json
     */
    /**
     * Sample code: InvoiceSectionDimensionsListWithFilter-MCA.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void invoiceSectionDimensionsListWithFilterMCA(
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/MCACustomerDimensionsListWithFilter.json
     */
    /**
     * Sample code: CustomerDimensionsListWithFilter-MCA.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void customerDimensionsListWithFilterMCA(
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ManagementGroupDimensionsListWithFilter.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/BillingAccountDimensionsList.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ManagementGroupDimensionsListExpandAndTop.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/MCABillingProfileDimensionsList.json
     */
    /**
     * Sample code: BillingProfileDimensionsList-MCA.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingProfileDimensionsListMCA(
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/DepartmentDimensionsListExpandAndTop.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/EnrollmentAccountDimensionsList.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/MCABillingAccountDimensionsListExpandAndTop.json
     */
    /**
     * Sample code: BillingAccountDimensionsListExpandAndTop-MCA.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingAccountDimensionsListExpandAndTopMCA(
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/BillingAccountDimensionsListWithFilter.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ResourceGroupDimensionsList.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/SubscriptionDimensionsList.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/BillingAccountDimensionsListExpandAndTop.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/MCABillingAccountDimensionsList.json
     */
    /**
     * Sample code: BillingAccountDimensionsList-MCA.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingAccountDimensionsListMCA(
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/EnrollmentAccountDimensionsListExpandAndTop.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/MCACustomerDimensionsListExpandAndTop.json
     */
    /**
     * Sample code: CustomerDimensionsListExpandAndTop-MCA.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void customerDimensionsListExpandAndTopMCA(
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/DepartmentDimensionsListWithFilter.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/MCAInvoiceSectionDimensionsList.json
     */
    /**
     * Sample code: InvoiceSectionDimensionsList-MCA.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void invoiceSectionDimensionsListMCA(
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

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/MCAInvoiceSectionDimensionsListExpandAndTop.json
     */
    /**
     * Sample code: InvoiceSectionDimensionsListExpandAndTop-MCA.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void invoiceSectionDimensionsListExpandAndTopMCA(
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ManagementGroupDimensionsList.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/MCABillingProfileDimensionsListExpandAndTop.json
     */
    /**
     * Sample code: BillingProfileDimensionsListExpandAndTop-MCA.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingProfileDimensionsListExpandAndTopMCA(
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/MCABillingProfileDimensionsListWithFilter.json
     */
    /**
     * Sample code: BillingProfileDimensionsListWithFilter-MCA.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingProfileDimensionsListWithFilterMCA(
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/EnrollmentAccountDimensionsListWithFilter.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/MCABillingAccountDimensionsListWithFilter.json
     */
    /**
     * Sample code: BillingAccountDimensionsListWithFilter-MCA.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingAccountDimensionsListWithFilterMCA(
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportCreateOrUpdateByManagementGroup.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportCreateOrUpdateByBillingAccount.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportCreateOrUpdateByDepartment.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportCreateOrUpdateByResourceGroup.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportCreateOrUpdateBySubscription.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportCreateOrUpdateByEnrollmentAccount.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportDeleteByBillingAccount.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportDeleteByManagementGroup.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportDeleteByResourceGroup.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportDeleteByDepartment.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportDeleteBySubscription.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportDeleteByEnrollmentAccount.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportRunByManagementGroup.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportRunByBillingAccount.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportRunBySubscription.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportRunByDepartment.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportRunByResourceGroup.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportRunByEnrollmentAccount.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportGetByEnrollmentAccount.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportGetByManagementGroup.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportGetByDepartment.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportGetBySubscription.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportGetByBillingAccount.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportGetByResourceGroup.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportRunHistoryGetByManagementGroup.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportRunHistoryGetBySubscription.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportRunHistoryGetByResourceGroup.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportRunHistoryGetByEnrollmentAccount.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportRunHistoryGetByBillingAccount.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportRunHistoryGetByDepartment.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportsGetByResourceGroup.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportsGetByDepartment.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportsGetByBillingAccount.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportsGetByEnrollmentAccount.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportsGetByManagementGroup.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExportsGetBySubscription.json
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
import com.azure.resourcemanager.costmanagement.models.ForecastAggregation;
import com.azure.resourcemanager.costmanagement.models.ForecastComparisonExpression;
import com.azure.resourcemanager.costmanagement.models.ForecastDataset;
import com.azure.resourcemanager.costmanagement.models.ForecastDefinition;
import com.azure.resourcemanager.costmanagement.models.ForecastFilter;
import com.azure.resourcemanager.costmanagement.models.ForecastOperatorType;
import com.azure.resourcemanager.costmanagement.models.ForecastTimePeriod;
import com.azure.resourcemanager.costmanagement.models.ForecastTimeframe;
import com.azure.resourcemanager.costmanagement.models.ForecastType;
import com.azure.resourcemanager.costmanagement.models.FunctionName;
import com.azure.resourcemanager.costmanagement.models.FunctionType;
import com.azure.resourcemanager.costmanagement.models.GranularityType;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Forecast ExternalCloudProviderUsage. */
public final class ForecastExternalCloudProviderUsageSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExternalSubscriptionForecast.json
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
                    .withTimeframe(ForecastTimeframe.CUSTOM)
                    .withTimePeriod(
                        new ForecastTimePeriod()
                            .withFrom(OffsetDateTime.parse("2022-08-01T00:00:00+00:00"))
                            .withTo(OffsetDateTime.parse("2022-08-31T23:59:59+00:00")))
                    .withDataset(
                        new ForecastDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withAggregation(
                                mapOf(
                                    "totalCost",
                                    new ForecastAggregation()
                                        .withName(FunctionName.COST)
                                        .withFunction(FunctionType.SUM)))
                            .withFilter(
                                new ForecastFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new ForecastFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new ForecastFilter()
                                                                    .withDimensions(
                                                                        new ForecastComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(ForecastOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new ForecastFilter()
                                                                    .withTags(
                                                                        new ForecastComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(ForecastOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new ForecastFilter()
                                                    .withDimensions(
                                                        new ForecastComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(ForecastOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExternalBillingAccountForecast.json
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
                    .withTimeframe(ForecastTimeframe.CUSTOM)
                    .withTimePeriod(
                        new ForecastTimePeriod()
                            .withFrom(OffsetDateTime.parse("2022-08-01T00:00:00+00:00"))
                            .withTo(OffsetDateTime.parse("2022-08-31T23:59:59+00:00")))
                    .withDataset(
                        new ForecastDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withAggregation(
                                mapOf(
                                    "totalCost",
                                    new ForecastAggregation()
                                        .withName(FunctionName.COST)
                                        .withFunction(FunctionType.SUM)))
                            .withFilter(
                                new ForecastFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new ForecastFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new ForecastFilter()
                                                                    .withDimensions(
                                                                        new ForecastComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(ForecastOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new ForecastFilter()
                                                                    .withTags(
                                                                        new ForecastComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(ForecastOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new ForecastFilter()
                                                    .withDimensions(
                                                        new ForecastComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(ForecastOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                null,
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

### Forecast_Usage

```java
import com.azure.resourcemanager.costmanagement.models.ForecastAggregation;
import com.azure.resourcemanager.costmanagement.models.ForecastComparisonExpression;
import com.azure.resourcemanager.costmanagement.models.ForecastDataset;
import com.azure.resourcemanager.costmanagement.models.ForecastDefinition;
import com.azure.resourcemanager.costmanagement.models.ForecastFilter;
import com.azure.resourcemanager.costmanagement.models.ForecastOperatorType;
import com.azure.resourcemanager.costmanagement.models.ForecastTimePeriod;
import com.azure.resourcemanager.costmanagement.models.ForecastTimeframe;
import com.azure.resourcemanager.costmanagement.models.ForecastType;
import com.azure.resourcemanager.costmanagement.models.FunctionName;
import com.azure.resourcemanager.costmanagement.models.FunctionType;
import com.azure.resourcemanager.costmanagement.models.GranularityType;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Forecast Usage. */
public final class ForecastUsageSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ResourceGroupForecast.json
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
                    .withTimeframe(ForecastTimeframe.CUSTOM)
                    .withTimePeriod(
                        new ForecastTimePeriod()
                            .withFrom(OffsetDateTime.parse("2022-08-01T00:00:00+00:00"))
                            .withTo(OffsetDateTime.parse("2022-08-31T23:59:59+00:00")))
                    .withDataset(
                        new ForecastDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withAggregation(
                                mapOf(
                                    "totalCost",
                                    new ForecastAggregation()
                                        .withName(FunctionName.COST)
                                        .withFunction(FunctionType.SUM)))
                            .withFilter(
                                new ForecastFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new ForecastFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new ForecastFilter()
                                                                    .withDimensions(
                                                                        new ForecastComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(ForecastOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new ForecastFilter()
                                                                    .withTags(
                                                                        new ForecastComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(ForecastOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new ForecastFilter()
                                                    .withDimensions(
                                                        new ForecastComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(ForecastOperatorType.IN)
                                                            .withValues(Arrays.asList("API")))))))
                    .withIncludeActualCost(false)
                    .withIncludeFreshPartialCost(false),
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/EnrollmentAccountForecast.json
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
                    .withTimeframe(ForecastTimeframe.CUSTOM)
                    .withTimePeriod(
                        new ForecastTimePeriod()
                            .withFrom(OffsetDateTime.parse("2022-08-01T00:00:00+00:00"))
                            .withTo(OffsetDateTime.parse("2022-08-31T23:59:59+00:00")))
                    .withDataset(
                        new ForecastDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withAggregation(
                                mapOf(
                                    "totalCost",
                                    new ForecastAggregation()
                                        .withName(FunctionName.COST)
                                        .withFunction(FunctionType.SUM)))
                            .withFilter(
                                new ForecastFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new ForecastFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new ForecastFilter()
                                                                    .withDimensions(
                                                                        new ForecastComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(ForecastOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new ForecastFilter()
                                                                    .withTags(
                                                                        new ForecastComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(ForecastOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new ForecastFilter()
                                                    .withDimensions(
                                                        new ForecastComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(ForecastOperatorType.IN)
                                                            .withValues(Arrays.asList("API")))))))
                    .withIncludeActualCost(false)
                    .withIncludeFreshPartialCost(false),
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/SubscriptionForecast.json
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
                    .withTimeframe(ForecastTimeframe.CUSTOM)
                    .withTimePeriod(
                        new ForecastTimePeriod()
                            .withFrom(OffsetDateTime.parse("2022-08-01T00:00:00+00:00"))
                            .withTo(OffsetDateTime.parse("2022-08-31T23:59:59+00:00")))
                    .withDataset(
                        new ForecastDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withAggregation(
                                mapOf(
                                    "totalCost",
                                    new ForecastAggregation()
                                        .withName(FunctionName.COST)
                                        .withFunction(FunctionType.SUM)))
                            .withFilter(
                                new ForecastFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new ForecastFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new ForecastFilter()
                                                                    .withDimensions(
                                                                        new ForecastComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(ForecastOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new ForecastFilter()
                                                                    .withTags(
                                                                        new ForecastComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(ForecastOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new ForecastFilter()
                                                    .withDimensions(
                                                        new ForecastComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(ForecastOperatorType.IN)
                                                            .withValues(Arrays.asList("API")))))))
                    .withIncludeActualCost(false)
                    .withIncludeFreshPartialCost(false),
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/BillingProfileForecast.json
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
                    .withTimeframe(ForecastTimeframe.CUSTOM)
                    .withTimePeriod(
                        new ForecastTimePeriod()
                            .withFrom(OffsetDateTime.parse("2022-08-01T00:00:00+00:00"))
                            .withTo(OffsetDateTime.parse("2022-08-31T23:59:59+00:00")))
                    .withDataset(
                        new ForecastDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withAggregation(
                                mapOf(
                                    "totalCost",
                                    new ForecastAggregation()
                                        .withName(FunctionName.COST)
                                        .withFunction(FunctionType.SUM)))
                            .withFilter(
                                new ForecastFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new ForecastFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new ForecastFilter()
                                                                    .withDimensions(
                                                                        new ForecastComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(ForecastOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new ForecastFilter()
                                                                    .withTags(
                                                                        new ForecastComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(ForecastOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new ForecastFilter()
                                                    .withDimensions(
                                                        new ForecastComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(ForecastOperatorType.IN)
                                                            .withValues(Arrays.asList("API")))))))
                    .withIncludeActualCost(false)
                    .withIncludeFreshPartialCost(false),
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/DepartmentForecast.json
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
                    .withTimeframe(ForecastTimeframe.CUSTOM)
                    .withTimePeriod(
                        new ForecastTimePeriod()
                            .withFrom(OffsetDateTime.parse("2022-08-01T00:00:00+00:00"))
                            .withTo(OffsetDateTime.parse("2022-08-31T23:59:59+00:00")))
                    .withDataset(
                        new ForecastDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withAggregation(
                                mapOf(
                                    "totalCost",
                                    new ForecastAggregation()
                                        .withName(FunctionName.COST)
                                        .withFunction(FunctionType.SUM)))
                            .withFilter(
                                new ForecastFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new ForecastFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new ForecastFilter()
                                                                    .withDimensions(
                                                                        new ForecastComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(ForecastOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new ForecastFilter()
                                                                    .withTags(
                                                                        new ForecastComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(ForecastOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new ForecastFilter()
                                                    .withDimensions(
                                                        new ForecastComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(ForecastOperatorType.IN)
                                                            .withValues(Arrays.asList("API")))))))
                    .withIncludeActualCost(false)
                    .withIncludeFreshPartialCost(false),
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/BillingAccountForecast.json
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
                    .withTimeframe(ForecastTimeframe.CUSTOM)
                    .withTimePeriod(
                        new ForecastTimePeriod()
                            .withFrom(OffsetDateTime.parse("2022-08-01T00:00:00+00:00"))
                            .withTo(OffsetDateTime.parse("2022-08-31T23:59:59+00:00")))
                    .withDataset(
                        new ForecastDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withAggregation(
                                mapOf(
                                    "totalCost",
                                    new ForecastAggregation()
                                        .withName(FunctionName.COST)
                                        .withFunction(FunctionType.SUM)))
                            .withFilter(
                                new ForecastFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new ForecastFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new ForecastFilter()
                                                                    .withDimensions(
                                                                        new ForecastComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(ForecastOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new ForecastFilter()
                                                                    .withTags(
                                                                        new ForecastComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(ForecastOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new ForecastFilter()
                                                    .withDimensions(
                                                        new ForecastComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(ForecastOperatorType.IN)
                                                            .withValues(Arrays.asList("API")))))))
                    .withIncludeActualCost(false)
                    .withIncludeFreshPartialCost(false),
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/InvoiceSectionForecast.json
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
                    .withTimeframe(ForecastTimeframe.CUSTOM)
                    .withTimePeriod(
                        new ForecastTimePeriod()
                            .withFrom(OffsetDateTime.parse("2022-08-01T00:00:00+00:00"))
                            .withTo(OffsetDateTime.parse("2022-08-31T23:59:59+00:00")))
                    .withDataset(
                        new ForecastDataset()
                            .withGranularity(GranularityType.DAILY)
                            .withAggregation(
                                mapOf(
                                    "totalCost",
                                    new ForecastAggregation()
                                        .withName(FunctionName.COST)
                                        .withFunction(FunctionType.SUM)))
                            .withFilter(
                                new ForecastFilter()
                                    .withAnd(
                                        Arrays
                                            .asList(
                                                new ForecastFilter()
                                                    .withOr(
                                                        Arrays
                                                            .asList(
                                                                new ForecastFilter()
                                                                    .withDimensions(
                                                                        new ForecastComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(ForecastOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new ForecastFilter()
                                                                    .withTags(
                                                                        new ForecastComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(ForecastOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new ForecastFilter()
                                                    .withDimensions(
                                                        new ForecastComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(ForecastOperatorType.IN)
                                                            .withValues(Arrays.asList("API")))))))
                    .withIncludeActualCost(false)
                    .withIncludeFreshPartialCost(false),
                null,
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

### GenerateCostDetailsReport_CreateOperation

```java
import com.azure.resourcemanager.costmanagement.models.CostDetailsMetricType;
import com.azure.resourcemanager.costmanagement.models.CostDetailsTimePeriod;
import com.azure.resourcemanager.costmanagement.models.GenerateCostDetailsReportRequestDefinition;

/** Samples for GenerateCostDetailsReport CreateOperation. */
public final class GenerateCostDetailsReportCreateOperationSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/GenerateCostDetailsReportBySubscriptionAndTimePeriod.json
     */
    /**
     * Sample code: GenerateCostDetailsReportBySubscriptionAndTimePeriod.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void generateCostDetailsReportBySubscriptionAndTimePeriod(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .generateCostDetailsReports()
            .createOperation(
                "subscriptions/00000000-0000-0000-0000-000000000000",
                new GenerateCostDetailsReportRequestDefinition()
                    .withMetric(CostDetailsMetricType.ACTUAL_COST)
                    .withTimePeriod(new CostDetailsTimePeriod().withStart("2020-03-01").withEnd("2020-03-15")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/GenerateCostDetailsReportByEnrollmentAccountsAndTimePeriod.json
     */
    /**
     * Sample code: GenerateCostDetailsReportByEnrollmentAccountsAndTimePeriod.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void generateCostDetailsReportByEnrollmentAccountsAndTimePeriod(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .generateCostDetailsReports()
            .createOperation(
                "providers/Microsoft.Billing/enrollmentAccounts/1234",
                new GenerateCostDetailsReportRequestDefinition()
                    .withMetric(CostDetailsMetricType.ACTUAL_COST)
                    .withTimePeriod(new CostDetailsTimePeriod().withStart("2020-03-01").withEnd("2020-03-15")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/GenerateCostDetailsReportByBillingProfileAndInvoiceId.json
     */
    /**
     * Sample code: GenerateCostDetailsReportByBillingProfileAndInvoiceId.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void generateCostDetailsReportByBillingProfileAndInvoiceId(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .generateCostDetailsReports()
            .createOperation(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/billingProfiles/13579",
                new GenerateCostDetailsReportRequestDefinition()
                    .withMetric(CostDetailsMetricType.ACTUAL_COST)
                    .withInvoiceId("M1234567"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/GenerateCostDetailsReportByBillingAccountEnterpriseAgreementCustomerAndBillingPeriod.json
     */
    /**
     * Sample code: GenerateCostDetailsReportByBillingAccountEnterpriseAgreementCustomerAndBillingPeriod.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void generateCostDetailsReportByBillingAccountEnterpriseAgreementCustomerAndBillingPeriod(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .generateCostDetailsReports()
            .createOperation(
                "providers/Microsoft.Billing/billingAccounts/12345",
                new GenerateCostDetailsReportRequestDefinition()
                    .withMetric(CostDetailsMetricType.ACTUAL_COST)
                    .withBillingPeriod("202205"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/GenerateCostDetailsReportByBillingProfileAndInvoiceIdAndCustomerId.json
     */
    /**
     * Sample code: GenerateCostDetailsReportByBillingProfileAndInvoiceIdAndCustomerId.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void generateCostDetailsReportByBillingProfileAndInvoiceIdAndCustomerId(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .generateCostDetailsReports()
            .createOperation(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/customers/13579",
                new GenerateCostDetailsReportRequestDefinition()
                    .withMetric(CostDetailsMetricType.ACTUAL_COST)
                    .withInvoiceId("M1234567"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/GenerateCostDetailsReportByCustomerAndTimePeriod.json
     */
    /**
     * Sample code: GenerateCostDetailsReportByCustomerAndTimePeriod.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void generateCostDetailsReportByCustomerAndTimePeriod(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .generateCostDetailsReports()
            .createOperation(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/customers/13579",
                new GenerateCostDetailsReportRequestDefinition()
                    .withMetric(CostDetailsMetricType.ACTUAL_COST)
                    .withTimePeriod(new CostDetailsTimePeriod().withStart("2020-03-01").withEnd("2020-03-15")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/GenerateCostDetailsReportByDepartmentsAndTimePeriod.json
     */
    /**
     * Sample code: GenerateCostDetailsReportByDepartmentsAndTimePeriod.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void generateCostDetailsReportByDepartmentsAndTimePeriod(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .generateCostDetailsReports()
            .createOperation(
                "providers/Microsoft.Billing/departments/12345",
                new GenerateCostDetailsReportRequestDefinition()
                    .withMetric(CostDetailsMetricType.ACTUAL_COST)
                    .withTimePeriod(new CostDetailsTimePeriod().withStart("2020-03-01").withEnd("2020-03-15")),
                com.azure.core.util.Context.NONE);
    }
}
```

### GenerateCostDetailsReport_GetOperationResults

```java
/** Samples for GenerateCostDetailsReport GetOperationResults. */
public final class GenerateCostDetailsReportGetOperationResultsSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/CostDetailsOperationResultsBySubscriptionScope.json
     */
    /**
     * Sample code: Get details of the operation result.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void getDetailsOfTheOperationResult(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .generateCostDetailsReports()
            .getOperationResults(
                "subscriptions/00000000-0000-0000-0000-000000000000",
                "00000000-0000-0000-0000-000000000000",
                com.azure.core.util.Context.NONE);
    }
}
```

### GenerateDetailedCostReport_CreateOperation

```java
import com.azure.resourcemanager.costmanagement.models.GenerateDetailedCostReportDefinition;
import com.azure.resourcemanager.costmanagement.models.GenerateDetailedCostReportMetricType;
import com.azure.resourcemanager.costmanagement.models.GenerateDetailedCostReportTimePeriod;

/** Samples for GenerateDetailedCostReport CreateOperation. */
public final class GenerateDetailedCostReportCreateOperationSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/GenerateDetailedCostReportByBillingProfileAndInvoiceId.json
     */
    /**
     * Sample code: GenerateDetailedCostReportByBillingProfileAndInvoiceId.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void generateDetailedCostReportByBillingProfileAndInvoiceId(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .generateDetailedCostReports()
            .createOperation(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/billingProfiles/13579",
                new GenerateDetailedCostReportDefinition()
                    .withMetric(GenerateDetailedCostReportMetricType.ACTUAL_COST)
                    .withInvoiceId("M1234567"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/GenerateDetailedCostReportByBillingAccountLegacyAndBillingPeriod.json
     */
    /**
     * Sample code: GenerateDetailedCostReportByBillingAccountLegacyAndBillingPeriod.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void generateDetailedCostReportByBillingAccountLegacyAndBillingPeriod(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .generateDetailedCostReports()
            .createOperation(
                "providers/Microsoft.Billing/billingAccounts/12345",
                new GenerateDetailedCostReportDefinition()
                    .withMetric(GenerateDetailedCostReportMetricType.ACTUAL_COST)
                    .withBillingPeriod("202008"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/GenerateDetailedCostReportByBillingProfileAndInvoiceIdAndCustomerId.json
     */
    /**
     * Sample code: GenerateDetailedCostReportByBillingProfileAndInvoiceIdAndCustomerId.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void generateDetailedCostReportByBillingProfileAndInvoiceIdAndCustomerId(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .generateDetailedCostReports()
            .createOperation(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/billingProfiles/13579",
                new GenerateDetailedCostReportDefinition()
                    .withMetric(GenerateDetailedCostReportMetricType.ACTUAL_COST)
                    .withInvoiceId("M1234567")
                    .withCustomerId("456789"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/GenerateDetailedCostReportBySubscriptionAndTimePeriod.json
     */
    /**
     * Sample code: GenerateDetailedCostReportBySubscriptionAndTimePeriod.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void generateDetailedCostReportBySubscriptionAndTimePeriod(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .generateDetailedCostReports()
            .createOperation(
                "subscriptions/00000000-0000-0000-0000-000000000000",
                new GenerateDetailedCostReportDefinition()
                    .withMetric(GenerateDetailedCostReportMetricType.ACTUAL_COST)
                    .withTimePeriod(
                        new GenerateDetailedCostReportTimePeriod().withStart("2020-03-01").withEnd("2020-03-15")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/GenerateDetailedCostReportByCustomerAndTimePeriod.json
     */
    /**
     * Sample code: GenerateDetailedCostReportByCustomerAndTimePeriod.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void generateDetailedCostReportByCustomerAndTimePeriod(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .generateDetailedCostReports()
            .createOperation(
                "providers/Microsoft.Billing/billingAccounts/12345:6789/customers/13579",
                new GenerateDetailedCostReportDefinition()
                    .withMetric(GenerateDetailedCostReportMetricType.ACTUAL_COST)
                    .withTimePeriod(
                        new GenerateDetailedCostReportTimePeriod().withStart("2020-03-01").withEnd("2020-03-15")),
                com.azure.core.util.Context.NONE);
    }
}
```

### GenerateDetailedCostReportOperationResults_Get

```java
/** Samples for GenerateDetailedCostReportOperationResults Get. */
public final class GenerateDetailedCostReportOperationResultsGetSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/GenerateDetailedCostReportOperationResultsBySubscriptionScope.json
     */
    /**
     * Sample code: Get details of the operation result.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void getDetailsOfTheOperationResult(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .generateDetailedCostReportOperationResults()
            .get(
                "00000000-0000-0000-0000-000000000000",
                "subscriptions/00000000-0000-0000-0000-000000000000",
                com.azure.core.util.Context.NONE);
    }
}
```

### GenerateDetailedCostReportOperationStatus_Get

```java
/** Samples for GenerateDetailedCostReportOperationStatus Get. */
public final class GenerateDetailedCostReportOperationStatusGetSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/GenerateDetailedCostReportOperationStatusBySubscriptionScope.json
     */
    /**
     * Sample code: Get details of the operation status.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void getDetailsOfTheOperationStatus(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .generateDetailedCostReportOperationStatus()
            .getWithResponse(
                "00000000-0000-0000-0000-000000000000",
                "subscriptions/00000000-0000-0000-0000-000000000000",
                com.azure.core.util.Context.NONE);
    }
}
```

### GenerateReservationDetailsReport_ByBillingAccountId

```java
/** Samples for GenerateReservationDetailsReport ByBillingAccountId. */
public final class GenerateReservationDetailsReportByBillingAccountIdSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/GenerateReservationDetailsReportByBillingAccount.json
     */
    /**
     * Sample code: ReservationDetails.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void reservationDetails(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .generateReservationDetailsReports()
            .byBillingAccountId("9845612", "2020-01-01", "2020-01-30", com.azure.core.util.Context.NONE);
    }
}
```

### GenerateReservationDetailsReport_ByBillingProfileId

```java
/** Samples for GenerateReservationDetailsReport ByBillingProfileId. */
public final class GenerateReservationDetailsReportByBillingProfileIdSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/GenerateReservationDetailsReportByBillingProfile.json
     */
    /**
     * Sample code: ReservationDetails.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void reservationDetails(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .generateReservationDetailsReports()
            .byBillingProfileId(
                "00000000-0000-0000-0000-000000000000",
                "CZSFR-SDFXC-DSDF",
                "2020-01-01",
                "2020-01-30",
                com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/OperationList.json
     */
    /**
     * Sample code: OperationList.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void operationList(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PriceSheet_Download

```java
/** Samples for PriceSheet Download. */
public final class PriceSheetDownloadSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/PricesheetDownload.json
     */
    /**
     * Sample code: PricesheetDownload.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void pricesheetDownload(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .priceSheets()
            .download(
                "7c05a543-80ff-571e-9f98-1063b3b53cf2:99ad03ad-2d1b-4889-a452-090ad407d25f_2019-05-31",
                "2USN-TPCD-BG7-TGB",
                "T000940677",
                com.azure.core.util.Context.NONE);
    }
}
```

### PriceSheet_DownloadByBillingProfile

```java
/** Samples for PriceSheet DownloadByBillingProfile. */
public final class PriceSheetDownloadByBillingProfileSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/PricesheetDownloadByBillingProfile.json
     */
    /**
     * Sample code: PricesheetDownloadByBillingProfile.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void pricesheetDownloadByBillingProfile(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .priceSheets()
            .downloadByBillingProfile(
                "7c05a543-80ff-571e-9f98-1063b3b53cf2:99ad03ad-2d1b-4889-a452-090ad407d25f_2019-05-31",
                "2USN-TPCD-BG7-TGB",
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/SubscriptionQueryGrouping.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/EnrollmentAccountQueryGrouping.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/DepartmentQuery.json
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
                                                                    .withDimensions(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTags(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimensions(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/MCABillingAccountQuery.json
     */
    /**
     * Sample code: BillingAccountQuery-MCA.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingAccountQueryMCA(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
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
                                                                    .withDimensions(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTags(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimensions(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/BillingAccountQueryGrouping.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/SubscriptionQuery.json
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
                                                                    .withDimensions(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTags(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimensions(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/MCABillingAccountQueryGrouping.json
     */
    /**
     * Sample code: BillingAccountQueryGrouping-MCA.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingAccountQueryGroupingMCA(
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/MCAInvoiceSectionQueryGrouping.json
     */
    /**
     * Sample code: InvoiceSectionQueryGrouping-MCA.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void invoiceSectionQueryGroupingMCA(
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/EnrollmentAccountQuery.json
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
                                                                    .withDimensions(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTags(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimensions(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ResourceGroupQueryGrouping.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/BillingAccountQuery.json
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
                                                                    .withDimensions(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTags(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimensions(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/MCABillingProfileQueryGrouping.json
     */
    /**
     * Sample code: BillingProfileQueryGrouping-MCA.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingProfileQueryGroupingMCA(
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ResourceGroupQuery.json
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
                                                                    .withDimensions(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTags(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimensions(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/MCAInvoiceSectionQuery.json
     */
    /**
     * Sample code: InvoiceSectionQuery-MCA.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void invoiceSectionQueryMCA(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
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
                                                                    .withDimensions(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTags(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimensions(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/MCACustomerQuery.json
     */
    /**
     * Sample code: CustomerQuery-MCA.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void customerQueryMCA(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
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
                                                                    .withDimensions(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTags(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimensions(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ManagementGroupQuery.json
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
                                                                    .withDimensions(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTags(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimensions(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/DepartmentQueryGrouping.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/MCABillingProfileQuery.json
     */
    /**
     * Sample code: BillingProfileQuery-MCA.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void billingProfileQueryMCA(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
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
                                                                    .withDimensions(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTags(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimensions(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ManagementGroupQueryGrouping.json
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

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/MCACustomerQueryGrouping.json
     */
    /**
     * Sample code: CustomerQueryGrouping-MCA.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void customerQueryGroupingMCA(
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExternalSubscriptionsQuery.json
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
                                                                    .withDimensions(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTags(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimensions(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ExternalBillingAccountsQuery.json
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
                                                                    .withDimensions(
                                                                        new QueryComparisonExpression()
                                                                            .withName("ResourceLocation")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays
                                                                                    .asList("East US", "West Europe"))),
                                                                new QueryFilter()
                                                                    .withTags(
                                                                        new QueryComparisonExpression()
                                                                            .withName("Environment")
                                                                            .withOperator(QueryOperatorType.IN)
                                                                            .withValues(
                                                                                Arrays.asList("UAT", "Prod"))))),
                                                new QueryFilter()
                                                    .withDimensions(
                                                        new QueryComparisonExpression()
                                                            .withName("ResourceGroup")
                                                            .withOperator(QueryOperatorType.IN)
                                                            .withValues(Arrays.asList("API"))))))),
                com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_CheckNameAvailability

```java
import com.azure.resourcemanager.costmanagement.models.CheckNameAvailabilityRequest;

/** Samples for ScheduledActions CheckNameAvailability. */
public final class ScheduledActionsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/scheduledActions/checkNameAvailability-private-scheduledAction.json
     */
    /**
     * Sample code: ScheduledActionCheckNameAvailability.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void scheduledActionCheckNameAvailability(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .scheduledActions()
            .checkNameAvailabilityWithResponse(
                new CheckNameAvailabilityRequest()
                    .withName("testName")
                    .withType("Microsoft.CostManagement/ScheduledActions"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_CheckNameAvailabilityByScope

```java
import com.azure.resourcemanager.costmanagement.models.CheckNameAvailabilityRequest;

/** Samples for ScheduledActions CheckNameAvailabilityByScope. */
public final class ScheduledActionsCheckNameAvailabilityByScopeSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/scheduledActions/checkNameAvailability-shared-scheduledAction.json
     */
    /**
     * Sample code: ScheduledActionCheckNameAvailabilityByScope.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void scheduledActionCheckNameAvailabilityByScope(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .scheduledActions()
            .checkNameAvailabilityByScopeWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000",
                new CheckNameAvailabilityRequest()
                    .withName("testName")
                    .withType("Microsoft.CostManagement/ScheduledActions"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_CreateOrUpdate

```java
import com.azure.resourcemanager.costmanagement.fluent.models.ScheduledActionInner;
import com.azure.resourcemanager.costmanagement.models.DaysOfWeek;
import com.azure.resourcemanager.costmanagement.models.NotificationProperties;
import com.azure.resourcemanager.costmanagement.models.ScheduleFrequency;
import com.azure.resourcemanager.costmanagement.models.ScheduleProperties;
import com.azure.resourcemanager.costmanagement.models.ScheduledActionKind;
import com.azure.resourcemanager.costmanagement.models.ScheduledActionStatus;
import com.azure.resourcemanager.costmanagement.models.WeeksOfMonth;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Samples for ScheduledActions CreateOrUpdate. */
public final class ScheduledActionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/scheduledActions/scheduledAction-createOrUpdate-private.json
     */
    /**
     * Sample code: CreateOrUpdatePrivateScheduledAction.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void createOrUpdatePrivateScheduledAction(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .scheduledActions()
            .createOrUpdateWithResponse(
                "monthlyCostByResource",
                new ScheduledActionInner()
                    .withKind(ScheduledActionKind.EMAIL)
                    .withDisplayName("Monthly Cost By Resource")
                    .withNotification(
                        new NotificationProperties()
                            .withTo(Arrays.asList("user@gmail.com", "team@gmail.com"))
                            .withSubject("Cost by resource this month"))
                    .withSchedule(
                        new ScheduleProperties()
                            .withFrequency(ScheduleFrequency.MONTHLY)
                            .withHourOfDay(10)
                            .withDaysOfWeek(Arrays.asList(DaysOfWeek.MONDAY))
                            .withWeeksOfMonth(Arrays.asList(WeeksOfMonth.FIRST, WeeksOfMonth.THIRD))
                            .withStartDate(OffsetDateTime.parse("2020-06-19T22:21:51.1287144Z"))
                            .withEndDate(OffsetDateTime.parse("2021-06-19T22:21:51.1287144Z")))
                    .withStatus(ScheduledActionStatus.ENABLED)
                    .withViewId("/providers/Microsoft.CostManagement/views/swaggerExample"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_CreateOrUpdateByScope

```java
import com.azure.resourcemanager.costmanagement.models.DaysOfWeek;
import com.azure.resourcemanager.costmanagement.models.FileDestination;
import com.azure.resourcemanager.costmanagement.models.FileFormat;
import com.azure.resourcemanager.costmanagement.models.NotificationProperties;
import com.azure.resourcemanager.costmanagement.models.ScheduleFrequency;
import com.azure.resourcemanager.costmanagement.models.ScheduleProperties;
import com.azure.resourcemanager.costmanagement.models.ScheduledActionKind;
import com.azure.resourcemanager.costmanagement.models.ScheduledActionStatus;
import com.azure.resourcemanager.costmanagement.models.WeeksOfMonth;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Samples for ScheduledActions CreateOrUpdateByScope. */
public final class ScheduledActionsCreateOrUpdateByScopeSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/scheduledActions/scheduledAction-createOrUpdate-shared.json
     */
    /**
     * Sample code: CreateOrUpdateScheduledActionByScope.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void createOrUpdateScheduledActionByScope(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .scheduledActions()
            .define("monthlyCostByResource")
            .withExistingScope("subscriptions/00000000-0000-0000-0000-000000000000")
            .withKind(ScheduledActionKind.EMAIL)
            .withDisplayName("Monthly Cost By Resource")
            .withFileDestination(new FileDestination().withFileFormats(Arrays.asList(FileFormat.CSV)))
            .withNotification(
                new NotificationProperties()
                    .withTo(Arrays.asList("user@gmail.com", "team@gmail.com"))
                    .withSubject("Cost by resource this month"))
            .withSchedule(
                new ScheduleProperties()
                    .withFrequency(ScheduleFrequency.MONTHLY)
                    .withHourOfDay(10)
                    .withDaysOfWeek(Arrays.asList(DaysOfWeek.MONDAY))
                    .withWeeksOfMonth(Arrays.asList(WeeksOfMonth.FIRST, WeeksOfMonth.THIRD))
                    .withStartDate(OffsetDateTime.parse("2020-06-19T22:21:51.1287144Z"))
                    .withEndDate(OffsetDateTime.parse("2021-06-19T22:21:51.1287144Z")))
            .withStatus(ScheduledActionStatus.ENABLED)
            .withViewId("/providers/Microsoft.CostManagement/views/swaggerExample")
            .create();
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/scheduledActions/scheduledAction-insightAlert-createOrUpdate-shared.json
     */
    /**
     * Sample code: CreateOrUpdateInsightAlertScheduledActionByScope.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void createOrUpdateInsightAlertScheduledActionByScope(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .scheduledActions()
            .define("dailyAnomalyByResource")
            .withExistingScope("subscriptions/00000000-0000-0000-0000-000000000000")
            .withKind(ScheduledActionKind.INSIGHT_ALERT)
            .withDisplayName("Daily anomaly by resource")
            .withNotification(
                new NotificationProperties()
                    .withTo(Arrays.asList("user@gmail.com", "team@gmail.com"))
                    .withSubject("Cost anomaly detected in the resource"))
            .withSchedule(
                new ScheduleProperties()
                    .withFrequency(ScheduleFrequency.DAILY)
                    .withStartDate(OffsetDateTime.parse("2020-06-19T22:21:51.1287144Z"))
                    .withEndDate(OffsetDateTime.parse("2021-06-19T22:21:51.1287144Z")))
            .withStatus(ScheduledActionStatus.ENABLED)
            .withViewId("/providers/Microsoft.CostManagement/views/swaggerExample")
            .create();
    }
}
```

### ScheduledActions_Delete

```java
/** Samples for ScheduledActions Delete. */
public final class ScheduledActionsDeleteSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/scheduledActions/scheduledAction-delete-private.json
     */
    /**
     * Sample code: PrivateScheduledActionDelete.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void privateScheduledActionDelete(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager.scheduledActions().deleteWithResponse("monthlyCostByResource", com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_DeleteByScope

```java
/** Samples for ScheduledActions DeleteByScope. */
public final class ScheduledActionsDeleteByScopeSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/scheduledActions/scheduledAction-delete-shared.json
     */
    /**
     * Sample code: ScheduledActionDeleteByScope.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void scheduledActionDeleteByScope(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .scheduledActions()
            .deleteByScopeWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000",
                "monthlyCostByResource",
                com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_Get

```java
/** Samples for ScheduledActions Get. */
public final class ScheduledActionsGetSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/scheduledActions/scheduledAction-get-private.json
     */
    /**
     * Sample code: PrivateScheduledAction.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void privateScheduledAction(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager.scheduledActions().getWithResponse("monthlyCostByResource", com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_GetByScope

```java
/** Samples for ScheduledActions GetByScope. */
public final class ScheduledActionsGetByScopeSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/scheduledActions/scheduledAction-get-shared.json
     */
    /**
     * Sample code: ScheduledActionByScope.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void scheduledActionByScope(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .scheduledActions()
            .getByScopeWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000",
                "monthlyCostByResource",
                com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_List

```java
/** Samples for ScheduledActions List. */
public final class ScheduledActionsListSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/scheduledActions/scheduledActions-listWithFilter-private.json
     */
    /**
     * Sample code: PrivateScheduledActionsListFilterByViewId.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void privateScheduledActionsListFilterByViewId(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .scheduledActions()
            .list(
                "properties/viewId eq '/providers/Microsoft.CostManagement/views/swaggerExample'",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/scheduledActions/scheduledActions-list-private.json
     */
    /**
     * Sample code: PrivateScheduledActionsList.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void privateScheduledActionsList(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager.scheduledActions().list(null, com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_ListByScope

```java
/** Samples for ScheduledActions ListByScope. */
public final class ScheduledActionsListByScopeSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/scheduledActions/scheduledActions-listWithFilter-shared.json
     */
    /**
     * Sample code: ScheduledActionsListByScopeFilterByViewId.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void scheduledActionsListByScopeFilterByViewId(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .scheduledActions()
            .listByScope(
                "subscriptions/00000000-0000-0000-0000-000000000000",
                "properties/viewId eq '/providers/Microsoft.CostManagement/views/swaggerExample'",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/scheduledActions/scheduledActions-list-shared.json
     */
    /**
     * Sample code: ScheduledActionsListByScope.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void scheduledActionsListByScope(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .scheduledActions()
            .listByScope("subscriptions/00000000-0000-0000-0000-000000000000", null, com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_Run

```java
/** Samples for ScheduledActions Run. */
public final class ScheduledActionsRunSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/scheduledActions/scheduledAction-sendNow-private.json
     */
    /**
     * Sample code: ScheduledActionSendNow.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void scheduledActionSendNow(com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager.scheduledActions().runWithResponse("monthlyCostByResource", com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_RunByScope

```java
/** Samples for ScheduledActions RunByScope. */
public final class ScheduledActionsRunByScopeSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/scheduledActions/scheduledAction-sendNow-shared.json
     */
    /**
     * Sample code: ScheduledActionRunByScope.
     *
     * @param manager Entry point to CostManagementManager.
     */
    public static void scheduledActionRunByScope(
        com.azure.resourcemanager.costmanagement.CostManagementManager manager) {
        manager
            .scheduledActions()
            .runByScopeWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000",
                "monthlyCostByResource",
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
import com.azure.resourcemanager.costmanagement.models.ReportConfigSortingType;
import com.azure.resourcemanager.costmanagement.models.ReportGranularityType;
import com.azure.resourcemanager.costmanagement.models.ReportTimeframeType;
import com.azure.resourcemanager.costmanagement.models.ReportType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Views CreateOrUpdate. */
public final class ViewsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/PrivateViewCreateOrUpdate.json
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
                    .withDataSet(
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
                                            .withDirection(ReportConfigSortingType.ASCENDING)
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
import com.azure.resourcemanager.costmanagement.models.ReportConfigSortingType;
import com.azure.resourcemanager.costmanagement.models.ReportGranularityType;
import com.azure.resourcemanager.costmanagement.models.ReportTimeframeType;
import com.azure.resourcemanager.costmanagement.models.ReportType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Views CreateOrUpdateByScope. */
public final class ViewsCreateOrUpdateByScopeSamples {
    /*
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ViewCreateOrUpdateByResourceGroup.json
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
            .withDataSet(
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
                                    .withDirection(ReportConfigSortingType.ASCENDING)
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/PrivateViewDelete.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ViewDeleteByResourceGroup.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/PrivateView.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ViewByResourceGroup.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/PrivateViewList.json
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
     * x-ms-original-file: specification/cost-management/resource-manager/Microsoft.CostManagement/stable/2022-10-01/examples/ViewListByResourceGroup.json
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

