// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.test.utils;

import com.azure.common.TestMode;
import com.azure.common.models.RecordedData;

import java.util.Objects;

/**
 * The class to contain the common test methods for testing SDK.
 */
public class SdkContext {
    private final ResourceNamer nameGenerator;

    /**
     * Creates an SDK context that keeps track of variable names for the recorded data.
     *
     * <ul>
     *     <li>If the {@code testMode} is {@link TestMode#PLAYBACK}, this will generate random variables by reading from
     *     {@code recordedData}.</li>
     *     <li>If the {@code testMode} is {@link TestMode#RECORD}, this will generate random variables and write them to
     *     {@code recordedData}.</li>
     * </ul>
     *
     * @param testMode The test for this context.
     * @param recordedData The data to persist or read any variables names to or from.
     */
    public SdkContext(TestMode testMode, RecordedData recordedData) {
        Objects.requireNonNull(recordedData);
        nameGenerator =  new TestResourceNamer("", testMode, recordedData);
    }

    /**
     * Gets a random name.
     *
     * @param prefix the prefix to be used if possible
     * @param maxLen the maximum length for the random generated name
     * @return the random name
     */
    public String randomResourceName(String prefix, int maxLen) {
        return nameGenerator.randomName(prefix, maxLen);
    }

    /**
     * Generates the specified number of random resource names with the same prefix.
     * @param prefix the prefix to be used if possible
     * @param maxLen the maximum length for the random generated name
     * @param count the number of names to generate
     * @return random names
     */
    public String[] randomResourceNames(String prefix, int maxLen, int count) {
        String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            names[i] = nameGenerator.randomName(prefix, maxLen);
        }
        return names;
    }
}
