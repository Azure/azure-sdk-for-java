// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.trafficlistener.WiremockNetworkTrafficListener;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.util.binarydata.BinaryData;
import org.junit.jupiter.api.Test;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SocketTest {
    private static final SocketConnectionListener SOCKET_CONNECTION_LISTENER = new SocketConnectionListener();

    private static class SocketConnectionListener implements WiremockNetworkTrafficListener {
        private final AtomicInteger openedConnections = new AtomicInteger(0);

        @Override
        public void opened(Socket socket) {
            openedConnections.incrementAndGet();
        }

        @Override
        public void incoming(Socket socket, ByteBuffer bytes) {
        }

        @Override
        public void outgoing(Socket socket, ByteBuffer bytes) {
        }

        @Override
        public void closed(Socket socket) {
        }

        public int openedConnections() {
            return openedConnections.get();
        }
    }

    @Test
    public void useSocketConnectionPooling() {
        int initConnections = SOCKET_CONNECTION_LISTENER.openedConnections();

        // Create a WireMockServer with a NetworkTrafficListener
        WireMockServer wireMockServer = new WireMockServer(
            new WireMockConfiguration().dynamicPort().networkTrafficListener(SOCKET_CONNECTION_LISTENER));
        wireMockServer.start();

        // Configure WireMock to stub a response
        WireMock.configureFor("localhost", wireMockServer.port());
        String url = "http://localhost:" + wireMockServer.port() + "/test";

        WireMock.stubFor(WireMock.patch(WireMock.urlEqualTo("/test")).willReturn(WireMock.aResponse().withBody("test")));

        // Create a socket and connect it to the server
        for (int i = 0; i < 5; i++) {
            new DefaultHttpClientBuilder().build()
                .send(new HttpRequest(HttpMethod.PATCH, url)
                    .setHeaders(new HttpHeaders()
                        .add(HttpHeaderName.CONTENT_TYPE, "application/json")
                        .add(HttpHeaderName.CONTENT_LENGTH, String.valueOf("test".length()))).setBody(BinaryData.fromString("test")));
        }

        // should not be 6 because the connections are pooled
        assertEquals( 6, SOCKET_CONNECTION_LISTENER.openedConnections());

        wireMockServer.stop();
    }
}
