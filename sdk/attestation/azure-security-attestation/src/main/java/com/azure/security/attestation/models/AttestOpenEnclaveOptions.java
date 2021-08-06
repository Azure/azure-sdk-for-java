package com.azure.security.attestation.models;

import com.azure.security.attestation.implementation.models.AttestOpenEnclaveOptionsImpl;

/**
 * An AttestOpenEnclaveRequest represents the parameters sent to the {@link com.azure.security.attestation.AttestationClient#attestOpenEnclave(AttestOpenEnclaveOptions)}
 * API.
 */
public interface AttestOpenEnclaveOptions {
    /**
     * Creates a new AttestOpenEnclaveRequest object with the OpenEnclave report from the enclave to be attested.
     *
     * @param report the report value to set.
     * @return the AttestOpenEnclaveRequest object itself.
     */
    static AttestOpenEnclaveOptions fromReport(byte[] report) {
        return new AttestOpenEnclaveOptionsImpl().setReport(report);
    }

    /**
     * Set the runtimeData property: Runtime data provided by the enclave at the time of report generation. The MAA will
     * verify that the first 32 bytes of the report_data field of the quote contains the SHA256 hash of the decoded
     * "data" field of the runtime data.
     *
     * @param runtimeData the runtimeData value to set.
     * @return the AttestOpenEnclaveRequest object itself.
     */
    AttestOpenEnclaveOptions setRunTimeData(byte[] runtimeData);

    /**
     * Retrieves the RunTimeData property to be sent to the service.
     * @return The RunTimeData value set by {@link AttestOpenEnclaveOptions#setRunTimeData}
     * @throws IllegalStateException Thrown if the caller had called {@link AttestOpenEnclaveOptions#setRunTimeJson}.
     */
    byte[] getRunTimeData();

    /**
     * Set the runtimeData property: Runtime data provided by the enclave at the time of report generation. The MAA will
     * verify that the first 32 bytes of the report_data field of the quote contains the SHA256 hash of the decoded
     * "data" field of the runtime data.
     *
     * @param runtimeData the runtimeData value to set.
     * @return the AttestOpenEnclaveRequest object itself.
     */
    AttestOpenEnclaveOptions setRunTimeJson(byte[] runtimeData);

    /**
     * Retrieves the RunTimeJson property to be sent to the service.
     * @return The RunTimeJson value set by {@link AttestOpenEnclaveOptions#setRunTimeJson}
     * @throws IllegalStateException Thrown if the caller had called {@link AttestOpenEnclaveOptions#setRunTimeData}.
     */
    byte[] getRunTimeJson();

    /**
     * Set the initTimeData property: Base64Url encoded "InitTime data". The MAA will verify that the init data was
     * known to the enclave. Note that InitTimeData is invalid for CoffeeLake processors.
     *
     * @param initTimeData the initTimeData value to set.
     * @return the AttestOpenEnclaveRequest object itself.
     */
    AttestOpenEnclaveOptions setInitTimeData(byte[] initTimeData);

    /**
     * Retrieves the InitTimeData property to be sent to the service.
     * @return The InitTimeData value set by {@link AttestOpenEnclaveOptions#setInitTimeData}
     * @throws IllegalStateException Thrown if the caller had called {@link AttestOpenEnclaveOptions#setInitTimeJson}.
     */
    byte[] getInitTimeData();

    /**
     * Set the initTimeData property: Base64Url encoded "InitTime data". The MAA will verify that the init data was
     * known to the enclave. Note that InitTimeData is invalid for CoffeeLake processors.
     *
     * @param initTimeData the initTimeData value to set.
     * @return the AttestOpenEnclaveRequest object itself.
     */
    AttestOpenEnclaveOptions setInitTimeJson(byte[] initTimeData);

    /**
     * Retrieves the InitTimeJson property to be sent to the service.
     * @return The InitTimeJson value set by {@link AttestOpenEnclaveOptions#setInitTimeJson}
     * @throws IllegalStateException Thrown if the caller had called {@link AttestOpenEnclaveOptions#setInitTimeData}.
     */
    byte[] getInitTimeJson();

    /**
     * Set the draftPolicyForAttestation property: Attest against the provided draft policy. Note that the resulting
     * token cannot be validated.

     * Note that the resulting token cannot be validated.
     *
     * @param draftPolicyForAttestation the draftPolicyForAttestation value to set.
     * @return the AttestOpenEnclaveRequest object itself.
     */
    AttestOpenEnclaveOptions setDraftPolicyForAttestation(String draftPolicyForAttestation);

    /**
     * Gets the draftPolicyForAttestation property which is used to attest against the draft policy.
     *
     * Note that the resulting token cannot be validated.
     *
     * @return The draft policy if set.
     */
    String getDraftPolicyForAttestation();

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    void validate();
}
