/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.videosearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for VideoResolution.
 */
public enum VideoResolution {
    /** Enum value All. */
    ALL("All"),

    /** Enum value SD480p. */
    SD480P("SD480p"),

    /** Enum value HD720p. */
    HD720P("HD720p"),

    /** Enum value HD1080p. */
    HD1080P("HD1080p");

    /** The actual serialized value for a VideoResolution instance. */
    private String value;

    VideoResolution(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a VideoResolution instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed VideoResolution object, or null if unable to parse.
     */
    @JsonCreator
    public static VideoResolution fromString(String value) {
        VideoResolution[] items = VideoResolution.values();
        for (VideoResolution item : items) {
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
