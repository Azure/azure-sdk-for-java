// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.providers.mysql;

import com.azure.spring.cloud.service.implementation.identity.AuthProperty;
import com.azure.spring.cloud.service.implementation.identity.credential.TokenCredentialProvider;
import com.azure.spring.cloud.service.implementation.identity.credential.CacheableTokenCredential;
import com.azure.spring.cloud.service.implementation.identity.credential.provider.DefaultTokenCredentialProvider;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.protocol.Protocol;
import com.mysql.cj.protocol.a.NativePacketPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AzureIdentityMysqlAuthenticationPluginTest {
    private static final String OSSRDBMS_SCOPES = "https://ossrdbms-aad.database.windows.net/.default";
    private static final String CLEAR_PASSWORD = "mysql_clear_password";
    private static final String PROPERTIES = "properties";

    Protocol<NativePacketPayload> protocol;
    Properties properties = new Properties();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        protocol = mock(Protocol.class);
        PropertySet propertySet = mock(PropertySet.class);
        when(protocol.getPropertySet()).thenReturn(propertySet);
        when(propertySet.exposeAsProperties()).thenReturn(properties);
    }

    @Test
    void testNoCache() {
        AzureIdentityMysqlAuthenticationPlugin plugin = new AzureIdentityMysqlAuthenticationPlugin();
        plugin.init(protocol);
        assertNull(properties.get(AuthProperty.CACHE_ENABLED.getPropertyKey()));
        Mono<String> getTokenAsPasswordAsync = ReflectionTestUtils.invokeMethod(plugin, "getTokenAsPasswordAsync");
        TokenCredentialProvider tokenCredentialProvider
            = (TokenCredentialProvider) ReflectionTestUtils.getField(plugin, "tokenCredentialProvider");

        assertNotNull(getTokenAsPasswordAsync);
        assertNotNull(tokenCredentialProvider);
        assertFalse(tokenCredentialProvider.get() instanceof CacheableTokenCredential);
    }

    @Test
    void testCache() {
        properties.setProperty(AuthProperty.CACHE_ENABLED.getPropertyKey(), "true");
        assertEquals("true", properties.get(AuthProperty.CACHE_ENABLED.getPropertyKey()));

        AzureIdentityMysqlAuthenticationPlugin plugin = new AzureIdentityMysqlAuthenticationPlugin();
        plugin.init(protocol);
        Mono<String> getTokenAsPasswordAsync = ReflectionTestUtils.invokeMethod(plugin, "getTokenAsPasswordAsync");
        TokenCredentialProvider tokenCredentialProvider
            = (TokenCredentialProvider) ReflectionTestUtils.getField(plugin, "tokenCredentialProvider");

        assertNotNull(getTokenAsPasswordAsync);
        assertNotNull(tokenCredentialProvider);

        assertTrue(tokenCredentialProvider.get() instanceof CacheableTokenCredential);
    }

    @Test
    void testDefaultTokenCredentialProvider() {

        AzureIdentityMysqlAuthenticationPlugin plugin = new AzureIdentityMysqlAuthenticationPlugin();
        TokenCredentialProvider tokenCredentialProviderBeforeInit
            = (TokenCredentialProvider) ReflectionTestUtils.getField(plugin, "tokenCredentialProvider");
        assertNull(tokenCredentialProviderBeforeInit);

        plugin.init(protocol);
        TokenCredentialProvider tokenCredentialProviderAfterInit
            = (TokenCredentialProvider) ReflectionTestUtils.getField(plugin, "tokenCredentialProvider");
        assertNotNull(tokenCredentialProviderAfterInit);
        assertTrue(tokenCredentialProviderAfterInit instanceof DefaultTokenCredentialProvider);
    }

    @Test
    void testTokenAudienceShouldConfig() {

        AzureIdentityMysqlAuthenticationPlugin plugin = new AzureIdentityMysqlAuthenticationPlugin();
        plugin.init(protocol);
        Properties propertiesInTemplate = (Properties) ReflectionTestUtils.getField(plugin, PROPERTIES);
        assertEquals(OSSRDBMS_SCOPES, propertiesInTemplate.get(AuthProperty.SCOPES.getPropertyKey()));
    }

    @Test
    void testThrowIllegalStateException() {
        AzureIdentityMysqlAuthenticationPlugin plugin = new AzureIdentityMysqlAuthenticationPlugin();
        assertThrows(IllegalStateException.class, () -> ReflectionTestUtils.invokeMethod(plugin, "getTokenAsPasswordAsync"));
    }

    @Test
    void testPluginName() {
        AzureIdentityMysqlAuthenticationPlugin plugin = new AzureIdentityMysqlAuthenticationPlugin();
        String protocolPluginName = plugin.getProtocolPluginName();
        assertEquals(CLEAR_PASSWORD, protocolPluginName);
    }
}
