# Code snippets and samples


## Accounts

- [AddRootCollectionAdmin](#accounts_addrootcollectionadmin)
- [CheckNameAvailability](#accounts_checknameavailability)
- [CreateOrUpdate](#accounts_createorupdate)
- [Delete](#accounts_delete)
- [GetByResourceGroup](#accounts_getbyresourcegroup)
- [List](#accounts_list)
- [ListByResourceGroup](#accounts_listbyresourcegroup)
- [ListKeys](#accounts_listkeys)
- [Update](#accounts_update)

## DefaultAccounts

- [Get](#defaultaccounts_get)
- [Remove](#defaultaccounts_remove)
- [Set](#defaultaccounts_set)

## Features

- [AccountGet](#features_accountget)
- [SubscriptionGet](#features_subscriptionget)

## IngestionPrivateEndpointConnections

- [List](#ingestionprivateendpointconnections_list)
- [UpdateStatus](#ingestionprivateendpointconnections_updatestatus)

## KafkaConfigurations

- [CreateOrUpdate](#kafkaconfigurations_createorupdate)
- [Delete](#kafkaconfigurations_delete)
- [Get](#kafkaconfigurations_get)
- [ListByAccount](#kafkaconfigurations_listbyaccount)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByAccount](#privateendpointconnections_listbyaccount)

## PrivateLinkResources

- [GetByGroupId](#privatelinkresources_getbygroupid)
- [ListByAccount](#privatelinkresources_listbyaccount)

## Usages

- [Get](#usages_get)
### Accounts_AddRootCollectionAdmin

```java
import com.azure.resourcemanager.purview.models.CollectionAdminUpdate;

/**
 * Samples for Accounts AddRootCollectionAdmin.
 */
public final class AccountsAddRootCollectionAdminSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/Accounts_AddRootCollectionAdmin.json
     */
    /**
     * Sample code: Accounts_AddRootCollectionAdmin.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void accountsAddRootCollectionAdmin(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.accounts()
            .addRootCollectionAdminWithResponse("SampleResourceGroup", "account1",
                new CollectionAdminUpdate().withObjectId("7e8de0e7-2bfc-4e1f-9659-2a5785e4356f"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_CheckNameAvailability

```java
import com.azure.resourcemanager.purview.models.CheckNameAvailabilityRequest;

/**
 * Samples for Accounts CheckNameAvailability.
 */
public final class AccountsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/Accounts_CheckNameAvailability.json
     */
    /**
     * Sample code: Accounts_CheckNameAvailability.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void accountsCheckNameAvailability(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.accounts()
            .checkNameAvailabilityWithResponse(
                new CheckNameAvailabilityRequest().withName("account1").withType("Microsoft.Purview/accounts"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_CreateOrUpdate

```java
/**
 * Samples for Accounts CreateOrUpdate.
 */
public final class AccountsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/Accounts_CreateOrUpdate.json
     */
    /**
     * Sample code: Accounts_CreateOrUpdate.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void accountsCreateOrUpdate(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.accounts()
            .define("account1")
            .withExistingResourceGroup("SampleResourceGroup")
            .withRegion("West US 2")
            .create();
    }
}
```

### Accounts_Delete

```java
/**
 * Samples for Accounts Delete.
 */
public final class AccountsDeleteSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/Accounts_Delete.json
     */
    /**
     * Sample code: Accounts_Delete.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void accountsDelete(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.accounts().delete("SampleResourceGroup", "account1", com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_GetByResourceGroup

```java
/**
 * Samples for Accounts GetByResourceGroup.
 */
public final class AccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/Accounts_Get.json
     */
    /**
     * Sample code: Accounts_Get.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void accountsGet(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.accounts()
            .getByResourceGroupWithResponse("SampleResourceGroup", "account1", com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_List

```java
/**
 * Samples for Accounts List.
 */
public final class AccountsListSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/Accounts_ListBySubscription.json
     */
    /**
     * Sample code: Accounts_ListBySubscription.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void accountsListBySubscription(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.accounts().list(null, com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_ListByResourceGroup

```java
/**
 * Samples for Accounts ListByResourceGroup.
 */
public final class AccountsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/Accounts_ListByResourceGroup.json
     */
    /**
     * Sample code: Accounts_ListByResourceGroup.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void accountsListByResourceGroup(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.accounts().listByResourceGroup("SampleResourceGroup", null, com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_ListKeys

```java
/**
 * Samples for Accounts ListKeys.
 */
public final class AccountsListKeysSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/Accounts_ListKeys.json
     */
    /**
     * Sample code: Accounts_ListKeys.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void accountsListKeys(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.accounts().listKeysWithResponse("SampleResourceGroup", "account1", com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_Update

```java
import com.azure.resourcemanager.purview.models.Account;
import com.azure.resourcemanager.purview.models.CloudConnectors;
import com.azure.resourcemanager.purview.models.IngestionStorage;
import com.azure.resourcemanager.purview.models.PublicNetworkAccess;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Accounts Update.
 */
public final class AccountsUpdateSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/Accounts_Update.json
     */
    /**
     * Sample code: Accounts_Update.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void accountsUpdate(com.azure.resourcemanager.purview.PurviewManager manager) {
        Account resource = manager.accounts()
            .getByResourceGroupWithResponse("SampleResourceGroup", "account1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("newTag", "New tag value."))
            .withCloudConnectors(new CloudConnectors())
            .withIngestionStorage(new IngestionStorage().withPublicNetworkAccess(PublicNetworkAccess.DISABLED))
            .withManagedResourcesPublicNetworkAccess(PublicNetworkAccess.DISABLED)
            .withPublicNetworkAccess(PublicNetworkAccess.DISABLED)
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

### DefaultAccounts_Get

```java
import com.azure.resourcemanager.purview.models.ScopeType;

/**
 * Samples for DefaultAccounts Get.
 */
public final class DefaultAccountsGetSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/DefaultAccounts_Get.json
     */
    /**
     * Sample code: DefaultAccounts_Get.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void defaultAccountsGet(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.defaultAccounts()
            .getWithResponse("ee85a74c-405e-4adc-bb47-ffa8ca0c9f31", ScopeType.TENANT,
                "12345678-1234-1234-1234-12345678abcd", com.azure.core.util.Context.NONE);
    }
}
```

### DefaultAccounts_Remove

```java
import com.azure.resourcemanager.purview.models.ScopeType;

/**
 * Samples for DefaultAccounts Remove.
 */
public final class DefaultAccountsRemoveSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/DefaultAccounts_Remove.json
     */
    /**
     * Sample code: DefaultAccounts_Remove.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void defaultAccountsRemove(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.defaultAccounts()
            .removeWithResponse("ee85a74c-405e-4adc-bb47-ffa8ca0c9f31", ScopeType.TENANT,
                "12345678-1234-1234-1234-12345678abcd", com.azure.core.util.Context.NONE);
    }
}
```

### DefaultAccounts_Set

```java
import com.azure.resourcemanager.purview.fluent.models.DefaultAccountPayloadInner;
import com.azure.resourcemanager.purview.models.ScopeType;

/**
 * Samples for DefaultAccounts Set.
 */
public final class DefaultAccountsSetSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/DefaultAccounts_Set.json
     */
    /**
     * Sample code: DefaultAccounts_Set.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void defaultAccountsSet(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.defaultAccounts()
            .setWithResponse(new DefaultAccountPayloadInner().withAccountName("myDefaultAccount")
                .withResourceGroupName("rg-1")
                .withScope("12345678-1234-1234-1234-12345678abcd")
                .withScopeTenantId("12345678-1234-1234-1234-12345678abcd")
                .withScopeType(ScopeType.TENANT)
                .withSubscriptionId("12345678-1234-1234-1234-12345678aaaa"), com.azure.core.util.Context.NONE);
    }
}
```

### Features_AccountGet

```java
import com.azure.resourcemanager.purview.models.BatchFeatureRequest;
import java.util.Arrays;

/**
 * Samples for Features AccountGet.
 */
public final class FeaturesAccountGetSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/Features_AccountGet.json
     */
    /**
     * Sample code: Features_AccountGet.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void featuresAccountGet(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.features()
            .accountGetWithResponse("SampleResourceGroup", "account1",
                new BatchFeatureRequest().withFeatures(Arrays.asList("Feature1", "Feature2", "FeatureThatDoesntExist")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Features_SubscriptionGet

```java
import com.azure.resourcemanager.purview.models.BatchFeatureRequest;
import java.util.Arrays;

/**
 * Samples for Features SubscriptionGet.
 */
public final class FeaturesSubscriptionGetSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/Features_SubscriptionGet.json
     */
    /**
     * Sample code: Features_SubscriptionGet.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void featuresSubscriptionGet(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.features()
            .subscriptionGetWithResponse("eastus",
                new BatchFeatureRequest().withFeatures(Arrays.asList("Feature1", "Feature2", "FeatureThatDoesntExist")),
                com.azure.core.util.Context.NONE);
    }
}
```

### IngestionPrivateEndpointConnections_List

```java
/**
 * Samples for IngestionPrivateEndpointConnections List.
 */
public final class IngestionPrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/IngestionPrivateEndpointConnections_List.json
     */
    /**
     * Sample code: IngestionPrivateEndpointConnections_List.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void
        ingestionPrivateEndpointConnectionsList(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.ingestionPrivateEndpointConnections()
            .list("SampleResourceGroup", "account1", com.azure.core.util.Context.NONE);
    }
}
```

### IngestionPrivateEndpointConnections_UpdateStatus

```java
import com.azure.resourcemanager.purview.models.PrivateEndpointConnectionStatusUpdateRequest;

/**
 * Samples for IngestionPrivateEndpointConnections UpdateStatus.
 */
public final class IngestionPrivateEndpointConnectionsUpdateStatusSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/IngestionPrivateEndpointConnections_UpdateStatus.json
     */
    /**
     * Sample code: IngestionPrivateEndpointConnections_UpdateStatus.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void
        ingestionPrivateEndpointConnectionsUpdateStatus(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.ingestionPrivateEndpointConnections()
            .updateStatusWithResponse("SampleResourceGroup", "account1",
                new PrivateEndpointConnectionStatusUpdateRequest().withPrivateEndpointId(
                    "/subscriptions/12345678-1234-1234-12345678abc/resourceGroups/SampleResourceGroup/providers/Microsoft.Purview/accounts/account1/privateEndpointConnections/privateEndpointConnection1")
                    .withStatus("Approved"),
                com.azure.core.util.Context.NONE);
    }
}
```

### KafkaConfigurations_CreateOrUpdate

```java
import com.azure.resourcemanager.purview.models.Credentials;
import com.azure.resourcemanager.purview.models.EventHubType;
import com.azure.resourcemanager.purview.models.EventStreamingState;
import com.azure.resourcemanager.purview.models.EventStreamingType;
import com.azure.resourcemanager.purview.models.KafkaConfigurationIdentityType;

/**
 * Samples for KafkaConfigurations CreateOrUpdate.
 */
public final class KafkaConfigurationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/KafkaConfigurations_CreateOrUpdate.json
     */
    /**
     * Sample code: KafkaConfigurations_CreateOrUpdate.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void kafkaConfigurationsCreateOrUpdate(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.kafkaConfigurations()
            .define("kafkaConfigName")
            .withExistingAccount("rgpurview", "account1")
            .withConsumerGroup("consumerGroup")
            .withCredentials(new Credentials().withIdentityId(
                "/subscriptions/47e8596d-ee73-4eb2-b6b4-cc13c2b87ssd/resourceGroups/testRG/providers/Microsoft.ManagedIdentity/userAssignedIdentities/testId")
                .withType(KafkaConfigurationIdentityType.USER_ASSIGNED))
            .withEventHubPartitionId("partitionId")
            .withEventHubResourceId(
                "/subscriptions/225be6fe-ec1c-4d51-a368-f69348d2e6c5/resourceGroups/testRG/providers/Microsoft.EventHub/namespaces/eventHubNameSpaceName")
            .withEventHubType(EventHubType.NOTIFICATION)
            .withEventStreamingState(EventStreamingState.ENABLED)
            .withEventStreamingType(EventStreamingType.AZURE)
            .create();
    }
}
```

### KafkaConfigurations_Delete

```java
/**
 * Samples for KafkaConfigurations Delete.
 */
public final class KafkaConfigurationsDeleteSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/KafkaConfigurations_Delete.json
     */
    /**
     * Sample code: KafkaConfigurations_Delete.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void kafkaConfigurationsDelete(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.kafkaConfigurations()
            .deleteWithResponse("rgpurview", "account1", "kafkaConfigName", com.azure.core.util.Context.NONE);
    }
}
```

### KafkaConfigurations_Get

```java
/**
 * Samples for KafkaConfigurations Get.
 */
public final class KafkaConfigurationsGetSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/KafkaConfigurations_Get.json
     */
    /**
     * Sample code: KafkaConfigurations_Get.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void kafkaConfigurationsGet(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.kafkaConfigurations()
            .getWithResponse("rgpurview", "account1", "kafkaConfigName", com.azure.core.util.Context.NONE);
    }
}
```

### KafkaConfigurations_ListByAccount

```java
/**
 * Samples for KafkaConfigurations ListByAccount.
 */
public final class KafkaConfigurationsListByAccountSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/KafkaConfigurations_ListByAccount.json
     */
    /**
     * Sample code: KafkaConfigurations_ListByAccount.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void kafkaConfigurationsListByAccount(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.kafkaConfigurations().listByAccount("rgpurview", "account1", "token", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2024-04-01-preview/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void operationsList(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.purview.models.PrivateLinkServiceConnectionState;
import com.azure.resourcemanager.purview.models.Status;

/**
 * Samples for PrivateEndpointConnections CreateOrUpdate.
 */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/PrivateEndpointConnections_CreateOrUpdate.json
     */
    /**
     * Sample code: PrivateEndpointConnections_CreateOrUpdate.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void
        privateEndpointConnectionsCreateOrUpdate(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.privateEndpointConnections()
            .define("privateEndpointConnection1")
            .withExistingAccount("SampleResourceGroup", "account1")
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState().withDescription("Approved by johndoe@company.com")
                    .withStatus(Status.APPROVED))
            .create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
/**
 * Samples for PrivateEndpointConnections Delete.
 */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/PrivateEndpointConnections_Delete.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Delete.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void privateEndpointConnectionsDelete(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.privateEndpointConnections()
            .delete("SampleResourceGroup", "account1", "privateEndpointConnection1", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
/**
 * Samples for PrivateEndpointConnections Get.
 */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/PrivateEndpointConnections_Get.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Get.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void privateEndpointConnectionsGet(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.privateEndpointConnections()
            .getWithResponse("SampleResourceGroup", "account1", "privateEndpointConnection1",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByAccount

```java
/**
 * Samples for PrivateEndpointConnections ListByAccount.
 */
public final class PrivateEndpointConnectionsListByAccountSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/PrivateEndpointConnections_ListByAccount.json
     */
    /**
     * Sample code: PrivateEndpointConnections_ListByAccount.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void
        privateEndpointConnectionsListByAccount(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.privateEndpointConnections()
            .listByAccount("SampleResourceGroup", "account1", null, com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_GetByGroupId

```java
/**
 * Samples for PrivateLinkResources GetByGroupId.
 */
public final class PrivateLinkResourcesGetByGroupIdSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/PrivateLinkResources_GetByGroupId.json
     */
    /**
     * Sample code: PrivateLinkResources_GetByGroupId.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void privateLinkResourcesGetByGroupId(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.privateLinkResources()
            .getByGroupIdWithResponse("SampleResourceGroup", "account1", "group1", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_ListByAccount

```java
/**
 * Samples for PrivateLinkResources ListByAccount.
 */
public final class PrivateLinkResourcesListByAccountSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/PrivateLinkResources_ListByAccount.json
     */
    /**
     * Sample code: PrivateLinkResources_ListByAccount.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void privateLinkResourcesListByAccount(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.privateLinkResources()
            .listByAccount("SampleResourceGroup", "account1", com.azure.core.util.Context.NONE);
    }
}
```

### Usages_Get

```java
/**
 * Samples for Usages Get.
 */
public final class UsagesGetSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/Usages_Get.json
     */
    /**
     * Sample code: Usages_Get.
     * 
     * @param manager Entry point to PurviewManager.
     */
    public static void usagesGet(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.usages().getWithResponse("West US 2", null, com.azure.core.util.Context.NONE);
    }
}
```

