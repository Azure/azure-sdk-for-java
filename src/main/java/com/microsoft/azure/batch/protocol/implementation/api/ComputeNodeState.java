/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for ComputeNodeState.
 */
public enum ComputeNodeState {
    /** Enum value idle. */
    IDLE("idle"),

    /** Enum value rebooting. */
    REBOOTING("rebooting"),

    /** Enum value reimaging. */
    REIMAGING("reimaging"),

    /** Enum value running. */
    RUNNING("running"),

    /** Enum value unusable. */
    UNUSABLE("unusable"),

    /** Enum value creating. */
    CREATING("creating"),

    /** Enum value starting. */
    STARTING("starting"),

    /** Enum value waitingforstarttask. */
    WAITINGFORSTARTTASK("waitingforstarttask"),

    /** Enum value starttaskfailed. */
    STARTTASKFAILED("starttaskfailed"),

    /** Enum value unknown. */
    UNKNOWN("unknown"),

    /** Enum value leavingpool. */
    LEAVINGPOOL("leavingpool"),

    /** Enum value offline. */
    OFFLINE("offline");

    /** The actual serialized value for a ComputeNodeState instance. */
    private String value;

    ComputeNodeState(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a ComputeNodeState instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a ComputeNodeState instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed ComputeNodeState object, or null if unable to parse.
     */
    @JsonCreator
    public static ComputeNodeState fromValue(String value) {
        ComputeNodeState[] items = ComputeNodeState.values();
        for (ComputeNodeState item : items) {
            if (item.toValue().equals(value)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return toValue();
    }
}
