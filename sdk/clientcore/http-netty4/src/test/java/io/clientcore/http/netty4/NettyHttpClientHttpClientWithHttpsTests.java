// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.shared.HttpBinJSON;
import io.clientcore.core.shared.HttpClientTests;
import io.clientcore.core.shared.HttpClientTestsServer;
import io.clientcore.core.shared.LocalTestServer;
import io.clientcore.core.utils.IOExceptionCheckedFunction;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Reactor Netty {@link HttpClientTests} with https.
 * Some request logic branches out if it's https like file uploads.
 */
@Timeout(value = 1, unit = TimeUnit.MINUTES)
public class NettyHttpClientHttpClientWithHttpsTests extends HttpClientTests {
    private static LocalTestServer server;

    private static final HttpClient HTTP_CLIENT_INSTANCE;

    static {
        try {
            SslContext sslContext
                = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();

            HTTP_CLIENT_INSTANCE = new NettyHttpClientBuilder().sslContext(sslContext).build();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    public static void startTestServer() {
        server = HttpClientTestsServer.getHttpClientTestsServer();
        server.start();
    }

    @AfterAll
    public static void stopTestServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Disabled("Need to implement server sent event support in NettyHttpClient")
    @Test
    public void canReceiveServerSentEvents() throws IOException {
    }

    @Disabled("Need to implement server sent event support in NettyHttpClient")
    @Test
    public void canRecognizeServerSentEvent() throws IOException {
    }

    @Disabled("Need to implement server sent event support in NettyHttpClient")
    @Test
    public void onErrorServerSentEvents() throws IOException {
    }

    @Disabled("Need to implement server sent event support in NettyHttpClient")
    @Test
    public void onRetryWithLastEventIdReceiveServerSentEvents() throws IOException {
    }

    @Disabled("Need to implement server sent event support in NettyHttpClient")
    @Test
    public void throwsExceptionForNoListener() {
    }

    @Override
    @Deprecated
    protected int getPort() {
        return server.getHttpsPort();
    }

    @Override
    protected String getServerUri(boolean secure) {
        return secure ? server.getHttpsUri() : server.getHttpUri();
    }

    @Override
    protected boolean isSecure() {
        return true;
    }

    @Override
    protected HttpClient getHttpClient() {
        return HTTP_CLIENT_INSTANCE;
    }

    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    @Test
    public void canSendBinaryDataDebugging() throws IOException {
        byte[] expectedResponseBody = new byte[8195];
        ThreadLocalRandom.current().nextBytes(expectedResponseBody);

        HttpRequest request = new HttpRequest().setMethod(HttpMethod.PUT)
            .setUri(getRequestUri(ECHO_RESPONSE))
            .setBody(BinaryData.fromBytes(expectedResponseBody));

        try (Response<BinaryData> response = getHttpClient().send(request)) {
            assertArrayEquals(expectedResponseBody, response.getValue().toBytes());
        }
    }

    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    @Test
    public void canSendTinyBinaryDataDebugging() throws IOException {
        byte[] expectedResponseBody = new byte[512];
        ThreadLocalRandom.current().nextBytes(expectedResponseBody);

        HttpRequest request = new HttpRequest().setMethod(HttpMethod.PUT)
            .setUri(getRequestUri(ECHO_RESPONSE))
            .setBody(BinaryData.fromBytes(expectedResponseBody));

        try (Response<BinaryData> response = getHttpClient().send(request)) {
            assertArrayEquals(expectedResponseBody, response.getValue().toBytes());
        }
    }

    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    @Test
    public void getRequestWithAnythingDebugging() {
        sendRequestAndConsumeHttpBinJson(new HttpRequest().setMethod(HttpMethod.GET).setUri(getRequestUri("anything")),
            json -> assertMatchWithHttpOrHttps("localhost/anything", json.uri()));
    }

    private void sendRequestAndConsumeHttpBinJson(HttpRequest request, Consumer<HttpBinJSON> jsonConsumer) {
        sendRequestAndConsumeHttpBinJson(request, getHttpClient()::send, jsonConsumer);
    }

    private void sendRequestAndConsumeHttpBinJson(HttpRequest request,
        IOExceptionCheckedFunction<HttpRequest, Response<BinaryData>> requestSend, Consumer<HttpBinJSON> jsonConsumer) {
        try (Response<BinaryData> response = requestSend.apply(request)) {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertNotNull(response.getValue());

            HttpBinJSON json = response.getValue().toObject(HttpBinJSON.class);
            assertNotNull(json);
            jsonConsumer.accept(json);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static void assertMatchWithHttpOrHttps(String uri1, String uri2) {
        final String s1 = "http://" + uri1;

        if (s1.equalsIgnoreCase(uri2)) {
            return;
        }

        final String s2 = "https://" + uri1;

        if (s2.equalsIgnoreCase(uri2)) {
            return;
        }

        fail("'" + uri2 + "' does not match with '" + s1 + "' or '" + s2 + "'.");
    }
}
