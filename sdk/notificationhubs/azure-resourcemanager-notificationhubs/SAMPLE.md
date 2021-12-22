# Code snippets and samples


## Namespaces

- [CheckAvailability](#namespaces_checkavailability)
- [CreateOrUpdate](#namespaces_createorupdate)
- [CreateOrUpdateAuthorizationRule](#namespaces_createorupdateauthorizationrule)
- [Delete](#namespaces_delete)
- [DeleteAuthorizationRule](#namespaces_deleteauthorizationrule)
- [GetAuthorizationRule](#namespaces_getauthorizationrule)
- [GetByResourceGroup](#namespaces_getbyresourcegroup)
- [List](#namespaces_list)
- [ListAuthorizationRules](#namespaces_listauthorizationrules)
- [ListByResourceGroup](#namespaces_listbyresourcegroup)
- [ListKeys](#namespaces_listkeys)
- [Patch](#namespaces_patch)
- [RegenerateKeys](#namespaces_regeneratekeys)

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
- [Patch](#notificationhubs_patch)
- [RegenerateKeys](#notificationhubs_regeneratekeys)

## Operations

- [List](#operations_list)
### Namespaces_CheckAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.notificationhubs.models.CheckAvailabilityParameters;

/** Samples for Namespaces CheckAvailability. */
public final class NamespacesCheckAvailabilitySamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/Namespaces/NHNameSpaceCheckNameAvailability.json
     */
    /**
     * Sample code: NameSpaceCheckNameAvailability.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void nameSpaceCheckNameAvailability(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager
            .namespaces()
            .checkAvailabilityWithResponse(
                new CheckAvailabilityParameters().withName("sdk-Namespace-2924"), Context.NONE);
    }
}
```

### Namespaces_CreateOrUpdate

```java
import com.azure.resourcemanager.notificationhubs.models.Sku;
import com.azure.resourcemanager.notificationhubs.models.SkuName;
import java.util.HashMap;
import java.util.Map;

/** Samples for Namespaces CreateOrUpdate. */
public final class NamespacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/Namespaces/NHNameSpaceCreate.json
     */
    /**
     * Sample code: NameSpaceCreate.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void nameSpaceCreate(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager
            .namespaces()
            .define("nh-sdk-ns")
            .withLocation("South Central US")
            .withExistingResourceGroup("5ktrial")
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
            .withSku(new Sku().withName(SkuName.STANDARD).withTier("Standard"))
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
import com.azure.resourcemanager.notificationhubs.fluent.models.SharedAccessAuthorizationRuleProperties;
import com.azure.resourcemanager.notificationhubs.models.AccessRights;
import java.util.Arrays;

/** Samples for Namespaces CreateOrUpdateAuthorizationRule. */
public final class NamespacesCreateOrUpdateAuthorizationRuleSamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/Namespaces/NHNameSpaceAuthorizationRuleCreate.json
     */
    /**
     * Sample code: NameSpaceAuthorizationRuleCreate.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void nameSpaceAuthorizationRuleCreate(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager
            .namespaces()
            .defineAuthorizationRule("sdk-AuthRules-1788")
            .withExistingNamespace("5ktrial", "nh-sdk-ns")
            .withProperties(
                new SharedAccessAuthorizationRuleProperties()
                    .withRights(Arrays.asList(AccessRights.LISTEN, AccessRights.SEND)))
            .create();
    }
}
```

### Namespaces_Delete

```java
import com.azure.core.util.Context;

/** Samples for Namespaces Delete. */
public final class NamespacesDeleteSamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/Namespaces/NHNameSpaceDelete.json
     */
    /**
     * Sample code: NameSpaceDelete.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void nameSpaceDelete(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.namespaces().delete("5ktrial", "nh-sdk-ns", Context.NONE);
    }
}
```

### Namespaces_DeleteAuthorizationRule

```java
import com.azure.core.util.Context;

/** Samples for Namespaces DeleteAuthorizationRule. */
public final class NamespacesDeleteAuthorizationRuleSamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/Namespaces/NHNameSpaceAuthorizationRuleDelete.json
     */
    /**
     * Sample code: NameSpaceAuthorizationRuleDelete.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void nameSpaceAuthorizationRuleDelete(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager
            .namespaces()
            .deleteAuthorizationRuleWithResponse("5ktrial", "nh-sdk-ns", "RootManageSharedAccessKey", Context.NONE);
    }
}
```

### Namespaces_GetAuthorizationRule

```java
import com.azure.core.util.Context;

/** Samples for Namespaces GetAuthorizationRule. */
public final class NamespacesGetAuthorizationRuleSamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/Namespaces/NHNameSpaceAuthorizationRuleGet.json
     */
    /**
     * Sample code: NameSpaceAuthorizationRuleGet.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void nameSpaceAuthorizationRuleGet(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager
            .namespaces()
            .getAuthorizationRuleWithResponse("5ktrial", "nh-sdk-ns", "RootManageSharedAccessKey", Context.NONE);
    }
}
```

### Namespaces_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Namespaces GetByResourceGroup. */
public final class NamespacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/Namespaces/NHNameSpaceGet.json
     */
    /**
     * Sample code: NameSpaceGet.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void nameSpaceGet(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.namespaces().getByResourceGroupWithResponse("5ktrial", "nh-sdk-ns", Context.NONE);
    }
}
```

### Namespaces_List

```java
import com.azure.core.util.Context;

/** Samples for Namespaces List. */
public final class NamespacesListSamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/Namespaces/NHNameSpaceList.json
     */
    /**
     * Sample code: NameSpaceList.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void nameSpaceList(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
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
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/Namespaces/NHNameSpaceAuthorizationRuleListAll.json
     */
    /**
     * Sample code: NameSpaceAuthorizationRuleListAll.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void nameSpaceAuthorizationRuleListAll(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.namespaces().listAuthorizationRules("5ktrial", "nh-sdk-ns", Context.NONE);
    }
}
```

### Namespaces_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Namespaces ListByResourceGroup. */
public final class NamespacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/Namespaces/NHNameSpaceListByResourceGroup.json
     */
    /**
     * Sample code: NameSpaceListByResourceGroup.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void nameSpaceListByResourceGroup(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.namespaces().listByResourceGroup("5ktrial", Context.NONE);
    }
}
```

### Namespaces_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for Namespaces ListKeys. */
public final class NamespacesListKeysSamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/Namespaces/NHNameSpaceAuthorizationRuleListKey.json
     */
    /**
     * Sample code: NameSpaceAuthorizationRuleListKey.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void nameSpaceAuthorizationRuleListKey(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.namespaces().listKeysWithResponse("5ktrial", "nh-sdk-ns", "RootManageSharedAccessKey", Context.NONE);
    }
}
```

### Namespaces_Patch

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.notificationhubs.models.NamespaceResource;
import com.azure.resourcemanager.notificationhubs.models.Sku;
import com.azure.resourcemanager.notificationhubs.models.SkuName;
import java.util.HashMap;
import java.util.Map;

/** Samples for Namespaces Patch. */
public final class NamespacesPatchSamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/Namespaces/NHNameSpaceUpdate.json
     */
    /**
     * Sample code: NameSpaceUpdate.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void nameSpaceUpdate(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        NamespaceResource resource =
            manager.namespaces().getByResourceGroupWithResponse("5ktrial", "nh-sdk-ns", Context.NONE).getValue();
        resource
            .update()
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
            .withSku(new Sku().withName(SkuName.STANDARD).withTier("Standard"))
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

### Namespaces_RegenerateKeys

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.notificationhubs.models.PolicykeyResource;

/** Samples for Namespaces RegenerateKeys. */
public final class NamespacesRegenerateKeysSamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/Namespaces/NHNameSpaceAuthorizationRuleRegenrateKey.json
     */
    /**
     * Sample code: NameSpaceAuthorizationRuleRegenerateKey.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void nameSpaceAuthorizationRuleRegenerateKey(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager
            .namespaces()
            .regenerateKeysWithResponse(
                "5ktrial",
                "nh-sdk-ns",
                "RootManageSharedAccessKey",
                new PolicykeyResource().withPolicyKey("PrimaryKey"),
                Context.NONE);
    }
}
```

### NotificationHubs_CheckNotificationHubAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.notificationhubs.models.CheckAvailabilityParameters;

/** Samples for NotificationHubs CheckNotificationHubAvailability. */
public final class NotificationHubsCheckNotificationHubAvailabilitySamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/NotificationHubs/NotificationHubCheckNameAvailability.json
     */
    /**
     * Sample code: notificationHubCheckNameAvailability.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void notificationHubCheckNameAvailability(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager
            .notificationHubs()
            .checkNotificationHubAvailabilityWithResponse(
                "5ktrial",
                "locp-newns",
                new CheckAvailabilityParameters().withName("sdktest").withLocation("West Europe"),
                Context.NONE);
    }
}
```

### NotificationHubs_CreateOrUpdate

```java
/** Samples for NotificationHubs CreateOrUpdate. */
public final class NotificationHubsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/NotificationHubs/NotificationHubCreate.json
     */
    /**
     * Sample code: NotificationHubCreate.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void notificationHubCreate(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager
            .notificationHubs()
            .define("nh-sdk-hub")
            .withRegion("eastus")
            .withExistingNamespace("5ktrial", "nh-sdk-ns")
            .create();
    }
}
```

### NotificationHubs_CreateOrUpdateAuthorizationRule

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.notificationhubs.fluent.models.SharedAccessAuthorizationRuleProperties;
import com.azure.resourcemanager.notificationhubs.models.AccessRights;
import com.azure.resourcemanager.notificationhubs.models.SharedAccessAuthorizationRuleCreateOrUpdateParameters;
import java.util.Arrays;

/** Samples for NotificationHubs CreateOrUpdateAuthorizationRule. */
public final class NotificationHubsCreateOrUpdateAuthorizationRuleSamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/NotificationHubs/NotificationHubAuthorizationRuleCreate.json
     */
    /**
     * Sample code: NotificationHubAuthorizationRuleCreate.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void notificationHubAuthorizationRuleCreate(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager
            .notificationHubs()
            .createOrUpdateAuthorizationRuleWithResponse(
                "5ktrial",
                "nh-sdk-ns",
                "nh-sdk-hub",
                "DefaultListenSharedAccessSignature",
                new SharedAccessAuthorizationRuleCreateOrUpdateParameters()
                    .withProperties(
                        new SharedAccessAuthorizationRuleProperties()
                            .withRights(Arrays.asList(AccessRights.LISTEN, AccessRights.SEND))),
                Context.NONE);
    }
}
```

### NotificationHubs_DebugSend

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import java.io.IOException;

/** Samples for NotificationHubs DebugSend. */
public final class NotificationHubsDebugSendSamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/NotificationHubs/NotificationHubDebugSend.json
     */
    /**
     * Sample code: debugsend.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void debugsend(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager)
        throws IOException {
        manager
            .notificationHubs()
            .debugSendWithResponse(
                "5ktrial",
                "nh-sdk-ns",
                "nh-sdk-hub",
                SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize("{\"data\":{\"message\":\"Hello\"}}", Object.class, SerializerEncoding.JSON),
                Context.NONE);
    }
}
```

### NotificationHubs_Delete

```java
import com.azure.core.util.Context;

/** Samples for NotificationHubs Delete. */
public final class NotificationHubsDeleteSamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/NotificationHubs/NotificationHubDelete.json
     */
    /**
     * Sample code: NotificationHubDelete.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void notificationHubDelete(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.notificationHubs().deleteWithResponse("5ktrial", "nh-sdk-ns", "nh-sdk-hub", Context.NONE);
    }
}
```

### NotificationHubs_DeleteAuthorizationRule

```java
import com.azure.core.util.Context;

/** Samples for NotificationHubs DeleteAuthorizationRule. */
public final class NotificationHubsDeleteAuthorizationRuleSamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/NotificationHubs/NotificationHubAuthorizationRuleDelete.json
     */
    /**
     * Sample code: NotificationHubAuthorizationRuleDelete.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void notificationHubAuthorizationRuleDelete(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager
            .notificationHubs()
            .deleteAuthorizationRuleWithResponse(
                "5ktrial", "nh-sdk-ns", "nh-sdk-hub", "DefaultListenSharedAccessSignature", Context.NONE);
    }
}
```

### NotificationHubs_Get

```java
import com.azure.core.util.Context;

/** Samples for NotificationHubs Get. */
public final class NotificationHubsGetSamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/NotificationHubs/NotificationHubGet.json
     */
    /**
     * Sample code: NotificationHubGet.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void notificationHubGet(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.notificationHubs().getWithResponse("5ktrial", "nh-sdk-ns", "nh-sdk-hub", Context.NONE);
    }
}
```

### NotificationHubs_GetAuthorizationRule

```java
import com.azure.core.util.Context;

/** Samples for NotificationHubs GetAuthorizationRule. */
public final class NotificationHubsGetAuthorizationRuleSamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/NotificationHubs/NotificationHubAuthorizationRuleGet.json
     */
    /**
     * Sample code: NotificationHubAuthorizationRuleGet.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void notificationHubAuthorizationRuleGet(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager
            .notificationHubs()
            .getAuthorizationRuleWithResponse(
                "5ktrial", "nh-sdk-ns", "nh-sdk-hub", "DefaultListenSharedAccessSignature", Context.NONE);
    }
}
```

### NotificationHubs_GetPnsCredentials

```java
import com.azure.core.util.Context;

/** Samples for NotificationHubs GetPnsCredentials. */
public final class NotificationHubsGetPnsCredentialsSamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/NotificationHubs/NotificationHubPnsCredentials.json
     */
    /**
     * Sample code: notificationHubPnsCredentials.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void notificationHubPnsCredentials(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.notificationHubs().getPnsCredentialsWithResponse("5ktrial", "nh-sdk-ns", "nh-sdk-hub", Context.NONE);
    }
}
```

### NotificationHubs_List

```java
import com.azure.core.util.Context;

/** Samples for NotificationHubs List. */
public final class NotificationHubsListSamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/NotificationHubs/NotificationHubListByNameSpace.json
     */
    /**
     * Sample code: NotificationHubListByNameSpace.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void notificationHubListByNameSpace(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.notificationHubs().list("5ktrial", "nh-sdk-ns", Context.NONE);
    }
}
```

### NotificationHubs_ListAuthorizationRules

```java
import com.azure.core.util.Context;

/** Samples for NotificationHubs ListAuthorizationRules. */
public final class NotificationHubsListAuthorizationRulesSamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/NotificationHubs/NotificationHubAuthorizationRuleListAll.json
     */
    /**
     * Sample code: NotificationHubAuthorizationRuleListAll.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void notificationHubAuthorizationRuleListAll(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.notificationHubs().listAuthorizationRules("5ktrial", "nh-sdk-ns", "nh-sdk-hub", Context.NONE);
    }
}
```

### NotificationHubs_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for NotificationHubs ListKeys. */
public final class NotificationHubsListKeysSamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/NotificationHubs/NotificationHubAuthorizationRuleListKey.json
     */
    /**
     * Sample code: NotificationHubAuthorizationRuleListKey.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void notificationHubAuthorizationRuleListKey(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager
            .notificationHubs()
            .listKeysWithResponse("5ktrial", "nh-sdk-ns", "nh-sdk-hub", "sdk-AuthRules-5800", Context.NONE);
    }
}
```

### NotificationHubs_Patch

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.notificationhubs.models.NotificationHubResource;

/** Samples for NotificationHubs Patch. */
public final class NotificationHubsPatchSamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/NotificationHubs/NotificationHubPatch.json
     */
    /**
     * Sample code: NotificationHubPatch.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void notificationHubPatch(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        NotificationHubResource resource =
            manager
                .notificationHubs()
                .getWithResponse("sdkresourceGroup", "nh-sdk-ns", "sdk-notificationHubs-8708", Context.NONE)
                .getValue();
        resource.update().apply();
    }
}
```

### NotificationHubs_RegenerateKeys

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.notificationhubs.models.PolicykeyResource;

/** Samples for NotificationHubs RegenerateKeys. */
public final class NotificationHubsRegenerateKeysSamples {
    /*
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/NotificationHubs/NotificationHubAuthorizationRuleRegenrateKey.json
     */
    /**
     * Sample code: NotificationHubAuthorizationRuleRegenrateKey.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void notificationHubAuthorizationRuleRegenrateKey(
        com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager
            .notificationHubs()
            .regenerateKeysWithResponse(
                "5ktrial",
                "nh-sdk-ns",
                "nh-sdk-hub",
                "DefaultListenSharedAccessSignature",
                new PolicykeyResource().withPolicyKey("PrimaryKey"),
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
     * x-ms-original-file: specification/notificationhubs/resource-manager/Microsoft.NotificationHubs/stable/2017-04-01/examples/NHOperationsList.json
     */
    /**
     * Sample code: OperationsList.
     *
     * @param manager Entry point to NotificationHubsManager.
     */
    public static void operationsList(com.azure.resourcemanager.notificationhubs.NotificationHubsManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

