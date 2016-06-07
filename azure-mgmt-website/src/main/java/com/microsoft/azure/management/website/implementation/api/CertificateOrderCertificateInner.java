/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Class representing the Key Vault container for certificate purchased
 * through Azure.
 */
@JsonFlatten
public class CertificateOrderCertificateInner extends Resource {
    /**
     * Key Vault Csm resource Id.
     */
    @JsonProperty(value = "properties.keyVaultId")
    private String keyVaultId;

    /**
     * Key Vault secret name.
     */
    @JsonProperty(value = "properties.keyVaultSecretName")
    private String keyVaultSecretName;

    /**
     * Status of the Key Vault secret. Possible values include: 'Initialized',
     * 'WaitingOnCertificateOrder', 'Succeeded', 'CertificateOrderFailed',
     * 'OperationNotPermittedOnKeyVault',
     * 'AzureServiceUnauthorizedToAccessKeyVault', 'KeyVaultDoesNotExist',
     * 'KeyVaultSecretDoesNotExist', 'UnknownError', 'Unknown'.
     */
    @JsonProperty(value = "properties.provisioningState")
    private KeyVaultSecretStatus provisioningState;

    /**
     * Get the keyVaultId value.
     *
     * @return the keyVaultId value
     */
    public String keyVaultId() {
        return this.keyVaultId;
    }

    /**
     * Set the keyVaultId value.
     *
     * @param keyVaultId the keyVaultId value to set
     * @return the CertificateOrderCertificateInner object itself.
     */
    public CertificateOrderCertificateInner withKeyVaultId(String keyVaultId) {
        this.keyVaultId = keyVaultId;
        return this;
    }

    /**
     * Get the keyVaultSecretName value.
     *
     * @return the keyVaultSecretName value
     */
    public String keyVaultSecretName() {
        return this.keyVaultSecretName;
    }

    /**
     * Set the keyVaultSecretName value.
     *
     * @param keyVaultSecretName the keyVaultSecretName value to set
     * @return the CertificateOrderCertificateInner object itself.
     */
    public CertificateOrderCertificateInner withKeyVaultSecretName(String keyVaultSecretName) {
        this.keyVaultSecretName = keyVaultSecretName;
        return this;
    }

    /**
     * Get the provisioningState value.
     *
     * @return the provisioningState value
     */
    public KeyVaultSecretStatus provisioningState() {
        return this.provisioningState;
    }

    /**
     * Set the provisioningState value.
     *
     * @param provisioningState the provisioningState value to set
     * @return the CertificateOrderCertificateInner object itself.
     */
    public CertificateOrderCertificateInner withProvisioningState(KeyVaultSecretStatus provisioningState) {
        this.provisioningState = provisioningState;
        return this;
    }

}
