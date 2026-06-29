// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.filter;

import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadProfileProperties;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.constants.AadJwtClaimNames;
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
import org.mockito.Mockito;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Test
    void tenantIdValidationSucceedsWhenMatchingConfiguredTenant() throws Exception {
        // Setup: create mocked AadAuthenticationProperties with configured tenant ID
        AadAuthenticationProperties properties = Mockito.mock(AadAuthenticationProperties.class);
        AadProfileProperties profileProperties = Mockito.mock(AadProfileProperties.class);
        Mockito.when(properties.getProfile()).thenReturn(profileProperties);
        Mockito.when(profileProperties.getTenantId()).thenReturn("test");

        AadJwtClaimsVerifier verifier = new AadJwtClaimsVerifier(properties, false, java.util.Collections.emptySet());

        // Create JWT claims set with matching tenant ID
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("https://sts.windows.net/test")
                .claim(AadJwtClaimNames.TID, "test")
                .build();

        assertThatCode(() -> verifier.verify(claimsSet, null)).doesNotThrowAnyException();
    }

    @Test
    void tenantIdValidationFailsWhenMismatchedTenant() throws Exception {
        // Setup: create mocked AadAuthenticationProperties with configured tenant ID "test"
        AadAuthenticationProperties properties = Mockito.mock(AadAuthenticationProperties.class);
        AadProfileProperties profileProperties = Mockito.mock(AadProfileProperties.class);
        Mockito.when(properties.getProfile()).thenReturn(profileProperties);
        Mockito.when(profileProperties.getTenantId()).thenReturn("test");

        AadJwtClaimsVerifier verifier = new AadJwtClaimsVerifier(properties, false, java.util.Collections.emptySet());

        // Create JWT claims set with different tenant ID (mismatched)
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("https://sts.windows.net/other-tenant-id")
                .claim(AadJwtClaimNames.TID, "other-tenant-id")
                .build();

        assertThatThrownBy(() -> verifier.verify(claimsSet, null))
            .isInstanceOf(BadJWTException.class)
            .hasMessageContaining("Invalid token tenant");
    }

    @Test
    void tenantIdValidationSkippedWhenNoTenantConfigured() throws Exception {
        // Setup: create mocked AadAuthenticationProperties with default multi-tenant value "common"
        AadAuthenticationProperties properties = Mockito.mock(AadAuthenticationProperties.class);
        AadProfileProperties profileProperties = Mockito.mock(AadProfileProperties.class);
        Mockito.when(properties.getProfile()).thenReturn(profileProperties);
        Mockito.when(profileProperties.getTenantId()).thenReturn("common");

        AadJwtClaimsVerifier verifier = new AadJwtClaimsVerifier(properties, false, java.util.Collections.emptySet());

        // Create JWT claims set with any tenant ID - should be accepted since "common" is multi-tenant
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("https://sts.windows.net/any-tenant")
                .claim(AadJwtClaimNames.TID, "any-tenant")
                .build();

        assertThatCode(() -> verifier.verify(claimsSet, null)).doesNotThrowAnyException();
    }

    @Test
    void tenantIdValidationSkippedWhenOrganizationsConfigured() throws Exception {
        // Setup: create mocked AadAuthenticationProperties with multi-tenant value "organizations"
        AadAuthenticationProperties properties = Mockito.mock(AadAuthenticationProperties.class);
        AadProfileProperties profileProperties = Mockito.mock(AadProfileProperties.class);
        Mockito.when(properties.getProfile()).thenReturn(profileProperties);
        Mockito.when(profileProperties.getTenantId()).thenReturn("organizations");

        AadJwtClaimsVerifier verifier = new AadJwtClaimsVerifier(properties, false, java.util.Collections.emptySet());

        // Create JWT claims set with any tenant ID
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("https://sts.windows.net/any-tenant")
                .claim(AadJwtClaimNames.TID, "any-tenant")
                .build();

        assertThatCode(() -> verifier.verify(claimsSet, null)).doesNotThrowAnyException();
    }

    @Test
    void tenantIdValidationSkippedWhenConsumersConfigured() throws Exception {
        // Setup: create mocked AadAuthenticationProperties with multi-tenant value "consumers"
        AadAuthenticationProperties properties = Mockito.mock(AadAuthenticationProperties.class);
        AadProfileProperties profileProperties = Mockito.mock(AadProfileProperties.class);
        Mockito.when(properties.getProfile()).thenReturn(profileProperties);
        Mockito.when(profileProperties.getTenantId()).thenReturn("consumers");

        AadJwtClaimsVerifier verifier = new AadJwtClaimsVerifier(properties, false, java.util.Collections.emptySet());

        // Create JWT claims set with any tenant ID
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("https://sts.windows.net/any-tenant")
                .claim(AadJwtClaimNames.TID, "any-tenant")
                .build();

        assertThatCode(() -> verifier.verify(claimsSet, null)).doesNotThrowAnyException();
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
