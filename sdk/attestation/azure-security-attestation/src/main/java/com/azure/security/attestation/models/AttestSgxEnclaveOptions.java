package com.azure.security.attestation.models;

import com.azure.security.attestation.implementation.models.AttestOpenEnclaveOptionsImpl;
import com.azure.security.attestation.implementation.models.AttestSgxEnclaveOptionsImpl;

public interface AttestSgxEnclaveOptions {

    /**
     * Creates a new AttestOpenEnclaveRequest object with the OpenEnclave report from the enclave to be attested.
     *
     * @param report the report value to set.
     * @return the AttestOpenEnclaveRequest object itself.
     */
    static AttestSgxEnclaveOptions fromQuote(byte[] report) {
        return new AttestSgxEnclaveOptionsImpl().setQuote(report);
    }

    /**
     * Get the quote property: Quote of the enclave to be attested.
     *
     * @return the quote value.
     */
    byte[] getQuote();

    /**
     * Get the runtimeData property: Runtime data provided by the enclave at the time of quote generation. The MAA will
     * verify that the first 32 bytes of the report_data field of the quote contains the SHA256 hash of the decoded
     * "data" field of the runtime data.
     *
     * @return the runtimeData value.
     */
    byte[] getRunTimeData();

    /**
     * Get the runtimeJson property: Runtime data provided by the enclave at the time of quote generation. The MAA will
     * verify that the first 32 bytes of the report_data field of the quote contains the SHA256 hash of the decoded
     * "data" field of the runtime data.
     *
     * @return the runtimeData value.
     */
    byte[] getRunTimeJson();

    /**
     * Set the runtimeData property: Runtime data provided by the enclave at the time of quote generation. The MAA will
     * verify that the first 32 bytes of the report_data field of the quote contains the SHA256 hash of the decoded
     * "data" field of the runtime data.
     *
     * @param runtimeData the runtimeData value to set.
     * @return the AttestSgxEnclaveRequest object itself.
     */
    AttestSgxEnclaveOptions setRunTimeData(byte[] runtimeData);

    /**
     * Set the runtimeData property: Runtime data provided by the enclave at the time of quote generation. The MAA will
     * verify that the first 32 bytes of the report_data field of the quote contains the SHA256 hash of the decoded
     * "data" field of the runtime data.
     *
     * @param runtimeData the runtimeData value to set.
     * @return the AttestSgxEnclaveRequest object itself.
     */
    AttestSgxEnclaveOptions setRunTimeJson(byte[] runtimeData);

    /**
     * Get the initTimeData property as Binary: Initialization data provided when the enclave is created. MAA will verify that the
     * init data was known to the enclave. Note that InitTimeData is invalid for CoffeeLake processors.
     *
     * @return the initTimeData value.
     */
    byte[] getInitTimeData();

    /**
     * Get the initTimeData property as Binary: Initialization data provided when the enclave is created. MAA will verify that the
     * init data was known to the enclave. Note that InitTimeData is invalid for CoffeeLake processors.
     *
     * @return the initTimeData value.
     */
    byte[] getInitTimeJson();

    /**
     * Set the initTimeData property: Initialization data provided when the enclave is created. MAA will verify that the
     * init data was known to the enclave. Note that InitTimeData is invalid for CoffeeLake processors.
     *
     * @param initTimeData the initTimeData value to set.
     * @return the AttestSgxEnclaveRequest object itself.
     */
    AttestSgxEnclaveOptions setInitTimeData(byte[] initTimeData);

    /**
     * Set the initTimeData property as JSON: Initialization data provided when the enclave is created. MAA will verify that the
     * init data was known to the enclave. Note that InitTimeData is invalid for CoffeeLake processors.
     *
     * @param initTimeData the initTimeData value to set.
     * @return the AttestSgxEnclaveRequest object itself.
     */
    AttestSgxEnclaveOptions setInitTimeJson(byte[] initTimeData);

    /**
     * Get the draftPolicyForAttestation property: Attest against the provided draft policy. Note that the resulting
     * token cannot be validated.
     *
     * @return the draftPolicyForAttestation value.
     */
    String getDraftPolicyForAttestation();

    /**
     * Set the draftPolicyForAttestation property: Attest against the provided draft policy. Note that the resulting
     * token cannot be validated.
     *
     * @param draftPolicyForAttestation the draftPolicyForAttestation value to set.
     * @return the AttestSgxEnclaveRequest object itself.
     */
    AttestSgxEnclaveOptions setDraftPolicyForAttestation(String draftPolicyForAttestation);

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    void validate();
}
