// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.aad.serde.jackson;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import tools.jackson.core.Version;
import tools.jackson.databind.module.SimpleModule;

/**
 * Jackson {@code Module} for ClientRegistration
 */
class AadOAuth2ClientJacksonModule extends SimpleModule {

    private static final long serialVersionUID = 30_80_00L;

    AadOAuth2ClientJacksonModule() {
        super(AadOAuth2ClientJacksonModule.class.getName(), new Version(3, 8, 0, null, null, null));
    }

    @Override
    public void setupModule(SetupContext context) {
        context.setMixIn(ClientRegistration.class, AadClientRegistrationMixin.class);
    }

}
