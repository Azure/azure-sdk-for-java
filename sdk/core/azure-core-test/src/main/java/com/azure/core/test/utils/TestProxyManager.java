// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.utils;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages running the test recording proxy server
 */
public class TestProxyManager {
    private static final ClientLogger LOGGER = new ClientLogger(TestProxyManager.class);
    private final File recordingPath;
    private Process proxy;

    private static final AtomicInteger PORT_COUNTER = new AtomicInteger(5000);

    private URL proxyUrl;

    /**
     * Construct a {@link TestProxyManager} for controlling the external test proxy.
     * @param recordingPath The local path in the file system where recordings are saved.
     */
    public TestProxyManager(File recordingPath) {
        this.recordingPath = recordingPath;

        // This is necessary to stop the proxy when the debugger is stopped.
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopProxy));
        if (runningLocally()) {
            TestProxyDownloader.installTestProxy();
        }
    }

    /**
     * Start an instance of the test proxy.
     * @throws UncheckedIOException There was an issue communicating with the proxy.
     * @throws RuntimeException There was an issue starting the proxy process.
     */
    public void startProxy() {

        try {
            String commandLine = "test-proxy";
            // if we're not running in CI, construct the local path. TF_BUILD indicates Azure DevOps. CI indicates Github Actions.
            if (runningLocally()) {
                commandLine = Paths.get(TestProxyDownloader.getProxyDirectory().toString(),
                    TestProxyUtils.getProxyProcessName()).toString();
            }

            ProcessBuilder builder = new ProcessBuilder(commandLine,
                "--storage-location",
                recordingPath.getPath(),
                "--",
                "--urls",
                getProxyUrl().toString());
            Map<String, String> environment = builder.environment();
            environment.put("LOGGING__LOGLEVEL", "Information");
            environment.put("LOGGING__LOGLEVEL__MICROSOFT", "Warning");
            environment.put("LOGGING__LOGLEVEL__DEFAULT", "Information");
            proxy = builder.start();

            HttpURLConnectionHttpClient client = new HttpURLConnectionHttpClient();
            HttpRequest request = new HttpRequest(HttpMethod.GET,
                String.format("%s/admin/isalive", getProxyUrl()));
            for (int i = 0; i < 10; i++) {
                HttpResponse response = null;
                try {
                    response = client.sendSync(request, Context.NONE);
                    if (response != null && response.getStatusCode() == 200) {
                        return;
                    }
                    TestProxyUtils.checkForTestProxyErrors(response);
                } catch (Exception ignored) {
                }
                Thread.sleep(6000);
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

    /**
     * Get the proxy URL.
     *
     * @return A string containing the proxy URL.
     * @throws RuntimeException The proxy URL could not be constructed.
     */
    public URL getProxyUrl() {
        if (proxyUrl != null) {
            return proxyUrl;
        }
        UrlBuilder builder = new UrlBuilder();
        builder.setHost("localhost");
        builder.setScheme("http");
        builder.setPort(PORT_COUNTER.getAndIncrement());
        try {
            proxyUrl = builder.toUrl();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return proxyUrl;
    }

    /**
     * Checks the environment variables commonly set in CI to determine if the run is local.
     * @return True if the run is local.
     */
    private boolean runningLocally() {
        return Configuration.getGlobalConfiguration().get("TF_BUILD") == null
            && Configuration.getGlobalConfiguration().get("CI") == null;
    }
}
