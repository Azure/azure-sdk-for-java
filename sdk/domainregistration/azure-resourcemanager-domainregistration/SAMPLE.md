# Code snippets and samples


## DomainRegistrationProvider

- [ListOperations](#domainregistrationprovider_listoperations)

## Domains

- [CheckAvailability](#domains_checkavailability)
- [CreateOrUpdate](#domains_createorupdate)
- [CreateOrUpdateOwnershipIdentifier](#domains_createorupdateownershipidentifier)
- [Delete](#domains_delete)
- [DeleteOwnershipIdentifier](#domains_deleteownershipidentifier)
- [GetByResourceGroup](#domains_getbyresourcegroup)
- [GetControlCenterSsoRequest](#domains_getcontrolcenterssorequest)
- [GetOwnershipIdentifier](#domains_getownershipidentifier)
- [List](#domains_list)
- [ListByResourceGroup](#domains_listbyresourcegroup)
- [ListOwnershipIdentifiers](#domains_listownershipidentifiers)
- [ListRecommendations](#domains_listrecommendations)
- [Renew](#domains_renew)
- [TransferOut](#domains_transferout)
- [Update](#domains_update)
- [UpdateOwnershipIdentifier](#domains_updateownershipidentifier)

## TopLevelDomains

- [Get](#topleveldomains_get)
- [List](#topleveldomains_list)
- [ListAgreements](#topleveldomains_listagreements)
### DomainRegistrationProvider_ListOperations

```java
/**
 * Samples for DomainRegistrationProvider ListOperations.
 */
public final class DomainRegistrationProviderListOperationsSamples {
    /*
     * x-ms-original-file: 2024-11-01/ListOperations.json
     */
    /**
     * Sample code: List operations.
     * 
     * @param manager Entry point to DomainregistrationManager.
     */
    public static void listOperations(com.azure.resourcemanager.domainregistration.DomainregistrationManager manager) {
        manager.domainRegistrationProviders().listOperations(com.azure.core.util.Context.NONE);
    }
}
```

### Domains_CheckAvailability

```java
import com.azure.resourcemanager.domainregistration.fluent.models.NameIdentifierInner;

/**
 * Samples for Domains CheckAvailability.
 */
public final class DomainsCheckAvailabilitySamples {
    /*
     * x-ms-original-file: 2024-11-01/CheckDomainAvailability.json
     */
    /**
     * Sample code: Check domain availability.
     * 
     * @param manager Entry point to DomainregistrationManager.
     */
    public static void
        checkDomainAvailability(com.azure.resourcemanager.domainregistration.DomainregistrationManager manager) {
        manager.domains()
            .checkAvailabilityWithResponse(new NameIdentifierInner().withName("abcd.com"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Domains_CreateOrUpdate

```java
import com.azure.resourcemanager.domainregistration.models.Address;
import com.azure.resourcemanager.domainregistration.models.Contact;
import com.azure.resourcemanager.domainregistration.models.DnsType;
import com.azure.resourcemanager.domainregistration.models.DomainPurchaseConsent;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Domains CreateOrUpdate.
 */
public final class DomainsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-11-01/CreateAppServiceDomain.json
     */
    /**
     * Sample code: Create App Service Domain.
     * 
     * @param manager Entry point to DomainregistrationManager.
     */
    public static void
        createAppServiceDomain(com.azure.resourcemanager.domainregistration.DomainregistrationManager manager) {
        manager.domains()
            .define("example.com")
            .withRegion("global")
            .withExistingResourceGroup("testrg123")
            .withTags(mapOf())
            .withContactAdmin(new Contact()
                .withAddressMailing(new Address().withAddress1("3400 State St")
                    .withCity("Chicago")
                    .withCountry("United States")
                    .withPostalCode("fakeTokenPlaceholder")
                    .withState("IL"))
                .withEmail("admin@email.com")
                .withFax("1-245-534-2242")
                .withJobTitle("Admin")
                .withNameFirst("John")
                .withNameLast("Doe")
                .withNameMiddle("")
                .withOrganization("Microsoft Inc.")
                .withPhone("1-245-534-2242"))
            .withContactBilling(new Contact()
                .withAddressMailing(new Address().withAddress1("3400 State St")
                    .withCity("Chicago")
                    .withCountry("United States")
                    .withPostalCode("fakeTokenPlaceholder")
                    .withState("IL"))
                .withEmail("billing@email.com")
                .withFax("1-245-534-2242")
                .withJobTitle("Billing")
                .withNameFirst("John")
                .withNameLast("Doe")
                .withNameMiddle("")
                .withOrganization("Microsoft Inc.")
                .withPhone("1-245-534-2242"))
            .withContactRegistrant(new Contact()
                .withAddressMailing(new Address().withAddress1("3400 State St")
                    .withCity("Chicago")
                    .withCountry("United States")
                    .withPostalCode("fakeTokenPlaceholder")
                    .withState("IL"))
                .withEmail("registrant@email.com")
                .withFax("1-245-534-2242")
                .withJobTitle("Registrant")
                .withNameFirst("John")
                .withNameLast("Doe")
                .withNameMiddle("")
                .withOrganization("Microsoft Inc.")
                .withPhone("1-245-534-2242"))
            .withContactTech(new Contact()
                .withAddressMailing(new Address().withAddress1("3400 State St")
                    .withCity("Chicago")
                    .withCountry("United States")
                    .withPostalCode("fakeTokenPlaceholder")
                    .withState("IL"))
                .withEmail("tech@email.com")
                .withFax("1-245-534-2242")
                .withJobTitle("Tech")
                .withNameFirst("John")
                .withNameLast("Doe")
                .withNameMiddle("")
                .withOrganization("Microsoft Inc.")
                .withPhone("1-245-534-2242"))
            .withPrivacy(false)
            .withAutoRenew(true)
            .withConsent(new DomainPurchaseConsent().withAgreementKeys(Arrays.asList("agreementKey1"))
                .withAgreedBy("192.0.2.1")
                .withAgreedAt(OffsetDateTime.parse("2021-09-10T19:30:53Z")))
            .withDnsType(DnsType.DEFAULT_DOMAIN_REGISTRAR_DNS)
            .withAuthCode("exampleAuthCode")
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

### Domains_CreateOrUpdateOwnershipIdentifier

```java
/**
 * Samples for Domains CreateOrUpdateOwnershipIdentifier.
 */
public final class DomainsCreateOrUpdateOwnershipIdentifierSamples {
    /*
     * x-ms-original-file: 2024-11-01/CreateAppServiceDomainOwnershipIdentifier.json
     */
    /**
     * Sample code: Create App Service Domain OwnershipIdentifier.
     * 
     * @param manager Entry point to DomainregistrationManager.
     */
    public static void createAppServiceDomainOwnershipIdentifier(
        com.azure.resourcemanager.domainregistration.DomainregistrationManager manager) {
        manager.domains()
            .defineOwnershipIdentifier("SampleOwnershipId")
            .withExistingDomain("testrg123", "example.com")
            .withOwnershipId("SampleOwnershipId")
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
     * x-ms-original-file: 2024-11-01/DeleteAppServiceDomain.json
     */
    /**
     * Sample code: Delete App Service Domain.
     * 
     * @param manager Entry point to DomainregistrationManager.
     */
    public static void
        deleteAppServiceDomain(com.azure.resourcemanager.domainregistration.DomainregistrationManager manager) {
        manager.domains().deleteWithResponse("testrg123", "example.com", true, com.azure.core.util.Context.NONE);
    }
}
```

### Domains_DeleteOwnershipIdentifier

```java
/**
 * Samples for Domains DeleteOwnershipIdentifier.
 */
public final class DomainsDeleteOwnershipIdentifierSamples {
    /*
     * x-ms-original-file: 2024-11-01/DeleteAppServiceDomainOwnershipIdentifier.json
     */
    /**
     * Sample code: Delete App Service Domain Ownership Identifier.
     * 
     * @param manager Entry point to DomainregistrationManager.
     */
    public static void deleteAppServiceDomainOwnershipIdentifier(
        com.azure.resourcemanager.domainregistration.DomainregistrationManager manager) {
        manager.domains()
            .deleteOwnershipIdentifierWithResponse("testrg123", "example.com", "ownershipIdentifier",
                com.azure.core.util.Context.NONE);
    }
}
```

### Domains_GetByResourceGroup

```java
/**
 * Samples for Domains GetByResourceGroup.
 */
public final class DomainsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-11-01/GetDomain.json
     */
    /**
     * Sample code: Get Domain.
     * 
     * @param manager Entry point to DomainregistrationManager.
     */
    public static void getDomain(com.azure.resourcemanager.domainregistration.DomainregistrationManager manager) {
        manager.domains().getByResourceGroupWithResponse("testrg123", "example.com", com.azure.core.util.Context.NONE);
    }
}
```

### Domains_GetControlCenterSsoRequest

```java
/**
 * Samples for Domains GetControlCenterSsoRequest.
 */
public final class DomainsGetControlCenterSsoRequestSamples {
    /*
     * x-ms-original-file: 2024-11-01/GetDomainControlCenterSsoRequest.json
     */
    /**
     * Sample code: Get Domain Control Center Sso Request.
     * 
     * @param manager Entry point to DomainregistrationManager.
     */
    public static void getDomainControlCenterSsoRequest(
        com.azure.resourcemanager.domainregistration.DomainregistrationManager manager) {
        manager.domains().getControlCenterSsoRequestWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### Domains_GetOwnershipIdentifier

```java
/**
 * Samples for Domains GetOwnershipIdentifier.
 */
public final class DomainsGetOwnershipIdentifierSamples {
    /*
     * x-ms-original-file: 2024-11-01/GetDomainOwnershipIdentifier.json
     */
    /**
     * Sample code: Get Domain Ownership Identifier.
     * 
     * @param manager Entry point to DomainregistrationManager.
     */
    public static void
        getDomainOwnershipIdentifier(com.azure.resourcemanager.domainregistration.DomainregistrationManager manager) {
        manager.domains()
            .getOwnershipIdentifierWithResponse("testrg123", "example.com", "SampleOwnershipId",
                com.azure.core.util.Context.NONE);
    }
}
```

### Domains_List

```java
/**
 * Samples for Domains List.
 */
public final class DomainsListSamples {
    /*
     * x-ms-original-file: 2024-11-01/ListDomainsBySubscription.json
     */
    /**
     * Sample code: List domains by subscription.
     * 
     * @param manager Entry point to DomainregistrationManager.
     */
    public static void
        listDomainsBySubscription(com.azure.resourcemanager.domainregistration.DomainregistrationManager manager) {
        manager.domains().list(com.azure.core.util.Context.NONE);
    }
}
```

### Domains_ListByResourceGroup

```java
/**
 * Samples for Domains ListByResourceGroup.
 */
public final class DomainsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-11-01/ListDomainsByResourceGroup.json
     */
    /**
     * Sample code: List domains by resource group.
     * 
     * @param manager Entry point to DomainregistrationManager.
     */
    public static void
        listDomainsByResourceGroup(com.azure.resourcemanager.domainregistration.DomainregistrationManager manager) {
        manager.domains().listByResourceGroup("testrg123", com.azure.core.util.Context.NONE);
    }
}
```

### Domains_ListOwnershipIdentifiers

```java
/**
 * Samples for Domains ListOwnershipIdentifiers.
 */
public final class DomainsListOwnershipIdentifiersSamples {
    /*
     * x-ms-original-file: 2024-11-01/ListDomainOwnershipIdentifiers.json
     */
    /**
     * Sample code: List Domain Ownership Identifiers.
     * 
     * @param manager Entry point to DomainregistrationManager.
     */
    public static void
        listDomainOwnershipIdentifiers(com.azure.resourcemanager.domainregistration.DomainregistrationManager manager) {
        manager.domains().listOwnershipIdentifiers("testrg123", "example.com", com.azure.core.util.Context.NONE);
    }
}
```

### Domains_ListRecommendations

```java
import com.azure.resourcemanager.domainregistration.models.DomainRecommendationSearchParameters;

/**
 * Samples for Domains ListRecommendations.
 */
public final class DomainsListRecommendationsSamples {
    /*
     * x-ms-original-file: 2024-11-01/ListDomainRecommendations.json
     */
    /**
     * Sample code: List domain recommendations.
     * 
     * @param manager Entry point to DomainregistrationManager.
     */
    public static void
        listDomainRecommendations(com.azure.resourcemanager.domainregistration.DomainregistrationManager manager) {
        manager.domains()
            .listRecommendations(new DomainRecommendationSearchParameters().withKeywords("fakeTokenPlaceholder")
                .withMaxDomainRecommendations(10), com.azure.core.util.Context.NONE);
    }
}
```

### Domains_Renew

```java
/**
 * Samples for Domains Renew.
 */
public final class DomainsRenewSamples {
    /*
     * x-ms-original-file: 2024-11-01/RenewDomain.json
     */
    /**
     * Sample code: Renew an existing domain.
     * 
     * @param manager Entry point to DomainregistrationManager.
     */
    public static void
        renewAnExistingDomain(com.azure.resourcemanager.domainregistration.DomainregistrationManager manager) {
        manager.domains().renewWithResponse("RG", "example.com", com.azure.core.util.Context.NONE);
    }
}
```

### Domains_TransferOut

```java
/**
 * Samples for Domains TransferOut.
 */
public final class DomainsTransferOutSamples {
    /*
     * x-ms-original-file: 2024-11-01/TransferOutDomain.json
     */
    /**
     * Sample code: Transfer out domain.
     * 
     * @param manager Entry point to DomainregistrationManager.
     */
    public static void
        transferOutDomain(com.azure.resourcemanager.domainregistration.DomainregistrationManager manager) {
        manager.domains().transferOutWithResponse("testrg123", "example.com", com.azure.core.util.Context.NONE);
    }
}
```

### Domains_Update

```java
import com.azure.resourcemanager.domainregistration.models.DnsType;
import com.azure.resourcemanager.domainregistration.models.Domain;

/**
 * Samples for Domains Update.
 */
public final class DomainsUpdateSamples {
    /*
     * x-ms-original-file: 2024-11-01/UpdateAppServiceDomain.json
     */
    /**
     * Sample code: Update App Service Domain.
     * 
     * @param manager Entry point to DomainregistrationManager.
     */
    public static void
        updateAppServiceDomain(com.azure.resourcemanager.domainregistration.DomainregistrationManager manager) {
        Domain resource = manager.domains()
            .getByResourceGroupWithResponse("testrg123", "example.com", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withPrivacy(false)
            .withAutoRenew(true)
            .withDnsType(DnsType.DEFAULT_DOMAIN_REGISTRAR_DNS)
            .apply();
    }
}
```

### Domains_UpdateOwnershipIdentifier

```java
import com.azure.resourcemanager.domainregistration.models.DomainOwnershipIdentifier;

/**
 * Samples for Domains UpdateOwnershipIdentifier.
 */
public final class DomainsUpdateOwnershipIdentifierSamples {
    /*
     * x-ms-original-file: 2024-11-01/UpdateAppServiceDomainOwnershipIdentifier.json
     */
    /**
     * Sample code: Update App Service Domain OwnershipIdentifier.
     * 
     * @param manager Entry point to DomainregistrationManager.
     */
    public static void updateAppServiceDomainOwnershipIdentifier(
        com.azure.resourcemanager.domainregistration.DomainregistrationManager manager) {
        DomainOwnershipIdentifier resource = manager.domains()
            .getOwnershipIdentifierWithResponse("testrg123", "example.com", "SampleOwnershipId",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withOwnershipId("SampleOwnershipId").apply();
    }
}
```

### TopLevelDomains_Get

```java
/**
 * Samples for TopLevelDomains Get.
 */
public final class TopLevelDomainsGetSamples {
    /*
     * x-ms-original-file: 2024-11-01/GetTopLevelDomain.json
     */
    /**
     * Sample code: Get Top Level Domain.
     * 
     * @param manager Entry point to DomainregistrationManager.
     */
    public static void
        getTopLevelDomain(com.azure.resourcemanager.domainregistration.DomainregistrationManager manager) {
        manager.topLevelDomains().getWithResponse("com", com.azure.core.util.Context.NONE);
    }
}
```

### TopLevelDomains_List

```java
/**
 * Samples for TopLevelDomains List.
 */
public final class TopLevelDomainsListSamples {
    /*
     * x-ms-original-file: 2024-11-01/ListTopLevelDomains.json
     */
    /**
     * Sample code: List Top Level Domains.
     * 
     * @param manager Entry point to DomainregistrationManager.
     */
    public static void
        listTopLevelDomains(com.azure.resourcemanager.domainregistration.DomainregistrationManager manager) {
        manager.topLevelDomains().list(com.azure.core.util.Context.NONE);
    }
}
```

### TopLevelDomains_ListAgreements

```java
import com.azure.resourcemanager.domainregistration.models.TopLevelDomainAgreementOption;

/**
 * Samples for TopLevelDomains ListAgreements.
 */
public final class TopLevelDomainsListAgreementsSamples {
    /*
     * x-ms-original-file: 2024-11-01/ListTopLevelDomainAgreements.json
     */
    /**
     * Sample code: List Top Level Domain Agreements.
     * 
     * @param manager Entry point to DomainregistrationManager.
     */
    public static void
        listTopLevelDomainAgreements(com.azure.resourcemanager.domainregistration.DomainregistrationManager manager) {
        manager.topLevelDomains()
            .listAgreements("in", new TopLevelDomainAgreementOption().withIncludePrivacy(true).withForTransfer(false),
                com.azure.core.util.Context.NONE);
    }
}
```

