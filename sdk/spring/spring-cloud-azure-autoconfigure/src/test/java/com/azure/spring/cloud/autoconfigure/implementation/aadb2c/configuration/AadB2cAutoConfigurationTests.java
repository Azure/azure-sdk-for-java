// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.aadb2c.configuration;

import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.AadB2cConstants;
import com.azure.spring.cloud.autoconfigure.implementation.aad.RestTemplateTestUtil;
import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.RestTemplateProxyCustomizerTestConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.configuration.conditions.AadB2cConditions;
import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.configuration.properties.AadB2cProperties;
import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.configuration.properties.AuthorizationClientProperties;
import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.security.AadB2cAuthorizationRequestResolver;
import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.security.AadB2cLogoutSuccessHandler;
import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.security.AadB2cOidcLoginConfigurer;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class AadB2cAutoConfigurationTests extends AbstractAadB2cOAuth2ClientTestConfigurations {

    private static final String SERVLET_WEB_APPLICATION_CLASS = "org.springframework.web.context.support.GenericWebApplicationContext";

    @Test
    void mapPropertiesSetting() {
        getDefaultContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.b2c.enabled=true",
                "spring.cloud.azure.active-directory.b2c.authorization-clients.test.authorizationGrantType = client_credentials",
                "spring.cloud.azure.active-directory.b2c.authorization-clients.test.scopes = test1,test2"
            )
            .run(context -> {
                AadB2cProperties properties = context.getBean(AadB2cProperties.class);

                Map<String, AuthorizationClientProperties> authorizationClients = properties.getAuthorizationClients();
                assertTrue(authorizationClients.containsKey("test"));
                assertTrue(authorizationClients.get("test").getScopes().containsAll(Arrays.asList("test1", "test2")));
                assertEquals(AuthorizationGrantType.CLIENT_CREDENTIALS,
                    authorizationClients.get("test").getAuthorizationGrantType());

                Map<String, Object> authenticateAdditionalParameters = properties.getAuthenticateAdditionalParameters();
                assertEquals(2, authenticateAdditionalParameters.size());
                assertTrue(authenticateAdditionalParameters.containsKey("login-hint"));
                assertTrue(authenticateAdditionalParameters.containsKey("prompt"));
                assertEquals(AadB2cConstants.TEST_LOGIN_HINT, authenticateAdditionalParameters.get("login-hint"));
                assertEquals(AadB2cConstants.TEST_PROMPT, authenticateAdditionalParameters.get("prompt"));
            });
    }

    @Override
    WebApplicationContextRunner getDefaultContextRunner() {
        return new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                AzureGlobalPropertiesAutoConfiguration.class,
                WebOAuth2ClientTestApp.class,
                AadB2cAutoConfiguration.class,
                HttpMessageConvertersAutoConfiguration.class,
                RestTemplateAutoConfiguration.class))
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .withPropertyValues(getWebappCommonPropertyValuesWithOutGlobalConfigurableItems())
            .withPropertyValues(getGlobalConfigurableItems());
    }

    private String[] getGlobalConfigurableItems() {
        return new String[] { String.format("%s=%s", AadB2cConstants.TENANT_ID, AadB2cConstants.TEST_TENANT_ID),
            String.format("%s=%s", AadB2cConstants.CLIENT_ID, AadB2cConstants.TEST_CLIENT_ID),
            String.format("%s=%s", AadB2cConstants.CLIENT_SECRET, AadB2cConstants.TEST_CLIENT_SECRET) };
    }

    private String[] getWebappCommonPropertyValuesWithOutGlobalConfigurableItems() {
        return new String[] { String.format("%s=%s", AadB2cConstants.BASE_URI, AadB2cConstants.TEST_BASE_URI),
            String.format("%s=%s", AadB2cConstants.LOGOUT_SUCCESS_URL, AadB2cConstants.TEST_LOGOUT_SUCCESS_URL),
            String.format("%s=%s", AadB2cConstants.LOGIN_FLOW, AadB2cConstants.TEST_KEY_SIGN_UP_OR_IN),
            String.format("%s.%s=%s", AadB2cConstants.USER_FLOWS,
                AadB2cConstants.TEST_KEY_SIGN_UP_OR_IN, AadB2cConstants.TEST_SIGN_UP_OR_IN_NAME),
            String.format("%s.%s=%s", AadB2cConstants.USER_FLOWS,
                AadB2cConstants.TEST_KEY_SIGN_IN, AadB2cConstants.TEST_SIGN_IN_NAME),
            String.format("%s.%s=%s", AadB2cConstants.USER_FLOWS,
                AadB2cConstants.TEST_KEY_SIGN_UP, AadB2cConstants.TEST_SIGN_UP_NAME),
            String.format("%s=%s", AadB2cConstants.CONFIG_PROMPT, AadB2cConstants.TEST_PROMPT),
            String.format("%s=%s", AadB2cConstants.CONFIG_LOGIN_HINT, AadB2cConstants.TEST_LOGIN_HINT),
            String.format("%s=%s", AadB2cConstants.USER_NAME_ATTRIBUTE_NAME, AadB2cConstants.TEST_ATTRIBUTE_NAME) };
    }

    @Test
    void servletApplication() {
        getDefaultContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.b2c.enabled=true")
            .run(context -> assertThat(context).hasSingleBean(AadB2cLogoutSuccessHandler.class));
    }

    @Test
    void nonServletApplication() {
        getDefaultContextRunner()
            .withClassLoader(new FilteredClassLoader(SERVLET_WEB_APPLICATION_CLASS))
            .withPropertyValues("spring.cloud.azure.active-directory.b2c.enabled=true")
            .run(context -> assertThat(context).doesNotHaveBean(AadB2cLogoutSuccessHandler.class));
    }

    @Test
    void testAutoConfigurationBean() {
        getDefaultContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.b2c.enabled=true")
            .run(c -> {
                final AadB2cAutoConfiguration autoConfig = c.getBean(AadB2cAutoConfiguration.class);
                Assertions.assertNotNull(autoConfig);
            });
    }

    @Test
    void testPropertiesBean() {
        getDefaultContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.b2c.enabled=true")
            .run(c -> {
                final AadB2cProperties properties = c.getBean(AadB2cProperties.class);

                Assertions.assertNotNull(properties);
                Assertions.assertEquals(AadB2cConstants.TEST_CLIENT_ID, properties.getCredential().getClientId());
                Assertions.assertEquals(AadB2cConstants.TEST_CLIENT_SECRET,
                    properties.getCredential().getClientSecret());
                Assertions.assertEquals(AadB2cConstants.TEST_ATTRIBUTE_NAME, properties.getUserNameAttributeName());

                Map<String, String> userFlows = properties.getUserFlows();
                Assertions.assertFalse(userFlows.isEmpty());
                final Object prompt = properties.getAuthenticateAdditionalParameters().get(AadB2cConstants.PROMPT);
                final String loginHint =
                    String.valueOf(properties.getAuthenticateAdditionalParameters().get(AadB2cConstants.LOGIN_HINT));
                Set<Object> clientNames = new HashSet<>(Arrays.asList(AadB2cConstants.TEST_SIGN_IN_NAME,
                    AadB2cConstants.TEST_SIGN_UP_NAME, AadB2cConstants.TEST_SIGN_UP_OR_IN_NAME));
                for (String clientName : userFlows.keySet()) {
                    Assertions.assertTrue(clientNames.contains(userFlows.get(clientName)));
                }
                Assertions.assertEquals(AadB2cConstants.TEST_PROMPT, prompt);
                Assertions.assertEquals(AadB2cConstants.TEST_LOGIN_HINT, loginHint);
            });
    }

    @Test
    void setDefaultValueFromAzureGlobalPropertiesTest() {
        new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                AzureGlobalPropertiesAutoConfiguration.class,
                WebOAuth2ClientTestApp.class,
                AadB2cAutoConfiguration.class,
                HttpMessageConvertersAutoConfiguration.class,
                RestTemplateAutoConfiguration.class))
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .withPropertyValues(getWebappCommonPropertyValuesWithOutGlobalConfigurableItems())
            .withPropertyValues(
                "spring.cloud.azure.active-directory.b2c.enabled = true",
                "spring.cloud.azure.credential.client-id = global-client-id",
                "spring.cloud.azure.credential.client-secret = global-client-secret",
                "spring.cloud.azure.profile.tenant-id = global-tenant-id",
                "spring.cloud.azure.active-directory.b2c.credential.client-id = aad-client-id",
                "spring.cloud.azure.active-directory.b2c.credential.client-secret = aad-client-secret",
                "spring.cloud.azure.active-directory.b2c.profile.tenant-id = aad-tenant-id"
            )
            .run(context -> {
                AadB2cProperties properties = context.getBean(AadB2cProperties.class);
                assertEquals("aad-client-id", properties.getCredential().getClientId());
                assertEquals("aad-client-secret", properties.getCredential().getClientSecret());
                assertEquals("aad-tenant-id", properties.getProfile().getTenantId());
            });
        new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                AzureGlobalPropertiesAutoConfiguration.class,
                WebOAuth2ClientTestApp.class,
                AadB2cAutoConfiguration.class,
                HttpMessageConvertersAutoConfiguration.class,
                RestTemplateAutoConfiguration.class))
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .withPropertyValues(getWebappCommonPropertyValuesWithOutGlobalConfigurableItems())
            .withPropertyValues(
                "spring.cloud.azure.active-directory.b2c.enabled = true",
                "spring.cloud.azure.credential.client-id = global-client-id",
                "spring.cloud.azure.credential.client-secret = global-client-secret",
                "spring.cloud.azure.profile.tenant-id = global-tenant-id"
            )
            .run(context -> {
                AadB2cProperties properties = context.getBean(AadB2cProperties.class);
                assertEquals("global-client-id", properties.getCredential().getClientId());
                assertEquals("global-client-secret", properties.getCredential().getClientSecret());
                assertEquals("global-tenant-id", properties.getProfile().getTenantId());
            });
    }

    @Test
    void testAADB2CAuthorizationRequestResolverBean() {
        getDefaultContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.b2c.enabled=true")
            .run(c -> {
                final AadB2cAuthorizationRequestResolver resolver = c.getBean(AadB2cAuthorizationRequestResolver.class);
                Assertions.assertNotNull(resolver);
            });
    }

    @Test
    void testLogoutSuccessHandlerBean() {
        getDefaultContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.b2c.enabled=true")
            .run(c -> {
                final AadB2cLogoutSuccessHandler handler = c.getBean(AadB2cLogoutSuccessHandler.class);
                Assertions.assertNotNull(handler);
            });
    }

    @Test
    void testWebappConditionsIsInvokedWhenAADB2CEnabled() {
        try (MockedStatic<BeanUtils> beanUtils = mockStatic(BeanUtils.class, Mockito.CALLS_REAL_METHODS)) {
            AadB2cConditions.UserFlowCondition userFlowCondition = spy(AadB2cConditions.UserFlowCondition.class);
            AadB2cConditions.ClientRegistrationCondition clientRegistrationCondition =
                spy(AadB2cConditions.ClientRegistrationCondition.class);
            beanUtils.when(() -> BeanUtils.instantiateClass(AadB2cConditions.UserFlowCondition.class))
                     .thenReturn(userFlowCondition);
            beanUtils.when(() -> BeanUtils.instantiateClass(AadB2cConditions.ClientRegistrationCondition.class))
                     .thenReturn(clientRegistrationCondition);
            getDefaultContextRunner()
                .withPropertyValues("spring.cloud.azure.active-directory.b2c.enabled=true")
                .run(c -> {
                    verify(userFlowCondition, atLeastOnce()).getMatchOutcome(any(), any());
                    verify(clientRegistrationCondition, atLeastOnce()).getMatchOutcome(any(), any());
                });
        }
    }

    @Test
    void testWebappConditionsIsNotInvokedWhenAADB2CDisabled() {
        try (MockedStatic<BeanUtils> beanUtils = mockStatic(BeanUtils.class, Mockito.CALLS_REAL_METHODS)) {
            AadB2cConditions.UserFlowCondition userFlowCondition = mock(AadB2cConditions.UserFlowCondition.class);
            AadB2cConditions.ClientRegistrationCondition clientRegistrationCondition =
                spy(AadB2cConditions.ClientRegistrationCondition.class);
            beanUtils.when(() -> BeanUtils.instantiateClass(AadB2cConditions.UserFlowCondition.class))
                .thenReturn(userFlowCondition);
            beanUtils.when(() -> BeanUtils.instantiateClass(AadB2cConditions.ClientRegistrationCondition.class))
                .thenReturn(clientRegistrationCondition);
            new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        WebResourceServerTestApp.class,
                        AadB2cResourceServerAutoConfiguration.class,
                        HttpMessageConvertersAutoConfiguration.class,
                        RestTemplateAutoConfiguration.class))
                .withPropertyValues(getWebappCommonPropertyValuesWithOutGlobalConfigurableItems())
                .withPropertyValues(getGlobalConfigurableItems())
                .run(c -> {
                    verify(userFlowCondition, never()).getMatchOutcome(any(), any());
                    verify(clientRegistrationCondition, never()).getMatchOutcome(any(), any());
                });
        }
    }

    @Test
    void testRestTemplateWellConfigured() {
        getDefaultContextRunner()
            .withUserConfiguration(RestTemplateProxyCustomizerTestConfiguration.class, AadB2cTestWebSecurityConfiguration.class)
            .withPropertyValues("spring.cloud.azure.active-directory.b2c.enabled=true")
            .run(RestTemplateTestUtil::assertRestTemplateWellConfigured);
    }

    @EnableWebSecurity
    public static class AadB2cTestWebSecurityConfiguration {

        @Bean
        public SecurityFilterChain apiFilterChain(HttpSecurity http, AadB2cOidcLoginConfigurer configurer) throws Exception {
            // @formatter:off
            http
                .authorizeHttpRequests(req -> req.anyRequest().authenticated())
                .with(configurer, Customizer.withDefaults());
            // @formatter:on
            return http.build();
        }
    }
}
