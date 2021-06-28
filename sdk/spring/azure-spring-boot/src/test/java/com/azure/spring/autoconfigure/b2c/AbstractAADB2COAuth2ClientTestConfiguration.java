// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import com.azure.spring.aad.AADAuthorizationGrantType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

import java.util.Map;

public abstract class AbstractAADB2COAuth2ClientTestConfiguration {

    @EnableWebSecurity
    @Import(OAuth2ClientAutoConfiguration.class)
    public static class WebOAuth2ClientApp {

    }

    @EnableWebSecurity
    public static class WebResourceServerApp {

    }

    protected static final String AAD_B2C_ENABLE_CONFIG_FILE_NAME = "aadb2c.enable.config";

    abstract WebApplicationContextRunner getDefaultContextRunner();

    protected String[] getAuthorizationClientPropertyValues() {
        return new String[]{ String.format("%s.%s.scopes=%s", AADB2CConstants.AUTHORIZATION_CLIENTS,
                AADB2CConstants.CLIENT_CREDENTIAL_NAME, AADB2CConstants.TEST_CLIENT_CREDENTIAL_SCOPES),
            String.format("%s.%s.authorization-grant-type=%s", AADB2CConstants.AUTHORIZATION_CLIENTS,
                AADB2CConstants.CLIENT_CREDENTIAL_NAME, AADB2CConstants.TEST_CLIENT_CREDENTIAL_GRANT_TYPE),
        };
    }

    @Test
    public void testClientCredentialProperties() {
        getDefaultContextRunner()
            .withPropertyValues(getAuthorizationClientPropertyValues())
            .run(c -> {
                final AADB2CProperties properties = c.getBean(AADB2CProperties.class);
                Assertions.assertNotNull(properties);
                Map<String, AuthorizationClientProperties> authorizationClients = properties.getAuthorizationClients();
                Assertions.assertTrue(authorizationClients.size() > 0);
                for (String clientName: authorizationClients.keySet()) {
                    Assertions.assertEquals(clientName, AADB2CConstants.CLIENT_CREDENTIAL_NAME);
                    Assertions.assertEquals(authorizationClients.get(clientName).getScopes().get(0),
                        AADB2CConstants.TEST_CLIENT_CREDENTIAL_SCOPES);
                    Assertions.assertEquals(authorizationClients.get(clientName).getAuthorizationGrantType(),
                        AADAuthorizationGrantType.CLIENT_CREDENTIALS);
                }
            });
    }

    @Test
    public void testClientRelatedBeans() {
        getDefaultContextRunner()
            .withPropertyValues(getAuthorizationClientPropertyValues())
            .run(c -> {
                final AADB2COAuth2ClientConfiguration config = c.getBean(AADB2COAuth2ClientConfiguration.class);
                final ClientRegistrationRepository clientRepo = c.getBean(ClientRegistrationRepository.class);
                final OAuth2AuthorizedClientService clientService = c.getBean(OAuth2AuthorizedClientService.class);
                final OAuth2AuthorizedClientRepository authorizedClientRepo =
                    c.getBean(OAuth2AuthorizedClientRepository.class);
                final OAuth2AuthorizedClientManager clientManager = c.getBean(OAuth2AuthorizedClientManager.class);

                Assertions.assertNotNull(config);
                Assertions.assertNotNull(clientRepo);
                Assertions.assertNotNull(clientService);
                Assertions.assertNotNull(authorizedClientRepo);
                Assertions.assertNotNull(clientManager);
            });
    }
}
