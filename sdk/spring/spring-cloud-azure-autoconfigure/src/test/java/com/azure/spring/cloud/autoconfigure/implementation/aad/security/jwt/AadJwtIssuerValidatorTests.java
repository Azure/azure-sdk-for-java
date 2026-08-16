// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.aad.security.jwt;

import com.azure.spring.cloud.autoconfigure.implementation.aad.security.constants.AadJwtClaimNames;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AadJwtIssuerValidatorTests {

    private final Jwt jwt = mock(Jwt.class);
    private final AadTrustedIssuerRepository aadTrustedIssuerRepository = new AadTrustedIssuerRepository("fake-tenant"
        + "-id");

    @Test
    void testIssuerSuccessVerify() {
        when(jwt.getClaim(AadJwtClaimNames.ISS)).thenReturn("https://sts.windows.net/fake-tenant-id/v2.0");

        AadJwtIssuerValidator validator = new AadJwtIssuerValidator(aadTrustedIssuerRepository);
        OAuth2TokenValidatorResult result = validator.validate(jwt);
        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void testIssuerFailureVerify() {
        when(jwt.getClaim(AadJwtClaimNames.ISS)).thenReturn("https://sts.failure.net/fake-tenant-id/v2.0");

        AadJwtIssuerValidator validator = new AadJwtIssuerValidator(aadTrustedIssuerRepository);
        OAuth2TokenValidatorResult result = validator.validate(jwt);
        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isNotEmpty();
    }

}
