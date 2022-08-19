// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.credential.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.providers.jdbc.implementation.credential.TokenCredentialProviderOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class SpringTokenCredentialProviderTest {
    public static final String DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME = "springCloudAzureDefaultCredential";
    public static final String CUSTOMIZED_TOKEN_CREDENTIAL_BEAN_NAME = "springCloudAzureCustomizedCredential";

    @BeforeEach
    void setGlobalApplicationContext() {
        SpringTokenCredentialProvider.setGlobalApplicationContext(null);
    }

    @Test
    void testDefaultConstructor() {
        SpringTokenCredentialProvider provider = new SpringTokenCredentialProvider(null);
        assertThrows(NullPointerException.class, () -> provider.get());
    }

    @Test
    void testDefaultCredentialBean() {
        ApplicationContext context = mock(ApplicationContext.class);
        TokenCredential credential = mock(TokenCredential.class);
        when(context.getBean(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME, TokenCredential.class)).thenReturn(credential);

        SpringTokenCredentialProvider provider = new SpringTokenCredentialProvider(null);
        provider.setApplicationContext(context);
        assertEquals(credential, provider.get());
    }

    @Test
    void testCustomizedCredentialBean() {
        ApplicationContext context = mock(ApplicationContext.class);
        TokenCredential credential = mock(TokenCredential.class);

        TokenCredentialProviderOptions options = new TokenCredentialProviderOptions();
        options.setTokenCredentialBeanName(CUSTOMIZED_TOKEN_CREDENTIAL_BEAN_NAME);
        when(context.getBean(CUSTOMIZED_TOKEN_CREDENTIAL_BEAN_NAME, TokenCredential.class)).thenReturn(credential);

        SpringTokenCredentialProvider provider = new SpringTokenCredentialProvider(options);
        provider.setApplicationContext(context);
        assertEquals(credential, provider.get());
    }

    @Test
    void testGlobalContext() {
        ApplicationContext globalContext = mock(ApplicationContext.class);
        TokenCredential credential = mock(TokenCredential.class);
        when(globalContext.getBean(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME, TokenCredential.class)).thenReturn(credential);

        SpringTokenCredentialProvider.setGlobalApplicationContext(globalContext);
        SpringTokenCredentialProvider provider = new SpringTokenCredentialProvider(null);

        assertEquals(credential, provider.get());
    }

    @Test
    void testLocalOverGlobalContext() {
        ApplicationContext globalContext = mock(ApplicationContext.class);
        ApplicationContext localContext = mock(ApplicationContext.class);

        TokenCredential globalCredential = mock(TokenCredential.class);
        TokenCredential localCredential = mock(TokenCredential.class);

        when(globalContext.getBean(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME, TokenCredential.class)).thenReturn(globalCredential);
        when(localContext.getBean(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME, TokenCredential.class)).thenReturn(localCredential);

        SpringTokenCredentialProvider.setGlobalApplicationContext(globalContext);

        SpringTokenCredentialProvider provider = new SpringTokenCredentialProvider(null);
        provider.setApplicationContext(localContext);

        assertNotEquals(globalCredential, provider.get());
        assertEquals(localCredential, provider.get());

    }



}
