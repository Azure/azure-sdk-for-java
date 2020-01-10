package com.azure.core.http.netty;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.netty.handler.proxy.HttpProxyHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.netty.tcp.ProxyProvider;
import reactor.netty.tcp.TcpClient;

import java.net.InetSocketAddress;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ReactorNettyProxyTests {
    private static final String PATH = "/test";
    private static final String EXPECTED_RESPONSE = "Hello tester!";

    private static final String PROXY_HOST = "https://localhost";
    private static final int PROXY_PORT = 443;
    private static final String PROXY_USERNAME = "username";
    private static final String PROXY_PASSWORD = "password";
    private static final String NON_PROXY_HOSTS = "https://microsoft.com*";
    private static final String AZURE_HTTPS_PROXY = "https://username:password@localhost";
    private static final String AZURE_HTTP_PROXY = "http://username:password@localhost";

    private static final String JAVA_PROXY_PREREQUISITE = "java.net.useSystemProxies";
    private static final String JAVA_NON_PROXY_HOSTS = "http.nonProxyHosts";

    private static final String JAVA_HTTPS_PROXY_HOST = "https.proxyHost";
    private static final String JAVA_HTTPS_PROXY_PORT = "https.proxyPost";
    private static final String JAVA_HTTPS_PROXY_USER = "https.proxyUser";
    private static final String JAVA_HTTPS_PROXY_PASSWORD = "https.proxyPassword";

    private static final String JAVA_HTTP_PROXY_HOST = "http.proxyHost";
    private static final String JAVA_HTTP_PROXY_PORT = "http.proxyPort";
    private static final String JAVA_HTTP_PROXY_USER = "http.proxyUser";
    private static final String JAVA_HTTP_PROXY_PASSWORD = "http.proxyPassword";

    private static WireMockServer server;

    @BeforeAll
    public static void setupServer() {
        WireMockConfiguration.options().port
        server = new WireMockServer(WireMockConfiguration.options().dynamicPort().disableRequestJournal());
        server.stubFor(WireMock.get(PATH).willReturn(WireMock.aResponse().withBody(EXPECTED_RESPONSE)));
        server.start();
    }

    @AfterAll
    public static void shutdownServer() {
        if (server != null) {
            server.shutdownServer();
        }
    }

    @Test
    public void proxyOptionsProxy() {
        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
            new InetSocketAddress(PROXY_HOST, PROXY_PORT));
        proxyOptions.setCredentials(PROXY_USERNAME, PROXY_PASSWORD);

        NettyAsyncHttpClient nettyClient = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder()
            .proxy(proxyOptions)
            .build();

        nettyClient.nettyClient.tcpConfiguration(tcpClient -> validateProxy(tcpClient, PROXY_HOST, PROXY_PORT,
            PROXY_USERNAME, PROXY_PASSWORD, null));

        nettyClient.send(new HttpRequest(HttpMethod.GET, getUrl(true))).block();
    }

    @Test
    public void azureHttpsProxy() {
        Configuration configuration = new Configuration()
            .put(Configuration.PROPERTY_HTTPS_PROXY, AZURE_HTTPS_PROXY);

        NettyAsyncHttpClient nettyClient = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder()
            .configuration(configuration)
            .build();

        nettyClient.nettyClient.tcpConfiguration(tcpClient -> validateProxy(tcpClient, PROXY_HOST, PROXY_PORT,
            PROXY_USERNAME, PROXY_PASSWORD, null));

        nettyClient.send(new HttpRequest(HttpMethod.GET, getUrl(true))).block();
    }

    @Test
    public void azureHttpProxy() {
        Configuration configuration = new Configuration()
            .put(Configuration.PROPERTY_HTTP_PROXY, AZURE_HTTP_PROXY);
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

    /*
     * Validates that the 'TcpClient' backing the 'HttpClient' has the expected proxy configuration.
     */
    private TcpClient validateProxy(TcpClient tcpClient, String host, int port, String username, String password,
        String nonProxyHosts) {
        ProxyProvider proxyProvider = tcpClient.proxyProvider();
        assertNotNull(proxyProvider);

        Pattern nonProxyHostsPattern = proxyProvider.getNonProxyHosts();
        if (nonProxyHosts == null) {
            assertNull(nonProxyHostsPattern);
        } else {
            assertEquals(nonProxyHosts, nonProxyHostsPattern.pattern());
        }

        InetSocketAddress socketAddress = proxyProvider.getAddress().get();
        assertEquals(host, socketAddress.getHostString());
        assertEquals(port, socketAddress.getPort());

        HttpProxyHandler proxyHandler = (HttpProxyHandler) proxyProvider.newProxyHandler();
        assertNotNull(proxyHandler);

        assertEquals(username, proxyHandler.username());
        assertEquals(password, proxyHandler.password());

        return tcpClient;
    }

    private String getUrl(boolean useHttps) {
        return useHttps
            ? String.format("https://localhost:%d%s", server.httpsPort(), PATH)
            : String.format("http://localhost:%d%s", server.port(), PATH);
    }
}
