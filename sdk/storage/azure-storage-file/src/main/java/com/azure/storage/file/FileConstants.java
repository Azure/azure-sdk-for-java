// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

/**
 * Holds the Constants used for the File Service.
 */
public final class FileConstants {
    /**
     * Default header value for file permission.
     */
    public static final String FILE_PERMISSION_INHERIT = "Inherit";

    /**
     * Default file attribute value for files.
     */
    public static final String FILE_ATTRIBUTES_NONE = "None";

    /**
     * Default file creation and file last write time.
     */
    public static final String FILE_TIME_NOW = "Now";

    /**
     * Default value for several SMB file headers.
     */
    public static final String PRESERVE = "Preserve";

    /**
     * Stores a reference to the date/time pattern expected for File SMB properties
     */
    public static final String SMB_DATE_STRING = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'";

    static final class MessageConstants {
        public static final String FILE_PERMISSION_FILE_PERMISSION_KEY_INVALID = "File permission and file permission key cannot both be set";

        private MessageConstants() {
        }
    }
}

