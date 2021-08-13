// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import com.azure.security.attestation.models.AttestationSigner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test for Attestation Signing Certificates APIs.
 */
public class AttestationSignersTest extends AttestationClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testGetSigningCertificates(HttpClient client, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(client, clientUri);

        List<AttestationSigner> signers = attestationBuilder.buildClient().listAttestationSigners();

        verifySigningCertificatesResponse(clientUri, signers);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testGetSigningCertificatesWithResponse(HttpClient client, String clientUri) {

        AttestationClient attestationClient = getBuilder(client, clientUri).buildClient();

        Response<List<AttestationSigner>> signers = attestationClient
            .listAttestationSignersWithResponse(Context.NONE);

        verifySigningCertificatesResponse(clientUri, signers.getValue());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testGetSigningCertificatesAsync(HttpClient client, String clientUri) {

        AttestationAsyncClient attestationClient = getBuilder(client, clientUri).buildAsyncClient();

        StepVerifier.create(attestationClient.listAttestationSigners())
            .assertNext(signers -> Assertions.assertDoesNotThrow(() -> verifySigningCertificatesResponse(clientUri, signers)))
            .expectComplete()
            .verify();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testGetSigningCertificatesWithResponseAsync(HttpClient client, String clientUri) {

        AttestationAsyncClient attestationClient = getBuilder(client, clientUri).buildAsyncClient();

        StepVerifier.create(attestationClient.listAttestationSignersWithResponse())
            .assertNext(signers -> Assertions.assertDoesNotThrow(() -> verifySigningCertificatesResponse(clientUri, signers.getValue())))
            .expectComplete()
            .verify();
    }


    /**
     * Verifies the response to the GetSigningCertificates (/certs) API.
     * <p>
     * Each certificate returned needs to be a valid X.509 certificate.
     * We also verify that self signed certificates are signed with the known trusted roots.
     *
     * @param clientUri Base URI for client, used to verify the contents of the certificates.
     * @param signers   AttestationSigners to verify.
     */
    private void verifySigningCertificatesResponse(String clientUri, List<AttestationSigner> signers) {
        Assertions.assertTrue(signers.size() > 1);

        signers.forEach(signer -> {
            assertNotNull(signer.getKeyId());
            assertNotNull(signer.getCertificates());
            Assertions.assertNotEquals(0, signer.getCertificates().size());
            signer.getCertificates().forEach(x5c -> {
                // If the certificate is self signed, it should be associated
                // with either the Microsoft root CA, the VBS self signed root, or the instance.
                if (x5c.getIssuerDN().equals(x5c.getSubjectDN())) {
                    if (x5c.getIssuerDN().toString().contains("Microsoft Root Certificate Authority")) {
                        assertEquals("CN=Microsoft Root Certificate Authority 2011, O=Microsoft Corporation, L=Redmond, ST=Washington, C=US", x5c.getIssuerDN().getName());
                    } else if (x5c.getIssuerDN().toString().contains("AttestationService-LocalTest-ReportSigning")) {
                        assertEquals("CN=AttestationService-LocalTest-ReportSigning", x5c.getIssuerDN().getName());
                    } else {
                        // In playback mode, the clientUri is bogus, so it cannot be validated.
                        if (testContextManager.getTestMode() != TestMode.PLAYBACK) {
                            assertEquals("CN=" + clientUri, x5c.getSubjectDN().getName());
                        }
                    }
                }
            });
        });
    }
}

