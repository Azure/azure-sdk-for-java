package com.azure.spring.aad.resource.validator;

import com.azure.spring.autoconfigure.aad.AADTokenClaim;
import java.util.Set;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.util.Assert;

public class AzureJwtTenantValidator implements OAuth2TokenValidator<Jwt> {

    private final JwtClaimValidator<String> validator;
    private static final String COMMON = "common";

    @SuppressWarnings({"unchecked", "rawtypes"})
    public AzureJwtTenantValidator(String tenantId, Set<String> allowedTenantIds) {
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
