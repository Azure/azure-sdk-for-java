// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation.implementation.models;

import com.azure.core.annotation.Immutable;
import com.azure.security.attestation.models.AttestationOptions;


/** Attestation request for OpenEnclave reports  generated from within Intel SGX enclaves. */
@Immutable
public final class AttestationOptionsImpl {
    private final AttestationOptions options;

    public AttestationOptionsImpl(AttestationOptions options) {
        this.options = options;
    }

    /**
     * Returns an internal type from a public type.
     * @return implementation type.
     */
    public AttestOpenEnclaveRequest getInternalAttestOpenEnclaveRequest() {
        return new AttestOpenEnclaveRequest()
            .setDraftPolicyForAttestation(options.getDraftPolicyForAttestation())
            .setRuntimeData(options.getRunTimeData() != null ? new RuntimeData()
                .setData(options.getRunTimeData().getData().toBytes())
                .setDataType(DataType.fromString(options.getRunTimeData().getInterpretation().toString())) : null)
            .setInitTimeData(options.getInitTimeData() != null ? new InitTimeData()
                .setData(options.getInitTimeData().getData().toBytes())
                .setDataType(DataType.fromString(options.getInitTimeData().getInterpretation().toString())) : null)
            .setReport(options.getEvidence().toBytes());
    }

    /**
     * Returns an internal type from a public type.
     * @return implementation type.
     */
    public AttestSgxEnclaveRequest getInternalAttestSgxRequest() {
        return new AttestSgxEnclaveRequest()
            .setDraftPolicyForAttestation(options.getDraftPolicyForAttestation())
            .setRuntimeData(options.getRunTimeData() != null ? new RuntimeData()
                .setData(options.getRunTimeData().getData().toBytes())
                .setDataType(DataType.fromString(options.getRunTimeData().getInterpretation().toString())) : null)
            .setInitTimeData(options.getInitTimeData() != null ? new InitTimeData()
                .setData(options.getInitTimeData().getData().toBytes())
                .setDataType(DataType.fromString(options.getInitTimeData().getInterpretation().toString())) : null)
            .setQuote(options.getEvidence().toBytes());
    }
}
