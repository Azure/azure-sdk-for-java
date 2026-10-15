// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.aad.security.jwt;

import com.azure.spring.cloud.autoconfigure.implementation.aad.security.constants.AadJwtClaimNames;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.util.Assert;

import java.util.function.Predicate;

/**
 * Validates the "iss" claim in a {@link Jwt}, that is matches a configured value
 *
 * @see OAuth2TokenValidator
 */
public class AadJwtIssuerValidator implements OAuth2TokenValidator<Jwt> {

    private final JwtClaimValidator<String> validator;

    private final AadTrustedIssuerRepository trustedIssuerRepo;

    /**
     * Constructs a {@link AadJwtIssuerValidator} using the provided parameters
     *
     * @param aadTrustedIssuerRepository trusted issuer repository.
     */
    public AadJwtIssuerValidator(AadTrustedIssuerRepository aadTrustedIssuerRepository) {
        Assert.notNull(aadTrustedIssuerRepository, "aadTrustedIssuerRepository cannot be null");
        this.trustedIssuerRepo = aadTrustedIssuerRepository;
        this.validator = new JwtClaimValidator<>(AadJwtClaimNames.ISS, trustedIssuerRepoValidIssuer());
    }

    private Predicate<String> trustedIssuerRepoValidIssuer() {
        return iss -> iss != null && trustedIssuerRepo.getTrustedIssuers().contains(iss);
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
