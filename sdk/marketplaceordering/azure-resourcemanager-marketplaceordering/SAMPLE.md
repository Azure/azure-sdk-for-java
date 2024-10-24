# Code snippets and samples


## MarketplaceAgreements

- [Cancel](#marketplaceagreements_cancel)
- [Create](#marketplaceagreements_create)
- [Get](#marketplaceagreements_get)
- [GetAgreement](#marketplaceagreements_getagreement)
- [List](#marketplaceagreements_list)
- [Sign](#marketplaceagreements_sign)

## Operations

- [List](#operations_list)
### MarketplaceAgreements_Cancel

```java
/**
 * Samples for MarketplaceAgreements Cancel.
 */
public final class MarketplaceAgreementsCancelSamples {
    /*
     * x-ms-original-file:
     * specification/marketplaceordering/resource-manager/Microsoft.MarketplaceOrdering/stable/2021-01-01/examples/
     * CancelMarketplaceTerms.json
     */
    /**
     * Sample code: SetMarketplaceTerms.
     * 
     * @param manager Entry point to MarketplaceOrderingManager.
     */
    public static void
        setMarketplaceTerms(com.azure.resourcemanager.marketplaceordering.MarketplaceOrderingManager manager) {
        manager.marketplaceAgreements()
            .cancelWithResponse("pubid", "offid", "planid", com.azure.core.util.Context.NONE);
    }
}
```

### MarketplaceAgreements_Create

```java
import com.azure.resourcemanager.marketplaceordering.fluent.models.AgreementTermsInner;
import com.azure.resourcemanager.marketplaceordering.models.OfferType;
import java.time.OffsetDateTime;

/**
 * Samples for MarketplaceAgreements Create.
 */
public final class MarketplaceAgreementsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/marketplaceordering/resource-manager/Microsoft.MarketplaceOrdering/stable/2021-01-01/examples/
     * SetMarketplaceTerms.json
     */
    /**
     * Sample code: SetMarketplaceTerms.
     * 
     * @param manager Entry point to MarketplaceOrderingManager.
     */
    public static void
        setMarketplaceTerms(com.azure.resourcemanager.marketplaceordering.MarketplaceOrderingManager manager) {
        manager.marketplaceAgreements()
            .createWithResponse(OfferType.VIRTUALMACHINE, "pubid", "offid", "planid",
                new AgreementTermsInner().withPublisher("pubid")
                    .withProduct("offid")
                    .withPlan("planid")
                    .withLicenseTextLink("test.licenseLink")
                    .withPrivacyPolicyLink("test.privacyPolicyLink")
                    .withMarketplaceTermsLink("test.marketplaceTermsLink")
                    .withRetrieveDatetime(OffsetDateTime.parse("2017-08-15T11:33:07.12132Z"))
                    .withSignature("ASDFSDAFWEFASDGWERLWER")
                    .withAccepted(false),
                com.azure.core.util.Context.NONE);
    }
}
```

### MarketplaceAgreements_Get

```java
import com.azure.resourcemanager.marketplaceordering.models.OfferType;

/**
 * Samples for MarketplaceAgreements Get.
 */
public final class MarketplaceAgreementsGetSamples {
    /*
     * x-ms-original-file:
     * specification/marketplaceordering/resource-manager/Microsoft.MarketplaceOrdering/stable/2021-01-01/examples/
     * GetMarketplaceTerms.json
     */
    /**
     * Sample code: GetMarketplaceTerms.
     * 
     * @param manager Entry point to MarketplaceOrderingManager.
     */
    public static void
        getMarketplaceTerms(com.azure.resourcemanager.marketplaceordering.MarketplaceOrderingManager manager) {
        manager.marketplaceAgreements()
            .getWithResponse(OfferType.VIRTUALMACHINE, "pubid", "offid", "planid", com.azure.core.util.Context.NONE);
    }
}
```

### MarketplaceAgreements_GetAgreement

```java
/**
 * Samples for MarketplaceAgreements GetAgreement.
 */
public final class MarketplaceAgreementsGetAgreementSamples {
    /*
     * x-ms-original-file:
     * specification/marketplaceordering/resource-manager/Microsoft.MarketplaceOrdering/stable/2021-01-01/examples/
     * GetAgreementMarketplaceTerms.json
     */
    /**
     * Sample code: SetMarketplaceTerms.
     * 
     * @param manager Entry point to MarketplaceOrderingManager.
     */
    public static void
        setMarketplaceTerms(com.azure.resourcemanager.marketplaceordering.MarketplaceOrderingManager manager) {
        manager.marketplaceAgreements()
            .getAgreementWithResponse("pubid", "offid", "planid", com.azure.core.util.Context.NONE);
    }
}
```

### MarketplaceAgreements_List

```java
/**
 * Samples for MarketplaceAgreements List.
 */
public final class MarketplaceAgreementsListSamples {
    /*
     * x-ms-original-file:
     * specification/marketplaceordering/resource-manager/Microsoft.MarketplaceOrdering/stable/2021-01-01/examples/
     * ListMarketplaceTerms.json
     */
    /**
     * Sample code: ListMarketplaceTerms.
     * 
     * @param manager Entry point to MarketplaceOrderingManager.
     */
    public static void
        listMarketplaceTerms(com.azure.resourcemanager.marketplaceordering.MarketplaceOrderingManager manager) {
        manager.marketplaceAgreements().listWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### MarketplaceAgreements_Sign

```java
/**
 * Samples for MarketplaceAgreements Sign.
 */
public final class MarketplaceAgreementsSignSamples {
    /*
     * x-ms-original-file:
     * specification/marketplaceordering/resource-manager/Microsoft.MarketplaceOrdering/stable/2021-01-01/examples/
     * SignMarketplaceTerms.json
     */
    /**
     * Sample code: SetMarketplaceTerms.
     * 
     * @param manager Entry point to MarketplaceOrderingManager.
     */
    public static void
        setMarketplaceTerms(com.azure.resourcemanager.marketplaceordering.MarketplaceOrderingManager manager) {
        manager.marketplaceAgreements().signWithResponse("pubid", "offid", "planid", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/marketplaceordering/resource-manager/Microsoft.MarketplaceOrdering/stable/2021-01-01/examples/
     * OperationsList.json
     */
    /**
     * Sample code: List Operations.
     * 
     * @param manager Entry point to MarketplaceOrderingManager.
     */
    public static void
        listOperations(com.azure.resourcemanager.marketplaceordering.MarketplaceOrderingManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

