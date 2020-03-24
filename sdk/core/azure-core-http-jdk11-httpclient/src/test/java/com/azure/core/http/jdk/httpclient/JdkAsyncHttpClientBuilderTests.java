// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link JdkAsyncHttpClientBuilder}.
 */
public class JdkAsyncHttpClientBuilderTests {
    private static final String COOKIE_VALIDATOR_PATH = "/cookieValidator";
    private static final String DEFAULT_PATH = "/default";
    private static final String DISPATCHER_PATH = "/dispatcher";

    private static WireMockServer server;

    private static String cookieValidatorUrl;
    private static String defaultUrl;
    private static String dispatcherUrl;

    @BeforeAll
    public static void setupWireMock() {
        server = new WireMockServer(WireMockConfiguration.options().dynamicPort().disableRequestJournal());

        // Mocked endpoint to test building a client with a prebuilt HttpClient.
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
     * Tests that an {@link JdkAsyncHttpClient} is able to be built from an existing
     * {@link java.net.http.HttpClient.Builder}.
     */
    @Test
    public void buildClientWithExistingClient() {
        java.net.http.HttpClient.Builder existingClientBuilder = java.net.http.HttpClient.newBuilder();
//            .addInterceptor(chain -> chain
//                .proceed(chain.request().newBuilder().addHeader("Cookie", "test=success").build()))
//            .build();

        JdkAsyncHttpClient client = (JdkAsyncHttpClient) new JdkAsyncHttpClientBuilder(existingClientBuilder)
            .build();

        StepVerifier.create(client.send(new HttpRequest(HttpMethod.GET, cookieValidatorUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that instantiating an {@link JdkAsyncHttpClientBuilder} with a {@code null} {@link JdkAsyncHttpClient}
     * will throw a {@link NullPointerException}.
     */
    @Test
    public void startingWithNullClientThrows() {
        assertThrows(NullPointerException.class, () -> new JdkAsyncHttpClientBuilder(null));
    }

//    /**
//     * Tests that adding an {@link Interceptor} is handled correctly.
//     */
//    @Test
//    public void addNetworkInterceptor() {
//        Interceptor testInterceptor = chain -> chain.proceed(chain.request().newBuilder()
//            .addHeader("Cookie", "test=success").build());
//
//        OkHttpAsyncHttpClient client = (OkHttpAsyncHttpClient) new OkHttpAsyncHttpClientBuilder()
//            .addNetworkInterceptor(testInterceptor)
//            .build();
//
//        StepVerifier.create(client.send(new HttpRequest(HttpMethod.GET, cookieValidatorUrl)))
//            .assertNext(response -> assertEquals(200, response.getStatusCode()))
//            .verifyComplete();
//    }
//
//    /**
//     * Tests that adding a {@code null} {@link Interceptor} will throw a {@link NullPointerException}.
//     */
//    @Test
//    public void nullNetworkInterceptorThrows() {
//        assertThrows(NullPointerException.class, () -> new OkHttpAsyncHttpClientBuilder().addNetworkInterceptor(null));
//    }
//
//    /**
//     * Tests that the {@link Interceptor interceptors} in the client are replace-able by setting a new list of
//     * interceptors.
//     */
//    @Test
//    public void setNetworkInterceptors() {
//        Interceptor badCookieSetter = chain -> chain.proceed(chain.request().newBuilder()
//            .addHeader("Cookie", "test=failure").build());
//        Interceptor goodCookieSetter = chain -> chain.proceed(chain.request().newBuilder()
//            .addHeader("Cookie", "test=success").build());
//
//        OkHttpAsyncHttpClient client = (OkHttpAsyncHttpClient) new OkHttpAsyncHttpClientBuilder()
//            .addNetworkInterceptor(badCookieSetter)
//            .networkInterceptors(Collections.singletonList(goodCookieSetter))
//            .build();
//
//        StepVerifier.create(client.send(new HttpRequest(HttpMethod.GET, cookieValidatorUrl)))
//            .assertNext(response -> assertEquals(200, response.getStatusCode()))
//            .verifyComplete();
//    }
//
//    /**
//     * Tests that setting the {@link Interceptor interceptors} to {@code null} will throw a {@link
//     * NullPointerException}.
//     */
//    @Test
//    public void nullNetworkInterceptorsThrows() {
//        assertThrows(NullPointerException.class, () -> new OkHttpAsyncHttpClientBuilder().networkInterceptors(null));
//    }
//
//    /**
//     * Tests building a client with a given {@code connectionTimeout}.
//     */
//    @Test
//    public void buildWithConnectionTimeout() {
//        int expectedConnectionTimeoutMillis = 3600 * 1000;
//        Interceptor validatorInterceptor = chain -> {
//            assertEquals(expectedConnectionTimeoutMillis, chain.connectTimeoutMillis());
//            return chain.proceed(chain.request());
//        };
//
//        OkHttpAsyncHttpClient okClient = (OkHttpAsyncHttpClient) new OkHttpAsyncHttpClientBuilder()
//            .addNetworkInterceptor(validatorInterceptor)
//            .connectionTimeout(Duration.ofSeconds(3600))
//            .build();
//
//        StepVerifier.create(okClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
//            .assertNext(response -> assertEquals(200, response.getStatusCode()))
//            .verifyComplete();
//    }

//    /**
//     * Tests building a client with a given {@code connectionTimeout}.
//     */
//    @Test
//    public void buildWithReadTimeout() {
//        int expectedReadTimeoutMillis = 3600 * 1000;
//        Interceptor validatorInterceptor = chain -> {
//            assertEquals(expectedReadTimeoutMillis, chain.readTimeoutMillis());
//            return chain.proceed(chain.request());
//        };
//
//        OkHttpAsyncHttpClient okClient = (OkHttpAsyncHttpClient) new OkHttpAsyncHttpClientBuilder()
//            .addNetworkInterceptor(validatorInterceptor)
//            .readTimeout(Duration.ofSeconds(3600))
//            .build();
//
//        StepVerifier.create(okClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
//            .assertNext(response -> assertEquals(200, response.getStatusCode()))
//            .verifyComplete();
//    }

    /**
     * Tests building a client with a given {@code connectionPool}.
     */
    @Test
    public void buildWithConnectionPool() {
        HttpClient httpClient = new JdkAsyncHttpClientBuilder()
            .executor(Executors.newFixedThreadPool(10))
            .build();

        StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that passing a {@code null} {@code executor} to the builder will throw a
     * {@link NullPointerException}.
     */
    @Test
    public void nullConnectionPoolThrows() {
        assertThrows(NullPointerException.class, () -> new JdkAsyncHttpClientBuilder().executor(null));
    }

//    /**
//     * Tests building a client with a given {@code dispatcher}.
//     */
//    @Test
//    public void buildWithDispatcher() {
//        String expectedThreadName = "testDispatcher";
//        Dispatcher dispatcher = new Dispatcher(Executors
//            .newFixedThreadPool(1, (Runnable r) -> new Thread(r, expectedThreadName)));
//
//        OkHttpAsyncHttpClient okClient = (OkHttpAsyncHttpClient) new OkHttpAsyncHttpClientBuilder()
//            .dispatcher(dispatcher)
//            .build();
//
//        /*
//         * Schedule a task that will run in one second to cancel all requests sent using the dispatcher. This should
//         * result in the request we are about to send to be cancelled since WireMock will wait 5 seconds before
//         * returning a response.
//         */
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                assertEquals(1, dispatcher.runningCallsCount());
//                dispatcher.cancelAll();
//            }
//        }, 1000);
//
//        StepVerifier.create(okClient.send(new HttpRequest(HttpMethod.GET, dispatcherUrl)))
//            .verifyError();
//    }
//
//    /**
//     * Tests that passing a {@code null} {@code dispatcher} to the builder will throw a {@link NullPointerException}.
//     */
//    @Test
//    public void nullDispatcherThrows() {
//        assertThrows(NullPointerException.class, () -> new OkHttpAsyncHttpClientBuilder().dispatcher(null));
//    }

    /**
     * Tests building a client with a given proxy.
     */
    @ParameterizedTest
    @EnumSource(ProxyOptions.Type.class)
    public void buildWithProxy(ProxyOptions.Type proxyType) {
        String expectedProxyHost = "localhost";
        int expectedProxyPort = 8888;

        ProxyOptions proxyOptions = new ProxyOptions(proxyType,
            new InetSocketAddress(expectedProxyHost, expectedProxyPort)).setCredentials("1", "1");

//        java.net.http.HttpClient validatorClient = java.net.http.HttpClient.newBuilder()
//            .eventListener(new TestEventListenerValidator(expectedProxyHost, expectedProxyPort))
//            .build();

        HttpClient httpClient = new JdkAsyncHttpClientBuilder(java.net.http.HttpClient.newBuilder())
            .proxy(proxyOptions)
            .build();

        StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .verifyError();
    }

    @Test
    public void buildWithConfigurationNone() {
        HttpClient httpClient = new JdkAsyncHttpClientBuilder()
            .configuration(Configuration.NONE)
            .build();

        StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

//    @Test
//    public void buildWithConfigurationProxy() {
//        Configuration configuration = new Configuration()
//            .put(Configuration.PROPERTY_HTTP_PROXY, "http://localhost:8888");
//
//        String expectedProxyHost = "localhost";
//        int expectedProxyPort = 8888;
//
//        OkHttpClient validatorClient = new OkHttpClient.Builder()
//            .eventListener(new TestEventListenerValidator(expectedProxyHost, expectedProxyPort))
//            .build();
//
//        OkHttpAsyncHttpClient okClient = (OkHttpAsyncHttpClient) new OkHttpAsyncHttpClientBuilder(validatorClient)
//            .configuration(configuration)
//            .build();
//
//        StepVerifier.create(okClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
//            .verifyError();
//    }

    @Test
    public void buildWithNonProxyConfigurationProxy() {
        Configuration configuration = new Configuration()
            .put(Configuration.PROPERTY_HTTP_PROXY, "http://localhost:8888")
            .put(Configuration.PROPERTY_NO_PROXY, "localhost");

        HttpClient httpClient = new JdkAsyncHttpClientBuilder()
            .configuration(configuration)
            .build();

        StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, defaultUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

//    private static final class TestEventListenerValidator extends EventListener {
//        private final String expectedProxyHost;
//        private final int expectedProxyPort;
//
//        private TestEventListenerValidator(String expectedProxyHost, int expectedProxyPort) {
//            this.expectedProxyHost = expectedProxyHost;
//            this.expectedProxyPort = expectedProxyPort;
//        }
//
//        @Override
//        public void connectStart(Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {
//            InetSocketAddress proxyAddress = (InetSocketAddress) proxy.address();
//            assertNotNull(proxyAddress);
//            assertEquals(expectedProxyHost, proxyAddress.getHostName());
//            assertEquals(expectedProxyPort, proxyAddress.getPort());
//            super.connectStart(call, inetSocketAddress, proxy);
//        }
//    }
}
