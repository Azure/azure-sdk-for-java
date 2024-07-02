// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security;

import com.azure.spring.cloud.autoconfigure.implementation.aad.jwk.TestJwks;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.RSAKey;
import jakarta.xml.bind.DatatypeConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.JWT_BEARER;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_JWT;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.NONE;

class AadOAuth2ClientAuthenticationJwkResolverTests {

    private Function<ClientRegistration, JWK> jwkFunction;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setup() {
        this.jwkFunction = mock(Function.class);
    }

    @Test
    void resolveJwkFunction() {
        AadOAuth2ClientAuthenticationJwkResolver jwkResolver =
            new AadOAuth2ClientAuthenticationJwkResolver("D:\\test\\test.pfx", "test");

        ClientRegistration clientRegistration1 = ClientRegistration
            .withRegistrationId("test")
            .clientId("test")
            .clientSecret("test-secret")
            .clientAuthenticationMethod(CLIENT_SECRET_JWT)
            .authorizationGrantType(JWT_BEARER)
            .tokenUri("http://localhost/token")
            .build();

        assertNull(jwkResolver.resolve(clientRegistration1));

        ClientRegistration clientRegistration2 = ClientRegistration
            .withRegistrationId("test")
            .clientId("test")
            .clientSecret("test-secret")
            .clientAuthenticationMethod(NONE)
            .authorizationGrantType(JWT_BEARER)
            .tokenUri("http://localhost/token")
            .build();
        assertNull(jwkResolver.resolve(clientRegistration2));
    }

    @SuppressWarnings("deprecation")
    @Test
    void jwkValue() {
        RSAKey rsaJwk = Mockito.spy(TestJwks.DEFAULT_RSA_JWK);
        given(this.jwkFunction.apply(any())).willReturn(rsaJwk);


        ClientRegistration clientRegistration = ClientRegistration
            .withRegistrationId("test")
            .clientId("test")
            .clientSecret("test-secret")
            .clientAuthenticationMethod(CLIENT_SECRET_JWT)
            .authorizationGrantType(JWT_BEARER)
            .tokenUri("http://localhost/token")
            .build();

        JWK jwk = jwkFunction.apply(clientRegistration);
        assertEquals("F6A8558E721545972D3B8EE60BA22F913A322601",
            DatatypeConverter.printHexBinary(jwk.getX509CertThumbprint().decode()));
        assertEquals(KeyType.RSA, jwk.getKeyType());
    }
}
