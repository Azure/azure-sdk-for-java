// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad.implementation.webapi;

import com.azure.spring.cloud.autoconfigure.aad.configuration.AadResourceServerConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

import java.util.List;

import static com.azure.spring.cloud.autoconfigure.aad.implementation.WebApplicationContextRunnerUtils.resourceServerContextRunner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AadResourceServerConfigurationTests {

    @Test
    void testNotExistBearerTokenAuthenticationToken() {
        resourceServerContextRunner()
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .run(context -> assertThrows(NoSuchBeanDefinitionException.class,
                () -> context.getBean(JWTClaimsSetAwareJWSKeySelector.class)));
    }

    @Test
    void testCreateJwtDecoderByJwkKeySetUri() {
        resourceServerContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true")
            .run(context -> {
                final JwtDecoder jwtDecoder = context.getBean(JwtDecoder.class);
                assertThat(jwtDecoder).isNotNull();
                assertThat(jwtDecoder).isExactlyInstanceOf(NimbusJwtDecoder.class);
            });
    }

    @Test
    void testNotAudienceDefaultValidator() {
        resourceServerContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true")
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                AadResourceServerConfiguration bean = context
                    .getBean(AadResourceServerConfiguration.class);
                List<OAuth2TokenValidator<Jwt>> defaultValidator = bean.createDefaultValidator(properties);
                assertThat(defaultValidator).isNotNull();
                assertThat(defaultValidator).hasSize(3);
            });
    }

    @Test
    void testExistAudienceDefaultValidator() {
        resourceServerContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true")
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                AadResourceServerConfiguration bean = context
                    .getBean(AadResourceServerConfiguration.class);
                List<OAuth2TokenValidator<Jwt>> defaultValidator = bean.createDefaultValidator(properties);
                assertThat(defaultValidator).isNotNull();
                assertThat(defaultValidator).hasSize(3);
            });
    }

    @Test
    void testCreateWebSecurityConfigurerAdapter() {
        resourceServerContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true")
            .run(context -> {
                WebSecurityConfigurerAdapter webSecurityConfigurerAdapter = context
                    .getBean(AadResourceServerConfiguration.DefaultAadResourceServerWebSecurityConfigurerAdapter.class);
                assertThat(webSecurityConfigurerAdapter).isNotNull();
            });
    }
}
