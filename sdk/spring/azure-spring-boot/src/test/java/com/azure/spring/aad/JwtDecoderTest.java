// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import org.junit.Test;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtDecoderTest {

    @Test
    public void jwtDecoderTest() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .run(context -> {
                JwtDecoder jwtDecoder = context.getBean(JwtDecoder.class);
                assertThat(jwtDecoder).isNotNull();
                assertThat(jwtDecoder).isExactlyInstanceOf(NimbusJwtDecoder.class);
            });
    }

    @Test
    public void testAudienceDefaultValidator1() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                // azure.activedirectory.client-id and azure.activedirectory.app-id-uri are not configured
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-secret=fake-client-secret")
            .run(context -> {
                AADConfiguration bean = context.getBean(AADConfiguration.class);
                List<OAuth2TokenValidator<Jwt>> defaultValidator = bean.createDefaultValidator();
                assertThat(defaultValidator).isNotNull();
                assertThat(defaultValidator).hasSize(2);
            });
    }

    @Test
    public void testAudienceDefaultValidator2() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret")
            .run(context -> {
                AADConfiguration bean = context.getBean(AADConfiguration.class);
                List<OAuth2TokenValidator<Jwt>> defaultValidator = bean.createDefaultValidator();
                assertThat(defaultValidator).isNotNull();
                assertThat(defaultValidator).hasSize(3);
            });
    }

    @Test
    public void testAudienceDefaultValidator3() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.app-id-uri=fake-app-id-uri",
                "azure.activedirectory.client-secret=fake-client-secret")
            .run(context -> {
                AADConfiguration bean = context.getBean(AADConfiguration.class);
                List<OAuth2TokenValidator<Jwt>> defaultValidator = bean.createDefaultValidator();
                assertThat(defaultValidator).isNotNull();
                assertThat(defaultValidator).hasSize(3);
            });
    }

    @Test
    public void testAudienceDefaultValidator4() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.app-id-uri=fake-app-id-uri",
                "azure.activedirectory.client-secret=fake-client-secret")
            .run(context -> {
                AADConfiguration bean = context.getBean(AADConfiguration.class);
                List<OAuth2TokenValidator<Jwt>> defaultValidator = bean.createDefaultValidator();
                assertThat(defaultValidator).isNotNull();
                assertThat(defaultValidator).hasSize(3);
            });
    }
}
