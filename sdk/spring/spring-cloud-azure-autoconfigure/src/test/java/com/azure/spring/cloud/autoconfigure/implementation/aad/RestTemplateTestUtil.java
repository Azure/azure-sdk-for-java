// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad;

import com.azure.spring.cloud.autoconfigure.implementation.aad.security.AadAzureDelegatedOAuth2AuthorizedClientProvider;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.oauth2.client.AuthorizationCodeOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.DelegatingOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationProvider;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

import static com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.RestTemplateProxyCustomizerTestConfiguration.FACTORY;
import static com.azure.spring.cloud.core.implementation.util.ReflectionUtils.getField;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class RestTemplateTestUtil {

    private RestTemplateTestUtil() {
    }

    public static void assertRestTemplateWellConfigured(ApplicationContext context) {
        assertRestTemplateWellConfiguredForOAuth2AuthorizationCodeAuthenticationProvider(context);
        assertRestTemplateWellConfiguredForAllOAuth2AuthorizedClientProviders(context);
        assertRestTemplateWellConfiguredForJwtDecoderFactory(context);
    }

    private static void assertRestTemplateWellConfiguredForOAuth2AuthorizationCodeAuthenticationProvider(ApplicationContext context) {
        OAuth2AuthorizationCodeAuthenticationProvider provider = getOAuth2AuthorizationCodeAuthenticationProvider(context);
        OAuth2AccessTokenResponseClient<?> client = (OAuth2AccessTokenResponseClient<?>) getField(provider.getClass(), "accessTokenResponseClient", provider);
        assertRestTemplateWellConfiguredInOAuth2AccessTokenResponseClient(client);
    }

    private static OAuth2AuthorizationCodeAuthenticationProvider getOAuth2AuthorizationCodeAuthenticationProvider(ApplicationContext context) {
        FilterChainProxy proxy = context.getBean(FilterChainProxy.class);
        OAuth2LoginAuthenticationFilter filter = proxy.getFilterChains()
                .get(0)
                .getFilters()
                .stream()
                .filter(f -> f instanceof OAuth2LoginAuthenticationFilter)
                .map(f -> (OAuth2LoginAuthenticationFilter) f)
                .findAny()
                .orElse(null);
        ProviderManager manager = (ProviderManager) getField(OAuth2LoginAuthenticationFilter.class, "authenticationManager", filter);
        OAuth2LoginAuthenticationProvider provider = manager.getProviders()
                .stream()
                .filter(p -> p instanceof OAuth2LoginAuthenticationProvider)
                .map(p -> (OAuth2LoginAuthenticationProvider) p)
                .findAny()
                .orElse(null);
        return (OAuth2AuthorizationCodeAuthenticationProvider) getField(OAuth2LoginAuthenticationProvider.class, "authorizationCodeAuthenticationProvider", provider);
    }

    private static void assertRestTemplateWellConfiguredForAllOAuth2AuthorizedClientProviders(ApplicationContext context) {
        List<OAuth2AuthorizedClientProvider> providers = getAllOAuth2AuthorizedClientProviderThatShouldConfiguredRestTemplate(context);
        // 3 providers: refreshToken, clientCredential.
        assertTrue(providers.size() >= 2);
        providers.forEach(provider -> {
            OAuth2AccessTokenResponseClient<?> client = (OAuth2AccessTokenResponseClient<?>) getField(provider.getClass(), "accessTokenResponseClient", provider);
            assertRestTemplateWellConfiguredInOAuth2AccessTokenResponseClient(client);
        });
    }

    @SuppressWarnings("unchecked")
    private static List<OAuth2AuthorizedClientProvider> getAllOAuth2AuthorizedClientProviderThatShouldConfiguredRestTemplate(ApplicationContext context) {
        final DefaultOAuth2AuthorizedClientManager manager = context.getBean(DefaultOAuth2AuthorizedClientManager.class);
        DelegatingOAuth2AuthorizedClientProvider delegatingProvider =
                (DelegatingOAuth2AuthorizedClientProvider) getField(DefaultOAuth2AuthorizedClientManager.class, "authorizedClientProvider", manager);
        List<OAuth2AuthorizedClientProvider> providers =
                (List<OAuth2AuthorizedClientProvider>) getField(DelegatingOAuth2AuthorizedClientProvider.class, "authorizedClientProviders", delegatingProvider);
        return providers.stream()
                .filter(provider -> !(provider instanceof AuthorizationCodeOAuth2AuthorizedClientProvider))
                .filter(provider -> !(provider instanceof AadAzureDelegatedOAuth2AuthorizedClientProvider))
                .collect(Collectors.toList());
    }

    private static void assertRestTemplateWellConfiguredInOAuth2AccessTokenResponseClient(OAuth2AccessTokenResponseClient<?> client) {
        RestTemplate restTemplate = (RestTemplate) getField(client.getClass(), "restOperations", client);
        assertEquals(FACTORY, restTemplate.getRequestFactory());
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    private static void assertRestTemplateWellConfiguredForJwtDecoderFactory(ApplicationContext context) {
        JwtDecoderFactory<ClientRegistration> factory = (JwtDecoderFactory<ClientRegistration>) context.getBean(JwtDecoderFactory.class);
        JwtDecoder jwtDecoder = factory.createDecoder(clientRegistration());
        assertTrue(jwtDecoder instanceof NimbusJwtDecoder);
        DefaultJWTProcessor<?> processor = (DefaultJWTProcessor<?>) getField(NimbusJwtDecoder.class, "jwtProcessor", jwtDecoder);
        JWSVerificationKeySelector<?> selector = (JWSVerificationKeySelector<?>) processor.getJWSKeySelector();
        RemoteJWKSet<?> source = (RemoteJWKSet<?>) selector.getJWKSource();
        ResourceRetriever retriever = source.getResourceRetriever();
        RestTemplate restTemplate = (RestTemplate) getField(retriever.getClass(), "restOperations", retriever);
        assertEquals(FACTORY, restTemplate.getRequestFactory());
    }

    private static ClientRegistration clientRegistration() {
        return ClientRegistration
                .withRegistrationId("registration-id-1")
                .redirectUri("{baseUrl}/{action}/oauth2/code/{registrationId}")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope("scope-1")
                .authorizationUri("https://example1.com/login/oauth/authorize")
                .tokenUri("https://example1.com/login/oauth/access_token")
                .jwkSetUri("https://example1.com/oauth2/jwk")
                .issuerUri("https://example1.com")
                .userInfoUri("https://api.example1.com/user")
                .userNameAttributeName("id-1")
                .clientName("Client Name 1")
                .clientId("client-id-1")
                .clientSecret("client-secret-1")
                .build();
    }
}
