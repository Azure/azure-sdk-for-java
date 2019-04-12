// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.common;

import com.azure.common.http.HttpClient;
import com.azure.common.http.PlaybackClient;
import com.azure.common.models.RecordedData;
import com.azure.common.policy.RecordNetworkCallPolicy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class InterceptorManager implements AutoCloseable {
    private final static String RECORD_FOLDER = "session-records/";

    private final Logger logger = LoggerFactory.getLogger(InterceptorManager.class);
    private final Map<String, String> textReplacementRules = new HashMap<>();
    private final String testName;
    private final TestMode testMode;

    // Stores a map of all the HTTP properties in a session
    // A state machine ensuring a test is always reset before another one is setup
    private final RecordedData recordedData;

    public InterceptorManager(String testName, TestMode testMode) throws IOException {
        this.testName = testName;
        this.testMode = testMode;

        this.recordedData = testMode == TestMode.PLAYBACK
            ? readDataFromFile()
            : new RecordedData();
    }

    public boolean isPlaybackMode() {
        return testMode == TestMode.PLAYBACK;
    }

    public RecordNetworkCallPolicy getRecordPolicy() {
        return new RecordNetworkCallPolicy(recordedData);
    }

    public HttpClient getPlaybackClient() {
        return new PlaybackClient(recordedData, textReplacementRules);
    }

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
                System.out.println("==> Unknown AZURE_TEST_MODE: " + testMode);
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
        recordFile.createNewFile();
        mapper.writeValue(recordFile, recordedData);
    }

    private File getRecordFile(String testName) {
        URL folderUrl = InterceptorManager.class.getClassLoader().getResource(".");
        File folderFile = new File(folderUrl.getPath() + RECORD_FOLDER);
        if (!folderFile.exists()) {
            folderFile.mkdir();
        }
        String filePath = folderFile.getPath() + "/" + testName + ".json";
        logger.info("==> Playback file path: " + filePath);
        return new File(filePath);
    }
}
