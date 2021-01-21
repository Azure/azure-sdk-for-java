// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
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
import com.nimbusds.jwt.JWTClaimsSet;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class AttestationPolicyManagementTests extends AttestationClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    public void testGetPolicyManagementCertificates(HttpClient httpClient, String clientUri)
        throws ParseException {

        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);
        PolicyCertificatesClient client = attestationBuilder.buildPolicyCertificatesClient();

        PolicyCertificatesResponse response = client.get();
        JWTClaimsSet claims = verifyAttestationToken(httpClient, clientUri, response.getToken()).block();

        assertNotNull(claims);
        verifyGetPolicyCertificatesResponse(clientUri, claims);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    public void testGetPolicyManagementCertificatesAsync(HttpClient httpClient, String clientUri) {
        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);
        PolicyCertificatesAsyncClient client = attestationBuilder.buildPolicyCertificatesAsyncClient();

        StepVerifier.create(client.get()
                .flatMap(response -> {
                    try {
                        return verifyAttestationToken(httpClient, clientUri, response.getToken());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return null;
                }))
            .assertNext(claims -> verifyGetPolicyCertificatesResponse(clientUri, claims))
            .verifyComplete();
    }


    /**
     * Verify the response to a get policy management certificates API call.
     * @param clientUri URI for client - used to determine the expected response.
     * @param claims Claims inside policy certificate token to be verified.
     */
    private void verifyGetPolicyCertificatesResponse(String clientUri, @NotNull JWTClaimsSet claims) {
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
     * @throws JOSEException Should never be thrown.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    public void testAddAttestationPolicyManagementCertificate(HttpClient httpClient, String clientUri)
        throws JOSEException, InvalidKeySpecException, NoSuchAlgorithmException, ParseException, JsonProcessingException {

        ClientTypes clientType = classifyClient(clientUri);

        // This test only works on isolated instances.
        if (clientType != ClientTypes.ISOLATED) {
            return;
        }

        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);
        PolicyCertificatesClient client = attestationBuilder.buildPolicyCertificatesClient();

        String signingCertificateBase64 = Configuration.getGlobalConfiguration().get("isolatedSigningCertificate");
        String signingKeyBase64 = Configuration.getGlobalConfiguration().get("isolatedSigningKey");

        String newPolicyCertificateBase64 = Configuration.getGlobalConfiguration().get("policySigningCertificate0");
//        String newPolicySigner = Configuration.getGlobalConfiguration().get("policySigningKey0");

        JWSSigner existingSigner = getJwsSigner(signingKeyBase64);

        JsonWebKey keyToAdd = new JsonWebKey("RS256");
        keyToAdd.setX5C(Collections.singletonList(newPolicyCertificateBase64));

        AttestationCertificateManagementBody certBody = new AttestationCertificateManagementBody();
        certBody.setPolicyCertificate(keyToAdd);
        ObjectMapper mapper = new ObjectMapper();

        String serializedKey = mapper.writeValueAsString(certBody);

        Payload setPolicyPayload = new Payload(serializedKey);

        JWSObject securedObject;

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
            .x509CertChain(Collections.singletonList(new com.nimbusds.jose.util.Base64(signingCertificateBase64)))
            .build();

        securedObject = new JWSObject(header, setPolicyPayload);
        securedObject.sign(existingSigner);

        PolicyCertificatesModifyResponse response = client.add(securedObject.serialize());
        JWTClaimsSet responseClaims = verifyAttestationToken(httpClient, clientUri, response.getToken()).block();
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
    public void testSetAttestationPolicyAsync(HttpClient httpClient, String clientUri) throws JOSEException, InvalidKeySpecException, NoSuchAlgorithmException, JsonProcessingException {

        ClientTypes clientType = classifyClient(clientUri);
        // This test only works on isolated instances.
        if (clientType != ClientTypes.ISOLATED) {
            return;
        }

        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);
        PolicyCertificatesAsyncClient client = attestationBuilder.buildPolicyCertificatesAsyncClient();

        String signingCertificateBase64 = Configuration.getGlobalConfiguration().get("isolatedSigningCertificate");
        String signingKeyBase64 = Configuration.getGlobalConfiguration().get("isolatedSigningKey");

        String newPolicyCertificateBase64 = Configuration.getGlobalConfiguration().get("policySigningCertificate0");
//        String newPolicySigner = Configuration.getGlobalConfiguration().get("policySigningKey0");

        JWSSigner existingSigner = getJwsSigner(signingKeyBase64);

        JsonWebKey keyToAdd = new JsonWebKey("RS256");
        keyToAdd.setX5C(Collections.singletonList(newPolicyCertificateBase64));

        AttestationCertificateManagementBody certBody = new AttestationCertificateManagementBody();
        certBody.setPolicyCertificate(keyToAdd);
        ObjectMapper mapper = new ObjectMapper();

        String serializedKey = mapper.writeValueAsString(certBody);

        Payload setPolicyPayload = new Payload(serializedKey);

        JWSObject securedObject;

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
            .x509CertChain(Collections.singletonList(new com.nimbusds.jose.util.Base64(signingCertificateBase64)))
            .build();

        securedObject = new JWSObject(header, setPolicyPayload);
        securedObject.sign(existingSigner);

        StepVerifier.create(client.add(securedObject.serialize())
            .flatMap(response -> {
                try {
                    return verifyAttestationToken(httpClient, clientUri, response.getToken());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return null;
            })
            .doOnNext(responseClaims -> {
                assertTrue(responseClaims.getClaims().containsKey("x-ms-policycertificates-result"));
                assertEquals("IsPresent", responseClaims.getClaims().get("x-ms-policycertificates-result").toString());
            }).flatMap(policyResponse -> client.remove(securedObject.serialize())
                .flatMap(response -> {
                    try {
                        return verifyAttestationToken(httpClient, clientUri, response.getToken());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return null;
                }))
                .doOnNext(responseClaims -> {
                    assertTrue(responseClaims.getClaims().containsKey("x-ms-policycertificates-result"));
                    assertEquals("IsAbsent", responseClaims.getClaims().get("x-ms-policycertificates-result").toString());
                }))
            .assertNext(claimsSet -> { })
            .verifyComplete();
    }

}

