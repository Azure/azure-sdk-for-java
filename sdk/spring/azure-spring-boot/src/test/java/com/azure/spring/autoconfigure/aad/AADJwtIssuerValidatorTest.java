// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.aad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Set;
import org.junit.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class AADJwtIssuerValidatorTest {

    final AADAuthenticationProperties aadAuthenticationProperties = mock(AADAuthenticationProperties.class);
    final Jwt jwt = mock(Jwt.class);

    @Test
    public void testSingleTenantExistAndIssuerSuccessVerify() {
        when(aadAuthenticationProperties.getTenantId()).thenReturn("fake-tenant-id");
        when(jwt.getClaim(AADTokenClaim.ISS)).thenReturn("https://sts.windows.net/fake-tenant-id/v2.0");
        String singleTenantId = aadAuthenticationProperties.getTenantId();
        Set<String> allowedTenantIds = aadAuthenticationProperties.getAllowedTenantIds();

        AADJwtIssuerValidator aadJwtIssuerValidator = new AADJwtIssuerValidator(singleTenantId,allowedTenantIds);
        OAuth2TokenValidatorResult result = aadJwtIssuerValidator.validate(jwt);

        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    public void testMultiTenantExistAndIssuerSuccessVerify() {
        when(aadAuthenticationProperties.getTenantId()).thenReturn("common");
        when(jwt.getClaim(AADTokenClaim.ISS)).thenReturn("https://sts.windows.net/fake-tenant-id/v2.0");
        String singleTenantId = aadAuthenticationProperties.getTenantId();
        Set<String> allowedTenantIds = aadAuthenticationProperties.getAllowedTenantIds();
        allowedTenantIds.add("fake-tenant-id");
        allowedTenantIds.add("fake-tenant-id2");
        allowedTenantIds.add("fake-tenant-id3");
        AADJwtIssuerValidator aadJwtIssuerValidator = new AADJwtIssuerValidator(singleTenantId,allowedTenantIds);
        OAuth2TokenValidatorResult result = aadJwtIssuerValidator.validate(jwt);

        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isEmpty();
    }


    @Test
    public void testTenantDefaultAndAllowedTenantIdsEmptyAndSkipIssuerVerify() {
        when(aadAuthenticationProperties.getTenantId()).thenReturn("common");
        when(jwt.getClaim(AADTokenClaim.ISS)).thenReturn("https://sts.windows.net/fake-tenant-id/v2.0");
        String singleTenantId = aadAuthenticationProperties.getTenantId();
        Set<String> allowedTenantIds = aadAuthenticationProperties.getAllowedTenantIds();
        AADJwtIssuerValidator aadJwtIssuerValidator = new AADJwtIssuerValidator(singleTenantId,allowedTenantIds);
        OAuth2TokenValidatorResult result = aadJwtIssuerValidator.validate(jwt);

        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isEmpty();
    }

}
