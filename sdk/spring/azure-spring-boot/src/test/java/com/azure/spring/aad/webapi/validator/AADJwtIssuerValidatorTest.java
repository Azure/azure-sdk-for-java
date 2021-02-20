// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.webapi.validator;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import com.azure.spring.autoconfigure.aad.AADTokenClaim;
import org.junit.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AADJwtIssuerValidatorTest {

    final AADAuthenticationProperties aadAuthenticationProperties = mock(AADAuthenticationProperties.class);
    final Jwt jwt = mock(Jwt.class);

    @Test
    public void testIssuerSuccessVerify() {
        when(aadAuthenticationProperties.getTenantId()).thenReturn("fake-tenant-id");
        when(jwt.getClaim(AADTokenClaim.ISS)).thenReturn("https://sts.windows.net/fake-tenant-id/v2.0");

        AADJwtIssuerValidator validator = new AADJwtIssuerValidator();
        OAuth2TokenValidatorResult result = validator.validate(jwt);
        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    public void testIssuerFailureVerify() {
        when(aadAuthenticationProperties.getTenantId()).thenReturn("common");
        when(jwt.getClaim(AADTokenClaim.ISS)).thenReturn("https://sts.failure.net/fake-tenant-id/v2.0");

        AADJwtIssuerValidator validator = new AADJwtIssuerValidator();
        OAuth2TokenValidatorResult result = validator.validate(jwt);
        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isNotEmpty();
    }

}
