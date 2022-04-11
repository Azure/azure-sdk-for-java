// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c.implementation;

import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthorizationGrantType;
import com.azure.spring.cloud.autoconfigure.aadb2c.AadB2cAuthorizationRequestResolver;
import com.azure.spring.cloud.autoconfigure.aadb2c.AadB2cLogoutSuccessHandler;
import com.azure.spring.cloud.autoconfigure.aadb2c.AadB2cAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.aadb2c.properties.AadB2cProperties;
import com.azure.spring.cloud.autoconfigure.aadb2c.AadB2cResourceServerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.aadb2c.properties.AuthorizationClientProperties;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

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
                assertEquals(authorizationClients.get("test").getAuthorizationGrantType(), AadAuthorizationGrantType.CLIENT_CREDENTIALS);

                Map<String, Object> authenticateAdditionalParameters = properties.getAuthenticateAdditionalParameters();
                assertEquals(authenticateAdditionalParameters.size(), 2);
                assertTrue(authenticateAdditionalParameters.containsKey("login-hint"));
                assertTrue(authenticateAdditionalParameters.containsKey("prompt"));
                assertEquals(authenticateAdditionalParameters.get("login-hint"), AadB2cConstants.TEST_LOGIN_HINT);
                assertEquals(authenticateAdditionalParameters.get("prompt"), AadB2cConstants.TEST_PROMPT);
            });
    }

    @Override
    WebApplicationContextRunner getDefaultContextRunner() {
        return new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureGlobalPropertiesAutoConfiguration.class, WebOAuth2ClientApp.class, AadB2cAutoConfiguration.class))
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
                Assertions.assertEquals(properties.getCredential().getClientId(), AadB2cConstants.TEST_CLIENT_ID);
                Assertions.assertEquals(properties.getCredential().getClientSecret(), AadB2cConstants.TEST_CLIENT_SECRET);
                Assertions.assertEquals(properties.getUserNameAttributeName(), AadB2cConstants.TEST_ATTRIBUTE_NAME);

                Map<String, String> userFlows = properties.getUserFlows();
                Assertions.assertTrue(userFlows.size() > 0);
                final Object prompt = properties.getAuthenticateAdditionalParameters().get(AadB2cConstants.PROMPT);
                final String loginHint =
                    String.valueOf(properties.getAuthenticateAdditionalParameters().get(AadB2cConstants.LOGIN_HINT));
                Set<Object> clientNames = new HashSet<>(Arrays.asList(AadB2cConstants.TEST_SIGN_IN_NAME,
                    AadB2cConstants.TEST_SIGN_UP_NAME, AadB2cConstants.TEST_SIGN_UP_OR_IN_NAME));
                for (String clientName : userFlows.keySet()) {
                    Assertions.assertTrue(clientNames.contains(userFlows.get(clientName)));
                }
                Assertions.assertEquals(prompt, AadB2cConstants.TEST_PROMPT);
                Assertions.assertEquals(loginHint, AadB2cConstants.TEST_LOGIN_HINT);
            });
    }

    @Test
    void setDefaultValueFromAzureGlobalPropertiesTest() {
        new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureGlobalPropertiesAutoConfiguration.class, WebOAuth2ClientApp.class, AadB2cAutoConfiguration.class))
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
            .withConfiguration(AutoConfigurations.of(AzureGlobalPropertiesAutoConfiguration.class, WebOAuth2ClientApp.class, AadB2cAutoConfiguration.class))
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
                .withConfiguration(AutoConfigurations.of(WebResourceServerApp.class,
                    AadB2cResourceServerAutoConfiguration.class))
                .withPropertyValues(getWebappCommonPropertyValuesWithOutGlobalConfigurableItems())
                .withPropertyValues(getGlobalConfigurableItems())
                .run(c -> {
                    verify(userFlowCondition, never()).getMatchOutcome(any(), any());
                    verify(clientRegistrationCondition, never()).getMatchOutcome(any(), any());
                });
        }
    }
}
