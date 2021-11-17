// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation.models;

import com.azure.core.annotation.Fluent;

import java.security.cert.X509Certificate;

/**
 * <p>
 * AttestationPolicySetOptions represent the parameters sent to the
 * {@link com.azure.security.attestation.AttestationAdministrationClient#addPolicyManagementCertificate(PolicyManagementCertificateOptions)}
 * or {@link com.azure.security.attestation.AttestationAdministrationClient#removePolicyManagementCertificate(PolicyManagementCertificateOptions)} API.
 * </p><p>
 * Each {@link AttestationPolicySetOptions} object expresses the options to verify the response
 * from the attestation service.
 * </p>
 */
@Fluent
public final class PolicyManagementCertificateOptions {

    private AttestationTokenValidationOptions validationOptions;
    private X509Certificate certificate;
    private AttestationSigningKey signer;

    /**
     * Sets the options used to validate attestation tokens returned from the service.
     * @param validationOptions Token Validation options to be used to enhance the validations
     *                          already performed by the SDK.
     * @return this {@link AttestationPolicySetOptions} object.
     */
    public PolicyManagementCertificateOptions setValidationOptions(AttestationTokenValidationOptions validationOptions) {
        this.validationOptions = validationOptions;
        return this;
    }

    /**
     * Returns the options used for token validation.
     * @return attestation token validation options.
     */
    public AttestationTokenValidationOptions getValidationOptions() {
        return validationOptions;
    }

    /**
     * Gets the attestation policy which will be used to generate an policy set request.
     * @return Attestation Policy Token associated with this request.
     */
    public X509Certificate getCertificate() {
        return this.certificate;
    }

    /**
     * Sets the attestation policy which will be used to generate an policy set request.
     * @param certificate Certificate to add or remove from the Policy Management Certificates.
     * @return This {@link AttestationPolicySetOptions}.
     */
    public PolicyManagementCertificateOptions setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
        return this;
    }

    /**
     * Gets the attestation signer which will be used to sign a policy set request.
     * @return Attestation Signer associated with this request.
     */
    public AttestationSigningKey getAttestationSigner() {
        return this.signer;
    }

    /**
     * Sets the attestation signer which will be used to generate a policy set request.
     * @param signer Signer for the set Policy request.
     * @return This {@link AttestationPolicySetOptions}.
     */
    public PolicyManagementCertificateOptions setAttestationSigner(AttestationSigningKey signer) {
        this.signer = signer;
        return this;
    }

}
