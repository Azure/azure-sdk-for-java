// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad.implementation.webapi.validator;

import com.azure.spring.cloud.autoconfigure.aad.implementation.constants.AADTokenClaim;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Validates the "aud" claim in a {@link Jwt}, that is matches a configured value
 *
 * @see OAuth2TokenValidator
 */
public class AADJwtAudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final AADJwtClaimValidator<List<String>> validator;

    /**
     * Constructs a {@link AADJwtAudienceValidator} using the provided parameters
     *
     * @param audiences - The audience that each {@link Jwt} should have.
     */
    public AADJwtAudienceValidator(List<String> audiences) {
        Assert.notNull(audiences, "audiences cannot be null");
        this.validator = new AADJwtClaimValidator<>(AADTokenClaim.AUD, audiences::containsAll);
    }

    /**
     * Verify the validity and/or constraints of the provided OAuth 2.0 Token.
     *
     * @param token an OAuth 2.0 token
     * @return OAuth2TokenValidationResult the success or failure detail of the validation
     */
    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        Assert.notNull(token, "token cannot be null");
        return this.validator.validate(token);
    }
}
