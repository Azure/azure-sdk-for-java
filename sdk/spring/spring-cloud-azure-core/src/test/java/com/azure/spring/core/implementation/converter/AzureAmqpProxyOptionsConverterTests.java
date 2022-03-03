// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.converter;

import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.spring.cloud.core.properties.proxy.AmqpProxyProperties;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AzureAmqpProxyOptionsConverterTests {

    @Test
    void correctlyConverted() {
        AmqpProxyProperties source = new AmqpProxyProperties();
        source.setHostname("localhost");
        source.setPort(1234);
        source.setAuthenticationType("basic");
        source.setUsername("my-username");
        source.setPassword("my-password");
        source.setType("socks");

        ProxyOptions target = AzureAmqpProxyOptionsConverter.AMQP_PROXY_CONVERTER.convert(source);

        assertNotNull(target);
        assertEquals(Proxy.Type.SOCKS, target.getProxyAddress().type());
        assertEquals(InetSocketAddress.class, target.getProxyAddress().address().getClass());
        assertEquals("localhost", ((InetSocketAddress) target.getProxyAddress().address()).getHostName());
        assertEquals(1234, ((InetSocketAddress) target.getProxyAddress().address()).getPort());

        assertEquals(ProxyAuthenticationType.BASIC, target.getAuthentication());
        assertEquals("my-username", target.getCredential().getUserName());
        assertEquals("my-password", new String(target.getCredential().getPassword()));
    }

    @Test
    void returnNullWhenNoHostnameProvided() {
        AmqpProxyProperties source = new AmqpProxyProperties();
        source.setPort(1234);
        source.setAuthenticationType("basic");
        source.setUsername("my-username");
        source.setPassword("my-password");
        source.setType("socks");

        ProxyOptions target = AzureAmqpProxyOptionsConverter.AMQP_PROXY_CONVERTER.convert(source);

        assertNull(target);
    }

    @Test
    void returnNullWhenNoPortProvided() {
        AmqpProxyProperties source = new AmqpProxyProperties();
        source.setHostname("localhost");
        source.setAuthenticationType("basic");
        source.setUsername("my-username");
        source.setPassword("my-password");
        source.setType("socks");

        ProxyOptions target = AzureAmqpProxyOptionsConverter.AMQP_PROXY_CONVERTER.convert(source);

        assertNull(target);
    }

    @Test
    void correctlyConvertProxyType() {
        AmqpProxyProperties source = new AmqpProxyProperties();
        source.setHostname("localhost");
        source.setPort(1234);
        ProxyOptions target;

        source.setType("socks");
        target = AzureAmqpProxyOptionsConverter.AMQP_PROXY_CONVERTER.convert(source);
        assertNotNull(target);
        assertEquals(Proxy.Type.SOCKS, target.getProxyAddress().type());

        source.setType("http");
        target = AzureAmqpProxyOptionsConverter.AMQP_PROXY_CONVERTER.convert(source);
        assertNotNull(target);
        assertEquals(Proxy.Type.HTTP, target.getProxyAddress().type());

        source.setType("abc");
        assertThrows(IllegalArgumentException.class, () -> AzureAmqpProxyOptionsConverter.AMQP_PROXY_CONVERTER.convert(source), "Wrong proxy type provided!");
    }

    @Test
    void correctlyConvertAuthenticationType() {
        AmqpProxyProperties source = new AmqpProxyProperties();
        source.setHostname("localhost");
        source.setPort(1234);
        source.setType("http");
        ProxyOptions target;

        source.setAuthenticationType(null);
        target = AzureAmqpProxyOptionsConverter.AMQP_PROXY_CONVERTER.convert(source);
        assertNotNull(target);
        assertEquals(ProxyAuthenticationType.NONE, target.getAuthentication());

        source.setAuthenticationType("abc");
        target = AzureAmqpProxyOptionsConverter.AMQP_PROXY_CONVERTER.convert(source);
        assertNotNull(target);
        assertEquals(ProxyAuthenticationType.NONE, target.getAuthentication());

        source.setAuthenticationType("basic");
        target = AzureAmqpProxyOptionsConverter.AMQP_PROXY_CONVERTER.convert(source);
        assertNotNull(target);
        assertEquals(ProxyAuthenticationType.BASIC, target.getAuthentication());


        source.setAuthenticationType("DIGEST");
        target = AzureAmqpProxyOptionsConverter.AMQP_PROXY_CONVERTER.convert(source);
        assertNotNull(target);
        assertEquals(ProxyAuthenticationType.DIGEST, target.getAuthentication());

    }

}
