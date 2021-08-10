// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.http.HttpClient;
import com.azure.security.attestation.models.AttestationOptions;
import com.azure.security.attestation.models.AttestationResult;
import com.azure.security.attestation.models.AttestationSigner;
import com.azure.security.attestation.models.AttestationType;
import com.azure.security.attestation.models.PolicyResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReadmeSamples {

    static void testAttestSgxEnclave(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = new AttestationClientBuilder()
            .httpClient(httpClient)
            .endpoint(clientUri);

        AttestationClient client = attestationBuilder.buildAttestationClient();

        byte[] decodedRuntimeData = new byte[1];
        byte[] decodedSgxQuote = new byte[1];

        AttestationOptions options = AttestationOptions
            .fromEvidence(decodedSgxQuote)
            .setRunTimeData(decodedRuntimeData)
            .interpretRunTimeDataAsBinary();
        AttestationResult result = client.attestSgxEnclave(options);

        assertNotNull(result.getIssuer());

        assertEquals(clientUri, result.getIssuer());
    }

    static void attestationPolicyGet(HttpClient httpClient, String clientUri) {
        AttestationClientBuilder attestationBuilder = new AttestationClientBuilder();
        attestationBuilder.httpClient(httpClient);
        attestationBuilder.endpoint(clientUri);

        PolicyClient client = attestationBuilder.buildPolicyClient();

        PolicyResponse policyResponse = client.get(AttestationType.SGX_ENCLAVE);
    }

    static void signingCertificatesGet() {

        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationClientBuilder attestationBuilder = new AttestationClientBuilder();
        AttestationClient client = attestationBuilder
            .endpoint(endpoint)
            .buildAttestationClient();

        AttestationSigner[] certs = client.getAttestationSigners();
    }

}
