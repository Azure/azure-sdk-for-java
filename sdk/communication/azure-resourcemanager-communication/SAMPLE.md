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

## SmtpUsernames

- [CreateOrUpdate](#smtpusernames_createorupdate)
- [Delete](#smtpusernames_delete)
- [Get](#smtpusernames_get)
- [List](#smtpusernames_list)

## SuppressionListAddresses

- [CreateOrUpdate](#suppressionlistaddresses_createorupdate)
- [Delete](#suppressionlistaddresses_delete)
- [Get](#suppressionlistaddresses_get)
- [List](#suppressionlistaddresses_list)

## SuppressionLists

- [CreateOrUpdate](#suppressionlists_createorupdate)
- [Delete](#suppressionlists_delete)
- [Get](#suppressionlists_get)
- [ListByDomain](#suppressionlists_listbydomain)
### CommunicationServices_CheckNameAvailability

```java
import com.azure.resourcemanager.communication.models.NameAvailabilityParameters;

/**
 * Samples for CommunicationServices CheckNameAvailability.
 */
public final class CommunicationServicesCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/
     * communicationServices/checkNameAvailabilityAvailable.json
     */
    /**
     * Sample code: Check name availability available.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        checkNameAvailabilityAvailable(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.communicationServices()
            .checkNameAvailabilityWithResponse(new NameAvailabilityParameters().withName("MyCommunicationService")
                .withType("Microsoft.Communication/CommunicationServices"), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/
     * communicationServices/checkNameAvailabilityUnavailable.json
     */
    /**
     * Sample code: Check name availability unavailable.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        checkNameAvailabilityUnavailable(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.communicationServices()
            .checkNameAvailabilityWithResponse(new NameAvailabilityParameters().withName("MyCommunicationService")
                .withType("Microsoft.Communication/CommunicationServices"), com.azure.core.util.Context.NONE);
    }
}
```

### CommunicationServices_CreateOrUpdate

```java
import com.azure.resourcemanager.communication.models.ManagedServiceIdentity;
import com.azure.resourcemanager.communication.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.communication.models.PublicNetworkAccess;

/**
 * Samples for CommunicationServices CreateOrUpdate.
 */
public final class CommunicationServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/
     * communicationServices/createOrUpdate.json
     */
    /**
     * Sample code: Create or update resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void createOrUpdateResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.communicationServices()
            .define("MyCommunicationResource")
            .withRegion("Global")
            .withExistingResourceGroup("MyResourceGroup")
            .withDataLocation("United States")
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/
     * communicationServices/createOrUpdateWithPublicNetworkAccess.json
     */
    /**
     * Sample code: Create or update resource with PublicNetworkAccess.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void createOrUpdateResourceWithPublicNetworkAccess(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.communicationServices()
            .define("MyCommunicationResource")
            .withRegion("Global")
            .withExistingResourceGroup("MyResourceGroup")
            .withDataLocation("United States")
            .withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/
     * communicationServices/createOrUpdateWithSystemAssignedIdentity.json
     */
    /**
     * Sample code: Create or update resource with managed identity.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void createOrUpdateResourceWithManagedIdentity(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.communicationServices()
            .define("MyCommunicationResource")
            .withRegion("Global")
            .withExistingResourceGroup("MyResourceGroup")
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
            .withDataLocation("United States")
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/
     * communicationServices/createOrUpdateWithDisableLocalAuth.json
     */
    /**
     * Sample code: Create or update resource with DisableLocalAuth.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void createOrUpdateResourceWithDisableLocalAuth(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.communicationServices()
            .define("MyCommunicationResource")
            .withRegion("Global")
            .withExistingResourceGroup("MyResourceGroup")
            .withDataLocation("United States")
            .withDisableLocalAuth(true)
            .create();
    }
}
```

### CommunicationServices_Delete

```java
/**
 * Samples for CommunicationServices Delete.
 */
public final class CommunicationServicesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/
     * communicationServices/delete.json
     */
    /**
     * Sample code: Delete resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void deleteResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.communicationServices()
            .delete("MyResourceGroup", "MyCommunicationResource", com.azure.core.util.Context.NONE);
    }
}
```

### CommunicationServices_GetByResourceGroup

```java
/**
 * Samples for CommunicationServices GetByResourceGroup.
 */
public final class CommunicationServicesGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/
     * communicationServices/get.json
     */
    /**
     * Sample code: Get resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void getResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.communicationServices()
            .getByResourceGroupWithResponse("MyResourceGroup", "MyCommunicationResource",
                com.azure.core.util.Context.NONE);
    }
}
```

### CommunicationServices_LinkNotificationHub

```java
import com.azure.resourcemanager.communication.models.LinkNotificationHubParameters;

/**
 * Samples for CommunicationServices LinkNotificationHub.
 */
public final class CommunicationServicesLinkNotificationHubSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/
     * communicationServices/linkNotificationHub.json
     */
    /**
     * Sample code: Link notification hub.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void linkNotificationHub(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.communicationServices()
            .linkNotificationHubWithResponse("MyResourceGroup", "MyCommunicationResource",
                new LinkNotificationHubParameters().withResourceId(
                    "/subscriptions/11112222-3333-4444-5555-666677778888/resourceGroups/MyOtherResourceGroup/providers/Microsoft.NotificationHubs/namespaces/MyNamespace/notificationHubs/MyHub")
                    .withConnectionString("Endpoint=sb://MyNamespace.servicebus.windows.net/;SharedAccessKey=abcd1234"),
                com.azure.core.util.Context.NONE);
    }
}
```

### CommunicationServices_List

```java
/**
 * Samples for CommunicationServices List.
 */
public final class CommunicationServicesListSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/
     * communicationServices/listBySubscription.json
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
/**
 * Samples for CommunicationServices ListByResourceGroup.
 */
public final class CommunicationServicesListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/
     * communicationServices/listByResourceGroup.json
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
/**
 * Samples for CommunicationServices ListKeys.
 */
public final class CommunicationServicesListKeysSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/
     * communicationServices/listKeys.json
     */
    /**
     * Sample code: List keys.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void listKeys(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.communicationServices()
            .listKeysWithResponse("MyResourceGroup", "MyCommunicationResource", com.azure.core.util.Context.NONE);
    }
}
```

### CommunicationServices_RegenerateKey

```java
import com.azure.resourcemanager.communication.models.KeyType;
import com.azure.resourcemanager.communication.models.RegenerateKeyParameters;

/**
 * Samples for CommunicationServices RegenerateKey.
 */
public final class CommunicationServicesRegenerateKeySamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/
     * communicationServices/regenerateKey.json
     */
    /**
     * Sample code: Regenerate key.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void regenerateKey(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.communicationServices()
            .regenerateKeyWithResponse("MyResourceGroup", "MyCommunicationResource",
                new RegenerateKeyParameters().withKeyType(KeyType.PRIMARY), com.azure.core.util.Context.NONE);
    }
}
```

### CommunicationServices_Update

```java
import com.azure.resourcemanager.communication.models.CommunicationServiceResource;
import com.azure.resourcemanager.communication.models.ManagedServiceIdentity;
import com.azure.resourcemanager.communication.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.communication.models.PublicNetworkAccess;
import com.azure.resourcemanager.communication.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for CommunicationServices Update.
 */
public final class CommunicationServicesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/
     * communicationServices/update.json
     */
    /**
     * Sample code: Update resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void updateResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        CommunicationServiceResource resource = manager.communicationServices()
            .getByResourceGroupWithResponse("MyResourceGroup", "MyCommunicationResource",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("newTag", "newVal")).apply();
    }

    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/
     * communicationServices/updateWithUserAssignedIdentity.json
     */
    /**
     * Sample code: Update resource to add a User Assigned managed identity.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void updateResourceToAddAUserAssignedManagedIdentity(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        CommunicationServiceResource resource = manager.communicationServices()
            .getByResourceGroupWithResponse("MyResourceGroup", "MyCommunicationResource",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("newTag", "newVal"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf("/user/assigned/resource/id", new UserAssignedIdentity())))
            .apply();
    }

    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/
     * communicationServices/updateWithPublicNetworkAccess.json
     */
    /**
     * Sample code: Update resource to add PublicNetworkAccess.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        updateResourceToAddPublicNetworkAccess(com.azure.resourcemanager.communication.CommunicationManager manager) {
        CommunicationServiceResource resource = manager.communicationServices()
            .getByResourceGroupWithResponse("MyResourceGroup", "MyCommunicationResource",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("newTag", "newVal"))
            .withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
            .apply();
    }

    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/
     * communicationServices/updateWithDisableLocalAuth.json
     */
    /**
     * Sample code: Update resource to add DisableLocalAuth.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        updateResourceToAddDisableLocalAuth(com.azure.resourcemanager.communication.CommunicationManager manager) {
        CommunicationServiceResource resource = manager.communicationServices()
            .getByResourceGroupWithResponse("MyResourceGroup", "MyCommunicationResource",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("newTag", "newVal")).withDisableLocalAuth(true).apply();
    }

    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/
     * communicationServices/updateWithSystemAssignedIdentity.json
     */
    /**
     * Sample code: Update resource to add a System Assigned managed identity.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void updateResourceToAddASystemAssignedManagedIdentity(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        CommunicationServiceResource resource = manager.communicationServices()
            .getByResourceGroupWithResponse("MyResourceGroup", "MyCommunicationResource",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("newTag", "newVal"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
            .apply();
    }

    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/
     * communicationServices/updateRemoveSystemIdentity.json
     */
    /**
     * Sample code: Update resource to remove identity.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        updateResourceToRemoveIdentity(com.azure.resourcemanager.communication.CommunicationManager manager) {
        CommunicationServiceResource resource = manager.communicationServices()
            .getByResourceGroupWithResponse("MyResourceGroup", "MyCommunicationResource",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("newTag", "newVal"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE))
            .apply();
    }

    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/
     * communicationServices/updateWithSystemAndUserIdentity.json
     */
    /**
     * Sample code: Update resource to add System and User managed identities.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void updateResourceToAddSystemAndUserManagedIdentities(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        CommunicationServiceResource resource = manager.communicationServices()
            .getByResourceGroupWithResponse("MyResourceGroup", "MyCommunicationResource",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("newTag", "newVal"))
            .withIdentity(
                new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
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

/**
 * Samples for Domains CancelVerification.
 */
public final class DomainsCancelVerificationSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/domains/
     * cancelVerification.json
     */
    /**
     * Sample code: Cancel verification.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void cancelVerification(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.domains()
            .cancelVerification("MyResourceGroup", "MyEmailServiceResource", "mydomain.com",
                new VerificationParameter().withVerificationType(VerificationType.SPF),
                com.azure.core.util.Context.NONE);
    }
}
```

### Domains_CreateOrUpdate

```java
import com.azure.resourcemanager.communication.models.DomainManagement;

/**
 * Samples for Domains CreateOrUpdate.
 */
public final class DomainsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/domains/
     * createOrUpdate.json
     */
    /**
     * Sample code: Create or update Domains resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        createOrUpdateDomainsResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.domains()
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
/**
 * Samples for Domains Delete.
 */
public final class DomainsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/domains/delete.
     * json
     */
    /**
     * Sample code: Delete Domains resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void deleteDomainsResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.domains()
            .delete("MyResourceGroup", "MyEmailServiceResource", "mydomain.com", com.azure.core.util.Context.NONE);
    }
}
```

### Domains_Get

```java
/**
 * Samples for Domains Get.
 */
public final class DomainsGetSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/domains/get.json
     */
    /**
     * Sample code: Get Domains resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void getDomainsResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.domains()
            .getWithResponse("MyResourceGroup", "MyEmailServiceResource", "mydomain.com",
                com.azure.core.util.Context.NONE);
    }
}
```

### Domains_InitiateVerification

```java
import com.azure.resourcemanager.communication.models.VerificationParameter;
import com.azure.resourcemanager.communication.models.VerificationType;

/**
 * Samples for Domains InitiateVerification.
 */
public final class DomainsInitiateVerificationSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/domains/
     * initiateVerification.json
     */
    /**
     * Sample code: Initiate verification.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void initiateVerification(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.domains()
            .initiateVerification("MyResourceGroup", "MyEmailServiceResource", "mydomain.com",
                new VerificationParameter().withVerificationType(VerificationType.SPF),
                com.azure.core.util.Context.NONE);
    }
}
```

### Domains_ListByEmailServiceResource

```java
/**
 * Samples for Domains ListByEmailServiceResource.
 */
public final class DomainsListByEmailServiceResourceSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/domains/
     * listByEmailService.json
     */
    /**
     * Sample code: List Domains resources by EmailServiceName.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        listDomainsResourcesByEmailServiceName(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.domains()
            .listByEmailServiceResource("MyResourceGroup", "MyEmailServiceResource", com.azure.core.util.Context.NONE);
    }
}
```

### Domains_Update

```java
import com.azure.resourcemanager.communication.models.DomainResource;
import com.azure.resourcemanager.communication.models.UserEngagementTracking;

/**
 * Samples for Domains Update.
 */
public final class DomainsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/domains/update.
     * json
     */
    /**
     * Sample code: Update Domains resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void updateDomainsResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        DomainResource resource = manager.domains()
            .getWithResponse("MyResourceGroup", "MyEmailServiceResource", "mydomain.com",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withUserEngagementTracking(UserEngagementTracking.ENABLED).apply();
    }
}
```

### EmailServices_CreateOrUpdate

```java
/**
 * Samples for EmailServices CreateOrUpdate.
 */
public final class EmailServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/emailServices/
     * createOrUpdate.json
     */
    /**
     * Sample code: Create or update EmailService resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        createOrUpdateEmailServiceResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.emailServices()
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
/**
 * Samples for EmailServices Delete.
 */
public final class EmailServicesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/emailServices/
     * delete.json
     */
    /**
     * Sample code: Delete EmailService resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        deleteEmailServiceResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.emailServices().delete("MyResourceGroup", "MyEmailServiceResource", com.azure.core.util.Context.NONE);
    }
}
```

### EmailServices_GetByResourceGroup

```java
/**
 * Samples for EmailServices GetByResourceGroup.
 */
public final class EmailServicesGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/emailServices/get
     * .json
     */
    /**
     * Sample code: Get EmailService resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void getEmailServiceResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.emailServices()
            .getByResourceGroupWithResponse("MyResourceGroup", "MyEmailServiceResource",
                com.azure.core.util.Context.NONE);
    }
}
```

### EmailServices_List

```java
/**
 * Samples for EmailServices List.
 */
public final class EmailServicesListSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/emailServices/
     * listBySubscription.json
     */
    /**
     * Sample code: List EmailService resources by subscription.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        listEmailServiceResourcesBySubscription(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.emailServices().list(com.azure.core.util.Context.NONE);
    }
}
```

### EmailServices_ListByResourceGroup

```java
/**
 * Samples for EmailServices ListByResourceGroup.
 */
public final class EmailServicesListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/emailServices/
     * listByResourceGroup.json
     */
    /**
     * Sample code: List EmailService resources by resource group.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        listEmailServiceResourcesByResourceGroup(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.emailServices().listByResourceGroup("MyResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### EmailServices_ListVerifiedExchangeOnlineDomains

```java
/**
 * Samples for EmailServices ListVerifiedExchangeOnlineDomains.
 */
public final class EmailServicesListVerifiedExchangeOnlineDomainsSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/emailServices/
     * getVerifiedExchangeOnlineDomains.json
     */
    /**
     * Sample code: Get verified Exchange Online domains.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        getVerifiedExchangeOnlineDomains(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.emailServices().listVerifiedExchangeOnlineDomainsWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### EmailServices_Update

```java
import com.azure.resourcemanager.communication.models.EmailServiceResource;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for EmailServices Update.
 */
public final class EmailServicesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/emailServices/
     * update.json
     */
    /**
     * Sample code: Update EmailService resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        updateEmailServiceResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        EmailServiceResource resource = manager.emailServices()
            .getByResourceGroupWithResponse("MyResourceGroup", "MyEmailServiceResource",
                com.azure.core.util.Context.NONE)
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
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/
     * communicationServices/operationsList.json
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
/**
 * Samples for SenderUsernames CreateOrUpdate.
 */
public final class SenderUsernamesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/senderUsernames/
     * createOrUpdate.json
     */
    /**
     * Sample code: Create or update SenderUsernames resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        createOrUpdateSenderUsernamesResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.senderUsernames()
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
/**
 * Samples for SenderUsernames Delete.
 */
public final class SenderUsernamesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/senderUsernames/
     * delete.json
     */
    /**
     * Sample code: Delete SenderUsernames resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        deleteSenderUsernamesResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.senderUsernames()
            .deleteWithResponse("MyResourceGroup", "MyEmailServiceResource", "mydomain.com", "contosoNewsAlerts",
                com.azure.core.util.Context.NONE);
    }
}
```

### SenderUsernames_Get

```java
/**
 * Samples for SenderUsernames Get.
 */
public final class SenderUsernamesGetSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/senderUsernames/
     * get.json
     */
    /**
     * Sample code: Get SenderUsernames resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        getSenderUsernamesResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.senderUsernames()
            .getWithResponse("contosoResourceGroup", "contosoEmailService", "contoso.com", "contosoNewsAlerts",
                com.azure.core.util.Context.NONE);
    }
}
```

### SenderUsernames_ListByDomains

```java
/**
 * Samples for SenderUsernames ListByDomains.
 */
public final class SenderUsernamesListByDomainsSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/senderUsernames/
     * listByDomain.json
     */
    /**
     * Sample code: Get SenderUsernames resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        getSenderUsernamesResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.senderUsernames()
            .listByDomains("contosoResourceGroup", "contosoEmailService", "contoso.com",
                com.azure.core.util.Context.NONE);
    }
}
```

### SmtpUsernames_CreateOrUpdate

```java
/**
 * Samples for SmtpUsernames CreateOrUpdate.
 */
public final class SmtpUsernamesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/smtpUsername/
     * createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate SmtpUsername resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        createOrUpdateSmtpUsernameResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.smtpUsernames()
            .define("smtpusername1")
            .withExistingCommunicationService("contosoResourceGroup", "contosoACSService")
            .withUsername("newuser1@contoso.com")
            .withEntraApplicationId("aaaa1111-bbbb-2222-3333-aaaa111122bb")
            .withTenantId("aaaa1111-bbbb-2222-3333-aaaa11112222")
            .create();
    }
}
```

### SmtpUsernames_Delete

```java
/**
 * Samples for SmtpUsernames Delete.
 */
public final class SmtpUsernamesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/smtpUsername/
     * delete.json
     */
    /**
     * Sample code: Delete a SmtpUsername resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        deleteASmtpUsernameResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.smtpUsernames()
            .deleteWithResponse("MyResourceGroup", "contosoACSService", "smtpusername1",
                com.azure.core.util.Context.NONE);
    }
}
```

### SmtpUsernames_Get

```java
/**
 * Samples for SmtpUsernames Get.
 */
public final class SmtpUsernamesGetSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/smtpUsername/get.
     * json
     */
    /**
     * Sample code: Get a SmtpUsername resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void getASmtpUsernameResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.smtpUsernames()
            .getWithResponse("contosoResourceGroup", "contosoACSService", "smtpusername1",
                com.azure.core.util.Context.NONE);
    }
}
```

### SmtpUsernames_List

```java
/**
 * Samples for SmtpUsernames List.
 */
public final class SmtpUsernamesListSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/smtpUsername/
     * getAll.json
     */
    /**
     * Sample code: Get all SmtpUsername resources for a CommunicationService resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void getAllSmtpUsernameResourcesForACommunicationServiceResource(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.smtpUsernames().list("contosoResourceGroup", "contosoACSService", com.azure.core.util.Context.NONE);
    }
}
```

### SuppressionListAddresses_CreateOrUpdate

```java
/**
 * Samples for SuppressionListAddresses CreateOrUpdate.
 */
public final class SuppressionListAddressesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/suppressionLists/
     * createOrUpdateAddress.json
     */
    /**
     * Sample code: CreateOrUpdate SuppressionListAddress resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void createOrUpdateSuppressionListAddressResource(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.suppressionListAddresses()
            .define("11112222-3333-4444-5555-aaaabbbbcccc")
            .withExistingSuppressionList("contosoResourceGroup", "contosoEmailService", "contoso.com",
                "aaaa1111-bbbb-2222-3333-aaaa11112222")
            .withEmail("newuser1@fabrikam.com")
            .withFirstName("updatedFirstName")
            .create();
    }
}
```

### SuppressionListAddresses_Delete

```java
/**
 * Samples for SuppressionListAddresses Delete.
 */
public final class SuppressionListAddressesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/suppressionLists/
     * deleteAddress.json
     */
    /**
     * Sample code: Delete a SuppressionListAddress resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        deleteASuppressionListAddressResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.suppressionListAddresses()
            .deleteWithResponse("MyResourceGroup", "MyEmailServiceResource", "mydomain.com",
                "aaaa1111-bbbb-2222-3333-aaaa11112222", "11112222-3333-4444-5555-999999999999",
                com.azure.core.util.Context.NONE);
    }
}
```

### SuppressionListAddresses_Get

```java
/**
 * Samples for SuppressionListAddresses Get.
 */
public final class SuppressionListAddressesGetSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/suppressionLists/
     * getAddress.json
     */
    /**
     * Sample code: Get a SuppressionListAddress resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        getASuppressionListAddressResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.suppressionListAddresses()
            .getWithResponse("contosoResourceGroup", "contosoEmailService", "contoso.com",
                "aaaa1111-bbbb-2222-3333-aaaa11112222", "11112222-3333-4444-5555-aaaabbbbcccc",
                com.azure.core.util.Context.NONE);
    }
}
```

### SuppressionListAddresses_List

```java
/**
 * Samples for SuppressionListAddresses List.
 */
public final class SuppressionListAddressesListSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/suppressionLists/
     * getAddresses.json
     */
    /**
     * Sample code: Get all SuppressionListAddresses resources for a SuppressionList resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void getAllSuppressionListAddressesResourcesForASuppressionListResource(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.suppressionListAddresses()
            .list("contosoResourceGroup", "contosoEmailService", "contoso.com", "aaaa1111-bbbb-2222-3333-aaaa11112222",
                com.azure.core.util.Context.NONE);
    }
}
```

### SuppressionLists_CreateOrUpdate

```java
/**
 * Samples for SuppressionLists CreateOrUpdate.
 */
public final class SuppressionListsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/suppressionLists/
     * createOrUpdateSuppressionList.json
     */
    /**
     * Sample code: CreateOrUpdate SuppressionLists resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        createOrUpdateSuppressionListsResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.suppressionLists()
            .define("aaaa1111-bbbb-2222-3333-aaaa11112222")
            .withExistingDomain("contosoResourceGroup", "contosoEmailService", "contoso.com")
            .withListName("contosoNewsAlerts")
            .create();
    }
}
```

### SuppressionLists_Delete

```java
/**
 * Samples for SuppressionLists Delete.
 */
public final class SuppressionListsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/suppressionLists/
     * deleteSuppressionList.json
     */
    /**
     * Sample code: Delete a SuppressionLists resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        deleteASuppressionListsResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.suppressionLists()
            .deleteWithResponse("MyResourceGroup", "MyEmailServiceResource", "mydomain.com",
                "aaaa1111-bbbb-2222-3333-aaaa11112222", com.azure.core.util.Context.NONE);
    }
}
```

### SuppressionLists_Get

```java
/**
 * Samples for SuppressionLists Get.
 */
public final class SuppressionListsGetSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/suppressionLists/
     * getSuppressionList.json
     */
    /**
     * Sample code: Get a SuppressionList resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        getASuppressionListResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.suppressionLists()
            .getWithResponse("contosoResourceGroup", "contosoEmailService", "contoso.com",
                "aaaa1111-bbbb-2222-3333-aaaa11112222", com.azure.core.util.Context.NONE);
    }
}
```

### SuppressionLists_ListByDomain

```java
/**
 * Samples for SuppressionLists ListByDomain.
 */
public final class SuppressionListsListByDomainSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2025-09-01/examples/suppressionLists/
     * getSuppressionLists.json
     */
    /**
     * Sample code: Get all SuppressionLists resources.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void
        getAllSuppressionListsResources(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.suppressionLists()
            .listByDomain("contosoResourceGroup", "contosoEmailService", "contoso.com",
                com.azure.core.util.Context.NONE);
    }
}
```

