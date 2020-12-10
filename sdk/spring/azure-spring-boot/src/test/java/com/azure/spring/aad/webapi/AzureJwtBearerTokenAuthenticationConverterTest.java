// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.webapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

public class AzureJwtBearerTokenAuthenticationConverterTest {

    private Jwt jwt = mock(Jwt.class);
    private Map<String, Object> claims = new HashMap<>();
    private Map<String, Object> headers = new HashMap<>();

    @Before
    public void init() {
        claims.put("iss", "fake-issuer");
        claims.put("tid", "fake-tid");
        headers.put("kid", "kg2LYs2T0CTjIfj4rt6JIynen38");
        when(jwt.getClaim("scp")).thenReturn("Order.read Order.write");
        when(jwt.getClaim("roles")).thenReturn("User.read User.write");
        when(jwt.getTokenValue()).thenReturn("fake-token-value");
        when(jwt.getIssuedAt()).thenReturn(Instant.now());
        when(jwt.getHeaders()).thenReturn(headers);
        when(jwt.getExpiresAt()).thenReturn(Instant.MAX);
        when(jwt.getClaims()).thenReturn(claims);
        when(jwt.containsClaim("scp")).thenReturn(true);
    }

    @Test
    public void testCreateUserPrincipal() {
        AzureJwtBearerTokenAuthenticationConverter azureJwtBearerTokenAuthenticationConverter
            = new AzureJwtBearerTokenAuthenticationConverter();
        AbstractAuthenticationToken authenticationToken = azureJwtBearerTokenAuthenticationConverter.convert(jwt);
        assertThat(authenticationToken.getPrincipal()).isExactlyInstanceOf(AzureOAuth2AuthenticatedPrincipal.class);
        AzureOAuth2AuthenticatedPrincipal principal = (AzureOAuth2AuthenticatedPrincipal) authenticationToken
            .getPrincipal();
        assertThat(principal.getClaims()).isNotEmpty();
        assertThat(principal.getIssuer()).isEqualTo(claims.get("iss"));
        assertThat(principal.getTenantId()).isEqualTo(claims.get("tid"));
    }

    @Test
    public void testExtractDefaultScopeAuthorities() {
        AzureJwtBearerTokenAuthenticationConverter azureJwtBearerTokenAuthenticationConverter
            = new AzureJwtBearerTokenAuthenticationConverter();
        AbstractAuthenticationToken authenticationToken = azureJwtBearerTokenAuthenticationConverter.convert(jwt);
        assertThat(authenticationToken.getPrincipal()).isExactlyInstanceOf(AzureOAuth2AuthenticatedPrincipal.class);
        AzureOAuth2AuthenticatedPrincipal principal = (AzureOAuth2AuthenticatedPrincipal) authenticationToken
            .getPrincipal();
        assertThat(principal.getAttributes()).isNotEmpty();
        assertThat(principal.getAttributes()).hasSize(2);
    }

    @Test
    public void testExtractCustomScopeAuthorities() {
        when(jwt.containsClaim("roles")).thenReturn(true);
        AzureJwtBearerTokenAuthenticationConverter azureJwtBearerTokenAuthenticationConverter
            = new AzureJwtBearerTokenAuthenticationConverter("roles", "ROLE_");
        AbstractAuthenticationToken authenticationToken = azureJwtBearerTokenAuthenticationConverter.convert(jwt);
        assertThat(authenticationToken.getPrincipal()).isExactlyInstanceOf(AzureOAuth2AuthenticatedPrincipal.class);
        AzureOAuth2AuthenticatedPrincipal principal = (AzureOAuth2AuthenticatedPrincipal) authenticationToken
            .getPrincipal();
        assertThat(principal.getAttributes()).isNotEmpty();
        assertThat(principal.getAttributes()).hasSize(2);
    }


}
