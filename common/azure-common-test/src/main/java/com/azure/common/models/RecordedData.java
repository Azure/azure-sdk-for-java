// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedList;
import java.util.Objects;

/**
 * Keeps track of the network calls that were made in a test session.
 */
public class RecordedData {
    @JsonProperty()
    private final LinkedList<NetworkCallRecord> networkCallRecords;
    @JsonProperty()
    private final LinkedList<String> variables;

    public RecordedData() {
        networkCallRecords = new LinkedList<>();
        variables = new LinkedList<>();
    }

    /**
     * Adds a network call to the end of the list.
     *
     * @param record The record to add.
     */
    public void addNetworkCall(NetworkCallRecord record) {
        Objects.requireNonNull(record);

        synchronized (networkCallRecords) {
            networkCallRecords.add(record);
        }
    }

    /**
     * Removes a network call from the beginning of the list and returns it.
     *
     * @return The network call that was at the beginning.
     */
    public NetworkCallRecord removeNetworkCall() {
        synchronized (networkCallRecords) {
            return networkCallRecords.remove();
        }
    }

    /**
     * Adds a variable to the end of the list.
     *
     * @param variable The variable to add to the list.
     */
    public void addVariable(String variable) {
        Objects.requireNonNull(variable);

        synchronized (variables) {
            variables.add(variable);
        }
    }

    /**
     * Removes the first variable from the list and returns it.
     *
     * @return The first variable.
     */
    public String removeVariable() {
        synchronized (variables) {
            return variables.remove();
        }
    }
}
