// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation.models;

import com.azure.core.annotation.Fluent;

import java.security.cert.X509Certificate;
import java.util.Objects;

/**
 * <p>
 * AttestationPolicySetOptions represent the parameters sent to the
 * {@link com.azure.security.attestation.AttestationAdministrationClient#addPolicyManagementCertificate(PolicyManagementCertificateOptions)}
 * or {@link com.azure.security.attestation.AttestationAdministrationClient#deletePolicyManagementCertificate(PolicyManagementCertificateOptions)} API.
 * </p><p>
 * Each {@link AttestationPolicySetOptions} object expresses the options to verify the response
 * from the attestation service.
 * </p>
 */
@Fluent
public final class PolicyManagementCertificateOptions {

    private AttestationTokenValidationOptions validationOptions;
    private final X509Certificate certificate;
    private final AttestationSigningKey signer;

    /**
     * Creates a new {@link PolicyManagementCertificateOptions}.
     *
     * @param certificate Specifies the X.509 certificate to add or remove to the set of policy management certificates.
     * @param signingKey Specifies the signing key which will be used to sign the request for the attestation service.
     */
    public PolicyManagementCertificateOptions(X509Certificate certificate, AttestationSigningKey signingKey) {
        Objects.requireNonNull(certificate);
        Objects.requireNonNull(signingKey);
        this.certificate = certificate;
        this.signer = signingKey;
    }

    /**
     * Sets the options used to validate attestation tokens returned from the service.
     * @param validationOptions Token Validation options to be used to enhance the validations
     *                          already performed by the SDK.
     * @return this {@link AttestationPolicySetOptions} object.
     */
    public PolicyManagementCertificateOptions
        setValidationOptions(AttestationTokenValidationOptions validationOptions) {
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
     * Gets the attestation policy which will be used to generate a policy set request.
     * @return Attestation Policy Token associated with this request.
     */
    public X509Certificate getCertificate() {
        return this.certificate;
    }

    /**
     * Gets the attestation signer which will be used to sign a policy set request.
     * @return Attestation Signer associated with this request.
     */
    public AttestationSigningKey getAttestationSigner() {
        return this.signer;
    }
}
