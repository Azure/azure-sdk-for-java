# Code snippets and samples


## ResourceProvider

- [ResourceChangeDetails](#resourceprovider_resourcechangedetails)
- [ResourceChanges](#resourceprovider_resourcechanges)
- [ResourcesHistory](#resourceprovider_resourceshistory)
### ResourceProvider_ResourceChangeDetails

```java
import com.azure.resourcemanager.resourcegraph.models.ResourceChangeDetailsRequestParameters;
import java.util.Arrays;

/**
 * Samples for ResourceProvider ResourceChangeDetails.
 */
public final class ResourceProviderResourceChangeDetailsSamples {
    /*
     * x-ms-original-file: 2020-09-01-preview/ResourceChangeDetails.json
     */
    /**
     * Sample code: Basic query.
     * 
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void basicQuery(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager.resourceProviders()
            .resourceChangeDetailsWithResponse(
                new ResourceChangeDetailsRequestParameters().withResourceIds(Arrays.asList(
                    "/subscriptions/4d962866-1e3f-47f2-bd18-450c08f914c1/resourceGroups/MyResourceGroup/providers/Microsoft.Storage/storageAccounts/mystorageaccount"))
                    .withChangeIds(Arrays.asList("53dc0515-b86b-4bc2-979b-e4694ab4a556")),
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_ResourceChanges

```java
import com.azure.resourcemanager.resourcegraph.models.ResourceChangesRequestParameters;
import com.azure.resourcemanager.resourcegraph.models.ResourceChangesRequestParametersInterval;
import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Samples for ResourceProvider ResourceChanges.
 */
public final class ResourceProviderResourceChangesSamples {
    /*
     * x-ms-original-file: 2020-09-01-preview/ResourceChanges.json
     */
    /**
     * Sample code: Basic query.
     * 
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void basicQuery(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager.resourceProviders()
            .resourceChangesWithResponse(new ResourceChangesRequestParameters().withResourceIds(Arrays.asList(
                "/subscriptions/4d962866-1e3f-47f2-bd18-450c08f914c1/resourceGroups/MyResourceGroup/providers/Microsoft.Storage/storageAccounts/mystorageaccount"))
                .withInterval(new ResourceChangesRequestParametersInterval()
                    .withStart(OffsetDateTime.parse("2018-10-30T12:09:03.141Z"))
                    .withEnd(OffsetDateTime.parse("2018-10-31T12:09:03.141Z"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2020-09-01-preview/ResourceChangesNextPage.json
     */
    /**
     * Sample code: Next page query.
     * 
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void nextPageQuery(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager.resourceProviders()
            .resourceChangesWithResponse(new ResourceChangesRequestParameters().withResourceIds(Arrays.asList(
                "/subscriptions/4d962866-1e3f-47f2-bd18-450c08f914c1/resourceGroups/MyResourceGroup/providers/Microsoft.Storage/storageAccounts/mystorageaccount"))
                .withInterval(new ResourceChangesRequestParametersInterval()
                    .withStart(OffsetDateTime.parse("2018-10-30T12:09:03.141Z"))
                    .withEnd(OffsetDateTime.parse("2018-10-31T12:09:03.141Z")))
                .withSkipToken("fakeTokenPlaceholder")
                .withTop(2), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2020-09-01-preview/ResourceChangesFirstPage.json
     */
    /**
     * Sample code: First page query.
     * 
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void firstPageQuery(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager.resourceProviders()
            .resourceChangesWithResponse(new ResourceChangesRequestParameters().withResourceIds(Arrays.asList(
                "/subscriptions/4d962866-1e3f-47f2-bd18-450c08f914c1/resourceGroups/MyResourceGroup/providers/Microsoft.Storage/storageAccounts/mystorageaccount"))
                .withInterval(new ResourceChangesRequestParametersInterval()
                    .withStart(OffsetDateTime.parse("2018-10-30T12:09:03.141Z"))
                    .withEnd(OffsetDateTime.parse("2018-10-31T12:09:03.141Z")))
                .withTop(2), com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_ResourcesHistory

```java
import com.azure.resourcemanager.resourcegraph.models.DateTimeInterval;
import com.azure.resourcemanager.resourcegraph.models.ResourcesHistoryRequest;
import com.azure.resourcemanager.resourcegraph.models.ResourcesHistoryRequestOptions;
import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Samples for ResourceProvider ResourcesHistory.
 */
public final class ResourceProviderResourcesHistorySamples {
    /*
     * x-ms-original-file: 2021-06-01-preview/ResourcesHistoryMgsGet.json
     */
    /**
     * Sample code: Resource History Management Group scope Query.
     * 
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void
        resourceHistoryManagementGroupScopeQuery(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager.resourceProviders()
            .resourcesHistoryWithResponse(
                new ResourcesHistoryRequest()
                    .withQuery("where name =~ 'cpu-utilization' | project id, name, properties")
                    .withOptions(new ResourcesHistoryRequestOptions().withInterval(
                        new DateTimeInterval().withStart(OffsetDateTime.parse("2020-11-12T01:00:00.0000000Z"))
                            .withEnd(OffsetDateTime.parse("2020-11-12T01:25:00.0000000Z"))))
                    .withManagementGroups(Arrays.asList("e927f598-c1d4-4f72-8541-95d83a6a4ac8", "ProductionMG")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2021-06-01-preview/ResourcesHistoryGet.json
     */
    /**
     * Sample code: Resource History Query.
     * 
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void resourceHistoryQuery(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager.resourceProviders()
            .resourcesHistoryWithResponse(
                new ResourcesHistoryRequest().withSubscriptions(Arrays.asList("a7f33fdb-e646-4f15-89aa-3a360210861e"))
                    .withQuery("where name =~ 'cpu-utilization' | project id, name, properties")
                    .withOptions(new ResourcesHistoryRequestOptions().withInterval(
                        new DateTimeInterval().withStart(OffsetDateTime.parse("2020-11-12T01:00:00.0000000Z"))
                            .withEnd(OffsetDateTime.parse("2020-11-12T01:25:00.0000000Z")))),
                com.azure.core.util.Context.NONE);
    }
}
```

