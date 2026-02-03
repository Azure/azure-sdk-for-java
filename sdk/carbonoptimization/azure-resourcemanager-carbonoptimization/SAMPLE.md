# Code snippets and samples


## CarbonService

- [QueryCarbonEmissionDataAvailableDateRange](#carbonservice_querycarbonemissiondataavailabledaterange)
- [QueryCarbonEmissionReports](#carbonservice_querycarbonemissionreports)

## Operations

- [List](#operations_list)
### CarbonService_QueryCarbonEmissionDataAvailableDateRange

```java
/**
 * Samples for CarbonService QueryCarbonEmissionDataAvailableDateRange.
 */
public final class CarbonServiceQueryCarbonEmissionDataAvailableDateRangeSamples {
    /*
     * x-ms-original-file: 2025-04-01/carbonEmissionsDataAvailableDateRange.json
     */
    /**
     * Sample code: CarbonService_QueryCarbonEmissionDataAvailableDateRange.
     * 
     * @param manager Entry point to CarbonOptimizationManager.
     */
    public static void carbonServiceQueryCarbonEmissionDataAvailableDateRange(
        com.azure.resourcemanager.carbonoptimization.CarbonOptimizationManager manager) {
        manager.carbonServices()
            .queryCarbonEmissionDataAvailableDateRangeWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### CarbonService_QueryCarbonEmissionReports

```java
import com.azure.resourcemanager.carbonoptimization.models.CategoryTypeEnum;
import com.azure.resourcemanager.carbonoptimization.models.DateRange;
import com.azure.resourcemanager.carbonoptimization.models.EmissionScopeEnum;
import com.azure.resourcemanager.carbonoptimization.models.ItemDetailsQueryFilter;
import com.azure.resourcemanager.carbonoptimization.models.MonthlySummaryReportQueryFilter;
import com.azure.resourcemanager.carbonoptimization.models.OrderByColumnEnum;
import com.azure.resourcemanager.carbonoptimization.models.OverallSummaryReportQueryFilter;
import com.azure.resourcemanager.carbonoptimization.models.SortDirectionEnum;
import com.azure.resourcemanager.carbonoptimization.models.TopItemsMonthlySummaryReportQueryFilter;
import com.azure.resourcemanager.carbonoptimization.models.TopItemsSummaryReportQueryFilter;
import java.time.LocalDate;
import java.util.Arrays;

/**
 * Samples for CarbonService QueryCarbonEmissionReports.
 */
public final class CarbonServiceQueryCarbonEmissionReportsSamples {
    /*
     * x-ms-original-file: 2025-04-01/queryCarbonEmissionsTopNResourceTypeItemsReport.json
     */
    /**
     * Sample code: QueryCarbonEmission Top N ResourceType Report.
     * 
     * @param manager Entry point to CarbonOptimizationManager.
     */
    public static void queryCarbonEmissionTopNResourceTypeReport(
        com.azure.resourcemanager.carbonoptimization.CarbonOptimizationManager manager) {
        manager.carbonServices()
            .queryCarbonEmissionReportsWithResponse(new TopItemsSummaryReportQueryFilter()
                .withDateRange(
                    new DateRange().withStart(LocalDate.parse("2024-05-01")).withEnd(LocalDate.parse("2024-05-01")))
                .withSubscriptionList(Arrays.asList("00000000-0000-0000-0000-000000000000",
                    "00000000-0000-0000-0000-000000000001,", "00000000-0000-0000-0000-000000000002",
                    "00000000-0000-0000-0000-000000000003", "00000000-0000-0000-0000-000000000004",
                    "00000000-0000-0000-0000-000000000005", "00000000-0000-0000-0000-000000000006",
                    "00000000-0000-0000-0000-000000000007", "00000000-0000-0000-0000-000000000008"))
                .withCarbonScopeList(Arrays.asList(EmissionScopeEnum.SCOPE1, EmissionScopeEnum.SCOPE3))
                .withCategoryType(CategoryTypeEnum.RESOURCE_TYPE)
                .withTopItems(5), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-01/queryCarbonEmissionsTopNResourceItemsReport.json
     */
    /**
     * Sample code: QueryCarbonEmission Top N resource Report.
     * 
     * @param manager Entry point to CarbonOptimizationManager.
     */
    public static void queryCarbonEmissionTopNResourceReport(
        com.azure.resourcemanager.carbonoptimization.CarbonOptimizationManager manager) {
        manager.carbonServices()
            .queryCarbonEmissionReportsWithResponse(new TopItemsSummaryReportQueryFilter()
                .withDateRange(
                    new DateRange().withStart(LocalDate.parse("2024-05-01")).withEnd(LocalDate.parse("2024-05-01")))
                .withSubscriptionList(Arrays.asList("00000000-0000-0000-0000-000000000000",
                    "00000000-0000-0000-0000-000000000001,", "00000000-0000-0000-0000-000000000002",
                    "00000000-0000-0000-0000-000000000003", "00000000-0000-0000-0000-000000000004",
                    "00000000-0000-0000-0000-000000000005", "00000000-0000-0000-0000-000000000006",
                    "00000000-0000-0000-0000-000000000007", "00000000-0000-0000-0000-000000000008"))
                .withCarbonScopeList(Arrays.asList(EmissionScopeEnum.SCOPE1, EmissionScopeEnum.SCOPE3))
                .withCategoryType(CategoryTypeEnum.RESOURCE)
                .withTopItems(5), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-01/queryCarbonEmissionsMonthlySummaryReportWithOtherOptionalFilter.json
     */
    /**
     * Sample code: QueryCarbonEmission Monthly Summary Report with optional filter - locationList, resourceTypeList,
     * resourceGroupUrlList.
     * 
     * @param manager Entry point to CarbonOptimizationManager.
     */
    public static void
        queryCarbonEmissionMonthlySummaryReportWithOptionalFilterLocationListResourceTypeListResourceGroupUrlList(
            com.azure.resourcemanager.carbonoptimization.CarbonOptimizationManager manager) {
        manager.carbonServices()
            .queryCarbonEmissionReportsWithResponse(
                new MonthlySummaryReportQueryFilter()
                    .withDateRange(
                        new DateRange().withStart(LocalDate.parse("2024-03-01")).withEnd(LocalDate.parse("2024-05-01")))
                    .withSubscriptionList(Arrays.asList("00000000-0000-0000-0000-000000000000"))
                    .withResourceGroupUrlList(
                        Arrays.asList("/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/rg-name"))
                    .withResourceTypeList(
                        Arrays.asList("microsoft.storage/storageaccounts", "microsoft.databricks/workspaces"))
                    .withLocationList(Arrays.asList("east us", "west us"))
                    .withCarbonScopeList(Arrays.asList(EmissionScopeEnum.SCOPE1, EmissionScopeEnum.SCOPE3)),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-01/queryCarbonEmissionsSubscriptionItemDetailsReportReport.json
     */
    /**
     * Sample code: QueryCarbonEmission Subscriptions item details Report.
     * 
     * @param manager Entry point to CarbonOptimizationManager.
     */
    public static void queryCarbonEmissionSubscriptionsItemDetailsReport(
        com.azure.resourcemanager.carbonoptimization.CarbonOptimizationManager manager) {
        manager.carbonServices()
            .queryCarbonEmissionReportsWithResponse(new ItemDetailsQueryFilter()
                .withDateRange(
                    new DateRange().withStart(LocalDate.parse("2024-05-01")).withEnd(LocalDate.parse("2024-05-01")))
                .withSubscriptionList(Arrays.asList("00000000-0000-0000-0000-000000000000",
                    "00000000-0000-0000-0000-000000000001,", "00000000-0000-0000-0000-000000000002",
                    "00000000-0000-0000-0000-000000000003", "00000000-0000-0000-0000-000000000004",
                    "00000000-0000-0000-0000-000000000005", "00000000-0000-0000-0000-000000000006",
                    "00000000-0000-0000-0000-000000000007", "00000000-0000-0000-0000-000000000008"))
                .withCarbonScopeList(Arrays.asList(EmissionScopeEnum.SCOPE1, EmissionScopeEnum.SCOPE3))
                .withCategoryType(CategoryTypeEnum.SUBSCRIPTION)
                .withOrderBy(OrderByColumnEnum.LATEST_MONTH_EMISSIONS)
                .withSortDirection(SortDirectionEnum.DESC)
                .withPageSize(100), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-01/queryCarbonEmissionsResourceGroupItemDetailsReport.json
     */
    /**
     * Sample code: QueryCarbonEmission ResourceGroup item details Report.
     * 
     * @param manager Entry point to CarbonOptimizationManager.
     */
    public static void queryCarbonEmissionResourceGroupItemDetailsReport(
        com.azure.resourcemanager.carbonoptimization.CarbonOptimizationManager manager) {
        manager.carbonServices()
            .queryCarbonEmissionReportsWithResponse(new ItemDetailsQueryFilter()
                .withDateRange(
                    new DateRange().withStart(LocalDate.parse("2024-05-01")).withEnd(LocalDate.parse("2024-05-01")))
                .withSubscriptionList(Arrays.asList("00000000-0000-0000-0000-000000000000",
                    "00000000-0000-0000-0000-000000000001,", "00000000-0000-0000-0000-000000000002",
                    "00000000-0000-0000-0000-000000000003", "00000000-0000-0000-0000-000000000004",
                    "00000000-0000-0000-0000-000000000005", "00000000-0000-0000-0000-000000000006",
                    "00000000-0000-0000-0000-000000000007", "00000000-0000-0000-0000-000000000008"))
                .withCarbonScopeList(Arrays.asList(EmissionScopeEnum.SCOPE1, EmissionScopeEnum.SCOPE3))
                .withCategoryType(CategoryTypeEnum.RESOURCE_GROUP)
                .withOrderBy(OrderByColumnEnum.LATEST_MONTH_EMISSIONS)
                .withSortDirection(SortDirectionEnum.DESC)
                .withPageSize(100), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-01/queryCarbonEmissionsTopNResourceItemsMonthlyReport.json
     */
    /**
     * Sample code: QueryCarbonEmission Top N resource monthly Report.
     * 
     * @param manager Entry point to CarbonOptimizationManager.
     */
    public static void queryCarbonEmissionTopNResourceMonthlyReport(
        com.azure.resourcemanager.carbonoptimization.CarbonOptimizationManager manager) {
        manager.carbonServices()
            .queryCarbonEmissionReportsWithResponse(new TopItemsMonthlySummaryReportQueryFilter()
                .withDateRange(
                    new DateRange().withStart(LocalDate.parse("2024-03-01")).withEnd(LocalDate.parse("2024-05-01")))
                .withSubscriptionList(Arrays.asList("00000000-0000-0000-0000-000000000000",
                    "00000000-0000-0000-0000-000000000001,", "00000000-0000-0000-0000-000000000002",
                    "00000000-0000-0000-0000-000000000003", "00000000-0000-0000-0000-000000000004",
                    "00000000-0000-0000-0000-000000000005", "00000000-0000-0000-0000-000000000006",
                    "00000000-0000-0000-0000-000000000007", "00000000-0000-0000-0000-000000000008"))
                .withCarbonScopeList(Arrays.asList(EmissionScopeEnum.SCOPE1, EmissionScopeEnum.SCOPE3))
                .withCategoryType(CategoryTypeEnum.RESOURCE)
                .withTopItems(2), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-01/queryCarbonEmissionsOverallSummaryReport.json
     */
    /**
     * Sample code: QueryCarbonEmission Overall Summary Report.
     * 
     * @param manager Entry point to CarbonOptimizationManager.
     */
    public static void queryCarbonEmissionOverallSummaryReport(
        com.azure.resourcemanager.carbonoptimization.CarbonOptimizationManager manager) {
        manager.carbonServices()
            .queryCarbonEmissionReportsWithResponse(
                new OverallSummaryReportQueryFilter()
                    .withDateRange(
                        new DateRange().withStart(LocalDate.parse("2023-06-01")).withEnd(LocalDate.parse("2023-06-01")))
                    .withSubscriptionList(Arrays.asList("00000000-0000-0000-0000-000000000000"))
                    .withCarbonScopeList(Arrays.asList(EmissionScopeEnum.SCOPE1, EmissionScopeEnum.SCOPE3)),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-01/queryCarbonEmissionsTopNSubscriptionItemsReport.json
     */
    /**
     * Sample code: QueryCarbonEmission Top N Subscriptions Report.
     * 
     * @param manager Entry point to CarbonOptimizationManager.
     */
    public static void queryCarbonEmissionTopNSubscriptionsReport(
        com.azure.resourcemanager.carbonoptimization.CarbonOptimizationManager manager) {
        manager.carbonServices()
            .queryCarbonEmissionReportsWithResponse(new TopItemsSummaryReportQueryFilter()
                .withDateRange(
                    new DateRange().withStart(LocalDate.parse("2024-05-01")).withEnd(LocalDate.parse("2024-05-01")))
                .withSubscriptionList(Arrays.asList("00000000-0000-0000-0000-000000000000",
                    "00000000-0000-0000-0000-000000000001,", "00000000-0000-0000-0000-000000000002",
                    "00000000-0000-0000-0000-000000000003", "00000000-0000-0000-0000-000000000004",
                    "00000000-0000-0000-0000-000000000005", "00000000-0000-0000-0000-000000000006",
                    "00000000-0000-0000-0000-000000000007", "00000000-0000-0000-0000-000000000008"))
                .withCarbonScopeList(Arrays.asList(EmissionScopeEnum.SCOPE1, EmissionScopeEnum.SCOPE3))
                .withCategoryType(CategoryTypeEnum.SUBSCRIPTION)
                .withTopItems(5), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-01/queryCarbonEmissionsTopNResourceGroupItemsReport.json
     */
    /**
     * Sample code: QueryCarbonEmission Top N ResourceGroup Report.
     * 
     * @param manager Entry point to CarbonOptimizationManager.
     */
    public static void queryCarbonEmissionTopNResourceGroupReport(
        com.azure.resourcemanager.carbonoptimization.CarbonOptimizationManager manager) {
        manager.carbonServices()
            .queryCarbonEmissionReportsWithResponse(new TopItemsSummaryReportQueryFilter()
                .withDateRange(
                    new DateRange().withStart(LocalDate.parse("2024-05-01")).withEnd(LocalDate.parse("2024-05-01")))
                .withSubscriptionList(Arrays.asList("00000000-0000-0000-0000-000000000000",
                    "00000000-0000-0000-0000-000000000001,", "00000000-0000-0000-0000-000000000002",
                    "00000000-0000-0000-0000-000000000003", "00000000-0000-0000-0000-000000000004",
                    "00000000-0000-0000-0000-000000000005", "00000000-0000-0000-0000-000000000006",
                    "00000000-0000-0000-0000-000000000007", "00000000-0000-0000-0000-000000000008"))
                .withCarbonScopeList(Arrays.asList(EmissionScopeEnum.SCOPE1, EmissionScopeEnum.SCOPE3))
                .withCategoryType(CategoryTypeEnum.RESOURCE_GROUP)
                .withTopItems(5), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-01/queryCarbonEmissionsResourceItemDetailsReport.json
     */
    /**
     * Sample code: QueryCarbonEmission resource item details Report.
     * 
     * @param manager Entry point to CarbonOptimizationManager.
     */
    public static void queryCarbonEmissionResourceItemDetailsReport(
        com.azure.resourcemanager.carbonoptimization.CarbonOptimizationManager manager) {
        manager.carbonServices()
            .queryCarbonEmissionReportsWithResponse(new ItemDetailsQueryFilter()
                .withDateRange(
                    new DateRange().withStart(LocalDate.parse("2024-05-01")).withEnd(LocalDate.parse("2024-05-01")))
                .withSubscriptionList(Arrays.asList("00000000-0000-0000-0000-000000000000",
                    "00000000-0000-0000-0000-000000000001,", "00000000-0000-0000-0000-000000000002",
                    "00000000-0000-0000-0000-000000000003", "00000000-0000-0000-0000-000000000004",
                    "00000000-0000-0000-0000-000000000005", "00000000-0000-0000-0000-000000000006",
                    "00000000-0000-0000-0000-000000000007", "00000000-0000-0000-0000-000000000008"))
                .withCarbonScopeList(Arrays.asList(EmissionScopeEnum.SCOPE1, EmissionScopeEnum.SCOPE3))
                .withCategoryType(CategoryTypeEnum.RESOURCE)
                .withOrderBy(OrderByColumnEnum.LATEST_MONTH_EMISSIONS)
                .withSortDirection(SortDirectionEnum.DESC)
                .withPageSize(100), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-01/queryCarbonEmissionsTopNSubscriptionItemsMonthlyReport.json
     */
    /**
     * Sample code: QueryCarbonEmission Top N Subscriptions monthly Report.
     * 
     * @param manager Entry point to CarbonOptimizationManager.
     */
    public static void queryCarbonEmissionTopNSubscriptionsMonthlyReport(
        com.azure.resourcemanager.carbonoptimization.CarbonOptimizationManager manager) {
        manager.carbonServices()
            .queryCarbonEmissionReportsWithResponse(new TopItemsMonthlySummaryReportQueryFilter()
                .withDateRange(
                    new DateRange().withStart(LocalDate.parse("2024-03-01")).withEnd(LocalDate.parse("2024-05-01")))
                .withSubscriptionList(Arrays.asList("00000000-0000-0000-0000-000000000000",
                    "00000000-0000-0000-0000-000000000001,", "00000000-0000-0000-0000-000000000002",
                    "00000000-0000-0000-0000-000000000003", "00000000-0000-0000-0000-000000000004",
                    "00000000-0000-0000-0000-000000000005", "00000000-0000-0000-0000-000000000006",
                    "00000000-0000-0000-0000-000000000007", "00000000-0000-0000-0000-000000000008"))
                .withCarbonScopeList(Arrays.asList(EmissionScopeEnum.SCOPE1, EmissionScopeEnum.SCOPE3))
                .withCategoryType(CategoryTypeEnum.SUBSCRIPTION)
                .withTopItems(2), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-01/queryCarbonEmissionsTopNLocationItemsReport.json
     */
    /**
     * Sample code: QueryCarbonEmission Top N Locations Report.
     * 
     * @param manager Entry point to CarbonOptimizationManager.
     */
    public static void queryCarbonEmissionTopNLocationsReport(
        com.azure.resourcemanager.carbonoptimization.CarbonOptimizationManager manager) {
        manager.carbonServices()
            .queryCarbonEmissionReportsWithResponse(new TopItemsSummaryReportQueryFilter()
                .withDateRange(
                    new DateRange().withStart(LocalDate.parse("2024-05-01")).withEnd(LocalDate.parse("2024-05-01")))
                .withSubscriptionList(Arrays.asList("00000000-0000-0000-0000-000000000000",
                    "00000000-0000-0000-0000-000000000001,", "00000000-0000-0000-0000-000000000002",
                    "00000000-0000-0000-0000-000000000003", "00000000-0000-0000-0000-000000000004",
                    "00000000-0000-0000-0000-000000000005", "00000000-0000-0000-0000-000000000006",
                    "00000000-0000-0000-0000-000000000007", "00000000-0000-0000-0000-000000000008"))
                .withCarbonScopeList(Arrays.asList(EmissionScopeEnum.SCOPE1, EmissionScopeEnum.SCOPE3))
                .withCategoryType(CategoryTypeEnum.LOCATION)
                .withTopItems(5), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-01/queryCarbonEmissionsResourceItemDetailsReportWithPaginationToken.json
     */
    /**
     * Sample code: QueryCarbonEmission resource item details Report with pagination token.
     * 
     * @param manager Entry point to CarbonOptimizationManager.
     */
    public static void queryCarbonEmissionResourceItemDetailsReportWithPaginationToken(
        com.azure.resourcemanager.carbonoptimization.CarbonOptimizationManager manager) {
        manager.carbonServices()
            .queryCarbonEmissionReportsWithResponse(new ItemDetailsQueryFilter()
                .withDateRange(
                    new DateRange().withStart(LocalDate.parse("2024-05-01")).withEnd(LocalDate.parse("2024-05-01")))
                .withSubscriptionList(Arrays.asList("00000000-0000-0000-0000-000000000000",
                    "00000000-0000-0000-0000-000000000001,", "00000000-0000-0000-0000-000000000002",
                    "00000000-0000-0000-0000-000000000003", "00000000-0000-0000-0000-000000000004",
                    "00000000-0000-0000-0000-000000000005", "00000000-0000-0000-0000-000000000006",
                    "00000000-0000-0000-0000-000000000007", "00000000-0000-0000-0000-000000000008"))
                .withCarbonScopeList(Arrays.asList(EmissionScopeEnum.SCOPE1, EmissionScopeEnum.SCOPE3))
                .withCategoryType(CategoryTypeEnum.RESOURCE)
                .withOrderBy(OrderByColumnEnum.LATEST_MONTH_EMISSIONS)
                .withSortDirection(SortDirectionEnum.DESC)
                .withPageSize(100)
                .withSkipToken("fakeTokenPlaceholder"), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-01/queryCarbonEmissionsLocationItemDetailsReport.json
     */
    /**
     * Sample code: QueryCarbonEmission Location item details Report.
     * 
     * @param manager Entry point to CarbonOptimizationManager.
     */
    public static void queryCarbonEmissionLocationItemDetailsReport(
        com.azure.resourcemanager.carbonoptimization.CarbonOptimizationManager manager) {
        manager.carbonServices()
            .queryCarbonEmissionReportsWithResponse(new ItemDetailsQueryFilter()
                .withDateRange(
                    new DateRange().withStart(LocalDate.parse("2024-05-01")).withEnd(LocalDate.parse("2024-05-01")))
                .withSubscriptionList(Arrays.asList("00000000-0000-0000-0000-000000000000",
                    "00000000-0000-0000-0000-000000000001,", "00000000-0000-0000-0000-000000000002",
                    "00000000-0000-0000-0000-000000000003", "00000000-0000-0000-0000-000000000004",
                    "00000000-0000-0000-0000-000000000005", "00000000-0000-0000-0000-000000000006",
                    "00000000-0000-0000-0000-000000000007", "00000000-0000-0000-0000-000000000008"))
                .withCarbonScopeList(Arrays.asList(EmissionScopeEnum.SCOPE1, EmissionScopeEnum.SCOPE3))
                .withCategoryType(CategoryTypeEnum.LOCATION)
                .withOrderBy(OrderByColumnEnum.LATEST_MONTH_EMISSIONS)
                .withSortDirection(SortDirectionEnum.DESC)
                .withPageSize(100), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-01/queryCarbonEmissionsMonthlySummaryReport.json
     */
    /**
     * Sample code: QueryCarbonEmission Overall Monthly Summary Report.
     * 
     * @param manager Entry point to CarbonOptimizationManager.
     */
    public static void queryCarbonEmissionOverallMonthlySummaryReport(
        com.azure.resourcemanager.carbonoptimization.CarbonOptimizationManager manager) {
        manager.carbonServices()
            .queryCarbonEmissionReportsWithResponse(
                new MonthlySummaryReportQueryFilter()
                    .withDateRange(
                        new DateRange().withStart(LocalDate.parse("2024-03-01")).withEnd(LocalDate.parse("2024-05-01")))
                    .withSubscriptionList(Arrays.asList("00000000-0000-0000-0000-000000000000"))
                    .withCarbonScopeList(Arrays.asList(EmissionScopeEnum.SCOPE1, EmissionScopeEnum.SCOPE3)),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-01/queryCarbonEmissionsOverallSummaryReportWithOtherOptionalFilter.json
     */
    /**
     * Sample code: QueryCarbonEmission Overall Summary Report with optional filter - locationList, resourceTypeList,
     * resourceGroupUrlList.
     * 
     * @param manager Entry point to CarbonOptimizationManager.
     */
    public static void
        queryCarbonEmissionOverallSummaryReportWithOptionalFilterLocationListResourceTypeListResourceGroupUrlList(
            com.azure.resourcemanager.carbonoptimization.CarbonOptimizationManager manager) {
        manager.carbonServices()
            .queryCarbonEmissionReportsWithResponse(
                new OverallSummaryReportQueryFilter()
                    .withDateRange(
                        new DateRange().withStart(LocalDate.parse("2023-06-01")).withEnd(LocalDate.parse("2023-06-01")))
                    .withSubscriptionList(Arrays.asList("00000000-0000-0000-0000-000000000000"))
                    .withResourceGroupUrlList(
                        Arrays.asList("/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/rg-name"))
                    .withResourceTypeList(
                        Arrays.asList("microsoft.storage/storageaccounts", "microsoft.databricks/workspaces"))
                    .withLocationList(Arrays.asList("east us", "west us"))
                    .withCarbonScopeList(Arrays.asList(EmissionScopeEnum.SCOPE1, EmissionScopeEnum.SCOPE3)),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-01/queryCarbonEmissionsTopNLocationItemsMonthlyReport.json
     */
    /**
     * Sample code: QueryCarbonEmission Top N Locations monthly Report.
     * 
     * @param manager Entry point to CarbonOptimizationManager.
     */
    public static void queryCarbonEmissionTopNLocationsMonthlyReport(
        com.azure.resourcemanager.carbonoptimization.CarbonOptimizationManager manager) {
        manager.carbonServices()
            .queryCarbonEmissionReportsWithResponse(new TopItemsMonthlySummaryReportQueryFilter()
                .withDateRange(
                    new DateRange().withStart(LocalDate.parse("2024-03-01")).withEnd(LocalDate.parse("2024-05-01")))
                .withSubscriptionList(Arrays.asList("00000000-0000-0000-0000-000000000000",
                    "00000000-0000-0000-0000-000000000001,", "00000000-0000-0000-0000-000000000002",
                    "00000000-0000-0000-0000-000000000003", "00000000-0000-0000-0000-000000000004",
                    "00000000-0000-0000-0000-000000000005", "00000000-0000-0000-0000-000000000006",
                    "00000000-0000-0000-0000-000000000007", "00000000-0000-0000-0000-000000000008"))
                .withCarbonScopeList(Arrays.asList(EmissionScopeEnum.SCOPE1, EmissionScopeEnum.SCOPE3))
                .withCategoryType(CategoryTypeEnum.LOCATION)
                .withTopItems(2), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-01/queryCarbonEmissionsTopNResourceTypeItemsMonthlyReport.json
     */
    /**
     * Sample code: QueryCarbonEmission Top N ResourceType monthly Report.
     * 
     * @param manager Entry point to CarbonOptimizationManager.
     */
    public static void queryCarbonEmissionTopNResourceTypeMonthlyReport(
        com.azure.resourcemanager.carbonoptimization.CarbonOptimizationManager manager) {
        manager.carbonServices()
            .queryCarbonEmissionReportsWithResponse(new TopItemsMonthlySummaryReportQueryFilter()
                .withDateRange(
                    new DateRange().withStart(LocalDate.parse("2024-03-01")).withEnd(LocalDate.parse("2024-05-01")))
                .withSubscriptionList(Arrays.asList("00000000-0000-0000-0000-000000000000",
                    "00000000-0000-0000-0000-000000000001,", "00000000-0000-0000-0000-000000000002",
                    "00000000-0000-0000-0000-000000000003", "00000000-0000-0000-0000-000000000004",
                    "00000000-0000-0000-0000-000000000005", "00000000-0000-0000-0000-000000000006",
                    "00000000-0000-0000-0000-000000000007", "00000000-0000-0000-0000-000000000008"))
                .withCarbonScopeList(Arrays.asList(EmissionScopeEnum.SCOPE1, EmissionScopeEnum.SCOPE3))
                .withCategoryType(CategoryTypeEnum.RESOURCE_TYPE)
                .withTopItems(2), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-01/queryCarbonEmissionsResourceTypeItemDetailsReport.json
     */
    /**
     * Sample code: QueryCarbonEmission ResourceType item details Report.
     * 
     * @param manager Entry point to CarbonOptimizationManager.
     */
    public static void queryCarbonEmissionResourceTypeItemDetailsReport(
        com.azure.resourcemanager.carbonoptimization.CarbonOptimizationManager manager) {
        manager.carbonServices()
            .queryCarbonEmissionReportsWithResponse(new ItemDetailsQueryFilter()
                .withDateRange(
                    new DateRange().withStart(LocalDate.parse("2024-05-01")).withEnd(LocalDate.parse("2024-05-01")))
                .withSubscriptionList(Arrays.asList("00000000-0000-0000-0000-000000000000",
                    "00000000-0000-0000-0000-000000000001,", "00000000-0000-0000-0000-000000000002",
                    "00000000-0000-0000-0000-000000000003", "00000000-0000-0000-0000-000000000004",
                    "00000000-0000-0000-0000-000000000005", "00000000-0000-0000-0000-000000000006",
                    "00000000-0000-0000-0000-000000000007", "00000000-0000-0000-0000-000000000008"))
                .withCarbonScopeList(Arrays.asList(EmissionScopeEnum.SCOPE1, EmissionScopeEnum.SCOPE3))
                .withCategoryType(CategoryTypeEnum.RESOURCE_TYPE)
                .withOrderBy(OrderByColumnEnum.LATEST_MONTH_EMISSIONS)
                .withSortDirection(SortDirectionEnum.DESC)
                .withPageSize(100), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-01/queryCarbonEmissionsTopNResourceGroupItemsMonthlyReport.json
     */
    /**
     * Sample code: QueryCarbonEmission Top N ResourceGroup monthly Report.
     * 
     * @param manager Entry point to CarbonOptimizationManager.
     */
    public static void queryCarbonEmissionTopNResourceGroupMonthlyReport(
        com.azure.resourcemanager.carbonoptimization.CarbonOptimizationManager manager) {
        manager.carbonServices()
            .queryCarbonEmissionReportsWithResponse(new TopItemsMonthlySummaryReportQueryFilter()
                .withDateRange(
                    new DateRange().withStart(LocalDate.parse("2024-03-01")).withEnd(LocalDate.parse("2024-05-01")))
                .withSubscriptionList(Arrays.asList("00000000-0000-0000-0000-000000000000",
                    "00000000-0000-0000-0000-000000000001,", "00000000-0000-0000-0000-000000000002",
                    "00000000-0000-0000-0000-000000000003", "00000000-0000-0000-0000-000000000004",
                    "00000000-0000-0000-0000-000000000005", "00000000-0000-0000-0000-000000000006",
                    "00000000-0000-0000-0000-000000000007", "00000000-0000-0000-0000-000000000008"))
                .withCarbonScopeList(Arrays.asList(EmissionScopeEnum.SCOPE1, EmissionScopeEnum.SCOPE3))
                .withCategoryType(CategoryTypeEnum.RESOURCE_GROUP)
                .withTopItems(2), com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-04-01/listOperations.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to CarbonOptimizationManager.
     */
    public static void operationsList(com.azure.resourcemanager.carbonoptimization.CarbonOptimizationManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

