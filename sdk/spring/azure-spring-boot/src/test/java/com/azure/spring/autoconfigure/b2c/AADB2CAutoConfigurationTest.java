// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AADB2CAutoConfigurationTest extends AbstractAADB2COAuth2ClientTestConfiguration {

    public AADB2CAutoConfigurationTest() {
        contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(WebOAuth2ClientApp.class, AADB2CAutoConfiguration.class))
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .withPropertyValues(
                String.format("%s=%s", AADB2CConstants.BASE_URI, AADB2CConstants.TEST_BASE_URI),
                String.format("%s=%s", AADB2CConstants.TENANT_ID, AADB2CConstants.TEST_TENANT_ID),
                String.format("%s=%s", AADB2CConstants.CLIENT_ID, AADB2CConstants.TEST_CLIENT_ID),
                String.format("%s=%s", AADB2CConstants.CLIENT_SECRET, AADB2CConstants.TEST_CLIENT_SECRET),
                String.format("%s=%s", AADB2CConstants.LOGOUT_SUCCESS_URL, AADB2CConstants.TEST_LOGOUT_SUCCESS_URL),
                String.format("%s=%s", AADB2CConstants.LOGIN_FLOW, AADB2CConstants.TEST_KEY_SIGN_UP_OR_IN),
                String.format("%s.%s=%s", AADB2CConstants.USER_FLOWS,
                    AADB2CConstants.TEST_KEY_SIGN_UP_OR_IN, AADB2CConstants.TEST_SIGN_UP_OR_IN_NAME),
                String.format("%s.%s=%s", AADB2CConstants.USER_FLOWS,
                    AADB2CConstants.TEST_KEY_SIGN_IN, AADB2CConstants.TEST_SIGN_IN_NAME),
                String.format("%s.%s=%s", AADB2CConstants.USER_FLOWS,
                    AADB2CConstants.TEST_KEY_SIGN_UP, AADB2CConstants.TEST_SIGN_UP_NAME),
                String.format("%s=%s", AADB2CConstants.CONFIG_PROMPT, AADB2CConstants.TEST_PROMPT),
                String.format("%s=%s", AADB2CConstants.CONFIG_LOGIN_HINT, AADB2CConstants.TEST_LOGIN_HINT),
                String.format("%s=%s", AADB2CConstants.USER_NAME_ATTRIBUTE_NAME, AADB2CConstants.TEST_ATTRIBUTE_NAME),
                String.format("%s=%s", AADB2CConstants.USER_NAME_ATTRIBUTE_NAME, AADB2CConstants.TEST_ATTRIBUTE_NAME)
            );
    }

    @Test
    public void testAutoConfigurationBean() {
        this.contextRunner.run(c -> {
            final AADB2CAutoConfiguration autoConfig = c.getBean(AADB2CAutoConfiguration.class);
            Assertions.assertNotNull(autoConfig);
        });
    }

    @Test
    public void testPropertiesBean() {
        this.contextRunner.run(c -> {
            final AADB2CProperties properties = c.getBean(AADB2CProperties.class);

            Assertions.assertNotNull(properties);
            Assertions.assertEquals(properties.getClientId(), AADB2CConstants.TEST_CLIENT_ID);
            Assertions.assertEquals(properties.getClientSecret(), AADB2CConstants.TEST_CLIENT_SECRET);
            Assertions.assertEquals(properties.getUserNameAttributeName(), AADB2CConstants.TEST_ATTRIBUTE_NAME);

            Map<String, String> userFlows = properties.getUserFlows();
            Assertions.assertTrue(userFlows.size() > 0);
            final Object prompt = properties.getAuthenticateAdditionalParameters().get(AADB2CConstants.PROMPT);
            final String loginHint =
                String.valueOf(properties.getAuthenticateAdditionalParameters().get(AADB2CConstants.LOGIN_HINT));
            Set<Object> clientNames = new HashSet<>(Arrays.asList(AADB2CConstants.TEST_SIGN_IN_NAME,
                AADB2CConstants.TEST_SIGN_UP_NAME, AADB2CConstants.TEST_SIGN_UP_OR_IN_NAME));
            for (String clientName: userFlows.keySet()) {
                Assertions.assertTrue(clientNames.contains(userFlows.get(clientName)));
            }
            Assertions.assertEquals(prompt, AADB2CConstants.TEST_PROMPT);
            Assertions.assertEquals(loginHint, AADB2CConstants.TEST_LOGIN_HINT);
        });
    }

    @Test
    public void testAADB2CAuthorizationRequestResolverBean() {
        this.contextRunner.run(c -> {
            final AADB2CAuthorizationRequestResolver resolver = c.getBean(AADB2CAuthorizationRequestResolver.class);
            Assertions.assertNotNull(resolver);
        });
    }

    @Test
    public void testLogoutSuccessHandlerBean() {
        this.contextRunner.run(c -> {
            final AADB2CLogoutSuccessHandler handler = c.getBean(AADB2CLogoutSuccessHandler.class);
            Assertions.assertNotNull(handler);
        });
    }
}
