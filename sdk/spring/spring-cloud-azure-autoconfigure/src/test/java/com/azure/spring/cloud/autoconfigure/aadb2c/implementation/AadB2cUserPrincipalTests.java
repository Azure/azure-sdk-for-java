// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c.implementation;

import com.azure.spring.cloud.autoconfigure.aad.implementation.oauth2.AadOAuth2AuthenticatedPrincipal;
import com.azure.spring.cloud.autoconfigure.aad.AadJwtBearerTokenAuthenticationConverter;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AadB2cUserPrincipalTests {

    private Jwt jwt;
    private Map<String, Object> claims;
    private Map<String, Object> headers;
    private JSONArray jsonArray = new JSONArray().appendElement("User.read").appendElement("User.write");

    @BeforeEach
    void init() {
        claims = new HashMap<>();
        claims.put("iss", "fake-issuer");
        claims.put("tid", "fake-tid");

        headers = new HashMap<>();
        headers.put("kid", "kg2LYs2T0CTjIfj4rt6JIynen38");

        jwt = mock(Jwt.class);
        when(jwt.getClaim("scp")).thenReturn("Order.read Order.write");
        when(jwt.getClaim("roles")).thenReturn(jsonArray);
        when(jwt.getTokenValue()).thenReturn("fake-token-value");
        when(jwt.getIssuedAt()).thenReturn(Instant.now());
        when(jwt.getHeaders()).thenReturn(headers);
        when(jwt.getExpiresAt()).thenReturn(Instant.MAX);
        when(jwt.getClaims()).thenReturn(claims);
    }

    @Test
    void testCreateUserPrincipal() {
        AadJwtBearerTokenAuthenticationConverter converter = new AadJwtBearerTokenAuthenticationConverter();
        AbstractAuthenticationToken authenticationToken = converter.convert(jwt);
        Assertions.assertTrue(authenticationToken.getPrincipal().getClass().isAssignableFrom(AadOAuth2AuthenticatedPrincipal.class));
        AadOAuth2AuthenticatedPrincipal principal = (AadOAuth2AuthenticatedPrincipal) authenticationToken
            .getPrincipal();
        Assertions.assertFalse(principal.getClaims().isEmpty());
        Assertions.assertEquals(principal.getIssuer(), claims.get("iss"));
        Assertions.assertEquals(principal.getTenantId(), claims.get("tid"));
    }

    @Test
    void testNoArgumentsConstructorDefaultScopeAndRoleAuthorities() {
        AadJwtBearerTokenAuthenticationConverter converter = new AadJwtBearerTokenAuthenticationConverter();
        AbstractAuthenticationToken authenticationToken = converter.convert(jwt);
        Assertions.assertTrue(authenticationToken.getPrincipal().getClass().isAssignableFrom(AadOAuth2AuthenticatedPrincipal.class));
        AadOAuth2AuthenticatedPrincipal principal = (AadOAuth2AuthenticatedPrincipal) authenticationToken
            .getPrincipal();
        Assertions.assertFalse(principal.getAttributes().isEmpty());
        Assertions.assertEquals(2, principal.getAttributes().size());
        Assertions.assertEquals(4, principal.getAuthorities().size());
        Assertions.assertTrue(principal.getAuthorities().contains(new SimpleGrantedAuthority("SCOPE_Order.read")));
        Assertions.assertTrue(principal.getAuthorities().contains(new SimpleGrantedAuthority("APPROLE_User.write")));
    }

    @Test
    void testNoArgumentsConstructorExtractScopeAuthorities() {
        when(jwt.getClaim("roles")).thenReturn(null);
        AadJwtBearerTokenAuthenticationConverter converter = new AadJwtBearerTokenAuthenticationConverter();
        AbstractAuthenticationToken authenticationToken = converter.convert(jwt);
        Assertions.assertTrue(authenticationToken.getPrincipal().getClass().isAssignableFrom(AadOAuth2AuthenticatedPrincipal.class));
        AadOAuth2AuthenticatedPrincipal principal = (AadOAuth2AuthenticatedPrincipal) authenticationToken
            .getPrincipal();
        Assertions.assertFalse(principal.getAttributes().isEmpty());
        Assertions.assertEquals(2, principal.getAttributes().size());
        Assertions.assertEquals(2, principal.getAuthorities().size());
        Assertions.assertTrue(principal.getAuthorities().contains(new SimpleGrantedAuthority("SCOPE_Order.read")));
        Assertions.assertTrue(principal.getAuthorities().contains(new SimpleGrantedAuthority("SCOPE_Order.write")));
        Assertions.assertFalse(principal.getAuthorities().contains(new SimpleGrantedAuthority("APPROLE_User.read")));
        Assertions.assertFalse(principal.getAuthorities().contains(new SimpleGrantedAuthority("APPROLE_User.write")));
    }

    @Test
    void testNoArgumentsConstructorExtractRoleAuthorities() {
        when(jwt.getClaim("scp")).thenReturn(null);
        AadJwtBearerTokenAuthenticationConverter converter = new AadJwtBearerTokenAuthenticationConverter();
        AbstractAuthenticationToken authenticationToken = converter.convert(jwt);
        Assertions.assertTrue(authenticationToken.getPrincipal().getClass().isAssignableFrom(AadOAuth2AuthenticatedPrincipal.class));
        AadOAuth2AuthenticatedPrincipal principal = (AadOAuth2AuthenticatedPrincipal) authenticationToken
            .getPrincipal();
        Assertions.assertFalse(principal.getAttributes().isEmpty());
        Assertions.assertEquals(2, principal.getAttributes().size());
        Assertions.assertEquals(2, principal.getAuthorities().size());
        Assertions.assertTrue(principal.getAuthorities().contains(new SimpleGrantedAuthority("APPROLE_User.read")));
        Assertions.assertTrue(principal.getAuthorities().contains(new SimpleGrantedAuthority("APPROLE_User.write")));
        Assertions.assertFalse(principal.getAuthorities().contains(new SimpleGrantedAuthority("SCOPE_Order.read")));
        Assertions.assertFalse(principal.getAuthorities().contains(new SimpleGrantedAuthority("SCOPE_Order.write")));
    }

    @Test
    void testConstructorExtractRoleAuthoritiesWithAuthorityPrefixMapParameter() {
        when(jwt.getClaim("scp")).thenReturn(null);
        Map<String, String> claimToAuthorityPrefixMap = new HashMap<>();
        claimToAuthorityPrefixMap.put("roles", "APPROLE_");
        AadJwtBearerTokenAuthenticationConverter converter = new AadJwtBearerTokenAuthenticationConverter("sub", claimToAuthorityPrefixMap);
        AbstractAuthenticationToken authenticationToken = converter.convert(jwt);
        assertThat(authenticationToken.getPrincipal()).isExactlyInstanceOf(AadOAuth2AuthenticatedPrincipal.class);
        AadOAuth2AuthenticatedPrincipal principal = (AadOAuth2AuthenticatedPrincipal) authenticationToken
            .getPrincipal();
        assertThat(principal.getAttributes()).isNotEmpty();
        assertThat(principal.getAttributes()).hasSize(2);
        assertThat(principal.getAuthorities()).hasSize(2);
        Assertions.assertTrue(principal.getAuthorities().contains(new SimpleGrantedAuthority("APPROLE_User.read")));
        Assertions.assertTrue(principal.getAuthorities().contains(new SimpleGrantedAuthority("APPROLE_User.write")));
    }

    @Test
    void testParameterConstructorExtractScopeAuthorities() {
        when(jwt.getClaim("roles")).thenReturn(null);
        AadJwtBearerTokenAuthenticationConverter converter = new AadJwtBearerTokenAuthenticationConverter("scp");
        AbstractAuthenticationToken authenticationToken = converter.convert(jwt);
        Assertions.assertTrue(authenticationToken.getPrincipal().getClass().isAssignableFrom(AadOAuth2AuthenticatedPrincipal.class));
        AadOAuth2AuthenticatedPrincipal principal = (AadOAuth2AuthenticatedPrincipal) authenticationToken
            .getPrincipal();
        Assertions.assertFalse(principal.getAttributes().isEmpty());
        Assertions.assertEquals(2, principal.getAttributes().size());
        Assertions.assertEquals(2, principal.getAuthorities().size());
        Assertions.assertTrue(principal.getAuthorities().contains(new SimpleGrantedAuthority("SCOPE_Order.read")));
        Assertions.assertTrue(principal.getAuthorities().contains(new SimpleGrantedAuthority("SCOPE_Order.write")));
        Assertions.assertFalse(principal.getAuthorities().contains(new SimpleGrantedAuthority("APPROLE_User.read")));
        Assertions.assertFalse(principal.getAuthorities().contains(new SimpleGrantedAuthority("APPROLE_User.write")));
    }

    @Test
    void testParameterConstructorExtractRoleAuthorities() {
        when(jwt.getClaim("scp")).thenReturn(null);
        AadJwtBearerTokenAuthenticationConverter converter = new AadJwtBearerTokenAuthenticationConverter("roles",
            "APPROLE_");
        AbstractAuthenticationToken authenticationToken = converter.convert(jwt);
        Assertions.assertTrue(authenticationToken.getPrincipal().getClass().isAssignableFrom(AadOAuth2AuthenticatedPrincipal.class));
        AadOAuth2AuthenticatedPrincipal principal = (AadOAuth2AuthenticatedPrincipal) authenticationToken
            .getPrincipal();
        Assertions.assertFalse(principal.getAttributes().isEmpty());
        Assertions.assertEquals(2, principal.getAttributes().size());
        Assertions.assertEquals(2, principal.getAuthorities().size());
        Assertions.assertTrue(principal.getAuthorities().contains(new SimpleGrantedAuthority("APPROLE_User.read")));
        Assertions.assertTrue(principal.getAuthorities().contains(new SimpleGrantedAuthority("APPROLE_User.write")));
        Assertions.assertFalse(principal.getAuthorities().contains(new SimpleGrantedAuthority("SCOPE_Order.read")));
        Assertions.assertFalse(principal.getAuthorities().contains(new SimpleGrantedAuthority("SCOPE_Order.write")));
    }
}
