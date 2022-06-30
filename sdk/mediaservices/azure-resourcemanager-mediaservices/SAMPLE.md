# Code snippets and samples


## AccountFilters

- [CreateOrUpdate](#accountfilters_createorupdate)
- [Delete](#accountfilters_delete)
- [Get](#accountfilters_get)
- [List](#accountfilters_list)
- [Update](#accountfilters_update)

## AssetFilters

- [CreateOrUpdate](#assetfilters_createorupdate)
- [Delete](#assetfilters_delete)
- [Get](#assetfilters_get)
- [List](#assetfilters_list)
- [Update](#assetfilters_update)

## Assets

- [CreateOrUpdate](#assets_createorupdate)
- [Delete](#assets_delete)
- [Get](#assets_get)
- [GetEncryptionKey](#assets_getencryptionkey)
- [List](#assets_list)
- [ListContainerSas](#assets_listcontainersas)
- [ListStreamingLocators](#assets_liststreaminglocators)
- [Update](#assets_update)

## ContentKeyPolicies

- [CreateOrUpdate](#contentkeypolicies_createorupdate)
- [Delete](#contentkeypolicies_delete)
- [Get](#contentkeypolicies_get)
- [GetPolicyPropertiesWithSecrets](#contentkeypolicies_getpolicypropertieswithsecrets)
- [List](#contentkeypolicies_list)
- [Update](#contentkeypolicies_update)

## Jobs

- [CancelJob](#jobs_canceljob)
- [Create](#jobs_create)
- [Delete](#jobs_delete)
- [Get](#jobs_get)
- [List](#jobs_list)
- [Update](#jobs_update)

## LiveEvents

- [Allocate](#liveevents_allocate)
- [Create](#liveevents_create)
- [Delete](#liveevents_delete)
- [Get](#liveevents_get)
- [List](#liveevents_list)
- [Reset](#liveevents_reset)
- [Start](#liveevents_start)
- [Stop](#liveevents_stop)
- [Update](#liveevents_update)

## LiveOutputs

- [Create](#liveoutputs_create)
- [Delete](#liveoutputs_delete)
- [Get](#liveoutputs_get)
- [List](#liveoutputs_list)

## Locations

- [CheckNameAvailability](#locations_checknameavailability)

## MediaServicesOperationResults

- [Get](#mediaservicesoperationresults_get)

## MediaServicesOperationStatuses

- [Get](#mediaservicesoperationstatuses_get)

## Mediaservices

- [CreateOrUpdate](#mediaservices_createorupdate)
- [Delete](#mediaservices_delete)
- [GetByResourceGroup](#mediaservices_getbyresourcegroup)
- [List](#mediaservices_list)
- [ListByResourceGroup](#mediaservices_listbyresourcegroup)
- [ListEdgePolicies](#mediaservices_listedgepolicies)
- [SyncStorageKeys](#mediaservices_syncstoragekeys)
- [Update](#mediaservices_update)

## OperationResults

- [Get](#operationresults_get)

## OperationStatuses

- [Get](#operationstatuses_get)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [List](#privateendpointconnections_list)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [List](#privatelinkresources_list)

## StreamingEndpoints

- [Create](#streamingendpoints_create)
- [Delete](#streamingendpoints_delete)
- [Get](#streamingendpoints_get)
- [List](#streamingendpoints_list)
- [Scale](#streamingendpoints_scale)
- [Skus](#streamingendpoints_skus)
- [Start](#streamingendpoints_start)
- [Stop](#streamingendpoints_stop)
- [Update](#streamingendpoints_update)

## StreamingLocators

- [Create](#streaminglocators_create)
- [Delete](#streaminglocators_delete)
- [Get](#streaminglocators_get)
- [List](#streaminglocators_list)
- [ListContentKeys](#streaminglocators_listcontentkeys)
- [ListPaths](#streaminglocators_listpaths)

## StreamingPolicies

- [Create](#streamingpolicies_create)
- [Delete](#streamingpolicies_delete)
- [Get](#streamingpolicies_get)
- [List](#streamingpolicies_list)

## Tracks

- [CreateOrUpdate](#tracks_createorupdate)
- [Delete](#tracks_delete)
- [Get](#tracks_get)
- [List](#tracks_list)
- [Update](#tracks_update)
- [UpdateTrackData](#tracks_updatetrackdata)

## Transforms

- [CreateOrUpdate](#transforms_createorupdate)
- [Delete](#transforms_delete)
- [Get](#transforms_get)
- [List](#transforms_list)
- [Update](#transforms_update)
### AccountFilters_CreateOrUpdate

```java
import com.azure.resourcemanager.mediaservices.models.FilterTrackPropertyCompareOperation;
import com.azure.resourcemanager.mediaservices.models.FilterTrackPropertyCondition;
import com.azure.resourcemanager.mediaservices.models.FilterTrackPropertyType;
import com.azure.resourcemanager.mediaservices.models.FilterTrackSelection;
import com.azure.resourcemanager.mediaservices.models.FirstQuality;
import com.azure.resourcemanager.mediaservices.models.PresentationTimeRange;
import java.util.Arrays;

/** Samples for AccountFilters CreateOrUpdate. */
public final class AccountFiltersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/accountFilters-create.json
     */
    /**
     * Sample code: Create an Account Filter.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void createAnAccountFilter(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .accountFilters()
            .define("newAccountFilter")
            .withExistingMediaService("contoso", "contosomedia")
            .withPresentationTimeRange(
                new PresentationTimeRange()
                    .withStartTimestamp(0L)
                    .withEndTimestamp(170000000L)
                    .withPresentationWindowDuration(9223372036854775000L)
                    .withLiveBackoffDuration(0L)
                    .withTimescale(10000000L)
                    .withForceEndTimestamp(false))
            .withFirstQuality(new FirstQuality().withBitrate(128000))
            .withTracks(
                Arrays
                    .asList(
                        new FilterTrackSelection()
                            .withTrackSelections(
                                Arrays
                                    .asList(
                                        new FilterTrackPropertyCondition()
                                            .withProperty(FilterTrackPropertyType.TYPE)
                                            .withValue("Audio")
                                            .withOperation(FilterTrackPropertyCompareOperation.EQUAL),
                                        new FilterTrackPropertyCondition()
                                            .withProperty(FilterTrackPropertyType.LANGUAGE)
                                            .withValue("en")
                                            .withOperation(FilterTrackPropertyCompareOperation.NOT_EQUAL),
                                        new FilterTrackPropertyCondition()
                                            .withProperty(FilterTrackPropertyType.FOUR_CC)
                                            .withValue("EC-3")
                                            .withOperation(FilterTrackPropertyCompareOperation.NOT_EQUAL))),
                        new FilterTrackSelection()
                            .withTrackSelections(
                                Arrays
                                    .asList(
                                        new FilterTrackPropertyCondition()
                                            .withProperty(FilterTrackPropertyType.TYPE)
                                            .withValue("Video")
                                            .withOperation(FilterTrackPropertyCompareOperation.EQUAL),
                                        new FilterTrackPropertyCondition()
                                            .withProperty(FilterTrackPropertyType.BITRATE)
                                            .withValue("3000000-5000000")
                                            .withOperation(FilterTrackPropertyCompareOperation.EQUAL)))))
            .create();
    }
}
```

### AccountFilters_Delete

```java
import com.azure.core.util.Context;

/** Samples for AccountFilters Delete. */
public final class AccountFiltersDeleteSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/accountFilters-delete.json
     */
    /**
     * Sample code: Delete an Account Filter.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void deleteAnAccountFilter(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .accountFilters()
            .deleteWithResponse("contoso", "contosomedia", "accountFilterWithTimeWindowAndTrack", Context.NONE);
    }
}
```

### AccountFilters_Get

```java
import com.azure.core.util.Context;

/** Samples for AccountFilters Get. */
public final class AccountFiltersGetSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/accountFilters-get-by-name.json
     */
    /**
     * Sample code: Get an Account Filter by name.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getAnAccountFilterByName(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.accountFilters().getWithResponse("contoso", "contosomedia", "accountFilterWithTrack", Context.NONE);
    }
}
```

### AccountFilters_List

```java
import com.azure.core.util.Context;

/** Samples for AccountFilters List. */
public final class AccountFiltersListSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/accountFilters-list-all.json
     */
    /**
     * Sample code: List all Account Filters.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listAllAccountFilters(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.accountFilters().list("contoso", "contosomedia", Context.NONE);
    }
}
```

### AccountFilters_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mediaservices.models.AccountFilter;
import com.azure.resourcemanager.mediaservices.models.FirstQuality;
import com.azure.resourcemanager.mediaservices.models.PresentationTimeRange;

/** Samples for AccountFilters Update. */
public final class AccountFiltersUpdateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/accountFilters-update.json
     */
    /**
     * Sample code: Update an Account Filter.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void updateAnAccountFilter(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        AccountFilter resource =
            manager
                .accountFilters()
                .getWithResponse("contoso", "contosomedia", "accountFilterWithTimeWindowAndTrack", Context.NONE)
                .getValue();
        resource
            .update()
            .withPresentationTimeRange(
                new PresentationTimeRange()
                    .withStartTimestamp(10L)
                    .withEndTimestamp(170000000L)
                    .withPresentationWindowDuration(9223372036854775000L)
                    .withLiveBackoffDuration(0L)
                    .withTimescale(10000000L)
                    .withForceEndTimestamp(false))
            .withFirstQuality(new FirstQuality().withBitrate(128000))
            .apply();
    }
}
```

### AssetFilters_CreateOrUpdate

```java
import com.azure.resourcemanager.mediaservices.models.FilterTrackPropertyCompareOperation;
import com.azure.resourcemanager.mediaservices.models.FilterTrackPropertyCondition;
import com.azure.resourcemanager.mediaservices.models.FilterTrackPropertyType;
import com.azure.resourcemanager.mediaservices.models.FilterTrackSelection;
import com.azure.resourcemanager.mediaservices.models.FirstQuality;
import com.azure.resourcemanager.mediaservices.models.PresentationTimeRange;
import java.util.Arrays;

/** Samples for AssetFilters CreateOrUpdate. */
public final class AssetFiltersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/assetFilters-create.json
     */
    /**
     * Sample code: Create an Asset Filter.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void createAnAssetFilter(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .assetFilters()
            .define("newAssetFilter")
            .withExistingAsset("contoso", "contosomedia", "ClimbingMountRainer")
            .withPresentationTimeRange(
                new PresentationTimeRange()
                    .withStartTimestamp(0L)
                    .withEndTimestamp(170000000L)
                    .withPresentationWindowDuration(9223372036854775000L)
                    .withLiveBackoffDuration(0L)
                    .withTimescale(10000000L)
                    .withForceEndTimestamp(false))
            .withFirstQuality(new FirstQuality().withBitrate(128000))
            .withTracks(
                Arrays
                    .asList(
                        new FilterTrackSelection()
                            .withTrackSelections(
                                Arrays
                                    .asList(
                                        new FilterTrackPropertyCondition()
                                            .withProperty(FilterTrackPropertyType.TYPE)
                                            .withValue("Audio")
                                            .withOperation(FilterTrackPropertyCompareOperation.EQUAL),
                                        new FilterTrackPropertyCondition()
                                            .withProperty(FilterTrackPropertyType.LANGUAGE)
                                            .withValue("en")
                                            .withOperation(FilterTrackPropertyCompareOperation.NOT_EQUAL),
                                        new FilterTrackPropertyCondition()
                                            .withProperty(FilterTrackPropertyType.FOUR_CC)
                                            .withValue("EC-3")
                                            .withOperation(FilterTrackPropertyCompareOperation.NOT_EQUAL))),
                        new FilterTrackSelection()
                            .withTrackSelections(
                                Arrays
                                    .asList(
                                        new FilterTrackPropertyCondition()
                                            .withProperty(FilterTrackPropertyType.TYPE)
                                            .withValue("Video")
                                            .withOperation(FilterTrackPropertyCompareOperation.EQUAL),
                                        new FilterTrackPropertyCondition()
                                            .withProperty(FilterTrackPropertyType.BITRATE)
                                            .withValue("3000000-5000000")
                                            .withOperation(FilterTrackPropertyCompareOperation.EQUAL)))))
            .create();
    }
}
```

### AssetFilters_Delete

```java
import com.azure.core.util.Context;

/** Samples for AssetFilters Delete. */
public final class AssetFiltersDeleteSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/assetFilters-delete.json
     */
    /**
     * Sample code: Delete an Asset Filter.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void deleteAnAssetFilter(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .assetFilters()
            .deleteWithResponse(
                "contoso", "contosomedia", "ClimbingMountRainer", "assetFilterWithTimeWindowAndTrack", Context.NONE);
    }
}
```

### AssetFilters_Get

```java
import com.azure.core.util.Context;

/** Samples for AssetFilters Get. */
public final class AssetFiltersGetSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/assetFilters-get-by-name.json
     */
    /**
     * Sample code: Get an Asset Filter by name.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getAnAssetFilterByName(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .assetFilters()
            .getWithResponse(
                "contoso", "contosomedia", "ClimbingMountRainer", "assetFilterWithTimeWindowAndTrack", Context.NONE);
    }
}
```

### AssetFilters_List

```java
import com.azure.core.util.Context;

/** Samples for AssetFilters List. */
public final class AssetFiltersListSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/assetFilters-list-all.json
     */
    /**
     * Sample code: List all Asset Filters.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listAllAssetFilters(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.assetFilters().list("contoso", "contosomedia", "ClimbingMountRainer", Context.NONE);
    }
}
```

### AssetFilters_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mediaservices.models.AssetFilter;
import com.azure.resourcemanager.mediaservices.models.FirstQuality;
import com.azure.resourcemanager.mediaservices.models.PresentationTimeRange;

/** Samples for AssetFilters Update. */
public final class AssetFiltersUpdateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/assetFilters-update.json
     */
    /**
     * Sample code: Update an Asset Filter.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void updateAnAssetFilter(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        AssetFilter resource =
            manager
                .assetFilters()
                .getWithResponse(
                    "contoso", "contosomedia", "ClimbingMountRainer", "assetFilterWithTimeWindowAndTrack", Context.NONE)
                .getValue();
        resource
            .update()
            .withPresentationTimeRange(
                new PresentationTimeRange()
                    .withStartTimestamp(10L)
                    .withEndTimestamp(170000000L)
                    .withPresentationWindowDuration(9223372036854775000L)
                    .withLiveBackoffDuration(0L)
                    .withTimescale(10000000L)
                    .withForceEndTimestamp(false))
            .withFirstQuality(new FirstQuality().withBitrate(128000))
            .apply();
    }
}
```

### Assets_CreateOrUpdate

```java
/** Samples for Assets CreateOrUpdate. */
public final class AssetsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/assets-create.json
     */
    /**
     * Sample code: Create an Asset.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void createAnAsset(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .assets()
            .define("ClimbingMountLogan")
            .withExistingMediaService("contoso", "contosomedia")
            .withDescription("A documentary showing the ascent of Mount Logan")
            .withStorageAccountName("storage0")
            .create();
    }
}
```

### Assets_Delete

```java
import com.azure.core.util.Context;

/** Samples for Assets Delete. */
public final class AssetsDeleteSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/assets-delete.json
     */
    /**
     * Sample code: Delete an Asset.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void deleteAnAsset(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.assets().deleteWithResponse("contoso", "contosomedia", "ClimbingMountAdams", Context.NONE);
    }
}
```

### Assets_Get

```java
import com.azure.core.util.Context;

/** Samples for Assets Get. */
public final class AssetsGetSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/assets-get-by-name.json
     */
    /**
     * Sample code: Get an Asset by name.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getAnAssetByName(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.assets().getWithResponse("contoso", "contosomedia", "ClimbingMountAdams", Context.NONE);
    }
}
```

### Assets_GetEncryptionKey

```java
import com.azure.core.util.Context;

/** Samples for Assets GetEncryptionKey. */
public final class AssetsGetEncryptionKeySamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/assets-get-encryption-keys.json
     */
    /**
     * Sample code: Get Asset Storage Encryption Keys.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getAssetStorageEncryptionKeys(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .assets()
            .getEncryptionKeyWithResponse("contoso", "contosomedia", "ClimbingMountSaintHelens", Context.NONE);
    }
}
```

### Assets_List

```java
import com.azure.core.util.Context;

/** Samples for Assets List. */
public final class AssetsListSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/assets-list-by-date.json
     */
    /**
     * Sample code: List Asset ordered by date.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listAssetOrderedByDate(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.assets().list("contoso", "contosomedia", null, null, "properties/created", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/assets-list-in-date-range.json
     */
    /**
     * Sample code: List Asset created in a date range.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listAssetCreatedInADateRange(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .assets()
            .list(
                "contoso",
                "contosomedia",
                "properties/created gt 2012-06-01 and properties/created lt 2013-07-01",
                null,
                "properties/created",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/assets-list-all.json
     */
    /**
     * Sample code: List all Assets.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listAllAssets(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.assets().list("contoso", "contosomedia", null, null, null, Context.NONE);
    }
}
```

### Assets_ListContainerSas

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mediaservices.models.AssetContainerPermission;
import com.azure.resourcemanager.mediaservices.models.ListContainerSasInput;
import java.time.OffsetDateTime;

/** Samples for Assets ListContainerSas. */
public final class AssetsListContainerSasSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/assets-list-sas-urls.json
     */
    /**
     * Sample code: List Asset SAS URLs.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listAssetSASURLs(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .assets()
            .listContainerSasWithResponse(
                "contoso",
                "contosomedia",
                "ClimbingMountBaker",
                new ListContainerSasInput()
                    .withPermissions(AssetContainerPermission.READ_WRITE)
                    .withExpiryTime(OffsetDateTime.parse("2018-01-01T10:00:00.007Z")),
                Context.NONE);
    }
}
```

### Assets_ListStreamingLocators

```java
import com.azure.core.util.Context;

/** Samples for Assets ListStreamingLocators. */
public final class AssetsListStreamingLocatorsSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/assets-list-streaming-locators.json
     */
    /**
     * Sample code: List Asset SAS URLs.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listAssetSASURLs(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .assets()
            .listStreamingLocatorsWithResponse("contoso", "contosomedia", "ClimbingMountSaintHelens", Context.NONE);
    }
}
```

### Assets_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mediaservices.models.Asset;

/** Samples for Assets Update. */
public final class AssetsUpdateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/assets-update.json
     */
    /**
     * Sample code: Update an Asset.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void updateAnAsset(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        Asset resource =
            manager.assets().getWithResponse("contoso", "contosomedia", "ClimbingMountBaker", Context.NONE).getValue();
        resource.update().withDescription("A documentary showing the ascent of Mount Baker in HD").apply();
    }
}
```

### ContentKeyPolicies_CreateOrUpdate

```java
import com.azure.resourcemanager.mediaservices.models.ContentKeyPolicyClearKeyConfiguration;
import com.azure.resourcemanager.mediaservices.models.ContentKeyPolicyOpenRestriction;
import com.azure.resourcemanager.mediaservices.models.ContentKeyPolicyOption;
import com.azure.resourcemanager.mediaservices.models.ContentKeyPolicyPlayReadyConfiguration;
import com.azure.resourcemanager.mediaservices.models.ContentKeyPolicyPlayReadyContentEncryptionKeyFromHeader;
import com.azure.resourcemanager.mediaservices.models.ContentKeyPolicyPlayReadyContentType;
import com.azure.resourcemanager.mediaservices.models.ContentKeyPolicyPlayReadyLicense;
import com.azure.resourcemanager.mediaservices.models.ContentKeyPolicyPlayReadyLicenseType;
import com.azure.resourcemanager.mediaservices.models.ContentKeyPolicyPlayReadyPlayRight;
import com.azure.resourcemanager.mediaservices.models.ContentKeyPolicyPlayReadyUnknownOutputPassingOption;
import com.azure.resourcemanager.mediaservices.models.ContentKeyPolicyRestrictionTokenType;
import com.azure.resourcemanager.mediaservices.models.ContentKeyPolicyRsaTokenKey;
import com.azure.resourcemanager.mediaservices.models.ContentKeyPolicySymmetricTokenKey;
import com.azure.resourcemanager.mediaservices.models.ContentKeyPolicyTokenRestriction;
import com.azure.resourcemanager.mediaservices.models.ContentKeyPolicyWidevineConfiguration;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Samples for ContentKeyPolicies CreateOrUpdate. */
public final class ContentKeyPoliciesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/content-key-policies-create-multiple-options.json
     */
    /**
     * Sample code: Creates a Content Key Policy with multiple options.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void createsAContentKeyPolicyWithMultipleOptions(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .contentKeyPolicies()
            .define("PolicyCreatedWithMultipleOptions")
            .withExistingMediaService("contoso", "contosomedia")
            .withDescription("ArmPolicyDescription")
            .withOptions(
                Arrays
                    .asList(
                        new ContentKeyPolicyOption()
                            .withName("ClearKeyOption")
                            .withConfiguration(new ContentKeyPolicyClearKeyConfiguration())
                            .withRestriction(
                                new ContentKeyPolicyTokenRestriction()
                                    .withIssuer("urn:issuer")
                                    .withAudience("urn:audience")
                                    .withPrimaryVerificationKey(
                                        new ContentKeyPolicySymmetricTokenKey()
                                            .withKeyValue("AAAAAAAAAAAAAAAAAAAAAA==".getBytes()))
                                    .withRestrictionTokenType(ContentKeyPolicyRestrictionTokenType.SWT)),
                        new ContentKeyPolicyOption()
                            .withName("widevineoption")
                            .withConfiguration(
                                new ContentKeyPolicyWidevineConfiguration()
                                    .withWidevineTemplate(
                                        "{\"allowed_track_types\":\"SD_HD\",\"content_key_specs\":[{\"track_type\":\"SD\",\"security_level\":1,\"required_output_protection\":{\"hdcp\":\"HDCP_V2\"}}],\"policy_overrides\":{\"can_play\":true,\"can_persist\":true,\"can_renew\":false}}"))
                            .withRestriction(new ContentKeyPolicyOpenRestriction())))
            .create();
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/content-key-policies-create-nodrm-token.json
     */
    /**
     * Sample code: Creates a Content Key Policy with ClearKey option and Token Restriction.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void createsAContentKeyPolicyWithClearKeyOptionAndTokenRestriction(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .contentKeyPolicies()
            .define("PolicyWithClearKeyOptionAndSwtTokenRestriction")
            .withExistingMediaService("contoso", "contosomedia")
            .withDescription("ArmPolicyDescription")
            .withOptions(
                Arrays
                    .asList(
                        new ContentKeyPolicyOption()
                            .withName("ClearKeyOption")
                            .withConfiguration(new ContentKeyPolicyClearKeyConfiguration())
                            .withRestriction(
                                new ContentKeyPolicyTokenRestriction()
                                    .withIssuer("urn:issuer")
                                    .withAudience("urn:audience")
                                    .withPrimaryVerificationKey(
                                        new ContentKeyPolicySymmetricTokenKey()
                                            .withKeyValue("AAAAAAAAAAAAAAAAAAAAAA==".getBytes()))
                                    .withRestrictionTokenType(ContentKeyPolicyRestrictionTokenType.SWT))))
            .create();
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/content-key-policies-create-playready-open.json
     */
    /**
     * Sample code: Creates a Content Key Policy with PlayReady option and Open Restriction.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void createsAContentKeyPolicyWithPlayReadyOptionAndOpenRestriction(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .contentKeyPolicies()
            .define("PolicyWithPlayReadyOptionAndOpenRestriction")
            .withExistingMediaService("contoso", "contosomedia")
            .withDescription("ArmPolicyDescription")
            .withOptions(
                Arrays
                    .asList(
                        new ContentKeyPolicyOption()
                            .withName("ArmPolicyOptionName")
                            .withConfiguration(
                                new ContentKeyPolicyPlayReadyConfiguration()
                                    .withLicenses(
                                        Arrays
                                            .asList(
                                                new ContentKeyPolicyPlayReadyLicense()
                                                    .withAllowTestDevices(true)
                                                    .withBeginDate(OffsetDateTime.parse("2017-10-16T18:22:53.46Z"))
                                                    .withPlayRight(
                                                        new ContentKeyPolicyPlayReadyPlayRight()
                                                            .withScmsRestriction(2)
                                                            .withDigitalVideoOnlyContentRestriction(false)
                                                            .withImageConstraintForAnalogComponentVideoRestriction(true)
                                                            .withImageConstraintForAnalogComputerMonitorRestriction(
                                                                false)
                                                            .withAllowPassingVideoContentToUnknownOutput(
                                                                ContentKeyPolicyPlayReadyUnknownOutputPassingOption
                                                                    .NOT_ALLOWED))
                                                    .withLicenseType(ContentKeyPolicyPlayReadyLicenseType.PERSISTENT)
                                                    .withContentKeyLocation(
                                                        new ContentKeyPolicyPlayReadyContentEncryptionKeyFromHeader())
                                                    .withContentType(
                                                        ContentKeyPolicyPlayReadyContentType.ULTRA_VIOLET_DOWNLOAD))))
                            .withRestriction(new ContentKeyPolicyOpenRestriction())))
            .create();
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/content-key-policies-create-widevine-token.json
     */
    /**
     * Sample code: Creates a Content Key Policy with Widevine option and Token Restriction.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void createsAContentKeyPolicyWithWidevineOptionAndTokenRestriction(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .contentKeyPolicies()
            .define("PolicyWithWidevineOptionAndJwtTokenRestriction")
            .withExistingMediaService("contoso", "contosomedia")
            .withDescription("ArmPolicyDescription")
            .withOptions(
                Arrays
                    .asList(
                        new ContentKeyPolicyOption()
                            .withName("widevineoption")
                            .withConfiguration(
                                new ContentKeyPolicyWidevineConfiguration()
                                    .withWidevineTemplate(
                                        "{\"allowed_track_types\":\"SD_HD\",\"content_key_specs\":[{\"track_type\":\"SD\",\"security_level\":1,\"required_output_protection\":{\"hdcp\":\"HDCP_V2\"}}],\"policy_overrides\":{\"can_play\":true,\"can_persist\":true,\"can_renew\":false}}"))
                            .withRestriction(
                                new ContentKeyPolicyTokenRestriction()
                                    .withIssuer("urn:issuer")
                                    .withAudience("urn:audience")
                                    .withPrimaryVerificationKey(
                                        new ContentKeyPolicyRsaTokenKey()
                                            .withExponent("AQAB".getBytes())
                                            .withModulus("AQAD".getBytes()))
                                    .withAlternateVerificationKeys(
                                        Arrays
                                            .asList(
                                                new ContentKeyPolicySymmetricTokenKey()
                                                    .withKeyValue("AAAAAAAAAAAAAAAAAAAAAA==".getBytes())))
                                    .withRestrictionTokenType(ContentKeyPolicyRestrictionTokenType.JWT))))
            .create();
    }
}
```

### ContentKeyPolicies_Delete

```java
import com.azure.core.util.Context;

/** Samples for ContentKeyPolicies Delete. */
public final class ContentKeyPoliciesDeleteSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/content-key-policies-delete.json
     */
    /**
     * Sample code: Delete a Key Policy.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void deleteAKeyPolicy(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .contentKeyPolicies()
            .deleteWithResponse("contoso", "contosomedia", "PolicyWithPlayReadyOptionAndOpenRestriction", Context.NONE);
    }
}
```

### ContentKeyPolicies_Get

```java
import com.azure.core.util.Context;

/** Samples for ContentKeyPolicies Get. */
public final class ContentKeyPoliciesGetSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/content-key-policies-get-by-name.json
     */
    /**
     * Sample code: Get a Content Key Policy by name.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getAContentKeyPolicyByName(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .contentKeyPolicies()
            .getWithResponse("contoso", "contosomedia", "PolicyWithMultipleOptions", Context.NONE);
    }
}
```

### ContentKeyPolicies_GetPolicyPropertiesWithSecrets

```java
import com.azure.core.util.Context;

/** Samples for ContentKeyPolicies GetPolicyPropertiesWithSecrets. */
public final class ContentKeyPoliciesGetPolicyPropertiesWithSecretsSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/content-key-policies-get-with-secrets.json
     */
    /**
     * Sample code: Get an Content Key Policy with secrets.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getAnContentKeyPolicyWithSecrets(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .contentKeyPolicies()
            .getPolicyPropertiesWithSecretsWithResponse(
                "contoso", "contosomedia", "PolicyWithMultipleOptions", Context.NONE);
    }
}
```

### ContentKeyPolicies_List

```java
import com.azure.core.util.Context;

/** Samples for ContentKeyPolicies List. */
public final class ContentKeyPoliciesListSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/content-key-policies-list-all.json
     */
    /**
     * Sample code: Lists all Content Key Policies.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listsAllContentKeyPolicies(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.contentKeyPolicies().list("contoso", "contosomedia", null, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/content-key-policies-list-in-date-range.json
     */
    /**
     * Sample code: Lists Content Key Policies with created and last modified filters.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listsContentKeyPoliciesWithCreatedAndLastModifiedFilters(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .contentKeyPolicies()
            .list(
                "contoso",
                "contosomedia",
                "properties/lastModified gt 2016-06-01 and properties/created lt 2013-07-01",
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/content-key-policies-list-by-lastModified.json
     */
    /**
     * Sample code: Lists Content Key Policies ordered by last modified.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listsContentKeyPoliciesOrderedByLastModified(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .contentKeyPolicies()
            .list("contoso", "contosomedia", null, null, "properties/lastModified", Context.NONE);
    }
}
```

### ContentKeyPolicies_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mediaservices.models.ContentKeyPolicy;
import com.azure.resourcemanager.mediaservices.models.ContentKeyPolicyClearKeyConfiguration;
import com.azure.resourcemanager.mediaservices.models.ContentKeyPolicyOpenRestriction;
import com.azure.resourcemanager.mediaservices.models.ContentKeyPolicyOption;
import java.util.Arrays;

/** Samples for ContentKeyPolicies Update. */
public final class ContentKeyPoliciesUpdateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/content-key-policies-update.json
     */
    /**
     * Sample code: Update a Content Key Policy.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void updateAContentKeyPolicy(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        ContentKeyPolicy resource =
            manager
                .contentKeyPolicies()
                .getWithResponse("contoso", "contosomedia", "PolicyWithClearKeyOptionAndTokenRestriction", Context.NONE)
                .getValue();
        resource
            .update()
            .withDescription("Updated Policy")
            .withOptions(
                Arrays
                    .asList(
                        new ContentKeyPolicyOption()
                            .withName("ClearKeyOption")
                            .withConfiguration(new ContentKeyPolicyClearKeyConfiguration())
                            .withRestriction(new ContentKeyPolicyOpenRestriction())))
            .apply();
    }
}
```

### Jobs_CancelJob

```java
import com.azure.core.util.Context;

/** Samples for Jobs CancelJob. */
public final class JobsCancelJobSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/jobs-cancel.json
     */
    /**
     * Sample code: Cancel a Job.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void cancelAJob(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .jobs()
            .cancelJobWithResponse("contosoresources", "contosomedia", "exampleTransform", "job1", Context.NONE);
    }
}
```

### Jobs_Create

```java
import com.azure.resourcemanager.mediaservices.models.JobInputAsset;
import com.azure.resourcemanager.mediaservices.models.JobOutputAsset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Jobs Create. */
public final class JobsCreateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/jobs-create.json
     */
    /**
     * Sample code: Create a Job.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void createAJob(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .jobs()
            .define("job1")
            .withExistingTransform("contosoresources", "contosomedia", "exampleTransform")
            .withInput(new JobInputAsset().withAssetName("job1-InputAsset"))
            .withOutputs(Arrays.asList(new JobOutputAsset().withAssetName("job1-OutputAsset")))
            .withCorrelationData(mapOf("Key 2", "Value 2", "key1", "value1"))
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

### Jobs_Delete

```java
import com.azure.core.util.Context;

/** Samples for Jobs Delete. */
public final class JobsDeleteSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/jobs-delete.json
     */
    /**
     * Sample code: Delete a Job.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void deleteAJob(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .jobs()
            .deleteWithResponse("contosoresources", "contosomedia", "exampleTransform", "jobToDelete", Context.NONE);
    }
}
```

### Jobs_Get

```java
import com.azure.core.util.Context;

/** Samples for Jobs Get. */
public final class JobsGetSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/jobs-get-by-name.json
     */
    /**
     * Sample code: Get a Job by name.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getAJobByName(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.jobs().getWithResponse("contosoresources", "contosomedia", "exampleTransform", "job1", Context.NONE);
    }
}
```

### Jobs_List

```java
import com.azure.core.util.Context;

/** Samples for Jobs List. */
public final class JobsListSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/jobs-list-all-filter-by-state-ne.json
     */
    /**
     * Sample code: Lists Jobs for the Transform filter by state not equal.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listsJobsForTheTransformFilterByStateNotEqual(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .jobs()
            .list(
                "contosoresources",
                "contosomedia",
                "exampleTransform",
                "properties/state ne Microsoft.Media.JobState'processing'",
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/jobs-list-all-filter-by-state-eq.json
     */
    /**
     * Sample code: Lists Jobs for the Transform filter by state equal.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listsJobsForTheTransformFilterByStateEqual(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .jobs()
            .list(
                "contosoresources",
                "contosomedia",
                "exampleTransform",
                "properties/state eq Microsoft.Media.JobState'Processing'",
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/jobs-list-all-filter-by-name.json
     */
    /**
     * Sample code: Lists Jobs for the Transform filter by name.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listsJobsForTheTransformFilterByName(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .jobs()
            .list(
                "contosoresources",
                "contosomedia",
                "exampleTransform",
                "name eq 'job1' or name eq 'job2'",
                "name",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/jobs-list-all-filter-by-lastmodified.json
     */
    /**
     * Sample code: Lists Jobs for the Transform filter by lastmodified.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listsJobsForTheTransformFilterByLastmodified(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .jobs()
            .list(
                "contosoresources",
                "contosomedia",
                "exampleTransform",
                "properties/lastmodified ge 2021-11-01T00:00:10.0000000Z and properties/lastmodified le"
                    + " 2021-11-01T00:00:20.0000000Z",
                "properties/lastmodified desc",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/jobs-list-all-filter-by-created.json
     */
    /**
     * Sample code: Lists Jobs for the Transform filter by created.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listsJobsForTheTransformFilterByCreated(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .jobs()
            .list(
                "contosoresources",
                "contosomedia",
                "exampleTransform",
                "properties/created ge 2021-11-01T00:00:10.0000000Z and properties/created le"
                    + " 2021-11-01T00:00:20.0000000Z",
                "properties/created",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/jobs-list-all.json
     */
    /**
     * Sample code: Lists all of the Jobs for the Transform.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listsAllOfTheJobsForTheTransform(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.jobs().list("contosoresources", "contosomedia", "exampleTransform", null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/jobs-list-all-filter-by-name-and-state.json
     */
    /**
     * Sample code: Lists Jobs for the Transform filter by name and state.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listsJobsForTheTransformFilterByNameAndState(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .jobs()
            .list(
                "contosoresources",
                "contosomedia",
                "exampleTransform",
                "name eq 'job3' and properties/state eq Microsoft.Media.JobState'finished'",
                null,
                Context.NONE);
    }
}
```

### Jobs_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mediaservices.models.Job;
import com.azure.resourcemanager.mediaservices.models.JobInputAsset;
import com.azure.resourcemanager.mediaservices.models.JobOutputAsset;
import com.azure.resourcemanager.mediaservices.models.Priority;
import java.util.Arrays;

/** Samples for Jobs Update. */
public final class JobsUpdateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/jobs-update.json
     */
    /**
     * Sample code: Update a Job.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void updateAJob(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        Job resource =
            manager
                .jobs()
                .getWithResponse("contosoresources", "contosomedia", "exampleTransform", "job1", Context.NONE)
                .getValue();
        resource
            .update()
            .withDescription("Example job to illustrate update.")
            .withInput(new JobInputAsset().withAssetName("job1-InputAsset"))
            .withOutputs(Arrays.asList(new JobOutputAsset().withAssetName("job1-OutputAsset")))
            .withPriority(Priority.HIGH)
            .apply();
    }
}
```

### LiveEvents_Allocate

```java
import com.azure.core.util.Context;

/** Samples for LiveEvents Allocate. */
public final class LiveEventsAllocateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/liveevent-allocate.json
     */
    /**
     * Sample code: Allocate a LiveEvent.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void allocateALiveEvent(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.liveEvents().allocate("mediaresources", "slitestmedia10", "myLiveEvent1", Context.NONE);
    }
}
```

### LiveEvents_Create

```java
import com.azure.resourcemanager.mediaservices.models.IpAccessControl;
import com.azure.resourcemanager.mediaservices.models.IpRange;
import com.azure.resourcemanager.mediaservices.models.LiveEventInput;
import com.azure.resourcemanager.mediaservices.models.LiveEventInputAccessControl;
import com.azure.resourcemanager.mediaservices.models.LiveEventInputProtocol;
import com.azure.resourcemanager.mediaservices.models.LiveEventPreview;
import com.azure.resourcemanager.mediaservices.models.LiveEventPreviewAccessControl;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for LiveEvents Create. */
public final class LiveEventsCreateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/liveevent-create.json
     */
    /**
     * Sample code: Create a LiveEvent.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void createALiveEvent(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .liveEvents()
            .define("myLiveEvent1")
            .withRegion("West US")
            .withExistingMediaservice("mediaresources", "slitestmedia10")
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
            .withDescription("test event 1")
            .withInput(
                new LiveEventInput()
                    .withStreamingProtocol(LiveEventInputProtocol.RTMP)
                    .withAccessControl(
                        new LiveEventInputAccessControl()
                            .withIp(
                                new IpAccessControl()
                                    .withAllow(
                                        Arrays
                                            .asList(
                                                new IpRange()
                                                    .withName("AllowAll")
                                                    .withAddress("0.0.0.0")
                                                    .withSubnetPrefixLength(0)))))
                    .withKeyFrameIntervalDuration("PT6S"))
            .withPreview(
                new LiveEventPreview()
                    .withAccessControl(
                        new LiveEventPreviewAccessControl()
                            .withIp(
                                new IpAccessControl()
                                    .withAllow(
                                        Arrays
                                            .asList(
                                                new IpRange()
                                                    .withName("AllowAll")
                                                    .withAddress("0.0.0.0")
                                                    .withSubnetPrefixLength(0))))))
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

### LiveEvents_Delete

```java
import com.azure.core.util.Context;

/** Samples for LiveEvents Delete. */
public final class LiveEventsDeleteSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/liveevent-delete.json
     */
    /**
     * Sample code: Delete a LiveEvent.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void deleteALiveEvent(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.liveEvents().delete("mediaresources", "slitestmedia10", "myLiveEvent1", Context.NONE);
    }
}
```

### LiveEvents_Get

```java
import com.azure.core.util.Context;

/** Samples for LiveEvents Get. */
public final class LiveEventsGetSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/liveevent-list-by-name.json
     */
    /**
     * Sample code: Get a LiveEvent by name.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getALiveEventByName(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.liveEvents().getWithResponse("mediaresources", "slitestmedia10", "myLiveEvent1", Context.NONE);
    }
}
```

### LiveEvents_List

```java
import com.azure.core.util.Context;

/** Samples for LiveEvents List. */
public final class LiveEventsListSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/liveevent-list-all.json
     */
    /**
     * Sample code: List all LiveEvents.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listAllLiveEvents(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.liveEvents().list("mediaresources", "slitestmedia10", Context.NONE);
    }
}
```

### LiveEvents_Reset

```java
import com.azure.core.util.Context;

/** Samples for LiveEvents Reset. */
public final class LiveEventsResetSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/liveevent-reset.json
     */
    /**
     * Sample code: Reset a LiveEvent.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void resetALiveEvent(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.liveEvents().reset("mediaresources", "slitestmedia10", "myLiveEvent1", Context.NONE);
    }
}
```

### LiveEvents_Start

```java
import com.azure.core.util.Context;

/** Samples for LiveEvents Start. */
public final class LiveEventsStartSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/liveevent-start.json
     */
    /**
     * Sample code: Start a LiveEvent.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void startALiveEvent(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.liveEvents().start("mediaresources", "slitestmedia10", "myLiveEvent1", Context.NONE);
    }
}
```

### LiveEvents_Stop

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mediaservices.models.LiveEventActionInput;

/** Samples for LiveEvents Stop. */
public final class LiveEventsStopSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/liveevent-stop.json
     */
    /**
     * Sample code: Stop a LiveEvent.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void stopALiveEvent(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .liveEvents()
            .stop(
                "mediaresources",
                "slitestmedia10",
                "myLiveEvent1",
                new LiveEventActionInput().withRemoveOutputsOnStop(false),
                Context.NONE);
    }
}
```

### LiveEvents_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mediaservices.models.IpAccessControl;
import com.azure.resourcemanager.mediaservices.models.IpRange;
import com.azure.resourcemanager.mediaservices.models.LiveEvent;
import com.azure.resourcemanager.mediaservices.models.LiveEventInput;
import com.azure.resourcemanager.mediaservices.models.LiveEventInputAccessControl;
import com.azure.resourcemanager.mediaservices.models.LiveEventInputProtocol;
import com.azure.resourcemanager.mediaservices.models.LiveEventPreview;
import com.azure.resourcemanager.mediaservices.models.LiveEventPreviewAccessControl;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for LiveEvents Update. */
public final class LiveEventsUpdateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/liveevent-update.json
     */
    /**
     * Sample code: Update a LiveEvent.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void updateALiveEvent(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        LiveEvent resource =
            manager
                .liveEvents()
                .getWithResponse("mediaresources", "slitestmedia10", "myLiveEvent1", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("tag1", "value1", "tag2", "value2", "tag3", "value3"))
            .withDescription("test event updated")
            .withInput(
                new LiveEventInput()
                    .withStreamingProtocol(LiveEventInputProtocol.FRAGMENTED_MP4)
                    .withAccessControl(
                        new LiveEventInputAccessControl()
                            .withIp(
                                new IpAccessControl()
                                    .withAllow(
                                        Arrays.asList(new IpRange().withName("AllowOne").withAddress("192.1.1.0")))))
                    .withKeyFrameIntervalDuration("PT6S"))
            .withPreview(
                new LiveEventPreview()
                    .withAccessControl(
                        new LiveEventPreviewAccessControl()
                            .withIp(
                                new IpAccessControl()
                                    .withAllow(
                                        Arrays.asList(new IpRange().withName("AllowOne").withAddress("192.1.1.0"))))))
            .apply();
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

### LiveOutputs_Create

```java
import com.azure.resourcemanager.mediaservices.models.Hls;
import java.time.Duration;

/** Samples for LiveOutputs Create. */
public final class LiveOutputsCreateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/liveoutput-create.json
     */
    /**
     * Sample code: Create a LiveOutput.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void createALiveOutput(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .liveOutputs()
            .define("myLiveOutput1")
            .withExistingLiveEvent("mediaresources", "slitestmedia10", "myLiveEvent1")
            .withDescription("test live output 1")
            .withAssetName("6f3264f5-a189-48b4-a29a-a40f22575212")
            .withArchiveWindowLength(Duration.parse("PT5M"))
            .withManifestName("testmanifest")
            .withHls(new Hls().withFragmentsPerTsSegment(5))
            .create();
    }
}
```

### LiveOutputs_Delete

```java
import com.azure.core.util.Context;

/** Samples for LiveOutputs Delete. */
public final class LiveOutputsDeleteSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/liveoutput-delete.json
     */
    /**
     * Sample code: Delete a LiveOutput.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void deleteALiveOutput(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.liveOutputs().delete("mediaresources", "slitestmedia10", "myLiveEvent1", "myLiveOutput1", Context.NONE);
    }
}
```

### LiveOutputs_Get

```java
import com.azure.core.util.Context;

/** Samples for LiveOutputs Get. */
public final class LiveOutputsGetSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/liveoutput-list-by-name.json
     */
    /**
     * Sample code: Get a LiveOutput by name.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getALiveOutputByName(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .liveOutputs()
            .getWithResponse("mediaresources", "slitestmedia10", "myLiveEvent1", "myLiveOutput1", Context.NONE);
    }
}
```

### LiveOutputs_List

```java
import com.azure.core.util.Context;

/** Samples for LiveOutputs List. */
public final class LiveOutputsListSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/liveoutput-list-all.json
     */
    /**
     * Sample code: List all LiveOutputs.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listAllLiveOutputs(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.liveOutputs().list("mediaresources", "slitestmedia10", "myLiveEvent1", Context.NONE);
    }
}
```

### Locations_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mediaservices.models.CheckNameAvailabilityInput;

/** Samples for Locations CheckNameAvailability. */
public final class LocationsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/accounts-check-name-availability.json
     */
    /**
     * Sample code: Check Name Availability.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void checkNameAvailability(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .locations()
            .checkNameAvailabilityWithResponse(
                "japanwest",
                new CheckNameAvailabilityInput().withName("contosotv").withType("videoAnalyzers"),
                Context.NONE);
    }
}
```

### MediaServicesOperationResults_Get

```java
import com.azure.core.util.Context;

/** Samples for MediaServicesOperationResults Get. */
public final class MediaServicesOperationResultsGetSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/media-service-operation-result-by-id.json
     */
    /**
     * Sample code: Get status of asynchronous operation.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getStatusOfAsynchronousOperation(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .mediaServicesOperationResults()
            .getWithResponse("westus", "6FBA62C4-99B5-4FF8-9826-FC4744A8864F", Context.NONE);
    }
}
```

### MediaServicesOperationStatuses_Get

```java
import com.azure.core.util.Context;

/** Samples for MediaServicesOperationStatuses Get. */
public final class MediaServicesOperationStatusesGetSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/media-service-operation-status-by-id-non-terminal-state-failed.json
     */
    /**
     * Sample code: Get status of asynchronous operation when it is completed with error.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getStatusOfAsynchronousOperationWhenItIsCompletedWithError(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .mediaServicesOperationStatuses()
            .getWithResponse("westus", "D612C429-2526-49D5-961B-885AE11406FD", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/media-service-operation-status-by-id-terminal-state.json
     */
    /**
     * Sample code: Get status of asynchronous operation when it is completed.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getStatusOfAsynchronousOperationWhenItIsCompleted(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .mediaServicesOperationStatuses()
            .getWithResponse("westus", "D612C429-2526-49D5-961B-885AE11406FD", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/media-service-operation-status-by-id-non-terminal-state.json
     */
    /**
     * Sample code: Get status of asynchronous operation when it is ongoing.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getStatusOfAsynchronousOperationWhenItIsOngoing(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .mediaServicesOperationStatuses()
            .getWithResponse("westus", "D612C429-2526-49D5-961B-885AE11406FD", Context.NONE);
    }
}
```

### Mediaservices_CreateOrUpdate

```java
import com.azure.resourcemanager.mediaservices.models.AccessControl;
import com.azure.resourcemanager.mediaservices.models.AccountEncryption;
import com.azure.resourcemanager.mediaservices.models.AccountEncryptionKeyType;
import com.azure.resourcemanager.mediaservices.models.DefaultAction;
import com.azure.resourcemanager.mediaservices.models.KeyDelivery;
import com.azure.resourcemanager.mediaservices.models.MediaServiceIdentity;
import com.azure.resourcemanager.mediaservices.models.PublicNetworkAccess;
import com.azure.resourcemanager.mediaservices.models.ResourceIdentity;
import com.azure.resourcemanager.mediaservices.models.StorageAccount;
import com.azure.resourcemanager.mediaservices.models.StorageAccountType;
import com.azure.resourcemanager.mediaservices.models.StorageAuthentication;
import com.azure.resourcemanager.mediaservices.models.UserAssignedManagedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Mediaservices CreateOrUpdate. */
public final class MediaservicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/async-accounts-create.json
     */
    /**
     * Sample code: Create a Media Services account.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void createAMediaServicesAccount(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .mediaservices()
            .define("contososports")
            .withRegion("South Central US")
            .withExistingResourceGroup("contoso")
            .withTags(mapOf("key1", "value1", "key2", "value2"))
            .withIdentity(
                new MediaServiceIdentity()
                    .withType("UserAssigned")
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/contoso/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1",
                            new UserAssignedManagedIdentity(),
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/contoso/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id2",
                            new UserAssignedManagedIdentity())))
            .withStorageAccounts(
                Arrays
                    .asList(
                        new StorageAccount()
                            .withId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/contoso/providers/Microsoft.Storage/storageAccounts/contososportsstore")
                            .withType(StorageAccountType.PRIMARY)
                            .withIdentity(
                                new ResourceIdentity()
                                    .withUserAssignedIdentity(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/contoso/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1")
                                    .withUseSystemAssignedIdentity(false))))
            .withStorageAuthentication(StorageAuthentication.MANAGED_IDENTITY)
            .withEncryption(
                new AccountEncryption()
                    .withType(AccountEncryptionKeyType.CUSTOMER_KEY)
                    .withIdentity(
                        new ResourceIdentity()
                            .withUserAssignedIdentity(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/contoso/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1")
                            .withUseSystemAssignedIdentity(false)))
            .withKeyDelivery(
                new KeyDelivery().withAccessControl(new AccessControl().withDefaultAction(DefaultAction.ALLOW)))
            .withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
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

### Mediaservices_Delete

```java
import com.azure.core.util.Context;

/** Samples for Mediaservices Delete. */
public final class MediaservicesDeleteSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/accounts-delete.json
     */
    /**
     * Sample code: Delete a Media Services account.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void deleteAMediaServicesAccount(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.mediaservices().deleteWithResponse("contoso", "contososports", Context.NONE);
    }
}
```

### Mediaservices_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Mediaservices GetByResourceGroup. */
public final class MediaservicesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/accounts-get-by-name.json
     */
    /**
     * Sample code: Get a Media Services account by name.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getAMediaServicesAccountByName(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.mediaservices().getByResourceGroupWithResponse("contoso", "contosotv", Context.NONE);
    }
}
```

### Mediaservices_List

```java
import com.azure.core.util.Context;

/** Samples for Mediaservices List. */
public final class MediaservicesListSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/accounts-subscription-list-all-accounts.json
     */
    /**
     * Sample code: List all Media Services accounts.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listAllMediaServicesAccounts(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.mediaservices().list(Context.NONE);
    }
}
```

### Mediaservices_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Mediaservices ListByResourceGroup. */
public final class MediaservicesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/accounts-list-all-accounts.json
     */
    /**
     * Sample code: List all Media Services accounts.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listAllMediaServicesAccounts(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.mediaservices().listByResourceGroup("contoso", Context.NONE);
    }
}
```

### Mediaservices_ListEdgePolicies

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mediaservices.models.ListEdgePoliciesInput;

/** Samples for Mediaservices ListEdgePolicies. */
public final class MediaservicesListEdgePoliciesSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/accounts-list-media-edge-policies.json
     */
    /**
     * Sample code: List the media edge policies.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listTheMediaEdgePolicies(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .mediaservices()
            .listEdgePoliciesWithResponse(
                "contoso",
                "contososports",
                new ListEdgePoliciesInput().withDeviceId("contosiothubhost_contosoiotdevice"),
                Context.NONE);
    }
}
```

### Mediaservices_SyncStorageKeys

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mediaservices.models.SyncStorageKeysInput;

/** Samples for Mediaservices SyncStorageKeys. */
public final class MediaservicesSyncStorageKeysSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/accounts-sync-storage-keys.json
     */
    /**
     * Sample code: Synchronizes Storage Account Keys.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void synchronizesStorageAccountKeys(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .mediaservices()
            .syncStorageKeysWithResponse(
                "contoso", "contososports", new SyncStorageKeysInput().withId("contososportsstore"), Context.NONE);
    }
}
```

### Mediaservices_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mediaservices.models.MediaService;
import java.util.HashMap;
import java.util.Map;

/** Samples for Mediaservices Update. */
public final class MediaservicesUpdateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/async-accounts-update.json
     */
    /**
     * Sample code: Update a Media Services accounts.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void updateAMediaServicesAccounts(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        MediaService resource =
            manager.mediaservices().getByResourceGroupWithResponse("contoso", "contososports", Context.NONE).getValue();
        resource.update().withTags(mapOf("key1", "value3")).apply();
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

### OperationResults_Get

```java
import com.azure.core.util.Context;

/** Samples for OperationResults Get. */
public final class OperationResultsGetSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/asset-tracks-operation-result-by-id.json
     */
    /**
     * Sample code: Get result of asynchronous operation.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getResultOfAsynchronousOperation(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .operationResults()
            .getWithResponse(
                "contoso",
                "contosomedia",
                "ClimbingMountRainer",
                "text1",
                "e78f8d40-7aaa-4f2f-8ae6-73987e7c5a08",
                Context.NONE);
    }
}
```

### OperationStatuses_Get

```java
import com.azure.core.util.Context;

/** Samples for OperationStatuses Get. */
public final class OperationStatusesGetSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/asset-tracks-operation-status-by-id-terminal-state-failed.json
     */
    /**
     * Sample code: Get status of asynchronous operation when it is completed with error.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getStatusOfAsynchronousOperationWhenItIsCompletedWithError(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .operationStatuses()
            .getWithResponse(
                "contoso",
                "contosomedia",
                "ClimbingMountRainer",
                "text1",
                "86835197-3b47-402e-b313-70b82eaba296",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/asset-tracks-operation-status-by-id-terminal-state.json
     */
    /**
     * Sample code: Get status of asynchronous operation when it is completed.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getStatusOfAsynchronousOperationWhenItIsCompleted(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .operationStatuses()
            .getWithResponse(
                "contoso",
                "contosomedia",
                "ClimbingMountRainer",
                "text1",
                "e78f8d40-7aaa-4f2f-8ae6-73987e7c5a08",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/asset-tracks-operation-status-by-id-non-terminal-state.json
     */
    /**
     * Sample code: Get status of asynchronous operation when it is ongoing.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getStatusOfAsynchronousOperationWhenItIsOngoing(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .operationStatuses()
            .getWithResponse(
                "contoso",
                "contosomedia",
                "ClimbingMountRainer",
                "text1",
                "5827d9a1-1fb4-4e54-ac40-8eeed9b862c8",
                Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/operations-list-all.json
     */
    /**
     * Sample code: List Operations.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listOperations(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.operations().listWithResponse(Context.NONE);
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mediaservices.models.PrivateEndpointConnection;
import com.azure.resourcemanager.mediaservices.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.mediaservices.models.PrivateLinkServiceConnectionState;

/** Samples for PrivateEndpointConnections CreateOrUpdate. */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/private-endpoint-connection-put.json
     */
    /**
     * Sample code: Update private endpoint connection.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void updatePrivateEndpointConnection(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        PrivateEndpointConnection resource =
            manager
                .privateEndpointConnections()
                .getWithResponse("contoso", "contososports", "connectionName1", Context.NONE)
                .getValue();
        resource
            .update()
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState()
                    .withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("Test description."))
            .apply();
    }
}
```

### PrivateEndpointConnections_Delete

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Delete. */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/private-endpoint-connection-delete.json
     */
    /**
     * Sample code: Delete private endpoint connection.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void deletePrivateEndpointConnection(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .privateEndpointConnections()
            .deleteWithResponse("contoso", "contososports", "connectionName1", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/private-endpoint-connection-get-by-name.json
     */
    /**
     * Sample code: Get private endpoint connection.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getPrivateEndpointConnection(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse("contoso", "contososports", "connectionName1", Context.NONE);
    }
}
```

### PrivateEndpointConnections_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections List. */
public final class PrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/private-endpoint-connection-list.json
     */
    /**
     * Sample code: Get all private endpoint connections.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getAllPrivateEndpointConnections(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.privateEndpointConnections().listWithResponse("contoso", "contososports", Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources Get. */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/private-link-resources-get-by-name.json
     */
    /**
     * Sample code: Get details of a group ID.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getDetailsOfAGroupID(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.privateLinkResources().getWithResponse("contoso", "contososports", "keydelivery", Context.NONE);
    }
}
```

### PrivateLinkResources_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources List. */
public final class PrivateLinkResourcesListSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/private-link-resources-list.json
     */
    /**
     * Sample code: Get list of all group IDs.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getListOfAllGroupIDs(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.privateLinkResources().listWithResponse("contoso", "contososports", Context.NONE);
    }
}
```

### StreamingEndpoints_Create

```java
import com.azure.resourcemanager.mediaservices.models.AkamaiAccessControl;
import com.azure.resourcemanager.mediaservices.models.AkamaiSignatureHeaderAuthenticationKey;
import com.azure.resourcemanager.mediaservices.models.IpAccessControl;
import com.azure.resourcemanager.mediaservices.models.IpRange;
import com.azure.resourcemanager.mediaservices.models.StreamingEndpointAccessControl;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for StreamingEndpoints Create. */
public final class StreamingEndpointsCreateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streamingendpoint-create.json
     */
    /**
     * Sample code: Create a streaming endpoint.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void createAStreamingEndpoint(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .streamingEndpoints()
            .define("myStreamingEndpoint1")
            .withRegion("West US")
            .withExistingMediaservice("mediaresources", "slitestmedia10")
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
            .withDescription("test event 1")
            .withScaleUnits(1)
            .withAvailabilitySetName("availableset")
            .withAccessControl(
                new StreamingEndpointAccessControl()
                    .withAkamai(
                        new AkamaiAccessControl()
                            .withAkamaiSignatureHeaderAuthenticationKeyList(
                                Arrays
                                    .asList(
                                        new AkamaiSignatureHeaderAuthenticationKey()
                                            .withIdentifier("id1")
                                            .withBase64Key("dGVzdGlkMQ==")
                                            .withExpiration(OffsetDateTime.parse("2029-12-31T16:00:00-08:00")),
                                        new AkamaiSignatureHeaderAuthenticationKey()
                                            .withIdentifier("id2")
                                            .withBase64Key("dGVzdGlkMQ==")
                                            .withExpiration(OffsetDateTime.parse("2030-12-31T16:00:00-08:00")))))
                    .withIp(
                        new IpAccessControl()
                            .withAllow(Arrays.asList(new IpRange().withName("AllowedIp").withAddress("192.168.1.1")))))
            .withCdnEnabled(false)
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

### StreamingEndpoints_Delete

```java
import com.azure.core.util.Context;

/** Samples for StreamingEndpoints Delete. */
public final class StreamingEndpointsDeleteSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streamingendpoint-delete.json
     */
    /**
     * Sample code: Delete a streaming endpoint.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void deleteAStreamingEndpoint(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.streamingEndpoints().delete("mediaresources", "slitestmedia10", "myStreamingEndpoint1", Context.NONE);
    }
}
```

### StreamingEndpoints_Get

```java
import com.azure.core.util.Context;

/** Samples for StreamingEndpoints Get. */
public final class StreamingEndpointsGetSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streamingendpoint-list-by-name.json
     */
    /**
     * Sample code: Get a streaming endpoint by name.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getAStreamingEndpointByName(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .streamingEndpoints()
            .getWithResponse("mediaresources", "slitestmedia10", "myStreamingEndpoint1", Context.NONE);
    }
}
```

### StreamingEndpoints_List

```java
import com.azure.core.util.Context;

/** Samples for StreamingEndpoints List. */
public final class StreamingEndpointsListSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streamingendpoint-list-all.json
     */
    /**
     * Sample code: List all streaming endpoints.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listAllStreamingEndpoints(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.streamingEndpoints().list("mediaresources", "slitestmedia10", Context.NONE);
    }
}
```

### StreamingEndpoints_Scale

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mediaservices.models.StreamingEntityScaleUnit;

/** Samples for StreamingEndpoints Scale. */
public final class StreamingEndpointsScaleSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streamingendpoint-scale.json
     */
    /**
     * Sample code: Scale a StreamingEndpoint.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void scaleAStreamingEndpoint(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .streamingEndpoints()
            .scale(
                "mediaresources",
                "slitestmedia10",
                "myStreamingEndpoint1",
                new StreamingEntityScaleUnit().withScaleUnit(5),
                Context.NONE);
    }
}
```

### StreamingEndpoints_Skus

```java
import com.azure.core.util.Context;

/** Samples for StreamingEndpoints Skus. */
public final class StreamingEndpointsSkusSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streamingendpoint-list-skus.json
     */
    /**
     * Sample code: List a streaming endpoint sku.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listAStreamingEndpointSku(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .streamingEndpoints()
            .skusWithResponse("mediaresources", "slitestmedia10", "myStreamingEndpoint1", Context.NONE);
    }
}
```

### StreamingEndpoints_Start

```java
import com.azure.core.util.Context;

/** Samples for StreamingEndpoints Start. */
public final class StreamingEndpointsStartSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streamingendpoint-start.json
     */
    /**
     * Sample code: Start a streaming endpoint.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void startAStreamingEndpoint(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.streamingEndpoints().start("mediaresources", "slitestmedia10", "myStreamingEndpoint1", Context.NONE);
    }
}
```

### StreamingEndpoints_Stop

```java
import com.azure.core.util.Context;

/** Samples for StreamingEndpoints Stop. */
public final class StreamingEndpointsStopSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streamingendpoint-stop.json
     */
    /**
     * Sample code: Stop a streaming endpoint.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void stopAStreamingEndpoint(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.streamingEndpoints().stop("mediaresources", "slitestmedia10", "myStreamingEndpoint1", Context.NONE);
    }
}
```

### StreamingEndpoints_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mediaservices.models.StreamingEndpoint;
import java.util.HashMap;
import java.util.Map;

/** Samples for StreamingEndpoints Update. */
public final class StreamingEndpointsUpdateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streamingendpoint-update.json
     */
    /**
     * Sample code: Update a streaming endpoint.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void updateAStreamingEndpoint(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        StreamingEndpoint resource =
            manager
                .streamingEndpoints()
                .getWithResponse("mediaresources", "slitestmedia10", "myStreamingEndpoint1", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("tag3", "value3", "tag5", "value5"))
            .withDescription("test event 2")
            .withScaleUnits(5)
            .withAvailabilitySetName("availableset")
            .apply();
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

### StreamingLocators_Create

```java
import com.azure.resourcemanager.mediaservices.models.StreamingLocatorContentKey;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;

/** Samples for StreamingLocators Create. */
public final class StreamingLocatorsCreateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streaming-locators-create-clear.json
     */
    /**
     * Sample code: Creates a Streaming Locator with clear streaming.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void createsAStreamingLocatorWithClearStreaming(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .streamingLocators()
            .define("UserCreatedClearStreamingLocator")
            .withExistingMediaService("contoso", "contosomedia")
            .withAssetName("ClimbingMountRainier")
            .withStreamingPolicyName("clearStreamingPolicy")
            .create();
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streaming-locators-create-secure.json
     */
    /**
     * Sample code: Creates a Streaming Locator with secure streaming.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void createsAStreamingLocatorWithSecureStreaming(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .streamingLocators()
            .define("UserCreatedSecureStreamingLocator")
            .withExistingMediaService("contoso", "contosomedia")
            .withAssetName("ClimbingMountRainier")
            .withStartTime(OffsetDateTime.parse("2018-03-01T00:00:00Z"))
            .withEndTime(OffsetDateTime.parse("2028-12-31T23:59:59.9999999Z"))
            .withStreamingPolicyName("secureStreamingPolicy")
            .create();
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streaming-locators-create-secure-userDefinedContentKeys.json
     */
    /**
     * Sample code: Creates a Streaming Locator with user defined content keys.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void createsAStreamingLocatorWithUserDefinedContentKeys(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .streamingLocators()
            .define("UserCreatedSecureStreamingLocatorWithUserDefinedContentKeys")
            .withExistingMediaService("contoso", "contosomedia")
            .withAssetName("ClimbingMountRainier")
            .withStreamingLocatorId(UUID.fromString("90000000-0000-0000-0000-00000000000A"))
            .withStreamingPolicyName("secureStreamingPolicy")
            .withContentKeys(
                Arrays
                    .asList(
                        new StreamingLocatorContentKey()
                            .withId(UUID.fromString("60000000-0000-0000-0000-000000000001"))
                            .withLabelReferenceInStreamingPolicy("aesDefaultKey")
                            .withValue("1UqLohAfWsEGkULYxHjYZg=="),
                        new StreamingLocatorContentKey()
                            .withId(UUID.fromString("60000000-0000-0000-0000-000000000004"))
                            .withLabelReferenceInStreamingPolicy("cencDefaultKey")
                            .withValue("4UqLohAfWsEGkULYxHjYZg=="),
                        new StreamingLocatorContentKey()
                            .withId(UUID.fromString("60000000-0000-0000-0000-000000000007"))
                            .withLabelReferenceInStreamingPolicy("cbcsDefaultKey")
                            .withValue("7UqLohAfWsEGkULYxHjYZg==")))
            .create();
    }
}
```

### StreamingLocators_Delete

```java
import com.azure.core.util.Context;

/** Samples for StreamingLocators Delete. */
public final class StreamingLocatorsDeleteSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streaming-locators-delete.json
     */
    /**
     * Sample code: Delete a Streaming Locator.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void deleteAStreamingLocator(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .streamingLocators()
            .deleteWithResponse("contoso", "contosomedia", "clearStreamingLocator", Context.NONE);
    }
}
```

### StreamingLocators_Get

```java
import com.azure.core.util.Context;

/** Samples for StreamingLocators Get. */
public final class StreamingLocatorsGetSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streaming-locators-get-by-name.json
     */
    /**
     * Sample code: Get a Streaming Locator by name.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getAStreamingLocatorByName(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.streamingLocators().getWithResponse("contoso", "contosomedia", "clearStreamingLocator", Context.NONE);
    }
}
```

### StreamingLocators_List

```java
import com.azure.core.util.Context;

/** Samples for StreamingLocators List. */
public final class StreamingLocatorsListSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streaming-locators-list.json
     */
    /**
     * Sample code: Lists Streaming Locators.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listsStreamingLocators(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.streamingLocators().list("contoso", "contosomedia", null, null, null, Context.NONE);
    }
}
```

### StreamingLocators_ListContentKeys

```java
import com.azure.core.util.Context;

/** Samples for StreamingLocators ListContentKeys. */
public final class StreamingLocatorsListContentKeysSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streaming-locators-list-content-keys.json
     */
    /**
     * Sample code: List Content Keys.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listContentKeys(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .streamingLocators()
            .listContentKeysWithResponse("contoso", "contosomedia", "secureStreamingLocator", Context.NONE);
    }
}
```

### StreamingLocators_ListPaths

```java
import com.azure.core.util.Context;

/** Samples for StreamingLocators ListPaths. */
public final class StreamingLocatorsListPathsSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streaming-locators-list-paths-streaming-only.json
     */
    /**
     * Sample code: List Paths which has streaming paths only.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listPathsWhichHasStreamingPathsOnly(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .streamingLocators()
            .listPathsWithResponse("contoso", "contosomedia", "secureStreamingLocator", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streaming-locators-list-paths-streaming-and-download.json
     */
    /**
     * Sample code: List Paths which has streaming paths and download paths.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listPathsWhichHasStreamingPathsAndDownloadPaths(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .streamingLocators()
            .listPathsWithResponse("contoso", "contosomedia", "clearStreamingLocator", Context.NONE);
    }
}
```

### StreamingPolicies_Create

```java
import com.azure.resourcemanager.mediaservices.models.CbcsDrmConfiguration;
import com.azure.resourcemanager.mediaservices.models.CencDrmConfiguration;
import com.azure.resourcemanager.mediaservices.models.CommonEncryptionCbcs;
import com.azure.resourcemanager.mediaservices.models.CommonEncryptionCenc;
import com.azure.resourcemanager.mediaservices.models.DefaultKey;
import com.azure.resourcemanager.mediaservices.models.EnabledProtocols;
import com.azure.resourcemanager.mediaservices.models.EnvelopeEncryption;
import com.azure.resourcemanager.mediaservices.models.NoEncryption;
import com.azure.resourcemanager.mediaservices.models.StreamingPolicyContentKeys;
import com.azure.resourcemanager.mediaservices.models.StreamingPolicyFairPlayConfiguration;
import com.azure.resourcemanager.mediaservices.models.StreamingPolicyPlayReadyConfiguration;
import com.azure.resourcemanager.mediaservices.models.StreamingPolicyWidevineConfiguration;
import com.azure.resourcemanager.mediaservices.models.TrackPropertyCompareOperation;
import com.azure.resourcemanager.mediaservices.models.TrackPropertyCondition;
import com.azure.resourcemanager.mediaservices.models.TrackPropertyType;
import com.azure.resourcemanager.mediaservices.models.TrackSelection;
import java.util.Arrays;

/** Samples for StreamingPolicies Create. */
public final class StreamingPoliciesCreateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streaming-policies-create-secure-streaming.json
     */
    /**
     * Sample code: Creates a Streaming Policy with secure streaming.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void createsAStreamingPolicyWithSecureStreaming(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .streamingPolicies()
            .define("UserCreatedSecureStreamingPolicy")
            .withExistingMediaService("contoso", "contosomedia")
            .withDefaultContentKeyPolicyName("PolicyWithMultipleOptions")
            .withEnvelopeEncryption(
                new EnvelopeEncryption()
                    .withEnabledProtocols(
                        new EnabledProtocols()
                            .withDownload(false)
                            .withDash(true)
                            .withHls(true)
                            .withSmoothStreaming(true))
                    .withContentKeys(
                        new StreamingPolicyContentKeys().withDefaultKey(new DefaultKey().withLabel("aesDefaultKey")))
                    .withCustomKeyAcquisitionUrlTemplate(
                        "https://contoso.com/{AssetAlternativeId}/envelope/{ContentKeyId}"))
            .withCommonEncryptionCenc(
                new CommonEncryptionCenc()
                    .withEnabledProtocols(
                        new EnabledProtocols()
                            .withDownload(false)
                            .withDash(true)
                            .withHls(false)
                            .withSmoothStreaming(true))
                    .withClearTracks(
                        Arrays
                            .asList(
                                new TrackSelection()
                                    .withTrackSelections(
                                        Arrays
                                            .asList(
                                                new TrackPropertyCondition()
                                                    .withProperty(TrackPropertyType.FOUR_CC)
                                                    .withOperation(TrackPropertyCompareOperation.EQUAL)
                                                    .withValue("hev1")))))
                    .withContentKeys(
                        new StreamingPolicyContentKeys().withDefaultKey(new DefaultKey().withLabel("cencDefaultKey")))
                    .withDrm(
                        new CencDrmConfiguration()
                            .withPlayReady(
                                new StreamingPolicyPlayReadyConfiguration()
                                    .withCustomLicenseAcquisitionUrlTemplate(
                                        "https://contoso.com/{AssetAlternativeId}/playready/{ContentKeyId}")
                                    .withPlayReadyCustomAttributes("PlayReady CustomAttributes"))
                            .withWidevine(
                                new StreamingPolicyWidevineConfiguration()
                                    .withCustomLicenseAcquisitionUrlTemplate(
                                        "https://contoso.com/{AssetAlternativeId}/widevine/{ContentKeyId"))))
            .withCommonEncryptionCbcs(
                new CommonEncryptionCbcs()
                    .withEnabledProtocols(
                        new EnabledProtocols()
                            .withDownload(false)
                            .withDash(false)
                            .withHls(true)
                            .withSmoothStreaming(false))
                    .withContentKeys(
                        new StreamingPolicyContentKeys().withDefaultKey(new DefaultKey().withLabel("cbcsDefaultKey")))
                    .withDrm(
                        new CbcsDrmConfiguration()
                            .withFairPlay(
                                new StreamingPolicyFairPlayConfiguration()
                                    .withCustomLicenseAcquisitionUrlTemplate(
                                        "https://contoso.com/{AssetAlternativeId}/fairplay/{ContentKeyId}")
                                    .withAllowPersistentLicense(true))))
            .create();
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streaming-policies-create-commonEncryptionCenc-only.json
     */
    /**
     * Sample code: Creates a Streaming Policy with commonEncryptionCenc only.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void createsAStreamingPolicyWithCommonEncryptionCencOnly(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .streamingPolicies()
            .define("UserCreatedSecureStreamingPolicyWithCommonEncryptionCencOnly")
            .withExistingMediaService("contoso", "contosomedia")
            .withDefaultContentKeyPolicyName("PolicyWithPlayReadyOptionAndOpenRestriction")
            .withCommonEncryptionCenc(
                new CommonEncryptionCenc()
                    .withEnabledProtocols(
                        new EnabledProtocols()
                            .withDownload(false)
                            .withDash(true)
                            .withHls(false)
                            .withSmoothStreaming(true))
                    .withClearTracks(
                        Arrays
                            .asList(
                                new TrackSelection()
                                    .withTrackSelections(
                                        Arrays
                                            .asList(
                                                new TrackPropertyCondition()
                                                    .withProperty(TrackPropertyType.FOUR_CC)
                                                    .withOperation(TrackPropertyCompareOperation.EQUAL)
                                                    .withValue("hev1")))))
                    .withContentKeys(
                        new StreamingPolicyContentKeys().withDefaultKey(new DefaultKey().withLabel("cencDefaultKey")))
                    .withDrm(
                        new CencDrmConfiguration()
                            .withPlayReady(
                                new StreamingPolicyPlayReadyConfiguration()
                                    .withCustomLicenseAcquisitionUrlTemplate(
                                        "https://contoso.com/{AssetAlternativeId}/playready/{ContentKeyId}")
                                    .withPlayReadyCustomAttributes("PlayReady CustomAttributes"))
                            .withWidevine(
                                new StreamingPolicyWidevineConfiguration()
                                    .withCustomLicenseAcquisitionUrlTemplate(
                                        "https://contoso.com/{AssetAlternativeId}/widevine/{ContentKeyId"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streaming-policies-create-commonEncryptionCbcs-only.json
     */
    /**
     * Sample code: Creates a Streaming Policy with commonEncryptionCbcs only.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void createsAStreamingPolicyWithCommonEncryptionCbcsOnly(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .streamingPolicies()
            .define("UserCreatedSecureStreamingPolicyWithCommonEncryptionCbcsOnly")
            .withExistingMediaService("contoso", "contosomedia")
            .withDefaultContentKeyPolicyName("PolicyWithMultipleOptions")
            .withCommonEncryptionCbcs(
                new CommonEncryptionCbcs()
                    .withEnabledProtocols(
                        new EnabledProtocols()
                            .withDownload(false)
                            .withDash(false)
                            .withHls(true)
                            .withSmoothStreaming(false))
                    .withContentKeys(
                        new StreamingPolicyContentKeys().withDefaultKey(new DefaultKey().withLabel("cbcsDefaultKey")))
                    .withDrm(
                        new CbcsDrmConfiguration()
                            .withFairPlay(
                                new StreamingPolicyFairPlayConfiguration()
                                    .withCustomLicenseAcquisitionUrlTemplate(
                                        "https://contoso.com/{AssetAlternativeId}/fairplay/{ContentKeyId}")
                                    .withAllowPersistentLicense(true))))
            .create();
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streaming-policies-create-clear.json
     */
    /**
     * Sample code: Creates a Streaming Policy with clear streaming.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void createsAStreamingPolicyWithClearStreaming(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .streamingPolicies()
            .define("UserCreatedClearStreamingPolicy")
            .withExistingMediaService("contoso", "contosomedia")
            .withNoEncryption(
                new NoEncryption()
                    .withEnabledProtocols(
                        new EnabledProtocols()
                            .withDownload(true)
                            .withDash(true)
                            .withHls(true)
                            .withSmoothStreaming(true)))
            .create();
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streaming-policies-create-envelopeEncryption-only.json
     */
    /**
     * Sample code: Creates a Streaming Policy with envelopeEncryption only.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void createsAStreamingPolicyWithEnvelopeEncryptionOnly(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .streamingPolicies()
            .define("UserCreatedSecureStreamingPolicyWithEnvelopeEncryptionOnly")
            .withExistingMediaService("contoso", "contosomedia")
            .withDefaultContentKeyPolicyName("PolicyWithClearKeyOptionAndTokenRestriction")
            .withEnvelopeEncryption(
                new EnvelopeEncryption()
                    .withEnabledProtocols(
                        new EnabledProtocols()
                            .withDownload(false)
                            .withDash(true)
                            .withHls(true)
                            .withSmoothStreaming(true))
                    .withContentKeys(
                        new StreamingPolicyContentKeys().withDefaultKey(new DefaultKey().withLabel("aesDefaultKey")))
                    .withCustomKeyAcquisitionUrlTemplate(
                        "https://contoso.com/{AssetAlternativeId}/envelope/{ContentKeyId}"))
            .create();
    }
}
```

### StreamingPolicies_Delete

```java
import com.azure.core.util.Context;

/** Samples for StreamingPolicies Delete. */
public final class StreamingPoliciesDeleteSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streaming-policies-delete.json
     */
    /**
     * Sample code: Delete a Streaming Policy.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void deleteAStreamingPolicy(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .streamingPolicies()
            .deleteWithResponse(
                "contoso", "contosomedia", "secureStreamingPolicyWithCommonEncryptionCbcsOnly", Context.NONE);
    }
}
```

### StreamingPolicies_Get

```java
import com.azure.core.util.Context;

/** Samples for StreamingPolicies Get. */
public final class StreamingPoliciesGetSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streaming-policy-get-by-name.json
     */
    /**
     * Sample code: Get a Streaming Policy by name.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getAStreamingPolicyByName(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.streamingPolicies().getWithResponse("contoso", "contosomedia", "clearStreamingPolicy", Context.NONE);
    }
}
```

### StreamingPolicies_List

```java
import com.azure.core.util.Context;

/** Samples for StreamingPolicies List. */
public final class StreamingPoliciesListSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/streaming-policies-list.json
     */
    /**
     * Sample code: Lists Streaming Policies.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listsStreamingPolicies(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.streamingPolicies().list("contoso", "contosomedia", null, null, null, Context.NONE);
    }
}
```

### Tracks_CreateOrUpdate

```java
import com.azure.resourcemanager.mediaservices.models.TextTrack;
import com.azure.resourcemanager.mediaservices.models.Visibility;

/** Samples for Tracks CreateOrUpdate. */
public final class TracksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/asset-tracks-create.json
     */
    /**
     * Sample code: Creates a Track.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void createsATrack(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .tracks()
            .define("text3")
            .withExistingAsset("contoso", "contosomedia", "ClimbingMountRainer")
            .withTrack(
                new TextTrack()
                    .withFileName("text3.ttml")
                    .withDisplayName("A new track")
                    .withPlayerVisibility(Visibility.VISIBLE))
            .create();
    }
}
```

### Tracks_Delete

```java
import com.azure.core.util.Context;

/** Samples for Tracks Delete. */
public final class TracksDeleteSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/asset-tracks-delete.json
     */
    /**
     * Sample code: Delete a Track.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void deleteATrack(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.tracks().delete("contoso", "contosomedia", "ClimbingMountRainer", "text2", Context.NONE);
    }
}
```

### Tracks_Get

```java
import com.azure.core.util.Context;

/** Samples for Tracks Get. */
public final class TracksGetSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/asset-tracks-get-by-name.json
     */
    /**
     * Sample code: Get a Track by name.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getATrackByName(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.tracks().getWithResponse("contoso", "contosomedia", "ClimbingMountRainer", "text1", Context.NONE);
    }
}
```

### Tracks_List

```java
import com.azure.core.util.Context;

/** Samples for Tracks List. */
public final class TracksListSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/asset-tracks-list-all.json
     */
    /**
     * Sample code: Lists all Tracks.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listsAllTracks(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.tracks().list("contoso", "contosomedia", "ClimbingMountRainer", Context.NONE);
    }
}
```

### Tracks_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mediaservices.models.AssetTrack;
import com.azure.resourcemanager.mediaservices.models.TextTrack;

/** Samples for Tracks Update. */
public final class TracksUpdateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/asset-tracks-update.json
     */
    /**
     * Sample code: Update a Track.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void updateATrack(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        AssetTrack resource =
            manager
                .tracks()
                .getWithResponse("contoso", "contosomedia", "ClimbingMountRainer", "text1", Context.NONE)
                .getValue();
        resource.update().withTrack(new TextTrack().withDisplayName("A new name")).apply();
    }
}
```

### Tracks_UpdateTrackData

```java
import com.azure.core.util.Context;

/** Samples for Tracks UpdateTrackData. */
public final class TracksUpdateTrackDataSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/asset-tracks-update-data.json
     */
    /**
     * Sample code: Update the data for a tracks.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void updateTheDataForATracks(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.tracks().updateTrackData("contoso", "contosomedia", "ClimbingMountRainer", "text2", Context.NONE);
    }
}
```

### Transforms_CreateOrUpdate

```java
import com.azure.resourcemanager.mediaservices.models.BuiltInStandardEncoderPreset;
import com.azure.resourcemanager.mediaservices.models.EncoderNamedPreset;
import com.azure.resourcemanager.mediaservices.models.TransformOutput;
import java.util.Arrays;

/** Samples for Transforms CreateOrUpdate. */
public final class TransformsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/transforms-create.json
     */
    /**
     * Sample code: Create or update a Transform.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void createOrUpdateATransform(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .transforms()
            .define("createdTransform")
            .withExistingMediaService("contosoresources", "contosomedia")
            .withDescription("Example Transform to illustrate create and update.")
            .withOutputs(
                Arrays
                    .asList(
                        new TransformOutput()
                            .withPreset(
                                new BuiltInStandardEncoderPreset()
                                    .withPresetName(EncoderNamedPreset.ADAPTIVE_STREAMING))))
            .create();
    }
}
```

### Transforms_Delete

```java
import com.azure.core.util.Context;

/** Samples for Transforms Delete. */
public final class TransformsDeleteSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/transforms-delete.json
     */
    /**
     * Sample code: Delete a Transform.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void deleteATransform(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.transforms().deleteWithResponse("contosoresources", "contosomedia", "sampleTransform", Context.NONE);
    }
}
```

### Transforms_Get

```java
import com.azure.core.util.Context;

/** Samples for Transforms Get. */
public final class TransformsGetSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/transforms-get-by-name.json
     */
    /**
     * Sample code: Get a Transform by name.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void getATransformByName(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.transforms().getWithResponse("contosoresources", "contosomedia", "sampleTransform", Context.NONE);
    }
}
```

### Transforms_List

```java
import com.azure.core.util.Context;

/** Samples for Transforms List. */
public final class TransformsListSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/transforms-list-all-filter-by-created.json
     */
    /**
     * Sample code: Lists the Transforms filter by created.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listsTheTransformsFilterByCreated(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .transforms()
            .list(
                "contosoresources",
                "contosomedia",
                "properties/created gt 2021-11-01T00:00:00.0000000Z and properties/created le"
                    + " 2021-11-01T00:00:10.0000000Z",
                "properties/created",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/transforms-list-all-filter-by-name.json
     */
    /**
     * Sample code: Lists the Transforms filter by name.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listsTheTransformsFilterByName(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .transforms()
            .list(
                "contosoresources",
                "contosomedia",
                "(name eq 'sampleEncode') or (name eq 'sampleEncodeAndVideoIndex')",
                "name desc",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/transforms-list-all-filter-by-lastmodified.json
     */
    /**
     * Sample code: Lists the Transforms filter by lastmodified.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listsTheTransformsFilterByLastmodified(
        com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager
            .transforms()
            .list(
                "contosoresources",
                "contosomedia",
                "properties/lastmodified gt 2021-11-01T00:00:00.0000000Z and properties/lastmodified le"
                    + " 2021-11-01T00:00:10.0000000Z",
                "properties/lastmodified desc",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/transforms-list-all.json
     */
    /**
     * Sample code: Lists the Transforms.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void listsTheTransforms(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        manager.transforms().list("contosoresources", "contosomedia", null, null, Context.NONE);
    }
}
```

### Transforms_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mediaservices.models.BuiltInStandardEncoderPreset;
import com.azure.resourcemanager.mediaservices.models.EncoderNamedPreset;
import com.azure.resourcemanager.mediaservices.models.Priority;
import com.azure.resourcemanager.mediaservices.models.Transform;
import com.azure.resourcemanager.mediaservices.models.TransformOutput;
import java.util.Arrays;

/** Samples for Transforms Update. */
public final class TransformsUpdateSamples {
    /*
     * x-ms-original-file: specification/mediaservices/resource-manager/Microsoft.Media/stable/2021-11-01/examples/transforms-update.json
     */
    /**
     * Sample code: Update a Transform.
     *
     * @param manager Entry point to MediaServicesManager.
     */
    public static void updateATransform(com.azure.resourcemanager.mediaservices.MediaServicesManager manager) {
        Transform resource =
            manager
                .transforms()
                .getWithResponse("contosoresources", "contosomedia", "transformToUpdate", Context.NONE)
                .getValue();
        resource
            .update()
            .withDescription("Example transform to illustrate update.")
            .withOutputs(
                Arrays
                    .asList(
                        new TransformOutput()
                            .withRelativePriority(Priority.HIGH)
                            .withPreset(
                                new BuiltInStandardEncoderPreset()
                                    .withPresetName(EncoderNamedPreset.H264MULTIPLE_BITRATE720P))))
            .apply();
    }
}
```

