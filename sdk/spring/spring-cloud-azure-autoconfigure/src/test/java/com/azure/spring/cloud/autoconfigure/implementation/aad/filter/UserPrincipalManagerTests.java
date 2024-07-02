// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.filter;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class UserPrincipalManagerTests {

    private static ImmutableJWKSet<SecurityContext> immutableJWKSet;

    @BeforeAll
    static void setupClass() throws Exception {
        final X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509")
                                                                         .generateCertificate(Files.newInputStream(Paths.get("src/test/resources/aad/test-public-key.txt")));
        immutableJWKSet = new ImmutableJWKSet<>(new JWKSet(JWK.parse(cert)));
    }

    private UserPrincipalManager userPrincipalManager;


    @Test
    void testAlgIsTakenFromJWT() throws Exception {
        userPrincipalManager = new UserPrincipalManager(immutableJWKSet);
        final UserPrincipal userPrincipal = userPrincipalManager.buildUserPrincipal(
            readFileToString("src/test/resources/aad/jwt-signed.txt"));
        assertThat(userPrincipal).isNotNull().extracting(UserPrincipal::getIssuer, UserPrincipal::getSubject)
                                 .containsExactly("https://sts.windows.net/test", "test@example.com");
    }

    @Test
    void invalidIssuer() {
        userPrincipalManager = new UserPrincipalManager(immutableJWKSet);
        assertThatCode(() -> userPrincipalManager.buildUserPrincipal(readJwtValidIssuerTxt()))
            .isInstanceOf(BadJWTException.class);
    }

    //TODO: add more generated tokens with other valid issuers to this file. Didn't manage to generate them
    @ParameterizedTest
    @MethodSource("readJwtValidIssuerTxtStream")
    void validIssuer(final String token) {
        userPrincipalManager = new UserPrincipalManager(immutableJWKSet);
        assertThatCode(() -> userPrincipalManager.buildUserPrincipal(token))
            .doesNotThrowAnyException();
    }

    @Test
    void nullIssuer() {
        userPrincipalManager = new UserPrincipalManager(immutableJWKSet);
        assertThatCode(() -> userPrincipalManager.buildUserPrincipal(readJwtValidIssuerTxt()))
            .isInstanceOf(BadJWTException.class);
    }

    @Test
    void getRolesTest() {
        rolesExtractedAsExpected(null, new ArrayList<>());
        rolesExtractedAsExpected("role1", Arrays.asList("role1"));
        rolesExtractedAsExpected(Arrays.asList("role1", "role2"), Arrays.asList("role1", "role2"));
        rolesExtractedAsExpected(new HashSet<>(Arrays.asList("role1", "role2")), Arrays.asList("role1", "role2"));
    }

    private void rolesExtractedAsExpected(Object rolesClaimValue, Collection<String> expected) {
        JWTClaimsSet set = new JWTClaimsSet.Builder()
                .claim("roles", rolesClaimValue)
                .build();
        Set<String> actual = new UserPrincipalManager(null).getRoles(set);
        assertEquals(expected.size(), actual.size());
        assertTrue(expected.containsAll(actual));
        assertTrue(actual.containsAll(expected));
    }

    private String readJwtValidIssuerTxt() {
        return readFileToString("src/test/resources/aad/jwt-null-issuer.txt");
    }

    private static Stream<String> readJwtValidIssuerTxtStream() {
        return Stream.of(readFileToString("src/test/resources/aad/jwt-valid-issuer.txt"));
    }

    private static String readFileToString(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
