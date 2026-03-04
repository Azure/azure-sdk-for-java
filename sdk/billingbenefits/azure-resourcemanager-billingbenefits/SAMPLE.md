# Code snippets and samples


## DiscountOperation

- [GetByResourceGroup](#discountoperation_getbyresourcegroup)
- [Update](#discountoperation_update)

## Discounts

- [Cancel](#discounts_cancel)
- [Create](#discounts_create)
- [Delete](#discounts_delete)
- [List](#discounts_list)
- [ListByResourceGroup](#discounts_listbyresourcegroup)
- [ScopeList](#discounts_scopelist)

## Operations

- [List](#operations_list)

## ReservationOrderAlias

- [Create](#reservationorderalias_create)
- [Get](#reservationorderalias_get)

## SavingsPlan

- [Get](#savingsplan_get)
- [List](#savingsplan_list)
- [ListAll](#savingsplan_listall)
- [Update](#savingsplan_update)
- [ValidateUpdate](#savingsplan_validateupdate)

## SavingsPlanOrder

- [Elevate](#savingsplanorder_elevate)
- [Get](#savingsplanorder_get)
- [List](#savingsplanorder_list)

## SavingsPlanOrderAlias

- [Create](#savingsplanorderalias_create)
- [Get](#savingsplanorderalias_get)
### DiscountOperation_GetByResourceGroup

```java
/**
 * Samples for DiscountOperation GetByResourceGroup.
 */
public final class DiscountOperationGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/DiscountGet.json
     */
    /**
     * Sample code: DiscountGet.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void discountGet(com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.discountOperations()
            .getByResourceGroupWithResponse("testrg", "testprimarydiscount", com.azure.core.util.Context.NONE);
    }
}
```

### DiscountOperation_Update

```java
import com.azure.resourcemanager.billingbenefits.models.DiscountPatchRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DiscountOperation Update.
 */
public final class DiscountOperationUpdateSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/DiscountsUpdate.json
     */
    /**
     * Sample code: DiscountGet.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void discountGet(com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.discountOperations()
            .update("testrg", "testprimarydiscount",
                new DiscountPatchRequest()
                    .withTags(mapOf("key1", "fakeTokenPlaceholder", "key2", "fakeTokenPlaceholder"))
                    .withDisplayName("Virtual Machines D Series"),
                com.azure.core.util.Context.NONE);
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

### Discounts_Cancel

```java
/**
 * Samples for Discounts Cancel.
 */
public final class DiscountsCancelSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/DiscountCancel.json
     */
    /**
     * Sample code: DiscountCancel.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void discountCancel(com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.discounts().cancel("testrg", "testdiscount", com.azure.core.util.Context.NONE);
    }
}
```

### Discounts_Create

```java
import com.azure.resourcemanager.billingbenefits.models.ApplyDiscountOn;
import com.azure.resourcemanager.billingbenefits.models.CatalogClaimsItem;
import com.azure.resourcemanager.billingbenefits.models.ConditionsItem;
import com.azure.resourcemanager.billingbenefits.models.CustomPriceProperties;
import com.azure.resourcemanager.billingbenefits.models.DiscountAppliedScopeType;
import com.azure.resourcemanager.billingbenefits.models.DiscountCombinationRule;
import com.azure.resourcemanager.billingbenefits.models.DiscountRuleType;
import com.azure.resourcemanager.billingbenefits.models.DiscountTypeCustomPrice;
import com.azure.resourcemanager.billingbenefits.models.DiscountTypeCustomPriceMultiCurrency;
import com.azure.resourcemanager.billingbenefits.models.DiscountTypeProductFamily;
import com.azure.resourcemanager.billingbenefits.models.DiscountTypeProductSku;
import com.azure.resourcemanager.billingbenefits.models.EntityTypeAffiliateDiscount;
import com.azure.resourcemanager.billingbenefits.models.EntityTypePrimaryDiscount;
import com.azure.resourcemanager.billingbenefits.models.MarketSetPricesItems;
import com.azure.resourcemanager.billingbenefits.models.PriceGuaranteeProperties;
import com.azure.resourcemanager.billingbenefits.models.PricingPolicy;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Discounts Create.
 */
public final class DiscountsCreateSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/DiscountsCreatePrimaryBackfill.json
     */
    /**
     * Sample code: DiscountsCreatePrimaryBackfill.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void
        discountsCreatePrimaryBackfill(com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.discounts()
            .define("testprimarydiscount")
            .withRegion("global")
            .withExistingResourceGroup("testrg")
            .withTags(mapOf("key1", "fakeTokenPlaceholder", "key2", "fakeTokenPlaceholder"))
            .withProperties(new EntityTypePrimaryDiscount().withProductCode("fakeTokenPlaceholder")
                .withStartAt(OffsetDateTime.parse("2023-07-01T00:00:00Z"))
                .withSystemId("13810867107109237")
                .withDisplayName("Virtual Machines D Series")
                .withAppliedScopeType(DiscountAppliedScopeType.BILLING_ACCOUNT)
                .withDiscountTypeProperties(
                    new DiscountTypeProductFamily().withApplyDiscountOn(ApplyDiscountOn.PURCHASE)
                        .withDiscountPercentage(14.0D)
                        .withDiscountCombinationRule(DiscountCombinationRule.BEST_OF)
                        .withConditions(Arrays.asList(new ConditionsItem().withConditionName("Cloud")
                            .withValue(Arrays.asList("US-Sec"))
                            .withType("equalAny")))
                        .withProductFamilyName("Azure"))
                .withEndAt(OffsetDateTime.parse("2024-07-01T23:59:59Z")))
            .create();
    }

    /*
     * x-ms-original-file: 2024-11-01-preview/DiscountsCreatePrimaryWithCustomPrice.json
     */
    /**
     * Sample code: DiscountsCreatePrimaryWithCustomPrice.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void discountsCreatePrimaryWithCustomPrice(
        com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.discounts()
            .define("testprimarydiscount")
            .withRegion("global")
            .withExistingResourceGroup("testrg")
            .withTags(mapOf("key1", "fakeTokenPlaceholder", "key2", "fakeTokenPlaceholder"))
            .withProperties(new EntityTypePrimaryDiscount().withProductCode("fakeTokenPlaceholder")
                .withStartAt(OffsetDateTime.parse("2023-07-01T00:00:00Z"))
                .withDisplayName("Virtual Machines D Series")
                .withAppliedScopeType(DiscountAppliedScopeType.BILLING_ACCOUNT)
                .withDiscountTypeProperties(new DiscountTypeCustomPrice().withApplyDiscountOn(ApplyDiscountOn.PURCHASE)
                    .withDiscountPercentage(14.0D)
                    .withDiscountCombinationRule(DiscountCombinationRule.BEST_OF)
                    .withConditions(Arrays.asList(new ConditionsItem().withConditionName("Cloud")
                        .withValue(Arrays.asList("US-Sec"))
                        .withType("equalAny")))
                    .withProductFamilyName("Azure")
                    .withProductId("DZH318Z0BQ35")
                    .withSkuId("0001")
                    .withCustomPriceProperties(new CustomPriceProperties()
                        .withRuleType(DiscountRuleType.FIXED_PRICE_LOCK)
                        .withCatalogId("4")
                        .withCatalogClaims(Arrays.asList(
                            new CatalogClaimsItem().withCatalogClaimsItemType("NationalCloud").withValue("USSec")))
                        .withTermUnits("ASI1251A")
                        .withMarketSetPrices(Arrays.asList(new MarketSetPricesItems().withMarkets(Arrays.asList("US"))
                            .withValue(125.16)
                            .withCurrency("USD")))))
                .withEndAt(OffsetDateTime.parse("2024-07-01T23:59:59Z")))
            .create();
    }

    /*
     * x-ms-original-file: 2024-11-01-preview/DiscountsCreatePrimaryWithPriceGuarantee.json
     */
    /**
     * Sample code: DiscountsCreatePrimaryWithPriceGuarantee.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void discountsCreatePrimaryWithPriceGuarantee(
        com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.discounts()
            .define("testprimarydiscount")
            .withRegion("global")
            .withExistingResourceGroup("testrg")
            .withTags(mapOf("key1", "fakeTokenPlaceholder", "key2", "fakeTokenPlaceholder"))
            .withProperties(new EntityTypePrimaryDiscount().withProductCode("fakeTokenPlaceholder")
                .withStartAt(OffsetDateTime.parse("2023-07-01T00:00:00Z"))
                .withDisplayName("Virtual Machines D Series")
                .withAppliedScopeType(DiscountAppliedScopeType.BILLING_ACCOUNT)
                .withDiscountTypeProperties(new DiscountTypeProductSku().withApplyDiscountOn(ApplyDiscountOn.PURCHASE)
                    .withDiscountCombinationRule(DiscountCombinationRule.BEST_OF)
                    .withPriceGuaranteeProperties(
                        new PriceGuaranteeProperties().withPricingPolicy(PricingPolicy.PROTECTED)
                            .withPriceGuaranteeDate(OffsetDateTime.parse("2024-11-01T00:00:00")))
                    .withConditions(Arrays.asList(new ConditionsItem().withConditionName("Cloud")
                        .withValue(Arrays.asList("US-Sec"))
                        .withType("equalAny")))
                    .withProductFamilyName("Azure")
                    .withProductId("DZH318Z0BQ35")
                    .withSkuId("0001"))
                .withEndAt(OffsetDateTime.parse("2024-07-01T23:59:59Z")))
            .create();
    }

    /*
     * x-ms-original-file: 2024-11-01-preview/DiscountsCreateAffiliate.json
     */
    /**
     * Sample code: DiscountsCreateAffiliate.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void
        discountsCreateAffiliate(com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.discounts()
            .define("testaffiliatediscount")
            .withRegion("global")
            .withExistingResourceGroup("testrg")
            .withTags(mapOf("key1", "fakeTokenPlaceholder", "key2", "fakeTokenPlaceholder"))
            .withProperties(new EntityTypeAffiliateDiscount().withProductCode("fakeTokenPlaceholder")
                .withStartAt(OffsetDateTime.parse("2023-07-01T00:00:00Z"))
                .withSystemId("13810867107109237")
                .withDisplayName("Virtual Machines D Series"))
            .create();
    }

    /*
     * x-ms-original-file: 2024-11-01-preview/DiscountsCreatePrimary.json
     */
    /**
     * Sample code: DiscountsCreatePrimary.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void
        discountsCreatePrimary(com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.discounts()
            .define("testprimarydiscount")
            .withRegion("global")
            .withExistingResourceGroup("testrg")
            .withTags(mapOf("key1", "fakeTokenPlaceholder", "key2", "fakeTokenPlaceholder"))
            .withProperties(new EntityTypePrimaryDiscount().withProductCode("fakeTokenPlaceholder")
                .withStartAt(OffsetDateTime.parse("2023-07-01T00:00:00Z"))
                .withDisplayName("Virtual Machines D Series")
                .withAppliedScopeType(DiscountAppliedScopeType.BILLING_ACCOUNT)
                .withDiscountTypeProperties(new DiscountTypeProductSku().withApplyDiscountOn(ApplyDiscountOn.PURCHASE)
                    .withDiscountPercentage(14.0D)
                    .withDiscountCombinationRule(DiscountCombinationRule.BEST_OF)
                    .withConditions(Arrays.asList(new ConditionsItem().withConditionName("Cloud")
                        .withValue(Arrays.asList("US-Sec"))
                        .withType("equalAny")))
                    .withProductFamilyName("Azure")
                    .withProductId("DZH318Z0BQ35")
                    .withSkuId("0001"))
                .withEndAt(OffsetDateTime.parse("2024-07-01T23:59:59Z")))
            .create();
    }

    /*
     * x-ms-original-file: 2024-11-01-preview/DiscountsCreatePrimaryWithCustomPriceMultiCurrency.json
     */
    /**
     * Sample code: DiscountsCreatePrimaryWithCustomPriceMultiCurrency.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void discountsCreatePrimaryWithCustomPriceMultiCurrency(
        com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.discounts()
            .define("testprimarydiscount")
            .withRegion("global")
            .withExistingResourceGroup("testrg")
            .withTags(mapOf("key1", "fakeTokenPlaceholder", "key2", "fakeTokenPlaceholder"))
            .withProperties(
                new EntityTypePrimaryDiscount().withProductCode("fakeTokenPlaceholder")
                    .withStartAt(OffsetDateTime.parse("2023-07-01T00:00:00Z"))
                    .withDisplayName("Virtual Machines D Series")
                    .withAppliedScopeType(DiscountAppliedScopeType.BILLING_ACCOUNT)
                    .withDiscountTypeProperties(new DiscountTypeCustomPriceMultiCurrency()
                        .withApplyDiscountOn(ApplyDiscountOn.PURCHASE)
                        .withDiscountPercentage(14.0D)
                        .withDiscountCombinationRule(DiscountCombinationRule.BEST_OF)
                        .withConditions(Arrays.asList(new ConditionsItem().withConditionName("Cloud")
                            .withValue(Arrays.asList("US-Sec"))
                            .withType("equalAny")))
                        .withProductFamilyName("Azure")
                        .withProductId("DZH318Z0BQ35")
                        .withSkuId("0001")
                        .withCustomPriceProperties(new CustomPriceProperties()
                            .withRuleType(DiscountRuleType.FIXED_PRICE_LOCK)
                            .withCatalogId("4")
                            .withCatalogClaims(Arrays.asList(
                                new CatalogClaimsItem().withCatalogClaimsItemType("NationalCloud").withValue("USSec")))
                            .withTermUnits("ASI1251A")
                            .withMarketSetPrices(Arrays.asList(
                                new MarketSetPricesItems().withMarkets(Arrays.asList("US"))
                                    .withValue(125.16)
                                    .withCurrency("USD"),
                                new MarketSetPricesItems().withMarkets(Arrays.asList("FR"))
                                    .withValue(110.16)
                                    .withCurrency("EUR")))))
                    .withEndAt(OffsetDateTime.parse("2024-07-01T23:59:59Z")))
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

### Discounts_Delete

```java
/**
 * Samples for Discounts Delete.
 */
public final class DiscountsDeleteSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/DiscountsDelete.json
     */
    /**
     * Sample code: ReservationOrderAliasCreate.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void
        reservationOrderAliasCreate(com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.discounts().delete("testrg", "testdiscount", com.azure.core.util.Context.NONE);
    }
}
```

### Discounts_List

```java
/**
 * Samples for Discounts List.
 */
public final class DiscountsListSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/DiscountList.json
     */
    /**
     * Sample code: DiscountSubscriptionList.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void
        discountSubscriptionList(com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.discounts().list(com.azure.core.util.Context.NONE);
    }
}
```

### Discounts_ListByResourceGroup

```java
/**
 * Samples for Discounts ListByResourceGroup.
 */
public final class DiscountsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/DiscountListResourceGroup.json
     */
    /**
     * Sample code: DiscountsResourceGroupList.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void
        discountsResourceGroupList(com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.discounts().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### Discounts_ScopeList

```java
/**
 * Samples for Discounts ScopeList.
 */
public final class DiscountsScopeListSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/ApplicableDiscountsList.json
     */
    /**
     * Sample code: DiscountScopeList.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void discountScopeList(com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.discounts()
            .scopeList("providers/Microsoft.Billing/billingAccounts/{acctId}", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2024-11-01-preview/OperationsGet.json
     */
    /**
     * Sample code: OperationsGet.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void operationsGet(com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ReservationOrderAlias_Create

```java
import com.azure.resourcemanager.billingbenefits.models.AppliedScopeProperties;
import com.azure.resourcemanager.billingbenefits.models.AppliedScopeType;
import com.azure.resourcemanager.billingbenefits.models.BillingPlan;
import com.azure.resourcemanager.billingbenefits.models.InstanceFlexibility;
import com.azure.resourcemanager.billingbenefits.models.ReservationOrderAliasRequest;
import com.azure.resourcemanager.billingbenefits.models.ReservationOrderAliasRequestPropertiesReservedResourceProperties;
import com.azure.resourcemanager.billingbenefits.models.ReservedResourceType;
import com.azure.resourcemanager.billingbenefits.models.ResourceSku;
import com.azure.resourcemanager.billingbenefits.models.Term;

/**
 * Samples for ReservationOrderAlias Create.
 */
public final class ReservationOrderAliasCreateSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/ReservationOrderAliasCreate.json
     */
    /**
     * Sample code: ReservationOrderAliasCreate.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void
        reservationOrderAliasCreate(com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.reservationOrderAlias()
            .create("reservationOrderAlias123", new ReservationOrderAliasRequest()
                .withSku(new ResourceSku().withName("Standard_M64s_v2"))
                .withLocation("eastus")
                .withDisplayName("ReservationOrder_2022-06-02")
                .withBillingScopeId("/subscriptions/10000000-0000-0000-0000-000000000000")
                .withTerm(Term.P1Y)
                .withBillingPlan(BillingPlan.P1M)
                .withAppliedScopeType(AppliedScopeType.SINGLE)
                .withAppliedScopeProperties(new AppliedScopeProperties()
                    .withResourceGroupId("/subscriptions/10000000-0000-0000-0000-000000000000/resourceGroups/testrg"))
                .withQuantity(5)
                .withRenew(true)
                .withReservedResourceType(ReservedResourceType.VIRTUAL_MACHINES)
                .withReservedResourceProperties(new ReservationOrderAliasRequestPropertiesReservedResourceProperties()
                    .withInstanceFlexibility(InstanceFlexibility.ON)),
                com.azure.core.util.Context.NONE);
    }
}
```

### ReservationOrderAlias_Get

```java
/**
 * Samples for ReservationOrderAlias Get.
 */
public final class ReservationOrderAliasGetSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/ReservationOrderAliasGet.json
     */
    /**
     * Sample code: ReservationOrderAliasGet.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void
        reservationOrderAliasGet(com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.reservationOrderAlias().getWithResponse("reservationOrderAlias123", com.azure.core.util.Context.NONE);
    }
}
```

### SavingsPlan_Get

```java
/**
 * Samples for SavingsPlan Get.
 */
public final class SavingsPlanGetSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/SavingsPlanItemExpandedGet.json
     */
    /**
     * Sample code: SavingsPlanItemWithExpandedRenewPropertiesGet.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void savingsPlanItemWithExpandedRenewPropertiesGet(
        com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.savingsPlans()
            .getWithResponse("20000000-0000-0000-0000-000000000000", "30000000-0000-0000-0000-000000000000",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-11-01-preview/SavingsPlanItemGet.json
     */
    /**
     * Sample code: SavingsPlanItemGet.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void savingsPlanItemGet(com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.savingsPlans()
            .getWithResponse("20000000-0000-0000-0000-000000000000", "30000000-0000-0000-0000-000000000000",
                com.azure.core.util.Context.NONE);
    }
}
```

### SavingsPlan_List

```java
/**
 * Samples for SavingsPlan List.
 */
public final class SavingsPlanListSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/SavingsPlansInOrderList.json
     */
    /**
     * Sample code: SavingsPlansInOrderList.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void
        savingsPlansInOrderList(com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.savingsPlans().list("20000000-0000-0000-0000-000000000000", com.azure.core.util.Context.NONE);
    }
}
```

### SavingsPlan_ListAll

```java
/**
 * Samples for SavingsPlan ListAll.
 */
public final class SavingsPlanListAllSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/SavingsPlansList.json
     */
    /**
     * Sample code: SavingsPlansList.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void savingsPlansList(com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.savingsPlans()
            .listAll("(properties/archived eq false)", "properties/displayName asc", "true", 50.0D, null, 1.0D,
                com.azure.core.util.Context.NONE);
    }
}
```

### SavingsPlan_Update

```java
import com.azure.resourcemanager.billingbenefits.models.AppliedScopeProperties;
import com.azure.resourcemanager.billingbenefits.models.AppliedScopeType;
import com.azure.resourcemanager.billingbenefits.models.BillingPlan;
import com.azure.resourcemanager.billingbenefits.models.Commitment;
import com.azure.resourcemanager.billingbenefits.models.CommitmentGrain;
import com.azure.resourcemanager.billingbenefits.models.PurchaseRequest;
import com.azure.resourcemanager.billingbenefits.models.RenewProperties;
import com.azure.resourcemanager.billingbenefits.models.ResourceSku;
import com.azure.resourcemanager.billingbenefits.models.SavingsPlanUpdateRequest;
import com.azure.resourcemanager.billingbenefits.models.SavingsPlanUpdateRequestProperties;
import com.azure.resourcemanager.billingbenefits.models.Term;

/**
 * Samples for SavingsPlan Update.
 */
public final class SavingsPlanUpdateSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/SavingsPlanUpdate.json
     */
    /**
     * Sample code: SavingsPlanUpdate.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void savingsPlanUpdate(com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.savingsPlans()
            .update("20000000-0000-0000-0000-000000000000", "30000000-0000-0000-0000-000000000000",
                new SavingsPlanUpdateRequest()
                    .withProperties(
                        new SavingsPlanUpdateRequestProperties().withDisplayName("TestDisplayName")
                            .withAppliedScopeType(AppliedScopeType.SINGLE)
                            .withAppliedScopeProperties(new AppliedScopeProperties().withResourceGroupId(
                                "/subscriptions/10000000-0000-0000-0000-000000000000/resourceGroups/testrg"))
                            .withRenew(true)
                            .withRenewProperties(new RenewProperties().withPurchaseProperties(new PurchaseRequest()
                                .withSku(new ResourceSku().withName("Compute_Savings_Plan"))
                                .withDisplayName("TestDisplayName_renewed")
                                .withBillingScopeId("/subscriptions/10000000-0000-0000-0000-000000000000")
                                .withTerm(Term.P1Y)
                                .withBillingPlan(BillingPlan.P1M)
                                .withAppliedScopeType(AppliedScopeType.SINGLE)
                                .withCommitment(new Commitment().withCurrencyCode("fakeTokenPlaceholder")
                                    .withAmount(15.23D)
                                    .withGrain(CommitmentGrain.HOURLY))
                                .withRenew(false)
                                .withAppliedScopeProperties(new AppliedScopeProperties().withResourceGroupId(
                                    "/subscriptions/10000000-0000-0000-0000-000000000000/resourceGroups/testrg"))))),
                com.azure.core.util.Context.NONE);
    }
}
```

### SavingsPlan_ValidateUpdate

```java
import com.azure.resourcemanager.billingbenefits.models.AppliedScopeProperties;
import com.azure.resourcemanager.billingbenefits.models.AppliedScopeType;
import com.azure.resourcemanager.billingbenefits.models.SavingsPlanUpdateRequestProperties;
import com.azure.resourcemanager.billingbenefits.models.SavingsPlanUpdateValidateRequest;
import java.util.Arrays;

/**
 * Samples for SavingsPlan ValidateUpdate.
 */
public final class SavingsPlanValidateUpdateSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/SavingsPlanValidateUpdate.json
     */
    /**
     * Sample code: SavingsPlanValidateUpdate.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void
        savingsPlanValidateUpdate(com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.savingsPlans()
            .validateUpdateWithResponse("20000000-0000-0000-0000-000000000000", "30000000-0000-0000-0000-000000000000",
                new SavingsPlanUpdateValidateRequest().withBenefits(Arrays.asList(
                    new SavingsPlanUpdateRequestProperties().withAppliedScopeType(AppliedScopeType.MANAGEMENT_GROUP)
                        .withAppliedScopeProperties(new AppliedScopeProperties()
                            .withTenantId("30000000-0000-0000-0000-000000000100")
                            .withManagementGroupId(
                                "/providers/Microsoft.Management/managementGroups/30000000-0000-0000-0000-000000000100")),
                    new SavingsPlanUpdateRequestProperties().withAppliedScopeType(AppliedScopeType.MANAGEMENT_GROUP)
                        .withAppliedScopeProperties(
                            new AppliedScopeProperties().withTenantId("30000000-0000-0000-0000-000000000100")
                                .withManagementGroupId("/providers/Microsoft.Management/managementGroups/MockMG")))),
                com.azure.core.util.Context.NONE);
    }
}
```

### SavingsPlanOrder_Elevate

```java
/**
 * Samples for SavingsPlanOrder Elevate.
 */
public final class SavingsPlanOrderElevateSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/SavingsPlanOrderElevate.json
     */
    /**
     * Sample code: SavingsPlanOrderElevate.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void
        savingsPlanOrderElevate(com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.savingsPlanOrders()
            .elevateWithResponse("20000000-0000-0000-0000-000000000000", com.azure.core.util.Context.NONE);
    }
}
```

### SavingsPlanOrder_Get

```java
/**
 * Samples for SavingsPlanOrder Get.
 */
public final class SavingsPlanOrderGetSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/SavingsPlanOrderGet.json
     */
    /**
     * Sample code: SavingsPlanOrderGet.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void savingsPlanOrderGet(com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.savingsPlanOrders()
            .getWithResponse("20000000-0000-0000-0000-000000000000", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-11-01-preview/SavingsPlanOrderExpandedGet.json
     */
    /**
     * Sample code: SavingsPlanOrderWithExpandedPaymentsGet.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void savingsPlanOrderWithExpandedPaymentsGet(
        com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.savingsPlanOrders()
            .getWithResponse("20000000-0000-0000-0000-000000000000", com.azure.core.util.Context.NONE);
    }
}
```

### SavingsPlanOrder_List

```java
/**
 * Samples for SavingsPlanOrder List.
 */
public final class SavingsPlanOrderListSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/SavingsPlanOrderList.json
     */
    /**
     * Sample code: SavingsPlanOrderList.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void savingsPlanOrderList(com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.savingsPlanOrders().list(com.azure.core.util.Context.NONE);
    }
}
```

### SavingsPlanOrderAlias_Create

```java
import com.azure.resourcemanager.billingbenefits.fluent.models.SavingsPlanOrderAliasModelInner;
import com.azure.resourcemanager.billingbenefits.models.AppliedScopeProperties;
import com.azure.resourcemanager.billingbenefits.models.AppliedScopeType;
import com.azure.resourcemanager.billingbenefits.models.BillingPlan;
import com.azure.resourcemanager.billingbenefits.models.Commitment;
import com.azure.resourcemanager.billingbenefits.models.CommitmentGrain;
import com.azure.resourcemanager.billingbenefits.models.ResourceSku;
import com.azure.resourcemanager.billingbenefits.models.Term;

/**
 * Samples for SavingsPlanOrderAlias Create.
 */
public final class SavingsPlanOrderAliasCreateSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/SavingsPlanOrderAliasCreate.json
     */
    /**
     * Sample code: SavingsPlanOrderAliasCreate.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void
        savingsPlanOrderAliasCreate(com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.savingsPlanOrderAlias()
            .create("spAlias123",
                new SavingsPlanOrderAliasModelInner().withSku(new ResourceSku().withName("Compute_Savings_Plan"))
                    .withDisplayName("Compute_SavingsPlan_10-28-2022_16-38")
                    .withBillingScopeId("/subscriptions/30000000-0000-0000-0000-000000000000")
                    .withTerm(Term.P3Y)
                    .withBillingPlan(BillingPlan.P1M)
                    .withAppliedScopeType(AppliedScopeType.SHARED)
                    .withCommitment(new Commitment().withCurrencyCode("fakeTokenPlaceholder")
                        .withAmount(0.001D)
                        .withGrain(CommitmentGrain.HOURLY)),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-11-01-preview/SavingsPlanOrderAliasCreateSingleScope.json
     */
    /**
     * Sample code: SavingsPlanOrderAliasCreateSingleScope.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void savingsPlanOrderAliasCreateSingleScope(
        com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.savingsPlanOrderAlias()
            .create("spAlias123", new SavingsPlanOrderAliasModelInner()
                .withSku(new ResourceSku().withName("Compute_Savings_Plan"))
                .withDisplayName("Compute_SavingsPlan_10-28-2022_16-38")
                .withBillingScopeId(
                    "/providers/Microsoft.Billing/billingAccounts/1234567/billingSubscriptions/30000000-0000-0000-0000-000000000000")
                .withTerm(Term.P3Y)
                .withBillingPlan(BillingPlan.P1M)
                .withAppliedScopeType(AppliedScopeType.SINGLE)
                .withAppliedScopeProperties(new AppliedScopeProperties()
                    .withSubscriptionId("/subscriptions/30000000-0000-0000-0000-000000000000"))
                .withCommitment(new Commitment().withCurrencyCode("fakeTokenPlaceholder")
                    .withAmount(0.001D)
                    .withGrain(CommitmentGrain.HOURLY)),
                com.azure.core.util.Context.NONE);
    }
}
```

### SavingsPlanOrderAlias_Get

```java
/**
 * Samples for SavingsPlanOrderAlias Get.
 */
public final class SavingsPlanOrderAliasGetSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/SavingsPlanOrderAliasGet.json
     */
    /**
     * Sample code: SavingsPlanOrderAliasGet.
     * 
     * @param manager Entry point to BillingBenefitsManager.
     */
    public static void
        savingsPlanOrderAliasGet(com.azure.resourcemanager.billingbenefits.BillingBenefitsManager manager) {
        manager.savingsPlanOrderAlias().getWithResponse("spAlias123", com.azure.core.util.Context.NONE);
    }
}
```

