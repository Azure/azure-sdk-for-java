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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Manages running the test recording proxy server
 */
public class TestProxyManager {
    private static final ClientLogger LOGGER = new ClientLogger(TestProxyManager.class);
    private static final Path PROXYPATH = Paths.get(System.getProperty("java.io.tmpdir"), "test-proxy");
    private final File recordingPath;
    private Process proxy;

    /**
     * Construct a {@link TestProxyManager} for controlling the external test proxy.
     * @param recordingPath The local path in the file system where recordings are saved.
     */
    public TestProxyManager(File recordingPath) {
        this.recordingPath = recordingPath;

        // This is necessary to stop the proxy when the debugger is stopped.
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopProxy));
    }

    /**
     * Start an instance of the test proxy.
     * @throws UncheckedIOException There was an issue communicating with the proxy.
     * @throws RuntimeException There was an issue starting the proxy process.
     */
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
                    if (response != null && response.getStatusCode() == 200) {
                        return;
                    }
                } catch (Exception ignored) {
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

    /**
     * Stop the running instance of the test proxy.
     */
    public void stopProxy() {
        if (proxy.isAlive()) {
            proxy.destroy();
        }
    }

    private String getProxyProcessName() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (osName.contains("windows")) {
            return "Azure.Sdk.Tools.TestProxy.exe";
        } else if (osName.contains("linux")) {
            throw new UnsupportedOperationException();
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
