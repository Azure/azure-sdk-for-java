// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.aad;

import java.util.Set;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.util.Assert;

/**
 * Validates the "tid" claim in a {@link Jwt}, that is matches a configured value
 */
public class AADJwtTenantValidator implements OAuth2TokenValidator<Jwt> {

    private final JwtClaimValidator<String> validator;
    private static final String COMMON = "common";

    /**
     * Constructs a {@link AADJwtIssuerValidator} using the provided parameters
     *
     * @param tenantId - The tenant that each {@link Jwt} should have.
     * @param allowedTenantIds - Multi-tenant is allowed tenantIds
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public AADJwtTenantValidator(String tenantId, Set<String> allowedTenantIds) {
        Assert.notNull(tenantId, "tenantId cannot be null");
        Assert.notNull(allowedTenantIds, "allowedTenantIds cannot be null");
        if (tenantId.equals(COMMON)) {
            if (allowedTenantIds.isEmpty()) {
                this.validator = new JwtClaimValidator(AADTokenClaim.TID, tid -> true);
            } else {
                this.validator = new JwtClaimValidator(AADTokenClaim.TID, allowedTenantIds::contains);
            }
        } else {
            this.validator = new JwtClaimValidator(AADTokenClaim.TID, tenantId::equals);
        }
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
