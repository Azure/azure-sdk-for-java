// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aadb2c.security;

import com.azure.spring.cloud.autoconfigure.implementation.aad.security.constants.AadJwtClaimNames;
import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.configuration.properties.AadB2cProperties;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
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

class AadB2cOidcIdTokenDecoderFactoryTests {

    private static final String CLIENT_ID = "client-id";
    private static final String TENANT_ID = "fake-tenant-id";
    private static final String BASE_URI = "https://faketenant.b2clogin.com/faketenant.onmicrosoft.com";
    private static final String USER_FLOW_SIGN_UP_OR_IN = "my-sign-up-or-in";
    private static final String JWK_SET_URI =
        "https://faketenant.b2clogin.com/faketenant.onmicrosoft.com/discovery/v2.0/keys";
    private static final String TRUSTED_ISSUER = "https://faketenant.b2clogin.com/" + TENANT_ID + "/v2.0/";

    @Test
    void createDecoderRejectsMissingJwkSetUri() {
        AadB2cOidcIdTokenDecoderFactory factory = createFactory();
        ClientRegistration clientRegistration = clientRegistration(null);
        assertThrows(OAuth2AuthenticationException.class, () -> factory.createDecoder(clientRegistration));
    }

    @Test
    void acceptsTrustedIssuer() {
        OAuth2TokenValidator<Jwt> validator = extractValidator(createFactory());
        Jwt jwt = idToken(TRUSTED_ISSUER, List.of(CLIENT_ID));
        assertThat(validator.validate(jwt).getErrors()).isEmpty();
    }

    @Test
    void rejectsUntrustedIssuer() {
        OAuth2TokenValidator<Jwt> validator = extractValidator(createFactory());
        Jwt jwt = idToken("https://attacker.b2clogin.com/attacker-tenant-id/v2.0/", List.of(CLIENT_ID));
        assertThat(validator.validate(jwt).getErrors()).isNotEmpty();
    }

    @Test
    void rejectsWrongAudience() {
        OAuth2TokenValidator<Jwt> validator = extractValidator(createFactory());
        Jwt jwt = idToken(TRUSTED_ISSUER, List.of("some-other-client-id"));
        assertThat(validator.validate(jwt).getErrors()).isNotEmpty();
    }

    private static Jwt idToken(String issuer, List<String> audience) {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim(AadJwtClaimNames.ISS, issuer)
                .subject("subject")
                .audience(audience)
                .issuedAt(Instant.now().minusSeconds(60))
                .expiresAt(Instant.now().plusSeconds(600))
                .build();
    }

    @SuppressWarnings("unchecked")
    private static OAuth2TokenValidator<Jwt> extractValidator(AadB2cOidcIdTokenDecoderFactory factory) {
        JwtDecoder decoder = factory.createDecoder(clientRegistration(JWK_SET_URI));
        return (OAuth2TokenValidator<Jwt>) getField(NimbusJwtDecoder.class, "jwtValidator", decoder);
    }

    private static AadB2cOidcIdTokenDecoderFactory createFactory() {
        AadB2cProperties properties = new AadB2cProperties();
        properties.setBaseUri(BASE_URI);
        properties.getProfile().setTenantId(TENANT_ID);
        properties.getCredential().setClientId(CLIENT_ID);
        properties.getUserFlows().put(AadB2cProperties.DEFAULT_KEY_SIGN_UP_OR_SIGN_IN, USER_FLOW_SIGN_UP_OR_IN);
        return new AadB2cOidcIdTokenDecoderFactory(new RestTemplate(), properties);
    }

    private static ClientRegistration clientRegistration(String jwkSetUri) {
        ClientRegistration.Builder builder = ClientRegistration
                .withRegistrationId("b2c")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/{action}/oauth2/code/{registrationId}")
                .scope("openid")
                .authorizationUri("https://faketenant.b2clogin.com/oauth2/v2.0/authorize")
                .tokenUri("https://faketenant.b2clogin.com/oauth2/v2.0/token")
                .clientName("b2c")
                .clientId(CLIENT_ID)
                .clientSecret("client-secret");
        if (jwkSetUri != null) {
            builder.jwkSetUri(jwkSetUri);
        }
        return builder.build();
    }
}
