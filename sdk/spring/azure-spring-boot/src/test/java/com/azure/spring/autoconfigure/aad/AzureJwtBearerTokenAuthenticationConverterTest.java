// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.aad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.azure.spring.aad.resource.server.AzureJwtBearerTokenAuthenticationConverter;
import com.azure.spring.aad.resource.server.AzureOAuth2AuthenticatedPrincipal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

public class AzureJwtBearerTokenAuthenticationConverterTest {

    private Jwt jwt = mock(Jwt.class);

    @Test
    public void testCreateUserPrincipal() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", "fake-issuer");
        claims.put("tid", "fake-tid");
        Map<String, Object> headers = new HashMap<>();
        headers.put("kid", "kg2LYs2T0CTjIfj4rt6JIynen38");
        when(jwt.getClaim("scp")).thenReturn("Order.read Order.write");
        when(jwt.getTokenValue()).thenReturn("fake-token-value");
        when(jwt.getIssuedAt()).thenReturn(Instant.now());
        when(jwt.getHeaders()).thenReturn(headers);
        when(jwt.getExpiresAt()).thenReturn(Instant.MAX);
        when(jwt.getClaims()).thenReturn(claims);
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
}
