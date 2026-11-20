# Code snippets and samples


## RateCard

- [Get](#ratecard_get)

## UsageAggregates

- [List](#usageaggregates_list)
### RateCard_Get

```java
/**
 * Samples for RateCard Get.
 */
public final class RateCardGetSamples {
    /*
     * x-ms-original-file: 2015-06-01-preview/GetRateCard.json
     */
    /**
     * Sample code: GetRateCard.
     * 
     * @param manager Entry point to CommerceManager.
     */
    public static void getRateCard(com.azure.resourcemanager.commerce.CommerceManager manager) {
        manager.rateCards()
            .getWithResponse(
                "OfferDurableId eq 'MS-AZR-0003P' and Currency eq 'USD' and Locale eq 'en-US' and RegionInfo eq 'US'",
                com.azure.core.util.Context.NONE);
    }
}
```

### UsageAggregates_List

```java
import com.azure.resourcemanager.commerce.models.AggregationGranularity;
import java.time.OffsetDateTime;

/**
 * Samples for UsageAggregates List.
 */
public final class UsageAggregatesListSamples {
    /*
     * x-ms-original-file: 2015-06-01-preview/GetUsageAggregatesList.json
     */
    /**
     * Sample code: GetUsageAggregatesList.
     * 
     * @param manager Entry point to CommerceManager.
     */
    public static void getUsageAggregatesList(com.azure.resourcemanager.commerce.CommerceManager manager) {
        manager.usageAggregates()
            .list(OffsetDateTime.parse("2014-05-01T00:00:00 00:00"), OffsetDateTime.parse("2015-06-01T00:00:00 00:00"),
                true, AggregationGranularity.DAILY, null, com.azure.core.util.Context.NONE);
    }
}
```

