// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapi;

import com.azure.spring.aad.webapp.AzureClientRegistrationRepository;
import org.junit.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureActiveDirectoryResourceServerClientConfigurationTest {

    private static final String AAD_PROPERTY_PREFIX = "azure.activedirectory.";

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
        .withPropertyValues(AAD_PROPERTY_PREFIX + "user-group.allowed-groups=group1",
            AAD_PROPERTY_PREFIX + "tenant-id=fake-tenant-id",
            AAD_PROPERTY_PREFIX + "client-id=fake-client-id",
            AAD_PROPERTY_PREFIX + "client-secret=fake-client-secret",
            AAD_PROPERTY_PREFIX + "resource-server.obo.enabled=true");

    @Test
    public void testNotExistBearerTokenAuthenticationToken() {
        this.contextRunner
            .withUserConfiguration(AzureActiveDirectoryResourceServerClientConfiguration.class)
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .run(context -> {
                assertThat(context).doesNotHaveBean("AADOAuth2OboAuthorizedClientRepository");
            });
    }

    @Test
    public void testNotExistOAuth2LoginAuthenticationFilter() {
        this.contextRunner
            .withUserConfiguration(AzureActiveDirectoryResourceServerClientConfiguration.class)
            .withClassLoader(new FilteredClassLoader(OAuth2LoginAuthenticationFilter.class))
            .run(context -> {
                assertThat(context).doesNotHaveBean("AADOAuth2OboAuthorizedClientRepository");
            });
    }

    @Test
    public void testOAuth2AuthorizedClientRepository() {
        this.contextRunner
            .withUserConfiguration(AzureActiveDirectoryResourceServerClientConfiguration.class)
            .run(context -> {
                final OAuth2AuthorizedClientRepository aadOboRepo = context.getBean(
                    AADOAuth2OboAuthorizedClientRepository.class);
                assertThat(aadOboRepo).isNotNull();
                assertThat(aadOboRepo).isExactlyInstanceOf(AADOAuth2OboAuthorizedClientRepository.class);
            });
    }

    @Test
    public void testClientRegistrationRepository() {
        this.contextRunner
            .withUserConfiguration(AzureActiveDirectoryResourceServerClientConfiguration.class)
            .run(context -> {
                final AzureClientRegistrationRepository azureRepo = context.getBean(AzureClientRegistrationRepository
                    .class);
                assertThat(azureRepo).isNotNull();
                assertThat(azureRepo).isExactlyInstanceOf(AzureClientRegistrationRepository.class);
            });
    }

}
