// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.jwt;

import com.azure.spring.cloud.autoconfigure.aad.AadOAuth2ClientAuthenticationJWKResolver;
import com.azure.spring.cloud.autoconfigure.aad.OAuth2ClientAuthenticationJWKResolver;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyType;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import javax.xml.bind.DatatypeConverter;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_JWT;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.NONE;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.PRIVATE_KEY_JWT;

public class AadOAuth2ClientAuthenticationJWKResolverTests {

    private OAuth2ClientAuthenticationJWKResolver resolver =
        new AadOAuth2ClientAuthenticationJWKResolver(
            "src/test/resources/aad/encrypted-private-key-and-certificate.pfx", "myPassword1");
    private Function<ClientRegistration, JWK> jwkFunction = resolver.resolve();

    @Test
    void functionReturnJwk() {
        assertNotNull(jwkFunction.apply(getClientRegistration(PRIVATE_KEY_JWT)));
        assertNull(jwkFunction.apply(getClientRegistration(CLIENT_SECRET_JWT)));
        assertNull(jwkFunction.apply(getClientRegistration(NONE)));
    }

    @Test
    void jwkValue() {
        JWK jwk = jwkFunction.apply(getClientRegistration(PRIVATE_KEY_JWT));
        assertEquals("D829DB4885D1C22B1207F72F533DFA8125861174",
            DatatypeConverter.printHexBinary(jwk.getX509CertThumbprint().decode()));
        assertEquals(KeyType.RSA, jwk.getKeyType());
    }

    private ClientRegistration getClientRegistration(ClientAuthenticationMethod method) {
        return ClientRegistration
            .withRegistrationId("test")
            .clientId("test")
            .clientSecret("test-secret")
            .clientAuthenticationMethod(method)
            .authorizationGrantType(AuthorizationGrantType.JWT_BEARER)
            .tokenUri("http://localhost/token")
            .build();
    }


}
