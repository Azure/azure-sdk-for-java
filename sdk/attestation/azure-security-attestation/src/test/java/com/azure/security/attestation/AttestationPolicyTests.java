// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.http.HttpClient;
import com.azure.security.attestation.models.AttestationType;
import com.azure.security.attestation.models.PolicyResponse;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.PlainObject;
import com.nimbusds.jwt.JWTClaimsSet;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class AttestationPolicyTests extends AttestationClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getPolicyClients")
    void testGetAttestationPolicy(HttpClient client, String clientUri, AttestationType attestationType) {

        AttestationClientBuilder attestationBuilder = getAdministrationBuilder(client, clientUri);

        PolicyResponse policyResponse = attestationBuilder.buildPolicyClient().get(attestationType);

        StepVerifier.create(verifyBasicGetAttestationPolicyResponse(client, clientUri, attestationType, policyResponse))
            .expectComplete()
            .verify();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getPolicyClients")
    void testGetAttestationPolicyAsync(HttpClient client, String clientUri, AttestationType attestationType) {
        AttestationClientBuilder attestationBuilder = getAdministrationBuilder(client, clientUri);

        StepVerifier.create(attestationBuilder.buildPolicyAsyncClient().get(attestationType))
            .assertNext(response -> assertDoesNotThrow(() -> verifyBasicGetAttestationPolicyResponse(client, clientUri, attestationType, response)))
            .expectComplete()
            .verify();
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

        AttestationClientBuilder attestationBuilder = getAdministrationBuilder(httpClient, clientUri);
        PolicyClient client = attestationBuilder.buildPolicyClient();

        String signingCertificateBase64 = getIsolatedSigningCertificate();
        String signingKeyBase64 = getIsolatedSigningKey();

        JWSSigner signer = getJwsSigner(signingKeyBase64);


        try {

            ArrayList<JOSEObject> policySetObjects = getPolicySetObjects(clientUri, signingCertificateBase64, signer);

            for (JOSEObject policyObject : policySetObjects) {
                PolicyResponse response = client.set(attestationType, policyObject.serialize());
                JWTClaimsSet responseClaims = verifyAttestationToken(httpClient, clientUri, response.getToken()).block();
                assertNotNull(responseClaims);
                assertTrue(responseClaims.getClaims().containsKey("x-ms-policy-result"));
                assertEquals("Updated", responseClaims.getClaims().get("x-ms-policy-result").toString());

            }
        } finally {
            PolicyResponse resetResponse = null;
            switch (clientType) {
                case AAD:
                    resetResponse = client.reset(attestationType, new PlainPolicyResetToken().serialize());
                    break;
                case ISOLATED:
                    resetResponse = client.reset(attestationType, new SecuredPolicyResetToken(signer, signingCertificateBase64).serialize());
                    break;
                default:
                    fail("Cannot ever hit this - unknown client type: " + clientType.toString());
            }
            JWTClaimsSet resetClaims = verifyAttestationToken(httpClient, clientUri, resetResponse.getToken()).block();
            assertNotNull(resetClaims);
            assertTrue(resetClaims.getClaims().containsKey("x-ms-policy-result"));
            assertEquals("Removed", resetClaims.getClaims().get("x-ms-policy-result").toString());

        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getPolicyClients")
    void testSetAttestationPolicyAsync(HttpClient httpClient, String clientUri, AttestationType attestationType) {
        ClientTypes clientType = classifyClient(clientUri);
        // We can't set attestation policy on the shared client, so just exit early.
        assumeTrue(clientType != ClientTypes.SHARED, "This test does not work on shared instances.");

        AttestationClientBuilder attestationBuilder = getAdministrationBuilder(httpClient, clientUri);
        PolicyAsyncClient client = attestationBuilder.buildPolicyAsyncClient();

        String signingCertificateBase64 = getIsolatedSigningCertificate();
        String signingKeyBase64 = getIsolatedSigningKey();

        JWSSigner signer = Assertions.assertDoesNotThrow(() -> getJwsSigner(signingKeyBase64));

        ArrayList<JOSEObject> policySetObjects = Assertions.assertDoesNotThrow(() -> getPolicySetObjects(clientUri, signingCertificateBase64, signer));

        for (JOSEObject policyObject : policySetObjects) {
            StepVerifier.create(client.set(attestationType, policyObject.serialize())
                .flatMap(response -> verifyAttestationToken(httpClient, clientUri, response.getToken())).doOnNext(responseClaims -> {
                    assertTrue(responseClaims.getClaims().containsKey("x-ms-policy-result"));
                    assertEquals("Updated", responseClaims.getClaims().get("x-ms-policy-result").toString());
                }).flatMap(responseClaims -> {
                    String resetToken;
                    switch (clientType) {
                        case AAD:
                            resetToken = new PlainPolicyResetToken().serialize();
                            break;
                        case ISOLATED:
                            resetToken = new SecuredPolicyResetToken(signer, signingCertificateBase64).serialize();
                            break;
                        default:
                            throw new RuntimeException("Cannot ever hit this - unknown client type: " + clientType.toString());
                    }
                    return client.reset(attestationType, resetToken)
                            .flatMap(response -> verifyAttestationToken(httpClient, clientUri, response.getToken()));
                })
                .doOnNext(responseClaims -> {
                    assertTrue(responseClaims.getClaims().containsKey("x-ms-policy-result"));
                    assertEquals("Removed", responseClaims.getClaims().get("x-ms-policy-result").toString());
                }))
                .assertNext(claimSet -> { })
                .expectComplete()
                .verify();
        }
    }

    /**
     * Verifies the basic response for a Get Attestation Policy API - this simply verifies that the server
     * returns a valid JWT and that the JWT contains a base64url encoded attestation policy.
     * @param client HTTP Client, used when retrieving signing certificates.
     * @param clientUri Client base URI - used when retrieving client signing certificates.
     * @param attestationType attestation type - policy results vary by attestation type.
     * @param policyResponse response from the getPolicy API.
     */
    private Mono<Void> verifyBasicGetAttestationPolicyResponse(HttpClient client, String clientUri, AttestationType attestationType, PolicyResponse policyResponse) {
        assertNotNull(policyResponse);
        assertNotNull(policyResponse.getToken());

        return verifyAttestationToken(client, clientUri, policyResponse.getToken())
            .flatMap(claims -> {
                if (claims != null) {

                    String policyDocument = claims.getClaims().get("x-ms-policy").toString();

                    JOSEObject policyJose;
                    try {
                        policyJose = JOSEObject.parse(policyDocument);
                    } catch (ParseException e) {
                        throw logger.logExceptionAsError(new RuntimeException(e.toString()));
                    }
                    assertNotNull(policyJose);
                    Map<String, Object> jsonObject = policyJose.getPayload().toJSONObject();
                    if (jsonObject != null) {
                        assertTrue(jsonObject.containsKey("AttestationPolicy"));
                        String base64urlPolicy = jsonObject.get("AttestationPolicy").toString();

                        byte[] attestationPolicyUtf8 = Base64.getUrlDecoder().decode(base64urlPolicy);
                        String attestationPolicy;
                        attestationPolicy = new String(attestationPolicyUtf8, StandardCharsets.UTF_8);

                        assertNotNull(attestationPolicy);
                    } else {
                        assertEquals("Tpm", attestationType.toString());
                    }
                } else {
                    // TPM is allowed to have an empty attestation policy, all the other AttestationTypes have policies.
                    assertEquals("Tpm", attestationType.toString());
                }
                return Mono.empty();
            });
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

