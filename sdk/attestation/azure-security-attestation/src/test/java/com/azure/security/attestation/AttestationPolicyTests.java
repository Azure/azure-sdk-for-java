// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Base64Util;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.security.attestation.models.AttestationPolicySetOptions;
import com.azure.security.attestation.models.AttestationResponse;
import com.azure.security.attestation.models.AttestationSigningKey;
import com.azure.security.attestation.models.AttestationType;
import com.azure.security.attestation.models.PolicyModification;
import com.azure.security.attestation.models.PolicyResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@LiveOnly
public class AttestationPolicyTests extends AttestationClientTestBase {
    // LiveOnly because "JWT cannot be stored in recordings."
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getPolicyClients")
    void testGetAttestationPolicy(HttpClient client, String clientUri, AttestationType attestationType) {
        AttestationAdministrationClientBuilder attestationBuilder
            = getAttestationAdministrationBuilder(client, clientUri);

        AttestationAdministrationClient adminClient = attestationBuilder.buildClient();

        String policy = adminClient.getAttestationPolicy(attestationType);
        if (policy == null) {
            assertEquals(AttestationType.TPM, attestationType);
        } else {
            assertTrue(policy.contains("version"));
        }
        Response<String> policyResponse
            = adminClient.getAttestationPolicyWithResponse(attestationType, null, Context.NONE);
        if (policyResponse.getValue() == null) {
            assertEquals(AttestationType.TPM, attestationType);
        } else {
            assertTrue(policyResponse.getValue().contains("version"));
        }
        assertTokenIsJws(policyResponse);
    }

