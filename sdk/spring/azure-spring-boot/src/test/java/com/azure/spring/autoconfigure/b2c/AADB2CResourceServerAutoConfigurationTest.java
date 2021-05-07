// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import com.azure.spring.aad.AADIssuerJWSKeySelector;
import com.azure.spring.aad.AADTrustedIssuerRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ContextConsumer;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class AADB2CResourceServerAutoConfigurationTest extends AbstractAADB2COAuth2ClientTestConfiguration {

    private final WebApplicationContextRunner resourceServerContextRunner = new WebApplicationContextRunner()
        .withClassLoader(new FilteredClassLoader(OAuth2LoginAuthenticationFilter.class))
        .withConfiguration(AutoConfigurations.of(WebResourceServerApp.class,
            AADB2CResourceServerAutoConfiguration.class))
        .withPropertyValues(getB2CResourceServerProperties());

    public AADB2CResourceServerAutoConfigurationTest() {
        contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(WebOAuth2ClientApp.class,
                AADB2CResourceServerAutoConfiguration.class))
            .withPropertyValues(getB2CResourceServerProperties());
    }

    private String[] getB2CResourceServerProperties() {
        return new String[] {
            String.format("%s=%s", AADB2CConstants.TENANT_ID, AADB2CConstants.TEST_TENANT_ID),
            String.format("%s=%s", AADB2CConstants.CLIENT_ID, AADB2CConstants.TEST_CLIENT_ID),
            String.format("%s=%s", AADB2CConstants.APP_ID_URI, AADB2CConstants.TEST_APP_ID_URI)
        };
    }

    private ContextConsumer<ApplicationContext> b2CAutoConfigurationBean() {
        return (c) -> {
            final AADB2CResourceServerAutoConfiguration autoResourceConfig =
                c.getBean(AADB2CResourceServerAutoConfiguration.class);
            Assertions.assertNotNull(autoResourceConfig);
        };
    }

    private ContextConsumer<ApplicationContext> b2CResourceServerPropertiesBean() {
        return (c) -> {
            final AADB2CProperties properties = c.getBean(AADB2CProperties.class);

            Assertions.assertNotNull(properties);
            Assertions.assertEquals(properties.getTenantId(), AADB2CConstants.TEST_TENANT_ID);
            Assertions.assertEquals(properties.getClientId(), AADB2CConstants.TEST_CLIENT_ID);
            Assertions.assertEquals(properties.getAppIdUri(), AADB2CConstants.TEST_APP_ID_URI);
        };
    }

    private ContextConsumer<ApplicationContext> b2CResourceServerBean() {
        return (c) -> {
            final JwtDecoder jwtDecoder = c.getBean(JwtDecoder.class);
            final AADIssuerJWSKeySelector jwsKeySelector = c.getBean(AADIssuerJWSKeySelector.class);
            final AADTrustedIssuerRepository issuerRepository = c.getBean(AADTrustedIssuerRepository.class);
            Assertions.assertNotNull(jwtDecoder);
            Assertions.assertNotNull(jwsKeySelector);
            Assertions.assertNotNull(issuerRepository);
        };
    }

    @Test
    public void testB2COAuth2ClientAutoConfigurationBean() {
        this.contextRunner.withPropertyValues(getAuthorizationClientPropertyValues())
                          .run(b2CAutoConfigurationBean());
    }

    @Test
    public void testB2COnlyAutoConfigurationBean() {
        this.resourceServerContextRunner.run(b2CAutoConfigurationBean());
    }

    @Test
    public void testB2COAuth2ClientResourceServerPropertiesBean() {
        this.contextRunner.withPropertyValues(getAuthorizationClientPropertyValues())
                          .run(b2CResourceServerPropertiesBean());
    }

    @Test
    public void testB2COnlyResourceServerPropertiesBean() {
        this.resourceServerContextRunner.run(b2CResourceServerPropertiesBean());
    }

    @Test
    public void testB2COAuth2ClientResourceServerBean() {
        this.contextRunner.withPropertyValues(getAuthorizationClientPropertyValues())
                          .run(b2CResourceServerBean());
    }

    @Test
    public void testB2COnlyResourceServerBean() {
        this.resourceServerContextRunner.run(b2CResourceServerBean());
    }

    @Test
    public void testResourceServerConditionsIsInvokedWhenAADB2CEnableFileExists() {
        try (MockedStatic<BeanUtils> beanUtils = mockStatic(BeanUtils.class, Mockito.CALLS_REAL_METHODS)) {
            AADB2CConditions.ClientRegistrationCondition clientRegistrationCondition =
                spy(AADB2CConditions.ClientRegistrationCondition.class);
            beanUtils.when(() -> BeanUtils.instantiateClass(AADB2CConditions.ClientRegistrationCondition.class))
                     .thenReturn(clientRegistrationCondition);
            this.contextRunner
                .withPropertyValues(getAuthorizationClientPropertyValues())
                .run(c -> {
                    Assertions.assertTrue(c.getResource(AAD_B2C_ENABLE_CONFIG_FILE_NAME).exists());
                    verify(clientRegistrationCondition, atLeastOnce()).getMatchOutcome(any(), any());
                });
        }
    }

    @Test
    public void testResourceServerConditionsIsNotInvokedWhenAADB2CEnableFileDoesNotExists() {
        try (MockedStatic<BeanUtils> beanUtils = mockStatic(BeanUtils.class, Mockito.CALLS_REAL_METHODS)) {
            AADB2CConditions.ClientRegistrationCondition clientRegistrationCondition =
                mock(AADB2CConditions.ClientRegistrationCondition.class);
            beanUtils.when(() -> BeanUtils.instantiateClass(AADB2CConditions.ClientRegistrationCondition.class))
                     .thenReturn(clientRegistrationCondition);
            new WebApplicationContextRunner()
                .withClassLoader(new FilteredClassLoader(new ClassPathResource(AAD_B2C_ENABLE_CONFIG_FILE_NAME)))
                .withConfiguration(AutoConfigurations.of(WebOAuth2ClientApp.class,
                    AADB2CResourceServerAutoConfiguration.class))
                .withPropertyValues(getB2CResourceServerProperties())
                .withPropertyValues(getAuthorizationClientPropertyValues())
                .run(c -> {
                    Assertions.assertFalse(c.getResource(AAD_B2C_ENABLE_CONFIG_FILE_NAME).exists());
                    verify(clientRegistrationCondition, never()).getMatchOutcome(any(), any());
                });
        }
    }
}
