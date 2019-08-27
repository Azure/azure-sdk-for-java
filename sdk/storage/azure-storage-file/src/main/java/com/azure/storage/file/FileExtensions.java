package com.azure.storage.file;

import com.azure.storage.common.Constants;
import com.azure.storage.common.Utility;

public class FileExtensions {
    public static void filePermissionAndKeyHelper(String filePermission, String  filePermissionKey) {
        if (filePermission != null && filePermissionKey != null) {
            // TODO: (gapra) Once SR is in common, make this a String there
            throw new IllegalArgumentException("File permission and file permission key cannot both be set");
        }

        if (filePermission != null) {
            Utility.assertInBounds("filePermission", filePermission.getBytes().length, 0, 8* Constants.KB);
        }
    }
}
