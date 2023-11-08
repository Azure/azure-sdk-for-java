# Code snippets and samples


## AccessPolicies

- [CreateOrUpdate](#accesspolicies_createorupdate)
- [Delete](#accesspolicies_delete)
- [Get](#accesspolicies_get)
- [ListByEnvironment](#accesspolicies_listbyenvironment)
- [Update](#accesspolicies_update)

## Environments

- [CreateOrUpdate](#environments_createorupdate)
- [Delete](#environments_delete)
- [GetByResourceGroup](#environments_getbyresourcegroup)
- [ListByResourceGroup](#environments_listbyresourcegroup)
- [ListBySubscription](#environments_listbysubscription)
- [Update](#environments_update)

## EventSources

- [CreateOrUpdate](#eventsources_createorupdate)
- [Delete](#eventsources_delete)
- [Get](#eventsources_get)
- [ListByEnvironment](#eventsources_listbyenvironment)
- [Update](#eventsources_update)

## Operations

- [List](#operations_list)

## ReferenceDataSets

- [CreateOrUpdate](#referencedatasets_createorupdate)
- [Delete](#referencedatasets_delete)
- [Get](#referencedatasets_get)
- [ListByEnvironment](#referencedatasets_listbyenvironment)
- [Update](#referencedatasets_update)
### AccessPolicies_CreateOrUpdate

```java
import com.azure.resourcemanager.timeseriesinsights.models.AccessPolicyRole;
import java.util.Arrays;

/** Samples for AccessPolicies CreateOrUpdate. */
public final class AccessPoliciesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/timeseriesinsights/resource-manager/Microsoft.TimeSeriesInsights/stable/2020-05-15/examples/AccessPoliciesCreate.json
     */
    /**
     * Sample code: AccessPoliciesCreate.
     *
     * @param manager Entry point to TimeSeriesInsightsManager.
     */
    public static void accessPoliciesCreate(
        com.azure.resourcemanager.timeseriesinsights.TimeSeriesInsightsManager manager) {
        manager
            .accessPolicies()
            .define("ap1")
            .withExistingEnvironment("rg1", "env1")
            .withPrincipalObjectId("aGuid")
            .withDescription("some description")
            .withRoles(Arrays.asList(AccessPolicyRole.READER))
            .create();
    }
}
```

### AccessPolicies_Delete

```java
/** Samples for AccessPolicies Delete. */
public final class AccessPoliciesDeleteSamples {
    /*
     * x-ms-original-file: specification/timeseriesinsights/resource-manager/Microsoft.TimeSeriesInsights/stable/2020-05-15/examples/AccessPoliciesDelete.json
     */
    /**
     * Sample code: AccessPoliciesDelete.
     *
     * @param manager Entry point to TimeSeriesInsightsManager.
     */
    public static void accessPoliciesDelete(
        com.azure.resourcemanager.timeseriesinsights.TimeSeriesInsightsManager manager) {
        manager.accessPolicies().deleteWithResponse("rg1", "env1", "ap1", com.azure.core.util.Context.NONE);
    }
}
```

### AccessPolicies_Get

```java
/** Samples for AccessPolicies Get. */
public final class AccessPoliciesGetSamples {
    /*
     * x-ms-original-file: specification/timeseriesinsights/resource-manager/Microsoft.TimeSeriesInsights/stable/2020-05-15/examples/AccessPoliciesGet.json
     */
    /**
     * Sample code: AccessPoliciesGet.
     *
     * @param manager Entry point to TimeSeriesInsightsManager.
     */
    public static void accessPoliciesGet(
        com.azure.resourcemanager.timeseriesinsights.TimeSeriesInsightsManager manager) {
        manager.accessPolicies().getWithResponse("rg1", "env1", "ap1", com.azure.core.util.Context.NONE);
    }
}
```

### AccessPolicies_ListByEnvironment

```java
/** Samples for AccessPolicies ListByEnvironment. */
public final class AccessPoliciesListByEnvironmentSamples {
    /*
     * x-ms-original-file: specification/timeseriesinsights/resource-manager/Microsoft.TimeSeriesInsights/stable/2020-05-15/examples/AccessPoliciesListByEnvironment.json
     */
    /**
     * Sample code: AccessPoliciesByEnvironment.
     *
     * @param manager Entry point to TimeSeriesInsightsManager.
     */
    public static void accessPoliciesByEnvironment(
        com.azure.resourcemanager.timeseriesinsights.TimeSeriesInsightsManager manager) {
        manager.accessPolicies().listByEnvironmentWithResponse("rg1", "env1", com.azure.core.util.Context.NONE);
    }
}
```

### AccessPolicies_Update

```java
import com.azure.resourcemanager.timeseriesinsights.models.AccessPolicyResource;
import com.azure.resourcemanager.timeseriesinsights.models.AccessPolicyRole;
import java.util.Arrays;

/** Samples for AccessPolicies Update. */
public final class AccessPoliciesUpdateSamples {
    /*
     * x-ms-original-file: specification/timeseriesinsights/resource-manager/Microsoft.TimeSeriesInsights/stable/2020-05-15/examples/AccessPoliciesPatchRoles.json
     */
    /**
     * Sample code: AccessPoliciesUpdate.
     *
     * @param manager Entry point to TimeSeriesInsightsManager.
     */
    public static void accessPoliciesUpdate(
        com.azure.resourcemanager.timeseriesinsights.TimeSeriesInsightsManager manager) {
        AccessPolicyResource resource =
            manager.accessPolicies().getWithResponse("rg1", "env1", "ap1", com.azure.core.util.Context.NONE).getValue();
        resource.update().withRoles(Arrays.asList(AccessPolicyRole.READER, AccessPolicyRole.CONTRIBUTOR)).apply();
    }
}
```

### Environments_CreateOrUpdate

```java
import com.azure.resourcemanager.timeseriesinsights.models.Gen1EnvironmentCreateOrUpdateParameters;
import com.azure.resourcemanager.timeseriesinsights.models.PropertyType;
import com.azure.resourcemanager.timeseriesinsights.models.Sku;
import com.azure.resourcemanager.timeseriesinsights.models.SkuName;
import com.azure.resourcemanager.timeseriesinsights.models.TimeSeriesIdProperty;
import java.time.Duration;
import java.util.Arrays;

/** Samples for Environments CreateOrUpdate. */
public final class EnvironmentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/timeseriesinsights/resource-manager/Microsoft.TimeSeriesInsights/stable/2020-05-15/examples/EnvironmentsCreate.json
     */
    /**
     * Sample code: EnvironmentsCreate.
     *
     * @param manager Entry point to TimeSeriesInsightsManager.
     */
    public static void environmentsCreate(
        com.azure.resourcemanager.timeseriesinsights.TimeSeriesInsightsManager manager) {
        manager
            .environments()
            .createOrUpdate(
                "rg1",
                "env1",
                new Gen1EnvironmentCreateOrUpdateParameters()
                    .withLocation("West US")
                    .withSku(new Sku().withName(SkuName.S1).withCapacity(1))
                    .withDataRetentionTime(Duration.parse("P31D"))
                    .withPartitionKeyProperties(
                        Arrays.asList(new TimeSeriesIdProperty().withName("DeviceId1").withType(PropertyType.STRING))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Environments_Delete

```java
/** Samples for Environments Delete. */
public final class EnvironmentsDeleteSamples {
    /*
     * x-ms-original-file: specification/timeseriesinsights/resource-manager/Microsoft.TimeSeriesInsights/stable/2020-05-15/examples/EnvironmentsDelete.json
     */
    /**
     * Sample code: EnvironmentsDelete.
     *
     * @param manager Entry point to TimeSeriesInsightsManager.
     */
    public static void environmentsDelete(
        com.azure.resourcemanager.timeseriesinsights.TimeSeriesInsightsManager manager) {
        manager.environments().deleteByResourceGroupWithResponse("rg1", "env1", com.azure.core.util.Context.NONE);
    }
}
```

### Environments_GetByResourceGroup

```java
/** Samples for Environments GetByResourceGroup. */
public final class EnvironmentsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/timeseriesinsights/resource-manager/Microsoft.TimeSeriesInsights/stable/2020-05-15/examples/EnvironmentsGet.json
     */
    /**
     * Sample code: EnvironmentsGet.
     *
     * @param manager Entry point to TimeSeriesInsightsManager.
     */
    public static void environmentsGet(com.azure.resourcemanager.timeseriesinsights.TimeSeriesInsightsManager manager) {
        manager.environments().getByResourceGroupWithResponse("rg1", "env1", null, com.azure.core.util.Context.NONE);
    }
}
```

### Environments_ListByResourceGroup

```java
/** Samples for Environments ListByResourceGroup. */
public final class EnvironmentsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/timeseriesinsights/resource-manager/Microsoft.TimeSeriesInsights/stable/2020-05-15/examples/EnvironmentsListByResourceGroup.json
     */
    /**
     * Sample code: EnvironmentsByResourceGroup.
     *
     * @param manager Entry point to TimeSeriesInsightsManager.
     */
    public static void environmentsByResourceGroup(
        com.azure.resourcemanager.timeseriesinsights.TimeSeriesInsightsManager manager) {
        manager.environments().listByResourceGroupWithResponse("rg1", com.azure.core.util.Context.NONE);
    }
}
```

### Environments_ListBySubscription

```java
/** Samples for Environments ListBySubscription. */
public final class EnvironmentsListBySubscriptionSamples {
    /*
     * x-ms-original-file: specification/timeseriesinsights/resource-manager/Microsoft.TimeSeriesInsights/stable/2020-05-15/examples/EnvironmentsListBySubscription.json
     */
    /**
     * Sample code: EnvironmentsBySubscription.
     *
     * @param manager Entry point to TimeSeriesInsightsManager.
     */
    public static void environmentsBySubscription(
        com.azure.resourcemanager.timeseriesinsights.TimeSeriesInsightsManager manager) {
        manager.environments().listBySubscriptionWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### Environments_Update

```java
import com.azure.resourcemanager.timeseriesinsights.models.EnvironmentUpdateParameters;
import java.util.HashMap;
import java.util.Map;

/** Samples for Environments Update. */
public final class EnvironmentsUpdateSamples {
    /*
     * x-ms-original-file: specification/timeseriesinsights/resource-manager/Microsoft.TimeSeriesInsights/stable/2020-05-15/examples/EnvironmentsPatchTags.json
     */
    /**
     * Sample code: EnvironmentsUpdate.
     *
     * @param manager Entry point to TimeSeriesInsightsManager.
     */
    public static void environmentsUpdate(
        com.azure.resourcemanager.timeseriesinsights.TimeSeriesInsightsManager manager) {
        manager
            .environments()
            .update(
                "rg1",
                "env1",
                new EnvironmentUpdateParameters().withTags(mapOf("someTag", "someTagValue")),
                com.azure.core.util.Context.NONE);
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

### EventSources_CreateOrUpdate

```java
import com.azure.resourcemanager.timeseriesinsights.models.EventHubEventSourceCreateOrUpdateParameters;
import com.azure.resourcemanager.timeseriesinsights.models.IngressStartAtType;
import com.azure.resourcemanager.timeseriesinsights.models.LocalTimestamp;
import com.azure.resourcemanager.timeseriesinsights.models.LocalTimestampFormat;
import com.azure.resourcemanager.timeseriesinsights.models.LocalTimestampTimeZoneOffset;

/** Samples for EventSources CreateOrUpdate. */
public final class EventSourcesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/timeseriesinsights/resource-manager/Microsoft.TimeSeriesInsights/stable/2020-05-15/examples/EventSourcesCreateEventHub.json
     */
    /**
     * Sample code: CreateEventHubEventSource.
     *
     * @param manager Entry point to TimeSeriesInsightsManager.
     */
    public static void createEventHubEventSource(
        com.azure.resourcemanager.timeseriesinsights.TimeSeriesInsightsManager manager) {
        manager
            .eventSources()
            .createOrUpdateWithResponse(
                "rg1",
                "env1",
                "es1",
                new EventHubEventSourceCreateOrUpdateParameters()
                    .withLocation("West US")
                    .withSharedAccessKey("fakeTokenPlaceholder")
                    .withServiceBusNamespace("sbn")
                    .withEventHubName("ehn")
                    .withConsumerGroupName("cgn")
                    .withKeyName("fakeTokenPlaceholder")
                    .withEventSourceResourceId("somePathInArm")
                    .withTimestampPropertyName("someTimestampProperty")
                    .withLocalTimestampPropertiesLocalTimestamp(
                        new LocalTimestamp()
                            .withFormat(LocalTimestampFormat.fromString("TimeSpan"))
                            .withTimeZoneOffset(
                                new LocalTimestampTimeZoneOffset().withPropertyName("someEventPropertyName")))
                    .withType(IngressStartAtType.EARLIEST_AVAILABLE),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/timeseriesinsights/resource-manager/Microsoft.TimeSeriesInsights/stable/2020-05-15/examples/EventSourcesCreateEventHubWithCustomEnquedTime.json
     */
    /**
     * Sample code: EventSourcesCreateEventHubWithCustomEnquedTime.
     *
     * @param manager Entry point to TimeSeriesInsightsManager.
     */
    public static void eventSourcesCreateEventHubWithCustomEnquedTime(
        com.azure.resourcemanager.timeseriesinsights.TimeSeriesInsightsManager manager) {
        manager
            .eventSources()
            .createOrUpdateWithResponse(
                "rg1",
                "env1",
                "es1",
                new EventHubEventSourceCreateOrUpdateParameters()
                    .withLocation("West US")
                    .withSharedAccessKey("fakeTokenPlaceholder")
                    .withServiceBusNamespace("sbn")
                    .withEventHubName("ehn")
                    .withConsumerGroupName("cgn")
                    .withKeyName("fakeTokenPlaceholder")
                    .withEventSourceResourceId("somePathInArm")
                    .withTimestampPropertyName("someTimestampProperty")
                    .withType(IngressStartAtType.CUSTOM_ENQUEUED_TIME)
                    .withTime("2017-04-01T19:20:33.2288820Z"),
                com.azure.core.util.Context.NONE);
    }
}
```

### EventSources_Delete

```java
/** Samples for EventSources Delete. */
public final class EventSourcesDeleteSamples {
    /*
     * x-ms-original-file: specification/timeseriesinsights/resource-manager/Microsoft.TimeSeriesInsights/stable/2020-05-15/examples/EventSourcesDelete.json
     */
    /**
     * Sample code: DeleteEventSource.
     *
     * @param manager Entry point to TimeSeriesInsightsManager.
     */
    public static void deleteEventSource(
        com.azure.resourcemanager.timeseriesinsights.TimeSeriesInsightsManager manager) {
        manager.eventSources().deleteWithResponse("rg1", "env1", "es1", com.azure.core.util.Context.NONE);
    }
}
```

### EventSources_Get

```java
/** Samples for EventSources Get. */
public final class EventSourcesGetSamples {
    /*
     * x-ms-original-file: specification/timeseriesinsights/resource-manager/Microsoft.TimeSeriesInsights/stable/2020-05-15/examples/EventSourcesGetEventHub.json
     */
    /**
     * Sample code: GetEventHubEventSource.
     *
     * @param manager Entry point to TimeSeriesInsightsManager.
     */
    public static void getEventHubEventSource(
        com.azure.resourcemanager.timeseriesinsights.TimeSeriesInsightsManager manager) {
        manager.eventSources().getWithResponse("rg1", "env1", "es1", com.azure.core.util.Context.NONE);
    }
}
```

### EventSources_ListByEnvironment

```java
/** Samples for EventSources ListByEnvironment. */
public final class EventSourcesListByEnvironmentSamples {
    /*
     * x-ms-original-file: specification/timeseriesinsights/resource-manager/Microsoft.TimeSeriesInsights/stable/2020-05-15/examples/EventSourcesListByEnvironment.json
     */
    /**
     * Sample code: ListEventSourcesByEnvironment.
     *
     * @param manager Entry point to TimeSeriesInsightsManager.
     */
    public static void listEventSourcesByEnvironment(
        com.azure.resourcemanager.timeseriesinsights.TimeSeriesInsightsManager manager) {
        manager.eventSources().listByEnvironmentWithResponse("rg1", "env1", com.azure.core.util.Context.NONE);
    }
}
```

### EventSources_Update

```java
import com.azure.resourcemanager.timeseriesinsights.models.EventSourceUpdateParameters;
import java.util.HashMap;
import java.util.Map;

/** Samples for EventSources Update. */
public final class EventSourcesUpdateSamples {
    /*
     * x-ms-original-file: specification/timeseriesinsights/resource-manager/Microsoft.TimeSeriesInsights/stable/2020-05-15/examples/EventSourcesPatchTags.json
     */
    /**
     * Sample code: UpdateEventSource.
     *
     * @param manager Entry point to TimeSeriesInsightsManager.
     */
    public static void updateEventSource(
        com.azure.resourcemanager.timeseriesinsights.TimeSeriesInsightsManager manager) {
        manager
            .eventSources()
            .updateWithResponse(
                "rg1",
                "env1",
                "es1",
                new EventSourceUpdateParameters().withTags(mapOf("someKey", "someValue")),
                com.azure.core.util.Context.NONE);
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

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/timeseriesinsights/resource-manager/Microsoft.TimeSeriesInsights/stable/2020-05-15/examples/Operation_List.json
     */
    /**
     * Sample code: List available operations for the Time Series Insights resource provider.
     *
     * @param manager Entry point to TimeSeriesInsightsManager.
     */
    public static void listAvailableOperationsForTheTimeSeriesInsightsResourceProvider(
        com.azure.resourcemanager.timeseriesinsights.TimeSeriesInsightsManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ReferenceDataSets_CreateOrUpdate

```java
import com.azure.resourcemanager.timeseriesinsights.models.ReferenceDataKeyPropertyType;
import com.azure.resourcemanager.timeseriesinsights.models.ReferenceDataSetKeyProperty;
import java.util.Arrays;

/** Samples for ReferenceDataSets CreateOrUpdate. */
public final class ReferenceDataSetsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/timeseriesinsights/resource-manager/Microsoft.TimeSeriesInsights/stable/2020-05-15/examples/ReferenceDataSetsCreate.json
     */
    /**
     * Sample code: ReferenceDataSetsCreate.
     *
     * @param manager Entry point to TimeSeriesInsightsManager.
     */
    public static void referenceDataSetsCreate(
        com.azure.resourcemanager.timeseriesinsights.TimeSeriesInsightsManager manager) {
        manager
            .referenceDataSets()
            .define("rds1")
            .withRegion("West US")
            .withExistingEnvironment("rg1", "env1")
            .withKeyProperties(
                Arrays
                    .asList(
                        new ReferenceDataSetKeyProperty()
                            .withName("DeviceId1")
                            .withType(ReferenceDataKeyPropertyType.STRING),
                        new ReferenceDataSetKeyProperty()
                            .withName("DeviceFloor")
                            .withType(ReferenceDataKeyPropertyType.DOUBLE)))
            .create();
    }
}
```

### ReferenceDataSets_Delete

```java
/** Samples for ReferenceDataSets Delete. */
public final class ReferenceDataSetsDeleteSamples {
    /*
     * x-ms-original-file: specification/timeseriesinsights/resource-manager/Microsoft.TimeSeriesInsights/stable/2020-05-15/examples/ReferenceDataSetsDelete.json
     */
    /**
     * Sample code: ReferenceDataSetsDelete.
     *
     * @param manager Entry point to TimeSeriesInsightsManager.
     */
    public static void referenceDataSetsDelete(
        com.azure.resourcemanager.timeseriesinsights.TimeSeriesInsightsManager manager) {
        manager.referenceDataSets().deleteWithResponse("rg1", "env1", "rds1", com.azure.core.util.Context.NONE);
    }
}
```

### ReferenceDataSets_Get

```java
/** Samples for ReferenceDataSets Get. */
public final class ReferenceDataSetsGetSamples {
    /*
     * x-ms-original-file: specification/timeseriesinsights/resource-manager/Microsoft.TimeSeriesInsights/stable/2020-05-15/examples/ReferenceDataSetsGet.json
     */
    /**
     * Sample code: ReferenceDataSetsGet.
     *
     * @param manager Entry point to TimeSeriesInsightsManager.
     */
    public static void referenceDataSetsGet(
        com.azure.resourcemanager.timeseriesinsights.TimeSeriesInsightsManager manager) {
        manager.referenceDataSets().getWithResponse("rg1", "env1", "rds1", com.azure.core.util.Context.NONE);
    }
}
```

### ReferenceDataSets_ListByEnvironment

```java
/** Samples for ReferenceDataSets ListByEnvironment. */
public final class ReferenceDataSetsListByEnvironmentSamples {
    /*
     * x-ms-original-file: specification/timeseriesinsights/resource-manager/Microsoft.TimeSeriesInsights/stable/2020-05-15/examples/ReferenceDataSetsListByEnvironment.json
     */
    /**
     * Sample code: ReferenceDataSetsListByEnvironment.
     *
     * @param manager Entry point to TimeSeriesInsightsManager.
     */
    public static void referenceDataSetsListByEnvironment(
        com.azure.resourcemanager.timeseriesinsights.TimeSeriesInsightsManager manager) {
        manager.referenceDataSets().listByEnvironmentWithResponse("rg1", "env1", com.azure.core.util.Context.NONE);
    }
}
```

### ReferenceDataSets_Update

```java
import com.azure.resourcemanager.timeseriesinsights.models.ReferenceDataSetResource;
import java.util.HashMap;
import java.util.Map;

/** Samples for ReferenceDataSets Update. */
public final class ReferenceDataSetsUpdateSamples {
    /*
     * x-ms-original-file: specification/timeseriesinsights/resource-manager/Microsoft.TimeSeriesInsights/stable/2020-05-15/examples/ReferenceDataSetsPatchTags.json
     */
    /**
     * Sample code: ReferenceDataSetsUpdate.
     *
     * @param manager Entry point to TimeSeriesInsightsManager.
     */
    public static void referenceDataSetsUpdate(
        com.azure.resourcemanager.timeseriesinsights.TimeSeriesInsightsManager manager) {
        ReferenceDataSetResource resource =
            manager
                .referenceDataSets()
                .getWithResponse("rg1", "env1", "rds1", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("someKey", "someValue")).apply();
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

