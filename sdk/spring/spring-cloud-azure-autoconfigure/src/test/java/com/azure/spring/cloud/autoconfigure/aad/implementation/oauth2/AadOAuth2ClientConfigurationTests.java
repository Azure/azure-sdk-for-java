// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.oauth2;

import com.azure.spring.cloud.autoconfigure.aad.AadClientRegistrationRepository;
import com.azure.spring.cloud.autoconfigure.aad.configuration.AadOAuth2ClientConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

import java.util.Set;

import static com.azure.spring.cloud.autoconfigure.aad.implementation.WebApplicationContextRunnerUtils.oauthClientAndResourceServerRunner;
import static com.azure.spring.cloud.autoconfigure.aad.implementation.WebApplicationContextRunnerUtils.resourceServerContextRunner;
import static com.azure.spring.cloud.autoconfigure.aad.implementation.WebApplicationContextRunnerUtils.resourceServerWithOboContextRunner;
import static com.azure.spring.cloud.autoconfigure.aad.implementation.WebApplicationContextRunnerUtils.webApplicationContextRunner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AadOAuth2ClientConfigurationTests {

    @Test
    void testWithoutAnyPropertiesSet() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AadOAuth2ClientConfiguration.class)
            .run(context -> {
                assertThat(context).doesNotHaveBean(AadAuthenticationProperties.class);
                assertThat(context).doesNotHaveBean(ClientRegistrationRepository.class);
                assertThat(context).doesNotHaveBean(OAuth2AuthorizedClientRepository.class);
            });
    }

    @Test
    void testWithRequiredPropertiesSet() {
        oauthClientAndResourceServerRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.credential.client-id=fake-client-id"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AadAuthenticationProperties.class);
                assertThat(context).hasSingleBean(ClientRegistrationRepository.class);
                assertThat(context).hasSingleBean(OAuth2AuthorizedClientRepository.class);
            });
    }

    @Test
    void testWebApplication() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.enabled=true"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AadAuthenticationProperties.class);
                assertThat(context).hasSingleBean(ClientRegistrationRepository.class);
                assertThat(context).hasSingleBean(OAuth2AuthorizedClientRepository.class);
            });
    }

    @Test
    void testResourceServer() {
        resourceServerContextRunner()
            .run(context -> assertThat(context).doesNotHaveBean(OAuth2AuthorizedClientRepository.class));
    }

    @Test
    void testResourceServerWithOboOnlyGraphClient() {
        resourceServerWithOboContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.authorization-clients.graph.scopes=https://graph.microsoft.com/User.Read")
            .run(context -> {
                final AadClientRegistrationRepository oboRepo = context.getBean(
                    AadClientRegistrationRepository.class);
                final OAuth2AuthorizedClientRepository aadOboRepo = context.getBean(
                    OAuth2AuthorizedClientRepository.class);

                ClientRegistration graph = oboRepo.findByRegistrationId("graph");
                Set<String> graphScopes = graph.getScopes();

                assertThat(aadOboRepo).isNotNull();
                assertThat(oboRepo).isExactlyInstanceOf(AadClientRegistrationRepository.class);
                assertThat(graph).isNotNull();
                assertThat(graphScopes).containsOnly("https://graph.microsoft.com/User.Read");
            });
    }

    @Test
    void testResourceServerWithOboInvalidGrantType1() {
        resourceServerWithOboContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.authorization-clients.graph.authorization-grant-type=authorization_code",
                "spring.cloud.azure.active-directory.enabled=true"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AadAuthenticationProperties.class))
            );
    }

    @Test
    void testResourceServerWithOboInvalidGrantType2() {
        resourceServerWithOboContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.authorization-clients.graph.authorization-grant-type=on_behalf_of",
                "spring.cloud.azure.active-directory.authorization-clients.graph.authorization-grant-type = authorization_code"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AadAuthenticationProperties.class))
            );
    }

    @Test
    void testResourceServerWithOboExistCustomAndGraphClient() {
        resourceServerWithOboContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.authorization-clients.graph.scopes=https://graph.microsoft.com/User.Read",
                "spring.cloud.azure.active-directory.authorization-clients.custom.scopes=api://52261059-e515-488e-84fd-a09a3f372814/File.Read"
            )
            .run(context -> {
                final AadClientRegistrationRepository oboRepo = context.getBean(
                    AadClientRegistrationRepository.class);
                final OAuth2AuthorizedClientRepository aadOboRepo = context.getBean(
                    OAuth2AuthorizedClientRepository.class);

                ClientRegistration graph = oboRepo.findByRegistrationId("graph");
                ClientRegistration custom = oboRepo.findByRegistrationId("custom");
                Set<String> graphScopes = graph.getScopes();
                Set<String> customScopes = custom.getScopes();

                assertThat(aadOboRepo).isNotNull();
                assertThat(oboRepo).isExactlyInstanceOf(AadClientRegistrationRepository.class);
                assertThat(graph).isNotNull();
                assertThat(customScopes).isNotNull();
                assertThat(graphScopes).containsOnly("https://graph.microsoft.com/User.Read");
                assertThat(customScopes).containsOnly("api://52261059-e515-488e-84fd-a09a3f372814/File.Read");
            });
    }
}
