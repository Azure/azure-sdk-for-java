// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.aadb2c.security;

import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.AadB2cConstants;
import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.configuration.AadB2cAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.configuration.properties.AadB2cProperties;
import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.configuration.WebOAuth2ClientTestApp;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.http.converter.autoconfigure.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.restclient.autoconfigure.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.util.Assert;

import java.util.Arrays;

class AadB2cAuthorizationRequestResolverTests {

    private WebApplicationContextRunner getContextRunner() {
        return new WebApplicationContextRunner()
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .withConfiguration(AutoConfigurations.of(
                AzureGlobalPropertiesAutoConfiguration.class,
                WebOAuth2ClientTestApp.class,
                AadB2cAutoConfiguration.class,
                HttpMessageConvertersAutoConfiguration.class,
                RestTemplateAutoConfiguration.class))
            .withPropertyValues(
                String.format("%s=%s", AadB2cConstants.TENANT_ID, AadB2cConstants.TEST_TENANT_ID),
                String.format("%s=%s", AadB2cConstants.BASE_URI, AadB2cConstants.TEST_BASE_URI),
                String.format("%s=%s", AadB2cConstants.CLIENT_ID, AadB2cConstants.TEST_CLIENT_ID),
                String.format("%s=%s", AadB2cConstants.CLIENT_SECRET, AadB2cConstants.TEST_CLIENT_SECRET),
                String.format("%s=%s", AadB2cConstants.LOGOUT_SUCCESS_URL, AadB2cConstants.TEST_LOGOUT_SUCCESS_URL),
                String.format("%s=%s", AadB2cConstants.LOGIN_FLOW, AadB2cConstants.TEST_KEY_SIGN_UP_OR_IN),
                String.format("%s.%s=%s", AadB2cConstants.USER_FLOWS,
                    AadB2cConstants.TEST_KEY_SIGN_UP_OR_IN, AadB2cConstants.TEST_SIGN_UP_OR_IN_NAME),
                String.format("%s.%s=%s", AadB2cConstants.USER_FLOWS,
                    AadB2cConstants.TEST_KEY_PROFILE_EDIT, AadB2cConstants.TEST_PROFILE_EDIT_NAME),
                String.format("%s=%s", AadB2cConstants.CONFIG_PROMPT, AadB2cConstants.TEST_PROMPT),
                String.format("%s=%s", AadB2cConstants.CONFIG_LOGIN_HINT, AadB2cConstants.TEST_LOGIN_HINT)
            );
    }

    private HttpServletRequest getHttpServletRequest(String uri) {
        Assert.hasText(uri, "uri must contain text.");

        final MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.GET.toString(), uri);

        request.setServletPath(uri);

