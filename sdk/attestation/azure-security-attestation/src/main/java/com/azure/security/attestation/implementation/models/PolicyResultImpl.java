// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation.implementation.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;
import com.azure.security.attestation.models.AttestationSigner;
import com.azure.security.attestation.models.PolicyModification;

/** The result of a policy certificate modification. */
@Immutable
public final class PolicyResultImpl implements com.azure.security.attestation.models.PolicyResult {

    /*
     * The result of the operation
     */
    private final PolicyModification policyResolution;

    /*
     * The SHA256 hash of the policy object modified
     */
    private final BinaryData policyTokenHash;

    /*
     * The certificate used to sign the policy object, if specified
     */
    private final AttestationSigner policySigner;

    /**
     * Get the policyResolution property: The result of the operation.
     *
     * @return the policyResolution value.
     */
    public PolicyModification getPolicyResolution() {
        return this.policyResolution;
    }

    /**
     * Get the policyTokenHash property: The SHA256 hash of the policy object modified.
     *
     * @return the policyTokenHash value.
     */
    public BinaryData getPolicyTokenHash() {
        return this.policyTokenHash;
    }

    /**
     * Get the policySigner property: The certificate used to sign the policy object, if specified.
     *
     * @return the policySigner value.
     */
    public AttestationSigner getPolicySigner() {
        return this.policySigner;
    }

    private PolicyResultImpl(JsonWebKey signer, PolicyModification resolution, byte[] policyTokenHash) {
        if (signer != null) {
            this.policySigner = AttestationSignerImpl.fromJsonWebKey(signer);
        } else {
            this.policySigner = null;
        }
        if (policyTokenHash != null) {
            this.policyTokenHash = BinaryData.fromBytes(policyTokenHash);
        } else {
            this.policyTokenHash = null;
        }
        this.policyResolution = resolution;
    }

    /**
     * Return the public PolicyResult object from the generated PolicyResult type.
     * @param generated Generated PolicyResult type.
     * @return a PolicyResult created from the generated PolicyResult object.
     */
    public static com.azure.security.attestation.models.PolicyResult fromGenerated(PolicyResult generated) {
        return new com.azure.security.attestation.implementation.models.PolicyResultImpl(generated.getPolicySigner(), generated.getPolicyResolution(), generated.getPolicyTokenHash());
    }
}
