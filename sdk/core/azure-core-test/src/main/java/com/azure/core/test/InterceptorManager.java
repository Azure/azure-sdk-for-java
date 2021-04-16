// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.test;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.http.PlaybackClient;
import com.azure.core.test.models.NetworkCallRecord;
import com.azure.core.test.models.RecordedData;
import com.azure.core.test.models.RecordingRedactor;
import com.azure.core.test.policy.RecordNetworkCallPolicy;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * A class that keeps track of network calls by either reading the data from an existing test session record or
 * recording the network calls in memory. Test session records are saved or read from: "<i>session-records/{@code
 * testName}.json</i>"
 *
 * <ul>
 *     <li>If the {@code testMode} is {@link TestMode#PLAYBACK}, the manager tries to find an existing test session
 *     record to read network calls from.</li>
 *     <li>If the {@code testMode} is {@link TestMode#RECORD}, the manager creates a new test session record and saves
 *     all the network calls to it.</li>
 *     <li>If the {@code testMode} is {@link TestMode#LIVE}, the manager won't attempt to read or create a test session
 *     record.</li>
 * </ul>
 *
 * When the {@link InterceptorManager} is disposed, if the {@code testMode} is {@link TestMode#RECORD}, the network
 * calls that were recorded are persisted to: "<i>session-records/{@code testName}.json</i>"
 */
public class InterceptorManager implements AutoCloseable {
    private static final String RECORD_FOLDER = "session-records/";
    private static final ObjectMapper RECORD_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private final ClientLogger logger = new ClientLogger(InterceptorManager.class);
    private final Map<String, String> textReplacementRules;
    private final String testName;
    private final String playbackRecordName;
    private final TestMode testMode;
    private final boolean allowedToReadRecordedValues;
    private final boolean allowedToRecordValues;

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
     * @throws UncheckedIOException If {@code testMode} is {@link TestMode#PLAYBACK} and an existing test session record
     * could not be located or the data could not be deserialized into an instance of {@link RecordedData}.
     * @throws NullPointerException If {@code testName} is {@code null}.
     * @deprecated Use {@link #InterceptorManager(TestContextManager)} instead.
     */
    @Deprecated
    public InterceptorManager(String testName, TestMode testMode) {
        this(testName, testName, testMode, false);
    }

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
     * @throws UncheckedIOException If {@code testMode} is {@link TestMode#PLAYBACK} and an existing test session record
     * could not be located or the data could not be deserialized into an instance of {@link RecordedData}.
     * @throws NullPointerException If {@code testName} is {@code null}.
     */
    public InterceptorManager(TestContextManager testContextManager) {
        this(testContextManager.getTestName(), testContextManager.getTestPlaybackRecordingName(),
            testContextManager.getTestMode(), testContextManager.doNotRecordTest());
    }

    private InterceptorManager(String testName, String playbackRecordName, TestMode testMode, boolean doNotRecord) {
        Objects.requireNonNull(testName, "'testName' cannot be null.");

        this.testName = testName;
        this.playbackRecordName = CoreUtils.isNullOrEmpty(playbackRecordName) ? testName : playbackRecordName;
        this.testMode = testMode;
        this.textReplacementRules = new HashMap<>();

        this.allowedToReadRecordedValues = (testMode == TestMode.PLAYBACK && !doNotRecord);
        this.allowedToRecordValues = (testMode == TestMode.RECORD && !doNotRecord);

        if (allowedToReadRecordedValues) {
            this.recordedData = readDataFromFile();
        } else if (allowedToRecordValues) {
            this.recordedData = new RecordedData();
        } else {
            this.recordedData = null;
        }
    }

    /**
     * Creates a new InterceptorManager that replays test session records. It takes a set of {@code
     * textReplacementRules}, that can be used by {@link PlaybackClient} to replace values in a {@link
     * NetworkCallRecord#getResponse()}.
     *
     * The test session records are read from: "<i>session-records/{@code testName}.json</i>"
     *
     * @param testName Name of the test session record.
     * @param textReplacementRules A set of rules to replace text in {@link NetworkCallRecord#getResponse()} when
     * playing back network calls.
     * @throws UncheckedIOException An existing test session record could not be located or the data could not be
     * deserialized into an instance of {@link RecordedData}.
     * @throws NullPointerException If {@code testName} or {@code textReplacementRules} is {@code null}.
     * @deprecated Use {@link #InterceptorManager(String, Map, boolean)} instead.
     */
    @Deprecated
    public InterceptorManager(String testName, Map<String, String> textReplacementRules) {
        this(testName, textReplacementRules, false, testName);
    }

    /**
     * Creates a new InterceptorManager that replays test session records. It takes a set of {@code
     * textReplacementRules}, that can be used by {@link PlaybackClient} to replace values in a {@link
     * NetworkCallRecord#getResponse()}.
     *
     * The test session records are read from: "<i>session-records/{@code testName}.json</i>"
     *
     * @param testName Name of the test session record.
     * @param textReplacementRules A set of rules to replace text in {@link NetworkCallRecord#getResponse()} when
     * playing back network calls.
     * @param doNotRecord Flag indicating whether network calls should be record or played back.
     * @throws UncheckedIOException An existing test session record could not be located or the data could not be
     * deserialized into an instance of {@link RecordedData}.
     * @throws NullPointerException If {@code testName} or {@code textReplacementRules} is {@code null}.
     * @deprecated Use {@link #InterceptorManager(String, Map, boolean, String)} instead.
     */
    @Deprecated
    public InterceptorManager(String testName, Map<String, String> textReplacementRules, boolean doNotRecord) {
        this(testName, textReplacementRules, doNotRecord, testName);
    }

    /**
     * Creates a new InterceptorManager that replays test session records. It takes a set of {@code
     * textReplacementRules}, that can be used by {@link PlaybackClient} to replace values in a {@link
     * NetworkCallRecord#getResponse()}.
     *
     * The test session records are read from: "<i>session-records/{@code testName}.json</i>"
     *
     * @param testName Name of the test.
     * @param textReplacementRules A set of rules to replace text in {@link NetworkCallRecord#getResponse()} when
     * playing back network calls.
     * @param doNotRecord Flag indicating whether network calls should be record or played back.
     * @param playbackRecordName Full name of the test including its iteration, used as the playback record name.
     * @throws UncheckedIOException An existing test session record could not be located or the data could not be
     * deserialized into an instance of {@link RecordedData}.
     * @throws NullPointerException If {@code testName} or {@code textReplacementRules} is {@code null}.
     */
    public InterceptorManager(String testName, Map<String, String> textReplacementRules, boolean doNotRecord,
        String playbackRecordName) {
        Objects.requireNonNull(testName, "'testName' cannot be null.");
        Objects.requireNonNull(textReplacementRules, "'textReplacementRules' cannot be null.");

        this.testName = testName;
        this.playbackRecordName = CoreUtils.isNullOrEmpty(playbackRecordName) ? testName : playbackRecordName;
        this.testMode = TestMode.PLAYBACK;
        this.allowedToReadRecordedValues = !doNotRecord;
        this.allowedToRecordValues = false;

        this.recordedData = allowedToReadRecordedValues ? readDataFromFile() : null;
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
     * Gets whether this InterceptorManager is in live mode.
     *
     * @return true if the InterceptorManager is in live mode and false otherwise.
     */
    public boolean isLiveMode() {
        return testMode == TestMode.LIVE;
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
     * Gets a new HTTP pipeline policy that records network calls and its data is managed by {@link
     * InterceptorManager}.
     *
     * @return HttpPipelinePolicy to record network calls.
     */
    public HttpPipelinePolicy getRecordPolicy() {
        return getRecordPolicy(Collections.emptyList());
    }

    /**
     * Gets a new HTTP pipeline policy that records network calls. The recorded content is redacted by the given list
     * of redactor functions to hide sensitive information.
     *
     * @param recordingRedactors The custom redactor functions that are applied in addition to the default redactor
     *                           functions defined in {@link RecordingRedactor}.
     * @return {@link HttpPipelinePolicy} to record network calls.
     */
    public HttpPipelinePolicy getRecordPolicy(List<Function<String, String>> recordingRedactors) {
        return new RecordNetworkCallPolicy(recordedData, recordingRedactors);
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
        if (allowedToRecordValues) {
            try (BufferedWriter writer = Files.newBufferedWriter(createRecordFile(playbackRecordName).toPath())) {
                RECORD_MAPPER.writeValue(writer, recordedData);
            } catch (IOException ex) {
                throw logger.logExceptionAsError(
                    new UncheckedIOException("Unable to write data to playback file.", ex));
            }
        }
    }

    private RecordedData readDataFromFile() {
        File recordFile = getRecordFile();

        try (BufferedReader reader = Files.newBufferedReader(recordFile.toPath())) {
            return RECORD_MAPPER.readValue(reader, RecordedData.class);
        } catch (IOException ex) {
            throw logger.logExceptionAsWarning(new UncheckedIOException(ex));
        }
    }

    /*
     * Creates a File which is the session-records folder.
     */
    private File getRecordFolder() {
        URL folderUrl = InterceptorManager.class.getClassLoader().getResource(".");
        return new File(folderUrl.getPath(), RECORD_FOLDER);
    }

    /*
     * Attempts to retrieve the playback file, if it is not found an exception is thrown as playback can't continue.
     */
    private File getRecordFile() {
        File recordFolder = getRecordFolder();
        File playbackFile = new File(recordFolder, playbackRecordName + ".json");
        File oldPlaybackFile = new File(recordFolder, testName + ".json");

        if (!playbackFile.exists() && !oldPlaybackFile.exists()) {
            throw logger.logExceptionAsError(new RuntimeException(String.format(
                "Missing both new and old playback files. Files are %s and %s.", playbackFile.getPath(),
                oldPlaybackFile.getPath())));
        }

        if (playbackFile.exists()) {
            logger.info("==> Playback file path: {}", playbackFile.getPath());
            return playbackFile;
        } else {
            logger.info("==> Playback file path: {}", oldPlaybackFile.getPath());
            return oldPlaybackFile;
        }
    }

    /*
     * Retrieves or creates the file that will be used to store the recorded test values.
     */
    private File createRecordFile(String testName) throws IOException {
        File recordFolder = getRecordFolder();
        if (!recordFolder.exists()) {
            if (recordFolder.mkdir()) {
                logger.verbose("Created directory: {}", recordFolder.getPath());
            }
        }

        File recordFile = new File(recordFolder, testName + ".json");
        if (recordFile.createNewFile()) {
            logger.verbose("Created record file: {}", recordFile.getPath());
        }

        logger.info("==> Playback file path: " + recordFile);
        return recordFile;
    }

    /**
     * Add text replacement rule (regex as key, the replacement text as value) into {@link
     * InterceptorManager#textReplacementRules}
     *
     * @param regex the pattern to locate the position of replacement
     * @param replacement the replacement text
     */
    public void addTextReplacementRule(String regex, String replacement) {
        textReplacementRules.put(regex, replacement);
    }
}
