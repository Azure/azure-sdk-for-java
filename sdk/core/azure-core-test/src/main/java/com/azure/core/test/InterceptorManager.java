// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.test;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.http.PlaybackClient;
import com.azure.core.test.models.NetworkCallRecord;
import com.azure.core.test.models.RecordedData;
import com.azure.core.test.policy.RecordNetworkCallPolicy;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A class that keeps track of network calls by either reading the data from an existing test session record or
 * recording the network calls in memory. Test session records are saved or read from:
 * "<i>session-records/{@code testName}.json</i>"
 *
 * <ul>
 *     <li>If the {@code testMode} is {@link TestMode#PLAYBACK}, the manager tries to find an existing test session
 *     record to read network calls from.</li>
 *     <li>If the {@code testMode} is {@link TestMode#RECORD}, the manager creates a new test session record and saves
 *     all the network calls to it.</li>
 * </ul>
 *
 * When the {@link InterceptorManager} is disposed, if the {@code testMode} is {@link TestMode#RECORD}, the network
 * calls that were recorded are persisted to: "<i>session-records/{@code testName}.json</i>"
 */
public class InterceptorManager implements AutoCloseable {
    private static final String RECORD_FOLDER = "session-records/";

    private final ClientLogger logger = new ClientLogger(InterceptorManager.class);
    private final Map<String, String> textReplacementRules;
    private final String testName;
    private final TestMode testMode;

    // Stores a map of all the HTTP properties in a session
    // A state machine ensuring a test is always reset before another one is setup
    private final RecordedData recordedData;

    /**
     * Creates a new InterceptorManager that either replays test-session records or saves them.
     *
     * <ul>
     *     <li>If {@code testMode} is {@link TestMode#PLAYBACK}, the manager tries to find an existing test session
     *     record to read network calls from.</li>
     *     <li>If {@code testMode} is {@link TestMode#RECORD}, the manager creates a new test session record and saves
     *     all the network calls to it.</li>
     * </ul>
     *
     * The test session records are persisted in the path: "<i>session-records/{@code testName}.json</i>"
     *
     * @param testName Name of the test session record.
     * @param testMode The {@link TestMode} for this interceptor.
     * @throws IOException If {@code testMode} is {@link TestMode#PLAYBACK} and an existing test session record could
     * not be located or the data could not be deserialized into an instance of {@link RecordedData}.
     * @throws NullPointerException If {@code testName} is {@code null}.
     */
    public InterceptorManager(String testName, TestMode testMode) throws IOException {
        Objects.requireNonNull(testName);

        this.testName = testName;
        this.testMode = testMode;
        this.textReplacementRules = new HashMap<>();

        this.recordedData = testMode == TestMode.PLAYBACK
            ? readDataFromFile()
            : new RecordedData();
    }

    /**
     * Creates a new InterceptorManager that replays test session records. It takes a set of
     * {@code textReplacementRules}, that can be used by {@link PlaybackClient} to replace values in a
     * {@link NetworkCallRecord#getResponse()}.
     *
     * The test session records are read from: "<i>session-records/{@code testName}.json</i>"
     *
     * @param testName Name of the test session record.
     * @param textReplacementRules A set of rules to replace text in {@link NetworkCallRecord#getResponse()} when playing
     * back network calls.
     * @throws IOException An existing test session record could not be located or the data could not be deserialized
     * into an instance of {@link RecordedData}.
     * @throws NullPointerException If {@code testName} or {@code textReplacementRules} is {@code null}.
     */
    public InterceptorManager(String testName, Map<String, String> textReplacementRules) throws IOException {
        Objects.requireNonNull(testName);
        Objects.requireNonNull(textReplacementRules);

        this.testName = testName;
        this.testMode = TestMode.PLAYBACK;

        this.recordedData = readDataFromFile();
        this.textReplacementRules = textReplacementRules;
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
     * Gets the recorded data InterceptorManager is keeping track of.
     *
     * @return The recorded data managed by InterceptorManager.
     */
    public RecordedData getRecordedData() {
        return recordedData;
    }

    /**
     * Gets a new HTTP pipeline policy that records network calls and its data is managed by {@link InterceptorManager}.
     *
     * @return HttpPipelinePolicy to record network calls.
     */
    public HttpPipelinePolicy getRecordPolicy() {
        return new RecordNetworkCallPolicy(recordedData);
    }

    /**
     * Gets a new HTTP client that plays back test session records managed by {@link InterceptorManager}.
     *
     * @return An HTTP client that plays back network calls from its recorded data.
     */
    public HttpClient getPlaybackClient() {
        return new PlaybackClient(recordedData, textReplacementRules);
    }

    /**
     * Disposes of resources used by this InterceptorManager.
     *
     * If {@code testMode} is {@link TestMode#RECORD}, all the network calls are persisted to:
     * "<i>session-records/{@code testName}.json</i>"
     */
    @Override
    public void close() {
        switch (testMode) {
            case RECORD:
                try {
                    writeDataToFile();
                } catch (IOException e) {
                    logger.error("Unable to write data to playback file.", e);
                }
                break;
            case PLAYBACK:
                // Do nothing
                break;
            default:
                logger.error("==> Unknown AZURE_TEST_MODE: {}", testMode);
                break;
        }
    }

    private RecordedData readDataFromFile() throws IOException {
        File recordFile = getRecordFile(testName);
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        return mapper.readValue(recordFile, RecordedData.class);
    }

    private void writeDataToFile() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        File recordFile = getRecordFile(testName);

        if (recordFile.createNewFile()) {
            logger.verbose("Created record file: {}", recordFile.getPath());
        }

        mapper.writeValue(recordFile, recordedData);
    }

    private File getRecordFile(String testName) {
        URL folderUrl = InterceptorManager.class.getClassLoader().getResource(".");
        File folderFile = new File(folderUrl.getPath() + RECORD_FOLDER);

        if (!folderFile.exists()) {
            if (folderFile.mkdir()) {
                logger.verbose("Created directory: {}", folderFile.getPath());
            }
        }

        String filePath = folderFile.getPath() + "/" + testName + ".json";
        logger.info("==> Playback file path: " + filePath);

        return new File(filePath);
    }

    /**
     * Add text replacement rule (regex as key, the replacement text as value) into {@link InterceptorManager#textReplacementRules}
     *
     * @param regex the pattern to locate the position of replacement
     * @param replacement the replacement text
     */
    public void addTextReplacementRule(String regex, String replacement) {
        textReplacementRules.put(regex, replacement);
    }
}
