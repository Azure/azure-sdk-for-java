// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for CallState.
 */
public enum CallState {
    /** Unknown not recognized. */
    UNKNOWN("Unknown"),

    /** Initial State. */
    IDLE("Idle"),

    /** The call has just been received. */
    INCOMING("Incoming"),

    /** The call establishment is in progress after initiating or accepting the call. */
    ESTABLISHING("Establishing"),

    /** The call is established. */
    ESTABLISHED("Established"),

    /** The call is on Hold. */
    HOLD("Hold"),

    /** The call is Unhold. */
    UNHOLD("Unhold"),

    /** The call has initiated a transfer. */
    TRANSFERRING("Transferring"),

    /** The call has initiated a redirection. */
    REDIRECTING("Redirecting"),

    /** The call is terminating. */
    TERMINATING("Terminating"),

    /** The call has terminated. */
    TERMINATED("Terminated");

    /** The actual serialized value for a CallState instance. */
    private String value;

    CallState(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a CallState instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed CallState object, or null if unable to parse.
     */
    @JsonCreator
    public static CallState fromString(String value) {
        CallState[] items = CallState.values();
        for (CallState item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
