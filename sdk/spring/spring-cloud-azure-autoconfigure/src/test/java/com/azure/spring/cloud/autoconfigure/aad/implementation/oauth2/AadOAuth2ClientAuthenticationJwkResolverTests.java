// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.oauth2;

import com.azure.spring.cloud.autoconfigure.aad.implementation.TestJwks;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.RSAKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import javax.xml.bind.DatatypeConverter;
import java.util.function.Function;

import static com.azure.spring.cloud.autoconfigure.aad.implementation.TestClientRegistrations.clientRegistration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.JWT_BEARER;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_JWT;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.NONE;

public class AadOAuth2ClientAuthenticationJwkResolverTests {

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
        assertNull(jwkResolver.resolve(clientRegistration(JWT_BEARER, CLIENT_SECRET_JWT).build()));
        assertNull(jwkResolver.resolve(clientRegistration(JWT_BEARER, NONE).build()));
    }

    @Test
    void jwkValue() {
        RSAKey rsaJwk = Mockito.spy(TestJwks.DEFAULT_RSA_JWK);
        given(this.jwkFunction.apply(any())).willReturn(rsaJwk);

        JWK jwk = jwkFunction.apply(clientRegistration(JWT_BEARER, CLIENT_SECRET_JWT).build());
        assertEquals("F6A8558E721545972D3B8EE60BA22F913A322601",
            DatatypeConverter.printHexBinary(jwk.getX509CertThumbprint().decode()));
        assertEquals(KeyType.RSA, jwk.getKeyType());
    }
}
