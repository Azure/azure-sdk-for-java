// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapi;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.junit.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class AADResourceServerClientConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
        .withPropertyValues(
            "azure.activedirectory.tenant-id=fake-tenant-id",
            "azure.activedirectory.client-id=fake-client-id",
            "azure.activedirectory.client-secret=fake-client-secret");

    @Test
    public void testWithoutAnyPropertiesSet() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADResourceServerClientConfiguration.class)
            .run(context -> {
                assertThat(context).doesNotHaveBean(AADAuthenticationProperties.class);
                assertThat(context).doesNotHaveBean(ClientRegistrationRepository.class);
                assertThat(context).doesNotHaveBean(OAuth2AuthorizedClientRepository.class);
            });
    }

    @Test
    public void testWithRequiredPropertiesSet() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADResourceServerClientConfiguration.class)
            .withPropertyValues("azure.activedirectory.client-id=fake-client-id")
            .run(context -> {
                assertThat(context).hasSingleBean(AADAuthenticationProperties.class);
                assertThat(context).hasSingleBean(ClientRegistrationRepository.class);
                assertThat(context).hasSingleBean(OAuth2AuthorizedClientRepository.class);
            });
    }

    @Test
    public void testNotExistBearerTokenAuthenticationToken() {
        this.contextRunner
            .withUserConfiguration(AADResourceServerClientConfiguration.class)
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .run(context -> assertThat(context).doesNotHaveBean(AADResourceServerOAuth2AuthorizedClientRepository.class));
    }

    @Test
    public void testNotExistOAuth2LoginAuthenticationFilter() {
        this.contextRunner
            .withUserConfiguration(AADResourceServerClientConfiguration.class)
            .withClassLoader(new FilteredClassLoader(OAuth2LoginAuthenticationFilter.class))
            .run(context -> assertThat(context).doesNotHaveBean(AADResourceServerOAuth2AuthorizedClientRepository.class));
    }

    @Test
    public void testOnlyGraphClient() {
        this.contextRunner
            .withUserConfiguration(AADResourceServerClientConfiguration.class)
            .withPropertyValues("azure.activedirectory.authorization-clients.graph.scopes="
                + "https://graph.microsoft.com/User.Read")
            .run(context -> {
                final InMemoryClientRegistrationRepository oboRepo = context.getBean(
                    InMemoryClientRegistrationRepository.class);
                final OAuth2AuthorizedClientRepository aadOboRepo = context.getBean(
                    AADResourceServerOAuth2AuthorizedClientRepository.class);

                ClientRegistration graph = oboRepo.findByRegistrationId("graph");
                Set<String> graphScopes = graph.getScopes();

                assertThat(aadOboRepo).isNotNull();
                assertThat(oboRepo).isExactlyInstanceOf(InMemoryClientRegistrationRepository.class);
                assertThat(graph).isNotNull();
                assertThat(graphScopes).containsOnly("https://graph.microsoft.com/User.Read");
            });
    }

    @Test(expected = IllegalStateException.class)
    public void testGrantTypeIsAuthorizationCodeClient() {
        this.contextRunner
            .withUserConfiguration(AADResourceServerClientConfiguration.class)
            .withPropertyValues("azure.activedirectory.authorization-clients.graph.authorization-grant-type="
                + "authorization_code")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
            });
    }

    @Test(expected = IllegalStateException.class)
    public void clientWhichGrantTypeIsOboButOnDemandExceptionTest() {
        this.contextRunner
            .withUserConfiguration(AADResourceServerClientConfiguration.class)
            .withPropertyValues("azure.activedirectory.authorization-clients.graph.authorization-grant-type="
                + "on-behalf-of")
            .withPropertyValues("azure.activedirectory.authorization-clients.graph.on-demand = true")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
            });
    }

    @Test
    public void testExistCustomAndGraphClient() {
        this.contextRunner
            .withUserConfiguration(AADResourceServerClientConfiguration.class)
            .withPropertyValues("azure.activedirectory.authorization-clients.graph.scopes="
                + "https://graph.microsoft.com/User.Read")
            .withPropertyValues("azure.activedirectory.authorization-clients.custom.scopes="
                + "api://52261059-e515-488e-84fd-a09a3f372814/File.Read")
            .run(context -> {
                final InMemoryClientRegistrationRepository oboRepo = context.getBean(
                    InMemoryClientRegistrationRepository.class);
                final OAuth2AuthorizedClientRepository aadOboRepo = context.getBean(
                    AADResourceServerOAuth2AuthorizedClientRepository.class);

                ClientRegistration graph = oboRepo.findByRegistrationId("graph");
                ClientRegistration custom = oboRepo.findByRegistrationId("custom");
                Set<String> graphScopes = graph.getScopes();
                Set<String> customScopes = custom.getScopes();

                assertThat(aadOboRepo).isNotNull();
                assertThat(oboRepo).isExactlyInstanceOf(InMemoryClientRegistrationRepository.class);
                assertThat(graph).isNotNull();
                assertThat(customScopes).isNotNull();
                assertThat(graphScopes).containsOnly("https://graph.microsoft.com/User.Read");
                assertThat(customScopes).containsOnly("api://52261059-e515-488e-84fd-a09a3f372814/File.Read");
            });
    }
}
