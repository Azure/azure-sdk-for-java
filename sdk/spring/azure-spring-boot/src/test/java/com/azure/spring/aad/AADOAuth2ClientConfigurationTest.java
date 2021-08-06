// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import com.azure.spring.autoconfigure.aad.AADAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AADOAuth2ClientConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
        .withPropertyValues(
            "azure.activedirectory.tenant-id=fake-tenant-id",
            "azure.activedirectory.client-id=fake-client-id",
            "azure.activedirectory.client-secret=fake-client-secret");

    @Test
    public void testWithoutAnyPropertiesSet() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADOAuth2ClientConfiguration.class)
            .run(context -> {
                assertThat(context).doesNotHaveBean(AADAuthenticationProperties.class);
                assertThat(context).doesNotHaveBean(ClientRegistrationRepository.class);
                assertThat(context).doesNotHaveBean(AADOAuth2AuthorizedClientRepository.class);
            });
    }

    @Test
    public void testWithRequiredPropertiesSet() {
        new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AADAutoConfiguration.class))
            .withPropertyValues("azure.activedirectory.client-id=fake-client-id")
            .run(context -> {
                assertThat(context).hasSingleBean(AADAuthenticationProperties.class);
                assertThat(context).hasSingleBean(ClientRegistrationRepository.class);
                assertThat(context).hasSingleBean(AADOAuth2AuthorizedClientRepository.class);
            });
    }

    @Test
    public void testWebApplication() {
        this.contextRunner
            .withConfiguration(AutoConfigurations.of(AADAutoConfiguration.class,
                AADOAuth2ClientConfiguration.class))
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .run(context -> {
                assertThat(context).hasSingleBean(AADAuthenticationProperties.class);
                assertThat(context).hasSingleBean(ClientRegistrationRepository.class);
                assertThat(context).hasSingleBean(AADOAuth2AuthorizedClientRepository.class);
            });
    }

    @Test
    public void testResourceServer() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(ClientRegistration.class))
            .withConfiguration(AutoConfigurations.of(AADOAuth2ClientConfiguration.class))
            .run(context -> assertThat(context).doesNotHaveBean(AADOAuth2AuthorizedClientRepository.class));
    }

    @Test
    public void testOnlyGraphClient() {
        this.contextRunner
            .withConfiguration(AutoConfigurations.of(AADAutoConfiguration.class))
            .withPropertyValues("azure.activedirectory.authorization-clients.graph.scopes="
                + "https://graph.microsoft.com/User.Read")
            .run(context -> {
                final AADClientRegistrationRepository oboRepo = context.getBean(
                    AADClientRegistrationRepository.class);
                final AADOAuth2AuthorizedClientRepository aadOboRepo = context.getBean(
                    AADOAuth2AuthorizedClientRepository.class);

                ClientRegistration graph = oboRepo.findByRegistrationId("graph");
                Set<String> graphScopes = graph.getScopes();

                assertThat(aadOboRepo).isNotNull();
                assertThat(oboRepo).isExactlyInstanceOf(AADClientRegistrationRepository.class);
                assertThat(graph).isNotNull();
                assertThat(graphScopes).containsOnly("https://graph.microsoft.com/User.Read");
            });
    }

    @Test
    public void testGrantTypeIsAuthorizationCodeClient() {
        this.contextRunner
            .withUserConfiguration(AADOAuth2ClientConfiguration.class)
            .withPropertyValues("azure.activedirectory.authorization-clients.graph.authorization-grant-type="
                + "authorization_code")
            .run(context -> {
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class));
            });
    }

    @Test
    public void clientWhichGrantTypeIsOboButOnDemandExceptionTest() {
        this.contextRunner
            .withUserConfiguration(AADAutoConfiguration.class)
            .withPropertyValues("azure.activedirectory.authorization-clients.graph.authorization-grant-type="
                + "on_behalf_of")
            .withPropertyValues("azure.activedirectory.authorization-clients.graph.on-demand = true")
            .run(context -> {
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class));
            });
    }

    @Test
    public void testExistCustomAndGraphClient() {
        this.contextRunner
            .withConfiguration(AutoConfigurations.of(AADAutoConfiguration.class))
            .withPropertyValues("azure.activedirectory.authorization-clients.graph.scopes="
                + "https://graph.microsoft.com/User.Read")
            .withPropertyValues("azure.activedirectory.authorization-clients.custom.scopes="
                + "api://52261059-e515-488e-84fd-a09a3f372814/File.Read")
            .run(context -> {
                final AADClientRegistrationRepository oboRepo = context.getBean(
                    AADClientRegistrationRepository.class);
                final AADOAuth2AuthorizedClientRepository aadOboRepo = context.getBean(
                    AADOAuth2AuthorizedClientRepository.class);

                ClientRegistration graph = oboRepo.findByRegistrationId("graph");
                ClientRegistration custom = oboRepo.findByRegistrationId("custom");
                Set<String> graphScopes = graph.getScopes();
                Set<String> customScopes = custom.getScopes();

                assertThat(aadOboRepo).isNotNull();
                assertThat(oboRepo).isExactlyInstanceOf(AADClientRegistrationRepository.class);
                assertThat(graph).isNotNull();
                assertThat(customScopes).isNotNull();
                assertThat(graphScopes).containsOnly("https://graph.microsoft.com/User.Read");
                assertThat(customScopes).containsOnly("api://52261059-e515-488e-84fd-a09a3f372814/File.Read");
            });
    }
}
