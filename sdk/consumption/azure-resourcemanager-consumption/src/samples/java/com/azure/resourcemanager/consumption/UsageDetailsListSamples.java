// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.consumption;

import com.azure.core.util.Context;
import com.azure.resourcemanager.consumption.models.Metrictype;

/** Samples for UsageDetails List. */
public final class UsageDetailsListSamples {
    /**
     * Sample code: BillingAccountUsageDetailsListForBillingPeriod-Legacy.
     *
     * @param consumptionManager Entry point to ConsumptionManager. Consumption management client provides access to
     *     consumption resources for Azure Enterprise Subscriptions.
     */
    public static void billingAccountUsageDetailsListForBillingPeriodLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager consumptionManager) {
        consumptionManager
            .usageDetails()
            .list("providers/Microsoft.Billing/BillingAccounts/1234", null, null, null, null, null, Context.NONE);
    }

    /**
     * Sample code: InvoiceSectionUsageDetailsList-Modern.
     *
     * @param consumptionManager Entry point to ConsumptionManager. Consumption management client provides access to
     *     consumption resources for Azure Enterprise Subscriptions.
     */
    public static void invoiceSectionUsageDetailsListModern(
        com.azure.resourcemanager.consumption.ConsumptionManager consumptionManager) {
        consumptionManager
            .usageDetails()
            .list(
                "providers/Microsoft.Billing/BillingAccounts/1234:56789/invoiceSections/98765",
                null,
                null,
                null,
                null,
                null,
                Context.NONE);
    }

    /**
     * Sample code: UsageDetailsList-Legacy.
     *
     * @param consumptionManager Entry point to ConsumptionManager. Consumption management client provides access to
     *     consumption resources for Azure Enterprise Subscriptions.
     */
    public static void usageDetailsListLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager consumptionManager) {
        consumptionManager
            .usageDetails()
            .list("subscriptions/00000000-0000-0000-0000-000000000000", null, null, null, null, null, Context.NONE);
    }

    /**
     * Sample code: UsageDetailsListFilterByTag-Legacy.
     *
     * @param consumptionManager Entry point to ConsumptionManager. Consumption management client provides access to
     *     consumption resources for Azure Enterprise Subscriptions.
     */
    public static void usageDetailsListFilterByTagLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager consumptionManager) {
        consumptionManager
            .usageDetails()
            .list(
                "subscriptions/00000000-0000-0000-0000-000000000000",
                null,
                "tags eq 'dev:tools'",
                null,
                null,
                null,
                Context.NONE);
    }

    /**
     * Sample code: BillingAccountUsageDetailsList-Legacy.
     *
     * @param consumptionManager Entry point to ConsumptionManager. Consumption management client provides access to
     *     consumption resources for Azure Enterprise Subscriptions.
     */
    public static void billingAccountUsageDetailsListLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager consumptionManager) {
        consumptionManager
            .usageDetails()
            .list("providers/Microsoft.Billing/BillingAccounts/1234", null, null, null, null, null, Context.NONE);
    }

    /**
     * Sample code: BillingProfileUsageDetailsList-Modern.
     *
     * @param consumptionManager Entry point to ConsumptionManager. Consumption management client provides access to
     *     consumption resources for Azure Enterprise Subscriptions.
     */
    public static void billingProfileUsageDetailsListModern(
        com.azure.resourcemanager.consumption.ConsumptionManager consumptionManager) {
        consumptionManager
            .usageDetails()
            .list(
                "providers/Microsoft.Billing/BillingAccounts/1234:56789/billingProfiles/2468",
                null,
                null,
                null,
                null,
                null,
                Context.NONE);
    }

    /**
     * Sample code: DepartmentUsageDetailsListForBillingPeriod-Legacy.
     *
     * @param consumptionManager Entry point to ConsumptionManager. Consumption management client provides access to
     *     consumption resources for Azure Enterprise Subscriptions.
     */
    public static void departmentUsageDetailsListForBillingPeriodLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager consumptionManager) {
        consumptionManager
            .usageDetails()
            .list("providers/Microsoft.Billing/Departments/1234", null, null, null, null, null, Context.NONE);
    }

    /**
     * Sample code: ManagementGroupUsageDetailsListForBillingPeriod-Legacy.
     *
     * @param consumptionManager Entry point to ConsumptionManager. Consumption management client provides access to
     *     consumption resources for Azure Enterprise Subscriptions.
     */
    public static void managementGroupUsageDetailsListForBillingPeriodLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager consumptionManager) {
        consumptionManager
            .usageDetails()
            .list("subscriptions/00000000-0000-0000-0000-000000000000", null, null, null, null, null, Context.NONE);
    }

    /**
     * Sample code: UsageDetailsListByMetricAmortizedCost-Legacy.
     *
     * @param consumptionManager Entry point to ConsumptionManager. Consumption management client provides access to
     *     consumption resources for Azure Enterprise Subscriptions.
     */
    public static void usageDetailsListByMetricAmortizedCostLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager consumptionManager) {
        consumptionManager
            .usageDetails()
            .list(
                "subscriptions/00000000-0000-0000-0000-000000000000",
                null,
                null,
                null,
                null,
                Metrictype.AMORTIZEDCOST,
                Context.NONE);
    }

    /**
     * Sample code: UsageDetailsListByMetricUsage-Legacy.
     *
     * @param consumptionManager Entry point to ConsumptionManager. Consumption management client provides access to
     *     consumption resources for Azure Enterprise Subscriptions.
     */
    public static void usageDetailsListByMetricUsageLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager consumptionManager) {
        consumptionManager
            .usageDetails()
            .list(
                "subscriptions/00000000-0000-0000-0000-000000000000",
                null,
                null,
                null,
                null,
                Metrictype.USAGE,
                Context.NONE);
    }

    /**
     * Sample code: ManagementGroupUsageDetailsList-Legacy.
     *
     * @param consumptionManager Entry point to ConsumptionManager. Consumption management client provides access to
     *     consumption resources for Azure Enterprise Subscriptions.
     */
    public static void managementGroupUsageDetailsListLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager consumptionManager) {
        consumptionManager
            .usageDetails()
            .list("subscriptions/00000000-0000-0000-0000-000000000000", null, null, null, null, null, Context.NONE);
    }

    /**
     * Sample code: DepartmentUsageDetailsList-Legacy.
     *
     * @param consumptionManager Entry point to ConsumptionManager. Consumption management client provides access to
     *     consumption resources for Azure Enterprise Subscriptions.
     */
    public static void departmentUsageDetailsListLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager consumptionManager) {
        consumptionManager
            .usageDetails()
            .list("providers/Microsoft.Billing/Departments/1234", null, null, null, null, null, Context.NONE);
    }

    /**
     * Sample code: UsageDetailsListByMetricActualCost-Legacy.
     *
     * @param consumptionManager Entry point to ConsumptionManager. Consumption management client provides access to
     *     consumption resources for Azure Enterprise Subscriptions.
     */
    public static void usageDetailsListByMetricActualCostLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager consumptionManager) {
        consumptionManager
            .usageDetails()
            .list(
                "subscriptions/00000000-0000-0000-0000-000000000000",
                null,
                null,
                null,
                null,
                Metrictype.ACTUALCOST,
                Context.NONE);
    }

    /**
     * Sample code: EnrollmentAccountUsageDetailsList-Legacy.
     *
     * @param consumptionManager Entry point to ConsumptionManager. Consumption management client provides access to
     *     consumption resources for Azure Enterprise Subscriptions.
     */
    public static void enrollmentAccountUsageDetailsListLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager consumptionManager) {
        consumptionManager
            .usageDetails()
            .list("providers/Microsoft.Billing/EnrollmentAccounts/1234", null, null, null, null, null, Context.NONE);
    }

    /**
     * Sample code: BillingAccountUsageDetailsList-Modern.
     *
     * @param consumptionManager Entry point to ConsumptionManager. Consumption management client provides access to
     *     consumption resources for Azure Enterprise Subscriptions.
     */
    public static void billingAccountUsageDetailsListModern(
        com.azure.resourcemanager.consumption.ConsumptionManager consumptionManager) {
        consumptionManager
            .usageDetails()
            .list("providers/Microsoft.Billing/BillingAccounts/1234:56789", null, null, null, null, null, Context.NONE);
    }

    /**
     * Sample code: CustomerUsageDetailsList-Modern.
     *
     * @param consumptionManager Entry point to ConsumptionManager. Consumption management client provides access to
     *     consumption resources for Azure Enterprise Subscriptions.
     */
    public static void customerUsageDetailsListModern(
        com.azure.resourcemanager.consumption.ConsumptionManager consumptionManager) {
        consumptionManager
            .usageDetails()
            .list(
                "providers/Microsoft.Billing/BillingAccounts/1234:56789/customers/00000000-0000-0000-0000-000000000000",
                null,
                null,
                null,
                null,
                null,
                Context.NONE);
    }

    /**
     * Sample code: UsageDetailsExpand-Legacy.
     *
     * @param consumptionManager Entry point to ConsumptionManager. Consumption management client provides access to
     *     consumption resources for Azure Enterprise Subscriptions.
     */
    public static void usageDetailsExpandLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager consumptionManager) {
        consumptionManager
            .usageDetails()
            .list(
                "subscriptions/00000000-0000-0000-0000-000000000000",
                "meterDetails,additionalInfo",
                "tags eq 'dev:tools'",
                null,
                1,
                null,
                Context.NONE);
    }

    /**
     * Sample code: EnrollmentAccountUsageDetailsListForBillingPeriod-Legacy.
     *
     * @param consumptionManager Entry point to ConsumptionManager. Consumption management client provides access to
     *     consumption resources for Azure Enterprise Subscriptions.
     */
    public static void enrollmentAccountUsageDetailsListForBillingPeriodLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager consumptionManager) {
        consumptionManager
            .usageDetails()
            .list("providers/Microsoft.Billing/EnrollmentAccounts/1234", null, null, null, null, null, Context.NONE);
    }

    /**
     * Sample code: UsageDetailsListForBillingPeriod-Legacy.
     *
     * @param consumptionManager Entry point to ConsumptionManager. Consumption management client provides access to
     *     consumption resources for Azure Enterprise Subscriptions.
     */
    public static void usageDetailsListForBillingPeriodLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager consumptionManager) {
        consumptionManager
            .usageDetails()
            .list("subscriptions/00000000-0000-0000-0000-000000000000", null, null, null, null, null, Context.NONE);
    }
}
