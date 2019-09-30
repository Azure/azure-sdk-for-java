// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.storage.common.Constants;
import com.azure.storage.common.Utility;

import java.nio.charset.StandardCharsets;

public class FileExtensions {

    /**
     * Verifies that the file permission and file permission key are not both set and if the file permission is set,
     * the file permission is of valid length.
     * @param filePermission The file permission.
     * @param filePermissionKey The file permission key.
     * @throws IllegalArgumentException for invalid file permission or file permission keys.
     */
    public static void filePermissionAndKeyHelper(String filePermission, String  filePermissionKey) {
        if (filePermission != null && filePermissionKey != null) {
            throw new IllegalArgumentException(
                FileConstants.MessageConstants.FILE_PERMISSION_FILE_PERMISSION_KEY_INVALID);
        }

        if (filePermission != null) {
            Utility.assertInBounds("filePermission",
                filePermission.getBytes(StandardCharsets.UTF_8).length, 0, 8 * Constants.KB);
        }
    }
}
