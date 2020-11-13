package com.azure.spring.aad.resource.validator;

import com.azure.spring.autoconfigure.aad.AADTokenClaim;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class AzureJwtIssuerValidator implements OAuth2TokenValidator<Jwt> {

    private static final String LOGIN_MICROSOFT_ONLINE_ISSUER = "https://login.microsoftonline.com/";
    private static final String STS_WINDOWS_ISSUER = "https://sts.windows.net/";
    private static final String STS_CHINA_CLOUD_API_ISSUER = "https://sts.chinacloudapi.cn/";
    private final JwtClaimValidator<String> validator;

    public AzureJwtIssuerValidator(String tenantId, Set<String> allowedTenantIds) {
        Assert.notNull(tenantId, "tenantId cannot be null");
        Assert.notNull(allowedTenantIds, "allowedTenantIds cannot be null");
        if (allowedTenantIds.isEmpty()) {
            if (StringUtils.isEmpty(tenantId)) {
                this.validator = new JwtClaimValidator(AADTokenClaim.ISS, iss -> true);
            } else {
                List<String> issuers = assembleIssuer(tenantId);
                this.validator = new JwtClaimValidator(AADTokenClaim.ISS, issuers::contains);
            }
        } else {
            this.validator = new JwtClaimValidator(AADTokenClaim.ISS, validIssuer(allowedTenantIds));
        }
    }

    private List<String> assembleIssuer(String tenantId) {
        List<String> issuers = new ArrayList<>();
        issuers.add(LOGIN_MICROSOFT_ONLINE_ISSUER + tenantId + "/");
        issuers.add(STS_WINDOWS_ISSUER + tenantId + "/");
        issuers.add(STS_CHINA_CLOUD_API_ISSUER + tenantId + "/");
        return issuers;
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
