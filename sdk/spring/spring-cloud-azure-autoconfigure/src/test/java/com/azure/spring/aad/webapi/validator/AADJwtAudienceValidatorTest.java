// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapi.validator;

import com.azure.spring.aad.implementation.constants.AADTokenClaim;
import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AADJwtAudienceValidatorTest {

    final AADAuthenticationProperties aadAuthenticationProperties = mock(AADAuthenticationProperties.class);
    final Jwt jwt = mock(Jwt.class);
    final List<String> audiences = new ArrayList<>();
    final List<String> claimAudience = new ArrayList<>();

    @Test
    public void testClientIdExistAndSuccessVerify() {
        when(aadAuthenticationProperties.getClientId()).thenReturn("fake-client-id");
        when(jwt.getClaim(AADTokenClaim.AUD)).thenReturn(claimAudience);
        claimAudience.add("fake-client-id");
        audiences.add(aadAuthenticationProperties.getClientId());
        AADJwtAudienceValidator validator = new AADJwtAudienceValidator(audiences);
        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    public void testAppIdUriExistAndSuccessVerify() {
        when(aadAuthenticationProperties.getClientId()).thenReturn("fake-app-id-uri");
        when(jwt.getClaim(AADTokenClaim.AUD)).thenReturn(claimAudience);
        claimAudience.add("fake-app-id-uri");
        audiences.add(aadAuthenticationProperties.getClientId());
        AADJwtAudienceValidator validator = new AADJwtAudienceValidator(audiences);
        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    public void testAppIdUriExistAndClientIdAndSuccessVerify() {
        when(aadAuthenticationProperties.getClientId()).thenReturn("fake-app-id-uri");
        when(aadAuthenticationProperties.getAppIdUri()).thenReturn("fake-client-id");
        when(jwt.getClaim(AADTokenClaim.AUD)).thenReturn(claimAudience);
        //claimAudience.add("fake-client-id");
        claimAudience.add("fake-app-id-uri");
        audiences.add(aadAuthenticationProperties.getClientId());
        audiences.add(aadAuthenticationProperties.getAppIdUri());
        AADJwtAudienceValidator validator = new AADJwtAudienceValidator(audiences);
        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isEmpty();
    }
}
