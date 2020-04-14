// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.autoconfigure.btoc;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import static com.microsoft.azure.spring.autoconfigure.btoc.AADB2CConstants.*;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class AADB2CAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AADB2CAutoConfiguration.class))
            .withPropertyValues(
                    String.format("%s=%s", TENANT, TEST_TENANT),
                    String.format("%s=%s", CLIENT_ID, TEST_CLIENT_ID),
                    String.format("%s=%s", CLIENT_SECRET, TEST_CLIENT_SECRET),
                    String.format("%s=%s", REPLY_URL, TEST_REPLY_URL),
                    String.format("%s=%s", LOGOUT_SUCCESS_URL, TEST_LOGOUT_SUCCESS_URL),
                    String.format("%s=%s", SIGN_UP_OR_SIGN_IN, TEST_SIGN_UP_OR_IN_NAME)
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
            assertThat(properties.getTenant()).isEqualTo(TEST_TENANT);
            assertThat(properties.getClientId()).isEqualTo(TEST_CLIENT_ID);
            assertThat(properties.getClientSecret()).isEqualTo(TEST_CLIENT_SECRET);
            assertThat(properties.getReplyUrl()).isEqualTo(TEST_REPLY_URL);

            final String signUpOrSignIn = properties.getUserFlows().getSignUpOrSignIn();

            assertThat(signUpOrSignIn).isEqualTo(TEST_SIGN_UP_OR_IN_NAME);
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
