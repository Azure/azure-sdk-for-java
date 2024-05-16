# Code snippets and samples


## Namespaces

- [CheckAvailability](#namespaces_checkavailability)
- [CreateOrUpdate](#namespaces_createorupdate)
- [CreateOrUpdateAuthorizationRule](#namespaces_createorupdateauthorizationrule)
- [Delete](#namespaces_delete)
- [DeleteAuthorizationRule](#namespaces_deleteauthorizationrule)
- [GetAuthorizationRule](#namespaces_getauthorizationrule)
- [GetByResourceGroup](#namespaces_getbyresourcegroup)
- [GetPnsCredentials](#namespaces_getpnscredentials)
- [List](#namespaces_list)
- [ListAuthorizationRules](#namespaces_listauthorizationrules)
- [ListByResourceGroup](#namespaces_listbyresourcegroup)
- [ListKeys](#namespaces_listkeys)
- [RegenerateKeys](#namespaces_regeneratekeys)
- [Update](#namespaces_update)

## NotificationHubs

- [CheckNotificationHubAvailability](#notificationhubs_checknotificationhubavailability)
- [CreateOrUpdate](#notificationhubs_createorupdate)
- [CreateOrUpdateAuthorizationRule](#notificationhubs_createorupdateauthorizationrule)
- [DebugSend](#notificationhubs_debugsend)
- [Delete](#notificationhubs_delete)
- [DeleteAuthorizationRule](#notificationhubs_deleteauthorizationrule)
- [Get](#notificationhubs_get)
- [GetAuthorizationRule](#notificationhubs_getauthorizationrule)
- [GetPnsCredentials](#notificationhubs_getpnscredentials)
- [List](#notificationhubs_list)
- [ListAuthorizationRules](#notificationhubs_listauthorizationrules)
- [ListKeys](#notificationhubs_listkeys)
- [RegenerateKeys](#notificationhubs_regeneratekeys)
- [Update](#notificationhubs_update)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [GetGroupId](#privateendpointconnections_getgroupid)
- [List](#privateendpointconnections_list)
- [ListGroupIds](#privateendpointconnections_listgroupids)
- [Update](#privateendpointconnections_update)
### Namespaces_CheckAvailability

```java
import com.azure.resourcemanager.notificationhubs.models.CheckAvailabilityParameters;

/**
 * Samples for Namespaces CheckAvailability.
 */
public final class NamespacesCheckAvailabilitySamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * Namespaces/CheckAvailability.json
     */
    /**
     * Sample code: Namespaces_CheckAvailability.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void
        namespacesCheckAvailability(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.namespaces().checkAvailabilityWithResponse(
            new CheckAvailabilityParameters().withName("sdk-Namespace-2924"), com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_CreateOrUpdate

```java
import com.azure.resourcemanager.notificationhubs.models.AccessRights;
import com.azure.resourcemanager.notificationhubs.models.IpRule;
import com.azure.resourcemanager.notificationhubs.models.NetworkAcls;
import com.azure.resourcemanager.notificationhubs.models.PublicInternetAuthorizationRule;
import com.azure.resourcemanager.notificationhubs.models.Sku;
import com.azure.resourcemanager.notificationhubs.models.SkuName;
import com.azure.resourcemanager.notificationhubs.models.ZoneRedundancyPreference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Namespaces CreateOrUpdate.
 */
public final class NamespacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * Namespaces/CreateOrUpdate.json
     */
    /**
     * Sample code: Namespaces_CreateOrUpdate.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void
        namespacesCreateOrUpdate(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.namespaces().define("nh-sdk-ns").withRegion("South Central US").withExistingResourceGroup("5ktrial")
            .withSku(new Sku().withName(SkuName.STANDARD).withTier("Standard"))
            .withTags(mapOf("tag1", "value1", "tag2", "value2")).withZoneRedundancy(ZoneRedundancyPreference.ENABLED)
            .withNetworkAcls(new NetworkAcls()
                .withIpRules(Arrays.asList(new IpRule().withIpMask("185.48.100.00/24")
                    .withRights(Arrays.asList(AccessRights.MANAGE, AccessRights.SEND, AccessRights.LISTEN))))
                .withPublicNetworkRule(
                    new PublicInternetAuthorizationRule().withRights(Arrays.asList(AccessRights.LISTEN))))
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

### Namespaces_CreateOrUpdateAuthorizationRule

```java
import com.azure.resourcemanager.notificationhubs.fluent.models.SharedAccessAuthorizationRuleResourceInner;
import com.azure.resourcemanager.notificationhubs.models.AccessRights;
import java.util.Arrays;

/**
 * Samples for Namespaces CreateOrUpdateAuthorizationRule.
 */
public final class NamespacesCreateOrUpdateAuthorizationRuleSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * Namespaces/AuthorizationRuleCreateOrUpdate.json
     */
    /**
     * Sample code: Namespaces_CreateOrUpdateAuthorizationRule.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void namespacesCreateOrUpdateAuthorizationRule(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.namespaces()
            .createOrUpdateAuthorizationRuleWithResponse(
                "5ktrial", "nh-sdk-ns", "sdk-AuthRules-1788", new SharedAccessAuthorizationRuleResourceInner()
                    .withRights(Arrays.asList(AccessRights.LISTEN, AccessRights.SEND)),
                com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_Delete

```java
/**
 * Samples for Namespaces Delete.
 */
public final class NamespacesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * Namespaces/Delete.json
     */
    /**
     * Sample code: Namespaces_Delete.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void namespacesDelete(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.namespaces().deleteByResourceGroupWithResponse("5ktrial", "nh-sdk-ns",
            com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_DeleteAuthorizationRule

```java
/**
 * Samples for Namespaces DeleteAuthorizationRule.
 */
public final class NamespacesDeleteAuthorizationRuleSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * Namespaces/AuthorizationRuleDelete.json
     */
    /**
     * Sample code: Namespaces_DeleteAuthorizationRule.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void
        namespacesDeleteAuthorizationRule(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.namespaces().deleteAuthorizationRuleWithResponse("5ktrial", "nh-sdk-ns", "RootManageSharedAccessKey",
            com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_GetAuthorizationRule

```java
/**
 * Samples for Namespaces GetAuthorizationRule.
 */
public final class NamespacesGetAuthorizationRuleSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * Namespaces/AuthorizationRuleGet.json
     */
    /**
     * Sample code: Namespaces_GetAuthorizationRule.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void
        namespacesGetAuthorizationRule(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.namespaces().getAuthorizationRuleWithResponse("5ktrial", "nh-sdk-ns", "RootManageSharedAccessKey",
            com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_GetByResourceGroup

```java
/**
 * Samples for Namespaces GetByResourceGroup.
 */
public final class NamespacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * Namespaces/Get.json
     */
    /**
     * Sample code: Namespaces_Get.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void namespacesGet(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.namespaces().getByResourceGroupWithResponse("5ktrial", "nh-sdk-ns", com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_GetPnsCredentials

```java
/**
 * Samples for Namespaces GetPnsCredentials.
 */
public final class NamespacesGetPnsCredentialsSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * Namespaces/PnsCredentialsGet.json
     */
    /**
     * Sample code: Namespaces_GetPnsCredentials.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void
        namespacesGetPnsCredentials(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.namespaces().getPnsCredentialsWithResponse("5ktrial", "nh-sdk-ns", com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_List

```java
/**
 * Samples for Namespaces List.
 */
public final class NamespacesListSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * Namespaces/ListBySubscription.json
     */
    /**
     * Sample code: Namespaces_ListAll.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void namespacesListAll(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.namespaces().list(null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_ListAuthorizationRules

```java
/**
 * Samples for Namespaces ListAuthorizationRules.
 */
public final class NamespacesListAuthorizationRulesSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * Namespaces/AuthorizationRuleList.json
     */
    /**
     * Sample code: Namespaces_ListAuthorizationRules.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void
        namespacesListAuthorizationRules(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.namespaces().listAuthorizationRules("5ktrial", "nh-sdk-ns", com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_ListByResourceGroup

```java
/**
 * Samples for Namespaces ListByResourceGroup.
 */
public final class NamespacesListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * Namespaces/ListByResourceGroup.json
     */
    /**
     * Sample code: Namespaces_List.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void namespacesList(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.namespaces().listByResourceGroup("5ktrial", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_ListKeys

```java
/**
 * Samples for Namespaces ListKeys.
 */
public final class NamespacesListKeysSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * Namespaces/AuthorizationRuleListKeys.json
     */
    /**
     * Sample code: Namespaces_ListKeys.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void namespacesListKeys(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.namespaces().listKeysWithResponse("5ktrial", "nh-sdk-ns", "RootManageSharedAccessKey",
            com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_RegenerateKeys

```java
import com.azure.resourcemanager.notificationhubs.models.PolicyKeyResource;
import com.azure.resourcemanager.notificationhubs.models.PolicyKeyType;

/**
 * Samples for Namespaces RegenerateKeys.
 */
public final class NamespacesRegenerateKeysSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * Namespaces/AuthorizationRuleRegenerateKey.json
     */
    /**
     * Sample code: Namespaces_RegenerateKeys.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void
        namespacesRegenerateKeys(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.namespaces().regenerateKeysWithResponse("5ktrial", "nh-sdk-ns", "RootManageSharedAccessKey",
            new PolicyKeyResource().withPolicyKey(PolicyKeyType.PRIMARY_KEY), com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_Update

```java
import com.azure.resourcemanager.notificationhubs.fluent.models.NamespaceProperties;
import com.azure.resourcemanager.notificationhubs.fluent.models.PnsCredentials;
import com.azure.resourcemanager.notificationhubs.models.GcmCredential;
import com.azure.resourcemanager.notificationhubs.models.NamespaceResource;
import com.azure.resourcemanager.notificationhubs.models.Sku;
import com.azure.resourcemanager.notificationhubs.models.SkuName;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Namespaces Update.
 */
public final class NamespacesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * Namespaces/Update.json
     */
    /**
     * Sample code: Namespaces_Update.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void namespacesUpdate(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        NamespaceResource resource = manager.namespaces()
            .getByResourceGroupWithResponse("5ktrial", "nh-sdk-ns", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value3")).withSku(new Sku().withName(SkuName.FREE))
            .withProperties(
                new NamespaceProperties().withPnsCredentials(new PnsCredentials().withGcmCredential(new GcmCredential()
                    .withGcmEndpoint("https://fcm.googleapis.com/fcm/send").withGoogleApiKey("fakeTokenPlaceholder"))))
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

### NotificationHubs_CheckNotificationHubAvailability

```java
import com.azure.resourcemanager.notificationhubs.models.CheckAvailabilityParameters;

/**
 * Samples for NotificationHubs CheckNotificationHubAvailability.
 */
public final class NotificationHubsCheckNotificationHubAvailabilitySamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * NotificationHubs/CheckAvailability.json
     */
    /**
     * Sample code: NotificationHubs_CheckNotificationHubAvailability.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void notificationHubsCheckNotificationHubAvailability(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.notificationHubs().checkNotificationHubAvailabilityWithResponse("5ktrial", "locp-newns",
            new CheckAvailabilityParameters().withName("sdktest").withLocation("West Europe"),
            com.azure.core.util.Context.NONE);
    }
}
```

### NotificationHubs_CreateOrUpdate

```java
/**
 * Samples for NotificationHubs CreateOrUpdate.
 */
public final class NotificationHubsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * NotificationHubs/CreateOrUpdate.json
     */
    /**
     * Sample code: NotificationHubs_CreateOrUpdate.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void
        notificationHubsCreateOrUpdate(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.notificationHubs().define("nh-sdk-hub").withRegion("eastus")
            .withExistingNamespace("5ktrial", "nh-sdk-ns").create();
    }
}
```

### NotificationHubs_CreateOrUpdateAuthorizationRule

```java
import com.azure.resourcemanager.notificationhubs.models.AccessRights;
import java.util.Arrays;

/**
 * Samples for NotificationHubs CreateOrUpdateAuthorizationRule.
 */
public final class NotificationHubsCreateOrUpdateAuthorizationRuleSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * NotificationHubs/AuthorizationRuleCreateOrUpdate.json
     */
    /**
     * Sample code: NotificationHubs_CreateOrUpdateAuthorizationRule.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void notificationHubsCreateOrUpdateAuthorizationRule(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.notificationHubs().defineAuthorizationRule("MyManageSharedAccessKey")
            .withExistingNotificationHub("5ktrial", "nh-sdk-ns", "nh-sdk-hub")
            .withRights(Arrays.asList(AccessRights.LISTEN, AccessRights.SEND)).create();
    }
}
```

### NotificationHubs_DebugSend

```java
/**
 * Samples for NotificationHubs DebugSend.
 */
public final class NotificationHubsDebugSendSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * NotificationHubs/DebugSend.json
     */
    /**
     * Sample code: NotificationHubs_DebugSend.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void
        notificationHubsDebugSend(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.notificationHubs().debugSendWithResponse("5ktrial", "nh-sdk-ns", "nh-sdk-hub",
            com.azure.core.util.Context.NONE);
    }
}
```

### NotificationHubs_Delete

```java
/**
 * Samples for NotificationHubs Delete.
 */
public final class NotificationHubsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * NotificationHubs/Delete.json
     */
    /**
     * Sample code: NotificationHubs_Delete.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void
        notificationHubsDelete(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.notificationHubs().deleteWithResponse("5ktrial", "nh-sdk-ns", "nh-sdk-hub",
            com.azure.core.util.Context.NONE);
    }
}
```

### NotificationHubs_DeleteAuthorizationRule

```java
/**
 * Samples for NotificationHubs DeleteAuthorizationRule.
 */
public final class NotificationHubsDeleteAuthorizationRuleSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * NotificationHubs/AuthorizationRuleDelete.json
     */
    /**
     * Sample code: NotificationHubs_DeleteAuthorizationRule.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void notificationHubsDeleteAuthorizationRule(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.notificationHubs().deleteAuthorizationRuleWithResponse("5ktrial", "nh-sdk-ns", "nh-sdk-hub",
            "DefaultListenSharedAccessSignature", com.azure.core.util.Context.NONE);
    }
}
```

### NotificationHubs_Get

```java
/**
 * Samples for NotificationHubs Get.
 */
public final class NotificationHubsGetSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * NotificationHubs/Get.json
     */
    /**
     * Sample code: NotificationHubs_Get.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void notificationHubsGet(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.notificationHubs().getWithResponse("5ktrial", "nh-sdk-ns", "nh-sdk-hub",
            com.azure.core.util.Context.NONE);
    }
}
```

### NotificationHubs_GetAuthorizationRule

```java
/**
 * Samples for NotificationHubs GetAuthorizationRule.
 */
public final class NotificationHubsGetAuthorizationRuleSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * NotificationHubs/AuthorizationRuleGet.json
     */
    /**
     * Sample code: NotificationHubs_GetAuthorizationRule.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void notificationHubsGetAuthorizationRule(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.notificationHubs().getAuthorizationRuleWithResponse("5ktrial", "nh-sdk-ns", "nh-sdk-hub",
            "DefaultListenSharedAccessSignature", com.azure.core.util.Context.NONE);
    }
}
```

### NotificationHubs_GetPnsCredentials

```java
/**
 * Samples for NotificationHubs GetPnsCredentials.
 */
public final class NotificationHubsGetPnsCredentialsSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * NotificationHubs/PnsCredentialsGet.json
     */
    /**
     * Sample code: NotificationHubs_GetPnsCredentials.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void
        notificationHubsGetPnsCredentials(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.notificationHubs().getPnsCredentialsWithResponse("5ktrial", "nh-sdk-ns", "nh-sdk-hub",
            com.azure.core.util.Context.NONE);
    }
}
```

### NotificationHubs_List

```java
/**
 * Samples for NotificationHubs List.
 */
public final class NotificationHubsListSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * NotificationHubs/List.json
     */
    /**
     * Sample code: NotificationHubs_List.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void
        notificationHubsList(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.notificationHubs().list("5ktrial", "nh-sdk-ns", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### NotificationHubs_ListAuthorizationRules

```java
/**
 * Samples for NotificationHubs ListAuthorizationRules.
 */
public final class NotificationHubsListAuthorizationRulesSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * NotificationHubs/AuthorizationRuleList.json
     */
    /**
     * Sample code: NotificationHubs_ListAuthorizationRules.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void notificationHubsListAuthorizationRules(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.notificationHubs().listAuthorizationRules("5ktrial", "nh-sdk-ns", "nh-sdk-hub",
            com.azure.core.util.Context.NONE);
    }
}
```

### NotificationHubs_ListKeys

```java
/**
 * Samples for NotificationHubs ListKeys.
 */
public final class NotificationHubsListKeysSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * NotificationHubs/AuthorizationRuleListKeys.json
     */
    /**
     * Sample code: NotificationHubs_ListKeys.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void
        notificationHubsListKeys(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.notificationHubs().listKeysWithResponse("5ktrial", "nh-sdk-ns", "nh-sdk-hub", "sdk-AuthRules-5800",
            com.azure.core.util.Context.NONE);
    }
}
```

### NotificationHubs_RegenerateKeys

```java
import com.azure.resourcemanager.notificationhubs.models.PolicyKeyResource;
import com.azure.resourcemanager.notificationhubs.models.PolicyKeyType;

/**
 * Samples for NotificationHubs RegenerateKeys.
 */
public final class NotificationHubsRegenerateKeysSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * NotificationHubs/AuthorizationRuleRegenerateKey.json
     */
    /**
     * Sample code: NotificationHubs_RegenerateKeys.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void
        notificationHubsRegenerateKeys(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.notificationHubs().regenerateKeysWithResponse("5ktrial", "nh-sdk-ns", "nh-sdk-hub",
            "DefaultListenSharedAccessSignature", new PolicyKeyResource().withPolicyKey(PolicyKeyType.PRIMARY_KEY),
            com.azure.core.util.Context.NONE);
    }
}
```

### NotificationHubs_Update

```java
import com.azure.resourcemanager.notificationhubs.models.GcmCredential;
import com.azure.resourcemanager.notificationhubs.models.NotificationHubResource;

/**
 * Samples for NotificationHubs Update.
 */
public final class NotificationHubsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * NotificationHubs/Update.json
     */
    /**
     * Sample code: NotificationHubs_Update.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void
        notificationHubsUpdate(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        NotificationHubResource resource = manager.notificationHubs().getWithResponse("sdkresourceGroup", "nh-sdk-ns",
            "sdk-notificationHubs-8708", com.azure.core.util.Context.NONE).getValue();
        resource
            .update().withRegistrationTtl("10675199.02:48:05.4775807").withGcmCredential(new GcmCredential()
                .withGcmEndpoint("https://fcm.googleapis.com/fcm/send").withGoogleApiKey("fakeTokenPlaceholder"))
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
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * NHOperationsList.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void operationsList(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * Namespaces/PrivateEndpointConnectionDelete.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Delete.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void
        privateEndpointConnectionsDelete(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.privateEndpointConnections().delete("5ktrial", "nh-sdk-ns",
            "nh-sdk-ns.1fa229cd-bf3f-47f0-8c49-afb36723997e", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * Namespaces/PrivateEndpointConnectionGet.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Get.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void
        privateEndpointConnectionsGet(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.privateEndpointConnections().getWithResponse("5ktrial", "nh-sdk-ns",
            "nh-sdk-ns.1fa229cd-bf3f-47f0-8c49-afb36723997e", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_GetGroupId

```java
/**
 * Samples for PrivateEndpointConnections GetGroupId.
 */
public final class PrivateEndpointConnectionsGetGroupIdSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * Namespaces/PrivateLinkResourceGet.json
     */
    /**
     * Sample code: PrivateEndpointConnections_GetGroupId.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void privateEndpointConnectionsGetGroupId(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.privateEndpointConnections().getGroupIdWithResponse("5ktrial", "nh-sdk-ns", "namespace",
            com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * Namespaces/PrivateEndpointConnectionList.json
     */
    /**
     * Sample code: PrivateEndpointConnections_List.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void
        privateEndpointConnectionsList(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.privateEndpointConnections().list("5ktrial", "nh-sdk-ns", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListGroupIds

```java
/**
 * Samples for PrivateEndpointConnections ListGroupIds.
 */
public final class PrivateEndpointConnectionsListGroupIdsSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * Namespaces/PrivateLinkResourceList.json
     */
    /**
     * Sample code: PrivateEndpointConnections_ListGroupIds.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void privateEndpointConnectionsListGroupIds(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.privateEndpointConnections().listGroupIds("5ktrial", "nh-sdk-ns", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Update

```java
import com.azure.resourcemanager.notificationhubs.fluent.models.PrivateEndpointConnectionResourceInner;
import com.azure.resourcemanager.notificationhubs.models.PrivateEndpointConnectionProperties;
import com.azure.resourcemanager.notificationhubs.models.PrivateLinkConnectionStatus;
import com.azure.resourcemanager.notificationhubs.models.RemotePrivateEndpointConnection;
import com.azure.resourcemanager.notificationhubs.models.RemotePrivateLinkServiceConnectionState;

/**
 * Samples for PrivateEndpointConnections Update.
 */
public final class PrivateEndpointConnectionsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/preview/2023-10-01-preview/examples/
     * Namespaces/PrivateEndpointConnectionUpdate.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Update.
     * 
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void
        privateEndpointConnectionsUpdate(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.privateEndpointConnections().update("5ktrial", "nh-sdk-ns",
            "nh-sdk-ns.1fa229cd-bf3f-47f0-8c49-afb36723997e",
            new PrivateEndpointConnectionResourceInner().withProperties(new PrivateEndpointConnectionProperties()
                .withPrivateEndpoint(new RemotePrivateEndpointConnection()).withPrivateLinkServiceConnectionState(
                    new RemotePrivateLinkServiceConnectionState().withStatus(PrivateLinkConnectionStatus.APPROVED))),
            com.azure.core.util.Context.NONE);
    }
}
```

