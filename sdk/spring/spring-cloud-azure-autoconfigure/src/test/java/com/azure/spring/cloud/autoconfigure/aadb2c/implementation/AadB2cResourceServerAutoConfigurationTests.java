// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c.implementation;

import com.azure.spring.cloud.autoconfigure.aadb2c.AadB2cTrustedIssuerRepository;
import com.azure.spring.cloud.autoconfigure.aadb2c.properties.AadB2cProperties;
import com.azure.spring.cloud.autoconfigure.aadb2c.AadB2cResourceServerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.implementation.jwt.AadIssuerJwsKeySelector;
import com.azure.spring.cloud.autoconfigure.aad.AadTrustedIssuerRepository;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import com.nimbusds.jwt.proc.JWTProcessor;
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
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class AadB2cResourceServerAutoConfigurationTests extends AbstractAadB2cOAuth2ClientTestConfigurations {

    private WebApplicationContextRunner getResourceServerContextRunner() {
        return new WebApplicationContextRunner()
            .withClassLoader(new FilteredClassLoader(OAuth2LoginAuthenticationFilter.class))
            .withConfiguration(AutoConfigurations.of(
                AzureGlobalPropertiesAutoConfiguration.class,
                WebResourceServerApp.class,
                AadB2cResourceServerAutoConfiguration.class))
            .withPropertyValues(getB2CResourceServerProperties());
    }

    @Override
    WebApplicationContextRunner getDefaultContextRunner() {
        return new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                WebOAuth2ClientApp.class,
                AzureGlobalPropertiesAutoConfiguration.class,
                AadB2cResourceServerAutoConfiguration.class))
            .withPropertyValues(getB2CResourceServerProperties());
    }

    private String[] getB2CResourceServerProperties() {
        return new String[] {
            "spring.cloud.azure.active-directory.b2c.enabled=true",
            String.format("%s=%s", AadB2cConstants.BASE_URI, AadB2cConstants.TEST_BASE_URI),
            String.format("%s=%s", AadB2cConstants.TENANT_ID, AadB2cConstants.TEST_TENANT_ID),
            String.format("%s=%s", AadB2cConstants.CLIENT_ID, AadB2cConstants.TEST_CLIENT_ID),
            String.format("%s=%s", AadB2cConstants.APP_ID_URI, AadB2cConstants.TEST_APP_ID_URI),
            String.format("%s.%s=%s", AadB2cConstants.USER_FLOWS, AadB2cConstants.TEST_KEY_SIGN_UP_OR_IN,
                AadB2cConstants.TEST_SIGN_UP_OR_IN_NAME),
        };
    }

    private ContextConsumer<ApplicationContext> b2CAutoConfigurationBean() {
        return (c) -> {
            final AadB2cResourceServerAutoConfiguration autoResourceConfig =
                c.getBean(AadB2cResourceServerAutoConfiguration.class);
            Assertions.assertNotNull(autoResourceConfig);
        };
    }

    private ContextConsumer<ApplicationContext> b2CResourceServerPropertiesBean() {
        return (c) -> {
            final AadB2cProperties properties = c.getBean(AadB2cProperties.class);

            Assertions.assertNotNull(properties);
            Assertions.assertEquals(properties.getProfile().getTenantId(), AadB2cConstants.TEST_TENANT_ID);
            Assertions.assertEquals(properties.getCredential().getClientId(), AadB2cConstants.TEST_CLIENT_ID);
            Assertions.assertEquals(properties.getAppIdUri(), AadB2cConstants.TEST_APP_ID_URI);
        };
    }

    private ContextConsumer<ApplicationContext> b2CResourceServerBean() {
        return (c) -> {
            final JwtDecoder jwtDecoder = c.getBean(JwtDecoder.class);
            final AadIssuerJwsKeySelector jwsKeySelector = c.getBean(AadIssuerJwsKeySelector.class);
            final AadTrustedIssuerRepository issuerRepository = c.getBean(AadTrustedIssuerRepository.class);
            Assertions.assertNotNull(jwtDecoder);
            Assertions.assertNotNull(jwsKeySelector);
            Assertions.assertNotNull(issuerRepository);
        };
    }

    @Test
    void testB2COAuth2ClientAutoConfigurationBean() {
        getDefaultContextRunner().withPropertyValues(getAuthorizationClientPropertyValues())
                                 .run(b2CAutoConfigurationBean());
    }

    @Test
    void testB2COnlyAutoConfigurationBean() {
        getResourceServerContextRunner().run(b2CAutoConfigurationBean());
    }

    @Test
    void testB2COAuth2ClientResourceServerPropertiesBean() {
        getDefaultContextRunner().withPropertyValues(getAuthorizationClientPropertyValues())
                                 .run(b2CResourceServerPropertiesBean());
    }

    @Test
    void testB2COnlyResourceServerPropertiesBean() {
        getResourceServerContextRunner().run(b2CResourceServerPropertiesBean());
    }

    @Test
    void testB2COAuth2ClientResourceServerBean() {
        getDefaultContextRunner().withPropertyValues(getAuthorizationClientPropertyValues())
                                 .run(b2CResourceServerBean());
    }

    @Test
    void testB2COnlyResourceServerBean() {
        getResourceServerContextRunner().run(b2CResourceServerBean());
    }

    @Test
    void testResourceServerConditionsIsInvokedWhenAADB2CEnableFileExists() {
        try (MockedStatic<BeanUtils> beanUtils = mockStatic(BeanUtils.class, Mockito.CALLS_REAL_METHODS)) {
            AadB2cConditions.ClientRegistrationCondition clientRegistrationCondition =
                spy(AadB2cConditions.ClientRegistrationCondition.class);
            beanUtils.when(() -> BeanUtils.instantiateClass(AadB2cConditions.ClientRegistrationCondition.class))
                     .thenReturn(clientRegistrationCondition);
            getDefaultContextRunner()
                .withPropertyValues(getAuthorizationClientPropertyValues())
                .run(c -> {
                    verify(clientRegistrationCondition, atLeastOnce()).getMatchOutcome(any(), any());
                });
        }
    }

    @Test
    void testResourceServerConditionsIsNotInvokedWhenAADB2CEnableFileDoesNotExists() {
        try (MockedStatic<BeanUtils> beanUtils = mockStatic(BeanUtils.class, Mockito.CALLS_REAL_METHODS)) {
            AadB2cConditions.ClientRegistrationCondition clientRegistrationCondition =
                mock(AadB2cConditions.ClientRegistrationCondition.class);
            beanUtils.when(() -> BeanUtils.instantiateClass(AadB2cConditions.ClientRegistrationCondition.class))
                     .thenReturn(clientRegistrationCondition);
            new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(WebOAuth2ClientApp.class,
                    AadB2cResourceServerAutoConfiguration.class))
                .withPropertyValues(getB2CResourceServerProperties())
                .withPropertyValues(getAuthorizationClientPropertyValues())
                .run(c -> {
                    verify(clientRegistrationCondition, never()).getMatchOutcome(any(), any());
                });
        }
    }

    @Test
    void testExistAADB2CTrustedIssuerRepositoryBean() {
        getDefaultContextRunner()
            .withPropertyValues(getB2CResourceServerProperties())
            .withUserConfiguration(AadB2cResourceServerAutoConfiguration.class)
            .run(context -> {
                final AadB2cTrustedIssuerRepository aadb2CTrustedIssuerRepository =
                    context.getBean(AadB2cTrustedIssuerRepository.class);
                assertThat(aadb2CTrustedIssuerRepository).isNotNull();
                assertThat(aadb2CTrustedIssuerRepository).isExactlyInstanceOf(AadB2cTrustedIssuerRepository.class);
            });
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void testExistjwtProcessorBean() {
        getDefaultContextRunner()
            .withPropertyValues(getB2CResourceServerProperties())
            .withUserConfiguration(AadB2cResourceServerAutoConfiguration.class)
            .run(context -> {
                JWTProcessor<SecurityContext> jwtProcessor = context.getBean(JWTProcessor.class);
                assertThat(jwtProcessor).isNotNull();
                assertThat(jwtProcessor).isExactlyInstanceOf(DefaultJWTProcessor.class);
            });
    }

    @Test
    void testExistJwtDecoderBean() {
        getDefaultContextRunner()
            .withPropertyValues(getB2CResourceServerProperties())
            .withUserConfiguration(AadB2cResourceServerAutoConfiguration.class)
            .run(context -> {
                final JwtDecoder jwtDecoder = context.getBean(JwtDecoder.class);
                assertThat(jwtDecoder).isNotNull();
                assertThat(jwtDecoder).isExactlyInstanceOf(NimbusJwtDecoder.class);
            });
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void testExistJWTClaimsSetAwareJWSKeySelectorBean() {
        getDefaultContextRunner()
            .withPropertyValues(getB2CResourceServerProperties())
            .withUserConfiguration(AadB2cResourceServerAutoConfiguration.class)
            .run(context -> {
                final JWTClaimsSetAwareJWSKeySelector jwsKeySelector =
                    context.getBean(JWTClaimsSetAwareJWSKeySelector.class);
                assertThat(jwsKeySelector).isNotNull();
                assertThat(jwsKeySelector).isExactlyInstanceOf(AadIssuerJwsKeySelector.class);
            });
    }
}
