// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.webapi;

import com.azure.spring.aad.AADOAuth2AuthenticatedPrincipal;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
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
public class AADJwtBearerTokenAuthenticationConverterTest {

    private Jwt jwt = mock(Jwt.class);
    private Map<String, Object> claims = new HashMap<>();
    private Map<String, Object> headers = new HashMap<>();
    private JSONArray jsonArray = new JSONArray().appendElement("User.read").appendElement("User.write");

    @BeforeAll
    public void init() {
        claims.put("iss", "fake-issuer");
        claims.put("tid", "fake-tid");
        headers.put("kid", "kg2LYs2T0CTjIfj4rt6JIynen38");
        when(jwt.getClaim("scp")).thenReturn("Order.read Order.write");
        when(jwt.getClaim("roles")).thenReturn(jsonArray);
        when(jwt.getTokenValue()).thenReturn("fake-token-value");
        when(jwt.getIssuedAt()).thenReturn(Instant.now());
        when(jwt.getHeaders()).thenReturn(headers);
        when(jwt.getExpiresAt()).thenReturn(Instant.MAX);
        when(jwt.getClaims()).thenReturn(claims);
    }

    @Test
    public void testCreateUserPrincipal() {
        AADJwtBearerTokenAuthenticationConverter converter = new AADJwtBearerTokenAuthenticationConverter();
        AbstractAuthenticationToken authenticationToken = converter.convert(jwt);
        assertThat(authenticationToken.getPrincipal()).isExactlyInstanceOf(AADOAuth2AuthenticatedPrincipal.class);
        AADOAuth2AuthenticatedPrincipal principal = (AADOAuth2AuthenticatedPrincipal) authenticationToken
            .getPrincipal();
        assertThat(principal.getClaims()).isNotEmpty();
        assertThat(principal.getIssuer()).isEqualTo(claims.get("iss"));
        assertThat(principal.getTenantId()).isEqualTo(claims.get("tid"));
    }

    @Test
    public void testNoArgumentsConstructorDefaultScopeAndRoleAuthorities() {
        AADJwtBearerTokenAuthenticationConverter converter = new AADJwtBearerTokenAuthenticationConverter();
        AbstractAuthenticationToken authenticationToken = converter.convert(jwt);
        assertThat(authenticationToken.getPrincipal()).isExactlyInstanceOf(AADOAuth2AuthenticatedPrincipal.class);
        AADOAuth2AuthenticatedPrincipal principal = (AADOAuth2AuthenticatedPrincipal) authenticationToken
            .getPrincipal();
        assertThat(principal.getAttributes()).isNotEmpty();
        assertThat(principal.getAttributes()).hasSize(2);
        assertThat(principal.getAuthorities()).hasSize(4);
    }

    @Test
    public void testNoArgumentsConstructorExtractScopeAuthorities() {
        AADJwtBearerTokenAuthenticationConverter converter = new AADJwtBearerTokenAuthenticationConverter();
        AbstractAuthenticationToken authenticationToken = converter.convert(jwt);
        assertThat(authenticationToken.getPrincipal()).isExactlyInstanceOf(AADOAuth2AuthenticatedPrincipal.class);
        AADOAuth2AuthenticatedPrincipal principal = (AADOAuth2AuthenticatedPrincipal) authenticationToken
            .getPrincipal();
        assertThat(principal.getAttributes()).isNotEmpty();
        assertThat(principal.getAttributes()).hasSize(2);
        assertThat(principal.getAuthorities()).hasSize(4);
    }

    @Test
    public void testParameterConstructorExtractScopeAuthorities() {
        AADJwtBearerTokenAuthenticationConverter converter = new AADJwtBearerTokenAuthenticationConverter("scp");
        AbstractAuthenticationToken authenticationToken = converter.convert(jwt);
        assertThat(authenticationToken.getPrincipal()).isExactlyInstanceOf(AADOAuth2AuthenticatedPrincipal.class);
        AADOAuth2AuthenticatedPrincipal principal = (AADOAuth2AuthenticatedPrincipal) authenticationToken
            .getPrincipal();
        assertThat(principal.getAttributes()).isNotEmpty();
        assertThat(principal.getAttributes()).hasSize(2);
        assertThat(principal.getAuthorities()).hasSize(2);
    }

    @Test
    public void testParameterConstructorExtractRoleAuthorities() {
        AADJwtBearerTokenAuthenticationConverter converter = new AADJwtBearerTokenAuthenticationConverter("roles",
            "APPROLE_");
        AbstractAuthenticationToken authenticationToken = converter.convert(jwt);
        assertThat(authenticationToken.getPrincipal()).isExactlyInstanceOf(AADOAuth2AuthenticatedPrincipal.class);
        AADOAuth2AuthenticatedPrincipal principal = (AADOAuth2AuthenticatedPrincipal) authenticationToken
            .getPrincipal();
        assertThat(principal.getAttributes()).isNotEmpty();
        assertThat(principal.getAttributes()).hasSize(2);
        assertThat(principal.getAuthorities()).hasSize(2);
    }

    @Test
    public void testConstructorExtractRoleAuthoritiesWithAuthorityPrefixMapParameter() {
        Map<String, String> claimToAuthorityPrefixMap = new HashMap<>();
        claimToAuthorityPrefixMap.put("roles", "APPROLE_");
        AADJwtBearerTokenAuthenticationConverter converter = new AADJwtBearerTokenAuthenticationConverter("scp", claimToAuthorityPrefixMap);
        AbstractAuthenticationToken authenticationToken = converter.convert(jwt);
        assertThat(authenticationToken.getPrincipal()).isExactlyInstanceOf(AADOAuth2AuthenticatedPrincipal.class);
        AADOAuth2AuthenticatedPrincipal principal = (AADOAuth2AuthenticatedPrincipal) authenticationToken
            .getPrincipal();
        assertThat(principal.getAttributes()).isNotEmpty();
        assertThat(principal.getAttributes()).hasSize(2);
        assertThat(principal.getAuthorities()).hasSize(2);
        Assertions.assertTrue(principal.getAuthorities().contains(new SimpleGrantedAuthority("APPROLE_User.read")));
        Assertions.assertTrue(principal.getAuthorities().contains(new SimpleGrantedAuthority("APPROLE_User.write")));
    }
}
