# Code snippets and samples


## Clusters

- [CreateOrUpdate](#clusters_createorupdate)
- [Delete](#clusters_delete)
- [GetByResourceGroup](#clusters_getbyresourcegroup)
- [List](#clusters_list)
- [ListAvailableClusterRegion](#clusters_listavailableclusterregion)
- [ListByResourceGroup](#clusters_listbyresourcegroup)
- [ListNamespaces](#clusters_listnamespaces)
- [Update](#clusters_update)

## Configuration

- [Get](#configuration_get)
- [Patch](#configuration_patch)

## ConsumerGroups

- [CreateOrUpdate](#consumergroups_createorupdate)
- [Delete](#consumergroups_delete)
- [Get](#consumergroups_get)
- [ListByEventHub](#consumergroups_listbyeventhub)

## DisasterRecoveryConfigs

- [BreakPairing](#disasterrecoveryconfigs_breakpairing)
- [CheckNameAvailability](#disasterrecoveryconfigs_checknameavailability)
- [CreateOrUpdate](#disasterrecoveryconfigs_createorupdate)
- [Delete](#disasterrecoveryconfigs_delete)
- [FailOver](#disasterrecoveryconfigs_failover)
- [Get](#disasterrecoveryconfigs_get)
- [GetAuthorizationRule](#disasterrecoveryconfigs_getauthorizationrule)
- [List](#disasterrecoveryconfigs_list)
- [ListAuthorizationRules](#disasterrecoveryconfigs_listauthorizationrules)
- [ListKeys](#disasterrecoveryconfigs_listkeys)

## EventHubs

- [CreateOrUpdate](#eventhubs_createorupdate)
- [CreateOrUpdateAuthorizationRule](#eventhubs_createorupdateauthorizationrule)
- [Delete](#eventhubs_delete)
- [DeleteAuthorizationRule](#eventhubs_deleteauthorizationrule)
- [Get](#eventhubs_get)
- [GetAuthorizationRule](#eventhubs_getauthorizationrule)
- [ListAuthorizationRules](#eventhubs_listauthorizationrules)
- [ListByNamespace](#eventhubs_listbynamespace)
- [ListKeys](#eventhubs_listkeys)
- [RegenerateKeys](#eventhubs_regeneratekeys)

## Namespaces

- [CheckNameAvailability](#namespaces_checknameavailability)
- [CreateOrUpdate](#namespaces_createorupdate)
- [CreateOrUpdateAuthorizationRule](#namespaces_createorupdateauthorizationrule)
- [CreateOrUpdateNetworkRuleSet](#namespaces_createorupdatenetworkruleset)
- [Delete](#namespaces_delete)
- [DeleteAuthorizationRule](#namespaces_deleteauthorizationrule)
- [GetAuthorizationRule](#namespaces_getauthorizationrule)
- [GetByResourceGroup](#namespaces_getbyresourcegroup)
- [GetNetworkRuleSet](#namespaces_getnetworkruleset)
- [List](#namespaces_list)
- [ListAuthorizationRules](#namespaces_listauthorizationrules)
- [ListByResourceGroup](#namespaces_listbyresourcegroup)
- [ListKeys](#namespaces_listkeys)
- [ListNetworkRuleSet](#namespaces_listnetworkruleset)
- [RegenerateKeys](#namespaces_regeneratekeys)
- [Update](#namespaces_update)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [List](#privateendpointconnections_list)

## PrivateLinkResources

- [Get](#privatelinkresources_get)

## SchemaRegistry

- [CreateOrUpdate](#schemaregistry_createorupdate)
- [Delete](#schemaregistry_delete)
- [Get](#schemaregistry_get)
- [ListByNamespace](#schemaregistry_listbynamespace)
### Clusters_CreateOrUpdate

```java
import com.azure.resourcemanager.eventhubs.generated.models.ClusterSku;
import com.azure.resourcemanager.eventhubs.generated.models.ClusterSkuName;
import java.util.HashMap;
import java.util.Map;

/** Samples for Clusters CreateOrUpdate. */
public final class ClustersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/Clusters/ClusterPut.json
     */
    /**
     * Sample code: ClusterPut.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void clusterPut(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .clusters()
            .define("testCluster")
            .withRegion("South Central US")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
            .withSku(new ClusterSku().withName(ClusterSkuName.DEDICATED).withCapacity(1))
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

### Clusters_Delete

```java
import com.azure.core.util.Context;

/** Samples for Clusters Delete. */
public final class ClustersDeleteSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/Clusters/ClusterDelete.json
     */
    /**
     * Sample code: ClusterDelete.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void clusterDelete(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager.clusters().delete("myResourceGroup", "testCluster", Context.NONE);
    }
}
```

### Clusters_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Clusters GetByResourceGroup. */
public final class ClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/Clusters/ClusterGet.json
     */
    /**
     * Sample code: ClusterGet.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void clusterGet(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager.clusters().getByResourceGroupWithResponse("myResourceGroup", "testCluster", Context.NONE);
    }
}
```

### Clusters_List

```java
import com.azure.core.util.Context;

/** Samples for Clusters List. */
public final class ClustersListSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/Clusters/ClustersListBySubscription.json
     */
    /**
     * Sample code: ClustersListBySubscription.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void clustersListBySubscription(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager.clusters().list(Context.NONE);
    }
}
```

### Clusters_ListAvailableClusterRegion

```java
import com.azure.core.util.Context;

/** Samples for Clusters ListAvailableClusterRegion. */
public final class ClustersListAvailableClusterRegionSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/Clusters/ListAvailableClustersGet.json
     */
    /**
     * Sample code: ListAvailableClusters.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void listAvailableClusters(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager.clusters().listAvailableClusterRegionWithResponse(Context.NONE);
    }
}
```

### Clusters_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Clusters ListByResourceGroup. */
public final class ClustersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/Clusters/ClustersListByResourceGroup.json
     */
    /**
     * Sample code: ClustersListByResourceGroup.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void clustersListByResourceGroup(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager.clusters().listByResourceGroup("myResourceGroup", Context.NONE);
    }
}
```

### Clusters_ListNamespaces

```java
import com.azure.core.util.Context;

/** Samples for Clusters ListNamespaces. */
public final class ClustersListNamespacesSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/Clusters/ListNamespacesInClusterGet.json
     */
    /**
     * Sample code: ListNamespacesInCluster.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void listNamespacesInCluster(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager.clusters().listNamespacesWithResponse("myResourceGroup", "testCluster", Context.NONE);
    }
}
```

### Clusters_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.eventhubs.generated.models.Cluster;
import java.util.HashMap;
import java.util.Map;

/** Samples for Clusters Update. */
public final class ClustersUpdateSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/Clusters/ClusterPatch.json
     */
    /**
     * Sample code: ClusterPatch.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void clusterPatch(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        Cluster resource =
            manager
                .clusters()
                .getByResourceGroupWithResponse("myResourceGroup", "testCluster", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag3", "value3", "tag4", "value4")).apply();
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

### Configuration_Get

```java
import com.azure.core.util.Context;

/** Samples for Configuration Get. */
public final class ConfigurationGetSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/Clusters/ClusterQuotaConfigurationGet.json
     */
    /**
     * Sample code: ClustersQuotasConfigurationGet.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void clustersQuotasConfigurationGet(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager.configurations().getWithResponse("myResourceGroup", "testCluster", Context.NONE);
    }
}
```

### Configuration_Patch

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.eventhubs.generated.fluent.models.ClusterQuotaConfigurationPropertiesInner;
import java.util.HashMap;
import java.util.Map;

/** Samples for Configuration Patch. */
public final class ConfigurationPatchSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/Clusters/ClusterQuotaConfigurationPatch.json
     */
    /**
     * Sample code: ClustersQuotasConfigurationPatch.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void clustersQuotasConfigurationPatch(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .configurations()
            .patchWithResponse(
                "ArunMonocle",
                "testCluster",
                new ClusterQuotaConfigurationPropertiesInner()
                    .withSettings(mapOf("eventhub-per-namespace-quota", "20", "namespaces-per-cluster-quota", "200")),
                Context.NONE);
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

### ConsumerGroups_CreateOrUpdate

```java
/** Samples for ConsumerGroups CreateOrUpdate. */
public final class ConsumerGroupsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/ConsumerGroup/EHConsumerGroupCreate.json
     */
    /**
     * Sample code: ConsumerGroupCreate.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void consumerGroupCreate(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .consumerGroups()
            .define("sdk-ConsumerGroup-5563")
            .withExistingEventhub("ArunMonocle", "sdk-Namespace-2661", "sdk-EventHub-6681")
            .withUserMetadata("New consumergroup")
            .create();
    }
}
```

### ConsumerGroups_Delete

```java
import com.azure.core.util.Context;

/** Samples for ConsumerGroups Delete. */
public final class ConsumerGroupsDeleteSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/ConsumerGroup/EHConsumerGroupDelete.json
     */
    /**
     * Sample code: ConsumerGroupDelete.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void consumerGroupDelete(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .consumerGroups()
            .deleteWithResponse(
                "ArunMonocle", "sdk-Namespace-2661", "sdk-EventHub-6681", "sdk-ConsumerGroup-5563", Context.NONE);
    }
}
```

### ConsumerGroups_Get

```java
import com.azure.core.util.Context;

/** Samples for ConsumerGroups Get. */
public final class ConsumerGroupsGetSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/ConsumerGroup/EHConsumerGroupGet.json
     */
    /**
     * Sample code: ConsumerGroupGet.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void consumerGroupGet(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .consumerGroups()
            .getWithResponse(
                "ArunMonocle", "sdk-Namespace-2661", "sdk-EventHub-6681", "sdk-ConsumerGroup-5563", Context.NONE);
    }
}
```

### ConsumerGroups_ListByEventHub

```java
import com.azure.core.util.Context;

/** Samples for ConsumerGroups ListByEventHub. */
public final class ConsumerGroupsListByEventHubSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/ConsumerGroup/EHConsumerGroupListByEventHub.json
     */
    /**
     * Sample code: ConsumerGroupsListAll.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void consumerGroupsListAll(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .consumerGroups()
            .listByEventHub("ArunMonocle", "sdk-Namespace-2661", "sdk-EventHub-6681", null, null, Context.NONE);
    }
}
```

### DisasterRecoveryConfigs_BreakPairing

```java
import com.azure.core.util.Context;

/** Samples for DisasterRecoveryConfigs BreakPairing. */
public final class DisasterRecoveryConfigsBreakPairingSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/disasterRecoveryConfigs/EHAliasBreakPairing.json
     */
    /**
     * Sample code: EHAliasBreakPairing.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void eHAliasBreakPairing(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .disasterRecoveryConfigs()
            .breakPairingWithResponse(
                "exampleResourceGroup", "sdk-Namespace-8859", "sdk-DisasterRecovery-3814", Context.NONE);
    }
}
```

### DisasterRecoveryConfigs_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.eventhubs.generated.models.CheckNameAvailabilityParameter;

/** Samples for DisasterRecoveryConfigs CheckNameAvailability. */
public final class DisasterRecoveryConfigsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/disasterRecoveryConfigs/EHAliasCheckNameAvailability.json
     */
    /**
     * Sample code: NamespacesCheckNameAvailability.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void namespacesCheckNameAvailability(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .disasterRecoveryConfigs()
            .checkNameAvailabilityWithResponse(
                "exampleResourceGroup",
                "sdk-Namespace-9080",
                new CheckNameAvailabilityParameter().withName("sdk-DisasterRecovery-9474"),
                Context.NONE);
    }
}
```

### DisasterRecoveryConfigs_CreateOrUpdate

```java
/** Samples for DisasterRecoveryConfigs CreateOrUpdate. */
public final class DisasterRecoveryConfigsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/disasterRecoveryConfigs/EHAliasCreate.json
     */
    /**
     * Sample code: EHAliasCreate.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void eHAliasCreate(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .disasterRecoveryConfigs()
            .define("sdk-DisasterRecovery-3814")
            .withExistingNamespace("exampleResourceGroup", "sdk-Namespace-8859")
            .withPartnerNamespace("sdk-Namespace-37")
            .create();
    }
}
```

### DisasterRecoveryConfigs_Delete

```java
import com.azure.core.util.Context;

/** Samples for DisasterRecoveryConfigs Delete. */
public final class DisasterRecoveryConfigsDeleteSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/disasterRecoveryConfigs/EHAliasDelete.json
     */
    /**
     * Sample code: EHAliasDelete.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void eHAliasDelete(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .disasterRecoveryConfigs()
            .deleteWithResponse(
                "exampleResourceGroup", "sdk-Namespace-5849", "sdk-DisasterRecovery-3814", Context.NONE);
    }
}
```

### DisasterRecoveryConfigs_FailOver

```java
import com.azure.core.util.Context;

/** Samples for DisasterRecoveryConfigs FailOver. */
public final class DisasterRecoveryConfigsFailOverSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/disasterRecoveryConfigs/EHAliasFailOver.json
     */
    /**
     * Sample code: EHAliasFailOver.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void eHAliasFailOver(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .disasterRecoveryConfigs()
            .failOverWithResponse(
                "exampleResourceGroup", "sdk-Namespace-8859", "sdk-DisasterRecovery-3814", Context.NONE);
    }
}
```

### DisasterRecoveryConfigs_Get

```java
import com.azure.core.util.Context;

/** Samples for DisasterRecoveryConfigs Get. */
public final class DisasterRecoveryConfigsGetSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/disasterRecoveryConfigs/EHAliasGet.json
     */
    /**
     * Sample code: EHAliasGet.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void eHAliasGet(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .disasterRecoveryConfigs()
            .getWithResponse("exampleResourceGroup", "sdk-Namespace-8859", "sdk-DisasterRecovery-3814", Context.NONE);
    }
}
```

### DisasterRecoveryConfigs_GetAuthorizationRule

```java
import com.azure.core.util.Context;

/** Samples for DisasterRecoveryConfigs GetAuthorizationRule. */
public final class DisasterRecoveryConfigsGetAuthorizationRuleSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/disasterRecoveryConfigs/EHAliasAuthorizationRuleGet.json
     */
    /**
     * Sample code: NameSpaceAuthorizationRuleGet.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void nameSpaceAuthorizationRuleGet(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .disasterRecoveryConfigs()
            .getAuthorizationRuleWithResponse(
                "exampleResourceGroup",
                "sdk-Namespace-9080",
                "sdk-DisasterRecovery-4879",
                "sdk-Authrules-4879",
                Context.NONE);
    }
}
```

### DisasterRecoveryConfigs_List

```java
import com.azure.core.util.Context;

/** Samples for DisasterRecoveryConfigs List. */
public final class DisasterRecoveryConfigsListSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/disasterRecoveryConfigs/EHAliasList.json
     */
    /**
     * Sample code: EHAliasList.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void eHAliasList(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager.disasterRecoveryConfigs().list("exampleResourceGroup", "sdk-Namespace-8859", Context.NONE);
    }
}
```

### DisasterRecoveryConfigs_ListAuthorizationRules

```java
import com.azure.core.util.Context;

/** Samples for DisasterRecoveryConfigs ListAuthorizationRules. */
public final class DisasterRecoveryConfigsListAuthorizationRulesSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/disasterRecoveryConfigs/EHAliasAuthorizationRuleListAll.json
     */
    /**
     * Sample code: ListAuthorizationRules.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void listAuthorizationRules(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .disasterRecoveryConfigs()
            .listAuthorizationRules(
                "exampleResourceGroup", "sdk-Namespace-9080", "sdk-DisasterRecovery-4047", Context.NONE);
    }
}
```

### DisasterRecoveryConfigs_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for DisasterRecoveryConfigs ListKeys. */
public final class DisasterRecoveryConfigsListKeysSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/disasterRecoveryConfigs/EHAliasAuthorizationRuleListKey.json
     */
    /**
     * Sample code: NameSpaceAuthorizationRuleListKey.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void nameSpaceAuthorizationRuleListKey(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .disasterRecoveryConfigs()
            .listKeysWithResponse(
                "exampleResourceGroup",
                "sdk-Namespace-2702",
                "sdk-DisasterRecovery-4047",
                "sdk-Authrules-1746",
                Context.NONE);
    }
}
```

### EventHubs_CreateOrUpdate

```java
import com.azure.resourcemanager.eventhubs.generated.models.CaptureDescription;
import com.azure.resourcemanager.eventhubs.generated.models.Destination;
import com.azure.resourcemanager.eventhubs.generated.models.EncodingCaptureDescription;
import com.azure.resourcemanager.eventhubs.generated.models.EntityStatus;

/** Samples for EventHubs CreateOrUpdate. */
public final class EventHubsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/EventHubs/EHEventHubCreate.json
     */
    /**
     * Sample code: EventHubCreate.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void eventHubCreate(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .eventHubs()
            .define("sdk-EventHub-6547")
            .withExistingNamespace("Default-NotificationHubs-AustraliaEast", "sdk-Namespace-5357")
            .withMessageRetentionInDays(4L)
            .withPartitionCount(4L)
            .withStatus(EntityStatus.ACTIVE)
            .withCaptureDescription(
                new CaptureDescription()
                    .withEnabled(true)
                    .withEncoding(EncodingCaptureDescription.AVRO)
                    .withIntervalInSeconds(120)
                    .withSizeLimitInBytes(10485763)
                    .withDestination(
                        new Destination()
                            .withName("EventHubArchive.AzureBlockBlob")
                            .withStorageAccountResourceId(
                                "/subscriptions/e2f361f0-3b27-4503-a9cc-21cfba380093/resourceGroups/Default-Storage-SouthCentralUS/providers/Microsoft.ClassicStorage/storageAccounts/arjunteststorage")
                            .withBlobContainer("container")
                            .withArchiveNameFormat(
                                "{Namespace}/{EventHub}/{PartitionId}/{Year}/{Month}/{Day}/{Hour}/{Minute}/{Second}")))
            .create();
    }
}
```

### EventHubs_CreateOrUpdateAuthorizationRule

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.eventhubs.generated.fluent.models.AuthorizationRuleInner;
import com.azure.resourcemanager.eventhubs.generated.models.AccessRights;
import java.util.Arrays;

/** Samples for EventHubs CreateOrUpdateAuthorizationRule. */
public final class EventHubsCreateOrUpdateAuthorizationRuleSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/EventHubs/EHEventHubAuthorizationRuleCreate.json
     */
    /**
     * Sample code: EventHubAuthorizationRuleCreate.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void eventHubAuthorizationRuleCreate(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .eventHubs()
            .createOrUpdateAuthorizationRuleWithResponse(
                "ArunMonocle",
                "sdk-Namespace-960",
                "sdk-EventHub-532",
                "sdk-Authrules-2513",
                new AuthorizationRuleInner().withRights(Arrays.asList(AccessRights.LISTEN, AccessRights.SEND)),
                Context.NONE);
    }
}
```

### EventHubs_Delete

```java
import com.azure.core.util.Context;

/** Samples for EventHubs Delete. */
public final class EventHubsDeleteSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/EventHubs/EHEventHubDelete.json
     */
    /**
     * Sample code: EventHubDelete.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void eventHubDelete(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager.eventHubs().deleteWithResponse("ArunMonocle", "sdk-Namespace-5357", "sdk-EventHub-6547", Context.NONE);
    }
}
```

### EventHubs_DeleteAuthorizationRule

```java
import com.azure.core.util.Context;

/** Samples for EventHubs DeleteAuthorizationRule. */
public final class EventHubsDeleteAuthorizationRuleSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/EventHubs/EHEventHubAuthorizationRuleDelete.json
     */
    /**
     * Sample code: EventHubAuthorizationRuleDelete.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void eventHubAuthorizationRuleDelete(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .eventHubs()
            .deleteAuthorizationRuleWithResponse(
                "ArunMonocle", "sdk-Namespace-960", "sdk-EventHub-532", "sdk-Authrules-2513", Context.NONE);
    }
}
```

### EventHubs_Get

```java
import com.azure.core.util.Context;

/** Samples for EventHubs Get. */
public final class EventHubsGetSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/EventHubs/EHEventHubGet.json
     */
    /**
     * Sample code: EventHubGet.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void eventHubGet(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .eventHubs()
            .getWithResponse(
                "Default-NotificationHubs-AustraliaEast", "sdk-Namespace-716", "sdk-EventHub-10", Context.NONE);
    }
}
```

### EventHubs_GetAuthorizationRule

```java
import com.azure.core.util.Context;

/** Samples for EventHubs GetAuthorizationRule. */
public final class EventHubsGetAuthorizationRuleSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/EventHubs/EHEventHubAuthorizationRuleGet.json
     */
    /**
     * Sample code: EventHubAuthorizationRuleGet.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void eventHubAuthorizationRuleGet(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .eventHubs()
            .getAuthorizationRuleWithResponse(
                "ArunMonocle", "sdk-Namespace-960", "sdk-EventHub-532", "sdk-Authrules-2513", Context.NONE);
    }
}
```

### EventHubs_ListAuthorizationRules

```java
import com.azure.core.util.Context;

/** Samples for EventHubs ListAuthorizationRules. */
public final class EventHubsListAuthorizationRulesSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/EventHubs/EHEventHubAuthorizationRuleListAll.json
     */
    /**
     * Sample code: EventHubAuthorizationRuleListAll.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void eventHubAuthorizationRuleListAll(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .eventHubs()
            .listAuthorizationRules("ArunMonocle", "sdk-Namespace-960", "sdk-EventHub-532", Context.NONE);
    }
}
```

### EventHubs_ListByNamespace

```java
import com.azure.core.util.Context;

/** Samples for EventHubs ListByNamespace. */
public final class EventHubsListByNamespaceSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/EventHubs/EHEventHubListByNameSpace.json
     */
    /**
     * Sample code: EventHubsListAll.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void eventHubsListAll(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .eventHubs()
            .listByNamespace("Default-NotificationHubs-AustraliaEast", "sdk-Namespace-5357", null, null, Context.NONE);
    }
}
```

### EventHubs_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for EventHubs ListKeys. */
public final class EventHubsListKeysSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/EventHubs/EHEventHubAuthorizationRuleListKey.json
     */
    /**
     * Sample code: EventHubAuthorizationRuleListKey.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void eventHubAuthorizationRuleListKey(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .eventHubs()
            .listKeysWithResponse(
                "ArunMonocle", "sdk-namespace-960", "sdk-EventHub-532", "sdk-Authrules-2513", Context.NONE);
    }
}
```

### EventHubs_RegenerateKeys

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.eventhubs.generated.models.KeyType;
import com.azure.resourcemanager.eventhubs.generated.models.RegenerateAccessKeyParameters;

/** Samples for EventHubs RegenerateKeys. */
public final class EventHubsRegenerateKeysSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/EventHubs/EHEventHubAuthorizationRuleRegenerateKey.json
     */
    /**
     * Sample code: EventHubAuthorizationRuleRegenerateKey.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void eventHubAuthorizationRuleRegenerateKey(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .eventHubs()
            .regenerateKeysWithResponse(
                "ArunMonocle",
                "sdk-namespace-960",
                "sdk-EventHub-532",
                "sdk-Authrules-1534",
                new RegenerateAccessKeyParameters().withKeyType(KeyType.PRIMARY_KEY),
                Context.NONE);
    }
}
```

### Namespaces_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.eventhubs.generated.models.CheckNameAvailabilityParameter;

/** Samples for Namespaces CheckNameAvailability. */
public final class NamespacesCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/NameSpaces/EHNameSpaceCheckNameAvailability.json
     */
    /**
     * Sample code: NamespacesCheckNameAvailability.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void namespacesCheckNameAvailability(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .namespaces()
            .checkNameAvailabilityWithResponse(
                new CheckNameAvailabilityParameter().withName("sdk-Namespace-8458"), Context.NONE);
    }
}
```

### Namespaces_CreateOrUpdate

```java
import com.azure.resourcemanager.eventhubs.generated.models.KeySource;
import com.azure.resourcemanager.eventhubs.generated.models.KeyVaultProperties;
import com.azure.resourcemanager.eventhubs.generated.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.eventhubs.generated.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Namespaces CreateOrUpdate. */
public final class NamespacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/NameSpaces/EHNameSpaceCreate.json
     */
    /**
     * Sample code: NamespaceCreate.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void namespaceCreate(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .namespaces()
            .define("NamespaceSample")
            .withRegion("East US")
            .withExistingResourceGroup("ResurceGroupSample")
            .withTypeIdentityType(ManagedServiceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
            .withUserAssignedIdentities(
                mapOf(
                    "/subscriptions/SampleSubscription/resourceGroups/ResurceGroupSample/providers/Microsoft.ManagedIdentity/userAssignedIdentities/ud1",
                    new UserAssignedIdentity(),
                    "/subscriptions/SampleSubscription/resourceGroups/ResurceGroupSample/providers/Microsoft.ManagedIdentity/userAssignedIdentities/ud2",
                    new UserAssignedIdentity()))
            .withClusterArmId(
                "/subscriptions/SampleSubscription/resourceGroups/ResurceGroupSample/providers/Microsoft.EventHub/clusters/enc-test")
            .withKeyVaultProperties(
                Arrays
                    .asList(
                        new KeyVaultProperties()
                            .withKeyName("Samplekey")
                            .withKeyVaultUri("https://aprao-keyvault-user.vault-int.azure-int.net/")
                            .withUserAssignedIdentity(
                                "/subscriptions/SampleSubscription/resourceGroups/ResurceGroupSample/providers/Microsoft.ManagedIdentity/userAssignedIdentities/ud1")))
            .withKeySource(KeySource.MICROSOFT_KEY_VAULT)
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

### Namespaces_CreateOrUpdateAuthorizationRule

```java
import com.azure.resourcemanager.eventhubs.generated.models.AccessRights;
import java.util.Arrays;

/** Samples for Namespaces CreateOrUpdateAuthorizationRule. */
public final class NamespacesCreateOrUpdateAuthorizationRuleSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/NameSpaces/EHNameSpaceAuthorizationRuleCreate.json
     */
    /**
     * Sample code: NameSpaceAuthorizationRuleCreate.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void nameSpaceAuthorizationRuleCreate(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .namespaces()
            .defineAuthorizationRule("sdk-Authrules-1746")
            .withExistingNamespace("ArunMonocle", "sdk-Namespace-2702")
            .withRights(Arrays.asList(AccessRights.LISTEN, AccessRights.SEND))
            .create();
    }
}
```

### Namespaces_CreateOrUpdateNetworkRuleSet

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.eventhubs.generated.fluent.models.NetworkRuleSetInner;
import com.azure.resourcemanager.eventhubs.generated.models.DefaultAction;
import com.azure.resourcemanager.eventhubs.generated.models.NWRuleSetIpRules;
import com.azure.resourcemanager.eventhubs.generated.models.NWRuleSetVirtualNetworkRules;
import com.azure.resourcemanager.eventhubs.generated.models.NetworkRuleIpAction;
import com.azure.resourcemanager.eventhubs.generated.models.Subnet;
import java.util.Arrays;

/** Samples for Namespaces CreateOrUpdateNetworkRuleSet. */
public final class NamespacesCreateOrUpdateNetworkRuleSetSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/NameSpaces/VirtualNetworkRule/EHNetworkRuleSetCreate.json
     */
    /**
     * Sample code: NameSpaceNetworkRuleSetCreate.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void nameSpaceNetworkRuleSetCreate(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .namespaces()
            .createOrUpdateNetworkRuleSetWithResponse(
                "ResourceGroup",
                "sdk-Namespace-6019",
                new NetworkRuleSetInner()
                    .withDefaultAction(DefaultAction.DENY)
                    .withVirtualNetworkRules(
                        Arrays
                            .asList(
                                new NWRuleSetVirtualNetworkRules()
                                    .withSubnet(
                                        new Subnet()
                                            .withId(
                                                "/subscriptions/subscriptionid/resourcegroups/resourcegroupid/providers/Microsoft.Network/virtualNetworks/myvn/subnets/subnet2"))
                                    .withIgnoreMissingVnetServiceEndpoint(true),
                                new NWRuleSetVirtualNetworkRules()
                                    .withSubnet(
                                        new Subnet()
                                            .withId(
                                                "/subscriptions/subscriptionid/resourcegroups/resourcegroupid/providers/Microsoft.Network/virtualNetworks/myvn/subnets/subnet3"))
                                    .withIgnoreMissingVnetServiceEndpoint(false),
                                new NWRuleSetVirtualNetworkRules()
                                    .withSubnet(
                                        new Subnet()
                                            .withId(
                                                "/subscriptions/subscriptionid/resourcegroups/resourcegroupid/providers/Microsoft.Network/virtualNetworks/myvn/subnets/subnet6"))
                                    .withIgnoreMissingVnetServiceEndpoint(false)))
                    .withIpRules(
                        Arrays
                            .asList(
                                new NWRuleSetIpRules().withIpMask("1.1.1.1").withAction(NetworkRuleIpAction.ALLOW),
                                new NWRuleSetIpRules().withIpMask("1.1.1.2").withAction(NetworkRuleIpAction.ALLOW),
                                new NWRuleSetIpRules().withIpMask("1.1.1.3").withAction(NetworkRuleIpAction.ALLOW),
                                new NWRuleSetIpRules().withIpMask("1.1.1.4").withAction(NetworkRuleIpAction.ALLOW),
                                new NWRuleSetIpRules().withIpMask("1.1.1.5").withAction(NetworkRuleIpAction.ALLOW))),
                Context.NONE);
    }
}
```

### Namespaces_Delete

```java
import com.azure.core.util.Context;

/** Samples for Namespaces Delete. */
public final class NamespacesDeleteSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/NameSpaces/EHNameSpaceDelete.json
     */
    /**
     * Sample code: NameSpaceDelete.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void nameSpaceDelete(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager.namespaces().delete("ResurceGroupSample", "NamespaceSample", Context.NONE);
    }
}
```

### Namespaces_DeleteAuthorizationRule

```java
import com.azure.core.util.Context;

/** Samples for Namespaces DeleteAuthorizationRule. */
public final class NamespacesDeleteAuthorizationRuleSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/NameSpaces/EHNameSpaceAuthorizationRuleDelete.json
     */
    /**
     * Sample code: NameSpaceAuthorizationRuleDelete.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void nameSpaceAuthorizationRuleDelete(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .namespaces()
            .deleteAuthorizationRuleWithResponse(
                "ArunMonocle", "sdk-Namespace-8980", "sdk-Authrules-8929", Context.NONE);
    }
}
```

### Namespaces_GetAuthorizationRule

```java
import com.azure.core.util.Context;

/** Samples for Namespaces GetAuthorizationRule. */
public final class NamespacesGetAuthorizationRuleSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/NameSpaces/EHNameSpaceAuthorizationRuleGet.json
     */
    /**
     * Sample code: NameSpaceAuthorizationRuleGet.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void nameSpaceAuthorizationRuleGet(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .namespaces()
            .getAuthorizationRuleWithResponse("ArunMonocle", "sdk-Namespace-2702", "sdk-Authrules-1746", Context.NONE);
    }
}
```

### Namespaces_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Namespaces GetByResourceGroup. */
public final class NamespacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/NameSpaces/EHNameSpaceGet.json
     */
    /**
     * Sample code: NameSpaceGet.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void nameSpaceGet(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager.namespaces().getByResourceGroupWithResponse("ResurceGroupSample", "NamespaceSample", Context.NONE);
    }
}
```

### Namespaces_GetNetworkRuleSet

```java
import com.azure.core.util.Context;

/** Samples for Namespaces GetNetworkRuleSet. */
public final class NamespacesGetNetworkRuleSetSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/NameSpaces/VirtualNetworkRule/EHNetworkRuleSetGet.json
     */
    /**
     * Sample code: NameSpaceNetworkRuleSetGet.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void nameSpaceNetworkRuleSetGet(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager.namespaces().getNetworkRuleSetWithResponse("ResourceGroup", "sdk-Namespace-6019", Context.NONE);
    }
}
```

### Namespaces_List

```java
import com.azure.core.util.Context;

/** Samples for Namespaces List. */
public final class NamespacesListSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/NameSpaces/EHNameSpaceList.json
     */
    /**
     * Sample code: NamespacesListBySubscription.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void namespacesListBySubscription(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager.namespaces().list(Context.NONE);
    }
}
```

### Namespaces_ListAuthorizationRules

```java
import com.azure.core.util.Context;

/** Samples for Namespaces ListAuthorizationRules. */
public final class NamespacesListAuthorizationRulesSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/NameSpaces/EHNameSpaceAuthorizationRuleListAll.json
     */
    /**
     * Sample code: ListAuthorizationRules.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void listAuthorizationRules(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager.namespaces().listAuthorizationRules("ArunMonocle", "sdk-Namespace-2702", Context.NONE);
    }
}
```

### Namespaces_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Namespaces ListByResourceGroup. */
public final class NamespacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/NameSpaces/EHNameSpaceListByResourceGroup.json
     */
    /**
     * Sample code: NamespaceListByResourceGroup.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void namespaceListByResourceGroup(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager.namespaces().listByResourceGroup("ResurceGroupSample", Context.NONE);
    }
}
```

### Namespaces_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for Namespaces ListKeys. */
public final class NamespacesListKeysSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/NameSpaces/EHNameSpaceAuthorizationRuleListKey.json
     */
    /**
     * Sample code: NameSpaceAuthorizationRuleListKey.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void nameSpaceAuthorizationRuleListKey(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .namespaces()
            .listKeysWithResponse("ArunMonocle", "sdk-Namespace-2702", "sdk-Authrules-1746", Context.NONE);
    }
}
```

### Namespaces_ListNetworkRuleSet

```java
import com.azure.core.util.Context;

/** Samples for Namespaces ListNetworkRuleSet. */
public final class NamespacesListNetworkRuleSetSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/NameSpaces/VirtualNetworkRule/EHNetworkRuleSetList.json
     */
    /**
     * Sample code: NameSpaceNetworkRuleSetList.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void nameSpaceNetworkRuleSetList(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager.namespaces().listNetworkRuleSetWithResponse("ResourceGroup", "sdk-Namespace-6019", Context.NONE);
    }
}
```

### Namespaces_RegenerateKeys

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.eventhubs.generated.models.KeyType;
import com.azure.resourcemanager.eventhubs.generated.models.RegenerateAccessKeyParameters;

/** Samples for Namespaces RegenerateKeys. */
public final class NamespacesRegenerateKeysSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/NameSpaces/EHNameSpaceAuthorizationRuleRegenerateKey.json
     */
    /**
     * Sample code: NameSpaceAuthorizationRuleRegenerateKey.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void nameSpaceAuthorizationRuleRegenerateKey(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .namespaces()
            .regenerateKeysWithResponse(
                "ArunMonocle",
                "sdk-Namespace-8980",
                "sdk-Authrules-8929",
                new RegenerateAccessKeyParameters().withKeyType(KeyType.PRIMARY_KEY),
                Context.NONE);
    }
}
```

### Namespaces_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.eventhubs.generated.models.EHNamespace;
import com.azure.resourcemanager.eventhubs.generated.models.ManagedServiceIdentityType;
import java.util.HashMap;
import java.util.Map;

/** Samples for Namespaces Update. */
public final class NamespacesUpdateSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/NameSpaces/EHNameSpaceUpdate.json
     */
    /**
     * Sample code: NamespacesUpdate.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void namespacesUpdate(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        EHNamespace resource =
            manager
                .namespaces()
                .getByResourceGroupWithResponse("ResurceGroupSample", "NamespaceSample", Context.NONE)
                .getValue();
        resource
            .update()
            .withTypeIdentityType(ManagedServiceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
            .withUserAssignedIdentities(
                mapOf(
                    "/subscriptions/SampleSubscription/resourceGroups/ResurceGroupSample/providers/Microsoft.ManagedIdentity/userAssignedIdentities/ud2",
                    null))
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

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/EHOperations_List.json
     */
    /**
     * Sample code: EHOperations_List.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void eHOperationsList(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.eventhubs.generated.models.ConnectionState;
import com.azure.resourcemanager.eventhubs.generated.models.EndPointProvisioningState;
import com.azure.resourcemanager.eventhubs.generated.models.PrivateEndpoint;
import com.azure.resourcemanager.eventhubs.generated.models.PrivateLinkConnectionStatus;

/** Samples for PrivateEndpointConnections CreateOrUpdate. */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/NameSpaces/PrivateEndPointConnectionCreate.json
     */
    /**
     * Sample code: NameSpacePrivateEndPointConnectionCreate.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void nameSpacePrivateEndPointConnectionCreate(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .privateEndpointConnections()
            .define("privateEndpointConnectionName")
            .withExistingNamespace("ArunMonocle", "sdk-Namespace-2924")
            .withPrivateEndpoint(
                new PrivateEndpoint()
                    .withId(
                        "/subscriptions/dbedb4e0-40e6-4145-81f3-f1314c150774/resourceGroups/SDK-EventHub-8396/providers/Microsoft.Network/privateEndpoints/sdk-Namespace-2847"))
            .withPrivateLinkServiceConnectionState(
                new ConnectionState().withStatus(PrivateLinkConnectionStatus.REJECTED).withDescription("testing"))
            .withProvisioningState(EndPointProvisioningState.SUCCEEDED)
            .create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Delete. */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/NameSpaces/PrivateEndPointConnectionDelete.json
     */
    /**
     * Sample code: NameSpacePrivateEndPointConnectionDelete.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void nameSpacePrivateEndPointConnectionDelete(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .privateEndpointConnections()
            .delete("ArunMonocle", "sdk-Namespace-3285", "928c44d5-b7c6-423b-b6fa-811e0c27b3e0", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/NameSpaces/PrivateEndPointConnectionGet.json
     */
    /**
     * Sample code: NameSpacePrivateEndPointConnectionGet.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void nameSpacePrivateEndPointConnectionGet(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse("SDK-EventHub-4794", "sdk-Namespace-5828", "privateEndpointConnectionName", Context.NONE);
    }
}
```

### PrivateEndpointConnections_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections List. */
public final class PrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/NameSpaces/PrivateEndPointConnectionList.json
     */
    /**
     * Sample code: NameSpaceCreate.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void nameSpaceCreate(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager.privateEndpointConnections().list("SDK-EventHub-4794", "sdk-Namespace-5828", Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources Get. */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/NameSpaces/PrivateLinkResourcesGet.json
     */
    /**
     * Sample code: NameSpacePrivateLinkResourcesGet.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void nameSpacePrivateLinkResourcesGet(
        com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager.privateLinkResources().getWithResponse("ArunMonocle", "sdk-Namespace-2924", Context.NONE);
    }
}
```

### SchemaRegistry_CreateOrUpdate

```java
import com.azure.resourcemanager.eventhubs.generated.models.SchemaCompatibility;
import com.azure.resourcemanager.eventhubs.generated.models.SchemaType;
import java.util.HashMap;
import java.util.Map;

/** Samples for SchemaRegistry CreateOrUpdate. */
public final class SchemaRegistryCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/SchemaRegistry/SchemaRegistryCreate.json
     */
    /**
     * Sample code: SchemaRegistryCreate.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void schemaRegistryCreate(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .schemaRegistries()
            .define("testSchemaGroup1")
            .withExistingNamespace("alitest", "ali-ua-test-eh-system-1")
            .withGroupProperties(mapOf())
            .withSchemaCompatibility(SchemaCompatibility.FORWARD)
            .withSchemaType(SchemaType.AVRO)
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

### SchemaRegistry_Delete

```java
import com.azure.core.util.Context;

/** Samples for SchemaRegistry Delete. */
public final class SchemaRegistryDeleteSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/SchemaRegistry/SchemaRegistryDelete.json
     */
    /**
     * Sample code: SchemaRegistryDelete.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void schemaRegistryDelete(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .schemaRegistries()
            .deleteWithResponse("alitest", "ali-ua-test-eh-system-1", "testSchemaGroup1", Context.NONE);
    }
}
```

### SchemaRegistry_Get

```java
import com.azure.core.util.Context;

/** Samples for SchemaRegistry Get. */
public final class SchemaRegistryGetSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/SchemaRegistry/SchemaRegistryGet.json
     */
    /**
     * Sample code: SchemaRegistryGet.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void schemaRegistryGet(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager
            .schemaRegistries()
            .getWithResponse("alitest", "ali-ua-test-eh-system-1", "testSchemaGroup1", Context.NONE);
    }
}
```

### SchemaRegistry_ListByNamespace

```java
import com.azure.core.util.Context;

/** Samples for SchemaRegistry ListByNamespace. */
public final class SchemaRegistryListByNamespaceSamples {
    /*
     * x-ms-original-file: specification/eventhub/resource-manager/Microsoft.EventHub/stable/2021-11-01/examples/SchemaRegistry/SchemaRegistryListByNamespace.json
     */
    /**
     * Sample code: SchemaRegistryListAll.
     *
     * @param manager Entry point to EventHubsManager.
     */
    public static void schemaRegistryListAll(com.azure.resourcemanager.eventhubs.generated.EventHubsManager manager) {
        manager.schemaRegistries().listByNamespace("alitest", "ali-ua-test-eh-system-1", null, null, Context.NONE);
    }
}
```

