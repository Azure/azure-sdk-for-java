/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for FaceAttributeTypes.
 */
public enum FaceAttributeTypes {
    /** Enum value age. */
    AGE("age"),

    /** Enum value gender. */
    GENDER("gender"),

    /** Enum value headPose. */
    HEAD_POSE("headPose"),

    /** Enum value smile. */
    SMILE("smile"),

    /** Enum value facialHair. */
    FACIAL_HAIR("facialHair"),

    /** Enum value glasses. */
    GLASSES("glasses"),

    /** Enum value emotion. */
    EMOTION("emotion"),

    /** Enum value hair. */
    HAIR("hair"),

    /** Enum value makeup. */
    MAKEUP("makeup"),

    /** Enum value occlusion. */
    OCCLUSION("occlusion"),

    /** Enum value accessories. */
    ACCESSORIES("accessories"),

    /** Enum value blur. */
    BLUR("blur"),

    /** Enum value exposure. */
    EXPOSURE("exposure"),

    /** Enum value noise. */
    NOISE("noise");

    /** The actual serialized value for a FaceAttributeTypes instance. */
    private String value;

    FaceAttributeTypes(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a FaceAttributeTypes instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed FaceAttributeTypes object, or null if unable to parse.
     */
    @JsonCreator
    public static FaceAttributeTypes fromString(String value) {
        FaceAttributeTypes[] items = FaceAttributeTypes.values();
        for (FaceAttributeTypes item : items) {
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
