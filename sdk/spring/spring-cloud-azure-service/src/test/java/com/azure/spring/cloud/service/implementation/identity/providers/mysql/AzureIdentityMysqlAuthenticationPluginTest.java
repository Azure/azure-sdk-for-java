// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.providers.mysql;

import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.protocol.Protocol;
import com.mysql.cj.protocol.a.NativePacketPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AzureIdentityMysqlAuthenticationPluginTest {
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
