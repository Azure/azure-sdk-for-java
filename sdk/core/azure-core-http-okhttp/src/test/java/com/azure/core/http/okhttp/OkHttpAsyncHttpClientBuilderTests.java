// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.EventListener;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

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
 * Tests {@link OkHttpAsyncHttpClientBuilder}.
 */
public class OkHttpAsyncHttpClientBuilderTests {
    private static final String COOKIE_VALIDATOR_PATH = "/cookieValidator";
    private static final String DEFAULT_PATH = "/default";
    private static final String DISPATCHER_PATH = "/dispatcher";

    private static final String JAVA_SYSTEM_PROXY_PREREQUISITE = "java.net.useSystemProxies";
    private static final String JAVA_NON_PROXY_HOSTS = "http.nonProxyHosts";

    private static final String JAVA_HTTP_PROXY_HOST = "http.proxyHost";
    private static final String JAVA_HTTP_PROXY_PORT = "http.proxyPort";
    private static final String JAVA_HTTP_PROXY_USER = "http.proxyUser";
    private static final String JAVA_HTTP_PROXY_PASSWORD = "http.proxyPassword";

    private static WireMockServer server;

    private static String cookieValidatorUrl;
    private static String defaultUrl;
    private static String dispatcherUrl;

    @BeforeAll
    public static void setupWireMock() {
        server = new WireMockServer(WireMockConfiguration.options().dynamicPort().disableRequestJournal());

        // Mocked endpoint to test building a client with a prebuilt OkHttpClient.
        server.stubFor(WireMock.get(COOKIE_VALIDATOR_PATH).withCookie("test", WireMock.matching("success"))
            .willReturn(WireMock.aResponse().withStatus(200)));

        // Mocked endpoint to test building a client with a timeout.
        server.stubFor(WireMock.get(DEFAULT_PATH).willReturn(WireMock.aResponse().withStatus(200)));

        // Mocked endpoint to test building a client with a dispatcher and uses a delayed response.
        server.stubFor(WireMock.get(DISPATCHER_PATH).willReturn(WireMock.aResponse().withStatus(200)
            .withFixedDelay(5000)));

        server.start();

        cookieValidatorUrl = "http://localhost:" + server.port() + COOKIE_VALIDATOR_PATH;
        defaultUrl = "http://localhost:" + server.port() + DEFAULT_PATH;
        dispatcherUrl = "http://localhost:" + server.port() + DISPATCHER_PATH;
    }

    @AfterAll
    public static void shutdownWireMock() {
        if (server.isRunning()) {
            server.shutdown();
        }
    }

