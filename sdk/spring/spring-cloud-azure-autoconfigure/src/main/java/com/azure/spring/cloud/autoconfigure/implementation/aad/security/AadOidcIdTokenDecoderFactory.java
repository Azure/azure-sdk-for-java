// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security;

import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.constants.AadJwtClaimNames;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.jwt.AadJwtIssuerValidator;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.jwt.AadTrustedIssuerRepository;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenValidator;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A factory that provides a {@link JwtDecoder} used for {@link OidcIdToken} signature verification and claim validation.
 * <p>
 * Besides verifying the token signature, the decoder validates the standard OpenID Connect ID token claims (audience,
 * expiry, issued-at and subject) via {@link OidcIdTokenValidator} and additionally validates the {@code iss} (issuer)
 * claim:
 * <ul>
 *     <li>For a single tenant application, the issuer must belong to the configured tenant.</li>
 *     <li>For a multi-tenant application (the {@code common}, {@code organizations} or {@code consumers} endpoints),
 *     the issuer must be a trusted Microsoft identity platform issuer that is consistent with the token's own
 *     {@code tid} (tenant id) claim, so that the relying application can safely use the {@code iss}/{@code tid} claims
 *     to restrict which tenants are allowed to sign in.</li>
 * </ul>
 *
 * @see <a href="https://learn.microsoft.com/azure/active-directory/develop/id-tokens">azure-active-directory id-tokens</a>
 */
public class AadOidcIdTokenDecoderFactory implements JwtDecoderFactory<ClientRegistration> {

    private final String jwkSetUri;
    private final RestOperations restOperations;
    private final AadAuthenticationProperties properties;
    private final Map<String, JwtDecoder> jwtDecoders = new ConcurrentHashMap<>();

    /**
     *
     * @param jwkSetUri The uri of the jwk set. For example:
     *                 <a href="https://login.microsoftonline.com/common/discovery/v2.0/keys">
     *                     https://login.microsoftonline.com/common/discovery/v2.0/keys</a>
     * @param restOperations The RestOperations used to retrieve jwk from jwkSetUri.
     * @param properties The AAD authentication properties used to build the ID token validators.
     */
    public AadOidcIdTokenDecoderFactory(String jwkSetUri, RestOperations restOperations,
                                        AadAuthenticationProperties properties) {
        Assert.notNull(jwkSetUri, "jwkSetUri cannot be null");
        Assert.notNull(restOperations, "restOperations cannot be null");
        Assert.notNull(properties, "properties cannot be null");
        this.jwkSetUri = jwkSetUri;
        this.restOperations = restOperations;
        this.properties = properties;
    }

    @Override
    public JwtDecoder createDecoder(ClientRegistration context) {
        Assert.notNull(context, "context cannot be null");
        return this.jwtDecoders.computeIfAbsent(context.getRegistrationId(), key -> {
            NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
                    .withJwkSetUri(jwkSetUri)
                    .jwsAlgorithm(SignatureAlgorithm.RS256)
                    .restOperations(restOperations)
                    .build();
            jwtDecoder.setJwtValidator(createJwtValidator(context));
            return jwtDecoder;
        });
    }

    private OAuth2TokenValidator<Jwt> createJwtValidator(ClientRegistration context) {
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        // Validates the standard OIDC ID token claims: audience (must contain the client id), expiry, issued-at,
        // subject and authorized party.
        validators.add(new OidcIdTokenValidator(context));
        // Validates the issuer to restrict which tenant(s) are allowed to sign in.
        validators.add(createIssuerValidator());
        return new DelegatingOAuth2TokenValidator<>(validators);
    }

    private OAuth2TokenValidator<Jwt> createIssuerValidator() {
        String tenantId = getTrimmedTenantId();
        if (isMultiTenant(tenantId)) {
            return new AadMultiTenantIssuerValidator();
        }
        return new AadJwtIssuerValidator(new AadTrustedIssuerRepository(tenantId));
    }

    private String getTrimmedTenantId() {
        String tenantId = properties.getProfile().getTenantId();
        return tenantId != null ? tenantId.trim().toLowerCase(Locale.ROOT) : null;
    }

    private static boolean isMultiTenant(String tenantId) {
        return !StringUtils.hasText(tenantId)
                || "common".equalsIgnoreCase(tenantId)
                || "organizations".equalsIgnoreCase(tenantId)
                || "consumers".equalsIgnoreCase(tenantId);
    }

    /**
     * Validates that the {@code iss} claim is a trusted Microsoft identity platform issuer that is consistent with the
     * token's own {@code tid} (tenant id) claim. This is used by multi-tenant applications, where the configured tenant
     * id ({@code common}, {@code organizations} or {@code consumers}) cannot be used to derive a single expected issuer.
     */
    private static final class AadMultiTenantIssuerValidator implements OAuth2TokenValidator<Jwt> {

        @Override
        public OAuth2TokenValidatorResult validate(Jwt token) {
            Assert.notNull(token, "token cannot be null");
            OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN,
                    "The iss claim is not valid.",
                    "https://learn.microsoft.com/azure/active-directory/develop/id-tokens");
            String issuer = token.getClaimAsString(AadJwtClaimNames.ISS);
            String tenantId = token.getClaimAsString(AadJwtClaimNames.TID);
            if (!StringUtils.hasText(issuer) || !StringUtils.hasText(tenantId)) {
                return OAuth2TokenValidatorResult.failure(error);
            }
            if (new AadTrustedIssuerRepository(tenantId).isTrusted(issuer)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(error);
        }
    }
}
