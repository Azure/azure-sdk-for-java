// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.utils;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;

/**
 * Manages running the test recording proxy server
 */
public final class TestProxyManager {
    private static final ClientLogger LOGGER = new ClientLogger(TestProxyManager.class);
    private static Process proxy;
    private static final Path WORKING_DIRECTORY = Paths.get(System.getProperty("user.dir"));

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(TestProxyManager::stopProxy));
        if (runningLocally()) {
            TestProxyDownloader.installTestProxy(WORKING_DIRECTORY);
        }
    }

    @Deprecated
    private TestProxyManager() {
    }

    /**
     * Start an instance of the test proxy.
     * @throws UncheckedIOException There was an issue communicating with the proxy.
     * @throws RuntimeException There was an issue starting the proxy process.
     */
    public static synchronized void startProxy() {
        try {
            // if we're not running in CI we will check to see if someone has started the proxy, and start one if not.
            if (runningLocally() && !checkAlive(1, Duration.ofSeconds(1), null)) {
                String commandLine = Paths
                    .get(TestProxyDownloader.getProxyDirectory().toString(), TestProxyUtils.getProxyProcessName())
                    .toString();

                Path repoRoot = TestUtils.getRepoRootResolveUntil(WORKING_DIRECTORY, "eng");

                // Resolve the path to the repo root 'target' folder and create the folder if it doesn't exist.
                // This folder will be used to store the 'test-proxy.log' file to enable simpler debugging of Test Proxy
                // locally. This is similar to what CI does, but CI uses a PowerShell process to run the Test Proxy
                // where running locally uses a Java ProcessBuilder.
                Path repoRootTarget = repoRoot.resolve("target");
                if (!Files.exists(repoRootTarget)) {
                    Files.createDirectory(repoRootTarget);
                }

                ProcessBuilder builder = new ProcessBuilder(commandLine, "--storage-location", repoRoot.toString())
                    .redirectOutput(repoRootTarget.resolve("test-proxy.log").toFile())
                    .redirectError(repoRootTarget.resolve("test-proxy-error.log").toFile());
                Map<String, String> environment = builder.environment();
                environment.put("LOGGING__LOGLEVEL", "Debug");
                environment.put("LOGGING__LOGLEVEL__MICROSOFT", "Debug");
                environment.put("LOGGING__LOGLEVEL__DEFAULT", "Debug");
                proxy = builder.start();
            }
            // in either case the proxy should now be started, so let's wait to make sure.
            if (checkAlive(10, Duration.ofSeconds(6), proxy)) {
                return;
            }

            // If the Test Proxy process doesn't start within the timeout period read the error stream of the Process
            // for any additional details that could help determine why the Test Proxy process didn't start.
            // Include this additional information in the exception message.
            ByteArrayOutputStream errorLog = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = proxy.getErrorStream().read(buffer)) != -1) {
                errorLog.write(buffer, 0, read);
            }

            String errorLogString = new String(errorLog.toByteArray(), StandardCharsets.UTF_8);
            if (CoreUtils.isNullOrEmpty(errorLogString)) {
                throw new RuntimeException("Test proxy did not initialize.");
            } else {
                throw new RuntimeException("Test proxy did not initialize. Error log: " + errorLogString);
            }
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean checkAlive(int loops, Duration waitTime, Process proxy) throws InterruptedException {
        HttpURLConnectionHttpClient client = new HttpURLConnectionHttpClient();
        HttpRequest request
            = new HttpRequest(HttpMethod.GET, String.format("%s/admin/isalive", TestProxyUtils.getProxyUrl()));
        for (int i = 0; i < loops; i++) {
            // If the proxy isn't alive and the exit value isn't 0, then the proxy process has exited with an error
            // and stop waiting.
            if (proxy != null && !proxy.isAlive() && proxy.exitValue() != 0) {
                return false;
            }

            try {
                HttpResponse response = client.sendSync(request, Context.NONE);
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
    private static void stopProxy() {
        if (proxy != null && proxy.isAlive()) {
            proxy.destroy();
        }
    }

    /**
     * Checks the environment variables commonly set in CI to determine if the run is local.
     * @return True if the run is local.
     */
    private static boolean runningLocally() {
        return Configuration.getGlobalConfiguration().get("TF_BUILD") == null
            && Configuration.getGlobalConfiguration().get("CI") == null;
    }
}
