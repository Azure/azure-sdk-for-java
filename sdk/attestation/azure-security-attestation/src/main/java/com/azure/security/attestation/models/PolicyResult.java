// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;

/**
 * Represents the result of a policy modification operation (setAttestationPolicy/resetAttestationPolicy).
 */
@Immutable
public interface PolicyResult {
    /**
     * Get the policyResolution property: The result of the operation.
     *
     * @return the policyResolution value.
     */
    PolicyModification getPolicyResolution();

    /**
     * Get the policyTokenHash property: The SHA256 hash of the policy object modified.
     *
     * @return the policyTokenHash value.
     */
    BinaryData getPolicyTokenHash();

    /**
     * Get the policySigner property: The certificate used to sign the policy object, if one was provided.
     *
     * @return the policySigner value.
     */
    AttestationSigner getPolicySigner();
}
