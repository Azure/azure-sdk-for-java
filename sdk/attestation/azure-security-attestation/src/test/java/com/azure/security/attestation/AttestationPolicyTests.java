// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import com.azure.security.attestation.models.AttestationType;
import com.azure.security.attestation.models.PolicyResponse;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import net.minidev.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class AttestationPolicyTests extends AttestationClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getPolicyClients")
    public void testGetAttestationPolicy(HttpClient client, String clientUri, AttestationType attestationType)
        throws ParseException {

        AttestationClientBuilder attestationBuilder = getBuilder(client, clientUri);

        PolicyResponse policyResponse = attestationBuilder.buildPolicyClient().get(attestationType);

        verifyBasicGetAttestationPolicyResponse(client, clientUri, attestationType, policyResponse);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getPolicyClients")
    public void testGetAttestationPolicyAsync(HttpClient client, String clientUri, AttestationType attestationType) {
        AttestationClientBuilder attestationBuilder = getBuilder(client, clientUri);

        StepVerifier.create(attestationBuilder.buildPolicyAsyncClient().get(attestationType))
            .assertNext(response -> assertDoesNotThrow(() -> verifyBasicGetAttestationPolicyResponse(client, clientUri, attestationType, response))).verifyComplete();
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
     * @throws JOSEException Should never be thrown.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getPolicyClients")
    public void testSetAttestationPolicy(HttpClient httpClient, String clientUri, AttestationType attestationType)
        throws JOSEException, InvalidKeySpecException, NoSuchAlgorithmException {

        ClientTypes clientType = classifyClient(clientUri);
        // We can't set attestation policy on the shared client, so just exit early.
        if (clientType.equals(ClientTypes.Shared))
        {
            return;
        }

        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);
        PolicyClient client = attestationBuilder.buildPolicyClient();

        String signingCertificateBase64 = Configuration.getGlobalConfiguration().get("isolatedSigningCertificate");
        String signingKeyBase64 = Configuration.getGlobalConfiguration().get("isolatedSigningKey");

        JWSSigner signer = getJwsSigner(signingKeyBase64);


        try {

            ArrayList<JOSEObject> policySetObjects = getPolicySetObjects(clientUri, signingCertificateBase64, signer);

            for (JOSEObject policyObject : policySetObjects) {
                client.set(attestationType, policyObject.serialize());
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        } finally {
            switch (clientType) {
                case Aad: {
                    client.reset(attestationType, new PlainPolicyResetToken().serialize());
                    break;
                }
                case Isolated:
                    client.reset(attestationType, new SecuredPolicyResetToken(signer, signingCertificateBase64).serialize());
                    break;
            }
        }
    }
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getPolicyClients")
    public void testSetAttestationPolicyAsync(HttpClient httpClient, String clientUri, AttestationType attestationType) throws JOSEException {
        ClientTypes clientType = classifyClient(clientUri);
        // We can't set attestation policy on the shared client, so just exit early.
        if (clientType.equals(ClientTypes.Shared))
        {
            return;
        }

        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);
        PolicyAsyncClient client = attestationBuilder.buildPolicyAsyncClient();
        {
            String signingCertificateBase64 = Configuration.getGlobalConfiguration().get("isolatedSigningCertificate");
            String signingKeyBase64 = Configuration.getGlobalConfiguration().get("isolatedSigningKey");

            JWSSigner signer = Assertions.assertDoesNotThrow(() -> getJwsSigner(signingKeyBase64));

            ArrayList<JOSEObject> policySetObjects = Assertions.assertDoesNotThrow(() -> getPolicySetObjects(clientUri, signingCertificateBase64, signer));

            try
            {
                for (JOSEObject policyObject : policySetObjects) {
                    StepVerifier.create(client.set(attestationType, policyObject.serialize()))
                        .assertNext(response -> {})
                        .verifyComplete();
                }
            } finally {
                switch (clientType) {
                    case Aad: {
                        StepVerifier.create(client.reset(attestationType, new PlainPolicyResetToken().serialize()))
                            .assertNext(response -> {})
                            .verifyComplete();
                        break;
                    }
                    case Isolated: {
                        StepVerifier.create(client.reset(attestationType, new SecuredPolicyResetToken(signer, signingCertificateBase64).serialize()))
                            .assertNext(response -> {})
                            .verifyComplete();
                        break;
                    }
                    default:
                        throw new IllegalStateException("Unexpected value: " + clientType);
                }
            }
        }
    }

    /**
     * Retrieve the policy set objects for the specified client URI.
     * @param clientUri - client URI
     * @return An array of JOSEObjects which should be used to set attestation policy on the client.
     * @throws NoSuchAlgorithmException Thrown if the RSA signing suite is not supported.
     * @throws InvalidKeySpecException Thrown if the configuration returns an invalid key
     * @throws JOSEException Thrown if we can't sign the JSON.
     */
    ArrayList<JOSEObject> getPolicySetObjects(String clientUri, String signingCertificateBase64, JWSSigner signer) throws NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {
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

        {
            List<com.nimbusds.jose.util.Base64> certs = new ArrayList<>();
            certs.add(new com.nimbusds.jose.util.Base64(signingCertificateBase64));
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .x509CertChain(certs)
                .build();

            securedObject = new JWSObject(header, setPolicyPayload);
            securedObject.sign(signer);
        }

        switch (clientType) {
            case Aad: {
                policySetObjects.add(plainObject);
                policySetObjects.add(securedObject);
                break;
            }
            case Isolated: {
                policySetObjects.add(securedObject);
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + classifyClient(clientUri));
        }
        return policySetObjects;
    }

    /**
     * Create a JWS Signer from the specified PKCS8 encoded signing key.
     * @param signingKeyBase64 Base64 encoded PKCS8 encoded RSA Private key.
     * @return JWSSigner created over the specified signing key.
     * @throws NoSuchAlgorithmException - should never  throws this.
     * @throws InvalidKeySpecException - Can throw this if the key is invalid.
     */
    @NotNull
    private JWSSigner getJwsSigner(String signingKeyBase64) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] signingKey = Base64.getDecoder().decode(signingKeyBase64);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(signingKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return new RSASSASigner(privateKey);
    }
}

