// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.webapi.validator;

import com.azure.spring.aad.webapi.AADTrustedIssuerRepository;
import com.azure.spring.autoconfigure.aad.AADTokenClaim;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.Assert;

import java.util.function.Predicate;

/**
 * Validates the "iss" claim in a {@link Jwt}, that is matches a configured value
 */
public class AADJwtIssuerValidator implements OAuth2TokenValidator<Jwt> {
    private final AADJwtClaimValidator<String> validator;
    private final AADTrustedIssuerRepository trustedIssuerRepository;

    /**
     * Constructs a {@link AADJwtIssuerValidator} using the provided parameters
     *
     * @param aadTrustedIssuerRepository trusted issuer repository.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public AADJwtIssuerValidator(AADTrustedIssuerRepository aadTrustedIssuerRepository) {
        this.trustedIssuerRepository = aadTrustedIssuerRepository;
        this.validator = new AADJwtClaimValidator<>(AADTokenClaim.ISS, validIssuer());
    }

    private Predicate<String> validIssuer() {
        return iss -> {
            if (iss == null) {
                return false;
            }
            return trustedIssuerRepository.getTrustedIssuers().contains(iss);
        };
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
