package com.azure.spring.autoconfigure.aad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.azure.spring.aad.resource.AzureJwtBearerTokenAuthenticationConverter;
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
        Map<String, Object> map = new HashMap<>();
        map.put("iss", "fake-issuer");
        map.put("tid", "fake-tid");
        when(jwt.getClaim("scp")).thenReturn("Order.read Order.write");
        when(jwt.getTokenValue()).thenReturn("fake-token-value");
        when(jwt.getIssuedAt()).thenReturn(Instant.now());
        when(jwt.getExpiresAt()).thenReturn(Instant.now());
        when(jwt.getClaims()).thenReturn(map);

        AzureJwtBearerTokenAuthenticationConverter azureJwtBearerTokenAuthenticationConverter
            = new AzureJwtBearerTokenAuthenticationConverter();
        AbstractAuthenticationToken authenticationToken = azureJwtBearerTokenAuthenticationConverter.convert(jwt);

        assertThat(authenticationToken.getPrincipal()).isExactlyInstanceOf(UserPrincipal.class);
        UserPrincipal userPrincipal = (UserPrincipal) authenticationToken.getPrincipal();

        assertThat(userPrincipal.getClaims()).isNotEmpty();
        assertThat(userPrincipal.getIssuer()).isEqualTo(map.get("iss"));
        assertThat(userPrincipal.getTenantId()).isEqualTo(map.get("tid"));
    }
}
