// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.webapi;

import com.azure.spring.autoconfigure.aad.AADAutoConfiguration;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

import java.util.List;

import static com.azure.spring.aad.WebApplicationContextRunnerUtils.resourceServerContextRunner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AADResourceServerConfigurationTest {

    @Test
    public void testNotExistBearerTokenAuthenticationToken() {
        resourceServerContextRunner()
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .run(context -> assertThrows(NoSuchBeanDefinitionException.class,
                () -> context.getBean(JWTClaimsSetAwareJWSKeySelector.class)));
    }

    @Test
    public void testCreateJwtDecoderByJwkKeySetUri() {
        resourceServerContextRunner()
            .run(context -> {
                final JwtDecoder jwtDecoder = context.getBean(JwtDecoder.class);
                assertThat(jwtDecoder).isNotNull();
                assertThat(jwtDecoder).isExactlyInstanceOf(NimbusJwtDecoder.class);
            });
    }

    @Test
    public void testNotAudienceDefaultValidator() {
        new WebApplicationContextRunner()
            .withClassLoader(new FilteredClassLoader(ClientRegistration.class))
            .withPropertyValues("azure.activedirectory.tenant-id=fake-tenant-id")
            .withUserConfiguration(AADAutoConfiguration.class)
            .run(context -> {
                AADResourceServerConfiguration bean = context
                    .getBean(AADResourceServerConfiguration.class);
                List<OAuth2TokenValidator<Jwt>> defaultValidator = bean.createDefaultValidator();
                assertThat(defaultValidator).isNotNull();
                assertThat(defaultValidator).hasSize(2);
            });
    }

    @Test
    public void testExistAudienceDefaultValidator() {
        resourceServerContextRunner()
            .run(context -> {
                AADResourceServerConfiguration bean = context
                    .getBean(AADResourceServerConfiguration.class);
                List<OAuth2TokenValidator<Jwt>> defaultValidator = bean.createDefaultValidator();
                assertThat(defaultValidator).isNotNull();
                assertThat(defaultValidator).hasSize(3);
            });
    }

    @Test
    public void testCreateWebSecurityConfigurerAdapter() {
        resourceServerContextRunner()
            .run(context -> {
                WebSecurityConfigurerAdapter webSecurityConfigurerAdapter = context
                    .getBean(AADResourceServerConfiguration.DefaultAADResourceServerWebSecurityConfigurerAdapter.class);
                assertThat(webSecurityConfigurerAdapter).isNotNull();
            });
    }
}
