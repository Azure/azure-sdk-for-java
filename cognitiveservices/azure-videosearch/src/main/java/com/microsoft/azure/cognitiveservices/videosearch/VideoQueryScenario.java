/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.videosearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for VideoQueryScenario.
 */
public enum VideoQueryScenario {
    /** Enum value List. */
    LIST("List"),

    /** Enum value SingleDominantVideo. */
    SINGLE_DOMINANT_VIDEO("SingleDominantVideo");

    /** The actual serialized value for a VideoQueryScenario instance. */
    private String value;

    VideoQueryScenario(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a VideoQueryScenario instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed VideoQueryScenario object, or null if unable to parse.
     */
    @JsonCreator
    public static VideoQueryScenario fromString(String value) {
        VideoQueryScenario[] items = VideoQueryScenario.values();
        for (VideoQueryScenario item : items) {
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
