// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.implementation;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The certificate issuer set parameters.
 */
@Fluent
public final class CertificateIssuerSetParameters {
    /**
     * The issuer provider.
     */
    @JsonProperty(value = "provider", required = true)
    private String provider;

    /**
     * The credentials to be used for the issuer.
     */
    @JsonProperty(value = "credentials")
    private IssuerCredentials credentials;

    /**
     * Details of the organization as provided to the issuer.
     */
    @JsonProperty(value = "org_details")
    private OrganizationDetails organizationDetails;

    /**
     * Attributes of the issuer object.
     */
    @JsonProperty(value = "attributes")
    private IssuerAttributes attributes;

    /**
     * Get the provider value.
     *
     * @return the provider value
     */
    public String provider() {
        return this.provider;
    }

    /**
     * Set the provider value.
     *
     * @param provider the provider value to set
     * @return the CertificateIssuerSetParameters object itself.
     */
    public CertificateIssuerSetParameters provider(String provider) {
        this.provider = provider;
        return this;
    }

    /**
     * Get the credentials value.
     *
     * @return the credentials value
     */
    public IssuerCredentials credentials() {
        return this.credentials;
    }

    /**
     * Set the credentials value.
     *
     * @param credentials the credentials value to set
     * @return the CertificateIssuerSetParameters object itself.
     */
    public CertificateIssuerSetParameters credentials(IssuerCredentials credentials) {
        this.credentials = credentials;
        return this;
    }

    /**
     * Get the organizationDetails value.
     *
     * @return the organizationDetails value
     */
    public OrganizationDetails organizationDetails() {
        return this.organizationDetails;
    }

    /**
     * Set the organizationDetails value.
     *
     * @param organizationDetails the organizationDetails value to set
     * @return the CertificateIssuerSetParameters object itself.
     */
    public CertificateIssuerSetParameters organizationDetails(OrganizationDetails organizationDetails) {
        this.organizationDetails = organizationDetails;
        return this;
    }

    /**
     * Get the attributes value.
     *
     * @return the attributes value
     */
    public IssuerAttributes attributes() {
        return this.attributes;
    }

    /**
     * Set the attributes value.
     *
     * @param attributes the attributes value to set
     * @return the CertificateIssuerSetParameters object itself.
     */
    public CertificateIssuerSetParameters attributes(IssuerAttributes attributes) {
        this.attributes = attributes;
        return this;
    }

}
