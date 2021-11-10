// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.security.attestation.models.AttestationPolicySetOptions;
import com.azure.security.attestation.models.AttestationResponse;
import com.azure.security.attestation.models.AttestationSigningKey;
import com.azure.security.attestation.models.AttestationType;
import com.azure.security.attestation.models.PolicyModification;
import com.azure.security.attestation.models.PolicyResult;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.PlainObject;
import net.minidev.json.JSONObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class AttestationPolicyTests extends AttestationClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getPolicyClients")
    void testGetAttestationPolicy(HttpClient client, String clientUri, AttestationType attestationType) {

        AttestationAdministrationClientBuilder attestationBuilder = getAdministrationBuilder(client, clientUri);

        AttestationAdministrationClient adminClient = attestationBuilder.buildClient();

        String policy = adminClient.getAttestationPolicy(attestationType);
        if (policy == null) {
            assertEquals(AttestationType.TPM, attestationType);
        } else {
            assertTrue(policy.contains("version"));
        }
        Response<String> policyResponse = adminClient.getAttestationPolicyWithResponse(attestationType, Context.NONE);
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
        AttestationAdministrationClientBuilder attestationBuilder = getAdministrationBuilder(client, clientUri);

        AttestationAdministrationAsyncClient adminClient = attestationBuilder.buildAsyncClient();

        StepVerifier.create(adminClient.getAttestationPolicy(attestationType)
                .switchIfEmpty(Mono.just("No policy set")))
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
        StepVerifier.create(adminClient.getAttestationPolicyWithResponse(attestationType))
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

    void verifySetPolicyResult(AttestationAdministrationClient client, PolicyResult result, AttestationType type, String expectedPolicy) {
        assertEquals(PolicyModification.UPDATED, result.getPolicyResolution());
        assertEquals(expectedPolicy, client.getAttestationPolicy(type));
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
     * @param attestationType AttestationType on which to set policy.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getPolicyClients")
    void testSetAttestationPolicy(HttpClient httpClient, String clientUri, AttestationType attestationType) {

        ClientTypes clientType = classifyClient(clientUri);
        // We can't set attestation policy on the shared client, so just exit early.
        assumeTrue(clientType != ClientTypes.SHARED, "This test does not work on shared instances.");

        AttestationAdministrationClientBuilder attestationAdministrationClientBuilder = getAdministrationBuilder(httpClient, clientUri);
        AttestationAdministrationClient client = attestationAdministrationClientBuilder.buildClient();

        String signingCertificateBase64 = getIsolatedSigningCertificateBase64();
        String signingKeyBase64 = getIsolatedSigningKeyBase64();

        JWSSigner signer = getJwsSigner(signingKeyBase64);

        // AAD or isolated: We want to try setting policy with the Isolated signing certificate.
        if (clientType == ClientTypes.AAD || clientType == ClientTypes.ISOLATED) {
            X509Certificate certificate = getIsolatedSigningCertificate();
            PrivateKey key = getIsolatedSigningKey();

            AttestationSigningKey signingKey = new AttestationSigningKey()
                .setPrivateKey(key)
                .setCertificate(certificate);

            String policyToSet = "version =1.0; authorizationrules{=> permit();}; issuancerules{};";
            assertDoesNotThrow(() -> {
                PolicyResult result = client.setAttestationPolicy(attestationType, new AttestationPolicySetOptions()
                    .setPolicy(policyToSet)
                    .setAttestationSigner(signingKey));
                verifySetPolicyResult(client, result, attestationType, policyToSet);
            });
        }

        if (clientType == ClientTypes.AAD) {
            X509Certificate certificate = getPolicySigningCertificate0();
            PrivateKey key = getPolicySigningKey0();

            AttestationSigningKey signingKey = new AttestationSigningKey()
                .setPrivateKey(key)
                .setCertificate(certificate);

            String policyToSet = "version=1.0; authorizationrules{=> permit();}; issuancerules{ };";
            assertDoesNotThrow(() -> {
                PolicyResult result = client.setAttestationPolicy(attestationType, new AttestationPolicySetOptions()
                    .setPolicy(policyToSet)
                    .setAttestationSigner(signingKey));
                verifySetPolicyResult(client, result, attestationType, policyToSet);
            });
        }

        if (clientType == ClientTypes.AAD) {
            String policyToSet = "version=1.0; authorizationrules{=> permit( );}; issuancerules{};";
            assertDoesNotThrow(() -> {
                PolicyResult result = client.setAttestationPolicy(attestationType, new AttestationPolicySetOptions()
                    .setPolicy(policyToSet));
                verifySetPolicyResult(client, result, attestationType, policyToSet);
            });
            assertEquals(policyToSet, client.getAttestationPolicy(attestationType));

        }
        if (clientType == ClientTypes.AAD) {
            String policyToSet = "version=1.0;authorizationrules{=> permit();}; issuancerules{};";
            assertDoesNotThrow(() -> {
                PolicyResult result = client.setAttestationPolicy(attestationType, policyToSet);
                verifySetPolicyResult(client, result, attestationType, policyToSet);
            });
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getPolicyClients")
    void testSetAttestationPolicyAsync(HttpClient httpClient, String clientUri, AttestationType attestationType) {
        ClientTypes clientType = classifyClient(clientUri);
        // We can't set attestation policy on the shared client, so just exit early.
        assumeTrue(clientType != ClientTypes.SHARED, "This test does not work on shared instances.");

        AttestationAdministrationClientBuilder attestationBuilder = getAdministrationBuilder(httpClient, clientUri);
        AttestationAdministrationAsyncClient client = attestationBuilder.buildAsyncClient();

        // AAD or isolated: We want to try setting policy with the Isolated signing certificate.
        if (clientType == ClientTypes.AAD || clientType == ClientTypes.ISOLATED) {
            X509Certificate certificate = getIsolatedSigningCertificate();
            PrivateKey key = getIsolatedSigningKey();

            AttestationSigningKey signingKey = new AttestationSigningKey()
                .setPrivateKey(key)
                .setCertificate(certificate);

            String policyToSet = "version=1.0; authorizationrules{=> permit();}; issuancerules{};";

            // Test setting the policy. This works for both AAD and Isolated mode.
            StepVerifier.create(client.setAttestationPolicy(attestationType, new AttestationPolicySetOptions()
                    .setPolicy(policyToSet)
                    .setAttestationSigner(signingKey))
                )
                .assertNext(result -> {
                    assertEquals(PolicyModification.UPDATED, result.getPolicyResolution());
                })
                .expectComplete()
                .verify();

            StepVerifier.create(client.getAttestationPolicy(attestationType))
                .assertNext(result -> assertEquals(policyToSet, result))
                .expectComplete()
                .verify();
        }

        // Try setting attestation policy with an arbitrary signing key - this should be allowed
        // in AAD mode.
        if (clientType == ClientTypes.AAD) {
            X509Certificate certificate = getPolicySigningCertificate0();
            PrivateKey key = getPolicySigningKey0();

            AttestationSigningKey signingKey = new AttestationSigningKey()
                .setPrivateKey(key)
                .setCertificate(certificate);

            // Test setting the policy. This works for both AAD and Isolated mode.
            StepVerifier.create(client.setAttestationPolicy(attestationType, new AttestationPolicySetOptions()
                    .setPolicy("version=1.0; authorizationrules{=> permit();}; issuancerules{};")
                    .setAttestationSigner(signingKey)))
                .assertNext(result -> {
                    assertEquals(PolicyModification.UPDATED, result.getPolicyResolution());
                })
                .expectComplete()
                .verify();
        }
        // Try setting attestation policy with an unsigned policy token - this should be allowed
        // in AAD mode.
        if (clientType == ClientTypes.AAD) {
            // Test setting the policy. This works for both AAD and Isolated mode.
            StepVerifier.create(client.setAttestationPolicy(attestationType, new AttestationPolicySetOptions()
                    .setPolicy("version=1.0; authorizationrules{=> permit();}; issuancerules{};")))
                .assertNext(result -> {
                    assertEquals(PolicyModification.UPDATED, result.getPolicyResolution());
                })
                .expectComplete()
                .verify();
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getPolicyClients")
    void testSetAttestationPolicyAadAsync(HttpClient httpClient, String clientUri, AttestationType attestationType) {
        ClientTypes clientType = classifyClient(clientUri);
        // We can't set attestation policy on the shared client, so just exit early.
        assumeTrue(clientType != ClientTypes.SHARED, "This test does not work on shared instances.");
        assumeTrue(clientType != ClientTypes.ISOLATED, "This test does not work on isolated instances.");

        AttestationAdministrationClientBuilder attestationBuilder = getAdministrationBuilder(httpClient, clientUri);
        AttestationAdministrationAsyncClient client = attestationBuilder.buildAsyncClient();

        String policyToSet = "version=1.0; authorizationrules{=> permit();}; issuancerules{};";
        // Test setting the policy. This works for both AAD and Isolated mode.
        StepVerifier.create(client.setAttestationPolicy(attestationType, policyToSet))
            .assertNext(result -> {
                assertEquals(PolicyModification.UPDATED, result.getPolicyResolution());
            })
            .expectComplete()
            .verify();

        StepVerifier.create(client.getAttestationPolicy(attestationType))
            .assertNext(result -> assertEquals(policyToSet, result))
            .expectComplete()
            .verify();

    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getPolicyClients")
    void testSetAttestationPolicyWithResponseAsync(HttpClient httpClient, String clientUri, AttestationType attestationType) {
        ClientTypes clientType = classifyClient(clientUri);
        // We can't set attestation policy on the shared client, so just exit early.
        assumeTrue(clientType != ClientTypes.SHARED, "This test does not work on shared instances.");

        AttestationAdministrationClientBuilder attestationBuilder = getAdministrationBuilder(httpClient, clientUri);
        AttestationAdministrationAsyncClient client = attestationBuilder.buildAsyncClient();

        // AAD or isolated: We want to try setting policy with the Isolated signing certificate.
        if (clientType == ClientTypes.AAD || clientType == ClientTypes.ISOLATED) {
            X509Certificate certificate = getIsolatedSigningCertificate();
            PrivateKey key = getIsolatedSigningKey();

            AttestationSigningKey signingKey = new AttestationSigningKey()
                .setPrivateKey(key)
                .setCertificate(certificate);

            // Test setting the policy. This works for both AAD and Isolated mode.
            StepVerifier.create(client.setAttestationPolicyWithResponse(attestationType, new AttestationPolicySetOptions()
                    .setPolicy("version=1.0; authorizationrules{=> permit();}; issuancerules{};")
                    .setAttestationSigner(signingKey)))
                .assertNext(response -> {
                    assertEquals(PolicyModification.UPDATED, response.getValue().getPolicyResolution());
                    assertEquals(1, response.getValue().getPolicySigner().getCertificates().size());
                    assertDoesNotThrow(() -> {
                        assertEquals(certificate.toString(), response.getValue().getPolicySigner().getCertificates().get(0).toString());
                    });
                })
                .expectComplete()
                .verify();
        }

        // Try setting attestation policy with an arbitrary signing key - this should be allowed
        // in AAD mode.
        if (clientType == ClientTypes.AAD) {
            X509Certificate certificate = getPolicySigningCertificate0();
            PrivateKey key = getPolicySigningKey0();

            AttestationSigningKey signingKey = new AttestationSigningKey()
                .setPrivateKey(key)
                .setCertificate(certificate);

            // Test setting the policy. This works for both AAD and Isolated mode.
            StepVerifier.create(client.setAttestationPolicyWithResponse(attestationType, new AttestationPolicySetOptions()
                    .setPolicy("version=1.0; authorizationrules{=> permit();}; issuancerules{};")
                    .setAttestationSigner(signingKey)))
                .assertNext(response -> {
                    assertEquals(PolicyModification.UPDATED, response.getValue().getPolicyResolution());
                })
                .expectComplete()
                .verify();
        }
        // Try setting attestation policy with an unsigned policy token - this should be allowed
        // in AAD mode.
        if (clientType == ClientTypes.AAD) {
            // Test setting the policy. This works for both AAD and Isolated mode.
            StepVerifier.create(client.setAttestationPolicyWithResponse(attestationType, new AttestationPolicySetOptions()
                    .setPolicy("version=1.0; authorizationrules{=> permit();}; issuancerules{};")))
                .assertNext(response -> {
                    assertEquals(PolicyModification.UPDATED, response.getValue().getPolicyResolution());
                })
                .expectComplete()
                .verify();
        }
    }

    /**
     * Retrieve the policy set objects for the specified client URI.
     * @param clientUri - client URI
     * @return An array of JOSEObjects which should be used to set attestation policy on the client.
     */
    private ArrayList<JOSEObject> getPolicySetObjects(String clientUri, String signingCertificateBase64, JWSSigner signer) {
        ClientTypes clientType = classifyClient(clientUri);
        ArrayList<JOSEObject> policySetObjects = new ArrayList<>();

        // Minimal policy document to set.
        String policyToSet = "version=1.0; authorizationrules { => permit(); }; issuancerules {};";
        byte[] encodedPolicyToSetUtf8 = Base64.getUrlEncoder().withoutPadding().encode(policyToSet.getBytes(StandardCharsets.UTF_8));
        String encodedPolicyToSet = new String(encodedPolicyToSetUtf8, StandardCharsets.UTF_8);
        // Form the JSON policy body from the base64url encoded policy, wrapped in a JSON object.

        Payload setPolicyPayload = new Payload(new JSONObject().appendField("AttestationPolicy", encodedPolicyToSet));

        PlainObject plainObject = new PlainObject(setPolicyPayload);
        JWSObject securedObject;

        List<com.nimbusds.jose.util.Base64> certs = new ArrayList<>();
        certs.add(new com.nimbusds.jose.util.Base64(signingCertificateBase64));
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
            .x509CertChain(certs)
            .build();

        securedObject = new JWSObject(header, setPolicyPayload);
        try {
            securedObject.sign(signer);
        } catch (JOSEException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.toString()));
        }

        switch (clientType) {
            case AAD:
                policySetObjects.add(plainObject);
                policySetObjects.add(securedObject);
                break;
            case ISOLATED:
                policySetObjects.add(securedObject);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + classifyClient(clientUri));
        }
        return policySetObjects;
    }
}

