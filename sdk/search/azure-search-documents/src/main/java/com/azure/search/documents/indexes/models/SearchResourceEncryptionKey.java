// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A customer-managed encryption key in Azure Key Vault. Keys that you create
 * and manage can be used to encrypt or decrypt data-at-rest in Azure Cognitive
 * Search, such as indexes and synonym maps.
 */
@Fluent
public final class SearchResourceEncryptionKey {
    /*
     * The name of your Azure Key Vault key to be used to encrypt your data at
     * rest.
     */
    @JsonProperty(value = "keyVaultKeyName", required = true)
    private String keyName;

    /*
     * The version of your Azure Key Vault key to be used to encrypt your data
     * at rest.
     */
    @JsonProperty(value = "keyVaultKeyVersion", required = true)
    private String keyVersion;

    /*
     * The URI of your Azure Key Vault, also referred to as DNS name, that
     * contains the key to be used to encrypt your data at rest. An example URI
     * might be https://my-keyvault-name.vault.azure.net.
     */
    @JsonProperty(value = "keyVaultUri", required = true)
    private String vaultUrl;

    /*
     * An AAD Application ID that was granted the required access permissions
     * to the Azure Key Vault that is to be used when encrypting your data at
     * rest. The Application ID should not be confused with the Object ID for
     * your AAD Application.
     */
    @JsonProperty(value = "applicationId", required = true)
    private String applicationId;

    /*
     * The authentication key of the specified AAD application.
     */
    @JsonProperty(value = "applicationSecret")
    private String applicationSecret;

    /**
     * Get the keyName property: The name of your Azure Key Vault key to be
     * used to encrypt your data at rest.
     *
     * @return the keyName value.
     */
    public String getKeyName() {
        return this.keyName;
    }

    /**
     * Set the keyName property: The name of your Azure Key Vault key to be
     * used to encrypt your data at rest.
     *
     * @param keyName the keyName value to set.
     * @return the SearchResourceEncryptionKey object itself.
     */
    public SearchResourceEncryptionKey setKeyName(String keyName) {
        this.keyName = keyName;
        return this;
    }

    /**
     * Get the keyVersion property: The version of your Azure Key Vault key to
     * be used to encrypt your data at rest.
     *
     * @return the keyVersion value.
     */
    public String getKeyVersion() {
        return this.keyVersion;
    }

    /**
     * Set the keyVersion property: The version of your Azure Key Vault key to
     * be used to encrypt your data at rest.
     *
     * @param keyVersion the keyVersion value to set.
     * @return the SearchResourceEncryptionKey object itself.
     */
    public SearchResourceEncryptionKey setKeyVersion(String keyVersion) {
        this.keyVersion = keyVersion;
        return this;
    }

    /**
     * Get the vaultUri property: The URI of your Azure Key Vault, also
     * referred to as DNS name, that contains the key to be used to encrypt
     * your data at rest. An example URI might be
     * https://my-keyvault-name.vault.azure.net.
     *
     * @return the vaultUri value.
     */
    public String getVaultUrl() {
        return this.vaultUrl;
    }

    /**
     * Set the vaultUri property: The URI of your Azure Key Vault, also
     * referred to as DNS name, that contains the key to be used to encrypt
     * your data at rest. An example URI might be
     * https://my-keyvault-name.vault.azure.net.
     *
     * @param vaultUrl the vaultUri value to set.
     * @return the SearchResourceEncryptionKey object itself.
     */
    public SearchResourceEncryptionKey setVaultUrl(String vaultUrl) {
        this.vaultUrl = vaultUrl;
        return this;
    }

    /**
     * Get the applicationId property: An AAD Application ID that was granted
     * the required access permissions to the Azure Key Vault that is to be
     * used when encrypting your data at rest. The Application ID should not be
     * confused with the Object ID for your AAD Application.
     *
     * @return the applicationId value.
     */
    public String getApplicationId() {
        return this.applicationId;
    }

    /**
     * Set the applicationId property: An AAD Application ID that was granted
     * the required access permissions to the Azure Key Vault that is to be
     * used when encrypting your data at rest. The Application ID should not be
     * confused with the Object ID for your AAD Application.
     *
     * @param applicationId the applicationId value to set.
     * @return the SearchResourceEncryptionKey object itself.
     */
    public SearchResourceEncryptionKey setApplicationId(String applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    /**
     * Get the applicationSecret property: The authentication key of the
     * specified AAD application.
     *
     * @return the applicationSecret value.
     */
    public String getApplicationSecret() {
        return this.applicationSecret;
    }

    /**
     * Set the applicationSecret property: The authentication key of the
     * specified AAD application.
     *
     * @param applicationSecret the applicationSecret value to set.
     * @return the SearchResourceEncryptionKey object itself.
     */
    public SearchResourceEncryptionKey setApplicationSecret(String applicationSecret) {
        this.applicationSecret = applicationSecret;
        return this;
    }
}
