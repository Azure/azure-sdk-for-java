// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.api;

import com.azure.core.credential.AccessToken;
import com.azure.identity.ClientSecretCredential;
import com.azure.spring.cloud.service.implementation.identity.AuthProperty;
import com.azure.spring.cloud.service.implementation.identity.AzureAuthenticationTemplate;
import com.azure.spring.cloud.service.implementation.identity.StaticAccessTokenCache;
import com.azure.spring.cloud.service.implementation.identity.credential.CacheableTokenCredential;
import com.azure.spring.cloud.service.implementation.identity.credential.TokenCredentialProvider;
import com.azure.spring.cloud.service.implementation.identity.credential.TokenCredentialProviderOptions;
import com.azure.spring.cloud.service.implementation.identity.credential.adapter.CacheableClientSecretCredential;
import com.azure.spring.cloud.service.implementation.identity.token.AccessTokenResolver;
import com.azure.spring.cloud.service.implementation.identity.token.AccessTokenResolverOptions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AzureAuthenticationTemplateTest {

    private static final String IS_INITIALIZED = "isInitialized";
    private static final String OSSRDBMS_SCOPE = "https://ossrdbms-aad.database.windows.net/.default";

    @Test
    void initCalledOnlyOnce() {
        AzureAuthenticationTemplate template = new AzureAuthenticationTemplate();
        AtomicBoolean isInitialized = (AtomicBoolean) ReflectionTestUtils.getField(template, IS_INITIALIZED);
        assertFalse(isInitialized.get());
        Properties properties = new Properties();
        template.init(properties);
        assertTrue(isInitialized.get());
        assertFalse(isInitialized.compareAndSet(false, true));
    }

    @Test
    void shouldCallInitOnly() {
    }

    @Test
    void shouldCallInitFirst() {
        AzureAuthenticationTemplate template = new AzureAuthenticationTemplate();
        assertThrows(IllegalStateException.class, template::getTokenAsPasswordAsync);
    }

    @Test
    void testCallInit() {
        AzureAuthenticationTemplate template = new AzureAuthenticationTemplate();
        Properties properties = new Properties();
        properties.setProperty(AuthProperty.TENANT_ID.getPropertyKey(), "fake-tenant-id");
        properties.setProperty(AuthProperty.SCOPES.getPropertyKey(), OSSRDBMS_SCOPE);
        template.init(properties);
        assertNotNull(template.getTokenAsPasswordAsync());
    }

    @Test
    void testCachedToken() {

        TokenCredentialProvider tokenCredentialProvider = getCachedTokenCredentialProvider();
        AccessTokenResolver accessTokenResolver = getAccessTokenResolver();

        Properties properties = new Properties();
        AzureAuthenticationTemplate template1 = new AzureAuthenticationTemplate(tokenCredentialProvider, accessTokenResolver);
        template1.init(properties);

        AzureAuthenticationTemplate template2 = new AzureAuthenticationTemplate(tokenCredentialProvider, accessTokenResolver);
        template2.init(properties);

        assertNotEquals(template1.getTokenAsPasswordAsync(), template2.getTokenAsPasswordAsync());
        assertEquals(template1.getTokenAsPassword(), template2.getTokenAsPassword());

    }

    @Test
    void testNotCachedToken() {

        AccessTokenResolver accessTokenResolver = getAccessTokenResolver();
        TokenCredentialProvider tokenCredentialProvider = getTokenCredentialProvider();

        Properties properties = new Properties();
        AzureAuthenticationTemplate template1 = new AzureAuthenticationTemplate(tokenCredentialProvider, accessTokenResolver);
        template1.init(properties);

        AzureAuthenticationTemplate template2 = new AzureAuthenticationTemplate(tokenCredentialProvider, accessTokenResolver);
        template2.init(properties);

        assertNotEquals(template1.getTokenAsPasswordAsync(), template2.getTokenAsPasswordAsync());
        assertNotEquals(template1.getTokenAsPassword(), template2.getTokenAsPassword());
    }

    @Test
    void testGetTokenAsPasswordAsync() {
        Properties properties = new Properties();

        AzureAuthenticationTemplate template1 = new AzureAuthenticationTemplate();
        template1.init(properties);
        AzureAuthenticationTemplate template2 = new AzureAuthenticationTemplate();
        template2.init(properties);

        assertNotNull(template1.getTokenAsPasswordAsync());
        assertNotNull(template2.getTokenAsPasswordAsync());

        assertNotEquals(template1.getTokenAsPasswordAsync(), template2.getTokenAsPasswordAsync());

    }

    private static ClientSecretCredential getClientSecretCredential() {
        ClientSecretCredential delegate = mock(ClientSecretCredential.class);
        when(delegate.getToken(any()))
            .thenReturn(Mono.just(new AccessToken("fake-access-token-1", OffsetDateTime.now().plusHours(2))))
            .thenReturn(Mono.just(new AccessToken("fake-access-token-2", OffsetDateTime.now().plusHours(2))));
        return delegate;
    }

    private static TokenCredentialProvider getCachedTokenCredentialProvider() {
        TokenCredentialProviderOptions providerOptions = getProviderOptions();
        ClientSecretCredential delegate = getClientSecretCredential();

        TokenCredentialProvider tokenCredentialProvider = mock(TokenCredentialProvider.class);
        when(tokenCredentialProvider.get())
            .thenReturn(
                new CacheableTokenCredential(
                    StaticAccessTokenCache.getInstance(),
                    new CacheableClientSecretCredential(providerOptions, delegate)));
        return tokenCredentialProvider;
    }

    private static TokenCredentialProvider getTokenCredentialProvider() {
        ClientSecretCredential delegate = getClientSecretCredential();

        TokenCredentialProvider tokenCredentialProvider = mock(TokenCredentialProvider.class);
        when(tokenCredentialProvider.get()).thenReturn(delegate);
        return tokenCredentialProvider;
    }

    private static AccessTokenResolver getAccessTokenResolver() {
        AccessTokenResolverOptions resolverOptions = new AccessTokenResolverOptions();
        resolverOptions.setTenantId("fake-tenant-id");
        resolverOptions.setScopes(new String[]{OSSRDBMS_SCOPE});
        return AccessTokenResolver.createDefault(resolverOptions);
    }

    private static TokenCredentialProviderOptions getProviderOptions() {
        TokenCredentialProviderOptions providerOptions = new TokenCredentialProviderOptions();
        providerOptions.setTenantId("fake-tenant-id");
        providerOptions.setClientId("fake-client-id");
        providerOptions.setClientSecret("fake-client-secret");
        providerOptions.setAuthorityHost("fake-authority-host");
        return providerOptions;
    }
}
