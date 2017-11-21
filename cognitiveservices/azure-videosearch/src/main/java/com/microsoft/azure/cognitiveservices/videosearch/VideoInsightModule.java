/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.videosearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for VideoInsightModule.
 */
public enum VideoInsightModule {
    /** Enum value All. */
    ALL("All"),

    /** Enum value RelatedVideos. */
    RELATED_VIDEOS("RelatedVideos"),

    /** Enum value VideoResult. */
    VIDEO_RESULT("VideoResult");

    /** The actual serialized value for a VideoInsightModule instance. */
    private String value;

    VideoInsightModule(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a VideoInsightModule instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed VideoInsightModule object, or null if unable to parse.
     */
    @JsonCreator
    public static VideoInsightModule fromString(String value) {
        VideoInsightModule[] items = VideoInsightModule.values();
        for (VideoInsightModule item : items) {
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
