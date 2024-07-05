// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.configuration;

import com.azure.spring.cloud.autoconfigure.implementation.aad.jwk.TestJwks;
import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.AadClientRegistrationRepository;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.AadJwtBearerGrantRequestEntityConverter;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.OAuth2ClientAuthenticationJwkResolver;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.client.JwtBearerOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.endpoint.DefaultJwtBearerTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.JwtBearerGrantRequest;
import org.springframework.security.oauth2.client.endpoint.JwtBearerGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.Set;

import static com.azure.spring.cloud.autoconfigure.implementation.aad.RestTemplateTestUtil.assertRestTemplateWellConfigured;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.WebApplicationContextRunnerUtils.oauthClientAndResourceServerRunner;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.WebApplicationContextRunnerUtils.resourceServerContextRunner;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.WebApplicationContextRunnerUtils.resourceServerWithOboContextRunner;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.WebApplicationContextRunnerUtils.webApplicationContextRunner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class AadOAuth2ClientConfigurationTests {

    @Test
    void testWithoutAnyPropertiesSet() {
        new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                HttpMessageConvertersAutoConfiguration.class,
                RestTemplateAutoConfiguration.class))
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

    @Test
    void defaultConverterInJwtBearerOAuth2AuthorizedClientProviderWhenNotUsingPrivateKeyJwtMethod() {
        resourceServerWithOboContextRunner()
            .withUserConfiguration(AadOAuth2ClientConfiguration.class)
            .withPropertyValues(
                "spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.authorization-clients.graph.scopes=https://graph.microsoft.com/User.Read",
                "spring.cloud.azure.active-directory.authorization-clients.graph.authorization-grant-type=on_behalf_of",
                "spring.cloud.azure.active-directory.authorization-clients.graph.scopes=api://52261059-e515-488e-84fd-a09a3f372814/File.Read"
            )
            .run(context -> {
                assertThat(context).doesNotHaveBean(OAuth2ClientAuthenticationJwkResolver.class);
                final JwtBearerOAuth2AuthorizedClientProvider jwtBearerProvider = context.getBean(
                        JwtBearerOAuth2AuthorizedClientProvider.class);
                final ClientRegistrationRepository clientRepository = context.getBean(
                        ClientRegistrationRepository.class);
                MultiValueMap<String, String> parameters = convertParameters(jwtBearerProvider, clientRepository);
                assertThat(parameters).containsEntry("requested_token_use", Arrays.asList("on_behalf_of"));
            });
    }

    @Test
    @SuppressWarnings("deprecation")
    void customConverterInJwtBearerOAuth2AuthorizedClientProviderWhenUsingPrivateKeyJwtMethod() {
        RSAKey rsaJwk = spy(TestJwks.DEFAULT_RSA_JWK);
        OAuth2ClientAuthenticationJwkResolver jwkResolver = spy(new TestOAuth2ClientAuthenticationJwkResolver(rsaJwk));
        given(jwkResolver.resolve(any())).willReturn(rsaJwk);
        given(rsaJwk.getX509CertThumbprint()).willReturn(new Base64URL("dGVzdA"));

        resourceServerWithOboContextRunner()
            .withBean(OAuth2ClientAuthenticationJwkResolver.class, () -> jwkResolver)
            .withUserConfiguration(AadOAuth2ClientConfiguration.class)
            .withPropertyValues(
                "spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.credential.client-certificate-path=/test/test.pfx",
                "spring.cloud.azure.active-directory.credential.client-certificate-password=test",
                "spring.cloud.azure.active-directory.authorization-clients.graph.scopes=https://graph.microsoft.com/User.Read",
                "spring.cloud.azure.active-directory.authorization-clients.graph.authorization-grant-type=urn:ietf:params:oauth:grant-type:jwt-bearer",
                "spring.cloud.azure.active-directory.authorization-clients.graph.scopes=api://52261059-e515-488e-84fd-a09a3f372814/File.Read",
                "spring.cloud.azure.active-directory.authorization-clients.graph.client-authentication-method=private_key_jwt"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(OAuth2ClientAuthenticationJwkResolver.class);
                final JwtBearerOAuth2AuthorizedClientProvider jwtBearerProvider = context.getBean(
                        JwtBearerOAuth2AuthorizedClientProvider.class);
                final ClientRegistrationRepository clientRepository = context.getBean(
                        ClientRegistrationRepository.class);

                MultiValueMap<String, String> parameters = convertParameters(jwtBearerProvider, clientRepository);
                assertThat(parameters).containsEntry("requested_token_use", Arrays.asList("on_behalf_of"));
                assertThat(parameters).containsKey(OAuth2ParameterNames.CLIENT_ASSERTION_TYPE);
                verify(jwkResolver).resolve(clientRepository.findByRegistrationId("graph"));
            });
    }

    @Test
    void restTemplateWellConfiguredWhenNotUsingPrivateKeyJwtMethod() {
        webApplicationContextRunner()
            .withUserConfiguration(AadOAuth2ClientConfiguration.class, RestTemplateProxyCustomizerTestConfiguration.class)
            .run(context -> {
                assertThat(context).doesNotHaveBean(OAuth2ClientAuthenticationJwkResolver.class);
                assertRestTemplateWellConfigured(context);
            });
    }

    @Test
    void restTemplateWellConfiguredWhenUsingPrivateKeyJwtMethod() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.credential.client-certificate-path=/test/test.pfx",
                "spring.cloud.azure.active-directory.credential.client-certificate-password=test",
                "spring.cloud.azure.active-directory.authorization-clients.graph.client-authentication-method=private_key_jwt",
                "spring.cloud.azure.active-directory.authorization-clients.graph.scopes=https://graph.microsoft.com/User.Read",
                "spring.cloud.azure.active-directory.authorization-clients.graph.scopes=api://52261059-e515-488e-84fd-a09a3f372814/File.Read"
            )
            .withUserConfiguration(AadOAuth2ClientConfiguration.class, RestTemplateProxyCustomizerTestConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(OAuth2ClientAuthenticationJwkResolver.class);
                assertRestTemplateWellConfigured(context);
            });
    }

    @SuppressWarnings("unchecked")
    private MultiValueMap<String, String> convertParameters(JwtBearerOAuth2AuthorizedClientProvider jwtBearerProvider,
                                                            ClientRegistrationRepository clientRepository) {
        OAuth2AccessTokenResponseClient<JwtBearerGrantRequest> client =
                (OAuth2AccessTokenResponseClient<JwtBearerGrantRequest>) ReflectionTestUtils.getField(jwtBearerProvider, "accessTokenResponseClient");
        assertThat(client.getClass().getSimpleName()).isEqualTo(DefaultJwtBearerTokenResponseClient.class.getSimpleName());

        JwtBearerGrantRequestEntityConverter requestEntityConverter =
                (JwtBearerGrantRequestEntityConverter) ReflectionTestUtils.getField(client, "requestEntityConverter");
        assertThat(requestEntityConverter.getClass().getSimpleName()).isEqualTo(AadJwtBearerGrantRequestEntityConverter.class.getSimpleName());

        Converter<JwtBearerGrantRequest, MultiValueMap<String, String>> parametersConverter =
                (Converter<JwtBearerGrantRequest, MultiValueMap<String, String>>) ReflectionTestUtils.getField(requestEntityConverter, "parametersConverter");
        JwtBearerGrantRequest request = new JwtBearerGrantRequest(clientRepository.findByRegistrationId("graph"), mock(Jwt.class));
        return parametersConverter.convert(request);
    }

    static class TestOAuth2ClientAuthenticationJwkResolver implements OAuth2ClientAuthenticationJwkResolver {

        private final JWK jwk;

        TestOAuth2ClientAuthenticationJwkResolver(JWK jwk) {
            this.jwk = jwk;
        }

        @Override
        public JWK resolve(ClientRegistration clientRegistration) {
            return this.jwk;
        }
    }
}
