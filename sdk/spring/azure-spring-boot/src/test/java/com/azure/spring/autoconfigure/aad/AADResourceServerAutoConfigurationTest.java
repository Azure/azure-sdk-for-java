// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.aad;

import static org.assertj.core.api.Assertions.assertThat;

import com.azure.spring.autoconfigure.aad.AADResourceServerAutoConfiguration.
    DefaultAzureOAuth2ResourceServerWebSecurityConfigurerAdapter;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

public class AADResourceServerAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AADResourceServerAutoConfiguration.class))
        .withPropertyValues("azure.activedirectory.client-id=fake-client-id",
            "azure.activedirectory.user-group.allowed-groups=fake-group",
            TestConstants.ALLOW_TELEMETRY_PROPERTY + "=false");

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
    public void testCreateWebSecurityConfigurerAdapter() {
        this.contextRunner
            .run(context -> {
                WebSecurityConfigurerAdapter webSecurityConfigurerAdapter = context
                    .getBean(DefaultAzureOAuth2ResourceServerWebSecurityConfigurerAdapter.class);
                assertThat(webSecurityConfigurerAdapter).isNotNull();
            });
    }


}
