# Code snippets and samples


## DomainTopics

- [CreateOrUpdate](#domaintopics_createorupdate)
- [Delete](#domaintopics_delete)
- [Get](#domaintopics_get)
- [ListByDomain](#domaintopics_listbydomain)

## Domains

- [CreateOrUpdate](#domains_createorupdate)
- [Delete](#domains_delete)
- [GetByResourceGroup](#domains_getbyresourcegroup)
- [List](#domains_list)
- [ListByResourceGroup](#domains_listbyresourcegroup)
- [ListSharedAccessKeys](#domains_listsharedaccesskeys)
- [RegenerateKey](#domains_regeneratekey)
- [Update](#domains_update)

## EventSubscriptions

- [CreateOrUpdate](#eventsubscriptions_createorupdate)
- [Delete](#eventsubscriptions_delete)
- [Get](#eventsubscriptions_get)
- [GetDeliveryAttributes](#eventsubscriptions_getdeliveryattributes)
- [GetFullUrl](#eventsubscriptions_getfullurl)
- [List](#eventsubscriptions_list)
- [ListByDomainTopic](#eventsubscriptions_listbydomaintopic)
- [ListByResource](#eventsubscriptions_listbyresource)
- [ListByResourceGroup](#eventsubscriptions_listbyresourcegroup)
- [ListGlobalByResourceGroupForTopicType](#eventsubscriptions_listglobalbyresourcegroupfortopictype)
- [ListGlobalBySubscriptionForTopicType](#eventsubscriptions_listglobalbysubscriptionfortopictype)
- [ListRegionalByResourceGroup](#eventsubscriptions_listregionalbyresourcegroup)
- [ListRegionalByResourceGroupForTopicType](#eventsubscriptions_listregionalbyresourcegroupfortopictype)
- [ListRegionalBySubscription](#eventsubscriptions_listregionalbysubscription)
- [ListRegionalBySubscriptionForTopicType](#eventsubscriptions_listregionalbysubscriptionfortopictype)
- [Update](#eventsubscriptions_update)

## ExtensionTopics

- [Get](#extensiontopics_get)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByResource](#privateendpointconnections_listbyresource)
- [Update](#privateendpointconnections_update)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [ListByResource](#privatelinkresources_listbyresource)

## SystemTopicEventSubscriptions

- [CreateOrUpdate](#systemtopiceventsubscriptions_createorupdate)
- [Delete](#systemtopiceventsubscriptions_delete)
- [Get](#systemtopiceventsubscriptions_get)
- [GetDeliveryAttributes](#systemtopiceventsubscriptions_getdeliveryattributes)
- [GetFullUrl](#systemtopiceventsubscriptions_getfullurl)
- [ListBySystemTopic](#systemtopiceventsubscriptions_listbysystemtopic)
- [Update](#systemtopiceventsubscriptions_update)

## SystemTopics

- [CreateOrUpdate](#systemtopics_createorupdate)
- [Delete](#systemtopics_delete)
- [GetByResourceGroup](#systemtopics_getbyresourcegroup)
- [List](#systemtopics_list)
- [ListByResourceGroup](#systemtopics_listbyresourcegroup)
- [Update](#systemtopics_update)

## TopicTypes

- [Get](#topictypes_get)
- [List](#topictypes_list)
- [ListEventTypes](#topictypes_listeventtypes)

## Topics

- [CreateOrUpdate](#topics_createorupdate)
- [Delete](#topics_delete)
- [GetByResourceGroup](#topics_getbyresourcegroup)
- [List](#topics_list)
- [ListByResourceGroup](#topics_listbyresourcegroup)
- [ListEventTypes](#topics_listeventtypes)
- [ListSharedAccessKeys](#topics_listsharedaccesskeys)
- [RegenerateKey](#topics_regeneratekey)
- [Update](#topics_update)
### DomainTopics_CreateOrUpdate

```java
import com.azure.core.util.Context;

/** Samples for DomainTopics CreateOrUpdate. */
public final class DomainTopicsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/DomainTopics_CreateOrUpdate.json
     */
    /**
     * Sample code: DomainTopics_CreateOrUpdate.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void domainTopicsCreateOrUpdate(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.domainTopics().createOrUpdate("examplerg", "exampledomain1", "exampledomaintopic1", Context.NONE);
    }
}
```

### DomainTopics_Delete

```java
import com.azure.core.util.Context;

/** Samples for DomainTopics Delete. */
public final class DomainTopicsDeleteSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/DomainTopics_Delete.json
     */
    /**
     * Sample code: DomainTopics_Delete.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void domainTopicsDelete(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.domainTopics().delete("examplerg", "exampledomain1", "exampledomaintopic1", Context.NONE);
    }
}
```

### DomainTopics_Get

```java
import com.azure.core.util.Context;

/** Samples for DomainTopics Get. */
public final class DomainTopicsGetSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/DomainTopics_Get.json
     */
    /**
     * Sample code: DomainTopics_Get.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void domainTopicsGet(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.domainTopics().getWithResponse("examplerg", "exampledomain2", "topic1", Context.NONE);
    }
}
```

### DomainTopics_ListByDomain

```java
import com.azure.core.util.Context;

/** Samples for DomainTopics ListByDomain. */
public final class DomainTopicsListByDomainSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/DomainTopics_ListByDomain.json
     */
    /**
     * Sample code: DomainTopics_ListByDomain.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void domainTopicsListByDomain(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.domainTopics().listByDomain("examplerg", "exampledomain2", null, null, Context.NONE);
    }
}
```

### Domains_CreateOrUpdate

```java
import com.azure.resourcemanager.eventgrid.models.InboundIpRule;
import com.azure.resourcemanager.eventgrid.models.IpActionType;
import com.azure.resourcemanager.eventgrid.models.PublicNetworkAccess;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Domains CreateOrUpdate. */
public final class DomainsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/Domains_CreateOrUpdate.json
     */
    /**
     * Sample code: Domains_CreateOrUpdate.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void domainsCreateOrUpdate(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .domains()
            .define("exampledomain1")
            .withRegion("westus2")
            .withExistingResourceGroup("examplerg")
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
            .withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
            .withInboundIpRules(
                Arrays
                    .asList(
                        new InboundIpRule().withIpMask("12.18.30.15").withAction(IpActionType.ALLOW),
                        new InboundIpRule().withIpMask("12.18.176.1").withAction(IpActionType.ALLOW)))
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

### Domains_Delete

```java
import com.azure.core.util.Context;

/** Samples for Domains Delete. */
public final class DomainsDeleteSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/Domains_Delete.json
     */
    /**
     * Sample code: Domains_Delete.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void domainsDelete(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.domains().delete("examplerg", "exampledomain1", Context.NONE);
    }
}
```

### Domains_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Domains GetByResourceGroup. */
public final class DomainsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/Domains_Get.json
     */
    /**
     * Sample code: Domains_Get.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void domainsGet(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.domains().getByResourceGroupWithResponse("examplerg", "exampledomain2", Context.NONE);
    }
}
```

### Domains_List

```java
import com.azure.core.util.Context;

/** Samples for Domains List. */
public final class DomainsListSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/Domains_ListBySubscription.json
     */
    /**
     * Sample code: Domains_ListBySubscription.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void domainsListBySubscription(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.domains().list(null, null, Context.NONE);
    }
}
```

### Domains_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Domains ListByResourceGroup. */
public final class DomainsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/Domains_ListByResourceGroup.json
     */
    /**
     * Sample code: Domains_ListByResourceGroup.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void domainsListByResourceGroup(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.domains().listByResourceGroup("examplerg", null, null, Context.NONE);
    }
}
```

### Domains_ListSharedAccessKeys

```java
import com.azure.core.util.Context;

/** Samples for Domains ListSharedAccessKeys. */
public final class DomainsListSharedAccessKeysSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/Domains_ListSharedAccessKeys.json
     */
    /**
     * Sample code: Domains_ListSharedAccessKeys.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void domainsListSharedAccessKeys(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.domains().listSharedAccessKeysWithResponse("examplerg", "exampledomain2", Context.NONE);
    }
}
```

### Domains_RegenerateKey

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.eventgrid.models.DomainRegenerateKeyRequest;

/** Samples for Domains RegenerateKey. */
public final class DomainsRegenerateKeySamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/Domains_RegenerateKey.json
     */
    /**
     * Sample code: Domains_RegenerateKey.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void domainsRegenerateKey(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .domains()
            .regenerateKeyWithResponse(
                "examplerg", "exampledomain2", new DomainRegenerateKeyRequest().withKeyName("key1"), Context.NONE);
    }
}
```

### Domains_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.eventgrid.models.Domain;
import com.azure.resourcemanager.eventgrid.models.InboundIpRule;
import com.azure.resourcemanager.eventgrid.models.IpActionType;
import com.azure.resourcemanager.eventgrid.models.PublicNetworkAccess;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Domains Update. */
public final class DomainsUpdateSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/Domains_Update.json
     */
    /**
     * Sample code: Domains_Update.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void domainsUpdate(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        Domain resource =
            manager.domains().getByResourceGroupWithResponse("examplerg", "exampledomain1", Context.NONE).getValue();
        resource
            .update()
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
            .withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
            .withInboundIpRules(
                Arrays
                    .asList(
                        new InboundIpRule().withIpMask("12.18.30.15").withAction(IpActionType.ALLOW),
                        new InboundIpRule().withIpMask("12.18.176.1").withAction(IpActionType.ALLOW)))
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

### EventSubscriptions_CreateOrUpdate

```java
import com.azure.resourcemanager.eventgrid.models.EventHubEventSubscriptionDestination;
import com.azure.resourcemanager.eventgrid.models.EventSubscriptionFilter;
import com.azure.resourcemanager.eventgrid.models.HybridConnectionEventSubscriptionDestination;
import com.azure.resourcemanager.eventgrid.models.StorageBlobDeadLetterDestination;
import com.azure.resourcemanager.eventgrid.models.StorageQueueEventSubscriptionDestination;
import com.azure.resourcemanager.eventgrid.models.WebhookEventSubscriptionDestination;

/** Samples for EventSubscriptions CreateOrUpdate. */
public final class EventSubscriptionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_CreateOrUpdateForCustomTopic_WebhookDestination.json
     */
    /**
     * Sample code: EventSubscriptions_CreateOrUpdateForCustomTopic_WebhookDestination.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsCreateOrUpdateForCustomTopicWebhookDestination(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .define("examplesubscription1")
            .withExistingScope(
                "subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4/resourceGroups/examplerg/providers/Microsoft.EventGrid/topics/exampletopic1")
            .withDestination(
                new WebhookEventSubscriptionDestination()
                    .withEndpointUrl(
                        "https://azurefunctionexample.azurewebsites.net/runtime/webhooks/EventGrid?functionName=EventGridTrigger1&code=PASSWORDCODE"))
            .withFilter(
                new EventSubscriptionFilter()
                    .withSubjectBeginsWith("ExamplePrefix")
                    .withSubjectEndsWith("ExampleSuffix")
                    .withIsSubjectCaseSensitive(false))
            .create();
    }

    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_CreateOrUpdateForSubscription.json
     */
    /**
     * Sample code: EventSubscriptions_CreateOrUpdateForSubscription.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsCreateOrUpdateForSubscription(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .define("examplesubscription3")
            .withExistingScope("subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4")
            .withDestination(new WebhookEventSubscriptionDestination().withEndpointUrl("https://requestb.in/15ksip71"))
            .withFilter(new EventSubscriptionFilter().withIsSubjectCaseSensitive(false))
            .create();
    }

    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_CreateOrUpdateForResource.json
     */
    /**
     * Sample code: EventSubscriptions_CreateOrUpdateForResource.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsCreateOrUpdateForResource(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .define("examplesubscription10")
            .withExistingScope(
                "subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4/resourceGroups/examplerg/providers/Microsoft.EventHub/namespaces/examplenamespace1")
            .withDestination(new WebhookEventSubscriptionDestination().withEndpointUrl("https://requestb.in/15ksip71"))
            .withFilter(
                new EventSubscriptionFilter()
                    .withSubjectBeginsWith("ExamplePrefix")
                    .withSubjectEndsWith("ExampleSuffix")
                    .withIsSubjectCaseSensitive(false))
            .create();
    }

    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_CreateOrUpdateForResourceGroup.json
     */
    /**
     * Sample code: EventSubscriptions_CreateOrUpdateForResourceGroup.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsCreateOrUpdateForResourceGroup(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .define("examplesubscription2")
            .withExistingScope("subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4/resourceGroups/examplerg")
            .withDestination(new WebhookEventSubscriptionDestination().withEndpointUrl("https://requestb.in/15ksip71"))
            .withFilter(
                new EventSubscriptionFilter()
                    .withSubjectBeginsWith("ExamplePrefix")
                    .withSubjectEndsWith("ExampleSuffix")
                    .withIsSubjectCaseSensitive(false))
            .create();
    }

    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_CreateOrUpdateForCustomTopic_EventHubDestination.json
     */
    /**
     * Sample code: EventSubscriptions_CreateOrUpdateForCustomTopic_EventHubDestination.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsCreateOrUpdateForCustomTopicEventHubDestination(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .define("examplesubscription1")
            .withExistingScope(
                "subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4/resourceGroups/examplerg/providers/Microsoft.EventGrid/topics/exampletopic1")
            .withDestination(
                new EventHubEventSubscriptionDestination()
                    .withResourceId(
                        "/subscriptions/55f3dcd4-cac7-43b4-990b-a139d62a1eb2/resourceGroups/TestRG/providers/Microsoft.EventHub/namespaces/ContosoNamespace/eventhubs/EH1"))
            .withFilter(
                new EventSubscriptionFilter()
                    .withSubjectBeginsWith("ExamplePrefix")
                    .withSubjectEndsWith("ExampleSuffix")
                    .withIsSubjectCaseSensitive(false))
            .withDeadLetterDestination(
                new StorageBlobDeadLetterDestination()
                    .withResourceId(
                        "/subscriptions/55f3dcd4-cac7-43b4-990b-a139d62a1eb2/resourceGroups/TestRG/providers/Microsoft.Storage/storageAccounts/contosostg")
                    .withBlobContainerName("contosocontainer"))
            .create();
    }

    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_CreateOrUpdateForCustomTopic_StorageQueueDestination.json
     */
    /**
     * Sample code: EventSubscriptions_CreateOrUpdateForCustomTopic_StorageQueueDestination.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsCreateOrUpdateForCustomTopicStorageQueueDestination(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .define("examplesubscription1")
            .withExistingScope(
                "subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4/resourceGroups/examplerg/providers/Microsoft.EventGrid/topics/exampletopic1")
            .withDestination(
                new StorageQueueEventSubscriptionDestination()
                    .withResourceId(
                        "/subscriptions/d33c5f7a-02ea-40f4-bf52-07f17e84d6a8/resourceGroups/TestRG/providers/Microsoft.Storage/storageAccounts/contosostg")
                    .withQueueName("queue1"))
            .withFilter(
                new EventSubscriptionFilter()
                    .withSubjectBeginsWith("ExamplePrefix")
                    .withSubjectEndsWith("ExampleSuffix")
                    .withIsSubjectCaseSensitive(false))
            .withDeadLetterDestination(
                new StorageBlobDeadLetterDestination()
                    .withResourceId(
                        "/subscriptions/55f3dcd4-cac7-43b4-990b-a139d62a1eb2/resourceGroups/TestRG/providers/Microsoft.Storage/storageAccounts/contosostg")
                    .withBlobContainerName("contosocontainer"))
            .create();
    }

    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_CreateOrUpdateForCustomTopic_HybridConnectionDestination.json
     */
    /**
     * Sample code: EventSubscriptions_CreateOrUpdateForCustomTopic_HybridConnectionDestination.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsCreateOrUpdateForCustomTopicHybridConnectionDestination(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .define("examplesubscription1")
            .withExistingScope(
                "subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4/resourceGroups/examplerg/providers/Microsoft.EventGrid/topics/exampletopic1")
            .withDestination(
                new HybridConnectionEventSubscriptionDestination()
                    .withResourceId(
                        "/subscriptions/d33c5f7a-02ea-40f4-bf52-07f17e84d6a8/resourceGroups/TestRG/providers/Microsoft.Relay/namespaces/ContosoNamespace/hybridConnections/HC1"))
            .withFilter(
                new EventSubscriptionFilter()
                    .withSubjectBeginsWith("ExamplePrefix")
                    .withSubjectEndsWith("ExampleSuffix")
                    .withIsSubjectCaseSensitive(false))
            .withDeadLetterDestination(
                new StorageBlobDeadLetterDestination()
                    .withResourceId(
                        "/subscriptions/55f3dcd4-cac7-43b4-990b-a139d62a1eb2/resourceGroups/TestRG/providers/Microsoft.Storage/storageAccounts/contosostg")
                    .withBlobContainerName("contosocontainer"))
            .create();
    }
}
```

### EventSubscriptions_Delete

```java
import com.azure.core.util.Context;

/** Samples for EventSubscriptions Delete. */
public final class EventSubscriptionsDeleteSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_DeleteForCustomTopic.json
     */
    /**
     * Sample code: EventSubscriptions_DeleteForCustomTopic.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsDeleteForCustomTopic(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .delete(
                "subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4/resourceGroups/examplerg/providers/Microsoft.EventGrid/topics/exampletopic1",
                "examplesubscription1",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_DeleteForResourceGroup.json
     */
    /**
     * Sample code: EventSubscriptions_DeleteForResourceGroup.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsDeleteForResourceGroup(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .delete(
                "subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4/resourceGroups/examplerg",
                "examplesubscription2",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_DeleteForSubscription.json
     */
    /**
     * Sample code: EventSubscriptions_DeleteForSubscription.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsDeleteForSubscription(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .delete("subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4", "examplesubscription3", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_DeleteForResource.json
     */
    /**
     * Sample code: EventSubscriptions_DeleteForResource.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsDeleteForResource(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .delete(
                "subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4/resourceGroups/examplerg/providers/Microsoft.EventHub/namespaces/examplenamespace1",
                "examplesubscription10",
                Context.NONE);
    }
}
```

### EventSubscriptions_Get

```java
import com.azure.core.util.Context;

/** Samples for EventSubscriptions Get. */
public final class EventSubscriptionsGetSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_GetForResourceGroup.json
     */
    /**
     * Sample code: EventSubscriptions_GetForResourceGroup.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsGetForResourceGroup(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .getWithResponse(
                "subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4/resourceGroups/examplerg",
                "examplesubscription2",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_GetForSubscription.json
     */
    /**
     * Sample code: EventSubscriptions_GetForSubscription.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsGetForSubscription(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .getWithResponse(
                "subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4", "examplesubscription3", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_GetForCustomTopic.json
     */
    /**
     * Sample code: EventSubscriptions_GetForCustomTopic.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsGetForCustomTopic(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .getWithResponse(
                "subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4/resourceGroups/examplerg/providers/Microsoft.EventGrid/topics/exampletopic2",
                "examplesubscription1",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_GetForResource.json
     */
    /**
     * Sample code: EventSubscriptions_GetForResource.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsGetForResource(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .getWithResponse(
                "subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4/resourceGroups/examplerg/providers/Microsoft.EventHub/namespaces/examplenamespace1",
                "examplesubscription1",
                Context.NONE);
    }
}
```

### EventSubscriptions_GetDeliveryAttributes

```java
import com.azure.core.util.Context;

/** Samples for EventSubscriptions GetDeliveryAttributes. */
public final class EventSubscriptionsGetDeliveryAttributesSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_GetDeliveryAttributes.json
     */
    /**
     * Sample code: EventSubscriptions_GetDeliveryAttributes.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsGetDeliveryAttributes(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .getDeliveryAttributesWithResponse("aaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaa", Context.NONE);
    }
}
```

### EventSubscriptions_GetFullUrl

```java
import com.azure.core.util.Context;

/** Samples for EventSubscriptions GetFullUrl. */
public final class EventSubscriptionsGetFullUrlSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_GetFullUrlForResource.json
     */
    /**
     * Sample code: EventSubscriptions_GetFullUrlForResource.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsGetFullUrlForResource(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .getFullUrlWithResponse(
                "subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4/resourceGroups/examplerg/providers/Microsoft.EventHub/namespaces/examplenamespace1",
                "examplesubscription1",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_GetFullUrlForResourceGroup.json
     */
    /**
     * Sample code: EventSubscriptions_GetFullUrlForResourceGroup.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsGetFullUrlForResourceGroup(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .getFullUrlWithResponse(
                "subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4/resourceGroups/examplerg",
                "examplesubscription2",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_GetFullUrlForSubscription.json
     */
    /**
     * Sample code: EventSubscriptions_GetFullUrlForSubscription.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsGetFullUrlForSubscription(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .getFullUrlWithResponse(
                "subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4", "examplesubscription3", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_GetFullUrlForCustomTopic.json
     */
    /**
     * Sample code: EventSubscriptions_GetFullUrlForCustomTopic.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsGetFullUrlForCustomTopic(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .getFullUrlWithResponse(
                "subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4/resourceGroups/examplerg/providers/Microsoft.EventGrid/topics/exampletopic2",
                "examplesubscription1",
                Context.NONE);
    }
}
```

### EventSubscriptions_List

```java
import com.azure.core.util.Context;

/** Samples for EventSubscriptions List. */
public final class EventSubscriptionsListSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_ListGlobalBySubscription.json
     */
    /**
     * Sample code: EventSubscriptions_ListGlobalBySubscription.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsListGlobalBySubscription(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.eventSubscriptions().list(null, null, Context.NONE);
    }
}
```

### EventSubscriptions_ListByDomainTopic

```java
import com.azure.core.util.Context;

/** Samples for EventSubscriptions ListByDomainTopic. */
public final class EventSubscriptionsListByDomainTopicSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_ListByDomainTopic.json
     */
    /**
     * Sample code: EventSubscriptions_ListByDomainTopic.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsListByDomainTopic(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.eventSubscriptions().listByDomainTopic("examplerg", "domain1", "topic1", null, null, Context.NONE);
    }
}
```

### EventSubscriptions_ListByResource

```java
import com.azure.core.util.Context;

/** Samples for EventSubscriptions ListByResource. */
public final class EventSubscriptionsListByResourceSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_ListByResource.json
     */
    /**
     * Sample code: EventSubscriptions_ListByResource.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsListByResource(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .listByResource("examplerg", "Microsoft.EventGrid", "topics", "exampletopic2", null, null, Context.NONE);
    }
}
```

### EventSubscriptions_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for EventSubscriptions ListByResourceGroup. */
public final class EventSubscriptionsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_ListGlobalByResourceGroup.json
     */
    /**
     * Sample code: EventSubscriptions_ListGlobalByResourceGroup.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsListGlobalByResourceGroup(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.eventSubscriptions().listByResourceGroup("examplerg", null, null, Context.NONE);
    }
}
```

### EventSubscriptions_ListGlobalByResourceGroupForTopicType

```java
import com.azure.core.util.Context;

/** Samples for EventSubscriptions ListGlobalByResourceGroupForTopicType. */
public final class EventSubscriptionsListGlobalByResourceGroupForTopicTypeSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_ListGlobalByResourceGroupForTopicType.json
     */
    /**
     * Sample code: EventSubscriptions_ListGlobalByResourceGroupForTopicType.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsListGlobalByResourceGroupForTopicType(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .listGlobalByResourceGroupForTopicType(
                "examplerg", "Microsoft.Resources.ResourceGroups", null, null, Context.NONE);
    }
}
```

### EventSubscriptions_ListGlobalBySubscriptionForTopicType

```java
import com.azure.core.util.Context;

/** Samples for EventSubscriptions ListGlobalBySubscriptionForTopicType. */
public final class EventSubscriptionsListGlobalBySubscriptionForTopicTypeSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_ListGlobalBySubscriptionForTopicType.json
     */
    /**
     * Sample code: EventSubscriptions_ListGlobalBySubscriptionForTopicType.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsListGlobalBySubscriptionForTopicType(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .listGlobalBySubscriptionForTopicType("Microsoft.Resources.Subscriptions", null, null, Context.NONE);
    }
}
```

### EventSubscriptions_ListRegionalByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for EventSubscriptions ListRegionalByResourceGroup. */
public final class EventSubscriptionsListRegionalByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_ListRegionalByResourceGroup.json
     */
    /**
     * Sample code: EventSubscriptions_ListRegionalByResourceGroup.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsListRegionalByResourceGroup(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.eventSubscriptions().listRegionalByResourceGroup("examplerg", "westus2", null, null, Context.NONE);
    }
}
```

### EventSubscriptions_ListRegionalByResourceGroupForTopicType

```java
import com.azure.core.util.Context;

/** Samples for EventSubscriptions ListRegionalByResourceGroupForTopicType. */
public final class EventSubscriptionsListRegionalByResourceGroupForTopicTypeSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_ListRegionalByResourceGroupForTopicType.json
     */
    /**
     * Sample code: EventSubscriptions_ListRegionalByResourceGroupForTopicType.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsListRegionalByResourceGroupForTopicType(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .listRegionalByResourceGroupForTopicType(
                "examplerg", "westus2", "Microsoft.EventHub.namespaces", null, null, Context.NONE);
    }
}
```

### EventSubscriptions_ListRegionalBySubscription

```java
import com.azure.core.util.Context;

/** Samples for EventSubscriptions ListRegionalBySubscription. */
public final class EventSubscriptionsListRegionalBySubscriptionSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_ListRegionalBySubscription.json
     */
    /**
     * Sample code: EventSubscriptions_ListRegionalBySubscription.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsListRegionalBySubscription(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.eventSubscriptions().listRegionalBySubscription("westus2", null, null, Context.NONE);
    }
}
```

### EventSubscriptions_ListRegionalBySubscriptionForTopicType

```java
import com.azure.core.util.Context;

/** Samples for EventSubscriptions ListRegionalBySubscriptionForTopicType. */
public final class EventSubscriptionsListRegionalBySubscriptionForTopicTypeSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_ListRegionalBySubscriptionForTopicType.json
     */
    /**
     * Sample code: EventSubscriptions_ListRegionalBySubscriptionForTopicType.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsListRegionalBySubscriptionForTopicType(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .eventSubscriptions()
            .listRegionalBySubscriptionForTopicType(
                "westus2", "Microsoft.EventHub.namespaces", null, null, Context.NONE);
    }
}
```

### EventSubscriptions_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.eventgrid.models.EventHubEventSubscriptionDestination;
import com.azure.resourcemanager.eventgrid.models.EventSubscription;
import com.azure.resourcemanager.eventgrid.models.EventSubscriptionFilter;
import com.azure.resourcemanager.eventgrid.models.WebhookEventSubscriptionDestination;
import java.util.Arrays;

/** Samples for EventSubscriptions Update. */
public final class EventSubscriptionsUpdateSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_UpdateForCustomTopic.json
     */
    /**
     * Sample code: EventSubscriptions_UpdateForCustomTopic.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsUpdateForCustomTopic(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        EventSubscription resource =
            manager
                .eventSubscriptions()
                .getWithResponse(
                    "subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4/resourceGroups/examplerg/providers/Microsoft.EventGrid/topics/exampletopic2",
                    "examplesubscription1",
                    Context.NONE)
                .getValue();
        resource
            .update()
            .withDestination(new WebhookEventSubscriptionDestination().withEndpointUrl("https://requestb.in/15ksip71"))
            .withFilter(
                new EventSubscriptionFilter()
                    .withSubjectBeginsWith("existingPrefix")
                    .withSubjectEndsWith("newSuffix")
                    .withIsSubjectCaseSensitive(true))
            .withLabels(Arrays.asList("label1", "label2"))
            .apply();
    }

    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_UpdateForResource.json
     */
    /**
     * Sample code: EventSubscriptions_UpdateForResource.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsUpdateForResource(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        EventSubscription resource =
            manager
                .eventSubscriptions()
                .getWithResponse(
                    "subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4/resourceGroups/examplerg/providers/Microsoft.EventHub/namespaces/examplenamespace1",
                    "examplesubscription1",
                    Context.NONE)
                .getValue();
        resource
            .update()
            .withDestination(new WebhookEventSubscriptionDestination().withEndpointUrl("https://requestb.in/15ksip71"))
            .withFilter(
                new EventSubscriptionFilter()
                    .withSubjectBeginsWith("existingPrefix")
                    .withSubjectEndsWith("newSuffix")
                    .withIsSubjectCaseSensitive(true))
            .withLabels(Arrays.asList("label1", "label2"))
            .apply();
    }

    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_UpdateForResourceGroup.json
     */
    /**
     * Sample code: EventSubscriptions_UpdateForResourceGroup.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsUpdateForResourceGroup(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        EventSubscription resource =
            manager
                .eventSubscriptions()
                .getWithResponse(
                    "subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4/resourceGroups/examplerg",
                    "examplesubscription2",
                    Context.NONE)
                .getValue();
        resource
            .update()
            .withDestination(
                new EventHubEventSubscriptionDestination()
                    .withResourceId(
                        "/subscriptions/55f3dcd4-cac7-43b4-990b-a139d62a1eb2/resourceGroups/TestRG/providers/Microsoft.EventHub/namespaces/ContosoNamespace/eventhubs/EH1"))
            .withFilter(
                new EventSubscriptionFilter()
                    .withSubjectBeginsWith("existingPrefix")
                    .withSubjectEndsWith("newSuffix")
                    .withIsSubjectCaseSensitive(true))
            .withLabels(Arrays.asList("label1", "label2"))
            .apply();
    }

    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/EventSubscriptions_UpdateForSubscription.json
     */
    /**
     * Sample code: EventSubscriptions_UpdateForSubscription.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void eventSubscriptionsUpdateForSubscription(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        EventSubscription resource =
            manager
                .eventSubscriptions()
                .getWithResponse(
                    "subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4", "examplesubscription3", Context.NONE)
                .getValue();
        resource
            .update()
            .withDestination(new WebhookEventSubscriptionDestination().withEndpointUrl("https://requestb.in/15ksip71"))
            .withFilter(
                new EventSubscriptionFilter()
                    .withSubjectBeginsWith("existingPrefix")
                    .withSubjectEndsWith("newSuffix")
                    .withIsSubjectCaseSensitive(true))
            .withLabels(Arrays.asList("label1", "label2"))
            .apply();
    }
}
```

### ExtensionTopics_Get

```java
import com.azure.core.util.Context;

/** Samples for ExtensionTopics Get. */
public final class ExtensionTopicsGetSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/ExtensionTopics_Get.json
     */
    /**
     * Sample code: ExtensionTopics_Get.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void extensionTopicsGet(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .extensionTopics()
            .getWithResponse(
                "subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4/resourceGroups/examplerg/providers/microsoft.storage/storageaccounts/exampleResourceName/providers/Microsoft.eventgrid/extensionTopics/default",
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
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void operationsList(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### PrivateEndpointConnections_Delete

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.eventgrid.models.PrivateEndpointConnectionsParentType;

/** Samples for PrivateEndpointConnections Delete. */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/PrivateEndpointConnections_Delete.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Delete.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void privateEndpointConnectionsDelete(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .privateEndpointConnections()
            .delete(
                "examplerg",
                PrivateEndpointConnectionsParentType.TOPICS,
                "exampletopic1",
                "BMTPE5.8A30D251-4C61-489D-A1AA-B37C4A329B8B",
                Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.eventgrid.models.PrivateEndpointConnectionsParentType;

/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/PrivateEndpointConnections_Get.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Get.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void privateEndpointConnectionsGet(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse(
                "examplerg",
                PrivateEndpointConnectionsParentType.TOPICS,
                "exampletopic1",
                "BMTPE5.8A30D251-4C61-489D-A1AA-B37C4A329B8B",
                Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByResource

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.eventgrid.models.PrivateEndpointConnectionsParentType;

/** Samples for PrivateEndpointConnections ListByResource. */
public final class PrivateEndpointConnectionsListByResourceSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/PrivateEndpointConnections_ListByResource.json
     */
    /**
     * Sample code: PrivateEndpointConnections_ListByResource.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void privateEndpointConnectionsListByResource(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .privateEndpointConnections()
            .listByResource(
                "examplerg", PrivateEndpointConnectionsParentType.TOPICS, "exampletopic1", null, null, Context.NONE);
    }
}
```

### PrivateEndpointConnections_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.eventgrid.fluent.models.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.eventgrid.models.ConnectionState;
import com.azure.resourcemanager.eventgrid.models.PersistedConnectionStatus;
import com.azure.resourcemanager.eventgrid.models.PrivateEndpointConnectionsParentType;

/** Samples for PrivateEndpointConnections Update. */
public final class PrivateEndpointConnectionsUpdateSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/PrivateEndpointConnections_Update.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Update.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void privateEndpointConnectionsUpdate(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .privateEndpointConnections()
            .update(
                "examplerg",
                PrivateEndpointConnectionsParentType.TOPICS,
                "exampletopic1",
                "BMTPE5.8A30D251-4C61-489D-A1AA-B37C4A329B8B",
                new PrivateEndpointConnectionInner()
                    .withPrivateLinkServiceConnectionState(
                        new ConnectionState()
                            .withStatus(PersistedConnectionStatus.APPROVED)
                            .withDescription("approving connection")
                            .withActionsRequired("None")),
                Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources Get. */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/PrivateLinkResources_Get.json
     */
    /**
     * Sample code: PrivateLinkResources_Get.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void privateLinkResourcesGet(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.privateLinkResources().getWithResponse("examplerg", "topics", "exampletopic1", "topic", Context.NONE);
    }
}
```

### PrivateLinkResources_ListByResource

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources ListByResource. */
public final class PrivateLinkResourcesListByResourceSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/PrivateLinkResources_ListByResource.json
     */
    /**
     * Sample code: PrivateLinkResources_ListByResource.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void privateLinkResourcesListByResource(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.privateLinkResources().listByResource("examplerg", "topics", "exampletopic1", null, null, Context.NONE);
    }
}
```

### SystemTopicEventSubscriptions_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.eventgrid.fluent.models.EventSubscriptionInner;
import com.azure.resourcemanager.eventgrid.models.EventSubscriptionFilter;
import com.azure.resourcemanager.eventgrid.models.WebhookEventSubscriptionDestination;

/** Samples for SystemTopicEventSubscriptions CreateOrUpdate. */
public final class SystemTopicEventSubscriptionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/SystemTopicEventSubscriptions_CreateOrUpdate.json
     */
    /**
     * Sample code: SystemTopicEventSubscriptions_CreateOrUpdate.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void systemTopicEventSubscriptionsCreateOrUpdate(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .systemTopicEventSubscriptions()
            .createOrUpdate(
                "examplerg",
                "exampleSystemTopic1",
                "exampleEventSubscriptionName1",
                new EventSubscriptionInner()
                    .withDestination(
                        new WebhookEventSubscriptionDestination().withEndpointUrl("https://requestb.in/15ksip71"))
                    .withFilter(
                        new EventSubscriptionFilter()
                            .withSubjectBeginsWith("ExamplePrefix")
                            .withSubjectEndsWith("ExampleSuffix")
                            .withIsSubjectCaseSensitive(false)),
                Context.NONE);
    }
}
```

### SystemTopicEventSubscriptions_Delete

```java
import com.azure.core.util.Context;

/** Samples for SystemTopicEventSubscriptions Delete. */
public final class SystemTopicEventSubscriptionsDeleteSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/SystemTopicEventSubscriptions_Delete.json
     */
    /**
     * Sample code: SystemTopicEventSubscriptions_Delete.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void systemTopicEventSubscriptionsDelete(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .systemTopicEventSubscriptions()
            .delete("examplerg", "exampleSystemTopic1", "examplesubscription1", Context.NONE);
    }
}
```

### SystemTopicEventSubscriptions_Get

```java
import com.azure.core.util.Context;

/** Samples for SystemTopicEventSubscriptions Get. */
public final class SystemTopicEventSubscriptionsGetSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/SystemTopicEventSubscriptions_Get.json
     */
    /**
     * Sample code: SystemTopicEventSubscriptions_Get.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void systemTopicEventSubscriptionsGet(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .systemTopicEventSubscriptions()
            .getWithResponse("examplerg", "exampleSystemTopic1", "examplesubscription1", Context.NONE);
    }
}
```

### SystemTopicEventSubscriptions_GetDeliveryAttributes

```java
import com.azure.core.util.Context;

/** Samples for SystemTopicEventSubscriptions GetDeliveryAttributes. */
public final class SystemTopicEventSubscriptionsGetDeliveryAttributesSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/SystemTopicEventSubscriptions_GetDeliveryAttributes.json
     */
    /**
     * Sample code: SystemTopicEventSubscriptions_GetDeliveryAttributes.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void systemTopicEventSubscriptionsGetDeliveryAttributes(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .systemTopicEventSubscriptions()
            .getDeliveryAttributesWithResponse(
                "examplerg", "exampleSystemTopic1", "examplesubscription1", Context.NONE);
    }
}
```

### SystemTopicEventSubscriptions_GetFullUrl

```java
import com.azure.core.util.Context;

/** Samples for SystemTopicEventSubscriptions GetFullUrl. */
public final class SystemTopicEventSubscriptionsGetFullUrlSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/SystemTopicEventSubscriptions_GetFullUrl.json
     */
    /**
     * Sample code: SystemTopicEventSubscriptions_GetFullUrl.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void systemTopicEventSubscriptionsGetFullUrl(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .systemTopicEventSubscriptions()
            .getFullUrlWithResponse("examplerg", "exampleSystemTopic1", "examplesubscription1", Context.NONE);
    }
}
```

### SystemTopicEventSubscriptions_ListBySystemTopic

```java
import com.azure.core.util.Context;

/** Samples for SystemTopicEventSubscriptions ListBySystemTopic. */
public final class SystemTopicEventSubscriptionsListBySystemTopicSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/SystemTopicEventSubscriptions_ListBySystemTopic.json
     */
    /**
     * Sample code: SystemTopicEventSubscriptions_ListBySystemTopic.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void systemTopicEventSubscriptionsListBySystemTopic(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .systemTopicEventSubscriptions()
            .listBySystemTopic("examplerg", "exampleSystemTopic1", null, null, Context.NONE);
    }
}
```

### SystemTopicEventSubscriptions_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.eventgrid.models.EventSubscriptionFilter;
import com.azure.resourcemanager.eventgrid.models.EventSubscriptionUpdateParameters;
import com.azure.resourcemanager.eventgrid.models.WebhookEventSubscriptionDestination;
import java.util.Arrays;

/** Samples for SystemTopicEventSubscriptions Update. */
public final class SystemTopicEventSubscriptionsUpdateSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/SystemTopicEventSubscriptions_Update.json
     */
    /**
     * Sample code: SystemTopicEventSubscriptions_Update.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void systemTopicEventSubscriptionsUpdate(
        com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .systemTopicEventSubscriptions()
            .update(
                "examplerg",
                "exampleSystemTopic1",
                "exampleEventSubscriptionName1",
                new EventSubscriptionUpdateParameters()
                    .withDestination(
                        new WebhookEventSubscriptionDestination().withEndpointUrl("https://requestb.in/15ksip71"))
                    .withFilter(
                        new EventSubscriptionFilter()
                            .withSubjectBeginsWith("existingPrefix")
                            .withSubjectEndsWith("newSuffix")
                            .withIsSubjectCaseSensitive(true))
                    .withLabels(Arrays.asList("label1", "label2")),
                Context.NONE);
    }
}
```

### SystemTopics_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for SystemTopics CreateOrUpdate. */
public final class SystemTopicsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/SystemTopics_CreateOrUpdate.json
     */
    /**
     * Sample code: SystemTopics_CreateOrUpdate.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void systemTopicsCreateOrUpdate(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .systemTopics()
            .define("exampleSystemTopic1")
            .withRegion("westus2")
            .withExistingResourceGroup("examplerg")
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
            .withSource(
                "/subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4/resourceGroups/azureeventgridrunnerrgcentraluseuap/providers/microsoft.storage/storageaccounts/pubstgrunnerb71cd29e")
            .withTopicType("microsoft.storage.storageaccounts")
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

### SystemTopics_Delete

```java
import com.azure.core.util.Context;

/** Samples for SystemTopics Delete. */
public final class SystemTopicsDeleteSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/SystemTopics_Delete.json
     */
    /**
     * Sample code: SystemTopics_Delete.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void systemTopicsDelete(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.systemTopics().delete("examplerg", "exampleSystemTopic1", Context.NONE);
    }
}
```

### SystemTopics_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for SystemTopics GetByResourceGroup. */
public final class SystemTopicsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/SystemTopics_Get.json
     */
    /**
     * Sample code: SystemTopics_Get.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void systemTopicsGet(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.systemTopics().getByResourceGroupWithResponse("examplerg", "exampleSystemTopic2", Context.NONE);
    }
}
```

### SystemTopics_List

```java
import com.azure.core.util.Context;

/** Samples for SystemTopics List. */
public final class SystemTopicsListSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/SystemTopics_ListBySubscription.json
     */
    /**
     * Sample code: SystemTopics_ListBySubscription.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void systemTopicsListBySubscription(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.systemTopics().list(null, null, Context.NONE);
    }
}
```

### SystemTopics_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for SystemTopics ListByResourceGroup. */
public final class SystemTopicsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/SystemTopics_ListByResourceGroup.json
     */
    /**
     * Sample code: SystemTopics_ListByResourceGroup.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void systemTopicsListByResourceGroup(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.systemTopics().listByResourceGroup("examplerg", null, null, Context.NONE);
    }
}
```

### SystemTopics_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.eventgrid.models.SystemTopic;
import java.util.HashMap;
import java.util.Map;

/** Samples for SystemTopics Update. */
public final class SystemTopicsUpdateSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/SystemTopics_Update.json
     */
    /**
     * Sample code: SystemTopics_Update.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void systemTopicsUpdate(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        SystemTopic resource =
            manager
                .systemTopics()
                .getByResourceGroupWithResponse("examplerg", "exampleSystemTopic1", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### TopicTypes_Get

```java
import com.azure.core.util.Context;

/** Samples for TopicTypes Get. */
public final class TopicTypesGetSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/TopicTypes_Get.json
     */
    /**
     * Sample code: TopicTypes_Get.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void topicTypesGet(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.topicTypes().getWithResponse("Microsoft.Storage.StorageAccounts", Context.NONE);
    }
}
```

### TopicTypes_List

```java
import com.azure.core.util.Context;

/** Samples for TopicTypes List. */
public final class TopicTypesListSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/TopicTypes_List.json
     */
    /**
     * Sample code: TopicTypes_List.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void topicTypesList(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.topicTypes().list(Context.NONE);
    }
}
```

### TopicTypes_ListEventTypes

```java
import com.azure.core.util.Context;

/** Samples for TopicTypes ListEventTypes. */
public final class TopicTypesListEventTypesSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/TopicTypes_ListEventTypes.json
     */
    /**
     * Sample code: TopicTypes_ListEventTypes.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void topicTypesListEventTypes(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.topicTypes().listEventTypes("Microsoft.Storage.StorageAccounts", Context.NONE);
    }
}
```

### Topics_CreateOrUpdate

```java
import com.azure.resourcemanager.eventgrid.models.InboundIpRule;
import com.azure.resourcemanager.eventgrid.models.IpActionType;
import com.azure.resourcemanager.eventgrid.models.PublicNetworkAccess;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Topics CreateOrUpdate. */
public final class TopicsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/Topics_CreateOrUpdate.json
     */
    /**
     * Sample code: Topics_CreateOrUpdate.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void topicsCreateOrUpdate(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .topics()
            .define("exampletopic1")
            .withRegion("westus2")
            .withExistingResourceGroup("examplerg")
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
            .withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
            .withInboundIpRules(
                Arrays
                    .asList(
                        new InboundIpRule().withIpMask("12.18.30.15").withAction(IpActionType.ALLOW),
                        new InboundIpRule().withIpMask("12.18.176.1").withAction(IpActionType.ALLOW)))
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

### Topics_Delete

```java
import com.azure.core.util.Context;

/** Samples for Topics Delete. */
public final class TopicsDeleteSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/Topics_Delete.json
     */
    /**
     * Sample code: Topics_Delete.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void topicsDelete(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.topics().delete("examplerg", "exampletopic1", Context.NONE);
    }
}
```

### Topics_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Topics GetByResourceGroup. */
public final class TopicsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/Topics_Get.json
     */
    /**
     * Sample code: Topics_Get.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void topicsGet(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.topics().getByResourceGroupWithResponse("examplerg", "exampletopic2", Context.NONE);
    }
}
```

### Topics_List

```java
import com.azure.core.util.Context;

/** Samples for Topics List. */
public final class TopicsListSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/Topics_ListBySubscription.json
     */
    /**
     * Sample code: Topics_ListBySubscription.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void topicsListBySubscription(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.topics().list(null, null, Context.NONE);
    }
}
```

### Topics_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Topics ListByResourceGroup. */
public final class TopicsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/Topics_ListByResourceGroup.json
     */
    /**
     * Sample code: Topics_ListByResourceGroup.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void topicsListByResourceGroup(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.topics().listByResourceGroup("examplerg", null, null, Context.NONE);
    }
}
```

### Topics_ListEventTypes

```java
import com.azure.core.util.Context;

/** Samples for Topics ListEventTypes. */
public final class TopicsListEventTypesSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/Topics_ListEventTypes.json
     */
    /**
     * Sample code: Topics_ListEventTypes.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void topicsListEventTypes(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .topics()
            .listEventTypes("examplerg", "Microsoft.Storage", "storageAccounts", "ExampleStorageAccount", Context.NONE);
    }
}
```

### Topics_ListSharedAccessKeys

```java
import com.azure.core.util.Context;

/** Samples for Topics ListSharedAccessKeys. */
public final class TopicsListSharedAccessKeysSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/Topics_ListSharedAccessKeys.json
     */
    /**
     * Sample code: Topics_ListSharedAccessKeys.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void topicsListSharedAccessKeys(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.topics().listSharedAccessKeysWithResponse("examplerg", "exampletopic2", Context.NONE);
    }
}
```

### Topics_RegenerateKey

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.eventgrid.models.TopicRegenerateKeyRequest;

/** Samples for Topics RegenerateKey. */
public final class TopicsRegenerateKeySamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/Topics_RegenerateKey.json
     */
    /**
     * Sample code: Topics_RegenerateKey.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void topicsRegenerateKey(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager
            .topics()
            .regenerateKey(
                "examplerg", "exampletopic2", new TopicRegenerateKeyRequest().withKeyName("key1"), Context.NONE);
    }
}
```

### Topics_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.eventgrid.models.InboundIpRule;
import com.azure.resourcemanager.eventgrid.models.IpActionType;
import com.azure.resourcemanager.eventgrid.models.PublicNetworkAccess;
import com.azure.resourcemanager.eventgrid.models.Topic;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Topics Update. */
public final class TopicsUpdateSamples {
    /*
     * x-ms-original-file: specification/eventgrid/resource-manager/Microsoft.EventGrid/stable/2021-12-01/examples/Topics_Update.json
     */
    /**
     * Sample code: Topics_Update.
     *
     * @param manager Entry point to EventGridManager.
     */
    public static void topicsUpdate(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        Topic resource =
            manager.topics().getByResourceGroupWithResponse("examplerg", "exampletopic1", Context.NONE).getValue();
        resource
            .update()
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
            .withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
            .withInboundIpRules(
                Arrays
                    .asList(
                        new InboundIpRule().withIpMask("12.18.30.15").withAction(IpActionType.ALLOW),
                        new InboundIpRule().withIpMask("12.18.176.1").withAction(IpActionType.ALLOW)))
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

