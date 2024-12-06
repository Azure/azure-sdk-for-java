# Code snippets and samples


## GraphQuery

- [CreateOrUpdate](#graphquery_createorupdate)
- [Delete](#graphquery_delete)
- [GetByResourceGroup](#graphquery_getbyresourcegroup)
- [List](#graphquery_list)
- [ListByResourceGroup](#graphquery_listbyresourcegroup)
- [Update](#graphquery_update)

## Operations

- [List](#operations_list)

## ResourceProvider

- [Resources](#resourceprovider_resources)
### GraphQuery_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for GraphQuery CreateOrUpdate.
 */
public final class GraphQueryCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/GraphQueryAdd.
     * json
     */
    /**
     * Sample code: Create Graph Query.
     * 
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void createGraphQuery(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager.graphQueries()
            .define("MyDockerVMs")
            .withRegion((String) null)
            .withExistingResourceGroup("024e2271-06fa-46b6-9079-f1ed3c7b070e", "my-resource-group")
            .withTags(mapOf())
            .withDescription("Docker VMs in PROD")
            .withQuery("where isnotnull(tags['Prod']) and properties.extensions[0].Name == 'docker'")
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

### GraphQuery_Delete

```java
/**
 * Samples for GraphQuery Delete.
 */
public final class GraphQueryDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/GraphQueryDelete.
     * json
     */
    /**
     * Sample code: Delete Graph Query.
     * 
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void deleteGraphQuery(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager.graphQueries()
            .deleteWithResponse("024e2271-06fa-46b6-9079-f1ed3c7b070e", "my-resource-group", "MyDockerVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### GraphQuery_GetByResourceGroup

```java
/**
 * Samples for GraphQuery GetByResourceGroup.
 */
public final class GraphQueryGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/GraphQueryGet.
     * json
     */
    /**
     * Sample code: Get Graph Query.
     * 
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void getGraphQuery(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager.graphQueries()
            .getByResourceGroupWithResponse("024e2271-06fa-46b6-9079-f1ed3c7b070e", "my-resource-group", "MyDockerVMs",
                com.azure.core.util.Context.NONE);
    }
}
```

### GraphQuery_List

```java
/**
 * Samples for GraphQuery List.
 */
public final class GraphQueryListSamples {
    /*
     * x-ms-original-file:
     * specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/GraphQueryList.
     * json
     */
    /**
     * Sample code: Get a list of Graph Queries.
     * 
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void getAListOfGraphQueries(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager.graphQueries().list("024e2271-06fa-46b6-9079-f1ed3c7b070e", com.azure.core.util.Context.NONE);
    }
}
```

### GraphQuery_ListByResourceGroup

```java
/**
 * Samples for GraphQuery ListByResourceGroup.
 */
public final class GraphQueryListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/GraphQueryList.
     * json
     */
    /**
     * Sample code: Get a list of Graph Queries.
     * 
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void getAListOfGraphQueries(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager.graphQueries()
            .listByResourceGroup("024e2271-06fa-46b6-9079-f1ed3c7b070e", "my-resource-group",
                com.azure.core.util.Context.NONE);
    }
}
```

### GraphQuery_Update

```java
import com.azure.resourcemanager.resourcegraph.models.GraphQueryResource;

/**
 * Samples for GraphQuery Update.
 */
public final class GraphQueryUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/GraphQueryUpdate.
     * json
     */
    /**
     * Sample code: Update Graph Query.
     * 
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void updateGraphQuery(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        GraphQueryResource resource = manager.graphQueries()
            .getByResourceGroupWithResponse("024e2271-06fa-46b6-9079-f1ed3c7b070e", "my-resource-group", "MyDockerVMs",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withEtag("b0809832-ca62-4133-8f13-0c46580f9db1")
            .withDescription("Modified description")
            .withQuery("where isnotnull(tags['Prod']) and properties.extensions[0].Name == 'docker'")
            .apply();
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
     * specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/OperationsList.
     * json
     */
    /**
     * Sample code: OperationsList.
     * 
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void operationsList(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_Resources

```java
import com.azure.resourcemanager.resourcegraph.models.FacetRequest;
import com.azure.resourcemanager.resourcegraph.models.FacetRequestOptions;
import com.azure.resourcemanager.resourcegraph.models.FacetSortOrder;
import com.azure.resourcemanager.resourcegraph.models.QueryRequest;
import com.azure.resourcemanager.resourcegraph.models.QueryRequestOptions;
import java.util.Arrays;

/**
 * Samples for ResourceProvider Resources.
 */
public final class ResourceProviderResourcesSamples {
    /*
     * x-ms-original-file:
     * specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/
     * ResourcesMgBasicQuery.json
     */
    /**
     * Sample code: Basic management group query.
     * 
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void basicManagementGroupQuery(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager.resourceProviders()
            .resourcesWithResponse(
                new QueryRequest()
                    .withManagementGroups(Arrays.asList("e927f598-c1d4-4f72-8541-95d83a6a4ac8", "ProductionMG"))
                    .withQuery("Resources | project id, name, type, location, tags | limit 3"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/
     * ResourcesBasicQuery.json
     */
    /**
     * Sample code: Basic query.
     * 
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void basicQuery(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager.resourceProviders()
            .resourcesWithResponse(
                new QueryRequest().withSubscriptions(Arrays.asList("cfbbd179-59d2-4052-aa06-9270a38aa9d6"))
                    .withQuery("Resources | project id, name, type, location, tags | limit 3"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/
     * ResourcesFacetQuery.json
     */
    /**
     * Sample code: Query with a facet request.
     * 
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void queryWithAFacetRequest(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager.resourceProviders()
            .resourcesWithResponse(new QueryRequest()
                .withSubscriptions(Arrays.asList("cfbbd179-59d2-4052-aa06-9270a38aa9d6"))
                .withQuery(
                    "Resources | where type =~ 'Microsoft.Compute/virtualMachines' | project id, name, location, resourceGroup, properties.storageProfile.osDisk.osType | limit 5")
                .withFacets(Arrays.asList(
                    new FacetRequest().withExpression("location")
                        .withOptions(new FacetRequestOptions().withSortOrder(FacetSortOrder.DESC).withTop(3)),
                    new FacetRequest().withExpression("properties.storageProfile.osDisk.osType")
                        .withOptions(new FacetRequestOptions().withSortOrder(FacetSortOrder.DESC).withTop(3)),
                    new FacetRequest().withExpression("nonExistingColumn")
                        .withOptions(new FacetRequestOptions().withSortOrder(FacetSortOrder.DESC).withTop(3)),
                    new FacetRequest().withExpression("resourceGroup")
                        .withOptions(new FacetRequestOptions().withSortBy("tolower(resourceGroup)")
                            .withSortOrder(FacetSortOrder.ASC)
                            .withTop(3)),
                    new FacetRequest().withExpression("resourceGroup")
                        .withOptions(
                            new FacetRequestOptions().withFilter("resourceGroup contains 'test'").withTop(3)))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/
     * ResourcesFilterQuery.json
     */
    /**
     * Sample code: Filter resources.
     * 
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void filterResources(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager.resourceProviders()
            .resourcesWithResponse(new QueryRequest()
                .withSubscriptions(Arrays.asList("cfbbd179-59d2-4052-aa06-9270a38aa9d6"))
                .withQuery(
                    "Resources | project id, name, type, location | where type =~ 'Microsoft.Compute/virtualMachines' | limit 3"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/
     * ResourcesSummarizeQuery.json
     */
    /**
     * Sample code: Summarize resources by location.
     * 
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void
        summarizeResourcesByLocation(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager.resourceProviders()
            .resourcesWithResponse(
                new QueryRequest().withSubscriptions(Arrays.asList("cfbbd179-59d2-4052-aa06-9270a38aa9d6"))
                    .withQuery("Resources | project id, name, type, location | summarize by location"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/
     * ResourcesPropertiesQuery.json
     */
    /**
     * Sample code: Access a properties field.
     * 
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void accessAPropertiesField(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager.resourceProviders()
            .resourcesWithResponse(new QueryRequest()
                .withSubscriptions(Arrays.asList("cfbbd179-59d2-4052-aa06-9270a38aa9d6"))
                .withQuery(
                    "Resources | where type =~ 'Microsoft.Compute/virtualMachines' | summarize count() by tostring(properties.storageProfile.osDisk.osType)"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/
     * ResourcesComplexQuery.json
     */
    /**
     * Sample code: Complex query.
     * 
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void complexQuery(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager.resourceProviders()
            .resourcesWithResponse(new QueryRequest()
                .withSubscriptions(Arrays.asList("cfbbd179-59d2-4052-aa06-9270a38aa9d6"))
                .withQuery(
                    "Resources | project id, name, type, location | where type =~ 'Microsoft.Compute/virtualMachines' | summarize count() by location | top 3 by count_"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/
     * ResourcesNextPageQuery.json
     */
    /**
     * Sample code: Next page query.
     * 
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void nextPageQuery(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager.resourceProviders()
            .resourcesWithResponse(
                new QueryRequest().withSubscriptions(Arrays.asList("cfbbd179-59d2-4052-aa06-9270a38aa9d6"))
                    .withQuery("Resources | where name contains 'test' | project id, name, type, location")
                    .withOptions(new QueryRequestOptions().withSkipToken("fakeTokenPlaceholder")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/
     * ResourcesFirstPageQuery.json
     */
    /**
     * Sample code: First page query.
     * 
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void firstPageQuery(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager.resourceProviders()
            .resourcesWithResponse(
                new QueryRequest().withSubscriptions(Arrays.asList("cfbbd179-59d2-4052-aa06-9270a38aa9d6"))
                    .withQuery("Resources | where name contains 'test' | project id, name, type, location")
                    .withOptions(new QueryRequestOptions().withTop(3).withSkip(0)),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/
     * ResourcesTenantBasicQuery.json
     */
    /**
     * Sample code: Basic tenant query.
     * 
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void basicTenantQuery(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager.resourceProviders()
            .resourcesWithResponse(
                new QueryRequest().withQuery("Resources | project id, name, type, location, tags | limit 3"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/resourcegraph/resource-manager/Microsoft.ResourceGraph/stable/2021-03-01/examples/
     * ResourcesRandomPageQuery.json
     */
    /**
     * Sample code: Random page query.
     * 
     * @param manager Entry point to ResourceGraphManager.
     */
    public static void randomPageQuery(com.azure.resourcemanager.resourcegraph.ResourceGraphManager manager) {
        manager.resourceProviders()
            .resourcesWithResponse(
                new QueryRequest().withSubscriptions(Arrays.asList("cfbbd179-59d2-4052-aa06-9270a38aa9d6"))
                    .withQuery("Resources | where name contains 'test' | project id, name, type, location")
                    .withOptions(new QueryRequestOptions().withTop(2).withSkip(10)),
                com.azure.core.util.Context.NONE);
    }
}
```

