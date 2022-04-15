# Code snippets and samples


## Operations

- [List](#operations_list)

## ResourceProvider

- [Resources](#resourceprovider_resources)
### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/OperationsList.json
     */
    /**
     * Sample code: OperationsList.
     *
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void operationsList(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### ResourceProvider_Resources

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.resourcegraph.models.FacetRequest;
import com.azure.resourcemanager.resourcegraph.models.FacetRequestOptions;
import com.azure.resourcemanager.resourcegraph.models.FacetSortOrder;
import com.azure.resourcemanager.resourcegraph.models.QueryRequest;
import com.azure.resourcemanager.resourcegraph.models.QueryRequestOptions;
import java.util.Arrays;

/** Samples for ResourceProvider Resources. */
public final class ResourceProviderResourcesSamples {
    /*
     * x-ms-original-file: specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/ResourcesMgBasicQuery.json
     */
    /**
     * Sample code: Basic management group query.
     *
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void basicManagementGroupQuery(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager
            .resourceProviders()
            .resourcesWithResponse(
                new QueryRequest()
                    .withManagementGroups(Arrays.asList("e927f598-c1d4-4f72-8541-95d83a6a4ac8", "ProductionMG"))
                    .withQuery("Resources | project id, name, type, location, tags | limit 3"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/ResourcesBasicQuery.json
     */
    /**
     * Sample code: Basic query.
     *
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void basicQuery(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager
            .resourceProviders()
            .resourcesWithResponse(
                new QueryRequest()
                    .withSubscriptions(Arrays.asList("cfbbd179-59d2-4052-aa06-9270a38aa9d6"))
                    .withQuery("Resources | project id, name, type, location, tags | limit 3"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/ResourcesFacetQuery.json
     */
    /**
     * Sample code: Query with a facet request.
     *
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void queryWithAFacetRequest(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager
            .resourceProviders()
            .resourcesWithResponse(
                new QueryRequest()
                    .withSubscriptions(Arrays.asList("cfbbd179-59d2-4052-aa06-9270a38aa9d6"))
                    .withQuery(
                        "Resources | where type =~ 'Microsoft.Compute/virtualMachines' | project id, name, location,"
                            + " resourceGroup, properties.storageProfile.osDisk.osType | limit 5")
                    .withFacets(
                        Arrays
                            .asList(
                                new FacetRequest()
                                    .withExpression("location")
                                    .withOptions(
                                        new FacetRequestOptions().withSortOrder(FacetSortOrder.DESC).withTop(3)),
                                new FacetRequest()
                                    .withExpression("properties.storageProfile.osDisk.osType")
                                    .withOptions(
                                        new FacetRequestOptions().withSortOrder(FacetSortOrder.DESC).withTop(3)),
                                new FacetRequest()
                                    .withExpression("nonExistingColumn")
                                    .withOptions(
                                        new FacetRequestOptions().withSortOrder(FacetSortOrder.DESC).withTop(3)),
                                new FacetRequest()
                                    .withExpression("resourceGroup")
                                    .withOptions(
                                        new FacetRequestOptions()
                                            .withSortBy("tolower(resourceGroup)")
                                            .withSortOrder(FacetSortOrder.ASC)
                                            .withTop(3)),
                                new FacetRequest()
                                    .withExpression("resourceGroup")
                                    .withOptions(
                                        new FacetRequestOptions()
                                            .withFilter("resourceGroup contains 'test'")
                                            .withTop(3)))),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/ResourcesFilterQuery.json
     */
    /**
     * Sample code: Filter resources.
     *
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void filterResources(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager
            .resourceProviders()
            .resourcesWithResponse(
                new QueryRequest()
                    .withSubscriptions(Arrays.asList("cfbbd179-59d2-4052-aa06-9270a38aa9d6"))
                    .withQuery(
                        "Resources | project id, name, type, location | where type =~"
                            + " 'Microsoft.Compute/virtualMachines' | limit 3"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/ResourcesSummarizeQuery.json
     */
    /**
     * Sample code: Summarize resources by location.
     *
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void summarizeResourcesByLocation(
        com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager
            .resourceProviders()
            .resourcesWithResponse(
                new QueryRequest()
                    .withSubscriptions(Arrays.asList("cfbbd179-59d2-4052-aa06-9270a38aa9d6"))
                    .withQuery("Resources | project id, name, type, location | summarize by location"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/ResourcesPropertiesQuery.json
     */
    /**
     * Sample code: Access a properties field.
     *
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void accessAPropertiesField(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager
            .resourceProviders()
            .resourcesWithResponse(
                new QueryRequest()
                    .withSubscriptions(Arrays.asList("cfbbd179-59d2-4052-aa06-9270a38aa9d6"))
                    .withQuery(
                        "Resources | where type =~ 'Microsoft.Compute/virtualMachines' | summarize count() by"
                            + " tostring(properties.storageProfile.osDisk.osType)"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/ResourcesComplexQuery.json
     */
    /**
     * Sample code: Complex query.
     *
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void complexQuery(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager
            .resourceProviders()
            .resourcesWithResponse(
                new QueryRequest()
                    .withSubscriptions(Arrays.asList("cfbbd179-59d2-4052-aa06-9270a38aa9d6"))
                    .withQuery(
                        "Resources | project id, name, type, location | where type =~"
                            + " 'Microsoft.Compute/virtualMachines' | summarize count() by location | top 3 by count_"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/ResourcesNextPageQuery.json
     */
    /**
     * Sample code: Next page query.
     *
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void nextPageQuery(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager
            .resourceProviders()
            .resourcesWithResponse(
                new QueryRequest()
                    .withSubscriptions(Arrays.asList("cfbbd179-59d2-4052-aa06-9270a38aa9d6"))
                    .withQuery("Resources | where name contains 'test' | project id, name, type, location")
                    .withOptions(
                        new QueryRequestOptions()
                            .withSkipToken("eyAibm8iOiAibHVjayIsICJidXQiOiAibmljZSIsICJ0cnkiOiAiISIgfQ==")),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/ResourcesFirstPageQuery.json
     */
    /**
     * Sample code: First page query.
     *
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void firstPageQuery(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager
            .resourceProviders()
            .resourcesWithResponse(
                new QueryRequest()
                    .withSubscriptions(Arrays.asList("cfbbd179-59d2-4052-aa06-9270a38aa9d6"))
                    .withQuery("Resources | where name contains 'test' | project id, name, type, location")
                    .withOptions(new QueryRequestOptions().withTop(3).withSkip(0)),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/ResourcesTenantBasicQuery.json
     */
    /**
     * Sample code: Basic tenant query.
     *
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void basicTenantQuery(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager
            .resourceProviders()
            .resourcesWithResponse(
                new QueryRequest().withQuery("Resources | project id, name, type, location, tags | limit 3"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/ResourcesRandomPageQuery.json
     */
    /**
     * Sample code: Random page query.
     *
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void randomPageQuery(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager
            .resourceProviders()
            .resourcesWithResponse(
                new QueryRequest()
                    .withSubscriptions(Arrays.asList("cfbbd179-59d2-4052-aa06-9270a38aa9d6"))
                    .withQuery("Resources | where name contains 'test' | project id, name, type, location")
                    .withOptions(new QueryRequestOptions().withTop(2).withSkip(10)),
                Context.NONE);
    }
}
```

