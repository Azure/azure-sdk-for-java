// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.models;

import java.util.LinkedList;

/**
 * Keeps track of the network calls that were made in a test session.
 */
public class RecordedData {
    private final LinkedList<NetworkCallRecord> networkCallRecords;
    private final LinkedList<String> variables;

    public RecordedData() {
        networkCallRecords = new LinkedList<>();
        variables = new LinkedList<>();
    }

    /**
     * Gets the network calls made during the test session.
     *
     * @return The network calls made during the session.
     */
    public LinkedList<NetworkCallRecord> getNetworkCallRecords() {
        return networkCallRecords;
    }

    /**
     * Gets the variables associated with this session.
     *
     * @return The variables used in the test session.
     */
    public LinkedList<String> getVariables() {
        return variables;
    }
}
