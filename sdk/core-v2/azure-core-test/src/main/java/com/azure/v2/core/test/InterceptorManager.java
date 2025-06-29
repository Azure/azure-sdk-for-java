// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.core.test;

import com.azure.v2.core.test.http.TestProxyPlaybackClient;
import com.azure.v2.core.test.models.TestProxyRecordingOptions;
import com.azure.v2.core.test.models.TestProxyRequestMatcher;
import com.azure.v2.core.test.models.TestProxySanitizer;
import com.azure.v2.core.test.policy.TestProxyRecordPolicy;
import com.azure.v2.core.test.utils.TestUtils;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.CoreUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A class that keeps track of network calls by either reading the data from an existing test session record or
 * recording the network calls in memory. Test session records are saved or read from:
 * "<i>session-records/{@code testName}.json</i>"
 *
 * <ul>
 * <li>If the {@code testMode} is {@link TestMode#PLAYBACK}, the manager tries to find an existing test session
 * record to read network calls from.</li>
 * <li>If the {@code testMode} is {@link TestMode#RECORD}, the manager creates a new test session record and saves
 * all the network calls to it.</li>
 * <li>If the {@code testMode} is {@link TestMode#LIVE}, the manager won't attempt to read or create a test session
 * record.</li>
 * </ul>
 *
 * When the {@link InterceptorManager} is disposed, if the {@code testMode} is {@link TestMode#RECORD}, the network
 * calls that were recorded are persisted to: "<i>session-records/{@code testName}.json</i>"
 */
public class InterceptorManager implements AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(InterceptorManager.class);
    private final String testName;
    private final String playbackRecordName;
    private final TestMode testMode;
    private final boolean allowedToReadRecordedValues;
    private final boolean allowedToRecordValues;

    private final boolean skipRecordingRequestBody;
    private TestProxyRecordPolicy testProxyRecordPolicy;
    private TestProxyPlaybackClient testProxyPlaybackClient;
    private final Queue<String> proxyVariableQueue = new LinkedList<>();
    private HttpClient httpClient;
    private final Path testClassPath;
    private String xRecordingFileLocation;

    /**
     * Creates a new InterceptorManager that either replays test-session records or saves them.
     *
     * <ul>
     *     <li>If {@code testMode} is {@link TestMode#PLAYBACK}, the manager tries to find an existing test session
     *     record to read network calls from.</li>
     *     <li>If {@code testMode} is {@link TestMode#RECORD}, the manager creates a new test session record and saves
     *     all the network calls to it.</li>
     *     <li>If {@code testMode} is {@link TestMode#LIVE}, the manager won't attempt to read or create a test session
     *     record.</li>
     * </ul>
     *
     * The test session records are persisted in the path: "<i>session-records/{@code testName}.json</i>"
     *
     * @param testContextManager Contextual information about the test being ran, such as test name, {@link TestMode},
     * and others.
     * @throws NullPointerException If {@code testName} is {@code null}.
     */
    public InterceptorManager(TestContextManager testContextManager) {
        this(testContextManager.getTestName(), testContextManager.getTestPlaybackRecordingName(),
            testContextManager.getTestMode(), testContextManager.doNotRecordTest(),
            testContextManager.skipRecordingRequestBody(), testContextManager.getTestClassPath());
    }

    private InterceptorManager(String testName, String playbackRecordName, TestMode testMode, boolean doNotRecord,
        boolean skipRecordingRequestBody, Path testClassPath) {
        Objects.requireNonNull(testName, "'testName' cannot be null.");

        this.testName = testName;
        this.playbackRecordName = CoreUtils.isNullOrEmpty(playbackRecordName) ? testName : playbackRecordName;
        this.testMode = testMode;
        this.skipRecordingRequestBody = skipRecordingRequestBody;
        this.testClassPath = testClassPath;

        this.allowedToReadRecordedValues = (testMode == TestMode.PLAYBACK && !doNotRecord);
        this.allowedToRecordValues = (testMode == TestMode.RECORD && !doNotRecord);
    }

    /**
     * Gets whether this InterceptorManager is in playback mode.
     *
     * @return true if the InterceptorManager is in playback mode and false otherwise.
     */
    public boolean isPlaybackMode() {
        return testMode == TestMode.PLAYBACK;
    }

    /**
     * Gets whether this InterceptorManager is in live mode.
     *
     * @return true if the InterceptorManager is in live mode and false otherwise.
     */
    public boolean isLiveMode() {
        return testMode == TestMode.LIVE;
    }

    /**
     * Gets whether this InterceptorManager is in record mode.
     *
     * @return true if the InterceptorManager is in record mode and false otherwise.
     */
    public boolean isRecordMode() {
        return testMode == TestMode.RECORD;
    }

    /**
     * A {@link Supplier} for retrieving a variable from a test proxy recording.
     *
     * @return The supplier for retrieving a variable.
     */
    public Supplier<String> getProxyVariableSupplier() {
        return () -> {
            Objects.requireNonNull(this.testProxyPlaybackClient, "Playback must be started to retrieve values");
            if (!CoreUtils.isNullOrEmpty(proxyVariableQueue)) {
                return proxyVariableQueue.remove();
            } else {
                throw LOGGER.throwableAtError()
                    .log("'proxyVariableQueue' cannot be null or empty.", IllegalArgumentException::new);
            }
        };
    }

    /**
     * Get a {@link Consumer} for adding variables used in test proxy tests.
     *
     * @return The consumer for adding a variable.
     */
    public Consumer<String> getProxyVariableConsumer() {
        return proxyVariableQueue::add;
    }

    /**
     * Gets a new HTTP pipeline policy that records network calls and its data is managed by
     * {@link InterceptorManager}.
     *
     * @return HttpPipelinePolicy to record network calls.
     * @throws IllegalStateException A recording policy was requested when the test proxy is enabled and test mode is not RECORD.
     */
    public HttpPipelinePolicy getRecordPolicy() {
        return getProxyRecordingPolicy();
    }

    /**
     * Gets the HTTP client that is used for this test.
     *
     * @return The {@link HttpClient} implementation used for this test.
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Gets a new HTTP client that plays back test session records managed by {@link InterceptorManager}.
     *
     * @return An HTTP client that plays back network calls from its recorded data.
     * @throws IllegalStateException A playback client was requested when the test proxy is enabled and test mode is LIVE.
     */
    public HttpClient getPlaybackClient() {
        if (!isPlaybackMode()) {
            throw new IllegalStateException("A playback client can only be requested in PLAYBACK mode.");
        }
        if (testProxyPlaybackClient == null) {
            testProxyPlaybackClient = new TestProxyPlaybackClient(httpClient, skipRecordingRequestBody);
            proxyVariableQueue.addAll(testProxyPlaybackClient.startPlayback(getTestProxyRecordFile(), testClassPath));
            xRecordingFileLocation = testProxyPlaybackClient.getRecordingFileLocation();
        }
        return testProxyPlaybackClient;
    }

    /**
     * Disposes of resources used by this InterceptorManager.
     * <p>
     * If {@code testMode} is {@link TestMode#RECORD}, all the network calls are persisted to:
     * "<i>session-records/{@code testName}.json</i>"
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        if (allowedToRecordValues) {
            testProxyRecordPolicy.stopRecording(proxyVariableQueue);
        } else if (isPlaybackMode() && allowedToReadRecordedValues) {
            testProxyPlaybackClient.stopPlayback();
        }
    }

    private HttpPipelinePolicy getProxyRecordingPolicy() {
        if (testProxyRecordPolicy == null) {
            if (!isRecordMode()) {
                throw new IllegalStateException("A recording policy can only be requested in RECORD mode.");
            }
            testProxyRecordPolicy = new TestProxyRecordPolicy(httpClient, skipRecordingRequestBody);
            testProxyRecordPolicy.startRecording(getTestProxyRecordFile(), testClassPath);
        }
        return testProxyRecordPolicy;
    }

    /**
     * Computes the relative path of the record file to the repo root.
     *
     * @return A {@link File} with the partial path to where the record file lives.
     */
    private File getTestProxyRecordFile() {
        Path repoRoot = TestUtils.getRepoRootResolveUntil(testClassPath, "eng");
        Path targetFolderRoot = TestUtils.getRepoRootResolveUntil(testClassPath, "target");
        Path filePath = Paths.get(targetFolderRoot.toString(), "src/test/resources/session-records",
            playbackRecordName + ".json");
        return repoRoot.relativize(filePath).toFile();
    }

    /**
     * Add sanitizer rule for sanitization during record or playback.
     *
     * @param testProxySanitizers the list of replacement regex and rules.
     * @throws RuntimeException Neither playback nor record has started.
     * @throws IOException If an error occurs while sending the request.
     */
    public void addSanitizers(List<TestProxySanitizer> testProxySanitizers) throws IOException {
        if (CoreUtils.isNullOrEmpty(testProxySanitizers)) {
            return;
        }
        if (testProxyPlaybackClient != null) {
            testProxyPlaybackClient.addProxySanitization(testProxySanitizers);
        } else if (testProxyRecordPolicy != null) {
            testProxyRecordPolicy.addProxySanitization(testProxySanitizers);
        } else {
            throw new RuntimeException("Playback or record must have been started before adding sanitizers.");
        }
    }

    /**
     * Disable common sanitizer rule for sanitization during record or playback.
     *
     * @param testProxySanitizersId the list of sanitizer rule ID to disable.
     * @throws RuntimeException Neither playback nor record has started.
     * @throws IOException If an error occurs while sending the request.
     */
    public void removeSanitizers(String... testProxySanitizersId) throws IOException {
        if (CoreUtils.isNullOrEmpty(testProxySanitizersId)) {
            return;
        }
        if (testProxyPlaybackClient != null) {
            testProxyPlaybackClient.removeProxySanitization(Arrays.asList(testProxySanitizersId));
        } else if (testProxyRecordPolicy != null) {
            testProxyRecordPolicy.removeProxySanitization(Arrays.asList(testProxySanitizersId));
        } else {
            throw new RuntimeException("Playback or record must have been started before removing sanitizers.");
        }
    }

    /**
     * Add sanitizer rule for sanitization during record or playback.
     *
     * @param testProxySanitizers the list of replacement regex and rules.
     * @throws IOException If an error occurs while sending the request.
     */
    public void addSanitizers(TestProxySanitizer... testProxySanitizers) throws IOException {
        if (testProxySanitizers != null) {
            addSanitizers(Arrays.asList(testProxySanitizers));
        }
    }

    /**
     * Add matcher rules to match recorded data in playback.
     * Matchers are only applied for playback session and so this will be a noop when invoked in RECORD/LIVE mode.
     *
     * @param testProxyMatchers the list of matcher rules when playing back recorded data.
     * @throws RuntimeException Playback has not started.
     * @throws IOException If an error occurs while sending the request.
     */
    public void addMatchers(List<TestProxyRequestMatcher> testProxyMatchers) throws IOException {
        if (CoreUtils.isNullOrEmpty(testProxyMatchers)) {
            return;
        }

        if (testMode != TestMode.PLAYBACK) {
            return;
        }
        if (testProxyPlaybackClient != null) {
            testProxyPlaybackClient.addMatcherRequests(testProxyMatchers);
        } else {
            throw new RuntimeException("Playback must have been started before adding matchers.");
        }
    }

    /**
     * Add matcher rules to match recorded data in playback.
     * Matchers are only applied for playback session and so this will be a noop when invoked in RECORD/LIVE mode.
     *
     * @param testProxyRequestMatchers the list of matcher rules when playing back recorded data.
     * @throws IOException If an error occurs while sending the request.
     */
    public void addMatchers(TestProxyRequestMatcher... testProxyRequestMatchers) throws IOException {
        if (testProxyRequestMatchers != null) {
            addMatchers(Arrays.asList(testProxyRequestMatchers));
        }
    }

    /**
     * Get the recording file location in assets repo.
     *
     * @return the assets repo location of the recording file.
     */
    public String getRecordingFileLocation() {
        return xRecordingFileLocation;
    }

    /**
     * Sets the httpClient to be used for this test.
     *
     * @param httpClient The {@link HttpClient} implementation to use.
     */
    void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Sets the recording options for the proxy.
     *
     * @param testProxyRecordingOptions The {@link TestProxyRecordingOptions} to use.
     * @throws RuntimeException if test mode is not record.
     */
    public void setProxyRecordingOptions(TestProxyRecordingOptions testProxyRecordingOptions) {
        if (testMode != TestMode.RECORD) {
            return;
        }
        if (testProxyRecordPolicy != null) {
            testProxyRecordPolicy.setRecordingOptions(testProxyRecordingOptions);
        } else {
            throw new RuntimeException("Recording must have been started before setting recording options.");
        }
    }
}
