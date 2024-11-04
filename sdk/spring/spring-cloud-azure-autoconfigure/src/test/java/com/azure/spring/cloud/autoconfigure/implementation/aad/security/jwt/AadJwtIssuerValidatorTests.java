// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.aad.security.jwt;

import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadProfileProperties;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.constants.AadJwtClaimNames;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AadJwtIssuerValidatorTests {

    private final AadAuthenticationProperties aadAuthenticationProperties = mock(AadAuthenticationProperties.class);
    private final Jwt jwt = mock(Jwt.class);
    private final AadTrustedIssuerRepository aadTrustedIssuerRepository = new AadTrustedIssuerRepository("fake-tenant"
        + "-id");

    @Test
    void testNoStructureIssuerSuccessVerify() {
        AadProfileProperties profile = new AadProfileProperties();
        profile.setTenantId("fake-tenant-id");
        when(aadAuthenticationProperties.getProfile()).thenReturn(profile);
        when(jwt.getClaim(AadJwtClaimNames.ISS)).thenReturn("https://sts.windows.net/fake-tenant-id/v2.0");

        AadJwtIssuerValidator validator = new AadJwtIssuerValidator();
        OAuth2TokenValidatorResult result = validator.validate(jwt);
        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void testNoStructureIssuerFailureVerify() {
        AadProfileProperties profile = new AadProfileProperties();
        profile.setTenantId("common");
        when(aadAuthenticationProperties.getProfile()).thenReturn(profile);
        when(jwt.getClaim(AadJwtClaimNames.ISS)).thenReturn("https://sts.failure.net/fake-tenant-id/v2.0");

        AadJwtIssuerValidator validator = new AadJwtIssuerValidator();
        OAuth2TokenValidatorResult result = validator.validate(jwt);
        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    void testIssuerSuccessVerify() {
        AadProfileProperties profile = new AadProfileProperties();
        profile.setTenantId("fake-tenant-id");
        when(aadAuthenticationProperties.getProfile()).thenReturn(profile);
        when(jwt.getClaim(AadJwtClaimNames.ISS)).thenReturn("https://sts.windows.net/fake-tenant-id/v2.0");

        AadJwtIssuerValidator validator = new AadJwtIssuerValidator(aadTrustedIssuerRepository);
        OAuth2TokenValidatorResult result = validator.validate(jwt);
        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void testIssuerFailureVerify() {
        AadProfileProperties profile = new AadProfileProperties();
        profile.setTenantId("common");
        when(aadAuthenticationProperties.getProfile()).thenReturn(profile);
        when(jwt.getClaim(AadJwtClaimNames.ISS)).thenReturn("https://sts.failure.net/fake-tenant-id/v2.0");

        AadJwtIssuerValidator validator = new AadJwtIssuerValidator(aadTrustedIssuerRepository);
        OAuth2TokenValidatorResult result = validator.validate(jwt);
        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isNotEmpty();
    }

}
