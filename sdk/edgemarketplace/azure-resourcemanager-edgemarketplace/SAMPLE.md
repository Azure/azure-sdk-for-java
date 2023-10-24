# Code snippets and samples


## Offers

- [GenerateAccessToken](#offers_generateaccesstoken)
- [Get](#offers_get)
- [GetAccessToken](#offers_getaccesstoken)
- [List](#offers_list)
- [ListBySubscription](#offers_listbysubscription)

## Operations

- [List](#operations_list)

## Publishers

- [Get](#publishers_get)
- [List](#publishers_list)
- [ListBySubscription](#publishers_listbysubscription)
### Offers_GenerateAccessToken

```java
import com.azure.resourcemanager.edgemarketplace.models.AccessTokenRequest;

/** Samples for Offers GenerateAccessToken. */
public final class OffersGenerateAccessTokenSamples {
    /*
     * x-ms-original-file: specification/edgemarketplace/resource-manager/Microsoft.EdgeMarketplace/preview/2023-08-01-preview/examples/GenerateAccessToken.json
     */
    /**
     * Sample code: Generate AccessToken.
     *
     * @param manager Entry point to EdgeMarketplaceManager.
     */
    public static void generateAccessToken(com.azure.resourcemanager.edgemarketplace.EdgeMarketplaceManager manager) {
        manager
            .offers()
            .generateAccessToken(
                "subscriptions/4bed37fd-19a1-4d31-8b44-40267555bec5/resourceGroups/edgemarketplace-rg/providers/Microsoft.DataBoxEdge/dataBoxEdgeDevices/edgemarketplace-demo",
                "0001-com-ubuntu-pro-jammy",
                new AccessTokenRequest()
                    .withPublisherName("ubuntu")
                    .withEdgeMarketPlaceRegion("EastUS2Euap")
                    .withEgeMarketPlaceResourceId("testid")
                    .withHypervGeneration("V2")
                    .withMarketPlaceSku("2022-datacenter-azure-edition-core")
                    .withMarketPlaceSkuVersion("20348.1129.221007")
                    .withDeviceSku("edge")
                    .withDeviceVersion("1.0.18062.1"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Offers_Get

```java
/** Samples for Offers Get. */
public final class OffersGetSamples {
    /*
     * x-ms-original-file: specification/edgemarketplace/resource-manager/Microsoft.EdgeMarketplace/preview/2023-08-01-preview/examples/GetOffer.json
     */
    /**
     * Sample code: Get offer.
     *
     * @param manager Entry point to EdgeMarketplaceManager.
     */
    public static void getOffer(com.azure.resourcemanager.edgemarketplace.EdgeMarketplaceManager manager) {
        manager
            .offers()
            .getWithResponse(
                "subscriptions/4bed37fd-19a1-4d31-8b44-40267555bec5/resourceGroups/edgemarketplace-rg/providers/Microsoft.DataBoxEdge/dataBoxEdgeDevices/edgemarketplace-demo",
                "0001-com-ubuntu-pro-jammy",
                com.azure.core.util.Context.NONE);
    }
}
```

### Offers_GetAccessToken

```java
import com.azure.resourcemanager.edgemarketplace.models.AccessTokenReadRequest;

/** Samples for Offers GetAccessToken. */
public final class OffersGetAccessTokenSamples {
    /*
     * x-ms-original-file: specification/edgemarketplace/resource-manager/Microsoft.EdgeMarketplace/preview/2023-08-01-preview/examples/GetAccessToken.json
     */
    /**
     * Sample code: Get AccessToken.
     *
     * @param manager Entry point to EdgeMarketplaceManager.
     */
    public static void getAccessToken(com.azure.resourcemanager.edgemarketplace.EdgeMarketplaceManager manager) {
        manager
            .offers()
            .getAccessTokenWithResponse(
                "subscriptions/4bed37fd-19a1-4d31-8b44-40267555bec5/resourceGroups/edgemarketplace-rg/providers/Microsoft.DataBoxEdge/dataBoxEdgeDevices/edgemarketplace-demo",
                "0001-com-ubuntu-pro-jammy",
                new AccessTokenReadRequest().withRequestId("1.0.18062.1"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Offers_List

```java
/** Samples for Offers List. */
public final class OffersListSamples {
    /*
     * x-ms-original-file: specification/edgemarketplace/resource-manager/Microsoft.EdgeMarketplace/preview/2023-08-01-preview/examples/ListOffers.json
     */
    /**
     * Sample code: List offers.
     *
     * @param manager Entry point to EdgeMarketplaceManager.
     */
    public static void listOffers(com.azure.resourcemanager.edgemarketplace.EdgeMarketplaceManager manager) {
        manager
            .offers()
            .list(
                "subscriptions/4bed37fd-19a1-4d31-8b44-40267555bec5/resourceGroups/edgemarketplace-rg/providers/Microsoft.DataBoxEdge/dataBoxEdgeDevices/edgemarketplace-demo",
                null,
                null,
                null,
                null,
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Offers_ListBySubscription

```java
/** Samples for Offers ListBySubscription. */
public final class OffersListBySubscriptionSamples {
    /*
     * x-ms-original-file: specification/edgemarketplace/resource-manager/Microsoft.EdgeMarketplace/preview/2023-08-01-preview/examples/ListOffersBySubscription.json
     */
    /**
     * Sample code: List offers by subscription.
     *
     * @param manager Entry point to EdgeMarketplaceManager.
     */
    public static void listOffersBySubscription(
        com.azure.resourcemanager.edgemarketplace.EdgeMarketplaceManager manager) {
        manager.offers().listBySubscription(null, null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/edgemarketplace/resource-manager/Microsoft.EdgeMarketplace/preview/2023-08-01-preview/examples/ListOperations.json
     */
    /**
     * Sample code: List operations.
     *
     * @param manager Entry point to EdgeMarketplaceManager.
     */
    public static void listOperations(com.azure.resourcemanager.edgemarketplace.EdgeMarketplaceManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Publishers_Get

```java
/** Samples for Publishers Get. */
public final class PublishersGetSamples {
    /*
     * x-ms-original-file: specification/edgemarketplace/resource-manager/Microsoft.EdgeMarketplace/preview/2023-08-01-preview/examples/GetPublisher.json
     */
    /**
     * Sample code: Get publisher.
     *
     * @param manager Entry point to EdgeMarketplaceManager.
     */
    public static void getPublisher(com.azure.resourcemanager.edgemarketplace.EdgeMarketplaceManager manager) {
        manager
            .publishers()
            .getWithResponse(
                "subscriptions/4bed37fd-19a1-4d31-8b44-40267555bec5/resourceGroups/edgemarketplace-rg/providers/Microsoft.DataBoxEdge/dataBoxEdgeDevices/edgemarketplace-demo",
                "canonical",
                com.azure.core.util.Context.NONE);
    }
}
```

### Publishers_List

```java
/** Samples for Publishers List. */
public final class PublishersListSamples {
    /*
     * x-ms-original-file: specification/edgemarketplace/resource-manager/Microsoft.EdgeMarketplace/preview/2023-08-01-preview/examples/ListPublishers.json
     */
    /**
     * Sample code: List publishers.
     *
     * @param manager Entry point to EdgeMarketplaceManager.
     */
    public static void listPublishers(com.azure.resourcemanager.edgemarketplace.EdgeMarketplaceManager manager) {
        manager
            .publishers()
            .list(
                "subscriptions/4bed37fd-19a1-4d31-8b44-40267555bec5/resourceGroups/edgemarketplace-rg/providers/Microsoft.DataBoxEdge/dataBoxEdgeDevices/edgemarketplace-demo",
                null,
                null,
                null,
                null,
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Publishers_ListBySubscription

```java
/** Samples for Publishers ListBySubscription. */
public final class PublishersListBySubscriptionSamples {
    /*
     * x-ms-original-file: specification/edgemarketplace/resource-manager/Microsoft.EdgeMarketplace/preview/2023-08-01-preview/examples/ListPublishersBySubscription.json
     */
    /**
     * Sample code: List publishers by subscription.
     *
     * @param manager Entry point to EdgeMarketplaceManager.
     */
    public static void listPublishersBySubscription(
        com.azure.resourcemanager.edgemarketplace.EdgeMarketplaceManager manager) {
        manager.publishers().listBySubscription(null, null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

