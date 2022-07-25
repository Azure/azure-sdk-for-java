# Code snippets and samples


## Operations

- [List](#operations_list)

## PrivateLinkResourcesOperation

- [Get](#privatelinkresourcesoperation_get)
- [List](#privatelinkresourcesoperation_list)

## RecoveryServices

- [CheckNameAvailability](#recoveryservices_checknameavailability)

## RegisteredIdentities

- [Delete](#registeredidentities_delete)

## ReplicationUsages

- [List](#replicationusages_list)

## ResourceProvider

- [GetOperationResult](#resourceprovider_getoperationresult)
- [GetOperationStatus](#resourceprovider_getoperationstatus)

## Usages

- [ListByVaults](#usages_listbyvaults)

## VaultCertificates

- [Create](#vaultcertificates_create)

## VaultExtendedInfo

- [CreateOrUpdate](#vaultextendedinfo_createorupdate)
- [Get](#vaultextendedinfo_get)
- [Update](#vaultextendedinfo_update)

## Vaults

- [CreateOrUpdate](#vaults_createorupdate)
- [Delete](#vaults_delete)
- [GetByResourceGroup](#vaults_getbyresourcegroup)
- [List](#vaults_list)
- [ListByResourceGroup](#vaults_listbyresourcegroup)
- [Update](#vaults_update)
### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/ListOperations.json
     */
    /**
     * Sample code: ListOperations.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void listOperations(com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### PrivateLinkResourcesOperation_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResourcesOperation Get. */
public final class PrivateLinkResourcesOperationGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/GetPrivateLinkResources.json
     */
    /**
     * Sample code: Get PrivateLinkResource.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void getPrivateLinkResource(
        com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        manager
            .privateLinkResourcesOperations()
            .getWithResponse("petesting", "pemsi-ecy-rsv2", "backupResource", Context.NONE);
    }
}
```

### PrivateLinkResourcesOperation_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResourcesOperation List. */
public final class PrivateLinkResourcesOperationListSamples {
    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/ListPrivateLinkResources.json
     */
    /**
     * Sample code: List PrivateLinkResources.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void listPrivateLinkResources(
        com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        manager.privateLinkResourcesOperations().list("petesting", "pemsi-ecy-rsv2", Context.NONE);
    }
}
```

### RecoveryServices_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.recoveryservices.models.CheckNameAvailabilityParameters;

/** Samples for RecoveryServices CheckNameAvailability. */
public final class RecoveryServicesCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/CheckNameAvailability_Available.json
     */
    /**
     * Sample code: Availability status of Resource Name when no resource with same name, type and subscription exists,
     * nor has been deleted within last 24 hours.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void
        availabilityStatusOfResourceNameWhenNoResourceWithSameNameTypeAndSubscriptionExistsNorHasBeenDeletedWithinLast24Hours(
            com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        manager
            .recoveryServices()
            .checkNameAvailabilityWithResponse(
                "resGroupFoo",
                "westus",
                new CheckNameAvailabilityParameters()
                    .withType("Microsoft.RecoveryServices/Vaults")
                    .withName("swaggerExample"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/CheckNameAvailability_NotAvailable.json
     */
    /**
     * Sample code: Availability status of Resource Name when resource with same name, type and subscription exists.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void availabilityStatusOfResourceNameWhenResourceWithSameNameTypeAndSubscriptionExists(
        com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        manager
            .recoveryServices()
            .checkNameAvailabilityWithResponse(
                "resGroupBar",
                "westus",
                new CheckNameAvailabilityParameters()
                    .withType("Microsoft.RecoveryServices/Vaults")
                    .withName("swaggerExample2"),
                Context.NONE);
    }
}
```

### RegisteredIdentities_Delete

```java
import com.azure.core.util.Context;

/** Samples for RegisteredIdentities Delete. */
public final class RegisteredIdentitiesDeleteSamples {
    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/DeleteRegisteredIdentities.json
     */
    /**
     * Sample code: Delete registered Identity.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void deleteRegisteredIdentity(
        com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        manager.registeredIdentities().deleteWithResponse("BCDRIbzRG", "BCDRIbzVault", "dpmcontainer01", Context.NONE);
    }
}
```

### ReplicationUsages_List

```java
import com.azure.core.util.Context;

/** Samples for ReplicationUsages List. */
public final class ReplicationUsagesListSamples {
    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/ListReplicationUsages.json
     */
    /**
     * Sample code: Gets Replication usages of vault.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void getsReplicationUsagesOfVault(
        com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        manager.replicationUsages().list("avrai7517RG1", "avrai7517Vault1", Context.NONE);
    }
}
```

### ResourceProvider_GetOperationResult

```java
import com.azure.core.util.Context;

/** Samples for ResourceProvider GetOperationResult. */
public final class ResourceProviderGetOperationResultSamples {
    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/GetOperationResult.json
     */
    /**
     * Sample code: Get Operation Result.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void getOperationResult(com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        manager
            .resourceProviders()
            .getOperationResultWithResponse(
                "HelloWorld",
                "swaggerExample",
                "YWUzNDFkMzQtZmM5OS00MmUyLWEzNDMtZGJkMDIxZjlmZjgzOzdmYzBiMzhmLTc2NmItNDM5NS05OWQ1LTVmOGEzNzg4MWQzNA==",
                Context.NONE);
    }
}
```

### ResourceProvider_GetOperationStatus

```java
import com.azure.core.util.Context;

/** Samples for ResourceProvider GetOperationStatus. */
public final class ResourceProviderGetOperationStatusSamples {
    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/GetOperationStatus.json
     */
    /**
     * Sample code: Get Operation Status.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void getOperationStatus(com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        manager
            .resourceProviders()
            .getOperationStatusWithResponse(
                "HelloWorld",
                "swaggerExample",
                "YWUzNDFkMzQtZmM5OS00MmUyLWEzNDMtZGJkMDIxZjlmZjgzOzdmYzBiMzhmLTc2NmItNDM5NS05OWQ1LTVmOGEzNzg4MWQzNA==",
                Context.NONE);
    }
}
```

### Usages_ListByVaults

```java
import com.azure.core.util.Context;

/** Samples for Usages ListByVaults. */
public final class UsagesListByVaultsSamples {
    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/ListUsages.json
     */
    /**
     * Sample code: Gets vault usages.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void getsVaultUsages(com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        manager.usages().listByVaults("Default-RecoveryServices-ResourceGroup", "swaggerExample", Context.NONE);
    }
}
```

### VaultCertificates_Create

```java
import com.azure.resourcemanager.recoveryservices.models.AuthType;
import com.azure.resourcemanager.recoveryservices.models.RawCertificateData;

/** Samples for VaultCertificates Create. */
public final class VaultCertificatesCreateSamples {
    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/PUTVaultCred.json
     */
    /**
     * Sample code: Download vault credential file.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void downloadVaultCredentialFile(
        com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        manager
            .vaultCertificates()
            .define("BCDRIbzVault77777777-d41f-4550-9f70-7708a3a2283b-12-18-2017-vaultcredentials")
            .withExistingVault("BCDRIbzRG", "BCDRIbzVault")
            .withProperties(
                new RawCertificateData()
                    .withAuthType(AuthType.AAD)
                    .withCertificate(
                        "MTTC3TCCAcWgAwIBAgIQEj9h+ZLlXK9KrqZX9UkAnzANBgkqhkiG9w0BAQUFADAeMRwwGgYDVQQDExNXaW5kb3dzIEF6dXJlIFRvb2xzMB4XDTE3MTIxODA5MTc1M1oXDTE3MTIyMzA5Mjc1M1owHjEcMBoGA1UEAxMTV2luZG93cyBBenVyZSBUb29sczCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK773/eZZ69RbZZAT05r9MjUxu9y1L1Pn1EgPk62IPJyHlO3OZA922eSBahhP4bgmFljN4LVReqQ5eT/wqO0Zhc+yFkUy4U4RdbQLeUZt2W7yy9XLXgVvqeYDgsjg/QhHetgHArQBW+tlQq5+zPdU7zchI4rbShSJrWhLrZFWiOyFPsuAE4joUQHNlRifdCTsBGKk8HRCY3j1S3c4bfEn3zxlrvrXXssRuW5mJM95rMk0tskoRxXSCi6i9bnlki2Cs9mpVMmBFeofs41KwzlWU0TgpdD8s1QEdvfGB5NbByfetPX7MfJaTBeHZEGbv/Iq8l72u8sPBoOhcaH7qDE/mECAwEAAaMXMBUwEwYDVR0lBAwwCgYIKwYBBQUHAwIwDQYJKoZIhvcNAQEFBQADggEBAILfgHluye1Q+WelhgWhpBBdIq2C0btfV8eFsZaTlBUrM0fwpxQSlAWc2oYHVMQI4A5iUjbDOY35O4yc+TnWKDBKf+laqDP+yos4aiUPuadGUZfvDk7kuw7xeECs64JpHAIEKdRHFW9rD3gwG+nIWaDnEL/7rTyhL3kXrRW2MSUAL8g3GX8Z45c+MQY0jmASIqWdhGn1vpAGyA9mKkzsqg7FXjg8GZb24tGl5Ky85+ip4dkBfXinDD8WwaGyjhGGK97ErvNmN36qly/H0H1Qngiovg1FbHDmkcFO5QclnEJsFFmcO2CcHp5Fqh2wXn5O1cQaxCIRTpQ/uXRpDjl2wKs="
                            .getBytes()))
            .create();
    }
}
```

### VaultExtendedInfo_CreateOrUpdate

```java
import com.azure.core.util.Context;

/** Samples for VaultExtendedInfo CreateOrUpdate. */
public final class VaultExtendedInfoCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/UpdateVaultExtendedInfo.json
     */
    /**
     * Sample code: Put ExtendedInfo of Resource.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void putExtendedInfoOfResource(
        com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        manager
            .vaultExtendedInfoes()
            .createOrUpdateWithResponse("Default-RecoveryServices-ResourceGroup", "swaggerExample", null, Context.NONE);
    }
}
```

### VaultExtendedInfo_Get

```java
import com.azure.core.util.Context;

/** Samples for VaultExtendedInfo Get. */
public final class VaultExtendedInfoGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/GETVaultExtendedInfo.json
     */
    /**
     * Sample code: Get ExtendedInfo of Resource.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void getExtendedInfoOfResource(
        com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        manager
            .vaultExtendedInfoes()
            .getWithResponse("Default-RecoveryServices-ResourceGroup", "swaggerExample", Context.NONE);
    }
}
```

### VaultExtendedInfo_Update

```java
import com.azure.core.util.Context;

/** Samples for VaultExtendedInfo Update. */
public final class VaultExtendedInfoUpdateSamples {
    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/UpdateVaultExtendedInfo.json
     */
    /**
     * Sample code: PATCH ExtendedInfo of Resource.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void pATCHExtendedInfoOfResource(
        com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        manager
            .vaultExtendedInfoes()
            .updateWithResponse("Default-RecoveryServices-ResourceGroup", "swaggerExample", null, Context.NONE);
    }
}
```

### Vaults_CreateOrUpdate

```java
import com.azure.resourcemanager.recoveryservices.models.AlertsState;
import com.azure.resourcemanager.recoveryservices.models.AzureMonitorAlertSettings;
import com.azure.resourcemanager.recoveryservices.models.ClassicAlertSettings;
import com.azure.resourcemanager.recoveryservices.models.CmkKekIdentity;
import com.azure.resourcemanager.recoveryservices.models.CmkKeyVaultProperties;
import com.azure.resourcemanager.recoveryservices.models.IdentityData;
import com.azure.resourcemanager.recoveryservices.models.InfrastructureEncryptionState;
import com.azure.resourcemanager.recoveryservices.models.MonitoringSettings;
import com.azure.resourcemanager.recoveryservices.models.ResourceIdentityType;
import com.azure.resourcemanager.recoveryservices.models.Sku;
import com.azure.resourcemanager.recoveryservices.models.SkuName;
import com.azure.resourcemanager.recoveryservices.models.UserIdentity;
import com.azure.resourcemanager.recoveryservices.models.VaultProperties;
import com.azure.resourcemanager.recoveryservices.models.VaultPropertiesEncryption;
import java.util.HashMap;
import java.util.Map;

/** Samples for Vaults CreateOrUpdate. */
public final class VaultsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/PUTVault_WithCMK.json
     */
    /**
     * Sample code: Create or Update Vault with CustomerManagedKeys.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void createOrUpdateVaultWithCustomerManagedKeys(
        com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        manager
            .vaults()
            .define("swaggerExample")
            .withRegion("West US")
            .withExistingResourceGroup("Default-RecoveryServices-ResourceGroup")
            .withIdentity(
                new IdentityData()
                    .withType(ResourceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/85bf5e8c-3084-4f42-add2-746ebb7e97b2/resourcegroups/defaultrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/examplemsi",
                            new UserIdentity())))
            .withProperties(
                new VaultProperties()
                    .withEncryption(
                        new VaultPropertiesEncryption()
                            .withKeyVaultProperties(
                                new CmkKeyVaultProperties()
                                    .withKeyUri(
                                        "https://cmk2xkv.vault.azure.net/keys/Key1/0767b348bb1a4c07baa6c4ec0055d2b3"))
                            .withKekIdentity(
                                new CmkKekIdentity()
                                    .withUserAssignedIdentity(
                                        "/subscriptions/85bf5e8c-3084-4f42-add2-746ebb7e97b2/resourcegroups/defaultrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/examplemsi"))
                            .withInfrastructureEncryption(InfrastructureEncryptionState.ENABLED)))
            .withSku(new Sku().withName(SkuName.STANDARD))
            .create();
    }

    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/PUTVault.json
     */
    /**
     * Sample code: Create or Update Recovery Services vault.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void createOrUpdateRecoveryServicesVault(
        com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        manager
            .vaults()
            .define("swaggerExample")
            .withRegion("West US")
            .withExistingResourceGroup("Default-RecoveryServices-ResourceGroup")
            .withIdentity(new IdentityData().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
            .withProperties(new VaultProperties())
            .withSku(new Sku().withName(SkuName.STANDARD))
            .create();
    }

    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/PUTVault_WithUserAssignedIdentity.json
     */
    /**
     * Sample code: Create or Update Vault with User Assigned Identity.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void createOrUpdateVaultWithUserAssignedIdentity(
        com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        manager
            .vaults()
            .define("swaggerExample")
            .withRegion("West US")
            .withExistingResourceGroup("Default-RecoveryServices-ResourceGroup")
            .withIdentity(
                new IdentityData()
                    .withType(ResourceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/85bf5e8c-3084-4f42-add2-746ebb7e97b2/resourcegroups/defaultrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/examplemsi",
                            new UserIdentity())))
            .withProperties(new VaultProperties())
            .withSku(new Sku().withName(SkuName.STANDARD))
            .create();
    }

    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/PUTVault_WithMonitoringSettings.json
     */
    /**
     * Sample code: Create or Update Vault With Monitoring Setting.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void createOrUpdateVaultWithMonitoringSetting(
        com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        manager
            .vaults()
            .define("swaggerExample")
            .withRegion("West US")
            .withExistingResourceGroup("Default-RecoveryServices-ResourceGroup")
            .withIdentity(new IdentityData().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
            .withProperties(
                new VaultProperties()
                    .withMonitoringSettings(
                        new MonitoringSettings()
                            .withAzureMonitorAlertSettings(
                                new AzureMonitorAlertSettings().withAlertsForAllJobFailures(AlertsState.ENABLED))
                            .withClassicAlertSettings(
                                new ClassicAlertSettings().withAlertsForCriticalOperations(AlertsState.DISABLED))))
            .withSku(new Sku().withName(SkuName.STANDARD))
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

### Vaults_Delete

```java
import com.azure.core.util.Context;

/** Samples for Vaults Delete. */
public final class VaultsDeleteSamples {
    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/DeleteVault.json
     */
    /**
     * Sample code: Delete Recovery Services Vault.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void deleteRecoveryServicesVault(
        com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        manager.vaults().deleteWithResponse("Default-RecoveryServices-ResourceGroup", "swaggerExample", Context.NONE);
    }
}
```

### Vaults_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Vaults GetByResourceGroup. */
public final class VaultsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/GETVault.json
     */
    /**
     * Sample code: Get Recovery Services Resource.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void getRecoveryServicesResource(
        com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        manager
            .vaults()
            .getByResourceGroupWithResponse("Default-RecoveryServices-ResourceGroup", "swaggerExample", Context.NONE);
    }
}
```

### Vaults_List

```java
import com.azure.core.util.Context;

/** Samples for Vaults List. */
public final class VaultsListSamples {
    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/ListBySubscriptionIds.json
     */
    /**
     * Sample code: List of Recovery Services Resources in SubscriptionId.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void listOfRecoveryServicesResourcesInSubscriptionId(
        com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        manager.vaults().list(Context.NONE);
    }
}
```

### Vaults_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Vaults ListByResourceGroup. */
public final class VaultsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/ListResources.json
     */
    /**
     * Sample code: List of Recovery Services Resources in ResourceGroup.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void listOfRecoveryServicesResourcesInResourceGroup(
        com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        manager.vaults().listByResourceGroup("Default-RecoveryServices-ResourceGroup", Context.NONE);
    }
}
```

### Vaults_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.recoveryservices.models.AlertsState;
import com.azure.resourcemanager.recoveryservices.models.AzureMonitorAlertSettings;
import com.azure.resourcemanager.recoveryservices.models.ClassicAlertSettings;
import com.azure.resourcemanager.recoveryservices.models.CmkKekIdentity;
import com.azure.resourcemanager.recoveryservices.models.CmkKeyVaultProperties;
import com.azure.resourcemanager.recoveryservices.models.IdentityData;
import com.azure.resourcemanager.recoveryservices.models.InfrastructureEncryptionState;
import com.azure.resourcemanager.recoveryservices.models.MonitoringSettings;
import com.azure.resourcemanager.recoveryservices.models.ResourceIdentityType;
import com.azure.resourcemanager.recoveryservices.models.UserIdentity;
import com.azure.resourcemanager.recoveryservices.models.Vault;
import com.azure.resourcemanager.recoveryservices.models.VaultProperties;
import com.azure.resourcemanager.recoveryservices.models.VaultPropertiesEncryption;
import java.util.HashMap;
import java.util.Map;

/** Samples for Vaults Update. */
public final class VaultsUpdateSamples {
    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/PATCHVault_WithMonitoringSettings.json
     */
    /**
     * Sample code: Update Vault With Monitoring Setting.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void updateVaultWithMonitoringSetting(
        com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        Vault resource =
            manager.vaults().getByResourceGroupWithResponse("HelloWorld", "swaggerExample", Context.NONE).getValue();
        resource
            .update()
            .withTags(mapOf("PatchKey", "PatchKeyUpdated"))
            .withProperties(
                new VaultProperties()
                    .withMonitoringSettings(
                        new MonitoringSettings()
                            .withAzureMonitorAlertSettings(
                                new AzureMonitorAlertSettings().withAlertsForAllJobFailures(AlertsState.ENABLED))
                            .withClassicAlertSettings(
                                new ClassicAlertSettings().withAlertsForCriticalOperations(AlertsState.DISABLED))))
            .apply();
    }

    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/PATCHVault_WithCMK.json
     */
    /**
     * Sample code: Update Resource With CustomerManagedKeys.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void updateResourceWithCustomerManagedKeys(
        com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        Vault resource =
            manager.vaults().getByResourceGroupWithResponse("HelloWorld", "swaggerExample", Context.NONE).getValue();
        resource
            .update()
            .withTags(mapOf("PatchKey", "PatchKeyUpdated"))
            .withProperties(
                new VaultProperties()
                    .withEncryption(
                        new VaultPropertiesEncryption()
                            .withKeyVaultProperties(
                                new CmkKeyVaultProperties()
                                    .withKeyUri(
                                        "https://cmk2xkv.vault.azure.net/keys/Key1/0767b348bb1a4c07baa6c4ec0055d2b3"))
                            .withKekIdentity(
                                new CmkKekIdentity()
                                    .withUserAssignedIdentity(
                                        "/subscriptions/85bf5e8c-3084-4f42-add2-746ebb7e97b2/resourcegroups/defaultrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/examplemsi"))
                            .withInfrastructureEncryption(InfrastructureEncryptionState.ENABLED)))
            .withIdentity(
                new IdentityData()
                    .withType(ResourceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/85bf5e8c-3084-4f42-add2-746ebb7e97b2/resourcegroups/defaultrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/examplemsi",
                            new UserIdentity())))
            .apply();
    }

    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/PatchVault_WithCMK2.json
     */
    /**
     * Sample code: Update Resource With CustomerManagedKeys2.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void updateResourceWithCustomerManagedKeys2(
        com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        Vault resource =
            manager.vaults().getByResourceGroupWithResponse("HelloWorld", "swaggerExample", Context.NONE).getValue();
        resource
            .update()
            .withTags(mapOf("PatchKey", "PatchKeyUpdated"))
            .withProperties(
                new VaultProperties()
                    .withEncryption(
                        new VaultPropertiesEncryption()
                            .withKekIdentity(new CmkKekIdentity().withUseSystemAssignedIdentity(true))))
            .withIdentity(new IdentityData().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
            .apply();
    }

    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/PATCHVault_WithCMK3.json
     */
    /**
     * Sample code: Update Resource With CustomerManagedKeys3.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void updateResourceWithCustomerManagedKeys3(
        com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        Vault resource =
            manager.vaults().getByResourceGroupWithResponse("HelloWorld", "swaggerExample", Context.NONE).getValue();
        resource
            .update()
            .withTags(mapOf("PatchKey", "PatchKeyUpdated"))
            .withProperties(
                new VaultProperties()
                    .withEncryption(
                        new VaultPropertiesEncryption()
                            .withKeyVaultProperties(
                                new CmkKeyVaultProperties()
                                    .withKeyUri(
                                        "https://cmk2xkv.vault.azure.net/keys/Key1/0767b348bb1a4c07baa6c4ec0055d2b3"))))
            .withIdentity(
                new IdentityData()
                    .withType(ResourceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/85bf5e8c-3084-4f42-add2-746ebb7e97b2/resourcegroups/defaultrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/examplemsi",
                            new UserIdentity())))
            .apply();
    }

    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/PATCHVault.json
     */
    /**
     * Sample code: Update Resource.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void updateResource(com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        Vault resource =
            manager.vaults().getByResourceGroupWithResponse("HelloWorld", "swaggerExample", Context.NONE).getValue();
        resource.update().withTags(mapOf("PatchKey", "PatchKeyUpdated")).apply();
    }

    /*
     * x-ms-original-file: specification/recoveryservices/resource-manager/Microsoft.RecoveryServices/stable/2022-04-01/examples/PATCHVault_WithUserAssignedIdentity.json
     */
    /**
     * Sample code: Update Resource With User Assigned Identity.
     *
     * @param manager Entry point to RecoveryServicesManager.
     */
    public static void updateResourceWithUserAssignedIdentity(
        com.azure.resourcemanager.recoveryservices.RecoveryServicesManager manager) {
        Vault resource =
            manager.vaults().getByResourceGroupWithResponse("HelloWorld", "swaggerExample", Context.NONE).getValue();
        resource
            .update()
            .withTags(mapOf("PatchKey", "PatchKeyUpdated"))
            .withIdentity(
                new IdentityData()
                    .withType(ResourceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/85bf5e8c-3084-4f42-add2-746ebb7e97b2/resourcegroups/defaultrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/examplemsi",
                            new UserIdentity())))
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

