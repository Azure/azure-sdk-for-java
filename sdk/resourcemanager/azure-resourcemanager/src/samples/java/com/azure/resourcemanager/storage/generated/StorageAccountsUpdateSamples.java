// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.storage.generated;

import com.azure.resourcemanager.storage.models.AccountImmutabilityPolicyProperties;
import com.azure.resourcemanager.storage.models.AccountImmutabilityPolicyState;
import com.azure.resourcemanager.storage.models.ActiveDirectoryProperties;
import com.azure.resourcemanager.storage.models.ActiveDirectoryPropertiesAccountType;
import com.azure.resourcemanager.storage.models.AllowedCopyScope;
import com.azure.resourcemanager.storage.models.AzureFilesIdentityBasedAuthentication;
import com.azure.resourcemanager.storage.models.DefaultAction;
import com.azure.resourcemanager.storage.models.DirectoryServiceOptions;
import com.azure.resourcemanager.storage.models.Encryption;
import com.azure.resourcemanager.storage.models.EncryptionIdentity;
import com.azure.resourcemanager.storage.models.EncryptionService;
import com.azure.resourcemanager.storage.models.EncryptionServices;
import com.azure.resourcemanager.storage.models.ExpirationAction;
import com.azure.resourcemanager.storage.models.Identity;
import com.azure.resourcemanager.storage.models.IdentityType;
import com.azure.resourcemanager.storage.models.ImmutableStorageAccount;
import com.azure.resourcemanager.storage.models.KeyPolicy;
import com.azure.resourcemanager.storage.models.KeySource;
import com.azure.resourcemanager.storage.models.KeyType;
import com.azure.resourcemanager.storage.models.KeyVaultProperties;
import com.azure.resourcemanager.storage.models.Kind;
import com.azure.resourcemanager.storage.models.MinimumTlsVersion;
import com.azure.resourcemanager.storage.models.NetworkRuleSet;
import com.azure.resourcemanager.storage.models.PublicNetworkAccess;
import com.azure.resourcemanager.storage.models.ResourceAccessRule;
import com.azure.resourcemanager.storage.models.RoutingChoice;
import com.azure.resourcemanager.storage.models.RoutingPreference;
import com.azure.resourcemanager.storage.models.SasPolicy;
import com.azure.resourcemanager.storage.models.Sku;
import com.azure.resourcemanager.storage.models.SkuName;
import com.azure.resourcemanager.storage.models.StorageAccountUpdateParameters;
import com.azure.resourcemanager.storage.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for StorageAccounts Update.
 */
