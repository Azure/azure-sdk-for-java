// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;

import java.util.Collections;


/**
 * Jackson databind module for AAD related classes.
 */
public class AADDatabindModule extends SimpleModule {


    private static final long serialVersionUID = 300L;

    public AADDatabindModule() {
        super(AADDatabindModule.class.getName(), new Version(3, 0, 0, null, null, null));
    }

    @Override
    public void setupModule(SetupContext context) {
        context.setMixInAnnotations(OAuth2AuthorizedClient.class, AADOAuth2AuthorizedClientMixin.class);
        context.setMixInAnnotations(ClientRegistration.class, AADClientRegistrationMixin.class);
        context.setMixInAnnotations(OAuth2AccessToken.class, AADOAuth2AccessTokenMixin.class);
        context.setMixInAnnotations(OAuth2RefreshToken.class, AADOAuth2RefreshTokenMixin.class);
        context.setMixInAnnotations(Collections.unmodifiableMap(Collections.emptyMap()).getClass(),
            AADUnmodifiableMapMixin.class);
    }
}
