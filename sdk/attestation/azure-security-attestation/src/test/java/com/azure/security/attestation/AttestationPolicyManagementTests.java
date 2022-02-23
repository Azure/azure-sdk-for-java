// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.security.attestation.models.AttestationSigner;
import com.azure.security.attestation.models.AttestationSignerCollection;
import com.azure.security.attestation.models.AttestationSigningKey;
import com.azure.security.attestation.models.CertificateModification;
import com.azure.security.attestation.models.PolicyCertificatesModificationResult;
import com.azure.security.attestation.models.PolicyManagementCertificateOptions;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.security.MessageDigest;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Execution(ExecutionMode.SAME_THREAD)
public class AttestationPolicyManagementTests extends AttestationClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testGetPolicyManagementCertificates(HttpClient httpClient, String clientUri) {

        AttestationAdministrationClientBuilder attestationBuilder = getAttestationAdministrationBuilder(httpClient, clientUri);
        AttestationAdministrationClient client = attestationBuilder.buildClient();

        AttestationSignerCollection response = client.listPolicyManagementCertificates();
        assertNotNull(response);
        verifyGetPolicyCertificatesResponse(clientUri, response);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testGetPolicyManagementCertificatesAsync(HttpClient httpClient, String clientUri) {
        AttestationAdministrationClientBuilder attestationBuilder = getAttestationAdministrationBuilder(httpClient, clientUri);
        AttestationAdministrationAsyncClient client = attestationBuilder.buildAsyncClient();


        StepVerifier.create(client.listPolicyManagementCertificates())
            .assertNext(response -> verifyGetPolicyCertificatesResponse(clientUri, response))
            .expectComplete()
            .verify();


    }


    /**
     * Verify the response to a get policy management certificates API call.
     * @param clientUri URI for client - used to determine the expected response.
     * @param signers Attestation signers returned by the service.
     */
    private void verifyGetPolicyCertificatesResponse(String clientUri, AttestationSignerCollection signers) {
        ClientTypes clientType = classifyClient(clientUri);
        if (clientType == ClientTypes.SHARED || clientType == ClientTypes.AAD) {
            assertEquals(0, signers.getAttestationSigners().size());
        } else {
            assertNotEquals(0, signers.getAttestationSigners().size());
            boolean foundIsolatedCertificate = false;
            for (AttestationSigner signer : signers.getAttestationSigners()) {
                if (signer.getCertificates().get(0).equals(getIsolatedSigningCertificate())) {
                    foundIsolatedCertificate = true;
                    break;
                }
            }
            assertTrue(foundIsolatedCertificate);
        }
    }


