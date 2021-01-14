// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.http.HttpClient;
import com.azure.security.attestation.models.AttestationType;
import com.azure.security.attestation.models.JsonWebKey;
import com.azure.security.attestation.models.JsonWebKeySet;
import com.azure.security.attestation.models.PolicyResponse;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObject;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.PlainObject;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.UnsupportedEncodingException;
import java.security.PublicKey;
import java.security.SignedObject;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class AttestationPolicyTests extends AttestationClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getPolicyClients")
    public void testGetAttestationPolicy(HttpClient client, String clientUri, AttestationType attestationType)
        throws ParseException, CertificateException, JOSEException, UnsupportedEncodingException {

        AttestationClientBuilder attestationBuilder = getBuilder(client, clientUri);

        PolicyResponse policyResponse = attestationBuilder.buildPolicyClient().get(attestationType);

        verifyBasicGetAttestationPolicyResponse(client, clientUri, attestationType, policyResponse);
    }
}

