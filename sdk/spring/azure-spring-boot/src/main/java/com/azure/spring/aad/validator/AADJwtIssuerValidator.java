// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.validator;

import com.azure.spring.autoconfigure.aad.AADTokenClaim;
import java.util.Set;
import java.util.function.Predicate;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.Assert;

/**
 * Validates the "iss" claim in a {@link Jwt}, that is matches a configured value
 */
public class AADJwtIssuerValidator implements OAuth2TokenValidator<Jwt> {

    private final AADJwtClaimValidator<String> validator;

    /**
     * Constructs a {@link AADJwtIssuerValidator} using the provided parameters
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public AADJwtIssuerValidator(Set<String> trustedIssuers) {
        this.validator = new AADJwtClaimValidator<>(AADTokenClaim.ISS, iss -> trustedIssuers.contains(iss));
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
