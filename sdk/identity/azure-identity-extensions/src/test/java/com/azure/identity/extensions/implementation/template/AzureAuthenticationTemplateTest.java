// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.extensions.implementation.template;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.extensions.implementation.credential.provider.DefaultTokenCredentialProvider;
import com.azure.identity.extensions.implementation.enums.AuthProperty;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class AzureAuthenticationTemplateTest {

    private static final String OSSRDBMS_SCOPE = "https://ossrdbms-aad.database.windows.net/.default";

    @Test
    void testInitShouldCalledOnlyOnce() {
        AzureAuthenticationTemplate template = new AzureAuthenticationTemplate();
        assertFalse(template.getIsInitialized().get());
        Properties properties = new Properties();
        Configuration configuration = new Configuration();
        configuration.put("a", "configurations");
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
    void testGetTokenAsPassword() throws InterruptedException {
        // setup
        String token1 = "token1";
        String token2 = "token2";
        int tokenExpireSeconds = 2;
        TokenCredential mockTokenCredential = mock(TokenCredential.class);
        OffsetDateTime offsetDateTime = OffsetDateTime.now().plusSeconds(tokenExpireSeconds);
        when(mockTokenCredential.getToken(any()))
            .thenAnswer(u -> {
                if (OffsetDateTime.now().isBefore(offsetDateTime)) {
                    return Mono.just(new AccessToken(token1, offsetDateTime));
                } else {
                    return Mono.just(new AccessToken(token2, offsetDateTime.plusSeconds(tokenExpireSeconds)));
                }
            });
        // mock
        try (MockedConstruction<DefaultTokenCredentialProvider> identityClientMock = mockConstruction(DefaultTokenCredentialProvider.class, (defaultTokenCredentialProvider, context) -> {
            when(defaultTokenCredentialProvider.get()).thenReturn(mockTokenCredential);
        })) {
            Properties properties = new Properties();

            AzureAuthenticationTemplate template = new AzureAuthenticationTemplate();
            template.init(properties);
            for (int i = 0; i < 5; i++) {
                assertEquals(token1, template.getTokenAsPassword());
            }
            TimeUnit.SECONDS.sleep(tokenExpireSeconds);
            assertEquals(token2, template.getTokenAsPassword());

            assertNotNull(identityClientMock);
        }
    }

}
