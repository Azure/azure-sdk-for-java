// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.http.HttpClient;
import com.azure.security.attestation.models.AttestationOptions;
import com.azure.security.attestation.models.AttestationResult;
import com.azure.security.attestation.models.AttestationSigner;
import com.azure.security.attestation.models.AttestationType;
import com.azure.security.attestation.models.PolicyResponse;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReadmeSamples {

    static void testAttestSgxEnclave() {

        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationClientBuilder attestationBuilder = new AttestationClientBuilder();
        AttestationClient client = attestationBuilder
            .endpoint(endpoint)
            .buildClient();

        byte[] decodedRuntimeData = SampleCollateral.getRunTimeData();
        byte[] decodedOpenEnclaveReport = SampleCollateral.getOpenEnclaveReport();

        AttestationOptions options = AttestationOptions
            .fromEvidence(decodedOpenEnclaveReport)
            .setRunTimeData(decodedRuntimeData)
            .interpretRunTimeDataAsBinary();
        AttestationResult result = client.attestOpenEnclave(options);

        assertNotNull(result.getIssuer());

        assertEquals(endpoint, result.getIssuer());
        String issuer = result.getIssuer();

        System.out.println("Attest OpenEnclave completed. Issuer: " + issuer);
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
            .buildClient();

        AttestationSigner[] certs = client.getAttestationSigners();

        Arrays.stream(certs).forEach(cert -> {
            System.out.println("Found certificate.");
            if (cert.getKeyId() != null) {
                System.out.println("    Certificate Key ID: " + cert.getKeyId());
            } else {
                System.out.println("    Signer does not have a Key ID");
            }
            Arrays.stream(cert.getCertificates()).forEach(chainElement -> {
                System.out.println("        Cert Subject: " + chainElement.getSubjectDN().getName());
                System.out.println("        Cert Issuer: " + chainElement.getIssuerDN().getName());
            });
        });
    }

}
