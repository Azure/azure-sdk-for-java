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

public class AADJwtTenantValidatorTest {

    final AADAuthenticationProperties aadAuthenticationProperties = mock(AADAuthenticationProperties.class);
    final Jwt jwt = mock(Jwt.class);

    @Test
    public void testSingleTenantExistAndSuccessVerify() {
        when(aadAuthenticationProperties.getTenantId()).thenReturn("fake-tenant-id");
        when(jwt.getClaim(AADTokenClaim.TID)).thenReturn("fake-tenant-id");
        String singleTenantId = aadAuthenticationProperties.getTenantId();
        Set<String> allowedTenantIds = aadAuthenticationProperties.getAllowedTenantIds();

        AADJwtTenantValidator aadJwtTenantValidator = new AADJwtTenantValidator(singleTenantId, allowedTenantIds);
        OAuth2TokenValidatorResult result = aadJwtTenantValidator.validate(jwt);

        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    public void testMultiTenantExistAndSuccessVerify() {
        when(aadAuthenticationProperties.getTenantId()).thenReturn("common");
        when(jwt.getClaim(AADTokenClaim.TID)).thenReturn("fake-tenant-id");
        String singleTenantId = aadAuthenticationProperties.getTenantId();
        Set<String> allowedTenantIds = aadAuthenticationProperties.getAllowedTenantIds();
        allowedTenantIds.add("fake-tenant-id");
        allowedTenantIds.add("fake-tenant-id2");
        allowedTenantIds.add("fake-tenant-id3");
        AADJwtTenantValidator aadJwtTenantValidator = new AADJwtTenantValidator(singleTenantId, allowedTenantIds);
        OAuth2TokenValidatorResult result = aadJwtTenantValidator.validate(jwt);

        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    public void testTenantDefaultAndAllowedTenantIdsEmptyAndSkipVerify() {
        when(aadAuthenticationProperties.getTenantId()).thenReturn("common");
        when(jwt.getClaim(AADTokenClaim.TID)).thenReturn("fake-tenant-id");
        String singleTenantId = aadAuthenticationProperties.getTenantId();
        Set<String> allowedTenantIds = aadAuthenticationProperties.getAllowedTenantIds();

        AADJwtTenantValidator aadJwtTenantValidator = new AADJwtTenantValidator(singleTenantId, allowedTenantIds);
        OAuth2TokenValidatorResult result = aadJwtTenantValidator.validate(jwt);

        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isEmpty();
    }

}

