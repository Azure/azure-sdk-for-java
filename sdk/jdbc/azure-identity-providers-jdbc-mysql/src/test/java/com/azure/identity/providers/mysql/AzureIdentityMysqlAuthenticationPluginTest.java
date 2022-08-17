package com.azure.identity.providers.mysql;


import com.azure.identity.providers.jdbc.template.AzureAuthenticationTemplate;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.protocol.Protocol;
import com.mysql.cj.protocol.a.NativePacketPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void testThrowIllegalStateException() {
        AzureIdentityMysqlAuthenticationPlugin plugin = new AzureIdentityMysqlAuthenticationPlugin();
//        assertThrows(IllegalStateException.class, () -> ReflectionTestUtils.invokeMethod(plugin, "getTokenAsPasswordAsync"));
    }

    @Test
    void testPluginName() {
        AzureIdentityMysqlAuthenticationPlugin plugin = new AzureIdentityMysqlAuthenticationPlugin();
        String protocolPluginName = plugin.getProtocolPluginName();
        assertEquals(CLEAR_PASSWORD, protocolPluginName);
    }

    @Test
    protected void tokenAudienceShouldConfig() {
        AzureAuthenticationTemplate template = new AzureAuthenticationTemplate();
        AzureIdentityMysqlAuthenticationPlugin plugin = new AzureIdentityMysqlAuthenticationPlugin(template);
        plugin.init(protocol);
//        Properties properties = (Properties) ReflectionUtils.getField(template.getClass(), "properties", template);
//        assertEquals(OSSRDBMS_SCOPES, properties.getProperty(AuthProperty.SCOPES.getPropertyKey()));
    }

}
