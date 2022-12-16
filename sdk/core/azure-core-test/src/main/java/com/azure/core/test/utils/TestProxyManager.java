// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.utils;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ConnectException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manages running the test recording proxy server
 */
public class TestProxyManager {
    private static final ClientLogger LOGGER = new ClientLogger(TestProxyManager.class);
    private static final Path PROXYPATH = Paths.get(System.getProperty("java.io.tmpdir"), "test-proxy");
    private final File recordingPath;
    private Process proxy;

    public TestProxyManager(File recordingPath) {
        this.recordingPath = recordingPath;

        // This is necessary to stop the proxy when the debugger is stopped.
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopProxy));
    }

    public void startProxy() {

        try {
            ProcessBuilder builder = new ProcessBuilder(Paths.get(PROXYPATH.toString(), getProxyProcessName()).toString(), "--storage-location", recordingPath.getPath())
                .inheritIO()
                .redirectErrorStream(true)
                .directory(PROXYPATH.toFile());
            proxy = builder.start();
            HttpURLConnectionHttpClient client = new HttpURLConnectionHttpClient();
            HttpRequest request = new HttpRequest(HttpMethod.GET, String.format("%s/admin/isalive", TestProxyUtils.getProxyUrl()));
            for (int i = 0; i < 10; i++) {
                HttpResponse response = null;
                try {
                    response = client.sendSync(request, Context.NONE);
                } catch (Exception ignored) {

                }
                if (response != null && response.getStatusCode() == 200) {
                    return;
                }
                Thread.sleep(1000);
            }
            throw new RuntimeException("Test proxy did not initialize.");

        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopProxy() {
        if (proxy.isAlive()) {
            proxy.destroy();
        }
    }

    private String getProxyProcessName() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            return "Azure.Sdk.Tools.TestProxy.exe";
        } else if (osName.contains("linux")) {
            throw new UnsupportedOperationException();
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
