package com.azure.security.attestation.models;

import com.azure.core.util.BinaryData;

public class TpmAttestationResult {
    BinaryData tpmResult;

    public TpmAttestationResult(String result) {
        this.tpmResult = BinaryData.fromString(result);
    }

    public String getString() {
        return tpmResult.toString();
    }
}
