// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.attestation.models.AttestationData;
import com.azure.security.attestation.models.AttestationDataInterpretation;
import com.azure.security.attestation.models.AttestationOptions;
import com.azure.security.attestation.models.AttestationResult;
import com.azure.security.attestation.models.AttestationSignerCollection;
import com.azure.security.attestation.models.AttestationType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReadmeSamples {

    static void testAttestSgxEnclave() {

        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationClientBuilder attestationBuilder = new AttestationClientBuilder();
        AttestationClient client = attestationBuilder
            .endpoint(endpoint)
            .buildClient();

        BinaryData decodedRuntimeData = BinaryData.fromBytes(SampleCollateral.getRunTimeData());
        BinaryData decodedOpenEnclaveReport = BinaryData.fromBytes(SampleCollateral.getOpenEnclaveReport());

        // BEGIN: readme-sample-attestSgxEnclave
        AttestationOptions options = new AttestationOptions(decodedOpenEnclaveReport)
            .setRunTimeData(new AttestationData(decodedRuntimeData, AttestationDataInterpretation.BINARY));
        AttestationResult result = client.attestOpenEnclave(options);
        // END: readme-sample-attestSgxEnclave
        assertNotNull(result.getIssuer());

        assertEquals(endpoint, result.getIssuer());
        String issuer = result.getIssuer();

        System.out.println("Attest OpenEnclave completed. Issuer: " + issuer);
    }

    static void getAttestationPolicy() {
        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationAdministrationClientBuilder attestationBuilder = new AttestationAdministrationClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build());

        AttestationAdministrationClient client = attestationBuilder.buildClient();

        String attestationPolicy = client.getAttestationPolicy(AttestationType.SGX_ENCLAVE);
    }

    static void signingCertificatesGet() {

        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationClientBuilder attestationBuilder = new AttestationClientBuilder();
        AttestationClient client = attestationBuilder
            .endpoint(endpoint)
            .buildClient();

        // BEGIN: readme-sample-getSigningCertificates
        AttestationSignerCollection certs = client.listAttestationSigners();

        certs.getAttestationSigners().forEach(cert -> {
            System.out.println("Found certificate.");
            if (cert.getKeyId() != null) {
                System.out.println("    Certificate Key ID: " + cert.getKeyId());
            } else {
                System.out.println("    Signer does not have a Key ID");
            }
            cert.getCertificates().forEach(chainElement -> {
                System.out.println("        Cert Subject: " + chainElement.getSubjectDN().getName());
                System.out.println("        Cert Issuer: " + chainElement.getIssuerDN().getName());
            });
        });
        // END: readme-sample-getSigningCertificates
    }

    static void executeSamples() {
        signingCertificatesGet();
        getAttestationPolicy();
        testAttestSgxEnclave();
    }


}
