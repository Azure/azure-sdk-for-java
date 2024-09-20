// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;

/**
 * The TpmAttestationResult class represents Tpm Attestation response data.
 *
 */

@Immutable
public class TpmAttestationResult {
    private final BinaryData tpmResult;

    /**
     * Creates a new TpmAttestationResult object for the Tpm Attestation result.
     * @param result - Result to be set.
     */
    public TpmAttestationResult(BinaryData result) {
        this.tpmResult = result;
    }

    /**
     * Returns the known Tpm Attestation result.
     *
     * @return the known Tpm Attestation Result.
     */
    public BinaryData getTpmResult() {
        return tpmResult;
    }
}
