// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

public class TestClientRegistrations {

    public static ClientRegistration.Builder clientRegistration(AuthorizationGrantType grantType,
                                                           ClientAuthenticationMethod method) {
        return ClientRegistration
            .withRegistrationId("test")
            .clientId("test")
            .clientSecret("test-secret")
            .clientAuthenticationMethod(method)
            .authorizationGrantType(grantType)
            .tokenUri("http://localhost/token");
    }
}
