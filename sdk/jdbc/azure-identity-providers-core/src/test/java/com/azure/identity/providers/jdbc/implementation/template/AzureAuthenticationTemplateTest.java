// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.template;

import com.azure.core.credential.AccessToken;
import com.azure.core.util.Configuration;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.providers.jdbc.implementation.credential.provider.TokenCredentialProvider;
import com.azure.identity.providers.jdbc.implementation.token.AccessTokenResolver;
import com.azure.identity.providers.jdbc.implementation.enums.AuthProperty;
import com.azure.identity.providers.jdbc.implementation.credential.TokenCredentialProviderOptions;
import com.azure.identity.providers.jdbc.implementation.token.AccessTokenResolverOptions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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

    private static ClientSecretCredential getClientSecretCredential() {
        ClientSecretCredential delegate = mock(ClientSecretCredential.class);
        when(delegate.getToken(any()))
            .thenReturn(Mono.just(new AccessToken("fake-access-token-1", OffsetDateTime.now().plusHours(2))))
            .thenReturn(Mono.just(new AccessToken("fake-access-token-2", OffsetDateTime.now().plusHours(2))));
        return delegate;
    }


    private static TokenCredentialProvider getTokenCredentialProvider() {
        ClientSecretCredential credential = getClientSecretCredential();

        TokenCredentialProvider tokenCredentialProvider = mock(TokenCredentialProvider.class);
        when(tokenCredentialProvider.get()).thenReturn(credential);
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
