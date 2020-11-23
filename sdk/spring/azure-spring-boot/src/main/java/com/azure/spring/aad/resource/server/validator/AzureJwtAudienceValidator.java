// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.resource.server.validator;

import com.azure.spring.autoconfigure.aad.AADTokenClaim;
import java.util.List;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.util.Assert;

/**
 * Validates the "aud" claim in a {@link Jwt}, that is matches a configured value
 */
public class AzureJwtAudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final JwtClaimValidator<List<String>> validator;

    /**
     * Constructs a {@link AzureJwtAudienceValidator} using the provided parameters
     *
     * @param audiences - The audience that each {@link Jwt} should have.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public AzureJwtAudienceValidator(List<String> audiences) {
        Assert.notNull(audiences, "audiences cannot be null");
        this.validator = new JwtClaimValidator(AADTokenClaim.AUD, aud -> audiences.containsAll((List<String>) aud));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        Assert.notNull(token, "token cannot be null");
        return this.validator.validate(token);
    }


}
