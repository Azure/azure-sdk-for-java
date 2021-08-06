// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.security.attestation.models.AttestSgxEnclaveOptions;

import java.util.Objects;

/** Attestation request for Intel SGX enclaves. */
@Fluent
public final class AttestSgxEnclaveOptionsImpl implements AttestSgxEnclaveOptions {
    /*
     * Quote of the enclave to be attested
     */
    private byte[] quote;

    /*
     * Runtime data provided by the enclave at the time of quote generation.
     * The MAA will verify that the first 32 bytes of the report_data field of
     * the quote contains the SHA256 hash of the decoded "data" field of the
     * runtime data.
     */
    private byte[] runTimeData;
    private DataType runTimeDataType;

    /*
     * Initialization data provided when the enclave is created. MAA will
     * verify that the init data was known to the enclave. Note that
     * InitTimeData is invalid for CoffeeLake processors.
     */
    private byte[] initTimeData;
    private DataType initTimeDataType;

    /*
     * Attest against the provided draft policy. Note that the resulting token
     * cannot be validated.
     */
    private String draftPolicyForAttestation;

    /**
     * Get the quote property: Quote of the enclave to be attested.
     *
     * @return the quote value.
     */
    @Override public byte[] getQuote() {
        return CoreUtils.clone(this.quote);
    }

    /**
     * Set the quote property: Quote of the enclave to be attested.
     *
     * @param quote the quote value to set.
     * @return the AttestSgxEnclaveRequest object itself.
     */
    public AttestSgxEnclaveOptions setQuote(byte[] quote) {
        this.quote = CoreUtils.clone(quote);
        return this;
    }

    /**
     * Get the runtimeData property: Runtime data provided by the enclave at the time of quote generation. The MAA will
     * verify that the first 32 bytes of the report_data field of the quote contains the SHA256 hash of the decoded
     * "data" field of the runtime data.
     *
     * @return the runtimeData value.
     */
    @Override public byte[] getRunTimeData() {
        if (runTimeDataType.equals(DataType.BINARY)) {
            return CoreUtils.clone(this.runTimeData);
        }
        throw new IllegalStateException("Cannot return RunTimeData if RunTimeJson has been set");
    }

    @Override public byte[] getRunTimeJson() {
        if (runTimeDataType.equals(DataType.JSON)) {
            return CoreUtils.clone(this.runTimeData);
        }
        throw new IllegalStateException("Cannot return RunTimeJson if RunTimeData has been set");
    }
    /**
     * Set the runtimeData property: Runtime data provided by the enclave at the time of quote generation. The MAA will
     * verify that the first 32 bytes of the report_data field of the quote contains the SHA256 hash of the decoded
     * "data" field of the runtime data.
     *
     * @param runtimeData the runtimeData value to set.
     * @return the AttestSgxEnclaveRequest object itself.
     */
    @Override public AttestSgxEnclaveOptions setRunTimeData(byte[] runtimeData) {
        this.runTimeData = runtimeData.clone();
        this.runTimeDataType = DataType.BINARY;
        return this;
    }

    /**
     * Set the runtimeData property: Runtime data provided by the enclave at the time of quote generation. The MAA will
     * verify that the first 32 bytes of the report_data field of the quote contains the SHA256 hash of the decoded
     * "data" field of the runtime data.
     *
     * @param runtimeData the runtimeData value to set.
     * @return the AttestSgxEnclaveRequest object itself.
     */
    @Override public AttestSgxEnclaveOptions setRunTimeJson(byte[] runtimeData) {
        this.runTimeData = CoreUtils.clone(runtimeData);
        this.runTimeDataType = DataType.JSON;
        return this;
    }


    /**
     * Get the initTimeData property as Binary: Initialization data provided when the enclave is created. MAA will verify that the
     * init data was known to the enclave. Note that InitTimeData is invalid for CoffeeLake processors.
     *
     * @return the initTimeData value.
     */
    @Override public byte[] getInitTimeData() {
        if (initTimeDataType.equals(DataType.BINARY)) {
            return CoreUtils.clone(this.initTimeData);
        }
        throw new IllegalStateException("Cannot return InitTimeData if InitTimeJson has been set");
    }

    /**
     * Set the initTimeData property: Initialization data provided when the enclave is created. MAA will verify that the
     * init data was known to the enclave. Note that InitTimeData is invalid for CoffeeLake processors.
     *
     * @param initTimeData the initTimeData value to set.
     * @return the AttestSgxEnclaveRequest object itself.
     */
    @Override public AttestSgxEnclaveOptions setInitTimeData(byte[] initTimeData) {
        this.initTimeData = CoreUtils.clone(initTimeData);
        this.initTimeDataType = DataType.BINARY;
        return this;
    }

    /**
     * Get the initTimeData property as Binary: Initialization data provided when the enclave is created. MAA will verify that the
     * init data was known to the enclave. Note that InitTimeData is invalid for CoffeeLake processors.
     *
     * @return the initTimeData value.
     */
    @Override public byte[] getInitTimeJson() {
        if (initTimeDataType.equals(DataType.JSON)) {
            return CoreUtils.clone(this.initTimeData);
        }
        throw new IllegalStateException("Cannot return InitTimeJson if InitTimeData has been set");
    }

    /**
     * Set the initTimeData property as JSON: Initialization data provided when the enclave is created. MAA will verify that the
     * init data was known to the enclave. Note that InitTimeData is invalid for CoffeeLake processors.
     *
     * @param initTimeData the initTimeData value to set.
     * @return the AttestSgxEnclaveRequest object itself.
     */
    @Override public AttestSgxEnclaveOptions setInitTimeJson(byte[] initTimeData) {
        this.initTimeData = CoreUtils.clone(initTimeData);
        this.initTimeDataType = DataType.JSON;
        return this;
    }

    /**
     * Get the draftPolicyForAttestation property: Attest against the provided draft policy. Note that the resulting
     * token cannot be validated.
     *
     * @return the draftPolicyForAttestation value.
     */
    @Override public String getDraftPolicyForAttestation() {
        return this.draftPolicyForAttestation;
    }

    /**
     * Set the draftPolicyForAttestation property: Attest against the provided draft policy. Note that the resulting
     * token cannot be validated.
     *
     * @param draftPolicyForAttestation the draftPolicyForAttestation value to set.
     * @return the AttestSgxEnclaveRequest object itself.
     */
    @Override public AttestSgxEnclaveOptions setDraftPolicyForAttestation(String draftPolicyForAttestation) {
        this.draftPolicyForAttestation = draftPolicyForAttestation;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override public void validate() {
        Objects.requireNonNull(quote);
    }

    /**
     * Convert the public AttestSgxEnclaveOptions to an internal AttestSgxEnclaveRequest.
     * @return implementation type.
     */
    public AttestSgxEnclaveRequest getInternalAttestRequest() {
        return new com.azure.security.attestation.implementation.models.AttestSgxEnclaveRequest()
            .setDraftPolicyForAttestation(draftPolicyForAttestation)
            .setRuntimeData(runTimeData != null ? new RuntimeData()
                .setData(runTimeData)
                .setDataType(runTimeDataType) : null)
            .setInitTimeData(initTimeData != null ? new InitTimeData()
                .setData(initTimeData)
                .setDataType(initTimeDataType) : null)
            .setQuote(quote);
    }


}
