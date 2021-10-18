// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.http.HttpClient;
import com.azure.security.attestation.models.AttestationCertificateManagementBody;
import com.azure.security.attestation.models.JsonWebKey;
import com.azure.security.attestation.models.PolicyCertificatesModifyResponse;
import com.azure.security.attestation.models.PolicyCertificatesResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jose.shaded.json.JSONObject;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class AttestationPolicyManagementTests extends AttestationClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testGetPolicyManagementCertificates(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getAdministrationBuilder(httpClient, clientUri);
        PolicyCertificatesClient client = attestationBuilder.buildPolicyCertificatesClient();

        PolicyCertificatesResponse response = client.get();
        JWTClaimsSet claims = verifyAttestationToken(httpClient, clientUri, response.getToken()).block();

        assertNotNull(claims);
        verifyGetPolicyCertificatesResponse(clientUri, claims);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testGetPolicyManagementCertificatesAsync(HttpClient httpClient, String clientUri) {
        AttestationClientBuilder attestationBuilder = getAdministrationBuilder(httpClient, clientUri);
        PolicyCertificatesAsyncClient client = attestationBuilder.buildPolicyCertificatesAsyncClient();

        StepVerifier.create(client.get()
                .flatMap(response -> verifyAttestationToken(httpClient, clientUri, response.getToken())))
            .assertNext(claims -> verifyGetPolicyCertificatesResponse(clientUri, claims))
            .expectComplete()
            .verify();


    }


    /**
     * Verify the response to a get policy management certificates API call.
     * @param clientUri URI for client - used to determine the expected response.
     * @param claims Claims inside policy certificate token to be verified.
     */
    private void verifyGetPolicyCertificatesResponse(String clientUri, JWTClaimsSet claims) {
        assertTrue(claims.getClaims().containsKey("x-ms-policy-certificates"));
        Object policyCertificates = claims.getClaims().get("x-ms-policy-certificates");
        assertTrue(policyCertificates instanceof JSONObject);
        JSONObject certificates = (JSONObject) policyCertificates;
        assertTrue(certificates.containsKey("keys"));
        assertTrue(certificates.get("keys") instanceof JSONArray);
        JSONArray certificateArray = (JSONArray) certificates.get("keys");

        ClientTypes clientType = classifyClient(clientUri);
        if (clientType == ClientTypes.SHARED || clientType == ClientTypes.AAD) {
            assertEquals(0, certificateArray.size());
        } else {
            assertNotEquals(0, certificateArray.size());
            for (Object certObject : certificateArray) {
                assertTrue(certObject instanceof JSONObject);
                JSONObject cert = (JSONObject) certObject;
                assertEquals("RSA", cert.get("kty").toString());
                assertEquals("RS256", cert.get("alg").toString());
                assertTrue(cert.containsKey("x5c"));
                assertTrue(cert.get("x5c") instanceof JSONArray);
                JSONArray x5c = (JSONArray) cert.get("x5c");
                assertTrue(x5c.get(0) instanceof String);
                /* TODO: Verify that the certificate is the isolated certificate */
            }
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

        AttestationClientBuilder attestationBuilder = getAdministrationBuilder(httpClient, clientUri);
        PolicyCertificatesClient client = attestationBuilder.buildPolicyCertificatesClient();

        String signingCertificateBase64 = getIsolatedSigningCertificate();
        String signingKeyBase64 = getIsolatedSigningKey();

        String newPolicyCertificateBase64 = getPolicySigningCertificate0();

        JWSSigner existingSigner;
        existingSigner = getJwsSigner(signingKeyBase64);

        JsonWebKey keyToAdd = new JsonWebKey("RS256");
        keyToAdd.setX5C(Collections.singletonList(newPolicyCertificateBase64));

        AttestationCertificateManagementBody certBody = new AttestationCertificateManagementBody();
        certBody.setPolicyCertificate(keyToAdd);
        ObjectMapper mapper = new ObjectMapper();

        String serializedKey;
        try {
            serializedKey = mapper.writeValueAsString(certBody);
        } catch (JsonProcessingException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.toString()));
        }

        Payload setPolicyPayload = new Payload(serializedKey);

        JWSObject securedObject;

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
            .x509CertChain(Collections.singletonList(new com.nimbusds.jose.util.Base64(signingCertificateBase64)))
            .build();

        securedObject = new JWSObject(header, setPolicyPayload);
        try {
            securedObject.sign(existingSigner);
        } catch (JOSEException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.toString()));
        }

        PolicyCertificatesModifyResponse response = client.add(securedObject.serialize());
        JWTClaimsSet responseClaims;
        responseClaims = verifyAttestationToken(httpClient, clientUri, response.getToken()).block();
        assertNotNull(responseClaims);
        assertTrue(responseClaims.getClaims().containsKey("x-ms-policycertificates-result"));
        assertEquals("IsPresent", responseClaims.getClaims().get("x-ms-policycertificates-result").toString());


        response = client.remove(securedObject.serialize());
        responseClaims = verifyAttestationToken(httpClient, clientUri, response.getToken()).block();
        assertNotNull(responseClaims);
        assertTrue(responseClaims.getClaims().containsKey("x-ms-policycertificates-result"));
        assertEquals("IsAbsent", responseClaims.getClaims().get("x-ms-policycertificates-result").toString());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testSetAttestationPolicyAsync(HttpClient httpClient, String clientUri) {

        ClientTypes clientType = classifyClient(clientUri);
        // This test only works on isolated instances.
        assumeTrue(clientType == ClientTypes.ISOLATED, "This test only works on isolated instances.");

        AttestationClientBuilder attestationBuilder = getAdministrationBuilder(httpClient, clientUri);
        PolicyCertificatesAsyncClient client = attestationBuilder.buildPolicyCertificatesAsyncClient();

        String signingCertificateBase64 = getIsolatedSigningCertificate();
        String signingKeyBase64 = getIsolatedSigningKey();

        String newPolicyCertificateBase64 = getPolicySigningCertificate0();

        JWSSigner existingSigner;
        existingSigner = getJwsSigner(signingKeyBase64);

        JsonWebKey keyToAdd = new JsonWebKey("RS256");
        keyToAdd.setX5C(Collections.singletonList(newPolicyCertificateBase64));

        AttestationCertificateManagementBody certBody = new AttestationCertificateManagementBody();
        certBody.setPolicyCertificate(keyToAdd);
        ObjectMapper mapper = new ObjectMapper();

        String serializedKey;
        try {
            serializedKey = mapper.writeValueAsString(certBody);
        } catch (JsonProcessingException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.toString()));
        }

        Payload setPolicyPayload = new Payload(serializedKey);

        JWSObject securedObject;

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
            .x509CertChain(Collections.singletonList(new com.nimbusds.jose.util.Base64(signingCertificateBase64)))
            .build();

        securedObject = new JWSObject(header, setPolicyPayload);
        try {
            securedObject.sign(existingSigner);
        } catch (JOSEException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.toString()));
        }

        StepVerifier.create(client.add(securedObject.serialize())
            .flatMap(response -> verifyAttestationToken(httpClient, clientUri, response.getToken()))
            .doOnNext(responseClaims -> {
                assertTrue(responseClaims.getClaims().containsKey("x-ms-policycertificates-result"));
                assertEquals("IsPresent", responseClaims.getClaims().get("x-ms-policycertificates-result").toString());
            }).flatMap(policyResponse -> client.remove(securedObject.serialize())
                .flatMap(response -> verifyAttestationToken(httpClient, clientUri, response.getToken())))
                .doOnNext(responseClaims -> {
                    assertTrue(responseClaims.getClaims().containsKey("x-ms-policycertificates-result"));
                    assertEquals("IsAbsent", responseClaims.getClaims().get("x-ms-policycertificates-result").toString());
                }))
            .assertNext(claimsSet -> { })
            .expectComplete()
            .verify();

    }

}

