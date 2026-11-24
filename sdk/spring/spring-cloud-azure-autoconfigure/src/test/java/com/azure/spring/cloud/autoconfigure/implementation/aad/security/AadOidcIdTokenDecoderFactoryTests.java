// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security;

import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.constants.AadJwtClaimNames;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;

import static com.azure.spring.cloud.core.implementation.util.ReflectionUtils.getField;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AadOidcIdTokenDecoderFactoryTests {

    private static final String CLIENT_ID = "client-id";
    private static final String TENANT_ID = "fake-tenant-id";
    private static final String ATTACKER_TENANT_ID = "attacker-tenant-id";
    private static final String JWK_SET_URI = "https://login.microsoftonline.com/common/discovery/v2.0/keys";

    @Test
    void createDecoderRejectsNullContext() {
        AadOidcIdTokenDecoderFactory factory = createFactory(TENANT_ID);
        assertThrows(IllegalArgumentException.class, () -> factory.createDecoder(null));
    }

    @Test
    void singleTenantRejectsUntrustedIssuer() {
        OAuth2TokenValidator<Jwt> validator = extractValidator(createFactory(TENANT_ID));
        Jwt jwt = idToken("https://sts.windows.net/" + ATTACKER_TENANT_ID + "/v2.0", ATTACKER_TENANT_ID);
        assertThat(validator.validate(jwt).getErrors()).isNotEmpty();
    }

    @Test
    void singleTenantAcceptsTrustedIssuer() {
        OAuth2TokenValidator<Jwt> validator = extractValidator(createFactory(TENANT_ID));
        Jwt jwt = idToken("https://login.microsoftonline.com/" + TENANT_ID + "/v2.0", TENANT_ID);
        assertThat(validator.validate(jwt).getErrors()).isEmpty();
    }

    @Test
    void multiTenantRejectsIssuerInconsistentWithTid() {
        OAuth2TokenValidator<Jwt> validator = extractValidator(createFactory("common"));
        // The issuer claims one tenant while the tid claims another: cross-tenant inconsistency must be rejected.
        Jwt jwt = idToken("https://login.microsoftonline.com/" + TENANT_ID + "/v2.0", ATTACKER_TENANT_ID);
        assertThat(validator.validate(jwt).getErrors()).isNotEmpty();
    }

    @Test
    void multiTenantAcceptsIssuerConsistentWithTid() {
        OAuth2TokenValidator<Jwt> validator = extractValidator(createFactory("common"));
        Jwt jwt = idToken("https://login.microsoftonline.com/" + TENANT_ID + "/v2.0", TENANT_ID);
        assertThat(validator.validate(jwt).getErrors()).isEmpty();
    }

    @Test
    void rejectsWrongAudience() {
        OAuth2TokenValidator<Jwt> validator = extractValidator(createFactory(TENANT_ID));
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim(AadJwtClaimNames.ISS, "https://login.microsoftonline.com/" + TENANT_ID + "/v2.0")
                .claim(AadJwtClaimNames.TID, TENANT_ID)
                .subject("subject")
                .audience(List.of("some-other-client-id"))
                .issuedAt(Instant.now().minusSeconds(60))
                .expiresAt(Instant.now().plusSeconds(600))
                .build();
        assertThat(validator.validate(jwt).getErrors()).isNotEmpty();
    }

    private static Jwt idToken(String issuer, String tenantId) {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim(AadJwtClaimNames.ISS, issuer)
                .claim(AadJwtClaimNames.TID, tenantId)
                .subject("subject")
                .audience(List.of(CLIENT_ID))
                .issuedAt(Instant.now().minusSeconds(60))
                .expiresAt(Instant.now().plusSeconds(600))
                .build();
    }

    @SuppressWarnings("unchecked")
    private static OAuth2TokenValidator<Jwt> extractValidator(AadOidcIdTokenDecoderFactory factory) {
        JwtDecoder decoder = factory.createDecoder(clientRegistration());
        return (OAuth2TokenValidator<Jwt>) getField(NimbusJwtDecoder.class, "jwtValidator", decoder);
    }

    private static AadOidcIdTokenDecoderFactory createFactory(String tenantId) {
        AadAuthenticationProperties properties = new AadAuthenticationProperties();
        properties.getProfile().setTenantId(tenantId);
        properties.getCredential().setClientId(CLIENT_ID);
        return new AadOidcIdTokenDecoderFactory(JWK_SET_URI, new RestTemplate(), properties);
    }

    private static ClientRegistration clientRegistration() {
        return ClientRegistration
                .withRegistrationId("azure")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/{action}/oauth2/code/{registrationId}")
                .scope("openid")
                .authorizationUri("https://login.microsoftonline.com/common/oauth2/v2.0/authorize")
                .tokenUri("https://login.microsoftonline.com/common/oauth2/v2.0/token")
                .jwkSetUri(JWK_SET_URI)
                .clientName("azure")
                .clientId(CLIENT_ID)
                .clientSecret("client-secret")
                .build();
    }
}
