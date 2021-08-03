// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.webapi.validator;

import com.azure.spring.aad.AADTrustedIssuerRepository;
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

    private static final String LOGIN_MICROSOFT_ONLINE_ISSUER = "https://login.microsoftonline.com/";

    private static final String STS_WINDOWS_ISSUER = "https://sts.windows.net/";

    private static final String STS_CHINA_CLOUD_API_ISSUER = "https://sts.chinacloudapi.cn/";

    private final AADJwtClaimValidator<String> validator;

    private final AADTrustedIssuerRepository trustedIssuerRepo;

    /**
     * Constructs a {@link AADJwtIssuerValidator} using the provided parameters
     */
    public AADJwtIssuerValidator() {
        this(null);
    }

    /**
     * Constructs a {@link AADJwtIssuerValidator} using the provided parameters
     *
     * @param aadTrustedIssuerRepository trusted issuer repository.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public AADJwtIssuerValidator(AADTrustedIssuerRepository aadTrustedIssuerRepository) {
        this.trustedIssuerRepo = aadTrustedIssuerRepository;
        this.validator = new AADJwtClaimValidator<>(AADTokenClaim.ISS, trustedIssuerRepoValidIssuer());
    }

    private Predicate<String> trustedIssuerRepoValidIssuer() {
        return iss -> {
            if (iss == null) {
                return false;
            }
            if (trustedIssuerRepo == null) {
                return iss.startsWith(LOGIN_MICROSOFT_ONLINE_ISSUER)
                    || iss.startsWith(STS_WINDOWS_ISSUER)
                    || iss.startsWith(STS_CHINA_CLOUD_API_ISSUER);
            }
            return trustedIssuerRepo.getTrustedIssuers().contains(iss);
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
