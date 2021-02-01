// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import org.junit.Test;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AADB2CAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
        .withUserConfiguration(AADB2CAutoConfiguration.class)
        .withPropertyValues(
            String.format("%s=%s", AADB2CConstants.TENANT, AADB2CConstants.TEST_TENANT),
            String.format("%s=%s", AADB2CConstants.CLIENT_ID, AADB2CConstants.TEST_CLIENT_ID),
            String.format("%s=%s", AADB2CConstants.CLIENT_SECRET, AADB2CConstants.TEST_CLIENT_SECRET),
            String.format("%s=%s", AADB2CConstants.REPLY_URL, AADB2CConstants.TEST_REPLY_URL),
            String.format("%s=%s", AADB2CConstants.LOGOUT_SUCCESS_URL, AADB2CConstants.TEST_LOGOUT_SUCCESS_URL),
            String.format("%s=%s", AADB2CConstants.SIGN_IN_USER_FLOW, AADB2CConstants.TEST_SIGN_UP_OR_IN_NAME),
            String.format("%s=%s,%s", AADB2CConstants.USER_FLOWS,
                AADB2CConstants.TEST_SIGN_IN_NAME, AADB2CConstants.TEST_SIGN_UP_NAME),
            String.format("%s=%s", AADB2CConstants.CONFIG_PROMPT, AADB2CConstants.TEST_PROMPT),
            String.format("%s=%s", AADB2CConstants.CONFIG_LOGIN_HINT, AADB2CConstants.TEST_LOGIN_HINT),
            String.format("%s=%s", AADB2CConstants.USER_NAME_ATTRIBUTE_NAME, AADB2CConstants.TEST_ATTRIBUTE_NAME)
        );

    @Test
    public void testAutoConfigurationBean() {
        this.contextRunner.run(c -> {
            final AADB2CAutoConfiguration config = c.getBean(AADB2CAutoConfiguration.class);

            assertThat(config).isNotNull();
        });
    }

    @Test
    public void testPropertiesBean() {
        this.contextRunner.run(c -> {
            final AADB2CProperties properties = c.getBean(AADB2CProperties.class);

            assertThat(properties).isNotNull();
            assertThat(properties.getTenant()).isEqualTo(AADB2CConstants.TEST_TENANT);
            assertThat(properties.getClientId()).isEqualTo(AADB2CConstants.TEST_CLIENT_ID);
            assertThat(properties.getClientSecret()).isEqualTo(AADB2CConstants.TEST_CLIENT_SECRET);
            assertThat(properties.getReplyUrl()).isEqualTo(AADB2CConstants.TEST_REPLY_URL);
            assertThat(properties.getUserNameAttributeName()).isEqualTo(AADB2CConstants.TEST_ATTRIBUTE_NAME);

            List<String> userFlows = properties.getUserFlows();
            final Object prompt = properties.getAuthenticateAdditionalParameters().get(AADB2CConstants.PROMPT);
            final String loginHint =
                String.valueOf(properties.getAuthenticateAdditionalParameters().get(AADB2CConstants.LOGIN_HINT));
            for (String userFlow: userFlows) {
                assertThat(userFlow).isIn(AADB2CConstants.TEST_SIGN_UP_OR_IN_NAME,
                    AADB2CConstants.TEST_SIGN_IN_NAME, AADB2CConstants.TEST_SIGN_UP_NAME);
            }
            assertThat(prompt).isEqualTo(AADB2CConstants.TEST_PROMPT);
            assertThat(loginHint).isEqualTo(AADB2CConstants.TEST_LOGIN_HINT);
        });
    }

    @Test
    public void testOAuth2AuthorizedClientRepositoryBean() {
        this.contextRunner.run(c -> {
            final OAuth2AuthorizedClientRepository authorizedClientRepository = c.getBean(OAuth2AuthorizedClientRepository.class);

            assertThat(authorizedClientRepository).isNotNull();
        });
    }

    @Test
    public void testAADB2CAuthorizationRequestResolverBean() {
        this.contextRunner.run(c -> {
            final AADB2CAuthorizationRequestResolver resolver = c.getBean(AADB2CAuthorizationRequestResolver.class);

            assertThat(resolver).isNotNull();
        });
    }

    @Test
    public void testLogoutSuccessHandlerBean() {
        this.contextRunner.run(c -> {
            final AADB2CLogoutSuccessHandler handler = c.getBean(AADB2CLogoutSuccessHandler.class);

            assertThat(handler).isNotNull();
        });
    }

    @Test
    public void testFilterBean() {
        this.contextRunner.run(c -> {
            final ClientRegistrationRepository repository = c.getBean(ClientRegistrationRepository.class);

            assertThat(repository).isNotNull();
        });
    }
}
