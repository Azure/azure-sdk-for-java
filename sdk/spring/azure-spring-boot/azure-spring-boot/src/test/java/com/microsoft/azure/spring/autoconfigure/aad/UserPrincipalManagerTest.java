/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.aad;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jwt.proc.BadJWTException;
import junitparams.FileParameters;
import junitparams.JUnitParamsRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@RunWith(JUnitParamsRunner.class)
public class UserPrincipalManagerTest {

    private static ImmutableJWKSet immutableJWKSet;

    @BeforeClass
    public static void setupClass() throws Exception {
        final X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(Files.newInputStream(Paths.get("src/test/resources/test-public-key.txt")));
        immutableJWKSet = new ImmutableJWKSet<>(new JWKSet(JWK.parse(
                cert)));
    }

    private UserPrincipalManager userPrincipalManager;


    @Test
    public void testAlgIsTakenFromJWT() throws Exception {
        userPrincipalManager = new UserPrincipalManager(immutableJWKSet);
        final UserPrincipal userPrincipal = userPrincipalManager.buildUserPrincipal(
                new String(Files.readAllBytes(
                        Paths.get("src/test/resources/jwt-signed.txt")), StandardCharsets.UTF_8));
        assertThat(userPrincipal).isNotNull().extracting(UserPrincipal::getIssuer, UserPrincipal::getSubject)
                .containsExactly("https://sts.windows.net/test", "test@example.com");
    }

    @Test
    public void invalidIssuer() {
        userPrincipalManager = new UserPrincipalManager(immutableJWKSet);
        assertThatCode(() -> userPrincipalManager.buildUserPrincipal(
                new String(Files.readAllBytes(
                        Paths.get("src/test/resources/jwt-bad-issuer.txt")), StandardCharsets.UTF_8)))
                .isInstanceOf(BadJWTException.class);
    }

    @Test
    //TODO: add more generated tokens with other valid issuers to this file. Didn't manage to generate them
    @FileParameters("src/test/resources/jwt-valid-issuer.txt")
    public void validIssuer(final String token) {
        userPrincipalManager = new UserPrincipalManager(immutableJWKSet);
        assertThatCode(() -> userPrincipalManager.buildUserPrincipal(token))
                .doesNotThrowAnyException();
    }

    @Test
    public void nullIssuer() {
        userPrincipalManager = new UserPrincipalManager(immutableJWKSet);
        assertThatCode(() -> userPrincipalManager.buildUserPrincipal(
                new String(Files.readAllBytes(
                        Paths.get("src/test/resources/jwt-null-issuer.txt")), StandardCharsets.UTF_8)))
                .isInstanceOf(BadJWTException.class);
    }
}
