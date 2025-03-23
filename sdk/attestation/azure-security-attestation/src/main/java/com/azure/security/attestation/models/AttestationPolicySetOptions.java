// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.Context;

/**
 * <p>
 * AttestationPolicySetOptions represent the parameters sent to the
 * {@link com.azure.security.attestation.AttestationAdministrationClient#getAttestationPolicy(AttestationType)}
 * or {@link com.azure.security.attestation.AttestationAdministrationClient#getAttestationPolicyWithResponse(AttestationType, AttestationTokenValidationOptions, Context)} API.
 * </p><p>
 * Each {@link AttestationPolicySetOptions} object expresses the options to verify the response
 * from the attestation service.
 * </p>
 */
@Fluent
public final class AttestationPolicySetOptions {

    private AttestationTokenValidationOptions validationOptions;
    private String policy;
    private AttestationSigningKey signer;

    /**
     * Sets the options used to validate attestation tokens returned from the service.
     * @param validationOptions Token Validation options to be used to enhance the validations
     *                          already performed by the SDK.
     * @return this {@link AttestationPolicySetOptions} object.
     */
    public AttestationPolicySetOptions setValidationOptions(AttestationTokenValidationOptions validationOptions) {
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
    public String getAttestationPolicy() {
        return this.policy;
    }

    /**
     * Sets the attestation policy which will be used to generate a policy set request.
     * @param policy Attestation Token to be set.
     * @return This {@link AttestationPolicySetOptions}.
     */
    public AttestationPolicySetOptions setAttestationPolicy(String policy) {
        this.policy = policy;
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
    public AttestationPolicySetOptions setAttestationSigner(AttestationSigningKey signer) {
        this.signer = signer;
        return this;
    }

}
