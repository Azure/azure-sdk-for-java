// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Keeps track of the network calls and variable names that were made in a test session.
 */
public class RecordedData {
    @JsonProperty()
    private final LinkedList<NetworkCallRecord> networkCallRecords;
    @JsonProperty()
    private final LinkedList<String> variables;

    /**
     * Creates a new instance of RecordedData to manage network calls and variables in a test session.
     */
    public RecordedData() {
        networkCallRecords = new LinkedList<>();
        variables = new LinkedList<>();
    }

    /**
     * Finds the first matching {@link NetworkCallRecord} based on the predicate, removes it from the current network
     * calls, and returns it. {@code null} is returned if network call could not be matched.
     *
     * @param isMatch Predicate to match for a given network call.
     * @return The first {@link NetworkCallRecord} that matched {@code isMatch}, otherwise {@code null} if no network
     * call could be matched.
     */
    public NetworkCallRecord findFirstAndRemoveNetworkCall(Predicate<NetworkCallRecord> isMatch) {
        Objects.requireNonNull(isMatch, "'isMatch' cannot be null.");

        synchronized (networkCallRecords) {
            Iterator<NetworkCallRecord> iterator = networkCallRecords.iterator();
            while (iterator.hasNext()) {
                NetworkCallRecord next = iterator.next();
                if (isMatch.test(next)) {
                    iterator.remove();
                    return next;
                }
            }
        }

        return null;
    }

    /**
     * Adds a network call to the end of the list.
     *
     * @param record The record to add.
     */
    public void addNetworkCall(NetworkCallRecord record) {
        Objects.requireNonNull(record, "'record' cannot be null.");

        synchronized (networkCallRecords) {
            networkCallRecords.add(record);
        }
    }

    /**
     * Adds a variable to the end of the list.
     *
     * @param variable The variable to add to the list.
     */
    public void addVariable(String variable) {
        Objects.requireNonNull(variable, "'variable' cannot be null.");

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
