// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.providers.postgresql;

import com.azure.spring.cloud.service.implementation.identity.AuthProperty;
import com.azure.spring.cloud.service.implementation.identity.credential.TokenCredentialProvider;
import com.azure.spring.cloud.service.implementation.identity.credential.CacheableTokenCredential;
import com.azure.spring.cloud.service.implementation.identity.credential.provider.DefaultTokenCredentialProvider;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AzureIdentityPostgresqlAuthenticationPluginTest {

    private static final String PROPERTIES = "properties";
    private static final String OSSRDBMS_SCOPES = "https://ossrdbms-aad.database.windows.net/.default";
    private static final String GET_TOKEN_AS_PASSWORD_ASYNC = "getTokenAsPasswordAsync";

    @Test
    void testTokenCredentialProvider() {
        Properties properties = new Properties();
        AzureIdentityPostgresqlAuthenticationPlugin plugin = new AzureIdentityPostgresqlAuthenticationPlugin(properties);
        TokenCredentialProvider tokenCredentialProviderAfterInit
            = (TokenCredentialProvider) ReflectionTestUtils.getField(plugin, "tokenCredentialProvider");
        assertNotNull(tokenCredentialProviderAfterInit);
        assertTrue(tokenCredentialProviderAfterInit instanceof DefaultTokenCredentialProvider);
    }

    @Test
    void testTokenAudienceShouldConfig() {
        Properties properties = new Properties();
        AzureIdentityPostgresqlAuthenticationPlugin plugin = new AzureIdentityPostgresqlAuthenticationPlugin(properties);
        Properties propertiesInTemplate = (Properties) ReflectionTestUtils.getField(plugin, PROPERTIES);
        assertEquals(OSSRDBMS_SCOPES, propertiesInTemplate.get(AuthProperty.SCOPES.getPropertyKey()));
    }

    @Test
    void testThrowIllegalStateException() {
        Properties properties = new Properties();
        AzureIdentityPostgresqlAuthenticationPlugin plugin = new AzureIdentityPostgresqlAuthenticationPlugin(properties);
        Mono<String> getTokenAsPasswordAsync = ReflectionTestUtils.invokeMethod(plugin, GET_TOKEN_AS_PASSWORD_ASYNC);
        assertNotNull(getTokenAsPasswordAsync);
    }

    @Test
    void testNoCache() {
        Properties propertiesNoCache = new Properties();
        AzureIdentityPostgresqlAuthenticationPlugin plugin = new AzureIdentityPostgresqlAuthenticationPlugin(propertiesNoCache);
        Mono<String> getTokenAsPasswordAsync = ReflectionTestUtils.invokeMethod(plugin, "getTokenAsPasswordAsync");
        TokenCredentialProvider tokenCredentialProvider
            = (TokenCredentialProvider) ReflectionTestUtils.getField(plugin, "tokenCredentialProvider");

        assertNotNull(getTokenAsPasswordAsync);
        assertNotNull(tokenCredentialProvider);
        assertFalse(tokenCredentialProvider.get() instanceof CacheableTokenCredential);
    }

    @Test
    void testCache() {
        Properties properties = new Properties();
        properties.setProperty(AuthProperty.CACHE_ENABLED.getPropertyKey(), "true");
        AzureIdentityPostgresqlAuthenticationPlugin plugin = new AzureIdentityPostgresqlAuthenticationPlugin(properties);
        Mono<String> getTokenAsPasswordAsync = ReflectionTestUtils.invokeMethod(plugin, "getTokenAsPasswordAsync");
        TokenCredentialProvider tokenCredentialProvider
            = (TokenCredentialProvider) ReflectionTestUtils.getField(plugin, "tokenCredentialProvider");

        assertNotNull(getTokenAsPasswordAsync);
        assertNotNull(tokenCredentialProvider);
        assertTrue(tokenCredentialProvider.get() instanceof CacheableTokenCredential);
    }

}
