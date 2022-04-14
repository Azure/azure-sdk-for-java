# Code snippets and samples


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

## MigrationConfigs

- [CompleteMigration](#migrationconfigs_completemigration)
- [CreateAndStartMigration](#migrationconfigs_createandstartmigration)
- [Delete](#migrationconfigs_delete)
- [Get](#migrationconfigs_get)
- [List](#migrationconfigs_list)
- [Revert](#migrationconfigs_revert)

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
- [ListNetworkRuleSets](#namespaces_listnetworkrulesets)
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

## Queues

- [CreateOrUpdate](#queues_createorupdate)
- [CreateOrUpdateAuthorizationRule](#queues_createorupdateauthorizationrule)
- [Delete](#queues_delete)
- [DeleteAuthorizationRule](#queues_deleteauthorizationrule)
- [Get](#queues_get)
- [GetAuthorizationRule](#queues_getauthorizationrule)
- [ListAuthorizationRules](#queues_listauthorizationrules)
- [ListByNamespace](#queues_listbynamespace)
- [ListKeys](#queues_listkeys)
- [RegenerateKeys](#queues_regeneratekeys)

## Rules

- [CreateOrUpdate](#rules_createorupdate)
- [Delete](#rules_delete)
- [Get](#rules_get)
- [ListBySubscriptions](#rules_listbysubscriptions)

## Subscriptions

- [CreateOrUpdate](#subscriptions_createorupdate)
- [Delete](#subscriptions_delete)
- [Get](#subscriptions_get)
- [ListByTopic](#subscriptions_listbytopic)

## Topics

- [CreateOrUpdate](#topics_createorupdate)
- [CreateOrUpdateAuthorizationRule](#topics_createorupdateauthorizationrule)
- [Delete](#topics_delete)
- [DeleteAuthorizationRule](#topics_deleteauthorizationrule)
- [Get](#topics_get)
- [GetAuthorizationRule](#topics_getauthorizationrule)
- [ListAuthorizationRules](#topics_listauthorizationrules)
- [ListByNamespace](#topics_listbynamespace)
- [ListKeys](#topics_listkeys)
- [RegenerateKeys](#topics_regeneratekeys)
### DisasterRecoveryConfigs_BreakPairing

```java
import com.azure.core.util.Context;

/** Samples for DisasterRecoveryConfigs BreakPairing. */
public final class DisasterRecoveryConfigsBreakPairingSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/disasterRecoveryConfigs/SBEHAliasBreakPairing.json
     */
    /**
     * Sample code: SBEHAliasBreakPairing.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void sBEHAliasBreakPairing(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .disasterRecoveryConfigs()
            .breakPairingWithResponse(
                "ardsouzatestRG", "sdk-Namespace-8860", "sdk-DisasterRecovery-3814", Context.NONE);
    }
}
```

### DisasterRecoveryConfigs_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.servicebus.generated.models.CheckNameAvailability;

/** Samples for DisasterRecoveryConfigs CheckNameAvailability. */
public final class DisasterRecoveryConfigsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/disasterRecoveryConfigs/SBAliasCheckNameAvailability.json
     */
    /**
     * Sample code: AliasNameAvailability.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void aliasNameAvailability(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .disasterRecoveryConfigs()
            .checkNameAvailabilityWithResponse(
                "exampleResourceGroup",
                "sdk-Namespace-9080",
                new CheckNameAvailability().withName("sdk-DisasterRecovery-9474"),
                Context.NONE);
    }
}
```

### DisasterRecoveryConfigs_CreateOrUpdate

```java
/** Samples for DisasterRecoveryConfigs CreateOrUpdate. */
public final class DisasterRecoveryConfigsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/disasterRecoveryConfigs/SBAliasCreate.json
     */
    /**
     * Sample code: SBAliasCreate.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void sBAliasCreate(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .disasterRecoveryConfigs()
            .define("sdk-Namespace-8860")
            .withExistingNamespace("ardsouzatestRG", "sdk-Namespace-8860")
            .withPartnerNamespace("sdk-Namespace-37")
            .withAlternateName("alternameforAlias-Namespace-8860")
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
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/disasterRecoveryConfigs/SBAliasDelete.json
     */
    /**
     * Sample code: SBAliasDelete.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void sBAliasDelete(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .disasterRecoveryConfigs()
            .deleteWithResponse("SouthCentralUS", "sdk-Namespace-8860", "sdk-DisasterRecovery-3814", Context.NONE);
    }
}
```

### DisasterRecoveryConfigs_FailOver

```java
import com.azure.core.util.Context;

/** Samples for DisasterRecoveryConfigs FailOver. */
public final class DisasterRecoveryConfigsFailOverSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/disasterRecoveryConfigs/SBAliasFailOver.json
     */
    /**
     * Sample code: SBAliasFailOver.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void sBAliasFailOver(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .disasterRecoveryConfigs()
            .failOverWithResponse(
                "ardsouzatestRG", "sdk-Namespace-8860", "sdk-DisasterRecovery-3814", null, Context.NONE);
    }
}
```

### DisasterRecoveryConfigs_Get

```java
import com.azure.core.util.Context;

/** Samples for DisasterRecoveryConfigs Get. */
public final class DisasterRecoveryConfigsGetSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/disasterRecoveryConfigs/SBAliasGet.json
     */
    /**
     * Sample code: SBAliasGet.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void sBAliasGet(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .disasterRecoveryConfigs()
            .getWithResponse("ardsouzatestRG", "sdk-Namespace-8860", "sdk-DisasterRecovery-3814", Context.NONE);
    }
}
```

### DisasterRecoveryConfigs_GetAuthorizationRule

```java
import com.azure.core.util.Context;

/** Samples for DisasterRecoveryConfigs GetAuthorizationRule. */
public final class DisasterRecoveryConfigsGetAuthorizationRuleSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/disasterRecoveryConfigs/SBAliasAuthorizationRuleGet.json
     */
    /**
     * Sample code: DisasterRecoveryConfigsAuthorizationRuleGet.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void disasterRecoveryConfigsAuthorizationRuleGet(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
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
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/disasterRecoveryConfigs/SBAliasList.json
     */
    /**
     * Sample code: SBAliasList.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void sBAliasList(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager.disasterRecoveryConfigs().list("ardsouzatestRG", "sdk-Namespace-8860", Context.NONE);
    }
}
```

### DisasterRecoveryConfigs_ListAuthorizationRules

```java
import com.azure.core.util.Context;

/** Samples for DisasterRecoveryConfigs ListAuthorizationRules. */
public final class DisasterRecoveryConfigsListAuthorizationRulesSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/disasterRecoveryConfigs/SBAliasAuthorizationRuleListAll.json
     */
    /**
     * Sample code: NameSpaceAuthorizationRuleListAll.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void nameSpaceAuthorizationRuleListAll(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
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
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/disasterRecoveryConfigs/SBAliasAuthorizationRuleListKey.json
     */
    /**
     * Sample code: DisasterRecoveryConfigsAuthorizationRuleListKey.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void disasterRecoveryConfigsAuthorizationRuleListKey(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
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

### MigrationConfigs_CompleteMigration

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.servicebus.generated.models.MigrationConfigurationName;

/** Samples for MigrationConfigs CompleteMigration. */
public final class MigrationConfigsCompleteMigrationSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Migrationconfigurations/SBMigrationconfigurationCompleteMigration.json
     */
    /**
     * Sample code: MigrationConfigurationsCompleteMigration.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void migrationConfigurationsCompleteMigration(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .migrationConfigs()
            .completeMigrationWithResponse(
                "ResourceGroup", "sdk-Namespace-41", MigrationConfigurationName.DEFAULT, Context.NONE);
    }
}
```

### MigrationConfigs_CreateAndStartMigration

```java
import com.azure.resourcemanager.servicebus.generated.models.MigrationConfigurationName;

/** Samples for MigrationConfigs CreateAndStartMigration. */
public final class MigrationConfigsCreateAndStartMigrationSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Migrationconfigurations/SBMigrationconfigurationCreateAndStartMigration.json
     */
    /**
     * Sample code: MigrationConfigurationsStartMigration.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void migrationConfigurationsStartMigration(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .migrationConfigs()
            .define(MigrationConfigurationName.DEFAULT)
            .withExistingNamespace("ResourceGroup", "sdk-Namespace-41")
            .withTargetNamespace(
                "/subscriptions/SubscriptionId/resourceGroups/ResourceGroup/providers/Microsoft.ServiceBus/namespaces/sdk-Namespace-4028")
            .withPostMigrationName("sdk-PostMigration-5919")
            .create();
    }
}
```

### MigrationConfigs_Delete

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.servicebus.generated.models.MigrationConfigurationName;

/** Samples for MigrationConfigs Delete. */
public final class MigrationConfigsDeleteSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Migrationconfigurations/SBMigrationconfigurationDelete.json
     */
    /**
     * Sample code: MigrationConfigurationsDelete.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void migrationConfigurationsDelete(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .migrationConfigs()
            .deleteWithResponse("ResourceGroup", "sdk-Namespace-41", MigrationConfigurationName.DEFAULT, Context.NONE);
    }
}
```

### MigrationConfigs_Get

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.servicebus.generated.models.MigrationConfigurationName;

/** Samples for MigrationConfigs Get. */
public final class MigrationConfigsGetSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Migrationconfigurations/SBMigrationconfigurationGet.json
     */
    /**
     * Sample code: MigrationConfigurationsGet.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void migrationConfigurationsGet(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .migrationConfigs()
            .getWithResponse("ResourceGroup", "sdk-Namespace-41", MigrationConfigurationName.DEFAULT, Context.NONE);
    }
}
```

### MigrationConfigs_List

```java
import com.azure.core.util.Context;

/** Samples for MigrationConfigs List. */
public final class MigrationConfigsListSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Migrationconfigurations/SBMigrationconfigurationList.json
     */
    /**
     * Sample code: MigrationConfigurationsList.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void migrationConfigurationsList(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager.migrationConfigs().list("ResourceGroup", "sdk-Namespace-9259", Context.NONE);
    }
}
```

### MigrationConfigs_Revert

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.servicebus.generated.models.MigrationConfigurationName;

/** Samples for MigrationConfigs Revert. */
public final class MigrationConfigsRevertSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Migrationconfigurations/SBMigrationconfigurationRevert.json
     */
    /**
     * Sample code: MigrationConfigurationsRevert.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void migrationConfigurationsRevert(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .migrationConfigs()
            .revertWithResponse("ResourceGroup", "sdk-Namespace-41", MigrationConfigurationName.DEFAULT, Context.NONE);
    }
}
```

### Namespaces_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.servicebus.generated.models.CheckNameAvailability;

/** Samples for Namespaces CheckNameAvailability. */
public final class NamespacesCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/NameSpaces/SBNameSpaceCheckNameAvailability.json
     */
    /**
     * Sample code: NameSpaceCheckNameAvailability.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void nameSpaceCheckNameAvailability(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .namespaces()
            .checkNameAvailabilityWithResponse(
                new CheckNameAvailability().withName("sdk-Namespace-2924"), Context.NONE);
    }
}
```

### Namespaces_CreateOrUpdate

```java
import com.azure.resourcemanager.servicebus.generated.models.SBSku;
import com.azure.resourcemanager.servicebus.generated.models.SkuName;
import com.azure.resourcemanager.servicebus.generated.models.SkuTier;
import java.util.HashMap;
import java.util.Map;

/** Samples for Namespaces CreateOrUpdate. */
public final class NamespacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/NameSpaces/SBNameSpaceCreate.json
     */
    /**
     * Sample code: NameSpaceCreate.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void nameSpaceCreate(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .namespaces()
            .define("sdk-Namespace2924")
            .withRegion("South Central US")
            .withExistingResourceGroup("ArunMonocle")
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
            .withSku(new SBSku().withName(SkuName.STANDARD).withTier(SkuTier.STANDARD))
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
import com.azure.resourcemanager.servicebus.generated.models.AccessRights;
import java.util.Arrays;

/** Samples for Namespaces CreateOrUpdateAuthorizationRule. */
public final class NamespacesCreateOrUpdateAuthorizationRuleSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/NameSpaces/SBNameSpaceAuthorizationRuleCreate.json
     */
    /**
     * Sample code: NameSpaceAuthorizationRuleCreate.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void nameSpaceAuthorizationRuleCreate(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .namespaces()
            .defineAuthorizationRule("sdk-AuthRules-1788")
            .withExistingNamespace("ArunMonocle", "sdk-Namespace-6914")
            .withRights(Arrays.asList(AccessRights.LISTEN, AccessRights.SEND))
            .create();
    }
}
```

### Namespaces_CreateOrUpdateNetworkRuleSet

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.servicebus.generated.fluent.models.NetworkRuleSetInner;
import com.azure.resourcemanager.servicebus.generated.models.DefaultAction;
import com.azure.resourcemanager.servicebus.generated.models.NWRuleSetIpRules;
import com.azure.resourcemanager.servicebus.generated.models.NWRuleSetVirtualNetworkRules;
import com.azure.resourcemanager.servicebus.generated.models.NetworkRuleIpAction;
import com.azure.resourcemanager.servicebus.generated.models.Subnet;
import java.util.Arrays;

/** Samples for Namespaces CreateOrUpdateNetworkRuleSet. */
public final class NamespacesCreateOrUpdateNetworkRuleSetSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/NameSpaces/VirtualNetworkRule/SBNetworkRuleSetCreate.json
     */
    /**
     * Sample code: NameSpaceNetworkRuleSetCreate.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void nameSpaceNetworkRuleSetCreate(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
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
                                                "/subscriptions/854d368f-1828-428f-8f3c-f2affa9b2f7d/resourcegroups/alitest/providers/Microsoft.Network/virtualNetworks/myvn/subnets/subnet2"))
                                    .withIgnoreMissingVnetServiceEndpoint(true),
                                new NWRuleSetVirtualNetworkRules()
                                    .withSubnet(
                                        new Subnet()
                                            .withId(
                                                "/subscriptions/854d368f-1828-428f-8f3c-f2affa9b2f7d/resourcegroups/alitest/providers/Microsoft.Network/virtualNetworks/myvn/subnets/subnet3"))
                                    .withIgnoreMissingVnetServiceEndpoint(false),
                                new NWRuleSetVirtualNetworkRules()
                                    .withSubnet(
                                        new Subnet()
                                            .withId(
                                                "/subscriptions/854d368f-1828-428f-8f3c-f2affa9b2f7d/resourcegroups/alitest/providers/Microsoft.Network/virtualNetworks/myvn/subnets/subnet6"))
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
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/NameSpaces/SBNameSpaceDelete.json
     */
    /**
     * Sample code: NameSpaceDelete.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void nameSpaceDelete(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager.namespaces().delete("ArunMonocle", "sdk-Namespace-3285", Context.NONE);
    }
}
```

### Namespaces_DeleteAuthorizationRule

```java
import com.azure.core.util.Context;

/** Samples for Namespaces DeleteAuthorizationRule. */
public final class NamespacesDeleteAuthorizationRuleSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/NameSpaces/SBNameSpaceAuthorizationRuleDelete.json
     */
    /**
     * Sample code: NameSpaceAuthorizationRuleDelete.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void nameSpaceAuthorizationRuleDelete(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .namespaces()
            .deleteAuthorizationRuleWithResponse(
                "ArunMonocle", "sdk-namespace-6914", "sdk-AuthRules-1788", Context.NONE);
    }
}
```

### Namespaces_GetAuthorizationRule

```java
import com.azure.core.util.Context;

/** Samples for Namespaces GetAuthorizationRule. */
public final class NamespacesGetAuthorizationRuleSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/NameSpaces/SBNameSpaceAuthorizationRuleGet.json
     */
    /**
     * Sample code: NameSpaceAuthorizationRuleGet.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void nameSpaceAuthorizationRuleGet(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .namespaces()
            .getAuthorizationRuleWithResponse("ArunMonocle", "sdk-Namespace-6914", "sdk-AuthRules-1788", Context.NONE);
    }
}
```

### Namespaces_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Namespaces GetByResourceGroup. */
public final class NamespacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/NameSpaces/SBNameSpaceGet.json
     */
    /**
     * Sample code: NameSpaceGet.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void nameSpaceGet(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager.namespaces().getByResourceGroupWithResponse("ArunMonocle", "sdk-Namespace-2924", Context.NONE);
    }
}
```

### Namespaces_GetNetworkRuleSet

```java
import com.azure.core.util.Context;

/** Samples for Namespaces GetNetworkRuleSet. */
public final class NamespacesGetNetworkRuleSetSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/NameSpaces/VirtualNetworkRule/SBNetworkRuleSetGet.json
     */
    /**
     * Sample code: NameSpaceNetworkRuleSetGet.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void nameSpaceNetworkRuleSetGet(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
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
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/NameSpaces/SBNameSpaceList.json
     */
    /**
     * Sample code: NameSpaceList.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void nameSpaceList(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
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
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/NameSpaces/SBNameSpaceAuthorizationRuleListAll.json
     */
    /**
     * Sample code: NameSpaceAuthorizationRuleListAll.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void nameSpaceAuthorizationRuleListAll(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager.namespaces().listAuthorizationRules("ArunMonocle", "sdk-Namespace-6914", Context.NONE);
    }
}
```

### Namespaces_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Namespaces ListByResourceGroup. */
public final class NamespacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/NameSpaces/SBNameSpaceListByResourceGroup.json
     */
    /**
     * Sample code: NameSpaceListByResourceGroup.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void nameSpaceListByResourceGroup(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager.namespaces().listByResourceGroup("ArunMonocle", Context.NONE);
    }
}
```

### Namespaces_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for Namespaces ListKeys. */
public final class NamespacesListKeysSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/NameSpaces/SBNameSpaceAuthorizationRuleListKey.json
     */
    /**
     * Sample code: NameSpaceAuthorizationRuleListKey.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void nameSpaceAuthorizationRuleListKey(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .namespaces()
            .listKeysWithResponse("ArunMonocle", "sdk-namespace-6914", "sdk-AuthRules-1788", Context.NONE);
    }
}
```

### Namespaces_ListNetworkRuleSets

```java
import com.azure.core.util.Context;

/** Samples for Namespaces ListNetworkRuleSets. */
public final class NamespacesListNetworkRuleSetsSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/NameSpaces/VirtualNetworkRule/SBNetworkRuleSetList.json
     */
    /**
     * Sample code: NameSpaceNetworkRuleSetList.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void nameSpaceNetworkRuleSetList(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager.namespaces().listNetworkRuleSets("ResourceGroup", "sdk-Namespace-6019", Context.NONE);
    }
}
```

### Namespaces_RegenerateKeys

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.servicebus.generated.models.KeyType;
import com.azure.resourcemanager.servicebus.generated.models.RegenerateAccessKeyParameters;

/** Samples for Namespaces RegenerateKeys. */
public final class NamespacesRegenerateKeysSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/NameSpaces/SBNameSpaceAuthorizationRuleRegenerateKey.json
     */
    /**
     * Sample code: NameSpaceAuthorizationRuleRegenerateKey.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void nameSpaceAuthorizationRuleRegenerateKey(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .namespaces()
            .regenerateKeysWithResponse(
                "ArunMonocle",
                "sdk-namespace-6914",
                "sdk-AuthRules-1788",
                new RegenerateAccessKeyParameters().withKeyType(KeyType.PRIMARY_KEY),
                Context.NONE);
    }
}
```

### Namespaces_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.servicebus.generated.models.SBNamespace;
import java.util.HashMap;
import java.util.Map;

/** Samples for Namespaces Update. */
public final class NamespacesUpdateSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/NameSpaces/SBNameSpaceUpdate.json
     */
    /**
     * Sample code: NameSpaceUpdate.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void nameSpaceUpdate(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        SBNamespace resource =
            manager
                .namespaces()
                .getByResourceGroupWithResponse("ArunMonocle", "sdk-Namespace-3285", Context.NONE)
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

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/SBOperations_List.json
     */
    /**
     * Sample code: OperationsList.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void operationsList(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.servicebus.generated.models.ConnectionState;
import com.azure.resourcemanager.servicebus.generated.models.EndPointProvisioningState;
import com.azure.resourcemanager.servicebus.generated.models.PrivateEndpoint;
import com.azure.resourcemanager.servicebus.generated.models.PrivateLinkConnectionStatus;

/** Samples for PrivateEndpointConnections CreateOrUpdate. */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/NameSpaces/PrivateEndPointConnectionCreate.json
     */
    /**
     * Sample code: NameSpacePrivateEndPointConnectionCreate.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void nameSpacePrivateEndPointConnectionCreate(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .privateEndpointConnections()
            .define("privateEndpointConnectionName")
            .withExistingNamespace("ArunMonocle", "sdk-Namespace-2924")
            .withPrivateEndpoint(
                new PrivateEndpoint()
                    .withId(
                        "/subscriptions/dbedb4e0-40e6-4145-81f3-f1314c150774/resourceGroups/SDK-ServiceBus-8396/providers/Microsoft.Network/privateEndpoints/sdk-Namespace-2847"))
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
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/NameSpaces/PrivateEndPointConnectionDelete.json
     */
    /**
     * Sample code: NameSpacePrivateEndPointConnectionDelete.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void nameSpacePrivateEndPointConnectionDelete(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
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
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/NameSpaces/PrivateEndPointConnectionGet.json
     */
    /**
     * Sample code: NameSpacePrivateEndPointConnectionGet.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void nameSpacePrivateEndPointConnectionGet(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse(
                "SDK-ServiceBus-4794", "sdk-Namespace-5828", "privateEndpointConnectionName", Context.NONE);
    }
}
```

### PrivateEndpointConnections_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections List. */
public final class PrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/NameSpaces/PrivateEndPointConnectionList.json
     */
    /**
     * Sample code: NameSpaceCreate.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void nameSpaceCreate(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager.privateEndpointConnections().list("SDK-ServiceBus-4794", "sdk-Namespace-5828", Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources Get. */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/NameSpaces/PrivateLinkResourcesGet.json
     */
    /**
     * Sample code: NameSpacePrivateLinkResourcesGet.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void nameSpacePrivateLinkResourcesGet(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager.privateLinkResources().getWithResponse("ArunMonocle", "sdk-Namespace-2924", Context.NONE);
    }
}
```

### Queues_CreateOrUpdate

```java
/** Samples for Queues CreateOrUpdate. */
public final class QueuesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Queues/SBQueueCreate.json
     */
    /**
     * Sample code: QueueCreate.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void queueCreate(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .queues()
            .define("sdk-Queues-5647")
            .withExistingNamespace("ArunMonocle", "sdk-Namespace-3174")
            .withEnablePartitioning(true)
            .create();
    }
}
```

### Queues_CreateOrUpdateAuthorizationRule

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.servicebus.generated.fluent.models.SBAuthorizationRuleInner;
import com.azure.resourcemanager.servicebus.generated.models.AccessRights;
import java.util.Arrays;

/** Samples for Queues CreateOrUpdateAuthorizationRule. */
public final class QueuesCreateOrUpdateAuthorizationRuleSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Queues/SBQueueAuthorizationRuleCreate.json
     */
    /**
     * Sample code: QueueAuthorizationRuleCreate.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void queueAuthorizationRuleCreate(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .queues()
            .createOrUpdateAuthorizationRuleWithResponse(
                "ArunMonocle",
                "sdk-Namespace-7982",
                "sdk-Queues-2317",
                "sdk-AuthRules-5800",
                new SBAuthorizationRuleInner().withRights(Arrays.asList(AccessRights.LISTEN, AccessRights.SEND)),
                Context.NONE);
    }
}
```

### Queues_Delete

```java
import com.azure.core.util.Context;

/** Samples for Queues Delete. */
public final class QueuesDeleteSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Queues/SBQueueDelete.json
     */
    /**
     * Sample code: QueueDelete.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void queueDelete(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager.queues().deleteWithResponse("ArunMonocle", "sdk-Namespace-183", "sdk-Queues-8708", Context.NONE);
    }
}
```

### Queues_DeleteAuthorizationRule

```java
import com.azure.core.util.Context;

/** Samples for Queues DeleteAuthorizationRule. */
public final class QueuesDeleteAuthorizationRuleSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Queues/SBQueueAuthorizationRuleDelete.json
     */
    /**
     * Sample code: QueueAuthorizationRuleDelete.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void queueAuthorizationRuleDelete(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .queues()
            .deleteAuthorizationRuleWithResponse(
                "ArunMonocle", "sdk-namespace-7982", "sdk-Queues-2317", "sdk-AuthRules-5800", Context.NONE);
    }
}
```

### Queues_Get

```java
import com.azure.core.util.Context;

/** Samples for Queues Get. */
public final class QueuesGetSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Queues/SBQueueGet.json
     */
    /**
     * Sample code: QueueGet.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void queueGet(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager.queues().getWithResponse("ArunMonocle", "sdk-Namespace-3174", "sdk-Queues-5647", Context.NONE);
    }
}
```

### Queues_GetAuthorizationRule

```java
import com.azure.core.util.Context;

/** Samples for Queues GetAuthorizationRule. */
public final class QueuesGetAuthorizationRuleSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Queues/SBQueueAuthorizationRuleGet.json
     */
    /**
     * Sample code: QueueAuthorizationRuleGet.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void queueAuthorizationRuleGet(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .queues()
            .getAuthorizationRuleWithResponse(
                "ArunMonocle", "sdk-Namespace-7982", "sdk-Queues-2317", "sdk-AuthRules-5800", Context.NONE);
    }
}
```

### Queues_ListAuthorizationRules

```java
import com.azure.core.util.Context;

/** Samples for Queues ListAuthorizationRules. */
public final class QueuesListAuthorizationRulesSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Queues/SBQueueAuthorizationRuleListAll.json
     */
    /**
     * Sample code: QueueAuthorizationRuleListAll.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void queueAuthorizationRuleListAll(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager.queues().listAuthorizationRules("ArunMonocle", "sdk-Namespace-7982", "sdk-Queues-2317", Context.NONE);
    }
}
```

### Queues_ListByNamespace

```java
import com.azure.core.util.Context;

/** Samples for Queues ListByNamespace. */
public final class QueuesListByNamespaceSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Queues/SBQueueListByNameSpace.json
     */
    /**
     * Sample code: QueueListByNameSpace.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void queueListByNameSpace(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager.queues().listByNamespace("ArunMonocle", "sdk-Namespace-3174", null, null, Context.NONE);
    }
}
```

### Queues_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for Queues ListKeys. */
public final class QueuesListKeysSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Queues/SBQueueAuthorizationRuleListKey.json
     */
    /**
     * Sample code: QueueAuthorizationRuleListKey.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void queueAuthorizationRuleListKey(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .queues()
            .listKeysWithResponse(
                "ArunMonocle", "sdk-namespace-7982", "sdk-Queues-2317", "sdk-AuthRules-5800", Context.NONE);
    }
}
```

### Queues_RegenerateKeys

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.servicebus.generated.models.KeyType;
import com.azure.resourcemanager.servicebus.generated.models.RegenerateAccessKeyParameters;

/** Samples for Queues RegenerateKeys. */
public final class QueuesRegenerateKeysSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Queues/SBQueueAuthorizationRuleRegenerateKey.json
     */
    /**
     * Sample code: QueueAuthorizationRuleRegenerateKey.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void queueAuthorizationRuleRegenerateKey(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .queues()
            .regenerateKeysWithResponse(
                "ArunMonocle",
                "sdk-namespace-7982",
                "sdk-Queues-2317",
                "sdk-AuthRules-5800",
                new RegenerateAccessKeyParameters().withKeyType(KeyType.PRIMARY_KEY),
                Context.NONE);
    }
}
```

### Rules_CreateOrUpdate

```java
import com.azure.resourcemanager.servicebus.generated.models.CorrelationFilter;
import com.azure.resourcemanager.servicebus.generated.models.FilterType;
import com.azure.resourcemanager.servicebus.generated.models.SqlFilter;
import java.util.HashMap;
import java.util.Map;

/** Samples for Rules CreateOrUpdate. */
public final class RulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Rules/RuleCreate_CorrelationFilter.json
     */
    /**
     * Sample code: RulesCreateCorrelationFilter.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void rulesCreateCorrelationFilter(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .rules()
            .define("sdk-Rules-6571")
            .withExistingResourceGroup(
                "resourceGroupName", "sdk-Namespace-1319", "sdk-Topics-2081", "sdk-Subscriptions-8691")
            .withFilterType(FilterType.CORRELATION_FILTER)
            .withCorrelationFilter(new CorrelationFilter().withProperties(mapOf("topicHint", "Crop")))
            .create();
    }

    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Rules/RuleCreate.json
     */
    /**
     * Sample code: RulesCreateOrUpdate.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void rulesCreateOrUpdate(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .rules()
            .define("sdk-Rules-6571")
            .withExistingResourceGroup(
                "resourceGroupName", "sdk-Namespace-1319", "sdk-Topics-2081", "sdk-Subscriptions-8691")
            .create();
    }

    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Rules/RuleCreate_SqlFilter.json
     */
    /**
     * Sample code: RulesCreateSqlFilter.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void rulesCreateSqlFilter(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .rules()
            .define("sdk-Rules-6571")
            .withExistingResourceGroup(
                "resourceGroupName", "sdk-Namespace-1319", "sdk-Topics-2081", "sdk-Subscriptions-8691")
            .withFilterType(FilterType.SQL_FILTER)
            .withSqlFilter(new SqlFilter().withSqlExpression("myproperty=test"))
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

### Rules_Delete

```java
import com.azure.core.util.Context;

/** Samples for Rules Delete. */
public final class RulesDeleteSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Rules/RuleDelete.json
     */
    /**
     * Sample code: RulesDelete.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void rulesDelete(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .rules()
            .deleteWithResponse(
                "ArunMonocle",
                "sdk-Namespace-1319",
                "sdk-Topics-2081",
                "sdk-Subscriptions-8691",
                "sdk-Rules-6571",
                Context.NONE);
    }
}
```

### Rules_Get

```java
import com.azure.core.util.Context;

/** Samples for Rules Get. */
public final class RulesGetSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Rules/RuleGet.json
     */
    /**
     * Sample code: RulesGet.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void rulesGet(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .rules()
            .getWithResponse(
                "ArunMonocle",
                "sdk-Namespace-1319",
                "sdk-Topics-2081",
                "sdk-Subscriptions-8691",
                "sdk-Rules-6571",
                Context.NONE);
    }
}
```

### Rules_ListBySubscriptions

```java
import com.azure.core.util.Context;

/** Samples for Rules ListBySubscriptions. */
public final class RulesListBySubscriptionsSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Rules/RuleListBySubscription.json
     */
    /**
     * Sample code: RulesListBySubscriptions.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void rulesListBySubscriptions(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .rules()
            .listBySubscriptions(
                "ArunMonocle",
                "sdk-Namespace-1319",
                "sdk-Topics-2081",
                "sdk-Subscriptions-8691",
                null,
                null,
                Context.NONE);
    }
}
```

### Subscriptions_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.servicebus.generated.fluent.models.SBSubscriptionInner;

/** Samples for Subscriptions CreateOrUpdate. */
public final class SubscriptionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Subscriptions/SBSubscriptionCreate.json
     */
    /**
     * Sample code: SubscriptionCreate.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void subscriptionCreate(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .subscriptions()
            .createOrUpdateWithResponse(
                "ResourceGroup",
                "sdk-Namespace-1349",
                "sdk-Topics-8740",
                "sdk-Subscriptions-2178",
                new SBSubscriptionInner().withEnableBatchedOperations(true),
                Context.NONE);
    }
}
```

### Subscriptions_Delete

```java
import com.azure.core.util.Context;

/** Samples for Subscriptions Delete. */
public final class SubscriptionsDeleteSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Subscriptions/SBSubscriptionDelete.json
     */
    /**
     * Sample code: SubscriptionDelete.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void subscriptionDelete(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .subscriptions()
            .deleteWithResponse(
                "ResourceGroup", "sdk-Namespace-5882", "sdk-Topics-1804", "sdk-Subscriptions-3670", Context.NONE);
    }
}
```

### Subscriptions_Get

```java
import com.azure.core.util.Context;

/** Samples for Subscriptions Get. */
public final class SubscriptionsGetSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Subscriptions/SBSubscriptionGet.json
     */
    /**
     * Sample code: SubscriptionGet.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void subscriptionGet(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .subscriptions()
            .getWithResponse(
                "ResourceGroup", "sdk-Namespace-1349", "sdk-Topics-8740", "sdk-Subscriptions-2178", Context.NONE);
    }
}
```

### Subscriptions_ListByTopic

```java
import com.azure.core.util.Context;

/** Samples for Subscriptions ListByTopic. */
public final class SubscriptionsListByTopicSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Subscriptions/SBSubscriptionListByTopic.json
     */
    /**
     * Sample code: SubscriptionListByTopic.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void subscriptionListByTopic(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .subscriptions()
            .listByTopic("ResourceGroup", "sdk-Namespace-1349", "sdk-Topics-8740", null, null, Context.NONE);
    }
}
```

### Topics_CreateOrUpdate

```java
/** Samples for Topics CreateOrUpdate. */
public final class TopicsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Topics/SBTopicCreate.json
     */
    /**
     * Sample code: TopicCreate.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void topicCreate(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .topics()
            .define("sdk-Topics-5488")
            .withExistingNamespace("ArunMonocle", "sdk-Namespace-1617")
            .withEnableExpress(true)
            .create();
    }
}
```

### Topics_CreateOrUpdateAuthorizationRule

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.servicebus.generated.fluent.models.SBAuthorizationRuleInner;
import com.azure.resourcemanager.servicebus.generated.models.AccessRights;
import java.util.Arrays;

/** Samples for Topics CreateOrUpdateAuthorizationRule. */
public final class TopicsCreateOrUpdateAuthorizationRuleSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Topics/SBTopicAuthorizationRuleCreate.json
     */
    /**
     * Sample code: TopicAuthorizationRuleCreate.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void topicAuthorizationRuleCreate(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .topics()
            .createOrUpdateAuthorizationRuleWithResponse(
                "ArunMonocle",
                "sdk-Namespace-6261",
                "sdk-Topics-1984",
                "sdk-AuthRules-4310",
                new SBAuthorizationRuleInner().withRights(Arrays.asList(AccessRights.LISTEN, AccessRights.SEND)),
                Context.NONE);
    }
}
```

### Topics_Delete

```java
import com.azure.core.util.Context;

/** Samples for Topics Delete. */
public final class TopicsDeleteSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Topics/SBTopicDelete.json
     */
    /**
     * Sample code: TopicDelete.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void topicDelete(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager.topics().deleteWithResponse("ArunMonocle", "sdk-Namespace-1617", "sdk-Topics-5488", Context.NONE);
    }
}
```

### Topics_DeleteAuthorizationRule

```java
import com.azure.core.util.Context;

/** Samples for Topics DeleteAuthorizationRule. */
public final class TopicsDeleteAuthorizationRuleSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Topics/SBTopicAuthorizationRuleDelete.json
     */
    /**
     * Sample code: TopicAuthorizationRuleDelete.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void topicAuthorizationRuleDelete(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .topics()
            .deleteAuthorizationRuleWithResponse(
                "ArunMonocle", "sdk-Namespace-6261", "sdk-Topics-1984", "sdk-AuthRules-4310", Context.NONE);
    }
}
```

### Topics_Get

```java
import com.azure.core.util.Context;

/** Samples for Topics Get. */
public final class TopicsGetSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Topics/SBTopicGet.json
     */
    /**
     * Sample code: TopicGet.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void topicGet(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager.topics().getWithResponse("ArunMonocle", "sdk-Namespace-1617", "sdk-Topics-5488", Context.NONE);
    }
}
```

### Topics_GetAuthorizationRule

```java
import com.azure.core.util.Context;

/** Samples for Topics GetAuthorizationRule. */
public final class TopicsGetAuthorizationRuleSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Topics/SBTopicAuthorizationRuleGet.json
     */
    /**
     * Sample code: TopicAuthorizationRuleGet.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void topicAuthorizationRuleGet(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .topics()
            .getAuthorizationRuleWithResponse(
                "ArunMonocle", "sdk-Namespace-6261", "sdk-Topics-1984", "sdk-AuthRules-4310", Context.NONE);
    }
}
```

### Topics_ListAuthorizationRules

```java
import com.azure.core.util.Context;

/** Samples for Topics ListAuthorizationRules. */
public final class TopicsListAuthorizationRulesSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Topics/SBTopicAuthorizationRuleListAll.json
     */
    /**
     * Sample code: TopicAuthorizationRuleListAll.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void topicAuthorizationRuleListAll(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager.topics().listAuthorizationRules("ArunMonocle", "sdk-Namespace-6261", "sdk-Topics-1984", Context.NONE);
    }
}
```

### Topics_ListByNamespace

```java
import com.azure.core.util.Context;

/** Samples for Topics ListByNamespace. */
public final class TopicsListByNamespaceSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Topics/SBTopicListByNameSpace.json
     */
    /**
     * Sample code: TopicGet.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void topicGet(com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager.topics().listByNamespace("Default-ServiceBus-WestUS", "sdk-Namespace-1617", null, null, Context.NONE);
    }
}
```

### Topics_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for Topics ListKeys. */
public final class TopicsListKeysSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Topics/SBTopicAuthorizationRuleListKey.json
     */
    /**
     * Sample code: TopicAuthorizationRuleListKey.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void topicAuthorizationRuleListKey(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .topics()
            .listKeysWithResponse(
                "Default-ServiceBus-WestUS", "sdk-Namespace8408", "sdk-Topics2075", "sdk-Authrules5067", Context.NONE);
    }
}
```

### Topics_RegenerateKeys

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.servicebus.generated.models.KeyType;
import com.azure.resourcemanager.servicebus.generated.models.RegenerateAccessKeyParameters;

/** Samples for Topics RegenerateKeys. */
public final class TopicsRegenerateKeysSamples {
    /*
     * x-ms-original-file: specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Topics/SBTopicAuthorizationRuleRegenerateKey.json
     */
    /**
     * Sample code: TopicAuthorizationRuleRegenerateKey.
     *
     * @param manager Entry point to ServiceBusManager.
     */
    public static void topicAuthorizationRuleRegenerateKey(
        com.azure.resourcemanager.servicebus.generated.ServiceBusManager manager) {
        manager
            .topics()
            .regenerateKeysWithResponse(
                "Default-ServiceBus-WestUS",
                "sdk-Namespace8408",
                "sdk-Topics2075",
                "sdk-Authrules5067",
                new RegenerateAccessKeyParameters().withKeyType(KeyType.PRIMARY_KEY),
                Context.NONE);
    }
}
```

