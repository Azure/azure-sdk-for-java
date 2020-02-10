// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.implementation.util;

import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.file.datalake.DataLakeServiceVersion;

public class TransformUtils {

    public static BlobServiceVersion toBlobServiceVersion(DataLakeServiceVersion version) {
        if (version == null) {
            return null;
        }
        if (DataLakeServiceVersion.V2019_07_07.equals(version.getVersion())) {
            return BlobServiceVersion.V2019_07_07;
        } else if (DataLakeServiceVersion.V2019_02_02.equals(version.getVersion())) {
            return BlobServiceVersion.V2019_02_02;
        }
        return null;
    }
}
