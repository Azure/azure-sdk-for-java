# Code snippets and samples


## ConfigurationStores

- [Create](#configurationstores_create)
- [Delete](#configurationstores_delete)
- [GetByResourceGroup](#configurationstores_getbyresourcegroup)
- [GetDeleted](#configurationstores_getdeleted)
- [List](#configurationstores_list)
- [ListByResourceGroup](#configurationstores_listbyresourcegroup)
- [ListDeleted](#configurationstores_listdeleted)
- [ListKeys](#configurationstores_listkeys)
- [PurgeDeleted](#configurationstores_purgedeleted)
- [RegenerateKey](#configurationstores_regeneratekey)
- [Update](#configurationstores_update)

## KeyValues

- [CreateOrUpdate](#keyvalues_createorupdate)
- [Delete](#keyvalues_delete)
- [Get](#keyvalues_get)

## Operations

- [CheckNameAvailability](#operations_checknameavailability)
- [List](#operations_list)
- [RegionalCheckNameAvailability](#operations_regionalchecknameavailability)

## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByConfigurationStore](#privateendpointconnections_listbyconfigurationstore)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [ListByConfigurationStore](#privatelinkresources_listbyconfigurationstore)

## Replicas

- [Create](#replicas_create)
- [Delete](#replicas_delete)
- [Get](#replicas_get)
- [ListByConfigurationStore](#replicas_listbyconfigurationstore)

## Snapshots

- [Create](#snapshots_create)
- [Get](#snapshots_get)
### ConfigurationStores_Create

```java
/**
 * Samples for PrivateEndpointConnections ListByConfigurationStore.
 */
public final class PrivateEndpointConnectionsListByConfigurationStoreSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresListPrivateEndpointConnections.json
     */
    /**
     * Sample code: PrivateEndpointConnection_List.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void
        privateEndpointConnectionList(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.privateEndpointConnections()
            .listByConfigurationStore("myResourceGroup", "contoso", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationStores_Delete

```java
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for KeyValues CreateOrUpdate.
 */
public final class KeyValuesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresCreateKeyValue.json
     */
    /**
     * Sample code: KeyValues_CreateOrUpdate.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void
        keyValuesCreateOrUpdate(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.keyValues()
            .define("myKey$myLabel")
            .withExistingConfigurationStore("myResourceGroup", "contoso")
            .withTags(mapOf("tag1", "tagValue1", "tag2", "tagValue2"))
            .withValue("myValue")
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

### ConfigurationStores_GetByResourceGroup

```java
/**
 * Samples for ConfigurationStores GetByResourceGroup.
 */
public final class ConfigurationStoresGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresGet.json
     */
    /**
     * Sample code: ConfigurationStores_Get.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void
        configurationStoresGet(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.configurationStores()
            .getByResourceGroupWithResponse("myResourceGroup", "contoso", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationStores_GetDeleted

```java
/**
 * Samples for ConfigurationStores PurgeDeleted.
 */
public final class ConfigurationStoresPurgeDeletedSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * DeletedConfigurationStoresPurge.json
     */
    /**
     * Sample code: Purge a deleted configuration store.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void
        purgeADeletedConfigurationStore(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.configurationStores().purgeDeleted("westus", "contoso", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationStores_List

```java
/**
 * Samples for ConfigurationStores List.
 */
public final class ConfigurationStoresListSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresList.json
     */
    /**
     * Sample code: ConfigurationStores_List.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void
        configurationStoresList(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.configurationStores().list(null, com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationStores_ListByResourceGroup

```java
/**
 * Samples for KeyValues Delete.
 */
public final class KeyValuesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresDeleteKeyValue.json
     */
    /**
     * Sample code: KeyValues_Delete.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void keyValuesDelete(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.keyValues().delete("myResourceGroup", "contoso", "myKey$myLabel", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationStores_ListDeleted

```java
/**
 * Samples for ConfigurationStores GetDeleted.
 */
public final class ConfigurationStoresGetDeletedSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * DeletedConfigurationStoresGet.json
     */
    /**
     * Sample code: DeletedConfigurationStores_Get.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void
        deletedConfigurationStoresGet(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.configurationStores().getDeletedWithResponse("westus", "contoso", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationStores_ListKeys

```java
import com.azure.resourcemanager.appconfiguration.models.ConfigurationStore;
import com.azure.resourcemanager.appconfiguration.models.IdentityType;
import com.azure.resourcemanager.appconfiguration.models.ResourceIdentity;
import com.azure.resourcemanager.appconfiguration.models.Sku;
import com.azure.resourcemanager.appconfiguration.models.UserIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ConfigurationStores Update.
 */
public final class ConfigurationStoresUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresUpdateDisableLocalAuth.json
     */
    /**
     * Sample code: ConfigurationStores_Update_Disable_Local_Auth.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void configurationStoresUpdateDisableLocalAuth(
        com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        ConfigurationStore resource = manager.configurationStores()
            .getByResourceGroupWithResponse("myResourceGroup", "contoso", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withSku(new Sku().withName("Standard")).withDisableLocalAuth(true).apply();
    }

    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresUpdate.json
     */
    /**
     * Sample code: ConfigurationStores_Update.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void
        configurationStoresUpdate(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        ConfigurationStore resource = manager.configurationStores()
            .getByResourceGroupWithResponse("myResourceGroup", "contoso", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("Category", "Marketing")).withSku(new Sku().withName("Standard")).apply();
    }

    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresUpdateWithIdentity.json
     */
    /**
     * Sample code: ConfigurationStores_Update_With_Identity.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void configurationStoresUpdateWithIdentity(
        com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        ConfigurationStore resource = manager.configurationStores()
            .getByResourceGroupWithResponse("myResourceGroup", "contoso", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("Category", "Marketing"))
            .withIdentity(new ResourceIdentity().withType(IdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/c80fb759-c965-4c6a-9110-9b2b2d038882/resourcegroups/myResourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identity2",
                    new UserIdentity())))
            .withSku(new Sku().withName("Standard"))
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

### ConfigurationStores_PurgeDeleted

```java
import com.azure.resourcemanager.appconfiguration.models.ConnectionStatus;
import com.azure.resourcemanager.appconfiguration.models.PrivateEndpointConnection;
import com.azure.resourcemanager.appconfiguration.models.PrivateLinkServiceConnectionState;

/**
 * Samples for PrivateEndpointConnections CreateOrUpdate.
 */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresUpdatePrivateEndpointConnection.json
     */
    /**
     * Sample code: PrivateEndpointConnection_Update.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void
        privateEndpointConnectionUpdate(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        PrivateEndpointConnection resource = manager.privateEndpointConnections()
            .getWithResponse("myResourceGroup", "contoso", "myConnection", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState().withStatus(ConnectionStatus.APPROVED)
                    .withDescription("Auto-Approved"))
            .apply();
    }
}
```

### ConfigurationStores_RegenerateKey

```java
import com.azure.resourcemanager.appconfiguration.models.RegenerateKeyParameters;

/**
 * Samples for ConfigurationStores RegenerateKey.
 */
public final class ConfigurationStoresRegenerateKeySamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresRegenerateKey.json
     */
    /**
     * Sample code: ConfigurationStores_RegenerateKey.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void
        configurationStoresRegenerateKey(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.configurationStores()
            .regenerateKeyWithResponse("myResourceGroup", "contoso",
                new RegenerateKeyParameters().withId("439AD01B4BE67DB1"), com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationStores_Update

```java
/**
 * Samples for Replicas Delete.
 */
public final class ReplicasDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresDeleteReplica.json
     */
    /**
     * Sample code: Replicas_Delete.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void replicasDelete(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.replicas().delete("myResourceGroup", "contoso", "myReplicaEus", com.azure.core.util.Context.NONE);
    }
}
```

### KeyValues_CreateOrUpdate

```java
/**
 * Samples for ConfigurationStores ListDeleted.
 */
public final class ConfigurationStoresListDeletedSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * DeletedConfigurationStoresList.json
     */
    /**
     * Sample code: DeletedConfigurationStores_List.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void
        deletedConfigurationStoresList(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.configurationStores().listDeleted(com.azure.core.util.Context.NONE);
    }
}
```

### KeyValues_Delete

```java
/**
 * Samples for PrivateEndpointConnections Get.
 */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresGetPrivateEndpointConnection.json
     */
    /**
     * Sample code: PrivateEndpointConnection_GetConnection.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void privateEndpointConnectionGetConnection(
        com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.privateEndpointConnections()
            .getWithResponse("myResourceGroup", "contoso", "myConnection", com.azure.core.util.Context.NONE);
    }
}
```

### KeyValues_Get

```java
/**
 * Samples for Replicas Create.
 */
public final class ReplicasCreateSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresCreateReplica.json
     */
    /**
     * Sample code: Replicas_Create.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void replicasCreate(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.replicas()
            .define("myReplicaEus")
            .withExistingConfigurationStore("myResourceGroup", "contoso")
            .withRegion("eastus")
            .create();
    }
}
```

### Operations_CheckNameAvailability

```java
import com.azure.resourcemanager.appconfiguration.models.CheckNameAvailabilityParameters;
import com.azure.resourcemanager.appconfiguration.models.ConfigurationResourceType;

/**
 * Samples for Operations CheckNameAvailability.
 */
public final class OperationsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * CheckNameAvailable.json
     */
    /**
     * Sample code: ConfigurationStores_CheckNameAvailable.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void configurationStoresCheckNameAvailable(
        com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.operations()
            .checkNameAvailabilityWithResponse(
                new CheckNameAvailabilityParameters().withName("contoso")
                    .withType(ConfigurationResourceType.MICROSOFT_APP_CONFIGURATION_CONFIGURATION_STORES),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * CheckNameNotAvailable.json
     */
    /**
     * Sample code: ConfigurationStores_CheckNameNotAvailable.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void configurationStoresCheckNameNotAvailable(
        com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.operations()
            .checkNameAvailabilityWithResponse(
                new CheckNameAvailabilityParameters().withName("contoso")
                    .withType(ConfigurationResourceType.MICROSOFT_APP_CONFIGURATION_CONFIGURATION_STORES),
                com.azure.core.util.Context.NONE);
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
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * OperationsList.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void operationsList(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.operations().list(null, com.azure.core.util.Context.NONE);
    }
}
```

### Operations_RegionalCheckNameAvailability

```java
/**
 * Samples for Replicas Get.
 */
public final class ReplicasGetSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresGetReplica.json
     */
    /**
     * Sample code: Replicas_Get.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void replicasGet(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.replicas()
            .getWithResponse("myResourceGroup", "contoso", "myReplicaEus", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.appconfiguration.models.KeyValueFilter;
import java.util.Arrays;

/**
 * Samples for Snapshots Create.
 */
public final class SnapshotsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresCreateSnapshot.json
     */
    /**
     * Sample code: Snapshots_Create.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void snapshotsCreate(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.snapshots()
            .define("mySnapshot")
            .withExistingConfigurationStore("myResourceGroup", "contoso")
            .withFilters(Arrays.asList(new KeyValueFilter().withKey("fakeTokenPlaceholder").withLabel("Production")))
            .withRetentionPeriod(3600L)
            .create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
/**
 * Samples for Replicas ListByConfigurationStore.
 */
public final class ReplicasListByConfigurationStoreSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresListReplicas.json
     */
    /**
     * Sample code: Replicas_ListByConfigurationStore.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void
        replicasListByConfigurationStore(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.replicas()
            .listByConfigurationStore("myResourceGroup", "contoso", null, com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
/**
 * Samples for PrivateLinkResources Get.
 */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * PrivateLinkResourceGet.json
     */
    /**
     * Sample code: PrivateLinkResources_Get.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void
        privateLinkResourcesGet(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.privateLinkResources()
            .getWithResponse("myResourceGroup", "contoso", "configurationStores", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByConfigurationStore

```java
/**
 * Samples for PrivateEndpointConnections Delete.
 */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresDeletePrivateEndpointConnection.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Delete.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void
        privateEndpointConnectionsDelete(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.privateEndpointConnections()
            .delete("myResourceGroup", "contoso", "myConnection", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
/**
 * Samples for ConfigurationStores Delete.
 */
public final class ConfigurationStoresDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresDelete.json
     */
    /**
     * Sample code: ConfigurationStores_Delete.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void
        configurationStoresDelete(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.configurationStores().delete("myResourceGroup", "contoso", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_ListByConfigurationStore

```java
/**
 * Samples for ConfigurationStores ListByResourceGroup.
 */
public final class ConfigurationStoresListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresListByResourceGroup.json
     */
    /**
     * Sample code: ConfigurationStores_ListByResourceGroup.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void configurationStoresListByResourceGroup(
        com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.configurationStores().listByResourceGroup("myResourceGroup", null, com.azure.core.util.Context.NONE);
    }
}
```

### Replicas_Create

```java
/**
 * Samples for KeyValues Get.
 */
public final class KeyValuesGetSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresGetKeyValue.json
     */
    /**
     * Sample code: KeyValues_Get.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void keyValuesGet(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.keyValues()
            .getWithResponse("myResourceGroup", "contoso", "myKey$myLabel", com.azure.core.util.Context.NONE);
    }
}
```

### Replicas_Delete

```java
import com.azure.resourcemanager.appconfiguration.models.AuthenticationMode;
import com.azure.resourcemanager.appconfiguration.models.DataPlaneProxyProperties;
import com.azure.resourcemanager.appconfiguration.models.IdentityType;
import com.azure.resourcemanager.appconfiguration.models.PrivateLinkDelegation;
import com.azure.resourcemanager.appconfiguration.models.ResourceIdentity;
import com.azure.resourcemanager.appconfiguration.models.Sku;
import com.azure.resourcemanager.appconfiguration.models.UserIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ConfigurationStores Create.
 */
public final class ConfigurationStoresCreateSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresCreate.json
     */
    /**
     * Sample code: ConfigurationStores_Create.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void
        configurationStoresCreate(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.configurationStores()
            .define("contoso")
            .withRegion("westus")
            .withExistingResourceGroup("myResourceGroup")
            .withSku(new Sku().withName("Standard"))
            .withTags(mapOf("myTag", "myTagValue"))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresCreateWithIdentity.json
     */
    /**
     * Sample code: ConfigurationStores_Create_With_Identity.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void configurationStoresCreateWithIdentity(
        com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.configurationStores()
            .define("contoso")
            .withRegion("westus")
            .withExistingResourceGroup("myResourceGroup")
            .withSku(new Sku().withName("Standard"))
            .withTags(mapOf("myTag", "myTagValue"))
            .withIdentity(new ResourceIdentity().withType(IdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/c80fb759-c965-4c6a-9110-9b2b2d038882/resourcegroups/myResourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identity2",
                    new UserIdentity())))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresCreateWithDataPlaneProxy.json
     */
    /**
     * Sample code: ConfigurationStores_Create_With_Data_Plane_Proxy.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void configurationStoresCreateWithDataPlaneProxy(
        com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.configurationStores()
            .define("contoso")
            .withRegion("westus")
            .withExistingResourceGroup("myResourceGroup")
            .withSku(new Sku().withName("Standard"))
            .withDataPlaneProxy(new DataPlaneProxyProperties().withAuthenticationMode(AuthenticationMode.PASS_THROUGH)
                .withPrivateLinkDelegation(PrivateLinkDelegation.ENABLED))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresCreateWithLocalAuthDisabled.json
     */
    /**
     * Sample code: ConfigurationStores_Create_With_Local_Auth_Disabled.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void configurationStoresCreateWithLocalAuthDisabled(
        com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.configurationStores()
            .define("contoso")
            .withRegion("westus")
            .withExistingResourceGroup("myResourceGroup")
            .withSku(new Sku().withName("Standard"))
            .withDisableLocalAuth(true)
            .withDataPlaneProxy(new DataPlaneProxyProperties().withAuthenticationMode(AuthenticationMode.PASS_THROUGH)
                .withPrivateLinkDelegation(PrivateLinkDelegation.DISABLED))
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

### Replicas_Get

```java
import com.azure.resourcemanager.appconfiguration.models.CheckNameAvailabilityParameters;
import com.azure.resourcemanager.appconfiguration.models.ConfigurationResourceType;

/**
 * Samples for Operations RegionalCheckNameAvailability.
 */
public final class OperationsRegionalCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * RegionalCheckNameAvailable.json
     */
    /**
     * Sample code: ConfigurationStores_CheckNameAvailable.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void configurationStoresCheckNameAvailable(
        com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.operations()
            .regionalCheckNameAvailabilityWithResponse("westus",
                new CheckNameAvailabilityParameters().withName("contoso")
                    .withType(ConfigurationResourceType.MICROSOFT_APP_CONFIGURATION_CONFIGURATION_STORES),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * RegionalCheckNameNotAvailable.json
     */
    /**
     * Sample code: ConfigurationStores_CheckNameNotAvailable.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void configurationStoresCheckNameNotAvailable(
        com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.operations()
            .regionalCheckNameAvailabilityWithResponse("westus",
                new CheckNameAvailabilityParameters().withName("contoso")
                    .withType(ConfigurationResourceType.MICROSOFT_APP_CONFIGURATION_CONFIGURATION_STORES),
                com.azure.core.util.Context.NONE);
    }
}
```

### Replicas_ListByConfigurationStore

```java
/**
 * Samples for Snapshots Get.
 */
public final class SnapshotsGetSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresGetSnapshot.json
     */
    /**
     * Sample code: Snapshots_Get.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void snapshotsGet(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.snapshots()
            .getWithResponse("myResourceGroup", "contoso", "mySnapshot", com.azure.core.util.Context.NONE);
    }
}
```

### Snapshots_Create

```java
/**
 * Samples for ConfigurationStores ListKeys.
 */
public final class ConfigurationStoresListKeysSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * ConfigurationStoresListKeys.json
     */
    /**
     * Sample code: ConfigurationStores_ListKeys.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void
        configurationStoresListKeys(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.configurationStores().listKeys("myResourceGroup", "contoso", null, com.azure.core.util.Context.NONE);
    }
}
```

### Snapshots_Get

```java
/**
 * Samples for PrivateLinkResources ListByConfigurationStore.
 */
public final class PrivateLinkResourcesListByConfigurationStoreSamples {
    /*
     * x-ms-original-file:
     * specification/appconfiguration/resource-manager/Microsoft.AppConfiguration/stable/2024-06-01/examples/
     * PrivateLinkResourcesListByConfigurationStore.json
     */
    /**
     * Sample code: PrivateLinkResources_ListGroupIds.
     * 
     * @param manager Entry point to AppConfigurationManager.
     */
    public static void
        privateLinkResourcesListGroupIds(com.azure.resourcemanager.appconfiguration.AppConfigurationManager manager) {
        manager.privateLinkResources()
            .listByConfigurationStore("myResourceGroup", "contoso", com.azure.core.util.Context.NONE);
    }
}
```

