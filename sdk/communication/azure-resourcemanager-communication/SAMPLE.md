# Code snippets and samples


## CommunicationService

- [CheckNameAvailability](#communicationservice_checknameavailability)
- [CreateOrUpdate](#communicationservice_createorupdate)
- [Delete](#communicationservice_delete)
- [GetByResourceGroup](#communicationservice_getbyresourcegroup)
- [LinkNotificationHub](#communicationservice_linknotificationhub)
- [List](#communicationservice_list)
- [ListByResourceGroup](#communicationservice_listbyresourcegroup)
- [ListKeys](#communicationservice_listkeys)
- [RegenerateKey](#communicationservice_regeneratekey)
- [Update](#communicationservice_update)
### CommunicationService_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.communication.models.NameAvailabilityParameters;

/** Samples for CommunicationService CheckNameAvailability. */
public final class CommunicationServiceCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/stable/2020-08-20/examples/checkNameAvailabilityAvailable.json
     */
    /**
     * Sample code: Check name availability available.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void checkNameAvailabilityAvailable(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager
            .communicationServices()
            .checkNameAvailabilityWithResponse(
                new NameAvailabilityParameters()
                    .withType("Microsoft.Communication/CommunicationServices")
                    .withName("MyCommunicationService"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/stable/2020-08-20/examples/checkNameAvailabilityUnavailable.json
     */
    /**
     * Sample code: Check name availability unavailable.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void checkNameAvailabilityUnavailable(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager
            .communicationServices()
            .checkNameAvailabilityWithResponse(
                new NameAvailabilityParameters()
                    .withType("Microsoft.Communication/CommunicationServices")
                    .withName("MyCommunicationService"),
                Context.NONE);
    }
}
```

### CommunicationService_CreateOrUpdate

```java
/** Samples for CommunicationService CreateOrUpdate. */
public final class CommunicationServiceCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/stable/2020-08-20/examples/createOrUpdate.json
     */
    /**
     * Sample code: Create or update resource.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void createOrUpdateResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager
            .communicationServices()
            .define("MyCommunicationResource")
            .withExistingResourceGroup("MyResourceGroup")
            .withRegion("Global")
            .withDataLocation("United States")
            .create();
    }
}
```

### CommunicationService_Delete

```java
import com.azure.core.util.Context;

/** Samples for CommunicationService Delete. */
public final class CommunicationServiceDeleteSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/stable/2020-08-20/examples/delete.json
     */
    /**
     * Sample code: Delete resource.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void deleteResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.communicationServices().delete("MyResourceGroup", "MyCommunicationResource", Context.NONE);
    }
}
```

### CommunicationService_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for CommunicationService GetByResourceGroup. */
public final class CommunicationServiceGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/stable/2020-08-20/examples/get.json
     */
    /**
     * Sample code: Get resource.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void getResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager
            .communicationServices()
            .getByResourceGroupWithResponse("MyResourceGroup", "MyCommunicationResource", Context.NONE);
    }
}
```

### CommunicationService_LinkNotificationHub

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.communication.models.LinkNotificationHubParameters;

/** Samples for CommunicationService LinkNotificationHub. */
public final class CommunicationServiceLinkNotificationHubSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/stable/2020-08-20/examples/linkNotificationHub.json
     */
    /**
     * Sample code: Link notification hub.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void linkNotificationHub(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager
            .communicationServices()
            .linkNotificationHubWithResponse(
                "MyResourceGroup",
                "MyCommunicationResource",
                new LinkNotificationHubParameters()
                    .withResourceId(
                        "/subscriptions/12345/resourceGroups/MyOtherResourceGroup/providers/Microsoft.NotificationHubs/namespaces/MyNamespace/notificationHubs/MyHub")
                    .withConnectionString("Endpoint=sb://MyNamespace.servicebus.windows.net/;SharedAccessKey=abcd1234"),
                Context.NONE);
    }
}
```

### CommunicationService_List

```java
import com.azure.core.util.Context;

/** Samples for CommunicationService List. */
public final class CommunicationServiceListSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/stable/2020-08-20/examples/listBySubscription.json
     */
    /**
     * Sample code: List by subscription.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void listBySubscription(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.communicationServices().list(Context.NONE);
    }
}
```

### CommunicationService_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for CommunicationService ListByResourceGroup. */
public final class CommunicationServiceListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/stable/2020-08-20/examples/listByResourceGroup.json
     */
    /**
     * Sample code: List by resource group.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void listByResourceGroup(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.communicationServices().listByResourceGroup("MyResourceGroup", Context.NONE);
    }
}
```

### CommunicationService_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for CommunicationService ListKeys. */
public final class CommunicationServiceListKeysSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/stable/2020-08-20/examples/listKeys.json
     */
    /**
     * Sample code: List keys.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void listKeys(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager
            .communicationServices()
            .listKeysWithResponse("MyResourceGroup", "MyCommunicationResource", Context.NONE);
    }
}
```

### CommunicationService_RegenerateKey

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.communication.models.KeyType;
import com.azure.resourcemanager.communication.models.RegenerateKeyParameters;

/** Samples for CommunicationService RegenerateKey. */
public final class CommunicationServiceRegenerateKeySamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/stable/2020-08-20/examples/regenerateKey.json
     */
    /**
     * Sample code: Regenerate key.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void regenerateKey(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager
            .communicationServices()
            .regenerateKeyWithResponse(
                "MyResourceGroup",
                "MyCommunicationResource",
                new RegenerateKeyParameters().withKeyType(KeyType.PRIMARY),
                Context.NONE);
    }
}
```

### CommunicationService_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.communication.models.CommunicationServiceResource;
import java.util.HashMap;
import java.util.Map;

/** Samples for CommunicationService Update. */
public final class CommunicationServiceUpdateSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/stable/2020-08-20/examples/update.json
     */
    /**
     * Sample code: Update resource.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void updateResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        CommunicationServiceResource resource =
            manager
                .communicationServices()
                .getByResourceGroupWithResponse("MyResourceGroup", "MyCommunicationResource", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("newTag", "newVal")).apply();
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

