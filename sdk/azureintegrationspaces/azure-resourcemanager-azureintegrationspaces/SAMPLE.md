# Code snippets and samples


## ApplicationResources

- [CreateOrUpdate](#applicationresources_createorupdate)
- [Delete](#applicationresources_delete)
- [Get](#applicationresources_get)
- [ListByApplication](#applicationresources_listbyapplication)
- [Patch](#applicationresources_patch)

## Applications

- [CreateOrUpdate](#applications_createorupdate)
- [Delete](#applications_delete)
- [DeleteBusinessProcessDevelopmentArtifact](#applications_deletebusinessprocessdevelopmentartifact)
- [Get](#applications_get)
- [GetBusinessProcessDevelopmentArtifact](#applications_getbusinessprocessdevelopmentartifact)
- [ListBusinessProcessDevelopmentArtifacts](#applications_listbusinessprocessdevelopmentartifacts)
- [ListBySpace](#applications_listbyspace)
- [Patch](#applications_patch)
- [SaveBusinessProcessDevelopmentArtifact](#applications_savebusinessprocessdevelopmentartifact)
- [ValidateBusinessProcessDevelopmentArtifact](#applications_validatebusinessprocessdevelopmentartifact)

## BusinessProcessVersions

- [Get](#businessprocessversions_get)
- [ListByBusinessProcess](#businessprocessversions_listbybusinessprocess)

## BusinessProcesses

- [CreateOrUpdate](#businessprocesses_createorupdate)
- [Delete](#businessprocesses_delete)
- [Get](#businessprocesses_get)
- [ListByApplication](#businessprocesses_listbyapplication)
- [Patch](#businessprocesses_patch)

## InfrastructureResources

- [CreateOrUpdate](#infrastructureresources_createorupdate)
- [Delete](#infrastructureresources_delete)
- [Get](#infrastructureresources_get)
- [ListBySpace](#infrastructureresources_listbyspace)
- [Patch](#infrastructureresources_patch)

## Operations

- [List](#operations_list)

## Spaces

- [CreateOrUpdate](#spaces_createorupdate)
- [Delete](#spaces_delete)
- [GetByResourceGroup](#spaces_getbyresourcegroup)
- [List](#spaces_list)
- [ListByResourceGroup](#spaces_listbyresourcegroup)
- [Patch](#spaces_patch)
### ApplicationResources_CreateOrUpdate

```java
/** Samples for ApplicationResources CreateOrUpdate. */
public final class ApplicationResourcesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/ApplicationResources_CreateOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdateApplicationResource.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void createOrUpdateApplicationResource(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager
            .applicationResources()
            .define("Resource1")
            .withExistingApplication("testrg", "Space1", "Application1")
            .withResourceType("Microsoft.Web/sites")
            .withResourceId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testrg/providers/Microsoft.Web/sites/LogicApp1")
            .withResourceKind("LogicApp")
            .create();
    }
}
```

### ApplicationResources_Delete

```java
/** Samples for ApplicationResources Delete. */
public final class ApplicationResourcesDeleteSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/ApplicationResources_Delete.json
     */
    /**
     * Sample code: DeleteApplicationResource.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void deleteApplicationResource(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager
            .applicationResources()
            .deleteWithResponse("testrg", "Space1", "Application1", "Resource1", com.azure.core.util.Context.NONE);
    }
}
```

### ApplicationResources_Get

```java
/** Samples for ApplicationResources Get. */
public final class ApplicationResourcesGetSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/ApplicationResources_Get.json
     */
    /**
     * Sample code: GetApplicationResource.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void getApplicationResource(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager
            .applicationResources()
            .getWithResponse("testrg", "Space1", "Application1", "Resource1", com.azure.core.util.Context.NONE);
    }
}
```

### ApplicationResources_ListByApplication

```java
/** Samples for ApplicationResources ListByApplication. */
public final class ApplicationResourcesListByApplicationSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/ApplicationResources_ListByApplication.json
     */
    /**
     * Sample code: ListApplicationResourceByApplication.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void listApplicationResourceByApplication(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager
            .applicationResources()
            .listByApplication(
                "testrg",
                "Space1",
                "Application1",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### ApplicationResources_Patch

```java
import com.azure.resourcemanager.azureintegrationspaces.models.ApplicationResource;

/** Samples for ApplicationResources Patch. */
public final class ApplicationResourcesPatchSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/ApplicationResources_Patch.json
     */
    /**
     * Sample code: PatchApplicationResource.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void patchApplicationResource(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        ApplicationResource resource =
            manager
                .applicationResources()
                .getWithResponse("testrg", "Space1", "Application1", "Resource1", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withResourceType("Microsoft.Web/sites")
            .withResourceId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testrg/providers/Microsoft.Web/sites/LogicApp1")
            .withResourceKind("LogicApp")
            .apply();
    }
}
```

### Applications_CreateOrUpdate

```java
import com.azure.resourcemanager.azureintegrationspaces.models.TrackingDataStore;
import java.util.HashMap;
import java.util.Map;

/** Samples for Applications CreateOrUpdate. */
public final class ApplicationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/Applications_CreateOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdateApplication.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void createOrUpdateApplication(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager
            .applications()
            .define("Application1")
            .withRegion("CentralUS")
            .withExistingSpace("testrg", "Space1")
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withDescription("This is the user provided description of the application.")
            .withTrackingDataStores(
                mapOf(
                    "dataStoreName1",
                    new TrackingDataStore()
                        .withDatabaseName("testDatabase1")
                        .withDataStoreResourceId(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testrg/providers/Microsoft.Kusto/Clusters/cluster1")
                        .withDataStoreUri("https://someClusterName.someRegionName.kusto.windows.net")
                        .withDataStoreIngestionUri("https://ingest-someClusterName.someRegionName.kusto.windows.net"),
                    "dataStoreName2",
                    new TrackingDataStore()
                        .withDatabaseName("testDatabase1")
                        .withDataStoreResourceId(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testrg/providers/Microsoft.Kusto/Clusters/cluster1")
                        .withDataStoreUri("https://someClusterName.someRegionName.kusto.windows.net")
                        .withDataStoreIngestionUri("https://ingest-someClusterName.someRegionName.kusto.windows.net")))
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

### Applications_Delete

```java
/** Samples for Applications Delete. */
public final class ApplicationsDeleteSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/Applications_Delete.json
     */
    /**
     * Sample code: DeleteApplication.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void deleteApplication(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager.applications().deleteWithResponse("testrg", "Space1", "Application1", com.azure.core.util.Context.NONE);
    }
}
```

### Applications_DeleteBusinessProcessDevelopmentArtifact

```java
import com.azure.resourcemanager.azureintegrationspaces.models.GetOrDeleteBusinessProcessDevelopmentArtifactRequest;

/** Samples for Applications DeleteBusinessProcessDevelopmentArtifact. */
public final class ApplicationsDeleteBusinessProcessDevelopmentArtifactSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/Applications_DeleteBusinessProcessDevelopmentArtifact.json
     */
    /**
     * Sample code: DeleteBusinessProcessDevelopmentArtifact.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void deleteBusinessProcessDevelopmentArtifact(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager
            .applications()
            .deleteBusinessProcessDevelopmentArtifactWithResponse(
                "testrg",
                "Space1",
                "Application1",
                new GetOrDeleteBusinessProcessDevelopmentArtifactRequest().withName("BusinessProcess1"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Applications_Get

```java
/** Samples for Applications Get. */
public final class ApplicationsGetSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/Applications_Get.json
     */
    /**
     * Sample code: GetApplication.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void getApplication(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager.applications().getWithResponse("testrg", "Space1", "Application1", com.azure.core.util.Context.NONE);
    }
}
```

### Applications_GetBusinessProcessDevelopmentArtifact

```java
import com.azure.resourcemanager.azureintegrationspaces.models.GetOrDeleteBusinessProcessDevelopmentArtifactRequest;

/** Samples for Applications GetBusinessProcessDevelopmentArtifact. */
public final class ApplicationsGetBusinessProcessDevelopmentArtifactSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/Applications_GetBusinessProcessDevelopmentArtifact.json
     */
    /**
     * Sample code: GetBusinessProcessDevelopmentArtifact.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void getBusinessProcessDevelopmentArtifact(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager
            .applications()
            .getBusinessProcessDevelopmentArtifactWithResponse(
                "testrg",
                "Space1",
                "Application1",
                new GetOrDeleteBusinessProcessDevelopmentArtifactRequest().withName("BusinessProcess1"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Applications_ListBusinessProcessDevelopmentArtifacts

```java
/** Samples for Applications ListBusinessProcessDevelopmentArtifacts. */
public final class ApplicationsListBusinessProcessDevelopmentArtifactsSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/Applications_ListBusinessProcessDevelopmentArtifacts.json
     */
    /**
     * Sample code: ListBusinessProcessDevelopmentArtifacts.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void listBusinessProcessDevelopmentArtifacts(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager
            .applications()
            .listBusinessProcessDevelopmentArtifactsWithResponse(
                "testrg", "Space1", "Application1", com.azure.core.util.Context.NONE);
    }
}
```

### Applications_ListBySpace

```java
/** Samples for Applications ListBySpace. */
public final class ApplicationsListBySpaceSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/Applications_ListBySpace.json
     */
    /**
     * Sample code: ListApplicationsBySpace.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void listApplicationsBySpace(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager
            .applications()
            .listBySpace(
                "testrg", "Space1", null, null, null, null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Applications_Patch

```java
import com.azure.resourcemanager.azureintegrationspaces.models.Application;
import java.util.HashMap;
import java.util.Map;

/** Samples for Applications Patch. */
public final class ApplicationsPatchSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/Applications_Patch.json
     */
    /**
     * Sample code: PatchApplication.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void patchApplication(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        Application resource =
            manager
                .applications()
                .getWithResponse("testrg", "Space1", "Application1", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withDescription("This is the user provided PATCHED description of the application.")
            .apply();
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

### Applications_SaveBusinessProcessDevelopmentArtifact

```java
import com.azure.resourcemanager.azureintegrationspaces.models.BusinessProcessDevelopmentArtifactProperties;
import com.azure.resourcemanager.azureintegrationspaces.models.BusinessProcessIdentifier;
import com.azure.resourcemanager.azureintegrationspaces.models.BusinessProcessMappingItem;
import com.azure.resourcemanager.azureintegrationspaces.models.BusinessProcessReference;
import com.azure.resourcemanager.azureintegrationspaces.models.BusinessProcessStage;
import com.azure.resourcemanager.azureintegrationspaces.models.FlowTrackingDefinition;
import com.azure.resourcemanager.azureintegrationspaces.models.SaveOrValidateBusinessProcessDevelopmentArtifactRequest;
import com.azure.resourcemanager.azureintegrationspaces.models.TrackingCorrelationContext;
import com.azure.resourcemanager.azureintegrationspaces.models.TrackingEventDefinition;
import com.azure.resourcemanager.azureintegrationspaces.models.TrackingProfileDefinition;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Applications SaveBusinessProcessDevelopmentArtifact. */
public final class ApplicationsSaveBusinessProcessDevelopmentArtifactSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/Applications_SaveBusinessProcessDevelopmentArtifact.json
     */
    /**
     * Sample code: SaveBusinessProcessDevelopmentArtifact.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void saveBusinessProcessDevelopmentArtifact(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager
            .applications()
            .saveBusinessProcessDevelopmentArtifactWithResponse(
                "testrg",
                "Space1",
                "Application1",
                new SaveOrValidateBusinessProcessDevelopmentArtifactRequest()
                    .withName("BusinessProcess1")
                    .withProperties(
                        new BusinessProcessDevelopmentArtifactProperties()
                            .withDescription("First Business Process")
                            .withIdentifier(
                                new BusinessProcessIdentifier()
                                    .withPropertyName("businessIdentifier-1")
                                    .withPropertyType("String"))
                            .withBusinessProcessStages(
                                mapOf(
                                    "Completed",
                                    new BusinessProcessStage()
                                        .withDescription("Completed")
                                        .withStagesBefore(Arrays.asList("Shipped")),
                                    "Denied",
                                    new BusinessProcessStage()
                                        .withDescription("Denied")
                                        .withStagesBefore(Arrays.asList("Processing")),
                                    "Processing",
                                    new BusinessProcessStage()
                                        .withDescription("Processing")
                                        .withProperties(
                                            mapOf(
                                                "ApprovalState",
                                                "String",
                                                "ApproverName",
                                                "String",
                                                "POAmount",
                                                "Integer"))
                                        .withStagesBefore(Arrays.asList("Received")),
                                    "Received",
                                    new BusinessProcessStage()
                                        .withDescription("received")
                                        .withProperties(
                                            mapOf(
                                                "City",
                                                "String",
                                                "Product",
                                                "String",
                                                "Quantity",
                                                "Integer",
                                                "State",
                                                "String")),
                                    "Shipped",
                                    new BusinessProcessStage()
                                        .withDescription("Shipped")
                                        .withProperties(mapOf("ShipPriority", "Integer", "TrackingID", "Integer"))
                                        .withStagesBefore(Arrays.asList("Denied"))))
                            .withBusinessProcessMapping(
                                mapOf(
                                    "Completed",
                                    new BusinessProcessMappingItem()
                                        .withLogicAppResourceId(
                                            "subscriptions/sub1/resourcegroups/group1/providers/Microsoft.Web/sites/logicApp1")
                                        .withWorkflowName("Fulfillment")
                                        .withOperationName("CompletedPO")
                                        .withOperationType("Action"),
                                    "Denied",
                                    new BusinessProcessMappingItem()
                                        .withLogicAppResourceId(
                                            "subscriptions/sub1/resourcegroups/group1/providers/Microsoft.Web/sites/logicApp1")
                                        .withWorkflowName("Fulfillment")
                                        .withOperationName("DeniedPO")
                                        .withOperationType("Action"),
                                    "Processing",
                                    new BusinessProcessMappingItem()
                                        .withLogicAppResourceId(
                                            "subscriptions/sub1/resourcegroups/group1/providers/Microsoft.Web/sites/logicApp1")
                                        .withWorkflowName("PurchaseOrder")
                                        .withOperationName("ApprovedPO")
                                        .withOperationType("Action"),
                                    "Received",
                                    new BusinessProcessMappingItem()
                                        .withLogicAppResourceId(
                                            "subscriptions/sub1/resourcegroups/group1/providers/Microsoft.Web/sites/logicApp1")
                                        .withWorkflowName("PurchaseOrder")
                                        .withOperationName("manual")
                                        .withOperationType("Trigger"),
                                    "Shipped",
                                    new BusinessProcessMappingItem()
                                        .withLogicAppResourceId(
                                            "subscriptions/sub1/resourcegroups/group1/providers/Microsoft.Web/sites/logicApp1")
                                        .withWorkflowName("Fulfillment")
                                        .withOperationName("ShippedPO")
                                        .withOperationType("Action")))
                            .withTrackingProfiles(
                                mapOf(
                                    "subscriptions/sub1/resourcegroups/group1/providers/Microsoft.Web/sites/logicApp1",
                                    new TrackingProfileDefinition()
                                        .withSchema(
                                            "https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2023-01-01/trackingdefinitionschema.json#")
                                        .withBusinessProcess(
                                            new BusinessProcessReference()
                                                .withName("businessProcess1")
                                                .withVersion("d52c9c91-6e10-4a90-9c1f-08ee5d01c656"))
                                        .withTrackingDefinitions(
                                            mapOf(
                                                "Fulfillment",
                                                new FlowTrackingDefinition()
                                                    .withCorrelationContext(
                                                        new TrackingCorrelationContext()
                                                            .withOperationType("Trigger")
                                                            .withOperationName("manual")
                                                            .withPropertyName("OrderNumber")
                                                            .withValue("@trigger().outputs.body.OrderNumber"))
                                                    .withEvents(
                                                        mapOf(
                                                            "Completed",
                                                            new TrackingEventDefinition()
                                                                .withOperationType("Action")
                                                                .withOperationName("CompletedPO")
                                                                .withProperties(mapOf()),
                                                            "Denied",
                                                            new TrackingEventDefinition()
                                                                .withOperationType("Action")
                                                                .withOperationName("DeniedPO")
                                                                .withProperties(mapOf()),
                                                            "Shipped",
                                                            new TrackingEventDefinition()
                                                                .withOperationType("Action")
                                                                .withOperationName("ShippedPO")
                                                                .withProperties(
                                                                    mapOf(
                                                                        "ShipPriority",
                                                                        "@action().inputs.shipPriority",
                                                                        "TrackingID",
                                                                        "@action().inputs.trackingID")))),
                                                "PurchaseOrder",
                                                new FlowTrackingDefinition()
                                                    .withCorrelationContext(
                                                        new TrackingCorrelationContext()
                                                            .withOperationType("Trigger")
                                                            .withOperationName("manual")
                                                            .withPropertyName("OrderNumber")
                                                            .withValue("@trigger().outputs.body.OrderNumber"))
                                                    .withEvents(
                                                        mapOf(
                                                            "Processing",
                                                            new TrackingEventDefinition()
                                                                .withOperationType("Action")
                                                                .withOperationName("ApprovedPO")
                                                                .withProperties(
                                                                    mapOf(
                                                                        "ApprovalStatus",
                                                                        "@action().inputs.ApprovalStatus",
                                                                        "ApproverName",
                                                                        "@action().inputs.ApproverName",
                                                                        "POAmount",
                                                                        "@action().inputs.POamount")),
                                                            "Received",
                                                            new TrackingEventDefinition()
                                                                .withOperationType("Trigger")
                                                                .withOperationName("manual")
                                                                .withProperties(
                                                                    mapOf(
                                                                        "City",
                                                                        "@trigger().outputs.body.Address.City",
                                                                        "Product",
                                                                        "@trigger().outputs.body.Product",
                                                                        "Quantity",
                                                                        "@trigger().outputs.body.Quantity",
                                                                        "State",
                                                                        "@trigger().outputs.body.Address.State"))))))))),
                com.azure.core.util.Context.NONE);
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

### Applications_ValidateBusinessProcessDevelopmentArtifact

```java
import com.azure.resourcemanager.azureintegrationspaces.models.BusinessProcessDevelopmentArtifactProperties;
import com.azure.resourcemanager.azureintegrationspaces.models.BusinessProcessIdentifier;
import com.azure.resourcemanager.azureintegrationspaces.models.BusinessProcessMappingItem;
import com.azure.resourcemanager.azureintegrationspaces.models.BusinessProcessReference;
import com.azure.resourcemanager.azureintegrationspaces.models.BusinessProcessStage;
import com.azure.resourcemanager.azureintegrationspaces.models.FlowTrackingDefinition;
import com.azure.resourcemanager.azureintegrationspaces.models.SaveOrValidateBusinessProcessDevelopmentArtifactRequest;
import com.azure.resourcemanager.azureintegrationspaces.models.TrackingCorrelationContext;
import com.azure.resourcemanager.azureintegrationspaces.models.TrackingEventDefinition;
import com.azure.resourcemanager.azureintegrationspaces.models.TrackingProfileDefinition;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Applications ValidateBusinessProcessDevelopmentArtifact. */
public final class ApplicationsValidateBusinessProcessDevelopmentArtifactSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/Applications_ValidateBusinessProcessDevelopmentArtifact.json
     */
    /**
     * Sample code: ValidateBusinessProcessDevelopmentArtifact.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void validateBusinessProcessDevelopmentArtifact(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager
            .applications()
            .validateBusinessProcessDevelopmentArtifactWithResponse(
                "testrg",
                "Space1",
                "Application1",
                new SaveOrValidateBusinessProcessDevelopmentArtifactRequest()
                    .withName("BusinessProcess1")
                    .withProperties(
                        new BusinessProcessDevelopmentArtifactProperties()
                            .withDescription("First Business Process")
                            .withIdentifier(
                                new BusinessProcessIdentifier()
                                    .withPropertyName("businessIdentifier-1")
                                    .withPropertyType("String"))
                            .withBusinessProcessStages(
                                mapOf(
                                    "Completed",
                                    new BusinessProcessStage()
                                        .withDescription("Completed")
                                        .withStagesBefore(Arrays.asList("Shipped")),
                                    "Denied",
                                    new BusinessProcessStage()
                                        .withDescription("Denied")
                                        .withStagesBefore(Arrays.asList("Processing")),
                                    "Processing",
                                    new BusinessProcessStage()
                                        .withDescription("Processing")
                                        .withProperties(
                                            mapOf(
                                                "ApprovalState",
                                                "String",
                                                "ApproverName",
                                                "String",
                                                "POAmount",
                                                "Integer"))
                                        .withStagesBefore(Arrays.asList("Received")),
                                    "Received@",
                                    new BusinessProcessStage()
                                        .withDescription("received")
                                        .withProperties(
                                            mapOf(
                                                "City",
                                                "String",
                                                "Product",
                                                "String",
                                                "Quantity",
                                                "Integer",
                                                "State",
                                                "String")),
                                    "Shipped",
                                    new BusinessProcessStage()
                                        .withDescription("Shipped")
                                        .withProperties(mapOf("ShipPriority", "Integer", "TrackingID", "Integer"))
                                        .withStagesBefore(Arrays.asList("Denied"))))
                            .withBusinessProcessMapping(
                                mapOf(
                                    "Completed",
                                    new BusinessProcessMappingItem()
                                        .withLogicAppResourceId(
                                            "subscriptions/sub1/resourcegroups/group1/providers/Microsoft.Web/sites/logicApp1")
                                        .withWorkflowName("Fulfillment")
                                        .withOperationName("CompletedPO")
                                        .withOperationType("Action"),
                                    "Denied",
                                    new BusinessProcessMappingItem()
                                        .withLogicAppResourceId(
                                            "subscriptions/sub1/resourcegroups/group1/providers/Microsoft.Web/sites/logicApp1")
                                        .withWorkflowName("Fulfillment")
                                        .withOperationName("DeniedPO")
                                        .withOperationType("Action"),
                                    "Processing",
                                    new BusinessProcessMappingItem()
                                        .withLogicAppResourceId(
                                            "subscriptions/sub1/resourcegroups/group1/providers/Microsoft.Web/sites/logicApp1")
                                        .withWorkflowName("PurchaseOrder")
                                        .withOperationName("ApprovedPO")
                                        .withOperationType("Action"),
                                    "Received",
                                    new BusinessProcessMappingItem()
                                        .withLogicAppResourceId(
                                            "subscriptions/sub1/resourcegroups/group1/providers/Microsoft.Web/sites/logicApp1")
                                        .withWorkflowName("PurchaseOrder")
                                        .withOperationName("manual")
                                        .withOperationType("Trigger"),
                                    "Shipped",
                                    new BusinessProcessMappingItem()
                                        .withLogicAppResourceId(
                                            "subscriptions/sub1/resourcegroups/group1/providers/Microsoft.Web/sites/logicApp1")
                                        .withWorkflowName("Fulfillment")
                                        .withOperationName("ShippedPO")
                                        .withOperationType("Action")))
                            .withTrackingProfiles(
                                mapOf(
                                    "subscriptions/sub1/resourcegroups/group1/providers/Microsoft.Web/sites/logicApp1",
                                    new TrackingProfileDefinition()
                                        .withSchema(
                                            "https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2023-01-01/trackingdefinitionschema.json#")
                                        .withBusinessProcess(
                                            new BusinessProcessReference()
                                                .withName("businessProcess1")
                                                .withVersion("d52c9c91-6e10-4a90-9c1f-08ee5d01c656"))
                                        .withTrackingDefinitions(
                                            mapOf(
                                                "Fulfillment",
                                                new FlowTrackingDefinition()
                                                    .withCorrelationContext(
                                                        new TrackingCorrelationContext()
                                                            .withOperationType("Trigger")
                                                            .withOperationName("manual")
                                                            .withPropertyName("OrderNumber")
                                                            .withValue("@trigger().outputs.body.OrderNumber"))
                                                    .withEvents(
                                                        mapOf(
                                                            "Completed",
                                                            new TrackingEventDefinition()
                                                                .withOperationType("Action")
                                                                .withOperationName("CompletedPO")
                                                                .withProperties(mapOf()),
                                                            "Denied",
                                                            new TrackingEventDefinition()
                                                                .withOperationType("Action")
                                                                .withOperationName("DeniedPO")
                                                                .withProperties(mapOf()),
                                                            "Shipped",
                                                            new TrackingEventDefinition()
                                                                .withOperationType("Action")
                                                                .withOperationName("ShippedPO")
                                                                .withProperties(
                                                                    mapOf(
                                                                        "ShipPriority",
                                                                        "@action().inputs.shipPriority",
                                                                        "TrackingID",
                                                                        "@action().inputs.trackingID")))),
                                                "PurchaseOrder",
                                                new FlowTrackingDefinition()
                                                    .withCorrelationContext(
                                                        new TrackingCorrelationContext()
                                                            .withOperationType("Trigger")
                                                            .withOperationName("manual")
                                                            .withPropertyName("OrderNumber")
                                                            .withValue("@trigger().outputs.body.OrderNumber"))
                                                    .withEvents(
                                                        mapOf(
                                                            "Processing",
                                                            new TrackingEventDefinition()
                                                                .withOperationType("Action")
                                                                .withOperationName("ApprovedPO")
                                                                .withProperties(
                                                                    mapOf(
                                                                        "ApprovalStatus",
                                                                        "@action().inputs.ApprovalStatus",
                                                                        "ApproverName",
                                                                        "@action().inputs.ApproverName",
                                                                        "POAmount",
                                                                        "@action().inputs.POamount")),
                                                            "Received",
                                                            new TrackingEventDefinition()
                                                                .withOperationType("Trigger")
                                                                .withOperationName("manual")
                                                                .withProperties(
                                                                    mapOf(
                                                                        "City",
                                                                        "@trigger().outputs.body.Address.City",
                                                                        "Product",
                                                                        "@trigger().outputs.body.Product",
                                                                        "Quantity",
                                                                        "@trigger().outputs.body.Quantity",
                                                                        "State",
                                                                        "@trigger().outputs.body.Address.State"))))))))),
                com.azure.core.util.Context.NONE);
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

### BusinessProcessVersions_Get

```java
/** Samples for BusinessProcessVersions Get. */
public final class BusinessProcessVersionsGetSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/BusinessProcessVersions_Get.json
     */
    /**
     * Sample code: GetBusinessProcessVersion.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void getBusinessProcessVersion(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager
            .businessProcessVersions()
            .getWithResponse(
                "testrg",
                "Space1",
                "Application1",
                "BusinessProcess1",
                "08585074782265427079",
                com.azure.core.util.Context.NONE);
    }
}
```

### BusinessProcessVersions_ListByBusinessProcess

```java
/** Samples for BusinessProcessVersions ListByBusinessProcess. */
public final class BusinessProcessVersionsListByBusinessProcessSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/BusinessProcessVersions_ListByBusinessProcess.json
     */
    /**
     * Sample code: ListBusinessProcessVersionsByBusinessProcess.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void listBusinessProcessVersionsByBusinessProcess(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager
            .businessProcessVersions()
            .listByBusinessProcess(
                "testrg",
                "Space1",
                "Application1",
                "BusinessProcess1",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### BusinessProcesses_CreateOrUpdate

```java
import com.azure.resourcemanager.azureintegrationspaces.models.BusinessProcessIdentifier;
import com.azure.resourcemanager.azureintegrationspaces.models.BusinessProcessMappingItem;
import com.azure.resourcemanager.azureintegrationspaces.models.BusinessProcessStage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for BusinessProcesses CreateOrUpdate. */
public final class BusinessProcessesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/BusinessProcesses_CreateOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdateBusinessProcess.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void createOrUpdateBusinessProcess(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager
            .businessProcesses()
            .define("BusinessProcess1")
            .withExistingApplication("testrg", "Space1", "Application1")
            .withDescription("First Business Process")
            .withTableName("table1")
            .withTrackingDataStoreReferenceName("trackingDataStoreReferenceName1")
            .withIdentifier(
                new BusinessProcessIdentifier().withPropertyName("businessIdentifier-1").withPropertyType("String"))
            .withBusinessProcessStages(
                mapOf(
                    "Completed",
                    new BusinessProcessStage().withDescription("Completed").withStagesBefore(Arrays.asList("Shipped")),
                    "Denied",
                    new BusinessProcessStage().withDescription("Denied").withStagesBefore(Arrays.asList("Processing")),
                    "Processing",
                    new BusinessProcessStage()
                        .withDescription("Processing")
                        .withProperties(
                            mapOf("ApprovalState", "String", "ApproverName", "String", "POAmount", "Integer"))
                        .withStagesBefore(Arrays.asList("Received")),
                    "Received",
                    new BusinessProcessStage()
                        .withDescription("received")
                        .withProperties(
                            mapOf("City", "String", "Product", "String", "Quantity", "Integer", "State", "String")),
                    "Shipped",
                    new BusinessProcessStage()
                        .withDescription("Shipped")
                        .withProperties(mapOf("ShipPriority", "Integer", "TrackingID", "Integer"))
                        .withStagesBefore(Arrays.asList("Denied"))))
            .withBusinessProcessMapping(
                mapOf(
                    "Completed",
                    new BusinessProcessMappingItem()
                        .withLogicAppResourceId(
                            "subscriptions/sub1/resourcegroups/group1/providers/Microsoft.Web/sites/logicApp1")
                        .withWorkflowName("Fulfillment")
                        .withOperationName("CompletedPO")
                        .withOperationType("Action"),
                    "Denied",
                    new BusinessProcessMappingItem()
                        .withLogicAppResourceId(
                            "subscriptions/sub1/resourcegroups/group1/providers/Microsoft.Web/sites/logicApp1")
                        .withWorkflowName("Fulfillment")
                        .withOperationName("DeniedPO")
                        .withOperationType("Action"),
                    "Processing",
                    new BusinessProcessMappingItem()
                        .withLogicAppResourceId(
                            "subscriptions/sub1/resourcegroups/group1/providers/Microsoft.Web/sites/logicApp1")
                        .withWorkflowName("PurchaseOrder")
                        .withOperationName("ApprovedPO")
                        .withOperationType("Action"),
                    "Received",
                    new BusinessProcessMappingItem()
                        .withLogicAppResourceId(
                            "subscriptions/sub1/resourcegroups/group1/providers/Microsoft.Web/sites/logicApp1")
                        .withWorkflowName("PurchaseOrder")
                        .withOperationName("manual")
                        .withOperationType("Trigger"),
                    "Shipped",
                    new BusinessProcessMappingItem()
                        .withLogicAppResourceId(
                            "subscriptions/sub1/resourcegroups/group1/providers/Microsoft.Web/sites/logicApp1")
                        .withWorkflowName("Fulfillment")
                        .withOperationName("ShippedPO")
                        .withOperationType("Action")))
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

### BusinessProcesses_Delete

```java
/** Samples for BusinessProcesses Delete. */
public final class BusinessProcessesDeleteSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/BusinessProcesses_Delete.json
     */
    /**
     * Sample code: DeleteBusinessProcess.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void deleteBusinessProcess(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager
            .businessProcesses()
            .deleteWithResponse(
                "testrg", "Space1", "Application1", "BusinessProcess1", com.azure.core.util.Context.NONE);
    }
}
```

### BusinessProcesses_Get

```java
/** Samples for BusinessProcesses Get. */
public final class BusinessProcessesGetSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/BusinessProcesses_Get.json
     */
    /**
     * Sample code: GetBusinessProcess.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void getBusinessProcess(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager
            .businessProcesses()
            .getWithResponse("testrg", "Space1", "Application1", "BusinessProcess1", com.azure.core.util.Context.NONE);
    }
}
```

### BusinessProcesses_ListByApplication

```java
/** Samples for BusinessProcesses ListByApplication. */
public final class BusinessProcessesListByApplicationSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/BusinessProcesses_ListByApplication.json
     */
    /**
     * Sample code: ListBusinessProcessesByApplication.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void listBusinessProcessesByApplication(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager
            .businessProcesses()
            .listByApplication(
                "testrg",
                "Space1",
                "Application1",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### BusinessProcesses_Patch

```java
import com.azure.resourcemanager.azureintegrationspaces.models.BusinessProcess;

/** Samples for BusinessProcesses Patch. */
public final class BusinessProcessesPatchSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/BusinessProcesses_Patch.json
     */
    /**
     * Sample code: PatchBusinessProcess.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void patchBusinessProcess(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        BusinessProcess resource =
            manager
                .businessProcesses()
                .getWithResponse(
                    "testrg", "Space1", "Application1", "BusinessProcess1", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withDescription("First updated business process.").apply();
    }
}
```

### InfrastructureResources_CreateOrUpdate

```java
/** Samples for InfrastructureResources CreateOrUpdate. */
public final class InfrastructureResourcesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/InfrastructureResources_CreateOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdateInfrastructureResource.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void createOrUpdateInfrastructureResource(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager
            .infrastructureResources()
            .define("InfrastructureResource1")
            .withExistingSpace("testrg", "Space1")
            .withResourceType("Microsoft.ApiManagement/service")
            .withResourceId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testrg/providers/Microsoft.ApiManagement/service/APIM1")
            .create();
    }
}
```

### InfrastructureResources_Delete

```java
/** Samples for InfrastructureResources Delete. */
public final class InfrastructureResourcesDeleteSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/InfrastructureResources_Delete.json
     */
    /**
     * Sample code: DeleteInfrastructureResource.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void deleteInfrastructureResource(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager
            .infrastructureResources()
            .deleteWithResponse("testrg", "Space1", "InfrastructureResource1", com.azure.core.util.Context.NONE);
    }
}
```

### InfrastructureResources_Get

```java
/** Samples for InfrastructureResources Get. */
public final class InfrastructureResourcesGetSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/InfrastructureResources_Get.json
     */
    /**
     * Sample code: GetInfrastructureResource.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void getInfrastructureResource(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager
            .infrastructureResources()
            .getWithResponse("testrg", "Space1", "InfrastructureResource1", com.azure.core.util.Context.NONE);
    }
}
```

### InfrastructureResources_ListBySpace

```java
/** Samples for InfrastructureResources ListBySpace. */
public final class InfrastructureResourcesListBySpaceSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/InfrastructureResources_ListBySpace.json
     */
    /**
     * Sample code: ListInfrastructureResourcesBySpace.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void listInfrastructureResourcesBySpace(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager
            .infrastructureResources()
            .listBySpace(
                "testrg", "Space1", null, null, null, null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### InfrastructureResources_Patch

```java
import com.azure.resourcemanager.azureintegrationspaces.models.InfrastructureResource;

/** Samples for InfrastructureResources Patch. */
public final class InfrastructureResourcesPatchSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/InfrastructureResources_Patch.json
     */
    /**
     * Sample code: PatchInfrastructureResource.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void patchInfrastructureResource(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        InfrastructureResource resource =
            manager
                .infrastructureResources()
                .getWithResponse("testrg", "Space1", "InfrastructureResource1", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withResourceType("Microsoft.ApiManagement/service")
            .withResourceId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testrg/providers/Microsoft.ApiManagement/service/APIM1")
            .apply();
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void operationsList(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Spaces_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for Spaces CreateOrUpdate. */
public final class SpacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/Spaces_CreateOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdateSpace.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void createOrUpdateSpace(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager
            .spaces()
            .define("Space1")
            .withRegion("CentralUS")
            .withExistingResourceGroup("testrg")
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withDescription("This is the user provided description of the space resource.")
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

### Spaces_Delete

```java
/** Samples for Spaces Delete. */
public final class SpacesDeleteSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/Spaces_Delete.json
     */
    /**
     * Sample code: DeleteSpace.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void deleteSpace(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager.spaces().deleteByResourceGroupWithResponse("testrg", "Space1", com.azure.core.util.Context.NONE);
    }
}
```

### Spaces_GetByResourceGroup

```java
/** Samples for Spaces GetByResourceGroup. */
public final class SpacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/Spaces_Get.json
     */
    /**
     * Sample code: GetSpace.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void getSpace(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager.spaces().getByResourceGroupWithResponse("testrg", "Space1", com.azure.core.util.Context.NONE);
    }
}
```

### Spaces_List

```java
/** Samples for Spaces List. */
public final class SpacesListSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/Spaces_ListBySubscription.json
     */
    /**
     * Sample code: ListSpacesBySubscription.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void listSpacesBySubscription(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager.spaces().list(com.azure.core.util.Context.NONE);
    }
}
```

### Spaces_ListByResourceGroup

```java
/** Samples for Spaces ListByResourceGroup. */
public final class SpacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/Spaces_ListByResourceGroup.json
     */
    /**
     * Sample code: ListSpacesByResourceGroup.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void listSpacesByResourceGroup(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        manager
            .spaces()
            .listByResourceGroup("testrg", null, null, null, null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Spaces_Patch

```java
import com.azure.resourcemanager.azureintegrationspaces.models.Space;
import java.util.HashMap;
import java.util.Map;

/** Samples for Spaces Patch. */
public final class SpacesPatchSamples {
    /*
     * x-ms-original-file: specification/azureintegrationspaces/resource-manager/Microsoft.IntegrationSpaces/preview/2023-11-14-preview/examples/Spaces_Patch.json
     */
    /**
     * Sample code: PatchSpace.
     *
     * @param manager Entry point to AzureintegrationspacesManager.
     */
    public static void patchSpace(
        com.azure.resourcemanager.azureintegrationspaces.AzureintegrationspacesManager manager) {
        Space resource =
            manager
                .spaces()
                .getByResourceGroupWithResponse("testrg", "Space1", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withDescription("This is the user provided description of the space resource.")
            .apply();
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