    /**
     * Verifies attestation policy set operations.
     *
     * This method iterates over the attestation types and instances and attempts to set attestation policies on
     * each instance.
     *
     * For AAD instances, we try set policy with two separate policies, one secured and the other unsecured.
     * For Isolated instances, we try to set policy with a policy document signed by the isolated signer.
     *
     * After the policy is set, the test method cleans up by resetting attestation policy to the default policy.
     *
     * @param httpClient HTTP Client used for operations.
     * @param clientUri Base URI for attestation instance.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAddAttestationPolicyManagementCertificate(HttpClient httpClient, String clientUri) {

        ClientTypes clientType = classifyClient(clientUri);

        assumeTrue(clientType == ClientTypes.ISOLATED, "This test only works on isolated instances.");

        AttestationAdministrationClientBuilder attestationBuilder = getAttestationAdministrationBuilder(httpClient, clientUri);
        AttestationAdministrationClient client = attestationBuilder.buildClient();

        PolicyCertificatesModificationResult result = client.addPolicyManagementCertificate(
            new PolicyManagementCertificateOptions(
                getPolicySigningCertificate0(),
                new AttestationSigningKey(getIsolatedSigningCertificate(), getIsolatedSigningKey())));

        assertEquals(CertificateModification.IS_PRESENT, result.getCertificateResolution());

        result = client.deletePolicyManagementCertificate(
            new PolicyManagementCertificateOptions(
                getPolicySigningCertificate0(),
                new AttestationSigningKey(getIsolatedSigningCertificate(), getIsolatedSigningKey())));

        assertEquals(CertificateModification.IS_ABSENT, result.getCertificateResolution());
    }
    /**
     * Verifies attestation policy set operations.
     *
     * This method iterates over the attestation types and instances and attempts to set attestation policies on
     * each instance.
     *
     * For AAD instances, we try set policy with two separate policies, one secured and the other unsecured.
     * For Isolated instances, we try to set policy with a policy document signed by the isolated signer.
     *
     * After the policy is set, the test method cleans up by resetting attestation policy to the default policy.
     *
     * @param httpClient HTTP Client used for operations.
     * @param clientUri Base URI for attestation instance.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAddAttestationPolicyManagementCertificateWithResponse(HttpClient httpClient, String clientUri) {

        ClientTypes clientType = classifyClient(clientUri);

        assumeTrue(clientType == ClientTypes.ISOLATED, "This test only works on isolated instances.");

        AttestationAdministrationClientBuilder attestationBuilder = getAttestationAdministrationBuilder(httpClient, clientUri);
        AttestationAdministrationClient client = attestationBuilder.buildClient();

        Response<PolicyCertificatesModificationResult> response = client.addPolicyManagementCertificateWithResponse(
            new PolicyManagementCertificateOptions(getPolicySigningCertificate0(),
                new AttestationSigningKey(getIsolatedSigningCertificate(), getIsolatedSigningKey())),
            Context.NONE);

        assertEquals(CertificateModification.IS_PRESENT, response.getValue().getCertificateResolution());

        response = client.deletePolicyManagementCertificateWithResponse(
            new PolicyManagementCertificateOptions(
                getPolicySigningCertificate0(),
                new AttestationSigningKey(getIsolatedSigningCertificate(), getIsolatedSigningKey())),
            Context.NONE);

        assertEquals(CertificateModification.IS_ABSENT, response.getValue().getCertificateResolution());
    }


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAddPolicyManagementCertificateAsync(HttpClient httpClient, String clientUri) {

        ClientTypes clientType = classifyClient(clientUri);
        // This test only works on isolated instances.
        assumeTrue(clientType == ClientTypes.ISOLATED, "This test only works on isolated instances.");

        AttestationAdministrationClientBuilder attestationBuilder = getAttestationAdministrationBuilder(httpClient, clientUri);
        AttestationAdministrationAsyncClient client = attestationBuilder.buildAsyncClient();

        X509Certificate certificate = getPolicySigningCertificate0();
        // Calculate the certificate thumbprint we're adding and removing.
        String expectedThumbprint =
            assertDoesNotThrow(() -> new String(Hex.encode(
                MessageDigest.getInstance("SHA-1").digest(
                    certificate.getEncoded()))).toUpperCase());

        StepVerifier.create(client.addPolicyManagementCertificate(new PolicyManagementCertificateOptions(
            certificate,
            new AttestationSigningKey(getIsolatedSigningCertificate(), getIsolatedSigningKey()))))
            .assertNext(modificationResult -> {
                assertEquals(CertificateModification.IS_PRESENT, modificationResult.getCertificateResolution());
                assertEquals(expectedThumbprint, modificationResult.getCertificateThumbprint());
            })
            .verifyComplete();

        // Now remove the certificate we just added.
        StepVerifier.create(client.deletePolicyManagementCertificate(new PolicyManagementCertificateOptions(
                getPolicySigningCertificate0(),
                new AttestationSigningKey(getIsolatedSigningCertificate(), getIsolatedSigningKey()))))
            .assertNext(removeResult -> {
                assertEquals(CertificateModification.IS_ABSENT, removeResult.getCertificateResolution());
                assertEquals(expectedThumbprint, removeResult.getCertificateThumbprint());
            })
            .verifyComplete();
    }

}