        return request;
    }

    @Test
    void testAutoConfigurationBean() {
        getContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.b2c.enabled=true")
            .run(c -> {
                String requestUri = "/fake-url";
                HttpServletRequest request = getHttpServletRequest(requestUri);
                final String registrationId = AadB2cConstants.TEST_SIGN_UP_OR_IN_NAME;
                final AadB2cAuthorizationRequestResolver resolver = c.getBean(AadB2cAuthorizationRequestResolver.class);

                Assertions.assertNotNull(resolver);
                Assertions.assertNull(resolver.resolve(request));
                Assertions.assertNull(resolver.resolve(request, registrationId));

                requestUri = "/oauth2/authorization/" + AadB2cConstants.TEST_SIGN_UP_OR_IN_NAME;
                request = getHttpServletRequest(requestUri);

                Assertions.assertNotNull(resolver.resolve(request));
                Assertions.assertNotNull(resolver.resolve(request, registrationId));

                Assertions.assertEquals(resolver.resolve(request).getAdditionalParameters().get("p"),
                    AadB2cConstants.TEST_SIGN_UP_OR_IN_NAME);
                Assertions.assertEquals(resolver.resolve(request).getAdditionalParameters().get(AadB2cConstants.PROMPT), AadB2cConstants.TEST_PROMPT);
                Assertions.assertEquals(resolver.resolve(request).getAdditionalParameters().get(AadB2cConstants.LOGIN_HINT), AadB2cConstants.TEST_LOGIN_HINT);
                Assertions.assertEquals((resolver.resolve(request).getClientId()), AadB2cConstants.TEST_CLIENT_ID);
                Assertions.assertEquals((resolver.resolve(request).getGrantType()),
                    AuthorizationGrantType.AUTHORIZATION_CODE);
                Assertions.assertTrue(resolver.resolve(request).getScopes().containsAll(Arrays.asList("openid", AadB2cConstants.TEST_CLIENT_ID)));
            });
    }

    @Test
    void testCustomAuthorizationRequestBaseUri() {
        final String customAuthorizationRequestBaseUri = "/login/oauth2/authorization";
        final String registrationId = AadB2cConstants.TEST_SIGN_UP_OR_IN_NAME;
        final AadB2cAuthorizationRequestResolver resolver = new AadB2cAuthorizationRequestResolver(
            customAuthorizationRequestBaseUri,
            new InMemoryClientRegistrationRepository(createClientRegistration(registrationId)),
            new AadB2cProperties());

        HttpServletRequest defaultRequest = getHttpServletRequest("/oauth2/authorization/" + registrationId);
        HttpServletRequest customRequest = getHttpServletRequest(customAuthorizationRequestBaseUri + "/" + registrationId);

        Assertions.assertNull(resolver.resolve(defaultRequest));
        Assertions.assertNull(resolver.resolve(defaultRequest, registrationId));

        final org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest resolved =
            resolver.resolve(customRequest);
        Assertions.assertNotNull(resolved);
        Assertions.assertNotNull(resolver.resolve(customRequest, registrationId));
        Assertions.assertEquals(registrationId, resolved.getAdditionalParameters().get("p"));
        Assertions.assertEquals("spring-boot-starter",
            resolved.getAdditionalParameters().get("x-client-SKU"));
    }

    @Test
    void testBaseUriNormalizationWithoutLeadingSlash() {
        // Base URI without leading '/' should be normalized to include it
        final String baseUriWithoutLeadingSlash = "oauth2/authorization";
        final String registrationId = AadB2cConstants.TEST_SIGN_UP_OR_IN_NAME;
        final AadB2cAuthorizationRequestResolver resolver = new AadB2cAuthorizationRequestResolver(
            baseUriWithoutLeadingSlash,
            new InMemoryClientRegistrationRepository(createClientRegistration(registrationId)),
            new AadB2cProperties());

        // Request with leading '/' should match
        HttpServletRequest request = getHttpServletRequest("/oauth2/authorization/" + registrationId);
        final org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest resolved =
            resolver.resolve(request);
        Assertions.assertNotNull(resolved);
    }

    @Test
    void testBaseUriNormalizationWithMultipleTrailingSlashes() {
        // Base URI with multiple trailing slashes should be normalized
        final String baseUriWithTrailingSlashes = "/oauth2/authorization///";
        final String registrationId = AadB2cConstants.TEST_SIGN_UP_OR_IN_NAME;
        final AadB2cAuthorizationRequestResolver resolver = new AadB2cAuthorizationRequestResolver(
            baseUriWithTrailingSlashes,
            new InMemoryClientRegistrationRepository(createClientRegistration(registrationId)),
            new AadB2cProperties());

        // Request should match against normalized path
        HttpServletRequest request = getHttpServletRequest("/oauth2/authorization/" + registrationId);
        final org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest resolved =
            resolver.resolve(request);
        Assertions.assertNotNull(resolved);
    }

    private ClientRegistration createClientRegistration(String registrationId) {
        return ClientRegistration.withRegistrationId(registrationId)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/{action}/oauth2/code/{registrationId}")
            .scope("openid")
            .authorizationUri("https://faketenant.b2clogin.com/oauth2/v2.0/authorize")
            .tokenUri("https://faketenant.b2clogin.com/oauth2/v2.0/token")
            .clientName("b2c")
            .clientId(AadB2cConstants.TEST_CLIENT_ID)
            .clientSecret(AadB2cConstants.TEST_CLIENT_SECRET)
            .build();
    }
}
