// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties;

/**
 * Azure Key Vault JCA certificate paths properties.
 *
 * @since 5.21.0
 */
public class AzureKeyVaultJcaCertificatePathsProperties {

    /**
     * The path to put custom certificates
     */
    private String custom;

    /**
     * The path to put well-known certificates
     */
    private String wellKnown;

    /**
     * Gets the path to put custom certificates.
     *
     * @return the path to put custom certificates
     */
    public String getCustom() {
        return custom;
    }

    /**
     * Gets the path to put well-known certificates.
     *
     * @return the path to put well-known certificates
     */
    public String getWellKnown() {
        return wellKnown;
    }

    /**
     * Sets the path to put custom certificates.
     *
     * @param custom the path to put custom certificates
     */
    public void setCustom(String custom) {
        this.custom = custom;
    }

    /**
     * Sets the path to put well-known certificates.
     *
     * @param wellKnown the path to put well-known certificates
     */
    public void setWellKnown(String wellKnown) {
        this.wellKnown = wellKnown;
    }

}
