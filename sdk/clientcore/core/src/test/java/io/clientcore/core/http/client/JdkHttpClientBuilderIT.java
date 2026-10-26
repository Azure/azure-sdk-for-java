// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.client;

import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.shared.LocalTestServer;
import io.clientcore.core.shared.TestConfigurationSource;
import io.clientcore.core.utils.configuration.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.Authenticator;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link JdkHttpClientBuilder}.
 * <p>
 * Now that the default HttpClient, and related code, are using multi-release JARs this must be an integration test as
 * the full JAR must be available to use the multi-release code.
 */
@DisabledForJreRange(max = JRE.JAVA_11)
public class JdkHttpClientBuilderIT {
    private static final String PROXY_USERNAME = "foo";
    private static final String PROXY_PASSWORD = "bar";
    private static final String PROXY_USER_INFO = PROXY_USERNAME + ":" + PROXY_PASSWORD + "@";
    private static final String SERVICE_ENDPOINT = "/default";

    private static LocalTestServer server;
    private static SimpleBasicAuthHttpProxyServer proxyServer;
    private static SimpleBasicAuthHttpProxyServer.ProxyEndpoint proxyEndpoint;

    @BeforeAll
    public static void startTestServer() {
        server = new LocalTestServer(HttpProtocolVersion.HTTP_1_1, false, (req, resp, requestBody) -> {
            if ("GET".equalsIgnoreCase(req.getMethod()) && SERVICE_ENDPOINT.equals(req.getServletPath())) {
                resp.setStatus(200);
            } else {
                throw new ServletException("Unexpected request: " + req.getMethod() + " " + req.getServletPath());
            }
        });

        server.start();

        proxyServer = new SimpleBasicAuthHttpProxyServer(PROXY_USERNAME, PROXY_PASSWORD, SERVICE_ENDPOINT);
        proxyEndpoint = proxyServer.start();
    }

    @AfterAll
    public static void stopTestServer() {
        if (server != null) {
            server.stop();
        }

        if (proxyServer != null) {
            proxyServer.shutdown();
        }
    }

    @Test
    public void buildWithHttpProxy() throws IOException {
        ProxyOptions clientProxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
            new InetSocketAddress(proxyEndpoint.getHost(), proxyEndpoint.getPort())).setCredentials(PROXY_USERNAME,
                PROXY_PASSWORD);

        HttpClient httpClient = new JdkHttpClientBuilder().proxy(clientProxyOptions).build();

        final String serviceUri = "http://localhost:80" + SERVICE_ENDPOINT;

