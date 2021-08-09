// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.security.attestation.models.AttestationOptions;

import java.util.Objects;

/** Attestation request for OpenEnclave reports  generated from within Intel SGX enclaves. */
@Fluent
public final class AttestationOptionsImpl implements AttestationOptions {
    /*
     * OpenEnclave report from the enclave to be attested
     */
    private byte[] evidence;

    /*
     * Runtime data provided by the enclave at the time of report generation.
     * The MAA will verify that the first 32 bytes of the report_data field of
     * the quote contains the SHA256 hash of the decoded "data" field of the
     * runtime data.
     */
    private byte[] runtimeData;
    private DataType runTimeDataType;
    boolean runTimeDataTypeSet;

    /*
     * Base64Url encoded "InitTime data". The MAA will verify that the init
     * data was known to the enclave. Note that InitTimeData is invalid for
     * CoffeeLake processors.
     */
    private byte[] initTimeData;
    private DataType initTimeDataType;
    boolean initTimeDataTypeSet;

    /*
     * Attest against the provided draft policy. Note that the resulting token
     * cannot be validated.
     */
    private String draftPolicyForAttestation;

    /**
     * Set the report property: OpenEnclave report from the enclave to be attested.
     *
     * @param report the report value to set.
     * @return the AttestOpenEnclaveRequest object itself.
     */
    public AttestationOptions setEvidence(byte[] report) {
        this.evidence = CoreUtils.clone(report);
        return this;
    }

    /**
     * Set the RunTimeData property: Runtime data provided by the enclave at the time of report generation. The MAA will
     * verify that the first 32 bytes of the report_data field of the quote contains the SHA256 hash of the decoded
     * "data" field of the runtime data.
     *
     * @param runtimeData the runtimeData value to set.
     * @return the AttestOpenEnclaveRequest object itself.
     */
    @Override public AttestationOptions setRunTimeData(byte[] runtimeData) {
        this.runtimeData = CoreUtils.clone(runtimeData);
        return this;
    }

    @Override
    public byte[] getRunTimeData() {
        return CoreUtils.clone(runtimeData);
    }

    @Override
    public AttestationOptions interpretRunTimeDataAsBinary() {
        this.runTimeDataType = DataType.BINARY;
        this.runTimeDataTypeSet = true;
        return this;
    }

    @Override
    public AttestationOptions interpretRunTimeDataAsJson() {
        this.runTimeDataType = DataType.JSON;
        this.runTimeDataTypeSet = true;
        return this;
    }

    /**
     * Set the initTimeData property: Base64Url encoded "InitTime data". The MAA will verify that the init data was
     * known to the enclave. Note that InitTimeData is invalid for CoffeeLake processors.
     *
     * @param initTimeData the initTimeData value to set.
     * @return the AttestOpenEnclaveRequest object itself.
     */
    @Override public AttestationOptions setInitTimeData(byte[] initTimeData) {
        this.initTimeData = CoreUtils.clone(initTimeData);
        return this;
    }

    @Override
    public byte[] getInitTimeData() {
        return CoreUtils.clone(initTimeData);
    }

    @Override
    public AttestationOptions interpretInitTimeDataAsBinary() {
        this.initTimeDataType = DataType.BINARY;
        this.initTimeDataTypeSet = true;
        return this;
    }

    @Override
    public AttestationOptions interpretInitTimeDataAsJson() {
        this.initTimeDataType = DataType.JSON;
        this.initTimeDataTypeSet = true;
        return this;
    }

    /**
     * Set the draftPolicyForAttestation property: Attest against the provided draft policy. Note that the resulting
     * token cannot be validated.
     *
     * @param draftPolicyForAttestation the draftPolicyForAttestation value to set.
     * @return the AttestOpenEnclaveRequest object itself.
     */
    @Override public AttestationOptions setDraftPolicyForAttestation(String draftPolicyForAttestation) {
        this.draftPolicyForAttestation = draftPolicyForAttestation;
        return this;
    }

    /**
     * Returns the draft policy for attestation if one has been set.
     * @return The draft policy for attestation if one was set.
     */
    @Override
    public String getDraftPolicyForAttestation() {
        return draftPolicyForAttestation;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override public void validate() {
        Objects.requireNonNull(this.evidence);
        if (this.runtimeData != null) {
            if (!this.runTimeDataTypeSet) {
                throw new IllegalArgumentException("RunTime Data is set, but interpretRunTimeDataAsBinary or interpretRunTimeDataAsJson was not called.");
            }
        }
        if (this.initTimeData != null) {
            if (!this.initTimeDataTypeSet) {
                throw new IllegalArgumentException("InitTime Data is set, but interpretInitTimeDataAsBinary or interpretInitTimeDataAsJson was not called.");
            }
        }
    }

    /**
     * Returns an internal type from a public type.
     * @return implementation type.
     */
    public AttestOpenEnclaveRequest getInternalAttestOpenEnclaveRequest() {
        return new AttestOpenEnclaveRequest()
            .setDraftPolicyForAttestation(draftPolicyForAttestation)
            .setRuntimeData(runtimeData != null ? new RuntimeData()
                .setData(runtimeData)
                .setDataType(runTimeDataType) : null)
            .setInitTimeData(initTimeData != null ? new InitTimeData()
                .setData(initTimeData)
                .setDataType(initTimeDataType) : null)
            .setReport(evidence);
    }

    /**
     * Returns an internal type from a public type.
     * @return implementation type.
     */
    public AttestSgxEnclaveRequest getInternalAttestSgxRequest() {
        return new AttestSgxEnclaveRequest()
            .setDraftPolicyForAttestation(draftPolicyForAttestation)
            .setRuntimeData(runtimeData != null ? new RuntimeData()
                .setData(runtimeData)
                .setDataType(runTimeDataType) : null)
            .setInitTimeData(initTimeData != null ? new InitTimeData()
                .setData(initTimeData)
                .setDataType(initTimeDataType) : null)
            .setQuote(evidence);
    }
}
