package com.azure.spring.aad.resource.validator;

import com.azure.spring.autoconfigure.aad.AADTokenClaim;
import java.util.Set;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class AzureJwtTenantValidator implements OAuth2TokenValidator<Jwt> {

    private final JwtClaimValidator<String> validator;

    public AzureJwtTenantValidator(String tenantId, Set<String> allowedTenantids) {
        Assert.notNull(tenantId, "tenantId cannot be null");
        Assert.notNull(allowedTenantids, "allowedTenantids cannot be null");
        if (allowedTenantids.isEmpty()) {
            if (StringUtils.isEmpty(tenantId)) {
                this.validator = new JwtClaimValidator(AADTokenClaim.TID, tid -> true);
            } else {
                this.validator = new JwtClaimValidator(AADTokenClaim.TID, tenantId::equals);
            }
        } else {
            this.validator = new JwtClaimValidator(AADTokenClaim.TID, allowedTenantids::contains);
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
