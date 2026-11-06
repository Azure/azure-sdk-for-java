# Code snippets and samples


## Operations

- [List](#operations_list)

## UpdateInstances

- [CheckNameAvailability](#updateinstances_checknameavailability)
- [Create](#updateinstances_create)
- [Delete](#updateinstances_delete)
- [GetByResourceGroup](#updateinstances_getbyresourcegroup)
- [LinkInitiate](#updateinstances_linkinitiate)
- [LinkNotify](#updateinstances_linknotify)
- [LinkPreflight](#updateinstances_linkpreflight)
- [LinkUpdate](#updateinstances_linkupdate)
- [List](#updateinstances_list)
- [ListByResourceGroup](#updateinstances_listbyresourcegroup)
- [Update](#updateinstances_update)
### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2026-11-02-preview/Operations_List.json
     */
    /**
     * Sample code: Gets the list of operations.
     * 
     * @param manager Entry point to DeviceRegistrySoftwareUpdateManager.
     */
    public static void getsTheListOfOperations(
        com.azure.resourcemanager.deviceregistry.softwareupdate.DeviceRegistrySoftwareUpdateManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### UpdateInstances_CheckNameAvailability

```java
import com.azure.resourcemanager.deviceregistry.softwareupdate.models.CheckNameAvailabilityRequest;

/**
 * Samples for UpdateInstances CheckNameAvailability.
 */
public final class UpdateInstancesCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: 2026-11-02-preview/UpdateInstances_CheckNameAvailability.json
     */
    /**
     * Sample code: Check Update Instance name availability.
     * 
     * @param manager Entry point to DeviceRegistrySoftwareUpdateManager.
     */
    public static void checkUpdateInstanceNameAvailability(
        com.azure.resourcemanager.deviceregistry.softwareupdate.DeviceRegistrySoftwareUpdateManager manager) {
        manager.updateInstances()
            .checkNameAvailabilityWithResponse(new CheckNameAvailabilityRequest().withName("contoso")
                .withType("Microsoft.DeviceUpdate/updateInstances"), com.azure.core.util.Context.NONE);
    }
}
```

### UpdateInstances_Create

```java
import com.azure.resourcemanager.deviceregistry.softwareupdate.fluent.models.UpdateInstancePropertiesInner;
import com.azure.resourcemanager.deviceregistry.softwareupdate.models.ManagedServiceIdentity;
import com.azure.resourcemanager.deviceregistry.softwareupdate.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.deviceregistry.softwareupdate.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for UpdateInstances Create.
 */
public final class UpdateInstancesCreateSamples {
    /*
     * x-ms-original-file: 2026-11-02-preview/UpdateInstances_Create.json
     */
    /**
     * Sample code: Creates or updates an Update Instance.
     * 
     * @param manager Entry point to DeviceRegistrySoftwareUpdateManager.
     */
    public static void createsOrUpdatesAnUpdateInstance(
        com.azure.resourcemanager.deviceregistry.softwareupdate.DeviceRegistrySoftwareUpdateManager manager) {
        manager.updateInstances()
            .define("contoso")
            .withRegion("eastus2")
            .withExistingResourceGroup("test-rg")
            .withTags(mapOf("env", "prod"))
            .withProperties(new UpdateInstancePropertiesInner())
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-mi",
                    new UserAssignedIdentity())))
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

### UpdateInstances_Delete

```java
/**
 * Samples for UpdateInstances Delete.
 */
public final class UpdateInstancesDeleteSamples {
    /*
     * x-ms-original-file: 2026-11-02-preview/UpdateInstances_Delete.json
     */
    /**
     * Sample code: Deletes an update instance.
     * 
     * @param manager Entry point to DeviceRegistrySoftwareUpdateManager.
     */
    public static void deletesAnUpdateInstance(
        com.azure.resourcemanager.deviceregistry.softwareupdate.DeviceRegistrySoftwareUpdateManager manager) {
        manager.updateInstances().delete("test-rg", "contoso", com.azure.core.util.Context.NONE);
    }
}
```

### UpdateInstances_GetByResourceGroup

```java
/**
 * Samples for UpdateInstances GetByResourceGroup.
 */
public final class UpdateInstancesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-11-02-preview/UpdateInstances_Get.json
     */
    /**
     * Sample code: Gets Update Instance details.
     * 
     * @param manager Entry point to DeviceRegistrySoftwareUpdateManager.
     */
    public static void getsUpdateInstanceDetails(
        com.azure.resourcemanager.deviceregistry.softwareupdate.DeviceRegistrySoftwareUpdateManager manager) {
        manager.updateInstances()
            .getByResourceGroupWithResponse("test-rg", "contoso", com.azure.core.util.Context.NONE);
    }
}
```

### UpdateInstances_LinkInitiate

```java
import com.azure.resourcemanager.deviceregistry.softwareupdate.models.InboundCallerIdentity;
import com.azure.resourcemanager.deviceregistry.softwareupdate.models.LinkInitiateRequest;
import com.azure.resourcemanager.deviceregistry.softwareupdate.models.ManagedServiceIdentityType;

/**
 * Samples for UpdateInstances LinkInitiate.
 */
public final class UpdateInstancesLinkInitiateSamples {
    /*
     * x-ms-original-file: 2026-11-02-preview/UpdateInstances_LinkInitiate.json
     */
    /**
     * Sample code: Initiate account linking.
     * 
     * @param manager Entry point to DeviceRegistrySoftwareUpdateManager.
     */
    public static void initiateAccountLinking(
        com.azure.resourcemanager.deviceregistry.softwareupdate.DeviceRegistrySoftwareUpdateManager manager) {
        manager.updateInstances()
            .linkInitiate("test-rg", "contoso", new LinkInitiateRequest().withNamespaceResourceId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.DeviceRegistry/namespaces/contoso-ns")
                .withNamespaceUuid("5d8b3e92-1f4c-4e1a-9b27-fe2a7b8d2a31")
                .withDataAddress("eastus2.api.deviceregistry.com")
                .withInboundCallerIdentity(
                    new InboundCallerIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED)),
                com.azure.core.util.Context.NONE);
    }
}
```

### UpdateInstances_LinkNotify

```java
import com.azure.resourcemanager.deviceregistry.softwareupdate.models.LinkNotifyAction;
import com.azure.resourcemanager.deviceregistry.softwareupdate.models.LinkNotifyRequest;

/**
 * Samples for UpdateInstances LinkNotify.
 */
public final class UpdateInstancesLinkNotifySamples {
    /*
     * x-ms-original-file: 2026-11-02-preview/UpdateInstances_LinkNotify.json
     */
    /**
     * Sample code: Notify linking state change (commit).
     * 
     * @param manager Entry point to DeviceRegistrySoftwareUpdateManager.
     */
    public static void notifyLinkingStateChangeCommit(
        com.azure.resourcemanager.deviceregistry.softwareupdate.DeviceRegistrySoftwareUpdateManager manager) {
        manager.updateInstances()
            .linkNotify("test-rg", "contoso", new LinkNotifyRequest().withAction(LinkNotifyAction.COMMIT),
                com.azure.core.util.Context.NONE);
    }
}
```

### UpdateInstances_LinkPreflight

```java
import com.azure.resourcemanager.deviceregistry.softwareupdate.models.InboundCallerIdentity;
import com.azure.resourcemanager.deviceregistry.softwareupdate.models.LinkPreflightRequest;
import com.azure.resourcemanager.deviceregistry.softwareupdate.models.ManagedServiceIdentityType;

/**
 * Samples for UpdateInstances LinkPreflight.
 */
public final class UpdateInstancesLinkPreflightSamples {
    /*
     * x-ms-original-file: 2026-11-02-preview/UpdateInstances_LinkPreflight.json
     */
    /**
     * Sample code: Preflight check for account linking.
     * 
     * @param manager Entry point to DeviceRegistrySoftwareUpdateManager.
     */
    public static void preflightCheckForAccountLinking(
        com.azure.resourcemanager.deviceregistry.softwareupdate.DeviceRegistrySoftwareUpdateManager manager) {
        manager.updateInstances()
            .linkPreflightWithResponse("test-rg", "contoso", new LinkPreflightRequest().withNamespaceResourceId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.DeviceRegistry/namespaces/contoso-ns")
                .withNamespaceUuid("5d8b3e92-1f4c-4e1a-9b27-fe2a7b8d2a31")
                .withInboundCallerIdentity(
                    new InboundCallerIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED)),
                com.azure.core.util.Context.NONE);
    }
}
```

### UpdateInstances_LinkUpdate

```java
import com.azure.resourcemanager.deviceregistry.softwareupdate.models.InboundCallerIdentity;
import com.azure.resourcemanager.deviceregistry.softwareupdate.models.LinkUpdateRequest;
import com.azure.resourcemanager.deviceregistry.softwareupdate.models.ManagedServiceIdentityType;

/**
 * Samples for UpdateInstances LinkUpdate.
 */
public final class UpdateInstancesLinkUpdateSamples {
    /*
     * x-ms-original-file: 2026-11-02-preview/UpdateInstances_LinkUpdate.json
     */
    /**
     * Sample code: Update linking properties.
     * 
     * @param manager Entry point to DeviceRegistrySoftwareUpdateManager.
     */
    public static void updateLinkingProperties(
        com.azure.resourcemanager.deviceregistry.softwareupdate.DeviceRegistrySoftwareUpdateManager manager) {
        manager.updateInstances()
            .linkUpdate("test-rg", "contoso", new LinkUpdateRequest().withDataAddress("eastus2.api.deviceregistry.com")
                .withInboundCallerIdentity(new InboundCallerIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentity(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-mi")),
                com.azure.core.util.Context.NONE);
    }
}
```

### UpdateInstances_List

```java
/**
 * Samples for UpdateInstances List.
 */
public final class UpdateInstancesListSamples {
    /*
     * x-ms-original-file: 2026-11-02-preview/UpdateInstances_ListBySubscription.json
     */
    /**
     * Sample code: Gets list of Update Instances by Subscription.
     * 
     * @param manager Entry point to DeviceRegistrySoftwareUpdateManager.
     */
    public static void getsListOfUpdateInstancesBySubscription(
        com.azure.resourcemanager.deviceregistry.softwareupdate.DeviceRegistrySoftwareUpdateManager manager) {
        manager.updateInstances().list(com.azure.core.util.Context.NONE);
    }
}
```

### UpdateInstances_ListByResourceGroup

```java
/**
 * Samples for UpdateInstances ListByResourceGroup.
 */
public final class UpdateInstancesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-11-02-preview/UpdateInstances_ListByResourceGroup.json
     */
    /**
     * Sample code: Gets list of Update Instances.
     * 
     * @param manager Entry point to DeviceRegistrySoftwareUpdateManager.
     */
    public static void getsListOfUpdateInstances(
        com.azure.resourcemanager.deviceregistry.softwareupdate.DeviceRegistrySoftwareUpdateManager manager) {
        manager.updateInstances().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### UpdateInstances_Update

```java
import com.azure.resourcemanager.deviceregistry.softwareupdate.models.UpdateInstance;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for UpdateInstances Update.
 */
public final class UpdateInstancesUpdateSamples {
    /*
     * x-ms-original-file: 2026-11-02-preview/UpdateInstances_Update.json
     */
    /**
     * Sample code: Updates Update Instance.
     * 
     * @param manager Entry point to DeviceRegistrySoftwareUpdateManager.
     */
    public static void updatesUpdateInstance(
        com.azure.resourcemanager.deviceregistry.softwareupdate.DeviceRegistrySoftwareUpdateManager manager) {
        UpdateInstance resource = manager.updateInstances()
            .getByResourceGroupWithResponse("test-rg", "contoso", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("tagKey", "fakeTokenPlaceholder")).apply();
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

