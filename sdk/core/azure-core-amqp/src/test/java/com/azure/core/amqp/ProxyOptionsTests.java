// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.InetSocketAddress;
import java.net.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProxyOptionsTests {
    @Test
    public void emptyConfiguration() {
        assertNull(ProxyOptions.fromConfiguration(new ConfigurationBuilder().build()));
    }

    @Test
    public void noneConfigurationReturnsNull() {
        assertNull(ProxyOptions.fromConfiguration(Configuration.NONE));
    }

    @Test
    public void noHostProperty() {
        Configuration configuration = new ConfigurationBuilder()
            .addProperty("amqp.proxy.authentication-type", "BASIC")
            .addProperty("amqp.proxy.type", "SOCKS")
            .addProperty("amqp.proxy.port", "123")
            .addProperty("amqp.proxy.username", "user")
            .addProperty("amqp.proxy.password", "password")
            .build();
        assertNull(ProxyOptions.fromConfiguration(configuration));
    }

    @Test
    public void noTypePropertyThrows() {
        Configuration configuration = new ConfigurationBuilder()
            .addProperty("amqp.proxy.hostname", "localhost")
            .addProperty("amqp.proxy.authentication-type", "BASIC")
            .addProperty("amqp.proxy.port", "123")
            .addProperty("amqp.proxy.username", "user")
            .addProperty("amqp.proxy.password", "password")
            .build();
        assertThrows(IllegalArgumentException.class, () -> ProxyOptions.fromConfiguration(configuration));
    }

    @Test
    public void validProperties() {
        Configuration configuration = new ConfigurationBuilder()
            .addProperty("amqp.proxy.hostname", "localhost")
            .addProperty("amqp.proxy.port", "123")
            .addProperty("amqp.proxy.type", "SOCKS")
            .addProperty("amqp.proxy.username", "user")
            .addProperty("amqp.proxy.password", "password")
            .build();

        ProxyOptions proxyOptions = ProxyOptions.fromConfiguration(configuration);
        assertNotNull(proxyOptions);

        assertInstanceOf(InetSocketAddress.class, proxyOptions.getProxyAddress().address());
        InetSocketAddress socketAddr = (InetSocketAddress) proxyOptions.getProxyAddress().address();

        assertFalse(socketAddr.isUnresolved());
        assertEquals(Proxy.Type.SOCKS, proxyOptions.getProxyAddress().type());
        assertEquals("localhost", socketAddr.getHostName());
        assertEquals(123, socketAddr.getPort());

        assertEquals(ProxyAuthenticationType.NONE, proxyOptions.getAuthentication());
        assertEquals("user", proxyOptions.getCredential().getUserName());
        assertEquals("password", new String(proxyOptions.getCredential().getPassword()));
    }

    @Test
    public void validGlobalAndLocalProperties() {
        Configuration configuration = new ConfigurationBuilder()
            .addProperty("amqp.proxy.hostname", "localhost")
            .addProperty("amqp.proxy.port", "123")
            .addProperty("amqp.proxy.type", "SOCKS")
            .addProperty("amqp.proxy.authentication-type", "DIGEST")
            .addProperty("foo.amqp.proxy.username", "user")
            .addProperty("foo.amqp.proxy.password", "password")
            .buildSection("foo");

        ProxyOptions proxyOptions = ProxyOptions.fromConfiguration(configuration);
        assertNotNull(proxyOptions);

        assertInstanceOf(InetSocketAddress.class, proxyOptions.getProxyAddress().address());
        InetSocketAddress socketAddr = (InetSocketAddress) proxyOptions.getProxyAddress().address();

        assertFalse(socketAddr.isUnresolved());
        assertEquals(Proxy.Type.SOCKS, proxyOptions.getProxyAddress().type());
        assertEquals("localhost", socketAddr.getHostName());
        assertEquals(123, socketAddr.getPort());

        assertEquals(ProxyAuthenticationType.DIGEST, proxyOptions.getAuthentication());
        assertEquals("user", proxyOptions.getCredential().getUserName());
        assertEquals("password", new String(proxyOptions.getCredential().getPassword()));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {""})
    public void portNullAndEmpty(String port) {
        ConfigurationBuilder configBuilder = new ConfigurationBuilder()
            .addProperty("amqp.proxy.hostname", "localhost")
            .addProperty("amqp.proxy.authentication-type", "BASIC")
            .addProperty("amqp.proxy.type", "SOCKS");

        if (port != null) {
            configBuilder.addProperty("amqp.proxy.port", port);
        }

        assertThrows(IllegalArgumentException.class, () -> ProxyOptions.fromConfiguration(configBuilder.build()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "not-an-int"})
    public void invalidPortThrows(String port) {
        Configuration configuration = new ConfigurationBuilder()
            .addProperty("amqp.proxy.hostname", "localhost")
            .addProperty("amqp.proxy.port", port)
            .addProperty("amqp.proxy.authentication-type", "BASIC")
            .addProperty("amqp.proxy.type", "SOCKS")
            .build();

        assertThrows(NumberFormatException.class, () -> ProxyOptions.fromConfiguration(configuration));
    }

    @ParameterizedTest
    @ValueSource(strings = {"NONE", "BASIC", "DIGEST"})
    public void validAuthenticationTypes(String authType) {
        Configuration configuration = new ConfigurationBuilder()
            .addProperty("amqp.proxy.hostname", "localhost")
            .addProperty("amqp.proxy.port", "123")
            .addProperty("amqp.proxy.authentication-type", authType)
            .addProperty("amqp.proxy.type", "SOCKS")
            .build();

        ProxyOptions proxyOptions = ProxyOptions.fromConfiguration(configuration);
        assertEquals(authType, proxyOptions.getAuthentication().toString());
    }

    @Test
    public void invalidAuthenticationType() {
        Configuration configuration = new ConfigurationBuilder()
            .addProperty("amqp.proxy.hostname", "localhost")
            .addProperty("amqp.proxy.port", "123")
            .addProperty("amqp.proxy.authentication-type", "foo")
            .addProperty("amqp.proxy.type", "SOCKS")
            .build();

        assertThrows(IllegalArgumentException.class, () -> ProxyOptions.fromConfiguration(configuration));
    }

    @ParameterizedTest
    @ValueSource(strings = {"HTTP", "SOCKS"})
    public void validProxyTypes(String proxyType) {
        Configuration configuration = new ConfigurationBuilder()
            .addProperty("amqp.proxy.hostname", "localhost")
            .addProperty("amqp.proxy.port", "443")
            .addProperty("amqp.proxy.type", proxyType)
            .build();

        ProxyOptions proxyOptions = ProxyOptions.fromConfiguration(configuration);
        assertEquals(proxyType, proxyOptions.getProxyAddress().type().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"DIRECT", "foo"})
    public void invalidProxyTypes(String proxyType) {
        Configuration configuration = new ConfigurationBuilder()
            .addProperty("amqp.proxy.hostname", "localhost")
            .addProperty("amqp.proxy.port", "443")
            .addProperty("amqp.proxy.type", proxyType)
            .build();

        // direct is valid Proxy.Type, but it's not possible to create a Proxy with DIRECT type
        assertThrows(IllegalArgumentException.class, () -> ProxyOptions.fromConfiguration(configuration));
    }
}