    void assertTokenIsJws(Response<String> response) {
        assertTrue(response instanceof AttestationResponse);
        // Verify we can access the underlying policy JWT.
        AttestationResponse<String> attestResponse = (AttestationResponse<String>) response;
        // This token is NOT a JWT, it's a JWS. As such, the token properties
        // won't be present.
        assertNull(attestResponse.getToken().getIssuer());
        assertNull(attestResponse.getToken().getExpiresOn());
        assertNull(attestResponse.getToken().getNotBefore());
        assertNull(attestResponse.getToken().getIssuedAt());
        assertNotNull(attestResponse.getToken().getAlgorithm());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getPolicyClients")
    void testGetAttestationPolicyAsync(HttpClient client, String clientUri, AttestationType attestationType) {
        AttestationAdministrationClientBuilder attestationBuilder
            = getAttestationAdministrationBuilder(client, clientUri);

        AttestationAdministrationAsyncClient adminClient = attestationBuilder.buildAsyncClient();

        StepVerifier.create(adminClient.getAttestationPolicy(attestationType).switchIfEmpty(Mono.just("No policy set")))
            .assertNext(response -> assertDoesNotThrow(() -> {
                // If the policy is null, it must be coming from TPM attestation.
                if (response.equals("No policy set")) {
                    assertEquals(AttestationType.TPM, attestationType);
                } else {
                    assertTrue(response.contains("version"));
                }
            }))
            .expectComplete()
            .verify();

        // Now test getAttestationPolicy with a response.
        StepVerifier.create(adminClient.getAttestationPolicyWithResponse(attestationType, null))
            .assertNext(response -> assertDoesNotThrow(() -> {
                // If the policy is null, it must be coming from TPM attestation.
                if (response.getValue() == null) {
                    assertEquals(AttestationType.TPM, attestationType);
                } else {
                    assertTrue(response.getValue().contains("version"));
                }
                assertTokenIsJws(response);
            }))
            .expectComplete()
            .verify();

    }

    static void verifySetPolicyResult(AttestationAdministrationClient client, PolicyResult result, AttestationType type,
        String expectedPolicy, AttestationSigningKey signer, PolicyModification expectedModification) {
        assertEquals(expectedModification, result.getPolicyResolution());
        if (expectedPolicy == null) {
            return;
        }

        assertEquals(expectedPolicy, client.getAttestationPolicy(type));
        BinaryData expectedHash = client.calculatePolicyTokenHash(expectedPolicy, signer);
        BinaryData actualHash = result.getPolicyTokenHash();
        // Base64 encode the binary data for easier comparison if there is a mismatch.
        assertEquals(Base64Util.encodeToString(expectedHash.toBytes()),
            Base64Util.encodeToString(actualHash.toBytes()));
    }

    /**
     * Verifies attestation policy set operations.
     * <p>
     * This method iterates over the attestation types and instances and attempts to set attestation policies on each
     * instance.
     * <p>
     * For AAD instances, we try set policy with two separate policies, one secured and the other unsecured. For
     * Isolated instances, we try to set policy with a policy document signed by the isolated signer.
     * <p>
     * After the policy is set, the test method cleans up by resetting attestation policy to the default policy.
     *
     * @param httpClient HTTP Client used for operations.
     * @param clientUri Base URI for attestation instance.
     * @param attestationType AttestationType on which to set policy.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getPolicyClients")
    void testSetAttestationPolicy(HttpClient httpClient, String clientUri, AttestationType attestationType) {

        ClientTypes clientType = classifyClient(clientUri);
        // We can't set attestation policy on the shared client, so just exit early.
        assumeTrue(clientType != ClientTypes.SHARED, "This test does not work on shared instances.");

        AttestationAdministrationClientBuilder attestationAdministrationClientBuilder
            = getAttestationAdministrationBuilder(httpClient, clientUri);
        AttestationAdministrationClient client = attestationAdministrationClientBuilder.buildClient();

        // AAD or isolated: We want to try setting policy with the Isolated signing certificate.
        if (clientType == ClientTypes.AAD || clientType == ClientTypes.ISOLATED) {
            X509Certificate certificate = getIsolatedSigningCertificate();
            PrivateKey key = getIsolatedSigningKey();

            AttestationSigningKey signingKey = new AttestationSigningKey(certificate, key);

            String policyToSet = "version =1.0; authorizationrules{=> permit();}; issuancerules{};";
            PolicyResult result = client.setAttestationPolicy(attestationType,
                new AttestationPolicySetOptions().setAttestationPolicy(policyToSet).setAttestationSigner(signingKey));
            verifySetPolicyResult(client, result, attestationType, policyToSet, signingKey, PolicyModification.UPDATED);
        }

        if (clientType == ClientTypes.AAD) {
            X509Certificate certificate = getPolicySigningCertificate0();
            PrivateKey key = getPolicySigningKey0();

            AttestationSigningKey signingKey = new AttestationSigningKey(certificate, key);

            String policyToSet = "version=1.0; authorizationrules{=> permit();}; issuancerules{ };";
            PolicyResult result = client.setAttestationPolicy(attestationType,
                new AttestationPolicySetOptions().setAttestationPolicy(policyToSet).setAttestationSigner(signingKey));
            verifySetPolicyResult(client, result, attestationType, policyToSet, signingKey, PolicyModification.UPDATED);

            result = client.resetAttestationPolicy(attestationType,
                new AttestationPolicySetOptions().setAttestationSigner(signingKey));
            verifySetPolicyResult(client, result, attestationType, null, signingKey, PolicyModification.REMOVED);
        }

        if (clientType == ClientTypes.AAD) {
            String policyToSet = "version=1.0; authorizationrules{=> permit( );}; issuancerules{};";

            verifySetPolicyResult(client,
                client.setAttestationPolicy(attestationType,
                    new AttestationPolicySetOptions().setAttestationPolicy(policyToSet)),
                attestationType, policyToSet, null, PolicyModification.UPDATED);

            assertEquals(policyToSet, client.getAttestationPolicy(attestationType));

            verifySetPolicyResult(client, client.resetAttestationPolicy(attestationType), attestationType, null, null,
                PolicyModification.REMOVED);
        }

        if (clientType == ClientTypes.AAD) {
            String policyToSet = "version=1.0;authorizationrules{=> permit();}; issuancerules{};";

            verifySetPolicyResult(client, client.setAttestationPolicy(attestationType, policyToSet), attestationType,
                policyToSet, null, PolicyModification.UPDATED);

            verifySetPolicyResult(client, client.resetAttestationPolicy(attestationType), attestationType, null, null,
                PolicyModification.REMOVED);
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getPolicyClients")
    void testSetAttestationPolicyWithResponse(HttpClient httpClient, String clientUri,
        AttestationType attestationType) {

        ClientTypes clientType = classifyClient(clientUri);
        // We can't set attestation policy on the shared client, so just exit early.
        assumeTrue(clientType != ClientTypes.SHARED, "This test does not work on shared instances.");

        AttestationAdministrationClientBuilder attestationAdministrationClientBuilder
            = getAttestationAdministrationBuilder(httpClient, clientUri);
        AttestationAdministrationClient client = attestationAdministrationClientBuilder.buildClient();

        // AAD or isolated: We want to try setting policy with the Isolated signing certificate.
        if (clientType == ClientTypes.AAD || clientType == ClientTypes.ISOLATED) {
            PrivateKey key = getIsolatedSigningKey();
            X509Certificate cert = getIsolatedSigningCertificate();

            AttestationSigningKey signingKey = new AttestationSigningKey(cert, key);
            assertDoesNotThrow(signingKey::verify);

            String policyToSet = "version =1.0; authorizationrules{=> permit();}; issuancerules{};";
            Response<PolicyResult> result = client.setAttestationPolicyWithResponse(attestationType,
                new AttestationPolicySetOptions().setAttestationPolicy(policyToSet).setAttestationSigner(signingKey),
                Context.NONE);
            verifySetPolicyResult(client, result.getValue(), attestationType, policyToSet, signingKey,
                PolicyModification.UPDATED);
        }

        if (clientType == ClientTypes.AAD) {
            X509Certificate certificate = getPolicySigningCertificate0();
            PrivateKey key = getPolicySigningKey0();
            AttestationSigningKey signingKey = new AttestationSigningKey(certificate, key);

            assertDoesNotThrow(signingKey::verify);

            String policyToSet = "version=1.0; authorizationrules{=> permit();}; issuancerules{ };";
            Response<PolicyResult> result = client.setAttestationPolicyWithResponse(attestationType,
                new AttestationPolicySetOptions().setAttestationPolicy(policyToSet).setAttestationSigner(signingKey),
                Context.NONE);
            verifySetPolicyResult(client, result.getValue(), attestationType, policyToSet, signingKey,
                PolicyModification.UPDATED);

            result = client.resetAttestationPolicyWithResponse(attestationType,
                new AttestationPolicySetOptions().setAttestationSigner(signingKey), Context.NONE);
            verifySetPolicyResult(client, result.getValue(), attestationType, null, signingKey,
                PolicyModification.REMOVED);
        }

        if (clientType == ClientTypes.AAD) {
            String policyToSet = "version=1.0; authorizationrules{=> permit( );}; issuancerules{};";
            Response<PolicyResult> result = client.setAttestationPolicyWithResponse(attestationType,
                new AttestationPolicySetOptions().setAttestationPolicy(policyToSet), Context.NONE);
            verifySetPolicyResult(client, result.getValue(), attestationType, policyToSet, null,
                PolicyModification.UPDATED);

            assertEquals(policyToSet, client.getAttestationPolicy(attestationType));

            verifySetPolicyResult(client, client.resetAttestationPolicy(attestationType), attestationType, null, null,
                PolicyModification.REMOVED);
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getPolicyClients")
    void testSetAttestationPolicyAsync(HttpClient httpClient, String clientUri, AttestationType attestationType) {
        ClientTypes clientType = classifyClient(clientUri);
        // We can't set attestation policy on the shared client, so just exit early.
        assumeTrue(clientType != ClientTypes.SHARED, "This test does not work on shared instances.");

        AttestationAdministrationClientBuilder attestationBuilder
            = getAttestationAdministrationBuilder(httpClient, clientUri);
        AttestationAdministrationAsyncClient client = attestationBuilder.buildAsyncClient();

        // AAD or isolated: We want to try setting policy with the Isolated signing certificate.
        if (clientType == ClientTypes.AAD || clientType == ClientTypes.ISOLATED) {
            X509Certificate certificate = getIsolatedSigningCertificate();
            PrivateKey key = getIsolatedSigningKey();

            AttestationSigningKey signingKey = new AttestationSigningKey(certificate, key);

            String policyToSet = "version=1.0; authorizationrules{=> permit();}; issuancerules{};";

            // Test setting the policy. This works for both AAD and Isolated mode.
            StepVerifier
                .create(client.setAttestationPolicy(attestationType,
                    new AttestationPolicySetOptions().setAttestationPolicy(policyToSet)
                        .setAttestationSigner(signingKey)))
                .assertNext(result -> assertEquals(PolicyModification.UPDATED, result.getPolicyResolution()))
                .expectComplete()
                .verify();

            StepVerifier.create(client.getAttestationPolicy(attestationType))
                .assertNext(result -> assertEquals(policyToSet, result))
                .expectComplete()
                .verify();

            // Now reset the policy we just set and verify that the new policy doesn't match the old.
            StepVerifier
                .create(client.resetAttestationPolicy(attestationType,
                    new AttestationPolicySetOptions().setAttestationSigner(signingKey)))
                .assertNext(result -> assertEquals(PolicyModification.REMOVED, result.getPolicyResolution()))
                .expectComplete()
                .verify();

            StepVerifier.create(client.getAttestationPolicy(attestationType).switchIfEmpty(Mono.just("None")))
                .assertNext(result -> assertNotEquals(policyToSet, result))
                .expectComplete()
                .verify();
        }

        // Try setting attestation policy with an arbitrary signing key - this should be allowed
        // in AAD mode.
        if (clientType == ClientTypes.AAD) {
            KeyPair rsaKey = assertDoesNotThrow(() -> createKeyPair("RSA"));
            X509Certificate cert
                = assertDoesNotThrow(() -> createSelfSignedCertificate("Test Certificate Secured 2", rsaKey));
            AttestationSigningKey signingKey
                = new AttestationSigningKey(cert, rsaKey.getPrivate()).setWeakKeyAllowed(true);

            String policyToSet = "version=1.0; authorizationrules{=> permit();}; issuancerules{};";
            // Test setting the policy. This works for both AAD and Isolated mode.
            StepVerifier
                .create(client.setAttestationPolicy(attestationType,
                    new AttestationPolicySetOptions().setAttestationPolicy(policyToSet)
                        .setAttestationSigner(signingKey)))
                .assertNext(result -> assertEquals(PolicyModification.UPDATED, result.getPolicyResolution()))
                .expectComplete()
                .verify();

            // Now reset the policy we just set and verify that the new policy doesn't match the old.
            StepVerifier.create(client.getAttestationPolicy(attestationType))
                .assertNext(result -> assertEquals(policyToSet, result))
                .expectComplete()
                .verify();

            // And reset the policy to the default using that key.
            StepVerifier
                .create(client.resetAttestationPolicy(attestationType,
                    new AttestationPolicySetOptions().setAttestationSigner(signingKey)))
                .assertNext(result -> assertEquals(PolicyModification.REMOVED, result.getPolicyResolution()))
                .expectComplete()
                .verify();

            StepVerifier.create(client.getAttestationPolicy(attestationType).switchIfEmpty(Mono.just("None")))
                .assertNext(result -> assertNotEquals(policyToSet, result))
                .expectComplete()
                .verify();
        }

        // Try setting attestation policy with an unsigned policy token - this should be allowed
        // in AAD mode.
        if (clientType == ClientTypes.AAD) {
            // Test setting the policy. This works for both AAD and Isolated mode.
            String policyToSet = "version=1.0; authorizationrules{=> permit();}; issuancerules{};";
            StepVerifier
                .create(client.setAttestationPolicy(attestationType,
                    new AttestationPolicySetOptions().setAttestationPolicy(policyToSet)))
                .assertNext(result -> assertEquals(PolicyModification.UPDATED, result.getPolicyResolution()))
                .expectComplete()
                .verify();

            // And reset the policy to the default using that key.
            StepVerifier.create(client.resetAttestationPolicy(attestationType))
                .assertNext(result -> assertEquals(PolicyModification.REMOVED, result.getPolicyResolution()))
                .expectComplete()
                .verify();

            StepVerifier.create(client.getAttestationPolicy(attestationType).switchIfEmpty(Mono.just("None")))
                .assertNext(result -> assertNotEquals(policyToSet, result))
                .expectComplete()
                .verify();
        }
    }

    @LiveOnly
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getPolicyClients")
    void testSetAttestationPolicyAadAsync(HttpClient httpClient, String clientUri, AttestationType attestationType) {
        ClientTypes clientType = classifyClient(clientUri);
        // We can't set attestation policy on the shared client, so just exit early.
        assumeTrue(clientType != ClientTypes.SHARED, "This test does not work on shared instances.");
        assumeTrue(clientType != ClientTypes.ISOLATED, "This test does not work on isolated instances.");

        AttestationAdministrationClientBuilder attestationBuilder
            = getAttestationAdministrationBuilder(httpClient, clientUri);
        AttestationAdministrationAsyncClient client = attestationBuilder.buildAsyncClient();

        String policyToSet = "version=1.0; authorizationrules{=> permit();}; issuancerules{};";
        // Test setting the policy. This works for both AAD and Isolated mode.
        StepVerifier.create(client.setAttestationPolicy(attestationType, policyToSet))
            .assertNext(result -> assertEquals(PolicyModification.UPDATED, result.getPolicyResolution()))
            .expectComplete()
            .verify();

        StepVerifier.create(client.getAttestationPolicy(attestationType))
            .assertNext(result -> assertEquals(policyToSet, result))
            .expectComplete()
            .verify();
    }

    @LiveOnly
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getPolicyClients")
    void testSetAttestationPolicyWithResponseAsync(HttpClient httpClient, String clientUri,
        AttestationType attestationType) {
        ClientTypes clientType = classifyClient(clientUri);
        // We can't set attestation policy on the shared client, so just exit early.
        assumeTrue(clientType != ClientTypes.SHARED, "This test does not work on shared instances.");

        AttestationAdministrationClientBuilder attestationBuilder
            = getAttestationAdministrationBuilder(httpClient, clientUri);
        AttestationAdministrationAsyncClient client = attestationBuilder.buildAsyncClient();

        // AAD or isolated: We want to try setting policy with the Isolated signing certificate.
        if (clientType == ClientTypes.AAD || clientType == ClientTypes.ISOLATED) {
            X509Certificate certificate = getIsolatedSigningCertificate();
            PrivateKey key = getIsolatedSigningKey();

            AttestationSigningKey signingKey = new AttestationSigningKey(certificate, key);

            // Test setting the policy. This works for both AAD and Isolated mode.
            StepVerifier.create(client.setAttestationPolicyWithResponse(attestationType,
                new AttestationPolicySetOptions()
                    .setAttestationPolicy("version=1.0; authorizationrules{=> permit();}; issuancerules{};")
                    .setAttestationSigner(signingKey)))
                .assertNext(response -> {
                    assertEquals(PolicyModification.UPDATED, response.getValue().getPolicyResolution());
                    assertEquals(1, response.getValue().getPolicySigner().getCertificates().size());
                    assertEquals(certificate.toString(),
                        response.getValue().getPolicySigner().getCertificates().get(0).toString());
                })
                .expectComplete()
                .verify();
        }

        // Try setting attestation policy with an arbitrary signing key - this should be allowed
        // in AAD mode.
        if (clientType == ClientTypes.AAD) {
            X509Certificate certificate = getPolicySigningCertificate0();
            PrivateKey key = getPolicySigningKey0();

            AttestationSigningKey signingKey = new AttestationSigningKey(certificate, key);

            // Test setting the policy. This works for both AAD and Isolated mode.
            StepVerifier
                .create(client.setAttestationPolicyWithResponse(attestationType,
                    new AttestationPolicySetOptions()
                        .setAttestationPolicy("version=1.0; authorizationrules{=> permit();}; issuancerules{};")
                        .setAttestationSigner(signingKey)))
                .assertNext(
                    response -> assertEquals(PolicyModification.UPDATED, response.getValue().getPolicyResolution()))
                .expectComplete()
                .verify();
        }
        // Try setting attestation policy with an unsigned policy token - this should be allowed
        // in AAD mode.
        if (clientType == ClientTypes.AAD) {
            // Test setting the policy. This works for both AAD and Isolated mode.
            StepVerifier
                .create(client.setAttestationPolicyWithResponse(attestationType,
                    new AttestationPolicySetOptions()
                        .setAttestationPolicy("version=1.0; authorizationrules{=> permit();}; issuancerules{};")))
                .assertNext(
                    response -> assertEquals(PolicyModification.UPDATED, response.getValue().getPolicyResolution()))
                .expectComplete()
                .verify();
        }
    }
}
