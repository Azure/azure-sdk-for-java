// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import com.azure.spring.aad.AADIssuerJWSKeySelector;
import com.azure.spring.aad.AADTrustedIssuerRepository;
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
import org.springframework.core.io.ClassPathResource;
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

public class AADB2CResourceServerAutoConfigurationTest extends AbstractAADB2COAuth2ClientTestConfiguration {

    private WebApplicationContextRunner getResourceServerContextRunner() {
        return new WebApplicationContextRunner()
            .withClassLoader(new FilteredClassLoader(OAuth2LoginAuthenticationFilter.class))
            .withConfiguration(AutoConfigurations.of(WebResourceServerApp.class,
                AADB2CResourceServerAutoConfiguration.class))
            .withPropertyValues(getB2CResourceServerProperties());
    }

    @Override
    public WebApplicationContextRunner getDefaultContextRunner() {
        return new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(WebOAuth2ClientApp.class,
                AADB2CResourceServerAutoConfiguration.class))
            .withPropertyValues(getB2CResourceServerProperties());
    }

    private String[] getB2CResourceServerProperties() {
        return new String[] {
            String.format("%s=%s", AADB2CConstants.BASE_URI, AADB2CConstants.TEST_BASE_URI),
            String.format("%s=%s", AADB2CConstants.TENANT_ID, AADB2CConstants.TEST_TENANT_ID),
            String.format("%s=%s", AADB2CConstants.CLIENT_ID, AADB2CConstants.TEST_CLIENT_ID),
            String.format("%s=%s", AADB2CConstants.APP_ID_URI, AADB2CConstants.TEST_APP_ID_URI),
            String.format("%s.%s=%s", AADB2CConstants.USER_FLOWS, AADB2CConstants.TEST_KEY_SIGN_UP_OR_IN,
                AADB2CConstants.TEST_SIGN_UP_OR_IN_NAME),
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
        getDefaultContextRunner().withPropertyValues(getAuthorizationClientPropertyValues())
                                 .run(b2CAutoConfigurationBean());
    }

    @Test
    public void testB2COnlyAutoConfigurationBean() {
        getResourceServerContextRunner().run(b2CAutoConfigurationBean());
    }

    @Test
    public void testB2COAuth2ClientResourceServerPropertiesBean() {
        getDefaultContextRunner().withPropertyValues(getAuthorizationClientPropertyValues())
                                 .run(b2CResourceServerPropertiesBean());
    }

    @Test
    public void testB2COnlyResourceServerPropertiesBean() {
        getResourceServerContextRunner().run(b2CResourceServerPropertiesBean());
    }

    @Test
    public void testB2COAuth2ClientResourceServerBean() {
        getDefaultContextRunner().withPropertyValues(getAuthorizationClientPropertyValues())
                                 .run(b2CResourceServerBean());
    }

    @Test
    public void testB2COnlyResourceServerBean() {
        getResourceServerContextRunner().run(b2CResourceServerBean());
    }

    @Test
    public void testResourceServerConditionsIsInvokedWhenAADB2CEnableFileExists() {
        try (MockedStatic<BeanUtils> beanUtils = mockStatic(BeanUtils.class, Mockito.CALLS_REAL_METHODS)) {
            AADB2CConditions.ClientRegistrationCondition clientRegistrationCondition =
                spy(AADB2CConditions.ClientRegistrationCondition.class);
            beanUtils.when(() -> BeanUtils.instantiateClass(AADB2CConditions.ClientRegistrationCondition.class))
                     .thenReturn(clientRegistrationCondition);
            getDefaultContextRunner()
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

    @Test
    public void testExistAADB2CTrustedIssuerRepositoryBean() {
        getDefaultContextRunner()
            .withPropertyValues(getB2CResourceServerProperties())
            .withUserConfiguration(AADB2CResourceServerAutoConfiguration.class)
            .run(context -> {
                final AADB2CTrustedIssuerRepository aadb2CTrustedIssuerRepository =
                    context.getBean(AADB2CTrustedIssuerRepository.class);
                assertThat(aadb2CTrustedIssuerRepository).isNotNull();
                assertThat(aadb2CTrustedIssuerRepository).isExactlyInstanceOf(AADB2CTrustedIssuerRepository.class);
            });
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExistjwtProcessorBean() {
        getDefaultContextRunner()
            .withPropertyValues(getB2CResourceServerProperties())
            .withUserConfiguration(AADB2CResourceServerAutoConfiguration.class)
            .run(context -> {
                JWTProcessor<SecurityContext> jwtProcessor = context.getBean(JWTProcessor.class);
                assertThat(jwtProcessor).isNotNull();
                assertThat(jwtProcessor).isExactlyInstanceOf(DefaultJWTProcessor.class);
            });
    }

    @Test
    public void testExistJwtDecoderBean() {
        getDefaultContextRunner()
            .withPropertyValues(getB2CResourceServerProperties())
            .withUserConfiguration(AADB2CResourceServerAutoConfiguration.class)
            .run(context -> {
                final JwtDecoder jwtDecoder = context.getBean(JwtDecoder.class);
                assertThat(jwtDecoder).isNotNull();
                assertThat(jwtDecoder).isExactlyInstanceOf(NimbusJwtDecoder.class);
            });
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExistJWTClaimsSetAwareJWSKeySelectorBean() {
        getDefaultContextRunner()
            .withPropertyValues(getB2CResourceServerProperties())
            .withUserConfiguration(AADB2CResourceServerAutoConfiguration.class)
            .run(context -> {
                final JWTClaimsSetAwareJWSKeySelector jwsKeySelector =
                    context.getBean(JWTClaimsSetAwareJWSKeySelector.class);
                assertThat(jwsKeySelector).isNotNull();
                assertThat(jwsKeySelector).isExactlyInstanceOf(AADIssuerJWSKeySelector.class);
            });
    }
}
