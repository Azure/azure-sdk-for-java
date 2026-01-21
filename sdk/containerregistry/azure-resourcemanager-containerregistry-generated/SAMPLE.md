# Code snippets and samples


## CacheRules

- [Create](#cacherules_create)
- [Delete](#cacherules_delete)
- [Get](#cacherules_get)
- [List](#cacherules_list)
- [Update](#cacherules_update)

## ConnectedRegistries

- [Create](#connectedregistries_create)
- [Deactivate](#connectedregistries_deactivate)
- [Delete](#connectedregistries_delete)
- [Get](#connectedregistries_get)
- [List](#connectedregistries_list)
- [Update](#connectedregistries_update)

## CredentialSets

- [Create](#credentialsets_create)
- [Delete](#credentialsets_delete)
- [Get](#credentialsets_get)
- [List](#credentialsets_list)
- [Update](#credentialsets_update)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [List](#privateendpointconnections_list)

## Registries

- [CheckNameAvailability](#registries_checknameavailability)
- [Create](#registries_create)
- [Delete](#registries_delete)
- [GenerateCredentials](#registries_generatecredentials)
- [GetByResourceGroup](#registries_getbyresourcegroup)
- [GetPrivateLinkResource](#registries_getprivatelinkresource)
- [ImportImage](#registries_importimage)
- [List](#registries_list)
- [ListByResourceGroup](#registries_listbyresourcegroup)
- [ListCredentials](#registries_listcredentials)
- [ListPrivateLinkResources](#registries_listprivatelinkresources)
- [ListUsages](#registries_listusages)
- [RegenerateCredential](#registries_regeneratecredential)
- [Update](#registries_update)

## Replications

- [Create](#replications_create)
- [Delete](#replications_delete)
- [Get](#replications_get)
- [List](#replications_list)
- [Update](#replications_update)

## ScopeMaps

- [Create](#scopemaps_create)
- [Delete](#scopemaps_delete)
- [Get](#scopemaps_get)
- [List](#scopemaps_list)
- [Update](#scopemaps_update)

## Tokens

- [Create](#tokens_create)
- [Delete](#tokens_delete)
- [Get](#tokens_get)
- [List](#tokens_list)
- [Update](#tokens_update)

## Webhooks

- [Create](#webhooks_create)
- [Delete](#webhooks_delete)
- [Get](#webhooks_get)
- [GetCallbackConfig](#webhooks_getcallbackconfig)
- [List](#webhooks_list)
- [ListEvents](#webhooks_listevents)
- [Ping](#webhooks_ping)
- [Update](#webhooks_update)
### CacheRules_Create

```java
import com.azure.resourcemanager.containerregistry.generated.models.CacheRuleProperties;

/**
 * Samples for CacheRules Create.
 */
public final class CacheRulesCreateSamples {
    /*
     * x-ms-original-file: 2025-11-01/CacheRuleCreate.json
     */
    /**
     * Sample code: CacheRuleCreate.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        cacheRuleCreate(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.cacheRules()
            .define("myCacheRule")
            .withExistingRegistry("myResourceGroup", "myRegistry")
            .withProperties(new CacheRuleProperties().withCredentialSetResourceId("fakeTokenPlaceholder")
                .withSourceRepository("docker.io/library/hello-world")
                .withTargetRepository("cached-docker-hub/hello-world"))
            .create();
    }
}
```

### CacheRules_Delete

```java
/**
 * Samples for CacheRules Delete.
 */
public final class CacheRulesDeleteSamples {
    /*
     * x-ms-original-file: 2025-11-01/CacheRuleDelete.json
     */
    /**
     * Sample code: CacheRuleDelete.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        cacheRuleDelete(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.cacheRules().delete("myResourceGroup", "myRegistry", "myCacheRule", com.azure.core.util.Context.NONE);
    }
}
```

### CacheRules_Get

```java
/**
 * Samples for CacheRules Get.
 */
public final class CacheRulesGetSamples {
    /*
     * x-ms-original-file: 2025-11-01/CacheRuleGet.json
     */
    /**
     * Sample code: CacheRuleGet.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        cacheRuleGet(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.cacheRules()
            .getWithResponse("myResourceGroup", "myRegistry", "myCacheRule", com.azure.core.util.Context.NONE);
    }
}
```

### CacheRules_List

```java
/**
 * Samples for CacheRules List.
 */
public final class CacheRulesListSamples {
    /*
     * x-ms-original-file: 2025-11-01/CacheRuleList.json
     */
    /**
     * Sample code: CacheRuleList.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        cacheRuleList(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.cacheRules().list("myResourceGroup", "myRegistry", com.azure.core.util.Context.NONE);
    }
}
```

### CacheRules_Update

```java
import com.azure.resourcemanager.containerregistry.generated.models.CacheRule;

/**
 * Samples for CacheRules Update.
 */
public final class CacheRulesUpdateSamples {
    /*
     * x-ms-original-file: 2025-11-01/CacheRuleUpdate.json
     */
    /**
     * Sample code: CacheRuleUpdate.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        cacheRuleUpdate(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        CacheRule resource = manager.cacheRules()
            .getWithResponse("myResourceGroup", "myRegistry", "myCacheRule", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withCredentialSetResourceId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.ContainerRegistry/registries/myRegistry/credentialSets/myCredentialSet2")
            .apply();
    }
}
```

### ConnectedRegistries_Create

```java
import com.azure.resourcemanager.containerregistry.generated.models.ConnectedRegistryMode;
import com.azure.resourcemanager.containerregistry.generated.models.ConnectedRegistryProperties;
import com.azure.resourcemanager.containerregistry.generated.models.GarbageCollectionProperties;
import com.azure.resourcemanager.containerregistry.generated.models.ParentProperties;
import com.azure.resourcemanager.containerregistry.generated.models.SyncProperties;
import java.time.Duration;
import java.util.Arrays;

/**
 * Samples for ConnectedRegistries Create.
 */
public final class ConnectedRegistriesCreateSamples {
    /*
     * x-ms-original-file: 2025-11-01/ConnectedRegistryCreate.json
     */
    /**
     * Sample code: ConnectedRegistryCreate.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void connectedRegistryCreate(
        com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.connectedRegistries()
            .define("myConnectedRegistry")
            .withExistingRegistry("myResourceGroup", "myRegistry")
            .withProperties(new ConnectedRegistryProperties().withMode(ConnectedRegistryMode.READ_WRITE)
                .withParent(
                    new ParentProperties().withSyncProperties(new SyncProperties().withTokenId("fakeTokenPlaceholder")
                        .withSchedule("0 9 * * *")
                        .withSyncWindow(Duration.parse("PT3H"))
                        .withMessageTtl(Duration.parse("P2D"))))
                .withClientTokenIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.ContainerRegistry/registries/myRegistry/tokens/client1Token"))
                .withNotificationsList(Arrays.asList("hello-world:*:*", "sample/repo/*:1.0:*"))
                .withGarbageCollection(new GarbageCollectionProperties().withEnabled(true).withSchedule("0 5 * * *")))
            .create();
    }
}
```

### ConnectedRegistries_Deactivate

```java
/**
 * Samples for ConnectedRegistries Deactivate.
 */
public final class ConnectedRegistriesDeactivateSamples {
    /*
     * x-ms-original-file: 2025-11-01/ConnectedRegistryDeactivate.json
     */
    /**
     * Sample code: ConnectedRegistryDeactivate.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void connectedRegistryDeactivate(
        com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.connectedRegistries()
            .deactivate("myResourceGroup", "myRegistry", "myConnectedRegistry", com.azure.core.util.Context.NONE);
    }
}
```

### ConnectedRegistries_Delete

```java
/**
 * Samples for ConnectedRegistries Delete.
 */
public final class ConnectedRegistriesDeleteSamples {
    /*
     * x-ms-original-file: 2025-11-01/ConnectedRegistryDelete.json
     */
    /**
     * Sample code: ConnectedRegistryDelete.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void connectedRegistryDelete(
        com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.connectedRegistries()
            .delete("myResourceGroup", "myRegistry", "myConnectedRegistry", com.azure.core.util.Context.NONE);
    }
}
```

### ConnectedRegistries_Get

```java
/**
 * Samples for ConnectedRegistries Get.
 */
public final class ConnectedRegistriesGetSamples {
    /*
     * x-ms-original-file: 2025-11-01/ConnectedRegistryGet.json
     */
    /**
     * Sample code: ConnectedRegistryGet.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        connectedRegistryGet(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.connectedRegistries()
            .getWithResponse("myResourceGroup", "myRegistry", "myConnectedRegistry", com.azure.core.util.Context.NONE);
    }
}
```

### ConnectedRegistries_List

```java
/**
 * Samples for ConnectedRegistries List.
 */
public final class ConnectedRegistriesListSamples {
    /*
     * x-ms-original-file: 2025-11-01/ConnectedRegistryList.json
     */
    /**
     * Sample code: ConnectedRegistryList.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        connectedRegistryList(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.connectedRegistries().list("myResourceGroup", "myRegistry", null, com.azure.core.util.Context.NONE);
    }
}
```

### ConnectedRegistries_Update

```java
import com.azure.resourcemanager.containerregistry.generated.models.AuditLogStatus;
import com.azure.resourcemanager.containerregistry.generated.models.ConnectedRegistry;
import com.azure.resourcemanager.containerregistry.generated.models.GarbageCollectionProperties;
import com.azure.resourcemanager.containerregistry.generated.models.LogLevel;
import com.azure.resourcemanager.containerregistry.generated.models.LoggingProperties;
import com.azure.resourcemanager.containerregistry.generated.models.SyncUpdateProperties;
import java.time.Duration;
import java.util.Arrays;

/**
 * Samples for ConnectedRegistries Update.
 */
public final class ConnectedRegistriesUpdateSamples {
    /*
     * x-ms-original-file: 2025-11-01/ConnectedRegistryUpdate.json
     */
    /**
     * Sample code: ConnectedRegistryUpdate.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void connectedRegistryUpdate(
        com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        ConnectedRegistry resource = manager.connectedRegistries()
            .getWithResponse("myResourceGroup", "myRegistry", "myScopeMap", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withSyncProperties(new SyncUpdateProperties().withSchedule("0 0 */10 * *")
                .withSyncWindow(Duration.parse("P2D"))
                .withMessageTtl(Duration.parse("P30D")))
            .withLogging(
                new LoggingProperties().withLogLevel(LogLevel.DEBUG).withAuditLogStatus(AuditLogStatus.ENABLED))
            .withClientTokenIds(Arrays.asList(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.ContainerRegistry/registries/myRegistry/tokens/client1Token",
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.ContainerRegistry/registries/myRegistry/tokens/client2Token"))
            .withNotificationsList(Arrays.asList("hello-world:*:*", "sample/repo/*:1.0:*"))
            .withGarbageCollection(new GarbageCollectionProperties().withEnabled(true).withSchedule("0 5 * * *"))
            .apply();
    }
}
```

### CredentialSets_Create

```java
import com.azure.resourcemanager.containerregistry.generated.models.AuthCredential;
import com.azure.resourcemanager.containerregistry.generated.models.CredentialName;
import com.azure.resourcemanager.containerregistry.generated.models.CredentialSetProperties;
import com.azure.resourcemanager.containerregistry.generated.models.IdentityProperties;
import com.azure.resourcemanager.containerregistry.generated.models.ResourceIdentityType;
import java.util.Arrays;

/**
 * Samples for CredentialSets Create.
 */
public final class CredentialSetsCreateSamples {
    /*
     * x-ms-original-file: 2025-11-01/CredentialSetCreate.json
     */
    /**
     * Sample code: CredentialSetCreate.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        credentialSetCreate(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.credentialSets()
            .define("myCredentialSet")
            .withExistingRegistry("myResourceGroup", "myRegistry")
            .withProperties(new CredentialSetProperties().withLoginServer("docker.io")
                .withAuthCredentials(Arrays.asList(new AuthCredential().withName(CredentialName.CREDENTIAL1)
                    .withUsernameSecretIdentifier("fakeTokenPlaceholder")
                    .withPasswordSecretIdentifier("fakeTokenPlaceholder"))))
            .withIdentity(new IdentityProperties().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
            .create();
    }
}
```

### CredentialSets_Delete

```java
/**
 * Samples for CredentialSets Delete.
 */
public final class CredentialSetsDeleteSamples {
    /*
     * x-ms-original-file: 2025-11-01/CredentialSetDelete.json
     */
    /**
     * Sample code: CredentialSetDelete.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        credentialSetDelete(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.credentialSets()
            .delete("myResourceGroup", "myRegistry", "myCredentialSet", com.azure.core.util.Context.NONE);
    }
}
```

### CredentialSets_Get

```java
/**
 * Samples for CredentialSets Get.
 */
public final class CredentialSetsGetSamples {
    /*
     * x-ms-original-file: 2025-11-01/CredentialSetGet.json
     */
    /**
     * Sample code: CredentialSetGet.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        credentialSetGet(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.credentialSets()
            .getWithResponse("myResourceGroup", "myRegistry", "myCredentialSet", com.azure.core.util.Context.NONE);
    }
}
```

### CredentialSets_List

```java
/**
 * Samples for CredentialSets List.
 */
public final class CredentialSetsListSamples {
    /*
     * x-ms-original-file: 2025-11-01/CredentialSetList.json
     */
    /**
     * Sample code: CredentialSetList.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        credentialSetList(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.credentialSets().list("myResourceGroup", "myRegistry", com.azure.core.util.Context.NONE);
    }
}
```

### CredentialSets_Update

```java
import com.azure.resourcemanager.containerregistry.generated.models.AuthCredential;
import com.azure.resourcemanager.containerregistry.generated.models.CredentialName;
import com.azure.resourcemanager.containerregistry.generated.models.CredentialSet;
import java.util.Arrays;

/**
 * Samples for CredentialSets Update.
 */
public final class CredentialSetsUpdateSamples {
    /*
     * x-ms-original-file: 2025-11-01/CredentialSetUpdate.json
     */
    /**
     * Sample code: CredentialSetUpdate.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        credentialSetUpdate(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        CredentialSet resource = manager.credentialSets()
            .getWithResponse("myResourceGroup", "myRegistry", "myCredentialSet", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withAuthCredentials(Arrays.asList(new AuthCredential().withName(CredentialName.CREDENTIAL1)
                .withUsernameSecretIdentifier("fakeTokenPlaceholder")
                .withPasswordSecretIdentifier("fakeTokenPlaceholder")))
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
     * x-ms-original-file: 2025-11-01/OperationList.json
     */
    /**
     * Sample code: OperationList.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        operationList(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.containerregistry.generated.models.ConnectionStatus;
import com.azure.resourcemanager.containerregistry.generated.models.PrivateEndpointConnectionProperties;
import com.azure.resourcemanager.containerregistry.generated.models.PrivateLinkServiceConnectionState;

/**
 * Samples for PrivateEndpointConnections CreateOrUpdate.
 */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-11-01/PrivateEndpointConnectionCreateOrUpdate.json
     */
    /**
     * Sample code: PrivateEndpointConnectionCreateOrUpdate.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void privateEndpointConnectionCreateOrUpdate(
        com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.privateEndpointConnections()
            .define("myConnection")
            .withExistingRegistry("myResourceGroup", "myRegistry")
            .withProperties(new PrivateEndpointConnectionProperties().withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState().withStatus(ConnectionStatus.APPROVED)
                    .withDescription("Auto-Approved")))
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
     * x-ms-original-file: 2025-11-01/PrivateEndpointConnectionDelete.json
     */
    /**
     * Sample code: PrivateEndpointConnectionDelete.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void privateEndpointConnectionDelete(
        com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.privateEndpointConnections()
            .delete("myResourceGroup", "myRegistry", "myConnection", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-11-01/PrivateEndpointConnectionGet.json
     */
    /**
     * Sample code: PrivateEndpointConnectionGet.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void privateEndpointConnectionGet(
        com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.privateEndpointConnections()
            .getWithResponse("myResourceGroup", "myRegistry", "myConnection", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_List

```java
/**
 * Samples for PrivateEndpointConnections List.
 */
public final class PrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: 2025-11-01/PrivateEndpointConnectionList.json
     */
    /**
     * Sample code: PrivateEndpointConnectionList.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void privateEndpointConnectionList(
        com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.privateEndpointConnections().list("myResourceGroup", "myRegistry", com.azure.core.util.Context.NONE);
    }
}
```

### Registries_CheckNameAvailability

```java
import com.azure.resourcemanager.containerregistry.generated.models.ContainerRegistryResourceType;
import com.azure.resourcemanager.containerregistry.generated.models.RegistryNameCheckRequest;

/**
 * Samples for Registries CheckNameAvailability.
 */
public final class RegistriesCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: 2025-11-01/RegistryCheckNameAvailable.json
     */
    /**
     * Sample code: RegistryCheckNameAvailable.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void registryCheckNameAvailable(
        com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.registries()
            .checkNameAvailabilityWithResponse(
                new RegistryNameCheckRequest().withName("myRegistry")
                    .withType(ContainerRegistryResourceType.MICROSOFT_CONTAINER_REGISTRY_REGISTRIES),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-11-01/RegistryCheckNameNotAvailable.json
     */
    /**
     * Sample code: RegistryCheckNameNotAvailable.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void registryCheckNameNotAvailable(
        com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.registries()
            .checkNameAvailabilityWithResponse(
                new RegistryNameCheckRequest().withName("myRegistry")
                    .withType(ContainerRegistryResourceType.MICROSOFT_CONTAINER_REGISTRY_REGISTRIES),
                com.azure.core.util.Context.NONE);
    }
}
```

### Registries_Create

```java
import com.azure.resourcemanager.containerregistry.generated.models.RegistryProperties;
import com.azure.resourcemanager.containerregistry.generated.models.RoleAssignmentMode;
import com.azure.resourcemanager.containerregistry.generated.models.Sku;
import com.azure.resourcemanager.containerregistry.generated.models.SkuName;
import com.azure.resourcemanager.containerregistry.generated.models.ZoneRedundancy;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Registries Create.
 */
public final class RegistriesCreateSamples {
    /*
     * x-ms-original-file: 2025-11-01/RegistryCreateAbac.json
     */
    /**
     * Sample code: RegistryCreateAbac.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        registryCreateAbac(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.registries()
            .define("myRegistry")
            .withRegion("westus")
            .withExistingResourceGroup("myResourceGroup")
            .withSku(new Sku().withName(SkuName.STANDARD))
            .withTags(mapOf("key", "fakeTokenPlaceholder"))
            .withProperties(
                new RegistryProperties().withRoleAssignmentMode(RoleAssignmentMode.ABAC_REPOSITORY_PERMISSIONS))
            .create();
    }

    /*
     * x-ms-original-file: 2025-11-01/RegistryCreateZoneRedundant.json
     */
    /**
     * Sample code: RegistryCreateZoneRedundant.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void registryCreateZoneRedundant(
        com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.registries()
            .define("myRegistry")
            .withRegion("westus")
            .withExistingResourceGroup("myResourceGroup")
            .withSku(new Sku().withName(SkuName.STANDARD))
            .withTags(mapOf("key", "fakeTokenPlaceholder"))
            .withProperties(new RegistryProperties().withZoneRedundancy(ZoneRedundancy.ENABLED))
            .create();
    }

    /*
     * x-ms-original-file: 2025-11-01/RegistryCreate.json
     */
    /**
     * Sample code: RegistryCreate.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        registryCreate(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.registries()
            .define("myRegistry")
            .withRegion("westus")
            .withExistingResourceGroup("myResourceGroup")
            .withSku(new Sku().withName(SkuName.STANDARD))
            .withTags(mapOf("key", "fakeTokenPlaceholder"))
            .withProperties(new RegistryProperties().withAdminUserEnabled(true))
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

### Registries_Delete

```java
/**
 * Samples for Registries Delete.
 */
public final class RegistriesDeleteSamples {
    /*
     * x-ms-original-file: 2025-11-01/RegistryDelete.json
     */
    /**
     * Sample code: RegistryDelete.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        registryDelete(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.registries().delete("myResourceGroup", "myRegistry", com.azure.core.util.Context.NONE);
    }
}
```

### Registries_GenerateCredentials

```java
import com.azure.resourcemanager.containerregistry.generated.models.GenerateCredentialsParameters;
import java.time.OffsetDateTime;

/**
 * Samples for Registries GenerateCredentials.
 */
public final class RegistriesGenerateCredentialsSamples {
    /*
     * x-ms-original-file: 2025-11-01/RegistryGenerateCredentials.json
     */
    /**
     * Sample code: RegistryGenerateCredentials.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void registryGenerateCredentials(
        com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.registries()
            .generateCredentials("myResourceGroup", "myRegistry",
                new GenerateCredentialsParameters().withTokenId("fakeTokenPlaceholder")
                    .withExpiry(OffsetDateTime.parse("2020-12-31T15:59:59.0707808Z")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Registries_GetByResourceGroup

```java
/**
 * Samples for Registries GetByResourceGroup.
 */
public final class RegistriesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-11-01/RegistryGet.json
     */
    /**
     * Sample code: RegistryGet.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        registryGet(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.registries()
            .getByResourceGroupWithResponse("myResourceGroup", "myRegistry", com.azure.core.util.Context.NONE);
    }
}
```

### Registries_GetPrivateLinkResource

```java
/**
 * Samples for Registries GetPrivateLinkResource.
 */
public final class RegistriesGetPrivateLinkResourceSamples {
    /*
     * x-ms-original-file: 2025-11-01/RegistryGetPrivateLinkResource.json
     */
    /**
     * Sample code: RegistryGetPrivateLinkResource.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void registryGetPrivateLinkResource(
        com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.registries()
            .getPrivateLinkResourceWithResponse("myResourceGroup", "myRegistry", "registry",
                com.azure.core.util.Context.NONE);
    }
}
```

### Registries_ImportImage

```java
import com.azure.resourcemanager.containerregistry.generated.models.ImportImageParameters;
import com.azure.resourcemanager.containerregistry.generated.models.ImportMode;
import com.azure.resourcemanager.containerregistry.generated.models.ImportSource;
import java.util.Arrays;

/**
 * Samples for Registries ImportImage.
 */
public final class RegistriesImportImageSamples {
    /*
     * x-ms-original-file: 2025-11-01/ImportImageFromPublicRegistry.json
     */
    /**
     * Sample code: ImportImageFromPublicRegistry.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void importImageFromPublicRegistry(
        com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.registries()
            .importImage("myResourceGroup", "myRegistry",
                new ImportImageParameters()
                    .withSource(new ImportSource().withRegistryUri("registry.hub.docker.com")
                        .withSourceImage("library/hello-world"))
                    .withTargetTags(Arrays.asList("targetRepository:targetTag"))
                    .withUntaggedTargetRepositories(Arrays.asList("targetRepository1"))
                    .withMode(ImportMode.FORCE),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-11-01/ImportImageByTag.json
     */
    /**
     * Sample code: ImportImageByTag.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        importImageByTag(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.registries()
            .importImage("myResourceGroup", "myRegistry",
                new ImportImageParameters().withSource(new ImportSource().withResourceId(
                    "/subscriptions/10000000-0000-0000-0000-000000000000/resourceGroups/sourceResourceGroup/providers/Microsoft.ContainerRegistry/registries/sourceRegistry")
                    .withSourceImage("sourceRepository:sourceTag"))
                    .withTargetTags(Arrays.asList("targetRepository:targetTag"))
                    .withUntaggedTargetRepositories(Arrays.asList("targetRepository1"))
                    .withMode(ImportMode.FORCE),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-11-01/ImportImageByManifestDigest.json
     */
    /**
     * Sample code: ImportImageByManifestDigest.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void importImageByManifestDigest(
        com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.registries()
            .importImage("myResourceGroup", "myRegistry",
                new ImportImageParameters().withSource(new ImportSource().withResourceId(
                    "/subscriptions/10000000-0000-0000-0000-000000000000/resourceGroups/sourceResourceGroup/providers/Microsoft.ContainerRegistry/registries/sourceRegistry")
                    .withSourceImage(
                        "sourceRepository@sha256:0000000000000000000000000000000000000000000000000000000000000000"))
                    .withTargetTags(Arrays.asList("targetRepository:targetTag"))
                    .withUntaggedTargetRepositories(Arrays.asList("targetRepository1"))
                    .withMode(ImportMode.FORCE),
                com.azure.core.util.Context.NONE);
    }
}
```

### Registries_List

```java
/**
 * Samples for Registries List.
 */
public final class RegistriesListSamples {
    /*
     * x-ms-original-file: 2025-11-01/RegistryList.json
     */
    /**
     * Sample code: RegistryList.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        registryList(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.registries().list(com.azure.core.util.Context.NONE);
    }
}
```

### Registries_ListByResourceGroup

```java
/**
 * Samples for Registries ListByResourceGroup.
 */
public final class RegistriesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-11-01/RegistryListByResourceGroup.json
     */
    /**
     * Sample code: RegistryListByResourceGroup.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void registryListByResourceGroup(
        com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.registries().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### Registries_ListCredentials

```java
/**
 * Samples for Registries ListCredentials.
 */
public final class RegistriesListCredentialsSamples {
    /*
     * x-ms-original-file: 2025-11-01/RegistryListCredentials.json
     */
    /**
     * Sample code: RegistryListCredentials.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void registryListCredentials(
        com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.registries()
            .listCredentialsWithResponse("myResourceGroup", "myRegistry", com.azure.core.util.Context.NONE);
    }
}
```

### Registries_ListPrivateLinkResources

```java
/**
 * Samples for Registries ListPrivateLinkResources.
 */
public final class RegistriesListPrivateLinkResourcesSamples {
    /*
     * x-ms-original-file: 2025-11-01/RegistryListPrivateLinkResources.json
     */
    /**
     * Sample code: RegistryListPrivateLinkResources.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void registryListPrivateLinkResources(
        com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.registries()
            .listPrivateLinkResources("myResourceGroup", "myRegistry", com.azure.core.util.Context.NONE);
    }
}
```

### Registries_ListUsages

```java
/**
 * Samples for Registries ListUsages.
 */
public final class RegistriesListUsagesSamples {
    /*
     * x-ms-original-file: 2025-11-01/RegistryListUsages.json
     */
    /**
     * Sample code: RegistryListUsages.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        registryListUsages(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.registries().listUsagesWithResponse("myResourceGroup", "myRegistry", com.azure.core.util.Context.NONE);
    }
}
```

### Registries_RegenerateCredential

```java
import com.azure.resourcemanager.containerregistry.generated.models.PasswordName;
import com.azure.resourcemanager.containerregistry.generated.models.RegenerateCredentialParameters;

/**
 * Samples for Registries RegenerateCredential.
 */
public final class RegistriesRegenerateCredentialSamples {
    /*
     * x-ms-original-file: 2025-11-01/RegistryRegenerateCredential.json
     */
    /**
     * Sample code: RegistryRegenerateCredential.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void registryRegenerateCredential(
        com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.registries()
            .regenerateCredentialWithResponse("myResourceGroup", "myRegistry",
                new RegenerateCredentialParameters().withName(PasswordName.PASSWORD), com.azure.core.util.Context.NONE);
    }
}
```

### Registries_Update

```java
import com.azure.resourcemanager.containerregistry.generated.models.Registry;
import com.azure.resourcemanager.containerregistry.generated.models.RoleAssignmentMode;
import com.azure.resourcemanager.containerregistry.generated.models.Sku;
import com.azure.resourcemanager.containerregistry.generated.models.SkuName;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Registries Update.
 */
public final class RegistriesUpdateSamples {
    /*
     * x-ms-original-file: 2025-11-01/RegistryUpdate.json
     */
    /**
     * Sample code: RegistryUpdate.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        registryUpdate(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        Registry resource = manager.registries()
            .getByResourceGroupWithResponse("myResourceGroup", "myRegistry", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key", "fakeTokenPlaceholder"))
            .withSku(new Sku().withName(SkuName.STANDARD))
            .withAdminUserEnabled(true)
            .withRoleAssignmentMode(RoleAssignmentMode.ABAC_REPOSITORY_PERMISSIONS)
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

### Replications_Create

```java
import com.azure.resourcemanager.containerregistry.generated.models.ReplicationProperties;
import com.azure.resourcemanager.containerregistry.generated.models.ZoneRedundancy;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Replications Create.
 */
public final class ReplicationsCreateSamples {
    /*
     * x-ms-original-file: 2025-11-01/ReplicationCreate.json
     */
    /**
     * Sample code: ReplicationCreate.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        replicationCreate(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.replications()
            .define("myReplication")
            .withRegion("eastus")
            .withExistingRegistry("myResourceGroup", "myRegistry")
            .withTags(mapOf("key", "fakeTokenPlaceholder"))
            .create();
    }

    /*
     * x-ms-original-file: 2025-11-01/ReplicationCreateZoneRedundant.json
     */
    /**
     * Sample code: ReplicationCreateZoneRedundant.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void replicationCreateZoneRedundant(
        com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.replications()
            .define("myReplication")
            .withRegion("eastus")
            .withExistingRegistry("myResourceGroup", "myRegistry")
            .withTags(mapOf("key", "fakeTokenPlaceholder"))
            .withProperties(
                new ReplicationProperties().withRegionEndpointEnabled(true).withZoneRedundancy(ZoneRedundancy.ENABLED))
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

### Replications_Delete

```java
/**
 * Samples for Replications Delete.
 */
public final class ReplicationsDeleteSamples {
    /*
     * x-ms-original-file: 2025-11-01/ReplicationDelete.json
     */
    /**
     * Sample code: ReplicationDelete.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        replicationDelete(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.replications()
            .delete("myResourceGroup", "myRegistry", "myReplication", com.azure.core.util.Context.NONE);
    }
}
```

### Replications_Get

```java
/**
 * Samples for Replications Get.
 */
public final class ReplicationsGetSamples {
    /*
     * x-ms-original-file: 2025-11-01/ReplicationGet.json
     */
    /**
     * Sample code: ReplicationGet.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        replicationGet(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.replications()
            .getWithResponse("myResourceGroup", "myRegistry", "myReplication", com.azure.core.util.Context.NONE);
    }
}
```

### Replications_List

```java
/**
 * Samples for Replications List.
 */
public final class ReplicationsListSamples {
    /*
     * x-ms-original-file: 2025-11-01/ReplicationList.json
     */
    /**
     * Sample code: ReplicationList.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        replicationList(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.replications().list("myResourceGroup", "myRegistry", com.azure.core.util.Context.NONE);
    }
}
```

### Replications_Update

```java
import com.azure.resourcemanager.containerregistry.generated.models.Replication;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Replications Update.
 */
public final class ReplicationsUpdateSamples {
    /*
     * x-ms-original-file: 2025-11-01/ReplicationUpdate.json
     */
    /**
     * Sample code: ReplicationUpdate.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        replicationUpdate(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        Replication resource = manager.replications()
            .getWithResponse("myResourceGroup", "myRegistry", "myReplication", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key", "fakeTokenPlaceholder")).apply();
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

### ScopeMaps_Create

```java
import com.azure.resourcemanager.containerregistry.generated.models.ScopeMapProperties;
import java.util.Arrays;

/**
 * Samples for ScopeMaps Create.
 */
public final class ScopeMapsCreateSamples {
    /*
     * x-ms-original-file: 2025-11-01/ScopeMapCreate.json
     */
    /**
     * Sample code: ScopeMapCreate.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        scopeMapCreate(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.scopeMaps()
            .define("myScopeMap")
            .withExistingRegistry("myResourceGroup", "myRegistry")
            .withProperties(new ScopeMapProperties().withDescription("Developer Scopes")
                .withActions(
                    Arrays.asList("repositories/myrepository/contentWrite", "repositories/myrepository/delete")))
            .create();
    }
}
```

### ScopeMaps_Delete

```java
/**
 * Samples for ScopeMaps Delete.
 */
public final class ScopeMapsDeleteSamples {
    /*
     * x-ms-original-file: 2025-11-01/ScopeMapDelete.json
     */
    /**
     * Sample code: ScopeMapDelete.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        scopeMapDelete(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.scopeMaps().delete("myResourceGroup", "myRegistry", "myScopeMap", com.azure.core.util.Context.NONE);
    }
}
```

### ScopeMaps_Get

```java
/**
 * Samples for ScopeMaps Get.
 */
public final class ScopeMapsGetSamples {
    /*
     * x-ms-original-file: 2025-11-01/ScopeMapGet.json
     */
    /**
     * Sample code: ScopeMapGet.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        scopeMapGet(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.scopeMaps()
            .getWithResponse("myResourceGroup", "myRegistry", "myScopeMap", com.azure.core.util.Context.NONE);
    }
}
```

### ScopeMaps_List

```java
/**
 * Samples for ScopeMaps List.
 */
public final class ScopeMapsListSamples {
    /*
     * x-ms-original-file: 2025-11-01/ScopeMapList.json
     */
    /**
     * Sample code: ScopeMapList.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        scopeMapList(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.scopeMaps().list("myResourceGroup", "myRegistry", com.azure.core.util.Context.NONE);
    }
}
```

### ScopeMaps_Update

```java
import com.azure.resourcemanager.containerregistry.generated.models.ScopeMap;
import java.util.Arrays;

/**
 * Samples for ScopeMaps Update.
 */
public final class ScopeMapsUpdateSamples {
    /*
     * x-ms-original-file: 2025-11-01/ScopeMapUpdate.json
     */
    /**
     * Sample code: ScopeMapUpdate.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        scopeMapUpdate(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        ScopeMap resource = manager.scopeMaps()
            .getWithResponse("myResourceGroup", "myRegistry", "myScopeMap", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withDescription("Developer Scopes")
            .withActions(
                Arrays.asList("repositories/myrepository/contentWrite", "repositories/myrepository/contentRead"))
            .apply();
    }
}
```

### Tokens_Create

```java
import com.azure.resourcemanager.containerregistry.generated.models.TokenCertificate;
import com.azure.resourcemanager.containerregistry.generated.models.TokenCertificateName;
import com.azure.resourcemanager.containerregistry.generated.models.TokenCredentialsProperties;
import com.azure.resourcemanager.containerregistry.generated.models.TokenProperties;
import com.azure.resourcemanager.containerregistry.generated.models.TokenStatus;
import java.util.Arrays;

/**
 * Samples for Tokens Create.
 */
public final class TokensCreateSamples {
    /*
     * x-ms-original-file: 2025-11-01/TokenCreate.json
     */
    /**
     * Sample code: TokenCreate.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        tokenCreate(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.tokens()
            .define("myToken")
            .withExistingRegistry("myResourceGroup", "myRegistry")
            .withProperties(new TokenProperties().withScopeMapId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.ContainerRegistry/registries/myRegistry/scopeMaps/myScopeMap")
                .withCredentials(new TokenCredentialsProperties()
                    .withCertificates(Arrays.asList(new TokenCertificate().withName(TokenCertificateName.CERTIFICATE1)
                        .withEncodedPemCertificate("fakeTokenPlaceholder"))))
                .withStatus(TokenStatus.DISABLED))
            .create();
    }
}
```

### Tokens_Delete

```java
/**
 * Samples for Tokens Delete.
 */
public final class TokensDeleteSamples {
    /*
     * x-ms-original-file: 2025-11-01/TokenDelete.json
     */
    /**
     * Sample code: TokenDelete.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        tokenDelete(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.tokens().delete("myResourceGroup", "myRegistry", "myToken", com.azure.core.util.Context.NONE);
    }
}
```

### Tokens_Get

```java
/**
 * Samples for Tokens Get.
 */
public final class TokensGetSamples {
    /*
     * x-ms-original-file: 2025-11-01/TokenGet.json
     */
    /**
     * Sample code: TokenGet.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        tokenGet(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.tokens().getWithResponse("myResourceGroup", "myRegistry", "myToken", com.azure.core.util.Context.NONE);
    }
}
```

### Tokens_List

```java
/**
 * Samples for Tokens List.
 */
public final class TokensListSamples {
    /*
     * x-ms-original-file: 2025-11-01/TokenList.json
     */
    /**
     * Sample code: TokenList.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        tokenList(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.tokens().list("myResourceGroup", "myRegistry", com.azure.core.util.Context.NONE);
    }
}
```

### Tokens_Update

```java
import com.azure.resourcemanager.containerregistry.generated.models.Token;
import com.azure.resourcemanager.containerregistry.generated.models.TokenCertificate;
import com.azure.resourcemanager.containerregistry.generated.models.TokenCertificateName;
import com.azure.resourcemanager.containerregistry.generated.models.TokenCredentialsProperties;
import java.util.Arrays;

/**
 * Samples for Tokens Update.
 */
public final class TokensUpdateSamples {
    /*
     * x-ms-original-file: 2025-11-01/TokenUpdate.json
     */
    /**
     * Sample code: TokenUpdate.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        tokenUpdate(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        Token resource = manager.tokens()
            .getWithResponse("myResourceGroup", "myRegistry", "myToken", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withScopeMapId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.ContainerRegistry/registries/myRegistry/scopeMaps/myNewScopeMap")
            .withCredentials(new TokenCredentialsProperties()
                .withCertificates(Arrays.asList(new TokenCertificate().withName(TokenCertificateName.CERTIFICATE1)
                    .withEncodedPemCertificate("fakeTokenPlaceholder"))))
            .apply();
    }
}
```

### Webhooks_Create

```java
import com.azure.resourcemanager.containerregistry.generated.models.WebhookAction;
import com.azure.resourcemanager.containerregistry.generated.models.WebhookStatus;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Webhooks Create.
 */
public final class WebhooksCreateSamples {
    /*
     * x-ms-original-file: 2025-11-01/WebhookCreate.json
     */
    /**
     * Sample code: WebhookCreate.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        webhookCreate(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.webhooks()
            .define("myWebhook")
            .withRegion("westus")
            .withExistingRegistry("myResourceGroup", "myRegistry")
            .withTags(mapOf("key", "fakeTokenPlaceholder"))
            .withServiceUri("http://myservice.com")
            .withCustomHeaders(mapOf("Authorization", "fakeTokenPlaceholder"))
            .withStatus(WebhookStatus.ENABLED)
            .withScope("myRepository")
            .withActions(Arrays.asList(WebhookAction.PUSH))
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

### Webhooks_Delete

```java
/**
 * Samples for Webhooks Delete.
 */
public final class WebhooksDeleteSamples {
    /*
     * x-ms-original-file: 2025-11-01/WebhookDelete.json
     */
    /**
     * Sample code: WebhookDelete.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        webhookDelete(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.webhooks().delete("myResourceGroup", "myRegistry", "myWebhook", com.azure.core.util.Context.NONE);
    }
}
```

### Webhooks_Get

```java
/**
 * Samples for Webhooks Get.
 */
public final class WebhooksGetSamples {
    /*
     * x-ms-original-file: 2025-11-01/WebhookGet.json
     */
    /**
     * Sample code: WebhookGet.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        webhookGet(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.webhooks()
            .getWithResponse("myResourceGroup", "myRegistry", "myWebhook", com.azure.core.util.Context.NONE);
    }
}
```

### Webhooks_GetCallbackConfig

```java
/**
 * Samples for Webhooks GetCallbackConfig.
 */
public final class WebhooksGetCallbackConfigSamples {
    /*
     * x-ms-original-file: 2025-11-01/WebhookGetCallbackConfig.json
     */
    /**
     * Sample code: WebhookGetCallbackConfig.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void webhookGetCallbackConfig(
        com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.webhooks()
            .getCallbackConfigWithResponse("myResourceGroup", "myRegistry", "myWebhook",
                com.azure.core.util.Context.NONE);
    }
}
```

### Webhooks_List

```java
/**
 * Samples for Webhooks List.
 */
public final class WebhooksListSamples {
    /*
     * x-ms-original-file: 2025-11-01/WebhookList.json
     */
    /**
     * Sample code: WebhookList.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        webhookList(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.webhooks().list("myResourceGroup", "myRegistry", com.azure.core.util.Context.NONE);
    }
}
```

### Webhooks_ListEvents

```java
/**
 * Samples for Webhooks ListEvents.
 */
public final class WebhooksListEventsSamples {
    /*
     * x-ms-original-file: 2025-11-01/WebhookListEvents.json
     */
    /**
     * Sample code: WebhookListEvents.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        webhookListEvents(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.webhooks().listEvents("myResourceGroup", "myRegistry", "myWebhook", com.azure.core.util.Context.NONE);
    }
}
```

### Webhooks_Ping

```java
/**
 * Samples for Webhooks Ping.
 */
public final class WebhooksPingSamples {
    /*
     * x-ms-original-file: 2025-11-01/WebhookPing.json
     */
    /**
     * Sample code: WebhookPing.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        webhookPing(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        manager.webhooks()
            .pingWithResponse("myResourceGroup", "myRegistry", "myWebhook", com.azure.core.util.Context.NONE);
    }
}
```

### Webhooks_Update

```java
import com.azure.resourcemanager.containerregistry.generated.models.Webhook;
import com.azure.resourcemanager.containerregistry.generated.models.WebhookAction;
import com.azure.resourcemanager.containerregistry.generated.models.WebhookStatus;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Webhooks Update.
 */
public final class WebhooksUpdateSamples {
    /*
     * x-ms-original-file: 2025-11-01/WebhookUpdate.json
     */
    /**
     * Sample code: WebhookUpdate.
     * 
     * @param manager Entry point to ContainerRegistryManager.
     */
    public static void
        webhookUpdate(com.azure.resourcemanager.containerregistry.generated.ContainerRegistryManager manager) {
        Webhook resource = manager.webhooks()
            .getWithResponse("myResourceGroup", "myRegistry", "myWebhook", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key", "fakeTokenPlaceholder"))
            .withServiceUri("http://myservice.com")
            .withCustomHeaders(mapOf("Authorization", "fakeTokenPlaceholder"))
            .withStatus(WebhookStatus.ENABLED)
            .withScope("myRepository")
            .withActions(Arrays.asList(WebhookAction.PUSH))
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

