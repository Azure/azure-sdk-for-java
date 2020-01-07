package com.azure.core.http.netty;

import com.azure.core.http.ProxyOptions;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReactorNettyProxyTests {
    private static final String PROXY_HOST = "https://localhost";
    private static final int PROXY_PORT = 443;
    private static final String PROXY_USERNAME = "username";
    private static final String PROXY_PASSWORD = "password";
    private static final String NON_PROXY_HOSTS = "https://microsoft.com*";

    @Test
    public void proxyOptionsProxy() {
        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
            new InetSocketAddress(PROXY_HOST, PROXY_PORT));
        proxyOptions.setCredentials(PROXY_USERNAME, PROXY_PASSWORD);

        NettyAsyncHttpClient nettyClient = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder()
            .proxy(proxyOptions)
            .build();

        nettyClient.nettyClient.tcpConfiguration(tcpClient -> {
            assertTrue(tcpClient.hasProxy());

            InetSocketAddress proxyAddress = tcpClient.proxyProvider().getAddress().get();
            assertEquals(PROXY_HOST, proxyAddress.getHostString());
            assertEquals(PROXY_PORT, proxyAddress.getPort());

            return tcpClient;
        });
    }

    @Test
    public void azureHttpsProxy() {

    }

    @Test
    public void azureHttpProxy() {

    }

    @Test
    public void javaHttpsProxy() {

    }

    @Test
    public void javaHttpProxy() {

    }

    @Test
    public void preferProxyOptions() {

    }

    @Test
    public void preferAzureProxy() {

    }

    @Test
    public void preferHttpsProxy() {

    }

    @Test
    public void ignoreDisabledJavaProxies() {

    }
}
