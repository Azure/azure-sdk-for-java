// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.implementation.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

/**
 * Jackson {@code Module} for ClientRegistration
 */
public class AADOAuth2ClientJackson2Module extends SimpleModule {

    private static final long serialVersionUID = 30_80_00L;

    public AADOAuth2ClientJackson2Module() {
        super(AADOAuth2ClientJackson2Module.class.getName(), new Version(3, 8, 0, null, null, null));
    }

    @Override
    public void setupModule(SetupContext context) {
        context.setMixInAnnotations(ClientRegistration.class, AADClientRegistrationMixin.class);
    }

}
