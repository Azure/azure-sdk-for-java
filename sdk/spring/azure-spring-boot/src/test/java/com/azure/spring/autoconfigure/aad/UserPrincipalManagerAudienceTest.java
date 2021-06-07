// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.azure.spring.aad.AADAuthorizationServerEndpoints;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Resource;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserPrincipalManagerAudienceTest {

    private static final String FAKE_CLIENT_ID = "dsflkjsdflkjsdf";
    private static final String FAKE_APPLICATION_URI = "https://oihiugjuzfvbhg";

    private JWSSigner signer;
    private String jwkString;
    private ResourceRetriever resourceRetriever;

    private AADAuthorizationServerEndpoints endpoints;
    private AADAuthenticationProperties properties;
    private UserPrincipalManager userPrincipalManager;

    @BeforeEach
    public void setupKeys() throws NoSuchAlgorithmException {
        final KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);

        final KeyPair kp = kpg.genKeyPair();
        final RSAPrivateKey privateKey = (RSAPrivateKey) kp.getPrivate();

        signer = new RSASSASigner(privateKey);

        final RSAKey rsaJWK = new RSAKey.Builder((RSAPublicKey) kp.getPublic())
            .privateKey((RSAPrivateKey) kp.getPrivate())
            .keyID("1")
            .build();
        final JWKSet jwkSet = new JWKSet(rsaJWK);
        jwkString = jwkSet.toString();

        resourceRetriever = url -> new Resource(jwkString, "application/json");

        endpoints = mock(AADAuthorizationServerEndpoints.class);
        properties = new AADAuthenticationProperties();
        properties.setClientId(FAKE_CLIENT_ID);
        properties.setAppIdUri(FAKE_APPLICATION_URI);
        when(endpoints.jwkSetEndpoint()).thenReturn("file://dummy");
    }

    @Test
    public void allowApplicationUriAsAudience() throws JOSEException {
        final JWTClaimsSet claimsSetOne = new JWTClaimsSet.Builder()
            .subject("foo")
            .issueTime(Date.from(Instant.now().minusSeconds(60)))
            .issuer("https://sts.windows.net/")
            .audience(FAKE_CLIENT_ID)
            .build();
        final SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSetOne);
        signedJWT.sign(signer);

        final String orderTwo = signedJWT.serialize();
        userPrincipalManager = new UserPrincipalManager(endpoints, properties,
            resourceRetriever, true);
        assertThatCode(() -> userPrincipalManager.buildUserPrincipal(orderTwo))
            .doesNotThrowAnyException();
    }

    @Test
    public void allowClientIdAsAudience() throws JOSEException {
        final JWTClaimsSet claimsSetOne = new JWTClaimsSet.Builder()
            .subject("foo")
            .issueTime(Date.from(Instant.now().minusSeconds(60)))
            .issuer("https://sts.windows.net/")
            .audience(FAKE_APPLICATION_URI)
            .build();
        final SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSetOne);
        signedJWT.sign(signer);

        final String orderTwo = signedJWT.serialize();
        userPrincipalManager = new UserPrincipalManager(endpoints, properties,
            resourceRetriever, true);
        assertThatCode(() -> userPrincipalManager.buildUserPrincipal(orderTwo))
            .doesNotThrowAnyException();
    }

    @Test
    public void failWithUnkownAudience() throws JOSEException {
        final JWTClaimsSet claimsSetOne = new JWTClaimsSet.Builder()
            .subject("foo")
            .issueTime(Date.from(Instant.now().minusSeconds(60)))
            .issuer("https://sts.windows.net/")
            .audience("unknown audience")
            .build();
        final SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSetOne);
        signedJWT.sign(signer);

        final String orderTwo = signedJWT.serialize();
        userPrincipalManager = new UserPrincipalManager(endpoints, properties,
            resourceRetriever, true);
        assertThatCode(() -> userPrincipalManager.buildUserPrincipal(orderTwo))
            .hasMessageContaining("Invalid token audience.");
    }

    @Test
    public void failOnInvalidSiganture() throws JOSEException {
        final JWTClaimsSet claimsSetOne = new JWTClaimsSet.Builder()
            .subject("foo")
            .issueTime(Date.from(Instant.now().minusSeconds(60)))
            .issuer("https://sts.windows.net/")
            .audience(FAKE_APPLICATION_URI)
            .build();
        final SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSetOne);
        signedJWT.sign(signer);

        final String orderTwo = signedJWT.serialize();
        final String invalidToken = orderTwo.substring(0, orderTwo.length() - 5);

        userPrincipalManager = new UserPrincipalManager(endpoints, properties,
            resourceRetriever, true);
        assertThatCode(() -> userPrincipalManager.buildUserPrincipal(invalidToken))
            .hasMessageContaining("JWT rejected: Invalid signature");
    }
}
