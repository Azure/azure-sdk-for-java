// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapi;

import org.junit.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class AADWebApiConfigurationTest {

    private static final String AAD_PROPERTY_PREFIX = "azure.activedirectory.";

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
        .withPropertyValues(AAD_PROPERTY_PREFIX + "user-group.allowed-groups=group1",
            AAD_PROPERTY_PREFIX + "tenant-id=fake-tenant-id",
            AAD_PROPERTY_PREFIX + "client-id=fake-client-id",
            AAD_PROPERTY_PREFIX + "client-secret=fake-client-secret");

    @Test
    public void testNotExistBearerTokenAuthenticationToken() {
        this.contextRunner
            .withUserConfiguration(AADWebApiConfiguration.class)
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .run(context -> {
                assertThat(context).doesNotHaveBean("AADOAuth2OboAuthorizedClientRepository");
            });
    }

    @Test
    public void testNotExistOAuth2LoginAuthenticationFilter() {
        this.contextRunner
            .withUserConfiguration(AADWebApiConfiguration.class)
            .withClassLoader(new FilteredClassLoader(OAuth2LoginAuthenticationFilter.class))
            .run(context -> {
                assertThat(context).doesNotHaveBean("AADOAuth2OboAuthorizedClientRepository");
            });
    }

    @Test
    public void testOAuth2AuthorizedClientRepository() {
        this.contextRunner
            .withUserConfiguration(AADWebApiConfiguration.class)
            .run(context -> {
                final OAuth2AuthorizedClientRepository aadOboRepo = context.getBean(
                    AADOAuth2OboAuthorizedClientRepository.class);
                assertThat(aadOboRepo).isNotNull();
                assertThat(aadOboRepo).isExactlyInstanceOf(AADOAuth2OboAuthorizedClientRepository.class);
            });
    }

    @Test
    public void testDefaultClientRegistrationRepository() {
        this.contextRunner
            .withUserConfiguration(AADWebApiConfiguration.class)
            .run(context -> {
                AADWebApiClientRegistrationRepository azureRepo = context.getBean(AADWebApiClientRegistrationRepository
                    .class);
                ClientRegistration graph = azureRepo.findByRegistrationId("graph");
                ClientRegistration azure = azureRepo.findByRegistrationId("azure");

                assertThat(azureRepo).isNotNull();
                assertThat(azureRepo).isExactlyInstanceOf(AADWebApiClientRegistrationRepository.class);

                assertThat(graph).isNull();

                assertThat(azure).isNotNull();
                assertThat(azure.getScopes()).hasSize(3);

            });
    }

    @Test
    public void testExistGraphClient() {
        this.contextRunner
            .withUserConfiguration(AADWebApiConfiguration.class)
            .withPropertyValues(AAD_PROPERTY_PREFIX + "authorization.graph.scopes=User.read")
            .run(context -> {
                AADWebApiClientRegistrationRepository azureRepo = context.getBean(AADWebApiClientRegistrationRepository
                    .class);
                ClientRegistration azure = azureRepo.findByRegistrationId("azure");
                ClientRegistration graph = azureRepo.findByRegistrationId("graph");
                Set<String> azureScopes = azure.getScopes();
                Set<String> graphScopes = graph.getScopes();

                assertThat(azureRepo).isExactlyInstanceOf(AADWebApiClientRegistrationRepository.class);
                assertThat(azure).isNotNull();
                assertThat(azureScopes).hasSize(5);

                assertThat(graph).isNotNull();
                assertThat(graphScopes).hasSize(1);

            });
    }
    @Test
    public void testExistAzureClient() {
        this.contextRunner
            .withUserConfiguration(AADWebApiConfiguration.class)
            .withPropertyValues(AAD_PROPERTY_PREFIX + "authorization.azure.scopes=User.read")
            .run(context -> {
                AADWebApiClientRegistrationRepository azureRepo = context.getBean(AADWebApiClientRegistrationRepository
                    .class);

                ClientRegistration azure = azureRepo.findByRegistrationId("azure");
                ClientRegistration graph = azureRepo.findByRegistrationId("graph");
                Set<String> azureScopes = azure.getScopes();

                assertThat(azureRepo).isExactlyInstanceOf(AADWebApiClientRegistrationRepository.class);
                assertThat(azure).isNotNull();
                assertThat(azureScopes).hasSize(5);

                assertThat(graph).isNull();

            });
    }

}
