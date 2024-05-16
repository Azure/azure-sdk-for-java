# Code snippets and samples


## AggregatedCost

- [GetByManagementGroup](#aggregatedcost_getbymanagementgroup)
- [GetForBillingPeriodByManagementGroup](#aggregatedcost_getforbillingperiodbymanagementgroup)

## Balances

- [GetByBillingAccount](#balances_getbybillingaccount)
- [GetForBillingPeriodByBillingAccount](#balances_getforbillingperiodbybillingaccount)

## Budgets

- [CreateOrUpdate](#budgets_createorupdate)
- [Delete](#budgets_delete)
- [Get](#budgets_get)
- [List](#budgets_list)

## Charges

- [List](#charges_list)

## Credits

- [Get](#credits_get)

## EventsOperation

- [ListByBillingAccount](#eventsoperation_listbybillingaccount)
- [ListByBillingProfile](#eventsoperation_listbybillingprofile)

## LotsOperation

- [ListByBillingAccount](#lotsoperation_listbybillingaccount)
- [ListByBillingProfile](#lotsoperation_listbybillingprofile)

## Marketplaces

- [List](#marketplaces_list)

## PriceSheet

- [Get](#pricesheet_get)
- [GetByBillingPeriod](#pricesheet_getbybillingperiod)

## ReservationRecommendationDetails

- [Get](#reservationrecommendationdetails_get)

## ReservationRecommendations

- [List](#reservationrecommendations_list)

## ReservationTransactions

- [List](#reservationtransactions_list)
- [ListByBillingProfile](#reservationtransactions_listbybillingprofile)

## ReservationsDetails

- [List](#reservationsdetails_list)
- [ListByReservationOrder](#reservationsdetails_listbyreservationorder)
- [ListByReservationOrderAndReservation](#reservationsdetails_listbyreservationorderandreservation)

## ReservationsSummaries

- [List](#reservationssummaries_list)
- [ListByReservationOrder](#reservationssummaries_listbyreservationorder)
- [ListByReservationOrderAndReservation](#reservationssummaries_listbyreservationorderandreservation)

## Tags

- [Get](#tags_get)

## UsageDetails

- [List](#usagedetails_list)
### AggregatedCost_GetByManagementGroup

```java
import com.azure.core.util.Context;

/** Samples for AggregatedCost GetByManagementGroup. */
public final class AggregatedCostGetByManagementGroupSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/AggregatedCostByManagementGroupFilterByDate.json
     */
    /**
     * Sample code: AggregatedCostByManagementGroupFilterByDate.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void aggregatedCostByManagementGroupFilterByDate(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .aggregatedCosts()
            .getByManagementGroupWithResponse(
                "managementGroupForTest",
                "usageStart ge '2018-08-15' and properties/usageStart le '2018-08-31'",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/AggregatedCostByManagementGroup.json
     */
    /**
     * Sample code: AggregatedCostByManagementGroup.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void aggregatedCostByManagementGroup(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager.aggregatedCosts().getByManagementGroupWithResponse("managementGroupForTest", null, Context.NONE);
    }
}
```

### AggregatedCost_GetForBillingPeriodByManagementGroup

```java
import com.azure.core.util.Context;

/** Samples for AggregatedCost GetForBillingPeriodByManagementGroup. */
public final class AggregatedCostGetForBillingPeriodByManagementGroupSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/AggregatedCostForBillingPeriodByManagementGroup.json
     */
    /**
     * Sample code: AggregatedCostListForBillingPeriodByManagementGroup.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void aggregatedCostListForBillingPeriodByManagementGroup(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .aggregatedCosts()
            .getForBillingPeriodByManagementGroupWithResponse("managementGroupForTest", "201807", Context.NONE);
    }
}
```

### Balances_GetByBillingAccount

```java
import com.azure.core.util.Context;

/** Samples for Balances GetByBillingAccount. */
public final class BalancesGetByBillingAccountSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/BalancesByBillingAccount.json
     */
    /**
     * Sample code: Balances.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void balances(com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager.balances().getByBillingAccountWithResponse("123456", Context.NONE);
    }
}
```

### Balances_GetForBillingPeriodByBillingAccount

```java
import com.azure.core.util.Context;

/** Samples for Balances GetForBillingPeriodByBillingAccount. */
public final class BalancesGetForBillingPeriodByBillingAccountSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/BalancesByBillingAccountForBillingPeriod.json
     */
    /**
     * Sample code: Balances.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void balances(com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager.balances().getForBillingPeriodByBillingAccountWithResponse("123456", "201702", Context.NONE);
    }
}
```

### Budgets_CreateOrUpdate

```java
import com.azure.resourcemanager.consumption.models.BudgetComparisonExpression;
import com.azure.resourcemanager.consumption.models.BudgetFilter;
import com.azure.resourcemanager.consumption.models.BudgetFilterProperties;
import com.azure.resourcemanager.consumption.models.BudgetOperatorType;
import com.azure.resourcemanager.consumption.models.BudgetTimePeriod;
import com.azure.resourcemanager.consumption.models.CategoryType;
import com.azure.resourcemanager.consumption.models.CultureCode;
import com.azure.resourcemanager.consumption.models.Notification;
import com.azure.resourcemanager.consumption.models.OperatorType;
import com.azure.resourcemanager.consumption.models.ThresholdType;
import com.azure.resourcemanager.consumption.models.TimeGrainType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Budgets CreateOrUpdate. */
public final class BudgetsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/CreateOrUpdateBudget.json
     */
    /**
     * Sample code: CreateOrUpdateBudget.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void createOrUpdateBudget(com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .budgets()
            .define("TestBudget")
            .withExistingScope("subscriptions/00000000-0000-0000-0000-000000000000")
            .withEtag("\"1d34d016a593709\"")
            .withCategory(CategoryType.COST)
            .withAmount(new BigDecimal("100.65"))
            .withTimeGrain(TimeGrainType.MONTHLY)
            .withTimePeriod(
                new BudgetTimePeriod()
                    .withStartDate(OffsetDateTime.parse("2017-10-01T00:00:00Z"))
                    .withEndDate(OffsetDateTime.parse("2018-10-31T00:00:00Z")))
            .withFilter(
                new BudgetFilter()
                    .withAnd(
                        Arrays
                            .asList(
                                new BudgetFilterProperties()
                                    .withDimensions(
                                        new BudgetComparisonExpression()
                                            .withName("ResourceId")
                                            .withOperator(BudgetOperatorType.IN)
                                            .withValues(
                                                Arrays
                                                    .asList(
                                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/MYDEVTESTRG/providers/Microsoft.Compute/virtualMachines/MSVM2",
                                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/MYDEVTESTRG/providers/Microsoft.Compute/virtualMachines/platformcloudplatformGeneric1"))),
                                new BudgetFilterProperties()
                                    .withTags(
                                        new BudgetComparisonExpression()
                                            .withName("category")
                                            .withOperator(BudgetOperatorType.IN)
                                            .withValues(Arrays.asList("Dev", "Prod"))),
                                new BudgetFilterProperties()
                                    .withTags(
                                        new BudgetComparisonExpression()
                                            .withName("department")
                                            .withOperator(BudgetOperatorType.IN)
                                            .withValues(Arrays.asList("engineering", "sales"))))))
            .withNotifications(
                mapOf(
                    "Actual_GreaterThan_80_Percent",
                    new Notification()
                        .withEnabled(true)
                        .withOperator(OperatorType.GREATER_THAN)
                        .withThreshold(new BigDecimal("80"))
                        .withContactEmails(Arrays.asList("johndoe@contoso.com", "janesmith@contoso.com"))
                        .withContactRoles(Arrays.asList("Contributor", "Reader"))
                        .withContactGroups(
                            Arrays
                                .asList(
                                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/MYDEVTESTRG/providers/microsoft.insights/actionGroups/SampleActionGroup"))
                        .withThresholdType(ThresholdType.ACTUAL)
                        .withLocale(CultureCode.EN_US)))
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

### Budgets_Delete

```java
import com.azure.core.util.Context;

/** Samples for Budgets Delete. */
public final class BudgetsDeleteSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/DeleteBudget.json
     */
    /**
     * Sample code: DeleteBudget.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void deleteBudget(com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .budgets()
            .deleteWithResponse("subscriptions/00000000-0000-0000-0000-000000000000", "TestBudget", Context.NONE);
    }
}
```

### Budgets_Get

```java
import com.azure.core.util.Context;

/** Samples for Budgets Get. */
public final class BudgetsGetSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/Budget.json
     */
    /**
     * Sample code: Budget.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void budget(com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .budgets()
            .getWithResponse("subscriptions/00000000-0000-0000-0000-000000000000", "TestBudget", Context.NONE);
    }
}
```

### Budgets_List

```java
import com.azure.core.util.Context;

/** Samples for Budgets List. */
public final class BudgetsListSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/BudgetsList.json
     */
    /**
     * Sample code: BudgetsList.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void budgetsList(com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager.budgets().list("subscriptions/00000000-0000-0000-0000-000000000000", Context.NONE);
    }
}
```

### Charges_List

```java
import com.azure.core.util.Context;

/** Samples for Charges List. */
public final class ChargesListSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ChargesListByModernBillingAccount.json
     */
    /**
     * Sample code: ChargesListByBillingAccount-Modern.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void chargesListByBillingAccountModern(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .charges()
            .listWithResponse(
                "providers/Microsoft.Billing/billingAccounts/1234:56789",
                "2019-09-01",
                "2019-10-31",
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ChargesForBillingPeriodByEnrollmentAccount.json
     */
    /**
     * Sample code: ChangesForBillingPeriodByEnrollmentAccount-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void changesForBillingPeriodByEnrollmentAccountLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .charges()
            .listWithResponse(
                "providers/Microsoft.Billing/BillingAccounts/1234/enrollmentAccounts/42425",
                null,
                null,
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ChargesListByModernBillingAccountGroupByCustomerId.json
     */
    /**
     * Sample code: ChargesListByBillingAccountGroupByCustomerId-Modern.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void chargesListByBillingAccountGroupByCustomerIdModern(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .charges()
            .listWithResponse(
                "providers/Microsoft.Billing/billingAccounts/1234:56789",
                "2019-09-01",
                "2019-09-30",
                null,
                "groupby((properties/customerId))",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ChargesListByModernBillingProfile.json
     */
    /**
     * Sample code: ChargesListByBillingProfile-Modern.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void chargesListByBillingProfileModern(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .charges()
            .listWithResponse(
                "providers/Microsoft.Billing/BillingAccounts/1234:56789/billingProfiles/2460",
                null,
                null,
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ChargesListByModernBillingProfileInvoiceSection.json
     */
    /**
     * Sample code: ChargesListByBillingProfileInvoiceSection-Modern.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void chargesListByBillingProfileInvoiceSectionModern(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .charges()
            .listWithResponse(
                "providers/Microsoft.Billing/billingAccounts/1234:56789/billingProfiles/42425/invoiceSections/67890",
                "2019-09-01",
                "2019-10-31",
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ChargesListByModernCustomer.json
     */
    /**
     * Sample code: ChargesListByCustomer-Modern.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void chargesListByCustomerModern(com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .charges()
            .listWithResponse(
                "providers/Microsoft.Billing/BillingAccounts/1234:56789/customers/67890",
                null,
                null,
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ChargesListForDepartmentFilterByStartEndDate.json
     */
    /**
     * Sample code: ChargesListByDepartment-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void chargesListByDepartmentLegacy(com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .charges()
            .listWithResponse(
                "providers/Microsoft.Billing/BillingAccounts/1234/departments/42425",
                null,
                null,
                "usageStart eq '2018-04-01' AND usageEnd eq '2018-05-30'",
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ChargesListForEnrollmentAccountFilterByStartEndDate.json
     */
    /**
     * Sample code: ChargesListForEnrollmentAccount-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void chargesListForEnrollmentAccountLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .charges()
            .listWithResponse(
                "providers/Microsoft.Billing/BillingAccounts/1234/enrollmentAccounts/42425",
                null,
                null,
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ChargesListByModernBillingProfileGroupByInvoiceSectionId.json
     */
    /**
     * Sample code: ChargesListByBillingProfileGroupByInvoiceSectionId-Modern.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void chargesListByBillingProfileGroupByInvoiceSectionIdModern(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .charges()
            .listWithResponse(
                "providers/Microsoft.Billing/billingAccounts/1234:56789/billingProfiles/42425",
                "2019-09-01",
                "2019-09-30",
                null,
                "groupby((properties/invoiceSectionId))",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ChargesListByModernBillingAccountGroupByBillingProfileId.json
     */
    /**
     * Sample code: ChargesListByBillingAccountGroupByBillingProfileId-Modern.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void chargesListByBillingAccountGroupByBillingProfileIdModern(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .charges()
            .listWithResponse(
                "providers/Microsoft.Billing/billingAccounts/1234:56789",
                "2019-09-01",
                "2019-09-30",
                null,
                "groupby((properties/billingProfileId))",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ChargesListByModernBillingAccountGroupByInvoiceSectionId.json
     */
    /**
     * Sample code: ChargesListByBillingAccountGroupByInvoiceSectionId-Modern.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void chargesListByBillingAccountGroupByInvoiceSectionIdModern(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .charges()
            .listWithResponse(
                "providers/Microsoft.Billing/billingAccounts/1234:56789/billingProfiles/42425",
                "2019-09-01",
                "2019-09-30",
                null,
                "groupby((properties/invoiceSectionId))",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ChargesForBillingPeriodByDepartment.json
     */
    /**
     * Sample code: ChangesForBillingPeriodByDepartment-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void changesForBillingPeriodByDepartmentLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .charges()
            .listWithResponse(
                "providers/Microsoft.Billing/BillingAccounts/1234/departments/42425",
                null,
                null,
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ChargesListByModernInvoiceSectionId.json
     */
    /**
     * Sample code: ChargesListByInvoiceSectionId-Modern.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void chargesListByInvoiceSectionIdModern(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .charges()
            .listWithResponse(
                "providers/Microsoft.Billing/BillingAccounts/1234:56789/invoiceSections/97531",
                null,
                null,
                null,
                null,
                Context.NONE);
    }
}
```

### Credits_Get

```java
import com.azure.core.util.Context;

/** Samples for Credits Get. */
public final class CreditsGetSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/CreditSummaryByBillingProfile.json
     */
    /**
     * Sample code: CreditSummaryByBillingProfile.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void creditSummaryByBillingProfile(com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager.credits().getWithResponse("1234:5678", "2468", Context.NONE);
    }
}
```

### EventsOperation_ListByBillingAccount

```java
import com.azure.core.util.Context;

/** Samples for EventsOperation ListByBillingAccount. */
public final class EventsOperationListByBillingAccountSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/EventsGetByBillingAccount.json
     */
    /**
     * Sample code: EventsGetByBillingAccount.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void eventsGetByBillingAccount(com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager.eventsOperations().listByBillingAccount("1234:5678", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/EventsGetByBillingAccountWithFilters.json
     */
    /**
     * Sample code: EventsGetByBillingAccountWithFilters.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void eventsGetByBillingAccountWithFilters(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .eventsOperations()
            .listByBillingAccount(
                "1234:5678", "lotid eq 'G202001083926600XXXXX' AND lotsource eq 'consumptioncommitment'", Context.NONE);
    }
}
```

### EventsOperation_ListByBillingProfile

```java
import com.azure.core.util.Context;

/** Samples for EventsOperation ListByBillingProfile. */
public final class EventsOperationListByBillingProfileSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/EventsListByBillingProfile.json
     */
    /**
     * Sample code: EventsListByBillingProfile.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void eventsListByBillingProfile(com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager.eventsOperations().listByBillingProfile("1234:5678", "4268", "2019-09-01", "2019-10-31", Context.NONE);
    }
}
```

### LotsOperation_ListByBillingAccount

```java
import com.azure.core.util.Context;

/** Samples for LotsOperation ListByBillingAccount. */
public final class LotsOperationListByBillingAccountSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/LotsListByBillingAccount.json
     */
    /**
     * Sample code: LotsListByBillingAccount.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void lotsListByBillingAccount(com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager.lotsOperations().listByBillingAccount("1234:5678", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/LotsListByBillingAccountWithFilters.json
     */
    /**
     * Sample code: LotsListByBillingAccountWithStatusFilter.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void lotsListByBillingAccountWithStatusFilter(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .lotsOperations()
            .listByBillingAccount(
                "1234:5678", "status eq 'active' AND source eq 'consumptioncommitment'", Context.NONE);
    }
}
```

### LotsOperation_ListByBillingProfile

```java
import com.azure.core.util.Context;

/** Samples for LotsOperation ListByBillingProfile. */
public final class LotsOperationListByBillingProfileSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/LotsListByBillingProfile.json
     */
    /**
     * Sample code: LotsListByBillingProfile.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void lotsListByBillingProfile(com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager.lotsOperations().listByBillingProfile("1234:5678", "2468", Context.NONE);
    }
}
```

### Marketplaces_List

```java
import com.azure.core.util.Context;

/** Samples for Marketplaces List. */
public final class MarketplacesListSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/MarketplacesByBillingAccountListForBillingPeriod.json
     */
    /**
     * Sample code: BillingAccountMarketplacesListForBillingPeriod.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void billingAccountMarketplacesListForBillingPeriod(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .marketplaces()
            .list("providers/Microsoft.Billing/billingAccounts/123456", null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/MarketplacesByEnrollmentAccounts_ListByBillingPeriod.json
     */
    /**
     * Sample code: EnrollmentAccountMarketplacesListForBillingPeriod.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void enrollmentAccountMarketplacesListForBillingPeriod(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .marketplaces()
            .list("providers/Microsoft.Billing/enrollmentAccounts/123456", null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/MarketplacesList.json
     */
    /**
     * Sample code: SubscriptionMarketplacesList.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void subscriptionMarketplacesList(com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .marketplaces()
            .list("subscriptions/00000000-0000-0000-0000-000000000000", null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/MarketplacesByEnrollmentAccountList.json
     */
    /**
     * Sample code: EnrollmentAccountMarketplacesList.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void enrollmentAccountMarketplacesList(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .marketplaces()
            .list("providers/Microsoft.Billing/enrollmentAccounts/123456", null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/MarketplacesByDepartment_ListByBillingPeriod.json
     */
    /**
     * Sample code: DepartmentMarketplacesListForBillingPeriod.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void departmentMarketplacesListForBillingPeriod(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager.marketplaces().list("providers/Microsoft.Billing/departments/123456", null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/MarketplacesByManagementGroupList.json
     */
    /**
     * Sample code: ManagementGroupMarketplacesList.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void managementGroupMarketplacesList(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .marketplaces()
            .list("subscriptions/00000000-0000-0000-0000-000000000000", null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/MarketplacesListForBillingPeriod.json
     */
    /**
     * Sample code: SubscriptionMarketplacesListForBillingPeriod.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void subscriptionMarketplacesListForBillingPeriod(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .marketplaces()
            .list("subscriptions/00000000-0000-0000-0000-000000000000", null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/MarketplacesByDepartmentList.json
     */
    /**
     * Sample code: DepartmentMarketplacesList.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void departmentMarketplacesList(com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager.marketplaces().list("providers/Microsoft.Billing/departments/123456", null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/MarketplacesByManagementGroup_ListForBillingPeriod.json
     */
    /**
     * Sample code: ManagementGroupMarketplacesListForBillingPeriod.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void managementGroupMarketplacesListForBillingPeriod(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .marketplaces()
            .list("subscriptions/00000000-0000-0000-0000-000000000000", null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/MarketplacesByBillingAccountList.json
     */
    /**
     * Sample code: BillingAccountMarketplacesList.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void billingAccountMarketplacesList(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .marketplaces()
            .list("providers/Microsoft.Billing/billingAccounts/123456", null, null, null, Context.NONE);
    }
}
```

### PriceSheet_Get

```java
import com.azure.core.util.Context;

/** Samples for PriceSheet Get. */
public final class PriceSheetGetSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/PriceSheet.json
     */
    /**
     * Sample code: PriceSheet.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void priceSheet(com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager.priceSheets().getWithResponse(null, null, null, Context.NONE);
    }
}
```

### PriceSheet_GetByBillingPeriod

```java
import com.azure.core.util.Context;

/** Samples for PriceSheet GetByBillingPeriod. */
public final class PriceSheetGetByBillingPeriodSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/PriceSheetForBillingPeriod.json
     */
    /**
     * Sample code: PriceSheetForBillingPeriod.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void priceSheetForBillingPeriod(com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager.priceSheets().getByBillingPeriodWithResponse("201801", null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/PriceSheetExpand.json
     */
    /**
     * Sample code: PriceSheetExpand.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void priceSheetExpand(com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager.priceSheets().getByBillingPeriodWithResponse("201801", "meterDetails", null, null, Context.NONE);
    }
}
```

### ReservationRecommendationDetails_Get

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.consumption.models.LookBackPeriod;
import com.azure.resourcemanager.consumption.models.Term;

/** Samples for ReservationRecommendationDetails Get. */
public final class ReservationRecommendationDetailsGetSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationRecommendationDetailsBySubscription.json
     */
    /**
     * Sample code: ReservationRecommendationsBySubscription-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationRecommendationsBySubscriptionLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationRecommendationDetails()
            .getWithResponse("Single", "westus", Term.P3Y, LookBackPeriod.LAST30DAYS, "Standard_DS13_v2", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationRecommendationDetailsByBillingProfile.json
     */
    /**
     * Sample code: ReservationRecommendationsByBillingProfile-Modern.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationRecommendationsByBillingProfileModern(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationRecommendationDetails()
            .getWithResponse(
                "Shared", "australiaeast", Term.P1Y, LookBackPeriod.LAST7DAYS, "Standard_B2s", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationRecommendationDetailsByResourceGroup.json
     */
    /**
     * Sample code: ReservationRecommendationsByResourceGroup-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationRecommendationsByResourceGroupLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationRecommendationDetails()
            .getWithResponse("Single", "westus", Term.P3Y, LookBackPeriod.LAST30DAYS, "Standard_DS13_v2", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationRecommendationDetailsByBillingAccount.json
     */
    /**
     * Sample code: ReservationRecommendationsByBillingAccount-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationRecommendationsByBillingAccountLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationRecommendationDetails()
            .getWithResponse("Shared", "eastus", Term.P1Y, LookBackPeriod.LAST60DAYS, "Standard_DS14_v2", Context.NONE);
    }
}
```

### ReservationRecommendations_List

```java
import com.azure.core.util.Context;

/** Samples for ReservationRecommendations List. */
public final class ReservationRecommendationsListSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationRecommendationsBySubscription.json
     */
    /**
     * Sample code: ReservationRecommendationsBySubscription-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationRecommendationsBySubscriptionLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationRecommendations()
            .list("subscriptions/00000000-0000-0000-0000-000000000000", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationRecommendationsByBillingProfile.json
     */
    /**
     * Sample code: ReservationRecommendationsByBillingProfile-Modern.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationRecommendationsByBillingProfileModern(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationRecommendations()
            .list("providers/Microsoft.Billing/billingAccounts/123456/billingProfiles/6420", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationRecommendationsByResourceGroup.json
     */
    /**
     * Sample code: ReservationRecommendationsByResourceGroup-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationRecommendationsByResourceGroupLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationRecommendations()
            .list("subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testGroup", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationRecommendationsFilterBySubscriptionForScopeLookBackPeriod.json
     */
    /**
     * Sample code: ReservationRecommendationsFilterBySubscriptionForScopeLookBackPeriod-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationRecommendationsFilterBySubscriptionForScopeLookBackPeriodLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationRecommendations()
            .list(
                "subscriptions/00000000-0000-0000-0000-000000000000",
                "properties/scope eq 'Single' AND properties/lookBackPeriod eq 'Last7Days'",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationRecommendationsByBillingAccount.json
     */
    /**
     * Sample code: ReservationRecommendationsByBillingAccount-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationRecommendationsByBillingAccountLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationRecommendations()
            .list("providers/Microsoft.Billing/billingAccounts/123456", null, Context.NONE);
    }
}
```

### ReservationTransactions_List

```java
import com.azure.core.util.Context;

/** Samples for ReservationTransactions List. */
public final class ReservationTransactionsListSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationTransactionsListByEnrollmentNumber.json
     */
    /**
     * Sample code: ReservationTransactionsByEnrollmentNumber.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationTransactionsByEnrollmentNumber(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationTransactions()
            .list("123456", "properties/eventDate ge 2020-05-20 AND properties/eventDate le 2020-05-30", Context.NONE);
    }
}
```

### ReservationTransactions_ListByBillingProfile

```java
import com.azure.core.util.Context;

/** Samples for ReservationTransactions ListByBillingProfile. */
public final class ReservationTransactionsListByBillingProfileSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationTransactionsListByBillingProfileId.json
     */
    /**
     * Sample code: ReservationTransactionsByBillingProfileId.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationTransactionsByBillingProfileId(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationTransactions()
            .listByBillingProfile(
                "fcebaabc-fced-4284-a83d-79f83dee183c:45796ba8-988f-45ad-bea9-7b71fc6c7513_2018-09-30",
                "Z76D-SGAF-BG7-TGB",
                "properties/eventDate ge 2020-05-20 AND properties/eventDate le 2020-05-30",
                Context.NONE);
    }
}
```

### ReservationsDetails_List

```java
import com.azure.core.util.Context;

/** Samples for ReservationsDetails List. */
public final class ReservationsDetailsListSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationDetailsByBillingProfileIdReservationId.json
     */
    /**
     * Sample code: ReservationDetailsByBillingProfileIdReservationId.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationDetailsByBillingProfileIdReservationId(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationsDetails()
            .list(
                "providers/Microsoft.Billing/billingAccounts/12345:2468/billingProfiles/13579",
                "2019-09-01",
                "2019-10-31",
                null,
                "1c6b6358-709f-484c-85f1-72e862a0cf3b",
                "9f39ba10-794f-4dcb-8f4b-8d0cb47c27dc",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationDetailsByBillingAccountId.json
     */
    /**
     * Sample code: ReservationDetailsByBillingAccountId.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationDetailsByBillingAccountId(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationsDetails()
            .list(
                "providers/Microsoft.Billing/billingAccounts/12345",
                null,
                null,
                "properties/usageDate ge 2017-10-01 AND properties/usageDate le 2017-12-05",
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationDetailsByBillingProfileId.json
     */
    /**
     * Sample code: ReservationDetailsByBillingProfileId.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationDetailsByBillingProfileId(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationsDetails()
            .list(
                "providers/Microsoft.Billing/billingAccounts/12345:2468/billingProfiles/13579",
                "2019-09-01",
                "2019-10-31",
                null,
                null,
                null,
                Context.NONE);
    }
}
```

### ReservationsDetails_ListByReservationOrder

```java
import com.azure.core.util.Context;

/** Samples for ReservationsDetails ListByReservationOrder. */
public final class ReservationsDetailsListByReservationOrderSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationDetails.json
     */
    /**
     * Sample code: ReservationDetails.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationDetails(com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationsDetails()
            .listByReservationOrder(
                "00000000-0000-0000-0000-000000000000",
                "properties/usageDate ge 2017-10-01 AND properties/usageDate le 2017-12-05",
                Context.NONE);
    }
}
```

### ReservationsDetails_ListByReservationOrderAndReservation

```java
import com.azure.core.util.Context;

/** Samples for ReservationsDetails ListByReservationOrderAndReservation. */
public final class ReservationsDetailsListByReservationOrderAndReservationSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationDetailsWithReservationId.json
     */
    /**
     * Sample code: ReservationDetailsWithReservationId.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationDetailsWithReservationId(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationsDetails()
            .listByReservationOrderAndReservation(
                "00000000-0000-0000-0000-000000000000",
                "00000000-0000-0000-0000-000000000000",
                "properties/usageDate ge 2017-10-01 AND properties/usageDate le 2017-12-05",
                Context.NONE);
    }
}
```

### ReservationsSummaries_List

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.consumption.models.Datagrain;

/** Samples for ReservationsSummaries List. */
public final class ReservationsSummariesListSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationSummariesDailyWithBillingAccountId.json
     */
    /**
     * Sample code: ReservationSummariesDailyWithBillingAccountId.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationSummariesDailyWithBillingAccountId(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationsSummaries()
            .list(
                "providers/Microsoft.Billing/billingAccounts/12345",
                Datagrain.DAILY,
                null,
                null,
                "properties/usageDate ge 2017-10-01 AND properties/usageDate le 2017-11-20",
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationSummariesDailyWithBillingProfileId.json
     */
    /**
     * Sample code: ReservationSummariesDailyWithBillingProfileId.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationSummariesDailyWithBillingProfileId(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationsSummaries()
            .list(
                "providers/Microsoft.Billing/billingAccounts/12345:2468/billingProfiles/13579",
                Datagrain.DAILY,
                "2017-10-01",
                "2017-11-20",
                null,
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationSummariesMonthlyWithBillingAccountId.json
     */
    /**
     * Sample code: ReservationSummariesMonthlyWithBillingAccountId.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationSummariesMonthlyWithBillingAccountId(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationsSummaries()
            .list(
                "providers/Microsoft.Billing/billingAccounts/12345",
                Datagrain.MONTHLY,
                null,
                null,
                null,
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationSummariesMonthlyWithBillingProfileId.json
     */
    /**
     * Sample code: ReservationSummariesMonthlyWithBillingProfileId.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationSummariesMonthlyWithBillingProfileId(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationsSummaries()
            .list(
                "providers/Microsoft.Billing/billingAccounts/12345:2468/billingProfiles/13579",
                Datagrain.MONTHLY,
                null,
                null,
                null,
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationSummariesMonthlyWithBillingProfileIdReservationId.json
     */
    /**
     * Sample code: ReservationSummariesMonthlyWithBillingProfileIdReservationId.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationSummariesMonthlyWithBillingProfileIdReservationId(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationsSummaries()
            .list(
                "providers/Microsoft.Billing/billingAccounts/12345:2468/billingProfiles/13579",
                Datagrain.MONTHLY,
                null,
                null,
                null,
                "1c6b6358-709f-484c-85f1-72e862a0cf3b",
                "9f39ba10-794f-4dcb-8f4b-8d0cb47c27dc",
                Context.NONE);
    }
}
```

### ReservationsSummaries_ListByReservationOrder

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.consumption.models.Datagrain;

/** Samples for ReservationsSummaries ListByReservationOrder. */
public final class ReservationsSummariesListByReservationOrderSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationSummariesMonthly.json
     */
    /**
     * Sample code: ReservationSummariesMonthly.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationSummariesMonthly(com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationsSummaries()
            .listByReservationOrder("00000000-0000-0000-0000-000000000000", Datagrain.MONTHLY, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationSummariesDaily.json
     */
    /**
     * Sample code: ReservationSummariesDaily.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationSummariesDaily(com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationsSummaries()
            .listByReservationOrder(
                "00000000-0000-0000-0000-000000000000",
                Datagrain.DAILY,
                "properties/usageDate ge 2017-10-01 AND properties/usageDate le 2017-11-20",
                Context.NONE);
    }
}
```

### ReservationsSummaries_ListByReservationOrderAndReservation

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.consumption.models.Datagrain;

/** Samples for ReservationsSummaries ListByReservationOrderAndReservation. */
public final class ReservationsSummariesListByReservationOrderAndReservationSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationSummariesDailyWithReservationId.json
     */
    /**
     * Sample code: ReservationSummariesDailyWithReservationId.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationSummariesDailyWithReservationId(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationsSummaries()
            .listByReservationOrderAndReservation(
                "00000000-0000-0000-0000-000000000000",
                "00000000-0000-0000-0000-000000000000",
                Datagrain.DAILY,
                "properties/usageDate ge 2017-10-01 AND properties/usageDate le 2017-11-20",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/ReservationSummariesMonthlyWithReservationId.json
     */
    /**
     * Sample code: ReservationSummariesMonthlyWithReservationId.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void reservationSummariesMonthlyWithReservationId(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .reservationsSummaries()
            .listByReservationOrderAndReservation(
                "00000000-0000-0000-0000-000000000000",
                "00000000-0000-0000-0000-000000000000",
                Datagrain.MONTHLY,
                null,
                Context.NONE);
    }
}
```

### Tags_Get

```java
import com.azure.core.util.Context;

/** Samples for Tags Get. */
public final class TagsGetSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/Tags.json
     */
    /**
     * Sample code: Tags_Get.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void tagsGet(com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager.tags().getWithResponse("providers/Microsoft.CostManagement/billingAccounts/1234", Context.NONE);
    }
}
```

### UsageDetails_List

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.consumption.models.Metrictype;

/** Samples for UsageDetails List. */
public final class UsageDetailsListSamples {
    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/UsageDetailsListForBillingPeriodByBillingAccount.json
     */
    /**
     * Sample code: BillingAccountUsageDetailsListForBillingPeriod-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void billingAccountUsageDetailsListForBillingPeriodLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .usageDetails()
            .list("providers/Microsoft.Billing/BillingAccounts/1234", null, null, null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/UsageDetailsListByMCAInvoiceSection.json
     */
    /**
     * Sample code: InvoiceSectionUsageDetailsList-Modern.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void invoiceSectionUsageDetailsListModern(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
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

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/UsageDetailsList.json
     */
    /**
     * Sample code: UsageDetailsList-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void usageDetailsListLegacy(com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .usageDetails()
            .list("subscriptions/00000000-0000-0000-0000-000000000000", null, null, null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/UsageDetailsListFilterByTag.json
     */
    /**
     * Sample code: UsageDetailsListFilterByTag-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void usageDetailsListFilterByTagLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
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

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/UsageDetailsListByBillingAccount.json
     */
    /**
     * Sample code: BillingAccountUsageDetailsList-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void billingAccountUsageDetailsListLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .usageDetails()
            .list("providers/Microsoft.Billing/BillingAccounts/1234", null, null, null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/UsageDetailsListByMCABillingProfile.json
     */
    /**
     * Sample code: BillingProfileUsageDetailsList-Modern.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void billingProfileUsageDetailsListModern(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
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

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/UsageDetailsListForBillingPeriodByDepartment.json
     */
    /**
     * Sample code: DepartmentUsageDetailsListForBillingPeriod-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void departmentUsageDetailsListForBillingPeriodLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .usageDetails()
            .list("providers/Microsoft.Billing/Departments/1234", null, null, null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/UsageDetailsListForBillingPeriodByManagementGroup.json
     */
    /**
     * Sample code: ManagementGroupUsageDetailsListForBillingPeriod-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void managementGroupUsageDetailsListForBillingPeriodLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .usageDetails()
            .list("subscriptions/00000000-0000-0000-0000-000000000000", null, null, null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/UsageDetailsListByMetricAmortizedCost.json
     */
    /**
     * Sample code: UsageDetailsListByMetricAmortizedCost-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void usageDetailsListByMetricAmortizedCostLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
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

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/UsageDetailsListByMetricUsage.json
     */
    /**
     * Sample code: UsageDetailsListByMetricUsage-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void usageDetailsListByMetricUsageLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
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

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/UsageDetailsListByManagementGroup.json
     */
    /**
     * Sample code: ManagementGroupUsageDetailsList-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void managementGroupUsageDetailsListLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .usageDetails()
            .list("subscriptions/00000000-0000-0000-0000-000000000000", null, null, null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/UsageDetailsListByDepartment.json
     */
    /**
     * Sample code: DepartmentUsageDetailsList-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void departmentUsageDetailsListLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .usageDetails()
            .list("providers/Microsoft.Billing/Departments/1234", null, null, null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/UsageDetailsListByMetricActualCost.json
     */
    /**
     * Sample code: UsageDetailsListByMetricActualCost-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void usageDetailsListByMetricActualCostLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
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

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/UsageDetailsListByEnrollmentAccount.json
     */
    /**
     * Sample code: EnrollmentAccountUsageDetailsList-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void enrollmentAccountUsageDetailsListLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .usageDetails()
            .list("providers/Microsoft.Billing/EnrollmentAccounts/1234", null, null, null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/UsageDetailsListByMCABillingAccount.json
     */
    /**
     * Sample code: BillingAccountUsageDetailsList-Modern.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void billingAccountUsageDetailsListModern(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .usageDetails()
            .list("providers/Microsoft.Billing/BillingAccounts/1234:56789", null, null, null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/UsageDetailsListByMCACustomer.json
     */
    /**
     * Sample code: CustomerUsageDetailsList-Modern.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void customerUsageDetailsListModern(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
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

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/UsageDetailsExpand.json
     */
    /**
     * Sample code: UsageDetailsExpand-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void usageDetailsExpandLegacy(com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
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

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/UsageDetailsListForBillingPeriodByEnrollmentAccount.json
     */
    /**
     * Sample code: EnrollmentAccountUsageDetailsListForBillingPeriod-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void enrollmentAccountUsageDetailsListForBillingPeriodLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .usageDetails()
            .list("providers/Microsoft.Billing/EnrollmentAccounts/1234", null, null, null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/consumption/resource-manager/Microsoft.Consumption/stable/2021-10-01/examples/UsageDetailsListForBillingPeriod.json
     */
    /**
     * Sample code: UsageDetailsListForBillingPeriod-Legacy.
     *
     * @param manager Entry point to ConsumptionManager.
     */
    public static void usageDetailsListForBillingPeriodLegacy(
        com.azure.resourcemanager.consumption.ConsumptionManager manager) {
        manager
            .usageDetails()
            .list("subscriptions/00000000-0000-0000-0000-000000000000", null, null, null, null, null, Context.NONE);
    }
}
```

