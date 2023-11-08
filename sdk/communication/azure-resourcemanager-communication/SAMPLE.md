# Code snippets and samples


## CommunicationServices

- [CheckNameAvailability](#communicationservices_checknameavailability)
- [CreateOrUpdate](#communicationservices_createorupdate)
- [Delete](#communicationservices_delete)
- [GetByResourceGroup](#communicationservices_getbyresourcegroup)
- [LinkNotificationHub](#communicationservices_linknotificationhub)
- [List](#communicationservices_list)
- [ListByResourceGroup](#communicationservices_listbyresourcegroup)
- [ListKeys](#communicationservices_listkeys)
- [RegenerateKey](#communicationservices_regeneratekey)
- [Update](#communicationservices_update)

## Domains

- [CancelVerification](#domains_cancelverification)
- [CreateOrUpdate](#domains_createorupdate)
- [Delete](#domains_delete)
- [Get](#domains_get)
- [InitiateVerification](#domains_initiateverification)
- [ListByEmailServiceResource](#domains_listbyemailserviceresource)
- [Update](#domains_update)

## EmailServices

- [CreateOrUpdate](#emailservices_createorupdate)
- [Delete](#emailservices_delete)
- [GetByResourceGroup](#emailservices_getbyresourcegroup)
- [List](#emailservices_list)
- [ListByResourceGroup](#emailservices_listbyresourcegroup)
- [ListVerifiedExchangeOnlineDomains](#emailservices_listverifiedexchangeonlinedomains)
- [Update](#emailservices_update)

## Operations

- [List](#operations_list)

## SenderUsernames

- [CreateOrUpdate](#senderusernames_createorupdate)
- [Delete](#senderusernames_delete)
- [Get](#senderusernames_get)
- [ListByDomains](#senderusernames_listbydomains)
### CommunicationServices_CheckNameAvailability

```java
import com.azure.resourcemanager.communication.models.NameAvailabilityParameters;

/** Samples for CommunicationServices CheckNameAvailability. */
public final class CommunicationServicesCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/communicationServices/checkNameAvailabilityAvailable.json
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
                    .withName("MyCommunicationService")
                    .withType("Microsoft.Communication/CommunicationServices"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/communicationServices/checkNameAvailabilityUnavailable.json
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
                    .withName("MyCommunicationService")
                    .withType("Microsoft.Communication/CommunicationServices"),
                com.azure.core.util.Context.NONE);
    }
}
```

### CommunicationServices_CreateOrUpdate

```java
import com.azure.resourcemanager.communication.models.ManagedServiceIdentity;
import com.azure.resourcemanager.communication.models.ManagedServiceIdentityType;

/** Samples for CommunicationServices CreateOrUpdate. */
public final class CommunicationServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/communicationServices/createOrUpdate.json
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
            .withRegion("Global")
            .withExistingResourceGroup("MyResourceGroup")
            .withDataLocation("United States")
            .create();
    }

    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/communicationServices/createOrUpdateWithSystemAssignedIdentity.json
     */
    /**
     * Sample code: Create or update resource with managed identity.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void createOrUpdateResourceWithManagedIdentity(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager
            .communicationServices()
            .define("MyCommunicationResource")
            .withRegion("Global")
            .withExistingResourceGroup("MyResourceGroup")
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
            .withDataLocation("United States")
            .create();
    }
}
```

### CommunicationServices_Delete

```java
/** Samples for CommunicationServices Delete. */
public final class CommunicationServicesDeleteSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/communicationServices/delete.json
     */
    /**
     * Sample code: Delete resource.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void deleteResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager
            .communicationServices()
            .delete("MyResourceGroup", "MyCommunicationResource", com.azure.core.util.Context.NONE);
    }
}
```

### CommunicationServices_GetByResourceGroup

```java
/** Samples for CommunicationServices GetByResourceGroup. */
public final class CommunicationServicesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/communicationServices/get.json
     */
    /**
     * Sample code: Get resource.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void getResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager
            .communicationServices()
            .getByResourceGroupWithResponse(
                "MyResourceGroup", "MyCommunicationResource", com.azure.core.util.Context.NONE);
    }
}
```

### CommunicationServices_LinkNotificationHub

```java
import com.azure.resourcemanager.communication.models.LinkNotificationHubParameters;

/** Samples for CommunicationServices LinkNotificationHub. */
public final class CommunicationServicesLinkNotificationHubSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/communicationServices/linkNotificationHub.json
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
                        "/subscriptions/11112222-3333-4444-5555-666677778888/resourceGroups/MyOtherResourceGroup/providers/Microsoft.NotificationHubs/namespaces/MyNamespace/notificationHubs/MyHub")
                    .withConnectionString("Endpoint=sb://MyNamespace.servicebus.windows.net/;SharedAccessKey=abcd1234"),
                com.azure.core.util.Context.NONE);
    }
}
```

### CommunicationServices_List

```java
/** Samples for CommunicationServices List. */
public final class CommunicationServicesListSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/communicationServices/listBySubscription.json
     */
    /**
     * Sample code: List by subscription.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void listBySubscription(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.communicationServices().list(com.azure.core.util.Context.NONE);
    }
}
```

### CommunicationServices_ListByResourceGroup

```java
/** Samples for CommunicationServices ListByResourceGroup. */
public final class CommunicationServicesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/communicationServices/listByResourceGroup.json
     */
    /**
     * Sample code: List by resource group.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void listByResourceGroup(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.communicationServices().listByResourceGroup("MyResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### CommunicationServices_ListKeys

```java
/** Samples for CommunicationServices ListKeys. */
public final class CommunicationServicesListKeysSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/communicationServices/listKeys.json
     */
    /**
     * Sample code: List keys.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void listKeys(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager
            .communicationServices()
            .listKeysWithResponse("MyResourceGroup", "MyCommunicationResource", com.azure.core.util.Context.NONE);
    }
}
```

### CommunicationServices_RegenerateKey

```java
import com.azure.resourcemanager.communication.models.KeyType;
import com.azure.resourcemanager.communication.models.RegenerateKeyParameters;

/** Samples for CommunicationServices RegenerateKey. */
public final class CommunicationServicesRegenerateKeySamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/communicationServices/regenerateKey.json
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
                com.azure.core.util.Context.NONE);
    }
}
```

### CommunicationServices_Update

```java
import com.azure.resourcemanager.communication.models.CommunicationServiceResource;
import com.azure.resourcemanager.communication.models.ManagedServiceIdentity;
import com.azure.resourcemanager.communication.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.communication.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/** Samples for CommunicationServices Update. */
public final class CommunicationServicesUpdateSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/communicationServices/update.json
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
                .getByResourceGroupWithResponse(
                    "MyResourceGroup", "MyCommunicationResource", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("newTag", "newVal")).apply();
    }

    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/communicationServices/updateWithUserAssignedIdentity.json
     */
    /**
     * Sample code: Update resource to add a User Assigned managed identity.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void updateResourceToAddAUserAssignedManagedIdentity(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        CommunicationServiceResource resource =
            manager
                .communicationServices()
                .getByResourceGroupWithResponse(
                    "MyResourceGroup", "MyCommunicationResource", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("newTag", "newVal"))
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(mapOf("/user/assigned/resource/id", new UserAssignedIdentity())))
            .apply();
    }

    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/communicationServices/updateWithSystemAssignedIdentity.json
     */
    /**
     * Sample code: Update resource to add a System Assigned managed identity.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void updateResourceToAddASystemAssignedManagedIdentity(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        CommunicationServiceResource resource =
            manager
                .communicationServices()
                .getByResourceGroupWithResponse(
                    "MyResourceGroup", "MyCommunicationResource", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("newTag", "newVal"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
            .apply();
    }

    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/communicationServices/updateRemoveSystemIdentity.json
     */
    /**
     * Sample code: Update resource to remove identity.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void updateResourceToRemoveIdentity(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        CommunicationServiceResource resource =
            manager
                .communicationServices()
                .getByResourceGroupWithResponse(
                    "MyResourceGroup", "MyCommunicationResource", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("newTag", "newVal"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE))
            .apply();
    }

    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/communicationServices/updateWithSystemAndUserIdentity.json
     */
    /**
     * Sample code: Update resource to add System and User managed identities.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void updateResourceToAddSystemAndUserManagedIdentities(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        CommunicationServiceResource resource =
            manager
                .communicationServices()
                .getByResourceGroupWithResponse(
                    "MyResourceGroup", "MyCommunicationResource", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("newTag", "newVal"))
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                    .withUserAssignedIdentities(mapOf("/user/assigned/resource/id", new UserAssignedIdentity())))
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

### Domains_CancelVerification

```java
import com.azure.resourcemanager.communication.models.VerificationParameter;
import com.azure.resourcemanager.communication.models.VerificationType;

/** Samples for Domains CancelVerification. */
public final class DomainsCancelVerificationSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/domains/cancelVerification.json
     */
    /**
     * Sample code: Cancel verification.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void cancelVerification(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager
            .domains()
            .cancelVerification(
                "MyResourceGroup",
                "MyEmailServiceResource",
                "mydomain.com",
                new VerificationParameter().withVerificationType(VerificationType.SPF),
                com.azure.core.util.Context.NONE);
    }
}
```

### Domains_CreateOrUpdate

```java
import com.azure.resourcemanager.communication.models.DomainManagement;

/** Samples for Domains CreateOrUpdate. */
public final class DomainsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/domains/createOrUpdate.json
     */
    /**
     * Sample code: Create or update Domains resource.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void createOrUpdateDomainsResource(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager
            .domains()
            .define("mydomain.com")
            .withRegion("Global")
            .withExistingEmailService("MyResourceGroup", "MyEmailServiceResource")
            .withDomainManagement(DomainManagement.CUSTOMER_MANAGED)
            .create();
    }
}
```

### Domains_Delete

```java
/** Samples for Domains Delete. */
public final class DomainsDeleteSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/domains/delete.json
     */
    /**
     * Sample code: Delete Domains resource.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void deleteDomainsResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager
            .domains()
            .delete("MyResourceGroup", "MyEmailServiceResource", "mydomain.com", com.azure.core.util.Context.NONE);
    }
}
```

### Domains_Get

```java
/** Samples for Domains Get. */
public final class DomainsGetSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/domains/get.json
     */
    /**
     * Sample code: Get Domains resource.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void getDomainsResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager
            .domains()
            .getWithResponse(
                "MyResourceGroup", "MyEmailServiceResource", "mydomain.com", com.azure.core.util.Context.NONE);
    }
}
```

### Domains_InitiateVerification

```java
import com.azure.resourcemanager.communication.models.VerificationParameter;
import com.azure.resourcemanager.communication.models.VerificationType;

/** Samples for Domains InitiateVerification. */
public final class DomainsInitiateVerificationSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/domains/initiateVerification.json
     */
    /**
     * Sample code: Initiate verification.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void initiateVerification(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager
            .domains()
            .initiateVerification(
                "MyResourceGroup",
                "MyEmailServiceResource",
                "mydomain.com",
                new VerificationParameter().withVerificationType(VerificationType.SPF),
                com.azure.core.util.Context.NONE);
    }
}
```

### Domains_ListByEmailServiceResource

```java
/** Samples for Domains ListByEmailServiceResource. */
public final class DomainsListByEmailServiceResourceSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/domains/listByEmailService.json
     */
    /**
     * Sample code: List Domains resources by EmailServiceName.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void listDomainsResourcesByEmailServiceName(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager
            .domains()
            .listByEmailServiceResource("MyResourceGroup", "MyEmailServiceResource", com.azure.core.util.Context.NONE);
    }
}
```

### Domains_Update

```java
import com.azure.resourcemanager.communication.models.DomainResource;
import com.azure.resourcemanager.communication.models.UserEngagementTracking;

/** Samples for Domains Update. */
public final class DomainsUpdateSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/domains/update.json
     */
    /**
     * Sample code: Update Domains resource.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void updateDomainsResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        DomainResource resource =
            manager
                .domains()
                .getWithResponse(
                    "MyResourceGroup", "MyEmailServiceResource", "mydomain.com", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withUserEngagementTracking(UserEngagementTracking.ENABLED).apply();
    }
}
```

### EmailServices_CreateOrUpdate

```java
/** Samples for EmailServices CreateOrUpdate. */
public final class EmailServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/emailServices/createOrUpdate.json
     */
    /**
     * Sample code: Create or update EmailService resource.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void createOrUpdateEmailServiceResource(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager
            .emailServices()
            .define("MyEmailServiceResource")
            .withRegion("Global")
            .withExistingResourceGroup("MyResourceGroup")
            .withDataLocation("United States")
            .create();
    }
}
```

### EmailServices_Delete

```java
/** Samples for EmailServices Delete. */
public final class EmailServicesDeleteSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/emailServices/delete.json
     */
    /**
     * Sample code: Delete EmailService resource.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void deleteEmailServiceResource(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.emailServices().delete("MyResourceGroup", "MyEmailServiceResource", com.azure.core.util.Context.NONE);
    }
}
```

### EmailServices_GetByResourceGroup

```java
/** Samples for EmailServices GetByResourceGroup. */
public final class EmailServicesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/emailServices/get.json
     */
    /**
     * Sample code: Get EmailService resource.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void getEmailServiceResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager
            .emailServices()
            .getByResourceGroupWithResponse(
                "MyResourceGroup", "MyEmailServiceResource", com.azure.core.util.Context.NONE);
    }
}
```

### EmailServices_List

```java
/** Samples for EmailServices List. */
public final class EmailServicesListSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/emailServices/listBySubscription.json
     */
    /**
     * Sample code: List EmailService resources by subscription.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void listEmailServiceResourcesBySubscription(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.emailServices().list(com.azure.core.util.Context.NONE);
    }
}
```

### EmailServices_ListByResourceGroup

```java
/** Samples for EmailServices ListByResourceGroup. */
public final class EmailServicesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/emailServices/listByResourceGroup.json
     */
    /**
     * Sample code: List EmailService resources by resource group.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void listEmailServiceResourcesByResourceGroup(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.emailServices().listByResourceGroup("MyResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### EmailServices_ListVerifiedExchangeOnlineDomains

```java
/** Samples for EmailServices ListVerifiedExchangeOnlineDomains. */
public final class EmailServicesListVerifiedExchangeOnlineDomainsSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/emailServices/getVerifiedExchangeOnlineDomains.json
     */
    /**
     * Sample code: Get verified Exchange Online domains.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void getVerifiedExchangeOnlineDomains(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.emailServices().listVerifiedExchangeOnlineDomainsWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### EmailServices_Update

```java
import com.azure.resourcemanager.communication.models.EmailServiceResource;
import java.util.HashMap;
import java.util.Map;

/** Samples for EmailServices Update. */
public final class EmailServicesUpdateSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/emailServices/update.json
     */
    /**
     * Sample code: Update EmailService resource.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void updateEmailServiceResource(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        EmailServiceResource resource =
            manager
                .emailServices()
                .getByResourceGroupWithResponse(
                    "MyResourceGroup", "MyEmailServiceResource", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("newTag", "newVal")).apply();
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

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/communicationServices/operationsList.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void operationsList(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### SenderUsernames_CreateOrUpdate

```java
/** Samples for SenderUsernames CreateOrUpdate. */
public final class SenderUsernamesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/senderUsernames/createOrUpdate.json
     */
    /**
     * Sample code: Create or update SenderUsernames resource.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void createOrUpdateSenderUsernamesResource(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager
            .senderUsernames()
            .define("contosoNewsAlerts")
            .withExistingDomain("contosoResourceGroup", "contosoEmailService", "contoso.com")
            .withUsername("contosoNewsAlerts")
            .withDisplayName("Contoso News Alerts")
            .create();
    }
}
```

### SenderUsernames_Delete

```java
/** Samples for SenderUsernames Delete. */
public final class SenderUsernamesDeleteSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/senderUsernames/delete.json
     */
    /**
     * Sample code: Delete SenderUsernames resource.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void deleteSenderUsernamesResource(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager
            .senderUsernames()
            .deleteWithResponse(
                "MyResourceGroup",
                "MyEmailServiceResource",
                "mydomain.com",
                "contosoNewsAlerts",
                com.azure.core.util.Context.NONE);
    }
}
```

### SenderUsernames_Get

```java
/** Samples for SenderUsernames Get. */
public final class SenderUsernamesGetSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/senderUsernames/get.json
     */
    /**
     * Sample code: Get SenderUsernames resource.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void getSenderUsernamesResource(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager
            .senderUsernames()
            .getWithResponse(
                "contosoResourceGroup",
                "contosoEmailService",
                "contoso.com",
                "contosoNewsAlerts",
                com.azure.core.util.Context.NONE);
    }
}
```

### SenderUsernames_ListByDomains

```java
/** Samples for SenderUsernames ListByDomains. */
public final class SenderUsernamesListByDomainsSamples {
    /*
     * x-ms-original-file: specification/communication/resource-manager/Microsoft.Communication/preview/2023-04-01-preview/examples/senderUsernames/listByDomain.json
     */
    /**
     * Sample code: Get SenderUsernames resource.
     *
     * @param manager Entry point to CommunicationManager.
     */
    public static void getSenderUsernamesResource(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager
            .senderUsernames()
            .listByDomains(
                "contosoResourceGroup", "contosoEmailService", "contoso.com", com.azure.core.util.Context.NONE);
    }
}
```

