// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.security.attestation.models.JsonWebKeySet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

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

        JsonWebKeySet certs = attestationBuilder.buildSigningCertificatesClient().get();


        verifySigningCertificatesResponse(clientUri, certs);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testGetSigningCertificatesAsync(HttpClient client, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(client, clientUri);

        StepVerifier.create(attestationBuilder.buildSigningCertificatesAsyncClient().get())
            .assertNext(certs -> Assertions.assertDoesNotThrow(() -> verifySigningCertificatesResponse(clientUri, certs)))
            .expectComplete()
            .verify();
    }

    /**
     * Verifies the response to the GetSigningCertificates (/certs) API.
     *
     * Each certificate returned needs to be a valid X.509 certificate.
     * We also verify that self signed certificates are signed with the known trusted roots.
     * @param clientUri Base URI for client, used to verify the contents of the certificates.
     * @param certs certificate response to verify.
     */
    private void verifySigningCertificatesResponse(String clientUri, JsonWebKeySet certs) {
        Assertions.assertTrue(certs.getKeys().size() > 1);

        CertificateFactory cf;
        try {
            cf = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.toString()));
        }
        CertificateFactory finalCf = cf;
        certs.getKeys().forEach(key -> {
            assertNotNull(key.getKid());
            assertNotNull(key.getX5C());
            Assertions.assertNotEquals(0, key.getX5C().size());
            key.getX5C().forEach(base64cert -> {
                try {
                    Certificate cert = finalCf.generateCertificate(base64ToStream(base64cert));

                    Assertions.assertTrue(cert instanceof X509Certificate);

                    X509Certificate x5c = (X509Certificate) cert;

//                    if (x5c.getExtensionValue("1.2.840.113556.10.1.1") != null) {
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
                } catch (CertificateException e) {
                    throw logger.logExceptionAsError(new RuntimeException(e.toString()));
                }
            });
        });
    }
}

