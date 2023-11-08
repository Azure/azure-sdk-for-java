# Code snippets and samples


## AvailabilityStatuses

- [GetByResource](#availabilitystatuses_getbyresource)
- [List](#availabilitystatuses_list)
- [ListByResourceGroup](#availabilitystatuses_listbyresourcegroup)
- [ListBySubscriptionId](#availabilitystatuses_listbysubscriptionid)

## ChildAvailabilityStatuses

- [GetByResource](#childavailabilitystatuses_getbyresource)
- [List](#childavailabilitystatuses_list)

## ChildResources

- [List](#childresources_list)

## EmergingIssues

- [Get](#emergingissues_get)
- [List](#emergingissues_list)

## EventOperation

- [FetchDetailsBySubscriptionIdAndTrackingId](#eventoperation_fetchdetailsbysubscriptionidandtrackingid)
- [FetchDetailsByTenantIdAndTrackingId](#eventoperation_fetchdetailsbytenantidandtrackingid)
- [GetBySubscriptionIdAndTrackingId](#eventoperation_getbysubscriptionidandtrackingid)
- [GetByTenantIdAndTrackingId](#eventoperation_getbytenantidandtrackingid)

## EventsOperation

- [List](#eventsoperation_list)
- [ListBySingleResource](#eventsoperation_listbysingleresource)
- [ListByTenantId](#eventsoperation_listbytenantid)

## ImpactedResources

- [Get](#impactedresources_get)
- [GetByTenantId](#impactedresources_getbytenantid)
- [ListBySubscriptionIdAndEventId](#impactedresources_listbysubscriptionidandeventid)
- [ListByTenantIdAndEventId](#impactedresources_listbytenantidandeventid)

## Metadata

- [GetEntity](#metadata_getentity)
- [List](#metadata_list)

## Operations

- [List](#operations_list)

## SecurityAdvisoryImpactedResources

- [ListBySubscriptionIdAndEventId](#securityadvisoryimpactedresources_listbysubscriptionidandeventid)
- [ListByTenantIdAndEventId](#securityadvisoryimpactedresources_listbytenantidandeventid)
### AvailabilityStatuses_GetByResource

```java
/** Samples for AvailabilityStatuses GetByResource. */
public final class AvailabilityStatusesGetByResourceSamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/AvailabilityStatus_GetByResource.json
     */
    /**
     * Sample code: GetCurrentHealthByResource.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void getCurrentHealthByResource(
        com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager
            .availabilityStatuses()
            .getByResourceWithResponse("resourceUri", null, "recommendedactions", com.azure.core.util.Context.NONE);
    }
}
```

### AvailabilityStatuses_List

```java
/** Samples for AvailabilityStatuses List. */
public final class AvailabilityStatusesListSamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/AvailabilityStatuses_List.json
     */
    /**
     * Sample code: GetHealthHistoryByResource.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void getHealthHistoryByResource(
        com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager.availabilityStatuses().list("resourceUri", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### AvailabilityStatuses_ListByResourceGroup

```java
/** Samples for AvailabilityStatuses ListByResourceGroup. */
public final class AvailabilityStatusesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/AvailabilityStatuses_ListByResourceGroup.json
     */
    /**
     * Sample code: ListByResourceGroup.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void listByResourceGroup(com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager
            .availabilityStatuses()
            .listByResourceGroup("resourceGroupName", null, "recommendedactions", com.azure.core.util.Context.NONE);
    }
}
```

### AvailabilityStatuses_ListBySubscriptionId

```java
/** Samples for AvailabilityStatuses ListBySubscriptionId. */
public final class AvailabilityStatusesListBySubscriptionIdSamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/AvailabilityStatuses_ListBySubscriptionId.json
     */
    /**
     * Sample code: ListHealthBySubscriptionId.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void listHealthBySubscriptionId(
        com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager
            .availabilityStatuses()
            .listBySubscriptionId(null, "recommendedactions", com.azure.core.util.Context.NONE);
    }
}
```

### ChildAvailabilityStatuses_GetByResource

```java
/** Samples for ChildAvailabilityStatuses GetByResource. */
public final class ChildAvailabilityStatusesGetByResourceSamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/ChildAvailabilityStatus_GetByResource.json
     */
    /**
     * Sample code: GetChildCurrentHealthByResource.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void getChildCurrentHealthByResource(
        com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager
            .childAvailabilityStatuses()
            .getByResourceWithResponse(
                "subscriptions/227b734f-e14f-4de6-b7fc-3190c21e69f6/resourceGroups/JUHACKETRHCTEST/providers/Microsoft.Compute/virtualMachineScaleSets/rhctest/virtualMachines/4",
                null,
                "recommendedactions",
                com.azure.core.util.Context.NONE);
    }
}
```

### ChildAvailabilityStatuses_List

```java
/** Samples for ChildAvailabilityStatuses List. */
public final class ChildAvailabilityStatusesListSamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/ChildAvailabilityStatuses_List.json
     */
    /**
     * Sample code: GetChildHealthHistoryByResource.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void getChildHealthHistoryByResource(
        com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager
            .childAvailabilityStatuses()
            .list(
                "subscriptions/227b734f-e14f-4de6-b7fc-3190c21e69f6/resourceGroups/JUHACKETRHCTEST/providers/Microsoft.Compute/virtualMachineScaleSets/rhctest/virtualMachines/4",
                null,
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### ChildResources_List

```java
/** Samples for ChildResources List. */
public final class ChildResourcesListSamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/ChildResources_List.json
     */
    /**
     * Sample code: GetCurrentChildHealthByResource.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void getCurrentChildHealthByResource(
        com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager.childResources().list("resourceUri", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### EmergingIssues_Get

```java
import com.azure.resourcemanager.resourcehealth.models.IssueNameParameter;

/** Samples for EmergingIssues Get. */
public final class EmergingIssuesGetSamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/EmergingIssues_Get.json
     */
    /**
     * Sample code: GetEmergingIssues.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void getEmergingIssues(com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager.emergingIssues().getWithResponse(IssueNameParameter.DEFAULT, com.azure.core.util.Context.NONE);
    }
}
```

### EmergingIssues_List

```java
/** Samples for EmergingIssues List. */
public final class EmergingIssuesListSamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/EmergingIssues_List.json
     */
    /**
     * Sample code: GetEmergingIssues.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void getEmergingIssues(com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager.emergingIssues().list(com.azure.core.util.Context.NONE);
    }
}
```

### EventOperation_FetchDetailsBySubscriptionIdAndTrackingId

```java
/** Samples for EventOperation FetchDetailsBySubscriptionIdAndTrackingId. */
public final class EventOperationFetchDetailsBySubscriptionIdAndTrackingIdSamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/Event_fetchDetailsBySubscriptionIdAndTrackingId.json
     */
    /**
     * Sample code: EventDetailsBySubscriptionIdAndTrackingId.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void eventDetailsBySubscriptionIdAndTrackingId(
        com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager
            .eventOperations()
            .fetchDetailsBySubscriptionIdAndTrackingIdWithResponse("eventTrackingId", com.azure.core.util.Context.NONE);
    }
}
```

### EventOperation_FetchDetailsByTenantIdAndTrackingId

```java
/** Samples for EventOperation FetchDetailsByTenantIdAndTrackingId. */
public final class EventOperationFetchDetailsByTenantIdAndTrackingIdSamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/Event_fetchDetailsByTenantIdAndTrackingId.json
     */
    /**
     * Sample code: EventDetailsByTenantIdAndTrackingId.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void eventDetailsByTenantIdAndTrackingId(
        com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager
            .eventOperations()
            .fetchDetailsByTenantIdAndTrackingIdWithResponse("eventTrackingId", com.azure.core.util.Context.NONE);
    }
}
```

### EventOperation_GetBySubscriptionIdAndTrackingId

```java
/** Samples for EventOperation GetBySubscriptionIdAndTrackingId. */
public final class EventOperationGetBySubscriptionIdAndTrackingIdSamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/Event_GetBySubscriptionIdAndTrackingId.json
     */
    /**
     * Sample code: SecurityAdvisoriesEventBySubscriptionIdAndTrackingId.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void securityAdvisoriesEventBySubscriptionIdAndTrackingId(
        com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager
            .eventOperations()
            .getBySubscriptionIdAndTrackingIdWithResponse(
                "eventTrackingId", "properties/status eq 'Active'", "7/10/2022", com.azure.core.util.Context.NONE);
    }
}
```

### EventOperation_GetByTenantIdAndTrackingId

```java
/** Samples for EventOperation GetByTenantIdAndTrackingId. */
public final class EventOperationGetByTenantIdAndTrackingIdSamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/Event_GetByTenantIdAndTrackingId.json
     */
    /**
     * Sample code: EventByTenantIdAndTrackingId.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void eventByTenantIdAndTrackingId(
        com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager
            .eventOperations()
            .getByTenantIdAndTrackingIdWithResponse(
                "eventTrackingId", "properties/status eq 'Active'", "7/10/2022", com.azure.core.util.Context.NONE);
    }
}
```

### EventsOperation_List

```java
/** Samples for EventsOperation List. */
public final class EventsOperationListSamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/Events_ListBySubscriptionId.json
     */
    /**
     * Sample code: ListEventsBySubscriptionId.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void listEventsBySubscriptionId(
        com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager
            .eventsOperations()
            .list(
                "service eq 'Virtual Machines' or region eq 'West US'", "7/24/2020", com.azure.core.util.Context.NONE);
    }
}
```

### EventsOperation_ListBySingleResource

```java
/** Samples for EventsOperation ListBySingleResource. */
public final class EventsOperationListBySingleResourceSamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/Events_ListBySingleResource.json
     */
    /**
     * Sample code: ListEventsBySingleResource.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void listEventsBySingleResource(
        com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager
            .eventsOperations()
            .listBySingleResource(
                "subscriptions/4abcdefgh-ijkl-mnop-qrstuvwxyz/resourceGroups/rhctestenv/providers/Microsoft.Compute/virtualMachines/rhctestenvV1PI",
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### EventsOperation_ListByTenantId

```java
/** Samples for EventsOperation ListByTenantId. */
public final class EventsOperationListByTenantIdSamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/Events_ListByTenantId.json
     */
    /**
     * Sample code: ListEventsByTenantId.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void listEventsByTenantId(com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager
            .eventsOperations()
            .listByTenantId(
                "service eq 'Virtual Machines' or region eq 'West US'", "7/24/2020", com.azure.core.util.Context.NONE);
    }
}
```

### ImpactedResources_Get

```java
/** Samples for ImpactedResources Get. */
public final class ImpactedResourcesGetSamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/ImpactedResources_Get.json
     */
    /**
     * Sample code: ImpactedResourcesGet.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void impactedResourcesGet(com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager.impactedResources().getWithResponse("BC_1-FXZ", "abc-123-ghj-456", com.azure.core.util.Context.NONE);
    }
}
```

### ImpactedResources_GetByTenantId

```java
/** Samples for ImpactedResources GetByTenantId. */
public final class ImpactedResourcesGetByTenantIdSamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/ImpactedResources_GetByTenantId.json
     */
    /**
     * Sample code: ImpactedResourcesGet.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void impactedResourcesGet(com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager
            .impactedResources()
            .getByTenantIdWithResponse("BC_1-FXZ", "abc-123-ghj-456", com.azure.core.util.Context.NONE);
    }
}
```

### ImpactedResources_ListBySubscriptionIdAndEventId

```java
/** Samples for ImpactedResources ListBySubscriptionIdAndEventId. */
public final class ImpactedResourcesListBySubscriptionIdAndEventIdSamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/ImpactedResources_ListBySubscriptionId_ListByEventId.json
     */
    /**
     * Sample code: ListImpactedResourcesBySubscriptionId.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void listImpactedResourcesBySubscriptionId(
        com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager
            .impactedResources()
            .listBySubscriptionIdAndEventId("BC_1-FXZ", "targetRegion eq 'westus'", com.azure.core.util.Context.NONE);
    }
}
```

### ImpactedResources_ListByTenantIdAndEventId

```java
/** Samples for ImpactedResources ListByTenantIdAndEventId. */
public final class ImpactedResourcesListByTenantIdAndEventIdSamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/ImpactedResources_ListByTenantId_ListByEventId.json
     */
    /**
     * Sample code: ListEventsByTenantId.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void listEventsByTenantId(com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager
            .impactedResources()
            .listByTenantIdAndEventId("BC_1-FXZ", "targetRegion eq 'westus'", com.azure.core.util.Context.NONE);
    }
}
```

### Metadata_GetEntity

```java
/** Samples for Metadata GetEntity. */
public final class MetadataGetEntitySamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/Metadata_GetEntity.json
     */
    /**
     * Sample code: GetMetadata.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void getMetadata(com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager.metadatas().getEntityWithResponse("status", com.azure.core.util.Context.NONE);
    }
}
```

### Metadata_List

```java
/** Samples for Metadata List. */
public final class MetadataListSamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/Metadata_List.json
     */
    /**
     * Sample code: GetMetadata.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void getMetadata(com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager.metadatas().list(com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: GetOperationsList.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void getOperationsList(com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager.operations().listWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### SecurityAdvisoryImpactedResources_ListBySubscriptionIdAndEventId

```java
/** Samples for SecurityAdvisoryImpactedResources ListBySubscriptionIdAndEventId. */
public final class SecurityAdvisoryImpactedResourcesListBySubscriptionIdAndEventIdSamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/SecurityAdvisoryImpactedResources_ListBySubscriptionId_ListByEventId.json
     */
    /**
     * Sample code: ListSecurityAdvisoryImpactedResourcesBySubscriptionId.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void listSecurityAdvisoryImpactedResourcesBySubscriptionId(
        com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager
            .securityAdvisoryImpactedResources()
            .listBySubscriptionIdAndEventId("BC_1-FXZ", null, com.azure.core.util.Context.NONE);
    }
}
```

### SecurityAdvisoryImpactedResources_ListByTenantIdAndEventId

```java
/** Samples for SecurityAdvisoryImpactedResources ListByTenantIdAndEventId. */
public final class SecurityAdvisoryImpactedResourcesListByTenantIdAndEventIdSamples {
    /*
     * x-ms-original-file: specification/resourcehealth/resource-manager/Microsoft.ResourceHealth/preview/2023-10-01-preview/examples/SecurityAdvisoryImpactedResources_ListByTenantId_ListByEventId.json
     */
    /**
     * Sample code: ListSecurityAdvisoryImpactedResourcesByTenantId.
     *
     * @param manager Entry point to ResourceHealthManager.
     */
    public static void listSecurityAdvisoryImpactedResourcesByTenantId(
        com.azure.resourcemanager.resourcehealth.ResourceHealthManager manager) {
        manager
            .securityAdvisoryImpactedResources()
            .listByTenantIdAndEventId("BC_1-FXZ", null, com.azure.core.util.Context.NONE);
    }
}
```

