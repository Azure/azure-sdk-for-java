// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.extensions.implementation.template;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.identity.extensions.implementation.credential.TokenCredentialProviderOptions;
import com.azure.identity.extensions.implementation.credential.provider.CachingTokenCredentialProvider;
import com.azure.identity.extensions.implementation.credential.provider.DefaultTokenCredentialProvider;
import com.azure.identity.extensions.implementation.enums.AuthProperty;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.identity.extensions.implementation.enums.AuthProperty.GET_TOKEN_TIMEOUT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class AzureAuthenticationTemplateTest {

    private static final String OSSRDBMS_SCOPE = "https://ossrdbms-aad.database.windows.net/.default";

    @Test
    void testInitShouldCalledOnlyOnce() {
        AzureAuthenticationTemplate template = new AzureAuthenticationTemplate();
        assertFalse(template.getIsInitialized().get());
        Properties properties = new Properties();
        template.init(properties);
        assertTrue(template.getIsInitialized().get());
        assertFalse(template.getIsInitialized().compareAndSet(false, true));
    }

    @Test
    void testShouldCallInitFirst() {
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

    @Test
    void cacheTokenCredential() {
        Properties properties = new Properties();
        AzureAuthenticationTemplate template = new AzureAuthenticationTemplate();
        template.init(properties);

        AzureAuthenticationTemplate template2 = new AzureAuthenticationTemplate();
        template2.init(properties);

        TokenCredentialProviderOptions providerOptions = new TokenCredentialProviderOptions(properties);

        TokenCredential tokenCredential = template.getTokenCredentialProvider().get(providerOptions);
        TokenCredential tokenCredential2 = template2.getTokenCredentialProvider().get(providerOptions);
        assertNotNull(tokenCredential);
        assertNotNull(tokenCredential2);
        assertSame(tokenCredential, tokenCredential2);
    }

    @Test
    void nonCacheTokenCredential() {
        Properties properties = new Properties();
        properties.setProperty("azure.tokenCredentialCacheEnabled", "false");
        AzureAuthenticationTemplate template = new AzureAuthenticationTemplate();
        template.init(properties);

        AzureAuthenticationTemplate template2 = new AzureAuthenticationTemplate();
        template2.init(properties);

        TokenCredentialProviderOptions providerOptions = new TokenCredentialProviderOptions(properties);

        assertNotNull(template.getTokenCredentialProvider().get(providerOptions));
        assertNotNull(template2.getTokenCredentialProvider().get(providerOptions));
        assertNotEquals(template.getTokenCredentialProvider().get(providerOptions),
            template2.getTokenCredentialProvider().get(providerOptions));
    }

    @Test
    void verityTokeWithDefaultCredentialProvider() throws InterruptedException {
        // setup
        String token1 = "token1";
        String token2 = "token2";
        int tokenExpireSeconds = 3;
        OffsetDateTime offsetDateTime = OffsetDateTime.now().plusSeconds(tokenExpireSeconds);
        TokenCredential mockTokenCredential = request -> {
            if (OffsetDateTime.now().isBefore(offsetDateTime)) {
                return Mono.just(new AccessToken(token1, offsetDateTime));
            } else {
                return Mono.just(new AccessToken(token2, offsetDateTime.plusSeconds(tokenExpireSeconds)));
            }
        };
        // mock
        try (MockedConstruction<DefaultTokenCredentialProvider> defaultCredentialProviderMock
            = mockConstruction(DefaultTokenCredentialProvider.class, (defaultTokenCredentialProvider,
                context) -> when(defaultTokenCredentialProvider.get()).thenReturn(mockTokenCredential))) {
            Properties properties = new Properties();
            properties.setProperty("azure.tokenCredentialCacheEnabled", "false");

            AzureAuthenticationTemplate template = new AzureAuthenticationTemplate();
            template.init(properties);
            for (int i = 0; i < 5; i++) {
                assertEquals(token1, template.getTokenAsPassword());
            }
            TimeUnit.SECONDS.sleep(tokenExpireSeconds);
            assertEquals(token2, template.getTokenAsPassword());

            assertNotNull(defaultCredentialProviderMock);
        }
    }

    @Disabled("Enable it when it is stable")
    @Test
    void verityTokenWithCachingCredentialProvider() throws InterruptedException {
        int tokenExpireSeconds = 2;
        AtomicInteger tokenIndex1 = new AtomicInteger();
        AtomicInteger tokenIndex2 = new AtomicInteger(1);
        OffsetDateTime offsetDateTime = OffsetDateTime.now().plusSeconds(tokenExpireSeconds);
        TokenCredential mockTokenCredential = request -> {
            if (OffsetDateTime.now().isBefore(offsetDateTime)) {
                return Mono.just(new AccessToken("token1-" + (tokenIndex1.getAndIncrement()), offsetDateTime));
            } else {
                return Mono.just(new AccessToken("token2-" + (tokenIndex2.getAndIncrement()),
                    offsetDateTime.plusSeconds(tokenExpireSeconds)));
            }
        };
        // mock
        try (MockedConstruction<CachingTokenCredentialProvider> credentialProviderMock
            = mockConstruction(CachingTokenCredentialProvider.class, (defaultTokenCredentialProvider,
                context) -> when(defaultTokenCredentialProvider.get()).thenReturn(mockTokenCredential))) {
            Properties properties = new Properties();

            AzureAuthenticationTemplate template = new AzureAuthenticationTemplate();
            template.init(properties);
            AzureAuthenticationTemplate template2 = new AzureAuthenticationTemplate();
            template2.init(properties);

            verifyToken("token1-", 0, template);
            TimeUnit.SECONDS.sleep(tokenExpireSeconds + 1);
            verifyToken("token2-", 1, template2);
            assertNotNull(credentialProviderMock);
        }
    }

    private static void verifyToken(String tokenPrefix, int tokenInitialIndexValue,
        AzureAuthenticationTemplate template) {
        for (int i = 0; i < 5; i++) {
            assertEquals(tokenPrefix + (tokenInitialIndexValue + i), template.getTokenAsPassword());
        }
    }

    @Test
    void useDefaultAccessTokenTimeout() throws NoSuchFieldException, IllegalAccessException {
        AzureAuthenticationTemplate template = new AzureAuthenticationTemplate();
        Properties properties = new Properties();
        template.init(properties);
        assertNotNull(template.getBlockTimeout());
        Field defaultValueField = GET_TOKEN_TIMEOUT.getClass().getDeclaredField("defaultValue");
        defaultValueField.setAccessible(true);
        String defaultVault = (String) defaultValueField.get(GET_TOKEN_TIMEOUT);
        assertEquals(template.getBlockTimeout().getSeconds() + "", defaultVault);
    }

    @Test
    void useCustomAccessTokenTimeout() {
        AzureAuthenticationTemplate template = new AzureAuthenticationTemplate();
        Properties properties = new Properties();
        properties.setProperty(AuthProperty.GET_TOKEN_TIMEOUT.getPropertyKey(), "35");
        template.init(properties);
        assertNotNull(template.getBlockTimeout());
        assertEquals(35, template.getBlockTimeout().getSeconds());
    }
}
