# Code snippets and samples


## MarketplaceAgreements

- [Cancel](#marketplaceagreements_cancel)
- [Create](#marketplaceagreements_create)
- [Get](#marketplaceagreements_get)
- [GetAgreement](#marketplaceagreements_getagreement)
- [List](#marketplaceagreements_list)
- [Sign](#marketplaceagreements_sign)
### MarketplaceAgreements_Cancel

```java
import com.azure.core.util.Context;

/** Samples for MarketplaceAgreements Cancel. */
public final class MarketplaceAgreementsCancelSamples {
    /*
     * x-ms-original-file: specification/marketplaceordering/resource-manager/Microsoft.MarketplaceOrdering/stable/2021-01-01/examples/CancelMarketplaceTerms.json
     */
    /**
     * Sample code: SetMarketplaceTerms.
     *
     * @param manager Entry point to MarketplaceOrderingManager.
     */
    public static void setMarketplaceTerms(
        com.azure.resourcemanager.marketplaceordering.MarketplaceOrderingManager manager) {
        manager.marketplaceAgreements().cancelWithResponse("pubid", "offid", "planid", Context.NONE);
    }
}
```

### MarketplaceAgreements_Create

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.marketplaceordering.fluent.models.AgreementTermsInner;
import com.azure.resourcemanager.marketplaceordering.models.OfferType;
import java.time.OffsetDateTime;

/** Samples for MarketplaceAgreements Create. */
public final class MarketplaceAgreementsCreateSamples {
    /*
     * x-ms-original-file: specification/marketplaceordering/resource-manager/Microsoft.MarketplaceOrdering/stable/2021-01-01/examples/SetMarketplaceTerms.json
     */
    /**
     * Sample code: SetMarketplaceTerms.
     *
     * @param manager Entry point to MarketplaceOrderingManager.
     */
    public static void setMarketplaceTerms(
        com.azure.resourcemanager.marketplaceordering.MarketplaceOrderingManager manager) {
        manager
            .marketplaceAgreements()
            .createWithResponse(
                OfferType.VIRTUALMACHINE,
                "pubid",
                "offid",
                "planid",
                new AgreementTermsInner()
                    .withPublisher("pubid")
                    .withProduct("offid")
                    .withPlan("planid")
                    .withLicenseTextLink("test.licenseLink")
                    .withPrivacyPolicyLink("test.privacyPolicyLink")
                    .withMarketplaceTermsLink("test.marketplaceTermsLink")
                    .withRetrieveDatetime(OffsetDateTime.parse("2017-08-15T11:33:07.12132Z"))
                    .withSignature("ASDFSDAFWEFASDGWERLWER")
                    .withAccepted(false),
                Context.NONE);
    }
}
```

### MarketplaceAgreements_Get

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.marketplaceordering.models.OfferType;

/** Samples for MarketplaceAgreements Get. */
public final class MarketplaceAgreementsGetSamples {
    /*
     * x-ms-original-file: specification/marketplaceordering/resource-manager/Microsoft.MarketplaceOrdering/stable/2021-01-01/examples/GetMarketplaceTerms.json
     */
    /**
     * Sample code: GetMarketplaceTerms.
     *
     * @param manager Entry point to MarketplaceOrderingManager.
     */
    public static void getMarketplaceTerms(
        com.azure.resourcemanager.marketplaceordering.MarketplaceOrderingManager manager) {
        manager
            .marketplaceAgreements()
            .getWithResponse(OfferType.VIRTUALMACHINE, "pubid", "offid", "planid", Context.NONE);
    }
}
```

### MarketplaceAgreements_GetAgreement

```java
import com.azure.core.util.Context;

/** Samples for MarketplaceAgreements GetAgreement. */
public final class MarketplaceAgreementsGetAgreementSamples {
    /*
     * x-ms-original-file: specification/marketplaceordering/resource-manager/Microsoft.MarketplaceOrdering/stable/2021-01-01/examples/GetAgreementMarketplaceTerms.json
     */
    /**
     * Sample code: SetMarketplaceTerms.
     *
     * @param manager Entry point to MarketplaceOrderingManager.
     */
    public static void setMarketplaceTerms(
        com.azure.resourcemanager.marketplaceordering.MarketplaceOrderingManager manager) {
        manager.marketplaceAgreements().getAgreementWithResponse("pubid", "offid", "planid", Context.NONE);
    }
}
```

### MarketplaceAgreements_List

```java
import com.azure.core.util.Context;

/** Samples for MarketplaceAgreements List. */
public final class MarketplaceAgreementsListSamples {
    /*
     * x-ms-original-file: specification/marketplaceordering/resource-manager/Microsoft.MarketplaceOrdering/stable/2021-01-01/examples/ListMarketplaceTerms.json
     */
    /**
     * Sample code: SetMarketplaceTerms.
     *
     * @param manager Entry point to MarketplaceOrderingManager.
     */
    public static void setMarketplaceTerms(
        com.azure.resourcemanager.marketplaceordering.MarketplaceOrderingManager manager) {
        manager.marketplaceAgreements().listWithResponse(Context.NONE);
    }
}
```

### MarketplaceAgreements_Sign

```java
import com.azure.core.util.Context;

/** Samples for MarketplaceAgreements Sign. */
public final class MarketplaceAgreementsSignSamples {
    /*
     * x-ms-original-file: specification/marketplaceordering/resource-manager/Microsoft.MarketplaceOrdering/stable/2021-01-01/examples/SignMarketplaceTerms.json
     */
    /**
     * Sample code: SetMarketplaceTerms.
     *
     * @param manager Entry point to MarketplaceOrderingManager.
     */
    public static void setMarketplaceTerms(
        com.azure.resourcemanager.marketplaceordering.MarketplaceOrderingManager manager) {
        manager.marketplaceAgreements().signWithResponse("pubid", "offid", "planid", Context.NONE);
    }
}
```

