// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.aadb2c.security.jwt;

import com.azure.spring.cloud.autoconfigure.implementation.aad.security.constants.AadJwtClaimNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Validates the Azure AD B2C user flow in the {@code tfp} claim, falling back to {@code acr} only when
 * {@code tfp} is absent. User flow names are matched case-insensitively, and blank claims are rejected.
 */
public final class AadB2cJwtUserFlowValidator implements OAuth2TokenValidator<Jwt> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AadB2cJwtUserFlowValidator.class);
    private static final OAuth2Error INVALID_USER_FLOW = new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN,
        "The tfp or acr claim does not match a configured user flow.", null);

    private final Collection<String> userFlows;

    /**
     * Creates a validator for the configured user flow names.
     *
     * @param userFlows the allowed user flow names, not the configuration map keys
     * @throws IllegalArgumentException if {@code userFlows} is null or empty
     */
    public AadB2cJwtUserFlowValidator(Collection<String> userFlows) {
        Assert.notEmpty(userFlows, "userFlows cannot be null or empty");
        this.userFlows = new ArrayList<>(userFlows);
    }

    /**
     * Validates the token's user flow against the configured names.
     *
     * @param jwt the token to validate
     * @return the validation result, containing an {@code invalid_token} error if the user flow is not allowed
     * @throws IllegalArgumentException if {@code jwt} is null
     */
    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        Assert.notNull(jwt, "jwt cannot be null");
        String claimName = jwt.hasClaim(AadJwtClaimNames.TFP) ? AadJwtClaimNames.TFP : AadJwtClaimNames.ACR;
        Object userFlow = jwt.getClaim(claimName);
        if (!(userFlow instanceof String)) {
            LOGGER.debug("Azure AD B2C user flow validation failed: the '{}' claim is missing or is not a string.",
                claimName);
            return OAuth2TokenValidatorResult.failure(INVALID_USER_FLOW);
        }
        if (!StringUtils.hasText((String) userFlow)) {
            LOGGER.debug("Azure AD B2C user flow validation failed: the '{}' claim is blank.", claimName);
            return OAuth2TokenValidatorResult.failure(INVALID_USER_FLOW);
        }
        if (userFlows.stream().anyMatch(configuredFlow -> configuredFlow.equalsIgnoreCase((String) userFlow))) {
            LOGGER.debug("Azure AD B2C user flow validation succeeded using the '{}' claim.", claimName);
            return OAuth2TokenValidatorResult.success();
        }
        LOGGER.debug("Azure AD B2C user flow validation failed: the '{}' claim does not match a configured user flow.",
            claimName);
        return OAuth2TokenValidatorResult.failure(INVALID_USER_FLOW);
    }
}
