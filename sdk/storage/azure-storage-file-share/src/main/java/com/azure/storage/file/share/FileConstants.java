// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

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

    static final class HeaderConstants {
        /**
         * Header that specifies file permission key.
         */
        public static final String FILE_PERMISSION_KEY = "x-ms-file-permission-key";

        /**
         * Header that specifies file attributes.
         */
        public static final String FILE_ATTRIBUTES = "x-ms-file-attributes";

        /**
         * Header that specifies file creation time.
         */
        public static final String FILE_CREATION_TIME = "x-ms-file-creation-time";

        /**
         *  Header that specifies file last write time.
         */
        public static final String FILE_LAST_WRITE_TIME = "x-ms-file-last-write-time";

        /**
         *  Header that specifies file change time.
         */
        public static final String FILE_CHANGE_TIME = "x-ms-file-change-time";

        /**
         * Header that specifies file id.
         */
        public static final String FILE_ID = "x-ms-file-id";

        /**
         * Header that spcifies file parent id.
         */
        public static final String FILE_PARENT_ID = "x-ms-file-parent-id";

        private HeaderConstants() {
        }
    }

    static final class MessageConstants {
        public static final String FILE_PERMISSION_FILE_PERMISSION_KEY_INVALID =
            "File permission and file permission key cannot both be set";

        private MessageConstants() {
        }
    }
}

