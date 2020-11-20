// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.aad;

import static org.assertj.core.api.Assertions.assertThat;

import com.azure.spring.aad.resource.AzureActiveDirectoryResourceConfiguration;
import com.azure.spring.aad.resource.AzureActiveDirectoryResourceConfiguration.DefaultAzureOAuth2ResourceServerWebSecurityConfigurerAdapter;
import java.util.List;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

public class AzureActiveDirectoryResourceConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureActiveDirectoryResourceConfiguration.class));

    @Test
    public void testTenantIdIsEmpty() {
        this.contextRunner
            .run(context -> {
                AzureActiveDirectoryResourceConfiguration bean = context
                    .getBean(AzureActiveDirectoryResourceConfiguration.class);
                assertThat(bean.azureActiveDirectoryProperties).isNotNull();
                assertThat(bean.azureActiveDirectoryProperties.getTenantId()).isEqualTo("common");
            });
    }

    @Test
    public void testTenantIdNotEmpty() {
        this.contextRunner
            .withPropertyValues("azure.active.directory.tenant-id=fake-tenant-id")
            .run(context -> {
                AzureActiveDirectoryResourceConfiguration bean = context
                    .getBean(AzureActiveDirectoryResourceConfiguration.class);
                assertThat(bean.azureActiveDirectoryProperties).isNotNull();
                assertThat(bean.azureActiveDirectoryProperties.getTenantId()).isEqualTo("fake-tenant-id");
            });
    }

    @Test
    public void testCreateJwtDecoderByJwkKeySetUri() {
        this.contextRunner
            .run(context -> {
                final JwtDecoder jwtDecoder = context.getBean(JwtDecoder.class);
                assertThat(jwtDecoder).isNotNull();
                assertThat(jwtDecoder).isExactlyInstanceOf(NimbusJwtDecoder.class);
            });
    }

    @Test
    public void testNotAudienceDefaultValidator() {
        this.contextRunner
            .run(context -> {
                AzureActiveDirectoryResourceConfiguration bean = context
                    .getBean(AzureActiveDirectoryResourceConfiguration.class);
                List<OAuth2TokenValidator<Jwt>> defaultValidator = bean.createDefaultValidator();
                assertThat(defaultValidator).isNotNull();
                assertThat(defaultValidator).hasSize(2);
            });
    }

    @Test
    public void testExistAudienceDefaultValidator() {
        this.contextRunner
            .withPropertyValues("azure.active.directory.app-id-uri=fake-app-id-uri")
            .run(context -> {
                AzureActiveDirectoryResourceConfiguration bean = context
                    .getBean(AzureActiveDirectoryResourceConfiguration.class);
                List<OAuth2TokenValidator<Jwt>> defaultValidator = bean.createDefaultValidator();
                assertThat(defaultValidator).isNotNull();
                assertThat(defaultValidator).hasSize(3);
            });
    }

    @Test
    public void testCreateWebSecurityConfigurerAdapter() {
        this.contextRunner
            .run(context -> {
                WebSecurityConfigurerAdapter webSecurityConfigurerAdapter = context
                    .getBean(DefaultAzureOAuth2ResourceServerWebSecurityConfigurerAdapter.class);
                assertThat(webSecurityConfigurerAdapter).isNotNull();
            });
    }

}
