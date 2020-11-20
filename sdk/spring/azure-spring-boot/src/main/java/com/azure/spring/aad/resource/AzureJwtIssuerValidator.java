// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.resource;

import com.azure.spring.autoconfigure.aad.AADTokenClaim;
import java.util.function.Predicate;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.util.Assert;

/**
 * Validates the "iss" claim in a {@link Jwt}, that is matches a configured value
 */
public class AzureJwtIssuerValidator implements OAuth2TokenValidator<Jwt> {

    private static final String LOGIN_MICROSOFT_ONLINE_ISSUER = "https://login.microsoftonline.com/";
    private static final String STS_WINDOWS_ISSUER = "https://sts.windows.net/";
    private static final String STS_CHINA_CLOUD_API_ISSUER = "https://sts.chinacloudapi.cn/";
    private final JwtClaimValidator<String> validator;

    /**
     * Constructs a {@link AzureJwtIssuerValidator} using the provided parameters
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public AzureJwtIssuerValidator() {
        this.validator = new JwtClaimValidator(AADTokenClaim.ISS, validIssuer());
    }

    private Predicate<String> validIssuer() {
        return iss -> {
            if (iss == null) {
                return false;
            }
            return iss.startsWith(LOGIN_MICROSOFT_ONLINE_ISSUER)
                || iss.startsWith(STS_WINDOWS_ISSUER)
                || iss.startsWith(STS_CHINA_CLOUD_API_ISSUER);
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
