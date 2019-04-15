// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.test.utils;

import com.azure.common.test.TestMode;
import com.azure.common.test.models.RecordedData;

import java.util.Objects;

/**
 * Provides random string names. If the test mode is {@link TestMode#PLAYBACK}, then names are fetched from
 * {@link RecordedData}. If the test mode is {@link TestMode#RECORD}, then the names are randomly generated and
 * persisted to {@link RecordedData}.
 */
class TestResourceNamer extends ResourceNamer {
    private final TestMode testMode;
    private final RecordedData recordedData;

    TestResourceNamer(String name, TestMode testMode, RecordedData recordedData) {
        super(name);
        Objects.requireNonNull(recordedData);
        this.recordedData = recordedData;
        this.testMode = testMode;
    }

    /**
     * Gets a random name.
     *
     * @param prefix the prefix to be used if possible
     * @param maxLen the max length for the random generated name
     * @return the random name
     */
    @Override
    public String randomName(String prefix, int maxLen) {
        if (testMode == TestMode.PLAYBACK) {
            return recordedData.removeVariable();
        } else {
            String name = super.randomName(prefix, maxLen);
            recordedData.addVariable(name);
            return name;
        }
    }

    /**
     * Gets a random UUID.
     *
     * @return A random UUID.
     */
    @Override
    public String randomUuid() {
        if (testMode == TestMode.PLAYBACK) {
            return recordedData.removeVariable();
        } else {
            String uuid = super.randomUuid();
            recordedData.addVariable(uuid);
            return uuid;
        }
    }
}
