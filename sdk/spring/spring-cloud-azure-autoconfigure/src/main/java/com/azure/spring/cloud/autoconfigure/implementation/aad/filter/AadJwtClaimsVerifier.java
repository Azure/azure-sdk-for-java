// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.filter;

import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.constants.AadJwtClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Custom JWT claims verifier that adds tenant ID validation for AAD authentication.
 * Extends {@link DefaultJWTClaimsVerifier} to validate that JWT tokens contain the expected tenant ID (tid claim).
 */
public class AadJwtClaimsVerifier extends DefaultJWTClaimsVerifier<com.nimbusds.jose.proc.SecurityContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AadJwtClaimsVerifier.class);
    private static final String LOGIN_MICROSOFT_ONLINE_ISSUER = "https://login.microsoftonline.com/";
    private static final String STS_WINDOWS_ISSUER = "https://sts.windows.net/";
    private static final String STS_CHINA_CLOUD_API_ISSUER = "https://sts.chinacloudapi.cn/";

    private final AadAuthenticationProperties aadAuthenticationProperties;
    private final boolean explicitAudienceCheck;
    private final java.util.Set<String> validAudiences;

    /**
     * Creates a new AadJwtClaimsVerifier.
     *
     * @param aadAuthenticationProperties AAD authentication properties (may be null)
     * @param explicitAudienceCheck whether to explicitly check the audience
     * @param validAudiences valid audience values for explicit audience check
     */
    public AadJwtClaimsVerifier(AadAuthenticationProperties aadAuthenticationProperties,
                                boolean explicitAudienceCheck,
                                java.util.Set<String> validAudiences) {
        super(null, null);
        this.aadAuthenticationProperties = aadAuthenticationProperties;
        this.explicitAudienceCheck = explicitAudienceCheck;
        this.validAudiences = validAudiences;
    }

    @Override
    public void verify(JWTClaimsSet claimsSet, com.nimbusds.jose.proc.SecurityContext ctx) throws BadJWTException {
        super.verify(claimsSet, ctx);
        
        // Issuer validation
        final String issuer = claimsSet.getIssuer();
        if (!isAadIssuer(issuer)) {
            throw new BadJWTException("Invalid token issuer");
        }
        
        // Audience validation
        if (explicitAudienceCheck) {
            java.util.Optional<String> matchedAudience = claimsSet.getAudience()
                                                        .stream()
                                                        .filter(validAudiences::contains)
                                                        .findFirst();
            if (matchedAudience.isPresent()) {
                LOGGER.debug("Matched audience: [{}]", matchedAudience.get());
            } else {
                throw new BadJWTException("Invalid token audience. Provided value " + claimsSet.getAudience()
                    + " does not match either the client-id or AppIdUri.");
            }
        }
        
        // Tenant ID validation
        if (aadAuthenticationProperties != null) {
            String configuredTenantId = aadAuthenticationProperties.getProfile().getTenantId();
            if (StringUtils.hasText(configuredTenantId)) {
                // Skip validation for multi-tenant values: common, organizations, consumers
                String trimmedTenantId = configuredTenantId.trim().toLowerCase(java.util.Locale.ROOT);
                if (!isMultiTenantValue(trimmedTenantId)) {
                    Object tidClaim = claimsSet.getClaim(AadJwtClaimNames.TID);
                    String tokenTid = tidClaim != null ? tidClaim.toString() : null;
                    String normalizedTokenTid = tokenTid != null
                        ? tokenTid.trim().toLowerCase(java.util.Locale.ROOT)
                        : null;
                    if (!trimmedTenantId.equals(normalizedTokenTid)) {
                        throw new BadJWTException("Invalid token tenant. Token tid claim '" + tokenTid
                            + "' does not match the configured tenant '" + configuredTenantId + "'.");
                    }
                    LOGGER.debug("Token tenant validated: [{}]", tokenTid);
                } else {
                    LOGGER.debug("Tenant ID verification skipped: multi-tenant configuration detected [{}]",
                        configuredTenantId);
                }
            }
        }
    }

    private static boolean isAadIssuer(String issuer) {
        if (issuer == null) {
            return false;
        }
        return issuer.startsWith(LOGIN_MICROSOFT_ONLINE_ISSUER)
            || issuer.startsWith(STS_WINDOWS_ISSUER)
            || issuer.startsWith(STS_CHINA_CLOUD_API_ISSUER);
    }

    /**
     * Checks if the given tenant ID represents a multi-tenant configuration.
     * Multi-tenant values (common, organizations, consumers) should skip tenant ID validation.
     */
    private static boolean isMultiTenantValue(String normalizedTenantId) {
        return "common".equals(normalizedTenantId)
            || "organizations".equals(normalizedTenantId)
            || "consumers".equals(normalizedTenantId);
    }
}
