// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.okhttp3;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.shared.LocalTestServer;
import io.clientcore.core.shared.TestConfigurationSource;
import io.clientcore.core.util.configuration.Configuration;
import io.clientcore.core.util.configuration.ConfigurationBuilder;
import io.clientcore.core.util.configuration.ConfigurationSource;
import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.EventListener;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link OkHttpHttpClientBuilder}.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class OkHttpHttpClientBuilderTests {
    private static final String COOKIE_VALIDATOR_PATH = "/cookieValidator";
    private static final String DEFAULT_PATH = "/default";
    private static final String DISPATCHER_PATH = "/dispatcher";
    private static final String REDIRECT_PATH = "/redirect";
    private static final String LOCATION_PATH = "/location";
    private static final String JAVA_SYSTEM_PROXY_PREREQUISITE = "java.net.useSystemProxies";
    private static final String JAVA_NON_PROXY_HOSTS = "http.nonProxyHosts";
    private static final String JAVA_HTTP_PROXY_HOST = "http.proxyHost";
    private static final String JAVA_HTTP_PROXY_PORT = "http.proxyPort";
    private static final String JAVA_HTTP_PROXY_USER = "http.proxyUser";
    private static final String JAVA_HTTP_PROXY_PASSWORD = "http.proxyPassword";
    private static final ConfigurationSource EMPTY_SOURCE = new TestConfigurationSource();

    private static LocalTestServer server;
    private static String cookieValidatorUri;
    private static String defaultUri;
    private static String dispatcherUri;
    private static String locationUri;
    private static String redirectUri;

    @BeforeAll
    public static void startTestServer() {
        server = new LocalTestServer((req, resp, requestBody) -> {
            String path = req.getServletPath();
            boolean get = "GET".equalsIgnoreCase(req.getMethod());

            if (get && COOKIE_VALIDATOR_PATH.equals(path)) {
                boolean hasCookie = req.getCookies() != null && Arrays.stream(req.getCookies())
                    .anyMatch(cookie -> "test".equals(cookie.getName()) && "success".equals(cookie.getValue()));

                if (!hasCookie) {
                    resp.setStatus(400);
                }
            } else if (get && DISPATCHER_PATH.equals(path)) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else if (get && REDIRECT_PATH.equals(path)) {
                resp.setStatus(307);
                resp.setHeader("Location", locationUri);
            } else if (get && (DEFAULT_PATH.equals(path) || LOCATION_PATH.equals(path))) {
                resp.setStatus(200);
            } else {
                throw new ServletException("Unexpected request: " + req.getMethod() + " " + path);
            }
        });

        server.start();

        cookieValidatorUri = server.getHttpUri() + COOKIE_VALIDATOR_PATH;
        defaultUri = server.getHttpUri() + DEFAULT_PATH;
        dispatcherUri = server.getHttpUri() + DISPATCHER_PATH;
        redirectUri = server.getHttpUri() + REDIRECT_PATH;
        locationUri = server.getHttpUri() + LOCATION_PATH;
    }

    @AfterAll
    public static void stopTestServer() {
        if (server != null) {
            server.stop();
        }
    }

    /**
     * Tests that an {@link OkHttpHttpClient} is able to be built from an existing {@link OkHttpClient}.
     */
    @Test
    public void buildClientWithExistingClient() throws IOException {
        OkHttpClient existingClient = new OkHttpClient.Builder()
            .addInterceptor(chain -> chain
                .proceed(chain.request().newBuilder().addHeader("Cookie", "test=success").build()))
            .build();
        HttpClient client = new OkHttpHttpClientBuilder(existingClient).build();

        try (Response<?> response = client.send(new HttpRequest(HttpMethod.GET, cookieValidatorUri))) {
            assertEquals(200, response.getStatusCode());
        }
    }

    /**
     * Tests that instantiating an {@link OkHttpHttpClientBuilder} with a {@code null} {@link OkHttpClient} will
     * throw a {@link NullPointerException}.
     */
    @Test
    public void startingWithNullClientThrows() {
        assertThrows(NullPointerException.class, () -> new OkHttpHttpClientBuilder(null));
    }

    /**
     * Tests that adding an {@link Interceptor} is handled correctly.
     */
    @Test
    public void addNetworkInterceptor() throws IOException {
        Interceptor testInterceptor = chain -> chain.proceed(chain.request().newBuilder()
            .addHeader("Cookie", "test=success").build());
        HttpClient client = new OkHttpHttpClientBuilder()
            .addNetworkInterceptor(testInterceptor)
            .build();

        try (Response<?> response = client.send(new HttpRequest(HttpMethod.GET, cookieValidatorUri))) {
            assertEquals(200, response.getStatusCode());
        }
    }

    /**
     * Tests that adding a {@code null} {@link Interceptor} will throw a {@link NullPointerException}.
     */
    @Test
    public void nullNetworkInterceptorThrows() {
        assertThrows(NullPointerException.class, () -> new OkHttpHttpClientBuilder().addNetworkInterceptor(null));
    }

    /**
     * Tests that the {@link Interceptor interceptors} in the client are replace-able by setting a new list of
     * interceptors.
     */
    @Test
    public void setNetworkInterceptors() throws IOException {
        Interceptor badCookieSetter = chain -> chain.proceed(chain.request().newBuilder()
            .addHeader("Cookie", "test=failure").build());
        Interceptor goodCookieSetter = chain -> chain.proceed(chain.request().newBuilder()
            .addHeader("Cookie", "test=success").build());
        HttpClient client = new OkHttpHttpClientBuilder()
            .addNetworkInterceptor(badCookieSetter)
            .networkInterceptors(Collections.singletonList(goodCookieSetter))
            .build();

        try (Response<?> response = client.send(new HttpRequest(HttpMethod.GET, cookieValidatorUri))) {
            assertEquals(200, response.getStatusCode());
        }
    }

    /**
     * Tests that setting the {@link Interceptor interceptors} to {@code null} will throw a
     * {@link NullPointerException}.
     */
    @Test
    public void nullNetworkInterceptorsThrows() {
        assertThrows(NullPointerException.class, () -> new OkHttpHttpClientBuilder().networkInterceptors(null));
    }

    /**
     * Tests building a client with a given {@code connectionTimeout}.
     */
    @Test
    public void buildWithConnectionTimeout() throws IOException {
        int expectedConnectionTimeoutMillis = 3600 * 1000;
        Interceptor validatorInterceptor = chain -> {
            assertEquals(expectedConnectionTimeoutMillis, chain.connectTimeoutMillis());

            return chain.proceed(chain.request());
        };
        HttpClient client = new OkHttpHttpClientBuilder()
            .addNetworkInterceptor(validatorInterceptor)
            .connectionTimeout(Duration.ofSeconds(3600))
            .build();

        try (Response<?> response = client.send(new HttpRequest(HttpMethod.GET, defaultUri))) {
            assertEquals(200, response.getStatusCode());
        }
    }


    @Test
    public void buildWithFollowRedirectSetToTrue() throws IOException {
        HttpClient client = new OkHttpHttpClientBuilder()
            .followRedirects(true)
            .build();

        try (Response<?> response = client.send(new HttpRequest(HttpMethod.GET, redirectUri))) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void buildWithFollowRedirectSetToFalse() throws IOException {
        HttpClient client = new OkHttpHttpClientBuilder()
            .followRedirects(false)
            .build();

        try (Response<?> response = client.send(new HttpRequest(HttpMethod.GET, redirectUri))) {
            assertEquals(307, response.getStatusCode());
        }
    }

    @Test
    public void buildWithFollowRedirectDefault() throws IOException {
        HttpClient client = new OkHttpHttpClientBuilder().build();

        try (Response<?> response = client.send(new HttpRequest(HttpMethod.GET, redirectUri))) {
            assertEquals(307, response.getStatusCode());
        }
    }

    /**
     * Tests building a client with a given {@code connectionTimeout}.
     */
    @Test
    public void buildWithReadTimeout() throws IOException {
        int expectedReadTimeoutMillis = 3600 * 1000;
        Interceptor validatorInterceptor = chain -> {
            assertEquals(expectedReadTimeoutMillis, chain.readTimeoutMillis());

            return chain.proceed(chain.request());
        };
        HttpClient client = new OkHttpHttpClientBuilder()
            .addNetworkInterceptor(validatorInterceptor)
            .readTimeout(Duration.ofSeconds(3600))
            .build();

        try (Response<?> response = client.send(new HttpRequest(HttpMethod.GET, defaultUri))) {
            assertEquals(200, response.getStatusCode());
        }
    }

    /**
     * Tests building a client with a given {@code callTimeout}.
     */
    @Test
    public void buildWithCallTimeout() throws IOException {
        long expectedCallTimeoutNanos = 3600000000000L;
        Interceptor validatorInterceptor = chain -> {
            assertEquals(expectedCallTimeoutNanos, chain.call().timeout().timeoutNanos());
            return chain.proceed(chain.request());
        };
        HttpClient client = new OkHttpHttpClientBuilder()
            .addNetworkInterceptor(validatorInterceptor)
            .callTimeout(Duration.ofSeconds(3600))
            .build();

        try (Response<?> response = client.send(new HttpRequest(HttpMethod.GET, defaultUri))) {
            assertEquals(200, response.getStatusCode());
        }
    }

    /**
     * Tests building a client with negative callTimeout.
     */
    @Test
    public void throwsWithNegativeCallTimeout() {
        assertThrows(IllegalArgumentException.class, () ->
            new OkHttpHttpClientBuilder()
                .callTimeout(Duration.ofSeconds(-1)));
    }

    /**
     * Tests building a client with default timeouts.
     */
    @Test
    public void buildWithDefaultTimeouts() throws IOException {
        Interceptor validatorInterceptor = chain -> {
            assertEquals(0L, chain.call().timeout().timeoutNanos());
            assertEquals(60000, chain.readTimeoutMillis());
            assertEquals(60000, chain.writeTimeoutMillis());
            assertEquals(10000, chain.connectTimeoutMillis());
            return chain.proceed(chain.request());
        };
        HttpClient client = new OkHttpHttpClientBuilder()
            .addNetworkInterceptor(validatorInterceptor)
            .build();

        try (Response<?> response = client.send(new HttpRequest(HttpMethod.GET, defaultUri))) {
            assertEquals(200, response.getStatusCode());
        }
    }

    /**
     * Tests building a client with a given {@code connectionPool}.
     */
    @Test
    public void buildWithConnectionPool() throws IOException {
        ConnectionPool connectionPool = new ConnectionPool();
        HttpClient client = new OkHttpHttpClientBuilder()
            .connectionPool(connectionPool)
            .build();

        try (Response<?> response = client.send(new HttpRequest(HttpMethod.GET, defaultUri))) {
            assertEquals(200, response.getStatusCode());
            assertEquals(1, connectionPool.connectionCount());
        }
    }

    /**
     * Tests that passing a {@code null} {@code connectionPool} to the builder will throw a
     * {@link NullPointerException}.
     */
    @Test
    public void nullConnectionPoolThrows() {
        assertThrows(NullPointerException.class, () -> new OkHttpHttpClientBuilder().connectionPool(null));
    }

    /**
     * Tests building a client with a given {@code dispatcher}.
     */
    @Test
    public void buildWithDispatcher() {
        String expectedThreadName = "testDispatcher";
        Dispatcher dispatcher = new Dispatcher(Executors
            .newFixedThreadPool(1, (Runnable r) -> new Thread(r, expectedThreadName)));
        HttpClient client = new OkHttpHttpClientBuilder()
            .dispatcher(dispatcher)
            .build();

        /*
         * Schedule a task that will run in one second to cancel all requests sent using the dispatcher. This should
         * result in the request we are about to send to be cancelled since the server will wait 5 seconds before
         * returning a response.
         */
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                assertEquals(1, dispatcher.runningCallsCount());
                dispatcher.cancelAll();
            }
        }, 1000);

        assertThrows(IOException.class, () -> client.send(new HttpRequest(HttpMethod.GET, dispatcherUri)).close());
    }

    /**
     * Tests that passing a {@code null} {@code dispatcher} to the builder will throw a {@link NullPointerException}.
     */
    @Test
    public void nullDispatcherThrows() {
        assertThrows(NullPointerException.class, () -> new OkHttpHttpClientBuilder().dispatcher(null));
    }

    /**
     * Tests that building a client with a proxy will send the request through the proxy server.
     */
    @ParameterizedTest
    @MethodSource("buildWithProxySupplier")
    public void buildWithProxy(boolean shouldHaveProxy, Proxy.Type proxyType, ProxyOptions proxyOptions,
                               String requestUri) {
        OkHttpClient validatorClient = okHttpClientWithProxyValidation(shouldHaveProxy, proxyType);
        HttpClient client = new OkHttpHttpClientBuilder(validatorClient)
            .proxy(proxyOptions)
            .build();

        assertThrows(RuntimeException.class, () -> client.send(new HttpRequest(HttpMethod.GET, requestUri)).close(),
            TestEventListenerValidator.EXPECTED_EXCEPTION_MESSAGE);
    }

    private static Stream<Arguments> buildWithProxySupplier() {
        InetSocketAddress proxyAddress = new InetSocketAddress("localhost", 12345);

        ProxyOptions socks4Proxy = new ProxyOptions(ProxyOptions.Type.SOCKS4, proxyAddress);
        ProxyOptions socks5Proxy = new ProxyOptions(ProxyOptions.Type.SOCKS5, proxyAddress);
        ProxyOptions simpleHttpProxy = new ProxyOptions(ProxyOptions.Type.HTTP, proxyAddress);

        List<Arguments> arguments = new ArrayList<>();

        /*
         * Simple non-authenticated proxies without non-proxy hosts configured.
         */
        arguments.add(Arguments.of(true, Proxy.Type.SOCKS, socks4Proxy, defaultUri));
        arguments.add(Arguments.of(true, Proxy.Type.SOCKS, socks5Proxy, defaultUri));
        arguments.add(Arguments.of(true, Proxy.Type.HTTP, simpleHttpProxy, defaultUri));

        /*
         * HTTP proxy with authentication configured.
         */
        ProxyOptions authenticatedHttpProxy = new ProxyOptions(ProxyOptions.Type.HTTP, proxyAddress)
            .setCredentials("1", "1");

        arguments.add(Arguments.of(true, Proxy.Type.HTTP, authenticatedHttpProxy, defaultUri));

        /*
         * Information for non-proxy hosts testing.
         */
        String rawNonProxyHosts = String.join("|", "localhost", "127.0.0.1", "*.microsoft.com", "*.linkedin.com");

        String[] requestUrisWithoutProxying = new String[]{
            "http://localhost", "http://127.0.0.1", "http://azure.microsoft.com", "http://careers.linkedin.com"
        };

        String[] requestUrisWithProxying = new String[]{
            "http://example.com", "http://portal.azure.com", "http://linkedin.com", "http://8.8.8.8"
        };

        /*
         * HTTP proxies with non-proxy hosts configured.
         */
        Supplier<ProxyOptions> nonProxyHostsSupplier = () ->
            new ProxyOptions(ProxyOptions.Type.HTTP, proxyAddress).setNonProxyHosts(rawNonProxyHosts);

        for (String requestUri : requestUrisWithoutProxying) {
            arguments.add(Arguments.of(false, Proxy.Type.HTTP, nonProxyHostsSupplier.get(), requestUri));
        }

        for (String requestUri : requestUrisWithProxying) {
            arguments.add(Arguments.of(true, Proxy.Type.HTTP, nonProxyHostsSupplier.get(), requestUri));
        }

        /*
         * HTTP proxies with authentication and non-proxy hosts configured.
         */
        Supplier<ProxyOptions> authenticatedNonProxyHostsSupplier = () -> nonProxyHostsSupplier.get()
            .setCredentials("1", "1");

        for (String requestUri : requestUrisWithoutProxying) {
            arguments.add(Arguments.of(false, Proxy.Type.HTTP, authenticatedNonProxyHostsSupplier.get(), requestUri));
        }

        for (String requestUri : requestUrisWithProxying) {
            arguments.add(Arguments.of(true, Proxy.Type.HTTP, authenticatedNonProxyHostsSupplier.get(), requestUri));
        }

        return arguments.stream();
    }

    @ParameterizedTest
    @MethodSource("buildWithEnvConfigurationProxySupplier")
    public void buildWithEnvConfigurationProxy(boolean shouldHaveProxy, Configuration configuration, String requestUri) {
        OkHttpClient validatorClient = okHttpClientWithProxyValidation(shouldHaveProxy, Proxy.Type.HTTP);
        HttpClient client = new OkHttpHttpClientBuilder(validatorClient)
            .configuration(configuration)
            .build();

        assertThrows(Throwable.class, () -> client.send(new HttpRequest(HttpMethod.GET, requestUri)).close(),
            TestEventListenerValidator.EXPECTED_EXCEPTION_MESSAGE);
    }

    @ParameterizedTest
    @MethodSource("buildWithExplicitConfigurationProxySupplier")
    public void buildWithExplicitConfigurationProxySupplier(boolean shouldHaveProxy, Configuration configuration, String requestUri) {
        OkHttpClient validatorClient = okHttpClientWithProxyValidation(shouldHaveProxy, Proxy.Type.HTTP);

        HttpClient client = new OkHttpHttpClientBuilder(validatorClient)
            .configuration(configuration)
            .build();

        assertThrows(Throwable.class, () -> client.send(new HttpRequest(HttpMethod.GET, requestUri)).close(),
            TestEventListenerValidator.EXPECTED_EXCEPTION_MESSAGE);
    }

    private static Stream<Arguments> buildWithEnvConfigurationProxySupplier() {
        Supplier<TestConfigurationSource> baseJavaProxyConfigurationSupplier = () -> new TestConfigurationSource()
            .put(JAVA_HTTP_PROXY_HOST, "localhost")
            .put(JAVA_HTTP_PROXY_PORT, "12345");
        List<Arguments> arguments = new ArrayList<>();

        /*
         * Simple non-authenticated HTTP proxies.
         */
        arguments.add(Arguments.of(true, new ConfigurationBuilder(EMPTY_SOURCE, baseJavaProxyConfigurationSupplier.get(), EMPTY_SOURCE).build(),
            defaultUri));

        Configuration simpleEnvProxy = new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE, new TestConfigurationSource()
            .put(Configuration.PROPERTY_HTTP_PROXY, "http://localhost:12345")
            .put(JAVA_SYSTEM_PROXY_PREREQUISITE, "true"))
            .build();

        arguments.add(Arguments.of(true, simpleEnvProxy, defaultUri));

        /*
         * HTTP proxy with authentication configured.
         */
        Configuration javaProxyWithAuthentication = new ConfigurationBuilder(EMPTY_SOURCE, baseJavaProxyConfigurationSupplier.get()
            .put(JAVA_HTTP_PROXY_USER, "1")
            .put(JAVA_HTTP_PROXY_PASSWORD, "1"),
            EMPTY_SOURCE)
            .build();

        arguments.add(Arguments.of(true, javaProxyWithAuthentication, defaultUri));

        Configuration envProxyWithAuthentication = new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE, new TestConfigurationSource()
            .put(Configuration.PROPERTY_HTTP_PROXY, "http://1:1@localhost:12345")
            .put(JAVA_SYSTEM_PROXY_PREREQUISITE, "true"))
            .build();

        arguments.add(Arguments.of(true, envProxyWithAuthentication, defaultUri));

        /*
         * Information for non-proxy hosts testing.
         */
        String rawJavaNonProxyHosts = String.join("|", "localhost", "127.0.0.1", "*.microsoft.com", "*.linkedin.com");
        String rawEnvNonProxyHosts = String.join(",", "localhost", "127.0.0.1", "*.microsoft.com", "*.linkedin.com");
        String[] requestUrisWithoutProxying = new String[]{
            "http://localhost", "http://127.0.0.1", "http://azure.microsoft.com", "http://careers.linkedin.com"
        };
        String[] requestUrisWithProxying = new String[]{
            "http://example.com", "http://portal.azure.com", "http://linkedin.com", "http://8.8.8.8"
        };

        /*
         * HTTP proxies with non-proxy hosts configured.
         */
        Supplier<TestConfigurationSource> javaNonProxyHostsSupplier = () -> baseJavaProxyConfigurationSupplier.get()
            .put(JAVA_NON_PROXY_HOSTS, rawJavaNonProxyHosts);
        Supplier<TestConfigurationSource> envNonProxyHostsSupplier = () -> new TestConfigurationSource()
            .put(Configuration.PROPERTY_HTTP_PROXY, "http://localhost:12345")
            .put(Configuration.PROPERTY_NO_PROXY, rawEnvNonProxyHosts)
            .put(JAVA_SYSTEM_PROXY_PREREQUISITE, "true");
        List<Supplier<TestConfigurationSource>> nonProxyHostsSuppliers = Arrays.asList(javaNonProxyHostsSupplier,
            envNonProxyHostsSupplier);

        for (Supplier<TestConfigurationSource> configurationSupplier : nonProxyHostsSuppliers) {
            for (String requestUri : requestUrisWithoutProxying) {
                arguments.add(Arguments.of(false, new ConfigurationBuilder(EMPTY_SOURCE, configurationSupplier.get(), EMPTY_SOURCE).build(), requestUri));
            }

            for (String requestUri : requestUrisWithProxying) {
                arguments.add(Arguments.of(true, new ConfigurationBuilder(EMPTY_SOURCE, configurationSupplier.get(), EMPTY_SOURCE).build(), requestUri));
            }
        }

        /*
         * HTTP proxies with authentication and non-proxy hosts configured.
         */
        Supplier<TestConfigurationSource> authenticatedJavaNonProxyHostsSupplier = () -> javaNonProxyHostsSupplier.get()
            .put(JAVA_HTTP_PROXY_USER, "1")
            .put(JAVA_HTTP_PROXY_PASSWORD, "1");
        Supplier<TestConfigurationSource> authenticatedEnvNonProxyHostsSupplier = () -> new TestConfigurationSource()
            .put(Configuration.PROPERTY_HTTP_PROXY, "http://1:1@localhost:12345")
            .put(Configuration.PROPERTY_NO_PROXY, rawEnvNonProxyHosts)
            .put(JAVA_SYSTEM_PROXY_PREREQUISITE, "true");
        List<Supplier<TestConfigurationSource>> authenticatedNonProxyHostsSuppliers = Arrays.asList(
            authenticatedJavaNonProxyHostsSupplier, authenticatedEnvNonProxyHostsSupplier);

        for (Supplier<TestConfigurationSource> configurationSupplier : authenticatedNonProxyHostsSuppliers) {
            for (String requestUri : requestUrisWithoutProxying) {
                arguments.add(Arguments.of(false, new ConfigurationBuilder(EMPTY_SOURCE, configurationSupplier.get(), EMPTY_SOURCE).build(), requestUri));
            }

            for (String requestUri : requestUrisWithProxying) {
                arguments.add(Arguments.of(true, new ConfigurationBuilder(EMPTY_SOURCE, configurationSupplier.get(), EMPTY_SOURCE).build(), requestUri));
            }
        }

        return arguments.stream();
    }

    private static Stream<Arguments> buildWithExplicitConfigurationProxySupplier() {
        Supplier<ConfigurationBuilder> baseHttpProxy = () -> new ConfigurationBuilder()
            .putProperty("http.proxy.hostname", "localhost")
            .putProperty("http.proxy.port", "12345");

        List<Arguments> arguments = new ArrayList<>();

        /*
         * Simple non-authenticated HTTP proxies.
         */
        arguments.add(Arguments.of(true, baseHttpProxy.get().build(), defaultUri));

        /*
         * HTTP proxy with authentication configured.
         */
        Configuration httpProxyWithAuthentication = baseHttpProxy.get()
            .putProperty("http.proxy.username", "1")
            .putProperty("http.proxy.password", "1")
            .build();

        arguments.add(Arguments.of(true, httpProxyWithAuthentication, defaultUri));

        /*
         * Information for non-proxy hosts testing.
         */
        String rawJavaNonProxyHosts = String.join("|", "localhost", "127.0.0.1", "*.microsoft.com", "*.linkedin.com");
        String rawEnvNonProxyHosts = String.join(",", "localhost", "127.0.0.1", "*.microsoft.com", "*.linkedin.com");
        String[] requestUrisWithoutProxying = new String[]{
            "http://localhost", "http://127.0.0.1", "http://azure.microsoft.com", "http://careers.linkedin.com"
        };
        String[] requestUrisWithProxying = new String[]{
            "http://portal.azure.com", "http://linkedin.com", "http://8.8.8.8"
        };

        /*
         * HTTP proxies with non-proxy hosts configured.
         */
        Supplier<ConfigurationBuilder> javaNonProxyHostsSupplier = () -> baseHttpProxy.get()
            .putProperty("http.proxy.non-proxy-hosts", rawJavaNonProxyHosts);
        for (String requestUri : requestUrisWithoutProxying) {
            arguments.add(Arguments.of(false, javaNonProxyHostsSupplier.get().build(), requestUri));
        }

        for (String requestUri : requestUrisWithProxying) {
            arguments.add(Arguments.of(true, javaNonProxyHostsSupplier.get().build(), requestUri));
        }

        /*
         * HTTP proxies with authentication and non-proxy hosts configured.
         */
        Supplier<ConfigurationBuilder> authenticatedJavaNonProxyHostsSupplier = () -> javaNonProxyHostsSupplier.get()
            .putProperty("http.proxy.username", "1")
            .putProperty("http.proxy.password", "1");

        for (String requestUri : requestUrisWithoutProxying) {
            arguments.add(Arguments.of(false, authenticatedJavaNonProxyHostsSupplier.get().build(), requestUri));
        }

        for (String requestUri : requestUrisWithProxying) {
            arguments.add(Arguments.of(true, authenticatedJavaNonProxyHostsSupplier.get().build(), requestUri));
        }

        return arguments.stream();
    }

    private static OkHttpClient okHttpClientWithProxyValidation(boolean shouldHaveProxy, Proxy.Type proxyType) {
        return new OkHttpClient.Builder()
            .eventListener(new TestEventListenerValidator(shouldHaveProxy, proxyType))
            // Use a custom Dispatcher and ExecutorService which overrides the uncaught exception handler.
            // This is done to prevent the tests using this from printing their error stack trace.
            // The reason this happens is the test throws an exception which goes uncaught in a thread, and this is an
            // expected exception, which results in the exception and its stack trace being logged, which is very
            // verbose.
            .dispatcher(new Dispatcher(Executors.newFixedThreadPool(2, r -> {
                Thread thread = new Thread(r);
                thread.setUncaughtExceptionHandler((t, e) -> {
                });

                return thread;
            })))
            .build();
    }

    private static final class TestEventListenerValidator extends EventListener {
        private static final String EXPECTED_EXCEPTION_MESSAGE = "This is a local test so we cannot connect to remote "
            + "hosts eagerly. This is exception is expected.";

        private static final RuntimeException EXPECTED_EXCEPTION = new RuntimeException(EXPECTED_EXCEPTION_MESSAGE);

        private final boolean shouldHaveProxy;
        private final Proxy.Type proxyType;

        private TestEventListenerValidator(boolean shouldHaveProxy, Proxy.Type proxyType) {
            this.shouldHaveProxy = shouldHaveProxy;
            this.proxyType = proxyType;
        }

        @Override
        public void connectStart(Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {
            RuntimeException exception = EXPECTED_EXCEPTION;

            try {
                if (shouldHaveProxy) {
                    assertNotNull(proxy.address());
                    assertEquals(proxyType, proxy.type());
                } else {
                    assertEquals(Proxy.NO_PROXY, proxy);
                }
            } catch (Throwable throwable) {
                exception = new RuntimeException(throwable);
            }

            throw exception;
        }
    }
}
