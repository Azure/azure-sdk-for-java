// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parameters for the issuer of the X509 component of a certificate.
 */
class IssuerParameters {

    IssuerParameters() {

    }

    IssuerParameters(CertificatePolicy certificatePolicy) {
        this.name = certificatePolicy.getIssuerName();
        this.certificateType = certificatePolicy.getCertificateType();
        this.certificateTransparency = certificatePolicy.isCertificateTransparent();
    }
    /**
     * Name of the referenced issuer object or reserved names; for example,
     * 'Self' or 'Unknown'.
     */
    @JsonProperty(value = "name")
    private String name;

    /**
     * Type of certificate to be requested from the issuer provider.
     */
    @JsonProperty(value = "cty")
    private String certificateType;

    /**
     * Indicates if the certificates generated under this policy should be
     * published to certificate transparency logs.
     */
    @JsonProperty(value = "cert_transparency")
    private Boolean certificateTransparency;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the IssuerParameters object itself.
     */
    public IssuerParameters name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the certificateType value.
     *
     * @return the certificateType value
     */
    public String certificateType() {
        return this.certificateType;
    }

    /**
     * Set the certificateType value.
     *
     * @param certificateType the certificateType value to set
     * @return the IssuerParameters object itself.
     */
    public IssuerParameters certificateType(String certificateType) {
        this.certificateType = certificateType;
        return this;
    }

    /**
     * Get the certificateTransparency value.
     *
     * @return the certificateTransparency value
     */
    public Boolean certificateTransparency() {
        return this.certificateTransparency;
    }

    /**
     * Set the certificateTransparency value.
     *
     * @param certificateTransparency the certificateTransparency value to set
     * @return the IssuerParameters object itself.
     */
    public IssuerParameters certificateTransparency(Boolean certificateTransparency) {
        this.certificateTransparency = certificateTransparency;
        return this;
    }
}
