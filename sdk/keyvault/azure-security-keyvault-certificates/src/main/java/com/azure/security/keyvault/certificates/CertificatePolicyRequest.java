// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.LifetimeAction;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Management policy for a certificate.
 */
class CertificatePolicyRequest {

    CertificatePolicyRequest(CertificatePolicy certificatePolicy) {
        this.keyProperties =  new KeyProperties(certificatePolicy);
        this.x509CertificateProperties = new X509CertificateProperties(certificatePolicy);
        this.secretProperties = certificatePolicy.getContentType() != null ? new SecretProperties(certificatePolicy.getContentType().toString()) : null;
        this.issuerParameters = new IssuerParameters(certificatePolicy);
        this.lifetimeActionRequests = certificatePolicy.getLifetimeActions() != null ? parseLifeTimeActions(certificatePolicy.getLifetimeActions()) : null;
        this.attributes = new CertificateRequestAttributes().enabled(certificatePolicy.isEnabled());
    }

    private List<LifetimeActionRequest> parseLifeTimeActions(List<LifetimeAction> input) {
        List<LifetimeActionRequest> output = new ArrayList<>();
        for (LifetimeAction action : input) {
            output.add(new LifetimeActionRequest(action));
        }
        return output;
    }

    /**
     * The certificate id.
     */
    @JsonProperty(value = "id", access = JsonProperty.Access.WRITE_ONLY)
    private String id;

    /**
     * Properties of the key backing a certificate.
     */
    @JsonProperty(value = "key_props")
    private KeyProperties keyProperties;

    /**
     * Properties of the secret backing a certificate.
     */
    @JsonProperty(value = "secret_props")
    private SecretProperties secretProperties;

    /**
     * Properties of the X509 component of a certificate.
     */
    @JsonProperty(value = "x509_props")
    private X509CertificateProperties x509CertificateProperties;

    /**
     * Actions that will be performed by Key Vault over the lifetime of a
     * certificate.
     */
    @JsonProperty(value = "lifetime_actions")
    private List<LifetimeActionRequest> lifetimeActionRequests;

    /**
     * Parameters for the issuer of the X509 component of a certificate.
     */
    @JsonProperty(value = "issuer")
    private IssuerParameters issuerParameters;

    /**
     * The certificate attributes.
     */
    @JsonProperty(value = "attributes")
    private CertificateRequestAttributes attributes;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * Get the keyProperties value.
     *
     * @return the keyProperties value
     */
    KeyProperties keyProperties() {
        return this.keyProperties;
    }

    /**
     * Set the keyProperties value.
     *
     * @param keyProperties the keyProperties value to set
     * @return the CertificatePolicyRequest object itself.
     */
    CertificatePolicyRequest keyProperties(KeyProperties keyProperties) {
        this.keyProperties = keyProperties;
        return this;
    }

    /**
     * Get the secretProperties value.
     *
     * @return the secretProperties value
     */
    SecretProperties secretProperties() {
        return this.secretProperties;
    }

    /**
     * Set the secretProperties value.
     *
     * @param secretProperties the secretProperties value to set
     * @return the CertificatePolicyRequest object itself.
     */
    CertificatePolicyRequest secretProperties(SecretProperties secretProperties) {
        this.secretProperties = secretProperties;
        return this;
    }

    /**
     * Get the x509CertificateProperties value.
     *
     * @return the x509CertificateProperties value
     */
    X509CertificateProperties x509CertificateProperties() {
        return this.x509CertificateProperties;
    }

    /**
     * Set the x509CertificateProperties value.
     *
     * @param x509CertificateProperties the x509CertificateProperties value to set
     * @return the CertificatePolicyRequest object itself.
     */
    CertificatePolicyRequest x509CertificateProperties(X509CertificateProperties x509CertificateProperties) {
        this.x509CertificateProperties = x509CertificateProperties;
        return this;
    }

    /**
     * Get the lifetimeActionRequests value.
     *
     * @return the lifetimeActionRequests value
     */
    List<LifetimeActionRequest> lifetimeActions() {
        return this.lifetimeActionRequests;
    }

    /**
     * Set the lifetimeActionRequests value.
     *
     * @param lifetimeActionRequests the lifetimeActionRequests value to set
     * @return the CertificatePolicyRequest object itself.
     */
    CertificatePolicyRequest lifetimeActions(List<LifetimeActionRequest> lifetimeActionRequests) {
        this.lifetimeActionRequests = lifetimeActionRequests;
        return this;
    }

    /**
     * Get the issuerParameters value.
     *
     * @return the issuerParameters value
     */
    IssuerParameters issuerParameters() {
        return this.issuerParameters;
    }

    /**
     * Set the issuerParameters value.
     *
     * @param issuerParameters the issuerParameters value to set
     * @return the CertificatePolicyRequest object itself.
     */
    CertificatePolicyRequest issuerParameters(IssuerParameters issuerParameters) {
        this.issuerParameters = issuerParameters;
        return this;
    }

    /**
     * Get the attributes value.
     *
     * @return the attributes value
     */
    CertificateRequestAttributes attributes() {
        return this.attributes;
    }

    /**
     * Set the attributes value.
     *
     * @param attributes the attributes value to set
     * @return the CertificatePolicyRequest object itself.
     */
    CertificatePolicyRequest attributes(CertificateRequestAttributes attributes) {
        this.attributes = attributes;
        return this;
    }
}
