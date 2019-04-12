// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common;

import com.azure.common.models.RecordedData;
import com.azure.common.utils.ResourceNamer;

import java.util.LinkedList;
import java.util.Objects;

/**
 * Provides random string names. If the test mode is {@link TestMode#PLAYBACK}, then names are fetched from
 * {@link RecordedData}. If the test mode is {@link TestMode#RECORD}, then the names are randomly generated and
 * persisted to {@link RecordedData}.
 */
public class TestResourceNamer extends ResourceNamer {
    private final TestMode testMode;
    private final LinkedList<String> variables;

    public TestResourceNamer(String name, RecordedData recordedData, TestMode testMode) {
        super(name);
        Objects.requireNonNull(recordedData);
        this.variables = new LinkedList<>(recordedData.getVariables());
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
            return getVariable();
        } else {
            return setVariable(super.randomName(prefix, maxLen));
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
            return getVariable();
        } else {
            return setVariable(super.randomUuid());
        }
    }

    private String getVariable() {
        synchronized (variables) {
            return variables.remove();
        }
    }

    private String setVariable(String variable) {
        synchronized (variables) {
            variables.add(variable);
        }

        return variable;
    }
}
