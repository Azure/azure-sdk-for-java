// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.DEFAULT_USER_AGENT_VALUE_PREFIX;
import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.VERSION;
import static org.junit.jupiter.api.Assertions.*;

public class HttpUtilTest {

    @Test
    public void getUserAgentPrefixTest() {
        assertEquals(DEFAULT_USER_AGENT_VALUE_PREFIX, HttpUtil.getUserAgentPrefix());
        assertEquals(DEFAULT_USER_AGENT_VALUE_PREFIX + VERSION, HttpUtil.USER_AGENT_VALUE);
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    public void getUsesJvmProxySystemProperties() throws Exception {
        String previousProxyHost = System.getProperty("http.proxyHost");
        String previousProxyPort = System.getProperty("http.proxyPort");
        String previousNonProxyHosts = System.getProperty("http.nonProxyHosts");

        try (ServerSocket proxyServer = new ServerSocket(0)) {
            CountDownLatch requestReceived = new CountDownLatch(1);
            AtomicReference<String> requestLine = new AtomicReference<>();
            AtomicReference<Exception> proxyFailure = new AtomicReference<>();

            Thread proxyThread
                = new Thread(() -> handleProxyRequest(proxyServer, requestLine, requestReceived, proxyFailure));
            proxyThread.setDaemon(true);
            proxyThread.start();

            System.setProperty("http.proxyHost", "localhost");
            System.setProperty("http.proxyPort", String.valueOf(proxyServer.getLocalPort()));
            System.clearProperty("http.nonProxyHosts");

            String response = HttpUtil.get("http://azure-keyvault-jca-proxy-test.invalid/path", null);

            assertTrue(requestReceived.await(5, TimeUnit.SECONDS), "Expected proxy server to receive the request.");
            assertNull(proxyFailure.get(), "Proxy server failed while handling the request.");
            assertEquals("proxied", response);
            assertEquals("GET http://azure-keyvault-jca-proxy-test.invalid/path HTTP/1.1", requestLine.get());
        } finally {
            restoreProperty("http.proxyHost", previousProxyHost);
            restoreProperty("http.proxyPort", previousProxyPort);
            restoreProperty("http.nonProxyHosts", previousNonProxyHosts);
        }
    }

    @Test
    @Disabled("Disable this because it will cause pipeline failure: https://dev.azure.com/azure-sdk/internal/_build/results?buildId=1196171&view=logs&j=4a83f3be-c53d-53dd-7954-86872056fb11&t=54174aae-5a55-579d-08e2-94fb446f7b77&l=29")
    public void testHttpUtilGet() {
        String url = "https://azure.com/";
        String result = HttpUtil.get(url, null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @Disabled("This is only used to test in localhost manually")
    public void testHttpUtilGet1() {
        String url = "http://localhost:8000/";
        String result = HttpUtil.get(url, null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    private static void handleProxyRequest(ServerSocket proxyServer, AtomicReference<String> requestLine,
        CountDownLatch requestReceived, AtomicReference<Exception> proxyFailure) {
        try (Socket socket = proxyServer.accept();
            BufferedReader reader
                = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            OutputStream outputStream = socket.getOutputStream()) {

            requestLine.set(reader.readLine());
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    break;
                }
            }

            byte[] body = "proxied".getBytes(StandardCharsets.UTF_8);
            outputStream.write(
                ("HTTP/1.1 200 OK\r\nContent-Length: " + body.length + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            outputStream.write(body);
            outputStream.flush();
            requestReceived.countDown();
        } catch (Exception e) {
            proxyFailure.set(e);
            requestReceived.countDown();
        }
    }

    private static void restoreProperty(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }
}
