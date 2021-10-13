// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.webapi.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.Assert;

import java.util.function.Predicate;

/**
 * Validates a claim in a {@link Jwt} against a provided {@link java.util.function.Predicate}.
 *
 * Note: Current implementation is not required, this is only used for compatibility with the
 * Spring Boot 2.2.x version. Once support version is more than 2.2.X, then we can use
 * "org.springframework.security.oauth2.jwt.JwtClaimValidator" instead.
 */
public final class AADJwtClaimValidator<T> implements OAuth2TokenValidator<Jwt> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AADJwtClaimValidator.class);
    private final String claim;
    private final OAuth2Error error;
    private final Predicate<T> test;

    /**
     * Constructs a {@link AADJwtClaimValidator} using the provided parameters
     *
     * @param claim - is the name of the claim in {@link Jwt} to validate.
     * @param test - is the predicate function for the claim to test against.
     */
    public AADJwtClaimValidator(String claim, Predicate<T> test) {
        Assert.notNull(claim, "claim can not be null");
        Assert.notNull(test, "test can not be null");
        this.claim = claim;
        this.test = test;
        this.error = new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST,
                "The " + this.claim + " claim is not valid",
                "https://tools.ietf.org/html/rfc6750#section-3.1");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        Assert.notNull(token, "token cannot be null");
        T claimValue = token.getClaim(this.claim);
        if (test.test(claimValue)) {
            return OAuth2TokenValidatorResult.success();
        } else {
            LOGGER.debug(error.getDescription());
            return OAuth2TokenValidatorResult.failure(error);
        }
    }
}
