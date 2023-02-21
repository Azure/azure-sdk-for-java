package com.azure.security.attestation.models;

import com.azure.core.util.BinaryData;

public class TpmAttestationResult {
    private final BinaryData tpmResult;

    public TpmAttestationResult(BinaryData result) {
        this.tpmResult = result;
    }

    public BinaryData getTpmResult() {
        return tpmResult;
    }
}
