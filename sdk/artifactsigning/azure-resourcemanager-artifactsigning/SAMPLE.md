# Code snippets and samples


## CertificateProfiles

- [Create](#certificateprofiles_create)
- [Delete](#certificateprofiles_delete)
- [Get](#certificateprofiles_get)
- [ListByCodeSigningAccount](#certificateprofiles_listbycodesigningaccount)
- [RevokeCertificate](#certificateprofiles_revokecertificate)

## CodeSigningAccounts

- [CheckNameAvailability](#codesigningaccounts_checknameavailability)
- [Create](#codesigningaccounts_create)
- [Delete](#codesigningaccounts_delete)
- [GetByResourceGroup](#codesigningaccounts_getbyresourcegroup)
- [List](#codesigningaccounts_list)
- [ListByResourceGroup](#codesigningaccounts_listbyresourcegroup)
- [Update](#codesigningaccounts_update)

## Operations

- [List](#operations_list)
### CertificateProfiles_Create

```java
import com.azure.resourcemanager.artifactsigning.models.ProfileType;

/**
 * Samples for CertificateProfiles Create.
 */
public final class CertificateProfilesCreateSamples {
    /*
     * x-ms-original-file: 2025-10-13/CertificateProfiles_Create.json
     */
    /**
     * Sample code: Create a certificate profile.
     * 
     * @param manager Entry point to ArtifactSigningManager.
     */
    public static void
        createACertificateProfile(com.azure.resourcemanager.artifactsigning.ArtifactSigningManager manager) {
        manager.certificateProfiles()
            .define("profileA")
            .withExistingCodeSigningAccount("MyResourceGroup", "MyAccount")
            .withProfileType(ProfileType.PUBLIC_TRUST)
            .withIncludeStreetAddress(false)
            .withIncludePostalCode(true)
            .withIdentityValidationId("00000000-1234-5678-3333-444444444444")
            .create();
    }
}
```

### CertificateProfiles_Delete

```java
/**
 * Samples for CertificateProfiles Delete.
 */
public final class CertificateProfilesDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-13/CertificateProfiles_Delete.json
     */
    /**
     * Sample code: Delete a certificate profile.
     * 
     * @param manager Entry point to ArtifactSigningManager.
     */
    public static void
        deleteACertificateProfile(com.azure.resourcemanager.artifactsigning.ArtifactSigningManager manager) {
        manager.certificateProfiles()
            .delete("MyResourceGroup", "MyAccount", "profileA", com.azure.core.util.Context.NONE);
    }
}
```

### CertificateProfiles_Get

```java
/**
 * Samples for CertificateProfiles Get.
 */
public final class CertificateProfilesGetSamples {
    /*
     * x-ms-original-file: 2025-10-13/CertificateProfiles_Get.json
     */
    /**
     * Sample code: Get details of a certificate profile.
     * 
     * @param manager Entry point to ArtifactSigningManager.
     */
    public static void
        getDetailsOfACertificateProfile(com.azure.resourcemanager.artifactsigning.ArtifactSigningManager manager) {
        manager.certificateProfiles()
            .getWithResponse("MyResourceGroup", "MyAccount", "profileA", com.azure.core.util.Context.NONE);
    }
}
```

### CertificateProfiles_ListByCodeSigningAccount

```java
/**
 * Samples for CertificateProfiles ListByCodeSigningAccount.
 */
public final class CertificateProfilesListByCodeSigningAccountSamples {
    /*
     * x-ms-original-file: 2025-10-13/CertificateProfiles_ListByCodeSigningAccount.json
     */
    /**
     * Sample code: List certificate profiles under an artifact signing account.
     * 
     * @param manager Entry point to ArtifactSigningManager.
     */
    public static void listCertificateProfilesUnderAnArtifactSigningAccount(
        com.azure.resourcemanager.artifactsigning.ArtifactSigningManager manager) {
        manager.certificateProfiles()
            .listByCodeSigningAccount("MyResourceGroup", "MyAccount", com.azure.core.util.Context.NONE);
    }
}
```

### CertificateProfiles_RevokeCertificate

```java
import com.azure.resourcemanager.artifactsigning.models.RevokeCertificate;
import java.time.OffsetDateTime;

/**
 * Samples for CertificateProfiles RevokeCertificate.
 */
public final class CertificateProfilesRevokeCertificateSamples {
    /*
     * x-ms-original-file: 2025-10-13/CertificateProfiles_RevokeCertificate.json
     */
    /**
     * Sample code: Revoke a certificate under a certificate profile.
     * 
     * @param manager Entry point to ArtifactSigningManager.
     */
    public static void revokeACertificateUnderACertificateProfile(
        com.azure.resourcemanager.artifactsigning.ArtifactSigningManager manager) {
        manager.certificateProfiles()
            .revokeCertificateWithResponse("MyResourceGroup", "MyAccount", "profileA",
                new RevokeCertificate().withSerialNumber("xxxxxxxxxxxxxxxxxx")
                    .withThumbprint("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
                    .withEffectiveAt(OffsetDateTime.parse("2023-11-12T23:40:25+00:00"))
                    .withReason("KeyCompromised")
                    .withRemarks("test"),
                com.azure.core.util.Context.NONE);
    }
}
```

### CodeSigningAccounts_CheckNameAvailability

```java
import com.azure.resourcemanager.artifactsigning.models.CheckNameAvailability;

/**
 * Samples for CodeSigningAccounts CheckNameAvailability.
 */
public final class CodeSigningAccountsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: 2025-10-13/CodeSigningAccounts_CheckNameAvailability.json
     */
    /**
     * Sample code: Checks if the artifact signing account name is available.
     * 
     * @param manager Entry point to ArtifactSigningManager.
     */
    public static void checksIfTheArtifactSigningAccountNameIsAvailable(
        com.azure.resourcemanager.artifactsigning.ArtifactSigningManager manager) {
        manager.codeSigningAccounts()
            .checkNameAvailabilityWithResponse(
                new CheckNameAvailability().withType("Microsoft.CodeSigning/codeSigningAccounts")
                    .withName("sample-account"),
                com.azure.core.util.Context.NONE);
    }
}
```

### CodeSigningAccounts_Create

```java
import com.azure.resourcemanager.artifactsigning.models.AccountSku;
import com.azure.resourcemanager.artifactsigning.models.SkuName;

/**
 * Samples for CodeSigningAccounts Create.
 */
public final class CodeSigningAccountsCreateSamples {
    /*
     * x-ms-original-file: 2025-10-13/CodeSigningAccounts_Create.json
     */
    /**
     * Sample code: Create an artifact signing account.
     * 
     * @param manager Entry point to ArtifactSigningManager.
     */
    public static void
        createAnArtifactSigningAccount(com.azure.resourcemanager.artifactsigning.ArtifactSigningManager manager) {
        manager.codeSigningAccounts()
            .define("MyAccount")
            .withRegion("westus")
            .withExistingResourceGroup("MyResourceGroup")
            .withSku(new AccountSku().withName(SkuName.BASIC))
            .create();
    }
}
```

### CodeSigningAccounts_Delete

```java
/**
 * Samples for CodeSigningAccounts Delete.
 */
public final class CodeSigningAccountsDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-13/CodeSigningAccounts_Delete.json
     */
    /**
     * Sample code: Delete an artifact signing account.
     * 
     * @param manager Entry point to ArtifactSigningManager.
     */
    public static void
        deleteAnArtifactSigningAccount(com.azure.resourcemanager.artifactsigning.ArtifactSigningManager manager) {
        manager.codeSigningAccounts().delete("MyResourceGroup", "MyAccount", com.azure.core.util.Context.NONE);
    }
}
```

### CodeSigningAccounts_GetByResourceGroup

```java
/**
 * Samples for CodeSigningAccounts GetByResourceGroup.
 */
public final class CodeSigningAccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-10-13/CodeSigningAccounts_Get.json
     */
    /**
     * Sample code: Get an artifact signing account.
     * 
     * @param manager Entry point to ArtifactSigningManager.
     */
    public static void
        getAnArtifactSigningAccount(com.azure.resourcemanager.artifactsigning.ArtifactSigningManager manager) {
        manager.codeSigningAccounts()
            .getByResourceGroupWithResponse("MyResourceGroup", "MyAccount", com.azure.core.util.Context.NONE);
    }
}
```

### CodeSigningAccounts_List

```java
/**
 * Samples for CodeSigningAccounts List.
 */
public final class CodeSigningAccountsListSamples {
    /*
     * x-ms-original-file: 2025-10-13/CodeSigningAccounts_ListBySubscription.json
     */
    /**
     * Sample code: Lists artifact signing accounts within a subscription.
     * 
     * @param manager Entry point to ArtifactSigningManager.
     */
    public static void listsArtifactSigningAccountsWithinASubscription(
        com.azure.resourcemanager.artifactsigning.ArtifactSigningManager manager) {
        manager.codeSigningAccounts().list(com.azure.core.util.Context.NONE);
    }
}
```

### CodeSigningAccounts_ListByResourceGroup

```java
/**
 * Samples for CodeSigningAccounts ListByResourceGroup.
 */
public final class CodeSigningAccountsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-10-13/CodeSigningAccounts_ListByResourceGroup.json
     */
    /**
     * Sample code: Lists artifact signing accounts within a resource group.
     * 
     * @param manager Entry point to ArtifactSigningManager.
     */
    public static void listsArtifactSigningAccountsWithinAResourceGroup(
        com.azure.resourcemanager.artifactsigning.ArtifactSigningManager manager) {
        manager.codeSigningAccounts().listByResourceGroup("MyResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### CodeSigningAccounts_Update

```java
import com.azure.resourcemanager.artifactsigning.models.CodeSigningAccount;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for CodeSigningAccounts Update.
 */
public final class CodeSigningAccountsUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-13/CodeSigningAccounts_Update.json
     */
    /**
     * Sample code: Update an artifact signing account.
     * 
     * @param manager Entry point to ArtifactSigningManager.
     */
    public static void
        updateAnArtifactSigningAccount(com.azure.resourcemanager.artifactsigning.ArtifactSigningManager manager) {
        CodeSigningAccount resource = manager.codeSigningAccounts()
            .getByResourceGroupWithResponse("MyResourceGroup", "MyAccount", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key1", "fakeTokenPlaceholder")).apply();
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
     * x-ms-original-file: 2025-10-13/Operations_List.json
     */
    /**
     * Sample code: List artifact signing account operations.
     * 
     * @param manager Entry point to ArtifactSigningManager.
     */
    public static void
        listArtifactSigningAccountOperations(com.azure.resourcemanager.artifactsigning.ArtifactSigningManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

