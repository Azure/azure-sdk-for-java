// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.utils;

import com.typespec.core.http.HttpMethod;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.util.Configuration;
import com.typespec.core.util.Context;
import com.typespec.core.util.logging.ClientLogger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;

/**
 * Manages running the test recording proxy server
 */
public class TestProxyManager {
    private static final ClientLogger LOGGER = new ClientLogger(TestProxyManager.class);
    private Process proxy;
    private final Path testClassPath;

    /**
     * Construct a {@link TestProxyManager} for controlling the external test proxy.
     * @param testClassPath the test class path
     */
    public TestProxyManager(Path testClassPath) {
        this.testClassPath = testClassPath;
        // This is necessary to stop the proxy when the debugger is stopped.
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopProxy));
        if (runningLocally()) {
            TestProxyDownloader.installTestProxy(testClassPath);
        }
    }

    /**
     * Start an instance of the test proxy.
     * @throws UncheckedIOException There was an issue communicating with the proxy.
     * @throws RuntimeException There was an issue starting the proxy process.
     */
    public void startProxy() {
        try {
            // if we're not running in CI we will check to see if someone has started the proxy, and start one if not.
            if (runningLocally() && !checkAlive(1, Duration.ofSeconds(1))) {
                String commandLine = Paths.get(TestProxyDownloader.getProxyDirectory().toString(),
                    TestProxyUtils.getProxyProcessName()).toString();

                ProcessBuilder builder = new ProcessBuilder(commandLine,
                    "--storage-location",
                    TestUtils.getRepoRootResolveUntil(testClassPath, "eng").toString());
                Map<String, String> environment = builder.environment();
                environment.put("LOGGING__LOGLEVEL", "Information");
                environment.put("LOGGING__LOGLEVEL__MICROSOFT", "Warning");
                environment.put("LOGGING__LOGLEVEL__DEFAULT", "Information");
                proxy = builder.start();
            }
            // in either case the proxy should now be started, so let's wait to make sure.
            if (checkAlive(10, Duration.ofSeconds(6))) {
                return;
            }
            throw new RuntimeException("Test proxy did not initialize.");

        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkAlive(int loops, Duration waitTime) throws InterruptedException {
        HttpURLConnectionHttpClient client = new HttpURLConnectionHttpClient();
        HttpRequest request = new HttpRequest(HttpMethod.GET,
            String.format("%s/admin/isalive", TestProxyUtils.getProxyUrl()));
        for (int i = 0; i < loops; i++) {
            HttpResponse response = null;
            try {
                response = client.sendSync(request, Context.NONE);
                if (response != null && response.getStatusCode() == 200) {
                    return true;
                }
                TestProxyUtils.checkForTestProxyErrors(response);
            } catch (Exception ignored) {
            }
            Thread.sleep(waitTime.toMillis());
        }
        return false;
    }

    /**
     * Stop the running instance of the test proxy.
     */
    public void stopProxy() {
        if (proxy != null && proxy.isAlive()) {
            proxy.destroy();
        }
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