public final class StorageAccountsUpdateSamples {
    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2024-01-01/examples/
     * StorageAccountUpdateWithImmutabilityPolicy.json
     */
    /**
     * Sample code: StorageAccountUpdateWithImmutabilityPolicy.
     * 
     * @param azure The entry point for accessing resource management APIs in Azure.
     */
    public static void
        storageAccountUpdateWithImmutabilityPolicy(com.azure.resourcemanager.AzureResourceManager azure) {
        azure.storageAccounts()
            .manager()
            .serviceClient()
            .getStorageAccounts()
            .updateWithResponse("res9407", "sto8596",
                new StorageAccountUpdateParameters()
                    .withImmutableStorageWithVersioning(new ImmutableStorageAccount().withEnabled(true)
                        .withImmutabilityPolicy(
                            new AccountImmutabilityPolicyProperties().withImmutabilityPeriodSinceCreationInDays(15)
                                .withState(AccountImmutabilityPolicyState.LOCKED)
                                .withAllowProtectedAppendWrites(true))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2024-01-01/examples/
     * StorageAccountUpdateUserAssignedIdentityWithFederatedIdentityClientId.json
     */
    /**
     * Sample code: StorageAccountUpdateUserAssignedIdentityWithFederatedIdentityClientId.
     * 
     * @param azure The entry point for accessing resource management APIs in Azure.
     */
    public static void storageAccountUpdateUserAssignedIdentityWithFederatedIdentityClientId(
        com.azure.resourcemanager.AzureResourceManager azure) {
        azure.storageAccounts()
            .manager()
            .serviceClient()
            .getStorageAccounts()
            .updateWithResponse("res131918", "sto131918", new StorageAccountUpdateParameters()
                .withSku(new Sku().withName(SkuName.STANDARD_LRS))
                .withIdentity(new Identity().withType(IdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(mapOf(
                        "/subscriptions/{subscription-id}/resourceGroups/res9101/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{managed-identity-name}",
                        new UserAssignedIdentity())))
                .withKind(Kind.STORAGE)
                .withEncryption(new Encryption()
                    .withServices(new EncryptionServices()
                        .withBlob(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT))
                        .withFile(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT)))
                    .withKeySource(KeySource.MICROSOFT_KEYVAULT)
                    .withKeyVaultProperties(new KeyVaultProperties().withKeyName("fakeTokenPlaceholder")
                        .withKeyVersion("fakeTokenPlaceholder")
                        .withKeyVaultUri("fakeTokenPlaceholder"))
                    .withEncryptionIdentity(new EncryptionIdentity().withEncryptionUserAssignedIdentity(
                        "/subscriptions/{subscription-id}/resourceGroups/res9101/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{managed-identity-name}")
                        .withEncryptionFederatedIdentityClientId("3109d1c4-a5de-4d84-8832-feabb916a4b6"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/storage/resource-manager/Microsoft.Storage/stable/2024-01-01/examples/StorageAccountEnableAD.json
     */
    /**
     * Sample code: StorageAccountEnableAD.
     * 
     * @param azure The entry point for accessing resource management APIs in Azure.
     */
    public static void storageAccountEnableAD(com.azure.resourcemanager.AzureResourceManager azure) {
        azure.storageAccounts()
            .manager()
            .serviceClient()
            .getStorageAccounts()
            .updateWithResponse("res9407", "sto8596",
                new StorageAccountUpdateParameters().withAzureFilesIdentityBasedAuthentication(
                    new AzureFilesIdentityBasedAuthentication().withDirectoryServiceOptions(DirectoryServiceOptions.AD)
                        .withActiveDirectoryProperties(new ActiveDirectoryProperties().withDomainName("adtest.com")
                            .withNetBiosDomainName("adtest.com")
                            .withForestName("adtest.com")
                            .withDomainGuid("aebfc118-9fa9-4732-a21f-d98e41a77ae1")
                            .withDomainSid("S-1-5-21-2400535526-2334094090-2402026252")
                            .withAzureStorageSid("S-1-5-21-2400535526-2334094090-2402026252-0012")
                            .withSamAccountName("sam12498")
                            .withAccountType(ActiveDirectoryPropertiesAccountType.USER))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2024-01-01/examples/
     * StorageAccountUpdateAllowedCopyScopeToAAD.json
     */
    /**
     * Sample code: StorageAccountUpdateAllowedCopyScopeToAAD.
     * 
     * @param azure The entry point for accessing resource management APIs in Azure.
     */
    public static void storageAccountUpdateAllowedCopyScopeToAAD(com.azure.resourcemanager.AzureResourceManager azure) {
        azure.storageAccounts()
            .manager()
            .serviceClient()
            .getStorageAccounts()
            .updateWithResponse("res9407", "sto8596",
                new StorageAccountUpdateParameters()
                    .withEncryption(new Encryption()
                        .withServices(new EncryptionServices()
                            .withBlob(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT))
                            .withFile(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT)))
                        .withKeySource(KeySource.MICROSOFT_STORAGE))
                    .withSasPolicy(new SasPolicy().withSasExpirationPeriod("1.15:59:59")
                        .withExpirationAction(ExpirationAction.LOG))
                    .withKeyPolicy(new KeyPolicy().withKeyExpirationPeriodInDays(20))
                    .withNetworkRuleSet(new NetworkRuleSet()
                        .withResourceAccessRules(Arrays.asList(new ResourceAccessRule()
                            .withTenantId("72f988bf-86f1-41af-91ab-2d7cd011db47")
                            .withResourceId(
                                "/subscriptions/a7e99807-abbf-4642-bdec-2c809a96a8bc/resourceGroups/res9407/providers/Microsoft.Synapse/workspaces/testworkspace")))
                        .withDefaultAction(DefaultAction.ALLOW))
                    .withRoutingPreference(new RoutingPreference().withRoutingChoice(RoutingChoice.MICROSOFT_ROUTING)
                        .withPublishMicrosoftEndpoints(true)
                        .withPublishInternetEndpoints(true))
                    .withAllowBlobPublicAccess(false)
                    .withMinimumTlsVersion(MinimumTlsVersion.TLS1_2)
                    .withAllowSharedKeyAccess(true)
                    .withAllowedCopyScope(AllowedCopyScope.AAD),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2024-01-01/examples/
     * StorageAccountUpdateDisablePublicNetworkAccess.json
     */
    /**
     * Sample code: StorageAccountUpdateDisablePublicNetworkAccess.
     * 
     * @param azure The entry point for accessing resource management APIs in Azure.
     */
    public static void
        storageAccountUpdateDisablePublicNetworkAccess(com.azure.resourcemanager.AzureResourceManager azure) {
        azure.storageAccounts()
            .manager()
            .serviceClient()
            .getStorageAccounts()
            .updateWithResponse("res9407", "sto8596",
                new StorageAccountUpdateParameters()
                    .withEncryption(new Encryption()
                        .withServices(new EncryptionServices()
                            .withBlob(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT))
                            .withFile(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT)))
                        .withKeySource(KeySource.MICROSOFT_STORAGE))
                    .withSasPolicy(new SasPolicy().withSasExpirationPeriod("1.15:59:59")
                        .withExpirationAction(ExpirationAction.LOG))
                    .withKeyPolicy(new KeyPolicy().withKeyExpirationPeriodInDays(20))
                    .withNetworkRuleSet(new NetworkRuleSet()
                        .withResourceAccessRules(Arrays.asList(new ResourceAccessRule()
                            .withTenantId("72f988bf-86f1-41af-91ab-2d7cd011db47")
                            .withResourceId(
                                "/subscriptions/a7e99807-abbf-4642-bdec-2c809a96a8bc/resourceGroups/res9407/providers/Microsoft.Synapse/workspaces/testworkspace")))
                        .withDefaultAction(DefaultAction.ALLOW))
                    .withRoutingPreference(new RoutingPreference().withRoutingChoice(RoutingChoice.MICROSOFT_ROUTING)
                        .withPublishMicrosoftEndpoints(true)
                        .withPublishInternetEndpoints(true))
                    .withAllowBlobPublicAccess(false)
                    .withMinimumTlsVersion(MinimumTlsVersion.TLS1_2)
                    .withAllowSharedKeyAccess(true)
                    .withPublicNetworkAccess(PublicNetworkAccess.DISABLED),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/storage/resource-manager/Microsoft.Storage/stable/2024-01-01/examples/StorageAccountEnableCMK.json
     */
    /**
     * Sample code: StorageAccountEnableCMK.
     * 
     * @param azure The entry point for accessing resource management APIs in Azure.
     */
    public static void storageAccountEnableCMK(com.azure.resourcemanager.AzureResourceManager azure) {
        azure.storageAccounts()
            .manager()
            .serviceClient()
            .getStorageAccounts()
            .updateWithResponse("res9407", "sto8596",
                new StorageAccountUpdateParameters().withEncryption(new Encryption()
                    .withServices(new EncryptionServices()
                        .withBlob(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT))
                        .withFile(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT)))
                    .withKeySource(KeySource.MICROSOFT_KEYVAULT)
                    .withKeyVaultProperties(new KeyVaultProperties().withKeyName("fakeTokenPlaceholder")
                        .withKeyVersion("fakeTokenPlaceholder")
                        .withKeyVaultUri("fakeTokenPlaceholder"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/storage/resource-manager/Microsoft.Storage/stable/2024-01-01/examples/StorageAccountUpdate.json
     */
    /**
     * Sample code: StorageAccountUpdate.
     * 
     * @param azure The entry point for accessing resource management APIs in Azure.
     */
    public static void storageAccountUpdate(com.azure.resourcemanager.AzureResourceManager azure) {
        azure.storageAccounts()
            .manager()
            .serviceClient()
            .getStorageAccounts()
            .updateWithResponse("res9407", "sto8596",
                new StorageAccountUpdateParameters()
                    .withEncryption(new Encryption()
                        .withServices(new EncryptionServices()
                            .withBlob(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT))
                            .withFile(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT)))
                        .withKeySource(KeySource.MICROSOFT_STORAGE))
                    .withSasPolicy(new SasPolicy().withSasExpirationPeriod("1.15:59:59")
                        .withExpirationAction(ExpirationAction.LOG))
                    .withKeyPolicy(new KeyPolicy().withKeyExpirationPeriodInDays(20))
                    .withIsSftpEnabled(true)
                    .withIsLocalUserEnabled(true)
                    .withEnableExtendedGroups(true)
                    .withNetworkRuleSet(new NetworkRuleSet()
                        .withResourceAccessRules(Arrays.asList(new ResourceAccessRule()
                            .withTenantId("72f988bf-86f1-41af-91ab-2d7cd011db47")
                            .withResourceId(
                                "/subscriptions/a7e99807-abbf-4642-bdec-2c809a96a8bc/resourceGroups/res9407/providers/Microsoft.Synapse/workspaces/testworkspace")))
                        .withDefaultAction(DefaultAction.ALLOW))
                    .withRoutingPreference(new RoutingPreference().withRoutingChoice(RoutingChoice.MICROSOFT_ROUTING)
                        .withPublishMicrosoftEndpoints(true)
                        .withPublishInternetEndpoints(true))
                    .withAllowBlobPublicAccess(false)
                    .withMinimumTlsVersion(MinimumTlsVersion.TLS1_2)
                    .withAllowSharedKeyAccess(true)
                    .withDefaultToOAuthAuthentication(false),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/storage/resource-manager/Microsoft.Storage/stable/2024-01-01/examples/
     * StorageAccountUpdateUserAssignedEncryptionIdentityWithCMK.json
     */
    /**
     * Sample code: StorageAccountUpdateUserAssignedEncryptionIdentityWithCMK.
     * 
     * @param azure The entry point for accessing resource management APIs in Azure.
     */
    public static void storageAccountUpdateUserAssignedEncryptionIdentityWithCMK(
        com.azure.resourcemanager.AzureResourceManager azure) {
        azure.storageAccounts()
            .manager()
            .serviceClient()
            .getStorageAccounts()
            .updateWithResponse("res9101", "sto4445", new StorageAccountUpdateParameters()
                .withSku(new Sku().withName(SkuName.STANDARD_LRS))
                .withIdentity(new Identity().withType(IdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(mapOf(
                        "/subscriptions/{subscription-id}/resourceGroups/res9101/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{managed-identity-name}",
                        new UserAssignedIdentity())))
                .withKind(Kind.STORAGE)
                .withEncryption(new Encryption()
                    .withServices(new EncryptionServices()
                        .withBlob(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT))
                        .withFile(new EncryptionService().withEnabled(true).withKeyType(KeyType.ACCOUNT)))
                    .withKeySource(KeySource.MICROSOFT_KEYVAULT)
                    .withKeyVaultProperties(new KeyVaultProperties().withKeyName("fakeTokenPlaceholder")
                        .withKeyVersion("fakeTokenPlaceholder")
                        .withKeyVaultUri("fakeTokenPlaceholder"))
                    .withEncryptionIdentity(new EncryptionIdentity().withEncryptionUserAssignedIdentity(
                        "/subscriptions/{subscription-id}/resourceGroups/res9101/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{managed-identity-name}"))),
                com.azure.core.util.Context.NONE);
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
