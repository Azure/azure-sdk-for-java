/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import com.microsoft.azure.SubResource;
import java.util.List;

/**
 * Describes a set of certificates which are all in the same Key Vault.
 */
public class VaultSecretGroup {
    /**
     * Gets or sets the Relative URL of the Key Vault containing all of the
     * certificates in VaultCertificates.
     */
    private SubResource sourceVault;

    /**
     * Gets or sets the list of key vault references in SourceVault which
     * contain certificates.
     */
    private List<VaultCertificate> vaultCertificates;

    /**
     * Get the sourceVault value.
     *
     * @return the sourceVault value
     */
    public SubResource sourceVault() {
        return this.sourceVault;
    }

    /**
     * Set the sourceVault value.
     *
     * @param sourceVault the sourceVault value to set
     * @return the VaultSecretGroup object itself.
     */
    public VaultSecretGroup withSourceVault(SubResource sourceVault) {
        this.sourceVault = sourceVault;
        return this;
    }

    /**
     * Get the vaultCertificates value.
     *
     * @return the vaultCertificates value
     */
    public List<VaultCertificate> vaultCertificates() {
        return this.vaultCertificates;
    }

    /**
     * Set the vaultCertificates value.
     *
     * @param vaultCertificates the vaultCertificates value to set
     * @return the VaultSecretGroup object itself.
     */
    public VaultSecretGroup withVaultCertificates(List<VaultCertificate> vaultCertificates) {
        this.vaultCertificates = vaultCertificates;
        return this;
    }

}
