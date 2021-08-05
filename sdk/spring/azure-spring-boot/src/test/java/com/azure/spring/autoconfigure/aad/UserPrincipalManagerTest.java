// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.BadJWTException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;


public class UserPrincipalManagerTest {

    private static ImmutableJWKSet<SecurityContext> immutableJWKSet;

    @BeforeAll
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
        assertThatCode(() -> userPrincipalManager.buildUserPrincipal(readJwtValidIssuerTxt()))
            .isInstanceOf(BadJWTException.class);
    }

    //TODO: add more generated tokens with other valid issuers to this file. Didn't manage to generate them
    @ParameterizedTest
    @MethodSource("readJwtValidIssuerTxtStream")
    public void validIssuer(final String token) {
        userPrincipalManager = new UserPrincipalManager(immutableJWKSet);
        assertThatCode(() -> userPrincipalManager.buildUserPrincipal(token))
            .doesNotThrowAnyException();
    }

    @Test
    public void nullIssuer() {
        userPrincipalManager = new UserPrincipalManager(immutableJWKSet);
        assertThatCode(() -> userPrincipalManager.buildUserPrincipal(readJwtValidIssuerTxt()))
            .isInstanceOf(BadJWTException.class);
    }

    private String readJwtValidIssuerTxt() throws IOException {
        return new String(Files.readAllBytes(
            Paths.get("src/test/resources/jwt-null-issuer.txt")), StandardCharsets.UTF_8);
    }

    private static Stream<String> readJwtValidIssuerTxtStream() throws IOException {
        return Stream.of(new String(Files.readAllBytes(
            Paths.get("src/test/resources/jwt-valid-issuer.txt")), StandardCharsets.UTF_8));
    }

}