        try (Response<?> response = httpClient.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(serviceUri))) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @ParameterizedTest
    @MethodSource("proxyAuthenticationRequests")
    public void proxyCredentialsAreScopedToProxy(InetSocketAddress proxyAddress, String requestingHost,
        int requestingPort, Authenticator.RequestorType requestorType, boolean authenticate)
        throws ReflectiveOperationException, IOException {
        HttpClient client = new JdkHttpClientBuilder().configuration(Configuration.none())
            .proxy(
                new ProxyOptions(ProxyOptions.Type.HTTP, proxyAddress).setCredentials(PROXY_USERNAME, PROXY_PASSWORD))
            .build();
        Optional<?> configuredAuthenticator = (Optional<?>) Class.forName("java.net.http.HttpClient")
            .getMethod("authenticator")
            .invoke(getNativeClient(client));
        Authenticator authenticator = (Authenticator) configuredAuthenticator.get();
        PasswordAuthentication authentication = (PasswordAuthentication) Authenticator.class
            .getMethod("requestPasswordAuthenticationInstance", String.class, InetAddress.class, int.class,
                String.class, String.class, String.class, URL.class, Authenticator.RequestorType.class)
            .invoke(authenticator, requestingHost, proxyAddress.getAddress(), requestingPort, "http", "test", "Basic",
                URI.create("http://service.example/").toURL(), requestorType);

        if (authenticate) {
            assertNotNull(authentication);
            assertEquals(PROXY_USERNAME, authentication.getUserName());
            assertArrayEquals(PROXY_PASSWORD.toCharArray(), authentication.getPassword());
        } else {
            assertNull(authentication);
        }
    }

    private static Stream<Arguments> proxyAuthenticationRequests() throws IOException {
        byte[] loopback = { 127, 0, 0, 1 };
        return Stream
            .of(InetSocketAddress.createUnresolved("proxy.example", 8888),
                new InetSocketAddress(InetAddress.getByAddress("proxy.example", loopback), 8888),
                new InetSocketAddress(InetAddress.getByAddress(loopback), 8888))
            .flatMap(address -> {
                String host = address.getHostString();
                return Stream.of(Arguments.of(address, host, 8888, Authenticator.RequestorType.PROXY, true),
                    Arguments.of(address, host.toUpperCase(Locale.ROOT), 8888, Authenticator.RequestorType.PROXY, true),
                    Arguments.of(address, host, 8889, Authenticator.RequestorType.PROXY, false),
                    Arguments.of(address, host, -1, Authenticator.RequestorType.PROXY, false),
                    Arguments.of(address, "other.example", 8888, Authenticator.RequestorType.PROXY, false),
                    Arguments.of(address, null, 8888, Authenticator.RequestorType.PROXY, false),
                    Arguments.of(address, host, 8888, Authenticator.RequestorType.SERVER, false));
            });
    }

    @Test
    public void serverChallengeCannotUseProxyCredentials() {
        AtomicInteger requests = new AtomicInteger();
        AtomicReference<String> authorization = new AtomicReference<>();
        LocalTestServer origin = new LocalTestServer(HttpProtocolVersion.HTTP_1_1, false, (request, response, body) -> {
            requests.incrementAndGet();
            String header = request.getHeader("Authorization");
            if (header != null) {
                authorization.set(header);
            }
            response.setStatus(header == null ? 401 : 200);
            response.setHeader("WWW-Authenticate", "Basic realm=\"origin\"");
            response.setContentLength(0);
            response.flushBuffer();
        });
        try {
            origin.start();
            ProxyOptions proxy = new ProxyOptions(ProxyOptions.Type.HTTP,
                new InetSocketAddress(proxyEndpoint.getHost(), proxyEndpoint.getPort()))
                    .setCredentials(PROXY_USERNAME, PROXY_PASSWORD)
                    .setNonProxyHosts("localhost");
            HttpClient client = new JdkHttpClientBuilder().configuration(Configuration.none())
                .proxy(proxy)
                .setMaximumHttpVersion(HttpProtocolVersion.HTTP_1_1)
                .build();
            CoreException error = assertThrows(CoreException.class, () -> {
                try (Response<?> response
                    = client.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(origin.getUri()))) {
                    assertNotNull(response);
                }
            });
            assertInstanceOf(IOException.class, error.getCause());
            assertEquals(1, requests.get());
            assertNull(authorization.get());
        } finally {
            origin.stop();
        }
    }

    @Test
    public void buildWithHttpProxyFromEnvConfiguration() throws IOException {
        Configuration configuration = Configuration.from(new TestConfigurationSource()
            .put(Configuration.HTTP_PROXY,
                "http://" + PROXY_USER_INFO + proxyEndpoint.getHost() + ":" + proxyEndpoint.getPort())
            .put(Configuration.HTTPS_PROXY, "")
            .put("java.net.useSystemProxies", "true"));

        HttpClient httpClient = new JdkHttpClientBuilder().configuration(configuration).build();

        final String serviceUri = "http://localhost:80" + SERVICE_ENDPOINT;

        try (Response<?> response = httpClient.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(serviceUri))) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @ParameterizedTest
    @ValueSource(longs = { -1, 0, 123000 })
    public void configuredTimeoutsAndExplicitOverrides(long timeoutMillis) throws ReflectiveOperationException {
        String value = Long.toString(timeoutMillis);
        Configuration configuration
            = Configuration.from(new TestConfigurationSource().put(Configuration.REQUEST_CONNECT_TIMEOUT_IN_MS, value)
                .put(Configuration.REQUEST_READ_TIMEOUT_IN_MS, value)
                .put(Configuration.REQUEST_WRITE_TIMEOUT_IN_MS, value)
                .put(Configuration.REQUEST_RESPONSE_TIMEOUT_IN_MS, value));
        JdkHttpClientBuilder builder = new JdkHttpClientBuilder().configuration(configuration);
        assertTimeouts(builder.build(), Math.max(0, timeoutMillis));
        assertTimeouts(builder.connectionTimeout(Duration.ofMillis(456000))
            .readTimeout(Duration.ofMillis(456000))
            .writeTimeout(Duration.ofMillis(456000))
            .responseTimeout(Duration.ofMillis(456000))
            .build(), 456000);
        assertTimeouts(builder.connectionTimeout(Duration.ofNanos(-1))
            .readTimeout(Duration.ZERO)
            .writeTimeout(Duration.ofNanos(-1))
            .responseTimeout(Duration.ZERO)
            .build(), 0);
    }

    private static void assertTimeouts(HttpClient client, long expected) throws ReflectiveOperationException {
        Object nativeClient = getNativeClient(client);
        Object connectTimeout
            = Class.forName("java.net.http.HttpClient").getMethod("connectTimeout").invoke(nativeClient);
        assertEquals(expected == 0 ? Optional.empty() : Optional.of(Duration.ofMillis(expected)), connectTimeout);
        for (String name : new String[] { "readTimeout", "writeTimeout", "responseTimeout" }) {
            java.lang.reflect.Field field = client.getClass().getDeclaredField(name);
            field.setAccessible(true);
            assertEquals(expected == 0 && !"readTimeout".equals(name) ? null : Duration.ofMillis(expected),
                field.get(client));
        }
    }

    private static Object getNativeClient(HttpClient client) throws ReflectiveOperationException {
        java.lang.reflect.Field nativeClientField = client.getClass().getDeclaredField("jdkHttpClient");
        nativeClientField.setAccessible(true);
        return nativeClientField.get(client);
    }

    @Test
    public void buildWithNullProxyAddress() {
        ProxyOptions mockPoxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, null);
        assertThrows(NullPointerException.class, () -> new JdkHttpClientBuilder().proxy(mockPoxyOptions));
    }

    @Test
    public void buildWithNullProxyType() {
        ProxyOptions mockPoxyOptions
            = new ProxyOptions(null, new InetSocketAddress(proxyEndpoint.getHost(), proxyEndpoint.getPort()));
        assertThrows(NullPointerException.class, () -> new JdkHttpClientBuilder().proxy(mockPoxyOptions));
    }

    @Test
    public void buildWithoutProxyAuthentication() throws IOException {
        ProxyOptions clientProxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
            new InetSocketAddress(proxyEndpoint.getHost(), proxyEndpoint.getPort()));

        HttpClient httpClient = new JdkHttpClientBuilder().proxy(clientProxyOptions).build();

        final String serviceUri = "http://localhost:80" + SERVICE_ENDPOINT;

        try (Response<?> response = httpClient.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(serviceUri))) {
            assertNotNull(response);
        }
    }
}