    /**
     * Tests that an {@link OkHttpAsyncHttpClient} is able to be built from an existing {@link OkHttpClient}.
     */
    @Test
    public void buildClientWithExistingClient() {
        OkHttpClient existingClient = new OkHttpClient.Builder()
            .addInterceptor(chain -> chain
                .proceed(chain.request().newBuilder().addHeader("Cookie", "test=success").build()))
            .build();

        HttpClient client = new OkHttpAsyncHttpClientBuilder(existingClient).build();

        StepVerifier.create(client.send(new HttpRequest(HttpMethod.GET, cookieValidatorUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that instantiating an {@link OkHttpAsyncHttpClientBuilder} with a {@code null} {@link OkHttpClient} will
     * throw a {@link NullPointerException}.
     */
    @Test
    public void startingWithNullClientThrows() {
        assertThrows(NullPointerException.class, () -> new OkHttpAsyncHttpClientBuilder(null));
    }

    /**
     * Tests that adding an {@link Interceptor} is handled correctly.
     */
    @Test
    public void addNetworkInterceptor() {
        Interceptor testInterceptor = chain -> chain.proceed(chain.request().newBuilder()
            .addHeader("Cookie", "test=success").build());

        HttpClient client = new OkHttpAsyncHttpClientBuilder()
            .addNetworkInterceptor(testInterceptor)
            .build();

        StepVerifier.create(client.send(new HttpRequest(HttpMethod.GET, cookieValidatorUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that adding a {@code null} {@link Interceptor} will throw a {@link NullPointerException}.
     */
    @Test
    public void nullNetworkInterceptorThrows() {
        assertThrows(NullPointerException.class, () -> new OkHttpAsyncHttpClientBuilder().addNetworkInterceptor(null));
    }

    /**
     * Tests that the {@link Interceptor interceptors} in the client are replace-able by setting a new list of
     * interceptors.
     */
    @Test
    public void setNetworkInterceptors() {
        Interceptor badCookieSetter = chain -> chain.proceed(chain.request().newBuilder()
            .addHeader("Cookie", "test=failure").build());
        Interceptor goodCookieSetter = chain -> chain.proceed(chain.request().newBuilder()
            .addHeader("Cookie", "test=success").build());

        HttpClient client = new OkHttpAsyncHttpClientBuilder()
            .addNetworkInterceptor(badCookieSetter)
            .networkInterceptors(Collections.singletonList(goodCookieSetter))
            .build();

        StepVerifier.create(client.send(new HttpRequest(HttpMethod.GET, cookieValidatorUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that setting the {@link Interceptor interceptors} to {@code null} will throw a {@link
     * NullPointerException}.
     */
    @Test
    public void nullNetworkInterceptorsThrows() {
        assertThrows(NullPointerException.class, () -> new OkHttpAsyncHttpClientBuilder().networkInterceptors(null));
    }

    /**
     * Tests building a client with a given {@code connectionTimeout}.
     */
    @Test
    public void buildWithConnectionTimeout() {
        int expectedConnectionTimeoutMillis = 3600 * 1000;
        Interceptor validatorInterceptor = chain -> {
            assertEquals(expectedConnectionTimeoutMillis, chain.connectTimeoutMillis());
            return chain.proceed(chain.request());
        };

        HttpClient okClient = new OkHttpAsyncHttpClientBuilder()
            .addNetworkInterceptor(validatorInterceptor)
            .connectionTimeout(Duration.ofSeconds(3600))
            .build();

        StepVerifier.create(okClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests building a client with a given {@code connectionTimeout}.
     */
    @Test
    public void buildWithReadTimeout() {
        int expectedReadTimeoutMillis = 3600 * 1000;
        Interceptor validatorInterceptor = chain -> {
            assertEquals(expectedReadTimeoutMillis, chain.readTimeoutMillis());
            return chain.proceed(chain.request());
        };

        HttpClient okClient = new OkHttpAsyncHttpClientBuilder()
            .addNetworkInterceptor(validatorInterceptor)
            .readTimeout(Duration.ofSeconds(3600))
            .build();

        StepVerifier.create(okClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests building a client with a given {@code connectionPool}.
     */
    @Test
    public void buildWithConnectionPool() {
        ConnectionPool connectionPool = new ConnectionPool();

        HttpClient okClient = new OkHttpAsyncHttpClientBuilder()
            .connectionPool(connectionPool)
            .build();

        StepVerifier.create(okClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();

        assertEquals(1, connectionPool.connectionCount());
    }

    /**
     * Tests that passing a {@code null} {@code connectionPool} to the builder will throw a {@link
     * NullPointerException}.
     */
    @Test
    public void nullConnectionPoolThrows() {
        assertThrows(NullPointerException.class, () -> new OkHttpAsyncHttpClientBuilder().connectionPool(null));
    }

    /**
     * Tests building a client with a given {@code dispatcher}.
     */
    @Test
    public void buildWithDispatcher() {
        String expectedThreadName = "testDispatcher";
        Dispatcher dispatcher = new Dispatcher(Executors
            .newFixedThreadPool(1, (Runnable r) -> new Thread(r, expectedThreadName)));

        HttpClient okClient = new OkHttpAsyncHttpClientBuilder()
            .dispatcher(dispatcher)
            .build();

        /*
         * Schedule a task that will run in one second to cancel all requests sent using the dispatcher. This should
         * result in the request we are about to send to be cancelled since WireMock will wait 5 seconds before
         * returning a response.
         */
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                assertEquals(1, dispatcher.runningCallsCount());
                dispatcher.cancelAll();
            }
        }, 1000);

        StepVerifier.create(okClient.send(new HttpRequest(HttpMethod.GET, dispatcherUrl)))
            .verifyError();
    }

    /**
     * Tests that passing a {@code null} {@code dispatcher} to the builder will throw a {@link NullPointerException}.
     */
    @Test
    public void nullDispatcherThrows() {
        assertThrows(NullPointerException.class, () -> new OkHttpAsyncHttpClientBuilder().dispatcher(null));
    }

    /**
     * Tests that building a client with a proxy will send the request through the proxy server.
     */
    @ParameterizedTest
    @MethodSource("buildWithProxySupplier")
    public void buildWithProxy(boolean shouldHaveProxy, Proxy.Type proxyType, ProxyOptions proxyOptions,
        String requestUrl) {
        OkHttpClient validatorClient = okHttpClientWithProxyValidation(shouldHaveProxy, proxyType);

        HttpClient okClient = new OkHttpAsyncHttpClientBuilder(validatorClient)
            .proxy(proxyOptions)
            .build();

        StepVerifier.create(okClient.send(new HttpRequest(HttpMethod.GET, requestUrl)))
            .verifyErrorMatches(throwable -> throwable.getMessage()
                .contains(TestEventListenerValidator.EXPECTED_EXCEPTION_MESSAGE));
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
        arguments.add(Arguments.of(true, Proxy.Type.SOCKS, socks4Proxy, defaultUrl));
        arguments.add(Arguments.of(true, Proxy.Type.SOCKS, socks5Proxy, defaultUrl));
        arguments.add(Arguments.of(true, Proxy.Type.HTTP, simpleHttpProxy, defaultUrl));

        /*
         * HTTP proxy with authentication configured.
         */
        ProxyOptions authenticatedHttpProxy = new ProxyOptions(ProxyOptions.Type.HTTP, proxyAddress)
            .setCredentials("1", "1");

        arguments.add(Arguments.of(true, Proxy.Type.HTTP, authenticatedHttpProxy, defaultUrl));

        /*
         * Information for non-proxy hosts testing.
         */
        String rawNonProxyHosts = String.join("|", "localhost", "127.0.0.1", "*.microsoft.com", "*.linkedin.com");

        String[] requestUrlsWithoutProxying = new String[]{
            "http://localhost", "http://127.0.0.1", "http://azure.microsoft.com", "http://careers.linkedin.com"
        };

        String[] requestUrlsWithProxying = new String[]{
            "http://example.com", "http://portal.azure.com", "http://linkedin.com", "http://8.8.8.8"
        };

        /*
         * HTTP proxies with non-proxy hosts configured.
         */
        Supplier<ProxyOptions> nonProxyHostsSupplier = () ->
            new ProxyOptions(ProxyOptions.Type.HTTP, proxyAddress).setNonProxyHosts(rawNonProxyHosts);

        for (String requestUrl : requestUrlsWithoutProxying) {
            arguments.add(Arguments.of(false, Proxy.Type.HTTP, nonProxyHostsSupplier.get(), requestUrl));
        }

        for (String requestUrl : requestUrlsWithProxying) {
            arguments.add(Arguments.of(true, Proxy.Type.HTTP, nonProxyHostsSupplier.get(), requestUrl));
        }

        /*
         * HTTP proxies with authentication and non-proxy hosts configured.
         */
        Supplier<ProxyOptions> authenticatedNonProxyHostsSupplier = () -> nonProxyHostsSupplier.get()
            .setCredentials("1", "1");

        for (String requestUrl : requestUrlsWithoutProxying) {
            arguments.add(Arguments.of(false, Proxy.Type.HTTP, authenticatedNonProxyHostsSupplier.get(), requestUrl));
        }

        for (String requestUrl : requestUrlsWithProxying) {
            arguments.add(Arguments.of(true, Proxy.Type.HTTP, authenticatedNonProxyHostsSupplier.get(), requestUrl));
        }

        return arguments.stream();
    }

    @Test
    public void buildWithConfigurationNone() {
        HttpClient okClient = new OkHttpAsyncHttpClientBuilder()
            .configuration(Configuration.NONE)
            .build();

        StepVerifier.create(okClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("buildWithConfigurationProxySupplier")
    public void buildWithConfigurationProxy(boolean shouldHaveProxy, Configuration configuration, String requestUrl) {
        OkHttpClient validatorClient = okHttpClientWithProxyValidation(shouldHaveProxy, Proxy.Type.HTTP);

        HttpClient okClient = new OkHttpAsyncHttpClientBuilder(validatorClient)
            .configuration(configuration)
            .build();

        StepVerifier.create(okClient.send(new HttpRequest(HttpMethod.GET, requestUrl)))
            .verifyErrorMatches(throwable -> throwable.getMessage()
                .contains(TestEventListenerValidator.EXPECTED_EXCEPTION_MESSAGE));
    }

    private static Stream<Arguments> buildWithConfigurationProxySupplier() {
        Supplier<Configuration> baseJavaProxyConfigurationSupplier = () -> new Configuration()
            .put(JAVA_HTTP_PROXY_HOST, "localhost")
            .put(JAVA_HTTP_PROXY_PORT, "12345");

        List<Arguments> arguments = new ArrayList<>();

        /*
         * Simple non-authenticated HTTP proxies.
         */
        arguments.add(Arguments.of(true, baseJavaProxyConfigurationSupplier.get(), defaultUrl));

        Configuration simpleEnvProxy = new Configuration()
            .put(Configuration.PROPERTY_HTTP_PROXY, "http://localhost:12345")
            .put(JAVA_SYSTEM_PROXY_PREREQUISITE, "true");
        arguments.add(Arguments.of(true, simpleEnvProxy, defaultUrl));

        /*
         * HTTP proxy with authentication configured.
         */
        Configuration javaProxyWithAuthentication = baseJavaProxyConfigurationSupplier.get()
            .put(JAVA_HTTP_PROXY_USER, "1")
            .put(JAVA_HTTP_PROXY_PASSWORD, "1");
        arguments.add(Arguments.of(true, javaProxyWithAuthentication, defaultUrl));

        Configuration envProxyWithAuthentication = new Configuration()
            .put(Configuration.PROPERTY_HTTP_PROXY, "http://1:1@localhost:12345")
            .put(JAVA_SYSTEM_PROXY_PREREQUISITE, "true");
        arguments.add(Arguments.of(true, envProxyWithAuthentication, defaultUrl));

        /*
         * Information for non-proxy hosts testing.
         */
        String rawJavaNonProxyHosts = String.join("|", "localhost", "127.0.0.1", "*.microsoft.com", "*.linkedin.com");
        String rawEnvNonProxyHosts = String.join(",", "localhost", "127.0.0.1", "*.microsoft.com", "*.linkedin.com");

        String[] requestUrlsWithoutProxying = new String[]{
            "http://localhost", "http://127.0.0.1", "http://azure.microsoft.com", "http://careers.linkedin.com"
        };

        String[] requestUrlsWithProxying = new String[]{
            "http://example.com", "http://portal.azure.com", "http://linkedin.com", "http://8.8.8.8"
        };

        /*
         * HTTP proxies with non-proxy hosts configured.
         */
        Supplier<Configuration> javaNonProxyHostsSupplier = () -> baseJavaProxyConfigurationSupplier.get()
            .put(JAVA_NON_PROXY_HOSTS, rawJavaNonProxyHosts);
        Supplier<Configuration> envNonProxyHostsSupplier = () -> new Configuration()
            .put(Configuration.PROPERTY_HTTP_PROXY, "http://localhost:12345")
            .put(Configuration.PROPERTY_NO_PROXY, rawEnvNonProxyHosts)
            .put(JAVA_SYSTEM_PROXY_PREREQUISITE, "true");

        List<Supplier<Configuration>> nonProxyHostsSuppliers = Arrays.asList(javaNonProxyHostsSupplier,
            envNonProxyHostsSupplier);

        for (Supplier<Configuration> configurationSupplier : nonProxyHostsSuppliers) {
            for (String requestUrl : requestUrlsWithoutProxying) {
                arguments.add(Arguments.of(false, configurationSupplier.get(), requestUrl));
            }

            for (String requestUrl : requestUrlsWithProxying) {
                arguments.add(Arguments.of(true, configurationSupplier.get(), requestUrl));
            }
        }

        /*
         * HTTP proxies with authentication and non-proxy hosts configured.
         */
        Supplier<Configuration> authenticatedJavaNonProxyHostsSupplier = () -> javaNonProxyHostsSupplier.get()
            .put(JAVA_HTTP_PROXY_USER, "1")
            .put(JAVA_HTTP_PROXY_PASSWORD, "1");
        Supplier<Configuration> authenticatedEnvNonProxyHostsSupplier = () -> new Configuration()
            .put(Configuration.PROPERTY_HTTP_PROXY, "http://1:1@localhost:12345")
            .put(Configuration.PROPERTY_NO_PROXY, rawEnvNonProxyHosts)
            .put(JAVA_SYSTEM_PROXY_PREREQUISITE, "true");

        List<Supplier<Configuration>> authenticatedNonProxyHostsSuppliers = Arrays.asList(
            authenticatedJavaNonProxyHostsSupplier, authenticatedEnvNonProxyHostsSupplier);

        for (Supplier<Configuration> configurationSupplier : authenticatedNonProxyHostsSuppliers) {
            for (String requestUrl : requestUrlsWithoutProxying) {
                arguments.add(Arguments.of(false, configurationSupplier.get(), requestUrl));
            }

            for (String requestUrl : requestUrlsWithProxying) {
                arguments.add(Arguments.of(true, configurationSupplier.get(), requestUrl));
            }
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
