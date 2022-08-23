// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.mysql;

import com.azure.identity.providers.jdbc.implementation.enums.AuthProperty;
import com.azure.identity.providers.jdbc.implementation.template.AzureAuthenticationTemplate;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.protocol.Protocol;
import com.mysql.cj.protocol.a.NativePacketPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AzureIdentityMysqlAuthenticationPluginTest {
    protected static final String OSSRDBMS_SCOPES = "https://ossrdbms-aad.database.windows.net/.default";
    private static final String CLEAR_PASSWORD = "mysql_clear_password";

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
    void testTokenAsPasswordAsyncWithoutInit() {
        AzureAuthenticationTemplate template = new AzureAuthenticationTemplate();
        assertThrows(IllegalStateException.class, () -> template.getTokenAsPasswordAsync());
    }

    @Test
    void testPluginName() {
        AzureIdentityMysqlAuthenticationPlugin plugin = new AzureIdentityMysqlAuthenticationPlugin();
        String protocolPluginName = plugin.getProtocolPluginName();
        assertEquals(CLEAR_PASSWORD, protocolPluginName);
    }

    @Test
    void tokenAudienceShouldConfig() {
        AzureAuthenticationTemplate template = new AzureAuthenticationTemplate();
        AzureIdentityMysqlAuthenticationPlugin plugin = new AzureIdentityMysqlAuthenticationPlugin(template);
        plugin.init(protocol);
        assertEquals(OSSRDBMS_SCOPES, properties.getProperty(AuthProperty.SCOPES.getPropertyKey()));
    }

    @Test
    void testRequiresConfidentiality() {
        AzureIdentityMysqlAuthenticationPlugin plugin = new AzureIdentityMysqlAuthenticationPlugin();
        assertTrue(plugin.requiresConfidentiality());
    }

    @Test
    void testIsReusable() {
        AzureIdentityMysqlAuthenticationPlugin plugin = new AzureIdentityMysqlAuthenticationPlugin();
        assertTrue(plugin.isReusable());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testNextAuthenticationStep() throws UnsupportedEncodingException {
        AzureAuthenticationTemplate template = mock(AzureAuthenticationTemplate.class);
        when(template.getTokenAsPassword()).thenReturn("fake-password");

        Protocol<NativePacketPayload> protocol = mock(Protocol.class, Answers.RETURNS_DEEP_STUBS);
        when(protocol.getSocketConnection().isSSLEstablished()).thenReturn(true);
        when(protocol.getServerSession().getCharsetSettings().getPasswordCharacterEncoding()).thenReturn("utf-8");

        AzureIdentityMysqlAuthenticationPlugin plugin = new AzureIdentityMysqlAuthenticationPlugin(template, protocol);
        NativePacketPayload fromServer = new NativePacketPayload(new byte[0]);
        List<NativePacketPayload> toServer = new ArrayList<>();
        plugin.nextAuthenticationStep(fromServer, toServer);
        assertTrue(new String(toServer.get(0).getByteBuffer(), "utf-8").startsWith("fake-password"));
    }

}
