/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;


/**
 * Describes a single certificate reference in a Key Vault, and where the
 * certificate should reside on the VM.
 */
public class VaultCertificate {
    /**
     * Gets or sets the URL referencing a secret in a Key Vault which contains
     * a properly formatted certificate.
     */
    private String certificateUrl;

    /**
     * Gets or sets the Certificate store in LocalMachine to add the
     * certificate to on Windows, leave empty on Linux.
     */
    private String certificateStore;

    /**
     * Get the certificateUrl value.
     *
     * @return the certificateUrl value
     */
    public String certificateUrl() {
        return this.certificateUrl;
    }

    /**
     * Set the certificateUrl value.
     *
     * @param certificateUrl the certificateUrl value to set
     * @return the VaultCertificate object itself.
     */
    public VaultCertificate withCertificateUrl(String certificateUrl) {
        this.certificateUrl = certificateUrl;
        return this;
    }

    /**
     * Get the certificateStore value.
     *
     * @return the certificateStore value
     */
    public String certificateStore() {
        return this.certificateStore;
    }

    /**
     * Set the certificateStore value.
     *
     * @param certificateStore the certificateStore value to set
     * @return the VaultCertificate object itself.
     */
    public VaultCertificate withCertificateStore(String certificateStore) {
        this.certificateStore = certificateStore;
        return this;
    }

}
