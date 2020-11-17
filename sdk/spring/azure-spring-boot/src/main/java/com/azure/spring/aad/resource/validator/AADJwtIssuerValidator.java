// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.resource.validator;

import com.azure.spring.autoconfigure.aad.AADTokenClaim;
import java.util.Set;
import java.util.function.Predicate;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.util.Assert;

/**
 * Validates the "iss" claim in a {@link Jwt}, that is matches a configured value
 */
public class AADJwtIssuerValidator implements OAuth2TokenValidator<Jwt> {

    private static final String LOGIN_MICROSOFT_ONLINE_ISSUER = "https://login.microsoftonline.com/";
    private static final String STS_WINDOWS_ISSUER = "https://sts.windows.net/";
    private static final String STS_CHINA_CLOUD_API_ISSUER = "https://sts.chinacloudapi.cn/";
    private static final String COMMON = "common";
    private final JwtClaimValidator<String> validator;

    /**
     * Constructs a {@link AADJwtIssuerValidator} using the provided parameters
     * @param tenantId - The tenant that each {@link Jwt} should have.
     * @param allowedTenantIds - Multi-tenant is allowed tenantIds
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public AADJwtIssuerValidator(String tenantId, Set<String> allowedTenantIds) {
        Assert.notNull(tenantId, "tenantId cannot be null");
        Assert.notNull(allowedTenantIds, "allowedTenantIds cannot be null");
        if (tenantId.equals(COMMON)) {
            if (allowedTenantIds.isEmpty()) {
                this.validator = new JwtClaimValidator(AADTokenClaim.ISS, iss -> true);
            } else {
                this.validator = new JwtClaimValidator(AADTokenClaim.ISS, validIssuer(allowedTenantIds));
            }
        } else {
            this.validator = new JwtClaimValidator(AADTokenClaim.ISS, validIssuer(tenantId));
        }
    }

    private Predicate<String> validIssuer(String tenantId) {
        return tid -> {
            if (tid.startsWith(LOGIN_MICROSOFT_ONLINE_ISSUER) || tid.startsWith(STS_WINDOWS_ISSUER) ||
                tid.startsWith(STS_CHINA_CLOUD_API_ISSUER)) {
                if(tid.contains(tenantId)){
                    return true;
                }
            }
            return false;
        };
    }

    private Predicate<String> validIssuer(Set<String> allowedTenantIds) {
        return tid -> {
            if (tid.startsWith(LOGIN_MICROSOFT_ONLINE_ISSUER) || tid.startsWith(STS_WINDOWS_ISSUER) ||
                tid.startsWith(STS_CHINA_CLOUD_API_ISSUER)) {
                for (String allowedTenantId : allowedTenantIds) {
                    if (tid.contains(allowedTenantId)) {
                        return true;
                    }
                }
            }
            return false;
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
