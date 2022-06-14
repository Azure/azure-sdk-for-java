# Code snippets and samples


## Changes

- [List](#changes_list)
- [ListByResourceGroup](#changes_listbyresourcegroup)

## Operations

- [List](#operations_list)

## ResourceChanges

- [List](#resourcechanges_list)
### Changes_List

```java
import com.azure.core.util.Context;
import java.time.OffsetDateTime;

/** Samples for Changes List. */
public final class ChangesListSamples {
    /*
     * x-ms-original-file: specification/changeanalysis/resource-manager/Microsoft.ChangeAnalysis/stable/2021-04-01/examples/ChangesListChangesBySubscription.json
     */
    /**
     * Sample code: Changes_ListChangesBySubscription.
     *
     * @param manager Entry point to AzureChangeAnalysisManager.
     */
    public static void changesListChangesBySubscription(
        com.azure.resourcemanager.changeanalysis.AzureChangeAnalysisManager manager) {
        manager
            .changes()
            .list(
                OffsetDateTime.parse("2021-04-25T12:09:03.141Z"),
                OffsetDateTime.parse("2021-04-26T12:09:03.141Z"),
                null,
                Context.NONE);
    }
}
```

### Changes_ListByResourceGroup

```java
import com.azure.core.util.Context;
import java.time.OffsetDateTime;

/** Samples for Changes ListByResourceGroup. */
public final class ChangesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/changeanalysis/resource-manager/Microsoft.ChangeAnalysis/stable/2021-04-01/examples/ChangesListChangesByResourceGroup.json
     */
    /**
     * Sample code: Changes_ListChangesByResourceGroup.
     *
     * @param manager Entry point to AzureChangeAnalysisManager.
     */
    public static void changesListChangesByResourceGroup(
        com.azure.resourcemanager.changeanalysis.AzureChangeAnalysisManager manager) {
        manager
            .changes()
            .listByResourceGroup(
                "MyResourceGroup",
                OffsetDateTime.parse("2021-04-25T12:09:03.141Z"),
                OffsetDateTime.parse("2021-04-26T12:09:03.141Z"),
                null,
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
     * x-ms-original-file: specification/changeanalysis/resource-manager/Microsoft.ChangeAnalysis/stable/2021-04-01/examples/OperationsList.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to AzureChangeAnalysisManager.
     */
    public static void operationsList(com.azure.resourcemanager.changeanalysis.AzureChangeAnalysisManager manager) {
        manager.operations().list(null, Context.NONE);
    }
}
```

### ResourceChanges_List

```java
import com.azure.core.util.Context;
import java.time.OffsetDateTime;

/** Samples for ResourceChanges List. */
public final class ResourceChangesListSamples {
    /*
     * x-ms-original-file: specification/changeanalysis/resource-manager/Microsoft.ChangeAnalysis/stable/2021-04-01/examples/ResourceChangesList.json
     */
    /**
     * Sample code: ResourceChanges_List.
     *
     * @param manager Entry point to AzureChangeAnalysisManager.
     */
    public static void resourceChangesList(
        com.azure.resourcemanager.changeanalysis.AzureChangeAnalysisManager manager) {
        manager
            .resourceChanges()
            .list(
                "subscriptions/4d962866-1e3f-47f2-bd18-450c08f914c1/resourceGroups/MyResourceGroup/providers/Microsoft.Web/sites/mysite",
                OffsetDateTime.parse("2021-04-25T12:09:03.141Z"),
                OffsetDateTime.parse("2021-04-26T12:09:03.141Z"),
                null,
                Context.NONE);
    }
}
```

