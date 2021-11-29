// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.webapi.validator;

import com.azure.spring.cloud.autoconfigure.aad.implementation.constants.AADTokenClaim;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AADJwtAudienceValidatorTest {

    @Test
    void oneValidAudienceTest() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(AADTokenClaim.AUD)).thenReturn(Arrays.asList("fake-client-id"));
        List<String> validAudiences = Arrays.asList("fake-client-id");
        AADJwtAudienceValidator validator = new AADJwtAudienceValidator(validAudiences);
        OAuth2TokenValidatorResult result = validator.validate(jwt);
        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void oneInValidAudienceTest() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(AADTokenClaim.AUD)).thenReturn(Arrays.asList("fake-client-id-1"));
        List<String> validAudiences = Arrays.asList("fake-client-id-2");
        AADJwtAudienceValidator validator = new AADJwtAudienceValidator(validAudiences);
        OAuth2TokenValidatorResult result = validator.validate(jwt);
        assertThat(result).isNotNull();
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    void multipleValidAudienceTest() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(AADTokenClaim.AUD)).thenReturn(Arrays.asList("fake-app-id-uri"));
        List<String> validAudiences = Arrays.asList("fake-client-id", "fake-app-id-uri");
        AADJwtAudienceValidator validator = new AADJwtAudienceValidator(validAudiences);
        OAuth2TokenValidatorResult result = validator.validate(jwt);
        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isEmpty();
    }
}
