// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.converter;

import com.azure.core.http.ProxyOptions;
import com.azure.spring.cloud.core.properties.proxy.HttpProxyProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AzureHttpProxyOptionsConverterTests {

    @Test
    void correctlyConverted() {
        HttpProxyProperties source = new HttpProxyProperties();
        source.setNonProxyHosts("localhost");
        source.setHostname("localhost");
        source.setPort(1234);
        source.setPassword("my-password");
        source.setUsername("my-username");
        source.setType("socks4");

        ProxyOptions target = AzureHttpProxyOptionsConverter.HTTP_PROXY_CONVERTER.convert(source);

        assertNotNull(target);
        assertEquals("(localhost)", target.getNonProxyHosts());
        assertEquals("localhost", target.getAddress().getHostName());
        assertEquals(1234, target.getAddress().getPort());
        assertEquals("my-password", target.getPassword());
        assertEquals("my-username", target.getUsername());
        assertEquals(ProxyOptions.Type.SOCKS4, target.getType());
    }

    @Test
    void returnNullWhenNoHostnameProvided() {
        HttpProxyProperties source = new HttpProxyProperties();
        source.setNonProxyHosts("localhost");
        source.setPort(1234);
        source.setPassword("my-password");
        source.setUsername("my-username");
        source.setType("socks4");

        ProxyOptions target = AzureHttpProxyOptionsConverter.HTTP_PROXY_CONVERTER.convert(source);
        assertNull(target);
    }

    @Test
    void returnNullWhenNoPortProvided() {
        HttpProxyProperties source = new HttpProxyProperties();
        source.setHostname("localhost");
        source.setNonProxyHosts("localhost");
        source.setPassword("my-password");
        source.setUsername("my-username");
        source.setType("socks4");

        ProxyOptions target = AzureHttpProxyOptionsConverter.HTTP_PROXY_CONVERTER.convert(source);
        assertNull(target);
    }

    @Test
    void correctlyConvertProxyType() {
        HttpProxyProperties source = new HttpProxyProperties();
        source.setHostname("localhost");
        source.setPort(1234);
        ProxyOptions target;

        source.setType("socks4");
        target = AzureHttpProxyOptionsConverter.HTTP_PROXY_CONVERTER.convert(source);
        assertNotNull(target);
        assertEquals(ProxyOptions.Type.SOCKS4, target.getType());

        source.setType("socks");
        target = AzureHttpProxyOptionsConverter.HTTP_PROXY_CONVERTER.convert(source);
        assertNotNull(target);
        assertEquals(ProxyOptions.Type.SOCKS4, target.getType());

        source.setType("socks5");
        target = AzureHttpProxyOptionsConverter.HTTP_PROXY_CONVERTER.convert(source);
        assertNotNull(target);
        assertEquals(ProxyOptions.Type.SOCKS5, target.getType());

        source.setType("http");
        target = AzureHttpProxyOptionsConverter.HTTP_PROXY_CONVERTER.convert(source);
        assertNotNull(target);
        assertEquals(ProxyOptions.Type.HTTP, target.getType());

        source.setType("abc");
        assertThrows(IllegalArgumentException.class, () -> AzureHttpProxyOptionsConverter.HTTP_PROXY_CONVERTER.convert(source), "Wrong proxy type provided!");

        source.setType(null);
        assertThrows(IllegalArgumentException.class, () -> AzureHttpProxyOptionsConverter.HTTP_PROXY_CONVERTER.convert(source), "Wrong proxy type provided!");
    }

}
