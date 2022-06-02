# Code snippets and samples


## RateCard

- [Get](#ratecard_get)
### RateCard_Get

```java
import com.azure.core.util.Context;

/** Samples for RateCard Get. */
public final class RateCardGetSamples {
    /*
     * x-ms-original-file: specification/commerce/resource-manager/Microsoft.Commerce/preview/2015-06-01-preview/examples/GetRateCard.json
     */
    /**
     * Sample code: GetRateCard.
     *
     * @param manager Entry point to UsageManager.
     */
    public static void getRateCard(com.azure.resourcemanager.commerce.UsageManager manager) {
        manager
            .rateCards()
            .getWithResponse(
                "OfferDurableId eq 'MS-AZR-0003P' and Currency eq 'USD' and Locale eq 'en-US' and RegionInfo eq 'US'",
                Context.NONE);
    }
}
```

