/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.computervision;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for ComputerVisionErrorCodes.
 */
public enum ComputerVisionErrorCodes {
    /** Enum value InvalidImageUrl. */
    INVALID_IMAGE_URL("InvalidImageUrl"),

    /** Enum value InvalidImageFormat. */
    INVALID_IMAGE_FORMAT("InvalidImageFormat"),

    /** Enum value InvalidImageSize. */
    INVALID_IMAGE_SIZE("InvalidImageSize"),

    /** Enum value NotSupportedVisualFeature. */
    NOT_SUPPORTED_VISUAL_FEATURE("NotSupportedVisualFeature"),

    /** Enum value NotSupportedImage. */
    NOT_SUPPORTED_IMAGE("NotSupportedImage"),

    /** Enum value InvalidDetails. */
    INVALID_DETAILS("InvalidDetails"),

    /** Enum value NotSupportedLanguage. */
    NOT_SUPPORTED_LANGUAGE("NotSupportedLanguage"),

    /** Enum value BadArgument. */
    BAD_ARGUMENT("BadArgument"),

    /** Enum value FailedToProcess. */
    FAILED_TO_PROCESS("FailedToProcess"),

    /** Enum value Timeout. */
    TIMEOUT("Timeout"),

    /** Enum value InternalServerError. */
    INTERNAL_SERVER_ERROR("InternalServerError"),

    /** Enum value Unspecified. */
    UNSPECIFIED("Unspecified"),

    /** Enum value StorageException. */
    STORAGE_EXCEPTION("StorageException");

    /** The actual serialized value for a ComputerVisionErrorCodes instance. */
    private String value;

    ComputerVisionErrorCodes(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ComputerVisionErrorCodes instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed ComputerVisionErrorCodes object, or null if unable to parse.
     */
    @JsonCreator
    public static ComputerVisionErrorCodes fromString(String value) {
        ComputerVisionErrorCodes[] items = ComputerVisionErrorCodes.values();
        for (ComputerVisionErrorCodes item : items) {
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
