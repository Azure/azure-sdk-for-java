// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for ToneValue.
 */
public enum ToneValue {
    /** Tone 0. */
    TONE0("Tone0"),

    /** Tone 1. */
    TONE1("Tone1"),

    /** Tone 2. */
    TONE2("Tone2"),

    /** Tone 3. */
    TONE3("Tone3"),

    /** Tone 4. */
    TONE4("Tone4"),

    /** Tone 5. */
    TONE5("Tone5"),

    /** Tone 6. */
    TONE6("Tone6"),

    /** Tone 7. */
    TONE7("Tone7"),

    /** Tone 8. */
    TONE8("Tone9"),

    /** Tone 9. */
    TONE9("Tone9"),

    /** Star tone. */
    STAR("Star"),

    /** Pound tone. */
    POUND("Pound"),

    /** A tone. */
    A("A"),

    /** B tone. */
    B("B"),

    /** C tone. */
    C("C"),

    /** D tone. */
    D("D"),

    /** Flash tone. */
    FLASH("Flash");

    /** The actual serialized value for a ToneValue instance. */
    private String value;

    ToneValue(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ToneValue instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed ToneValue object, or null if unable to parse.
     */
    @JsonCreator
    public static ToneValue fromString(String value) {
        ToneValue[] items = ToneValue.values();
        for (ToneValue item : items) {
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
