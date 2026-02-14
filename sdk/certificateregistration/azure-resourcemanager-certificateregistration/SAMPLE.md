# Code snippets and samples


## AppServiceCertificateOrders

- [CreateOrUpdate](#appservicecertificateorders_createorupdate)
- [CreateOrUpdateCertificate](#appservicecertificateorders_createorupdatecertificate)
- [Delete](#appservicecertificateorders_delete)
- [DeleteCertificate](#appservicecertificateorders_deletecertificate)
- [GetByResourceGroup](#appservicecertificateorders_getbyresourcegroup)
- [GetCertificate](#appservicecertificateorders_getcertificate)
- [List](#appservicecertificateorders_list)
- [ListByResourceGroup](#appservicecertificateorders_listbyresourcegroup)
- [ListCertificates](#appservicecertificateorders_listcertificates)
- [Reissue](#appservicecertificateorders_reissue)
- [Renew](#appservicecertificateorders_renew)
- [ResendEmail](#appservicecertificateorders_resendemail)
- [ResendRequestEmails](#appservicecertificateorders_resendrequestemails)
- [RetrieveCertificateActions](#appservicecertificateorders_retrievecertificateactions)
- [RetrieveCertificateEmailHistory](#appservicecertificateorders_retrievecertificateemailhistory)
- [RetrieveSiteSeal](#appservicecertificateorders_retrievesiteseal)
- [Update](#appservicecertificateorders_update)
- [UpdateCertificate](#appservicecertificateorders_updatecertificate)
- [ValidatePurchaseInformation](#appservicecertificateorders_validatepurchaseinformation)
- [VerifyDomainOwnership](#appservicecertificateorders_verifydomainownership)

## CertificateOrdersDiagnostics

- [GetAppServiceCertificateOrderDetectorResponse](#certificateordersdiagnostics_getappservicecertificateorderdetectorresponse)
- [ListAppServiceCertificateOrderDetectorResponse](#certificateordersdiagnostics_listappservicecertificateorderdetectorresponse)

## CertificateRegistrationProvider

- [ListOperations](#certificateregistrationprovider_listoperations)
### AppServiceCertificateOrders_CreateOrUpdate

```java
import com.azure.resourcemanager.certificateregistration.fluent.models.AppServiceCertificate;
import com.azure.resourcemanager.certificateregistration.models.CertificateProductType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AppServiceCertificateOrders CreateOrUpdate.
 */
public final class AppServiceCertificateOrdersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-11-01/CreateAppServiceCertificateOrder.json
     */
    /**
     * Sample code: Create Certificate order.
     * 
     * @param manager Entry point to CertificateRegistrationManager.
     */
    public static void createCertificateOrder(
        com.azure.resourcemanager.certificateregistration.CertificateRegistrationManager manager) {
        manager.appServiceCertificateOrders()
            .define("SampleCertificateOrderName")
            .withRegion("Global")
            .withExistingResourceGroup("testrg123")
            .withCertificates(mapOf("SampleCertName1",
                new AppServiceCertificate().withKeyVaultId("fakeTokenPlaceholder")
                    .withKeyVaultSecretName("fakeTokenPlaceholder"),
                "SampleCertName2",
                new AppServiceCertificate().withKeyVaultId("fakeTokenPlaceholder")
                    .withKeyVaultSecretName("fakeTokenPlaceholder")))
            .withDistinguishedName("CN=SampleCustomDomain.com")
            .withValidityInYears(2)
            .withKeySize(2048)
            .withProductType(CertificateProductType.STANDARD_DOMAIN_VALIDATED_SSL)
            .withAutoRenew(true)
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

### AppServiceCertificateOrders_CreateOrUpdateCertificate

```java
/**
 * Samples for AppServiceCertificateOrders CreateOrUpdateCertificate.
 */
public final class AppServiceCertificateOrdersCreateOrUpdateCertificateSamples {
    /*
     * x-ms-original-file: 2024-11-01/CreateAppServiceCertificate.json
     */
    /**
     * Sample code: Create Certificate.
     * 
     * @param manager Entry point to CertificateRegistrationManager.
     */
    public static void
        createCertificate(com.azure.resourcemanager.certificateregistration.CertificateRegistrationManager manager) {
        manager.appServiceCertificateOrders()
            .defineCertificate("SampleCertName1")
            .withRegion("Global")
            .withExistingCertificateOrder("testrg123", "SampleCertificateOrderName")
            .withKeyVaultId(
                "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourcegroups/testrg123/providers/microsoft.keyvault/vaults/SamplevaultName")
            .withKeyVaultSecretName("SampleSecretName1")
            .create();
    }
}
```

### AppServiceCertificateOrders_Delete

```java
/**
 * Samples for AppServiceCertificateOrders Delete.
 */
public final class AppServiceCertificateOrdersDeleteSamples {
    /*
     * x-ms-original-file: 2024-11-01/DeleteAppServiceCertificateOrder.json
     */
    /**
     * Sample code: Delete App Service Certificate Order.
     * 
     * @param manager Entry point to CertificateRegistrationManager.
     */
    public static void deleteAppServiceCertificateOrder(
        com.azure.resourcemanager.certificateregistration.CertificateRegistrationManager manager) {
        manager.appServiceCertificateOrders()
            .deleteByResourceGroupWithResponse("testrg123", "SampleCertificateOrderName",
                com.azure.core.util.Context.NONE);
    }
}
```

### AppServiceCertificateOrders_DeleteCertificate

```java
/**
 * Samples for AppServiceCertificateOrders DeleteCertificate.
 */
public final class AppServiceCertificateOrdersDeleteCertificateSamples {
    /*
     * x-ms-original-file: 2024-11-01/DeleteAppServiceCertificate.json
     */
    /**
     * Sample code: Delete App Service Certificate.
     * 
     * @param manager Entry point to CertificateRegistrationManager.
     */
    public static void deleteAppServiceCertificate(
        com.azure.resourcemanager.certificateregistration.CertificateRegistrationManager manager) {
        manager.appServiceCertificateOrders()
            .deleteCertificateWithResponse("testrg123", "SampleCertificateOrderName", "SampleCertName1",
                com.azure.core.util.Context.NONE);
    }
}
```

### AppServiceCertificateOrders_GetByResourceGroup

```java
/**
 * Samples for AppServiceCertificateOrders GetByResourceGroup.
 */
public final class AppServiceCertificateOrdersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-11-01/GetAppServiceCertificateOrder.json
     */
    /**
     * Sample code: Get App Service Certificate Order.
     * 
     * @param manager Entry point to CertificateRegistrationManager.
     */
    public static void getAppServiceCertificateOrder(
        com.azure.resourcemanager.certificateregistration.CertificateRegistrationManager manager) {
        manager.appServiceCertificateOrders()
            .getByResourceGroupWithResponse("testrg123", "SampleCertificateOrderName",
                com.azure.core.util.Context.NONE);
    }
}
```

### AppServiceCertificateOrders_GetCertificate

```java
/**
 * Samples for AppServiceCertificateOrders GetCertificate.
 */
public final class AppServiceCertificateOrdersGetCertificateSamples {
    /*
     * x-ms-original-file: 2024-11-01/GetAppServiceCertificate.json
     */
    /**
     * Sample code: Get App Service Certificate.
     * 
     * @param manager Entry point to CertificateRegistrationManager.
     */
    public static void getAppServiceCertificate(
        com.azure.resourcemanager.certificateregistration.CertificateRegistrationManager manager) {
        manager.appServiceCertificateOrders()
            .getCertificateWithResponse("testrg123", "SampleCertificateOrderName", "SampleCertName1",
                com.azure.core.util.Context.NONE);
    }
}
```

### AppServiceCertificateOrders_List

```java
/**
 * Samples for AppServiceCertificateOrders List.
 */
public final class AppServiceCertificateOrdersListSamples {
    /*
     * x-ms-original-file: 2024-11-01/ListAppServiceCertificateOrdersBySubscription.json
     */
    /**
     * Sample code: List App Service Certificate orders by subscription.
     * 
     * @param manager Entry point to CertificateRegistrationManager.
     */
    public static void listAppServiceCertificateOrdersBySubscription(
        com.azure.resourcemanager.certificateregistration.CertificateRegistrationManager manager) {
        manager.appServiceCertificateOrders().list(com.azure.core.util.Context.NONE);
    }
}
```

### AppServiceCertificateOrders_ListByResourceGroup

```java
/**
 * Samples for AppServiceCertificateOrders ListByResourceGroup.
 */
public final class AppServiceCertificateOrdersListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-11-01/ListAppServiceCertificateOrdersByResourceGroup.json
     */
    /**
     * Sample code: List App Service Certificate orders by resource group.
     * 
     * @param manager Entry point to CertificateRegistrationManager.
     */
    public static void listAppServiceCertificateOrdersByResourceGroup(
        com.azure.resourcemanager.certificateregistration.CertificateRegistrationManager manager) {
        manager.appServiceCertificateOrders().listByResourceGroup("testrg123", com.azure.core.util.Context.NONE);
    }
}
```

### AppServiceCertificateOrders_ListCertificates

```java
/**
 * Samples for AppServiceCertificateOrders ListCertificates.
 */
public final class AppServiceCertificateOrdersListCertificatesSamples {
    /*
     * x-ms-original-file: 2024-11-01/ListCertificatesByAppServiceCertificateOrder.json
     */
    /**
     * Sample code: List certificates by App Service Certificate.
     * 
     * @param manager Entry point to CertificateRegistrationManager.
     */
    public static void listCertificatesByAppServiceCertificate(
        com.azure.resourcemanager.certificateregistration.CertificateRegistrationManager manager) {
        manager.appServiceCertificateOrders()
            .listCertificates("testrg123", "SampleCertificateOrderName", com.azure.core.util.Context.NONE);
    }
}
```

### AppServiceCertificateOrders_Reissue

```java
import com.azure.resourcemanager.certificateregistration.models.ReissueCertificateOrderRequest;

/**
 * Samples for AppServiceCertificateOrders Reissue.
 */
public final class AppServiceCertificateOrdersReissueSamples {
    /*
     * x-ms-original-file: 2024-11-01/ReissueAppServiceCertificateOrder.json
     */
    /**
     * Sample code: Reissue App Service Certificate Order.
     * 
     * @param manager Entry point to CertificateRegistrationManager.
     */
    public static void reissueAppServiceCertificateOrder(
        com.azure.resourcemanager.certificateregistration.CertificateRegistrationManager manager) {
        manager.appServiceCertificateOrders()
            .reissueWithResponse("testrg123", "SampleCertificateOrderName",
                new ReissueCertificateOrderRequest().withKeySize(2048)
                    .withDelayExistingRevokeInHours(2)
                    .withCsr("CSR1223238Value")
                    .withIsPrivateKeyExternal(false),
                com.azure.core.util.Context.NONE);
    }
}
```

### AppServiceCertificateOrders_Renew

```java
import com.azure.resourcemanager.certificateregistration.models.RenewCertificateOrderRequest;

/**
 * Samples for AppServiceCertificateOrders Renew.
 */
public final class AppServiceCertificateOrdersRenewSamples {
    /*
     * x-ms-original-file: 2024-11-01/RenewAppServiceCertificateOrder.json
     */
    /**
     * Sample code: Renew App Service Certificate Order.
     * 
     * @param manager Entry point to CertificateRegistrationManager.
     */
    public static void renewAppServiceCertificateOrder(
        com.azure.resourcemanager.certificateregistration.CertificateRegistrationManager manager) {
        manager.appServiceCertificateOrders()
            .renewWithResponse("testrg123", "SampleCertificateOrderName",
                new RenewCertificateOrderRequest().withKeySize(2048)
                    .withCsr("CSR1223238Value")
                    .withIsPrivateKeyExternal(false),
                com.azure.core.util.Context.NONE);
    }
}
```

### AppServiceCertificateOrders_ResendEmail

```java
/**
 * Samples for AppServiceCertificateOrders ResendEmail.
 */
public final class AppServiceCertificateOrdersResendEmailSamples {
    /*
     * x-ms-original-file: 2024-11-01/ResendAppServiceCertificateOrderEmail.json
     */
    /**
     * Sample code: Resend App Service Certificate Order email.
     * 
     * @param manager Entry point to CertificateRegistrationManager.
     */
    public static void resendAppServiceCertificateOrderEmail(
        com.azure.resourcemanager.certificateregistration.CertificateRegistrationManager manager) {
        manager.appServiceCertificateOrders()
            .resendEmailWithResponse("testrg123", "SampleCertificateOrderName", com.azure.core.util.Context.NONE);
    }
}
```

### AppServiceCertificateOrders_ResendRequestEmails

```java
import com.azure.resourcemanager.certificateregistration.models.NameIdentifier;

/**
 * Samples for AppServiceCertificateOrders ResendRequestEmails.
 */
public final class AppServiceCertificateOrdersResendRequestEmailsSamples {
    /*
     * x-ms-original-file: 2024-11-01/ResendDomainOwnershipVerificationEmail.json
     */
    /**
     * Sample code: Resend Domain Ownership verification email.
     * 
     * @param manager Entry point to CertificateRegistrationManager.
     */
    public static void resendDomainOwnershipVerificationEmail(
        com.azure.resourcemanager.certificateregistration.CertificateRegistrationManager manager) {
        manager.appServiceCertificateOrders()
            .resendRequestEmailsWithResponse("testrg123", "SampleCertificateOrderName",
                new NameIdentifier().withName("Domain name"), com.azure.core.util.Context.NONE);
    }
}
```

### AppServiceCertificateOrders_RetrieveCertificateActions

```java
/**
 * Samples for AppServiceCertificateOrders RetrieveCertificateActions.
 */
public final class AppServiceCertificateOrdersRetrieveCertificateActionsSamples {
    /*
     * x-ms-original-file: 2024-11-01/RetrieveCertificateOrderActions.json
     */
    /**
     * Sample code: Retrieve Certificate Order Actions.
     * 
     * @param manager Entry point to CertificateRegistrationManager.
     */
    public static void retrieveCertificateOrderActions(
        com.azure.resourcemanager.certificateregistration.CertificateRegistrationManager manager) {
        manager.appServiceCertificateOrders()
            .retrieveCertificateActionsWithResponse("testrg123", "SampleCertOrder", com.azure.core.util.Context.NONE);
    }
}
```

### AppServiceCertificateOrders_RetrieveCertificateEmailHistory

```java
/**
 * Samples for AppServiceCertificateOrders RetrieveCertificateEmailHistory.
 */
public final class AppServiceCertificateOrdersRetrieveCertificateEmailHistorySamples {
    /*
     * x-ms-original-file: 2024-11-01/RetrieveCertificateEmailHistory.json
     */
    /**
     * Sample code: Retrieve Certificate Email History.
     * 
     * @param manager Entry point to CertificateRegistrationManager.
     */
    public static void retrieveCertificateEmailHistory(
        com.azure.resourcemanager.certificateregistration.CertificateRegistrationManager manager) {
        manager.appServiceCertificateOrders()
            .retrieveCertificateEmailHistoryWithResponse("testrg123", "SampleCertOrder",
                com.azure.core.util.Context.NONE);
    }
}
```

### AppServiceCertificateOrders_RetrieveSiteSeal

```java
import com.azure.resourcemanager.certificateregistration.models.SiteSealRequest;

/**
 * Samples for AppServiceCertificateOrders RetrieveSiteSeal.
 */
public final class AppServiceCertificateOrdersRetrieveSiteSealSamples {
    /*
     * x-ms-original-file: 2024-11-01/RetrieveSiteSeal.json
     */
    /**
     * Sample code: Retrieve Site Seal.
     * 
     * @param manager Entry point to CertificateRegistrationManager.
     */
    public static void
        retrieveSiteSeal(com.azure.resourcemanager.certificateregistration.CertificateRegistrationManager manager) {
        manager.appServiceCertificateOrders()
            .retrieveSiteSealWithResponse("testrg123", "SampleCertOrder",
                new SiteSealRequest().withLightTheme(true).withLocale("en-us"), com.azure.core.util.Context.NONE);
    }
}
```

### AppServiceCertificateOrders_Update

```java
import com.azure.resourcemanager.certificateregistration.fluent.models.AppServiceCertificate;
import com.azure.resourcemanager.certificateregistration.models.AppServiceCertificateOrder;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AppServiceCertificateOrders Update.
 */
public final class AppServiceCertificateOrdersUpdateSamples {
    /*
     * x-ms-original-file: 2024-11-01/UpdateAppServiceCertificateOrder.json
     */
    /**
     * Sample code: Update Certificate order.
     * 
     * @param manager Entry point to CertificateRegistrationManager.
     */
    public static void updateCertificateOrder(
        com.azure.resourcemanager.certificateregistration.CertificateRegistrationManager manager) {
        AppServiceCertificateOrder resource = manager.appServiceCertificateOrders()
            .getByResourceGroupWithResponse("testrg123", "SampleCertificateOrderName", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withCertificates(mapOf("SampleCertName1",
                new AppServiceCertificate().withKeyVaultId("fakeTokenPlaceholder")
                    .withKeyVaultSecretName("fakeTokenPlaceholder"),
                "SampleCertName2",
                new AppServiceCertificate().withKeyVaultId("fakeTokenPlaceholder")
                    .withKeyVaultSecretName("fakeTokenPlaceholder")))
            .withAutoRenew(true)
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

### AppServiceCertificateOrders_UpdateCertificate

```java
import com.azure.resourcemanager.certificateregistration.models.AppServiceCertificateResource;

/**
 * Samples for AppServiceCertificateOrders UpdateCertificate.
 */
public final class AppServiceCertificateOrdersUpdateCertificateSamples {
    /*
     * x-ms-original-file: 2024-11-01/UpdateAppServiceCertificate.json
     */
    /**
     * Sample code: Update Certificate.
     * 
     * @param manager Entry point to CertificateRegistrationManager.
     */
    public static void
        updateCertificate(com.azure.resourcemanager.certificateregistration.CertificateRegistrationManager manager) {
        AppServiceCertificateResource resource = manager.appServiceCertificateOrders()
            .getCertificateWithResponse("testrg123", "SampleCertificateOrderName", "SampleCertName1",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withKeyVaultId(
                "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourcegroups/testrg123/providers/microsoft.keyvault/vaults/SamplevaultName")
            .withKeyVaultSecretName("SampleSecretName1")
            .apply();
    }
}
```

### AppServiceCertificateOrders_ValidatePurchaseInformation

```java
import com.azure.resourcemanager.certificateregistration.fluent.models.AppServiceCertificate;
import com.azure.resourcemanager.certificateregistration.fluent.models.AppServiceCertificateOrderInner;
import com.azure.resourcemanager.certificateregistration.models.CertificateProductType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AppServiceCertificateOrders ValidatePurchaseInformation.
 */
public final class AppServiceCertificateOrdersValidatePurchaseInformationSamples {
    /*
     * x-ms-original-file: 2024-11-01/ValidateAppServiceCertificatePurchaseInformationBySubscription.json
     */
    /**
     * Sample code: Validate App Service Certificate purchase information by subscription.
     * 
     * @param manager Entry point to CertificateRegistrationManager.
     */
    public static void validateAppServiceCertificatePurchaseInformationBySubscription(
        com.azure.resourcemanager.certificateregistration.CertificateRegistrationManager manager) {
        manager.appServiceCertificateOrders()
            .validatePurchaseInformationWithResponse(new AppServiceCertificateOrderInner().withLocation("Global")
                .withCertificates(mapOf("SampleCertName1",
                    new AppServiceCertificate().withKeyVaultId("fakeTokenPlaceholder")
                        .withKeyVaultSecretName("fakeTokenPlaceholder"),
                    "SampleCertName2",
                    new AppServiceCertificate().withKeyVaultId("fakeTokenPlaceholder")
                        .withKeyVaultSecretName("fakeTokenPlaceholder")))
                .withDistinguishedName("CN=SampleCustomDomain.com")
                .withValidityInYears(2)
                .withKeySize(2048)
                .withProductType(CertificateProductType.STANDARD_DOMAIN_VALIDATED_SSL)
                .withAutoRenew(true), com.azure.core.util.Context.NONE);
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

### AppServiceCertificateOrders_VerifyDomainOwnership

```java
/**
 * Samples for AppServiceCertificateOrders VerifyDomainOwnership.
 */
public final class AppServiceCertificateOrdersVerifyDomainOwnershipSamples {
    /*
     * x-ms-original-file: 2024-11-01/VerifyDomainOwnership.json
     */
    /**
     * Sample code: Verify Domain Ownership.
     * 
     * @param manager Entry point to CertificateRegistrationManager.
     */
    public static void verifyDomainOwnership(
        com.azure.resourcemanager.certificateregistration.CertificateRegistrationManager manager) {
        manager.appServiceCertificateOrders()
            .verifyDomainOwnershipWithResponse("testrg123", "SampleCertificateOrderName",
                com.azure.core.util.Context.NONE);
    }
}
```

### CertificateOrdersDiagnostics_GetAppServiceCertificateOrderDetectorResponse

```java

/**
 * Samples for CertificateOrdersDiagnostics GetAppServiceCertificateOrderDetectorResponse.
 */
public final class CertificateOrdersDiagnosticsGetAppServiceCertificateOrderDeSamples {
    /*
     * x-ms-original-file: 2024-11-01/Diagnostics_GetAppServiceCertificateOrderDetectorResponse.json
     */
    /**
     * Sample code: Get app service certificate order detector response.
     * 
     * @param manager Entry point to CertificateRegistrationManager.
     */
    public static void getAppServiceCertificateOrderDetectorResponse(
        com.azure.resourcemanager.certificateregistration.CertificateRegistrationManager manager) {
        manager.certificateOrdersDiagnostics()
            .getAppServiceCertificateOrderDetectorResponseWithResponse("Sample-WestUSResourceGroup",
                "SampleCertificateOrderName", "AutoRenewStatus", null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### CertificateOrdersDiagnostics_ListAppServiceCertificateOrderDetectorResponse

```java
/**
 * Samples for CertificateOrdersDiagnostics ListAppServiceCertificateOrderDetectorResponse.
 */
public final class CertificateOrdersDiagnosticsListAppServiceCertificateOrderDSamples {
    /*
     * x-ms-original-file: 2024-11-01/Diagnostics_ListAppServiceCertificateOrderDetectorResponse.json
     */
    /**
     * Sample code: List app service certificate detector response.
     * 
     * @param manager Entry point to CertificateRegistrationManager.
     */
    public static void listAppServiceCertificateDetectorResponse(
        com.azure.resourcemanager.certificateregistration.CertificateRegistrationManager manager) {
        manager.certificateOrdersDiagnostics()
            .listAppServiceCertificateOrderDetectorResponse("Sample-WestUSResourceGroup", "SampleCertificateOrderName",
                com.azure.core.util.Context.NONE);
    }
}
```

### CertificateRegistrationProvider_ListOperations

```java
/**
 * Samples for CertificateRegistrationProvider ListOperations.
 */
public final class CertificateRegistrationProviderListOperationsSamples {
    /*
     * x-ms-original-file: 2024-11-01/ListOperations.json
     */
    /**
     * Sample code: List operations.
     * 
     * @param manager Entry point to CertificateRegistrationManager.
     */
    public static void
        listOperations(com.azure.resourcemanager.certificateregistration.CertificateRegistrationManager manager) {
        manager.certificateRegistrationProviders().listOperations(com.azure.core.util.Context.NONE);
    }
}
```

